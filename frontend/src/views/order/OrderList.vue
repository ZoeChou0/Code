<template>
  <div class="order-list-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的订单</span>
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
        <el-table-column label="宠物名称" min-width="120">
          <template #default="scope">
            <span>{{ scope.row.petName || 'N/A' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额 (元)" width="120">
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
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="scope">
            <span>{{ formatDateTime(scope.row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="payTime" label="支付时间" width="180">
          <template #default="scope">
            <span>{{ scope.row.payTime ? formatDateTime(scope.row.payTime) : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 'pending' || scope.row.status === '待支付'"
              type="primary"
              size="small"
              @click="handlePay(scope.row)"
              :loading="scope.row.paying"
            >
              去支付
            </el-button>
            <el-button
              v-if="canCancelOrder(scope.row)"
              type="danger"
              size="small"
              @click="handleCancelOrder(scope.row.id)"
              :loading="scope.row.cancelling"
            >
              取消订单
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
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Order } from '@/api/order' // 确保 Order 类型定义正确
import { getUserOrderList, cancelOrder as apiCancelOrder } from '@/api/order'
import { initiatePcPayment } from '@/api/alipay' // 导入支付宝支付API
import type { BackendResult } from '@/types/api'

interface OrderWithActions extends Order {
  paying?: boolean
  cancelling?: boolean
  serviceName?: string 
  petName?: string    
  reservationServiceStartTime?: string;
}

const orders = ref<OrderWithActions[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10) // 根据需要调整
const totalOrders = ref(0)

const fetchOrders = async () => {
  loading.value = true
  try {
    // 如果API支持分页，传递 currentPage.value 和 pageSize.value
    const res: BackendResult<Order[]> = await getUserOrderList(/* { page: currentPage.value, size: pageSize.value } */)
    if (res.code === 200 && res.data) {
      orders.value = res.data.map(order => ({ ...order, paying: false, cancelling: false }))
      // 如果API返回总数，请设置 totalOrders.value
      // totalOrders.value = res.pagination?.total || res.data.length; // 假设API返回分页信息
      totalOrders.value = res.data.length; // 简单处理，如果API不直接返回总数
    } else {
      ElMessage.error(res.message || '获取订单列表失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取订单列表请求失败')
  } finally {
    loading.value = false
  }
}

const handlePay = async (order: OrderWithActions) => {
  if (!order.id) {
    ElMessage.error('订单ID无效')
    return
  }
  order.paying = true
  try {
    const res: BackendResult<string> = await initiatePcPayment({ outTradeNo: String(order.id) })
    if (res.code === 200 && typeof res.data === 'string') {
      // ElMessage.success('正在跳转到支付宝...');
      const alipayFormHtml = res.data
      const div = document.createElement('div')
      div.style.display = 'none' // 避免在页面上闪现
      div.innerHTML = alipayFormHtml
      document.body.appendChild(div)
      const form = div.querySelector('form')
      if (form) {
        form.submit()
        document.body.removeChild(div) // 提交后移除
      } else {
        ElMessage.error('支付宝支付表单加载失败，请重试。')
        document.body.removeChild(div) // 即使失败也移除
      }
    } else {
      ElMessage.error(res.message || '发起支付失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '支付请求失败')
  } finally {
    order.paying = false
  }
}

const handleCancelOrder = async (orderId: number) => {
  const order = orders.value.find(o => o.id === orderId)
  if (!order) return

  try {
    await ElMessageBox.confirm('确定要取消此订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    order.cancelling = true
    const res: BackendResult<null> = await apiCancelOrder(orderId)
    if (res.code === 200) {
      ElMessage.success('订单取消成功')
      // 刷新列表或更新该订单状态
      const index = orders.value.findIndex(o => o.id === orderId)
      if (index !== -1) {
        orders.value[index].status = 'cancelled' // 或后端返回的实际状态
      }
    } else {
      ElMessage.error(res.message || '取消订单失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') { // 用户点击了取消按钮
        ElMessage.error(error.message || '取消订单请求失败')
    }
  } finally {
    if (order) order.cancelling = false
  }
}


const canCancelOrder = (order: OrderWithActions): boolean => {
  if (!order || !order.status) {
    return false;
  }

  const currentStatus = order.status.toLowerCase(); 

  // 状态1: '待支付' 或 'pending' (英文) 总是可以取消
  if (currentStatus === 'pending' || currentStatus === '待支付') {
    return true;
  }

  // 状态2: '已支付' 或 'paid' (英文)
  if (currentStatus === 'paid' || currentStatus === '已支付/待服务') {
    // 检查是否有服务开始时间，并且是否在24小时之外
    if (order.reservationServiceStartTime) {
      try {
        const serviceStartTime = new Date(order.reservationServiceStartTime);
        const now = new Date();
        const twentyFourHoursInMs = 24 * 60 * 60 * 1000;

        // 如果服务开始时间在当前时间的24小时之后，则可以取消
        if (serviceStartTime.getTime() > now.getTime() + twentyFourHoursInMs) {
          return true;
        } else {
          // ElMessage.info('距离服务开始不足24小时，无法取消。'); // 可选：给出提示
          return false;
        }
      } catch (e) {
        console.error("Error parsing reservationServiceStartTime: ", order.reservationServiceStartTime, e);
        return false; // 日期格式错误，默认不可取消
      }
    } else {
      // 如果没有服务开始时间信息，根据业务规则决定是否允许取消。
      // 这里假设如果没有时间信息，已支付的订单不允许用户前端直接取消，可能需要联系客服。
      return false;
    }
  }

  // 其他状态 (如 completed, cancelled) 不可取消
  return false;
};



const formatOrderStatus = (status: string): string => {
  // 你可能需要一个更完善的状态映射
  const statusMap: Record<string, string> = {
    pending: '待支付',
    paid: '已支付/待服务',
    completed: '已完成',
    cancelled: '已取消',
    // 后端可能返回中文状态
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
    case '已支付/待服务':
    case 'completed':
    case '已完成':
      return 'success'
    case 'pending':
    case '待支付':
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
    return dateTimeStr // 如果格式化失败，返回原始字符串
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
.order-list-container {
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