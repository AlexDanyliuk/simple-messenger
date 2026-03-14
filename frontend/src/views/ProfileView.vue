<template>
  <div class="page">

    <div class="topbar">
      <div style="display: flex; justify-content: space-between; align-items: center; width: 100%;">
        <button class="back-btn" @click="$router.push('/chats')">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
            <path d="M19 12H5M5 12l7 7M5 12l7-7"
              stroke="currentColor" stroke-width="2"
              stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          {{ t("profileBack") }}
        </button>
        <div class="theme-language-controls profile-controls">
          <button
            :class="['theme-toggle', { active: isDark }]"
            @click="toggleTheme"
            :title="isDark ? 'Світла тема' : 'Темна тема'"
          >
            {{ isDark ? '☀️' : '🌙' }}
          </button>
          <select v-model="currentLanguage" @change="changeLanguage" class="language-select">
            <option value="uk">Українська</option>
            <option value="en">English</option>
          </select>
        </div>
      </div>
    </div>

    <div class="card">

      <div class="avatar-section">
        <div class="avatar-wrap" @click="pickAvatar" :title="t('profileAvatarTitle')">
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
            {{ avatarUploading ? t("profileSaving") : t("profileAvatarSave") }}
          </button>
          <button class="btn-avatar-cancel" @click="cancelAvatar">{{ t("commonCancel") }}</button>
        </div>

        <div class="avatar-name">{{ user.username || '...' }}</div>
        <div class="avatar-email">{{ user.email || '' }}</div>
      </div>

      <div class="form-section" v-if="!loading">
        <div class="field">
          <label>{{ t("profileTitleUsername") }}</label>
          <input
            v-model="user.username"
            @blur="touch('username')"
            @input="normalizeUsername"
            :placeholder="t('profileUsernamePlaceholder')"
            :class="{ 'input-error': errors.username || user.username.length > 20 }"
          />
          <span v-if="user.username.length > 20" class="field-error">{{ t("registerUsernameMax") }}</span>
          <span v-else-if="errors.username" class="field-error">{{ errors.username }}</span>
        </div>
        <div class="field">
          <label>{{ t("profileTitleFullName") }}</label>
          <input
            v-model="user.fullName"
            @blur="touch('fullName')"
            @input="normalizeFullName"
            :placeholder="t('profileFullNamePlaceholder')"
            :class="{ 'input-error': errors.fullName }"
          />
          <span v-if="errors.fullName" class="field-error">{{ errors.fullName }}</span>
        </div>
        <div class="field">
          <label>{{ t("profileTitleEmail") }}</label>
          <input v-model="user.email" disabled />
        </div>

        <div class="field">
          <label>{{ t("profileTitleTheme") }}</label>
          <select v-model="user.theme" @change="applyCurrentPreferences">
            <option value="light">{{ t("profileThemeLight") }}</option>
            <option value="dark">{{ t("profileThemeDark") }}</option>
          </select>
        </div>

        <div class="field">
          <label>{{ t("profileTitleLanguage") }}</label>
          <select v-model="user.language" @change="applyCurrentPreferences">
            <option value="uk">{{ t("profileLangUk") }}</option>
            <option value="en">{{ t("profileLangEn") }}</option>
          </select>
        </div>

        <button class="btn-save" @click="updateProfile" :disabled="saving">{{ saving ? t("profileSaving") : t("profileSave") }}</button>

        <div class="password-section">
          <h3>{{ t("profileChangePasswordTitle") }}</h3>

          <div class="field">
            <label>{{ t("profileCurrentPassword") }}</label>
            <input
              v-model="passwordForm.currentPassword"
              type="password"
              @blur="touchPassword('currentPassword')"
              :class="{ 'input-error': passwordErrors.currentPassword }"
            />
            <span v-if="passwordErrors.currentPassword" class="field-error">{{ passwordErrors.currentPassword }}</span>
          </div>

          <div class="field">
            <label>{{ t("profileNewPassword") }}</label>
            <input
              v-model="passwordForm.newPassword"
              type="password"
              @blur="touchPassword('newPassword')"
              :class="{ 'input-error': passwordErrors.newPassword }"
            />
            <span v-if="passwordErrors.newPassword" class="field-error">{{ passwordErrors.newPassword }}</span>
          </div>

          <div class="field">
            <label>{{ t("profileConfirmNewPassword") }}</label>
            <input
              v-model="passwordForm.confirmPassword"
              type="password"
              @blur="touchPassword('confirmPassword')"
              :class="{ 'input-error': passwordErrors.confirmPassword }"
            />
            <span v-if="passwordErrors.confirmPassword" class="field-error">{{ passwordErrors.confirmPassword }}</span>
          </div>

          <button class="btn-password" @click="changePassword" :disabled="savingPassword">
            {{ savingPassword ? t("profileSaving") : t("profileChangePasswordSubmit") }}
          </button>
        </div>

        <div v-if="message" class="msg-banner msg-success">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
          {{ message }}
        </div>
        <div v-if="error" class="msg-banner msg-error">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          {{ error }}
        </div>
      </div>

      <div class="loading" v-if="loading">{{ t("commonLoading") }}</div>

      <div class="logout-area">
        <button class="btn-logout" @click="logout">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none">
            <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4
                     M16 17l5-5-5-5M21 12H9"
              stroke="currentColor" stroke-width="2"
              stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          {{ t("commonLogout") }}
        </button>
      </div>

    </div>
  </div>
</template>

<script>
import api from "../services/api";
import { disconnect } from "../services/websocket";
import { applyUserPreferences, preferences, t, applyTheme, applyLanguage } from "../services/userPreferences";

export default {
  name: "ProfileView",
  data() {
    return {
      user: { username: "", fullName: "", email: "", avatarUrl: "", theme: "light", language: "uk", lastSeenAt: null },
      loading: true,
      saving: false,
      message: "",
      error: "",
      touched: { username: false, fullName: false },
      passwordForm: {
        currentPassword: "",
        newPassword: "",
        confirmPassword: ""
      },
      passwordTouched: {
        currentPassword: false,
        newPassword: false,
        confirmPassword: false
      },
      savingPassword: false,
      avatarPreview: null,
      avatarFile: null,
      avatarUploading: false,
      prefs: preferences,
      isDark: preferences.theme === "dark",
      currentLanguage: preferences.language
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
      const username = this.user.username.trim();
      const fullName = (this.user.fullName || "").replace(/\s+/g, " ").trim();
      if (this.touched.username) {
        if (!username) e.username = t("registerUsernameRequired");
        else if (username.length < 3) e.username = t("registerUsernameMin");
        else if (username.length > 20) e.username = t("registerUsernameMax");
        else if (!/^[a-zA-Z]+$/.test(username)) e.username = t("registerUsernameLatinOnly");
      }
      if (this.touched.fullName) {
        if (!fullName) e.fullName = t("registerFullNameRequired");
        else if (fullName.length < 2) e.fullName = t("registerFullNameMin");
        else if (fullName.length > 50) e.fullName = t("registerFullNameMax");
        else if (!/^[a-zA-Z]+(?:[ '-][a-zA-Z]+)*$/.test(fullName)) e.fullName = t("registerFullNameLatinOnly");
      }
      return e;
    },
    passwordErrors() {
      const e = {};
      if (this.passwordTouched.currentPassword && !this.passwordForm.currentPassword) {
        e.currentPassword = t("loginPasswordRequired");
      }

      if (this.passwordTouched.newPassword && !this.passwordForm.newPassword) {
        e.newPassword = t("registerPasswordRequired");
      } else if (this.passwordTouched.newPassword && this.passwordForm.newPassword.length < 8) {
        e.newPassword = t("registerPasswordMin");
      } else if (this.passwordTouched.newPassword && !/[A-Z]/.test(this.passwordForm.newPassword)) {
        e.newPassword = t("registerPasswordUpper");
      } else if (this.passwordTouched.newPassword && !/[a-z]/.test(this.passwordForm.newPassword)) {
        e.newPassword = t("registerPasswordLower");
      } else if (this.passwordTouched.newPassword && !/[0-9]/.test(this.passwordForm.newPassword)) {
        e.newPassword = t("registerPasswordDigit");
      } else if (this.passwordTouched.newPassword && /\s/.test(this.passwordForm.newPassword)) {
        e.newPassword = t("registerPasswordNoSpaces");
      }

      if (this.passwordTouched.confirmPassword && !this.passwordForm.confirmPassword) {
        e.confirmPassword = t("registerConfirmRequired");
      } else if (this.passwordTouched.confirmPassword && this.passwordForm.confirmPassword !== this.passwordForm.newPassword) {
        e.confirmPassword = t("registerConfirmMismatch");
      }

      return e;
    }
  },
  async mounted() {
    try {
      const res = await api.get("/user/profile");
      const d = res.data;
      this.user = {
        ...d,
        fullName: d.fullName || d.full_name || "",
        theme: d.theme || "light",
        language: d.language || "uk",
        lastSeenAt: d.lastSeenAt || null
      };
      applyUserPreferences(this.user);
    } catch (err) {
      if (err.response?.status === 401) this.logout();
      else this.error = t("profileLoadFailed");
    } finally {
      this.loading = false;
    }
  },
  methods: {
    t,
    toggleTheme() {
      this.isDark = !this.isDark;
      this.user.theme = this.isDark ? "dark" : "light";
      applyTheme(this.user.theme);
    },
    changeLanguage() {
      this.user.language = this.currentLanguage;
      applyLanguage(this.currentLanguage);
      this.$forceUpdate();
    },
    normalizeUsername() {
      this.user.username = this.user.username.replace(/\s+/g, "");
    },
    normalizeFullName() {
      this.user.fullName = this.user.fullName.replace(/\s{2,}/g, " ");
    },
    applyCurrentPreferences() {
      applyUserPreferences(this.user);
    },
    touch(field) {
      this.touched[field] = true;
    },
    touchPassword(field) {
      this.passwordTouched[field] = true;
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
          username: this.user.username.trim(),
          fullName: this.user.fullName.replace(/\s+/g, " ").trim(),
          full_name: this.user.fullName.replace(/\s+/g, " ").trim(),
          theme: this.user.theme,
          language: this.user.language
        });
        applyUserPreferences(this.user);
        this.message = t("profileSaved");
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
            this.error = t("profileUsernameExists", { username: this.user.username });
          } else {
            this.error = err.response?.data?.message || t("profileUpdateError");
          }
        }
      } finally {
        this.saving = false;
      }
    },
    async logout() {
      try {
        await api.post("/auth/logout");
      } catch (e) {
      } finally {
        disconnect();
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
        this.message = t("profileAvatarSaved");
        setTimeout(() => { this.message = ""; }, 3000);
      } catch (e) {
        this.error = t("profileAvatarUploadFailed");
      } finally {
        this.avatarUploading = false;
      }
    },

    async changePassword() {
      Object.keys(this.passwordTouched).forEach(key => {
        this.passwordTouched[key] = true;
      });

      if (Object.keys(this.passwordErrors).length > 0) {
        return;
      }

      this.savingPassword = true;
      this.error = "";
      this.message = "";

      try {
        await api.patch("/user/password", {
          currentPassword: this.passwordForm.currentPassword,
          newPassword: this.passwordForm.newPassword
        });

        this.passwordForm.currentPassword = "";
        this.passwordForm.newPassword = "";
        this.passwordForm.confirmPassword = "";
        Object.keys(this.passwordTouched).forEach(key => {
          this.passwordTouched[key] = false;
        });
        this.message = t("profilePasswordChanged");
        setTimeout(() => { this.message = ""; }, 3000);
      } catch (err) {
        if (err.response?.status === 401) {
          this.logout();
        } else {
          this.error = err.response?.data?.message || t("profileUpdateError");
        }
      } finally {
        this.savingPassword = false;
      }
    }
  }
};
</script>
<style scoped>
.page {
  min-height: 100vh;
  background: var(--app-bg);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32px 16px 60px;
  font-family: inherit;
}

.topbar {
  width: 100%;
  max-width: 500px;
  margin-bottom: 20px;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: transparent;
  border: 1px solid var(--border-color);
  font-size: 14px;
  font-weight: 500;
  color: var(--app-text);
  cursor: pointer;
  padding: 10px 14px;
  border-radius: 8px;
  transition: all 0.2s ease;
  font-family: inherit;
}

.back-btn:hover {
  background: var(--panel-bg-hover);
  border-color: var(--border-color);
  transform: translateX(-2px);
}

.back-btn svg {
  flex-shrink: 0;
  stroke: currentColor;
}

.card {
  width: 100%;
  max-width: 500px;
  background: var(--panel-bg);
  border-radius: 16px;
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border-color);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.05), rgba(124, 58, 237, 0.05));
  border-bottom: 1px solid var(--border-color);
}

.avatar-wrap {
  position: relative;
  cursor: pointer;
  margin-bottom: 16px;
}

.avatar-wrap:hover .avatar-overlay {
  opacity: 1;
}

.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: #649C2B;
  color: #fff;
  font-size: 32px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.avatar-img {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  display: block;
  background: var(--panel-bg-soft);
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.avatar-overlay svg {
  width: 20px;
  height: 20px;
  color: white;
}

.avatar-actions {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.btn-avatar-save {
  padding: 10px 18px;
  border-radius: 8px;
  border: none;
  background: #649C2B;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-avatar-save:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(100, 156, 43, 0.3);
}

.btn-avatar-save:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-avatar-cancel {
  padding: 10px 16px;
  border-radius: 8px;
  border: 1.5px solid var(--border-color);
  background: transparent;
  color: var(--app-text);
  font-size: 14px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-avatar-cancel:hover {
  background: var(--panel-bg-hover);
  border-color: var(--border-color-strong);
}

.avatar-name {
  font-size: 20px;
  font-weight: 700;
  color: var(--app-text);
  margin-bottom: 4px;
  text-align: center;
}

.avatar-email {
  font-size: 14px;
  color: var(--text-tertiary);
  text-align: center;
}

.form-section {
  padding: 32px;
  display: flex;
  flex-direction: column;
}

.loading {
  padding: 28px 32px;
  color: var(--text-tertiary);
  font-size: 14px;
  text-align: center;
}

.field {
  display: flex;
  flex-direction: column;
  margin-bottom: 20px;
}

.field label {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  color: var(--text-tertiary);
  margin-bottom: 8px;
}

input, select {
  padding: 11px 14px;
  border-radius: 8px;
  border: 1.5px solid var(--border-color);
  background: var(--app-bg-secondary);
  font-size: 14px;
  font-family: inherit;
  color: var(--app-text);
  outline: none;
  transition: all 0.2s ease;
}

input::placeholder, select::placeholder {
  color: var(--text-tertiary);
}

input:focus, select:focus {
  border-color: var(--accent);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

input:disabled {
  background: var(--panel-bg-soft);
  color: var(--text-tertiary);
  cursor: not-allowed;
  border-color: var(--border-color);
  opacity: 0.6;
}

.btn-save {
  padding: 12px;
  border-radius: 8px;
  border: none;
  background: #649C2B;
  color: #fff;
  font-weight: 600;
  font-size: 15px;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-top: 8px;
}

.btn-save:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(100, 156, 43, 0.3);
}

.btn-save:disabled {
  opacity: 0.5;
}

.password-section {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--border-color);
}

.password-section h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 16px;
  color: var(--app-text);
}

.btn-password {
  width: 100%;
  padding: 12px;
  border-radius: 8px;
  border: 1.5px solid var(--border-color);
  background: transparent;
  color: var(--app-text);
  font-weight: 600;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-password:hover:not(:disabled) {
  background: var(--panel-bg-hover);
  border-color: var(--border-color-strong);
}

.btn-password:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.msg-banner {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 16px;
  padding: 12px 14px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  border: 1px solid;
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.msg-success {
  background: var(--success-light);
  border-color: var(--success);
  color: var(--success-text);
}

html[data-theme="dark"] .msg-success {
  background: rgba(16, 185, 129, 0.1);
  border-color: rgba(16, 185, 129, 0.3);
  color: var(--success);
}

.msg-error {
  background: var(--error-light);
  border-color: var(--error);
  color: var(--error-text);
}

html[data-theme="dark"] .msg-error {
  background: rgba(239, 68, 68, 0.1);
  border-color: rgba(239, 68, 68, 0.3);
  color: var(--error);
}

.field-error {
  font-size: 13px;
  color: var(--error);
  margin-top: 6px;
  font-weight: 500;
}

.field-hint {
  font-size: 13px;
  color: var(--text-tertiary);
  margin-top: 6px;
}

.input-error {
  border-color: var(--error) !important;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1) !important;
}

.logout-area {
  padding: 24px 32px 28px;
  border-top: 1px solid var(--border-color);
  margin-top: 20px;
}

.btn-logout {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 12px 16px;
  border-radius: 8px;
  border: 1.5px solid var(--error);
  background: transparent;
  color: var(--error);
  font-weight: 600;
  font-size: 15px;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-logout:hover {
  background: rgba(239, 68, 68, 0.1);
  border-color: var(--error);
}

@media (max-width: 768px) {
  .page {
    padding: 24px 12px 50px;
  }

  .card {
    max-width: 100%;
  }

  .avatar-section {
    padding: 32px 24px;
  }

  .avatar {
    width: 70px;
    height: 70px;
    font-size: 28px;
  }

  .avatar-img {
    width: 70px;
    height: 70px;
  }

  .avatar-name {
    font-size: 18px;
  }

  .form-section {
    padding: 24px;
  }

  .field {
    margin-bottom: 16px;
  }
}

@media (max-width: 480px) {
  .page {
    padding: 16px 12px 40px;
  }

  .card {
    border-radius: 12px;
  }

  .avatar-section {
    padding: 24px 16px;
  }

  .avatar {
    width: 64px;
    height: 64px;
    font-size: 26px;
  }

  .avatar-img {
    width: 64px;
    height: 64px;
  }

  .avatar-name {
    font-size: 16px;
  }

  .avatar-email {
    font-size: 12px;
  }

  .form-section {
    padding: 20px 16px;
  }

  .field {
    margin-bottom: 14px;
  }

  input, select {
    font-size: 13px;
    padding: 10px 12px;
  }

  .btn-save, .btn-password, .btn-logout {
    font-size: 14px;
    padding: 10px 14px;
  }

  .msg-banner {
    font-size: 12px;
    padding: 10px 12px;
  }

  .logout-area {
    padding: 16px;
    margin-top: 16px;
  }
}

.theme-language-controls {
  display: flex;
  gap: 12px;
  align-items: center;
  z-index: 100;
}

.profile-controls {
  position: relative;
}

.theme-toggle {
  background: var(--panel-bg-hover);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: var(--app-text);
}

.theme-toggle:hover {
  background: var(--panel-bg-strong);
  transform: scale(1.05);
}

.language-select {
  background: var(--panel-bg-hover);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 8px 12px;
  color: var(--app-text);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: inherit;
}

.language-select:hover {
  background: var(--panel-bg-strong);
}

.language-select:focus {
  outline: none;
  border-color: var(--accent);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.language-select option {
  background: var(--panel-bg);
  color: var(--app-text);
}

</style>
