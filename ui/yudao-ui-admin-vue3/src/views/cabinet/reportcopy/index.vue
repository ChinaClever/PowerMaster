<template>
  <CommonMenu :showCheckbox="false" @node-click="handleClick" :showSearch="true" :dataList="serverRoomArr" navTitle="机柜报表">
    <template #NavInfo >
      <div >
        <!-- <div class="header">
          <div class="header_img"><img alt="" src="@/assets/imgs/wmk.jpg" /></div>
          <div class="name">机柜</div>
          <div></div>
        </div> -->
        <!-- <div class="line"></div> -->
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
        <!-- <div class="line"></div> -->
         <br/>

      </div>
    </template>
    <template #ActionBar>
      <el-form
        class="-mb-15px"
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        style="float: left;"

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
          <el-button @click="handleExport"  ><Icon icon="ep:search" class="mr-5px" :loading="true" /> 导出</el-button>
        </el-form-item>
        <!-- <el-text size="large">
          报警次数：{{ pduInfo.alarm }}
        </el-text> -->
      </el-form>
    </template>
    <template #Content>
      <div id="pdfRef">
      <div v-show="visControll.visAllReport" class="page" >
        <div class="page-con">
          <div class="pageBox" >
            <div class="page-conTitle">
              机柜基本信息
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
            </el-row>
          </div>

          <div class="pageBox" >
            <div class="page-conTitle">
              机架基本信息
            </div>
            <br/>
            <el-row :gutter="24" >
              <el-col :span="24 - serChartContainerWidth">
                <div class="centered-div">
                  <el-table 
                    :data="rack" 
                    :header-cell-style="arraySpanMethod"
                    >
                    <el-table-column  align="center" label="序号" type="index" prop="id" />
                    <el-table-column  align="center" label="名称" prop="name"  />
                    <el-table-column  align="center" label="总功率" prop="totalPower" />
                    <el-table-column  align="center" label="A路电流" prop="acurrent" />
                    <el-table-column  align="center" label="B路电流" prop="bcurrent" />
                    <el-table-column label="操作" align="center">
                    <template #default="scope">
                    <el-button @click="generateDailyReport(scope.row.id)">日报</el-button>
                    <el-button @click="generateMonthlyReport(scope.row.id)">月报</el-button>
                    </template>
      </el-table-column>
                  </el-table>
                </div>
              </el-col>
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
          <div class="pageBox" v-if="isPDU">
            <div class="page-conTitle">
              A路相电流历史曲线趋势图
            </div>
            <ACurLine class="adaptiveStyle" :list="AcurVolData" v-if="dataLoaded"/>
          </div>
          <div class="pageBox" v-if="isPDU">
            <div class="page-conTitle">
              A路相电压历史曲线趋势图
            </div>
            <AVolLine class="adaptiveStyle" :list="AcurVolData" v-if="dataLoaded"/>
          </div>
          <div class="pageBox" v-if="isPDU">
            <div class="page-conTitle">
              B路相电流历史曲线趋势图
            </div>
            <BBCurLine class="adaptiveStyle" :list="BcurVolData" v-if="dataLoaded"/>
          </div>
          <div class="pageBox" v-if="isPDU">
            <div class="page-conTitle">
              B路相电压历史曲线趋势图
            </div>
            <BVolLine class="adaptiveStyle" :list="BcurVolData" v-if="dataLoaded"/>
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
            <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;最大有功功率{{powData.activePowMaxValue}}kVA， 发生时间{{powData.activePowMaxTime}}。最小有功功率{{powData.activePowMinValue}}kVA， 发生时间{{powData.activePowMinTime}}</p>
            <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;最大无功功率{{powData.reactivePowMaxValue}}kVA， 发生时间{{powData.reactivePowMaxTime}}。最小有功功率{{powData.reactivePowMinValue}}kVA， 发生时间{{powData.reactivePowMinTime}}</p>
            <Line class="Container"  width="70vw" height="58vh" :list="totalLineList"/>
          </div>
          <div class="pageBox"  v-if="visControll.ApowVis">
            <div class="page-conTitle">
              A路平均功率曲线
            </div>
            <p class="paragraph" >本周期内，最大视在功率{{powData.AapparentPowMaxValue}}kVA， 发生时间{{powData.AapparentPowMaxTime}}。最小视在功率{{powData.AapparentPowMinValue}}kVA， 发生时间{{powData.AapparentPowMinTime}}</p>
            <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;最大有功功率{{powData.AactivePowMaxValue}}kVA， 发生时间{{powData.AactivePowMaxTime}}。最小有功功率{{powData.AactivePowMinValue}}kVA， 发生时间{{powData.AactivePowMinTime}}</p>
            <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;最大有功功率{{powData.AreactivePowMaxValue}}kVA， 发生时间{{powData.AreactivePowMaxTime}}。最小有功功率{{powData.AreactivePowMinValue}}kVA， 发生时间{{powData.AreactivePowMinTime}}</p>
            <Line class="Container" width="70vw" height="58vh" :list="aLineList"/>
          </div>
          <div class="pageBox"  v-if="visControll.BpowVis">
            <div class="page-conTitle">
              B路平均功率曲线
            </div>
            <p class="paragraph" >本周期内，最大视在功率{{powData.BapparentPowMaxValue}}kVA， 发生时间{{powData.BapparentPowMaxTime}}。最小视在功率{{powData.BapparentPowMinValue}}kVA， 发生时间{{powData.BapparentPowMinTime}}</p>
            <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;最大有功功率{{powData.BactivePowMaxValue}}kVA， 发生时间{{powData.BactivePowMaxTime}}。最小有功功率{{powData.BactivePowMinValue}}kVA， 发生时间{{powData.BactivePowMinTime}}</p>
            <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;最大有功功率{{powData.BreactivePowMaxValue}}kVA， 发生时间{{powData.BreactivePowMaxTime}}。最小有功功率{{powData.BreactivePowMinValue}}kVA， 发生时间{{powData.BreactivePowMinTime}}</p>
            <Line class="Container" width="70vw" height="58vh" :list="bLineList"/>
          </div>
          <div class="pageBox" v-if="visControll.BpowVis">
            <div class="page-conTitle" >
              机架耗电电量排名（先写死，后续根据需要修改）
            </div>
            <HorizontalBar :width="computedWidth" height="58vh" />
          </div>
          <!-- <div class="pageBox" v-if="visControll.outletVis">
            <div class="page-conTitle" >
              输出位电量排名
            </div>
            <div ref="outputRankChartContainer" id="outputRankChartContainer" style="width: 70vw; height: 58vh;"></div>
          </div> -->
          <div class="pageBox" v-if="visControll.iceTemVis">
            <div class="page-conTitle">
             冷通道温度曲线
            </div>
            <p class="paragraph" v-show="iceTemList.temMaxValue">本周期内，最高温度{{iceTemList.temMaxValue}}°C， 最高温度发生时间{{iceTemList.temMaxTime}}，由温度传感器{{iceTemList.temMaxSensorId}}采集得到</p>
            <p class="paragraph" v-show="iceTemList.temMinValue">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;最低温度{{iceTemList.temMinValue}}°C， 最高温度发生时间{{iceTemList.temMinTime}}，由温度传感器{{iceTemList.temMinSensorId}}采集得到</p>
            <EnvTemLine class="Container" width="70vw" height="58vh" :list="iceTemList" />
          </div>
          <div class="pageBox" v-if="visControll.hotTemVis">
            <div class="page-conTitle">
              热通道温度曲线
            </div>
            <p class="paragraph" v-show="hotTemList.temMaxValue">本周期内，最高温度{{hotTemList.temMaxValue}}°C， 最高温度发生时间{{hotTemList.temMaxTime}}，由温度传感器{{hotTemList.temMaxSensorId}}采集得到</p>
            <p class="paragraph" v-show="hotTemList.temMinValue">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;最低温度{{hotTemList.temMinValue}}°C， 最高温度发生时间{{hotTemList.temMinTime}}，由温度传感器{{hotTemList.temMinSensorId}}采集得到</p>
            <EnvTemLine class="Container" width="70vw" height="58vh" :list="hotTemList" />
          </div>
        </div>
        
      </div>
    </div>
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
import Line from './component/Line.vue'
import PFLine from './component/PFLine.vue'
import Bar from './component/Bar.vue'
import EnvTemLine from './component/EnvTemLine.vue'
import { PDUDeviceApi } from '@/api/pdu/pdudevice'
import ACurLine from './component/AcurLine.vue'
import AVolLine from './component/AvolLine.vue'
import BBCurLine from './component/BcurLine.vue'
import BVolLine from './component/BvolLine.vue'
import HorizontalBar from './component/HorizontalBar.vue'
// 引入方法
import { htmlPdf } from "@/utils/htmlToPDF.js"  
	// 导出成PDF
	const handleExport = (name) => {
	  var fileName= '机柜报表'
	  const fileList = document.getElementsByClassName('pdfRef')   // 很重要
	  htmlPdf(fileName, document.querySelector('#pdfRef'), fileList)
	}


/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })

const AcurVolData = ref();
const BcurVolData = ref();
const dataLoaded = ref(false);

const pfLineList = ref() as any;
const iceTemList = ref([]) as any;
const hotTemList = ref([]) as any;
const eleList = ref() as any;
const totalLineList = ref() as any;
const aLineList = ref() as any;
const bLineList = ref() as any;
const idList = ref() as any;
const now = ref()
const switchValue = ref(1);

let lineidChartA = null as echarts.ECharts | null; // 显式声明 rankChart 的类型
const lineidChartContainerA = ref<HTMLElement | null>(null);
let lineidChartOneA = null as echarts.ECharts | null; // 显式声明 rankChart 的类型
const lineidChartContainerOneA = ref<HTMLElement | null>(null);
let lineidChartB = null as echarts.ECharts | null;
const lineidChartContainerB = ref<HTMLElement | null>(null);
let lineidChartOneB = null as echarts.ECharts | null;
const lineidChartContainerOneB = ref<HTMLElement | null>(null);
const dateTimeName = ref('seventytwoHour')


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
  flag: false,
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

const { push } = useRouter()

const generateDailyReport = (id) => {
      push('/u/rackreport?id='+id+'&Type='+0);
    };

    const generateMonthlyReport = (id) => {
      push('/u/rackreport?id='+id+'&Type='+1);
    };


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
  cascadeAddr : 0
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

    queryParams.Id = row.id
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
  loading.value = true
  
  await handleConsumeQuery();
  await handlePowQuery();
  await handleIceQuery();
  await handleHotQuery();
  await handleDetailQuery();
  await handlePFLineQuery();

  visControll.visAllReport = true;
  loading.value = false

}

const handlePFLineQuery = async () => {
  const data = await IndexApi.getCabinetPFLine(queryParams);
  pfLineList.value = data.pfLineRes;
  //保留俩位小数
  
  
  if(pfLineList.value?.time != null && pfLineList.value?.time?.length > 0){
    visControll.pfVis = true;
  }else {
    visControll.pfVis = false;
  }
}

const AlChartData = ref({
  volValueList : [] as number[], //电压
  curValueList : [] as number[] //电流
});
const AllChartData = ref({
  volValueList : [] as number[],
  curValueList : [] as number[]
});
const AlllChartData = ref({
  volValueList : [] as number[],
  curValueList : [] as number[]
});

const BlChartData = ref({
  volValueList : [] as number[], //电压
  curValueList : [] as number[] //电流
});
const BllChartData = ref({
  volValueList : [] as number[],
  curValueList : [] as number[]
});
const BlllChartData = ref({
  volValueList : [] as number[],
  curValueList : [] as number[]
});

const AlineidDateTimes = ref([] as string[])

  const BlineidDateTimes = ref([] as string[])

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

const isPDU = ref(true);

const PDUHdaLineHisdata = async () => {
  try {
    const result = await PDUDeviceApi.getPDUHdaLineHisdataByCabinet({ CabinetId: queryParams.Id, type: dateTimeName.value });

    AcurVolData.value = result.A;
    BcurVolData.value = result.B;
    console.log('BBBBBBBBBBBBBBBBBBBBBBBB', BcurVolData.value);
    console.log('BBBBBBBBBBBBBBBBBBBBBBBB', BcurVolData.value);

    dataLoaded.value = true;
  }catch (error) {
    isPDU.value = false;
  }
};
const rack = ref([] as any);
//获取机架信息
const getRackByCabinet = async () => {
  const result = await IndexApi.getRackByCabinet({id : queryParams.Id});
  
  // 保留俩位小数
  for (let i = 0; i < result.length; i++) {
    result[i].acurrent = result[i].acurrent?.toFixed(2);
    result[i].bcurrent = result[i].bcurrent?.toFixed(2);
    result[i].totalPower = result[i].totalPower?.toFixed(3);
  }
  rack.value = result;
  console.log("机架信息",result);
  console.log("机架信息",rack.value);
}

const handleIceQuery = async () => {
  if(queryParams.Id != null){ 
    const data = await IndexApi.getCabinetIceTemAndHumById({id : queryParams.Id,timeType : queryParams.timeType, oldTime : queryParams.oldTime, newTime : queryParams.newTime});

    if(data?.temResult?.time != null && data?.temResult?.time?.length > 0){
      console.log(1)
      iceTemList.value = data;
      console.log("iceTemList.value ",iceTemList.value )
      if(iceTemList.value?.temMinValue != null){
        iceTemList.value.temMinValue = iceTemList.value?.temMinValue?.toFixed(2);
      }
      if(iceTemList.value?.temMaxValue != null){
        iceTemList.value.temMaxValue = iceTemList.value?.temMaxValue?.toFixed(2);
      }
      visControll.iceTemVis = true;
    }else{
      visControll.iceTemVis = false;
    }
  }
}

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

const handleHotQuery = async () => {
  if(queryParams.Id != null){
    const data = await IndexApi.getCabinetHotTemAndHumById({id : queryParams.Id,timeType : queryParams.timeType, oldTime : queryParams.oldTime, newTime : queryParams.newTime});
    if(data?.temResult?.time != null && data?.temResult?.time?.length > 0){
      console.log(2)
      hotTemList.value = data;
      if(hotTemList.value?.temMinValue != null){
        hotTemList.value.temMinValue = hotTemList.value?.temMinValue?.toFixed(2);
      }
      if(hotTemList.value?.temMaxValue != null){
        hotTemList.value.temMaxValue = hotTemList.value?.temMaxValue?.toFixed(2);
      }
      visControll.hotTemVis = true;
    }else{
      visControll.hotTemVis = false;
    }
  }
}

const handlePowQuery = async () => {
  powData.value = await IndexApi.getPowData(queryParams);
  totalLineList.value = powData.value.totalLineRes;
  aLineList.value = powData.value.aLineRes;
  bLineList.value = powData.value.bLineRes;

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
  
  var CabinetInfo1 = await CabinetApi.getCabinetDetail({id : queryParams.Id});
  var CabinetInfo = CabinetInfo1.redisData;
  var apow = CabinetInfo?.cabinet_power?.path_a?.pow_active;
  var bpow = CabinetInfo?.cabinet_power?.path_b?.pow_active;
  var percentageValue = 50 as any;
  if(apow == null && bpow == null){
    percentageValue = null;
  } else if (apow != null && bpow == null){
    percentageValue = 100;
  } else if (apow == null && bpow != null){
    percentageValue = 0;
  } else if (apow != 0 && bpow == 0){
    percentageValue = 100;
  } else if (apow == 0 && bpow != 0){
    percentageValue = 0;
  } else if (apow != 0 && bpow != 0) {
    percentageValue = apow / (apow + bpow);
    percentageValue *= 100;
  }
  
  temp.push({
    baseInfoName : "所属位置",
    baseInfoValue : CabinetInfo1?.name,
    consumeName : "当前总视在功率",
    consumeValue : CabinetInfo?.cabinet_power?.total_data?.pow_apparent != null ? CabinetInfo?.cabinet_power?.total_data?.pow_apparent?.toFixed(3) + "kVA" : '/',
    percentageName: "当前AB路占比",
    percentageValue: percentageValue?.toFixed(0),
  })
  temp.push({
    baseInfoName : "电力容量",
    baseInfoValue : CabinetInfo?.pow_capacity != null ?  CabinetInfo?.pow_capacity : '/',
    consumeName : "当前总有功功率",
    consumeValue : CabinetInfo?.cabinet_power?.total_data?.pow_active != null ? CabinetInfo?.cabinet_power?.total_data?.pow_active?.toFixed(3) + "kW" : '/'
  })
  temp.push({
    baseInfoName : "负载率",
    baseInfoValue : CabinetInfo?.load_factor != null ? CabinetInfo?.load_factor?.toFixed(2) + "%" : '/',
    consumeName : "当前总无功功率",
    consumeValue : CabinetInfo?.cabinet_power?.total_data?.pow_reactive != null ? CabinetInfo?.cabinet_power?.total_data?.pow_reactive?.toFixed(3) + "kVar" : '/'
  })
  CabinetTableData.value = temp;
}

watch(filterText, (val) => {
  treeRef.value!.filter(val)
})


// const message = useMessage() // 消息弹窗
// const { t } = useI18n() // 国际化

const loading = ref(false) // 列表的加载中

// const total = ref(0) // 列表的总页数
const queryFormRef = ref() // 搜索的表单
// const exportLoading = ref(false) // 导出的加载中

/** 搜索按钮操作 */
const handleQuery = async () => {

  if(queryParams.Id){
    if(queryParams.oldTime && queryParams.newTime){
      await getList();
      await PDUHdaLineHisdata();
      // await lineidFlashChartData();
      await getRackByCabinet();
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
      background-color: #3b8bf5;
    }
    .right {
      text-align: center;
      background-color:  #f86f13;
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
// :deep(.el-form .el-form-item) {
//   margin-right: 0;
// }

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
