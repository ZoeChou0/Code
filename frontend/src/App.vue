<script setup lang="ts">
import DefaultLayout from './layouts/DefaultLayout.vue'
import { onMounted } from 'vue';
import { useUserStore } from '@/stores/user';


const userStore = useUserStore();


onMounted(async () => {

  if (userStore.token && !userStore.userInfo) {
    console.log('[App.vue onMounted] 检测到 token 但无 userInfo，开始获取...');
    await userStore.getUserInfoAction(); // 等待用户信息获取完成
    console.log('[App.vue onMounted] 用户信息获取结果:', userStore.userInfo);
  }
});
</script>

<template>
  <DefaultLayout>
    <router-view></router-view>
  </DefaultLayout>
</template>

<style>
#app {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
}
</style>
