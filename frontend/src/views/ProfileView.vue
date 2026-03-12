<template>
  <div class="page">

    <div class="topbar">
      <button class="back-btn" @click="$router.push('/chats')">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
          <path d="M19 12H5M5 12l7 7M5 12l7-7"
            stroke="currentColor" stroke-width="2"
            stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        Назад до чатів
      </button>
    </div>

    <div class="card">

      <div class="avatar-section">
        <div class="avatar-wrap" @click="pickAvatar" title="Змінити аватарку">
          <img v-if="avatarPreview || user.avatarUrl"
               :src="avatarPreview || user.avatarUrl"
               class="avatar avatar-img"
               alt="avatar" />
          <div v-else class="avatar">{{ userInitial }}</div>
          <div class="avatar-overlay">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
              <polyline points="17 8 12 3 7 8"/>
              <line x1="12" y1="3" x2="12" y2="15"/>
            </svg>
          </div>
        </div>
        <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="onFileChange" />

        <div v-if="avatarPreview" class="avatar-actions">
          <button class="btn-avatar-save" :disabled="avatarUploading" @click="uploadAvatar">
            {{ avatarUploading ? 'Збереження...' : 'Зберегти фото' }}
          </button>
          <button class="btn-avatar-cancel" @click="cancelAvatar">Скасувати</button>
        </div>

        <div class="avatar-name">{{ user.username || '...' }}</div>
        <div class="avatar-email">{{ user.email || '' }}</div>
      </div>

      <div class="form-section" v-if="!loading">
        <div class="field">
          <label>Імʼя користувача</label>
          <input
            v-model="user.username"
            @blur="touch('username')"
            @input="user.username = user.username.toLowerCase()"
            placeholder="Імʼя користувача"
            :class="{ 'input-error': errors.username || user.username.length > 20 }"
          />
          <span v-if="user.username.length > 20" class="field-error">Максимум 20 символів</span>
          <span v-else-if="errors.username" class="field-error">{{ errors.username }}</span>
        </div>
        <div class="field">
          <label>Повне імʼя</label>
          <input
            v-model="user.fullName"
            @blur="touch('fullName')"
            placeholder="Повне імʼя"
            :class="{ 'input-error': errors.fullName }"
          />
          <span v-if="errors.fullName" class="field-error">{{ errors.fullName }}</span>
        </div>
        <div class="field">
          <label>Email</label>
          <input v-model="user.email" disabled />
        </div>

        <button class="btn-save" @click="updateProfile" :disabled="saving">{{ saving ? 'Збереження...' : 'Зберегти зміни' }}</button>

        <div v-if="message" class="msg-banner msg-success">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
          {{ message }}
        </div>
        <div v-if="error" class="msg-banner msg-error">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          {{ error }}
        </div>
      </div>

      <div class="loading" v-if="loading">Завантаження...</div>

      <div class="logout-area">
        <button class="btn-logout" @click="logout">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none">
            <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4
                     M16 17l5-5-5-5M21 12H9"
              stroke="currentColor" stroke-width="2"
              stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          Вийти
        </button>
      </div>

    </div>
  </div>
</template>

<script>
import api from "../services/api";
import { disconnect } from "../services/websocket";

export default {
  name: "ProfileView",
  data() {
    return {
      user: { username: "", fullName: "", email: "", avatarUrl: "" },
      loading: true,
      saving: false,
      message: "",
      error: "",
      touched: { username: false, fullName: false },
      avatarPreview: null,
      avatarFile: null,
      avatarUploading: false
    };
  },
  computed: {
    userInitial() {
      return this.user.username
        ? this.user.username.charAt(0).toUpperCase()
        : "?";
    },
    errors() {
      const e = {};
      if (this.touched.username) {
        if (!this.user.username) e.username = "Введіть імʼя користувача";
        else if (this.user.username.length < 3) e.username = "Мінімум 3 символи";
        else if (this.user.username.length > 20) e.username = "Максимум 20 символів";
        else if (!/^[a-z0-9_.]+$/.test(this.user.username)) e.username = "Лише малі літери, цифри, _ .";
      }
      if (this.touched.fullName) {
        if (!this.user.fullName?.trim()) e.fullName = "Введіть повне імʼя";
      }
      return e;
    }
  },
  async mounted() {
    if (!localStorage.getItem("token")) {
      this.$router.replace("/login");
      return;
    }
    try {
      const res = await api.get("/user/profile");
      const d = res.data;
      this.user = {
        ...d,
        fullName: d.fullName || d.full_name || ""
      };
    } catch (err) {
      if (err.response?.status === 401) this.logout();
      else this.error = "Не вдалося завантажити профіль";
    } finally {
      this.loading = false;
    }
  },
  methods: {
    touch(field) {
      this.touched[field] = true;
    },
    isValid() {
      Object.keys(this.touched).forEach(k => (this.touched[k] = true));
      return Object.keys(this.errors).length === 0;
    },
    async updateProfile() {
      if (!this.isValid()) return;
      this.saving = true;
      this.error = "";
      this.message = "";
      try {
        await api.patch("/user/profile", {
          username: this.user.username,
          fullName: this.user.fullName,
          full_name: this.user.fullName
        });
        this.message = "Збережено ✓";
        setTimeout(() => { this.message = ""; }, 3000);
      } catch (err) {
        if (err.response?.status === 401) {
          this.logout();
        } else {
          const status = err.response?.status;
          const msg = (err.response?.data?.message || "").toLowerCase();
          if (
            msg.includes("username") ||
            msg.includes("already") ||
            msg.includes("exist") ||
            msg.includes("duplicate") ||
            status === 409 ||
            status === 500
          ) {
            this.error = `Користувач з іменем "${this.user.username}" вже існує`;
          } else {
            this.error = err.response?.data?.message || "Помилка оновлення";
          }
        }
      } finally {
        this.saving = false;
      }
    },
    async logout() {
      try {
        await api.post("/user/logout");
      } catch (e) {
      } finally {
        disconnect();
        localStorage.removeItem("token");
        localStorage.removeItem("user");
        this.$router.replace("/login");
      }
    },

    pickAvatar() {
      this.$refs.fileInput.click();
    },

    onFileChange(e) {
      const file = e.target.files[0];
      if (!file) return;
      this.avatarFile = file;
      this.avatarPreview = URL.createObjectURL(file);
      e.target.value = "";
    },

    cancelAvatar() {
      this.avatarPreview = null;
      this.avatarFile = null;
    },

    async uploadAvatar() {
      if (!this.avatarFile) return;
      this.avatarUploading = true;
      this.error = "";
      try {
        const form = new FormData();
        form.append("file", this.avatarFile);
        const res = await api.post("/user/avatar", form, {
          headers: { "Content-Type": "multipart/form-data" }
        });
        this.user.avatarUrl = res.data.avatarUrl;
        this.avatarPreview = null;
        this.avatarFile = null;
        this.message = "Аватарку збережено ✓";
        setTimeout(() => { this.message = ""; }, 3000);
      } catch (e) {
        this.error = "Не вдалося завантажити аватарку";
      } finally {
        this.avatarUploading = false;
      }
    }
  }
};
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f7f7f7;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 28px 16px 60px;
  font-family: 'DM Sans', -apple-system, BlinkMacSystemFont, sans-serif;
}

.topbar {
  width: 100%;
  max-width: 440px;
  margin-bottom: 16px;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  background: none;
  border: none;
  font-size: 13px;
  font-weight: 500;
  color: #666;
  cursor: pointer;
  padding: 7px 12px 7px 8px;
  border-radius: 8px;
  transition: background 0.12s, color 0.12s;
  font-family: inherit;
}
.back-btn:hover {
  background: #e8e8e8;
  color: #111;
}

.card {
  width: 100%;
  max-width: 440px;
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.07);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 36px 40px 24px;
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
}

.avatar-wrap {
  position: relative;
  cursor: pointer;
  margin-bottom: 14px;
}
.avatar-wrap:hover .avatar-overlay {
  opacity: 1;
}

.avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: #111;
  color: #fff;
  font-size: 28px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-img {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  object-fit: cover;
  display: block;
  background: #eee;
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: rgba(0,0,0,0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.18s;
}

.avatar-actions {
  display: flex;
  gap: 8px;
  margin-bottom: 14px;
}

.btn-avatar-save {
  padding: 7px 18px;
  border-radius: 8px;
  border: none;
  background: #111;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: opacity 0.15s;
}
.btn-avatar-save:hover { opacity: 0.75; }
.btn-avatar-save:disabled { opacity: 0.45; cursor: not-allowed; }

.btn-avatar-cancel {
  padding: 7px 14px;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
  background: #fff;
  color: #555;
  font-size: 13px;
  font-weight: 500;
  font-family: inherit;
  cursor: pointer;
  transition: background 0.12s;
}
.btn-avatar-cancel:hover { background: #f5f5f5; }
.avatar-name {
  font-size: 18px;
  font-weight: 700;
  color: #111;
  margin-bottom: 4px;
}
.avatar-email {
  font-size: 13px;
  color: #aaa;
}

.form-section {
  padding: 28px 36px 4px;
  display: flex;
  flex-direction: column;
}
.loading {
  padding: 24px 36px;
  color: #aaa;
  font-size: 14px;
}

.field {
  display: flex;
  flex-direction: column;
  margin-bottom: 18px;
}
.field label {
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.8px;
  text-transform: uppercase;
  color: #aaa;
  margin-bottom: 6px;
}
input {
  padding: 11px 14px;
  border-radius: 10px;
  border: 1px solid #e4e4e4;
  background: #f9f9f9;
  font-size: 14px;
  font-family: inherit;
  color: #111;
  outline: none;
  transition: border-color 0.15s, background 0.15s;
}
input::placeholder { color: #ccc; }
input:focus {
  border-color: #111;
  background: #fff;
}
input:disabled {
  background: #f3f3f3;
  color: #bbb;
  cursor: not-allowed;
  border-color: #eee;
}

.btn-save {
  padding: 13px;
  border-radius: 10px;
  border: none;
  background: #111;
  color: #fff;
  font-weight: 600;
  font-size: 14px;
  font-family: inherit;
  cursor: pointer;
  transition: opacity 0.15s;
  margin-top: 4px;
}
.btn-save:hover { opacity: 0.78; }

.msg-banner {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 14px;
  padding: 11px 14px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 500;
  animation: fadeIn 0.2s ease;
}
.msg-success {
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  color: #15803d;
}
.msg-error {
  background: #fff1f2;
  border: 1px solid #fecdd3;
  color: #be123c;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-4px); }
  to   { opacity: 1; transform: translateY(0); }
}

.field-error {
  font-size: 12px;
  color: #e53e3e;
  margin-top: 5px;
  animation: fadeIn 0.2s ease;
}
.field-hint {
  font-size: 12px;
  color: #aaa;
  margin-top: 5px;
}
.input-error {
  border-color: #e53e3e !important;
  background: #fff5f5 !important;
}

.logout-area {
  padding: 24px 36px 28px;
  border-top: 1px solid #f0f0f0;
  margin-top: 24px;
}
.btn-logout {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 12px 16px;
  border-radius: 10px;
  border: 1px solid #f0dedd;
  background: #fff;
  color: #cc2200;
  font-weight: 600;
  font-size: 14px;
  font-family: inherit;
  cursor: pointer;
  transition: background 0.12s, border-color 0.12s;
}
.btn-logout:hover {
  background: #fff4f2;
  border-color: #f5bfb8;
}
</style>