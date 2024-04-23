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

          <el-form-item label="参数类型" prop="type">
            <el-select
              v-model="queryParams.type"
              placeholder="请选择参数类型"
              class="!w-120px"
            >
              <el-option label="总" value="total" />
              <el-option label="相" value="line" />
              <el-option label="回路" value="loop" />
              <el-option label="输出位" value="outlet" />
            </el-select>
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
                <el-button link type="danger" @click="handleDelete(row.id)" v-hasPermi="['pdu:history-data:delete']">删除</el-button>
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
import { HistoryDataApi } from '@/api/pdu/historydata'
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
const list = ref<Array<{ 
    id: number; 
    location: string; 
    lineId: number; 
    loopId: number; 
    outletId: number; 

    vol:number
    cur:number
    active_pow: number; 
    apparent_pow: number; 

    volAvgValue: number; 
    volMaxValue: number; 
    volMaxTime: string; 
    volMinValue: number; 
    volMinTime: string; 

    curAvgValue: number; 
    curMaxValue: number; 
    curMaxTime: string; 
    curMinValue: number; 
    curMinTime: string; 

    activePowAvgValue: number; 
    activePowMaxValue: number; 
    activePowMaxTime: string; 
    activePowMinValue: number; 
    activePowMinTime: string; 

    apparentPowAvgValue: number; 
    apparentPowMaxValue: number; 
    apparentPowMaxTime: string; 
    apparentPowMinValue: number; 
    apparentPowMinTime: string; 

    powerFactor:number
    create_time:string
}>>([]); // 列表数据
const total = ref(0) // 数据总条数 超过10000条为10000
const realTotel = ref(0) // 数据的真实总条数
const cascadeAddr = ref(0) // 数字类型的级联地址
const queryParams = reactive({
  pageNo: 1,
  pageSize: 15,
  type: 'total',
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
const defaultOptionsCol = ref([["pow_active"], ["pow_apparent"], ["power_factor"]])
const optionsCol = ref([
  { value: "pow_active", label: '有功功率'},
  { value: "pow_apparent", label: '视在功率'},
  { value: "power_factor", label: '功率因素'},
])
const originalArray = ref(["pow_active", "pow_apparent", "power_factor"])

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

watch([() => queryParams.type, () => queryParams.granularity], (newValues) => {
    const [newType, newGranularity] = newValues;
    // 处理参数变化
    if (newType == 'total'){
      if ( newGranularity == 'realtime'){
        // 配置筛选列
        defaultOptionsCol.value = [["pow_active"], ["pow_apparent"], ["power_factor"]];
        optionsCol.value = [
          { value: "pow_active", label: '总有功功率'},
          { value: "pow_apparent", label: '总视在功率'},
          { value: "power_factor", label: '功率因素'},
        ];
        originalArray.value =["pow_active", "pow_apparent", "power_factor"];
        // 配置表格列
        tableColumns.value =([
            { label: '位置', align: 'center', prop: 'location' , istrue:true},
            { label: '总有功功率(kW)', align: 'center', prop: 'pow_active', istrue:true, formatter: formatPower},
            { label: '总视在功率(kVA)', align: 'center', prop: 'pow_apparent', istrue:true, formatter: formatPower},
            { label: '功率因素', align: 'center', prop: 'power_factor' , istrue:true, formatter: formatPowerFactor},
            { label: '时间', align: 'center', prop: 'create_time', formatter: dateFormatter, istrue:true},
            { label: '操作', align: 'center', slot: 'actions' , istrue:true, width: '200px'},
        ]);
        queryParams.pageNo = 1;
        queryParams.pageSize = 15;
        getList();
      }else{
        // 配置筛选列
        defaultOptionsCol.value = [
          ["pow_active", "pow_active_avg_value"],["pow_active", "pow_active_max"], 
          ["pow_active", "pow_active_min"]
        ];
        optionsCol.value = [
          { value: "pow_active", label: '有功功率', children: [
              { value: "pow_active_avg_value", label: '平均有功功率'},
              { value: "pow_active_max", label: '最大有功功率' },
              { value: "pow_active_min", label: '最小有功功率' },
            ]
          },
          { value: "pow_apparent", label: '视在功率', children: [
              { value: "pow_apparent_avg_value", label: '平均视在功率'},
              { value: "pow_apparent_max", label: '最大视在功率' },
              { value: "pow_apparent_min", label: '最小视在功率' },
            ]
          },
        ] as any;
        originalArray.value =["pow_active_avg_value", "pow_active_max", "pow_active_min", 
                            "pow_apparent_avg_value", "pow_apparent_max", "pow_apparent_min"],
        // 配置表格列
        tableColumns.value = [
          { label: '位置', align: 'center', prop: 'location', istrue:true, width: '180px'},
          { label: '平均有功功率(kW)', align: 'center', prop: 'pow_active_avg_value', istrue:true, width: '180px', formatter: formatPower},
          { label: '最大有功功率(kW)', align: 'center', prop: 'pow_active_max_value', istrue:true, width: '180px', formatter: formatPower},
          { label: '最大有功功率时间', align: 'center', prop: 'pow_active_max_time', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '最小有功功率(kW)', align: 'center', prop: 'pow_active_min_value', istrue:true, width: '180px', formatter: formatPower},
          { label: '最小有功功率时间', align: 'center', prop: 'pow_active_min_time', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '平均视在功率(kVA)', align: 'center', prop: 'pow_apparent_avg_value', istrue:false, width: '180px, formatter: formatPower'},
          { label: '最大视在功率(kVA)', align: 'center', prop: 'pow_apparent_max_value', istrue:false, width: '180px', formatter: formatPower},
          { label: '最大视在功率时间', align: 'center', prop: 'pow_apparent_max_time', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '最小视在功率(kVA)', align: 'center', prop: 'pow_apparent_min_value', istrue:false, width: '180px', formatter: formatPower},
          { label: '最小视在功率时间', align: 'center', prop: 'pow_apparent_min_time', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '创建时间', align: 'center', prop: 'create_time', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '操作', align: 'center', slot: 'actions', istrue:true, width: '200px'},
        ] as any;
        queryParams.pageNo = 1;
        queryParams.pageSize = 15;
        getList();
      }
    }
    
    if (newType == 'line'){
      if ( newGranularity == 'realtime'){
        // 配置筛选列
        defaultOptionsCol.value = [["vol_value"], ["cur_value"], ["pow_active"], ["pow_apparent"], ["power_factor"]];
        optionsCol.value = [
          { value: "vol_value", label: '电压'},
          { value: "cur_value", label: '电流'},
          { value: "pow_active", label: '有功功率'},
          { value: "pow_apparent", label: '视在功率'},
          { value: "power_factor", label: '功率因素'},
        ];
        originalArray.value =["vol_value", "cur_value", "pow_active", "pow_apparent", "power_factor"];
        // 配置表格列
        tableColumns.value = [
          { label: '位置', align: 'center', prop: 'location', istrue:true},
          { label: '相', align: 'center', prop: 'line_id', istrue:true, formatter: formatLineId},
          { label: '电压(V)', align: 'center', prop: 'vol_value', istrue:true, formatter: formatVoltage},
          { label: '电流(A)', align: 'center', prop: 'cur_value', istrue:true, formatter: formatCurrent},
          { label: '有功功率(kW)', align: 'center', prop: 'pow_active', istrue:true, formatter: formatPower},
          { label: '视在功率(kVA)', align: 'center', prop: 'pow_apparent', istrue:true, formatter: formatPower},
          { label: '功率因素', align: 'center', prop: 'power_factor', istrue:true, formatter: formatPowerFactor},
          { label: '时间', align: 'center', prop: 'create_time', formatter: dateFormatter, istrue:true, width: '180px'},
          { label: '操作', align: 'center', slot: 'actions', istrue:true, width: '200px'},
        ] as any;
        queryParams.pageNo = 1;
        queryParams.pageSize = 15;
        getList();
      }else{
        // 配置筛选列
        defaultOptionsCol.value = [
          ["pow_active", "pow_active_avg_value"],
          ["pow_active", "pow_active_max"],
          ["pow_active", "pow_active_min"],
        ];
        optionsCol.value = [
          { value: "vol_value", label: '电压', children: [
              { value: "vol_avg_value", label: '平均电压' },
              { value: "vol_max", label: '最大电压' },
              { value: "vol_min", label: '最小电压' },
            ]
          },
            { value: "cur_value", label: '电流', children: [
              { value: "cur_avg_value", label: '平均电流' },
              { value: "cur_max", label: '最大电流' },
              { value: "cur_min", label: '最小电流' },
            ]
          },
          { value: "pow_active", label: '有功功率', children: [
              { value: "pow_active_avg_value", label: '平均有功功率' },
              { value: "pow_active_max", label: '最大有功功率' },
              { value: "pow_active_min", label: '最小有功功率' },
            ]
          },
          { value: "pow_apparent", label: '视在功率', children: [
              { value: "pow_apparent_avg_value", label: '平均视在功率' },
              { value: "pow_apparent_max", label: '最大视在功率' },
              { value: "pow_apparent_min", label: '最小视在功率' },
            ]
          },
        ] as any;
        originalArray.value =["vol_avg_value", "vol_max", "vol_min",
                            "cur_avg_value", "cur_max", "cur_min",
                            "pow_active_avg_value", "pow_active_max", "pow_active_min", 
                            "pow_apparent_avg_value", "pow_apparent_max", "pow_apparent_min"],
        // 配置表格列
        tableColumns.value = [
          { label: '位置', align: 'center', prop: 'location', istrue:true, width: '180px'},
          { label: '相', align: 'center', prop: 'line_id', istrue:true, width: '100px', formatter: formatLineId},
          { label: '平均电压(V)', align: 'center', prop: 'vol_avg_value', istrue:false, width: '140px', formatter: formatVoltage},
          { label: '最大电压(V)', align: 'center', prop: 'vol_max_value', istrue:false, width: '120px', formatter: formatVoltage},
          { label: '最大电压时间', align: 'center', prop: 'vol_max_time', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '最小电压(V)', align: 'center', prop: 'vol_min_value', istrue:false, width: '140px', formatter: formatVoltage},
          { label: '最小电压时间', align: 'center', prop: 'vol_min_time', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '平均电流(A)', align: 'center', prop: 'cur_avg_value', istrue:false, width: '140px', formatter: formatCurrent},
          { label: '最大电流(A)', align: 'center', prop: 'cur_max_value', istrue:false, width: '140px', formatter: formatCurrent},
          { label: '最大电流时间', align: 'center', prop: 'cur_max_time', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '最小电流(A)', align: 'center', prop: 'cur_min_value', istrue:false, width: '140px', formatter: formatCurrent},
          { label: '最小电流时间', align: 'center', prop: 'cur_min_time', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '平均有功功率(kW)', align: 'center', prop: 'pow_active_avg_value', istrue:true, width: '180px', formatter: formatPower},
          { label: '最大有功功率(kW)', align: 'center', prop: 'pow_active_max_value', istrue:true, width: '180px', formatter: formatPower},
          { label: '最大有功功率时间', align: 'center', prop: 'pow_active_max_time', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '最小有功功率(kW)', align: 'center', prop: 'pow_active_min_value', istrue:true, width: '180px', formatter: formatPower},
          { label: '最小有功功率时间', align: 'center', prop: 'pow_active_min_time', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '平均视在功率(kVA)', align: 'center', prop: 'pow_apparent_avg_value', istrue:false, width: '180px', formatter: formatPower},
          { label: '最大视在功率(kVA)', align: 'center', prop: 'pow_apparent_max_value', istrue:false, width: '180px', formatter: formatPower},
          { label: '最大视在功率时间', align: 'center', prop: 'pow_apparent_max_time', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '最小视在功率(kVA)', align: 'center', prop: 'pow_apparent_min_value', istrue:false, width: '180px', formatter: formatPower},
          { label: '最小视在功率时间', align: 'center', prop: 'pow_apparent_min_time', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '创建时间', align: 'center', prop: 'create_time', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '操作', align: 'center', slot: 'actions', istrue:true, width: '200px'},
        ] as any;
        queryParams.pageNo = 1;
        queryParams.pageSize = 15;
        getList();
      }
    }

    if (newType == 'loop'){
      if ( newGranularity == 'realtime'){
        // 配置筛选列
        defaultOptionsCol.value = [["vol_value"], ["cur_value"], ["pow_active"], ["pow_apparent"], ["power_factor"]];
        optionsCol.value = [
          { value: "vol_value", label: '电压'},
          { value: "cur_value", label: '电流'},
          { value: "pow_active", label: '有功功率'},
          { value: "pow_apparent", label: '视在功率'},
          { value: "power_factor", label: '功率因素'},
        ];
        originalArray.value =["vol_value", "cur_value", "pow_active", "pow_apparent", "power_factor"];
        // 配置表格列
        tableColumns.value = [
          { label: '位置', align: 'center', prop: 'location', istrue:true},
          { label: '回路', align: 'center', prop: 'loop_id', istrue:true, formatter: formatLoopId},
          { label: '电压(V)', align: 'center', prop: 'vol_value', istrue:true, formatter: formatVoltage},
          { label: '电流(A)', align: 'center', prop: 'cur_value', istrue:true, formatter: formatCurrent},
          { label: '有功功率(kW)', align: 'center', prop: 'pow_active', istrue:true, formatter: formatPower},
          { label: '视在功率(kVA)', align: 'center', prop: 'pow_apparent', istrue:true, formatter: formatPower},
          { label: '功率因素', align: 'center', prop: 'power_factor', istrue:true, formatter: formatPowerFactor},
          { label: '时间', align: 'center', prop: 'create_time', formatter: dateFormatter, istrue:true, width: '180px'},
          { label: '操作', align: 'center', slot: 'actions', istrue:true, width: '200px'},
        ] as any ;
        queryParams.pageNo = 1;
        queryParams.pageSize = 15;
        getList();
      }else{
        // 配置筛选列
        defaultOptionsCol.value = [
          ["pow_active", "pow_active_avg_value"],
          ["pow_active", "pow_active_max"],
          ["pow_active", "pow_active_min"],
        ];
        optionsCol.value = [
          { value: "vol_value", label: '电压', children: [
              { value: "vol_avg_value", label: '平均电压' },
              { value: "vol_max", label: '最大电压' },
              { value: "vol_min", label: '最小电压' },
            ]
          },
            { value: "cur_value", label: '电流', children: [
              { value: "cur_avg_value", label: '平均电流' },
              { value: "cur_max", label: '最大电流' },
              { value: "cur_min", label: '最小电流' },
            ]
          },
          { value: "pow_active", label: '有功功率', children: [
              { value: "pow_active_avg_value", label: '平均有功功率' },
              { value: "pow_active_max", label: '最大有功功率' },
              { value: "pow_active_min", label: '最小有功功率' },
            ]
          },
          { value: "pow_apparent", label: '视在功率', children: [
              { value: "pow_apparent_avg_value", label: '平均视在功率' },
              { value: "pow_apparent_max", label: '最大视在功率' },
              { value: "pow_apparent_min", label: '最小视在功率' },
            ]
          },
        ] as any;
        originalArray.value =["vol_avg_value", "vol_max", "vol_min",
                            "cur_avg_value", "cur_max", "cur_min",
                            "pow_active_avg_value", "pow_active_max", "pow_active_min", 
                            "pow_apparent_avg_value", "pow_apparent_max", "pow_apparent_min"],
        // 配置表格列
        tableColumns.value = [
          { label: '位置', align: 'center', prop: 'location', istrue:true, width: '180px'},
          { label: '回路', align: 'center', prop: 'loop_id', istrue:true, width: '100px', formatter: formatLoopId},
          { label: '平均电压(V)', align: 'center', prop: 'vol_avg_value', istrue:false, width: '140px', formatter: formatVoltage},
          { label: '最大电压(V)', align: 'center', prop: 'vol_max_value', istrue:false, width: '140px', formatter: formatVoltage},
          { label: '最大电压时间', align: 'center', prop: 'vol_max_time', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '最小电压(V)', align: 'center', prop: 'vol_min_value', istrue:false, width: '140px', formatter: formatVoltage},
          { label: '最小电压时间', align: 'center', prop: 'vol_min_time', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '平均电流(A)', align: 'center', prop: 'cur_avg_value', istrue:false, width: '140px', formatter: formatCurrent},
          { label: '最大电流(A)', align: 'center', prop: 'cur_max_value', istrue:false, width: '140px', formatter: formatCurrent},
          { label: '最大电流时间', align: 'center', prop: 'cur_max_time', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '最小电流(A)', align: 'center', prop: 'cur_min_value', istrue:false, width: '140px', formatter: formatCurrent},
          { label: '最小电流时间', align: 'center', prop: 'cur_min_time', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '平均有功功率(kW)', align: 'center', prop: 'pow_active_avg_value', istrue:true, width: '180px', formatter: formatPower},
          { label: '最大有功功率(kW)', align: 'center', prop: 'pow_active_max_value', istrue:true, width: '180px', formatter: formatPower},
          { label: '最大有功功率时间', align: 'center', prop: 'pow_active_max_time', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '最小有功功率(kW)', align: 'center', prop: 'pow_active_min_value', istrue:true, width: '180px', formatter: formatPower},
          { label: '最小有功功率时间', align: 'center', prop: 'pow_active_min_time', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '平均视在功率(kVA)', align: 'center', prop: 'pow_apparent_avg_value', istrue:false, width: '180px', formatter: formatPower},
          { label: '最大视在功率(kVA)', align: 'center', prop: 'pow_apparent_max_value', istrue:false, width: '180px', formatter: formatPower},
          { label: '最大视在功率时间', align: 'center', prop: 'pow_apparent_max_time', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '最小视在功率(kVA)', align: 'center', prop: 'pow_apparent_min_value', istrue:false, width: '180px', formatter: formatPower},
          { label: '最小视在功率时间', align: 'center', prop: 'pow_apparent_min_time', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '创建时间', align: 'center', prop: 'create_time', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '操作', align: 'center', slot: 'actions', istrue:true, width: '200px'},
        ] as any;
        queryParams.pageNo = 1;
        queryParams.pageSize = 15;
        getList();
      }
    }

    if (newType == 'outlet'){
      if ( newGranularity == 'realtime'){
        // 配置筛选列
        defaultOptionsCol.value = [["cur_value"], ["pow_active"], ["pow_apparent"], ["power_factor"]];
        optionsCol.value = [
          { value: "cur_value", label: '电流'},
          { value: "pow_active", label: '有功功率'},
          { value: "pow_apparent", label: '视在功率'},
          { value: "power_factor", label: '功率因素'},
        ];
        originalArray.value =["cur_value", "pow_active", "pow_apparent", "power_factor"];
        // 配置表格列
        tableColumns.value = [
          { label: '位置', align: 'center', prop: 'location', istrue:true},
          { label: '输出位', align: 'center', prop: 'outlet_id', istrue:true},
          { label: '电流(A)', align: 'center', prop: 'cur_value', istrue:true, formatter: formatCurrent},
          { label: '有功功率(kW)', align: 'center', prop: 'pow_active', istrue:true, formatter: formatPower},
          { label: '视在功率(kVA)', align: 'center', prop: 'pow_apparent', istrue:true, formatter: formatPower},
          { label: '功率因素', align: 'center', prop: 'power_factor', istrue:true, formatter: formatPowerFactor},
          { label: '时间', align: 'center', prop: 'create_time', formatter: dateFormatter, istrue:true, width: '180px'},
          { label: '操作', align: 'center', slot: 'actions', istrue:true, width: '200px'},
        ] as any;
        queryParams.pageNo = 1;
        queryParams.pageSize = 15;
        getList();
      }else{
         // 配置筛选列
        defaultOptionsCol.value = [
          ["pow_active", "pow_active_avg_value"],
          ["pow_active", "pow_active_max"],
          ["pow_active", "pow_active_min"],
        ];
        optionsCol.value = [
          { value: "cur_value", label: '电流', children: [
              { value: "cur_avg_value", label: '平均电流' },
              { value: "cur_max", label: '最大电流' },
              { value: "cur_min", label: '最小电流' },
            ]
          },
          { value: "pow_active", label: '有功功率', children: [
              { value: "pow_active_avg_value", label: '平均有功功率' },
              { value: "pow_active_max", label: '最大有功功率' },
              { value: "pow_active_min", label: '最小有功功率' },
            ]
          },
          { value: "pow_apparent", label: '视在功率', children: [
              { value: "pow_apparent_avg_value", label: '平均视在功率' },
              { value: "pow_apparent_max", label: '最大视在功率' },
              { value: "pow_apparent_min", label: '最小视在功率' },
            ]
          },
        ] as any;
        originalArray.value =["cur_avg_value", "cur_max", "cur_min",
                            "pow_active_avg_value", "pow_active_max", "pow_active_min", 
                            "pow_apparent_avg_value", "pow_apparent_max", "pow_apparent_min"],
        // 配置表格列
        tableColumns.value = [
          { label: '位置', align: 'center', prop: 'location', istrue:true, width: '180px'},
          { label: '输出位', align: 'center', prop: 'outlet_id', istrue:true, width: '100px'},
          { label: '平均电流(A)', align: 'center', prop: 'cur_avg_value', istrue:false, width: '140px', formatter: formatCurrent},
          { label: '最大电流(A)', align: 'center', prop: 'cur_max_value', istrue:false, width: '140px', formatter: formatCurrent},
          { label: '最大电流时间', align: 'center', prop: 'cur_max_time', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '最小电流(A)', align: 'center', prop: 'cur_min_value', istrue:false, width: '140px', formatter: formatCurrent},
          { label: '最小电流时间', align: 'center', prop: 'cur_min_time', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '平均有功功率(kW)', align: 'center', prop: 'pow_active_avg_value', istrue:true, width: '180px', formatter: formatPower},
          { label: '最大有功功率(kW)', align: 'center', prop: 'pow_active_max_value', istrue:true, width: '180px', formatter: formatPower},
          { label: '最大有功功率时间', align: 'center', prop: 'pow_active_max_time', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '最小有功功率(kW)', align: 'center', prop: 'pow_active_min_value', istrue:true, width: '180px', formatter: formatPower},
          { label: '最小有功功率时间', align: 'center', prop: 'pow_active_min_time', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '平均视在功率(kVA)', align: 'center', prop: 'pow_apparent_avg_value', istrue:false, width: '180px', formatter: formatPower},
          { label: '最大视在功率(kVA)', align: 'center', prop: 'pow_apparent_max_value', istrue:false, width: '180px', formatter: formatPower},
          { label: '最大视在功率时间', align: 'center', prop: 'pow_apparent_max_time', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '最小视在功率(kVA)', align: 'center', prop: 'pow_apparent_min_value', istrue:false, width: '180px', formatter: formatPower},
          { label: '最小视在功率时间', align: 'center', prop: 'pow_apparent_min_time', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '创建时间', align: 'center', prop: 'create_time', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '操作', align: 'center', slot: 'actions', istrue:true, width: '200px'},
        ] as any;
        queryParams.pageNo = 1;
        queryParams.pageSize = 15;
        getList();
      }
    }
  });

const tableColumns = ref([
    { label: '位置', align: 'center', prop: 'location' , istrue:true},
    { label: '总有功功率(kW)', align: 'center', prop: 'pow_active', istrue:true, formatter: formatPower },
    { label: '总视在功率(kVA)', align: 'center', prop: 'pow_apparent', istrue:true, formatter: formatPower },
    { label: '功率因素', align: 'center', prop: 'power_factor' , istrue:true, formatter: formatPowerFactor },
    { label: '时间', align: 'center', prop: 'create_time', formatter: dateFormatter, istrue:true},
    { label: '操作', align: 'center', slot: 'actions' , istrue:true, width: '200px'},
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

// 格式化电压(V)列数据，保留一位小数
function formatVoltage(row: any, column: any, cellValue: number): string {
  return cellValue.toFixed(1);
}

// 格式化电流(A)列数据，保留两位小数
function formatCurrent(row: any, column: any, cellValue: number): string {
  return cellValue.toFixed(2);
}

// 格式化功率列数据，保留三位小数
function formatPower(row: any, column: any, cellValue: number): string {
  return cellValue.toFixed(3);
}

// 格式化电能列数据，保留三位小数
function formatEle(row: any, column: any, cellValue: number): string {
  return cellValue.toFixed(1);
}

// 格式化功率因素列数据，保留三位小数
function formatPowerFactor(row: any, column: any, cellValue: number): string {
  return cellValue.toFixed(2);
}

// 格式化相id
function formatLineId(row: any, column: any, cellValue: number): string {
   return 'L'+cellValue;
}

// 格式化回路id
function formatLoopId(row: any, column: any, cellValue: number): string {
   return 'C'+cellValue;
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
    const data = await HistoryDataApi.exportHistoryData(queryParams)
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