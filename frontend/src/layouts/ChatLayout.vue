<template>
  <div class="chat-layout">

    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="search-wrap" :class="{ 'search-active': searchQuery }">
          <svg class="search-icon" width="15" height="15" viewBox="0 0 24 24" fill="none">
            <circle cx="11" cy="11" r="7" stroke="#999" stroke-width="2"/>
            <path d="M16.5 16.5L21 21" stroke="#999" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <input
            ref="searchInput"
            v-model="searchQuery"
            class="search-input"
            type="text"
            placeholder="Пошук"
            @input="onSearchInput"
          />
          <button v-if="searchQuery" class="search-clear" @click="clearSearch">✕</button>
        </div>
      </div>

      <template v-if="searchQuery">
        <div class="search-results">
          <div v-if="!isSearchReady" class="search-state">Введіть щонайменше 3 символи</div>
          <div v-else-if="searchLoading" class="search-state">Пошук…</div>
          <div v-else-if="searchResults.length === 0" class="search-state">Користувачів не знайдено</div>
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
            В мережі
          </div>
        </div>
        <div class="user-chevron">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
            <path d="M8 9l4-4 4 4M8 15l4 4 4-4" stroke="#aaa" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
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
                <circle cx="12" cy="8" r="4" stroke="#555" stroke-width="2"/>
                <path d="M4 20c0-4 3.6-7 8-7s8 3 8 7" stroke="#555" stroke-width="2" stroke-linecap="round"/>
              </svg>
              Редагувати профіль
            </div>
            <div class="popup-item popup-item--danger" @click="logout">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none">
                <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4M16 17l5-5-5-5M21 12H9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              Вийти
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

    const res = await api.get("/user/conversations");
    this.users = res.data.filter(u => String(u.id) !== String(this.me?.id));

    await connect();
    this.statusSubscription = await subscribe("/topic/status", (update) => {
      this.users = this.users.map(u =>
        String(u.id) === String(update.userId)
          ? { ...u, status: update.status }
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
  background: #ffffff;
  font-family: inherit;
  overflow: hidden;
}

.sidebar {
  width: 280px;
  min-width: 280px;
  background: linear-gradient(180deg, #ffffff 0%, #fbfdff 100%);
  border-right: 1px solid #e8eef5;
  display: flex;
  flex-direction: column;
  position: relative;
  box-shadow: 10px 0 30px rgba(15, 23, 42, 0.035);
}

.sidebar-header {
  padding: 12px 14px 10px;
  border-bottom: 1px solid #eef2f7;
  flex-shrink: 0;
  backdrop-filter: blur(12px);
}

.search-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 7px 10px;
  transition: background 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease, transform 0.18s ease;
}

.search-wrap.search-active,
.search-wrap:focus-within {
  background: #ffffff;
  border-color: #cbd5e1;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.07), 0 0 0 4px rgba(148, 163, 184, 0.12);
  transform: translateY(-1px);
}

.search-icon {
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  font-size: 13px;
  color: #111;
  min-width: 0;
}

.search-input::placeholder {
  color: #bbb;
}

.search-clear {
  background: none;
  border: none;
  cursor: pointer;
  color: #aaa;
  font-size: 12px;
  padding: 0;
  line-height: 1;
  flex-shrink: 0;
  transition: color 0.12s;
}

.search-clear:hover {
  color: #555;
}

.search-results {
  flex: 1;
  overflow-y: auto;
  padding: 6px 0;
}

.search-results::-webkit-scrollbar {
  width: 3px;
}

.search-results::-webkit-scrollbar-thumb {
  background: #e0e0e0;
  border-radius: 4px;
}

.search-state {
  padding: 22px 20px;
  text-align: center;
  font-size: 13px;
  color: #94a3b8;
}

.user-footer {
  display: flex;
  align-items: center;
  padding: 14px 18px;
  border-top: 1px solid #e8eef5;
  cursor: pointer;
  transition: background 0.18s ease, transform 0.18s ease;
  flex-shrink: 0;
  gap: 10px;
  background: linear-gradient(180deg, #ffffff 0%, #fbfdff 100%);
  user-select: none;
}

.user-footer:hover {
  background: linear-gradient(180deg, #ffffff 0%, #f7fafc 100%);
  transform: translateY(-1px);
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0f172a 0%, #334155 100%);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
  flex-shrink: 0;
  overflow: hidden;
  box-shadow: 0 10px 20px rgba(15, 23, 42, 0.16);
}

.user-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  border-radius: 50%;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 13px;
  font-weight: 600;
  color: #111111;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-status {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 11px;
  color: #999999;
  margin-top: 2px;
}

.status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #4caf50;
  flex-shrink: 0;
}

.user-chevron {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.profile-popup {
  position: absolute;
  bottom: 70px;
  left: 12px;
  width: 244px;
  background: linear-gradient(180deg, #ffffff 0%, #fbfdff 100%);
  border-radius: 18px;
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.16), 0 4px 14px rgba(15, 23, 42, 0.08);
  z-index: 200;
  overflow: hidden;
  border: 1px solid #e8eef5;
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
  background: #111111;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 18px;
  flex-shrink: 0;
  overflow: hidden;
}

.popup-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  border-radius: 50%;
}

.popup-user-info {
  min-width: 0;
}

.popup-name {
  font-size: 14px;
  font-weight: 600;
  color: #111111;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.popup-email {
  font-size: 11px;
  color: #aaaaaa;
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.popup-divider {
  height: 1px;
  background: #f0f0f0;
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
  font-size: 13px;
  font-weight: 500;
  color: #333333;
  cursor: pointer;
  transition: background 0.12s;
}

.popup-item:hover {
  background: #f5f5f5;
}

.popup-item--danger {
  color: #cc2200;
}

.popup-item--danger:hover {
  background: #fff4f2;
}

.chat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f9f9f9;
  min-width: 0;
}

.popup-enter-active,
.popup-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}

.popup-enter-from,
.popup-leave-to {
  opacity: 0;
  transform: scale(0.95) translateY(8px);
}

@media (max-width: 768px) {
  .sidebar {
    width: 200px;
    min-width: 200px;
    max-height: none;
    border-right: 1px solid #ebebeb;
    border-bottom: none;
    order: initial;
  }

  .chat-content {
    order: initial;
    flex: 1;
  }

  .search-input {
    font-size: 12px;
  }

  .user-footer {
    padding: 10px 14px;
  }

  .user-info {
    display: flex;
  }

  .user-avatar {
    width: 32px;
    height: 32px;
    font-size: 11px;
  }

  .user-chevron {
    display: none;
  }

  .user-name {
    font-size: 11px;
  }

  .user-status {
    font-size: 9px;
  }

  .profile-popup {
    bottom: 60px;
    left: 8px;
    width: 180px;
  }
}

@media (max-width: 480px) {
  .sidebar {
    width: 120px;
    min-width: 120px;
  }

  .sidebar-header {
    padding: 8px 10px 6px;
  }

  .search-wrap {
    padding: 5px 6px;
  }

  .search-input {
    font-size: 11px;
  }

  .search-clear {
    font-size: 10px;
  }

  .search-results {
    padding: 4px 0;
  }

  .user-footer {
    padding: 8px 10px;
    gap: 6px;
  }

  .user-avatar {
    width: 28px;
    height: 28px;
    font-size: 10px;
  }

  .user-name {
    font-size: 10px;
  }

  .user-status {
    font-size: 8px;
  }

  .status-dot {
    width: 5px;
    height: 5px;
  }

  .search-state {
    padding: 12px;
    font-size: 10px;
  }

  .profile-popup {
    bottom: 50px;
    left: 4px;
    width: 160px;
  }

  .popup-header {
    padding: 10px;
  }

  .popup-avatar {
    width: 36px;
    height: 36px;
    font-size: 14px;
  }

  .popup-name {
    font-size: 11px;
  }

  .popup-email {
    font-size: 9px;
  }

  .popup-item {
    padding: 8px 8px;
    font-size: 10px;
    gap: 6px;
  }
}
</style>