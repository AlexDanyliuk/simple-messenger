<template>
  <div class="chat-head" @click="$emit('click')" title="Переглянути профіль">
    <div class="chat-head__avatar">
      <img v-if="recipient && recipient.avatarUrl" :src="recipient.avatarUrl" class="head-avatar-img" alt="" />
      <span v-else>{{ recipientInitial }}</span>
    </div>
    <div class="chat-head__info">
      <div class="chat-head__title">{{ recipient ? recipient.username : '...' }}</div>
      <transition name="typing-fade" mode="out-in">
        <div v-if="isTyping" class="chat-head__sub typing-indicator" key="typing">
          <span class="dot"></span><span class="dot"></span><span class="dot"></span>
          друкує...
        </div>
        <div
          v-else
          class="chat-head__sub"
          :class="recipient && recipient.status === 'ONLINE' ? 'status-online' : 'status-offline'"
          key="status"
        >
          {{ recipient ? (recipient.status === 'ONLINE' ? 'В мережі' : 'Не в мережі') : '' }}
        </div>
      </transition>
    </div>
    <div class="chat-head__hint">переглянути профіль</div>
  </div>
</template>

<script>
export default {
  name: 'ChatHead',
  props: {
    recipient: { type: Object, default: null },
    recipientInitial: { type: String, default: '?' },
    isTyping: { type: Boolean, default: false }
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
  background: linear-gradient(180deg, #ffffff 0%, #fbfdff 100%);
  border-bottom: 1px solid #e8eef5;
  flex-shrink: 0;
  cursor: pointer;
  transition: background 0.18s ease, box-shadow 0.18s ease;
  gap: 12px;
}
.chat-head:hover {
  background: linear-gradient(180deg, #ffffff 0%, #f7fafc 100%);
  box-shadow: inset 0 -1px 0 rgba(148, 163, 184, 0.14);
}

.chat-head__avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0f172a 0%, #334155 100%);
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
  box-shadow: 0 10px 20px rgba(15, 23, 42, 0.18);
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
  color: #0f172a;
}

.chat-head__sub {
  font-size: 12px;
  color: #64748b;
}

.status-online  { color: #4caf50 !important; }
.status-offline { color: #aaaaaa !important; }

.chat-head__hint {
  font-size: 11px;
  color: #94a3b8;
  white-space: nowrap;
  transition: color 0.18s ease, transform 0.18s ease;
}

.chat-head:hover .chat-head__hint {
  color: #64748b;
  transform: translateX(2px);
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #4caf50;
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
