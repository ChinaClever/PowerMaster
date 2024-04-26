<template>
  <el-row :gutter="20">
    <el-col :span="treeWidth" :xs="24">
      
      <el-input
        v-model="filterText"
        style="width: 190px"
        placeholder="Filter keyword"
      />

      <el-tree
        ref="treeRef"
        style="max-width: 600px"
        class="filter-tree"
        :data="serverRoomArr"
        :props="defaultProps"
        default-expand-all

        :filter-node-method="filterNode"
      />
    </el-col>
    <el-col :span="24 - treeWidth" :xs="24">
      <ContentWrap>
        <el-form
          class="-mb-15px"
          :model="queryParams"
          ref="queryFormRef"
          :inline="true"
          label-width="120px"
        >
          <el-form-item label="" prop="collaspe">
            <el-switch 
            v-model="isCollapsed"  
            active-color="#409EFF" 
            inactive-color="#909399"
            active-text="折叠"  
            active-value="100"
            inactive-value="0" 
            @change="toggleCollapse" />
          </el-form-item>
          
          <!-- <el-form-item label="网络地址" prop="devKey">
            <el-input
              v-model="queryParams.devKey"
              placeholder="请输入网络地址"
              clearable
              @keyup.enter="handleQuery"
              class="!w-240px"
            />
          </el-form-item> -->

          <el-form-item label="IP地址" prop="ipAddr" >
            <el-input
              v-model="queryParams.ipAddr"
              placeholder="请输入IP地址"
              clearable
              class="!w-140px"
            />
          </el-form-item>

          <el-form-item label="级联地址" prop="cascadeAddr" label-width="70px">
            <el-input-number
              v-model="queryParams.cascadeAddr"
              :min="0"
              controls-position="right"
              :value-on-clear="0"
                class="!w-100px"
            />
          </el-form-item>
          <el-form-item label="时间段" prop="createTime" label-width="100px">
            <el-button 
              @click="queryParams.timeType = 0;queryParams.oldTime = null;queryParams.newTime = null;queryParams.timeArr = null;visControll.visAllReport = false;switchValue = 0;" 
              :type="switchValue == 0 ? 'primary' : ''"
            >
              日报
            </el-button>
            <el-button 
              @click="queryParams.timeType = 1;queryParams.oldTime = null;queryParams.newTime = null;queryParams.timeArr = null;visControll.visAllReport = false;switchValue = 1;" 
              :type="switchValue == 1 ? 'primary' : ''"
            >
              月报
            </el-button>
            <el-button 
              @click="queryParams.timeType = 2;queryParams.oldTime = null;queryParams.newTime = null;queryParams.timeArr = null;visControll.visAllReport = false;switchValue = 2;" 
              :type="switchValue == 2 ? 'primary' : ''"
            >
              自定义
            </el-button>
            
            
          </el-form-item>
          <el-form-item>
            <el-date-picker
              v-if="switchValue == 0"
              v-model="queryParams.oldTime"
              value-format="YYYY-MM-DD HH:mm:ss"
              type="date"
              :disabled-date="disabledDate"
              @change="handleDayPick"
              class="!w-160px"
            />
            <el-date-picker
              v-if="switchValue == 1"
              v-model="queryParams.oldTime"
              value-format="YYYY-MM-DD HH:mm:ss"
              type="month"
              :disabled-date="disabledDate"
              @change="handleMonthPick"
              class="!w-160px"
            />
            <el-date-picker
              v-if="switchValue == 2"
              v-model="queryParams.timeArr"
              value-format="YYYY-MM-DD HH:mm:ss"
              type="daterange"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              :disabled-date="disabledDate"
              @change="handleDayPick"
              class="!w-200px"
            />
          </el-form-item>
          <el-form-item>
            <el-button @click="handleQuery"  ><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
          </el-form-item>
          <!-- <el-text size="large">
            报警次数：{{ pduInfo.alarm }}
          </el-text> -->
        </el-form>
      </ContentWrap>
      <ContentWrap v-show="visControll.visAllReport" >
        <div class="page" >
          <div class="pageBox" >
            <div class="page-conTitle">
              PDU基本信息
            </div>
              <el-table 
                :data="PDUTableData" 
                :header-cell-style="arraySpanMethod"
                >
                <el-table-column  align="center" label="基本信息"  prop="baseInfoName" />
                <el-table-column  prop="baseInfoValue" />
                <el-table-column label="能耗" :rowspan="2" prop="consumeName"  />
                <el-table-column  prop="consumeValue"  />
              </el-table>
            </div>
          <div class="pageBox" v-if="visControll.eqVis" >
            <div class="page-conTitle" v-if="!visControll.isSameDay">
              电量分布
            </div>
            <div class="page-conTitle" v-if="visControll.isSameDay">
              电能分布
            </div>
            <p v-if="!visControll.isSameDay">本周期内，共计使用电量{{eqData.totalEle}}kWh，最大用电量{{eqData.maxEle}}kWh， 最大负荷发生时间{{eqData.maxEleTime}}</p>
            <p v-if="visControll.isSameDay && eqData.eq">本周期内，开始时电能为{{eqData.eq[0]}}kWh，结束时电能为{{eqData.eq[eqData.eq.length - 1]}}kWh， 电能增长{{(eqData.eq[eqData.eq.length - 1] - eqData.eq[0]).toFixed(1)}}kWh</p>
            <div ref="rankChartContainer" id="rankChartContainer" style="width: 70vw; height: 58vh;"></div>
          </div>
          <div class="pageBox"  v-if="visControll.powVis">
            <div class="page-conTitle">
              平均功率曲线
            </div>
            <p>本周期内，最大视在功率{{powData.apparentPowMaxValue}}kVA， 最大负荷发生时间{{powData.apparentPowMaxTime}}</p>
            <div ref="powChartContainer" id="powChartContainer" style="width: 70vw; height: 58vh;"></div>
          </div>
          <div class="pageBox" v-if="visControll.outletVis">
            <div class="page-conTitle" v-if="!visControll.isSameDay">
              输出位电量排名
            </div>
            <div class="page-conTitle" v-if="visControll.isSameDay">
              输出位电能排名
            </div>
            <div ref="outputRankChartContainer" id="outputRankChartContainer" style="width: 70vw; height: 58vh;"></div>

          </div>
          <div class="pageBox" v-if="visControll.temVis">
            <div class="page-conTitle">
              温度曲线
            </div>
            <p>本周期内，最大温度{{temData.temMaxValue}}°C， 最大温度发生时间{{temData.temMaxTime}}</p>
            <div ref="temChartContainer" id="temChartContainer" style="width: 70vw; height: 58vh;"></div>
          </div>
        </div>
      </ContentWrap>
      
    </el-col>
  </el-row>
  <!-- 表单弹窗：添加/修改 -->
  <PDUDeviceForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
// import download from '@/utils/download'
import { PDUDeviceApi } from '@/api/pdu/pdudevice'
import * as echarts from 'echarts';
import { ElTree } from 'element-plus'


/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })

const switchValue = ref(1);
const instance = getCurrentInstance();
const visControll = reactive({
  visAllReport : false,
  isSameDay : false,
  eqVis : false,
  powVis : false,
  outletVis : false,
  temVis : false,
})

const disabledDate = (date) => {
  // 获取今天的日期
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  // 设置date的时间为0时0分0秒，以便与today进行比较
  date.setHours(0, 0, 0, 0);

  // 如果date在今天之后，则禁用
  if(switchValue.value == 0){
    return date > today;
  }else {
    return date >= today;
  }
  
}

const handleDayPick = () => {
  if(queryParams?.oldTime && switchValue.value == 2){

    queryParams.oldTime = null;
    queryParams.newTime = null;
  }
  if(queryParams?.oldTime && switchValue.value == 0){
    queryParams.newTime = queryParams.oldTime.split(" ")[0] + " " + "23:59:59";
    visControll.isSameDay = true;

  } else if (queryParams.timeArr && switchValue.value == 2) {

    // 获取选择的开始日期和结束日期
    const startDate = new Date(queryParams.timeArr[0]);
    const endDate = new Date(queryParams.timeArr[1]);

    // 计算两个日期之间的天数差
    const diffTime = Math.abs(endDate - startDate);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    // 如果天数差超过32天，则重置选择的日期
    if (diffDays > 32) {
      queryParams.timeArr = null;
      ElMessage({
        message: '日期选择不超过31天',
        type: 'warning',
      })
    }else {
      if(areDatesEqual(new Date(queryParams.timeArr[0]),new Date(queryParams.timeArr[1]))){
        visControll.isSameDay = true;
      }else{
        visControll.isSameDay = false;
      }
      queryParams.oldTime = queryParams.timeArr[0];
      queryParams.newTime = queryParams.timeArr[1];


    }
  }
  
}

const handleMonthPick = (newV) => {

  if(queryParams.oldTime){
    var newTime = new Date(queryParams.oldTime);
    newTime.setMonth(newTime.getMonth() + 1);
    newTime.setHours(23,59,59)
    queryParams.newTime = getFullTimeByDate(newTime);
    visControll.isSameDay = false;
  }else {
    queryParams.newTime = null;
  }
  
} 

const getFullTimeByDate = (date) => {
  var year = date.getFullYear();//年
  var month = date.getMonth();//月
  var day = date.getDate();//日
  var hours = date.getHours();//时
  var min = date.getMinutes();//分
  var second = date.getSeconds();//秒
  return year + "-" +
      ((month + 1) > 9 ? (month + 1) : "0" + (month + 1)) + "-" +
      (day > 9 ? day : ("0" + day)) + " " +
      (hours > 9 ? hours : ("0" + hours)) + ":" +
      (min > 9 ? min : ("0" + min)) + ":" +
      (second > 9 ? second : ("0" + second));
}

const areDatesEqual = (date1, date2) => {
  return (
    date1.getFullYear() === date2.getFullYear() &&
    date1.getMonth() === date2.getMonth() &&
    date1.getDate() === date2.getDate()
  );
}


// const activeNames = ref(["1","2","3","4","5"])

const PDUTableData = ref([]) as any

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  devKey : undefined,
  id: undefined,
  type: 'total',
  eqGranularity:"day",
  powGranularity : "hour",
  temGranularity : "hour",
  outputNumber : 10,
  ipAddr: undefined,
  createTime: undefined,
  timeArr:[],
  oldTime : getFullTimeByDate(new Date(new Date().getFullYear(),new Date().getMonth(),1,0,0,0)),
  newTime : getFullTimeByDate(new Date(new Date().getFullYear(),new Date().getMonth() + 1,1,23,59,59)),
  timeType: 1,
  cascadeAddr : 0
}) as any

const serverRoomArr =  [
  {
    value: '1',
    label: '机房1',
    children: [
      {
        value: '1-1',
        label: '机柜1',
        children: [
        {
          value: '1-1-1',
          label: 'PDU1',
        },
        {
          value: '1-1-2',
          label: 'PDU2',
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
        label: '机柜1',
        children: [
        {
          value: '2-1-1',
          label: 'PDU1',
        },
        {
          value: '2-1-2',
          label: 'PDU2',
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
        label: '机柜1',
        children: [
        {
          value: '3-1-1',
          label: 'PDU1',
        },
        {
          value: '3-1-2',
          label: 'PDU2',
        },]
      },
      {
        value: '3-2',
        label: '机柜2',
        children: [
        {
          value: '3-1-1',
          label: 'PDU1',
        },
        {
          value: '3-1-2',
          label: 'PDU2',
        },]
      },
    ],
  },
]

//折叠功能
let treeWidth = ref(3)
let isCollapsed = ref(0);



const pduInfo = ref({
  alarm : 6,
  name : "PDU1",
  statuses : "空闲设备",
  dev_key : "192.168.1.1-0",
  owner :"机房1-机柜1",
  eq : "200",
  total_apparent_pow_max_value : "200",
  total_pow_max_value : "200",
  ele:"112",
  tem_max_value : "25",
})

//柱状图宽度
const barWid = ref(20);

//折线图数据
interface EqData {
  eq: number[];
  time: string[];
  totalEle : number;
  maxEle : number;
  maxEleTime : string;
}
const eqData = ref<EqData>({
  eq : [],
  time : [],
  totalEle : 0,
  maxEle : 0,
  maxEleTime : ""
})

interface PowData {
  apparentPowAvgValue: number[];
  activePowAvgValue: number[];
  time: string[];
  total_pow_apparent : number;
  apparentPowMaxValue : number;
  apparentPowMaxTime : string;
}
const powData = ref<PowData>({
  apparentPowAvgValue : [],
  activePowAvgValue: [],
  time:[],
  total_pow_apparent : 0,
  apparentPowMaxValue : 0,
  apparentPowMaxTime : ""
})

interface TemData {
  temAvgValue: number[];
  time: string[];
  temMaxValue : number;
  temMaxTime : string;
}
const temData = ref<TemData>({
  temAvgValue : [],
  time : [],
  temMaxValue : 0,
  temMaxTime : ""
})

interface ServerData {
  nameAndMax: object[];
  value: number[];
}
const serverData = ref<ServerData>({
  nameAndMax : [
    { name: '服务器1', max: 250 },
    { name: '服务器2', max: 250 },
    { name: '服务器3', max: 250 },
    { name: '服务器4', max: 250 },
    { name: '服务器5', max: 250 },
    { name: '服务器6', max: 250 },
    { name: '服务器7', max: 250 },
    { name: '服务器8', max: 250 }
  ],
  value: [200, 180, 170, 220, 167, 189,200,150]
})

interface OutLetRankData {
  outLetId: string[];
  eleValue: number[];
}

const outletRankData = ref<OutLetRankData>({
  outLetId : [],
  eleValue : [],
})

//树型控件
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

const getList = async () => {
  loading.value = true
  eqData.value = await PDUDeviceApi.getConsumeData(queryParams);
  if( eqData.value.eq && eqData.value.eq.length > 0){
    visControll.eqVis = true;
    eqData.value.eq.forEach((obj,index)=>{
      eqData.value.eq[index] = obj.toFixed(1);
    })
    eqData.value.maxEle = eqData.value.maxEle.toFixed(1);
    eqData.value.totalEle = eqData.value.totalEle.toFixed(1);
  } else{
    visControll.eqVis = false;
  }
  

  powData.value = await PDUDeviceApi.getPowData(queryParams);
  if(powData.value.activePowAvgValue && powData.value.activePowAvgValue.length > 0){
    visControll.powVis = true;
    powData.value.activePowAvgValue.forEach((obj,index)=>{
      powData.value.activePowAvgValue[index] = obj.toFixed(3);
    })

    powData.value.apparentPowAvgValue.forEach((obj,index)=>{
      powData.value.apparentPowAvgValue[index] = obj.toFixed(3);
    })

    powData.value.apparentPowMaxValue = powData.value.apparentPowMaxValue.toFixed(3);
  }else{
    visControll.powVis = false;
  }
  
  outletRankData.value = await PDUDeviceApi.getOutLetData(queryParams);
  if(outletRankData.value.eleValue && outletRankData.value.eleValue.length > 0){
    visControll.outletVis = true;
    outletRankData.value.eleValue.forEach((obj,index)=>{
      outletRankData.value.eleValue[index] = obj.toFixed(1);
    })

    outletRankData.value.outLetId.forEach((obj,index)=>{
      outletRankData.value.outLetId[index] = "输出位" + obj;
    })
  }else{
    visControll.outletVis = false;
  }
  
  temData.value = await PDUDeviceApi.getTemData(queryParams);
  if(temData.value.temAvgValue && temData.value.temAvgValue.length > 0){
    visControll.temVis = true;
  }else{
    visControll.temVis = false;
  }

  temData.value.temAvgValue?.forEach(element => {
    element = element?.toFixed(2);
  });
  temData.value.temMaxValue = temData.value.temMaxValue?.toFixed(2);
  var temp = [] as any;
  temp.push({
    baseInfoName : "PDU名称",
    baseInfoValue : "xx",
    consumeName : "用电量",
    consumeValue : visControll.isSameDay ? (eqData.value.eq[eqData.value.eq.length - 1] - eqData.value.eq[0]).toFixed(1) : eqData.value.totalEle,
  })
  temp.push({
    baseInfoName : "所属机房",
    baseInfoValue : "xx",
    consumeName : "最大视在功率",
    consumeValue : powData.value.apparentPowMaxValue
  })
  temp.push({
    baseInfoName : "网络地址",
    baseInfoValue : queryParams.ipAddr + "-" + queryParams.cascadeAddr,
    consumeName : "最大温度",
    consumeValue : temData.value.temMaxValue 
  })
  PDUTableData.value = temp;
  // initChart();
  loading.value = false

}

const rankChartContainer = ref<HTMLElement | null>(null);
let rankChart = null as echarts.ECharts | null; // 显式声明 rankChart 的类型
const powChartContainer = ref<HTMLElement | null>(null);
let powChart = null as echarts.ECharts | null; // 显式声明 powChart 的类型
const temChartContainer = ref<HTMLElement | null>(null);
let temChart = null as echarts.ECharts | null; // 显式声明 temChart 的类型
const serChartContainer = ref<HTMLElement | null>(null);
let serChart = null as echarts.ECharts | null; // 显式声明 serChart 的类型
const outputRankChartContainer = ref<HTMLElement | null>(null);
let outputRankChart = null as echarts.ECharts | null; // 显式声明 serChart 的类型

const initChart =  () => {
  if (rankChartContainer.value && instance) {
    rankChart = echarts.init(rankChartContainer.value);

    rankChart.setOption({
      // 这里设置 Echarts 的配置项和数据
      dataZoom:[{ type:"inside"}],
      title: { text: ''},
      tooltip: { trigger: 'axis',formatter: function (params) {
                                              var dataIndex = params[0].dataIndex;
                                              var eleValue = eqData.value.eq[dataIndex];
                                              return  eqData.value.time[dataIndex] + "<br/>" + eleValue + " kWh"; // 自定义浮窗显示的内容
                                            },},
      toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
      xAxis: {type: 'category' ,data:eqData.value.time},
      yAxis: { type: 'value'},
      series: [
        { type: 'bar', data: eqData.value.eq, label: { show: true, position: 'top' }, barWidth: barWid.value},// 你可以根据需要选择标签的位置，比如 'top', 'insideTop', 'inside', 等等
      ],
    });
    // 将 rankChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    instance.appContext.config.globalProperties.rankChart = rankChart;
  }
  if (outputRankChartContainer.value && instance) {
    outputRankChart = echarts.init(outputRankChartContainer.value);
    outputRankChart.setOption({
      // 这里设置 Echarts 的配置项和数据
      dataZoom:[{ type:"inside"}],
      title: { text: ''},
      tooltip: { trigger: 'axis',formatter: function (params) {
                                              var dataIndex = params[0].dataIndex;
                                              var eleValue = outletRankData.value.eleValue[dataIndex];
                                              return  outletRankData.value.outLetId[dataIndex] + "<br/>"  + eleValue + " kWh"; // 自定义浮窗显示的内容
                                            },},
      toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
      xAxis: { type: 'value' },
      yAxis: {type: 'category' ,data:outletRankData.value.outLetId},
      series: [
        {  type: 'bar', data: outletRankData.value.eleValue, label: { show: true, position: 'right' }, barWidth: barWid.value},// 你可以根据需要选择标签的位置，比如 'top', 'insideTop', 'inside', 等等
      ],
    });
    // 将 outputRankChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    instance.appContext.config.globalProperties.outputRankChart = outputRankChart;
  }
  if (powChartContainer.value && instance) {
    powChart = echarts.init(powChartContainer.value);
    powChart.setOption({
      // 这里设置 Echarts 的配置项和数据
      dataZoom:[{ type:"inside"}],
      title: { text: ''},
      tooltip: { trigger: 'axis' },
      legend: { data: ['总平均视在功率','总平均有功功率']},
      grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
      toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
      xAxis: {type: 'category', boundaryGap: false, data:powData.value.time},
      yAxis: { type: 'value'},
      series: [
        {name: '总平均视在功率', type: 'line', data: powData.value.apparentPowAvgValue},
        {name: '总平均有功功率', type: 'line', data: powData.value.activePowAvgValue},
      ],

    });
    // 将 powChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    instance.appContext.config.globalProperties.powChart = powChart;
  }
  if (temChartContainer.value && instance) {
    temChart = echarts.init(temChartContainer.value);
    temChart.setOption({
      // 这里设置 Echarts 的配置项和数据
      dataZoom:[{ type:"inside"}],
      title: { text: ''},
      tooltip: { trigger: 'axis' },
      legend: { data: ['平均温度']},
      grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
      toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
      xAxis: {type: 'category', boundaryGap: false, data:temData.value.time},
      yAxis: { type: 'value'},
      series: [
        {name: '平均温度', type: 'line', data: temData.value.temAvgValue},
      ],
    });
    // 将 temChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    instance.appContext.config.globalProperties.temChart = temChart;
  }
  if (serChartContainer.value && instance) {
    serChart = echarts.init(serChartContainer.value);
    serChart.setOption({
      radar: { indicator: serverData.value.nameAndMax },
      series: [
        { name: '服务器IT总视在功率', type: 'radar', label: { show: true, position: 'top' } ,data: [ { value: serverData.value.value, name: '总视在功率' }, ] }
      ]
    });
    // 将 serChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    instance.appContext.config.globalProperties.serChart = serChart;
  }
  visControll.visAllReport = true;
};

// 在组件销毁时手动销毁图表
// const beforeRankUnmount = () => {
//     rankChart?.dispose(); // 销毁图表实例
// };

// const beforeSerRankUnmount = () => {
//     outputRankChart?.dispose(); // 销毁图表实例
// };

// const beforePowUnmount = () => {
//     powChart?.dispose();  // 销毁图表实例
// };

// const beforeTemUnmount = () => {
//     temChart?.dispose(); // 销毁图表实例
// };

// const beforeSerUnmount = () => {
//     serChart?.dispose(); // 销毁图表实例
// };

// window.addEventListener('resize', function() {
//     rankChart?.resize(); 
//     powChart?.resize(); 
//     temChart?.resize(); 
// });

watch(filterText, (val) => {
  treeRef.value!.filter(val)
})

// watch([() => queryParams.outputNumber], ([newOutPutNum]) => {
//     // 销毁原有的图表实例
//     beforeSerRankUnmount();
//     // 创建新的图表实例
//     outputRankChart = echarts.init(document.getElementById('outputRankChartContainer'));
//     outletRankData.value.eleValue = generateRandomIntegers(newOutPutNum);
//     outletRankData.value.outLetId = generateShuffledStrings(newOutPutNum,"服务器");
//     // 设置新的配置对象
//     if (outputRankChart) {
//       outputRankChart.setOption({
//         // 这里设置 Echarts 的配置项和数据
//         title: { text: ''},
//         tooltip: { trigger: 'axis'},
//         legend: { data: ['耗电量']},
//         toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
//         xAxis: {type: 'category' ,data:outletRankData.value.outLetId},
//         yAxis: { type: 'value'},
//         series: [
//           {name:"耗电量",  type: 'bar', data: outletRankData.value.eleValue, label: { show: true, position: 'top' }, barWidth: barWid.value},// 你可以根据需要选择标签的位置，比如 'top', 'insideTop', 'inside', 等等
//         ],
//       });
//     }
    
// })

// 监听类型颗粒度
// watch([ () => queryParams.eqGranularity], (eqNew) => {
//     const [ newEqGranularity] = eqNew;
//     // 处理参数变化

//     if ( newEqGranularity == 'day'){
//       // 销毁原有的图表实例
//       beforeRankUnmount()
//       // 创建新的图表实例
//       rankChart = echarts.init(document.getElementById('rankChartContainer'));
//       getList();
//       // 设置新的配置对象
//       if (rankChart) {
//         rankChart.setOption({
//         // 这里设置 Echarts 的配置项和数据
//         title: { text: ''},
//         tooltip: { trigger: 'axis'},
//         legend: { data: ['耗电量']},
//         toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
//         xAxis: {type: 'category' ,data:eqData.value.time},
//         yAxis: { type: 'value'},
//         series: [
//           { type: 'bar', data: eqData.value.eq, label: { show: true, position: 'top' }, barWidth: barWid.value},// 你可以根据需要选择标签的位置，比如 'top', 'insideTop', 'inside', 等等
//         ],
//       });
//     }
//     }else if(newEqGranularity == 'week'){
//       // 销毁原有的图表实例
//       beforeRankUnmount()
//       // 创建新的图表实例
//       rankChart = echarts.init(document.getElementById('rankChartContainer'));
//       eqData.value.time = ["2023-04-第一周","2023-03-第二周","2023-02-第三周","2023-01-第一周","2023-03-第一周","2023-02-第一周","2023-04-第一周"]
//       // 设置新的配置对象
//       if (rankChart) {
//         rankChart.setOption({
//           // 这里设置 Echarts 的配置项和数据
//           title: { text: ''},
//           tooltip: { trigger: 'axis'},
//           legend: { data: ['耗电量']},
//           toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
//           xAxis: {type: 'category' ,data:eqData.value.time},
//           yAxis: { type: 'value'},
//           series: [
//             { type: 'bar', data: eqData.value.eq, label: { show: true, position: 'top' }, barWidth: barWid.value},// 你可以根据需要选择标签的位置，比如 'top', 'insideTop', 'inside', 等等
//           ],
//         });
//       }
//     }else{
//       // 销毁原有的图表实例
//       beforeRankUnmount()
//       // 创建新的图表实例
//       rankChart = echarts.init(document.getElementById('rankChartContainer'));
//       eqData.value.time = ["2023-04","2023-08","2023-07","2023-02","2023-10","2023-06","2023-09"]
//       // 设置新的配置对象
//       if (rankChart) {
//         rankChart.setOption({
//           // 这里设置 Echarts 的配置项和数据
//           title: { text: ''},
//           tooltip: { trigger: 'axis'},
//           legend: { data: ['耗电量']},
//           toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
//           xAxis: {type: 'category' ,data:eqData.value.time},
//           yAxis: { type: 'value'},
//           series: [
//             { type: 'bar', data: eqData.value.eq, label: { show: true, position: 'top' }, barWidth: barWid.value},// 你可以根据需要选择标签的位置，比如 'top', 'insideTop', 'inside', 等等
//           ],
//         });
//       }
//     }
    
// });

// 下拉框选项数组
// const deviceStatus = ref([])

// const message = useMessage() // 消息弹窗
// const { t } = useI18n() // 国际化

const toggleCollapse = () => {
  treeWidth.value = isCollapsed.value == 0 ? 3 : 0;
};

const loading = ref(false) // 列表的加载中

// const total = ref(0) // 列表的总页数
const queryFormRef = ref() // 搜索的表单
// const exportLoading = ref(false) // 导出的加载中


const arraySpanMethod = ({
  row,
}) => {
  row[0].colSpan = 2
  row[1].colSpan = 0
  row[2].colSpan = 2
  row[3].colSpan = 0
}

/** 搜索按钮操作 */
const handleQuery = async () => {

  if(queryParams.ipAddr){
    if(queryParams.oldTime && queryParams.newTime){
      queryParams.devKey = queryParams.ipAddr +'-' +  queryParams.cascadeAddr;
      await getList();
      
      initChart();
      queryParams.devKey = null;
    }
  }
  
}

/** 重置按钮操作 */
// const resetQuery = () => {
//   queryFormRef.value.resetFields()
//   handleQuery()
// }

/** 添加/修改操作 */
const formRef = ref()
// const openForm = (type: string, id?: number) => {
//   formRef.value.open(type, id)
// }

// /** 删除按钮操作 */
// const handleDelete = async (id: number) => {
//   try {
//     // 删除的二次确认
//     await message.delConfirm()
//     // 发起删除
//     await PDUDeviceApi.deletePDUDevice(id)
//     message.success(t('common.delSuccess'))
//     // 刷新列表
//     await getList()
//   } catch {}
// }

// /** 导出按钮操作 */
// const handleExport = async () => {
//   try {
//     // 导出的二次确认
//     await message.exportConfirm()
//     // 发起导出
//     exportLoading.value = true
//     const data = await PDUDeviceApi.exportPDUDevice(queryParams)
//     download.excel(data, 'PDU设备.xls')
//   } catch {
//   } finally {
//     exportLoading.value = false
//   }
// }

/** 初始化 **/
onMounted( async () =>  {
  // getList();
  // initChart();
})
</script>

