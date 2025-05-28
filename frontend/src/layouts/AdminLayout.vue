<!-- <template>
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
</style> -->


<template>
  <div class="admin-layout">
    <el-container>
      <el-aside width="200px">
        <el-menu :router="true" :default-active="$route.path" class="el-menu-vertical">
          <el-menu-item index="/admin/users">
            <el-icon><user /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/providers">
            <el-icon><shop /></el-icon>
            <span>服务商管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/services">
            <el-icon><service /></el-icon>
            <span>服务管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/orders">
            <el-icon><list /></el-icon>
            <span>订单管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/send-notification">
            <el-icon><Promotion /></el-icon>
            <span>发送通知</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
// 确保导入了所有用到的图标，包括新增的 Promotion
import { User, Shop, Service, List, ArrowDown, Promotion, Bell } from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()

// handleCommand 函数用于处理可能的头部下拉菜单命令，与侧边栏导航不直接相关，保持不变
const handleCommand = async (command: string) => {
  if (command === 'profile') {
    // 假设管理员的个人资料编辑页路由也是 /admin/profile/edit
    // 如果您的路由配置中，管理员编辑个人资料的路由是 /admin/profile/edit
    // 那么这里不需要修改。如果路由不同，请相应调整。
    // 例如，如果AdminEditProfile组件实际路由是 /admin/edit-profile
    // router.push('/admin/edit-profile');
    router.push('/admin/profile/edit') // 与您 router/index.ts 中 AdminEditProfile 对应
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
      // 用户取消退出
      console.log('用户取消退出');
    }
  }
}

// 您可以根据需要添加其他 <script setup> 逻辑，例如菜单的激活状态等
// const activePath = computed(() => router.currentRoute.value.path);
// 然后在 el-menu 的 :default-active 上使用 activePath
// 不过您已经使用了 :default-active="$route.path"，这是更简洁的方式

</script>

<style scoped lang="scss">
.admin-layout {
  min-height: 100vh;

  .el-container {
    min-height: 100vh;
  }

  .el-aside {
    background-color: #304156; // 深色背景
    box-shadow: 2px 0 6px rgba(0,21,41,.35); // 添加一点阴影

    .el-menu {
      border-right: none; // 移除默认的右边框
      background-color: #304156; // 与 aside 背景色一致
      height: 100%; // 让菜单填充整个侧边栏高度

      .el-menu-item {
        color: #bfcbd9; // 菜单项文字颜色
        &:hover {
          background-color: #263445; // 鼠标悬停背景色
          color: #fff; // 鼠标悬停文字颜色
        }
        &.is-active {
          color: #409EFF; // 激活状态的文字颜色 (Element Plus 主题色)
          background-color: #1f2d3d; // 激活状态的背景色 (可以自定义)
        }
        .el-icon {
          color: #bfcbd9; // 图标颜色
        }
         &.is-active .el-icon {
            color: #409EFF; // 激活状态图标颜色
        }
      }
       // 如果使用子菜单，也可以为子菜单项设置类似样式
      .el-sub-menu__title {
        color: #bfcbd9;
        &:hover {
          background-color: #263445;
          color: #fff;
        }
        .el-icon {
          color: #bfcbd9;
        }
      }
    }
  }

  // 如果您决定添加 el-header，这里的样式会生效
  .el-header {
    background-color: #fff; // 头部背景色
    color: #333;
    line-height: 60px; // 垂直居中内容
    border-bottom: 1px solid #e6e6e6; // 底部边框
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 20px;

    .header-content { // 这是您之前代码中的，如果取消注释 el-header，确保它能正常工作
      display: flex;
      align-items: center;
      justify-content: space-between;
      width: 100%; // 确保内容充满 header

      h2 { // 如果有标题
        margin: 0;
        font-size: 1.2rem;
      }

      .user-info { // 用户信息下拉
        cursor: pointer;
        .el-dropdown-link {
          display: flex;
          align-items: center;
          color: #333;
           .el-icon {
             margin-left: 5px;
           }
        }
      }
    }
  }


  .el-main {
    padding: 20px;
    background-color: #f0f2f5; // 主内容区背景色，稍浅一些
    // overflow-y: auto; // 如果内容可能超出，允许垂直滚动
  }
}
</style>