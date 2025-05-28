<template>
  <el-card class="notification-sender-card">
    <template #header>
      <div class="card-header">
        <span>发送管理员通知</span>
      </div>
    </template>

    <el-form :model="notificationForm" ref="notificationFormRef" label-width="120px" @submit.prevent="submitNotification">
      <el-form-item
        label="通知类型"
        prop="type"
        :rules="[{ required: true, message: '请输入或选择通知类型', trigger: 'blur' }]"
      >
        <el-select
          v-model="notificationForm.type"
          placeholder="选择或输入通知类型"
          filterable
          allow-create
          default-first-option
          style="width: 100%;"
        >
          <el-option label="活动通知 (activity_announcement)" value="activity_announcement" />
          <el-option label="系统广播 (system_broadcast)" value="system_broadcast" />
          <el-option label="紧急警报 (urgent_alert)" value="urgent_alert" />
          </el-select>
        <div class="form-item-help">用于前端区分不同消息并进行相应处理。</div>
      </el-form-item>

      <el-form-item label="通知标题" prop="title">
        <el-input v-model="notificationForm.title" placeholder="例如：🎉 新活动上线！" />
      </el-form-item>

      <el-form-item
        label="通知内容"
        prop="content"
        :rules="[{ required: true, message: '通知内容不能为空', trigger: 'blur' }]"
      >
        <el-input
          type="textarea"
          v-model="notificationForm.content"
          rows="5"
          placeholder="请输入详细的通知信息..."
          show-word-limit
          maxlength="500"
        />
      </el-form-item>

      <el-form-item label="消息级别" prop="level">
        <el-select v-model="notificationForm.level" placeholder="选择消息级别" style="width: 100%;">
          <el-option label="普通 (Info)" value="info" />
          <el-option label="成功 (Success)" value="success" />
          <el-option label="警告 (Warning)" value="warning" />
          <el-option label="错误 (Error)" value="error" />
        </el-select>
        <div class="form-item-help">影响通知在前端的显示样式。</div>
      </el-form-item>

      <el-divider content-position="left">附加数据 (可选)</el-divider>
      <div v-for="(item, index) in notificationForm.dataItems" :key="index" class="data-item-row">
        <el-form-item
          :label="`数据项 ${index + 1}`"
          :inline="true"
          style="margin-bottom: 10px;"
        >
          <el-input v-model="item.key" placeholder="键 (Key)" style="width: 40%; margin-right: 10px;" />
          <el-input v-model="item.value" placeholder="值 (Value)" style="width: 40%; margin-right: 10px;" />
          <el-button type="danger" :icon="Delete" circle @click="removeDataItem(index)" v-if="notificationForm.dataItems.length > 0" />
        </el-form-item>
      </div>
      <el-form-item>
        <el-button type="success" :icon="Plus" plain @click="addDataItem">添加数据项</el-button>
      </el-form-item>


      <el-form-item style="margin-top: 20px;">
        <el-button type="primary" @click="submitNotification" :loading="loading" native-type="submit">
          发送通知
        </el-button>
        <el-button @click="resetForm(notificationFormRef)">重置表单</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { ElMessage, ElNotification, type FormInstance } from 'element-plus';
import { Delete, Plus } from '@element-plus/icons-vue'; // 导入图标
import request from '@/utils/request'; // 您的 Axios 实例

// 后端 AdminNotificationRequestDTO 对应的接口
interface AdminNotificationPayload {
  type: string;
  title?: string;
  content: string;
  level?: 'info' | 'success' | 'warning' | 'error';
  data?: Record<string, any>;
}

// 表单内部使用的数据项结构
interface DataItem {
  key: string;
  value: string;
}

const notificationFormRef = ref<FormInstance>();
const initialFormState = {
  type: 'system_broadcast',
  title: '',
  content: '',
  level: 'info' as 'info' | 'success' | 'warning' | 'error', // 明确类型并给默认值
  dataItems: [] as DataItem[], // 用于动态添加键值对
};

const notificationForm = reactive({ ...initialFormState });
const loading = ref(false);

const addDataItem = () => {
  notificationForm.dataItems.push({ key: '', value: '' });
};

const removeDataItem = (index: number) => {
  notificationForm.dataItems.splice(index, 1);
};

const prepareDataForSubmission = (): Record<string, any> | undefined => {
  if (notificationForm.dataItems.length === 0) {
    return undefined;
  }
  const dataPayload: Record<string, any> = {};
  notificationForm.dataItems.forEach(item => {
    if (item.key.trim() !== '') {
      // 尝试将值转换为数字或布尔值（如果适用），否则保持为字符串
      let value: any = item.value.trim();
      if (!isNaN(Number(value)) && value !== '') { // 是数字
        value = Number(value);
      } else if (value.toLowerCase() === 'true') {
        value = true;
      } else if (value.toLowerCase() === 'false') {
        value = false;
      }
      dataPayload[item.key.trim()] = value;
    }
  });
  return Object.keys(dataPayload).length > 0 ? dataPayload : undefined;
};

const submitNotification = async () => {
  if (!notificationFormRef.value) return;
  await notificationFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      const payload: AdminNotificationPayload = {
        type: notificationForm.type,
        title: notificationForm.title || undefined, // 如果为空字符串则不传或传 undefined
        content: notificationForm.content,
        level: notificationForm.level || 'info',
        data: prepareDataForSubmission(),
      };

      try {
        await request({
          url: '/admin/notify/send', 
          method: 'post',
          data: payload,
        });
        ElMessage.success('通知发送成功！');
        resetForm(notificationFormRef.value); // 发送成功后重置表单
      } catch (error: any) {
        console.error('发送通知失败:', error);
        ElNotification({
            title: '发送失败',
            message: error?.response?.data?.message || error.message || '发送通知时发生未知错误，请重试。',
            type: 'error',
            duration: 0, // 错误消息不自动关闭
        });
      } finally {
        loading.value = false;
      }
    } else {
      ElMessage.error('表单信息填写有误，请检查！');
      // 不需要返回 false，validate 的回调主要是执行操作或提示
    }
  });
};

const resetForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return;
  formEl.resetFields();
  // 手动重置 dataItems 和其他非表单直接绑定的响应式数据
  notificationForm.type = 'system_broadcast';
  notificationForm.title = '';
  notificationForm.content = '';
  notificationForm.level = 'info';
  notificationForm.dataItems = [];
};

</script>

<style scoped>
.notification-sender-card {
  max-width: 800px;
  margin: 20px auto; /* 增加上下边距 */
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 1.2em; /* 稍微调大 */
  font-weight: bold;
}
.form-item-help {
  font-size: 0.85em;
  color: #909399;
  line-height: 1.5;
  margin-top: 4px;
}
.data-item-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px; /* 确保行间距 */
}
.data-item-row .el-form-item {
  margin-bottom: 0 !important; /* 覆盖 Element Plus 默认的 el-form-item margin */
  flex-grow: 1;
}
</style>