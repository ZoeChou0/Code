<template>
  <div class="provider-dashboard-page">
    <header class="dashboard-header">
      <h1>服务商工作台</h1>
      <p v-if="userStore.userInfo">欢迎您，{{ userStore.userInfo.name }}！</p>
    </header>

    <el-row :gutter="20" class="stats-cards">
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background-color: #E6A23C20; color: #E6A23C;">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.pendingReservationsCount ?? 'N/A' }}</div>
              <div class="stat-label">待处理预约</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background-color: #409EFF20; color: #409EFF;">
              <el-icon><Briefcase /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.activeServicesCount ?? 'N/A' }}</div>
              <div class="stat-label">上架中服务</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background-color: #67C23A20; color: #67C23A;">
              <el-icon><Finished /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.completedOrdersThisMonth ?? 'N/A' }}</div>
              <div class="stat-label">本月完成订单</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="box-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span><el-icon><List /></el-icon> 我的服务</span>
          <el-button type="primary" :icon="Plus" @click="goToAddService">发布新服务</el-button>
        </div>
      </template>
      <div v-if="loadingServices" class="loading-placeholder">
        <el-skeleton :rows="3" animated />
      </div>
      <el-alert v-else-if="loadServicesError" title="加载服务列表失败" type="error" show-icon :closable="false" />
      <el-table v-else :data="myServices" stripe style="width: 100%" empty-text="您还没有发布任何服务">
        <el-table-column prop="name" label="服务名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="price" label="价格 (¥)" width="100" />
        <el-table-column prop="duration" label="时长 (分钟)" width="110" />
        <el-table-column prop="reviewStatus" label="审核状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getServiceStatusTagType(row.reviewStatus)" size="small" effect="light">
              {{ getServiceStatusText(row.reviewStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="当前状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.reviewStatus === 'APPROVED'" :type="isServiceEffectivelyAvailable(row) ? 'success' : 'info'" size="small">
              {{ isServiceEffectivelyAvailable(row) ? '上架中' : '已下架' }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" :icon="Edit" @click="goToEditService(row.id)">编辑</el-button>
            <el-tooltip :content="row.reviewStatus === 'APPROVED' ? '下架服务' : '申请上架'" placement="top">
              <el-button
                link
                :type="row.reviewStatus === 'APPROVED' ? 'warning' : 'success'"
                size="small"
                :icon="row.reviewStatus === 'APPROVED' ? UploadFilled : Download"
                @click="toggleServiceAvailability(row)"
                :disabled="row.reviewStatus === 'REJECTED'"
              >
                {{ row.reviewStatus === 'APPROVED' ? '下架' : '上架' }}
              </el-button>
            </el-tooltip>
            <el-button link type="danger" size="small" :icon="Delete" @click="handleDeleteService(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="box-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span><el-icon><Calendar /></el-icon> 收到的预约</span>
          <el-button link type="primary" @click="fetchReservations(true)" :icon="Refresh">刷新</el-button>
        </div>
      </template>
      <div v-if="loadingReservations" class="loading-placeholder">
        <el-skeleton :rows="3" animated />
      </div>
      <el-alert v-else-if="loadReservationsError" title="加载预约列表失败" type="error" show-icon :closable="false">
        {{ reservationErrorMessage || '获取预约数据失败，请稍后重试。' }}
      </el-alert>
      <el-table v-else :data="sortedIncomingReservations" stripe style="width: 100%" empty-text="暂无新的预约">
        <el-table-column prop="id" label="预约ID" width="80" />
        <el-table-column prop="serviceName" label="服务名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="userName" label="预约用户" width="120">
          <template #default="{ row }">{{ row.userName || '用户 ' + row.userId }}</template>
        </el-table-column>
        <el-table-column prop="petName" label="预约宠物" width="120">
          <template #default="{ row }">{{ row.petName || '宠物 ' + row.petId }}</template>
        </el-table-column>
        <el-table-column prop="reservationStartDate" label="开始日期" width="120" />
        <el-table-column prop="serviceStartTime" label="开始时间" width="140" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getReservationStatusTagType(row.status)" size="small">
              {{ getReservationStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="success" size="small" :icon="Check" @click="handleConfirmReservation(row.id)" v-if="row.status === 'PENDING_CONFIRMATION'">确认</el-button>
            <el-button link type="danger" size="small" :icon="Close" @click="handleRejectReservation(row.id)" v-if="row.status === 'PENDING_CONFIRMATION'">拒绝</el-button>
            <el-button link type="primary" size="small" :icon="Finished" @click="handleCompleteReservation(row.id)" v-if="row.status === 'CONFIRMED'">完成</el-button>
            <el-button link type="warning" size="small" :icon="CancelIcon" @click="handleCancelByProvider(row.id)" v-if="row.status === 'CONFIRMED'">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import {
  ElCard, ElButton, ElTable, ElTableColumn, ElTag,
  ElIcon, ElSkeleton, ElAlert, ElTooltip, ElMessage, ElMessageBox
} from 'element-plus';
import {
  Plus, Edit, Delete,
  Calendar, Briefcase, Check, Close, Finished,
  CircleClose as CancelIcon,
  UploadFilled, Download, Refresh
} from '@element-plus/icons-vue';
import { useUserStore } from '@/stores/user';
import {
  getProviderServices, deleteProviderService, setServiceAvailability
} from '@/api/service';
import {
  getProviderReservations, confirmReservationByProvider,
  rejectReservationByProvider, completeReservationByProvider,
  cancelReservationByProvider
} from '@/api/reservation';
import { getProviderDashboardStats, type ProviderDashboardStats } from '@/api/dashboard';
import type { Service } from '@/api/service';
import type { Reservation } from '@/api/reservation';

const router = useRouter();
const userStore = useUserStore();

const loadingServices = ref(true);
const loadServicesError = ref(false);
const myServices = ref<Service[]>([]);
const loadingReservations = ref(true);
const loadReservationsError = ref(false);
const reservationErrorMessage = ref('');
const incomingReservations = ref<Reservation[]>([]);
const stats = ref<ProviderDashboardStats>({ pendingReservationsCount: 0, activeServicesCount: 0, completedOrdersThisMonth: 0 });

const sortedIncomingReservations = computed(() => {
  return [...incomingReservations.value].sort((a, b) => {
    if (a.status === 'PENDING_CONFIRMATION' && b.status !== 'PENDING_CONFIRMATION') return -1;
    if (a.status !== 'PENDING_CONFIRMATION' && b.status === 'PENDING_CONFIRMATION') return 1;
    if (a.createdAt && b.createdAt) return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
    return 0;
  });
});

onMounted(() => {
  if (!userStore.userInfo) {
    userStore.getUserInfoAction().then(fetchDashboardData);
  } else {
    fetchDashboardData();
  }
});

const fetchDashboardData = () => {
  fetchMyServices();
  fetchReservations();
  fetchStats();
};

const fetchStats = async () => {
  try {
    const res = await getProviderDashboardStats();
    if (res.code === 200 && res.data) stats.value = res.data;
  } catch {}
};

const fetchMyServices = async () => {
  loadingServices.value = true; loadServicesError.value = false;
  try {
    const res = await getProviderServices();
    if (res.code === 200) myServices.value = res.data;
    else loadServicesError.value = true;
  } catch { loadServicesError.value = true; }
  finally { loadingServices.value = false; }
};

const fetchReservations = async (showMsg = false) => {
  loadingReservations.value = true; loadReservationsError.value = false; reservationErrorMessage.value = '';
  try {
    const res = await getProviderReservations();
    if (res.code === 200) {
      incomingReservations.value = res.data;
      if (showMsg) ElMessage.success('预约列表已刷新');
    } else {
      throw new Error(res.message);
    }
  } catch (e: any) {
    loadReservationsError.value = true;
    reservationErrorMessage.value = e.message;
  } finally { loadingReservations.value = false; }
};

const goToAddService = () => router.push('/provider/services/add');
const goToEditService = (id: number) => router.push(`/provider/services/edit/${id}`);

const handleDeleteService = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定删除此服务？', '确认', { type: 'warning' });
    const res = await deleteProviderService(id);
    if (res.code === 200) { ElMessage.success('删除成功'); fetchMyServices(); fetchStats(); }
    else ElMessage.error(res.message);
  } catch (e: any) { if (e !== 'cancel') ElMessage.error(e.message); }
};

const toggleServiceAvailability = async (service: Service) => {
  const toAvailable = service.reviewStatus !== 'APPROVED';
  const msg = toAvailable ? `申请上架 "${service.name}"？` : `下架 "${service.name}"？`;
  try {
    await ElMessageBox.confirm(msg, '确认', { type: 'warning' });
    const res = await setServiceAvailability(service.id, toAvailable);
    if (res.code === 200) { ElMessage.success('操作成功'); fetchMyServices(); fetchStats(); }
    else ElMessage.error(res.message);
  } catch (e: any) { if (e !== 'cancel') ElMessage.error(e.message); }
};

const isServiceEffectivelyAvailable = (s: Service) => s.reviewStatus === 'APPROVED';

const handleConfirmReservation = async (id: number) => {
  try {
    const res = await confirmReservationByProvider(id);
    if (res.code === 200) { ElMessage.success('已确认'); fetchReservations(); fetchStats(); }
    else ElMessage.error(res.message);
  } catch {};
};

const handleRejectReservation = async (id: number) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝理由', '拒绝', {});
    const res = await rejectReservationByProvider(id, value);
    if (res.code === 200) { ElMessage.success('已拒绝'); fetchReservations(); fetchStats(); }
    else ElMessage.error(res.message);
  } catch {}
};

const handleCompleteReservation = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认完成服务？','完成',{type:'info'});
    const res = await completeReservationByProvider(id);
    if (res.code===200) { ElMessage.success('已完成'); fetchReservations(); fetchStats(); }
    else ElMessage.error(res.message);
  } catch {}
};

const handleCancelByProvider = async (id: number) => {
  try {
    const { value } = await ElMessageBox.prompt('输入取消理由','取消预约',{inputType:'textarea',inputValidator: v=>!!v||'必填'});
    const res = await cancelReservationByProvider(id, value);
    if (res.code===200) { ElMessage.success('已取消'); fetchReservations(); fetchStats(); }
    else ElMessage.error(res.message);
  } catch {}
};

const getServiceStatusTagType = (status?: string) => {
  switch (status) {
    case 'APPROVED': return 'success';
    case 'PENDING_CONFIRMATION': return 'warning';
    case 'REJECTED': return 'danger';
    default: return 'info';
  }
};
const getServiceStatusText = (status?: string) => {
  switch (status) {
    case 'APPROVED': return '已批准';
    case 'PENDING_CONFIRMATION': return '审核中';
    case 'REJECTED': return '已拒绝';
    default: return '未知';
  }
};
const getReservationStatusTagType = (status?: string) => {
  switch (status) {
    case 'CONFIRMED': return 'success';
    case 'PENDING_CONFIRMATION': return 'warning';
    case 'REJECTED': return 'danger';
    case 'COMPLETED': return 'primary';
    default: return 'info';
  }
};
const getReservationStatusText = (status?: string) => {
  switch (status) {
    case 'CONFIRMED': return '已确认';
    case 'PENDING_CONFIRMATION': return '待确认';
    case 'REJECTED': return '已拒绝';
    case 'COMPLETED': return '已完成';
    default: return '未知';
  }
};
</script>

<style scoped lang="scss">
.provider-dashboard-page {
  padding: 20px;
}
.dashboard-header {
  margin-bottom: 20px;
  h1 { margin: 0; }
}
.stats-cards { margin-bottom: 25px;
  .el-card { border-radius: 8px; }
  .stat-item { display: flex; align-items: center; }
  .stat-icon { font-size: 24px; padding: 12px; margin-right: 15px; border-radius: 50%; display: flex; align-items: center; justify-content: center;
    .el-icon { font-size: 24px !important; }
  }
  .stat-content { .stat-value { font-size: 20px; font-weight: bold; } .stat-label { font-size: 13px; color: #909399; } }
}
.box-card { margin-bottom: 20px; .card-header { display: flex; justify-content: space-between; align-items: center; } }
.loading-placeholder { padding: 20px; }
</style>
