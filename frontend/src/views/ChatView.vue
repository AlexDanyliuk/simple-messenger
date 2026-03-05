<template>
  <div class="chat-view">
    <ChatHead
      :recipient="recipient"
      :recipient-initial="recipientInitial"
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
    />

    <ChatComposer @send="send" />
  </div>
</template>

<script>
import api from '../services/api';
import { connect, subscribe, sendMessage as wsSend } from '../services/websocket';
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
      showModal: false
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

    // Перевірка: не дозволяємо чат з собою
    if (this.currentUserId === this.recipientId) {
      this.$router.replace('/chats');
      return;
    }

    await this.loadRecipient();
    
    // Якщо користувач не існує - перенаправляємо на /chats
    if (!this.recipient) {
      this.$router.replace('/chats');
      return;
    }

    await this.loadMessages();
    await this.markAsRead();
    this.initSocket();
    this.$nextTick(() => this.$refs.messageList?.scrollDown());
  },

  beforeUnmount() {
    this.subscription?.unsubscribe();
    this.statusSubscription?.unsubscribe();
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

    // позначає всі повідомлення від recipientId до currentUserId як прочитані
    async markAsRead() {
      try {
        await api.post(`/messages/${this.recipientId}/${this.currentUserId}/read`);
      } catch {
        // ігноруємо помилку якщо немає повідомлень
      }
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

      this.subscription = await subscribe(`/topic/chat/${chatId}`, (incoming) => {
        const isThisChat =
          (incoming.senderId === this.currentUserId && incoming.recipientId === this.recipientId) ||
          (incoming.senderId === this.recipientId && incoming.recipientId === this.currentUserId);

        if (isThisChat) {
          this.messages.push(incoming);
          this.$nextTick(() => this.$refs.messageList?.scrollDown());

          // якщо нове повідомлення від співрозмовника — одразу позначаємо як прочитане
          if (incoming.senderId === this.recipientId) {
            this.markAsRead();
          }
        }
      });
    },

    send(msg) {
      wsSend({ recipientId: this.recipientId, content: msg });
    }
  },

  watch: {
    '$route.params.id': {
      async handler(newId) {
        if (!newId) return;

        this.subscription?.unsubscribe();

        this.recipientId = Number(newId);
        this.messages = [];
        this.recipient = null;

        await this.loadRecipient();
        await this.loadMessages();
        await this.markAsRead();
        this.initSocket();
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