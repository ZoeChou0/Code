<template>
  <el-container class="notification-center-container">
    <el-header class="notification-header">
      <h1>我的通知</h1>
    
    </el-header>

    <el-main v-loading="loading">
      <div v-if="!loading && notifications.length === 0" class="empty-state">
        <el-empty description="暂无通知" />
      </div>

      <el-scrollbar v-else-if="notifications.length > 0" class="notification-list-scrollbar">
        <ul class="notification-list">
          <li
            v-for="notification in notifications"
            :key="notification.id"
            :class="['notification-item', `level-${notification.level || 'info'}`]"
            @click="handleNotificationClick(notification)"
          >
            <div class="item-header">
              <el-tag :type="getTagType(notification.level)" size="small" effect="light" class="level-tag">
                {{ getLevelText(notification.level) }}
              </el-tag>
              <span class="item-title">{{ notification.title || '系统通知' }}</span>
              <span class="item-time">{{ formatTime(notification.created_at) }}</span>
            </div>
            <div class="item-content" v-html="sanitizeContent(notification.content)"></div>
            {/* */}
            <div v-if="notification.data && Object.keys(notification.data).length > 0" class="item-data">
              <el-collapse accordion v-if="shouldShowDetails(notification.type)">
                <el-collapse-item title="查看详情" :name="notification.id.toString()">
                  <pre>{{ notification.data }}</pre>
                  <el-button
                    v-if="notification.type === 'activity_announcement' && notification.data.detailsUrl"
                    type="primary"
                    link
                    size="small"
                    @click.stop="navigateToUrl(notification.data.detailsUrl as string)"
                    style="margin-top: 5px;"
                  >
                    前往活动页面
                  </el-button>
                </el-collapse-item>
              </el-collapse>
            </div>
          </li>
        </ul>
      </el-scrollbar>

      <el-pagination
        v-if="!loading && totalNotifications > 0"
        background
        layout="prev, pager, next, total"
        :total="totalNotifications"
        :current-page="currentPage"
        :page-size="pageSize"
        @current-change="handlePageChange"
        class="pagination-bar"
      />
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRouter } from 'vue-router';
import type { NotificationItem, PaginatedNotifications } from '@/api/notification'; // 导入 PaginatedNotifications
import { getMyNotifications } from '@/api/notification';

const router = useRouter();
const loading = ref(false);
const notifications = ref<NotificationItem[]>([]);
const totalNotifications = ref(0);
const currentPage = ref(1);
const pageSize = ref(10); // 与后端协商或从后端获取

const fetchNotifications = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
    };
    // getMyNotifications 现在返回 Promise<BackendResult<PaginatedNotifications>>
    const res = await getMyNotifications(params);
    if (res.code === 200 && res.data) {
      // res.data 现在是 PaginatedNotifications 类型
      notifications.value = res.data.items.map(item => {
        // 确保 item 已经是 NotificationItem 类型，或者在此处进行转换/映射
        // 如果后端 NotificationViewDTO 的 data 字段已经是 Map<String, Object>
        // 并且前端 NotificationItem 的 data 字段是 Record<string, any>，则类型应该匹配
        return {
          ...item,
          // parsedData 字段可以移除，如果 data 字段已是所需格式
        };
      });
      totalNotifications.value = res.data.total;
      currentPage.value = res.data.currentPage; // 现在可以安全访问
      // pageSize.value = res.data.pageSize; // 如果后端也返回 pageSize
    } else {
      ElMessage.error(res.message || '获取通知列表失败');
      notifications.value = [];
      totalNotifications.value = 0;
    }
  } catch (error: any) {
    console.error('获取通知列表异常:', error);
    ElMessage.error(error.message || '获取通知列表时发生网络错误');
    notifications.value = [];
    totalNotifications.value = 0;
  } finally {
    loading.value = false;
  }
};

const handlePageChange = (page: number) => {
  currentPage.value = page;
  fetchNotifications();
};

const handleNotificationClick = (notification: NotificationItem) => {
  console.log('Notification clicked:', notification);
  // 确保 notification.data 存在且 detailsUrl 存在
  if (notification.type === 'activity_announcement' && notification.data?.detailsUrl) {
    navigateToUrl(notification.data.detailsUrl as string);
  } else if (notification.data && Object.keys(notification.data).length > 0) {
    ElMessageBox.alert(`<pre>${JSON.stringify(notification.data, null, 2)}</pre>`, notification.title || '通知详情', {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '关闭',
    });
  }
};

const navigateToUrl = (url: string) => {
  if (url.startsWith('/')) {
    router.push(url).catch(err => {
      console.error('[Vue Router] Navigation failed:', err);
      window.open(window.location.origin + url, '_blank');
    });
  } else {
    window.open(url, '_blank');
  }
};

const formatTime = (timeStr: string) => {
  if (!timeStr) return '';
  const date = new Date(timeStr);
  return date.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' });
};

const getTagType = (level?: string): 'success' | 'info' | 'warning' | 'danger' | '' => {
  switch (level) {
    case 'success': return 'success';
    case 'warning': return 'warning';
    case 'error': return 'danger';
    case 'info':
    default:
      return 'info';
  }
};
const getLevelText = (level?: string): string => {
  switch (level) {
    case 'success': return '成功';
    case 'warning': return '警告';
    case 'error': return '错误';
    case 'info':
    default:
      return '通知';
  }
};

const sanitizeContent = (content: string) => {
  return content; // 假设后端内容安全
};

const shouldShowDetails = (type?: string): boolean => {
    return type === 'activity_announcement' || type === 'system_broadcast';
};

onMounted(() => {
  fetchNotifications();
});

</script>

<style scoped lang="scss">
/* 样式与之前版本类似 */
.notification-center-container {
  height: calc(100vh - 100px); 
  display: flex;
  flex-direction: column;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #ebeef5;
  h1 {
    margin: 0;
    font-size: 1.5em;
  }
}

.el-main {
  padding: 0; 
  display: flex;
  flex-direction: column;
  flex-grow: 1;
  overflow: hidden; 
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-grow: 1;
  color: #909399;
}

.notification-list-scrollbar {
  flex-grow: 1;
  padding: 0 20px 20px 20px; 
}

.notification-list {
  list-style: none;
  padding: 0;
  margin: 0;

  .notification-item {
    padding: 15px;
    border-bottom: 1px solid #ebeef5;
    cursor: pointer;
    transition: background-color 0.2s;
    position: relative;

    &:hover {
      background-color: #f5f7fa;
    }

    .item-header {
      display: flex;
      align-items: center;
      margin-bottom: 8px;
      .level-tag {
        margin-right: 8px;
      }
      .item-title {
        flex-grow: 1;
        font-size: 1.1em;
        color: #303133;
      }
      .item-time {
        font-size: 0.85em;
        color: #909399;
        margin-left: 15px;
      }
    }

    .item-content {
      font-size: 0.95em;
      color: #606266;
      line-height: 1.6;
      word-break: break-word;
      white-space: pre-wrap; 
    }
    .item-data {
        margin-top: 10px;
        font-size: 0.9em;
        background-color: #f9f9f9;
        padding: 8px;
        border-radius: 4px;
        pre {
            white-space: pre-wrap;
            word-break: break-all;
            margin: 0;
        }
    }
  }
}
.pagination-bar {
  padding: 15px 20px;
  text-align: right;
  border-top: 1px solid #ebeef5;
}
</style>