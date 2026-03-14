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

    <!-- Pinned messages panel -->
    <div v-if="pinnedMessages.length > 0" class="pinned-panel">
      <div class="pinned-header">
        <span class="pinned-title">📌 Прикріплені повідомлення ({{ pinnedMessages.length }})</span>
      </div>
      <div class="pinned-list">
        <button
          v-for="msg in pinnedMessages"
          :key="msg.id"
          class="pinned-item"
          @click="scrollToMessage(msg.id)"
        >
          <span class="pinned-sender">{{ getSenderName(msg) }}</span>
          <span class="pinned-content">{{ msg.content || '📎 ' + (msg.fileName || 'файл') }}</span>
        </button>
      </div>
    </div>

    <MessageList
      ref="messageList"
      :messages="messages"
      :current-user-id="currentUserId"
      @edit="onEdit"
      @reaction="onReaction"
      @pin="onPin"
      @scroll="onMessageListScroll"
    />

    <!-- Scroll down button -->
    <button v-if="showScrollButton" class="scroll-to-bottom" @click="scrollToBottom" title="До кінця чату">
      <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
        <path d="M7 10l5 5 5-5z"/>
      </svg>
    </button>

    <ChatComposer @send="send" @typing="onTyping" />
  </div>
</template>

<script>
import api from '../services/api';
import { connect, subscribe, sendMessage as wsSend, sendTyping, sendRead, sendEdit, sendReaction, sendPin } from '../services/websocket';
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
      reactionSubscription: null,
      pinSubscription: null,
      showModal: false,
      recipientTyping: false,
      typingClearTimer: null,
      showPinnedPanel: true,
      showScrollButton: false,
      lastScrollHeight: 0
    };
  },

  computed: {
    recipientInitial() {
      return this.recipient?.username
        ? this.recipient.username.charAt(0).toUpperCase()
        : '?';
    },
    pinnedMessages() {
      return this.messages.filter(m => m.pinned);
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
    this.reactionSubscription?.unsubscribe();
    this.pinSubscription?.unsubscribe();
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
            this.recipient = {
              ...this.recipient,
              status: update.status,
              lastSeenAt: update.lastSeenAt || this.recipient.lastSeenAt
            };
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

      this.reactionSubscription = await subscribe(`/topic/chat-reaction/${chatId}`, (updated) => {
        this.messages = this.messages.map(m =>
          Number(m.id) === Number(updated.id)
            ? { ...m, reactions: updated.reactions || {} }
            : m
        );
      });

      this.pinSubscription = await subscribe(`/topic/chat-pin/${chatId}`, (updated) => {
        this.messages = this.messages.map(m =>
          Number(m.id) === Number(updated.id)
            ? { ...m, pinned: updated.pinned }
            : m
        );
      });
    },

    send(payload) {
      // payload is an object with content and optional file data
      const message = {
        recipientId: this.recipientId,
        content: payload.content || ''
      };
      
      if (payload.fileUrl) {
        message.fileUrl = payload.fileUrl;
        message.fileName = payload.fileName;
        message.fileType = payload.fileType;
        message.fileSize = payload.fileSize;
      }
      
      wsSend(message);
    },

    onTyping(isTyping) {
      sendTyping({ recipientId: this.recipientId, typing: isTyping });
    },
    onEdit(payload) {
      sendEdit(payload);
    },
    onReaction(payload) {
      sendReaction(payload);
    },
    onPin(payload) {
      sendPin(payload);
    },

    onMessageListScroll() {
      const el = this.$refs.messageList?.$refs?.list;
      if (!el) return;
      
      const isAtBottom = el.scrollHeight - el.scrollTop - el.clientHeight < 50;
      this.showScrollButton = !isAtBottom;
    },

    scrollToBottom() {
      this.$nextTick(() => {
        this.$refs.messageList?.scrollDown();
        this.showScrollButton = false;
      });
    },

    scrollToMessage(messageId) {
      // Scroll to the message with specific ID
      const el = this.$refs.messageList?.$refs?.list;
      if (!el) return;

      // Find message element by data-message-id attribute
      const foundElement = el.querySelector(`[data-message-id="${messageId}"]`);

      if (foundElement) {
        foundElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
        foundElement.style.backgroundColor = 'rgba(255, 193, 7, 0.15)';
        setTimeout(() => {
          foundElement.style.backgroundColor = '';
        }, 1500);
      }
    },

    getSenderName(msg) {
      if (Number(msg.senderId) === Number(this.currentUserId)) return 'Ви';
      return this.recipient?.username || 'Користувач';
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
        this.reactionSubscription?.unsubscribe();
        this.pinSubscription?.unsubscribe();
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
  background: var(--app-bg);
}

.pinned-panel {
  background: var(--panel-bg);
  border-bottom: 1px solid var(--border-color);
  padding: 8px 16px;
  max-height: 90px;
  overflow: hidden;
}

.pinned-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  font-weight: 600;
  font-size: 12px;
  color: var(--app-text);
}

.pinned-title {
  flex: 1;
}

.pinned-list {
  display: flex;
  flex-direction: row;
  gap: 6px;
  overflow-x: auto;
  overflow-y: hidden;
}

.pinned-list::-webkit-scrollbar {
  height: 3px;
}

.pinned-list::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 2px;
}

.pinned-item {
  background: var(--app-bg);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  padding: 6px 10px;
  text-align: left;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 150px;
  flex-shrink: 0;
}

.pinned-item:hover {
  background: #f5f5f5;
  border-color: var(--accent);
}

@media (prefers-color-scheme: dark) {
  .pinned-item:hover {
    background: #1a1a2e;
  }
}

.pinned-sender {
  font-size: 10px;
  color: var(--text-secondary);
  font-weight: 600;
}

.pinned-content {
  font-size: 12px;
  color: var(--app-text);
  word-break: break-word;
  white-space: normal;
  line-height: 1.3;
  max-height: 30px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
}

.scroll-to-bottom {
  position: absolute;
  bottom: 90px;
  right: 20px;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--accent);
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-md);
  transition: all 0.2s;
  z-index: 50;
}

.scroll-to-bottom:hover {
  transform: scale(1.1);
  box-shadow: var(--shadow-lg);
}
</style>