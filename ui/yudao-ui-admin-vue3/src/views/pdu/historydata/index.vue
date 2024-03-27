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
          <el-form-item label="相/回路/输出位" prop="type">
            <el-select
              v-model="queryParams.type"
              placeholder="请选择总/相/回路/输出位"
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
              class="!w-120px"
            >
              <el-option label="分钟" value="realtime" />
              <el-option label="小时" value="hour" />
              <el-option label="天" value="day" />
            </el-select>
          </el-form-item>
          <el-form-item label="时间段" prop="createTime">
            <el-date-picker
              v-model="queryParams.createTime"
              value-format="YYYY-MM-DD HH:mm:ss"
              type="daterange"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
              class="!w-210px"
            />
          </el-form-item>
          <div style="display: flex; justify-content: flex-end;">
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

        <el-icon style="float: right; font-size: 33px" class="show-col-btn">
            <el-popover placement="bottom" trigger="click" width="200">
              <template #reference>
                <el-button text><Icon icon="ep:operation" :size="30" /></el-button>
              </template>
              <div style="overflow: auto; max-height: 650px;">
                <div v-for="(column, index) in tableColumns" :key="index">
                  <el-checkbox type="checkbox" v-model="column.istrue"/> {{ column.label }}
                </div>
              </div>
            </el-popover>
          </el-icon>

          <div style="display: flex; float: right;">
            <div v-for="(column, index) in checkColumns" :key="index" style="margin-right: 20px;">
              <div v-if="column.isView">
                <el-checkbox type="checkbox" v-model="column.istrue"  @click="handleIsView(column.label, column.istrue)"/> {{ column.label }}
              </div>    
            </div>
          </div>

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
const list = ref<Array<{ 
    id: number; 
    location: string; 
    lineId: number; 
    loopId: number; 
    outletId: number; 

    vol:number
    cur:number
    activePow: number; 
    apparentPow: number; 

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
    createTime:string
}>>([]); // 列表数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  id: undefined,
  type: 'total',
  granularity: 'realtime',
  ipAddr: undefined,
  createTime: undefined,
})
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中

watch([() => queryParams.type, () => queryParams.granularity], (newValues) => {
    const [newType, newGranularity] = newValues;
    // 处理参数变化
    if (newType == 'total'){
      checkColumns.value.forEach(column => {
          if (column.prop === 'vol' || column.prop === 'cur') {
            column.isView = false;
          }else{
            column.isView = true;
          }
      });
      if ( newGranularity == 'realtime'){
        tableColumns.value = [
          { label: '编号', align: 'center', prop: 'id', istrue:true},
          { label: '位置', align: 'center', prop: 'location', istrue:true, width: '180px'},
          { label: '总有功功率(kVA)', align: 'center', prop: 'activePow', istrue:true, width: '140px'},
          { label: '总视在功率(kW)', align: 'center', prop: 'apparentPow', istrue:true, width: '140px'},
          { label: '功率因素', align: 'center', prop: 'powerFactor', istrue:true},
          { label: '时间', align: 'center', prop: 'createTime', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '操作', align: 'center', slot: 'actions', istrue:true},
        ];
      }else{
        checkColumns.value.forEach(column => {
            column.istrue = true;
          }  
        );
        tableColumns.value = [
          { label: '编号', align: 'center', prop: 'id', istrue:true},
          { label: '位置', align: 'center', prop: 'location', istrue:true, width: '180px'},
          { label: '平均有功功率(kVA)', align: 'center', prop: 'activePowAvgValue', istrue:true, width: '180px'},
          { label: '最大有功功率(kVA)', align: 'center', prop: 'activePowMaxValue', istrue:true, width: '180px'},
          { label: '最大有功功率时间', align: 'center', prop: 'activePowMaxTime', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '最小有功功率(kVA)', align: 'center', prop: 'activePowMinValue', istrue:true, width: '180px'},
          { label: '最小有功功率时间', align: 'center', prop: 'activePowMinTime', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '平均视在功率(kW)', align: 'center', prop: 'apparentPowAvgValue', istrue:true, width: '180px'},
          { label: '最大视在功率(kW)', align: 'center', prop: 'apparentPowMaxValue', istrue:true, width: '180px'},
          { label: '最大视在功率时间', align: 'center', prop: 'apparentPowMaxTime', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '最小视在功率(kW)', align: 'center', prop: 'apparentPowMinValue', istrue:true, width: '180px'},
          { label: '最小视在功率时间', align: 'center', prop: 'apparentPowMinTime', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '操作', align: 'center', slot: 'actions', istrue:true},
        ];
      }
    }
    
    if (newType == 'line'){
      checkColumns.value.forEach(column => {
          column.isView = true;
          column.istrue = true;
        });
      if ( newGranularity == 'realtime'){
        tableColumns.value = [
          { label: '编号', align: 'center', prop: 'id', istrue:true},
          { label: '位置', align: 'center', prop: 'location', istrue:true, width: '180px'},
          { label: '相', align: 'center', prop: 'lineId', istrue:true},
          { label: '电压(V)', align: 'center', prop: 'vol', istrue:true},
          { label: '电流(A)', align: 'center', prop: 'cur', istrue:true},
          { label: '有功功率(kVA)', align: 'center', prop: 'activePow', istrue:true, width: '140px'},
          { label: '视在功率(kW)', align: 'center', prop: 'apparentPow', istrue:true, width: '140px'},
          { label: '功率因素', align: 'center', prop: 'powerFactor', istrue:true},
          { label: '时间', align: 'center', prop: 'createTime', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '操作', align: 'center', slot: 'actions', istrue:true},
        ];
      }else{
        checkColumns.value.forEach(column => {
          column.isView = true;
          if (column.prop === 'activePow') {
            column.istrue = true;
          }else{
            column.istrue = false;
          }  
        });
        tableColumns.value = [
          { label: '编号', align: 'center', prop: 'id', istrue:true},
          { label: '位置', align: 'center', prop: 'location', istrue:true, width: '180px'},
          { label: '相', align: 'center', prop: 'lineId', istrue:true},
          { label: '平均电压(V)', align: 'center', prop: 'volAvgValue', istrue:false, width: '140px'},
          { label: '最大电压(V)', align: 'center', prop: 'volMaxValue', istrue:false, width: '140px'},
          { label: '最大电压时间', align: 'center', prop: 'volMaxTime', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '最小电压(V)', align: 'center', prop: 'volMinValue', istrue:false, width: '140px'},
          { label: '最小电压时间', align: 'center', prop: 'volMinTime', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '平均电流(A)', align: 'center', prop: 'curAvgValue', istrue:false, width: '140px'},
          { label: '最大电流(A)', align: 'center', prop: 'curMaxValue', istrue:false, width: '140px'},
          { label: '最大电流时间', align: 'center', prop: 'curMaxTime', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '最小电流(A)', align: 'center', prop: 'curMinValue', istrue:false, width: '140px'},
          { label: '最小电流时间', align: 'center', prop: 'curMinTime', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '平均有功功率(kVA)', align: 'center', prop: 'activePowAvgValue', istrue:true, width: '180px'},
          { label: '最大有功功率(kVA)', align: 'center', prop: 'activePowMaxValue', istrue:true, width: '180px'},
          { label: '最大有功功率时间', align: 'center', prop: 'activePowMaxTime', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '最小有功功率(kVA)', align: 'center', prop: 'activePowMinValue', istrue:true, width: '180px'},
          { label: '最小有功功率时间', align: 'center', prop: 'activePowMinTime', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '平均视在功率(kW)', align: 'center', prop: 'apparentPowAvgValue', istrue:false, width: '180px'},
          { label: '最大视在功率(kW)', align: 'center', prop: 'apparentPowMaxValue', istrue:false, width: '180px'},
          { label: '最大视在功率时间', align: 'center', prop: 'apparentPowMaxTime', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '最小视在功率(kW)', align: 'center', prop: 'apparentPowMinValue', istrue:false, width: '180px'},
          { label: '最小视在功率时间', align: 'center', prop: 'apparentPowMinTime', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '操作', align: 'center', slot: 'actions', istrue:true},
        ];
      }
    }

    if (newType == 'loop'){
      checkColumns.value.forEach(column => {
          column.isView = true;
          column.istrue = true;
        });
      if ( newGranularity == 'realtime'){
        tableColumns.value = [
          { label: '编号', align: 'center', prop: 'id', istrue:true},
          { label: '位置', align: 'center', prop: 'location', istrue:true, width: '180px'},
          { label: '回路', align: 'center', prop: 'loopId', istrue:true},
          { label: '电压(V)', align: 'center', prop: 'vol', istrue:true},
          { label: '电流(A)', align: 'center', prop: 'cur', istrue:true},
          { label: '有功功率(kVA)', align: 'center', prop: 'activePow', istrue:true, width: '140px'},
          { label: '视在功率(kW)', align: 'center', prop: 'apparentPow', istrue:true, width: '140px'},
          { label: '功率因素', align: 'center', prop: 'powerFactor', istrue:true},
          { label: '时间', align: 'center', prop: 'createTime', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '操作', align: 'center', slot: 'actions', istrue:true},
        ];
      }else{
        checkColumns.value.forEach(column => {
          column.isView = true;
          if (column.prop === 'activePow') {
            column.istrue = true;
          }else{
            column.istrue = false;
          }  
        });
        tableColumns.value = [
          { label: '编号', align: 'center', prop: 'id', istrue:true},
          { label: '位置', align: 'center', prop: 'location', istrue:true, width: '180px'},
          { label: '回路', align: 'center', prop: 'loopId', istrue:true},
          { label: '平均电压(V)', align: 'center', prop: 'volAvgValue', istrue:false, width: '140px'},
          { label: '最大电压(V)', align: 'center', prop: 'volMaxValue', istrue:false, width: '140px'},
          { label: '最大电压时间', align: 'center', prop: 'volMaxTime', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '最小电压(v)', align: 'center', prop: 'volMinValue', istrue:false, width: '140px'},
          { label: '最小电压时间', align: 'center', prop: 'volMinTime', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '平均电流(A)', align: 'center', prop: 'curAvgValue', istrue:false, width: '140px'},
          { label: '最大电流(A)', align: 'center', prop: 'curMaxValue', istrue:false, width: '140px'},
          { label: '最大电流时间', align: 'center', prop: 'curMaxTime', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '最小电流(A)', align: 'center', prop: 'curMinValue', istrue:false, width: '140px'},
          { label: '最小电流时间', align: 'center', prop: 'curMinTime', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '平均有功功率(kVA)', align: 'center', prop: 'activePowAvgValue', istrue:true, width: '180px'},
          { label: '最大有功功率(kVA)', align: 'center', prop: 'activePowMaxValue', istrue:true, width: '180px'},
          { label: '最大有功功率时间', align: 'center', prop: 'activePowMaxTime', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '最小有功功率(kVA)', align: 'center', prop: 'activePowMinValue', istrue:true, width: '180px'},
          { label: '最小有功功率时间', align: 'center', prop: 'activePowMinTime', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '平均视在功率(kW)', align: 'center', prop: 'apparentPowAvgValue', istrue:false, width: '180px'},
          { label: '最大视在功率(kW)', align: 'center', prop: 'apparentPowMaxValue', istrue:false, width: '180px'},
          { label: '最大视在功率时间', align: 'center', prop: 'apparentPowMaxTime', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '最小视在功率(kW)', align: 'center', prop: 'apparentPowMinValue', istrue:false, width: '180px'},
          { label: '最小视在功率时间', align: 'center', prop: 'apparentPowMinTime', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '操作', align: 'center', slot: 'actions', istrue:true},
        ];
      }
    }

    if (newType == 'outlet'){
      checkColumns.value.forEach(column => {
        if (column.prop === 'vol') {
            column.isView = false;
          }else{
            column.isView = true;
          }
        column.istrue = true;
        });
      if ( newGranularity == 'realtime'){
        tableColumns.value = [
          { label: '编号', align: 'center', prop: 'id', istrue:true},
          { label: '位置', align: 'center', prop: 'location', istrue:true, width: '180px'},
          { label: '输出位', align: 'center', prop: 'outletId', istrue:true},
          { label: '电流(A)', align: 'center', prop: 'cur', istrue:true},
          { label: '有功功率(kVA)', align: 'center', prop: 'activePow', istrue:true, width: '140px'},
          { label: '视在功率(kW)', align: 'center', prop: 'apparentPow', istrue:true, width: '140px'},
          { label: '功率因素', align: 'center', prop: 'powerFactor', istrue:true},
          { label: '时间', align: 'center', prop: 'createTime', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '操作', align: 'center', slot: 'actions', istrue:true},
        ];
      }else{
        checkColumns.value.forEach(column => {
          if (column.prop === 'vol') {
            column.isView = false;
          }
          if (column.prop === 'activePow') {
            column.istrue = true;
          }else {
            column.istrue = false;
          }  
        });
        tableColumns.value = [
          { label: '编号', align: 'center', prop: 'id', istrue:true},
          { label: '位置', align: 'center', prop: 'location', istrue:true, width: '180px'},
          { label: '输出位', align: 'center', prop: 'outletId', istrue:true},
          { label: '平均电流(A)', align: 'center', prop: 'curAvgValue', istrue:false, width: '140px'},
          { label: '最大电流(A)', align: 'center', prop: 'curMaxValue', istrue:false, width: '140px'},
          { label: '最大电流时间', align: 'center', prop: 'curMaxTime', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '最小电流(A)', align: 'center', prop: 'curMinValue', istrue:false, width: '140px'},
          { label: '最小电流时间', align: 'center', prop: 'curMinTime', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '平均有功功率(kVA)', align: 'center', prop: 'activePowAvgValue', istrue:true, width: '180px'},
          { label: '最大有功功率(kVA)', align: 'center', prop: 'activePowMaxValue', istrue:true, width: '180px'},
          { label: '最大有功功率时间', align: 'center', prop: 'activePowMaxTime', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '最小有功功率(kVA)', align: 'center', prop: 'activePowMinValue', istrue:true, width: '180px'},
          { label: '最小有功功率时间', align: 'center', prop: 'activePowMinTime', formatter: dateFormatter, width: '200px', istrue:true},
          { label: '平均视在功率(kW)', align: 'center', prop: 'apparentPowAvgValue', istrue:false, width: '180px'},
          { label: '最大视在功率(kW)', align: 'center', prop: 'apparentPowMaxValue', istrue:false, width: '180px'},
          { label: '最大视在功率时间', align: 'center', prop: 'apparentPowMaxTime', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '最小视在功率(kW)', align: 'center', prop: 'apparentPowMinValue', istrue:false, width: '180px'},
          { label: '最小视在功率时间', align: 'center', prop: 'apparentPowMinTime', formatter: dateFormatter, width: '200px', istrue:false},
          { label: '操作', align: 'center', slot: 'actions', istrue:true},
        ];
      }
    }
  });

const tableColumns = ref([
    { label: '编号', align: 'center', prop: 'id' , istrue:true},
    { label: '位置', align: 'center', prop: 'location' , istrue:true, width: '180px'},
    { label: '总有功功率(kVA)', align: 'center', prop: 'activePow' , istrue:true},
    { label: '总视在功率(kW)', align: 'center', prop: 'apparentPow' , istrue:true},
    { label: '功率因素', align: 'center', prop: 'powerFactor' , istrue:true},
    { label: '时间', align: 'center', prop: 'createTime', formatter: dateFormatter, width: '200px' , istrue:true},
    { label: '操作', align: 'center', slot: 'actions' , istrue:true},
]);

const checkColumns = ref([
    { label: '电压', align: 'center', prop: 'vol' , istrue:false, isView:false},
    { label: '电流', align: 'center', prop: 'cur' , istrue:false, isView:false},
    { label: '有功功率', align: 'center', prop: 'activePow' , istrue:true, isView:true},
    { label: '视在功率', align: 'center', prop: 'apparentPow' , istrue:true, isView:true},
]);

const handleIsView = (label: string, istrue: boolean) => {
  tableColumns.value.forEach(column => {

      if (column.label.includes(label)) {
          column.istrue = !istrue;
        }  
      
  });
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
 // 生成假数据
const fakeData = [
    {
        id: 1,
        location: "机房1-机柜1",
        lineId: 123,
        loopId: 456,
        outletId: 789,
        vol: 220,
        cur: 10,
        activePow: 2200,
        apparentPow: 2300,
        volAvgValue: 210,
        volMaxValue: 230,
        volMaxTime: "2024-03-27 12:00:00",
        volMinValue: 200,
        volMinTime: "2024-03-27 06:00:00",
        curAvgValue: 12,
        curMaxValue: 14,
        curMaxTime: "2024-03-27 11:00:00",
        curMinValue: 10,
        curMinTime: "2024-03-27 05:00:00",
        activePowAvgValue: 2000,
        activePowMaxValue: 2500,
        activePowMaxTime: "2024-03-27 14:00:00",
        activePowMinValue: 1800,
        activePowMinTime: "2024-03-27 04:00:00",
        apparentPowAvgValue: 2200,
        apparentPowMaxValue: 2400,
        apparentPowMaxTime: "2024-03-27 15:00:00",
        apparentPowMinValue: 2100,
        apparentPowMinTime: "2024-03-27 03:00:00",
        powerFactor: 0.95,
        createTime: "2024-03-27 18:00:00"
    },
    {
        id: 2,
        location: "机房1-机柜1",
        lineId: 124,
        loopId: 457,
        outletId: 790,
        vol: 210,
        cur: 12,
        activePow: 2500,
        apparentPow: 2600,
        volAvgValue: 200,
        volMaxValue: 220,
        volMaxTime: "2024-03-27 11:00:00",
        volMinValue: 190,
        volMinTime: "2024-03-27 05:00:00",
        curAvgValue: 12,
        curMaxValue: 14,
        curMaxTime: "2024-03-27 11:00:00",
        curMinValue: 10,
        curMinTime: "2024-03-27 05:00:00",
        activePowAvgValue: 2300,
        activePowMaxValue: 2700,
        activePowMaxTime: "2024-03-27 13:00:00",
        activePowMinValue: 2100,
        activePowMinTime: "2024-03-27 02:00:00",
        apparentPowAvgValue: 2400,
        apparentPowMaxValue: 2600,
        apparentPowMaxTime: "2024-03-27 16:00:00",
        apparentPowMinValue: 2200,
        apparentPowMinTime: "2024-03-27 01:00:00",
        powerFactor: 0.96,
        createTime: "2024-03-27 17:00:00"
    },
    {
        id: 3,
        location: "机房1-机柜1",
        lineId: 124,
        loopId: 457,
        outletId: 790,
        vol: 210,
        cur: 12,
        activePow: 2500,
        apparentPow: 2600,
        volAvgValue: 200,
        volMaxValue: 220,
        volMaxTime: "2024-03-27 11:00:00",
        volMinValue: 190,
        volMinTime: "2024-03-27 05:00:00",
        curAvgValue: 12,
        curMaxValue: 14,
        curMaxTime: "2024-03-27 11:00:00",
        curMinValue: 10,
        curMinTime: "2024-03-27 05:00:00",
        activePowAvgValue: 2300,
        activePowMaxValue: 2700,
        activePowMaxTime: "2024-03-27 13:00:00",
        activePowMinValue: 2100,
        activePowMinTime: "2024-03-27 02:00:00",
        apparentPowAvgValue: 2400,
        apparentPowMaxValue: 2600,
        apparentPowMaxTime: "2024-03-27 16:00:00",
        apparentPowMinValue: 2200,
        apparentPowMinTime: "2024-03-27 01:00:00",
        powerFactor: 0.96,
        createTime: "2024-03-27 17:00:00"
    },{
        id: 4,
        location: "机房1-机柜1",
        lineId: 124,
        loopId: 457,
        outletId: 790,
        vol: 210,
        cur: 12,
        activePow: 2500,
        apparentPow: 2600,
        volAvgValue: 200,
        volMaxValue: 220,
        volMaxTime: "2024-03-27 11:00:00",
        volMinValue: 190,
        volMinTime: "2024-03-27 05:00:00",
        curAvgValue: 12,
        curMaxValue: 14,
        curMaxTime: "2024-03-27 11:00:00",
        curMinValue: 10,
        curMinTime: "2024-03-27 05:00:00",
        activePowAvgValue: 2300,
        activePowMaxValue: 2700,
        activePowMaxTime: "2024-03-27 13:00:00",
        activePowMinValue: 2100,
        activePowMinTime: "2024-03-27 02:00:00",
        apparentPowAvgValue: 2400,
        apparentPowMaxValue: 2600,
        apparentPowMaxTime: "2024-03-27 16:00:00",
        apparentPowMinValue: 2200,
        apparentPowMinTime: "2024-03-27 01:00:00",
        powerFactor: 0.96,
        createTime: "2024-03-27 17:00:00"
    },{
        id: 5,
        location: "机房1-机柜1",
        lineId: 124,
        loopId: 457,
        outletId: 790,
        vol: 210,
        cur: 12,
        activePow: 2500,
        apparentPow: 2600,
        volAvgValue: 200,
        volMaxValue: 220,
        volMaxTime: "2024-03-27 11:00:00",
        volMinValue: 190,
        volMinTime: "2024-03-27 05:00:00",
        curAvgValue: 12,
        curMaxValue: 14,
        curMaxTime: "2024-03-27 11:00:00",
        curMinValue: 10,
        curMinTime: "2024-03-27 05:00:00",
        activePowAvgValue: 2300,
        activePowMaxValue: 2700,
        activePowMaxTime: "2024-03-27 13:00:00",
        activePowMinValue: 2100,
        activePowMinTime: "2024-03-27 02:00:00",
        apparentPowAvgValue: 2400,
        apparentPowMaxValue: 2600,
        apparentPowMaxTime: "2024-03-27 16:00:00",
        apparentPowMinValue: 2200,
        apparentPowMinTime: "2024-03-27 01:00:00",
        powerFactor: 0.96,
        createTime: "2024-03-27 17:00:00"
    }
];

    // const data = await HistoryDataApi.getHistoryDataPage(queryParams)
    list.value = fakeData
    total.value = 5
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

/** 添加/修改操作 */
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

/** 详情操作*/
const toDetails = (id?: number) => {
  console.log(id)
}
/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await HistoryDataApi.deleteHistoryData(id)
    message.success(t('common.delSuccess'))
    // 刷新列表
    await getList()
  } catch {}
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