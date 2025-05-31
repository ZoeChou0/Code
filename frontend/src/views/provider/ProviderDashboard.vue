<template>
  <div class="provider-dashboard">
    <el-container>
      <el-main>
        <el-row :gutter="20" class="data-overview">
          <el-col :span="6">
            <el-card shadow="hover" class="data-card real-data-card">
              <template #header>
                <div class="card-header">
                  <el-icon><Tickets /></el-icon>
                  <span>待处理预约</span>
                </div>
              </template>
              <div class="card-content">
                <div class="number">3</div>
                 <div class="trend">
                  <router-link :to="{ name: 'ProviderReservationList' }">查看详情</router-link>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="data-card real-data-card">
              <template #header>
                <div class="card-header">
                  <el-icon><Briefcase /></el-icon>
                  <span>上架中服务</span>
                </div>
              </template>
              <div class="card-content">
                <div class="number">{{ providerStats.activeServicesCount }}</div>
                 <div class="trend">
                  <router-link :to="{ name: 'ProviderServiceList' }">查看详情</router-link>
                </div>
              </div>
            </el-card>
          </el-col>
           <el-col :span="6">
            <el-card shadow="hover" class="data-card">
              <template #header>
                <div class="card-header">
                  <el-icon><Calendar /></el-icon>
                  <span>本月订单 (模拟)</span>
                </div>
              </template>
              <div class="card-content">
                <div class="number">{{ mockData.monthlyOrders }}</div>
                <div class="trend" :class="{ 'up': mockData.orderTrend > 0 }">
                  <el-icon><CaretTop v-if="mockData.orderTrend > 0" /><CaretBottom v-else /></el-icon>
                  {{ Math.abs(mockData.orderTrend) }}% 较上月
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="data-card">
              <template #header>
                <div class="card-header">
                  <el-icon><Money /></el-icon>
                  <span>本月收入</span>
                </div>
              </template>
              <div class="card-content">
                <div class="number">¥{{ mockData.monthlyIncome.toLocaleString() }}</div>
                <div class="trend" :class="{ 'up': mockData.incomeTrend > 0 }">
                  <el-icon><CaretTop v-if="mockData.incomeTrend > 0" /><CaretBottom v-else /></el-icon>
                  {{ Math.abs(mockData.incomeTrend) }}% 较上月
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
         <el-row :gutter="20" class="data-overview">
        </el-row>

        <el-row :gutter="20" class="charts-section">
          <el-col :span="16">
            <el-card shadow="hover" class="chart-card">
              <template #header>
                <div class="card-header">
                  <span>收入趋势 (模拟)</span>
                  <el-radio-group v-model="incomeChartPeriod" size="small">
                    <el-radio-button label="week">周</el-radio-button>
                    <el-radio-button label="month">月</el-radio-button>
                    <el-radio-button label="year">年</el-radio-button>
                  </el-radio-group>
                </div>
              </template>
              <div class="chart-container" ref="incomeChartRef"></div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover" class="chart-card">
              <template #header>
                <div class="card-header">
                  <span>服务类型分布 (模拟)</span>
                </div>
              </template>
              <div class="chart-container" ref="serviceTypeChartRef"></div>
            </el-card>
          </el-col>
        </el-row>

        <el-row :gutter="20" class="reviews-section">
          <el-col :span="12">
            <el-card shadow="hover" class="reviews-card">
              <template #header>
                <div class="card-header">
                  <span>评分分布</span>
                </div>
              </template>
              <div class="rating-distribution">
                <div v-for="(count, rating) in mockData.ratingDistribution" :key="rating" class="rating-bar">
                  <span class="rating-label">{{ rating }}星</span>
                  <el-progress 
                    :percentage="mockData.totalReviews > 0 ? (count / mockData.totalReviews) * 100 : 0" 
                    :color="getRatingColor(parseInt(rating as string))"
                    :show-text="false"
                  />
                  <span class="rating-count">{{ count }}</span>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover" class="reviews-card">
              <template #header>
                <div class="card-header">
                  <span>最新评价 (模拟)</span>
                </div>
              </template>
              <div class="recent-reviews">
                <div v-for="review in mockData.recentReviews" :key="review.id" class="review-item">
                  <div class="review-header">
                    <span class="reviewer-name">{{ review.userName }}</span>
                    <el-rate v-model="review.rating" disabled />
                    <span class="review-date">{{ review.date }}</span>
                  </div>
                  <p class="review-content">{{ review.comment }}</p>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { 
  Calendar, Money, Star, User, CaretTop, CaretBottom,
  Tickets, Briefcase // 新增导入
} from '@element-plus/icons-vue';
import * as echarts from 'echarts';
import { getProviderDashboardStats } from '@/api/dashboard'; // API调用
import type { ProviderDashboardStats } from '@/api/dashboard'; // API 类型

// 图表引用
const incomeChartRef = ref<HTMLElement>();
const serviceTypeChartRef = ref<HTMLElement>();

// ECharts 实例
let incomeChartInstance: echarts.ECharts | null = null;
let serviceTypeChartInstance: echarts.ECharts | null = null;


// 图表周期选择
const incomeChartPeriod = ref('month');

// 从后端获取的真实统计数据
const providerStats = ref<Partial<ProviderDashboardStats>>({
  pendingReservationsCount: 0,
  activeServicesCount: 0,
  // completedOrdersThisMonth: 0, // 这些字段后端DTO有，但目前未填充
  // monthlyRevenue: 0,
});

// 模拟数据 (用于模板中尚未从后端获取的部分)
const mockData = reactive({
  // 概览数据
  monthlyOrders: 4,
  orderTrend: 12.5,
  monthlyIncome: 600,
  incomeTrend: 8.3,
  averageRating: 4.7,
  activeCustomers: 89,
  customerTrend: 5.2,

  // 评分分布
  ratingDistribution: {
    5: 3,
    4: 2,
    3: 0,
    2: 0,
    1: 0
  },
  totalReviews: 93,

  // 最新评价
  recentReviews: [
    { id: 1, userName: '张小明', rating: 5, date: '2025-05-15', comment: '服务很专业，宠物很享受美容过程，环境也很干净。'},
    { id: 2, userName: '李小花', rating: 4, date: '2025-05-14', comment: '美容师很有耐心，对宠物很温柔，效果很好！'},
  ]
});

// 获取评分颜色
const getRatingColor = (rating: number) => {
  const colors: { [key: number]: string } = { // 明确key类型
    5: '#67C23A', 4: '#85CE61', 3: '#E6A23C', 2: '#F56C6C', 1: '#F56C6C'
  };
  return colors[rating];
};

// 获取后端数据
async function fetchProviderStats() {
  try {
    const res = await getProviderDashboardStats();
    if (res.code === 200 && res.data) {
      providerStats.value = { // 只更新后端提供的字段
        pendingReservationsCount: res.data.pendingReservationsCount,
        activeServicesCount: res.data.activeServicesCount,
        // completedOrdersThisMonth: res.data.completedOrdersThisMonth, // 如果后端开始提供，取消注释
        // monthlyRevenue: res.data.monthlyRevenue, // 如果后端开始提供，取消注释
      };
      ElMessage.success('统计数据已更新');
    } else {
      ElMessage.error(res.message || '获取服务商统计数据失败');
    }
  } catch (error) {
    console.error('获取服务商统计数据出错:', error);
    ElMessage.error('获取统计数据时发生网络错误');
  }
}


// 初始化收入趋势图表
const initIncomeChart = () => {
  if (!incomeChartRef.value) return;
  if (incomeChartInstance) {
    incomeChartInstance.dispose();
  }
  incomeChartInstance = echarts.init(incomeChartRef.value);
  
  // TODO: 替换为真实数据或基于 incomeChartPeriod 从后端获取
  let xAxisData: string[] = [];
  let seriesData: number[] = [];

  switch(incomeChartPeriod.value) {
    case 'week':
      xAxisData = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
      seriesData = [3200, 4500, 5800, 4200, 6300, 7800, 6900];
      break;
    case 'month':
      xAxisData = ['第一周', '第二周', '第三周', '第四周']; // 简化示例
      seriesData = [15000, 17000, 16000, 18000];
      break;
    case 'year':
      xAxisData = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'];
      seriesData = [45000, 50000, 55000, 60000, 65000, 70000, 75000, 80000, 78000, 72000, 68000, 70000];
      break;
  }

  const option = {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: xAxisData },
    yAxis: { type: 'value', name: '收入 (元)' },
    series: [{
      data: seriesData, type: 'line', smooth: true, areaStyle: { opacity: 0.1 }, itemStyle: { color: '#409EFF' }
    }]
  };
  incomeChartInstance.setOption(option);
};

// 初始化服务类型分布图表
const initServiceTypeChart = () => {
  if (!serviceTypeChartRef.value) return;
   if (serviceTypeChartInstance) {
    serviceTypeChartInstance.dispose();
  }
  serviceTypeChartInstance = echarts.init(serviceTypeChartRef.value);
  // TODO: 替换为真实的服务类型数据
  const option = {
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', right: 10, top: 'center' },
    series: [{
      type: 'pie', radius: ['40%', '70%'], avoidLabelOverlap: false,
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: '14', fontWeight: 'bold' }},
      labelLine: { show: false },
      data: [ // 模拟数据
        { value: 2, name: '宠物美容' }, { value: 2, name: '宠物寄养' },
        { value: 2, name: '宠物医疗' }, { value: 2, name: '宠物训练' }
      ]
    }]
  };
  serviceTypeChartInstance.setOption(option);
};

// 监听图表周期变化
watch(incomeChartPeriod, () => {
  initIncomeChart();
});

onMounted(() => {
  fetchProviderStats(); // 获取后端数据
  initIncomeChart();
  initServiceTypeChart();

  // 监听窗口大小变化，重绘图表
  window.addEventListener('resize', () => {
    incomeChartInstance?.resize();
    serviceTypeChartInstance?.resize();
  });
});
</script>

<style scoped lang="scss">
.provider-dashboard {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;

  .data-overview {
    margin-bottom: 20px;
  }

  .data-card {
    .card-header {
      display: flex;
      align-items: center;
      font-size: 16px;
      font-weight: 500;
      
      .el-icon {
        margin-right: 8px;
        font-size: 18px;
      }
    }
    &.real-data-card .card-header .el-icon { /* 特定样式给真实数据卡片图标 */
      color: #67C23A; 
    }
     &:not(.real-data-card) .card-header .el-icon { /* 模拟数据卡片图标 */
      color: #409EFF;
    }


    .card-content {
      text-align: center;
      padding: 10px 0;

      .number {
        font-size: 24px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 8px;
      }

      .trend {
        font-size: 14px;
        color: #F56C6C; // 默认下降趋势颜色
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 4px;

        &.up {
          color: #67C23A; // 上升趋势颜色
        }
      }
      .rating-stars {
        display: flex;
        justify-content: center;
        margin-top: 8px;
      }
    }
  }

  .charts-section {
    margin-bottom: 20px;
  }

  .chart-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .chart-container {
      height: 300px;
    }
  }

  .reviews-section {
    .reviews-card {
      .rating-distribution {
        padding: 10px;
        .rating-bar {
          display: flex;
          align-items: center;
          margin-bottom: 12px;
          .rating-label { width: 40px; font-size: 14px; color: #606266; }
          .el-progress { flex: 1; margin: 0 12px; }
          .rating-count { width: 30px; text-align: right; font-size: 14px; color: #909399;}
        }
      }
      .recent-reviews {
        .review-item {
          padding: 12px 0;
          border-bottom: 1px solid #EBEEF5;
          &:last-child { border-bottom: none; }
          .review-header {
            display: flex; align-items: center; margin-bottom: 8px; gap: 12px;
            .reviewer-name { font-weight: 500; color: #303133; }
            .review-date { color: #909399; font-size: 13px; }
          }
          .review-content { margin: 0; font-size: 14px; color: #606266; line-height: 1.5; }
        }
      }
    }
  }
}
</style>