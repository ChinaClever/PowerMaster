<template>
  <CommonMenu :dataList="navList" @check="handleCheck" navTitle="机房实时能耗">
    <template #NavInfo>
    <br/>
        <div class="nav_data">
<div class="descriptions-container" style="font-size: 14px;">
          <div class="description-item">
            <span class="label">按时间范围查询实时能耗</span>
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
         <el-form-item label="时间段" prop="timeRange">
            <el-date-picker
            value-format="YYYY-MM-DD"
            v-model="selectTimeRange"
            type="daterange"
            :shortcuts="shortcuts"
            range-separator="-"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :disabled-date="disabledDate"
            :clearable="false"
          />
          </el-form-item>

         <el-form-item >
           <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
         </el-form-item>
         <el-form-item style="position: absolute;right: 0px;">
          <el-button type="success" plain :loading="exportLoading" @click="handleExport">
             <Icon icon="ep:download" class="mr-5px" /> 导出
           </el-button>         
          </el-form-item>
      </el-form> 
    </template>
    <template #Content>
      <!-- <ContentWrap>
        <div v-loading="loading" ref="rankChartContainer" id="rankChartContainer" style="height: 65vh"></div>
      </ContentWrap> -->
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
            <el-button v-if="row.eleActive!=null" type="primary" @click="toDetails(row.roomId, row.createTimeMin,row.createTimeMax)">详情</el-button>
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
                <el-button type="primary" @click="toDetails(row.roomId,row.createTimeMin,row.createTimeMax)">详情</el-button>
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
      <ContentWrap>
        <div v-loading="loading" ref="rankChartContainer" id="rankChartContainer" style="height: 65vh"></div>
      </ContentWrap>
    </template>
  </CommonMenu>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import download from '@/utils/download'
import { EnergyConsumptionApi } from '@/api/room/energyConsumption'
import { formatDate, endOfDay, convertDate, addTime, beginOfDay } from '@/utils/formatTime'
import { IndexApi } from '@/api/room/roomindex'
import * as echarts from 'echarts';
import { RoomEnergyApi } from '@/api/room/roomenergy'
import { time } from 'console'
import { on } from 'events'
const message = useMessage() // 消息弹窗
// import PDUImage from '@/assets/imgs/PDU.jpg'
const { push } = useRouter()
defineOptions({ name: 'PowerAnalysis' })

const navList = ref([]) as any // 左侧导航栏树结构列表
const lastDayTotalData = ref(0)
const lastWeekTotalData = ref(0)
const lastMonthTotalData = ref(0)
const instance = getCurrentInstance();
const loading = ref(false)
const list = ref<Array<{ }>>([]) as any; 
const total = ref(0)
const realTotel = ref(0) // 数据的真实总条数
const selectTimeRange = ref([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 15,
  granularity: 'day',
  timeRange: undefined as string[] | undefined,
  roomIds:[]
})
const pageSizeArr = ref([15,30,50,100])
const queryFormRef = ref()
const exportLoading = ref(false)
// const carouselItems = ref([
//       { imgUrl: PDUImage},
//       { imgUrl: PDUImage},
//       { imgUrl: PDUImage},
//       { imgUrl: PDUImage}, 
//     ]);//侧边栏轮播图图片路径
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
      title: { text: '各机房耗电量'},
      tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
      legend: { data: []},
      toolbox: {feature: {saveAsImage:{}}},
      xAxis: {type: 'category', data: getPageNumbers(queryParams.pageNo)},
      yAxis: { type: 'value', name: "kWh"},
      series: [
        {name:"耗电量",  type: 'bar', data: eqData.value, label: { show: true, position: 'top' }}
      ],
    });
    rankChart.on('click', function(params) {
      // 控制台打印数据的名称
      toDetails(list.value[params.dataIndex].roomId,
      list.value[params.dataIndex].createTimeMin,
      list.value[params.dataIndex].createTimeMax);
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
  { label: '位置', align: 'center', prop: 'name' , istrue:true, width: '180px'},
{ label: '开始电能', align: 'center', istrue: true, children: [
      { label: '开始电能(kWh)', align: 'center', prop: 'eleActiveStart' , istrue:true, formatter: formatEle},
      { label: '开始时间', align: 'center', prop: 'createTimeMin' , formatter: formatTime1, width: '150px' , istrue:true},
    ]
  },
    { label: '结束电能', align: 'center', istrue: true, children: [
      { label: '结束电能(kWh)', align: 'center', prop: 'eleActiveEnd' , istrue:true, formatter: formatEle},
      { label: '结束时间', align: 'center', prop: 'createTimeMax' , formatter: formatTime1, width: '150px' , istrue:true},
    ]
  },
  { label: '耗电量(kWh)', align: 'center', prop: 'eleActive' ,istrue: true,formatter: formatEle },
  { label: '操作', align: 'center', slot: 'actions' , istrue:true, width: '120px'},
]) as any;

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    if ( selectTimeRange.value != undefined){
      // 格式化时间范围 加上23:59:59的时分秒 
      const selectedStartTime = formatDate(beginOfDay(convertDate(selectTimeRange.value[0])))
      // 结束时间的天数多加一天 ，  一天的毫秒数
      const selectedEndTime = formatDate(endOfDay(convertDate(selectTimeRange.value[1])))
      queryParams.timeRange = [selectedStartTime, selectedEndTime];
    }
    // 时间段清空后值会变成null 此时搜索不能带上时间段
    if(selectTimeRange.value == null){
      queryParams.timeRange = undefined
    }
    if(selectTimeRange.value == null){
      // queryParams.timeRange = undefined
      ElMessage({
        message: '请输入时间范围',
        type: 'error',
        plain: true,
      })
    return;
    }
    const data = await RoomEnergyApi.getRoomEleTotalRealtime(queryParams)
    eqData.value = data.list.map((item) => formatEQ(item.eleActive, 1));
    list.value = data.list
    realTotel.value = data.total
    if (data.total > 10000){
      total.value = 10000
    }else{
      total.value = data.total
    }
  } finally {
    rankChart?.off("click")
    rankChart?.dispose();
    initChart();
    loading.value = false
  }
}

// 自定义图表提示框
function customTooltipFormatter(params: any[]) {
  var tooltipContent = ''; 
  var item = params[0]; // 获取第一个数据点的信息
  tooltipContent += '位置：'+list.value[item.dataIndex].name + '<br/>'
                    +item.marker + '开始日期：'+formatTime(null, null, list.value[item.dataIndex].createTimeMin) + '  开始电能：'+formatEle(null, null, list.value[item.dataIndex].eleActiveStart)  + 'kWh <br/>' 
                    +item.marker + '结束日期：'+formatTime(null, null, list.value[item.dataIndex].createTimeMax) + '  结束电能：'+formatEle(null, null, list.value[item.dataIndex].eleActiveEnd) + 'kWh  <br/>' 
                    +item.marker +'耗电量：'+formatEle(null, null, list.value[item.dataIndex].eleActive) + 'kWh';
                    // +item.marker + '开始电能：'+formatEle(null, null, list.value[item.dataIndex].eleActiveStart)  + 'kWh 开始日期：'+formatTime(null, null, list.value[item.dataIndex].createTimeMin) + '<br/>' 
                    // +item.marker +'结束电能：'+formatEle(null, null, list.value[item.dataIndex].eleActiveEnd) + 'kWh 结束日期：'+formatTime(null, null, list.value[item.dataIndex].createTimeMax) + '<br/>'
  return tooltipContent;
}

// 最后一页显示数据量过大的提示
const shouldShowDataExceedMessage = computed(() => {
  const lastPageNo = Math.ceil(total.value / queryParams.pageSize);
  return queryParams.pageNo === lastPageNo && total.value >= 10000;
});

// 格式化日期(表格列的时间去掉时分秒 不用传参)
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
  return dayjs(cellValue).format('YYYY-MM-DD')
}

// 格式化电能列数据，保留1位小数
function formatEle(_row: any, _column: any, cellValue: number): string {
    if (cellValue == null) {
    return '';
  }
  return Number(cellValue).toFixed(1);
}

// 格式化耗电量列数据，保留1位小数
const formatEQ = (value: number, decimalPlaces: number)=>{
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
  // 设置date的日期为0时0分0秒，以便与today进行比较
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
   console.log(node)
  let arr = [] as any
    node.forEach(item => { 
        arr.push(item.id);
    });
    
    if(arr.length == 0  && node.length != 0){
      arr.push(0)
      rankChart?.clear()
      ElMessage({
        message: '暂无数据',
        type: 'warning',
      });
    }
  queryParams.roomIds = arr
  handleQuery()
}



// 接口获取机房导航列表
const getNavList = async() => {
  const res = await IndexApi.getRoomList()
  // navList.value = res
    navList.value=res.map((item)=>{return {id:item.id,name:item.roomName,children:[]}})
}

// 获取导航的数据显示
const getNavNewData = async() => {
  const res = await EnergyConsumptionApi.getNavNewData({})
  lastDayTotalData.value = res.day
  lastWeekTotalData.value = res.week
  lastMonthTotalData.value = res.month
}
watch(()=>selectTimeRange.value,(newValue)=>{
  queryParams.timeRange = newValue
});
/** 导出按钮操作 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    if(queryParams.timeRange == null||queryParams.timeRange.length != 2){
      ElMessage({
        message: '请输入时间范围',
        type: 'error',
        plain: true,
      })
      return;
    }
    await message.exportConfirm()
    // 发起导出
    queryParams.pageNo = 1
    exportLoading.value = true
    const axiosConfig = {
      timeout: 0 // 设置超时时间为0
    }
    const data = await RoomEnergyApi.getRoomEleTotalRealtimeExcel(queryParams, axiosConfig)
    await download.excel(data, '机房能耗查询.xlsx')
  } catch (error) {
    // 处理异常
    console.error('导出失败：', error)
  } finally {
    exportLoading.value = false
  }
}

// export function postExcelFile (params, url) {
//   const form = document.createElement('form')
//   form.style.display = 'none'
//   form.enctype = 'application/json;charset=utf-8' // 向后端确定他的请求头
//   form.action = url
//   form.method = 'post'
//   document.body.appendChild(form)
//   for (const key in params) {
//     const input = document.createElement('input')
//     input.type = 'hidden'
//     input.name = key
//     input.value = params[key]
//     form.appendChild(input)
//   }
//   form.submit()
//   form.remove()
// }




/** 详情操作*/
const toDetails = (roomId: number, createTimeMin : string,createTimeMax : string) => {
  //   push('/room/energyConsumption/powerAnalysis?type=total&granularity=day&start='+createTimeMin+
  // '&end='+createTimeMax+'&roomId='+ roomId+"&startTime="+(selectTimeRange.value!=null?selectTimeRange.value[0]:"")+"&endTime="+(selectTimeRange.value!=null?selectTimeRange.value[1]:""));
  push({path:"/room/energyConsumption/powerAnalysis",state:{type:"total",granularity:"day",
  start:createTimeMin,end:createTimeMax,roomId,startTime:(selectTimeRange.value!=null?selectTimeRange.value[0]:""),endTime:(selectTimeRange.value!=null?selectTimeRange.value[1]:"")}})
}
/** 初始化 **/
onMounted(() => {
  getNavList()
  // getNavNewData()
  // getList();
});
onBeforeUnmount(() => {
  rankChart?.off("click");
  rankChart?.dispose();
});
let now = new Date()
selectTimeRange.value = [dayjs(new Date(now.getFullYear(),now.getMonth(),1)).format("YYYY-MM-DD"),dayjs(now).format("YYYY-MM-DD")]
getList();
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
  font-size: 14px;
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
</style>
