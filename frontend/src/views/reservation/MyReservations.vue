<template>
  <div>
    <h1>我的预约</h1>
    <el-table :data="reservations" v-loading="loading" style="width: 100%" empty-text="暂无预约记录">
      <el-table-column prop="id" label="预约ID" width="100" sortable />
      <el-table-column label="服务项目ID" prop="serviceId" width="120"/>
      <el-table-column label="宠物ID" prop="petId" width="100"/>

      <el-table-column label="预约日期">
        <template #default="scope">
        <span v-if="scope.row.reservationStartDate && scope.row.reservationEndDate">
            {{ scope.row.reservationStartDate }} 至 {{ scope.row.reservationEndDate }}
        </span>
        <span v-else-if="scope.row.reservationStartDate">
            {{ scope.row.reservationStartDate }}
        </span>
        <span v-else>N/A</span>
        </template>
      </el-table-column>

       <el-table-column label="预约时间" prop="serviceStartTime" width="180" sortable>
         <template #default="scope">
           {{ formatDisplayDateTime(scope.row.serviceStartTime) }}
         </template>
       </el-table-column>

      <el-table-column prop="status" label="状态" width="180">
         <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)" disable-transitions>
                {{ formatStatus(scope.row.status) }}
            </el-tag>
         </template>
      </el-table-column>

      <el-table-column prop="amount" label="金额" width="100" sortable>
         <template #default="scope">
           ¥{{ scope.row.amount?.toFixed(2) }} </template>
      </el-table-column>

       <el-table-column label="创建时间" prop="createdAt" width="180" sortable>
         <template #default="scope">
           {{ formatDisplayDateTime(scope.row.createdAt) }}
         </template>
       </el-table-column>


      <el-table-column label="操作" fixed="right" width="150">
        <template #default="scope">
          <el-button
            link
            type="danger"
            size="small"
            @click="handleCancel(scope.row)"
            :disabled="!canCancel(scope.row.status, scope.row.serviceStartTime)" >
            取消预约
          </el-button>
          </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router'; // 如果需要跳转，导入 useRouter
import { getMyReservations, cancelReservation } from '@/api/reservation'; // 导入 API
import type { Reservation } from '@/api/reservation'; // 导入类型
import { ElTable, ElTableColumn, ElButton, ElTag, ElMessage, ElMessageBox } from 'element-plus';
import dayjs from 'dayjs'; // 用于日期格式化

const router = useRouter(); // 初始化 router
const reservations = ref<Reservation[]>([]);
const loading = ref(false);

// 获取我的预约列表
const fetchReservations = async () => {
  loading.value = true;
  try {
    const res = await getMyReservations(); // 返回 BackendResult<Reservation[]>
    if (res.code === 200 && Array.isArray(res.data)) {
      // 按服务开始时间降序排序 (如果后端没排序)
      reservations.value = res.data.sort((a, b) =>
        dayjs(b.serviceStartTime).valueOf() - dayjs(a.serviceStartTime).valueOf()
      );
    } else {
      ElMessage.error(res.message || '获取预约列表失败');
      reservations.value = []; // 清空数据
    }
  } catch (error: any) {
    //ElMessage.error(error.message || '获取预约列表时出错');
    reservations.value = []; // 清空数据
  } finally {
    loading.value = false;
  }
};

// --- 格式化函数 ---
const formatDisplayDate = (dateString: string | undefined | null): string => {
    if (!dateString) return 'N/A';
    return dayjs(dateString).format('YYYY-MM-DD');
}
const formatDisplayDateTime = (dateTimeString: string | undefined | null): string => {
    if (!dateTimeString) return 'N/A';
    return dayjs(dateTimeString).format('YYYY-MM-DD HH:mm');
}
const formatStatus = (status: string | undefined | null): string => {
    if (!status) return '未知';
    const statusMap: Record<string, string> = {
        'PENDING_CONFIRMATION': '待确认',
        'CONFIRMED': '已确认', // 可改为“待支付”或“待服务”
        'AWAITING_PAYMENT': '待支付',
        'PAID': '待服务',
        'IN_PROGRESS': '服务中',
        'COMPLETED': '已完成',
        'CANCELLED_USER': '已取消(用户)',
        'CANCELLED_PROVIDER': '已取消(服务商)',
        'REJECTED': '已拒绝',
    };
    return statusMap[status.toUpperCase()] || status; // 转大写匹配，找不到返回原始值
}

// --- 类型修正：返回值不包含 ''，并提供默认有效类型 ---
const getStatusTagType = (status: string | undefined | null): ('success' | 'info' | 'warning' | 'danger') => {
    if (!status) return 'info'; // 默认类型
    switch (status.toUpperCase()) { // 转大写比较
        case 'COMPLETED': return 'success';
        case 'PAID':
        case 'CONFIRMED':
        case 'IN_PROGRESS': return 'info';
        case 'PENDING_CONFIRMATION':
        case 'AWAITING_PAYMENT': return 'warning';
        case 'CANCELLED_USER':
        case 'CANCELLED_PROVIDER':
        case 'REJECTED': return 'danger';
        default: return 'info'; // 未知状态也给个默认类型
    }
}

// --- 判断是否可取消 (接收参数) ---
const canCancel = (status: string | undefined | null, serviceStartTime: string | undefined | null): boolean => {
    if (!status || !serviceStartTime) return false;
    // 允许取消的状态
    const cancellableStatuses = ['PENDING_CONFIRMATION', 'CONFIRMED', 'AWAITING_PAYMENT', 'PAID'];
    const isCancellableStatus = cancellableStatuses.includes(status.toUpperCase());
    // 检查服务开始时间是否在未来 (例如，现在之后)
    const isFuture = dayjs(serviceStartTime).isAfter(dayjs());
    // 可以在这里加入更严格的时间限制，比如提前 24 小时
    // const canCancelByTime = dayjs(serviceStartTime).isAfter(dayjs().add(24, 'hour'));
    // return isCancellableStatus && canCancelByTime;
    return isCancellableStatus && isFuture;
};

// --- 处理取消操作 ---
const handleCancel = async (reservation: Reservation) => {
  if (!reservation || !reservation.id) return; // 基本检查

  try {
    // 确认弹窗
    await ElMessageBox.confirm(
      `确定要取消预约 ID 为 ${reservation.id} 的服务吗？`,
      '确认取消', { confirmButtonText: '确定', cancelButtonText: '点错了', type: 'warning' }
    );

    // 用户确认后执行取消
    loading.value = true; // 开始加载状态
    try {
        const res = await cancelReservation(reservation.id);
        if (res.code === 200) {
            ElMessage.success('预约已取消');
            fetchReservations(); // 成功后刷新列表
        } else {
            ElMessage.error(res.message || '取消失败');
            loading.value = false; // 失败时也结束加载状态
        }
    } catch (apiError: any) {
         ElMessage.error(apiError.message || '取消操作时出错');
         loading.value = false; // API出错时结束加载状态
    }
    // 注意：fetchReservations 成功后会自己把 loading 改回 false

  } catch (cancel) {
    // 用户点击了 ElMessageBox 的“点错了”或关闭按钮
    console.log('用户取消了操作');
    // 确保 loading 状态被重置 (如果用户在 loading 状态时取消)
    if (loading.value) loading.value = false;
  }
};

// --- 其他操作处理函数 (示例) ---
// const handlePay = (reservation: Reservation) => {
//   if(!reservation.orderId) {
//       ElMessage.warning('此预约没有关联的订单信息');
//       return;
//   }
//   // router.push({ name: 'PaymentPage', query: { orderId: reservation.orderId } });
//   console.log(`跳转支付页面，订单ID: ${reservation.orderId}`);
// };
// const handleReview = (reservation: Reservation) => {
//   // router.push({ name: 'CreateReviewPage', params: { reservationId: reservation.id } });
//    console.log(`跳转评价页面，预约ID: ${reservation.id}`);
// }

// 组件挂载时获取数据
onMounted(() => {
  fetchReservations();
});
</script>

<style scoped>
/* 如果需要可以添加样式 */
.el-table .el-button {
  /* 调整按钮间距等 */
  margin-right: 8px;
}
</style>