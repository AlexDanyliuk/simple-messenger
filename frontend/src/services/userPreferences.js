import { reactive } from "vue";

const translations = {
  uk: {
    online: "В мережі",
    offline: "Не в мережі",
    lastSeen: "Був(ла)",
    typing: "друкує...",
    timeJustNow: "щойно",
    timeMinAgo: "{count} хв тому",
    timeHourAgo: "{count} год тому",
    commonLoading: "Завантаження...",
    commonCancel: "Скасувати",
    commonSave: "Зберегти",
    commonEmail: "Email",
    commonLogout: "Вийти",
    chatSelectPlaceholder: "Оберіть чат",
    chatSearchPlaceholder: "Пошук",
    chatSearchMinChars: "Введіть щонайменше 3 символи",
    chatSearchLoading: "Пошук...",
    chatSearchNotFound: "Користувачів не знайдено",
    chatProfileHint: "переглянути профіль",
    chatEditProfile: "Редагувати профіль",
    chatMessagePlaceholder: "Написати повідомлення...",
    chatSend: "Надіслати",
    chatEdit: "Редагувати",
    chatEdited: "ред.",
    loginTitle: "Вхід",
    loginHintGmail: "Дозволена тільки адреса Gmail, наприклад user@gmail.com",
    loginPasswordPlaceholder: "Пароль",
    loginPasswordHint: "Введіть пароль від акаунту",
    loginSubmit: "Увійти",
    loginNoAccount: "Немає акаунту? Зареєструватись",
    loginEmailRequired: "Введіть email",
    loginEmailInvalid: "Невірний формат email",
    loginEmailGmailOnly: "Дозволена тільки пошта @gmail.com",
    loginPasswordRequired: "Введіть пароль",
    loginPasswordSpacesOnly: "Пароль не може складатися лише з пробілів",
    loginInvalidCredentials: "Невірний email або пароль",
    registerTitle: "Реєстрація",
    registerUsernamePlaceholder: "Ім'я користувача, наприклад taras",
    registerUsernameMax: "Максимум 20 символів",
    registerFullNamePlaceholder: "Повне ім'я, наприклад Taras Shevchenko",
    registerEmailHint: "Вкажіть адресу Gmail, наприклад user@gmail.com",
    registerPasswordPlaceholder: "Пароль",
    registerPasswordHint: "Мін. 8 символів, велика літера, мала літера і цифра",
    registerConfirmPasswordPlaceholder: "Підтвердіть пароль",
    registerSubmit: "Зареєструватися",
    registerHaveAccount: "Вже є акаунт? Увійти",
    registerUsernameRequired: "Введіть ім'я користувача",
    registerUsernameMin: "Мінімум 3 символи",
    registerUsernameLatinOnly: "Ім'я користувача має містити тільки англійські літери",
    registerFullNameRequired: "Введіть повне ім'я",
    registerFullNameMin: "Мінімум 2 символи",
    registerFullNameMax: "Максимум 50 символів",
    registerFullNameLatinOnly: "Повне ім'я має містити тільки англійські літери",
    registerEmailRequired: "Введіть email",
    registerEmailInvalid: "Невірний формат email",
    registerEmailGmailOnly: "Використайте адресу Gmail",
    registerPasswordRequired: "Введіть пароль",
    registerPasswordMin: "Мінімум 8 символів",
    registerPasswordUpper: "Потрібна хоча б одна велика літера",
    registerPasswordLower: "Потрібна хоча б одна мала літера",
    registerPasswordDigit: "Потрібна хоча б одна цифра",
    registerPasswordNoSpaces: "Пароль не повинен містити пробіли",
    registerConfirmRequired: "Підтвердіть пароль",
    registerConfirmMismatch: "Паролі не збігаються",
    registerInvalidData: "Невірні дані. Перевірте введення.",
    registerUserExists: "Користувач з іменем \"{username}\" вже існує",
    registerEmailUsed: "Пошта \"{email}\" вже використовується",
    registerGenericError: "Помилка реєстрації. Спробуйте ще раз.",
    profileBack: "Назад до чатів",
    profileTitleUsername: "Ім'я користувача",
    profileTitleFullName: "Повне ім'я",
    profileTitleEmail: "Email",
    profileTitleTheme: "Тема",
    profileTitleLanguage: "Мова",
    profileThemeLight: "Світла",
    profileThemeDark: "Темна",
    profileLangUk: "Українська",
    profileLangEn: "English",
    profileLastSeen: "Останній раз в мережі",
    profileSave: "Зберегти зміни",
    profileSaving: "Збереження...",
    profileAvatarTitle: "Змінити аватарку",
    profileAvatarSave: "Зберегти фото",
    profileAvatarSaved: "Аватарку збережено ✓",
    profileAvatarUploadFailed: "Не вдалося завантажити аватарку",
    profileUsernamePlaceholder: "Ім'я користувача, наприклад taras",
    profileFullNamePlaceholder: "Повне ім'я, наприклад Taras Shevchenko",
    profileLoadFailed: "Не вдалося завантажити профіль",
    profileSaved: "Збережено ✓",
    profileUpdateError: "Помилка оновлення",
    profileUsernameExists: "Користувач з іменем \"{username}\" вже існує"
  },
  en: {
    online: "Online",
    offline: "Offline",
    lastSeen: "Last seen",
    typing: "typing...",
    timeJustNow: "just now",
    timeMinAgo: "{count} min ago",
    timeHourAgo: "{count} h ago",
    commonLoading: "Loading...",
    commonCancel: "Cancel",
    commonSave: "Save",
    commonEmail: "Email",
    commonLogout: "Log out",
    chatSelectPlaceholder: "Select a chat",
    chatSearchPlaceholder: "Search",
    chatSearchMinChars: "Type at least 3 characters",
    chatSearchLoading: "Searching...",
    chatSearchNotFound: "No users found",
    chatProfileHint: "view profile",
    chatEditProfile: "Edit profile",
    chatMessagePlaceholder: "Write a message...",
    chatSend: "Send",
    chatEdit: "Edit",
    chatEdited: "edited",
    loginTitle: "Sign in",
    loginHintGmail: "Only Gmail addresses are allowed, for example user@gmail.com",
    loginPasswordPlaceholder: "Password",
    loginPasswordHint: "Enter your account password",
    loginSubmit: "Sign in",
    loginNoAccount: "No account? Register",
    loginEmailRequired: "Enter email",
    loginEmailInvalid: "Invalid email format",
    loginEmailGmailOnly: "Only @gmail.com email is allowed",
    loginPasswordRequired: "Enter password",
    loginPasswordSpacesOnly: "Password cannot contain only spaces",
    loginInvalidCredentials: "Invalid email or password",
    registerTitle: "Registration",
    registerUsernamePlaceholder: "Username, for example taras",
    registerUsernameMax: "Maximum 20 characters",
    registerFullNamePlaceholder: "Full name, for example Taras Shevchenko",
    registerEmailHint: "Use a Gmail address, for example user@gmail.com",
    registerPasswordPlaceholder: "Password",
    registerPasswordHint: "Min 8 chars, uppercase, lowercase, and number",
    registerConfirmPasswordPlaceholder: "Confirm password",
    registerSubmit: "Register",
    registerHaveAccount: "Already have an account? Sign in",
    registerUsernameRequired: "Enter username",
    registerUsernameMin: "Minimum 3 characters",
    registerUsernameLatinOnly: "Username must contain only English letters",
    registerFullNameRequired: "Enter full name",
    registerFullNameMin: "Minimum 2 characters",
    registerFullNameMax: "Maximum 50 characters",
    registerFullNameLatinOnly: "Full name must contain only English letters",
    registerEmailRequired: "Enter email",
    registerEmailInvalid: "Invalid email format",
    registerEmailGmailOnly: "Use a Gmail address",
    registerPasswordRequired: "Enter password",
    registerPasswordMin: "Minimum 8 characters",
    registerPasswordUpper: "At least one uppercase letter is required",
    registerPasswordLower: "At least one lowercase letter is required",
    registerPasswordDigit: "At least one digit is required",
    registerPasswordNoSpaces: "Password must not contain spaces",
    registerConfirmRequired: "Confirm password",
    registerConfirmMismatch: "Passwords do not match",
    registerInvalidData: "Invalid data. Check your input.",
    registerUserExists: "User with username \"{username}\" already exists",
    registerEmailUsed: "Email \"{email}\" is already in use",
    registerGenericError: "Registration failed. Please try again.",
    profileBack: "Back to chats",
    profileTitleUsername: "Username",
    profileTitleFullName: "Full name",
    profileTitleEmail: "Email",
    profileTitleTheme: "Theme",
    profileTitleLanguage: "Language",
    profileThemeLight: "Light",
    profileThemeDark: "Dark",
    profileLangUk: "Ukrainian",
    profileLangEn: "English",
    profileLastSeen: "Last seen",
    profileSave: "Save changes",
    profileSaving: "Saving...",
    profileAvatarTitle: "Change avatar",
    profileAvatarSave: "Save photo",
    profileAvatarSaved: "Avatar saved ✓",
    profileAvatarUploadFailed: "Failed to upload avatar",
    profileUsernamePlaceholder: "Username, for example taras",
    profileFullNamePlaceholder: "Full name, for example Taras Shevchenko",
    profileLoadFailed: "Failed to load profile",
    profileSaved: "Saved ✓",
    profileUpdateError: "Update failed",
    profileUsernameExists: "User with username \"{username}\" already exists"
  }
};

export const preferences = reactive({
  theme: "light",
  language: "uk"
});

const STORAGE_KEY = "simple_messenger_preferences";

function normalizeTheme(theme) {
  return theme === "dark" ? "dark" : "light";
}

function normalizeLanguage(language) {
  return language === "en" ? "en" : "uk";
}

export function applyTheme(theme) {
  preferences.theme = normalizeTheme(theme);
  document.documentElement.setAttribute("data-theme", preferences.theme);
  persistPreferences();
}

export function applyLanguage(language) {
  preferences.language = normalizeLanguage(language);
  document.documentElement.setAttribute("lang", preferences.language === "uk" ? "uk" : "en");
  persistPreferences();
}

export function applyUserPreferences(user) {
  if (user?.theme) {
    applyTheme(user.theme);
  }
  if (user?.language) {
    applyLanguage(user.language);
  }
}

export function t(key, params = {}) {
  const template = translations[preferences.language]?.[key] || translations.uk[key] || key;
  return Object.keys(params).reduce(
    (acc, name) => acc.replaceAll(`{${name}}`, String(params[name])),
    template
  );
}

export function getInitialPreferences() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) {
      return { theme: "light", language: "uk" };
    }
    const parsed = JSON.parse(raw);
    return {
      theme: normalizeTheme(parsed?.theme),
      language: normalizeLanguage(parsed?.language)
    };
  } catch {
    return { theme: "light", language: "uk" };
  }
}

function persistPreferences() {
  try {
    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({ theme: preferences.theme, language: preferences.language })
    );
  } catch {
    // Ignore storage failures and keep runtime preferences only.
  }
}
