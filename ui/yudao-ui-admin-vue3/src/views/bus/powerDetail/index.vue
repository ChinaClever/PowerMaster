<template>
<div class="change" style="background-color: #E7E7E7;">
  <div class="header_app">
    <div class="header_app_text">
        <span style="margin-right:10px;">机房：{{ roomName?roomName : '未绑定' }}</span>
        <span style="margin-right:10px;">名称：{{ busName }}</span>
        <span style="margin-right:10px;">网络地址：{{ devKey }}</span> 
    </div>
    <div class="header_app_text_other1">
          <el-col :span="10" >
            <el-form
              class="-mb-15px"
              :model="queryParamsSearch"
              ref="queryFormRef"
              :inline="true"
              label-width="120px"
            >
              <el-form-item label="网络地址" prop="devKey" style="margin-top:2px;">
              <el-autocomplete
                v-model="queryParamsSearch.devKey"
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
    <div class="header_app_text_other flex-container">
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
            <el-select v-model="typeRadioShow" placeholder="请选择" style="width: 100px;margin-left: 5px;">
                <el-option label="实时" value="实时" />
                <el-option label="平均" value="平均" />
                <el-option label="最大" value="最大" />
                <el-option label="最小" value="最小" />
            </el-select>
        <el-button @click="changeTime('近一小时');" :type="queryParams.timeGranularity == '近一小时' ? 'primary' : ''" style="margin-left: 5px;">近一小时</el-button>
        <el-button @click="changeTime('今天');" :type="queryParams.timeGranularity == '今天' ? 'primary' : ''">今天</el-button>
        <el-button @click="changeTime('近一天');" :type="queryParams.timeGranularity == '近一天' ? 'primary' : ''">近一天</el-button>
        <el-button @click="changeTime('近三天');" :type="queryParams.timeGranularity == '近三天' ? 'primary' : ''">近三天</el-button>
    </div>
  </div>
  <div class="TransformerMonitor">
    <div class="center-part">
      <div class="left-part">
        <!-- <el-tag size="large">{{ location }}</el-tag> -->
        <div style="height:85%"><Gauge class="chart" v-if="visContro.gaugeVis" width="100%" height="100%" :load-factor="Math.round(redisData.loadFactor)" /></div>
        <div style="height:15%;display:flex;align-items: center;margin-left:5px">              
            <p style="color:black;">最大需量：<span  class="vale-part BColor" >{{peakDemand}}</span>kVA(发生时间：{{peakDemandTime}})</p>
        </div>
        <p v-if="!visContro.gaugeVis" class="noData">暂无数据</p>
      </div>
      <div  class="right-part">
        <div  class="center-top-part">
          <div  class="div-part">
            <div  class="div-part1">
              <p  class="middletxt txt1">{{redisData?.fr}}<span >Hz</span></p>
            </div>
            <p >频率</p>
          </div>
          <div  class="div-part">
            <div  class="div-part2">
              <p  class="middletxt txt2">{{redisData?.pf}}</p>
            </div>
            <p >功率因数</p>
          </div>
          <div  class="div-part">
            <div  class="div-part3">
              <p  class="middletxt txt3">{{redisData?.vub}}<span >%</span></p>
            </div>
            <p >三相电压不平衡度</p>
          </div>
          <div  class="div-part">
            <div  class="div-part4">
              <p  class="middletxt txt4">{{redisData?.cub}}<span >%</span></p>
            </div>
            <p >三相电流不平衡度</p>
          </div>
        </div>
        <div  class="center-bottom-part">
          <div  class="top-part">
            <span >| 负载</span>
            <span >| 电流</span>
            <span >| 电压</span>
            <span >| 线电压</span>
            <span >| 温度</span>
            <span >| 总谐波含有率
                <el-button @click="selectedOption = 'current'" :type="selectedOption == 'current' ? 'primary' : ''"  style="width: 50px;height:25px; background-color:#F5F7FA;margin-bottom:3px;color:#606266" >电流</el-button>
            </span>
            <span style="width: 50px;padding:0">
                <el-button @click="selectedOption = 'voltage'" :type="selectedOption == 'voltage' ? 'primary' : ''" style="width: 50px;height:25px; background-color:#F5F7FA;margin-bottom:3px;color:#606266">电压</el-button>
            </span>
          </div>
          <div  class="block-part">
            <div  class="content-part">
              <p>额定容量<span  class="vale-part BColor">{{redisData?.finstalledCapacity}}</span>kVA </p>
              <p>视在功率<span  class="vale-part BColor">{{redisData?.s}}</span>kVA</p>
              <p>有功功率<span  class="vale-part BColor">{{redisData?.p}}</span>kW</p>
              <p>无功功率<span  class="vale-part BColor">{{redisData?.q}}</span>kVar</p>
            </div>
            <div  class="content-part">
              <p  >A相 <span  class="vale-part BColor" :style="{ backgroundColor: getBackgroundColor(redisData?.curStatusA) }">{{redisData?.ia}}</span>A </p>
              <p  >B相 <span  class="vale-part BColor" :style="{ backgroundColor: getBackgroundColor(redisData?.curStatusB) }">{{redisData?.ib}}</span>A </p>
              <p  >C相 <span  class="vale-part BColor" :style="{ backgroundColor: getBackgroundColor(redisData?.curStatusC) }">{{redisData?.ic}}</span>A </p>
            </div>
            <div  class="content-part">
              <p  >Ua <span  class="vale-part BColor" :style="{ backgroundColor: getBackgroundColor(redisData?.volStatusA) }">{{redisData?.ua}}</span>V </p>
              <p  >Ub <span  class="vale-part BColor" :style="{ backgroundColor: getBackgroundColor(redisData?.volStatusB) }">{{redisData?.ub}}</span>V </p>
              <p  >Uc <span  class="vale-part BColor" :style="{ backgroundColor: getBackgroundColor(redisData?.volStatusC) }">{{redisData?.uc}}</span>V </p>
            </div>
            <div  class="content-part">
              <p  >Uab <span  class="vale-part BColor" :style="{ backgroundColor: getBackgroundColor(redisData?.volLineStatusA) }">{{redisData?.uab}}</span>V</p>
              <p  >Ubc <span  class="vale-part BColor" :style="{ backgroundColor: getBackgroundColor(redisData?.volLineStatusB) }">{{redisData?.ubc}}</span>V</p>
              <p  >Uca <span  class="vale-part BColor" :style="{ backgroundColor: getBackgroundColor(redisData?.volLineStatusC) }">{{redisData?.uca}}</span>V</p>
            </div>
            <div  class="content-part">
              <p  >A相温度 <span  class="vale-part BColor" :style="{ backgroundColor: getBackgroundColor(redisData?.temStatusA) }">{{redisData?.tempA}}</span>℃</p>
              <p  >B相温度 <span  class="vale-part BColor" :style="{ backgroundColor: getBackgroundColor(redisData?.temStatusB) }">{{redisData?.tempB}}</span>℃</p>
              <p  >C相温度 <span  class="vale-part BColor" :style="{ backgroundColor: getBackgroundColor(redisData?.temStatusC) }">{{redisData?.tempC}}</span>℃</p>
              <p  >N相温度 <span  class="vale-part BColor" :style="{ backgroundColor: getBackgroundColor(redisData?.temStatusN) }">{{redisData?.tempN}}</span>℃</p>
            </div><!---->
            <div  class="content-part">
              <p  v-show="selectedOption === 'current'">A相电流 <span  class="vale-part BColor">{{redisData?.iaTHD}}</span>%</p>
              <p  v-show="selectedOption === 'current'">B相电流 <span  class="vale-part BColor">{{redisData?.ibTHD}}</span>%</p>
              <p  v-show="selectedOption === 'current'">C相电流 <span  class="vale-part BColor">{{redisData?.icTHD}}</span>%</p>
              <p  v-show="selectedOption === 'voltage'">A相电压 <span  class="vale-part BColor">{{redisData?.uaTHD}}</span>%</p>
              <p  v-show="selectedOption === 'voltage'">B相电压 <span  class="vale-part BColor">{{redisData?.ubTHD}}</span>%</p>
              <p  v-show="selectedOption === 'voltage'">C相电压 <span  class="vale-part BColor">{{redisData?.ucTHD}}</span>%</p>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div  class="bottom-part">
      <div  class="bottomLineDiv">
        <p >| 负载率曲线</p>    
        <MarkLine v-if="visContro.loadRateVis"  width="100%" height="100%" :list="loadRateList"/>
        <p v-if="!visContro.loadRateVis" class="noData">暂无数据</p>
      </div>
      <div  class="bottomLineDiv">
        <p >| 有功功率</p>
        <PowActiveLine v-if="visContro.powActiveVis"  width="100%" height="100%" :list="powActiveList"/>
      </div>
      <div  class="bottomLineDiv">
        <p >| 无功功率</p>
        <PowReactiveLine v-if="visContro.powReactiveVis"  width="100%" height="100%" :list="powReactiveList"/>
      </div>
    </div>
  </div>
</div> 
</template>

<script setup lang="ts">

import { ref } from 'vue';
import Gauge from './component/Gauge.vue';
import MarkLine from './component/MarkLine.vue';
import PowReactiveLine from './component/PowReactiveLine.vue';
import PowActiveLine from './component/PowActiveLine.vue';
import { IndexApi } from '@/api/bus/busindex';
import { BusPowerLoadDetailApi } from '@/api/bus/buspowerloaddetail';

const peakDemand = ref(0);
const peakDemandTime = ref('');
const redisData = ref() as any;
const loadRateList = ref() as any;
const powActiveList = ref() as any;
const powReactiveList = ref() as any;
const selectedOption = ref('current');
const location = ref(history?.state?.location);
const busName = ref(history?.state?.busName);
const roomName = ref(history?.state?.roomName);
const devKey = ref(history?.state?.devKey);

const visContro = ref({
  gaugeVis : false,
  loadRateVis : false,
  powActiveVis : false,
  powReactiveVis : false,
})

const flashListTimer = ref();
const firstTimerCreate = ref(true);

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
const queryParamsSearch = reactive({
  devKey : history?.state?.devKey as string | undefined,
})
const queryParams = reactive({
  pageNo: 1,
  pageSize: 24,
  devKey: history?.state?.devKey,
  busId : history?.state?.busId,
  createTime: [],
  cascadeNum: undefined,
  serverRoomData:undefined,
  status:[],
  cabinetIds:[],
  timeType : 0,
  timeArr:[],
  timeGranularity: '近一小时',
  oldTime : getFullTimeByDate(new Date(new Date().getFullYear(),new Date().getMonth(),new Date().getDate(),0,0,0)),
  newTime : getFullTimeByDate(new Date(new Date().getFullYear(),new Date().getMonth(),new Date().getDate(),23,59,59)),
}) as any

const getRedisData = async () => {
  const data =  await IndexApi.getBusPowerRedisData(queryParams);//devKey
  let loopItem = {} as any;
  for (let key in data) {  
    if (data.hasOwnProperty(key)) {  
        // 排除null值，但保留updateTime  
        if (data[key] !== null && key != 'updateTime') { 
            loopItem[key] = data[key].toFixed(2); // 注意这将转换为字符串  
        }else if (data[key] == null){
            loopItem[key] = '/';
        }
    }  
  }
  loopItem['updateTime'] = data['updateTime'];
  redisData.value = loopItem;
  if(redisData.value.loadFactor != null){
    visContro.value.gaugeVis = true;
  }

//   redisData.value.loadFactor = Math.round(redisData.value.loadFactor);

  if(redisData.value.loadFactor >= 100 ){
    console.log(redisData.value.loadFactor+'负载率爆表了')
    redisData.value.loadFactor = 100
  }
}

const getLoadRateList = async () =>{
    const data = await IndexApi.getBusLoadRateLine(queryParams);//oldtime newtime id
    console.log('负载率曲线的data',data);
    loadRateList.value = data;
    if(loadRateList.value?.time != null && loadRateList.value?.time?.length > 0){
        visContro.value.loadRateVis = true;
    }else {
        visContro.value.loadRateVis = false;
    }
}

const getBusPowActiveList = async () =>{
    const data = await IndexApi.getBusPowActiveLine(queryParams);//oldtime newtime id
    powActiveList.value = data;

    if(powActiveList.value?.time != null && powActiveList.value?.time?.length > 0){
        visContro.value.powActiveVis = true;
    }else {
        visContro.value.powActiveVis = false;
    }
}

const getBusIdAndLocation =async () => {
 try {
    const data = await BusPowerLoadDetailApi.getBusIdAndLocation(queryParams);//devKey
    if (data != null){
      location.value = data.location
      queryParams.busId = data.busId
      busName.value = data.busName
    }else{
      location.value = null
      queryParams.busId = null
      busName.value = null
    }
 } finally {
 }
}

const getPeakDemand =async () => {
 try {
    const data = await IndexApi.getPeakDemand(queryParams);
    if (data != null){
        peakDemand.value = data.peakDemand.toFixed(3);
        peakDemandTime.value = data.peakDemandTime
    }
 } finally {
 }
}

const getBusPowReactiveList = async () =>{
    const data = await IndexApi.getBusPowReactiveLine(queryParams);//oldtime newtime id
    powReactiveList.value = data;
    if(powReactiveList.value?.time != null && powReactiveList.value?.time?.length > 0){
        visContro.value.powReactiveVis = true;
    }else {
        visContro.value.powReactiveVis = false;
    }
}
//刷新数据
const flashChartData = async () =>{
    await getRedisData();
    await getPeakDemand();
    await getLoadRateList();
    await getBusPowActiveList();
    await getBusPowReactiveList();
}

const handleQuery = async () => {
  queryParams.devKey = queryParamsSearch.devKey;
   await getBusIdAndLocation();
   await flashChartData();
}

const changeTime = async (data) => {
    queryParams.timeGranularity = data;   
    handleQuery();
}

const devKeyList = ref([])
const loadAll = async () => {
  //debugger
  var data = await BusPowerLoadDetailApi.getBusdevKeyList();
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

const getBackgroundColor = (wornStatus: number) => {
  if (wornStatus != 0) {
    return '#fa3333' //红色
  }
}
   
onBeforeUnmount(()=>{
  if(flashListTimer.value){
    clearInterval(flashListTimer.value)
    flashListTimer.value = null;
  }
})

onBeforeRouteLeave(()=>{
  if(flashListTimer.value){
    clearInterval(flashListTimer.value)
    flashListTimer.value = null;
    firstTimerCreate.value = false;
  }
})

/** 初始化 **/
onMounted(async () => {
  // await getDetailData();
  // await getLineChartData();
  devKeyList.value = await loadAll();
  await getBusIdAndLocation()
  await getRedisData();
  await getPeakDemand();
  await getLoadRateList();
  await getBusPowActiveList();
  await getBusPowReactiveList();
  flashListTimer.value = setInterval((getRedisData), 5000);
  // initChart1();
  // initChart2();


})

</script>


<style scoped lang="scss">
.selectBtn.el-button--primary {
    background-color: #eebf29!important
}

.AColor {
    color: #ffa700!important
}

.BColor {
    color: #8fc31f!important
}

.CColor {
    color: #e60012!important
}

.TransformerMonitor {
    height: calc(100vh - 84px);
    padding: 10px 10px;
}

.TransformerMonitor .topdiv {
    height: 45px;
    padding: 5px;
    margin-bottom: 5px
}

.TransformerMonitor .topdiv span {
    line-height: 35px;
    font-size: 14px
}

body .TransformerMonitor .topdiv span,body .TransformerMonitor .topdiv span,body .TransformerMonitor .topdiv span,body .TransformerMonitor .topdiv span {
    color: #000
}

body .TransformerMonitor .topdiv span,body .TransformerMonitor .topdiv span,body .TransformerMonitor .topdiv span {
    color: #fff
}

.TransformerMonitor .center-part {
    height: 350px;
    width: 100%;
    margin-bottom: 5px
}

.TransformerMonitor .center-part .left-part {
    display: inline-block;
    width: 25%;
    height: 100%;
    margin-right: 5px
}

body .TransformerMonitor .center-part .left-part,body .TransformerMonitor .center-part .left-part,body .TransformerMonitor .center-part .left-part,body .TransformerMonitor .center-part .left-part {
    color: #000;
    border: 1px solid #e2e2e2;
    background-color: #fff!important
}

body .TransformerMonitor .center-part .left-part {
    color: #fff;
    border: 0;
    background-color: #238!important
}

body .TransformerMonitor .center-part .left-part {
    color: #fff;
    border: 0;
    background-color: #1d3666!important
}

body .TransformerMonitor .center-part .left-part {
    color: #fff;
    border: 0;
    background-color: #ffffff!important
}

.TransformerMonitor .center-part .left-part #transformChart {
    width: 100%;
    height: 100%
}

.TransformerMonitor .center-part .right-part {
    display: inline-block;
    width: calc(75% - 5px);
    height: 100%;
    vertical-align: top
}

.TransformerMonitor .center-part .center-top-part {
    display: inline-block;
    width: 100%;
    height: calc(50% - 2.5px);
    margin-bottom: 5px
}

body .TransformerMonitor .center-part .center-top-part,body .TransformerMonitor .center-part .center-top-part,body .TransformerMonitor .center-part .center-top-part,body .TransformerMonitor .center-part .center-top-part {
    color: #000;
    border: 1px solid #e2e2e2;
    background-color: #fff!important
}

body .TransformerMonitor .center-part .center-top-part {
    color: #fff;
    border: 0;
    background-color: #238!important
}

body .TransformerMonitor .center-part .center-top-part {
    color: #fff;
    border: 0;
    background-color: #1d3666!important
}

body .TransformerMonitor .center-part .center-top-part {
    color: #fff;
    border: 0;
    background-color: #ffffff!important
}

.TransformerMonitor .center-part .center-top-part .div-part {
    display: inline-block;
    width: 24%;
    height: 100%;
    vertical-align: top
}

.TransformerMonitor .center-part .center-top-part .div-part p {
    text-align: center;
    margin: -5px 0 0 0;
    font-size: 16px
}

body .TransformerMonitor .center-part .center-top-part .div-part p,body .TransformerMonitor .center-part .center-top-part .div-part p,body .TransformerMonitor .center-part .center-top-part .div-part p,body .TransformerMonitor .center-part .center-top-part .div-part p {
    color: #1e9fe9
}

body .TransformerMonitor .center-part .center-top-part .div-part p,body .TransformerMonitor .center-part .center-top-part .div-part p,body .TransformerMonitor .center-part .center-top-part .div-part p {
    color: rgb(131, 131, 133)  
}

.TransformerMonitor .center-part .center-top-part .div-part .div-part1,.TransformerMonitor .center-part .center-top-part .div-part .div-part2,.TransformerMonitor .center-part .center-top-part .div-part .div-part3,.TransformerMonitor .center-part .center-top-part .div-part .div-part4 {
    text-align: center;
    vertical-align: middle
}

body .TransformerMonitor .center-part .center-top-part .div-part .div-part1,body .TransformerMonitor .center-part .center-top-part .div-part .div-part2,body .TransformerMonitor .center-part .center-top-part .div-part .div-part3,body .TransformerMonitor .center-part .center-top-part .div-part .div-part4,body .TransformerMonitor .center-part .center-top-part .div-part .div-part1,body .TransformerMonitor .center-part .center-top-part .div-part .div-part2,body .TransformerMonitor .center-part .center-top-part .div-part .div-part3,body .TransformerMonitor .center-part .center-top-part .div-part .div-part4,body .TransformerMonitor .center-part .center-top-part .div-part .div-part1,body .TransformerMonitor .center-part .center-top-part .div-part .div-part2,body .TransformerMonitor .center-part .center-top-part .div-part .div-part3,body .TransformerMonitor .center-part .center-top-part .div-part .div-part4,body .TransformerMonitor .center-part .center-top-part .div-part .div-part1,body .TransformerMonitor .center-part .center-top-part .div-part .div-part2,body .TransformerMonitor .center-part .center-top-part .div-part .div-part3,body .TransformerMonitor .center-part .center-top-part .div-part .div-part4 {
    color: #000
}

body .TransformerMonitor .center-part .center-top-part .div-part .div-part1,body .TransformerMonitor .center-part .center-top-part .div-part .div-part2,body .TransformerMonitor .center-part .center-top-part .div-part .div-part3,body .TransformerMonitor .center-part .center-top-part .div-part .div-part4,body .TransformerMonitor .center-part .center-top-part .div-part .div-part1,body .TransformerMonitor .center-part .center-top-part .div-part .div-part2,body .TransformerMonitor .center-part .center-top-part .div-part .div-part3,body .TransformerMonitor .center-part .center-top-part .div-part .div-part4,body .TransformerMonitor .center-part .center-top-part .div-part .div-part1,body .TransformerMonitor .center-part .center-top-part .div-part .div-part2,body .TransformerMonitor .center-part .center-top-part .div-part .div-part3,body .TransformerMonitor .center-part .center-top-part .div-part .div-part4 {
    color: #fff
}

.TransformerMonitor .center-part .center-top-part .div-part .div-part1 .middletxt,.TransformerMonitor .center-part .center-top-part .div-part .div-part2 .middletxt,.TransformerMonitor .center-part .center-top-part .div-part .div-part3 .middletxt,.TransformerMonitor .center-part .center-top-part .div-part .div-part4 .middletxt {
    margin: 0;
    position: relative;
    top: 50%;
    -webkit-transform: translateY(-50%);
    transform: translateY(-50%);
    font-family: YSBTH
}

body .TransformerMonitor .center-part .center-top-part .div-part .div-part1 .middletxt.txt1,body .TransformerMonitor .center-part .center-top-part .div-part .div-part2 .middletxt.txt1,body .TransformerMonitor .center-part .center-top-part .div-part .div-part3 .middletxt.txt1,body .TransformerMonitor .center-part .center-top-part .div-part .div-part4 .middletxt.txt1,body .TransformerMonitor .center-part .center-top-part .div-part .div-part1 .middletxt.txt1,body .TransformerMonitor .center-part .center-top-part .div-part .div-part2 .middletxt.txt1,body .TransformerMonitor .center-part .center-top-part .div-part .div-part3 .middletxt.txt1,body .TransformerMonitor .center-part .center-top-part .div-part .div-part4 .middletxt.txt1,body .TransformerMonitor .center-part .center-top-part .div-part .div-part1 .middletxt.txt1,body .TransformerMonitor .center-part .center-top-part .div-part .div-part2 .middletxt.txt1,body .TransformerMonitor .center-part .center-top-part .div-part .div-part3 .middletxt.txt1,body .TransformerMonitor .center-part .center-top-part .div-part .div-part4 .middletxt.txt1,body .TransformerMonitor .center-part .center-top-part .div-part .div-part1 .middletxt.txt1,body .TransformerMonitor .center-part .center-top-part .div-part .div-part2 .middletxt.txt1,body .TransformerMonitor .center-part .center-top-part .div-part .div-part3 .middletxt.txt1,body .TransformerMonitor .center-part .center-top-part .div-part .div-part4 .middletxt.txt1 {
    color: #000000
}

body .TransformerMonitor .center-part .center-top-part .div-part .div-part1 .middletxt.txt1,body .TransformerMonitor .center-part .center-top-part .div-part .div-part2 .middletxt.txt1,body .TransformerMonitor .center-part .center-top-part .div-part .div-part3 .middletxt.txt1,body .TransformerMonitor .center-part .center-top-part .div-part .div-part4 .middletxt.txt1,body .TransformerMonitor .center-part .center-top-part .div-part .div-part1 .middletxt.txt1,body .TransformerMonitor .center-part .center-top-part .div-part .div-part2 .middletxt.txt1,body .TransformerMonitor .center-part .center-top-part .div-part .div-part3 .middletxt.txt1,body .TransformerMonitor .center-part .center-top-part .div-part .div-part4 .middletxt.txt1,body .TransformerMonitor .center-part .center-top-part .div-part .div-part1 .middletxt.txt1,body .TransformerMonitor .center-part .center-top-part .div-part .div-part2 .middletxt.txt1,body .TransformerMonitor .center-part .center-top-part .div-part .div-part3 .middletxt.txt1,body .TransformerMonitor .center-part .center-top-part .div-part .div-part4 .middletxt.txt1 {
    color: #000000
}

body .TransformerMonitor .center-part .center-top-part .div-part .div-part1 .middletxt.txt2,body .TransformerMonitor .center-part .center-top-part .div-part .div-part2 .middletxt.txt2,body .TransformerMonitor .center-part .center-top-part .div-part .div-part3 .middletxt.txt2,body .TransformerMonitor .center-part .center-top-part .div-part .div-part4 .middletxt.txt2,body .TransformerMonitor .center-part .center-top-part .div-part .div-part1 .middletxt.txt2,body .TransformerMonitor .center-part .center-top-part .div-part .div-part2 .middletxt.txt2,body .TransformerMonitor .center-part .center-top-part .div-part .div-part3 .middletxt.txt2,body .TransformerMonitor .center-part .center-top-part .div-part .div-part4 .middletxt.txt2,body .TransformerMonitor .center-part .center-top-part .div-part .div-part1 .middletxt.txt2,body .TransformerMonitor .center-part .center-top-part .div-part .div-part2 .middletxt.txt2,body .TransformerMonitor .center-part .center-top-part .div-part .div-part3 .middletxt.txt2,body .TransformerMonitor .center-part .center-top-part .div-part .div-part4 .middletxt.txt2,body .TransformerMonitor .center-part .center-top-part .div-part .div-part1 .middletxt.txt2,body .TransformerMonitor .center-part .center-top-part .div-part .div-part2 .middletxt.txt2,body .TransformerMonitor .center-part .center-top-part .div-part .div-part3 .middletxt.txt2,body .TransformerMonitor .center-part .center-top-part .div-part .div-part4 .middletxt.txt2 {
    color: #000000
}

body .TransformerMonitor .center-part .center-top-part .div-part .div-part1 .middletxt.txt2,body .TransformerMonitor .center-part .center-top-part .div-part .div-part2 .middletxt.txt2,body .TransformerMonitor .center-part .center-top-part .div-part .div-part3 .middletxt.txt2,body .TransformerMonitor .center-part .center-top-part .div-part .div-part4 .middletxt.txt2,body .TransformerMonitor .center-part .center-top-part .div-part .div-part1 .middletxt.txt2,body .TransformerMonitor .center-part .center-top-part .div-part .div-part2 .middletxt.txt2,body .TransformerMonitor .center-part .center-top-part .div-part .div-part3 .middletxt.txt2,body .TransformerMonitor .center-part .center-top-part .div-part .div-part4 .middletxt.txt2,body .TransformerMonitor .center-part .center-top-part .div-part .div-part1 .middletxt.txt2,body .TransformerMonitor .center-part .center-top-part .div-part .div-part2 .middletxt.txt2,body .TransformerMonitor .center-part .center-top-part .div-part .div-part3 .middletxt.txt2,body .TransformerMonitor .center-part .center-top-part .div-part .div-part4 .middletxt.txt2 {
    color: #000000
}

body .TransformerMonitor .center-part .center-top-part .div-part .div-part1 .middletxt.txt3,body .TransformerMonitor .center-part .center-top-part .div-part .div-part2 .middletxt.txt3,body .TransformerMonitor .center-part .center-top-part .div-part .div-part3 .middletxt.txt3,body .TransformerMonitor .center-part .center-top-part .div-part .div-part4 .middletxt.txt3,body .TransformerMonitor .center-part .center-top-part .div-part .div-part1 .middletxt.txt3,body .TransformerMonitor .center-part .center-top-part .div-part .div-part2 .middletxt.txt3,body .TransformerMonitor .center-part .center-top-part .div-part .div-part3 .middletxt.txt3,body .TransformerMonitor .center-part .center-top-part .div-part .div-part4 .middletxt.txt3,body .TransformerMonitor .center-part .center-top-part .div-part .div-part1 .middletxt.txt3,body .TransformerMonitor .center-part .center-top-part .div-part .div-part2 .middletxt.txt3,body .TransformerMonitor .center-part .center-top-part .div-part .div-part3 .middletxt.txt3,body .TransformerMonitor .center-part .center-top-part .div-part .div-part4 .middletxt.txt3,body .TransformerMonitor .center-part .center-top-part .div-part .div-part1 .middletxt.txt3,body .TransformerMonitor .center-part .center-top-part .div-part .div-part2 .middletxt.txt3,body .TransformerMonitor .center-part .center-top-part .div-part .div-part3 .middletxt.txt3,body .TransformerMonitor .center-part .center-top-part .div-part .div-part4 .middletxt.txt3 {
    color: #000000
}

body .TransformerMonitor .center-part .center-top-part .div-part .div-part1 .middletxt.txt3,body .TransformerMonitor .center-part .center-top-part .div-part .div-part2 .middletxt.txt3,body .TransformerMonitor .center-part .center-top-part .div-part .div-part3 .middletxt.txt3,body .TransformerMonitor .center-part .center-top-part .div-part .div-part4 .middletxt.txt3,body .TransformerMonitor .center-part .center-top-part .div-part .div-part1 .middletxt.txt3,body .TransformerMonitor .center-part .center-top-part .div-part .div-part2 .middletxt.txt3,body .TransformerMonitor .center-part .center-top-part .div-part .div-part3 .middletxt.txt3,body .TransformerMonitor .center-part .center-top-part .div-part .div-part4 .middletxt.txt3,body .TransformerMonitor .center-part .center-top-part .div-part .div-part1 .middletxt.txt3,body .TransformerMonitor .center-part .center-top-part .div-part .div-part2 .middletxt.txt3,body .TransformerMonitor .center-part .center-top-part .div-part .div-part3 .middletxt.txt3,body .TransformerMonitor .center-part .center-top-part .div-part .div-part4 .middletxt.txt3 {
    color: #000000
}

body .TransformerMonitor .center-part .center-top-part .div-part .div-part1 .middletxt.txt4,body .TransformerMonitor .center-part .center-top-part .div-part .div-part2 .middletxt.txt4,body .TransformerMonitor .center-part .center-top-part .div-part .div-part3 .middletxt.txt4,body .TransformerMonitor .center-part .center-top-part .div-part .div-part4 .middletxt.txt4,body .TransformerMonitor .center-part .center-top-part .div-part .div-part1 .middletxt.txt4,body .TransformerMonitor .center-part .center-top-part .div-part .div-part2 .middletxt.txt4,body .TransformerMonitor .center-part .center-top-part .div-part .div-part3 .middletxt.txt4,body .TransformerMonitor .center-part .center-top-part .div-part .div-part4 .middletxt.txt4,body .TransformerMonitor .center-part .center-top-part .div-part .div-part1 .middletxt.txt4,body .TransformerMonitor .center-part .center-top-part .div-part .div-part2 .middletxt.txt4,body .TransformerMonitor .center-part .center-top-part .div-part .div-part3 .middletxt.txt4,body .TransformerMonitor .center-part .center-top-part .div-part .div-part4 .middletxt.txt4,body .TransformerMonitor .center-part .center-top-part .div-part .div-part1 .middletxt.txt4,body .TransformerMonitor .center-part .center-top-part .div-part .div-part2 .middletxt.txt4,body .TransformerMonitor .center-part .center-top-part .div-part .div-part3 .middletxt.txt4,body .TransformerMonitor .center-part .center-top-part .div-part .div-part4 .middletxt.txt4 {
    color: #000000
}

body .TransformerMonitor .center-part .center-top-part .div-part .div-part1 .middletxt.txt4,body .TransformerMonitor .center-part .center-top-part .div-part .div-part2 .middletxt.txt4,body .TransformerMonitor .center-part .center-top-part .div-part .div-part3 .middletxt.txt4,body .TransformerMonitor .center-part .center-top-part .div-part .div-part4 .middletxt.txt4,body .TransformerMonitor .center-part .center-top-part .div-part .div-part1 .middletxt.txt4,body .TransformerMonitor .center-part .center-top-part .div-part .div-part2 .middletxt.txt4,body .TransformerMonitor .center-part .center-top-part .div-part .div-part3 .middletxt.txt4,body .TransformerMonitor .center-part .center-top-part .div-part .div-part4 .middletxt.txt4,body .TransformerMonitor .center-part .center-top-part .div-part .div-part1 .middletxt.txt4,body .TransformerMonitor .center-part .center-top-part .div-part .div-part2 .middletxt.txt4,body .TransformerMonitor .center-part .center-top-part .div-part .div-part3 .middletxt.txt4,body .TransformerMonitor .center-part .center-top-part .div-part .div-part4 .middletxt.txt4 {
    color: #000000
}

.TransformerMonitor .center-part .center-top-part .div-part .div-part1 span,.TransformerMonitor .center-part .center-top-part .div-part .div-part2 span,.TransformerMonitor .center-part .center-top-part .div-part .div-part3 span,.TransformerMonitor .center-part .center-top-part .div-part .div-part4 span {
    font-size: 14px
}

.TransformerMonitor .center-part .center-top-part .div-part .div-part1 {
    background: url(../../../assets/imgs/transformMonitor30001.8104cb6f.png) no-repeat 50%;
    background-size: 45%;
    height: calc(100% - 30px)
}

.TransformerMonitor .center-part .center-top-part .div-part .div-part2 {
    background: url(../../../assets/imgs/transformMonitor30002.e194a426.png) no-repeat 50%;
    background-size: 45%;
    height: calc(100% - 30px)
}

.TransformerMonitor .center-part .center-top-part .div-part .div-part3 {
    background: url(../../../assets/imgs/transformMonitor30003.8df2d215.png) no-repeat 50%;
    background-size: 45%;
    height: calc(100% - 30px)
}

.TransformerMonitor .center-part .center-top-part .div-part .div-part4 {
    background: url(../../../assets/imgs/transformMonitor30004.0656ad32.png) no-repeat 50%;
    background-size: 45%;
    height: calc(100% - 30px)
}

.TransformerMonitor .center-part .center-bottom-part {
    display: inline-block;
    width: 100%;
    height: calc(50% - 2.5px)
}

body .TransformerMonitor .center-part .center-bottom-part,body .TransformerMonitor .center-part .center-bottom-part,body .TransformerMonitor .center-part .center-bottom-part,body .TransformerMonitor .center-part .center-bottom-part {
    color: #000;
    border: 1px solid #e2e2e2;
    background-color: #fff!important
}

body .TransformerMonitor .center-part .center-bottom-part {
    color: #fff;
    border: 0;
    background-color: #238!important
}

body .TransformerMonitor .center-part .center-bottom-part {
    color: #fff;
    border: 0;
    background-color: #1d3666!important
}

body .TransformerMonitor .center-part .center-bottom-part {
    color: #fff;
    border: 0;
    background-color: #ffffff!important
}

.TransformerMonitor .center-part .center-bottom-part .top-part {
    display: inline-block;
    width: 100%;
    height: 40px;
    background-color: rgba(60,102,175,.41)
}

.TransformerMonitor .center-part .center-bottom-part .top-part span {
    display: inline-block;
    width: 14%;
    line-height: 40px;
    font-size: 16px;
    padding-left: 20px;
    color: #65c5fc
}

.TransformerMonitor .center-part .center-bottom-part .block-part {
    width: 100%;
    height: calc(100% - 40px)
}

.TransformerMonitor .center-part .center-bottom-part .block-part .content-part {
    width: 16%;
    height: 100%;
    display: inline-block;
    vertical-align: top;
    padding-top: 15px
}

.TransformerMonitor .center-part .center-bottom-part .block-part .content-part p {
    margin: 8px 0;
    font-size: 14px
}

body .TransformerMonitor .center-part .center-bottom-part .block-part .content-part p,body .TransformerMonitor .center-part .center-bottom-part .block-part .content-part p,body .TransformerMonitor .center-part .center-bottom-part .block-part .content-part p,body .TransformerMonitor .center-part .center-bottom-part .block-part .content-part p {
    color: #000
}

body .TransformerMonitor .center-part .center-bottom-part .block-part .content-part p,body .TransformerMonitor .center-part .center-bottom-part .block-part .content-part p,body .TransformerMonitor .center-part .center-bottom-part .block-part .content-part p {
    color: #000
}

.TransformerMonitor .center-part .center-bottom-part .block-part .content-part .vale-part {
    color: #01ada8;
    width: 40px;
    display: inline-block;
    margin: 0 10px
}

.TransformerMonitor .bottom-part {
    height: calc(50% - 10px);
    width: 100%
}

body .TransformerMonitor .bottom-part,body .TransformerMonitor .bottom-part,body .TransformerMonitor .bottom-part,body .TransformerMonitor .bottom-part {
    color: #000;
    border: 1px solid #e2e2e2;
    background-color: #fff!important
}

body .TransformerMonitor .bottom-part {
    color: #fff;
    border: 0;
    background-color: #238!important
}

body .TransformerMonitor .bottom-part {
    color: #fff;
    border: 0;
    background-color: #1d3666!important
}

body .TransformerMonitor .bottom-part {
    color: #fff;
    border: 0;
    background-color: #ffffff!important
}

.TransformerMonitor .bottom-part .bottomLineDiv {
    display: inline-block;
    width: 33.33%;
    height: 100%;
    vertical-align: top;
}

.TransformerMonitor .bottom-part .bottomLineDiv p {
    color: #65c5fc;
    font-weight: 700;
    padding-left: 8px;
    border-left: 2px solid;
    line-height: 36px;
    margin: 0
}

.TransformerMonitor .bottom-part .bottomLineDiv #transformLine1,.TransformerMonitor .bottom-part .bottomLineDiv #transformLine2,.TransformerMonitor .bottom-part .bottomLineDiv #transformLine3 {
    width: 100%;
    height: 100%
}

.noData {
    color: #fff;
    text-align: center;
    line-height: 200px
}

//@media screen and (max-width: 2160px) {
//    .TransformerMonitor .center-part .center-top-part .div-part .div-part1 .middletxt,.TransformerMonitor .center-part .center-top-part .div-part .div-part2 .middletxt,.TransformerMonitor .center-part .center-top-part .div-part .div-part3 .middletxt,.TransformerMonitor .center-part .center-top-part .div-part .div-part4 .middletxt {
//        font-size:30px
//    }
//
//    .TransformerMonitor .bottom-part .bottomLineDiv p,.TransformerMonitor .center-part .center-bottom-part .block-part .content-part p,.TransformerMonitor .center-part .center-top-part .div-part p {
//        font-size: 16px
//    }
//
//    .TransformerMonitor .center-part .center-bottom-part .top-part {
//        height: 50px
//    }
//
//    .TransformerMonitor .center-part .center-bottom-part .top-part span {
//        line-height: 50px;
//        font-size: 16px
//    }
//
//    .TransformerMonitor .center-part .center-bottom-part .block-part{
//        margin-top:-5px;
//    }
//    
//    .TransformerMonitor .center-part .center-bottom-part .block-part .content-part p {
//        margin: 8px 0
//    }
//
//    .TransformerMonitor .center-part .center-bottom-part .block-part .content-part .vale-part {
//        width: 66px
//    }
//}

@media screen and (min-width: 1920px) {
    .change{
        height:90vh;
    }

    .TransformerMonitor .center-part .center-top-part .div-part .div-part1 .middletxt,.TransformerMonitor .center-part .center-top-part .div-part .div-part2 .middletxt,.TransformerMonitor .center-part .center-top-part .div-part .div-part3 .middletxt,.TransformerMonitor .center-part .center-top-part .div-part .div-part4 .middletxt {
        font-size:28px
    }

    .TransformerMonitor .bottom-part .bottomLineDiv p,.TransformerMonitor .center-part .center-top-part .div-part p {
        font-size: 16px
    }

    .TransformerMonitor .center-part .center-bottom-part .block-part .content-part {
        padding-top: 0
    }

    .TransformerMonitor .center-part .center-bottom-part .block-part .content-part p {
        font-size: 15px
    }

    .TransformerMonitor .center-part .center-bottom-part .top-part {
        height: 40px
    }

    .TransformerMonitor .center-part .center-bottom-part .top-part span {
        line-height: 32px;
        font-size: 14px
    }

    .TransformerMonitor .center-part .center-bottom-part .block-part .content-part .vale-part {
        width: 48px
    }
}

@media screen and (max-width: 1920px) {
    .change{
        height:850px;
    }

    .TransformerMonitor .center-part .center-top-part .div-part .div-part1 .middletxt,.TransformerMonitor .center-part .center-top-part .div-part .div-part2 .middletxt,.TransformerMonitor .center-part .center-top-part .div-part .div-part3 .middletxt,.TransformerMonitor .center-part .center-top-part .div-part .div-part4 .middletxt {
        font-size:28px
    }

    .TransformerMonitor .bottom-part .bottomLineDiv p,.TransformerMonitor .center-part .center-top-part .div-part p {
        font-size: 16px
    }

    .TransformerMonitor .center-part .center-bottom-part .block-part .content-part {
        padding-top: 0
    }

    .TransformerMonitor .center-part .center-bottom-part .block-part .content-part p {
        font-size: 15px
    }

    .TransformerMonitor .center-part .center-bottom-part .top-part {
        height: 40px
    }

    .TransformerMonitor .center-part .center-bottom-part .top-part span {
        line-height: 32px;
        font-size: 14px
    }

    .TransformerMonitor .center-part .center-bottom-part .block-part .content-part .vale-part {
        width: 48px
    }
}

@media screen and (max-width: 1680px) {
    .change{
        height:800px;
    }

    .TransformerMonitor .center-part .center-bottom-part .block-part .content-part {
        padding-top:5px
    }

    .TransformerMonitor .center-part .center-top-part .div-part .div-part1 .middletxt,.TransformerMonitor .center-part .center-top-part .div-part .div-part2 .middletxt,.TransformerMonitor .center-part .center-top-part .div-part .div-part3 .middletxt,.TransformerMonitor .center-part .center-top-part .div-part .div-part4 .middletxt {
        font-size: 24px
    }

    .TransformerMonitor .center-part .center-bottom-part .block-part .content-part p {
        margin: 8px 0
    }
}

@media screen and (max-width: 1600px) {
    .TransformerMonitor .center-part .center-bottom-part .block-part .content-part {
        padding-top:0
    }

    .TransformerMonitor .center-part .center-top-part .div-part .div-part1 .middletxt,.TransformerMonitor .center-part .center-top-part .div-part .div-part2 .middletxt,.TransformerMonitor .center-part .center-top-part .div-part .div-part3 .middletxt,.TransformerMonitor .center-part .center-top-part .div-part .div-part4 .middletxt {
        font-size: 20px
    }

    .TransformerMonitor .center-part .center-bottom-part .top-part {
        height: 32px
    }

    .TransformerMonitor .center-part .center-bottom-part .top-part span {
        line-height: 32px;
        font-size: 14px
    }

    .TransformerMonitor .center-part .center-bottom-part .block-part .content-part p {
        margin: 5px 0;
        font-size: 14px
    }
}

@media screen and (max-width: 1366px) {
    .TransformerMonitor .center-part .center-top-part .div-part p {
        margin:10px 0 10px 0
    }

    .TransformerMonitor .center-part .center-top-part .div-part .div-part1 .middletxt,.TransformerMonitor .center-part .center-top-part .div-part .div-part2 .middletxt,.TransformerMonitor .center-part .center-top-part .div-part .div-part3 .middletxt,.TransformerMonitor .center-part .center-top-part .div-part .div-part4 .middletxt {
        font-size: 20px
    }

    .TransformerMonitor .center-part .center-bottom-part .block-part .content-part .vale-part {
        width: 38px
    }

    .TransformerMonitor .center-part .center-bottom-part .top-part {
        height: 25px
    }

    .TransformerMonitor .center-part .center-bottom-part .top-part span {
        line-height: 25px
    }

    .TransformerMonitor .center-part .center-bottom-part .block-part .content-part p {
        margin: 4px 0
    }

    .TransformerMonitor .center-part .center-bottom-part .top-part span,.TransformerMonitor .center-part .center-top-part .div-part p {
        font-size: 14px
    }

    .TransformerMonitor .center-part .center-bottom-part .block-part .content-part p {
        font-size: 13px
    }

    .TransformerMonitor .bottom-part .bottomLineDiv p {
        font-size: 14px
    }
}

body .TransformerMonitor .center-part .center-bottom-part .top-part {
    background: #f5f7fa
}

body .TransformerMonitor .center-part .center-bottom-part .top-part span {
    color: #000;

}

body .TransformerMonitor .bottom-part .bottomLineDiv p,body .TransformerMonitor .bottom-part .bottomLineDiv p {
    border-left: 2px solid #fff;
    background: #f5f7fa;
    color: #606266
}

body .TransformerMonitor .center-part .center-bottom-part .top-part,body .TransformerMonitor .center-part .center-bottom-part .top-part {
    //background: #01ada8
}

body .TransformerMonitor .center-part .center-bottom-part .top-part span,body .TransformerMonitor .center-part .center-bottom-part .top-part span {
    color: #606266
}

.header_app{
  background-color: white;
  display: flex;
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

//.header_app_text_other{
//  width: 80%;
//  align-content: center;
//  background-color: white;
//  margin-right:-10vh;
//}

.header_app_text_other1{
  align-content: center;
  background-color: white;
}

.flex-container {
    display: flex;
    justify-content: flex-end; /* 将内容对齐到右边 */
    align-items: center; /* 垂直方向居中对齐，可选 */
}
</style>