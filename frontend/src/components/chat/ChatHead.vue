<template>
  <div class="chat-head" @click="$emit('click')" :title="t('chatProfileHint')">
    <div class="chat-head__avatar">
      <img v-if="recipient && recipient.avatarUrl" :src="recipient.avatarUrl" class="head-avatar-img" alt="" />
      <span v-else>{{ recipientInitial }}</span>
    </div>
    <div class="chat-head__info">
      <div class="chat-head__title">{{ recipient ? recipient.username : '...' }}</div>
      <transition name="typing-fade" mode="out-in">
        <div v-if="isTyping" class="chat-head__sub typing-indicator" key="typing">
          <span class="dot"></span><span class="dot"></span><span class="dot"></span>
          {{ t("typing") }}
        </div>
        <div
          v-else
          class="chat-head__sub"
          :class="recipient && recipient.status === 'ONLINE' ? 'status-online' : 'status-offline'"
          key="status"
        >
          {{ statusText }}
        </div>
      </transition>
    </div>
    <div class="chat-head__hint">{{ t("chatProfileHint") }}</div>
  </div>
</template>

<script>
import { preferences, t } from "../../services/userPreferences";

export default {
  name: 'ChatHead',
  props: {
    recipient: { type: Object, default: null },
    recipientInitial: { type: String, default: '?' },
    isTyping: { type: Boolean, default: false }
  },
  computed: {
    language() {
      return preferences.language;
    },
    statusText() {
      if (!this.recipient) return "";
      if (this.recipient.status === "ONLINE") return t("online");
      if (this.recipient.lastSeenAt) return `${t("lastSeen")} ${this.formatLastSeen(this.recipient.lastSeenAt)}`;
      return t("offline");
    }
  },
  methods: {
    t,
    formatLastSeen(ts) {
      const d = new Date(ts);
      const now = new Date();
      const diffMs = Math.max(0, now - d);
      const mins = Math.floor(diffMs / 60000);

      if (mins < 1) return t("timeJustNow");
      if (mins < 60) return t("timeMinAgo", { count: mins });

      const hours = Math.floor(mins / 60);
      if (hours < 24) return t("timeHourAgo", { count: hours });

      return d.toLocaleDateString([], { day: "2-digit", month: "2-digit" }) + " " +
        d.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
    }
  },
  emits: ['click']
};
</script>

<style scoped>
.chat-head {
  display: flex;
  align-items: center;
  padding: 0 20px;
  height: 64px;
  background: var(--panel-bg);
  border-bottom: 1px solid var(--border-color);
  flex-shrink: 0;
  cursor: pointer;
  transition: background 0.18s ease, box-shadow 0.18s ease;
  gap: 12px;
}
.chat-head:hover {
  background: var(--panel-bg-hover);
  box-shadow: inset 0 -1px 0 var(--border-color);
}

.chat-head__avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--accent);
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
  box-shadow: var(--shadow-lg);
}

.head-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  border-radius: 50%;
}

.chat-head__info {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.chat-head__title {
  font-weight: 700;
  font-size: 15px;
  color: var(--app-text);
}

.chat-head__sub {
  font-size: 12px;
  color: var(--text-secondary);
}

.status-online  { color: var(--success) !important; }
.status-offline { color: var(--text-tertiary) !important; }

.chat-head__hint {
  font-size: 11px;
  color: var(--text-tertiary);
  white-space: nowrap;
  transition: color 0.18s ease, transform 0.18s ease;
}

.chat-head:hover .chat-head__hint {
  color: var(--text-secondary);
  transform: translateX(2px);
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--success);
  font-size: 12px;
}

.dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #4caf50;
  animation: blink 1.2s infinite;
}
.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes blink {
  0%, 80%, 100% { opacity: 0.2; transform: scale(0.8); }
  40%           { opacity: 1;   transform: scale(1); }
}

.typing-fade-enter-active, .typing-fade-leave-active { transition: opacity 0.15s; }
.typing-fade-enter-from, .typing-fade-leave-to { opacity: 0; }

@media (max-width: 768px) {
  .chat-head {
    padding: 0 16px;
    height: 56px;
    gap: 10px;
  }

  .chat-head__avatar {
    width: 32px;
    height: 32px;
    font-size: 13px;
  }

  .chat-head__title {
    font-size: 14px;
  }

  .chat-head__sub {
    font-size: 11px;
  }

  .chat-head__hint {
    font-size: 10px;
  }
}

@media (max-width: 480px) {
  .chat-head {
    padding: 0 12px;
    height: 52px;
    gap: 8px;
  }

  .chat-head__avatar {
    width: 28px;
    height: 28px;
    font-size: 11px;
  }

  .chat-head__title {
    font-size: 13px;
    font-weight: 500;
  }

  .chat-head__sub {
    font-size: 10px;
  }

  .chat-head__hint {
    display: none;
  }
}
</style>
