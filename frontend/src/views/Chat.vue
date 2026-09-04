<template>
  <div class="chat-page">
    <div class="chat-container" :class="{ 'has-active': activeId }">
      <!-- 会话列表 -->
      <aside class="conv-list">
        <div class="conv-header">消息</div>
        <div class="conv-items" v-loading="listLoading">
          <div v-if="conversations.length === 0" class="empty">暂无会话</div>
          <div
            v-for="c in conversations"
            :key="c.otherUserId"
            class="conv-item"
            :class="{ active: c.otherUserId === activeId }"
            @click="selectConversation(c.otherUserId)"
          >
            <el-avatar :size="42" :src="c.otherUserAvatar">{{ (c.otherUserName || 'U')[0] }}</el-avatar>
            <div class="conv-info">
              <div class="conv-name">{{ c.otherUserName }}</div>
              <div class="conv-last">{{ c.lastMessage }}</div>
            </div>
            <el-badge v-if="c.unreadCount > 0" :value="c.unreadCount" class="conv-badge" />
          </div>
        </div>
      </aside>

      <!-- 对话窗口 -->
      <section class="conv-window">
        <template v-if="activeId">
          <div class="win-header">
            <el-button class="back-btn" link @click="activeId = null">
              <el-icon><ArrowLeft /></el-icon>
            </el-button>
            <span class="win-name">{{ activeName }}</span>
          </div>
          <div class="win-messages" ref="msgBox">
            <div v-if="messages.length === 0" class="msg-empty">打个招呼开始聊天吧～</div>
            <div v-for="m in messages" :key="m.id" class="msg-row" :class="{ mine: m.mine }">
              <div class="bubble">{{ m.content }}</div>
            </div>
          </div>
          <div class="win-input">
            <el-input v-model="input" placeholder="输入消息…" @keyup.enter="send" />
            <el-button type="primary" @click="send">发送</el-button>
          </div>
        </template>
        <template v-else>
          <div class="win-empty">
            <el-icon :size="48"><ChatDotRound /></el-icon>
            <p>选择一个会话开始聊天</p>
          </div>
        </template>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { getConversations, getHistory, sendMessage } from '../api/message'

const route = useRoute()
const conversations = ref([])
const activeId = ref(null)
const messages = ref([])
const input = ref('')
const listLoading = ref(false)
const msgBox = ref(null)
let pollTimer = null

const activeName = computed(() => {
  const c = conversations.value.find((c) => c.otherUserId === activeId.value)
  return c ? c.otherUserName : ''
})

async function loadConversations() {
  try {
    const res = await getConversations()
    conversations.value = res.data || []
  } catch (e) {
    conversations.value = []
  }
}

async function loadHistory(id, scroll = false) {
  try {
    const res = await getHistory(id)
    messages.value = res.data || []
    if (scroll) scrollToBottom()
  } catch (e) {
    messages.value = []
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (msgBox.value) msgBox.value.scrollTop = msgBox.value.scrollHeight
  })
}

async function selectConversation(id) {
  activeId.value = id
  await loadHistory(id, true)
  loadConversations()
}

async function send() {
  const content = input.value.trim()
  if (!content || !activeId.value) return
  try {
    const res = await sendMessage({ receiverId: activeId.value, content })
    messages.value.push(res.data)
    input.value = ''
    scrollToBottom()
    loadConversations()
  } catch (e) {
    // 错误由拦截器统一提示
  }
}

onMounted(async () => {
  await loadConversations()
  const withId = route.query.with
  if (withId) {
    await selectConversation(Number(withId))
  }
  pollTimer = setInterval(async () => {
    await loadConversations()
    if (activeId.value) await loadHistory(activeId.value)
  }, 10000)
})

onBeforeUnmount(() => {
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<style scoped>
.chat-page {
  max-width: 1100px;
  margin: 0 auto;
  height: calc(100vh - 60px);
  padding: 16px;
}
.chat-container {
  display: flex;
  height: 100%;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
.conv-list {
  width: 280px;
  border-right: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}
.conv-header {
  padding: 16px;
  font-size: 16px;
  font-weight: 700;
  color: #303133;
  border-bottom: 1px solid #f0f0f0;
}
.conv-items {
  flex: 1;
  overflow-y: auto;
}
.empty {
  text-align: center;
  color: #909399;
  padding: 40px 0;
  font-size: 14px;
}
.conv-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  cursor: pointer;
}
.conv-item:hover {
  background: #f5f7fa;
}
.conv-item.active {
  background: #eef1ff;
}
.conv-info {
  flex: 1;
  min-width: 0;
}
.conv-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
.conv-last {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-top: 2px;
}
.conv-window {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.win-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
}
.win-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
.back-btn {
  display: none;
}
.win-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f8f9fb;
}
.msg-empty {
  text-align: center;
  color: #c0c4cc;
  font-size: 13px;
  padding-top: 40px;
}
.msg-row {
  display: flex;
  margin-bottom: 12px;
}
.msg-row.mine {
  justify-content: flex-end;
}
.bubble {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 12px;
  background: #fff;
  color: #303133;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}
.msg-row.mine .bubble {
  background: #4f6ef7;
  color: #fff;
}
.win-input {
  display: flex;
  gap: 10px;
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
}
.win-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
}
.win-empty p {
  margin-top: 12px;
  font-size: 14px;
}
@media (max-width: 640px) {
  .chat-page {
    height: calc(100vh - 60px - 56px);
    padding: 0;
  }
  .chat-container {
    border-radius: 0;
  }
  .conv-list {
    width: 100%;
  }
  .conv-window {
    display: none;
  }
  .chat-container.has-active .conv-list {
    display: none;
  }
  .chat-container.has-active .conv-window {
    display: flex;
  }
  .back-btn {
    display: inline-flex;
  }
}
</style>
