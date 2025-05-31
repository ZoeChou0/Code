<template>
  <div class="provider-order-list-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span><el-icon><Document /></el-icon> 我的服务订单</span>
          </div>
      </template>

      <el-table :data="displayedOrders" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="订单ID" width="100" />
        <el-table-column prop="reservationId" label="预约ID" width="100" />
        <el-table-column label="服务名称" min-width="180">
           <template #default="scope">
            <span>{{ scope.row.serviceName || 'N/A' }}</span>
          </template>
        </el-table-column>
         <el-table-column label="客户名称" min-width="120">
           <template #default="scope">
            <span>{{ scope.row.userName || 'N/A' }}</span> </template>
        </el-table-column>
        <el-table-column label="宠物名称" min-width="120">
          <template #default="scope">
            <span>{{ scope.row.petName || 'N/A' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="订单金额 (元)" width="130">
          <template #default="scope">
            <span>¥ {{ scope.row.amount?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getOrderStatusType(scope.row.status)">
              {{ formatOrderStatus(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="下单时间" width="180">
           <template #default="scope">
            <span>{{ formatDateTime(scope.row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="payTime" label="支付时间" width="180">
          <template #default="scope">
            <span>{{ scope.row.payTime ? formatDateTime(scope.row.payTime) : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="scope">
            <el-button
              v-if="canCompleteOrder(scope.row.status)" type="success"
              size="small"
              @click="handleCompleteOrder(scope.row.id)"
               :loading="scope.row.completing"
            >
              标记完成
            </el-button>
            </template>
        </el-table-column>
      </el-table>
        <el-pagination
        v-if="totalOrders > 0"
        layout="prev, pager, next, total, jumper"
        :total="totalOrders"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
        class="pagination-container"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'; // 确保导入 computed
import { ElMessage, ElMessageBox, ElCard, ElTable, ElTableColumn, ElTag, ElButton, ElPagination, ElIcon } from 'element-plus';
import { Document } from '@element-plus/icons-vue';
import { getProviderOrderList, completeOrder as apiCompleteOrder } from '@/api/order';
import type { BackendResult } from '@/types/api';
import type { OrderViewDTO } from '@/api/order';

interface DisplayOrder extends OrderViewDTO {
  completing?: boolean;
  userName?: string;
}

const allOrders = ref<DisplayOrder[]>([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const totalOrders = ref(0);

const displayedOrders = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return allOrders.value.slice(start, end);
});

const fetchOrders = async () => {
  loading.value = true;
  try {
    const res: BackendResult<OrderViewDTO[]> = await getProviderOrderList();
    if (res.code === 200 && res.data) {
      allOrders.value = res.data.map(order => ({ ...order, completing: false }));
      totalOrders.value = allOrders.value.length;
    } else {
      ElMessage.error(res.message || '获取订单列表失败');
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取订单列表请求失败');
  } finally {
    loading.value = false;
  }
};

const handleCompleteOrder = async (orderId: number) => {
  const order = allOrders.value.find(o => o.id === orderId);
  if (!order) return;

  try {
    await ElMessageBox.confirm('确定要将此订单标记为已完成吗？服务完成后，客户可能会对本次服务进行评价。', '确认完成服务', {
      confirmButtonText: '确定完成',
      cancelButtonText: '取消',
      type: 'info'
    });

    order.completing = true;
    const res: BackendResult<OrderViewDTO | null> = await apiCompleteOrder(orderId);
    if (res.code === 200) {
      ElMessage.success('订单已成功标记为完成！');
      if (res.data) {
          const index = allOrders.value.findIndex(o => o.id === orderId);
          if (index !== -1) {
            allOrders.value[index] = { ...allOrders.value[index], ...res.data, completing: false };
          }
      } else {
        await fetchOrders();
      }
    } else {
      ElMessage.error(res.message || '标记完成失败');
    }
  } catch (rejectionReason: any) {
    if (rejectionReason === 'cancel' || rejectionReason === 'close') {
      ElMessage.info('操作已取消');
    } else if (rejectionReason instanceof Error) {
       ElMessage.error(rejectionReason.message || '标记完成请求失败');
    } else {
       ElMessage.error('标记完成操作时发生未知错误');
    }
  } finally {
    if (order) order.completing = false;
  }
};

const formatOrderStatus = (statusKey: string | undefined): string => {
  if (!statusKey) return '未知状态';
  const statusMap: Record<string, string> = {
    'pending_payment': '待支付',
    'paid': '已支付/待服务',
    'completed': '已完成',
    'cancelled': '已取消',
    '待支付': '待支付',
    '已支付/待服务': '已支付/待服务',
    '服务中': '服务中',
    '已完成': '已完成',
    '已取消': '已取消',
  };
  return statusMap[statusKey.toLowerCase()] || statusKey;
};

const getOrderStatusType = (statusKey: string | undefined): 'success' | 'warning' | 'info' | 'danger' | 'primary' => {
  if (!statusKey) return 'info';
  switch (statusKey.toLowerCase()) {
    case 'paid':
    case '已支付/待服务':
    case '服务中':
      return 'warning';
    case 'completed':
    case '已完成':
      return 'success';
    case 'pending_payment':
    case '待支付':
      return 'info';
    case 'cancelled':
    case '已取消':
      return 'danger';
    // case 'some_other_status_that_is_primary': // 示例 primary 类型
    //   return 'primary';
    default:
      return 'info';
  }
};

const formatDateTime = (dateTimeStr: string | undefined): string => {
  if (!dateTimeStr) return '-';
  try {
    return new Date(dateTimeStr).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' });
  } catch (e) {
    return dateTimeStr;
  }
};

const handlePageChange = (newPage: number) => {
  currentPage.value = newPage;
};

const canCompleteOrder = (status: string | undefined): boolean => {
  if (!status) return false;
  const lowerStatus = status.toLowerCase();
  return lowerStatus === 'paid' || lowerStatus === '已支付/待服务' || lowerStatus === '服务中';
};

onMounted(() => {
  fetchOrders();
});
</script>

<style scoped>
.provider-order-list-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px; /* 保持或调整您的样式 */
  font-weight: bold; /* 保持或调整您的样式 */
}
.card-header span {
  display: flex;
  align-items: center;
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