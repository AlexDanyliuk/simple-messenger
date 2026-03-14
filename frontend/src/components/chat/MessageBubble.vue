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
  padding: 10px 14px;
  margin-bottom: 6px;
  border-radius: 12px;
  font-size: 15px;
  line-height: 1.5;
  word-break: break-word;
  animation: fadeInUp 0.2s ease-out;
  transition: all 0.2s ease;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.own {
  align-self: flex-end;
  background: var(--msg-own-bg);
  color: var(--msg-own-color);
  border-bottom-right-radius: 4px;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.25);
}

.own:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.other {
  align-self: flex-start;
  background: var(--msg-other-bg);
  color: var(--msg-other-color);
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.other:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
}

html[data-theme="dark"] .bubble {
  transition: all 0.2s ease;
}

@media (max-width: 768px) {
  .bubble {
    max-width: 70%;
    padding: 9px 12px;
    font-size: 14px;
    border-radius: 10px;
  }
}

@media (max-width: 480px) {
  .bubble {
    max-width: 80%;
    padding: 8px 12px;
    font-size: 13px;
    border-radius: 10px;
    margin-bottom: 4px;
  }

  .own {
    border-bottom-right-radius: 2px;
  }

  .other {
    border-bottom-left-radius: 2px;
  }
}
</style>