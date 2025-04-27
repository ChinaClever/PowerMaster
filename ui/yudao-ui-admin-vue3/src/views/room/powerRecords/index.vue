<template>
  <CommonMenu :dataList="navList" @check="handleCheck" navTitle="机房电能记录">
    <template #NavInfo>
      <br/>
      <div class="nav_data">
        <div class="carousel-container">
          <!-- <el-carousel :interval="2500" motion-blur height="150px" arrow="never" trigger="click">
            <el-carousel-item v-for="(item, index) in carouselItems" :key="index">
              <img width="auto" height="auto" :src="item.imgUrl" alt="" class="carousel-image" />
            </el-carousel-item>
          </el-carousel> -->
        </div>
        <div class="nav_content">
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
          </div>
          <!-- <div class="nav_content1" >
            <span class="label">全部机房最近一天新增记录</span>
          </div>
           <div class="description-item">
            <span class="label">电能 :</span>
            <span class="value">{{ navTotalData }} 条</span>
          </div>  -->
          <!-- <el-descriptions title="全部机房最近一天新增记录" direction="vertical" :column="1" width="80px" border >
            <el-descriptions-item label="电能" >{{ navTotalData }}条</el-descriptions-item>
          </el-descriptions> -->
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
      <el-form-item label="时间段" prop="timeRange">
        <el-date-picker
        value-format="YYYY-MM-DD HH:mm:ss"
        v-model="queryParams.timeRange"
        type="datetimerange"
        :shortcuts="shortcuts"
        range-separator="-"
        start-placeholder="开始时间"
        end-placeholder="结束时间"
        :disabled-date="disabledDate"
      />
      </el-form-item>

      <el-form-item >
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
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
          class="!w-180px"
        />
        </el-form-item>
        
        <el-form-item style="position: absolute; right: 0;">
          <el-button type="success" plain :loading="exportLoading" @click="handleExport">
            <Icon icon="ep:download" class="mr-5px" /> 导出
          </el-button>
        </el-form-item>
      </el-form>
    </template>
    <template #Content>
      <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true" :border="true" :header-cell-style="headCellStyle">
        <!-- 添加行号列 -->
        <el-table-column label="序号" align="center" width="80px">
          <template #default="{ $index }">
            {{ $index + 1 + (queryParams.pageNo - 1) * queryParams.pageSize }}
          </template>
        </el-table-column>
        <!-- 遍历其他列 -->  
        <template v-for="column in tableColumns">
          <el-table-column :key="column.prop" :label="column.label" :align="column.align" :prop="column.prop" :formatter="column.formatter" v-if="column.istrue"/>
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
      <div class="realTotal">共 {{ realTotel }} 条</div>
     </template>
  </CommonMenu>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import download from '@/utils/download'
import { EnergyConsumptionApi } from '@/api/room/energyConsumption'
import { IndexApi } from '@/api/room/roomindex'
import { last } from 'lodash-es'
// import PDUImage from '@/assets/imgs/PDU.jpg';
defineOptions({ name: 'PowerRecords' })

const navList = ref([]) as any // 左侧导航栏树结构列表
const navTotalData = ref(0)
const loading = ref(true)
const message = useMessage() // 消息弹窗
const list = ref<Array<{ }>>([]) as any; 
const total = ref(0)
const realTotel = ref(0) // 数据的真实总条数
const lastDayTotalData=ref()
const lastWeekTotalData=ref()
const lastMonthTotalData=ref()
let now=new Date()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 15,
  timeRange: [dayjs(new Date(now.getFullYear(), now.getMonth(), 1,0,0,0)).format("YYYY-MM-DD HH:mm:ss"),dayjs(now).format("YYYY-MM-DD HH:mm:ss")],
  roomIds: [],
})
const pageSizeArr = ref([15,30,50,100])
const queryFormRef = ref()
const exportLoading = ref(false)
// const carouselItems = ref([
//       { imgUrl: PDUImage},
//       { imgUrl: PDUImage},
//       { imgUrl: PDUImage},
//       { imgUrl: PDUImage},
//     ]);//侧边栏轮播图图片路径
// 时间段快捷选项
const shortcuts = [
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

//筛选选项
const props = { multiple: true}
const defaultOptionsCol = ref([])
const optionsCol = ref([
  { value: "ele_a", label: 'A路电能'},
  { value: "ele_b", label: 'B路电能'},
])
const originalArray = ref([ "ele_a", "ele_b"])

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


const tableColumns = ref([
  { label: '位置', align: 'center', prop: 'location' , istrue:true},
  { label: '记录时间', align: 'center', prop: 'create_time', formatter: formatTime, istrue:true},
  { label: 'A路电能(kWh)', align: 'center', prop: 'ele_a' , istrue:false, formatter: formatEle},
  { label: 'B路电能(kWh)', align: 'center', prop: 'ele_b' , istrue:false, formatter: formatEle},
  { label: '总电能(kWh)', align: 'center', prop: 'ele_total' , istrue:true, formatter: formatEle},
]);

/** 初始化数据 */
const getList = async () => {
  loading.value = true
  try {
    const data = await EnergyConsumptionApi.getRealtimeEQDataPage(queryParams)
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


// 格式化电能列数据，保留1位小数
function formatEle(_row: any, _column: any, cellValue: number): string {
  return Number(cellValue).toFixed(1);
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

// 格式化日期
function formatTime(_row: any, _column: any, cellValue: number): string {
  if (!cellValue) {
    return ''
  }
  return dayjs(cellValue).format('YYYY-MM-DD HH:mm:ss')
}


// 导航栏选择后触发
const handleCheck = async (node) => {
  let arr = [] as any
  node.forEach(item => arr.push(item.id));
  queryParams.roomIds = arr
  handleQuery()
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
    const data = await EnergyConsumptionApi.exportRealtimeEQPageData(queryParams, axiosConfig)
    await download.excel(data, '机房电能记录.xlsx')
  } catch (error) {
    // 处理异常
    console.error('导出失败：', error)
  } finally {
    exportLoading.value = false
  }
}

// 接口获取机房导航列表
const getNavList = async() => {
  const res = await IndexApi.getRoomList()
  // navList.value = res
  navList.value=res.map((item)=>{return {id:item.id,name:item.roomName,children:[]}})
}

// 获取导航的数据显示
const getNavData = async() => {
  const res = await EnergyConsumptionApi.getNavNewData({})
  lastDayTotalData.value = res.day
  lastWeekTotalData.value = res.week
  lastMonthTotalData.value = res.month
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 初始化 **/
onMounted(() => {
  getNavList()
  getNavData()
  getList();
})

function headCellStyle(){
  return {
    backgroundColor: 'rgb(245, 247, 250)',
  }
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
/* .nav_content1{
  text-align: center;
} */
.nav_content1 span{
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
</style>
