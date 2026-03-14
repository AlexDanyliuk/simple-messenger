<template>
  <div class="auth-container">
    <div class="theme-language-controls">
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

    <div class="auth-form">
      <h2>{{ t("loginTitle") }}</h2>

      <div class="field">
        <input
          v-model="email"
          @blur="touch('email')"
          @input="normalizeEmail"
          type="email"
          placeholder="Email"
          :class="{ 'input-error': errors.email }"
        />
        <span v-if="errors.email" class="field-error">{{ errors.email }}</span>
        <span v-else class="field-hint">{{ t("loginHintGmail") }}</span>
      </div>

      <div class="field">
        <input
          v-model="password"
          @blur="touch('password')"
          type="password"
          :placeholder="t('loginPasswordPlaceholder')"
          :class="{ 'input-error': errors.password }"
        />
        <span v-if="errors.password" class="field-error">{{ errors.password }}</span>
        <span v-else class="field-hint">{{ t("loginPasswordHint") }}</span>
      </div>

      <button @click="login" :disabled="loading">
        {{ loading ? t("commonLoading") : t("loginSubmit") }}
      </button>

      <a href="#" @click.prevent="goToForgotPassword" class="aux-link">
        {{ t("loginForgotPassword") }}
      </a>

            <div v-if="serverError" class="msg-banner msg-error">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          {{ serverError }}
        </div>

      <a href="#" @click.prevent="goToRegister">
        {{ t("loginNoAccount") }}
      </a>
    </div>
  </div>
</template>

<script>
import axios from "axios";
import { t, applyTheme, applyLanguage, getInitialPreferences } from "../services/userPreferences";

export default {
  name: "LoginView",
  data() {
    return {
      email: "",
      password: "",
      loading: false,
      serverError: "",
      touched: { email: false, password: false },
      currentLanguage: "uk",
      isDark: false
    };
  },
  mounted() {
    const prefs = getInitialPreferences();
    this.currentLanguage = prefs.language;
    this.isDark = prefs.theme === "dark";
  },
  computed: {
    errors() {
      const e = {};
      const email = this.email.trim().toLowerCase();
      if (this.touched.email) {
        if (!email) e.email = t("loginEmailRequired");
        else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) e.email = t("loginEmailInvalid");
        else if (!/@gmail\.com$/i.test(email)) e.email = t("loginEmailGmailOnly");
      }
      if (this.touched.password) {
        if (!this.password) e.password = t("loginPasswordRequired");
        else if (!this.password.trim()) e.password = t("loginPasswordSpacesOnly");
      }
      return e;
    }
  },
  methods: {
    t,
    toggleTheme() {
      this.isDark = !this.isDark;
      applyTheme(this.isDark ? "dark" : "light");
    },
    changeLanguage() {
      applyLanguage(this.currentLanguage);
      this.$forceUpdate();
    },
    normalizeEmail() {
      this.email = this.email.trim().toLowerCase();
    },
    touch(field) {
      this.touched[field] = true;
    },
    isValid() {
      Object.keys(this.touched).forEach(k => (this.touched[k] = true));
      return Object.keys(this.errors).length === 0;
    },
    async login() {
      if (!this.isValid()) return;
      this.loading = true;
      this.serverError = "";
      try {
        await axios.post("/api/auth/sign-in", {
          email: this.email.trim().toLowerCase(),
          password: this.password
        }, {
          withCredentials: true
        });
        this.$router.push("/chats");
      } catch (err) {
        this.serverError = err.response?.data?.message || t("loginInvalidCredentials");
      } finally {
        this.loading = false;
      }
    },
    goToRegister() {
      this.$router.push("/register");
    },
    goToForgotPassword() {
      this.$router.push("/forgot-password");
    }
  }
};
</script>

<style scoped>
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #ffffff;
  padding: 20px;
  transition: background 0.3s ease;
}

html[data-theme="dark"] .auth-container {
  background: var(--app-bg);
}

.theme-language-controls {
  position: absolute;
  top: 20px;
  right: 20px;
  display: flex;
  gap: 12px;
  align-items: center;
  z-index: 100;
}

.theme-toggle {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 8px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: white;
}

.theme-toggle:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.05);
}

html[data-theme="dark"] .theme-toggle {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.2);
  color: var(--app-text);
}

html[data-theme="dark"] .theme-toggle:hover {
  background: rgba(255, 255, 255, 0.15);
}

.language-select {
  background: rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(0, 0, 0, 0.2);
  border-radius: 8px;
  padding: 8px 12px;
  color: rgba(0, 0, 0, 0.8);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.language-select:hover {
  background: rgba(0, 0, 0, 0.15);
}

.language-select:focus {
  outline: none;
  background: rgba(0, 0, 0, 0.2);
}

html[data-theme="dark"] .language-select {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.2);
  color: var(--app-text);
}

html[data-theme="dark"] .language-select:hover {
  background: rgba(255, 255, 255, 0.15);
}

html[data-theme="dark"] .language-select:focus {
  background: rgba(255, 255, 255, 0.2);
}

.language-select option {
  background: var(--panel-bg);
  color: var(--app-text);
}

.auth-form {
  width: 100%;
  max-width: 420px;
  background: var(--panel-bg);
  border-radius: 16px;
  padding: 40px;
  box-shadow: var(--shadow-xl);
  display: flex;
  flex-direction: column;
  animation: slideUp 0.4s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

h2 {
  margin-bottom: 28px;
  text-align: center;
  font-size: 28px;
  color: #649C2B;
}

html[data-theme="dark"] h2 {
  -webkit-text-fill-color: unset;
  background: none;
  color: var(--app-text);
}

.field {
  margin-bottom: 18px;
  display: flex;
  flex-direction: column;
}

.field input {
  width: 100%;
  padding: 11px 14px;
  border: 1.5px solid var(--border-color);
  border-radius: 8px;
  background: var(--app-bg-secondary);
  color: var(--app-text);
  font-size: 15px;
  transition: all 0.2s ease;
}

.field input::placeholder {
  color: var(--text-tertiary);
}

.field input:focus {
  border-color: #649C2B;
  box-shadow: 0 0 0 3px rgba(100, 156, 43, 0.1);
}

.field input.input-error {
  border-color: var(--error);
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1);
}

.field-hint {
  font-size: 13px;
  color: var(--text-tertiary);
  margin-top: 6px;
  font-weight: 400;
}

.field-error {
  font-size: 13px;
  color: var(--error);
  margin-top: 6px;
  font-weight: 500;
}

.auth-form button {
  width: 100%;
  padding: 12px 20px;
  background: #649C2B;
  color: white;
  font-weight: 600;
  border-radius: 8px;
  margin: 20px 0 0 0;
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 15px;
}

.auth-form button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(100, 156, 43, 0.3);
}

.auth-form button:active {
  transform: translateY(0);
}

.auth-form button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.msg-banner {
  padding: 12px 14px;
  border-radius: 8px;
  margin: 16px 0;
  font-size: 14px;
  display: flex;
  gap: 8px;
  align-items: center;
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

.auth-form a {
  display: block;
  text-align: center;
  margin-top: 12px;
  color: var(--accent);
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  transition: color 0.2s ease;
}

.auth-form a:hover {
  color: #649C2B;
  text-decoration: underline;
}

.aux-link {
  margin-bottom: 8px;
}

@media (max-width: 768px) {
  .auth-form {
    padding: 32px 24px;
    border-radius: 12px;
  }

  h2 {
    font-size: 24px;
    margin-bottom: 24px;
  }

  .field {
    margin-bottom: 16px;
  }

  .field input {
    font-size: 14px;
  }

  .auth-form button {
    padding: 11px 18px;
    font-size: 14px;
    margin: 16px 0 0 0;
  }
}

@media (max-width: 480px) {
  .auth-container {
    padding: 16px;
  }

  .auth-form {
    width: 100%;
    padding: 24px 16px;
    border-radius: 12px;
  }

  h2 {
    font-size: 22px;
    margin-bottom: 20px;
  }

  .field {
    margin-bottom: 14px;
  }

  .field input {
    font-size: 14px;
    padding: 10px 12px;
  }

  .auth-form button {
    padding: 10px 16px;
    font-size: 14px;
    margin: 14px 0 0 0;
  }

  .auth-form a {
    font-size: 13px;
    margin-top: 10px;
  }
}
</style>