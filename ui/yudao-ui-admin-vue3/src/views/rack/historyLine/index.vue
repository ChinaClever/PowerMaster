<template>
 <CommonMenu :dataList="navList" @node-click="handleClick" navTitle="机架电力分析" :showCheckbox="false">
    <template #NavInfo>
      <br/>    <br/> 
      <div class="nav_data">
        <div class="carousel-container">
          <!-- <el-carousel :interval="2500" motion-blur height="150px" arrow="never" trigger="click">
            <el-carousel-item v-for="(item, index) in carouselItems" :key="index">
              <img width="auto" height="auto" :src="item.imgUrl" alt="" class="carousel-image" />
            </el-carousel-item>
          </el-carousel> -->
        </div> 
        <div class="nav_header">
          <span v-if="nowAddress">{{nowAddress}}</span>
          <br/>
          <template v-if="queryParams.granularity == 'realtime' && queryParams.timeRange != null">
            <span>{{queryParams.timeRange[0]}}</span>
            <span>至</span>
            <span>{{queryParams.timeRange[1]}}</span>
          </template>
          <br/>
        </div>
        <div class="nav_content" v-if="queryParams.granularity == 'realtime' ">
        <el-descriptions title="全部机架有功功率极值记录" direction="vertical" :column="1" border >
          <el-descriptions-item label="有功功率最大值 | 发生时间">
            <span>{{ formatNumber(maxActivePowDataTemp, 3) }} kWh</span> <br/>
            <span v-if="maxActivePowDataTimeTemp">{{ maxActivePowDataTimeTemp }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="有功功率最小值 | 发生时间">
            <span>{{ formatNumber(minActivePowDataTemp, 3) }} kWh</span><br/>
            <span v-if="minActivePowDataTimeTemp">{{ minActivePowDataTimeTemp }}</span>
          </el-descriptions-item>
        </el-descriptions>
        </div>
      </div>
    </template>
    <template #ActionBar>
      <el-tabs v-model="activeName">
        <el-tab-pane label="原始数据" name="realtimeTabPane"/>
        <el-tab-pane label="小时极值数据" name="hourExtremumTabPane"/>
        <el-tab-pane label="天极值数据" name="dayExtremumTabPane"/>
      </el-tabs>
       <!-- 搜索工作栏 -->
       <el-form
         class="-mb-15px"
         :model="queryParams"
         ref="queryFormRef"
         :inline="true"
         label-width="70px"
       >

      <el-form-item label="时间段" prop="timeRange" >
          <el-date-picker
            value-format="YYYY-MM-DD HH:mm:ss"
            v-model="queryParams.timeRange"
            type="datetimerange"
            :shortcuts="activeName === 'realtimeTabPane' ? shortcuts : (activeName === 'hourExtremumTabPane' ? shortcuts1 : (activeName === 'dayExtremumTabPane' ? shortcuts2 : []))"
            range-separator="-"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            :disabled-date="disabledDate"
            class="!w-350px"
          />
                 <!-- @change="handleDayPick" -->
        </el-form-item>
         
         <el-form-item >
           <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
         </el-form-item>
       </el-form>
    </template>
    <template #Content>
      <div v-loading="loading">
        <el-tabs v-model="activeName1">
          <el-tab-pane label="图表" name="myChart">
            <div ref="chartContainer" id="chartContainer" style="width: 70vw; height: 65vh;"></div>
          </el-tab-pane>
          <el-tab-pane label="数据" name="myData">
            <div style="height: 67vh;">
            <el-table  
              border
              :data="tableData"
              style="height: 67vh; width: 99.97%;--el-table-border-color: none;border-right: 1px #143275 solid;border-left: 1px #143275 solid;border-bottom: 1px #143275 solid;"
              :highlight-current-row="false"
              :header-cell-style="{ backgroundColor: '#143275', color: '#ffffff', fontSize: '18px', textAlign: 'center', borderLeft: '0.5px #ffffff solid', borderBottom: '1px #ffffff solid' }"
              :cell-style="{ color: '#000000', fontSize: '16px', textAlign: 'center', borderBottom: '0.5px #143275 solid', borderLeft: '0.5px #143275 solid' }"
              :row-style="{ color: '#fff', fontSize: '14px', textAlign: 'center', }"
              empty-text="暂无数据" max-height="818">
              <el-table-column prop="create_time" label="记录时间" />
              <!-- 动态生成表头 -->
              <template v-for="item in headerData" :key="item.name">
                <el-table-column v-if="item.name === '最大有功功率'" label="有功功率最大值">
                  <el-table-column :prop="item.name" label="数值"/>   
                  <el-table-column prop="totalActivePowMaxTimeData" label="发生时间"/>
                </el-table-column>
                <el-table-column v-else-if="item.name === '最小有功功率'" label="有功功率最小值">
                  <el-table-column :prop="item.name" label="数值"/>   
                  <el-table-column prop="totalActivePowMinTimeData" label="发生时间"/>
                </el-table-column>
                <el-table-column v-else-if="item.name === '最大视在功率'" label="视在功率最大值">
                  <el-table-column :prop="item.name" label="数值"/>   
                  <el-table-column prop="totalApparentPowMaxTimeData" label="发生时间"/>
                </el-table-column>
                 <el-table-column v-else-if="item.name === '最小视在功率'" label="视在功率最小值">
                  <el-table-column :prop="item.name" label="数值"/>   
                  <el-table-column prop="totalApparentPowMinTimeData" label="发生时间"/>
                </el-table-column>
                <el-table-column v-else :prop="item.name" :label="item.name"/>   
              </template>
            </el-table>
            </div>
          </el-tab-pane>
        </el-tabs>
        
        <!-- <el-empty v-show="!isHaveData" description="暂无数据" /> -->
      </div>
    </template>
  </CommonMenu>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts';
import { onMounted } from 'vue'
import { HistoryDataApi } from '@/api/rack/historydata'
import { formatDate } from '@/utils/formatTime'
import { CabinetApi } from '@/api/cabinet/info'
import { IndexApi } from '@/api/rack/index'
/** 机柜历史曲线 */
defineOptions({ name: 'CabinetHistoryLine' })
const navList = ref([]) as any // 左侧导航栏树结构列表
const nowAddress = ref('')// 导航栏的位置信息
const nowAddressTemp = ref('')// 暂时存储点击导航栏的位置信息 确认有数据再显示
const activeName = ref('realtimeTabPane') // tab默认显示
const activeName1 = ref('myChart')
const instance = getCurrentInstance()
const tableData = ref<Array<{ }>>([]) // 列表数据
const headerData = ref<any[]>([])
const needFlush = ref(0) // 是否需要刷新图表
const queryParams = reactive({
  rackId: undefined as number | undefined,
  granularity: 'realtime',
  timeRange: defaultHourTimeRange(1) as any,
})
const loading = ref(false) // 列表的加载中

// 时间段快捷选项
const shortcuts = [
  {
    text: '最近一小时',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 1)
      return [start, end]
    },
  },
  {
    text: '最近十二小时',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 12)
      return [start, end]
    },
  },
  {
    text: '最近一天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 24)
      return [start, end]
    },
  },
  {
    text: '最近两天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 48)
      return [start, end]
    },
  },
]
const shortcuts1 = [
  {
    text: '最近一天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 24)
      return [start, end]
    },
  },
  {
    text: '最近三天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 72)
      return [start, end]
    },
  },
  {
    text: '最近七天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 168)
      return [start, end]
    },
  },
]
const shortcuts2 = [
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 24*30)
      return [start, end]
    },
  },
  {
    text: '最近三个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 24*30*3)
      return [start, end]
    },
  },
  {
    text: '最近六个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 24*30*6)
      return [start, end]
    },
  },
]

// 处理折线图数据
const createTimeData = ref<string[]>([]);
const totalActivePowData = ref<number[]>([]);
const totalApparentPowData = ref<number[]>([]);

const totalActivePowAvgValueData = ref<number[]>([]);
const totalActivePowMaxValueData = ref<number[]>([]);
const totalActivePowMaxTimeData = ref<string[]>([]);
const totalActivePowMinValueData = ref<number[]>([]);
const totalActivePowMinTimeData = ref<string[]>([]);

const totalApparentPowAvgValueData = ref<number[]>([]);
const totalApparentPowMaxValueData = ref<number[]>([]);
const totalApparentPowMaxTimeData = ref<string[]>([]);
const totalApparentPowMinValueData = ref<number[]>([]);
const totalApparentPowMinTimeData = ref<string[]>([]);

// 侧边栏显示需要
const maxActivePowDataTemp = ref(0);// 最大有功功率 
const maxActivePowDataTimeTemp = ref();// 最大有功功率的发生时间 
const minActivePowDataTemp = ref(0);// 最小有功功率 
const minActivePowDataTimeTemp = ref();// 最小有功功率的发生时间 

/** 查询列表 */
const isHaveData = ref(false);
const getList = async () => {
loading.value = true
 try {
    const data = await HistoryDataApi.getHistoryDataDetails(queryParams);
    if (data != null && data.total != 0){
      isHaveData.value = true
      totalActivePowData.value = data.list.map((item) => formatNumber(item.active_total, 3));
      totalApparentPowData.value = data.list.map((item) => formatNumber(item.apparent_total, 3)); 
      if (activeName.value === 'dayExtremumTabPane'){
        createTimeData.value = data.list.map((item) => formatDate(item.create_time, 'YYYY-MM-DD'));
      }else{
        createTimeData.value = data.list.map((item) => formatDate(item.create_time));
      }
      totalActivePowAvgValueData.value = data.list.map((item) => formatNumber(item.active_total_avg_value, 3));
      totalActivePowMaxValueData.value = data.list.map((item) => formatNumber(item.active_total_max_value, 3));
      totalActivePowMaxTimeData.value = data.list.map((item) => formatDate(item.active_total_max_time));
      totalActivePowMinValueData.value = data.list.map((item) => formatNumber(item.active_total_min_value, 3));
      totalActivePowMinTimeData.value = data.list.map((item) => formatDate(item.active_total_min_time));

      totalApparentPowAvgValueData.value = data.list.map((item) => formatNumber(item.apparent_total_avg_value, 3));
      totalApparentPowMaxValueData.value = data.list.map((item) => formatNumber(item.apparent_total_max_value, 3));
      totalApparentPowMaxTimeData.value = data.list.map((item) => formatDate(item.apparent_total_max_time));
      totalApparentPowMinValueData.value = data.list.map((item) => formatNumber(item.apparent_total_min_value, 3));
      totalApparentPowMinTimeData.value = data.list.map((item) => formatDate(item.apparent_total_min_time));

      maxActivePowDataTemp.value = Math.max(...totalActivePowData.value);
      minActivePowDataTemp.value = Math.min(...totalActivePowData.value);
      totalActivePowData.value.forEach(function(num, index) {
        if (num == maxActivePowDataTemp.value){
          maxActivePowDataTimeTemp.value = createTimeData.value[index]
        }
        if (num == minActivePowDataTemp.value){
          minActivePowDataTimeTemp.value = createTimeData.value[index]
        }
      });
      // 图表显示的位置变化
      nowAddress.value = nowAddressTemp.value

    }else{
      isHaveData.value = false;
      ElMessage({
        message: '暂无数据',
        type: 'warning',
      });
    }
 } finally {
   loading.value = false
 }
}

//初始化折线图
const chartContainer = ref<HTMLElement | null>(null);
let realtimeChart = null as echarts.ECharts | null; 
const initChart = () => {
  if ( isHaveData.value == true ){
    if (chartContainer.value && instance) {
      realtimeChart = echarts.init(chartContainer.value);
      if (realtimeChart) {
        if (activeName.value == 'realtimeTabPane'){
          realtimeChart.setOption({
            title: { text: ''},
            tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
            legend: { data: ['有功功率','视在功率']},
            grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
            toolbox: {feature: { restore:{}, saveAsImage: {}}},
            xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
            yAxis: { type: 'value'},
            series: [
              {name: '有功功率', type: 'line', symbol: 'none', data: totalActivePowData.value},
              {name: '视在功率', type: 'line', symbol: 'none', data: totalApparentPowData.value},
            ],
            dataZoom:[{type: "inside"}],
          });
        }else{
          realtimeChart.setOption( {
            title: {text: ''},
            tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
            legend: { data: ['平均有功功率', '最大有功功率', '最小有功功率','平均视在功率', '最大视在功率', '最小视在功率'],
                  selected: { 平均有功功率: true, 最大有功功率: false, 最小有功功率: false, 平均视在功率: true, 最大视在功率: false, 最小视在功率: false}},
            grid: {left: '3%', right: '4%',bottom: '3%', containLabel: true },
            toolbox: {feature: { restore:{}, saveAsImage: {}}},
            xAxis: {type: 'category', boundaryGap: false, data: createTimeData.value},
            yAxis: { type: 'value'},
            series: [
              { name: '平均有功功率', type: 'line',data: totalActivePowAvgValueData.value},
              { name: '最大有功功率', type: 'line',data: totalActivePowMaxValueData.value, lineStyle: {type: 'dashed'} },
              { name: '最小有功功率',type: 'line',data: totalActivePowMinValueData.value, lineStyle: {type: 'dashed'}},
              { name: '平均视在功率',type: 'line',data:  totalApparentPowAvgValueData.value},
              { name: '最大视在功率', type: 'line', data: totalApparentPowMaxValueData.value, lineStyle: {type: 'dashed'}},
              { name: '最小视在功率',type: 'line',data:  totalApparentPowMinValueData.value, lineStyle: {type: 'dashed'}},
            ],
            dataZoom:[{type: "inside"}],
          });
        }

      }
      // 将 realtimeChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
      instance.appContext.config.globalProperties.realtimeChart = realtimeChart;
    }
  }
  // 每次切换图就要动态生成数据表头
  headerData.value = realtimeChart?.getOption().series as any[];
  updateTableData();
};

// 在组件销毁时手动销毁图表
const beforeUnmount = () => {
    realtimeChart?.dispose(); // 销毁图表实例
};
window.addEventListener('resize', function() {
    realtimeChart?.resize(); 
});

// 监听切换原始数据、极值数据tab
watch( ()=>activeName.value, async(newActiveName)=>{
  if ( newActiveName == 'realtimeTabPane'){
    queryParams.granularity = 'realtime'
    queryParams.timeRange = defaultHourTimeRange(1)
  }else if (newActiveName == 'hourExtremumTabPane'){
    queryParams.granularity = 'hour'
    queryParams.timeRange = defaultHourTimeRange(24)
  }else{
    queryParams.granularity = 'day'
    queryParams.timeRange = defaultHourTimeRange(24*30)
  }
  needFlush.value ++;
});

// 监听颗粒度
watch(() => [activeName.value, needFlush.value], async (newValues) => {
  const [newActiveName] = newValues;
  if ( newActiveName == 'realtimeTabPane'){
    await getList();
    // 销毁原有的图表实例
    beforeUnmount()
    if ( isHaveData.value == true ){
      // 创建新的图表实例
      realtimeChart = echarts.init(document.getElementById('chartContainer'));
      // 设置新的配置对象
      if (realtimeChart) {
        realtimeChart.setOption({
        title: { text: ''},
        tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
        legend: { data: ['有功功率','视在功率']},
        grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
        toolbox: {feature: { restore:{}, saveAsImage: {}}},
        xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
        yAxis: { type: 'value'},
        series: [
          {name: '有功功率', type: 'line', symbol: 'none', data: totalActivePowData.value},
          {name: '视在功率', type: 'line', symbol: 'none', data: totalApparentPowData.value},
        ],
        dataZoom:[{type: "inside"}],
        });
      }
    }

  }else{
    await getList();
    // 销毁原有的图表实例
    beforeUnmount()
    if ( isHaveData.value == true ){
      // 创建新的图表实例
      realtimeChart = echarts.init(document.getElementById('chartContainer'));
      // 设置新的配置对象
      if (realtimeChart) {
        realtimeChart.setOption( {
          title: {text: ''},
          tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
          legend: { data: ['平均有功功率', '最大有功功率', '最小有功功率','平均视在功率', '最大视在功率', '最小视在功率'],
                selected: { 平均有功功率: true, 最大有功功率: false, 最小有功功率: false, 平均视在功率: true, 最大视在功率: false, 最小视在功率: false}},
          grid: {left: '3%', right: '4%',bottom: '3%', containLabel: true },
          toolbox: {feature: { restore:{}, saveAsImage: {}}},
          xAxis: {type: 'category', boundaryGap: false, data: createTimeData.value},
          yAxis: { type: 'value'},
          series: [
            { name: '平均有功功率', type: 'line',data: totalActivePowAvgValueData.value},
            { name: '最大有功功率', type: 'line',data: totalActivePowMaxValueData.value, lineStyle: {type: 'dashed'} },
            { name: '最小有功功率',type: 'line',data: totalActivePowMinValueData.value, lineStyle: {type: 'dashed'}},
            { name: '平均视在功率',type: 'line',data:  totalApparentPowAvgValueData.value},
            { name: '最大视在功率', type: 'line', data: totalApparentPowMaxValueData.value, lineStyle: {type: 'dashed'}},
            { name: '最小视在功率',type: 'line',data:  totalApparentPowMinValueData.value, lineStyle: {type: 'dashed'}},
          ],
          dataZoom:[{type: "inside"}],
        });
      }
    }
  }
  // 每次切换图就要动态生成数据表头
  headerData.value = realtimeChart?.getOption().series as any[];
  updateTableData();
});

// 表格映射图数据
const updateTableData = () => {
  if ( isHaveData.value == true ){
    const data: any[] = [];
    const length = headerData.value[0]?.data?.length || 0;
    for (let i = 0; i < length; i++) {
      const rowData: { [key: string]: any } = {};
      rowData['create_time'] = createTimeData.value[i];
      rowData['totalActivePowMaxTimeData'] = totalActivePowMaxTimeData.value[i];
      rowData['totalActivePowMinTimeData'] = totalActivePowMinTimeData.value[i];
      rowData['totalApparentPowMaxTimeData'] = totalApparentPowMaxTimeData.value[i];
      rowData['totalApparentPowMinTimeData'] = totalApparentPowMinTimeData.value[i];

      for (const item of headerData.value) {
        rowData[item.name] = item.data[i];
      }
      data.push(rowData);
    }
    tableData.value = data;
  }

};

// 给折线图提示框的数据加单位
function customTooltipFormatter(params: any[]) {
  var tooltipContent = ''; // X 轴数值
  params.forEach(function(item) {
    switch( item.seriesName ){
      case '有功功率':
      case '平均有功功率':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' kW  记录时间: ' +params[0].name + '<br/>';
        break;
      case '最大有功功率':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' kW  发生时间: ' +totalActivePowMaxTimeData.value[item.dataIndex] + '<br/>';
        break;
      case '最小有功功率':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' kW  发生时间: ' +totalActivePowMinTimeData.value[item.dataIndex] + '<br/>';
        break;
      case '视在功率':
      case '平均视在功率':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' kVA  记录时间: ' +params[0].name + '<br/>';
        break;
     case '最大视在功率':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' kVA  发生时间: ' +totalApparentPowMaxTimeData.value[item.dataIndex] + '<br/>';
        break;
      case '最小视在功率':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' kVA  发生时间: ' +totalApparentPowMinTimeData.value[item.dataIndex] + '<br/>';
        break;
    }
    
  });
  return tooltipContent;
}

// 原始数据默认查询的时间范围
function defaultHourTimeRange(hour: number){
  // 先获取本地时区偏移量（以分钟为单位，需要转换为毫秒）
  var timezoneOffset = new Date().getTimezoneOffset() * 60 * 1000
  // 计算当前时间和1小时前的时间，并考虑时区偏移量
  var end = new Date(new Date().getTime() - timezoneOffset);
  var start = new Date(end.getTime() - 60 * 60 * 1000 * hour);
  // 格式化时间并返回
  return [start.toISOString().slice(0, 19).replace('T', ' '), end.toISOString().slice(0, 19).replace('T', ' ')]
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

// 处理数据后有几位小数点
function formatNumber(value, decimalPlaces) {
    if (!isNaN(value)) {
        return Number(value).toFixed(decimalPlaces);
    } else {
        return null; // 或者其他默认值
    }
}

/** 搜索按钮操作 */
const handleQuery = async() => {
  await getList();
  // await getRankChartData();
  initChart();
  // initRankChart();
}

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

/** 初始化 **/
onMounted( async () => {
  getNavList()
  // 获取路由参数中的 pdu_id
  const queryRackId = useRoute().query.rackId as string  | undefined;
  const queryLocation = useRoute().query.location as string;
  queryParams.rackId = queryRackId ? parseInt(queryRackId, 10) : undefined;
  if (queryParams.rackId != undefined){
    await handleQuery();
    nowAddress.value = queryLocation
    console.log(queryLocation)
    nowAddressTemp.value = queryLocation
  }
})

</script>

<style scoped>
/*  
// 表格部分样式
// 最外层透明 */
:deep( .el-table),
:deep( .el-table__expanded-cell) {
  background-color: transparent;
  color: #93dcfe;
  font-size: 24px;
}
 
/* 表格内背景颜色  */
:deep( .el-table th),
:deep( .el-table tr),
:deep( .el-table td) {
  background-color: transparent;
  border: 0px;
  color: #93dcfe;
  font-size: 24px;
  height: 5px;
  font-family: Source Han Sans CN Normal, Source Han Sans CN Normal-Normal;
  font-weight: Normal;
}
 
/* // 去掉最下面的那一条线  */
.el-table::before {
  height: 0px;
}
 
/* // 设置表格行高度 */
:deep( .el-table__body tr)
:deep( .el-table__body td) {
  padding: 0;
  height: 54px;
}
 
/* // 修改高亮当前行颜色 */
:deep( .el-table tbody tr:hover>td ){
  background: #063570 !important;
}
 
/* // 取消当前行高亮 */
:deep( .el-table tbody tr) {
  pointer-events: none;
}
 
/* 修改表头样式-加边框 */
/* ::v-deep .el-table__header-wrapper {
  border: solid 1px #04c2ed;
} */
 
/* // 表格斑马自定义颜色 */
:deep(.el-table__row.warning-row)  {
  background: #01205A;
}
 
 
/* 去掉表格里的padding */
:deep(.el-table .cell),
:deep(.el-table th div) {
  padding-left: 0px;
  padding-right: 0px;
  padding-top: 0px;
  padding-bottom: 0px;
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
}
.nav_header {
    display: flex;
    flex-direction: column;
    align-items: center;
    font-size: 14px;
  }

.nav_data{
  padding-left: 5px;
  width: 195px;
}
.nav_content span{
  /* font-size: 18px; */
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