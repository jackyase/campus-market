<template>
  <div class="profile-page">
    <div class="profile-header">
      <div class="cover" :style="coverStyle"></div>
      <div class="header-content container">
        <el-avatar :size="88" class="avatar" :src="profile?.avatar">{{ initial }}</el-avatar>
        <div class="info">
          <div class="name-row">
            <h1 class="name">{{ displayName }}</h1>
            <el-button v-if="isMe" size="small" round @click="openEdit">
              <el-icon><Edit /></el-icon>&nbsp;编辑资料
            </el-button>
            <el-button v-if="isMe && !isMerchant" type="primary" size="small" round @click="storeDialogVisible = true">
              <el-icon><Shop /></el-icon>&nbsp;开店
            </el-button>
            <el-button v-if="!isMe" :type="profile?.following ? 'default' : 'primary'" size="small" round @click="onFollow">
              {{ profile?.following ? '已关注' : '+ 关注' }}
            </el-button>
            <el-button v-if="!isMe" size="small" round @click="onMessage">
              <el-icon><ChatDotRound /></el-icon>&nbsp;私信
            </el-button>
          </div>
          <div class="username">@{{ profile?.username }}</div>
          <div v-if="schoolLine" class="school">
            <el-icon><School /></el-icon>{{ schoolLine }}
          </div>
          <div v-if="profile?.bio" class="bio">{{ profile.bio }}</div>
          <div v-else-if="isMe" class="bio empty-bio">还没有自我介绍，点击「编辑资料」写点什么吧～</div>
          <div v-if="interestTags.length" class="tags">
            <el-tag v-for="t in interestTags" :key="t" size="small" round effect="plain">{{ t }}</el-tag>
          </div>
          <div class="stats-line">
            <span><b>{{ profile?.onSaleCount || 0 }}</b> 在售</span>
            <span><b>{{ profile?.followerCount || 0 }}</b> 粉丝</span>
            <span><b>{{ profile?.followingCount || 0 }}</b> 关注</span>
          </div>
        </div>
        <div v-if="profile?.sellerRatingCount" class="rating-box">
          <div class="score">{{ (profile.sellerRating || 0).toFixed(1) }}</div>
          <el-rate :model-value="Math.round(profile.sellerRating || 0)" disabled />
          <div class="count">{{ profile.sellerRatingCount }} 条评价</div>
        </div>
      </div>
    </div>

    <div class="container body">
      <div v-if="isMe && isMerchant" class="merchant-nav">
        <el-button @click="$router.push('/merchant')"><el-icon><Shop /></el-icon>&nbsp;商家中心</el-button>
        <el-button @click="$router.push('/merchant/products')"><el-icon><Goods /></el-icon>&nbsp;货架管理</el-button>
        <el-button @click="$router.push('/merchant/orders')"><el-icon><List /></el-icon>&nbsp;卖出订单</el-button>
      </div>
      <el-tabs v-model="activeTab">
        <el-tab-pane :label="`动态 (${posts.length})`" name="posts">
          <div v-if="posts.length" class="post-list">
            <div v-for="p in posts" :key="p.id" class="post-item">
              <div class="post-content">{{ p.content }}</div>
              <div v-if="p.topics.length" class="post-topics">
                <el-tag v-for="t in p.topics" :key="t" size="small" round effect="plain"># {{ t }}</el-tag>
              </div>
              <div class="post-time">{{ formatTime(p.createdAt) }}</div>
            </div>
          </div>
          <el-empty v-else :description="isMe ? '你还没有发过帖子' : 'TA 还没有发过帖子'" :image-size="100" />
        </el-tab-pane>

        <el-tab-pane v-if="isMerchant" :label="`在卖的宝贝 (${products.length})`" name="products">
          <div v-if="products.length" class="prod-grid">
            <el-card v-for="p in products" :key="p.id" class="prod-card" :body-style="{ padding: 0 }" shadow="hover"
              @click="$router.push(`/product/${p.id}`)">
              <el-image lazy :src="p.images[0]" fit="cover" class="prod-img">
                <template #error><div class="img-fallback"><el-icon :size="30"><Picture /></el-icon></div></template>
              </el-image>
              <div class="prod-body">
                <div class="prod-title">{{ p.title }}</div>
                <div class="prod-price">¥{{ p.price }}</div>
              </div>
            </el-card>
          </div>
          <el-empty v-else :description="isMe ? '你还没有在售的商品' : 'TA 还没有在售的商品'" :image-size="100" />
        </el-tab-pane>

        <el-tab-pane v-if="isMerchant" :label="`收到的评价 (${reviews.length})`" name="reviews">
          <div v-if="reviews.length" class="review-list">
            <div v-for="r in reviews" :key="r.id" class="review-item">
              <el-avatar :size="38" :src="r.reviewerAvatar">{{ (r.reviewerName || 'U')[0] }}</el-avatar>
              <div class="review-main">
                <div class="review-head">
                  <span class="review-name">{{ r.reviewerName }}</span>
                  <el-rate :model-value="r.sellerRating" disabled size="small" />
                </div>
                <div class="review-product">商品：{{ r.productTitle }}</div>
                <div class="review-content">{{ r.content || '（无文字评价）' }}</div>
                <div class="review-time">{{ formatTime(r.createdAt) }}</div>
              </div>
            </div>
          </div>
          <el-empty v-else :description="isMe ? '还没有收到评价' : 'TA 还没有收到评价'" :image-size="100" />
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="storeDialogVisible" title="开通商户" width="480px">
      <el-form :model="storeForm" label-width="80px">
        <el-form-item label="店铺名">
          <el-input v-model="storeForm.name" maxlength="100" placeholder="给你的小店起个名字" />
        </el-form-item>
        <el-form-item label="公告">
          <el-input v-model="storeForm.announcement" type="textarea" :rows="3" maxlength="300" placeholder="选填，如：毕业闲置，价格可小刀" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="storeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="openingStore" @click="onOpenStore">开通</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editVisible" title="编辑资料" width="620px" top="6vh">
      <el-form :model="form" label-width="80px">
        <el-form-item label="头像">
          <el-upload :show-file-list="false" :http-request="uploadAvatar" accept="image/*">
            <el-avatar :size="64" :src="form.avatar" class="edit-avatar">{{ initial }}</el-avatar>
            <span class="upload-hint">点击更换头像</span>
          </el-upload>
        </el-form-item>
        <el-form-item label="封面图">
          <el-upload :show-file-list="false" :http-request="uploadCover" accept="image/*">
            <div class="cover-upload" :style="coverFormStyle">
              <span>点击更换封面</span>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" maxlength="50" />
        </el-form-item>
        <el-form-item label="自我介绍">
          <el-input v-model="form.bio" type="textarea" :rows="4" maxlength="500" show-word-limit
            placeholder="介绍一下自己，让同学们更了解你～" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="form.gender" placeholder="请选择" style="width: 160px">
            <el-option label="男" value="MALE" />
            <el-option label="女" value="FEMALE" />
            <el-option label="保密" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="生日">
          <el-date-picker v-model="form.birthday" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 180px" />
        </el-form-item>
        <el-form-item label="学校">
          <el-input v-model="form.school" placeholder="如：某某大学" />
        </el-form-item>
        <el-form-item label="学院">
          <el-input v-model="form.college" placeholder="如：计算机学院" />
        </el-form-item>
        <el-form-item label="学号">
          <el-input v-model="form.studentId" placeholder="选填" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="选填" />
        </el-form-item>
        <el-form-item label="兴趣标签">
          <el-input v-model="form.interests" placeholder="用逗号分隔，如：篮球,音乐,摄影" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Picture, School, Edit, Shop, Goods, List } from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'
import { getMe } from '../api/auth'
import { updateProfile, getPublicProfile, getUserProducts, getUserReviews } from '../api/user'
import { followUser, getUserPosts } from '../api/community'
import { openStore } from '../api/store'
import { uploadImage } from '../api/upload'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isMe = computed(() => !route.params.id)
const userId = ref(null)
const profile = ref(null)
const products = ref([])
const reviews = ref([])
const posts = ref([])
const activeTab = ref('posts')
const editVisible = ref(false)
const saving = ref(false)

const form = reactive({
  nickname: '', avatar: '', cover: '', bio: '', gender: '',
  birthday: '', school: '', college: '', studentId: '', phone: '', interests: ''
})

const displayName = computed(() => profile.value?.nickname || profile.value?.username || '')
const initial = computed(() => (displayName.value || 'U')[0])
const schoolLine = computed(() => [profile.value?.school, profile.value?.college].filter(Boolean).join(' · '))
const interestTags = computed(() => {
  if (!profile.value?.interests) return []
  return profile.value.interests.split(/[,，]/).map((s) => s.trim()).filter(Boolean)
})
const coverStyle = computed(() =>
  profile.value?.cover ? { backgroundImage: `url(${profile.value.cover})` } : {}
)
const coverFormStyle = computed(() =>
  form.cover ? { backgroundImage: `url(${form.cover})` } : {}
)

const isMerchant = computed(() => !!profile.value?.storeName)
const storeDialogVisible = ref(false)
const openingStore = ref(false)
const storeForm = reactive({ name: '', announcement: '' })

async function load() {
  if (isMe.value) {
    const me = await getMe()
    userId.value = me.data.id
  } else {
    userId.value = Number(route.params.id)
  }
  const [pub, prod, rev, pst] = await Promise.all([
    getPublicProfile(userId.value),
    getUserProducts(userId.value),
    getUserReviews(userId.value),
    getUserPosts(userId.value)
  ])
  profile.value = pub.data
  products.value = prod.data
  reviews.value = rev.data
  posts.value = pst.data
}

function openEdit() {
  Object.assign(form, {
    nickname: profile.value?.nickname || '',
    avatar: profile.value?.avatar || '',
    cover: profile.value?.cover || '',
    bio: profile.value?.bio || '',
    gender: profile.value?.gender || '',
    birthday: profile.value?.birthday || '',
    school: profile.value?.school || '',
    college: profile.value?.college || '',
    studentId: '',
    phone: '',
    interests: profile.value?.interests || ''
  })
  // 拉取完整个人资料填充私密字段
  if (isMe.value) {
    getMe().then((res) => {
      form.studentId = res.data.studentId || ''
      form.phone = res.data.phone || ''
    })
  }
  editVisible.value = true
}

async function uploadAvatar(options) {
  const res = await uploadImage(options.file)
  form.avatar = res.data.url
  options.onSuccess(res)
}

async function uploadCover(options) {
  const res = await uploadImage(options.file)
  form.cover = res.data.url
  options.onSuccess(res)
}

async function onSave() {
  saving.value = true
  try {
    const res = await updateProfile({
      nickname: form.nickname,
      avatar: form.avatar,
      cover: form.cover,
      bio: form.bio,
      gender: form.gender,
      birthday: form.birthday || null,
      school: form.school,
      college: form.college,
      studentId: form.studentId,
      phone: form.phone,
      interests: form.interests
    })
    ElMessage.success('保存成功')
    editVisible.value = false
    if (userStore.user && res.data) {
      userStore.user.nickname = res.data.nickname
      localStorage.setItem('user', JSON.stringify(userStore.user))
    }
    load()
  } finally {
    saving.value = false
  }
}

function formatTime(t) {
  return t ? t.replace('T', ' ').substring(0, 16) : ''
}

async function onFollow() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  const res = await followUser(userId.value)
  profile.value.following = res.data.following
  profile.value.followerCount = res.data.followerCount
  profile.value.followingCount = res.data.followingCount
  ElMessage.success(res.data.following ? '已关注' : '已取消关注')
}

function onMessage() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  router.push(`/chat?with=${userId.value}`)
}

async function onOpenStore() {
  if (!storeForm.name.trim()) {
    ElMessage.warning('请输入店铺名')
    return
  }
  openingStore.value = true
  try {
    await openStore({ name: storeForm.name, announcement: storeForm.announcement })
    ElMessage.success('开店成功，现在可以发布商品了')
    storeDialogVisible.value = false
    if (userStore.user) {
      userStore.user.role = 'MERCHANT'
      localStorage.setItem('user', JSON.stringify(userStore.user))
    }
    load()
  } finally {
    openingStore.value = false
  }
}

watch(() => route.params.id, load)
onMounted(load)
</script>

<style scoped>
.profile-header {
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}
.cover {
  height: 200px;
  background: linear-gradient(135deg, #4f6ef7 0%, #7b5cf0 55%, #b06ef0 100%);
  background-size: cover;
  background-position: center;
}
.header-content {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  padding: 0 16px 20px;
}
.avatar {
  margin-top: -44px;
  border: 4px solid #fff;
  background: #4f6ef7;
  color: #fff;
  font-size: 32px;
  flex-shrink: 0;
}
.info {
  flex: 1;
  min-width: 0;
  padding-top: 6px;
}
.name-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.name {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}
.username {
  color: #909399;
  font-size: 14px;
  margin-top: 2px;
}
.school {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #606266;
  font-size: 14px;
  margin-top: 8px;
}
.bio {
  color: #303133;
  font-size: 14px;
  margin-top: 10px;
  line-height: 1.6;
  white-space: pre-wrap;
  max-width: 560px;
}
.empty-bio {
  color: #c0c4cc;
}
.tags {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  flex-wrap: wrap;
}
.stats-line {
  display: flex;
  gap: 22px;
  margin-top: 14px;
  font-size: 14px;
  color: #606266;
}
.stats-line b {
  color: #303133;
}
.merchant-nav {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
}
.post-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.post-item {
  background: #fff;
  border-radius: 10px;
  padding: 14px;
}
.post-content {
  color: #303133;
  line-height: 1.7;
  white-space: pre-wrap;
}
.post-topics {
  display: flex;
  gap: 8px;
  margin-top: 10px;
  flex-wrap: wrap;
}
.post-time {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 10px;
}
@media (max-width: 640px) {
  .cover {
    height: 140px;
  }
  .header-content {
    flex-direction: column;
    gap: 12px;
  }
  .avatar {
    margin-top: -32px;
  }
  .rating-box {
    align-self: stretch;
  }
}
.rating-box {
  text-align: center;
  background: #f8f9fb;
  border-radius: 12px;
  padding: 16px 24px;
  flex-shrink: 0;
}
.rating-box .score {
  font-size: 30px;
  font-weight: 700;
  color: #f56c6c;
}
.rating-box .count {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.body {
  padding: 20px 16px 48px;
}
.prod-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 14px;
}
.prod-card {
  cursor: pointer;
  border-radius: 10px;
  overflow: hidden;
}
.prod-img {
  width: 100%;
  height: 150px;
  display: block;
}
.img-fallback {
  height: 150px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
  color: #c0c4cc;
}
.prod-body {
  padding: 10px;
}
.prod-title {
  font-size: 13px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.prod-price {
  color: #f56c6c;
  font-weight: 700;
  margin-top: 6px;
}
.review-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.review-item {
  display: flex;
  gap: 12px;
  padding: 14px;
  background: #fff;
  border-radius: 10px;
}
.review-main {
  flex: 1;
}
.review-head {
  display: flex;
  align-items: center;
  gap: 12px;
}
.review-name {
  font-weight: 600;
  color: #303133;
}
.review-product {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.review-content {
  color: #606266;
  margin-top: 8px;
  line-height: 1.6;
}
.review-time {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 8px;
}
.edit-avatar {
  background: #4f6ef7;
  color: #fff;
  cursor: pointer;
}
.upload-hint {
  font-size: 12px;
  color: #909399;
  margin-left: 10px;
}
.cover-upload {
  width: 100%;
  height: 80px;
  border-radius: 8px;
  background: linear-gradient(135deg, #4f6ef7 0%, #7b5cf0 100%);
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 13px;
  cursor: pointer;
}
</style>
