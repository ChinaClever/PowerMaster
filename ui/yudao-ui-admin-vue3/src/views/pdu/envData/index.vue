<template>
  <CommonMenu :dataList="navList" @check="handleCheck" navTitle="环境数据">
    <template #NavInfo>
      <div class="nav_header">
        <div class="nav_header_img"><img alt="" src="@/assets/imgs/PDU.jpg" /></div>
        <br/>
        <span>全部传感器最近一小时新增记录</span>
          <br/>
      </div>
      <div class="nav_data">
        <el-statistic title="" :value="navTotalData">
            <template #suffix>条</template>
        </el-statistic>
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
        <!-- <el-form-item label="IP地址" prop="ipAddr">
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
                class="!w-148px"
            />
        </el-form-item> -->

        <el-form-item label="检测点" prop="detect">
          <el-select
            v-model="detect"
            class="!w-130px"
            @change="handleQuery"
            >
            <el-option
              v-for="item in sensorOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="颗粒度" prop="type">
          <el-select
            v-model="queryParams.granularity"
            placeholder="请选择分钟/小时/天"
            class="!w-100px">
            <el-option label="分钟" value="realtime" />
            <el-option label="小时" value="hour" />
            <el-option label="天" value="day" />
          </el-select>
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
            class="!w-200px"
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
            <template #default="{ row }">
              <div v-if="column.slot === 'actions'">
                <el-button link type="primary" @click="toDetails(row.pdu_id, row.location, row.address.address, row.address.channel, row.address.position, row.sensor_id)">详情</el-button>
              </div>
              <div v-else-if="column.slot === 'detect'">
                {{ getCombinedString(row.address?.channel, row.address?.position) }}
              </div>
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
import { EnvDataApi } from '@/api/pdu/envData'
import { CabinetApi } from '@/api/cabinet/info'
import { ElMessage } from 'element-plus'
const { push } = useRouter()
/** pdu历史数据 列表 */
defineOptions({ name: 'PDUEnvHistoryData' })

const navList = ref([]) as any // 左侧导航栏树结构列表
const navTotalData = ref(0)
const message = useMessage() // 消息弹窗
const loading = ref(true) // 列表的加载中
const list = ref<Array<{ }>>([]); // 列表数据
const total = ref(0) // 数据总条数 超过10000条为10000
const realTotel = ref(0) // 数据的真实总条数
const cascadeAddr = ref(0) // 数字类型的级联地址
const detect = ref('all') // 检测点的值 默认全部
const queryParams = reactive({
  pageNo: 1,
  pageSize: 15,
  granularity: 'realtime',
  channel: undefined as number | undefined,
  position: undefined as number | undefined,
  // ipAddr: undefined,
  // cascadeAddr: '0',
  // sensorId: 0,
  timeRange: undefined,
  cabinetIds:[]
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

// 传感器选项
const sensorOptions = ref([
  { value: 'all', label: '全部'},
  { value: "11", label: '前上'},
  { value: "12", label: '前中'},
  { value: "13", label: '前下'},
  { value: "21", label: '后上'},
  { value: "22", label: '后中'},
  { value: "23", label: '后下'}
])

//筛选选项
const props = { multiple: true}
const defaultOptionsCol = ref([["tem"], ["hum"]])
const optionsCol = ref([
  { value: "tem", label: '温度'},
  { value: "hum", label: '湿度'},
  { value: "sensor_id", label: '传感器ID'},
  { value: "location", label: '网络地址'},
])
const originalArray = ref(["tem", "hum", "location", "sensor_id"])

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
        { value: "sensor_id", label: '传感器ID'},
        { value: "location", label: '网络地址'},
      ];
      originalArray.value =["tem", "hum", "location", "sensor_id"];
      // 配置表格列
      tableColumns.value =([
        { label: '位置', align: 'center', prop: 'address.address' , istrue:true},
        { label: '检测点', align: 'center', slot: 'detect' , istrue: true},
        { label: '传感器ID', align: 'center', prop: 'sensor_id' , istrue:false, width: '160px'},
        { label: '温度(℃)', align: 'center', prop: 'tem_value', istrue:true, formatter: formatData},
        { label: '湿度(%RH)', align: 'center', prop: 'hum_value' , istrue:true, formatter: formatData},
        { label: '时间', align: 'center', prop: 'create_time', width: '230px', formatter: formatTime, istrue:true},
        { label: '网络地址', align: 'center', prop: 'location' , istrue:false},
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
            { value: "tem_max", label: '最高温度' },
            { value: "tem_min", label: '最低温度' },
          ]
        },
        { value: "hum_value", label: '湿度', children: [
            { value: "hum_avg_value", label: '平均湿度'},
            { value: "hum_max", label: '最大湿度' },
            { value: "hum_min", label: '最小湿度' },
          ]
        },
        {
          value: "sensor_id", label: '传感器ID'
        },
        {
          value: "location", label: '网络地址'
        }
      ] as any;
      originalArray.value =["tem_avg_value", "tem_max", "tem_min", 
                          "hum_avg_value", "hum_max", "hum_min", "location", "sensor_id"],
      // 配置表格列
      tableColumns.value = [
        { label: '位置', align: 'center', prop: 'address.address', istrue:true, width: '180px'}, 
        { label: '检测点', align: 'center', slot: 'detect' , istrue: true},
        { label: '传感器ID', align: 'center', prop: 'sensor_id' , istrue:false, width: '160px'},
        { label: '平均温度(℃)', align: 'center', prop: 'tem_avg_value', istrue:true, width: '180px', formatter: formatData },
        { label: '最高温度(℃)', align: 'center', prop: 'tem_max_value', istrue:true, width: '180px', formatter: formatData },
        { label: '最高温度时间', align: 'center', prop: 'tem_max_time' , width: '230px', istrue:true},
        { label: '最低温度(℃)', align: 'center', prop: 'tem_min_value', istrue:true, width: '180px', formatter: formatData },
        { label: '最低温度时间', align: 'center', prop: 'tem_min_time' , width: '230px', istrue:true},
        { label: '平均湿度(%RH)', align: 'center', prop: 'hum_avg_value', istrue:false, width: '180px', formatter: formatData},
        { label: '最大湿度(%RH)', align: 'center', prop: 'hum_max_value', istrue:false, width: '180px', formatter: formatData },
        { label: '最大湿度时间', align: 'center', prop: 'hum_max_time' , width: '230px', istrue:false},
        { label: '最小湿度(%RH)', align: 'center', prop: 'hum_min_value', istrue:false, width: '180px', formatter: formatData },
        { label: '最小湿度时间', align: 'center', prop: 'hum_min_time' , width: '230px', istrue:false},
        { label: '记录时间', align: 'center', prop: 'create_time' , width: '230px', istrue:true},
        { label: '网络地址', align: 'center', prop: 'location' , istrue:false, width: '160px'},
        { label: '操作', align: 'center', slot: 'actions', istrue:true, width: '160px'},
      ] as any;
      queryParams.pageNo = 1;
      queryParams.pageSize = 15;
      getList();
    }
  });

const tableColumns = ref([
    { label: '位置', align: 'center', prop: 'address.address' , istrue:true},
    { label: '检测点', align: 'center', slot: 'detect' , istrue: true},
    { label: '传感器ID', align: 'center', prop: 'sensor_id' , istrue:false, width: '160px'},
    { label: '温度(℃)', align: 'center', prop: 'tem_value', istrue:true},
    { label: '湿度(%RH)', align: 'center', prop: 'hum_value' , istrue:true},
    { label: '时间', align: 'center', prop: 'create_time', width: '230px', formatter: formatTime, istrue:true},
    { label: '网络地址', align: 'center', prop: 'location' , istrue:false, width: '120px'},
    { label: '操作', align: 'center', slot: 'actions' , istrue:true, width: '160px'},
]) as any;

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

// 格式化检测位置
function getCombinedString(channel: number, position: number) {
  let channelText = '';
  let positionText = '';

   if (channel === 1) {
    channelText = '前';
  } else if (channel === 2) {
    channelText = '后';
  }
  if (position === 1) {
    positionText = '上';
  } else if (position === 2) {
    positionText = '中';
  } else if (position === 3) {
    positionText = '下';
  }
  return `${channelText}${positionText}`;
}

// 格式化日期
function formatTime(row: any, column: any, cellValue: number): string {
  if (!cellValue) {
    return ''
  }

  return dayjs(cellValue).format('YYYY-MM-DD HH:mm:ss.SSS')
}

// 格式化温湿度列数据，保留一位小数
function formatData(row: any, column: any, cellValue: number): string {
  return cellValue.toFixed(1);
}

// 获取参数类型最大值 例如lineId=6 表示下拉框为L1~L6
// const getSensorIdMaxValue = async () => {
//   const data = await EnvDataApi.getSensorIdMaxValue()
//   const sensorIdMaxValue = data.sensor_id_max_value;
//   const sensorSelectionValue: { value: number; label: string; }[] = [];
//   sensorSelectionValue.push({ value: 0, label: '全部' },)
//   for (let i = 1; i <= sensorIdMaxValue; i++) {
//     sensorSelectionValue.push({ value: i, label: `${i}`});
//   }
//   sensorOptions.value = sensorSelectionValue;
// }

// 导航栏选择后触发
const handleCheck = async (node) => {
   let arr = [] as any
  node.forEach(item => { 
    if(item.type == 3){
      arr.push(item.id);
    }
  });
  queryParams.cabinetIds = arr
  handleQuery()
}

// 接口获取机房导航列表
const getNavList = async() => {
  const res = await CabinetApi.getRoomMenuAll({})
  navList.value = res
}

// 禁选未来的日期
const disabledDate = (date) => {
  const today = new Date();
  return date > today;
}

/** 搜索按钮操作 */
const handleQuery = () => {
    queryParams.pageNo = 1
    if (detect.value != 'all'){
      if (queryParams.cabinetIds?.length != 1){
        ElMessage.error('仅选定一个机柜时可以筛选监测点！')
        detect.value = 'all'
        queryParams.channel = undefined
        queryParams.position = undefined
          return
      }

      queryParams.channel = Number(detect.value.split('')[0])
      queryParams.position = Number(detect.value.split('')[1])
    }else{
      queryParams.channel = undefined
      queryParams.position = undefined
    }
    getList()
}

/** 详情操作*/
const toDetails = (pduId: number, location: string, address: string, channel: number, position: number, sensorId: number) => {
  let detectValue = channel?.toString()+position?.toString()
  push('/pdu/record/envAnalysis?pduId='+pduId+'&location='+location+'&address='+address+'&detectValue='+detectValue+'&sensorId='+sensorId);
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

// 获取导航的数据显示
const getNavOneHourData = async() => {
  const res = await EnvDataApi.getNavOneHourData({})
  navTotalData.value = res.total
}

/** 初始化 **/
onMounted( () => {
  getNavList()
  getNavOneHourData()
  // getSensorIdMaxValue();
  getList()
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
.nav_header {
    display: flex;
    flex-direction: column;
    align-items: center;
    font-size: 13px;
    padding-top: 28px;
  }
  .nav_header_img {
    width: 110px;
    height: 110px;
    border-radius: 50%;
    display: flex;
    justify-content: center;
    align-items: center;
    border: 1px solid #555;
  }

  img {
      width: 75px;
      height: 75px;
  }

.nav_data{
  padding-left: 48px;
}

  .line {
    height: 1px;
    margin-top: 28px;
    margin-bottom: 20px;
    background: linear-gradient(297deg, #fff, #dcdcdc 51%, #fff);
  }
</style>