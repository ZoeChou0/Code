<template>
  <div class="service-list-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>服务项目管理与审核</span>
          <el-button :icon="Refresh" @click="refreshData" :loading="loading.global" circle title="刷新列表"></el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="待审核" name="pending">
          <ServiceTable :services="pendingServices" :loading="loading.pending" @approve="handleApprove" @reject="handleReject" />
          <el-empty v-if="!loading.pending && pendingServices.length === 0" description="暂无待审核的服务"></el-empty>
        </el-tab-pane>
        <el-tab-pane label="全部服务" name="all">
          <ServiceTable :services="allServices" :loading="loading.all" @approve="handleApprove" @reject="handleReject" />
           <el-empty v-if="!loading.all && allServices.length === 0" description="暂无任何服务项目"></el-empty>
        </el-tab-pane>
         </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'; // 移除了 computed，因为直接操作 ref 数组
import { ElTabs, ElTabPane, ElCard, ElButton, ElMessage, ElMessageBox, ElEmpty } from 'element-plus'; // ElIcon 已在 ServiceTable 中使用或由 ElButton 自动处理
import { Refresh } from '@element-plus/icons-vue';
import type { Service as ApiServiceType } from '@/api/service'; // 统一使用 ApiServiceType
import {
  getAllServicesForAdmin,
  getPendingServicesForAdmin,
  approveService,
  rejectService
} from '@/api/service';

import ServiceTable from './components/ServiceTable.vue'; // 确保路径正确

// 为不同的列表使用不同的加载状态
const loading = ref({
  global: false, // 用于刷新按钮和批准/拒绝操作
  pending: false,
  all: false,
});

const activeTab = ref('pending');
const allServices = ref<ApiServiceType[]>([]);
const pendingServices = ref<ApiServiceType[]>([]);

const fetchAllServices = async () => {
  if (loading.value.all) return; // 防止重复加载
  loading.value.all = true;
  loading.value.global = true;
  try {
    const res = await getAllServicesForAdmin();
    if (res.code === 200 && Array.isArray(res.data)) {
      allServices.value = res.data;
      console.log('Data received in AdminServiceList:', allServices.value); 
    } else {
      ElMessage.error(res.message || '获取全部服务列表失败');
      allServices.value = []; // 出错时清空
    }
  } catch (error: any) {
    console.error("获取全部服务列表出错:", error);
    ElMessage.error('获取全部服务列表请求失败: ' + (error?.response?.data?.message || error?.message || '请检查网络'));
    allServices.value = []; // 出错时清空
  } finally {
    loading.value.all = false;
    loading.value.global = false;
  }
};

const fetchPendingServices = async () => {
   if (loading.value.pending) return; // 防止重复加载
   loading.value.pending = true;
   loading.value.global = true;
  try {
    const res = await getPendingServicesForAdmin();
    if (res.code === 200 && Array.isArray(res.data)) {
      pendingServices.value = res.data;
      console.log('Data received in AdminServiceList:', pendingServices.value);
    } else {
      ElMessage.error(res.message || '获取待审核服务列表失败');
      pendingServices.value = []; // 出错时清空
    }
  } catch (error: any) {
     console.error("获取待审核服务列表出错:", error);
     ElMessage.error('获取待审核服务列表请求失败: ' + (error?.response?.data?.message || error?.message || '请检查网络'));
     pendingServices.value = []; // 出错时清空
  } finally {
     loading.value.pending = false;
     loading.value.global = false;
  }
};

const handleTabClick = (tab: any) => {
  const tabName = tab.props.name;
  activeTab.value = tabName; // 确保 activeTab 被更新
  if (tabName === 'pending') {
    if (pendingServices.value.length === 0 || !loading.value.pending) { // 仅当列表为空或未在加载时获取
        fetchPendingServices();
    }
  } else if (tabName === 'all') {
    if (allServices.value.length === 0 || !loading.value.all) { // 仅当列表为空或未在加载时获取
        fetchAllServices();
    }
  }
};

const refreshData = () => {
  if (activeTab.value === 'pending') {
    fetchPendingServices();
  } else if (activeTab.value === 'all') {
    fetchAllServices();
  }
};

const handleApprove = async (serviceId: number) => {
   try {
    await ElMessageBox.confirm(
      `确定要批准服务项 (ID: ${serviceId}) 吗？`,
      '确认批准',
      { confirmButtonText: '批准', cancelButtonText: '取消', type: 'success' }
    );
    loading.value.global = true; // 使用全局loading
    const res = await approveService(serviceId);
    if (res.code === 200) {
      ElMessage.success('服务批准成功');
      // 从待审核列表中移除
      pendingServices.value = pendingServices.value.filter(s => s.id !== serviceId);
      // 更新 allServices 列表中的状态
      const indexInAll = allServices.value.findIndex(s => s.id === serviceId);
      if (indexInAll !== -1) {
          allServices.value[indexInAll].reviewStatus = 'APPROVED'; // 确保状态字符串与后端一致
          allServices.value[indexInAll].rejectionReason = undefined;
      }
      // 如果当前就在“全部服务”页，且allServices为空，则刷新一下以显示批准后的项
      if (activeTab.value === 'all' && allServices.value.length === 0) {
          fetchAllServices();
      }

    } else {
       ElMessage.error(res.message || '批准失败');
    }
  } catch (error: any) {
     if (error !== 'cancel' && error?.code !== 'cancel') { // 处理 ElMessageBox 取消操作
       console.error("批准服务时出错:", error);
       ElMessage.error('批准操作失败: ' + (error?.response?.data?.message || error?.message || '请稍后重试'));
     } else {
       ElMessage.info('已取消批准');
     }
  } finally {
      loading.value.global = false;
  }
};

const handleReject = async (serviceId: number) => {
   try {
    const { value: reason } = await ElMessageBox.prompt(
      `请输入拒绝服务项 (ID: ${serviceId}) 的原因：`,
      '确认拒绝',
      {
        confirmButtonText: '确认拒绝',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputPlaceholder: '请填写拒绝原因',
        inputValidator: (val) => {
          if (!val || val.trim().length === 0) {
            return '拒绝原因不能为空';
          }
          if (val.trim().length > 200) { // 示例：原因长度限制
            return '拒绝原因不能超过200个字符';
          }
          return true;
        }
      }
    );

    loading.value.global = true;
    const res = await rejectService(serviceId, reason.trim());
    if (res.code === 200) {
      ElMessage.success('服务已拒绝');
      pendingServices.value = pendingServices.value.filter(s => s.id !== serviceId);
       const indexInAll = allServices.value.findIndex(s => s.id === serviceId);
       if (indexInAll !== -1) {
           allServices.value[indexInAll].reviewStatus = 'REJECTED'; // 确保状态字符串与后端一致
           allServices.value[indexInAll].rejectionReason = reason.trim();
       }
        if (activeTab.value === 'all' && allServices.value.length === 0) {
          fetchAllServices();
      }
    } else {
       ElMessage.error(res.message || '拒绝失败');
    }
  } catch (error: any) {
     if (error !== 'cancel' && error?.code !== 'cancel') {
       console.error("拒绝服务时出错:", error);
        if (error?.message && error.message.includes('validation failed')) {
             console.warn('Reject reason validation failed.');
        } else {
             ElMessage.error('拒绝操作失败: ' + (error?.response?.data?.message || error?.message || '请稍后重试'));
        }
     } else {
       ElMessage.info('已取消拒绝');
     }
  } finally {
     loading.value.global = false;
  }
};

onMounted(() => {
  // 初始加载当前激活的 Tab，默认为 'pending'
  if (activeTab.value === 'pending') {
    fetchPendingServices();
  } else {
    // 如果初始不是pending，可以加载all，或者根据具体逻辑
    fetchAllServices();
  }
});

</script>

<style scoped>
.service-list-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.el-table {
  margin-top: 15px;
}
</style>