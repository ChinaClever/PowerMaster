<template>
  <el-row :gutter="24">
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
        <Echart :options="totalData" :height="200" />
        <el-row class="text-container"> 
          <el-col :span="8">
            <el-text line-clamp="2">
              电能消耗:<br />
              {{ electricityConsumption }} kWh
            </el-text>
          </el-col>
          <el-col :span="8">
            <el-text line-clamp="2">
              频率:<br />
              {{ frequency }} Hz
            </el-text>
          </el-col>
          <el-col :span="8">
            <el-text line-clamp="2">
              PF:<br />
              {{ PF }}
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
          <Echart :options="AData" :height="200" />
          <el-row class="text-container">
            <el-col :span="8">
              <el-text line-clamp="2">
                U1:<br />
                {{ U1 }} V
              </el-text>
            </el-col>
            <el-col :span="8">
              <el-text line-clamp="2">
                P1:<br />
                {{ P1 }} kW
              </el-text>
            </el-col>
            <el-col :span="8">
              <el-text line-clamp="2">
                PF1:<br />
                {{ PF1 }}
              </el-text>
            </el-col>
          </el-row>
      </el-card>
    </el-col>
    <el-col :span="6" class="card-box" >
      <el-card>
        <template #header>
            <div>
              <span>B相</span>
            </div>
          </template>                                                                           
          <Echart :options="BData" :height="200" />
          <el-row class="text-container">
            <el-col :span="8">
              <el-text line-clamp="2">
                U2:<br />
                {{ U2 }} V
              </el-text>
            </el-col>
            <el-col :span="8">
              <el-text line-clamp="2">
                P2:<br />
                {{ P2 }} kW
              </el-text>
            </el-col>
            <el-col :span="8">
              <el-text line-clamp="2">
                PF2:<br />
                {{ PF2 }}
              </el-text>
            </el-col>
          </el-row>
      </el-card>
    </el-col>
    <el-col :span="6" class="card-box" >          
      <el-card>
        <template #header>
            <div>
              <span>C相</span>
            </div>
          </template>
          <Echart :options="CData" :height="200" />
          <el-row class="text-container">
            <el-col :span="8">
              <el-text line-clamp="2">
                U3:<br />
                {{ U3 }} V
              </el-text>
            </el-col>
            <el-col :span="8">
              <el-text line-clamp="2">
                P3:<br />
                {{ P3 }} kW
              </el-text>
            </el-col>
            <el-col :span="8">
              <el-text line-clamp="2">
                PF3:<br />
                {{ PF3 }}
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
            <template #default="scope">
              <el-tag type="primary" v-if="scope.row.breaker == 1">开启</el-tag>
              <el-tag type="danger" v-if="scope.row.breaker == 0">关闭</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="当前电流" align="center" prop="cur_value" />
          <el-table-column label="当前电压" align="center" prop="vol_value" />
          <el-table-column label="有功功率" align="center" prop="pow_value" />
          <el-table-column label="电能消耗" align="center" prop="ele" />
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
              <el-tag type="danger" v-if="scope.row.relay_state == 0">关闭</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="输出电流(A)" align="center" prop="cur_value" />
          <el-table-column label="有功功率(kW)" align="center" prop="pow_value" />
          <el-table-column label="功率因数" align="center" prop="pf" />
          <el-table-column label="电能消耗(kWh)" align="center" prop="ele" />
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
const electricityConsumption = ref(229.6)
const frequency = ref(50)
const PF = ref(0.66)
const U1 = ref(231.0)
const P1 = ref(0.000)
const PF1 = ref(0.00)
const U2 = ref(231.0)
const P2 = ref(0.000)
const PF2 = ref(0.00)
const U3 = ref(231.0)
const P3 = ref(0.000)
const PF3 = ref(0.00)
const totalReActivePower = ref(0);
const totalActivePower = ref(0);
const aReActivePower = ref(0);
const aActivePower = ref(0);
const bReActivePower = ref(0);
const bActivePower = ref(0);
const cReActivePower = ref(0);
const cActivePower = ref(0);

const haveB = ref(false)
const haveC = ref(false)

const totalData = reactive({
    series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        data: [
            {value: totalReActivePower, name: 'A', unit: 'kw' , itemStyle: {color: 'blue'}},
            {value: totalActivePower, name: 'B', unit: 'kVa' , itemStyle: {color: 'cyan'}},
        ],
        label: {
            show: true,
            position: 'outside',
            formatter: (params: any) => {
                let unit = '';
                if (params.data.unit) {
                    unit = params.data.unit;
                }
                return params.value + unit;
            }
        }
    }]
})

const AData = reactive({
    series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        data: [
            {value: aReActivePower, name: 'A', unit: 'kw' , itemStyle: {color: 'blue'}},
            {value: aActivePower, name: 'B', unit: 'kVa' , itemStyle: {color: 'cyan'}},
        ],
        label: {
            show: true,
            position: 'outside',
            formatter: (params: any) => {
                let unit = '';
                if (params.data.unit) {
                    unit = params.data.unit;
                }
                return params.value + unit;
            }
        }
    }]
})

const BData = reactive({
    series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        data: [
            {value: bReActivePower, name: 'A', unit: 'kw' , itemStyle: {color: 'blue'}},
            {value: bActivePower, name: 'B', unit: 'kVa' , itemStyle: {color: 'cyan'}},
        ],
        label: {
            show: true,
            position: 'outside',
            formatter: (params: any) => {
                let unit = '';
                if (params.data.unit) {
                    unit = params.data.unit;
                }
                return params.value + unit;
            }
        }
    }]
})

const CData = reactive({
    series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        data: [
            {value: cReActivePower, name: 'A', unit: 'kw' , itemStyle: {color: 'blue'}},
            {value: cActivePower, name: 'B', unit: 'kVa' , itemStyle: {color: 'cyan'}},
        ],
        label: {
            show: true,
            position: 'outside',
            formatter: (params: any) => {
                let unit = '';
                if (params.data.unit) {
                    unit = params.data.unit;
                }
                return params.value + unit;
            }
        }
    }]
})




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

  totalReActivePower.value = testData.value.pdu_data.pdu_tg_data.apparent_pow;
  totalActivePower.value = testData.value.pdu_data.pdu_tg_data.pow;
  electricityConsumption.value = testData.value.pdu_data.pdu_tg_data.ele;

  PF.value = testData.value.pdu_data.pdu_tg_data.pf;
  aReActivePower.value = testData.value.pdu_data.line_item_list.apparent_pow[0];
  aActivePower.value = testData.value.pdu_data.line_item_list.pow_value[0];
  U1.value = testData.value.pdu_data.line_item_list.vol_value[0];
  P1.value =testData.value.pdu_data.line_item_list.pow_value[0];
  PF1.value = testData.value.pdu_data.line_item_list.pf[0];

  if(testData.value.pdu_data.line_item_list.ele.length > 1){
    bReActivePower.value = testData.value.pdu_data.line_item_list.apparent_pow[1];
    bActivePower.value = testData.value.pdu_data.line_item_list.pow_value[1];
    U2.value = testData.value.pdu_data.line_item_list.vol_value[1];
    P2.value =testData.value.pdu_data.line_item_list.pow_value[1];
    PF2.value = testData.value.pdu_data.line_item_list.pf[1];
    haveB.value = true;
  }
  if(testData.value.pdu_data.line_item_list.ele.length > 2){
    cReActivePower.value = testData.value.pdu_data.line_item_list.apparent_pow[2];
    cActivePower.value = testData.value.pdu_data.line_item_list.pow_value[2];
    U3.value = testData.value.pdu_data.line_item_list.vol_value[2];
    P3.value =testData.value.pdu_data.line_item_list.pow_value[2];
    PF3.value = testData.value.pdu_data.line_item_list.pf[2];
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

