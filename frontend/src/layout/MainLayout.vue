<template>
  <div class="layout">
    <header class="header">
      <div class="header-inner">
        <router-link to="/" class="logo">
          <el-icon :size="22"><School /></el-icon>
          <span>校园二手集市</span>
        </router-link>
        <el-menu mode="horizontal" :default-active="activeMenu" router class="nav desktop-only" :ellipsis="false">
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/community">社区</el-menu-item>
          <el-menu-item index="/profile">个人中心</el-menu-item>
        </el-menu>
        <div class="right">
          <el-popover
            v-if="userStore.isLoggedIn"
            placement="bottom-end"
            :width="320"
            trigger="click"
            :teleported="false"
            @show="loadNotifications"
          >
            <template #reference>
              <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notify-badge">
                <el-button circle>
                  <el-icon><Bell /></el-icon>
                </el-button>
              </el-badge>
            </template>
            <div class="notify-panel">
              <div class="notify-head">
                <span class="notify-title">通知</span>
                <el-button
                  v-if="unreadCount > 0"
                  link
                  type="primary"
                  size="small"
                  @click="onMarkAllRead"
                >全部已读</el-button>
              </div>
              <div class="notify-list">
                <div v-if="notifications.length === 0" class="notify-empty">暂无通知</div>
                <div
                  v-for="n in notifications"
                  :key="n.id"
                  class="notify-item"
                  :class="{ unread: !n.read }"
                  @click="onRead(n)"
                >
                  <div class="notify-content">{{ n.content }}</div>
                  <div class="notify-time">{{ formatTime(n.createdAt) }}</div>
                </div>
              </div>
            </div>
          </el-popover>
          <el-badge v-if="userStore.isLoggedIn" :value="msgUnreadCount" :hidden="msgUnreadCount === 0" class="notify-badge">
            <el-button circle @click="$router.push('/chat')">
              <el-icon><ChatDotRound /></el-icon>
            </el-button>
          </el-badge>
          <el-badge :value="cartCount" :hidden="cartCount === 0" class="cart-badge desktop-only">
            <el-button circle @click="$router.push('/cart')">
              <el-icon><ShoppingCart /></el-icon>
            </el-button>
          </el-badge>
          <el-button type="primary" round class="desktop-only" @click="$router.push('/publish')">
            <el-icon><Plus /></el-icon>&nbsp;发布
          </el-button>
          <template v-if="userStore.isLoggedIn">
            <el-dropdown @command="onCommand">
              <span class="user-chip">
                <el-avatar :size="30" class="avatar">
                  {{ (userStore.user?.nickname || 'U')[0] }}
                </el-avatar>
                <span class="name">{{ userStore.user?.nickname }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                  <el-dropdown-item command="cart">购物车</el-dropdown-item>
                  <el-dropdown-item command="favorites">我的收藏</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button link @click="$router.push('/login')">登录</el-button>
            <el-button type="primary" round @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </header>
    <main class="main">
      <router-view />
    </main>

    <nav class="bottom-nav">
      <router-link to="/" class="tab" :class="{ active: route.path === '/' }">
        <el-icon :size="22"><HomeFilled /></el-icon><span>首页</span>
      </router-link>
      <router-link to="/community" class="tab" :class="{ active: route.path === '/community' }">
        <el-icon :size="22"><ChatDotRound /></el-icon><span>社区</span>
      </router-link>
      <router-link to="/publish" class="tab publish-tab">
        <el-icon :size="26"><Plus /></el-icon>
      </router-link>
      <router-link to="/cart" class="tab" :class="{ active: route.path === '/cart' }">
        <el-badge :value="cartCount" :hidden="cartCount === 0">
          <el-icon :size="22"><ShoppingCart /></el-icon>
        </el-badge><span>购物车</span>
      </router-link>
      <router-link to="/profile" class="tab" :class="{ active: route.path === '/profile' }">
        <el-icon :size="22"><User /></el-icon><span>我的</span>
      </router-link>
    </nav>
  </div>
</template>

<script setup>
import { computed, ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { getCart } from '../api/cart'
import {
  getNotifications,
  getUnreadCount,
  markNotificationRead,
  markAllNotificationsRead
} from '../api/notification'
import { getUnreadCount as getMsgUnreadCount } from '../api/message'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartCount = ref(0)

const unreadCount = ref(0)
const notifications = ref([])
const msgUnreadCount = ref(0)

const activeMenu = computed(() => '/' + (route.path.split('/')[1] || ''))

async function loadCartCount() {
  if (!userStore.isLoggedIn) {
    cartCount.value = 0
    return
  }
  try {
    const res = await getCart()
    cartCount.value = res.data.length
  } catch (e) {
    cartCount.value = 0
  }
}

async function loadUnreadCount() {
  if (!userStore.isLoggedIn) {
    unreadCount.value = 0
    return
  }
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data
  } catch (e) {
    unreadCount.value = 0
  }
}

async function loadMsgUnreadCount() {
  if (!userStore.isLoggedIn) {
    msgUnreadCount.value = 0
    return
  }
  try {
    const res = await getMsgUnreadCount()
    msgUnreadCount.value = res.data
  } catch (e) {
    msgUnreadCount.value = 0
  }
}

async function loadNotifications() {
  if (!userStore.isLoggedIn) return
  try {
    const res = await getNotifications(0, 20)
    notifications.value = res.data.list || []
  } catch (e) {
    notifications.value = []
  }
}

async function onRead(n) {
  if (n.read) return
  try {
    await markNotificationRead(n.id)
    n.read = true
    unreadCount.value = Math.max(unreadCount.value - 1, 0)
  } catch (e) {
    // ignore
  }
}

async function onMarkAllRead() {
  try {
    await markAllNotificationsRead()
    notifications.value.forEach((n) => (n.read = true))
    unreadCount.value = 0
  } catch (e) {
    // ignore
  }
}

function formatTime(t) {
  return t ? t.replace('T', ' ').substring(0, 16) : ''
}

function onCommand(cmd) {
  if (cmd === 'profile') router.push('/profile')
  if (cmd === 'orders') router.push('/orders')
  if (cmd === 'cart') router.push('/cart')
  if (cmd === 'favorites') router.push('/favorites')
  if (cmd === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}

let pollTimer = null

function startPolling() {
  loadUnreadCount()
  loadMsgUnreadCount()
  if (pollTimer) clearInterval(pollTimer)
  pollTimer = setInterval(() => {
    loadUnreadCount()
    loadMsgUnreadCount()
  }, 30000)
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

watch(() => userStore.isLoggedIn, (loggedIn) => {
  loadCartCount()
  if (loggedIn) {
    startPolling()
  } else {
    unreadCount.value = 0
    notifications.value = []
    msgUnreadCount.value = 0
    stopPolling()
  }
})

onMounted(() => {
  loadCartCount()
  if (userStore.isLoggedIn) {
    startPolling()
  }
})

onBeforeUnmount(stopPolling)
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.header {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  gap: 24px;
}
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 700;
  color: #4f6ef7;
  text-decoration: none;
  white-space: nowrap;
}
.nav {
  flex: 1;
  border-bottom: none;
}
.right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.user-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #333;
}
.avatar {
  background: #4f6ef7;
  color: #fff;
}
.name {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.main {
  flex: 1;
}
.notify-panel {
  display: flex;
  flex-direction: column;
}
.notify-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}
.notify-title {
  font-weight: 600;
  color: #303133;
}
.notify-list {
  max-height: 320px;
  overflow-y: auto;
  margin-top: 4px;
}
.notify-empty {
  text-align: center;
  color: #909399;
  padding: 24px 0;
  font-size: 13px;
}
.notify-item {
  padding: 10px 4px;
  border-bottom: 1px solid #f7f7f7;
  cursor: pointer;
}
.notify-item:hover {
  background: #f5f7fa;
}
.notify-item.unread .notify-content {
  font-weight: 600;
  color: #303133;
}
.notify-content {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
}
.notify-time {
  margin-top: 4px;
  font-size: 12px;
  color: #c0c4cc;
}
.bottom-nav {
  display: none;
}
@media (max-width: 640px) {
  .desktop-only {
    display: none !important;
  }
  .logo span {
    display: none;
  }
  .header-inner {
    gap: 12px;
  }
  .main {
    padding-bottom: 60px;
  }
  .bottom-nav {
    display: flex;
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    height: 56px;
    background: #fff;
    border-top: 1px solid #eee;
    z-index: 100;
    align-items: center;
    justify-content: space-around;
  }
  .bottom-nav .tab {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 2px;
    font-size: 11px;
    color: #909399;
    text-decoration: none;
  }
  .bottom-nav .tab.active {
    color: #4f6ef7;
  }
  .bottom-nav .publish-tab {
    color: #4f6ef7;
  }
}
</style>
