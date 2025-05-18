<template>
  <div class="admin-order-list-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>所有订单</span>
          <el-input v-model="searchKeyword" placeholder="搜索订单ID/用户ID/服务ID" clearable style="width: 300px; margin-left: 20px;" @keyup.enter="handleSearch" >
             <template #append>
                <el-button @click="handleSearch"><el-icon><Search /></el-icon></el-button>
            </template>
          </el-input>
        </div>
      </template>

      <el-table :data="filteredOrders" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="订单ID" width="90" sortable/>
        <el-table-column prop="userId" label="用户ID" width="90" sortable/>
        <el-table-column prop="reservationId" label="预约ID" width="100" />
        <el-table-column prop="serviceId" label="服务ID" width="90" />
         <el-table-column label="服务名称" min-width="150">
           <template #default="scope">
            <span>{{ scope.row.serviceName || 'N/A' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额 (元)" width="110" sortable>
           <template #default="scope">
            <span>¥ {{ scope.row.amount?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120" sortable>
          <template #default="scope">
            <el-tag :type="getOrderStatusType(scope.row.status)">
              {{ formatOrderStatus(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" sortable>
           <template #default="scope">
            <span>{{ formatDateTime(scope.row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="payTime" label="支付时间" width="170" sortable>
            <template #default="scope">
            <span>{{ scope.row.payTime ? formatDateTime(scope.row.payTime) : '-' }}</span>
          </template>
        </el-table-column>
         <el-table-column prop="completeTime" label="完成时间" width="170" sortable>
            <template #default="scope">
            <span>{{ scope.row.completeTime ? formatDateTime(scope.row.completeTime) : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="180">
          <template #default="scope">
            <el-button type="primary" link size="small" @click="viewOrderDetails(scope.row.id)">详情</el-button>
            <el-button
                v-if="canForceCancel(scope.row)"
                type="danger" link
                size="small"
                @click="handleForceCancelOrder(scope.row.id)"
                :loading="scope.row.cancelling">
                强制取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>
       <el-pagination
        v-if="totalOrders > 0 && !searchKeyword"
        layout="prev, pager, next, total, jumper, sizes"
        :total="totalOrders"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
        :current-page="currentPage"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        class="pagination-container"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" title="订单详情" width="60%">
      <div v-if="selectedOrder">
        <p><strong>订单ID:</strong> {{ selectedOrder.id }}</p>
        <p><strong>用户ID:</strong> {{ selectedOrder.userId }}</p>
        <p><strong>预约ID:</strong> {{ selectedOrder.reservationId }}</p>
        <p><strong>服务ID:</strong> {{ selectedOrder.serviceId }}</p>
        <p><strong>金额:</strong> ¥ {{ selectedOrder.amount?.toFixed(2) }}</p>
        <p><strong>状态:</strong> {{ formatOrderStatus(selectedOrder.status) }}</p>
        <p><strong>创建时间:</strong> {{ formatDateTime(selectedOrder.createdAt) }}</p>
        <p><strong>支付时间:</strong> {{ selectedOrder.payTime ? formatDateTime(selectedOrder.payTime) : '未支付' }}</p>
        <p><strong>完成时间:</strong> {{ selectedOrder.completeTime ? formatDateTime(selectedOrder.completeTime) : '未完成' }}</p>
        <p><strong>支付宝交易号:</strong> {{ selectedOrder.alipayTradeNo || '-' }}</p>
        </div>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, ElDialog } from 'element-plus'
import type { Order } from '@/api/order'
import { getAllOrderList, cancelOrder as apiCancelOrder } from '@/api/order' // 假设管理员取消订单也用这个，或有专门API
import type { BackendResult } from '@/types/api'
import { Search } from '@element-plus/icons-vue'


interface AdminOrder extends Order {
  serviceName?: string; // 假设后端提供
  userName?: string;    // 假设后端提供
  petName?: string;     // 假设后端提供
  cancelling?: boolean;
}


const allOrders = ref<AdminOrder[]>([]) // 存储从API获取的所有订单
const loading = ref(false)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const totalOrders = ref(0) // 用于分页

const dialogVisible = ref(false)
const selectedOrder = ref<AdminOrder | null>(null)


const fetchOrders = async () => {
  loading.value = true
  try {
    // 管理员接口可能需要支持更复杂的筛选和分页
    const params: any = {
        page: currentPage.value,
        size: pageSize.value,
        // keyword: searchKeyword.value // 如果后端支持关键词搜索
    };
    const res: BackendResult<Order[]> = await getAllOrderList(params)
    if (res.code === 200 && res.data) {
      allOrders.value = res.data.map(order => ({...order, cancelling: false}));
      // 假设API返回分页信息，如 res.pagination.total
      // totalOrders.value = res.pagination?.total || res.data.length;
      totalOrders.value = res.data.length; // 简单处理
    } else {
      ElMessage.error(res.message || '获取订单列表失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取订单列表请求失败')
  } finally {
    loading.value = false
  }
}

const filteredOrders = computed(() => {
  if (!searchKeyword.value) {
    return allOrders.value; // 如果API做了分页，这里应该是当前页的数据
  }
  const keyword = searchKeyword.value.toLowerCase().trim()
  return allOrders.value.filter(order =>
    String(order.id).includes(keyword) ||
    String(order.userId).includes(keyword) ||
    String(order.serviceId).includes(keyword) ||
    String(order.reservationId).includes(keyword) ||
    (order.status && order.status.toLowerCase().includes(keyword))
  )
})

const handleSearch = () => {
    // 如果后端支持关键词搜索，则调用 fetchOrders
    // fetchOrders();
    // 前端简单筛选，如果数据量大，强烈建议后端筛选分页
    if (!searchKeyword.value) { // 如果清空了搜索词，重新获取数据
        fetchOrders();
    }
    currentPage.value = 1; // 搜索后重置到第一页
}


const viewOrderDetails = (orderId: number) => {
  selectedOrder.value = allOrders.value.find(o => o.id === orderId) || null;
  if (selectedOrder.value) {
      dialogVisible.value = true;
  } else {
      ElMessage.warning('未找到订单详情');
  }
}

const handleForceCancelOrder = async (orderId: number) => {
  const order = allOrders.value.find(o => o.id === orderId);
  if (!order) return;

  try {
    await ElMessageBox.confirm('确定要强制取消此订单吗？此操作可能影响用户。', '警告', {
      confirmButtonText: '强制取消',
      cancelButtonText: '关闭',
      type: 'error'
    });
    order.cancelling = true;
    // 管理员取消订单可能需要一个专门的API，或者在apiCancelOrder中通过角色判断权限
    const res: BackendResult<null> = await apiCancelOrder(orderId); // 假设使用与用户取消相同的API
    if (res.code === 200) {
      ElMessage.success('订单已强制取消');
      // 更新列表
      const index = allOrders.value.findIndex(o => o.id === orderId);
      if (index !== -1) {
        allOrders.value[index].status = 'cancelled'; // 或后端返回的实际状态
      }
    } else {
      ElMessage.error(res.message || '强制取消订单失败');
    }
  } catch (error: any) {
    if (error !== 'cancel') {
        ElMessage.error(error.message || '强制取消订单请求失败');
    }
  } finally {
    if (order) order.cancelling = false;
  }
}

const canForceCancel = (order: Order): boolean => {
    const currentStatus = order.status.toLowerCase(); // 先转为小写统一处理
    const uncancelableStatuses = ['completed', 'cancelled', '已完成', '已取消'];
    // 检查当前状态的小写形式是否在不可取消状态列表中
    return !uncancelableStatuses.some(s => currentStatus === s.toLowerCase());
}

const formatOrderStatus = (status: string): string => {
  const statusMap: Record<string, string> = {
    pending: '待支付',
    paid: '已支付/待服务',
    processing: '处理中',
    completed: '已完成',
    cancelled: '已取消',
    '待支付': '待支付',
    '已支付': '已支付/待服务',
    '处理中': '处理中',
    '已完成': '已完成',
    '已取消': '已取消',
  }
  return statusMap[status] || status
}

const getOrderStatusType = (status: string): 'success' | 'warning' | 'info' | 'danger' | '' => {
  switch (status.toLowerCase()) {
    case 'paid':
    case '已支付/待服务':
      return 'success'
    case 'completed':
    case '已完成':
      return 'success'
    case 'pending':
    case '待支付':
      return 'warning'
    case 'processing':
    case '处理中':
      return 'warning'
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
const handleSizeChange = (newSize: number) => {
  pageSize.value = newSize
  currentPage.value = 1 // 切换每页数量时，回到第一页
  fetchOrders()
}


onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.admin-order-list-container {
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