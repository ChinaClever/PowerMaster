<template>
  <el-row :gutter="20">
   <el-col :span="treeWidth" :xs="24">
     <el-input
       v-model="filterText"
       style="width: 190px"
       placeholder=""
     />

     <el-tree
       ref="treeRef"
       style="max-width: 600px"
       class="filter-tree"
       :data="serverRoomArr"
       :props="defaultProps"
       default-expand-all
       show-checkbox
       :filter-node-method="filterNode"
     />
   </el-col>
   <el-col :span="24 - treeWidth" :xs="24">
     <ContentWrap>
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
        <el-form-item label="参数类型" prop="type">
          <el-cascader
            v-model="typeDefaultSelected"
            collapse-tags
            :options="typeSelection"
            collapse-tags-tooltip
            :show-all-levels="true"
            @change="typeCascaderChange"
            class="!w-140px"
          />
        </el-form-item>

        <el-form-item label="时间段" prop="timeRange" >
          <el-date-picker
            value-format="YYYY-MM-DD HH:mm:ss"
            v-model="queryParams.timeRange"
            type="datetimerange"
            :shortcuts="shortcuts"
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
           <el-button type="primary" plain><Icon icon="ep:download" class="mr-5px" /> 导出</el-button>
         </el-form-item>
       </el-form>
     </ContentWrap>
     <!-- 列表 -->
     <ContentWrap style="overflow: visible;">
      <div v-loading="loading" ref="chartContainer" id="chartContainer" style="width: 70vw; height: 58vh;"></div>
    </ContentWrap>
    <ContentWrap style="overflow: visible;">
      <div v-loading="loading1" ref="rankContainer" id="rankContainer" style="width: 70vw; height: 58vh;"></div>
    </ContentWrap>
   </el-col>
  </el-row>

</template>

<script setup lang="ts">
import { ElTree, ElMessage } from 'element-plus'
import * as echarts from 'echarts';
import { onMounted } from 'vue'
import { formatDate } from '@/utils/formatTime'
import { EnergyConsumptionApi } from '@/api/pdu/energyConsumption'
import { HistoryDataApi } from '@/api/pdu/historydata'
import { da } from 'element-plus/es/locale';

defineOptions({ name: 'ECDistribution' })
const activeName = ref('dayTabPane')
const instance = getCurrentInstance();
const queryParams = reactive({
  pduId: 22,
  outletId: undefined as number | undefined,
  type: 'total',
  granularity: 'day',
  ipAddr: undefined,
  // 进入页面原始数据默认显示最近2周
  timeRange: defaultHourTimeRange(24 * 14)
})

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

const shortcuts = [
  {
    text: '最近2周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 24 * 7 * 2)
      return [start, end]
    },
  },
  {
    text: '最近1个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 24 * 7 * 4)
      return [start, end]
    },
  },
  {
    text: '最近3个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 24 * 7 * 4 * 3)
      return [start, end]
    },
  },
  {
    text: '最近半年',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 24 * 7 * 4 * 6)
      return [start, end]
    },
  },
  {
    text: '最近1年',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 24 * 7 * 4 * 12)
      return [start, end]
    },
  },
]

const serverRoomArr =  [
 {
   value: '1',
   label: '机房1',
   children: [
     {
       value: '1-1',
       label: '柜列1',
       children: [
       {
         value: '1-1-1',
         label: '机柜1',
       },
       {
         value: '1-1-2',
         label: '机柜2',
       },]
     },
   ],
 },
 {
   value: '2',
   label: '机房2',
   children: [
     {
       value: '2-1',
       label: '柜列1',
       children: [
       {
         value: '2-1-1',
         label: '机柜1',
       },
       {
         value: '2-1-2',
         label: '机柜2',
       },]
     },
   ],
 },
 {
   value: '3',
   label: '机房3',
   children: [
     {
       value: '3-1',
       label: '柜列1',
       children: [
       {
         value: '3-1-1',
         label: '机柜1',
       },
       {
         value: '3-1-2',
         label: '机柜2',
       },]
     },
   ],
 },
]
//折叠功能
let treeWidth = ref(3)
let isCollapsed = ref(0);
const toggleCollapse = () => {
 treeWidth.value = isCollapsed.value == 0 ? 3 : 0;
};
interface Tree {
 [key: string]: any
}
const filterText = ref('')
const treeRef = ref<InstanceType<typeof ElTree>>()
const filterNode = (value: string, data: Tree) => {
 if (!value) return true
 return data.label.includes(value)
}
const defaultProps = {
 children: 'children',
 label: 'label',
}
watch(filterText, (val) => {
 treeRef.value!.filter(val)
})
const loading = ref(true) 
const loading1 = ref(true) 
// 总/输出位筛选
const typeDefaultSelected = ref(['total'])
const typeSelection = ref([]) as any;
const typeCascaderChange = async (selected) => {
  queryParams.type = selected[0];
  if (selected[0] === 'outlet'){
    queryParams.outletId = selected[1];
  }
  await getLineChartData();
  initLineChart();
}

// 监听切换日周月tab切换
watch( ()=>activeName.value, async(newActiveName)=>{
  if ( newActiveName == 'dayTabPane'){
    queryParams.granularity = 'day'
    queryParams.timeRange = defaultHourTimeRange(24 * 14)
  }else if (newActiveName == 'weekTabPane'){
    queryParams.granularity = 'week'
    queryParams.timeRange = defaultHourTimeRange(24 * 7 * 4 * 3)//三个月
  }else{
    queryParams.granularity = 'month'
    queryParams.timeRange = defaultHourTimeRange(24 * 7 * 4 * 12)//一年
  }
  handleQuery();
});

// 折线图数据
const startEleData = ref<number[]>([]);
const startTimeData = ref<string[]>([]);
const endEleData = ref<number[]>([]);
const endTimeData = ref<string[]>([]);
const eqData = ref<number[]>([]);
const createTimeData = ref<string[]>([]);
const totalEqData = ref(0);
// 获取数据
const getLineChartData =async () => {
loading.value = true
 try {
    const data = await EnergyConsumptionApi.getEQDataDetails(queryParams);
    if (data != null && data.total != 0){
      totalEqData.value = 0;
      startEleData.value = data.list.map((item) => formatNumber(item.start_ele, 1));
      startTimeData.value = data.list.map((item) => formatDate(item.start_time, 'YYYY-MM-DD'));
      endEleData.value = data.list.map((item) => formatNumber(item.end_ele, 1));
      endTimeData.value = data.list.map((item) => formatDate(item.end_time, 'YYYY-MM-DD'));
      eqData.value = data.list.map((item) => formatNumber(item.eq_value, 1));
      createTimeData.value = data.list.map((item) => formatDate(item.create_time, 'YYYY-MM-DD'));
      eqData.value.forEach(function(num) {
        totalEqData.value += Number(num);
      });
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

// 排行榜图数据
const outletIdData = ref<string[]>([]);
const sumEqData = ref<number[]>([]);
const getRankChartData =async () => {
loading1.value = true
 try {
    const data = await EnergyConsumptionApi.getOutletsEQData(queryParams);
    if (data != null && data.total != 0){
      outletIdData.value = data.map((item) => '输出位 '+item.outlet_id);
      sumEqData.value = data.map((item) => formatNumber(item.sum_eq_value, 1));
    }else{
      ElMessage({
        message: '暂无数据',
        type: 'warning',
      });
    }
 } finally {
   loading1.value = false
 }
}

// 初始化折线图
const chartContainer = ref<HTMLElement | null>(null);
let lineChart = null as echarts.ECharts | null; 
const initLineChart = () => {
  if (chartContainer.value && instance) {
    lineChart = echarts.init(chartContainer.value);
    lineChart.setOption({
      title: { text: '[位置]总耗电量'+totalEqData.value+'kWh', top: -4},
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
  }
};

// 初始化输出位排行榜图表
const rankContainer = ref<HTMLElement | null>(null);
let rankChart = null as echarts.ECharts | null; 
const initRankChart = () => {
  if (rankContainer.value && instance) {
    rankChart = echarts.init(rankContainer.value);
    rankChart.setOption({
      title: { text: '输出位耗电量排行', top: -4},
      tooltip: { show: false, trigger: 'axis',  axisPointer: { type: "shadow"} },
      grid: {left: '3%', right: '4%', bottom: '3%', containLabel: true},
      toolbox: {feature: {saveAsImage: {}}},
      xAxis: {
        type: "value",
        axisLine: {
          show: false,
        },
        axisTick: {
          show: false,
        },
        //不显示X轴刻度线和数字
        splitLine: { show: false },
        axisLabel: { show: false },
      },
      yAxis: {
        type: "category",
        data: outletIdData.value,
        //升序
        inverse: true,
        splitLine: { show: false },
        axisLine: {
          show: false,
        },
        axisLabel: { fontSize: 16 },
        axisTick: {
          show: false,
        },
        //key和图间距
        offset: 10,
        //动画部分
        animationDuration: 300,
        animationDurationUpdate: 300,
        //key文字大小
        nameTextStyle: {
          fontSize: 15,
        },
      },
      series: [
        {
          //柱状图自动排序，排序自动让Y轴名字跟着数据动
          realtimeSort: true,
          name: "耗电量",
          type: "bar",
          data: sumEqData.value,
          barWidth: 20,
          barGap: 5,
          smooth: true,
          valueAnimation: true,
          label: {
            show: true,
            position: "right",
            valueAnimation: true,
            offset: [5, -2],
            color: "#333",
            fontSize: 16,
            // formatter: '{value}kWh'
          },
          emphasis: {
            itemStyle: {
              borderRadius: 7,
            },
            //颜色样式部分
              borderRadius: 7,
              color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                { offset: 0, color: "#3977E6" },
                { offset: 1, color: "#37BBF8" },
              ]),
          },
        },
      ],
      //动画部分
      animationDuration: 0,
      animationDurationUpdate: 3000,
      animationEasing: "linear",
      animationEasingUpdate: "linear",
    });
    instance.appContext.config.globalProperties.rankChart = rankChart;
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

// 获取参数类型最大值 例如lineId=6 表示下拉框为L1~L6
const getTypeMaxValue = async () => {
    const data = await HistoryDataApi.getTypeMaxValue()
    const outletIdMaxValue = data.outlet_id_max_value;
    const typeSelectionValue  = [
    {
      value: "total",
      label: '总'
    },
    {
      value: "outlet",
      label: '输出位',
      children: (() => {
        const outlets: { value: any; label: string; }[] = [];
        for (let i = 1; i <= outletIdMaxValue; i++) {
          outlets.push({ value: `${i}`, label: `${i}` });
        }
        return outlets;
      })(),
    },
  ]
  typeSelection.value = typeSelectionValue;
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
  rankChart?.resize();  
});

/** 搜索按钮操作 */
const handleQuery = async() => {
  await getLineChartData();
  await getRankChartData();
  initLineChart();
  initRankChart();
}

/** 初始化 **/
onMounted(async () => {
  getTypeMaxValue();
  await getLineChartData();
  await getRankChartData();
  initLineChart();
  initRankChart();
})

</script>