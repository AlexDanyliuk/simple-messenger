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
  background: #d0e8ff;
  color: #111111;
  border-bottom-right-radius: 4px;
}

.other {
  align-self: flex-start;
  background: #ffffff;
  color: #111111;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}
</style>