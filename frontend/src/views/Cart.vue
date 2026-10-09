<template>
  <div class="cart-page">
    <div class="container">
      <h2 class="page-title">购物车</h2>

      <el-card v-if="items.length === 0" class="empty">
        <el-empty description="购物车是空的，去逛逛吧">
          <el-button type="primary" @click="$router.push('/')">去首页</el-button>
        </el-empty>
      </el-card>

      <el-card v-else :body-style="{ padding: 0 }">
        <el-table ref="tableRef" :data="items" @selection-change="onSelectionChange" style="width: 100%">
          <el-table-column type="selection" width="50" />
          <el-table-column label="商品" min-width="320">
            <template #default="{ row }">
              <div class="goods" @click="$router.push(`/product/${row.productId}`)">
                <el-image :src="row.image" fit="cover" class="goods-img">
                  <template #error><div class="img-fallback"><el-icon><Picture /></el-icon></div></template>
                </el-image>
                <div class="goods-info">
                  <div class="goods-title">{{ row.title }}</div>
                  <el-tag v-if="!row.available" type="danger" size="small">已失效</el-tag>
                  <el-tag v-else size="small">{{ CONDITION_LABELS[row.condition] }}</el-tag>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="单价" width="120">
            <template #default="{ row }">
              <span class="price">¥{{ row.price }}</span>
            </template>
          </el-table-column>
          <el-table-column label="数量" width="160">
            <template #default="{ row }">
              <el-input-number
                v-model="row.quantity"
                :min="1"
                :max="Math.max(row.stock, 1)"
                size="small"
                @change="(v) => onChangeQuantity(row, v)"
              />
            </template>
          </el-table-column>
          <el-table-column label="小计" width="120">
            <template #default="{ row }">
              <span class="price">¥{{ (row.price * row.quantity).toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="90">
            <template #default="{ row }">
              <el-button link type="danger" @click="onRemove(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <div v-if="items.length > 0" class="footer-bar">
        <div class="total">
          已选 <span class="count">{{ selected.length }}</span> 件，合计：
          <span class="amount">¥{{ totalAmount }}</span>
        </div>
        <el-button type="danger" size="large" :disabled="selected.length === 0" :loading="checkingOut" @click="onCheckout">
          去结算
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { getCart, updateCart, removeCart } from '../api/cart'
import { checkoutCart } from '../api/order'
import { CONDITION_LABELS } from '../utils/constants'

const router = useRouter()
const tableRef = ref(null)
const items = ref([])
const selected = ref([])
const checkingOut = ref(false)

const totalAmount = computed(() =>
  selected.value
    .filter((i) => i.available)
    .reduce((sum, i) => sum + i.price * i.quantity, 0)
    .toFixed(2)
)

async function load() {
  const res = await getCart()
  items.value = res.data
  // 默认全选所有可用商品，让「去结算」无需手动勾选即可用
  await nextTick()
  items.value.forEach((row) => {
    if (row.available) tableRef.value?.toggleRowSelection(row, true)
  })
}

function onSelectionChange(rows) {
  selected.value = rows
}

async function onChangeQuantity(row, val) {
  if (!row.available) return
  try {
    await updateCart(row.id, val)
  } catch (e) {
    load()
  }
}

async function onRemove(row) {
  await ElMessageBox.confirm('确定删除该商品吗？', '提示', { type: 'warning' })
  await removeCart(row.id)
  ElMessage.success('已删除')
  load()
}

async function onCheckout() {
  const valid = selected.value.filter((i) => i.available)
  if (valid.length === 0) {
    ElMessage.warning('请选择有效的商品')
    return
  }
  checkingOut.value = true
  try {
    await checkoutCart(valid.map((i) => i.id))
    ElMessage.success('下单成功，请尽快付款')
    router.push('/orders')
  } finally {
    checkingOut.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px 16px 48px;
}
.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 16px;
}
.goods {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}
.goods-img {
  width: 64px;
  height: 64px;
  border-radius: 6px;
  flex-shrink: 0;
}
.img-fallback {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
  color: #c0c4cc;
}
.goods-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.goods-title {
  font-size: 14px;
  color: #303133;
}
.price {
  color: #f56c6c;
  font-weight: 600;
}
.footer-bar {
  position: sticky;
  bottom: 0;
  margin-top: 16px;
  background: #fff;
  border-radius: 10px;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 20px;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.04);
}
.count {
  color: #f56c6c;
  font-weight: 700;
}
.amount {
  color: #f56c6c;
  font-size: 22px;
  font-weight: 700;
}
</style>
