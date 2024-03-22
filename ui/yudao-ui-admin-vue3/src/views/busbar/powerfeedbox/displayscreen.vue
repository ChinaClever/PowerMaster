<template>

  <ContentWrap>
    总有功功率:{{totalActivePower}}kW 总视在功率:{{totalApparentPower}}kW 总无功功率:{{totalReactivePower}}kVar 总电能{{totalElectricalEnergy }}kWh
    A相温度:{{ATemperature}}°C B相温度:{{BTemperature}}°C C相温度:{{CTemperature}}°C 零线温度:{{neutralTemperature}}°C
    过载电流:{{overloadCurrent}}A 负载率:{{loadFactor}}% 版本:{{ version }}
    <el-table v-loading="loading" :data="inputList" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="输入" align="center" prop="input" /> 
      <el-table-column label="电压" align="center" prop="voltage" />
      <el-table-column label="电流" align="center" prop="current" /> 
      <el-table-column label="过载电流" align="center" prop="overloadCurrent" /> 
      <el-table-column label="有功功率" align="center" prop="activePower" />
      <el-table-column label="功率因数" align="center" prop="powerFactor" />
      <el-table-column label="无功功率" align="center" prop="reactivePower" />  
      <el-table-column label="电能" align="center" prop="electricalEnergy" /> 
    </el-table>
  </ContentWrap>
  <ContentWrap>
    <span>谐波含量</span>
    <el-table v-loading="loading" :data="harmonicContent" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="L" align="center" prop="L" />
      <el-table-column label="负载率(%)" align="center" prop="loadFactor" />
      <el-table-column label="电压谐波含量(%)" align="center" prop="voltageHarmonicContent" />
      <el-table-column label="电流谐波含量(%)" align="center" prop="currentHarmonicContent" />
    </el-table>
  </ContentWrap>  
  <ContentWrap>
    <el-button>Ua</el-button>
    <el-button>Ub</el-button>
    <el-button>Uc</el-button>
    <el-button>Ia</el-button>
    <el-button>Ib</el-button>
    <el-button>Ic</el-button>
    <span>单相谐波含量xx</span>
  </ContentWrap> 

   <ContentWrap>
    <span>各次谐波含量THD(%)</span>
    <el-table v-loading="loading" :data="thd" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="1" align="center" prop="one" />
      <el-table-column label="2" align="center" prop="two" />
      <el-table-column label="3" align="center" prop="three" />
      <el-table-column label="4" align="center" prop="four" />
      <el-table-column label="5" align="center" prop="five" />
      <el-table-column label="6" align="center" prop="six" />
      <el-table-column label="7" align="center" prop="seven" />
      <el-table-column label="8" align="center" prop="eight" />
      <el-table-column label="9" align="center" prop="nine" />
      <el-table-column label="10" align="center" prop="ten" />
      <el-table-column label="11" align="center" prop="eleven" />
      <el-table-column label="12" align="center" prop="twelve" />
    </el-table>
  </ContentWrap>

  <ContentWrap>
    <Echart :options="analysis" :height="400" />
  </ContentWrap> 

  
</template>

<script setup lang="ts">

import download from '@/utils/download'
import { PDUDeviceApi } from '@/api/pdu/pdudevice'


/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const totalActivePower = ref(220)
const totalApparentPower = ref(220)
const totalReactivePower = ref(0)
const totalElectricalEnergy = ref(220)
const neutralTemperature = ref(20)
const ATemperature = ref(20)
const BTemperature = ref(20)
const CTemperature = ref(20)
const version = ref("V3.0.0")
const overloadCurrent = ref(40.00)
const loadFactor = ref(30)

const loading = ref(true) // 列表的加载中
const inputList = ref([
  {
    input: 'A',
    voltage: '238.9V',
    current: '0.000A',
    overloadCurrent:"40.000A",
    activePower:"0.000kW",
    powerFactor:"0.00",
    reactivePower:"0kVar",
    electricalEnergy:"1.9kWh",
  },
  {
    input: 'B',
    voltage: '237.9V',
    current: '0.000A',
    overloadCurrent:"40.000A",
    activePower:"0.000kW",
    powerFactor:"0.00",
    reactivePower:"0kVar",
    electricalEnergy:"1.9kWh",
  },
  {
    input: 'C',
    voltage: '237.4V',
    current: '0.000A',
    overloadCurrent:"40.000A",
    activePower:"0.000kW",
    powerFactor:"0.00",
    reactivePower:"0kVar",
    electricalEnergy:"1.9kWh",
  },
]) // 列表的数据

const harmonicContent = ref([
  {
    L: 'A',
    loadFactor: '0',
    voltageHarmonicContent: '0',
    currentHarmonicContent:"0",
  },
  {
    L: 'B',
    loadFactor: '0',
    voltageHarmonicContent: '0',
    currentHarmonicContent:"0",
  },
  {
    L: 'C',
    loadFactor: '0',
    voltageHarmonicContent: '0',
    currentHarmonicContent:"0",
  },
]) // 列表的数据

const thd = ref([
  {
    "one": "1.55",
    "two": "0.78",
    "three": "3.12",
    "four": "0.23",
    "five": "2.87",
    "six": "0.99",
    "seven": "1.34",
    "eight": "2.76",
    "nine": "0.45",
    "ten": "3.89",
    "eleven": "1.23",
    "twelve": "2.01"
  },
  {
    "one": "3.45",
    "two": "0.12",
    "three": "1.78",
    "four": "2.33",
    "five": "0.67",
    "six": "3.99",
    "seven": "0.56",
    "eight": "1.89",
    "nine": "2.76",
    "ten": "0.45",
    "eleven": "3.01",
    "twelve": "1.23"
  },
  {
    "one": "2.10",
    "two": "0.78",
    "three": "3.45",
    "four": "1.23",
    "five": "0.34",
    "six": "2.89",
    "seven": "1.67",
    "eight": "3.01",
    "nine": "0.56",
    "ten": "2.78",
    "eleven": "1.99",
    "twelve": "0.45"
  }
  
]) // 列表的数据

const analysis = ref({
  xAxis: {
    type: 'category', // x轴类型为类目轴
    data: ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20',
     '21', '22', '23', '24', '25', '26', '27', '28', '29', '30', '31', '32'] // x轴的数据，表示时间段或类别
  },
  yAxis: {
    type: 'value', // y轴类型为数值轴
    max: 16,
    interval: 1, // 刻度的间隔
  },
  series: [{
    data: [4,0,4,0,1,2,3,0,1,0,2,4,1,2,0,2,1,2,3,1,2,1,2,3,1,2,3,1,2,1,2,1,1], // y轴的数据
    type: 'bar' // 柱状图类型
  }]
});

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
})
</script>

