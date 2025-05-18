<template>
  <el-card class="filter-panel" shadow="never">
    <template #header>
      <div class="card-header">
        <span><el-icon><Filter /></el-icon> 搜索筛选</span>
      </div>
    </template>

    <el-form :model="filterData" label-position="top">
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="服务类型">
            <el-input
              v-model="filterData.serviceTypeKeyword"
              placeholder="输入服务类型关键词"
              clearable
            />
            <div class="form-item-subtitle">将模糊匹配服务名称或描述</div>
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="地点">
            <el-input
              v-model="filterData.locationKeyword"
              placeholder="输入城市、省份或地址关键字"
              clearable
            >
              <template #prepend>
                <el-icon><Location /></el-icon>
              </template>
            </el-input>
            <div class="form-item-subtitle">将匹配服务商地址信息</div>
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item :label="`价格范围 (每项服务): ￥${filterData.priceRange[0]} - ￥${filterData.priceRange[1]}${filterData.priceRange[1] === 1000 ? '+' : ''}`">
            <el-slider
              v-model="filterData.priceRange"
              range
              :min="0"
              :max="1000"
              :step="10"
              show-stops
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-divider content-position="center">以下筛选暂不可用</el-divider>

      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="服务日期">
            <el-date-picker
              v-model="filterData.serviceDateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              disabled
              style="width: 100%;"
            />
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="您的宠物类型">
            <el-checkbox-group v-model="filterData.petTypes" disabled>
              <el-checkbox label="dog">狗</el-checkbox>
              <el-checkbox label="cat">猫</el-checkbox>
              <el-checkbox label="other">其他</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="服务商住房条件">
            <el-checkbox-group v-model="filterData.providerHousingConditions" disabled>
              <el-checkbox label="independentHouse">有独立房屋</el-checkbox>
              <el-checkbox label="fencedYard">有围栏的院子</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item>
        <el-button type="primary" @click="handleApplyFilters" :icon="Search">应用筛选并搜索</el-button>
        <el-button @click="handleResetFilters" :icon="Refresh">重置筛选</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import { Filter, Location, Search, Refresh } from '@element-plus/icons-vue';
import { getActiveServices } from '@/api/service'; // 假设API路径正确
// import type { Service } from '@/api/service'; // 如果需要服务列表的类型

// 定义筛选数据结构
interface FilterData {
  serviceTypeKeyword: string;
  locationKeyword: string;
  priceRange: [number, number];
  serviceDateRange: [Date | null, Date | null] | null; // Element Plus daterange 可能返回 null
  petTypes: string[];
  providerHousingConditions: string[];
}

const initialFilterData: FilterData = {
  serviceTypeKeyword: '',
  locationKeyword: '',
  priceRange: [0, 500], // 初始价格范围，例如图片中的 ¥0-¥500
  serviceDateRange: null,
  petTypes: [],
  providerHousingConditions: [],
};

// 使用 reactive 使整个对象响应式，或使用 ref 为每个属性创建响应式引用
const filterData = reactive<FilterData>({ ...initialFilterData });

// 存储搜索结果 (示例)
// const searchResults = ref<Service[]>([]);
// const isLoading = ref(false);

const handleApplyFilters = async () => {
  ElMessage.info('应用筛选并搜索...');
  // isLoading.value = true;
  try {
    const params: Record<string, any> = {};

    if (filterData.serviceTypeKeyword) {
      params.keyword = filterData.serviceTypeKeyword; // 后端API需要支持此参数
    }
    if (filterData.locationKeyword) {
      params.location = filterData.locationKeyword; // 后端API需要支持此参数
    }

    // 价格范围处理
    // 假设价格单位是元，如果后端期望分，需要转换
    params.minPrice = filterData.priceRange[0];
    if (filterData.priceRange[1] < 1000) { // 1000 作为滑块的最大值
      params.maxPrice = filterData.priceRange[1];
    } // 如果 filterData.priceRange[1] === 1000，可以理解为1000及以上，此时不传maxPrice或传一个极大值

    // ---- 对于禁用的筛选条件，启用后才加入参数 ----
    // if (filterData.serviceDateRange && filterData.serviceDateRange[0] && filterData.serviceDateRange[1]) {
    //   params.startDate = filterData.serviceDateRange[0].toISOString().split('T')[0];
    //   params.endDate = filterData.serviceDateRange[1].toISOString().split('T')[0];
    // }
    // if (filterData.petTypes.length > 0) {
    //   params.petTypes = filterData.petTypes.join(','); // 或者根据后端API要求格式化
    // }
    // if (filterData.providerHousingConditions.length > 0) {
    //   params.housingConditions = filterData.providerHousingConditions.join(',');
    // }

    console.log('发送给API的筛选参数:', params);

    // 调用API
    // const response = await getActiveServices(params);
    // if (response.code === 200 && response.data) { // 假设您的API返回结构包含code和data
    //   searchResults.value = response.data;
    //   ElMessage.success(`找到了 ${response.data.length} 个服务`);
    // } else {
    //   ElMessage.error(response.message || '搜索服务失败');
    //   searchResults.value = [];
    // }
    ElMessage.warning('API调用部分已注释，请取消注释并根据您的后端调整。');

  } catch (error: any) {
    console.error('搜索服务时出错:', error);
    //ElMessage.error(error.message || '搜索服务时发生网络错误');
    // searchResults.value = [];
  } finally {
    // isLoading.value = false;
  }
};

const handleResetFilters = () => {
  Object.assign(filterData, initialFilterData); // 重置表单数据
  // searchResults.value = []; // 清空搜索结果
  ElMessage.success('筛选条件已重置');
  // 考虑是否在重置后自动执行一次不带参数的搜索
  // handleApplyFilters(); // 或者只清空，让用户手动再次搜索
};

</script>

<style scoped>
.filter-panel {
  max-width: 450px; /* 根据您的布局调整 */
  margin: 20px auto;
}
.card-header {
  display: flex;
  align-items: center;
  font-weight: bold;
}
.card-header .el-icon {
  margin-right: 8px;
}
.form-item-subtitle {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  margin-top: 4px;
}
.el-divider {
  margin-top: 30px;
  margin-bottom: 20px;
}
</style>/