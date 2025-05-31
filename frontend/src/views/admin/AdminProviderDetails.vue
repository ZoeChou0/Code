<template>
  <div class="provider-details-page el-container-main-custom">
    <el-page-header @back="goBack" class="page-header-custom">
      <template #content>
        <span class="text-large font-600 mr-3"> 服务商详情 </span>
      </template>
    </el-page-header>

    <el-card v-if="provider" shadow="never" class="details-card">
      <el-descriptions :column="isMobile ? 1 : 2" border title="基本信息">
        <el-descriptions-item label="ID" label-align="right" align="left">{{ provider.id }}</el-descriptions-item>
        <el-descriptions-item label="名称" label-align="right" align="left">{{ provider.name }}</el-descriptions-item>
        <el-descriptions-item label="邮箱" label-align="right" align="left">{{ provider.email }}</el-descriptions-item>
        <el-descriptions-item label="手机号" label-align="right" align="left">{{ provider.phone }}</el-descriptions-item>
        <el-descriptions-item label="角色" label-align="right" align="left">
          <el-tag size="small">{{ provider.role === 'provider' ? '服务商' : provider.role }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="账号状态" label-align="right" align="left">
           <el-tag :type="provider.status === 'active' ? 'success' : 'danger'" size="small">
            {{ provider.status === 'active' ? '正常' : (provider.status === 'banned' ? '已禁用' : (provider.status || '未知')) }}
           </el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <el-descriptions :column="isMobile ? 1 : 2" border title="资质与地址信息" style="margin-top: 20px;">
        <el-descriptions-item label="资质状态" label-align="right" align="left">
          <el-tag :type="getQualificationStatusTag(provider.qualificationStatus)" size="small">
            {{ formatQualificationStatus(provider.qualificationStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item 
          label="拒绝原因" 
          v-if="provider.qualificationStatus === 'REJECTED' && provider.qualificationRejectionReason" 
          label-align="right" 
          align="left" 
          :span="isMobile ? 1 : (provider.qualificationStatus === 'REJECTED' ? 1 : 2)"
        >
          {{ provider.qualificationRejectionReason }}
        </el-descriptions-item>
         <el-descriptions-item label="生日" label-align="right" align="left" v-if="provider.birthday">{{ provider.birthday }}</el-descriptions-item>

        <el-descriptions-item label="地址" label-align="right" align="left" :span="isMobile || !(provider.qualificationStatus === 'REJECTED' && provider.qualificationRejectionReason && provider.birthday) ? 2 : 1">{{ getFullAddress(provider) }}</el-descriptions-item>
      </el-descriptions>

       <el-descriptions :column="isMobile ? 1 : 2" border title="时间信息" style="margin-top: 20px;">
        <el-descriptions-item label="注册时间" label-align="right" align="left">{{ formatDateTime(provider.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="最后更新时间" label-align="right" align="left">{{ formatDateTime(provider.updatedAt) }}</el-descriptions-item>
      </el-descriptions>

      </el-card>
    <div v-if="loading && !provider" class="loading-container"> {/* 修改 v-if 条件，仅在 provider 为 null 时显示骨架屏 */}
      <el-skeleton :rows="10" animated />
    </div>
    <el-alert
      v-if="error && !loading"
      :title="error"
      type="error"
      show-icon
      :closable="false"
      style="margin-top: 20px;"
    />
    <el-empty v-if="!loading && !provider && !error" description="未找到服务商信息" /> {/* 新增：未找到服务商时的提示 */}
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'; // 引入 onUnmounted
import { useRoute, useRouter } from 'vue-router';
import { ElCard, ElDescriptions, ElDescriptionsItem, ElTag, ElMessage, ElSkeleton, ElAlert, ElPageHeader, ElEmpty } from 'element-plus';
import { getProviderById, type ProviderDTO } from '@/api/admin';

const route = useRoute();
const router = useRouter();

const provider = ref<ProviderDTO | null>(null);
const loading = ref(true);
const error = ref<string | null>(null);

const windowWidth = ref(window.innerWidth);
const isMobile = computed(() => windowWidth.value < 768);
const updateWidth = () => windowWidth.value = window.innerWidth;

onMounted(async () => {
  window.addEventListener('resize', updateWidth);
  const idParam = route.params.id;
  if (typeof idParam === 'string') {
    const providerId = parseInt(idParam, 10);
    if (!isNaN(providerId)) {
      await fetchProviderDetails(providerId);
    } else {
      handleError('无效的服务商ID参数。');
    }
  } else {
    handleError('未能在路由参数中找到服务商ID。');
  }
});

onUnmounted(() => { // 添加 onUnmounted
  window.removeEventListener('resize', updateWidth);
});


const fetchProviderDetails = async (id: number) => {
  loading.value = true;
  error.value = null;
  provider.value = null; // 重置 provider
  try {
    const res = await getProviderById(id);
    if (res.code === 200 && res.data) {
      provider.value = res.data;
    } else {
      // 如果 res.data 为空，也视为一种错误或未找到
      const message = res.message || (res.data === null ? '未找到指定的服务商信息' : '获取服务商详情失败');
      throw new Error(message);
    }
  } catch (err: any) {
    console.error('获取服务商详情失败:', err);
    handleError(err.message || '加载服务商数据时发生网络错误。');
  } finally {
    loading.value = false;
  }
};

const handleError = (message: string) => {
    error.value = message;
    loading.value = false; // 确保在出错时也停止加载
    // ElMessage.error(message); // 错误信息通过 ElAlert 展示，避免重复
}

const goBack = () => {
  router.push({ name: 'AdminProviderList' }); // 假设服务商列表路由名称
};

const getFullAddress = (p: ProviderDTO | null) => {
  if (!p) return 'N/A';
  const addressParts = [p.addressLine1, p.city, p.state, p.zipCode].filter(val => val && String(val).trim() !== '');
  return addressParts.length > 0 ? addressParts.join(', ') : '未提供地址';
};

const formatQualificationStatus = (status: string | undefined): string => {
  if (!status) return '未知';
  if (status === 'PENDING_REVIEW') return '待审核';
  if (status === 'APPROVED') return '已认证';
  if (status === 'REJECTED') return '已拒绝';
  return status;
};

const getQualificationStatusTag = (status: string | undefined): ('warning' | 'success' | 'danger' | 'info') => {
  if (!status) return 'info';
  if (status === 'PENDING_REVIEW') return 'warning';
  if (status === 'APPROVED') return 'success';
  if (status === 'REJECTED') return 'danger';
  return 'info';
};

const formatDateTime = (dateTimeString?: string): string => {
    if (!dateTimeString) return 'N/A';
    try {
        const date = new Date(dateTimeString);
        if (isNaN(date.getTime())) return dateTimeString;
        return date.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit' });
    } catch (e) {
        return dateTimeString;
    }
};

</script>

<style scoped lang="scss">
.provider-details-page {
  padding: 16px;
}
.page-header-custom {
    margin-bottom: 16px;
    background-color: #fff;
    padding: 16px 24px;
    border-radius: 4px;
    box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}
.details-card {
  border: none;
  :deep(.el-card__header) {
    font-size: 1.1rem;
    font-weight: 600;
  }
   :deep(.el-descriptions__label.is-bordered-label) {
    background-color: #fafcff;
    font-weight: 500;
    width: 120px; 
    @media (max-width: 768px) { // 移动端标签宽度
        width: 100px;
    }
  }
}
.loading-container {
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
}
</style>