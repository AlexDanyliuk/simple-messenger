<template>
  <div class="chat-list">
    <ChatItem
      v-for="chat in chats"
      :key="chat.id"
      :chat="chat"
      @click="$emit('select', chat.id)"
    />
  </div>
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
  background: #ffffff;
}

.list-header {
  padding: 20px 20px 12px;
  border-bottom: 1px solid #ebebeb;
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
  background: #f0f0f0;
}

.header-title {
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 2px;
  color: #111;
  margin: 0;
}

.chat-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.chat-list::-webkit-scrollbar {
  width: 3px;
}

.chat-list::-webkit-scrollbar-track {
  background: transparent;
}

.chat-list::-webkit-scrollbar-thumb {
  background: #e0e0e0;
  border-radius: 4px;
}
</style>