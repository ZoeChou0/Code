<template>
  <div class="provider-order-list-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的服务订单</span>
          </div>
      </template>

      <el-table :data="orders" stripe style="width: 100%" v-loading="loading">
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
              v-if="scope.row.status === 'paid' || scope.row.status === '已支付/待服务'"
              type="success"
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
        layout="prev, pager, next, total"
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Order } from '@/api/order'
import { getProviderOrderList, completeOrder as apiCompleteOrder } from '@/api/order'
import type { BackendResult } from '@/types/api'

interface ProviderOrder extends Order {
  serviceName?: string;
  petName?: string;
  userName?: string; // 客户名称
  completing?: boolean;
}

const orders = ref<ProviderOrder[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const totalOrders = ref(0)

const fetchOrders = async () => {
  loading.value = true
  try {
    const res: BackendResult<Order[]> = await getProviderOrderList(/* { page: currentPage.value, size: pageSize.value } */)
    if (res.code === 200 && res.data) {
      orders.value = res.data.map(order => ({ ...order, completing: false }));
      totalOrders.value = res.data.length; // 假设 res.data 就是当前页的数组
    } else {
      ElMessage.error(res.message || '获取订单列表失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取订单列表请求失败')
  } finally {
    loading.value = false
  }
}

const handleCompleteOrder = async (orderId: number) => {
  const order = orders.value.find(o => o.id === orderId);
  if (!order) return;

  try {
    await ElMessageBox.confirm('确定要将此订单标记为已完成吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    });

    order.completing = true;
    const res: BackendResult<null> = await apiCompleteOrder(orderId); // 调用标记完成API
    if (res.code === 200) {
      ElMessage.success('订单已标记为完成');
      // 更新列表或该订单的状态
      const index = orders.value.findIndex(o => o.id === orderId);
      if (index !== -1) {
        orders.value[index].status = 'completed'; // 或后端返回的实际状态
      }
    } else {
      ElMessage.error(res.message || '标记完成失败');
    }
  } catch (error: any) {
     if (error !== 'cancel') {
        ElMessage.error(error.message || '标记完成请求失败');
    }
  } finally {
    if (order) order.completing = false;
  }
};


const formatOrderStatus = (status: string): string => {
  const statusMap: Record<string, string> = {
    pending: '待支付',
    paid: '已支付/待服务', // 服务商视角
    completed: '已完成',
    cancelled: '已取消',
    '待支付': '待支付',
    '已支付': '已支付/待服务',
    '已完成': '已完成',
    '已取消': '已取消',
  }
  return statusMap[status] || status
}

const getOrderStatusType = (status: string): 'success' | 'warning' | 'info' | 'danger' | '' => {
   switch (status.toLowerCase()) {
    case 'paid':
    case '已支付':
      return 'warning' // 待服务是warning
    case 'completed':
    case '已完成':
      return 'success'
    case 'pending':
    case '待支付':
      return 'info'
    case 'cancelled':
    case '已取消':
      return 'danger'
    default:
      return 'info'
  }
}

const formatDateTime = (dateTimeStr: string | undefined): string => {
  if (!dateTimeStr) return '-'
  try {
    return new Date(dateTimeStr).toLocaleString()
  } catch (e) {
    return dateTimeStr
  }
}

const handlePageChange = (newPage: number) => {
  currentPage.value = newPage
  fetchOrders()
}


onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.provider-order-list-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>