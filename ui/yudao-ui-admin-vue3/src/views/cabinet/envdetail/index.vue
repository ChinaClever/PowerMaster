<template>
  <CommonMenu :showCheckbox="false"  @node-click="handleClick" :showSearch="true" :dataList="serverRoomArr" navTitle="机柜环境详情">
    <template #NavInfo>
      <div >
        <div class="header">
          <div class="header_img"><img alt="" src="@/assets/imgs/temhum.png" /></div>
          <div class="name">温湿度</div>
        </div>
        <div class="line"></div>
        <!-- <div class="status">
          <div class="box">
            <div class="top">
              <div class="tag"></div>&lt;15%
            </div>
            <div class="value"><span class="number">{{statusNumber.lessFifteen}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag empty"></div>小电流
            </div>
            <div class="value"><span class="number">{{statusNumber.smallCurrent}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag warn"></div>15%-30%
            </div>
            <div class="value"><span class="number">{{statusNumber.greaterFifteen}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag error"></div>&gt;30
            </div>
            <div class="value"><span class="number">{{statusNumber.greaterThirty}}</span>个</div>
          </div>
        </div> -->
        <!-- <div class="line"></div>
        <div class="overview">
          <div class="count">
            <img class="count_img" alt="" src="@/assets/imgs/dn.jpg" />
            <div class="info">
              <div>总电能</div>
              <div class="value">295.87 kW·h</div>
            </div>
          </div>
          <div class="count">
            <img class="count_img" alt="" src="@/assets/imgs/dh.jpg" />
            <div class="info">
              <div>今日用电</div>
              <div class="value">295.87 kW·h</div>
            </div>
          </div>
          <div class="count">
            <img class="count_img" alt="" src="@/assets/imgs/dn.jpg" />
            <div class="info">
              <div>今日用电</div>
              <div class="value">295.87 kW·h</div>
            </div>
          </div>
        </div> -->
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
        
        <!-- <el-form-item label="网络地址" prop="devKey">
          <el-input
            v-model="queryParams.devKey"
            placeholder="请输入网络地址"
            clearable
            @keyup.enter="handleQuery"
            class="!w-240px"
          />
        </el-form-item> -->

        <el-form-item label="机柜Id" prop="ipAddr" >
          <el-input
            v-model="queryParams.id"
            placeholder="请输入IP"
            clearable
            class="!w-140px"
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
      <el-row v-show="visControll.visAllReport" :gutter="24" >
        <el-col :span="9">
          <el-card >
            <template #header>
              <div class="card-header">
                <el-row :gutter="24">
                  <el-col :span="12">
                    <div style="float:left">
                      <h2>前门</h2>
                    </div>
                  </el-col>
                  <el-col :span="12">
                    <div style="float:right">
                      <h2>后门</h2>
                    </div>
                  </el-col>
                </el-row>
              </div>
            </template>
            <el-row justify="center">
              <h1>上层</h1>
            </el-row>
            <el-row :gutter="24">
              <el-col :span="12">
                <div style="float:left">
                  {{  cabinetEnvData?.iceTopTem != null ? "温度:" + cabinetEnvData?.iceTopTem + "°C" : ''}}
                  <br/>
                  {{  cabinetEnvData?.iceTopHum != null ? "湿度:" + cabinetEnvData?.iceTopHum + "%" : ''}}
                </div>
              </el-col>
              <el-col :span="12">
                <div style="float:right">
                  {{  cabinetEnvData?.hotTopTem != null ? "温度:" + cabinetEnvData?.hotTopTem + "°C" : ''}}
                  <br/>
                  {{  cabinetEnvData?.hotTopHum != null ? "湿度:" + cabinetEnvData?.hotTopHum + "%"  : ''}}
                </div>
              </el-col>
            </el-row>
            <el-row justify="center">
              <h1>中层</h1>
            </el-row>
            <el-row :gutter="24">
              <el-col :span="12">
                <div style="float:left">
                  {{  cabinetEnvData?.iceMidTem != null ? "温度:" + cabinetEnvData?.iceMidTem + "°C" : ''}}
                  <br/>
                  {{  cabinetEnvData?.iceMidHum != null ? "湿度:" + cabinetEnvData?.iceMidHum + "%" : ''}}
                </div>
              </el-col>
              <el-col :span="12">
                <div style="float:right">
                  {{  cabinetEnvData?.hotMidTem != null ? "温度:" + cabinetEnvData?.hotMidTem + "°C" : ''}}
                  <br/>
                  {{  cabinetEnvData?.hotMidHum != null ? "湿度:" + cabinetEnvData?.hotMidHum + "%" : ''}}
                </div>
              </el-col>
            </el-row>
            <el-row justify="center">
              <h1 >下层</h1>
            </el-row>
            <el-row :gutter="24">
              <el-col :span="12">
                <div style="float:left">
                  {{  cabinetEnvData?.iceBomTem != null ? "温度:" + cabinetEnvData?.iceBomTem + "°C" : ''}}
                  <br/>
                  {{  cabinetEnvData?.iceBomHum != null ? "湿度:" + cabinetEnvData?.iceBomHum + "%" : ''}}
                </div>
              </el-col>
              <el-col :span="12">
                <div style="float:right">
                  {{  cabinetEnvData?.hotBomTem != null ? "温度:" + cabinetEnvData?.hotBomTem + "°C" : ''}}
                  <br/>
                  {{  cabinetEnvData?.hotBomHum != null ? "湿度:" + cabinetEnvData?.hotBomHum + "%" : ''}}
                </div>
              </el-col>
            </el-row>
          </el-card>
          <el-card >

            <el-row :gutter="24">
              <el-col :span="13">
                <div style="float:left">
                  门禁状态：
                </div>
                
              </el-col>
              <el-col :span="11">
                <div >
                  门禁状态：未检测出状态
                </div>
                
              </el-col>
            </el-row>
          </el-card>
          <el-card >

            <el-row :gutter="24">
              <el-col :span="13">
                <div style="float:left">
                  水浸状态：
                </div>
                
              </el-col>
              <el-col :span="11">
                <div >
                  水浸状态：
                </div>
                
              </el-col>
            </el-row>
          </el-card>
          <el-card >

            <el-row :gutter="24">
              <el-col :span="13">
                <div style="float:left">
                  烟雾状态：
                </div>
                
              </el-col>
              <el-col :span="11">
                <div >
                  烟雾状态：
                </div>
                
              </el-col>
            </el-row>
          </el-card>
        </el-col>
        <el-col :span="15">

          <el-row>
            <el-card>
              <el-row>
                <el-col >
                  <h1 style="width: 100%">前门</h1>
                </el-col>
                <el-col>                    
                  <el-form>
                    <el-form-item >
                      <el-button @click="iceTemAndHumSwitchValue = 1;" :type="iceTemAndHumSwitchValue == 1 ? 'primary' : ''">温度</el-button>
                      <el-button @click="iceTemAndHumSwitchValue = 2;" :type="iceTemAndHumSwitchValue == 2 ? 'primary' : ''">湿度</el-button>                      
                    </el-form-item>
                    <el-form-item style="margin-bottom: 10px;">
                      <el-button @click="queryParams.iceTimeType = 1;queryParams.timeArr = null;queryParams.id ? handleIceQuery() : ''" :type="queryParams.iceTimeType == 1 ? 'primary' : ''">过去24小时</el-button>
                      <el-button @click="queryParams.iceTimeType = 2;" :type="queryParams.iceTimeType == 2 ? 'primary' : ''">自定义</el-button>                      
                      <el-date-picker
                        v-if="queryParams.iceTimeType == 2"
                        v-model="queryParams.timeArr"
                        value-format="YYYY-MM-DD HH:mm:ss"
                        type="daterange"
                        start-placeholder="开始日期"
                        end-placeholder="结束日期"
                        :disabled-date="disabledDate"
                        @change="handleIceDayPick"
                        class="!w-200px"
                      />
                    </el-form-item>
                  </el-form>                                                  
                </el-col> 
              </el-row>
              <div style="display: flex; justify-content: center; align-items: center;">
                <div ref="iceTemOrHumChartContainer" id="iceTemOrHumChartContainer" style="width: 52vw; height: 38vh;"></div>
              </div>                
            </el-card>
          </el-row>
          <el-row>
            <el-card>
              <el-row>
                <el-col >
                  <h1 style="width: 100%">后门</h1>
                </el-col>
                <el-col >
                  <el-form>          
                    <el-form-item>
                      <el-button @click="hotTemAndHumSwitchValue = 1;" :type="hotTemAndHumSwitchValue == 1 ? 'primary' : ''">温度</el-button>
                      <el-button @click="hotTemAndHumSwitchValue = 2;" :type="hotTemAndHumSwitchValue == 2 ? 'primary' : ''">湿度</el-button>                      
                    </el-form-item>
                    <div style="float:right;margin-top: 0;">
                      <el-form-item>
                        <el-button @click="queryParams.hotTimeType = 1;queryParams.timeArr = null;queryParams.id ? handleHotQuery() : ''" :type="queryParams.hotTimeType == 1 ? 'primary' : ''">过去24小时</el-button>
                        <el-button @click="queryParams.hotTimeType = 2;" :type="queryParams.hotTimeType == 2 ? 'primary' : ''">自定义</el-button>                      
                        <el-date-picker
                          v-if="queryParams.hotTimeType == 2"
                          v-model="queryParams.timeArr"
                          value-format="YYYY-MM-DD HH:mm:ss"
                          type="daterange"
                          start-placeholder="开始日期"
                          end-placeholder="结束日期"
                          :disabled-date="disabledDate"
                          @change="handleHotDayPick"
                          class="!w-200px"
                        />
                      </el-form-item>
                    </div>
                  </el-form>                        
                </el-col> 
              </el-row>
              <div style="display: flex; justify-content: center; align-items: center;">
                <div ref="hotTemOrHumChartContainer" id="hotTemOrHumChartContainer" style="width: 52vw; height: 38vh;"></div>
              </div>                
            </el-card>
          </el-row>
        </el-col>
      </el-row>
    </template>
  </CommonMenu>

  <!-- 表单弹窗：添加/修改 -->
  <!-- <PDUDeviceForm ref="formRef" @success="getList" /> -->
</template>

<script setup lang="ts">
// import download from '@/utils/download'
import { IndexApi } from '@/api/cabinet/index'
import * as echarts from 'echarts';
import { CabinetApi } from '@/api/cabinet/info'
import { ElTree } from 'element-plus'
import router from '@/router';
/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })

const switchValue = ref(1);
const iceTemAndHumSwitchValue = ref(1)
const hotTemAndHumSwitchValue = ref(1)
const instance = getCurrentInstance();
const visControll = reactive({
  visAllReport : true,
  isSameDay : false,
  eqVis : false,
  powVis : false,
  outletVis : false,
  temVis : false,
  ApowVis :false,
  BpowVis : false,

})



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

const handleIceDayPick = () => {
  if(queryParams?.oldTime && queryParams.iceTimeType == 2){

    queryParams.oldTime = null;
    queryParams.newTime = null;
  }

 if (queryParams.timeArr && queryParams.iceTimeType == 2) {

    queryParams.oldTime = queryParams.timeArr[0];
    queryParams.newTime = queryParams.timeArr[1].split(" ")[0]+ " " + "23:59:59";
    if(queryParams.id != null){

      handleIceQuery();
    }
  }
}

const handleHotDayPick = () => {
  if(queryParams?.oldTime && queryParams.hotTimeType == 2){

    queryParams.oldTime = null;
    queryParams.newTime = null;
  }

 if (queryParams.timeArr && queryParams.hotTimeType == 2) {

    queryParams.oldTime = queryParams.timeArr[0];
    queryParams.newTime = queryParams.timeArr[1].split(" ")[0]+ " " + "23:59:59";
    if(queryParams.id != null){
      handleHotQuery();
    }
  }
}

// const handleMonthPick = () => {

//   if(queryParams.oldTime){
//     var newTime = new Date(queryParams.oldTime);
//     newTime.setMonth(newTime.getMonth() + 1);
//     newTime.setDate(newTime.getDate() - 1);
//     newTime.setHours(23,59,59)
//     queryParams.newTime = getFullTimeByDate(newTime);
//     visControll.isSameDay = false;
//   }else {
//     queryParams.newTime = null;
//   }
  
// } 

// const getFullTimeByDate = (date) => {
//   var year = date.getFullYear();//年
//   var month = date.getMonth();//月
//   var day = date.getDate();//日
//   var hours = date.getHours();//时
//   var min = date.getMinutes();//分
//   var second = date.getSeconds();//秒
//   return year + "-" +
//       ((month + 1) > 9 ? (month + 1) : "0" + (month + 1)) + "-" +
//       (day > 9 ? day : ("0" + day)) + " " +
//       (hours > 9 ? hours : ("0" + hours)) + ":" +
//       (min > 9 ? min : ("0" + min)) + ":" +
//       (second > 9 ? second : ("0" + second));
// }

// const areDatesEqual = (date1, date2) => {
//   return (
//     date1.getFullYear() === date2.getFullYear() &&
//     date1.getMonth() === date2.getMonth() &&
//     date1.getDate() === date2.getDate()
//   );
// }


// const activeNames = ref(["1","2","3","4","5"])

// const CabinetTableData = ref([]) as any

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
  oldTime : null,
  newTime : null,
  cascadeAddr : 0,
  iceTimeType : 1,
  hotTimeType : 1
}) as any

const serverRoomArr =  ref([]) as any

//折叠功能


const getNavList = async() => {
  const res = await CabinetApi.getRoomMenuAll({})
  serverRoomArr.value = res
  if (res && res.length > 0) {
    const room = res[0]
    const keys = [] as string[]
    room.children.forEach(child => {
      if(child.children.length > 0) {
        child.children.forEach(son => {
          keys.push(son.id + '-' + son.type)
        })
      }
    })
  }
}

const handleClick = (row) => {
  if(row.type != null  && row.type == 3){
    queryParams.id = row.id
    handleQuery();
  }
}


//柱状图宽度
const list = ref([
  { 
    id:null,
    status:null,
    apparentPow:null,
    pow:null,
    ele:null,
    devKey:null,
    location:null,
    dataUpdateTime : "",
    pduAlarm:"",
    pf:null,
    acur : null,
    bcur : null,
    ccur : null,
    curUnbalance : null,
  }
]) as any// 列表的数据

const cabinetEnvData = ref({}) as any

//折线图数据
// interface EqData {
//   eq: number[];
//   time: string[];
//   totalEle : number;
//   maxEle : number;
//   maxEleTime : string;
//   firstEq : number;
//   lastEq : number;
// }
// const eqData = ref<EqData>({
//   eq : [],
//   time : [],
//   totalEle : 0,
//   maxEle : 0,
//   maxEleTime : "",
//   firstEq : 0,
//   lastEq : 0,
// })as any

// interface PowData {
//   apparentPowAvgValue: number[];
//   activePowAvgValue: number[];
//   AapparentPowAvgValue: number[];
//   AactivePowAvgValue: number[];
//   BapparentPowAvgValue: number[];
//   BactivePowAvgValue: number[];
//   time: string[];
//   total_pow_apparent : number;
//   apparentPowMaxValue : number;
//   apparentPowMaxTime : string;
//   apparentPowMinValue : number;
//   apparentPowMinTime : string;
//   activePowMaxValue : number;
//   activePowMaxTime : string;
//   activePowMinValue : number;
//   activePowMinTime : string;
//   AapparentPowMaxValue : number;
//   AapparentPowMaxTime : string;
//   AapparentPowMinValue : number;
//   AapparentPowMinTime : string;
//   AactivePowMaxValue : number;
//   AactivePowMaxTime : string;
//   AactivePowMinValue : number;
//   AactivePowMinTime : string;
//   BapparentPowMaxValue : number;
//   BapparentPowMaxTime : string;
//   BapparentPowMinValue : number;
//   BapparentPowMinTime : string;
//   BactivePowMaxValue : number;
//   BactivePowMaxTime : string;
//   BactivePowMinValue : number;
//   BactivePowMinTime : string;
// }
// const powData = ref<PowData>({
//   apparentPowAvgValue : [],
//   activePowAvgValue: [],
//   AapparentPowAvgValue : [],
//   AactivePowAvgValue: [],
//   BapparentPowAvgValue : [],
//   BactivePowAvgValue: [],
//   time:[],
//   total_pow_apparent : 0,
//   apparentPowMaxValue : 0,
//   apparentPowMaxTime : "",
//   apparentPowMinValue : 0,
//   apparentPowMinTime : "",
//   activePowMaxValue : 0,
//   activePowMaxTime : "",
//   activePowMinValue : 0,
//   activePowMinTime : "",
//   AapparentPowMaxValue : 0,
//   AapparentPowMaxTime : "",
//   AapparentPowMinValue : 0,
//   AapparentPowMinTime : "",
//   AactivePowMaxValue : 0,
//   AactivePowMaxTime : "",
//   AactivePowMinValue : 0,
//   AactivePowMinTime : "",
//   BapparentPowMaxValue : 0,
//   BapparentPowMaxTime : "",
//   BapparentPowMinValue : 0,
//   BapparentPowMinTime : "",
//   BactivePowMaxValue : 0,
//   BactivePowMaxTime : "",
//   BactivePowMinValue : 0,
//   BactivePowMinTime : "",
// })as any

const iceTemAndHumData = ({
  temAvgValue : [],
  humAvgValue : [],
  time : [],
})as any

const hotTemAndHumData = ({
  temAvgValue : [],
  humAvgValue : [],
  time : [],
})as any


// interface ServerData {
//   nameAndMax: object[];
//   value: number[];
// }
// const serverData = ref<ServerData>({
//   nameAndMax : [
//   ],
//   value: []
// })as any

// interface OutLetRankData {
//   outLetId: string[];
//   eleValue: number[];
// }

// const outletRankData = ref<OutLetRankData>({
//   outLetId : [],
//   eleValue : [],
// })as any

const filterText = ref('')
const treeRef = ref<InstanceType<typeof ElTree>>()

const getList = async () => {
  loading.value = true
  // await IndexApi.getCabinetIceTemAndHumById(queryParams);
  // eqData.value = await IndexApi.getCabinetHotTemAndHumById(queryParams);
  // if( eqData.value.eq && eqData.value.eq.length > 0){
  //   visControll.eqVis = true;
  //   eqData.value.eq.forEach((obj,index)=>{
  //     eqData.value.eq[index] = obj.toFixed(1);
  //   })
  //   eqData.value.maxEle = eqData.value.maxEle?.toFixed(1);
  //   eqData.value.totalEle = eqData.value.totalEle?.toFixed(1);
  //   if(eqData.value.firstEq){
  //     eqData.value.firstEq = eqData.value.firstEq?.toFixed(1);
  //   }
  //   if(eqData.value.lastEq){
  //     eqData.value.lastEq = eqData.value.lastEq?.toFixed(1);
  //   }
    
  // } else{
  //   visControll.eqVis = false;
  // }
  

  // powData.value = await IndexApi.getPowData(queryParams);

  // if(powData.value.activePowAvgValue && powData.value.activePowAvgValue.length > 0){
  //   powData.value.activePowAvgValue.forEach((obj,index)=>{
  //     powData.value.activePowAvgValue[index] = obj.toFixed(3);
  //   })
  //   powData.value.apparentPowAvgValue.forEach((obj,index)=>{
  //     powData.value.apparentPowAvgValue[index] = obj.toFixed(3);
  //   })
  //   powData.value.apparentPowMaxValue = powData.value.apparentPowMaxValue?.toFixed(3);
  //   powData.value.apparentPowMinValue =  powData.value.apparentPowMinValue?.toFixed(3);
  //   powData.value.activePowMaxValue = powData.value.activePowMaxValue?.toFixed(3);
  //   powData.value.activePowMinValue = powData.value.activePowMinValue?.toFixed(3);
  //   visControll.powVis = true;
  // }else{
  //   visControll.powVis = false;
  // }

  // if(powData.value.AactivePowAvgValue && powData.value.AactivePowAvgValue.length > 0){
  //   powData.value.AactivePowAvgValue.forEach((obj,index)=>{
  //     powData.value.AactivePowAvgValue[index] = obj.toFixed(3);
  //   })
  //   powData.value.AapparentPowAvgValue.forEach((obj,index)=>{
  //     powData.value.AapparentPowAvgValue[index] = obj.toFixed(3);
  //   })
  //   powData.value.AapparentPowMaxValue = powData.value.AapparentPowMaxValue?.toFixed(3);
  //   powData.value.AapparentPowMinValue =  powData.value.AapparentPowMinValue?.toFixed(3);
  //   powData.value.AactivePowMaxValue = powData.value.AactivePowMaxValue?.toFixed(3);
  //   powData.value.AactivePowMinValue = powData.value.AactivePowMinValue?.toFixed(3);
  //   visControll.ApowVis = true;
  // }else{
  //   visControll.ApowVis = false;
  // }

  // if(powData.value.BactivePowAvgValue && powData.value.BactivePowAvgValue.length > 0){
    
  //   powData.value.BactivePowAvgValue.forEach((obj,index)=>{
  //     powData.value.BactivePowAvgValue[index] = obj.toFixed(3);
  //   })

  //   powData.value.BapparentPowAvgValue.forEach((obj,index)=>{
  //     powData.value.BapparentPowAvgValue[index] = obj.toFixed(3);
  //   })

  //   powData.value.BapparentPowMaxValue = powData.value.BapparentPowMaxValue?.toFixed(3);
  //   powData.value.BapparentPowMinValue =  powData.value.BapparentPowMinValue?.toFixed(3);
  //   powData.value.BactivePowMaxValue = powData.value.BactivePowMaxValue?.toFixed(3);
  //   powData.value.BactivePowMinValue = powData.value.BactivePowMinValue?.toFixed(3);

  //   visControll.BpowVis = true;
  // }else{
  //   visControll.BpowVis = false;
  // }
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
  
  iceTemAndHumData.value = await IndexApi.getCabinetIceTemAndHumById({id : queryParams.id,timeType : queryParams.iceTimeType, oldTime : queryParams.oldTime, newTime : queryParams.newTime});

  if(iceTemAndHumData.value?.temAvgValue && iceTemAndHumData.value.temAvgValue?.length > 0){
    visControll.temVis = true;
    iceTemAndHumData.value.temAvgValue?.forEach((obj,index) => {
      obj?.forEach((element,innerIndex) => {
        iceTemAndHumData.value.temAvgValue[index][innerIndex] = element?.toFixed(2);
      });
    });

  }
  if(iceTemAndHumData.value.humAvgValue && iceTemAndHumData.value.humAvgValue?.length > 0){
    visControll.temVis = true;
    iceTemAndHumData.value.humAvgValue?.forEach((obj,index) => {
      obj?.forEach((element,innerIndex) => {
        iceTemAndHumData.value.humAvgValue[index][innerIndex] = element?.toFixed(2);
      });
    });
  }

  hotTemAndHumData.value = await IndexApi.getCabinetHotTemAndHumById({id : queryParams.id,timeType : queryParams.hotTimeType, oldTime : queryParams.oldTime, newTime : queryParams.newTime});

  if(hotTemAndHumData.value.temAvgValue && hotTemAndHumData.value.temAvgValue?.length > 0){
    visControll.temVis = true;
    hotTemAndHumData.value.temAvgValue?.forEach((obj,index) => {
      obj?.forEach((element,innerIndex) => {
        hotTemAndHumData.value.temAvgValue[index][innerIndex] = element?.toFixed(2);
      });
    });

  }
  if(hotTemAndHumData.value.humAvgValue && hotTemAndHumData.value.humAvgValue?.length > 0){
    visControll.temVis = true;
    hotTemAndHumData.value.humAvgValue?.forEach((obj,index) => {
      obj?.forEach((element,innerIndex) => {
        hotTemAndHumData.value.humAvgValue[index][innerIndex] = element?.toFixed(2);
      });
    });
  }

  const data = await IndexApi.getCabinetEnvPage({cabinetIds : [queryParams.id]});
  list.value = data.list

  list.value.forEach((obj) => {
    obj.iceTopTem = obj.iceTopTem?.toFixed(1);
    obj.iceTopHum = obj.iceTopHum?.toFixed(1);
    obj.iceMidTem = obj.iceMidTem?.toFixed(1);
    obj.iceMidHum = obj.iceMidHum?.toFixed(1);
    obj.iceBomTem = obj.iceBomTem?.toFixed(1);
    obj.iceBomHum = obj.iceBomHum?.toFixed(1);
    obj.hotTopTem = obj.hotTopTem?.toFixed(1);
    obj.hotTopHum = obj.hotTopHum?.toFixed(1);
    obj.hotMidTem = obj.hotMidTem?.toFixed(1);
    obj.hotMidHum = obj.hotMidHum?.toFixed(1);
    obj.hotBomTem = obj.hotBomTem?.toFixed(1);
    obj.hotBomHum = obj.hotBomHum?.toFixed(1);
  });
  cabinetEnvData.value = list.value[0];

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
    // var CabinetInfo = await CabinetApi.getCabinetDetail({id : queryParams.id});
  

    // var apow = CabinetInfo?.cabinet_power?.path_a?.pow_active;
    // var bpow = CabinetInfo?.cabinet_power?.path_b?.pow_active;
    // console.log("apow",apow,"bpow",bpow)
    // var percentageValue = 50 as any;
    // if(apow == null && bpow == null){
    //   percentageValue = null;
    // } else if (apow != null && bpow == null){
    //   percentageValue = 100;
    // } else if (apow == null && bpow != null){
    //   percentageValue = 0;
    // } else if (apow != 0 && bpow == 0){
    //   percentageValue = 100;
    // } else if (apow == 0 && bpow != 0){
    //   percentageValue = 0;
    // } else if (apow != 0 && bpow != 0) {
    //   percentageValue = apow / (apow + bpow);
    //   percentageValue *= 100;
    // }
    // temp.push({
    //   baseInfoName : "所属位置",
    //   baseInfoValue : CabinetInfo?.room_name + (CabinetInfo?.room_name ? '-' : null)  + CabinetInfo?.cabinet_name,
    //   consumeName : "当前总视在功率",
    //   consumeValue : CabinetInfo?.cabinet_power?.total_data?.pow_apparent?.toFixed(3) + "kVA",
    //   percentageName: "当前AB路占比",
    //   percentageValue: percentageValue?.toFixed(0),
    // })
    // temp.push({
    //   baseInfoName : "电力容量",
    //   baseInfoValue : CabinetInfo?.pow_capacity,
    //   consumeName : "当前总有功功率",
    //   consumeValue : CabinetInfo?.cabinet_power?.total_data?.pow_active?.toFixed(3) + "kW"
    // })
    // temp.push({
    //   baseInfoName : "负载率",
    //   baseInfoValue : CabinetInfo?.load_factor?.toFixed(2) + "%",
    //   consumeName : "当前总无功功率",
    //   consumeValue : CabinetInfo?.cabinet_power?.total_data?.pow_reactive?.toFixed(3) + "kVar"
    // })
    // CabinetTableData.value = temp;
  
  
  // initChart();
  loading.value = false

}

// const rankChartContainer = ref<HTMLElement | null>(null);
// let rankChart = null as echarts.ECharts | null; // 显式声明 rankChart 的类型
// const powChartContainer = ref<HTMLElement | null>(null);
// let powChart = null as echarts.ECharts | null; // 显式声明 powChart 的类型
// const ApowChartContainer = ref<HTMLElement | null>(null);
// let ApowChart = null as echarts.ECharts | null; // 显式声明 powChart 的类型
// const BpowChartContainer = ref<HTMLElement | null>(null);
// let BpowChart = null as echarts.ECharts | null; // 显式声明 powChart 的类型
// const temChartContainer = ref<HTMLElement | null>(null);
// let temChart = null as echarts.ECharts | null; // 显式声明 temChart 的类型
// const serChartContainer = ref<HTMLElement | null>(null);
// let serChart = null as echarts.ECharts | null; // 显式声明 serChart 的类型
// const outputRankChartContainer = ref<HTMLElement | null>(null);
// let outputRankChart = null as echarts.ECharts | null; // 显式声明 outputRankChart 的类型
const iceTemOrHumChartContainer = ref<HTMLElement | null>(null);
let iceTemOrHumChart = null as echarts.ECharts | null; // 显式声明 serChart 的类型
const hotTemOrHumChartContainer = ref<HTMLElement | null>(null);
let hotTemOrHumChart = null as echarts.ECharts | null; // 显式声明 serChart 的类型

const initChart =  () => {
  // if (rankChartContainer.value && instance && eqData.value.time && eqData.value.time.length > 0) {
  //   rankChart = echarts.init(rankChartContainer.value);
  //   rankChart.setOption({
  //     // 这里设置 Echarts 的配置项和数据
  //     dataZoom:[{ type:"inside"}],
  //     title: { text: ''},
  //     tooltip: { trigger: 'axis',formatter: function (params) {
  //                                             var dataIndex = params[0].dataIndex;
  //                                             var eleValue = eqData.value.eq[dataIndex];
  //                                             return  eqData.value.time[dataIndex] + "<br/>" + params[0].marker +  eleValue + " kWh"; // 自定义浮窗显示的内容
  //                                           },},
  //     toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
  //     xAxis: {type: 'category' ,data:eqData.value.time},
  //     yAxis: { type: 'value' , name : "kWh"},
  //     series: [
  //       { type: 'bar', data: eqData.value.eq, label: { show: true, position: 'top' }, barWidth: barWid.value,
  //         itemStyle: {
  //           emphasis: {
  //             barBorderRadius: 3,
  //           },
  //           //颜色样式部分
  //           normal: {
  //             barBorderRadius: 3,
  //             color: new echarts.graphic.LinearGradient(0, 1, 0, 0, [
  //               { offset: 0, color: "#3977E6" },
  //               { offset: 1, color: "#37BBF8" },
  //             ]),
  //           },
  //         },
  //       },
  //     ],
  //   });
  //   // 将 rankChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
  //   instance.appContext.config.globalProperties.rankChart = rankChart;
  // }
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
  // if (powChartContainer.value && instance && powData.value.time && powData.value.time.length > 0) {
  //   powChart = echarts.init(powChartContainer.value);
  //   powChart.setOption({
  //     // 这里设置 Echarts 的配置项和数据
  //     dataZoom:[{ type:"inside"}],
  //     title: { text: ''},
  //     tooltip: { trigger: 'axis', formatter: function(params) {
  //                                   var result = params[0].name + '<br>';
  //                                   for (var i = 0; i < params.length; i++) {
  //                                     result +=  params[i].marker + params[i].seriesName + ': &nbsp&nbsp&nbsp&nbsp' + params[i].value;
  //                                     if (params[i].seriesName === '总平均视在功率') {
  //                                       result += ' kVA'; 
  //                                     } else if (params[i].seriesName === '总平均有功功率') {
  //                                       result += ' kW';
  //                                     }
  //                                     result += '<br>';
  //                                   }
  //                                   return result;
  //                                 } },
  //     legend: { data: ['总平均视在功率','总平均有功功率']},
  //     grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
  //     toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
  //     xAxis: {type: 'category', boundaryGap: false, data:powData.value.time},
  //     yAxis: { type: 'value'},
  //     series: [
  //       {name: '总平均视在功率', type: 'line', symbol: 'circle', symbolSize: 4, data: powData.value.apparentPowAvgValue},
  //       {name: '总平均有功功率', type: 'line', symbol: 'circle', symbolSize: 4, data: powData.value.activePowAvgValue},
  //     ],

  //   });
  //   // 将 powChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
  //   instance.appContext.config.globalProperties.powChart = powChart;
  // }
  // if (ApowChartContainer.value && instance && powData.value.time && powData.value.time.length > 0) {
  //   ApowChart = echarts.init(ApowChartContainer.value);
  //   ApowChart.setOption({
  //     // 这里设置 Echarts 的配置项和数据
  //     dataZoom:[{ type:"inside"}],
  //     title: { text: ''},
  //     tooltip: { trigger: 'axis', formatter: function(params) {
  //                                   var result = params[0].name + '<br>';
  //                                   for (var i = 0; i < params.length; i++) {
  //                                     result +=  params[i].marker + params[i].seriesName + ': &nbsp&nbsp&nbsp&nbsp' + params[i].value;
  //                                     if (params[i].seriesName === 'A平均视在功率') {
  //                                       result += ' kVA'; 
  //                                     } else if (params[i].seriesName === 'A平均有功功率') {
  //                                       result += ' kW';
  //                                     }
  //                                     result += '<br>';
  //                                   }
  //                                   return result;
  //                                 } },
  //     legend: { data: ['A平均视在功率','A平均有功功率']},
  //     grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
  //     toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
  //     xAxis: {type: 'category', boundaryGap: false, data:powData.value.time},
  //     yAxis: { type: 'value'},
  //     series: [
  //       {name: 'A平均视在功率', type: 'line', symbol: 'circle', symbolSize: 4, data: powData.value.AapparentPowAvgValue},
  //       {name: 'A平均有功功率', type: 'line', symbol: 'circle', symbolSize: 4, data: powData.value.AactivePowAvgValue},
  //     ],

  //   });
  //   // 将 ApowChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
  //   instance.appContext.config.globalProperties.ApowChart = ApowChart;
  // }
  // if (BpowChartContainer.value && instance && powData.value.time && powData.value.time.length > 0) {
  //   BpowChart = echarts.init(BpowChartContainer.value);
  //   BpowChart.setOption({
  //     // 这里设置 Echarts 的配置项和数据
  //     dataZoom:[{ type:"inside"}],
  //     title: { text: ''},
  //     tooltip: { trigger: 'axis', formatter: function(params) {
  //                                   var result = params[0].name + '<br>';
  //                                   for (var i = 0; i < params.length; i++) {
  //                                     result +=  params[i].marker + params[i].seriesName + ': &nbsp&nbsp&nbsp&nbsp' + params[i].value;
  //                                     if (params[i].seriesName === 'B平均视在功率') {
  //                                       result += ' kVA'; 
  //                                     } else if (params[i].seriesName === 'B平均有功功率') {
  //                                       result += ' kW';
  //                                     }
  //                                     result += '<br>';
  //                                   }
  //                                   return result;
  //                                 } },
  //     legend: { data: ['B平均视在功率','B平均有功功率']},
  //     grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
  //     toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
  //     xAxis: {type: 'category', boundaryGap: false, data:powData.value.time},
  //     yAxis: { type: 'value'},
  //     series: [
  //       {name: 'B平均视在功率', type: 'line', symbol: 'circle', symbolSize: 4, data: powData.value.BapparentPowAvgValue},
  //       {name: 'B平均有功功率', type: 'line', symbol: 'circle', symbolSize: 4, data: powData.value.BactivePowAvgValue},
  //     ],

  //   });
  //   // 将 ApowChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
  //   instance.appContext.config.globalProperties.BpowChart = BpowChart;
  // }
  if (iceTemOrHumChartContainer.value && instance && iceTemAndHumData.value.temAvgValue && iceTemAndHumData.value.temAvgValue.length > 0) {
    iceTemOrHumChart = echarts.init(iceTemOrHumChartContainer.value);
    var seriesArr = [] as any;
    var legendData = [] as any;
    iceTemAndHumData.value.temAvgValue?.forEach((obj,index) => {
      if(index != 0 && iceTemAndHumData.value.temAvgValue[index].length > 0){
        seriesArr.push({
          name: (index == 1 ? "上层" : index == 2 ? "中层": "下层") +  '温度传感器' ,
          type: 'line',
          data: obj,
          symbol: 'circle', 
          symbolSize: 4,
        });
        legendData.push((index == 1 ? "上层" : index == 2 ? "中层": "下层") + '温度传感器')
      }
    });
    iceTemOrHumChart.setOption({
      // 这里设置 Echarts 的配置项和数据
      dataZoom:[{ type:"inside"}],
      title: { text: ''},
      tooltip: { trigger: 'axis',formatter: function(params) {
                                    var result = params[0].name + '<br>';
                                    for (var i = 0; i < params.length; i++) {
                                      result +=  params[i].marker + params[i].seriesName + ': &nbsp&nbsp&nbsp&nbsp' + params[i].value;
                                      if (params[i].seriesName.includes('温度')) {
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
          data: iceTemAndHumData.value.time
        },
      yAxis: { type: 'value'},
      series: seriesArr ,
    });
    // 将 temChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    instance.appContext.config.globalProperties.iceTemOrHumChart = iceTemOrHumChart;
  }

  if (hotTemOrHumChartContainer.value && instance && hotTemAndHumData.value.temAvgValue && hotTemAndHumData.value.temAvgValue.length > 0) {
    hotTemOrHumChart = echarts.init(hotTemOrHumChartContainer.value);
    var seriesArr = [] as any;
    var legendData = [] as any;
    hotTemAndHumData.value.temAvgValue?.forEach((obj,index) => {
      if(index != 0 && hotTemAndHumData.value.temAvgValue[index].length > 0){
        seriesArr.push({
          name: (index == 1 ? "上层" : index == 2 ? "中层": "下层") +  '温度传感器' ,
          type: 'line',
          data: obj,
          symbol: 'circle', 
          symbolSize: 4,
        });
        legendData.push((index == 1 ? "上层" : index == 2 ? "中层": "下层") + '温度传感器')
      }
    });

    hotTemOrHumChart.setOption({
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
          data: hotTemAndHumData.value.time
        },
      yAxis: { type: 'value'},
      series: seriesArr ,
    });
    // 将 temChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    instance.appContext.config.globalProperties.hotTemOrHumChart = hotTemOrHumChart;
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
// const beforeRankUnmount = () => {
//     rankChart?.dispose(); // 销毁图表实例
// };

// const beforeOutPutRankUnmount = () => {
//     outputRankChart?.dispose(); // 销毁图表实例
// };

// const beforePowUnmount = () => {
//     powChart?.dispose();  // 销毁图表实例
// };

// const beforeAPowUnmount = () => {
//     ApowChart?.dispose();  // 销毁图表实例
// };

// const beforeBPowUnmount = () => {
//     BpowChart?.dispose();  // 销毁图表实例
// };

// const beforeTemUnmount = () => {
//     temChart?.dispose(); // 销毁图表实例
// };

// const beforeTemUnmount = () => {
//     temChart?.dispose(); // 销毁图表实例
// };

const beforeIceTemOrHumUnmount = () => {
  iceTemOrHumChart?.dispose(); // 销毁图表实例
};
const beforeHotTemOrHumUnmount = () => {
  hotTemOrHumChart?.dispose(); // 销毁图表实例
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

watch([() => iceTemAndHumSwitchValue.value], ([newValue]) => {
    // 销毁原有的图表实例
    beforeIceTemOrHumUnmount();
    // 创建新的图表实例
    iceTemOrHumChart = echarts.init(document.getElementById('iceTemOrHumChartContainer'));
    // 设置新的配置对象
    if(iceTemAndHumData.value != null){
      if(newValue == 1 ){
        var seriesArr = [] as any;
        var legendData = [] as any;
        iceTemAndHumData.value.temAvgValue?.forEach((obj,index) => {
          if(index != 0 && iceTemAndHumData.value.temAvgValue[index].length > 0){
            seriesArr.push({
              name: (index == 1 ? "上层" : index == 2 ? "中层": "下层") +  '温度传感器' ,
              type: 'line',
              data: obj,
              symbol: 'circle', 
              symbolSize: 4,
            });
            legendData.push((index == 1 ? "上层" : index == 2 ? "中层": "下层") + '温度传感器')
          }
        });
        iceTemOrHumChart.setOption({
          // 这里设置 Echarts 的配置项和数据
          dataZoom:[{ type:"inside"}],
          title: { text: ''},
          tooltip: { trigger: 'axis',formatter: function(params) {
                                        var result = params[0].name + '<br>';
                                        for (var i = 0; i < params.length; i++) {
                                          result +=  params[i].marker + params[i].seriesName + ': &nbsp&nbsp&nbsp&nbsp' + params[i].value;
                                          if (params[i].seriesName.includes('温度')) {
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
              data: iceTemAndHumData.value.time
            },
          yAxis: { type: 'value'},
          series: seriesArr ,
        });
      } else {
        var seriesArr = [] as any;
        var legendData = [] as any;
        iceTemAndHumData.value.humAvgValue?.forEach((obj,index) => {
          if(index != 0 && iceTemAndHumData.value.humAvgValue[index].length > 0){
            seriesArr.push({
              name: (index == 1 ? "上层" : index == 2 ? "中层": "下层") +  '湿度传感器' ,
              type: 'line',
              data: obj,
              symbol: 'circle', 
              symbolSize: 4,
            });
            legendData.push((index == 1 ? "上层" : index == 2 ? "中层": "下层") + '湿度传感器')
          }
        });
        iceTemOrHumChart.setOption({
          // 这里设置 Echarts 的配置项和数据
          dataZoom:[{ type:"inside"}],
          title: { text: ''},
          tooltip: { trigger: 'axis',formatter: function(params) {
                                        var result = params[0].name + '<br>';
                                        for (var i = 0; i < params.length; i++) {
                                          result +=  params[i].marker + params[i].seriesName + ': &nbsp&nbsp&nbsp&nbsp' + params[i].value;
                                          if (params[i].seriesName.includes('湿度')) {
                                            result += '%'; 
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
              data: iceTemAndHumData.value.time
            },
          yAxis: { type: 'value'},
          series: seriesArr ,
        });
      }    
    }
})

watch([() => hotTemAndHumSwitchValue.value], ([newValue]) => {
    // 销毁原有的图表实例
    beforeHotTemOrHumUnmount();
    // 创建新的图表实例
    hotTemOrHumChart = echarts.init(document.getElementById('hotTemOrHumChartContainer'));
    // 设置新的配置对象
    if(hotTemAndHumData.value != null){
      if(newValue == 1 ){
        var seriesArr = [] as any;
        var legendData = [] as any;
        hotTemAndHumData.value.temAvgValue?.forEach((obj,index) => {
          if(index != 0 && hotTemAndHumData.value.temAvgValue[index].length > 0){
            seriesArr.push({
              name: (index == 1 ? "上层" : index == 2 ? "中层": "下层") +  '温度传感器' ,
              type: 'line',
              data: obj,
              symbol: 'circle', 
              symbolSize: 4,
            });
            legendData.push((index == 1 ? "上层" : index == 2 ? "中层": "下层") + '温度传感器')
          }
        });
        hotTemOrHumChart.setOption({
          // 这里设置 Echarts 的配置项和数据
          dataZoom:[{ type:"inside"}],
          title: { text: ''},
          tooltip: { trigger: 'axis',formatter: function(params) {
                                        var result = params[0].name + '<br>';
                                        for (var i = 0; i < params.length; i++) {
                                          result +=  params[i].marker + params[i].seriesName + ': &nbsp&nbsp&nbsp&nbsp' + params[i].value;
                                          if (params[i].seriesName.includes('温度')) {
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
              data: hotTemAndHumData.value.time
            },
          yAxis: { type: 'value'},
          series: seriesArr ,
        });
      } else {
        var seriesArr = [] as any;
        var legendData = [] as any;
        hotTemAndHumData.value.humAvgValue?.forEach((obj,index) => {
          if(index != 0 && hotTemAndHumData.value.humAvgValue[index].length > 0){
            seriesArr.push({
              name: (index == 1 ? "上层" : index == 2 ? "中层": "下层") +  '湿度传感器' ,
              type: 'line',
              data: obj,
              symbol: 'circle', 
              symbolSize: 4,
            });
            legendData.push((index == 1 ? "上层" : index == 2 ? "中层": "下层") + '湿度传感器')
          }
        });
        hotTemOrHumChart.setOption({
          // 这里设置 Echarts 的配置项和数据
          dataZoom:[{ type:"inside"}],
          title: { text: ''},
          tooltip: { trigger: 'axis',formatter: function(params) {
                                        var result = params[0].name + '<br>';
                                        for (var i = 0; i < params.length; i++) {
                                          result +=  params[i].marker + params[i].seriesName + ': &nbsp&nbsp&nbsp&nbsp' + params[i].value;
                                          if (params[i].seriesName.includes('湿度')) {
                                            result += '%'; 
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
              data: hotTemAndHumData.value.time
            },
          yAxis: { type: 'value'},
          series: seriesArr ,
        });
      }    
    }
})

// 下拉框选项数组
// const deviceStatus = ref([])

// const message = useMessage() // 消息弹窗
// const { t } = useI18n() // 国际化

const loading = ref(false) // 列表的加载中

// const total = ref(0) // 列表的总页数
const queryFormRef = ref() // 搜索的表单
// const exportLoading = ref(false) // 导出的加载中




/** 搜索按钮操作 */
const handleQuery = async () => {

  if(queryParams.id != null ){
    await getList();
    // beforeRankUnmount();
    // beforeOutPutRankUnmount();
    // beforePowUnmount();
    // beforeAPowUnmount();
    // beforeBPowUnmount();
    // beforeTemUnmount();
    beforeIceTemOrHumUnmount();
    beforeHotTemOrHumUnmount();
    initChart();
    queryParams.devKey = null;
  }
}


const handleIceQuery = async () => {

  if(queryParams.id != null){
    iceTemAndHumData.value = await IndexApi.getCabinetIceTemAndHumById({id : queryParams.id,timeType : queryParams.iceTimeType, oldTime : queryParams.oldTime, newTime : queryParams.newTime});

    if(iceTemAndHumData.value.temAvgValue && iceTemAndHumData.value.temAvgValue?.length > 0){
      visControll.temVis = true;
      iceTemAndHumData.value.temAvgValue?.forEach((obj,index) => {
        obj?.forEach((element,innerIndex) => {
          iceTemAndHumData.value.temAvgValue[index][innerIndex] = element?.toFixed(2);
        });
      });

    }
    if(iceTemAndHumData.value.humAvgValue && iceTemAndHumData.value.humAvgValue?.length > 0){
      visControll.temVis = true;
      iceTemAndHumData.value.humAvgValue?.forEach((obj,index) => {
        obj?.forEach((element,innerIndex) => {
          iceTemAndHumData.value.humAvgValue[index][innerIndex] = element?.toFixed(2);
        });
      });
    }
    // beforeRankUnmount();
    // beforeOutPutRankUnmount();
    // beforePowUnmount();
    // beforeAPowUnmount();
    // beforeBPowUnmount();
    // beforeTemUnmount();
    // 销毁原有的图表实例
    beforeIceTemOrHumUnmount();
    // 创建新的图表实例
    iceTemOrHumChart = echarts.init(document.getElementById('iceTemOrHumChartContainer'));
    // 设置新的配置对象
    if(iceTemAndHumData.value != null){
      if(iceTemAndHumSwitchValue.value == 1 ){
        var seriesArr = [] as any;
        var legendData = [] as any;
        iceTemAndHumData.value.temAvgValue?.forEach((obj,index) => {
          if(index != 0 && iceTemAndHumData.value.temAvgValue[index].length > 0){
            seriesArr.push({
              name: (index == 1 ? "上层" : index == 2 ? "中层": "下层") +  '温度传感器' ,
              type: 'line',
              data: obj,
              symbol: 'circle', 
              symbolSize: 4,
            });
            legendData.push((index == 1 ? "上层" : index == 2 ? "中层": "下层") + '温度传感器')
          }
        });
        iceTemOrHumChart.setOption({
          // 这里设置 Echarts 的配置项和数据
          dataZoom:[{ type:"inside"}],
          title: { text: ''},
          tooltip: { trigger: 'axis',formatter: function(params) {
                                        var result = params[0].name + '<br>';
                                        for (var i = 0; i < params.length; i++) {
                                          result +=  params[i].marker + params[i].seriesName + ': &nbsp&nbsp&nbsp&nbsp' + params[i].value;
                                          if (params[i].seriesName.includes('温度')) {
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
              data: iceTemAndHumData.value.time
            },
          yAxis: { type: 'value'},
          series: seriesArr ,
        });
      } else {
        var seriesArr = [] as any;
        var legendData = [] as any;
        iceTemAndHumData.value.humAvgValue?.forEach((obj,index) => {
          if(index != 0 && iceTemAndHumData.value.humAvgValue[index].length > 0){
            seriesArr.push({
              name: (index == 1 ? "上层" : index == 2 ? "中层": "下层") +  '湿度传感器' ,
              type: 'line',
              data: obj,
              symbol: 'circle', 
              symbolSize: 4,
            });
            legendData.push((index == 1 ? "上层" : index == 2 ? "中层": "下层") + '湿度传感器')
          }
        });
        iceTemOrHumChart.setOption({
          // 这里设置 Echarts 的配置项和数据
          dataZoom:[{ type:"inside"}],
          title: { text: ''},
          tooltip: { trigger: 'axis',formatter: function(params) {
                                        var result = params[0].name + '<br>';
                                        for (var i = 0; i < params.length; i++) {
                                          result +=  params[i].marker + params[i].seriesName + ': &nbsp&nbsp&nbsp&nbsp' + params[i].value;
                                          if (params[i].seriesName.includes('湿度')) {
                                            result += '%'; 
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
              data: iceTemAndHumData.value.time
            },
          yAxis: { type: 'value'},
          series: seriesArr ,
        });
      }    
    }
  }
}

const handleHotQuery = async () => {

  if(queryParams.id != null){
    hotTemAndHumData.value = await IndexApi.getCabinetHotTemAndHumById({id : queryParams.id,timeType : queryParams.hotTimeType, oldTime : queryParams.oldTime, newTime : queryParams.newTime});

    if(hotTemAndHumData.value.temAvgValue && hotTemAndHumData.value.temAvgValue?.length > 0){
      visControll.temVis = true;
      hotTemAndHumData.value.temAvgValue?.forEach((obj,index) => {
        obj?.forEach((element,innerIndex) => {
          hotTemAndHumData.value.temAvgValue[index][innerIndex] = element?.toFixed(2);
        });
      });

    }
    if(hotTemAndHumData.value.humAvgValue && hotTemAndHumData.value.humAvgValue?.length > 0){
      visControll.temVis = true;
      hotTemAndHumData.value.humAvgValue?.forEach((obj,index) => {
        obj?.forEach((element,innerIndex) => {
          hotTemAndHumData.value.humAvgValue[index][innerIndex] = element?.toFixed(2);
        });
      });
    }

    // 销毁原有的图表实例
    beforeHotTemOrHumUnmount();
    // 创建新的图表实例
    hotTemOrHumChart = echarts.init(document.getElementById('hotTemOrHumChartContainer'));
    // 设置新的配置对象
    if(hotTemAndHumData.value != null){
      if(hotTemAndHumSwitchValue.value == 1 ){
        var seriesArr = [] as any;
        var legendData = [] as any;
        hotTemAndHumData.value.temAvgValue?.forEach((obj,index) => {
          if(index != 0 && hotTemAndHumData.value.temAvgValue[index].length > 0){
            seriesArr.push({
              name: (index == 1 ? "上层" : index == 2 ? "中层": "下层") +  '温度传感器' ,
              type: 'line',
              data: obj,
              symbol: 'circle', 
              symbolSize: 4,
            });
            legendData.push((index == 1 ? "上层" : index == 2 ? "中层": "下层") + '温度传感器')
          }
        });
        hotTemOrHumChart.setOption({
          // 这里设置 Echarts 的配置项和数据
          dataZoom:[{ type:"inside"}],
          title: { text: ''},
          tooltip: { trigger: 'axis',formatter: function(params) {
                                        var result = params[0].name + '<br>';
                                        for (var i = 0; i < params.length; i++) {
                                          result +=  params[i].marker + params[i].seriesName + ': &nbsp&nbsp&nbsp&nbsp' + params[i].value;
                                          if (params[i].seriesName.includes('温度')) {
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
              data: hotTemAndHumData.value.time
            },
          yAxis: { type: 'value'},
          series: seriesArr ,
        });
      } else {
        var seriesArr = [] as any;
        var legendData = [] as any;
        hotTemAndHumData.value.humAvgValue?.forEach((obj,index) => {
          if(index != 0 && hotTemAndHumData.value.humAvgValue[index].length > 0){
            seriesArr.push({
              name: (index == 1 ? "上层" : index == 2 ? "中层": "下层") +  '湿度传感器' ,
              type: 'line',
              data: obj,
              symbol: 'circle', 
              symbolSize: 4,
            });
            legendData.push((index == 1 ? "上层" : index == 2 ? "中层": "下层") + '湿度传感器')
          }
        });
        hotTemOrHumChart.setOption({
          // 这里设置 Echarts 的配置项和数据
          dataZoom:[{ type:"inside"}],
          title: { text: ''},
          tooltip: { trigger: 'axis',formatter: function(params) {
                                        var result = params[0].name + '<br>';
                                        for (var i = 0; i < params.length; i++) {
                                          result +=  params[i].marker + params[i].seriesName + ': &nbsp&nbsp&nbsp&nbsp' + params[i].value;
                                          if (params[i].seriesName.includes('湿度')) {
                                            result += '%'; 
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
              data: hotTemAndHumData.value.time
            },
          yAxis: { type: 'value'},
          series: seriesArr ,
        });
      }    
    }
  }
}
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
  getNavList();
})

onBeforeMount(async () =>{
  queryParams.id = router.currentRoute.value.query.id as string;
  
  await getList();
  beforeIceTemOrHumUnmount();
  beforeHotTemOrHumUnmount();
  initChart();
})
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
  margin-left: 30px;
  .progress {
    width: 400px;
    display: flex;
    align-items: center;
    font-size: 14px;
    color: #eee;
    box-sizing: border-box;
    position: relative;
    display: flex;
    justify-content: center;
    .line {
      width: 3px;
      height: 36px;
      background-color: #000;
    }
    .left {
      text-align: center;
      box-sizing: border-box;
      background-color: #3b8bf5;
    }
    .right {
      text-align: center;
      background-color:  #f86f13;
    }
  }
}

:deep(.master-left .el-card__body) {
  padding: 0;
}
:deep(.el-form) {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
}
:deep(.el-form .el-form-item) {
  margin-right: 0;
}
</style>
