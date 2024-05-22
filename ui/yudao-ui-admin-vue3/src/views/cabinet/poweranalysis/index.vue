<template>
  <CommonMenu :dataList="navList" @check="handleCheck" navTitle="机柜能耗趋势">
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
           <!-- <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button> -->
           <el-button type="success" plain :loading="exportLoading">
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
        <template v-for="column in tableColumns">
          <el-table-column :key="column.prop" :label="column.label" :align="column.align" :prop="column.prop" :formatter="column.formatter" :width="column.width" v-if="column.istrue">
            <template #default="{ row }" v-if="column.slot === 'actions'">
              <el-button link type="primary" @click="toDetails(row.cabinetId, row.address)">详情</el-button>
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
    </template>
  </CommonMenu>
  <ContentWrap>
    <div v-loading="loading" ref="rankChartContainer" id="rankChartContainer" style="height: 65vh"></div>
  </ContentWrap>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import download from '@/utils/download'
import { EnergyConsumptionApi } from '@/api/pdu/energyConsumption'
import { HistoryDataApi } from '@/api/pdu/historydata'
import { formatDate, endOfDay, convertDate, addTime, betweenDay } from '@/utils/formatTime'
import { CabinetApi } from '@/api/cabinet/info'
import type Node from 'element-plus/es/components/tree/src/model/node'
import * as echarts from 'echarts';
const { push } = useRouter()
defineOptions({ name: 'PowerAnalysis' })

const navList = ref([]) as any // 左侧导航栏树结构列表
const instance = getCurrentInstance();
const message = useMessage()
const activeName = ref('myData') 
const loading = ref(true)
const list = ref<Array<{ }>>([]) as any; 
const total = ref(0)
const realTotel = ref(0) // 数据的真实总条数
const selectTimeRange = ref(undefined)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 15,
  granularity: 'day',
  timeRange: undefined as string[] | undefined,
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
    text: '最近六个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setMonth(start.getMonth() - 6)
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
      title: { text: '各机柜耗电量'},
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
  { label: '位置', align: 'center', prop: 'address' , istrue:true, width: '180px'},
  { label: '开始时间', align: 'center', prop: 'start_time' , formatter: formatTime, width: '200px' , istrue:true},
  { label: '开始电能(kWh)', align: 'center', prop: 'start_ele' , istrue:true, formatter: formatEle},
  { label: '结束时间', align: 'center', prop: 'end_time' , formatter: formatTime, width: '200px' , istrue:true},
  { label: '结束电能(kWh)', align: 'center', prop: 'end_ele' , istrue:true, formatter: formatEle},
  { label: '耗电量(kWh)', align: 'center', prop: 'eq_value' , istrue:true, formatter: formatEle},
  { label: '记录时间', align: 'center', prop: 'create_time', formatter: formatTime, width: '200px' , istrue:true},
  { label: '操作', align: 'center', slot: 'actions' , istrue:true, width: '120px'},
]) as any;

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    if ( selectTimeRange.value != undefined){
      // 格式化时间范围 加上23:59:59的时分秒 
      const selectedStartTime = formatDate(endOfDay(convertDate(selectTimeRange.value[0])))
      // 结束时间的天数多加一天 ，  一天的毫秒数
      const oneDay = 24 * 60 * 60 * 1000;
      const selectedEndTime = formatDate(endOfDay(addTime(convertDate(selectTimeRange.value[1]), oneDay )))
      queryParams.timeRange = [selectedStartTime, selectedEndTime];
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
  tooltipContent += '位置：'+list.value[item.dataIndex].address + '  '
  tooltipContent += '<br/>'+ item.marker + item.seriesName + ': ' + item.value + 'kWh 记录时间：'+formatTime(null, null, list.value[item.dataIndex].create_time) + '<br/>'                 
                    +item.marker + '结束电能：'+list.value[item.dataIndex].end_ele + 'kWh 结束时间：'+formatTime(null, null, list.value[item.dataIndex].end_time) + '<br/>' 
                    +item.marker +'开始电能：'+formatEle(null, null, list.value[item.dataIndex].start_ele) + 'kWh 开始时间：'+formatTime(null, null, list.value[item.dataIndex].start_time) + '<br/>'
  return tooltipContent;
}

// 最后一页显示数据量过大的提示
const shouldShowDataExceedMessage = computed(() => {
  const lastPageNo = Math.ceil(total.value / queryParams.pageSize);
  return queryParams.pageNo === lastPageNo && total.value >= 10000;
});

// 格式化日期
function formatTime(row: any, column: any, cellValue: number): string {
  if (!cellValue) {
    return ''
  }

  return dayjs(cellValue).format('YYYY-MM-DD')
}

// 格式化电能列数据，保留1位小数
function formatEle(row: any, column: any, cellValue: number): string {
  return cellValue.toFixed(1);
}

// 格式化耗电量列数据，保留1位小数
function formatEQ(value, decimalPlaces){
  if (!isNaN(value)) {
    return value.toFixed(decimalPlaces);
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
      if(item.type == 4){
        arr.push(item.unique);
      }
    });
    // handleQuery()
}

// 接口获取机房导航列表
const getNavList = async() => {
  const res = await CabinetApi.getRoomList({})
  let arr = [] as any
  for (let i=0; i<res.length;i++){
  var temp = await CabinetApi.getRoomPDUList({id : res[i].id})
  arr = arr.concat(temp);
  }
  navList.value = arr
}


/** 详情操作*/
const toDetails = (cabinetId: number, address: string) => {
  push('/cabinet/nenghao/ecdistribution?cabinetId='+cabinetId+'&address='+address);
}

/** 初始化 **/
onMounted(() => {
  getNavList()
  getList();
});

</script>

<style scoped>
.el-form-item__label{
  width: auto;
}
.realTotal{
  float: right;
  padding-top: 20px;
  padding-right: 20px;
  font-size: 14px;
  font-weight: 400; 
  color: #606266
}
</style>