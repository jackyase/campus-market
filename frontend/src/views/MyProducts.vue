<template>
  <div class="my-products">
    <div class="container" v-loading="loading">
      <div class="head">
        <h2>货架管理</h2>
        <el-button type="primary" @click="$router.push('/publish')">上架商品</el-button>
      </div>

      <el-empty v-if="!loading && products.length === 0" description="还没有商品，去上架一个吧" :image-size="120" />

      <el-table v-else :data="products" style="width: 100%">
        <el-table-column label="商品" min-width="280">
          <template #default="{ row }">
            <div class="goods" @click="$router.push(`/product/${row.id}`)">
              <el-image lazy :src="row.images[0]" fit="cover" class="goods-img">
                <template #error><div class="img-fallback"><el-icon><Picture /></el-icon></div></template>
              </el-image>
              <div class="goods-title">{{ row.title }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="价格" width="110">
          <template #default="{ row }"><span class="price">¥{{ row.price }}</span></template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ STATUS_LABELS[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button v-if="row.status === 'ON_SALE'" size="small" @click="toggle(row, 'OFF_SALE')">下架</el-button>
            <el-button v-else-if="row.status === 'OFF_SALE'" size="small" type="primary" @click="toggle(row, 'ON_SALE')">上架</el-button>
            <span v-else class="muted">-</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { getMyProducts, updateProductStatus } from '../api/product'
import { STATUS_LABELS } from '../utils/constants'

const products = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const res = await getMyProducts()
    products.value = res.data
  } finally {
    loading.value = false
  }
}

async function toggle(row, status) {
  await updateProductStatus(row.id, status)
  ElMessage.success(status === 'ON_SALE' ? '已上架' : '已下架')
  load()
}

function statusType(status) {
  if (status === 'ON_SALE') return 'success'
  if (status === 'OFF_SALE') return 'info'
  if (status === 'SOLD') return 'danger'
  return 'warning'
}

onMounted(load)
</script>

<style scoped>
.container {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px 16px 48px;
}
.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.head h2 {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
}
.goods {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}
.goods-img {
  width: 56px;
  height: 56px;
  border-radius: 6px;
  flex-shrink: 0;
}
.img-fallback {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
  color: #c0c4cc;
}
.goods-title {
  font-size: 14px;
  color: #303133;
}
.price {
  color: #f56c6c;
  font-weight: 600;
}
.muted {
  color: #c0c4cc;
}
</style>
