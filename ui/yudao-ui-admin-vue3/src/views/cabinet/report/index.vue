<template>
  <el-row :gutter="20">
    <el-col :span="treeWidth" :xs="24">
      
      <el-input
        v-model="filterText"
        style="width: 190px"
        placeholder="Filter keyword"
      />

      <el-tree
        ref="treeRef"
        style="max-width: 600px"
        class="filter-tree"
        :data="serverRoomArr"
        :props="defaultProps"
        default-expand-all

        :filter-node-method="filterNode"
      />
    </el-col>
    <el-col :span="24 - treeWidth" :xs="24">
      <el-form
         class="-mb-15px"
         :model="queryParams"
         ref="queryFormRef"
         :inline="true"
         label-width="120px"
       >
       <el-form-item label="" prop="collaspe">
            <el-switch 
            v-model="isCollapsed"  
            active-color="#409EFF" 
            inactive-color="#909399"
            active-text="折叠"  
            active-value="100"
            inactive-value="0" 
            @change="toggleCollapse" />
          </el-form-item>
         <el-form-item label="时间段" prop="createTime">
           <el-date-picker
             v-model="queryParams.createTime"
             value-format="YYYY-MM-DD HH:mm:ss"
             type="daterange"
             start-placeholder="开始日期"
             end-placeholder="结束日期"
             :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
             class="!w-210px"
           />
         </el-form-item>
         <el-text size="large">
          报警次数：{{ cabinetInfo.alarm }}
         </el-text>
         
       </el-form>
      <el-collapse  v-model="activeNames">
        <el-collapse-item title="机柜信息" name="1">
          <el-row class="text-container"> 
            <el-col :span="8" >
              <div style="font-size: 30px;">
                机柜名称 {{ cabinetInfo.name }}<br/>
                类型 {{ cabinetInfo.type }}<br/>
                容量 {{ cabinetInfo.pow_capacity }}kW<br/>
                所属机房 {{ cabinetInfo.owner }} <br/>
                平衡度 {{ cabinetInfo.balance }} <br/>
              </div>
            </el-col>
            <el-col :span="6">
              <div style="font-size: 28px;">
                用电量 {{ cabinetInfo.eq }}kW<br/>
                总视在功率(最大) {{ cabinetInfo.total_apparent_pow_max_value }}kWh  <br/>
                最大温度 {{ cabinetInfo.tem_max_value }}°C<br/>
                最大湿度 {{ cabinetInfo.hum_max_value }}%<br/>
                负载百分比 {{ cabinetInfo.loadPer }}%<br/>
              </div>
            </el-col> 
            <el-col :span="10">
              <el-row justify="center">
                <el-col  :span="6">服务器IT总视在功率</el-col>
                <el-col  :span="6">&nbsp;</el-col>
              </el-row>
              <el-row>
                <div ref="serChartContainer" id="serChartContainer" style="width: 23vw; height: 30vh"></div>  
              </el-row>
            </el-col>
          </el-row>
         
        </el-collapse-item>
        <el-collapse-item title="耗电排名(日期)" name="2">
          <el-form-item label="颗粒度" prop="type">
           <el-select
             v-model="queryParams.eqGranularity"
             placeholder="请选择天/周/月"
             class="!w-120px"
           >
             <el-option label="天" value="day" />
             <el-option label="周" value="week" />
             <el-option label="月" value="month" />
            </el-select>
          </el-form-item>

            <div ref="rankChartContainer" id="rankChartContainer" style="width: 75vw; height: 58vh;"></div>

        </el-collapse-item>

        <el-collapse-item title="功率曲线" name="3">
          <el-form-item label="颗粒度" prop="type">
           <el-select
             v-model="queryParams.powGranularity"
             placeholder="请选择分钟/小时/天"
             class="!w-120px"
           >
           <el-option label="分钟" value="realtime" />
             <el-option label="小时" value="hour" />
             <el-option label="天" value="day" />
            </el-select>
          </el-form-item>
          <ContentWrap style="overflow: visible;">
            <div ref="powChartContainer" id="powChartContainer" style="width: 70vw; height: 58vh;"></div>
          </ContentWrap>
        </el-collapse-item>
        <el-collapse-item title="服务器耗电排名" name="4">
            <el-form-item label="显示数量" prop="type">
            <el-select
              v-model="queryParams.serverNumber"
              placeholder="请选择数量"
              class="!w-120px"
            >
                <el-option label="10" value= 10 />
                <el-option label="7" value= 7 />
              </el-select>
            </el-form-item>
          <div ref="serRankChartContainer" id="serRankChartContainer" style="width: 75vw; height: 58vh;"></div>
        </el-collapse-item>
        <el-collapse-item title="温度曲线" name="5">
          <el-form-item label="颗粒度" prop="type">
           <el-select
             v-model="queryParams.temGranularity"
             placeholder="请选择分钟/小时/天"
             class="!w-120px"
           >
           <el-option label="分钟" value="realtime" />
             <el-option label="小时" value="hour" />
             <el-option label="天" value="day" />
            </el-select>
          </el-form-item>
          <ContentWrap style="overflow: visible;">
            <div ref="temChartContainer" id="temChartContainer" style="width: 70vw; height: 58vh;"></div>
          </ContentWrap>
        </el-collapse-item>
      </el-collapse>
    </el-col>
  </el-row>
  <!-- 表单弹窗：添加/修改 -->
  <PDUDeviceForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
// import download from '@/utils/download'
// import { PDUDeviceApi } from '@/api/pdu/pdudevice'
import * as echarts from 'echarts';
import { ElTree } from 'element-plus'

/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })

//产生元素为200-500之间随机整数的数组
const generateRandomIntegers = (n: number): number[] => {
  const randomIntegers: number[] = [];
  for (let i = 0; i < n; i++) {
    const randomInteger = Math.floor(Math.random() * (500 - 200 + 1)) + 200;
    randomIntegers.push(randomInteger);
  }
  return randomIntegers.sort((a, b) => b - a);
}

//产生随机字符串数组，元素值为机柜+n
const generateShuffledStrings = (n: number,name : string): string[] => {
  const strings: string[] = [];
  for (let i = 1; i <= n; i++) {
    const string = name + i;
    strings.push(string);
  }
  for (let i = strings.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [strings[i], strings[j]] = [strings[j], strings[i]];
  }
  return strings;
}

const activeNames = ref(["1","2","3","4","5"])

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  id: undefined,
  type: 'total',
  eqGranularity:"day",
  powGranularity : "hour",
  temGranularity : "hour",
  serverNumber : 10,
  ipAddr: undefined,
  createTime: undefined,
})

const serverRoomArr =  [
  {
    value: '1',
    label: '机房1',
    children: [
      {
        value: '1-1',
        label: '母线1',
        // children: [
        // {
        //   value: '1-1-1',
        //   label: '机柜1',
        // },
        // {
        //   value: '1-1-2',
        //   label: '机柜2',
        // },]
      },
    ],
  },
  {
    value: '2',
    label: '机房2',
    children: [
      {
        value: '2-1',
        label: '母线1',
        // children: [
        // {
        //   value: '2-1-1',
        //   label: '机柜1',
        // },
        // {
        //   value: '2-1-2',
        //   label: '机柜2',
        // },]
      },
    ],
  },
  {
    value: '3',
    label: '机房3',
    children: [
      {
        value: '3-1',
        label: '母线1',
        // children: [
        // {
        //   value: '3-1-1',
        //   label: '机柜1',
        // },
        // {
        //   value: '3-1-2',
        //   label: '机柜2',
        // },]
      },
      {
        value: '3-2',
        label: '母线2',
        // children: [
        // {
        //   value: '3-1-1',
        //   label: '机柜1',
        // },
        // {
        //   value: '3-1-2',
        //   label: '机柜2',
        // },]
      },
    ],
  },
]

//折叠功能
let treeWidth = ref(3)
let isCollapsed = ref(0);

//柱状图宽度
const barWid = ref(60);

const cabinetInfo = ref({
  alarm : 6,
  name : "机柜1",
  type : "xx",
  pow_capacity : "250",
  owner :"机房1",
  eq : "200",
  total_apparent_pow_max_value : "200",
  tem_max_value : "25",
  hum_max_value : "75",
  balance : "50",
  loadPer : "80"
})

//折线图数据
interface EqData {
  eq: number[];
  time: string[];
}
const eqData = ref<EqData>({
  eq : [],
  time : []
})

interface PowData {
  total_apparent_pow_avg_value: number[];
  total_active_pow_avg_value: number[];
  time: string[];
}
const powData = ref<PowData>({
  total_apparent_pow_avg_value : [],
  total_active_pow_avg_value: [],
  time:[]
})

interface TemData {
  tem_avg_value: number[];
  time: string[];
}
const temData = ref<TemData>({
  tem_avg_value : [],
  time : []
})

interface ServerData {
  nameAndMax: object[];
  value: number[];
}
const serverData = ref<ServerData>({
  nameAndMax : [
    { name: '服务器1', max: 250 },
    { name: '服务器2', max: 250 },
    { name: '服务器3', max: 250 },
    { name: '服务器4', max: 250 },
    { name: '服务器5', max: 250 },
    { name: '服务器6', max: 250 },
    { name: '服务器7', max: 250 },
    { name: '服务器8', max: 250 }
  ],
  value: [200, 180, 170, 220, 167, 189,200,150]
})

interface SerRankData {
  name: string[];
  eq: number[];
}

const serRankData = ref<SerRankData>({
  name : [],
  eq : [],
})

//树型控件
interface Tree {
  [key: string]: any
}

const filterText = ref('')
const treeRef = ref<InstanceType<typeof ElTree>>()

const filterNode = (value: string, data: Tree) => {
  if (!value) return true
  return data.label.includes(value)
}

const defaultProps = {
  children: 'children',
  label: 'label',
}

const getList = () => {
  loading.value = true
  try {
    // 生成假数据
    const fakeData = [
      {
          id: 1,
          location: "机房1-机柜1",
          eq: 501,
          eqCreateTime: "2024-03-13 08:00:00",
          total_apparent_pow_avg_value : 200,
          total_active_pow_avg_value : 180,
          powCreateTime : "2024-03-13 08:00:00",
          tem_avg_value : 25,
          temCrateTime : "2024-03-13 08:00:00",
      },
      {
          id: 2,
          location: "机房1-机柜1",
          eq: 435,
          eqCreateTime: "2024-03-05 08:00:00",
          total_apparent_pow_avg_value : 190,
          total_active_pow_avg_value : 170,
          powCreateTime : "2024-03-13 09:00:00",
          tem_avg_value : 27,
          temCrateTime : "2024-03-13 09:00:00",
      },
      {
          id: 3,
          location: "机房1-机柜1",
          eq: 360,
          eqCreateTime: "2024-02-15 08:00:00",
          total_apparent_pow_avg_value : 220,
          total_active_pow_avg_value : 200,
          powCreateTime : "2024-03-13 10:00:00",
          tem_avg_value : 26.4,
          temCrateTime : "2024-03-13 10:00:00",
      },
      {
          id: 4,
          location: "机房1-机柜1",
          eq: 352,
          eqCreateTime: "2024-03-20 08:00:00",
          total_apparent_pow_avg_value : 170,
          total_active_pow_avg_value : 160,
          powCreateTime : "2024-03-13 11:00:00",
          tem_avg_value : 27.8,
          temCrateTime : "2024-03-13 11:00:00",
      },
      {
          id: 5,
          location: "机房1-机柜1",
          eq: 290,
          eqCreateTime: "2024-02-05 08:00:00",
          total_apparent_pow_avg_value : 200,
          total_active_pow_avg_value : 180,
          powCreateTime : "2024-03-13 12:00:00",
          tem_avg_value : 24.3,
          temCrateTime : "2024-03-13 12:00:00",
      },
      {
          id: 6,
          location: "机房1-机柜1",
          eq: 275,
          eqCreateTime: "2024-02-25 08:00:00",
          total_apparent_pow_avg_value : 200,
          total_active_pow_avg_value : 180,
          powCreateTime : "2024-03-13 13:00:00",
          tem_avg_value : 24.3,
          temCrateTime : "2024-03-13 13:00:00",
      },
      {
          id: 7,
          location: "机房1-机柜1",
          eq: 260,
          eqCreateTime: "2024-03-25 08:00:00",
          total_apparent_pow_avg_value : 200,
          total_active_pow_avg_value : 180,
          powCreateTime : "2024-03-13 14:00:00",
          tem_avg_value : 24.3,
          temCrateTime : "2024-03-13 14:00:00",
      },
    ];

    eqData.value.eq = fakeData.map((item) => item.eq);
    eqData.value.time = fakeData.map((item) => item.eqCreateTime);

    powData.value.total_apparent_pow_avg_value = fakeData.map((item) => item.total_apparent_pow_avg_value);
    powData.value.total_active_pow_avg_value = fakeData.map((item) => item.total_active_pow_avg_value);
    powData.value.time = fakeData.map((item) => item.powCreateTime);

    temData.value.tem_avg_value = fakeData.map((item) => item.tem_avg_value);
    temData.value.time = fakeData.map((item) => item.temCrateTime);

    serRankData.value.name = generateShuffledStrings(queryParams.serverNumber,"服务器");
    serRankData.value.eq = generateRandomIntegers(queryParams.serverNumber);
  }finally{
    loading.value = false
  }
}

const rankChartContainer = ref<HTMLElement | null>(null);
let rankChart = null as echarts.ECharts | null; // 显式声明 rankChart 的类型
const powChartContainer = ref<HTMLElement | null>(null);
let powChart = null as echarts.ECharts | null; // 显式声明 powChart 的类型
const temChartContainer = ref<HTMLElement | null>(null);
let temChart = null as echarts.ECharts | null; // 显式声明 temChart 的类型
const serChartContainer = ref<HTMLElement | null>(null);
let serChart = null as echarts.ECharts | null; // 显式声明 serChart 的类型
const serRankChartContainer = ref<HTMLElement | null>(null);
let serRankChart = null as echarts.ECharts | null; // 显式声明 serChart 的类型

const initChart = () => {
  const instance = getCurrentInstance();
  if (rankChartContainer.value && instance) {
    rankChart = echarts.init(rankChartContainer.value);
    rankChart.setOption({
      // 这里设置 Echarts 的配置项和数据
      title: { text: ''},
      tooltip: { trigger: 'axis'},
      legend: { data: ['耗电量']},
      toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
      xAxis: {type: 'category' ,data:eqData.value.time},
      yAxis: { type: 'value'},
      series: [
        {name:"耗电量",  type: 'bar', data: eqData.value.eq, label: { show: true, position: 'top' }, barWidth: barWid.value},// 你可以根据需要选择标签的位置，比如 'top', 'insideTop', 'inside', 等等
      ],
    });
    // 将 rankChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    instance.appContext.config.globalProperties.rankChart = rankChart;
  }
  if (serRankChartContainer.value && instance) {
    serRankChart = echarts.init(serRankChartContainer.value);
    serRankChart.setOption({
      // 这里设置 Echarts 的配置项和数据
      title: { text: ''},
      tooltip: { trigger: 'axis'},
      legend: { data: ['耗电量']},
      toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
      xAxis: {type: 'category' ,data:serRankData.value.name},
      yAxis: { type: 'value'},
      series: [
        {name:"耗电量",  type: 'bar', data: serRankData.value.eq, label: { show: true, position: 'top' }, barWidth: barWid.value},// 你可以根据需要选择标签的位置，比如 'top', 'insideTop', 'inside', 等等
      ],
    });
    // 将 serRankChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    instance.appContext.config.globalProperties.serRankChart = serRankChart;
  }
  if (powChartContainer.value && instance) {
    powChart = echarts.init(powChartContainer.value);
    powChart.setOption({
      // 这里设置 Echarts 的配置项和数据
      title: { text: ''},
      tooltip: { trigger: 'axis' },
      legend: { data: ['总平均视在功率','总平均有功功率']},
      grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
      toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
      xAxis: {type: 'category', boundaryGap: false, data:powData.value.time},
      yAxis: { type: 'value'},
      series: [
        {name: '总平均视在功率', type: 'line', data: powData.value.total_apparent_pow_avg_value, lineStyle: {type: 'dashed'}},
        {name: '总平均有功功率', type: 'line', data: powData.value.total_active_pow_avg_value, lineStyle: {type: 'dashed'}},
      ],

    });
    // 将 powChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    instance.appContext.config.globalProperties.powChart = powChart;
  }
  if (temChartContainer.value && instance) {
    temChart = echarts.init(temChartContainer.value);
    temChart.setOption({
      // 这里设置 Echarts 的配置项和数据
      title: { text: ''},
      tooltip: { trigger: 'axis' },
      legend: { data: ['温度']},
      grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
      toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
      xAxis: {type: 'category', boundaryGap: false, data:temData.value.time},
      yAxis: { type: 'value'},
      series: [
        {name: '温度', type: 'line', data: temData.value.tem_avg_value, lineStyle: {type: 'dashed'}},
      ],
    });
    // 将 temChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    instance.appContext.config.globalProperties.temChart = temChart;
  }
  if (serChartContainer.value && instance) {
    serChart = echarts.init(serChartContainer.value);
    serChart.setOption({
      radar: { indicator: serverData.value.nameAndMax },
      series: [
        { name: '服务器IT总视在功率', type: 'radar', label: { show: true, position: 'top' } ,data: [ { value: serverData.value.value, name: '总视在功率' }, ] }
      ]
    });
    // 将 serChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    instance.appContext.config.globalProperties.serChart = serChart;
  }
};

// 在组件销毁时手动销毁图表
const beforeRankUnmount = () => {
    rankChart?.dispose(); // 销毁图表实例
};

const beforeSerRankUnmount = () => {
    serRankChart?.dispose(); // 销毁图表实例
};

// const beforePowUnmount = () => {
//     powChart?.dispose();  // 销毁图表实例
// };

// const beforeTemUnmount = () => {
//     temChart?.dispose(); // 销毁图表实例
// };

// const beforeSerUnmount = () => {
//     serChart?.dispose(); // 销毁图表实例
// };

window.addEventListener('resize', function() {
    rankChart?.resize(); 
    powChart?.resize(); 
    temChart?.resize(); 
});

watch(filterText, (val) => {
  treeRef.value!.filter(val)
})

watch([() => queryParams.serverNumber], ([newSerNumber]) => {
    // 销毁原有的图表实例
    beforeSerRankUnmount();
    // 创建新的图表实例
    serRankChart = echarts.init(document.getElementById('serRankChartContainer'));
    serRankData.value.eq = generateRandomIntegers(newSerNumber);
    serRankData.value.name = generateShuffledStrings(newSerNumber,"服务器");
    // 设置新的配置对象
    if (serRankChart) {
      serRankChart.setOption({
        // 这里设置 Echarts 的配置项和数据
        title: { text: ''},
        tooltip: { trigger: 'axis'},
        legend: { data: ['耗电量']},
        toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
        xAxis: {type: 'category' ,data:serRankData.value.name},
        yAxis: { type: 'value'},
        series: [
          {name:"耗电量",  type: 'bar', data: serRankData.value.eq, label: { show: true, position: 'top' }, barWidth: barWid.value},// 你可以根据需要选择标签的位置，比如 'top', 'insideTop', 'inside', 等等
        ],
      });
    }
    
})

// 监听类型颗粒度
watch([ () => queryParams.eqGranularity], (eqNew) => {
    const [ newEqGranularity] = eqNew;
    // 处理参数变化

    if ( newEqGranularity == 'day'){
      // 销毁原有的图表实例
      beforeRankUnmount()
      // 创建新的图表实例
      rankChart = echarts.init(document.getElementById('rankChartContainer'));
      getList();
      // 设置新的配置对象
      if (rankChart) {
        rankChart.setOption({
        // 这里设置 Echarts 的配置项和数据
        title: { text: ''},
        tooltip: { trigger: 'axis'},
        legend: { data: ['耗电量']},
        toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
        xAxis: {type: 'category' ,data:eqData.value.time},
        yAxis: { type: 'value'},
        series: [
          { type: 'bar', data: eqData.value.eq, label: { show: true, position: 'top' }, barWidth: barWid.value},// 你可以根据需要选择标签的位置，比如 'top', 'insideTop', 'inside', 等等
        ],
      });
    }
    }else if(newEqGranularity == 'week'){
      // 销毁原有的图表实例
      beforeRankUnmount()
      // 创建新的图表实例
      rankChart = echarts.init(document.getElementById('rankChartContainer'));
      eqData.value.time = ["2023-04-第一周","2023-03-第二周","2023-02-第三周","2023-01-第一周","2023-03-第一周","2023-02-第一周","2023-04-第一周"]
      // 设置新的配置对象
      if (rankChart) {
        rankChart.setOption({
          // 这里设置 Echarts 的配置项和数据
          title: { text: ''},
          tooltip: { trigger: 'axis'},
          legend: { data: ['耗电量']},
          toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
          xAxis: {type: 'category' ,data:eqData.value.time},
          yAxis: { type: 'value'},
          series: [
            { type: 'bar', data: eqData.value.eq, label: { show: true, position: 'top' }, barWidth: barWid.value},// 你可以根据需要选择标签的位置，比如 'top', 'insideTop', 'inside', 等等
          ],
        });
      }
    }else{
      // 销毁原有的图表实例
      beforeRankUnmount()
      // 创建新的图表实例
      rankChart = echarts.init(document.getElementById('rankChartContainer'));
      eqData.value.time = ["2023-04","2023-08","2023-07","2023-02","2023-10","2023-06","2023-09"]
      // 设置新的配置对象
      if (rankChart) {
        rankChart.setOption({
          // 这里设置 Echarts 的配置项和数据
          title: { text: ''},
          tooltip: { trigger: 'axis'},
          legend: { data: ['耗电量']},
          toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
          xAxis: {type: 'category' ,data:eqData.value.time},
          yAxis: { type: 'value'},
          series: [
            { type: 'bar', data: eqData.value.eq, label: { show: true, position: 'top' }, barWidth: barWid.value},// 你可以根据需要选择标签的位置，比如 'top', 'insideTop', 'inside', 等等
          ],
        });
      }
    }
    
});

// 下拉框选项数组
// const deviceStatus = ref([])

// const message = useMessage() // 消息弹窗
// const { t } = useI18n() // 国际化

const toggleCollapse = () => {
  treeWidth.value = isCollapsed.value == 0 ? 3 : 0;
};

const loading = ref(false) // 列表的加载中
// const list = ref([
//   { 
//     id:"1",
//     status:"空闲设备",
//     totalApparentPower:"200kW",
//     totalActivePower:"210kVA",
//     totalElectricalEnergy:"10.112kWh",
//     ipAddr:"192.168.1.1-0",
//     location:"机房2-机柜1-A路",
//     updateTime:"15:25:00"
//   },
//   { 
//     id:"2",
//     status:"离线设备",
//     totalApparentPower:"200kW",
//     totalActivePower:"210kVA",
//     totalElectricalEnergy:"10.112kWh",
//     ipAddr:"192.168.1.2-1",
//     location:"机房2-机柜2-B路",
//     updateTime:"15:25:00"
//   },{ 
//     id:"3",
//     status:"未绑定设备",
//     totalApparentPower:"200kW",
//     totalActivePower:"210kVA",
//     totalElectricalEnergy:"10.112kWh",
//     ipAddr:"192.168.1.3-2",
//     location:"机房2-机柜3-C路",
//     updateTime:"15:25:00"
//   },
// ]) // 列表的数据
// const total = ref(0) // 列表的总页数
const queryFormRef = ref() // 搜索的表单
// const exportLoading = ref(false) // 导出的加载中

/** 查询列表 */
// const getList = async () => {
//   loading.value = true
//   try {
//     const data = await PDUDeviceApi.getPDUDevicePage(queryParams)
//     list.value = data.list
//     total.value = data.total
//   } finally {
//     loading.value = false
//   }
// }

/** 搜索按钮操作 */
// const handleQuery = () => {
//   queryParams.pageNo = 1
//   // getList()
// }

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
onMounted(() => {
  getList();
  initChart();
})
</script>

