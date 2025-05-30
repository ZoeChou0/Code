<template>
  <div class="admin-provider-list-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span><el-icon><Shop /></el-icon> 服务商管理</span>
        </div>
      </template>

      <el-tabs v-model="activeTabName" @tab-click="handleTabClick">
        <el-tab-pane label="待审核资质" name="pending">
          <ProviderTable
            :providers="pendingProviders"
            :loading="loadingStates.pending"
            @approve="handleApprove"
            @reject="handleReject"
            status-to-show="PENDING_REVIEW"
          />
        </el-tab-pane>
        <el-tab-pane label="全部服务商" name="all">
           <ProviderTable
            :providers="allProviders"
            :loading="loadingStates.all"
            @approve="handleApprove"
            @reject="handleReject"
            status-to-show="ALL" 
          />
        </el-tab-pane>
        <el-tab-pane label="已批准资质" name="approved">
           <ProviderTable
            :providers="approvedProviders"
            :loading="loadingStates.approved"
            @approve="handleApprove"
            @reject="handleReject"
            status-to-show="APPROVED"
          />
        </el-tab-pane>
        <el-tab-pane label="已拒绝资质" name="rejected">
           <ProviderTable
            :providers="rejectedProviders"
            :loading="loadingStates.rejected"
            @approve="handleApprove"
            @reject="handleReject"
            status-to-show="REJECTED"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue';
import { ElTabs, ElTabPane, ElCard, ElMessage, ElMessageBox, ElIcon } from 'element-plus';
import { Shop } from '@element-plus/icons-vue';
import type { TabsPaneContext } from 'element-plus';
import ProviderTable from './components/ProviderTable.vue';
import {
  getAllProviders, // 用于 'all' 标签页
  getPendingProviders, // 用于 'pending' 标签页
  getApprovedProviders, // 用于 'approved' 标签页
  getRejectedProviders, // 用于 'rejected' 标签页
  approveProvider as apiApproveProvider,
  rejectProvider as apiRejectProvider,
  type ProviderDTO,
} from '@/api/admin';
import type { BackendResult } from '@/types/api';

type ProviderTabName = 'pending' | 'all' | 'approved' | 'rejected';

const activeTabName = ref<ProviderTabName>('pending'); // 默认显示待审核

const allProviders = ref<ProviderDTO[]>([]);
const pendingProviders = ref<ProviderDTO[]>([]);
const approvedProviders = ref<ProviderDTO[]>([]);
const rejectedProviders = ref<ProviderDTO[]>([]);

const loadingStates = reactive<Record<ProviderTabName, boolean>>({
  all: false,
  pending: false,
  approved: false,
  rejected: false,
});

const fetchData = async (tabName: ProviderTabName) => {
  if (loadingStates[tabName]) return;
  loadingStates[tabName] = true;

  let fetchFunction;
  let targetRef;

  switch (tabName) {
    case 'all':
      fetchFunction = getAllProviders;
      targetRef = allProviders;
      break;
    case 'pending':
      fetchFunction = getPendingProviders;
      targetRef = pendingProviders;
      break;
    case 'approved':
      fetchFunction = getApprovedProviders;
      targetRef = approvedProviders;
      break;
    case 'rejected':
      fetchFunction = getRejectedProviders;
      targetRef = rejectedProviders;
      break;
    default:
      return;
  }

  try {
    const res: BackendResult<ProviderDTO[]> = await fetchFunction();
    if (res.code === 200 && res.data) {
      targetRef.value = res.data;
    } else {
      ElMessage.error(res.message || `获取服务商列表 (${tabName}) 失败`);
    }
  } catch (error: any) {
    ElMessage.error(error.message || `请求服务商列表 (${tabName}) 失败`);
  } finally {
    loadingStates[tabName] = false;
  }
};

const handleTabClick = (tab: TabsPaneContext) => {
  const tabName = tab.paneName as ProviderTabName;
  if (tabName) {
    activeTabName.value = tabName;
    // 切换时总是重新获取数据，或者根据需要判断是否已加载
    fetchData(tabName);
  }
};

const handleApprove = async (provider: ProviderDTO) => {
  try {
    await ElMessageBox.confirm(`确定要批准服务商 "${provider.name}" 的资质吗？`, '确认批准', {
      confirmButtonText: '批准',
      cancelButtonText: '取消',
      type: 'success',
    });
    const res = await apiApproveProvider(provider.id);
    if (res.code === 200) {
      ElMessage.success(`服务商 "${provider.name}" 已批准`);
      // 刷新当前列表和可能相关的列表
      fetchData(activeTabName.value);
      if (activeTabName.value !== 'approved') fetchData('approved');
      if (activeTabName.value !== 'all') fetchData('all');
    } else {
      ElMessage.error(res.message || '批准失败');
    }
  } catch (action) {
    if (action !== 'confirm') ElMessage.info('操作已取消');
  }
};

const handleReject = async (provider: ProviderDTO) => {
  try {
    const { value: reason } = await ElMessageBox.prompt(
      `请输入拒绝服务商 "${provider.name}" 的原因：`,
      '确认拒绝',
      {
        confirmButtonText: '确定拒绝',
        cancelButtonText: '取消',
        type: 'warning',
        inputPlaceholder: '拒绝原因 (必填)',
        inputValidator: (val) => (val && val.trim().length > 0) || '拒绝原因不能为空',
      }
    );

    // 'value' will be the input string if confirm is clicked
    // If cancel is clicked, an error is thrown by ElMessageBox which is caught by 'catch'
    const res = await apiRejectProvider(provider.id, reason); // reason is now guaranteed to be a non-empty string
    if (res.code === 200) {
      ElMessage.success(`服务商 "${provider.name}" 已拒绝，原因: ${reason}`);
      fetchData(activeTabName.value);
      if (activeTabName.value !== 'rejected') fetchData('rejected');
      if (activeTabName.value !== 'all') fetchData('all');
    } else {
      ElMessage.error(res.message || '拒绝失败');
    }
  } catch (action) {
    // Only 'cancel' or if prompt is closed by ESC/overlay click will lead to 'cancel' string in action
    if (action === 'cancel' || action === 'close') {
       ElMessage.info('操作已取消');
    } else {
        // This part might not be reached if inputValidator works as expected,
        // or if apiRejectProvider throws an error that's not an instance of 'cancel'/'close'
        console.error("Rejection error or unexpected action:", action);
    }
  }
};

onMounted(() => {
  fetchData(activeTabName.value);
});
</script>

<style scoped>
.admin-provider-list-container {
  padding: 20px;
}
.card-header {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}
.card-header .el-icon {
  margin-right: 8px;
}
</style>