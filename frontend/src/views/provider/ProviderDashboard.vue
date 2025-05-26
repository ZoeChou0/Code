<template>
  <div class="provider-dashboard">
    <el-container>
      <el-main>
        <!-- 数据概览卡片 -->
        <el-row :gutter="20" class="data-overview">
          <el-col :span="6">
            <el-card shadow="hover" class="data-card">
              <template #header>
                <div class="card-header">
                  <el-icon><Calendar /></el-icon>
                  <span>本月订单</span>
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
          <el-col :span="6">
            <el-card shadow="hover" class="data-card">
              <template #header>
                <div class="card-header">
                  <el-icon><Star /></el-icon>
                  <span>平均评分</span>
                </div>
              </template>
              <div class="card-content">
                <div class="number">{{ mockData.averageRating.toFixed(1) }}</div>
                <div class="rating-stars">
                  <el-rate
                    v-model="mockData.averageRating"
                    disabled
                    show-score
                    text-color="#ff9900"
                  />
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="data-card">
              <template #header>
                <div class="card-header">
                  <el-icon><User /></el-icon>
                  <span>活跃客户</span>
                </div>
              </template>
              <div class="card-content">
                <div class="number">{{ mockData.activeCustomers }}</div>
                <div class="trend" :class="{ 'up': mockData.customerTrend > 0 }">
                  <el-icon><CaretTop v-if="mockData.customerTrend > 0" /><CaretBottom v-else /></el-icon>
                  {{ Math.abs(mockData.customerTrend) }}% 较上月
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 图表区域 -->
        <el-row :gutter="20" class="charts-section">
          <el-col :span="16">
            <el-card shadow="hover" class="chart-card">
              <template #header>
                <div class="card-header">
                  <span>收入趋势</span>
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
                  <span>服务类型分布</span>
                </div>
              </template>
              <div class="chart-container" ref="serviceTypeChartRef"></div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 评价统计 -->
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
                    :percentage="(count / mockData.totalReviews) * 100" 
                    :color="getRatingColor(rating)"
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
                  <span>最新评价</span>
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
import { ref, onMounted, reactive } from 'vue';
import { 
  Calendar, Money, Star, User, CaretTop, CaretBottom,
  Document, ChatDotRound
} from '@element-plus/icons-vue';
import * as echarts from 'echarts';

// 图表引用
const incomeChartRef = ref<HTMLElement>();
const serviceTypeChartRef = ref<HTMLElement>();

// 图表周期选择
const incomeChartPeriod = ref('month');

// 模拟数据
const mockData = reactive({
  // 概览数据
  monthlyOrders: 156,
  orderTrend: 12.5,
  monthlyIncome: 45680,
  incomeTrend: 8.3,
  averageRating: 4.7,
  activeCustomers: 89,
  customerTrend: 5.2,

  // 评分分布
  ratingDistribution: {
    5: 45,
    4: 28,
    3: 12,
    2: 5,
    1: 3
  },
  totalReviews: 93,

  // 最新评价
  recentReviews: [
    {
      id: 1,
      userName: '张小明',
      rating: 5,
      date: '2024-03-15',
      comment: '服务很专业，宠物很享受美容过程，环境也很干净。'
    },
    {
      id: 2,
      userName: '李小花',
      rating: 4,
      date: '2024-03-14',
      comment: '美容师很有耐心，对宠物很温柔，效果很好！'
    },
    {
      id: 3,
      userName: '王大力',
      rating: 5,
      date: '2024-03-13',
      comment: '寄养环境很好，每天都会发视频，很放心。'
    }
  ]
});

// 获取评分颜色
const getRatingColor = (rating: number) => {
  const colors = {
    5: '#67C23A',
    4: '#85CE61',
    3: '#E6A23C',
    2: '#F56C6C',
    1: '#F56C6C'
  };
  return colors[rating as keyof typeof colors];
};

// 初始化收入趋势图表
const initIncomeChart = () => {
  if (!incomeChartRef.value) return;
  
  const chart = echarts.init(incomeChartRef.value);
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: {
      type: 'value',
      name: '收入 (元)'
    },
    series: [{
      data: [3200, 4500, 5800, 4200, 6300, 7800, 6900],
      type: 'line',
      smooth: true,
      areaStyle: {
        opacity: 0.1
      },
      itemStyle: {
        color: '#409EFF'
      }
    }]
  };
  chart.setOption(option);
};

// 初始化服务类型分布图表
const initServiceTypeChart = () => {
  if (!serviceTypeChartRef.value) return;
  
  const chart = echarts.init(serviceTypeChartRef.value);
  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center'
    },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false
      },
      emphasis: {
        label: {
          show: true,
          fontSize: '14',
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
      data: [
        { value: 45, name: '宠物美容' },
        { value: 30, name: '宠物寄养' },
        { value: 15, name: '宠物医疗' },
        { value: 10, name: '宠物训练' }
      ]
    }]
  };
  chart.setOption(option);
};

onMounted(() => {
  initIncomeChart();
  initServiceTypeChart();

  // 监听窗口大小变化，重绘图表
  window.addEventListener('resize', () => {
    const incomeChart = echarts.getInstanceByDom(incomeChartRef.value!);
    const serviceTypeChart = echarts.getInstanceByDom(serviceTypeChartRef.value!);
    incomeChart?.resize();
    serviceTypeChart?.resize();
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
        color: #409EFF;
      }
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
        color: #F56C6C;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 4px;

        &.up {
          color: #67C23A;
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

          .rating-label {
            width: 40px;
            font-size: 14px;
            color: #606266;
          }

          .el-progress {
            flex: 1;
            margin: 0 12px;
          }

          .rating-count {
            width: 30px;
            text-align: right;
            font-size: 14px;
            color: #909399;
          }
        }
      }

      .recent-reviews {
        .review-item {
          padding: 12px 0;
          border-bottom: 1px solid #EBEEF5;

          &:last-child {
            border-bottom: none;
          }

          .review-header {
            display: flex;
            align-items: center;
            margin-bottom: 8px;
            gap: 12px;

            .reviewer-name {
              font-weight: 500;
              color: #303133;
            }

            .review-date {
              color: #909399;
              font-size: 13px;
            }
          }

          .review-content {
            margin: 0;
            font-size: 14px;
            color: #606266;
            line-height: 1.5;
          }
        }
      }
    }
  }
}
</style> 