<template>
  <el-dialog v-model="detailVis" width="70vw" height="58vh" >
    <div style="display: flex; align-items: center; justify-content: space-between; width: 100%;margin-top:-50px;">
      <div>
        <span style="font-weight:bold;font-size:20px;margin-right:10px;">{{queryParams.lineType == 0 ? `电流详情`: `功率详情`}}</span>
        <span style="margin-right:10px;">结果所在位置：{{ location }}</span>
        <span>时间段: {{ queryParams.oldTime }}&nbsp;&nbsp;到&nbsp;&nbsp;{{ queryParams.newTime }}</span>
      </div>
      <div v-if="visMode != 2" style="display: flex; gap: 10px;margin-right:30px;"> <!-- 子div用于包含按钮，并设置按钮之间的间距 -->
        <el-button
          @click="switchChartOrTable = 0"
          :type="switchChartOrTable == 0 ? 'primary' : ''"
        >
          图表
        </el-button>
        <el-button
          @click="switchChartOrTable = 1"
          :type="switchChartOrTable == 1 ? 'primary' : ''"
        >
          数据
        </el-button>
        <el-button type="success" plain @click="handleExport" :loading="exportLoading">
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
      </div>
    </div>
    <div style="margin-top:3vh;">
      <RequirementLine v-if="switchChartOrTable == 0" width="68vw" height="58vh" :list="requirementLine"  />
      <el-table style="height:550px;ovrflow:hidden;overflow-y:auto;" v-if="switchChartOrTable == 1" :data="pfTableList" :stripe="true" :show-overflow-tooltip="true" >
        <el-table-column label="设备识别码" align="center" prop="devKey" />
        <el-table-column label="相" align="center" prop="line" />
        <el-table-column label="最大电流时间" align="center" prop="cur_max_time" />
        <el-table-column label="最大电流" align="center" prop="cur_max_value" />
        <el-table-column label="最大有功功率时间" align="center" prop="pow_active_max_time" />
        <el-table-column label="最大有功功率" align="center" prop="pow_active_max_value" />
      </el-table>
    </div>

  </el-dialog>
</template>

<script lang="ts" setup>
import { IndexApi } from '@/api/bus/boxindex'
import RequirementLine from '@/views/box/requirement/component/RequirementLine.vue'
import download from '@/utils/download'

const exportLoading = ref(false) // 导出的加载中

const getFullTimeByDate = (date) => {
  var year = date.getFullYear();//年
  var month = date.getMonth();//月
  var day = date.getDate();//日
  var hours = date.getHours();//时
  var min = date.getMinutes();//分
  var second = date.getSeconds();//秒
  return year + "-" +
      ((month + 1) > 9 ? (month + 1) : "0" + (month + 1)) + "-" +
      (day > 9 ? day : ("0" + day)) + " " +
      (hours > 9 ? hours : ("0" + hours)) + ":" +
      (min > 9 ? min : ("0" + min)) + ":" +
      (second > 9 ? second : ("0" + second));
}

const queryParams = reactive({
  pageNo: 1,
  pageSize: 24,
  devKey: undefined,
  createTime: [],
  cascadeNum: undefined,
  serverRoomData:undefined,
  status:[],
  cabinetIds:[],
  timeType : 0,
  timeArr:[],
  boxType: 3,
  oldTime : getFullTimeByDate(new Date(new Date().getFullYear(),new Date().getMonth(),1,0,0,0)),
  newTime : getFullTimeByDate(new Date(new Date().getFullYear(),new Date().getMonth() + 1,1,23,59,59)),
}) as any

const switchChartOrTable = ref(0)
const pfTableList = ref([]) as any
const requirementLine = ref([]) as any;
const startTime = ref() as any;
const endTime = ref() as any;
const location = ref();
const roomName = ref();
const detailVis = ref(false);
const visMode = ref(2);

const message = useMessage() // 消息弹窗

/** 打开弹窗 */
const open = async (data) => {
  openDetail(data)
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

const openDetail = async (row) =>{
  queryParams.boxId = row.boxId;
  const tableData = await IndexApi.getBoxLineCurLinePage(queryParams)
  pfTableList.value = tableData?.list;
  pfTableList.value.forEach(item => item.cur_max_value = item.cur_max_value.toFixed(2)+'A')
  pfTableList.value.forEach(item => item.pow_active_max_value = item.pow_active_max_value+'KW')

  const lineData = await IndexApi.getBoxLineCurLine(queryParams);
  requirementLine.value = lineData;
  requirementLine.value.formatter = queryParams.lineType == 0 ? '{value} A' : '{value} kW';
  location.value = row.location != null ? row.location : row.devKey
  startTime.value = lineData.time[0];
  endTime.value = lineData.time[lineData.time.length - 1];
  detailVis.value = true;
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
    const data = await IndexApi.getBoxLineCurLineExcel(queryParams, axiosConfig)
    await download.excel(data, '电流详细.xlsx')
  } catch (error) {
    // 处理异常
    console.error('导出失败：', error)
  } finally {
    exportLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
</style>