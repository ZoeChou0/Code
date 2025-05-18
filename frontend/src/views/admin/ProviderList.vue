<template>
  <div class="provider-list-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>服务商资质审核</span>
           <el-button :icon="Refresh" @click="fetchPending" :loading="loading" circle title="刷新待审核列表"></el-button>
          </div>
      </template>

      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="待审核" name="pending">
          <el-table :data="pendingProviders" v-loading="loading" style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="名称" width="150" />
            <el-table-column prop="email" label="邮箱" width="200" />
            <el-table-column prop="phone" label="手机号" width="150" />
            <el-table-column prop="qualificationStatus" label="当前状态" width="120">
              <template #default="{ row }">
                <el-tag type="warning">{{ formatQualificationStatus(row.qualificationStatus) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" align="center" fixed="right">
               <template #default="{ row }">
                 <el-button size="small" type="success" :icon="CircleCheck" @click="handleApprove(row.id)">批准</el-button>
                 <el-button size="small" type="danger" :icon="CircleClose" @click="handleReject(row.id)">拒绝</el-button>
                 </template>
            </el-table-column>
          </el-table>
           <el-empty v-if="!loading && pendingProviders.length === 0" description="暂无待审核的服务商"></el-empty>
        </el-tab-pane>
         <el-tab-pane label="全部服务商" name="all">
            <p>显示所有服务商的功能待实现...</p>
         </el-tab-pane>
          <el-tab-pane label="已批准" name="approved">
             <p>显示已批准服务商的功能待实现...</p>
         </el-tab-pane>
          <el-tab-pane label="已拒绝" name="rejected">
             <p>显示已拒绝服务商的功能待实现...</p>
         </el-tab-pane>
      </el-tabs>

    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElTable, ElTableColumn, ElButton, ElCard, ElIcon, ElTag, ElMessage, ElMessageBox, ElTabs, ElTabPane, ElEmpty } from 'element-plus';
import { Refresh, CircleCheck, CircleClose, View } from '@element-plus/icons-vue';
import type { UserInfo } from '@/api/user'; // 确保 UserInfo 包含 qualificationStatus
// 导入新的 API 函数
import { getPendingProviders, approveProvider, rejectProvider } from '@/api/user'; // 或 @/api/admin

const pendingProviders = ref<UserInfo[]>([]);
const loading = ref(false);
const activeTab = ref('pending'); // 默认显示待审核

// 获取待审核服务商列表
const fetchPending = async () => {
  if (loading.value) return; // 防止重复加载
  loading.value = true;
  try {
    const res = await getPendingProviders();
    if (res.code === 200 && Array.isArray(res.data)) {
      pendingProviders.value = res.data;
    } else {
      ElMessage.error(res.message || '获取待审核列表失败');
      pendingProviders.value = []; // 清空以防显示旧数据
    }
  } catch (error: any) {
    console.error("获取待审核列表出错:", error);
    ElMessage.error('获取待审核列表失败: ' + (error?.message || '请检查网络连接'));
     pendingProviders.value = [];
  } finally {
    loading.value = false;
  }
};

// 处理批准操作
const handleApprove = async (providerId: number) => {
   try {
    await ElMessageBox.confirm(
      `确定要批准服务商 (ID: ${providerId}) 的资质吗？`,
      '确认批准',
      { confirmButtonText: '批准', cancelButtonText: '取消', type: 'success' }
    );
    loading.value = true; // 开始加载
    const res = await approveProvider(providerId); // 调用批准 API
    if (res.code === 200) {
      ElMessage.success('资质批准成功');
      // 批准成功后从待审核列表中移除，或者刷新列表
      // 方式一：移除
      pendingProviders.value = pendingProviders.value.filter(p => p.id !== providerId);
      // 方式二：刷新
      // await fetchPending();
    } else {
      ElMessage.error(res.message || '批准失败');
    }
  } catch (error: any) {
     if (error !== 'cancel') {
       console.error("批准资质时出错:", error);
       ElMessage.error('批准失败: ' + (error?.message || '请稍后重试'));
     } else {
       ElMessage.info('已取消批准');
     }
  } finally {
     loading.value = false; // 结束加载
  }
};

// 处理拒绝操作
const handleReject = async (providerId: number) => {
   try {
    // 如果后端需要拒绝原因，这里需要弹框输入 ElMessageBox.prompt(...)
    await ElMessageBox.confirm(
      `确定要拒绝服务商 (ID: ${providerId}) 的资质申请吗？`,
      '确认拒绝',
      { confirmButtonText: '拒绝', cancelButtonText: '取消', type: 'warning' }
    );
    loading.value = true;
    // const reason = promptResult.value; // 如果用了 prompt
    // const res = await rejectProvider(providerId, reason); // 如果需要原因
    const res = await rejectProvider(providerId); // 调用拒绝 API
    if (res.code === 200) {
      ElMessage.success('资质已拒绝');
      // 拒绝成功后从待审核列表中移除或刷新
       pendingProviders.value = pendingProviders.value.filter(p => p.id !== providerId);
      // await fetchPending();
    } else {
      ElMessage.error(res.message || '拒绝失败');
    }
  } catch (error: any) {
     if (error !== 'cancel') {
       console.error("拒绝资质时出错:", error);
       ElMessage.error('拒绝失败: ' + (error?.message || '请稍后重试'));
     } else {
       ElMessage.info('已取消拒绝');
     }
  } finally {
     loading.value = false;
  }
};

// (可以添加) 处理 Tab 点击事件，用于加载不同状态的服务商列表
const handleTabClick = (tab: any) => {
  console.log('切换到 Tab:', tab.props.name);
  if (tab.props.name === 'pending') {
    fetchPending();
  } else if (tab.props.name === 'all') {
     // fetchAllProviders(); // 需要实现获取所有服务商的 API 和逻辑
     ElMessage.info('加载所有服务商功能待实现');
  }
   // ... 处理其他 Tab
};


// 格式化资质状态显示
const formatQualificationStatus = (status: string | undefined | null): string => {
  if (!status) return '未申请/未知';
  switch (status) {
    case 'PENDING_REVIEW': return '待审核';
    case 'APPROVED': return '已认证';
    case 'REJECTED': return '已拒绝';
    default: return status;
  }
};

// 组件挂载时默认加载待审核列表
onMounted(() => {
  fetchPending();
});

</script>

<style scoped>
.provider-list-container {
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
.el-button + .el-button {
    margin-left: 8px;
}
</style>