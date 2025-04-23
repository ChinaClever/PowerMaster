<template>
  <CommonMenu1 :dataList="navList" @node-click="handleClick" navTitle="机房能耗排名" :showCheckbox="false" :hightCurrent="true" :currentKey="currentKey" ref="commonMenu1">
    <template #NavInfo>
      <br/>
      <div class="nav_data">
      <div class="nav_header">      
        <span>{{nowAddress}}</span>
      </div>
      <div class="nav_content">
       <div class="description-item" v-if="totalEqData!=null">
          <span class="label">总耗电量 :</span>
          <span >{{ formatNumber(totalEqData, 1) }} kWh</span>
        </div>
        <div class="description-item show" v-if="maxEqDataTemp!=null" style="white-space: nowrap;">
          <span class="label">最大耗电量 :</span>
          <span style="white-space: nowrap;">{{ formatNumber(maxEqDataTemp, 1) }} kWh</span>
        </div>
        <div v-if="maxEqDataTimeTemp!=null" class="description-item">
          <span class="label">记录时间 :</span>
          <span class="value">{{ maxEqDataTimeTemp }}</span>
        </div>
        <div class="description-item" v-if="minEqDataTemp!=null">
          <span class="label">最小耗电量 :</span>
          <span >{{ formatNumber(minEqDataTemp, 1) }} kWh</span>
        </div>
        <div v-if="minEqDataTimeTemp!=null" class="description-item">
          <span class="label">记录时间 :</span>
          <span class="value">{{ minEqDataTimeTemp }}</span>
        </div>
      </div>
    </div>
    </template>
    <template #ActionBar>
      <el-tabs v-model="activeName">
        <el-tab-pane label="日数据" name="dayTabPane"/>
        <el-tab-pane label="周数据" name="weekTabPane"/>
        <el-tab-pane label="月数据" name="monthTabPane"/>
      </el-tabs>
      <!-- 搜索工作栏 -->
      <el-form
        class="-mb-15px"
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        label-width="auto"
      >

      <el-form-item label="时间段" prop="timeRange" >
        <el-date-picker
          value-format="YYYY-MM-DD"
          v-model="selectTimeRange"
          type="daterange"
          :shortcuts="shortcuts"
          range-separator="-"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          :disabled-date="disabledDate"
          class="!w-350px"
          @change="handleDayPick"
        />
      </el-form-item>

        <el-form-item >
          <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        </el-form-item>
        <el-form-item style="position: absolute; right: 0;">
          <el-button type="success" plain @click="handleExport1" :loading="exportLoading">
             <Icon icon="ep:download" class="mr-5px" /> 导出
           </el-button>
          </el-form-item>
      </el-form>
    </template>
    <template #Content>
      <!-- 列表 -->
      <el-tabs v-model="activeName1">
        <el-tab-pane label="图表" name="lineChart">
          <div v-loading="loading" ref="chartContainer" id="chartContainer" style="width: 70vw; height: 58vh;"></div>
        </el-tab-pane>
        <el-tab-pane label="数据" name="lineChartData">
          <div style="height: 70vh;">
            <el-table  
              v-if="queryParams.roomId!=null"
              :border="true"
              :stripe="true"
              :data="tableData"
              style="height: 100%; width: 99.97%;"
              :header-cell-style="{ backgroundColor: '#F5F7FA', color: '#909399', textAlign: 'center', borderLeft: '1px #EDEEF2 solid', borderBottom: '1px #EDEEF2 solid', fontFamily: 'Microsoft YaHei',fontWeight: 'bold'}"
              :cell-style="{ color: '#606266', fontSize: '14px', textAlign: 'center', borderBottom: '0.25px #F5F7FA solid', borderLeft: '0.25px #F5F7FA solid' }"
              :row-style="{ fontSize: '14px', textAlign: 'center', }"
              empty-text="暂无数据" max-height="818">
               <!-- 添加行号列 -->
              <el-table-column label="序号" align="center" width="80px">
                <template #default="{ $index }">
                  {{ $index + 1 }}
                </template>  
              </el-table-column>
              <!-- 动态生成表头 -->
              <template  v-for="item in headerData" :key="item.name">
                <el-table-column  label="开始电能">
                  <el-table-column prop="startEleData" label="开始电能(kWh)"/>   
                  <el-table-column prop="startTimeData" label="开始时间"/>
                </el-table-column>
                <el-table-column  label="结束电能">
                  <el-table-column prop="endEleData" label="结束电能(kWh)"/>   
                  <el-table-column prop="endTimeData" label="结束时间"/>
                </el-table-column>
                <el-table-column v-if="item.name === '耗电量'" label="耗电量">
                  <el-table-column :prop="item.name" label="耗电量(kWh)"/>   
                  <el-table-column prop="create_time" label="记录时间"/>
                </el-table-column>
              </template>


            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </template>
  </CommonMenu1>

</template>

<script setup lang="ts">
import { dayjs, ElMessage } from 'element-plus'
import * as echarts from 'echarts';
import { onMounted } from 'vue'
import { IndexApi } from '@/api/room/roomindex'
import { formatDate, endOfDay, convertDate, betweenDay,startOfDay, addTime } from '@/utils/formatTime'
import { EnergyConsumptionApi } from '@/api/room/energyConsumption'
import download from '@/utils/download';
import CommonMenu1 from '@/components/CommonMenu1.vue/CommonMenu1.vue';
defineOptions({ name: 'ECDistribution' })

const exportLoading = ref(false)
const navList = ref([]) as any // 左侧导航栏树结构列表
const nowAddress = ref('')// 导航栏的位置信息
const nowAddressTemp = ref('')// 暂时存储点击导航栏的位置信息 确认有数据再显示
const activeName = ref('dayTabPane')
const activeName1 = ref('lineChart')
const tableData = ref<Array<{ }>>([]); // 折线图表格数据
const headerData = ref<any[]>([]);
const instance = getCurrentInstance();
const currentKey=ref()
const route=useRoute()
let lastdate=null;
let lastWeekOrMonth=null;
const selectTimeRange : any= ref(defaultDayTimeRange(7))
if(history.state.start!=null&&history.state.end!=null&&history.state.start!=''&&history.state.end!=''){
  selectTimeRange.value=[history.state.start as string,history.state.end as string]
}
const loading = ref(false) 
const message = useMessage() // 消息弹窗
const queryParams = reactive({
  roomId: undefined as number | undefined,
  granularity: 'day',
  // 进入页面原始数据默认显示最近2周
  timeRange: ['', ''],
})
// const carouselItems = ref([
//       { imgUrl: PDUImage},
//       { imgUrl: PDUImage},
//       { imgUrl: PDUImage},
//       { imgUrl: PDUImage},
//     ]);//侧边栏轮播图图片路径
// 默认查询的时间范围，单位：天
function defaultDayTimeRange(day: number){
  // 获取当前日期
  var endDate = new Date();
  // 计算半个月前的日期
  var startDate = new Date();
  startDate.setDate(startDate.getDate() - day); // 15天为半个月
  // 格式化日期并返回
  return [
    startDate.toISOString().slice(0, 10),
    endDate.toISOString().slice(0, 10)
  ];
}

// 默认查询的时间范围，单位：月
function defaultMonthTimeRange(month) {
  // 获取当前日期
  var endDate = new Date();

  // 计算指定月数前的日期
  var startDate = new Date();
  startDate.setMonth(startDate.getMonth() - month);

  // 格式化日期并返回
  return [
    startDate.toISOString().slice(0, 10), 
    endDate.toISOString().slice(0, 10) 
  ];
}

const shortcuts = [
  {
    text: '最近两周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 24 * 7 * 2)
      return [start, end]
    },
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setUTCMonth(start.getUTCMonth() - 1)
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
      start.setUTCMonth(start.getUTCMonth() - 6)
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

function defaultYear(){
  const preYear=new Date();
  preYear.setFullYear(preYear.getFullYear()-1)
  return [dayjs(preYear).format("YYYY-MM-DD HH:mm:ss"),dayjs(new Date()).format("YYYY-MM-DD HH:mm:ss")]
}
// 监听切换日周月tab切换
watch( ()=>activeName.value, async(newActiveName,oldActiveName)=>{
  if(oldActiveName=="dayTabPane"){
    lastdate=selectTimeRange.value;
  }else{
    lastWeekOrMonth=selectTimeRange.value;
  }
  if ( newActiveName == 'dayTabPane'){
    queryParams.granularity = 'day'
    // selectTimeRange.value = defaultDayTimeRange(7)
    if(lastdate!=null){
      selectTimeRange.value=lastdate;
    }
  }else if (newActiveName == 'weekTabPane'){
    queryParams.granularity = 'week'
    if(lastWeekOrMonth!=null){
      selectTimeRange.value=lastWeekOrMonth
    }else{
      selectTimeRange.value = defaultYear()
    }
  }else{
    queryParams.granularity = 'month'
    if(lastWeekOrMonth!=null){
      selectTimeRange.value=lastWeekOrMonth
    }else{
      selectTimeRange.value = defaultYear()
    }
  }
  handleQuery();
});

// 表格映射图数据
const updateTableData = () => {
  const data: any[] = [];
  const length = headerData.value[0]?.data?.length || 0;
  for (let i = 0; i < length; i++) {
    const rowData: { [key: string]: any } = {};
    rowData['create_time'] = createTimeData.value[i];
    rowData['startEleData'] = startEleData.value[i];
    rowData['startTimeData'] = startTimeData.value[i];
    rowData['endEleData'] = endEleData.value[i];
    rowData['endTimeData'] = endTimeData.value[i];
    for (const item of headerData.value) {
      rowData[item.name] = item.data[i];
    }
    data.push(rowData);
  }
  tableData.value = data;
};

// 折线图数据
const startEleData = ref<number[]>([]);
const startTimeData = ref<string[]>([]);
const endEleData = ref<number[]>([]);
const endTimeData = ref<string[]>([]);
const eqData = ref<number[]>([]);
const createTimeData = ref<string[]>([]);
const totalEqData = ref();
const maxEqDataTemp = ref();// 最大耗电量 
const maxEqDataTimeTemp = ref();// 最大耗电量的发生时间 
const minEqDataTemp = ref();// 最小耗电量 
const minEqDataTimeTemp = ref();// 最小耗电量的发生时间 
// 获取折线图数据
const getLineChartData =async () => {
loading.value = true
 try {
    // 格式化时间范围 加上23:59:59的时分秒 
    queryParams.timeRange[0] = formatDate(endOfDay(convertDate(selectTimeRange.value[0])))
    // 结束时间的天数多加一天 ，  一天的毫秒数
    queryParams.timeRange[1] = formatDate(endOfDay(addTime(convertDate(selectTimeRange.value[1]),1000*60*60*24)))

    const data = await EnergyConsumptionApi.getEQDataDetails(queryParams);
    if (data != null && data.total != 0){
      totalEqData.value = 0;
      startEleData.value = data.list.map((item) => formatNumber(item.start_ele, 1));
      startTimeData.value = data.list.map((item) => formatDate(item.start_time, 'YYYY-MM-DD'));
      endEleData.value = data.list.map((item) => formatNumber(item.end_ele, 1));
      endTimeData.value = data.list.map((item) => formatDate(item.end_time, 'YYYY-MM-DD'));
      eqData.value = data.list.map((item) => formatNumber(item.eq_value, 1));
      createTimeData.value = data.list.map((item) => formatDate(item.create_time, 'YYYY-MM-DD'));

      maxEqDataTemp.value = Math.max(...eqData.value);
      minEqDataTemp.value = Math.min(...eqData.value);
      
      eqData.value.forEach(function(num, index) {
        if (num == maxEqDataTemp.value){
          maxEqDataTimeTemp.value = createTimeData.value[index]
        }
        if (num == minEqDataTemp.value){
          minEqDataTimeTemp.value = createTimeData.value[index]
        }
        totalEqData.value += Number(num);
             console.log(index)
      });
 
      // 图表显示的位置变化
      nowAddress.value = nowAddressTemp.value
    }else{
      ElMessage({
        message: '暂无数据',
        type: 'warning',
      });
      totalEqData.value = null;
      data.list = [];
      startEleData.value = [];
      startTimeData.value = [];
      endEleData.value = [];
      endTimeData.value = [];
      eqData.value = [];
      createTimeData.value = [];
      maxEqDataTemp.value = null;
      minEqDataTemp.value = null;
      maxEqDataTimeTemp.value = null
      minEqDataTimeTemp.value = null
      nowAddress.value = nowAddressTemp.value
    }
 } finally {
   loading.value = false
 }
}

// 初始化折线图
const chartContainer = ref<HTMLElement | null>(null);
let lineChart = null as echarts.ECharts | null; 
const initLineChart = () => {
  lineChart?.dispose();
  lineChart=null;
  if (chartContainer.value && instance) {
    lineChart = echarts.init(chartContainer.value);
    lineChart.setOption({
      title: { text: '耗电量趋势图', top: -4},
      tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
      legend: { data: []},
      grid: {left: '3%', right: '4%', bottom: '3%', containLabel: true},
      toolbox: {feature: {  restore:{}, saveAsImage: {}}},
      xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
      yAxis: { type: 'value', name: "kWh"},
      series: [{name: '耗电量', type: 'line', data: eqData.value}],
      dataZoom:[{type: "inside"}],
    });
    instance.appContext.config.globalProperties.lineChart = lineChart;
    // 每次切换图就要动态生成数据表头
    headerData.value = lineChart?.getOption().series as any[];
    updateTableData();
  }
};

// 处理数据后有几位小数点
function formatNumber(value, decimalPlaces) {
    if (!isNaN(value)) {
        return Number(value).toFixed(decimalPlaces);
    } else {
        return null; // 或者其他默认值
    }
}

// 给折线图提示框的数据加单位
function customTooltipFormatter(params: any[]) {
  console.log("params==",params)
  var tooltipContent = '';
  tooltipContent += params[0].marker + ' ' +'记录时间: ' + params[0].name;
  tooltipContent += "&nbsp;&nbsp;&nbsp;&nbsp;" + params[0].seriesName + ': ' + params[0].value + ' kWh';
  tooltipContent += '<br/>&nbsp;&nbsp;&nbsp;&nbsp;'+'开始时间: ' + tableData.value[params[0].dataIndex].startTimeData;
  tooltipContent += '&nbsp;&nbsp;&nbsp;&nbsp;开始电能: ' + tableData.value[params[0].dataIndex].startEleData + ' kWh';
  tooltipContent += '<br/>&nbsp;&nbsp;&nbsp;&nbsp;'+'结束时间: ' + tableData.value[params[0].dataIndex].endTimeData;
  tooltipContent += '&nbsp;&nbsp;&nbsp;&nbsp;结束电能: ' + tableData.value[params[0].dataIndex].endEleData + ' kWh';
  return tooltipContent;
}

// 处理时间选择不超过xxx范围
const handleDayPick = () => {
  if (activeName.value=='weekTabPane'){
    // 计算两个日期之间的天数差
    const diffDays = betweenDay(convertDate(selectTimeRange.value[0]), convertDate(selectTimeRange.value[1]))
    // 如果天数差不超过7天，则重置选择的日期
    if (diffDays < 7) {
      selectTimeRange.value = defaultDayTimeRange(7)
      ElMessage({
        message: '时间选择不少于7天,已默认选择最近一周',
        type: 'warning',
      })
    }
  }
  if (activeName.value=='monthTabPane'){
    // 计算两个日期之间的天数差
    const diffDays = betweenDay(convertDate(selectTimeRange.value[0]), convertDate(selectTimeRange.value[1]))
    // 如果天数差超过30天，则重置选择的日期
    if (diffDays < 30) {
      selectTimeRange.value = defaultMonthTimeRange(1)
      ElMessage({
        message: '时间选择不少于1个月,已默认选择最近一个月',
        type: 'warning',
      })
    }
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

window.addEventListener('resize', function() {
  lineChart?.resize();
  // rankChart?.resize();  
});

// 导航栏选择后触发
const handleClick = async (row) => {
    queryParams.roomId = row.id
    nowAddressTemp.value = row.name
    handleQuery()
}

// 接口获取机房导航列表
const getNavList = async() => {
  const res = await IndexApi.getRoomList()
  // navList.value = res
  navList.value=res.map((item)=>{return {id:item.id,name:item.roomName,children:[]}})
}

/** 搜索按钮操作 */
const handleQuery = async() => {
  if(queryParams.roomId ==undefined){
    ElMessage.error('机房不能为空')
    return;
  }
  // if(selectTimeRange.value==null||selectTimeRange.value.length!=2){
  //   ElMessage.error('时间段不能为空')
  //   return;
  // }
  await getLineChartData();
  // await getRankChartData();
  initLineChart();
  // initRankChart();
}


if(history.state.roomId!=null){
  currentKey.value = history.state.roomId
}

/** 初始化 **/ 
onMounted(async () => {
  getNavList()
  const queryRoomId = history.state.roomId as string | undefined;
  console.log("queryRoomId==",queryRoomId)
  const queryLocation = history.state.location as string;
  queryParams.roomId = queryRoomId ? parseInt(queryRoomId, 10) : undefined;
  if (queryParams.roomId != undefined){
    await getLineChartData();
    nowAddress.value = queryLocation;
    nowAddressTemp.value = queryLocation;
    initLineChart();
  }
})
onBeforeUnmount(() => {
  lineChart?.dispose();
  lineChart = null;
})
//导出Excel
const handleExport1 = async () => {
  try {
    // 导出的二次确认
    if(queryParams.roomId ==undefined){
      ElMessage.error('机房不能为空')
      return;
    }
    await message.exportConfirm()
    // 发起导出
    exportLoading.value = true
    const axiosConfig = {
      timeout: 0 // 设置超时时间为0
    }
    const data = await EnergyConsumptionApi.getEQDataDetailsExcel(queryParams, axiosConfig)
    await download.excel(data, '机房能耗分析.xlsx')
  } catch (error) {
    // 处理异常
    console.error('导出失败：', error)
  } finally {
    exportLoading.value = false
  }
}
</script>

<style scoped>
.nav_header {
    display: flex;
    flex-direction: column;
    align-items: center;
    font-size: 14px;
  }
.nav_data{
  padding-left: 0px;
  width: 95%px;
}
.nav_content span{
  font-size: 14px;
  margin-left: 10px;
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
