<template>
  <div class="edit-pet-page">
    <div class="page-header">
      <h1 class="header-title">编辑宠物信息</h1>
      <h3 class="header-subtitle">更新您的宠物资料</h3>
    </div>

    <div v-if="pageLoading" class="loading-state">
      <el-skeleton :rows="10" animated />
    </div>

    <el-alert
      v-else-if="!pageLoading && loadError"
      title="加载宠物信息失败"
      type="error"
      :closable="false"
      show-icon
      class="mb-4"
    >
      无法加载宠物信息，请稍后重试或联系管理员。
      <el-button link type="primary" @click="router.back()">返回</el-button>
    </el-alert>

    <PetForm
      v-else-if="!pageLoading && petData"
      :pet-data="petData" 
      @submit-success="handleEditSuccess" 
      @cancel="handleCancel"         
    />

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElSkeleton, ElAlert, ElButton } from 'element-plus'; // 按需导入
import PetForm from '@/components/PetForm.vue'; // 确认 PetForm 的导入路径
import { getPetDetail } from '@/api/pet';       // 导入获取详情的 API
import type { Pet } from '@/api/pet';           // 导入 Pet 类型

const router = useRouter();
const route = useRoute();

const pageLoading = ref(true); // 页面加载状态
const loadError = ref(false);   // 加载错误状态
const petData = ref<Pet | null>(null); // 存储从后端获取的宠物数据

// --- 组件挂载：获取宠物数据 ---
onMounted(async () => {
  pageLoading.value = true;
  loadError.value = false;
  try {
    const petId = route.params.id; // 从路由获取 ID
    if (!petId || typeof petId !== 'string') {
      throw new Error('无效或未找到宠物ID');
    }
    const res = await getPetDetail(petId); // 调用 API
    if (res.code === 200 && res.data) {
      petData.value = res.data; // 将获取到的数据存入 ref
    } else {
      throw new Error(res.message || '获取宠物信息失败');
    }
  } catch (e: any) {
    console.error("获取宠物信息失败:", e);
    ElMessage.error(e.message || '获取宠物信息时发生错误');
    loadError.value = true;
  } finally {
    pageLoading.value = false;
  }
});

// --- 事件处理 ---

// PetForm 提交成功后的处理函数
const handleEditSuccess = () => {
  ElMessage.success('宠物信息更新成功!');
  // 跳转回列表页或上一页
  router.push('/pets'); // 或者 router.back();
};

// PetForm 取消后的处理函数
const handleCancel = () => {
  router.back(); // 直接返回上一页
};

</script>

<style scoped lang="scss">
/* 样式可以保持或根据需要调整 */
.edit-pet-page {
  padding: 20px;
  max-width: 900px; /* 可以适当调整宽度以容纳 PetForm */
  margin: 0 auto;
}
.page-header {
  margin-bottom: 20px;
  text-align: center;
}
.header-title { /* ... */ }
.header-subtitle { /* ... */ }
.loading-state {
    padding: 20px;
    background: #fff;
    border-radius: 8px;
}
.mb-4 {
  margin-bottom: 16px;
}
</style>