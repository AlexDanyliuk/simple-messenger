<template>
  <div class="chat-layout">

    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="search-wrap" :class="{ 'search-active': searchQuery }">
          <svg class="search-icon" width="15" height="15" viewBox="0 0 24 24" fill="none">
            <circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="2"/>
            <path d="M16.5 16.5L21 21" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <input
            ref="searchInput"
            v-model="searchQuery"
            class="search-input"
            type="text"
            :placeholder="t('chatSearchPlaceholder')"
            @input="onSearchInput"
          />
          <button v-if="searchQuery" class="search-clear" @click="clearSearch">✕</button>
        </div>
      </div>

      <template v-if="searchQuery">
        <div class="search-results">
          <div v-if="!isSearchReady" class="search-state">{{ t("chatSearchMinChars") }}</div>
          <div v-else-if="searchLoading" class="search-state">{{ t("chatSearchLoading") }}</div>
          <div v-else-if="searchResults.length === 0" class="search-state">{{ t("chatSearchNotFound") }}</div>
          <ChatItem
            v-for="user in searchResults"
            :key="user.id"
            :chat="user"
            @click="openChat(user.id)"
          />
        </div>
      </template>

      <template v-else>
        <ChatList
          :chats="users"
          @select="openChat"
        />
      </template>

      <div class="user-footer" @click="toggleProfile">
        <div class="user-avatar">
          <img v-if="me && me.avatarUrl" :src="me.avatarUrl" class="user-avatar-img" alt="" />
          <span v-else>{{ meInitial }}</span>
        </div>
        <div class="user-info">
          <div class="user-name">{{ me ? me.username : '...' }}</div>
          <div class="user-status">
            <span class="status-dot"></span>
            {{ t("online") }}
          </div>
        </div>
        <div class="user-chevron">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
            <path d="M8 9l4-4 4 4M8 15l4 4 4-4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
      </div>

      <transition name="popup">
        <div class="profile-popup" v-if="showProfile">
          <div class="popup-header">
          <div class="popup-avatar">
          <img v-if="me && me.avatarUrl" :src="me.avatarUrl" class="popup-avatar-img" alt="" />
          <span v-else>{{ meInitial }}</span>
        </div>
            <div class="popup-user-info">
              <div class="popup-name">{{ me ? me.username : '' }}</div>
              <div class="popup-email">{{ me ? me.email : '' }}</div>
            </div>
          </div>

          <div class="popup-divider"></div>

          <div class="popup-menu">
            <div class="popup-item" @click="goToProfile">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="8" r="4" stroke="currentColor" stroke-width="2"/>
                <path d="M4 20c0-4 3.6-7 8-7s8 3 8 7" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              {{ t("chatEditProfile") }}
            </div>
            <div class="popup-item popup-item--danger" @click="logout">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none">
                <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4M16 17l5-5-5-5M21 12H9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              {{ t("commonLogout") }}
            </div>
          </div>
        </div>
      </transition>
    </aside>

    <main class="chat-content" @click="closeProfile">
      <router-view />
    </main>

  </div>
</template>

<script>
import ChatList from "../components/chat/ChatList.vue";
import ChatItem from "../components/chat/ChatItem.vue";
import api from "../services/api";
import { connect, subscribe, disconnect } from "../services/websocket";
import { applyUserPreferences, t } from "../services/userPreferences";

export default {
  components: { ChatList, ChatItem },

  data() {
    return {
      users: [],
      me: null,
      showProfile: false,
      statusSubscription: null,
      conversationSubscription: null,
      typingTimers: {},
      searchQuery: "",
      searchResults: [],
      searchLoading: false,
      searchTimeout: null
    };
  },

  computed: {
    isSearchReady() {
      return this.searchQuery.trim().length >= 3;
    },
    meInitial() {
      if (!this.me || !this.me.username) return "?";
      return this.me.username.charAt(0).toUpperCase();
    }
  },

  async mounted() {
    const profileRes = await api.get("/user/profile");
    this.me = profileRes.data;
    applyUserPreferences(this.me);

    const res = await api.get("/user/conversations");
    this.users = res.data.filter(u => String(u.id) !== String(this.me?.id));

    await connect();
    this.statusSubscription = await subscribe("/topic/status", (update) => {
      this.users = this.users.map(u =>
        String(u.id) === String(update.userId)
          ? { ...u, status: update.status, lastSeenAt: update.lastSeenAt || u.lastSeenAt }
          : u
      );
    });

    this.conversationSubscription = await subscribe(
      `/topic/conversations/${this.me.id}`,
      (updatedUser) => {
        if (String(updatedUser.id) === String(this.me?.id)) {
          return;
        }
        const idx = this.users.findIndex(u => String(u.id) === String(updatedUser.id));
        if (idx === -1) {
          this.users.unshift(updatedUser);
          return;
        }

        const updated = { ...this.users[idx], ...updatedUser };
        this.users.splice(idx, 1);
        this.users.unshift(updated);

        // Handle typing flag: auto-clear after 4s
        if (updatedUser.typing) {
          const userId = updatedUser.id;
          if (this.typingTimers[userId]) clearTimeout(this.typingTimers[userId]);
          this.typingTimers[userId] = setTimeout(() => {
            this.users = this.users.map(u => String(u.id) === String(userId) ? { ...u, typing: false } : u);
            delete this.typingTimers[userId];
          }, 4000);
        } else if (updatedUser.typing === false) {
          const userId = updatedUser.id;
          if (this.typingTimers[userId]) {
            clearTimeout(this.typingTimers[userId]);
            delete this.typingTimers[userId];
          }
        }
      }
    );

    document.addEventListener("mousedown", this.handleOutsideClick);
  },

  beforeUnmount() {
    document.removeEventListener("mousedown", this.handleOutsideClick);
    if (this.statusSubscription) {
      this.statusSubscription.unsubscribe();
    }
    if (this.conversationSubscription) {
      this.conversationSubscription.unsubscribe();
    }
  },

  methods: {
    t,
    openChat(userId) {
      if (String(userId) === String(this.me?.id)) {
        return;
      }
      this.clearSearch();
      this.$router.push(`/chat/${userId}`);
    },

    onSearchInput() {
      clearTimeout(this.searchTimeout);
      if (!this.isSearchReady) {
        this.searchResults = [];
        this.searchLoading = false;
        return;
      }
      this.searchLoading = true;
      this.searchTimeout = setTimeout(async () => {
        try {
          const res = await api.get("/user/search", {
            params: { q: this.searchQuery.trim() }
          });
          this.searchResults = res.data.filter(
            u => String(u.id) !== String(this.me?.id)
          );
        } catch (e) {
          this.searchResults = [];
        } finally {
          this.searchLoading = false;
        }
      }, 300);
    },

    clearSearch() {
      this.searchQuery = "";
      this.searchResults = [];
      this.searchLoading = false;
      clearTimeout(this.searchTimeout);
    },

    toggleProfile(e) {
      e.stopPropagation();
      this.showProfile = !this.showProfile;
    },

    closeProfile() {
      this.showProfile = false;
    },

    handleOutsideClick(e) {
      const footer = this.$el.querySelector(".user-footer");
      const popup = this.$el.querySelector(".profile-popup");
      if (
        footer && !footer.contains(e.target) &&
        popup && !popup.contains(e.target)
      ) {
        this.showProfile = false;
      }
    },

    goToProfile() {
      this.showProfile = false;
      this.$router.push("/profile");
    },

    async logout() {
      try {
        await api.post("/auth/logout");
      } catch (e) {
      } finally {
        disconnect();
        this.$router.replace("/login");
      }
    }
  }
};
</script>

<style scoped>
.chat-layout {
  display: flex;
  height: 100vh;
  background: var(--app-bg);
  font-family: inherit;
  overflow: hidden;
}

.sidebar {
  width: 300px;
  min-width: 300px;
  background: var(--panel-bg);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  position: relative;
  box-shadow: inset -1px 0 0 var(--border-color);
}

.sidebar-header {
  padding: 14px;
  border-bottom: 1px solid var(--border-color);
  flex-shrink: 0;
}

.search-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--panel-bg-soft);
  border: 1.5px solid var(--border-color);
  border-radius: 10px;
  padding: 8px 12px;
  transition: all 0.2s ease;
}

.search-wrap.search-active,
.search-wrap:focus-within {
  background: var(--app-bg-secondary);
  border-color: var(--accent);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.search-icon {
  flex-shrink: 0;
  color: var(--text-tertiary);
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  font-size: 14px;
  color: var(--app-text);
  min-width: 0;
}

.search-input::placeholder {
  color: var(--text-tertiary);
}

.search-clear {
  background: none;
  border: none;
  cursor: pointer;
  color: var(--text-tertiary);
  font-size: 16px;
  padding: 0;
  line-height: 1;
  flex-shrink: 0;
  transition: color 0.2s ease;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-clear:hover {
  color: var(--app-text);
}

.search-results {
  flex: 1;
  overflow-y: auto;
  padding: 6px 0;
}

.search-results::-webkit-scrollbar {
  width: 6px;
}

.search-results::-webkit-scrollbar-track {
  background: transparent;
}

.search-results::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 3px;
}

.search-results::-webkit-scrollbar-thumb:hover {
  background: var(--border-color-strong);
}

.search-state {
  padding: 24px 20px;
  text-align: center;
  font-size: 14px;
  color: var(--text-tertiary);
}

.user-footer {
  display: flex;
  align-items: center;
  padding: 12px 14px;
  border-top: 1px solid var(--border-color);
  cursor: pointer;
  transition: background 0.2s ease;
  flex-shrink: 0;
  gap: 10px;
  background: var(--panel-bg);
  user-select: none;
}

.user-footer:hover {
  background: var(--panel-bg-hover);
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--accent);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 16px;
  flex-shrink: 0;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(100, 156, 43, 0.25);
}

.user-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--app-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-status {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 2px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--online-dot);
  flex-shrink: 0;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

.user-chevron {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  color: var(--text-tertiary);
}

.profile-popup {
  position: absolute;
  bottom: 70px;
  left: 12px;
  width: 260px;
  background: var(--panel-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  box-shadow: var(--shadow-xl);
  z-index: 200;
  overflow: hidden;
  transform-origin: bottom left;
}

.popup-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
}

.popup-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: var(--accent);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 18px;
  flex-shrink: 0;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(100, 156, 43, 0.25);
}

.popup-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.popup-user-info {
  min-width: 0;
  flex: 1;
}

.popup-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--app-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.popup-email {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.popup-divider {
  height: 1px;
  background: var(--border-color);
}

.popup-menu {
  padding: 6px;
}

.popup-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  color: var(--app-text);
  cursor: pointer;
  transition: all 0.2s ease;
}

.popup-item:hover {
  background: var(--panel-bg-hover);
}

.popup-item svg {
  flex-shrink: 0;
  width: 16px;
  height: 16px;
  stroke: currentColor;
}

.popup-item--danger {
  color: var(--error);
}

.popup-item--danger:hover {
  background: rgba(239, 68, 68, 0.1);
}

.chat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--app-bg);
  min-width: 0;
}

.popup-enter-active,
.popup-leave-active {
  transition: all 0.15s ease;
}

.popup-enter-from,
.popup-leave-to {
  opacity: 0;
  transform: scale(0.95) translateY(8px);
}

@media (max-width: 768px) {
  .sidebar {
    width: 240px;
    min-width: 240px;
  }

  .sidebar-header {
    padding: 12px;
  }

  .search-input {
    font-size: 13px;
  }

  .user-footer {
    padding: 10px 12px;
  }

  .user-avatar {
    width: 36px;
    height: 36px;
    font-size: 14px;
  }

  .user-name {
    font-size: 13px;
  }

  .user-status {
    font-size: 11px;
  }

  .profile-popup {
    width: 220px;
  }
}

@media (max-width: 480px) {
  .sidebar {
    width: 100%;
    min-width: 80px;
    max-width: 200px;
    position: absolute;
    left: -100%;
    top: 0;
    height: 100%;
    z-index: 100;
    transition: left 0.3s ease;
    border-right: 1px solid var(--border-color);
  }

  .chat-layout:has(.sidebar:hover) .sidebar {
    left: 0;
  }

  .sidebar-header {
    padding: 10px;
  }

  .search-wrap {
    padding: 6px 10px;
  }

  .search-input {
    font-size: 12px;
  }

  .user-footer {
    padding: 8px 10px;
  }

  .user-avatar {
    width: 32px;
    height: 32px;
    font-size: 12px;
  }

  .user-name {
    font-size: 12px;
  }

  .user-status {
    font-size: 10px;
  }

  .user-chevron {
    display: none;
  }

  .search-state {
    padding: 16px;
    font-size: 12px;
  }

  .profile-popup {
    width: 180px;
  }

  .popup-header {
    padding: 12px;
  }

  .popup-avatar {
    width: 40px;
    height: 40px;
    font-size: 16px;
  }

  .popup-name {
    font-size: 12px;
  }

  .popup-email {
    font-size: 11px;
  }

  .popup-item {
    padding: 8px 10px;
    font-size: 12px;
    gap: 8px;
  }
}
</style>