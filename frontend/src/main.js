import { createApp } from 'vue'
import { createPinia } from 'pinia'
import {
  ArrowDown, ArrowLeft, ArrowRight, Bell, ChatDotRound, CircleClose,
  Delete, Edit, Goods, HomeFilled, List, Lock, MoreFilled, Picture,
  Plus, Postcard, School, Search, Shop, ShoppingCart, Star, StarFilled,
  User, VideoPlay
} from '@element-plus/icons-vue'
import { ElLoading } from 'element-plus'
import App from './App.vue'
import router from './router'
import 'element-plus/theme-chalk/el-message.css'
import 'element-plus/theme-chalk/el-message-box.css'
import 'element-plus/theme-chalk/el-loading.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.directive('loading', ElLoading.directive)

const icons = {
  ArrowDown, ArrowLeft, ArrowRight, Bell, ChatDotRound, CircleClose,
  Delete, Edit, Goods, HomeFilled, List, Lock, MoreFilled, Picture,
  Plus, Postcard, School, Search, Shop, ShoppingCart, Star, StarFilled,
  User, VideoPlay
}
for (const [name, component] of Object.entries(icons)) {
  app.component(name, component)
}

app.mount('#app')
