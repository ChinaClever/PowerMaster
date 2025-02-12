<template>
  <CommonMenu :dataList="navList" @check="handleCheck" navTitle="机架电力数据">
    <template #NavInfo>
      <br/>    <br/> 
      <div class="nav_data">
        <!-- <div class="carousel-container"> -->
          <!-- <el-carousel :interval="2500" motion-blur height="150px" arrow="never" trigger="click">
            <el-carousel-item v-for="(item, index) in carouselItems" :key="index">
              <img width="auto" height="auto" :src="item.imgUrl" alt="" class="carousel-image" />
            </el-carousel-item>
          </el-carousel> -->
        <!-- </div>
        <div class="nav_header">
          <br/>
          <span v-if="queryParams.granularity == 'realtime' ">全部机架最近一分钟新增记录</span>
          <span v-if="queryParams.granularity == 'hour' ">全部机架最近一小时新增记录</span>
          <span v-if="queryParams.granularity == 'day' ">全部机架最近一天新增记录</span>
        </div>
        <div class="nav_content" >
          <el-descriptions title="" direction="vertical" :column="1" border >
            <el-descriptions-item label=""><span >{{ navTotalData }} 条</span></el-descriptions-item>
          </el-descriptions>
        </div> -->

        <div class="descriptions-container" style="font-size: 14px;">
          <div class="description-item">
            <span class="label" >总数据 :</span>
            <span class="value">{{ navTotalData }}条</span>
          </div>
          <div style="text-align: center">
            <div v-if="queryParams.granularity == 'realtime' " style="text-align: center"><span>全部PDU最近一分钟新增记录</span></div>
              <div v-if="queryParams.granularity == 'hour' " style="text-align: center"><span>全部PDU最近一小时新增记录</span></div>
              <div v-if="queryParams.granularity == 'day' " style="text-align: center"><span>全部PDU最近一天新增记录</span></div>
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
          label-width="80px"
        >
          <el-form-item label="颗粒度" prop="type">
            <el-select
              v-model="queryParams.granularity"
              @change="granularityChange"
              placeholder="请选择分钟/小时/天"
              class="!w-120px" >
              <el-option label="分钟" value="realtime" />
              <el-option label="小时" value="hour" />
              <el-option label="天" value="day" />
            </el-select>
          </el-form-item>

        <el-form-item label="时间段" prop="timeRange">
            <el-date-picker
            value-format="YYYY-MM-DD HH:mm:ss"
            v-model="selectTimeRange"
            type="datetimerange"
            :shortcuts="shortcuts"
            range-separator="-"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
          />
          </el-form-item>

          <el-form-item label="筛选列">
          <el-cascader
            v-model="defaultOptionsCol"
            :options="optionsCol"
            :props="props"
            collapse-tags
            collapse-tags-tooltip
            :show-all-levels="false"
            @change="cascaderChange"
            class="!w-210px"
          />
          </el-form-item>

          <el-form-item >
            <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
            <el-button
              type="success"
              plain
              @click="handleExport"
              :loading="exportLoading"
            >
              <Icon icon="ep:download" class="mr-5px" /> 导出
            </el-button>
          </el-form-item>
        </el-form>
      </template>
      <template #Content>
        <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true" :border="true">
        <!-- 添加行号列 -->
        <el-table-column label="序号" align="center" width="80px">
          <template #default="{ $index }">
            {{ $index + 1 + (queryParams.pageNo - 1) * queryParams.pageSize }}
          </template>
        </el-table-column>
          <!-- 遍历其他列 -->
          <template v-for="column in tableColumns">
            <el-table-column :key="column.prop" :label="column.label" :align="column.align" :prop="column.prop" :formatter="column.formatter" :width="column.width" v-if="column.istrue" >
              <template #default="{ row }" v-if="column.slot === 'actions'">
                <el-button link type="primary" @click="toDetails(row.rack_id, row.location, row.rack_name)">详情</el-button>
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
            @pagination="getList"
          />
          <div class="realTotal">共 {{ realTotel }} 条</div>
     </template>
  </CommonMenu>
</template>

<script setup lang="ts">
import download from '@/utils/download'
import { HistoryDataApi } from '@/api/rack/historydata'
import { CabinetApi } from '@/api/cabinet/info'
import { IndexApi } from '@/api/rack/index'
import dayjs from 'dayjs'
import { formatDate, endOfDay, convertDate, addTime} from '@/utils/formatTime'

const { push } = useRouter()
/** 机架历史数据 列表 */
defineOptions({ name: 'CabinetHistoryData' })

const navList = ref([]) as any // 左侧导航栏树结构列表
const navTotalData = ref(0)
const message = useMessage() // 消息弹窗
const loading = ref(true) // 列表的加载中
const list = ref<Array<{ }>>([]); // 列表数据
const total = ref(0) // 数据条数 超过10000条为10000
const realTotel = ref(0) // 数据的真实条数
const pageSizeArr = ref([15,30,50,100])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 15,
  granularity: 'realtime',
  timeRange: undefined,
  rackIds:[]
})
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中
const selectTimeRange = ref(undefined)


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
    text: '最近一天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setDate(start.getDate() - 1)
      return [start, end]
    },
  },
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

//筛选选项
const props = { multiple: true}
const defaultOptionsCol = ref([["active_total"], [ "apparent_total"]])
const optionsCol = ref([
  { value: "active_total", label: '有功功率' },
  { value: "apparent_total", label: '视在功率' },
])
const originalArray = ref(["active_total", "apparent_total"])

// 处理筛选列变化
const cascaderChange = (selectedCol) => {
  let selectedCol1 = selectedCol.map((arr) => arr[arr.length - 1]);
  let notSelectedCol = originalArray.value.filter(item => !selectedCol1.includes(item));
  tableColumns.value.forEach(column => {
    for (const col of selectedCol1) {
      if (column.prop?.startsWith(col)){
        column.istrue = true;
        break;
      }
    };     
    for (const col of notSelectedCol) {
      if (column.prop?.startsWith(col)){
        column.istrue = false;
        break;
      }
    }
  });
}

// 处理颗粒度筛选变化 有变化重新获取导航栏显示的新增记录
const granularityChange = () => {
   getNavNewData()
}

watch(() => queryParams.granularity, (newValues) => { 
    const newGranularity = newValues;
    if ( newGranularity == 'realtime'){
      defaultOptionsCol.value =[["active_total"], [ "apparent_total"]]
      optionsCol.value = [
        { value: "active_total", label: '有功功率' },
        { value: "apparent_total", label: '视在功率' },
      ]
      originalArray.value = ["active_total", "apparent_total"]        
 
      tableColumns.value =[
        { label: '位置', align: 'center', prop: 'location', istrue:true},
        { label: '机架名', align: 'center', prop: 'rack_name', istrue:true},
        { label: '有功功率(kW)', align: 'center', prop: 'active_total' , istrue:true, formatter: formatPower},
        { label: '视在功率(kVA)', align: 'center', prop: 'apparent_total' , istrue:true, formatter: formatPower},
        { label: '时间', align: 'center', prop: 'create_time', formatter: formatTime, istrue:true},
        { label: '操作', align: 'center', slot: 'actions' , istrue:true},
      ]
      queryParams.pageNo = 1;
      queryParams.pageSize = 15;
      getList();
    }else{
      defaultOptionsCol.value = [
        ["active_pow", "active_total_avg_value"],["active_pow", "active_total_max"],["active_pow", "active_total_min"],
        ["apparent_pow", "apparent_total_avg_value"],["apparent_pow", "apparent_total_max"],["apparent_pow", "apparent_total_min"]
      ]
      optionsCol.value = [
        { value: "active_pow", label: '有功功率', children:[
            { value: "active_total_avg_value", label: '平均有功功率' },
            { value: "active_total_max", label: '最大有功功率' },
            { value: "active_total_min", label: '最小有功功率' }
          ] 
        },
        { value: "apparent_pow", label: '视在功率', children:[
            { value: "apparent_total_avg_value", label: '平均视在功率' },
            { value: "apparent_total_max", label: '最大视在功率' },
            { value: "apparent_total_min", label: '最小视在功率' }
          ] 
        },
      ] as any;
      originalArray.value = ["active_total_avg_value", "active_total_max", "active_total_min", "apparent_total_avg_value", "apparent_total_max", "apparent_total_min"]    

      tableColumns.value = [
        { label: '位置', align: 'center', prop: 'location' , width: '160px' , istrue:true},
        { label: '机架名', align: 'center', prop: 'rack_name', width: '160px', istrue:true},
        { label: '记录时间', align: 'center', prop: 'create_time' , width: '230px',  formatter: formatTime,istrue:true},
        { label: '平均有功功率(kW)', align: 'center', prop: 'active_total_avg_value', istrue:true, width: '180px', formatter: formatPower},
        { label: '最大有功功率(kW)', align: 'center', prop: 'active_total_max_value', istrue:true, width: '180px', formatter: formatPower},
        { label: '最大有功功率时间', align: 'center', prop: 'active_total_max_time', formatter: formatTime, width: '200px', istrue:true},
        { label: '最小有功功率(kW)', align: 'center', prop: 'active_total_min_value', istrue:true, width: '180px', formatter: formatPower},
        { label: '最小有功功率时间', align: 'center', prop: 'active_total_min_time', formatter: formatTime, width: '200px', istrue:true},
        { label: '平均视在功率(kVA)', align: 'center', prop: 'apparent_total_avg_value', istrue:true, width: '180px', formatter: formatPower},
        { label: '最大视在功率(kVA)', align: 'center', prop: 'apparent_total_max_value', istrue:true, width: '180px', formatter: formatPower},
        { label: '最大视在功率时间', align: 'center', prop: 'apparent_total_max_time', formatter: formatTime, width: '200px', istrue:true},
        { label: '最小视在功率(kVA)', align: 'center', prop: 'apparent_total_min_value', istrue:true, width: '180px', formatter: formatPower},
        { label: '最小视在功率时间', align: 'center', prop: 'apparent_total_min_time', formatter: formatTime, width: '200px', istrue:true},
        { label: '操作', align: 'center', slot: 'actions', istrue:true},
      ];
      queryParams.pageNo = 1;
      queryParams.pageSize = 15;
      getList();
    }
    
});

const tableColumns = ref([
  { label: '位置', align: 'center', prop: 'location', istrue:true},
  { label: '机架名', align: 'center', prop: 'rack_name', istrue:true},
  { label: '有功功率(kW)', align: 'center', prop: 'active_total' , istrue:true, formatter: formatPower},
  { label: '视在功率(kVA)', align: 'center', prop: 'apparent_total' , istrue:true, formatter: formatPower},
  { label: '时间', align: 'center', prop: 'create_time', formatter: formatTime, istrue:true},
  { label: '操作', align: 'center', slot: 'actions' , istrue:true},
]) as any;


/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    if ( selectTimeRange.value != undefined){
      // 格式化时间范围 加上23:59:59的时分秒 
      const selectedStartTime = formatDate(endOfDay(convertDate(selectTimeRange.value[0])))
      // 结束时间的天数多加一天 ，  一天的毫秒数
      const oneDay = 24 * 60 * 60 * 1000;
      const selectedEndTime = formatDate(endOfDay(convertDate(selectTimeRange.value[1])))
      selectTimeRange.value = [selectedStartTime, selectedEndTime]
      queryParams.timeRange = [selectedStartTime, selectedEndTime];
    }


    const data = await HistoryDataApi.getHistoryDataPage(queryParams)
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

// 格式化功率列数据，保留三位小数
function formatPower(_row: any, _column: any, cellValue: number): string {
  return Number(cellValue).toFixed(3);
}

// 最后一页显示数据量过大的提示
const shouldShowDataExceedMessage = computed(() => {
  const lastPageNo = Math.ceil(total.value / queryParams.pageSize);
  return queryParams.pageNo === lastPageNo && total.value >= 10000;
});

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

// 导航栏选择后触发
const handleCheck = async (node) => {
  let arr = [] as any
  node.forEach(item => { 
    if(item.type == 5){
      arr.push(item.id);
    }
  });
  //没筛选到 不显示任何数据 参数传0 后端返回空
  if(arr.length == 0 && node.length != 0){
    arr.push(0)
    ElMessage({
      message: '暂无数据',
      type: 'warning',
    });
  }
  queryParams.rackIds = arr
  handleQuery()
}

// 接口获取机房导航列表
const getNavList = async() => {
  const res = await CabinetApi.getRoomList({})
  let arr = [] as any
  for (let i=0; i<res.length;i++){
  var temp = await IndexApi.getRackAll({id : res[i].id})
  arr = arr.concat(temp);
  }
  navList.value = arr
}

// 获取导航的数据显示
const getNavNewData = async() => {
  const res = await HistoryDataApi.getNavNewData(queryParams.granularity)
  navTotalData.value = res.total
}

/** 详情操作*/
const toDetails = (rackId: number, location:string, rackName:string) => {
  location += '-'+rackName
  push('/u/record/historyLine?rackId='+rackId+'&location='+location);
}
// 格式化日期
function formatTime(_row: any, _column: any, cellValue: number): string {
  if (!cellValue) {
    return ''
  }
  return dayjs(cellValue).format('YYYY-MM-DD HH:mm')
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
    const data = await HistoryDataApi.exportHistoryData(queryParams, axiosConfig)
    await download.excel(data, '机架电力历史数据.xlsx')
  } catch (error) {
    // 处理异常
    console.error('导出失败：', error)
  } finally {
    exportLoading.value = false
  }
}

const format = (date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

/** 初始化 **/
onMounted(() => {
  const now = new Date()
      const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
   // 使用上述自定义的 format 函数将日期对象转换为指定格式的字符串
selectTimeRange.value = [
  format(startOfMonth),
  format(now)
];
  getNavList()
  getNavNewData()
  getList()
})
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
.nav_header {
    display: flex;
    flex-direction: column;
    align-items: center;
    font-size: 15px; 
    font-weight: bold;
  }


.nav_data{
  padding-left: 7px;
  width: 200px;
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

  ::v-deep .el-table .el-table__header th {
    background-color: #F5F7FA;
    color: #909399;
}
</style>