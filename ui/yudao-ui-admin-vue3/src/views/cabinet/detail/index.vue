<template>
<div style="background-color: #E7E7E7;margin-bottom:20px;" class="centainer-height">
  <div style="background-color: #fff; display: flex; justify-content: space-between; align-items: center; padding: 10px;margin:0 28px 10px 20px;" class="header_app">
    <div style="padding: 5px 10px;" class="header_app_text_left">
      <span>所在位置：{{ location }}-{{ busName }}</span>
      <span style="margin-left:10px;">A路网络地址：{{ keyAlocation }}</span>
      <span style="margin-left:10px;">B路网络地址：{{ keyBlocation }}</span>
      <span v-if="pduBox === false" style="margin-left:10px;"><el-button @click="goPDU(keyA,location,busName,'A路')">A路PDU详情</el-button><el-button @click="goPDU(keyB,location,busName,'B路')">B路PDU详情</el-button></span>
      <span v-else-if="pduBox === true" style="margin-left:10px;"><el-button @click="goBus(keyA,location,busName,'A路')">A路母线详情</el-button><el-button @click="goBus(keyB,location,busName,'A路')">B路母线详情</el-button></span>
    </div>
    <div  style="display: flex; justify-content: flex-end; gap: 10px;" class="header_app_text_right">
      <!--<el-button @click="handleQuery"  ><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>-->
      <el-button @click="changeTime ('hour');" :type="queryParams.timeGranularity == 'hour' ? 'primary' : ''">近一小时</el-button>
      <el-button @click="changeTime ('day');" :type="queryParams.timeGranularity == 'day' ? 'primary' : ''">今天</el-button>
      <el-button @click="changeTime('today');" :type="queryParams.timeGranularity == 'today' ? 'primary' : ''">近一天</el-button>
      <el-button @click="changeTime('threeDay');" :type="queryParams.timeGranularity == 'threeDay' ? 'primary' : ''">近三天</el-button>
    </div>
  </div>
  <div class="TransformerMonitor">
    <div class="center-part">
      <div class="left-part">
        <!-- <el-tag size="large">{{ location }}</el-tag>
        <div style="height:20px;display:flex;align-items: center;margin:10px 0 10px 10px;">              
            <span style="color:black;font-size:26px;">负载率</span>
        </div>
        <div style="height:20px;display:flex;align-items: center;margin-left:10px">              
            <span style="color:#ccc;font-size:14px;">最大需量：<span  class="vale-part BColor" >{{peakDemand}}</span>kVA</span>
        </div>
        <div style="height:20px;display:flex;align-items: center;margin-left:10px;">              
            <span style="color:#ccc;font-size:14px;">发生时间：{{peakDemandTime}}</span>
        </div>-->
        <div style="height:20px;display:flex;align-items: center;margin:10px 0 10px 10px;">              
            <span style="color:black;font-weight:bold;height:20px;width:100px;">负载率</span>
            <div style="margin-left:100px;">
                <span style="color:black;font-size:14px;margin-right:10px;">最大负载率: {{loadFactorBig}}</span>
                <span style="color:black;font-size:14px;">发生时间: {{loadFactorTime}}</span>
            </div>
        </div>
        <!--<div style="height:20px;display:flex;align-items: center;margin-left:10px;">              
            <span style="color:#ccc;font-size:14px;border-bottom:1px solid #ccc;width:90%;"></span>
        </div>-->
        <div style="height:40vh;width:100%;margin-top:-40px;">
            <Gauge class="chart" v-if="visContro.gaugeVis" width="100%" height="100%" :load-factor="resultData.loadFactor" />
        </div>
        <!--<div style="position: relative; top: -80px; left: 0; width: 100%; text-align: center; padding-top: 10px;">
            <div style="color: black;font-size: 30px;">{{redisData?.loadFactor}}</div>
            <div style="color: black;">负载率（%）</div>
        </div>-->
        <p v-if="!visContro.gaugeVis" class="noData">暂无数据</p>
      </div>
      <div class="right-part">
        <div class="center-top-part">
          <div style="color: black;margin:10px 0 0 10px;font-weight: bold;">实时功率</div>
          <RealTimePower style="margin-top:-10px;" class="chart" v-if="visContro.gaugeVis" width="100%" height="100%" :load-factor="resultData"/>
        </div>
        <div class="center-top-right-part">
          <div class="label-container">
            <span class="bullet" style="color:#000;">•</span><span style="width:80px;font-size:14px;">额定容量:</span><span style="font-size:16px;">0KVA</span>
          </div>
          <div class="label-container">
            <span class="bullet" style="color:#5470c6;">•</span><span style="width:80px;font-size:14px;">总视在功率:</span><span style="font-size:16px;">{{resultData?.powApparentTotal}}KVA</span>
          </div>
          <div class="label-container">
            <span class="bullet" style="color:#91cc75;">•</span><span style="width:80px;font-size:14px;">总有功功率:</span><span style="font-size:16px;">{{resultData?.powActiveTotal}}KW</span>
          </div>
          <div class="label-container">
            <span class="bullet" style="color:#800080;">•</span><span style="width:80px;font-size:14px;">总无功功率:</span><span style="font-size:16px;">{{resultData?.powReactiveTotal}}KVAR</span>
          </div>
        </div>
      </div>
      <div class="right-right-part" style="color: black;font-weight:bold;">
        <div style="margin:10px;">负载率曲线</div>
        <MarkLine style="margin-top:-40px;" v-if="visContro.gaugeVis"  width="100%" height="100%" :list="resultData"/>
      </div>
    </div>
    <div class="bottom-part">
      <div style="display: inline-block;
        width: 50%;
        height: 100%;">
          <div style="color: black;margin:10px 0 0 10px;font-weight:bold;">A路电压</div>
          <AVol style="margin-top:-30px;" class="chart" v-if="visContro.gaugeVis" width="100%" height="100%" :load-factor="resultData"/>
        </div>
        <div style="display: inline-block;
            position: absolute;
            width: 50%;
            height: 100%;
            top: 25%;">
          <div class="label-container">
            <span class="bullet" style="color:#075F71;">•</span><span style="width:50px;font-size:14px;">Ua:</span><span style="font-size:16px;">{{resultData?.volA?.[0] || 'N/A'}}V</span>
          </div>
          <div class="label-container">
            <span class="bullet" style="color:#119CB5;">•</span><span style="width:50px;font-size:14px;">Ub:</span><span style="font-size:16px;">{{resultData?.volA?.[1] || 'N/A'}}V</span>
          </div>
          <div class="label-container">
            <span class="bullet" style="color:#45C0C9;">•</span><span style="width:50px;font-size:14px;">Uc:</span><span style="font-size:16px;">{{resultData?.volA?.[2] || 'N/A'}}V</span>
          </div>
        </div>
    </div>
    <div class="bottom-part">
      <div style="display: inline-block;
        width: 50%;
        height: 100%;">
          <div style="color: black;margin:10px 0 0 10px;font-weight:bold;">A路电流</div>
          <ACur style="margin-top:-30px;" class="chart" v-if="visContro.gaugeVis" width="100%" height="100%" :load-factor="resultData"/>
        </div>
        <div style="display: inline-block;
            position: absolute;
            width: 50%;
            height: 100%;
            top: 25%;">
          <div class="label-container">
            <span class="bullet" style="color:#E5B849">•</span><span style="width:50px;font-size:14px;">Ia</span><span style="font-size:16px;">{{resultData?.curA?.[0]}}A</span>
          </div>
          <div class="label-container">
            <span class="bullet" style="color:#C8603A;">•</span><span style="width:50px;font-size:14px;">Ib</span><span style="font-size:16px;">{{resultData?.curA?.[1]}}A</span>
          </div>
          <div class="label-container">
            <span class="bullet" style="color:#AD3762;">•</span><span style="width:50px;font-size:14px;">Ic</span><span style="font-size:16px;">{{resultData?.curA?.[2]}}A</span>
          </div>
        </div>
    </div>
    <div class="bottom-part">
      <div style="display: inline-block;
        width: 50%;
        height: 100%;">
          <div style="color: black;margin:10px 0 0 10px;font-weight:bold;">功率因数</div>
          <PowerFactor style="margin-top:-30px;" class="chart" v-if="visContro.gaugeVis" width="100%" height="100%" :load-factor="resultData"/>
        </div>
        <div style="display: inline-block;
            position: absolute;
            width: 50%;
            height: 100%;
            top: 25%;">
          <!--<div class="label-container">
            <span class="bullet" style="color:#E5B849;">•</span><span style="font-size:14px;">频率:</span><span style="font-size:16px;">0Hz</span>
          </div>-->
          <div class="label-container">
            <span class="bullet" style="color:#C8603A;">•</span><span style="font-size:14px;">总功率因数:</span><span style="font-size:16px;">{{resultData?.powerFactor}}</span>
          </div>
          <div class="label-container">
            <span class="bullet" style="color:#C8603A;">•</span><span style="font-size:14px;">A路功率因数:</span><span style="font-size:16px;">{{resultData?.powerFactorA}}</span>
          </div>
          <div class="label-container">
            <span class="bullet" style="color:#C8603A;">•</span><span style="font-size:14px;">B路功率因数:</span><span style="font-size:16px;">{{resultData?.powerFactorB}}</span>
          </div>
          <!--<div class="label-container">
            <span class="bullet" style="color:#AD3762;">•</span><span style="font-size:14px;">三相电压不平衡度:</span><span style="font-size:16px;">0%</span>
          </div>
          <div class="label-container">
            <span class="bullet" style="color:#B47660;">•</span><span style="font-size:14px;">三相电流不平衡度:</span><span style="font-size:16px;">0%</span>
          </div>-->
        </div>
    </div>
    <div class="bottom-part">
      <div style="display: inline-block;
        width: 50%;
        height: 100%;">
          <div style="color: black;margin:10px 0 0 10px;font-weight:bold;">B路电压</div>
          <BVol style="margin-top:-30px;" class="chart" v-if="visContro.gaugeVis" width="100%" height="100%" :load-factor="resultData"/>
        </div>
        <div style="display: inline-block;
            position: absolute;
            width: 50%;
            height: 100%;
            top: 25%;">
          <div class="label-container">
            <span class="bullet" style="color:#075F71;">•</span><span style="width:50px;font-size:14px;">Ua:</span><span style="font-size:16px;">{{resultData?.volB?.[0]}}V</span>
          </div>
          <div class="label-container">
            <span class="bullet" style="color:#119CB5;">•</span><span style="width:50px;font-size:14px;">Ub:</span><span style="font-size:16px;">{{resultData?.volB?.[1]}}V</span>
          </div>
          <div class="label-container">
            <span class="bullet" style="color:#45C0C9;">•</span><span style="width:50px;font-size:14px;">Uc:</span><span style="font-size:16px;">{{resultData?.volB?.[2]}}V</span>
          </div>
        </div>
    </div>
    <div class="bottom-part">
      <div style="display: inline-block;
        width: 50%;
        height: 100%;">
          <div style="color: black;margin:10px 0 0 10px;font-weight:bold;">B路电流</div>
          <BCur style="margin-top:-30px;" class="chart" v-if="visContro.gaugeVis" width="100%" height="100%" :load-factor="resultData"/>
        </div>
        <div style="display: inline-block;
            position: absolute;
            width: 50%;
            height: 100%;
            top: 25%;">
          <div class="label-container">
            <span class="bullet" style="color:#E5B849;">•</span><span style="width:50px;font-size:14px;">Ia</span><span style="font-size:16px;">{{resultData?.curB?.[0]}}A</span>
          </div>
          <div class="label-container">
            <span class="bullet" style="color:#C8603A;">•</span><span style="width:50px;font-size:14px;">Ib</span><span style="font-size:16px;">{{resultData?.curB?.[1]}}A</span>
          </div>
          <div class="label-container">
            <span class="bullet" style="color:#AD3762;">•</span><span style="width:50px;font-size:14px;">Ic</span><span style="font-size:16px;">{{resultData?.curB?.[2]}}A</span>
          </div>
        </div>
    </div>
    <div class="bottom-part">
      <div style="display: inline-block;
        width: 50%;
        height: 90%;
        margin-right:-15px;"
      >
        <div style="color: black;margin:10px 0 0 10px;"><span style="font-weight:bold;">AB路功率</span><span style="margin-left:80px;">A路</span></div>
        <Environment style="margin-top:-10px;" class="chart" v-if="visContro.gaugeVis" width="100%" height="100%" :load-factor="resultData"/>
      </div>
      <div style="display: inline-block;
          width: 50%;
          height: 90%;">
        <div style="display:inline-block;color:black;"><span style="margin-left:100px;">B路</span></div>
        <EnvironmentCopy style="margin-top:-10px;" class="chart" v-if="visContro.gaugeVis" width="100%" height="100%" :load-factor="resultData"/>
      </div>
    </div>
  </div>
</div> 
</template>

<script setup lang="ts">

import { ref } from 'vue'
import RealTimePower from './component/RealTimePower.vue'
import PowerFactor from './component/PowerFactor.vue'
import Gauge from './component/Gauge.vue'
import MarkLine from './component/MarkLine.vue'
import Environment from './component/Environment.vue'
import PowReactiveLine from './component/PowReactiveLine.vue'
import PowActiveLine from './component/PowActiveLine.vue'
import AVol from './component/AVol.vue'
import BVol from './component/BVol.vue'
import ACur from './component/ACur.vue'
import BCur from './component/BCur.vue'
import EnvironmentCopy from './component/EnvironmentCopy.vue'
import { IndexApi } from '@/api/bus/busindex'
import { CabinetApi } from '@/api/cabinet/detail'
import { BusPowerLoadDetailApi } from '@/api/bus/buspowerloaddetail'
import { useRouter } from 'vue-router';

const router = useRouter();

const { push } = useRouter(); // 路由跳转
const message = useMessage(); // 消息弹窗
const peakDemand = ref(0);
//const peakDemandTime = ref('');
const resultData = ref() as any;
const loadRateList = ref() as any;
const selectedOption = ref('current')
const location = ref(history?.state?.location);
const busName = ref(history?.state?.cabinetName);
const id = ref(history?.state?.id);
const roomId = ref(history?.state?.roomId);
const type = ref(history?.state.type);
const loadFactorBig = ref();
const loadFactorTime = ref();
const powActiveBig = ref();
const powActiveTime = ref();
const pduBox = ref();
const visContro = ref({
  gaugeVis : false,
  loadRateVis : false,
  powActiveVis : false,
  powReactiveVis : false,
})
const keyA = ref();
const keyB = ref();
const keyAlocation = ref();
const keyBlocation = ref();

const goPDU = (devKey,location,busName,path) => {
  if(devKey == -0){
    message.error('未绑定A路设备!')
    return;    
  }
  var name= location+'-'+busName+'-'+path;
  console.log('跳转', devKey );
  push({path: '/pdu/pdudisplayscreen', query: { devKey, location: name }});
}

const goBus = (devKey,location,busName,path) => {
  if(devKey == -0){
    message.error('未绑定B路设备!')
    return;    
  }
  push({path: '/bus/busmonitor/buspowerdetail', state: { devKey ,location,busName,roomName:path}})
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
  timeGranularity: 'day',
  oldTime : getFullTimeByDate(new Date(new Date().getFullYear(),new Date().getMonth(),new Date().getDate(),0,0,0)),
  newTime : getFullTimeByDate(new Date(new Date().getFullYear(),new Date().getMonth(),new Date().getDate(),23,59,59)),
}) as any

const getRedisData = async () => {
      visContro.value.gaugeVis = false;
  console.log('visContro.value.gaugeVis',visContro.value.gaugeVis);
  const result = await CabinetApi.getCabinetdistributionDetails({
    id:id.value,
    roomId:roomId.value,
    type:type.value
  })
  loadFactorBig.value = result.loadFactorBig;
  loadFactorTime.value = result.loadFactorTime;
  powActiveBig.value = result.powActiveBig;
  powActiveTime.value = result.powActiveTime;
  location.value = result.roomName;
  busName.value = result.cabinetName;
  pduBox.value = result.pduBox;
  keyA.value = result.keyA.split('-').slice(0, 2).join('-');
  keyB.value = result.keyB.split('-').slice(0, 2).join('-');
  keyAlocation.value = result.keyA;
  keyBlocation.value = result.keyB;
  // console.log('keyA',keyAlocation.value);
  // console.log('keyB',keyBlocation.value);
  // console.log('pduBox.value',pduBox.value);
  console.log('result',result);
  resultData.value = result;
    console.log('resultData',resultData.value);
  if(resultData.value.loadFactor != null){
    visContro.value.gaugeVis = true;
  }
  
  if(resultData.value.loadFactor >= 100 ){
    resultData.value.loadFactor = 100;
  }
}

const getLoadRateList = async () =>{
    const data = await IndexApi.getBusLoadRateLine(queryParams);
    loadRateList.value = data;
    if(loadRateList.value?.time != null && loadRateList.value?.time?.length > 0){
        visContro.value.loadRateVis = true;
    }else {
        visContro.value.loadRateVis = false;
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

//刷新数据
const flashChartData = async () =>{
    await getRedisData();
    await getLoadRateList();
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
   
/** 初始化 **/
onMounted(async () => {
  // await getDetailData();
  // await getLineChartData();
  // devKeyList.value = await loadAll();
  await getRedisData();
  //await getLoadRateList();
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
    margin-left: 20px;
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
    height: 35%;
    width: 100%;
    margin-bottom: 5px
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

.bullet {
  font-size: 34px; /* 根据需要调整大小 */
  margin-right: 8px; /* 设置小圆点与后续文本之间的间距 */
}

@media screen and (min-width:2048px) {
    .centainer-height{
        height:90vh
    }

    .TransformerMonitor .center-part .left-part {
        display: inline-block;
        width: 32.5%;
        height: 100%;
        margin-right: 15px
    }
    .TransformerMonitor .center-part .right-part {
        display: inline-block;
        width: 32.4%;
        height: 100%;
        vertical-align: top;
        background-color: #fff;
        position: relative;
        margin-right:15px;
    }
    
    .TransformerMonitor .center-part .center-top-part {
        display: inline-block;
        width: 50%;
        height: 100%;
        margin-bottom: 5px;
    }
    
    .TransformerMonitor .center-part .center-top-right-part {
        display: inline-block;
        width: 50%;
        height: 100%;
        top: 30%;
        margin-bottom: 5px;
        position: absolute;
    }
    
    .TransformerMonitor .center-part .right-right-part{
        display: inline-block;
        width: 32.3%;
        height: 100%;
        vertical-align: top;
        background-color: #fff;
        margin-right:15px;
    }


    .TransformerMonitor .bottom-part {
        display: inline-block;
        height: 27%;
        width: 32.4%;
        margin-right: 15px;
        margin-top: 5px;
        margin-bottom: 5px;
        position: relative;
    }

    .header_app_text_other{
      width: 65%;
      align-content: center;
      background-color: white;
      margin-right: 5px;
    }

    .header_app_text_other1{
      align-content: center;
      background-color: white;
    }

    .label-container {
      display: flex; /* 使用 Flexbox 布局 */
      align-items: center; /* 垂直居中 */
      color:#000;
    }
}

@media screen and (max-width:2048px) and (min-width:1600px) {
    .centainer-height{
        height:93vh
    }

    .TransformerMonitor .center-part .left-part {
        display: inline-block;
        width: 32.3%;
        height: 100%;
        margin-right: 15px
    }
    .TransformerMonitor .center-part .right-part {
        display: inline-block;
        width: 32.3%;
        height: 100%;
        vertical-align: top;
        background-color: #fff;
        position: relative;
        margin-right:15px;
    }
    
    .TransformerMonitor .center-part .center-top-part {
        display: inline-block;
        width: 50%;
        height: 100%;
        margin-bottom: 5px;
    }
    
    .TransformerMonitor .center-part .center-top-right-part {
        display: inline-block;
        width: 50%;
        height: 100%;
        top: 30%;
        margin-bottom: 5px;
        position: absolute;
    }
    
    .TransformerMonitor .center-part .right-right-part{
        display: inline-block;
        width: 32%;
        height: 100%;
        vertical-align: top;
        background-color: #fff;
        margin-right:15px;
    }

    .TransformerMonitor .bottom-part {
        display: inline-block;
        height: 27%;
        width: 32.2%;
        margin-right: 15px;
        margin-top: 5px;
        margin-bottom: 5px;
        position: relative;
    }

    .header_app_text_other{
      width: 65%;
      align-content: center;
      background-color: white;
      margin-right: 5px;
    }

    .header_app_text_other1{
      align-content: center;
      background-color: white;
    }

    .label-container {
      display: flex; /* 使用 Flexbox 布局 */
      align-items: center; /* 垂直居中 */
      color:#000;
    }
}

@media screen and (max-width:1600px) {
    .centainer-height{
        height:93vh
    }

    .TransformerMonitor .center-part .left-part {
        display: inline-block;
        width: 32.3%;
        height: 100%;
        margin-right: 15px
    }
    .TransformerMonitor .center-part .right-part {
        display: inline-block;
        width: 32.3%;
        height: 100%;
        vertical-align: top;
        background-color: #fff;
        position: relative;
        margin-right:15px;
    }
    
    .TransformerMonitor .center-part .center-top-part {
        display: inline-block;
        width: 50%;
        height: 100%;
        margin-bottom: 5px;
    }
    
    .TransformerMonitor .center-part .center-top-right-part {
        display: inline-block;
        width: 50%;
        height: 100%;
        top: 30%;
        margin-bottom: 5px;
        position: absolute;
    }
    
    .TransformerMonitor .center-part .right-right-part{
        display: inline-block;
        width: 32%;
        height: 100%;
        vertical-align: top;
        background-color: #fff;
        margin-right:15px;
    }


    .TransformerMonitor .bottom-part {
        display: inline-block;
        height: 27%;
        width: 32.2%;
        margin-right: 15px;
        margin-top: 5px;
        margin-bottom: 5px;
        position: relative;
    }

    .header_app_text_other{
      width: 80%;
      align-content: center;
      background-color: white;
      margin-right: 5px;
    }

    .header_app_text_other1{
      align-content: center;
      background-color: white;

    }
    .label-container {
      display: flex; /* 使用 Flexbox 布局 */
      align-items: center; /* 垂直居中 */
      color:#000;
      margin-top: -10px;
      margin-left: -20px;
    }
}
 
.label-container span:nth-of-type(2) {
  display: inline-block;
  width: 120px;
  vertical-align: top;
}
 
.label-container span:last-of-type {
  display: inline-block;
  margin-left: 10px;
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
    width: 14%;
    height: 100%;
    min-width: 155px;
    padding-left: 20px;
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

.TransformerMonitor .bottom-part .bottomLineDiv #transformLine1,.TransformerMonitor .bottom-part .bottomLineDiv #transformLine2,.TransformerMonitor .bottom-part .bottomLineDiv #transformLine3 {
    width: 100%;
    height: 100%;
}

.noData {
    color: #fff;
    text-align: center;
    line-height: 200px;
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
  height: 30px;
  padding-left: 10px;
  box-shadow: 20px;
}
.header_app_text_right{                     
  background-color: white;
  width: 20%;
  align-content: center;
  color:#606266;
}  
.header_app_text_left{                     
  background-color: white;
  width: 180%;
  align-content: center;
  color:#606266;
}                                                       
</style>
