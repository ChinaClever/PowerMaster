<template>
 <CommonMenu1 :dataList="navList" @node-click="handleClick" navTitle="机房趋势分析" :showCheckbox="false" :hightCurrent="true" :currentKey="currentKey">
    <template #NavInfo>
      <br/>
      <div class="nav_data">
        <div v-if="nowAddress" style="display: flex;justify-content: center;"><span>{{nowAddress}}</span></div>
        <div class="nav_header">
           <div  class="description-item" v-if="maxActivePowDataTemp!=null">
            <span class="label">最大值 :</span>
            <span >{{ formatNumber(maxActivePowDataTemp, 3) }} kW</span>
          </div>
          <div v-if="maxActivePowDataTimeTemp" class="description-item">
            <span class="label">发生时间 :</span>
            <span class="value">{{ formatDate(maxActivePowDataTimeTemp, 'YYYY-MM-DD') }}</span>
          </div>
          <div  class="description-item" v-if="minActivePowDataTemp!=null">
            <span class="label">最小值 :</span>
            <span >{{ formatNumber(minActivePowDataTemp, 3) }} kW</span>
          </div>
          <div v-if="minActivePowDataTimeTemp" class="description-item">
            <span class="label">发生时间 :</span>
            <span class="value">{{ formatDate(minActivePowDataTimeTemp, 'YYYY-MM-DD')  }}</span>
          </div> 
        </div>
      </div>
    </template>
    <template #ActionBar>
      <el-tabs v-model="activeName">
        <el-tab-pane label="天极值数据" name="dayExtremumTabPane"/>
        <el-tab-pane label="小时极值数据" name="hourExtremumTabPane"/>
        <el-tab-pane label="原始数据" name="realtimeTabPane"/>
      </el-tabs>
       <!-- 搜索工作栏 -->
       <el-form
         class="-mb-15px"
         :model="queryParams"
         ref="queryFormRef"
         :inline="true"
         label-width="70px"
       >
        <el-form-item label="参数类型" prop="type">
           <el-select
             v-model="paramType"
             class="!w-120px">
             <el-option label="总" value="total" />
             <el-option label="A路" value="a" />
             <el-option label="B路" value="b" />
           </el-select>
         </el-form-item>

      <el-form-item label="时间段" prop="timeRange" >
          <el-date-picker
            value-format="YYYY-MM-DD HH:mm:ss"
            v-model="queryParams.timeRange"
            type="datetimerange"
            :shortcuts="activeName === 'realtimeTabPane' ? shortcuts : (activeName === 'hourExtremumTabPane' ? shortcuts1 : (activeName === 'dayExtremumTabPane' ? shortcuts2 : []))"
            range-separator="-"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            :disabled-date="disabledDate"
            class="!w-350px"
          />
                 <!-- @change="handleDayPick" -->
        </el-form-item>

        <el-form-item >
           <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
         </el-form-item>

        <el-form-item>
          <el-button-group>
            <el-button @click="changeTime('pre')"><el-icon class="el-icon--right"><ArrowLeft /></el-icon>{{pre}}</el-button>
            <el-button @click="changeTime('next')">{{next}}<el-icon class="el-icon--right"><ArrowRight /></el-icon></el-button>
          </el-button-group>
         </el-form-item>
         
         <el-form-item style="position: absolute; right: 0;">
          <el-button type="success" plain @click="handleExport" :loading="exportLoading">
             <Icon icon="ep:download" class="mr-5px" /> 导出
           </el-button>
         </el-form-item>
       </el-form>
    </template>
    <template #Content>
      <div v-loading="loading">
        <el-tabs v-model="activeName1">
          <el-tab-pane label="图表" name="myChart">
            <div v-show="isHaveData" ref="chartContainer" id="chartContainer" style="width: 70vw; height: 65vh;"></div>
          </el-tab-pane>
          <el-tab-pane label="数据" name="myData">
            <div style="height: 67vh;">
            <el-table  
              v-loading="tableLoading"
              v-show="isHaveData"
              :border="true"
              :stripe="true"
              :data="tableData"
              style="height: 67vh; width: 99.97%;"
              :header-cell-style="{ backgroundColor: '#F5F7FA', color: '#909399', textAlign: 'center', borderLeft: '1px #EDEEF2 solid', borderBottom: '1px #EDEEF2 solid', fontFamily: 'Microsoft YaHei',fontWeight: 'bold'}"
              :cell-style="{ color: '#606266', fontSize: '14px', textAlign: 'center', borderBottom: '0.25px #F5F7FA solid', borderLeft: '0.25px #F5F7FA solid' }"
              :row-style="{ fontSize: '14px', textAlign: 'center', }"
              empty-text="暂无数据" max-height="818">
              <!-- 添加行号列 -->
              <el-table-column label="序号" align="center" width="80px">
                <template #default="{ $index }">
                  {{ $index + 1 }}
                </template> 
              </el-table-column>
              <el-table-column prop="create_time" label="记录时间" />
              <el-table-column v-if="activeName!='realtimeTabPane'&&paramType=='total'" label="总有功功率(kW)">
                <el-table-column prop="总平均有功功率" label="平均值" />
                <el-table-column prop="总最大有功功率" label="最大值"/>   
                <el-table-column prop="totalActivePowMaxTimeData" label="发生时间"/>
                <el-table-column prop="总最小有功功率" label="最小值"/>   
                <el-table-column prop="totalActivePowMinTimeData" label="发生时间"/>
              </el-table-column>
              <el-table-column v-if="activeName!='realtimeTabPane'&&paramType=='total'" label="总视在功率(kVA)">
                <el-table-column prop="总平均视在功率" label="平均值" />
                <el-table-column prop="总最大视在功率" label="最大值"/>   
                <el-table-column prop="totalApparentPowMaxTimeData" label="发生时间"/>
                <el-table-column prop="总最小视在功率" label="最小值"/>   
                <el-table-column prop="totalApparentPowMinTimeData" label="发生时间"/>
              </el-table-column>
              <el-table-column v-if="activeName!='realtimeTabPane'&&paramType=='total'" label="总无功功率(kVar)">
                <el-table-column prop="总平均无功功率" label="平均值" />
                <el-table-column prop="总最大无功功率" label="最大值"/>   
                <el-table-column prop="totalReactivePowMaxTimeData" label="发生时间"/>
                <el-table-column prop="总最小无功功率" label="最小值"/>   
                <el-table-column prop="totalReactivePowMinTimeData" label="发生时间"/>
              </el-table-column>
              <el-table-column v-if="activeName!='realtimeTabPane'&&paramType=='total'" label="总平均功率因素" prop="总平均功率因素"/>
              <el-table-column v-if="activeName!='realtimeTabPane'&&paramType=='a'" label="A路功功率(kW)">
                <el-table-column prop="A路平均有功功率" label="平均值" />
                <el-table-column prop="A路最大有功功率" label="最大值"/>   
                <el-table-column prop="aActivePowMaxTimeData" label="发生时间"/>
                <el-table-column prop="A路最小有功功率" label="最小值"/>   
                <el-table-column prop="aActivePowMinTimeData" label="发生时间"/>
              </el-table-column>
              <el-table-column v-if="activeName!='realtimeTabPane'&&paramType=='a'" label="A路视在功率(kVA)">
                <el-table-column prop="A路平均视在功率" label="平均值" />
                <el-table-column prop="A路最大视在功率" label="最大值"/>   
                <el-table-column prop="aApparentPowMaxTimeData" label="发生时间"/>
                <el-table-column prop="A路最小视在功率" label="最小值"/>   
                <el-table-column prop="aApparentPowMinTimeData" label="发生时间"/>
              </el-table-column>
              <el-table-column v-if="activeName!='realtimeTabPane'&&paramType=='a'" prop="A路平均无功功率" label="A路平均无功功率(kVar)"/>
              <el-table-column v-if="activeName!='realtimeTabPane'&&paramType=='a'" prop="A路平均功率因素" label="A路平均功率因素"/>
              <el-table-column v-if="activeName!='realtimeTabPane'&&paramType=='b'" label="B路功功率(kW)">
                <el-table-column prop="B路平均有功功率" label="平均值" />
                <el-table-column prop="B路最大有功功率" label="最大值"/>   
                <el-table-column prop="bActivePowMaxTimeData" label="发生时间"/>
                <el-table-column prop="B路最小有功功率" label="最小值"/>   
                <el-table-column prop="bActivePowMinTimeData" label="发生时间"/>
              </el-table-column>
              <el-table-column v-if="activeName!='realtimeTabPane'&&paramType=='b'" label="B路视在功率(kVA)">
                <el-table-column prop="B路平均视在功率" label="平均值" />
                <el-table-column prop="B路最大视在功率" label="最大值"/>   
                <el-table-column prop="bApparentPowMaxTimeData" label="发生时间"/>
                <el-table-column prop="B路最小视在功率" label="最小值"/>   
                <el-table-column prop="bApparentPowMinTimeData" label="发生时间"/>
              </el-table-column>
              <el-table-column v-if="activeName!='realtimeTabPane'&&paramType=='b'" prop="B路平均无功功率" label="B路平均无功功率(kVar)"/>
              <el-table-column v-if="activeName!='realtimeTabPane'&&paramType=='b'" prop="B路平均功率因素" label="B路平均功率因素"/>
              <el-table-column v-if="activeName=='realtimeTabPane'&&paramType=='total'" label="总有功功率(kW)" prop="总有功功率"/>
              <el-table-column v-if="activeName=='realtimeTabPane'&&paramType=='total'" label="总视在功率(kVA)" prop="总视在功率"/>
              <el-table-column v-if="activeName=='realtimeTabPane'&&paramType=='total'" label="总无功功率(kVar)" prop="总无功功率"/>
              <el-table-column v-if="activeName=='realtimeTabPane'&&paramType=='total'" label="总功率因素" prop="总功率因素"/>
              <el-table-column v-if="activeName=='realtimeTabPane'&&paramType=='a'" label="A路有功功率(kW)" prop="A路有功功率"/>
              <el-table-column v-if="activeName=='realtimeTabPane'&&paramType=='a'" label="A路视在功率(kVA)" prop="A路视在功率"/>
              <el-table-column v-if="activeName=='realtimeTabPane'&&paramType=='a'" label="A路无功功率(kVar)" prop="A路无功功率"/>
              <el-table-column v-if="activeName=='realtimeTabPane'&&paramType=='a'" label="A路功率因素" prop="A路功率因素"/>
              <el-table-column v-if="activeName=='realtimeTabPane'&&paramType=='b'" label="B路有功功率(kW)" prop="B路有功功率"/>
              <el-table-column v-if="activeName=='realtimeTabPane'&&paramType=='b'" label="B路视在功率(kVA)" prop="B路视在功率"/>
              <el-table-column v-if="activeName=='realtimeTabPane'&&paramType=='b'" label="B路无功功率(kVar)" prop="B路无功功率"/>
              <el-table-column v-if="activeName=='realtimeTabPane'&&paramType=='b'" label="B路功率因素" prop="B路功率因素"/>

              <!-- <el-table-column v-else :prop="item.name" :label="item.name"/>    -->
            </el-table>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </template>
  </CommonMenu1>
</template>

<script setup lang="ts">
import draggable from 'vuedraggable';
import { dayjs, ElMessage } from 'element-plus'
import * as echarts from 'echarts';
import { onMounted } from 'vue'
import { HistoryDataApi } from '@/api/room/historydata'
import { formatDate } from '@/utils/formatTime'
import { IndexApi } from '@/api/room/roomindex'
import download from '@/utils/download';
import {ArrowLeft,ArrowRight} from '@element-plus/icons-vue'
import { cu } from 'dist-prod/assets/installCanvasRenderer-WHaFMoQ9';
import { table } from 'console';
// import PDUImage from '@/assets/imgs/PDU.jpg'
/** 机房历史曲线 */
defineOptions({ name: 'RoomHistoryLine' })
const navList = ref([]) as any // 左侧导航栏树结构列表
const nowAddress = ref('')// 导航栏的位置信息
const nowAddressTemp = ref('')// 暂时存储点击导航栏的位置信息 确认有数据再显示
const activeName = ref('dayExtremumTabPane') // tab默认显示
const activeName1 = ref('myChart')
const instance = getCurrentInstance()
const tableData = ref<Array<{ }>>([]) // 列表数据
const headerData = ref<any[]>([])
const needFlush = ref(0) // 是否需要刷新图表
const paramType = ref('total')
const message = useMessage() // 消息弹窗
const exportLoading = ref(false)
const queryParams = reactive({
  roomId: undefined as number | undefined,
  granularity: 'day',
  nowAddress: undefined as string | undefined,
  timeRange: defaultHourTimeRange(24*31) as any,
})
const route= useRoute();
if(history.state.start!=null&&history.state.end!=null&&history.state.start!=''&&history.state.end!=''){
  queryParams.timeRange = [history.state.start, history.state.end]
}
const next=ref("下一月");
const pre=ref("上一月");
let lastDate=null;
let lastHour=null;
let lastRaw=null;
const loading = ref(false) // 列表的加载中
// const carouselItems = ref([
//       { imgUrl: PDUImage},
//       { imgUrl: PDUImage},
//       { imgUrl: PDUImage},
//       { imgUrl: PDUImage},
//     ]);//侧边栏轮播图图片路径
// 时间段快捷选项
const shortcuts = [
  {
    text: '最近一小时',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 1)
      return [start, end]
    },
  },
  {
    text: '最近十二小时',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 12)
      return [start, end]
    },
  },
  {
    text: '最近一天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 24)
      return [start, end]
    },
  },
  {
    text: '最近两天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 48)
      return [start, end]
    },
  },
]
const shortcuts1 = [
  {
    text: '最近一天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 24)
      return [start, end]
    },
  },
  {
    text: '最近三天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 72)
      return [start, end]
    },
  },
  {
    text: '最近七天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 168)
      return [start, end]
    },
  },
]
const shortcuts2 = [
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setUTCMonth(start.getUTCMonth() - 1)
      return [start, end]
    },
  },
  {
    text: '最近三个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setUTCMonth(start.getUTCMonth() - 3)
      return [start, end]
    },
  },
  {
    text: '最近六个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setUTCMonth(start.getUTCMonth() - 6)
      return [start, end]
    },
  },
]

// 处理折线图数据
const createTimeData = ref<string[]>([]);
const totalActivePowData = ref<number[]>([]);
const aActivePowData = ref<number[]>([]);
const bActivePowData = ref<number[]>([]);
const totalApparentPowData = ref<number[]>([]);
const aApparentPowData = ref<number[]>([]);
const bApparentPowData = ref<number[]>([]);

const totalReactivePowData = ref<number[]>([]);
const aReactivePowData = ref<number[]>([]);
const bReactivePowData = ref<number[]>([]);
const factorTotalData = ref<number[]>([]);
const factorAData = ref<number[]>([]);
const factorBData = ref<number[]>([]);

const totalActivePowAvgValueData = ref<number[]>([]);
const totalActivePowMaxValueData = ref<number[]>([]);
const totalActivePowMaxTimeData = ref<string[]>([]);
const totalActivePowMinValueData = ref<number[]>([]);
const totalActivePowMinTimeData = ref<string[]>([]);

const aActivePowAvgValueData = ref<number[]>([]);
const aActivePowMaxValueData = ref<number[]>([]);
const aActivePowMaxTimeData = ref<string[]>([]);
const aActivePowMinValueData = ref<number[]>([]);
const aActivePowMinTimeData = ref<string[]>([]);

const bActivePowAvgValueData = ref<number[]>([]);
const bActivePowMaxValueData = ref<number[]>([]);
const bActivePowMaxTimeData = ref<string[]>([]);
const bActivePowMinValueData = ref<number[]>([]);
const bActivePowMinTimeData = ref<string[]>([]);

const totalApparentPowAvgValueData = ref<number[]>([]);
const totalApparentPowMaxValueData = ref<number[]>([]);
const totalApparentPowMaxTimeData = ref<string[]>([]);
const totalApparentPowMinValueData = ref<number[]>([]);
const totalApparentPowMinTimeData = ref<string[]>([]);

const aApparentPowAvgValueData = ref<number[]>([]);
const aApparentPowMaxValueData = ref<number[]>([]);
const aApparentPowMaxTimeData = ref<string[]>([]);
const aApparentPowMinValueData = ref<number[]>([]);
const aApparentPowMinTimeData = ref<string[]>([]);

const bApparentPowAvgValueData = ref<number[]>([]);
const bApparentPowMaxValueData = ref<number[]>([]);
const bApparentPowMaxTimeData = ref<string[]>([]);
const bApparentPowMinValueData = ref<number[]>([]);
const bApparentPowMinTimeData = ref<string[]>([]);

const totalReactivePowAvgValueData = ref<number[]>([]);
const totalReactivePowMaxValueData = ref<number[]>([]);
const totalReactivePowMaxTimeData = ref<string[]>([]);
const totalReactivePowMinValueData = ref<number[]>([]);
const totalReactivePowMinTimeData = ref<string[]>([]);

const aReactivePowAvgValueData = ref<number[]>([]);
const bReactivePowAvgValueData = ref<number[]>([]);

const factorTotalAvgValueData = ref<number[]>([]);
const factorAAvgValueData = ref<number[]>([]);
const factorBAvgValueData = ref<number[]>([]);

const maxActivePowDataTemp = ref();// 最大有功功率 
const maxActivePowDataTimeTemp = ref();// 最大有功功率的发生时间 
const minActivePowDataTemp = ref();// 最小有功功率 
const minActivePowDataTimeTemp = ref();// 最小有功功率的发生时间 

/** 查询列表 */
const isHaveData = ref(false);
const getList = async () => {
if (queryParams.roomId == null){
  ElMessage.warning('请选择机房')
  return
}
loading.value = true
 try {
    const data = await HistoryDataApi.getHistoryDataDetails(queryParams);
    if (data != null && data.total != 0){
      isHaveData.value = true
      // 总、A、B路实时数据 有功视在
      totalActivePowData.value = data.list.map((item) => formatNumber(item.active_total, 3));
      aActivePowData.value = data.list.map((item) => formatNumber(item.active_a, 3));
      bActivePowData.value = data.list.map((item) => formatNumber(item.active_b, 3));
      totalApparentPowData.value = data.list.map((item) => formatNumber(item.apparent_total, 3)); 
      aApparentPowData.value = data.list.map((item) => formatNumber(item.apparent_a, 3));
      bApparentPowData.value = data.list.map((item) => formatNumber(item.apparent_b, 3));
      // 总、A、B路实时数据 无功 功率因素
      totalReactivePowData.value = data.list.map((item) => formatNumber(item.reactive_total, 3));
      aReactivePowData.value = data.list.map((item) => formatNumber(item.reactive_a, 3));
      bReactivePowData.value = data.list.map((item) => formatNumber(item.reactive_b, 3));
      factorTotalData.value = data.list.map((item) => formatNumber(item.factor_total, 2));
      factorAData.value = data.list.map((item) => formatNumber(item.factor_a, 2));
      factorBData.value = data.list.map((item) => formatNumber(item.factor_b, 2));

      if (activeName.value === 'dayExtremumTabPane'){
        createTimeData.value = data.list.map((item) => formatDate(item.create_time, 'YYYY-MM-DD'));
      }else{
        createTimeData.value = data.list.map((item) => formatDate(item.create_time));
      }
      totalActivePowAvgValueData.value = data.list.map((item) => formatNumber(item.active_total_avg_value, 3));
      totalActivePowMaxValueData.value = data.list.map((item) => formatNumber(item.active_total_max_value, 3));
      totalActivePowMaxTimeData.value = data.list.map((item) => formatDate(item.active_total_max_time));
      totalActivePowMinValueData.value = data.list.map((item) => formatNumber(item.active_total_min_value, 3));
      totalActivePowMinTimeData.value = data.list.map((item) => formatDate(item.active_total_min_time));

      aActivePowAvgValueData.value = data.list.map((item) => formatNumber(item.active_a_avg_value, 3));
      aActivePowMaxValueData.value = data.list.map((item) => formatNumber(item.active_a_max_value, 3));
      aActivePowMaxTimeData.value = data.list.map((item) => formatDate(item.active_a_max_time));
      aActivePowMinValueData.value = data.list.map((item) => formatNumber(item.active_a_min_value, 3));
      aActivePowMinTimeData.value = data.list.map((item) => formatDate(item.active_a_min_time));
      
      bActivePowAvgValueData.value = data.list.map((item) => formatNumber(item.active_b_avg_value, 3));
      bActivePowMaxValueData.value = data.list.map((item) => formatNumber(item.active_b_max_value, 3));
      bActivePowMaxTimeData.value = data.list.map((item) => formatDate(item.active_b_max_time));
      bActivePowMinValueData.value = data.list.map((item) => formatNumber(item.active_b_min_value, 3));
      bActivePowMinTimeData.value = data.list.map((item) => formatDate(item.active_b_min_time));

      totalReactivePowAvgValueData.value = data.list.map((item) => formatNumber(item.reactive_total_avg_value, 3));
      totalReactivePowMaxValueData.value = data.list.map((item) => formatNumber(item.reactive_total_max_value, 3));
      totalReactivePowMaxTimeData.value = data.list.map((item) => formatDate(item.reactive_total_max_time));
      totalReactivePowMinValueData.value = data.list.map((item) => formatNumber(item.reactive_total_min_value, 3));
      totalReactivePowMinTimeData.value = data.list.map((item) => formatDate(item.reactive_total_min_time));

      aReactivePowAvgValueData.value = data.list.map((item) => formatNumber(item.reactive_a_avg_value, 3));
      bReactivePowAvgValueData.value = data.list.map((item) => formatNumber(item.reactive_b_avg_value, 3));

      totalApparentPowAvgValueData.value = data.list.map((item) => formatNumber(item.apparent_total_avg_value, 3));
      totalApparentPowMaxValueData.value = data.list.map((item) => formatNumber(item.apparent_total_max_value, 3));
      totalApparentPowMaxTimeData.value = data.list.map((item) => formatDate(item.apparent_total_max_time));
      totalApparentPowMinValueData.value = data.list.map((item) => formatNumber(item.apparent_total_min_value, 3));
      totalApparentPowMinTimeData.value = data.list.map((item) => formatDate(item.apparent_total_min_time));

      aApparentPowAvgValueData.value = data.list.map((item) => formatNumber(item.apparent_a_avg_value, 3));
      aApparentPowMaxValueData.value = data.list.map((item) => formatNumber(item.apparent_a_max_value, 3));
      aApparentPowMaxTimeData.value = data.list.map((item) => formatDate(item.apparent_a_max_time));
      aApparentPowMinValueData.value = data.list.map((item) => formatNumber(item.apparent_a_min_value, 3));
      aApparentPowMinTimeData.value = data.list.map((item) => formatDate(item.apparent_a_min_time));

      bApparentPowAvgValueData.value = data.list.map((item) => formatNumber(item.apparent_b_avg_value, 3));
      bApparentPowMaxValueData.value = data.list.map((item) => formatNumber(item.apparent_b_max_value, 3));
      bApparentPowMaxTimeData.value = data.list.map((item) => formatDate(item.apparent_b_max_time));
      bApparentPowMinValueData.value = data.list.map((item) => formatNumber(item.apparent_b_min_value, 3));
      bApparentPowMinTimeData.value = data.list.map((item) => formatDate(item.apparent_b_min_time));

      factorTotalAvgValueData.value = data.list.map((item) => formatNumber(item.factor_total_avg_value, 2));
      factorAAvgValueData.value = data.list.map((item) => formatNumber(item.factor_a_avg_value, 2));
      factorBAvgValueData.value = data.list.map((item) => formatNumber(item.factor_b_avg_value, 2));

      // 侧边栏数据计算
      if(activeName.value === 'realtimeTabPane'){
        if(paramType.value=='total'){
          maxActivePowDataTemp.value = Math.max(...totalActivePowData.value);
          minActivePowDataTemp.value = Math.min(...totalActivePowData.value);
          totalActivePowData.value.forEach(function(num, index) {
            if (num == maxActivePowDataTemp.value){
              maxActivePowDataTimeTemp.value = createTimeData.value[index]
            }
            if (num == minActivePowDataTemp.value){
              minActivePowDataTimeTemp.value = createTimeData.value[index]
            }
          });
        }else if(paramType.value=='a'){
          maxActivePowDataTemp.value = Math.max(...aActivePowData.value);
          minActivePowDataTemp.value = Math.min(...aActivePowData.value);
          aActivePowData.value.forEach(function(num, index) {
            if (num == maxActivePowDataTemp.value){
              maxActivePowDataTimeTemp.value = createTimeData.value[index]
            }
            if (num == minActivePowDataTemp.value){
              minActivePowDataTimeTemp.value = createTimeData.value[index]
            }
          });
        }else if(paramType.value=='b'){
          maxActivePowDataTemp.value = Math.max(...bActivePowData.value);
          minActivePowDataTemp.value = Math.min(...bActivePowData.value);
          bActivePowData.value.forEach(function(num, index) {
            if (num == maxActivePowDataTemp.value){
              maxActivePowDataTimeTemp.value = createTimeData.value[index]
            }
            if (num == minActivePowDataTemp.value){
              minActivePowDataTimeTemp.value = createTimeData.value[index]
            }
          });
        }
      }else{
        if(paramType.value=='total'){
          maxActivePowDataTemp.value = Math.max(...totalActivePowMaxValueData.value);
          minActivePowDataTemp.value = Math.min(...totalActivePowMaxValueData.value);
          totalActivePowMaxValueData.value.forEach(function(num, index) {
            if (num == maxActivePowDataTemp.value){
              maxActivePowDataTimeTemp.value = createTimeData.value[index]
            }
            if (num == minActivePowDataTemp.value){
              minActivePowDataTimeTemp.value = createTimeData.value[index]
            }
          });
        }else if(paramType.value=='a'){
          maxActivePowDataTemp.value = Math.max(...aActivePowMaxValueData.value);
          minActivePowDataTemp.value = Math.min(...aActivePowMaxValueData.value);
          aActivePowMaxValueData.value.forEach(function(num, index) {
            if (num == maxActivePowDataTemp.value){
              maxActivePowDataTimeTemp.value = createTimeData.value[index]
            }
            if (num == minActivePowDataTemp.value){
              minActivePowDataTimeTemp.value = createTimeData.value[index]
            }
          });
        }else if(paramType.value=='b'){
          maxActivePowDataTemp.value = Math.max(...bActivePowMaxValueData.value);
          minActivePowDataTemp.value = Math.min(...bActivePowMaxValueData.value);
          bActivePowMaxValueData.value.forEach(function(num, index) {
            if (num == maxActivePowDataTemp.value){
              maxActivePowDataTimeTemp.value = createTimeData.value[index]
            }
            if (num == minActivePowDataTemp.value){
              minActivePowDataTimeTemp.value = createTimeData.value[index]
            }
          });
        }
      }
    }else{
      // 总、A、B路实时数据 有功视在
      totalActivePowData.value = [];
      aActivePowData.value = [];
      bActivePowData.value = [];
      totalApparentPowData.value = []; 
      aApparentPowData.value = [];
      bApparentPowData.value = [];
      // 总、A、B路实时数据 无功 功率因素
      totalReactivePowData.value = [];
      aReactivePowData.value = [];
      bReactivePowData.value = [];
      factorTotalData.value = [];
      factorAData.value = [];
      factorBData.value = [];
      createTimeData.value = [];
      totalActivePowAvgValueData.value = [];
      totalActivePowMaxValueData.value = [];
      totalActivePowMaxTimeData.value = [];
      totalActivePowMinValueData.value = [];
      totalActivePowMinTimeData.value = [];

      aActivePowAvgValueData.value = [];
      aActivePowMaxValueData.value = [];
      aActivePowMaxTimeData.value = [];
      aActivePowMinValueData.value = [];
      aActivePowMinTimeData.value = [];
      
      bActivePowAvgValueData.value =[];
      bActivePowMaxValueData.value = [];
      bActivePowMaxTimeData.value = [];
      bActivePowMinValueData.value = [];
      bActivePowMinTimeData.value = [];

      totalReactivePowAvgValueData.value = [];
      totalReactivePowMaxValueData.value = [];
      totalReactivePowMaxTimeData.value = [];
      totalReactivePowMinValueData.value = [];
      totalReactivePowMinTimeData.value = [];

      aReactivePowAvgValueData.value = [];
      bReactivePowAvgValueData.value = [];

      totalApparentPowAvgValueData.value = [];
      totalApparentPowMaxValueData.value = [];
      totalApparentPowMaxTimeData.value = [];
      totalApparentPowMinValueData.value = [];
      totalApparentPowMinTimeData.value = [];

      aApparentPowAvgValueData.value = [];
      aApparentPowMaxValueData.value = [];
      aApparentPowMaxTimeData.value = [];
      aApparentPowMinValueData.value = [];
      aApparentPowMinTimeData.value = [];

      bApparentPowAvgValueData.value = [];
      bApparentPowMaxValueData.value = [];
      bApparentPowMaxTimeData.value = [];
      bApparentPowMinValueData.value = [];
      bApparentPowMinTimeData.value = [];

      factorTotalAvgValueData.value = [];
      factorAAvgValueData.value = []
      factorBAvgValueData.value = [];
      
      isHaveData.value = false;
      maxActivePowDataTimeTemp.value=null;
      minActivePowDataTimeTemp.value=null;
      minActivePowDataTemp.value=null;
      maxActivePowDataTemp.value=null;
      ElMessage({
        message: '暂无数据',
        type: 'warning',
      });
    }
    // 图表显示的位置变化
    nowAddress.value = nowAddressTemp.value
 } finally {
   loading.value = false
 }
}

//初始化折线图
const chartContainer = ref<HTMLElement | null>(null);
let realtimeChart = null as echarts.ECharts | null; 
const initChart = () => {
  if ( isHaveData.value == true ){
    if (chartContainer.value && instance) {
      realtimeChart?.off('legendselectchanged');
      realtimeChart?.dispose();
      realtimeChart = echarts.init(chartContainer.value);
      if (realtimeChart) {
        // realtimeChart.setOption({
        //   title: { text: ''},
        //   tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
        //   legend: { data: ['总有功功率','总视在功率','总无功功率','总功率因素'],
        //   selected:{"总有功功率":true,"总视在功率":true,"总无功功率":true,"总功率因素":false}
        //   },
        //   grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
        //   toolbox: {feature: { restore:{}, saveAsImage: {}}},
        //   xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
        //   yAxis: { type: 'value'},
        //   series: [
        //     {name: '总有功功率', type: 'line', symbol: 'none', data: totalActivePowData.value},
        //     {name: '总视在功率', type: 'line', symbol: 'none', data: totalApparentPowData.value},
        //     {name: '总无功功率', type: 'line', symbol: 'none', data: totalReactivePowData.value},
        //     {name: '总功率因素', type: 'line', symbol: 'none', data: factorTotalData.value},
        //   ],
        //   dataZoom:[{type: "inside"}],
        // });
        if (activeName.value == 'realtimeTabPane'){
          realtimeChart.setOption({
            title: { text: ''},
            tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
            legend: { selected:{"总有功功率":true,"总视在功率":true,"总无功功率":true,"总功率因素":false},data: ['总有功功率','总视在功率','总无功功率','总功率因素']},
            grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
            toolbox: {feature: { restore:{}, saveAsImage: {}}},
            xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
            yAxis: { type: 'value'},
            series: [
              {name: '总有功功率', type: 'line', symbol: 'none', data: totalActivePowData.value, itemStyle:{normal:{lineStyle:{color:'#E5B849'},color:'#E5B849'}}},
              {name: '总视在功率', type: 'line', symbol: 'none', data: totalApparentPowData.value,itemStyle:{normal:{lineStyle:{color:'#C8603A'},color:'#C8603A'}}},
              {name: '总无功功率', type: 'line', symbol: 'none', data: totalReactivePowData.value,itemStyle:{normal:{lineStyle:{color:'#AD3762'},color:'#AD3762'}}},
              {name: '总功率因素', type: 'line', symbol: 'none', data: factorTotalData.value,itemStyle:{normal:{lineStyle:{color:'#B47660'},color:'#B47660'}}},
            ],
            dataZoom:[{type: "inside"}],
          });
        }else{
          realtimeChart.setOption( {
            title: {text: ''},
            tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
            legend: { data: ['总平均有功功率', '总最大有功功率', '总最小有功功率','总平均视在功率', '总最大视在功率', '总最小视在功率'
                            , '总平均无功功率','总最大无功功率', '总最小无功功率', '总平均功率因素'],
                  selected:  { 总平均有功功率: false, 总最大有功功率: true, 总最小有功功率: false, 总平均视在功率: false, 总最大视在功率: true, 总最小视在功率: false
                            , 总平均无功功率: false, 总最大无功功率: true, 总最小无功功率: false, 总平均功率因素: false}},
            grid: {left: '3%', right: '4%',bottom: '3%', containLabel: true },
            toolbox: {feature: { restore:{}, saveAsImage: {}}},
            xAxis: {type: 'category', boundaryGap: false, data: createTimeData.value},
            yAxis: { type: 'value'},
            series: [
              { name: '总平均有功功率', type: 'line',symbol: 'none',data: totalActivePowAvgValueData.value, itemStyle:{normal:{lineStyle:{color:'#E5B849'},color:'#E5B849'}}},
              { name: '总最大有功功率', type: 'line',symbol: 'none',data: totalActivePowMaxValueData.value, itemStyle:{normal:{lineStyle:{color:'#C8603A'},color:'#C8603A'}} },
              { name: '总最小有功功率',type: 'line',symbol: 'none',data: totalActivePowMinValueData.value, itemStyle:{normal:{lineStyle:{color:'#AD3762'},color:'#AD3762'}}},
              { name: '总平均视在功率',type: 'line',symbol: 'none',data:  totalApparentPowAvgValueData.value, itemStyle:{normal:{lineStyle:{color:'#B47660'},color:'#B47660'}}},
              { name: '总最大视在功率', type: 'line',symbol: 'none', data: totalApparentPowMaxValueData.value, itemStyle:{normal:{lineStyle:{color:'#614E43'},color:'#614E43'}}},
              { name: '总最小视在功率',type: 'line',symbol: 'none',data:  totalApparentPowMinValueData.value, itemStyle:{normal:{lineStyle:{color:'#5337A9'},color:'#5337A9'}}},

              { name: '总平均无功功率',type: 'line',symbol: 'none',data:  totalReactivePowAvgValueData.value,itemStyle:{normal:{lineStyle:{color:'#5D82DB'},color:'#5D82DB'}}},
              { name: '总最大无功功率', type: 'line',symbol: 'none', data: totalReactivePowMaxValueData.value, itemStyle:{normal:{lineStyle:{color:'#6899DC'},color:'#6899DC'}}},
              { name: '总最小无功功率',type: 'line',symbol: 'none',data:  totalReactivePowMinValueData.value, itemStyle:{normal:{lineStyle:{color:'#94B159'},color:'#94B159'}}},
              { name: '总平均功率因素',type: 'line',symbol: 'none',data:  factorTotalAvgValueData.value},
            ],
            dataZoom:[{type: "inside"}],
          });
        }
      }
      // 监听图例点击事件
      realtimeChart.on('legendselectchanged', function (params) {
        console.log(params.name)
        const selectedSeries = params.selected;
        const option = realtimeChart.getOption();
        console.log(option)
        if(params.name.includes ('功率因素')&& selectedSeries[params.name] == true){
          // console.log("总功率因素")
          option.legend[0].data.forEach(function(item, index) {
            // console.log(item)
            if (!item.includes ('功率因素')){
              option.legend[0].selected[item] = false;
            }
          });
        }
        if(!params.name.includes('功率因素')&& selectedSeries[params.name] == true){
          // option.legend[0].selected['总功率因素'] = false;
          option.legend[0].data.forEach(function(item, index) {
            // console.log(item)
            if (item.includes ('功率因素')){
              option.legend[0].selected[item] = false;
            }
          });
        }
        realtimeChart.setOption(option);
      });
      // 将 realtimeChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
      instance.appContext.config.globalProperties.realtimeChart = realtimeChart;
    }
  }
  // 每次切换图就要动态生成数据表头
  headerData.value = realtimeChart?.getOption().series as any[];
  updateTableData();
  // let t=activeName.value
  // activeName.value = 'realtimeTabPane'
  // activeName.value = t     
};

// 在组件销毁时手动销毁图表
const beforeUnmount = () => {
  realtimeChart?.off("legendselectchanged")
  realtimeChart?.dispose(); // 销毁图表实例
  window.removeEventListener('resize', resize);
};
function resize() {
    realtimeChart?.resize(); 
}
window.addEventListener('resize', resize);

function preMonth(date){
 const pre = new Date(date.replace(" ", "T"))
 if(pre.getMonth() == 0){
  pre.setMonth(11)
  pre.setFullYear(pre.getFullYear() - 1)
 }else{
  pre.setMonth(pre.getMonth() - 1)
 }
 return dayjs(pre).format('YYYY-MM-DD HH:mm:ss')
}

function nextMonth(date){
 const pre = new Date(date.replace(" ", "T"))
 if(pre.getMonth() == 11){
  pre.setMonth(0)
  pre.setFullYear(pre.getFullYear() +1)
 }else{
  pre.setMonth(pre.getMonth() +1)
 }
 return dayjs(pre).format('YYYY-MM-DD HH:mm:ss')
}

function preTime(date,time){
  return dayjs(new Date(new Date(date.replace(" ", "T")).getTime()-time)).format('YYYY-MM-DD HH:mm:ss')
}
function calculateTime(date1,date2){
  try{
    const dateLeft=date1.replace(" ", "T")
    const dateRight=date2.replace(" ", "T")
    return new Date(dateLeft).getTime() - new Date(dateRight).getTime()
  }catch(e){
    return 1000*60*60*24*32;
  }
}
function changeTime(to){
  if(to=="next"){
    if ( activeName.value == 'realtimeTabPane'){
      queryParams.timeRange=[preTime(queryParams.timeRange[0],-1000*60*60*24),preTime(queryParams.timeRange[1],-1000*60*60*24)]
    }else if (activeName.value == 'hourExtremumTabPane'){
      queryParams.timeRange=[preTime(queryParams.timeRange[0],-1000*60*60*24*7),preTime(queryParams.timeRange[1],-1000*60*60*24*7)]
    }else{
      queryParams.timeRange=[nextMonth(queryParams.timeRange[0]),nextMonth(queryParams.timeRange[1])]
    }
  }else if(to="pre"){
    if ( activeName.value == 'realtimeTabPane'){
      queryParams.timeRange=[preTime(queryParams.timeRange[0],1000*60*60*24),preTime(queryParams.timeRange[1],1000*60*60*24)]
    }else if (activeName.value == 'hourExtremumTabPane'){
      queryParams.timeRange=[preTime(queryParams.timeRange[0],1000*60*60*24*7),preTime(queryParams.timeRange[1],1000*60*60*24*7)]
    }else{
      queryParams.timeRange=[preMonth(queryParams.timeRange[0]),preMonth(queryParams.timeRange[1])]
    }
  }
  handleQuery();
}
// 监听切换原始数据、极值数据tab
watch( ()=>activeName.value, async(newActiveName,oldActiveName)=>{
  if(oldActiveName=="realtimeTabPane"){
    lastRaw=queryParams.timeRange;
  }else if(oldActiveName=="hourExtremumTabPane"){
    lastHour=queryParams.timeRange;
  }else{
    lastDate=queryParams.timeRange;
  }
  console.log("middle")
  if ( newActiveName == 'realtimeTabPane'){
    next.value="下一天";
    pre.value="上一天";
    queryParams.granularity = 'realtime'
    console.log("time=",calculateTime(queryParams.timeRange[1],queryParams.timeRange[0]));
    if(lastRaw!=null){
      queryParams.timeRange=lastRaw;
    }else{
      if(calculateTime(queryParams.timeRange[1],queryParams.timeRange[0])>1000*60*60*24){
        queryParams.timeRange=[preTime(queryParams.timeRange[1],1000*60*60*24),queryParams.timeRange[1]]
      }
    }
  }else if (newActiveName == 'hourExtremumTabPane'){
    next.value="下一周";
    pre.value="上一周";
    queryParams.granularity = 'hour';
    if(lastHour!=null){
      queryParams.timeRange=lastHour;
    }else{
      if(calculateTime(queryParams.timeRange[1],queryParams.timeRange[0])>1000*60*60*24*7){
        queryParams.timeRange = [preTime(queryParams.timeRange[1],1000*60*60*24*7),queryParams.timeRange[1]];
      }
    }
  }else{
    next.value="下一月";
    pre.value="上一月";
    queryParams.granularity = 'day';
    if(lastDate!=null){
      queryParams.timeRange=lastDate;
    }else{
      if(calculateTime(queryParams.timeRange[1],queryParams.timeRange[0])>calculateTime(queryParams.timeRange[1],preMonth(queryParams.timeRange[1]))){
        queryParams.timeRange = [preMonth(queryParams.timeRange[1]),queryParams.timeRange[1]]
      }
    }
    
    // queryParams.timeRange = defaultMonthTimeRange(1)
  }
  needFlush.value ++;
});

const tableLoading=ref(false);
// 监听参数类型
watch(() => paramType.value , (newValues) => {
  tableLoading.value = true;
  try{
    const newParamType = newValues;
  if(activeName.value == 'realtimeTabPane'){
    if(isHaveData.value){
      if(newParamType=='total'){
          maxActivePowDataTemp.value = Math.max(...totalActivePowData.value);
          minActivePowDataTemp.value = Math.min(...totalActivePowData.value);
          totalActivePowData.value.forEach(function(num, index) {
            if (num == maxActivePowDataTemp.value){
              maxActivePowDataTimeTemp.value = createTimeData.value[index]
            }
            if (num == minActivePowDataTemp.value){
              minActivePowDataTimeTemp.value = createTimeData.value[index]
            }
          });
        }else if(newParamType=='a'){
          maxActivePowDataTemp.value = Math.max(...aActivePowData.value);
          minActivePowDataTemp.value = Math.min(...aActivePowData.value);
          aActivePowData.value.forEach(function(num, index) {
            if (num == maxActivePowDataTemp.value){
              maxActivePowDataTimeTemp.value = createTimeData.value[index]
            }
            if (num == minActivePowDataTemp.value){
              minActivePowDataTimeTemp.value = createTimeData.value[index]
            }
          });
        }else if(newParamType=='b'){
          maxActivePowDataTemp.value = Math.max(...bActivePowData.value);
          minActivePowDataTemp.value = Math.min(...bActivePowData.value);
          bActivePowData.value.forEach(function(num, index) {
            if (num == maxActivePowDataTemp.value){
              maxActivePowDataTimeTemp.value = createTimeData.value[index]
            }
            if (num == minActivePowDataTemp.value){
              minActivePowDataTimeTemp.value = createTimeData.value[index]
            }
          });
        }
    }else{
      maxActivePowDataTimeTemp.value=null;
      minActivePowDataTimeTemp.value=null;
      minActivePowDataTemp.value=null;
      maxActivePowDataTemp.value=null;
    }
    if ( newParamType == 'total'){
      realtimeChart?.setOption({
        legend: { data: ['总有功功率', '总视在功率','总无功功率','总功率因素'] 
          ,selected:{"总有功功率":true,"总视在功率":true,"总无功功率":true,"总功率因素":false}
        },
        series: [
          {name: '总有功功率', type: 'line', symbol: 'none', data: totalActivePowData.value,itemStyle:{normal:{lineStyle:{color:'#E5B849'},color:'#E5B849'}}},
          {name: '总视在功率', type: 'line', symbol: 'none', data: totalApparentPowData.value,itemStyle:{normal:{lineStyle:{color:'#C8603A'},color:'#C8603A'}}},
          {name: '总无功功率', type: 'line', symbol: 'none', data: totalReactivePowData.value,itemStyle:{normal:{lineStyle:{color:'#AD3762'},color:'#AD3762'}}},
          {name: '总功率因素', type: 'line', symbol: 'none', data: factorTotalData.value,itemStyle:{normal:{lineStyle:{color:'#B47660'},color:'#B47660'}}},
        ],
      })
    }else if( newParamType == 'a' ){
      realtimeChart?.setOption({
        legend: { data: ['A路有功功率', 'A路视在功率', 'A路无功功率', 'A路功率因素'],
          selected:{"A路有功功率":true,"A路视在功率":true,"A路无功功率":true,"A路功率因素":false}
         },
        series: [
          {name: 'A路有功功率', type: 'line', symbol: 'none', data: aActivePowData.value,itemStyle:{normal:{lineStyle:{color:'#E5B849'},color:'#E5B849'}}},
          {name: 'A路视在功率', type: 'line', symbol: 'none', data: aApparentPowData.value,itemStyle:{normal:{lineStyle:{color:'#C8603A'},color:'#C8603A'}}},
          {name: 'A路无功功率', type: 'line', symbol: 'none', data: aReactivePowData.value,itemStyle:{normal:{lineStyle:{color:'#AD3762'},color:'#AD3762'}}},
          {name: 'A路功率因素', type: 'line', symbol: 'none', data: factorAData.value,itemStyle:{normal:{lineStyle:{color:'#B47660'},color:'#B47660'}}},
        ],
      })
    }else{
      realtimeChart?.setOption({
        legend: { data: ['B路有功功率', 'B路视在功率', 'B路无功功率', 'B路功率因素'],
          selected:{"B路有功功率":true,"B路视在功率":true,"B路无功功率":true,"B路功率因素":false}
         },
        series: [
          {name: 'B路有功功率', type: 'line', symbol: 'none', data: bActivePowData.value,itemStyle:{normal:{lineStyle:{color:'#E5B849'},color:'#E5B849'}}},
          {name: 'B路视在功率', type: 'line', symbol: 'none', data: bApparentPowData.value,itemStyle:{normal:{lineStyle:{color:'#C8603A'},color:'#C8603A'}}},
          {name: 'B路无功功率', type: 'line', symbol: 'none', data: bReactivePowData.value,itemStyle:{normal:{lineStyle:{color:'#AD3762'},color:'#AD3762'}}},
          {name: 'B路功率因素', type: 'line', symbol: 'none', data: factorBData.value,itemStyle:{normal:{lineStyle:{color:'#B47660'},color:'#B47660'}}},
        ],
      })
    }
  }else{
    if(isHaveData.value){
      if(newParamType=='total'){
          maxActivePowDataTemp.value = Math.max(...totalActivePowMaxValueData.value);
          minActivePowDataTemp.value = Math.min(...totalActivePowMaxValueData.value);
          totalActivePowMaxValueData.value.forEach(function(num, index) {
            if (num == maxActivePowDataTemp.value){
              maxActivePowDataTimeTemp.value = createTimeData.value[index]
            }
            if (num == minActivePowDataTemp.value){
              minActivePowDataTimeTemp.value = createTimeData.value[index]
            }
          });
        }else if(newParamType=='a'){
          maxActivePowDataTemp.value = Math.max(...aActivePowMaxValueData.value);
          minActivePowDataTemp.value = Math.min(...aActivePowMaxValueData.value);
          aActivePowMaxValueData.value.forEach(function(num, index) {
            if (num == maxActivePowDataTemp.value){
              maxActivePowDataTimeTemp.value = createTimeData.value[index]
            }
            if (num == minActivePowDataTemp.value){
              minActivePowDataTimeTemp.value = createTimeData.value[index]
            }
          });
        }else if(newParamType=='b'){
          maxActivePowDataTemp.value = Math.max(...bActivePowMaxValueData.value);
          minActivePowDataTemp.value = Math.min(...bActivePowMaxValueData.value);
          bActivePowMaxValueData.value.forEach(function(num, index) {
            if (num == maxActivePowDataTemp.value){
              maxActivePowDataTimeTemp.value = createTimeData.value[index]
            }
            if (num == minActivePowDataTemp.value){
              minActivePowDataTimeTemp.value = createTimeData.value[index]
            }
          });
        }
    }else{
      maxActivePowDataTimeTemp.value=null;
      minActivePowDataTimeTemp.value=null;
      minActivePowDataTemp.value=null;
      maxActivePowDataTemp.value=null;
    }
    realtimeChart?.off("legendselectchanged")
    realtimeChart?.dispose();
    realtimeChart = echarts.init(document.getElementById('chartContainer'));
    if ( newParamType == 'total'){
      realtimeChart.setOption( {
          title: {text: ''},
          tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
          legend: { data: ['总平均有功功率', '总最大有功功率', '总最小有功功率','总平均视在功率', '总最大视在功率', '总最小视在功率'
                          , '总平均无功功率','总最大无功功率', '总最小无功功率', '总平均功率因素'],
                selected:  { 总平均有功功率: false, 总最大有功功率: true, 总最小有功功率: false, 总平均视在功率: false, 总最大视在功率: true, 总最小视在功率: false
                          , 总平均无功功率: false, 总最大无功功率: true, 总最小无功功率: false, 总平均功率因素: false}},
          grid: {left: '3%', right: '4%',bottom: '3%', containLabel: true },
          toolbox: {feature: { restore:{}, saveAsImage: {}}},
          xAxis: {type: 'category', boundaryGap: false, data: createTimeData.value},
          yAxis: { type: 'value'},
          series: [
            { name: '总平均有功功率',symbol: 'none', type: 'line',data: totalActivePowAvgValueData.value,itemStyle:{normal:{lineStyle:{color:'#E5B849'},color:'#E5B849'}}},
            { name: '总最大有功功率',symbol: 'none', type: 'line',data: totalActivePowMaxValueData.value, itemStyle:{normal:{lineStyle:{color:'#C8603A'},color:'#C8603A'}} },
            { name: '总最小有功功率',symbol: 'none',type: 'line',data: totalActivePowMinValueData.value, itemStyle:{normal:{lineStyle:{color:'#AD3762'},color:'#AD3762'}}},
            { name: '总平均视在功率',symbol: 'none',type: 'line',data:  totalApparentPowAvgValueData.value,itemStyle:{normal:{lineStyle:{color:'#B47660'},color:'#B47660'}}},
            { name: '总最大视在功率',symbol: 'none', type: 'line', data: totalApparentPowMaxValueData.value, itemStyle:{normal:{lineStyle:{color:'#614E43'},color:'#614E43'}}},
            { name: '总最小视在功率',symbol: 'none',type: 'line',data:  totalApparentPowMinValueData.value, itemStyle:{normal:{lineStyle:{color:'#5337A9'},color:'#5337A9'}}},

            { name: '总平均无功功率',symbol: 'none',type: 'line',data:  totalReactivePowAvgValueData.value,itemStyle:{normal:{lineStyle:{color:'#5D82DB'},color:'#5D82DB'}}},
            { name: '总最大无功功率',symbol: 'none', type: 'line', data: totalReactivePowMaxValueData.value, itemStyle:{normal:{lineStyle:{color:'#6899DC'},color:'#6899DC'}}},
            { name: '总最小无功功率',symbol: 'none',type: 'line',data:  totalReactivePowMinValueData.value,itemStyle:{normal:{lineStyle:{color:'#94B159'},color:'#94B159'}}},
            { name: '总平均功率因素',symbol: 'none',type: 'line',data:  factorTotalAvgValueData.value},
          ],
          dataZoom:[{type: "inside"}],
        });
    }else if( newParamType == 'a' ){
      realtimeChart.setOption( {
          title: {text: ''},
          tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
          legend: { data: ['A路平均有功功率', 'A路最大有功功率', 'A路最小有功功率','A路平均视在功率', 'A路最大视在功率', 'A路最小视在功率'
                , 'A路平均无功功率', 'A路平均功率因素'],
              selected: { A路平均有功功率: false, A路最大有功功率: true, A路最小有功功率: false, A路平均视在功率: false, A路最大视在功率: true, A路最小视在功率: false
                    , A路平均无功功率: false, A路平均功率因素: false}},
          grid: {left: '3%', right: '4%',bottom: '3%', containLabel: true },
          toolbox: {feature: { restore:{}, saveAsImage: {}}},
          xAxis: {type: 'category', boundaryGap: false, data: createTimeData.value},
          yAxis: { type: 'value'},
          series: [
          { name: 'A路平均有功功率',symbol: 'none', type: 'line', data: aActivePowAvgValueData.value,itemStyle:{normal:{lineStyle:{color:'#E5B849'},color:'#E5B849'}}},
          { name: 'A路最大有功功率',symbol: 'none', type: 'line', data: aActivePowMaxValueData.value, itemStyle:{normal:{lineStyle:{color:'#C8603A'},color:'#C8603A'}} },
          { name: 'A路最小有功功率',symbol: 'none', type: 'line', data: aActivePowMinValueData.value, itemStyle:{normal:{lineStyle:{color:'#AD3762'},color:'#AD3762'}}},
          { name: 'A路平均视在功率',symbol: 'none', type: 'line', data: aApparentPowAvgValueData.value,itemStyle:{normal:{lineStyle:{color:'#B47660'},color:'#B47660'}}},
          { name: 'A路最大视在功率',symbol: 'none', type: 'line', data: aApparentPowMaxValueData.value, itemStyle:{normal:{lineStyle:{color:'#614E43'},color:'#614E43'}}},
          { name: 'A路最小视在功率',symbol: 'none', type: 'line', data: aApparentPowMinValueData.value, itemStyle:{normal:{lineStyle:{color:'#5337A9'},color:'#5337A9'}}},
          { name: 'A路平均无功功率',symbol: 'none',type: 'line',data:  aReactivePowAvgValueData.value,itemStyle:{normal:{lineStyle:{color:'#5D82DB'},color:'#5D82DB'}}},
          { name: 'A路平均功率因素',symbol: 'none',type: 'line',data:  factorAAvgValueData.value,itemStyle:{normal:{lineStyle:{color:'#6899DC'},color:'#6899DC'}}},
          ],
          dataZoom:[{type: "inside"}],
        });
    }else{
      realtimeChart.setOption( {
          title: {text: ''},
          tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
          legend: { data: ['B路平均有功功率', 'B路最大有功功率', 'B路最小有功功率','B路平均视在功率', 'B路最大视在功率', 'B路最小视在功率'
                    , 'B路平均无功功率', 'B路平均功率因素'],
              selected: { B路平均有功功率: false, B路最大有功功率: true, B路最小有功功率: false, B路平均视在功率: false, B路最大视在功率: true, B路最小视在功率: false
              , B路平均无功功率: false, B路平均功率因素: false}},
          grid: {left: '3%', right: '4%',bottom: '3%', containLabel: true },
          toolbox: {feature: { restore:{}, saveAsImage: {}}},
          xAxis: {type: 'category', boundaryGap: false, data: createTimeData.value},
          yAxis: { type: 'value'},
          series: [
          { name: 'B路平均有功功率',symbol: 'none', type: 'line', data: bActivePowAvgValueData.value,itemStyle:{normal:{lineStyle:{color:'#E5B849'},color:'#E5B849'}}},
          { name: 'B路最大有功功率',symbol: 'none', type: 'line', data: bActivePowMaxValueData.value, itemStyle:{normal:{lineStyle:{color:'#C8603A'},color:'#C8603A'}} },
          { name: 'B路最小有功功率',symbol: 'none', type: 'line', data: bActivePowMinValueData.value, itemStyle:{normal:{lineStyle:{color:'#AD3762'},color:'#AD3762'}}},
          { name: 'B路平均视在功率',symbol: 'none', type: 'line', data: bApparentPowAvgValueData.value,itemStyle:{normal:{lineStyle:{color:'#B47660'},color:'#B47660'}}},
          { name: 'B路最大视在功率',symbol: 'none', type: 'line', data: bApparentPowMaxValueData.value, itemStyle:{normal:{lineStyle:{color:'#614E43'},color:'#614E43'}}},
          { name: 'B路最小视在功率',symbol: 'none', type: 'line', data: bApparentPowMinValueData.value, itemStyle:{normal:{lineStyle:{color:'#5337A9'},color:'#5337A9'}}},
          { name: 'B路平均无功功率',symbol: 'none',type: 'line',data:  bReactivePowAvgValueData.value,itemStyle:{normal:{lineStyle:{color:'#5D82DB'},color:'#5D82DB'}}},
          { name: 'B路平均功率因素',symbol: 'none',type: 'line',data:  factorBAvgValueData.value,itemStyle:{normal:{lineStyle:{color:'#6899DC'},color:'#6899DC'}}},
          ],
          dataZoom:[{type: "inside"}],
        });
    }
  }
  // 监听图例点击事件
  realtimeChart?.on('legendselectchanged', function (params) {
        console.log(params.name)
        const selectedSeries = params.selected;
        const option = realtimeChart.getOption();
        console.log(option)
        if(params.name.includes ('功率因素')&& selectedSeries[params.name] == true){
          // console.log("总功率因素")
          option.legend[0].data.forEach(function(item, index) {
            // console.log(item)
            if (!item.includes ('功率因素')){
              option.legend[0].selected[item] = false;
            }
          });
        }
        if(!params.name.includes('功率因素')&& selectedSeries[params.name] == true){
          // option.legend[0].selected['总功率因素'] = false;
          option.legend[0].data.forEach(function(item, index) {
            // console.log(item)
            if (item.includes ('功率因素')){
              option.legend[0].selected[item] = false;
            }
          });
        }
        realtimeChart.setOption(option);
      });
  // 每次切换图就要动态生成数据表头
  headerData.value = realtimeChart?.getOption()?.series as any[];
  console.log("headerData",headerData.value)
  updateTableData();
  }finally {
    tableLoading.value = false;
  }
  
    
})
console.log("math([])=",Math.max(...[]));

// 监听颗粒度
watch(() => [activeName.value, needFlush.value], async (newValues) => {
  const [newActiveName] = newValues;
  if ( newActiveName == 'realtimeTabPane'){
    paramType.value = 'total'
    await getList();
    // 销毁原有的图表实例
    beforeUnmount()
    if ( isHaveData.value == true ){
      // 参数类型变回总
      // paramType.value = 'total'
      realtimeChart?.off("legendselectchanged")
      realtimeChart?.dispose();
      // 创建新的图表实例
      realtimeChart = echarts.init(document.getElementById('chartContainer'));
      // 设置新的配置对象
      if (realtimeChart) {
        realtimeChart.setOption({
        title: { text: ''},
        tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
        legend: { data: ['总有功功率','总视在功率','总无功功率','总功率因素'],
          selected:{"总有功功率":true,"总视在功率":true,"总无功功率":true,"总功率因素":false},
        },
        grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
        toolbox: {feature: { restore:{}, saveAsImage: {}}},
        xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
        yAxis: { type: 'value'},
        series: [
          {name: '总有功功率', type: 'line', symbol: 'none', data: totalActivePowData.value,itemStyle:{normal:{lineStyle:{color:'#E5B849'},color:'#E5B849'}}},
          {name: '总视在功率', type: 'line', symbol: 'none', data: totalApparentPowData.value,itemStyle:{normal:{lineStyle:{color:'#C8603A'},color:'#C8603A'}}},
          {name: '总无功功率', type: 'line', symbol: 'none', data: totalReactivePowData.value,itemStyle:{normal:{lineStyle:{color:'#AD3762'},color:'#AD3762'}}},
          {name: '总功率因素', type: 'line', symbol: 'none', data: factorTotalData.value,itemStyle:{normal:{lineStyle:{color:'#B47660'},color:'#B47660'}}},
        ],
        dataZoom:[{type: "inside"}],
        });
      }

      // 监听图例点击事件
      realtimeChart.on('legendselectchanged', function (params) {
        console.log(params.name)
        const selectedSeries = params.selected;
        const option = realtimeChart.getOption();
        console.log(option)
        if(params.name.includes ('功率因素')&& selectedSeries[params.name] == true){
          // console.log("总功率因素")
          option.legend[0].data.forEach(function(item, index) {
            // console.log(item)
            if (!item.includes ('功率因素')){
              option.legend[0].selected[item] = false;
            }
          });
        }
        if(!params.name.includes('功率因素')&& selectedSeries[params.name] == true){
          // option.legend[0].selected['总功率因素'] = false;
          option.legend[0].data.forEach(function(item, index) {
            // console.log(item)
            if (item.includes ('功率因素')){
              option.legend[0].selected[item] = false;
            }
          });
        }
        realtimeChart.setOption(option);
      });
    }
    // 每次切换图就要动态生成数据表头
    headerData.value = realtimeChart?.getOption()?.series as any[];
    updateTableData();
  }else{
    await getList();
    // 销毁原有的图表实例
    beforeUnmount()
    if ( isHaveData.value == true ){
      // 参数类型变回总
      paramType.value = 'total'
      // 创建新的图表实例
      realtimeChart?.off("legendselectchanged")
      realtimeChart?.dispose();
      realtimeChart = echarts.init(document.getElementById('chartContainer'));
      // 设置新的配置对象
      if (realtimeChart) {
        realtimeChart.setOption( {
          title: {text: ''},
          tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
          legend: { data: ['总平均有功功率', '总最大有功功率', '总最小有功功率','总平均视在功率', '总最大视在功率', '总最小视在功率'
                          , '总平均无功功率','总最大无功功率', '总最小无功功率', '总平均功率因素'],
                selected:  { 总平均有功功率: false, 总最大有功功率: true, 总最小有功功率: false, 总平均视在功率: false, 总最大视在功率: true, 总最小视在功率: false
                          , 总平均无功功率: false, 总最大无功功率: true, 总最小无功功率: false, 总平均功率因素: false}},
          grid: {left: '3%', right: '4%',bottom: '3%', containLabel: true },
          toolbox: {feature: { restore:{}, saveAsImage: {}}},
          xAxis: {type: 'category', boundaryGap: false, data: createTimeData.value},
          yAxis: { type: 'value'},
          series: [
            { name: '总平均有功功率', type: 'line',symbol: 'none',data: totalActivePowAvgValueData.value,itemStyle:{normal:{lineStyle:{color:'#E5B849'},color:'#E5B849'}}},
            { name: '总最大有功功率', type: 'line',symbol: 'none',data: totalActivePowMaxValueData.value, itemStyle:{normal:{lineStyle:{color:'#C8603A'},color:'#C8603A'}} },
            { name: '总最小有功功率',type: 'line',symbol: 'none',data: totalActivePowMinValueData.value, itemStyle:{normal:{lineStyle:{color:'#AD3762'},color:'#AD3762'}}},
            { name: '总平均视在功率',type: 'line',symbol: 'none',data:  totalApparentPowAvgValueData.value,itemStyle:{normal:{lineStyle:{color:'#B47660'},color:'#B47660'}}},
            { name: '总最大视在功率', type: 'line',symbol: 'none', data: totalApparentPowMaxValueData.value, itemStyle:{normal:{lineStyle:{color:'#614E43'},color:'#614E43'}}},
            { name: '总最小视在功率',type: 'line',symbol: 'none',data:  totalApparentPowMinValueData.value, itemStyle:{normal:{lineStyle:{color:'#5337A9'},color:'#5337A9'}}},

            { name: '总平均无功功率',type: 'line',symbol: 'none',data:  totalReactivePowAvgValueData.value,itemStyle:{normal:{lineStyle:{color:'#5D82DB'},color:'#5D82DB'}}},
            { name: '总最大无功功率', type: 'line',symbol: 'none', data: totalReactivePowMaxValueData.value, itemStyle:{normal:{lineStyle:{color:'#6899DC'},color:'#6899DC'}}},
            { name: '总最小无功功率',type: 'line',symbol: 'none',data:  totalReactivePowMinValueData.value, itemStyle:{normal:{lineStyle:{color:'#94B159'},color:'#94B159'}}},
            { name: '总平均功率因素',type: 'line',symbol: 'none',data:  factorTotalAvgValueData.value},
          ],
          dataZoom:[{type: "inside"}],
        });

        // 监听图例点击事件
      realtimeChart.on('legendselectchanged', function (params) {
        console.log(params.name)
        const selectedSeries = params.selected;
        const option = realtimeChart.getOption();
        console.log(option)
        if(params.name.includes ('功率因素')&& selectedSeries[params.name] == true){
          // console.log("总功率因素")
          option.legend[0].data.forEach(function(item, index) {
            // console.log(item)
            if (!item.includes ('功率因素')){
              option.legend[0].selected[item] = false;
            }
          });
        }
        if(!params.name.includes('功率因素')&& selectedSeries[params.name] == true){
          // option.legend[0].selected['总功率因素'] = false;
          option.legend[0].data.forEach(function(item, index) {
            // console.log(item)
            if (item.includes ('功率因素')){
              option.legend[0].selected[item] = false;
            }
          });
        }
        realtimeChart.setOption(option);
      });
      }
    }
    // 每次切换图就要动态生成数据表头
    headerData.value = realtimeChart?.getOption()?.series as any[];
    updateTableData();
  }
});

// 表格映射图数据
const updateTableData = () => {
  const data: any[] = [];
  const length = headerData.value?.[0]?.data?.length || 0;
  console.log("headerData===",headerData.value);
  for (let i = 0; i < length; i++) {
    const rowData: { [key: string]: any } = {};
    rowData['create_time'] = createTimeData.value[i];
    rowData['totalActivePowMaxTimeData'] = totalActivePowMaxTimeData.value[i];
    rowData['totalActivePowMinTimeData'] = totalActivePowMinTimeData.value[i];
    rowData['aActivePowMaxTimeData'] = aActivePowMaxTimeData.value[i];
    rowData['aActivePowMinTimeData'] = aActivePowMinTimeData.value[i];
    rowData['bActivePowMaxTimeData'] = bActivePowMaxTimeData.value[i];
    rowData['bActivePowMinTimeData'] = bActivePowMinTimeData.value[i];

    rowData['totalApparentPowMaxTimeData'] = totalApparentPowMaxTimeData.value[i];
    rowData['totalApparentPowMinTimeData'] = totalApparentPowMinTimeData.value[i];
    rowData['aApparentPowMaxTimeData'] = aApparentPowMaxTimeData.value[i];
    rowData['aApparentPowMinTimeData'] = aApparentPowMinTimeData.value[i];
    rowData['bApparentPowMaxTimeData'] = bApparentPowMaxTimeData.value[i];
    rowData['bApparentPowMinTimeData'] = bApparentPowMinTimeData.value[i];

    rowData['totalReactivePowMaxTimeData'] = totalReactivePowMaxTimeData.value[i];
    rowData['totalReactivePowMinTimeData'] = totalReactivePowMinTimeData.value[i];
    for (const item of headerData.value) {
      rowData[item.name] = item.data[i];
    }
    data.push(rowData);
  }
  tableData.value = data;
};

// 给折线图提示框的数据加单位
function customTooltipFormatter(params: any[]) {
  console.log("提示的params",params)
  var tooltipContent = ''; // X 轴数值
  params.forEach(function(item) {
    switch( item.seriesName ){
      case '总有功功率':
      case 'A路有功功率':
      case 'B路有功功率':
      case '总平均有功功率':
      case 'A路平均有功功率':
      case 'B路平均有功功率':
        tooltipContent += item.marker + ' 记录时间: ' +createTimeData.value[item.dataIndex] + ' ' + item.seriesName + ': ' + item.value + ' kW  <br/>';
        break;
      case '总视在功率':
      case 'A路视在功率':
      case 'B路视在功率':
      case '总平均视在功率':
      case 'A路平均视在功率':
      case 'B路平均视在功率':
      tooltipContent += item.marker + ' 记录时间: ' +createTimeData.value[item.dataIndex] +' ' +  item.seriesName + ': ' + item.value + ' kVA  <br/>';
      break;
      case '总无功功率':
      case 'A路无功功率':
      case 'B路无功功率':
      case '总平均无功功率':
      case 'A路平均无功功率':
      case 'B路平均无功功率':
        tooltipContent += item.marker + ' 记录时间: ' +createTimeData.value[item.dataIndex] + ' ' + item.seriesName + ': ' + item.value + ' kVar  <br/>';
        break;
      case 'A路功率因素':
      case 'B路功率因素':
      case '总功率因素':
      case '总平均功率因素':
      case 'A路平均功率因素':
      case 'B路平均功率因素':
      tooltipContent += item.marker + ' 记录时间: ' +createTimeData.value[item.dataIndex] + ' ' + item.seriesName + ': ' + item.value + '<br/>';
      break;
      case '总最大有功功率':
        tooltipContent += item.marker +' 发生时间: ' +totalActivePowMaxTimeData.value[item.dataIndex] +  ' ' + item.seriesName + ': ' + item.value + 'kW  <br/>';
        break;
      case '总最小有功功率':
        tooltipContent += item.marker +' 发生时间: ' +totalActivePowMinTimeData.value[item.dataIndex] +  ' ' + item.seriesName + ': ' + item.value + 'kW  <br/>';
        break;
      case 'A路最大有功功率':
        tooltipContent += item.marker +' 发生时间: ' +aActivePowMaxTimeData.value[item.dataIndex] +  ' ' + item.seriesName + ': ' + item.value + 'kW  <br/>';
        break;
      case 'A路最小有功功率':
        tooltipContent += item.marker +' 发生时间: ' +aActivePowMinTimeData.value[item.dataIndex] +  ' ' + item.seriesName + ': ' + item.value + 'kW  <br/>';
        break;
      case 'B路最大有功功率':
        tooltipContent += item.marker +' 发生时间: ' +bActivePowMaxTimeData.value[item.dataIndex] +  ' ' + item.seriesName + ': ' + item.value + 'kW  <br/>';
        break;
      case 'B路最小有功功率':
        tooltipContent += item.marker +' 发生时间: ' +bActivePowMinTimeData.value[item.dataIndex] + ' ' + item.seriesName + ': ' + item.value + 'kW  <br/>';
        break;
      case '总最大无功功率':
        tooltipContent += item.marker +' 发生时间: ' +totalReactivePowMaxTimeData.value[item.dataIndex] +  ' ' + item.seriesName + ': ' + item.value + 'kVar  <br/>';
        break;
      case '总最小无功功率':
        tooltipContent += item.marker +' 发生时间: ' +totalReactivePowMinTimeData.value[item.dataIndex] +  ' ' + item.seriesName + ': ' + item.value + 'kVar  <br/>';
        break;
     case '总最大视在功率':
        tooltipContent += item.marker +' 发生时间: ' +totalApparentPowMaxTimeData.value[item.dataIndex] +  ' ' + item.seriesName + ': ' + item.value + 'kVA  <br/>';
        break;
      case '总最小视在功率':
        tooltipContent += item.marker +' 发生时间: ' +totalApparentPowMinTimeData.value[item.dataIndex] +  ' ' + item.seriesName + ': ' + item.value + 'kVA  <br/>';
        break;
      case 'A路最大视在功率':
        tooltipContent += item.marker +' 发生时间: ' +aApparentPowMaxTimeData.value[item.dataIndex] +  ' ' + item.seriesName + ': ' + item.value + 'kVA  <br/>';
        break;
      case 'A路最小视在功率':
        tooltipContent += item.marker +' 发生时间: ' +aApparentPowMinTimeData.value[item.dataIndex] +  ' ' + item.seriesName + ': ' + item.value + 'kVA  <br/>';
        break;
      case 'B路最大视在功率':
        tooltipContent += item.marker +' 发生时间: ' +bApparentPowMaxTimeData.value[item.dataIndex] +  ' ' + item.seriesName + ': ' + item.value + 'kVA  <br/>';
        break;
      case 'B路最小视在功率':
        tooltipContent += item.marker +' 发生时间: ' +bApparentPowMinTimeData.value[item.dataIndex] + ' ' + item.seriesName + ': ' + item.value + 'kVA  <br/>';
        break;
    }
  });
  return tooltipContent;
}

// 原始数据默认查询的时间范围
function defaultHourTimeRange(hour: number){
  // 先获取本地时区偏移量（以分钟为单位，需要转换为毫秒）
  var timezoneOffset = new Date().getTimezoneOffset() * 60 * 1000
  // 计算当前时间和1小时前的时间，并考虑时区偏移量
  var end = new Date(new Date().getTime() - timezoneOffset);
  var start = new Date(end.getTime() - 60 * 60 * 1000 * hour);
  // 格式化时间并返回
  return [
    start.toISOString().slice(0, 19).replace('T', ' '), 
    end.toISOString().slice(0, 19).replace('T', ' ')
  ]
}

// 默认查询的时间范围，单位：月
function defaultMonthTimeRange(month) {
  // 获取当前日期
  var endDate = new Date();

  // 计算指定月数前的日期
  var startDate = new Date();
  startDate.setMonth(startDate.getMonth() - month);

  // 格式化日期并返回
  return [
    startDate.toISOString().slice(0, 19).replace('T', ' '), 
    endDate.toISOString().slice(0, 19).replace('T', ' ') 
  ];
}

// 禁选未来的日期
const disabledDate = (date) => {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  // 设置date的时间为0时0分0秒，以便与today进行比较
  date.setHours(0, 0, 0, 0);
  // 如果date在今天之后，则禁用
  return date > today;
}

// 处理数据后有几位小数点
function formatNumber(value, decimalPlaces) {
    if (!isNaN(value)) {
        return Number(value).toFixed(decimalPlaces);
    } else {
        return null; // 或者其他默认值
    }
}

/** 搜索按钮操作 */
const handleQuery = async() => {
  // let temp:any=null;
  // if(paramType.value!="total"){
  //   temp=paramType.value;
  //   paramType.value="total";
  // }
  await getList();
  tableLoading.value = true;
  try{
    const newParamType = paramType.value;
  if(activeName.value == 'realtimeTabPane'){
    // console.log('realtimeTabPane======================')
    if(isHaveData.value){
      if(newParamType=='total'){
          maxActivePowDataTemp.value = Math.max(...totalActivePowData.value);
          minActivePowDataTemp.value = Math.min(...totalActivePowData.value);
          totalActivePowData.value.forEach(function(num, index) {
            if (num == maxActivePowDataTemp.value){
              maxActivePowDataTimeTemp.value = createTimeData.value[index]
            }
            if (num == minActivePowDataTemp.value){
              minActivePowDataTimeTemp.value = createTimeData.value[index]
            }
          });
        }else if(newParamType=='a'){
          maxActivePowDataTemp.value = Math.max(...aActivePowData.value);
          minActivePowDataTemp.value = Math.min(...aActivePowData.value);
          aActivePowData.value.forEach(function(num, index) {
            if (num == maxActivePowDataTemp.value){
              maxActivePowDataTimeTemp.value = createTimeData.value[index]
            }
            if (num == minActivePowDataTemp.value){
              minActivePowDataTimeTemp.value = createTimeData.value[index]
            }
          });
        }else if(newParamType=='b'){
          maxActivePowDataTemp.value = Math.max(...bActivePowData.value);
          minActivePowDataTemp.value = Math.min(...bActivePowData.value);
          bActivePowData.value.forEach(function(num, index) {
            if (num == maxActivePowDataTemp.value){
              maxActivePowDataTimeTemp.value = createTimeData.value[index]
            }
            if (num == minActivePowDataTemp.value){
              minActivePowDataTimeTemp.value = createTimeData.value[index]
            }
          });
        }
    }else{
      maxActivePowDataTimeTemp.value=null;
      minActivePowDataTimeTemp.value=null;
      minActivePowDataTemp.value=null;
      maxActivePowDataTemp.value=null;
    }
    realtimeChart?.off("legendselectchanged")
    realtimeChart?.dispose();
    realtimeChart = echarts.init(document.getElementById('chartContainer'));
    if ( newParamType == 'total'){
      realtimeChart?.setOption({
        title: { text: ''},
        tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
        grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
        toolbox: {feature: { restore:{}, saveAsImage: {}}},
        xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
        yAxis: { type: 'value'},
        dataZoom:[{type: "inside"}],
        legend: { data: ['总有功功率', '总视在功率','总无功功率','总功率因素'] 
          ,selected:{"总有功功率":true,"总视在功率":true,"总无功功率":true,"总功率因素":false}
        },
        series: [
          {name: '总有功功率', type: 'line', symbol: 'none', data: totalActivePowData.value,itemStyle:{normal:{lineStyle:{color:'#E5B849'},color:'#E5B849'}}},
          {name: '总视在功率', type: 'line', symbol: 'none', data: totalApparentPowData.value,itemStyle:{normal:{lineStyle:{color:'#C8603A'},color:'#C8603A'}}},
          {name: '总无功功率', type: 'line', symbol: 'none', data: totalReactivePowData.value,itemStyle:{normal:{lineStyle:{color:'#AD3762'},color:'#AD3762'}}},
          {name: '总功率因素', type: 'line', symbol: 'none', data: factorTotalData.value,itemStyle:{normal:{lineStyle:{color:'#B47660'},color:'#B47660'}}},
        ],
      })
    }else if( newParamType == 'a' ){
      realtimeChart?.setOption({
        title: { text: ''},
        tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
        grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
        toolbox: {feature: { restore:{}, saveAsImage: {}}},
        xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
        yAxis: { type: 'value'},
        dataZoom:[{type: "inside"}],
        legend: { data: ['A路有功功率', 'A路视在功率', 'A路无功功率', 'A路功率因素'],
          selected:{"A路有功功率":true,"A路视在功率":true,"A路无功功率":true,"A路功率因素":false}
         },
        series: [
          {name: 'A路有功功率', type: 'line', symbol: 'none', data: aActivePowData.value,itemStyle:{normal:{lineStyle:{color:'#E5B849'},color:'#E5B849'}}},
          {name: 'A路视在功率', type: 'line', symbol: 'none', data: aApparentPowData.value,itemStyle:{normal:{lineStyle:{color:'#C8603A'},color:'#C8603A'}}},
          {name: 'A路无功功率', type: 'line', symbol: 'none', data: aReactivePowData.value,itemStyle:{normal:{lineStyle:{color:'#AD3762'},color:'#AD3762'}}},
          {name: 'A路功率因素', type: 'line', symbol: 'none', data: factorAData.value,itemStyle:{normal:{lineStyle:{color:'#B47660'},color:'#B47660'}}},
        ],
      })
    }else{
      realtimeChart?.setOption({
        title: { text: ''},
        tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
        grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
        toolbox: {feature: { restore:{}, saveAsImage: {}}},
        xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
        yAxis: { type: 'value'},
        dataZoom:[{type: "inside"}],
        legend: { data: ['B路有功功率', 'B路视在功率', 'B路无功功率', 'B路功率因素'],
          selected:{"B路有功功率":true,"B路视在功率":true,"B路无功功率":true,"B路功率因素":false}
         },
        series: [
          {name: 'B路有功功率', type: 'line', symbol: 'none', data: bActivePowData.value,itemStyle:{normal:{lineStyle:{color:'#E5B849'},color:'#E5B849'}}},
          {name: 'B路视在功率', type: 'line', symbol: 'none', data: bApparentPowData.value,itemStyle:{normal:{lineStyle:{color:'#C8603A'},color:'#C8603A'}}},
          {name: 'B路无功功率', type: 'line', symbol: 'none', data: bReactivePowData.value,itemStyle:{normal:{lineStyle:{color:'#AD3762'},color:'#AD3762'}}},
          {name: 'B路功率因素', type: 'line', symbol: 'none', data: factorBData.value,itemStyle:{normal:{lineStyle:{color:'#B47660'},color:'#B47660'}}},
        ],
      })
    }
  }else{
    if(isHaveData.value){
      if(newParamType=='total'){
          maxActivePowDataTemp.value = Math.max(...totalActivePowMaxValueData.value);
          minActivePowDataTemp.value = Math.min(...totalActivePowMaxValueData.value);
          totalActivePowMaxValueData.value.forEach(function(num, index) {
            if (num == maxActivePowDataTemp.value){
              maxActivePowDataTimeTemp.value = createTimeData.value[index]
            }
            if (num == minActivePowDataTemp.value){
              minActivePowDataTimeTemp.value = createTimeData.value[index]
            }
          });
        }else if(newParamType=='a'){
          maxActivePowDataTemp.value = Math.max(...aActivePowMaxValueData.value);
          minActivePowDataTemp.value = Math.min(...aActivePowMaxValueData.value);
          aActivePowMaxValueData.value.forEach(function(num, index) {
            if (num == maxActivePowDataTemp.value){
              maxActivePowDataTimeTemp.value = createTimeData.value[index]
            }
            if (num == minActivePowDataTemp.value){
              minActivePowDataTimeTemp.value = createTimeData.value[index]
            }
          });
        }else if(newParamType=='b'){
          maxActivePowDataTemp.value = Math.max(...bActivePowMaxValueData.value);
          minActivePowDataTemp.value = Math.min(...bActivePowMaxValueData.value);
          bActivePowMaxValueData.value.forEach(function(num, index) {
            if (num == maxActivePowDataTemp.value){
              maxActivePowDataTimeTemp.value = createTimeData.value[index]
            }
            if (num == minActivePowDataTemp.value){
              minActivePowDataTimeTemp.value = createTimeData.value[index]
            }
          });
        }
    }else{
      maxActivePowDataTimeTemp.value=null;
      minActivePowDataTimeTemp.value=null;
      minActivePowDataTemp.value=null;
      maxActivePowDataTemp.value=null;
    }
    realtimeChart?.off("legendselectchanged")
    realtimeChart?.dispose();
    realtimeChart = echarts.init(document.getElementById('chartContainer'));
    if ( newParamType == 'total'){
      realtimeChart.setOption( {
          title: {text: ''},
          tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
          legend: { data: ['总平均有功功率', '总最大有功功率', '总最小有功功率','总平均视在功率', '总最大视在功率', '总最小视在功率'
                          , '总平均无功功率','总最大无功功率', '总最小无功功率', '总平均功率因素'],
                selected:  { 总平均有功功率: false, 总最大有功功率: true, 总最小有功功率: false, 总平均视在功率: false, 总最大视在功率: true, 总最小视在功率: false
                          , 总平均无功功率: false, 总最大无功功率: true, 总最小无功功率: false, 总平均功率因素: false}},
          grid: {left: '3%', right: '4%',bottom: '3%', containLabel: true },
          toolbox: {feature: { restore:{}, saveAsImage: {}}},
          xAxis: {type: 'category', boundaryGap: false, data: createTimeData.value},
          yAxis: { type: 'value'},
          series: [
            { name: '总平均有功功率',symbol: 'none', type: 'line',data: totalActivePowAvgValueData.value,itemStyle:{normal:{lineStyle:{color:'#E5B849'},color:'#E5B849'}}},
            { name: '总最大有功功率',symbol: 'none', type: 'line',data: totalActivePowMaxValueData.value, itemStyle:{normal:{lineStyle:{color:'#C8603A'},color:'#C8603A'}} },
            { name: '总最小有功功率',symbol: 'none',type: 'line',data: totalActivePowMinValueData.value, itemStyle:{normal:{lineStyle:{color:'#AD3762'},color:'#AD3762'}}},
            { name: '总平均视在功率',symbol: 'none',type: 'line',data:  totalApparentPowAvgValueData.value,itemStyle:{normal:{lineStyle:{color:'#B47660'},color:'#B47660'}}},
            { name: '总最大视在功率',symbol: 'none', type: 'line', data: totalApparentPowMaxValueData.value,itemStyle:{normal:{lineStyle:{color:'#614E43'},color:'#614E43'}}},
            { name: '总最小视在功率',symbol: 'none',type: 'line',data:  totalApparentPowMinValueData.value,itemStyle:{normal:{lineStyle:{color:'#5337A9'},color:'#5337A9'}}},

            { name: '总平均无功功率',symbol: 'none',type: 'line',data:  totalReactivePowAvgValueData.value,itemStyle:{normal:{lineStyle:{color:'#5D82DB'},color:'#5D82DB'}}},
            { name: '总最大无功功率',symbol: 'none', type: 'line', data: totalReactivePowMaxValueData.value, itemStyle:{normal:{lineStyle:{color:'#6899DC'},color:'#6899DC'}}},
            { name: '总最小无功功率',symbol: 'none',type: 'line',data:  totalReactivePowMinValueData.value, itemStyle:{normal:{lineStyle:{color:'#94B159'},color:'#94B159'}}},
            { name: '总平均功率因素',symbol: 'none',type: 'line',data:  factorTotalAvgValueData.value},
          ],
          dataZoom:[{type: "inside"}],
        });
    }else if( newParamType == 'a' ){
      realtimeChart.setOption( {
          title: {text: ''},
          tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
          legend: { data: ['A路平均有功功率', 'A路最大有功功率', 'A路最小有功功率','A路平均视在功率', 'A路最大视在功率', 'A路最小视在功率'
                , 'A路平均无功功率', 'A路平均功率因素'],
              selected: { A路平均有功功率: false, A路最大有功功率: true, A路最小有功功率: false, A路平均视在功率: false, A路最大视在功率: true, A路最小视在功率: false
                    , A路平均无功功率: false, A路平均功率因素: false}},
          grid: {left: '3%', right: '4%',bottom: '3%', containLabel: true },
          toolbox: {feature: { restore:{}, saveAsImage: {}}},
          xAxis: {type: 'category', boundaryGap: false, data: createTimeData.value},
          yAxis: { type: 'value'},
          series: [
          { name: 'A路平均有功功率',symbol: 'none', type: 'line', data: aActivePowAvgValueData.value,itemStyle:{normal:{lineStyle:{color:'#E5B849'},color:'#E5B849'}}},
          { name: 'A路最大有功功率',symbol: 'none', type: 'line', data: aActivePowMaxValueData.value, itemStyle:{normal:{lineStyle:{color:'#C8603A'},color:'#C8603A'}} },
          { name: 'A路最小有功功率',symbol: 'none', type: 'line', data: aActivePowMinValueData.value, itemStyle:{normal:{lineStyle:{color:'#AD3762'},color:'#AD3762'}}},
          { name: 'A路平均视在功率',symbol: 'none', type: 'line', data: aApparentPowAvgValueData.value,itemStyle:{normal:{lineStyle:{color:'#B47660'},color:'#B47660'}}},
          { name: 'A路最大视在功率',symbol: 'none', type: 'line', data: aApparentPowMaxValueData.value, itemStyle:{normal:{lineStyle:{color:'#614E43'},color:'#614E43'}}},
          { name: 'A路最小视在功率',symbol: 'none', type: 'line', data: aApparentPowMinValueData.value, itemStyle:{normal:{lineStyle:{color:'#5337A9'},color:'#5337A9'}}},
          { name: 'A路平均无功功率',symbol: 'none',type: 'line',data:  aReactivePowAvgValueData.value,itemStyle:{normal:{lineStyle:{color:'#5D82DB'},color:'#5D82DB'}}},
          { name: 'A路平均功率因素',symbol: 'none',type: 'line',data:  factorAAvgValueData.value,itemStyle:{normal:{lineStyle:{color:'#6899DC'},color:'#6899DC'}}},
          ],
          dataZoom:[{type: "inside"}],
        });
    }else{
      realtimeChart.setOption( {
          title: {text: ''},
          tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
          legend: { data: ['B路平均有功功率', 'B路最大有功功率', 'B路最小有功功率','B路平均视在功率', 'B路最大视在功率', 'B路最小视在功率'
                    , 'B路平均无功功率', 'B路平均功率因素'],
              selected: { B路平均有功功率: false, B路最大有功功率: true, B路最小有功功率: false, B路平均视在功率: false, B路最大视在功率: true, B路最小视在功率: false
              , B路平均无功功率: false, B路平均功率因素: false}},
          grid: {left: '3%', right: '4%',bottom: '3%', containLabel: true },
          toolbox: {feature: { restore:{}, saveAsImage: {}}},
          xAxis: {type: 'category', boundaryGap: false, data: createTimeData.value},
          yAxis: { type: 'value'},
          series: [
          { name: 'B路平均有功功率',symbol: 'none', type: 'line', data: bActivePowAvgValueData.value,itemStyle:{normal:{lineStyle:{color:'#E5B849'},color:'#E5B849'}}},
          { name: 'B路最大有功功率',symbol: 'none', type: 'line', data: bActivePowMaxValueData.value, itemStyle:{normal:{lineStyle:{color:'#C8603A'},color:'#C8603A'}} },
          { name: 'B路最小有功功率',symbol: 'none', type: 'line', data: bActivePowMinValueData.value, itemStyle:{normal:{lineStyle:{color:'#AD3762'},color:'#AD3762'}}},
          { name: 'B路平均视在功率',symbol: 'none', type: 'line', data: bApparentPowAvgValueData.value,itemStyle:{normal:{lineStyle:{color:'#B47660'},color:'#B47660'}}},
          { name: 'B路最大视在功率',symbol: 'none', type: 'line', data: bApparentPowMaxValueData.value, itemStyle:{normal:{lineStyle:{color:'#614E43'},color:'#614E43'}}},
          { name: 'B路最小视在功率',symbol: 'none', type: 'line', data: bApparentPowMinValueData.value, itemStyle:{normal:{lineStyle:{color:'#5337A9'},color:'#5337A9'}}},
          { name: 'B路平均无功功率',symbol: 'none',type: 'line',data:  bReactivePowAvgValueData.value,itemStyle:{normal:{lineStyle:{color:'#5D82DB'},color:'#5D82DB'}}},
          { name: 'B路平均功率因素',symbol: 'none',type: 'line',data:  factorBAvgValueData.value,itemStyle:{normal:{lineStyle:{color:'#6899DC'},color:'#6899DC'}}},
          ],
          dataZoom:[{type: "inside"}],
        });
    }
  }
  // 监听图例点击事件
  realtimeChart?.on('legendselectchanged', function (params) {
        console.log(params.name)
        const selectedSeries = params.selected;
        const option = realtimeChart.getOption();
        console.log(option)
        if(params.name.includes ('功率因素')&& selectedSeries[params.name] == true){
          // console.log("总功率因素")
          option.legend[0].data.forEach(function(item, index) {
            // console.log(item)
            if (!item.includes ('功率因素')){
              option.legend[0].selected[item] = false;
            }
          });
        }
        if(!params.name.includes('功率因素')&& selectedSeries[params.name] == true){
          // option.legend[0].selected['总功率因素'] = false;
          option.legend[0].data.forEach(function(item, index) {
            // console.log(item)
            if (item.includes ('功率因素')){
              option.legend[0].selected[item] = false;
            }
          });
        }
        realtimeChart.setOption(option);
      });
  // 每次切换图就要动态生成数据表头
  headerData.value = realtimeChart?.getOption()?.series as any[];
  console.log("headerData",headerData.value)
  updateTableData();
  }finally {
    tableLoading.value = false;
  }
  // await getRankChartData();
  // initChart();
  // if(temp!=null){
  //   paramType.value=temp
  // }
  
  // initRankChart();
  // tableLoading.value = true;
  // try{
  //   // const newParamType = newValues;
  // if(activeName.value == 'realtimeTabPane'){
  //   if(isHaveData.value){
  //     if(paramType.value=='total'){
  //         maxActivePowDataTemp.value = Math.max(...totalActivePowData.value);
  //         minActivePowDataTemp.value = Math.min(...totalActivePowData.value);
  //         totalActivePowData.value.forEach(function(num, index) {
  //           if (num == maxActivePowDataTemp.value){
  //             maxActivePowDataTimeTemp.value = createTimeData.value[index]
  //           }
  //           if (num == minActivePowDataTemp.value){
  //             minActivePowDataTimeTemp.value = createTimeData.value[index]
  //           }
  //         });
  //       }else if(paramType.value=='a'){
  //         maxActivePowDataTemp.value = Math.max(...aActivePowData.value);
  //         minActivePowDataTemp.value = Math.min(...aActivePowData.value);
  //         aActivePowData.value.forEach(function(num, index) {
  //           if (num == maxActivePowDataTemp.value){
  //             maxActivePowDataTimeTemp.value = createTimeData.value[index]
  //           }
  //           if (num == minActivePowDataTemp.value){
  //             minActivePowDataTimeTemp.value = createTimeData.value[index]
  //           }
  //         });
  //       }else if(paramType.value=='b'){
  //         maxActivePowDataTemp.value = Math.max(...bActivePowData.value);
  //         minActivePowDataTemp.value = Math.min(...bActivePowData.value);
  //         bActivePowData.value.forEach(function(num, index) {
  //           if (num == maxActivePowDataTemp.value){
  //             maxActivePowDataTimeTemp.value = createTimeData.value[index]
  //           }
  //           if (num == minActivePowDataTemp.value){
  //             minActivePowDataTimeTemp.value = createTimeData.value[index]
  //           }
  //         });
  //       }
  //   }else{
  //     maxActivePowDataTimeTemp.value=null;
  //     minActivePowDataTimeTemp.value=null;
  //     minActivePowDataTemp.value=null;
  //     maxActivePowDataTemp.value=null;
  //   }
  //   if ( paramType.value == 'total'){
  //     realtimeChart?.setOption({
  //       legend: { data: ['总有功功率', '总视在功率','总无功功率','总功率因素'] 
  //         ,selected:{"总有功功率":true,"总视在功率":true,"总无功功率":true,"总功率因素":false}
  //       },
  //       series: [
  //         {name: '总有功功率', type: 'line', symbol: 'none', data: totalActivePowData.value},
  //         {name: '总视在功率', type: 'line', symbol: 'none', data: totalApparentPowData.value},
  //         {name: '总无功功率', type: 'line', symbol: 'none', data: totalReactivePowData.value},
  //         {name: '总功率因素', type: 'line', symbol: 'none', data: factorTotalData.value},
  //       ],
  //     })
  //   }else if( paramType.value == 'a' ){
  //     realtimeChart?.setOption({
  //       legend: { data: ['A路有功功率', 'A路视在功率', 'A路无功功率', 'A路功率因素'],
  //         selected:{"A路有功功率":true,"A路视在功率":true,"A路无功功率":true,"A路功率因素":false}
  //        },
  //       series: [
  //         {name: 'A路有功功率', type: 'line', symbol: 'none', data: aActivePowData.value},
  //         {name: 'A路视在功率', type: 'line', symbol: 'none', data: aApparentPowData.value},
  //         {name: 'A路无功功率', type: 'line', symbol: 'none', data: aReactivePowData.value},
  //         {name: 'A路功率因素', type: 'line', symbol: 'none', data: factorAData.value},
  //       ],
  //     })
  //   }else{
  //     realtimeChart?.setOption({
  //       legend: { data: ['B路有功功率', 'B路视在功率', 'B路无功功率', 'B路功率因素'],
  //         selected:{"B路有功功率":true,"B路视在功率":true,"B路无功功率":true,"B路功率因素":false}
  //        },
  //       series: [
  //         {name: 'B路有功功率', type: 'line', symbol: 'none', data: bActivePowData.value},
  //         {name: 'B路视在功率', type: 'line', symbol: 'none', data: bApparentPowData.value},
  //         {name: 'B路无功功率', type: 'line', symbol: 'none', data: bReactivePowData.value},
  //         {name: 'B路功率因素', type: 'line', symbol: 'none', data: factorBData.value},
  //       ],
  //     })
  //   }
  // }else{
  //   if(isHaveData.value){
  //     if(paramType.value=='total'){
  //         maxActivePowDataTemp.value = Math.max(...totalActivePowMaxValueData.value);
  //         minActivePowDataTemp.value = Math.min(...totalActivePowMaxValueData.value);
  //         totalActivePowMaxValueData.value.forEach(function(num, index) {
  //           if (num == maxActivePowDataTemp.value){
  //             maxActivePowDataTimeTemp.value = createTimeData.value[index]
  //           }
  //           if (num == minActivePowDataTemp.value){
  //             minActivePowDataTimeTemp.value = createTimeData.value[index]
  //           }
  //         });
  //       }else if(paramType.value=='a'){
  //         maxActivePowDataTemp.value = Math.max(...aActivePowMaxValueData.value);
  //         minActivePowDataTemp.value = Math.min(...aActivePowMaxValueData.value);
  //         aActivePowMaxValueData.value.forEach(function(num, index) {
  //           if (num == maxActivePowDataTemp.value){
  //             maxActivePowDataTimeTemp.value = createTimeData.value[index]
  //           }
  //           if (num == minActivePowDataTemp.value){
  //             minActivePowDataTimeTemp.value = createTimeData.value[index]
  //           }
  //         });
  //       }else if(paramType.value=='b'){
  //         maxActivePowDataTemp.value = Math.max(...bActivePowMaxValueData.value);
  //         minActivePowDataTemp.value = Math.min(...bActivePowMaxValueData.value);
  //         bActivePowMaxValueData.value.forEach(function(num, index) {
  //           if (num == maxActivePowDataTemp.value){
  //             maxActivePowDataTimeTemp.value = createTimeData.value[index]
  //           }
  //           if (num == minActivePowDataTemp.value){
  //             minActivePowDataTimeTemp.value = createTimeData.value[index]
  //           }
  //         });
  //       }
  //   }else{
  //     maxActivePowDataTimeTemp.value=null;
  //     minActivePowDataTimeTemp.value=null;
  //     minActivePowDataTemp.value=null;
  //     maxActivePowDataTemp.value=null;
  //   }
  //   realtimeChart?.off("legendselectchanged")
  //   realtimeChart?.dispose();
  //   realtimeChart = echarts.init(document.getElementById('chartContainer'));
  //   if ( paramType.value == 'total'){
  //     realtimeChart.setOption( {
  //         title: {text: ''},
  //         tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
  //         legend: { data: ['总平均有功功率', '总最大有功功率', '总最小有功功率','总平均视在功率', '总最大视在功率', '总最小视在功率'
  //                         , '总平均无功功率','总最大无功功率', '总最小无功功率', '总平均功率因素'],
  //               selected:  { 总平均有功功率: false, 总最大有功功率: true, 总最小有功功率: false, 总平均视在功率: false, 总最大视在功率: true, 总最小视在功率: false
  //                         , 总平均无功功率: false, 总最大无功功率: true, 总最小无功功率: false, 总平均功率因素: false}},
  //         grid: {left: '3%', right: '4%',bottom: '3%', containLabel: true },
  //         toolbox: {feature: { restore:{}, saveAsImage: {}}},
  //         xAxis: {type: 'category', boundaryGap: false, data: createTimeData.value},
  //         yAxis: { type: 'value'},
  //         series: [
  //           { name: '总平均有功功率', type: 'line',data: totalActivePowAvgValueData.value},
  //           { name: '总最大有功功率', type: 'line',data: totalActivePowMaxValueData.value, lineStyle: {type: 'dashed'} },
  //           { name: '总最小有功功率',type: 'line',data: totalActivePowMinValueData.value, lineStyle: {type: 'dashed'}},
  //           { name: '总平均视在功率',type: 'line',data:  totalApparentPowAvgValueData.value},
  //           { name: '总最大视在功率', type: 'line', data: totalApparentPowMaxValueData.value, lineStyle: {type: 'dashed'}},
  //           { name: '总最小视在功率',type: 'line',data:  totalApparentPowMinValueData.value, lineStyle: {type: 'dashed'}},

  //           { name: '总平均无功功率',type: 'line',data:  totalReactivePowAvgValueData.value},
  //           { name: '总最大无功功率', type: 'line', data: totalReactivePowMaxValueData.value, lineStyle: {type: 'dashed'}},
  //           { name: '总最小无功功率',type: 'line',data:  totalReactivePowMinValueData.value, lineStyle: {type: 'dashed'}},
  //           { name: '总平均功率因素',type: 'line',data:  factorTotalAvgValueData.value},
  //         ],
  //         dataZoom:[{type: "inside"}],
  //       });
  //   }else if( paramType.value == 'a' ){
  //     realtimeChart.setOption( {
  //         title: {text: ''},
  //         tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
  //         legend: { data: ['A路平均有功功率', 'A路最大有功功率', 'A路最小有功功率','A路平均视在功率', 'A路最大视在功率', 'A路最小视在功率'
  //               , 'A路平均无功功率', 'A路平均功率因素'],
  //             selected: { A路平均有功功率: false, A路最大有功功率: true, A路最小有功功率: false, A路平均视在功率: false, A路最大视在功率: true, A路最小视在功率: false
  //                   , A路平均无功功率: false, A路平均功率因素: false}},
  //         grid: {left: '3%', right: '4%',bottom: '3%', containLabel: true },
  //         toolbox: {feature: { restore:{}, saveAsImage: {}}},
  //         xAxis: {type: 'category', boundaryGap: false, data: createTimeData.value},
  //         yAxis: { type: 'value'},
  //         series: [
  //         { name: 'A路平均有功功率', type: 'line', data: aActivePowAvgValueData.value},
  //         { name: 'A路最大有功功率', type: 'line', data: aActivePowMaxValueData.value, lineStyle: {type: 'dashed'} },
  //         { name: 'A路最小有功功率', type: 'line', data: aActivePowMinValueData.value, lineStyle: {type: 'dashed'}},
  //         { name: 'A路平均视在功率', type: 'line', data: aApparentPowAvgValueData.value},
  //         { name: 'A路最大视在功率', type: 'line', data: aApparentPowMaxValueData.value, lineStyle: {type: 'dashed'}},
  //         { name: 'A路最小视在功率', type: 'line', data: aApparentPowMinValueData.value, lineStyle: {type: 'dashed'}},
  //         { name: 'A路平均无功功率',type: 'line',data:  aReactivePowAvgValueData.value},
  //         { name: 'A路平均功率因素',type: 'line',data:  factorAAvgValueData.value},
  //         ],
  //         dataZoom:[{type: "inside"}],
  //       });
  //   }else{
  //     realtimeChart.setOption( {
  //         title: {text: ''},
  //         tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
  //         legend: { data: ['B路平均有功功率', 'B路最大有功功率', 'B路最小有功功率','B路平均视在功率', 'B路最大视在功率', 'B路最小视在功率'
  //                   , 'B路平均无功功率', 'B路平均功率因素'],
  //             selected: { B路平均有功功率: false, B路最大有功功率: true, B路最小有功功率: false, B路平均视在功率: false, B路最大视在功率: true, B路最小视在功率: false
  //             , B路平均无功功率: false, B路平均功率因素: false}},
  //         grid: {left: '3%', right: '4%',bottom: '3%', containLabel: true },
  //         toolbox: {feature: { restore:{}, saveAsImage: {}}},
  //         xAxis: {type: 'category', boundaryGap: false, data: createTimeData.value},
  //         yAxis: { type: 'value'},
  //         series: [
  //         { name: 'B路平均有功功率', type: 'line', data: bActivePowAvgValueData.value},
  //         { name: 'B路最大有功功率', type: 'line', data: bActivePowMaxValueData.value, lineStyle: {type: 'dashed'} },
  //         { name: 'B路最小有功功率', type: 'line', data: bActivePowMinValueData.value, lineStyle: {type: 'dashed'}},
  //         { name: 'B路平均视在功率', type: 'line', data: bApparentPowAvgValueData.value},
  //         { name: 'B路最大视在功率', type: 'line', data: bApparentPowMaxValueData.value, lineStyle: {type: 'dashed'}},
  //         { name: 'B路最小视在功率', type: 'line', data: bApparentPowMinValueData.value, lineStyle: {type: 'dashed'}},
  //         { name: 'B路平均无功功率',type: 'line',data:  bReactivePowAvgValueData.value},
  //         { name: 'B路平均功率因素',type: 'line',data:  factorBAvgValueData.value},
  //         ],
  //         dataZoom:[{type: "inside"}],
  //       });
  //   }
  // }
  // // 监听图例点击事件
  // realtimeChart?.on('legendselectchanged', function (params) {
  //       console.log(params.name)
  //       const selectedSeries = params.selected;
  //       const option = realtimeChart.getOption();
  //       console.log(option)
  //       if(params.name.includes ('功率因素')&& selectedSeries[params.name] == true){
  //         // console.log("总功率因素")
  //         option.legend[0].data.forEach(function(item, index) {
  //           // console.log(item)
  //           if (!item.includes ('功率因素')){
  //             option.legend[0].selected[item] = false;
  //           }
  //         });
  //       }
  //       if(!params.name.includes('功率因素')&& selectedSeries[params.name] == true){
  //         // option.legend[0].selected['总功率因素'] = false;
  //         option.legend[0].data.forEach(function(item, index) {
  //           // console.log(item)
  //           if (item.includes ('功率因素')){
  //             option.legend[0].selected[item] = false;
  //           }
  //         });
  //       }
  //       realtimeChart.setOption(option);
  //     });
  // // 每次切换图就要动态生成数据表头
  // headerData.value = realtimeChart?.getOption()?.series as any[];
  // console.log("headerData",headerData.value)
  // updateTableData();
  // }finally {
  //   tableLoading.value = false;
  // }
}

// 导航栏选择后触发
const handleClick = async (row) => {
  queryParams.roomId = row.id
  nowAddressTemp.value = row.name
  paramType.value="total"
  handleQuery();
}


// 接口获取机房导航列表
const getNavList = async() => {
  const res = await IndexApi.getRoomList()
  // navList.value = res
  navList.value=res.map((item)=>{return {id:item.id,name:item.roomName,children:[]}})
}
const currentKey = ref(null)
if(history.state.roomId!=null){
  currentKey.value=history.state.roomId
}
/** 初始化 **/
onMounted( async () => {
  getNavList()
  const queryRoomId = history.state.roomId as string | undefined;
  const queryLocation = history.state.location as string;
  queryParams.roomId = queryRoomId ? parseInt(queryRoomId, 10) : undefined;
  if(calculateTime(queryParams.timeRange[1],queryParams.timeRange[0])>calculateTime(queryParams.timeRange[1],preMonth(queryParams.timeRange[1]))){
    queryParams.timeRange=[preMonth(queryParams.timeRange[1]),queryParams.timeRange[1]]
  }
  if (queryParams.roomId != undefined){
    await handleQuery();
    nowAddress.value = queryLocation
    nowAddressTemp.value =queryLocation
  }
})
//导出Excel
const handleExport = async () => {
  try {
    // 导出的二次确认
    if (queryParams.roomId == null){
      ElMessage.warning('请选择机房')
      return
    }
    await message.exportConfirm()
    // 发起导出
    exportLoading.value = true
    queryParams.nowAddress = nowAddress.value;
    const axiosConfig = {
      timeout: 0 // 设置超时时间为0
    }
    const data =  await HistoryDataApi.getHistoryDataDetailsExcel({...queryParams,abtotal:paramType.value}, axiosConfig)
    await download.excel(data, '机房趋势分析.xlsx')
  } catch (error) {
    // 处理异常
    console.error('导出失败：', error)
  } finally {
    exportLoading.value = false
  }
}
</script>

<style scoped>
/*  
// 表格部分样式
// 最外层透明 */
:deep( .el-table),
:deep( .el-table__expanded-cell) {
  background-color: transparent;
  color: #93dcfe;
  font-size: 24px;
}
 
/* 表格内背景颜色  */
:deep( .el-table th),
:deep( .el-table tr),
:deep( .el-table td) {
  background-color: transparent;
  border: 0px;
  color: #93dcfe;
  font-size: 24px;
  height: 5px;
  font-family: Source Han Sans CN Normal, Source Han Sans CN Normal-Normal;
  font-weight: Normal;
}
 
/* // 去掉最下面的那一条线  */
.el-table::before {
  height: 0px;
}
 
/* // 设置表格行高度 */
:deep( .el-table__body tr)
:deep( .el-table__body td) {
  padding: 0;
  height: 54px;
}
 
/* // 修改高亮当前行颜色 */
:deep( .el-table tbody tr:hover>td ){
  background: #063570 !important;
}
 
/* // 取消当前行高亮 */
:deep( .el-table tbody tr) {
  pointer-events: none;
}
.description-item {
  display: flex;
  align-items: center;
}

/* 修改表头样式-加边框 */
/* ::v-deep .el-table__header-wrapper {
  border: solid 1px #04c2ed;
} */
 
/* // 表格斑马自定义颜色 */
:deep(.el-table__row.warning-row)  {
  background: #01205A;
}
 
 
/* 去掉表格里的padding */
:deep(.el-table .cell),
:deep(.el-table th div) {
  padding-left: 0px;
  padding-right: 0px;
  padding-top: 0px;
  padding-bottom: 0px;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.nav_header {
  font-size: 14px;
}

.nav_data{
  padding-left: 5px;
  width: 195px;
}
/* .nav_content span{
  font-size: 14px;
} */
.carousel-container {
  width: 100%;
  max-width: 100%;
}
.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover; 
}
</style>
