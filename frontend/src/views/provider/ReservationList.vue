<template>
  <div class="provider-reservation-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span><el-icon><Calendar /></el-icon> 收到的预约</span>
          <el-select
            v-model="filterStatus"
            placeholder="筛选状态"
            clearable
            @change="fetchReservations"
            style="width: 200px"
          >
            <el-option label="全部状态" value="ALL" />
            <el-option label="待确认 (Pending)" value="PENDING_CONFIRMATION" />
            <el-option label="已确认 (Confirmed)" value="CONFIRMED" />
            <el-option label="待支付 (Awaiting Payment)" value="AWAITING_PAYMENT" />
            <el-option label="已支付/待服务 (Paid)" value="PAID" />
            <el-option label="已完成 (Completed)" value="COMPLETED" />
            <el-option label="用户取消 (Cancelled by User)" value="CANCELLED_USER" />
            <el-option label="服务商取消 (Cancelled by Provider)" value="CANCELLED_PROVIDER" />
            <el-option label="已拒绝 (Rejected)" value="REJECTED" />
            </el-select>
        </div>
      </template>

      <el-table :data="reservations" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userName" label="客户名称" width="120" />
        <el-table-column prop="petName" label="宠物名称" width="120" />
        <el-table-column prop="serviceName" label="服务名称" width="180" />
        <el-table-column label="预约日期" width="140">
          <template #default="{ row }">
            {{ formatDate(row.reservationStartDate) }}
            <span v-if="row.reservationEndDate && row.reservationEndDate !== row.reservationStartDate">
              至 {{ formatDate(row.reservationEndDate) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="开始时间" width="120">
            <template #default="{ row }">
                {{ formatTime(row.serviceStartTime) }}
            </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="150">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">{{ formatStatus(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="用户备注" show-overflow-tooltip />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="canConfirm(row.status)"
              type="success"
              size="small"
              @click="handleConfirm(row)"
              :loading="row.loadingConfirm"
            >
              确认
            </el-button>
            <el-button
              v-if="canReject(row.status)"
              type="danger"
              size="small"
              @click="handleReject(row)"
              :loading="row.loadingReject"
            >
              拒绝
            </el-button>
            <el-button
              v-if="canComplete(row.status)"
              type="primary"
              size="small"
              @click="handleComplete(row)"
              :loading="row.loadingComplete"
            >
              标记完成
            </el-button>
            </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-if="totalReservations > 0"
        class="pagination-container"
        background
        layout="prev, pager, next, total"
        :total="totalReservations"
        :current-page="currentPage"
        :page-size="pageSize"
        @current-change="handlePageChange"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import {
  getProviderReservations,
  confirmReservationByProvider,
  rejectReservationByProvider,
  completeReservationByProvider,
  type ReservationProviderView, // 确保从 API 文件导入
} from '@/api/reservation';
import type { BackendResult } from '@/types/api';
import { ElMessage, ElMessageBox, ElTag, ElTable, ElTableColumn, ElButton, ElSelect, ElOption, ElIcon, ElCard, ElPagination } from 'element-plus';
import { Calendar } from '@element-plus/icons-vue';

const reservations = ref<(ReservationProviderView & { loadingConfirm?: boolean; loadingReject?: boolean; loadingComplete?: boolean; })[]>([]);
const loading = ref(false);
const filterStatus = ref('ALL'); // 默认显示所有状态

// 分页相关
const currentPage = ref(1);
const pageSize = ref(10); // 或者从配置中获取
const totalReservations = ref(0); // 后端目前不直接支持分页，前端分页处理

// 计算当前页应显示的数据 (前端分页)
const paginatedReservations = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return reservations.value.slice(start, end);
});

const fetchReservations = async () => {
  loading.value = true;
  try {
    const params: { status?: string } = {};
    if (filterStatus.value && filterStatus.value !== 'ALL') {
      params.status = filterStatus.value;
    }
    const res: BackendResult<ReservationProviderView[]> = await getProviderReservations(params);
    if (res.code === 200 && res.data) {
      reservations.value = res.data.map(r => ({ ...r, loadingConfirm: false, loadingReject: false, loadingComplete: false }));
      totalReservations.value = res.data.length; // 更新总数以供前端分页
      currentPage.value = 1; // 重置到第一页
    } else {
      ElMessage.error(res.message || '获取预约列表失败');
    }
  } catch (error) {
    console.error('获取预约列表错误:', error);
    ElMessage.error('获取预约列表时发生错误');
  } finally {
    loading.value = false;
  }
};

const handlePageChange = (page: number) => {
  currentPage.value = page;
};


const updateReservationInList = (updatedReservation: ReservationProviderView) => {
  const index = reservations.value.findIndex(r => r.id === updatedReservation.id);
  if (index !== -1) {
    // 合并时保留 loading 状态，因为它们不在 updatedReservation 中
    reservations.value[index] = {
        ...reservations.value[index], // 保留原有的 loading 状态
        ...updatedReservation, // 用后端返回的最新数据覆盖
    };
  }
};

const handleConfirm = async (row: ReservationProviderView & { loadingConfirm?: boolean }) => {
  row.loadingConfirm = true;
  try {
    const res = await confirmReservationByProvider(row.id);
    if (res.code === 200 && res.data) {
      updateReservationInList(res.data);
      ElMessage.success('预约已确认');
    } else {
      ElMessage.error(res.message || '确认预约失败');
    }
  } catch (error) {
    ElMessage.error('确认预约操作失败');
  } finally {
    row.loadingConfirm = false;
  }
};

const handleReject = async (row: ReservationProviderView & { loadingReject?: boolean }) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝原因（可选）', '拒绝预约', {
      confirmButtonText: '确定拒绝',
      cancelButtonText: '取消',
      inputPlaceholder: '拒绝原因',
    });
    // 如果用户点击取消，value 会是 null 或 undefined，如果输入为空则为空字符串
    // 我们允许空字符串作为原因，后端会处理

    row.loadingReject = true;
    const res = await rejectReservationByProvider(row.id, value || undefined); // 如果 value 是空字符串或 null，传 undefined
    if (res.code === 200 && res.data) {
      updateReservationInList(res.data);
      ElMessage.success('预约已拒绝');
    } else {
      ElMessage.error(res.message || '拒绝预约失败');
    }
  } catch (action) { // 用户点击取消 "catch" 捕获的是 'cancel' 或 'close'
    if (action !== 'confirm') {
        ElMessage.info('操作已取消');
    } else {
        // 这是ElMessageBox.prompt内部错误，理论上不应发生，除非API变动
        ElMessage.error('拒绝预约时发生未知错误');
    }
  } finally {
    if (row) row.loadingReject = false;
  }
};

const handleComplete = async (row: ReservationProviderView & { loadingComplete?: boolean }) => {
  row.loadingComplete = true;
  try {
    const res = await completeReservationByProvider(row.id);
    if (res.code === 200 && res.data) {
      updateReservationInList(res.data);
      ElMessage.success('预约已标记为完成');
    } else {
      ElMessage.error(res.message || '标记完成失败');
    }
  } catch (error) {
    ElMessage.error('标记完成操作失败');
  } finally {
    row.loadingComplete = false;
  }
};

// 根据状态判断是否可以执行操作
const canConfirm = (status: string) => status === 'PENDING_CONFIRMATION';
const canReject = (status: string) => status === 'PENDING_CONFIRMATION';
const canComplete = (status: string) => status === 'CONFIRMED' || status === 'PAID'; // 假设已支付/待服务的也可以标记完成

const formatDate = (dateStr: string | null | undefined) => {
  if (!dateStr) return 'N/A';
  // 后端返回的可能是 "YYYY-MM-DD" 或 ISO "YYYY-MM-DDTHH:mm:ss"
  // 如果已经是 YYYY-MM-DD，直接用。如果是完整时间，取日期部分。
  return dateStr.startsWith('0001-01-01') ? 'N/A' : dateStr.split('T')[0];
};

const formatTime = (dateTimeStr: string | null | undefined) => {
    if(!dateTimeStr) return 'N/A';
    try {
        const date = new Date(dateTimeStr);
        // 检查日期是否有效，避免显示 Invalid Date
        if (isNaN(date.getTime())) return 'N/A';
        return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: false });
    } catch (e) {
        return 'N/A';
    }
};

const statusMap: Record<string, { text: string; type: 'success' | 'info' | 'warning' | 'danger' | '' }> = {
  PENDING_CONFIRMATION: { text: '待确认', type: 'warning' },
  CONFIRMED: { text: '已确认', type: 'success' },
  AWAITING_PAYMENT: { text: '待支付', type: 'info' },
  PAID: { text: '已支付/待服务', type: 'primary' as any }, // el-tag type 'primary' is valid
  COMPLETED: { text: '已完成', type: 'success' },
  CANCELLED_USER: { text: '用户已取消', type: 'info' },
  CANCELLED_PROVIDER: { text: '服务商已取消', type: 'info' },
  REJECTED: { text: '已拒绝', type: 'danger' },
  // 根据需要添加更多状态
};

const formatStatus = (status: string) => {
  return statusMap[status]?.text || status;
};

const getStatusTagType = (status: string) => {
  return statusMap[status]?.type || 'info';
};

onMounted(() => {
  fetchReservations();
});
</script>

<style scoped>
.provider-reservation-list {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-header span {
    display: flex;
    align-items: center;
    font-size: 18px;
    font-weight: 500;
}
.card-header .el-icon {
    margin-right: 8px;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>