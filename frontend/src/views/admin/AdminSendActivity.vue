<script setup lang="ts">
import { ref, reactive } from 'vue';
import { ElMessage, type FormInstance } from 'element-plus';
import request from '@/utils/request';

// ... (interfaces ActivityDetails, ActivityForm) ...
interface ActivityDetails {
  link?: string;
  activityId?: string;
}

interface ActivityForm {
  title: string;
  content: string;
  level: 'info' | 'success' | 'warning' | 'error';
  details: ActivityDetails;
}

const activityFormRef = ref<FormInstance>();
const activityForm = reactive<ActivityForm>({
  title: '',
  content: '',
  level: 'warning',
  details: {
    link: '',
    activityId: '',
  },
});

const loading = ref(false);

const submitActivityNotification = async () => {
  if (!activityFormRef.value) return;
  // validate 方法本身返回一个 Promise<boolean>，可以直接 await 它
  // 或者使用其回调形式
  try {
    // 使用 Element Plus 的 validate 方法，它接受一个回调
    await activityFormRef.value.validate(async (valid, fields) => {
      if (valid) {
        loading.value = true;
        try {
          await request({
            url: '/api/admin/notify/activity', // 请确认此URL是否正确
            method: 'post',
            data: activityForm,
          });
          ElMessage.success('活动通知发送成功！');
          activityFormRef.value?.resetFields();
          // reactive 对象内的嵌套对象可能需要手动重置
          activityForm.details = { link: '', activityId: '' };
        } catch (error: any) {
          console.error('发送活动通知失败:', error);
          ElMessage.error(error?.response?.data?.message || error.message || '发送失败，请重试');
        } finally {
          loading.value = false;
        }
      } else {
        console.log('表单验证失败:', fields);
        ElMessage.error('表单信息填写有误，请检查！');
        // **不应该在这里 return false;**
      }
    });
  } catch (validationPromiseRejected) {
    // 如果 validate 的 Promise 本身被 reject (例如，如果配置了 scrollToError 且发生错误)
    // 这种情况比较少见，除非有特定的表单配置
    console.log('表单验证 Promise 被拒绝:', validationPromiseRejected);
    ElMessage.error('表单验证过程中出现问题，请稍后重试。');
  }
};
</script>
