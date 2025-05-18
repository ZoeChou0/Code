<template>
  <div>
    <h1>添加新宠物</h1>
    <PetForm @submit-success="handlePetAdded" @cancel="handleCancel" />
  </div>
</template>

<script setup lang="ts">
import PetForm from '@/components/PetForm.vue';
import { useRouter } from 'vue-router';

const router = useRouter();

const handlePetAdded = () => {
  router.push('/pets');
}

const handleCancel = () => {
  router.back();
}
</script>

<style scoped>
/* AddPet.vue 页面的样式 */
h1 {
  margin-bottom: 20px;
}
</style>