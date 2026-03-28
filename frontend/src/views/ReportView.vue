<template>
  <div class="report-view-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>报告列表</span>
          <el-button type="primary" @click="triggerCollection" :loading="collecting">
            <el-icon><Refresh /></el-icon>
            立即采集
          </el-button>
        </div>
      </template>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        style="margin-bottom: 20px"
        @change="handleDateChange"
      />
      <el-table :data="reports" style="width: 100%" v-loading="loading">
        <el-table-column prop="reportDate" label="报告日期" width="180" />
        <el-table-column prop="hotelCount" label="监控酒店数" width="120">
          <template #default="scope">
            {{ scope.row.hotelCount || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="platformCount" label="平台数" width="120">
          <template #default="scope">
            {{ scope.row.platformCount || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="llmSuggestions" label="大模型建议" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.llmSuggestions" type="success">有</el-tag>
            <el-tag v-else type="info">无</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewReport(scope.row)">
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 报告详情对话框 -->
    <el-dialog
      v-model="reportDialogVisible"
      title="报告详情"
      width="90%"
      :close-on-click-modal="false"
    >
      <div class="report-detail" v-loading="loadingReport">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="价格汇总" name="summary">
            <el-table :data="reportData.priceSummary" style="width: 100%" class="mb-20">
              <el-table-column prop="platform" label="平台" width="120" />
              <el-table-column prop="avgPrice" label="平均价格" width="120">
                <template #default="scope">
                  {{ scope.row.avgPrice }} 元
                </template>
              </el-table-column>
              <el-table-column prop="minPrice" label="最低价格" width="120">
                <template #default="scope">
                  {{ scope.row.minPrice }} 元
                </template>
              </el-table-column>
              <el-table-column prop="maxPrice" label="最高价格" width="120">
                <template #default="scope">
                  {{ scope.row.maxPrice }} 元
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="价格趋势" name="trend">
            <div id="priceTrendChart" style="height: 400px"></div>
          </el-tab-pane>

          <el-tab-pane label="竞品对比" name="comparison">
            <el-table :data="reportData.competitorComparison" style="width: 100%" class="mb-20">
              <el-table-column prop="hotelName" label="酒店名称" width="200" />
              <el-table-column prop="platform" label="平台" width="100" />
              <el-table-column prop="roomType" label="房型" width="120" />
              <el-table-column prop="price" label="价格" width="100">
                <template #default="scope">
                  {{ scope.row.price }} 元
                </template>
              </el-table-column>
              <el-table-column prop="priceDiff" label="价格差异" width="120">
                <template #default="scope">
                  <span :class="scope.row.priceDiff > 0 ? 'text-red' : scope.row.priceDiff < 0 ? 'text-green' : ''">
                    {{ scope.row.priceDiff > 0 ? '+' : '' }}{{ scope.row.priceDiff }}%
                  </span>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane label="优化建议" name="suggestions">
            <div class="suggestions">
              <el-card shadow="hover" class="mb-10">
                <template #header>
                  <div class="suggestion-header">
                    <span>基于规则的建议</span>
                  </div>
                </template>
                <div class="suggestion-content">
                  {{ reportData.ruleSuggestions || '暂无建议' }}
                </div>
              </el-card>
              <el-card v-if="reportData.llmSuggestions" shadow="hover">
                <template #header>
                  <div class="suggestion-header">
                    <span>大模型智能建议</span>
                    <el-tag type="success" size="small">AI</el-tag>
                  </div>
                </template>
                <div class="suggestion-content">
                  {{ reportData.llmSuggestions }}
                </div>
              </el-card>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, nextTick } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import api from '../utils/api'

const loading = ref(false)
const loadingReport = ref(false)
const collecting = ref(false)
const reports = ref([])
const dateRange = ref([])
const reportDialogVisible = ref(false)
const activeTab = ref('summary')
let priceTrendChart = null

const reportData = reactive({
  priceSummary: [],
  priceTrend: [],
  competitorComparison: [],
  ruleSuggestions: '',
  llmSuggestions: ''
})

const loadReports = async () => {
  loading.value = true
  try {
    const params = {}
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0].toISOString().split('T')[0]
      params.endDate = dateRange.value[1].toISOString().split('T')[0]
    }
    
    const response = await api.getReports(params)
    if (response.code === 200) {
      reports.value = response.data || []
    }
  } catch (error) {
    console.error('加载报告列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleDateChange = () => {
  loadReports()
}

const viewReport = async (report) => {
  reportDialogVisible.value = true
  loadingReport.value = true
  
  try {
    const response = await api.getReportById(report.id)
    if (response.code === 200 && response.data) {
      const data = response.data
      
      try {
        const content = JSON.parse(data.content)
        reportData.priceSummary = content.priceSummary || []
        reportData.priceTrend = content.priceTrend || []
        reportData.competitorComparison = content.competitorComparison || []
        reportData.ruleSuggestions = content.ruleSuggestions || ''
      } catch (e) {
        console.error('解析报告内容失败:', e)
        reportData.priceSummary = []
        reportData.priceTrend = []
        reportData.competitorComparison = []
        reportData.ruleSuggestions = ''
      }
      
      reportData.llmSuggestions = data.llmSuggestions || ''
      
      await nextTick()
      if (activeTab.value === 'trend') {
        initPriceTrendChart()
      }
    }
  } catch (error) {
    console.error('加载报告详情失败:', error)
    ElMessage.error('加载报告详情失败')
  } finally {
    loadingReport.value = false
  }
}

const triggerCollection = async () => {
  collecting.value = true
  try {
    const response = await api.triggerCollection()
    if (response.code === 200) {
      ElMessage.success('采集任务已启动')
      loadReports()
    } else {
      ElMessage.error(response.message || '采集启动失败')
    }
  } catch (error) {
    console.error('触发采集失败:', error)
    ElMessage.error('采集启动失败')
  } finally {
    collecting.value = false
  }
}

const initPriceTrendChart = () => {
  const chartDom = document.getElementById('priceTrendChart')
  if (!chartDom) return
  
  if (priceTrendChart) {
    priceTrendChart.dispose()
  }
  
  priceTrendChart = echarts.init(chartDom)
  
  const platforms = {}
  const dates = []
  
  for (const trend of reportData.priceTrend) {
    dates.push(trend.date)
    if (trend.prices) {
      for (const [platform, price] of Object.entries(trend.prices)) {
        if (!platforms[platform]) {
          platforms[platform] = []
        }
        platforms[platform].push(price)
      }
    }
  }
  
  const series = Object.entries(platforms).map(([platform, data]) => ({
    name: platform,
    type: 'line',
    data: data
  }))
  
  const option = {
    title: {
      text: '价格趋势分析'
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: Object.keys(platforms)
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '{value} 元'
      }
    },
    series: series
  }
  
  priceTrendChart.setOption(option)
  
  window.addEventListener('resize', () => {
    priceTrendChart && priceTrendChart.resize()
  })
}

watch(activeTab, (newTab) => {
  if (newTab === 'trend') {
    nextTick(() => {
      initPriceTrendChart()
    })
  }
})

onMounted(() => {
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - 7)
  dateRange.value = [start, end]
  
  loadReports()
})
</script>

<style scoped>
.report-view-container {
  padding: 20px 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.report-detail {
  max-height: 70vh;
  overflow-y: auto;
}

.mb-10 {
  margin-bottom: 10px;
}

.mb-20 {
  margin-bottom: 20px;
}

.suggestion-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.suggestion-content {
  line-height: 1.6;
}

.text-red {
  color: #f56c6c;
}

.text-green {
  color: #67c23a;
}
</style>