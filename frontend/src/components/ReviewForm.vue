<template>
  <el-dialog
    v-model="dialogVisible"
    title="服务评价"
    width="500px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="80px"
      @submit.prevent="handleSubmit"
    >
      <el-form-item label="评分" prop="rating">
        <el-rate
          v-model="form.rating"
          :max="5"
          :texts="['很差', '较差', '一般', '较好', '很好']"
          show-text
        />
      </el-form-item>
      
      <el-form-item label="评价内容" prop="comment">
        <el-input
          v-model="form.comment"
          type="textarea"
          :rows="4"
          placeholder="请输入您的评价内容（选填）"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">
          提交评价
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { submitReview } from '@/api/review'
import type { ReviewSubmitData } from '@/api/review'

const props = defineProps<{
  reservationId: number
  serviceItemId: number
}>()

const emit = defineEmits<{
  (e: 'success'): void
  (e: 'cancel'): void
}>()

const dialogVisible = ref(true)
const loading = ref(false)
const formRef = ref<FormInstance>()

const form = reactive<ReviewSubmitData>({
  reservationId: props.reservationId,
  rating: 5,
  comment: ''
})

const rules = reactive<FormRules>({
  rating: [
    { required: true, message: '请选择评分', trigger: 'change' }
  ],
  comment: [
    { max: 500, message: '评价内容不能超过500字', trigger: 'blur' }
  ]
})

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    loading.value = true
    
    const res = await submitReview(form)
    if (res.code === 200) {
      ElMessage.success('评价提交成功')
      emit('success')
      dialogVisible.value = false
    } else {
      ElMessage.error(res.message || '评价提交失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('提交评价失败:', error)
      ElMessage.error(error.message || '提交评价失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

const handleCancel = () => {
  emit('cancel')
  dialogVisible.value = false
}
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style> 