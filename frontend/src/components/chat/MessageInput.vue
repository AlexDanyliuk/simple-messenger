<template>
  <div class="input-area">
    <div class="clip-icon">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none"><path d="M21.44 11.05l-9.19 9.19a6 6 0 01-8.49-8.49l9.19-9.19a4 4 0 015.66 5.66l-9.2 9.19a2 2 0 01-2.83-2.83l8.49-8.48" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
    </div>
    <input
      v-model="text"
      @keyup.enter="send"
      placeholder="Type a message..."
    />
    <button @click="send">SEND</button>
  </div>
</template>

<script>
export default {
  data() {
    return { text: "" };
  },
  methods: {
    send() {
      if (!this.text.trim()) return;
      this.$emit("send", this.text);
      this.text = "";
    }
  }
};
</script>

<style scoped>
.input-area {
  display: flex;
  align-items: center;
  padding: 16px 24px;
  background: var(--panel-bg);
  border-top: 1px solid var(--border-color);
  gap: 12px;
}

.clip-icon {
  color: var(--text-tertiary);
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: color 0.15s;
  flex-shrink: 0;
}

.clip-icon:hover {
  color: var(--text-secondary);
}

input {
  flex: 1;
  padding: 11px 18px;
  border-radius: 22px;
  border: 1px solid var(--border-color);
  background: var(--panel-bg-soft);
  font-size: 14px;
  font-family: inherit;
  outline: none;
  transition: all 0.15s;
  color: var(--app-text);
}

input::placeholder {
  color: var(--text-tertiary);
}

input:focus {
  border-color: var(--accent);
  background: var(--panel-bg);
}

button {
  padding: 0;
  background: none;
  border: none;
  color: var(--app-text);
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 1px;
  text-transform: uppercase;
  cursor: pointer;
  opacity: 0.85;
  transition: opacity 0.15s;
  white-space: nowrap;
  font-family: inherit;
}

button:hover {
  opacity: 0.4;
}

@media (max-width: 768px) {
  .input-area {
    padding: 12px 18px;
    gap: 10px;
  }

  input {
    padding: 10px 16px;
    font-size: 13px;
  }

  button {
    font-size: 12px;
    letter-spacing: 0.5px;
  }

  .clip-icon svg {
    width: 16px;
    height: 16px;
  }
}

@media (max-width: 480px) {
  .input-area {
    padding: 10px 12px;
    gap: 8px;
  }

  input {
    padding: 9px 14px;
    border-radius: 18px;
    font-size: 12px;
  }

  button {
    font-size: 11px;
    letter-spacing: 0;
  }

  .clip-icon svg {
    width: 15px;
    height: 15px;
  }
}
</style>