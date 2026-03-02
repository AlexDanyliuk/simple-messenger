<template>
  <div class="chat-layout">

    <!-- ЛІВА ЧАСТИНА -->
    <aside class="sidebar">
      <!-- SEARCH BAR -->
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
            placeholder="Пошук користувачів…"
            @input="onSearchInput"
          />
          <button v-if="searchQuery" class="search-clear" @click="clearSearch">✕</button>
        </div>
      </div>

      <!-- SEARCH RESULTS -->
      <template v-if="searchQuery">
        <div class="search-results">
          <div v-if="searchLoading" class="search-state">Пошук…</div>
          <div v-else-if="searchResults.length === 0" class="search-state">Користувачів не знайдено</div>
          <ChatItem
            v-for="user in searchResults"
            :key="user.id"
            :chat="user"
            @click="openChat(user.id)"
          />
        </div>
      </template>

      <!-- REGULAR LIST -->
      <template v-else>
        <ChatList
          :chats="users"
          @select="openChat"
        />
      </template>

      <!-- USER FOOTER -->
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

      <!-- PROFILE POPUP -->
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

    <!-- ПРАВА ЧАСТИНА -->
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
      searchQuery: "",
      searchResults: [],
      searchLoading: false,
      searchTimeout: null
    };
  },

  computed: {
    meInitial() {
      if (!this.me || !this.me.username) return "?";
      return this.me.username.charAt(0).toUpperCase();
    }
  },

  async mounted() {
    const profileRes = await api.get("/user/profile");
    this.me = profileRes.data;

    const res = await api.get("/user/conversations");
    this.users = res.data;

    // підписуємось на статуси
    await connect();
    this.statusSubscription = await subscribe("/topic/status", (update) => {
      this.users = this.users.map(u =>
        String(u.id) === String(update.userId)
          ? { ...u, status: update.status }
          : u
      );
    });

    // підписуємось на нових співрозмовників
    this.conversationSubscription = await subscribe(
      `/topic/conversations/${this.me.id}`,
      (updatedUser) => {
        const idx = this.users.findIndex(u => String(u.id) === String(updatedUser.id));
        if (idx === -1) {
          this.users.unshift(updatedUser);
        } else {
          // оновлюємо превʼю і переміщуємо нагору списку
          const updated = { ...this.users[idx], ...updatedUser };
          this.users.splice(idx, 1);
          this.users.unshift(updated);
        }
      }
    );

    // Close popup on outside click
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
      this.clearSearch();
      this.$router.push(`/chat/${userId}`);
    },

    onSearchInput() {
      clearTimeout(this.searchTimeout);
      if (!this.searchQuery.trim()) {
        this.searchResults = [];
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
        await api.post("/user/logout");
      } catch (e) {
        // виходимо в будь-якому випадку
      } finally {
        disconnect();
        localStorage.removeItem("token");
        localStorage.removeItem("user");
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
  font-family: 'DM Sans', -apple-system, BlinkMacSystemFont, sans-serif;
  overflow: hidden;
}

/* ── SIDEBAR ── */
.sidebar {
  width: 280px;
  min-width: 280px;
  background: #ffffff;
  border-right: 1px solid #ebebeb;
  display: flex;
  flex-direction: column;
  position: relative;
}

/* ── SIDEBAR HEADER / SEARCH ── */
.sidebar-header {
  padding: 12px 14px 10px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.search-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f5f5f5;
  border-radius: 10px;
  padding: 7px 10px;
  transition: background 0.15s, box-shadow 0.15s;
}

.search-wrap.search-active,
.search-wrap:focus-within {
  background: #f0f0f0;
  box-shadow: 0 0 0 2px #e0e0e0;
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
  padding: 20px;
  text-align: center;
  font-size: 13px;
  color: #aaa;
}

/* ── USER FOOTER ── */
.user-footer {
  display: flex;
  align-items: center;
  padding: 14px 18px;
  border-top: 1px solid #ebebeb;
  cursor: pointer;
  transition: background 0.12s;
  flex-shrink: 0;
  gap: 10px;
  background: #ffffff;
  user-select: none;
}

.user-footer:hover {
  background: #f7f7f7;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #111111;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
  flex-shrink: 0;
  overflow: hidden;
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

/* ── PROFILE POPUP ── */
.profile-popup {
  position: absolute;
  bottom: 70px;
  left: 12px;
  width: 244px;
  background: #ffffff;
  border-radius: 14px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.13), 0 2px 8px rgba(0,0,0,0.06);
  z-index: 200;
  overflow: hidden;
  border: 1px solid #ebebeb;
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

/* ── CHAT CONTENT ── */
.chat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f9f9f9;
  min-width: 0;
}

/* ── POPUP ANIMATION ── */
.popup-enter-active,
.popup-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}

.popup-enter-from,
.popup-leave-to {
  opacity: 0;
  transform: scale(0.95) translateY(8px);
}
</style>