import axios from 'axios'

const apiClient = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

apiClient.interceptors.request.use(
  config => {
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

apiClient.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

export default {
  // 酒店相关API
  getHotels() {
    return apiClient.get('/hotels')
  },
  
  getHotelById(id) {
    return apiClient.get(`/hotels/${id}`)
  },
  
  getOwnHotels() {
    return apiClient.get('/hotels/own')
  },
  
  createHotel(hotel) {
    return apiClient.post('/hotels', hotel)
  },
  
  updateHotel(id, hotel) {
    return apiClient.put(`/hotels/${id}`, hotel)
  },
  
  deleteHotel(id) {
    return apiClient.delete(`/hotels/${id}`)
  },
  
  getRoomTypes(hotelId) {
    return apiClient.get(`/hotels/${hotelId}/room-types`)
  },
  
  createRoomType(roomType) {
    return apiClient.post('/hotels/room-types', roomType)
  },
  
  updateRoomType(id, roomType) {
    return apiClient.put(`/hotels/room-types/${id}`, roomType)
  },
  
  deleteRoomType(id) {
    return apiClient.delete(`/hotels/room-types/${id}`)
  },

  // 采集相关API
  triggerCollection() {
    return apiClient.post('/collection/trigger')
  },
  
  triggerCollectionByPlatform(platform) {
    return apiClient.post(`/collection/trigger/${platform}`)
  },
  
  getPriceRecords(params) {
    return apiClient.get('/collection/records', { params })
  },
  
  getCollectionConfig() {
    return apiClient.get('/collection/config')
  },
  
  saveCollectionConfig(config) {
    return apiClient.post('/collection/config', config)
  },

  // 报告相关API
  getReports(params) {
    return apiClient.get('/reports', { params })
  },
  
  getReportById(id) {
    return apiClient.get(`/reports/${id}`)
  },
  
  getReportByDate(date) {
    return apiClient.get(`/reports/date/${date}`)
  },
  
  generateReport() {
    return apiClient.post('/reports/generate')
  },
  
  deleteReport(id) {
    return apiClient.delete(`/reports/${id}`)
  },

  // 大模型相关API
  getLLMConfig() {
    return apiClient.get('/llm/config')
  },
  
  saveLLMConfig(config) {
    return apiClient.post('/llm/config', config)
  },
  
  testLLMConnection() {
    return apiClient.post('/llm/test')
  },
  
  isLLMEnabled() {
    return apiClient.get('/llm/enabled')
  },
  
  getPromptTemplates() {
    return apiClient.get('/llm/templates')
  },
  
  savePromptTemplate(template) {
    return apiClient.post('/llm/templates', template)
  },
  
  deletePromptTemplate(id) {
    return apiClient.delete(`/llm/templates/${id}`)
  }
}