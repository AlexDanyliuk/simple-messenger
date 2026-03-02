<template>
  <div ref="list" class="messages">
    <div
      v-for="m in messages"
      :key="m.id || m.timestamp + '-' + m.content"
      class="msg"
      :class="{ own: m.senderId === currentUserId }"
    >
      <div class="bubble">
        {{ m.content }}
        <div class="time">{{ formatTime(m.timestamp) }}</div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MessageList',
  props: {
    messages: { type: Array, default: () => [] },
    currentUserId: { type: [Number, String], default: null }
  },
  methods: {
    formatTime(ts) {
      if (!ts) return '';
      try {
        const d = new Date(ts);
        return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
      } catch {
        return '';
      }
    },
    scrollDown() {
      const el = this.$refs.list;
      if (!el) return;
      el.scrollTop = el.scrollHeight;
    }
  }
};
</script>

<style scoped>
.messages {
  flex: 1;
  padding: 24px 40px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.messages::-webkit-scrollbar {
  width: 3px;
}

.messages::-webkit-scrollbar-thumb {
  background: #e0e0e0;
  border-radius: 4px;
}

.msg {
  display: flex;
  flex-direction: column;
}

.msg.own {
  align-items: flex-end;
}

.msg:not(.own) {
  align-items: flex-start;
}

.bubble {
  max-width: 56%;
  padding: 10px 16px;
  border-radius: 18px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
  position: relative;
}

.msg.own .bubble {
  background: #d0e8ff;
  color: #111111;
  border-bottom-right-radius: 4px;
}

.msg:not(.own) .bubble {
  background: #ffffff;
  color: #111111;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}

.time {
  font-size: 10px;
  color: #aaaaaa;
  margin-top: 4px;
  padding: 0 4px;
}
</style>
