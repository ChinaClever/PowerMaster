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
          label-width="80px"
        >
          <el-form-item label="颗粒度" prop="type">
            <el-select
              v-model="queryParams.granularity"
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
              type="primary"
              plain
              @click="openForm('create')"
              v-hasPermi="['pdu:history-data:create']"
            >
              <Icon icon="ep:plus" class="mr-5px" /> 新增
            </el-button>
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
      </ContentWrap>
      <!-- 列表 -->
      <ContentWrap>
          <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
           <!-- 添加行号列 -->
          <el-table-column label="序号" align="center" width="100px">
            <template #default="{ $index }">
              {{ $index + 1 + (queryParams.pageNo - 1) * queryParams.pageSize }}
            </template>
          </el-table-column>
          <!-- 遍历其他列 -->
          <template v-for="column in tableColumns">
            <el-table-column :key="column.prop" :label="column.label" :align="column.align" :prop="column.prop" :formatter="column.formatter" :width="column.width" v-if="column.istrue" >
              <template #default="{ row }" v-if="column.slot === 'actions'">
                <el-button link type="primary" @click="toDetails(row.cabinet_id)">详情</el-button>
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
      </ContentWrap>

    </el-col>
   </el-row>

</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { HistoryDataApi } from '@/api/cabinet/historydata'
import { ElTree } from 'element-plus'
const { push } = useRouter()
/** pdu历史数据 列表 */
defineOptions({ name: 'CabinetHistoryData' })

const serverRoomArr =  [
  {
    value: '1',
    label: '机房1',
    children: [
      {
        value: '1-1',
        label: '柜列1',
        children: [
        {
          value: '1-1-1',
          label: '机柜1',
        },
        {
          value: '1-1-2',
          label: '机柜2',
        },]
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
//折叠功能
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

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const loading = ref(true) // 列表的加载中
const list = ref<Array<{ }>>([]); // 列表数据
const total = ref(0) // 数据总条数 超过10000条为10000
const realTotel = ref(0) // 数据的真实总条数
const pageSizeArr = ref([15,30,50,100])
const cascadeAddr = ref(0) // 数字类型的级联地址
const queryParams = reactive({
  pageNo: 1,
  pageSize: 15,
  granularity: 'realtime',
  timeRange: undefined,
})
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中

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

//筛选选项
const props = { multiple: true}
const defaultOptionsCol = ref([["total", "active_total"], ["total", "apparent_total"], ["A", "active_a"], 
                ["A", "apparent_a"], ["B", "active_b"], ["B", "apparent_b"]])
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

watch(() => queryParams.granularity, (newValues) => { 
    const newGranularity = newValues;
    if ( newGranularity == 'realtime'){
      defaultOptionsCol.value = [
        ["total", "active_total"], ["total", "apparent_total"], ["A", "active_a"], 
        ["A", "apparent_a"], ["B", "active_b"], ["B", "apparent_b"]
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
        { label: '位置', align: 'center', prop: 'location' , width: '100px' , istrue:true},
        { label: '总有功功率(kW)', align: 'center', prop: 'active_total' , istrue:true, formatter: formatPower},
        { label: '总视在功率(kVA)', align: 'center', prop: 'apparent_total' , istrue:true, formatter: formatPower},
        { label: 'A路有功功率(kW)', align: 'center', prop: 'active_a' , istrue:true, formatter: formatPower},
        { label: 'A路视在功率(kVA)', align: 'center', prop: 'apparent_a' , istrue:true, formatter: formatPower},
        { label: 'B路有功功率(kW)', align: 'center', prop: 'active_b' , istrue:true, formatter: formatPower},
        { label: 'B路视在功率(kVA)', align: 'center', prop: 'apparent_b' , istrue:true, formatter: formatPower},
        { label: '时间', align: 'center', prop: 'create_time', formatter: dateFormatter, width: '230px' , istrue:true},
        { label: '操作', align: 'center', slot: 'actions' , istrue:true},
      ]
      queryParams.pageNo = 1;
      queryParams.pageSize = 15;
      getList();
    }else{
      defaultOptionsCol.value = [
        ["total", "active_pow", "active_total_avg_value"],["total", "apparent_pow", "apparent_total_avg_value"],["A", "active_pow", "active_a_avg_value"],
        ["A", "apparent_pow", "apparent_a_avg_value"],["B", "active_pow", "active_b_avg_value"],["B", "apparent_pow", "apparent_b_avg_value"],
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
        { label: '位置', align: 'center', prop: 'location' , width: '100px' , istrue:true},
        { label: '总平均有功功率(kW)', align: 'center', prop: 'active_total_avg_value', istrue:true, width: '180px', formatter: formatPower},
        { label: '总最大有功功率(kW)', align: 'center', prop: 'active_total_max_value', istrue:false, width: '180px', formatter: formatPower},
        { label: '总最大有功功率时间', align: 'center', prop: 'active_total_max_time', formatter: dateFormatter, width: '200px', istrue:false},
        { label: '总最小有功功率(kW)', align: 'center', prop: 'active_total_min_value', istrue:false, width: '180px', formatter: formatPower},
        { label: '总最小有功功率时间', align: 'center', prop: 'active_total_min_time', formatter: dateFormatter, width: '200px', istrue:false},
        { label: '总平均视在功率(kVA)', align: 'center', prop: 'apparent_total_avg_value', istrue:true, width: '180px', formatter: formatPower},
        { label: '总最大视在功率(kVA)', align: 'center', prop: 'apparent_total_max_value', istrue:false, width: '180px', formatter: formatPower},
        { label: '总最大视在功率时间', align: 'center', prop: 'apparent_total_max_time', formatter: dateFormatter, width: '200px', istrue:false},
        { label: '总最小视在功率(kVA)', align: 'center', prop: 'apparent_total_min_value', istrue:false, width: '180px', formatter: formatPower},
        { label: '总最小视在功率时间', align: 'center', prop: 'apparent_total_min_time', formatter: dateFormatter, width: '200px', istrue:false},

        { label: 'A路平均有功功率(kW)', align: 'center', prop: 'active_a_avg_value', istrue:true, width: '180px', formatter: formatPower},
        { label: 'A路最大有功功率(kW)', align: 'center', prop: 'active_a_max_value', istrue:false, width: '180px', formatter: formatPower},
        { label: 'A路最大有功功率时间', align: 'center', prop: 'active_a_max_time', formatter: dateFormatter, width: '200px', istrue:false},
        { label: 'A路最小有功功率(kW)', align: 'center', prop: 'active_a_min_value', istrue:false, width: '180px', formatter: formatPower},
        { label: 'A路最小有功功率时间', align: 'center', prop: 'active_a_min_time', formatter: dateFormatter, width: '200px', istrue:false},
        { label: 'A路平均视在功率(kVA)', align: 'center', prop: 'apparent_a_avg_value', istrue:true, width: '180px', formatter: formatPower},
        { label: 'A路最大视在功率(kVA)', align: 'center', prop: 'apparent_a_max_value', istrue:false, width: '180px', formatter: formatPower},
        { label: 'A路最大视在功率时间', align: 'center', prop: 'apparent_a_max_time', formatter: dateFormatter, width: '200px', istrue:false},
        { label: 'A路最小视在功率(kVA)', align: 'center', prop: 'apparent_a_min_value', istrue:false, width: '180px', formatter: formatPower},
        { label: 'A路最小视在功率时间', align: 'center', prop: 'apparent_a_min_time', formatter: dateFormatter, width: '200px', istrue:false},

        { label: 'B路平均有功功率(kW)', align: 'center', prop: 'active_b_avg_value', istrue:true, width: '180px', formatter: formatPower},
        { label: 'B路最大有功功率(kW)', align: 'center', prop: 'active_b_max_value', istrue:false, width: '180px', formatter: formatPower},
        { label: 'B路最大有功功率时间', align: 'center', prop: 'active_b_max_time', formatter: dateFormatter, width: '200px', istrue:false},
        { label: 'B路最小有功功率(kW)', align: 'center', prop: 'active_b_min_value', istrue:false, width: '180px', formatter: formatPower},
        { label: 'B路最小有功功率时间', align: 'center', prop: 'active_b_min_time', formatter: dateFormatter, width: '200px', istrue:false},
        { label: 'B路平均视在功率(kVA)', align: 'center', prop: 'apparent_b_avg_value', istrue:true, width: '180px', formatter: formatPower},
        { label: 'B路最大视在功率(kVA)', align: 'center', prop: 'apparent_b_max_value', istrue:false, width: '180px', formatter: formatPower},
        { label: 'B路最大视在功率时间', align: 'center', prop: 'apparent_b_max_time', formatter: dateFormatter, width: '200px', istrue:false},
        { label: 'B路最小视在功率(kVA)', align: 'center', prop: 'apparent_b_min_value', istrue:false, width: '180px', formatter: formatPower},
        { label: 'B路最小视在功率时间', align: 'center', prop: 'apparent_b_min_time', formatter: dateFormatter, width: '200px', istrue:false},
        { label: '记录时间', align: 'center', prop: 'create_time' , width: '230px', istrue:true},
        { label: '操作', align: 'center', slot: 'actions', istrue:true},
      ];
      queryParams.pageNo = 1;
      queryParams.pageSize = 15;
      getList();
    }
    
});

const tableColumns = ref([
  { label: '位置', align: 'center', prop: 'location' , width: '100px' , istrue:true},
  { label: '总有功功率(kW)', align: 'center', prop: 'active_total' , istrue:true, formatter: formatPower},
  { label: '总视在功率(kVA)', align: 'center', prop: 'apparent_total' , istrue:true, formatter: formatPower},
  { label: 'A路有功功率(kW)', align: 'center', prop: 'active_a' , istrue:true, formatter: formatPower},
  { label: 'A路视在功率(kVA)', align: 'center', prop: 'apparent_a' , istrue:true, formatter: formatPower},
  { label: 'B路有功功率(kW)', align: 'center', prop: 'active_b' , istrue:true, formatter: formatPower},
  { label: 'B路视在功率(kVA)', align: 'center', prop: 'apparent_b' , istrue:true, formatter: formatPower},
  { label: '时间', align: 'center', prop: 'create_time', formatter: dateFormatter, width: '180px' , istrue:true},
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
function formatPower(row: any, column: any, cellValue: number): string {
  return cellValue.toFixed(3);
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

/** 详情操作*/
const toDetails = (cabinetId: number) => {
  push('/cabinet/record/historyLine?cabinetId='+cabinetId);
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    exportLoading.value = true
    const data = await HistoryDataApi.exportHistoryData(queryParams)
    download.excel(data, '机柜历史数据.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

/** 初始化 **/
onMounted(() => {
  getList()
})
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