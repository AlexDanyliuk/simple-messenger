<template>
  <div class="composer">
    <div class="file-input-wrapper">
      <input
        ref="fileInput"
        type="file"
        class="file-input-hidden"
        @change="onFileSelected"
        accept="*/*"
      />
      <button
        class="attach-btn"
        @click="triggerFileInput"
        :title="t('attachFile') || 'Attach file'"
      >
        <svg viewBox="0 0 24 24" width="18" height="18" fill="none">
          <path d="M21.44 11.05l-9.19 9.19a6 6 0 01-8.49-8.49l9.19-9.19a4 4 0 015.66 5.66l-9.2 9.19a2 2 0 01-2.83-2.83l8.49-8.48"
            stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
    </div>

    <div class="input-wrapper">
      <input
        v-model="text"
        :placeholder="t('chatMessagePlaceholder')"
        @keyup.enter="send"
        @input="onInput"
        @blur="stopTyping"
      />
      <div v-if="selectedFile" class="file-preview">
        <span class="file-name">{{ selectedFile.name }}</span>
        <button class="remove-file-btn" @click="clearFile">×</button>
      </div>
    </div>

    <button
      @click="send"
      :disabled="isUploading"
      class="send-btn"
    >
      {{ isUploading ? "..." : t("chatSend") }}
    </button>
  </div>
</template>

<script>
import { t } from "../../services/userPreferences";
import api from "../../services/api";

export default {
  name: 'ChatComposer',
  emits: ['send', 'typing'],
  data() {
    return {
      text: '',
      typingTimeout: null,
      selectedFile: null,
      isUploading: false
    };
  },
  methods: {
    t,
    triggerFileInput() {
      this.$refs.fileInput?.click();
    },
    async onFileSelected(event) {
      const file = event.target.files?.[0];
      if (file) {
        this.selectedFile = file;
      }
    },
    clearFile() {
      this.selectedFile = null;
      if (this.$refs.fileInput) {
        this.$refs.fileInput.value = '';
      }
    },
    async send() {
      const msg = (this.text || '').trim();
      const hasFile = this.selectedFile != null;

      if (!msg && !hasFile) return;

      try {
        this.isUploading = true;

        let fileData = null;
        if (this.selectedFile) {
          // Upload file
          const formData = new FormData();
          formData.append('file', this.selectedFile);

          const response = await api.post('/upload', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
          });

          fileData = response.data;
        }

        // Send message with optional file
        const payload = {
          content: msg
        };

        if (fileData) {
          payload.fileUrl = fileData.fileUrl;
          payload.fileName = fileData.fileName;
          payload.fileType = fileData.fileType;
          payload.fileSize = fileData.fileSize;
        }

        this.$emit('send', payload);
        this.text = '';
        this.clearFile();
        this.stopTyping();
      } catch (err) {
        console.error('Error sending message:', err);
        alert('Failed to send message');
      } finally {
        this.isUploading = false;
      }
    },
    onInput() {
      this.$emit('typing', true);
      clearTimeout(this.typingTimeout);
      this.typingTimeout = setTimeout(() => this.stopTyping(), 2000);
    },
    stopTyping() {
      clearTimeout(this.typingTimeout);
      this.$emit('typing', false);
    }
  }
};
</script>

<style scoped>
.composer {
  display: flex;
  align-items: flex-end;
  padding: 12px 24px;
  background: var(--panel-bg);
  border-top: 1px solid var(--border-color);
  gap: 12px;
  flex-shrink: 0;
}

.file-input-wrapper {
  position: relative;
  flex-shrink: 0;
}

.file-input-hidden {
  display: none;
}

.attach-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  padding: 0;
  background: none;
  border: none;
  color: var(--text-tertiary);
  cursor: pointer;
  transition: color 0.15s;
  flex-shrink: 0;
}

.attach-btn:hover {
  color: var(--text-secondary);
}

.input-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input-wrapper input {
  padding: 11px 18px;
  border-radius: 22px;
  border: 1px solid var(--border-color);
  background: var(--panel-bg-soft);
  font-size: 14px;
  font-family: inherit;
  outline: none;
  transition: all 0.15s;
  color: var(--app-text);
}

.input-wrapper input::placeholder {
  color: var(--text-tertiary);
}

.input-wrapper input:focus {
  border-color: var(--accent);
  background: var(--panel-bg);
}

.file-preview {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background: var(--panel-bg-soft);
  border-radius: 8px;
  border: 1px solid var(--border-color);
  font-size: 12px;
  color: var(--text-secondary);
}

.file-name {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-right: 8px;
}

.remove-file-btn {
  padding: 0;
  width: 20px;
  height: 20px;
  background: none;
  border: none;
  color: var(--text-tertiary);
  font-size: 18px;
  cursor: pointer;
  flex-shrink: 0;
  transition: color 0.15s;
}

.remove-file-btn:hover {
  color: var(--app-text);
}

.send-btn {
  padding: 0;
  background: none;
  border: none;
  color: var(--app-text);
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 1px;
  text-transform: uppercase;
  cursor: pointer;
  opacity: 0.9;
  transition: opacity 0.15s;
  white-space: nowrap;
  flex-shrink: 0;
}

.send-btn:hover:not(:disabled) {
  opacity: 0.5;
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
