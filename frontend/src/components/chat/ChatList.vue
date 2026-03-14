<template>
  <transition-group name="chat-list" tag="div" class="chat-list">
    <ChatItem
      v-for="chat in chats"
      :key="chat.id"
      :chat="chat"
      @click="$emit('select', chat.id)"
    />
  </transition-group>
</template>

<script>
import ChatItem from "./ChatItem.vue";

export default {
  name: "ChatList",
  props: {
    chats: {
      type: Array,
      required: true
    }
  },
  components: { ChatItem }
};
</script>
<style scoped>
.chat-list-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--panel-bg);
}

.list-header {
  padding: 20px 20px 12px;
  border-bottom: 1px solid var(--border-color);
  flex-shrink: 0;
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.header-logo {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-search {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 50%;
  transition: background 0.15s;
}

.header-search:hover {
  background: var(--panel-bg-hover);
}

.header-title {
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 2px;
  color: var(--app-text);
  margin: 0;
}

.chat-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px 10px 14px;
}

.chat-list::-webkit-scrollbar {
  width: 3px;
}

.chat-list::-webkit-scrollbar-track {
  background: transparent;
}

.chat-list::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 4px;
}

.chat-list-enter-active,
.chat-list-leave-active {
  transition: opacity 0.24s ease, transform 0.24s ease;
}

.chat-list-move {
  transition: transform 0.26s ease;
}

.chat-list-enter-from,
.chat-list-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>