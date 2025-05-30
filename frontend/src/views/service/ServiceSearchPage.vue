
<template>
  <div class="service-search-page">
    <el-container class="page-container">
      <el-aside width="280px" class="sidebar-aside">
        <el-card shadow="never" class="filter-card">
          <template #header>
            <div class="filter-header">
              <el-icon><FilterIcon /></el-icon>
              <span>搜索筛选</span>
            </div>
          </template>
          <el-form :model="searchForm" label-position="top" size="small">

            <el-form-item label="服务类型/关键词"> <el-select
                v-model="searchForm.serviceTypeKeyword"
                placeholder="选择或输入服务类型/关键词"
                clearable
                filterable
                allow-create
                default-first-option
                style="width: 100%;"
              >
                <el-option label="宠物美容" value="宠物美容"></el-option>
                <el-option label="宠物寄养" value="宠物寄养"></el-option>
                <el-option label="宠物医疗" value="宠物医疗"></el-option>
                <el-option label="宠物训练" value="宠物训练"></el-option>
                </el-select>
              <p class="filter-tip">匹配服务名称、描述或服务商名称</p>
            </el-form-item>

            <el-form-item label="地点">
              <el-input v-model="searchForm.locationKeyword" placeholder="输入城市、省份或地址关键字" clearable>
                 <template #prepend><el-icon><LocationInformation /></el-icon></template>
              </el-input>
               <p class="filter-tip">将匹配服务商地址信息</p>
            </el-form-item>

            <el-form-item label="价格范围 (每项服务)">
               <el-slider v-model="searchForm.priceRange" range :max="1000" :step="10" :marks="{0:'¥0', 500:'¥500', 1000:'¥1000+'}" />
               <div class="price-range-display">
                 ¥{{ searchForm.priceRange[0] }} - ¥{{ searchForm.priceRange[1] >= 1000 ? '1000+' : searchForm.priceRange[1] }} </div>
            </el-form-item>

            <el-divider>详细筛选</el-divider>

            <el-form-item label="服务日期 (可选)">
              <el-date-picker
                v-model="searchForm.dates"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                style="width: 100%;"
                value-format="YYYY-MM-DD"
                clearable
                :disabled-date="disabledDate"
              />
               <p class="filter-tip">提示: 此筛选依赖后端支持服务可用日期查询。</p>
            </el-form-item>

            <el-form-item label="您的宠物类型 (用于不接受品种筛选)">
               <el-checkbox-group v-model="searchForm.petTypes">
                  <el-checkbox label="Dog">狗</el-checkbox>
                  <el-checkbox label="Cat">猫</el-checkbox>
                  </el-checkbox-group>
                <p class="filter-tip">筛选出不禁止您宠物类型的服务。</p>
            </el-form-item>

            <el-form-item label="服务商住房条件">
               <el-checkbox-group v-model="searchForm.housing">
                  <el-checkbox label="house">有独立房屋</el-checkbox>
                  <el-checkbox label="fenced_yard">有围栏的院子</el-checkbox>
               </el-checkbox-group>
            </el-form-item>

             <el-form-item>
               <el-button type="primary" @click="handleSearch" :icon="SearchIcon" style="width: 100%;" :loading="submitLoading">
                 应用筛选并搜索
               </el-button>
               <el-button @click="resetFilters" style="width: 100%; margin-top: 10px; margin-left: 0;" :icon="Refresh">
                 重置筛选
               </el-button>
             </el-form-item>

          </el-form>
        </el-card>
      </el-aside>

      <el-main class="main-content">
        <div class="results-header">
            <span v-if="!loading && !loadError">
                {{ filteredServiceList.length > 0 ? `根据您的筛选，找到 ${filteredServiceList.length} 个相关服务` : '当前条件下未找到匹配的服务' }}
            </span>
            <span v-if="loading">正在努力加载服务...</span>
        </div>

        <div v-if="loading" class="loading-state-main">
          <el-skeleton :rows="8" animated />
        </div>
        <el-alert
          v-else-if="loadError"
          title="加载服务列表失败"
          type="error"
          :description="errorMessage || '无法加载服务信息，请检查您的网络连接或稍后重试。'"
          :closable="false"
          show-icon
        >
          <template #default>
            <el-button link type="primary" @click="() => fetchServices(false)" style="margin-left: 0px; padding-top: 5px;">点此重试</el-button>
          </template>
        </el-alert>
        <div v-else-if="filteredServiceList.length === 0" class="empty-state-main">
          <el-empty description="没有找到符合您筛选条件的服务。请尝试调整筛选条件或重置所有筛选。"></el-empty>
        </div>
        <div v-else class="service-list-grid-main">
          <el-card v-for="service in filteredServiceList" :key="service.id" shadow="hover" class="service-card-main">
            <template #header>
                <div class="service-card-header">
                  <span class="service-name" :title="service.name">{{ service.name }}</span>
                  <span class="service-price">¥{{ service.price?.toFixed(2) ?? '待议' }} / {{ service.duration ? service.duration+'分钟' : '次' }}</span>
                </div>
             </template>
             <div class="service-card-body">
                <div class="provider-info-placeholder">
                    <el-avatar :size="40" :src="service.providerProfilePhotoUrl || undefined" :icon="!service.providerProfilePhotoUrl ? ElUser : undefined" style="margin-right: 10px; flex-shrink: 0;"/>
                    <div style="flex-grow: 1;">
                       <div class="provider-name" :title="service.providerName">{{ service.providerName || `服务商ID: ${service.providerId}` }}</div>
                       <div style="font-size: 12px; color: #909399;">
                        地点: {{ service.providerCity || service.providerAddressLine1 || '未知地点' }}
                        <span v-if="service.providerAverageRating != null && service.providerAverageRating > 0"> | 评分: {{ service.providerAverageRating.toFixed(1) }} / 5.0</span>
                        <span v-else> | 暂无评分</span>
                       </div>
                    </div>
                </div>
                 <p class="description" :title="service.description" v-if="service.description">{{ service.description }}</p>
                 <p class="description" v-else>暂无详细描述。</p>
                 <div class="details">
                    <el-tag size="small" effect="plain" type="info" class="detail-tag" v-if="service.dailyCapacity && service.dailyCapacity > 0">每日容量: {{ service.dailyCapacity }}</el-tag>
                    <el-tag size="small" effect="plain" type="success" class="detail-tag" v-if="service.minAge != null">最低年龄: {{ service.minAge }}岁</el-tag>
                    <el-tag size="small" effect="plain" type="warning" class="detail-tag" v-if="service.maxAge != null">最高年龄: {{ service.maxAge }}岁</el-tag>
                 </div>
                 <div class="requirements" v-if="hasRequirements(service)">
                     <el-tooltip v-if="service.requiredVaccinations" :content="`要求疫苗: ${service.requiredVaccinations}`" placement="top">
                       <el-tag size="small" type="warning" effect="light" class="requirement-tag"><el-icon><FirstAidKit /></el-icon>需疫苗</el-tag>
                     </el-tooltip>
                     <el-tooltip v-if="service.requiresNeutered != null" :content="service.requiresNeutered ? '要求宠物已绝育' : '不要求宠物绝育'" placement="top">
                       <el-tag :type="service.requiresNeutered ? 'danger' : 'success'" size="small" effect="light" class="requirement-tag"><el-icon><Link /></el-icon>{{ service.requiresNeutered ? '需绝育' : '不限绝育' }}</el-tag>
                     </el-tooltip>
                     <el-tooltip v-if="service.temperamentRequirements" :content="`性格要求: ${service.temperamentRequirements}`" placement="top">
                        <el-tag size="small" type="info" effect="light" class="requirement-tag"><el-icon><ChatDotRound /></el-icon>性格要求</el-tag>
                     </el-tooltip>
                     <el-tooltip v-if="service.prohibitedBreeds" :content="`不接受品种: ${service.prohibitedBreeds}`" placement="top">
                        <el-tag size="small" color="#BEBEBE" effect="light" class="requirement-tag"><el-icon><CircleClose /></el-icon>禁养品种</el-tag>
                     </el-tooltip>
                 </div>
             </div>
             <template #footer>
               <div class="card-actions">
                  <el-button type="primary" plain size="small" @click="viewServiceDetail(service)">查看详情</el-button>
                  <el-button type="success" size="small" @click="goToReservation(service.id)">立即预约</el-button>
               </div>
             </template>
          </el-card>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'; // 引入 watch
import {
  ElContainer, ElAside, ElMain, ElCard, ElForm, ElFormItem, ElInput, ElSelect, ElOption, ElDatePicker, ElSlider,
  ElCheckboxGroup, ElCheckbox, ElButton, ElTag, ElIcon, ElEmpty, ElSkeleton, ElAlert, ElMessage,
  ElAvatar, ElDivider, ElTooltip // ElImage, ElRadioGroup, ElRadioButton (如果需要)
} from 'element-plus';
import {
    Filter as FilterIcon,
    Search as SearchIcon,
    LocationInformation,
    // Promotion, // 未使用
    Link,
    User as ElUser,
    // Picture, Comment, Dish, Memo, Edit, // 未直接使用，但可以保留或按需移除
    FirstAidKit,
    Refresh,
    ChatDotRound, // 用于性格要求
    CircleClose // 用于禁养品种
} from '@element-plus/icons-vue';
import { getActiveServices } from '@/api/service';
import type { Service as ApiServiceType } from '@/api/service'; // 重命名以避免与HTML <service> 标签冲突
import type { BackendResult } from '@/types/api';
import { useUserStore } from '@/stores/user';
import { useRoute, useRouter } from 'vue-router';

interface ServiceUIData extends ApiServiceType {
  // 可以在这里添加前端特有的临时状态，如果需要
}

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const loading = ref(true); // 初始加载状态为true
const loadError = ref(false);
const errorMessage = ref('');
const serviceList = ref<ServiceUIData[]>([]);
const submitLoading = ref(false); // 用于搜索按钮的loading

const initialSearchForm = {
  serviceTypeKeyword: '',
  locationKeyword: '',
  dates: undefined as [string, string] | undefined,
  priceRange: [0, 1000] as [number, number],
  petTypes: [] as string[],
  housing: [] as string[],
};
const searchForm = reactive({ ...initialSearchForm });

const disabledDate = (time: Date) => {
  return time.getTime() < Date.now() - 8.64e7; // 只能选择今天及以后的日期
};

// 监听路由查询参数的变化，以便在用户通过URL直接访问带参数的搜索页时，能更新表单并触发搜索
watch(() => route.query, (newQuery) => {
    // 只有当路由查询参数实际改变，并且与当前表单状态不同时才更新和重新获取
    // 这避免了因 router.replace 自身触发的不必要更新
    let formNeedsUpdate = false;
    if (newQuery.keyword !== searchForm.serviceTypeKeyword) {
        searchForm.serviceTypeKeyword = (newQuery.keyword as string) || initialSearchForm.serviceTypeKeyword;
        formNeedsUpdate = true;
    }
    if (newQuery.location !== searchForm.locationKeyword) {
        searchForm.locationKeyword = (newQuery.location as string) || initialSearchForm.locationKeyword;
        formNeedsUpdate = true;
    }
    // ... (对其他参数也进行类似比较和更新) ...
    // 简单的实现：如果路由参数变化，就重新获取
    // 但要注意避免 fetchServices -> router.replace -> watch -> fetchServices 的无限循环
    // 通常 onMounted 处理初始加载，用户操作触发 fetchData(true) 来更新路由
}, { deep: true });


onMounted(() => {
  const query = route.query;
  // 初始化表单时，避免触发不必要的 watch 或重复的 router.replace
  searchForm.serviceTypeKeyword = (query.keyword as string) || initialSearchForm.serviceTypeKeyword;
  searchForm.locationKeyword = (query.location as string) || initialSearchForm.locationKeyword;

  const queryMinPrice = parseInt(query.minPrice as string);
  const queryMaxPrice = parseInt(query.maxPrice as string);
  if (!isNaN(queryMinPrice) || !isNaN(queryMaxPrice)) {
    searchForm.priceRange = [
        !isNaN(queryMinPrice) ? queryMinPrice : initialSearchForm.priceRange[0],
        !isNaN(queryMaxPrice) && queryMaxPrice <= 1000 ? queryMaxPrice : initialSearchForm.priceRange[1], // 确保不超过slider的max
    ];
  } else {
    searchForm.priceRange = [...initialSearchForm.priceRange];
  }


  if (query.startDate && query.endDate) {
      searchForm.dates = [query.startDate as string, query.endDate as string];
  } else {
    searchForm.dates = undefined;
  }
  searchForm.petTypes = query.petTypes ? (query.petTypes as string).split(',') : [...initialSearchForm.petTypes];
  searchForm.housing = query.housingConditions ? (query.housingConditions as string).split(',') : [...initialSearchForm.housing];

  fetchServices(false); // 页面加载时获取一次服务列表
});

const fetchServices = async (isUserSearch: boolean = false) => {
  loading.value = true;
  if (isUserSearch) submitLoading.value = true;
  loadError.value = false;
  errorMessage.value = '';

  if (isUserSearch) {
    // ElMessage.info('正在根据筛选条件搜索...'); // 可以保留，但成功/失败/无结果的消息更重要
  }

  try {
    const params: Record<string, any> = {};
    if (searchForm.serviceTypeKeyword && searchForm.serviceTypeKeyword.trim() !== '') params.keyword = searchForm.serviceTypeKeyword.trim();
    if (searchForm.locationKeyword && searchForm.locationKeyword.trim() !== '') params.location = searchForm.locationKeyword.trim();

    if (searchForm.priceRange[0] > 0) params.minPrice = searchForm.priceRange[0];
    if (searchForm.priceRange[1] < 1000) params.maxPrice = searchForm.priceRange[1]; // 只有当上限不是1000时才传递，因为1000代表1000+ (即无上限)

    // 服务日期筛选 (提醒：后端 ServiceItemMapper.xml 当前没有处理这两个参数)
    if (searchForm.dates && searchForm.dates[0] && searchForm.dates[1]) {
      params.startDate = searchForm.dates[0];
      params.endDate = searchForm.dates[1];
    }

    if (searchForm.petTypes && searchForm.petTypes.length > 0) {
      params.petTypes = searchForm.petTypes.join(',');
    }

    if (searchForm.housing && searchForm.housing.length > 0) {
      params.housingConditions = searchForm.housing.join(',');
    }

    console.log("发送搜索参数到后端:", params);

    // 更新URL中的查询参数 (仅在用户主动搜索时，或初始加载时如果已有参数)
    const queryToUpdate: Record<string, any> = {};
    for (const key in params) {
        if (Object.prototype.hasOwnProperty.call(params, key) && params[key] !== undefined && String(params[key]).trim() !== '') {
            queryToUpdate[key] = params[key];
        }
    }
    // 避免在非用户搜索且无初始query时写入空query
    if (isUserSearch || Object.keys(route.query).length > 0 || Object.keys(queryToUpdate).length > 0) {
        if (JSON.stringify(route.query) !== JSON.stringify(queryToUpdate)) { // 仅当参数实际变化时才替换路由
            router.replace({ query: queryToUpdate });
        }
    }


    const res: BackendResult<ApiServiceType[]> = await getActiveServices(params);

    if (res.code === 200 && Array.isArray(res.data)) {
      serviceList.value = res.data;
      if (isUserSearch) { // 仅在用户主动搜索时给出反馈
        if (serviceList.value.length === 0) {
            ElMessage.warning('没有找到符合当前筛选条件的服务。');
        } else {
            ElMessage.success(`为您找到了 ${serviceList.value.length} 个相关服务。`);
        }
      }
    } else {
      // 如果 res.code 不是 200，或者 res.data 不是数组，都视为错误
      serviceList.value = []; // 清空列表
      throw new Error(res.message || '获取服务列表数据格式错误或未返回数据');
    }
  } catch (error: any) {
    console.error("获取服务列表失败:", error);
    loadError.value = true; // 设置错误状态
    errorMessage.value = error.message || '获取服务列表时发生未知网络错误，请稍后重试。';
    serviceList.value = []; // 清空列表
    if (isUserSearch) { // 用户主动搜索失败也给提示
        ElMessage.error(errorMessage.value);
    }
  } finally {
    loading.value = false;
    if (isUserSearch) submitLoading.value = false;
  }
};

const handleSearch = () => {
  fetchServices(true); // 标记为用户搜索
};

const resetFilters = () => {
  // 使用深拷贝来重置，或者逐个属性赋值
  Object.assign(searchForm, JSON.parse(JSON.stringify(initialSearchForm)));
  // 如果URL中还有参数，清空它们并重新获取无筛选的服务列表
  if (Object.keys(route.query).length > 0) {
      router.replace({ query: {} });
  }
  fetchServices(false); // 获取所有服务
  ElMessage.info('筛选条件已重置，显示所有可用服务。');
};

const goToReservation = (serviceId: number | undefined) => {
  if (serviceId === undefined) {
      ElMessage.error('服务ID无效，无法预约');
      return;
  }
  if (!userStore.token) {
      ElMessage.warning('请先登录后再进行预约！');
      router.push({ name: 'Login', query: { redirect: route.fullPath } });
      return;
  }
  router.push({ name: 'CreateReservation', params: { serviceId: String(serviceId) } });
};

const viewServiceDetail = (service: ApiServiceType) => {
  if (service && service.id !== undefined) {
    router.push({ name: 'ServiceDetail', params: { id: String(service.id) } });
  } else {
    ElMessage.error('无法查看详情，服务ID无效。');
  }
};

// computed property 不再需要，直接使用 serviceList.value
const filteredServiceList = computed(() => serviceList.value);


const hasRequirements = (service: ApiServiceType): boolean => {
  return !!(
    (service.requiredVaccinations && service.requiredVaccinations.trim() !== '') ||
    (service.requiresNeutered !== null && service.requiresNeutered !== undefined) ||
    (service.temperamentRequirements && service.temperamentRequirements.trim() !== '') ||
    (service.prohibitedBreeds && service.prohibitedBreeds.trim() !== '')
  );
};

</script>

<style scoped lang="scss">
/* 您的 SCSS 样式，保持不变 */
.service-search-page {
  background-color: #f4f6f8;
  min-height: 100vh;
}
.page-container {
  max-width: 1400px; /* 或根据您的布局调整 */
  margin: 0 auto;
  padding: 20px 15px;
}
.sidebar-aside {
  padding-right: 20px; /* 给侧边栏和主内容一些间距 */
  .filter-card {
    position: sticky; /* 让筛选器在滚动时固定 */
    top: 20px;      /* 固定在顶部以下20px */
    border: none;
    background-color: #ffffff;
    border-radius: 8px;
    box-shadow: 0 1px 3px rgba(0,0,0,0.05), 0 1px 2px rgba(0,0,0,0.03);

    .filter-header {
       display: flex;
       align-items: center;
       font-size: 1rem; // 16px
       font-weight: 600;
       color: #333;
       .el-icon { margin-right: 10px; font-size: 1.1rem; }
    }
    .el-form-item { margin-bottom: 18px; }
    .filter-tip { font-size: 0.75rem; color: #777; margin-top: 3px; line-height: 1.5;}
    .price-range-display { font-size: 0.8rem; color: #555; text-align: center; margin-top: -2px; }
    .el-divider { margin: 24px 0; }
    .el-button { transition: background-color 0.2s ease, border-color 0.2s ease; }
  }
}
.main-content {
  background-color: transparent; /* 主内容区域背景透明 */
  border-radius: 0;
  padding: 0; /* 移除内边距，让卡片自己处理 */
  box-shadow: none;
}
.results-header {
  margin-bottom: 18px;
  color: #555;
  font-size: 0.9rem;
  padding-left: 5px; /* 轻微调整，如果需要 */
}
.loading-state-main, .empty-state-main {
  padding: 50px 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

.service-list-grid-main {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); /* 响应式网格 */
  gap: 20px;
}
.service-card-main {
  background-color: #fff;
  border-radius: 8px;
  border: 1px solid #e9ecef; // 更柔和的边框
  transition: transform 0.2s ease-out, box-shadow 0.2s ease-out;
  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 6px 16px rgba(0,0,0,0.08); // 更明显的悬浮效果
  }

  .service-card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    .service-name {
      font-weight: 600;
      font-size: 1.05rem; // 略微调整
      color: #2c3e50; // 深蓝灰色
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      max-width: 70%; // 避免名称过长挤压价格
    }
    .service-price {
      font-size: 0.9rem;
      color: #dd6b20; // 橙色系，醒目
      font-weight: 600;
      white-space: nowrap;
    }
  }
  // 使用 :deep() 来穿透 Element Plus 组件的封装，修改内部样式
  :deep(.el-card__header) {
    padding: 16px; // Element Plus v2.3.x+ 默认就是 20px，可按需调整
    border-bottom: 1px solid #f0f2f5; // 确保分隔线颜色
  }
  :deep(.el-card__body) {
    padding: 16px;
  }
   .service-card-body {
     .provider-info-placeholder {
       display: flex;
       align-items: center;
       margin-bottom: 12px;
       padding-bottom: 10px;
       border-bottom: 1px solid #f0f2f5;
       color: #6c757d; // Boostrap text-muted color
       font-size: 0.8rem;
       .provider-name {
           font-weight: 500;
           color: #495057;
       }
     }
     .description {
       font-size: 0.85rem;
       color: #495057;
       line-height: 1.6;
       min-height: calc(1.6em * 2); // 至少两行的高度
       display: -webkit-box;
       -webkit-box-orient: vertical;
       -webkit-line-clamp: 2; // 最多显示2行
       overflow: hidden;
       text-overflow: ellipsis;
       margin-bottom: 10px;
     }
     .details {
       margin-bottom: 10px;
       .detail-tag { margin-right: 6px; margin-bottom: 4px; }
     }
     .requirements {
        margin-top: 8px;
        display: flex; // 让标签横向排列
        flex-wrap: wrap; // 允许换行
        gap: 6px; // 标签之间的间隙
        .requirement-tag {
            // margin: 3px 5px 3px 0; // 旧的 margin
            .el-icon { vertical-align: -2px; margin-right: 3px; }
        }
     }
  }
   :deep(.el-card__footer) {
     padding: 12px 16px;
     background-color: #fdfdfd; // 轻微的背景色
     border-top: 1px solid #f0f2f5;
   }
   .card-actions { text-align: right; }
}
</style>