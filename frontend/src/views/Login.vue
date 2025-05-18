<!-- <template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h2>登录 - 宠乐居</h2>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" @submit.prevent="handleSubmit">
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" prefix-icon="Message" clearable />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password prefix-icon="Lock"
            @keyup.enter="handleSubmit" />
        </el-form-item>
        <el-form-item label="登录身份">
          <el-radio-group v-model="role">
            <el-radio-button label="user">普通用户</el-radio-button>
            <el-radio-button label="provider">服务商</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading" style="width: 100%">
            登 录
          </el-button>
        </el-form-item>
        <div class="form-footer">
          <router-link to="/register">还没有账号？立即注册</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage, ElCard, ElForm, ElFormItem, ElInput, ElButton, ElIcon } from 'element-plus';
import { Message, Lock } from '@element-plus/icons-vue';
import type { LoginData } from '@/api/user';
import { useUserStore } from '@/stores/user';
import axios from 'axios'; 

const router = useRouter();
const userStore = useUserStore();
const formRef = ref<FormInstance>();
const loading = ref(false);
const role = ref<'user' | 'provider'>('user');

const form = reactive<LoginData>({
  email: '',
  password: ''
});

const rules = reactive<FormRules>({
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: ['blur', 'change'] } // 可以在 change 时也触发校验
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    // 密码长度校验可以根据实际需求调整或移除
    // { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ]
});


const handleSubmit = () => {
  if (!formRef.value) return;

  formRef.value.validate((valid) => {
    if (valid) {
      login();
    } else {
      ElMessage.warning('请检查输入项');
    }
  });
};

const login = async () => {
  loading.value = true
  try {
    const success = await userStore.loginAction(form)

    if (success) {
      const role = userStore.userInfo?.role

      if (role === 'user') {
        router.push('/')
      } else if (role === 'provider') {
        router.push('/provider/services')
      } else if (role === 'admin') {
        router.push('/admin/users')
      } else {
        router.push('/')
      }
    } else {
      console.warn('loginAction returned false.')
    }
  } catch (error) {
    console.error('Login component caught error:', error)
  } finally {
    loading.value = false
  }
}


</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
  /* 或者添加背景图 */
}

.login-card {
  width: 400px;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.login-card h2 {
  text-align: center;
  margin-bottom: 20px;
  color: #303133;
}

.el-button {
  margin-top: 10px;
}

.form-footer {
  margin-top: 15px;
  text-align: center;
  font-size: 14px;
}

.form-footer a {
  color: #409eff;
  text-decoration: none;
}

.form-footer a:hover {
  text-decoration: underline;
}

:deep(.el-input__prefix) {
  display: inline-flex;
  align-items: center;
  margin-right: 5px;
}
</style> -->

<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h2>登录 - 宠乐居</h2>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" @submit.prevent="handleSubmit">
        <el-form-item label="账号" prop="identifier">
          <el-input
            v-model="form.identifier"
            placeholder="请输入用户名/邮箱/手机号"
            :prefix-icon="UserIcon"  
            clearable />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
            :prefix-icon="LockIcon"
            @keyup.enter="handleSubmit" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading" style="width: 100%">
            登 录
          </el-button>
        </el-form-item>
        <div class="form-footer">
          <router-link to="/register">还没有账号？立即注册</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import type { FormInstance, FormRules } from 'element-plus';
// 确保导入需要的组件和图标
import { ElMessage, ElCard, ElForm, ElFormItem, ElInput, ElButton } from 'element-plus';
// import { Message, Lock } from '@element-plus/icons-vue'; // 旧图标
import { User as UserIcon, Lock as LockIcon } from '@element-plus/icons-vue'; // 导入新图标
import type { LoginData } from '@/api/user'; // LoginData 应该已经在 api/user.ts 中修改为 { identifier, password }
import { useUserStore } from '@/stores/user';
// import axios from 'axios'; // 这个导入似乎没有使用，可以移除

const router = useRouter();
const userStore = useUserStore();
const formRef = ref<FormInstance>();
const loading = ref(false);
// const role = ref<'user' | 'provider'>('user'); // 不再需要前端选择角色

// 修改点 3: 更新 form 结构
const form = reactive<LoginData>({
  // email: '', // 移除 email
  identifier: '', // 添加 identifier
  password: ''
});

// 修改点 4: 更新 rules
const rules = reactive<FormRules>({
  identifier: [ // 修改 email 为 identifier
    { required: true, message: '请输入用户名、邮箱或手机号', trigger: 'blur' },
    // 不再需要 email 格式校验
    // { type: 'email', message: '请输入正确的邮箱格式', trigger: ['blur', 'change'] }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    // { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ]
});


const handleSubmit = () => {
  if (!formRef.value) return;

  formRef.value.validate((valid) => {
    if (valid) {
      login();
    } else {
      ElMessage.warning('请检查输入项');
    }
  });
};

const login = async () => {
  loading.value = true;
  try {
    // loginAction 现在接收 { identifier, password }
    const success = await userStore.loginAction(form);

    if (success) {
      // 登录成功后的跳转逻辑保持不变，它依赖于 userStore.userInfo.role
      const role = userStore.userInfo?.role;

      if (role === 'user') {
        router.push('/');
      } else if (role === 'provider') {
        router.push('/provider/services'); 
      } else if (role === 'admin') {
        router.push('/admin'); 
      } else {
        router.push('/'); 
      }
      ElMessage.success('登录成功'); 
    } else {
      // loginAction 返回 false 通常意味着后端返回了非 200 或 token 获取失败
      // ElMessage.error 应该已经在 request.ts 或 loginAction 内部处理了
      console.warn('loginAction returned false.');
      // 如果 loginAction 内部没有提示，可以在这里加一个通用提示
      // ElMessage.error('登录失败，请检查您的账号和密码');
    }
  } catch (error) {
    // 一般性的错误（如网络问题）会在 request.ts 中处理并提示
    console.error('Login component caught error:', error);
    // 可以在这里添加一个备用提示
    // ElMessage.error('登录过程中发生错误，请稍后重试');
  } finally {
    loading.value = false;
  }
}


</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
}

.login-card {
  width: 400px;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.login-card h2 {
  text-align: center;
  margin-bottom: 20px;
  color: #303133;
}

.el-button {
  margin-top: 10px;
}

.form-footer {
  margin-top: 15px;
  text-align: center;
  font-size: 14px;
}

.form-footer a {
  color: #409eff;
  text-decoration: none;
}

.form-footer a:hover {
  text-decoration: underline;
}

/* 这个 deep selector 可能不再需要，取决于 Element Plus 版本和具体样式 */
/*
:deep(.el-input__prefix) {
  display: inline-flex;
  align-items: center;
  margin-right: 5px;
}
*/
</style>