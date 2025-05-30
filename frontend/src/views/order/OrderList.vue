<template>
  <div class="my-order-list-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span><el-icon><Tickets /></el-icon> 我的订单</span>
        </div>
      </template>

      <el-table :data="orders" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="订单ID" width="90" />
        <el-table-column prop="reservationId" label="预约ID" width="90" />
        <el-table-column label="服务名称" min-width="180">
           <template #default="{ row }">
            <span>{{ row.serviceName || 'N/A' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="宠物名称" min-width="120">
          <template #default="{ row }">
            <span>{{ row.petName || 'N/A' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额 (元)" width="110">
          <template #default="{ row }">
            ¥{{ row.amount?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="130">
          <template #default="{ row }">
            <el-tag :type="getOrderStatusType(row.status)">
              {{ formatOrderStatus(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="下单时间" width="170">
           <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="200"> <template #default="{ row }">
            <el-button
              v-if="canCancelOrder(row.status)"
              type="danger"
              size="small"
              @click="handleCancelOrder(row.id)"
              :loading="row.cancelling"
            >
              取消订单
            </el-button>
            <el-button
              v-if="canPayOrder(row.status)"
              type="primary"
              size="small"
              @click="handlePayOrder(row.id)"
            >
              去支付
            </el-button>

            <el-button
              v-if="canReviewOrder(row.status) && !row.reviewId"
              type="warning"
              size="small"
              @click="handleOpenReviewDialog(row)"
            >
              评价服务
            </el-button>
            <el-button
              v-if="row.reviewId"
              type="info"
              size="small"
              @click="handleViewReview(row)"
            >
              查看评价
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-if="totalOrders > 0"
        class="pagination-container"
        background
        layout="prev, pager, next, total, jumper"
        :total="totalOrders"
        :current-page="currentPage"
        :page-size="pageSize"
        @current-change="handlePageChange"
      />
    </el-card>

    <el-dialog v-model="reviewSubmitDialogVisible" title="评价服务" width="500px" @closed="resetReviewForm">
      <el-form ref="reviewFormRef" :model="reviewFormData" :rules="reviewFormRules" label-position="top">
        <el-form-item label="评分" prop="rating">
          <el-rate v-model="reviewFormData.rating" :colors="['#99A9BF', '#F7BA2A', '#FF9900']" allow-half :max="5"/>
        </el-form-item>
        <el-form-item label="评价内容" prop="comment">
          <el-input type="textarea" v-model="reviewFormData.comment" :rows="4" placeholder="请分享您的服务体验..." maxlength="500" show-word-limit/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewSubmitDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview" :loading="submittingReview">提交评价</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reviewViewDialogVisible" title="查看我的评价" width="500px">
      <div v-if="loadingReviewDetail" v-loading="loadingReviewDetail" style="min-height: 150px;"></div>
      <div v-else-if="currentViewedReview">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="服务名称">{{ currentReviewingOrder?.serviceName || 'N/A' }}</el-descriptions-item>
          <el-descriptions-item label="评价时间">{{ formatDateTime(currentViewedReview.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="我的评分">
            <el-rate v-model="currentViewedReview.rating" disabled show-score text-color="#ff9900" :max="5" />
          </el-descriptions-item>
          <el-descriptions-item label="评价内容">
            <div style="white-space: pre-wrap;">{{ currentViewedReview.comment }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <el-empty v-else description="未能加载评价详情或您尚未评价"></el-empty>
      <template #footer>
        <el-button type="primary" @click="reviewViewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue';
// import { useRouter } from 'vue-router'; // 如果需要编程式导航
import { 
  ElMessage, ElMessageBox, ElCard, ElTable, ElTableColumn, ElTag, ElButton, 
  ElPagination, ElIcon, ElDialog, ElForm, ElRate, ElInput,
  ElDescriptions, ElDescriptionsItem, ElEmpty,
  type FormInstance, type FormItemRule 
} from 'element-plus';
import { Tickets } from '@element-plus/icons-vue';
import { getUserOrderList, cancelOrder as apiCancelOrder /*, payOrder as apiPayOrder */ } from '@/api/order';
import { submitReview as apiSubmitReview, getReviewByReservation, type Review } from '@/api/review';
import type { OrderViewDTO } from '@/api/order'; // 确保 OrderViewDTO 定义了 reviewId?: number
import type { ReviewSubmitData } from '@/api/review';
import type { BackendResult } from '@/types/api';

// const router = useRouter();

interface DisplayOrder extends OrderViewDTO {
  cancelling?: boolean;
}

const orders = ref<DisplayOrder[]>([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(10); // 根据需要调整
const totalOrders = ref(0);

const reviewSubmitDialogVisible = ref(false);
const submittingReview = ref(false);
const reviewFormRef = ref<FormInstance>();
const currentReviewingOrder = ref<OrderViewDTO | null>(null);
const reviewFormData = reactive<ReviewSubmitData>({
  reservationId: 0,
  rating: 0,
  comment: ''
});
const reviewFormRules = reactive<Record<string, FormItemRule[]>>({
  rating: [{ type: 'number', required: true, min: 0.5, message: '请至少选择0.5颗星', trigger: 'change' }], // el-rate 允许0.5
  comment: [{ required: true, message: '请输入评价内容', trigger: 'blur' }]
});

const reviewViewDialogVisible = ref(false);
const currentViewedReview = ref<Review | null>(null);
const loadingReviewDetail = ref(false);

const fetchOrders = async (page = currentPage.value) => {
  loading.value = true;
  try {
    // 假设 getUserOrderList API 支持分页或返回全量数据，然后前端分页
    // 如果后端支持分页，应该传递 page 和 pageSize:
    // const res: BackendResult<PaginatedData<OrderViewDTO>> = await getUserOrderList({ page: page, size: pageSize.value });
    // if (res.code === 200 && res.data) {
    //   orders.value = res.data.records.map(o => ({ ...o, cancelling: false }));
    //   totalOrders.value = res.data.total;
    //   currentPage.value = res.data.current;
    // }
    // 当前假设返回全量数据
    const res: BackendResult<OrderViewDTO[]> = await getUserOrderList();
    if (res.code === 200 && res.data) {
      orders.value = res.data.map(o => ({ ...o, cancelling: false }));
      totalOrders.value = res.data.length;
      currentPage.value = page; // 如果是前端分页，切换页码不应重置为1除非是搜索
    } else {
      ElMessage.error(res.message || '获取订单列表失败');
    }
  } catch (error) {
    console.error("Fetch orders error:", error);
    ElMessage.error('请求订单列表出错');
  } finally {
    loading.value = false;
  }
};

const handlePageChange = (page: number) => {
  currentPage.value = page;
  // 如果是后端分页，则调用 fetchOrders(page);
  // 如果是前端分页，则不需要在这里调用 fetchOrders，因为 computed property 会处理
  // 假设当前是前端分页，所以这里可以不调用 fetchOrders，但如果数据量大，后端分页更好
  // 为简单起见，如果数据量不大，可以保持现状或每次都重新获取（取决于getUserOrderList的实现）
  fetchOrders(page); // 如果 getUserOrderList 不支持分页，这会重新加载所有数据
};

const canCancelOrder = (status: string | undefined) => status?.toLowerCase() === 'pending' || status?.toLowerCase() === '待支付';
const canPayOrder = (status: string | undefined) => status?.toLowerCase() === 'pending' || status?.toLowerCase() === '待支付';
const canReviewOrder = (status: string | undefined) => status?.toLowerCase() === 'completed' || status?.toLowerCase() === '已完成';

const handleCancelOrder = async (orderId: number) => {
  const order = orders.value.find(o => o.id === orderId);
  if (!order) return;
  try {
    await ElMessageBox.confirm('您确定要取消这个订单吗？', '取消订单', {
      confirmButtonText: '确定取消',
      cancelButtonText: '再想想',
      type: 'warning',
    });
    order.cancelling = true;
    const res = await apiCancelOrder(orderId);
    if (res.code === 200) {
      ElMessage.success('订单已取消');
      fetchOrders(currentPage.value); // 刷新列表
    } else {
      ElMessage.error(res.message || '取消订单失败');
    }
  } catch (action) {
    if (action !== 'confirm') ElMessage.info('操作已取消');
  } finally {
    if (order) order.cancelling = false;
  }
};

const handlePayOrder = async (orderId: number) => {
  ElMessage.info(`订单 ${orderId} 的支付功能待集成支付宝或微信支付。`);
  // 实际支付逻辑:
  // try {
  //   const res = await initiateAlipayPayment({ outTradeNo: String(orderId) }); // 假设有此API
  //   if (res.code === 200 && res.data) {
  //     // 处理支付宝返回的表单或URL
  //     const div = document.createElement('div');
  //     div.innerHTML = res.data; // res.data 是form表单HTML
  //     document.body.appendChild(div);
  //     (div.forms as HTMLCollectionOf<HTMLFormElement>)[0].submit();
  //     document.body.removeChild(div);
  //   } else {
  //     ElMessage.error(res.message || '发起支付失败');
  //   }
  // } catch (error) {
  //   ElMessage.error('支付请求失败');
  // }
};

const handleOpenReviewDialog = (order: OrderViewDTO) => {
  if (!order.reservationId) {
    ElMessage.error('无法评价：订单缺少关联的预约信息。');
    return;
  }
  currentReviewingOrder.value = order; // 保存整个订单信息，方便在弹窗中显示服务名等
  reviewFormData.reservationId = order.reservationId;
  reviewFormData.rating = 0;
  reviewFormData.comment = '';
  reviewSubmitDialogVisible.value = true;
};

const resetReviewForm = () => {
  reviewFormRef.value?.resetFields();
  reviewFormData.rating = 0;
  reviewFormData.comment = '';
  currentReviewingOrder.value = null;
};

const submitReview = async () => {
  if (!reviewFormRef.value) return;
  await reviewFormRef.value.validate(async (valid) => {
    if (valid) {
      submittingReview.value = true;
      try {
        const res = await apiSubmitReview(reviewFormData);
        if (res.code === 200) {
          ElMessage.success('评价提交成功！感谢您的反馈。');
          reviewSubmitDialogVisible.value = false;
          fetchOrders(currentPage.value); // 刷新列表以更新 reviewId
        } else {
          ElMessage.error(res.message || '评价提交失败');
        }
      } catch (error) {
        console.error("Submit review error:", error);
        ElMessage.error('提交评价时发生错误');
      } finally {
        submittingReview.value = false;
      }
    }
  });
};

const handleViewReview = async (order: OrderViewDTO) => {
  if (!order.reservationId) {
    ElMessage.error('无法查看评价：订单缺少关联的预约信息。');
    return;
  }
  currentReviewingOrder.value = order; // 保存订单信息，用于在弹窗中显示服务名
  loadingReviewDetail.value = true;
  reviewViewDialogVisible.value = true;
  currentViewedReview.value = null;
  try {
    const res = await getReviewByReservation(order.reservationId);
    if (res.code === 200 && res.data) {
      currentViewedReview.value = res.data;
    } else if (res.code === 200 && !res.data) {
      ElMessage.info('您尚未对该服务进行评价。'); // 理论上不应到这里，因为按钮条件是 row.reviewId
      reviewViewDialogVisible.value = false;
    } else {
      ElMessage.error(res.message || '获取评价详情失败');
      reviewViewDialogVisible.value = false;
    }
  } catch (error) {
    console.error("View review error:", error);
    ElMessage.error('请求评价详情时发生错误');
    reviewViewDialogVisible.value = false;
  } finally {
    loadingReviewDetail.value = false;
  }
};

const formatDateTime = (dateTimeStr: string | undefined): string => {
  if (!dateTimeStr) return '-';
  try {
    return new Date(dateTimeStr).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second:'2-digit' });
  } catch (e) { return dateTimeStr; }
};
const formatOrderStatus = (statusKey: string | undefined): string => {
  if (!statusKey) return '未知';
  const statusMap: Record<string, string> = {
    'pending': '待支付',
    'paid': '已支付/待服务',
    'completed': '已完成',
    'cancelled': '已取消',
  };
  return statusMap[statusKey.toLowerCase()] || statusKey;
};
const getOrderStatusType = (statusKey: string | undefined): 'success' | 'warning' | 'info' | 'danger' | 'primary' => {
  if (!statusKey) return 'info';
  switch (statusKey.toLowerCase()) {
    case 'paid': return 'warning';
    case 'completed': return 'success';
    case 'pending': return 'info';
    case 'cancelled': return 'danger';
    default: return 'info';
  }
};

onMounted(() => {
  fetchOrders();
});
</script>

<style scoped>
.my-order-list-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}
.card-header span .el-icon {
  margin-right: 8px;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
.el-descriptions {
  margin-top: 10px;
}
</style>