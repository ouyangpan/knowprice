<template>
  <div class="filter-setting-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>筛选条件配置</span>
        </div>
      </template>
      <el-form :model="filterForm" label-width="120px" v-loading="loading">
        <el-form-item label="目标地区">
          <el-input v-model="filterForm.region" placeholder="请输入目标地区，如：杭州市西湖区" />
        </el-form-item>
        <el-form-item label="酒店星级">
          <el-checkbox-group v-model="filterForm.starRatings">
            <el-checkbox :label="2">二星级</el-checkbox>
            <el-checkbox :label="3">三星级</el-checkbox>
            <el-checkbox :label="4">四星级</el-checkbox>
            <el-checkbox :label="5">五星级</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="酒店品牌">
          <el-input v-model="filterForm.brand" placeholder="请输入品牌名称，多个用逗号分隔" />
        </el-form-item>
        <el-form-item label="价格区间">
          <el-input v-model="filterForm.priceRange" placeholder="请输入价格区间，如：200-500" />
        </el-form-item>
        <el-form-item label="定时采集时间">
          <el-time-picker
            v-model="filterForm.scheduleTime"
            format="HH:mm"
            placeholder="选择定时采集时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="启用采集">
          <el-switch v-model="filterForm.isActive" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveFilterConfig" :loading="saving">保存配置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover" class="mt-20">
      <template #header>
        <div class="card-header">
          <span>高级筛选</span>
        </div>
      </template>
      <el-form :model="advancedFilter" label-width="120px">
        <el-form-item label="房间类型">
          <el-input v-model="advancedFilter.roomType" placeholder="请输入房间类型" />
        </el-form-item>
        <el-form-item label="设施服务">
          <el-checkbox-group v-model="advancedFilter.amenities">
            <el-checkbox label="WiFi">WiFi</el-checkbox>
            <el-checkbox label="空调">空调</el-checkbox>
            <el-checkbox label="早餐">早餐</el-checkbox>
            <el-checkbox label="停车场">停车场</el-checkbox>
            <el-checkbox label="健身房">健身房</el-checkbox>
            <el-checkbox label="游泳池">游泳池</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveAdvancedFilter" :loading="savingAdvanced">保存高级配置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../utils/api'

const loading = ref(false)
const saving = ref(false)
const savingAdvanced = ref(false)

const filterForm = reactive({
  region: '',
  starRatings: [],
  brand: '',
  priceRange: '',
  scheduleTime: null,
  isActive: true
})

const advancedFilter = reactive({
  roomType: '',
  amenities: []
})

const loadConfig = async () => {
  loading.value = true
  try {
    const response = await api.getCollectionConfig()
    if (response.code === 200 && response.data) {
      const config = response.data
      filterForm.region = config.region || '杭州市西湖区'
      filterForm.starRatings = config.starRatings ? JSON.parse(config.starRatings) : [4, 5]
      filterForm.brand = config.brands ? JSON.parse(config.brands).join(', ') : ''
      filterForm.priceRange = config.priceRange || ''
      filterForm.isActive = config.isActive !== false
      
      if (config.scheduleTime) {
        const [hours, minutes] = config.scheduleTime.split(':')
        filterForm.scheduleTime = new Date(2026, 0, 1, hours, minutes)
      }
    }
  } catch (error) {
    console.error('加载配置失败:', error)
  } finally {
    loading.value = false
  }
}

const saveFilterConfig = async () => {
  saving.value = true
  try {
    const configData = {
      region: filterForm.region,
      starRatings: JSON.stringify(filterForm.starRatings),
      brands: JSON.stringify(filterForm.brand.split(',').map(b => b.trim()).filter(b => b)),
      priceRange: filterForm.priceRange,
      scheduleTime: filterForm.scheduleTime 
        ? `${filterForm.scheduleTime.getHours()}:${filterForm.scheduleTime.getMinutes()}:00`
        : '02:00:00',
      isActive: filterForm.isActive
    }
    
    const response = await api.saveCollectionConfig(configData)
    if (response.code === 200) {
      ElMessage.success('筛选配置保存成功')
    } else {
      ElMessage.error(response.message || '保存失败')
    }
  } catch (error) {
    console.error('保存配置失败:', error)
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

const saveAdvancedFilter = async () => {
  savingAdvanced.value = true
  try {
    ElMessage.success('高级筛选配置保存成功（演示模式）')
  } catch (error) {
    console.error('保存高级配置失败:', error)
    ElMessage.error('保存失败，请重试')
  } finally {
    savingAdvanced.value = false
  }
}

onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
.filter-setting-container {
  padding: 20px 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.mt-20 {
  margin-top: 20px;
}
</style>