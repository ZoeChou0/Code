<template>
  <el-table :data="providers" stripe style="width: 100%" v-loading="loading">
    <el-table-column prop="id" label="ID" width="80" />
    <el-table-column prop="name" label="服务商名称" min-width="150" />
    <el-table-column prop="email" label="邮箱" min-width="180" />
    <el-table-column prop="phone" label="电话" min-width="130" />
    <el-table-column prop="qualificationStatus" label="资质状态" width="120">
      <template #default="{ row }">
        <el-tag :type="getStatusTagType(row.qualificationStatus)">
          {{ formatStatus(row.qualificationStatus) }}
        </el-tag>
      </template>
    </el-table-column>
    <el-table-column prop="addressLine1" label="地址" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
            {{ formatAddress(row) }}
        </template>
    </el-table-column>
    <el-table-column label="注册时间" width="170">
      <template #default="{ row }">
        {{ formatDateTime(row.createdAt) }}
      </template>
    </el-table-column>
    <el-table-column label="操作" fixed="right" width="200"> <template #default="{ row }">
        <el-button
          v-if="row.qualificationStatus === 'PENDING_REVIEW'"
          type="success"
          size="small"
          @click="$emit('approve', row)"
        >
          批准
        </el-button>
        <el-button
          v-if="row.qualificationStatus === 'PENDING_REVIEW'"
          type="danger"
          size="small"
          @click="$emit('reject', row)"
        >
          拒绝
        </el-button>
        <el-button size="small" @click="viewProviderDetails(row)" style="margin-left: 5px;">查看详情</el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup lang="ts">
import { defineProps, defineEmits } from 'vue'; // Removed 'reactive' if not used for actionLoadingStates here
import { ElTable, ElTableColumn, ElButton, ElTag, ElMessage } from 'element-plus';
import type { ProviderDTO } from '@/api/admin';
// import { useRouter } from 'vue-router'; // Uncomment if viewProviderDetails uses router

defineProps<{
  providers: ProviderDTO[];
  loading: boolean;
  statusToShow: 'PENDING_REVIEW' | 'ALL' | 'APPROVED' | 'REJECTED';
}>();

defineEmits(['approve', 'reject']);

// const router = useRouter(); // Uncomment if viewProviderDetails uses router

const formatDateTime = (dateTimeStr: string | undefined): string => {
  if (!dateTimeStr) return '-';
  try {
    return new Date(dateTimeStr).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' });
  } catch (e) {
    return dateTimeStr;
  }
};

const statusMap: Record<string, { text: string; type: 'success' | 'warning' | 'info' | 'danger' | 'primary' }> = {
  'PENDING_REVIEW': { text: '待审核', type: 'warning' },
  'APPROVED': { text: '已批准', type: 'success' },
  'REJECTED': { text: '已拒绝', type: 'danger' },
};

const formatStatus = (statusKey: string | undefined): string => {
  if (!statusKey) return '未知';
  return statusMap[statusKey]?.text || statusKey;
};

const getStatusTagType = (statusKey: string | undefined): 'success' | 'warning' | 'info' | 'danger' | 'primary' => {
  if (!statusKey) return 'info';
  return statusMap[statusKey]?.type || 'info';
};

const formatAddress = (provider: ProviderDTO): string => {
    let addressParts: string[] = [];
    if (provider.addressLine1) addressParts.push(provider.addressLine1);
    if (provider.city) addressParts.push(provider.city);
    if (provider.state) addressParts.push(provider.state); // Assuming ProviderDTO has state
    if (provider.zipCode) addressParts.push(provider.zipCode); // Assuming ProviderDTO has zipCode
    return addressParts.join(', ').trim() || 'N/A';
};

const viewProviderDetails = (provider: ProviderDTO) => {
  console.log('查看服务商详情: ', provider);
  ElMessage.info(`查看服务商 ${provider.name} 的详情 (功能待实现)`);
  // Example: router.push({ name: 'AdminProviderDetailsPageName', params: { id: provider.id } });
};
</script>

<style scoped>
/* 可根据需要添加样式 */
</style>