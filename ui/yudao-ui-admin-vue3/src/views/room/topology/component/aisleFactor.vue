<template>
  <el-dialog v-model="detailVis" title="柜列功率因素"  width="70vw" >
    <el-row style="position: absolute;top: 20px; left: 20%; vertical-align: middle;">
      <span style="vertical-align:middle; position: relative;top: 5px; right: 120px;">机房：{{ location?.split("-")[0] }}<span  v-for="n in Array(10)" :key="n">&nbsp;</span>柜列：{{location?.split("-")[1]}}</span>
      <span style="position: relative;left: 220px;">
        <div style="vertical-align:middle;display: inline-block;">
          日期:
          <el-date-picker
            :clearable="false"
            v-model="detailQueryParams.oldTime"
            value-format="YYYY-MM-DD HH:mm:ss"
            type="date"
            :disabled-date="disabledDate"
            @change="handleDayPick"
            class="!w-160px"
          />
        </div>
        <el-button 
          @click="subtractOneDay();handleDayPick()" 
          :type=" 'primary'"
        >
          &lt;前一日
        </el-button>
        <el-button 
          @click="addtractOneDay();handleDayPick()" 
          :type=" 'primary'"
        >
          &gt;后一日
        </el-button>
        <el-button 
          @click="switchChartOrTable = 0" 
          :type="switchChartOrTable == 0 ?  'primary' : ``"
        >
          图表
        </el-button>
        <el-button 
          @click="switchChartOrTable = 1" 
          :type=" switchChartOrTable == 1 ?  'primary' : ``"
        >
          数据
        </el-button>
        <el-button 
        @click="exportExcel()"
        :loading="exportLoading"
        v-show="switchChartOrTable == 1">
          导出
        </el-button>
      </span>
    </el-row>
    <br/>
    <PFDetail v-show="switchChartOrTable == 0"  width="68vw" height="58vh"  :list="pfESList"   />
    <el-table v-show="switchChartOrTable == 1" :data="pfTableList" :stripe="true" :show-overflow-tooltip="true" style="width: 100%;height:58vh;overflow-y:auto;" >
      <el-table-column label="时间" align="center" prop="time" />
      <el-table-column label="总功率因素" align="center" prop="factorTotalAvgValue" />
      <el-table-column label="A路功率因素" align="center" prop="factorAAvgValue" />
      <el-table-column label="B路功率因素" align="center" prop="factorBAvgValue" />
    </el-table>
  </el-dialog>
</template>

<script lang="ts" setup>
import download from '@/utils/download'
import { IndexApi } from '@/api/aisle/aisleindex'
import PFDetail from '@/views/aisle/powerfactor/component/PFDetail.vue'

const location = ref() as any;

const switchChartOrTable = ref(0)
const detailVis = ref(false);
const pfESList = ref({}) as any
const pfTableList = ref([]) as any

const message = useMessage() // 消息弹窗
const exportLoading = ref(false) // 导出的加载中

const detailQueryParams = reactive({
  devKey: undefined,
  createTime: [],
  cascadeNum: undefined,
  serverRoomData:undefined,
  status:[],
  cabinetIds : [],
  timeType : 0,
})as any

/** 打开弹窗 */
const open = async (data) => {
  openPFDetail(data)
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

const openPFDetail = async (row) =>{
  detailQueryParams.id = row.id;
  detailQueryParams.oldTime = getFullTimeByDate(new Date(new Date().getFullYear(),new Date().getMonth(),new Date().getDate(),0,0,0));
  location.value = row.location ? row.location  : row.devKey ;
  await getDetail();
  detailVis.value = true;
}

/** 查询列表 */
const getDetail = async () => {
  const data = await IndexApi.getAislePFDetail(detailQueryParams);
  pfESList.value = data?.chart;
  pfESList.value?.factorTotalAvgValue?.forEach((obj) => {
    obj = obj?.toFixed(2);
  });
  pfESList.value?.factorAAvgValue?.forEach((obj) => {
    obj = obj?.toFixed(2);
  });
  pfESList.value?.factorBAvgValue?.forEach((obj) => {
    obj = obj?.toFixed(2);
  });

  pfTableList.value = data?.table;
  pfTableList.value?.forEach((obj) => {
    obj.factorTotalAvgValue = obj?.factorTotalAvgValue?.toFixed(2);
    obj.factorAAvgValue = obj?.factorAAvgValue?.toFixed(2);
    obj.factorBAvgValue = obj?.factorBAvgValue?.toFixed(2);
  });
}

//导出excel
async function exportExcel(){
  try{
    await message.exportConfirm()
    exportLoading.value = true
    const data=await IndexApi.getAislePFDetailExcel({
      ...detailQueryParams
    })
    download.excel(data, '功率因素详情.xls');
  }
  catch(error){
    console.error('导出失败：', error);
  }
  finally{
    exportLoading.value = false
  } 
}

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

const handleDayPick = async () => {

  if(detailQueryParams?.oldTime ){
    await getDetail();
    
  } 
}

const subtractOneDay = () => {
  var date = new Date(detailQueryParams.oldTime + "Z"); // 添加 "Z" 表示 UTC 时间

  date.setDate(date.getDate() - 1); // 减去一天

  detailQueryParams.oldTime = date.toISOString().slice(0, 19).replace("T", " "); // 转换为新的日期字符串
};

const addtractOneDay = () => {
  var date = new Date(detailQueryParams.oldTime + "Z"); // 添加 "Z" 表示 UTC 时间

  date.setDate(date.getDate() + 1); // 减去一天

  detailQueryParams.oldTime = date.toISOString().slice(0, 19).replace("T", " "); // 转换为新的日期字符串
};

</script>

<style lang="scss" scoped>
.tagInDialog{
  position: absolute;
  left: 30%;
  top: 22px;
}
</style>