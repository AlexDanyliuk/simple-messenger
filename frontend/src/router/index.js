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
  { path: "/profile",  component: ProfileView  },

  {
    path: "/",
    component: ChatLayout,
    children: [
      {
        path: "chats",
        component: SelectChatPlaceholder
      },
      {
        path: "chat/:id",
        component: ChatView
      }
    ]
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;