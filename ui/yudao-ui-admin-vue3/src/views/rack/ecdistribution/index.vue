<template>
  <CommonMenu :dataList="navList" @node-click="handleClick" navTitle="机架能耗排名" :showCheckbox="false">
    <template #NavInfo>
      <div class="nav_header">
        <!-- <div class="nav_header_img"><img alt="" src="@/assets/imgs/wmk.jpg" /></div> -->
        <br/>
        <span v-if="nowAddress">{{nowAddress}}</span>
        <br/>
        <span>{{selectTimeRange[0]}} 至 {{selectTimeRange[1]}}</span>
        <br/>
      </div>
      <div class="nav_data">
        <el-statistic title="总耗电量" :value="formatNumber(totalEqData, 1)">
          <template #suffix>kWh</template>
        </el-statistic>
          <br/>
        <el-statistic title="最大耗电量" :value="formatNumber(maxEqDataTemp, 1)">
          <template #suffix>kWh</template>
        </el-statistic>
        <el-statistic v-if="formatNumber(totalEqData, 1) != 0.0" title="发生于" :value="maxEqDataTimeTemp"/>
        <el-statistic v-if="formatNumber(totalEqData, 1) == 0.0" title="发生于" :value="Object('-')"/>
          <br/>
        <el-statistic title="最小耗电量" :value="formatNumber(minEqDataTemp, 1)">
          <template #suffix>kWh</template>
        </el-statistic>
        <el-statistic v-if="formatNumber(totalEqData, 1) != 0.0" title="发生于" :value="minEqDataTimeTemp"/>
        <el-statistic v-if="formatNumber(totalEqData, 1) == 0.0" title="发生于" :value="Object('-')"/>
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
          <el-button type="primary" plain><Icon icon="ep:download" class="mr-5px" /> 导出</el-button>
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
          <div style="height: 58vh;">
            <el-table  
              border
              :data="tableData"
              style="height: 58vh; width: 99.97%;--el-table-border-color: none;border-right: 1px #143275 solid;border-left: 1px #143275 solid;border-bottom: 1px #143275 solid;"
              :highlight-current-row="false"
              :header-cell-style="{ backgroundColor: '#143275', color: '#ffffff', fontSize: '18px', textAlign: 'center', borderLeft: '0.5px #ffffff solid', borderBottom: '1px #ffffff solid' }"
              :cell-style="{ color: '#000000', fontSize: '16px', textAlign: 'center', borderBottom: '0.5px #143275 solid', borderLeft: '0.5px #143275 solid' }"
              :row-style="{ color: '#fff', fontSize: '14px', textAlign: 'center', }"
              empty-text="暂无数据" max-height="818">
              <!-- 动态生成表头 -->
              <template v-for="item in headerData" :key="item.name">
                <el-table-column  label="开始电能">
                  <el-table-column prop="startEleData" label="数值"/>   
                  <el-table-column prop="startTimeData" label="发生时间"/>
                </el-table-column>
                <el-table-column  label="结束电能">
                  <el-table-column prop="endEleData" label="数值"/>   
                  <el-table-column prop="endTimeData" label="发生时间"/>
                </el-table-column>
                <el-table-column v-if="item.name === '耗电量'" label="耗电量">
                  <el-table-column :prop="item.name" label="数值"/>   
                  <el-table-column prop="create_time" label="记录时间"/>
                </el-table-column>
              </template>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </template>
  </CommonMenu>

</template>

<script setup lang="ts">
import { ElTree, ElMessage } from 'element-plus'
import * as echarts from 'echarts';
import { onMounted } from 'vue'
import { CabinetApi } from '@/api/cabinet/info'
import { IndexApi } from '@/api/rack/index'
import { formatDate, endOfDay, convertDate, addTime, betweenDay } from '@/utils/formatTime'
import { EnergyConsumptionApi } from '@/api/rack/energyConsumption'

defineOptions({ name: 'ECDistribution' })

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
const queryParams = reactive({
  rackId: undefined as number | undefined,
  granularity: 'day',
  // 进入页面原始数据默认显示最近2周
  timeRange: ['', ''],
})

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
loading.value = true
 try {
    // 格式化时间范围 加上23:59:59的时分秒 
    queryParams.timeRange[0] = formatDate(endOfDay(convertDate(selectTimeRange.value[0])))
    // 结束时间的天数多加一天 ，  一天的毫秒数
    const oneDay = 24 * 60 * 60 * 1000;
    queryParams.timeRange[1] = formatDate(endOfDay(addTime(convertDate(selectTimeRange.value[1]), oneDay )))

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

// 处理数据后有几位小数点
function formatNumber(value, decimalPlaces) {
    if (!isNaN(value)) {
        return value.toFixed(decimalPlaces);
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
   if(row.type != null  && row.type == 5){
    queryParams.rackId = row.id
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
  const res = await CabinetApi.getRoomList({})
  let arr = [] as any
  for (let i=0; i<res.length;i++){
  var temp = await IndexApi.getRackAll({id : res[i].id})
  arr = arr.concat(temp);
  }
  navList.value = arr
}

/** 搜索按钮操作 */
const handleQuery = async() => {
  await getLineChartData();
  initLineChart();
}

/** 初始化 **/
onMounted(async () => {
  getNavList()
  // 获取路由参数中的 pdu_id
  const queryRackId = useRoute().query.rackId as string  | undefined;
  const queryLocation = useRoute().query.location as string;
  queryParams.rackId = queryRackId ? parseInt(queryRackId, 10) : undefined;
  if (queryParams.rackId != undefined){
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
    font-size: 13px;
    padding-top: 28px;
  }
  .nav_header_img {
    width: 110px;
    height: 110px;
    border-radius: 50%;
    display: flex;
    justify-content: center;
    align-items: center;
    border: 1px solid #555;
  }

  img {
      width: 75px;
      height: 75px;
  }

.nav_data{
  padding-left: 50px;
}

  .line {
    height: 1px;
    margin-top: 28px;
    margin-bottom: 20px;
    background: linear-gradient(297deg, #fff, #dcdcdc 51%, #fff);
  }

  
</style>