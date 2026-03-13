<template>
  <div class="auth-container">
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
import { t } from "../services/userPreferences";

export default {
  name: "RegisterView",
  data() {
    return {
      username: "",
      fullName: "",
      email: "",
      password: "",
      confirmPassword: "",
      loading: false,
      serverError: "",
      touched: { username: false, fullName: false, email: false, password: false, confirmPassword: false }
    };
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
      try {
        await axios.post("/api/user/registration", {
          username: this.username.trim(),
          fullName: this.fullName.replace(/\s+/g, " ").trim(),
          email: this.email.trim().toLowerCase(),
          password: this.password
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
  height: 100vh;
  background: #f7f7f7;
}

.auth-form {
  width: 360px;
  background: #ffffff;
  border-radius: 16px;
  padding: 48px 40px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.07);
  display: flex;
  flex-direction: column;
  animation: fadeUp 0.3s ease;
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(16px); }
  to   { opacity: 1; transform: translateY(0); }
}

@media (max-width: 480px) {
  .auth-form {
    width: calc(100% - 32px);
    padding: 32px 24px;
    margin: 16px;
    border-radius: 12px;
  }

  h2 {
    font-size: 20px;
    margin-bottom: 24px;
  }

  input {
    margin-bottom: 12px;
    padding: 11px 14px;
    font-size: 13px;
  }

  button {
    padding: 12px;
    font-size: 13px;
  }

  a {
    margin-top: 16px;
    font-size: 12px;
  }

  .field {
    margin-bottom: 4px;
  }
}

h2 {
  font-size: 22px;
  font-weight: 700;
  color: #111111;
  margin-bottom: 32px;
  letter-spacing: -0.3px;
}

input {
  margin-bottom: 14px;
  padding: 12px 16px;
  border-radius: 10px;
  border: 1px solid #e4e4e4;
  background: #f9f9f9;
  font-size: 14px;
  font-family: inherit;
  color: #111111;
  outline: none;
  transition: all 0.15s;
}

input::placeholder {
  color: #bbbbbb;
}

input:focus {
  border-color: #111111;
  background: #ffffff;
}

button {
  margin-top: 6px;
  padding: 13px;
  border-radius: 10px;
  background: #111111;
  color: #ffffff;
  font-size: 14px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: opacity 0.15s;
}

button:hover {
  opacity: 0.8;
}

.error {
  color: #cc0000;
  font-size: 13px;
  margin-top: 12px;
  text-align: center;
}

a {
  margin-top: 20px;
  text-align: center;
  font-size: 13px;
  color: #666666;
  transition: color 0.15s;
}

a:hover {
  color: #111111;
}

.field {
  display: flex;
  flex-direction: column;
}

.input-error {
  border-color: #e11d48 !important;
  background: linear-gradient(180deg, #fff8f8 0%, #fff1f2 100%) !important;
  box-shadow: 0 0 0 3px rgba(225, 29, 72, 0.08);
}

.field-error {
  display: inline-flex;
  align-items: center;
  width: 100%;
  font-size: 12px;
  line-height: 1.45;
  color: #be123c;
  background: #fff1f2;
  border: 1px solid #fecdd3;
  border-radius: 10px;
  padding: 8px 10px;
  margin-top: -4px;
  margin-bottom: 10px;
  animation: fadeIn 0.2s ease;
}

.field-hint {
  font-size: 12px;
  color: #667085;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 8px 10px;
  margin-top: -4px;
  margin-bottom: 10px;
}

.msg-banner {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
  padding: 11px 14px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 500;
  animation: fadeIn 0.2s ease;
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

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>