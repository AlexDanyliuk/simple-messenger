<template>
  <div class="auth-container">
    <div class="auth-form">
      <h2>Реєстрація</h2>

      <div class="field">
        <input
          v-model="username"
          @blur="touch('username')"
          @input="username = username.toLowerCase()"
          type="text"
          placeholder="Імʼя користувача"
          :class="{ 'input-error': errors.username || username.length > 20 }"
        />
        <span v-if="username.length > 20" class="field-error">Максимум 20 символів</span>
        <span v-else-if="errors.username" class="field-error">{{ errors.username }}</span>
      </div>

      <div class="field">
        <input
          v-model="fullName"
          @blur="touch('fullName')"
          type="text"
          placeholder="Повне імʼя"
          :class="{ 'input-error': errors.fullName }"
        />
        <span v-if="errors.fullName" class="field-error">{{ errors.fullName }}</span>
      </div>

      <div class="field">
        <input
          v-model="email"
          @blur="touch('email')"
          type="email"
          placeholder="Email"
          :class="{ 'input-error': errors.email }"
        />
        <span v-if="errors.email" class="field-error">{{ errors.email }}</span>
        <span v-else class="field-hint">Вкажіть дійсну адресу, наприклад user@gmail.com</span>
      </div>

      <div class="field">
        <input
          v-model="password"
          @blur="touch('password')"
          type="password"
          placeholder="Пароль"
          :class="{ 'input-error': errors.password }"
        />
        <span v-if="errors.password" class="field-error">{{ errors.password }}</span>
        <span v-else class="field-hint">Не менше 8 символів</span>
      </div>

      <div class="field">
        <input
          v-model="confirmPassword"
          @blur="touch('confirmPassword')"
          type="password"
          placeholder="Підтвердіть пароль"
          :class="{ 'input-error': errors.confirmPassword }"
        />
        <span v-if="errors.confirmPassword" class="field-error">{{ errors.confirmPassword }}</span>
      </div>

      <button @click="register" :disabled="loading">
        {{ loading ? 'Завантаження...' : 'Зареєструватися' }}
      </button>

            <div v-if="serverError" class="msg-banner msg-error">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          {{ serverError }}
        </div>

      <a href="#" @click.prevent="goToLogin">
        Вже є акаунт? Увійти
      </a>
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  name: "RegisterView",
  data() {
    return {
      username: "",
      fullName: "",
      email: "",
      password: "",
      confirmPassword: "",
      confirmPassword: "",
      loading: false,
      serverError: "",
      touched: { username: false, fullName: false, email: false, password: false, confirmPassword: false }
    };
  },
  computed: {
    errors() {
      const e = {};
      if (this.touched.username) {
        if (!this.username) e.username = "Введіть імʼя користувача";
        else if (this.username.length < 3) e.username = "Мінімум 3 символи";
        else if (this.username.length > 20) e.username = "Максимум 20 символів";
        else if (!/^[a-z0-9_.]+$/.test(this.username)) e.username = "Лише малі літери, цифри, _ .";
      }
      if (this.touched.fullName) {
        if (!this.fullName.trim()) e.fullName = "Введіть повне імʼя";
      }
      if (this.touched.email) {
        if (!this.email) e.email = "Введіть email";
        else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.email)) e.email = "Невірний формат email";
      }
      if (this.touched.password) {
        if (!this.password) e.password = "Введіть пароль";
        else if (this.password.length < 8) e.password = "Мінімум 8 символів";
      }
      if (this.touched.confirmPassword) {
        if (!this.confirmPassword) e.confirmPassword = "Підтвердіть пароль";
        else if (this.confirmPassword !== this.password) e.confirmPassword = "Паролі не збігаються";
      }
      return e;
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
    async register() {
      if (!this.isValid()) return;
      this.loading = true;
      this.serverError = "";
      try {
        await axios.post("/api/user/registration", {
          username: this.username,
          fullName: this.fullName,
          email: this.email,
          password: this.password
        });
        this.$router.push("/login");
      } catch (err) {
        const msg = (err.response?.data?.message || "").toLowerCase();
        if (msg.includes("username") || (msg.includes("user") && msg.includes("exist"))) {
          this.serverError = `Користувач з іменем "${this.username}" вже існує`;
        } else if (msg.includes("email") || msg.includes("mail")) {
          this.serverError = `Пошта "${this.email}" вже використовується`;
        } else if (err.response?.status === 500 || err.response?.status === 409 || err.response?.status === 400) {
          this.serverError = `Користувач з іменем "${this.username}" вже існує`;
        } else {
          this.serverError = "Помилка реєстрації. Спробуйте ще раз.";
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
  border-color: #e53e3e !important;
  background: #fff5f5 !important;
}

.field-error {
  font-size: 12px;
  color: #e53e3e;
  margin-top: -8px;
  margin-bottom: 10px;
  animation: fadeIn 0.2s ease;
}

.field-hint {
  font-size: 12px;
  color: #aaaaaa;
  margin-top: -8px;
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