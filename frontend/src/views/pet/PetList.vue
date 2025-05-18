<template>
  <div class="pet-list-container">
    <h2 class="page-title">我的宠物</h2>
    <p class="page-description">在这里管理您的宠物信息，帮助照护人员更好地了解和照顾它们。</p>
    
    <div class="pet-cards" v-loading="loading">
      <!-- 宠物卡片 -->
      <div v-for="pet in petList" :key="pet.id" class="pet-card">
        <div class="pet-image">
          <img :src="pet.photoUrl || '/default-pet-avatar.png'" :alt="pet.name" />
        </div>
        <div class="pet-info">
          <h4 class="pet-name">{{ pet.name || '未命名宠物' }}</h4>
          <div class="pet-stats">
            <!-- 品种信息 -->
            <div class="stat-row">
              <div class="stat-icon">
                <el-icon><Avatar /></el-icon>
              </div>
              <div class="stat-content">
                <span class="stat-label">品种</span>
                <span class="stat-value">{{ pet.species }}</span>
              </div>
            </div>
            <!-- 年龄和体重信息 -->
            <div class="stat-row">
              <div class="stat-icon">
                <el-icon><InfoFilled /></el-icon>
              </div>
              <div class="stat-content">
                <span class="stat-value">
                  {{ formatGender(pet.gender) }}, 
                  {{ formatAge(pet.ageInYears, pet.ageInMonths) }}
                  {{ pet.weight ? `, ${pet.weight}kg` : '' }}
                </span>
              </div>
            </div>
          </div>
          <div class="pet-actions">
            <el-button 
              type="primary" 
              link 
              class="action-button"
              @click="goToEditPetPage(pet)"
            >
              编辑宠物信息
              <el-icon class="arrow-icon"><ArrowRight /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 固定在底部的添加按钮 -->
    <div class="add-pet-button">
      <el-button type="primary" class="add-button" @click="goToAddPetPage">
        <el-icon class="add-icon"><Plus /></el-icon>
        <span>添加新宠物</span>
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { getPetList } from '@/api/pet';
import type { Pet } from '@/api/pet';
import { Edit, View, Plus, InfoFilled, ArrowRight, Avatar } from '@element-plus/icons-vue';

const router = useRouter();
const loading = ref(false);
const petList = ref<Pet[]>([]);

const formatAge = (years?: number, months?: number): string => {
  const y = years ?? 0;
  const m = months ?? 0;
  if (y === 0 && m === 0) return '未知年龄';
  let ageStr = '';
  if (y > 0) ageStr += `${y}岁`;
  if (m > 0) ageStr += `${m}个月`;
  return ageStr || '小于1个月';
};

const formatGender = (gender?: string): string => {
  if (gender === 'Male') return '公';
  if (gender === 'Female') return '母';
  return '未知';
};

const viewPetDetails = (pet: Pet) => {
  router.push(`/pets/${pet.id}`);
};

const goToAddPetPage = () => {
  router.push('/pets/add');
};

const goToEditPetPage = (pet: Pet) => {
  router.push({ name: 'EditPet', params: { id: pet.id } });
};

const fetchPetList = async () => {
  loading.value = true;
  try {
    const response = await getPetList();
    if (response.code === 200 && response.data) {
      petList.value = response.data;
    } else {
      ElMessage.error(response.message || '获取宠物列表失败');
      petList.value = [];
    }
  } catch (error: any) {
    console.error("获取宠物列表出错:", error);
    //ElMessage.error(error.message || '请求宠物列表时出错');
    petList.value = [];
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchPetList();
});
</script>

<style scoped lang="scss">
.pet-list-container {
  padding: 20px;
  padding-bottom: 100px; // 为底部按钮留出更多空间
  min-height: calc(100vh - 140px);
  position: relative; // 用于定位底部按钮
}

.page-title {
  margin: 0 0 12px;
  font-size: 28px;
  font-weight: 600;
  color: #1f2124;
}

.page-description {
  margin: 0 0 32px;
  font-size: 16px;
  color: #666;
}

.pet-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
  margin-bottom: 20px;
}

.pet-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  transition: transform 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0,0,0,0.12);
  }
}

.pet-image {
  width: 100%;
  height: 192px;
  overflow: hidden;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.pet-info {
  padding: 20px;
}

.pet-name {
  margin: 0 0 16px;
  font-size: 20px;
  font-weight: 600;
  color: #1f2124;
}

.pet-stats {
  margin-bottom: 20px;
}

.stat-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 12px;

  &:last-child {
    margin-bottom: 0;
  }
}

.stat-icon {
  width: 16px;
  height: 16px;
  margin-right: 12px;
  color: #1f2124;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.stat-label {
  font-size: 14px;
  color: #1f2124;
  margin-bottom: 2px;
}

.stat-value {
  font-size: 14px;
  color: #1f2124;
}

.pet-actions {
  margin-top: 16px;
  border-top: 1px solid #eee;
  padding-top: 16px;
}

.action-button {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 8px 0;
  font-weight: 600;
  font-size: 15px;

  .arrow-icon {
    margin-left: 8px;
  }

  &:hover {
    opacity: 0.8;
  }
}

// 新的添加按钮样式
.add-pet-button {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px 20px;
  background: #fff;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.08);
  z-index: 1000;
  display: flex;
  justify-content: center;
}

.add-button {
  height: 48px;
  padding: 0 32px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-width: 200px;
  
  .add-icon {
    font-size: 24px;
  }
}

@media screen and (max-width: 768px) {
  .pet-cards {
    grid-template-columns: 1fr;
  }

  .page-title {
    font-size: 24px;
  }

  .page-description {
    font-size: 14px;
  }

  .add-pet-button {
    padding: 12px 16px;
  }

  .add-button {
    width: 100%;
  }
}
</style>