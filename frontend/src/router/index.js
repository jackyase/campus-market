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
      { path: 'orders/:id', name: 'order-detail', component: () => import('../views/OrderDetail.vue'), meta: { requiresAuth: true } }
    ]
  },
  { path: '/login', name: 'login', component: () => import('../views/Login.vue') },
  { path: '/register', name: 'register', component: () => import('../views/Register.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  if (to.meta.requiresAuth && !localStorage.getItem('token')) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
})

export default router
