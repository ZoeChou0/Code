<template>
  <div class="service-list">
    <div class="header">
      <h2>服务列表</h2>
      <el-input v-model="searchQuery" placeholder="搜索服务..." style="width: 200px" />
    </div>

    <el-row :gutter="20">
      <el-col :span="8" v-for="service in filteredServices" :key="service.id">
        <el-card class="service-card">
          <template #header>
            <div class="card-header">
              <span>{{ service.name }}</span>
              <el-tag :type="getStatusType(service.reviewStatus)">
                {{ getStatusText(service.reviewStatus) }}
              </el-tag>
            </div>
          </template>
          <div class="card-content">
            <p class="description">{{ service.description }}</p>
            <div class="info">
              <p><strong>价格：</strong>¥{{ service.price }}</p>
              <p><strong>时长：</strong>{{ service.duration }}分钟</p>
              <p><strong>每日容量：</strong>{{ service.dailyCapacity }}</p>
            </div>
            <div class="requirements" v-if="hasRequirements(service)">
              <h4>服务要求：</h4>
              <ul>
                <li v-if="service.requiredVaccinations">
                  <strong>所需疫苗：</strong>{{ service.requiredVaccinations }}
                </li>
                <li v-if="service.requiresNeutered">
                  <strong>要求绝育</strong>
                </li>
                <li v-if="service.minAge || service.maxAge">
                  <strong>年龄要求：</strong>
                  {{ service.minAge ? `≥${service.minAge}岁` : '' }}
                  {{ service.maxAge ? `≤${service.maxAge}岁` : '' }}
                </li>
                <li v-if="service.temperamentRequirements">
                  <strong>性格要求：</strong>{{ service.temperamentRequirements }}
                </li>
                <li v-if="service.prohibitedBreeds">
                  <strong>不接受品种：</strong>{{ service.prohibitedBreeds }}
                </li>
              </ul>
            </div>
            <el-button type="primary" @click="handleReserve(service)" :disabled="service.reviewStatus !== 'APPROVED'">
              预约服务
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 预约服务对话框 -->
    <el-dialog v-model="dialogVisible" title="预约服务" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="选择宠物" prop="petId">
          <el-select v-model="form.petId" placeholder="请选择宠物">
            <el-option v-for="pet in petList" :key="pet.id" :label="pet.name" :value="pet.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="预约日期" prop="reservationDate">
          <el-date-picker v-model="form.reservationDate" type="date" placeholder="选择预约日期"
            :disabled-date="disabledDate" />
        </el-form-item>
        <el-form-item label="预约时间" prop="reservationTime">
          <el-time-picker v-model="form.reservationTime" placeholder="选择预约时间" :disabled-hours="disabledHours" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { getActiveServices, reserveService } from '@/api/service'
import type { Service, ServiceReservation } from '@/api/service'
import { getPetList } from '@/api/pet'
import type { Pet } from '@/api/pet'
import { useUserStore }  from '@/stores/user'


const searchQuery = ref('')
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const services = ref<Service[]>([])
const petList = ref<Pet[]>([])

const form = reactive<Omit<ServiceReservation, 'id' | 'status' | 'userId'>>({
  serviceId: 0,
  petId: 0,
  reservationDate: '',
  reservationTime: '',
  remark: ''
})

const rules = reactive<FormRules>({
  petId: [
    { required: true, message: '请选择宠物', trigger: 'change' }
  ],
  reservationDate: [
    { required: true, message: '请选择预约日期', trigger: 'change' }
  ],
  reservationTime: [
    { required: true, message: '请选择预约时间', trigger: 'change' }
  ]
})

// 过滤服务列表
const filteredServices = computed(() => {
  if (!searchQuery.value) return services.value
  return services.value.filter(service =>
    service.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
    service.description.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

// 获取状态类型
const getStatusType = (status: string) => {
  switch (status) {
    case 'APPROVED':
      return 'success'
    case 'PENDING':
      return 'warning'
    case 'REJECTED':
      return 'danger'
    default:
      return 'info'
  }
}

// 获取状态文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'APPROVED':
      return '已通过'
    case 'PENDING':
      return '审核中'
    case 'REJECTED':
      return '已拒绝'
    default:
      return '未知'
  }
}

// 检查是否有服务要求
const hasRequirements = (service: Service) => {
  return service.requiredVaccinations ||
    service.requiresNeutered ||
    service.minAge ||
    service.maxAge ||
    service.temperamentRequirements ||
    service.prohibitedBreeds
}

// 禁用过去的日期
const disabledDate = (time: Date) => {
  return time.getTime() < Date.now() - 8.64e7
}

// 禁用非工作时间
const disabledHours = () => {
  const hours = []
  for (let i = 0; i < 24; i++) {
    if (i < 9 || i > 17) {
      hours.push(i)
    }
  }
  return hours
}

// 获取服务列表
const fetchServices = async () => {
  try {
    const response = await getActiveServices()
    services.value = response.data
  } catch (error) {
    ElMessage.error('获取服务列表失败')
  }
}

// 获取宠物列表
const fetchPets = async () => {
  try {
    const response = await getPetList()
    petList.value = response.data
  } catch (error) {
    ElMessage.error('获取宠物列表失败')
  }
}

// 预约服务
const handleReserve = (service: Service) => {
  form.serviceId = service.id
  dialogVisible.value = true
}

// 提交预约
const handleSubmit = async () => {
  if (!formRef.value) return;

  await formRef.value.validate(async (valid, fields) => {
    if (valid) {
      submitting.value = true;
      try {
        // 1. 从 Store 获取 userId
        const currentUserId = useUserStore().userInfo?.id;

        // 2. 检查 userId 是否有效
        if (!currentUserId) {
            ElMessage.error('无法获取用户信息，请重新登录后再试');
            submitting.value = false; // 停止 loading
            // 可选：跳转登录页
            // router.push('/login');
            return;
        }

        // 3. 创建包含 userId 的 payload 对象
        //    类型是 Omit<ServiceReservation, 'id' | 'status'>
        const payload: Omit<ServiceReservation, 'id' | 'status'> = {
            serviceId: form.serviceId,
            petId: form.petId,
            reservationDate: form.reservationDate, // 确保格式是后端需要的
            reservationTime: form.reservationTime, // 确保格式是后端需要的
            remark: form.remark,
            userId: currentUserId // <-- 添加 userId
        };

        // 4. 调用 API，传入 payload
        const res = await reserveService(payload);

        // 5. 处理响应 (假设返回 BackendResult)
         if (res.code === 200) { // 检查后端返回的 code
            ElMessage.success('预约成功');
            dialogVisible.value = false; // 关闭对话框
            formRef.value?.resetFields(); // 清空表单
            // 可能需要刷新某些数据或导航
         } else {
            ElMessage.error(res.message || '预约失败');
         }
      } catch (error: any) {
         console.error("预约失败:", error);
         ElMessage.error(`预约失败: ${error.message || '未知错误'}`);
      } finally {
        submitting.value = false;
      }
    } else {
      console.log('表单验证失败!', fields);
      ElMessage.error('请检查表单填写是否完整正确');
    }
  });
};
// 初始化
fetchServices()
fetchPets()
</script>

<style scoped lang="scss">
.service-list {
  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 {
      margin: 0;
      color: #303133;
    }
  }

  .service-card {
    margin-bottom: 20px;
    transition: transform 0.3s;

    &:hover {
      transform: translateY(-5px);
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .card-content {
      .description {
        margin-bottom: 15px;
        color: #606266;
      }

      .info {
        margin-bottom: 15px;

        p {
          margin: 5px 0;
        }
      }

      .requirements {
        margin-bottom: 15px;
        padding: 10px;
        background-color: #f5f7fa;
        border-radius: 4px;

        h4 {
          margin: 0 0 10px 0;
          color: #303133;
        }

        ul {
          margin: 0;
          padding-left: 20px;

          li {
            margin: 5px 0;
            color: #606266;
          }
        }
      }
    }
  }
}
</style>