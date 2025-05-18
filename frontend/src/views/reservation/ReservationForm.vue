<template>
  <div>
    <h1>服务预约</h1>
    <el-row justify="center"> <el-col :xs="24" :sm="20" :md="16" :lg="12" :xl="10"> <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" v-loading="loading">
           <el-form-item label="服务项目">
             <span>服务 ID: {{ serviceId }}</span>
           </el-form-item>

           <el-form-item label="选择宠物" prop="petId">
             <el-select v-model="form.petId" placeholder="请选择您的宠物" clearable style="width: 100%;">
               <el-option v-for="pet in userPets" :key="pet.id" :label="pet.name" :value="pet.id"/>
             </el-select>
           </el-form-item>

           <el-form-item label="预约日期范围" prop="reservationDateRange">
             <el-date-picker
               v-model="form.reservationDateRange"
               type="daterange"
               range-separator="至"
               start-placeholder="开始日期"
               end-placeholder="结束日期"
               format="YYYY-MM-DD"
               value-format="YYYY-MM-DD"
               :disabled-date="disabledDate"
               style="width: 100%;" />
           </el-form-item>

           <el-form-item label="开始时间" prop="reservationTime">
             <el-time-picker
               v-model="form.reservationTime"
               placeholder="选择开始时间"
               format="HH:mm"
               value-format="HH:mm:ss"
               :disabled-hours="disabledHours"
               :disabled-minutes="disabledMinutes"
               style="width: 100%;" />
           </el-form-item>

           <el-form-item>
             <el-button type="primary" @click="submitForm" :loading="loading">提交预约</el-button>
             <el-button @click="resetForm">重置</el-button>
           </el-form-item>
        </el-form>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getPetList } from '@/api/pet';
import { createReservation } from '@/api/reservation';
import type { Pet } from '@/api/pet';
import { createOrderFromReservation } from '@/api/order'
import type { Reservation } from '@/api/reservation'; 
import type { FormInstance, FormRules } from 'element-plus';
import { ElForm, ElFormItem, ElSelect, ElOption, ElDatePicker, ElTimePicker, ElButton, ElMessage } from 'element-plus';
import dayjs from 'dayjs';

// 1. 修改接口：使用 undefined 代替 null
interface ReservationFormData {
  petId: number | undefined;
  reservationDateRange: [string, string] | undefined; // 使用 undefined
  reservationTime: string;
}

const route = useRoute();
const router = useRouter();
const formRef = ref<FormInstance>();
const loading = ref(false);
const userPets = ref<Pet[]>([]);
const serviceId = computed(() => Number(route.params.serviceId));

// 2. 修改初始化：使用 undefined
const form = reactive<ReservationFormData>({
  petId: undefined,
  reservationDateRange: undefined, // 初始化为 undefined
  reservationTime: '',
});

// 3. 修改验证规则 (类型提示更精确)
const rules = reactive<FormRules<ReservationFormData>>({
  petId: [{ required: true, message: '请选择宠物', trigger: 'change' }],
  reservationDateRange: [{ required: true, message: '请选择预约日期范围', trigger: 'change' }],
  reservationTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
});

// --- 日期时间限制 ---
const disabledDate = (time: Date) => {
  return dayjs(time).isBefore(dayjs(), 'day');
}

// 4. 修正 disabledHours 函数：添加 return 语句
const disabledHours = (): number[] => { // 明确返回类型 number[]
  const hours: number[] = [];
  for (let i = 0; i < 24; i++) {
    if (i < 9 || i >= 18) {
      hours.push(i);
    }
  }
  return hours; 
};

const disabledMinutes = (hour: number): number[] => { // 明确返回类型 number[]
    if (hour < 9 || hour >= 18) return Array.from({ length: 60 }, (_, i) => i);
    // 如果需要在 9 点和 17 点有特殊限制，可以在这里添加
    // if (hour === 9) return [...];
    // if (hour === 17) return [...];
    return []; // 默认不禁用任何分钟
}
// --- 日期时间限制结束 ---

const fetchUserPets = async () => {
  loading.value = true; // 开始加载数据，显示loading
  try {
    const res = await getPetList();
    if (res.code === 200 && res.data) {
      userPets.value = res.data;
      console.log('获取到的宠物列表:', userPets.value);
      if (userPets.value.length === 0) {
        ElMessage.info('您当前没有已注册的宠物。');
        // 可选：如果一个宠物都没有，可以禁用提交按钮或给出更强的提示
      }
    } else {
      ElMessage.error(res.message || '获取您的宠物列表失败');
      userPets.value = []; // 清空以确保显示 "No data"
    }
  } catch (error: any) {
    console.error('获取宠物列表时发生错误:', error);
    ElMessage.error('获取宠物列表时发生网络错误或服务异常: ' + error.message);
    userPets.value = [];
  } finally {
    // 注意：这里的 loading.value = false; 可能需要根据您页面的其他加载逻辑来决定何时关闭
    // 如果页面还有其他数据在加载，可能需要在所有数据加载完毕后才关闭 loading
    // 但如果这个 loading 仅用于宠物列表，可以在这里关闭
    loading.value = false; 
  }
};

const submitForm = async () => {
  if (!formRef.value) return;

  try {
    const isValid = await formRef.value.validate();
    if (isValid) {
      if (form.petId === undefined) { ElMessage.warning('请选择宠物'); return; }
      if (!form.reservationDateRange) { ElMessage.warning('请选择预约日期范围'); return; }
      if (!form.reservationTime) { ElMessage.warning('请选择开始时间'); return; }
      if (serviceId.value === undefined || isNaN(serviceId.value)) { ElMessage.error('无效的服务项目ID'); return; }

      loading.value = true;
      let createdReservation: Reservation | null = null; // 用于存储创建的预约信息

      // --- 步骤 1: 创建预约 ---
      try {
        const [startDate, endDate] = form.reservationDateRange;
        const startDateTimeString = `${startDate}T${form.reservationTime}`;

        // !! 仍然需要后端修改 API 以支持日期范围 !!
        const reservationData = {
          petId: form.petId,
          serviceId: serviceId.value,
          reservationStartDate: startDate,
          reservationEndDate: endDate,
          reservationTime: startDateTimeString,
        } as any; // 暂时 as any

        // 调用创建预约 API
        const resReservation = await createReservation(reservationData);

        if (resReservation.code === 200 && resReservation.data) {
          ElMessage.success('预约创建成功，正在生成订单...');
          createdReservation = resReservation.data; // 保存预约信息，特别是 ID 和 amount
        } else {
          ElMessage.error(resReservation.message || '预约失败');
          loading.value = false; // 预约失败，停止 loading
          return; // 阻止后续订单创建
        }

      } catch (error: any) {
        //ElMessage.error(error.message || '提交预约时出错');
        loading.value = false; // 预约出错，停止 loading
        return; // 阻止后续订单创建
      }

      // --- 步骤 2: 如果预约成功，则创建订单 ---
      if (createdReservation && createdReservation.id) {
          try {
              // !! 需要后端创建 /orders/from-reservation 接口 !!
              // !! OrderCreateDTO 可能需要调整，或使用新的DTO !!
              // 需要从 createdReservation 获取必要信息，如 reservationId, amount, userId (或后端从token获取)
              const orderData = {
                  reservationId: createdReservation.id,
                  // userId: ..., // 通常后端从 token 获取
                  // amount: createdReservation.amount, // 后端应该根据 reservation 查金额
              };

              // 调用创建订单 API (假设函数名为 createOrderFromReservation)
              const resOrder = await createOrderFromReservation(orderData);

              if (resOrder.code === 200 && resOrder.data) {
                  ElMessage.success('订单创建成功！');
                  // 创建订单成功后的操作，例如跳转到订单详情页或支付页
                  // router.push({ name: 'OrderDetails', params: { orderId: resOrder.data.id } });
                  // 或直接跳转到我的预约/订单列表
                  router.push({ name: 'MyReservations' }); // 或 'MyOrders'
              } else {
                  // 订单创建失败，但预约可能已创建
                  ElMessage.error(resOrder.message || `订单创建失败 (预约ID: ${createdReservation.id})`);
                  // 考虑是否需要提示用户手动处理或联系客服
              }
          } catch (orderError: any) {
              ElMessage.error(orderError.message || `创建订单时出错 (预约ID: ${createdReservation.id})`);
          } finally {
              loading.value = false; // 无论订单成功与否，结束 loading
          }
      } else {
          // 预约成功但未获取到预约信息，理论上不应发生
          ElMessage.error('无法获取已创建的预约信息以生成订单');
          loading.value = false;
      }

    } else {
      // 表单验证失败
      ElMessage.warning('请检查表单填写是否正确');
    }
  } catch (validationError) {
    console.error('表单验证过程出错:', validationError);
    ElMessage.error('表单验证失败');
  }
};

// 6. 修改重置表单：重置为 undefined
const resetForm = () => {
  if (!formRef.value) return;
  formRef.value.resetFields();
  form.petId = undefined;
  form.reservationDateRange = undefined; // 重置为 undefined
  form.reservationTime = '';
};

onMounted(() => {
  fetchUserPets(); // 组件挂载时调用，获取宠物列表
});
</script>
