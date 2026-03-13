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
      conversationSubscription: null,
      typingTimers: {}
    };
  },

  async mounted() {
    const profileRes = await api.get("/user/profile");
    this.me = profileRes.data;

    const usersRes = await api.get("/user/conversations");
    this.users = usersRes.data;

    await connect();

    this.statusSubscription = await subscribe(
      "/topic/status",
      (update) => {
        this.users = this.users.map(u =>
          String(u.id) === String(update.userId)
            ? { ...u, status: update.status, lastSeenAt: update.lastSeenAt || u.lastSeenAt }
            : u
        );
      }
    );

    this.conversationSubscription = await subscribe(
      `/topic/conversations/${this.me.id}`,
      (updatedUser) => {
        const exists = this.users.some(u => String(u.id) === String(updatedUser.id));
        if (!exists) {
          return;
        }

        this.users = this.users.map(u => {
          if (String(u.id) !== String(updatedUser.id)) return u;
          const merged = { ...u };
          Object.keys(updatedUser).forEach(key => {
            if (updatedUser[key] !== null && updatedUser[key] !== undefined) {
              merged[key] = updatedUser[key];
            }
          });
          return merged;
        });

        // Handle typing flag: auto-clear after 4s if received
        if (updatedUser.typing) {
          const userId = updatedUser.id;
          if (this.typingTimers[userId]) clearTimeout(this.typingTimers[userId]);
          this.typingTimers[userId] = setTimeout(() => {
            this.users = this.users.map(u => String(u.id) === String(userId) ? { ...u, typing: false } : u);
            delete this.typingTimers[userId];
          }, 4000);
        } else if (updatedUser.typing === false) {
          // clear any existing timer
          const userId = updatedUser.id;
          if (this.typingTimers[userId]) {
            clearTimeout(this.typingTimers[userId]);
            delete this.typingTimers[userId];
          }
        }
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