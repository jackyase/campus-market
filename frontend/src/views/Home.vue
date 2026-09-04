<template>
  <div class="home">
    <div class="hero">
      <h1>发现校园好物</h1>
      <p>买卖二手，就在身边</p>
      <div class="search">
        <el-input
          v-model="query.keyword"
          class="search-input"
          size="large"
          placeholder="搜索你想要的商品，如：自行车、教材、吉他…"
          :prefix-icon="Search"
          clearable
          @keyup.enter="onSearch"
        />
        <el-button type="primary" size="large" @click="onSearch">搜索</el-button>
      </div>
    </div>

    <div class="container">
      <div class="cats">
        <el-radio-group v-model="query.categoryId" @change="onSearch">
          <el-radio-button :value="null">全部</el-radio-button>
          <el-radio-button v-for="c in categories" :key="c.id" :value="c.id">
            {{ c.name }}
          </el-radio-button>
        </el-radio-group>
      </div>

      <div class="filter-bar">
        <el-select v-model="query.condition" placeholder="成色" clearable style="width: 160px" @change="onSearch">
          <el-option v-for="(label, key) in CONDITION_LABELS" :key="key" :label="label" :value="key" />
        </el-select>
        <el-input-number v-model="query.minPrice" :min="0" placeholder="最低价" :controls="false" style="width: 120px" @change="onSearch" />
        <span class="dash">—</span>
        <el-input-number v-model="query.maxPrice" :min="0" placeholder="最高价" :controls="false" style="width: 120px" @change="onSearch" />
        <el-select v-model="query.sort" style="width: 140px" @change="onSearch">
          <el-option label="最新发布" value="latest" />
          <el-option label="价格从低到高" value="priceAsc" />
          <el-option label="价格从高到低" value="priceDesc" />
          <el-option label="热度最高" value="hot" />
        </el-select>
      </div>

      <div v-if="loading" class="grid">
        <el-skeleton v-for="i in 8" :key="i" animated>
          <template #template>
            <el-skeleton-item variant="image" style="width: 100%; height: 180px" />
            <el-skeleton-item variant="text" style="width: 80%; margin-top: 10px" />
            <el-skeleton-item variant="text" style="width: 40%" />
          </template>
        </el-skeleton>
      </div>

      <el-empty v-else-if="products.length === 0" description="没有找到相关商品" :image-size="120" />

      <div v-else class="grid">
        <el-card v-for="p in products" :key="p.id" class="card" shadow="hover" :body-style="{ padding: 0 }" @click="goDetail(p.id)">
          <el-image lazy :src="p.images[0]" fit="cover" class="cover">
            <template #error>
              <div class="img-fallback"><el-icon :size="32"><Picture /></el-icon></div>
            </template>
          </el-image>
          <div class="card-body">
            <div class="title">{{ p.title }}</div>
            <div class="price-row">
              <span class="price">¥{{ p.price }}</span>
              <span v-if="p.originalPrice" class="original">¥{{ p.originalPrice }}</span>
              <el-tag :type="CONDITION_TAG_TYPES[p.condition] || 'info'" size="small" class="tag">
                {{ CONDITION_LABELS[p.condition] }}
              </el-tag>
            </div>
            <div class="meta">
              <span class="seller">{{ p.sellerName }}</span>
              <span class="cat">{{ p.categoryName }}</span>
            </div>
          </div>
        </el-card>
      </div>

      <div v-if="total > query.size" class="pager">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="query.size"
          :current-page="query.page + 1"
          @current-change="onPageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Picture } from '@element-plus/icons-vue'
import { getProducts } from '../api/product'
import { getCategories } from '../api/category'
import { CONDITION_LABELS, CONDITION_TAG_TYPES } from '../utils/constants'

const router = useRouter()
const categories = ref([])
const products = ref([])
const total = ref(0)
const loading = ref(false)

const query = reactive({
  keyword: '',
  categoryId: null,
  condition: null,
  minPrice: null,
  maxPrice: null,
  sort: 'latest',
  page: 0,
  size: 12
})

async function load() {
  loading.value = true
  try {
    const params = { page: query.page, size: query.size }
    if (query.keyword) params.keyword = query.keyword
    if (query.categoryId != null) params.categoryId = query.categoryId
    if (query.condition) params.condition = query.condition
    if (query.minPrice != null) params.minPrice = query.minPrice
    if (query.maxPrice != null) params.maxPrice = query.maxPrice
    if (query.sort && query.sort !== 'latest') params.sort = query.sort
    const res = await getProducts(params)
    products.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function onSearch() {
  query.page = 0
  load()
}

function onPageChange(page) {
  query.page = page - 1
  load()
}

function goDetail(id) {
  router.push(`/product/${id}`)
}

onMounted(async () => {
  const res = await getCategories()
  categories.value = res.data
  load()
})
</script>

<style scoped>
.hero {
  background: linear-gradient(135deg, #4f6ef7 0%, #7b5cf0 100%);
  padding: 48px 16px;
  text-align: center;
  color: #fff;
}
.hero h1 {
  font-size: 32px;
  font-weight: 700;
}
.hero p {
  margin-top: 6px;
  font-size: 15px;
  opacity: 0.9;
}
.search {
  max-width: 560px;
  margin: 24px auto 0;
  display: flex;
  gap: 12px;
}
.search-input {
  flex: 1;
}
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px 16px 48px;
}
.cats {
  margin-bottom: 16px;
  overflow-x: auto;
  white-space: nowrap;
}
.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.dash {
  color: #909399;
}
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(230px, 1fr));
  gap: 16px;
}
.card {
  cursor: pointer;
  border-radius: 10px;
  overflow: hidden;
}
.cover {
  width: 100%;
  height: 180px;
  display: block;
}
.img-fallback {
  height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
  color: #c0c4cc;
}
.card-body {
  padding: 12px;
}
.title {
  font-size: 14px;
  color: #303133;
  height: 40px;
  line-height: 20px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.price-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
}
.price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: 700;
}
.original {
  color: #c0c4cc;
  font-size: 13px;
  text-decoration: line-through;
}
.tag {
  margin-left: auto;
}
.meta {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}
.pager {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
@media (max-width: 640px) {
  .hero {
    padding: 32px 16px;
  }
  .hero h1 {
    font-size: 26px;
  }
  .hero p {
    font-size: 14px;
  }
}
</style>
