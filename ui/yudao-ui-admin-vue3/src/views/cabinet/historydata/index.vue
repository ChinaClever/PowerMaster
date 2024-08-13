<template>
  <CommonMenu :dataList="navList" @check="handleCheck" navTitle="机柜电力数据">
    <template #NavInfo>
      <br/>    <br/> 
      <div class="nav_data">
        <div class="carousel-container">
          <el-carousel :interval="2500" motion-blur height="150px" arrow="never" trigger="click">
            <el-carousel-item v-for="(item, index) in carouselItems" :key="index">
              <img width="auto" height="auto" :src="item.imgUrl" alt="" class="carousel-image" />
            </el-carousel-item>
          </el-carousel>
        </div>
        <div class="nav_header">
          <br/>
          <span v-if="queryParams.granularity == 'realtime' ">全部机柜最近一分钟新增记录</span>
          <span v-if="queryParams.granularity == 'hour' ">全部机柜最近一小时新增记录</span>
          <span v-if="queryParams.granularity == 'day' ">全部机柜最近一天新增记录</span>
        </div>
        <div class="nav_content" >
          <el-descriptions title="" direction="vertical" :column="1" border >
            <el-descriptions-item label=""><span >{{ navTotalData }} 条</span></el-descriptions-item>
          </el-descriptions>
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
          <el-form-item label="颗粒度" prop="type">
            <el-select
              v-model="queryParams.granularity"
              placeholder="请选择分钟/小时/天"
              @change="granularityChange"
              class="!w-120px" >
              <el-option label="分钟" value="realtime" />
              <el-option label="小时" value="hour" />
              <el-option label="天" value="day" />
            </el-select>
          </el-form-item>

        <el-form-item label="时间段" prop="timeRange">
            <el-date-picker
            value-format="YYYY-MM-DD HH:mm:ss"
            v-model="queryParams.timeRange"
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
        <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
           <!-- 添加行号列 -->
          <el-table-column label="序号" align="center" width="60px">
            <template #default="{ $index }">
              {{ $index + 1 + (queryParams.pageNo - 1) * queryParams.pageSize }}
            </template>
          </el-table-column>
          <!-- 遍历其他列 -->
          <template v-for="column in tableColumns">
            <el-table-column :key="column.prop" :label="column.label" :align="column.align" :prop="column.prop" :formatter="column.formatter" :width="column.width" v-if="column.istrue" >
              <template #default="{ row }" v-if="column.slot === 'actions'">
                <el-button link type="primary" @click="toDetails(row.cabinet_id, row.location)">详情</el-button>
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
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { HistoryDataApi } from '@/api/cabinet/historydata'
import { CabinetApi } from '@/api/cabinet/info'
import PDUImage from '@/assets/imgs/PDU.jpg';
const { push } = useRouter()
/** 机柜历史数据 列表 */
defineOptions({ name: 'CabinetHistoryData' })

const navList = ref([]) as any // 左侧导航栏树结构列表
const navTotalData = ref(0)
const message = useMessage() // 消息弹窗
const loading = ref(true) // 列表的加载中
const list = ref<Array<{ }>>([]); // 列表数据
const total = ref(0) // 数据总条数 超过10000条为10000
const realTotel = ref(0) // 数据的真实总条数
const pageSizeArr = ref([15,30,50,100])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 15,
  granularity: 'realtime',
  timeRange: undefined,
  cabinetIds:[]
})
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中
const carouselItems = ref([
      { imgUrl: PDUImage},
      { imgUrl: PDUImage},
      { imgUrl: PDUImage},
      { imgUrl: PDUImage},
    ]);//侧边栏轮播图图片路径
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
const defaultOptionsCol = ref([["total", "active_total"], ["total", "apparent_total"]])
const optionsCol = ref([
  {
    value: "total",
    label: '总',
    children: [
      { value: "active_total", label: '总有功功率' },
      { value: "apparent_total", label: '总视在功率' },
    ],
  },
  {
    value: "A",
    label: 'A路',
    children: [
      { value: "active_a", label: 'A路有功功率' },
      { value: "apparent_a", label: 'A路视在功率' },
    ],
  },
  {
    value: "B",
    label: 'B路',
    children: [
      { value: "active_b", label: 'B路有功功率' },
      { value: "apparent_b", label: 'B路视在功率' },
    ],
  },
])
const originalArray = ref(["active_total", "apparent_total", "active_a", "apparent_a", "active_b", "apparent_b"])

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
      defaultOptionsCol.value = [
        ["total", "active_total"], ["total", "apparent_total"]
      ]
      optionsCol.value = [
        {
        value: "total",
        label: '总',
        children: [
          { value: "active_total", label: '总有功功率' },
          { value: "apparent_total", label: '总视在功率' },
        ],
        },
        {
          value: "A",
          label: 'A路',
          children: [
            { value: "active_a", label: 'A路有功功率' },
            { value: "apparent_a", label: 'A路视在功率' },
          ],
        },
        {
          value: "B",
          label: 'B路',
          children: [
            { value: "active_b", label: 'B路有功功率' },
            { value: "apparent_b", label: 'B路视在功率' },
          ],
        },
      ]
      originalArray.value = ["active_total", "apparent_total", "active_a", "apparent_a", "active_b", "apparent_b"]        
 
      tableColumns.value =[
        { label: '位置', align: 'center', prop: 'location' , width: '250px', istrue:true},
        { label: '时间', align: 'center', prop: 'create_time', formatter: dateFormatter, width: '200px' , istrue:true},
        { label: '总有功功率(kW)', align: 'center', prop: 'active_total' , istrue:true, formatter: formatPower},
        { label: '总视在功率(kVA)', align: 'center', prop: 'apparent_total' , istrue:true, formatter: formatPower},
        { label: 'A路有功功率(kW)', align: 'center', prop: 'active_a' , istrue:false, formatter: formatPower},
        { label: 'A路视在功率(kVA)', align: 'center', prop: 'apparent_a' , istrue:false, formatter: formatPower},
        { label: 'B路有功功率(kW)', align: 'center', prop: 'active_b' , istrue:false, formatter: formatPower},
        { label: 'B路视在功率(kVA)', align: 'center', prop: 'apparent_b' , istrue:false, formatter: formatPower},
        { label: '操作', align: 'center', slot: 'actions' , istrue:true},
      ]
      queryParams.pageNo = 1;
      queryParams.pageSize = 15;
      getList();
    }else{
      defaultOptionsCol.value = [
        ["total", "active_pow", "active_total_avg_value"], ["total", "active_pow", "active_total_max"], ["total", "active_pow", "active_total_min"],
        ["total", "apparent_pow", "apparent_total_avg_value"], ["total", "apparent_pow", "apparent_total_max"], ["total", "apparent_pow", "apparent_total_min"]
      ]
      optionsCol.value = [
        {
        value: "total",
        label: '总',
        children: [
          { value: "active_pow", label: '有功功率', children:[
              { value: "active_total_avg_value", label: '总平均有功功率' },
              { value: "active_total_max", label: '总最大有功功率' },
              { value: "active_total_min", label: '总最小有功功率' }
            ] 
          },
          { value: "apparent_pow", label: '视在功率', children:[
              { value: "apparent_total_avg_value", label: '总平均视在功率' },
              { value: "apparent_total_max", label: '总最大视在功率' },
              { value: "apparent_total_min", label: '总最小视在功率' }
            ] 
           },
          ],
        },
        {
          value: "A",
          label: 'A路',
          children: [
          { value: "active_pow", label: '有功功率', children:[
              { value: "active_a_avg_value", label: 'A路平均有功功率' },
              { value: "active_a_max", label: 'A路最大有功功率' },
              { value: "active_a_min", label: 'A路最小有功功率' }
            ] 
          },
          { value: "apparent_pow", label: '视在功率', children:[
              { value: "apparent_a_avg_value", label: 'A路平均视在功率' },
              { value: "apparent_a_max", label: 'A路最大视在功率' },
              { value: "apparent_a_min", label: 'A路最小视在功率' }
            ] 
           },
          ],
        },
        {
          value: "B",
          label: 'B路',
          children: [
          { value: "active_pow", label: '有功功率', children:[
              { value: "active_b_avg_value", label: 'B路平均有功功率' },
              { value: "active_b_max", label: 'B路最大有功功率' },
              { value: "active_b_min", label: 'B路最小有功功率' }
            ] 
          },
          { value: "apparent_pow", label: '视在功率', children:[
              { value: "apparent_b_avg_value", label: 'B路平均视在功率' },
              { value: "apparent_b_max", label: 'B路最大视在功率' },
              { value: "apparent_b_min", label: 'B路最小视在功率' }
            ] 
           },
          ],
        },
      ] as any;
      originalArray.value = ["active_total_avg_value", "active_total_max", "active_total_min", "apparent_total_avg_value", "apparent_total_max", "apparent_total_min", 
                            "active_a_avg_value", "active_a_max", "active_a_min", "apparent_a_avg_value", "apparent_a_max", "apparent_a_min",
                            "active_b_avg_value", "active_b_max", "active_b_min", "apparent_b_avg_value", "apparent_b_max", "apparent_b_min",
                          ]    

      tableColumns.value = [
        { label: '位置', align: 'center', prop: 'location' , width: '250px', istrue:true},
        { label: '记录时间', align: 'center', prop: 'create_time' , width: '200px', istrue:true},
        { label: '总平均有功功率(kW)', align: 'center', prop: 'active_total_avg_value', istrue:true, width: '180px', formatter: formatPower},
        { label: '总最大有功功率(kW)', align: 'center', prop: 'active_total_max_value', istrue:true, width: '180px', formatter: formatPower},
        { label: '总最大有功功率时间', align: 'center', prop: 'active_total_max_time', formatter: dateFormatter, width: '200px', istrue:true},
        { label: '总最小有功功率(kW)', align: 'center', prop: 'active_total_min_value', istrue:true, width: '180px', formatter: formatPower},
        { label: '总最小有功功率时间', align: 'center', prop: 'active_total_min_time', formatter: dateFormatter, width: '200px', istrue:true},
        { label: '总平均视在功率(kVA)', align: 'center', prop: 'apparent_total_avg_value', istrue:true, width: '180px', formatter: formatPower},
        { label: '总最大视在功率(kVA)', align: 'center', prop: 'apparent_total_max_value', istrue:true, width: '180px', formatter: formatPower},
        { label: '总最大视在功率时间', align: 'center', prop: 'apparent_total_max_time', formatter: dateFormatter, width: '200px', istrue:true},
        { label: '总最小视在功率(kVA)', align: 'center', prop: 'apparent_total_min_value', istrue:true, width: '180px', formatter: formatPower},
        { label: '总最小视在功率时间', align: 'center', prop: 'apparent_total_min_time', formatter: dateFormatter, width: '200px', istrue:true},

        { label: 'A路平均有功功率(kW)', align: 'center', prop: 'active_a_avg_value', istrue:false, width: '180px', formatter: formatPower},
        { label: 'A路最大有功功率(kW)', align: 'center', prop: 'active_a_max_value', istrue:false, width: '180px', formatter: formatPower},
        { label: 'A路最大有功功率时间', align: 'center', prop: 'active_a_max_time', formatter: dateFormatter, width: '200px', istrue:false},
        { label: 'A路最小有功功率(kW)', align: 'center', prop: 'active_a_min_value', istrue:false, width: '180px', formatter: formatPower},
        { label: 'A路最小有功功率时间', align: 'center', prop: 'active_a_min_time', formatter: dateFormatter, width: '200px', istrue:false},
        { label: 'A路平均视在功率(kVA)', align: 'center', prop: 'apparent_a_avg_value', istrue:false, width: '180px', formatter: formatPower},
        { label: 'A路最大视在功率(kVA)', align: 'center', prop: 'apparent_a_max_value', istrue:false, width: '180px', formatter: formatPower},
        { label: 'A路最大视在功率时间', align: 'center', prop: 'apparent_a_max_time', formatter: dateFormatter, width: '200px', istrue:false},
        { label: 'A路最小视在功率(kVA)', align: 'center', prop: 'apparent_a_min_value', istrue:false, width: '180px', formatter: formatPower},
        { label: 'A路最小视在功率时间', align: 'center', prop: 'apparent_a_min_time', formatter: dateFormatter, width: '200px', istrue:false},

        { label: 'B路平均有功功率(kW)', align: 'center', prop: 'active_b_avg_value', istrue:false, width: '180px', formatter: formatPower},
        { label: 'B路最大有功功率(kW)', align: 'center', prop: 'active_b_max_value', istrue:false, width: '180px', formatter: formatPower},
        { label: 'B路最大有功功率时间', align: 'center', prop: 'active_b_max_time', formatter: dateFormatter, width: '200px', istrue:false},
        { label: 'B路最小有功功率(kW)', align: 'center', prop: 'active_b_min_value', istrue:false, width: '180px', formatter: formatPower},
        { label: 'B路最小有功功率时间', align: 'center', prop: 'active_b_min_time', formatter: dateFormatter, width: '200px', istrue:false},
        { label: 'B路平均视在功率(kVA)', align: 'center', prop: 'apparent_b_avg_value', istrue:false, width: '180px', formatter: formatPower},
        { label: 'B路最大视在功率(kVA)', align: 'center', prop: 'apparent_b_max_value', istrue:false, width: '180px', formatter: formatPower},
        { label: 'B路最大视在功率时间', align: 'center', prop: 'apparent_b_max_time', formatter: dateFormatter, width: '200px', istrue:false},
        { label: 'B路最小视在功率(kVA)', align: 'center', prop: 'apparent_b_min_value', istrue:false, width: '180px', formatter: formatPower},
        { label: 'B路最小视在功率时间', align: 'center', prop: 'apparent_b_min_time', formatter: dateFormatter, width: '200px', istrue:false},
        { label: '操作', align: 'center', slot: 'actions', istrue:true},
      ];
      queryParams.pageNo = 1;
      queryParams.pageSize = 15;
      getList();
    }
    
});

const tableColumns = ref([
  { label: '位置', align: 'center', prop: 'location', width: '250px' ,  istrue:true},
  { label: '时间', align: 'center', prop: 'create_time', formatter: dateFormatter, width: '200px' , istrue:true},
  { label: '总有功功率(kW)', align: 'center', prop: 'active_total' , istrue:true, formatter: formatPower},
  { label: '总视在功率(kVA)', align: 'center', prop: 'apparent_total' , istrue:true, formatter: formatPower},
  { label: 'A路有功功率(kW)', align: 'center', prop: 'active_a' , istrue:false, formatter: formatPower},
  { label: 'A路视在功率(kVA)', align: 'center', prop: 'apparent_a' , istrue:false, formatter: formatPower},
  { label: 'B路有功功率(kW)', align: 'center', prop: 'active_b' , istrue:false, formatter: formatPower},
  { label: 'B路视在功率(kVA)', align: 'center', prop: 'apparent_b' , istrue:false, formatter: formatPower},
  { label: '操作', align: 'center', slot: 'actions' , istrue:true},
]);


/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
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
    if(item.type == 3){
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
  queryParams.cabinetIds = arr
  handleQuery()
}

// 接口获取机房导航列表
const getNavList = async() => {
  const res = await CabinetApi.getRoomMenuAll({})
  navList.value = res
}

// 获取导航的数据显示
const getNavNewData = async() => {
  const res = await HistoryDataApi.getNavNewData(queryParams.granularity)
  navTotalData.value = res.total
}

/** 详情操作*/
const toDetails = (cabinetId: number, location:string) => {
  push('/cabinet/record/historyLine?cabinetId='+cabinetId+'&location='+location);
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
    await download.excel(data, '机柜电力历史数据.xlsx')
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
</style>