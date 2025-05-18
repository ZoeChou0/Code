<template>
  <el-container direction="vertical" v-loading="loadingInitialData">
    <el-header height="auto" style="padding-bottom: 20px;">
      <h1>服务商工作台</h1>
      <p v-if="userStore.userInfo">欢迎回来，{{ userStore.userInfo.name }}！</p>
    </el-header>

    <el-main v-if="!loadingInitialData">
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-card shadow="hover" class="statistic-card">
            <el-statistic title="即将到来的预约" :value="summary.upcomingReservationsCount" />
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-card shadow="hover" class="statistic-card">
            <el-statistic title="待审核服务" :value="summary.pendingServicesCount">
              <template #suffix>
                <el-tooltip effect="dark" content="您提交的等待管理员审核的服务数量" placement="top">
                  <el-icon style="margin-left: 4px"><Warning /></el-icon>
                </el-tooltip>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-card shadow="hover" class="statistic-card">
            <el-statistic title="已发布服务" :value="summary.totalActiveServicesCount">
               <template #suffix>
                 <el-tooltip effect="dark" content="当前已通过审核并在线的服务总数" placement="top">
                   <el-icon style="margin-left: 4px"><Service /></el-icon>
                 </el-tooltip>
               </template>
             </el-statistic>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
           <el-card shadow="hover" class="statistic-card">
             <el-statistic title="需要处理的预约" :value="summary.pendingActionReservationsCount">
                <template #suffix>
                  <el-tooltip effect="dark" content="等待您确认或正在进行中的预约" placement="top">
                    <el-icon style="margin-left: 4px"><Bell /></el-icon>
                  </el-tooltip>
                </template>
              </el-statistic>
           </el-card>
         </el-col>
      </el-row>

      <el-card shadow="never" style="margin-bottom: 20px;">
        <template #header>
          <div class="card-header">
            <span>快速操作</span>
          </div>
        </template>
        <el-space wrap :size="10">
          <el-button type="primary" :icon="Edit" @click="goTo('/provider/services')">管理我的服务</el-button>
          <el-button type="success" :icon="Plus" @click="goTo('/provider/services/add')">添加新服务</el-button>
          <el-button :icon="Tickets" @click="goTo('/provider/reservations')">查看预约列表</el-button>
          <el-button :icon="User" @click="goTo('/provider/profile/edit')">编辑个人资料</el-button>
        </el-space>
      </el-card>

      <el-card shadow="never">
         <template #header>
           <div class="card-header">
             <span>近期或待处理预约</span>
             <el-button class="button" text @click="goTo('/provider/reservations')">查看全部</el-button>
           </div>
         </template>
         <el-table :data="upcomingReservations" style="width: 100%" v-loading="loadingReservations" empty-text="暂无近期或待处理预约">
            <el-table-column prop="reservationDate" label="日期" width="120" sortable />
            <el-table-column label="时间" width="160">
              <template #default="scope">
                {{ formatTimeRange(scope.row.serviceStartTime, scope.row.serviceEndTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="serviceName" label="服务名称" show-overflow-tooltip />
            <el-table-column prop="petName" label="宠物" width="100" show-overflow-tooltip />
            <el-table-column prop="userName" label="客户" width="100" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="150">
               <template #default="scope">
                 <el-tag :type="getStatusTagType(scope.row.status)">{{ formatStatus(scope.row.status) }}</el-tag>
               </template>
             </el-table-column>
             <el-table-column prop="amount" label="金额(元)" width="100">
                 <template #default="scope">
                    {{ scope.row.amount?.toFixed(2) ?? '-' }}
                 </template>
             </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
               <template #default="scope">
                 <el-button link type="primary" size="small" @click="viewReservation(scope.row.id)">
                   处理/详情
                 </el-button>
                 </template>
             </el-table-column>
         </el-table>
      </el-card>

    </el-main>
     <el-main v-else style="text-align: center; padding: 50px;">
        <p>正在加载工作台数据...</p>
     </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import {
  ElCard, ElContainer, ElHeader, ElMain, ElRow, ElCol, ElStatistic,
  ElButton, ElSpace, ElTable, ElTableColumn, ElMessage, ElTag, ElIcon, ElTooltip, vLoading
} from 'element-plus';
import { Warning, Service, Bell, Edit, Plus, Tickets, User } from '@element-plus/icons-vue'; // 导入图标
// 导入你的 API 函数 - ***你需要创建这些 API 函数***
// import { getProviderDashboardSummary, getProviderUpcomingReservations } from '@/api/provider';
// import type { ProviderDashboardSummary } from '@/api/provider';

// --- 模拟 API 函数 (需要替换为真实的 API 调用) ---
// 模拟后端返回的汇总数据类型
interface MockProviderDashboardSummary {
  upcomingReservationsCount: number;
  pendingServicesCount: number;
  totalActiveServicesCount: number;
  pendingActionReservationsCount: number;
}
// 模拟后端返回的预约数据类型 (需要包含关联字段)
interface MockReservationItem {
  id: number | string;
  reservationDate: string; // 'YYYY-MM-DD'
  serviceStartTime: string; // ISO DateTime string
  serviceEndTime: string | null; // ISO DateTime string or null
  serviceName: string;
  petName: string;
  userName: string;
  status: string; // e.g., 'PENDING_CONFIRMATION', 'CONFIRMED', 'IN_PROGRESS'
  amount: number | null;
}

const getProviderDashboardSummary = async (): Promise<{ code: number; data?: MockProviderDashboardSummary; message?: string }> => {
  console.log("模拟获取汇总数据...");
  await new Promise(resolve => setTimeout(resolve, 500)); // 模拟网络延迟
  // ***在此处替换为真实 API 调用***
  // const { data } = await actualGetProviderDashboardSummary();
  // return data;
  // 模拟数据：
  return {
    code: 200,
    data: {
      upcomingReservationsCount: 3,
      pendingServicesCount: 1,
      totalActiveServicesCount: 5,
      pendingActionReservationsCount: 2, // 待确认 + 进行中
    }
  };
};

const getProviderUpcomingReservations = async (params?: { limit?: number }): Promise<{ code: number; data?: MockReservationItem[]; message?: string }> => {
  console.log("模拟获取近期预约数据...", params);
  await new Promise(resolve => setTimeout(resolve, 800)); // 模拟网络延迟
   // ***在此处替换为真实 API 调用***
   // const { data } = await actualGetProviderUpcomingReservations(params);
   // return data;
   // 模拟数据 (确保包含了 serviceName, petName, userName):
   return {
    code: 200,
    data: [
      { id: 101, reservationDate: '2025-05-03', serviceStartTime: '2025-05-03T10:00:00Z', serviceEndTime: '2025-05-03T11:00:00Z', serviceName: '宠物洗澡', petName: '旺财', userName: '张三', status: 'CONFIRMED', amount: 50.00 },
      { id: 102, reservationDate: '2025-05-03', serviceStartTime: '2025-05-03T14:00:00Z', serviceEndTime: '2025-05-03T15:30:00Z', serviceName: '宠物美容', petName: '咪咪', userName: '李四', status: 'PENDING_CONFIRMATION', amount: 120.50 },
      { id: 103, reservationDate: '2025-05-04', serviceStartTime: '2025-05-04T09:00:00Z', serviceEndTime: '2025-05-04T17:00:00Z', serviceName: '日间寄养', petName: '豆豆', userName: '王五', status: 'IN_PROGRESS', amount: 80.00 },
    ].slice(0, params?.limit ?? 5) // 模拟分页/限制
   };
};
// --- 模拟结束 ---

const router = useRouter();
const userStore = useUserStore();

// 使用 reactive 管理汇总数据
const summary = reactive<MockProviderDashboardSummary>({
  upcomingReservationsCount: 0,
  pendingServicesCount: 0,
  totalActiveServicesCount: 0,
  pendingActionReservationsCount: 0,
});

// 使用 ref 管理预约列表和加载状态
const upcomingReservations = ref<MockReservationItem[]>([]);
const loadingInitialData = ref(true); // 初始整体加载状态
const loadingReservations = ref(false); // 预约列表单独加载状态

// 获取并填充仪表盘数据
const fetchDashboardData = async () => {
  loadingInitialData.value = true; // 开始加载
  loadingReservations.value = true; // 预约列表也开始加载
  try {
    // 并行获取数据（如果 API 允许）
    const [summaryRes, reservationsRes] = await Promise.all([
      getProviderDashboardSummary(),
      getProviderUpcomingReservations({ limit: 5 }) // 获取最近5条
    ]);

    // 处理汇总信息
    if (summaryRes.code === 200 && summaryRes.data) {
      Object.assign(summary, summaryRes.data);
    } else {
      ElMessage.error(summaryRes.message || '获取统计信息失败');
    }

    // 处理预约列表
    if (reservationsRes.code === 200 && reservationsRes.data) {
      upcomingReservations.value = reservationsRes.data;
    } else {
      ElMessage.error(reservationsRes.message || '获取近期预约失败');
    }

  } catch (error: any) {
    console.error("加载工作台数据时出错:", error);
    ElMessage.error('加载工作台数据时出错: ' + error.message);
  } finally {
    loadingInitialData.value = false; // 结束加载
    loadingReservations.value = false; // 预约列表也结束加载
  }
};

// --- 导航和辅助函数 ---
const goTo = (path: string) => {
  router.push(path);
};

const viewReservation = (id: number | string) => {
  // 跳转到预约详情/处理页面
  router.push(`/provider/reservations/${id}`); // 示例路由
};

// 格式化时间范围
const formatTimeRange = (start: string | null | undefined, end: string | null | undefined): string => {
  const format = (timeStr: string | null | undefined): string => {
    if (!timeStr) return '?';
    try {
      return new Date(timeStr).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', hour12: false });
    } catch {
      return '?';
    }
  };
  return `${format(start)} - ${format(end)}`;
};

// 格式化状态显示文本 (根据需要自定义)
const formatStatus = (status: string): string => {
  const statusMap: Record<string, string> = {
    'PENDING_CONFIRMATION': '待确认',
    'CONFIRMED': '已确认/待支付', // 或分为两个状态
    'AWAITING_PAYMENT': '待支付',
    'PAID': '已支付/待服务',
    'IN_PROGRESS': '服务中',
    'COMPLETED': '已完成',
    'CANCELLED_USER': '用户取消',
    'CANCELLED_PROVIDER': '服务商取消',
    'REJECTED': '已拒绝'
  };
  return statusMap[status.toUpperCase()] || status; // 转大写以匹配，找不到则返回原始值
};

// 获取状态标签类型 (用于颜色区分)
const getStatusTagType = (status: string): 'primary' | 'success' | 'info' | 'warning' | 'danger' => {
  const upperStatus = status.toUpperCase();
  if (upperStatus.includes('CANCELLED') || upperStatus.includes('REJECTED') || upperStatus.includes('已取消')|| upperStatus.includes('已拒绝')) {
    return 'danger';
  } else if (upperStatus.includes('COMPLETED') || upperStatus.includes('已完成')) {
    return 'success';
  } else if (upperStatus.includes('PENDING') || upperStatus.includes('待确认')) {
    return 'warning';
  } else if (upperStatus.includes('PROGRESS') || upperStatus.includes('服务中')) {
    return 'primary';
  } else {
    return 'info'; // 其他如 CONFIRMED, PAID 等
  }
};

// --- 生命周期钩子 ---
onMounted(() => {
  // 确保用户信息加载后再获取数据，或者 API 本身会处理权限
  if (userStore.userInfo) {
    fetchDashboardData();
  } else {
    // 可以监听用户信息加载完成事件或稍作延迟
    console.warn("用户信息尚未加载，稍后尝试获取仪表盘数据...");
    // 如果 userStore 会异步加载信息，可能需要 watch
    // watch(() => userStore.userInfo, (newInfo) => { if (newInfo) fetchDashboardData(); });
    // 或者简单延迟
    setTimeout(() => {
        if(userStore.userInfo) fetchDashboardData();
        else ElMessage.error("无法加载用户信息，请重新登录");
    }, 500);
  }
});

</script>

<style scoped>
.el-header {
  border-bottom: 1px solid var(--el-border-color-light);
  /* margin-bottom: 20px; */ /* Removed for potentially tighter layout */
}
.el-main {
    padding-top: 20px; /* Add padding if header margin removed */
}
.statistic-card {
  text-align: center;
}
.statistic-card :deep(.el-statistic__head) {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
  line-height: 1.2; /* Adjust line height */
}
.statistic-card :deep(.el-statistic__content) {
 font-size: 24px;
 font-weight: bold;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.el-space {
    display: flex; /* Ensure space takes full width if needed */
}
.el-table {
    margin-top: 10px; /* Add some space above the table */
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .el-col {
    margin-bottom: 15px; /* Add space between stacked cards on smaller screens */
  }
  .el-space {
    justify-content: center; /* Center buttons on small screens */
  }
}
</style>
