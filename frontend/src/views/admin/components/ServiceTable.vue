<template>
  <el-table :data="services" :loading="loading" style="width: 100%">
    <el-table-column prop="id" label="ID" width="80" sortable />
    <el-table-column prop="name" label="服务名称" width="200" show-overflow-tooltip />
    <el-table-column prop="providerId" label="服务商ID" width="110" />
    <el-table-column prop="price" label="价格(元)" width="100" sortable>
         <template #default="{ row }">
            {{ row.price?.toFixed(2) }}
         </template>
    </el-table-column>
    <el-table-column prop="duration" label="时长(分钟)" width="120" sortable />
     <el-table-column prop="reviewStatus" label="审核状态" width="120">
       <template #default="{ row }">
         <el-tag :type="getStatusTagType(row.reviewStatus)">
           {{ formatStatus(row.reviewStatus) }}
         </el-tag>
       </template>
    </el-table-column>
     <el-table-column prop="rejectionReason" label="拒绝原因" show-overflow-tooltip />
    <el-table-column label="操作" width="180" align="center" fixed="right">
      <template #default="{ row }">
        <template v-if="row.reviewStatus === 'PENDING'">
          <el-button size="small" type="success" :icon="CircleCheck" @click="emitApprove(row.id)">批准</el-button>
          <el-button size="small" type="danger" :icon="CircleClose" @click="emitReject(row.id)">拒绝</el-button>
        </template>
        <span v-else>无待处理操作</span>
        </template>
    </el-table-column>
  </el-table>
</template>

<script setup lang="ts">
import { defineProps, defineEmits } from 'vue';
import { ElTable, ElTableColumn, ElButton, ElTag, ElIcon } from 'element-plus';
import { CircleCheck, CircleClose, View } from '@element-plus/icons-vue';
import type { Service } from '@/api/service'; // 导入 Service 类型

// 定义接收的 props
interface Props {
  services: Service[];
  loading: boolean;
}
const props = defineProps<Props>();

// 定义要触发的事件
const emit = defineEmits(['approve', 'reject']);

// 触发批准事件
const emitApprove = (serviceId: number) => {
  emit('approve', serviceId);
};

// 触发拒绝事件
const emitReject = (serviceId: number) => {
  emit('reject', serviceId);
};

// --- 格式化函数 ---
const formatStatus = (status: string | undefined): string => {
  if (!status) return '未知';
  switch (status) {
    case 'PENDING': return '待审核';
    case 'APPROVED': return '已批准';
    case 'REJECTED': return '已拒绝';
    default: return status;
  }
};

const getStatusTagType = (status: string | undefined): ('warning' | 'success' | 'danger' | 'info') => {
  if (!status) return 'info';
  switch (status) {
    case 'PENDING': return 'warning';
    case 'APPROVED': return 'success';
    case 'REJECTED': return 'danger';
    default: return 'info';
  }
};
</script>

<style scoped>
.el-button + .el-button {
    margin-left: 8px;
}
</style>