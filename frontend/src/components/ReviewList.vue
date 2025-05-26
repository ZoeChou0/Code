<template>
  <div class="review-list">
    <div class="review-header">
      <h3>用户评价</h3>
      <div class="average-rating" v-if="averageRating !== null">
        <span class="rating-value">{{ averageRating.toFixed(1) }}</span>
        <el-rate
          v-model="averageRating"
          disabled
          show-score
          text-color="#ff9900"
          score-template="{value}"
        />
        <span class="review-count">({{ reviews.length }}条评价)</span>
      </div>
    </div>

    <div v-if="reviews.length === 0" class="no-reviews">
      暂无评价
    </div>

    <div v-else class="review-items">
      <div v-for="review in reviews" :key="review.id" class="review-item">
        <div class="review-user">
          <el-avatar :size="40" :icon="UserFilled" />
          <div class="user-info">
            <span class="user-name">用户{{ review.userId }}</span>
            <div class="review-meta">
              <el-rate
                v-model="review.rating"
                disabled
                show-score
                text-color="#ff9900"
                score-template="{value}"
              />
              <span class="review-time">{{ formatDate(review.createdAt) }}</span>
            </div>
          </div>
        </div>
        <div class="review-content" v-if="review.comment">
          {{ review.comment }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { UserFilled } from '@element-plus/icons-vue'
import { getReviewsByService, getAverageRatingByService } from '@/api/review'
import type { Review } from '@/api/review'

const props = defineProps<{
  serviceItemId: number
}>()

const reviews = ref<Review[]>([])
const averageRating = ref<number | null>(null)

const fetchReviews = async () => {
  try {
    const [reviewsRes, ratingRes] = await Promise.all([
      getReviewsByService(props.serviceItemId),
      getAverageRatingByService(props.serviceItemId)
    ])
    
    if (reviewsRes.code === 200) {
      reviews.value = reviewsRes.data
    }
    if (ratingRes.code === 200) {
      averageRating.value = ratingRes.data
    }
  } catch (error) {
    console.error('获取评价列表失败:', error)
  }
}

const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  fetchReviews()
})
</script>

<style scoped lang="scss">
.review-list {
  .review-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h3 {
      margin: 0;
      font-size: 18px;
      color: #303133;
    }

    .average-rating {
      display: flex;
      align-items: center;
      gap: 8px;

      .rating-value {
        font-size: 24px;
        font-weight: bold;
        color: #ff9900;
      }

      .review-count {
        color: #909399;
        font-size: 14px;
      }
    }
  }

  .no-reviews {
    text-align: center;
    color: #909399;
    padding: 40px 0;
  }

  .review-items {
    .review-item {
      padding: 20px 0;
      border-bottom: 1px solid #ebeef5;

      &:last-child {
        border-bottom: none;
      }

      .review-user {
        display: flex;
        gap: 12px;
        margin-bottom: 12px;

        .user-info {
          flex: 1;

          .user-name {
            font-weight: 500;
            color: #303133;
          }

          .review-meta {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-top: 4px;

            .review-time {
              color: #909399;
              font-size: 14px;
            }
          }
        }
      }

      .review-content {
        color: #606266;
        line-height: 1.6;
        margin-left: 52px;
      }
    }
  }
}
</style> 