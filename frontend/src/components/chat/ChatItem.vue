<template>
  <div class="chat-item">
    <div class="avatar-wrap">
      <div class="avatar">
        <img v-if="chat.avatarUrl" :src="chat.avatarUrl" class="avatar-img" alt="" />
        <span v-else>{{ (chat.username || "?").charAt(0).toUpperCase() }}</span>
      </div>
      <div class="status-dot" :class="isOnline ? 'dot-online' : 'dot-offline'"></div>
    </div>

    <div class="info">
      <div class="info-top">
        <div class="name">{{ chat.username }}</div>
        <div class="msg-time" v-if="chat.lastMessageTime">{{ formatTime(chat.lastMessageTime) }}</div>
        <div class="meta-status" v-else :class="isOnline ? 'text-online' : 'text-offline'">
          {{ isOnline ? 'В мережі' : 'Не в мережі' }}
        </div>
      </div>
      <div class="preview-row">
        <div class="preview" :class="{ unread: hasUnread }">
          <template v-if="isTyping">
            <span class="typing-dots"><span></span><span></span><span></span></span> друкує...
          </template>
          <template v-else>{{ chat.lastMessage || '\u00A0' }}</template>
        </div>
        <div class="unread-badge" v-if="hasUnread">{{ chat.unreadCount }}</div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: ["chat"],

  computed: {
    isOnline() {
      return this.chat.status === "ONLINE";
    },
    hasUnread() {
      return this.chat.unreadCount > 0;
    }
    ,
    isTyping() {
      return this.chat.typing === true;
    }
  },

  methods: {
    formatTime(ts) {
      if (!ts) return "";
      const d = new Date(ts);
      const now = new Date();
      const isToday = d.toDateString() === now.toDateString();
      if (isToday) {
        return d.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
      }
      return d.toLocaleDateString([], { day: "2-digit", month: "2-digit" });
    }
  }
};
</script>

<style scoped>
.chat-item {
  display: flex;
  align-items: center;
  padding: 13px 14px;
  margin-bottom: 6px;
  border-radius: 18px;
  border: 1px solid transparent;
  background: linear-gradient(180deg, #ffffff 0%, #fdfdfd 100%);
  box-shadow: 0 1px 0 rgba(17, 24, 39, 0.02);
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease, background 0.18s ease, border-color 0.18s ease;
}

.chat-item:hover {
  background: linear-gradient(180deg, #ffffff 0%, #f7fafc 100%);
  border-color: #e6edf5;
  box-shadow: 0 10px 22px rgba(15, 23, 42, 0.06);
  transform: translateY(-1px);
}

.avatar-wrap {
  position: relative;
  flex-shrink: 0;
}

.avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0f172a 0%, #334155 100%);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
  overflow: hidden;
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.18);
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  border-radius: 50%;
}

.status-dot {
  position: absolute;
  bottom: 1px;
  right: 1px;
  width: 11px;
  height: 11px;
  border-radius: 50%;
  border: 2px solid #ffffff;
  box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.7);
  transition: background 0.3s ease, transform 0.3s ease;
}

.dot-online  { background: #22c55e; animation: status-pulse 2.4s infinite; }
.dot-offline { background: #cccccc; }

.info {
  margin-left: 12px;
  flex: 1;
  min-width: 0;
}

.info-top {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 3px;
}

.name {
  font-weight: 700;
  font-size: 14px;
  color: #0f172a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.meta-status {
  font-size: 11px;
  flex-shrink: 0;
  margin-left: 6px;
  font-weight: 500;
  transition: color 0.3s ease;
}

.text-online  { color: #4caf50; }
.text-offline { color: #bbbbbb; }

.msg-time {
  font-size: 11px;
  color: #94a3b8;
  flex-shrink: 0;
  margin-left: 6px;
}

.preview-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.preview {
  font-size: 12px;
  color: #64748b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}

.preview.unread {
  font-weight: 700;
  color: #0f172a;
}

.unread-badge {
  flex-shrink: 0;
  margin-left: 8px;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  color: #ffffff;
  font-size: 11px;
  font-weight: 600;
  min-width: 18px;
  height: 18px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 5px;
  box-shadow: 0 6px 14px rgba(15, 23, 42, 0.18);
}

.typing-dots {
  display: inline-flex;
  gap: 4px;
  margin-right: 8px;
}
.typing-dots span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #4caf50;
  display: inline-block;
  opacity: 0.3;
  transform: scale(0.8);
  animation: chat-dot 1.2s infinite;
}
.typing-dots span:nth-child(2) { animation-delay: 0.2s }
.typing-dots span:nth-child(3) { animation-delay: 0.4s }

@keyframes chat-dot {
  0%,80%,100% { opacity: 0.3; transform: scale(0.8); }
  40% { opacity: 1; transform: scale(1); }
}

@keyframes status-pulse {
  0%, 100% { transform: scale(1); box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.7); }
  50% { transform: scale(1.08); box-shadow: 0 0 0 5px rgba(34, 197, 94, 0.12); }
}

@media (max-width: 768px) {
  .chat-item {
    padding: 10px 16px;
  }

  .avatar {
    width: 40px;
    height: 40px;
    font-size: 14px;
  }

  .name {
    font-size: 13px;
  }

  .preview {
    font-size: 11px;
  }

  .msg-time,
  .meta-status {
    font-size: 10px;
  }

  .unread-badge {
    min-width: 16px;
    height: 16px;
    font-size: 10px;
  }
}

@media (max-width: 480px) {
  .chat-item {
    padding: 8px 12px;
  }

  .avatar {
    width: 36px;
    height: 36px;
    font-size: 12px;
  }

  .status-dot {
    width: 10px;
    height: 10px;
    border-width: 1.5px;
  }

  .info {
    margin-left: 10px;
  }

  .name {
    font-size: 12px;
  }

  .preview {
    font-size: 10px;
  }

  .msg-time,
  .meta-status {
    font-size: 9px;
  }

  .unread-badge {
    min-width: 15px;
    height: 15px;
    font-size: 9px;
    margin-left: 6px;
  }
}
</style>