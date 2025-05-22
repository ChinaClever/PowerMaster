<template>
  <el-dialog v-model="detailVis" width="80%" align-center>
    <div class="custom-row" style="display: flex; align-items: center;">
      <!-- 位置标签 -->
      <div class="location-tag el-col">
        <span style="margin-right:10px;font-size:18px;font-weight:bold;">温度详情</span>
        <span>机房：{{ roomName }}&nbsp;&nbsp;</span>
        <span>母线：{{ busName }}&nbsp;&nbsp;</span>
        <span>插接箱：{{ boxName }}&nbsp;&nbsp;</span>
        <span> 网络地址：{{ devkey }}</span>
      </div>

      <!-- 日期选择器 -->
      <div class="date-picker-col el-col"> 
        <el-date-picker
          v-model="queryParams.oldTime"
          :clearable = "false"
          :editable = "false"
          :showNow = "false"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="datetime"
          :disabled-date="disabledDate"
          @change="handleDayPick()"
          placeholder="选择日期时间"
        />
        <el-button @click="subtractOneDay(); handleDayPick()" type="primary" style="margin-left:10px;">&lt; 前一日</el-button>
        <el-button :disabled="clickAdd" @click="addtractOneDay(); handleDayPick()" type="primary">&gt; 后一日</el-button>
      </div>

      <!-- 图表/数据切换按钮组 -->
      <div class="chart-data-buttons el-col" style="margin-right: 50px;">
        <div class="button-group">
          <el-select v-model="typeRadioShow" placeholder="请选择" style="width: 100px">
            <el-option label="平均" value="平均" />
            <el-option label="最大" value="最大" />
            <el-option label="最小" value="最小" />
          </el-select>
          <el-button @click="switchChartOrTable = 0" :type="switchChartOrTable === 0 ? 'primary' : ''">图表</el-button>
          <el-button @click="switchChartOrTable = 1" :type="switchChartOrTable === 1 ? 'primary' : ''">数据</el-button>
          <el-button type="success" plain @click="handleExportXLS" :loading="exportLoading">
            <i class="el-icon-download"></i> 导出
          </el-button>
        </div>
      </div>
    </div>
    <br/>
    <TemDetail v-if="switchChartOrTable == 0" width="75vw" height="70vh"  :list="temESList"  />
    <div v-else-if="switchChartOrTable == 1" style="width: 100%;height:70vh;overflow-y:auto;">
      <el-table :data="temTableList" :stripe="true" :show-overflow-tooltip="true" style="height:70vh;">
        <el-table-column label="序号" align="center" prop="index" width="60px" />
        <el-table-column label="时间" align="center" prop="create_time" />
        <el-table-column label="A相温度" align="center" prop="temAvgValueA">
          <template #default="scope">
            <el-text line-clamp="2"> {{ typeRadioShow == '最大' ? scope.row.tem_a_max_value : (typeRadioShow == '最小' ? scope.row.tem_a_min_value : scope.row.tem_a_avg_value) }}°C </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="typeRadioShow != '平均'" label="发生时间" align="center" :prop="`tem_a${typeRadioShow == '最大' ? '_max_' : '_min_'}time`" />

        <el-table-column label="B相温度" align="center" prop="temAvgValueB">
          <template #default="scope">
            <el-text line-clamp="2"> {{ typeRadioShow == '最大' ? scope.row.tem_b_max_value : (typeRadioShow == '最小' ? scope.row.tem_b_min_value : scope.row.tem_b_avg_value) }}°C </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="typeRadioShow != '平均'" label="发生时间" align="center" :prop="`tem_b${typeRadioShow == '最大' ? '_max_' : '_min_'}time`" />

        <el-table-column label="C相温度" align="center" prop="temAvgValueC">
          <template #default="scope">
            <el-text line-clamp="2"> {{ typeRadioShow == '最大' ? scope.row.tem_c_max_value : (typeRadioShow == '最小' ? scope.row.tem_c_min_value : scope.row.tem_c_avg_value) }}°C </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="typeRadioShow != '平均'" label="发生时间" align="center" :prop="`tem_c${typeRadioShow == '最大' ? '_max_' : '_min_'}time`" />

        <el-table-column label="N相温度" align="center" prop="temAvgValueN">
          <template #default="scope">
            <el-text line-clamp="2"> {{ typeRadioShow == '最大' ? scope.row.tem_n_max_value : (typeRadioShow == '最小' ? scope.row.tem_n_min_value : scope.row.tem_n_avg_value) }}°C </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="typeRadioShow != '平均'" label="发生时间" align="center" :prop="`tem_n${typeRadioShow == '最大' ? '_max_' : '_min_'}time`" />
      </el-table>
    </div>
  </el-dialog>
</template>

<script lang="ts" setup>
import download from '@/utils/download'
import { IndexApi } from '@/api/bus/boxindex'
import { BusPowerLoadDetailApi } from '@/api/bus/buspowerloaddetail'

import TemDetail from '@/views/box/tem/component/TemDetail.vue'

const location = ref() as any;
const roomName = ref() as any;
const devkey = ref() as any;
const busName = ref() as any;
const boxName = ref() as any;
const detailVis = ref(false);
const switchValue = ref(0)
const switchChartOrTable = ref(0);

const temESList = ref([]) as any;
const temTableList = ref([]) as any;

const typeRadioShow = ref("最大")

const clickAdd = ref(true)

const queryParams = reactive({
  pageNo: 1,
  pageSize: 24,
  devKey : history?.state?.devKey as string | undefined,
  createTime: [],
  cascadeNum: undefined,
  serverRoomData:undefined,
  status:[],
  cabinetIds : [],
})as any

const exportLoading = ref(false) // 导出的加载中

/** 打开弹窗 */
const open = async (data) => {
  const res = await BusPowerLoadDetailApi.getBoxIdAndLocation({devKey: data.devKey});
  openTemDetail(res)
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

const openTemDetail = async (row) =>{
  queryParams.boxId = row.boxId;
  queryParams.oldTime = getFullTimeByDate(new Date(new Date().getFullYear(),new Date().getMonth(),new Date().getDate(),0,0,0));
  location.value = row.location ? row.location : '未绑定'
  roomName.value = row.roomName
  busName.value = row.busName
  boxName.value = row.boxName
  devkey.value = row.devKey;
  await getDetail();
  clickAdd.value = true
  detailVis.value = true;
  typeRadioShow.value = '最大'
}

/** 查询列表 */
const getDetail = async () => {
  const data = await IndexApi.getBoxTemDetail(queryParams);
  temTableList.value = data?.table
  temTableList.value?.forEach((obj,index) => {
    obj.index = index+1
    obj.tem_a_avg_value = obj?.tem_a_avg_value?.toFixed(0);
    obj.tem_b_avg_value = obj?.tem_b_avg_value?.toFixed(0);
    obj.tem_c_avg_value = obj?.tem_c_avg_value?.toFixed(0);
    obj.tem_n_avg_value = obj?.tem_n_avg_value?.toFixed(0);

    obj.tem_a_max_value = obj?.tem_a_max_value?.toFixed(0);
    obj.tem_b_max_value = obj?.tem_b_max_value?.toFixed(0);
    obj.tem_c_max_value = obj?.tem_c_max_value?.toFixed(0);
    obj.tem_n_max_value = obj?.tem_n_max_value?.toFixed(0);

    obj.tem_a_max_time = obj.tem_a_max_time ? obj.tem_a_max_time.slice(0,-3) : '';
    obj.tem_b_max_time = obj.tem_b_max_time ? obj.tem_b_max_time.slice(0,-3) : '';
    obj.tem_c_max_time = obj.tem_c_max_time ? obj.tem_c_max_time.slice(0,-3) : '';
    obj.tem_n_max_time = obj.tem_n_max_time ? obj.tem_n_max_time.slice(0,-3) : '';

    obj.tem_a_min_value = obj?.tem_a_min_value?.toFixed(0);
    obj.tem_b_min_value = obj?.tem_b_min_value?.toFixed(0);
    obj.tem_c_min_value = obj?.tem_c_min_value?.toFixed(0);
    obj.tem_n_min_value = obj?.tem_n_min_value?.toFixed(0);
    
    obj.tem_a_min_time = obj.tem_a_min_time ? obj.tem_a_min_time.slice(0,-3) : '';
    obj.tem_b_min_time = obj.tem_b_min_time ? obj.tem_b_min_time.slice(0,-3) : '';
    obj.tem_c_min_time = obj.tem_c_min_time ? obj.tem_c_min_time.slice(0,-3) : '';
    obj.tem_n_min_time = obj.tem_n_min_time ? obj.tem_n_min_time.slice(0,-3) : '';
  });

  getTemESList()
}

watch( ()=>typeRadioShow.value, ()=>{
  getTemESList()
})

const getTemESList = () => {
  let itemTemType = typeRadioShow.value == "最小" ? '_min_' : (typeRadioShow.value == "最大" ? '_max_' : '')

  temESList.value = []
  if(itemTemType != '') {
    temESList.value = temTableList.value.map(obj => ({
      tem_a_value: obj[`tem_a${itemTemType}value`],
      tem_time: [
        obj[`tem_a${itemTemType}time`],
        obj[`tem_b${itemTemType}time`],
        obj[`tem_c${itemTemType}time`],
        obj[`tem_n${itemTemType}time`],
      ],
      tem_b_value: obj[`tem_b${itemTemType}value`],
      tem_c_value: obj[`tem_c${itemTemType}value`],
      tem_n_value: obj[`tem_n${itemTemType}value`],
      time: obj.create_time
    }));
  } else {
    temESList.value = temTableList.value.map(obj => ({
      tem_a_value: obj.tem_a_avg_value,
      tem_b_value: obj.tem_b_avg_value,
      tem_c_value: obj.tem_c_avg_value,
      tem_n_value: obj.tem_n_avg_value,
      time: obj.create_time
    }));
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

const message = useMessage() // 消息弹窗

const handleExportXLS = async ()=>{
  try {
    // 导出的二次确认
    await message.exportConfirm();
    // 发起导出
    queryParams.pageNo = 1;
    exportLoading.value = true;
    const axiosConfig = {
      timeout: 0 // 设置超时时间为0
    }
    const data = await IndexApi.getBoxTemDetailExcel(queryParams, axiosConfig);
    console.log("data",data);
    await download.excel(data, '温度详细.xlsx');
  } catch (error) {
    // 处理异常
    console.error('导出失败：', error);
  } finally {
    exportLoading.value = false;
  }
}

const handleDayPick = async () => {
  if(queryParams?.oldTime ){
    var date = new Date(queryParams.oldTime + 'Z') // 添加 "Z" 表示 UTC 时间

    var today = new Date(); // 今天的日期
    today.setHours(0, 0, 0, 0); // 去掉时间部分，只比较日期


    if (date.getFullYear() == today.getFullYear() && date.getMonth() == today.getMonth() && date.getDate() == today.getDate()) {
      clickAdd.value = true
    } else {
      clickAdd.value = false
    }

    await getDetail();
  } 
}

const subtractOneDay = () => {
  var date = new Date(queryParams.oldTime + "Z"); // 添加 "Z" 表示 UTC 时间

  date.setDate(date.getDate() - 1); // 减去一天
  
  clickAdd.value = false

  queryParams.oldTime = date.toISOString().slice(0, 19).replace("T", " "); // 转换为新的日期字符串
};

const addtractOneDay = () => {
  var date = new Date(queryParams.oldTime + "Z"); // 添加 "Z" 表示 UTC 时间

  date.setDate(date.getDate() + 1); // 加一天

  var today = new Date(); // 今天的日期
  today.setHours(0, 0, 0, 0); // 去掉时间部分，只比较日期


  if (date.getFullYear() == today.getFullYear() && date.getMonth() == today.getMonth() && date.getDate() == today.getDate()) {
    clickAdd.value = true
  }

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
</script>

<style lang="scss" scoped>
:deep(.ip:hover) {
  color: blue !important;
  cursor: pointer;
}

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

.btn_offline,
.btn_normal,
.btn_warn,
.btn_error {
  width: 58px;
  height: 35px;
  cursor: pointer;
  border-radius: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
  &:hover {
    color: #7bc25a;
  }
}
.btn_offline {
  border: 1px solid #aaa;
  background-color: #fff;
  margin-right: 8px;
}
.offline {
  background-color: #aaa;
  color: #fff;
  &:hover {
    color: #fff;
  }
}
.btn_normal {
  border: 1px solid #3bbb00;
  background-color: #fff;
  margin-right: 8px;
}
.normal {
  background-color: #3bbb00;
  color: #fff;
  &:hover {
    color: #fff;
  }
}
.btn_warn {
  border: 1px solid #ffc402;
  background-color: #fff;
  margin-right: 8px;
}
.warn {
  background-color: #ffc402;
  color: #fff;
  &:hover {
    color: #fff;
  }
}
.btn_error {
  border: 1px solid #fa3333;
  background-color: #fff;
  margin-right: 8px;
}
.error {
  background-color: #fa3333;
  color: #fff;
  &:hover {
    color: #fff;
  }
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
    margin-top: 30px;
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

@media screen and (min-width:2048px){
  .arrayContainer {
    width:100%;
    height: 720px;
    overflow: hidden;
    overflow-y: auto;
    display: flex;
    flex-wrap: wrap;
    align-content: flex-start;
    margin-top: -10px;
  .arrayItem {
    width: 25%;
    height: 140px;
    font-size: 13px;
    box-sizing: border-box;
    background-color: #eef4fc;
    border: 5px solid #fff;
    padding-top: 40px;
    position: relative;
    .content {
      display: flex;
      align-items: center;
      justify-content: space-between;
      .count_img {
        margin: 0 35px 0 13px;
      }
      .icon {
        width: 60px;
        height: 60px;
        margin: 0 64px 0 0;
        text-align: center;
      }
      .info{
          font-size: 14px;
          margin-left: 20px;
        }
    }
    .devKey{
      position: absolute;
      left: 8px;
      top: 8px;
    }
    .room {
      position: absolute;
      left: 8px;
      top: 8px;
    }
    .status {
      width: 40px;
      height: 20px;
      font-size: 12px;
      display: flex;
      align-items: center;
      justify-content: center;

      color: #fff;
      position: absolute;
      right: 38px;
      top: 8px;
    }
    .detail {
      width: 40px;
      height: 25px;
      padding: 0;
      border: 1px solid #ccc;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #fff;
      position: absolute;
      right: 8px;
      bottom: 8px;
      cursor: pointer;
    }
  }
}
}

@media screen and (max-width:2048px) and (min-width:1600px){
  .arrayContainer {
    width:100%;
    height: 720px;
    overflow: hidden;
    overflow-y: auto;
    display: flex;
    flex-wrap: wrap;
    align-content: flex-start;
    margin-top: -10px;
  .arrayItem {
    width: 25%;
    height: 140px;
    font-size: 13px;
    box-sizing: border-box;
    background-color: #eef4fc;
    border: 5px solid #fff;
    padding-top: 40px;
    position: relative;
    .content {
      display: flex;
      align-items: center;
      justify-content: space-between;
      .count_img {
        margin: 0 35px 0 13px;
      }
      .icon {
        width: 60px;
        height: 30px;
        margin: 0 64px 0 0;
        text-align: center;
      }
      .info{
          font-size: 14px;
          margin-left: 20px;
        }
    }
    .devKey{
      position: absolute;
      left: 8px;
      top: 8px;
    }
    .room {
      position: absolute;
      left: 8px;
      top: 8px;
    }
    .status {
      width: 40px;
      height: 20px;
      font-size: 12px;
      display: flex;
      align-items: center;
      justify-content: center;

      color: #fff;
      position: absolute;
      right: 38px;
      top: 8px;
    }
    .detail {
      width: 40px;
      height: 25px;
      padding: 0;
      border: 1px solid #ccc;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #fff;
      position: absolute;
      right: 8px;
      bottom: 8px;
      cursor: pointer;
    }
  }
}
}

@media screen and (max-width:1600px){
  .arrayContainer {
    width:100%;
    height: 720px;
    overflow: hidden;
    overflow-y: auto;
    display: flex;
    flex-wrap: wrap;
    align-content: flex-start;
    margin-top: -10px;
  .arrayItem {
    width: 33%;
    height: 140px;
    font-size: 13px;
    box-sizing: border-box;
    background-color: #eef4fc;
    border: 5px solid #fff;
    padding-top: 40px;
    position: relative;
    .content {
      display: flex;
      align-items: center;
      justify-content: space-between;
      .count_img {
        margin: 0 35px 0 13px;
      }
      .icon {
        width: 60px;
        height: 60px;
        margin: 0 64px 0 0;
        text-align: center;
      }
      .info{
          font-size: 14px;
          margin-left: 20px;
        }
    }
    .devKey{
      position: absolute;
      left: 8px;
      top: 8px;
    }
    .room {
      position: absolute;
      left: 8px;
      top: 8px;
    }
    .status {
      width: 40px;
      height: 20px;
      font-size: 12px;
      display: flex;
      align-items: center;
      justify-content: center;

      color: #fff;
      position: absolute;
      right: 38px;
      top: 8px;
    }
    .detail {
      width: 40px;
      height: 25px;
      padding: 0;
      border: 1px solid #ccc;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #fff;
      position: absolute;
      right: 8px;
      bottom: 8px;
      cursor: pointer;
    }
  }
}
}

.btnallSelected {
  margin-right: 10px;
  width: 58px;
  height: 32px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #409EFF;
  color: white;
  border: none;
  border-radius: 5px;
}

.btnallNotSelected{
  margin-right: 10px;
  width: 58px;
  height: 32px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  color: #000000;
  border: 1px solid #409EFF;
  border-radius: 5px;
  &:hover {
    color: #7bc25a;
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

::v-deep .el-table .el-table__header th{
  background-color: #f5f7fa;
  color: #909399;
  height: 80px;

}

.custom-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: nowrap;
  margin-top:-50px;
}
 
.button-group {
  display: flex;
  gap: 10px;
}

:deep(.el-card){
  --el-card-padding:5px;
}

:deep(.el-tag){
  margin-right:-60px;
}

:deep(.el-dialog){
  width: 80%;
  height: 80%;
  margin-top: 100px;
}

.flex-container {
  display: flex;
  flex-direction: column;
}
 
.flex-row {
  display: flex;
  justify-content: space-between;
}
 
.flex-row > div {
  flex: 1;
  margin: 10px;
  text-align: center;
}
</style>