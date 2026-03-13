import { createRouter, createWebHistory } from "vue-router";
import { defineComponent, h } from "vue";
import { t } from "../services/userPreferences";
import LoginView    from "../views/LoginView.vue";
import RegisterView from "../views/RegisterView.vue";
import ProfileView  from "../views/ProfileView.vue";
import ChatLayout   from "../layouts/ChatLayout.vue";
import ChatView     from "../views/ChatView.vue";

async function hasActiveSession() {
  const response = await fetch("/api/user/profile", {
    credentials: "include"
  });

  return response.ok;
}

const SelectChatPlaceholder = defineComponent({
  render() {
    return h("div", { style: "padding: 20px; color: #aaa; font-size: 14px;" }, t("chatSelectPlaceholder"));
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

router.beforeEach(async (to, from, next) => {
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
  const shouldCheckSession = requiresAuth || to.path === "/login" || to.path === "/register";
  const isLoggedIn = shouldCheckSession ? await hasActiveSession() : false;

  if (requiresAuth && !isLoggedIn) {
    next("/login");
  } else if ((to.path === "/login" || to.path === "/register") && isLoggedIn) {
    next("/profile");
  } else {
    next();
  }
});

export default router;