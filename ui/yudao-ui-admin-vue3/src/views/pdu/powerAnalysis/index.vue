<template>
  <CommonMenu :dataList="navList" @check="handleCheck" navTitle="PDU能耗数据" placeholder="如:192.168.1.96-0">
    <template #NavInfo>
        <br/>    <br/> 
        <div class="nav_data">
        <div class="descriptions-container" style="font-size: 14px;">
          <div class="description-item">
            <span class="label">最近一天 :</span>
            <span class="value">{{ lastDayTotalData }}条</span>
          </div>
          <div class="description-item">
            <span class="label">最近一周 :</span>
            <span class="value">{{ lastWeekTotalData }}条</span>
          </div>
          <div class="description-item">
            <span class="label">最近一月 :</span>
            <span class="value">{{ lastMonthTotalData }}条</span>
          </div>    <br/>
          <div ><span>全部PDU新增能耗记录</span>
            <div class="line" style="margin-top: 10px;"></div>
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
         <el-form-item label="参数类型" prop="type">
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
          </el-form-item>

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
      <el-table  v-loading="loading" :data="list"  :show-overflow-tooltip="true" :header-cell-style="{background:'#f7f7f7',color:'#606266'}">
        <!-- 添加行号列 -->
        <el-table-column label="序号" align="center" width="80px">
          <template #default="{ $index }">
            {{ $index + 1 + (queryParams.pageNo - 1) * queryParams.pageSize }}
          </template>   
        </el-table-column>
       <!-- 遍历其他列 -->
      <template
       v-for="column in tableColumns" :key="column.label">
        <el-table-column
          v-if="!column.children && column.istrue"
          :key="column.prop"
          :label="column.label"
          :align="column.align"
          :prop="column.prop"
          :formatter="column.formatter"
          :width="column.width"
        >
          <template #default="{ row }" v-if="column.slot === 'actions'">
            <el-button type="primary" @click="toDetails(row.pdu_id,row.address,row.location)">详情</el-button>
          </template>
        </el-table-column>
        


        <el-table-column
          v-else-if="column.istrue"
          :label="column.label"
          :align="column.align"
        >
          <template v-for="child in column.children" :key="child.prop">
            <el-table-column
            v-if="child.istrue"
              :label="child.label"
              :align="child.align"
              :prop="child.prop"
              :formatter="child.formatter"
              :width="child.width"
            >
              <template #default="{ row }" v-if="child.slot === 'actions'">
                <el-button type="primary" @click="toDetails(row.pdu_id, row.address,row.location)">详情</el-button>
              </template>
            </el-table-column>
          </template>
        </el-table-column>


      </template>
      <!-- 超过一万条数据提示信息 -->
      <template v-if="shouldShowDataExceedMessage" #append>
        <tr>
          <td colspan="列数" style="text-align: center; padding: 12px 0;">
            <span style="margin:0 12px; color: red;">数据量过大，请筛选后查看更多数据。</span>
          </td>
        </tr>
      </template>
      </el-table>
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
import { formatDate, endOfDay, convertDate, addTime, startOfDay } from '@/utils/formatTime'
import { CabinetApi } from '@/api/cabinet/info'
import * as echarts from 'echarts';
import { ElMessage } from 'element-plus'
import PDUImage from '@/assets/imgs/PDU.jpg';
import download from '@/utils/download'
import router from '@/router'
const { push } = useRouter()
defineOptions({ name: 'PowerAnalysis' })

const navList = ref([]) as any // 左侧导航栏树结构列表
const lastDayTotalData = ref(0)
const lastWeekTotalData = ref(0)
const lastMonthTotalData = ref(0)
const instance = getCurrentInstance();
const loading = ref(true)
const message = useMessage() // 消息弹窗
const list = ref<Array<{ }>>([]) as any; 
const total = ref(0)
const realTotel = ref(0) // 数据的真实总条数
const now=new Date()
const selectTimeRange = ref([dayjs(new Date(now.getFullYear(), now.getMonth(), 1)).format("YYYY-MM-DD"),dayjs(now).format("YYYY-MM-DD")])
const carouselItems = ref([
      { imgUrl: PDUImage},
      { imgUrl: PDUImage},
      { imgUrl: PDUImage},
      { imgUrl: PDUImage},
    ]);//侧边栏轮播图图片路径
const queryParams = reactive({
  pageNo: 1,
  pageSize: 15,
  ipAddr: undefined,
  cascadeAddr : 0,
  outletId: undefined,
  type: 'total',
  granularity: 'day',
  timeRange: undefined as string[] | undefined,
  ipArray: undefined as string[] | undefined,
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
    text: '最近六个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setMonth(start.getMonth() - 6)
      return [start, end]
    },
  },
]

// 总/输出位筛选
const typeDefaultSelected = ref(['total'])
const typeSelection = ref([]) as any;
const typeCascaderChange = (selected) => {
  queryParams.type = selected[0];
  if (selected[0] === 'outlet'){
    queryParams.outletId = selected[1];
    // 检查是否已经存在 label 为 '输出位' 的行
    const exists = tableColumns.value.some(column => column.label === '输出位');
    if (!exists) {
      // 在列表行索引1(位置后面)插入输出位行 
      const newRow = { label: '输出位', align: 'center', prop: 'outlet_id', istrue: true};
      tableColumns.value.splice(2, 1, newRow);
    }
  }else{
    // 选择总，移除索引为 1 的位置上的行数据
    // tableColumns.value.splice(1, 1);
    for(let i=0;i<tableColumns.value.length;i++){
      if(tableColumns.value[i].label === '输出位'){
        tableColumns.value.splice(i, 1,{ label: '记录日期', align: 'center', prop: 'create_time', formatter: formatTime, istrue: true});
      }
    }
    // if(!haveRecord){
      // for(let i=0;i<tableColumns.value.length;i++){
      //   if(tableColumns.value[i].label === '网络地址'){
      //     tableColumns.value.splice(i, 0, { label: '记录时间', align: 'center', prop: 'record_time', istrue: true});
      //   }
      // }
    // }
  }
  handleQuery();
}

// 返回当前页的序号数组
const getPageNumber = (pageNumber) => {
  const start = (pageNumber - 1) * queryParams.pageSize + 1;
  const end = pageNumber * queryParams.pageSize;
  const count = end - start + 1;
  return count;
};

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
      title: { text: '各PDU耗电量'},
      tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
      legend: { data: []},
      toolbox: {feature: {saveAsImage:{}}},
      xAxis: {type: 'category', data: getPageNumbers(queryParams.pageNo),
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
        {name:"耗电量",  
        type: 'bar', 
        data: eqData.value.map(num => formatEQ(num,1)), 
        label: { 
          show: totalPages <= labelThreshold, position: 'top' 
        },
      },
      ],
    });
    rankChart.on("click",(params)=>{
      toDetails(list.value[params.dataIndex].pdu_id, list.value[params.dataIndex].address,list.value[params.dataIndex].location)
    })
    instance.appContext.config.globalProperties.rankChart = rankChart;
  }
};

window.addEventListener('resize', function() {
  rankChart?.resize(); 
});

watch(() => queryParams.granularity, () => {
  handleQuery();
});

const tableColumns = ref([
  { label: '所在位置', align: 'center', prop: 'address' , istrue:true},
  { label: '网络地址', align: 'center', prop: 'location' , istrue:true, width: '150px'},
  { label: '记录日期', align: 'center', prop: 'create_time', formatter: formatTime, width: '150px' , istrue:true},
  { label: '开始电能', align: 'center', istrue: true, children: [
      { label: '开始电能(kWh)', align: 'center', prop: 'start_ele' , istrue:true, formatter: formatEle,width: '130px'},
      { label: '开始时间', align: 'center', prop: 'start_time' , formatter: formatTime1, width: '130px' , istrue:true},
    ]
  },
  { label: '结束电能', align: 'center', istrue: true, children: [
      { label: '结束电能(kWh)', align: 'center', prop: 'end_ele' , istrue:true, formatter: formatEle,width: '130px'},
      { label: '结束时间', align: 'center', prop: 'end_time' , formatter: formatTime1, width: '130px' , istrue:true},
    ]
  },
  { label: '耗电量(kWh)', align: 'center', prop: 'eq_value' ,istrue: true,formatter: formatPowerEle ,width: '130px'},
  { label: '操作', align: 'center', slot: 'actions' , istrue:true, width: '120px'},
]) as any;

// /** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    if ( selectTimeRange.value != undefined){
      // 格式化时间范围 加上23:59:59的时分秒 
      const selectedStartTime = formatDate(endOfDay(convertDate(selectTimeRange.value[0])))
      const selectedEndTime = formatDate(endOfDay(addTime(convertDate(selectTimeRange.value[1]),1000*60*60*24)))
      queryParams.timeRange = [selectedStartTime, selectedEndTime];
    }
    // 时间段清空后值会变成null 此时搜索不能带上时间段
    if(selectTimeRange.value == null){
      queryParams.timeRange = undefined
    }
    // if (ip.value != undefined){
    //   queryParams.ipArray = [ip.value];
    // }
    const data = await EnergyConsumptionApi.getEQDataPage(queryParams)
    //eqData.value = data.list.map((item) => formatEQ(item.eq_value, 1));
    eqData.value = data.list.map((item) => {
       const difference = item.end_ele - item.start_ele;
       return difference < 0 ? item.end_ele : formatEQ(difference, 1);
    });
    list.value = data.list
    if (data.total > 10000){
      total.value = 10000
    }else{
      total.value = data.total
    }
    realTotel.value = data.total
  }finally{
    loading.value = false;
  }
}

//     list.value = data.list
//     realTotel.value = data.total
//     if (data.total > 10000){
//       total.value = 10000
//     }else{
//       total.value = data.total
//     }
    
//   } finally {
//     initChart();
//     loading.value = false
//   }
// }

const getLists = async () => {
  loading.value = true
  //&& start.value != ""
  try {
    if (start.value != "" && start.value != undefined){
      // 格式化时间范围 加上23:59:59的时分秒 
      const selectedStartTime = formatDate(endOfDay(convertDate(start.value)))
      // 结束时间的天数多加一天 ，  一天的毫秒数
      const selectedEndTime = formatDate(endOfDay(addTime(convertDate(end.value),1000*60*60*24)))
      selectTimeRange.value = [start.value, end.value];
      queryParams.timeRange = [selectedStartTime, selectedEndTime];
    }else{
      const selectedStartTime = formatDate(startOfDay(convertDate(selectTimeRange.value[0])))
      const selectedEndTime = formatDate(endOfDay(convertDate(selectTimeRange.value[1])))
      queryParams.timeRange = [selectedStartTime, selectedEndTime];
}
    // 时间段清空后值会变成null 此时搜索不能带上时间段
    if(start.value == null){
      queryParams.timeRange = undefined
    }
    if (ip.value != undefined){
      queryParams.ipArray = [ip.value];
    }
    // queryParams.ipArray = [ip.value];
    const data = await EnergyConsumptionApi.getEQDataPage(queryParams)
    eqData.value = data.list.map((item) => formatEQ(item.eq_value, 1));
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
  // params.forEach(function(item) {
  tooltipContent += '网络地址：'+list.value[item.dataIndex].location + '  '
  // 添加条件判断
  if (queryParams.type == 'outlet') {
      tooltipContent += '输出位：' + list.value[item.dataIndex].outlet_id;
  }
  tooltipContent += '<br/>'+ item.marker + '记录日期：'+formatTime(null, null, list.value[item.dataIndex].create_time) + ' ' + item.seriesName + ': ' + item.value +'kWh <br/>'                 
                    +item.marker +'结束日期：'+formatTime(null, null, list.value[item.dataIndex].end_time) +  ' 结束电能：'+formatEle(null, null, list.value[item.dataIndex].end_ele) + 'kWh <br/>' 
                    +item.marker +'开始日期：'+formatTime(null, null, list.value[item.dataIndex].start_time) + ' 开始电能：'+formatEle(null, null, list.value[item.dataIndex].start_ele) + 'kWh <br/>'
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
  return dayjs(cellValue).format('YYYY-MM-DD')
}

// 格式化日期(表格列的时间去掉时分秒和年)
function formatTime1(_row: any, _column: any, cellValue: number): string {
  if (!cellValue) {
    return ''
  }
  return dayjs(cellValue).format('MM-DD')
}

// 格式化电能列数据，保留1位小数（不用传参）
function formatEle(_row: any, _column: any, cellValue: number): string {
  return Number(cellValue).toFixed(1);
}

function formatPowerEle(_row: any, _column: any, cellValue: number): string {
   let numberele;
   if(queryParams.granularity == "day"){
       numberele = _row.end_ele  -  _row.start_ele;
      if(numberele < 0){
          numberele = _row.end_ele;
      }
   }else{
       numberele = _row.end_ele  -  _row.start_ele;
   }
   return Number(numberele).toFixed(1);
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
const getTypeMaxValue = async () => {
    const data = await HistoryDataApi.getTypeMaxValue()
    const outletIdMaxValue = data.outlet_id_max_value;
    const typeSelectionValue  = [
    {
      value: "total",
      label: '总'
    },
    {
      value: "outlet",
      label: '输出位',
      children: (() => {
        const outlets: { value: any; label: string; }[] = [];
        outlets.push({ value: undefined, label: '全部' },)
        for (let i = 1; i <= outletIdMaxValue; i++) {
          outlets.push({ value: `${i}`, label: `${i}` });
        }
        return outlets;
      })(),
    },
  ]
  typeSelection.value = typeSelectionValue;
}

// 导航栏选择后触发
const handleCheck = async (node) => {
  console.log("node",node)
    let arr = [] as any
    node.forEach(item => { 
      if(item.type == 4){
        arr.push(item.unique);
      }
    });
    // if (ip.value != null ){
    //   arr =[ip]
    // }
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
// const getNavList = async() => {
//   const res = await CabinetApi.getRoomList({})
//   let arr = [] as any
//   for (let i=0; i<res.length;i++){
//   var temp = await CabinetApi.getRoomPDUList({id : res[i].id})
//   arr = arr.concat(temp);
//   }
//   navList.value = arr
// }
const getNavList = async() => {
  let arr = [] as any
  var temp = await CabinetApi.getRoomPDUList()
  arr = arr.concat(temp);
  navList.value = arr
}

// 获取导航的数据显示
const getNavNewData = async() => {
  const res = await EnergyConsumptionApi.getNavNewData({})
  lastDayTotalData.value = res.day
  lastWeekTotalData.value = res.week
  lastMonthTotalData.value = res.month
}

/** 详情操作*/
const toDetails = (pduId: number, address: string, location: string) => {
  push('/pdu/nenghao/ecdistribution?pduId='+pduId+'&address='+address+"&location="+location+
  (selectTimeRange.value!=null&&selectTimeRange.value.length==2?'&start='+selectTimeRange.value[0]+'&end='+selectTimeRange.value[1]:''));
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
    const data = await EnergyConsumptionApi.exportEQPageData(queryParams, axiosConfig)
    await download.excel(data, 'PDU能耗数据.xlsx')
  } catch (error) {
    // 处理异常
    console.error('导出失败：', error)
  } finally {
    exportLoading.value = false
  }
}

const start = ref('')
const end = ref('')
const ip =  ref('')

/** 初始化 **/
onMounted(() => {
  getNavList()
  getNavNewData()
  getTypeMaxValue();
  start.value = useRoute().query.start as string;
  end.value = useRoute().query.end as string;
  ip.value = useRoute().query.ip as string;
  getLists();
  
  // if (start.value != null){
  // 	console.log('详情页', start);
	// console.log('详情页1', ip);
  // getLists();
  // }
});

/** 清空按钮操作 */
const clearQuery = () => {
  end.value= '';
  start.value='';
  ip.value='';
  queryParams.timeRange = undefined;
  queryParams.ipArray = undefined;
}

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
  /* ::v-deep .el-table th,
   ::v-deep .el-table td{
    border-right: none;
   } */
</style>
