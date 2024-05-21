<template>
  <el-row :gutter="20">
   <el-col :span="treeWidth" :xs="24">
     <el-input
       v-model="filterText"
       style="width: 190px"
       placeholder=""
     />

     <el-tree
       ref="treeRef"
       style="max-width: 600px"
       class="filter-tree"
       :data="serverRoomArr"
       :props="defaultProps"
       default-expand-all
       show-checkbox
       :filter-node-method="filterNode"
     />
   </el-col>
   <el-col :span="24 - treeWidth" :xs="24">
     <ContentWrap>
       <!-- 搜索工作栏 -->
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
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            :disabled-date="disabledDate"
          />
          </el-form-item>

         <el-form-item >
           <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
           <!-- <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button> -->
           <el-button type="success" plain :loading="exportLoading">
             <Icon icon="ep:download" class="mr-5px" /> 导出
           </el-button>
         </el-form-item>
       </el-form>
    </ContentWrap>
    <ContentWrap>
     <!-- 列表 -->
      <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
        <!-- 添加行号列 -->
        <el-table-column label="序号" align="center" width="80px">
          <template #default="{ $index }">
            {{ $index + 1 + (queryParams.pageNo - 1) * queryParams.pageSize }}
          </template>
        </el-table-column>
        <!-- 遍历其他列 -->  
        <template v-for="column in tableColumns">
          <el-table-column :key="column.prop" :label="column.label" :align="column.align" :prop="column.prop" :formatter="column.formatter" :width="column.width" v-if="column.istrue">
            <template #default="{ row }" v-if="column.slot === 'actions'">
              <el-button link type="primary" @click="toDetails(row.pdu_id)">详情</el-button>
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
      <div class="realTotal">共 {{ realTotel }} 条</div>
      </ContentWrap>
   </el-col>
  </el-row>
 
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import download from '@/utils/download'
import { EnergyConsumptionApi } from '@/api/pdu/energyConsumption'
import { HistoryDataApi } from '@/api/pdu/historydata'
import { ElTree, ElIcon, ElMessage } from 'element-plus'
import { formatDate, endOfDay, convertDate, addTime, betweenDay} from '@/utils/formatTime'
import * as echarts from 'echarts';
const { push } = useRouter()
defineOptions({ name: 'PowerAnalysis' })

//折叠功能
const serverRoomArr =  [
 {
   value: '1',
   label: '机房1',
   children: [
     {
       value: '1-1',
       label: '柜列1',
     },
   ],
 },
 {
   value: '2',
   label: '机房2',
   children: [
     {
       value: '2-1',
       label: '柜列1',
       children: [
       {
         value: '2-1-1',
         label: '机柜1',
       },
       {
         value: '2-1-2',
         label: '机柜2',
       },]
     },
   ],
 },
 {
   value: '3',
   label: '机房3',
   children: [
     {
       value: '3-1',
       label: '柜列1',
       children: [
       {
         value: '3-1-1',
         label: '机柜1',
       },
       {
         value: '3-1-2',
         label: '机柜2',
       },]
     },
   ],
 },
]
let treeWidth = ref(3)
let isCollapsed = ref(0);
const toggleCollapse = () => {
 treeWidth.value = isCollapsed.value == 0 ? 3 : 0;
};
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
watch(filterText, (val) => {
 treeRef.value!.filter(val)
})

const instance = getCurrentInstance();
const message = useMessage()
const activeName = ref('myData') 
const loading = ref(true)
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
        { label: '位置', align: 'center', prop: 'location' , istrue:true},
        { label: '输出位', align: 'center', prop: 'outlet_id' , istrue:true}, 
        { label: '日期', align: 'center', prop: 'start_time', formatter: formatTime, istrue:true},
        { label: '耗电量(kWh)', align: 'center', prop: 'eq_value' , istrue:true, formatter: formatEle},
        { label: '电费(元)', align: 'center', prop: 'bill_value' , istrue:true, formatter: formatEle},
      ]
    }else{
       tableColumns.value = [
        { label: '位置', align: 'center', prop: 'location' , istrue:true},
        { label: '输出位', align: 'center', prop: 'outlet_id' , istrue:true}, 
        { label: '开始日期', align: 'center', prop: 'start_time', formatter: formatTime, istrue:true},
        { label: '结束日期', align: 'center', prop: 'end_time', formatter: formatTime, istrue:true},
        { label: '耗电量(kWh)', align: 'center', prop: 'eq_value' , istrue:true, formatter: formatEle},
        { label: '电费(元)', align: 'center', prop: 'bill_value' , istrue:true, formatter: formatEle},
      ]
    }
  }else{
    if (queryParams.granularity == 'day'){
      tableColumns.value = [
        { label: '位置', align: 'center', prop: 'location' , istrue:true},
        { label: '日期', align: 'center', prop: 'start_time' , formatter: formatTime, width: '200px' , istrue:true},
        { label: '耗电量(kWh)', align: 'center', prop: 'eq_value' , istrue:true, formatter: formatEle},
        { label: '电费(元)', align: 'center', prop: 'bill_value' , istrue:true, formatter: formatBill},
      ]
    }else{
      tableColumns.value = [
        { label: '位置', align: 'center', prop: 'location' , istrue:true},
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
  { label: '位置', align: 'center', prop: 'location' , istrue:true},
  { label: '日期', align: 'center', prop: 'start_time' , formatter: formatTime, width: '200px' , istrue:true},
  { label: '耗电量(kWh)', align: 'center', prop: 'eq_value' , istrue:true, formatter: formatEle},
  { label: '电费(元)', align: 'center', prop: 'bill_value' , istrue:true, formatter: formatBill},
]);

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    if ( selectTimeRange.value != undefined){
      // 格式化时间范围 加上23:59:59的时分秒 
      const selectedStartTime = formatDate(endOfDay(convertDate(selectTimeRange.value[0])))
      // 结束时间的天数多加一天 ，  一天的毫秒数
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
function formatTime(row: any, column: any, cellValue: number): string {
  if (!cellValue) {
    return ''
  }

  return dayjs(cellValue).format('YYYY-MM-DD')
}

// 格式化电能列数据，保留1位小数
function formatEle(row: any, column: any, cellValue: number): string {
  return cellValue.toFixed(1);
}

// 格式化电费列数据
function formatBill(row: any, column: any, cellValue: number): string {
  return cellValue.toFixed(3);
}

// 格式化耗电量列数据，保留1位小数
function formatEQ(value, decimalPlaces){
  if (!isNaN(value)) {
    return value.toFixed(decimalPlaces);
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

// /** 重置按钮操作 */
// const resetQuery = () => {
//  queryFormRef.value.resetFields()
//  handleQuery()
// }

// /** 导出按钮操作 */
// const handleExport = async () => {
//  try {
//    // 导出的二次确认
//    await message.exportConfirm()
//    // 发起导出
//    exportLoading.value = true
//    const data = await HistoryDataApi.exportHistoryData(queryParams)
//    download.excel(data, '电能分析.xls')
//  } catch {
//  } finally {
//    exportLoading.value = false
//  }
// }

/** 初始化 **/
onMounted(() => {
  getTypeMaxValue();
  getList();
});

</script>

<style scoped>
.el-form-item__label{
  width: auto;
}
.realTotal{
  float: right;
  padding-top: 20px;
  padding-right: 20px;
  font-size: 14px;
  font-weight: 400; 
  color: #606266
}
</style>