<template>
  <CommonMenu :showCheckbox="false" @node-click="handleClick" :showSearch="false"  :lazy="true" :load="loadNode" navTitle="母线报表">
    <template #NavInfo>
      <div >
        <div class="header">
          <div class="header_img"><img alt="" src="@/assets/imgs/Bus.png" /></div>
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

        <el-form-item label="网络地址" prop="devKey">
          <el-autocomplete
            v-model="queryParams.devKey"
            :fetch-suggestions="querySearch"
            clearable
            class="!w-200px"
            placeholder="请输入网络地址"
            @select="handleQuery"
          />
        </el-form-item>

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
              母线基本信息
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
                            <el-tag type="info" v-if="scope.row.baseInfoValue == 5">离线</el-tag>
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
            </el-row>
          </div>
          <div class="pageBox" v-if="visControll.eqVis" >
            <div class="page-conTitle" >
              电量分布
            </div>
            <p v-if="!visControll.isSameDay">本周期内，共计使用电量{{eqData.totalEle}}kWh，最大用电量{{eqData.maxEle}}kWh， 最大负荷发生时间{{eqData.maxEleTime}}</p>
            <p v-if="visControll.isSameDay">本周期内，开始时电能为{{eqData.firstEq}}kWh，结束时电能为{{eqData.lastEq}}kWh， 电能增长{{(eqData.lastEq - eqData.firstEq).toFixed(1)}}kWh</p>
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
              平均功率曲线
            </div>
            <p>本周期内，最大视在功率{{powData.apparentPowMaxValue}}kVA， 发生时间{{powData.apparentPowMaxTime}}。最小视在功率{{powData.apparentPowMinValue}}kVA， 发生时间{{powData.apparentPowMinTime}}</p>
            <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;最大有功功率{{powData.activePowMaxValue}}kVA， 发生时间{{powData.activePowMaxTime}}。最小有功功率{{powData.activePowMinValue}}kVA， 发生时间{{powData.activePowMinTime}}</p>
            <Line class="Container"  width="70vw" height="58vh" :list="totalLineList"/>
          </div>
          <div class="pageBox" v-if="visControll.temVis">
            <div class="page-conTitle">
              温度曲线
            </div>
            <p v-show="temData.temAMaxValue">本周期内，A相最高温度{{temData.temAMaxValue}}°C， 最高温度发生时间{{temData.temAMaxTime}}&nbsp;&nbsp;最低温度{{temData.temAMinValue}}°C， 最低温度发生时间{{temData.temAMinTime}}</p>        
            <p v-show="temData.temBMaxValue">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;B相最高温度{{temData.temBMaxValue}}°C， 最高温度发生时间{{temData.temBMaxTime}}&nbsp;&nbsp;最低温度{{temData.temBMinValue}}°C， 最低温度发生时间{{temData.temBMinTime}}</p>        
            <p v-show="temData.temCMaxValue">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;C相最高温度{{temData.temCMaxValue}}°C， 最高温度发生时间{{temData.temCMaxTime}}&nbsp;&nbsp;最低温度{{temData.temCMinValue}}°C， 最低温度发生时间{{temData.temCMinTime}}</p>          
            <p v-show="temData.temNMaxValue">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;N相最高温度{{temData.temNMaxValue}}°C， 最高温度发生时间{{temData.temNMaxTime}}&nbsp;&nbsp;最低温度{{temData.temNMinValue}}°C， 最低温度发生时间{{temData.temNMinTime}}</p>            
            <EnvTemLine  width="70vw" height="58vh" :list="temList"  />
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
import * as echarts from 'echarts';
import { ElTree } from 'element-plus'
import { IndexApi } from '@/api/bus/busindex'
import type Node from 'element-plus/es/components/tree/src/model/node'
import Line from './component/Line.vue'
import PFLine from './component/PFLine.vue'
import Bar from './component/Bar.vue'
import EnvTemLine from './component/EnvTemLine.vue'


/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })


const temList = ref() as any;
const eleList = ref() as any;
const totalLineList = ref() as any;
const pfLineList = ref() as any;
const now = ref()
const switchValue = ref(1);
const visControll = reactive({
  visAllReport : false,
  isSameDay : false,
  eqVis : false,
  powVis : false,
  outletVis : false,
  temVis : false,
  pfVis: false,
})
const serChartContainerWidth = ref(0)


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

const devKeyList = ref([])
const loadAll = async () => {
  var data = await IndexApi.devKeyList();
  var objectArray = data.map((str) => {
    return { value: str };
  });
  return objectArray;
}

const areDatesEqual = (date1, date2) => {
  return (
    date1.getFullYear() === date2.getFullYear() &&
    date1.getMonth() === date2.getMonth() &&
    date1.getDate() === date2.getDate()
  );
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

const loadNode = async (node: Node, resolve: (data: Tree[]) => void) => {
  if (node.level === 0) {
    var temp = await IndexApi.getBusMenu();
    return resolve(temp)
  }
  // if (node.level === 1){
  //   var temp = await CabinetApi.getRoomPDUList({id : node.data.id});
  //   return resolve(temp[0].children);
  // } 
  if (node.level >= 1){
    return resolve(node?.data?.children);
  } 
}


const handleClick = (row) => {
  if(row.type != null  && row.type == 6){
    queryParams.devKey = row.unique
    handleQuery();
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
}) as any

interface PowData {
  apparentPowAvgValue: number[];
  activePowAvgValue: number[];
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
}
const powData = ref<PowData>({
  apparentPowAvgValue : [],
  activePowAvgValue: [],
  time:[],
  total_pow_apparent : 0,
  apparentPowMaxValue : 0,
  apparentPowMaxTime : "",
  apparentPowMinValue : 0,
  apparentPowMinTime : "",
  activePowMaxValue : 0,
  activePowMaxTime : "",
  activePowMinValue : 0,
  activePowMinTime : ""
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

const getList = async () => {
  loading.value = true
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
  
  const data = await IndexApi.getBusPFLine(queryParams);
  pfLineList.value = data.pfLineRes;
  
  if(pfLineList.value?.time != null && pfLineList.value?.time?.length > 0){
    visControll.pfVis = true;
  }else {
    visControll.pfVis = false;
  }

  powData.value = await IndexApi.getPowData(queryParams);
  totalLineList.value = powData.value.totalLineRes;
  if(totalLineList.value?.time != null && totalLineList.value?.time?.length > 0){
    powData.value.apparentPowMaxValue = powData.value.apparentPowMaxValue?.toFixed(3);
    powData.value.apparentPowMinValue =  powData.value.apparentPowMinValue?.toFixed(3);
    powData.value.activePowMaxValue = powData.value.activePowMaxValue?.toFixed(3);
    powData.value.activePowMinValue = powData.value.activePowMinValue?.toFixed(3);
    visControll.powVis = true;
  }else{
    visControll.powVis = false;
  }
  
  temData.value = await IndexApi.getTemData(queryParams);
  temList.value = temData.value.lineRes;
  if(temList.value?.time != null && temList.value?.time?.length > 0 ){

    temData.value.temAMinValue = temData.value.temAMinValue?.toFixed(2);
    temData.value.temAMaxValue = temData.value.temAMaxValue?.toFixed(2);

    temData.value.temBMinValue = temData.value.temBMinValue?.toFixed(2);
    temData.value.temBMaxValue = temData.value.temBMaxValue?.toFixed(2);

    temData.value.temCMinValue = temData.value.temCMinValue?.toFixed(2);
    temData.value.temCMaxValue = temData.value.temCMaxValue?.toFixed(2);

    temData.value.temNMinValue = temData.value.temNMinValue?.toFixed(2);
    temData.value.temNMaxValue = temData.value.temNMaxValue?.toFixed(2);
    visControll.temVis = true;
  }else{
    visControll.temVis = false;
  }

  var Bus = await IndexApi.getBusRedisByDevKey(queryParams);
  Bus = JSON.parse(Bus)
  var temp = [] as any;
  var baseInfo = await IndexApi.getIndexPage(queryParams);

  temp.push({
    baseInfoName : "所属位置",
    baseInfoValue : baseInfo?.list && baseInfo?.list.length > 0 ? baseInfo?.list[0].location : "/",
    consumeName : "消耗电量",
    consumeValue : eqData.value?.barRes?.series && eqData.value?.barRes?.series.length > 0? visControll.isSameDay ? (eqData.value.lastEq - eqData.value.firstEq).toFixed(1) + "kWh" : eqData.value.totalEle + "kWh" : '/',
  })
  temp.push({
    baseInfoName : "网络地址",
    baseInfoValue : queryParams.devKey,
    consumeName : "当前视在功率",
    consumeValue : Bus?.bus_data?.bus_total_data ? Bus.bus_data.bus_total_data.pow_apparent.toFixed(3) + "kVA" : '/'
  })
  temp.push({
    baseInfoName : "设备状态",
    baseInfoValue : Bus?.status != null ? Bus.status : '/',
    pduAlarm : Bus?.pdu_alarm,
    consumeName : "当前功率因素",
    consumeValue : Bus?.bus_data?.bus_total_data ? Bus.bus_data.bus_total_data.power_factor?.toFixed(2) : '/'
  })
  PDUTableData.value = temp;
  
  visControll.visAllReport = true;
  // initChart();
  loading.value = false

}

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

  if(queryParams.devKey){
    if(queryParams.oldTime && queryParams.newTime){
      await getList();
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
  devKeyList.value = await loadAll();
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
