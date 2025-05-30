<template>
  <div class="service-detail-page" v-if="!loading && service">
    <el-card>
      <template #header>
        <div class="card-header">
          <h1>{{ service.name }}</h1>
          <el-tag type="success" size="large" effect="dark">
            ¥{{ service.price?.toFixed(2) ?? '价格待议' }} / {{ service.duration ? service.duration + '分钟' : '次' }}
          </el-tag>
        </div>
      </template>

      <el-descriptions :column="2" border class="service-meta">
        <el-descriptions-item label="服务商">
          <div v-if="service.providerName" class="provider-info">
            <el-avatar :size="24" :src="service.providerProfilePhotoUrl || undefined" :icon="!service.providerProfilePhotoUrl ? ElUser : undefined" style="margin-right: 8px;"/>
            <span>{{ service.providerName }} (ID: {{ service.providerId }})</span>
          </div>
          <span v-else>服务商ID: {{ service.providerId }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="服务商地点">
          {{ service.providerCity || service.providerAddressLine1 || '未知地点' }}
        </el-descriptions-item>
        <el-descriptions-item label="服务商平均评分" v-if="service.providerAverageRating != null && service.providerAverageRating > 0">
          <el-rate
            v-model="service.providerAverageRating"
            disabled
            show-score
            text-color="#ff9900"
            score-template="{value} / 5.0"
            :max="5"
          />
        </el-descriptions-item>
         <el-descriptions-item label="服务商平均评分" v-else>
          暂无评分
        </el-descriptions-item>
        <el-descriptions-item label="每日容量" v-if="service.dailyCapacity && service.dailyCapacity > 0">
          {{ service.dailyCapacity }}
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left"><h3>服务描述</h3></el-divider>
      <p class="description-text">{{ service.description || '暂无详细描述。' }}</p>

      <el-divider content-position="left"><h3>服务要求</h3></el-divider>
      <div class="requirements-section">
        <div v-if="service.requiredVaccinations">
          <el-icon><FirstAidKit /></el-icon> <strong>要求疫苗:</strong> {{ service.requiredVaccinations }}
        </div>
        <div v-if="service.requiresNeutered != null">
          <el-icon><LinkIcon /></el-icon> <strong>是否要求绝育:</strong> {{ service.requiresNeutered ? '是' : '否' }}
        </div>
        <div v-if="service.minAge != null">
          <el-icon><TimerIcon /></el-icon> <strong>最低年龄:</strong> {{ service.minAge }} 岁
        </div>
        <div v-if="service.maxAge != null">
          <el-icon><TimerIcon /></el-icon> <strong>最高年龄:</strong> {{ service.maxAge }} 岁
        </div>
        <div v-if="service.temperamentRequirements">
          <el-icon><ChatDotRound /></el-icon> <strong>性格要求:</strong> {{ service.temperamentRequirements }}
        </div>
        <div v-if="service.prohibitedBreeds">
          <el-icon><CircleClose /></el-icon> <strong>不接受品种:</strong> {{ service.prohibitedBreeds }}
        </div>
        <p v-if="!hasAnyRequirements(service)">此服务暂无特殊要求。</p>
      </div>

      <div class="actions-footer">
        <el-button type="primary" size="large" @click="goToReservationPage" :icon="Promotion">
          立即预约此服务
        </el-button>
      </div>
    </el-card>
  </div>
  <div v-else-if="loading" class="loading-state">
    <el-skeleton :rows="10" animated />
  </div>
  <div v-else-if="loadError" class="error-state">
    <el-alert
      title="加载服务详情失败"
      type="error"
      :description="errorMessage || '无法加载服务详细信息，请稍后重试或返回上一页。'"
      show-icon
      :closable="false"
    />
  </div>
  <div v-else class="empty-state">
     <el-empty description="未找到指定的服务详情"></el-empty>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  ElCard, ElTag, ElIcon, ElEmpty, ElSkeleton, ElAlert, ElMessage, ElButton,
  ElAvatar, ElDivider, ElDescriptions, ElDescriptionsItem, ElRate
} from 'element-plus';
import {
  User as ElUser, FirstAidKit, Link as LinkIcon, Timer as TimerIcon,
  ChatDotRound, CircleClose, Promotion
} from '@element-plus/icons-vue';
import { getActiveServices } from '@/api/service'; // getActiveServices 可能需要修改或新增一个 getServiceDetailById
import type { Service as ApiServiceType } from '@/api/service';
import type { BackendResult } from '@/types/api';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const service = ref<ApiServiceType | null>(null);
const loading = ref(true);
const loadError = ref(false);
const errorMessage = ref('');

const serviceId = computed(() => route.params.id as string); // 从路由参数获取服务ID

// 【重要】您需要一个根据ID获取单个服务详情的API函数
// 假设 getActiveServices 可以通过传递ID来获取单个服务，如果不行，您需要在 api/service.ts 中新增一个
// 例如: export function getServiceDetail(id: string): Promise<BackendResult<ApiServiceType>> { ... }
const fetchServiceDetail = async () => {
  loading.value = true;
  loadError.value = false;
  errorMessage.value = '';
  if (!serviceId.value) {
    errorMessage.value = '无效的服务ID';
    loadError.value = true;
    loading.value = false;
    return;
  }

  try {
    // 假设 getActiveServices({ id: serviceId.value }) 可以获取单个服务
    // 更理想的是有一个专门的 getServiceById(serviceId.value) API
    const res: BackendResult<ApiServiceType[] | ApiServiceType> = await getActiveServices({ id: serviceId.value });

    if (res.code === 200 && res.data) {
      if (Array.isArray(res.data)) { // 如果 getActiveServices 总是返回数组
        service.value = res.data.find(s => s.id === parseInt(serviceId.value)) || null;
      } else { // 如果 getServiceById 返回单个对象
        service.value = res.data as ApiServiceType;
      }
      if (!service.value) {
        throw new Error('未找到指定的服务。');
      }
    } else {
      throw new Error(res.message || '获取服务详情失败');
    }
  } catch (error: any) {
    console.error("获取服务详情失败:", error);
    loadError.value = true;
    errorMessage.value = error.message || '加载服务详情时发生错误。';
    service.value = null;
  } finally {
    loading.value = false;
  }
};

const hasAnyRequirements = (svc: ApiServiceType | null): boolean => {
  if (!svc) return false;
  return !!(
    (svc.requiredVaccinations && svc.requiredVaccinations.trim() !== '') ||
    (svc.requiresNeutered !== null && svc.requiresNeutered !== undefined) ||
    (svc.minAge != null) ||
    (svc.maxAge != null) ||
    (svc.temperamentRequirements && svc.temperamentRequirements.trim() !== '') ||
    (svc.prohibitedBreeds && svc.prohibitedBreeds.trim() !== '')
  );
};

const goToReservationPage = () => {
  if (!service.value) return;
  if (!userStore.token) {
      ElMessage.warning('请先登录后再进行预约！');
      // 保存当前详情页路径，以便登录后跳回
      router.push({ name: 'Login', query: { redirect: route.fullPath } });
      return;
  }
  router.push({ name: 'CreateReservation', params: { serviceId: String(service.value.id) } });
};

onMounted(() => {
  fetchServiceDetail();
});
</script>

<style scoped lang="scss">
.service-detail-page {
  padding: 20px;
  max-width: 900px;
  margin: 20px auto;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  h1 {
    margin: 0;
    font-size: 1.8em;
    color: #303133;
  }
}
.service-meta {
  margin-top: 20px;
  .provider-info {
    display: flex;
    align-items: center;
  }
}
.description-text {
  font-size: 1rem;
  line-height: 1.7;
  color: #606266;
  white-space: pre-wrap; /* 保留换行和空格 */
  margin-top: 10px;
}
.requirements-section {
  margin-top: 10px;
  font-size: 0.9rem;
  color: #555;
  div {
    display: flex;
    align-items: center;
    margin-bottom: 8px;
    .el-icon {
      margin-right: 8px;
      color: var(--el-color-primary);
    }
  }
}
.actions-footer {
  margin-top: 30px;
  text-align: center;
}
.loading-state, .error-state, .empty-state {
  padding: 40px;
  text-align: center;
}
</style>