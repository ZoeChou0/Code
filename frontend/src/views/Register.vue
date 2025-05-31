<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <h2>注册</h2>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="用户名" prop="name">
          <el-input v-model="form.name" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱">
            <template #append>
              <el-button :disabled="!form.email || isEmailSending || countdown > 0" @click="handleSendCode"
                :loading="isEmailSending">
                {{ countdown > 0 ? `${countdown}秒后重试` : '发送验证码' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="验证码" prop="verificationCode">
          <el-input v-model="form.verificationCode" placeholder="请输入邮箱验证码" maxlength="6" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" show-password />
        </el-form-item>
        <el-form-item label="注册身份" prop="role">
          <el-radio-group v-model="form.role">
            <el-radio label="user">普通用户</el-radio>
            <el-radio label="provider">服务商</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading" style="width: 100%">
            注册
          </el-button>
        </el-form-item>
        <div class="form-footer">
          <router-link to="/login">已有账号？立即登录</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { register, sendEmailCode } from '@/api/user'
import type { RegisterData } from '@/api/user'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const isEmailSending = ref(false)
const countdown = ref(0)

const form = reactive({
  name: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: '',
  verificationCode: '',
  role: 'user' as 'user' | 'provider'
})

const startCountdown = () => {
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

const handleSendCode = async () => {
  try {
    isEmailSending.value = true
    await sendEmailCode(form.email)
    ElMessage.success('验证码已发送到您的邮箱')
    startCountdown()
  } catch (error: any) {
    ElMessage.error(error.response?.data || '验证码发送失败')
  } finally {
    isEmailSending.value = false
  }
}

const validatePass = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else {
    if (form.confirmPassword !== '') {
      formRef.value?.validateField('confirmPassword')
    }
    callback()
  }
}

const validatePass2 = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const rules = reactive<FormRules>({
  name: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  verificationCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码长度必须为6位', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  password: [
    { required: true, validator: validatePass, trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validatePass2, trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择注册身份', trigger: 'change' }
  ]
})

const handleSubmit = async () => {
  if (!formRef.value) return;

  await formRef.value.validate(async (valid, fields) => {
    if (valid) {
      loading.value = true;
      try {
        const { confirmPassword, ...registerDataToSend } = form;
        await register(registerDataToSend as RegisterData);
        ElMessage.success('注册成功');
        router.push('/login');
      } catch (error: any) {
        const errorMessage = error?.response?.data?.message || error?.message || '注册失败，请检查您的输入或稍后再试';
        ElMessage.error(errorMessage);
        console.error('注册失败详情:', error.response?.data || error);
      } finally {
        loading.value = false;
      }
    } else {
      console.log('表单校验失败!', fields);
      if (fields) {
        // 获取第一个校验失败的字段的错误信息
        const firstErrorField = Object.keys(fields)[0]; // 获取第一个错误的字段名
        if (firstErrorField && fields[firstErrorField] && fields[firstErrorField].length > 0) {
          const specificErrorMessage = fields[firstErrorField][0].message; // 获取该字段的第一条错误信息
          ElMessage.error(specificErrorMessage || '请检查表单输入项是否正确');
        } else {
          ElMessage.error('请检查表单输入项是否正确'); // 备用提示
        }
      } else {
        ElMessage.error('请检查表单输入项是否正确'); // 备用提示
      }
    }
  });
};
</script>

<style scoped lang="scss">
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-color: #f5f7fa;
  padding: 20px;
  box-sizing: border-box;

  .register-card {
    width: 500px;
    margin: 0;

    :deep(.el-card__header) {
      text-align: center;
      padding: 20px;

      h2 {
        margin: 0;
        color: #303133;
      }
    }

    :deep(.el-input-group__append) {
      padding: 0;

      .el-button {
        margin: 0;
        border: none;
        height: 100%;
        padding: 0 15px;
      }
    }

    .form-footer {
      text-align: center;
      margin-top: 20px;

      a {
        color: #409EFF;
        text-decoration: none;

        &:hover {
          color: #66b1ff;
        }
      }
    }
  }
}
</style>