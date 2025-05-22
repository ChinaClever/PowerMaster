<template>
  <el-dialog v-model="detailVis" title="柜列需量" width="70vw" height="58vh" >
    <div class="tagInDialog">
      <span style="position: relative;left: -270px;">机房：{{ location?.split("-")[0] }}<span v-for="i in Array(5)" :key="i">&nbsp;</span>柜列：{{ location?.split("-")[1] }} <span v-for="i in Array(5)" :key="i">&nbsp;</span> 结果所在时间段: {{ startTime }}&nbsp;&nbsp;到&nbsp;&nbsp;{{ endTime }}</span> 
      <span style="position: relative;right: -220px">
        <el-button type="success" plain @click="handleExport" :loading="exportLoading">
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
      </span>
    </div>
    <div>
      <RequirementLine width="68vw" height="58vh" :list="requirementLine"  />
    </div>
  </el-dialog>
</template>

<script lang="ts" setup>
import { IndexApi } from '@/api/aisle/aisleindex'
import RequirementLine from './RequirementLine.vue'
import { dayjs } from 'element-plus'
import download from '@/utils/download'

const requirementLine = ref([]) as any;

const queryParams = reactive({
  name:null,
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
  oldTime : dayjs(new Date()).subtract(1, 'day').format('YYYY-MM-DD HH:mm:ss'),
  newTime : dayjs(new Date()).format('YYYY-MM-DD HH:mm:ss'),
}) as any
const startTime = ref() as any;
const endTime = ref() as any;
const location = ref() as any;
const detailVis = ref(false)
const exportLoading = ref(false) // 导出的加载中

/** 打开弹窗 */
const open = async (data) => {
  console.log(data)
  openDetail(data)
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

const message = useMessage() // 消息弹窗

const openDetail = async (row) =>{
  queryParams.id = row.id;
  const lineData = await IndexApi.getAisleLineCurLine(queryParams);
  requirementLine.value = lineData;
  requirementLine.value.formatter = queryParams.lineType == 0 ? '{value} A' : '{value} kW';
  location.value = row?.location 
  // requirementLine.value.series.splice(1,0,requirementLine.value.series.pop());
  console.log('requirementLine',requirementLine.value);
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
    exportLoading.value = true
    const data = await IndexApi.getAislePFDetailExcel(queryParams)
    download.excel(data, '需量检测.xlsx')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

</script>

<style lang="scss" scoped>
.tagInDialog{
  position: absolute;
  left: 30%;
  top: 22px;
}
</style>