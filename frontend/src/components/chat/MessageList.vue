<template>
  <div ref="list" class="messages" @scroll="onScroll">
    <div
      v-for="m in messages"
      :key="m.id || m.timestamp + '-' + m.content"
      :data-message-id="m.id"
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
          <button class="btn-cancel" @click="cancelEdit">{{ t("commonCancel") }}</button>
          <button class="btn-save" @click="submitEdit(m)">{{ t("commonSave") }}</button>
        </div>
      </div>

      <!-- normal bubble -->
      <div
        v-else
        class="bubble"
        :class="{ 'bubble--own': Number(m.senderId) === Number(currentUserId), 'bubble--pinned': m.pinned }"
        @contextmenu.prevent="showContextMenu($event, m)"
      >
        <!-- pencil button (only own, appears on hover) -->
        <button
          v-if="Number(m.senderId) === Number(currentUserId)"
          class="edit-btn"
          :title="t('chatEdit')"
          @click="startEdit(m)"
        >
          <svg viewBox="0 0 16 16" width="13" height="13" fill="none">
            <path d="M11.3 1.3a1 1 0 0 1 1.4 0l2 2a1 1 0 0 1 0 1.4L5.5 13.9 1 15l1.1-4.5L11.3 1.3Z"
              stroke="currentColor" stroke-width="1.4" stroke-linejoin="round"/>
          </svg>
        </button>

        <div class="message-content">
          <!-- File attachment display -->
          <div v-if="m.fileUrl" class="file-attachment">
            <a v-if="isImage(m.fileType)" :href="m.fileUrl" target="_blank" class="file-link">
              <img :src="m.fileUrl" :alt="m.fileName" class="file-image" />
            </a>
            <video v-else-if="isVideo(m.fileType)" controls class="file-video">
              <source :src="m.fileUrl" :type="m.fileType" />
            </video>
            <a v-else :href="m.fileUrl" target="_blank" class="file-link file-document">
              <span class="file-icon">📎</span>
              <span class="file-info">
                <span class="file-name">{{ m.fileName }}</span>
                <span class="file-size">{{ formatFileSize(m.fileSize) }}</span>
              </span>
            </a>
          </div>

          <!-- Text content -->
          <span v-if="m.content" class="content">{{ m.content }}</span>
        </div>

        <span class="meta">
          <span v-if="m.editedAt" class="edited-label">{{ t("chatEdited") }}</span>
          <span class="time-text">{{ formatTime(m.timestamp) }}</span>
          <span
            v-if="Number(m.senderId) === Number(currentUserId)"
            class="ticks"
            :class="'ticks--' + tickStatus(m)"
          >
            <svg v-if="!m.readAt" viewBox="0 0 16 11" width="11" height="8">
              <path d="M1 5.5L5.5 10L15 1" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
            </svg>
            <svg v-else viewBox="0 0 20 11" width="14" height="8">
              <path d="M1 5.5L5.5 10L15 1" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
              <path d="M6 5.5L10.5 10L20 1" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
            </svg>
          </span>
        </span>

        <!-- Reactions inside bubble -->
        <div v-if="msgId(m) != null && reactionSummary(m).length" class="reactions-inline">
          <button
            v-for="reaction in reactionSummary(m)"
            :key="`${msgId(m)}-${reaction.emoji}-inline`"
            class="reaction-badge"
            :class="{ active: reaction.active }"
            @click="toggleReaction(m, reaction.emoji)"
          >
            <span class="emoji">{{ reaction.emoji }}</span>
            <span class="count" v-if="reaction.count > 1">{{ reaction.count }}</span>
          </button>
        </div>
      </div>

      <!-- Context menu -->
      <div
        v-if="contextMenuMessageId === msgId(m)"
        class="context-menu"
        :style="{ top: contextMenuPos.y + 'px', left: contextMenuPos.x + 'px' }"
      >
        <div class="context-emoji-row">
          <button
            v-for="emoji in reactionEmojis"
            :key="`ctx-${msgId(m)}-${emoji}`"
            class="context-emoji-btn"
            @click="pickEmojiFor(m, emoji); closeContextMenu()"
          >
            {{ emoji }}
          </button>
        </div>
        <div class="context-divider"></div>
        <button
          v-if="Number(m.senderId) === Number(currentUserId)"
          class="context-menu-item"
          @click="startEdit(m); closeContextMenu()"
        >
          {{ t("chatEdit") }}
        </button>
        <button
          class="context-menu-item"
          @click="togglePin(m); closeContextMenu()"
        >
          {{ m.pinned ? "📌 Відкріпити" : "📌 Прикріпити" }}
        </button>
      </div>
    </div>

  </div>
</template>

<script>
import { t } from "../../services/userPreferences";

export default {
  name: 'MessageList',
  props: {
    messages: { type: Array, default: () => [] },
    currentUserId: { type: [Number, String], default: null }
  },
  emits: ['edit', 'reaction', 'pin', 'scroll'],
  data() {
    return {
      editingId: null,
      editText: '',
      reactionEmojis: ["👍", "❤️", "😂", "😮", "😢", "🙏"],
      hoveredMessageId: null,
      contextMenuMessageId: null,
      contextMenuPos: { x: 0, y: 0 }
    };
  },
  methods: {
    t,
    formatTime(ts) {
      if (!ts) return '';
      try {
        return new Date(ts).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
      } catch { return ''; }
    },
    formatFileSize(bytes) {
      if (!bytes) return '';
      const units = ['B', 'KB', 'MB', 'GB'];
      let size = bytes;
      let unitIndex = 0;
      while (size >= 1024 && unitIndex < units.length - 1) {
        size /= 1024;
        unitIndex++;
      }
      return `${size.toFixed(unitIndex === 0 ? 0 : 1)} ${units[unitIndex]}`;
    },
    isImage(fileType) {
      return fileType && fileType.startsWith('image/');
    },
    isVideo(fileType) {
      return fileType && fileType.startsWith('video/');
    },
    scrollDown() {
      const el = this.$refs.list;
      if (!el) return;
      el.scrollTop = el.scrollHeight;
    },
    tickStatus(m) {
      if (m.readAt) return 'read';
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
    },
    toggleReaction(m, emoji) {
      const id = this.msgId(m);
      if (id == null) return;
      this.$emit('reaction', { messageId: id, emoji });
    },
    reactionSummary(m) {
      const reactions = m?.reactions || {};
      return Object.entries(reactions)
        .filter(([, users]) => Array.isArray(users) && users.length > 0)
        .map(([emoji, users]) => ({
          emoji,
          count: users.length,
          active: users.map(Number).includes(Number(this.currentUserId))
        }));
    },
    showPickerFor(m) {
      const id = this.msgId(m);
      if (id == null) return;
      this.hoveredMessageId = id;
    },
    pickEmojiFor(m, emoji) {
      this.toggleReaction(m, emoji);
      this.closePicker();
    },
    closePicker() {
      this.hoveredMessageId = null;
    },
    showContextMenu(event, m) {
      const menuWidth = 220;
      const menuHeight = 200;
      const padding = 10;
      
      let x = event.clientX;
      let y = event.clientY;
      
      // Перевіряємо чи меню вилізає за право
      if (x + menuWidth > window.innerWidth - padding) {
        x = window.innerWidth - menuWidth - padding;
      }
      
      // Перевіряємо чи меню вилізає вниз
      if (y + menuHeight > window.innerHeight - padding) {
        y = Math.max(padding, y - menuHeight - 10);
      }
      
      this.contextMenuPos = { x, y };
      this.contextMenuMessageId = this.msgId(m);
      document.addEventListener('click', this.closeContextMenu);
    },
    closeContextMenu() {
      this.contextMenuMessageId = null;
      document.removeEventListener('click', this.closeContextMenu);
    },
    togglePin(m) {
      const id = this.msgId(m);
      if (id == null) return;
      this.$emit('pin', { messageId: id, pinned: !m.pinned });
    },
    onScroll() {
      this.closePicker();
      this.$emit('scroll');
    }
  },
  beforeUnmount() {
    document.removeEventListener('click', this.closeContextMenu);
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
.messages::-webkit-scrollbar-thumb { background: var(--border-color); border-radius: 4px; }
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

.reactions-inline {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
  margin-top: 6px;
}

.reaction-badge {
  border: none;
  background: rgba(0, 0, 0, 0.08);
  border-radius: 999px;
  padding: 2px 6px;
  font-size: 11px;
  display: inline-flex;
  gap: 2px;
  align-items: center;
  cursor: pointer;
  transition: all 0.15s;
}

.reaction-badge:hover {
  background: rgba(0, 0, 0, 0.12);
}

.reaction-badge.active {
  background: rgba(59, 130, 246, 0.15);
}

.reaction-badge .emoji {
  font-size: 12px;
}

.reaction-badge .count {
  font-size: 10px;
  color: var(--text-secondary);
}

.msg.own .reaction-badge {
  background: rgba(255, 255, 255, 0.3);
}

.msg.own .reaction-badge.active {
  background: rgba(255, 255, 255, 0.5);
}

.reactions-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 4px 2px 10px;
  flex-wrap: wrap;
}

.reactions-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 4px 2px 10px;
  flex-wrap: wrap;
}

.reaction-pill {
  border: 1px solid var(--accent-light);
  background: var(--accent-lighter);
  color: var(--accent);
  border-radius: 999px;
  padding: 2px 8px;
  font-size: 12px;
  display: inline-flex;
  gap: 4px;
  align-items: center;
  cursor: pointer;
}

.reaction-pill.active {
  border-color: var(--accent);
  background: var(--accent-light);
}

.emoji-hover-panel {
  position: absolute;
  top: -38px;
  left: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px;
  background: var(--panel-bg);
  border: 1px solid var(--border-color);
  border-radius: 999px;
  box-shadow: var(--shadow-lg);
}

.emoji-hover-panel.own {
  left: auto;
  right: 0;
}

.emoji-hover-btn {
  border: none;
  background: var(--panel-bg-soft);
  border-radius: 999px;
  width: 30px;
  height: 30px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  cursor: pointer;
}

.emoji-hover-btn:hover {
  background: var(--panel-bg-hover);
}

@media (hover: none) {
  .emoji-hover-panel {
    display: none;
  }
}

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
  flex-direction: column;
  gap: 6px;
  position: relative;
}
.msg:not(.own) .bubble {
  background: var(--msg-other-bg);
  color: var(--msg-other-color);
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.07);
}
.bubble--own,
.msg.own .bubble {
  background: var(--msg-own-bg);
  color: var(--msg-own-color);
  border-bottom-right-radius: 4px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.08);
}

.bubble--pinned::before {
  content: "📌";
  position: absolute;
  top: -18px;
  left: 8px;
  font-size: 16px;
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
.message-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.content {
  word-break: break-word;
}

.file-attachment {
  display: flex;
  flex-direction: column;
  border-radius: 8px;
  overflow: hidden;
}

.file-link {
  text-decoration: none;
  color: inherit;
  display: flex;
  transition: opacity 0.15s;
}

.file-link:hover {
  opacity: 0.8;
}

.file-image {
  max-width: 100%;
  max-height: 300px;
  border-radius: 8px;
  object-fit: cover;
}

.file-video {
  max-width: 100%;
  max-height: 300px;
  border-radius: 8px;
  background: #000;
}

.file-document {
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 8px;
}

.file-icon {
  font-size: 24px;
  flex-shrink: 0;
}

.file-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.file-name {
  font-weight: 500;
  word-break: break-word;
  white-space: normal;
}

.file-size {
  font-size: 12px;
  opacity: 0.7;
}

.meta {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  flex-shrink: 0;
  align-self: flex-end;
  margin-top: 2px;
}

.edited-label {
  font-size: 10px;
  color: var(--text-tertiary);
  font-style: italic;
}
.time-text {
  font-size: 10px;
  color: var(--text-tertiary);
  white-space: nowrap;
  line-height: 1;
}

/* ── ticks ── */
.ticks { display: inline-flex; align-items: center; line-height: 1; margin-left: 4px; }
.ticks svg { display: block; transition: color 0.25s ease; }
.ticks--sent      { color: #ffffff; }
.ticks--read      { color: #ffffff; }

/* ── editing bubble ── */
.bubble--editing {
  flex-direction: column;
  align-items: stretch;
  gap: 8px;
  min-width: 220px;
  padding: 10px 14px;
  background: var(--msg-own-bg);
  border-bottom-right-radius: 4px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.08);
}
.edit-input {
  width: 100%;
  border: none;
  outline: none;
  background: var(--surface-input, rgba(255,255,255,0.7));
  border-radius: 8px;
  padding: 6px 10px;
  font-size: 14px;
  line-height: 1.5;
  resize: none;
  font-family: inherit;
  color: var(--app-text);
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

/* Context Menu */
.context-menu {
  position: fixed;
  background: var(--panel-bg);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  box-shadow: var(--shadow-lg);
  z-index: 1000;
  padding: 8px;
  min-width: 200px;
}

.context-emoji-row {
  display: flex;
  gap: 4px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.context-emoji-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: var(--panel-bg-soft);
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.15s;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}

.context-emoji-btn:hover {
  background: var(--panel-bg-hover);
}

.context-divider {
  height: 1px;
  background: var(--border-color);
  margin: 6px 0;
}

.context-menu-item {
  width: 100%;
  padding: 10px 12px;
  border: none;
  background: transparent;
  border-radius: 6px;
  font-size: 14px;
  color: var(--app-text);
  cursor: pointer;
  text-align: left;
  transition: background 0.15s;
  font-family: inherit;
}

.context-menu-item:hover {
  background: var(--panel-bg-hover);
}
</style>

