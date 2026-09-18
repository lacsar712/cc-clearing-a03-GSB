<template>
  <div class="page">
    <h2 class="page-title">清算日历</h2>
    <p class="page-desc">维护清算假日；目标交割日为假日时轧差将被拒绝（只读账户仅可查看）</p>

    <div class="card-panel" v-if="auth.isOperator">
      <div class="toolbar" style="margin-bottom:0">
        <el-date-picker v-model="newDate" type="date" value-format="YYYY-MM-DD" placeholder="假日日期" />
        <el-input v-model="newName" style="width:240px" placeholder="假日名称，如 国庆节" @keyup.enter="addHoliday" />
        <el-button type="primary" :loading="saving" @click="addHoliday">添加假日</el-button>
      </div>
    </div>

    <div class="card-panel" style="margin-top:16px">
      <div class="toolbar" style="justify-content:space-between">
        <strong>假日列表（{{ holidays.length }}）</strong>
        <el-button @click="load">刷新</el-button>
      </div>
      <el-table :data="holidays" v-loading="loading" stripe>
        <el-table-column prop="date" label="日期" width="160">
          <template #default="{ row }"><span class="mono">{{ row.date }}</span></template>
        </el-table-column>
        <el-table-column prop="name" label="假日名称" min-width="200" />
        <el-table-column label="星期" width="100">
          <template #default="{ row }">{{ weekday(row.date) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button
              size="small"
              type="danger"
              :disabled="!auth.isOperator"
              @click="removeHoliday(row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && holidays.length === 0" description="暂无假日" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api/client'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const holidays = ref([])
const loading = ref(false)
const saving = ref(false)
const newDate = ref('')
const newName = ref('')

const WEEKDAYS = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

function weekday(date) {
  return WEEKDAYS[new Date(date + 'T00:00:00').getDay()]
}

async function load() {
  loading.value = true
  try {
    const { data } = await api.get('/clearing-calendar/holidays')
    holidays.value = data
  } finally {
    loading.value = false
  }
}

async function addHoliday() {
  if (!newDate.value) {
    ElMessage.warning('请选择假日日期')
    return
  }
  if (!newName.value.trim()) {
    ElMessage.warning('请输入假日名称')
    return
  }
  saving.value = true
  try {
    await api.post('/clearing-calendar/holidays', {
      date: newDate.value,
      name: newName.value.trim()
    })
    ElMessage.success('假日已添加')
    newDate.value = ''
    newName.value = ''
    await load()
  } finally {
    saving.value = false
  }
}

async function removeHoliday(row) {
  try {
    await ElMessageBox.confirm(`确认删除假日 ${row.date}（${row.name}）？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await api.delete(`/clearing-calendar/holidays/${row.date}`)
  ElMessage.success('假日已删除')
  await load()
}

onMounted(load)
</script>
