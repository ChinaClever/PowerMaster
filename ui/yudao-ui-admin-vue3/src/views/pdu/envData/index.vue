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
          <el-form-item label="" prop="collaspe">
            <el-switch 
              v-model="isCollapsed"  
              active-color="#409EFF" 
              inactive-color="#909399"
              active-text="折叠"  
              active-value="100"
              inactive-value="0" 
              @change="toggleCollapse" />
          </el-form-item>

          <el-form-item label="IP地址" prop="ipAddr">
            <el-input
              v-model="queryParams.ipAddr"
              placeholder="请输入IP地址"
              clearable
              @keyup.enter="handleQuery"
              class="!w-160px"
            />
          </el-form-item>

          <el-form-item label="级联地址" prop="cascadeAddr">
              <el-input-number
                v-model="cascadeAddr"
                :min="0"
                controls-position="right"
                :value-on-clear="0"
                 class="!w-100px"
              />
          </el-form-item>

          <el-form-item label="颗粒度" prop="type">
            <el-select
              v-model="queryParams.granularity"
              placeholder="请选择分钟/小时/天"
              class="!w-120px">
              <el-option label="分钟" value="realtime" />
              <el-option label="小时" value="hour" />
              <el-option label="天" value="day" />
            </el-select>
          </el-form-item>

          <el-form-item label="筛选列" prop="createTime">
            <el-cascader
              v-model="defaultOptionsCol"
              :options="optionsCol"
              :props="props"
              collapse-tags
              collapse-tags-tooltip
              :show-all-levels="false"
              @change="cascaderChange"
              class="!w-220px"
            />
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

          <div style="float:right">
          <el-form-item >
            <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
            <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
            <el-button type="success" plain @click="handleExport" :loading="exportLoading">
              <Icon icon="ep:download" class="mr-5px" /> 导出
            </el-button>
          </el-form-item>
          </div>
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
                <el-button link type="primary" @click="toDetails(row.id)">详情</el-button>
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
import dayjs from 'dayjs'
import download from '@/utils/download'
import { EnvDataApi } from '@/api/pdu/envData'
import { ElTree, ElIcon, ElMessage } from 'element-plus'

/** pdu历史数据 列表 */
defineOptions({ name: 'HistoryData' })

//折叠功能
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
const cascadeAddr = ref(0) // 数字类型的级联地址
const queryParams = reactive({
  pageNo: 1,
  pageSize: 15,
  granularity: 'realtime',
  ipAddr: undefined,
  cascadeAddr: '0',
  timeRange: undefined,
})
const pageSizeArr = ref([15,30,50,100])
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
    text: '最近半年',
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
const defaultOptionsCol = ref([["tem"], ["hum"]])
const optionsCol = ref([
  { value: "tem", label: '温度'},
  { value: "hum", label: '湿度'},
])
const originalArray = ref(["tem", "hum"])

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
      defaultOptionsCol.value = [["tem"], ["hum"]];
      optionsCol.value = [
        { value: "tem", label: '温度'},
        { value: "hum", label: '湿度'},
      ];
      originalArray.value =["tem", "hum"];
      // 配置表格列
      tableColumns.value =([
        { label: '位置', align: 'center', prop: 'location' , istrue:true},
        { label: '传感器', align: 'center', prop: 'sensor_id', istrue:true},
        { label: '温度(℃)', align: 'center', prop: 'tem_value', istrue:true},
        { label: '湿度(%RH)', align: 'center', prop: 'hum_value' , istrue:true},
        { label: '时间', align: 'center', prop: 'create_time', formatter: formatTime, istrue:true},
        { label: '操作', align: 'center', slot: 'actions' , istrue:true, width: '230px'},
      ]);
      queryParams.pageNo = 1;
      queryParams.pageSize = 15;
      getList();
    }else{
      // 配置筛选列
      defaultOptionsCol.value = [
        ["tem_value", "tem_avg_value"],["tem_value", "tem_max"], ["tem_value", "tem_min"],
        ["hum_value", "hum_avg_value"],["hum_value", "hum_max"], ["hum_value", "hum_min"],
      ];
      optionsCol.value = [
        { value: "tem_value", label: '温度', children: [
            { value: "tem_avg_value", label: '平均温度'},
            { value: "tem_max", label: '最大温度' },
            { value: "tem_min", label: '最小温度' },
          ]
        },
        { value: "hum_value", label: '湿度', children: [
            { value: "hum_avg_value", label: '平均湿度'},
            { value: "hum_max", label: '最大湿度' },
            { value: "hum_min", label: '最小湿度' },
          ]
        },
      ] as any;
      originalArray.value =["tem_avg_value", "tem_max", "tem_min", 
                          "hum_avg_value", "hum_max", "hum_min"],
      // 配置表格列
      tableColumns.value = [
        { label: '位置', align: 'center', prop: 'location', istrue:true, width: '180px'}, 
        { label: '平均温度(℃)', align: 'center', prop: 'tem_avg_value', istrue:true, width: '180px' },
        { label: '最大温度(℃)', align: 'center', prop: 'tem_max_value', istrue:true, width: '180px' },
        { label: '最大温度时间', align: 'center', prop: 'tem_max_time' , width: '230px', istrue:true},
        { label: '最小温度(℃)', align: 'center', prop: 'tem_min_value', istrue:true, width: '180px' },
        { label: '最小温度时间', align: 'center', prop: 'tem_min_time' , width: '230px', istrue:true},
        { label: '平均湿度(%RH)', align: 'center', prop: 'hum_avg_value', istrue:false, width: '180px '},
        { label: '最大湿度(%RH)', align: 'center', prop: 'hum_max_value', istrue:false, width: '180px' },
        { label: '最大湿度时间', align: 'center', prop: 'hum_max_time' , width: '230px', istrue:false},
        { label: '最小湿度(%RH)', align: 'center', prop: 'hum_min_value', istrue:false, width: '180px' },
        { label: '最小湿度时间', align: 'center', prop: 'hum_min_time' , width: '230px', istrue:false},
        { label: '创建时间', align: 'center', prop: 'create_time' , width: '230px', istrue:true},
        { label: '操作', align: 'center', slot: 'actions', istrue:true, width: '230px'},
      ] as any;
      queryParams.pageNo = 1;
      queryParams.pageSize = 15;
      getList();
    }
  });

const tableColumns = ref([
    { label: '位置', align: 'center', prop: 'location' , istrue:true},
    { label: '传感器', align: 'center', prop: 'sensor_id', istrue:true},
    { label: '温度(℃)', align: 'center', prop: 'tem_value', istrue:true},
    { label: '湿度(%RH)', align: 'center', prop: 'hum_value' , istrue:true},
    { label: '时间', align: 'center', prop: 'create_time', formatter: formatTime, istrue:true},
    { label: '操作', align: 'center', slot: 'actions' , istrue:true, width: '230px'},
]);

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await EnvDataApi.getEnvDataPage(queryParams)
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
function formatTime(row: any, column: any, cellValue: number): string {
  if (!cellValue) {
    return ''
  }

  return dayjs(cellValue).format('YYYY-MM-DD HH:mm:ss.SSS')
}


/** 搜索按钮操作 */
const handleQuery = () => {
   // IP地址的正则表达式
  const ipRegex = /^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/;
  if (queryParams.ipAddr == null || queryParams.ipAddr == '' || ipRegex.test(queryParams.ipAddr)){
    queryParams.pageNo = 1
    queryParams.cascadeAddr = cascadeAddr.value.toString();
    getList()
  }else{
    ElMessage.error('IP地址格式有误,请重新输入！')
  }

}

/** 重置按钮操作 */
const resetQuery = () => {
  cascadeAddr.value = 0
  queryFormRef.value.resetFields()
  handleQuery()
}


/** 详情操作*/
const toDetails = (id?: number) => {
  console.log(id)
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    exportLoading.value = true
    const data = await EnvDataApi.exportEnvData(queryParams)
    download.excel(data, 'pdu历史数据.xls')
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