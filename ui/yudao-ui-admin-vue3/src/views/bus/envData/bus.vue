<template>
  <CommonMenu :dataList="navList" @check="handleCheck" navTitle="始端箱温度数据" >
    <template #NavInfo>
      <br/>  
        <div class="nav_data">
          <!-- <div class="nav_content">
            <el-descriptions title="全部始端箱新增环境记录" direction="vertical" :column="1" width="60px" border >
              <el-descriptions-item label="最近一小时"><span >{{ lastHourTotalData }} 条</span></el-descriptions-item>
              <el-descriptions-item label="最近一天"><span >{{ lastDayTotalData }} 条</span></el-descriptions-item>
              <el-descriptions-item label="最近一周" ><span >{{ lastWeekTotalData }} 条</span></el-descriptions-item>
            </el-descriptions>
          </div> -->
          <div class="descriptions-container" style="font-size: 14px;">
            <div class="description-item">
                <span class="label">最近一周 :</span>
                <span class="value">{{ lastWeekTotalData }}条</span>
            </div>
            <div class="description-item">
                <span class="label">最近一天 :</span>
                <span class="value">{{ lastDayTotalData }}条</span>
            </div>
            <div class="description-item">
                <span class="label">最近一小时 :</span>
                <span class="value">{{ lastHourTotalData }}条</span>
            </div>
            <div ><span>全部始端箱新增环境记录</span>
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
        <el-form-item label="颗粒度" prop="granularity">
          <el-select
            v-model="queryParams.granularity"
            placeholder="请选择分钟/小时/天"
            class="!w-100px">
            <el-option label="分钟" value="realtime" />
            <el-option label="小时" value="hour" />
            <el-option label="天" value="day" />
          </el-select>
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
            class="!w-200px"
          />
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
          :disabled-date="disabledDate" 
          class="!w-335px"
        />
        </el-form-item>

        <!-- <div style="float:right; padding-right:78px"> -->
        <el-form-item >
          <el-button @click="handleQuery" style="background-color: #00778c;color:#ffffff;font-size: 13px;"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        </el-form-item> 
        <el-form-item style="position: absolute;right: -18px;">
          <el-button type="success" plain @click="handleExport" :loading="exportLoading" style="background-color: #00778c;color:#ffffff;font-size: 13px;">
            <Icon icon="ep:download" class="mr-5px" /> 导出
          </el-button>
        </el-form-item>
        <!-- </div> -->
      </el-form>
    </template>
    <template #Content>
      <el-table v-loading="loading" :data="list"  :show-overflow-tooltip="false" border>
          <!-- 添加行号列 -->
        <el-table-column label="序号" align="center" width="100px">
          <template #default="{ $index }">
            {{ $index + 1 + (queryParams.pageNo - 1) * queryParams.pageSize }}
          </template>
        </el-table-column>
        <!-- 遍历其他列 -->
        <template v-for="column in tableColumns">
          <el-table-column :key="column.prop" :label="column.label" :align="column.align" :prop="column.prop" :formatter="column.formatter" :min-width="column.width" v-if="column.istrue&&column.slot !== 'actions'" >
            <template #default="{ row }">
              <div v-if="column.slot === 'actions'">
                <el-button type="primary" @click="toDetails(row.bus_id, row.dev_key, row.location)" style="background-color: #00778c;color:#ffffff;font-size: 13px;">详情</el-button>
              </div>
            </template>
          </el-table-column>
          <el-table-column :key="column.prop" :label="column.label" :align="column.align" :prop="column.prop" :formatter="column.formatter" :width="column.width" v-if="column.istrue&&column.slot == 'actions'" fixed="right">
            <template #default="{ row }">
              <div v-if="column.slot === 'actions'">
                <el-button type="primary" @click="toDetails(row.bus_id, row.dev_key, row.location)" style="background-color: #00778c;color:#ffffff;font-size: 13px;">详情</el-button>
              </div>
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
      <div class="realTotal" v-if="list.length != 0">共 {{ realTotel }} 条</div>
    </template>
  </CommonMenu>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import download from '@/utils/download'
import { EnvDataApi } from '@/api/bus/envData'
import { formatDate, endOfDay, convertDate, addTime} from '@/utils/formatTime'
import { IndexApi } from '@/api/bus/busindex'
// import PDUImage from '@/assets/imgs/PDU.jpg';
const { push } = useRouter()
/** bus历史数据 列表 */
defineOptions({ name: 'BusEnvHistoryData' })

const navList = ref([]) as any // 左侧导航栏树结构列表
const lastHourTotalData = ref(0)
const lastDayTotalData = ref(0)
const lastWeekTotalData = ref(0)
const message = useMessage() // 消息弹窗
const loading = ref(true) // 列表的加载中
const list = ref<Array<{ }>>([]); // 列表数据
const total = ref(0) // 数据总条数 超过10000条为10000
const realTotel = ref(0) // 数据的真实总条数
const selectTimeRange = ref()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 15,
  granularity: 'realtime',
  timeRange: undefined,
  devkeys:[]
})
const pageSizeArr = ref([15,30,50,100])
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中

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
const defaultOptionsCol = ref( [["tem_a"], ["tem_b"], ["tem_c"], ["tem_n"], ["dev_key"]])
const optionsCol = ref([
    { value: "tem_a", label: 'A路温度'},
    { value: "tem_b", label: 'B路温度'},
    { value: "tem_c", label: 'C路温度'},
    { value: "tem_n", label: '中线温度'},
    { value: "dev_key", label: '设备地址'},
])
const originalArray = ref(["tem_a", "tem_b", "tem_c", "tem_n", "dev_key"])

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

// 最后一页显示数据量过大的提示
const shouldShowDataExceedMessage = computed(() => {
  const lastPageNo = Math.ceil(total.value / queryParams.pageSize);
  return queryParams.pageNo === lastPageNo && total.value >= 10000;
});

watch(() => queryParams.granularity, (newValues) => {
    const  newGranularity = newValues;
    if ( newGranularity == 'realtime'){
      // 配置筛选列
      defaultOptionsCol.value = [["tem_a"], ["tem_b"], ["tem_c"], ["tem_n"], ["dev_key"]];
      optionsCol.value = [
        { value: "tem_a", label: 'A路温度'},
        { value: "tem_b", label: 'B路温度'},
        { value: "tem_c", label: 'C路温度'},
        { value: "tem_n", label: '中线温度'},
        { value: "dev_key", label: '设备地址'},
      ];
      originalArray.value =["tem_a", "tem_b", "tem_c", "tem_n", "dev_key"];
      // 配置表格列
      tableColumns.value =([
        { label: '所在位置', align: 'center', prop: 'location' , istrue:true, width: '300%'},
        { label: '设备地址', align: 'center', prop: 'dev_key' , istrue:true, width: '250px'},
        { label: '设备名称',align: 'center',prop: 'bus_name', istrue:true, width: '200%'},

        { label: '时间', align: 'center', prop: 'create_time', width: '200px', formatter: formatTime, istrue:true},
        { label: 'A路温度(℃)', align: 'center', prop: 'tem_a', istrue:true, formatter: formatData ,width: '110px'},
        { label: 'B路温度(℃)', align: 'center', prop: 'tem_b', istrue:true, formatter: formatData,width: '110px'},
        { label: 'C路温度(℃)', align: 'center', prop: 'tem_c', istrue:true, formatter: formatData,width:'110px'},
        { label: '中线温度(℃)', align: 'center', prop: 'tem_n', istrue:true, formatter: formatData,width: '110px'},
        { label: '操作', align: 'center', slot: 'actions' , istrue:true, width: '120px'},
      ]);
      queryParams.pageNo = 1;
      queryParams.pageSize = 15;
      getList();
    }else{
      // 配置筛选列
      defaultOptionsCol.value = [
        ["tem_a_value", "tem_a_avg_value"],["tem_a_value", "tem_a_max"], ["tem_a_value", "tem_a_min"],
        ["tem_b_value", "tem_b_avg_value"],["tem_b_value", "tem_b_max"], ["tem_b_value", "tem_b_min"],
        ["tem_c_value", "tem_c_avg_value"],["tem_c_value", "tem_c_max"], ["tem_c_value", "tem_c_min"],
        ["tem_n_value", "tem_n_avg_value"],["tem_n_value", "tem_n_max"], ["tem_n_value", "tem_n_min"],
        ["dev_key"],
      ];
      optionsCol.value = [
        { value: "tem_a_value", label: 'A路温度', children: [
            { value: "tem_a_avg_value", label: '平均温度'},
            { value: "tem_a_max", label: '最高温度' },
            { value: "tem_a_min", label: '最低温度' },
          ]
        },
        { value: "tem_b_value", label: 'B路温度', children: [
            { value: "tem_b_avg_value", label: '平均温度'},
            { value: "tem_b_max", label: '最高温度' },
            { value: "tem_b_min", label: '最低温度' },
          ]
        },
        { value: "tem_c_value", label: 'C路温度', children: [
            { value: "tem_c_avg_value", label: '平均温度'},
            { value: "tem_c_max", label: '最高温度' },
            { value: "tem_c_min", label: '最低温度' },
          ]
        },
        { value: "tem_n_value", label: '中线温度', children: [
            { value: "tem_n_avg_value", label: '平均温度'},
            { value: "tem_n_max", label: '最高温度' },
            { value: "tem_n_min", label: '最低温度' },
          ]
        },
        {
          value: "dev_key", label: '设备地址'
        }
      ] as any;
      originalArray.value =["tem_a_avg_value", "tem_a_max", "tem_a_min", "tem_b_avg_value", "tem_b_max", "tem_b_min", 
                          "tem_c_avg_value", "tem_c_max", "tem_c_min", "tem_n_avg_value", "tem_n_max", "tem_n_min", "dev_key",],
      // 配置表格列
      tableColumns.value = [
        { label: '所在位置', align: 'center', prop: 'location', istrue:true, width: '300%'}, 
        { label: '设备地址', align: 'center', prop: 'dev_key' , istrue:true, width: '250px'},
      { label: '设备名称',align: 'center',prop: 'bus_name', istrue:true, width: '200%'},

        { label: '记录时间', align: 'center', prop: 'create_time' , width: '230px', istrue:true,formatter: formatTime},

        { label: 'A路平均温度(℃)', align: 'center', prop: 'tem_a_avg_value', istrue:true, width: '180px', formatter: formatData },
        { label: 'A路最高温度(℃)', align: 'center', prop: 'tem_a_max_value', istrue:true, width: '180px', formatter: formatData },
        { label: 'A路最高温度时间', align: 'center', prop: 'tem_a_max_time' , width: '230px', istrue:true,formatter: formatTime},
        { label: 'A路最低温度(℃)', align: 'center', prop: 'tem_a_min_value', istrue:true, width: '180px', formatter: formatData },
        { label: 'A路最低温度时间', align: 'center', prop: 'tem_a_min_time' , width: '230px', istrue:true},

        { label: 'B路平均温度(℃)', align: 'center', prop: 'tem_b_avg_value', istrue:true, width: '180px', formatter: formatData },
        { label: 'B路最高温度(℃)', align: 'center', prop: 'tem_b_max_value', istrue:true, width: '180px', formatter: formatData },
        { label: 'B路最高温度时间', align: 'center', prop: 'tem_b_max_time' , width: '230px', istrue:true,formatter: formatTime},
        { label: 'B路最低温度(℃)', align: 'center', prop: 'tem_b_min_value', istrue:true, width: '180px', formatter: formatData },
        { label: 'B路最低温度时间', align: 'center', prop: 'tem_b_min_time' , width: '230px', istrue:true,formatter: formatTime},

        { label: 'C路平均温度(℃)', align: 'center', prop: 'tem_c_avg_value', istrue:true, width: '180px', formatter: formatData },
        { label: 'C路最高温度(℃)', align: 'center', prop: 'tem_c_max_value', istrue:true, width: '180px', formatter: formatData },
        { label: 'C路最高温度时间', align: 'center', prop: 'tem_c_max_time' , width: '230px', istrue:true,formatter: formatTime},
        { label: 'C路最低温度(℃)', align: 'center', prop: 'tem_c_min_value', istrue:true, width: '180px', formatter: formatData },
        { label: 'C路最低温度时间', align: 'center', prop: 'tem_c_min_time' , width: '230px', istrue:true,formatter: formatTime},

        { label: '中线平均温度(℃)', align: 'center', prop: 'tem_n_avg_value', istrue:true, width: '180px', formatter: formatData },
        { label: '中线最高温度(℃)', align: 'center', prop: 'tem_n_max_value', istrue:true, width: '180px', formatter: formatData },
        { label: '中线最高温度时间', align: 'center', prop: 'tem_n_max_time' , width: '230px', istrue:true,formatter: formatTime},
        { label: '中线最低温度(℃)', align: 'center', prop: 'tem_n_min_value', istrue:true, width: '180px', formatter: formatData },
        { label: '中线最低温度时间', align: 'center', prop: 'tem_n_min_time' , width: '230px', istrue:true,formatter: formatTime},

        { label: '操作', align: 'center', slot: 'actions', istrue:true, width: '160px'},
      ] as any;
      queryParams.pageNo = 1;
      queryParams.pageSize = 15;
      getList();
    }
  });

const tableColumns = ref([
  { label: '所在位置', align: 'center', prop: 'location' , istrue:true, width: '300%'},
  { label: '设备地址', align: 'center', prop: 'dev_key' , istrue:true, width: '250px'},
  { label: '设备名称',align: 'center',prop: 'bus_name', istrue:true, width: '200%'},

  { label: '时间', align: 'center', prop: 'create_time', width: '200px', formatter: formatTime, istrue:true},
  { label: 'A路温度(℃)', align: 'center', prop: 'tem_a', istrue:true, formatter: formatData, width: '110px'},
  { label: 'B路温度(℃)', align: 'center', prop: 'tem_b', istrue:true, formatter: formatData, width: '110px'},
  { label: 'C路温度(℃)', align: 'center', prop: 'tem_c', istrue:true, formatter: formatData, width: '110px'},
  { label: '中线温度(℃)', align: 'center', prop: 'tem_n', istrue:true, formatter: formatData, width: '110px'},
  { label: '操作', align: 'center', slot: 'actions' , istrue:true, width: '120px'},
]) as any;

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    if ( selectTimeRange.value != undefined){
      // 格式化时间范围 加上23:59:59的时分秒 
      const selectedStartTime = formatDate(selectTimeRange.value[0])
      // 结束时间的天数多加一天 ，  一天的毫秒数
      const oneDay = 24 * 60 * 60 * 1000;
      const selectedEndTime = formatDate(selectTimeRange.value[1])
      queryParams.timeRange = [selectedStartTime, selectedEndTime];
    }
    const data = await EnvDataApi.getBusEnvDataPage(queryParams)
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


// 格式化日期
function formatTime(_row: any, _column: any, cellValue: number): string {
  if (!cellValue) {
    return ''
  }

  return dayjs(cellValue).format('YYYY-MM-DD HH:mm')
}

// 格式化温湿度列数据，保留一位小数
function formatData(_row: any, _column: any, cellValue: number): string {
  return Number(cellValue).toFixed(0);
}

// 导航栏选择后触发
const handleCheck = async (node) => {
  let arr = [] as any
  node.forEach(item => { 
    if(item.type == 6){
      arr.push(item.unique);
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
  queryParams.devkeys = arr
  handleQuery()
}

// 接口获取机房导航列表
const getNavList = async() => {
  const res = await IndexApi.getBusMenu()
  navList.value = res
}

// 禁选未来的日期
const disabledDate = (date) => {
  const today = new Date();
  return date > today;
}

/** 搜索按钮操作 */
const handleQuery = () => {
    queryParams.pageNo = 1
    getList()
}

/** 详情操作*/
const toDetails = (busId: number, dev_key: string, location: string) => {
  const devKey = dev_key;
  const start=selectTimeRange.value?.[0];
  const end=selectTimeRange.value?.[1];
  console.log("selectTimeRange:start",selectTimeRange.value?.[0])
  console.log("selectTimeRange:end",selectTimeRange.value?.[1])
  if(start!=null && end!=null&&start!=''&&end!=''){
    console.log("have time")
    push({path: '/bus/record/bus/envAnalysis', state: {busId,devKey,location,start,end}})
  }else{
    push({path: '/bus/record/bus/envAnalysis', state: {busId,devKey,location}})
  }
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
    const data = await EnvDataApi.exportBusEnvHistoryData(queryParams, axiosConfig)
    await download.excel(data, '始端箱环境历史数据.xlsx')
  } catch (error) {
    // 处理异常
    console.error('导出失败：', error)
  } finally {
    exportLoading.value = false
  }
}

// 获取导航的数据显示
const getNavNewData = async() => {
  const res = await EnvDataApi.getBusEnvNavNewData()
  lastHourTotalData.value = res.hour
  lastDayTotalData.value = res.day
  lastWeekTotalData.value = res.week
}
const format = (date) => {
   return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
};
/** 初始化 **/
onMounted( () => {
  const now = new Date()
      const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
   // 使用上述自定义的 format 函数将日期对象转换为指定格式的字符串
selectTimeRange.value = [
  formatDate(startOfMonth),
  formatDate(now)
];
  getNavList()
  getNavNewData()
  getList()
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
  padding-left: 12px;
  width: 190px;
}
.nav_content span{
  font-size: 18px;
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
  ::v-deep .el-table .el-table__header th {
    background-color: #F5F7FA;
    color: #909399;
}
/deep/ .el-pagination.is-background .el-pager li.is-active {
  background-color: #00778c;
}
    /deep/  .el-pager li:hover {
    color: #00778c;
}
</style>