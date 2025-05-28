<!-- <script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue';
// 从 Element Plus 导入 ElNotification。ElMessageBox 已被移除，因为未被使用。
import { ElNotification } from 'element-plus';
import { useRouter } from 'vue-router';

const webSocket = ref<WebSocket | null>(null);
const isConnected = ref(false);
// 定时器ID在浏览器环境中是 number 类型
const retryInterval = ref<number | null>(null);
let heartbeatTimer: number | null = null;

const router = useRouter();

interface WebSocketMessage {
  type: string;
  title?: string;
  content: string;
  timestamp: number;
  level?: 'info' | 'success' | 'warning' | 'error';
  data?: any;
}

const getWebSocketURL = (): string => {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  const backendHost = 'localhost:8080'; // 后端 WebSocket 服务器地址
  const wsPath = '/ws/notifications'; // 后端 WebSocket 端点路径
  // 在生产环境中，您可能需要更动态地配置 backendHost
  return `${protocol}//${backendHost}${wsPath}`;
};

const connectWebSocket = () => {
  const wsUrl = getWebSocketURL();
  console.log('[WebSocket] Attempting to connect to:', wsUrl);

  if (webSocket.value && (webSocket.value.readyState === WebSocket.OPEN || webSocket.value.readyState === WebSocket.CONNECTING)) {
    console.log('[WebSocket] Already connected or connecting.');
    return;
  }

  webSocket.value = new WebSocket(wsUrl);

  webSocket.value.onopen = () => {
    console.log('[WebSocket] Connection established.');
    isConnected.value = true;
    if (retryInterval.value !== null) {
      window.clearInterval(retryInterval.value); // 明确使用 window.clearInterval
      retryInterval.value = null;
      console.log('[WebSocket] Reconnect interval cleared.');
    }
    ElNotification({
      title: '系统提示',
      message: '已连接到实时通知服务',
      type: 'success',
      duration: 2500,
    });
    startHeartbeat();
  };

  webSocket.value.onmessage = (event) => {
    console.log('[WebSocket] Message from server: ', event.data);
    try {
      const message = JSON.parse(event.data as string) as WebSocketMessage;
      const notificationTitle = message.title || '系统通知';
      let notificationMessage = message.content || '您有一条新消息。';
      const notificationType = message.level || 'info';
      let duration = 5000;
      let showCloseButton = false;
      let onClickHandler: (() => void) | undefined = undefined;

      switch (message.type) {
        case 'system_broadcast':
          // General system broadcast
          break;
        case 'provider_approved':
          if (message.data && message.data.providerName) {
            notificationMessage = `服务商 "${message.data.providerName}" 的资质已通过审核！`;
          }
          duration = 7000;
          break;
        case 'activity_announcement':
          showCloseButton = true;
          duration = 10000;
          if (message.data && message.data.detailsUrl) {
            onClickHandler = () => {
              console.log('[WebSocket] Activity notification clicked, navigating to:', message.data.detailsUrl);
              if (typeof message.data.detailsUrl === 'string' && message.data.detailsUrl.startsWith('/')) {
                router.push(message.data.detailsUrl).catch(err => {
                  console.error('[Vue Router] Navigation failed:', err);
                  // Fallback to opening in a new tab if router push fails or not applicable
                  window.open(window.location.origin + message.data.detailsUrl, '_blank');
                });
              } else if (typeof message.data.detailsUrl === 'string') {
                window.open(message.data.detailsUrl, '_blank');
              }
            };
          }
          break;
        default:
          console.warn('[WebSocket] Received unhandled message type:', message.type);
      }

      ElNotification({
        title: notificationTitle,
        message: notificationMessage,
        type: notificationType as 'success' | 'warning' | 'info' | 'error',
        duration: duration,
        showClose: showCloseButton,
        onClick: onClickHandler,
      });

    } catch (error) {
      console.error('[WebSocket] Failed to parse message or not a recognized JSON structure:', error);
      ElNotification({
        title: '收到原始消息',
        message: event.data as string,
        type: 'info',
      });
    }
  };

  webSocket.value.onclose = (event) => {
    console.warn(`[WebSocket] Connection closed: Code=${event.code}, Reason=${event.reason}, WasClean=${event.wasClean}`);
    isConnected.value = false;
    stopHeartbeat();

    // Reconnect logic: only if not a clean close and no retry is already in progress
    if (event.code !== 1000 && event.code !== 1001) { // 1000: Normal Closure, 1001: Going Away
        if (retryInterval.value === null) { 
            console.log('[WebSocket] Attempting to reconnect in 5 seconds...');
            retryInterval.value = window.setInterval(() => { // 明确使用 window.setInterval
                if (!isConnected.value) {
                    console.log('[WebSocket] Retrying connection...');
                    connectWebSocket();
                } else if (retryInterval.value !== null) { // Connection established, clear interval
                    window.clearInterval(retryInterval.value); // 明确使用 window.clearInterval
                    retryInterval.value = null;
                }
            }, 5000);
        }
    } else {
        console.log('[WebSocket] Connection closed normally or navigating away, no auto-reconnect initiated by this close event.');
    }
  };

  webSocket.value.onerror = (errorEvent) => {
    console.error('[WebSocket] Error: ', errorEvent);
    // isConnected.value will be set to false by the onclose event that usually follows an error
  };
};

const startHeartbeat = () => {
  if (heartbeatTimer !== null) window.clearInterval(heartbeatTimer); // 明确使用 window.clearInterval
  heartbeatTimer = window.setInterval(() => { // 明确使用 window.setInterval
    if (webSocket.value && webSocket.value.readyState === WebSocket.OPEN) {
      webSocket.value.send('ping');
      console.log('[WebSocket] Sent ping to server.');
    } else {
      console.warn('[WebSocket] Heartbeat: Connection not open. Current state:', webSocket.value?.readyState);
      // If connection is not open, onclose should handle reconnection attempts.
      // We can stop the heartbeat here if connection is permanently lost or closed.
      if (webSocket.value && 
          (webSocket.value.readyState === WebSocket.CLOSED || webSocket.value.readyState === WebSocket.CLOSING)) {
          stopHeartbeat();
      }
    }
  }, 30000); // Send ping every 30 seconds
  console.log('[WebSocket] Heartbeat started.');
};

const stopHeartbeat = () => {
  if (heartbeatTimer !== null) {
    window.clearInterval(heartbeatTimer); // 明确使用 window.clearInterval
    heartbeatTimer = null;
    console.log('[WebSocket] Heartbeat stopped.');
  }
};

onMounted(() => {
  console.log('[App.vue] Component mounted. Initializing WebSocket.');
  connectWebSocket();
});

onUnmounted(() => {
  console.log('[App.vue] Component unmounted. Closing WebSocket and clearing timers.');
  stopHeartbeat();
  if (retryInterval.value !== null) {
    window.clearInterval(retryInterval.value); // 明确使用 window.clearInterval
    retryInterval.value = null;
  }

  const wsInstance = webSocket.value; // Capture the instance before nulling out the ref
  if (wsInstance) {
    // Detach event handlers to prevent them from running after component is unmounted,
    // especially if they reference component state or methods.
    wsInstance.onopen = null;
    wsInstance.onmessage = null;
    wsInstance.onerror = null;
    
    const currentOnClose = wsInstance.onclose;
    // Replace onclose to prevent our reconnect logic from firing after deliberate close.
    wsInstance.onclose = (event: CloseEvent) => {
        if (currentOnClose) {
            // If there was an original onclose (like the one we set up),
            // call it with the correct 'this' context if needed, though here we are overriding.
            // currentOnClose.call(wsInstance, event); 
        }
        console.log(`[WebSocket] Connection deliberately closed on component unmount. Code: ${event.code}`);
    };

    if (wsInstance.readyState === WebSocket.OPEN || wsInstance.readyState === WebSocket.CONNECTING) {
      console.log('[WebSocket] Closing connection on component unmount...');
      wsInstance.close(1000, "Client component unmounting"); // 1000 indicates a normal closure
    }
  }
  webSocket.value = null; // Clear the ref
});

</script>

<template>
  <router-view />
</template>

<style>
body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
</style> -->


<script setup lang="ts">
import DefaultLayout from './layouts/DefaultLayout.vue'
import { onMounted } from 'vue';
import { useUserStore } from '@/stores/user';


const userStore = useUserStore();


onMounted(async () => {

  if (userStore.token && !userStore.userInfo) {
    console.log('[App.vue onMounted] 检测到 token 但无 userInfo，开始获取...');
    await userStore.getUserInfoAction(); // 等待用户信息获取完成
    console.log('[App.vue onMounted] 用户信息获取结果:', userStore.userInfo);
  }
});
</script>

<template>
  <DefaultLayout>
    <router-view></router-view>
  </DefaultLayout>
</template>

<style>
#app {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
}
</style>