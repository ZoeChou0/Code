<!-- <template>
  <div class="user-list-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>用户列表管理</span>
          <el-input v-model="searchQuery" placeholder="按名称或邮箱搜索" clearable style="width: 240px; margin-right: 10px;"
            :prefix-icon="Search" @clear="fetchUsers" @keyup.enter="fetchUsers" />
          <el-button :icon="Refresh" @click="fetchUsers" :loading="loading" circle></el-button>
        </div>
      </template>

      <el-table :data="filteredUsers" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" sortable />
        <el-table-column prop="name" label="用户名" width="150" sortable />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column prop="role" label="角色" width="100" sortable>
          <template #default="scope">
            <el-tag :type="getRoleTagType(scope.row.role)">
              {{ formatRole(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="qualificationStatus" label="服务商资质" width="150">
          <template #default="scope">
            <span v-if="scope.row.role === 'provider'">
              {{ formatQualificationStatus(scope.row.qualificationStatus) || '未申请' }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="scope">
            <el-button size="small" :icon="View" @click="handleViewDetails(scope.row)">查看详情</el-button>
            <el-button size="small" type="danger" :icon="Delete" @click="handleDeleteUser(scope.row.id)"
              disabled>删除</el-button>
          </template>
        </el-table-column>
      </el-table>

    </el-card>

    <el-dialog v-model="detailsDialogVisible" title="用户详情" width="500px">
      <div v-if="selectedUser">
        <p><strong>ID:</strong> {{ selectedUser.id }}</p>
        <p><strong>用户名:</strong> {{ selectedUser.name }}</p>
        <p><strong>邮箱:</strong> {{ selectedUser.email }}</p>
        <p><strong>手机号:</strong> {{ selectedUser.phone }}</p>
        <p><strong>角色:</strong> {{ formatRole(selectedUser.role) }}</p>
        <p v-if="selectedUser.role === 'provider'"><strong>服务商资质:</strong> {{
          formatQualificationStatus(selectedUser.qualificationStatus) || '未申请' }}</p>
      </div>
      <template #footer>
        <el-button @click="detailsDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { ElTable, ElTableColumn, ElButton, ElCard, ElIcon, ElInput, ElTag, ElDialog, ElMessage, ElMessageBox } from 'element-plus';
import { Refresh, Search, View, Delete } from '@element-plus/icons-vue';
import { getAllUsers } from '@/api/user'; // 引入将要添加的 API 函数
import type { UserInfo } from '@/api/user'; // 使用 UserInfo 类型 (或创建一个更完整的 User 类型)

const allUsers = ref<UserInfo[]>([]); // 存储从后端获取的所有用户
const loading = ref(false);
const searchQuery = ref(''); // 搜索关键词

// --- 详情对话框 ---
const detailsDialogVisible = ref(false);
const selectedUser = ref<UserInfo | null>(null);

// 计算属性，用于根据搜索词过滤用户
const filteredUsers = computed(() => {
  if (!searchQuery.value) {
    return allUsers.value;
  }
  const lowerQuery = searchQuery.value.toLowerCase();
  return allUsers.value.filter(user =>
    user.name?.toLowerCase().includes(lowerQuery) ||
    user.email?.toLowerCase().includes(lowerQuery)
  );
});

// 获取所有用户数据
const fetchUsers = async () => {
  loading.value = true;
  try {
    const res = await getAllUsers(); // 调用 API 获取数据
    if (res.code === 200) {
      allUsers.value = res.data || [];
    } else {
      ElMessage.error(res.message || '获取用户列表失败');
      allUsers.value = [];
    }
  } catch (error: any) {
    console.error("获取用户列表出错:", error);
    ElMessage.error('获取用户列表失败: ' + (error?.message || '请检查网络连接'));
    allUsers.value = [];
  } finally {
    loading.value = false;
  }
};

// --- 操作处理 ---

const handleViewDetails = (user: UserInfo) => {
  selectedUser.value = user;
  detailsDialogVisible.value = true;
};

const handleDeleteUser = (userId: number) => {
  ElMessageBox.confirm(
    `确定要删除用户 ID: ${userId} 吗？此操作通常不可逆！`,
    '警告',
    {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      // 调用后端删除用户的 API (需要先在 api/user.ts 和后端实现)
      ElMessage.warning('删除用户功能尚未实现');
      // try {
      //   await deleteUserApi(userId);
      //   ElMessage.success('用户删除成功');
      //   fetchUsers(); // Refresh list
      // } catch (error) {
      //    ElMessage.error('删除用户失败');
      // }
    })
    .catch(() => {
      ElMessage.info('已取消删除');
    });
};

// --- 格式化函数 ---

const formatRole = (role: string | undefined): string => {
  if (!role) return '未知';
  switch (role.toLowerCase()) {
    case 'user': return '普通用户';
    case 'provider': return '服务商';
    case 'admin': return '管理员';
    default: return role;
  }
};

const getRoleTagType = (role: string | undefined): ('success' | 'warning' | 'danger' | 'info') => {
  if (!role) return 'info';
  switch (role.toLowerCase()) {
    case 'user': return 'success';
    case 'provider': return 'warning';
    case 'admin': return 'danger';
    default: return 'info';
  }
};

const formatQualificationStatus = (status: string | undefined | null): string => {
  if (!status) return '';
  switch (status) {
    case 'PENDING_REVIEW': return '待审核';
    case 'APPROVED': return '已认证';
    case 'REJECTED': return '已拒绝';
    default: return status;
  }
};


// 组件挂载时获取用户数据
onMounted(() => {
  fetchUsers();
});
</script>

<style scoped>
.user-list-container {
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

.el-button+.el-button {
  margin-left: 8px;
}
</style> -->
<template>
  <div class="user-list-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>用户列表管理</span>
          <el-input v-model="searchQuery" placeholder="按名称或邮箱搜索" clearable style="width: 240px; margin-right: 10px;"
            :prefix-icon="Search" @clear="fetchUsers" @keyup.enter="fetchUsers" />
          <el-button :icon="Refresh" @click="fetchUsers" :loading="loading" circle></el-button>
        </div>
      </template>

      <el-table :data="filteredUsers" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" sortable />
        <el-table-column prop="name" label="用户名" width="150" sortable />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column prop="role" label="角色" width="100" sortable>
          <template #default="scope">
            <el-tag :type="getRoleTagType(scope.row.role)">
              {{ formatRole(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
           <template #default="{ row }">
             <el-tag :type="row.status === 'banned' ? 'danger' : 'success'" disable-transitions>
               {{ formatStatus(row.status) }}
             </el-tag>
           </template>
        </el-table-column>
        <el-table-column prop="qualificationStatus" label="服务商资质" width="150">
           <template #default="scope">
            <span v-if="scope.row.role === 'provider'">
              {{ formatQualificationStatus(scope.row.qualificationStatus) || '未申请' }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
             <el-button size="small" :icon="View" @click="handleViewDetails(row)" style="margin-right: 5px;">详情</el-button>

            <el-button
              v-if="row.status !== 'banned' && row.role !== 'admin'"
              size="small"
              type="warning"
              :icon="CircleClose"
              @click="handleBanUser(row)"
              :disabled="row.id === currentUserInfo?.id" style="margin-right: 5px;">
              禁用
            </el-button>

            <el-button
              v-if="row.status === 'banned'"
              size="small"
              type="success"
              :icon="CircleCheck"
              @click="handleUnbanUser(row)"
              style="margin-right: 5px;">
              解禁
            </el-button>

             <el-button
              size="small"
              type="danger"
              :icon="Delete"
              @click="handleDeleteUser(row.id)"
              :disabled="row.role === 'admin' || row.id === currentUserInfo?.id"> 删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

    </el-card>

     <el-dialog v-model="detailsDialogVisible" title="用户详情" width="500px">
      <div v-if="selectedUser">
        <p><strong>ID:</strong> {{ selectedUser.id }}</p>
        <p><strong>用户名:</strong> {{ selectedUser.name }}</p>
        <p><strong>邮箱:</strong> {{ selectedUser.email }}</p>
        <p><strong>手机号:</strong> {{ selectedUser.phone }}</p>
        <p><strong>角色:</strong> {{ formatRole(selectedUser.role) }}</p>
         <p><strong>状态:</strong> <el-tag :type="selectedUser.status === 'banned' ? 'danger' : 'success'">{{ formatStatus(selectedUser.status) }}</el-tag></p>
        <p v-if="selectedUser.role === 'provider'"><strong>服务商资质:</strong> {{
          formatQualificationStatus(selectedUser.qualificationStatus) || '未申请' }}</p>
      </div>
      <template #footer>
        <el-button @click="detailsDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
// --- 导入需要的组件和图标 ---
import { ElTable, ElTableColumn, ElButton, ElCard, ElIcon, ElInput, ElTag, ElDialog, ElMessage, ElMessageBox } from 'element-plus';
import { Refresh, Search, View, Delete, CircleClose, CircleCheck } from '@element-plus/icons-vue'; // 导入新图标
// --- 导入 API 函数和类型 ---
import { getAllUsers, banUser, unbanUser } from '@/api/user'; // 导入新 API 函数 (假设在 user.ts)
import type { UserInfo } from '@/api/user';
import { useUserStore } from '@/stores/user'; // 导入用户 store

const allUsers = ref<UserInfo[]>([]);
const loading = ref(false);
const searchQuery = ref('');
const detailsDialogVisible = ref(false);
const selectedUser = ref<UserInfo | null>(null);
const userStore = useUserStore(); // 获取用户 store 实例
const currentUserInfo = computed(() => userStore.userInfo); // 获取当前登录用户信息

const filteredUsers = computed(() => {
  // ... 过滤逻辑不变 ...
  if (!searchQuery.value) {
    return allUsers.value;
  }
  const lowerQuery = searchQuery.value.toLowerCase();
  return allUsers.value.filter(user =>
    user.name?.toLowerCase().includes(lowerQuery) ||
    user.email?.toLowerCase().includes(lowerQuery)
  );
});

const fetchUsers = async () => {
  // ... 获取用户列表逻辑不变 ...
   loading.value = true;
  try {
    const res = await getAllUsers();
    if (res.code === 200 && Array.isArray(res.data)) {
      // 确保 status 字段被正确接收
      allUsers.value = res.data.map(user => ({
        ...user,
        status: user.status || 'active' // 如果后端没返回 status，默认设为 active
      }));
    } else {
      ElMessage.error(res.message || '获取用户列表失败');
      allUsers.value = [];
    }
  } catch (error: any) {
    console.error("获取用户列表出错:", error);
    ElMessage.error('获取用户列表失败: ' + (error?.message || '请检查网络连接'));
    allUsers.value = [];
  } finally {
    loading.value = false;
  }
};

const handleViewDetails = (user: UserInfo) => {
  // ... 查看详情逻辑不变 ...
   selectedUser.value = user;
  detailsDialogVisible.value = true;
};

// --- 新增：禁用用户处理 ---
const handleBanUser = async (user: UserInfo) => {
  if (user.role === 'admin') {
     ElMessage.warning('不能禁用管理员账户');
     return;
  }
   if (user.id === currentUserInfo.value?.id) {
     ElMessage.warning('不能禁用自己');
     return;
  }
  try {
    await ElMessageBox.confirm(
      `确定要禁用用户 "${user.name}" (ID: ${user.id}) 吗？`,
      '确认禁用',
      {
        confirmButtonText: '确定禁用',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );
    // 用户点击了确定
    loading.value = true; // 开始加载状态
    const res = await banUser(user.id); // 调用禁用 API
    if (res.code === 200) {
      ElMessage.success('用户禁用成功');
      // 更新列表中的用户状态（比重新拉取列表体验更好）
      const index = allUsers.value.findIndex(u => u.id === user.id);
      if (index !== -1) {
        allUsers.value[index].status = 'banned';
      }
      // 或者重新拉取列表: await fetchUsers();
    } else {
      ElMessage.error(res.message || '禁用用户失败');
    }
  } catch (error: any) {
    if (error !== 'cancel') {
       console.error("禁用用户时出错:", error);
       ElMessage.error('禁用用户失败: ' + (error?.message || '请稍后重试'));
    } else {
       ElMessage.info('已取消禁用');
    }
  } finally {
     loading.value = false; // 结束加载状态
  }
};

// --- 新增：解禁用户处理 ---
const handleUnbanUser = async (user: UserInfo) => {
  try {
    await ElMessageBox.confirm(
      `确定要解禁用户 "${user.name}" (ID: ${user.id}) 吗？`,
      '确认解禁',
      {
        confirmButtonText: '确定解禁',
        cancelButtonText: '取消',
        type: 'success', // 可以用 success 类型
      }
    );
    // 用户点击了确定
     loading.value = true;
    const res = await unbanUser(user.id); // 调用解禁 API
    if (res.code === 200) {
      ElMessage.success('用户解禁成功');
      // 更新列表中的用户状态
       const index = allUsers.value.findIndex(u => u.id === user.id);
      if (index !== -1) {
        allUsers.value[index].status = 'active'; // 假设解禁后状态是 'active'
      }
      // 或者重新拉取列表: await fetchUsers();
    } else {
      ElMessage.error(res.message || '解禁用户失败');
    }
  } catch (error: any) {
     if (error !== 'cancel') {
       console.error("解禁用户时出错:", error);
       ElMessage.error('解禁用户失败: ' + (error?.message || '请稍后重试'));
     } else {
       ElMessage.info('已取消解禁');
     }
  } finally {
     loading.value = false;
  }
};


const handleDeleteUser = (userId: number) => {
   // ... 删除用户逻辑保持不变（目前是禁用状态）...
  ElMessageBox.confirm(
    `确定要删除用户 ID: ${userId} 吗？此操作通常不可逆！`,
    '警告',
    {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      ElMessage.warning('删除用户功能尚未实现');
    })
    .catch(() => {
      ElMessage.info('已取消删除');
    });
};

const formatRole = (role: string | undefined): string => {
   // ... 角色格式化不变 ...
    if (!role) return '未知';
  switch (role.toLowerCase()) {
    case 'user': return '普通用户';
    case 'provider': return '服务商';
    case 'admin': return '管理员';
    default: return role;
  }
};

const getRoleTagType = (role: string | undefined): ('success' | 'warning' | 'danger' | 'info') => {
   // ... 角色标签类型不变 ...
    if (!role) return 'info';
  switch (role.toLowerCase()) {
    case 'user': return 'success';
    case 'provider': return 'warning';
    case 'admin': return 'danger';
    default: return 'info';
  }
};

// --- 新增：格式化用户状态 ---
const formatStatus = (status: string | undefined): string => {
  if (status === 'banned') return '已禁用';
  if (status === 'active') return '正常';
  return status || '正常'; // 默认为正常
}

const formatQualificationStatus = (status: string | undefined | null): string => {
   // ... 资质格式化不变 ...
    if (!status) return '';
  switch (status) {
    case 'PENDING_REVIEW': return '待审核';
    case 'APPROVED': return '已认证';
    case 'REJECTED': return '已拒绝';
    default: return status;
  }
};

onMounted(() => {
  fetchUsers();
});
</script>

<style scoped>
/* ... 样式保持不变 ... */
.user-list-container {
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

.el-button+.el-button {
  margin-left: 8px;
}
</style>