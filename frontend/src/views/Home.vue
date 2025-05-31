<template>
  <div class="home-rover">
    <!-- 顶部横幅 -->
    <div class="banner">
      <h1>在您身边的宠物服务平台</h1>
      <p>为您的爱宠预订值得信赖的寄养、遛狗、日托等服务</p>
    </div>

    <!-- 服务搜索区块 -->
    <el-card class="search-card">
      <div class="search-form">
        <div class="pet-type">
          <span>我需要为我的：</span>
          <el-checkbox v-model="petTypes.dog">狗</el-checkbox>
          <el-checkbox v-model="petTypes.cat">猫</el-checkbox>
        </div>
        <div class="service-type">
          <span>服务类型：</span>
          <el-radio-group v-model="serviceType">
            <el-radio-button label="宠物寄养" />
            <el-radio-button label="宠物美容" />
            <el-radio-button label="宠物训练" />
            <el-radio-button label="宠物医疗" />
          </el-radio-group>
        </div>
        <div class="search-fields">
          <el-input v-model="location" placeholder="请输入地点或小区" class="location-input" />
          <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" class="date-picker" />
          <el-select v-model="dogSize" placeholder="宠物体型" class="size-select">
            <el-option label="小型犬/猫 (0-15斤)" value="small" />
            <el-option label="中型犬 (16-40斤)" value="medium" />
            <el-option label="大型犬 (41-100斤)" value="large" />
            <el-option label="巨型犬 (100斤以上)" value="giant" />
          </el-select>
          <el-button type="primary" class="search-btn" @click="handleSearchClick">搜索</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router';

const petTypes = reactive({ dog: true, cat: false })
const serviceType = ref('寄养')
const location = ref('')
const dateRange = ref([])
const dogSize = ref('')

const router = useRouter();

const handleSearchClick = () => {
  router.push({
    path: '/services/search',
    query: {
      
    }


  });
}

</script>

<style scoped lang="scss">
.home-rover {
  min-height: 100vh;
  background: #f7f8fa;
  padding: 0 0 40px 0;

  .banner {
    text-align: center;
    padding: 60px 0 30px 0;
    background: linear-gradient(90deg, #e0f7fa 0%, #fff 100%);
    h1 {
      font-size: 2.6em;
      color: #409eff;
      margin-bottom: 10px;
    }
    p {
      font-size: 1.3em;
      color: #666;
    }
  }

  .search-card {
    max-width: 900px;
    margin: -40px auto 30px auto;
    box-shadow: 0 2px 12px 0 rgba(0,0,0,0.08);
    border-radius: 12px;
    padding: 30px 40px;
    background: #fff;
    .search-form {
      display: flex;
      flex-direction: column;
      gap: 18px;
      .pet-type, .service-type {
        display: flex;
        align-items: center;
        gap: 10px;
        font-size: 1.1em;
      }
      .search-fields {
        display: flex;
        gap: 12px;
        align-items: center;
        .location-input {
          width: 200px;
        }
        .date-picker {
          width: 260px;
        }
        .size-select {
          width: 160px;
        }
        .search-btn {
          min-width: 100px;
        }
      }
    }
  }

  .promo-card {
    max-width: 900px;
    margin: 0 auto;
    border-radius: 12px;
    background: #fffbe6;
    .promo-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 1.1em;
      color: #b26d00;
      padding: 18px 24px;
      span:first-child {
        font-weight: bold;
      }
    }
  }
}
</style> 
