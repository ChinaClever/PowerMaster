<template>
  <CommonMenu :dataList="navList" @check="handleCheck" navTitle="插接箱历史数据">
    <template #NavInfo>
        <br/>    <br/> 
      <div class="nav_data">
        <!-- <div class="carousel-container"> -->
          <!-- <el-carousel :interval="2500" motion-blur height="150px" arrow="never" trigger="click">
            <el-carousel-item v-for="(item, index) in carouselItems" :key="index">
              <img width="auto" height="auto" :src="item.imgUrl" alt="" class="carousel-image" />
            </el-carousel-item>
          </el-carousel> -->
        <!-- </div>
        <div class="nav_header">
          <br/>
          <span v-if="queryParams.granularity == 'realtime' ">全部插接箱最近一分钟新增记录</span>
          <span v-if="queryParams.granularity == 'hour' ">全部插接箱最近一小时新增记录</span>
          <span v-if="queryParams.granularity == 'day' ">全部插接箱最近一天新增记录</span>
        </div>
        <div class="nav_content" >
          <el-descriptions title="" direction="vertical" :column="1" border >
            <el-descriptions-item label="总数据"><span >{{ navTotalData }} 条</span></el-descriptions-item>
            <el-descriptions-item label="相数据"><span >{{ navLineData }} 条</span></el-descriptions-item>
          </el-descriptions>
        </div> -->

        <div class="descriptions-container" style="font-size: 14px;">
          <div class="description-item">
            <span class="label">总数据 :</span>
            <span class="value">{{ navTotalData }}条</span>
          </div>
          <div class="description-item">
            <span class="label">相数据 :</span>
            <span class="value">{{ navLineData }}条</span>
          </div>
          <div class="description-item">
            <span class="label">回路数据 :</span>
            <span class="value">{{ navLoopData }}条</span>
          </div>
          <div class="description-item">
            <span class="label">输出位数据 :</span>
            <span class="value">{{ navOutletData }}条</span>
          </div>


          <div style="font-size: 14px">
            <div v-if="queryParams.granularity == 'realtime' "><span>插接箱最近一分钟新增记录</span></div>
              <div v-if="queryParams.granularity == 'hour' " ><span>插接箱最近一小时新增记录</span></div>
              <div v-if="queryParams.granularity == 'day' " ><span>插接箱最近一天新增记录</span></div>
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
        <el-form-item label="参数类型" prop="type">
        <el-cascader
          v-model="defaultSelected"
          collapse-tags
          :options="typeSelection"
          collapse-tags-tooltip
          :show-all-levels="true"
          @change="typeCascaderChange"
          class="!w-110px"
        />
      </el-form-item>

        <el-form-item label="颗粒度" prop="granularity">
          <el-select
            v-model="queryParams.granularity"
            placeholder="请选择分钟/小时/天"
            @change="granularityChange"
            class="!w-100px">
            <el-option label="分钟" value="realtime" />
            <el-option label="小时" value="hour" />
            <el-option label="天" value="day" />
          </el-select>
        </el-form-item>

        <el-form-item label="筛选列" prop="optionsCol">
          <el-cascader
            v-model="defaultOptionsCol"
            :options="optionsCol"
            :props="props"
            collapse-tags
            collapse-tags-tooltip
            :show-all-levels="false"
            @change="cascaderChange"
            class="!w-180px"
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
          :disabled-date="disabledDate" 
          class="!w-335px"
        />
        </el-form-item>
        <!-- <div style="float:right; padding-right:78px"> -->
        <el-form-item >
          <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
          <!-- <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button> -->
          <el-button type="success" plain @click="handleExport" :loading="exportLoading">
            <Icon icon="ep:download" class="mr-5px" /> 导出
          </el-button>
        </el-form-item>
        <!-- </div> -->
      </el-form>
    </template>
    <template #Content>
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
              <el-button link type="primary" @click="toDetails(row.box_id, row.location)">详情</el-button>
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
    </template>
  </CommonMenu>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import download from '@/utils/download'
import { HistoryDataApi } from '@/api/bus/historydata'
import { IndexApi } from '@/api/bus/busindex'
const { push } = useRouter()
/** 插接箱历史数据 列表 */
defineOptions({ name: 'BusHistoryData' })

const navList = ref([]) as any // 左侧导航栏树结构列表
const navTotalData = ref(0)
const navLineData = ref(0)
const navLoopData = ref(0)
const navOutletData = ref(0)
const message = useMessage() // 消息弹窗
const loading = ref(true) // 列表的加载中
const list = ref<Array<{ }>>([]); // 列表数据
const total = ref(0) // 数据总条数 超过10000条为10000
const realTotel = ref(0) // 数据的真实总条数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 15,
  lineId: undefined,
  loopId: undefined,
  outletId: undefined,
  type: 'total', 
  granularity: 'realtime',
  timeRange: undefined,
  devkeys: [],
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

// 参数类型选项
const defaultSelected = ref(['total'])
const typeSelection = ref([]) as any;

// 参数类型改变触发
const typeCascaderChange = (selected) => {
  queryParams.type = selected[0];
  switch(selected[0]){
    case 'line':
      queryParams.lineId = selected[1];
      queryParams.loopId = undefined;
      queryParams.outletId = undefined;
      break;
    case 'loop':
      queryParams.loopId = selected[1];
      queryParams.lineId = undefined;
      queryParams.outletId = undefined;
      break;
    case 'outlet':
      queryParams.outletId = selected[1];
      queryParams.loopId = undefined;
      queryParams.lineId = undefined;
      break;
    case 'total':
      queryParams.lineId = undefined;
      queryParams.loopId = undefined;
      queryParams.outletId = undefined;
      break;
  }
  // 自动搜索
  handleQuery()
}

//筛选选项
const props = { multiple: true}
const defaultOptionsCol = ref([["pow_active"], ["pow_reactive"], ["pow_apparent"], ["power_factor"]])
const optionsCol = ref([
  { value: "pow_active", label: '总有功功率'},
  { value: "pow_reactive", label: '总无功功率'},
  { value: "pow_apparent", label: '总视在功率'},
  { value: "power_factor", label: '功率因素'},
])
const originalArray = ref(["pow_active", "pow_reactive", "pow_apparent", "power_factor"])

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
const granularityChange = () => {
  getBoxNavNewData()
}

// 最后一页显示数据量过大的提示
const shouldShowDataExceedMessage = computed(() => {
  const lastPageNo = Math.ceil(total.value / queryParams.pageSize);
  return queryParams.pageNo === lastPageNo && total.value >= 10000;
});

watch(() => [queryParams.type, queryParams.granularity], (newValues) => {
    const [newType, newGranularity] = newValues;
    // 处理参数变化
    if (newType == 'total'){
      if ( newGranularity == 'realtime'){
        // 配置筛选列
        defaultOptionsCol.value = [["pow_active"], ["pow_reactive"], ["pow_apparent"], ["power_factor"]];
        optionsCol.value = [
          { value: "pow_active", label: '总有功功率'},
          { value: "pow_reactive", label: '总无功功率'},
          { value: "pow_apparent", label: '总视在功率'},
          { value: "power_factor", label: '功率因素'},
        ];
        originalArray.value = ["pow_active", "pow_reactive", "pow_apparent", "power_factor"];
        // 配置表格列
        tableColumns.value =([
          { label: '所在位置', align: 'center', prop: 'location' , width: '250px', istrue:true},
          { label: '设备地址', align: 'center', prop: 'dev_key', width: '250px', istrue:true},
          { label: '设备名称', align: 'center', prop: 'bus_name', istrue:true, width: '300%'},

          { label: '总有功功率(kW)', align: 'center', prop: 'pow_active', istrue:true, width: '160px', formatter: formatPower},
          { label: '总无功功率(kVar)', align: 'center', prop: 'pow_reactive', istrue:true, width: '160px', formatter: formatPower},
          { label: '总视在功率(kVA)', align: 'center', prop: 'pow_apparent', istrue:true, width: '160px', formatter: formatPower},
          { label: '功率因素', align: 'center', prop: 'power_factor' , istrue:true, width: '160px', formatter: formatPowerFactor},
          { label: '时间', align: 'center', prop: 'create_time', formatter: formatTime, width: '200px', istrue:true},
          { label: '操作', align: 'center', slot: 'actions' , istrue:true, width: '160px'},
        ]);
        queryParams.pageNo = 1;
        getList();
      }else{
        // 配置筛选列
        defaultOptionsCol.value = [
          ["pow_active", "pow_active_avg_value"],["pow_active", "pow_active_max"], ["pow_active", "pow_active_min"]
        ];
        optionsCol.value = [
          { value: "pow_active", label: '有功功率', children: [
              { value: "pow_active_avg_value", label: '平均有功功率'},
              { value: "pow_active_max", label: '最大有功功率' },
              { value: "pow_active_min", label: '最小有功功率' },
            ]
          },
          { value: "pow_reactive", label: '无功功率', children: [
              { value: "pow_reactive_avg_value", label: '平均无功功率'},
              { value: "pow_reactive_max", label: '最大无功功率' },
              { value: "pow_reactive_min", label: '最小无功功率' },
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
                            "pow_reactive_avg_value", "pow_reactive_max", "pow_reactive_min",
                            "pow_apparent_avg_value", "pow_apparent_max", "pow_apparent_min",
                        ],
        // 配置表格列
        tableColumns.value = [
          { label: '所在位置', align: 'center', prop: 'location', istrue:true, width: '250px'},
          { label: '设备地址', align: 'center', prop: 'dev_key' , istrue:true, width: '250px'},
          { label: '设备名称', align: 'center', prop: 'bus_name', istrue:true, width: '300%'},

          { label: '平均有功功率(kW)', align: 'center', prop: 'pow_active_avg_value', istrue:true, width: '160px', formatter: formatPower},
          { label: '最大有功功率(kW)', align: 'center', prop: 'pow_active_max_value', istrue:true, width: '160px', formatter: formatPower},
          { label: '最大有功功率时间', align: 'center', prop: 'pow_active_max_time', formatter: formatTime, width: '200px', istrue:true},
          { label: '最小有功功率(kW)', align: 'center', prop: 'pow_active_min_value', istrue:true, width: '160px', formatter: formatPower},
          { label: '最小有功功率时间', align: 'center', prop: 'pow_active_min_time', formatter: formatTime, width: '200px', istrue:true},

          { label: '平均无功功率(kW)', align: 'center', prop: 'pow_reactive_avg_value', istrue:false, width: '160px', formatter: formatPower},
          { label: '最大无功功率(kW)', align: 'center', prop: 'pow_reactive_max_value', istrue:false, width: '160px', formatter: formatPower},
          { label: '最大无功功率时间', align: 'center', prop: 'pow_reactive_max_time', formatter: formatTime, width: '200px', istrue:false},
          { label: '最小无功功率(kW)', align: 'center', prop: 'pow_reactive_min_value', istrue:false, width: '160px', formatter: formatPower},
          { label: '最小无功功率时间', align: 'center', prop: 'pow_reactive_min_time', formatter: formatTime, width: '200px', istrue:false},

          { label: '平均视在功率(kVA)', align: 'center', prop: 'pow_apparent_avg_value', istrue:false, width: '160px, formatter: formatPower'},
          { label: '最大视在功率(kVA)', align: 'center', prop: 'pow_apparent_max_value', istrue:false, width: '160px', formatter: formatPower},
          { label: '最大视在功率时间', align: 'center', prop: 'pow_apparent_max_time', formatter: formatTime, width: '200px', istrue:false},
          { label: '最小视在功率(kVA)', align: 'center', prop: 'pow_apparent_min_value', istrue:false, width: '160px', formatter: formatPower},
          { label: '最小视在功率时间', align: 'center', prop: 'pow_apparent_min_time', formatter: formatTime, width: '200px', istrue:false},

          { label: '记录时间', align: 'center', prop: 'create_time', formatter: formatTime, width: '200px', istrue:true},
          { label: '操作', align: 'center', slot: 'actions', istrue:true, width: '160px'},
        ] as any;
        queryParams.pageNo = 1;
        getList();
      }
    }
    
    if (newType == 'line'){
      if ( newGranularity == 'realtime'){
        // 配置筛选列
        defaultOptionsCol.value = [["vol_value"], ["cur_value"], ["pow_active"], ["pow_reactive"], ["pow_apparent"], 
                                  ["power_factor"], ["load_rate"], ["cur_thd"]];
        optionsCol.value = [
          { value: "pow_active", label: '有功功率'},
          { value: "pow_reactive", label: '无功功率'},
          { value: "pow_apparent", label: '视在功率'},
          { value: "power_factor", label: '功率因素'},
          { value: "vol_value", label: '电压'},
          { value: "cur_value", label: '电流'},
          { value: "load_rate", label: '负载率'},
          { value: "cur_thd", label: '电流谐波含量'},
        ];
        originalArray.value =["vol_value", "cur_value", "pow_active", "pow_reactive", "pow_apparent", "power_factor", "load_rate", "cur_thd"];
        // 配置表格列
        tableColumns.value = [
          { label: '所在位置', align: 'center', prop: 'location', istrue:true, width: '250px'},
          { label: '设备地址', align: 'center', prop: 'dev_key' , istrue:true, width: '250px'},
        { label: '设备名称', align: 'center', prop: 'bus_name', istrue:true, width: '300%'},

          { label: '相', align: 'center', prop: 'line_id', istrue:true, formatter: formatLineId, width: '140px'},
          { label: '有功功率(kW)', align: 'center', prop: 'pow_active', istrue:true, formatter: formatPower, width: '140px'},
          { label: '无功功率(kVar)', align: 'center', prop: 'pow_reactive', istrue:true, formatter: formatPower, width: '140px'},
          { label: '视在功率(kVA)', align: 'center', prop: 'pow_apparent', istrue:true, formatter: formatPower, width: '140px'},
          { label: '功率因素', align: 'center', prop: 'power_factor', istrue:true, formatter: formatPowerFactor, width: '140px'},
          { label: '电压(V)', align: 'center', prop: 'vol_value', istrue:true, formatter: formatVoltage, width: '140px'},
          { label: '电流(A)', align: 'center', prop: 'cur_value', istrue:true, formatter: formatCurrent, width: '140px'},
          { label: '负载率', align: 'center', prop: 'load_rate', istrue:true, width: '140px'},
          { label: '电流谐波含量', align: 'center', prop: 'cur_thd', istrue:true, width: '140px'},
          { label: '时间', align: 'center', prop: 'create_time', formatter: formatTime, istrue:true, width: '220px'},
          { label: '操作', align: 'center', slot: 'actions', istrue:true, width: '160px'},
        ] as any;
        queryParams.pageNo = 1;
        getList();
      }else{
        // 配置筛选列
        defaultOptionsCol.value = [
          ["pow_active", "pow_active_avg_value"], ["pow_active", "pow_active_max"], ["pow_active", "pow_active_min"],
        ];
        optionsCol.value = [
          { value: "pow_active", label: '有功功率', children: [
              { value: "pow_active_avg_value", label: '平均有功功率' },
              { value: "pow_active_max", label: '最大有功功率' },
              { value: "pow_active_min", label: '最小有功功率' },
            ]
          },
          { value: "pow_reactive", label: '无功功率', children: [
              { value: "pow_reactive_avg_value", label: '平均无功功率'},
              { value: "pow_reactive_max", label: '最大无功功率' },
              { value: "pow_reactive_min", label: '最小无功功率' },
            ]
          },
          { value: "pow_apparent", label: '视在功率', children: [
              { value: "pow_apparent_avg_value", label: '平均视在功率' },
              { value: "pow_apparent_max", label: '最大视在功率' },
              { value: "pow_apparent_min", label: '最小视在功率' },
            ]
          },
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
          { value: "cur_thd", label: '电流谐波含量', children: [
              { value: "cur_thd_avg", label: '平均电流谐波含量' },
              { value: "cur_thd_max", label: '最大电流谐波含量' },
              { value: "cur_thd_min", label: '最小电流谐波含量' },
            ]
          },
        ] as any;
        originalArray.value =["vol_avg_value", "vol_max", "vol_min",
                            "cur_avg_value", "cur_max", "cur_min",
                            "vol_line_avg_value", "vol_line_max", "vol_line_min",
                            "pow_active_avg_value", "pow_active_max", "pow_active_min", 
                            "pow_reactive_avg_value", "pow_reactive_max", "pow_reactive_min", 
                            "pow_apparent_avg_value", "pow_apparent_max", "pow_apparent_min",
                            "cur_thd_avg", "cur_thd_max", "cur_thd_min"],
        // 配置表格列
        tableColumns.value = [
          { label: '所在位置', align: 'center', prop: 'location', istrue:true, width: '250px'},
          { label: '设备地址', align: 'center', prop: 'dev_key' , istrue:true, width: '250px'},
        { label: '设备名称', align: 'center', prop: 'bus_name', istrue:true, width: '300%'},

          { label: '相', align: 'center', prop: 'line_id', istrue:true, width: '100px', formatter: formatLineId},

          { label: '平均有功功率(kW)', align: 'center', prop: 'pow_active_avg_value', istrue:true, width: '180px', formatter: formatPower},
          { label: '最大有功功率(kW)', align: 'center', prop: 'pow_active_max_value', istrue:true, width: '180px', formatter: formatPower},
          { label: '最大有功功率时间', align: 'center', prop: 'pow_active_max_time', formatter: formatTime, width: '230px', istrue:true},
          { label: '最小有功功率(kW)', align: 'center', prop: 'pow_active_min_value', istrue:true, width: '180px', formatter: formatPower},
          { label: '最小有功功率时间', align: 'center', prop: 'pow_active_min_time', formatter: formatTime, width: '230px', istrue:true},

          { label: '平均无功功率(kW)', align: 'center', prop: 'pow_reactive_avg_value', istrue:false, width: '180px', formatter: formatPower},
          { label: '最大无功功率(kW)', align: 'center', prop: 'pow_reactive_max_value', istrue:false, width: '180px', formatter: formatPower},
          { label: '最大无功功率时间', align: 'center', prop: 'pow_reactive_max_time', formatter: formatTime, width: '230px', istrue:false},
          { label: '最小无功功率(kW)', align: 'center', prop: 'pow_reactive_min_value', istrue:false, width: '180px', formatter: formatPower},
          { label: '最小无功功率时间', align: 'center', prop: 'pow_reactive_min_time', formatter: formatTime, width: '230px', istrue:false},

          { label: '平均视在功率(kVA)', align: 'center', prop: 'pow_apparent_avg_value', istrue:false, width: '180px', formatter: formatPower},
          { label: '最大视在功率(kVA)', align: 'center', prop: 'pow_apparent_max_value', istrue:false, width: '180px', formatter: formatPower},
          { label: '最大视在功率时间', align: 'center', prop: 'pow_apparent_max_time', formatter: formatTime, width: '230px', istrue:false},
          { label: '最小视在功率(kVA)', align: 'center', prop: 'pow_apparent_min_value', istrue:false, width: '180px', formatter: formatPower},
          { label: '最小视在功率时间', align: 'center', prop: 'pow_apparent_min_time', formatter: formatTime, width: '230px', istrue:false},
  
          { label: '平均电压(V)', align: 'center', prop: 'vol_avg_value', istrue:false, width: '140px', formatter: formatVoltage},
          { label: '最大电压(V)', align: 'center', prop: 'vol_max_value', istrue:false, width: '120px', formatter: formatVoltage},
          { label: '最大电压时间', align: 'center', prop: 'vol_max_time', formatter: formatTime, width: '230px', istrue:false},
          { label: '最小电压(V)', align: 'center', prop: 'vol_min_value', istrue:false, width: '140px', formatter: formatVoltage},
          { label: '最小电压时间', align: 'center', prop: 'vol_min_time', formatter: formatTime, width: '230px', istrue:false},

          { label: '平均电流(A)', align: 'center', prop: 'cur_avg_value', istrue:false, width: '140px', formatter: formatCurrent},
          { label: '最大电流(A)', align: 'center', prop: 'cur_max_value', istrue:false, width: '140px', formatter: formatCurrent},
          { label: '最大电流时间', align: 'center', prop: 'cur_max_time', formatter: formatTime, width: '230px', istrue:false},
          { label: '最小电流(A)', align: 'center', prop: 'cur_min_value', istrue:false, width: '140px', formatter: formatCurrent},
          { label: '最小电流时间', align: 'center', prop: 'cur_min_time', formatter: formatTime, width: '230px', istrue:false},

          { label: '平均电流谐波含量', align: 'center', prop: 'cur_thd_avg_value', istrue:false, width: '140px'},
          { label: '最大电流谐波含量', align: 'center', prop: 'cur_thd_max_value', istrue:false, width: '120px'},
          { label: '最大电流谐波含量时间', align: 'center', prop: 'cur_thd_max_time', formatter: formatTime, width: '230px', istrue:false},
          { label: '最小电流谐波含量', align: 'center', prop: 'cur_thd_min_value', istrue:false, width: '140px'},
          { label: '最小电流谐波含量时间', align: 'center', prop: 'cur_thd_min_time', formatter: formatTime, width: '230px', istrue:false},

          { label: '记录时间', align: 'center', prop: 'create_time', formatter: formatTime, width: '230px', istrue:true},
          { label: '操作', align: 'center', slot: 'actions', istrue:true, width: '160px'},
        ] as any;
        queryParams.pageNo = 1;
        getList();
      }
    }

    if (newType == 'loop'){
      if ( newGranularity == 'realtime'){
        // 配置筛选列
        defaultOptionsCol.value = [["vol_value"], ["cur_value"], ["pow_active"], ["pow_reactive"], ["pow_apparent"], ["power_factor"]];
        optionsCol.value = [
          { value: "pow_active", label: '有功功率'},
          { value: "pow_reactive", label: '无功功率'},
          { value: "pow_apparent", label: '视在功率'},
          { value: "power_factor", label: '功率因素'},
          { value: "vol_value", label: '电压'},
          { value: "cur_value", label: '电流'},
        ];
        originalArray.value =["vol_value", "cur_value", "pow_active", "pow_reactive", "pow_apparent", "power_factor"];
        // 配置表格列
        tableColumns.value = [
          { label: '所在位置', align: 'center', prop: 'location', istrue:true, width: '250px'},
          { label: '设备地址', align: 'center', prop: 'dev_key' , istrue:true, width: '250px'},
        { label: '设备名称', align: 'center', prop: 'bus_name', istrue:true, width: '300%'},

          { label: '回路', align: 'center', prop: 'loop_id', istrue:true, formatter: formatLoopId, width: '140px'},
          { label: '有功功率(kW)', align: 'center', prop: 'pow_active', istrue:true, formatter: formatPower, width: '140px'},
          { label: '无功功率(kVar)', align: 'center', prop: 'pow_reactive', istrue:true, formatter: formatPower, width: '140px'},
          { label: '视在功率(kVA)', align: 'center', prop: 'pow_apparent', istrue:true, formatter: formatPower, width: '140px'},
          { label: '功率因素', align: 'center', prop: 'power_factor', istrue:true, formatter: formatPowerFactor, width: '140px'},
          { label: '电压(V)', align: 'center', prop: 'vol_value', istrue:true, formatter: formatVoltage, width: '140px'},
          { label: '电流(A)', align: 'center', prop: 'cur_value', istrue:true, formatter: formatCurrent, width: '140px'},
          { label: '时间', align: 'center', prop: 'create_time', formatter: formatTime, istrue:true, width: '220px'},
          { label: '操作', align: 'center', slot: 'actions', istrue:true, width: '160px'},
        ] as any;
        queryParams.pageNo = 1;
        getList();
      }else{
        // 配置筛选列
        defaultOptionsCol.value = [
          ["pow_active", "pow_active_avg_value"], ["pow_active", "pow_active_max"], ["pow_active", "pow_active_min"],
        ];
        optionsCol.value = [
          { value: "pow_active", label: '有功功率', children: [
              { value: "pow_active_avg_value", label: '平均有功功率' },
              { value: "pow_active_max", label: '最大有功功率' },
              { value: "pow_active_min", label: '最小有功功率' },
            ]
          },
          { value: "pow_reactive", label: '无功功率', children: [
              { value: "pow_reactive_avg_value", label: '平均无功功率'},
              { value: "pow_reactive_max", label: '最大无功功率' },
              { value: "pow_reactive_min", label: '最小无功功率' },
            ]
          },
          { value: "pow_apparent", label: '视在功率', children: [
              { value: "pow_apparent_avg_value", label: '平均视在功率' },
              { value: "pow_apparent_max", label: '最大视在功率' },
              { value: "pow_apparent_min", label: '最小视在功率' },
            ]
          },
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
        ] as any;
        originalArray.value =[
          "vol_avg_value", "vol_max", "vol_min",
          "cur_avg_value", "cur_max", "cur_min",
          "pow_active_avg_value", "pow_active_max", "pow_active_min",
          "pow_reactive_avg_value", "pow_reactive_max", "pow_reactive_min",
          "pow_apparent_avg_value", "pow_apparent_max", "pow_apparent_min",
        ],
        // 配置表格列
        tableColumns.value = [
          { label: '所在位置', align: 'center', prop: 'location', istrue:true, width: '250px'},
          { label: '设备地址', align: 'center', prop: 'dev_key' , istrue:true, width: '250px'},
        { label: '设备名称', align: 'center', prop: 'bus_name', istrue:true, width: '300%'},

          { label: '回路', align: 'center', prop: 'loop_id', istrue:true, width: '100px', formatter: formatLoopId},

          { label: '平均有功功率(kW)', align: 'center', prop: 'pow_active_avg_value', istrue:true, width: '180px', formatter: formatPower},
          { label: '最大有功功率(kW)', align: 'center', prop: 'pow_active_max_value', istrue:true, width: '180px', formatter: formatPower},
          { label: '最大有功功率时间', align: 'center', prop: 'pow_active_max_time', formatter: formatTime, width: '230px', istrue:true},
          { label: '最小有功功率(kW)', align: 'center', prop: 'pow_active_min_value', istrue:true, width: '180px', formatter: formatPower},
          { label: '最小有功功率时间', align: 'center', prop: 'pow_active_min_time', formatter: formatTime, width: '230px', istrue:true},

          { label: '平均无功功率(kW)', align: 'center', prop: 'pow_reactive_avg_value', istrue:false, width: '180px', formatter: formatPower},
          { label: '最大无功功率(kW)', align: 'center', prop: 'pow_reactive_max_value', istrue:false, width: '180px', formatter: formatPower},
          { label: '最大无功功率时间', align: 'center', prop: 'pow_reactive_max_time', formatter: formatTime, width: '230px', istrue:false},
          { label: '最小无功功率(kW)', align: 'center', prop: 'pow_reactive_min_value', istrue:false, width: '180px', formatter: formatPower},
          { label: '最小无功功率时间', align: 'center', prop: 'pow_reactive_min_time', formatter: formatTime, width: '230px', istrue:false},

          { label: '平均视在功率(kVA)', align: 'center', prop: 'pow_apparent_avg_value', istrue:false, width: '180px', formatter: formatPower},
          { label: '最大视在功率(kVA)', align: 'center', prop: 'pow_apparent_max_value', istrue:false, width: '180px', formatter: formatPower},
          { label: '最大视在功率时间', align: 'center', prop: 'pow_apparent_max_time', formatter: formatTime, width: '230px', istrue:false},
          { label: '最小视在功率(kVA)', align: 'center', prop: 'pow_apparent_min_value', istrue:false, width: '180px', formatter: formatPower},
          { label: '最小视在功率时间', align: 'center', prop: 'pow_apparent_min_time', formatter: formatTime, width: '230px', istrue:false},
  
          { label: '平均电压(V)', align: 'center', prop: 'vol_avg_value', istrue:false, width: '140px', formatter: formatVoltage},
          { label: '最大电压(V)', align: 'center', prop: 'vol_max_value', istrue:false, width: '120px', formatter: formatVoltage},
          { label: '最大电压时间', align: 'center', prop: 'vol_max_time', formatter: formatTime, width: '230px', istrue:false},
          { label: '最小电压(V)', align: 'center', prop: 'vol_min_value', istrue:false, width: '140px', formatter: formatVoltage},
          { label: '最小电压时间', align: 'center', prop: 'vol_min_time', formatter: formatTime, width: '230px', istrue:false},

          { label: '平均电流(A)', align: 'center', prop: 'cur_avg_value', istrue:false, width: '140px', formatter: formatCurrent},
          { label: '最大电流(A)', align: 'center', prop: 'cur_max_value', istrue:false, width: '140px', formatter: formatCurrent},
          { label: '最大电流时间', align: 'center', prop: 'cur_max_time', formatter: formatTime, width: '230px', istrue:false},
          { label: '最小电流(A)', align: 'center', prop: 'cur_min_value', istrue:false, width: '140px', formatter: formatCurrent},
          { label: '最小电流时间', align: 'center', prop: 'cur_min_time', formatter: formatTime, width: '230px', istrue:false},

          { label: '记录时间', align: 'center', prop: 'create_time', formatter: formatTime, width: '230px', istrue:true},
          { label: '操作', align: 'center', slot: 'actions', istrue:true, width: '160px'},
        ] as any;
        queryParams.pageNo = 1;
        getList();
      }
    }

    if (newType == 'outlet'){
      if ( newGranularity == 'realtime'){
        // 配置筛选列
        defaultOptionsCol.value = [["pow_active"], ["pow_reactive"], ["pow_apparent"], ["power_factor"]];
        optionsCol.value = [
          { value: "pow_active", label: '有功功率'},
          { value: "pow_reactive", label: '无功功率'},
          { value: "pow_apparent", label: '视在功率'},
          { value: "power_factor", label: '功率因素'},
        ];
        originalArray.value =[ "pow_active", "pow_reactive", "pow_apparent", "power_factor"];
        // 配置表格列
        tableColumns.value = [
          { label: '所在位置', align: 'center', prop: 'location', istrue:true, width: '250px'},
          { label: '设备地址', align: 'center', prop: 'dev_key' , istrue:true, width: '250px'},
        { label: '设备名称', align: 'center', prop: 'bus_name', istrue:true, width: '300%'},

          { label: '输出位', align: 'center', prop: 'outlet_id', istrue:true, width: '140px'},
          { label: '有功功率(kW)', align: 'center', prop: 'pow_active', istrue:true, formatter: formatPower, width: '140px'},
          { label: '无功功率(kVar)', align: 'center', prop: 'pow_reactive', istrue:true, formatter: formatPower, width: '140px'},
          { label: '视在功率(kVA)', align: 'center', prop: 'pow_apparent', istrue:true, formatter: formatPower, width: '140px'},
          { label: '功率因素', align: 'center', prop: 'power_factor', istrue:true, formatter: formatPowerFactor, width: '140px'},
          { label: '时间', align: 'center', prop: 'create_time', formatter: formatTime, istrue:true, width: '220px'},
          { label: '操作', align: 'center', slot: 'actions', istrue:true, width: '160px'},
        ] as any;
        queryParams.pageNo = 1;
        getList();
      }else{
        // 配置筛选列
        defaultOptionsCol.value = [
          ["pow_active", "pow_active_avg_value"], ["pow_active", "pow_active_max"], ["pow_active", "pow_active_min"],
        ];
        optionsCol.value = [
          { value: "pow_active", label: '有功功率', children: [
              { value: "pow_active_avg_value", label: '平均有功功率' },
              { value: "pow_active_max", label: '最大有功功率' },
              { value: "pow_active_min", label: '最小有功功率' },
            ]
          },
          { value: "pow_reactive", label: '无功功率', children: [
              { value: "pow_reactive_avg_value", label: '平均无功功率'},
              { value: "pow_reactive_max", label: '最大无功功率' },
              { value: "pow_reactive_min", label: '最小无功功率' },
            ]
          },
          { value: "pow_apparent", label: '视在功率', children: [
              { value: "pow_apparent_avg_value", label: '平均视在功率' },
              { value: "pow_apparent_max", label: '最大视在功率' },
              { value: "pow_apparent_min", label: '最小视在功率' },
            ]
          },
        ] as any;
        originalArray.value =[
          "pow_active_avg_value", "pow_active_max", "pow_active_min",
          "pow_reactive_avg_value", "pow_reactive_max", "pow_reactive_min",
          "pow_apparent_avg_value", "pow_apparent_max", "pow_apparent_min",
        ],
        // 配置表格列
        tableColumns.value = [
          { label: '所在位置', align: 'center', prop: 'location', istrue:true, width: '250px'},
          { label: '设备地址', align: 'center', prop: 'dev_key' , istrue:true, width: '250px'},
        { label: '设备名称', align: 'center', prop: 'bus_name', istrue:true, width: '300%'},
          
          { label: '输出位', align: 'center', prop: 'outlet_id', istrue:true, width: '100px'},

          { label: '平均有功功率(kW)', align: 'center', prop: 'pow_active_avg_value', istrue:true, width: '180px', formatter: formatPower},
          { label: '最大有功功率(kW)', align: 'center', prop: 'pow_active_max_value', istrue:true, width: '180px', formatter: formatPower},
          { label: '最大有功功率时间', align: 'center', prop: 'pow_active_max_time', formatter: formatTime, width: '230px', istrue:true},
          { label: '最小有功功率(kW)', align: 'center', prop: 'pow_active_min_value', istrue:true, width: '180px', formatter: formatPower},
          { label: '最小有功功率时间', align: 'center', prop: 'pow_active_min_time', formatter: formatTime, width: '230px', istrue:true},

          { label: '平均无功功率(kW)', align: 'center', prop: 'pow_reactive_avg_value', istrue:false, width: '180px', formatter: formatPower},
          { label: '最大无功功率(kW)', align: 'center', prop: 'pow_reactive_max_value', istrue:false, width: '180px', formatter: formatPower},
          { label: '最大无功功率时间', align: 'center', prop: 'pow_reactive_max_time', formatter: formatTime, width: '230px', istrue:false},
          { label: '最小无功功率(kW)', align: 'center', prop: 'pow_reactive_min_value', istrue:false, width: '180px', formatter: formatPower},
          { label: '最小无功功率时间', align: 'center', prop: 'pow_reactive_min_time', formatter: formatTime, width: '230px', istrue:false},

          { label: '平均视在功率(kVA)', align: 'center', prop: 'pow_apparent_avg_value', istrue:false, width: '180px', formatter: formatPower},
          { label: '最大视在功率(kVA)', align: 'center', prop: 'pow_apparent_max_value', istrue:false, width: '180px', formatter: formatPower},
          { label: '最大视在功率时间', align: 'center', prop: 'pow_apparent_max_time', formatter: formatTime, width: '230px', istrue:false},
          { label: '最小视在功率(kVA)', align: 'center', prop: 'pow_apparent_min_value', istrue:false, width: '180px', formatter: formatPower},
          { label: '最小视在功率时间', align: 'center', prop: 'pow_apparent_min_time', formatter: formatTime, width: '230px', istrue:false},
  
          { label: '记录时间', align: 'center', prop: 'create_time', formatter: formatTime, width: '230px', istrue:true},
          { label: '操作', align: 'center', slot: 'actions', istrue:true, width: '160px'},
        ] as any;
        queryParams.pageNo = 1;
        getList();
      }
    }
  });

const tableColumns = ref([
  { label: '所在位置', align: 'center', prop: 'location' , width: '250px', istrue:true},
  { label: '设备地址', align: 'center', prop: 'dev_key', width: '250px', istrue:true},
{ label: '设备名称', align: 'center', prop: 'bus_name', istrue:true, width: '300%'},

  { label: '总有功功率(kW)', align: 'center', prop: 'pow_active', istrue:true, width: '160px', formatter: formatPower},
  { label: '总无功功率(kVar)', align: 'center', prop: 'pow_reactive', istrue:true, width: '160px', formatter: formatPower},
  { label: '总视在功率(kVA)', align: 'center', prop: 'pow_apparent', istrue:true, width: '160px', formatter: formatPower},
  { label: '功率因素', align: 'center', prop: 'power_factor' , istrue:true, width: '140px', formatter: formatPowerFactor},
  { label: '时间', align: 'center', prop: 'create_time', formatter: formatTime, width: '200px', istrue:true},
  { label: '操作', align: 'center', slot: 'actions' , istrue:true, width: '160px'},
]);

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await HistoryDataApi.getBoxHistoryDataPage(queryParams)
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
function formatVoltage(_row: any, _column: any, cellValue: number): string {
  return Number(cellValue).toFixed(1);
}

// 格式化电流(A)列数据，保留两位小数
function formatCurrent(_row: any, _column: any, cellValue: number): string {
  return Number(cellValue).toFixed(2);
}

// 格式化功率列数据，保留三位小数
function formatPower(_row: any, _column: any, cellValue: number): string {
  return Number(cellValue).toFixed(3);
}

// 格式化功率因素列数据，保留两位小数
function formatPowerFactor(_row: any, _column: any, cellValue: number): string {
  return Number(cellValue).toFixed(2);
}

// 格式化相id
function formatLineId(_row: any, _column: any, cellValue: number): string {
   return 'L'+cellValue;
}

// 格式化回路id
function formatLoopId(_row: any, _column: any, cellValue: number): string {
   return 'C'+cellValue;
}

// 格式化日期
function formatTime(_row: any, _column: any, cellValue: number): string {
  if (!cellValue) {
    return ''
  }
  return dayjs(cellValue).format('YYYY-MM-DD HH:mm')
}

// 禁选未来的日期
const disabledDate = (date) => {
  const today = new Date();
  return date > today;
}

// 导航栏选择后触发
const handleCheck = async (node) => {
  let arr = [] as any
  node.forEach(item => { 
    if(item.type == 7){
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
  const res = await IndexApi.getBoxMenu()
  // let arr = [] as any
  // for (let i=0; i<res.length;i++){
  // var temp = await CabinetApi.getRoomPDUList({id : res[i].id})
  // arr = arr.concat(temp);
  // }
  navList.value = res
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1;
  getList(); 
}

//详情操作 跳转电力分析
const toDetails = (boxId: number, location?: string) => {
  push('/bus/record/historyLine/box?boxId='+boxId+'&location='+location);
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
    const data = await HistoryDataApi.exportBoxHistoryData(queryParams, axiosConfig)
    await download.excel(data, '母线插接箱历史数据.xlsx')
  } catch (error) {
    // 处理异常
    console.error('导出失败：', error)
  } finally {
    exportLoading.value = false
  }
}

// 获取导航的数据显示
const getBoxNavNewData = async() => {
  const res = await HistoryDataApi.getBoxNavNewData(queryParams.granularity)
  navTotalData.value = res.total
  navLineData.value = res.line
  navLoopData.value = res.loop
  navOutletData.value = res.outlet
}

// 参数类型选择框初始化，相固定3相
const getTypeMaxValue = async () => {
    const data = await HistoryDataApi.getTypeMaxValue(queryParams.devkeys)
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

/** 初始化 **/
onMounted( () => {
  getNavList()
  getBoxNavNewData()
  getTypeMaxValue();
  getList();
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
.nav_header {
    display: flex;
    flex-direction: column;
    align-items: center;
    font-size: 14px; 
    font-weight: bold;
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
</style>