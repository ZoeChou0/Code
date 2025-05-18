<!-- <!-- <template>
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

             <el-form-item label="服务类型">
                <el-select v-model="searchForm.serviceTypeKeyword" placeholder="输入服务类型关键词" clearable filterable style="width: 100%;">
                   </el-select>
                 <p class="filter-tip">将模糊匹配服务名称或描述</p>
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
                 ¥{{ searchForm.priceRange[0] }} - ¥{{ searchForm.priceRange[1] === 1000 ? '1000+' : searchForm.priceRange[1] }}
               </div>
            </el-form-item>

            <el-divider>以下筛选暂不可用</el-divider>

            <el-form-item label="服务日期">
              <el-date-picker
                v-model="searchForm.dates"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                style="width: 100%;"
                value-format="YYYY-MM-DD"
                disabled 
              />
            </el-form-item>

            <el-form-item label="您的宠物类型">
               <el-checkbox-group v-model="searchForm.petTypes" disabled>
                  <el-checkbox label="Dog">狗</el-checkbox>
                  <el-checkbox label="Cat">猫</el-checkbox>
                  <el-checkbox label="Other">其他</el-checkbox>
               </el-checkbox-group>
            </el-form-item>

            <el-form-item label="服务商住房条件">
               <el-checkbox-group v-model="searchForm.housing" disabled>
                  <el-checkbox label="house">有独立房屋</el-checkbox>
                  <el-checkbox label="fenced_yard">有围栏的院子</el-checkbox>
               </el-checkbox-group>
            </el-form-item>

             <el-form-item>
               <el-button type="primary" @click="handleSearch" :icon="SearchIcon" style="width: 100%;" :loading="loading">
                 应用筛选并搜索
               </el-button>
               <el-button @click="resetFilters" style="width: 100%; margin-top: 10px; margin-left: 0;">
                 重置筛选
               </el-button>
             </el-form-item>

          </el-form>
        </el-card>
      </el-aside>

      <el-main class="main-content">
        <div class="results-header">
            <span v-if="!loading && !loadError">找到 {{ filteredServiceList.length }} 个相关服务</span>
            </div>

        <div v-if="loading" class="loading-state-main">
          <el-skeleton :rows="8" animated />
        </div>
        <el-alert
          v-else-if="loadError"
          title="加载服务列表失败"
          type="error"
          :closable="false"
          show-icon
        >
          {{ errorMessage || '无法加载服务信息，请稍后重试。' }}
          <el-button link type="primary" @click="() => fetchServices(true)" style="margin-left: 10px;">重试</el-button>
        </el-alert>
        <div v-else-if="filteredServiceList.length === 0" class="empty-state-main">
          <el-empty description="没有找到符合筛选条件的服务"></el-empty>
        </div>
        <div v-else class="service-list-grid-main">
          <el-card v-for="service in filteredServiceList" :key="service.id" shadow="hover" class="service-card-main">
            <template #header>
                <div class="service-card-header">
                  <span class="service-name">{{ service.name }}</span>
                  <span class="service-price">¥{{ service.price?.toFixed(2) ?? 'N/A' }} / {{ service.duration ? service.duration+'分钟' : '次' }}</span>
                </div>
             </template>
             <div class="service-card-body">
                <div class="provider-info-placeholder">
                    <el-avatar :size="40" :icon="ElUser" style="margin-right: 10px; flex-shrink: 0;"/>
                    <div style="flex-grow: 1;">
                       <div>服务商: ID {{ service.providerId }} (名称待加载)</div>
                       <div style="font-size: 12px; color: #909399;">地点待加载 | 评分待加载</div>
                    </div>
                </div>
                 <p class="description" v-if="service.description">{{ service.description }}</p>
                 <p class="description" v-else>暂无详细描述。</p>
                 <div class="details">
                    <span>容量: {{ service.dailyCapacity ?? '无限制' }}</span>
                 </div>
                 <div class="requirements" v-if="hasRequirements(service)">
                     <el-tooltip v-if="service.requiredVaccinations" content="要求的疫苗" placement="top">
                       <el-tag size="small" type="info" effect="light"><el-icon><Promotion /></el-icon>疫苗: {{ service.requiredVaccinations.substring(0,15) }}...</el-tag>
                     </el-tooltip>
                     <el-tooltip v-if="service.requiresNeutered != null" :content="service.requiresNeutered ? '要求已绝育' : '不要求绝育'" placement="top">
                       <el-tag :type="service.requiresNeutered ? 'warning' : 'success'" size="small" effect="light"><el-icon><Link /></el-icon>{{ service.requiresNeutered ? '需绝育' : '不限' }}</el-tag>
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
import { ref, reactive, onMounted, computed } from 'vue';

import {
  ElContainer, ElAside, ElMain, ElCard, ElForm, ElFormItem, ElInput, ElSelect, ElOption, ElDatePicker, ElSlider,
  ElCheckboxGroup, ElCheckbox, ElButton, ElTag, ElIcon, ElEmpty, ElSkeleton, ElAlert, ElMessage, ElRadioGroup, ElRadioButton, // 导入所需组件
  ElAvatar, ElDivider, ElTooltip, ElImage // 补充导入
} from 'element-plus';
import { Filter as FilterIcon, Search as SearchIcon, LocationInformation, Promotion, Link, User as ElUser, Picture, Comment, FirstAidKit, Dish, Memo, Edit } from '@element-plus/icons-vue'; // 导入图标
import { getActiveServices } from '@/api/service';
import type { Service } from '@/api/service';
import { useUserStore } from '@/stores/user';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore(); // 获取用户 Store



// --- 状态 ---
const loading = ref(true);
const loadError = ref(false);
const errorMessage = ref('');
const serviceList = ref<Service[]>([]); // 原始服务列表
// 对话框不再需要，移除 searchDialogVisible
// const searchDialogVisible = ref(false);
const submitLoading = ref(false); // 用于搜索按钮的 loading
const uploading = ref(false); // (如果侧边栏有上传功能)

// 搜索表单数据模型
const searchForm = reactive({
  serviceTypeKeyword: '', // 修改为关键词搜索
  locationKeyword: '', // 修改为关键词搜索
  dates: undefined as [string, string] | undefined,
  priceRange: [0, 1000] as [number, number],
  // 以下为暂不可用的筛选条件模型
  petTypes: [],
  housing: [],
});

const queryPetTypes = route.query.petTypes
  ? JSON.parse(route.query.petTypes as string)
  : { dog: false, cat: false };

const queryServiceType = route.query.serviceType as string ?? '';
const queryLocation = route.query.location as string ?? '';
const queryDateStart = route.query.dateStart as string ?? '';
const queryDateEnd = route.query.dateEnd as string ?? '';

searchForm.serviceTypeKeyword = queryServiceType;
searchForm.locationKeyword = queryLocation;
searchForm.dates = queryDateStart && queryDateEnd
  ? [queryDateStart, queryDateEnd]
  : undefined;


// --- 数据获取与处理 ---
onMounted(() => {
  fetchServices(); // 页面加载时获取初始列表
});

// 修改 fetchServices 以接收参数
const fetchServices = async (isSearch: boolean = false) => {
  loading.value = true;
  loadError.value = false;
  errorMessage.value = '';
  if (isSearch) {
     ElMessage.info('正在根据筛选条件搜索...');
  }
  try {
    // 准备传递给后端的参数
    const params = {
        serviceType: searchForm.serviceTypeKeyword || undefined, // 空字符串转为 undefined
        location: searchForm.locationKeyword || undefined,
        minPrice: searchForm.priceRange[0] > 0 ? searchForm.priceRange[0] : undefined, // 0 不作为下限
        maxPrice: searchForm.priceRange[1] < 1000 ? searchForm.priceRange[1] : undefined, // 1000 (最大值) 不作为上限
         // 日期、宠物类型等暂不传递
    };

    // 清理掉值为 undefined 的参数 (可选，取决于后端如何处理)
    Object.keys(params).forEach(key => {
        if (params[key as keyof typeof params] === undefined) {
            delete params[key as keyof typeof params];
        }
    });

    console.log("发送搜索参数:", params);

    // 调用 getActiveServices API 并传递参数
    const res = await getActiveServices(params);

    if (res.code === 200 && Array.isArray(res.data)) {
      serviceList.value = res.data;
      if (isSearch && serviceList.value.length === 0) {
          ElMessage.warning('没有找到符合当前筛选条件的服务。');
      } else if (isSearch) {
          ElMessage.success(`找到 ${serviceList.value.length} 个服务。`);
      }
    } else {
      throw new Error(res.message || '获取服务列表数据格式错误');
    }
  } catch (error: any) {
    console.error("获取服务列表失败:", error);
    loadError.value = true;
    errorMessage.value = error.message || '获取服务列表时发生未知错误';
    serviceList.value = [];
  } finally {
    loading.value = false;
  }
};

// --- 筛选处理 ---
const handleSearch = () => {
  fetchServices(true);
};

const resetFilters = () => {
   searchForm.serviceTypeKeyword = '';
   searchForm.locationKeyword = '';
   searchForm.dates = undefined;
   searchForm.priceRange = [0, 1000];
   searchForm.petTypes = [];
   searchForm.housing = [];
   // 重置后立即重新获取所有服务
   fetchServices();
   ElMessage.info('筛选条件已重置');
};

const goToReservation = (serviceId: number) => {
  // 跳转到预约表单路由，并将 serviceId 作为参数传递
  router.push({ name: 'CreateReservation', params: { serviceId } });
};


// --- 计算属性 ---
// 由于筛选已（部分）移至后端，filteredServiceList 现在可以直接返回 serviceList
// 如果需要纯前端筛选作为补充或临时方案，可以在这里实现
const filteredServiceList = computed(() => {
  return serviceList.value;
});

// --- 辅助函数 (与 ViewPet 类似) ---
const hasRequirements = (service: Service): boolean => {
  return !!(service.requiredVaccinations || service.requiresNeutered !== null && service.requiresNeutered !== undefined);
};

const formatAge = (years?: number, months?: number): string => {
  if (years === undefined && months === undefined) return '';
  const y = years ?? 0;
  const m = months ?? 0;
  if (y === 0 && m === 0) return '未知';
  if (y === 0) return `${m}个月`;
  if (m === 0) return `${y}岁`;
  return `${y}岁${m}个月`;
};

const formatGender = (gender?: 'Male' | 'Female'): string => {
  if (gender === 'Male') return '雄性';
  if (gender === 'Female') return '雌性';
  return '未知';
};

const formatSpecies = (species?: 'Dog' | 'Cat'): string => {
  if (species === 'Dog') return '狗';
  if (species === 'Cat') return '猫';
  return '其他';
};

const formatEnergyLevel = (level?: 'High' | 'Moderate' | 'Low'): string => {
  if (level === 'High') return '高能量';
  if (level === 'Moderate') return '中等能量';
  if (level === 'Low') return '低能量';
  return '未知';
};

const energyLevelTagType = (level?: 'High' | 'Moderate' | 'Low'): 'danger' | 'warning' | 'success' | 'info' => {
  if (level === 'High') return 'danger';
  if (level === 'Moderate') return 'warning';
  if (level === 'Low') return 'success';
  return 'info'; // 默认
};
// --- 导航 (占位) ---
const viewServiceDetail = (service: Service) => { /* ... */ };
const makeReservation = (service: Service) => { /* ... */ };

</script>

<style scoped lang="scss">
.service-search-page {
  background-color: #f9fafb;
}
.page-container {
  max-width: 1400px; // 页面总宽度
  margin: 0 auto;
  padding-top: 20px;
}
.sidebar-aside {
  padding-right: 15px; // 和主内容区间隔
  .filter-card {
    position: sticky;
    top: 20px; // 侧边栏固定
    .filter-header {
       display: flex;
       align-items: center;
       font-size: 16px;
       font-weight: bold;
       color: #303133;
       .el-icon { margin-right: 8px; }
    }
    .el-form-item { margin-bottom: 14px; } // 减小筛选表单项间距
    .filter-tip { font-size: 12px; color: #909399; margin-top: 2px; line-height: 1.4;}
    .price-range-display { font-size: 13px; color: #606266; text-align: center; margin-top: -5px; }
    .el-divider { margin: 18px 0; }
  }
}
.main-content {
  background-color: #fff; // 主内容区背景色
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.results-header {
  margin-bottom: 20px;
  color: #606266;
  font-size: 14px;
}
.loading-state-main, .empty-state-main { padding: 40px 20px; }

.service-list-grid-main {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); // 调整最小宽度
  gap: 20px;
}
.service-card-main {
  // 样式可以参考之前的 .service-card
  transition: box-shadow 0.3s ease;
  &:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.1); }

  .service-card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    .service-name { font-weight: bold; font-size: 16px; color: #303133; }
    .service-price { font-size: 14px; color: #e6a23c; font-weight: 500; }
  }
  .service-card-body {
     padding: 16px;
     .provider-info-placeholder { // 服务商信息占位样式
       display: flex;
       align-items: center;
       margin-bottom: 12px;
       padding-bottom: 12px;
       border-bottom: 1px solid #f0f0f0;
       color: #909399;
       font-size: 13px;
     }
     .description { /* ... */ min-height: 42px; -webkit-line-clamp: 2; }
     .details { /* ... */ }
     .requirements {
        margin-top: 10px;
        .el-tag { margin: 2px 4px 2px 0; }
        .el-icon { vertical-align: middle; margin-right: 2px;}
     }
  }
   .el-card__footer { padding: 12px 16px; }
   .card-actions { text-align: right; }
}

.text-muted { color: #909399; }
.small-text { font-size: 12px; margin-top: 4px; line-height: 1.4; }
.mb-4 { margin-bottom: 16px; }
.mt-6 { margin-top: 24px; }
</style> -->


<!-- <template>
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

            <el-form-item label="服务类型">
              <el-select
                v-model="searchForm.serviceTypeKeyword"
                placeholder="选择或输入服务类型"
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
              <p class="filter-tip">可选择类型或输入关键词模糊匹配</p>
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
                ¥{{ searchForm.priceRange[0] }} - ¥{{ searchForm.priceRange[1] === 1000 ? '1000+' : searchForm.priceRange[1] }}
              </div>
            </el-form-item>

            <el-divider>以下筛选暂不可用</el-divider>

            <el-form-item label="服务日期">
              <el-date-picker
                v-model="searchForm.dates"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                style="width: 100%;"
                value-format="YYYY-MM-DD"
                disabled
              />
            </el-form-item>

            <el-form-item label="您的宠物类型">
              <el-checkbox-group v-model="searchForm.petTypes" disabled>
                <el-checkbox label="Dog">狗</el-checkbox>
                <el-checkbox label="Cat">猫</el-checkbox>
                <el-checkbox label="Other">其他</el-checkbox>
              </el-checkbox-group>
            </el-form-item>

            <el-form-item label="服务商住房条件">
              <el-checkbox-group v-model="searchForm.housing" disabled>
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
            {{ filteredServiceList.length > 0 ? `找到 ${filteredServiceList.length} 个相关服务` : '当前条件下未找到服务' }}
          </span>
        </div>

        <div v-if="loading" class="loading-state-main">
          <el-skeleton :rows="8" animated />
        </div>
        <el-alert
          v-else-if="loadError"
          title="加载服务列表失败"
          type="error"
          :description="errorMessage || '无法加载服务信息，请稍后重试。'"
          :closable="false"
          show-icon
        >
          <template #default>
            <el-button link type="primary" @click="() => fetchServices(false)" style="margin-left: 0px; padding-top: 5px;">点此重试</el-button>
          </template>
        </el-alert>
        <div v-else-if="filteredServiceList.length === 0" class="empty-state-main">
          <el-empty description="没有找到符合筛选条件的服务。尝试调整筛选条件或重置。"></el-empty>
        </div>
        <div v-else class="service-list-grid-main">
          <el-card v-for="service in filteredServiceList" :key="service.id" shadow="hover" class="service-card-main">
            <template #header>
              <div class="service-card-header">
                <span class="service-name">{{ service.name }}</span>
                <span class="service-price">¥{{ service.price?.toFixed(2) ?? '待议' }} / {{ service.duration ? service.duration+'分钟' : '次' }}</span>
              </div>
            </template>
            <div class="service-card-body">
              <div class="provider-info-placeholder">
                <el-avatar :size="40" :icon="ElUser" style="margin-right: 10px; flex-shrink: 0;"/>
                <div style="flex-grow: 1;">
                  <div>服务商 ID: {{ service.providerId }}</div>
                  </div>
              </div>
              <p class="description" v-if="service.description">{{ service.description }}</p>
              <p class="description" v-else>暂无详细描述。</p>
              <div class="details">
                <el-tag size="small" effect="plain" type="info" class="detail-tag" v-if="service.dailyCapacity">每日容量: {{ service.dailyCapacity }}</el-tag>
                </div>
              <div class="requirements" v-if="hasRequirements(service)">
                <el-tooltip v-if="service.requiredVaccinations" :content="`要求疫苗: ${service.requiredVaccinations}`" placement="top">
                  <el-tag size="small" type="warning" effect="light" class="requirement-tag">
                    <el-icon><FirstAidKit /></el-icon>疫苗
                  </el-tag>
                </el-tooltip>
                <el-tooltip v-if="service.requiresNeutered != null" :content="service.requiresNeutered ? '要求宠物已绝育' : '不要求宠物绝育'" placement="top">
                  <el-tag :type="service.requiresNeutered ? 'danger' : 'success'" size="small" effect="light" class="requirement-tag">
                    <el-icon><Link /></el-icon>{{ service.requiresNeutered ? '需绝育' : '不限绝育' }}
                  </el-tag>
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
import { ref, reactive, onMounted, computed } from 'vue';
import {
  ElContainer, ElAside, ElMain, ElCard, ElForm, ElFormItem, ElInput, ElSelect, ElOption, ElDatePicker, ElSlider,
  ElCheckboxGroup, ElCheckbox, ElButton, ElTag, ElIcon, ElEmpty, ElSkeleton, ElAlert, ElMessage, ElRadioGroup, ElRadioButton,
  ElAvatar, ElDivider, ElTooltip, ElImage
} from 'element-plus';
import { 
    Filter as FilterIcon, 
    Search as SearchIcon, 
    LocationInformation, 
    Promotion, 
    Link, 
    User as ElUser, 
    Picture, 
    Comment, 
    FirstAidKit, // 确保 FirstAidKit 图标已导入
    Dish, 
    Memo, 
    Edit,
    Refresh // 确保 Refresh 图标已导入
} from '@element-plus/icons-vue';
import { getActiveServices, Service } from '@/api/service';
import type { BackendResult } from '@/types/api';  // 假设 BackendResult 也在 service.ts 或 types/api.ts 中定义
import { useUserStore } from '@/stores/user';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

// --- 状态 ---
const loading = ref(false); // 修改初始值为 false，通常在调用 API 前设为 true
const loadError = ref(false);
const errorMessage = ref('');
const serviceList = ref<Service[]>([]);
const submitLoading = ref(false); // 用于搜索按钮的 loading

// 搜索表单数据模型
const initialSearchForm = {
  serviceTypeKeyword: '',
  locationKeyword: '',
  dates: undefined as [string, string] | undefined, // 保持 YYYY-MM-DD 格式
  priceRange: [0, 1000] as [number, number],
  petTypes: [],
  housing: [],
};
const searchForm = reactive({ ...initialSearchForm });

// --- 初始化 ---
onMounted(() => {
  // 从路由查询参数初始化表单
  const query = route.query;
  searchForm.serviceTypeKeyword = (query.serviceType as string) || initialSearchForm.serviceTypeKeyword;
  searchForm.locationKeyword = (query.location as string) || initialSearchForm.locationKeyword;
  const queryMinPrice = parseInt(query.minPrice as string);
  const queryMaxPrice = parseInt(query.maxPrice as string);
  searchForm.priceRange = [
    !isNaN(queryMinPrice) ? queryMinPrice : initialSearchForm.priceRange[0],
    !isNaN(queryMaxPrice) ? queryMaxPrice : initialSearchForm.priceRange[1],
  ];
  // 日期等其他参数类似初始化
  if (query.dateStart && query.dateEnd) {
      searchForm.dates = [query.dateStart as string, query.dateEnd as string];
  }
  // ...

  fetchServices(false); // 页面加载时获取初始列表 (不带 "正在搜索"提示)
});

// --- 数据获取与处理 ---
const fetchServices = async (isUserSearch: boolean = false) => {
  loading.value = true;
  if (isUserSearch) submitLoading.value = true; // 仅在用户点击搜索时显示按钮loading
  loadError.value = false;
  errorMessage.value = '';

  if (isUserSearch) {
    ElMessage.info('正在根据筛选条件搜索...');
  }

  try {
    const params: Record<string, any> = {};
    if (searchForm.serviceTypeKeyword) params.keyword = searchForm.serviceTypeKeyword; // API可能用 'keyword' 或 'serviceType'
    if (searchForm.locationKeyword) params.location = searchForm.locationKeyword;
    
    if (searchForm.priceRange[0] > 0) params.minPrice = searchForm.priceRange[0];
    if (searchForm.priceRange[1] < 1000) params.maxPrice = searchForm.priceRange[1];
    
    // 对于禁用的筛选条件，未来启用时再加入参数
    // if (searchForm.dates && searchForm.dates[0] && searchForm.dates[1]) {
    //   params.startDate = searchForm.dates[0]; // 已经是 YYYY-MM-DD
    //   params.endDate = searchForm.dates[1];
    // }
    // if (searchForm.petTypes.length > 0) params.petTypes = searchForm.petTypes.join(',');
    // if (searchForm.housing.length > 0) params.housingConditions = searchForm.housing.join(',');


    console.log("发送搜索参数:", params);
    
    // 更新路由查询参数，以便刷新页面或分享链接时能保留筛选条件
    if (isUserSearch) {
        router.replace({ query: { ...route.query, ...params } });
    }

    const res: BackendResult<Service[]> = await getActiveServices(params);

    if (res.code === 200 && Array.isArray(res.data)) {
      serviceList.value = res.data;
      if (isUserSearch && serviceList.value.length === 0) {
        ElMessage.warning('没有找到符合当前筛选条件的服务。');
      } else if (isUserSearch && serviceList.value.length > 0) {
        ElMessage.success(`找到了 ${serviceList.value.length} 个服务。`);
      }
    } else {
      throw new Error(res.message || '获取服务列表数据格式错误或未返回数据');
    }
  } catch (error: any) {
    console.error("获取服务列表失败:", error);
    loadError.value = true;
    errorMessage.value = error.message || '获取服务列表时发生未知错误';
    serviceList.value = []; // 清空列表以避免显示旧数据
  } finally {
    loading.value = false;
    if (isUserSearch) submitLoading.value = false;
  }
};

// --- 筛选处理 ---
const handleSearch = () => {
  fetchServices(true); // 用户手动搜索
};

const resetFilters = () => {
  Object.assign(searchForm, initialSearchForm); // 重置表单数据
  router.replace({ query: {} }); // 清空路由查询参数
  fetchServices(false); // 重置后获取所有服务，不显示 "正在搜索"
  ElMessage.info('筛选条件已重置');
};

// --- 导航 ---
const goToReservation = (serviceId: number) => {
  if (!userStore.token) {
      ElMessage.warning('请先登录后再进行预约！');
      router.push({ name: 'Login', query: { redirect: route.fullPath } });
      return;
  }
  router.push({ name: 'CreateReservation', params: { serviceId: serviceId.toString() } });
};

const viewServiceDetail = (service: Service) => {
  // 实际项目中，您会有一个服务详情页的路由
  // router.push({ name: 'ServiceDetail', params: { id: service.id } });
  ElMessage.info(`查看服务详情: ${service.name} (ID: ${service.id}) - 导航功能待实现`);
};


// --- 计算属性 (当前直接返回 serviceList，因为后端筛选) ---
const filteredServiceList = computed(() => {
  return serviceList.value;
});

// --- 辅助函数 (部分已在模板中使用) ---
const hasRequirements = (service: Service): boolean => {
  return !!(service.requiredVaccinations || (service.requiresNeutered !== null && service.requiresNeutered !== undefined) );
};



</script>

<style scoped lang="scss">
.service-search-page {
  background-color: #f4f6f8; // 更柔和的背景色
  min-height: 100vh;
}
.page-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px 15px; // 统一内边距
}
.sidebar-aside {
  padding-right: 20px;
  .filter-card {
    position: sticky;
    top: 20px;
    border: none; // 去除卡片边框，更融入背景
    background-color: #ffffff; // 筛选区背景
    border-radius: 8px;
    box-shadow: 0 1px 3px rgba(0,0,0,0.05), 0 1px 2px rgba(0,0,0,0.03); // 微调阴影

    .filter-header {
       display: flex;
       align-items: center;
       font-size: 1rem; // 调整字号
       font-weight: 600; // 调整字重
       color: #333; // 深色文字
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
  background-color: transparent; // 主内容区背景透明，依赖父级背景色
  border-radius: 0; // 去除主内容区的圆角和阴影，让卡片自己显示
  padding: 0; // 主内容区不加内边距，由内部元素控制
  box-shadow: none;
}
.results-header {
  margin-bottom: 18px;
  color: #555;
  font-size: 0.9rem;
  padding-left: 5px; // 轻微左内边距
}
.loading-state-main, .empty-state-main { 
  padding: 50px 20px; 
  background-color: #fff; 
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

.service-list-grid-main {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); // 增加最小宽度
  gap: 20px;
}
.service-card-main {
  background-color: #fff;
  border-radius: 8px; // 卡片圆角
  border: 1px solid #e9ecef; // 更淡的边框
  transition: transform 0.2s ease-out, box-shadow 0.2s ease-out;
  &:hover { 
    transform: translateY(-3px);
    box-shadow: 0 6px 16px rgba(0,0,0,0.08); 
  }

  .service-card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 10px; // 头部内边距
    border-bottom: 1px solid #f0f2f5; // 分隔线

    .service-name { 
      font-weight: 600; 
      font-size: 1.05rem; 
      color: #2c3e50; 
      // white-space: nowrap;
      // overflow: hidden;
      // text-overflow: ellipsis;
    }
    .service-price { 
      font-size: 0.9rem; 
      color: #dd6b20; // 橙色系价格
      font-weight: 600; 
    }
  }
  // 确保 :deep() 用于穿透到 el-card 的内部结构
  :deep(.el-card__header) {
    padding: 16px; // 调整el-card头部内边距
    border-bottom: 1px solid #f0f2f5;
  }
  :deep(.el-card__body) {
    padding: 16px; // 调整el-card主体内边距
  }
   .service-card-body {
     // padding: 16px; // 已移至 :deep(.el-card__body)
     .provider-info-placeholder {
       display: flex;
       align-items: center;
       margin-bottom: 12px;
       padding-bottom: 10px;
       border-bottom: 1px solid #f0f2f5; // 统一分隔线
       color: #6c757d; // 调整颜色
       font-size: 0.8rem;
     }
     .description { 
       font-size: 0.85rem; 
       color: #495057; 
       line-height: 1.6;
       min-height: 40px; // 约两行
       display: -webkit-box;
       -webkit-box-orient: vertical;
       -webkit-line-clamp: 2; // 限制两行
       overflow: hidden;
       text-overflow: ellipsis;
       margin-bottom: 10px;
     }
     .details { 
       margin-bottom: 10px;
       .detail-tag { margin-right: 6px; }
     }
     .requirements {
        margin-top: 8px;
        .requirement-tag { margin: 3px 5px 3px 0; .el-icon { vertical-align: -2px; margin-right: 3px; } }
     }
  }
   :deep(.el-card__footer) { 
     padding: 12px 16px; 
     background-color: #fdfdfd; // 页脚背景色
     border-top: 1px solid #f0f2f5; // 页脚顶部边框
   }
   .card-actions { text-align: right; }
}

// 从 style.css 中移入的通用样式 (如果适用)
.text-muted { color: #6c757d; }
.small-text { font-size: 0.75rem; margin-top: 4px; line-height: 1.4; }
.mb-4 { margin-bottom: 1rem; } // 16px
.mt-6 { margin-top: 1.5rem; } // 24px
</style> --> 

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

            <el-form-item label="服务类型">
              <el-select
                v-model="searchForm.serviceTypeKeyword"
                placeholder="选择或输入服务类型"
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
              <p class="filter-tip">可选择类型或输入关键词模糊匹配</p>
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
                 ¥{{ searchForm.priceRange[0] }} - ¥{{ searchForm.priceRange[1] === 1000 ? '1000+' : searchForm.priceRange[1] }}
               </div>
            </el-form-item>

            <el-divider>详细筛选</el-divider> 

            <el-form-item label="服务日期">
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
            </el-form-item>

            <el-form-item label="您的宠物类型">
               <el-checkbox-group v-model="searchForm.petTypes">
                  <el-checkbox label="Dog">狗</el-checkbox>
                  <el-checkbox label="Cat">猫</el-checkbox>
                  <el-checkbox label="Other">其他</el-checkbox>
               </el-checkbox-group>
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
                {{ filteredServiceList.length > 0 ? `找到 ${filteredServiceList.length} 个相关服务` : '当前条件下未找到服务' }}
            </span>
        </div>

        <div v-if="loading" class="loading-state-main">
          <el-skeleton :rows="8" animated />
        </div>
        <el-alert
          v-else-if="loadError"
          title="加载服务列表失败"
          type="error"
          :description="errorMessage || '无法加载服务信息，请稍后重试。'"
          :closable="false"
          show-icon
        >
          <template #default>
            <el-button link type="primary" @click="() => fetchServices(false)" style="margin-left: 0px; padding-top: 5px;">点此重试</el-button>
          </template>
        </el-alert>
        <div v-else-if="filteredServiceList.length === 0" class="empty-state-main">
          <el-empty description="没有找到符合筛选条件的服务。尝试调整筛选条件或重置。"></el-empty>
        </div>
        <div v-else class="service-list-grid-main">
          <el-card v-for="service in filteredServiceList" :key="service.id" shadow="hover" class="service-card-main">
            <template #header>
                <div class="service-card-header">
                  <span class="service-name">{{ service.name }}</span>
                  <span class="service-price">¥{{ service.price?.toFixed(2) ?? '待议' }} / {{ service.duration ? service.duration+'分钟' : '次' }}</span>
                </div>
             </template>
             <div class="service-card-body">
                <div class="provider-info-placeholder">
                    <el-avatar :size="40" :icon="ElUser" style="margin-right: 10px; flex-shrink: 0;"/>
                    <div style="flex-grow: 1;">
                       <div>服务商 ID: {{ service.providerId }}</div>
                       <div style="font-size: 12px; color: #909399;">
                        地点: {{ service.providerCity || service.providerAddressLine1 || '未知' }} <span v-if="service.providerAverageRating != null"> | 评分: {{ service.providerAverageRating.toFixed(1) }} / 5.0</span> <span v-else> | 评分: 暂无</span> </div>
                    </div>
                </div>
                 <p class="description" v-if="service.description">{{ service.description }}</p>
                 <p class="description" v-else>暂无详细描述。</p>
                 <div class="details">
                    <el-tag size="small" effect="plain" type="info" class="detail-tag" v-if="service.dailyCapacity">每日容量: {{ service.dailyCapacity }}</el-tag>
                 </div>
                 <div class="requirements" v-if="hasRequirements(service)">
                     <el-tooltip v-if="service.requiredVaccinations" :content="`要求疫苗: ${service.requiredVaccinations}`" placement="top">
                       <el-tag size="small" type="warning" effect="light" class="requirement-tag"><el-icon><FirstAidKit /></el-icon>疫苗</el-tag>
                     </el-tooltip>
                     <el-tooltip v-if="service.requiresNeutered != null" :content="service.requiresNeutered ? '要求宠物已绝育' : '不要求宠物绝育'" placement="top">
                       <el-tag :type="service.requiresNeutered ? 'danger' : 'success'" size="small" effect="light" class="requirement-tag"><el-icon><Link /></el-icon>{{ service.requiresNeutered ? '需绝育' : '不限绝育' }}</el-tag>
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
import { ref, reactive, onMounted, computed } from 'vue';
import {
  ElContainer, ElAside, ElMain, ElCard, ElForm, ElFormItem, ElInput, ElSelect, ElOption, ElDatePicker, ElSlider,
  ElCheckboxGroup, ElCheckbox, ElButton, ElTag, ElIcon, ElEmpty, ElSkeleton, ElAlert, ElMessage, ElRadioGroup, ElRadioButton, 
  ElAvatar, ElDivider, ElTooltip, ElImage 
} from 'element-plus';
import { 
    Filter as FilterIcon, 
    Search as SearchIcon, 
    LocationInformation, 
    Promotion, 
    Link, 
    User as ElUser, 
    Picture, 
    Comment, 
    FirstAidKit, 
    Dish, 
    Memo, 
    Edit,
    Refresh 
} from '@element-plus/icons-vue';
import { getActiveServices } from '@/api/service';
import type { Service } from '@/api/service'; 
import type { BackendResult } from '@/types/api'; // Corrected import
import { useUserStore } from '@/stores/user';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const loading = ref(false); 
const loadError = ref(false);
const errorMessage = ref('');
const serviceList = ref<Service[]>([]);
const submitLoading = ref(false);

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
  return time.getTime() < Date.now() - 8.64e7; 
};

onMounted(() => {
  const query = route.query;
  searchForm.serviceTypeKeyword = (query.keyword as string) || initialSearchForm.serviceTypeKeyword; // Assuming backend uses 'keyword'
  searchForm.locationKeyword = (query.location as string) || initialSearchForm.locationKeyword;
  
  const queryMinPrice = parseInt(query.minPrice as string);
  const queryMaxPrice = parseInt(query.maxPrice as string);
  searchForm.priceRange = [
    !isNaN(queryMinPrice) ? queryMinPrice : initialSearchForm.priceRange[0],
    !isNaN(queryMaxPrice) ? queryMaxPrice : initialSearchForm.priceRange[1],
  ];
  
  if (query.startDate && query.endDate) {
      searchForm.dates = [query.startDate as string, query.endDate as string];
  }
  if (query.petTypes) searchForm.petTypes = (query.petTypes as string).split(',');
  if (query.housingConditions) searchForm.housing = (query.housingConditions as string).split(',');


  fetchServices(false);
});

const fetchServices = async (isUserSearch: boolean = false) => {
  loading.value = true;
  if (isUserSearch) submitLoading.value = true;
  loadError.value = false;
  errorMessage.value = '';

  if (isUserSearch) {
    ElMessage.info('正在根据筛选条件搜索...');
  }

  try {
    const params: Record<string, any> = {};
    if (searchForm.serviceTypeKeyword) params.keyword = searchForm.serviceTypeKeyword;
    if (searchForm.locationKeyword) params.location = searchForm.locationKeyword;
    
    if (searchForm.priceRange[0] > 0) params.minPrice = searchForm.priceRange[0];
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
    
    console.log("发送搜索参数:", params);
    
    const queryToUpdate: Record<string, any> = {};
    for (const key in params) {
        if (Object.prototype.hasOwnProperty.call(params, key) && params[key] !== undefined && params[key] !== '') {
            queryToUpdate[key] = params[key];
        }
    }
    
    if (isUserSearch || Object.keys(route.query).length > 0) { // Update route if it's a user search or if query params already exist (onMount)
        router.replace({ query: queryToUpdate });
    }


    const res: BackendResult<Service[]> = await getActiveServices(params);

    if (res.code === 200 && Array.isArray(res.data)) {
      serviceList.value = res.data;
      if (isUserSearch && serviceList.value.length === 0) {
        ElMessage.warning('没有找到符合当前筛选条件的服务。');
      } else if (isUserSearch && serviceList.value.length > 0) {
        ElMessage.success(`找到了 ${serviceList.value.length} 个服务。`);
      }
    } else {
      throw new Error(res.message || '获取服务列表数据格式错误或未返回数据');
    }
  } catch (error: any) {
    console.error("获取服务列表失败:", error);
    loadError.value = true;
    errorMessage.value = error.message || '获取服务列表时发生未知错误';
    serviceList.value = [];
  } finally {
    loading.value = false;
    if (isUserSearch) submitLoading.value = false;
  }
};

const handleSearch = () => {
  fetchServices(true);
};

const resetFilters = () => {
  Object.assign(searchForm, initialSearchForm);
  router.replace({ query: {} }); 
  fetchServices(false);
  ElMessage.info('筛选条件已重置');
};

const goToReservation = (serviceId: number) => {
  if (!userStore.token) {
      ElMessage.warning('请先登录后再进行预约！');
      router.push({ name: 'Login', query: { redirect: route.fullPath } });
      return;
  }
  router.push({ name: 'CreateReservation', params: { serviceId: serviceId.toString() } });
};

const viewServiceDetail = (service: Service) => {
  ElMessage.info(`查看服务详情: ${service.name} (ID: ${service.id}) - 导航功能待实现`);
};

const filteredServiceList = computed(() => {
  return serviceList.value;
});

const hasRequirements = (service: Service): boolean => {
  return !!(service.requiredVaccinations || (service.requiresNeutered !== null && service.requiresNeutered !== undefined) );
};

</script>

<style scoped lang="scss">
/* Your existing styles - make sure they are complete as in the previous example */
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
    // padding-bottom: 10px; // Removed as :deep(.el-card__header) handles padding
    // border-bottom: 1px solid #f0f2f5; // Removed as :deep(.el-card__header) handles border

    .service-name { 
      font-weight: 600; 
      font-size: 1.05rem; 
      color: #2c3e50; 
    }
    .service-price { 
      font-size: 0.9rem; 
      color: #dd6b20; 
      font-weight: 600; 
    }
  }
  :deep(.el-card__header) {
    padding: 16px; 
    border-bottom: 1px solid #f0f2f5; // ensure this is applied
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
     }
     .description { 
       font-size: 0.85rem; 
       color: #495057; 
       line-height: 1.6;
       min-height: 40px; 
       display: -webkit-box;
       -webkit-box-orient: vertical;
       -webkit-line-clamp: 2; 
       overflow: hidden;
       text-overflow: ellipsis;
       margin-bottom: 10px;
     }
     .details { 
       margin-bottom: 10px;
       .detail-tag { margin-right: 6px; }
     }
     .requirements {
        margin-top: 8px;
        .requirement-tag { margin: 3px 5px 3px 0; .el-icon { vertical-align: -2px; margin-right: 3px; } }
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