<template>
  <div class="layout">
    <ChatList
      :chats="users"
      @select="openChat"
    />
  </div>
</template>

<script>
import ChatList from "../components/chat/ChatList.vue";
import api from "@/services/api";
import { connect, subscribe } from "@/services/websocket";

export default {
  name: "ChatsView",

  components: {
    ChatList
  },

  data() {
    return {
      users: [],
      me: null,
      statusSubscription: null,
      conversationSubscription: null
    };
  },

  async mounted() {
    const profileRes = await api.get("/user/profile");
    this.me = profileRes.data;

    // ✅ /conversations повертає юзерів з lastMessage та unreadCount
    const usersRes = await api.get("/user/conversations");
    this.users = usersRes.data;

    await connect();

    // Підписка на статуси в реальному часі
    this.statusSubscription = await subscribe(
      "/topic/status",
      (update) => {
        this.users = this.users.map(u =>
          String(u.id) === String(update.userId)
            ? { ...u, status: update.status }
            : u
        );
      }
    );

    // Підписка на оновлення розмов (lastMessage, unreadCount)
    this.conversationSubscription = await subscribe(
      `/topic/conversations/${this.me.id}`,
      (updatedUser) => {
        const exists = this.users.some(u => String(u.id) === String(updatedUser.id));
        if (exists) {
          this.users = this.users.map(u => {
            if (String(u.id) !== String(updatedUser.id)) return u;
            // Мержимо тільки non-null поля, щоб не затерти lastMessage
            const merged = { ...u };
            Object.keys(updatedUser).forEach(key => {
              if (updatedUser[key] !== null && updatedUser[key] !== undefined) {
                merged[key] = updatedUser[key];
              }
            });
            return merged;
          });
        } else {
          // Новий співрозмовник якого ще немає в списку — додаємо
          this.users = [updatedUser, ...this.users];
        }

        // Сортуємо за часом останнього повідомлення
        this.users = [...this.users].sort((a, b) => {
          if (!a.lastMessageTime) return 1;
          if (!b.lastMessageTime) return -1;
          return new Date(b.lastMessageTime) - new Date(a.lastMessageTime);
        });
      }
    );
  },

  beforeUnmount() {
    if (this.statusSubscription) {
      this.statusSubscription.unsubscribe();
    }
    if (this.conversationSubscription) {
      this.conversationSubscription.unsubscribe();
    }
  },

  methods: {
    openChat(userId) {
      this.$router.push(`/chat/${userId}`);
    }
  }
};
</script>

<style scoped>
.layout {
  display: flex;
  height: 100vh;
}
</style>