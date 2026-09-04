<template>
  <div class="community">
    <div class="container">
      <div class="composer" v-if="userStore.isLoggedIn">
        <el-input
          v-model="draft.content"
          type="textarea"
          :rows="3"
          maxlength="2000"
          show-word-limit
          placeholder="分享你的想法、经验、或者闲置好物～"
        />
        <div v-if="mediaList.length" class="media-preview">
          <div v-for="(m, i) in mediaList" :key="i" class="media-item">
            <el-image v-if="m.type === 'IMAGE'" :src="m.url" fit="cover" class="thumb" />
            <div v-else class="thumb video-thumb"><el-icon><VideoPlay /></el-icon></div>
            <el-icon class="remove" @click="removeMedia(i)"><CircleClose /></el-icon>
          </div>
        </div>
        <div class="composer-bar">
          <el-input v-model="draft.topics" placeholder="话题标签，用逗号分隔，如：考研, 篮球" class="topics" size="small" />
          <div class="composer-actions">
            <el-upload :show-file-list="false" :http-request="doUploadMedia" accept="image/*,video/*" multiple>
              <el-button size="small"><el-icon><Plus /></el-icon>&nbsp;图片/视频</el-button>
            </el-upload>
            <el-button type="primary" size="small" :loading="publishing" @click="onPublish">发布</el-button>
          </div>
        </div>
      </div>
      <div class="composer-guest" v-else>
        <el-alert title="登录后即可发帖、点赞、评论" type="info" :closable="false" show-icon>
          <el-button type="primary" size="small" @click="$router.push('/login')">去登录</el-button>
        </el-alert>
      </div>

      <div v-loading="loading" class="feed">
        <div v-for="post in posts" :key="post.id" class="post-card">
          <div class="post-head">
            <el-avatar :size="40" :src="post.authorAvatar" class="avatar" @click="goProfile(post.userId)">
              {{ (post.authorName || 'U')[0] }}
            </el-avatar>
            <div class="author" @click="goProfile(post.userId)">
              <div class="name">{{ post.authorName }}</div>
              <div class="time">{{ formatTime(post.createdAt) }}</div>
            </div>
            <el-dropdown v-if="post.mine" @command="(c) => c === 'del' && onDeletePost(post)">
              <el-icon class="more"><MoreFilled /></el-icon>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="del">删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>

          <div class="post-content">{{ post.content }}</div>

          <div v-if="post.topics.length" class="topics">
            <el-tag v-for="t in post.topics" :key="t" size="small" round effect="plain"># {{ t }}</el-tag>
          </div>

          <div v-if="post.media.length" class="media">
            <div v-for="m in post.media" :key="m.url" class="media-block">
              <el-image v-if="m.type === 'IMAGE'" :src="m.url" fit="cover" class="post-img" :preview-src-list="[m.url]" />
              <video v-else :src="m.url" controls preload="metadata" class="post-video"></video>
            </div>
          </div>

          <div class="post-foot">
            <span class="action" :class="{ liked: post.liked }" @click="onLike(post)">
              <span class="icon">{{ post.liked ? '❤️' : '🤍' }}</span> {{ post.likeCount }}
            </span>
            <span class="action" @click="openComments(post)">
              <span class="icon">💬</span> {{ post.commentCount }}
            </span>
          </div>
        </div>

        <el-empty v-if="!loading && posts.length === 0" description="还没有帖子，来发第一条吧" :image-size="120" />
      </div>

      <div v-if="total > size" class="pager">
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="size"
          :current-page="page + 1" @current-change="onPageChange" />
      </div>
    </div>

    <el-drawer v-model="drawerVisible" title="评论" size="420px">
      <div v-if="currentPost" class="drawer-body">
        <div class="drawer-post">{{ currentPost.content }}</div>
        <div class="comments">
          <div v-for="c in comments" :key="c.id" class="comment-item">
            <el-avatar :size="32" :src="c.userAvatar">{{ (c.userName || 'U')[0] }}</el-avatar>
            <div class="comment-main">
              <div class="comment-head">
                <span class="comment-name">{{ c.userName }}</span>
                <span class="comment-time">{{ formatTime(c.createdAt) }}</span>
                <el-icon v-if="c.mine" class="comment-del" @click="onDeleteComment(c)"><Delete /></el-icon>
              </div>
              <div class="comment-content">{{ c.content }}</div>
            </div>
          </div>
          <el-empty v-if="comments.length === 0" description="还没有评论" :image-size="80" />
        </div>
        <div class="comment-input">
          <el-input v-model="commentText" placeholder="写下你的评论…" @keyup.enter="onAddComment" />
          <el-button type="primary" :loading="submittingComment" @click="onAddComment">发送</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, VideoPlay, Plus, Delete, CircleClose, MoreFilled } from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'
import { getPosts, createPost, deletePost, likePost, getComments, addComment, deleteComment } from '../api/community'
import { uploadImage, uploadVideo } from '../api/upload'

const router = useRouter()
const userStore = useUserStore()

const posts = ref([])
const total = ref(0)
const page = ref(0)
const size = 10
const loading = ref(false)

const draft = reactive({ content: '', topics: '' })
const mediaList = ref([])
const publishing = ref(false)

const drawerVisible = ref(false)
const currentPost = ref(null)
const comments = ref([])
const commentText = ref('')
const submittingComment = ref(false)

async function load() {
  loading.value = true
  try {
    const res = await getPosts({ page: page.value, size })
    posts.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function doUploadMedia(options) {
  const file = options.file
  const isVideo = file.type && file.type.startsWith('video/')
  try {
    const res = isVideo ? await uploadVideo(file) : await uploadImage(file)
    mediaList.value.push({ url: res.data.url, type: isVideo ? 'VIDEO' : 'IMAGE' })
    options.onSuccess(res)
  } catch (e) {
    options.onError(e)
  }
}

function removeMedia(i) {
  mediaList.value.splice(i, 1)
}

async function onPublish() {
  if (!draft.content.trim()) {
    ElMessage.warning('写点什么吧')
    return
  }
  publishing.value = true
  try {
    const topics = draft.topics.split(/[,，]/).map((s) => s.trim()).filter(Boolean)
    await createPost({
      content: draft.content,
      topics: topics.length ? topics : null,
      media: mediaList.value
    })
    ElMessage.success('发布成功')
    draft.content = ''
    draft.topics = ''
    mediaList.value = []
    page.value = 0
    load()
  } finally {
    publishing.value = false
  }
}

async function onLike(post) {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  const res = await likePost(post.id)
  post.liked = res.data.liked
  post.likeCount = res.data.likeCount
}

async function openComments(post) {
  currentPost.value = post
  commentText.value = ''
  drawerVisible.value = true
  const res = await getComments(post.id)
  comments.value = res.data
}

async function onAddComment() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  if (!commentText.value.trim()) return
  submittingComment.value = true
  try {
    const res = await addComment(currentPost.value.id, commentText.value)
    comments.value.push(res.data)
    currentPost.value.commentCount++
    commentText.value = ''
  } finally {
    submittingComment.value = false
  }
}

async function onDeletePost(post) {
  await ElMessageBox.confirm('确定删除这条帖子吗？', '提示', { type: 'warning' })
  await deletePost(post.id)
  ElMessage.success('已删除')
  load()
}

async function onDeleteComment(c) {
  await deleteComment(c.id)
  comments.value = comments.value.filter((x) => x.id !== c.id)
  currentPost.value.commentCount--
}

function goProfile(id) {
  router.push(`/user/${id}`)
}

function formatTime(t) {
  return t ? t.replace('T', ' ').substring(0, 16) : ''
}

function onPageChange(p) {
  page.value = p - 1
  load()
}

onMounted(load)
</script>

<style scoped>
.container {
  max-width: 720px;
  margin: 0 auto;
  padding: 24px 16px 48px;
}
.composer {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 20px;
}
.media-preview {
  display: flex;
  gap: 8px;
  margin-top: 10px;
  flex-wrap: wrap;
}
.media-item {
  position: relative;
}
.thumb {
  width: 72px;
  height: 72px;
  border-radius: 8px;
  background: #f0f2f5;
}
.video-thumb {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 24px;
}
.remove {
  position: absolute;
  top: -6px;
  right: -6px;
  color: #f56c6c;
  background: #fff;
  border-radius: 50%;
  cursor: pointer;
}
.composer-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 12px;
}
.topics {
  flex: 1;
}
.composer-actions {
  display: flex;
  gap: 8px;
}
.composer-guest {
  margin-bottom: 20px;
}
.feed {
  min-height: 200px;
}
.post-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
}
.post-head {
  display: flex;
  align-items: center;
  gap: 10px;
}
.avatar {
  background: #4f6ef7;
  color: #fff;
  cursor: pointer;
}
.author {
  flex: 1;
  cursor: pointer;
}
.name {
  font-weight: 600;
  color: #303133;
}
.time {
  font-size: 12px;
  color: #c0c4cc;
}
.more {
  color: #909399;
  cursor: pointer;
}
.post-content {
  margin-top: 12px;
  color: #303133;
  line-height: 1.7;
  white-space: pre-wrap;
}
.topics {
  display: flex;
  gap: 8px;
  margin-top: 10px;
  flex-wrap: wrap;
}
.media {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  flex-wrap: wrap;
}
.media-block {
  max-width: 100%;
}
.post-img {
  width: 200px;
  height: 160px;
  border-radius: 8px;
  display: block;
}
.post-video {
  width: 280px;
  border-radius: 8px;
  background: #000;
}
.post-foot {
  display: flex;
  gap: 24px;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid #f5f6f8;
}
.action {
  cursor: pointer;
  color: #606266;
  font-size: 14px;
  user-select: none;
}
.action.liked {
  color: #f56c6c;
}
.icon {
  margin-right: 2px;
}
.pager {
  display: flex;
  justify-content: center;
}
.drawer-post {
  padding: 12px;
  background: #f8f9fb;
  border-radius: 8px;
  color: #303133;
  margin-bottom: 16px;
  white-space: pre-wrap;
}
.comments {
  flex: 1;
  overflow-y: auto;
}
.comment-item {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}
.comment-main {
  flex: 1;
}
.comment-head {
  display: flex;
  align-items: center;
  gap: 8px;
}
.comment-name {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}
.comment-time {
  font-size: 12px;
  color: #c0c4cc;
}
.comment-del {
  margin-left: auto;
  color: #c0c4cc;
  cursor: pointer;
}
.comment-content {
  color: #606266;
  margin-top: 4px;
  line-height: 1.6;
}
.comment-input {
  display: flex;
  gap: 8px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}
</style>
