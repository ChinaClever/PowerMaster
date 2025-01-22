<template>
  <CommonMenu1 :dataList="navList" @node-click="handleClick" navTitle="始端箱能耗排名" :showCheckbox="false">
    <template #NavInfo>
      
      <div class="nav_data">
        <!-- <div class="carousel-container">
          <el-carousel :interval="2500" motion-blur height="150px" arrow="never" trigger="click">
            <el-carousel-item v-for="(item, index) in carouselItems" :key="index">
              <img width="auto" height="auto" :src="item.imgUrl" alt="" class="carousel-image" />
            </el-carousel-item>
          </el-carousel>
        </div>  -->
      <!-- <div class="nav_header">
        <span v-if="nowAddress">{{nowAddress}}</span>
        <br/>
        <span>{{selectTimeRange[0]}} </span>
        <span>至</span> 
        <span>{{selectTimeRange[1]}}</span>
        <br/>
      </div> -->
      <!-- <div class="nav_content">
        <el-descriptions title="" direction="vertical" :column="1" border >
          <el-descriptions-item label="总耗电量">
            <span >{{ formatNumber(totalEqData, 1) }} kWh</span>
          </el-descriptions-item>
          <el-descriptions-item label="最大耗电量 | 发生时间">
            <span >{{ formatNumber(maxEqDataTemp, 1) }} kWh</span> <br/>
            <span  v-if="maxEqDataTimeTemp">{{ maxEqDataTimeTemp }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="最小耗电量 | 发生时间">
            <span >{{ formatNumber(minEqDataTemp, 1) }} kWh</span> <br/>
            <span  v-if="minEqDataTimeTemp">{{ minEqDataTimeTemp }}</span>
          </el-descriptions-item>
        </el-descriptions>
      </div> -->

      <div class="nav_header">       
          <span v-if="nowAddress">{{nowAddress?'暂未绑定设备':nowAddress}}</span>
        </div>
        <br/> 
      <div class="descriptions-container"  v-if="maxEqDataTimeTemp" style="font-size: 14px;">
      <div class="description-item">
        <span class="label">总耗电量 :</span>
        <span >{{ formatNumber(totalEqData, 1) }} kWh</span>
      </div>
      <div class="description-item">
        <span class="label">最大耗电量 :</span>
        <span >{{ formatNumber(maxEqDataTemp, 1) }} kWh</span>
      </div>
      <div v-if="maxEqDataTimeTemp" class="description-item">
        <span class="label">发生时间 :</span>
        <span class="value">{{ maxEqDataTimeTemp }}</span>
      </div>
  
     <div class="description-item">
        <span class="label">最小耗电量 :</span>
        <span >{{ formatNumber(minEqDataTemp, 1) }} kWh</span>
      </div>
      <div v-if="minEqDataTimeTemp" class="description-item">
        <span class="label">发生时间 :</span>
        <span class="value">{{ minEqDataTimeTemp }}</span>
      </div>
      <div class="line" style="margin-top: 10px;"></div>
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
          <el-button type="success" plain @click="handleExport1" :loading="exportLoading">
             <Icon icon="ep:download" class="mr-5px" /> 导出
           </el-button>
        </el-form-item>
      </el-form>
    </template>
    <template #Content>
      <!-- 列表 -->
      <el-tabs v-model="activeName1" v-if="loading2">
        <el-tab-pane label="图表" name="lineChart">
          <div v-loading="loading" ref="chartContainer" id="chartContainer" style="width: 70vw; height: 58vh;"></div>
        </el-tab-pane>
        <el-tab-pane label="数据" name="lineChartData">
          <div style="height: 58vh;">
            <el-table  
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
                  {{ $index + 1 + (queryParams.pageNo - 1) * queryParams.pageSize }}
                </template>
              </el-table-column>
              <!-- 动态生成表头 -->
              <template v-for="item in headerData" :key="item.name">
                <el-table-column  label="开始电能">
                  <el-table-column prop="startEleData" label="开始电能(kWh)"/>   
                  <el-table-column prop="startTimeData" label="开始日期"/>
                </el-table-column>
                <el-table-column  label="结束电能">
                  <el-table-column prop="endEleData" label="结束电能(kWh)"/>   
                  <el-table-column prop="endTimeData" label="结束日期"/>
                </el-table-column>
                <el-table-column v-if="item.name === '耗电量'" label="耗电量">
                  <el-table-column :prop="item.name" label="耗电量(kWh)"/>   
                  <el-table-column prop="create_time" label="记录日期"/>
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
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts';
import { onMounted } from 'vue'
import { IndexApi } from '@/api/bus/busindex'
import { formatDate, endOfDay, convertDate, addTime, betweenDay } from '@/utils/formatTime'
import { EnergyConsumptionApi } from '@/api/bus/busenergyConsumption'
import PDUImage from '@/assets/imgs/PDU.jpg';
import download from '@/utils/download'
import  CommonMenu1 from './component/CommonMenu1.vue'
defineOptions({ name: 'ECDistribution' })

const exportLoading = ref(false)
const message = useMessage() // 消息弹窗
const navList = ref([]) as any // 左侧导航栏树结构列表
const nowAddress = ref('')// 导航栏的位置信息
const nowAddressTemp = ref('')// 暂时存储点击导航栏的位置信息 确认有数据再显示
const activeName = ref('dayTabPane')
const activeName1 = ref('lineChart')
const tableData = ref<Array<{ }>>([]); // 折线图表格数据
const headerData = ref<any[]>([]);
const instance = getCurrentInstance();
const selectTimeRange = ref(defaultDayTimeRange(14))
const loading = ref(false) 
const loading2 = ref(false)

const queryParams = reactive({
  pageNo: 1,
  pageSize: 15,
  nowAddress: undefined as string | undefined,
  devkey: undefined as string | undefined,
  busId: undefined as number | undefined,
  granularity: 'day',
  // 进入页面原始数据默认显示最近2周
  timeRange: ['', ''],
})
const carouselItems = ref([
      { imgUrl: PDUImage},
      { imgUrl: PDUImage},
      { imgUrl: PDUImage},
      { imgUrl: PDUImage},
    ]);//侧边栏轮播图图片路径
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

// 监听切换日周月tab切换
watch( ()=>activeName.value, async(newActiveName)=>{
  if ( newActiveName == 'dayTabPane'){
    queryParams.granularity = 'day'
    selectTimeRange.value = defaultDayTimeRange(14)
  }else if (newActiveName == 'weekTabPane'){
    queryParams.granularity = 'week'
    selectTimeRange.value = defaultMonthTimeRange(3)
  }else{
    queryParams.granularity = 'month'
    selectTimeRange.value = defaultMonthTimeRange(12)
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
const totalEqData = ref(0);
const maxEqDataTemp = ref(0);// 最大耗电量 
const maxEqDataTimeTemp = ref();// 最大耗电量的发生时间 
const minEqDataTemp = ref(0);// 最小耗电量 
const minEqDataTimeTemp = ref();// 最小耗电量的发生时间 
// 获取折线图数据
const getLineChartData =async () => {
    if (queryParams.devkey == undefined){
    message.warning('请先选择设备！')
    return
  }
loading.value = true
 try {
    // 格式化时间范围 加上23:59:59的时分秒 
    queryParams.timeRange[0] = formatDate(endOfDay(convertDate(selectTimeRange.value[0])))
    // 结束时间的天数多加一天 ，  一天的毫秒数
    const oneDay = 24 * 60 * 60 * 1000;
    queryParams.timeRange[1] = formatDate(endOfDay(addTime(convertDate(selectTimeRange.value[1]), oneDay )))

    const data = await EnergyConsumptionApi.getEQDataDetails(queryParams);
    if (data.list == null){
      ElMessage.warning('暂无数据！')
      //清空数据
      data.list = [];
    }
    if (data != null && data.total != 0){
      loading2.value = true
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
          maxEqDataTimeTemp.value = startTimeData.value[index]
        }
        if (num == minEqDataTemp.value){
          minEqDataTimeTemp.value = startTimeData.value[index]
        }
        totalEqData.value += Number(num);
      });
      // 图表显示的位置变化
      nowAddress.value = nowAddressTemp.value
    }else{
      maxEqDataTemp.value = 0;
      minEqDataTemp.value = 0;
      totalEqData.value = 0;
      maxEqDataTimeTemp.value='';
      minEqDataTimeTemp.value='';
      
      loading2.value = false
      ElMessage({
        message: '暂无数据',
        type: 'warning',
      });
    }
 } finally {
   loading.value = false
 }
}

// 初始化折线图
const chartContainer = ref<HTMLElement | null>(null);
let lineChart = null as echarts.ECharts | null; 
const initLineChart = () => {
  if (lineChart) {
    lineChart.dispose(); // 销毁之前的实例
  }
  if (chartContainer.value && instance) {
    lineChart = echarts.init(chartContainer.value);
    lineChart.setOption({
      title: { text: '耗电量趋势图', top: -4},
      tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
      legend: { data: []},
      grid: {left: '3%', right: '4%', bottom: '3%', containLabel: true},
      toolbox: {feature: {  restore:{}, saveAsImage: {}}},
      xAxis: {type: 'category', boundaryGap: false, data:startTimeData.value},
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
//导出Excel
const handleExport1 = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    queryParams.pageNo = 1
    exportLoading.value = true
    queryParams.nowAddress = nowAddress.value
    const axiosConfig = {
      timeout: 0 // 设置超时时间为0
    }
    const data = await EnergyConsumptionApi.exportBusDetailsPageData(queryParams, axiosConfig)
    await download.excel(data, '始端箱能耗排名历史数据.xlsx')
  } catch (error) {
    // 处理异常
    console.error('导出失败：', error)
  } finally {
    exportLoading.value = false
  }
}
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
  var tooltipContent = '';
  params.forEach(function(item) {
    switch( item.seriesName ){
      case '耗电量':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' kWh';
        break;
    }
  });
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
  if(row.type != null  && row.type == 6){
    queryParams.busId = undefined
    queryParams.devkey = row.unique
    findFullName(navList.value, row.unique, fullName => {
      nowAddressTemp.value = fullName
    });
    handleQuery();
  }
}

// 得到位置全名
function findFullName(data, targetUnique, callback, fullName = '') {
  for (let item of data) {
    const newFullName = fullName === '' ? item.name : fullName + '-' + item.name;
    if (item.unique === targetUnique) {
      callback(newFullName);
    }
    if (item.children && item.children.length > 0) {
      findFullName(item.children, targetUnique, callback, newFullName);
    }
  }
}

// 接口获取机房导航列表
const getNavList = async() => {
  const res = await IndexApi.getBusMenu()
  navList.value = res
}

/** 搜索按钮操作 */
const handleQuery = async() => {
  await getLineChartData();
  // await getRankChartData();
  initLineChart();
  // initRankChart();
}

/** 初始化 **/ 
onMounted(async () => {
  getNavList()
  // 获取路由参数中的 bus_id
  
  const queryBusId = useRoute().query.busId as string | undefined;
  const queryLocation = useRoute().query.location as string;
  const queryDevkey = useRoute().query.devKey as string | undefined;
  queryParams.busId = queryBusId ? parseInt(queryBusId, 10) : undefined;
  queryParams.devkey =queryDevkey? queryDevkey : undefined;
  if (queryParams.busId != undefined){
    await getLineChartData();
    nowAddress.value = queryLocation;
    
    nowAddressTemp.value = queryLocation;
    initLineChart();
  }
})

</script>

<style scoped>
.nav_header {
    display: flex;
    flex-direction: column;
    align-items: center;
    font-size: 14px;
    padding-top: 28px;
  }
.nav_data{
  padding-left: 5px;
  width: 195px;
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
  text-align: right; /* 文本右对齐 */
  margin-right: 10px; /* 控制冒号后的间距 */
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
</style>