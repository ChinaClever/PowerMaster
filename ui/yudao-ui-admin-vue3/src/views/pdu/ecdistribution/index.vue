<template>
  <CommonMenu1  :dataList="navList" @node-click="handleClick" navTitle="PDU能耗趋势" :showCheckbox="false" placeholder="如:192.168.1.96-0" :hightCurrent="true" nodeKey="unique" :currentKey="currentKey" :highlightTypes="[4]" :defaultExpandedKeys="defaultExpandedKeys">
    <template #NavInfo>
      <br/>   
      <div class="nav_data">

        <div class="nav_header">      
          <span v-if="nowAddress">{{nowAddress=='null'?'':nowAddress}}</span>
          <span v-if="nowLocation">( {{nowLocation}} ) </span>
        </div>
        <br/> 
        <div class="descriptions-container"  v-if="maxEqDataTimeTemp" style="font-size: 14px;">
        <!-- <div class="description-item" >
          <span class="label">开始日期 :</span>
          <span >{{selectTimeRange[0]}}</span>
        </div>
        <div class="description-item" >
          <span class="label">结束日期 :</span>
          <span >{{selectTimeRange[1]}}</span>
        </div> -->
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
          value-format="YYYY-MM-DD"
          v-model="selectTimeRange"
          type="daterange"
          :shortcuts="shortcuts"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :disabled-date="disabledDate"
          class="!w-350px"
          @change="handleDayPick"
        />
      </el-form-item>

        <el-form-item >
          <el-button @click="handleQuery" style="background-color: #00778c;color:#ffffff;font-size: 13px;"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>

        </el-form-item>
        <el-button type="success" plain @click="handleExport1" :loading="exportLoading" style="float: right;margin-right: 10px;background-color: #00778c;color:#ffffff;font-size: 13px;" >
             <Icon icon="ep:download" class="mr-5px" /> 导出
           </el-button>
      </el-form>
    </template>
      <!-- 列表 -->
      <template #Content>
        <div v-loading="loading">
      <el-tabs v-model="activeName1">
        <el-tab-pane v-if="loading2" label="图表" name="lineChart">
          <div  v-loading="loading" ref="chartContainer" id="chartContainer" style="width: 70vw; height: 58vh;"></div>
        </el-tab-pane>
        <el-tab-pane  v-if="loading2"  v-loading="loading" label="数据" name="lineChartData">
          <div   style="height: 70vh;">
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
    </div>
    </template>
    <!-- <template #Content>
      <div  v-if="loading3" style="overflow: visible;">
        <div v-loading="loading1" ref="rankContainer" id="rankContainer" style="width: 70vw; height: 90vh;"></div>
      </div>
    </template> -->
  </CommonMenu1>

</template>

<script setup lang="ts">
import { dayjs, ElMessage } from 'element-plus'
import * as echarts from 'echarts';
import { onMounted } from 'vue'
import { CabinetApi } from '@/api/cabinet/info'
import { formatDate, endOfDay, convertDate, addTime, betweenDay, startOfDay } from '@/utils/formatTime'
import { EnergyConsumptionApi } from '@/api/pdu/energyConsumption'
import { HistoryDataApi } from '@/api/pdu/historydata'
import PDUImage from '@/assets/imgs/PDU.jpg';
import { pa } from 'element-plus/es/locale';
import download from '@/utils/download'
defineOptions({ name: 'ECDistribution' })
// import  CommonMenu1 from './CommonMenu1.vue'
import { now } from 'lodash-es';
import { unique } from 'element-plus/es/utils';
import { cu } from 'dist-prod/assets/installCanvasRenderer-WHaFMoQ9';

const navList = ref([]) as any // 左侧导航栏树结构列表
const nowAddress = ref()// 导航栏的位置信息
const nowLocation = ref('')// 导航栏的位置信息
const nowAddressTemp = ref('')// 暂时存储点击导航栏的位置信息 确认有数据再显示
const nowLocationTemp = ref('')// 暂时存储点击导航栏的位置信息 确认有数据再显示
if(history.state.location!=null){
  nowLocationTemp.value = history.state.location
}
const activeName = ref('dayTabPane')
const activeName1 = ref('lineChart')
const tableData = ref<Array<{ }>>([]); // 折线图表格数据
const headerData = ref<any[]>([]);
const instance = getCurrentInstance();
const message = useMessage() // 消息弹窗
const exportLoading = ref(false)
const loading3 =ref(false)
const selectTimeRange = ref()
const carouselItems = ref([
      { imgUrl: PDUImage},
      { imgUrl: PDUImage},
      { imgUrl: PDUImage},
      { imgUrl: PDUImage},
    ]);//侧边栏轮播图图片路径
const queryParams = reactive({
  pageNo: 1,
  pageSize: 15,
  pduId: undefined as number | undefined,
  outletId: undefined as number | undefined,
  type: 'total',
  granularity: 'day',
  ipAddr: undefined,
  cascadeAddr: undefined,
  nowAddress: undefined as string | undefined,
  // 进入页面原始数据默认显示最近2周
  timeRange: ['', ''],
})

// 默认查询的日期范围，单位：天
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

// 默认查询的日期范围，单位：月
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
  }
]

const loading = ref(false) 
const loading1 = ref(false) 
const loading2 = ref(false)
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

let lastdate=null;
let lastWeekOrMonth=null;
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
  // if ( newActiveName == 'dayTabPane'){
  //   queryParams.granularity = 'day'
  //   // selectTimeRange.value = defaultDayTimeRange(14)
  // }else if (newActiveName == 'weekTabPane'){
  //   queryParams.granularity = 'week'
  //   // selectTimeRange.value = defaultMonthTimeRange(3)
  // }else{
  //   queryParams.granularity = 'month'
  //   // selectTimeRange.value = defaultMonthTimeRange(12)
  // }
  handleQuery();
});
console.log("history===",history.state)
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
const eqData = ref<number[]>([]);// 耗电量数组
const createTimeData = ref<string[]>([]);
const totalEqData = ref(0);
const maxEqDataTemp = ref(0);// 最大耗电量 
const maxEqDataTimeTemp = ref();// 最大耗电量的发生日期 
const minEqDataTemp = ref(0);// 最小耗电量 
const minEqDataTimeTemp = ref();// 最小耗电量的发生日期


// 获取折线图数据
const getLineChartData =async () => {
loading.value = true

 try {
    // 格式化日期范围 加上23:59:59的时分秒 
    queryParams.timeRange[0] = formatDate(endOfDay(convertDate(selectTimeRange.value[0])))
    // 结束日期的天数多加一天 ，  一天的毫秒数
    const oneDay = 24 * 60 * 60 * 1000;
    queryParams.timeRange[1] = formatDate(endOfDay(addTime(convertDate(selectTimeRange.value[1]),oneDay)))

    const data = await EnergyConsumptionApi.getEQDataDetails(queryParams);
    
    if (data != null && data.total != 0){
      loading2.value=true
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
      });

      // 图表显示的位置变化
      nowAddress.value = nowAddressTemp.value
      nowLocation.value = nowLocationTemp.value
    }else{
      loading2.value=false
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
  // 
loading1.value = true
 try {
    // 格式化日期范围 加上23:59:59的时分秒 
    queryParams.timeRange[0] = formatDate(endOfDay(convertDate(selectTimeRange.value[0])))
    // 结束日期的天数多加一天 ，  一天的毫秒数
    const oneDay = 24 * 60 * 60 * 1000;
    queryParams.timeRange[1] = formatDate(endOfDay(addTime(convertDate(selectTimeRange.value[1]), oneDay )))

    const data = await EnergyConsumptionApi.getOutletsEQData(queryParams);
    // console.log(data.length)
    if (data != null && data.total != 0){
      if(data.length==0){
        loading3.value=false
      }
      else{
        loading3.value=true
      }
      outletIdData.value = data.map((item) => {
        if (item.outlet_id < 10) {
          return '输出位 0' + item.outlet_id;
        } else {
          return '输出位 ' + item.outlet_id;
        }
      });
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
  if (lineChart) {
    lineChart.dispose(); // 销毁之前的实例
  }
  if (chartContainer.value && instance) {
    lineChart = echarts.init(chartContainer.value);
    lineChart.setOption({
      title: { text: "耗电量趋势图", top: -4},
      tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
      legend: { data: []},
      grid: {left: '3%', right: '4%', bottom: '3%', containLabel: true},
      toolbox: {feature: {  restore:{}, saveAsImage: {}}},
      xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
      yAxis: { type: 'value', name: "kWh"},
      series: [{name: '耗电量', type: 'line', symbol:"none",data: eqData.value,itemStyle:{normal:{lineStyle:{color:'#C8603A'},color:'#C8603A'}}}],
      dataZoom:[{type: "inside"}],
    });
    instance.appContext.config.globalProperties.lineChart = lineChart;
    // 每次切换图就要动态生成数据表头
    headerData.value = lineChart?.getOption().series as any[];
    updateTableData();
  }
};

// 初始化输出位排行榜图表
const rankContainer = ref<HTMLElement | null>(null);
let rankChart = null as echarts.ECharts | null; 
const initRankChart = () => {
  
  if (rankChart) {
    rankChart.dispose(); // 销毁之前的实例
  }
  
  if (rankContainer.value && instance) {
    rankChart = echarts.init(rankContainer.value);
    rankChart.setOption({
      title: { text: '输出位耗电量排行图', top: -4},
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
            normal: {
              show: true,
              position: "right",
              valueAnimation: true,
              offset: [5, -2],
              textStyle: {
                color: "#333",
                fontSize: 16,
              },
              formatter: '{c} kWh'  // 设置数据标签的格式化字符串
            },
          },
          itemStyle: {
            emphasis: {
              barBorderRadius: 7,
            },
            //颜色样式部分
             normal: {
              barBorderRadius: 7,
              color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                { offset: 0, color: "#3977E6" },
                { offset: 1, color: "#37BBF8" },
              ]),
            },
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
        return Number(value).toFixed(decimalPlaces);
    } else {
        return null; // 或者其他默认值
    }
}

// 给折线图提示框的数据加单位
function customTooltipFormatter(params: any[]) {
  var tooltipContent = '';
  // console.log(params)
  // params.forEach(function(item) {
  //   switch( item.seriesName ){
  //     case '耗电量':
  //       tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' kWh';
  //       break;
  //   }
  // });
  tooltipContent += params[0].marker + ' ' +'记录时间: ' + params[0].name;
  tooltipContent += "&nbsp;&nbsp;&nbsp;&nbsp;" + params[0].seriesName + ': ' + params[0].value + ' kWh';
  tooltipContent += '<br/>&nbsp;&nbsp;&nbsp;&nbsp;'+'开始时间: ' + tableData.value[params[0].dataIndex].startTimeData;
  tooltipContent += '&nbsp;&nbsp;&nbsp;&nbsp;开始电能: ' + tableData.value[params[0].dataIndex].startEleData + ' kWh';
  tooltipContent += '<br/>&nbsp;&nbsp;&nbsp;&nbsp;'+'结束时间: ' + tableData.value[params[0].dataIndex].endTimeData;
  tooltipContent += '&nbsp;&nbsp;&nbsp;&nbsp;结束电能: ' + tableData.value[params[0].dataIndex].endEleData + ' kWh';
  // tooltipContent += '<br/>时间: ' + params[0].name;
  return tooltipContent;
}

// 处理日期选择不超过xxx范围
const handleDayPick = () => {
  // if (activeName.value=='weekTabPane'){
  //   // 计算两个日期之间的天数差
  //   const diffDays = betweenDay(convertDate(selectTimeRange.value[0]), convertDate(selectTimeRange.value[1]))
  //   // 如果天数差不超过7天，则重置选择的日期
  //   if (diffDays < 7) {
  //     selectTimeRange.value = defaultDayTimeRange(7)
  //     ElMessage({
  //       message: '日期选择不少于7天,已默认选择最近一周',
  //       type: 'warning',
  //     })
  //   }
  // }
  // if (activeName.value=='monthTabPane'){
  //   // 计算两个日期之间的天数差
  //   const diffDays = betweenDay(convertDate(selectTimeRange.value[0]), convertDate(selectTimeRange.value[1]))
  //   // 如果天数差超过30天，则重置选择的日期
  //   if (diffDays < 30) {
  //     selectTimeRange.value = defaultMonthTimeRange(1)
  //     ElMessage({
  //       message: '日期选择不少于1个月,已默认选择最近一个月',
  //       type: 'warning',
  //     })
  //   }
  // }
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
  // 设置date的日期为0时0分0秒，以便与today进行比较
  date.setHours(0, 0, 0, 0);
  // 如果date在今天之后，则禁用
  return date > today;
}

function resize() {
  lineChart?.resize();
  rankChart?.resize();  
}
window.addEventListener('resize', resize);

// 导航栏选择后触发
const handleClick = async (row) => {
   if(row.type != null  && row.type == 4){
    queryParams.pduId = undefined
    queryParams.ipAddr = row.ip
    queryParams.cascadeAddr = row?.unique?.split("-")[1];
    findFullName(navList.value, row.unique, fullName => {
      nowAddressTemp.value = fullName
      nowLocationTemp.value = row.unique
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
  let arr = [] as any
  var temp = await CabinetApi.getRoomPDUList()
  arr = arr.concat(temp);
  navList.value = arr
  if(history.state.location!=null){
    console.log(arr,"=================arr")
    console.log(defaultExpandedKeys.value,"=================defaultExpandedKeys")
    setDefaultCheckedKeys(arr);
    console.log("currentKey================",currentKey.value)
  }
}

const currentKey=ref()
const defaultExpandedKeys = ref([])
if(history.state.location!=null){
  currentKey.value = history.state.location;
}
function setDefaultCheckedKeys(arr) {
  if(arr==null||arr.length == 0)return false;
  for(let i = 0; i < arr.length; i++) {
    if(arr[i].type==4){
      if(arr[i].ip==history.state.location){
        defaultExpandedKeys.value.push(arr[i].unique);
        return true;
      }
    }else{
      if(setDefaultCheckedKeys(arr[i].children)){
        defaultExpandedKeys.value.push(arr[i].unique);
        return true;
      }
    }
  }
  return false;
}

/** 搜索按钮操作 */
const handleQuery = async() => {
  await getLineChartData();
  // await getRankChartData();
  initLineChart();
  initRankChart();
}

const start = ref('')
const end = ref('')


/** 初始化 **/
onMounted(async () => {
  getNavList()
  getTypeMaxValue();
  start.value = history.state.start as string;
  end.value = history.state.end as string;
  if(start.value!=null&&end.value!=null&&start.value!=''&&end.value!=''){
    const selectedStartTime = formatDate(endOfDay(convertDate(start.value)))
    const selectedEndTime = formatDate(endOfDay(convertDate(end.value)))
    selectTimeRange.value = [selectedStartTime, selectedEndTime];
  }else{
    selectTimeRange.value = defaultDayTimeRange(7);
  }
  
  // 获取路由参数中的 pdu_id
  const queryPduId = history.state.pduId as string | undefined;
  const queryAddress = history.state.address as string;
  queryParams.pduId = queryPduId ? parseInt(queryPduId, 10) : undefined;
  console.log('11111撒大声地',queryParams.pduId)
  if (queryParams.pduId != undefined){
    await getLineChartData();
    // await getRankChartData();
    nowAddress.value = queryAddress;
    nowAddressTemp.value = queryAddress;
    initLineChart();
    initRankChart();
  }
})

onBeforeUnmount(() => {
  rankChart?.dispose()
  lineChart?.dispose()
  window.removeEventListener('resize', resize)
})
//导出Excel
const handleExport1 = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    queryParams.nowAddress =nowAddress.value
    queryParams.pageNo = 1
    exportLoading.value = true
    const axiosConfig = {
      timeout: 0 // 设置超时时间为0
    }
    const data = await EnergyConsumptionApi.exportOutletsPageData(queryParams, axiosConfig)
    await download.excel(data, 'PDU能耗趋势.xlsx')
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
  padding-left: 5px;
  width: 195px;
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
  /deep/ .el-tabs__item.is-active {
  color:#00778c;
}
/deep/ .el-tabs__active-bar {
  background-color: #00778c;
}
/deep/ .el-tabs__item:hover{
  color:#00778c;
}
</style>