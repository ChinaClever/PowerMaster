<template>
  <CommonMenu :dataList="navList" @check="handleCheck" navTitle="机架能耗趋势">
    <template #NavInfo>
   <br/>    <br/> 
        <div class="nav_data">
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
          </div>    <br/>
          <div>
            <span>全部机架新增能耗记录</span>
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
           <el-button type="success" plain :loading="exportLoading" @click="handleExport">
             <Icon icon="ep:download" class="mr-5px" /> 导出
           </el-button>
         </el-form-item>
      </el-form>
    </template>
    
    <template #Content>
      <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
        <!-- 添加行号列 -->
        <el-table-column label="序号" align="center" width="80px">
          <template #default="{ $index }">
            {{ $index + 1 + (queryParams.pageNo - 1) * queryParams.pageSize }}
          </template>
        </el-table-column>
        <!-- 遍历其他列 -->  
        <template v-for="column in tableColumns" :key="column.label">
          <el-table-column
            v-if="!column.children && column.istrue"
            :key="column.prop"
            :label="column.label"
            :align="column.align"
            :prop="column.prop"
            :formatter="column.formatter"
            :width="column.width"
          >
            <template #default="{ row }" v-if="column.slot === 'actions'">
              <el-button link type="primary" @click="toDetails(row.rack_id, row.location, row.rack_name)">详情</el-button>
            </template>
          </el-table-column>
          
          <el-table-column
            v-else-if="column.istrue"
            :label="column.label"
            :align="column.align"
          >
            <template v-for="child in column.children" :key="child.prop">
              <el-table-column
                :key="child.prop"
                :label="child.label"
                :align="child.align"
                :prop="child.prop"
                :formatter="child.formatter"
                :width="child.width"
                v-if="child.istrue"
              >
                <template #default="{ row }" v-if="child.slot === 'actions'">
                   <el-button link type="primary" @click="toDetails(row.rack_id, row.location, row.rack_name)">详情</el-button>
                </template>
              </el-table-column>
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
       <br/><br/><br/><br/>
      <ContentWrap >
        <div v-loading="loading" ref="rankChartContainer" id="rankChartContainer" style="height: 65vh"></div>
      </ContentWrap>
    </template>
  </CommonMenu>
 
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import download from '@/utils/download'
import { EnergyConsumptionApi } from '@/api/rack/energyConsumption'
import { formatDate, endOfDay, convertDate, addTime } from '@/utils/formatTime'
import { CabinetApi } from '@/api/cabinet/info'
import { IndexApi } from '@/api/rack/index'
import * as echarts from 'echarts';
const { push } = useRouter()
defineOptions({ name: 'PowerAnalysis' })

const navList = ref([]) as any // 左侧导航栏树结构列表
const lastDayTotalData = ref(0)
const lastWeekTotalData = ref(0)
const lastMonthTotalData = ref(0)
const instance = getCurrentInstance();
const loading = ref(true)
const message = useMessage() // 消息弹窗
const list = ref<Array<{ }>>([]) as any; 
const total = ref(0)
const realTotel = ref(0) // 数据的真实总条数
const selectTimeRange = ref()
const queryParams = reactive({
  pageNo: 1, 
  pageSize: 15,
  granularity: 'day',
  timeRange: undefined as string[] | undefined,
  rackIds: [] as (string | undefined)[]
})
const pageSizeArr = ref([15,30,50,100])
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
    text: '最近三个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setUTCMonth(start.getUTCMonth() - 3)
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
  {
    text: '最近一年',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setFullYear(start.getFullYear() - 1)
      return [start, end]
    },
  },
]

// 返回当前页的序号数组
const getPageNumbers = (pageNumber) => {
  const start = (pageNumber - 1) * queryParams.pageSize + 1;
  const end = pageNumber * queryParams.pageSize;
  const pageNumbers: string[] = [];
  for (let i = start; i <= end; i++) {
    pageNumbers.push('序号'+i);
  }
  return pageNumbers;
};

// 柱状图
const rankChartContainer = ref<HTMLElement | null>(null);
let rankChart = null as echarts.ECharts | null;
const eqData = ref<number[]>([]);
const initChart = () => {
  if (rankChartContainer.value && instance) {
    rankChart = echarts.init(rankChartContainer.value);
    rankChart.setOption({
      title: { text: '各机架耗电量'},
      tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
      legend: { data: []},
      toolbox: {feature: {saveAsImage:{}}},
      xAxis: {type: 'category', data: getPageNumbers(queryParams.pageNo)},
      yAxis: { type: 'value', name: "kWh"},
      series: [
        {name:"耗电量",  type: 'bar', data: eqData.value, label: { show: true, position: 'top' }, barWidth: 50},
      ],
    });
    instance.appContext.config.globalProperties.rankChart = rankChart;
  }
};

window.addEventListener('resize', function() {
  rankChart?.resize(); 
});

watch(() => queryParams.granularity, () => {
  handleQuery();
});

const tableColumns = ref([
  { label: '位置', align: 'center', prop: 'location' , istrue:true, width: '180px'},
  { label: '机架名', align: 'center', prop: 'rack_name' , istrue:true, width: '100px'},
  { label: '记录日期', align: 'center', prop: 'create_time', formatter: formatTime, width: '150px' , istrue:true},
  { label: '开始', align: 'center', istrue: true, children: [
      { label: '日期', align: 'center', prop: 'start_time' , formatter: formatTime1, width: '150px' , istrue:true},
      { label: '电能(kWh)', align: 'center', prop: 'start_ele' , istrue:true, formatter: formatEle},
    ]
  },
  { label: '结束', align: 'center', istrue: true, children: [
      { label: '日期', align: 'center', prop: 'end_time' , formatter: formatTime1, width: '150px' , istrue:true},
      { label: '电能(kWh)', align: 'center', prop: 'end_ele' , istrue:true, formatter: formatEle},
    ]
  },
  { label: '耗电量(kWh)', align: 'center', prop: 'eq_value' ,istrue: true,formatter: formatEle },
  { label: '操作', align: 'center', slot: 'actions' , istrue:true, width: '100px'},
]) as any;

const start = ref('')
const end = ref('')

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    if ( start.value != undefined){
      // 格式化时间范围 加上23:59:59的时分秒 
      const selectedStartTime = formatDate(endOfDay(convertDate(start.value)))
      // 结束时间的天数多加一天 ，  一天的毫秒数
      const oneDay = 24 * 60 * 60 * 1000;
      const selectedEndTime = formatDate(endOfDay(convertDate(end.value)))
      selectTimeRange.value = [selectedStartTime, selectedEndTime];
      queryParams.timeRange = [selectedStartTime, selectedEndTime];
    }
    // 时间段清空后值会变成null 此时搜索不能带上时间段
    if(selectTimeRange.value == null){
      queryParams.timeRange = undefined
    }
    const data = await EnergyConsumptionApi.getEQDataPage(queryParams)
    eqData.value = data.list.map((item) => formatEQ(item.eq_value, 1));
    list.value = data.list
    realTotel.value = data.total
    if (data.total > 10000){
      total.value = 10000
    }else{
      total.value = data.total
    }
  } finally {
    initChart();
    loading.value = false
  }
}

// 自定义图表提示框
function customTooltipFormatter(params: any[]) {
  var tooltipContent = ''; 
  var item = params[0]; // 获取第一个数据点的信息
  tooltipContent += '位置：'+list.value[item.dataIndex].location + '  '
  tooltipContent += '<br/>'+ item.marker + '记录时间：'+formatTime(null, null, list.value[item.dataIndex].create_time) + ' ' + item.seriesName + ': ' + item.value +'kWh <br/>'                 
                    +item.marker + '结束时间：'+formatTime(null, null, list.value[item.dataIndex].end_time) +  ' 结束电能：'+list.value[item.dataIndex].end_ele +'kWh <br/>' 
                    +item.marker + '开始时间：'+formatTime(null, null, list.value[item.dataIndex].start_time) + ' 开始电能：'+formatEle(null, null, list.value[item.dataIndex].start_ele) +'kWh <br/>'
  return tooltipContent;
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

// 格式化日期(表格列的时间去掉时分秒和年)
function formatTime1(_row: any, _column: any, cellValue: number): string {
  if (!cellValue) {
    return ''
  }
  return dayjs(cellValue).format('MM-DD')
}

// 格式化电能列数据，保留1位小数
function formatEle(_row: any, _column: any, cellValue: number): string {
  return Number(cellValue).toFixed(1);
}

// 格式化耗电量列数据，保留1位小数
function formatEQ(value, decimalPlaces){
  if (!isNaN(value)) {
    return Number(value).toFixed(decimalPlaces);
  } else {
      return null; // 或者其他默认值
  }
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

// 导航栏选择后触发
const handleCheck = async (node) => {
  let arr = [] as any
  node.forEach(item => { 
    if(item.type == 5){
      arr.push(item.id);
    }
  });
  //没筛选到 不显示任何数据 参数传0 后端返回空
  if(arr.length == 0 && node.length != 0){
  arr.push(0)
  rankChart?.clear()
  ElMessage({
    message: '暂无数据',
    type: 'warning',
  });
}
  queryParams.rackIds = arr
  handleQuery()
}

// 接口获取机房导航列表
const getNavList = async() => {
    const res = await CabinetApi.getRackMenuAll({})
  navList.value = res
}

// 获取导航的数据显示
const getNavNewData = async() => {
  const res = await EnergyConsumptionApi.getNavNewData({})
  lastDayTotalData.value = res.day
  lastWeekTotalData.value = res.week
  lastMonthTotalData.value = res.month
}


/** 详情操作*/
const toDetails = (rackId: number, location: string, rackName: string) => {
  location += '-' + rackName
  push('/u/nenghao/ecdistribution?rackId='+rackId+'&location='+location);
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
    const data = await EnergyConsumptionApi.exportEQPageData(queryParams, axiosConfig)
    await download.excel(data, '机架能耗趋势.xlsx')
  } catch (error) {
    // 处理异常
    console.error('导出失败：', error)
  } finally {
    exportLoading.value = false
  }
}

/** 初始化 **/
onMounted(() => {
  start.value = useRoute().query.start as string;
  end.value = useRoute().query.end as string;
  const queryRackId = useRoute().query.rackId as string  
  if (queryRackId!== undefined) {
  queryParams.rackIds.push(queryRackId);
  }
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
  margin-right: 10px; /* 控制冒号后的间距 */
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
</style>