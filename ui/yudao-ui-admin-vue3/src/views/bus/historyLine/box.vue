<template>
  <CommonMenu :dataList="navList" @node-click="handleClick" navTitle="插接箱电力分析" :showCheckbox="false">
    <template #NavInfo>
      <br/>    <br/> 
      <div class="nav_data">
        <!-- <div class="carousel-container"> -->
          <!-- <el-carousel :interval="2500" motion-blur height="150px" arrow="never" trigger="click">
            <el-carousel-item v-for="(item, index) in carouselItems" :key="index">
              <img width="auto" height="auto" :src="item.imgUrl" alt="" class="carousel-image" />
            </el-carousel-item>
          </el-carousel> -->
        <!-- </div>  -->
        <!-- <div class="nav_header">
          <span v-if="nowAddress">{{nowAddress}}</span>
          <span v-if="nowLocation">( {{nowLocation}} ) </span>
          <br/>
          <template v-if="queryParams.granularity == 'realtime' && queryParams.type == 'total' && queryParams.timeRange != null">
            <span>{{queryParams.timeRange[0]}}</span>
            <span>至</span>
            <span>{{queryParams.timeRange[1]}}</span>
          </template>
          <br/>
        </div> -->
        <!-- <div class="nav_content" v-if="queryParams.granularity == 'realtime' && queryParams.type == 'total'">
        <el-descriptions title="" direction="vertical" :column="1" border >
          <el-descriptions-item label="有功功率最大值(kW) | 发生时间">
            <span>{{ formatNumber(maxActivePowDataTemp, 3) }} kWh</span> <br/>
            <span v-if="maxActivePowDataTimeTemp">{{ maxActivePowDataTimeTemp }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="有功功率最小值 | 发生时间">
            <span>{{ formatNumber(minActivePowDataTemp, 3) }} kWh</span><br/>
            <span v-if="minActivePowDataTimeTemp">{{ minActivePowDataTimeTemp }}</span>
          </el-descriptions-item>
        </el-descriptions>
        </div> -->

        
        <div class="nav_header" style="font-size: 14px;">
          <span v-if="nowAddress">{{nowAddress}}</span>
          <span v-if="nowLocation">( {{nowLocation}} ) </span>
          <br/>
      </div>
      
        <div  class="descriptions-container" v-if="maxActivePowDataTimeTemp" style="font-size: 14px;">
          <div  class="description-item">
            <span class="label">最大值 :</span>
            <span >{{ formatNumber(maxActivePowDataTemp, 3) }} kW</span>
          </div>
          <div v-if="maxActivePowDataTimeTemp" class="description-item">
            <span class="label">发生时间 :</span>
            <span class="value">{{ maxActivePowDataTimeTemp }}</span>
          </div>
          <br/>
          <div  class="description-item">
            <span class="label">最小值 :</span>
            <span >{{ formatNumber(minActivePowDataTemp, 3) }} kW</span>
          </div>
          <div v-if="minActivePowDataTimeTemp" class="description-item">
            <span class="label">发生时间 :</span>
            <span class="value">{{ minActivePowDataTimeTemp }}</span>
          </div>
          <div style="text-align: center">
              <div class="line" style="margin-top: 10px;"></div>
            </div>
        </div>
      </div>
    </template> 
    <template #ActionBar>
      <el-tabs v-model="activeName">
        <el-tab-pane label="原始数据" name="realtimeTabPane"/>
        <el-tab-pane label="小时极值数据" name="hourExtremumTabPane"/>
        <el-tab-pane label="天极值数据" name="dayExtremumTabPane"/>
      </el-tabs>
      <!-- 搜索工作栏 -->
      <el-form
        class="-mb-5px"
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        label-width="auto"
      >
      <el-form-item label="参数类型" prop="type">
        <el-cascader
          v-model="defaultSelected"
          collapse-tags
          :options="typeSelection"
          collapse-tags-tooltip
          :show-all-levels="true"
          @change="typeCascaderChange"
          class="!w-110px"
        />
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
          <el-button type="success" plain @click="handleExport1" :loading="exportLoading">
             <Icon icon="ep:download" class="mr-5px" /> 导出
          </el-button>
        </el-form-item>

      </el-form>
    </template>
    <template #Content>
      <div v-loading="loading">
        <el-tabs v-model="activeName1" v-if="loading2">
          <el-tab-pane label="图表" name="myChart">
            <div ref="chartContainer" id="chartContainer" style="width: 70vw; height: 65vh;"></div>
          </el-tab-pane>
          <el-tab-pane label="数据" name="myData">
            <div style="height: 67vh;">
            <el-table  
            :border="true"
              :stripe="true"
              :data="tableData"
              style="height: 100%; width: 99.97%;"
              :header-cell-style="{ backgroundColor: '#F5F7FA', color: '#909399', textAlign: 'center', borderLeft: '1px #EDEEF2 solid', borderBottom: '1px #EDEEF2 solid', fontFamily: 'Microsoft YaHei',fontWeight: 'bold'}"
              :cell-style="{ color: '#606266', fontSize: '14px', textAlign: 'center', borderBottom: '0.25px #F5F7FA solid', borderLeft: '0.25px #F5F7FA solid' }"
              :row-style="{ fontSize: '14px', textAlign: 'center', }"
              empty-text="暂无数据" max-height="818">
              <!-- 添加行号列 -->
              <el-table-column label="序号" align="center" width="100px">
                <template #default="{ $index }">
                  {{ $index + 1 + (queryParams.pageNo - 1) * queryParams.pageSize }}
                </template>
              </el-table-column>
              <el-table-column prop="create_time" label="记录时间" />
              <!-- 动态生成表头 -->
              <template v-for="item in headerData" :key="item.name">
                <el-table-column v-if="item.name === '最大有功功率(kW)'" label="有功功率最大值">
                  <el-table-column :prop="item.name" label="有功功率最大值(kW)" width="120%"/>  
                  <el-table-column prop="activePowMaxTimeData" label="发生时间"  width="110%"/>
                </el-table-column>
                <el-table-column v-else-if="item.name === '最小有功功率(kW)'" label="有功功率最小值">
                  <el-table-column :prop="item.name" label="有功功率最小值(kW)" width="120%"/>  
                  <el-table-column prop="activePowMinTimeData" label="发生时间" width="110%"/>
                </el-table-column>

                <el-table-column v-else-if="item.name === '最大无功功率(kVar)'" label="无功功率最大值">
                  <el-table-column :prop="item.name" label="无功功率最大值(kVar)" width="130%"/>  
                  <el-table-column prop="reactivePowMaxTimeData" label="发生时间" width="110%"/>
                </el-table-column>
                <el-table-column v-else-if="item.name === '最小无功功率(kVar)'" label="无功功率最小值">
                  <el-table-column :prop="item.name" label="无功功率最小值(kVar)" width="130%"/>  
                  <el-table-column prop="reactivePowMinTimeData" label="发生时间" width="110%"/>
                </el-table-column>

                <el-table-column v-else-if="item.name === '最大电流谐波含量'" label="电流谐波含量最大值">
                  <el-table-column :prop="item.name" label="电流谐波含量最大值"/>  
                  <el-table-column prop="curThdMaxTimeData" label="发生时间" width="110%"/>
                </el-table-column>
                <el-table-column v-else-if="item.name === '最小电流谐波含量'" label="电流谐波含量最小值">
                  <el-table-column :prop="item.name" label="电流谐波含量最小值"/>  
                  <el-table-column prop="curThdMinTimeData" label="发生时间" width="110%"/>
                </el-table-column>

                <el-table-column v-else-if="item.name === '最大视在功率(kVA)'" label="视在功率最大值">
                  <el-table-column :prop="item.name" label="视在功率最大值(kVA)" width="120%"/>  
                  <el-table-column prop="apparentPowMaxTimeData" label="发生时间" width="110%"/>
                </el-table-column>
                 <el-table-column v-else-if="item.name === '最小视在功率(kVA)'" label="视在功率最小值">
                  <el-table-column :prop="item.name" label="视在功率最小值(kVA)" width="120%"/>  
                  <el-table-column prop="apparentPowMinTimeData" label="发生时间" width="110%"/>
                </el-table-column>
                <el-table-column v-else-if="item.name === '最大电压(V)'" label="电压最大值">
                  <el-table-column :prop="item.name" label="电压最大值(V)" width="120%"/>  
                  <el-table-column prop="volMaxTimeData" label="发生时间" width="110%"/>
                </el-table-column>
                 <el-table-column v-else-if="item.name === '最小电压(V)'" label="电压最小值">
                  <el-table-column :prop="item.name" label="电压最小值(V)" width="120%"/>  
                  <el-table-column prop="volMinTimeData" label="发生时间" width="110%"/>
                </el-table-column>
                <el-table-column v-else-if="item.name === '最大线电压'" label="线电压最大值">
                  <el-table-column :prop="item.name" label="线电压最大值(V)" width="120%"/>  
                  <el-table-column prop="volLineMaxTimeData" label="发生时间" width="110%"/>
                </el-table-column>
                 <el-table-column v-else-if="item.name === '最小线电压'" label="线电压最小值">
                  <el-table-column :prop="item.name" label="线电压最小值(V)" width="120%"/>  
                  <el-table-column prop="volLineMinTimeData" label="发生时间" width="110%"/>
                </el-table-column>

                <el-table-column v-else-if="item.name === '最大电流(A)'" label="电流最大值">
                  <el-table-column :prop="item.name" label="电流最大值(A)" width="120%"/>  
                  <el-table-column prop="curMaxTimeData" label="发生时间" width="110%"/>
                </el-table-column>
                 <el-table-column v-else-if="item.name === '最小电流(A)'" label="电流最小值">
                  <el-table-column :prop="item.name" label="电流最小值(A)" width="120%"/>  
                  <el-table-column prop="curMinTimeData" label="发生时间" width="110%"/>
                </el-table-column>
                <el-table-column v-else :prop="item.name" :label="item.name"  width="120%"/>   
              </template>
            </el-table>
            </div>
          </el-tab-pane>
        </el-tabs>
        
        <!-- <el-empty v-show="!isHaveData" description="暂无数据" /> -->
      </div>
    </template>
  </CommonMenu>
</template>

<script setup lang="ts">
import * as echarts from 'echarts';
import { onMounted } from 'vue'
import { HistoryDataApi } from '@/api/bus/historydata'
import { formatDate} from '@/utils/formatTime'
import { IndexApi } from '@/api/bus/busindex'
import download from '@/utils/download'
import { ElMessage } from 'element-plus'

defineOptions({ name: 'PDUHistoryLine' })

const activeName = ref('realtimeTabPane') // tab默认显示
const activeName1 = ref('myChart') // tab默认显示
const navList = ref([]) as any // 左侧导航栏树结构列表
const message = useMessage() // 消息弹窗
const nowAddress = ref('')// 导航栏的位置信息
const nowLocation = ref('')// 导航栏的位置信息
const nowAddressTemp = ref('')// 暂时存储点击导航栏的位置信息 确认有数据再显示
const nowLocationTemp = ref('')// 暂时存储点击导航栏的位置信息 确认有数据再显示
const instance = getCurrentInstance();
const tableData = ref<Array<{ }>>([]); // 列表数据
const headerData = ref<any[]>([]);
const needFlush = ref(0) // 是否需要刷新图表
const loading = ref(false) // 加载中
const exportLoading = ref(false)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 15,
  boxId: undefined as number | undefined,
  lineId: undefined,
  loopId: undefined,
  outletId: undefined,
  nowAddress: undefined as string | undefined,
  type: 'total',
  granularity: 'realtime',
  // 进入页面原始数据默认显示最近一小时
  timeRange: defaultHourTimeRange(1) as any,
  devkey: undefined as string | undefined,
})

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
  {
    text: '最近三天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 72)
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
    text: '最近一周',
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
      start.setHours(start.getHours() - 24*30)
      return [start, end]
    },
  },
  {
    text: '最近三个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 24*30*3)
      return [start, end]
    },
  },
  {
    text: '最近六个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 24*30*6)
      return [start, end]
    },
  },
]

// 参数类型选项
const defaultSelected = ref(['total'])
const typeSelection = ref([]) as any;

// 参数类型改变触发
const typeCascaderChange = (selected) => {
  queryParams.type = selected[0];
  switch(selected[0]){
    case 'line':
      queryParams.lineId = selected[1];
      queryParams.loopId = undefined;
      queryParams.outletId = undefined;
      break;
    case 'loop':
      queryParams.loopId = selected[1];
      queryParams.lineId = undefined;
      queryParams.outletId = undefined;
      break;
    case 'outlet':
      queryParams.outletId = selected[1];
      queryParams.loopId = undefined;
      queryParams.lineId = undefined;
      break;
    case 'total':
      queryParams.lineId = undefined;
      queryParams.loopId = undefined;
      queryParams.outletId = undefined;
      break;
  }
  needFlush.value ++

}

// 处理折线图数据
const createTimeData = ref<string[]>([]);
const powActiveData = ref<number[]>([]);
const powApparentData = ref<number[]>([]);
const powReactiveData = ref<number[]>([]);
const powerFactorData = ref<number[]>([]);
const curValueData = ref<number[]>([]);
const volValueData = ref<number[]>([]);
const curThdData = ref<number[]>([]);
const loadRateData = ref<number[]>([]);

const powActiveAvgValueData = ref<number[]>([]);
const powActiveMaxValueData = ref<number[]>([]);
const powActiveMaxTimeData = ref<string[]>([]);
const powActiveMinValueData = ref<number[]>([]);
const powActiveMinTimeData = ref<string[]>([]);

const powReactiveAvgValueData = ref<number[]>([]);
const powReactiveMaxValueData = ref<number[]>([]);
const powReactiveMaxTimeData = ref<string[]>([]);
const powReactiveMinValueData = ref<number[]>([]);
const powReactiveMinTimeData = ref<string[]>([]);

const powApparentAvgValueData = ref<number[]>([]);
const powApparentMaxValueData = ref<number[]>([]);
const powApparentMaxTimeData = ref<string[]>([]);
const powApparentMinValueData = ref<number[]>([]);
const powApparentMinTimeData = ref<string[]>([]);

const curValue= ref<number[]>([]);
const curAvgValueData = ref<number[]>([]);
const curMaxValueData = ref<number[]>([]);
const curMaxTimeData = ref<string[]>([]);
const curMinValueData = ref<number[]>([]);
const curMinTimeData = ref<string[]>([]);

const volValue= ref<number[]>([]);
const volAvgValueData = ref<number[]>([]);
const volMaxValueData = ref<number[]>([]);
const volMaxTimeData = ref<string[]>([]);
const volMinValueData = ref<number[]>([]);
const volMinTimeData = ref<string[]>([]);

const curThdAvgValueData = ref<number[]>([]);
const curThdMaxValueData = ref<number[]>([]);
const curThdMaxTimeData = ref<string[]>([]);
const curThdMinValueData = ref<number[]>([]);
const curThdMinTimeData = ref<string[]>([]);

// 侧边栏显示需要
const maxActivePowDataTemp = ref(0);// 最大有功功率(kW) 
const maxActivePowDataTimeTemp = ref();// 最大有功功率的发生时间 
const minActivePowDataTemp = ref(0);// 最小有功功率(kW) 
const minActivePowDataTimeTemp = ref();// 最小有功功率的发生时间 
const loading2=ref(false);

/** 查询列表 */
const isHaveData = ref(false);
const getList = async () => {
  loading.value = true;
  try {
    const data = await HistoryDataApi.getBoxHistoryDataDetails(queryParams);
    if (data != null && data.total != 0){
      loading2.value=true;
      isHaveData.value = true
      curValue.value = data.list.map((item) => formatNumber(item.cur_value, 2));
      volValue.value = data.list.map((item) => formatNumber(item.vol_value, 1));
      powActiveData.value = data.list.map((item) => formatNumber(item.pow_active, 3));
      powReactiveData.value = data.list.map((item) => formatNumber(item.pow_reactive, 3));
      powApparentData.value = data.list.map((item) => formatNumber(item.pow_apparent, 3));
      powerFactorData.value = data.list.map((item) => formatNumber(item.power_factor, 2));
      if (activeName.value === 'dayExtremumTabPane'){
        createTimeData.value = data.list.map((item) => formatDate(item.create_time, 'YYYY-MM-DD'));
      }else{
        createTimeData.value = data.list.map((item) => formatDate(item.create_time,"YYYY-MM-DD HH:mm"));
      }
      loadRateData.value = data.list.map((item) => formatNumber(item.load_rate, 2));
      curThdData.value = data.list.map((item) => formatNumber(item.cur_thd, 1));
      curValueData.value = data.list.map((item) => formatNumber(item.cur_value, 2));
      volValueData.value = data.list.map((item) => formatNumber(item.vol_value, 1));

      curAvgValueData.value = data.list.map((item) => formatNumber(item.cur_avg_value, 2));
      curMaxValueData.value = data.list.map((item) => formatNumber(item.cur_max_value, 2));
      curMaxTimeData.value = data.list.map((item) => formatDate(item.cur_max_time,"YYYY-MM-DD HH:mm"));
      curMinValueData.value = data.list.map((item) => formatNumber(item.cur_min_value, 2));
      curMinTimeData.value = data.list.map((item) => formatDate(item.cur_min_time,"YYYY-MM-DD HH:mm"));

      volAvgValueData.value = data.list.map((item) => formatNumber(item.vol_avg_value, 1));
      volMaxValueData.value = data.list.map((item) => formatNumber(item.vol_max_value, 1));
      volMaxTimeData.value = data.list.map((item) => formatDate(item.vol_max_time,"YYYY-MM-DD HH:mm"));
      volMinValueData.value = data.list.map((item) => formatNumber(item.vol_min_value, 1));
      volMinTimeData.value = data.list.map((item) => formatDate(item.vol_min_time,"YYYY-MM-DD HH:mm"));

      curThdAvgValueData.value = data.list.map((item) => formatNumber(item.cur_thd_avg_value, 1));
      curThdMaxValueData.value = data.list.map((item) => formatNumber(item.cur_thd_max_value, 1));
      curThdMaxTimeData.value = data.list.map((item) => formatDate(item.cur_thd_max_time,"YYYY-MM-DD HH:mm"));
      curThdMinValueData.value = data.list.map((item) => formatNumber(item.cur_thd_min_value, 1));
      curThdMinTimeData.value = data.list.map((item) => formatDate(item.cur_thd_min_time,"YYYY-MM-DD HH:mm"));

      powActiveAvgValueData.value = data.list.map((item) => formatNumber(item.pow_active_avg_value, 3));
      powActiveMaxValueData.value = data.list.map((item) => formatNumber(item.pow_active_max_value, 3));
      powActiveMaxTimeData.value = data.list.map((item) => formatDate(item.pow_active_max_time,"YYYY-MM-DD HH:mm"));
      powActiveMinValueData.value = data.list.map((item) => formatNumber(item.pow_active_min_value, 3));
      powActiveMinTimeData.value = data.list.map((item) => formatDate(item.pow_active_min_time,"YYYY-MM-DD HH:mm"));

      powReactiveAvgValueData.value = data.list.map((item) => formatNumber(item.pow_reactive_avg_value, 3));
      powReactiveMaxValueData.value = data.list.map((item) => formatNumber(item.pow_reactive_max_value, 3));
      powReactiveMaxTimeData.value = data.list.map((item) => formatDate(item.pow_reactive_max_time,"YYYY-MM-DD HH:mm"));
      powReactiveMinValueData.value = data.list.map((item) => formatNumber(item.pow_reactive_min_value, 3));
      powReactiveMinTimeData.value = data.list.map((item) => formatDate(item.pow_reactive_min_time,"YYYY-MM-DD HH:mm"));

      powApparentAvgValueData.value = data.list.map((item) => formatNumber(item.pow_apparent_avg_value, 3));
      powApparentMaxValueData.value = data.list.map((item) => formatNumber(item.pow_apparent_max_value, 3));
      powApparentMaxTimeData.value = data.list.map((item) => formatDate(item.pow_apparent_max_time,"YYYY-MM-DD HH:mm"));
      powApparentMinValueData.value = data.list.map((item) => formatNumber(item.pow_apparent_min_value, 3));
      powApparentMinTimeData.value = data.list.map((item) => formatDate(item.pow_apparent_min_time,"YYYY-MM-DD HH:mm"));

      maxActivePowDataTemp.value = Math.max(...powActiveData.value);
      minActivePowDataTemp.value = Math.min(...powActiveData.value);
      powActiveData.value.forEach(function(num, index) {
        if (num == maxActivePowDataTemp.value){
          maxActivePowDataTimeTemp.value = createTimeData.value[index]
        }
        if (num == minActivePowDataTemp.value){
          minActivePowDataTimeTemp.value = createTimeData.value[index]
        }
      });

      // 图表显示的位置变化
      nowAddress.value = nowAddressTemp.value
      nowLocation.value = nowLocationTemp.value
    }else{
      isHaveData.value = false;
      loading2.value=false;
      ElMessage({
        message: '暂无数据',
        type: 'warning',
      });
    }
  } finally {
    loading.value = false;

  }
}

//初始化折线图
const chartContainer = ref<HTMLElement | null>(null);
let realtimeChart = null as echarts.ECharts | null; 
const initChart = () => {
  if ( isHaveData.value == true ){
    if (chartContainer.value && instance) {
      realtimeChart = echarts.init(chartContainer.value);
      if (realtimeChart) {
        realtimeChart.setOption({
          title: { text: ''},
          tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
          legend: { data: ['总有功功率(kW)', '总视在功率(kVA)', '总无功功率(kVar)', '功率因素'],
                selected: {  "总有功功率(kW)": true, "总视在功率(kVA)": false, "总无功功率(kVar)": false, "功率因素": false, }
              },
          grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
          toolbox: {feature: {  restore:{}, saveAsImage: {}}},
          xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
          yAxis: { type: 'value'},
          series: [
            {name: '总有功功率(kW)', type: 'line', symbol: 'none', data: powActiveData.value},
            {name: '总无功功率(kVar)', type: 'line', symbol: 'none', data: powReactiveData.value},
            {name: '总视在功率(kVA)', type: 'line', symbol: 'none', data: powApparentData.value},
            {name: '功率因素', type: 'line', symbol: 'none', data: powerFactorData.value},
         
        ],
          dataZoom:[{type: "inside"}],
        });
      }
      // 图例切换监听
      totalRealtimeLegendListener(realtimeChart);
      // 将 realtimeChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
      instance.appContext.config.globalProperties.realtimeChart = realtimeChart;
    }
  }
  // 每次切换图就要动态生成数据表头
  headerData.value = realtimeChart?.getOption().series as any[];
  updateTableData();
};

// 表格映射图数据
const updateTableData = () => {
  const data: any[] = [];
  const length = headerData.value[0]?.data?.length || 0;
  for (let i = 0; i < length; i++) {
    const rowData: { [key: string]: any } = {};
    rowData['create_time'] = createTimeData.value[i];
    rowData['curMaxTimeData'] = curMaxTimeData.value[i];
    rowData['curMinTimeData'] = curMinTimeData.value[i];
    rowData['volMaxTimeData'] = volMaxTimeData.value[i];
    rowData['volMinTimeData'] = volMinTimeData.value[i];
    rowData['curThdMaxTimeData'] = curThdMaxTimeData.value[i];
    rowData['curThdMinTimeData'] = curThdMinTimeData.value[i];

    rowData['activePowMaxTimeData'] = powActiveMaxTimeData.value[i];
    rowData['activePowMinTimeData'] = powActiveMinTimeData.value[i];
    rowData['reactivePowMaxTimeData'] = powReactiveMaxTimeData.value[i];
    rowData['reactivePowMinTimeData'] = powReactiveMinTimeData.value[i];
    rowData['apparentPowMaxTimeData'] = powApparentMaxTimeData.value[i];
    rowData['apparentPowMinTimeData'] = powApparentMinTimeData.value[i];

    for (const item of headerData.value) {
      rowData[item.name] = item.data[i];
    }
    data.push(rowData);
  }
  tableData.value = data;
};

// 在组件销毁时手动销毁图表
const beforeUnmount = () => {
  realtimeChart?.dispose();
};
// 折线图随着窗口大小改变
window.addEventListener('resize', function() {
  realtimeChart?.resize(); 
});

// 监听切换原始数据、极值数据tab
watch( ()=>activeName.value, async(newActiveName)=>{
  if ( newActiveName == 'realtimeTabPane'){
    queryParams.granularity = 'realtime'
    queryParams.timeRange = defaultHourTimeRange(50)
  }else if (newActiveName == 'hourExtremumTabPane'){
    queryParams.granularity = 'hour'
    queryParams.timeRange = defaultHourTimeRange(100)
  }else{
    queryParams.granularity = 'day'
    queryParams.timeRange = defaultHourTimeRange(24*30)
  }
  needFlush.value ++;
});

// 监听类型颗粒度
watch(() => [activeName.value, queryParams.type, needFlush.value], async (newValues) => {
    
    const [newActiveName, newType] = newValues;
          console.log(newType)
    // 处理参数变化
    if (newType == 'total'){
      if ( newActiveName == 'realtimeTabPane'){
        await getList();
        // 销毁原有的图表实例
        beforeUnmount()
        if ( isHaveData.value == true ){
          // 创建新的图表实例
          realtimeChart = echarts.init(document.getElementById('chartContainer'));
          // 设置新的配置对象
          if (realtimeChart) {
            realtimeChart.setOption({
              title: { text: ''},
              tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
              legend: { data: ['总有功功率(kW)', '总视在功率(kVA)', '总无功功率(kVar)', '功率因素'],
                    selected: {  "总有功功率(kW)": true, "总视在功率(kVA)": true, "总无功功率(kVar)": true, "功率因素": false, }
                  },
              grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
              toolbox: {feature: {  restore:{}, saveAsImage: {}}},
              xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
              yAxis: { type: 'value'},
              series: [
                {name: '总有功功率(kW)', type: 'line', symbol: 'none', data: powActiveData.value},
                {name: '总无功功率(kVar)', type: 'line', symbol: 'none', data: powReactiveData.value},
                {name: '总视在功率(kVA)', type: 'line', symbol: 'none', data: powApparentData.value},
                {name: '功率因素', type: 'line', symbol: 'none', data: powerFactorData.value},
            
            ],
              dataZoom:[{type: "inside"}],
            });
          }
            // 图例切换监听
            totalRealtimeLegendListener(realtimeChart);
        }
         // 每次切换图就要动态生成数据表头
        headerData.value = realtimeChart?.getOption().series as any[];
        updateTableData();
      }else{
        await getList();
        // 销毁原有的图表实例
        beforeUnmount()
        if ( isHaveData.value == true ){
          // 创建新的图表实例
          realtimeChart = echarts.init(document.getElementById('chartContainer'));
          // 设置新的配置对象
          if (realtimeChart) {
            realtimeChart.setOption({     
              title: {text: ''},
              tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
              legend: { data: ['平均有功功率(kW)', '最大有功功率(kW)', '最小有功功率(kW)','平均无功功率(kVar)', '最大无功功率(kVar)', '最小无功功率(kVar)','平均视在功率(kVA)', '最大视在功率(kVA)', '最小视在功率(kVA)' ],
                        selected: { "平均有功功率(kW)": true, "最大有功功率(kW)": false, "最小有功功率(kW)": false, "平均无功功率(kVar)": true, "最大无功功率(kVar)": false, "最小无功功率(kVar)": false,
                                  "平均视在功率(kVA)": true, "最大视在功率(kVA)": false, "最小视在功率(kVA)": false }
                      },
              grid: {left: '3%', right: '4%', bottom: '3%', containLabel: true },
              toolbox: {feature: {  restore:{}, saveAsImage: {}}},
              xAxis: [
                {type: 'category', boundaryGap: false, data: createTimeData.value}
              ],
              yAxis: { type: 'value'},
              series: [
                { name: '平均有功功率(kW)', type: 'line', symbol: 'none', data: powActiveAvgValueData.value, },
                { name: '最大有功功率(kW)', type: 'line', symbol: 'none', data: powActiveMaxValueData.value, lineStyle: {type: 'dashed'}},
                { name: '最小有功功率(kW)', type: 'line', symbol: 'none', data: powActiveMinValueData.value, lineStyle: {type: 'dashed'}},
                { name: '平均无功功率(kVar)', type: 'line', symbol: 'none', data: powReactiveAvgValueData.value, },
                { name: '最大无功功率(kVar)', type: 'line', symbol: 'none', data: powReactiveMaxValueData.value, lineStyle: {type: 'dashed'}},
                { name: '最小无功功率(kVar)', type: 'line', symbol: 'none', data: powReactiveMinValueData.value, lineStyle: {type: 'dashed'}},
                { name: '平均视在功率(kVA)', type: 'line', symbol: 'none', data: powApparentAvgValueData.value, },
                { name: '最大视在功率(kVA)', type: 'line', symbol: 'none', data: powApparentMaxValueData.value, lineStyle: {type: 'dashed'}},
                { name: '最小视在功率(kVA)', type: 'line', symbol: 'none', data: powApparentMinValueData.value, lineStyle: {type: 'dashed'}},
              ],
              dataZoom:[{type: "inside"}],
            });
          }
            // 图例切换监听
            totalAndOutletHourAndDayLegendListener(realtimeChart);
        }
        // 每次切换图就要动态生成数据表头
        headerData.value = realtimeChart?.getOption().series as any[];
        updateTableData();
      }
    }
    
    if (newType == 'line'){
      if ( newActiveName == 'realtimeTabPane'){
          await getList();
          // 销毁原有的图表实例
          beforeUnmount()
          if ( isHaveData.value == true ){
            // 创建新的图表实例
            realtimeChart = echarts.init(document.getElementById('chartContainer'));
            // 设置新的配置对象
            if (realtimeChart) {
              realtimeChart.setOption({
                // 这里设置 Echarts 的配置项和数据
                title: { text: ''},
                tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
                legend: { data:  ['有功功率(kW)', '视在功率(kVA)', '无功功率(kVar)', '功率因素', '电压(V)', '电流(A)', '负载率', '电流谐波含量'],
                          selected: {  "有功功率(kW)": false, "视在功率(kVA)": false, "无功功率(kVar)": false, "功率因素": false
                            , "电压(V)": false, "电流(A)": true, "负载率": false, "电流谐波含量": false
                           }},
                grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
                toolbox: {feature: {  restore:{}, saveAsImage: {}}},
                xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
                yAxis: { type: 'value'},
                series: [
                  {name: '有功功率(kW)', type: 'line', symbol: 'none', data: powActiveData.value},
                  {name: '无功功率(kVar)', type: 'line', symbol: 'none', data: powReactiveData.value},
                  {name: '功率因素', type: 'line', symbol: 'none', data: powerFactorData.value},
                  {name: '视在功率(kVA)', type: 'line', symbol: 'none', data: powApparentData.value},
                  {name: '电压(V)', type: 'line', symbol: 'none', data: volValue.value},
                  {name: '电流(A)', type: 'line', symbol: 'none', data: curValue.value},
                  {name: '负载率', type: 'line', symbol: 'none', data: loadRateData.value},
                  {name: '电流谐波含量', type: 'line', symbol: 'none', data: curThdData.value},
                ],
                dataZoom:[{type: "inside"}],
              });
            }
            // 图例切换监听
            lineRealtimeLegendListener(realtimeChart);
          }
          // 每次切换图就要动态生成数据表头
          headerData.value = realtimeChart?.getOption().series as any[];
          updateTableData();
      }else{
        await getList();
        // 销毁原有的图表实例
        beforeUnmount()
        if ( isHaveData.value == true ){
          // 创建新的图表实例
          realtimeChart = echarts.init(document.getElementById('chartContainer'));
          // 设置新的配置对象
          if (realtimeChart) {
            realtimeChart.setOption( {
            title: {text: ''},
            tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
            legend: { data: [ '平均有功功率(kW)', '最大有功功率(kW)', '最小有功功率(kW)','平均无功功率(kVar)', '最大无功功率(kVar)', '最小无功功率(kVar)','平均视在功率(kVA)', '最大视在功率(kVA)', '最小视在功率(kVA)',
                               '平均电流(A)', '最大电流(A)', '最小电流(A)','平均电压(V)', '最大电压(V)', '最小电压(V)','平均电流谐波含量', '最大电流谐波含量', '最小电流谐波含量'],
                      selected: {  "平均有功功率(kW)": false, "最大有功功率(kW)": false, "最小有功功率(kW)": false, "平均无功功率(kVar)": false, "最大无功功率(kVar)": false, "最小无功功率(kVar)": false,
                                  "平均视在功率(kVA)": false, "最大视在功率(kVA)": false, "最小视在功率(kVA)": false, "平均电流(A)": true, "最大电流(A)": true, "最小电流(A)": true, 
                                  "平均电压(V)": false, "最大电压(V)": false, "最小电压(V)": false, "平均电流谐波含量": false, "最大电流谐波含量": false, "最小电流谐波含量": false }
                    },
            grid: {left: '3%', right: '4%',bottom: '3%', containLabel: true },
            toolbox: {feature: {  restore:{}, saveAsImage: {}}},
            xAxis: [
              {type: 'category', boundaryGap: false, data: createTimeData.value},
            ],
            yAxis: { type: 'value'},
            series: [
              { name: '平均电流(A)', type: 'line', symbol: 'none', data: curAvgValueData.value, },
              { name: '最大电流(A)', type: 'line', symbol: 'none', data: curMaxValueData.value, lineStyle: {type: 'dashed'}},
              { name: '最小电流(A)', type: 'line', symbol: 'none', data: curMinValueData.value, lineStyle: {type: 'dashed'}},
              { name: '平均电压(V)', type: 'line', symbol: 'none', data: volAvgValueData.value, },
              { name: '最大电压(V)', type: 'line', symbol: 'none', data: volMaxValueData.value, lineStyle: {type: 'dashed'}},
              { name: '最小电压(V)', type: 'line', symbol: 'none', data: volMinValueData.value, lineStyle: {type: 'dashed'}},
              { name: '平均电流谐波含量', type: 'line', symbol: 'none', data: curThdAvgValueData.value, },
              { name: '最大电流谐波含量', type: 'line', symbol: 'none', data: curThdMaxValueData.value, lineStyle: {type: 'dashed'}},
              { name: '最小电流谐波含量', type: 'line', symbol: 'none', data: curThdMinValueData.value, lineStyle: {type: 'dashed'}},

              { name: '平均有功功率(kW)', type: 'line', symbol: 'none', data: powActiveAvgValueData.value, },
              { name: '最大有功功率(kW)', type: 'line', symbol: 'none', data: powActiveMaxValueData.value, lineStyle: {type: 'dashed'}},
              { name: '最小有功功率(kW)', type: 'line', symbol: 'none', data: powActiveMinValueData.value, lineStyle: {type: 'dashed'}},
              { name: '平均无功功率(kVar)', type: 'line', symbol: 'none', data: powReactiveAvgValueData.value, },
              { name: '最大无功功率(kVar)', type: 'line', symbol: 'none', data: powReactiveMaxValueData.value, lineStyle: {type: 'dashed'}},
              { name: '最小无功功率(kVar)', type: 'line', symbol: 'none', data: powReactiveMinValueData.value, lineStyle: {type: 'dashed'}},
              { name: '平均视在功率(kVA)', type: 'line', symbol: 'none', data: powApparentAvgValueData.value, },
              { name: '最大视在功率(kVA)', type: 'line', symbol: 'none', data: powApparentMaxValueData.value, lineStyle: {type: 'dashed'}},
              { name: '最小视在功率(kVA)', type: 'line', symbol: 'none', data: powApparentMinValueData.value, lineStyle: {type: 'dashed'}},
            ],
            dataZoom:[{type: "inside"}],
            });
          }
          // 图例切换监听
          lineHourAndDayLegendListener(realtimeChart);
      }
        // 每次切换图就要动态生成数据表头
        headerData.value = realtimeChart?.getOption().series as any[];
        updateTableData();      
      }
    }

    if (newType == 'loop'){
      if ( newActiveName == 'realtimeTabPane'){
          await getList();
          // 销毁原有的图表实例
          beforeUnmount()
          if ( isHaveData.value == true ){
            // 创建新的图表实例
            realtimeChart = echarts.init(document.getElementById('chartContainer'));
            // 设置新的配置对象
            if (realtimeChart) {
              realtimeChart.setOption({
                // 这里设置 Echarts 的配置项和数据
                title: { text: ''},
                tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
                legend: { data:  ['有功功率(kW)', '视在功率(kVA)', '无功功率(kVar)', '功率因素', '电压(V)', '电流(A)'],
                          selected: {  "有功功率(kW)": false, "视在功率(kVA)": false, "无功功率(kVar)": false, "功率因素": false, "电压(V)": false, "电流(A)": true }},
                grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
                toolbox: {feature: {  restore:{}, saveAsImage: {}}},
                xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
                yAxis: { type: 'value'},
                series: [
                  {name: '有功功率(kW)', type: 'line', symbol: 'none', data: powActiveData.value},
                  {name: '无功功率(kVar)', type: 'line', symbol: 'none', data: powReactiveData.value},
                  {name: '功率因素', type: 'line', symbol: 'none', data: powerFactorData.value},
                  {name: '视在功率(kVA)', type: 'line', symbol: 'none', data: powApparentData.value},
                  {name: '电压(V)', type: 'line', symbol: 'none', data: volValueData.value},
                  {name: '电流(A)', type: 'line', symbol: 'none', data: curValueData.value},
                ],
                dataZoom:[{type: "inside"}],
              });
            }
            // 图例切换监听
            loopRealtimeLegendListener(realtimeChart);
          }
          // 每次切换图就要动态生成数据表头
          headerData.value = realtimeChart?.getOption().series as any[];
          updateTableData();
      }else{
        await getList();
        // 销毁原有的图表实例
        beforeUnmount()
        if ( isHaveData.value == true ){
          // 创建新的图表实例
          realtimeChart = echarts.init(document.getElementById('chartContainer'));
          // 设置新的配置对象
          if (realtimeChart) {
            realtimeChart.setOption( {
            title: {text: ''},
            tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
            legend: { data: [ '平均有功功率(kW)', '最大有功功率(kW)', '最小有功功率(kW)','平均无功功率(kVar)', '最大无功功率(kVar)', '最小无功功率(kVar)','平均视在功率(kVA)', '最大视在功率(kVA)', '最小视在功率(kVA)',
                               '平均电流(A)', '最大电流(A)', '最小电流(A)','平均电压(V)', '最大电压(V)', '最小电压(V)'],
                      selected: {  "平均有功功率(kW)": false, "最大有功功率(kW)": false, "最小有功功率(kW)": false, "平均无功功率(kVar)": false, "最大无功功率(kVar)": false, "最小无功功率(kVar)": false,
                                  "平均视在功率(kVA)": false, "最大视在功率(kVA)": false, "最小视在功率(kVA)": false, "平均电流(A)": true, "最大电流(A)": true, "最小电流(A)": true, 
                                 "平均电压(V)": false, "最大电压(V)": false, "最小电压(V)": false }
                    },
            grid: {left: '3%', right: '4%',bottom: '3%', containLabel: true },
            toolbox: {feature: {  restore:{}, saveAsImage: {}}},
            xAxis: [
              {type: 'category', boundaryGap: false, data: createTimeData.value},
            ],
            yAxis: { type: 'value'},
            series: [
              { name: '平均电流(A)', type: 'line', symbol: 'none', data: curAvgValueData.value, },
              { name: '最大电流(A)', type: 'line', symbol: 'none', data: curMaxValueData.value, lineStyle: {type: 'dashed'}},
              { name: '最小电流(A)', type: 'line', symbol: 'none', data: curMinValueData.value, lineStyle: {type: 'dashed'}},
              { name: '平均电压(V)', type: 'line', symbol: 'none', data: volAvgValueData.value, },
              { name: '最大电压(V)', type: 'line', symbol: 'none', data: volMaxValueData.value, lineStyle: {type: 'dashed'}},
              { name: '最小电压(V)', type: 'line', symbol: 'none', data: volMinValueData.value, lineStyle: {type: 'dashed'}},
        
              { name: '平均有功功率(kW)', type: 'line', symbol: 'none', data: powActiveAvgValueData.value, },
              { name: '最大有功功率(kW)', type: 'line', symbol: 'none', data: powActiveMaxValueData.value, lineStyle: {type: 'dashed'}},
              { name: '最小有功功率(kW)', type: 'line', symbol: 'none', data: powActiveMinValueData.value, lineStyle: {type: 'dashed'}},
              { name: '平均无功功率(kVar)', type: 'line', symbol: 'none', data: powReactiveAvgValueData.value, },
              { name: '最大无功功率(kVar)', type: 'line', symbol: 'none', data: powReactiveMaxValueData.value, lineStyle: {type: 'dashed'}},
              { name: '最小无功功率(kVar)', type: 'line', symbol: 'none', data: powReactiveMinValueData.value, lineStyle: {type: 'dashed'}},
              { name: '平均视在功率(kVA)', type: 'line', symbol: 'none', data: powApparentAvgValueData.value, },
              { name: '最大视在功率(kVA)', type: 'line', symbol: 'none', data: powApparentMaxValueData.value, lineStyle: {type: 'dashed'}},
              { name: '最小视在功率(kVA)', type: 'line', symbol: 'none', data: powApparentMinValueData.value, lineStyle: {type: 'dashed'}},
            ],
            dataZoom:[{type: "inside"}],
            });
          }
          // 图例切换监听
          loopHourAndDayLegendListener(realtimeChart);
      }
        // 每次切换图就要动态生成数据表头
        headerData.value = realtimeChart?.getOption().series as any[];
        updateTableData();      
      }
    }

    if (newType == 'outlet'){
      if ( newActiveName == 'realtimeTabPane'){
        await getList();
        // 销毁原有的图表实例
        beforeUnmount()
        if ( isHaveData.value == true ){
          // 创建新的图表实例
          realtimeChart = echarts.init(document.getElementById('chartContainer'));
          // 设置新的配置对象
          if (realtimeChart) {
            realtimeChart.setOption({
              title: { text: ''},
              tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
              legend: { data: ['有功功率(kW)', '视在功率(kVA)', '无功功率(kVar)', '功率因素'],
                    selected: {  "有功功率(kW)": true, "视在功率(kVA)": false, "无功功率(kVar)": false, "功率因素": false, }
                  },
              grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
              toolbox: {feature: {  restore:{}, saveAsImage: {}}},
              xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
              yAxis: { type: 'value'},
              series: [
                {name: '有功功率(kW)', type: 'line', symbol: 'none', data: powActiveData.value},
                {name: '无功功率(kVar)', type: 'line', symbol: 'none', data: powReactiveData.value},
                {name: '视在功率(kVA)', type: 'line', symbol: 'none', data: powApparentData.value},
                {name: '功率因素', type: 'line', symbol: 'none', data: powerFactorData.value},
            
            ],
              dataZoom:[{type: "inside"}],
            });
          }
            // 图例切换监听
            outletRealtimeLegendListener(realtimeChart);
        }
         // 每次切换图就要动态生成数据表头
        headerData.value = realtimeChart?.getOption().series as any[];
        updateTableData();
      }else{
        await getList();
        // 销毁原有的图表实例
        beforeUnmount()
        if ( isHaveData.value == true ){
          // 创建新的图表实例
          realtimeChart = echarts.init(document.getElementById('chartContainer'));
          // 设置新的配置对象
          if (realtimeChart) {
            realtimeChart.setOption({     
              title: {text: ''},
              tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
              legend: { data: ['平均有功功率(kW)', '最大有功功率(kW)', '最小有功功率(kW)','平均无功功率(kVar)', '最大无功功率(kVar)', '最小无功功率(kVar)','平均视在功率(kVA)', '最大视在功率(kVA)', '最小视在功率(kVA)' ],
                        selected: { "平均有功功率(kW)": true, "最大有功功率(kW)": false, "最小有功功率(kW)": false, "平均无功功率(kVar)": true, "最大无功功率(kVar)": false, "最小无功功率(kVar)": false,
                                  "平均视在功率(kVA)": true, "最大视在功率(kVA)": false, "最小视在功率(kVA)": false }
                      },
              grid: {left: '3%', right: '4%', bottom: '3%', containLabel: true },
              toolbox: {feature: {  restore:{}, saveAsImage: {}}},
              xAxis: [
                {type: 'category', boundaryGap: false, data: createTimeData.value}
              ],
              yAxis: { type: 'value'},
              series: [
                { name: '平均有功功率(kW)', type: 'line', symbol: 'none', data: powActiveAvgValueData.value, },
                { name: '最大有功功率(kW)', type: 'line', symbol: 'none', data: powActiveMaxValueData.value, lineStyle: {type: 'dashed'}},
                { name: '最小有功功率(kW)', type: 'line', symbol: 'none', data: powActiveMinValueData.value, lineStyle: {type: 'dashed'}},
                { name: '平均无功功率(kVar)', type: 'line', symbol: 'none', data: powReactiveAvgValueData.value, },
                { name: '最大无功功率(kVar)', type: 'line', symbol: 'none', data: powReactiveMaxValueData.value, lineStyle: {type: 'dashed'}},
                { name: '最小无功功率(kVar)', type: 'line', symbol: 'none', data: powReactiveMinValueData.value, lineStyle: {type: 'dashed'}},
                { name: '平均视在功率(kVA)', type: 'line', symbol: 'none', data: powApparentAvgValueData.value, },
                { name: '最大视在功率(kVA)', type: 'line', symbol: 'none', data: powApparentMaxValueData.value, lineStyle: {type: 'dashed'}},
                { name: '最小视在功率(kVA)', type: 'line', symbol: 'none', data: powApparentMinValueData.value, lineStyle: {type: 'dashed'}},
              ],
              dataZoom:[{type: "inside"}],
            });
          }
            // 图例切换监听
            totalAndOutletHourAndDayLegendListener(realtimeChart);
        }
        // 每次切换图就要动态生成数据表头
        headerData.value = realtimeChart?.getOption().series as any[];
        updateTableData();
      }
    }

});

// 总数据实时图例切换函数
function totalRealtimeLegendListener(realtimeChart) {
  realtimeChart?.on('legendselectchanged', function (params) {
    var legendName = params.name;
    var optionsToUpdate = {};
    switch (legendName) {
      case '总有功功率(kW)':
     if (params.selected[legendName]){
          optionsToUpdate = {  "总有功功率(kW)": true , "功率因素": false};
        }
        break;

      case '总视在功率(kVA)':
      if (params.selected[legendName]){
          optionsToUpdate = {  "总视在功率(kVA)": true , "功率因素": false};
      }
        break;

      case '总无功功率(kVar)':
      if (params.selected[legendName]){
          optionsToUpdate = {  "总无功功率(kVar)": true , "功率因素": false};
      }
        break;

      case '功率因素':
      if (params.selected[legendName]){
          optionsToUpdate = {  "功率因素": true , "总有功功率(kW)": false, "总无功功率(kVar)": false, "总视在功率(kVA)": false};
      }
        break;
  
      default:
        break;
    }

    realtimeChart?.setOption({
      legend: {
        data: ['总有功功率(kW)', '总视在功率(kVA)', '总无功功率(kVar)', '功率因素'],
        selected: optionsToUpdate
      },
    });
  });
}

// 总和输出位数据小时、天 图例切换函数
function totalAndOutletHourAndDayLegendListener(realtimeChart) {
  realtimeChart?.on('legendselectchanged', function (params) {
    var legendName = params.name;
    var optionsToUpdate = {};
    switch (legendName) {
      case '平均视在功率(kVA)':
      case '最大视在功率(kVA)':
      case '最小视在功率(kVA)':
      if (params.selected[legendName]){
        optionsToUpdate = { "平均视在功率(kVA)": true, "最大视在功率(kVA)": true, "最小视在功率(kVA)": true};
      }
        break;

      case '平均有功功率(kW)':
      case '最大有功功率(kW)':
      case '最小有功功率(kW)':
      if (params.selected[legendName]){
        optionsToUpdate = { "平均有功功率(kW)": true, "最大有功功率(kW)": true, "最小有功功率(kW)": true};
      }
        break;

      case '平均无功功率(kVar)':
      case '最大无功功率(kVar)':
      case '最小无功功率(kVar)':
      if (params.selected[legendName]){
        optionsToUpdate = { "平均无功功率(kVar)": true, "最大无功功率(kVar)": true, "最小无功功率(kVar)": true };
      }
        break;

      default:
        break;
    }

    realtimeChart?.setOption({
      legend: {
        data:  ['平均有功功率(kW)', '最大有功功率(kW)', '最小有功功率(kW)','平均无功功率(kVar)', '最大无功功率(kVar)', '最小无功功率(kVar)','平均视在功率(kVA)', '最大视在功率(kVA)', '最小视在功率(kVA)', 
                '平均剩余电流(A)', '最大剩余电流(A)', '最小剩余电流(A)', '平均零线电流(A)', '最大零线电流(A)', '最小零线电流(A)'],
        selected: optionsToUpdate
      },
    });
  });
}

// 相数据实时图例切换函数
function lineRealtimeLegendListener(realtimeChart) {
  realtimeChart?.on('legendselectchanged', function (params) {
    var legendName = params.name;
    var optionsToUpdate = {};
    switch (legendName) {
      case '有功功率(kW)':
     if (params.selected[legendName]){
          optionsToUpdate = { "有功功率(kW)": true,  "功率因素": false , "电压(V)": false, 
                            "电流(A)": false, "负载率": false, "电流谐波含量": false }
        }
        break;

      case '视在功率(kVA)':
      if (params.selected[legendName]){
          optionsToUpdate = { "视在功率(kVA)": true,  "功率因素": false , "电压(V)": false, 
                            "电流(A)": false, "负载率": false, "电流谐波含量": false }
      }
        break;

      case '无功功率(kVar)':
      if (params.selected[legendName]){
          optionsToUpdate = { "无功功率(kVar)": true,  "功率因素": false , "电压(V)": false, 
                            "电流(A)": false, "负载率": false, "电流谐波含量": false }
      }
        break;

      case '功率因素':
      if (params.selected[legendName]){
        optionsToUpdate = { "功率因素": true,  "有功功率(kW)": false,  "视在功率(kVA)": false,  
                           "无功功率(kVar)": false , "电压(V)": false, "电流(A)": false, "负载率": false, "电流谐波含量": false }
      }
        break;

      case '电流(A)':
      if (params.selected[legendName]){
        optionsToUpdate = { "电流(A)": true, "功率因素": false , "有功功率(kW)": false,  "视在功率(kVA)": false,  
                             "无功功率(kVar)": false , "电压(V)": false,  "负载率": false, "电流谐波含量": false }
      }
        break;

      case '电压(V)':
      if (params.selected[legendName]){
        optionsToUpdate = { "电压(V)": true, "功率因素": false , "有功功率(kW)": false, "电流(A)": false, 
                          "视在功率(kVA)": false,  "无功功率(kVar)": false, "负载率": false, "电流谐波含量": false}
      }
        break;

      case '负载率':
      if (params.selected[legendName]){
        optionsToUpdate = { "负载率": true, "电压(V)": false, "电流谐波含量": false, "功率因素": false ,"电流(A)": false,  
                            "有功功率(kW)": false,  "视在功率(kVA)": false,  "无功功率(kVar)": false}
      }
        break;

      case '电流谐波含量':
      if (params.selected[legendName]){
        optionsToUpdate = { "电流谐波含量": true, "电压(V)": false, "功率因素": false ,"电流(A)": false,  
                            "有功功率(kW)": false,  "视在功率(kVA)": false, "无功功率(kVar)": false, "负载率": false, }
      }
        break;
        
      default:
        break;
    }

    realtimeChart?.setOption({
      legend: {
        data: ['有功功率(kW)', '视在功率(kVA)', '无功功率(kVar)', '功率因素', '电压(V)', '电流(A)', '负载率', '电流谐波含量'],
        selected: optionsToUpdate
      },
    });
  });
}

// 相数据小时、天 图例切换函数
function lineHourAndDayLegendListener(realtimeChart) {
  realtimeChart?.on('legendselectchanged', function (params) {
    var legendName = params.name;
    var optionsToUpdate = {};
    switch (legendName) {
      case '平均视在功率(kVA)':
      case '最大视在功率(kVA)':
      case '最小视在功率(kVA)':
      if (params.selected[legendName]){
        optionsToUpdate = { "平均视在功率(kVA)": true, "最大视在功率(kVA)": true, "最小视在功率(kVA)": true, "平均电流(A)": false, "最大电流(A)": false, "最小电流(A)": false, 
                            "平均电压(V)": false, "最大电压(V)": false, "最小电压(V)": false, "平均电流谐波含量": false, "最大电流谐波含量": false, "最小电流谐波含量": false };
      }
        break;

      case '平均有功功率(kW)':
      case '最大有功功率(kW)':
      case '最小有功功率(kW)':
      if (params.selected[legendName]){
        optionsToUpdate = { "平均有功功率(kW)": true, "最大有功功率(kW)": true, "最小有功功率(kW)": true, "平均电流(A)": false, "最大电流(A)": false, "最小电流(A)": false, 
                          "平均电压(V)": false, "最大电压(V)": false, "最小电压(V)": false, "平均电流谐波含量": false, "最大电流谐波含量": false, "最小电流谐波含量": false };
      }
        break;

      case '平均无功功率(kVar)':
      case '最大无功功率(kVar)':
      case '最小无功功率(kVar)':
      if (params.selected[legendName]){
        optionsToUpdate = { "平均无功功率(kVar)": true, "最大无功功率(kVar)": true, "最小无功功率(kVar)": true, "平均电流(A)": false, "最大电流(A)": false, "最小电流(A)": false, 
                         "平均电压(V)": false, "最大电压(V)": false, "最小电压(V)": false, "平均电流谐波含量": false, "最大电流谐波含量": false, "最小电流谐波含量": false };
      }
        break;

      case '平均电流(A)':
      case '最大电流(A)':
      case '最小电流(A)':
      if (params.selected[legendName]){
        optionsToUpdate = { "平均电流(A)": true, "最大电流(A)": true, "最小电流(A)": true, "平均视在功率(kVA)": false, "最大视在功率(kVA)": false, "最小视在功率(kVA)": false,
                          "平均有功功率(kW)": false, "最大有功功率(kW)": false, "最小有功功率(kW)": false,"平均无功功率(kVar)": false, "最大无功功率(kVar)": false, "最小无功功率(kVar)": false,
                           "平均电压(V)": false, "最大电压(V)": false, "最小电压(V)": false, "平均电流谐波含量": false, "最大电流谐波含量": false, "最小电流谐波含量": false};
      }
        break;

      case '平均电压(V)':
      case '最大电压(V)':
      case '最小电压(V)':
      if (params.selected[legendName]){
        optionsToUpdate = { "平均电压(V)": true, "最大电压(V)": true, "最小电压(V)": true, "平均视在功率(kVA)": false, "最大视在功率(kVA)": false, "最小视在功率(kVA)": false,
                 "平均有功功率(kW)": false, "最大有功功率(kW)": false, "最小有功功率(kW)": false,"平均无功功率(kVar)": false, "最大无功功率(kVar)": false, "最小无功功率(kVar)": false,
                 "平均电流(A)": false, "最大电流(A)": false, "最小电流(A)": false, "平均电流谐波含量": false, "最大电流谐波含量": false, "最小电流谐波含量": false }
          }
        break;

      case '平均电流谐波含量':
      case '最大电流谐波含量':
      case '最小电流谐波含量':
      if (params.selected[legendName]){
        optionsToUpdate = { "平均电流谐波含量": true, "最大电流谐波含量": true, "最小电流谐波含量": true, "平均视在功率(kVA)": false, "最大视在功率(kVA)": false, "最小视在功率(kVA)": false,
                 "平均有功功率(kW)": false, "最大有功功率(kW)": false, "最小有功功率(kW)": false,"平均无功功率(kVar)": false, "最大无功功率(kVar)": false, "最小无功功率(kVar)": false,
                 "平均电流(A)": false, "最大电流(A)": false, "最小电流(A)": false, "平均电压(V)": false, "最大电压(V)": false, "最小电压(V)": false }
          }
        break;


      default:
        break;
    }

    realtimeChart?.setOption({
      legend: {
        data:  ['平均有功功率(kW)', '最大有功功率(kW)', '最小有功功率(kW)','平均无功功率(kVar)', '最大无功功率(kVar)', '最小无功功率(kVar)','平均视在功率(kVA)', '最大视在功率(kVA)', '最小视在功率(kVA)', 
                '平均电流(A)', '最大电流(A)', '最小电流(A)', '平均电压(V)', '最大电压(V)', '最小电压(V)', '平均电流谐波含量', '最大电流谐波含量', '最小电流谐波含量'],
        selected: optionsToUpdate
      },
    });
  });
}

// 回路数据实时图例切换函数
function loopRealtimeLegendListener(realtimeChart) {
  realtimeChart?.on('legendselectchanged', function (params) {
    var legendName = params.name;
    var optionsToUpdate = {};
    switch (legendName) {
      case '有功功率(kW)':
     if (params.selected[legendName]){
          optionsToUpdate = { "有功功率(kW)": true,  "功率因素": false , "电压(V)": false, "电流(A)": false}
        }
        break;

      case '视在功率(kVA)':
      if (params.selected[legendName]){
          optionsToUpdate = { "视在功率(kVA)": true,  "功率因素": false , "电压(V)": false, "电流(A)": false}
      }
        break;

      case '无功功率(kVar)':
      if (params.selected[legendName]){
          optionsToUpdate = { "无功功率(kVar)": true,  "功率因素": false , "电压(V)": false, "电流(A)": false}
      }
        break;

      case '功率因素':
      if (params.selected[legendName]){
        optionsToUpdate = { "功率因素": true,  "有功功率(kW)": false,  "视在功率(kVA)": false,  
                           "无功功率(kVar)": false , "电压(V)": false, "电流(A)": false}
      }
        break;

      case '电流(A)':
      if (params.selected[legendName]){
        optionsToUpdate = { "电流(A)": true, "功率因素": false , "有功功率(kW)": false,  "视在功率(kVA)": false,  
                             "无功功率(kVar)": false , "电压(V)": false}
      }
        break;

      case '电压(V)':
      if (params.selected[legendName]){
        optionsToUpdate = { "电压(V)": true, "功率因素": false , "有功功率(kW)": false, "电流(A)": false, 
                          "视在功率(kVA)": false,  "无功功率(kVar)": false}
      }
        break;
        
      default:
        break;
    }

    realtimeChart?.setOption({
      legend: {
        data: ['有功功率(kW)', '视在功率(kVA)', '无功功率(kVar)', '功率因素', '电压(V)', '电流(A)'],
        selected: optionsToUpdate
      },
    });
  });
}

// 回路数据小时、天 图例切换函数
function loopHourAndDayLegendListener(realtimeChart) {
  realtimeChart?.on('legendselectchanged', function (params) {
    var legendName = params.name;
    var optionsToUpdate = {};
    switch (legendName) {
      case '平均视在功率(kVA)':
      case '最大视在功率(kVA)':
      case '最小视在功率(kVA)':
      if (params.selected[legendName]){
        optionsToUpdate = { "平均视在功率(kVA)": true, "最大视在功率(kVA)": true, "最小视在功率(kVA)": true, "平均电流(A)": false, "最大电流(A)": false, "最小电流(A)": false, 
                            "平均电压(V)": false, "最大电压(V)": false, "最小电压(V)": false };
      }
        break;

      case '平均有功功率(kW)':
      case '最大有功功率(kW)':
      case '最小有功功率(kW)':
      if (params.selected[legendName]){
        optionsToUpdate = { "平均有功功率(kW)": true, "最大有功功率(kW)": true, "最小有功功率(kW)": true, "平均电流(A)": false, "最大电流(A)": false, "最小电流(A)": false, 
                          "平均电压(V)": false, "最大电压(V)": false, "最小电压(V)": false};
      }
        break;

      case '平均无功功率(kVar)':
      case '最大无功功率(kVar)':
      case '最小无功功率(kVar)':
      if (params.selected[legendName]){
        optionsToUpdate = { "平均无功功率(kVar)": true, "最大无功功率(kVar)": true, "最小无功功率(kVar)": true, "平均电流(A)": false, "最大电流(A)": false, "最小电流(A)": false, 
                         "平均电压(V)": false, "最大电压(V)": false, "最小电压(V)": false };
      }
        break;

      case '平均电流(A)':
      case '最大电流(A)':
      case '最小电流(A)':
      if (params.selected[legendName]){
        optionsToUpdate = { "平均电流(A)": true, "最大电流(A)": true, "最小电流(A)": true, "平均视在功率(kVA)": false, "最大视在功率(kVA)": false, "最小视在功率(kVA)": false,
                          "平均有功功率(kW)": false, "最大有功功率(kW)": false, "最小有功功率(kW)": false, "平均无功功率(kVar)": false, "最大无功功率(kVar)": false, "最小无功功率(kVar)": false,
                           "平均电压(V)": false, "最大电压(V)": false, "最小电压(V)": false };
      }
        break;

      case '平均电压(V)':
      case '最大电压(V)':
      case '最小电压(V)':
      if (params.selected[legendName]){
        optionsToUpdate = { "平均电压(V)": true, "最大电压(V)": true, "最小电压(V)": true, "平均视在功率(kVA)": false, "最大视在功率(kVA)": false, "最小视在功率(kVA)": false,
                 "平均有功功率(kW)": false, "最大有功功率(kW)": false, "最小有功功率(kW)": false,"平均无功功率(kVar)": false, "最大无功功率(kVar)": false, "最小无功功率(kVar)": false,
                 "平均电流(A)": false, "最大电流(A)": false, "最小电流(A)": false }
          }
        break;

      default:
        break;
    }

    realtimeChart?.setOption({
      legend: {
        data:  ['平均有功功率(kW)', '最大有功功率(kW)', '最小有功功率(kW)','平均无功功率(kVar)', '最大无功功率(kVar)', '最小无功功率(kVar)','平均视在功率(kVA)', '最大视在功率(kVA)', '最小视在功率(kVA)', 
                '平均电流(A)', '最大电流(A)', '最小电流(A)', '平均电压(V)', '最大电压(V)', '最小电压(V)'],
        selected: optionsToUpdate
      },
    });
  });
}

// 总数据实时图例切换函数
function outletRealtimeLegendListener(realtimeChart) {
  realtimeChart?.on('legendselectchanged', function (params) {
    var legendName = params.name;
    var optionsToUpdate = {};
    switch (legendName) {
      case '有功功率(kW)':
     if (params.selected[legendName]){
          optionsToUpdate = {  "有功功率(kW)": true , "功率因素": false};
        }
        break;

      case '视在功率(kVA)':
      if (params.selected[legendName]){
          optionsToUpdate = {  "视在功率(kVA)": true , "功率因素": false};
      }
        break;

      case '无功功率(kVar)':
      if (params.selected[legendName]){
          optionsToUpdate = {  "无功功率(kVar)": true , "功率因素": false};
      }
        break;

      case '功率因素':
      if (params.selected[legendName]){
          optionsToUpdate = {  "功率因素": true , "有功功率(kW)": false, "无功功率(kVar)": false, "视在功率(kVA)": false};
      }
        break;

        
      default:
        break;
    }

    realtimeChart?.setOption({
      legend: {
        data: ['有功功率(kW)', '视在功率(kVA)', '无功功率(kVar)', '功率因素'],
        selected: optionsToUpdate
      },
    });
  });
}

// 给折线图提示框的数据加单位
function customTooltipFormatter(params: any[]) {
  var tooltipContent = ''; 
  params.forEach(function(item) {
    switch( item.seriesName ){

      case '平均有功功率(kW)':
      case '平均无功功率(kVar)':
      tooltipContent += item.marker +' 记录时间：'  +params[0].name+  ' ' + item.seriesName + ': ' + item.value  +'<br/>';
      break;
      case '最大有功功率(kW)':
        tooltipContent += item.marker +' 发生时间：' +powActiveMaxTimeData.value[item.dataIndex]+  ' ' + item.seriesName + ': ' + item.value+'<br/>';
        break;
      case '最小有功功率(kW)':
      tooltipContent += item.marker +' 发生时间：'+powActiveMinTimeData.value[item.dataIndex]+  ' ' + item.seriesName + ': ' + item.value +'<br/>';
              break;
      case '最大无功功率(kVar)':
      tooltipContent += item.marker +' 发生时间：'+powReactiveMaxTimeData.value[item.dataIndex] +  ' ' + item.seriesName + ': ' + item.value +'<br/>';
              break;
      case '最小无功功率(kVar)':
      tooltipContent += item.marker +' 发生时间：'  +powReactiveMaxTimeData.value[item.dataIndex]+  ' ' + item.seriesName + ': ' + item.value+'<br/>';
              break;

      case '平均视在功率(kVA)':
      tooltipContent += item.marker +' 记录时间：'  +params[0].name+  ' ' + item.seriesName + ': ' + item.value  +'<br/>';
      break;
      case '最大视在功率(kVA)':
      tooltipContent += item.marker +' 发生时间：' +powApparentMaxTimeData.value[item.dataIndex] +  ' ' + item.seriesName + ': ' + item.value+'<br/>';
              break;
      case '最小视在功率(kVA)':
      tooltipContent += item.marker +' 发生时间：'  +powApparentMaxTimeData.value[item.dataIndex]+  ' ' + item.seriesName + ': ' + item.value+'<br/>';
              break;

      case '电流(A)':
      case '电流三相不平衡': 
      case '平均电流(A)':
      tooltipContent += item.marker +' 记录时间：'  +params[0].name+  ' ' + item.seriesName + ': ' + item.value  +'<br/>';
      break;
      case '最大电流(A)':
      tooltipContent += item.marker +' 发生时间：' +curMaxTimeData.value[item.dataIndex]+  ' ' + item.seriesName + ': ' + item.value +'<br/>' ;
      break;
      case '最小电流(A)':
      tooltipContent += item.marker +' 发生时间：' +curMinTimeData.value[item.dataIndex] +  ' ' + item.seriesName + ': ' + item.value+'<br/>';
      break;

      case '电压(V)':
      case '线电压':
      case '电压三相不平衡': 
      case '平均电压(V)':
      case '平均线电压':
      tooltipContent += item.marker +' 记录时间：'  +params[0].name+  ' ' + item.seriesName + ': ' + item.value  +'<br/>';
      break;
      case '最大电压(V)':
      tooltipContent += item.marker +' 发生时间：'+volMaxTimeData.value[item.dataIndex] +  ' ' + item.seriesName + ': ' + item.value  +'<br/>';
      break;
      case '最小电压(V)':
      tooltipContent += item.marker +' 发生时间：' +volMinTimeData.value[item.dataIndex] +  ' ' + item.seriesName + ': ' + item.value +'<br/>';
      break;

      case '最大电流谐波含量':
      tooltipContent += item.marker +' '+curThdMaxTimeData.value[item.dataIndex]+  ' ' + item.seriesName + ': ' + item.value  +'<br/>';
      break;
      case '最小电流谐波含量':
      tooltipContent += item.marker +" 发生时间："+curThdMinTimeData.value[item.dataIndex]+  ' ' + item.seriesName + ': ' + item.value + '<br/>';
      break;

      case '功率因素':
      case '负载率':
      case '电流谐波含量':
      case '平均电流谐波含量':  
      tooltipContent += item.marker +' 记录时间：'  +params[0].name+  ' ' + item.seriesName + ': ' + item.value  +'<br/>';
        break;
      case '无功功率(kVar)':
      case '视在功率(kVA)':
      case '有功功率(kW)':
      case '总有功功率(kW)':
      case '总无功功率(kVar)':
      case '总视在功率(kVA)':
      tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value  + '<br/>';
      break;
    }
    
  })
  if(params[0].seriesName == "总有功功率(kW)"|| params[0].seriesName == "总无功功率(kVar)" || params[0].seriesName == "总视在功率(kVA)"||params[0].seriesName == "有功功率(kW)"|| params[0].seriesName =='无功功率(kVar)'|| params[0].seriesName == '视在功率(kVA)'){
    tooltipContent +='记录时间：'  +params[0].name+ '<br/>'
  }
  
  
  return tooltipContent;
}

// 处理数据后有几位小数点
function formatNumber(value, decimalPlaces) {
    if (!isNaN(value)) {
        return Number(value).toFixed(decimalPlaces);
    } else {
        return null; // 或者其他默认值
    }
}

// 原始数据默认查询的时间范围
function defaultHourTimeRange(hour: number){
  // 先获取本地时区偏移量（以分钟为单位，需要转换为毫秒）
  var timezoneOffset = new Date().getTimezoneOffset() * 60 * 1000
  // 计算当前时间和1小时前的时间，并考虑时区偏移量
  var end = new Date(new Date().getTime() - timezoneOffset);
  var start = new Date(end.getTime() - 60 * 60 * 1000 * hour);
  // 格式化时间并返回
  return [start.toISOString().slice(0, 19).replace('T', ' '), end.toISOString().slice(0, 19).replace('T', ' ')]
}

// 禁选未来的日期
const disabledDate = (date) => {
  const today = new Date();
  // today.setHours(0, 0, 0, 0);
  // // 设置date的时间为0时0分0秒，以便与today进行比较
  // date.setHours(0, 0, 0, 0);
  // 如果date在今天之后，则禁用
  return date > today;
}

// 处理实时数据的时间选择不超过xxx范围
// const handleDayPick = () => {
//   if (activeName.value=='realtimeTabPane'){
//     // 计算两个日期之间的天数差
//    const diffDays = betweenDay(convertDate(queryParams.timeRange[0]), convertDate(queryParams.timeRange[1]))
//     // 如果天数差超过2天，则重置选择的日期
//     if (diffDays > 2) {
//       queryParams.timeRange = [];
//       ElMessage({
//         message: '时间选择不超过48小时',
//         type: 'warning',
//       })
//     }
//   }
//   if (activeName.value=='hourExtremumTabPane'){
//     // 计算两个日期之间的天数差
//    const diffDays = betweenDay(convertDate(queryParams.timeRange[0]), convertDate(queryParams.timeRange[1]))
//     // 如果天数差超过7天，则重置选择的日期
//     if (diffDays > 7) {
//       queryParams.timeRange = [];
//       ElMessage({
//         message: '时间选择不超过7天',
//         type: 'warning',
//       })
//     }
//   }
//     if (activeName.value=='dayExtremumTabPane'){
//     // 计算两个日期之间的天数差
//    const diffDays = betweenDay(convertDate(queryParams.timeRange[0]), convertDate(queryParams.timeRange[1]))
//     // 如果天数差超过7天，则重置选择的日期
//     if (diffDays > 180) {
//       queryParams.timeRange = [];
//       ElMessage({
//         message: '时间选择不超过6个月',
//         type: 'warning',
//       })
//     }
//   }
// }


// 导航栏选择后触发
const handleClick = async (row) => {
   if(row.type != null  && row.type == 7){
    queryParams.boxId = undefined
    queryParams.devkey = row.unique
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

// 接口获取机房导航列表
const getNavList = async() => {
  const res = await IndexApi.getBoxMenu()
  navList.value = res
}

/** 搜索按钮操作 */
const handleQuery = () => {
  needFlush.value++;
}

// 参数类型选择框初始化
const getTypeMaxValue = async () => {
    const data = await HistoryDataApi.getTypeMaxValue(queryParams.boxId)
    
    const lineIdMaxValue = data.line_id_max_value;
    const loopIdMaxValue = data.loop_id_max_value;
    const outletIdMaxValue = data.outlet_id_max_value;
    const typeSelectionValue  = [
    {
      value: "total",
      label: '总'
    },
    {
      value: "line",
      label: '相',
      children: (() => {
        const lines: { value: any; label: string; }[] = [];
        for (let i = 1; i <= lineIdMaxValue; i++) {
          lines.push({ value: `${i}`, label: `L${i}` });
        }
        return lines;
      })(),
    },
     {
      value: "loop",
      label: '回路',
      children: (() => {
        const loops: { value: any; label: string; }[] = [];
        for (let i = 1; i <= loopIdMaxValue; i++) {
          loops.push({ value: `${i}`, label: `C${i}` });
        }
        return loops;
      })(),
    },
    {
      value: "outlet",
      label: '输出位',
      children: (() => {
        const outlets: { value: any; label: string; }[] = [];
        for (let i = 1; i <= outletIdMaxValue; i++) {
          outlets.push({ value: `${i}`, label: `${i}` });
        }
        return outlets;
      })(),
    },
  ]
  typeSelection.value = typeSelectionValue;
}

//导出Excel
const handleExport1 = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    queryParams.pageNo = 1
    queryParams.nowAddress = nowAddress.value
    exportLoading.value = true
    const axiosConfig = {
      timeout: 0 // 设置超时时间为0
    }
    const data = await HistoryDataApi.exportBoxHistorydetailsPageData(queryParams, axiosConfig)
    await download.excel(data, '母线插接箱电力分析.xlsx')
  } catch (error) {
    // 处理异常
    console.error('导出失败：', error)
  } finally {
    exportLoading.value = false
  }
}
/** 初始化 **/
onMounted( async () => { 
  getNavList()
  // 获取路由参数中的 pdu_id
  const queryBoxId = useRoute().query.boxId as string  | undefined;
  const queryLocation = useRoute().query.location as string;
  queryParams.boxId = queryBoxId ? parseInt(queryBoxId, 10) : undefined;
  getTypeMaxValue();

  if (queryParams.boxId != undefined){
    await getList(); 
    nowAddress.value = queryLocation
    nowAddressTemp.value = queryLocation
    initChart();
  }
})


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
    display: flex;
    flex-direction: column;
    align-items: center;
    font-size: 16px;
  }

.nav_data{
  padding-left: 5px;
  width: 195px;
}
.nav_content span{
  font-size: 18px;
}
.carousel-container {
  width: 100%;
  max-width: 100%;
}
.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover; 
}
.description-item {
  display: flex;
  align-items: center;
}

.label {
  text-align: right; /* 文本右对齐 */
  margin-right: 10px; /* 控制冒号后的间距 */
  text-align: left;
}

.value {
  flex: 1; /* 自动扩展以对齐数据 */
  text-align: left;

}
  .line {
    height: 1px;
    margin-top: 28px;

    background: linear-gradient(297deg, #fff, #dcdcdc 51%, #fff);
  }

</style>