<template>
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
            <el-col :span="8">
              <el-text line-clamp="2">
                {{ location }}
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
    <span style="width: 100%">趋势图</span>
    <el-button type="primary" @click="openNewPage(queryParams.devKey)" >进入管理界面</el-button>
    <div ref="chartContainer" id="chartContainer" style="width: 70vw; height: 58vh;"></div>
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
    <el-collapse-item title="传感器" name="2">
      <ContentWrap>
        <el-table  :data="sensorList" :stripe="true" :show-overflow-tooltip="true">
          <el-table-column label="传感器名称" align="center" prop="sensorName" />
          <el-table-column label="传感器状态" align="center" prop="sensorStatus" />
          <el-table-column label="传感器名称" align="center" prop="sensorName2" />
          <el-table-column label="传感器状态" align="center" prop="sensorStatus2" />
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
  </el-collapse>


  
 
</template>

<script setup lang="ts">

// import download from '@/utils/download'
import { PDUDeviceApi } from '@/api/pdu/pdudevice'
import * as echarts from 'echarts';
import router from '@/router';

/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })

const instance = getCurrentInstance();

const activeNames = ref(["1","2","3","4","5"])

const controlVis = ref({
  haveCircle : false,
  haveOutPut : false,
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

const circleList = ref([]) // 列表的数据

const output = ref([]) // 列表的数据

const sensorList = ref([
  {
    sensorName: '温度1',
    sensorStatus: 'NA',
    sensorName2: '湿度1',
    sensorStatus2:"NA",
  },
  {
    sensorName: '温度2',
    sensorStatus: 'NA',
    sensorName2: '湿度2',
    sensorStatus2:"NA",
  },
  {
    sensorName: '门禁1',
    sensorStatus: 'NA',
    sensorName2: '门禁2',
    sensorStatus2:"NA",
  },
]) // 列表的数据

const chartData = ref({
  apparentList : [],
  activeList : [],
  dateTimes : []
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

const initChart =  () => {
  if (chartContainer.value && instance) {
    chart = echarts.init(chartContainer.value);
    console.log(chartData)
    chart.setOption({
      // 这里设置 Echarts 的配置项和数据
      title: { text: ''},
      tooltip: { trigger: 'axis' },
      legend: { data: ['视在功率','有功功率']},
      grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
      toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
      xAxis: {type: 'category', boundaryGap: false, data:chartData.value.dateTimes},
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

window.addEventListener('resize', function() {
  chart?.resize(); 
});

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
  chartData.value = await PDUDeviceApi.PDUHis(queryParams.id);  
  
  initChart();

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
  totalData.value.pow =  testData.value.pdu_data.pdu_tg_data.pow;
  if(testData.value.pdu_data.pdu_tg_data.apparent_pow != 0){
    totalData.value.powPercentage = (testData.value.pdu_data.pdu_tg_data.pow / testData.value.pdu_data.pdu_tg_data.apparent_pow) * 100;
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



/** 初始化 **/
onMounted(() => {
  // getList()
  
  // addDataPoint();
  // initChart();
})

onBeforeMount(async () =>{
  location.value = router.currentRoute.value.query.location as string;
  queryParams.devKey = router.currentRoute.value.query.dev_key as string;
  queryParams.id = router.currentRoute.value.query.id as string;
  
  getTestData();
})


</script>

