<template>
  <el-collapse  v-model="activeNames">
    <el-collapse-item title="输入信息" name="1">
      <ContentWrap>
        总有功功率:{{totalActivePower}}kW 总视在功率:{{totalApparentPower}}kW 总无功功率:{{totalReactivePower}}kVar 
        零线电流:{{neutralCurrent}}A 零线温度:{{neutralTemperature}}°C
        <el-table v-loading="loading" :data="inputList" :stripe="true" :show-overflow-tooltip="true">
          <el-table-column label="输入" align="center" prop="input" /> 
          <el-table-column label="电压" align="center" prop="voltage" />
          <el-table-column label="电流" align="center" prop="current" /> 
          <el-table-column label="负载率" align="center" prop="loadRate" />
          <el-table-column label="电流谐波含量" align="center" prop="currentHarmonicContent" /> 
          <el-table-column label="功率" align="center" prop="power" /> 
          <el-table-column label="电能" align="center" prop="electricalEnergy" /> 
          <el-table-column label="温度" align="center" prop="temperature" />
        </el-table>
      </ContentWrap>
    </el-collapse-item>
    <el-collapse-item title="插拔位" name="2">
      <ContentWrap>
        <el-table v-loading="loading" :data="insertionList" :stripe="true" :show-overflow-tooltip="true">
          <el-table-column label="编号" align="center" prop="no" />
          <el-table-column label="总有功功率" align="center" prop="totalActivePower" />
          <el-table-column label="总视在功率" align="center" prop="totalApparentPower" />
          <el-table-column label="总电能" align="center" prop="totalReactivePower" />
          <el-table-column label="对应机柜" align="center" prop="cabinet" />
        </el-table>
      </ContentWrap>
    </el-collapse-item>
    <el-collapse-item title="回路信息" name="3">
      <ContentWrap>
        <el-table v-loading="loading" :data="loopList" :stripe="true" :show-overflow-tooltip="true">
          <el-table-column label="编号" align="center" prop="no" /> 
          <el-table-column label="名称" align="center" prop="name" /> 
          <el-table-column label="继路器" align="center" prop="relay" /> 
          <el-table-column label="电压" align="center" prop="voltage" /> 
          <el-table-column label="电流" align="center" prop="current" /> 
          <el-table-column label="功率" align="center" prop="power" /> 
          <el-table-column label="功率因素" align="center" prop="powerFactor" /> 
          <el-table-column label="电能" align="center" prop="electricalEnergy" />
        </el-table>
      </ContentWrap>
    </el-collapse-item>
  </el-collapse>

</template>

<script setup lang="ts">

import download from '@/utils/download'
import { PDUDeviceApi, PDUDeviceVO } from '@/api/pdu/pdudevice'


/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化
const activeNames = ref(['1']) //默认展开的折叠列

const totalActivePower = ref(220)
const totalApparentPower = ref(220)
const totalReactivePower = ref(0)
const neutralCurrent = ref(0)
const neutralTemperature = ref(20)

const loading = ref(true) // 列表的加载中
const inputList = ref([
  {
    input: 'A',
    voltage: '238.9V',
    current: '0.022A',
    loadRate:"1.0%",
    currentHarmonicContent:"4.26%",
    power:"0kW",
    electricalEnergy:"0.000kWh",
    temperature:"0°C",
  },
  {
    input: 'B',
    voltage: '235.1V',
    current: '0.022A',
    loadRate:"1.0%",
    currentHarmonicContent:"4.22%",
    power:"0kW",
    electricalEnergy:"0.000kWh",
    temperature:"0°C",
  },
  {
    input: 'C',
    voltage: '235.5V',
    current: '0.023A',
    loadRate:"1.0%",
    currentHarmonicContent:"4.23%",
    power:"0kW",
    electricalEnergy:"0.000kWh",
    temperature:"0°C",
  },
]) // 列表的数据

const insertionList = ref([
  {
    no:"1",
    totalActivePower: '220kW',
    totalApparentPower: '0kW',
    totalReactivePower: '0kVar',
    cabinet:"机房1-柜列1-机柜2-A",
  },
  {
    no:"2",
    totalActivePower: '220kW',
    totalApparentPower: '0kW',
    totalReactivePower: '0kVar',
    cabinet:"机房1-柜列1-机柜2-B",
  },
  {
    no:"3",
    totalActivePower: '220kW',
    totalApparentPower: '0kW',
    totalReactivePower: '0kVar',
    cabinet:"机房1-柜列1-机柜2-C",
  },
]) // 列表的数据

const loopList = ref([
  {
    no: '1',
    name: 'loop1',
    relay: '关',
    voltage:"238.0V",
    current:"0.020A",
    power:"0kW",
    powerFactor:"0.0",
    electricalEnergy:"0.000kWh",
  },
  {
    no: '2',
    name: 'loop2',
    relay: '开',
    voltage:"238.0V",
    current:"0.020A",
    power:"0kW",
    powerFactor:"0.0",
    electricalEnergy:"0.000kWh",
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

