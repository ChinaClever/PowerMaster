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

        </el-form>

        <el-icon style="float: right; font-size: 33px" class="show-col-btn">
            <el-popover placement="bottom" trigger="click" width="220">
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
      id: number,
      location: string,
      cabinetId: number,
      totalApparentPow: number,
      aApparentPow: number,
      bApparentPow: number,
      totalActivePow: number,
      aActivePow: number,
      bActivePow: number,
      createTime:  string,

      totalApparentPowAvgValue: number,
      totalApparentPowMaxValue: number,
      totalApparentPowMaxTime:  string,
      totalApparentPowMinValue: number,
      totalApparentPowMinTime:  string,

      aApparentPowAvgValue: number,
      aApparentPowMaxValue: number,
      aApparentPowMaxTime:  string,
      aApparentPowMinValue: number,
      aApparentPowMinTime:  string,
     
      bApparentPowAvgValue: number,
      bApparentPowMaxValue: number,
      bApparentPowMaxTime: string,
      bApparentPowMinValue: number,
      bApparentPowMinTime:  string,

      totalActivePowAvgValue: number,
      totalActivePowMaxValue: number,
      totalActivePowMaxTime: string,
      totalActivePowMinValue: number,
      totalActivePowMinTime: string,

      aActivePowAvgValue: number,
      aActivePowMaxValue: number,
      aActivePowMaxTime:  string,
      aActivePowMinValue: number,
      aActivePowMinTime: string,
     
      bActivePowAvgValue: number,
      bActivePowMaxValue: number,
      bActivePowMaxTime:  string,
      bActivePowMinValue: number,
      bActivePowMinTime: string,
}>>([]); // 列表数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  id: undefined,
  granularity: 'realtime',
  ipAddr: undefined,
  createTime: undefined,
})
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中

watch(() => queryParams.granularity, (newValues) => {
    checkColumns.value.forEach(column => {
        column.istrue = true;
      }  
    );
    const newGranularity = newValues;
    if ( newGranularity == 'realtime'){
      tableColumns.value = [
        { label: '编号', align: 'center', prop: 'id' , istrue:true},
        { label: '位置', align: 'center', prop: 'location' , istrue:true, width: '180px'},
        { label: '总有功功率(kVA)', align: 'center', prop: 'totalActivePow' , istrue:true},
        { label: 'a路有功功率', align: 'center', prop: 'aActivePow' , istrue:true},
        { label: 'b路有功功率', align: 'center', prop: 'bActivePow' , istrue:true},
        { label: '总视在功率(kW)', align: 'center', prop: 'totalApparentPow' , istrue:true},
        { label: 'a路视在功率', align: 'center', prop: 'aApparentPow' , istrue:true},
        { label: 'b路视在功率', align: 'center', prop: 'bApparentPow' , istrue:true},
        { label: '时间', align: 'center', prop: 'createTime', formatter: dateFormatter, width: '200px' , istrue:true},
        { label: '操作', align: 'center', slot: 'actions' , istrue:true},
      ];
    }else{
      checkColumns.value.forEach(column => {
        if (column.prop != 'total' && column.prop != 'activePow') {
            column.istrue = false;
          }else{
            column.istrue = true;
          }
          
        }  
      );
      tableColumns.value = [
        { label: '编号', align: 'center', prop: 'id', istrue:true},
        { label: '位置', align: 'center', prop: 'location', istrue:true, width: '180px'},
        { label: '总平均有功功率(kVA)', align: 'center', prop: 'totalActivePowAvgValue', istrue:true, width: '180px'},
        { label: '总最大有功功率(kVA)', align: 'center', prop: 'totalActivePowMaxValue', istrue:true, width: '180px'},
        { label: '总最大有功功率时间', align: 'center', prop: 'totalActivePowMaxTime', formatter: dateFormatter, width: '200px', istrue:true},
        { label: '总最小有功功率(kVA)', align: 'center', prop: 'totalActivePowMinValue', istrue:true, width: '180px'},
        { label: '总最小有功功率时间', align: 'center', prop: 'totalActivePowMinTime', formatter: dateFormatter, width: '200px', istrue:true},
        { label: '总平均视在功率(kW)', align: 'center', prop: 'totalApparentPowAvgValue', istrue:false, width: '180px'},
        { label: '总最大视在功率(kW)', align: 'center', prop: 'totalApparentPowMaxValue', istrue:false, width: '180px'},
        { label: '总最大视在功率时间', align: 'center', prop: 'totalApparentPowMaxTime', formatter: dateFormatter, width: '200px', istrue:false},
        { label: '总最小视在功率(kW)', align: 'center', prop: 'totalApparentPowMinValue', istrue:false, width: '180px'},
        { label: '总最小视在功率时间', align: 'center', prop: 'totalApparentPowMinTime', formatter: dateFormatter, width: '200px', istrue:false},

        { label: 'a路平均有功功率(kVA)', align: 'center', prop: 'aActivePowAvgValue', istrue:false, width: '180px'},
        { label: 'a路最大有功功率(kVA)', align: 'center', prop: 'aActivePowMaxValue', istrue:false, width: '180px'},
        { label: 'a路最大有功功率时间', align: 'center', prop: 'aActivePowMaxTime', formatter: dateFormatter, width: '200px', istrue:false},
        { label: 'a路最小有功功率(kVA)', align: 'center', prop: 'aActivePowMinValue', istrue:false, width: '180px'},
        { label: 'a路最小有功功率时间', align: 'center', prop: 'aActivePowMinTime', formatter: dateFormatter, width: '200px', istrue:false},
        { label: 'a路平均视在功率(kW)', align: 'center', prop: 'aApparentPowAvgValue', istrue:false, width: '180px'},
        { label: 'a路最大视在功率(kW)', align: 'center', prop: 'aApparentPowMaxValue', istrue:false, width: '180px'},
        { label: 'a路最大视在功率时间', align: 'center', prop: 'aApparentPowMaxTime', formatter: dateFormatter, width: '200px', istrue:false},
        { label: 'a路最小视在功率(kW)', align: 'center', prop: 'aApparentPowMinValue', istrue:false, width: '180px'},
        { label: 'a路最小视在功率时间', align: 'center', prop: 'aApparentPowMinTime', formatter: dateFormatter, width: '200px', istrue:false},

        { label: 'b路平均有功功率(kVA)', align: 'center', prop: 'bActivePowAvgValue', istrue:false, width: '180px'},
        { label: 'b路最大有功功率(kVA)', align: 'center', prop: 'bActivePowMaxValue', istrue:false, width: '180px'},
        { label: 'b路最大有功功率时间', align: 'center', prop: 'bActivePowMaxTime', formatter: dateFormatter, width: '200px', istrue:false},
        { label: 'b路最小有功功率(kVA)', align: 'center', prop: 'bActivePowMinValue', istrue:false, width: '180px'},
        { label: 'b路最小有功功率时间', align: 'center', prop: 'bActivePowMinTime', formatter: dateFormatter, width: '200px', istrue:false},
        { label: 'b路平均视在功率(kW)', align: 'center', prop: 'bApparentPowAvgValue', istrue:false, width: '180px'},
        { label: 'b路最大视在功率(kW)', align: 'center', prop: 'bApparentPowMaxValue', istrue:false, width: '180px'},
        { label: 'b路最大视在功率时间', align: 'center', prop: 'bApparentPowMaxTime', formatter: dateFormatter, width: '200px', istrue:false},
        { label: 'b路最小视在功率(kW)', align: 'center', prop: 'bApparentPowMinValue', istrue:false, width: '180px'},
        { label: 'b路最小视在功率时间', align: 'center', prop: 'bApparentPowMinTime', formatter: dateFormatter, width: '200px', istrue:false},
        
        { label: '操作', align: 'center', slot: 'actions', istrue:true},
      ];
    }
    
  });

const tableColumns = ref([
    { label: '编号', align: 'center', prop: 'id' , istrue:true},
    { label: '位置', align: 'center', prop: 'location' , istrue:true, width: '180px'},
    { label: '总有功功率(kVA)', align: 'center', prop: 'totalActivePow' , istrue:true},
    { label: 'a路有功功率', align: 'center', prop: 'aActivePow' , istrue:true},
    { label: 'b路有功功率', align: 'center', prop: 'bActivePow' , istrue:true},
    { label: '总视在功率(kW)', align: 'center', prop: 'totalApparentPow' , istrue:true},
    { label: 'a路视在功率', align: 'center', prop: 'aApparentPow' , istrue:true},
    { label: 'b路视在功率', align: 'center', prop: 'bApparentPow' , istrue:true},
    { label: '时间', align: 'center', prop: 'createTime', formatter: dateFormatter, width: '200px' , istrue:true},
    { label: '操作', align: 'center', slot: 'actions' , istrue:true},
]);

const checkColumns = ref([
    { label: '总', align: 'center', prop: 'total' , istrue:true, isView:true},
    { label: 'a路', align: 'center', prop: 'a' , istrue:true, isView:true},
    { label: 'b路', align: 'center', prop: 'b' , istrue:true, isView:true},
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
      cabinetId: 123,
      totalApparentPow: 2000,
      aApparentPow: 2300,
      bApparentPow: 2300,
      totalActivePow: 2000,
      aActivePow: 2100,
      bActivePow: 2100,
      createTime: "2024-03-27 18:00:00",

      totalApparentPowAvgValue: 2300,
      totalApparentPowMaxValue: 2300,
      totalApparentPowMaxTime: "2024-03-27 14:00:00",
      totalApparentPowMinValue: 2200,
      totalApparentPowMinTime: "2024-03-27 14:00:00",

      aApparentPowAvgValue: 2200,
      aApparentPowMaxValue: 2400,
      aApparentPowMaxTime: "2024-03-27 15:00:00",
      aApparentPowMinValue: 2100,
      aApparentPowMinTime: "2024-03-27 03:00:00",
     
      bApparentPowAvgValue: 2200,
      bApparentPowMaxValue: 2400,
      bApparentPowMaxTime: "2024-03-27 15:00:00",
      bApparentPowMinValue: 2100,
      bApparentPowMinTime: "2024-03-27 03:00:00",

      totalActivePowAvgValue: 2300,
      totalActivePowMaxValue: 2300,
      totalActivePowMaxTime: "2024-03-27 14:00:00",
      totalActivePowMinValue: 2200,
      totalActivePowMinTime: "2024-03-27 14:00:00",

      aActivePowAvgValue: 2200,
      aActivePowMaxValue: 2400,
      aActivePowMaxTime: "2024-03-27 15:00:00",
      aActivePowMinValue: 2100,
      aActivePowMinTime: "2024-03-27 03:00:00",
     
      bActivePowAvgValue: 2200,
      bActivePowMaxValue: 2400,
      bActivePowMaxTime: "2024-03-27 15:00:00",
      bActivePowMinValue: 2100,
      bActivePowMinTime: "2024-03-27 03:00:00",
    },
    {
      id: 2,
      location: "机房1-机柜1",
      cabinetId: 123,
      totalApparentPow: 2000,
      aApparentPow: 2300,
      bApparentPow: 2300,
      totalActivePow: 2000,
      aActivePow: 2100,
      bActivePow: 2100,
      createTime: "2024-03-27 18:00:00",

      totalApparentPowAvgValue: 2300,
      totalApparentPowMaxValue: 2300,
      totalApparentPowMaxTime: "2024-03-27 14:00:00",
      totalApparentPowMinValue: 2200,
      totalApparentPowMinTime: "2024-03-27 14:00:00",

      aApparentPowAvgValue: 2200,
      aApparentPowMaxValue: 2400,
      aApparentPowMaxTime: "2024-03-27 15:00:00",
      aApparentPowMinValue: 2100,
      aApparentPowMinTime: "2024-03-27 03:00:00",
     
      bApparentPowAvgValue: 2200,
      bApparentPowMaxValue: 2400,
      bApparentPowMaxTime: "2024-03-27 15:00:00",
      bApparentPowMinValue: 2100,
      bApparentPowMinTime: "2024-03-27 03:00:00",

      totalActivePowAvgValue: 2300,
      totalActivePowMaxValue: 2300,
      totalActivePowMaxTime: "2024-03-27 14:00:00",
      totalActivePowMinValue: 2200,
      totalActivePowMinTime: "2024-03-27 14:00:00",

      aActivePowAvgValue: 2200,
      aActivePowMaxValue: 2400,
      aActivePowMaxTime: "2024-03-27 15:00:00",
      aActivePowMinValue: 2100,
      aActivePowMinTime: "2024-03-27 03:00:00",
     
      bActivePowAvgValue: 2200,
      bActivePowMaxValue: 2400,
      bActivePowMaxTime: "2024-03-27 15:00:00",
      bActivePowMinValue: 2100,
      bActivePowMinTime: "2024-03-27 03:00:00",
    },
    {
      id: 3,
      location: "机房1-机柜1",
      cabinetId: 123,
      totalApparentPow: 2000,
      aApparentPow: 2300,
      bApparentPow: 2300,
      totalActivePow: 2000,
      aActivePow: 2100,
      bActivePow: 2100,
      createTime: "2024-03-27 18:00:00",

      totalApparentPowAvgValue: 2300,
      totalApparentPowMaxValue: 2300,
      totalApparentPowMaxTime: "2024-03-27 14:00:00",
      totalApparentPowMinValue: 2200,
      totalApparentPowMinTime: "2024-03-27 14:00:00",

      aApparentPowAvgValue: 2200,
      aApparentPowMaxValue: 2400,
      aApparentPowMaxTime: "2024-03-27 15:00:00",
      aApparentPowMinValue: 2100,
      aApparentPowMinTime: "2024-03-27 03:00:00",
     
      bApparentPowAvgValue: 2200,
      bApparentPowMaxValue: 2400,
      bApparentPowMaxTime: "2024-03-27 15:00:00",
      bApparentPowMinValue: 2100,
      bApparentPowMinTime: "2024-03-27 03:00:00",

      totalActivePowAvgValue: 2300,
      totalActivePowMaxValue: 2300,
      totalActivePowMaxTime: "2024-03-27 14:00:00",
      totalActivePowMinValue: 2200,
      totalActivePowMinTime: "2024-03-27 14:00:00",

      aActivePowAvgValue: 2200,
      aActivePowMaxValue: 2400,
      aActivePowMaxTime: "2024-03-27 15:00:00",
      aActivePowMinValue: 2100,
      aActivePowMinTime: "2024-03-27 03:00:00",
     
      bActivePowAvgValue: 2200,
      bActivePowMaxValue: 2400,
      bActivePowMaxTime: "2024-03-27 15:00:00",
      bActivePowMinValue: 2100,
      bActivePowMinTime: "2024-03-27 03:00:00",
    },
    {
      id: 4,
      location: "机房1-机柜1",
      cabinetId: 123,
      totalApparentPow: 2000,
      aApparentPow: 2300,
      bApparentPow: 2300,
      totalActivePow: 2000,
      aActivePow: 2100,
      bActivePow: 2100,
      createTime: "2024-03-27 18:00:00",

      totalApparentPowAvgValue: 2300,
      totalApparentPowMaxValue: 2300,
      totalApparentPowMaxTime: "2024-03-27 14:00:00",
      totalApparentPowMinValue: 2200,
      totalApparentPowMinTime: "2024-03-27 14:00:00",

      aApparentPowAvgValue: 2200,
      aApparentPowMaxValue: 2400,
      aApparentPowMaxTime: "2024-03-27 15:00:00",
      aApparentPowMinValue: 2100,
      aApparentPowMinTime: "2024-03-27 03:00:00",
     
      bApparentPowAvgValue: 2200,
      bApparentPowMaxValue: 2400,
      bApparentPowMaxTime: "2024-03-27 15:00:00",
      bApparentPowMinValue: 2100,
      bApparentPowMinTime: "2024-03-27 03:00:00",

      totalActivePowAvgValue: 2300,
      totalActivePowMaxValue: 2300,
      totalActivePowMaxTime: "2024-03-27 14:00:00",
      totalActivePowMinValue: 2200,
      totalActivePowMinTime: "2024-03-27 14:00:00",

      aActivePowAvgValue: 2200,
      aActivePowMaxValue: 2400,
      aActivePowMaxTime: "2024-03-27 15:00:00",
      aActivePowMinValue: 2100,
      aActivePowMinTime: "2024-03-27 03:00:00",
     
      bActivePowAvgValue: 2200,
      bActivePowMaxValue: 2400,
      bActivePowMaxTime: "2024-03-27 15:00:00",
      bActivePowMinValue: 2100,
      bActivePowMinTime: "2024-03-27 03:00:00",
    },
    {
      id: 5,
      location: "机房1-机柜1",
      cabinetId: 123,
      totalApparentPow: 2000,
      aApparentPow: 2300,
      bApparentPow: 2300,
      totalActivePow: 2000,
      aActivePow: 2100,
      bActivePow: 2100,
      createTime: "2024-03-27 18:00:00",

      totalApparentPowAvgValue: 2300,
      totalApparentPowMaxValue: 2300,
      totalApparentPowMaxTime: "2024-03-27 14:00:00",
      totalApparentPowMinValue: 2200,
      totalApparentPowMinTime: "2024-03-27 14:00:00",

      aApparentPowAvgValue: 2200,
      aApparentPowMaxValue: 2400,
      aApparentPowMaxTime: "2024-03-27 15:00:00",
      aApparentPowMinValue: 2100,
      aApparentPowMinTime: "2024-03-27 03:00:00",
     
      bApparentPowAvgValue: 2200,
      bApparentPowMaxValue: 2400,
      bApparentPowMaxTime: "2024-03-27 15:00:00",
      bApparentPowMinValue: 2100,
      bApparentPowMinTime: "2024-03-27 03:00:00",

      totalActivePowAvgValue: 2300,
      totalActivePowMaxValue: 2300,
      totalActivePowMaxTime: "2024-03-27 14:00:00",
      totalActivePowMinValue: 2200,
      totalActivePowMinTime: "2024-03-27 14:00:00",

      aActivePowAvgValue: 2200,
      aActivePowMaxValue: 2400,
      aActivePowMaxTime: "2024-03-27 15:00:00",
      aActivePowMinValue: 2100,
      aActivePowMinTime: "2024-03-27 03:00:00",
     
      bActivePowAvgValue: 2200,
      bActivePowMaxValue: 2400,
      bActivePowMaxTime: "2024-03-27 15:00:00",
      bActivePowMinValue: 2100,
      bActivePowMinTime: "2024-03-27 03:00:00",
    },
];

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