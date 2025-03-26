<template>
  <CommonMenu :dataList="navList" @check="handleCheck" navTitle="柜列电费统计">
    <template #NavInfo>
      <br/>    <br/> 
      <div class="nav_data">
        <!-- <div class="carousel-container"> -->
          <!-- <el-carousel :interval="2500" motion-blur height="150px" arrow="never" trigger="click">
            <el-carousel-item v-for="(item, index) in carouselItems" :key="index">
              <img width="auto" height="auto" :src="item.imgUrl" alt="" class="carousel-image" />
            </el-carousel-item>
          </el-carousel> -->
        <!-- </div>
        <div class="nav_content">
          <el-descriptions title="全部柜列新增电费记录" direction="vertical" :column="1" width="60px" border >
            <el-descriptions-item label="最近一天"><span >{{ lastDayTotalData }} 条</span></el-descriptions-item>
            <el-descriptions-item label="最近一周"><span >{{ lastWeekTotalData }} 条</span></el-descriptions-item>
            <el-descriptions-item label="最近一月" ><span >{{ lastMonthTotalData }} 条</span></el-descriptions-item>
          </el-descriptions>
        </div> -->
        
        
        <div class="descriptions-container" style="font-size: 14px;">
          <div class="description-item">
            <span class="label">最近一天 :</span>
            <span class="value">{{ lastDayTotalData }}条</span>
          </div>
          <div class="description-item">
            <span class="label">最近一周 :</span>
            <span class="value">{{ lastWeekTotalData }}条</span>
          </div>
          <div class="description-item">
            <span class="label">最近一月 :</span>
            <span class="value">{{ lastMonthTotalData }}条</span>
          </div>
          <div>
            <span>全部柜列新增电费统计</span>
            <div class="line" style="margin-top: 10px;"></div>
          </div>
        </div>
      </div>
    </template>
    <template #ActionBar>
      <el-form
        class="-mb-15px"
        :model="queryParams"
        ref="queryFormRef"
        :inline="true" 
        label-width="auto"
      >
        <el-form-item label="颗粒度" prop="granularity">
          <el-select
            v-model="queryParams.granularity"
            placeholder="请选择天/周/月"
            class="!w-120px" >
            <el-option label="天" value="day" />
            <el-option label="周" value="week" />
            <el-option label="月" value="month" />
          </el-select>
        </el-form-item>

        <el-form-item label="时间段" prop="timeRange">
          <el-date-picker
          value-format="YYYY-MM-DD"
          v-model="selectTimeRange"
          type="daterange"
          :shortcuts="shortcuts"
          range-separator="-"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          :disabled-date="disabledDate"
        />
        </el-form-item>

        <el-form-item >
          <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        </el-form-item>
        <el-form-item style="position: absolute; right: 0;">
          <el-button type="success" plain :loading="exportLoading" @click="handleExport">
            <Icon icon="ep:download" class="mr-5px" /> 导出
          </el-button>
        </el-form-item>
      </el-form>
    </template>
    <template #Content>
      <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true" :border="true">
        <!-- 添加行号列 -->
        <el-table-column label="序号" align="center" width="80px">
          <template #default="{ $index }">
            {{ $index + 1 + (queryParams.pageNo - 1) * queryParams.pageSize }}
          </template>
        </el-table-column>
        <!-- 遍历其他列 -->  
        <template v-for="column in tableColumns">
          <el-table-column :key="column.prop" :label="column.label" :align="column.align" :prop="column.prop" :formatter="column.formatter" :width="column.width" v-if="column.istrue">
            <template #default="{ row }" v-if="column.slot === 'actions' && queryParams.granularity == 'day'">
              <el-button link type="primary" v-if="row.bill_mode_real && row.bill_mode_real == 2" @click="showDetails(row.aisle_id, row.start_time, row.location, row.end_time)">分段计费</el-button>
              <div v-else>固定计费</div>
            </template>
          </el-table-column>
        </template>
        <!-- 超过一万条数据提示信息 -->
          <template v-if="shouldShowDataExceedMessage" #append>
            <tr>
              <td colspan="列数" style="text-align: center; padding: 12px 0;">
                <span style="margin:0 12px; color: red;">数据量过大，请筛选后查看更多数据。</span>
              </td>
            </tr>
          </template>
      </el-table>

      <!-- 分页 -->
      <Pagination
        :total="total"
        :page-size-arr="pageSizeArr"
        layout = "sizes, prev, pager, next, jumper"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"/>
      <div class="realTotal">共 {{ realTotel }} 条</div>

      <!-- 分段收费详情弹窗 -->
      <el-dialog v-model="dialogVisible" :title=dialogTitle width="600" draggable>
        <span style="font-size: 13px;">{{dialogTimeRange}}</span>
        <el-table :data="dialogTableData" border show-summary  :summary-method="getSummaries" style="width: 100%">
          <el-table-column prop="bill_period" label="时间段" />
          <el-table-column prop="eq_value" label="耗电量(kWh)" :formatter="formatEle" />
          <el-table-column prop="bill_value" label="电费(元)" :formatter="formatEle" />
        </el-table>
      </el-dialog>
    </template>
  </CommonMenu>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import download from '@/utils/download'
import { EnergyConsumptionApi } from '@/api/aisle/energyConsumption'
import { formatDate, endOfDay, convertDate, addTime, startOfDay } from '@/utils/formatTime'
import { IndexApi } from '@/api/aisle/aisleindex'
import { useMessage } from '@/hooks/web/useMessage'
import { ElMessage } from 'element-plus'

defineOptions({ name: 'BillStatistics' })

const navList = ref([]) as any // 左侧导航栏树结构列表
const lastDayTotalData = ref(0)
const lastWeekTotalData = ref(0)
const lastMonthTotalData = ref(0)
const dialogVisible = ref(false)
const dialogTableData = ref<Array<{}>>([]) as any; 
const dialogTitle = ref('')
const dialogTimeRange = ref('')
const message = useMessage() // 消息弹窗
const loading = ref(true)
const list = ref<Array<{}>>([]) as any; 
const total = ref(0)
const realTotel = ref(0) // 数据的真实总条数
const selectTimeRange = ref(undefined)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 15,
  granularity: 'day',
  timeRange: undefined as string[] | undefined,
  aisleIds: []
})
const pageSizeArr = ref([15, 30, 50, 100])
const queryFormRef = ref()
const exportLoading = ref(false)

// 时间段快捷选项
const shortcuts = [
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setDate(start.getDate() - 7)
      return [start, end]
    },
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setMonth(start.getMonth() - 1)
      return [start, end]
    },
  },
  {
    text: '最近六个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setMonth(start.getMonth() - 6)
      return [start, end]
    },
  },
]

watch(() => queryParams.granularity, () => {
  if (queryParams.granularity == 'day') {
    tableColumns.value = [
      { label: '位置', align: 'center', prop: 'location', istrue: true },
      { label: '日期', align: 'center', prop: 'start_time', formatter: formatTime, width: '200px', istrue: true },
      { label: '耗电量(kWh)', align: 'center', prop: 'eq_value', istrue: true, formatter: formatEle },
      { label: '电费(元)', align: 'center', prop: 'bill_value', istrue: true, formatter: formatBill },
      { label: '计费方式', align: 'center', slot: 'actions', istrue: true },
    ]
  } else {
    tableColumns.value = [
      { label: '位置', align: 'center', prop: 'location', istrue: true },
      { label: '开始日期', align: 'center', prop: 'start_time', formatter: formatTime, istrue: true },
      { label: '结束日期', align: 'center', prop: 'end_time', formatter: formatTime, istrue: true },
      { label: '耗电量(kWh)', align: 'center', prop: 'eq_value', istrue: true, formatter: formatEle },
      { label: '电费(元)', align: 'center', prop: 'bill_value', istrue: true, formatter: formatEle },
    ]
  }

  handleQuery();
})

const tableColumns = ref([
  { label: '位置', align: 'center', prop: 'location', istrue: true },
  { label: '日期', align: 'center', prop: 'start_time', formatter: formatTime, width: '200px', istrue: true },
  { label: '耗电量(kWh)', align: 'center', prop: 'eq_value', istrue: true, formatter: formatEle },
  { label: '电费(元)', align: 'center', prop: 'bill_value', istrue: true, formatter: formatBill },
  { label: '计费方式', align: 'center', slot: 'actions', istrue: true },
]) as any;

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    if (selectTimeRange.value != undefined) {
      // 格式化时间范围 加上23:59:59的时分秒 
      const selectedStartTime = formatDate(startOfDay(convertDate(selectTimeRange.value[0])))
      // 结束时间的天数多加一天 ，  一天的毫秒数
      const selectedEndTime = formatDate(endOfDay(convertDate(selectTimeRange.value[1])))
      queryParams.timeRange = [selectedStartTime, selectedEndTime];
    }

    const data = await EnergyConsumptionApi.getBillDataPage(queryParams)
    list.value = data.list
    realTotel.value = data.total
    if (data.total > 10000) {
      total.value = 10000
    } else {
      total.value = data.total
    }
  } finally {
    loading.value = false
  }
}

// 最后一页显示数据量过大的提示
const shouldShowDataExceedMessage = computed(() => {
  const lastPageNo = Math.ceil(total.value / queryParams.pageSize);
  return queryParams.pageNo === lastPageNo && total.value >= 10000;
});

// 格式化日期
function formatTime(_row: any, _column: any, cellValue: number): string {
  if (!cellValue) {
    return ''
  }

  return dayjs(cellValue).format('YYYY-MM-DD')
}

// 格式化电能列数据，保留1位小数
function formatEle(_row: any, _column: any, cellValue: number): string {
  return Number(cellValue).toFixed(1);
}

// 格式化电费列数据
function formatBill(_row: any, _column: any, cellValue: number): string {
  return Number(cellValue).toFixed(1);
}

// 禁选未来的日期
const disabledDate = (date) => {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  // 设置date的时间为0时0分0秒，以便与today进行比较
  date.setHours(0, 0, 0, 0);
  // 如果date在今天之后，则禁用
  return date > today;
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    queryParams.pageNo = 1
    exportLoading.value = true
    const axiosConfig = {
      timeout: 0 // 设置超时时间为0
    }
    const data = await EnergyConsumptionApi.exportBillPageData(queryParams, axiosConfig)
    await download.excel(data, '柜列电费统计.xlsx')
  } catch (error) {
    // 处理异常
    console.error('导出失败：', error)
  } finally {
    exportLoading.value = false
  }
}

/** 详情操作*/
const showDetails = async (aisleId: number, startTime: string, location: string, endTime: string) => {
  dialogTableData.value = []
  dialogTitle.value = ''
  dialogTimeRange.value = ''
  let params = {
    aisleId: aisleId,
    startTime: startTime
  }
  const data = await EnergyConsumptionApi.getSubBillDetails(params)
  dialogTableData.value = data.list
  dialogTitle.value = location
  dialogTimeRange.value = startTime + ' - ' + endTime
  dialogVisible.value = true
}

// 计算表格汇总
const getSummaries = (param) => {
  const { columns, data } = param;
  const sums: (string | number)[] = []; // 明确 sums 数组的类型
  columns.forEach((column, index) => {
    if (index === 0) {
      sums[index] = '合计';
      return;
    }
    const values = data.map(item => Number(item[column.property]));
    if (!values.every(value => isNaN(value))) {
      sums[index] = values.reduce((prev, curr) => {
        const value = Number(curr);
        if (!isNaN(value)) {
          return prev + curr;
        } else {
          return prev;
        }
      }, 0);
      // 保留两位小数
      sums[index] = sums[index].toFixed(1);
    } else {
      sums[index] = 'N/A';
    }
  });

  return sums;
}

// 导航栏选择后触发
const handleCheck = async (node) => {
  let arr = [] as any
  node.forEach(item => { 
    if (item.type == 2) {
      arr.push(item.id);
    }
  });
  // 没筛选到 不显示任何数据 参数传0 后端返回空
  if (arr.length == 0 && node.length != 0) {
    arr.push(0)
    ElMessage({
      message: '暂无数据',
      type: 'warning',
    });
  }
  queryParams.aisleIds = arr
  handleQuery()
}

// 接口获取机房导航列表
const getNavList = async () => {
  const res = await IndexApi.getAisleMenu()
  navList.value = res
}

// 获取导航的数据显示
const getNavNewData = async () => {
  const res = await EnergyConsumptionApi.getNavNewData({})
  lastDayTotalData.value = res.day
  lastWeekTotalData.value = res.week
  lastMonthTotalData.value = res.month
}

/** 初始化 **/
onMounted(() => {
  const now=new Date()
  const oneOfMonth=new Date(now.getFullYear(), now.getMonth(), 1,23,59,59)
  selectTimeRange.value=[dayjs(oneOfMonth).format("YYYY-MM-DD HH:mm:ss"),dayjs(now).format("YYYY-MM-DD HH:mm:ss")]
  getNavList()
  getNavNewData()
  getList();
});
</script>

<style scoped>
.realTotal{
  float: right;
  padding-top: 20px;
  padding-right: 20px;
  font-size: 14px;
  font-weight: 400; 
  color: #606266
}
.nav_data{
  padding-left: 20px;
  width: 170px;
}

.nav_content span{
  font-size: 18px;
}
.carousel-container {
  width: 100%;
  max-width: 100%;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover; 
}

.description-item {
  display: flex;
  align-items: center;
}

.label {
  text-align: left;
  margin-right: 5px; /* 控制冒号后的间距 */
}

.value {
  flex: 1; /* 自动扩展以对齐数据 */
  text-align: left;
}

.line {
  height: 1px;
  margin-top: 28px;
  background: linear-gradient(297deg, #fff, #dcdcdc 51%, #fff);
}

::v-deep .el-table .el-table__header th {
  background-color: #F5F7FA;
  color: #909399;
}
</style>