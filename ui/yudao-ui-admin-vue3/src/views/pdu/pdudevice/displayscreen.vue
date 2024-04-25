<template>
  <el-row :gutter="18" >
    <el-col>
      <el-card>
        <el-row :gutter="18" >
          <el-col :span="2">
            <el-text line-clamp="2">
              <el-text class="mx-1" size="large">所在位置：{{ location }}</el-text>
            </el-text>
          </el-col>
          <el-col :span="20">
            <el-text line-clamp="2">
              <el-text class="mx-1" size="large">网络地址：{{ queryParams.devKey }}</el-text>
            </el-text>
          </el-col>
          <el-col :span="2">
            <el-button type="primary" @click="openNewPage(queryParams.devKey)" >进入管理界面</el-button>
          </el-col>
        </el-row>
      </el-card>
    </el-col>
  </el-row>
  <el-row :gutter="24" >
    <el-col :span="6" class="card-box">
      <el-card>
        <template #header>
          <el-row class="text-container"> 
            <el-col :span="12">
              <el-text line-clamp="2">
                总数据
              </el-text>
            </el-col>   
          </el-row>
        </template>
        <el-row justify="center">
          <el-progress type="circle" :percentage="totalData.powPercentage" :width="200" >
            <template #default="{}">
              <span class="percentage-value">{{ totalData.pow }}kW</span>
            </template>
          </el-progress>
        </el-row>
        <el-row class="text-container"> 
          <el-col :span="8">
            <el-text line-clamp="2" >
              电能消耗:<br />
              {{ totalData.ele }} kWh
            </el-text>
          </el-col>
          <el-col :span="8">
            <el-text line-clamp="2">
              频率:<br />
              {{ totalData.frequency }} Hz
            </el-text>
          </el-col>
          <el-col :span="8">
            <el-text line-clamp="2">
              PF:<br />
              {{ totalData.pf }}
            </el-text>
          </el-col>
        </el-row>
      </el-card>       
    </el-col>
    <el-col :span="6" class="card-box">
      <el-card>
        <template #header>
            <div>
              <span>A相</span>
            </div>
          </template>
          <el-row justify="center">
            <el-progress type="circle" :percentage="A.curPercemtage" :width="200" :status="A.curColor">
              <template #default="{}">
                <span class="percentage-value">{{ A.cur_value }}A</span>
              </template>
            </el-progress>
          </el-row>
          <el-row class="text-container">
            <el-col :span="8">
              <el-text line-clamp="2" :style="{ backgroundColor: A.volColor }">
                U1:<br />
                {{ A.vol_value }} V
              </el-text>
            </el-col>
            <el-col :span="8">
              <el-text line-clamp="2" :style="{ backgroundColor: A.powColor }">
                P1:<br />
                {{ A.pow_value }} kW
              </el-text>
            </el-col>
            <el-col :span="8">
              <el-text line-clamp="2" >
                PF1:<br />
                {{ A.pf }}
              </el-text>
            </el-col>
          </el-row>
      </el-card>
    </el-col>
    <el-col :span="6" class="card-box" v-if="controlVis.haveB">
      <el-card>
        <template #header>
            <div>
              <span>B相</span>
            </div>
          </template>                                                                           
          <el-row justify="center">
            <el-progress type="circle" :percentage="B.curPercemtage" :width="200" :status="B.curColor">
              <template #default="{}">
                <span class="percentage-value">{{ B.cur_value }}A</span>
              </template>
            </el-progress>
          </el-row>
          <el-row class="text-container">
            <el-col :span="8">
              <el-text line-clamp="2"  :style="{ backgroundColor: B.volColor }">
                U2:<br />
                {{ B.vol_value }} V
              </el-text>
            </el-col>
            <el-col :span="8">
              <el-text line-clamp="2" :style="{ backgroundColor: B.powColor }">
                P2:<br />
                {{ B.pow_value }} kW
              </el-text>
            </el-col>
            <el-col :span="8">
              <el-text line-clamp="2">
                PF2:<br />
                {{ B.pf }}
              </el-text>
            </el-col>
          </el-row>
      </el-card>
    </el-col>
    <el-col :span="6" class="card-box" v-if="controlVis.haveC">          
      <el-card>
        <template #header>
            <div>
              <span>C相</span>
            </div>
          </template>
          <el-row justify="center">
            <el-progress type="circle" :percentage="C.curPercemtage" :width="200" :status="C.curColor">
              <template #default="{}">
                <span class="percentage-value">{{ C.cur_value }}A</span>
              </template>
            </el-progress>
          </el-row>
          <el-row class="text-container">
            <el-col :span="8">
              <el-text line-clamp="2"  :style="{ backgroundColor: C.volColor }">
                U3:<br />
                {{ C.vol_value }} V
              </el-text>
            </el-col>
            <el-col :span="8">
              <el-text line-clamp="2" :style="{ backgroundColor: C.powColor }">
                P3:<br />
                {{ C.pow_value }} kW
              </el-text>
            </el-col>
            <el-col :span="8">
              <el-text line-clamp="2">
                PF3:<br />
                {{ C.pf }}
              </el-text>
            </el-col>
          </el-row>
      </el-card>
    </el-col>
  </el-row>
  <el-collapse v-model="activeNames" >
    <el-card>
      <el-row>
        <el-col >
          <span style="width: 100%">趋势图</span>
        </el-col>
        <el-col >
          <div style="float:right;margin-top: 0;">
            <el-form-item label="颗粒度" prop="type">
              <el-button @click="queryParams.powGranularity = `oneHour`;switchValue = 0;" :type="!switchValue ? 'primary' : ''">最近一小时</el-button>
              <el-button @click="queryParams.powGranularity = `twentyfourHour`;switchValue = 1;" :type="switchValue ? 'primary' : ''">过去24小时</el-button>
            </el-form-item>
          </div>
        </el-col> 
      </el-row>
      <div style="display: flex; justify-content: center; align-items: center;">
        <div ref="chartContainer" id="chartContainer" style="width: 70vw; height: 58vh;"></div>
      </div>
      
    </el-card>
    <el-collapse-item title="回路" name="1" v-if="controlVis.haveCircle">
      <ContentWrap>
        <el-table  :data="circleList" :stripe="true" :show-overflow-tooltip="true">
          <el-table-column label="回路" align="center" prop="circuit" />
          <el-table-column label="断路器状态" align="center" prop="breaker" v-if="controlVis.circleTableCol.breaker"> 
            <template #default="scope" >
              <el-tag type="" v-if="scope.row.breaker == 1">开启</el-tag>
              <el-tag type="danger" v-if="scope.row.breaker == 0">关闭</el-tag>
            </template>
          </el-table-column>                        
          <el-table-column label="当前电流" align="center" prop="cur_value" v-if="controlVis.circleTableCol.cur_value" >
            <template #default="scope" >
              <el-text line-clamp="2"  :style="{ backgroundColor: scope.row.curColor }">
                {{ scope.row.cur_value }} A
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="当前电压" align="center" prop="vol_value" v-if="controlVis.circleTableCol.vol_value" >
            <template #default="scope">
              <el-text line-clamp="2"  :style="{ backgroundColor: scope.row.volColor }">
                {{ scope.row.vol_value }} V
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="有功功率" align="center" prop="pow_value" v-if="controlVis.circleTableCol.pow_value" >
            <template #default="scope">
              <el-text line-clamp="2"  :style="{ backgroundColor: scope.row.powColor }">
                {{ scope.row.pow_value }} kW
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="电能消耗" align="center" prop="ele" v-if="controlVis.circleTableCol.ele">
            <template #default="scope">
             {{ scope.row.ele }}kWh
            </template>
          </el-table-column>
        </el-table>
      </ContentWrap>
    </el-collapse-item>
    <el-collapse-item title="输出位" name="3" v-if="controlVis.haveOutPut">
      <ContentWrap>
        <el-table  :data="output" :stripe="true" :show-overflow-tooltip="true">
          <el-table-column label="序号" align="center" prop="no" />
          <el-table-column label="名称" align="center" prop="name" />
          <el-table-column label="开关状态" align="center" prop="relay_state" v-if="controlVis.outPutTableCol.relay_state">
            <template #default="scope">
              <el-tag type="" v-if="scope.row.relay_state == 1">开启</el-tag>
              <el-tag type="danger" v-if="scope.row.relay_state == 0" >关闭</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="输出电流(A)" align="center" prop="cur_value"  v-if="controlVis.outPutTableCol.cur_value">
            <template #default="scope">
              <el-text line-clamp="2"  :style="{ backgroundColor: scope.row.curColor }">
                {{ scope.row.cur_value }} A
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="有功功率(kW)" align="center" prop="pow_value"  v-if="controlVis.outPutTableCol.pow_value">
            <template #default="scope">
              <el-text line-clamp="2"  :style="{ backgroundColor: scope.row.powColor }">
                {{ scope.row.pow_value }} kW
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="功率因数" align="center" prop="pf"  v-if="controlVis.outPutTableCol.pf"/>
          <el-table-column label="电能消耗(kWh)" align="center" prop="ele"  v-if="controlVis.outPutTableCol.ele">
            <template #default="scope">
             {{ scope.row.ele }}kWh
            </template>
          </el-table-column>
        </el-table>
      </ContentWrap>
    </el-collapse-item>
    <el-collapse-item title="传感器" name="2" v-if="controlVis.haveSensor">
      <ContentWrap>
        <el-table  :data="sensorList" :stripe="true" :show-overflow-tooltip="true">
          <el-table-column label="传感器名称" align="center" prop="temName" />
          <el-table-column label="传感器状态" align="center" prop="tem_alarm_status" >
            <template #default="scope" >
              <el-tag type="" v-if="scope.row.tem_alarm_status == 0">正常</el-tag>
              <el-tag type="danger" v-if="scope.row.tem_alarm_status == 1">最小值告警</el-tag>
              <el-tag type="warning" v-if="scope.row.tem_alarm_status == 2">下限预警</el-tag>
              <el-tag type="warning" v-if="scope.row.tem_alarm_status == 4">上限预警</el-tag>
              <el-tag type="danger" v-if="scope.row.tem_alarm_status == 8">最大值告警</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="传感器名称" align="center" prop="humName" />
          <el-table-column label="传感器状态" align="center" prop="hum_alarm_status" >
            <template #default="scope" >
              <el-tag type="" v-if="scope.row.hum_alarm_status == 0">正常</el-tag>
              <el-tag type="danger" v-if="scope.row.hum_alarm_status == 1">最小值告警</el-tag>
              <el-tag type="warning" v-if="scope.row.hum_alarm_status == 2">下限预警</el-tag>
              <el-tag type="warning" v-if="scope.row.hum_alarm_status == 4">上限预警</el-tag>
              <el-tag type="danger" v-if="scope.row.hum_alarm_status == 8">最大值告警</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </ContentWrap>
    </el-collapse-item>
  </el-collapse>

</template>

<script setup lang="ts">

// import download from '@/utils/download'
import { PDUDeviceApi } from '@/api/pdu/pdudevice'
import * as echarts from 'echarts';
import router from '@/router';
// import { object } from 'vue-types';

/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })

const instance = getCurrentInstance();
const switchValue = ref(0);

//折叠列表显示的项
const activeNames = ref(["1","2","3","4","5"])

//计时器
const flashListTimer = ref({
  tableDataTimer : null as any,
  chartTimer : null as any,
});
const firstTimerCreate = ref(true);

const controlVis = ref({
  haveCircle : false,
  haveOutPut : false,
  haveSensor : false,
  haveC : false,
  haveB : false,
  circleTableCol : {
    breaker : false,
    cur_value : false,
    vol_value : false,
    pow_value : false,
    ele : false
  },
  outPutTableCol : {
    relay_state : false,
    cur_value : false, 
    pow_value : false,
    pf : false,
    ele : false
  },
})

const location = ref();
// const message = useMessage() // 消息弹窗
// const { t } = useI18n() // 国际化

const testData = ref({
})as any

// const loading = ref(true) // 列表的加载中

const circleList = ref([]) as any // 列表的数据

const output = ref([]) as any// 列表的数据

const sensorList = ref([
]) as any// 列表的数据

const chartData = ref({
  apparentList : [] as number[],
  activeList : [] as number[],
  dateTimes : [] as string[]
})

// const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  devKey: "",
  ipAddr: undefined,
  createTime: [],
  cascadeNum: undefined,
  id : '',
  powGranularity: "oneHour",
})

// const queryFormRef = ref() // 搜索的表单
// const exportLoading = ref(false) // 导出的加载中

//数据
const totalData = ref({
  ele : 0,
  frequency : 0,
  pow : 0,
  powPercentage : 0,
  pf : 0
})
const A = ref({
  vol_value : null,
  volColor : null,
  pow_value : null,
  powColor : null,
  cur_value : null,
  curColor : null,
  curPercemtage: null,
  pf : null
}) as any
const B = ref({
  vol_value : null,
  volColor : null,
  pow_value : null,
  powColor : null,
  cur_value : null,
  curColor : null,
  curPercemtage: null,
  pf : null
}) as any
const C = ref({
  vol_value : null,
  volColor : null,
  pow_value : null,
  powColor : null,
  cur_value : null,
  curColor : null,
  curPercemtage: null,
  pf : null
}) as any

// const redColor = ref("red")


/** 查询列表 */
// const getList = async () => {
//   loading.value = true
//   try {
//     const data = await PDUDeviceApi.getPDUDevicePage(queryParams)
//     list.value = data.list
//     total.value = data.total
//   } finally {
//     loading.value = false
//   }
// }

/** 搜索按钮操作 */
// const handleQuery = () => {
//   queryParams.pageNo = 1
//   getList()
// }

/** 重置按钮操作 */
// const resetQuery = () => {
//   queryFormRef.value.resetFields()
//   handleQuery()
// }

/** 添加/修改操作 */
// const formRef = ref()
// const openForm = (type: string, id?: number) => {
//   formRef.value.open(type, id)
// }

/** 删除按钮操作 */
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

const openNewPage = (devKey) => {
  const url = 'http://' + devKey.split('-')[0] + '/index.html';
  window.open(url, '_blank');
}

let chart = null as echarts.ECharts | null; // 显式声明 rankChart 的类型
const chartContainer = ref<HTMLElement | null>(null);

const initChart = async () => {
  var tempParams = { id : queryParams.id, type : queryParams.powGranularity}
  chartData.value = await PDUDeviceApi.PDUHis(tempParams); 
  if (chartContainer.value && instance) {
    chart = echarts.init(chartContainer.value);
    chart.setOption({
      // 这里设置 Echarts 的配置项和数据
      title: { text: ''},
      tooltip: { trigger: 'axis' },
      legend: { data: ['视在功率','有功功率']},
      grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
      toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
      xAxis: {type: 'category', axisLabel: { formatter: 
            function (value) {
              if(queryParams.powGranularity == "oneHour"){
                // 截取字符串的前n位，即yyyy-MM-dd HH:mm:ss
                return value.substring(11, 19);
              } else if(queryParams.powGranularity == "twentyfourHour"){
                // 截取字符串的n位，即yyyy-MM-dd HH:mm:ss
                return value.substring(5, 19);
              }
            }
          },boundaryGap: false, data:chartData.value.dateTimes},
      yAxis: { type: 'value'},
      series: [
        {name: '视在功率', type: 'line', data: chartData.value.apparentList , symbol: 'circle', symbolSize: 4},
        {name: '有功功率', type: 'line', data: chartData.value.activeList , symbol: 'circle', symbolSize: 4},
      ],

    });
    // 将 chart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    instance.appContext.config.globalProperties.powChart = chart;
  }
};

// 在组件销毁时手动销毁图表
const beforeChartUnmount = () => {
  chart?.dispose(); // 销毁图表实例
};

// window.addEventListener('resize', function() {
//   chart?.resize(); 
// });

const setNewChartData = async () => {
  var params = { id : queryParams.id , oldTime : chartData.value.dateTimes[chartData.value.dateTimes.length - 1], type : queryParams.powGranularity}
  var temp =  {dateTime : '' , apparent : 0, active: 0 };
  temp =  await PDUDeviceApi.ChartNewData(params);
  console.log(temp);
  chartData.value.apparentList.shift()
  chartData.value.activeList.shift()
  chartData.value.dateTimes.shift()

  chartData.value.dateTimes.push(temp.dateTime);
  chartData.value.apparentList.push(temp.apparent);
  chartData.value.activeList.push(temp.active);
  console.log(chartData.value.dateTimes);

  chart?.setOption({
    xAxis: { data: chartData.value.dateTimes },
    series: [
      { data: chartData.value.apparentList },
      { data: chartData.value.activeList},
    ],
  });
}

const flashChartData = async () =>{
  var tempParams = { id : queryParams.id, type : queryParams.powGranularity}
  chartData.value = await PDUDeviceApi.PDUHis(tempParams); 

  chart?.setOption({
    xAxis: { data: chartData.value.dateTimes },
    series: [
      { data: chartData.value.apparentList },
      { data: chartData.value.activeList},
    ],
  });
}

// const addDataPoint = () => {
//   setInterval(() => {
//     const xAxisData = chart.value?.getOption().xAxis[0].data;
//     const seriesData = chart.value?.getOption().series[0].data;
//     const timestamp = new Date().toLocaleTimeString();
//     const randomValue = Math.random() * 100; // Replace this with your own data source or logic
//     xAxisData?.push(timestamp);
//     seriesData?.push(randomValue);
//     const maxDataPoints = 10; // Number of data points to show on the chart
//     if (xAxisData && seriesData && xAxisData.length > maxDataPoints) {
//       xAxisData.shift();
//       seriesData.shift();
//     }
//     chart.value?.setOption({ xAxis: { data: xAxisData }, series: [{ data: seriesData }] });
//   }, 3000);
// };

/** 导出按钮操作 */
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

const getTestData = async()=>{

  testData.value = await PDUDeviceApi.PDUDisplay(queryParams);
  circleList.value = [];
  output.value = [];

  if(testData.value.pdu_data?.loop_item_list){
    for (let i = 0; i < testData.value.pdu_data?.loop_item_list["pow_apparent"].length; i++) {
      let loopItem = {} as any;
      for (let key in testData.value.pdu_data.loop_item_list) {
        loopItem[key] = testData.value.pdu_data.loop_item_list[key][i];
        loopItem["circuit"] = "C" + (i + 1); 
        controlVis.value.circleTableCol[key] = true;
        if (key.includes("alarm_status")) {
          if(testData.value.pdu_data.loop_item_list[key][i] == 1 ||testData.value.pdu_data.loop_item_list[key][i] == 8){
            var alarmColor = key.split("_")[0] + "Color";
            loopItem[alarmColor] = "red";
          }
        }
      }
      circleList.value.push(loopItem);
    }
    controlVis.value.haveCircle = true;
  }

  if(testData.value.pdu_data?.output_item_list){
    for (let i = 0; i < testData.value.pdu_data.output_item_list["name"].length; i++) {
      let loopItem = {} as any;
      for (let key in testData.value.pdu_data.output_item_list) {
        loopItem[key] = testData.value.pdu_data.output_item_list[key][i];
        loopItem["no"] = i + 1;
        controlVis.value.outPutTableCol[key] = true;
        if (key.includes("alarm_status")) {
          if(testData.value.pdu_data.output_item_list[key][i] == 1 ||testData.value.pdu_data.output_item_list[key][i] == 8){
            var alarmColor = key.split("_")[0] + "Color";
            loopItem[alarmColor] = "red";
          }
        }
      }
      output.value.push(loopItem);
    }
    controlVis.value.haveOutPut = true;
  }

  if(testData.value.pdu_data?.env_item_list?.tem_value){
    for(let i = 0; i < testData.value.pdu_data.env_item_list["tem_value"].length; i++){
      let loopItem = {} as any;
      for (let key in testData.value.pdu_data.env_item_list) {
        loopItem[key] = testData.value.pdu_data.env_item_list[key][i];
      }
      loopItem["temName"] = "温度" + i;
      loopItem["hummName"] = "湿度" + i;
      if(loopItem["insert"][i] == 1){
        controlVis.value.haveSensor = true;
      }
      sensorList.value.push(loopItem)
    }
  }

  totalData.value.pow =  testData.value.pdu_data.pdu_tg_data.pow;
  if(testData.value.pdu_data.pdu_tg_data.pow_apparent != 0){
    totalData.value.powPercentage = (testData.value.pdu_data.pdu_tg_data.pow / testData.value.pdu_data.pdu_tg_data.pow_apparent) * 100;
  } else {
    totalData.value.powPercentage = 0;
  }
  
  totalData.value.ele = testData.value.pdu_data.pdu_tg_data.ele;

  totalData.value.pf = testData.value.pdu_data.pdu_tg_data.pf;
  
  A.value.cur_value = testData.value.pdu_data.line_item_list.cur_value[0];
  A.value.curPercemtage = (A.value.cur_value / testData.value.pdu_data.line_item_list.cur_alarm_max[0]) * 100;
  let curalarm = testData.value.pdu_data.line_item_list.cur_alarm_status[0];
  if(curalarm == 1 || curalarm == 8 ){
    A.value.curColor = "exception";
  }
  if(curalarm == 2 || curalarm == 4 ){
    A.value.curColor = "warning";
  }

  A.value.vol_value = testData.value.pdu_data.line_item_list.vol_value[0];
  let u1alarm = testData.value.pdu_data.line_item_list.vol_alarm_status[0];
  if(u1alarm == 1 || u1alarm == 8 ){
    A.value.volColor = "red";
  }
  if(u1alarm == 2 || u1alarm == 4 ){
    A.value.volColor = "yellow";
  }
  
  A.value.pow_value =testData.value.pdu_data.line_item_list.pow_value[0];
  let powalarm = testData.value.pdu_data.line_item_list.pow_alarm_status[0];
  if(powalarm == 1 || powalarm == 8 ){
    A.value.powColor = "red";
  }
  if(powalarm == 2 || powalarm == 4 ){
    A.value.powColor = "yellow";
  }

  A.value.pf = testData.value.pdu_data.line_item_list.pf[0];

  if(testData.value.pdu_data.line_item_list.ele.length > 1){
    B.value.cur_value = testData.value.pdu_data.line_item_list.cur_value[1];
    B.value.curPercemtage = (B.value.cur_value / testData.value.pdu_data.line_item_list.cur_alarm_max[1]) * 100;
    let curalarm = testData.value.pdu_data.line_item_list.cur_alarm_status[1];
    if(curalarm == 1 || curalarm == 8 ){
      B.value.curColor = "exception";
    }
    if(curalarm == 2 || curalarm == 4 ){
      B.value.curColor = "warning";
    }

    B.value.vol_value = testData.value.pdu_data.line_item_list.vol_value[1];
    let u2alarm = testData.value.pdu_data.line_item_list.vol_alarm_status[1];
    if(u2alarm == 1 || u2alarm == 8 ){
      B.value.volColor = "red";
    }
    if(u2alarm == 2 || u2alarm == 4 ){
      B.value.volColor = "yellow";
    }
    
    B.value.pow_value =testData.value.pdu_data.line_item_list.pow_value[1];
    let powalarm = testData.value.pdu_data.line_item_list.pow_alarm_status[1];
    if(powalarm == 1 || powalarm == 8 ){
      B.value.powColor = "red";
    }
    if(powalarm == 2 || powalarm == 4 ){
      B.value.powColor = "yellow";
    }
    
    B.value.pf = testData.value.pdu_data.line_item_list.pf[1];
    controlVis.value.haveB = true;
  }
  if(testData.value.pdu_data.line_item_list.ele.length > 2){
    C.value.cur_value = testData.value.pdu_data.line_item_list.cur_value[2];
    C.value.curPercemtage = (C.value.cur_value / testData.value.pdu_data.line_item_list.cur_alarm_max[2]) * 100;
    let curalarm = testData.value.pdu_data.line_item_list.cur_alarm_status[2];
    if(curalarm == 1 || curalarm == 8 ){
      C.value.curColor = "exception";
    }
    if(curalarm == 2 || curalarm == 4 ){
      C.value.curColor = "warning";
    }

    C.value.vol_value = testData.value.pdu_data.line_item_list.vol_value[2];
    let u2alarm = testData.value.pdu_data.line_item_list.vol_alarm_status[2];
    if(u2alarm == 1 || u2alarm == 8 ){
      C.value.volColor = "red";
    }
    if(u2alarm == 2 || u2alarm == 4 ){
      C.value.volColor = "yellow";
    }
    
    C.value.pow_value =testData.value.pdu_data.line_item_list.pow_value[2];
    let powalarm = testData.value.pdu_data.line_item_list.pow_alarm_status[2];
    if(powalarm == 1 || powalarm == 8 ){
      C.value.powColor = "red";
    }
    if(powalarm == 2 || powalarm == 4 ){
      C.value.powColor = "yellow";
    }

    C.value.pf = testData.value.pdu_data.line_item_list.pf[2];
    controlVis.value.haveC = true;
  }
  
}

watch([() => queryParams.powGranularity], async ([newPowGranularity]) => {
    // 销毁原有的图表实例
    beforeChartUnmount();
    //获取数据
    var tempParams = { id : queryParams.id, type : newPowGranularity}
    chartData.value = await PDUDeviceApi.PDUHis(tempParams); 

    // 创建新的图表实例
    chart = echarts.init(document.getElementById('chartContainer'));
    // 设置新的配置对象
    if (chart) {
      chart.setOption({
        // 这里设置 Echarts 的配置项和数据
        title: { text: ''},
        tooltip: { trigger: 'axis' },
        legend: { data: ['视在功率','有功功率']},
        grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
        toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
        xAxis: {type: 'category', axisLabel: { formatter: 
            function (value) {
              if(queryParams.powGranularity == "oneHour"){
                // 截取字符串的前n位，即yyyy-MM-dd HH:mm:ss
                return value.substring(11, 19);
              } else if(queryParams.powGranularity == "twentyfourHour"){
                // 截取字符串的n位，即yyyy-MM-dd HH:mm:ss
                return value.substring(5, 19);
              }
            }
          }, boundaryGap: false, data:chartData.value.dateTimes},
        yAxis: { type: 'value'},
        series: [
          {name: '视在功率', type: 'line', data: chartData.value.apparentList , symbol: 'circle', symbolSize: 4},
          {name: '有功功率', type: 'line', data: chartData.value.activeList , symbol: 'circle', symbolSize: 4},
        ],

      });
    }
    if(flashListTimer.value.chartTimer){
      var time = 0;
      if(queryParams.powGranularity == "oneHour"){
        time = 60000;
        // time = 3000;
      } else if(queryParams.powGranularity == "twentyfourHour"){
        time = 3600000;
        // time = 3000;
      }
      clearInterval(flashListTimer.value.chartTimer)
      flashListTimer.value.chartTimer = null;
      flashListTimer.value.chartTimer = setInterval((setNewChartData), time);
    }
})


/** 初始化 **/
onMounted(() => {

  // getList()
  
  // addDataPoint();
  // initChart();
})

onBeforeMount(async () =>{
  location.value = router.currentRoute.value.query.location as string;
  queryParams.devKey = router.currentRoute.value.query.devKey as string;
  queryParams.id = router.currentRoute.value.query.id as string;
  
  getTestData();
  initChart();
  flashListTimer.value.tableDataTimer = setInterval((getTestData), 5000);
  flashListTimer.value.chartTimer = setInterval((setNewChartData), 60000);
})

onBeforeUnmount(()=>{
  if(flashListTimer.value.tableDataTimer && flashListTimer.value.chartTimer){
    clearInterval(flashListTimer.value.tableDataTimer)
    clearInterval(flashListTimer.value.chartTimer)
    flashListTimer.value.tableDataTimer = null;
    flashListTimer.value.chartTimer = null;
  }
})

onActivated(() => {
  getTestData();
  flashChartData();
  if(!firstTimerCreate.value){
    flashListTimer.value.tableDataTimer = setInterval((getTestData), 5000);
    var time = 0;
    if(queryParams.powGranularity == "oneHour"){
      time = 60000;
      // time = 3000;
    } else if(queryParams.powGranularity == "twentyfourHour"){
      time = 3600000;
      // time = 3000;
    }
    flashListTimer.value.chartTimer = setInterval((setNewChartData), time);
  }
})

onBeforeRouteLeave(()=>{
  if(flashListTimer.value.tableDataTimer && flashListTimer.value.chartTimer){
    clearInterval(flashListTimer.value.tableDataTimer)
    clearInterval(flashListTimer.value.chartTimer)
    flashListTimer.value.tableDataTimer = null;
    flashListTimer.value.chartTimer = null;
    firstTimerCreate.value = false;
  }
  
})





</script>

