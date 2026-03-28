<template>
  <div class="app-container">
    <el-container>
      <el-header height="60px" class="app-header">
        <h1>酒店价格监控助手</h1>
      </el-header>
      <el-container>
        <el-aside width="200px" class="app-sidebar">
          <el-menu
            :default-active="activeMenu"
            class="sidebar-menu"
            router
          >
            <el-menu-item index="/">
              <el-icon><HomeFilled /></el-icon>
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="/hotel">
              <el-icon><House /></el-icon>
              <span>酒店设置</span>
            </el-menu-item>
            <el-menu-item index="/filter">
              <el-icon><Filter /></el-icon>
              <span>筛选配置</span>
            </el-menu-item>
            <el-menu-item index="/report">
              <el-icon><Document /></el-icon>
              <span>报告查看</span>
            </el-menu-item>
            <el-menu-item index="/llm">
              <el-icon><ChatLineRound /></el-icon>
              <span>大模型设置</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-main class="app-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { HomeFilled, House, Filter, Document, ChatLineRound } from '@element-plus/icons-vue'

const route = useRoute()
const activeMenu = computed(() => route.path || '/')
</script>

<style>
.app-container {
  height: 100vh;
  overflow: hidden;
}

.app-header {
  background-color: #409EFF;
  color: white;
  display: flex;
  align-items: center;
  padding-left: 20px;
}

.app-header h1 {
  margin: 0;
  font-size: 20px;
}

.app-sidebar {
  background-color: #f5f7fa;
  border-right: 1px solid #e4e7ed;
}

.sidebar-menu {
  height: 100%;
  border-right: none;
}

.app-main {
  padding: 20px;
  overflow-y: auto;
}
</style>