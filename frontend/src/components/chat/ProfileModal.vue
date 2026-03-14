<template>
  <transition name="fade">
    <div class="modal-overlay" v-if="show" @click.self="$emit('close')">
      <div class="modal-card">
        <button class="modal-close" @click="$emit('close')">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"/>
            <line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </button>
        <div class="modal-avatar">
          <img v-if="recipient && recipient.avatarUrl" :src="recipient.avatarUrl" class="modal-avatar-img" alt="" />
          <span v-else>{{ recipientInitial }}</span>
        </div>
        <div class="modal-name">{{ recipient ? recipient.username : '' }}</div>
        <div class="modal-rows">
          <div class="modal-row">
            <span class="modal-label">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
              {{ t("profileTitleUsername") }}
            </span>
            <span class="modal-value">{{ recipient ? recipient.username : '—' }}</span>
          </div>
          <div class="modal-row">
            <span class="modal-label">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="2" y="3" width="20" height="14" rx="2"/>
                <path d="M8 21h8M12 17v4"/>
              </svg>
              {{ t("profileTitleFullName") }}
            </span>
            <span class="modal-value">{{ recipient ? (recipient.fullName || '—') : '—' }}</span>
          </div>
          <div class="modal-row">
            <span class="modal-label">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                <polyline points="22,6 12,13 2,6"/>
              </svg>
              {{ t("profileTitleEmail") }}
            </span>
            <span class="modal-value">{{ (recipient && recipient.email) ? recipient.email : '—' }}</span>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script>
import { t } from "../../services/userPreferences";

export default {
  name: 'ProfileModal',
  props: {
    show: { type: Boolean, default: false },
    recipient: { type: Object, default: null },
    recipientInitial: { type: String, default: '?' }
  },
  emits: ['close'],
  methods: { t }
};
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.30);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-card {
  background: var(--panel-bg);
  border-radius: 18px;
  padding: 32px 28px 28px;
  width: 320px;
  box-shadow: var(--shadow-xl);
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  animation: modalIn 0.22s cubic-bezier(0.34,1.56,0.64,1);
}

@keyframes modalIn {
  from { opacity: 0; transform: scale(0.93) translateY(10px); }
  to   { opacity: 1; transform: scale(1) translateY(0); }
}

.modal-close {
  position: absolute;
  top: 14px;
  right: 14px;
  background: none;
  border: none;
  cursor: pointer;
  color: var(--text-tertiary);
  padding: 4px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  transition: color 0.12s, background 0.12s;
}
.modal-close:hover { color: var(--app-text); background: var(--panel-bg-soft); }

.modal-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: var(--accent);
  color: #fff;
  font-size: 26px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
  overflow: hidden;
}

.modal-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  border-radius: 50%;
}

.modal-name {
  font-size: 18px;
  font-weight: 700;
  color: var(--app-text);
  margin-bottom: 20px;
}

.modal-rows {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 0;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--border-color);
}

.modal-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: var(--panel-bg-soft);
  border-bottom: 1px solid var(--border-color);
}
.modal-row:last-child { border-bottom: none; }

.modal-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.4px;
}

.modal-value {
  font-size: 14px;
  color: var(--app-text);
  font-weight: 500;
}

.fade-enter-active, .fade-leave-active { transition: opacity 0.18s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
