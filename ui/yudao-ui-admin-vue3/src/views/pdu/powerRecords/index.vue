<template>
  <CommonMenu :dataList="navList" @check="handleCheck" navTitle="PDU电能记录" placeholder="如:192.168.1.96-0">
    <template #NavInfo>

      <br/>
      <div class="nav_data">


        <div class="descriptions-container" style="font-size: 14px;">
          <div >
            <span>PDU近一天新增电能记录</span>
          </div>
          <div class="description-item">
            <span class="label">电能 :</span>
            <span class="value">{{ navTotalData }}条</span>
          </div>
          <div class="description-item">
            <span class="label">相电能 :</span>
            <span class="value">{{ navLineData }}条</span>
          </div>
          <div v-if="navLoopData" class="description-item">
            <span class="label">回路电能 :</span>
            <span class="value">{{ navLoopData }}条</span>
          </div>
          <div v-if="navOutletData" class="description-item">
            <span class="label">输出位电能 :</span>
            <span class="value">{{ navOutletData }}条</span>
          </div>
          <div class="line" style="margin-top: 10px;"></div>
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
      />
      </el-form-item>

        <el-form-item >
          <el-button @click="handleQuery" style="background-color: #00778c;color:#ffffff;font-size: 13px;"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
          
        </el-form-item>
        <el-button type="success" plain :loading="exportLoading" @click="handleExport" style="float: right;margin-right: 10px;background-color: #00778c;color:#ffffff;font-size: 13px;">
            <Icon icon="ep:download" class="mr-5px" /> 导出
          </el-button>
      </el-form>
    </template>
    <template #Content>
      <el-table v-loading="loading" :data="list"  :show-overflow-tooltip="true" border>
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
      <div class="realTotal" v-if="list.length != 0">共 {{ realTotel }} 条</div>
     </template>
  </CommonMenu>
</template>

<script setup lang="ts">
import dayjs from 'dayjs';
import download from '@/utils/download';
import { EnergyConsumptionApi } from '@/api/pdu/energyConsumption';
import { HistoryDataApi } from '@/api/pdu/historydata';
import { CabinetApi } from '@/api/cabinet/info';
import PDUImage from '@/assets/imgs/PDU.jpg';
import { formatDate,startOfDay, endOfDay, convertDate, addTime} from '@/utils/formatTime'
import { ElMessage } from 'element-plus';
defineOptions({ name: 'PowerRecords' });

const navList = ref([]) as any; // 左侧导航栏树结构列表
const navTotalData = ref(0);
const navLineData = ref(0);
const navLoopData = ref(0);
const navOutletData = ref(0);
const loading = ref(true);
const message = useMessage(); // 消息弹窗
const list = ref<Array<{ }>>([]) as any; 
const total = ref(0);
const realTotel = ref(0); // 数据的真实总条数
const selectTimeRange = ref(undefined)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 15,
  lineId: undefined,
  loopId: undefined,
  outletId: undefined,
  type: 'total',
  timeRange: undefined as any,
  ipArray: [],
});
const pageSizeArr = ref([15,30,50,100]);
const queryFormRef = ref();
const exportLoading = ref(false);
const carouselItems = ref([
      { imgUrl: PDUImage},
      { imgUrl: PDUImage},
      { imgUrl: PDUImage},
      { imgUrl: PDUImage},
    ]);//侧边栏轮播图图片路径
// 时间段快捷选项
const shortcuts = [
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

// 总/输出位筛选
const typeDefaultSelected = ref(['total'])
const typeSelection = ref([]) as any;
const typeCascaderChange = (selected) => {
  queryParams.type = selected[0];
  switch(selected[0]){
    case 'line':
      tableColumns.value = [
        { label: '所在位置', align: 'center', prop: 'address' , istrue:true, width: '300%'},
        { label: '网络地址', align: 'center', prop: 'location' , istrue:true},
        { label: '发生时间', align: 'center', prop: 'create_time', formatter: formatTime, istrue:true},
        { label: '相', align: 'center', prop: 'line_id' , istrue:true, formatter: formatLineId}, 
        { label: '电能(kWh)', align: 'center', prop: 'ele_active' , istrue:true, formatter: formatEle},
      ]
      queryParams.lineId = selected[1];
      queryParams.loopId = undefined;
      queryParams.outletId = undefined;
      break;
    case 'loop':
      tableColumns.value = [
        { label: '所在位置', align: 'center', prop: 'address' , istrue:true, width: '300%'},
        { label: '网络地址', align: 'center', prop: 'location' , istrue:true},
        { label: '发生时间', align: 'center', prop: 'create_time', formatter: formatTime, istrue:true},
        { label: '回路', align: 'center', prop: 'loop_id' , istrue:true, formatter: formatLoopId},
        { label: '电能(kWh)', align: 'center', prop: 'ele_active' , istrue:true, formatter: formatEle},
      ]
      queryParams.loopId = selected[1];
      queryParams.lineId = undefined;
      queryParams.outletId = undefined;
      break;
    case 'outlet':
      tableColumns.value = [
        { label: '所在位置', align: 'center', prop: 'address' , istrue:true, width: '300%'},
        { label: '网络地址', align: 'center', prop: 'location' , istrue:true},
        { label: '发生时间', align: 'center', prop: 'create_time', formatter: formatTime, istrue:true},
        { label: '输出位', align: 'center', prop: 'outlet_id' , istrue:true},
        { label: '电能(kWh)', align: 'center', prop: 'ele_active' , istrue:true, formatter: formatEle},
      ]
      queryParams.outletId = selected[1];
      queryParams.loopId = undefined;
      queryParams.lineId = undefined;
      break;
    case 'total':
      tableColumns.value = [
        { label: '所在位置', align: 'center', prop: 'address' , istrue:true, width: '300%'},
        { label: '网络地址', align: 'center', prop: 'location' , istrue:true},
        { label: '发生时间', align: 'center', prop: 'create_time', formatter: formatTime, istrue:true},
        { label: '电能(kWh)', align: 'center', prop: 'ele_active' , istrue:true, formatter: formatEle},
      ]
      queryParams.lineId = undefined;
      queryParams.loopId = undefined;
      queryParams.outletId = undefined;
      break;
  }
  // 自动搜索
  handleQuery();
}

const tableColumns = ref([
  { label: '所在位置', align: 'center', prop: 'address' , istrue:true, width: '300%'},
  { label: '网络地址', align: 'center', prop: 'location' , istrue:true},
  { label: '发生时间', align: 'center', prop: 'create_time', formatter: formatTime, istrue:true},
  { label: '电能(kWh)', align: 'center', prop: 'ele_active' , istrue:true, formatter: formatEle},
]);

/** 初始化数据 */
const getList = async () => {
  loading.value = true
  try {
    if ( selectTimeRange.value != undefined){
      // 格式化日期范围 加上23:59:59的时分秒 
      const selectedStartTime = selectTimeRange.value[0];
     
      const selectedEndTime = selectTimeRange.value[1]
      // selectTimeRange.value = [selectedStartTime, selectedEndTime];
      queryParams.timeRange = [selectedStartTime, selectedEndTime];
    }
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
  return dayjs(cellValue).format('YYYY-MM-DD HH:mm')
}

// 获取参数类型最大值 例如lineId=6 表示下拉框为L1~L6
const getTypeMaxValue = async () => {
    const data = await HistoryDataApi.getTypeMaxValue()
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
        lines.push({ value: undefined, label: '全部' },)
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
        loops.push({ value: undefined, label: '全部' },)
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

// 格式化相id
function formatLineId(_row: any, _column: any, cellValue: number): string {
   return 'L'+cellValue;
}

// 格式化回路id
function formatLoopId(_row: any, _column: any, cellValue: number): string {
   return 'C'+cellValue;
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
  let arr = [] as any
  var temp = await CabinetApi.getRoomPDUList()
  arr = arr.concat(temp);
  navList.value = arr
}

// 获取导航的数据显示
const getNavOneDayData = async() => {
  const res = await EnergyConsumptionApi.getNavOneDayData({})
  navTotalData.value = res.total
  navLineData.value = res.line
  navLoopData.value = res.loop
  navOutletData.value = res.outlet
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
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
    await download.excel(data, 'PDU电能记录.xlsx')
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
  getNavList();
  getNavOneDayData();
  getTypeMaxValue();
  const now = new Date()
      const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
   // 使用上述自定义的 format 函数将日期对象转换为指定格式的字符串
    selectTimeRange.value = [
      // format(startOfMonth),
      dayjs(startOfMonth).format("YYYY-MM-DD HH:mm:ss"),
      dayjs(now).format("YYYY-MM-DD HH:mm:ss")
    ];
  getList();
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
    background-color: #f7f7f7;
    color: #909399;
    height: 60px;
}
/deep/ .el-pagination.is-background .el-pager li.is-active {
  background-color: #00778c;
}
    /deep/  .el-pager li:hover {
    color: #00778c;
}
</style>
