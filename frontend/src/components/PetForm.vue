<!-- <template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>{{ isEditMode ? '编辑宠物信息' : '添加新宠物' }}</span>
      </div>
    </template>

    <el-form
      ref="petFormRef"
      :model="formData"
      :rules="formRules"
      label-width="150px"
      label-position="top"
      class="pet-form"
    >
      <el-divider content-position="left"><h3>基本信息</h3></el-divider>
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="宠物照片" prop="photoUrl">
            <el-upload
              class="avatar-uploader"
              action="/your/upload/api"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload"
              v-loading="uploading"
            >
              <img v-if="formData.photoUrl" :src="formData.photoUrl" class="avatar" alt="Pet Avatar"/>
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              <template #tip>
                <div class="el-upload__tip">只能上传 jpg/png 文件，且不超过 2MB</div>
              </template>
            </el-upload>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="宠物昵称" prop="name">
            <el-input v-model="formData.name" placeholder="请输入宠物昵称"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="种类" prop="species">
            <el-radio-group v-model="formData.species">
              <el-radio label="Dog">狗</el-radio>
              <el-radio label="Cat">猫</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="年龄 (岁)" prop="ageYears">
            <el-input-number v-model="formData.ageYears" :min="0" :max="50" placeholder="Years" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="年龄 (月)" prop="ageMonths">
            <el-input-number v-model="formData.ageMonths" :min="0" :max="11" placeholder="Months" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="性别" prop="gender">
            <el-radio-group v-model="formData.gender">
              <el-radio label="Male">雄性</el-radio>
              <el-radio label="Female">雌性</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="体重 (kg)" prop="weight">
            <el-input-number v-model="formData.weight" :precision="1" :step="0.1" :min="0" placeholder="例如: 5.5" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="是否绝育" prop="neutered">
        <el-checkbox v-model="formData.neutered">已绝育</el-checkbox>
      </el-form-item>

      <el-form-item label="宠物介绍" prop="description">
        <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="简单介绍一下您的宠物吧"></el-input>
      </el-form-item>

      <el-divider content-position="left"><h3>健康与习性</h3></el-divider>

      <el-form-item label="疫苗接种情况" prop="vaccinationInfo">
        <el-input v-model="formData.vaccinationInfo" type="textarea" :rows="3" placeholder="请描述疫苗接种情况..."></el-input>
      </el-form-item>

      <el-form-item label="过敏情况" prop="allergies">
        <el-input v-model="formData.allergies" type="textarea" :rows="2" placeholder="请描述已知的过敏源..."></el-input>
      </el-form-item>

      <el-form-item label="健康记录" prop="medicalNotes">
        <el-input v-model="formData.medicalNotes" type="textarea" :rows="3" placeholder="请描述任何已知的健康问题..."></el-input>
      </el-form-item>

      <el-form-item label="性格特点" prop="temperament">
        <el-input v-model="formData.temperament" type="textarea" :rows="2" placeholder="如温顺、活泼"></el-input>
      </el-form-item>

      <el-form-item label="精力水平" prop="energyLevel">
        <el-radio-group v-model="formData.energyLevel">
          <el-radio label="High">高</el-radio>
          <el-radio label="Moderate">中等</el-radio>
          <el-radio label="Low">低</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="喂养计划" prop="feedingSchedule">
        <el-select v-model="formData.feedingSchedule" placeholder="请选择喂养频率" clearable style="width: 100%">
          <el-option label="早上喂食" value="Morning" />
          <el-option label="一天两次" value="Twice a day" />
          <el-option label="自由采食" value="Free feeding" />
          <el-option label="其他" value="Other" />
        </el-select>
      </el-form-item>

      <el-form-item label="喂养特殊说明" prop="feedingInstructions">
        <el-input v-model="formData.feedingInstructions" type="textarea" :rows="2" placeholder="例如: 食物品牌、过敏食物..."></el-input>
      </el-form-item>

      <el-form-item label="如厕频率" prop="pottyBreakFrequency">
        <el-select v-model="formData.pottyBreakFrequency" placeholder="请选择如厕频率" clearable style="width: 100%">
          <el-option label="每小时" value="Every hour" />
          <el-option label="每 2 小时" value="Every 2 hours" />
          <el-option label="每 4 小时" value="Every 4 hours" />
          <el-option label="其他" value="Other" />
        </el-select>
      </el-form-item>

      <el-form-item label="如厕特殊说明" prop="pottyBreakInstructions">
        <el-input v-model="formData.pottyBreakInstructions" type="textarea" :rows="2" placeholder="例如：是否需要尿垫..."></el-input>
      </el-form-item>

      <el-form-item label="可独处时间" prop="aloneTimeTolerance">
        <el-select v-model="formData.aloneTimeTolerance" placeholder="请选择可独处时间" clearable style="width: 100%">
          <el-option label="1 小时或更短" value="1 hour or less" />
          <el-option label="1-4 小时" value="1-4 hours" />
          <el-option label="4-8 小时" value="4-8 hours" />
          <el-option label="8 小时以上" value="8+ hours" />
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="submitForm" :loading="isSubmitting">{{ isEditMode ? '更新宠物信息' : '添加宠物' }}</el-button>
        <el-button @click="resetForm">取消</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue';
import { ElForm, ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { addPet, updatePet } from '@/api/pet';
import type { Pet } from '@/api/pet';
import { useUserStore } from '@/stores/user'; 
interface Props {
  petData?: Pet | null;
}
const props = defineProps<Props>();
const emit = defineEmits(['submitSuccess', 'cancel']);

const petFormRef = ref<InstanceType<typeof ElForm>>();
const isSubmitting = ref(false);
const uploading = ref(false);

const formData = reactive({
  id: undefined as number | undefined,
  name: '',
  species: 'Dog' as 'Dog' | 'Cat',
  ageYears: 0,
  ageMonths: 0,
  gender: 'Male' as 'Male' | 'Female',
  weight: undefined as number | undefined,
  neutered: false,
  photoUrl: '',
  description: '',
  vaccinationInfo: '',
  allergies: '',
  medicalNotes: '',
  temperament: '',
  energyLevel: 'Moderate' as 'High' | 'Moderate' | 'Low',
  feedingSchedule: undefined as string | undefined,
  feedingInstructions: '',
  pottyBreakFrequency: undefined as string | undefined,
  pottyBreakInstructions: '',
  aloneTimeTolerance: undefined as string | undefined,
});

// 修正 rules 中 type 字段
const formRules = reactive<Record<string, import('element-plus').FormItemRule[]>>({
  name: [{ required: true, message: '请输入宠物昵称', trigger: 'blur' }],
  species: [{ required: true, message: '请选择种类', trigger: 'change' }],
  ageYears: [{ type: 'number', min: 0, message: '请输入有效的岁数', trigger: 'blur' }],
  ageMonths: [{ type: 'number', min: 0, max: 11, message: '请输入有效的月数', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
});

const isEditMode = computed(() => !!props.petData?.id);

// 如果是编辑模式，初始化 formData
if (props.petData) {
  const d = props.petData;
  formData.id = d.id;
  formData.name = d.name;
  formData.species = d.species;
  formData.ageYears = d.ageInYears;
  formData.ageMonths = d.ageInMonths;
  formData.gender = d.gender;
  formData.weight = d.weight;
  formData.neutered = d.neutered;
  formData.photoUrl = d.photoUrl;
  formData.description = d.description;
  formData.vaccinationInfo = d.vaccinationInfo;
  formData.allergies = d.allergies;
  formData.medicalNotes = d.medicalNotes;
  formData.temperament = d.temperament;
  formData.energyLevel = d.energyLevel;
  formData.feedingSchedule = d.feedingSchedule;
  formData.feedingInstructions = d.feedingInstructions;
  formData.pottyBreakFrequency = d.pottyBreakFrequency;
  formData.pottyBreakInstructions = d.pottyBreakInstructions;
  formData.aloneTimeTolerance = d.aloneTimeTolerance;
}

const handleAvatarSuccess = (response: any) => {
  uploading.value = false;
  if (response.code === 200 && response.data?.url) {
    formData.photoUrl = response.data.url;
    ElMessage.success('图片上传成功!');
  } else {
    ElMessage.error(response.message || '图片上传失败!');
  }
};

const beforeAvatarUpload = (rawFile: File) => {
  uploading.value = true;
  const isJPG = rawFile.type === 'image/jpeg';
  const isPNG = rawFile.type === 'image/png';
  const isLt2M = rawFile.size / 1024 / 1024 < 2;
  if (!isJPG && !isPNG) {
    ElMessage.error('只能上传 JPG/PNG 格式图片!');
    uploading.value = false;
    return false;
  }
  if (!isLt2M) {
    ElMessage.error('上传图片大小不能超过 2MB!');
    uploading.value = false;
    return false;
  }
  return true;
};

const resetForm = () => {
  petFormRef.value?.resetFields();
  Object.assign(formData, {
    id: undefined,
    name: '',
    species: 'Dog',
    ageYears: 0,
    ageMonths: 0,
    gender: 'Male',
    weight: undefined,
    neutered: false,
    photoUrl: '',
    description: '',
    vaccinationInfo: '',
    allergies: '',
    medicalNotes: '',
    temperament: '',
    energyLevel: 'Moderate',
    feedingSchedule: undefined,
    feedingInstructions: '',
    pottyBreakFrequency: undefined,
    pottyBreakInstructions: '',
    aloneTimeTolerance: undefined,
  });
};

const submitForm = async () => {
  if (!petFormRef.value || isSubmitting.value) return;

  try {
    await petFormRef.value.validate();
    isSubmitting.value = true;

    const baseData = {
      name: formData.name,
      species: formData.species,
      gender: formData.gender,
      ageInYears: formData.ageYears,
      ageInMonths: formData.ageMonths,
      weight: formData.weight ?? 0,
      neutered: formData.neutered,
      photoUrl:	formData.photoUrl,
      description: formData.description || '',
      vaccinationInfo: formData.vaccinationInfo || '',
      allergies: formData.allergies || '',
      medicalNotes: formData.medicalNotes || '',
      temperament: formData.temperament || '',
      energyLevel: formData.energyLevel || 'Moderate',
      feedingSchedule: formData.feedingSchedule || '',
      feedingInstructions: formData.feedingInstructions || '',
      pottyBreakFrequency: formData.pottyBreakFrequency || '',
      pottyBreakInstructions: formData.pottyBreakInstructions || '',
      aloneTimeTolerance: formData.aloneTimeTolerance || '',
    };
    const userStore = useUserStore();
    const userId = userStore.userInfo?.id;
if (!userId) {
  // 处理未获取到用户信息的情况（理论上如果 token 有效，userInfo 应该也存在）
  console.error('无法获取用户ID，请确保已登录并获取了用户信息');
  ElMessage.error('无法获取用户信息，请重新登录');
  // 可能需要重新获取用户信息或跳转登录
  return;
}
    if (isEditMode.value) {
      if (formData.id === undefined) throw new Error('缺少宠物 ID');
      const updateData: Pet = { id: formData.id, userId, ...baseData };
      const res = await updatePet(updateData);
      if (res.code === 200) {
        ElMessage.success('宠物信息更新成功!'); emit('submitSuccess');
      } else {
        ElMessage.error(res.message || '更新失败');
      }
    } else {
      const addData: Omit<Pet,'id'> = { userId, ...baseData };
      const res = await addPet(addData);
      if (res.code === 200) {
        ElMessage.success('宠物添加成功!'); emit('submitSuccess'); resetForm();
      } else {
        ElMessage.error(res.message || '添加失败');
      }
    }
  } catch (error: any) {
    console.error('提交失败', error);
    ElMessage.error(error.message || '提交失败，请检查表单');
  } finally {
    isSubmitting.value = false;
  }
};
</script>

<style scoped lang="scss">
.pet-form { max-width: 800px;	margin: 0 auto; }
.card-header { font-weight: bold; font-size: 1.2em; }
.avatar-uploader .avatar { width: 120px; height: 120px; object-fit: cover; }
.avatar-uploader .el-upload { border: 1px dashed var(--el-border-color); border-radius: 6px; }
.avatar-uploader .el-upload:hover { border-color: var(--el-color-primary); }
.el-icon.avatar-uploader-icon { font-size: 28px; color: #8c939d; width: 120px; height: 120px; text-align: center; }
</style> -->


<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>{{ isEditMode ? '编辑宠物信息' : '添加新宠物' }}</span>
      </div>
    </template>

    <el-form
      ref="petFormRef"
      :model="formData"
      :rules="formRules"
      label-width="150px"
      label-position="top"
      class="pet-form"
    >
      <el-divider content-position="left"><h3>基本信息</h3></el-divider>

      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="宠物照片" prop="photoUrl">
            <el-upload
              class="avatar-uploader"
              action="/api/upload"
              :show-file-list="false"
              :headers="uploadHeaders" 
              :on-success="handleAvatarSuccess"
              :on-error="handleAvatarError"
              :before-upload="beforeAvatarUpload"
              v-loading="uploading"
            >
              <img
                v-if="formData.photoUrl"
                :src="formData.photoUrl"
                class="avatar"
                alt="Pet Avatar"
              />
              <el-icon v-else class="avatar-uploader-icon">
                <Plus />
              </el-icon>
              <template #tip>
                <div class="el-upload__tip">
                  只能上传 jpg/png 格式，且不超过 2MB
                </div>
              </template>
            </el-upload>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="宠物昵称" prop="name">
            <el-input v-model="formData.name" placeholder="请输入宠物昵称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="种类" prop="species">
            <el-radio-group v-model="formData.species">
              <el-radio value="Dog">狗</el-radio>
              <el-radio value="Cat">猫</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="年龄 (岁)" prop="ageYears">
            <el-input-number
              v-model="formData.ageYears"
              :min="0"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="年龄 (月)" prop="ageMonths">
            <el-input-number
              v-model="formData.ageMonths"
              :min="0"
              :max="11"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="性别" prop="gender">
            <el-radio-group v-model="formData.gender">
              <el-radio value="Male">雄性</el-radio>
              <el-radio value="Female">雌性</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="体重 (kg)" prop="weight">
            <el-input-number
              v-model="formData.weight"
              :precision="1"
              :step="0.1"
              :min="0"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="是否绝育" prop="neutered">
        <el-checkbox v-model="formData.neutered">已绝育</el-checkbox>
      </el-form-item>

      <el-form-item label="宠物介绍" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
        />
      </el-form-item>

      <el-divider content-position="left"><h3>健康与习性</h3></el-divider>

      <el-form-item label="疫苗接种情况" prop="vaccinationInfo">
        <el-input
          v-model="formData.vaccinationInfo"
          type="textarea"
          :rows="3"
        />
      </el-form-item>

      <el-form-item label="过敏情况" prop="allergies">
        <el-input
          v-model="formData.allergies"
          type="textarea"
          :rows="2"
        />
      </el-form-item>

      <el-form-item label="健康记录" prop="medicalNotes">
        <el-input
          v-model="formData.medicalNotes"
          type="textarea"
          :rows="3"
        />
      </el-form-item>

      <el-form-item label="性格特点" prop="temperament">
        <el-input
          v-model="formData.temperament"
          type="textarea"
          :rows="2"
        />
      </el-form-item>

      <el-form-item label="精力水平" prop="energyLevel">
        <el-radio-group v-model="formData.energyLevel">
          <el-radio value="High">高</el-radio>
          <el-radio value="Moderate">中等</el-radio>
          <el-radio value="Low">低</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="喂养计划" prop="feedingSchedule">
        <el-select v-model="formData.feedingSchedule" clearable>
          <el-option label="早上喂食" value="Morning" />
          <el-option label="一天两次" value="Twice a day" />
          <el-option label="自由采食" value="Free feeding" />
          <el-option label="其他" value="Other" />
        </el-select>
      </el-form-item>

      <el-form-item label="喂养特殊说明" prop="feedingInstructions">
        <el-input
          v-model="formData.feedingInstructions"
          type="textarea"
          :rows="2"
        />
      </el-form-item>

      <el-form-item label="如厕频率" prop="pottyBreakFrequency">
        <el-select v-model="formData.pottyBreakFrequency" clearable>
          <el-option label="每小时" value="Every hour" />
          <el-option label="每 2 小时" value="Every 2 hours" />
          <el-option label="每 4 小时" value="Every 4 hours" />
          <el-option label="其他" value="Other" />
        </el-select>
      </el-form-item>

      <el-form-item label="如厕特殊说明" prop="pottyBreakInstructions">
        <el-input
          v-model="formData.pottyBreakInstructions"
          type="textarea"
          :rows="2"
        />
      </el-form-item>

      <el-form-item label="可独处时间" prop="aloneTimeTolerance">
        <el-select v-model="formData.aloneTimeTolerance" clearable>
          <el-option label="1 小时或更短" value="1 hour or less" />
          <el-option label="1-4 小时" value="1-4 hours" />
          <el-option label="4-8 小时" value="4-8 hours" />
          <el-option label="8 小时以上" value="8+ hours" />
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="submitForm" :loading="isSubmitting">
          {{ isEditMode ? '更新宠物信息' : '添加宠物' }}
        </el-button>
        <el-button @click="resetForm">取消</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'; // 建议添加 onMounted 如果需要在编辑模式下初始化
import {
  ElForm,
  ElMessage,
  ElInput,
  ElInputNumber,
  ElRadioGroup,
  ElRadio,
  ElCheckbox,
  ElSelect,
  ElOption,
  ElButton,
  ElUpload,
  ElIcon,
  ElDivider,
  ElRow,
  ElCol,
  ElCard,
  type FormInstance, // 用于 ElForm 实例类型
  type FormItemRule, // 用于表单规则类型
  type UploadProps,  // 【添加】用于上传组件回调类型
  type UploadRawFile // 【添加】用于 beforeUpload 的文件类型
} from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { addPet, updatePet } from '@/api/pet';
import type { Pet } from '@/api/pet'; // Pet 接口应包含所有字段，包括 id 和 userId
import { useUserStore } from '@/stores/user';
import type { BackendResult } from '@/types/api'; // 假设后端上传接口也返回 BackendResult

const userStore = useUserStore();
const petFormRef = ref<FormInstance>(); // 使用 FormInstance 类型
const isSubmitting = ref(false);
const uploading = ref(false);

interface Props {
  petData?: Pet | null; // 宠物数据，用于编辑模式
}
const props = defineProps<Props>();
const emit = defineEmits(['submitSuccess', 'cancel']);

// 表单数据模型
const formData = reactive({
  id: undefined as number | undefined,
  name: '',
  species: 'Dog' as 'Dog' | 'Cat',
  ageYears: 0, // API 中是 ageInYears
  ageMonths: 0, // API 中是 ageInMonths
  gender: 'Male' as 'Male' | 'Female',
  weight: undefined as number | undefined,
  neutered: false,
  photoUrl: '',
  description: '',
  vaccinationInfo: '',
  allergies: '',
  medicalNotes: '',
  temperament: '',
  energyLevel: 'Moderate' as 'High' | 'Moderate' | 'Low',
  feedingSchedule: '' as string, // 初始化为空字符串或 undefined
  feedingInstructions: '',
  pottyBreakFrequency: '' as string, // 初始化为空字符串或 undefined
  pottyBreakInstructions: '',
  aloneTimeTolerance: '' as string, // 初始化为空字符串或 undefined
});

// 表单验证规则
const formRules = reactive<Record<string, FormItemRule[]>>({
  name: [{ required: true, message: '请输入宠物昵称', trigger: 'blur' }],
  species: [{ required: true, message: '请选择种类', trigger: 'change' }],
  ageYears: [
    { type: 'number', message: '岁数必须是数字', trigger: 'blur' },
    { validator: (rule, value, callback) => {
        if (value < 0) {
          callback(new Error('岁数不能为负'));
        } else if (value > 50) {
          callback(new Error('岁数不能超过50'));
        } else {
          callback();
        }
      }, trigger: 'blur' }
  ],
  ageMonths: [
    { type: 'number', message: '月数必须是数字', trigger: 'blur' },
    { validator: (rule, value, callback) => {
        if (value < 0) {
          callback(new Error('月数不能为负'));
        } else if (value > 11) {
          callback(new Error('月数不能超过11'));
        } else {
          callback();
        }
      }, trigger: 'blur' }
  ],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  weight: [{ type: 'number', min: 0, message: '体重不能为负', trigger: 'blur', transform: (value) => Number(value) || 0 }],
});

const isEditMode = computed(() => !!props.petData?.id);

// 初始化表单数据 (用于编辑模式)
const initializeForm = (data: Pet | null | undefined) => {
  if (data) {
    formData.id = data.id;
    formData.name = data.name;
    formData.species = data.species;
    formData.ageYears = data.ageInYears; // 注意字段名匹配
    formData.ageMonths = data.ageInMonths; // 注意字段名匹配
    formData.gender = data.gender;
    formData.weight = data.weight;
    formData.neutered = data.neutered;
    formData.photoUrl = data.photoUrl;
    formData.description = data.description;
    formData.vaccinationInfo = data.vaccinationInfo;
    formData.allergies = data.allergies;
    formData.medicalNotes = data.medicalNotes;
    formData.temperament = data.temperament;
    formData.energyLevel = data.energyLevel;
    formData.feedingSchedule = data.feedingSchedule;
    formData.feedingInstructions = data.feedingInstructions;
    formData.pottyBreakFrequency = data.pottyBreakFrequency;
    formData.pottyBreakInstructions = data.pottyBreakInstructions;
    formData.aloneTimeTolerance = data.aloneTimeTolerance;
  } else {
     resetFormInternal(); // 如果没有 petData (例如直接访问添加页)，确保是初始状态
  }
};

// onMounted 或 watch 来初始化表单，确保 props.petData 加载后表单能正确填充
onMounted(() => {
  if (isEditMode.value && props.petData) {
    initializeForm(props.petData);
  }
});
// 如果 petData 可能会异步变化，可以添加一个 watch
// import { watch } from 'vue';
// watch(() => props.petData, (newData) => {
//   initializeForm(newData);
// });


const uploadHeaders = ref({
  Authorization: `Bearer ${localStorage.getItem('token') || ''}`
});

const handleAvatarSuccess: UploadProps['onSuccess'] = (
  response: BackendResult<{ url: string }>, // 明确 response 类型
  // uploadFile: UploadFile // UploadFile 类型也从 element-plus 导入
) => {
  uploading.value = false;
  if (response.code === 200 && response.data?.url) {
    formData.photoUrl = response.data.url;
    ElMessage.success('图片上传成功!');
  } else {
    ElMessage.error(response.message || '图片上传失败!');
  }
};

const handleAvatarError: UploadProps['onError'] = (error: Error /*, uploadFile: UploadFile, uploadFiles: UploadFiles */) => {
  uploading.value = false;
  let errorMessage = '图片上传失败，请重试';
   if (error.message) { // error 通常是 Error 对象
    try {
      // Axios 的错误对象通常在 error.response.data 中包含服务器返回的JSON
      // 或者 error.message 可能已经是序列化的JSON字符串 (如果请求库这样处理)
      // 这里的解析逻辑取决于您的 HTTP 客户端和错误处理方式
      const parsedError = JSON.parse(error.message); // 尝试解析
      if (parsedError.message) {
        errorMessage = parsedError.message;
      } else if (parsedError.code && parsedError.data) { // 如果是 BackendResult 结构
        errorMessage = parsedError.data || '上传失败';
      }
    } catch (e) {
      // 如果解析失败，使用 error.message
      if (error.message.includes('Network Error')) {
        errorMessage = '网络错误，请检查连接。';
      } else if (error.message.includes('timeout')) {
        errorMessage = '上传超时。';
      } else if (error.message.includes('status code 401')) {
        errorMessage = '未授权，请重新登录后尝试。';
      } else if (error.message.includes('status code 413')) {
        errorMessage = '文件过大，请上传小于2MB的图片。';
      }
    }
  }
  ElMessage.error(errorMessage);
};

const beforeAvatarUpload: UploadProps['beforeUpload'] = (rawFile: UploadRawFile) => {
  uploading.value = true;
  const allowedTypes = ['image/jpeg', 'image/png'];
  const isImg = allowedTypes.includes(rawFile.type);
  const isLt2M = rawFile.size / 1024 / 1024 < 2;

  if (!isImg) {
    ElMessage.error('只能上传 JPG/PNG 格式的图片!');
    uploading.value = false;
    return false;
  }
  if (!isLt2M) {
    ElMessage.error('上传图片大小不能超过 2MB!');
    uploading.value = false;
    return false;
  }
  return true;
};

const resetFormInternal = () => { // 内部重置逻辑
  Object.assign(formData, {
    id: undefined, name: '', species: 'Dog', ageYears: 0, ageMonths: 0, gender: 'Male',
    weight: undefined, neutered: false, photoUrl: '', description: '', vaccinationInfo: '',
    allergies: '', medicalNotes: '', temperament: '', energyLevel: 'Moderate',
    feedingSchedule: '', feedingInstructions: '', pottyBreakFrequency: '',
    pottyBreakInstructions: '', aloneTimeTolerance: '',
  });
};


const resetForm = () => {
  petFormRef.value?.resetFields(); // Element Plus 的重置，会重置到初始值
  resetFormInternal(); // 强制重置为我们的默认初始值
  emit('cancel'); // 如果有取消事件需要通知父组件
};

const submitForm = async () => {
  if (!petFormRef.value) {
    ElMessage.error('表单引用未就绪!');
    return;
  }
  if (isSubmitting.value) return;

  try {
    await petFormRef.value.validate();
    isSubmitting.value = true;

    const userId = userStore.userInfo?.id;
    if (!userStore.token || !userId) {
      ElMessage.error('用户未登录或信息不完整，请重新登录。');
      isSubmitting.value = false;
      return;
    }

    // 从 formData 准备提交给 API 的数据
    // Pet 接口定义了所有字段，后端 DTO (PetCreateDTO, PetUpdateDTO) 可能只包含部分
    const apiPayload = {
      name: formData.name,
      species: formData.species,
      gender: formData.gender,
      ageInYears: formData.ageYears, // 后端 Pet 实体和 DTO 使用的是 ageInYears
      ageInMonths: formData.ageMonths, // 后端 Pet 实体和 DTO 使用的是 ageInMonths
      weight: formData.weight ?? 0,
      neutered: formData.neutered,
      photoUrl: formData.photoUrl,
      description: formData.description,
      vaccinationInfo: formData.vaccinationInfo,
      allergies: formData.allergies,
      medicalNotes: formData.medicalNotes,
      temperament: formData.temperament,
      energyLevel: formData.energyLevel,
      feedingSchedule: formData.feedingSchedule,
      feedingInstructions: formData.feedingInstructions,
      pottyBreakFrequency: formData.pottyBreakFrequency,
      pottyBreakInstructions: formData.pottyBreakInstructions,
      aloneTimeTolerance: formData.aloneTimeTolerance,
    };

    if (isEditMode.value) {
      if (formData.id == null) {
        ElMessage.error('缺少宠物ID，无法更新');
        isSubmitting.value = false;
        return;
      }
      const updateData: Pet = {
        id: formData.id,
        userId: userId, // userId 必须从 UserStore 获取
        ...apiPayload
      };
      const res = await updatePet(updateData);
      if (res.code === 200) {
        ElMessage.success('宠物信息更新成功!');
        emit('submitSuccess', res.data || updateData); // 返回更新后的数据
      } else {
        ElMessage.error(res.message || '更新宠物信息失败');
      }
    } else {
      // addPet API 期望 Omit<Pet, 'id' | 'userId'>
      // 后端 PetServiceImpl 会从当前登录用户设置 userId
      const addData: Omit<Pet, 'id' | 'userId'> = apiPayload;
      const res = await addPet(addData);
      if (res.code === 200) {
        ElMessage.success('宠物添加成功!');
        emit('submitSuccess', res.data);
        resetForm();
      } else {
        ElMessage.error(res.message || '添加宠物失败');
      }
    }
  } catch (error) { // 通常是表单验证失败
    console.log('表单验证失败或提交出错:', error);
    // ElMessage.error('请检查表单填写是否正确'); // el-form 自动处理验证错误提示
  } finally {
    isSubmitting.value = false;
  }
};
</script>

<style scoped lang="scss">
.pet-form {
  max-width: 800px;
  margin: 0 auto;
  padding-bottom: 20px; /* 给底部按钮一些空间 */
}
.card-header {
  font-weight: bold;
  font-size: 1.2em;
  text-align: center;
}
.avatar-uploader .avatar {
  width: 120px;
  height: 120px;
  object-fit: cover;
  display: block; /* 确保图片是块级元素 */
}
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}
.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}
.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px; /* 与 avatar 尺寸一致 */
  height: 120px; /* 与 avatar 尺寸一致 */
  line-height: 120px; /* 垂直居中 */
  text-align: center;
}
.el-form-item {
  margin-bottom: 22px; /* 统一表单项间距 */
}
.el-select, .el-input-number {
  width: 100%; /* 让选择框和数字输入框充满其容器 */
}
</style>