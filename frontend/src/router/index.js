import { createRouter, createWebHistory } from "vue-router";
import { defineComponent, h } from "vue";

import LoginView    from "../views/LoginView.vue";
import RegisterView from "../views/RegisterView.vue";
import ProfileView  from "../views/ProfileView.vue";
import ChatLayout   from "../layouts/ChatLayout.vue";
import ChatView     from "../views/ChatView.vue";

// Замість inline template — defineComponent з render-функцією
const SelectChatPlaceholder = defineComponent({
  render() {
    return h("div", { style: "padding: 20px; color: #aaa; font-size: 14px;" }, "Оберіть чат");
  }
});

const routes = [
  { path: "/", redirect: "/profile" },

  { path: "/login",    component: LoginView    },
  { path: "/register", component: RegisterView },
  { path: "/profile",  component: ProfileView, meta: { requiresAuth: true }  },

  {
    path: "/",
    component: ChatLayout,
    children: [
      {
        path: "chats",
        component: SelectChatPlaceholder,
        meta: { requiresAuth: true }
      },
      {
        path: "chat/:id",
        component: ChatView,
        meta: { requiresAuth: true }
      }
    ]
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// Функція для перевірки авторизації
function isAuthenticated() {
  return !!localStorage.getItem("token");
}

// Навігаційний guard
router.beforeEach((to, from, next) => {
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
  const isLoggedIn = isAuthenticated();

  if (requiresAuth && !isLoggedIn) {
    // Якщо маршрут вимагає авторизацію, а користувач не авторизований
    next("/login");
  } else if ((to.path === "/login" || to.path === "/register") && isLoggedIn) {
    // Якщо користувач уже авторизований і намагається зайти на login/register
    next("/profile");
  } else {
    // Дозволяємо перехід
    next();
  }
});

export default router;