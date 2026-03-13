window.global = window;

import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import "./style.css";
import { applyLanguage, applyTheme, getInitialPreferences } from "./services/userPreferences";

const initialPreferences = getInitialPreferences();
applyTheme(initialPreferences.theme);
applyLanguage(initialPreferences.language);

createApp(App)
  .use(router)
  .mount("#app");
