<template>
  <CommonMenu :dataList="navList" @check="handleCheck" navTitle="PDU实时能耗查询" placeholder="如:192.168.1.96-0">
    <template #NavInfo>
        <br/>    <br/> 
        <div class="nav_data">
          <!-- <div class="carousel-container">
            <el-carousel :interval="2500" motion-blur height="150px" arrow="never" trigger="click">
              <el-carousel-item v-for="(item, index) in carouselItems" :key="index">
                <img width="auto" height="auto" :src="item.imgUrl" alt="" class="carousel-image" />
              </el-carousel-item>
            </el-carousel>
          </div>
          <div class="nav_content">
            <el-descriptions title="全部PDU新增能耗记录" direction="vertical" :column="1" width="60px" border >
              <el-descriptions-item label="最近一天"><span >{{ lastDayTotalData }} 条</span></el-descriptions-item>
              <el-descriptions-item label="最近一周"><span >{{ lastWeekTotalData }} 条</span></el-descriptions-item>
              <el-descriptions-item label="最近一月" ><span >{{ lastMonthTotalData }} 条</span></el-descriptions-item>
            </el-descriptions>
          </div>
        </div> -->
        <div class="descriptions-container" style="font-size: 14px;">
          <div class="description-item">
            <span class="label">按时间范围查询实时能耗</span>
          </div>
        </div>
        
      </div>
    </template>
    <template #ActionBar>
      <el-form
         class="-mb-15px"
         :model="queryParams"
         ref="queryFormRef"
         :inline="true"
         label-width="auto"
       >
         <!-- <el-form-item label="参数类型" prop="type">
          <el-cascader
            v-model="typeDefaultSelected"
            collapse-tags
            :options="typeSelection"
            collapse-tags-tooltip
            :show-all-levels="true"
            @change="typeCascaderChange"
            class="!w-140px"
          />
        </el-form-item>

         <el-form-item label="颗粒度" prop="granularity">
            <el-select
              v-model="queryParams.granularity"
              placeholder="请选择天/周/月"
              class="!w-120px" >
              <el-option label="天" value="day" />
              <el-option label="周" value="week" />
              <el-option label="月" value="month" />
            </el-select>
          </el-form-item> -->

         <el-form-item label="时间段" prop="timeRange">
            <el-date-picker
            value-format="YYYY-MM-DD"
            v-model="selectTimeRange"
            type="daterange"
            :shortcuts="shortcuts"
            range-separator="-"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :disabled-date="disabledDate"
          />
          </el-form-item>

         <el-form-item >
           <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
           
         </el-form-item>
         <el-button type="success" plain @click="handleExport" :loading="exportLoading" style="float: right;margin-right: 10px;">
             <Icon icon="ep:download" class="mr-5px" /> 导出
           </el-button>
      </el-form>
    </template>
    <template #Content>
      <div>

        <el-table  :data="list"  :show-overflow-tooltip="true" width="1800px"  :header-cell-style="{background:'#f7f7f7',color:'#606266',height:'30px'}">
          <el-table-column label="编号" align="center" prop="tableId" width="80px" >
          <template #default="{ $index }">
            {{ $index + 1 + (queryParams.pageNo - 1) * queryParams.pageSize }}
          </template>  
        </el-table-column>
                <!-- 数据库查询 -->
        <el-table-column label="所在位置" align="center" prop="address"  />
        <el-table-column label= '网络地址' align= 'center' prop= 'location' width= '150px'/>
        <el-table-column label= '开始电能' align='center' >
        <el-table-column label='开始电能(kWh)' align= 'center' prop= 'eleActiveStart'  width= '130px'>
        <template #default="scope">
              <el-text line-clamp="2" v-if="scope.row.eleActiveStart">
                {{ scope.row.eleActiveStart.toFixed(1) }}
              </el-text>
            </template>
            </el-table-column>
        <el-table-column label= '开始时间' align= 'center' prop='createTimeMin'   width= '165px'  >
          <template #default="scope">
              <el-text line-clamp="2" v-if="scope.row.createTimeMin">
                {{ scope.row.createTimeMin }}
              </el-text>
            </template>
        </el-table-column>
      </el-table-column>
      <el-table-column label= '结束电能' align= 'center' >
          <el-table-column label= '结束电能(kWh)' align= 'center' prop= 'eleActiveEnd'  width= '130px'>
            <template #default="scope">
              <el-text line-clamp="2" v-if="scope.row.eleActiveEnd">
                {{ scope.row.eleActiveEnd.toFixed(1) }}
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label= '结束时间' align= 'center' prop= 'createTimeMax'  width= '165px'  >
            <template #default="scope">
              <el-text line-clamp="2" v-if="scope.row.createTimeMax">
                {{ scope.row.createTimeMax }}
              </el-text>
            </template>
          </el-table-column>
        </el-table-column>
        <el-table-column label= '耗电量(kWh)' align= 'center' prop= 'eleActive'  width= '130px'>
          <template #default="scope">
              <el-text line-clamp="2" v-if="scope.row.eleActive">
                {{ scope.row.eleActive.toFixed(1) }}
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center">
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click="toDetails(scope.row.location,scope.row.createTimeMin,scope.row.createTimeMax)"
            >
            详情
            </el-button>
          </template>
        </el-table-column>
      </el-table> 













        <!-- <el-table :datv-loading="loading" :data="list"  :show-overflow-tooltip="true" >
            
        <el-table-column label="序号" align="center" width="80px">
          <template #default="{ $index }">
            {{ $index + 1 + (queryParams.pageNo - 1) * queryParams.pageSize }}
          </template>   
        </el-table-column> -->
       <!-- 遍历其他列 -->
        <!-- <el-table-column label= '所在位置'  align = 'center' prop= 'address' , istrue:true, width='150%'/>
       <el-table-column label= '网络地址' align= 'center' prop= 'location' , istrue:true, width= '150px'/>
       <el-table-column label= '开始电能' align='center' istrue: true>
        <el-table-column label='开始电能(kWh)' align= 'center' prop= 'eleActiveStart' , istrue:true, formatter: formatEle ,width= '130px'/>
        <el-table-column label= '开始时间' align= 'center' prop='createTimeMin' , formatter: formatTime1, width= '130px'  istrue:true/>
      </el-table-column>
        <el-table-column label= '结束电能' align= 'center' istrue: true>
          <el-table-column label= '结束电能(kWh)' align= 'center' prop= 'eleActiveEnd' , istrue:true, formatter: formatEle,width= '130px'/>
          <el-table-column label= '结束时间' align= 'center' prop= 'createTimeMax' , formatter: formatTime1, width= '130px'  istrue:true/>
        </el-table-column>
        <el-table-column label= '耗电量(kWh)' align= 'center' prop= 'eleActive' ,istrue: true,formatter: formatEle ,width= '130px'/>
        <el-table-column label="操作" align="center">
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click="toDetails(scope.row.location,scope.row.createTimeMin,scope.row.createTimeMax)"
            >
            设备详情
            </el-button>
          </template>
        </el-table-column>
       
 -->
      
      </div>
      <!-- 分页 -->
      <Pagination
        :total="total"
        :page-size-arr="pageSizeArr"
        layout = "sizes, prev, pager, next, jumper"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"/>
      <div class="realTotal" v-if="list.length != 0">共 {{ realTotel }} 条</div>
      <br/><br/><br/><br/>
      <ContentWrap>
        <div v-loading="loading" ref="rankChartContainer" id="rankChartContainer" style="height: 65vh"></div>
      </ContentWrap>
    </template>
  </CommonMenu>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import { EnergyConsumptionApi } from '@/api/pdu/energyConsumption'
import { HistoryDataApi } from '@/api/pdu/historydata'
import { formatDate, endOfDay, convertDate, addTime, beginOfDay } from '@/utils/formatTime'
import { CabinetApi } from '@/api/cabinet/info'
import * as echarts from 'echarts';
import { ElMessage } from 'element-plus'
import PDUImage from '@/assets/imgs/PDU.jpg';
import download from '@/utils/download'
import router from '@/router'
const { push } = useRouter()
defineOptions({ name: 'PowerAnalysis' })

const navList = ref([]) as any // 左侧导航栏树结构列表
// const lastDayTotalData = ref(0)
// const lastWeekTotalData = ref(0)
// const lastMonthTotalData = ref(0)
const instance = getCurrentInstance();
const loading = ref(false)
const message = useMessage() // 消息弹窗
const list = ref<Array<{ }>>([]) as any; 
const total = ref(0)
const realTotel = ref(0) // 数据的真实总条数
const selectTimeRange = ref(undefined)
const carouselItems = ref([
      { imgUrl: PDUImage},
      { imgUrl: PDUImage},
      { imgUrl: PDUImage},
      { imgUrl: PDUImage},
    ]);//侧边栏轮播图图片路径
const queryParams = reactive({
  pageNo: 1,
  pageSize: 15,
  // ipAddr: undefined,
  // cascadeAddr : 0,
  // outletId: undefined,
  // type: 'total',
  // granularity: 'day',
  timeRange: undefined as string[] | undefined,
  ipArray: [],
})
const pageSizeArr = ref([15,30,50,100])
const queryFormRef = ref()
const exportLoading = ref(false)

// 时间段快捷选项
const shortcuts = [
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setDate(start.getDate() - 7)
      return [start, end]
    },
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setMonth(start.getMonth() - 1)
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
      start.setMonth(start.getMonth() - 6)
      return [start, end]
    },
  },
  {
    text: '最近一年',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setFullYear(start.getFullYear() - 1)
      return [start, end]
    },
  },
]



// 返回当前页的序号数组
const getPageNumbers = (pageNumber) => {
  const start = (pageNumber - 1) * queryParams.pageSize + 1;
  const end = pageNumber * queryParams.pageSize;
  const pageNumbers: string[] = [];
  for (let i = start; i <= end; i++) {
    pageNumbers.push('序号'+i);
  }
  return pageNumbers;
};

// 返回当前页的序号数组
const getPageNumber = (pageNumber) => {
  const start = (pageNumber - 1) * queryParams.pageSize + 1;
  const end = pageNumber * queryParams.pageSize;
  const count = end - start + 1;
  return count;
};

// 柱状图
const rankChartContainer = ref<HTMLElement | null>(null);
let rankChart = null as echarts.ECharts | null;
const eqData = ref<number[]>([]);
  const initChart = () => {
    if (rankChartContainer.value && instance) {
    // 假设这是您的分页阈值
    const labelThreshold = 30; // 您可以根据需要调整这个值

    // 计算当前分页数量
    const totalPages = getPageNumber(queryParams.pageNo);
    rankChart = echarts.init(rankChartContainer.value);
    rankChart.setOption({
      title: { text: '各PDU实时耗电量'},
      tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
      legend: { data: []},
      toolbox: {feature: {saveAsImage:{}}},
      xAxis: {
        type: 'category',
        data: getPageNumbers(queryParams.pageNo),
        axisLabel: {
          interval: 0, // 根据实际情况调整
          formatter: function (value, index) {
            // 如果超过阈值，则只显示索引
            return totalPages > labelThreshold ? '' : value;
          },  // 如果需要，可以旋转标签
        }
      },
      yAxis: { type: 'value', name: "kWh"},
      series: [
        {
          name: "耗电量",
          type: 'bar',
          barWidth: 'auto', // 自动调整宽度，或指定一个合适的固定宽度
          barGap: '30%',
          data: eqData.value,
          label: {
                        show: totalPages <= labelThreshold,
                        position: 'top'
                    }

        },
      ],
    });
    
    rankChart.on('click', function(params) {
      toDetails(list.value[params.dataIndex].location,
        list.value[params.dataIndex].createTimeMin,
        list.value[params.dataIndex].createTimeMax);
    });

    instance.appContext.config.globalProperties.rankChart = rankChart;
  }
};

window.addEventListener('resize', function() {
  rankChart?.resize(); 
});

// watch(() => queryParams.granularity, () => {
//   handleQuery();
// });

const tableColumns = ref([
  { label: '所在位置', align: 'center', prop: 'address' , istrue:true, width: '150%'},
  { label: '网络地址', align: 'center', prop: 'location' , istrue:true, width: '150px'},
    { label: '开始电能', align: 'center', istrue: true, children: [
      { label: '开始电能(kWh)', align: 'center', prop: 'eleActiveStart' , istrue:true, formatter: formatEle ,width: '130px'},
      { label: '开始时间', align: 'center', prop: 'createTimeMin' , formatter: formatTime1, width: '130px' , istrue:true},
    ]
  },
    { label: '结束电能', align: 'center', istrue: true, children: [
      { label: '结束电能(kWh)', align: 'center', prop: 'eleActiveEnd' , istrue:true, formatter: formatEle,width: '130px'},
      { label: '结束时间', align: 'center', prop: 'createTimeMax' , formatter: formatTime1, width: '130px' , istrue:true},
    ]
  },
  { label: '耗电量(kWh)', align: 'center', prop: 'eleActive' ,istrue: true,formatter: formatEle ,width: '130px'},
  { label: '操作', align: 'center', slot: 'actions' , istrue:true, width: '120px'},
]) as any;

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    if ( selectTimeRange.value != undefined){
      // 格式化时间范围 加上23:59:59的时分秒 
      const selectedStartTime = formatDate(beginOfDay(convertDate(selectTimeRange.value[0])))
      // 结束时间的天数多加一天 ，  一天的毫秒数
      // const oneDay = 24 * 60 * 60 * 1000;
      const selectedEndTime = formatDate(endOfDay(convertDate(selectTimeRange.value[1])))
      queryParams.timeRange = [selectedStartTime, selectedEndTime];
    }
    // 时间段清空后值会变成null 此时搜索不能带上时间段
    if(selectTimeRange.value == null){
      // queryParams.timeRange = undefined
      alert('请输入时间范围');
    return;
    }
    const data = await EnergyConsumptionApi.getEleTotalRealtime(queryParams);
    eqData.value = data.list.map((item) => formatEQ(item.eleActive, 1));
    list.value = data.list
    realTotel.value = data.total
    if (data.total > 10000){
      total.value = 10000
    }else{
      total.value = data.total
    }
  } finally {
    initChart();
    loading.value = false
  }
}

// 自定义图表提示框
function customTooltipFormatter(params: any[]) {
  var tooltipContent = ''; 
  var item = params[0]; // 获取第一个数据点的信息
  tooltipContent += '所在位置：' + (list.value[item.dataIndex].address ? list.value[item.dataIndex].address : '未绑定设备') + '<br/>'
                + item.marker + '网络地址：' + list.value[item.dataIndex].location + '<br/>'
                + item.marker + '开始日期：' + formatTime(null, null, list.value[item.dataIndex].createTimeMin) + ' 开始电能：' + formatEle(null, null, list.value[item.dataIndex].eleActiveStart) + 'kWh <br/>' 
                + item.marker + '结束日期：' + formatTime(null, null, list.value[item.dataIndex].createTimeMax) + ' 结束电能：' + formatEle(null, null, list.value[item.dataIndex].eleActiveEnd) + 'kWh <br/>'
                + item.marker + '耗电量：' + formatEle(null, null, list.value[item.dataIndex].eleActive) + 'kWh';
return tooltipContent;
}

// 最后一页显示数据量过大的提示
const shouldShowDataExceedMessage = computed(() => {
  const lastPageNo = Math.ceil(total.value / queryParams.pageSize);
  return queryParams.pageNo === lastPageNo && total.value >= 10000;
});

// 格式化日期(表格列的时间去掉时分秒 不用传参)
function formatTime(_row: any, _column: any, cellValue: number): string {
  if (!cellValue) {
    return ''
  }
  return dayjs(cellValue).format('YYYY-MM-DD HH:mm:ss')
}

// 格式化日期(表格列的时间去掉时分秒和年)
function formatTime1(_row: any, _column: any, cellValue: number): string {
  if (!cellValue) {
    return ''
  }
  return dayjs(cellValue).format('YYYY-MM-DD')
}

// 格式化电能列数据，保留1位小数（不用传参）
function formatEle(_row: any, _column: any, cellValue: number): string {
  if (cellValue == null) {
    return '';
  }
  return Number(cellValue).toFixed(1);
}

// 格式化耗电量列数据，保留1位小数
function formatEQ(value: number, decimalPlaces: number | undefined){
  if (!isNaN(value)) {
    return Number(value).toFixed(decimalPlaces);
  } else {
      return null; // 或者其他默认值
  }
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

/** 搜索按钮操作 */
const handleQuery = () => {
 queryParams.pageNo = 1
 getList()
}

// 获取参数类型最大值 例如lineId=6 表示下拉框为L1~L6
// const getTypeMaxValue = async () => {
//     const data = await HistoryDataApi.getTypeMaxValue()
//     const outletIdMaxValue = data.outlet_id_max_value;
//     const typeSelectionValue  = [
//     {
//       value: "total",
//       label: '总'
//     },
//     {
//       value: "outlet",
//       label: '输出位',
//       children: (() => {
//         const outlets: { value: any; label: string; }[] = [];
//         outlets.push({ value: undefined, label: '全部' },)
//         for (let i = 1; i <= outletIdMaxValue; i++) {
//           outlets.push({ value: `${i}`, label: `${i}` });
//         }
//         return outlets;
//       })(),
//     },
//   ]
//   typeSelection.value = typeSelectionValue;
// }

// 导航栏选择后触发
const handleCheck = async (node) => {
    let arr = [] as any
    node.forEach(item => { 
      if(item.type == 4){
        arr.push(item.unique);
      }
    });
    //没筛选到pdu 不显示任何数据 ipArray参数传0 后端返回空
    if(arr.length == 0  && node.length != 0){
      arr.push(0)
      rankChart?.clear()
      ElMessage({
        message: '暂无数据',
        type: 'warning',
      });
    }
    queryParams.ipArray = arr
    handleQuery()
}

// 接口获取导航列表
const getNavList = async() => {
  const res = await CabinetApi.getRoomList({})
  let arr = [] as any
  for (let i=0; i<res.length;i++){
  var temp = await CabinetApi.getRoomPDUList({id : res[i].id})
  arr = arr.concat(temp);
  }
  navList.value = arr
}


/** 详情操作*/
const toDetails = (location: string,createTimeMin : string,createTimeMax : string) => {
  push('/pdu/nenghao/powerAnalysis?type=total&granularity=day&start='+createTimeMin+
  '&end='+createTimeMax+'&ip='+location);
}


/** 导出按钮操作 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    queryParams.pageNo = 1
    exportLoading.value = true
    const axiosConfig = {
      timeout: 0 // 设置超时时间为0
    }
    const data = await EnergyConsumptionApi.getEleTotalRealtimeExcel(queryParams, axiosConfig);
    await download.excel(data, 'PDU实时能耗统计.xlsx')
  } catch (error) {
    // 处理异常
    console.error('导出失败：', error)
  } finally {
    exportLoading.value = false
  }
}

/** 初始化 **/
onMounted(() => {
  getNavList()
  // getNavNewData()
  // getTypeMaxValue();
  // getList();
});
 
</script>

<style scoped>
.realTotal{
  float: right;
  padding-top: 20px;
  padding-right: 20px;
  font-size: 14px;
  font-weight: 400; 
  color: #606266
}

.nav_data{
  padding-left: 20px;
  width: 170px;
}
.nav_content span{
  font-size: 14px;
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
.value {
  flex: 1; /* 自动扩展以对齐数据 */
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
   
   ::v-deep .el-table th,
   ::v-deep .el-table td{
    border-right: none;
   }
</style>