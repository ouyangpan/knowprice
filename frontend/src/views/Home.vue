<template>
  <div class="home-container">
    <el-card shadow="hover" class="welcome-card">
      <template #header>
        <div class="card-header">
          <span>欢迎使用酒店价格监控助手</span>
        </div>
      </template>
      <div class="welcome-content">
        <p>本系统帮助您实时监控不同平台上的酒店价格，分析市场趋势，优化定价策略。</p>
        <el-button type="primary" @click="navigateToHotelSetting">开始设置</el-button>
      </div>
    </el-card>

    <div class="stats-container">
      <el-card shadow="hover" class="stat-card">
        <div class="stat-item">
          <el-icon class="stat-icon"><DataAnalysis /></el-icon>
          <div class="stat-info">
            <div class="stat-value">0</div>
            <div class="stat-label">已监控酒店</div>
          </div>
        </div>
      </el-card>
      <el-card shadow="hover" class="stat-card">
        <div class="stat-item">
          <el-icon class="stat-icon"><Timer /></el-icon>
          <div class="stat-info">
            <div class="stat-value">0</div>
            <div class="stat-label">采集任务</div>
          </div>
        </div>
      </el-card>
      <el-card shadow="hover" class="stat-card">
        <div class="stat-item">
          <el-icon class="stat-icon"><Document /></el-icon>
          <div class="stat-info">
            <div class="stat-value">0</div>
            <div class="stat-label">生成报告</div>
          </div>
        </div>
      </el-card>
      <el-card shadow="hover" class="stat-card">
        <div class="stat-item">
          <el-icon class="stat-icon"><ChatLineRound /></el-icon>
          <div class="stat-info">
            <div class="stat-value">0</div>
            <div class="stat-label">智能建议</div>
          </div>
        </div>
      </el-card>
    </div>

    <el-card shadow="hover" class="quick-actions">
      <template #header>
        <div class="card-header">
          <span>快捷操作</span>
        </div>
      </template>
      <div class="action-buttons">
        <el-button type="success" @click="navigateToHotelSetting">
          <el-icon><House /></el-icon>
          酒店设置
        </el-button>
        <el-button type="warning" @click="navigateToFilterSetting">
          <el-icon><Filter /></el-icon>
          筛选配置
        </el-button>
        <el-button type="info" @click="navigateToReportView">
          <el-icon><Document /></el-icon>
          查看报告
        </el-button>
        <el-button type="primary" @click="navigateToLLMSettings">
          <el-icon><ChatLineRound /></el-icon>
          大模型设置
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { DataAnalysis, Timer, Document, ChatLineRound, House, Filter } from '@element-plus/icons-vue'

const router = useRouter()

const navigateToHotelSetting = () => {
  router.push('/hotel')
}

const navigateToFilterSetting = () => {
  router.push('/filter')
}

const navigateToReportView = () => {
  router.push('/report')
}

const navigateToLLMSettings = () => {
  router.push('/llm')
}
</script>

<style scoped>
.home-container {
  padding: 20px 0;
}

.welcome-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-content {
  padding: 20px 0;
}

.welcome-content p {
  margin-bottom: 20px;
  line-height: 1.6;
}

.stats-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-item {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 0 20px;
}

.stat-icon {
  font-size: 32px;
  color: #409EFF;
  margin-right: 20px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.quick-actions {
  margin-top: 20px;
}

.action-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.action-buttons .el-button {
  flex: 1;
  min-width: 120px;
}
</style>