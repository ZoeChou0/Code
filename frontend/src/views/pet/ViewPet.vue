<template>
  <div class="view-pet-page">
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="15" animated />
    </div>

    <el-alert
      v-else-if="loadError || !pet"
      title="加载宠物信息失败"
      type="error"
      :closable="false"
      show-icon
      class="mb-4"
    >
      无法加载宠物信息，宠物可能不存在或发生错误。
      <el-button link type="primary" @click="router.back()">返回</el-button>
    </el-alert>

    <div v-else class="pet-detail-content">
      <el-row :gutter="24">
        <el-col :xs="24" :sm="8" :md="7">
          <el-card shadow="never" class="pet-summary-card">
            <div class="pet-avatar-section">
              <el-image
                :src="pet.photoUrl || '/default-pet-avatar.png'"
                fit="cover"
                class="pet-avatar-large"
                lazy
              >
                 <template #error>
                   <div class="image-slot">
                     <el-icon><Picture /></el-icon> 
                   </div>
                 </template>
                 <template #placeholder>
                   <div class="image-slot">加载中<span class="dot">...</span></div>
                 </template>
              </el-image>
            </div>
            <h2 class="pet-name-title">{{ pet.name }}</h2>
            <el-descriptions :column="1" border size="small" class="basic-info-desc">
               <el-descriptions-item label="类型">{{ formatSpecies(pet.species) }}</el-descriptions-item>
               <el-descriptions-item label="性别">{{ formatGender(pet.gender) }}</el-descriptions-item>
               <el-descriptions-item label="年龄">{{ formatAge(pet.ageInYears, pet.ageInMonths) }}</el-descriptions-item>
               <el-descriptions-item label="体重">{{ pet.weight ? `${pet.weight} kg` : '未记录' }}</el-descriptions-item>
               <el-descriptions-item label="是否绝育">
                  <el-tag :type="pet.neutered ? 'success' : 'info'" size="small">
                     {{ pet.neutered ? '是' : '否' }}
                  </el-tag>
               </el-descriptions-item>
               <el-descriptions-item label="活力水平">
                  <el-tag :type="energyLevelTagType(pet.energyLevel)" size="small">
                    {{ formatEnergyLevel(pet.energyLevel) }}
                  </el-tag>
               </el-descriptions-item>
               <el-descriptions-item label="性格特点">{{ pet.temperament || '未记录' }}</el-descriptions-item>
            </el-descriptions>
             <el-button type="primary" @click="goToEditPage" class="edit-button" :icon="Edit">编辑宠物信息</el-button>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="16" :md="17">
           <el-card shadow="never" class="detail-section-card">
             <template #header><div><el-icon><Memo /></el-icon> 宠物描述</div></template>
             <p class="description-text">{{ pet.description || '暂无详细描述。' }}</p>
           </el-card>

           <el-card shadow="never" class="detail-section-card">
             <template #header><div><el-icon><FirstAidKit /></el-icon> 健康信息</div></template>
             <el-descriptions :column="1" border size="small">
                <el-descriptions-item label="疫苗接种">{{ pet.vaccinationInfo || '未记录' }}</el-descriptions-item>
                <el-descriptions-item label="过敏情况">{{ pet.allergies || '未记录' }}</el-descriptions-item>
                <el-descriptions-item label="健康备注">{{ pet.medicalNotes || '未记录' }}</el-descriptions-item>
             </el-descriptions>
           </el-card>

           <el-card shadow="never" class="detail-section-card">
             <template #header><div><el-icon><Dish /></el-icon> 喂养与照护</div></template>
             <el-descriptions :column="1" border size="small">
                <el-descriptions-item label="喂食计划">{{ pet.feedingSchedule || '未记录' }}</el-descriptions-item>
                <el-descriptions-item label="喂食说明">{{ pet.feedingInstructions || '未记录' }}</el-descriptions-item>
                <el-descriptions-item label="如厕频率">{{ pet.pottyBreakFrequency || '未记录' }}</el-descriptions-item>
                <el-descriptions-item label="如厕说明">{{ pet.pottyBreakInstructions || '未记录' }}</el-descriptions-item>
                <el-descriptions-item label="独处耐受">{{ pet.aloneTimeTolerance || '未记录' }}</el-descriptions-item>
             </el-descriptions>
           </el-card>
        </el-col>
      </el-row>
       <div class="actions-footer">
          <el-button @click="router.back()">返回</el-button>
       </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  ElMessage, ElCard, ElRow, ElCol, ElImage, ElDescriptions, ElDescriptionsItem,
  ElTag, ElIcon, ElButton, ElSkeleton, ElAlert
} from 'element-plus'; // 按需导入
import { Picture, Edit, Memo, FirstAidKit, Dish, User as ElUser } from '@element-plus/icons-vue'; // 导入图标
import { getPetDetail } from '@/api/pet'; // 确认 API 函数名
import type { Pet } from '@/api/pet';     // 确认 Pet 类型

const route = useRoute();
const router = useRouter();

const loading = ref(true);
const loadError = ref(false);
const pet = ref<Pet | null>(null); // 存储宠物信息

const petId = computed(() => route.params.id as string); // 获取路由参数

// --- 数据加载 ---
onMounted(async () => {
  loading.value = true;
  loadError.value = false;
  if (!petId.value) {
    ElMessage.error('无效的宠物ID');
    loadError.value = true;
    loading.value = false;
    return;
  }
  try {
    const res = await getPetDetail(petId.value);
    if (res.code === 200 && res.data) {
      pet.value = res.data;
    } else {
      throw new Error(res.message || '获取宠物信息失败');
    }
  } catch (e: any) {
    console.error("加载宠物详情失败:", e);
    ElMessage.error(e.message || '加载宠物详情时出错');
    loadError.value = true;
  } finally {
    loading.value = false;
  }
});

// --- 辅助格式化函数 ---
const formatSpecies = (species?: 'Dog' | 'Cat'): string => {
  if (species === 'Dog') return '狗狗';
  if (species === 'Cat') return '猫咪';
  return '未知类型';
};

const formatGender = (gender?: 'Male' | 'Female'): string => {
  if (gender === 'Male') return '公';
  if (gender === 'Female') return '母';
  return '未知';
};

const formatAge = (years?: number, months?: number): string => {
  const y = years ?? 0;
  const m = months ?? 0;
  if (y === 0 && m === 0) return '未知年龄';
  let ageStr = '';
  if (y > 0) ageStr += `${y}岁`;
  if (m > 0) ageStr += ` ${m}个月`; // 加个空格
  return ageStr.trim() || '小于1个月';
};

const formatEnergyLevel = (level?: 'High' | 'Moderate' | 'Low'): string => {
  if (level === 'High') return '高';
  if (level === 'Moderate') return '中等';
  if (level === 'Low') return '低';
  return '未知';
}

const energyLevelTagType = (level?: 'High' | 'Moderate' | 'Low'): ('danger' | 'warning' | 'success' | 'info') => {
  if (level === 'High') return 'danger';
  if (level === 'Moderate') return 'warning';
  if (level === 'Low') return 'success';
  return 'info';
}


// --- 导航 ---
const goToEditPage = () => {
  if (pet.value) {
     // 确保路由名称 'EditPet' 是正确的
     router.push({ name: 'EditPet', params: { id: pet.value.id } });
  }
};

</script>

<style scoped lang="scss">
.view-pet-page {
  padding: 20px;
  background-color: #f7f8fa; // 淡灰色背景
}

.loading-state, .el-alert {
  max-width: 1100px;
  margin: 20px auto;
}

.pet-detail-content {
  max-width: 1100px;
  margin: 0 auto;
}

.pet-summary-card {
  text-align: center; /* 头像和名字居中 */
  margin-bottom: 20px; /* 和右侧卡片间距 */
  position: sticky; /* 让左侧卡片在滚动时固定 */
  top: 20px; /* 固定在顶部下方 20px */
}

.pet-avatar-section {
  margin-bottom: 16px;
}

.pet-avatar-large {
  width: 150px; /* 增大头像 */
  height: 150px;
  border-radius: 50%;
  border: 3px solid #fff;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  background-color: #eee; /* 图片加载时背景 */

  .image-slot {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    height: 100%;
    background: var(--el-fill-color-light);
    color: var(--el-text-color-secondary);
    font-size: 30px;
    .el-icon { font-size: 30px; }
    .dot { /* ... */ }
  }
}


.pet-name-title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #303133;
}

.basic-info-desc {
  margin-bottom: 20px;
  text-align: left; /* 描述列表左对齐 */
  :deep(.el-descriptions__label) { // 调整标签宽度和样式
    width: 80px;
    color: #909399;
  }
   :deep(.el-descriptions__content) {
     color: #303133;
   }
}

.detail-section-card {
  margin-bottom: 20px; /* 卡片间距 */
  .el-card__header div { /* 卡片标题 */
      font-size: 16px;
      font-weight: bold;
      color: #303133;
      display: flex;
      align-items: center;
      .el-icon { margin-right: 8px; }
  }
  .description-text {
    color: #606266;
    line-height: 1.6;
    padding: 0 10px; /* 给描述一些内边距 */
  }
  :deep(.el-descriptions__label) { // 调整描述标签样式
     width: 100px; // 可以调整标签宽度
     color: #909399;
   }
   :deep(.el-descriptions__content) {
     color: #303133;
   }
}

.actions-footer {
    text-align: center;
    margin-top: 30px;
}

.edit-button {
    width: 80%; /* 编辑按钮宽度 */
    margin-top: 10px; /* 与上方描述列表间距 */
}

.mb-4 { margin-bottom: 16px; }
.mt-6 { margin-top: 24px; }
</style>