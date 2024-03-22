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
                机房2-机柜1-A路
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
          <Echart :options="totalData" :height="200" />
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
    <el-col :span="6" class="card-box">
      <el-card>
        <template #header>
            <div>
              <span>B相</span>
            </div>
          </template>                                                                           
          <Echart :options="totalData" :height="200" />
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
    <el-col :span="6" class="card-box">          
      <el-card>
        <template #header>
            <div>
              <span>C相</span>
            </div>
          </template>
          <Echart :options="totalData" :height="200" />
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
        <el-table v-loading="loading" :data="circleList" :stripe="true" :show-overflow-tooltip="true">
          <el-table-column label="回路" align="center" prop="circuit" />
          <el-table-column label="断路器状态" align="center" prop="breakerStatus" />
          <el-table-column label="输出位" align="center" prop="outputPosition" />
          <el-table-column label="当前电流" align="center" prop="current" />
          <el-table-column label="额定电流" align="center" prop="ratedCurrent" />
          <el-table-column label="当前电压" align="center" prop="currentVoltage" />
          <el-table-column label="有功功率" align="center" prop="activePower" />
          <el-table-column label="电能消耗" align="center" prop="energyConsumption" />
        </el-table>
      </ContentWrap>
    </el-collapse-item>
    <el-collapse-item title="传感器" name="2">
      <ContentWrap>
        <el-table v-loading="loading" :data="sensorList" :stripe="true" :show-overflow-tooltip="true">
          <el-table-column label="传感器名称" align="center" prop="sensorName" />
          <el-table-column label="传感器状态" align="center" prop="sensorStatus" />
          <el-table-column label="传感器名称" align="center" prop="sensorName2" />
          <el-table-column label="传感器状态" align="center" prop="sensorStatus2" />
        </el-table>
      </ContentWrap>
    </el-collapse-item>
    <el-collapse-item title="输出位" name="3">
      <ContentWrap>
        <el-table v-loading="loading" :data="output" :stripe="true" :show-overflow-tooltip="true">
          <el-table-column label="序号" align="center" prop="no" />
          <el-table-column label="名称" align="center" prop="name" />
          <el-table-column label="开关状态" align="center" prop="switchState" />
          <el-table-column label="输出电流(A)" align="center" prop="outputCurrent" />
          <el-table-column label="有功功率(kW)" align="center" prop="activePower" />
          <el-table-column label="功率因数" align="center" prop="powerFactor" />
          <el-table-column label="电能消耗(kWh)" align="center" prop="energyConsumption" />
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

/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const loading = ref(true) // 列表的加载中
const circleList = ref([
  {
    circuit: 'C1',
    breakerStatus: '闭合',
    outputPosition: '1-8',
    current:"0.00A",
    ratedCurrent:"16.00A",
    currentVoltage:"231.0V",
    activePower:"0.000kW",
    energyConsumption:"0.0kWh",
  },
  {
    circuit: 'C2',
    breakerStatus: '闭合',
    outputPosition: '1-8',
    current:"0.00A",
    ratedCurrent:"16.00A",
    currentVoltage:"231.0V",
    activePower:"0.000kW",
    energyConsumption:"0.0kWh",
  },
  {
    circuit: 'C3',
    breakerStatus: '闭合',
    outputPosition: '1-8',
    current:"0.00A",
    ratedCurrent:"16.00A",
    currentVoltage:"231.0V",
    activePower:"0.000kW",
    energyConsumption:"0.0kWh",
  },
]) // 列表的数据

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

const output = ref([
  {
    no: '1',
    name: 'Output1',
    switchState: '关',
    outputCurrent:"0.00",
    activePower:"0.000",
    powerFactor:"0.00",
    energyConsumption:"0.0",
  },
  {
    no: '2',
    name: 'Output2',
    switchState: '关',
    outputCurrent:"0.00",
    activePower:"0.000",
    powerFactor:"0.00",
    energyConsumption:"0.0",
  },
  {
    no: '3',
    name: 'Output3',
    switchState: '关',
    outputCurrent:"0.00",
    activePower:"0.000",
    powerFactor:"0.00",
    energyConsumption:"0.0",
  },
  {
    no: '4',
    name: 'Output4',
    switchState: '关',
    outputCurrent:"0.00",
    activePower:"0.000",
    powerFactor:"0.00",
    energyConsumption:"0.0",
  },
  {
    no: '5',
    name: 'Output5',
    switchState: '关',
    outputCurrent:"0.00",
    activePower:"0.000",
    powerFactor:"0.00",
    energyConsumption:"0.0",
  },
]) // 列表的数据

const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  devKey: undefined,
  ipAddr: undefined,
  createTime: [],
  cascadeNum: undefined,
})

const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中


const totalData = reactive({
    series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        data: [
            {value: 335, name: 'A', unit: 'kw' , itemStyle: {color: 'blue'}},
            {value: 310, name: 'B', unit: 'kVa' , itemStyle: {color: 'cyan'}},
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


/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await PDUDeviceApi.getPDUDevicePage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

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

/** 初始化 **/
onMounted(() => {
  getList()
  initChart();
  addDataPoint();
})
</script>

