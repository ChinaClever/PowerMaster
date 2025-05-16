<template>
  <CommonMenu :dataList="navList" @check="handleCheck" navTitle="机房电力数据">
    <template #NavInfo>
      <br/>
      <div class="nav_data">
        <div class="carousel-container">
          <!-- <el-carousel :interval="2500" motion-blur height="150px" arrow="never" trigger="click">
            <el-carousel-item v-for="(item, index) in carouselItems" :key="index">
              <img width="auto" height="auto" :src="item.imgUrl" alt="" class="carousel-image" />
            </el-carousel-item>
          </el-carousel> -->
        </div>
        <div class="nav_header">
          <div>最近一分钟:{{ minTotal }}条</div>
          <div>最近一小时:{{ hourTotal }}条</div>
          <div>最近一天:{{ dayTotal }}条</div>
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

          <el-form-item >
            <el-button @click="handleQuery"  style="background-color: #00778c;color:#ffffff;font-size: 13px;"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
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
          
          <el-form-item style="position: absolute;right: 0;">
            <el-button
              type="success"
              plain
              @click="handleExport"
              :loading="exportLoading"
               style="background-color: #00778c;color:#ffffff;font-size: 13px;position:absolute;top: 2px;right: -28px;"
            >
              <Icon icon="ep:download" class="mr-5px" /> 导出
            </el-button>
          </el-form-item>
        </el-form>
      </template>
      <template #Content>
        <el-table v-loading="loading" :data="list" :border="true" :stripe="true" :show-overflow-tooltip="true" :header-cell-style="headCellStyle">
           <!-- 添加行号列 -->
          <el-table-column label="序号" align="center" width="60px">
            <template #default="{ $index }">
              {{ $index + 1 + (queryParams.pageNo - 1) * queryParams.pageSize }}
            </template>
          </el-table-column>
          <!-- 遍历其他列 -->
          <template v-for="column in tableColumns">
            <el-table-column :key="column.prop" :label="column.label" :align="column.align" :prop="column.prop" :formatter="column.formatter" :min-width="column.width" v-if="column.istrue&&column.label!='操作'" >
              <!-- <template #default="{ row }" v-if="column.slot === 'actions'">
                <el-button type="primary" @click="toDetails(row.room_id, row.location)">详情</el-button>
              </template> -->
            </el-table-column>
          </template>
          <el-table-column label="操作" align="center" width="90" fixed="right">
            <template #default="{ row }">
                <el-button type="primary" @click="toDetails(row.room_id, row.location)"  style="background-color: #00778c;color:#ffffff;font-size: 13px;">详情</el-button>
              </template>
          </el-table-column>
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
import { HistoryDataApi } from '@/api/room/historydata'
import { IndexApi } from '@/api/room/roomindex'
import dayjs from 'dayjs'
import { on } from 'events'
// import PDUImage from '@/assets/imgs/PDU.jpg';
const { push } = useRouter()
/** 机房历史数据 列表 */
defineOptions({ name: 'RoomHistoryData' })

const navList = ref([]) as any // 左侧导航栏树结构列表
const navTotalData = ref(0)
const message = useMessage() // 消息弹窗
const loading = ref(true) // 列表的加载中
const list = ref<Array<{ }>>([]); // 列表数据
const total = ref(0) // 数据总条数 超过10000条为10000
const realTotel = ref(0) // 数据的真实总条数
const pageSizeArr = ref([15,30,50,100])
const minTotal=ref(0)
const hourTotal=ref(0)
const dayTotal=ref(0)
let now=new Date()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 15,
  granularity: 'realtime',
  timeRange: [],
  roomIds:[]
})
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中
// const carouselItems = ref([
//       { imgUrl: PDUImage},
//       { imgUrl: PDUImage},
//       { imgUrl: PDUImage},
//       { imgUrl: PDUImage},
//     ]);//侧边栏轮播图图片路径
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
const defaultOptionsCol = ref([["total", "active_total"], ["total", "apparent_total"], ["total", "reactive_total"], ["total", "factor_total"]])
const optionsCol = ref([
  {
    value: "total",
    label: '总',
    children: [
      { value: "active_total", label: '有功功率' },
      { value: "apparent_total", label: '视在功率' },
      { value: "reactive_total", label: '无功功率' },
      { value: "factor_total", label: '功率因素' },
    ],
  },
  {
    value: "A",
    label: 'A路',
    children: [
      { value: "active_a", label: '有功功率' },
      { value: "apparent_a", label: '视在功率' },
      { value: "reactive_a", label: '无功功率' },
      { value: "factor_a", label: '功率因素' },
    ],
  },
  {
    value: "B",
    label: 'B路',
    children: [
      { value: "active_b", label: '有功功率' },
      { value: "apparent_b", label: '视在功率' },
      { value: "reactive_b", label: '无功功率' },
      { value: "factor_b", label: '功率因素' },
    ],
  },
])
const originalArray = ref([
      "active_total", "apparent_total","reactive_total", "factor_total", 
      "active_a", "apparent_a", "reactive_a", "factor_a", 
      "active_b", "apparent_b", "reactive_b", "factor_b"
])

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
// const granularityChange = () => {
//    getNavNewData()
// }

watch(() => queryParams.granularity, (newValues) => { 
    const newGranularity = newValues;
    if ( newGranularity == 'realtime'){
      defaultOptionsCol.value = [["total", "active_total"], ["total", "apparent_total"], ["total", "reactive_total"], ["total", "factor_total"]]
      optionsCol.value = [
        {
        value: "total",
        label: '总',
        children: [
          { value: "active_total", label: '有功功率' },
          { value: "apparent_total", label: '视在功率' },
          { value: "reactive_total", label: '无功功率' },
          { value: "factor_total", label: '功率因素' },
        ],
        },
        {
          value: "A",
          label: 'A路',
          children: [
            { value: "active_a", label: '有功功率' },
            { value: "apparent_a", label: '视在功率' },
            { value: "reactive_a", label: '无功功率' },
            { value: "factor_a", label: '功率因素' },
          ],
        },
        {
          value: "B",
          label: 'B路',
          children: [
            { value: "active_b", label: '有功功率' },
            { value: "apparent_b", label: '视在功率' },
            { value: "reactive_b", label: '无功功率' },
            { value: "factor_b", label: '功率因素' },
          ],
        },
      ]
      originalArray.value = [ "active_total", "apparent_total","reactive_total", "factor_total", 
                            "active_a", "apparent_a", "reactive_a", "factor_a", 
                            "active_b", "apparent_b", "reactive_b", "factor_b"
                          ]        
 
      tableColumns.value =[
        { label: '位置', align: 'center', prop: 'location' , width: '230px' , istrue:true},
        { label: '保存策略',align: 'center',prop: 'data_source', istrue:true, width: '100px',formatter:formatSave},
        { label: '时间', align: 'center', prop: 'create_time', formatter: dateFormatter, width: '200px' , istrue:true},
        { label: '总有功功率(kW)', align: 'center', prop: 'active_total' , istrue:true, width: '180px', formatter: formatPower},
        { label: '总视在功率(kVA)', align: 'center', prop: 'apparent_total' , istrue:true, width: '180px', formatter: formatPower},
        { label: '总无功功率(kVar)', align: 'center', prop: 'reactive_total' , istrue:true, width: '180px', formatter: formatPower},
        { label: '总功率因素', align: 'center', prop: 'factor_total' , istrue:true, width: '180px', formatter: formatPowerFactor},

        { label: 'A路有功功率(kW)', align: 'center', prop: 'active_a' , istrue:false, width: '180px', formatter: formatPower},
        { label: 'A路视在功率(kVA)', align: 'center', prop: 'apparent_a' , istrue:false, width: '180px', formatter: formatPower},
        { label: 'A路无功功率(kVar)', align: 'center', prop: 'reactive_a' , istrue:false, width: '180px', formatter: formatPower},
        { label: 'A路功率因素', align: 'center', prop: 'factor_a' , istrue:false, width: '180px', formatter: formatPowerFactor},
        
        { label: 'B路有功功率(kW)', align: 'center', prop: 'active_b' , istrue:false, width: '180px', formatter: formatPower},
        { label: 'B路视在功率(kVA)', align: 'center', prop: 'apparent_b' , istrue:false, width: '180px', formatter: formatPower},
        { label: 'B路无功功率(kVar)', align: 'center', prop: 'reactive_b' , istrue:false, width: '180px', formatter: formatPower},
        { label: 'B路功率因素', align: 'center', prop: 'factor_b' , istrue:false, width: '180px', formatter: formatPowerFactor},
        { label: '操作', align: 'center', slot: 'actions' , istrue:true},
      ]
      queryParams.pageNo = 1;
      queryParams.pageSize = 15;
      getList();
    }else{
      defaultOptionsCol.value = [
        ["total", "active_pow", "active_total_max"],["total", "apparent_pow", "apparent_total_max"],["total", "reactive_pow", "reactive_total_max"],
        ["total", "factor_total_avg_value"],
      ]
      optionsCol.value = [
        {
        value: "total",
        label: '总',
        children: [
            { value: "active_pow", label: '有功功率', children:[
                { value: "active_total_avg_value", label: '平均' },
                { value: "active_total_max", label: '最大' },
                { value: "active_total_min", label: '最小' }
              ] 
            },
            { value: "apparent_pow", label: '视在功率', children:[
                { value: "apparent_total_avg_value", label: '平均' },
                { value: "apparent_total_max", label: '最大' },
                { value: "apparent_total_min", label: '最小' }
              ] 
            },
            { value: "reactive_pow", label: '无功功率', children:[
                { value: "reactive_total_avg_value", label: '平均' },
                { value: "reactive_total_max", label: '最大' },
                { value: "reactive_total_min", label: '最小' }
              ] 
            },
            { value: "factor_total_avg_value", label: '平均功率因素'},
          ],
        },
        {
          value: "A",
          label: 'A路',
          children: [
          { value: "active_pow", label: '有功功率', children:[
              { value: "active_a_avg_value", label: '平均' },
              { value: "active_a_max", label: '最大' },
              { value: "active_a_min", label: '最小' }
            ] 
          },
          { value: "apparent_pow", label: '视在功率', children:[
              { value: "apparent_a_avg_value", label: '平均' },
              { value: "apparent_a_max", label: '最大' },
              { value: "apparent_a_min", label: '最小' }
            ] 
           },
           { value: "reactive_a_avg_value", label: '平均无功功率'},
           { value: "factor_a_avg_value", label: '平均功率因素'},
          ],
        },
        {
          value: "B",
          label: 'B路',
          children: [
          { value: "active_pow", label: '有功功率', children:[
              { value: "active_b_avg_value", label: '平均' },
              { value: "active_b_max", label: '最大' },
              { value: "active_b_min", label: '最小' }
            ] 
          },
          { value: "apparent_pow", label: '视在功率', children:[
              { value: "apparent_b_avg_value", label: '平均' },
              { value: "apparent_b_max", label: '最大' },
              { value: "apparent_b_min", label: '最小' }
            ] 
           },
           { value: "reactive_b_avg_value", label: '平均无功功率'},
            { value: "factor_b_avg_value", label: '平均功率因素'},
          ],
        },
      ] as any;
      originalArray.value = [
        "active_total_avg_value", "active_total_max", "active_total_min", "apparent_total_avg_value", "apparent_total_max", "apparent_total_min", 
        "reactive_total_min", "reactive_total_max", "reactive_total_avg_value", "factor_total_avg_value",
        "active_a_avg_value", "active_a_max", "active_a_min", "apparent_a_avg_value", "apparent_a_max", "apparent_a_min",
       "reactive_a_avg_value", "factor_a_avg_value",
        "active_b_avg_value", "active_b_max", "active_b_min", "apparent_b_avg_value", "apparent_b_max", "apparent_b_min",
        "reactive_b_avg_value", "factor_b_avg_value",
      ]    

      tableColumns.value = [
        { label: '位置', align: 'center', prop: 'location' , width: '240px' , istrue:true},
        { label: '记录时间', align: 'center', prop: 'create_time' , width: '200px', istrue:true},
        { label: '总平均有功功率(kW)', align: 'center', prop: 'active_total_avg_value', istrue:false, width: '180px', formatter: formatPower},
        { label: '总最大有功功率(kW)', align: 'center', prop: 'active_total_max_value', istrue:true, width: '180px', formatter: formatPower},
        { label: '总最大有功功率时间', align: 'center', prop: 'active_total_max_time', formatter: dateFormatter, width: '200px', istrue:true},
        { label: '总最小有功功率(kW)', align: 'center', prop: 'active_total_min_value', istrue:false, width: '180px', formatter: formatPower},
        { label: '总最小有功功率时间', align: 'center', prop: 'active_total_min_time', formatter: dateFormatter, width: '200px', istrue:false},
        { label: '总平均视在功率(kVA)', align: 'center', prop: 'apparent_total_avg_value', istrue:false, width: '180px', formatter: formatPower},
        { label: '总最大视在功率(kVA)', align: 'center', prop: 'apparent_total_max_value', istrue:true, width: '180px', formatter: formatPower},
        { label: '总最大视在功率时间', align: 'center', prop: 'apparent_total_max_time', formatter: dateFormatter, width: '200px', istrue:true},
        { label: '总最小视在功率(kVA)', align: 'center', prop: 'apparent_total_min_value', istrue:false, width: '180px', formatter: formatPower},
        { label: '总最小视在功率时间', align: 'center', prop: 'apparent_total_min_time', formatter: dateFormatter, width: '200px', istrue:false},

        { label: '总平均无功功率(kVar)', align: 'center', prop: 'reactive_total_avg_value', istrue:false, width: '180px', formatter: formatPower},
        { label: '总最大无功功率(kVAar)', align: 'center', prop: 'reactive_total_max_value', istrue:true, width: '180px', formatter: formatPower},
        { label: '总最大无功功率时间', align: 'center', prop: 'reactive_total_max_time', formatter: dateFormatter, width: '200px', istrue:true},
        { label: '总最小无功功率(kVAar)', align: 'center', prop: 'reactive_total_min_value', istrue:false, width: '180px', formatter: formatPower},
        { label: '总最小无功功率时间', align: 'center', prop: 'reactive_total_min_time', formatter: dateFormatter, width: '200px', istrue:false},
        { label: '总平均功率因素', align: 'center', prop: 'factor_total_avg_value', istrue:true, width: '180px', formatter: formatPowerFactor},

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

        { label: 'A路平均无功功率(kVar)', align: 'center', prop: 'reactive_a_avg_value', istrue:false, width: '180px', formatter: formatPower},
        { label: 'A路平均功率因素', align: 'center', prop: 'factor_a_avg_value', istrue:false, width: '180px', formatter: formatPowerFactor},

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

        { label: 'B路平均无功功率(kVar)', align: 'center', prop: 'reactive_b_avg_value', istrue:false, width: '180px', formatter: formatPower},
        { label: 'B路平均功率因素', align: 'center', prop: 'factor_b_avg_value', istrue:false, width: '180px', formatter: formatPowerFactor},
        { label: '操作', align: 'center', slot: 'actions', istrue:true},
      ];
      queryParams.pageNo = 1;
      queryParams.pageSize = 15;
      getList();
    }
    
});

const tableColumns = ref([
  { label: '位置', align: 'center', prop: 'location' , width: '230px' , istrue:true},
  { label: '保存策略',align: 'center',prop: 'data_source', istrue:true, width: '100px',formatter:formatSave},
  { label: '时间', align: 'center', prop: 'create_time', formatter: dateFormatter, width: '200px' , istrue:true},
  { label: '总有功功率(kW)', align: 'center', prop: 'active_total' , istrue:true, width: '180px', formatter: formatPower},
  { label: '总视在功率(kVA)', align: 'center', prop: 'apparent_total' , istrue:true, width: '180px', formatter: formatPower},
  { label: '总无功功率(kVar)', align: 'center', prop: 'reactive_total' , istrue:true, width: '180px', formatter: formatPower},
  { label: '总功率因素', align: 'center', prop: 'factor_total' , istrue:true, width: '180px', formatter: formatPowerFactor},

  { label: 'A路有功功率(kW)', align: 'center', prop: 'active_a' , istrue:false, width: '180px', formatter: formatPower},
  { label: 'A路视在功率(kVA)', align: 'center', prop: 'apparent_a' , istrue:false, width: '180px', formatter: formatPower},
  { label: 'A路无功功率(kVar)', align: 'center', prop: 'reactive_a' , istrue:false, width: '180px', formatter: formatPower},
  { label: 'A路功率因素', align: 'center', prop: 'factor_a' , istrue:false, width: '180px', formatter: formatPowerFactor},
  
  { label: 'B路有功功率(kW)', align: 'center', prop: 'active_b' , istrue:false, width: '180px', formatter: formatPower},
  { label: 'B路视在功率(kVA)', align: 'center', prop: 'apparent_b' , istrue:false, width: '180px', formatter: formatPower},
  { label: 'B路无功功率(kVar)', align: 'center', prop: 'reactive_b' , istrue:false, width: '180px', formatter: formatPower},
  { label: 'B路功率因素', align: 'center', prop: 'factor_b' , istrue:false, width: '180px', formatter: formatPowerFactor},
  { label: '操作', align: 'center', slot: 'actions' , istrue:true},
]);


/** 查询列表 */
const getList = async (isLoading = true) => {
  loading.value = isLoading
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

function formatSave(_row: any, _column: any, cellValue: number): string {
  if(cellValue==0) return '定时记录';
  if(cellValue==1) return '波动数据';
  if(cellValue==2) return '突变数据';
  if(cellValue==3) return '告警数据';
  return '';
}

// 格式化功率因素列数据，保留两位小数
function formatPowerFactor(_row: any, _column: any, cellValue: number): string {
  return Number(cellValue).toFixed(2);
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
  node.forEach(item => arr.push(item.id));
  queryParams.roomIds = arr
  handleQuery()
}

// 接口获取机房导航列表
const getNavList = async() => {
  const res = await IndexApi.getRoomList()
  // navList.value = res
    navList.value=res.map((item)=>{return {id:item.id,name:item.roomName,children:[]}})
}

// 获取导航的数据显示
const getNavNewData = async() => {
  let promiseArr = []
  promiseArr.push(HistoryDataApi.getNavNewData("realtime"));
  promiseArr.push(HistoryDataApi.getNavNewData("hour"));
  promiseArr.push(HistoryDataApi.getNavNewData("day"));
  Promise.all(promiseArr).then((res) => {
    minTotal.value=res[0].total;
    hourTotal.value=res[1].total;
    dayTotal.value=res[2].total;
  }).catch(()=>{
    minTotal.value=0;
    hourTotal.value=0;
    dayTotal.value=0;
  })
}

/** 详情操作*/
const toDetails = (roomId: number, location:string) => {
  push({path:"/room/record/historyLine",state:{roomId,location,start:(queryParams.timeRange!=null&&queryParams.timeRange.length==2?queryParams.timeRange[0]:''),
  end:(queryParams.timeRange!=null&&queryParams.timeRange.length==2?queryParams.timeRange[1]:'')}});
  // push('/room/record/historyLine?roomId='+roomId+'&location='+location+"&start="+(queryParams.timeRange!=null&&queryParams.timeRange.length==2?queryParams.timeRange[0]:'')+"&end="+(queryParams.timeRange!=null&&queryParams.timeRange.length==2?queryParams.timeRange[1]:''));
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
    await download.excel(data, '机房电力数据.xlsx')
  } catch (error) {
    // 处理异常
    console.error('导出失败：', error)
  } finally {
    exportLoading.value = false
  }
}
const intervalId:any=ref(null)
/** 初始化 **/
onMounted(() => {
  getNavList()
  getNavNewData()
  getList()
  intervalId.value=setInterval(() => {
    getList(false)
  }, 60000)
})
onBeforeUnmount(() => {
  if(intervalId.value!=null)
  clearInterval(intervalId.value)
})
function headCellStyle(){
  return {
    background:"rgb(245, 247, 250)"
  }
}
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
    /* display: flex; */
    /* flex-direction: column;
    align-items: center; */
    font-size: 14px;
    /* font-weight: bold; */
    margin-left: 10px;
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

/deep/ .el-pagination.is-background .el-pager li.is-active {
  background-color: #00778c;
}
    /deep/  .el-pager li:hover {
    color: #00778c;
}
</style>
