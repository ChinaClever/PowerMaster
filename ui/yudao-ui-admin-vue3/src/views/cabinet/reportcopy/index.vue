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

          <el-form-item label="机房Id" prop="ipAddr" >
            <el-input
              v-model="queryParams.Id"
              placeholder="请输入IP"
              clearable
              class="!w-140px"
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
              机柜基本信息
            </div>
            <!-- <el-row :gutter="24" >
              <el-col :span="24 - serChartContainerWidth">
                <div class="centered-div">
                  <el-table 
                    :data="PDUTableData" 
                    :header-cell-style="arraySpanMethod"
                    >
                    <el-table-column  align="center" label="基本信息"  prop="baseInfoName" />
                    <el-table-column  prop="baseInfoValue" >
                      <template #default="scope">
                        <span v-if="scope.$index === 2">
                          <el-tag  v-if="scope.row.baseInfoValue == 0">正常</el-tag>
                          <el-tag type="warning" v-if="scope.row.baseInfoValue == 1">预警</el-tag>
                          <el-popover
                              placement="top-start"
                              title="告警内容"
                              :width="500"
                              trigger="hover"
                              :content="scope.row.pduAlarm"
                              v-if="scope.row.baseInfoValue == 2"
                            >
                              <template #reference>
                                <el-tag type="danger">告警</el-tag>
                              </template>
                            </el-popover>
                          <el-tag type="info" v-if="scope.row.baseInfoValue == 4">故障</el-tag>
                          <el-tag type="info" v-if="scope.row.baseInfoValue == 5">离线</el-tag>
                        </span>
                        <span v-else>{{ scope.row.baseInfoValue }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column label="能耗" :rowspan="2" prop="consumeName"  />
                    <el-table-column  prop="consumeValue" />
                  </el-table>
                </div>
              </el-col>
              <el-col :span="serChartContainerWidth">
                <div class="right-div" ref="serChartContainer" id="serChartContainer" style="width: 29vw; height: 25vh;"></div>
              </el-col>
            </el-row> -->
          </div>
          <div class="pageBox" v-if="visControll.eqVis" >
            <div class="page-conTitle" >
              电量分布
            </div>
            <p v-if="!visControll.isSameDay">本周期内，共计使用电量{{eqData.totalEle}}kWh，最大用电量{{eqData.maxEle}}kWh， 最大负荷发生时间{{eqData.maxEleTime}}</p>
            <p v-if="visControll.isSameDay && eqData.eq">本周期内，开始时电能为{{eqData.firstEq}}kWh，结束时电能为{{eqData.lastEq}}kWh， 电能增长{{(eqData.lastEq - eqData.firstEq).toFixed(1)}}kWh</p>
            <div ref="rankChartContainer" id="rankChartContainer" style="width: 70vw; height: 58vh;"></div>
          </div>
          <div class="pageBox"  v-if="visControll.powVis">
            <div class="page-conTitle">
              总平均功率曲线
            </div>
            <p>本周期内，最大视在功率{{powData.apparentPowMaxValue}}kVA， 发生时间{{powData.apparentPowMaxTime}}。最小视在功率{{powData.apparentPowMinValue}}kVA， 发生时间{{powData.apparentPowMinTime}}</p>
            <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;最大有功功率{{powData.activePowMaxValue}}kVA， 发生时间{{powData.activePowMaxTime}}。最小有功功率{{powData.activePowMinValue}}kVA， 发生时间{{powData.activePowMinTime}}</p>
            <div ref="powChartContainer" id="powChartContainer" style="width: 70vw; height: 58vh;"></div>
          </div>
          <div class="pageBox"  v-if="visControll.ApowVis">
            <div class="page-conTitle">
              A路平均功率曲线
            </div>
            <p>本周期内，最大视在功率{{powData.AapparentPowMaxValue}}kVA， 发生时间{{powData.AapparentPowMaxTime}}。最小视在功率{{powData.AapparentPowMinValue}}kVA， 发生时间{{powData.AapparentPowMinTime}}</p>
            <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;最大有功功率{{powData.AactivePowMaxValue}}kVA， 发生时间{{powData.AactivePowMaxTime}}。最小有功功率{{powData.AactivePowMinValue}}kVA， 发生时间{{powData.AactivePowMinTime}}</p>
            <div ref="ApowChartContainer" id="ApowChartContainer" style="width: 70vw; height: 58vh;"></div>
          </div>
          <div class="pageBox"  v-if="visControll.BpowVis">
            <div class="page-conTitle">
              B路平均功率曲线
            </div>
            <p>本周期内，最大视在功率{{powData.BapparentPowMaxValue}}kVA， 发生时间{{powData.BapparentPowMaxTime}}。最小视在功率{{powData.BapparentPowMinValue}}kVA， 发生时间{{powData.BapparentPowMinTime}}</p>
            <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;最大有功功率{{powData.BactivePowMaxValue}}kVA， 发生时间{{powData.BactivePowMaxTime}}。最小有功功率{{powData.BactivePowMinValue}}kVA， 发生时间{{powData.BactivePowMinTime}}</p>
            <div ref="BpowChartContainer" id="BpowChartContainer" style="width: 70vw; height: 58vh;"></div>
          </div>
          <!-- <div class="pageBox" v-if="visControll.outletVis">
            <div class="page-conTitle" >
              输出位电量排名
            </div>
            <div ref="outputRankChartContainer" id="outputRankChartContainer" style="width: 70vw; height: 58vh;"></div>
          </div> -->
          <div class="pageBox" v-if="visControll.temVis">
            <div class="page-conTitle">
              温度曲线
            </div>
            <p v-show="temData.temMaxValue">本周期内，最高温度{{temData.temMaxValue}}°C， 最高温度发生时间{{temData.temMaxTime}}，由温度传感器{{temData.temMaxSensorId}}采集得到</p>
            <p v-show="temData.temMinValue">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;最低温度{{temData.temMinValue}}°C， 最高温度发生时间{{temData.temMinTime}}，由温度传感器{{temData.temMinSensorId}}采集得到</p>
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
import { IndexApi } from '@/api/cabinet/index'
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
  ApowVis :false,
  BpowVis : false
})
const serChartContainerWidth = ref(10)

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

const handleMonthPick = () => {

  if(queryParams.oldTime){
    var newTime = new Date(queryParams.oldTime);
    newTime.setMonth(newTime.getMonth() + 1);
    newTime.setDate(newTime.getDate() - 1);
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
  firstEq : number;
  lastEq : number;
}
const eqData = ref<EqData>({
  eq : [],
  time : [],
  totalEle : 0,
  maxEle : 0,
  maxEleTime : "",
  firstEq : 0,
  lastEq : 0,
})

interface PowData {
  apparentPowAvgValue: number[];
  activePowAvgValue: number[];
  AapparentPowAvgValue: number[];
  AactivePowAvgValue: number[];
  BapparentPowAvgValue: number[];
  BactivePowAvgValue: number[];
  time: string[];
  total_pow_apparent : number;
  apparentPowMaxValue : number;
  apparentPowMaxTime : string;
  apparentPowMinValue : number;
  apparentPowMinTime : string;
  activePowMaxValue : number;
  activePowMaxTime : string;
  activePowMinValue : number;
  activePowMinTime : string;
  AapparentPowMaxValue : number;
  AapparentPowMaxTime : string;
  AapparentPowMinValue : number;
  AapparentPowMinTime : string;
  AactivePowMaxValue : number;
  AactivePowMaxTime : string;
  AactivePowMinValue : number;
  AactivePowMinTime : string;
  BapparentPowMaxValue : number;
  BapparentPowMaxTime : string;
  BapparentPowMinValue : number;
  BapparentPowMinTime : string;
  BactivePowMaxValue : number;
  BactivePowMaxTime : string;
  BactivePowMinValue : number;
  BactivePowMinTime : string;
}
const powData = ref<PowData>({
  apparentPowAvgValue : [],
  activePowAvgValue: [],
  AapparentPowAvgValue : [],
  AactivePowAvgValue: [],
  BapparentPowAvgValue : [],
  BactivePowAvgValue: [],
  time:[],
  total_pow_apparent : 0,
  apparentPowMaxValue : 0,
  apparentPowMaxTime : "",
  apparentPowMinValue : 0,
  apparentPowMinTime : "",
  activePowMaxValue : 0,
  activePowMaxTime : "",
  activePowMinValue : 0,
  activePowMinTime : "",
  AapparentPowMaxValue : 0,
  AapparentPowMaxTime : "",
  AapparentPowMinValue : 0,
  AapparentPowMinTime : "",
  AactivePowMaxValue : 0,
  AactivePowMaxTime : "",
  AactivePowMinValue : 0,
  AactivePowMinTime : "",
  BapparentPowMaxValue : 0,
  BapparentPowMaxTime : "",
  BapparentPowMinValue : 0,
  BapparentPowMinTime : "",
  BactivePowMaxValue : 0,
  BactivePowMaxTime : "",
  BactivePowMinValue : 0,
  BactivePowMinTime : "",
})

interface TemData {
  temAvgValue: any;
  time: string[];
  temMaxValue : number;
  temMaxTime : string;
  temMaxSensorId : number;
  temMinValue : number;
  temMinTime : string;
  temMinSensorId : number;
}
const temData = ref<TemData>({
  temAvgValue : [],
  time : [],
  temMaxValue : 0,
  temMaxTime : "",
  temMaxSensorId : 0,
  temMinValue : 0,
  temMinTime : "",
  temMinSensorId : 0,
})

interface ServerData {
  nameAndMax: object[];
  value: number[];
}
const serverData = ref<ServerData>({
  nameAndMax : [
  ],
  value: []
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
  eqData.value = await IndexApi.getConsumeData(queryParams);
  if( eqData.value.eq && eqData.value.eq.length > 0){
    visControll.eqVis = true;
    eqData.value.eq.forEach((obj,index)=>{
      eqData.value.eq[index] = obj.toFixed(1);
    })
    eqData.value.maxEle = eqData.value.maxEle?.toFixed(1);
    eqData.value.totalEle = eqData.value.totalEle?.toFixed(1);
    if(eqData.value.firstEq){
      eqData.value.firstEq = eqData.value.firstEq?.toFixed(1);
    }
    if(eqData.value.lastEq){
      eqData.value.lastEq = eqData.value.lastEq?.toFixed(1);
    }
    
  } else{
    visControll.eqVis = false;
  }
  

  powData.value = await IndexApi.getPowData(queryParams);
  if(powData.value.activePowAvgValue && powData.value.activePowAvgValue.length > 0){
    
    powData.value.activePowAvgValue.forEach((obj,index)=>{
      powData.value.activePowAvgValue[index] = obj.toFixed(3);
    })

    powData.value.apparentPowAvgValue.forEach((obj,index)=>{
      powData.value.apparentPowAvgValue[index] = obj.toFixed(3);
    })

    powData.value.apparentPowMaxValue = powData.value.apparentPowMaxValue?.toFixed(3);
    powData.value.apparentPowMinValue =  powData.value.apparentPowMinValue?.toFixed(3);
    powData.value.activePowMaxValue = powData.value.activePowMaxValue?.toFixed(3);
    powData.value.activePowMinValue = powData.value.activePowMinValue?.toFixed(3);

    visControll.powVis = true;
  }else{
    visControll.powVis = false;
  }

  if(powData.value.AactivePowAvgValue && powData.value.AactivePowAvgValue.length > 0){
   
    powData.value.AactivePowAvgValue.forEach((obj,index)=>{
      powData.value.AactivePowAvgValue[index] = obj.toFixed(3);
    })

    powData.value.AapparentPowAvgValue.forEach((obj,index)=>{
      powData.value.AapparentPowAvgValue[index] = obj.toFixed(3);
    })

    powData.value.AapparentPowMaxValue = powData.value.AapparentPowMaxValue?.toFixed(3);
    powData.value.AapparentPowMinValue =  powData.value.AapparentPowMinValue?.toFixed(3);
    powData.value.AactivePowMaxValue = powData.value.AactivePowMaxValue?.toFixed(3);
    powData.value.AactivePowMinValue = powData.value.AactivePowMinValue?.toFixed(3);

    visControll.ApowVis = true;
  }else{
    visControll.ApowVis = false;
  }
  
  if(powData.value.BactivePowAvgValue && powData.value.BactivePowAvgValue.length > 0){
    
    powData.value.BactivePowAvgValue.forEach((obj,index)=>{
      powData.value.BactivePowAvgValue[index] = obj.toFixed(3);
    })

    powData.value.BapparentPowAvgValue.forEach((obj,index)=>{
      powData.value.BapparentPowAvgValue[index] = obj.toFixed(3);
    })

    powData.value.BapparentPowMaxValue = powData.value.BapparentPowMaxValue?.toFixed(3);
    powData.value.BapparentPowMinValue =  powData.value.BapparentPowMinValue?.toFixed(3);
    powData.value.BactivePowMaxValue = powData.value.BactivePowMaxValue?.toFixed(3);
    powData.value.BactivePowMinValue = powData.value.BactivePowMinValue?.toFixed(3);

    visControll.BpowVis = true;
  }else{
    visControll.BpowVis = false;
  }
  // outletRankData.value = await IndexApi.getOutLetData(queryParams);
  // if(outletRankData.value.eleValue && outletRankData.value.eleValue.length > 0){
  //   visControll.outletVis = true;
  //   outletRankData.value.eleValue.forEach((obj,index)=>{
  //     outletRankData.value.eleValue[index] = obj?.toFixed(1);
  //   })

  //   outletRankData.value.outLetId.forEach((obj,index)=>{
  //     outletRankData.value.outLetId[index] = "输出位" + obj;
  //   })
  // }else{
  //   visControll.outletVis = false;
  // }
  
  temData.value = await IndexApi.getTemData(queryParams);
  if(temData.value.temAvgValue && temData.value.temAvgValue[1] && temData.value.temAvgValue[1]?.length > 0){
    visControll.temVis = true;
    temData.value.temAvgValue?.forEach((obj,index) => {
      obj?.forEach((element,innerIndex) => {
        temData.value.temAvgValue[index][innerIndex] = element?.toFixed(2);
      });
    });
    
    temData.value.temMinValue = temData.value.temMinValue?.toFixed(2);
    temData.value.temMaxValue = temData.value.temMaxValue?.toFixed(2);
  }else{
    visControll.temVis = false;
  }

  // var PDU = await IndexApi.PDUDisplay(queryParams);
  // var temp = [] as any;
  
  // // 假设 PDU.pdu_data.output_item_list.pow_value 是一个 double 数组
  // var powValueArray = PDU.pdu_data?.output_item_list?.pow_value;
  // // 过滤出大于 0 的元素，并将值与下标保存到对象数组中
  // if(powValueArray && powValueArray.length > 0){
  //   var resultArray = [];
  //   for (var i = 0; i < powValueArray.length; i++) {
  //     if (powValueArray[i] > 0) {
  //       resultArray.push({
  //         value: powValueArray[i],
  //         index: i + 1
  //       });
  //     }
  //   }

  //   // 按值进行排序
  //   resultArray.sort(function(a, b) {
  //     return a.value - b.value;
  //   });

  //   // 只保留前十个元素
  //   resultArray = resultArray.slice(0, 10);

  //   // 根据 resultArray 中的元素生成 nameAndMax 数组和 value 数组
  //   var element = [];
  //   var valueArr = [];
  //   for (var j = 0; j < resultArray.length; j++) {
  //     var name = "输出位" + resultArray[j].index;
  //     element.push({
  //       name: name,
  //       max: resultArray[j].value + 0.001
  //     });
  //     valueArr.push(resultArray[j].value?.toFixed(3))
  //   }
  //   serverData.value.nameAndMax = element;
  //   serverData.value.value = valueArr;
  //   serChartContainerWidth.value = 10;
  // }else{
  //   serChartContainerWidth.value = 0;
  // }
  

  //   temp.push({
  //     baseInfoName : "所属位置",
  //     baseInfoValue : "",
  //     consumeName : "消耗电量",
  //     consumeValue : eqData.value.eq && eqData.value.eq.length > 0? visControll.isSameDay ? (eqData.value.lastEq - eqData.value.firstEq).toFixed(1) + "kWh" : eqData.value.totalEle + "kWh" : '/',
  //   })
  //   temp.push({
  //     baseInfoName : "网络地址",
  //     baseInfoValue : queryParams.ipAddr + "-" + queryParams.cascadeAddr,
  //     consumeName : "当前视在功率",
  //     consumeValue : PDU?.pdu_data?.pdu_total_data ? PDU.pdu_data.pdu_total_data.pow_apparent.toFixed(3) + "kVA" : '/'
  //   })
  //   temp.push({
  //     baseInfoName : "设备状态",
  //     baseInfoValue : PDU.status != null ? PDU.status : '/',
  //     pduAlarm : PDU.pdu_alarm,
  //     consumeName : "当前功率因素",
  //     consumeValue : PDU?.pdu_data?.pdu_total_data ? PDU.pdu_data.pdu_total_data.power_factor?.toFixed(2) : '/'
  //   })
  //   PDUTableData.value = temp;
  
  
  // initChart();
  loading.value = false

}

const rankChartContainer = ref<HTMLElement | null>(null);
let rankChart = null as echarts.ECharts | null; // 显式声明 rankChart 的类型
const powChartContainer = ref<HTMLElement | null>(null);
let powChart = null as echarts.ECharts | null; // 显式声明 powChart 的类型
const ApowChartContainer = ref<HTMLElement | null>(null);
let ApowChart = null as echarts.ECharts | null; // 显式声明 powChart 的类型
const BpowChartContainer = ref<HTMLElement | null>(null);
let BpowChart = null as echarts.ECharts | null; // 显式声明 powChart 的类型
const temChartContainer = ref<HTMLElement | null>(null);
let temChart = null as echarts.ECharts | null; // 显式声明 temChart 的类型
const serChartContainer = ref<HTMLElement | null>(null);
let serChart = null as echarts.ECharts | null; // 显式声明 serChart 的类型
const outputRankChartContainer = ref<HTMLElement | null>(null);
let outputRankChart = null as echarts.ECharts | null; // 显式声明 serChart 的类型

const initChart =  () => {
  if (rankChartContainer.value && instance && eqData.value.time && eqData.value.time.length > 0) {
    rankChart = echarts.init(rankChartContainer.value);

    rankChart.setOption({
      // 这里设置 Echarts 的配置项和数据
      dataZoom:[{ type:"inside"}],
      title: { text: ''},
      tooltip: { trigger: 'axis',formatter: function (params) {
                                              var dataIndex = params[0].dataIndex;
                                              var eleValue = eqData.value.eq[dataIndex];
                                              return  eqData.value.time[dataIndex] + "<br/>" + params[0].marker +  eleValue + " kWh"; // 自定义浮窗显示的内容
                                            },},
      toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
      xAxis: {type: 'category' ,data:eqData.value.time},
      yAxis: { type: 'value' , name : "kWh"},
      series: [
        { type: 'bar', data: eqData.value.eq, label: { show: true, position: 'top' }, barWidth: barWid.value,
          itemStyle: {
            emphasis: {
              barBorderRadius: 3,
            },
            //颜色样式部分
            normal: {
              barBorderRadius: 3,
              color: new echarts.graphic.LinearGradient(0, 1, 0, 0, [
                { offset: 0, color: "#3977E6" },
                { offset: 1, color: "#37BBF8" },
              ]),
            },
          },
        },
      ],
    });
    // 将 rankChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    instance.appContext.config.globalProperties.rankChart = rankChart;
  }
  // if (outputRankChartContainer.value && instance && outletRankData.value.outLetId  && outletRankData.value.outLetId.length > 0 ) {
  //   outputRankChart = echarts.init(outputRankChartContainer.value);
  //   outputRankChart.setOption({
  //     // 这里设置 Echarts 的配置项和数据
  //     dataZoom:[{ type:"inside"}],
  //     title: { text: ''},
  //     tooltip: { trigger: 'axis',formatter: function (params) {
  //                                             var dataIndex = params[0].dataIndex;
  //                                             var eleValue = outletRankData.value.eleValue[dataIndex];
  //                                             return  outletRankData.value.outLetId[dataIndex] + "<br/>"  + params[0].marker + eleValue + " kWh"; // 自定义浮窗显示的内容
  //                                           },},
  //     toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
  //     xAxis: { type: 'value' },
  //     yAxis: {type: 'category' ,data:outletRankData.value.outLetId},
  //     series: [
  //       { type: 'bar', data: outletRankData.value.eleValue, label: { show: true, position: 'right' }, barWidth: barWid.value, 
  //         itemStyle: {
  //           emphasis: {
  //             barBorderRadius: 7,
  //           },
  //           //颜色样式部分
  //           normal: {
  //             barBorderRadius: 7,
  //             color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
  //               { offset: 0, color: "#3977E6" },
  //               { offset: 1, color: "#37BBF8" },
  //             ]),
  //           },
  //         },
  //       },
  //     ],
  //   });
  //   // 将 outputRankChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
  //   instance.appContext.config.globalProperties.outputRankChart = outputRankChart;
  // }
  if (powChartContainer.value && instance && powData.value.time && powData.value.time.length > 0) {
    powChart = echarts.init(powChartContainer.value);
    powChart.setOption({
      // 这里设置 Echarts 的配置项和数据
      dataZoom:[{ type:"inside"}],
      title: { text: ''},
      tooltip: { trigger: 'axis', formatter: function(params) {
                                    var result = params[0].name + '<br>';
                                    for (var i = 0; i < params.length; i++) {
                                      result +=  params[i].marker + params[i].seriesName + ': &nbsp&nbsp&nbsp&nbsp' + params[i].value;
                                      if (params[i].seriesName === '总平均视在功率') {
                                        result += ' kVA'; 
                                      } else if (params[i].seriesName === '总平均有功功率') {
                                        result += ' kW';
                                      }
                                      result += '<br>';
                                    }
                                    return result;
                                  } },
      legend: { data: ['总平均视在功率','总平均有功功率']},
      grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
      toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
      xAxis: {type: 'category', boundaryGap: false, data:powData.value.time},
      yAxis: { type: 'value'},
      series: [
        {name: '总平均视在功率', type: 'line', symbol: 'circle', symbolSize: 4, data: powData.value.apparentPowAvgValue},
        {name: '总平均有功功率', type: 'line', symbol: 'circle', symbolSize: 4, data: powData.value.activePowAvgValue},
      ],

    });
    // 将 powChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    instance.appContext.config.globalProperties.powChart = powChart;
  }
  if (ApowChartContainer.value && instance && powData.value.time && powData.value.time.length > 0) {
    ApowChart = echarts.init(ApowChartContainer.value);
    ApowChart.setOption({
      // 这里设置 Echarts 的配置项和数据
      dataZoom:[{ type:"inside"}],
      title: { text: ''},
      tooltip: { trigger: 'axis', formatter: function(params) {
                                    var result = params[0].name + '<br>';
                                    for (var i = 0; i < params.length; i++) {
                                      result +=  params[i].marker + params[i].seriesName + ': &nbsp&nbsp&nbsp&nbsp' + params[i].value;
                                      if (params[i].seriesName === 'A平均视在功率') {
                                        result += ' kVA'; 
                                      } else if (params[i].seriesName === 'A平均有功功率') {
                                        result += ' kW';
                                      }
                                      result += '<br>';
                                    }
                                    return result;
                                  } },
      legend: { data: ['A平均视在功率','A平均有功功率']},
      grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
      toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
      xAxis: {type: 'category', boundaryGap: false, data:powData.value.time},
      yAxis: { type: 'value'},
      series: [
        {name: 'A平均视在功率', type: 'line', symbol: 'circle', symbolSize: 4, data: powData.value.AapparentPowAvgValue},
        {name: 'A平均有功功率', type: 'line', symbol: 'circle', symbolSize: 4, data: powData.value.AactivePowAvgValue},
      ],

    });
    // 将 ApowChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    instance.appContext.config.globalProperties.ApowChart = ApowChart;
  }
  if (BpowChartContainer.value && instance && powData.value.time && powData.value.time.length > 0) {
    BpowChart = echarts.init(BpowChartContainer.value);
    BpowChart.setOption({
      // 这里设置 Echarts 的配置项和数据
      dataZoom:[{ type:"inside"}],
      title: { text: ''},
      tooltip: { trigger: 'axis', formatter: function(params) {
                                    var result = params[0].name + '<br>';
                                    for (var i = 0; i < params.length; i++) {
                                      result +=  params[i].marker + params[i].seriesName + ': &nbsp&nbsp&nbsp&nbsp' + params[i].value;
                                      if (params[i].seriesName === 'B平均视在功率') {
                                        result += ' kVA'; 
                                      } else if (params[i].seriesName === 'B平均有功功率') {
                                        result += ' kW';
                                      }
                                      result += '<br>';
                                    }
                                    return result;
                                  } },
      legend: { data: ['B平均视在功率','B平均有功功率']},
      grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
      toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
      xAxis: {type: 'category', boundaryGap: false, data:powData.value.time},
      yAxis: { type: 'value'},
      series: [
        {name: 'B平均视在功率', type: 'line', symbol: 'circle', symbolSize: 4, data: powData.value.BapparentPowAvgValue},
        {name: 'B平均有功功率', type: 'line', symbol: 'circle', symbolSize: 4, data: powData.value.BactivePowAvgValue},
      ],

    });
    // 将 ApowChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    instance.appContext.config.globalProperties.BpowChart = BpowChart;
  }
  if (temChartContainer.value && instance && temData.value.temAvgValue && temData.value.temAvgValue.length > 0) {
    temChart = echarts.init(temChartContainer.value);
    var seriesArr = [] as any;
    var legendData = [] as any;
    temData.value.temAvgValue?.forEach((obj,index) => {
      if(index != 0 && temData.value.temAvgValue[index].length > 0){
        seriesArr.push({
          name: '温度传感器' + index,
          type: 'line',
          data: obj,
          symbol: 'circle', 
          symbolSize: 4,
        });
        legendData.push('温度传感器' + index)
      }
    });
    temChart.setOption({
      // 这里设置 Echarts 的配置项和数据
      dataZoom:[{ type:"inside"}],
      title: { text: ''},
      tooltip: { trigger: 'axis',formatter: function(params) {
                                    var result = params[0].name + '<br>';
                                    for (var i = 0; i < params.length; i++) {
                                      result +=  params[i].marker + params[i].seriesName + ': &nbsp&nbsp&nbsp&nbsp' + params[i].value;
                                      if (params[i].seriesName.includes('平均温度')) {
                                        result += '°C'; 
                                      } 
                                      result += '<br>';
                                    }
                                    return result;
                                  } },
      legend: { data: legendData},
      
      grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
      toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
      xAxis: {
          type: 'category', 
          boundaryGap: false, 
          data: temData.value.time[1]
        },
      yAxis: { type: 'value'},
      series: seriesArr ,
    });
    // 将 temChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    instance.appContext.config.globalProperties.temChart = temChart;
  }
  // if (serChartContainer.value && instance && serverData.value.nameAndMax && serverData.value.nameAndMax.length > 0) {
  //   serChart = echarts.init(serChartContainer.value);
  //   serChart.setOption({
  //     radar: { indicator: serverData.value.nameAndMax },
  //     series: [
  //       { name: '服务器IT总视在功率', type: 'radar', label: { show: true, position: 'top' } ,data: [ { value: serverData.value.value, name: '总视在功率' }, ] }
  //     ]
  //   });
  //   // 将 serChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
  //   instance.appContext.config.globalProperties.serChart = serChart;
  // }
  visControll.visAllReport = true;
};

// 在组件销毁时手动销毁图表
const beforeRankUnmount = () => {
    rankChart?.dispose(); // 销毁图表实例
};

const beforeOutPutRankUnmount = () => {
    outputRankChart?.dispose(); // 销毁图表实例
};

const beforePowUnmount = () => {
    powChart?.dispose();  // 销毁图表实例
};

const beforeAPowUnmount = () => {
    ApowChart?.dispose();  // 销毁图表实例
};

const beforeBPowUnmount = () => {
    BpowChart?.dispose();  // 销毁图表实例
};

const beforeTemUnmount = () => {
    temChart?.dispose(); // 销毁图表实例
};

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

  if(queryParams.Id){
    console.log(1)
    if(queryParams.oldTime && queryParams.newTime){
      await getList();
      beforeRankUnmount();
      beforeOutPutRankUnmount();
      beforePowUnmount();
      beforeAPowUnmount();
      beforeBPowUnmount();
      beforeTemUnmount();
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
<style>
.centered-div {
  display: flex;
  justify-content: center; /* 水平居中 */
  align-items: center; /* 垂直居中 */
  height: 100%; /* 使用父容器的高度 */
}
.right-div{
  display: flex;
  justify-content: right; /* 水平居右 */
  align-items: center; /* 垂直居中 */
  height: 100%; /* 使用父容器的高度 */
}
</style>
