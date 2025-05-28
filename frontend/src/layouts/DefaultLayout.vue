<template>
  <div class="layout">
    <el-container>
      <el-header>
        <nav class="nav">
          <div class="logo">
            <router-link to="/" class="logo-link">宠乐居系统</router-link>
          </div>
          <div class="nav-links">
            <router-link to="/">首页</router-link>
            <template v-if="!userStore.token">
              <router-link to="/login">登录</router-link>
              <router-link to="/register">注册</router-link>
            </template>
            <template v-else>
              <!-- 普通用户菜单 -->
              <template v-if="userStore.isUser()">
                <router-link to="/services">服务列表</router-link>
                <router-link to="/pets">我的宠物</router-link>
                <router-link to="/orders">我的订单</router-link>
                <router-link to="/notifications">消息通知</router-link>
              </template>

              <!-- 服务商菜单 -->
              <template v-if="userStore.isProvider()">
                <router-link to="/provider/dashboard">服务商中心</router-link>
                <router-link to="/provider/services">服务管理</router-link>
                <router-link to="/provider/reservations">预约管理</router-link>
                <router-link to="/provider/orders">订单管理</router-link>
              </template>

              <!-- 管理员菜单 -->
              <template v-if="userStore.isAdmin()">
                <router-link to="/admin/users">用户管理</router-link>
                <router-link to="/admin/providers">服务商管理</router-link>
                <router-link to="/admin/services">服务管理</router-link>
                <router-link to="/admin/orders">订单管理</router-link>
              </template>

              <el-dropdown @command="handleCommand">
  <span class="user-info">
    {{ userStore.userInfo?.name }}
    <el-icon><arrow-down /></el-icon>
  </span>
  <template #dropdown>
    <el-dropdown-menu>
      <!-- 普通用户菜单 -->
      <template v-if="userStore.isUser()">
        <el-dropdown-item command="reservations">服务预约</el-dropdown-item>
        <el-dropdown-item command="pets">我的宠物</el-dropdown-item>
        <el-dropdown-item command="orders">我的订单</el-dropdown-item>
      </template>

      <!-- 服务商菜单 -->
      <template v-else-if="userStore.isProvider()">
        <el-dropdown-item command="providerServices">服务管理</el-dropdown-item>
        <el-dropdown-item command="providerOrders">订单管理</el-dropdown-item>
      </template>

      <!-- 管理员菜单 -->
      <template v-else-if="userStore.isAdmin()">
        <el-dropdown-item command="adminUsers">用户管理</el-dropdown-item>
        <el-dropdown-item command="adminProviders">服务商管理</el-dropdown-item>
        <el-dropdown-item command="adminServices">服务管理</el-dropdown-item>
        <el-dropdown-item command="adminOrders">订单管理</el-dropdown-item>
      </template>

      <el-dropdown-item command="profile">个人信息</el-dropdown-item>
      <el-dropdown-item command="logout">退出登录</el-dropdown-item>
    </el-dropdown-menu>
  </template>
</el-dropdown>
            </template>
          </div>
        </nav>
      </el-header>
      <el-main>
        <router-view></router-view>
      </el-main>
      <el-footer>
        <p>&copy; 2025 宠物服务系统. All rights reserved.</p>
      </el-footer>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()

// const handleCommand = async (command: string) => {
//   if (command === 'services') {
//     router.push('/services')
//   } else if (command === 'pets') {
//     router.push('/pets')
//   } else if (command === 'orders') {
//     router.push('/orders')
//   } else if (command === 'profile') {
//     router.push('/profile')
//   } else if (command === 'logout') {
//     try {
//       await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
//         confirmButtonText: '确定',
//         cancelButtonText: '取消',
//         type: 'warning'
//       })
//       await userStore.logoutAction()
//       router.push('/login')
//     } catch {
//       // 用户取消操作
//     }
//   }
// }
const handleCommand = async (command: string) => {
  switch (command) {
    case 'reservations':
      router.push('/reservations/my')
      break
    case 'pets':
      router.push('/pets')
      break
    case 'orders':
      router.push('/orders')
      break
    case 'providerServices':
      router.push('/provider/services')
      break
    case '':
      router.push('/provider/reservations')
      break
    case 'adminUsers':
      router.push('/admin/users')
      break
    case 'adminProviders':
      router.push('/admin/providers')
      break
    case 'adminServices':
      router.push('/admin/services')
      break
    case 'adminOrders':
      router.push('/admin/orders')
      break
    case 'profile':
      router.push('/profile')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await userStore.logoutAction()
        router.push('/login')
      } catch {
        // 用户取消
      }
      break
  }
}

</script>

<style scoped lang="scss">
.layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  .el-container {
    height: 100%;
    display: flex;
    flex-direction: column;
  }

  .el-header {
    background-color: #fff;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    padding: 0 20px;
    flex-shrink: 0;

    .nav {
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: space-between;

      .logo {
        font-size: 20px;
        font-weight: bold;

        .logo-link {
          color: #409EFF;
          text-decoration: none;
          transition: color 0.3s;

          &:hover {
            color: #66b1ff;
          }
        }
      }

      .nav-links {
        display: flex;
        align-items: center;
        gap: 20px;

        a {
          text-decoration: none;
          color: #606266;
          font-size: 16px;

          &:hover {
            color: #409EFF;
          }

          &.router-link-active {
            color: #409EFF;
          }
        }

        .user-info {
          display: flex;
          align-items: center;
          gap: 5px;
          cursor: pointer;
          color: #606266;
          font-size: 16px;
          &:hover {
            color: #409EFF;
          }
          outline: none;
          box-shadow: none;
          border: none;
          background: transparent;
        }
      }
    }
  }

  .el-main {
    flex: 1;
    padding: 20px;
    background-color: #f5f7fa;
    overflow-y: auto;
  }

  .el-footer {
    text-align: center;
    color: #909399;
    padding: 20px;
    background-color: #fff;
    flex-shrink: 0;
  }
}

/* 去除 el-dropdown 被选中时的默认高亮边框 */
:deep(.el-dropdown .user-info:focus),
:deep(.el-dropdown .user-info:active) {
  outline: none !important;
  box-shadow: none !important;
  border: none !important;
  background: transparent !important;
  color: #409EFF;
}
</style>