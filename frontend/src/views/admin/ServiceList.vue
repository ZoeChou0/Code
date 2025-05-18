<template>
  <div class="service-list-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>服务项目管理与审核</span>
          <el-button :icon="Refresh" @click="refreshData" :loading="loading" circle title="刷新列表"></el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="待审核" name="pending">
          <ServiceTable :services="pendingServices" :loading="loading" @approve="handleApprove" @reject="handleReject" />
          <el-empty v-if="!loading && pendingServices.length === 0" description="暂无待审核的服务"></el-empty>
        </el-tab-pane>
        <el-tab-pane label="全部服务" name="all">
          <ServiceTable :services="allServices" :loading="loading" @approve="handleApprove" @reject="handleReject" />
           <el-empty v-if="!loading && allServices.length === 0" description="暂无任何服务项目"></el-empty>
        </el-tab-pane>
         </el-tabs>

    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { ElTabs, ElTabPane, ElCard, ElButton, ElIcon, ElMessage, ElMessageBox, ElEmpty } from 'element-plus';
import { Refresh } from '@element-plus/icons-vue';
// 导入 Service 类型和 API 函数
import type { Service } from '@/api/service';
import {
  getAllServicesForAdmin,
  getPendingServicesForAdmin,
  approveService,
  rejectService
} from '@/api/service'; // 假设你把 API 函数放在 service.ts 里了

// 导入子组件 (我们将表格封装一下)
import ServiceTable from './components/ServiceTable.vue'; // 假设表格在同目录下的 components 文件夹

const loading = ref(false);
const activeTab = ref('pending'); // 默认显示待审核
const allServices = ref<Service[]>([]);
const pendingServices = ref<Service[]>([]);
// const approvedServices = ref<Service[]>([]); // 如果需要单独加载
// const rejectedServices = ref<Service[]>([]); // 如果需要单独加载

// --- 数据获取 ---
const fetchAllServices = async () => {
  if (loading.value) return;
  loading.value = true;
  try {
    const res = await getAllServicesForAdmin();
    if (res.code === 200 && Array.isArray(res.data)) {
      allServices.value = res.data;
    } else {
      ElMessage.error(res.message || '获取全部服务列表失败');
    }
  } catch (error: any) {
    console.error("获取全部服务列表出错:", error);
    ElMessage.error('获取全部服务列表请求失败: ' + (error?.message || '请检查网络'));
  } finally {
    // 只有当活动 Tab 是 'all' 时才停止加载，避免干扰其他 Tab 的加载状态
    if (activeTab.value === 'all') {
       loading.value = false;
    }
  }
};

const fetchPendingServices = async () => {
   if (loading.value) return;
   loading.value = true;
  try {
    const res = await getPendingServicesForAdmin();
    if (res.code === 200 && Array.isArray(res.data)) {
      pendingServices.value = res.data;
    } else {
      ElMessage.error(res.message || '获取待审核服务列表失败');
    }
  } catch (error: any) {
     console.error("获取待审核服务列表出错:", error);
     ElMessage.error('获取待审核服务列表请求失败: ' + (error?.message || '请检查网络'));
  } finally {
     if (activeTab.value === 'pending') {
        loading.value = false;
     }
  }
};

// --- Tab 切换处理 ---
const handleTabClick = (tab: any) => {
  const tabName = tab.props.name;
  console.log('切换到 Tab:', tabName);
  if (tabName === 'pending') {
    fetchPendingServices();
  } else if (tabName === 'all') {
    fetchAllServices();
  }
  // else if (tabName === 'approved') { fetchApprovedServices(); }
  // else if (tabName === 'rejected') { fetchRejectedServices(); }
};

// --- 刷新当前 Tab 数据 ---
const refreshData = () => {
  if (activeTab.value === 'pending') {
    fetchPendingServices();
  } else if (activeTab.value === 'all') {
    fetchAllServices();
  }
   // ... 刷新其他 tab ...
};

// --- 审核操作处理 ---
const handleApprove = async (serviceId: number) => {
   try {
    await ElMessageBox.confirm(
      `确定要批准服务项 (ID: ${serviceId}) 吗？`,
      '确认批准',
      { confirmButtonText: '批准', cancelButtonText: '取消', type: 'success' }
    );
    loading.value = true;
    const res = await approveService(serviceId);
    if (res.code === 200) {
      ElMessage.success('服务批准成功');
      // 批准后刷新数据
      refreshData();
      // 如果 'all' 列表已加载，也可以尝试在本地更新状态，避免重新请求 all
      const indexInAll = allServices.value.findIndex(s => s.id === serviceId);
      if (indexInAll !== -1) {
          allServices.value[indexInAll].reviewStatus = 'APPROVED';
          allServices.value[indexInAll].rejectionReason = undefined; // 清空拒绝原因
      }
    } else {
       ElMessage.error(res.message || '批准失败');
    }
  } catch (error: any) {
     if (error !== 'cancel') {
       console.error("批准服务时出错:", error);
       ElMessage.error('批准失败: ' + (error?.message || '请稍后重试'));
     } else {
       ElMessage.info('已取消批准');
     }
  } finally {
      // 确保在所有情况下都停止加载，即使批准的是 all 列表中的项
      loading.value = false;
  }
};

const handleReject = async (serviceId: number) => {
   try {
    // 弹出输入框让管理员填写原因
    const { value: reason } = await ElMessageBox.prompt(
      `请输入拒绝服务项 (ID: ${serviceId}) 的原因：`,
      '确认拒绝',
      {
        confirmButtonText: '确认拒绝',
        cancelButtonText: '取消',
        inputType: 'textarea', // 使用文本域输入
        inputPlaceholder: '请填写拒绝原因',
        inputValidator: (val) => { // 简单的非空验证
          if (!val || val.trim().length === 0) {
            return '拒绝原因不能为空';
          }
          return true;
        }
      }
    );

    // 用户点击了确认并输入了原因
     loading.value = true;
    const res = await rejectService(serviceId, reason.trim()); // 调用拒绝 API，传入原因
    if (res.code === 200) {
      ElMessage.success('服务已拒绝');
      refreshData(); // 刷新列表
      // 更新 allServices 列表中的状态 (如果已加载)
       const indexInAll = allServices.value.findIndex(s => s.id === serviceId);
       if (indexInAll !== -1) {
           allServices.value[indexInAll].reviewStatus = 'REJECTED';
           allServices.value[indexInAll].rejectionReason = reason.trim(); // 更新拒绝原因
       }
    } else {
       ElMessage.error(res.message || '拒绝失败');
    }
  } catch (error: any) {
     if (error !== 'cancel' && error?.code !== 'cancel') { // ElMessageBox 的 prompt 取消可能返回特定对象
       console.error("拒绝服务时出错:", error);
       // 检查是否是因为验证失败（用户没输入原因就点了确定）
        if (error?.message && error.message.includes('validation failed')) {
             // ElMessageBox 的 validator 已经提示了，这里可以不用再弹
             console.warn('Reject reason validation failed.');
        } else {
             ElMessage.error('拒绝失败: ' + (error?.message || '请稍后重试'));
        }
     } else {
       ElMessage.info('已取消拒绝');
     }
  } finally {
     loading.value = false;
  }
};

// --- 组件挂载时加载默认 Tab 数据 ---
onMounted(() => {
  if (activeTab.value === 'pending') {
    fetchPendingServices();
  } else if (activeTab.value === 'all') {
    fetchAllServices();
  }
  // ... 加载其他 tab 初始数据 ...
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