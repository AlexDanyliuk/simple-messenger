<template>
  <div class="chat-view">
    <ChatHead
      :recipient="recipient"
      :recipient-initial="recipientInitial"
      :is-typing="recipientTyping"
      @click="showModal = true"
    />

    <ProfileModal
      :show="showModal"
      :recipient="recipient"
      :recipient-initial="recipientInitial"
      @close="showModal = false"
    />

    <MessageList
      ref="messageList"
      :messages="messages"
      :current-user-id="currentUserId"
      @edit="onEdit"
    />

    <ChatComposer @send="send" @typing="onTyping" />
  </div>
</template>

<script>
import api from '../services/api';
import { connect, subscribe, sendMessage as wsSend, sendTyping, sendRead, sendEdit } from '../services/websocket';
import ChatHead from '../components/chat/ChatHead.vue';
import ProfileModal from '../components/chat/ProfileModal.vue';
import MessageList from '../components/chat/MessageList.vue';
import ChatComposer from '../components/chat/ChatComposer.vue';

export default {
  components: { ChatHead, ProfileModal, MessageList, ChatComposer },

  data() {
    return {
      messages: [],
      currentUserId: null,
      recipientId: null,
      recipient: null,
      subscription: null,
      statusSubscription: null,
      typingSubscription: null,
      messageStatusSubscription: null,
      editSubscription: null,
      showModal: false,
      recipientTyping: false,
      typingClearTimer: null
    };
  },

  computed: {
    recipientInitial() {
      return this.recipient?.username
        ? this.recipient.username.charAt(0).toUpperCase()
        : '?';
    }
  },

  async mounted() {
    try {
      const profileRes = await api.get('/user/profile');
      this.currentUserId = profileRes.data.id;
    } catch {
      this.$router.replace('/login');
      return;
    }

    this.recipientId = Number(this.$route.params.id);

    if (this.currentUserId === this.recipientId) {
      this.$router.replace('/chats');
      return;
    }

    await this.loadRecipient();
    if (!this.recipient) {
      this.$router.replace('/chats');
      return;
    }

    await this.loadMessages();
    await this.initSocket();
    await this.markAsRead();
    this.$nextTick(() => this.$refs.messageList?.scrollDown());
  },

  beforeUnmount() {
    this.subscription?.unsubscribe();
    this.statusSubscription?.unsubscribe();
    this.typingSubscription?.unsubscribe();
    this.messageStatusSubscription?.unsubscribe();
    this.editSubscription?.unsubscribe();
    clearTimeout(this.typingClearTimer);
    if (this.recipientId) sendTyping({ recipientId: this.recipientId, typing: false });
  },

  methods: {
    async loadRecipient() {
      try {
        const res = await api.get(`/user/${this.recipientId}`);
        const d = res.data;
        this.recipient = { ...d, fullName: d.fullName || d.full_name || '' };
      } catch {
        this.recipient = null;
      }
    },

    async loadMessages() {
      const res = await api.get(`/messages/${this.currentUserId}/${this.recipientId}`);
      this.messages = Array.isArray(res.data) ? res.data : [];
      this.$nextTick(() => this.$refs.messageList?.scrollDown());
    },

    async markAsRead() {
      if (!this.recipientId) return;
      sendRead({ senderId: this.recipientId });
    },

    async initSocket() {
      await connect();

      if (!this.statusSubscription) {
        this.statusSubscription = await subscribe('/topic/status', (update) => {
          if (this.recipient && String(this.recipient.id) === String(update.userId)) {
            this.recipient = { ...this.recipient, status: update.status };
          }
        });
      }

      const chatId = [this.currentUserId, this.recipientId].sort().join('_');

      this.typingSubscription = await subscribe(`/topic/typing/${chatId}`, (data) => {
        if (String(data.senderId) === String(this.recipientId)) {
          this.recipientTyping = data.typing;
          clearTimeout(this.typingClearTimer);
          if (data.typing) {
            this.typingClearTimer = setTimeout(() => { this.recipientTyping = false; }, 4000);
          }
        }
      });

      this.subscription = await subscribe(`/topic/chat/${chatId}`, (incoming) => {
        const isThisChat =
          (incoming.senderId === this.currentUserId && incoming.recipientId === this.recipientId) ||
          (incoming.senderId === this.recipientId && incoming.recipientId === this.currentUserId);

        if (isThisChat) {
          this.messages.push(incoming);
          this.$nextTick(() => this.$refs.messageList?.scrollDown());

          if (incoming.senderId === this.recipientId) {
            this.markAsRead();
          }
        }
      });

      this.messageStatusSubscription = await subscribe(`/topic/message-status/${chatId}`, (update) => {
        const now = new Date().toISOString();
        this.messages = this.messages.map(m => {
          if (Number(m.senderId) !== Number(update.senderId)) return m;
          if (update.status === 'DELIVERED' && !m.deliveredAt) {
            return { ...m, deliveredAt: now };
          }
          if (update.status === 'READ') {
            return { ...m, deliveredAt: m.deliveredAt || now, readAt: m.readAt || now };
          }
          return m;
        });
      });

      this.editSubscription = await subscribe(`/topic/chat-edit/${chatId}`, (updated) => {
        this.messages = this.messages.map(m =>
          Number(m.id) === Number(updated.id)
            ? { ...m, content: updated.content, editedAt: updated.editedAt }
            : m
        );
      });
    },

    send(msg) {
      wsSend({ recipientId: this.recipientId, content: msg });
    },

    onTyping(isTyping) {
      sendTyping({ recipientId: this.recipientId, typing: isTyping });
    },
    onEdit(payload) {
      sendEdit(payload);
    }
  },

  watch: {
    '$route.params.id': {
      async handler(newId) {
        if (!newId) return;

        this.subscription?.unsubscribe();
        this.typingSubscription?.unsubscribe();
        this.messageStatusSubscription?.unsubscribe();
        this.editSubscription?.unsubscribe();
        clearTimeout(this.typingClearTimer);
        this.recipientTyping = false;

        this.recipientId = Number(newId);
        this.messages = [];
        this.recipient = null;

        await this.loadRecipient();
        await this.loadMessages();
        await this.initSocket();
        await this.markAsRead();
      }
    }
  }
};
</script>

<style scoped>
.chat-view {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f9f9f9;
}
</style>