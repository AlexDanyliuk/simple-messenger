<template>
  <div ref="list" class="messages">
    <div
      v-for="m in messages"
      :key="m.id || m.timestamp + '-' + m.content"
      class="msg"
      :class="{ own: Number(m.senderId) === Number(currentUserId) }"
    >
      <!-- edit input (own messages only) -->
      <div v-if="editingId != null && editingId === msgId(m)" class="bubble bubble--editing">
        <textarea
          ref="editInput"
          v-model="editText"
          class="edit-input"
          rows="1"
          @keydown.enter.exact.prevent="submitEdit(m)"
          @keydown.esc="cancelEdit"
        />
        <div class="edit-actions">
          <button class="btn-cancel" @click="cancelEdit">Скасувати</button>
          <button class="btn-save" @click="submitEdit(m)">Зберегти</button>
        </div>
      </div>

      <!-- normal bubble -->
      <div
        v-else
        class="bubble"
        :class="{ 'bubble--own': Number(m.senderId) === Number(currentUserId) }"
      >
        <!-- pencil button (only own, appears on hover) -->
        <button
          v-if="Number(m.senderId) === Number(currentUserId)"
          class="edit-btn"
          title="Редагувати"
          @click="startEdit(m)"
        >
          <svg viewBox="0 0 16 16" width="13" height="13" fill="none">
            <path d="M11.3 1.3a1 1 0 0 1 1.4 0l2 2a1 1 0 0 1 0 1.4L5.5 13.9 1 15l1.1-4.5L11.3 1.3Z"
              stroke="currentColor" stroke-width="1.4" stroke-linejoin="round"/>
          </svg>
        </button>

        <span class="content">{{ m.content }}</span>
        <span class="meta">
          <span v-if="m.editedAt" class="edited-label">ред.</span>
          <span class="time-text">{{ formatTime(m.timestamp) }}</span>
          <span
            v-if="Number(m.senderId) === Number(currentUserId)"
            class="ticks"
            :class="'ticks--' + tickStatus(m)"
          >
            <svg v-if="tickStatus(m) === 'sent'" viewBox="0 0 16 11" width="16" height="11">
              <path d="M1 5.5L5.5 10L15 1" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
            </svg>
            <svg v-else viewBox="0 0 20 11" width="20" height="11">
              <path d="M1 5.5L5.5 10L15 1" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
              <path d="M6 5.5L10.5 10L20 1" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
            </svg>
          </span>
        </span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MessageList',
  props: {
    messages: { type: Array, default: () => [] },
    currentUserId: { type: [Number, String], default: null }
  },
  emits: ['edit'],
  data() {
    return {
      editingId: null,
      editText: ''
    };
  },
  methods: {
    formatTime(ts) {
      if (!ts) return '';
      try {
        return new Date(ts).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
      } catch { return ''; }
    },
    scrollDown() {
      const el = this.$refs.list;
      if (!el) return;
      el.scrollTop = el.scrollHeight;
    },
    tickStatus(m) {
      if (m.readAt) return 'read';
      if (m.deliveredAt) return 'delivered';
      return 'sent';
    },
    msgId(m) {
      return m.id ?? null;
    },
    startEdit(m) {
      this.editingId = this.msgId(m);
      this.editText = m.content;
      this.$nextTick(() => {
        const el = this.$refs.editInput;
        const input = Array.isArray(el) ? el[0] : el;
        if (input) { input.focus(); input.selectionStart = input.value.length; }
      });
    },
    cancelEdit() {
      this.editingId = null;
      this.editText = '';
    },
    submitEdit(m) {
      const trimmed = this.editText.trim();
      if (!trimmed || trimmed === m.content) { this.cancelEdit(); return; }
      this.$emit('edit', { messageId: this.msgId(m), content: trimmed });
      this.cancelEdit();
    }
  }
};
</script>

<style scoped>
.messages {
  flex: 1;
  padding: 24px 40px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.messages::-webkit-scrollbar { width: 3px; }
.messages::-webkit-scrollbar-thumb { background: #e0e0e0; border-radius: 4px; }
@media (max-width: 768px) { .messages { padding: 16px 24px; } }
@media (max-width: 480px) { .messages { padding: 12px 14px; } }

/* ── row ── */
.msg {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  margin-bottom: 2px;
  position: relative;
}
.msg.own { align-items: flex-end; }

/* ── bubble ── */
.bubble {
  max-width: 58%;
  min-width: 72px;
  padding: 8px 12px 6px 14px;
  border-radius: 18px;
  font-size: 14px;
  line-height: 1.55;
  word-break: break-word;
  display: inline-flex;
  align-items: flex-end;
  gap: 6px;
  position: relative;
}
.msg:not(.own) .bubble {
  background: #fff;
  color: #111;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.07);
}
.bubble--own,
.msg.own .bubble {
  background: #d4eaff;
  color: #111;
  border-bottom-right-radius: 4px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.08);
}

/* ── edit pencil button ── */
.edit-btn {
  display: none;
  position: absolute;
  top: 50%;
  left: -30px;
  transform: translateY(-50%);
  width: 24px;
  height: 24px;
  border: none;
  background: #e8f4ff;
  border-radius: 50%;
  cursor: pointer;
  align-items: center;
  justify-content: center;
  color: #5b9bd5;
  padding: 0;
  transition: background 0.15s;
}
.edit-btn:hover { background: #cde3f8; }
.msg.own:hover .edit-btn { display: flex; }

/* ── content + meta ── */
.content { flex: 1; word-break: break-word; }

.meta {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  flex-shrink: 0;
  align-self: flex-end;
  margin-bottom: 1px;
}

.edited-label {
  font-size: 10px;
  color: #aaa;
  font-style: italic;
}
.time-text {
  font-size: 10px;
  color: #999;
  white-space: nowrap;
  line-height: 1;
}

/* ── ticks ── */
.ticks { display: inline-flex; align-items: center; line-height: 1; }
.ticks svg { display: block; transition: color 0.25s ease; }
.ticks--sent      { color: #b0b8c1; }
.ticks--delivered { color: #b0b8c1; }
.ticks--read      { color: #4fc3f7; }

/* ── editing bubble ── */
.bubble--editing {
  flex-direction: column;
  align-items: stretch;
  gap: 8px;
  min-width: 220px;
  padding: 10px 14px;
  background: #d4eaff;
  border-bottom-right-radius: 4px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.08);
}
.edit-input {
  width: 100%;
  border: none;
  outline: none;
  background: rgba(255,255,255,0.7);
  border-radius: 8px;
  padding: 6px 10px;
  font-size: 14px;
  line-height: 1.5;
  resize: none;
  font-family: inherit;
  color: #111;
  field-sizing: content;
  min-height: 32px;
}
.edit-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}
.btn-cancel, .btn-save {
  border: none;
  cursor: pointer;
  border-radius: 8px;
  padding: 4px 12px;
  font-size: 12px;
  font-weight: 600;
  transition: background 0.15s;
}
.btn-cancel { background: #e0e0e0; color: #555; }
.btn-cancel:hover { background: #d0d0d0; }
.btn-save { background: #4a90d9; color: #fff; }
.btn-save:hover { background: #3a7dc4; }
</style>

