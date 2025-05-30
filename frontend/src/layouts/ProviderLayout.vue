<template>
  <el-container class="provider-layout">
    <el-aside width="200px" class="provider-aside">
      <!-- <el-menu :default-active="activeMenu" class="el-menu-vertical-demo" router :collapse="isCollapse">
        <el-menu-item index="/provider/dashboard">
          <el-icon><DataLine /></el-icon>
          <span>服务商中心</span>
        </el-menu-item>
        <el-menu-item index="/provider/services">
          <el-icon><Service /></el-icon>
          <span>服务管理</span>
        </el-menu-item>
        <el-menu-item index="/provider/reservations">
          <el-icon><Calendar /></el-icon>
          <span>预约管理</span>
        </el-menu-item>
        <el-menu-item index="/provider/orders">
          <el-icon><Tickets /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          <span>退出登录</span>
        </el-menu-item>
      </el-menu> -->
      <el-button @click="toggleCollapse" class="collapse-button">
        <el-icon v-if="isCollapse">
          <Expand />
        </el-icon>
        <el-icon v-else>
          <Fold />
        </el-icon>
      </el-button>
    </el-aside>

    <el-main class="provider-main">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElContainer, ElAside, ElMain, ElMenu, ElMenuItem, ElIcon, ElButton, ElMessage } from 'element-plus';
import { Service, Tickets, SwitchButton, Fold, Expand, DataLine, Calendar } from '@element-plus/icons-vue';
import { useUserStore } from '@/stores/user'; // Import user store

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

// --- Menu Collapse State ---
const isCollapse = ref(false); // Controls if the sidebar is collapsed

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value;
};


// Determine the active menu item based on the current route
const activeMenu = computed(() => route.path);

// --- Logout Functionality ---
const handleLogout = async () => {
  const success = await userStore.logoutAction();
  if (success) {
    // Redirect to login page after successful logout
    router.push('/login');
    ElMessage.success('已退出登录');
  } else {
    // Handle logout failure, e.g., show an error message
    ElMessage.error('退出登录失败，请稍后重试');
  }
};
</script>

<style scoped>
.provider-layout {
  height: 100vh;
  /* Full viewport height */
  background-color: #f4f7f6;
  /* Light background for the layout */
}

.provider-aside {
  background-color: #ffffff;
  /* White background for sidebar */
  border-right: 1px solid #e0e0e0;
  /* Subtle border */
  transition: width 0.3s ease;
  /* Smooth transition for collapse */
  position: relative;
  /* Needed for absolute positioning of collapse button */
  display: flex;
  /* Use flexbox for layout */
  flex-direction: column;
  /* Stack menu and button vertically */
}

.el-menu-vertical-demo:not(.el-menu--collapse) {
  width: 200px;
  min-height: 400px;
}

.el-menu {
  border-right: none;
  /* Remove default border */
  flex-grow: 1;
  /* Allow menu to take available space */
}

.el-menu-item {
  font-size: 14px;
}

.el-menu-item i {
  /* Style icons */
  margin-right: 10px;
}

.el-menu-item.is-active {
  background-color: #ecf5ff;
  /* Highlight active menu item */
  color: #409EFF;
}

.provider-main {
  padding: 20px;
  background-color: #ffffff;
  /* White background for main content */
  overflow-y: auto;
  /* Allow scrolling if content overflows */
}

/* Style for the collapse button */
.collapse-button {
  position: absolute;
  bottom: 20px;
  /* Position at the bottom */
  left: 50%;
  /* Center horizontally */
  transform: translateX(-50%);
  /* Adjust horizontal centering */
  border: none;
  background: none;
  padding: 10px;
  cursor: pointer;
  color: #606266;
  /* Icon color */
}

.collapse-button:hover {
  color: #409EFF;
  /* Highlight on hover */
}


/* Transition for router view */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
