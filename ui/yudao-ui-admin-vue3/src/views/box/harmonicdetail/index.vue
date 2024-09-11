<template>
  <CommonMenu :showCheckbox="false" @node-click="handleClick" :showSearch="true" :dataList="serverRoomArr" navTitle="PDU报表">
    <template #NavInfo>
      <div >
        <!-- <div class="header">
          <div class="header_img"><img alt="" src="@/assets/imgs/Box.png" /></div>
        </div>
        <div class="line"></div> -->
        <!-- <div class="status">
          <div class="box">
            <div class="top">
              <div class="tag"></div>&lt;15%
            </div>
            <div class="value"><span class="number">{{statusNumber.lessFifteen}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag empty"></div>小电流
            </div>
            <div class="value"><span class="number">{{statusNumber.smallCurrent}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag warn"></div>15%-30%
            </div>
            <div class="value"><span class="number">{{statusNumber.greaterFifteen}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag error"></div>&gt;30
            </div>
            <div class="value"><span class="number">{{statusNumber.greaterThirty}}</span>个</div>
          </div>
        </div> -->
        <div class="line"></div>

      </div>
    </template>
    <template #ActionBar>
      <el-form
        class="-mb-15px"
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        label-width="120px"
      >      
        <!-- <el-form-item label="网络地址" prop="devKey">
          <el-input
            v-model="queryParams.devKey"
            placeholder="请输入网络地址"
            clearable
            @keyup.enter="handleQuery"
            class="!w-240px"
          />
        </el-form-item> -->
        <el-form-item>       
          <el-tag size="large">{{ location }}</el-tag>  
          <el-select
            v-model="queryParams.harmonicType"
            placeholder="请选择"
            style="width: 240px"
          >
            <el-option label="A相电流谐波" :value = 3 />
            <el-option label="B相电流谐波" :value = 4 />
            <el-option label="C相电流谐波" :value = 5 />
          </el-select>

          <el-select
            v-model="queryParams.harmonicArr"
            multiple
            placeholder="Select"
            collapse-tags
            collapse-tags-tooltip
            style="width: 240px"
          >
          <el-option label='全选' value='全选' @click='selectAll' />
            <el-option
              v-for="item in harmonicMultiple"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>

          <el-button 
            @click="subtractOneDay();handleDayPick()" 
          >
            &lt;
          </el-button>
          <el-date-picker
            v-model="queryParams.oldTime"
            value-format="YYYY-MM-DD HH:mm:ss"
            type="date"
            :disabled-date="disabledDate"
            @change="handleDayPick"
            class="!w-160px"
          />
          <el-button 
            @click="addtractOneDay();handleDayPick()" 
          >
            &gt;
          </el-button>
        </el-form-item>

        <el-form-item>
          <el-button @click="handleQuery"  ><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        </el-form-item>
        <!-- <el-text size="large">
          报警次数：{{ pduInfo.alarm }}
        </el-text> -->
      </el-form>
    </template>
    <template #Content>
      <div >
        <div v-if="realTimeVis">
          <HarmonicRealTime  width="70vw" height="58vh" :list="harmonicRealTime"/>
        </div>
        <br/>
        <div v-if="lineVis">
          <HarmonicLine  width="70vw" height="58vh" :list="harmonicLine"/>
        </div>
      </div>
    </template>
  </CommonMenu>


</template>

<script setup lang="ts">
// import download from '@/utils/download'
import { IndexApi } from '@/api/bus/boxindex'
import { ElTree } from 'element-plus'
import HarmonicRealTime from './component/HarmonicRealTime.vue'
import HarmonicLine from './component/HarmonicLine.vue'

/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })

const location = ref(history?.state?.location);
const haveSearch = ref(false);
const switchValue = ref(1);
const harmonicRealTime = ref();
const harmonicLine = ref({ value: { series: [], time: [] } }) as any;
const realTimeVis = ref(false);
const lineVis = ref(false);
const seriesAndTimeArr = ref() as any;
const harmonicMultiple = ref([
  {
  label: "A相电流平均谐波",
  value: 1
  }])

const serverRoomArr =  ref([])

const selectAll = () => {

  if(queryParams.harmonicArr != null && queryParams.harmonicArr?.length < 33 ){
    queryParams.harmonicArr = [1]
  }
  if(queryParams.harmonicArr != null && queryParams.harmonicArr?.length == 33){
    queryParams.harmonicArr = [1]
  }
}

const disabledDate = (date) => {
  // 获取今天的日期
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  // 设置date的时间为0时0分0秒，以便与today进行比较
  date.setHours(0, 0, 0, 0);

  // 如果date在今天之后，则禁用
  if(switchValue.value == 0){
    return date > today;
  }else {
    return date >= today;
  }
  
}

const handleDayPick = async () => {

  if(queryParams?.oldTime ){
    await getDetail();
  } 
}


const subtractOneDay = () => {
  var date = new Date(queryParams.oldTime + "Z"); // 添加 "Z" 表示 UTC 时间

  date.setDate(date.getDate() - 1); // 减去一天

  queryParams.oldTime = date.toISOString().slice(0, 19).replace("T", " "); // 转换为新的日期字符串
};

const addtractOneDay = () => {
  var date = new Date(queryParams.oldTime + "Z"); // 添加 "Z" 表示 UTC 时间

  date.setDate(date.getDate() + 1); // 减去一天

  queryParams.oldTime = date.toISOString().slice(0, 19).replace("T", " "); // 转换为新的日期字符串
};

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


// const activeNames = ref(["1","2","3","4","5"])

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  harmonicType : 3,
  harmonicArr:[1],
  devKey : history?.state?.devKey,
  boxId: history?.state?.boxId,
  outputNumber : 10,
  createTime: undefined,
  timeArr:[],
  oldTime : getFullTimeByDate(new Date(new Date().getFullYear(),new Date().getMonth(),new Date().getDate(),0,0,0)),
  newTime : undefined,
}) as any


const handleClick = async (row) => {
  if(row.type != null  && row.type == 7){
    queryParams.devKey = row.unique
    const data = await IndexApi.getBoxIdByDevKey({devKey : queryParams.devKey});
    queryParams.boxId = data;
    haveSearch.value = false;
    handleQuery();
  }
}

const getDetail = async () => {

  if(!haveSearch.value){
    const data = await IndexApi.getHarmonicRedis(queryParams);
    harmonicRealTime.value = data;
    if(harmonicRealTime.value.times != null){
      realTimeVis.value = true;
    }else{
      realTimeVis.value = false;
    }
    haveSearch.value = true;
  }

  const lineData = await IndexApi.getHarmonicLine(queryParams);
  seriesAndTimeArr.value = lineData;
  if(seriesAndTimeArr.value.time != null && seriesAndTimeArr.value.time?.length > 0){
    const filteredSeries = seriesAndTimeArr.value.series.filter((item,index) => queryParams.harmonicArr.includes(index));
    harmonicLine.value.series = filteredSeries;
    harmonicLine.value.time = seriesAndTimeArr.value.time;
    lineVis.value = true;
  } else {
    lineVis.value = false;
  }
}


const filterText = ref('')
const treeRef = ref<InstanceType<typeof ElTree>>()

// const beforeSerUnmount = () => {
//     serChart?.dispose(); // 销毁图表实例
// };

// window.addEventListener('resize', function() {
//     rankChart?.resize(); 
//     powChart?.resize(); 
//     temChart?.resize(); 
// });

watch(filterText, (val) => {
  treeRef.value!.filter(val)
})

watch(() => [queryParams.harmonicType], () => {
  if(queryParams.harmonicType == 3){
    harmonicMultiple.value = [{
      label: "A相电流平均谐波",
      value: 1
      }]
    queryParams.harmonicArr = [1]
  }
  if(queryParams.harmonicType == 4){
    harmonicMultiple.value = [{
      label: "B相电流平均谐波",
      value: 1
      }]
    queryParams.harmonicArr = [1]
  }
  if(queryParams.harmonicType == 5){
    harmonicMultiple.value = [{
      label: "C相电流平均谐波",
      value: 1
      }]
  }
  haveSearch.value = false;
  handleQuery();
});

watch(() => [queryParams.harmonicArr], () => {
  if(seriesAndTimeArr.value.series != null && seriesAndTimeArr.value.series?.length > 0){
    const filteredSeries = seriesAndTimeArr.value.series.filter((item,index) => queryParams.harmonicArr.includes(index));
    harmonicLine.value.series = filteredSeries;
    harmonicLine.value.time = seriesAndTimeArr.value.time;
  }
});

// const loading = ref(false) // 列表的加载中

// const total = ref(0) // 列表的总页数
const queryFormRef = ref() // 搜索的表单
// const exportLoading = ref(false) // 导出的加载中

const getNavList = async() => {
  const res = await IndexApi.getBoxMenu()
  serverRoomArr.value = res
  if (res && res.length > 0) {
    const room = res[0]
    const keys = [] as string[]
    room.children.forEach(child => {
      if(child.children.length > 0) {
        child.children.forEach(son => {
          keys.push(son.id + '-' + son.type)
        })
      }
    })
  }
}


/** 搜索按钮操作 */
const handleQuery = async () => {
  if(queryParams.oldTime){

    await getDetail();
  }
}

onBeforeMount(async () =>{
  await getNavList();
  await getDetail();
})

/** 重置按钮操作 */
// const resetQuery = () => {
//   queryFormRef.value.resetFields()
//   handleQuery()
// }

/** 添加/修改操作 */
// const formRef = ref()
// const openForm = (type: string, id?: number) => {
//   formRef.value.open(type, id)
// }

// /** 删除按钮操作 */
// const handleDelete = async (id: number) => {
//   try {
//     // 删除的二次确认
//     await message.delConfirm()
//     // 发起删除
//     await PDUDeviceApi.deletePDUDevice(id)
//     message.success(t('common.delSuccess'))
//     // 刷新列表
//     await getList()
//   } catch {}
// }

// /** 导出按钮操作 */
// const handleExport = async () => {
//   try {
//     // 导出的二次确认
//     await message.exportConfirm()
//     // 发起导出
//     exportLoading.value = true
//     const data = await PDUDeviceApi.exportPDUDevice(queryParams)
//     download.excel(data, 'PDU设备.xls')
//   } catch {
//   } finally {
//     exportLoading.value = false
//   }
// }

/** 初始化 **/
onMounted( async () =>  {

})
</script>
<style scoped lang="scss">

.master {
  width: 100%;
  box-sizing: border-box;
  display: flex;
  .master-left {
    position: relative;
    overflow: hidden;
    box-sizing: border-box;
    margin-right: 20px;
    transition: all 0.2s linear;
    .openNavtree {
      box-sizing: border-box;
      text-align: right;
      cursor: pointer;
      position: absolute;
      right: 10px;
      top: 12px;
      font-size: 15px;
      display: flex;
      align-items: center;
    }
    .reduce {
      display: flex;
      align-items: center;
      position: absolute;
      right: 10px;
      top: 52px;
      color: #777777;
      cursor: pointer;
      font-size: 13px;
    }
    .expand {
      width: 30px;
      display: flex;
      flex-direction: column;
      align-items: center;
      color: #777;
      cursor: pointer;
      background-color: #eef4fc;
      padding: 10px 0;
    }
  }
  .master-right {
    flex: 1;
    overflow: hidden;
  }
}

.centered-div {
  display: flex;
  justify-content: center; /* 水平居中 */
  align-items: center; /* 垂直居中 */
  height: 100%; /* 使用父容器的高度 */
}
.right-div{
  display: flex;
  justify-content: right; /* 水平居右 */
  align-items: center; /* 垂直居中 */
  height: 100%; /* 使用父容器的高度 */
}

.navBar {
  box-sizing: border-box;
  width: 100%;
  height: 46px;
  line-height: 46px;
  padding-left: 10px;
  background-color: #d5ffc1;
  font-size: 14px;
}
.nav-left {
  width: 215px;
  height: 100%;
  .overview {
    padding: 0 20px;
    .count {
      height: 70px;
      margin-bottom: 15px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-left: 15px;
      padding-right: 10px;
      box-shadow: 0 3px 4px 1px rgba(0,0,0,.12);
      border-radius: 3px;
      border: 1px solid #eee;
      .info {
        height: 46px;
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        justify-content: space-between;
        font-size: 13px;
        .value {
          font-size: 15px;
          font-weight: bold;
        }
      }
    }
  }
  .status {
    display: flex;
    flex-wrap: wrap;
    .box {
      height: 70px;
      width: 50%;
      box-sizing: border-box;
      display: flex;
      justify-content: center;
      align-items: center;
      flex-direction: column;
      .top {
        display: flex;
        align-items: center;
        .tag {
          width: 8px;
          height: 8px;
          background-color: #3bbb00;
          margin-right: 3px;
          margin-top: 2px;
        }
        .empty {
          background-color: #ccc;
        }
        .warn {
          background-color: #ffc402;
        }
        .error {
          background-color: #fa3333;
        }
      }
      .value {
        font-size: 14px;
        margin-top: 5px;
        color: #aaa;
        .number {
          font-size: 14px;
          font-weight: bold;
          margin-right: 5px;
          color: #000;
        }
      }
    }
  }
  .header {
    display: flex;
    flex-direction: column;
    align-items: center;
    font-size: 13px;
    padding-top: 28px;
    .header_img {
      width: 110px;
      height: 110px;
      border-radius: 50%;
      display: flex;
      justify-content: center;
      align-items: center;
      border: 1px solid #555;
      img {
        width: 75px;
        height: 75px;
      }
    }
    .name {
      font-size: 15px;
      margin: 15px 0;
    }
  }
  .line {
    height: 1px;
    margin-top: 28px;
    margin-bottom: 20px;
    background: linear-gradient(297deg, #fff, #dcdcdc 51%, #fff);
  }
}
.progressContainer {
  display: flex;
  justify-content: center;
  align-items: center;
  .progress {
    width: 100px;
    height: 18px;
    line-height: 18px;
    font-size: 12px;
    box-sizing: border-box;
    border-radius: 150px;
    overflow: hidden;
    position: relative;
    vertical-align: middle;
    background-color: #bfa;
    display: flex;
    justify-content: center;
    .left {
      overflow: hidden;
      box-sizing: border-box;
      background-color: var(--el-color-primary);

    }
    .right {
      overflow: hidden;
      background-color:  #f56c6c;
    }
  }
}

:deep(.master-left .el-card__body) {
  padding: 0;
}
:deep(.el-form) {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
}
:deep(.el-form .el-form-item) {
  margin-right: 0;
}
</style>
