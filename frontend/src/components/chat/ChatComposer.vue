<template>
  <div class="composer">
    <input
      v-model="text"
      placeholder="Написати повідомлення…"
      @keyup.enter="send"
    />
    <button @click="send">Надіслати</button>
  </div>
</template>

<script>
export default {
  name: 'ChatComposer',
  emits: ['send'],
  data() {
    return { text: '' };
  },
  methods: {
    send() {
      const msg = (this.text || '').trim();
      if (!msg) return;
      this.$emit('send', msg);
      this.text = '';
    }
  }
};
</script>

<style scoped>
.composer {
  display: flex;
  align-items: center;
  padding: 16px 24px;
  background: #ffffff;
  border-top: 1px solid #ebebeb;
  gap: 12px;
  flex-shrink: 0;
}

.composer input {
  flex: 1;
  padding: 11px 18px;
  border-radius: 22px;
  border: 1px solid #e4e4e4;
  background: #f7f7f7;
  font-size: 14px;
  font-family: inherit;
  outline: none;
  transition: all 0.15s;
}

.composer input:focus {
  border-color: #bbbbbb;
  background: #ffffff;
}

.composer button {
  padding: 0;
  background: none;
  border: none;
  color: #111111;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 1px;
  text-transform: uppercase;
  cursor: pointer;
  opacity: 0.9;
  transition: opacity 0.15s;
  white-space: nowrap;
}

.composer button:hover {
  opacity: 0.5;
}
</style>
