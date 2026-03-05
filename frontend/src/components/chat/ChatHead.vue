<template>
  <div class="chat-head" @click="$emit('click')" title="Переглянути профіль">
    <div class="chat-head__avatar">
      <img v-if="recipient && recipient.avatarUrl" :src="recipient.avatarUrl" class="head-avatar-img" alt="" />
      <span v-else>{{ recipientInitial }}</span>
    </div>
    <div class="chat-head__info">
      <div class="chat-head__title">{{ recipient ? recipient.username : '...' }}</div>
      <div
        class="chat-head__sub"
        :class="recipient && recipient.status === 'ONLINE' ? 'status-online' : 'status-offline'"
      >
        {{ recipient ? (recipient.status === 'ONLINE' ? 'В мережі' : 'Не в мережі') : '' }}
      </div>
    </div>
    <div class="chat-head__hint">переглянути профіль</div>
  </div>
</template>

<script>
export default {
  name: 'ChatHead',
  props: {
    recipient: { type: Object, default: null },
    recipientInitial: { type: String, default: '?' }
  },
  emits: ['click']
};
</script>

<style scoped>
.chat-head {
  display: flex;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  background: #ffffff;
  border-bottom: 1px solid #ebebeb;
  flex-shrink: 0;
  cursor: pointer;
  transition: background 0.12s;
  gap: 12px;
}
.chat-head:hover {
  background: #f7f7f7;
}

.chat-head__avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #111;
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
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
  font-weight: 600;
  font-size: 15px;
  color: #111111;
}

.chat-head__sub {
  font-size: 12px;
  color: #aaaaaa;
}

.status-online  { color: #4caf50 !important; }
.status-offline { color: #aaaaaa !important; }

.chat-head__hint {
  font-size: 11px;
  color: #cccccc;
  white-space: nowrap;
}

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
