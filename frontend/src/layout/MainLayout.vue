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
import { computed, ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { getCart } from '../api/cart'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartCount = ref(0)

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

function onCommand(cmd) {
  if (cmd === 'profile') router.push('/profile')
  if (cmd === 'orders') router.push('/orders')
  if (cmd === 'cart') router.push('/cart')
  if (cmd === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}

watch(() => userStore.isLoggedIn, loadCartCount)
onMounted(loadCartCount)
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
