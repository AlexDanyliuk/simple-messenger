<template>
  <div class="auth-container">
    <div class="auth-form">
      <h2>Вхід</h2>

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
        <span v-else class="field-hint">Введіть пароль від акаунту</span>
      </div>

      <button @click="login" :disabled="loading">
        {{ loading ? 'Завантаження...' : 'Увійти' }}
      </button>

            <div v-if="serverError" class="msg-banner msg-error">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          {{ serverError }}
        </div>

      <a href="#" @click.prevent="goToRegister">
        Немає акаунту? Зареєструватись
      </a>
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  name: "LoginView",
  data() {
    return {
      email: "",
      password: "",
      loading: false,
      serverError: "",
      touched: { email: false, password: false }
    };
  },
  computed: {
    errors() {
      const e = {};
      if (this.touched.email) {
        if (!this.email) e.email = "Введіть email";
        else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.email)) e.email = "Невірний формат email";
      }
      if (this.touched.password) {
        if (!this.password) e.password = "Введіть пароль";
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
    async login() {
      if (!this.isValid()) return;
      this.loading = true;
      this.serverError = "";
      try {
        const response = await axios.post("/api/auth/sign-in", {
          email: this.email,
          password: this.password
        });
        localStorage.setItem("token", response.data.token);
        this.$router.push("/chats");
      } catch (err) {
        this.serverError = err.response?.data?.message || "Невірний email або пароль";
      } finally {
        this.loading = false;
      }
    },
    goToRegister() {
      this.$router.push("/register");
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