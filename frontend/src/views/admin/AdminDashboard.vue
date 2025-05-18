<template>
  <div class="admin-dashboard">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>欢迎使用管理后台</span>
        </div>
      </template>
      <div>
        <p v-if="userStore.userInfo">
          您好，<strong>{{ userStore.userInfo.name }}</strong>！欢迎回来。
        </p>
        <p>
          您可以通过左侧菜单管理系统的不同模块。
        </p>
        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :span="6">
            <el-card shadow="hover" @click="goTo('/admin/users')" class="dashboard-card">
               <el-icon size="24"><User /></el-icon>
               <p>用户管理</p>
            </el-card>
          </el-col>
           <el-col :span="6">
             <el-card shadow="hover" @click="goTo('/admin/services')" class="dashboard-card">
               <el-icon size="24"><Service /></el-icon>
               <p>服务管理</p>
             </el-card>
          </el-col>
           <el-col :span="6">
             <el-card shadow="hover" @click="goTo('/admin/providers')" class="dashboard-card">
                <el-icon size="24"><Shop /></el-icon>
               <p>服务商管理</p>
            </el-card>
          </el-col>
           <el-col :span="6">
            <el-card shadow="hover" @click="goTo('/admin/orders')" class="dashboard-card">
              <el-icon size="24"><List /></el-icon>
               <p>订单管理</p>
             </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
// 导入需要的 Element Plus 组件和图标
import { ElCard, ElRow, ElCol, ElIcon } from 'element-plus'
import { User, Service, Shop, List } from '@element-plus/icons-vue' // 导入快捷方式图标

const userStore = useUserStore()
const router = useRouter()

// 跳转函数
const goTo = (path: string) => {
  router.push(path)
}

// 如果需要，可以在 onMounted 钩子中获取仪表盘需要的初始数据
// import { onMounted } from 'vue'
// onMounted(() => {
//   // fetchDashboardData()
// })
</script>

<style scoped lang="scss">
.admin-dashboard {
  padding: 0; /* 通常 el-main 会有内边距，这里可以不用额外加 */
}

.card-header {
  font-weight: bold;
}

.dashboard-card {
  cursor: pointer;
  text-align: center;
  color: #606266;
  transition: all 0.3s ease;

  .el-icon {
     margin-bottom: 10px;
     color: #409EFF; /* 图标颜色 */
  }

  p {
    margin: 0;
    font-size: 14px;
  }

  &:hover {
     transform: translateY(-5px);
     box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
     color: #409EFF;
  }
}
</style>