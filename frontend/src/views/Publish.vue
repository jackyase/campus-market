<template>
  <div class="publish-page">
    <div class="container">
      <el-card>
        <template #header>
          <span class="card-title">发布闲置商品</span>
        </template>

        <el-form :model="form" :rules="rules" ref="formRef" label-width="90px" style="max-width: 640px">
          <el-form-item label="商品标题" prop="title">
            <el-input v-model="form.title" maxlength="100" show-word-limit placeholder="一句话描述你的商品" />
          </el-form-item>

          <el-form-item label="分类" prop="categoryId">
            <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
              <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </el-form-item>

          <el-form-item label="成色" prop="condition">
            <el-select v-model="form.condition" placeholder="请选择成色" style="width: 100%">
              <el-option v-for="(label, key) in CONDITION_LABELS" :key="key" :label="label" :value="key" />
            </el-select>
          </el-form-item>

          <el-form-item label="价格" prop="price">
            <el-input-number v-model="form.price" :min="0.01" :precision="2" :step="1" style="width: 200px" />
          </el-form-item>

          <el-form-item label="原价">
            <el-input-number v-model="form.originalPrice" :min="0" :precision="2" :step="1" style="width: 200px" />
          </el-form-item>

          <el-form-item label="库存">
            <el-input-number v-model="form.stock" :min="1" :max="999" style="width: 200px" />
          </el-form-item>

          <el-form-item label="商品图片">
            <div class="uploader">
              <el-upload
                list-type="picture-card"
                accept="image/*"
                :http-request="doUpload"
                :on-remove="onRemove"
                :limit="6"
                :file-list="fileList"
              >
                <el-icon><Plus /></el-icon>
              </el-upload>
              <div class="upload-tip">最多 6 张，第一张为封面</div>
            </div>
          </el-form-item>

          <el-form-item label="描述">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="5"
              maxlength="1000"
              show-word-limit
              placeholder="补充商品的详细情况，如购买时间、使用情况、出售原因等"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" :loading="submitting" @click="onSubmit">发布</el-button>
            <el-button @click="$router.back()">取消</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getCategories } from '../api/category'
import { createProduct } from '../api/product'
import { uploadImage } from '../api/upload'
import { useUserStore } from '../store/user'
import { CONDITION_LABELS } from '../utils/constants'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const categories = ref([])
const submitting = ref(false)
const imageUrls = ref([])
const fileList = ref([])

const form = reactive({
  title: '',
  categoryId: null,
  condition: 'LIGHTLY_USED',
  price: null,
  originalPrice: null,
  stock: 1,
  description: ''
})

const rules = {
  title: [{ required: true, message: '请输入商品标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  condition: [{ required: true, message: '请选择成色', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

async function doUpload(options) {
  try {
    const res = await uploadImage(options.file)
    const url = res.data.url
    imageUrls.value.push(url)
    options.onSuccess(url)
  } catch (e) {
    options.onError(e)
    ElMessage.error('图片上传失败')
  }
}

function onRemove(file) {
  const url = file.response || file.url
  const idx = imageUrls.value.indexOf(url)
  if (idx >= 0) imageUrls.value.splice(idx, 1)
}

async function onSubmit() {
  if (userStore.user?.role !== 'MERCHANT') {
    ElMessage.warning('请先在个人中心开通商户')
    router.push('/profile')
    return
  }
  await formRef.value.validate()
  submitting.value = true
  try {
    const res = await createProduct({
      title: form.title,
      categoryId: form.categoryId,
      condition: form.condition,
      price: form.price,
      originalPrice: form.originalPrice || null,
      stock: form.stock,
      description: form.description,
      imageUrls: imageUrls.value
    })
    ElMessage.success('发布成功')
    router.push(`/product/${res.data.id}`)
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  const res = await getCategories()
  categories.value = res.data
})
</script>

<style scoped>
.container {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px 16px 48px;
}
.card-title {
  font-size: 16px;
  font-weight: 700;
}
.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 6px;
}
</style>
