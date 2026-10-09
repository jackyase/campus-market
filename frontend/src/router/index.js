import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layout/MainLayout.vue'

const routes = [
  {
    path: '/',
    component: MainLayout,
    children: [
      { path: '', name: 'home', component: () => import('../views/Home.vue') },
      { path: 'community', name: 'community', component: () => import('../views/Community.vue') },
      { path: 'profile', name: 'profile', component: () => import('../views/Profile.vue'), meta: { requiresAuth: true } },
      { path: 'product/:id', name: 'product-detail', component: () => import('../views/ProductDetail.vue') },
      { path: 'user/:id', name: 'user-profile', component: () => import('../views/Profile.vue') },
      { path: 'publish', name: 'publish', component: () => import('../views/Publish.vue'), meta: { requiresAuth: true } },
      { path: 'merchant', name: 'merchant', component: () => import('../views/MerchantCenter.vue'), meta: { requiresAuth: true } },
      { path: 'merchant/products', name: 'merchant-products', component: () => import('../views/MyProducts.vue'), meta: { requiresAuth: true } },
      { path: 'merchant/orders', name: 'merchant-orders', component: () => import('../views/SoldOrders.vue'), meta: { requiresAuth: true } },
      { path: 'cart', name: 'cart', component: () => import('../views/Cart.vue'), meta: { requiresAuth: true } },
      { path: 'orders', name: 'orders', component: () => import('../views/OrderList.vue'), meta: { requiresAuth: true } },
      { path: 'orders/:id', name: 'order-detail', component: () => import('../views/OrderDetail.vue'), meta: { requiresAuth: true } },
      { path: 'chat', name: 'chat', component: () => import('../views/Chat.vue'), meta: { requiresAuth: true } },
      { path: 'favorites', name: 'favorites', component: () => import('../views/Favorites.vue'), meta: { requiresAuth: true } },
      { path: 'bargains', name: 'bargains', component: () => import('../views/Bargains.vue'), meta: { requiresAuth: true } }
    ]
  },
  { path: '/login', name: 'login', component: () => import('../views/Login.vue') },
  { path: '/register', name: 'register', component: () => import('../views/Register.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition
    return { top: 0 }
  }
})

// —— 顶部加载进度条（轻量，无第三方依赖）——
let progressEl = null

function ensureProgress() {
  if (progressEl) return progressEl
  progressEl = document.createElement('div')
  progressEl.style.cssText =
    'position:fixed;top:0;left:0;height:2px;width:0;background:linear-gradient(90deg,#4f6ef7,#7b5cf0);' +
    'z-index:9999;opacity:0;box-shadow:0 0 6px rgba(79,110,247,.4);pointer-events:none;'
  document.body.appendChild(progressEl)
  return progressEl
}

function startProgress() {
  const el = ensureProgress()
  el.style.transition = 'none'
  el.style.width = '0'
  el.style.opacity = '1'
  void el.offsetWidth
  el.style.transition = 'width .3s ease'
  el.style.width = '72%'
}

function finishProgress() {
  if (!progressEl) return
  progressEl.style.transition = 'width .15s ease, opacity .3s ease'
  progressEl.style.width = '100%'
  setTimeout(() => {
    progressEl.style.opacity = '0'
    setTimeout(() => {
      progressEl.style.width = '0'
    }, 300)
  }, 150)
}

router.beforeEach((to) => {
  startProgress()
  if (to.meta.requiresAuth && !localStorage.getItem('token')) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
})

router.afterEach(() => {
  finishProgress()
})

router.onError(() => {
  finishProgress()
})

export default router
