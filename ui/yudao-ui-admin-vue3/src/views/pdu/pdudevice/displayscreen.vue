<template>
 <div style="background-color: #E7E7E7;">
  <div class="header_app">
    <div class="header_app_text">所在位置：{{ location2 }}</div>
    <div class="header_app_text_other1">
          <el-col :span="10">
            <el-form
              class="-mb-15px"
              :model="queryParams"
              ref="queryFormRef"
              :inline="true"
              label-width="120px"
            >
              <el-form-item label="网络地址" prop="devKey" >
              <el-autocomplete
                v-model="queryParams.devKey"
                :fetch-suggestions="querySearch"
                placeholder="请输入网络地址"  
                clearable
                class="!w-160px"
                @select="handleQuery" 
              />
              </el-form-item>
            </el-form>
          </el-col>      
    </div>
    <div class="header_app_text_other">
      <el-button @click="handleQuery"  ><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
    </div>
    <div class="header_app_text_other">
          <el-col :span="2">
            <el-button type="primary" @click="openNewPage(queryParams.devKey)" >进入管理界面</el-button>
          </el-col>
    </div>
  </div>

  <!-- <el-row :gutter="18">
    <el-col>
      <el-card >
        <el-row  :gutter="18">
          <el-col :span="5">
            <el-text line-clamp="2">                  
              <el-text class="mx-1" size="large">所在位置：{{ location }}</el-text>
            </el-text>
          </el-col>

          <el-col :span="5">
            <el-form
              class="-mb-15px"
              :model="queryParams"
              ref="queryFormRef"
              :inline="true"
              label-width="120px"
            >
              <el-form-item label="网络地址" prop="devKey" >
              <el-autocomplete
                v-model="queryParams.devKey"
                :fetch-suggestions="querySearch"
                placeholder="请输入网络地址"  
                clearable
                class="!w-160px"
                @select="handleQuery"
              />
               <el-button @click="handleQuery"  ><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
              </el-form-item>
            </el-form>
          </el-col>
          <el-col :span="2">
            <el-button type="primary" @click="openNewPage(queryParams.devKey)" >进入管理界面</el-button>
          </el-col>
        </el-row>
      </el-card>
    </el-col>
  </el-row> -->

<!-- <el-col :span="5">
  <el-text line-clamp="2">
    <el-text class="mx-1" size="large">网络地址：{{ queryParams.devKey }}</el-text>
  </el-text>
</el-col> -->

    <!-- <el-form-item label="级联地址" prop="cascadeAddr" label-width="70px">
      <el-input-number
        v-model="queryParams.cascadeAddr"
        :min="0"
        controls-position="right"
        :value-on-clear="0"
          class="!w-100px"
      />
    </el-form-item> -->
  <div>
    <el-row :gutter="20" style="margin: 0px; margin-top : 10px;margin-top : 10px" >
      <el-col :span="6" class="card-box" >
        <el-card >
          <template #header>
            <el-row  class="text-container"> 
              <el-col :span="12">
                <el-text line-clamp="2">
                  总数据
                </el-text>
              </el-col>   
            </el-row>
          </template>
          <el-row justify="center">
            <div ref="totalChartContainer" id="totalChartContainer" style="width: 400px; height: 200px;"></div>
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
              <div ref="AChartContainer" id="AChartContainer" style="width: 400px; height: 200px;"></div>
            </el-row>
            <el-row class="text-container">
              <el-col :span="8">
                <el-text line-clamp="2" :style="{ backgroundColor: A.volColor,color: A.volTextColor }">
                  U1:<br />
                  {{ A.vol_value }} V
                </el-text>
              </el-col>
              <el-col :span="8">
                <el-text line-clamp="2" :style="{ backgroundColor: A.powColor,color: A.powTextColor }">
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
      <el-col :span="6" class="card-box" v-if="controlVis.haveB" >
        <el-card>
          <template #header>
              <div>
                <span>B相</span>
              </div>
            </template>                                                                           
            <el-row justify="center">
              <div ref="BChartContainer" id="BChartContainer" style="width: 400px; height: 200px;"></div>
            </el-row>
            <el-row class="text-container">
              <el-col :span="8">
                <el-text line-clamp="2"  :style="{ backgroundColor: B.volColor,color: B.volTextColor }">
                  U2:<br />
                  {{ B.vol_value }} V
                </el-text>
              </el-col>
              <el-col :span="8">
                <el-text line-clamp="2" :style="{ backgroundColor: B.powColor,color: B.powTextColor }">
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
              <div ref="CChartContainer" id="CChartContainer" style="width: 400px; height: 200px;"></div>
            </el-row>
            <el-row class="text-container">
              <el-col :span="8">
                <el-text line-clamp="2"  :style="{ backgroundColor: C.volColor,color:C.volTextColor }">
                  U3:<br />
                  {{ C.vol_value }} V
                </el-text>
              </el-col>
              <el-col :span="8">
                <el-text line-clamp="2" :style="{ backgroundColor: C.powColor,color: C.powTextColor}">
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
      <el-card style="margin: 10px;">
        <el-row>
          <el-col >
            <span style="width: 100%">总功率趋势图</span>
          </el-col>
          <el-col >
            <div style="float:right;margin-top: 0;">
              <el-form-item  prop="type">
                <el-select v-if="queryParams.powGranularity != 'oneHour'" v-model="typeRadioShowPower" placeholder="请选择" style="width: 100px;margin-right: 15px;">
                  <el-option label="平均" value="平均" />
                  <el-option label="最大" value="最大" />
                  <el-option label="最小" value="最小" />
                </el-select>
                <el-button @click="queryParams.powGranularity = `oneHour`;switchValue = 0;" :type="switchValue === 0 ? 'primary' : ''">近一小时</el-button>
                <el-button @click="queryParams.powGranularity = `twentyfourHour`;switchValue = 1;" :type="switchValue === 1 ? 'primary' : ''">近一天</el-button>
                <el-button @click="queryParams.powGranularity = `seventytwoHour`;switchValue = 2;" :type="switchValue === 2 ? 'primary' : ''">近三天</el-button>
                <el-button @click="queryParams.powGranularity = `oneMonth`;switchValue = 3;" :type="switchValue === 3 ? 'primary' : ''">近一个月</el-button>
              </el-form-item>
            </div>
          </el-col> 
        </el-row>
        <div style="display: flex; justify-content: center; align-items: center;">
          <div ref="chartContainer" id="chartContainer" class="adaptiveStyle"></div>
        </div>
      </el-card>

      <el-card style="margin: 10px;">
        <el-row>
          <el-col >
            <span style="width: 100%">功率因数趋势图</span>
          </el-col>
          <el-col >
            <div style="float:right;margin-top: 0;">
              <el-form-item  prop="type">
                <el-select v-if="queryParams.powGranularityF != 'oneHour'" v-model="typeRadioShowFactor" placeholder="请选择" style="width: 100px;margin-right: 15px;">
                  <el-option label="平均" value="平均" />
                  <el-option label="最大" value="最大" />
                  <el-option label="最小" value="最小" />
                </el-select>
                <el-button @click="queryParams.powGranularityF = `oneHour`;switchValueF = 0;" :type="switchValueF === 0 ? 'primary' : ''">近一小时</el-button>
                <el-button @click="queryParams.powGranularityF = `twentyfourHour`;switchValueF = 1;" :type="switchValueF === 1 ? 'primary' : ''">近一天</el-button>
                <el-button @click="queryParams.powGranularityF = `seventytwoHour`;switchValueF = 2;" :type="switchValueF === 2 ? 'primary' : ''">近三天</el-button>
                <el-button @click="queryParams.powGranularityF = `oneMonth`;switchValueF = 3;" :type="switchValueF === 3 ? 'primary' : ''">近一个月</el-button>
              </el-form-item>
            </div>
          </el-col> 
        </el-row>
        <div style="display: flex; justify-content: center; align-items: center;">
          <div ref="chartContainerF" id="chartContainerF" class="adaptiveStyle"></div>
        </div>
      </el-card>

      <el-card style="margin: 10px;">
        <el-row>
          <el-col>
            <span style="width: 100%">输入相的电流趋势图</span>
          </el-col>
          <el-col>
            <div style="float:right;margin-top: 0px;">
              <el-form-item  prop="type">
                <el-select v-if="toggleTime != 'oneHour'" v-model="typeRadioShowCur" placeholder="请选择" style="width: 100px;margin-right: 15px;">
                  <el-option label="平均" value="平均" />
                  <el-option label="最大" value="最大" />
                  <el-option label="最小" value="最小" />
                </el-select>
                <el-button @click="toggleTime = `oneHour`;toggleValue = 0;" :type="toggleValue === 0 ? 'primary' : ''">近一小时</el-button>
                <el-button @click="toggleTime = `twentyfourHour`;toggleValue = 1;" :type="toggleValue === 1 ? 'primary' : ''">近一天</el-button>
                <el-button @click="toggleTime = `seventytwoHour`;toggleValue = 2;" :type="toggleValue === 2 ? 'primary' : ''">近三天</el-button>
                <el-button @click="toggleTime = `oneMonth`;toggleValue = 3;" :type="toggleValue === 3 ? 'primary' : ''">近一个月</el-button>
              </el-form-item>
            </div>
          </el-col> 
        </el-row>
        <div style="display: flex; justify-content: center; align-items: center;">
          <div ref="lineidChartContainer" id="lineidChartContainer" class="adaptiveStyle"></div>
        </div>
      </el-card>

      <el-card style="margin: 10px;">
        <el-row>
          <el-col>
            <span style="width: 100%">输入相的电压趋势图</span>
          </el-col>
          <el-col>
            <div style="float:right;margin-top: 0px;">
              <el-form-item  prop="type">
                <el-select v-if="toggleTimeV != 'oneHour'" v-model="typeRadioShowVol" placeholder="请选择" style="width: 100px;margin-right: 15px;">
                  <el-option label="平均" value="平均" />
                  <el-option label="最大" value="最大" />
                  <el-option label="最小" value="最小" />
                </el-select>
                <el-button @click="toggleTimeV = `oneHour`;toggleValueV = 0;" :type="toggleValueV === 0 ? 'primary' : ''">近一小时</el-button>
                <el-button @click="toggleTimeV = `twentyfourHour`;toggleValueV = 1;" :type="toggleValueV === 1 ? 'primary' : ''">近一天</el-button>
                <el-button @click="toggleTimeV = `seventytwoHour`;toggleValueV = 2;" :type="toggleValueV === 2 ? 'primary' : ''">近三天</el-button>
                <el-button @click="toggleTimeV = `oneMonth`;toggleValueV = 3;" :type="toggleValueV === 3 ? 'primary' : ''">近一个月</el-button>
              </el-form-item>
            </div>
          </el-col> 
        </el-row>
        <div style="display: flex; justify-content: center; align-items: center;">
          <div ref="lineidChartContainerV" id="lineidChartContainerV" class="adaptiveStyle"></div>
        </div>
      </el-card>

      <el-collapse-item name="1" v-if="controlVis.haveCircle" style="margin: 15px 10px 15px 10px; ">
        <template #title>
          <div style="width: 5%;font-size: 16px;">回路</div>
        </template>
        <ContentWrap>
          <el-table  :data="circleList" :stripe="true" :show-overflow-tooltip="true" :empty-text="'ajsdhj'">
            <el-table-column label="回路" align="center" prop="circuit" />
            <el-table-column label="断路器状态" align="center" prop="breaker" v-if="controlVis.circleTableCol.breaker"> 
              <template #default="scope" >
                <el-tag type="" v-if="scope.row.breaker == 1">闭合</el-tag>
                <el-tag type="danger" v-if="scope.row.breaker == 0">断开</el-tag>
              </template>
            </el-table-column>                        
            <el-table-column label="当前电流(A)" align="center" prop="cur_value" v-if="controlVis.circleTableCol.cur_value" >
              <template #default="scope" >
                <el-text line-clamp="2"  :style="{ backgroundColor: scope.row.curColor }">
                  {{ scope.row.cur_value }}
                </el-text>
              </template>
            </el-table-column>
            <el-table-column label="当前电压(V)" align="center" prop="vol_value" v-if="controlVis.circleTableCol.vol_value" >
              <template #default="scope">
                <el-text line-clamp="2"  :style="{ backgroundColor: scope.row.volColor }">
                  {{ scope.row.vol_value }}
                </el-text>
              </template>
            </el-table-column>
            <el-table-column label="有功功率(kW)" align="center" prop="pow_value" v-if="controlVis.circleTableCol.pow_value" >
              <template #default="scope">
                <el-text line-clamp="2"  :style="{ backgroundColor: scope.row.powColor }">
                  {{ scope.row.pow_value }}
                </el-text>
              </template>
            </el-table-column>
            <el-table-column label="功率因数" align="center" prop="pow_value" v-if="controlVis.circleTableCol.power_factor" >
              <template #default="scope">
                <el-text line-clamp="2"  :style="{ backgroundColor: scope.row.powerColor }">
                  {{ scope.row.power_factor }}
                </el-text>
              </template>
            </el-table-column>
            <el-table-column label="电能消耗(kWh)" align="center" prop="ele_active" v-if="controlVis.circleTableCol.ele_active">
              <template #default="scope">
              {{ scope.row.ele_active }}
              </template>
            </el-table-column>
          </el-table>
        </ContentWrap>
      </el-collapse-item>
      <el-collapse-item  name="3" v-if="controlVis.haveOutPut" style="margin: 15px 10px 15px 10px; ">
        <template #title>
          <div style="width: 5%;font-size: 16px;">输出位</div>
        </template>
        <ContentWrap>
          <el-table  :data="output" :stripe="true" :show-overflow-tooltip="true">
            <el-table-column label="序号" align="center" prop="no" />
            <el-table-column label="名称" align="center" prop="name" />
            <el-table-column label="开关状态" align="center" prop="relay_state" v-if="controlVis.outPutTableCol.relay_state">
              <template #default="scope">
                <el-tag type="" v-if="scope.row.relay_state == 1">闭合</el-tag>
                <el-tag type="danger" v-if="scope.row.relay_state == 0" >断开</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="输出电流(A)" align="center" prop="cur_value"  v-if="controlVis.outPutTableCol.cur_value">
              <template #default="scope">
                <el-text line-clamp="2"  :style="{ backgroundColor: scope.row.curColor }">
                  {{ scope.row.cur_value }}
                </el-text>
              </template>
            </el-table-column>
            <el-table-column label="有功功率(kW)" align="center" prop="pow_value"  v-if="controlVis.outPutTableCol.pow_value">
              <template #default="scope">
                <el-text line-clamp="2"  :style="{ backgroundColor: scope.row.powColor }">
                  {{ scope.row.pow_value }}
                </el-text>
              </template>
            </el-table-column>
            <el-table-column label="功率因数" align="center" prop="pow_value" v-if="controlVis.outPutTableCol.power_factor" >
              <template #default="scope">
                <el-text line-clamp="2"  :style="{ backgroundColor: scope.row.powerColor }">
                  {{ scope.row.power_factor }}
                </el-text>
              </template>
            </el-table-column>
            <el-table-column label="电能消耗(kWh)" align="center" prop="ele_active"  v-if="controlVis.outPutTableCol.ele_active">
              <template #default="scope">
              {{ scope.row.ele_active }}
              </template>
            </el-table-column>
          </el-table>
        </ContentWrap>
      </el-collapse-item>
      <el-collapse-item name="2" v-if="controlVis.haveSensor">
        <template #title>
          <div style="width: 5%;font-size: 16px;">传感器</div>
        </template>
        <ContentWrap>
          <el-table  :data="sensorList" :stripe="true" :show-overflow-tooltip="true">
            <el-table-column label="传感器名称" align="center" prop="temName" />
            <el-table-column label="传感器状态" align="center" prop="tem_value" v-if="controlVis.envTableCol.tem_value">
              <template #default="scope">
                <el-text line-clamp="2" :style="{ backgroundColor: scope.row.temColor }">
                  {{ parseFloat(scope.row.tem_value).toFixed(1) }}°C
                </el-text>
              </template>
            </el-table-column>
            <el-table-column label="传感器名称" align="center" prop="humName" />
            <el-table-column label="传感器状态" align="center" prop="hum_value" v-if="controlVis.envTableCol.hum_value">
              <template #default="scope">
                <el-text line-clamp="2" :style="{ backgroundColor: scope.row.temColor }">
                  {{ parseFloat(scope.row.hum_value).toFixed(1) }}%
                </el-text>
              </template>
            </el-table-column>
          </el-table>
        </ContentWrap>
      </el-collapse-item>
    </el-collapse>
  </div>
  
 </div>
</template>

<script setup lang="ts">

// import download from '@/utils/download'
import { PDUDeviceApi } from '@/api/pdu/pdudevice'
import * as echarts from 'echarts'
import { formatter } from 'element-plus'
import { formatDate } from '@/utils/formatTime'
import { onMounted, onUpdated} from 'vue'
import { useRoute } from 'vue-router'
// import { object } from 'vue-types';

/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })

const instance = getCurrentInstance();
const switchValue = ref(2);
const switchValueF = ref(2);
const toggleValue = ref(2);
const toggleValueV = ref(2);
const toggleTime = ref('seventytwoHour');
const toggleTimeV = ref('seventytwoHour');
const message = useMessage() // 消息弹窗
const typeRadioShowCur = ref("最大")
const typeRadioShowVol = ref("最大")
const typeRadioShowPower = ref("最大")
const typeRadioShowFactor = ref("最大")


//折叠列表显示的项
const activeNames = ref(["1","2","3","4","5"])

//计时器
const flashListTimer = ref({
  tableDataTimer : null as any,
  chartTimer : null as any,
  abcChartTimer: null as any
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
    ele_active : false,
    power_factor : false,
  },
  outPutTableCol : {
    relay_state : false,
    cur_value : false, 
    pow_value : false,
    pf : false,
    ele_active : false,
    power_factor : false
  },
  envTableCol : {
    hum_value : false,
    tem_value : false
  },
  display: false,
})

//const location = ref(history?.state?.location || "/");
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
  factorList : [] as number[],
  reactiveList : [] as number[],
  dateTimes : [] as string[]
}) as any

const chartDataF = ref({
  apparentList : [] as number[],
  activeList : [] as number[],
  factorList : [] as number[],
  dateTimes : [] as string[]
}) as any

// const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  devKey: "",
  ipAddr: null,
  cascadeAddr:0,
  createTime: [],
  cascadeNum: undefined,
  id : 0,
  powGranularity: "seventytwoHour",
  powGranularityF: "seventytwoHour",
})

const queryFormRef = ref() // 搜索的表单
// const exportLoading = ref(false) // 导出的加载中

//数据
const totalData = ref({
  ele : 0,
  frequency : 0,
  pow : 0,
  powPercentage : 0,
  pf : 0,
  powApparent : 0,
  powReactive:0
})
const A = ref({
  vol_value : null,
  volColor : null,
  volTextColor : null,
  pow_value : null,
  powColor : null,
  powTextColor : null,
  cur_value : null,
  curColor : null,
  curTextColor : null,
  sphereColor : null,
  curPercemtage: null,
  pf : null
}) as any
const B = ref({
  vol_value : null,
  volColor : null,
  volTextColor : null,
  pow_value : null,
  powColor : null,
  powTextColor : null,
  cur_value : null,
  curColor : null,
  curTextColor : null,
  sphereColor : null,
  curPercemtage: null,
  pf : null
}) as any
const C = ref({
  vol_value : null,
  volColor : null,
  volTextColor : null,
  pow_value : null,
  powColor : null,
  powTextColor : null,
  cur_value : null,
  curColor : null,
  curTextColor : null,
  sphereColor : null,
  curPercemtage: null,
  pf : null
}) as any

const devKeyList = ref([])
const loadAll = async () => {
  //debugger
  var data = await PDUDeviceApi.devKeyList();
  var objectArray = data.map((str) => {
    return { value: str };
  });
  console.log(objectArray)
  return objectArray;
}

const querySearch = (queryString: string, cb: any) => {

  const results = queryString
    ? devKeyList.value.filter(createFilter(queryString))
    : devKeyList.value
  // call callback function to return suggestions
  cb(results)
}

const createFilter = (queryString: string) => {
  return (devKeyList) => {
    return (
      devKeyList.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0
    )
  }
}
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
  const url = 'https://' + devKey.split('-')[0] + '/index.html';
  window.open(url, '_blank');
}

let chart = null as echarts.ECharts | null; // 显式声明 rankChart 的类型
const chartContainer = ref<HTMLElement | null>(null);

let chartF = null as echarts.ECharts | null; // 显式声明 rankChart 的类型
const chartContainerF = ref<HTMLElement | null>(null);

let totalChart = null as echarts.ECharts | null; // 显式声明 rankChart 的类型
const totalChartContainer = ref<HTMLElement | null>(null);
let AChart = null as echarts.ECharts | null; // 显式声明 rankChart 的类型
const AChartContainer = ref<HTMLElement | null>(null);
let BChart = null as echarts.ECharts | null; // 显式声明 rankChart 的类型
const BChartContainer = ref<HTMLElement | null>(null);
let CChart = null as echarts.ECharts | null; // 显式声明 rankChart 的类型
const CChartContainer = ref<HTMLElement | null>(null);

let lineidChart = null as echarts.ECharts | null; // 显式声明 rankChart 的类型
const lineidChartContainer = ref<HTMLElement | null>(null);

let lineidChartV = null as echarts.ECharts | null; // 显式声明 rankChart 的类型
const lineidChartContainerV = ref<HTMLElement | null>(null);

const resultCur = ref()
const resultVol = ref()

function buKongGe(value,du){
  value=Number(value);
  console.log(value);
  if(value<100&&value>=10) return "&nbsp;&nbsp;&nbsp;"+value.toFixed(du);
  if(value<10) return "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"+value.toFixed(du);
  return "&nbsp;"+value.toFixed(du);
}

const initChart = async () => {
  let itemApparentType = 'apparentListMax'
  let itemActiveType = 'activeListMax'
  let itemFactorType = 'factorListMax'
  let itemReactiveType = 'reactiveListMax'


  var tempParams = { devKey : queryParams.devKey, type : queryParams.powGranularity}

  chartData.value = await PDUDeviceApi.PDUHis(tempParams); 
  chartDataF.value = chartData.value

  chartData.value?.apparentList?.forEach((obj,index) => {
    chartData.value.apparentList[index] = obj?.toFixed(3);
    chartData.value.apparentListMin[index] = chartData.value.apparentListMin[index]?.toFixed(3);
    chartData.value.apparentListMax[index] = chartData.value.apparentListMax[index]?.toFixed(3);
    chartData.value.apparentListMinTime[index] = chartData.value.apparentListMinTime[index];
    chartData.value.apparentListMaxTime[index] = chartData.value.apparentListMaxTime[index];

    chartData.value.activeList[index] = chartData.value.activeList[index]?.toFixed(3);
    chartData.value.activeListMin[index] = chartData.value.activeListMin[index]?.toFixed(3);
    chartData.value.activeListMax[index] = chartData.value.activeListMax[index]?.toFixed(3);
    chartData.value.activeListMinTime[index] = chartData.value.activeListMinTime[index];
    chartData.value.activeListMaxTime[index] = chartData.value.activeListMaxTime[index];

    chartData.value.reactiveList[index] = chartData.value.reactiveList[index]?.toFixed(3);
    chartData.value.reactiveListMin[index] = chartData.value.reactiveListMin[index]?.toFixed(3);
    chartData.value.reactiveListMax[index] = chartData.value.reactiveListMax[index]?.toFixed(3);
    chartData.value.reactiveListMinTime[index] = chartData.value.reactiveListMinTime[index];
    chartData.value.reactiveListMaxTime[index] = chartData.value.reactiveListMaxTime[index];

    chartData.value.factorList[index] = chartData.value.factorList[index]?.toFixed(2);
    chartData.value.factorListMin[index] = chartData.value.factorListMin[index]?.toFixed(2);
    chartData.value.factorListMax[index] = chartData.value.factorListMax[index]?.toFixed(2);
    chartData.value.factorListMinTime[index] = chartData.value.factorListMinTime[index];
    chartData.value.factorListMaxTime[index] = chartData.value.factorListMaxTime[index];

    if(chartData.value.size == 3) {
      chartData.value.factorLista[index] = chartData.value.factorLista[index]?.toFixed(2);
      chartData.value.factorListMina[index] = chartData.value.factorListMina[index]?.toFixed(2);
      chartData.value.factorListMaxa[index] = chartData.value.factorListMaxa[index]?.toFixed(2);
      chartData.value.factorListMinTimea[index] = chartData.value.factorListMinTimea[index];
      chartData.value.factorListMaxTimea[index] = chartData.value.factorListMaxTimea[index];
    
      chartData.value.factorListb[index] = chartData.value.factorListb[index]?.toFixed(2);
      chartData.value.factorListMinb[index] = chartData.value.factorListMinb[index]?.toFixed(2);
      chartData.value.factorListMaxb[index] = chartData.value.factorListMaxb[index]?.toFixed(2);
      chartData.value.factorListMinTimeb[index] = chartData.value.factorListMinTimeb[index];
      chartData.value.factorListMaxTimeb[index] = chartData.value.factorListMaxTimeb[index];

      chartData.value.factorListc[index] = chartData.value.factorListc[index]?.toFixed(2);
      chartData.value.factorListMinc[index] = chartData.value.factorListMinc[index]?.toFixed(2);
      chartData.value.factorListMaxc[index] = chartData.value.factorListMaxc[index]?.toFixed(2);
      chartData.value.factorListMinTimec[index] = chartData.value.factorListMinTimec[index];
      chartData.value.factorListMaxTimec[index] = chartData.value.factorListMaxTimec[index];
    }
    
  });

  if(queryParams.powGranularity === 'oneHour'){
    chartData.value.dateTimes = chartData.value?.dateTimes
    chartDataF.value.dateTimes = chartDataF.value?.dateTimes
  }else if(queryParams.powGranularity === 'twentyfourHour'){
    chartData.value.dateTimes = chartData.value?.dateTimes?.map(item => item.slice(5, item.length));
    chartDataF.value.dateTimes = chartDataF.value?.dateTimes?.map(item => item.slice(5, item.length));
  }else if(queryParams.powGranularity === 'seventytwoHour'){
    chartData.value.dateTimes = chartData.value?.dateTimes?.map(item => item.slice(0, 16));
    chartDataF.value.dateTimes = chartDataF.value?.dateTimes?.map(item => item.slice(0, 16));
  }

  let timeArr = [itemActiveType + 'Time',itemApparentType + 'Time',itemReactiveType + 'Time']

  if (chartContainer.value && instance) {
    chart = echarts.init(chartContainer.value);
    chart.setOption({
  toolbox: {
            show: true,//是否显示工具箱开关
            top: 'center',
            orient: 'vertical',
            feature: {
              // dataView: { show: true, readOnly: true },//数据视图打开并允许编辑
              // restore: { show: true },//重新加载视图
              saveAsImage: { show: true,name: "总功率趋势图" + formatDate(new Date) },//保存为图片
              // dataZoom :{},
            },
          },
      // 这里设置 Echarts 的配置项和数据
      title: { text: ''},
      tooltip: { trigger: 'axis' ,formatter: function(params) {
        let result = params[0].name + '<br>';
        for (var i = 0; i < params.length; i++) {
          result +=  params[i].marker + params[i].seriesName;
          result += ':' + buKongGe(params[i].value,3)
          //判断是否给鼠标悬停上显示符号
          if (params[i].seriesName === '总视在功率') {
            result += ' kVA&nbsp;'; 
          } else if (params[i].seriesName === '总有功功率') {
            result += ' kW&nbsp;&nbsp;';
          }else if (params[i].seriesName === '总无功功率') {
            result += ' kVar';
          }
          if(chartData.value[timeArr[i]].length) {
            result += '&nbsp;发生时间:' + chartData.value[timeArr[i]][params[i].dataIndex]
          }
          result += '<br>';
        }
        return result;
      }},
      //显示线的按钮
      legend: { data: ['总有功功率','总视在功率','总无功功率']},
      grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
      xAxis: {type: 'category', axisLabel: { formatter: 
            function (value) {
              if(toggleTime.value == "oneHour"){
                // 截取字符串的前n位，即yyyy-MM-dd HH:mm:ss
                return value.substring(11, 19);
              } else if(toggleTime.value == "twentyfourHour" ||toggleTime.value == "seventytwoHour"){
                // 截取字符串的n位，即yyyy-MM-dd HH:mm:ss
                return value.substring(5, 19);
              } 
            }
          },boundaryGap: false, data:chartData.value.dateTimes},
      yAxis: { type: 'value'},
      //鼠标悬停的显示
      series: [
          {name: '总有功功率', type: 'line', data: chartData.value[itemActiveType] , ...(chartData.value[itemActiveType]?.length==1? {symbol: 'circle'}:{symbol:'none'}), symbolSize: 4},
          {name: '总视在功率', type: 'line', data: chartData.value[itemApparentType] , ...(chartData.value[itemApparentType]?.length==1? {symbol: 'circle'}:{symbol:'none'}), symbolSize: 4},
          {name: '总无功功率', type: 'line', data: chartData.value[itemReactiveType] , ...(chartData.value[itemReactiveType]?.length==1? {symbol: 'circle'}:{symbol:'none'}), symbolSize: 4},
      ],
    },true);
    // 将 chart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    instance.appContext.config.globalProperties.chart = chart;
  }

  if (chartContainerF.value && instance) {
    let legendList
    let seriesList
    if(chartDataF.value.size == 3) {
      legendList = ['总功率因数','L1功率因数','L2功率因数','L3功率因数']
      seriesList = [
        {name: '总功率因数', type: 'line', data: chartDataF.value[itemFactorType] , ...(chartDataF.value[itemFactorType]?.length==1? {symbol: 'circle'}:{symbol:'none'}), symbolSize: 4},
        {name: 'L1功率因数', type: 'line', data: chartDataF.value[itemFactorType +'a'] , ...(chartDataF.value[itemFactorType+ 'a']?.length==1? {symbol: 'circle'}:{symbol:'none'}), symbolSize: 4},
        {name: 'L2功率因数', type: 'line', data: chartDataF.value[itemFactorType + 'b'] , ...(chartDataF.value[itemFactorType+ 'b']?.length==1? {symbol: 'circle'}:{symbol:'none'}), symbolSize: 4},
        {name: 'L3功率因数', type: 'line', data: chartDataF.value[itemFactorType + 'c'] , ...(chartDataF.value[itemFactorType+ 'c']?.length==1? {symbol: 'circle'}:{symbol:'none'}), symbolSize: 4},
      ]
    } else {
      legendList = ['总功率因数','L1功率因数','L2功率因数','L3功率因数']
      seriesList = [
        {name: '总功率因数', type: 'line', data: chartDataF.value[itemFactorType] , ...(chartDataF.value[itemFactorType]?.length==1? {symbol: 'circle'}:{symbol:'none'}), symbolSize: 4}
      ]
    }

    chartF = echarts.init(chartContainerF.value);
    chartF.setOption({
        // 这里设置 Echarts 的配置项和数据
        title: { text: ''},
        tooltip: { trigger: 'axis' ,formatter: function(params) {
          let result = params[0].name + '<br>';
          for (var i = 0; i < params.length; i++) {
            result +=  params[i].marker + params[i].seriesName;
            result += ':' + params[i].value
            if(itemFactorType != 'factorList' && chartDataF.value?.[`${itemFactorType}Time`].length) {
              result += '&nbsp&nbsp发生时间:' + chartDataF.value[`${itemFactorType}Time`][params[i].dataIndex]
            }
            result += '<br>';
          }
          return result;
        }},
        legend: { data: legendList},
        grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
        toolbox: {
            show: true,//是否显示工具箱开关
            top: 'center',
            orient: 'vertical',
            feature: {
              // dataView: { show: true, readOnly: true },//数据视图打开并允许编辑
              // restore: { show: true },//重新加载视图
              saveAsImage: { show: true,name: "功率因数趋势图" + formatDate(new Date) },//保存为图片
              // dataZoom :{},
            },
          },
        xAxis: {type: 'category', axisLabel: { formatter: 
              function (value) {
                if(queryParams.powGranularityF == "oneHour"){
                  // 截取字符串的前n位，即yyyy-MM-dd HH:mm:ss
                  return value.substring(11, 19);
                } else if(queryParams.powGranularityF == "twentyfourHour" || queryParams.powGranularityF == "seventytwoHour"){
                  // 截取字符串的n位，即yyyy-MM-dd HH:mm:ss
                  return value.substring(5, 19);
                }
              }
            },boundaryGap: false, data:chartDataF.value.dateTimes},
        yAxis: { type: 'value'},
        series: seriesList,
      },true);
    // 将 chart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    instance.appContext.config.globalProperties.chartF = chartF;
  }

  if (totalChartContainer.value && instance) {
    totalChart = echarts.init(totalChartContainer.value);
    totalChart.setOption({
      // 这里设置 Echarts 的配置项和数据
      title: { text: ''},
      legend: {
              orient: 'vertical',
              right: 10,
              top: 'center',
              data: totalData.value.pow+totalData.value.powReactive,
            },
            graphic: {
                elements: [
                    {
                        type: 'text',
                        left: 'center',
                        top: 'center',
                        style: {
                            text: (Number(totalData.value.pow)+Number(totalData.value.powReactive)).toFixed(3)+"kVA",
                            fontSize: 13,
                            // fontWeight: 'bold',
                            fill: '#000'
                        }
                    }
                ]
            },
      tooltip: { trigger: 'item', formatter: function(params) {
                                      if (params.name === '无功功率') {
                                          return params.name + ': ' + params.value + 'kVar';
                                      } else if (params.name === '有功功率') {
                                          return params.name + ': ' + params.value + 'kW';
                                      }
                                  } },
      grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
      series: [
        { type: 'pie', radius: ['50%', '65%'], avoidLabelOverlap: false,  labelLine: { show: false },
          data: [{value : totalData.value.pow, name: '有功功率', label: { show: true, position: 'outside', formatter: '{c}kW',fontSize: 13 },itemStyle: { color: '#91cc75' }  },
                 {value : totalData.value.powReactive , name : '无功功率' , label: { show: true, position: 'outside', formatter: '{c}kVar',fontSize: 13  }, itemStyle: { color: '#fac858' } },],
        },
      ],
    });
    // 将 totalChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    instance.appContext.config.globalProperties.totalChart = totalChart;
  }
  if (AChartContainer.value && instance) {
    AChart = echarts.init(AChartContainer.value);
    var aCurMax =  A.value.cur_alarm_max - A.value.cur_value;
    AChart.setOption({
      // 这里设置 Echarts 的配置项和数据
      title: { text: ''},
      // tooltip: { trigger: 'item', formatter: '{b}: {c}A' },
      grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
      series: [
        { type: 'pie', radius: ['50%', '65%'], avoidLabelOverlap: false,  labelLine: { show: false } , emphasis:{disabled:false,scale:false,scaleSize:0,},
          data: [
            {value : A.value.cur_value, name: '电流', label: { show: true, position: 'center', formatter: '{c}A',fontSize: 13 ,backgroundColor : A.value.curColor,color:A.value.curTextColor },itemStyle: { color: A.value.sphereColor } },
            {value : aCurMax, itemStyle: { color: '#CCCCCC',shadowBlur:0 } },
          ],
        },
      ],
    });
    // 将 AChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    instance.appContext.config.globalProperties.AChart = AChart;
  }
  if (BChartContainer.value && instance) {
    BChart = echarts.init(BChartContainer.value);
    var bCurMax =  B.value.cur_alarm_max - B.value.cur_value;
    BChart.setOption({
      // 这里设置 Echarts 的配置项和数据
      title: { text: ''},
      // tooltip: { trigger: 'item', formatter: '{b}: {c}A' },
      grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
      series: [
        { type: 'pie', radius: ['50%', '65%'], avoidLabelOverlap: false,  labelLine: { show: false },emphasis:{disabled:false,scale:false,scaleSize:0,},
          data: [
            {value : B.value.cur_value, name: '电流', label: { show: true, position: 'center', formatter: '{c}A',fontSize: 13,backgroundColor : B.value.curColor,color:B.value.curTextColor  },itemStyle: { color: B.value.sphereColor }  },
            {value : bCurMax,itemStyle: { color: '#CCCCCC',shadowBlur:0 } },
          ],
        },
      ],
    });
    // 将 BChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    instance.appContext.config.globalProperties.BChart = BChart;
  }
  if (CChartContainer.value && instance) {
    CChart = echarts.init(CChartContainer.value);
    var cCurMax =  C.value.cur_alarm_max - C.value.cur_value;
    CChart.setOption({
      // 这里设置 Echarts 的配置项和数据
      title: { text: ''},
      // tooltip: { trigger: 'item', formatter: '{b}: {c}A' },
      grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
      series: [
        { type: 'pie', radius: ['50%', '65%'], avoidLabelOverlap: false,  labelLine: { show: false },emphasis:{disabled:false,scale:false,scaleSize:0,},
          data: [
            {value : C.value.cur_value, name: '电流', label: { show: true, position: 'center', formatter: '{c}A',fontSize: 13,  backgroundColor : C.value.curColor,color:C.value.curTextColor },itemStyle: { color: C.value.sphereColor }  },
            {value : cCurMax , itemStyle: { color: '#CCCCCC',shadowBlur:0 } },
          ],
        },
      ],
    });
    // 将 CChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    instance.appContext.config.globalProperties.CChart = CChart;
  }

};

// 在组件销毁时手动销毁图表
const beforeChartUnmount = () => {
  chart?.dispose(); // 销毁图表实例
  chartF?.dispose(); // 销毁图表实例
};

// window.addEventListener('resize', function() {
//   chart?.resize(); 
// });

const setNewChartData = async () => {
  var params = { devKey : queryParams.devKey , oldTime : chartData.value.dateTimes[chartData.value.dateTimes.length - 1], type : queryParams.powGranularity}
  var temp =  {dateTime : '' , apparent : 0, active: 0 ,reactive : 0, factor : 0};
  temp =  await PDUDeviceApi.ChartNewData(params);

  chartData.value.apparentList.shift()
  chartData.value.activeList.shift()
  chartData.value.dateTimes.shift()
  chartData.value.reactiveList.shift()
  chartData.value.factorList.shift()

  chartData.value.dateTimes.push(temp.dateTime);
  chartData.value.apparentList.push(temp.apparent?.toFixed(3));
  chartData.value.activeList.push(temp.active?.toFixed(3));
  chartData.value.reactiveList.push(temp.reactive?.toFixed(3));
  chartData.value.factorList.push(temp.factor?.toFixed(2));

  if(queryParams.powGranularity === 'oneHour'){
    chartData.value.dateTimes = chartData.value.dateTimes
  }else if(queryParams.powGranularity === 'twentyfourHour'){
    chartData.value.dateTimes = chartData.value.dateTimes.map(item => item.slice(5, item.length));
  }else if(queryParams.powGranularity === 'seventytwoHour'){
    chartData.value.dateTimes = chartData.value.dateTimes.map(item => item.slice(0, 16));
  }

  chart?.setOption({
    xAxis: { data: chartData.value.dateTimes },
    series: [
      { data: chartData.value.apparentList },
      { data: chartData.value.activeList},
      { data: chartData.value.reactiveList},
    ],
  });

  chartF?.setOption({
    xAxis: { data: chartData.value.dateTimes },
    series: [
      { data: chartData.value.factorList },
    ],
  });

  await PDUHdaLineHisdata(toggleTime.value)
  await PDUHdaLineHisdataV(toggleTimeV.value)
  lineidFlashChartData()
}

const setNewABCChartData = () => {
  if (totalChartContainer.value && instance) {
    totalChart = echarts.init(totalChartContainer.value);
    totalChart.setOption({
      // 这里设置 Echarts 的配置项和数据
      title: { text: ''},
      graphic: {
                elements: [
                    {
                        type: 'text',
                        left: 'center',
                        top: 'center',
                        style: {
                            text: (Number(totalData.value.pow)+Number(totalData.value.powReactive)).toFixed(3)+"kVA",
                            fontSize: 13,
                            // fontWeight: 'bold',
                            fill: '#000'
                        }
                    }
                ]
            },
      tooltip: { trigger: 'item', formatter: function(params) {
                                      if (params.name === '无功功率') {
                                          return params.name + ': ' + params.value + 'kVar';
                                      } else if (params.name === '有功功率') {
                                          return params.name + ': ' + params.value + 'kW';
                                      }
                                  } },
      grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
      series: [
        { type: 'pie', radius: ['50%', '65%'], avoidLabelOverlap: false,  labelLine: { show: false },
          data: [{value : totalData.value.pow, name: '有功功率', label: { show: true, position: 'outside', formatter: '{c}kW',fontSize: 13 },itemStyle: { color: '#91cc75' }  },
                 {value : totalData.value.powReactive , name : '无功功率' , label: { show: true, position: 'outside', formatter: '{c}kVar',fontSize: 13  }, itemStyle: { color: '#fac858' } }],
        },
      ],
    });
    // 将 totalChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    instance.appContext.config.globalProperties.totalChart = totalChart;
  }

  if (AChartContainer.value && instance) {
    AChart = echarts.init(AChartContainer.value);
    var aCurMax =  A.value.cur_alarm_max - A.value.cur_value;
    AChart.setOption({
      // 这里设置 Echarts 的配置项和数据
      title: { text: ''},
      // tooltip: { trigger: 'item', formatter: '{b}: {c}A' },
      grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
      series: [
        { type: 'pie', radius: ['50%', '65%'], avoidLabelOverlap: false,  labelLine: { show: false } , emphasis:{disabled:false,scale:false,scaleSize:0,},
          data: [
            {value : A.value.cur_value, name: '电流', label: { show: true, position: 'center', formatter: '{c}A',fontSize: 13 ,backgroundColor : A.value.curColor,color:A.value.curTextColor },itemStyle: { color: A.value.sphereColor } },
            {value : aCurMax, itemStyle: { color: '#CCCCCC',shadowBlur:0 } },
          ],
        },
      ],
    });
    // 将 AChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    instance.appContext.config.globalProperties.AChart = AChart;
  }
  if (BChartContainer.value && instance) {
    BChart = echarts.init(BChartContainer.value);
    var bCurMax =  B.value.cur_alarm_max - B.value.cur_value;
    BChart.setOption({
      // 这里设置 Echarts 的配置项和数据
      title: { text: ''},
      // tooltip: { trigger: 'item', formatter: '{b}: {c}A' },
      grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
      series: [
        { type: 'pie', radius: ['50%', '65%'], avoidLabelOverlap: false,  labelLine: { show: false },emphasis:{disabled:false,scale:false,scaleSize:0,},
          data: [
            {value : B.value.cur_value, name: '电流', label: { show: true, position: 'center', formatter: '{c}A',fontSize: 13,backgroundColor : B.value.curColor,color:B.value.curTextColor  },itemStyle: { color: B.value.sphereColor }  },
            {value : bCurMax,itemStyle: { color: '#CCCCCC',shadowBlur:0 } },
          ],
        },
      ],
    });
    // 将 BChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    instance.appContext.config.globalProperties.BChart = BChart;
  }
  if (CChartContainer.value && instance) {
    CChart = echarts.init(CChartContainer.value);
    var cCurMax =  C.value.cur_alarm_max - C.value.cur_value;
    CChart.setOption({
      // 这里设置 Echarts 的配置项和数据
      title: { text: ''},
      // tooltip: { trigger: 'item', formatter: '{b}: {c}A' },
      grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
      series: [
        { type: 'pie', radius: ['50%', '65%'], avoidLabelOverlap: false,  labelLine: { show: false },emphasis:{disabled:false,scale:false,scaleSize:0,},
          data: [
            {value : C.value.cur_value, name: '电流', label: { show: true, position: 'center', formatter: '{c}A',fontSize: 13, backgroundColor : C.value.curColor,color:C.value.curTextColor  },itemStyle: { color: C.value.sphereColor }  },
            {value : cCurMax , itemStyle: { color: '#CCCCCC',shadowBlur:0 } },
          ],
        },
      ],
    });
    // 将 CChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    instance.appContext.config.globalProperties.CChart = CChart;
  }
}

const flashChartData = async () =>{

  beforeChartUnmount()

  let itemApparentType = typeRadioShowPower.value == "最小" ? 'apparentListMin' : (typeRadioShowPower.value == "最大" ? 'apparentListMax' : 'apparentList')
  let itemActiveType = typeRadioShowPower.value == "最小" ? 'activeListMin' : (typeRadioShowPower.value == "最大" ? 'activeListMax' : 'activeList')
  let itemFactorType = typeRadioShowFactor.value == "最小" ? 'factorListMin' : (typeRadioShowFactor.value == "最大" ? 'factorListMax' : 'factorList')
  let itemReactiveType = typeRadioShowPower.value == "最小" ? 'reactiveListMin' : (typeRadioShowPower.value == "最大" ? 'reactiveListMax' : 'reactiveList')

  var tempParams = { devKey : queryParams.devKey, type : queryParams.powGranularity}
  chartData.value = await PDUDeviceApi.PDUHis(tempParams);
  chartData.value.apparentList.forEach((obj,index) => {
    chartData.value[`${itemApparentType}`][index] = obj?.toFixed(3);
    chartData.value[`${itemActiveType}`][index] = chartData.value[`${itemActiveType}`][index]?.toFixed(3);
    chartData.value[`${itemFactorType}`][index] = chartData.value[`${itemFactorType}`][index]?.toFixed(2);
    chartData.value[`${itemReactiveType}`][index] = chartData.value[`${itemReactiveType}`][index]?.toFixed(3);
    
    chartData.value[`${itemApparentType}Time`][index] = chartData.value[`${itemApparentType}Time`][index];
    chartData.value[`${itemActiveType}Time`][index] = chartData.value[`${itemActiveType}Time`][index];
    chartData.value[`${itemFactorType}Time`][index] = chartData.value[`${itemFactorType}Time`][index];
    chartData.value[`${itemReactiveType}Time`][index] = chartData.value[`${itemReactiveType}Time`][index];
  });

  if(queryParams.powGranularity === 'oneHour'){
    chartData.value.dateTimes = chartData.value.dateTimes
  }else if(queryParams.powGranularity === 'twentyfourHour'){
    chartData.value.dateTimes = chartData.value.dateTimes.map(item => item.slice(5, item.length));
  }else if(queryParams.powGranularity === 'seventytwoHour'){
    chartData.value.dateTimes = chartData.value.dateTimes.map(item => item.slice(0, 16));
  }
  
  let timeArr = [itemActiveType + 'Time',itemApparentType + 'Time',itemReactiveType + 'Time']

  // 创建新的图表实例
  chart = echarts.init(document.getElementById('chartContainer'));
  // 设置新的配置对象
  if (chart) {
    chart.setOption({
  toolbox: {
            show: true,//是否显示工具箱开关
            top: 'center',
            orient: 'vertical',
            feature: {
              // dataView: { show: true, readOnly: true },//数据视图打开并允许编辑
              // restore: { show: true },//重新加载视图
              saveAsImage: { show: true,name: "总功率趋势图" + formatDate(new Date) },//保存为图片
              // dataZoom :{},
            },
          },
      // 这里设置 Echarts 的配置项和数据
      title: { text: ''},
      tooltip: { trigger: 'axis' ,formatter: function(params) {
        let result = params[0].name + '<br>';
        for (var i = 0; i < params.length; i++) {
          // result +=  params[i].marker + params[i].seriesName;
          // if(itemApparentType != 'apparentList' && chartData.value[timeArr[i]].length) {
          //   result += '&nbsp&nbsp发生时间:' + chartData.value[timeArr[i]][params[i].dataIndex]
          // }
          // result += '&nbsp&nbsp' + params[i].value
          // if (params[i].seriesName === '总视在功率') {
          //   result += ' kVA'; 
          // } else if (params[i].seriesName === '总有功功率') {
          //   result += ' kW';
          // }else if (params[i].seriesName === '总无功功率') {
          //   result += ' kVar';
          // }
          // result += '<br>';
          result +=  params[i].marker + params[i].seriesName;
          result += ':' + buKongGe(params[i].value,3)
          //判断是否给鼠标悬停上显示符号
          if (params[i].seriesName === '总视在功率') {
            result += ' kVA&nbsp;'; 
          } else if (params[i].seriesName === '总有功功率') {
            result += ' kW&nbsp;&nbsp;';
          }else if (params[i].seriesName === '总无功功率') {
            result += ' kVar';
          }
          if(itemApparentType != 'apparentList' && chartData.value[timeArr[i]].length) {
            result += '&nbsp;发生时间:' + chartData.value[timeArr[i]][params[i].dataIndex]
          }
        }
        return result;
      }},
      legend: { data: ['总有功功率','总视在功率','总无功功率']},
      grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
      xAxis: {type: 'category', axisLabel: { formatter: 
            function (value) {
              if(queryParams.powGranularity == "oneHour"){
                // 截取字符串的前n位，即yyyy-MM-dd HH:mm:ss
                return value.substring(11, 19);
              } else if(queryParams.powGranularity == "twentyfourHour" || queryParams.powGranularity == "seventytwoHour"){
                // 截取字符串的n位，即yyyy-MM-dd HH:mm:ss
                return value.substring(5, 19);
              }
            }
          },boundaryGap: false, data:chartData.value.dateTimes},
      yAxis: { type: 'value'},
      series: [
          {name: '总有功功率', type: 'line', data: chartData.value.activeList , ...(chartData.value?.activeList?.length==1? {symbol: 'circle'}:{symbol:'none'}), symbolSize: 4},
          {name: '总视在功率', type: 'line', data: chartData.value.apparentList , ...(chartData.value?.apparentList?.length==1? {symbol: 'circle'}:{symbol:'none'}), symbolSize: 4},
          {name: '总无功功率', type: 'line', data: chartData.value.reactiveList , ...(chartData.value?.reactiveList?.length==1? {symbol: 'circle'}:{symbol:'none'}), symbolSize: 4},
      ],
    },true);
  }

  var tempParams = { devKey : queryParams.devKey, type : queryParams.powGranularityF}
  chartDataF.value = await PDUDeviceApi.PDUHis(tempParams);
  chartDataF.value.apparentList.forEach((obj,index) => {
    chartDataF.value.apparentList[index] = obj?.toFixed(3);
  });
  chartDataF.value.activeList.forEach((obj,index) => {
    chartDataF.value.activeList[index] = obj?.toFixed(3);
  });
  chartDataF.value.factorList.forEach((obj,index) => {
    chartDataF.value.factorList[index] = obj?.toFixed(2);
  });

  if(queryParams.powGranularityF === 'oneHour'){
    chartDataF.value.dateTimes = chartDataF.value.dateTimes
  }else if(queryParams.powGranularityF === 'twentyfourHour'){
    chartDataF.value.dateTimes = chartDataF.value.dateTimes.map(item => item.slice(5, item.length));
  }else if(queryParams.powGranularityF === 'seventytwoHour'){
    chartDataF.value.dateTimes = chartDataF.value.dateTimes.map(item => item.slice(0, 16));
  }
  
  // 创建新的图表实例
  chartF = echarts.init(document.getElementById('chartContainerF'));
  // 设置新的配置对象
  if (chartF) {
    let legendList
    let seriesList
    if(chartDataF.value.size == 3) {
      legendList = ['总功率因数','L1功率因数','L2功率因数','L3功率因数']
      seriesList = [
        {name: '总功率因数', type: 'line', data: chartDataF.value[itemFactorType] , ...(chartDataF.value[itemFactorType]?.length==1? {symbol: 'circle'}:{symbol:'none'}), symbolSize: 4},
        {name: 'L1功率因数', type: 'line', data: chartDataF.value[itemFactorType +'a'] , ...(chartDataF.value[itemFactorType + 'a']?.length==1? {symbol: 'circle'}:{symbol:'none'}), symbolSize: 4},
        {name: 'L2功率因数', type: 'line', data: chartDataF.value[itemFactorType + 'b'] , ...(chartDataF.value[itemFactorType + 'b']?.length==1? {symbol: 'circle'}:{symbol:'none'}), symbolSize: 4},
        {name: 'L3功率因数', type: 'line', data: chartDataF.value[itemFactorType + 'c'] , ...(chartDataF.value[itemFactorType + 'c']?.length==1? {symbol: 'circle'}:{symbol:'none'}), symbolSize: 4},
      ]
    } else {
      legendList = ['总功率因数','L1功率因数','L2功率因数','L3功率因数']
      seriesList = [
        {name: '总功率因数', type: 'line', data: chartDataF.value[itemFactorType] , ...(chartDataF.value[itemFactorType]?.length==1? {symbol: 'circle'}:{symbol:'none'}), symbolSize: 4}
      ]
    }

    chartF.setOption({
        // 这里设置 Echarts 的配置项和数据
        title: { text: ''},
        tooltip: { trigger: 'axis' ,formatter: function(params) {
          let result = params[0].name + '<br>';
          for (var i = 0; i < params.length; i++) {
            result +=  params[i].marker + params[i].seriesName;
            result += ':' + params[i].value
            if(itemFactorType != 'factorList' && chartDataF.value?.[`${itemFactorType}Time`].length) {
              result += '&nbsp&nbsp发生时间:' + chartDataF.value[`${itemFactorType}Time`][params[i].dataIndex]
            }
            result += '<br>';
          }
          return result;
        }},
        legend: { data: legendList},
        grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
        toolbox: {
            show: true,//是否显示工具箱开关
            top: 'center',
            orient: 'vertical',
            feature: {
              // dataView: { show: true, readOnly: true },//数据视图打开并允许编辑
              // restore: { show: true },//重新加载视图
              saveAsImage: { show: true,name: "功率因数趋势图" + formatDate(new Date) },//保存为图片
              // dataZoom :{},
            },
          },
        xAxis: {type: 'category', axisLabel: { formatter: 
              function (value) {
                if(queryParams.powGranularityF == "oneHour"){
                  // 截取字符串的前n位，即yyyy-MM-dd HH:mm:ss
                  return value.substring(11, 19);
                } else if(queryParams.powGranularityF == "twentyfourHour" || queryParams.powGranularityF == "seventytwoHour"){
                  // 截取字符串的n位，即yyyy-MM-dd HH:mm:ss
                  return value.substring(5, 19);
                }
              }
            },boundaryGap: false, data:chartDataF.value.dateTimes},
        yAxis: { type: 'value'},
        series: seriesList,
      },true);
  }



//总数据的饼图
  totalChart?.setOption({
    series: [
        { 
          data: [{value : totalData.value.pow, name: '有功功率', label: { show: true, position: 'outside', formatter: '{c}kW',fontSize: 13 },itemStyle: { color: '#91cc75' }  },
                 {value : totalData.value.powApparent , name : '无功功率' , label: { show: true, position: 'outside', formatter: '{c}kVar',fontSize: 13  }, itemStyle: { color: '#fac858' } }],
        },
    ],
  });

  var aCurMax = A.value.cur_alarm_max - A.value.cur_value;
  AChart?.setOption({
    series: [
        { 
          data: [
            {value : A.value.cur_value, name: '电流', label: { show: true, position: 'center', formatter: '{c}A',fontSize: 13 ,backgroundColor : A.value.curColor },itemStyle: { color: '#0AD0EE' }},
            {value : aCurMax , itemStyle: { color: '#CCCCCC' } },],
        },
      ],
  });

  var bCurMax =  B.value.cur_alarm_max - B.value.cur_value;
  BChart?.setOption({
    series: [
        { 
          data: [
            {value : B.value.cur_value, name: '电流', label: { show: true, position: 'center', formatter: '{c}A',fontSize: 13 ,backgroundColor : A.value.curColor },itemStyle: { color: '#0AD0EE' }  },
            {value : bCurMax,itemStyle: { color: '#CCCCCC',shadowBlur:0 } },
          ],
        },
      ],
  });

  var cCurMax =  C.value.cur_alarm_max - C.value.cur_value;
  CChart?.setOption({
    series: [
        { 
          data: [
            {value : C.value.cur_value, name: '电流', label: { show: true, position: 'center', formatter: '{c}A',fontSize: 13 ,backgroundColor : A.value.curColor },itemStyle: { color: '#0AD0EE' }},
            {value : cCurMax,itemStyle: { color: '#CCCCCC',shadowBlur:0 } },
          ],
        },
      ],
  });
}

//在浏览器窗口大小发生变化时触发，暂时还没调试好，存在一个bug当页面缩放到某一个分辨率的时候会不执行css设定的样式
const updateDimensions = () => {
  flashChartData()
  lineidFlashChartData()
}

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
  const data = await PDUDeviceApi.PDUDisplay(queryParams);
  console.log("data",data);
  // console.log('data',data)
  testData.value = JSON.parse(data)
  circleList.value = [];
  output.value = [];

  if(testData.value?.pdu_data?.loop_item_list?.pow_apparent != null && testData.value?.pdu_data?.loop_item_list?.pow_apparent.length > 0){
    var temp = [] as any;
    for (let i = 0; i < testData.value.pdu_data?.loop_item_list["pow_apparent"].length; i++) {
      let loopItem = {} as any;
      for (let key in testData.value.pdu_data.loop_item_list) {
        loopItem[key] = testData.value.pdu_data.loop_item_list[key][i];
        loopItem["circuit"] = "C" + (i + 1); 
        controlVis.value.circleTableCol[key] = true;
        if (key.includes("alarm_status")) {
          var alarmStatus = testData.value.pdu_data.loop_item_list[key][i];
          if(alarmStatus == 1 ||alarmStatus == 8){
            var alarmColor = key.split("_")[0] + "Color";
            loopItem[alarmColor] = "red";
          } else if(alarmStatus == 2 ||alarmStatus == 4) {
            var alarmColor = key.split("_")[0] + "Color";
            loopItem[alarmColor] = "yellow";
          }else{
            var alarmColor = key.split("_")[0] + "Color";
            loopItem[alarmColor] = "";
          }
        }
      }
      temp.push(loopItem);
    }
    console.log('temp',temp)
    circleList.value = temp;
    controlVis.value.haveCircle = true;
  }else{
    controlVis.value.haveCircle = false;
  }

  circleList.value.forEach(element => {
    element.cur_value = element.cur_value?.toFixed(2);
    element.vol_value = element.vol_value?.toFixed(1);
    element.pow_value = element.pow_value?.toFixed(3);
    element.ele_active = element.ele_active?.toFixed(1);
    element.power_factor = element.power_factor?.toFixed(2);
  });

  
  if(testData.value?.pdu_data?.output_item_list?.name != null && testData.value?.pdu_data?.output_item_list?.name.length > 0){
    var temp = [] as any;
    for (let i = 0; i < testData.value.pdu_data.output_item_list["name"].length; i++) {
      let loopItem = {} as any;
      for (let key in testData.value.pdu_data.output_item_list) {
        loopItem[key] = testData.value.pdu_data.output_item_list[key][i];
        loopItem["no"] = i + 1;
        controlVis.value.outPutTableCol[key] = true;
        if (key.includes("alarm_status")) {
          var alarmStatus = testData.value.pdu_data.output_item_list[key][i];
          if(alarmStatus == 1 ||alarmStatus == 8){
            var alarmColor = key.split("_")[0] + "Color";
            loopItem[alarmColor] = "red";
          } else if(alarmStatus == 2||alarmStatus == 4) {
            var alarmColor = key.split("_")[0] + "Color";
            loopItem[alarmColor] = "yellow";
          }else {
            var alarmColor = key.split("_")[0] + "Color";
            loopItem[alarmColor] = "";
          }
        }
      }
      temp.push(loopItem);
    }
    output.value = temp;
    controlVis.value.haveOutPut = true;
  } else {
    controlVis.value.haveOutPut = false;
  }

  output.value.forEach(element => {
    element.cur_value = element.cur_value?.toFixed(2);
    element.pow_value = element.pow_value?.toFixed(3);
    element.pf = element.pf?.toFixed(2);
    element.ele_active = element.ele_active?.toFixed(1);
    element.power_factor = element.power_factor?.toFixed(2);
  });

  if(testData.value?.pdu_data?.env_item_list?.tem_value){
    var temp = [] as any;
    for(let i = 0; i < testData.value.pdu_data.env_item_list["tem_value"].length; i++){
      if(testData.value.pdu_data.env_item_list.insert[i] != 1){
        continue;
      }
      let loopItem = {} as any;
      for (let key in testData.value.pdu_data.env_item_list) {
        loopItem[key] = testData.value.pdu_data.env_item_list[key][i];
        controlVis.value.envTableCol[key] = true;
        if (key.includes("alarm_status")) {
          var alarmStatus = testData.value.pdu_data.env_item_list[key][i];
          if(alarmStatus == 1 ||alarmStatus == 8){
            var alarmColor = key.split("_")[0] + "Color";
            loopItem[alarmColor] = "red";
          } else if(alarmStatus == 2 ||alarmStatus == 4){
            var alarmColor = key.split("_")[0] + "Color";
            loopItem[alarmColor] = "yellow";
          }else{
            var alarmColor = key.split("_")[0] + "Color";
            loopItem[alarmColor] = "";
          }
        }
      }
      loopItem["temName"] = "温度" + (i + 1);
      loopItem["humName"] = "湿度" + (i + 1);
      temp.push(loopItem);
    }
    sensorList.value = temp;
    controlVis.value.haveSensor = true;
  } else {
    controlVis.value.haveSensor = false;
  }


  if(testData.value?.pdu_data?.pdu_total_data?.pow_active == null){
    message.error("设备离线或者输入的地址不正确");
    return;
  }

  //开始无判断
  if(testData.value.pdu_data?.pdu_total_data){
  totalData.value.pow =  testData.value.pdu_data.pdu_total_data.pow_active?.toFixed(3);

  totalData.value.ele = testData.value.pdu_data.pdu_total_data.ele_active?.toFixed(1);

  totalData.value.pf = testData.value.pdu_data.pdu_total_data.power_factor?.toFixed(2);
  totalData.value.frequency = testData.value.dev_hz;
  totalData.value.powApparent = testData.value.pdu_data.pdu_total_data.pow_apparent?.toFixed(3);
  totalData.value.powReactive=testData.value.pdu_data.pdu_total_data.pow_reactive?.toFixed(3);
  }
  if (testData.value.pdu_data?.line_item_list){
  A.value.cur_value = testData.value.pdu_data.line_item_list.cur_value[0]?.toFixed(2);
  A.value.curPercemtage = (testData.value.pdu_data.line_item_list.cur_value[0] / testData.value.pdu_data.line_item_list.cur_alarm_max[0]) * 100;
  A.value.cur_alarm_max = testData.value.pdu_data.line_item_list.cur_alarm_max[0];
  if(testData.value.pdu_data.line_item_list.cur_alarm_status){
    let curalarm = testData.value.pdu_data.line_item_list.cur_alarm_status[0];
    if(curalarm == 1 || curalarm == 8 ){
      A.value.curColor = "red";
      A.value.sphereColor ='red';
      A.value.curTextColor = "white";
    } else if(curalarm == 2 || curalarm == 4 ){
      A.value.curColor = "yellow";
      A.value.sphereColor ='yellow';
      A.value.curTextColor = "white";
    } else{
      A.value.curColor = "";
      A.value.curTextColor = "black";
      A.value.sphereColor ='#0AD0EE';
    }
  }

  A.value.vol_value = testData.value.pdu_data.line_item_list.vol_value[0]?.toFixed(1);
  if(testData.value.pdu_data.line_item_list.vol_alarm_status){
    let u1alarm = testData.value.pdu_data.line_item_list.vol_alarm_status[0];
    if(u1alarm == 1 || u1alarm == 8 ){
      A.value.volColor = "red";
      A.value.volTextColor="white";
    } else if(u1alarm == 2 || u1alarm == 4 ){
      A.value.volColor = "yellow";
      A.value.volTextColor="white";
    } else{
      A.value.volColor = "";
      A.value.volTextColor="";
    }    
  }

  A.value.pow_value =testData.value.pdu_data.line_item_list.pow_value[0]?.toFixed(3);
  if(testData.value.pdu_data.line_item_list.pow_alarm_status){
    let powalarm = testData.value.pdu_data.line_item_list.pow_alarm_status[0];
    if(powalarm == 1 || powalarm == 8 ){
      A.value.powColor = "red";
      A.value.powTextColor="white";
    } else if(powalarm == 2 || powalarm == 4 ){
      A.value.powColor = "yellow";
      A.value.powTextColor="white";
    } else {
      A.value.powColor = "";
      A.value.powTextColor="";
    }    
  }
  A.value.pf = testData.value.pdu_data.line_item_list.power_factor[0]?.toFixed(2);

  if(testData.value.pdu_data.line_item_list.ele_active.length > 1){
    B.value.cur_value = testData.value.pdu_data.line_item_list.cur_value[1]?.toFixed(2);
    B.value.curPercemtage = (testData.value.pdu_data.line_item_list.cur_value[1] / testData.value.pdu_data.line_item_list.cur_alarm_max[1]) * 100;
    B.value.cur_alarm_max = testData.value.pdu_data.line_item_list.cur_alarm_max[1];
    if(testData.value.pdu_data.line_item_list.cur_alarm_status){
      let curalarm = testData.value.pdu_data.line_item_list.cur_alarm_status[1];
      if(curalarm == 1 || curalarm == 8 ){
        B.value.curColor = "red";
        B.value.sphereColor ='red';
        B.value.curTextColor="white";
      } else if(curalarm == 2 || curalarm == 4 ){
        B.value.curColor = "yellow";
        B.value.sphereColor ='yellow';
        B.value.curTextColor="white";
      } else{
        B.value.curColor = "";
        B.value.curTextColor = "black";
        B.value.sphereColor ='#0AD0EE';
      }      
    }

    B.value.vol_value = testData.value.pdu_data.line_item_list.vol_value[1]?.toFixed(1);
    if(testData.value.pdu_data.line_item_list.vol_alarm_status){
      let u2alarm = testData.value.pdu_data.line_item_list.vol_alarm_status[1];
      if(u2alarm == 1 || u2alarm == 8 ){
        B.value.volColor = "red";
        B.value.volTextColor="white";
      } else if(u2alarm == 2 || u2alarm == 4 ){
        B.value.volColor = "yellow";
        B.value.volTextColor="white";
      } else {
        B.value.volColor = "";
        B.value.volTextColor="";
      }      
    }
    
    B.value.pow_value =testData.value.pdu_data.line_item_list.pow_value[1]?.toFixed(3);
  if(testData.value.pdu_data.line_item_list.pow_alarm_status){    
    let powalarm = testData.value.pdu_data.line_item_list.pow_alarm_status[1];
    if(powalarm == 1 || powalarm == 8 ){
      B.value.powColor = "red";
      B.value.powTextColor="white";
    } else if(powalarm == 2 || powalarm == 4 ){
      B.value.powColor = "yellow";
      B.value.powTextColor="white";
    } else {
      B.value.powColor = "";
      B.value.powTextColor="";
    }
  }    
    B.value.pf = testData.value.pdu_data.line_item_list.power_factor[1]?.toFixed(2);
    controlVis.value.haveB = true;
  }
  if(testData.value.pdu_data.line_item_list.ele_active.length > 2){
    C.value.cur_value = testData.value.pdu_data.line_item_list.cur_value[2]?.toFixed(2);
    C.value.curPercemtage = (testData.value.pdu_data.line_item_list.cur_value[2] / testData.value.pdu_data.line_item_list.cur_alarm_max[2]) * 100;
    C.value.cur_alarm_max = testData.value.pdu_data.line_item_list.cur_alarm_max[2];
    if(testData.value.pdu_data.line_item_list.cur_alarm_status){
      let curalarm = testData.value.pdu_data.line_item_list.cur_alarm_status[2];
      if(curalarm == 1 || curalarm == 8 ){
        C.value.curColor = "red";
        C.value.curTextColor="white";
        C.value.sphereColor ='red';
      } else if(curalarm == 2 || curalarm == 4 ){
        C.value.curColor = "yellow";
        C.value.curTextColor="white";
        C.value.sphereColor ='yellow';
      } else{
        C.value.curColor = "";
        C.value.curTextColor = "black";
        C.value.sphereColor ='#0AD0EE';
      }
    }

    C.value.vol_value = testData.value.pdu_data.line_item_list.vol_value[2]?.toFixed(1);
    if(testData.value.pdu_data.line_item_list.vol_alarm_status){
      let u2alarm = testData.value.pdu_data.line_item_list.vol_alarm_status[2];
      if(u2alarm == 1 || u2alarm == 8 ){
        C.value.volColor = "red";
        C.value.volTextColor="white";
      } else if(u2alarm == 2 || u2alarm == 4 ){
        C.value.volColor = "yellow";
        C.value.volTextColor="white";
      } else{
        C.value.volColor = "";
        C.value.volTextColor="";
      }
    }
 
    C.value.pow_value =testData.value.pdu_data.line_item_list.pow_value[2]?.toFixed(3);
  if(testData.value.pdu_data.line_item_list.pow_alarm_status){
    if(testData.value.pdu_data.line_item_list.pow_alarm_status){
      let powalarm = testData.value.pdu_data.line_item_list.pow_alarm_status[2];
      if(powalarm == 1 || powalarm == 8 ){
        C.value.powColor = "red";
        C.value.powTextColor="white";
      } else if(powalarm == 2 || powalarm == 4 ){
        C.value.powColor = "yellow";
        C.value.powTextColor="white";
      } else {
        C.value.powColor = "";
        C.value.powTextColor="";
      }
    }
  }
    C.value.pf = testData.value.pdu_data.line_item_list.power_factor[2]?.toFixed(2);
    controlVis.value.haveC = true;
  }
  }
  controlVis.value.display = true;
}

watch([() => typeRadioShowCur.value],async () => {
    // lineidBeforeChartUnmount();

    let itemCurTimeType = typeRadioShowCur.value == "最小" ? 'cur_min_time' : (typeRadioShowCur.value == "最大" ? 'cur_max_time' : '')
    dataTimeCur.value = {
      L1DataTime: [],
      L2DataTime: [],
      L3DataTime: []
    }
    if(itemCurTimeType != '') {
      dataTimeCur.value.L1DataTime = resultCur.value.l.map((item) => item[itemCurTimeType] ? item[itemCurTimeType] : '');
      dataTimeCur.value.L2DataTime = resultCur.value.ll.map((item) => item[itemCurTimeType] ? item[itemCurTimeType] : '');
      dataTimeCur.value.L3DataTime = resultCur.value.lll.map((item) => item[itemCurTimeType] ? item[itemCurTimeType] : '');
    }

    processChartData(resultCur.value.l, lChartData);
    processChartData(resultCur.value.ll, llChartData);
    processChartData(resultCur.value.lll, lllChartData);

      // 创建新的图表实例
    lineidChart = echarts.init(document.getElementById('lineidChartContainer'));
    // 设置新的配置对象
    if (lineidChart) {
    lineidChart.setOption({
      title: { text: ''},
      tooltip: { trigger: 'axis',      formatter: function (params) {
        let result = params[0].name + '<br>';
        params.forEach((param,i) => {
          result +=  params[i].marker + params[i].seriesName;
          result += ':' + buKongGe(params[i].value,2)
          if (param.seriesName === 'L1-电压' || param.seriesName === 'L2-电压' || param.seriesName === 'L3-电压') {
            result += 'V';
          } else  {
            result += ' A';
          }
          if(dataTimeCur.value[`L${i+1}DataTime`].length && dataTimeCur.value[`L${i+1}DataTime`][params[i].dataIndex] != '') {
            result += '&nbsp;&nbsp;发生时间:' + dataTimeCur.value[`L${i+1}DataTime`][params[i].dataIndex]
          }
          result += '<br>';
        });
        return result.trimEnd(); // 去除末尾多余的换行符
      }},
      legend: {data: ['L1-电流', 'L2-电流', 'L3-电流']},
      grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
      toolbox: {
            show: true,//是否显示工具箱开关
            top: 'center',
            orient: 'vertical',
            feature: {
              // dataView: { show: true, readOnly: true },//数据视图打开并允许编辑
              // restore: { show: true },//重新加载视图
              saveAsImage: { show: true,name: "电流" + formatDate(new Date) },//保存为图片
              // dataZoom :{},
            },
          },
      xAxis: {
        type: 'category',axisLabel: { formatter: 
            function (value) {
              if(toggleTime.value == "oneHour"){
                // 截取字符串的前n位，即yyyy-MM-dd HH:mm:ss
                return value.substring(11, 19);
              } else if(toggleTime.value == "twentyfourHour" || toggleTime.value == "seventytwoHour"){
                // 截取字符串的n位，即yyyy-MM-dd HH:mm:ss
                return value.substring(5, 19);
              } else if(toggleTime.value=='oneMonth'){
                return value.substring(0, 10);
              }
            }
          },
        boundaryGap: false,
        data:lineidDateTimes.value
      },
      yAxis: {
        type: 'value',
        axisLabel: {formatter: '{value}A'}
      },
      series: [
        {
          name: 'L1-电流',
          type: 'line',
          data: lChartData.value.curValueList,
          ...(lChartData.value.curValueList?.length==1? {symbol: 'circle'}:{symbol:'none'}),
          symbolSize: 4
        },
        {
          name: 'L2-电流',
          type: 'line',
          data: llChartData.value.curValueList,
          ...(llChartData.value.curValueList?.length==1? {symbol: 'circle'}:{symbol:'none'}),
          symbolSize: 4,
        },
        {
          name: 'L3-电流',
          type: 'line',
          data: lllChartData.value.curValueList,
          ...(lllChartData.value.curValueList?.length==1? {symbol: 'circle'}:{symbol:'none'}),
          symbolSize: 4,
        }
      ]
    },true)
    lineidChart.clear;
    }
})

watch([() => typeRadioShowVol.value],async () => {

    let itemVolTimeType = typeRadioShowVol.value == "最小" ? 'vol_min_time' : (typeRadioShowVol.value == "最大" ? 'vol_max_time' : '')
    dataTime.value = {
      L1DataTime: [],
      L2DataTime: [],
      L3DataTime: []
    }
    if(itemVolTimeType != '') {
      dataTime.value.L1DataTime = resultVol.value.l.map((item) => item[itemVolTimeType] ? item[itemVolTimeType] : '');
      dataTime.value.L2DataTime = resultVol.value.ll.map((item) => item[itemVolTimeType] ? item[itemVolTimeType] : '');
      dataTime.value.L3DataTime = resultVol.value.lll.map((item) => item[itemVolTimeType] ? item[itemVolTimeType]: '');
    }

    processChartDataV(resultVol.value.l, lChartDataV);
    processChartDataV(resultVol.value.ll, llChartDataV);
    processChartDataV(resultVol.value.lll, lllChartDataV);

      // 创建新的图表实例
    lineidChartV = echarts.init(document.getElementById('lineidChartContainerV'));
    // 设置新的配置对象
    if (lineidChartV) {
    lineidChartV.setOption({
      title: { text: ''},
      tooltip: { trigger: 'axis',      formatter: function (params) {
        let result = params[0].name + '<br>';
        params.forEach((param,i) => {
          result +=  params[i].marker + params[i].seriesName;
          result += ':' + buKongGe(params[i].value,1);
          if (param.seriesName === 'L1-电压' || param.seriesName === 'L2-电压' || param.seriesName === 'L3-电压') {
            result += 'V';
          } else  {
            result += ' A';
          }
          if(dataTime.value[`L${i+1}DataTime`].length && dataTime.value[`L${i+1}DataTime`][params[i].dataIndex] != '') {
            result += '&nbsp&nbsp发生时间:' + dataTime.value[`L${i+1}DataTime`][params[i].dataIndex]
          }
          result += '<br>';
        });
        return result.trimEnd(); // 去除末尾多余的换行符
      }},
      legend: {data: ['L1-电压', 'L2-电压', 'L3-电压']},
      grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
      toolbox: {
            show: true,//是否显示工具箱开关
            top: 'center',
            orient: 'vertical',
            feature: {
              // dataView: { show: true, readOnly: true },//数据视图打开并允许编辑
              // restore: { show: true },//重新加载视图
              saveAsImage: { show: true,name: "电压" + formatDate(new Date) },//保存为图片
              // dataZoom :{},
            },
          },
      xAxis: {
        type: 'category',axisLabel: { formatter: 
            function (value) {
              if(toggleTimeV.value == "oneHour"){
                // 截取字符串的前n位，即yyyy-MM-dd HH:mm:ss
                return value.substring(11, 19);
              } else if(toggleTimeV.value == "twentyfourHour" || toggleTimeV.value == "seventytwoHour"){
                // 截取字符串的n位，即yyyy-MM-dd HH:mm:ss
                return value.substring(5, 19);
              }else if(toggleTimeV.value == "oneMonth" ){
                // 截取字符串的n位，即yyyy-MM-dd HH:mm:ss
                return value.substring(0, 10);
              }
            }
          },
        boundaryGap: false,
        data:lineidDateTimesV.value
      },
      yAxis: {
        type: 'value',
        axisLabel: {formatter: '{value}V'}
      },
      series: [
        {
          name: 'L1-电压',
          type: 'line',
          data: lChartDataV.value.volValueList,
          ...(lChartDataV.value.volValueList?.length==1? {symbol: 'circle'}:{symbol:'none'}),
          symbolSize: 4
        },
        {
          name: 'L2-电压',
          type: 'line',
          data: llChartDataV.value.volValueList,
          ...(llChartDataV.value.volValueList?.length==1? {symbol: 'circle'}:{symbol:'none'}),
          symbolSize: 4,
        },
        {
          name: 'L3-电压',
          type: 'line',
          data: lllChartDataV.value.volValueList,
          ...(lllChartDataV.value.volValueList?.length==1? {symbol: 'circle'}:{symbol:'none'}),
          symbolSize: 4,
        }
      ]
    },true)
    }
})

watch([() => toggleTime.value],async ()=>{
    // lineidBeforeChartUnmount();

    await PDUHdaLineHisdata(toggleTime.value)

      // 创建新的图表实例
    lineidChart = echarts.init(document.getElementById('lineidChartContainer'));
    // 设置新的配置对象
    if (lineidChart) {
    lineidChart.setOption({
      title: { text: ''},
      tooltip: { trigger: 'axis',      formatter: function (params) {
        let result = params[0].name + '<br>';
        params.forEach((param,i) => {
          result +=  params[i].marker + params[i].seriesName;
          result += ':' + buKongGe(params[i].value,2)
          if (param.seriesName === 'L1-电压' || param.seriesName === 'L2-电压' || param.seriesName === 'L3-电压') {
            result += 'V';
          } else  {
            result += ' A';
          }
          if(dataTimeCur.value[`L${i+1}DataTime`].length && dataTimeCur.value[`L${i+1}DataTime`][params[i].dataIndex] != '') {
            result += '&nbsp;&nbsp;发生时间:' + dataTimeCur.value[`L${i+1}DataTime`][params[i].dataIndex]
          }
          result += '<br>';
        });
        return result.trimEnd(); // 去除末尾多余的换行符
      }},
      legend: {data: ['L1-电流', 'L2-电流', 'L3-电流']},
      grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
      toolbox: {
            show: true,//是否显示工具箱开关
            top: 'center',
            orient: 'vertical',
            feature: {
              // dataView: { show: true, readOnly: true },//数据视图打开并允许编辑
              // restore: { show: true },//重新加载视图
              saveAsImage: { show: true,name: "电流" + formatDate(new Date) },//保存为图片
              // dataZoom :{},
            },
          },
      xAxis: {
        type: 'category',axisLabel: { formatter: 
            function (value) {
              if(toggleTime.value == "oneHour"){
                // 截取字符串的前n位，即yyyy-MM-dd HH:mm:ss
                return value.substring(11, 19);
              } else if(toggleTime.value == "twentyfourHour" || toggleTime.value == "seventytwoHour"){
                // 截取字符串的n位，即yyyy-MM-dd HH:mm:ss
                return value.substring(5, 19);
              }else if(toggleTime.value == "oneMonth"){
                return value.substring(0, 10);
              }
            }
          },
        boundaryGap: false,
        data:lineidDateTimes.value
      },
      yAxis: {
        type: 'value',
        axisLabel: {formatter: '{value}A'}
      },
      series: [
        {
          name: 'L1-电流',
          type: 'line',
          data: lChartData.value.curValueList,
          ...(lllChartData.value.curValueList?.length==1? {symbol: 'circle'}:{symbol:'none'}),
          symbolSize: 4
        },
        {
          name: 'L2-电流',
          type: 'line',
          data: llChartData.value.curValueList,
          ...(lllChartData.value.curValueList?.length==1? {symbol: 'circle'}:{symbol:'none'}),
          symbolSize: 4,
        },
        {
          name: 'L3-电流',
          type: 'line',
          data: lllChartData.value.curValueList,
          ...(lllChartData.value.curValueList?.length==1? {symbol: 'circle'}:{symbol:'none'}),
          symbolSize: 4,
        }
      ]
    },true)
    }
})

watch([() => toggleTimeV.value],async ()=>{
    await PDUHdaLineHisdataV(toggleTimeV.value)

      // 创建新的图表实例
    lineidChartV = echarts.init(document.getElementById('lineidChartContainerV'));
    // 设置新的配置对象
    if (lineidChartV) {
    lineidChartV.setOption({
      title: { text: ''},
      tooltip: { trigger: 'axis',      formatter: function (params) {
        let result = params[0].name + '<br>';
        params.forEach((param,i) => {
          result +=  params[i].marker + params[i].seriesName;
          result += ':' + buKongGe(params[i].value,1);
          if (param.seriesName === 'L1-电压' || param.seriesName === 'L2-电压' || param.seriesName === 'L3-电压') {
            result += 'V';
          } else  {
            result += ' A';
          }
          if(dataTime.value[`L${i+1}DataTime`].length && dataTime.value[`L${i+1}DataTime`][params[i].dataIndex] != '') {
            result += '&nbsp&nbsp发生时间:' + dataTime.value[`L${i+1}DataTime`][params[i].dataIndex]
          }
          result += '<br>';
        });
        return result.trimEnd(); // 去除末尾多余的换行符
      }},
      legend: {data: ['L1-电压', 'L2-电压', 'L3-电压']},
      grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
      toolbox: {
            show: true,//是否显示工具箱开关
            top: 'center',
            orient: 'vertical',
            feature: {
              // dataView: { show: true, readOnly: true },//数据视图打开并允许编辑
              // restore: { show: true },//重新加载视图
              saveAsImage: { show: true,name: "电压" + formatDate(new Date) },//保存为图片
              // dataZoom :{},
            },
          },
          //        toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
      xAxis: {
        type: 'category',axisLabel: { formatter: 
            function (value) {
              if(toggleTimeV.value == "oneHour"){
                // 截取字符串的前n位，即yyyy-MM-dd HH:mm:ss
                return value.substring(11, 19);
              } else if(toggleTimeV.value == "twentyfourHour" || toggleTimeV.value == "seventytwoHour"){
                // 截取字符串的n位，即yyyy-MM-dd HH:mm:ss
                return value.substring(5, 19);
              }else if(toggleTimeV.value == "oneMonth"){
                return value.substring(0, 10);
              }
            }
          },
        boundaryGap: false,
        data:lineidDateTimesV.value
      },
      yAxis: {
        type: 'value',
        axisLabel: {formatter: '{value}V'}
      },
      series: [
        {
          name: 'L1-电压',
          type: 'line',
          data: lChartDataV.value.volValueList,
          ...(lChartDataV.value.volValueList?.length==1? {symbol: 'circle'}:{symbol:'none'}),
          symbolSize: 4
        },
        {
          name: 'L2-电压',
          type: 'line',
          data: llChartDataV.value.volValueList,
          ...(llChartDataV.value.volValueList?.length==1? {symbol: 'circle'}:{symbol:'none'}),
          symbolSize: 4,
        },
        {
          name: 'L3-电压',
          type: 'line',
          data: lllChartDataV.value.volValueList,
          ...(lllChartDataV.value.volValueList?.length==1? {symbol: 'circle'}:{symbol:'none'}),
          symbolSize: 4,
        }
      ]
    },true)
    }
})

watch([() => typeRadioShowPower.value], async ([value]) => {
  let itemApparentType = value == "最小" ? 'apparentListMin' : (value == "最大" ? 'apparentListMax' : 'apparentList')
  let itemActiveType = value == "最小" ? 'activeListMin' : (value == "最大" ? 'activeListMax' : 'activeList')
  let itemReactiveType = value == "最小" ? 'reactiveListMin' : (value == "最大" ? 'reactiveListMax' : 'reactiveList')

  let timeArr = [itemActiveType + 'Time',itemApparentType + 'Time',itemReactiveType + 'Time']

    // 创建新的图表实例
    chart = echarts.init(document.getElementById('chartContainer'));
    chart.setOption({})

    // 设置新的配置对象
    if (chart) {
      chart.setOption({
  toolbox: {
            show: true,//是否显示工具箱开关
            top: 'center',
            orient: 'vertical',
            feature: {
              // dataView: { show: true, readOnly: true },//数据视图打开并允许编辑
              // restore: { show: true },//重新加载视图
              saveAsImage: { show: true,name: "总功率趋势图" + formatDate(new Date) },//保存为图片
              // dataZoom :{},
            },
          },
        // 这里设置 Echarts 的配置项和数据
        title: { text: ''},
        tooltip: { trigger: 'axis' ,formatter: function(params) {
          let result = params[0].name + '<br>';
          for (var i = 0; i < params.length; i++) {
            // result +=  params[i].marker + params[i].seriesName;
            // if(itemApparentType != 'apparentList' && chartData.value[timeArr[i]].length) {
            //   result += '&nbsp&nbsp发生时间:' + chartData.value[timeArr[i]][params[i].dataIndex]
            // }
            // result += '&nbsp&nbsp' + params[i].value
            // if (params[i].seriesName === '总视在功率') {
            //   result += ' kVA'; 
            // } else if (params[i].seriesName === '总有功功率') {
            //   result += ' kW';
            // }else if (params[i].seriesName === '总无功功率') {
            //   result += ' kVar';
            // }
            result +=  params[i].marker + params[i].seriesName;
            result += ':' + buKongGe(params[i].value,3)
            //判断是否给鼠标悬停上显示符号
            if (params[i].seriesName === '总视在功率') {
              result += ' kVA&nbsp;'; 
            } else if (params[i].seriesName === '总有功功率') {
              result += ' kW&nbsp;&nbsp;';
            }else if (params[i].seriesName === '总无功功率') {
              result += ' kVar';
            }
            if(itemApparentType != 'apparentList' && chartData.value[timeArr[i]].length) {
              result += '&nbsp;发生时间:' + chartData.value[timeArr[i]][params[i].dataIndex]
            }
            result += '<br>';
          }
          return result;
        }},
        legend: { data: ['总有功功率','总视在功率','总无功功率']},
        grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
        xAxis: {type: 'category', axisLabel: { formatter: 
              function (value) {
                if(queryParams.powGranularity == "oneHour"){
                  // 截取字符串的前n位，即yyyy-MM-dd HH:mm:ss
                  return value.substring(11, 19);
                } else if(queryParams.powGranularity == "twentyfourHour" || queryParams.powGranularity == "seventytwoHour"){
                  // 截取字符串的n位，即yyyy-MM-dd HH:mm:ss
                  return value.substring(5, 19);
                }
              }
            },boundaryGap: false, data:chartData.value.dateTimes},
        yAxis: { type: 'value'},
        series: [
        {name: '总有功功率', type: 'line', data: chartData.value[itemActiveType] , ...(chartData.value[itemActiveType]?.length==1? {symbol: 'circle'}:{symbol:'none'}), symbolSize: 4},
        {name: '总视在功率', type: 'line', data: chartData.value[itemApparentType] , ...(chartData.value[itemApparentType]?.length==1? {symbol: 'circle'}:{symbol:'none'}), symbolSize: 4},
        {name: '总无功功率', type: 'line', data: chartData.value[itemReactiveType] , ...(chartData.value[itemReactiveType]?.length==1? {symbol: 'circle'}:{symbol:'none'}), symbolSize: 4},
        ],
      },true);
    }

    if(flashListTimer.value.chartTimer){
      var time = 0;
      if(queryParams.powGranularity == "oneHour"){
        time = 60000;
        // time = 3000;
      } else if(queryParams.powGranularity == "twentyfourHour"){
        time = 3600000;
        // time = 3000;
      } else if(queryParams.powGranularity == "seventytwoHour"){
        time = 3600000;
        // time = 3000;
      }
      clearInterval(flashListTimer.value.chartTimer)
      flashListTimer.value.chartTimer = null;
      // flashListTimer.value.chartTimer = setInterval((setNewChartData), time);
    }
})

watch([() => typeRadioShowFactor.value], async ([value]) => {
    let itemFactorType = value == "最小" ? 'factorListMin' : (value == "最大" ? 'factorListMax' : 'factorList')
    console.log(itemFactorType)
    // 创建新的图表实例
    chartF = echarts.init(document.getElementById('chartContainerF'));

    // 设置新的配置对象
    if (chartF) {
      let legendList
      let seriesList
      if(chartDataF.value.size == 3) {
        legendList = ['总功率因数','L1功率因数','L2功率因数','L3功率因数']
        seriesList = [
          {name: '总功率因数', type: 'line', data: chartDataF.value[itemFactorType] , ...(chartDataF.value[itemFactorType]?.length==1? {symbol: 'circle'}:{symbol:'none'}), symbolSize: 4},
          {name: 'L1功率因数', type: 'line', data: chartDataF.value[itemFactorType +'a'] , ...(chartDataF.value[itemFactorType+ 'a']?.length==1? {symbol: 'circle'}:{symbol:'none'}), symbolSize: 4},
          {name: 'L2功率因数', type: 'line', data: chartDataF.value[itemFactorType + 'b'] , ...(chartDataF.value[itemFactorType+ 'b']?.length==1? {symbol: 'circle'}:{symbol:'none'}), symbolSize: 4},
          {name: 'L3功率因数', type: 'line', data: chartDataF.value[itemFactorType + 'c'] , ...(chartDataF.value[itemFactorType+ 'c']?.length==1? {symbol: 'circle'}:{symbol:'none'}), symbolSize: 4},
        ]
      } else {
        legendList = ['总功率因数','L1功率因数','L2功率因数','L3功率因数']
        seriesList = [
          {name: '总功率因数', type: 'line', data: chartDataF.value[itemFactorType] , ...(chartDataF.value[itemFactorType]?.length==1? {symbol: 'circle'}:{symbol:'none'}), symbolSize: 4}
        ]
      }

      chartF.setOption({
        // 这里设置 Echarts 的配置项和数据
        title: { text: ''},
        tooltip: { trigger: 'axis' ,formatter: function(params) {
          let result = params[0].name + '<br>';
          for (var i = 0; i < params.length; i++) {
            result +=  params[i].marker + params[i].seriesName;
            result += ':' + params[i].value
            if(itemFactorType != 'factorList' && chartDataF.value?.[`${itemFactorType}Time`].length) {
              result += '&nbsp&nbsp发生时间:' + chartDataF.value[`${itemFactorType}Time`][params[i].dataIndex]
            }
            result += '<br>';
          }
          return result;
        }},
        legend: { data: legendList},
        grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
        toolbox: {
            show: true,//是否显示工具箱开关
            top: 'center',
            orient: 'vertical',
            feature: {
              // dataView: { show: true, readOnly: true },//数据视图打开并允许编辑
              // restore: { show: true },//重新加载视图
              saveAsImage: { show: true,name: "功率因数趋势图" + formatDate(new Date) },//保存为图片
              // dataZoom :{},
            },
          },
        xAxis: {type: 'category', axisLabel: { formatter: 
              function (value) {
                if(queryParams.powGranularityF == "oneHour"){
                  // 截取字符串的前n位，即yyyy-MM-dd HH:mm:ss
                  return value.substring(11, 19);
                } else if(queryParams.powGranularityF == "twentyfourHour" || queryParams.powGranularityF == "seventytwoHour"){
                  // 截取字符串的n位，即yyyy-MM-dd HH:mm:ss
                  return value.substring(5, 19);
                }
              }
            },boundaryGap: false, data:chartDataF.value.dateTimes},
        yAxis: { type: 'value'},
        series: seriesList,
      },true);
    }

    if(flashListTimer.value.chartTimer){
      var time = 0;
      if(queryParams.powGranularityF == "oneHour"){
        time = 60000;
        // time = 3000;
      } else if(queryParams.powGranularityF == "twentyfourHour"){
        time = 3600000;
        // time = 3000;
      } else if(queryParams.powGranularityF == "seventytwoHour"){
        time = 3600000;
        // time = 3000;
      }
      clearInterval(flashListTimer.value.chartTimer)
      flashListTimer.value.chartTimer = null;
      // flashListTimer.value.chartTimer = setInterval((setNewChartData), time);
    }
})

watch([() => queryParams.powGranularity], async ([newPowGranularity]) => {
    // 销毁原有的图表实例
    //beforeChartUnmount();
    //获取数据
    let itemApparentType = typeRadioShowPower.value == "最小" ? 'apparentListMin' : (typeRadioShowPower.value == "最大" ? 'apparentListMax' : 'apparentList')
    let itemActiveType = typeRadioShowPower.value == "最小" ? 'activeListMin' : (typeRadioShowPower.value == "最大" ? 'activeListMax' : 'activeList')
    let itemReactiveType = typeRadioShowPower.value == "最小" ? 'reactiveListMin' : (typeRadioShowPower.value == "最大" ? 'reactiveListMax' : 'reactiveList')

    if(newPowGranularity == 'oneHour') {
      itemApparentType = 'apparentList'
      itemActiveType = 'activeList'
      itemReactiveType = 'reactiveList'
    }

    console.log(itemApparentType,itemActiveType,itemReactiveType)

    var tempParams = { devKey : queryParams.devKey, type : newPowGranularity}
    chartData.value = await PDUDeviceApi.PDUHis(tempParams); 

    chartData.value.apparentList.forEach((obj,index) => {
      chartData.value.apparentList[index] = obj?.toFixed(3);
      chartData.value.apparentListMin[index] = chartData.value.apparentListMin[index]?.toFixed(3);
      chartData.value.apparentListMax[index] = chartData.value.apparentListMax[index]?.toFixed(3);
      chartData.value.apparentListMinTime[index] = chartData.value.apparentListMinTime[index];
      chartData.value.apparentListMaxTime[index] = chartData.value.apparentListMaxTime[index];

      chartData.value.activeList[index] = chartData.value.activeList[index]?.toFixed(3);
      chartData.value.activeListMin[index] = chartData.value.activeListMin[index]?.toFixed(3);
      chartData.value.activeListMax[index] = chartData.value.activeListMax[index]?.toFixed(3);
      chartData.value.activeListMinTime[index] = chartData.value.activeListMinTime[index];
      chartData.value.activeListMaxTime[index] = chartData.value.activeListMaxTime[index];

      chartData.value.reactiveList[index] = chartData.value.reactiveList[index]?.toFixed(3);
      chartData.value.reactiveListMin[index] = chartData.value.reactiveListMin[index]?.toFixed(3);
      chartData.value.reactiveListMax[index] = chartData.value.reactiveListMax[index]?.toFixed(3);
      chartData.value.reactiveListMinTime[index] = chartData.value.reactiveListMinTime[index];
      chartData.value.reactiveListMaxTime[index] = chartData.value.reactiveListMaxTime[index];
    });

  if(queryParams.powGranularity === 'oneHour'){
    chartData.value.dateTimes = chartData.value.dateTimes
  }else if(queryParams.powGranularity === 'twentyfourHour'){
    chartData.value.dateTimes = chartData.value.dateTimes.map(item => item.slice(5, item.length));
  }else if(queryParams.powGranularity === 'seventytwoHour'){
    chartData.value.dateTimes = chartData.value.dateTimes.map(item => item.slice(0, 16));
  }else if(queryParams.powGranularity === 'oneMonth'){
    chartData.value.dateTimes = chartData.value.dateTimes.map(item => item.slice(0, 10));
  }

  let timeArr = [itemActiveType + 'Time',itemApparentType + 'Time',itemReactiveType + 'Time']

    // 创建新的图表实例
    chart = echarts.init(document.getElementById('chartContainer'));

    // 设置新的配置对象
    if (chart) {
      chart.setOption({
  toolbox: {
            show: true,//是否显示工具箱开关
            top: 'center',
            orient: 'vertical',
            feature: {
              // dataView: { show: true, readOnly: true },//数据视图打开并允许编辑
              // restore: { show: true },//重新加载视图
              saveAsImage: { show: true,name: "总功率趋势图" + formatDate(new Date) },//保存为图片
              // dataZoom :{},
            },
          },
        // 这里设置 Echarts 的配置项和数据
        title: { text: ''},
        tooltip: { trigger: 'axis' ,formatter: function(params) {
          let result = params[0].name + '<br>';
          for (var i = 0; i < params.length; i++) {
            // result +=  params[i].marker + params[i].seriesName;
            // if(itemApparentType != 'apparentList' && chartData.value[timeArr[i]].length) {
            //   result += '&nbsp&nbsp发生时间:' + chartData.value[timeArr[i]][params[i].dataIndex]
            // }
            // result += '&nbsp&nbsp' + params[i].value
            // if (params[i].seriesName === '总视在功率') {
            //   result += ' kVA'; 
            // } else if (params[i].seriesName === '总有功功率') {
            //   result += ' kW';
            // }else if (params[i].seriesName === '总无功功率') {
            //   result += ' kVar';
            // }
            result +=  params[i].marker + params[i].seriesName;
            result += ':' + buKongGe(params[i].value,3)
            //判断是否给鼠标悬停上显示符号
            if (params[i].seriesName === '总视在功率') {
              result += ' kVA&nbsp;'; 
            } else if (params[i].seriesName === '总有功功率') {
              result += ' kW&nbsp;&nbsp;';
            }else if (params[i].seriesName === '总无功功率') {
              result += ' kVar';
            }
            if(itemApparentType != 'apparentList' && chartData.value[timeArr[i]].length) {
              result += '&nbsp;发生时间:' + chartData.value[timeArr[i]][params[i].dataIndex]
            }
              result += '<br>';
            }
          return result;
        }},
        legend: { data: ['总有功功率','总视在功率','总无功功率']},
        grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
        xAxis: {type: 'category', axisLabel: { formatter: 
              function (value) {
                if(queryParams.powGranularity == "oneHour"){
                  // 截取字符串的前n位，即yyyy-MM-dd HH:mm:ss
                  return value.substring(11, 19);
                } else if(queryParams.powGranularity == "twentyfourHour" || queryParams.powGranularity == "seventytwoHour"){
                  // 截取字符串的n位，即yyyy-MM-dd HH:mm:ss
                  return value.substring(5, 19);
                }else if(queryParams.powGranularity == "oneMonth"){
                  return value.substring(0, 10)
                }
              }
            },boundaryGap: false, data:chartData.value.dateTimes},
        yAxis: { type: 'value'},
        series: [
        {name: '总有功功率', type: 'line', data: chartData.value[itemActiveType] , ...(chartData.value[itemActiveType]?.length==1? {symbol: 'circle'}:{symbol:'none'}), symbolSize: 4},
        {name: '总视在功率', type: 'line', data: chartData.value[itemApparentType] , ...(chartData.value[itemApparentType]?.length==1? {symbol: 'circle'}:{symbol:'none'}), symbolSize: 4},
        {name: '总无功功率', type: 'line', data: chartData.value[itemReactiveType] , ...(chartData.value[itemReactiveType]?.length==1? {symbol: 'circle'}:{symbol:'none'}), symbolSize: 4},
        ],
      },true);
    }

    if(flashListTimer.value.chartTimer){
      var time = 0;
      if(queryParams.powGranularity == "oneHour"){
        time = 60000;
        // time = 3000;
      } else if(queryParams.powGranularity == "twentyfourHour"){
        time = 3600000;
        // time = 3000;
      } else if(queryParams.powGranularity == "seventytwoHour"){
        time = 3600000;
        // time = 3000;
      }
      clearInterval(flashListTimer.value.chartTimer)
      flashListTimer.value.chartTimer = null;
      // flashListTimer.value.chartTimer = setInterval((setNewChartData), time);
    }
})

watch([() => queryParams.powGranularityF], async ([newPowGranularityF]) => {
    // 销毁原有的图表实例
     //beforeChartUnmount();
    //获取数据
    let itemFactorType = typeRadioShowFactor.value == "最小" ? 'factorListMin' : (typeRadioShowFactor.value == "最大" ? 'factorListMax' : 'factorList')

    if(newPowGranularityF == 'oneHour') {
      itemFactorType = 'factorList'
    }

    var tempParams = { devKey : queryParams.devKey, type : newPowGranularityF}
    chartDataF.value = await PDUDeviceApi.PDUHis(tempParams); 
    chartDataF.value.factorList.forEach((obj,index) => {
      chartDataF.value.factorList[index] = obj?.toFixed(2);
      chartDataF.value.factorListMin[index] = chartDataF.value.factorListMin[index]?.toFixed(2);
      chartDataF.value.factorListMax[index] = chartDataF.value.factorListMax[index]?.toFixed(2);
      chartDataF.value.factorListMinTime[index] = chartDataF.value.factorListMinTime[index];
      chartDataF.value.factorListMaxTime[index] = chartDataF.value.factorListMaxTime[index];

      if(chartDataF.value.size == 3) {
        chartDataF.value.factorLista[index] = chartDataF.value.factorLista[index]?.toFixed(2);
        chartDataF.value.factorListMina[index] = chartDataF.value.factorListMina[index]?.toFixed(2);
        chartDataF.value.factorListMaxa[index] = chartDataF.value.factorListMaxa[index]?.toFixed(2);
        chartDataF.value.factorListMinTimea[index] = chartDataF.value.factorListMinTimea[index];
        chartDataF.value.factorListMaxTimea[index] = chartDataF.value.factorListMaxTimea[index];
        
        chartDataF.value.factorListb[index] = chartDataF.value.factorListb[index]?.toFixed(2);
        chartDataF.value.factorListMinb[index] = chartDataF.value.factorListMinb[index]?.toFixed(2);
        chartDataF.value.factorListMaxb[index] = chartDataF.value.factorListMaxb[index]?.toFixed(2);
        chartDataF.value.factorListMinTimeb[index] = chartDataF.value.factorListMinTimeb[index];
        chartDataF.value.factorListMaxTimeb[index] = chartDataF.value.factorListMaxTimeb[index];

        chartDataF.value.factorListc[index] = chartDataF.value.factorListc[index]?.toFixed(2);
        chartDataF.value.factorListMinc[index] = chartDataF.value.factorListMinc[index]?.toFixed(2);
        chartDataF.value.factorListMaxc[index] = chartDataF.value.factorListMaxc[index]?.toFixed(2);
        chartDataF.value.factorListMinTimec[index] = chartDataF.value.factorListMinTimec[index];
        chartDataF.value.factorListMaxTimec[index] = chartDataF.value.factorListMaxTimec[index];
      }
    });

  if(queryParams.powGranularityF === 'oneHour'){
    chartDataF.value.dateTimes = chartDataF.value.dateTimes
  }else if(queryParams.powGranularityF === 'twentyfourHour'){
    chartDataF.value.dateTimes = chartDataF.value.dateTimes.map(item => item.slice(5, item.length));
  }else if(queryParams.powGranularityF === 'seventytwoHour'){
    chartDataF.value.dateTimes = chartDataF.value.dateTimes.map(item => item.slice(0, 16));
  }else if(queryParams.powGranularityF === 'oneMonth'){
    chartDataF.value.dateTimes = chartDataF.value.dateTimes.map(item => item.slice(0, 10));
  }
    // 创建新的图表实例
    chartF = echarts.init(document.getElementById('chartContainerF'));

    // 设置新的配置对象
    if (chartF) {
      let legendList
      let seriesList
      if(chartDataF.value.size == 3) {
        legendList = ['总功率因数','L1功率因数','L2功率因数','L3功率因数']
        seriesList = [
          {name: '总功率因数', type: 'line', data: chartDataF.value[itemFactorType] , ...(chartDataF.value[itemFactorType]?.length==1? {symbol: 'circle'}:{symbol:'none'}), symbolSize: 4},
          {name: 'L1功率因数', type: 'line', data: chartDataF.value[itemFactorType +'a'] , ...(chartDataF.value[itemFactorType + 'a']?.length==1? {symbol: 'circle'}:{symbol:'none'}), symbolSize: 4},
          {name: 'L2功率因数', type: 'line', data: chartDataF.value[itemFactorType + 'b'] , ...(chartDataF.value[itemFactorType + 'b']?.length==1? {symbol: 'circle'}:{symbol:'none'}), symbolSize: 4},
          {name: 'L3功率因数', type: 'line', data: chartDataF.value[itemFactorType + 'c'] , ...(chartDataF.value[itemFactorType + 'c']?.length==1? {symbol: 'circle'}:{symbol:'none'}), symbolSize: 4},
        ]
      } else {
        legendList = ['总功率因数','L1功率因数','L2功率因数','L3功率因数']
        seriesList = [
          {name: '总功率因数', type: 'line', data: chartDataF.value[itemFactorType] , ...(chartDataF.value[itemFactorType]?.length==1? {symbol: 'circle'}:{symbol:'none'}), symbolSize: 4}
        ]
      }

      chartF.setOption({
        // 这里设置 Echarts 的配置项和数据
        title: { text: ''},
        tooltip: { trigger: 'axis' ,formatter: function(params) {
          let result = params[0].name + '<br>';
          for (var i = 0; i < params.length; i++) {
            result +=  params[i].marker + params[i].seriesName;
            result += ':' + params[i].value
            if(itemFactorType != 'factorList' && chartDataF.value?.[`${itemFactorType}Time`].length) {
              result += '&nbsp&nbsp发生时间:' + chartDataF.value[`${itemFactorType}Time`][params[i].dataIndex]
            }
            result += '<br>';
          }
          return result;
        }},
        legend: { data: legendList},
        grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
        toolbox: {
            show: true,//是否显示工具箱开关
            top: 'center',
            orient: 'vertical',
            feature: {
              // dataView: { show: true, readOnly: true },//数据视图打开并允许编辑
              // restore: { show: true },//重新加载视图
              saveAsImage: { show: true,name: "功率因数趋势图" + formatDate(new Date) },//保存为图片
              // dataZoom :{},
            },
          },
        xAxis: {type: 'category', axisLabel: { formatter: 
              function (value) {
                if(queryParams.powGranularityF == "oneHour"){
                  // 截取字符串的前n位，即yyyy-MM-dd HH:mm:ss
                  return value.substring(11, 19);
                } else if(queryParams.powGranularityF == "twentyfourHour" || queryParams.powGranularityF == "seventytwoHour"){
                  // 截取字符串的n位，即yyyy-MM-dd HH:mm:ss
                  return value.substring(5, 19);
                }else if(queryParams.powGranularityF == "oneMonth" ){
                  // 截取字符串的n位，即yyyy-MM-dd HH:mm:ss
                  return value.substring(0, 10);
                }
              }
            },boundaryGap: false, data:chartDataF.value.dateTimes},
        yAxis: { type: 'value'},
        series: seriesList,
      },true);
    }

    if(flashListTimer.value.chartTimer){
      var time = 0;
      if(queryParams.powGranularityF == "oneHour"){
        time = 60000;
        // time = 3000;
      } else if(queryParams.powGranularityF == "twentyfourHour"){
        time = 3600000;
        // time = 3000;
      } else if(queryParams.powGranularityF == "seventytwoHour"){
        time = 3600000;
        // time = 3000;
      }
      clearInterval(flashListTimer.value.chartTimer)
      flashListTimer.value.chartTimer = null;
      // flashListTimer.value.chartTimer = setInterval((setNewChartData), time);
    }
})

/** 搜索按钮操作 */
const handleQuery = async () => {
  controlVis.value.display = false;
  // if(queryParams.ipAddr){
  //   queryParams.devKey = queryParams.ipAddr +'-' +  queryParams.cascadeAddr;
  //   await getTestData();
  //   flashChartData();
  // }
    await getTestData();
    await getLocation();
    flashChartData();
    
    window.history.pushState({}, '', new URL(window.location.href).pathname+'?devKey='+queryParams.devKey+'&location='+location2.value);
}

const getLocation = async () => {
  const data = await PDUDeviceApi.getLocation(queryParams);
  if(data){
    location2.value = data;
  }else{
    location2.value = '';
  }

}

//L1,L2,L3的数据
const lChartData = ref({
  volValueList : [] as number[], //电压
  curValueList : [] as number[] //电流
});

const llChartData = ref({
  volValueList : [] as number[],
  curValueList : [] as number[]
});

const lllChartData = ref({
  volValueList : [] as number[],
  curValueList : [] as number[]
});

const lChartDataV = ref({
  volValueList : [] as number[], //电压
  curValueList : [] as number[], //电流
  volTimeList: [] as string[]
});

const llChartDataV = ref({
  volValueList : [] as number[], //电压
  curValueList : [] as number[], //电流
  volTimeList: [] as string[]
});

const lllChartDataV = ref({
  volValueList : [] as number[], //电压
  curValueList : [] as number[], //电流
  volTimeList: [] as string[]
});

const lineidDateTimes = ref([] as string[])
const lineidDateTimesV = ref([] as string[])

const lineidBeforeChartUnmount = () => {
lineidChart?.dispose() // 销毁图表实例
//lineidChartV?.dispose() // 销毁图表实例
}

const lineidBeforeChartUnmountV = () => {

lineidChartV?.dispose() // 销毁图表实例
}

function processChartData(data, chartData) {
  if (data == null) return;

  try {
    let itemCurType = typeRadioShowCur.value == "最小" ? 'cur_min_value' : (typeRadioShowCur.value == "最大" ? 'cur_max_value' : 'cur_avg_value')
    if(toggleTime.value == "oneHour") {
      itemCurType = 'cur_value'
    }

    const curValues = data.map(item => {
      if (typeof item[itemCurType] === 'number' && !isNaN(item[itemCurType])) {
        return item[itemCurType].toFixed(2);
      }
      throw new Error('Invalid cur_value');
    });

    // 批量操作以减少响应式更新次数
    chartData.value.curValueList = curValues;
  } catch (error) {
    console.error('Error processing chart data:', error);
  }
}

function processChartDataV(data, chartData) {
  if (data == null) return;

  try {
    let itemVolType = typeRadioShowVol.value == "最小" ? 'vol_min_value' : (typeRadioShowVol.value == "最大" ? 'vol_max_value' : 'vol_avg_value')
    if(toggleTimeV.value == "oneHour") {
      itemVolType = 'vol_value'
    }

    const volValues = data.map(item => {
      if (typeof item[itemVolType] === 'number' && !isNaN(item[itemVolType])) {
        return item[itemVolType].toFixed(1);
      }
      throw new Error('Invalid vol_value');
    });

    // 批量操作以减少响应式更新次数
    chartData.value.volValueList = volValues;
  } catch (error) {
    console.error('Error processing chart data:', error);
  }
}

const dataTimeCur = ref({
  L1DataTime: [],
  L2DataTime: [],
  L3DataTime: []
})

const dataTime = ref({
  L1DataTime: [],
  L2DataTime: [],
  L3DataTime: []
})

//获取最近一个小时的PDU相历史数据，处理L1,L2,L3的数据
const PDUHdaLineHisdata = async (type) => {

//   if (lData != null){
//   lData.forEach(item => {
//     lChartData.value.volValueList.push(item.vol_value.toFixed(1))
//     lChartData.value.curValueList.push(item.cur_value.toFixed(2))
//   })
// }
//   if (llData != null){
//   llData.forEach(item => {
//     llChartData.value.volValueList.push(item.vol_value.toFixed(1))
//     llChartData.value.curValueList.push(item.cur_value.toFixed(2))
//   })
// }
//   if (lllData != null){
//   lllData.forEach(item => {
//     lllChartData.value.volValueList.push(item.vol_value.toFixed(1))
//     lllChartData.value.curValueList.push(item.cur_value.toFixed(2))
//   })
//   }
  let result
  if(type === 'oneHour'){
    result = await PDUDeviceApi.getPDUHdaLineHisdata({ devKey : queryParams.devKey, type: 'oneHour'})
    lineidDateTimes.value = result.dateTimes
  }else if(type === 'twentyfourHour'){
    result = await PDUDeviceApi.getPDUHdaLineHisdata({ devKey : queryParams.devKey , type: 'twentyfourHour'})
    lineidDateTimes.value = result.dateTimes.map(item => item.slice(5, item.length));
  }else if(type === 'seventytwoHour'){
    result = await PDUDeviceApi.getPDUHdaLineHisdata({ devKey : queryParams.devKey , type: 'seventytwoHour'})
    lineidDateTimes.value = result.dateTimes.map(item => item.slice(0, 16));
  }else if(type === 'oneMonth'){
    result = await PDUDeviceApi.getPDUHdaLineHisdata({ devKey : queryParams.devKey , type: 'oneMonth'})
    lineidDateTimes.value = result.dateTimes.map(item => item.slice(0, 10));
  }
  resultCur.value = result
  //{ devKey : queryParams.devKey, type : newPowGranularity} '192.168.1.184-0'

  let itemCurTimeType = typeRadioShowCur.value == "最小" ? 'cur_min_time' : (typeRadioShowCur.value == "最大" ? 'cur_max_time' : '')
  dataTimeCur.value = {
    L1DataTime: [],
    L2DataTime: [],
    L3DataTime: []
  }
  if(itemCurTimeType != '') {
    dataTimeCur.value.L1DataTime = resultCur.value.l.map((item) => item[itemCurTimeType] ? item[itemCurTimeType] : '');
    dataTimeCur.value.L2DataTime = resultCur.value.ll.map((item) => item[itemCurTimeType] ? item[itemCurTimeType] : '');
    dataTimeCur.value.L3DataTime = resultCur.value.lll.map((item) => item[itemCurTimeType] ? item[itemCurTimeType] : '');
  }

  processChartData(result.l, lChartData);
  processChartData(result.ll, llChartData);
  processChartData(result.lll, lllChartData);
}

//获取最近一个小时的PDU相历史数据，处理L1,L2,L3的数据
const PDUHdaLineHisdataV = async (type) => {

  // lDatal.forEach(item => {
  //   lChartDataV.value.volValueList.push(item.vol_value.toFixed(1))
  //   lChartDataV.value.curValueList.push(item.cur_value.toFixed(2))
  // })

  // llDatall.forEach(item => {
  //   llChartDataV.value.volValueList.push(item.vol_value.toFixed(1))
  //   llChartDataV.value.curValueList.push(item.cur_value.toFixed(2))
  // })

  // lllDatalll.forEach(item => {
  //   lllChartDataV.value.volValueList.push(item.vol_value.toFixed(1))
  //   lllChartDataV.value.curValueList.push(item.cur_value.toFixed(2))
  // })
  let result
  if(type === 'oneHour'){
    result = await PDUDeviceApi.getPDUHdaLineHisdata({ devKey : queryParams.devKey, type: 'oneHour'})
    lineidDateTimesV.value = result.dateTimes
  }else if(type === 'twentyfourHour'){
    result = await PDUDeviceApi.getPDUHdaLineHisdata({ devKey : queryParams.devKey , type: 'twentyfourHour'})
    lineidDateTimesV.value = result.dateTimes.map(item => item.slice(5, item.length));
  }else if(type === 'seventytwoHour'){
    result = await PDUDeviceApi.getPDUHdaLineHisdata({ devKey : queryParams.devKey , type: 'seventytwoHour'})
    lineidDateTimesV.value = result.dateTimes.map(item => item.slice(0, 16));
  }else if(type === 'oneMonth'){
    result = await PDUDeviceApi.getPDUHdaLineHisdata({ devKey : queryParams.devKey , type: 'oneMonth'})
    lineidDateTimesV.value = result.dateTimes.map(item => item.slice(0, 10));
  }
  resultVol.value = result
  //{ devKey : queryParams.devKey, type : newPowGranularity} '192.168.1.184-0'

  let itemVolTimeType = typeRadioShowVol.value == "最小" ? 'vol_min_time' : (typeRadioShowVol.value == "最大" ? 'vol_max_time' : '')
  dataTime.value = {
    L1DataTime: [],
    L2DataTime: [],
    L3DataTime: []
  }
  if(itemVolTimeType != '') {
    dataTime.value.L1DataTime = resultVol.value.l.map((item) => item[itemVolTimeType] ? item[itemVolTimeType] : '');
    dataTime.value.L2DataTime = resultVol.value.ll.map((item) => item[itemVolTimeType] ? item[itemVolTimeType] : '');
    dataTime.value.L3DataTime = resultVol.value.lll.map((item) => item[itemVolTimeType] ? item[itemVolTimeType] : '');
  }


  processChartDataV(result.l, lChartDataV);
  processChartDataV(result.ll, llChartDataV);
  processChartDataV(result.lll, lllChartDataV);
}

const lineidFlashChartData = async () =>{
  lineidBeforeChartUnmount()

  await PDUHdaLineHisdata(toggleTime.value)

  // 创建新的图表实例
  lineidChart = echarts.init(document.getElementById('lineidChartContainer'));
  // 设置新的配置对象
  if (lineidChart) {
    lineidChart.setOption({
      title: { text: ''},
      tooltip: { trigger: 'axis',      formatter: function (params) {
        let result = params[0].name + '<br>';
        params.forEach((param,i) => {
          result +=  params[i].marker + params[i].seriesName;
          result += ':' + buKongGe(params[i].value,2)
          if (param.seriesName === 'L1-电压' || param.seriesName === 'L2-电压' || param.seriesName === 'L3-电压') {
            result += 'V';
          } else  {
            result += ' A';
          }
          if(dataTimeCur.value[`L${i+1}DataTime`].length && dataTimeCur.value[`L${i+1}DataTime`][params[i].dataIndex] != '') {
            result += '&nbsp;&nbsp;发生时间:' + dataTimeCur.value[`L${i+1}DataTime`][params[i].dataIndex]
          }
          result += '<br>';
        });
        return result.trimEnd(); // 去除末尾多余的换行符
      }},
      legend: {
        data: ['L1-电流', 'L2-电流', 'L3-电流'], // 图例项
        selected: { 'L1-电流':true,'L2-电流':true,'L3-电流':true }
      },
      grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
      toolbox: {
            show: true,//是否显示工具箱开关
            top: 'center',
            orient: 'vertical',
            feature: {
              // dataView: { show: true, readOnly: true },//数据视图打开并允许编辑
              // restore: { show: true },//重新加载视图
              saveAsImage: { show: true,name: "电流" + formatDate(new Date)  },//保存为图片
              // dataZoom :{},
            },
          },
      xAxis: {
        type: 'category',nameLocation: 'end',axisLabel: { formatter: 
            function (value) {
              if(toggleTime.value == "oneHour"){
                // 截取字符串的前n位，即yyyy-MM-dd HH:mm:ss
                return value.substring(11, 19);
              } else if(toggleTime.value == "twentyfourHour" || toggleTime.value == "seventytwoHour"){
                // 截取字符串的n位，即yyyy-MM-dd HH:mm:ss
                return value.substring(5, 19);
              }
            }
          },
        boundaryGap: false,
        data:lineidDateTimes.value
      },
      yAxis: {
        type: 'value',
        axisLabel: {formatter: '{value}A'}
      },
      series: [
       {
          name: 'L1-电流',
          type: 'line',
          data: lChartData.value.curValueList,
          ...(lChartData.value?.curValueList?.length==1? {symbol: 'circle'}:{symbol:'none'}),
          symbolSize: 4
        },
        {
          name: 'L2-电流',
          type: 'line',
          data: llChartData.value.curValueList,
          ...(llChartData.value?.curValueList?.length==1? {symbol: 'circle'}:{symbol:'none'}),
          symbolSize: 4,
        },
        {
          name: 'L3-电流',
          type: 'line',
          data: lllChartData.value.curValueList,
          ...(lllChartData.value?.curValueList?.length==1? {symbol: 'circle'}:{symbol:'none'}),
          symbolSize: 4,
        }
      ]
    },true)
  }
  lineidBeforeChartUnmountV()
  await PDUHdaLineHisdataV(toggleTimeV.value)

  // 创建新的图表实例
  lineidChartV = echarts.init(document.getElementById('lineidChartContainerV'));
  // 设置新的配置对象
  if (lineidChartV) {
    lineidChartV.setOption({
      title: { text: ''},
      tooltip: { trigger: 'axis',      formatter: function (params) {
        let result = params[0].name + '<br>';
        params.forEach((param,i) => {
          result +=  params[i].marker + params[i].seriesName;
          result += ':' + buKongGe(params[i].value,1);
          if (param.seriesName === 'L1-电压' || param.seriesName === 'L2-电压' || param.seriesName === 'L3-电压') {
            result += 'V';
          } else  {
            result += ' A';
          }
          if(dataTime.value[`L${i+1}DataTime`].length && dataTime.value[`L${i+1}DataTime`][params[i].dataIndex] != '') {
            result += '&nbsp&nbsp发生时间:' + dataTime.value[`L${i+1}DataTime`][params[i].dataIndex]
          }
          result += '<br>';
      });
        return result.trimEnd(); // 去除末尾多余的换行符
      }},
      legend: {data: ['L1-电压', 'L2-电压', 'L3-电压']},
      grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
      toolbox: {
            show: true,//是否显示工具箱开关
            top: 'center',
            orient: 'vertical',
            feature: {
              // dataView: { show: true, readOnly: true },//数据视图打开并允许编辑
              // restore: { show: true },//重新加载视图
              saveAsImage: { show: true,name: "电压" + formatDate(new Date) },//保存为图片
              // dataZoom :{},
            },
          },
      xAxis: {
        type: 'category',nameLocation: 'end',axisLabel: { formatter: 
            function (value) {
              if(toggleTimeV.value == "oneHour"){
                // 截取字符串的前n位，即yyyy-MM-dd HH:mm:ss
                return value.substring(11, 19);
              } else if(toggleTimeV.value == "twentyfourHour" || toggleTimeV.value == "seventytwoHour"){
                // 截取字符串的n位，即yyyy-MM-dd HH:mm:ss
                return value.substring(5, 19);
              }
            }
          },
        boundaryGap: false,
        data:lineidDateTimes.value
      },
      yAxis: {
        type: 'value',
        axisLabel: {formatter: '{value}V'}
      },
      series: [
        {
          name: 'L1-电压',
          type: 'line',
          data: lChartDataV.value.volValueList,
          ...(lChartDataV.value?.volValueList?.length==1? {symbol: 'circle'}:{symbol:'none'}),
          symbolSize: 4
        },
        {
          name: 'L2-电压',
          type: 'line',
          data: llChartDataV.value.volValueList,
          ...(llChartDataV.value?.volValueList?.length==1? {symbol: 'circle'}:{symbol:'none'}),
          symbolSize: 4,
        },
        {
          name: 'L3-电压',
          type: 'line',
          data: lllChartDataV.value.volValueList,
          ...(lllChartDataV.value?.volValueList?.length==1? {symbol: 'circle'}:{symbol:'none'}),
          symbolSize: 4,
        }
      ]
    },true)
  }
}

/** 初始化 **/
onMounted(async () => {
  //debugger
  //1
  devKeyList.value = await loadAll();
  await window.addEventListener('resize', updateDimensions)
  if(!location2.value) {
    await getLocation()
  }
  // console.log(devKeyList.value)
  await lineidFlashChartData()
})

onUnmounted(async () => {
  await window.removeEventListener('resize', updateDimensions)
})

onBeforeMount(async () =>{
  await getTestData();
  await initChart();
  flashListTimer.value.tableDataTimer = setInterval((getTestData), 5000);
  flashListTimer.value.abcChartTimer = setInterval((setNewABCChartData), 5000);
  // flashListTimer.value.chartTimer = setInterval((setNewChartData), 60000);
})

onBeforeUnmount(()=>{
  if(flashListTimer.value.tableDataTimer && flashListTimer.value.abcChartTimer){
    clearInterval(flashListTimer.value.tableDataTimer)
    clearInterval(flashListTimer.value.chartTimer)
    clearInterval(flashListTimer.value.abcChartTimer)
    flashListTimer.value.tableDataTimer = null;
    flashListTimer.value.abcChartTimer = null;
    flashListTimer.value.chartTimer = null;
  }
})

onActivated( async () => {
  await getTestData();
  flashChartData();
  lineidFlashChartData();
  if(!firstTimerCreate.value){
    flashListTimer.value.tableDataTimer = setInterval(getTestData, 5000);
    flashListTimer.value.abcChartTimer = setInterval((setNewABCChartData), 5000);
    var time = 0;
    if(queryParams.powGranularity == "oneHour"){
      time = 60000;
    } else if(queryParams.powGranularity == "twentyfourHour"){
      time = 3600000;
    }else if(queryParams.powGranularity == "seventytwoHour"){
      time = 25920000;
    }
    // flashListTimer.value.chartTimer = setInterval((setNewChartData), time);
  }
})

onBeforeRouteLeave(()=>{
  if(flashListTimer.value.tableDataTimer && flashListTimer.value.abcChartTimer){
    clearInterval(flashListTimer.value.tableDataTimer)
    clearInterval(flashListTimer.value.chartTimer)
    clearInterval(flashListTimer.value.abcChartTimer)
    flashListTimer.value.tableDataTimer = null;
    flashListTimer.value.chartTimer = null;
    flashListTimer.value.abcChartTimer = null;
    firstTimerCreate.value = false;
  }
})

const route = useRoute();
const query = route.query;

// 将查询参数转换为适当的类型
const devKey = query.devKey as string;
// const id = parseInt(query.id as string, 10);
const location = query.location as string;
const location2 =  ref('');
location2.value = location;

queryParams.devKey = devKey;
// queryParams.id = id;
</script>

<style scoped lang="scss">
//   ::v-deep .el-card__body {
//     padding:12px;

// }
   ::v-deep .el-button--primary{
    --el-button-border-color:#00778c;
    --el-button-bg-color:#00778c;
    --el-button-hover-bg-color:#00778c;
   }
   ::v-deep .el-button:hover{
        --el-button-hover-text-color: #fff;
        --el-button-hover-border-color: #00778c;
        background-color: #00778c;
   }
   ::v-deep .el-select__wrapper.is-focused{
      --el-color-primary:#00778c;
   }
   ::v-deep .el-select-dropdown__item.is-selected{
    --el-color-primary: #00778c;
   }
.header_app{
  background-color: white;
  display: flex;
  align-items: center;
  height: 50px;
  padding-left: 10px;
  box-shadow: 20px;
}
.header_app_text{                     
  background-color: white;
  width: 100%;
  align-content: center;
  color:#606266;
}                                                       
.header_app_text_other{
  align-content: center;
  background-color: white;
  margin-right: 5px;
}
.header_app_text_other1{
  align-content: center;
  background-color: white;

}

@media screen and (max-width:1599px) {
  .adaptiveStyle {
    width: 90vw;
    height: 42vh;
  }
}

@media screen and  (max-width:2048px) and (min-width:1600px) {
  .adaptiveStyle {
    width: 85vw;
    height: 42vh;
  }
}

@media screen and (min-width:2049px) {
  .adaptiveStyle {
    width: 95vw;
    height: 42vh;
  }
}

</style>
