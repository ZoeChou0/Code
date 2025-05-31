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

            <el-form-item label="服务类型/关键词">
              <el-select
                v-model="searchForm.serviceTypeKeyword"
                placeholder="选择服务类型或输入关键词"
                clearable
                filterable
                allow-create
                default-first-option
                style="width: 100%;"
                @change="handleSearch" 
              >
                <el-option label="宠物美容" value="宠物美容"></el-option>
                <el-option label="宠物寄养" value="宠物寄养"></el-option>
                <el-option label="宠物医疗" value="宠物医疗"></el-option>
                <el-option label="宠物训练" value="宠物训练"></el-option>
                </el-select>
              <p class="filter-tip">选择类型进行精确匹配，输入自定义词则模糊匹配。</p>
            </el-form-item>

            <el-form-item label="城市">
              <el-select
                v-model="searchForm.selectedCity"
                placeholder="选择城市"
                clearable
                filterable
                style="width: 100%;"
                @change="handleSearch"
              >
                <el-option v-for="city in cities" :key="city.value" :label="city.label" :value="city.value" />
              </el-select>
            </el-form-item>

            <el-form-item label="价格范围 (每项服务)">
               <el-slider v-model="searchForm.priceRange" range :max="1000" :step="10" :marks="{0:'¥0', 500:'¥500', 1000:'¥1000+'}" @change="handleSearch"/>
               <div class="price-range-display">
                 ¥{{ searchForm.priceRange[0] }} - ¥{{ searchForm.priceRange[1] >= 1000 ? '1000+' : searchForm.priceRange[1] }}
               </div>
            </el-form-item>

            <el-form-item label="排序方式">
              <div style="display: flex; width: 100%; gap: 10px;">
                <el-select v-model="searchForm.sortBy" placeholder="排序字段" clearable @change="handleSearch" style="flex-grow: 1;">
                  <el-option label="默认排序" value=""></el-option>
                  <el-option label="按价格" value="price"></el-option>
                  <el-option label="按评分" value="rating"></el-option>
                </el-select>
                <el-select v-model="searchForm.sortOrder" placeholder="顺序" :disabled="!searchForm.sortBy" @change="handleSearch" style="width: 90px;">
                  <el-option label="降序" value="desc"></el-option>
                  <el-option label="升序" value="asc"></el-option>
                </el-select>
              </div>
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
                @change="handleSearch"
              />
               <p class="filter-tip">提示: 此筛选依赖后端支持服务可用日期查询。</p>
            </el-form-item>

            <!-- <el-form-item label="您的宠物类型 (用于不接受品种筛选)">
               <el-checkbox-group v-model="searchForm.petTypes" @change="handleSearch">
                  <el-checkbox label="Dog">狗</el-checkbox>
                  <el-checkbox label="Cat">猫</el-checkbox>
               </el-checkbox-group>
                <p class="filter-tip">筛选出不禁止您宠物类型的服务。</p>
            </el-form-item> -->

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
                {{ serviceList.length > 0 ? `根据您的筛选，找到 ${serviceList.length} 个相关服务` : '当前条件下未找到匹配的服务' }}
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
        <div v-else-if="serviceList.length === 0" class="empty-state-main">
          <el-empty description="没有找到符合您筛选条件的服务。请尝试调整筛选条件或重置所有筛选。"></el-empty>
        </div>
        <div v-else class="service-list-grid-main">
          <el-card v-for="service in serviceList" :key="service.id" shadow="hover" class="service-card-main">
            <template #header>
                <div class="service-card-header">
                  <span class="service-name" :title="service.name">{{ service.name }}</span>
                  <span class="service-price">¥{{ service.price?.toFixed(2) ?? '待议' }} / {{ formatDuration(service.duration) }}</span>
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
import { ref, reactive, onMounted, watch } from 'vue';
import {
  ElContainer, ElAside, ElMain, ElCard, ElForm, ElFormItem, ElInput, ElSelect, ElOption, ElDatePicker, ElSlider,
  ElCheckboxGroup, ElCheckbox, ElButton, ElTag, ElIcon, ElEmpty, ElSkeleton, ElAlert, ElMessage,
  ElAvatar, ElDivider, ElTooltip
} from 'element-plus';
import {
    Filter as FilterIcon,
    Search as SearchIcon,
    LocationInformation,
    Link,
    User as ElUser,
    FirstAidKit,
    Refresh,
    ChatDotRound,
    CircleClose
} from '@element-plus/icons-vue';
import { getActiveServices } from '@/api/service';
import type { Service as ApiServiceType } from '@/api/service';
import type { BackendResult } from '@/types/api';
import { useUserStore } from '@/stores/user';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const loading = ref(true);
const loadError = ref(false);
const errorMessage = ref('');
const serviceList = ref<ApiServiceType[]>([]);
const submitLoading = ref(false);

const cities = ref([
  { value: '北京', label: '北京' },
  { value: '上海', label: '上海' },
  { value: '广州', label: '广州' },
  { value: '深圳', label: '深圳' },
  { value: '杭州', label: '杭州' },
  { value: '成都', label: '成都' },
  { value: '重庆', label: '重庆' },
  { value: '武汉', label: '武汉' },
  { value: '西安', label: '西安' },
  { value: '南京', label: '南京' },
]);

const predefinedServiceTypes = ['宠物美容', '宠物寄养', '宠物医疗', '宠物训练'];

const initialSearchForm = {
  serviceTypeKeyword: '',
  selectedCity: '',
  locationKeyword: '',
  dates: undefined as [string, string] | undefined,
  priceRange: [0, 1000] as [number, number],
  petTypes: [] as string[],
  housing: [] as string[],
  sortBy: '',
  sortOrder: 'desc',
};
const searchForm = reactive({ ...initialSearchForm });

const disabledDate = (time: Date) => {
  return time.getTime() < Date.now() - 8.64e7;
};

watch(() => route.query, (newQuery, oldQuery) => {
    // 仅当查询参数实际改变且当前不处于加载中时才处理
    if (JSON.stringify(newQuery) === JSON.stringify(oldQuery) || loading.value) {
        return;
    }
    initializeFormFromRoute();
    // 仅当路由中有实际查询参数，或旧查询参数也非空时，才因路由变化自动获取数据
    // 避免从有参数导航到无参数（如重置）时，watch又错误地触发一次fetch
    if (Object.keys(newQuery).length > 0 || Object.keys(oldQuery).length > 0) {
        fetchServices(false); // 标记为非用户主动搜索
    }
}, { deep: true });


const initializeFormFromRoute = () => {
    const query = route.query;
    let formChanged = false;

    const updateField = (formKey: keyof typeof searchForm, queryKey: string, defaultValue: any, isArray = false, isNumberPair = false) => {
        const queryValue = query[queryKey];
        let newValue = defaultValue;

        if (isArray) {
            newValue = queryValue ? (queryValue as string).split(',') : [...defaultValue];
            if (JSON.stringify(searchForm[formKey]) !== JSON.stringify(newValue)) {
                (searchForm[formKey] as any) = newValue;
                formChanged = true;
            }
        } else if (isNumberPair && queryKey === 'priceRange') { // 特殊处理价格范围
            const qMin = parseInt(query.minPrice as string);
            const qMax = parseInt(query.maxPrice as string);
            const currentVal = searchForm[formKey] as [number, number];
            const newMin = !isNaN(qMin) ? qMin : defaultValue[0];
            const newMax = !isNaN(qMax) && qMax <= 1000 ? qMax : defaultValue[1];
            if (currentVal[0] !== newMin || currentVal[1] !== newMax) {
                 (searchForm[formKey] as [number, number]) = [newMin, newMax];
                 formChanged = true;
            }
        } else if(queryKey === 'serviceTypeKeyword') { // 特殊处理 serviceTypeKeyword
            if (query.category) {
                newValue = query.category as string;
            } else if (query.keyword) {
                newValue = query.keyword as string;
            } else {
                newValue = defaultValue;
            }
            if (searchForm[formKey] !== newValue) {
                (searchForm[formKey] as string) = newValue;
                formChanged = true;
            }
        }
        else {
            newValue = (queryValue as string) || defaultValue;
             if (searchForm[formKey] !== newValue) {
                (searchForm[formKey] as string) = newValue;
                formChanged = true;
            }
        }
    };

    updateField('serviceTypeKeyword', 'serviceTypeKeyword', initialSearchForm.serviceTypeKeyword, false, false); // 使用自身作为key
    updateField('selectedCity', 'city', initialSearchForm.selectedCity);
    updateField('locationKeyword', 'location', initialSearchForm.locationKeyword);
    updateField('sortBy', 'sortBy', initialSearchForm.sortBy);
    updateField('sortOrder', 'sortOrder', initialSearchForm.sortOrder);
    updateField('priceRange', 'priceRange', initialSearchForm.priceRange, false, true); // 触发minPrice/maxPrice读取

    if (query.startDate && query.endDate) {
        const newDates: [string, string] = [query.startDate as string, query.endDate as string];
        if(JSON.stringify(searchForm.dates) !== JSON.stringify(newDates)) {
            searchForm.dates = newDates;
            formChanged = true;
        }
    } else if (searchForm.dates !== undefined) {
        searchForm.dates = undefined;
        formChanged = true;
    }
    updateField('petTypes', 'petTypes', initialSearchForm.petTypes, true);
    updateField('housing', 'housingConditions', initialSearchForm.housing, true);

    return formChanged; // 返回表单是否有变化
};

onMounted(() => {
  initializeFormFromRoute();
  fetchServices(false); // 初始加载，非用户搜索
});

const fetchServices = async (isUserSearch: boolean = false) => {
  loading.value = true;
  if (isUserSearch) submitLoading.value = true;
  loadError.value = false;
  errorMessage.value = '';

  try {
    const params: Record<string, any> = {};

    // 服务类型/关键词 处理逻辑
    if (searchForm.serviceTypeKeyword && searchForm.serviceTypeKeyword.trim() !== '') {
      const value = searchForm.serviceTypeKeyword.trim();
      if (predefinedServiceTypes.includes(value)) {
        params.category = value; // 如果是预设类型，作为 category 发送
      } else {
        params.keyword = value; // 否则，作为 keyword 发送
      }
    }

    if (searchForm.selectedCity && searchForm.selectedCity.trim() !== '') params.city = searchForm.selectedCity.trim();
    if (searchForm.locationKeyword && searchForm.locationKeyword.trim() !== '') params.location = searchForm.locationKeyword.trim();

    if (searchForm.priceRange[0] > 0) params.minPrice = searchForm.priceRange[0];
    // 注意：如果价格上限是滑块最大值（例如1000代表1000+），则不应将此值作为maxPrice发送给后端（意味着无上限）
    if (searchForm.priceRange[1] < 1000) params.maxPrice = searchForm.priceRange[1];


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

    if (searchForm.sortBy) {
      params.sortBy = searchForm.sortBy;
      params.sortOrder = (searchForm.sortOrder === 'asc' || searchForm.sortOrder === 'desc') ? searchForm.sortOrder : 'desc';
    }

    console.log("发送到后端的参数 (fetchServices):", params);

    const queryToUpdate: Record<string, any> = {};
    // 根据 params 构建 queryToUpdate，确保只包含实际发送到API的有效参数
    if (params.category) queryToUpdate.category = params.category;
    else if (params.keyword) queryToUpdate.keyword = params.keyword; // 如果没有category，但有keyword

    if (params.city) queryToUpdate.city = params.city;
    if (params.location) queryToUpdate.location = params.location;
    // 对价格范围特殊处理，仅当它们不是初始值时才加入URL
    if (params.minPrice && params.minPrice !== initialSearchForm.priceRange[0]) queryToUpdate.minPrice = params.minPrice.toString();
    if (params.maxPrice && params.maxPrice !== initialSearchForm.priceRange[1]) queryToUpdate.maxPrice = params.maxPrice.toString();

    if (params.startDate && params.endDate) {
        queryToUpdate.startDate = params.startDate;
        queryToUpdate.endDate = params.endDate;
    }
    if (params.petTypes) queryToUpdate.petTypes = params.petTypes; // join 后的字符串
    if (params.housingConditions) queryToUpdate.housingConditions = params.housingConditions; // join 后的字符串
    if (params.sortBy) {
        queryToUpdate.sortBy = params.sortBy;
        queryToUpdate.sortOrder = params.sortOrder;
    }
    
    // 仅当生成的查询参数与当前路由查询参数不同时才更新路由
    if (JSON.stringify(route.query) !== JSON.stringify(queryToUpdate)) {
        router.replace({ query: queryToUpdate });
    }

    const res: BackendResult<ApiServiceType[]> = await getActiveServices(params);

    if (res.code === 200 && Array.isArray(res.data)) {
      serviceList.value = res.data;
      if (isUserSearch) { // 只有用户主动搜索才给提示
        ElMessage[serviceList.value.length > 0 ? 'success' : 'warning'](
          serviceList.value.length > 0
            ? `为您找到了 ${serviceList.value.length} 个相关服务。`
            : '没有找到符合当前筛选条件的服务。'
        );
      }
    } else {
      serviceList.value = [];
      throw new Error(res.message || '获取服务列表数据格式错误或未返回数据');
    }
  } catch (error: any) {
    console.error("获取服务列表失败:", error);
    loadError.value = true;
    errorMessage.value = error.message || '获取服务列表时发生未知网络错误，请稍后重试。';
    serviceList.value = [];
    if (isUserSearch) { // 用户主动搜索失败也给提示
        ElMessage.error(errorMessage.value);
    }
  } finally {
    loading.value = false;
    if (isUserSearch) submitLoading.value = false;
  }
};

const handleSearch = () => {
  fetchServices(true); // 标记为用户主动搜索
};

const resetFilters = () => {
  // 深拷贝 initialSearchForm 以避免直接修改
  Object.assign(searchForm, JSON.parse(JSON.stringify(initialSearchForm)));
  // 清空URL查询参数并触发搜索
  if (Object.keys(route.query).length > 0) {
      router.replace({ query: {} }); // 这会触发 watch, watch会调用 fetchServices(false)
  } else {
      fetchServices(false); // 如果query本来就空，直接获取所有
  }
  ElMessage.info('筛选条件已重置。');
};

watch(() => searchForm.sortBy, (newSortBy) => {
  if (!newSortBy) {
    searchForm.sortOrder = 'desc';
  }
  // 注意：由于所有筛选控件的 @change 都调用了 handleSearch，
  // 单独监听 sortBy 来触发搜索可能导致重复调用。
  // 如果确实需要在 sortBy 清空时自动刷新（且不是通过 resetFilters），
  // 需要更复杂的逻辑来避免重复搜索。目前由 @change="handleSearch" 统一处理。
});


// 时长格式化函数
const formatDuration = (minutes: number | undefined | null): string => {
  if (minutes == null || minutes <= 0) {
    return '次';
  }
  if (minutes < 60) {
    return `${minutes}分钟`;
  }
  const hours = Math.floor(minutes / 60);
  const remainingMinutes = minutes % 60;
  if (remainingMinutes === 0) {
    return `${hours}小时`;
  }
  return `${hours}小时${remainingMinutes}分钟`;
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
/* 样式保持不变 */
.service-search-page {
  background-color: #f4f6f8;
  min-height: 100vh;
}
.page-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px 15px;
}
.sidebar-aside {
  padding-right: 20px;
  .filter-card {
    position: sticky;
    top: 20px;
    border: none;
    background-color: #ffffff;
    border-radius: 8px;
    box-shadow: 0 1px 3px rgba(0,0,0,0.05), 0 1px 2px rgba(0,0,0,0.03);

    .filter-header {
       display: flex;
       align-items: center;
       font-size: 1rem;
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
  background-color: transparent;
  border-radius: 0;
  padding: 0;
  box-shadow: none;
}
.results-header {
  margin-bottom: 18px;
  color: #555;
  font-size: 0.9rem;
  padding-left: 5px;
}
.loading-state-main, .empty-state-main {
  padding: 50px 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

.service-list-grid-main {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}
.service-card-main {
  background-color: #fff;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  transition: transform 0.2s ease-out, box-shadow 0.2s ease-out;
  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 6px 16px rgba(0,0,0,0.08);
  }

  .service-card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    .service-name {
      font-weight: 600;
      font-size: 1.05rem;
      color: #2c3e50;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      max-width: 70%;
    }
    .service-price {
      font-size: 0.9rem;
      color: #dd6b20;
      font-weight: 600;
      white-space: nowrap;
    }
  }
  :deep(.el-card__header) {
    padding: 16px;
    border-bottom: 1px solid #f0f2f5;
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
       color: #6c757d;
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
       min-height: calc(1.6em * 2);
       display: -webkit-box;
       -webkit-box-orient: vertical;
       -webkit-line-clamp: 2;
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
        display: flex;
        flex-wrap: wrap;
        gap: 6px;
        .requirement-tag {
            .el-icon { vertical-align: -2px; margin-right: 3px; }
        }
     }
  }
   :deep(.el-card__footer) {
     padding: 12px 16px;
     background-color: #fdfdfd;
     border-top: 1px solid #f0f2f5;
   }
   .card-actions { text-align: right; }
}
</style>