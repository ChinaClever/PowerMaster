<template>
  <div class="TransformerMonitor">
    <div class="center-part">
      <div class="left-part">
        <el-tag size="large" >{{location}}</el-tag>
        <Gauge class="chart" v-if="visContro.gaugeVis" width="100%" height="100%" :load-factor="redisData.loadFactor" />
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
            <span >负载</span>
            <span >电流</span>
            <span >电压</span>
            <span >温度</span>
            <span >总谐波含有率</span>
          </div>
          <div  class="block-part">
            <div  class="content-part">
              <p >额定容量<span  class="vale-part">{{redisData?.finstalledCapacity}}</span>kVA </p>
              <p >视在功率<span  class="vale-part">{{redisData?.s}}</span>kVA</p>
              <p >有功功率<span  class="vale-part">{{redisData?.p}}</span>kW</p>
              <p >无功功率<span  class="vale-part">{{redisData?.q}}</span>kVar</p>
              <p >最大需量<span  class="vale-part">{{redisData?.md}}</span>kW </p>
              <p >{{redisData?.updateTime}}</p>
            </div>
            <div  class="content-part">
              <p  class="AColor">A相 <span  class="vale-part AColor">{{redisData?.ia}}</span>A </p>
              <p  class="BColor">B相 <span  class="vale-part BColor">{{redisData?.ib}}</span>A </p>
              <p  class="CColor">C相 <span  class="vale-part CColor">{{redisData?.ic}}</span>A </p>
            </div>
            <div  class="content-part">
              <p  class="AColor">Uab <span  class="vale-part AColor">{{redisData?.uab}}</span>V</p>
              <p  class="BColor">Ubc <span  class="vale-part BColor">{{redisData?.ubc}}</span>V</p>
              <p  class="CColor">Uca <span  class="vale-part CColor">{{redisData?.uca}}</span>V</p>
              <p  class="AColor">Ua <span  class="vale-part AColor">{{redisData?.ua}}</span>V </p>
              <p  class="BColor">Ub <span  class="vale-part BColor">{{redisData?.ub}}</span>V </p>
              <p  class="CColor">Uc <span  class="vale-part CColor">{{redisData?.uc}}</span>V </p>
            </div>
            <div  class="content-part">
              <p  class="AColor">A相温度 <span  class="vale-part AColor">{{redisData?.tempA}}</span>℃</p>
              <p  class="BColor">B相温度 <span  class="vale-part BColor">{{redisData?.tempB}}</span>℃</p>
              <p  class="CColor">C相温度 <span  class="vale-part CColor">{{redisData?.tempC}}</span>℃</p>
              <p  class="CColor">N相温度 <span  class="vale-part CColor">{{redisData?.tempN}}</span>℃</p>
            </div><!---->
            <div  class="content-part">
              <p  class="AColor">A相电流 <span  class="vale-part AColor">{{redisData?.iaTHD}}</span>%</p>
              <p  class="BColor">B相电流 <span  class="vale-part BColor">{{redisData?.ibTHD}}</span>%</p>
              <p  class="CColor">C相电流 <span  class="vale-part CColor">{{redisData?.icTHD}}</span>%</p>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div  class="bottom-part">
      <div  class="bottomLineDiv">
        <p >负载率曲线</p>
        <MarkLine v-if="visContro.loadRateVis"  width="100%" height="100%" :list="loadRateList"/>
      </div>
      <div  class="bottomLineDiv">
        <p >有功功率</p>
        <PowActiveLine v-if="visContro.powActiveVis"  width="100%" height="100%" :list="powActiveList"/>
      </div>
      <div  class="bottomLineDiv">
        <p >无功功率</p>
        <PowReactiveLine v-if="visContro.powReactiveVis"  width="100%" height="100%" :list="powReactiveList"/>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">

import { ref } from 'vue'
import Gauge from './component/Gauge.vue'
import MarkLine from './component/MarkLine.vue'
import PowReactiveLine from './component/PowReactiveLine.vue'
import PowActiveLine from './component/PowActiveLine.vue'
import { IndexApi } from '@/api/bus/boxindex'

const redisData = ref() as any;
const loadRateList = ref() as any;
const powActiveList = ref() as any;
const powReactiveList = ref() as any;
const location = ref(history?.state?.location)
const visContro = ref({
  gaugeVis : false,
  loadRateVis : false,
  powActiveVis : false,
  powReactiveVis : false,
})

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

const queryParams = reactive({
  pageNo: 1,
  pageSize: 24,
  devKey: history?.state?.devKey,
  boxId : history?.state?.boxId,
  createTime: [],
  cascadeNum: undefined,
  serverRoomData:undefined,
  status:[],
  cabinetIds:[],
  timeType : 0,
  timeArr:[],
  oldTime : getFullTimeByDate(new Date(new Date().getFullYear(),new Date().getMonth(),new Date().getDate(),0,0,0)),
  newTime : getFullTimeByDate(new Date(new Date().getFullYear(),new Date().getMonth(),new Date().getDate(),23,59,59)),
}) as any

const getRedisData = async () => {
  const data =  await IndexApi.getBoxPowerRedisData(queryParams);
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
}

const getLoadRateList = async () =>{
    const data = await IndexApi.getBoxLoadRateLine(queryParams);
    loadRateList.value = data;
    if(loadRateList.value?.time != null && loadRateList.value?.time?.length > 0){
        visContro.value.loadRateVis = true;
    }else {
        visContro.value.loadRateVis = false;
    }
}

const getBusPowActiveList = async () =>{
    const data = await IndexApi.getBoxPowActiveLine(queryParams);
    powActiveList.value = data;
    if(powActiveList.value?.time != null && powActiveList.value?.time?.length > 0){
        visContro.value.powActiveVis = true;
    }else {
        visContro.value.powActiveVis = false;
    }
}

const getBusPowReactiveList = async () =>{
    const data = await IndexApi.getBoxPowReactiveLine(queryParams);
    powReactiveList.value = data;
    if(powReactiveList.value?.time != null && powReactiveList.value?.time?.length > 0){
        visContro.value.powReactiveVis = true;
    }else {
        visContro.value.powReactiveVis = false;
    }
}
   
/** 初始化 **/
onMounted(async () => {
  // await getDetailData();
  // await getLineChartData();
  await getRedisData();
  await getLoadRateList();
  await getBusPowActiveList();
  await getBusPowReactiveList();
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
    padding: 0 5px
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
    height: 50%;
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
    color: #1e9fe9  
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
    width: 17%;
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
    width: 17%;
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

.TransformerMonitor .bottom-part {
    height: calc(50% - 60px);
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
    vertical-align: top
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

@media screen and (max-width: 2160px) {
    .TransformerMonitor .center-part .center-top-part .div-part .div-part1 .middletxt,.TransformerMonitor .center-part .center-top-part .div-part .div-part2 .middletxt,.TransformerMonitor .center-part .center-top-part .div-part .div-part3 .middletxt,.TransformerMonitor .center-part .center-top-part .div-part .div-part4 .middletxt {
        font-size:30px
    }

    .TransformerMonitor .bottom-part .bottomLineDiv p,.TransformerMonitor .center-part .center-bottom-part .block-part .content-part p,.TransformerMonitor .center-part .center-top-part .div-part p {
        font-size: 22px
    }

    .TransformerMonitor .center-part .center-bottom-part .top-part {
        height: 60px
    }

    .TransformerMonitor .center-part .center-bottom-part .top-part span {
        line-height: 60px;
        font-size: 25px
    }

    .TransformerMonitor .center-part .center-bottom-part .block-part .content-part p {
        margin: 8px 0
    }

    .TransformerMonitor .center-part .center-bottom-part .block-part .content-part .vale-part {
        width: 66px
    }
}

@media screen and (max-width: 1920px) {
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
        line-height: 40px;
        font-size: 16px
    }

    .TransformerMonitor .center-part .center-bottom-part .block-part .content-part .vale-part {
        width: 48px
    }
}

@media screen and (max-width: 1680px) {
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
    background: #fff
}

body .TransformerMonitor .center-part .center-bottom-part .top-part span {
    color: #000
}

body .TransformerMonitor .bottom-part .bottomLineDiv p,body .TransformerMonitor .bottom-part .bottomLineDiv p {
    border-left: 2px solid #fff;
    background: #01ada8;
    color: #fff
}

body .TransformerMonitor .center-part .center-bottom-part .top-part,body .TransformerMonitor .center-part .center-bottom-part .top-part {
    background: #01ada8
}

body .TransformerMonitor .center-part .center-bottom-part .top-part span,body .TransformerMonitor .center-part .center-bottom-part .top-part span {
    color: #fff
}

</style>