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
        <div class="preview" :class="{ unread: hasUnread }">{{ chat.lastMessage || '&nbsp;' }}</div>
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
  padding: 12px 20px;
  cursor: pointer;
  transition: background 0.12s ease;
}

.chat-item:hover {
  background: #f5f5f5;
}

.avatar-wrap {
  position: relative;
  flex-shrink: 0;
}

.avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: #111111;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
  overflow: hidden;
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
  transition: background 0.3s ease;
}

.dot-online  { background: #4caf50; }
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
  font-weight: 600;
  font-size: 14px;
  color: #111111;
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
  color: #bbbbbb;
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
  color: #999999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}

.preview.unread {
  font-weight: 700;
  color: #111111;
}

.unread-badge {
  flex-shrink: 0;
  margin-left: 8px;
  background: #111111;
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