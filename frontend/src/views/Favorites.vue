<template>
  <div class="favorites-page">
    <div class="container">
      <h2 class="page-title">我的收藏</h2>
      <div v-if="products.length" class="prod-grid">
        <el-card
          v-for="p in products"
          :key="p.id"
          class="prod-card"
          :body-style="{ padding: 0 }"
          shadow="hover"
          @click="$router.push(`/product/${p.id}`)"
        >
          <el-image lazy :src="p.images[0]" fit="cover" class="prod-img">
            <template #error><div class="img-fallback"><el-icon :size="30"><Picture /></el-icon></div></template>
          </el-image>
          <div class="prod-body">
            <div class="prod-title">{{ p.title }}</div>
            <div class="prod-price">¥{{ p.price }}</div>
          </div>
        </el-card>
      </div>
      <el-empty v-else description="还没有收藏的商品" :image-size="100" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getFavorites } from '../api/favorite'

const products = ref([])

onMounted(async () => {
  try {
    const res = await getFavorites()
    products.value = res.data || []
  } catch (e) {
    products.value = []
  }
})
</script>

<style scoped>
.container {
  max-width: 1100px;
  margin: 0 auto;
  padding: 24px 16px 48px;
}
.page-title {
  font-size: 20px;
  color: #303133;
  margin: 0 0 20px;
}
.prod-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.prod-card {
  cursor: pointer;
}
.prod-img {
  width: 100%;
  height: 180px;
  background: #f5f6f8;
}
.img-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 180px;
  color: #c0c4cc;
}
.prod-body {
  padding: 12px;
}
.prod-title {
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  min-height: 40px;
}
.prod-price {
  color: #f56c6c;
  font-size: 17px;
  font-weight: 700;
  margin-top: 8px;
}
@media (max-width: 768px) {
  .prod-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  .prod-img {
    height: 140px;
  }
}
</style>
