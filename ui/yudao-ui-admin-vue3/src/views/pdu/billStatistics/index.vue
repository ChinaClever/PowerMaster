<template>
  <CommonMenu :dataList="navList" @check="handleCheck" navTitle="PDU电费统计" placeholder="如:192.168.1.96-0">
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
          <el-descriptions title="全部PDU新增电费记录" direction="vertical" :column="1" border >
            <el-descriptions-item label="最近一天"><span >{{ lastDayTotalData }} 条</span></el-descriptions-item>
            <el-descriptions-item label="最近一周"><span >{{ lastWeekTotalData }} 条</span></el-descriptions-item>
            <el-descriptions-item label="最近一月" ><span >{{ lastMonthTotalData }} 条</span></el-descriptions-item>
          </el-descriptions>
        </div>
      </div> -->

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
    </div>
    <div style="text-align: center"><span>全部PDU新增电费统计</span>
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
          <el-button type="success" plain @click="handleExport" :loading="exportLoading">
            <Icon icon="ep:download" class="mr-5px" /> 导出
          </el-button>
        </el-form-item>
      </el-form>
    </template>
    <template #Content>
      <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
        <!-- 添加行号列 -->
        <el-table-column label="序号" align="center" width="80px" >
          <template #default="{ $index }">
            {{ $index + 1 + (queryParams.pageNo - 1) * queryParams.pageSize }}
          </template>
        </el-table-column>
        <!-- 遍历其他列 -->  
        <template v-for="column in tableColumns">
          <el-table-column :key="column.prop" :label="column.label" :align="column.align" :prop="column.prop" :formatter="column.formatter" :width="column.width" v-if="column.istrue">
            <template #default="{ row }" v-if="column.slot === 'actions' && queryParams.granularity == 'day'">
              <el-button link type="primary" v-if="row.bill_mode_real && row.bill_mode_real == 2 && queryParams.type == 'total'" @click="showDetails(row.pdu_id, row.start_time, row.location, row.end_time)">分段计费</el-button>
              <el-button link type="primary" v-else-if="row.bill_mode_real && row.bill_mode_real == 2 && queryParams.type == 'outlet'" @click="showDetails(row.pdu_id, row.outlet_id, row.start_time, row.location, row.end_time)">分段计费</el-button>
              <div v-else>固定计费</div>
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
    </template>
  </CommonMenu>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import download from '@/utils/download'
import { EnergyConsumptionApi } from '@/api/pdu/energyConsumption'
import { HistoryDataApi } from '@/api/pdu/historydata'
import { formatDate, endOfDay, convertDate, addTime} from '@/utils/formatTime'
import { CabinetApi } from '@/api/cabinet/info'
import PDUImage from '@/assets/imgs/PDU.jpg';
import { ElMessage } from 'element-plus'
defineOptions({ name: 'BillStatistics' })

const navList = ref([]) as any // 左侧导航栏树结构列表
const lastDayTotalData = ref(0)
const lastWeekTotalData = ref(0)
const lastMonthTotalData = ref(0)
const loading = ref(true)
const dialogVisible = ref(false)
const dialogTableData = ref<Array<{ }>>([]) as any; 
const dialogTitle = ref('')
const dialogTimeRange = ref('')
const message = useMessage() // 消息弹窗
const list = ref<Array<{ }>>([]) as any; 
const total = ref(0)
const realTotel = ref(0) // 数据的真实总条数
const selectTimeRange = ref(undefined)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 15,
  outletId: undefined,
  type: 'total',
  granularity: 'day',
  timeRange: undefined as string[] | undefined,
  ipArray: [],
})
const pageSizeArr = ref([15,30,50,100])
const queryFormRef = ref()
const exportLoading = ref(false)
const carouselItems = ref([
      { imgUrl: PDUImage},
      { imgUrl: PDUImage},
      { imgUrl: PDUImage},
      { imgUrl: PDUImage},
    ]);//侧边栏轮播图图片路径
// 日期段快捷选项
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
    queryParams.outletId = selected[1]
  }else{
    queryParams.outletId = undefined
  }
  handleQuery();
}


watch(() => [queryParams.granularity, queryParams.type], () => {
  if (queryParams.type === 'outlet'){
    if (queryParams.granularity == 'day'){
      tableColumns.value = [
        { label: '位置', align: 'center', prop: 'address' , istrue:true},
        { label: '输出位', align: 'center', prop: 'outlet_id' , istrue:true}, 
        { label: '网络地址', align: 'center', prop: 'location' , istrue:true},
        { label: '日期', align: 'center', prop: 'start_time', formatter: formatTime, istrue:true},
        { label: '耗电量(kWh)', align: 'center', prop: 'eq_value' , istrue:true, formatter: formatEle},
        { label: '电费(元)', align: 'center', prop: 'bill_value' , istrue:true, formatter: formatEle},
        { label: '计费方式', align: 'center', slot: 'actions' , istrue:true},
      ]
    }else{
       tableColumns.value = [
        { label: '位置', align: 'center', prop: 'address' , istrue:true},
        { label: '输出位', align: 'center', prop: 'outlet_id' , istrue:true}, 
        { label: '网络地址', align: 'center', prop: 'location' , istrue:true},
        { label: '开始日期', align: 'center', prop: 'start_time', formatter: formatTime, istrue:true},
        { label: '结束日期', align: 'center', prop: 'end_time', formatter: formatTime, istrue:true},
        { label: '耗电量(kWh)', align: 'center', prop: 'eq_value' , istrue:true, formatter: formatEle},
        { label: '电费(元)', align: 'center', prop: 'bill_value' , istrue:true, formatter: formatEle},
      ]
    }
  }else{
    if (queryParams.granularity == 'day'){
      tableColumns.value = [
        { label: '位置', align: 'center', prop: 'address' , istrue:true},
        { label: '网络地址', align: 'center', prop: 'location' , istrue:true},
        { label: '日期', align: 'center', prop: 'start_time' , formatter: formatTime, width: '200px' , istrue:true},
        { label: '耗电量(kWh)', align: 'center', prop: 'eq_value' , istrue:true, formatter: formatEle},
        { label: '电费(元)', align: 'center', prop: 'bill_value' , istrue:true, formatter: formatBill},
        { label: '计费方式', align: 'center', slot: 'actions' , istrue:true},
      ]
    }else{
      tableColumns.value = [
        { label: '位置', align: 'center', prop: 'address' , istrue:true},
        { label: '网络地址', align: 'center', prop: 'location' , istrue:true},
        { label: '开始日期', align: 'center', prop: 'start_time', formatter: formatTime, istrue:true},
        { label: '结束日期', align: 'center', prop: 'end_time', formatter: formatTime, istrue:true},
        { label: '耗电量(kWh)', align: 'center', prop: 'eq_value' , istrue:true, formatter: formatEle},
        { label: '电费(元)', align: 'center', prop: 'bill_value' , istrue:true, formatter: formatEle},
      ]
    }
  }
  handleQuery();
});

const tableColumns = ref([
  { label: '位置', align: 'center', prop: 'address' , istrue:true},
  { label: '网络地址', align: 'center', prop: 'location' , istrue:true},
  { label: '日期', align: 'center', prop: 'start_time' , formatter: formatTime, width: '200px' , istrue:true},
  { label: '耗电量(kWh)', align: 'center', prop: 'eq_value' , istrue:true, formatter: formatEle},
  { label: '电费(元)', align: 'center', prop: 'bill_value' , istrue:true, formatter: formatBill},
  { label: '计费方式', align: 'center', slot: 'actions' , istrue:true},
]) as any;

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    if ( selectTimeRange.value != undefined){
      // 格式化日期范围 加上23:59:59的时分秒 
      const selectedStartTime = formatDate(endOfDay(convertDate(selectTimeRange.value[0])))
      // 结束日期的天数多加一天 ，  一天的毫秒数
      const oneDay = 24 * 60 * 60 * 1000;
      const selectedEndTime = formatDate(endOfDay(addTime(convertDate(selectTimeRange.value[1]), oneDay )))
      queryParams.timeRange = [selectedStartTime, selectedEndTime];
    }
  
    const data = await EnergyConsumptionApi.getBillDataPage(queryParams)
    list.value = data.list
    realTotel.value = data.total
    if (data.total > 10000){
      total.value = 10000
    }else{
      total.value = data.total
    }
  } finally {
    loading.value = false
  }
}

// 最后一页显示数据量过大的提示
const shouldShowDataExceedMessage = computed(() => {
  const lastPageNo = Math.ceil(total.value / queryParams.pageSize);
  return queryParams.pageNo === lastPageNo && total.value >= 10000;
});

// 格式化日期
function formatTime(_row: any, _column: any, cellValue: number): string {
  if (!cellValue) {
    return ''
  }

  return dayjs(cellValue).format('YYYY-MM-DD')
}

// 格式化电能列数据，保留1位小数
function formatEle(_row: any, _column: any, cellValue: number): string {
  return Number(cellValue).toFixed(1);
}

// 格式化电费列数据
function formatBill(_row: any, _column: any, cellValue: number): string {
  return Number(cellValue).toFixed(1);
}

// 禁选未来的日期
const disabledDate = (date) => {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  // 设置date的日期为0时0分0秒，以便与today进行比较
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
    let arr = [] as any
    node.forEach(item => { 
      if(item.type == 4){
        arr.push(item.unique);
      }
    });
   //没筛选到pdu 不显示任何数据 ipArray参数传0 后端返回空
   if(arr.length == 0 && node.length != 0){
      arr.push(0)
      ElMessage({
        message: '暂无数据',
        type: 'warning',
      });
    }
    queryParams.ipArray = arr
    handleQuery()
   
}

// 接口获取机房导航列表
const getNavList = async() => {
  const res = await CabinetApi.getRoomList({})
  let arr = [] as any
  for (let i=0; i<res.length;i++){
  var temp = await CabinetApi.getRoomPDUList({id : res[i].id})
  arr = arr.concat(temp);
  }
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
const showDetails = async (pduId: number, startTime:string, location:string, endTime: string, outletId?: number) => {
  dialogTableData.value = []
  dialogTitle.value = ''
  dialogTimeRange.value = ''
  let params = {}
  if (queryParams.type == 'total'){
    params = {
      pduId: pduId,
      startTime: startTime,
      type: queryParams.type
    }
  }else{
    params = {
      pduId: pduId,
      outletId: outletId,
      startTime: startTime,
      type: queryParams.type
    }
  }
  const data = await EnergyConsumptionApi.getSubBillDetails(params)
  dialogTableData.value = data.list
  dialogTitle.value = location
  dialogTimeRange.value = startTime + ' - ' + endTime
  dialogVisible.value = true

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
    const data = await EnergyConsumptionApi.exportBillPageData(queryParams, axiosConfig)
    await download.excel(data, 'PDU电费统计.xlsx')
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
  getNavNewData()
  getTypeMaxValue();
  getList();
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
.description-item {
  display: flex;
  align-items: center;
}

.label {
  width:100px; /* 控制冒号前的宽度 */
  text-align: right; /* 文本右对齐 */
  margin-right: 20px; /* 控制冒号后的间距 */
}

.value {
  flex: 1; /* 自动扩展以对齐数据 */
}
  .line {
    height: 1px;
    margin-top: 28px;

    background: linear-gradient(297deg, #fff, #dcdcdc 51%, #fff);
  }

</style>