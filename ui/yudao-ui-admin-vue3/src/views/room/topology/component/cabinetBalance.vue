<template>
  <el-dialog v-model="dialogVisibleCur" @close="handleClose" width="80%" align-center>
    <!-- 自定义的头部内容（可选） -->
    <template #header>
      <div style="font-size: 18px;line-height: 24px;color: #303133;padding-bottom: 20px">机柜供电平衡</div>
      <CardTitle title="AB占比情况" />
      <div class="powerContainer" v-if="balanceObj.pow_active_percent > 0">
        <div class="power">
          <div class="label">有功功率：</div>
          <div class="progressContainer">
            <div class="progress">
              <div class="left" :style="`flex: ${balanceObj.pow_active_percent}`">{{balanceObj.pow_active_percent.toFixed(0)}}%</div>
              <div class="line"></div>
              <div class="right" :style="`flex: ${100 - balanceObj.pow_active_percent}`">{{100 - balanceObj.pow_active_percent.toFixed(0)}}%</div>
            </div>
          </div>
        </div>
        <div class="power">
          <div class="label">视在功率：</div>
          <div class="progressContainer">
            <div class="progress">
              <div class="left" :style="`flex: ${balanceObj.pow_apparent_percent}`">{{balanceObj.pow_apparent_percent.toFixed(0)}}%</div>
              <div class="line"></div>
              <div class="right" :style="`flex: ${100 - balanceObj.pow_apparent_percent}`">{{100 - balanceObj.pow_apparent_percent.toFixed(0)}}%</div>
            </div>
          </div>
        </div>
      </div>
    </template>
    <!-- 自定义的主要内容 -->
    <div class="custom-content">
      <CardTitle title="A路电流不平衡" />
      <div class="custom-content-container" v-if="apow !== null">
        <el-card class="cardChilc" shadow="hover">
          <curUnblance :max="balanceObj.imbalanceValueA"/>
        </el-card>
        <el-card class="cardChilc" style="margin: 0 10px" shadow="hover">
          <div class="IechartBar">
            <Echart :options="ABarOption" :height="300" />
          </div>
        </el-card>
        <el-card  class="cardChilc" shadow="hover">
          <div class="IechartBar">
            <Echart :options="ALineOption" :height="300"/>
          </div>
        </el-card>
      </div>

      <CardTitle title="B路电流不平衡" />
      <div class="custom-content-container" v-if="bpow !== null">
        <el-card class="cardChilc" shadow="hover">
          <volUnblance :max="balanceObj.imbalanceValueB"/>
        </el-card>
        <el-card class="cardChilc" style="margin: 0 10px" shadow="hover">
          <div class="IechartBar">
            <Echart :options="BBarOption" :height="300"/>
          </div>
        </el-card>
        <el-card  class="cardChilc" shadow="hover">
          <div class="IechartBar">
            <Echart :options="BLineOption" :height="300"/>
          </div>
        </el-card>
      </div>
    </div>
  </el-dialog>
</template>

<script lang="ts" setup>// 处理对话框关闭
import { EChartsOption } from 'echarts';
import { CabinetApi } from '@/api/cabinet/info';
import curUnblance from '@/views/cabinet/balance/component/curUnblance.vue';
import volUnblance from '@/views/cabinet/balance/component/volUnblance.vue';

const balanceObj = reactive({
  pow_apparent_percent: 0,
  pow_active_percent: 0,
  cur_valueA: [],
  cur_valueB: [],
  imbalanceValueA: 0,
  imbalanceValueB: 0,
  colorIndex: 0,
})

const cabinetId = ref();
const colorFlag = ref(0);
const dialogVisibleCur = ref(false);
const curdevkey = ref();
const busName = ref();
const curlocation = ref();
const apow = ref(null);
const bpow = ref(null);

const ABarOption = ref<EChartsOption>({});
const BBarOption = ref<EChartsOption>({});

const ALineOption = ref<EChartsOption>({
  title: {
    text: 'A路电流趋势',
    left: 'center'
  },
  tooltip: {
    trigger: 'axis'
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  yAxis: {
    type: 'value',
    name: '电流',
    axisLabel: {
      formatter: '{value} A'
    }
  },
  xAxis:{},
  series: []
})

const BLineOption = ref<EChartsOption>({
  title: {
    text: 'B路电流趋势',
    left: 'center'
  },
  tooltip: {
    trigger: 'axis'
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  yAxis: {
    type: 'value',
    name: '电流',
    axisLabel: {
      formatter: '{value} A'
    }
  },
  xAxis:{},
  series: []
})


/** 打开弹窗 */
const open = async (data) => {
  showDialog(data)
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

const handleClose = () => {
  dialogVisibleCur.value = false;

  balanceObj.pow_apparent_percent= 0;
  balanceObj.pow_active_percent= 0;
  balanceObj.cur_valueA= [];
  balanceObj.cur_valueB= [];
  balanceObj.imbalanceValueA= 0;
  balanceObj.imbalanceValueB= 0;
  balanceObj.colorIndex= 0;

}

const showDialog = async (item) => {
  cabinetId.value = item.id || 1;
  colorFlag.value = item.color;
  dialogVisibleCur.value = true;
  curdevkey.value = item.devKey;
  busName.value = item.busName;
  curlocation.value = item.location;
  apow.value = item.apow;
  bpow.value = item.bpow;
  await getBalanceDetail(item.id);
  await getBalanceTrend();

}

const getBalanceDetail = async(item) => {
  const data = await CabinetApi.getDetail({id:item});
  const res = data.redisData;
    balanceObj.imbalanceValueA = data.curUnbalancea
    balanceObj.imbalanceValueB = data.curUnbalanceb
  if (res && res.cabinet_power.path_a && res.cabinet_power.path_b) {
    if (res.cabinet_power.path_a.pow_apparent == 0) balanceObj.pow_apparent_percent = 0
    else balanceObj.pow_apparent_percent = (res.cabinet_power.path_a.pow_apparent / res.cabinet_power.total_data.pow_apparent as any).toFixed(2) * 100
    if (res.cabinet_power.path_a.pow_active == 0) balanceObj.pow_active_percent = 0
    else balanceObj.pow_active_percent = (res.cabinet_power.path_a.pow_active / res.cabinet_power.total_data.pow_active as any).toFixed(2) * 100
  }
  if (res && res.cabinet_power.path_a) {
    const cur_valueA = res.cabinet_power.path_a.cur_value.map(item => item.toFixed(2));
    ABarOption.value = {
      title: {
        text: 'A路电流饼形图',
        left: 'left'
      },
      tooltip: {
        trigger: 'item',
        formatter: '{b} : {c}'
      },
      series: [
        {
          type: 'pie',
          radius: [20, 120],
          center: ['50%', '50%'],
          roseType: 'radius',
          itemStyle: {
            borderRadius: 5
          },
          label: {
            show: true,
            position: 'inside', // 将标签显示在饼图内部
            formatter: (params) => {
              return `${params.value}A`;
            },
            fontSize: 14,
            fontWeight: 'bold'
          },
          data: [
            { value: cur_valueA[0], name: 'A相电流', itemStyle: { color: '#075F71' } },
            { value: cur_valueA[1], name: 'B相电流', itemStyle: { color: '#119CB5' } },
            { value: cur_valueA[2], name: 'C相电流', itemStyle: { color: '#45C0C9' } },
          ]
        }
      ]
    }
  }
  if (res && res.cabinet_power.path_b) {
    const cur_valueB = res.cabinet_power.path_b.cur_value.map(item => item.toFixed(2));
    BBarOption.value = {
      title: {
        text: 'B路电流饼形图',
        left: 'left'
      },
      tooltip: {
        trigger: 'item',
        formatter: '{b} : {c}'
      },
      series: [
        {
          type: 'pie',
          radius: [20, 120],
          center: ['50%', '50%'],
          roseType: 'radius',
          itemStyle: {
            borderRadius: 5
          },
          label: {
            show: true,
            position: 'inside', // 将标签显示在饼图内部
            formatter: (params) => {
              return `${params.value}A`;
            },
            fontSize: 14,
            fontWeight: 'bold'
          },
          data: [
            { value: cur_valueB[0], name: 'A相电流', itemStyle: { color: '#075F71' } },
            { value: cur_valueB[1], name: 'B相电流', itemStyle: { color: '#119CB5' } },
            { value: cur_valueB[2], name: 'C相电流', itemStyle: { color: '#45C0C9' } },
          ]
        }
      ]
    }
  }
}

const getBalanceTrend = async () => {
  const res = await CabinetApi.getBalanceTrend({
    id: cabinetId.value
  })
  if (res.length > 0) {
    const timeList = res.map(item => item.dateTime)
    if(res[0].curA && res[0].curA.length == 1) {
      ALineOption.value.xAxis = {
        type: 'category',
        boundaryGap: false,
        data: timeList
      }
      ALineOption.value.series = [
        {
          name: 'A1',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.curA[0].curValue.toFixed(2)),
        },
      ]
    } else if (res[0].curA && res[0].curA.length == 3) {
      ALineOption.value.xAxis = {
        type: 'category',
        boundaryGap: false,
        data: timeList
      }
      ALineOption.value.series = [
        {
          name: 'A1',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.curA[0].curValue.toFixed(2)),
        },
        {
          name: 'A2',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.curA[1].curValue.toFixed(2)),
        },
        {
          name: 'A3',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.curA[2].curValue.toFixed(2)),
        },
      ]
    }
    if (res[0].curB && res[0].curB.length == 1) {
      BLineOption.value.xAxis = {
        type: 'category',
        boundaryGap: false,
        data: timeList
      }
      BLineOption.value.series = [
        {
          name: 'B1',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.curB[0].curValue.toFixed(2)),
        },
      ]
    } else if(res[0].curB && res[0].curB.length == 3) {
      BLineOption.value.xAxis = {
        type: 'category',
        boundaryGap: false,
        data: timeList
      }
      BLineOption.value.series = [
        {
          name: 'B1',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.curB[0].curValue.toFixed(2)),
        },
        {
          name: 'B2',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.curB[1].curValue.toFixed(2)),
        },
        {
          name: 'B3',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.curB[2].curValue.toFixed(2)),
        },
      ]
    }
  }
}

</script>

<style lang="scss" scoped>
:deep(.el-form) {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
}
:deep(.el-form .el-form-item) {
  margin-right: 0;
}
.navInfo {
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
      .progress {
        width: 100px;
        display: flex;
        align-items: center;
        font-size: 14px;
        color: #eee;
        box-sizing: border-box;
        position: relative;
        display: flex;
        justify-content: center;
        .progress-line {
          width: 3px;
          height: 25px;
          background-color: #000;
        }
        .left {
          text-align: center;
          box-sizing: border-box;
          background-color: #3b8bf5;
          // border-right: 1px solid #000;
        }
        .right {
          text-align: center;
          background-color:  #f86f13;
        }
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
.matrixContainer {
  height: 720px;
  // calc(100vh - 320px);
  overflow: auto;
  display: flex;
  flex-wrap: wrap;
  align-content: flex-start;
  .item {
    width: 25%;
    min-width: 290px;
    height: 130px;
    font-size: 12px;
    box-sizing: border-box;
    background-color: #eef4fc;
    border: 5px solid #fff;
    position: relative;
    .progressContainer {
      position: relative;
      height: 50px;
      display: flex;
      justify-content: center;
      align-items: center;
      margin-top: 40px;
      .progress {
        width: 180px;
        display: flex;
        align-items: center;
        font-size: 14px;
        color: #eee;
        box-sizing: border-box;
        position: relative;
        display: flex;
        justify-content: center;
        .tip {
          width: 180px;
          position: absolute;
          top: -12px;
          left: 0;
          display: flex;
          justify-content: space-between;
          color: #000;
          font-size: 12px;
        }
        .line {
          width: 3px;
          height: 25px;
          background-color: #000;
        }
        .left {
          text-align: center;
          box-sizing: border-box;
          background-color: #3b8bf5;
          // border-right: 1px solid #000;
        }
        .right {
          text-align: center;
          background-color:  #f86f13;
        }
      }
    }
    .content {
      font-size: 14px;
      display: flex;
      align-items: center;
      justify-content: center;
      & > .valueList:last-of-type {
        margin-right: 0;
      }
      .road {
        margin-right: 10px;
      }
      .valueList {
        margin-right: 40px;
      }
    }
    .room {
      position: absolute;
      left: 10px;
      top: 8px;
      font-size: 13px;
    }
    .detail {
      width: 35px;
      height: 20px;
      cursor: pointer;
      font-size: 12px;
      padding: 0;
      border: 1px solid #ccc;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #fff;
      position: absolute;
      right: 10px;
      bottom: 8px;
    }
  }
}

.powerContainer {
  display: flex;
  .power {
    width: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    // padding-left: 50px;
    margin: 20px 0;
    .label {
      font-size: 16px;
      font-weight: bold;
    }
    .progressContainer {
      display: flex;
      justify-content: center;
      align-items: center;
      margin-left: 30px;
      .progress {
        width: 400px;
        display: flex;
        align-items: center;
        font-size: 14px;
        color: #eee;
        box-sizing: border-box;
        position: relative;
        display: flex;
        justify-content: center;
        .line {
          width: 3px;
          height: 36px;
          background-color: #000;
        }
        .left {
          text-align: center;
          box-sizing: border-box;
          background-color: #3b8bf5;
          // border-right: 1px solid #000;
        }
        .right {
          text-align: center;
          background-color:  #f86f13;
        }
      }
    }
  }
}

:deep(.el-dialog) {
  width: 80%;
  margin-top: 50px;
  background-color: #f1f1f1;
}

.custom-content{
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.custom-content-container{
  display: flex;
  justify-content: space-between;
  flex-wrap: nowrap;
}

.cardChilc {
  flex: 1;
  height: 100%;
}

.IechartBar {
  width: 100%;
  height: 100%;
}
</style>