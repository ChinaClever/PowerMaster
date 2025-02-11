<template>
  <CommonMenu :dataList="navList" @node-click="handleClick"  navTitle="PDU报表" :showCheckbox="false" >
    <template #NavInfo>
        <br/>
      <div class="nav_data">
         <div class="nav_header">      
          <span v-if="nowAddress">{{nowAddress}}</span>
          <span v-if="nowLocation">( {{nowLocation}} ) </span>
        </div>

      </div>
    </template>
    <template #ActionBar>
      <el-form
        class="-mb-15px"
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        label-width="120px"
      >      
        <el-form-item  label="IP地址" prop="ipAddr" >
          <el-autocomplete
            v-model="queryParams.ipAddr"
            :fetch-suggestions="querySearch"
            clearable
            class="!w-140px"
            placeholder="请输入IP地址"
            @keyup.enter="handleQuery"
            @select="handleQuery"
          />
        </el-form-item>
        <el-form-item label="级联地址" prop="cascadeAddr" label-width="70px">
          <el-input-number
            v-model="queryParams.cascadeAddr"
            :min="0"
            controls-position="right"
            :value-on-clear="0"
              class="!w-100px"
          />
        </el-form-item>
        <el-form-item label="时间段" prop="createTime" label-width="100px">
          <el-button 
            @click="queryParams.timeType = 0;dateTimeName='twentyfourHour';now = new Date();now.setHours(0,0,0,0);queryParams.oldTime = getFullTimeByDate(now);queryParams.newTime = null;queryParams.timeArr = null;visControll.visAllReport = false;switchValue = 0;handleDayPick();handleQuery()" 
            :type="switchValue == 0 ? 'primary' : ''"
          >
            日报 
          </el-button>
          <el-button 
            @click="queryParams.timeType = 1;dateTimeName='seventytwoHour';now = new Date();now.setDate(1);now.setHours(0,0,0,0);queryParams.oldTime = getFullTimeByDate(now);queryParams.newTime = null;queryParams.timeArr = null;visControll.visAllReport = false;switchValue = 1;handleMonthPick();handleQuery()" 
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
    </template>
    <template #Content>
      <div v-show="visControll.visAllReport" class="page" >
        <div class="page-con">
          <div class="pageBox" >
            <div class="page-conTitle">
              PDU基本信息
            </div>
            <el-row :gutter="24" >
              <el-col :span="24 - serChartContainerWidth">
                <div class="centered-div">
                  <el-table 
                    :data="PDUTableData" 
                    :header-cell-style="arraySpanMethod"
                    >
                    <el-table-column  align="center" label="基本信息" >
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
                            <el-tag type="info" v-if="scope.row.baseInfoValue == '/' || scope.row.baseInfoValue == 5">离线</el-tag>
                          </span>
                          <span v-else>{{ scope.row.baseInfoValue }}</span>
                        </template>
                      </el-table-column>
                    </el-table-column>
                    <el-table-column  align="center" label="能耗">
                      <el-table-column  prop="consumeName"  />
                      <el-table-column  prop="consumeValue" />
                    </el-table-column>  
                    
                  </el-table>
                </div>
              </el-col>
              <el-col v-if="serChartContainerWidth == 10" :span="serChartContainerWidth">  
                
                <!-- <Radar width="29vw" height="25vh" :list="serverData" /> -->
                <div >
                 <div ref="serChartContainer" id="serChartContainer" style="width: 60vh; height: 25vh"></div>
               </div>
              </el-col>

            </el-row>

          </div>
          <div class="pageBox" v-if="visControll.eqVis" >
            <div class="page-conTitle" >
              电量分布
            </div>
            <p v-if="!visControll.isSameDay">本周期内，共计使用电量{{eqData.totalEle}}kWh，最大用电量{{eqData.maxEle}}kWh， 最大负荷发生时间{{eqData.maxEleTime}}</p>
            <p v-if="visControll.isSameDay">本周期内，开始时电能为{{eqData.firstEq}}kWh，结束时电能为{{eqData.lastEq}}kWh， 电能增长{{(eqData.lastEq - eqData.firstEq).toFixed(1)}}kWh</p>
            <Bar class="Container" :width="computedWidth" height="58vh" :list="eleList"/>
          </div>
          <div class="pageBox" v-if="visControll.flag">
            <div class="page-conTitle">
              相电流历史曲线趋势图
            </div>
            <!--<div ref="lineidChartContainer" id="lineidChartContainer" class="adaptiveStyle"></div>-->
            <CurLine class="adaptiveStyle" :list="curVolData"/>
          </div>
          <div class="pageBox" v-if="visControll.flag">
            <div class="page-conTitle">
              相电压历史曲线趋势图
            </div>
            <!--<div ref="lineidChartContainerOne" id="lineidChartContainerOne" class="adaptiveStyle"></div>-->
            <VolLine class="adaptiveStyle" :list="curVolData"/>
          </div>
          <div class="pageBox"  v-if="visControll.pfVis">
            <div class="page-conTitle">
              功率因素曲线
            </div>        
            <PFLine class="Container" :width="computedWidth" height="58vh" :list="pfLineList"/>
          </div>
          <div class="pageBox"  v-if="visControll.powVis">
            <div class="page-conTitle">
              平均功率曲线
            </div>
            <p>本周期内，最大视在功率{{powData.apparentPowMaxValue}}kVA， 发生时间{{powData.apparentPowMaxTime}}。最小视在功率{{powData.apparentPowMinValue}}kVA， 发生时间{{powData.apparentPowMinTime}}</p>
            <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;最大有功功率{{powData.activePowMaxValue}}kVA， 发生时间{{powData.activePowMaxTime}}。最小有功功率{{powData.activePowMinValue}}kVA， 发生时间{{powData.activePowMinTime}}</p>
            <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;最大无功功率{{powData.reactivePowMaxValue}}kVA， 发生时间{{powData.reactivePowMaxTime}}。最小无功功率{{powData.reactivePowMinValue}}kVA， 发生时间{{powData.reactivePowMinTime}}</p>
            <Line class="Container" :width="computedWidth" height="58vh" :list="totalLineList"/>
          </div>
          <div class="pageBox" v-if="visControll.volVis">
            <div class="page-conTitle" >
              电压曲线
            </div>
            <vol width="70vw" height="58vh" :list="volList" />
          </div>   
          <div class="pageBox" v-if="visControll.curVis">
            <div class="page-conTitle" >
              电流曲线
            </div>
            <cur width="70vw" height="58vh" :list="curList" />
          </div>   
          <div class="pageBox" v-if="visControll.outletVis">
            <div class="page-conTitle" >
              输出位电量排名
            </div>
            <HorizontalBar :width="computedWidth" height="58vh" :list="outletList" />
          </div>       
          <div class="pageBox" v-if="visControll.temVis">
            <div class="page-conTitle">
              温度曲线
            </div>
            <p v-show="temData.temMaxValue">本周期内，最高温度{{temData.temMaxValue}}°C， 最高温度发生时间{{temData.temMaxTime}}，由温度传感器{{temData.temMaxSensorId}}采集得到</p>
            <p v-show="temData.temMinValue">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;最低温度{{temData.temMinValue}}°C， 最低温度发生时间{{temData.temMinTime}}，由温度传感器{{temData.temMinSensorId}}采集得到</p>
            <EnvTemLine :width="computedWidth" height="58vh" :list="temList"  />
          </div>


          
          <div class="pageBox"  v-if="temp1 && temp1.length > 0">
            <div class="page-conTitle">
              告警信息
            </div>
                <el-table
                  ref="multipleTableRef"
                  :data="temp1"
                  highlight-current-row
                  style="width: 100%"
                  :stripe="true" 
                  :border="true"
                  @current-change="handleCurrentChange"
                >
                    <!-- <el-table-column type="selection" width="55" /> -->
                    <el-table-column type="index" width="80" label="序号" align="center" />
                    <el-table-column property="devPosition" label="区域" min-width="100" align="center" />
                    <el-table-column property="devName" label="设备" min-width="100" align="center" />
                    <el-table-column property="alarmLevelDesc" label="告警等级" min-width="100" align="center" />
                    <el-table-column property="alarmTypeDesc" label="告警类型" min-width="100" align="center" />
                    <el-table-column property="alarmDesc" label="描述" min-width="120" align="center">
                      <template #default="scope">
                        <el-tooltip  placement="right">
                          <div class="table-desc">{{scope.row.alarmDesc}}</div>
                          <template #content>
                            <div class="tooltip-width">{{scope.row.alarmDesc}}</div>
                          </template>
                        </el-tooltip>
                      </template>
                    </el-table-column>
                    <el-table-column property="startTime" label="开始时间" min-width="100" align="center" />
                    <el-table-column property="endTime" label="结束时间" min-width="100" align="center" />
                    <el-table-column property="finishReason" label="结束原因" min-width="100" align="center" />
                    <el-table-column property="confirmReason" label="确认原因" min-width="100" align="center" />
                </el-table>
          </div>
          
        </div>

      </div>
    </template>
  </CommonMenu>

  <!-- 表单弹窗：添加/修改 -->
  <PDUDeviceForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
// import download from '@/utils/download'
import { EnergyConsumptionApi } from '@/api/pdu/energyConsumption'
import { AlarmApi } from '@/api/system/notify/alarm'
import { PDUDeviceApi } from '@/api/pdu/pdudevice'
import * as echarts from 'echarts';
import { ElTree } from 'element-plus'
import { CabinetApi } from '@/api/cabinet/info'
import type Node from 'element-plus/es/components/tree/src/model/node'
import Line from './component/Line.vue'
import PFLine from './component/PFLine.vue'
import vol from './component/vol.vue'
import cur from './component/cur.vue'
import Bar from './component/Bar.vue'
import CurLine from './component/curLine.vue'
import VolLine from './component/volLine.vue'
import HorizontalBar from './component/HorizontalBar.vue'
import EnvTemLine from './component/EnvTemLine.vue'
import Radar from './component/Radar.vue'
import { Flag } from '@element-plus/icons-vue/dist/types';
import { size } from 'min-dash';
import { viewDepthKey } from 'vue-router';
import { Bottom } from '@element-plus/icons-vue/dist/types';
//import { ElMessageBox, ElMessage } from 'element-plus'
import { remove } from 'nprogress';

// 创建一个响应式引用来存储窗口宽度
const windowWidth = ref(window.innerWidth);
 
// 计算属性，根据窗口宽度返回不同的width值
const computedWidth = computed(() => {
  if (windowWidth.value >= 2400) {
    return '90vw';
  } else if (windowWidth.value >= 1600) {
    return '70vw';
  } else {
    return '80vw';
  }
});

// 监听窗口尺寸变化并更新windowWidth的值
const updateWindowWidth = () => {
  windowWidth.value = window.innerWidth;
};
// import Line from './component/Line.vue'
// import PFLine from './component/PFLine.vue'
// import Bar from './component/Bar.vue'
// import EnvTemLine from './component/EnvTemLine.vue'

/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })

//let lineidChart = null as echarts.ECharts | null; // 显式声明 rankChart 的类型
//const lineidChartContainer = ref<HTMLElement | null>(null);
//let lineidChartOne = null as echarts.ECharts | null; // 显式声明 rankChart 的类型
//const lineidChartContainerOne = ref<HTMLElement | null>(null);
const dateTimeName = ref('seventytwoHour');
const curVolData = ref();

const navList = ref([]) as any // 左侧导航栏树结构列表
const outletList = ref() as any;
const volList = ref() as any;
const curList = ref() as any;
const temList = ref() as any;
const eleList = ref() as any;
const totalLineList = ref() as any;
const pfLineList = ref() as any;
const nowAddressTemp = ref('')// 暂时存储点击导航栏的位置信息 确认有数据再显示
const nowLocationTemp = ref('')// 暂时存储点击导航栏的位置信息 确认有数据再显示
const now = ref()
const switchValue = ref(1);
const ipList = ref([])// 初始化 ipList 为一个空数组
const instance = getCurrentInstance();
const visControll = reactive({
  visAllReport : false,
  isSameDay : false,
  eqVis : false,
  powVis : false,
  volVis : false,
  curVis : false,
  outletVis : false,
  temVis : false,
  pfVis: false,
  flag: false,
})
const serChartContainerWidth = ref(0)

const loadAll = async () => {
  var data = await PDUDeviceApi.ipList();
  var objectArray = data.map((str) => {
    return { value: str };
  });
  return objectArray;
}

const querySearch = (queryString: string, cb: any) => {
  
  const results = queryString
    ? ipList.value.filter(createFilter(queryString))
    : ipList.value
  // call callback function to return suggestions
  cb(results)
}

const createFilter = (queryString: string) => {
  return (ipList) => {
    return (
      ipList.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0
    )
  }
}

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

    if(areDatesEqual(new Date(queryParams.timeArr[0]),new Date(queryParams.timeArr[1]))){
      visControll.isSameDay = true;
    }else{
      visControll.isSameDay = false;
    }
    queryParams.oldTime = queryParams.timeArr[0];
    queryParams.newTime = queryParams.timeArr[1].split(" ")[0]+ " " + "23:59:59";
    
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
  a: 0,
  outletId: undefined as number | undefined,
  pduId: undefined as number | undefined,
  devKey : "",
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


const loadNode = async (node: Node, resolve: (data: Tree[]) => void) => {
  if (node.level === 0) {
    var temp = await CabinetApi.getRoomList({});
    return resolve(temp)
  }
  if (node.level === 1){
    var temp = await CabinetApi.getRoomPDUList({id : node.data.id});
    return resolve(temp[0].children);
  } 
  if (node.level >= 2){
    return resolve(node?.data?.children);
  } 
}

// 导航栏选择后触发
const handleClick = async (row) => {
   if(row.type != null  && row.type == 4){
    queryParams.pduId = undefined
    queryParams.ipAddr = row.ip
    queryParams.cascadeAddr = row?.unique?.split("-")[1];
    findFullName(navList.value, row.unique, fullName => {
      nowAddressTemp.value = fullName
      nowLocationTemp.value = row.unique
    });
    handleQuery();
  }
}

// 得到位置全名
function findFullName(data, targetUnique, callback, fullName = '') {
  for (let item of data) {
    const newFullName = fullName === '' ? item.name : fullName + '-' + item.name;
    if (item.unique === targetUnique) {
      callback(newFullName);
    }
    if (item.children && item.children.length > 0) {
      findFullName(item.children, targetUnique, callback, newFullName);
    }
  }
}

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

const lineidDateTimes = ref([] as string[])

//const lineidBeforeChartUnmount = () => {
//lineidChart?.dispose() // 销毁图表实例
//}

const filterTimesFromDate = (dateTimeStrings, targetDate) => {
  const targetDateObj = new Date(targetDate);
  const targetDateString = targetDateObj.toISOString().split('T')[0]; // YYYY-MM-DD
  
  return dateTimeStrings.filter(dateTimeString => {
    const [datePart, _] = dateTimeString.split(' ');
    return datePart >= targetDateString;
  });
};
 
// 获取当前日期（或者您可以指定任意日期）
const currentDate = new Date().toISOString().split('T')[0];
//获取PDU相历史数据，处理L1,L2,L3的数据
const PDUHdaLineHisdata = async () => {
  const result = await PDUDeviceApi.getPDUHdaLineHisdata({ devKey : queryParams.devKey, type: dateTimeName.value})
  curVolData.value = result;


  
  if(curVolData.value?.dateTimes != null && curVolData.value?.dateTimes?.length > 0){
    visControll.flag = true;
  }else{
    visControll.flag = false;
  }
  //console.log('dateTimes',result.dateTimes)
  console.log('result111111111111',result);
  //{ devKey : queryParams.devKey, type : newPowGranularity} '192.168.1.184-0'

  //const lData = result.l
  //const llData = result.ll
  //const lllData = result.lll
//
  //lData.forEach(item => {
  //  lChartData.value.volValueList.push(item.vol_avg_value.toFixed(1))
  //  lChartData.value.curValueList.push(item.cur_avg_value.toFixed(2))
  //})
//
  //llData.forEach(item => {
  //  llChartData.value.volValueList.push(item.vol_avg_value.toFixed(1))
  //  llChartData.value.curValueList.push(item.cur_avg_value.toFixed(2))
  //})
//
  //lllData.forEach(item => {
  //  lllChartData.value.volValueList.push(item.vol_avg_value.toFixed(1))
  //  lllChartData.value.curValueList.push(item.cur_avg_value.toFixed(2))
  //})

  if(result?.dateTimes != null && result?.dateTimes?.length > 0){
    visControll.flag = true;
  }else{
    visControll.flag = false;
  }

  if (dateTimeName.value === 'twentyfourHour') {
    lineidDateTimes.value = filterTimesFromDate(result.dateTimes, currentDate).map(item => item.substring(11, 16))
  } else if (dateTimeName.value === 'seventytwoHour') {
    lineidDateTimes.value = result.dateTimes.map(item => item.substring(0, 10))
  }

  console.log('lineidDateTimes111111111111111')
}


//const lineidFlashChartData = async () =>{
//  lineidBeforeChartUnmount()
//
//  await PDUHdaLineHisdata()
//
//  // 创建新的图表实例
//  lineidChart = echarts.init(document.getElementById('lineidChartContainer'));
//  // 设置新的配置对象
//  if (lineidChart) {
//    lineidChart.setOption({
//      title: { text: ''},
//      tooltip: { trigger: 'axis',      formatter: function (params) {
//        let result = params[0].name + '<br>';
//        params.forEach(param => {
//          result += param.marker + param.seriesName + ': &nbsp;&nbsp;&nbsp;&nbsp' + param.value;
//          if (param.seriesName === 'L1-电流' || param.seriesName === 'L2-电流' || param.seriesName === 'L3-电流') {
//            result += 'A';
//          }
//          result += '<br>';
//        });
//        return result.trimEnd(); // 去除末尾多余的换行符
//      }},
//      legend: {
//        data: ['L1-电流', 'L2-电流', 'L3-电流'], // 图例项
//        selectedMode: 'multiple'
//      },
//      grid: {left: '3%', right: '4%', bottom: '5%',containLabel: true},
//      toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
//      xAxis: {
//        type: 'category',nameLocation: 'end',
//        boundaryGap: false,
//        data:lineidDateTimes.value
//      },
//      yAxis: {
//        type: 'value'
//      },
//      series: [
//        {
//          name: 'L1-电流',
//          type: 'line',
//          data: lChartData.value.curValueList,
//          symbol: 'circle',
//          symbolSize: 4
//        },
//        {
//          name: 'L2-电流',
//          type: 'line',
//          data: llChartData.value.curValueList,
//          symbol: 'circle',
//          symbolSize: 4,
//        },
//        {
//          name: 'L3-电流',
//          type: 'line',
//          data: lllChartData.value.curValueList,
//          symbol: 'circle',
//          symbolSize: 4,
//        }
//      ]
//    })
//  }
//
//  lineidChartOne = echarts.init(document.getElementById('lineidChartContainerOne'));
//  // 设置新的配置对象
//  if (lineidChartOne) {
//    lineidChartOne.setOption({
//      title: { text: ''},
//      tooltip: { trigger: 'axis',      formatter: function (params) {
//        let result = params[0].name + '<br>';
//        params.forEach(param => {
//          result += param.marker + param.seriesName + ': &nbsp;&nbsp;&nbsp;&nbsp' + param.value;
//          if (param.seriesName === 'L1-电压' || param.seriesName === 'L2-电压' || param.seriesName === 'L3-电压') {
//            result += 'V';
//          } 
//          result += '<br>';
//        });
//        return result.trimEnd(); // 去除末尾多余的换行符
//      }},
//      legend: {
//        data: ['L1-电压', 'L2-电压', 'L3-电压'], // 图例项
//        selectedMode: 'multiple'
//      },
//      grid: {left: '3%', right: '4%', bottom: '5%',containLabel: true},
//      toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
//      xAxis: {
//        type: 'category',nameLocation: 'end',
//        boundaryGap: false,
//        data:lineidDateTimes.value
//      },
//      yAxis: {
//        type: 'value'
//      },
//      series: [
//        {
//          name: 'L1-电压',
//          type: 'line',
//          data: lChartData.value.volValueList,
//          symbol: 'circle',
//          symbolSize: 4
//        },
//        {
//          name: 'L2-电压',
//          type: 'line',
//          data: llChartData.value.volValueList,
//          symbol: 'circle',
//          symbolSize: 4,
//        },
//        {
//          name: 'L3-电压',
//          type: 'line',
//          data: lllChartData.value.volValueList,
//          symbol: 'circle',
//          symbolSize: 4,
//        }
//      ]
//    })
//  }
//}

// 接口获取机房导航列表
const getNavList = async() => {
  let arr = [] as any
  var temp = await CabinetApi.getRoomPDUList()
  arr = arr.concat(temp);
  navList.value = arr
  await PDUHdaLineHisdata()
}

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
}) as any

interface PowData {
  apparentPowAvgValue: number[];
  activePowAvgValue: number[];
  reactivePowAvgValue: number[];
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
  reactivePowMaxValue : number;
  reactivePowMaxTime : string;
  reactivePowMinValue : number;
  reactivePowMinTime : string;
}
const powData = ref<PowData>({
  apparentPowAvgValue : [],
  activePowAvgValue: [],
  reactivePowAvgValue: [],
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
  reactivePowMaxValue : 0,
  reactivePowMaxTime : "",
  reactivePowMinValue : 0,
  reactivePowMinTime : ""
}) as any

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
interface RadarData{
  index: number;
  powApparent: number;
  powValue : number;
  curValue : number;
  name: string;
}
const result=ref<RadarData>({
  index : 0,
  powApparent : 0.000,
  powValue : 0.000,
  curValue : 0.00,
  name: ""
})
const temData = ref<TemData>({
  temAvgValue : [],
  time : [],
  temMaxValue : 0,
  temMaxTime : "",
  temMaxSensorId : 0,
  temMinValue : 0,
  temMinTime : "",
  temMinSensorId : 0,
}) as any


interface ServerData {
  nameAndMax: object[];
  curvalue: number[];
  powvalue: number[];
  powapparent: number[];
}
// 获得雷达视在功率数据
const serverData = ref<ServerData>({
  nameAndMax : [],
  curvalue: [],
  powvalue: [],
  powapparent: []
}) as any
//切换功率时调整nameAndMax
const serverData1 = ref<ServerData>({
  nameAndMax : [],
  curvalue: [],
  powvalue: [],
  powapparent: []
}) as any
interface OutLetRankData {
  outLetId: string[];
  eleValue: number[];
}

const outletRankData = ref<OutLetRankData>({
  outLetId : [],
  eleValue : [],
}) as any

//树型控件
interface Tree {
  [key: string]: any
}

const filterText = ref('')
const treeRef = ref<InstanceType<typeof ElTree>>()
const itemStyle = ref({  
  emphasis: {  
    barBorderRadius: 3  
  },  
  normal: {  
    barBorderRadius: 3,  
    color: new echarts.graphic.LinearGradient(  
      0, 1, 0, 0, [  
        { offset: 0, color: '#3977E6' },  
        { offset: 1, color: '#37BBF8' }  
      ]  
    )  
  }  
});
// 截取前10个元素
const truncateArrays = (data: ServerData): ServerData => {
  return {
    nameAndMax: data.nameAndMax.slice(0, 12),
    curvalue: data.curvalue.slice(0, 12),
    powvalue: data.powvalue.slice(0, 12),
    powapparent: data.powapparent.slice(0, 12)
  };
};
const outletItemStyle = ref({
  emphasis: {
    barBorderRadius: 7,
  },
  //颜色样式部分
  normal: {
    barBorderRadius: 7,
    color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
      { offset: 0, color: "#3977E6" },
      { offset: 1, color: "#37BBF8" },
    ]),
  },
});
const temp1 = ref([]) as any
const getList = async () => {
  await PDUHdaLineHisdata();
  
  loading.value = true
  eqData.value = await PDUDeviceApi.getConsumeData(queryParams);
  
  if(eqData.value?.barRes?.series[0]){
    eqData.value.barRes.series[0].itemStyle = itemStyle.value;
  }
  eleList.value = eqData.value.barRes;
  if( eleList.value?.time != null && eleList.value?.time?.length > 0){
    eqData.value.maxEle = eqData.value.maxEle?.toFixed(1);
    eqData.value.totalEle = eqData.value.totalEle?.toFixed(1);
    if(eqData.value.firstEq){
      eqData.value.firstEq = eqData.value.firstEq?.toFixed(1);
    }
    if(eqData.value.lastEq){
      eqData.value.lastEq = eqData.value.lastEq?.toFixed(1);
    }    
    visControll.eqVis = true;
  } else{
    visControll.eqVis = false;
  }
  
  const data = await PDUDeviceApi.getPDUPFLine(queryParams);
  console.log('getPDUPFLine',data);
  pfLineList.value = data.pfLineRes;
  console.log("pfLineList.value",pfLineList.value);
  if(pfLineList.value?.time != null && pfLineList.value?.time?.length > 0){
    visControll.pfVis = true;
  }else {
    visControll.pfVis = false;
  }

  powData.value = await PDUDeviceApi.getPowData(queryParams);
  totalLineList.value = powData.value.totalLineRes;
  if(totalLineList.value?.time != null && totalLineList.value?.time?.length > 0){
    powData.value.apparentPowMaxValue = powData.value.apparentPowMaxValue?.toFixed(3);
    powData.value.apparentPowMinValue =  powData.value.apparentPowMinValue?.toFixed(3);
    powData.value.activePowMaxValue = powData.value.activePowMaxValue?.toFixed(3);
    powData.value.activePowMinValue = powData.value.activePowMinValue?.toFixed(3);
    powData.value.reactivePowMaxValue = powData.value.reactivePowMaxValue?.toFixed(3);
    powData.value.reactivePowMinValue = powData.value.reactivePowMinValue?.toFixed(3);
    visControll.powVis = true;
  }else{
    visControll.powVis = false;
  }
  
  outletRankData.value = await PDUDeviceApi.getOutLetData(queryParams);
  if(outletRankData.value?.barRes?.series[0]){
    outletRankData.value.barRes.series[0].itemStyle = outletItemStyle.value;
  }
  outletList.value = outletRankData.value.barRes;
  if(outletList.value?.time != null  && outletList.value?.time?.length > 0){
    visControll.outletVis = true;
  }else{
    visControll.outletVis = false;
  }
  
  temData.value = await PDUDeviceApi.getTemData(queryParams);
  temList.value = temData.value.lineRes;
  if(temList.value?.series != null && temList.value?.series?.length > 0 ){
    temData.value.temMinValue = temData.value.temMinValue?.toFixed(2);
    temData.value.temMaxValue = temData.value.temMaxValue?.toFixed(2);
    visControll.temVis = true;
  }else{
    visControll.temVis = false;
  }

  var PDU = await PDUDeviceApi.PDUDisplay(queryParams);
  PDU = JSON.parse(PDU)
  var temp = [] as any;
  var resultArray=[] as any;
  var baseInfo = await PDUDeviceApi.getPDUDevicePage(queryParams);
  // 假设 PDU.pdu_data.output_item_list.pow_value 是一个 double 数组
  var powApparentValueArray = PDU?.pdu_data?.output_item_list?.pow_apparent;
  var powValueArray = PDU?.pdu_data?.output_item_list?.pow_value;
  var curValueArray = PDU?.pdu_data?.output_item_list?.cur_value;

  // console.log(powValueArray)
  // 将值与下标保存到对象数组中
  if(powValueArray && powValueArray.length >= 0){
    for (var i = 0; i < powValueArray.length; i++) {
      result.value.index=i + 1
      result.value.name = "输出位" + (i + 1)
      result.value.powApparent = powApparentValueArray[i]
      result.value.curValue = curValueArray[i]
      result.value.powValue= powValueArray[i]
      resultArray.push({ ...result.value });
    }
    // 按值进行排序
    resultArray.sort((a, b) => b.curValue - a.curValue);
    // 只保留前十二个元素
    resultArray = resultArray.slice(0, 12);
    console.log("resultArray",resultArray)
    for(var i=0;i<resultArray.length;i++){
      serverData.value.nameAndMax.push({
        name: resultArray[i].name,
        max: resultArray[i].curValue+0.001,
      })
      serverData1.value.nameAndMax.push({
        name: resultArray[i].name,
        max: resultArray[i].powApparent+0.001,
      })
      // console.log(serverData1.value.nameAndMax)
      serverData.value.curvalue.push(resultArray[i].curValue.toFixed(2))
      serverData.value.powvalue.push(resultArray[i].powValue)
      serverData.value.powapparent.push(resultArray[i].powApparent)
    }
    // 根据 resultArray 中的元素生成 nameAndMax 数组和 value 数组
    serChartContainerWidth.value = 10;
    // console.log(" serChartContainerWidth.value", serChartContainerWidth.value)
    if(resultArray.length==0){
      serChartContainerWidth.value = 0;
    }
  }else{
    serChartContainerWidth.value = 0;
  }
  temp.push({
    baseInfoName : "所属位置",
    baseInfoValue : baseInfo?.list && baseInfo?.list.length > 0 ? baseInfo?.list[0].location : "/",
    consumeName : "消耗电量",
    consumeValue : eqData.value?.barRes?.series && eqData.value?.barRes?.series.length > 0? visControll.isSameDay ? (eqData.value.lastEq - eqData.value.firstEq).toFixed(1) + "kWh" : eqData.value.totalEle + "kWh" : '/',
  })
  temp.push({
    baseInfoName : "网络地址",
    baseInfoValue : queryParams.ipAddr + "-" + queryParams.cascadeAddr,
    consumeName : "当前视在功率",
    consumeValue : PDU?.pdu_data?.pdu_total_data != null ? PDU.pdu_data.pdu_total_data.pow_apparent.toFixed(3) + "kVA" : '/'
  })
  temp.push({
    baseInfoName : "设备状态",
    baseInfoValue : PDU?.status != null ? PDU.status : '/',
    pduAlarm : PDU?.pdu_alarm,
    consumeName : "当前功率因素",
    consumeValue : PDU?.pdu_data?.pdu_total_data != null ? PDU.pdu_data.pdu_total_data.power_factor?.toFixed(2) : '/'
  })
  PDUTableData.value = temp;
  loading.value = false
  //清除temp1的缓存数据
  temp1.value=[]
  //获得告警信息
  getTableData()
  //处理告警信息数据
  // //debugger
  //处理时间信息
  const oldDate = new Date(queryParams.oldTime);
  const newDate = new Date(queryParams.newTime);
  
  
  console.log('`````````````', tableData.value)
  console.log('表格的数据',temp)
  Object.values(tableData.value).forEach((item: any)=>item.devKey== temp[1].baseInfoValue&&newDate>=new Date(item.startTime)&&new Date(item.startTime)>=oldDate?temp1.value.push(item):console.log("no"))
  console.log('temp1',temp1.value)
  
}

const serChartContainer = ref<HTMLElement | null>(null);
let serChart = null as echarts.ECharts | null; // 显式声明 serChart 的类型
let num=0
const indicator =ref<ServerData>({
  nameAndMax: [],
  curvalue: [],
  powvalue: [],
  powapparent: [],
})
indicator.value.nameAndMax=serverData.value.nameAndMax
const initChart =  () => {
  if (serChartContainer.value && instance && serverData.value.nameAndMax && serverData.value.nameAndMax.length > 0) {
    
    serChart = echarts.init(serChartContainer.value);
    serChart.setOption({
      legend: {
                data: ['电流'],
                selected: { // 默认选择状态
                  '电流': true, },
                  right: 80, // 距离右侧10px
        bottom: 10 // 距离底部10px
              },
      grid: {
                bottom: 0,
                top: 0,
              },
      radar: { indicator: indicator.value.nameAndMax},
      series: [
        { 
          name: 'PDU输出位电能', 
          type: 'radar', 
          label: { show: true, position: 'inside' } ,
          data:  [ { value: serverData.value.curvalue, name: '电流' }, ] ,
          itemStyle: {
            color: 'skyblue'
          }
        }
      ]
    });
    
    serChart.on('legendselectchanged', function (queryParams) {
      // console.log(indicator.value.nameAndMax)
      // console.log(serverData.value.nameAndMax)
      // console.log(serverData1.value.nameAndMax)
      const selected = queryParams.selected;
      console.log(selected)
      console.log(!selected['电流'])
      if(selected['电流']&&(selected['视在功率']||selected['有功功率'])){
        num++;
      }
      // if(!selected['电流']&&selected['视在功率']&&!selected['有功功率']){
      //   selected['电流'] = false;
      //   selected['视在功率'] = true;
      //   selected['有功功率'] = false;
      //   indicator.value.nameAndMax=serverData1.value.nameAndMax
      // }
      // else if(!selected['电流']&&!selected['视在功率']&&selected['有功功率']){
      //   selected['电流'] = false;
      //   selected['视在功率'] = false;
      //   selected['有功功率'] = true;
      //   indicator.value.nameAndMax=serverData1.value.nameAndMax
      // }
      if(selected['电流']&&!selected['视在功率']&&!selected['有功功率']){
        selected['电流'] = true;
        num=0;
        indicator.value.nameAndMax=serverData.value.nameAndMax
      }
      else if(num%2==0){
        selected['电流'] = true;
        indicator.value.nameAndMax=serverData.value.nameAndMax
      }
      else{
        selected['电流'] = false;
        indicator.value.nameAndMax=serverData1.value.nameAndMax
      }
      

      // 更新图表配置
      if(serChart){
      serChart.setOption({legend: {selected: selected},radar: { indicator: indicator.value.nameAndMax,max: 16}});
      }
});
        // serverData.value.nameAndMax=[]
        // serverData.value.curvalue=[]
        // serverData.value.powapparent=[]
        // serverData.value.powvalue=[]
        // serverData1.value.nameAndMax=[]
    // 将 serChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    // 使用截取后的数据
    serverData.value = truncateArrays(serverData.value);
    serverData1.value = truncateArrays(serverData1.value);
    instance.appContext.config.globalProperties.serChart = serChart;
  }

  visControll.visAllReport = true;
};


watch(filterText, (val) => {
  treeRef.value!.filter(val)
})

// 下拉框选项数组
// const deviceStatus = ref([])

// const message = useMessage() // 消息弹窗
// const { t } = useI18n() // 国际化


const loading = ref(false) // 列表的加载中

// const total = ref(0) // 列表的总页数
const queryFormRef = ref() // 搜索的表单
// const exportLoading = ref(false) // 导出的加载中

const arraySpanMethod = ({
  rowIndex,
}) => {
  if (rowIndex === 1) {
  //这里为了是将第二列的表头隐藏，就形成了合并表头的效果
      return {display: 'none'}
  }
}

/** 搜索按钮操作 */
const handleQuery = async () => {
  
  if(queryParams.ipAddr){
    if(queryParams.oldTime && queryParams.newTime){
      queryParams.devKey = queryParams.ipAddr +'-' +  queryParams.cascadeAddr;
      await getList();
      initChart();
      await PDUHdaLineHisdata();
      //await lineidFlashChartData();
      queryParams.devKey = null;
    }
  }
  
}

// const queryLoading = ref(false)
const tableLoading = ref(false)
const preStatus = ref([0])
const tableData = ref([])
// const message = useMessage() // 消息弹窗
// const alarmForm = ref()
const targetId = ref('')

// // 获取警告配置
// const getAlarmConfig = async() => {
//   queryLoading.value = true
//   try {
//     const res = await AlarmApi.getAlarmConfig({})
//     console.log('res', res)
//     if (res) {
//       queryParams.id = res.id
//       queryParams.openEmail = !!res.mailAlarm
//       queryParams.openVoice = !!res.voiceAlarm
//       queryParams.openMessage = !!res.smsAlarm
//     }
//   } finally  {
//     queryLoading.value = false
//   }
// }

// 获取告警信息数据
const getTableData = async(reset = false) => {
  tableLoading.value = true
  try {
    // //debugger
    const res = await AlarmApi.getAlarmRecord({
      pageNo: 1,
      pageSize: 10,
      a: 1,
      status: preStatus.value,
      likeName: queryParams.search
    })
    console.log('res', res)
    if (res.list) {    
      tableData.value = res.list
      queryParams.pageTotal = res.total   
    }
  } finally {
    tableLoading.value = false
    queryParams.a.value=0
  }
}
getTableData()
// 保存警告配置
// const saveAlarmConfig = async() => {
//   queryLoading.value = true
//   try {
//     const res = await AlarmApi.saveAlarmConfig({
//       id: queryParams.id,
//       mailAlarm: queryParams.openEmail ? 1 : 0,
//       voiceAlarm: queryParams.openVoice ? 1 : 0,
//       smsAlarm: queryParams.openMessage ? 1 : 0,
//     })
//     console.log('保存警告配置', res)
//     if (res) message.success('配置修改成功')
//   } finally {
//     queryLoading.value = false
//   }
//   console.log('saveAlarmConfig')
// }

// 处理表单弹窗
// const handleFormOpen = (type) => {
//   alarmForm.value.open(type)
// }

// 警告处理
// const toHandle = (type) => {
//   const statusList = ['挂起', '确认', '结束']
//   ElMessageBox.prompt('请输入原因：', `${statusList[type-1]}告警`, {
//     confirmButtonText: '确定',
//     cancelButtonText: '取消',
//     inputErrorMessage: '请输入',
//   })
//     .then(({ value }) => {
//       console.log(value)
//       AlarmApi.saveAlarmRecord({
//         id: targetId.value,
//         status: type,
//         confirmReason: value
//       }).then(() => {
//         ElMessage({
//           type: 'success', // 0 未处理 1 挂起 2确认 3结束
//           message: '成功',
//         })
//       })
//     })
// }

// 表格行选择处理
const handleCurrentChange = (val) => {
  if (!val) return
  targetId.value = val.id
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

//window.addEventListener('resize', function() {
//  lineidChart.resize();
//  lineidChartOne.resize();
//});
/** 初始化 **/
onMounted( async () =>  {
  getNavList()
  // getList();
  // initChart();
  ipList.value = await loadAll();
  window.addEventListener('resize', updateWindowWidth);
})

onUnmounted(() => {
  window.removeEventListener('resize', updateWindowWidth);
});
</script>
<style scoped lang="scss">

.master {
  width: 100%;
  box-sizing: border-box;
  display: flex;
  .master-left {
    position: relative;
    overflow: hidden;
    box-sizing: border-box;
    margin-right: 20px;
    transition: all 0.2s linear;
    .openNavtree {
      box-sizing: border-box;
      text-align: right;
      cursor: pointer;
      position: absolute;
      right: 10px;
      top: 12px;
      font-size: 15px;
      display: flex;
      align-items: center;
    }
    .reduce {
      display: flex;
      align-items: center;
      position: absolute;
      right: 10px;
      top: 52px;
      color: #777777;
      cursor: pointer;
      font-size: 13px;
    }
    .expand {
      width: 30px;
      display: flex;
      flex-direction: column;
      align-items: center;
      color: #777;
      cursor: pointer;
      background-color: #eef4fc;
      padding: 10px 0;
    }
  }
  .master-right {
    flex: 1;
    overflow: hidden;
  }
}

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

.navBar {
  box-sizing: border-box;
  width: 100%;
  height: 46px;
  line-height: 46px;
  padding-left: 10px;
  background-color: #d5ffc1;
  font-size: 14px;
}
.nav-left {
  width: 215px;
  height: 100%;
  .overview {
    padding: 0 20px;
    .count {
      height: 70px;
      margin-bottom: 15px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-left: 15px;
      padding-right: 10px;
      box-shadow: 0 3px 4px 1px rgba(0,0,0,.12);
      border-radius: 3px;
      border: 1px solid #eee;
      .info {
        height: 46px;
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        justify-content: space-between;
        font-size: 13px;
        .value {
          font-size: 15px;
          font-weight: bold;
        }
      }
    }
  }
  .status {
    display: flex;
    flex-wrap: wrap;
    .box {
      height: 70px;
      width: 50%;
      box-sizing: border-box;
      display: flex;
      justify-content: center;
      align-items: center;
      flex-direction: column;
      .top {
        display: flex;
        align-items: center;
        .tag {
          width: 8px;
          height: 8px;
          background-color: #3bbb00;
          margin-right: 3px;
          margin-top: 2px;
        }
        .empty {
          background-color: #ccc;
        }
        .warn {
          background-color: #ffc402;
        }
        .error {
          background-color: #fa3333;
        }
      }
      .value {
        font-size: 14px;
        margin-top: 5px;
        color: #aaa;
        .number {
          font-size: 14px;
          font-weight: bold;
          margin-right: 5px;
          color: #000;
        }
      }
    }
  }
  .header {
    display: flex;
    flex-direction: column;
    align-items: center;
    font-size: 13px;
    padding-top: 28px;
    .header_img {
      width: 110px;
      height: 110px;
      border-radius: 50%;
      display: flex;
      justify-content: center;
      align-items: center;
      border: 1px solid #555;
      img {
        width: 75px;
        height: 75px;
      }
    }
    .name {
      font-size: 15px;
      margin: 15px 0;
    }
  }
  .line {
    height: 1px;
    margin-top: 28px;
    margin-bottom: 20px;
    background: linear-gradient(297deg, #fff, #dcdcdc 51%, #fff);
  }
}
.progressContainer {
  display: flex;
  justify-content: center;
  align-items: center;
  .progress {
    width: 100px;
    height: 18px;
    line-height: 18px;
    font-size: 12px;
    box-sizing: border-box;
    border-radius: 150px;
    overflow: hidden;
    position: relative;
    vertical-align: middle;
    background-color: #bfa;
    display: flex;
    justify-content: center;
    .left {
      overflow: hidden;
      box-sizing: border-box;
      background-color: var(--el-color-primary);

    }
    .right {
      overflow: hidden;
      background-color:  #f56c6c;
    }
  }
}


.page-conTitle {
    font-size: 18px;
    font-weight: 600;
    padding: 10px 0;
}

.paragraph {
    text-indent: 25px;
    font-size: 16px;
    font-weight: 530;
    line-height: 1.7;
}

.page-con {
    padding: 0 20px 0 20px;
}

.page {
    -webkit-box-shadow: 0 0px 0px rgba(0, 0, 0, .1);
    box-shadow: 0 0px 0px rgba(0, 0, 0, .1);
    background: #fff;
    color: rgba(0, 0, 0, .8);
}

.pageBox{
  margin-top:1vw
}

.Container{

  left: 0px; 
  top: 0px; 
  user-select: none; 
  -webkit-tap-highlight-color: rgba(0, 0, 0, 0); 
  padding: 0px; 
  margin: 0px; 
  border-width: 0px;
}

.el-table--border {
    border: 1px solid #e8e8e8 !important;
}

.el-table {
    color: #2c2c2c !important;
}

:deep .el-table thead tr th {
    background: #F5F7FA!important;
    color: #909399;
}

:deep(.master-left .el-card__body) {
  padding: 0;
}
:deep(.el-form) {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
}

:deep(.el-form .el-form-item:nth-child(1)) {
  margin-left:-3vw;
}

:deep(.el-form .el-form-item:nth-child(2)) {
  margin-left:-1vw;
}

:deep(.el-form .el-form-item:nth-child(3)) {
  margin-left: 14vw;
}

@media screen and (min-width:2048px) {
  .adaptiveStyle {
    width: 75vw;
    height: 42vh;
  }
}

@media screen and  (max-width:2048px) and (min-width:1600px) {
  .adaptiveStyle {
    width: 70vw;
    height: 42vh;
  }
}

@media screen and (max-width:1600px) {
  .adaptiveStyle {
    width: 85vw;
    height: 42vh;
  }
}
</style>
