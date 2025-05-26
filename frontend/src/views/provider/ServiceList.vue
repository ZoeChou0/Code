<template>
  <div class="service-list-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>我的服务列表</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">添加新服务</el-button>
        </div>
      </template>

      <el-table :data="services" v-loading="loading" style="width: 100%">
        <el-table-column prop="name" label="服务名称" width="180" />
        <el-table-column prop="description" label="服务描述" show-overflow-tooltip />
        <el-table-column prop="price" label="价格 (元)" width="100" align="right">
          <template #default="scope">
            ￥{{ scope.row.price?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="时长 (分钟)" width="110" align="center" />
        <el-table-column prop="reviewStatus" label="审核状态" width="120" align="center">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.reviewStatus)">
              {{ formatReviewStatus(scope.row.reviewStatus) }}
            </el-tag>
            <el-tooltip v-if="scope.row.reviewStatus === 'REJECTED' && scope.row.rejectionReason"
              :content="scope.row.rejectionReason" placement="top">
              <el-icon style="margin-left: 5px; cursor: help;">
                <WarningFilled />
              </el-icon>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="dailyCapacity" label="每日容量" width="100" align="center" />
        <el-table-column prop="requiredVaccinations" label="疫苗要求" width="150" show-overflow-tooltip />
        <el-table-column prop="requiresNeutered" label="要求绝育" width="100" align="center">
          <template #default="scope">
            {{ scope.row.requiresNeutered ? '是' : '否' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="scope">
            <el-button size="small" :icon="Edit" @click="handleEdit(scope.row)"
              :disabled="scope.row.reviewStatus === 'APPROVED'">编辑</el-button>
            <el-button size="small" type="danger" :icon="Delete" @click="handleDelete(scope.row.id)"
              :disabled="scope.row.reviewStatus === 'APPROVED'">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :title="isEditing ? '编辑服务' : '添加新服务'" v-model="dialogVisible" width="60%" :close-on-click-modal="false"
      @close="resetForm">
      <el-form :model="serviceForm" :rules="rules" ref="serviceFormRef" label-width="120px">
        <el-form-item label="服务名称" prop="name">
          <el-input v-model="serviceForm.name" placeholder="请输入服务名称"></el-input>
        </el-form-item>
        <el-form-item label="服务描述" prop="description">
          <el-input type="textarea" v-model="serviceForm.description" placeholder="请输入服务描述"></el-input>
        </el-form-item>
        <el-form-item label="价格 (元)" prop="price">
          <el-input-number v-model="serviceForm.price" :precision="2" :step="1" :min="0"
            controls-position="right"></el-input-number>
        </el-form-item>
        <el-form-item label="时长 (分钟)" prop="duration">
          <el-input-number v-model="serviceForm.duration" :min="1" :step="15"
            controls-position="right"></el-input-number>
        </el-form-item>
        <el-form-item label="每日容量" prop="dailyCapacity">
          <el-input-number v-model="serviceForm.dailyCapacity" :min="1" :step="1"
            controls-position="right"></el-input-number>
        </el-form-item>
        <el-form-item label="要求疫苗" prop="requiredVaccinations">
          <el-input v-model="serviceForm.requiredVaccinations" placeholder=""></el-input>
        </el-form-item>
        <el-form-item label="要求绝育" prop="requiresNeutered">
          <el-switch v-model="serviceForm.requiresNeutered"></el-switch>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最低年龄 (月)" prop="minAge">
              <el-input-number v-model="serviceForm.minAge" :min="0" controls-position="right"
                placeholder="可选"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最高年龄 (月)" prop="maxAge">
              <el-input-number v-model="serviceForm.maxAge" :min="serviceForm.minAge || 0" controls-position="right"
                placeholder="可选"></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="性格要求" prop="temperamentRequirements">
          <el-input v-model="serviceForm.temperamentRequirements" placeholder="如: 仅限友好, 不接受攻击性 (可选)"></el-input>
        </el-form-item>
        <el-form-item label="禁止品种" prop="prohibitedBreeds">
          <el-input v-model="serviceForm.prohibitedBreeds" placeholder="多种品种请用英文逗号分隔 (可选)"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
            {{ isEditing ? '保存更新' : '确认添加' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue';
import { ElTable, ElTableColumn, ElButton, ElCard, ElIcon, ElDialog, ElForm, ElFormItem, ElInput, ElInputNumber, ElMessage, ElMessageBox, ElTag, ElTooltip, ElSwitch, ElRow, ElCol } from 'element-plus';
import { Plus, Edit, Delete, WarningFilled } from '@element-plus/icons-vue';
import { getProviderServices, addService, updateService, deleteService } from '@/api/service';
import type { Service } from '@/api/service'; // Import the Service type
import type { FormInstance, FormRules } from 'element-plus';

// Define the type for the form, excluding fields not managed by provider
type ServiceFormData = Omit<Service, 'id' | 'reviewStatus' | 'rejectionReason' | 'providerId'>;

const services = ref<Service[]>([]); // Holds the list of services
const loading = ref(false); // Loading state for the table
const dialogVisible = ref(false); // Controls the add/edit dialog
const isEditing = ref(false); // Flag to indicate if editing or adding
const submitLoading = ref(false); // Loading state for dialog submission
const serviceFormRef = ref<FormInstance>(); // Reference to the form instance

// Reactive form data
const serviceForm = reactive<Partial<ServiceFormData>>({ // Use Partial for initial empty state
  name: '',
  description: '',
  price: 0.00,
  duration: 60,
  dailyCapacity: 1,
  requiredVaccinations: '',
  requiresNeutered: false,
  minAge: undefined,
  maxAge: undefined,
  temperamentRequirements: '',
  prohibitedBreeds: '',
});

// Keep track of the ID being edited
const editingServiceId = ref<number | null>(null);

// Form validation rules
const rules = reactive<FormRules<ServiceFormData>>({
  name: [{ required: true, message: '请输入服务名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入服务描述', trigger: 'blur' }],
  price: [{ required: true, type: 'number', message: '请输入有效的价格', trigger: 'blur' }],
  duration: [{ required: true, type: 'number', message: '请输入有效的时长', trigger: 'blur' }],
  dailyCapacity: [{ required: true, type: 'number', message: '请输入每日容量', trigger: 'blur' }],
});

// Fetch services from the backend
const fetchServices = async () => {
  loading.value = true;
  try {
    const res = await getProviderServices();
    // Assuming the API returns { code: 200, data: [...], message: '...' }
    if (res.code === 200) {
      services.value = res.data || [];
    } else {
      ElMessage.error(res.message || '获取服务列表失败');
      services.value = []; // Clear list on error
    }
  } catch (error: any) {
    console.error("Error fetching services:", error);
    ElMessage.error('获取服务列表失败: ' + (error?.message || '请检查网络连接'));
    services.value = []; // Clear list on error
  } finally {
    loading.value = false;
  }
};

// --- Dialog and Form Handling ---

// Reset form fields
const resetForm = () => {
  serviceFormRef.value?.resetFields(); // Reset validation and fields
  // Manually reset non-string fields if needed, as resetFields might not cover all cases perfectly
  Object.assign(serviceForm, {
    name: '',
    description: '',
    price: 0.00,
    duration: 60,
    dailyCapacity: 1,
    requiredVaccinations: '',
    requiresNeutered: false,
    minAge: undefined,
    maxAge: undefined,
    temperamentRequirements: '',
    prohibitedBreeds: '',
  });
  editingServiceId.value = null; // Clear editing ID
};

// Handle opening the dialog for adding a new service
const handleAdd = () => {
  isEditing.value = false;
  resetForm(); // Ensure form is clean before opening
  dialogVisible.value = true;
};

// Handle opening the dialog for editing an existing service
const handleEdit = (row: Service) => {
  // Check if the service is approved, prevent editing if so
  if (row.reviewStatus === 'APPROVED') {
    ElMessage.warning('已批准的服务无法编辑。如需修改，请联系管理员下架后重新提交。');
    return;
  }
  isEditing.value = true;
  resetForm(); // Reset first to clear any previous state
  // Populate form with existing data
  Object.assign(serviceForm, {
    name: row.name,
    description: row.description,
    price: row.price,
    duration: row.duration,
    dailyCapacity: row.dailyCapacity,
    requiredVaccinations: row.requiredVaccinations,
    requiresNeutered: row.requiresNeutered,
    minAge: row.minAge,
    maxAge: row.maxAge,
    temperamentRequirements: row.temperamentRequirements,
    prohibitedBreeds: row.prohibitedBreeds,
  });
  editingServiceId.value = row.id; // Store the ID of the service being edited
  dialogVisible.value = true;
};

// Handle form submission (Add or Update)
const handleSubmit = async () => {
  if (!serviceFormRef.value) return;
  await serviceFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true;
      try {
        let res;
        const payload = { ...serviceForm } as ServiceFormData; // Ensure payload matches required type

        if (isEditing.value && editingServiceId.value !== null) {
          // Update existing service
          const updatePayload: Service = {
            ...payload,
            id: editingServiceId.value,
            // These fields are set by backend or not editable here
            reviewStatus: 'PENDING', // Or fetch existing status if needed, but usually editing resets status
            providerId: 0, // Backend should use authenticated provider ID
          };
          res = await updateService(updatePayload);
        } else {
          // Add new service
          res = await addService(payload);
        }

        if (res.code === 200) {
          ElMessage.success(isEditing.value ? '服务更新成功，将重新提交审核' : '服务添加成功，等待审核');
          dialogVisible.value = false;
          fetchServices(); // Refresh the list
        } else {
          ElMessage.error(res.message || (isEditing.value ? '更新失败' : '添加失败'));
        }
      } catch (error: any) {
        console.error("Error submitting service:", error);
        ElMessage.error('操作失败: ' + (error?.message || '请稍后重试'));
      } finally {
        submitLoading.value = false;
      }
    } else {
      console.log('Form validation failed');
      return false;
    }
  });
};

// --- Deletion Handling ---

// Handle deleting a service
const handleDelete = (id: number) => {
  // Find the service to check its status
  const serviceToDelete = services.value.find(s => s.id === id);
  if (serviceToDelete?.reviewStatus === 'APPROVED') {
    ElMessage.warning('已批准的服务无法直接删除。如需删除，请联系管理员下架。');
    return;
  }

  ElMessageBox.confirm(
    '确定要删除这项服务吗？此操作无法撤销。',
    '警告',
    {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      loading.value = true; // Show loading on table while deleting
      try {
        const res = await deleteService(id);
        if (res.code === 200) {
          ElMessage.success('服务删除成功');
          fetchServices(); // Refresh the list
        } else {
          ElMessage.error(res.message || '删除失败');
        }
      } catch (error: any) {
        console.error("Error deleting service:", error);
        ElMessage.error('删除失败: ' + (error?.message || '请稍后重试'));
      } finally {
        loading.value = false;
      }
    })
    .catch(() => {
      // User clicked cancel
      ElMessage.info('已取消删除');
    });
};

// --- Utility Functions ---

// Format review status for display
const formatReviewStatus = (status: Service['reviewStatus']): string => {
  switch (status) {
    case 'PENDING': return '待审核';
    case 'APPROVED': return '已批准';
    case 'REJECTED': return '已拒绝';
    default: return '未知';
  }
};

// Get tag type for status display
const getStatusTagType = (status: Service['reviewStatus']): ('warning' | 'success' | 'danger' | 'info') => {
  switch (status) {
    case 'PENDING': return 'warning';
    case 'APPROVED': return 'success';
    case 'REJECTED': return 'danger';
    default: return 'info';
  }
};


// Fetch services when the component is mounted
onMounted(() => {
  fetchServices();
});
</script>

<style scoped>
.service-list-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.el-table {
  margin-top: 15px;
}

.el-button+.el-button {
  margin-left: 8px;
}

.dialog-footer {
  text-align: right;
}
</style>
