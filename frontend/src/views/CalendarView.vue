<template>
  <div class="page">
    <h2 class="page-title">清算日历</h2>
    <p class="page-desc">
      维护清算假日：目标交割日为假日时轧差将被拦截并明确报错。
      {{ auth.isOperator ? '操作员可新增 / 删除假日。' : '当前为只读账号，仅可查看假日。' }}
    </p>

    <div class="calendar-grid">
      <div class="card-panel">
        <el-calendar v-model="calendarMonth">
          <template #date-cell="{ data }">
            <div
              class="cal-cell"
              :class="{ 'is-holiday': holidaySet.has(data.day.slice(0, 10)) }"
            >
              <span class="cal-num">{{ Number(data.day.slice(8, 10)) }}</span>
              <span v-if="holidaySet.has(data.day.slice(0, 10))" class="cal-mark">休</span>
            </div>
          </template>
        </el-calendar>
      </div>

      <div class="card-panel">
        <div class="toolbar" style="justify-content:space-between">
          <strong>假日列表（{{ holidays.length }}）</strong>
          <el-button @click="load">刷新</el-button>
        </div>

        <div v-if="auth.isOperator" class="add-row">
          <el-date-picker
            v-model="newDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择假日日期"
            style="width:170px"
          />
          <el-input
            v-model="newName"
            placeholder="假日名称（可选，如 国庆节）"
            style="width:220px"
            @keyup.enter="addHoliday"
          />
          <el-button type="primary" :loading="saving" @click="addHoliday">新增假日</el-button>
        </div>
        <el-alert
          v-else
          title="只读账号：不可新增或删除假日"
          type="info"
          :closable="false"
          show-icon
          style="margin:12px 0"
        />

        <el-table :data="holidays" v-loading="loading" stripe max-height="420">
          <el-table-column prop="holidayDate" label="日期" width="130">
            <template #default="{ row }">
              <el-tag type="danger" effect="plain">{{ row.holidayDate }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="名称" min-width="160" />
          <el-table-column label="星期" width="90">
            <template #default="{ row }">{{ weekdayOf(row.holidayDate) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button
                size="small"
                type="danger"
                :disabled="!auth.isOperator"
                @click="removeHoliday(row)"
              >删除</el-button>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="暂无假日，该月所有日期均可轧差" :image-size="60" />
          </template>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api/client'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const holidays = ref([])
const loading = ref(false)
const saving = ref(false)
const newDate = ref('')
const newName = ref('')
const calendarMonth = ref(new Date())

const holidaySet = computed(() => new Set(holidays.value.map((h) => h.holidayDate)))

const WEEKDAYS = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
function weekdayOf(isoDate) {
  return WEEKDAYS[new Date(isoDate + 'T00:00:00Z').getUTCDay()]
}

async function load() {
  loading.value = true
  try {
    const { data } = await api.get('/holidays')
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
  saving.value = true
  try {
    await api.post('/holidays', {
      holidayDate: newDate.value,
      name: newName.value.trim() || undefined
    })
    ElMessage.success(`已将 ${newDate.value} 标为清算假日`)
    newDate.value = ''
    newName.value = ''
    await load()
  } finally {
    saving.value = false
  }
}

async function removeHoliday(row) {
  try {
    await ElMessageBox.confirm(
      `确认删除假日 ${row.holidayDate}（${row.name}）？删除后该日可执行轧差。`,
      '删除假日',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }
    )
  } catch {
    return
  }
  await api.delete(`/holidays/${row.holidayDate}`)
  ElMessage.success('假日已删除')
  await load()
}

onMounted(load)
</script>

<style scoped>
.calendar-grid {
  display: grid;
  grid-template-columns: minmax(320px, 1fr) minmax(360px, 480px);
  gap: 16px;
  align-items: start;
}
@media (max-width: 960px) {
  .calendar-grid {
    grid-template-columns: 1fr;
  }
}
.add-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin: 12px 0;
}
.cal-cell {
  height: 100%;
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 4px 8px;
  box-sizing: border-box;
}
.cal-cell.is-holiday {
  background: #fef0f0;
  border-radius: 4px;
}
.cal-num {
  font-size: 13px;
}
.is-holiday .cal-num {
  color: #f56c6c;
  font-weight: 700;
}
.cal-mark {
  margin-top: auto;
  align-self: flex-end;
  font-size: 12px;
  color: #fff;
  background: #f56c6c;
  border-radius: 8px;
  padding: 0 6px;
}
</style>
