<template>
  <div class="chat-container">

    <div class="messages">
      <div
        v-for="msg in messages"
        :key="msg.id"
        :class="['message', msg.senderId === currentUserId ? 'own' : 'other']"
      >
        {{ msg.content }}
      </div>
    </div>

    <div class="input-area">
      <input v-model="text" @keyup.enter="send" placeholder="Type message..." />
      <button @click="send">Send</button>
    </div>

  </div>
</template>

<script>
export default {
  props: ["messages", "currentUserId"],
  data() {
    return { text: "" };
  },
  methods: {
    send() {
      if (!this.text.trim()) return;
      this.$emit("sendMessage", this.text);
      this.text = "";
    }
  }
};
</script>

<style scoped>
.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--app-bg);
}

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

.message {
  padding: 10px 16px;
  border-radius: 18px;
  max-width: 56%;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}

.own {
  background: var(--msg-own-bg);
  color: var(--msg-own-color);
  align-self: flex-end;
  border-bottom-right-radius: 4px;
}

.other {
  background: var(--msg-other-bg);
  color: var(--msg-other-color);
  align-self: flex-start;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}

.input-area {
  display: flex;
  align-items: center;
  padding: 16px 24px;
  background: var(--panel-bg);
  border-top: 1px solid var(--border-color);
  gap: 12px;
}

input {
  flex: 1;
  padding: 11px 18px;
  border-radius: 22px;
  border: 1px solid var(--border-color);
  background: var(--panel-soft);
  font-size: 14px;
  font-family: inherit;
  outline: none;
  transition: all 0.15s;
  color: var(--app-text);
}

input::placeholder {
  color: #bbbbbb;
}

input:focus {
  border-color: #bbbbbb;
  background: var(--panel-bg);
}

button {
  background: none;
  border: none;
  color: #111111;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 1px;
  text-transform: uppercase;
  cursor: pointer;
  font-family: inherit;
  opacity: 0.85;
  transition: opacity 0.15s;
}

button:hover {
  opacity: 0.4;
}
</style>