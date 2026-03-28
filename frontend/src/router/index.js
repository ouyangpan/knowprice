import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue')
  },
  {
    path: '/hotel',
    name: 'Hotel',
    component: () => import('../views/HotelSetting.vue')
  },
  {
    path: '/filter',
    name: 'Filter',
    component: () => import('../views/FilterSetting.vue')
  },
  {
    path: '/report',
    name: 'Report',
    component: () => import('../views/ReportView.vue')
  },
  {
    path: '/llm',
    name: 'LLM',
    component: () => import('../views/LLMSettings.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router