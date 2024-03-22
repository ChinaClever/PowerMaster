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
      <el-collapse  v-model="activeNames">
        <el-collapse-item title="A相设置" name="1">

            <el-row class="text-container"> 
              <el-col :span="10" :ondblclick="handleDoubleClick">
                电流当前值 {{ currentA }}A <el-progress :text-inside="true" :stroke-width="20" :percentage="0" style="display: inline-flex;width: 60%;"/>
              </el-col>
              <el-col :span="8">
                电压当前值 {{ voltageA }}V <el-progress :text-inside="true" :stroke-width="20" :percentage="70" style="display: inline-flex;width: 60%;"/>
              </el-col>
            </el-row>
            <el-row class="text-container"> 
              <el-col :span="10">
                频率 {{ frequencyA }}Hz <el-progress :text-inside="true" :stroke-width="20" :percentage="0" style="display: inline-flex;width: 60%;"/>
              </el-col>
              <el-col :span="8">
                功率当前值 {{ powerA }}kW 
              </el-col>
            </el-row>
            <el-row class="text-container"> 
              温度当前值 {{ temperatureA }}°C
            </el-row>

        </el-collapse-item>
        <el-collapse-item title="B相设置" name="2">

            <el-row class="text-container"> 
              <el-col :span="10">
                电流当前值 {{ currentB }}A <el-progress :text-inside="true" :stroke-width="20" :percentage="0" style="display: inline-flex;width: 60%;"/>
              </el-col>
              <el-col :span="8">
                电压当前值 {{ voltageB }}V <el-progress :text-inside="true" :stroke-width="20" :percentage="70" style="display: inline-flex;width: 60%;"/>
              </el-col>
            </el-row>
            <el-row class="text-container"> 
              <el-col :span="10">
                频率 {{ frequencyB }}Hz <el-progress :text-inside="true" :stroke-width="20" :percentage="0" style="display: inline-flex;width: 60%;"/>
              </el-col>
              <el-col :span="8">
                功率当前值 {{ powerB }}kW 
              </el-col>
            </el-row>
            <el-row class="text-container"> 
              温度当前值 {{ temperatureB }}°C
            </el-row>

        </el-collapse-item>
        <el-collapse-item title="C相设置" name="3">

            <el-row class="text-container"> 
              <el-col :span="10">
                电流当前值 {{ currentC }}A <el-progress :text-inside="true" :stroke-width="20" :percentage="0" style="display: inline-flex;width: 60%;"/>
              </el-col>
              <el-col :span="8">
                电压当前值 {{ voltageC }}V <el-progress :text-inside="true" :stroke-width="20" :percentage="70" style="display: inline-flex;width: 60%;"/>
              </el-col>
            </el-row>
            <el-row class="text-container"> 
              <el-col :span="10">
                频率 {{ frequencyC }}Hz <el-progress :text-inside="true" :stroke-width="20" :percentage="0" style="display: inline-flex;width: 60%;"/>
              </el-col>
              <el-col :span="8">
                功率当前值 {{ powerC }}kW 
              </el-col>
            </el-row>
            <el-row class="text-container"> 
              温度当前值 {{ temperatureC }}°C
            </el-row>

        </el-collapse-item>

            零线 
            <el-row class="text-container"> 
              <el-col :span="10">
                电流当前值 {{ neutralCurrent }}A 
              </el-col>
              <el-col :span="8">
                温度当前值 {{ neutralTemperature }}°C
              </el-col>
            </el-row>

        <el-collapse-item title="支路设置" name="4">
          <el-table v-loading="loading" :data="loopList" :stripe="true" :show-overflow-tooltip="true">
            <el-table-column label="插接箱" align="center" prop="pluginBox" /> 
            <el-table-column label="A1电流" align="center" prop="A1Current" /> 
            <el-table-column label="B1电流" align="center" prop="B1Current" /> 
            <el-table-column label="C1电流" align="center" prop="C1Current" /> 
            <el-table-column label="A1功率" align="center" prop="A1Power" /> 
            <el-table-column label="B1功率" align="center" prop="B1Power" /> 
            <el-table-column label="C1功率" align="center" prop="C1Power" /> 
          </el-table>
           
          <el-table v-loading="loading" :data="loopList" :stripe="true" :show-overflow-tooltip="true">
            <el-table-column label="插接箱" align="center" prop="pluginBox" /> 
            <el-table-column label="A2电流" align="center" prop="A2Current" /> 
            <el-table-column label="B2电流" align="center" prop="B2Current" /> 
            <el-table-column label="C2电流" align="center" prop="C2Current" /> 
            <el-table-column label="A2功率" align="center" prop="A2Power" /> 
            <el-table-column label="B2功率" align="center" prop="B2Power" /> 
            <el-table-column label="C2功率" align="center" prop="C2Power" /> 
          </el-table>
          <el-table v-loading="loading" :data="loopList" :stripe="true" :show-overflow-tooltip="true">
            <el-table-column label="插接箱" align="center" prop="pluginBox" /> 
            <el-table-column label="A3电流" align="center" prop="A3Current" /> 
            <el-table-column label="B3电流" align="center" prop="B3Current" /> 
            <el-table-column label="C3电流" align="center" prop="C3Current" /> 
            <el-table-column label="A3功率" align="center" prop="A3Power" /> 
            <el-table-column label="B3功率" align="center" prop="B3Power" /> 
            <el-table-column label="C3功率" align="center" prop="C3Power" /> 
          </el-table>

          <el-table v-loading="loading" :data="loopList" :stripe="true" :show-overflow-tooltip="true">
            <el-table-column label="插接箱" align="center" prop="pluginBox" /> 
            <el-table-column label="温度1" align="center" prop="temperature1" /> 
            <el-table-column label="温度2" align="center" prop="temperature2" /> 
            <el-table-column label="温度3" align="center" prop="temperature3" /> 
            <el-table-column label="温度4" align="center" prop="temperature4" /> 
            <el-table-column label="中性电流" align="center" prop="neutralCurrent" /> 
            <el-table-column label="零线电流" align="center" prop="neutralCurrent" /> 
            <el-table-column label="零线温度" align="center" prop="neutralTemperature" /> 
          </el-table>

        </el-collapse-item>
      </el-collapse>
    </el-col>
  </el-row>
  <!-- 表单弹窗：添加/修改 -->
  <PDUDeviceForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import download from '@/utils/download'
import { PDUDeviceApi, PDUDeviceVO } from '@/api/pdu/pdudevice'

import { ElTree } from 'element-plus'

/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })


const serverRoomArr =  [
  {
    value: '1',
    label: '机房1',
    children: [
      {
        value: '1-1',
        label: '母线1',
        // children: [
        // {
        //   value: '1-1-1',
        //   label: '机柜1',
        // },
        // {
        //   value: '1-1-2',
        //   label: '机柜2',
        // },]
      },
    ],
  },
  {
    value: '2',
    label: '机房2',
    children: [
      {
        value: '2-1',
        label: '母线1',
        // children: [
        // {
        //   value: '2-1-1',
        //   label: '机柜1',
        // },
        // {
        //   value: '2-1-2',
        //   label: '机柜2',
        // },]
      },
    ],
  },
  {
    value: '3',
    label: '机房3',
    children: [
      {
        value: '3-1',
        label: '母线1',
        // children: [
        // {
        //   value: '3-1-1',
        //   label: '机柜1',
        // },
        // {
        //   value: '3-1-2',
        //   label: '机柜2',
        // },]
      },
      {
        value: '3-2',
        label: '母线2',
        // children: [
        // {
        //   value: '3-1-1',
        //   label: '机柜1',
        // },
        // {
        //   value: '3-1-2',
        //   label: '机柜2',
        // },]
      },
    ],
  },
]

//折叠功能
let treeWidth = ref(3)
let isCollapsed = ref(0);

const currentA = ref(0.14);
const voltageA = ref(234.5);
const frequencyA = ref(49.9);
const powerA = ref(0.567);
const temperatureA = ref(23);
const currentB = ref(0.14);
const voltageB = ref(234.5);
const frequencyB = ref(49.9);
const powerB = ref(0.567);
const temperatureB = ref(23);
const currentC = ref(0.14);
const voltageC = ref(234.5);
const frequencyC = ref(49.9);
const powerC = ref(0.567);
const temperatureC = ref(23);
const neutralCurrent = ref(0)
const neutralTemperature = ref(20)


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

const handleDoubleClick = ()=> {
  // 在这里编写双击事件的处理逻辑
  console.log("双击事件触发了");
}

const defaultProps = {
  children: 'children',
  label: 'label',
}

watch(filterText, (val) => {
  treeRef.value!.filter(val)
})

// 下拉框选项数组
const deviceStatus = ref([])

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const loading = ref(false) // 列表的加载中
const list = ref([
  { 
    id:"1",
    status:"空闲设备",
    totalApparentPower:"200kW",
    totalActivePower:"210kVA",
    totalElectricalEnergy:"10.112kWh",
    ipAddr:"192.168.1.1-0",
    location:"机房2-机柜1-A路",
    updateTime:"15:25:00"
  },
  { 
    id:"2",
    status:"离线设备",
    totalApparentPower:"200kW",
    totalActivePower:"210kVA",
    totalElectricalEnergy:"10.112kWh",
    ipAddr:"192.168.1.2-1",
    location:"机房2-机柜2-B路",
    updateTime:"15:25:00"
  },{ 
    id:"3",
    status:"未绑定设备",
    totalApparentPower:"200kW",
    totalActivePower:"210kVA",
    totalElectricalEnergy:"10.112kWh",
    ipAddr:"192.168.1.3-2",
    location:"机房2-机柜3-C路",
    updateTime:"15:25:00"
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
  serverRoomData:undefined,
  status:undefined,
})
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中

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
  // getList()
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
  // getList()
})
</script>

