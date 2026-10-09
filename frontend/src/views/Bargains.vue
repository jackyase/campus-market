<template>
  <div class="bargains-page">
    <div class="container">
      <h2 class="page-title">砍价</h2>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="我发起的" name="mine">
          <div v-if="mineList.length" class="list">
            <div v-for="b in mineList" :key="b.id" class="item">
              <div class="item-main">
                <div class="item-title">{{ b.productTitle }}</div>
                <div class="item-sub">出价 ¥{{ b.price }}</div>
              </div>
              <el-tag :type="statusType(b.status)" size="small">{{ statusLabel(b.status) }}</el-tag>
            </div>
          </div>
          <el-empty v-else description="还没有发起过砍价" :image-size="90" />
        </el-tab-pane>
        <el-tab-pane label="收到的砍价" name="incoming">
          <div v-if="incomingList.length" class="list">
            <div v-for="b in incomingList" :key="b.id" class="item">
              <div class="item-main">
                <div class="item-title">{{ b.productTitle }}</div>
                <div class="item-sub">{{ b.buyerName }} 出价 ¥{{ b.price }}</div>
              </div>
              <template v-if="b.status === 'PENDING'">
                <el-button size="small" type="success" @click="onAccept(b)">接受</el-button>
                <el-button size="small" @click="onReject(b)">拒绝</el-button>
              </template>
              <el-tag v-else :type="statusType(b.status)" size="small">{{ statusLabel(b.status) }}</el-tag>
            </div>
          </div>
          <el-empty v-else description="还没有收到砍价" :image-size="90" />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyBargains, getIncomingBargains, acceptBargain, rejectBargain } from '../api/bargain'

const activeTab = ref('mine')
const mineList = ref([])
const incomingList = ref([])

function statusType(s) {
  if (s === 'ACCEPTED') return 'success'
  if (s === 'COMPLETED') return 'success'
  if (s === 'REJECTED') return 'info'
  return 'warning'
}
function statusLabel(s) {
  if (s === 'ACCEPTED') return '已接受'
  if (s === 'COMPLETED') return '已成单'
  if (s === 'REJECTED') return '已拒绝'
  return '待处理'
}

async function load() {
  try {
    const [mine, incoming] = await Promise.all([getMyBargains(), getIncomingBargains()])
    mineList.value = mine.data || []
    incomingList.value = incoming.data || []
  } catch (e) {
    // ignore
  }
}

async function onAccept(b) {
  await acceptBargain(b.id)
  ElMessage.success('已接受砍价')
  load()
}

async function onReject(b) {
  await rejectBargain(b.id)
  ElMessage.success('已拒绝砍价')
  load()
}

onMounted(load)
</script>

<style scoped>
.container {
  max-width: 860px;
  margin: 0 auto;
  padding: 24px 16px 48px;
}
.page-title {
  font-size: 20px;
  color: #303133;
  margin: 0 0 20px;
}
.list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.item {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fff;
  border-radius: 10px;
  padding: 14px 16px;
}
.item-main {
  flex: 1;
  min-width: 0;
}
.item-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.item-sub {
  font-size: 13px;
  color: #f56c6c;
  margin-top: 4px;
}
</style>
