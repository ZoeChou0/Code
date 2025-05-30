
<template>
  <div class="edit-profile-page">
    <header class="page-header">
      <h1 class="header-title">编辑个人资料</h1>
      <h3 class="header-subtitle">更新您的个人信息</h3>
    </header>

    <el-alert
      v-if="alert.message"
      :type="alert.type"
      :title="alert.message"
      class="mb-4"
      show-icon
      closable
      @close="alert.message = ''"
    />

    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="100px"
      class="account-info-form"
      size="default"
      v-loading="pageLoading"
      element-loading-text="加载中..."
      @submit.prevent="submitForm"
    >
      <el-alert
        v-if="!pageLoading && loadError"
        title="加载用户信息失败"
        type="error"
        :closable="false"
        show-icon
        class="mb-4"
      >
        无法加载用户信息，请稍后重试或联系管理员。
        <el-button link type="primary" @click="router.back()">返回</el-button>
      </el-alert>

      <div v-if="!pageLoading && !loadError">
        <h3 class="form-section-title">基本信息</h3>

        <el-form-item label="地址" prop="addressLine1">
          <el-input
            v-model="formData.addressLine1"
            maxlength="200"
            placeholder="填写街道地址"
            clearable
          />
        </el-form-item>

        <el-form-item label="城市" prop="city">
          <el-input
            v-model="formData.city"
            maxlength="75"
            placeholder="填写城市"
            clearable
          />
        </el-form-item>

        <el-form-item label="省/直辖市" prop="state">
          <el-select v-model="formData.state" placeholder="请选择省份" style="width: 100%;">
            <el-option
              v-for="item in provinces"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="邮编" prop="zipCode">
          <el-input
            v-model="formData.zipCode"
            maxlength="6"
            placeholder="6位数字邮编"
            clearable
          />
        </el-form-item>

        <h3 class="form-section-title mt-6">头像上传</h3>
        <el-form-item label="头像" prop="profilePhotoUrl">
          <div class="avatar-upload-wrapper">
            <el-upload
              class="avatar-uploader"
              action="/api/upload"
              :show-file-list="false"
              :on-success="handleUploadSuccess"
              :before-upload="beforeUpload"
              :on-error="handleUploadError"
              v-loading="uploading"
              :headers="{ Authorization: `Bearer ${userStore.token}` }"
            >
              <template #trigger>
                <img
                  v-if="formData.profilePhotoUrl"
                  :src="formData.profilePhotoUrl"
                  class="avatar"
                  alt="Avatar"
                />
                <el-icon v-else class="avatar-uploader-icon">
                  <Plus />
                </el-icon>
              </template>
            </el-upload>
            <div class="upload-tip">
              只能上传 jpg/png 文件，且不超过 2MB
            </div>
          </div>
        </el-form-item>

        <h3 class="form-section-title mt-6">联系方式</h3>
        <el-form-item label="邮箱">
          <el-input v-model="formData.email" disabled />
        </el-form-item>

        <h3 class="form-section-title mt-6">生日验证</h3>
        <el-form-item label="生日" prop="birthday">
          <el-date-picker
            v-model="formData.birthday"
            type="date"
            placeholder="选择生日"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%;"
          />
        </el-form-item>

        <el-form-item class="form-actions">
          <el-button @click="handleCancel">取消</el-button>
          <el-button type="primary" native-type="submit" :loading="submitLoading">
            保存更改
          </el-button>
        </el-form-item>
      </div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import {
  ElMessage,
  ElForm,
  FormInstance,
  UploadProps,
  UploadRawFile,
  ElButton,
  ElUpload,
  ElIcon,
} from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { useUserStore } from '@/stores/user';
import { updateUserProfile } from '@/api/user';
import type { UserUpdateProfileData } from '@/api/user';

const router = useRouter();
const userStore = useUserStore();

const formRef = ref<FormInstance>();
const pageLoading = ref(true);
const loadError = ref(false);
const submitLoading = ref(false);
const uploading = ref(false);
const alert = reactive<{ message: string; type: 'success' | 'error' }>({ message: '', type: 'success' });

const formData = reactive({
  name: '',
  phone: '',
  addressLine1: '',
  city: '',
  state: '',
  zipCode: '',
  profilePhotoUrl: '',
  email: '',
  birthday: undefined as string | undefined,
});

const rules = reactive({
  name: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  phone: [],
  addressLine1: [{ required: false, message: '请输入地址', trigger: 'blur' }],
  city: [{ required: false, message: '请输入城市', trigger: 'blur' }],
  state: [{ required: false, message: '请选择省份', trigger: 'change' }],
  zipCode: [
    { required: false, message: '请输入邮编', trigger: 'blur' },
    { pattern: /^[0-9]{6}$/, message: '邮编通常为6位数字', trigger: 'blur' },
  ],
  birthday: [{ required: false, message: '请选择生日', trigger: 'change' }],
});

const provinces = [
  '北京市','天津市','上海市','重庆市','河北省','山西省','辽宁省',
  '吉林省','黑龙江省','江苏省','浙江省','安徽省','福建省',
  '江西省','山东省','河南省','湖北省','湖南省','广东省',
  '海南省','四川省','贵州省','云南省','陕西省','甘肃省',
  '青海省','台湾省','内蒙古自治区','广西壮族自治区','西藏自治区',
  '宁夏回族自治区','新疆维吾尔自治区','香港特别行政区','澳门特别行政区',
].map(name => ({ label: name, value: name }));

onMounted(async () => {
  pageLoading.value = true;
  try {
    if (!userStore.userInfo) await userStore.getUserInfoAction();
    if (userStore.userInfo) {
      Object.assign(formData, {
        name: userStore.userInfo.name || '',
        phone: userStore.userInfo.phone || '',
        addressLine1: userStore.userInfo.addressLine1 || '',
        city: userStore.userInfo.city || '',
        state: userStore.userInfo.state || '',
        zipCode: userStore.userInfo.zipCode || '',
        profilePhotoUrl: userStore.userInfo.profilePhotoUrl || '',
        email: userStore.userInfo.email,
        birthday: userStore.userInfo.birthday ? userStore.userInfo.birthday.toString() : undefined,
      });
    } else {
      throw new Error('获取用户信息失败');
    }
  } catch (e) {
    console.error(e);
    alert.message = '获取用户信息失败';
    alert.type = 'error';
    loadError.value = true;
  } finally {
    pageLoading.value = false;
  }
});

const beforeUpload: UploadProps['beforeUpload'] = (file: UploadRawFile) => {
  uploading.value = true;
  const isImage = file.type.startsWith('image/');
  if (!isImage) {
    ElMessage.error('只能上传图片文件 (JPG/PNG/GIF)');
    uploading.value = false;
    return false;
  }
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isLt2M) {
    ElMessage.error('上传图片大小不能超过 2MB');
    uploading.value = false;
    return false;
  }
  return true;
};

const handleUploadSuccess: UploadProps['onSuccess'] = (res) => {
  let url = '';
  if (res.data?.url) url = res.data.url;
  else if (res.url) url = res.url;
  if (url) {
    formData.profilePhotoUrl = url;
    ElMessage.success('头像上传成功');
  } else {
    ElMessage.error('头像上传失败：服务器返回错误');
    console.error('Upload response:', res);
  }
  uploading.value = false;
};

const handleUploadError: UploadProps['onError'] = (err) => {
  console.error('Upload error:', err);
  ElMessage.error(`头像上传失败：${err.message || '未知错误'}`);
  uploading.value = false;
};

const submitForm = async () => {
  if (!formRef.value) return;
  submitLoading.value = true;
  try {
    const valid = await formRef.value.validate();
    if (!valid) {
      ElMessage.error('请检查表单填写是否正确');
      return;
    }
    const payload: UserUpdateProfileData = {
      name: formData.name,
      phone: formData.phone,
      addressLine1: formData.addressLine1,
      city: formData.city,
      state: formData.state,
      zipCode: formData.zipCode,
      birthday: formData.birthday,
      profilePhotoUrl: formData.profilePhotoUrl,
    };
    const resp = await updateUserProfile(payload);
    if (resp.code === 200) {
      ElMessage.success('个人资料已更新');
      await userStore.getUserInfoAction();
      router.back();
    } else {
      ElMessage.error(resp.message || '更新失败');
    }
  } catch (e: any) {
    console.error(e);
    ElMessage.error(e.message || '提交出错');
  } finally {
    submitLoading.value = false;
  }
};

const handleCancel = () => router.back();
</script>

<style scoped lang="scss">
.edit-profile-page {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}
.page-header {
  margin-bottom: 20px;
  text-align: center;
}
.header-title {
  font-size: 28px;
  font-weight: 600;
  color: #1f2124;
}
.header-subtitle {
  font-size: 16px;
  color: #606266;
}
.account-info-form {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #ebeef5;
}
.form-section-title {
  font-size: 18px;
  font-weight: 600;
  margin-top: 24px;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
  color: #303133;
}
.form-section-title:first-of-type {
  margin-top: 0;
}

.avatar-upload-wrapper {
  display: flex;
  align-items: center;
}

.avatar-uploader {
  width: 100px;
  height: 100px;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  display: flex !important;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #909399;
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-tip {
  margin-left: 12px;
  color: #909399;
  font-size: 12px;
  line-height: 1.4;
}

.mb-4 {
  margin-bottom: 16px;
}
.mt-6 {
  margin-top: 24px;
}
.form-actions {
  margin-top: 30px;
  text-align: right;
}
</style>