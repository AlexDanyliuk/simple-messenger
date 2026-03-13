<template>
  <div :class="['bubble', isOwn ? 'own' : 'other']">
    {{ message.content }}
  </div>
</template>

<script>
export default {
  props: ["message", "currentUserId"],
  computed: {
    isOwn() {
      return this.message.senderId === this.currentUserId;
    }
  }
};
</script>

<style scoped>
.bubble {
  max-width: 56%;
  padding: 10px 16px;
  margin-bottom: 4px;
  border-radius: 18px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}

.own {
  align-self: flex-end;
  background: var(--msg-own-bg);
  color: var(--msg-own-color);
  border-bottom-right-radius: 4px;
}

.other {
  align-self: flex-start;
  background: var(--msg-other-bg);
  color: var(--msg-other-color);
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}

@media (max-width: 768px) {
  .bubble {
    max-width: 75%;
    padding: 9px 14px;
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .bubble {
    max-width: 85%;
    padding: 8px 12px;
    font-size: 12px;
    border-radius: 14px;
  }

  .own {
    border-bottom-right-radius: 2px;
  }

  .other {
    border-bottom-left-radius: 2px;
  }
}
</style>