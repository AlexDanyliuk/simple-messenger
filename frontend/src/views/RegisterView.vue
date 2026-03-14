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
      <h2>{{ t("registerTitle") }}</h2>

      <div class="field">
        <input
          v-model="username"
          @blur="touch('username')"
          @input="normalizeUsername"
          type="text"
          :placeholder="t('registerUsernamePlaceholder')"
          :class="{ 'input-error': errors.username || username.length > 20 }"
        />
        <span v-if="username.length > 20" class="field-error">{{ t("registerUsernameMax") }}</span>
        <span v-else-if="errors.username" class="field-error">{{ errors.username }}</span>
      </div>

      <div class="field">
        <input
          v-model="fullName"
          @blur="touch('fullName')"
          @input="normalizeFullName"
          type="text"
          :placeholder="t('registerFullNamePlaceholder')"
          :class="{ 'input-error': errors.fullName }"
        />
        <span v-if="errors.fullName" class="field-error">{{ errors.fullName }}</span>
      </div>

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
        <span v-else class="field-hint">{{ t("registerEmailHint") }}</span>
      </div>

      <button @click="requestVerificationCode" :disabled="loadingCode" class="btn-secondary">
        {{ loadingCode ? t("commonLoading") : t("registerRequestCode") }}
      </button>

      <div class="field">
        <input
          v-model="verificationCode"
          @blur="touch('verificationCode')"
          type="text"
          maxlength="6"
          :placeholder="t('registerVerificationCodePlaceholder')"
          :class="{ 'input-error': errors.verificationCode }"
        />
        <span v-if="errors.verificationCode" class="field-error">{{ errors.verificationCode }}</span>
      </div>

      <div class="field">
        <input
          v-model="password"
          @blur="touch('password')"
          type="password"
          :placeholder="t('registerPasswordPlaceholder')"
          :class="{ 'input-error': errors.password }"
        />
        <span v-if="errors.password" class="field-error">{{ errors.password }}</span>
        <span v-else class="field-hint">{{ t("registerPasswordHint") }}</span>
      </div>

      <div class="field">
        <input
          v-model="confirmPassword"
          @blur="touch('confirmPassword')"
          type="password"
          :placeholder="t('registerConfirmPasswordPlaceholder')"
          :class="{ 'input-error': errors.confirmPassword }"
        />
        <span v-if="errors.confirmPassword" class="field-error">{{ errors.confirmPassword }}</span>
      </div>

      <button @click="register" :disabled="loading">
        {{ loading ? t("commonLoading") : t("registerSubmit") }}
      </button>

      <div v-if="infoMessage" class="msg-banner msg-success">
        {{ infoMessage }}
      </div>

            <div v-if="serverError" class="msg-banner msg-error">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          {{ serverError }}
        </div>

      <a href="#" @click.prevent="goToLogin">
        {{ t("registerHaveAccount") }}
      </a>
    </div>
  </div>
</template>

<script>
import axios from "axios";
import { t, applyTheme, applyLanguage, getInitialPreferences } from "../services/userPreferences";

export default {
  name: "RegisterView",
  data() {
    return {
      username: "",
      fullName: "",
      email: "",
      verificationCode: "",
      password: "",
      confirmPassword: "",
      loading: false,
      loadingCode: false,
      infoMessage: "",
      serverError: "",
      touched: { username: false, fullName: false, email: false, verificationCode: false, password: false, confirmPassword: false },
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
      const username = this.username.trim();
      const fullName = this.fullName.replace(/\s+/g, " ").trim();
      const email = this.email.trim().toLowerCase();
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
      if (this.touched.email) {
        if (!email) e.email = t("registerEmailRequired");
        else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) e.email = t("registerEmailInvalid");
        else if (!/@gmail\.com$/i.test(email)) e.email = t("registerEmailGmailOnly");
      }
      if (this.touched.verificationCode) {
        if (!this.verificationCode) e.verificationCode = t("registerVerificationCodeRequired");
        else if (!/^\d{6}$/.test(this.verificationCode)) e.verificationCode = t("registerVerificationCodeInvalid");
      }
      if (this.touched.password) {
        if (!this.password) e.password = t("registerPasswordRequired");
        else if (this.password.length < 8) e.password = t("registerPasswordMin");
        else if (!/[A-Z]/.test(this.password)) e.password = t("registerPasswordUpper");
        else if (!/[a-z]/.test(this.password)) e.password = t("registerPasswordLower");
        else if (!/[0-9]/.test(this.password)) e.password = t("registerPasswordDigit");
        else if (/\s/.test(this.password)) e.password = t("registerPasswordNoSpaces");
      }
      if (this.touched.confirmPassword) {
        if (!this.confirmPassword) e.confirmPassword = t("registerConfirmRequired");
        else if (this.confirmPassword !== this.password) e.confirmPassword = t("registerConfirmMismatch");
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
    normalizeUsername() {
      this.username = this.username.replace(/\s+/g, "");
    },
    normalizeFullName() {
      this.fullName = this.fullName.replace(/\s{2,}/g, " ");
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
    async register() {
      if (!this.isValid()) return;
      this.loading = true;
      this.serverError = "";
      this.infoMessage = "";
      try {
        await axios.post("/api/user/registration", {
          username: this.username.trim(),
          fullName: this.fullName.replace(/\s+/g, " ").trim(),
          email: this.email.trim().toLowerCase(),
          password: this.password,
          verificationCode: this.verificationCode.trim()
        });
        this.$router.push("/login");
      } catch (err) {
        const msg = (err.response?.data?.message || "").toLowerCase();
        if (err.response?.status === 400) {
          this.serverError = err.response?.data?.message || t("registerInvalidData");
        } else if (msg.includes("username") || (msg.includes("user") && msg.includes("exist"))) {
          this.serverError = t("registerUserExists", { username: this.username });
        } else if (msg.includes("email") || msg.includes("mail")) {
          this.serverError = t("registerEmailUsed", { email: this.email });
        } else if (err.response?.status === 500 || err.response?.status === 409) {
          this.serverError = t("registerUserExists", { username: this.username });
        } else {
          this.serverError = t("registerGenericError");
        }
      } finally {
        this.loading = false;
      }
    },
    async requestVerificationCode() {
      this.touched.email = true;
      this.normalizeEmail();
      if (this.errors.email) {
        return;
      }

      this.loadingCode = true;
      this.serverError = "";
      this.infoMessage = "";

      try {
        await axios.post("/api/auth/register/request-code", {
          email: this.email.trim().toLowerCase()
        });
        this.infoMessage = t("registerRequestCodeSent");
      } catch (err) {
        this.serverError = err.response?.data?.message || t("registerRequestCodeFailed");
      } finally {
        this.loadingCode = false;
      }
    },
    goToLogin() {
      this.$router.push("/login");
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

.btn-secondary {
  background: var(--panel-bg-soft) !important;
  color: var(--accent) !important;
  border: 1.5px solid var(--accent) !important;
}

.btn-secondary:hover:not(:disabled) {
  background: var(--accent-lighter) !important;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15) !important;
}

html[data-theme="dark"] .btn-secondary {
  background: var(--panel-bg-soft) !important;
  color: var(--accent) !important;
  border: 1.5px solid #649C2B !important;
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
