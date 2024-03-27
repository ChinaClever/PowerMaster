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
              {{ totalData.ele.toFixed(1) }} kWh
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
                {{ A.vol_value.toFixed(1) }} V
              </el-text>
            </el-col>
            <el-col :span="8">
              <el-text line-clamp="2" :style="{ backgroundColor: A.powColor }">
                P1:<br />
                {{ A.pow_value.toFixed(3) }} kW
              </el-text>
            </el-col>
            <el-col :span="8">
              <el-text line-clamp="2" >
                PF1:<br />
                {{ A.pf.toFixed(2) }}
              </el-text>
            </el-col>
          </el-row>
      </el-card>
    </el-col>
    <el-col :span="6" class="card-box" v-if="haveB">
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
                {{ B.vol_value.toFixed(1) }} V
              </el-text>
            </el-col>
            <el-col :span="8">
              <el-text line-clamp="2" :style="{ backgroundColor: B.powColor }">
                P2:<br />
                {{ B.pow_value.toFixed(3) }} kW
              </el-text>
            </el-col>
            <el-col :span="8">
              <el-text line-clamp="2">
                PF2:<br />
                {{ B.pf.toFixed(2) }}
              </el-text>
            </el-col>
          </el-row>
      </el-card>
    </el-col>
    <el-col :span="6" class="card-box" v-if="haveC">          
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
                {{ C.vol_value.toFixed(1) }} V
              </el-text>
            </el-col>
            <el-col :span="8">
              <el-text line-clamp="2" :style="{ backgroundColor: C.powColor }">
                P3:<br />
                {{ C.pow_value.toFixed(3) }} kW
              </el-text>
            </el-col>
            <el-col :span="8">
              <el-text line-clamp="2">
                PF3:<br />
                {{ C.pf.toFixed(2) }}
              </el-text>
            </el-col>
          </el-row>
      </el-card>
    </el-col>
  </el-row>
  <el-collapse >
    <el-collapse-item title="回路" name="1">
      <ContentWrap>
        <el-table  :data="circleList" :stripe="true" :show-overflow-tooltip="true">
          <el-table-column label="回路" align="center" prop="circuit" />
          <el-table-column label="断路器状态" align="center" prop="breaker" > 
            <template #default="scope" >
              <el-tag type="primary" v-if="scope.row.breaker == 1">开启</el-tag>
              <el-tag type="danger" v-if="scope.row.breaker == 0">关闭</el-tag>
            </template>
          </el-table-column>                        
          <el-table-column label="当前电流" align="center" prop="cur_value" >
            <template #default="scope">
             {{ scope.row.cur_value.toFixed(2) }}A
            </template>
          </el-table-column>
          <el-table-column label="当前电压" align="center" prop="vol_value" >
            <template #default="scope">
             {{ scope.row.vol_value.toFixed(1) }}V
            </template>
          </el-table-column>
          <el-table-column label="有功功率" align="center" prop="pow_value" >
            <template #default="scope">
             {{ scope.row.pow_value.toFixed(3) }}kW
            </template>
          </el-table-column>
          <el-table-column label="电能消耗" align="center" prop="ele" >
            <template #default="scope">
             {{ scope.row.ele.toFixed(1) }}kWh
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
    <el-collapse-item title="输出位" name="3">
      <ContentWrap>
        <el-table  :data="output" :stripe="true" :show-overflow-tooltip="true">
          <el-table-column label="序号" align="center" prop="no" />
          <el-table-column label="名称" align="center" prop="name" />
          <el-table-column label="开关状态" align="center" prop="relay_state" >
            <template #default="scope">
              <el-tag type="primary" v-if="scope.row.relay_state == 1">开启</el-tag>
              <el-tag type="danger" v-if="scope.row.relay_state == 0" >关闭</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="输出电流(A)" align="center" prop="cur_value" >
            <template #default="scope">
              {{ scope.row.cur_value.toFixed(2) }}A
            </template>
          </el-table-column>
          <el-table-column label="有功功率(kW)" align="center" prop="pow_value" >
            <template #default="scope">
             {{ scope.row.pow_value.toFixed(3) }}kW
            </template>
          </el-table-column>
          <el-table-column label="功率因数" align="center" prop="pf" />
          <el-table-column label="电能消耗(kWh)" align="center" prop="ele" >
            <template #default="scope">
             {{ scope.row.ele.toFixed(1) }}kWh
            </template>
          </el-table-column>
        </el-table>
      </ContentWrap>
    </el-collapse-item>
  </el-collapse>

  <el-card>
    <span style="width: 100%">实时动态趋势图</span>
    <el-button type="primary">进入管理界面</el-button>
    <div ref="chartContainer" style="width: 100%; height: 400px;"></div>
  </el-card>
  
 
</template>

<script setup lang="ts">

import download from '@/utils/download'
import { PDUDeviceApi, PDUDeviceVO } from '@/api/pdu/pdudevice'
import * as echarts from 'echarts';
import router from '@/router';

/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })

const location = ref();
const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const testData = ref({
})

const loading = ref(true) // 列表的加载中
const circleList = ref([]) // 列表的数据

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

const output = ref([]) // 列表的数据

const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  devKey: "",
  ipAddr: undefined,
  createTime: [],
  cascadeNum: undefined,
})

const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中

//数据
const totalData = ref({
  ele : 229.6,
  frequency : 50,
  pow : 0,
  powPercentage : 0,
  pf : 0.66
})
const A = ref({
  vol_value : 231.0,
  volColor : "white",
  pow_value : 0.000,
  powColor : "white",
  cur_value : 0,
  curColor : "success",
  curPercemtage: 0,
  pf : 0.00
})
const B = ref({
  vol_value : 231.0,
  volColor : "white",
  pow_value : 0.000,
  powColor : "white",
  cur_value : 0,
  curColor : "success",
  curPercemtage: 0,
  pf : 0.00
})
const C = ref({
  vol_value : 231.0,
  volColor : "white",
  pow_value : 0.000,
  powColor : "white",
  cur_value : 0,
  curColor : "success",
  curPercemtage: 0,
  pf : 0.00
})

const redColor = ref("red")
const haveB = ref(false)
const haveC = ref(false)


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
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

/** 添加/修改操作 */
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await PDUDeviceApi.deletePDUDevice(id)
    message.success(t('common.delSuccess'))
    // 刷新列表
    await getList()
  } catch {}
}

const chart = ref<echarts.ECharts | null>(null);
const chartContainer = ref<HTMLElement | null>(null);

const initChart = () => {
  chart.value = echarts.init(chartContainer.value!);
  chart.value.setOption({
    xAxis: {
      type: 'category',
      data: [],
    },
    yAxis: {
      type: 'value',
    },
    series: [{
      data: [],
      type: 'line',
    }],
  });
};

const addDataPoint = () => {
  setInterval(() => {
    const xAxisData = chart.value?.getOption().xAxis[0].data;
    const seriesData = chart.value?.getOption().series[0].data;
    const timestamp = new Date().toLocaleTimeString();
    const randomValue = Math.random() * 100; // Replace this with your own data source or logic
    xAxisData?.push(timestamp);
    seriesData?.push(randomValue);
    const maxDataPoints = 10; // Number of data points to show on the chart
    if (xAxisData && seriesData && xAxisData.length > maxDataPoints) {
      xAxisData.shift();
      seriesData.shift();
    }
    chart.value?.setOption({ xAxis: { data: xAxisData }, series: [{ data: seriesData }] });
  }, 3000);
};

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    exportLoading.value = true
    const data = await PDUDeviceApi.exportPDUDevice(queryParams)
    download.excel(data, 'PDU设备.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

const getTestData = async()=>{

  testData.value = await PDUDeviceApi.PDUDisplay(queryParams)

  for (let i = 0; i < testData.value.pdu_data.loop_item_list["apparent_pow"].length; i++) {
    let loopItem = {};
    for (let key in testData.value.pdu_data.loop_item_list) {
      loopItem[key] = testData.value.pdu_data.loop_item_list[key][i];
      loopItem["circuit"] = "C" + (i + 1); 
    }
    circleList.value.push(loopItem);
  }

  for (let i = 0; i < testData.value.pdu_data.output_item_list["apparent_pow"].length; i++) {
    let loopItem = {};
    for (let key in testData.value.pdu_data.output_item_list) {
      loopItem[key] = testData.value.pdu_data.output_item_list[key][i];
      loopItem["no"] = i + 1;
    }
    output.value.push(loopItem);
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
    haveB.value = true;
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
    haveC.value = true;
  }
  
}

/** 初始化 **/
onMounted(() => {
  // getList()
  initChart();
  addDataPoint();
  
})

onBeforeMount(async () =>{
  location.value = router.currentRoute.value.query.location as string;
  queryParams.devKey = router.currentRoute.value.query.dev_key as string;
  getTestData();
})


</script>

