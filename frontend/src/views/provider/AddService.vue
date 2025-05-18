<template>
  <el-container direction="vertical">
    <el-header height="auto">
      <h1>添加新服务</h1>
    </el-header>
    <el-main>
      <el-card shadow="never">
        <el-form
          ref="formRef"
          :model="serviceForm"
          :rules="formRules"
          label-width="160px"
          label-position="right"
          style="max-width: 800px; margin: auto;"
          @submit.prevent="handleSubmit"
          v-loading="loading"
        >
          <el-form-item label="服务名称" prop="name">
            <el-input v-model="serviceForm.name" placeholder="例如：宠物洗澡美容" clearable />
          </el-form-item>

          <el-form-item label="服务描述" prop="description">
            <el-input
              type="textarea"
              v-model="serviceForm.description"
              :rows="3"
              placeholder="详细描述服务内容、流程、包含的项目等"
              clearable
            />
          </el-form-item>

          <el-form-item label="价格 (元)" prop="price">
            <el-input-number v-model="serviceForm.price" :min="0" :precision="2" controls-position="right" style="width: 100%;" />
          </el-form-item>

          <el-form-item label="服务时长 (分钟)" prop="duration">
            <el-input-number v-model="serviceForm.duration" :min="1" :step="10" controls-position="right" style="width: 100%;" />
          </el-form-item>

          <el-form-item label="每日服务容量" prop="dailyCapacity">
            <el-input-number v-model="serviceForm.dailyCapacity" :min="1" controls-position="right" style="width: 100%;" placeholder="每天最多可接待的宠物数量" />
          </el-form-item>

          <el-divider content-position="left">宠物要求 (可选)</el-divider>

          <el-form-item label="要求接种的疫苗" prop="requiredVaccinations">
            <el-input v-model="serviceForm.requiredVaccinations" placeholder="例如：狂犬疫苗,犬瘟热疫苗 (用逗号分隔)" clearable />
          </el-form-item>

          <el-form-item label="是否要求绝育" prop="requiresNeutered">
            <el-switch v-model="serviceForm.requiresNeutered" />
             <el-tooltip content="如果开启，只有已绝育的宠物才能预约此服务" placement="top-start">
                 <el-icon style="margin-left: 8px; color: #909399;"><QuestionFilled /></el-icon>
             </el-tooltip>
          </el-form-item>

          <el-form-item label="最低年龄 (岁)" prop="minAge">
             <el-input-number v-model="serviceForm.minAge" :min="0" controls-position="right" style="width: 100%;" placeholder="不填则无限制" />
          </el-form-item>

          <el-form-item label="最高年龄 (岁)" prop="maxAge">
            <el-input-number v-model="serviceForm.maxAge" :min="serviceForm.minAge ?? 0" controls-position="right" style="width: 100%;" placeholder="不填则无限制" />
             <el-text v-if="serviceForm.maxAge !== undefined && serviceForm.minAge !== undefined && serviceForm.maxAge < serviceForm.minAge" type="danger" size="small">最高年龄不能小于最低年龄</el-text>
          </el-form-item>

          <el-form-item label="性格要求" prop="temperamentRequirements">
            <el-input v-model="serviceForm.temperamentRequirements" placeholder="例如：仅限友好, 不接受攻击性宠物" clearable />
          </el-form-item>

          <el-form-item label="不接受的品种" prop="prohibitedBreeds">
            <el-input v-model="serviceForm.prohibitedBreeds" placeholder="例如：比特犬,罗威纳 (用逗号分隔)" clearable />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" native-type="submit" :loading="loading">提交审核</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import {
  ElContainer, ElHeader, ElMain, ElCard, ElForm, ElFormItem, ElInput,
  ElInputNumber, ElSwitch, ElButton, ElMessage, ElDivider, ElIcon, ElTooltip, ElText, vLoading
} from 'element-plus';
import { QuestionFilled } from '@element-plus/icons-vue'; // 导入图标
import type { FormInstance, FormRules } from 'element-plus';
// 导入你的 API 函数
import { addService } from '@/api/service';
// 导入或定义 Service 的类型 (部分，仅用于表单)
interface ServiceFormData {
  name: string;
  description: string;
  price: number | undefined;
  duration: number | undefined;
  dailyCapacity: number | undefined;
  requiredVaccinations: string;
  requiresNeutered: boolean;
  minAge: number | undefined;
  maxAge: number | undefined;
  temperamentRequirements: string;
  prohibitedBreeds: string;
}

const router = useRouter();
const formRef = ref<FormInstance>();
const loading = ref(false);

// 表单数据模型
const serviceForm = reactive<ServiceFormData>({
  name: '',
  description: '',
  price: undefined,
  duration: 60, // 默认值示例
  dailyCapacity: 5, // 默认值示例
  requiredVaccinations: '',
  requiresNeutered: false,
  minAge: undefined,
  maxAge: undefined,
  temperamentRequirements: '',
  prohibitedBreeds: '',
});

// 表单验证规则
const formRules = reactive<FormRules<ServiceFormData>>({
  name: [{ required: true, message: '请输入服务名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入服务描述', trigger: 'blur' }],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' },
    { type: 'number', message: '价格必须是数字', trigger: 'change' }
  ],
  duration: [
    { required: true, message: '请输入服务时长', trigger: 'blur' },
    { type: 'integer', message: '时长必须是整数', trigger: 'change' }
  ],
  dailyCapacity: [
    { required: true, message: '请输入每日容量', trigger: 'blur' },
    { type: 'integer', message: '容量必须是整数', trigger: 'change' }
  ],
  // 其他字段可以根据需要添加验证规则
});

// 处理表单提交
const handleSubmit = async () => {
  if (!formRef.value) return;
  try {
    await formRef.value.validate(); // 触发表单验证
    loading.value = true;

     // 准备提交给后端的数据，确保类型正确，并排除后端不需要的字段
     // 注意：addService API 函数期望的参数类型可能需要调整
     // 这里假设 addService 接受 ServiceFormData 类型，并能被后端正确处理
     // 如果后端严格需要 Omit<Service, ...> 类型，你可能需要在这里做转换
    const dataToSubmit = { ...serviceForm };

    // 清理可选字段的空字符串或 undefined 为 null (如果后端需要 null)
    // dataToSubmit.minAge = dataToSubmit.minAge ?? null; // 示例
    // dataToSubmit.maxAge = dataToSubmit.maxAge ?? null; // 示例
    // dataToSubmit.requiredVaccinations = dataToSubmit.requiredVaccinations || null; //示例

    const res = await addService(dataToSubmit as any); // 使用 as any 暂时绕过类型检查，最好调整API函数或类型定义

    if (res.code === 200) {
      ElMessage.success('服务添加成功，已提交管理员审核！');
      // 提交成功后跳转回服务列表页
      router.push('/provider/services');
    } else {
      ElMessage.error(res.message || '添加服务失败');
    }
  } catch (error) {
    // 如果是表单验证失败，validate 会 reject，无需额外处理
    // 如果是 API 调用失败
    if (error instanceof Error) {
       ElMessage.error(`提交失败: ${error.message}`);
    } else {
       console.error('表单提交或 API 调用出错:', error);
       ElMessage.error('提交服务时发生未知错误');
    }
  } finally {
    loading.value = false;
  }
};

// 重置表单
const handleReset = () => {
  formRef.value?.resetFields();
};

</script>

<style scoped>
.el-header {
  border-bottom: 1px solid var(--el-border-color-light);
  margin-bottom: 20px;
}
/* 可以添加更多样式 */
</style>