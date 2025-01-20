<template>
<div style="background-color: #E7E7E7;" class="centainer-height">
  <div class="TransformerMonitor">
    <div class="center-part">
      <div class="left-part">
        <!-- <el-tag size="large">{{ location }}</el-tag> -->
        <div style="height:20px;display:flex;align-items: center;margin:10px 0 10px 10px;">              
            <span style="color:black;font-weight:bold;height:20px;width:100px;">负载率</span>
            <div style="margin-left:100px;">
                <span style="color:black;font-size:14px;margin-right:10px;">最大负载率: {{redisData?.loadFactorValue}}</span>
                <span style="color:black;font-size:14px;">发生时间: {{redisData?.loadFactorTime}}</span>
            </div>
        </div>
        <!--<div style="height:20px;display:flex;align-items: center;margin-left:10px">              
            <span style="color:#ccc;font-size:14px;">最大需量：<span  class="vale-part BColor" >{{peakDemand}}</span>kVA</span>
        </div>
        <div style="height:20px;display:flex;align-items: center;margin-left:10px;">              
            <span style="color:#ccc;font-size:14px;">发生时间：{{}}</span>
        </div>
        <div style="height:20px;display:flex;align-items: center;margin-left:10px;">              
            <span style="color:#ccc;font-size:14px;border-bottom:1px solid #ccc;width:90%;"></span>
        </div>-->
        <div style="height:340px;width:100%;margin-top:-30px;">
            <Gauge class="chart" v-if="visContro.gaugeVis" width="100%" height="100%" :data="redisData.loadFactor" />
        </div>
        <!--<div style="position: relative; top: -80px; left: 0; width: 100%; text-align: center; padding-top: 10px;">
            <div style="color: black;font-size: 30px;">{{redisData?.loadFactor}}</div>
            <div style="color: black;">负载率（%）</div>
        </div>-->
        <p v-if="!visContro.gaugeVis" class="noData">暂无数据</p>
      </div>
      <div class="right-part">
        <div class="center-top-part">
          <div style="color: black;margin:10px 0 0 10px;font-weight: bold;">总功率</div>
          <RealTimePower style="margin-top:-10px;" class="chart" v-if="visContro.gaugeVis" width="100%" height="100%" :load-factor="redisData"/>
        </div>
        <div class="center-top-right-part">
          <div style="margin-top:-70px;margin-bottom:50px;margin-left:-50px;">
            <span style="color:black;font-size:14px;margin-right:10px;">最大功率: {{redisData?.powActiveValue}}KW</span>
            <span style="color:black;font-size:14px;">发生时间: {{redisData?.powActiveTime}}</span>
          </div>
          <div class="label-container">
            <span class="bullet" style="color:#B47660;">•</span><span style="width:80px;font-size:14px;">额定功率:</span><span style="font-size:16px;">{{redisData?.powApparentTotal}}KVA</span>
          </div>
          <div class="label-container">
            <span class="bullet" style="color:#C8603A;">•</span><span style="width:80px;font-size:14px;">视在功率:</span><span style="font-size:16px;">{{redisData?.totalPowApparent}}kVA</span>
          </div>
          <div class="label-container">
            <span class="bullet" style="color:#AD3762;">•</span><span style="width:80px;font-size:14px;">有功功率:</span><span style="font-size:16px;">{{redisData?.totalPowActive}}kW</span>
          </div>
          <div class="label-container">
            <span class="bullet" style="color:#E5B849;">•</span><span style="width:80px;font-size:14px;">无功功率:</span><span style="font-size:16px;">{{redisData?.totalPowReactive}}kVar</span>
          </div>
        </div>
      </div>
      <div class="right-right-part" style="position: relative;">
        <div style="display: inline-block;
        width: 50%;
        height: 100%;">
          <div style="color: black;font-weight:bold;margin:10px;">温度</div>
          <TemValue style="margin-top:-30px;" class="chart" v-if="visContro.gaugeVis" width="100%" height="100%" :load-factor="redisData"/>
        </div>
        <div style="display: inline-block;
            position: absolute;
            width: 50%;
            height: 100%;
            top: 70px;">
          <div class="label-container">
            <span class="bullet" style="color:#B47660;">•</span><span style="width:50px;font-size:14px;">A温度:</span><span style="font-size:16px;">{{redisData?.temValue[0]}}℃</span>
          </div>
          <div class="label-container">
            <span class="bullet" style="color:#C8603A;">•</span><span style="width:50px;font-size:14px;">B温度:</span><span style="font-size:16px;">{{redisData?.temValue[1]}}℃</span>
          </div>
          <div class="label-container">
            <span class="bullet" style="color:#AD3762;">•</span><span style="width:50px;font-size:14px;">C温度:</span><span style="font-size:16px;">{{redisData?.temValue[2]}}℃</span>
          </div>
          <div class="label-container">
            <span class="bullet" style="color:#E5B849;">•</span><span style="width:50px;font-size:14px;">N温度:</span><span style="font-size:16px;">{{redisData?.temValue[3]}}℃</span>
          </div>
        </div>
      </div>
    </div>
    <div class="bottom-part">
      <div style="display: inline-block;
        width: 50%;
        height: 100%;">
          <div style="color: black;margin:10px 0 0 10px;font-weight:bold;">相电压</div>
          <AVol style="margin-top:-30px;" class="chart" v-if="visContro.gaugeVis" width="100%" height="100%" :load-factor="redisData"/>
        </div>
        <div style="display: inline-block;
            position: absolute;
            width: 50%;
            height: 100%;
            top: 50px;">
          <div class="label-container">
            <span class="bullet" style="color:#E5B849;">•</span><span style="width:50px;font-size:14px;">Ua:</span><span style="font-size:16px;">{{redisData?.lineVolValue[0]}}V</span>
          </div>
          <div class="label-container">
            <span class="bullet" style="color:#C8603A;">•</span><span style="width:50px;font-size:14px;">Ub:</span><span style="font-size:16px;">{{redisData?.lineVolValue[1]}}V</span>
          </div>
          <div class="label-container">
            <span class="bullet" style="color:#AD3762;">•</span><span style="width:50px;font-size:14px;">Uc:</span><span style="font-size:16px;">{{redisData?.lineVolValue[2]}}V</span>
          </div>
        </div>
    </div>
    <div class="bottom-part">
      <div style="display: inline-block;
        width: 50%;
        height: 100%;">
          <div style="color: black;margin:10px 0 0 10px;font-weight:bold;">相电流</div>
          <ACur style="margin-top:-10px;" class="chart" v-if="visContro.gaugeVis" width="100%" height="100%" :load-factor="redisData"/>
        </div>
        <div style="display: inline-block;
            position: absolute;
            width: 50%;
            height: 100%;
            top: 50px;">
          <div class="label-container">
            <span class="bullet" style="color:#075F71;">•</span><span style="width:50px;font-size:14px;">Ia</span><span style="font-size:16px;">{{redisData?.lineCurValue[0]}}A</span>
          </div>
          <div class="label-container">
            <span class="bullet" style="color:#119CB5;">•</span><span style="width:50px;font-size:14px;">Ib</span><span style="font-size:16px;">{{redisData?.lineCurValue[1]}}A</span>
          </div>
          <div class="label-container">
            <span class="bullet" style="color:#45C0C9;">•</span><span style="width:50px;font-size:14px;">Ic</span><span style="font-size:16px;">{{redisData?.lineCurValue[2]}}A</span>
          </div>
        </div>
    </div>
    <div class="bottom-part">
      <div style="display: inline-block;
        width: 50%;
        height: 230px;">
          <div style="color: black;margin:10px 0 0 10px;font-weight:bold;">功率因数</div>
          <PowerFactor style="margin-top:-10px;" class="chart" v-if="visContro.gaugeVis" width="100%" height="100%" :load-factor="redisData"/>
        </div>
        <div style="display: inline-block;
            position: absolute;
            width: 50%;
            height: 100%;
            top: 20px;">
          <div class="label-container">
            <span class="bullet" style="color:#B47660;">•</span><span style="width:80px;font-size:14px;">功率因数:</span><span style="font-size:16px;">{{redisData?.totalPowerFactor.toFixed(2)}}</span>
          </div>
          <div class="label-container">
            <span class="bullet" style="color:#C8603A;">•</span><span style="width:80px;font-size:14px;">A相:</span><span style="font-size:16px;">{{redisData?.linePowerFactor[0].toFixed(2)}}</span>
          </div>
          <div class="label-container">
            <span class="bullet" style="color:#AD3762;">•</span><span style="width:80px;font-size:14px;">B相:</span><span style="font-size:16px;">{{redisData?.linePowerFactor[1].toFixed(2)}}</span>
          </div>
          <div class="label-container">
            <span class="bullet" style="color:#E5B849;">•</span><span style="width:80px;font-size:14px;">C相:</span><span style="font-size:16px;">{{redisData?.linePowerFactor[2].toFixed(2)}}</span>
          </div>
        </div>
    </div>
    <div class="bottom-part">
      <div style="display: inline-block;
        width: 50%;
        height: 100%;">
          <div style="color: black;margin:10px 0 0 10px;font-weight:bold;">输出位1</div>
          <OutputOne style="margin-top:-10px;" class="chart" v-if="visContro.gaugeVis" width="100%" height="100%" :load-factor="redisData"/>
        </div>
        <div style="display: inline-block;
            position: absolute;
            width: 50%;
            height: 100%;
            top: 30px;">
          <div class="label-container">
            <span class="bullet" style="color:#B47660;">•</span><span style="width:80px;font-size:14px;">有功功率</span><span style="font-size:16px;">{{redisData?.linePowActive[0]}}kW</span>
          </div>
          <div class="label-container">
            <span class="bullet" style="color:#C8603A;">•</span><span style="width:80px;font-size:14px;">无功功率</span><span style="font-size:16px;">{{redisData?.linePowReactive[0]}}kVar</span>
          </div>
          <div class="label-container">
            <span class="bullet" style="color:#AD3762;">•</span><span style="width:80px;font-size:14px;">视在功率</span><span style="font-size:16px;">{{redisData?.linePowApparent[0]}}kVA</span>
          </div>
          <div class="label-container">
            <span class="bullet" style="color:#E5B849;">•</span><span style="width:80px;font-size:14px;">功率因数:</span><span style="font-size:16px;">{{redisData?.linePowerFactor[0].toFixed(2)}}</span>
          </div>
        </div>
    </div>
    <div class="bottom-part">
      <div style="display: inline-block;
        width: 50%;
        height: 100%;">
          <div style="color: black;margin:10px 0 0 10px;font-weight:bold;">输出位2</div>
          <OutputTwo style="margin-top:-10px;" class="chart" v-if="visContro.gaugeVis" width="100%" height="100%" :load-factor="redisData"/>
        </div>
        <div style="display: inline-block;
            position: absolute;
            width: 50%;
            height: 100%;
            top: 30px;">
          <div class="label-container">
            <span class="bullet" style="color:#B47660;">•</span><span style="width:80px;font-size:14px;">有功功率</span><span style="font-size:16px;">{{redisData?.linePowActive[1]}}kW</span>
          </div>
          <div class="label-container">
            <span class="bullet" style="color:#C8603A;">•</span><span style="width:80px;font-size:14px;">无功功率</span><span style="font-size:16px;">{{redisData?.linePowReactive[1]}}kVar</span>
          </div>
          <div class="label-container">
            <span class="bullet" style="color:#AD3762;">•</span><span style="width:80px;font-size:14px;">视在功率</span><span style="font-size:16px;">{{redisData?.linePowApparent[1]}}kVA</span>
          </div>
          <div class="label-container">
            <span class="bullet" style="color:#E5B849;">•</span><span style="width:80px;font-size:14px;">功率因数:</span><span style="font-size:16px;">{{redisData?.linePowerFactor[1].toFixed(2)}}</span>
          </div>
        </div>
    </div>
    <div class="bottom-part">
      <div style="display: inline-block;
        width: 50%;
        height: 100%;">
          <div style="color: black;margin:10px 0 0 10px;font-weight:bold;">输出位3</div>
          <OutputThree style="margin-top:-10px;" class="chart" v-if="visContro.gaugeVis" width="100%" height="100%" :load-factor="redisData"/>
        </div>
        <div style="display: inline-block;
            position: absolute;
            width: 50%;
            height: 100%;
            top: 30px;">
          <div class="label-container">
            <span class="bullet" style="color:#B47660;">•</span><span style="width:80px;font-size:14px;">有功功率</span><span style="font-size:16px;">{{redisData?.linePowActive[2]}}kW</span>
          </div>
          <div class="label-container">
            <span class="bullet" style="color:#C8603A;">•</span><span style="width:80px;font-size:14px;">无功功率</span><span style="font-size:16px;">{{redisData?.linePowReactive[2]}}kVar</span>
          </div>
          <div class="label-container">
            <span class="bullet" style="color:#AD3762;">•</span><span style="width:80px;font-size:14px;">视在功率</span><span style="font-size:16px;">{{redisData?.linePowApparent[2]}}kVA</span>
          </div>
          <div class="label-container">
            <span class="bullet" style="color:#E5B849;">•</span><span style="width:80px;font-size:14px;">功率因数:</span><span style="font-size:16px;">{{redisData?.linePowerFactor[2].toFixed(2)}}</span>
          </div>
        </div>
    </div>
    <div style="width:98.5%;heigth:100%;">
      <div style="margin-top:30px;background-color:#fff;font-weight:bold;">
        <span style="margin-left:10px;">回路数据</span>
      </div>
      <el-table :data="tableData" border style="width: 100%;">
        <el-table-column align="center" prop="loopId" label="编号" width="100px" />
        <el-table-column label="断路器状态" prop="breakerStatus" align="center" width="450px" />
          <!--<el-table-column label="A相" align="center" prop="acur" width="150px">
            <template #default="scope">
              <el-text line-clamp="2" v-if="scope.row.acur != null">
                {{ scope.row.acur.toFixed(2) }}
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="B相" align="center" prop="bcur" width="150px">
            <template #default="scope">
              <el-text line-clamp="2" v-if="scope.row.bcur != null">
                {{ scope.row.bcur.toFixed(2) }}
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="C相" align="center" prop="ccur" width="150px">
            <template #default="scope">
              <el-text line-clamp="2" v-if="scope.row.ccur != null">
                {{ scope.row.ccur.toFixed(2) }}
              </el-text>
            </template>
          </el-table-column>
        </el-table-column>-->
        <el-table-column align="center" prop="loopVolValue" label="电压" width="150"/>
        <el-table-column align="center" prop="loopCurValue" label="电流" width="150" />
        <el-table-column align="center" prop="loopPowValue" label="有功功率" width="150" />
        <el-table-column align="center" prop="loopPowReactive" label="无功功率" width="150" />
        <el-table-column align="center" prop="loopPowApparent" label="视在功率" width="150"/>
        <el-table-column align="center" prop="loopPowerFactor" label="功率因数" width="150" />
        <el-table-column align="center" prop="loopEleActive" label="电能" />
      </el-table>
    </div>
  </div>
</div> 
</template>

<script setup lang="ts">

import { ref } from 'vue'
import Gauge from './component/Gauge.vue'
import RealTimePower from './component/RealTimePower.vue'
import Environment from './component/Environment.vue'
import AVol from './component/AVol.vue'
import BVol from './component/BVol.vue'
import ACur from './component/ACur.vue'
import BCur from './component/BCur.vue'
import EnvironmentCopy from './component/EnvironmentCopy.vue'
import PowerFactor from './component/PowerFactor.vue'
import OutputOne from './component/OutputOne.vue'
import OutputTwo from './component/OutputTwo.vue'
import OutputThree from './component/OutputThree.vue'
import TemValue from './component/TemValue.vue'
import { IndexApi } from '@/api/bus/boxindex'
import { CabinetApi } from '@/api/cabinet/detail'
import { BusPowerLoadDetailApi } from '@/api/bus/buspowerloaddetail'

const redisData = ref() as any;
const peakDemand = ref(0);
const peakDemandTime = ref('');
const resultData = ref() as any;
const loadRateList = ref() as any;
const selectedOption = ref('current')
const location = ref(history?.state?.location);
const busName = ref(history?.state?.busName);
const id = ref(history?.state?.id);
const roomId = ref(history?.state?.roomId);
const type = ref(history?.state.type);
const visContro = ref({
  gaugeVis : false,
  loadRateVis : false,
  powActiveVis : false,
  powReactiveVis : false,
})
const tableData = ref()

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
  timeGranularity: 'hour',
}) as any


const getRedisData = async () => {
  const data =  await IndexApi.getBoxPowerRedisData(queryParams);
  console.log('data',data);
  //let loopItem = {} as any;
  //for (let key in data) {  
  //  if (data.hasOwnProperty(key)) {  
  //      // 排除null值，但保留updateTime  
  //      if (data[key] !== null && key != 'updateTime') { 
  //          loopItem[key] = data[key].toFixed(2); // 注意这将转换为字符串  
  //      }else if (data[key] == null){
  //          loopItem[key] = '/';
  //      }
  //  }  
  //}
  //loopItem['updateTime'] = data['updateTime'];
  redisData.value = data;
  tableData.value = redisData.value.boxLoopItemResVO;

  tableData.value = tableData.value.map(item => {
    let newItem = { ...item }; // 创建对象的浅拷贝
    if (newItem.breakerStatus === 1) {
      newItem.breakerStatus = '断开';
    } else if (newItem.breakerStatus === 2) {
      newItem.breakerStatus = '闭合';
    }
    return newItem;
  });

  if(redisData.value.loadFactor != null){
    visContro.value.gaugeVis = true;
  }
}

//刷新数据
const flashChartData = async () =>{
    await getRedisData();
}

const handleQuery = async () => {
  queryParams.devKey = queryParamsSearch.devKey;
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
   
/** 初始化 **/
onMounted(async () => {
  devKeyList.value = await loadAll();
  await getRedisData();
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
        margin-bottom: 5px;
        top:80px;
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
        height:150vh
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
        top:80px;
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
        top:80px;
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
</style>