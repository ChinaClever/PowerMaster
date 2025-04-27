<template>
  <CommonMenu :showCheckbox="false" @node-click="handleClick" :showSearch="true" :dataList="serverRoomArr" navTitle="机房报表">
    <template #NavInfo>
      <div >
        <div class="header">
          <div class="header_img"><img alt="" src="@/assets/imgs/wmk.jpg" /></div>
          <div class="name">机房</div>
          <div >{{ location}}</div>
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
        <div class="line"></div>

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

        <!-- <el-form-item label="机房Id" prop="ipAddr" >
          <el-autocomplete
            v-model="queryParams.id"
            :fetch-suggestions="querySearch"
            clearable
            class="!w-140px"
            placeholder="请输入id"
            @select="handleQuery"
          />
        </el-form-item> -->

        <el-form-item label="时间段" prop="createTime" label-width="100px">
          <el-button 
            @click="queryParams.timeType = 0;now = new Date();now.setHours(0,0,0,0);queryParams.oldTime = getFullTimeByDate(now);queryParams.newTime = null;queryParams.timeArr = null;visControll.visAllReport = false;switchValue = 0;handleDayPick();handleQuery()" 
            :type="switchValue == 0 ? 'primary' : ''"
          >
            日报
          </el-button>
          <el-button 
            @click="queryParams.timeType = 1;now = new Date();now.setDate(1);now.setHours(0,0,0,0);queryParams.oldTime = getFullTimeByDate(now);queryParams.newTime = null;queryParams.timeArr = null;visControll.visAllReport = false;switchValue = 1;handleMonthPick();handleQuery()" 
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
        <el-form-item style="position: absolute;left: 23%;">
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
        <el-form-item style="position: absolute;left: 39%;">
          <el-button @click="handleQuery"  ><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        </el-form-item>
        <!-- <el-text size="large">
          报警次数：{{ pduInfo.alarm }}
        </el-text> -->

        <!-- 时间选择器2025422 -->
        <el-col :span="12">
              <div style="display: flex;align-items: center;justify-content: space-around">
                <el-select v-model="typeRadioShow" placeholder="请选择" :style="{width: '100px'}">
                  <el-option label="最大" value="最大" />
                  <el-option label="平均" value="平均" />
                  <el-option label="最小" value="最小" />
                </el-select>
                <!-- <el-radio-group v-model="timeRadio">
                  <el-radio-button label="近一小时" value="近一小时" :disabled="isHourDisabled" />
                  <el-radio-button label="近一天" value="近一天" />
                  <el-radio-button label="近三天" value="近三天" />
                  <el-radio-button label="近一月" value="近一月" />
                </el-radio-group> -->
              </div>
            </el-col>
     <!-- 时间选择器2025422 -->
      </el-form>
    </template>
    <template #Content>
      <div v-show="visControll.visAllReport" class="page" >
        <div class="page-con">
          <div class="pageBox" >
            <div class="page-conTitle">
              机房基本信息
            </div>
            <br/>
            <el-row :gutter="24" >
              <el-col :span="24 - serChartContainerWidth">
                <div class="centered-div">
                  <el-table 
                    :data="CabinetTableData" 
                    :header-cell-style="arraySpanMethod"
                    >
                    <el-table-column  align="center" label="基本信息" >
                      <el-table-column :show-header="false" prop="baseInfoName" />
                      <el-table-column :show-header="false" prop="baseInfoValue" />
                    </el-table-column>
                    
                      <!-- <template #default="scope">
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
                    </el-table-column> -->
                    <el-table-column  align="center" label="能耗" >
                      <el-table-column :show-header="false" prop="consumeName"  />
                      <el-table-column :show-header="false" prop="consumeValue" />
                    </el-table-column>
                    <el-table-column  align="center" label="占比" >
                      <el-table-column :show-header="false" prop="percentageName"  />
                      <el-table-column :show-header="false" prop="percentageValue" >
                        <template #default="scope">
                          <span v-if="scope.$index === 0 && scope.row.percentageValue != null">
                            <div class="progressContainer">
                              <div class="progress">
                                <div class="left" :style="`flex: ${scope.row.percentageValue}`">{{scope.row.percentageValue}}%</div>
                                <div class="line"></div>
                                <div class="right" :style="`flex: ${100 - scope.row.percentageValue}`">{{100 - scope.row.percentageValue}}%</div>
                              </div>
                            </div>                            
                          </span>
                        </template>
                      </el-table-column>
                    </el-table-column>
                    
                  </el-table>
                </div>
              </el-col>
              <!-- <el-col :span="serChartContainerWidth">
                <div class="right-div" ref="serChartContainer" id="serChartContainer" style="width: 29vw; height: 25vh;"></div>
              </el-col> -->
            </el-row>
          </div>
          <div class="pageBox" v-if="visControll.eqVis" >
            <div class="page-conTitle" >
              电量分布
            </div>
            <p class="paragraph" v-if="!visControll.isSameDay">本周期内，共计使用电量{{eqData.totalEle}}kWh，最大用电量{{eqData.maxEle}}kWh， 最大负荷发生时间{{eqData.maxEleTime}}</p>
            <p class="paragraph" v-if="visControll.isSameDay && eqData.firstEq">本周期内，开始时电能为{{eqData.firstEq}}kWh，结束时电能为{{eqData.lastEq}}kWh， 电能增长{{(eqData.lastEq - eqData.firstEq).toFixed(1)}}kWh</p>
            <Bar class="Container" width="70vw" height="58vh" :list="eleList"/>
          </div>
          <div class="pageBox"  v-if="visControll.pfVis">
            <div class="page-conTitle">
              功率因素曲线
            </div>        
            <PFLine class="Container"  width="70vw" height="58vh" :list="pfLineList"/>
          </div>
          <div class="pageBox"  v-if="visControll.powVis">
            <div class="page-conTitle">
              总平均功率曲线
            </div>
            <p class="paragraph">本周期内，最大视在功率{{powData.apparentPowMaxValue}}kVA， 发生时间{{powData.apparentPowMaxTime}}。最小视在功率{{powData.apparentPowMinValue}}kVA， 发生时间{{powData.apparentPowMinTime}}</p>
            <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;最大有功功率{{powData.activePowMaxValue}}kW， 发生时间{{powData.activePowMaxTime}}。最小有功功率{{powData.activePowMinValue}}kW， 发生时间{{powData.activePowMinTime}}</p>
            <Line class="Container"  width="70vw" height="58vh" :list="totalLineList"/>
          </div>
          <div class="pageBox"  v-if="visControll.ApowVis">
            <div class="page-conTitle">
              A路平均功率曲线
            </div>
            <p class="paragraph" >本周期内，最大视在功率{{powData.AapparentPowMaxValue}}kVA， 发生时间{{powData.AapparentPowMaxTime}}。最小视在功率{{powData.AapparentPowMinValue}}kVA， 发生时间{{powData.AapparentPowMinTime}}</p>
            <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;最大有功功率{{powData.AactivePowMaxValue}}kW， 发生时间{{powData.AactivePowMaxTime}}。最小有功功率{{powData.AactivePowMinValue}}kW， 发生时间{{powData.AactivePowMinTime}}</p>
            <Line class="Container" width="70vw" height="58vh" :list="aLineList"/>
          </div>
          <div class="pageBox"  v-if="visControll.BpowVis">
            <div class="page-conTitle">
              B路平均功率曲线
            </div>
            <p class="paragraph" >本周期内，最大视在功率{{powData.BapparentPowMaxValue}}kVA， 发生时间{{powData.BapparentPowMaxTime}}。最小视在功率{{powData.BapparentPowMinValue}}kVA， 发生时间{{powData.BapparentPowMinTime}}</p>
            <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;最大有功功率{{powData.BactivePowMaxValue}}kW， 发生时间{{powData.BactivePowMaxTime}}。最小有功功率{{powData.BactivePowMinValue}}kW， 发生时间{{powData.BactivePowMinTime}}</p>
            <Line class="Container" width="70vw" height="58vh" :list="bLineList"/>
          </div>
          <!-- <div class="pageBox" v-if="visControll.outletVis">
            <div class="page-conTitle" >
              输出位电量排名
            </div>
            <div ref="outputRankChartContainer" id="outputRankChartContainer" style="width: 70vw; height: 58vh;"></div>
          </div> -->

        </div>
        
      </div>
    </template>
  </CommonMenu>

  <!-- 表单弹窗：添加/修改 -->
  <!-- <PDUDeviceForm ref="formRef" @success="getList" /> -->
</template>

<script setup lang="ts">
// import download from '@/utils/download'
import { IndexApi } from '@/api/room/roomindex'
import * as echarts from 'echarts';
import { ElTree } from 'element-plus'
import Line from './component/Line.vue'
import PFLine from './component/PFLine.vue'
import Bar from './component/Bar.vue'



/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })

const location = ref() as any;
const pfLineList = ref() as any;
const eleList = ref() as any;
const totalLineList = ref() as any;
const aLineList = ref() as any;
const bLineList = ref() as any;
const idList = ref() as any;
const now = ref()
const switchValue = ref(1);

const visControll = reactive({
  visAllReport : false,
  isSameDay : false,
  eqVis : false,
  powVis : false,
  outletVis : false,
  iceTemVis : false,
  hotTemVis : false,
  ApowVis :false,
  BpowVis : false,
  pfVis: false,
})
const serChartContainerWidth = ref(0)

const loadAll = async () => {
  var data = await IndexApi.idList();
  var objectArray = data.map((str) => {
    return { value: str };
  });

  return objectArray;
}

const querySearch = (queryString: string, cb: any) => {
  const results = queryString
    ? idList.value.filter(createFilter(queryString))
    : idList.value
  // call callback function to return suggestions
  cb(results)
}

const createFilter = (query: string | number) => {
  const queryStr = typeof query === 'string' ? query.toLowerCase() : query.toString();
  return (item: { value: string | number }) => {
    const itemValueStr = typeof item.value === 'string' ? item.value.toLowerCase() : item.value.toString();
    if (typeof query === 'string') {
      return itemValueStr.startsWith(queryStr);
    } else {
      return itemValueStr === queryStr;
    }
  };
};


// 2025422
const typeRadioShow = ref("最大")
// 2025422

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

const CabinetTableData = ref([]) as any

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
  cascadeAddr : 0,
  dataType : 1
}) as any

const serverRoomArr =  ref([]) as any

//折叠功能


const getNavList = async() => {
  const res = await IndexApi.getRoomList()
  // console.log("res="+JSON. res);
  serverRoomArr.value = res.map(res => {
    return {
      id:res.id,
      name:res.roomName
    }
  });
  // if (res && res.length > 0) {
  //   const room = res[0]
  //   const keys = [] as string[]
  //   room.children.forEach(child => {
  //     if(child.children.length > 0) {
  //       child.children.forEach(son => {
  //         keys.push(son.id + '-' + son.type)
  //       })
  //     }
  //   })
  // }
}

const handleClick = (row) => {
  if(row.id != null){

    queryParams.id = row.id
    handleQuery();
  }
}

const arraySpanMethod = ({
  rowIndex,
}) => {
  if (rowIndex === 1) {
  //这里为了是将第二列的表头隐藏，就形成了合并表头的效果
      return {display: 'none'}
  }
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
})as any

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
})as any

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
const getList = async () => {
  if(queryParams.id == null){
    return;
  }
  loading.value = true
  
  await handleConsumeQuery();
  await handlePowQuery();
  await handleDetailQuery();
  // await handlePFLineQuery();

  visControll.visAllReport = true;
  loading.value = false

}

// const handlePFLineQuery = async () => {
//   const data = await IndexApi.getRoomPFLine(queryParams);
//   pfLineList.value = data.pfLineRes;
  
//   if(pfLineList.value?.time != null && pfLineList.value?.time?.length > 0){
//     visControll.pfVis = true;
//   }else {
//     visControll.pfVis = false;
//   }
// }


const handlePowQuery = async () => {
 
  powData.value = await IndexApi.getPowData(queryParams);
  totalLineList.value = powData.value.totalLineRes;
  aLineList.value = powData.value.aLineRes;
  bLineList.value = powData.value.bLineRes;
  pfLineList.value = powData.value.pfLineRes;
  

  if(pfLineList.value?.time != null && pfLineList.value?.time?.length > 0){
    visControll.pfVis = true;
  }else {
    visControll.pfVis = false;
  }


  if(totalLineList.value?.time != null && totalLineList.value?.time?.length > 0){
    powData.value.apparentPowMaxValue = powData.value.apparentPowMaxValue?.toFixed(3);
    powData.value.apparentPowMinValue =  powData.value.apparentPowMinValue?.toFixed(3);
    powData.value.activePowMaxValue = powData.value.activePowMaxValue?.toFixed(3);
    powData.value.activePowMinValue = powData.value.activePowMinValue?.toFixed(3);





    visControll.powVis = true;
  }else{
    visControll.powVis = false;
  }
  
  if(aLineList.value?.time != null && aLineList.value?.time?.length > 0){
    powData.value.AapparentPowMaxValue = powData.value.AapparentPowMaxValue?.toFixed(3);
    powData.value.AapparentPowMinValue =  powData.value.AapparentPowMinValue?.toFixed(3);
    powData.value.AactivePowMaxValue = powData.value.AactivePowMaxValue?.toFixed(3);
    powData.value.AactivePowMinValue = powData.value.AactivePowMinValue?.toFixed(3);
    visControll.ApowVis = true;
  }else{
    visControll.ApowVis = false;
  }

  if(bLineList.value?.time != null && bLineList.value?.time?.length > 0){
    powData.value.BapparentPowMaxValue = powData.value.BapparentPowMaxValue?.toFixed(3);
    powData.value.BapparentPowMinValue =  powData.value.BapparentPowMinValue?.toFixed(3);
    powData.value.BactivePowMaxValue = powData.value.BactivePowMaxValue?.toFixed(3);
    powData.value.BactivePowMinValue = powData.value.BactivePowMinValue?.toFixed(3);
    visControll.BpowVis = true;
  }else{
    visControll.BpowVis = false;
  }
}

const handleConsumeQuery = async () => {
  eqData.value = await IndexApi.getConsumeData(queryParams);
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
}

const handleDetailQuery = async () => {
  var temp = [] as any;
  
  var data = await IndexApi.getRoomBalancePage({id : queryParams.id});
  var RoomInfo = data.list[0];

  location.value = RoomInfo?.location;
  temp.push({
    baseInfoName : "所属位置",
    baseInfoValue : RoomInfo?.location ,
    consumeName : "当前总视在功率",
    consumeValue : RoomInfo?.powApparentTotal != null ? RoomInfo?.powApparentTotal.toFixed(3) + "kVA" : '/',
    percentageName: "当前AB路占比",
    percentageValue: RoomInfo.rateA != null ? RoomInfo.rateA.toFixed(0) : 50,
  })
  temp.push({
    consumeName : "当前总有功功率",
    consumeValue : RoomInfo?.powActiveTotal != null ?  RoomInfo?.powActiveTotal?.toFixed(3) + "kW" : '/'
  })
  temp.push({
    consumeName : "当前总无功功率",
    consumeValue : RoomInfo?.powReactiveTotal != null ? RoomInfo?.powReactiveTotal?.toFixed(3) + "kVar" : '/'
  })
  CabinetTableData.value = temp;
}

watch(filterText, (val) => {
  treeRef.value!.filter(val)
})

// 2025422
watch( ()=>typeRadioShow.value, async()=>{
  if(typeRadioShow.value =="最大"){
    queryParams.dataType = 1
  }else if(typeRadioShow.value =="平均") {
    queryParams.dataType = 0
  }else if(typeRadioShow.value =="最小"){
    queryParams.dataType = -1
  }
  handlePowQuery()
})


// const message = useMessage() // 消息弹窗
// const { t } = useI18n() // 国际化

const loading = ref(false) // 列表的加载中

// const total = ref(0) // 列表的总页数
const queryFormRef = ref() // 搜索的表单
// const exportLoading = ref(false) // 导出的加载中

/** 搜索按钮操作 */
const handleQuery = async () => {
  if(queryParams.id){
    if(queryParams.oldTime && queryParams.newTime){
      await getList();
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
  idList.value = await loadAll();
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
      // background-color: #3b8bf5;
      background-color: #E5B849;
    }
    .right {
      text-align: center;
      background-color:  #C8603A;
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
    background: #01ada8 !important;
    color: #fff;
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
