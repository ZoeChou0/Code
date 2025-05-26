<template>
  <div class="service-detail" v-loading="loading">
    <template v-if="service">
      <div class="service-header">
        <h1>{{ service.name }}</h1>
        <div class="service-price">
          <span class="price">¥{{ service.price }}</span>
          <span class="duration">/{{ service.duration }}分钟</span>
        </div>
      </div>

      <div class="service-info">
        <div class="provider-info">
          <h3>服务商信息</h3>
          <p>{{ service.providerName }}</p>
        </div>

        <div class="service-description">
          <h3>服务描述</h3>
          <p>{{ service.description }}</p>
        </div>

        <div class="service-requirements" v-if="hasRequirements">
          <h3>服务要求</h3>
          <div class="requirement-tags">
            <el-tag v-if="service.requiredVaccinations" type="warning" effect="light">
              <el-icon><FirstAidKit /></el-icon>
              疫苗要求: {{ service.requiredVaccinations }}
            </el-tag>
            <el-tag v-if="service.requiresNeutered !== null" :type="service.requiresNeutered ? 'danger' : 'success'" effect="light">
              <el-icon><Link /></el-icon>
              {{ service.requiresNeutered ? '需绝育' : '不限绝育' }}
            </el-tag>
          </div>
        </div>

        <div class="service-capacity">
          <h3>服务容量</h3>
          <p>每日可预约数量：{{ service.dailyCapacity }}个</p>
        </div>
      </div>

      <div class="service-actions">
        <el-button type="primary" size="large" @click="handleReserve" :disabled="!canReserve">
          立即预约
        </el-button>
      </div>

      <div class="service-reviews">
        <h2>用户评价</h2>
        <ReviewList :service-item-id="service.id" />
      </div>
    </template>
    <template v-else>
      <el-empty description="服务不存在或已被删除" />
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { FirstAidKit, Link } from '@element-plus/icons-vue'
import { getServiceById } from '@/api/service'
import type { Service } from '@/api/service'
import ReviewList from '@/components/ReviewList.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const service = ref<Service | null>(null)

const hasRequirements = computed(() => {
  if (!service.value) return false
  return service.value.requiredVaccinations || 
         service.value.requiresNeutered !== null ||
         service.value.temperamentRequirements ||
         service.value.prohibitedBreeds
})

const canReserve = computed(() => {
  if (!service.value) return false
  return service.value.reviewStatus === 'APPROVED'
})

const fetchServiceDetail = async () => {
  const serviceId = Number(route.params.id)
  if (!serviceId) {
    ElMessage.error('服务ID无效')
    router.push('/services')
    return
  }

  loading.value = true
  try {
    const res = await getServiceById(serviceId)
    if (res.code === 200 && res.data) {
      service.value = res.data
    } else {
      ElMessage.error(res.message || '获取服务详情失败')
      router.push('/services')
    }
  } catch (error: any) {
    console.error('获取服务详情失败:', error)
    ElMessage.error(error.message || '获取服务详情失败，请稍后重试')
    router.push('/services')
  } finally {
    loading.value = false
  }
}

const handleReserve = () => {
  const serviceId = Number(route.params.id)
  if (!serviceId) {
    ElMessage.error('服务ID无效')
    router.push('/services')
    return
  }

  if (!userStore.token || !userStore.userInfo) {
    ElMessage.warning('请先登录后再预约服务')
    router.push({
      path: '/login',
      query: { redirect: `/services/${serviceId}` }
    })
    return
  }

  if (userStore.userInfo.role !== 'user') {
    ElMessage.warning('只有普通用户才能预约服务')
    return
  }

  router.push({
    path: '/reservations/create',
    query: { serviceId: serviceId }
  })
}

onMounted(() => {
  fetchServiceDetail()
})
</script>

<style scoped>
.service-detail {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.service-header {
  margin-bottom: 30px;
}

.service-header h1 {
  font-size: 28px;
  color: #303133;
  margin-bottom: 10px;
}

.service-price {
  font-size: 20px;
  color: #f56c6c;
}

.service-price .duration {
  font-size: 14px;
  color: #909399;
  margin-left: 5px;
}

.service-info {
  margin-bottom: 30px;
}

.service-info h3 {
  font-size: 18px;
  color: #303133;
  margin-bottom: 10px;
}

.service-info p {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 20px;
}

.requirement-tags {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.service-actions {
  margin: 30px 0;
  text-align: center;
}

.service-reviews {
  margin-top: 40px;
}

.service-reviews h2 {
  font-size: 24px;
  color: #303133;
  margin-bottom: 20px;
}
</style> 