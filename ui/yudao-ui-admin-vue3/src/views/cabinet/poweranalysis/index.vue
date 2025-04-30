<template>
  <CommonMenu :dataList="navList" @check="handleCheck" navTitle="机柜能耗数据" :defaultCheckedKeys="defaultCheckedKeys" :defaultExpandedKeys="defaultExpandedKeys">
    <template #NavInfo>
    <br/>
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
            <span>全部机柜新增能耗记录</span>
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

          <el-form-item label="时间段" prop="timeRange" v-if="queryParams.granularity !== 'month'">
            <el-date-picker
            value-format="YYYY-MM-DD"
            v-model="selectTimeRangeHaveDay"
            type="daterange"
            :shortcuts="shortcuts"
            range-separator="-"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :disabled-date="disabledDate"
          />
          </el-form-item>

          <el-form-item label="时间段" prop="timeRange" v-else>
            <el-date-picker
            value-format="YYYY-MM"
            v-model="selectTimeRangeNoDay"
            type="monthrange"
            :shortcuts="shortcuts"
            range-separator="-"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :disabled-date="disabledDate"
          />
          </el-form-item>

         <el-form-item >
           <el-button @click="handleQuery" style="background-color: #00778c;color:#ffffff;"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
         </el-form-item>
         <el-form-item style="position: absolute; right: 0;">
          <el-button type="success" plain :loading="exportLoading" @click="handleExport" style="background-color: #00778c;color:#ffffff;">
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
            <el-button type="primary" @click="toDetails(row.cabinet_id, row.location)" style="background-color: #00778c;color:#ffffff;">详情</el-button>
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
                <el-button type="primary" @click="toDetails(row.cabinet_id, row.location)">详情</el-button>
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
import { EnergyConsumptionApi } from '@/api/cabinet/energyConsumption'
import { formatDate, endOfDay, convertDate, addTime, startOfDay } from '@/utils/formatTime'
import { CabinetApi } from '@/api/cabinet/info'
import * as echarts from 'echarts';
const message = useMessage() // 消息弹窗
import PDUImage from '@/assets/imgs/PDU.jpg'
const { push } = useRouter()
defineOptions({ name: 'PowerAnalysis' })

const navList = ref([]) as any // 左侧导航栏树结构列表
const lastDayTotalData = ref(0)
const lastWeekTotalData = ref(0)
const lastMonthTotalData = ref(0)
const instance = getCurrentInstance();
const loading = ref(true)
const list = ref<Array<{ }>>([]) as any; 
const total = ref(0)
const realTotel = ref(0) // 数据的真实总条数
const now=new Date()
const selectTimeRangeHaveDay = ref()
const selectTimeRangeNoDay = ref()
if(history.state.start!=null&&history.state.start!=""&&history.state.end!=null&&history.state.end!=""){
  console.log("有值")
  selectTimeRangeHaveDay.value = [history.state.start, history.state.end]
}else{
  console.log("无值")
  let now = new Date();
  selectTimeRangeHaveDay.value=[dayjs(new Date(now.getFullYear(),now.getMonth(),1)).format("YYYY-MM-DD"),dayjs(now).format("YYYY-MM-DD")]
}
const queryParams = reactive({
  pageNo: 1,
  pageSize: 15,
  granularity: 'day',
  timeRange: undefined as string[] | undefined,
  cabinetIds: history.state.id!=null?[history.state.id]:[] as number[]
})
const defaultCheckedKeys = ref([])

if(history.state.id!=null){
  defaultCheckedKeys.value=[history.state.id]
}
const pageSizeArr = ref([15,30,50,100])
const queryFormRef = ref()
const exportLoading = ref(false)
const carouselItems = ref([
      { imgUrl: PDUImage},
      { imgUrl: PDUImage},
      { imgUrl: PDUImage},
      { imgUrl: PDUImage},
    ]);//侧边栏轮播图图片路径
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
  const end = Math.min(pageNumber * queryParams.pageSize,total.value);
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
    rankChart?.off("click");
    rankChart?.dispose();
    rankChart = echarts.init(rankChartContainer.value);
    rankChart.setOption({
      title: { text: '各机柜耗电量'},
      tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
      legend: { data: []},
      barMaxWidth: '30px',
      toolbox: {feature: {saveAsImage:{}}},
      xAxis: {type: 'category', data: getPageNumbers(queryParams.pageNo)},
      yAxis: { type: 'value', name: "kWh"},
      series: [
        {name:"耗电量",  type: 'bar', data: eqData.value, label: { show: true, position: 'top' },itemStyle: {
          color: new echarts.graphic.LinearGradient(  
          0, 1, 0, 0, [  
            { offset: 0, color: '#00778c' },  
            { offset: 1, color: '#069ab4' }  
          ]  
        ) }},
      ],
    });
    rankChart.on("click",(params)=>{
      toDetails(list.value[params.dataIndex].cabinet_id, list.value[params.dataIndex].location)
    })
    instance.appContext.config.globalProperties.rankChart = rankChart;
  }
};
function resize() {
  rankChart?.resize(); 
}
window.addEventListener('resize', resize);

function startOfMonth(date: Date): Date {
  return dayjs(new Date(date.getFullYear(), date.getMonth(), 1)).format("YYYY-MM-DD HH:mm:ss");
}
function endOfMonth(date: Date): Date {
  const bigMonth=[1,3,5,7,8,10,12]
  if(date.getMonth() === 1){
    if((date.getFullYear() % 4 === 0&&date.getFullYear() % 100 !== 0)||date.getFullYear() % 400 === 0){
      return dayjs(new Date(date.getFullYear(), date.getMonth(), 29,23,59,59)).format("YYYY-MM-DD HH:mm:ss");
    }else{
      return dayjs(new Date(date.getFullYear(), date.getMonth(), 28,23,59,59)).format("YYYY-MM-DD HH:mm:ss");
    }
  }else if(bigMonth.includes(date.getMonth()+1)){
    return dayjs(new Date(date.getFullYear(), date.getMonth(), 31,23,59,59)).format("YYYY-MM-DD HH:mm:ss");
  }else{
    return dayjs(new Date(date.getFullYear(), date.getMonth(), 30,23,59,59)).format("YYYY-MM-DD HH:mm:ss");
  }
}
watch(() => queryParams.granularity, (newValue) => {
  if(newValue == "month"){
    if(selectTimeRangeHaveDay.value!=null&&selectTimeRangeHaveDay.value.length==2){
      selectTimeRangeNoDay.value=[dayjs(startOfMonth(convertDate(selectTimeRangeHaveDay.value[0]))).format("YYYY-MM"),dayjs(endOfMonth(convertDate(selectTimeRangeHaveDay.value[1]))).format("YYYY-MM")]
    }
  }
  handleQuery();
});

const tableColumns = ref([
  { label: '位置', align: 'center', prop: 'location' , istrue:true, width: '230px'},
  { label: '记录日期', align: 'center', prop: 'create_time', formatter: formatTime, width: '200px' , istrue:true},
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
  { label: '耗电量\n(kWh)', align: 'center', prop: 'eq_value' ,istrue: true,formatter: formatEle },
  { label: '操作', align: 'center', slot: 'actions' , istrue:true, width: '120px'},
]) as any;

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    if ( queryParams.granularity!="month" && selectTimeRangeHaveDay.value!=null){
      console.log("noMonth===>",selectTimeRangeHaveDay.value)
      const selectedStartTime = formatDate(startOfDay(convertDate(selectTimeRangeHaveDay.value[0])))
      const selectedEndTime = formatDate(endOfDay(convertDate(selectTimeRangeHaveDay.value[1])))
      queryParams.timeRange = [selectedStartTime, selectedEndTime];
    }
    if(queryParams.granularity=="month"&&selectTimeRangeNoDay.value!=null){
      console.log("month===>",selectTimeRangeNoDay.value)
      const selectedStartTime = formatDate(startOfMonth(convertDate(selectTimeRangeNoDay.value[0])))
      const selectedEndTime = formatDate(endOfMonth(convertDate(selectTimeRangeNoDay.value[1])))
      queryParams.timeRange = [selectedStartTime, selectedEndTime];
    }
    // 时间段清空后值会变成null 此时搜索不能带上时间段
    if(( queryParams.granularity!="month" && selectTimeRangeHaveDay.value==null)||(queryParams.granularity=="month"&&selectTimeRangeNoDay.value==null)){
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

const getList1 = async () => {
  loading.value = true
  try {
    if ( start.value != undefined){
      // 格式化时间范围 加上23:59:59的时分秒 
      const selectedStartTime = formatDate(startOfDay(convertDate(start.value)))
      // 结束时间的天数多加一天 ，  一天的毫秒数
      const selectedEndTime = formatDate(endOfDay(convertDate(end.value)))
      // selectTimeRange.value = [selectedStartTime, selectedEndTime];
      queryParams.timeRange = [selectedStartTime, selectedEndTime];
    }
    	console.log('入参', queryParams);
    // 时间段清空后值会变成null 此时搜索不能带上时间段
    if(start.value == null){
      queryParams.timeRange = undefined
    }
    queryParams.cabinetIds =[id.value]
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
  tooltipContent += '<br/>'+ item.marker +'记录日期：'+formatTime(null, null, list.value[item.dataIndex].create_time) + ' '+ item.seriesName + ': ' + item.value + 'kWh <br/>'                 
                    +item.marker + '结束日期：'+formatTime(null, null, list.value[item.dataIndex].end_time) +  ' 结束电能：'+formatEle(null, null, list.value[item.dataIndex].end_ele) +'kWh <br/>' 
                    +item.marker + '开始日期：'+formatTime(null, null, list.value[item.dataIndex].start_time) + ' 开始电能：'+formatEle(null, null, list.value[item.dataIndex].start_ele) +'kWh <br/>'
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
  let arr = [] as any
  node.forEach(item => { 
    if(item.type == 3){
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
  queryParams.cabinetIds = arr
  handleQuery()
}

let tempId=-1;
function setNavList(list) {
  if(list==null||list.length==0) return;
  list.forEach(item => {
    if(item.type!=3){
      item.id=tempId--;
    }
    if(item.children!=null&&item.children.length>0){
      setNavList(item.children);
    }
  });
}

const defaultExpandedKeys = ref([])
if(history.state.id!=null){
  defaultExpandedKeys.value.push(history.state.id)
}

// 接口获取机房导航列表
const getNavList = async() => {
  const res = await CabinetApi.getRoomMenuAll({})
  setNavList(res)
  setDefaultExpandedKeys(res)
  navList.value = res
}

function setDefaultExpandedKeys(list):boolean {
  if(list==null||list.length==0) return false;
   for(let item of list){
    if(defaultExpandedKeys.value.includes(item.id)) {
      return true;
    }
    if(item.children!=null&&item.children.length>0){
      if(setDefaultExpandedKeys(item.children)==true){
        defaultExpandedKeys.value.push(item.id)
        return true;
      }
    }
  }
  return false;
}


// 获取导航的数据显示
const getNavNewData = async() => {
  const res = await EnergyConsumptionApi.getNavNewData({})
  lastDayTotalData.value = res.day
  lastWeekTotalData.value = res.week
  lastMonthTotalData.value = res.month
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
    await download.excel(data, '机柜能耗数据.xlsx')
  } catch (error) {
    // 处理异常
    console.error('导出失败：', error)
  } finally {
    exportLoading.value = false
  }
}
const format = (date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

/** 详情操作*/
const toDetails = (cabinetId: number, location: string) => {
  if(queryParams.granularity!='month'){
    push({path:"/cabinet/nenghao/ecdistribution",state:{cabinetId,location,start:(selectTimeRangeHaveDay.value!=null&&selectTimeRangeHaveDay.value.length==2?selectTimeRangeHaveDay.value[0]:null),end:(selectTimeRangeHaveDay.value!=null&&selectTimeRangeHaveDay.value.length==2?selectTimeRangeHaveDay.value[1]:null)}});
  }else{
    // console.log(selectTimeRangeNoDay.value,"==============selectTimeRangeNoDay.value")
    push({path:"/cabinet/nenghao/ecdistribution",state:{cabinetId,location,start:(selectTimeRangeNoDay.value!=null&&selectTimeRangeNoDay.value.length==2?dayjs(startOfMonth(convertDate(selectTimeRangeNoDay.value[0]))).format("YYYY-MM-DD"):''),end:(selectTimeRangeNoDay.value!=null&&selectTimeRangeNoDay.value.length==2?dayjs(endOfMonth(convertDate(selectTimeRangeNoDay.value[1]))).format("YYYY-MM-DD"):'')}});
  }
}
const start = ref('')
const end = ref('')
const id =  ref(0)
/** 初始化 **/
onMounted(() => {
  getNavList()
  getNavNewData()
//   const now = new Date()
//       const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
//    // 使用上述自定义的 format 函数将日期对象转换为指定格式的字符串
// selectTimeRange.value = [
//   format(startOfMonth),
//   format(now)
// ];
  // start.value = history.state.start as string;
  // end.value = history.state.end as string;
  // id.value = history.state.id as unknown as number;
  // if (start.value != null){
  // 	console.log('详情页', start);
	// console.log('详情页1', id);
  // getList1();
  // }else{
  getList();
  // }
});
onBeforeUnmount(() => {
  rankChart?.off("click");
  rankChart?.dispose();
  window.removeEventListener("resize", resize);
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
  margin-right: 5px; /* 控制冒号后的间距 */
  text-align: left;
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
/deep/ .el-pagination.is-background .el-pager li.is-active {
  background-color: #00778c;
}
</style>