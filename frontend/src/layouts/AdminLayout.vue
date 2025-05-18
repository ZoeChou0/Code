<template>
  <div class="admin-layout">
    <el-container>
      <el-aside width="200px">
        <el-menu :router="true" :default-active="$route.path" class="el-menu-vertical">
          <el-menu-item index="/admin/users">
            <el-icon>
              <user />
            </el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/providers">
            <el-icon>
              <shop />
            </el-icon>
            <span>服务商管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/services">
            <el-icon>
              <service />
            </el-icon>
            <span>服务管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/orders">
            <el-icon>
              <list />
            </el-icon>
            <span>订单管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header>
          <div class="header-content">
            <h2>系统管理后台</h2>
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                {{ userStore.userInfo?.name }}
                <el-icon><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        <el-main>
          <router-view></router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { User, Shop, Service, List, ArrowDown } from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()

// const handleCommand = (command: string) => {
//   if (command === 'profile') {
//     router.push('/admin/profile/edit')
//   } else if (command === 'logout') {
//     ElMessageBox.confirm('确定要退出登录吗？', '提示', {
//       confirmButtonText: '确定',
//       cancelButtonText: '取消',
//       type: 'warning'
//     }).then(() => {
//       userStore.logoutAction()
//       router.push('/login')
//     })
//   }
// }

const handleCommand = async (command: string) => {
  if (command === 'profile') {
    router.push('/admin/profile/edit')
  } else if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })

      const success = await userStore.logoutAction()

      if (success) {
        router.push('/login')
      }
    } catch {
      // 用户取消退出，或其他异常，无需处理
    }
  }
}

</script>

<style scoped lang="scss">
.admin-layout {
  min-height: 100vh;

  .el-container {
    min-height: 100vh;
  }

  .el-aside {
    background-color: #304156;

    .el-menu {
      border-right: none;
      background-color: #304156;
    }
  }

  .el-header {
    background-color: #fff;
    box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
    padding: 0 20px;

    .header-content {
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: space-between;

      h2 {
        margin: 0;
        color: #303133;
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
      }
    }
  }

  .el-main {
    padding: 20px;
    background-color: #f5f7fa;
  }
}
</style>