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
import { ref, reactive, computed } from 'vue';
import { ElForm, ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { addPet, updatePet } from '@/api/pet';
import type { Pet } from '@/api/pet';
import { useUserStore } from '@/stores/user';

const userStore = useUserStore();
const petFormRef = ref<InstanceType<typeof ElForm>>();
const isSubmitting = ref(false);
const uploading = ref(false);

interface Props {
  petData?: Pet | null;
}
const props = defineProps<Props>();
// 运行时写法，不使用 TS 元组泛型
const emit = defineEmits(['submitSuccess', 'cancel']);

const formData = reactive({
  id: undefined as number|undefined,
  name: '',
  species: 'Dog' as 'Dog'|'Cat',
  ageYears: 0,
  ageMonths: 0,
  gender: 'Male' as 'Male'|'Female',
  weight: undefined as number|undefined,
  neutered: false,
  photoUrl: '',
  description: '',
  vaccinationInfo: '',
  allergies: '',
  medicalNotes: '',
  temperament: '',
  energyLevel: 'Moderate' as 'High'|'Moderate'|'Low',
  feedingSchedule: undefined as string|undefined,
  feedingInstructions: '',
  pottyBreakFrequency: undefined as string|undefined,
  pottyBreakInstructions: '',
  aloneTimeTolerance: undefined as string|undefined,
});

const formRules = reactive<Record<string, import('element-plus').FormItemRule[]>>({
  name: [{ required: true, message: '请输入宠物昵称', trigger: 'blur' }],
  species: [{ required: true, message: '请选择种类', trigger: 'change' }],
  ageYears: [{ type: 'number', min: 0, message: '请输入有效岁数', trigger: 'blur' }],
  ageMonths: [{ type: 'number', min: 0, max: 11, message: '请输入有效月数', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
});

const isEditMode = computed(() => !!props.petData?.id);
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

const handleAvatarSuccess = (res: any) => {
  uploading.value = false;
  if (res.code === 200 && res.data?.url) {
    formData.photoUrl = res.data.url;
    ElMessage.success('图片上传成功!');
  } else {
    ElMessage.error(res.message || '上传失败');
  }
};
const handleAvatarError = () => {
  uploading.value = false;
  ElMessage.error('上传失败，请重试');
};
const beforeAvatarUpload = (file: File) => {
  uploading.value = true;
  const isImg = file.type === 'image/jpeg' || file.type === 'image/png';
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isImg) {
    ElMessage.error('只能上传 JPG/PNG 格式');
    uploading.value = false;
    return false;
  }
  if (!isLt2M) {
    ElMessage.error('大小不能超过 2MB');
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
  console.log('submitForm 函数被点击调用了！');
  console.log('检查 petFormRef:', petFormRef.value);
  console.log('检查 isSubmitting:', isSubmitting.value);

  if (!petFormRef.value) {
    console.error('错误：无法获取到 el-form 的引用 (petFormRef)!');
    ElMessage.error('无法提交，表单组件未正确加载。');
    return;
  }
  if (isSubmitting.value) {
    console.warn('submitForm 提前退出：isSubmitting 为 true');
    return;
  }

  try {
    console.log('尝试执行 validate...');
    await petFormRef.value.validate();
    console.log('validate 通过');
    isSubmitting.value = true;

    // 调试日志，检查用户状态
    console.log('isEditMode:', isEditMode.value);
    console.log('User Info (from store):', JSON.stringify(userStore.userInfo)); // 使用 JSON.stringify 查看对象内容
    console.log('User Token (from store):', userStore.token);

    const userId = userStore.userInfo?.id;
    console.log('Extracted userId:', userId);

    // 统一的登录状态检查
    if (!userStore.token) { // 后端 API 通常需要 token 进行用户识别和授权
        console.error('CONDITION MET: User token is missing. Returning...');
        ElMessage.error('请先登录或登录状态已失效');
        isSubmitting.value = false;
        return;
    }
    // 对于编辑操作，userId 是必须的；对于添加操作，后端会从token获取，但前端也需要它来构造某些对象
    if (!userId) {
        console.error('CONDITION MET: User ID is missing from store. Returning...');
        ElMessage.error('无法获取用户信息，请尝试重新登录');
        isSubmitting.value = false;
        return;
    }

    console.log('Token and userId checks passed. Proceeding to prepare data.');

    const baseData = {
      name: formData.name,
      species: formData.species,
      gender: formData.gender,
      ageInYears: formData.ageYears,
      ageInMonths: formData.ageMonths,
      weight: formData.weight ?? 0, // 后端 PetCreateDTO 定义了这些字段，不含 userId
      neutered: formData.neutered,
      photoUrl: formData.photoUrl,
      description: formData.description,
      vaccinationInfo: formData.vaccinationInfo,
      allergies: formData.allergies,
      medicalNotes: formData.medicalNotes,
      temperament: formData.temperament,
      energyLevel: formData.energyLevel,
      feedingSchedule: formData.feedingSchedule || '',
      feedingInstructions: formData.feedingInstructions || '',
      pottyBreakFrequency: formData.pottyBreakFrequency || '',
      pottyBreakInstructions: formData.pottyBreakInstructions || '',
      aloneTimeTolerance: formData.aloneTimeTolerance || '',
    };

    if (isEditMode.value) {
      console.log('Executing updatePet logic...');
      if (formData.id == null) { // 检查 formData.id 是否存在
        console.error('Missing pet ID in edit mode.');
        ElMessage.error('缺少宠物ID，无法更新');
        isSubmitting.value = false;
        return;
      }
      // Pet 类型通常包含 id 和 userId
      const updatePayload: Pet = { 
        id: formData.id, 
        userId: userId, // 从 store 获取的 userId
        ...baseData 
      };
      console.log('Update payload:', updatePayload);
      const res = await updatePet(updatePayload);
      if (res.code === 200) {
        ElMessage.success('宠物信息更新成功!');
        emit('submitSuccess', res.data); // 触发成功事件
      } else {
        ElMessage.error(res.message || '更新宠物信息失败');
      }
    } else {
      // --- 添加宠物的逻辑 ---
      console.log('Executing addPet logic...');
      // addPet API 期望的参数类型是 Omit<Pet, 'id' | 'userId'>
      // 所以 baseData 本身就是符合的，因为它不包含 id 和 userId
      const addPayload: Omit<Pet, 'id' | 'userId'> = baseData;
      console.log('Add payload:', addPayload);
      const res = await addPet(addPayload);
      if (res.code === 200) {
        ElMessage.success('宠物添加成功!');
        emit('submitSuccess', res.data); // 触发成功事件
        resetForm(); // 添加成功后重置表单
      } else {
        ElMessage.error(res.message || '添加宠物失败');
      }
    }
  } catch (error: any) { // 这通常会捕获 validate() 失败的 Promise.reject
    console.error('表单提交或验证过程中发生错误:', error);
    // ElMessage.error(error.message || '提交失败，请检查表单是否正确填写'); // validate 失败时 el-form 会自动提示
  } finally {
    isSubmitting.value = false;
    console.log('Submission process finished.');
  }
};


</script>

<style scoped lang="scss">
.pet-form {
  max-width: 800px;
  margin: 0 auto;
}
.card-header {
  font-weight: bold;
  font-size: 1.2em;
}
.avatar-uploader .avatar {
  width: 120px;
  height: 120px;
  object-fit: cover;
}
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
}
.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}
.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  text-align: center;
}
</style>