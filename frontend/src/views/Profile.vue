<template>
  <div class="profile-page">
    <div class="profile-banner">
      <h2>个人中心</h2>
    </div>
    <div class="profile-content">
      <el-card class="user-info-card" v-if="userInfo">
        <div class="avatar-block">
          <el-avatar
            v-if="userInfo.profilePhotoUrl"
            :size="80"
            :src="userInfo.profilePhotoUrl"
          />
          <el-avatar
            v-else
            :size="80"
            icon="el-icon-user"
          />
        </div>
        <div class="user-info-list">
          <div class="user-row"><strong>用户名：</strong>{{ userInfo.name }}</div>
          <div class="user-row"><strong>手机号：</strong>{{ userInfo.phone }}</div>
          <div class="user-row"><strong>邮箱：</strong>{{ userInfo.email }}</div>
          <div class="user-row"><strong>身份：</strong>{{ roleText[userInfo.role] || userInfo.role }}</div>
        </div>
        <el-button type="primary" class="edit-btn" @click="editProfile">编辑资料</el-button>
      </el-card>
      <el-card v-else>
        用户加载中...
      </el-card>

      <el-card class="pet-info-card">
        <div class="pet-header">
          <span class="pet-title">我的宠物</span>
        </div>
        <div v-if="loadingPets">加载宠物信息中...</div>
        <div v-else-if="pets.length === 0" class="add-pet-block">
          <el-empty description="还没有添加宠物">
            <el-button type="primary" @click="addPet">
              <el-icon><Plus /></el-icon> 添加宠物
            </el-button>
          </el-empty>
        </div>
        <div v-else class="pet-list">
          <div v-for="pet in pets" :key="pet.id" class="pet-card-wrapper">
            <div class="pet-card">
              <div class="pet-wrap" :style="{ backgroundImage: `url(${pet.photoUrl || '/default-pet-avatar.png'})` }">
                <div class="pet-info-wrap">
                  <div class="pet-info">
                    <h3 class="pet-name">{{ pet.name }}</h3>
                    <div class="pet-breed">{{ pet.species }}</div>
                    <div class="pet-details">
                      {{ formatAge(pet.ageInYears, pet.ageInMonths) }}
                      {{ pet.weight ? `, ${pet.weight}kg` : '' }}
                    </div>
                  </div>
                </div>
              </div>
              <div class="pet-actions">
                <ul class="action-list">
                  <li>
                    <el-button link type="primary" @click="editPet(pet)">
                      <el-icon><Edit /></el-icon>
                      <span>编辑</span>
                    </el-button>
                  </li>
                  <li>
                    <el-button link type="primary" @click="viewPet(pet)">
                      <el-icon><View /></el-icon>
                      <span>查看</span>
                    </el-button>
                  </li>
                </ul>
              </div>
            </div>
          </div>
          <div class="pet-card-wrapper">
            <div class="add-pet-card" @click="addPet">
              <el-icon class="add-icon"><Plus /></el-icon>
              <span>添加新宠物</span>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    </div>
</template>

<script setup lang="ts">
// 导入需要的模块
import { ref, onMounted, computed } from 'vue'
import { ElAvatar, ElButton, ElCard, ElEmpty, ElIcon, ElMessage } from 'element-plus' // 引入 ElMessage
import { Plus, Edit, View } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getPetList } from '@/api/pet' 
import type { Pet } from '@/api/pet' 
import { useRouter } from 'vue-router'
// import router from '@/router'; // 如果需要路由跳转，导入 router

// 使用用户 store
const userStore = useUserStore()

const userInfo = computed(() => userStore.userInfo)

// 定义角色文本映射
const roleText: Record<string, string> = { // 添加类型注解
  user: '普通用户',
  provider: '服务商',
  admin: '管理员',
}

// 宠物列表 ref
const pets = ref<Pet[]>([]) // 使用导入的 Pet 类型
const loadingPets = ref(false) // 添加加载状态

// 获取宠物列表的函数
const fetchPetList = async () => {
  loadingPets.value = true
  try {
    const res = await getPetList() // 调用 API
    if (res && res.code === 200) {
      pets.value = res.data || [] // 更新宠物列表
    } else {
      ElMessage.error(res.message || '获取宠物列表失败')
    }
  } catch (error: any) {
    ElMessage.error('请求宠物列表时出错: ' + (error.message || '未知错误'))
    console.error("Error fetching pets:", error)
  } finally {
    loadingPets.value = false
  }
}

// 组件挂载后执行
onMounted(() => {
  // 检查用户信息是否已加载，如果未加载（例如直接访问此页面），尝试重新加载
  if (!userInfo.value) {
    userStore.getUserInfoAction().catch((err:unknown) => {
        console.error("Failed to fetch user info on mount:", err);
        // 可以根据需要处理错误，例如提示用户重新登录
    });
  }
  // 获取宠物列表
  fetchPetList()
})

// 编辑资料处理函数
const editProfile = () => {
  const role = userStore.userInfo?.role
  if (role === 'admin') {
    router.push('/admin/profile/edit')
  } else if (role === 'provider') {
    router.push('/provider/profile/edit')
  } else {
    router.push('/profile/edit')
  }
}

// 添加宠物处理函数
const router = useRouter()
const addPet = () => {
  router.push('/pets/add')
}

// 编辑宠物处理函数
const editPet = (pet: Pet) => {
  router.push(`/pets/edit/${pet.id}`);
}

const formatAge = (years?: number, months?: number): string => {
  const y = years ?? 0;
  const m = months ?? 0;
  if (y === 0 && m === 0) return '未知年龄';
  let ageStr = '';
  if (y > 0) ageStr += `${y}岁`;
  if (m > 0) ageStr += `${m}个月`;
  return ageStr || '小于1个月';
};

const viewPet = (pet: Pet) => {
  router.push(`/pets/${pet.id}`);
};
</script>

<style scoped lang="scss">
.profile-page {
  min-height: 100vh;
  background: #f7f8fa;
  .profile-banner {
    background: linear-gradient(90deg, #e0f7fa 0%, #fff 100%);
    padding: 36px 0 18px 40px;
    h2 {
      font-size: 2em;
      color: #409eff;
      margin: 0;
    }
  }
  .profile-content {
    display: flex;
    gap: 32px;
    max-width: 1100px;
    margin: 0 auto;
    padding: 30px 0;
    .user-info-card {
      width: 320px;
      min-height: 340px;
      display: flex;
      flex-direction: column;
      align-items: center;
      .avatar-block {
        margin-bottom: 18px;
      }
      .user-info-list {
        width: 100%;
        margin-bottom: 18px;
        .user-row {
          margin-bottom: 8px;
          font-size: 1.1em;
          word-break: break-all; // 防止邮箱过长溢出
        }
      }
      .edit-btn {
        width: 100%;
        margin-top: auto; // 将按钮推到底部
      }
    }
    .pet-info-card {
      flex: 1;
      min-height: 340px;
      .pet-header {
        display: flex;
        align-items: center;
        margin-bottom: 18px;
        .pet-title {
          font-size: 1.3em;
          font-weight: bold;
        }
      }
      .add-pet-block {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        min-height: 220px; // 使用 min-height 适应内容
      }
      .pet-list {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
        gap: 24px;
        padding: 4px;
      }
    }
  }
}

.pet-card-wrapper {
  width: 100%;
}

.pet-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  height: 100%;
  display: flex;
  flex-direction: column;
}

.pet-wrap {
  position: relative;
  height: 200px;
  background-size: cover;
  background-position: center;
  background-color: #2c3e50;

  &::before {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 70%;
    background: linear-gradient(to top, rgba(0,0,0,0.8), rgba(0,0,0,0));
  }
}

.pet-info-wrap {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20px;
  color: #fff;
}

.pet-info {
  text-align: left;
}

.pet-name {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 600;
  color: #fff;

  &:hover {
    text-decoration: underline;
  }
}

.pet-breed {
  font-size: 16px;
  margin-bottom: 4px;
  color: #fff;
}

.pet-details {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.pet-actions {
  padding: 12px 16px;
  background: #fff;
}

.action-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  gap: 16px;

  li {
    .el-button {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 14px;
      
      .el-icon {
        font-size: 16px;
      }
    }
  }
}

.add-pet-card {
  height: 100%;
  min-height: 200px;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  background: #fafbfc;

  &:hover {
    border-color: #409eff;
    background: #f5f7fa;
  }

  .add-icon {
    font-size: 32px;
    color: #909399;
    margin-bottom: 12px;
  }

  span {
    color: #909399;
    font-size: 16px;
  }
}
</style>