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
          label-width="120px"
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
          <template v-for="column in tableColumns">
            <el-table-column :key="column.prop" :label="column.label" :align="column.align" :prop="column.prop" :formatter="column.formatter" :width="column.width" v-if="column.istrue" >
              <template #default="{ row }" v-if="column.slot === 'actions'">
                <el-button link type="primary" @click="toDetails(row.id)">详情</el-button>
                <el-button link type="danger" @click="handleDelete(row.id)" v-hasPermi="['pdu:history-data:delete']">删除</el-button>
              </template>
            </el-table-column>
          </template>
        </el-table>
        <!-- 分页 -->
        <Pagination
          :total="total"
          v-model:page="queryParams.pageNo"
          v-model:limit="queryParams.pageSize"
          @pagination="getList"
        />
      </ContentWrap>

    </el-col>
   </el-row>

</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { HistoryDataApi } from '@/api/pdu/historydata'
import { ElTree, ElIcon } from 'element-plus'

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
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  type: 'total',
  granularity: 'realtime',
  ipAddr: undefined,
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
        tableColumns.value = [
          { label: '编号', align: 'center', prop: 'id' , istrue:true, width: '100px'},
          { label: '位置', align: 'center', prop: 'location' , istrue:true, width: '200px'},
          { label: '总有功功率(kW)', align: 'center', prop: 'pow_active', istrue:true, width: '200px'},
          { label: '总视在功率(kVA)', align: 'center', prop: 'pow_apparent', istrue:true, width: '200px'},
          { label: '功率因素', align: 'center', prop: 'power_factor' , istrue:true},
          { label: '时间', align: 'center', prop: 'create_time', formatter: dateFormatter, width: '200px' , istrue:true},
          { label: '操作', align: 'center', slot: 'actions' , istrue:true},
        ];
        queryParams.pageNo = 1;
        queryParams.pageSize = 10;
        getList();
      }else{
        // 配置筛选列
        defaultOptionsCol.value = [
          ["pow_active", "pow_active_avg_value"],["pow_active", "pow_active_max"], 
          ["pow_active", "pow_active_min"],["pow_apparent", "pow_apparent_avg_value"], 
          ["pow_apparent", "pow_apparent_max"], ["pow_apparent", "pow_apparent_min"]
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
          { label: '编号', align: 'center', prop: 'id', istrue:true, width: '100px'},
          { label: '位置', align: 'center', prop: 'location', istrue:true, width: '180px'},
          { label: '平均有功功率(kW)', align: 'center', prop: 'pow_active_avg_value', istrue:true, width: '180px'},
          { label: '最大有功功率(kW)', align: 'center', prop: 'pow_active_max_value', istrue:true, width: '180px'},
          { label: '最大有功功率时间', align: 'center', prop: 'pow_active_max_time', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '最小有功功率(kW)', align: 'center', prop: 'pow_active_min_value', istrue:true, width: '180px'},
          { label: '最小有功功率时间', align: 'center', prop: 'pow_active_min_time', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '平均视在功率(kVA)', align: 'center', prop: 'pow_apparent_avg_value', istrue:true, width: '180px'},
          { label: '最大视在功率(kVA)', align: 'center', prop: 'pow_apparent_max_value', istrue:true, width: '180px'},
          { label: '最大视在功率时间', align: 'center', prop: 'pow_apparent_max_time', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '最小视在功率(kVA)', align: 'center', prop: 'pow_apparent_min_value', istrue:true, width: '180px'},
          { label: '最小视在功率时间', align: 'center', prop: 'pow_apparent_min_time', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '创建时间', align: 'center', prop: 'create_time', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '操作', align: 'center', slot: 'actions', istrue:true},
        ];
        queryParams.pageNo = 1;
        queryParams.pageSize = 10;
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
          { label: '编号', align: 'center', prop: 'id', istrue:true, width: '100px'},
          { label: '位置', align: 'center', prop: 'location', istrue:true, width: '180px'},
          { label: '电压(V)', align: 'center', prop: 'vol_value', istrue:true},
          { label: '电流(A)', align: 'center', prop: 'cur_value', istrue:true},
          { label: '有功功率(kW)', align: 'center', prop: 'pow_active', istrue:true, width: '140px'},
          { label: '视在功率(kVA)', align: 'center', prop: 'pow_apparent', istrue:true, width: '140px'},
          { label: '功率因素', align: 'center', prop: 'power_factor', istrue:true},
          { label: '时间', align: 'center', prop: 'create_time', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '操作', align: 'center', slot: 'actions', istrue:true},
        ];
        queryParams.pageNo = 1;
        queryParams.pageSize = 10;
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
          { label: '编号', align: 'center', prop: 'id', istrue:true, width: '100px'},
          { label: '位置', align: 'center', prop: 'location', istrue:true, width: '180px'},
          { label: '平均电压(V)', align: 'center', prop: 'vol_avg_value', istrue:false, width: '140px'},
          { label: '最大电压(V)', align: 'center', prop: 'vol_max_value', istrue:false, width: '140px'},
          { label: '最大电压时间', align: 'center', prop: 'vol_max_time', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '最小电压(V)', align: 'center', prop: 'vol_min_value', istrue:false, width: '140px'},
          { label: '最小电压时间', align: 'center', prop: 'vol_min_time', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '平均电流(A)', align: 'center', prop: 'cur_avg_value', istrue:false, width: '140px'},
          { label: '最大电流(A)', align: 'center', prop: 'cur_max_value', istrue:false, width: '140px'},
          { label: '最大电流时间', align: 'center', prop: 'cur_max_time', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '最小电流(A)', align: 'center', prop: 'cur_min_value', istrue:false, width: '140px'},
          { label: '最小电流时间', align: 'center', prop: 'cur_min_time', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '平均有功功率(kW)', align: 'center', prop: 'pow_active_avg_value', istrue:true, width: '180px'},
          { label: '最大有功功率(kW)', align: 'center', prop: 'pow_active_max_value', istrue:true, width: '180px'},
          { label: '最大有功功率时间', align: 'center', prop: 'pow_active_max_time', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '最小有功功率(kW)', align: 'center', prop: 'pow_active_min_value', istrue:true, width: '180px'},
          { label: '最小有功功率时间', align: 'center', prop: 'pow_active_min_time', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '平均视在功率(kVA)', align: 'center', prop: 'pow_apparent_avg_value', istrue:false, width: '180px'},
          { label: '最大视在功率(kVA)', align: 'center', prop: 'pow_apparent_max_value', istrue:false, width: '180px'},
          { label: '最大视在功率时间', align: 'center', prop: 'pow_apparent_max_time', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '最小视在功率(kVA)', align: 'center', prop: 'pow_apparent_min_value', istrue:false, width: '180px'},
          { label: '最小视在功率时间', align: 'center', prop: 'pow_apparent_min_time', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '创建时间', align: 'center', prop: 'create_time', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '操作', align: 'center', slot: 'actions', istrue:true},
        ];
        queryParams.pageNo = 1;
        queryParams.pageSize = 10;
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
          { label: '编号', align: 'center', prop: 'id', istrue:true, width: '100px'},
          { label: '位置', align: 'center', prop: 'location', istrue:true, width: '180px'},
          { label: '电压(V)', align: 'center', prop: 'vol_value', istrue:true},
          { label: '电流(A)', align: 'center', prop: 'cur_value', istrue:true},
          { label: '有功功率(kW)', align: 'center', prop: 'pow_active', istrue:true, width: '140px'},
          { label: '视在功率(kVA)', align: 'center', prop: 'pow_apparent', istrue:true, width: '140px'},
          { label: '功率因素', align: 'center', prop: 'power_factor', istrue:true},
          { label: '时间', align: 'center', prop: 'create_time', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '操作', align: 'center', slot: 'actions', istrue:true},
        ];
        queryParams.pageNo = 1;
        queryParams.pageSize = 10;
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
          { label: '编号', align: 'center', prop: 'id', istrue:true, width: '100px'},
          { label: '位置', align: 'center', prop: 'location', istrue:true, width: '180px'},
          { label: '平均电压(V)', align: 'center', prop: 'vol_avg_value', istrue:false, width: '140px'},
          { label: '最大电压(V)', align: 'center', prop: 'vol_max_value', istrue:false, width: '140px'},
          { label: '最大电压时间', align: 'center', prop: 'vol_max_time', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '最小电压(V)', align: 'center', prop: 'vol_min_value', istrue:false, width: '140px'},
          { label: '最小电压时间', align: 'center', prop: 'vol_min_time', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '平均电流(A)', align: 'center', prop: 'cur_avg_value', istrue:false, width: '140px'},
          { label: '最大电流(A)', align: 'center', prop: 'cur_max_value', istrue:false, width: '140px'},
          { label: '最大电流时间', align: 'center', prop: 'cur_max_time', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '最小电流(A)', align: 'center', prop: 'cur_min_value', istrue:false, width: '140px'},
          { label: '最小电流时间', align: 'center', prop: 'cur_min_time', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '平均有功功率(kW)', align: 'center', prop: 'pow_active_avg_value', istrue:true, width: '180px'},
          { label: '最大有功功率(kW)', align: 'center', prop: 'pow_active_max_value', istrue:true, width: '180px'},
          { label: '最大有功功率时间', align: 'center', prop: 'pow_active_max_time', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '最小有功功率(kW)', align: 'center', prop: 'pow_active_min_value', istrue:true, width: '180px'},
          { label: '最小有功功率时间', align: 'center', prop: 'pow_active_min_time', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '平均视在功率(kVA)', align: 'center', prop: 'pow_apparent_avg_value', istrue:false, width: '180px'},
          { label: '最大视在功率(kVA)', align: 'center', prop: 'pow_apparent_max_value', istrue:false, width: '180px'},
          { label: '最大视在功率时间', align: 'center', prop: 'pow_apparent_max_time', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '最小视在功率(kVA)', align: 'center', prop: 'pow_apparent_min_value', istrue:false, width: '180px'},
          { label: '最小视在功率时间', align: 'center', prop: 'pow_apparent_min_time', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '创建时间', align: 'center', prop: 'create_time', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '操作', align: 'center', slot: 'actions', istrue:true},
        ];
        queryParams.pageNo = 1;
        queryParams.pageSize = 10;
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
          { label: '编号', align: 'center', prop: 'id', istrue:true},
          { label: '位置', align: 'center', prop: 'location', istrue:true, width: '180px'},
          { label: '电流(A)', align: 'center', prop: 'cur_value', istrue:true},
          { label: '有功功率(kW)', align: 'center', prop: 'pow_active', istrue:true, width: '140px'},
          { label: '视在功率(kVA)', align: 'center', prop: 'pow_apparent', istrue:true, width: '140px'},
          { label: '功率因素', align: 'center', prop: 'power_factor', istrue:true},
          { label: '时间', align: 'center', prop: 'create_time', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '操作', align: 'center', slot: 'actions', istrue:true},
        ];
        queryParams.pageNo = 1;
        queryParams.pageSize = 10;
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
          { label: '编号', align: 'center', prop: 'id', istrue:true, width: '100px'},
          { label: '位置', align: 'center', prop: 'location', istrue:true, width: '180px'},
          { label: '平均电流(A)', align: 'center', prop: 'cur_avg_value', istrue:false, width: '140px'},
          { label: '最大电流(A)', align: 'center', prop: 'cur_max_value', istrue:false, width: '140px'},
          { label: '最大电流时间', align: 'center', prop: 'cur_max_time', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '最小电流(A)', align: 'center', prop: 'cur_min_value', istrue:false, width: '140px'},
          { label: '最小电流时间', align: 'center', prop: 'cur_min_time', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '平均有功功率(kW)', align: 'center', prop: 'pow_active_avg_value', istrue:true, width: '180px'},
          { label: '最大有功功率(kW)', align: 'center', prop: 'pow_active_max_value', istrue:true, width: '180px'},
          { label: '最大有功功率时间', align: 'center', prop: 'pow_active_max_time', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '最小有功功率(kW)', align: 'center', prop: 'pow_active_min_value', istrue:true, width: '180px'},
          { label: '最小有功功率时间', align: 'center', prop: 'pow_active_min_time', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '平均视在功率(kVA)', align: 'center', prop: 'pow_apparent_avg_value', istrue:false, width: '180px'},
          { label: '最大视在功率(kVA)', align: 'center', prop: 'pow_apparent_max_value', istrue:false, width: '180px'},
          { label: '最大视在功率时间', align: 'center', prop: 'pow_apparent_max_time', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '最小视在功率(kVA)', align: 'center', prop: 'pow_apparent_min_value', istrue:false, width: '180px'},
          { label: '最小视在功率时间', align: 'center', prop: 'pow_apparent_min_time', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '创建时间', align: 'center', prop: 'create_time', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '操作', align: 'center', slot: 'actions', istrue:true},
        ];
        queryParams.pageNo = 1;
        queryParams.pageSize = 10;
        getList();
      }
    }
  });

const tableColumns = ref([
    { label: '编号', align: 'center', prop: 'id' , istrue:true, width: '100px'},
    { label: '位置', align: 'center', prop: 'location' , istrue:true, width: '200px'},
    { label: '总有功功率(kW)', align: 'center', prop: 'pow_active', istrue:true, width: '200px'},
    { label: '总视在功率(kVA)', align: 'center', prop: 'pow_apparent', istrue:true, width: '200px'},
    { label: '功率因素', align: 'center', prop: 'power_factor' , istrue:true},
    { label: '时间', align: 'center', prop: 'create_time', formatter: dateFormatter, width: '200px' , istrue:true},
    { label: '操作', align: 'center', slot: 'actions' , istrue:true},
]);

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await HistoryDataApi.getHistoryDataPage(queryParams)
    list.value = data.list
    total.value = data.total

  } finally {
    loading.value = false
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
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