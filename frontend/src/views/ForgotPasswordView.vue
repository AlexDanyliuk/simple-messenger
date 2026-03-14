<template>
  <div class="auth-container">
    <div class="auth-form">
      <h2>{{ t("forgotPasswordTitle") }}</h2>

      <div class="field">
        <input
          v-model="email"
          type="email"
          @blur="touch('email')"
          @input="normalizeEmail"
          :placeholder="t('commonEmail')"
          :class="{ 'input-error': errors.email }"
        />
        <span v-if="errors.email" class="field-error">{{ errors.email }}</span>
      </div>

      <button @click="requestCode" :disabled="loadingRequest">
        {{ loadingRequest ? t("commonLoading") : t("forgotPasswordRequestCode") }}
      </button>

      <div class="field">
        <input
          v-model="code"
          type="text"
          @blur="touch('code')"
          maxlength="6"
          :placeholder="t('forgotPasswordCodePlaceholder')"
          :class="{ 'input-error': errors.code }"
        />
        <span v-if="errors.code" class="field-error">{{ errors.code }}</span>
      </div>

      <div class="field">
        <input
          v-model="newPassword"
          type="password"
          @blur="touch('newPassword')"
          :placeholder="t('forgotPasswordNewPassword')"
          :class="{ 'input-error': errors.newPassword }"
        />
        <span v-if="errors.newPassword" class="field-error">{{ errors.newPassword }}</span>
      </div>

      <div class="field">
        <input
          v-model="confirmPassword"
          type="password"
          @blur="touch('confirmPassword')"
          :placeholder="t('forgotPasswordConfirmPassword')"
          :class="{ 'input-error': errors.confirmPassword }"
        />
        <span v-if="errors.confirmPassword" class="field-error">{{ errors.confirmPassword }}</span>
      </div>

      <button @click="confirmReset" :disabled="loadingReset">
        {{ loadingReset ? t("commonLoading") : t("forgotPasswordSubmit") }}
      </button>

      <div v-if="message" class="msg-banner msg-success">{{ message }}</div>
      <div v-if="error" class="msg-banner msg-error">{{ error }}</div>

      <a href="#" @click.prevent="$router.push('/login')">
        {{ t("forgotPasswordBackToLogin") }}
      </a>
    </div>
  </div>
</template>

<script>
import axios from "axios";
import { t } from "../services/userPreferences";

export default {
  name: "ForgotPasswordView",
  data() {
    return {
      email: "",
      code: "",
      newPassword: "",
      confirmPassword: "",
      loadingRequest: false,
      loadingReset: false,
      message: "",
      error: "",
      touched: {
        email: false,
        code: false,
        newPassword: false,
        confirmPassword: false
      }
    };
  },
  computed: {
    errors() {
      const e = {};
      const email = this.email.trim().toLowerCase();
      if (this.touched.email) {
        if (!email) e.email = t("registerEmailRequired");
        else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) e.email = t("registerEmailInvalid");
        else if (!/@gmail\.com$/i.test(email)) e.email = t("registerEmailGmailOnly");
      }

      if (this.touched.code && this.code && !/^\d{6}$/.test(this.code)) e.code = t("forgotPasswordCodeInvalid");

      if (this.touched.newPassword && this.newPassword) {
        if (this.newPassword.length < 8) e.newPassword = t("registerPasswordMin");
        else if (!/[A-Z]/.test(this.newPassword)) e.newPassword = t("registerPasswordUpper");
        else if (!/[a-z]/.test(this.newPassword)) e.newPassword = t("registerPasswordLower");
        else if (!/[0-9]/.test(this.newPassword)) e.newPassword = t("registerPasswordDigit");
        else if (/\s/.test(this.newPassword)) e.newPassword = t("registerPasswordNoSpaces");
      }

      if (this.touched.confirmPassword && this.confirmPassword && this.confirmPassword !== this.newPassword) {
        e.confirmPassword = t("registerConfirmMismatch");
      }

      return e;
    }
  },
  methods: {
    t,
    touch(field) {
      this.touched[field] = true;
    },
    normalizeEmail() {
      this.email = this.email.trim().toLowerCase();
    },
    async requestCode() {
      this.touched.email = true;
      this.normalizeEmail();
      if (this.errors.email) return;

      this.loadingRequest = true;
      this.error = "";
      this.message = "";

      try {
        const res = await axios.post("/api/auth/forgot-password/request", { email: this.email });
        this.message = res.data?.message || t("forgotPasswordRequestSent");
      } catch (err) {
        this.error = err.response?.data?.message || t("forgotPasswordRequestFailed");
      } finally {
        this.loadingRequest = false;
      }
    },
    async confirmReset() {
      Object.keys(this.touched).forEach(key => {
        this.touched[key] = true;
      });
      this.normalizeEmail();
      if (this.errors.email || this.errors.code || this.errors.newPassword || this.errors.confirmPassword) {
        return;
      }
      if (!this.code) {
        this.error = t("forgotPasswordCodeRequired");
        return;
      }
      if (!this.newPassword) {
        this.error = t("registerPasswordRequired");
        return;
      }
      if (!this.confirmPassword) {
        this.error = t("registerConfirmRequired");
        return;
      }

      this.loadingReset = true;
      this.error = "";
      this.message = "";

      try {
        await axios.post("/api/auth/forgot-password/confirm", {
          email: this.email,
          code: this.code,
          newPassword: this.newPassword
        });
        this.message = t("forgotPasswordResetSuccess");
        this.code = "";
        this.newPassword = "";
        this.confirmPassword = "";
      } catch (err) {
        this.error = err.response?.data?.message || t("forgotPasswordResetFailed");
      } finally {
        this.loadingReset = false;
      }
    }
  }
};
</script>

<style scoped>
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: #ffffff;
  transition: background 0.3s ease;
}

html[data-theme="dark"] .auth-container {
  background: var(--app-bg);
}

.auth-form {
  width: 380px;
  background: #ffffff;
  border-radius: 16px;
  padding: 40px 32px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.07);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

html[data-theme="dark"] .auth-form {
  background: var(--panel-bg);
  box-shadow: 0 4px 24px rgba(0,0,0,0.3);
}

h2 {
  font-size: 22px;
  margin-bottom: 8px;
  color: #649C2B;
}

html[data-theme="dark"] h2 {
  color: #649C2B;
}

.field {
  display: flex;
  flex-direction: column;
}

input {
  padding: 12px 16px;
  border-radius: 10px;
  border: 1px solid #e4e4e4;
  background: #f9f9f9;
  font-size: 14px;
  color: #0f172a;
  font-family: inherit;
}

html[data-theme="dark"] input {
  border-color: var(--border-color);
  background: var(--app-bg-secondary);
  color: var(--app-text);
}

button {
  padding: 13px;
  border-radius: 10px;
  border: none;
  background: #649C2B;
  color: #ffffff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

button:hover:not(:disabled) {
  background: #578c24;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(100, 156, 43, 0.3);
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.field-error {
  font-size: 12px;
  color: #be123c;
  margin-top: 6px;
}

.msg-banner {
  margin-top: 6px;
  padding: 10px 12px;
  border-radius: 10px;
  font-size: 13px;
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

.input-error {
  border-color: #e11d48 !important;
}

html[data-theme="dark"] input:focus {
  border-color: #649C2B;
  box-shadow: 0 0 0 3px rgba(100, 156, 43, 0.1);
}

a {
  margin-top: 8px;
  text-align: center;
  color: #649C2B;
  text-decoration: none;
  transition: color 0.2s ease;
}

a:hover {
  color: #578c24;
  text-decoration: underline;
}

html[data-theme="dark"] a {
  color: #7ab637;
}

html[data-theme="dark"] a:hover {
  color: #649C2B;
}

@media (max-width: 480px) {
  .auth-form {
    width: calc(100% - 32px);
    padding: 30px 20px;
  }
}
</style>
