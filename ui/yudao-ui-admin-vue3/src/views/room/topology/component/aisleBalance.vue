<template>
  <el-dialog v-loading="detailLoading" v-model="showDetailDialog" @close="handleClose" title="柜列供电平衡" width="80%" align-center>
    <!-- 自定义的头部内容（可选） -->
    <!-- <template #header>
      <CardTitle title="AB占比情况" />
    </template> -->
    <!-- 自定义的主要内容 -->
    <div class="custom-content">
      <div class="powerContainer" 
      v-if="balanceObj.a_active_power !=null && balanceObj.b_active_power != null"
      >
        <el-card class="topCard">
          <div class="powerInDialog">
            <div class="label"><b>总有功功率</b></div>
            <div class="progressContainer">
              <!-- <div class="progress progressInDialog">
                <div class="left" :style="`flex: ${balanceObj.pow_active_percent}`">{{balanceObj.pow_active_percent.toFixed(0)}}%</div>
                <div class="line"></div>
                <div class="right" :style="`flex: ${100 - balanceObj.pow_active_percent}`">{{100 - balanceObj.pow_active_percent.toFixed(0)}}%</div>
              </div> -->
              <div class="progressInDialog">
                <div class="left" :style="{
                  width:`${0.8*Math.max(Math.min(balanceObj.pow_active_percent,60),40)}%`
                }">{{balanceObj.pow_active_percent!=null?balanceObj.pow_active_percent.toFixed(0):0}}%</div>
                <div class="line"></div>
                <div class="right" :style="{
                  width:`${0.8*Math.max(Math.min((100 - balanceObj.pow_active_percent),60),40)}%`
                }">{{balanceObj.pow_active_percent==null? 100 - balanceObj.pow_active_percent.toFixed(0):0}}%</div>
              </div>
              <div class="tipInDialog">
                <span class="leftTip" :style="{width: 0.8*Math.max(Math.min(balanceObj.pow_active_percent,60),40)+'%'}">
                  <div style="white-space: nowrap;">{{balanceObj.a_active_power ? balanceObj.a_active_power: '0.000'}}kW</div>
                  <div style="white-space: nowrap;">A路有功功率</div>
                </span>
                <span class="rightTip" :style="{width:0.8*Math.max(Math.min((100 - balanceObj.pow_active_percent),60),40)+'%'}">
                  <div style="white-space: nowrap;">{{balanceObj.b_active_power ? balanceObj.b_active_power: '0.000'}}kW</div>
                  <div style="white-space: nowrap;">B路有功功率</div>
                </span>
              </div>
          </div>
        </div>
        </el-card>
        <el-card class="topCard" style="margin: 0 15px">
          <div class="powerInDialog">
            <div class="label"><b>总视在功率</b></div>
            <div class="progressContainer">
              <!-- <div class="progress">
                <div class="left" :style="`flex: ${balanceObj.pow_apparent_percent}`">{{balanceObj.pow_apparent_percent.toFixed(0)}}%</div>
                <div class="line"></div>
                <div class="right" :style="`flex: ${100 - balanceObj.pow_apparent_percent}`">{{100 - balanceObj.pow_apparent_percent.toFixed(0)}}%</div>
              </div> -->
              <!-- <div class="progressInDialog">
                  <span class="left" :style="{
                  width:`${0.8*Math.max(Math.min(balanceObj.pow_active_percent,60),40)}%`
                }">
                    <div style="white-space: nowrap;">{{balanceObj.a_apparent_power ? balanceObj.a_apparent_power: '0.000'}}kVA</div>
                    <div style="white-space: nowrap;">A路视在功率</div>
                  </span>
                  <div class="line"></div>
                  <span class="right" :style="{
                  width:`${0.8*Math.max(Math.min((100 - balanceObj.pow_active_percent),60),40)}%`
                }">
                    <div style="white-space: nowrap;">{{balanceObj.b_apparent_power ? balanceObj.b_apparent_power: '0.000'}}kVA</div>
                    <div style="white-space: nowrap;">B路视在功率</div>
                  </span>
              </div> -->
              <div class="progressInDialog">
                <div class="left" :style="{
                  width:`${0.8*Math.max(Math.min(balanceObj.pow_apparent_percent,60),40)}%`
                }">{{balanceObj.pow_apparent_percent!=null?balanceObj.pow_apparent_percent.toFixed(0):0}}%</div>
                <div class="line"></div>
                <div class="right" :style="{
                  width:`${0.8*Math.max(Math.min((100 - balanceObj.pow_apparent_percent),60),40)}%`
                }">{{balanceObj.pow_apparent_percent!=null?(100 - balanceObj.pow_apparent_percent.toFixed(0)):0}}%</div>
              </div>
              <div class="tipInDialog">
                <span class="leftTip" :style="{width: 0.8*Math.max(Math.min(balanceObj.pow_apparent_percent,60),40)+'%'}">
                  <div style="white-space: nowrap;">{{balanceObj.a_apparent_power ? balanceObj.a_apparent_power: '0.000'}}kVA</div>
                  <div style="white-space: nowrap;">A路视在功率</div>
                </span>
                <span class="rightTip" :style="{width:0.8*Math.max(Math.min((100 - balanceObj.pow_apparent_percent),60),40)+'%'}">
                  <div style="white-space: nowrap;">{{balanceObj.b_apparent_power ? balanceObj.b_apparent_power: '0.000'}}kVA</div>
                  <div style="white-space: nowrap;">B路视在功率</div>
                </span>
              </div>
            </div>
          </div>
        </el-card>
        <el-card class="topCard">
          <div class="powerInDialog">
            <div class="label"><b>总无功功率</b></div>
            <div class="progressContainer">
              <div class="progressInDialog">
                <div class="left" :style="{
                  width:`${0.8*Math.max(Math.min(balanceObj.pow_reactive_percent,60),40)}%`
                }">{{balanceObj.pow_reactive_percent != null?balanceObj.pow_reactive_percent.toFixed(0):0}}%</div>
                <div class="line"></div>
                <div class="right" :style="{
                  width:`${0.8*Math.max(Math.min((100 - balanceObj.pow_reactive_percent),60),40)}%`
                }">{{balanceObj.pow_reactive_percent !=null?100 - balanceObj.pow_reactive_percent.toFixed(0):0}}%</div>
              </div>
              <div class="tipInDialog">
                <span class="leftTip" :style="{width: 0.8*Math.max(Math.min(balanceObj.pow_reactive_percent,60),40)+'%'}">
                  <div style="white-space: nowrap;">{{balanceObj.a_reactive_power ? balanceObj.a_reactive_power: '0.000'}}kVar</div>
                  <div style="white-space: nowrap;">A路无功功率</div>
                </span>
                <span class="rightTip" :style="{width:0.8*Math.max(Math.min((100 - balanceObj.pow_reactive_percent),60),40)+'%'}">
                  <div style="white-space: nowrap;">{{balanceObj.b_reactive_power ? balanceObj.b_reactive_power: '0.000'}}kVar</div>
                  <div style="white-space: nowrap;">B路无功功率</div>
                </span>
              </div>
            </div>
          </div>
        </el-card>
      </div>
      <div style="margin-top: 15px;" class="custom-content-container" v-if="apow !== null">
        <el-card class="cardChilc" shadow="hover">
          <curUnblance :max="balanceObj.imbalanceValueA==null?0:balanceObj.imbalanceValueA.toFixed(0)" name="A路电流不平衡"/>
        </el-card>
        <el-card class="cardChilc" style="margin: 0 15px; position: relative;" shadow="hover">
          <div class="IechartBar" style="position: relative; left: -10px;">
            <Echart :options="ABarOption" :height="300" />
          </div>
          <div v-show="ABarOption!=null&&ABarOption?.series!=null" style="display: inline-block;
              position: absolute;
              width: 150px;
              height: 100px;
              top: 35%;
              right: 0;">
            <div>
              <span class="bullet" style="color:#E5B849;position: absolute;right: 115px;">•</span><span style="width:50px;font-size:14px;position: absolute; right: 65px;white-space: nowrap;overflow: visible;">Ia：<span style="font-size:16px;">{{ABarOption.series[0].data[0].value}}A</span></span>
            </div>
            <div style="margin-top:30px;">
              <span class="bullet" style="color:#C8603A;position: absolute;right: 115px;">•</span><span style="width:50px;font-size:14px;position: absolute; right: 65px;white-space: nowrap;overflow: visible;">Ib：<span style="font-size:16px;">{{ABarOption.series[0].data[1].value}}A</span></span>
            </div>
            <div style="margin-top:60px;">
              <span class="bullet" style="color:#AD3762;position: absolute;right: 115px;">•</span><span style="width:50px;font-size:14px;position: absolute; right: 65px;white-space: nowrap;overflow: visible;">Ic：<span style="font-size:16px">{{ABarOption.series[0].data[2].value}}A</span></span>
            </div>
          </div>
        </el-card>
        <el-card  class="cardChilc" shadow="hover">
          <div class="IechartBar">
            <Echart :options="ALineOption" :height="300"/>
          </div>
        </el-card>
      </div>
      <br/>
      <div class="custom-content-container" style="margin-top: -5px;" v-if="bpow !== null">
        <el-card class="cardChilc" shadow="hover">
          <volUnblance :max="balanceObj.imbalanceValueB==null?0:balanceObj.imbalanceValueB.toFixed(0)" name="B路电流不平衡"/>
        </el-card>
        <el-card class="cardChilc" style="margin: 0 15px;position: relative;" shadow="hover">
          <div class="IechartBar">
            <Echart :options="BBarOption" :height="300"/>
          </div>
          <div v-show="BBarOption!=null&&BBarOption?.series!=null" style="display: inline-block;
              position: absolute;
              width: 150px;
              height: 100px;
              top: 35%;
              right: 0;">
            <div>
              <span class="bullet" style="color:#E5B849;position: absolute;right: 115px;">•</span><span style="width:50px;font-size:14px;position: absolute; right: 65px;white-space: nowrap;overflow: visible;">Ia：<span style="font-size:16px;">{{BBarOption.series[0].data[0].value}}A</span></span>
            </div>
            <div style="margin-top:30px;">
              <span class="bullet" style="color:#C8603A;position: absolute;right: 115px;">•</span><span style="width:50px;font-size:14px;position: absolute; right: 65px;white-space: nowrap;overflow: visible;">Ib：<span style="font-size:16px">{{BBarOption.series[0].data[1].value}}A</span></span>
            </div>
            <div style="margin-top:60px;">
              <span class="bullet" style="color:#AD3762;position: absolute;right: 115px;">•</span><span style="width:50px;font-size:14px;position: absolute; right: 65px;white-space: nowrap;overflow: visible;">Ic：<span style="font-size:16px;">{{BBarOption.series[0].data[2].value}}A</span></span>
            </div>
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

<script lang="ts" setup>
import { IndexApi } from '@/api/aisle/aisleindex'
const curUnblance = defineAsyncComponent(() => import('@/views/aisle/balance/component/curUnblance.vue'))
const volUnblance = defineAsyncComponent(() => import('@/views/aisle/balance/component/curUnblance.vue'))

const tableData = ref([]) as any
const showDetailDialog = ref(false)
const apow = ref(null)
const bpow = ref(null)
const detailLoading = ref(false)
const balanceObj = reactive({
  a_reactive_power: 0,
  b_reactive_power: 0,
  a_apparent_power: 0,
  b_apparent_power: 0,
  a_active_power: 0,
  b_active_power: 0,
  pow_reactive_percent: 0,
  pow_apparent_percent: 0,
  pow_active_percent: 0,
  cur_valueA: [],
  cur_valueB: [],
  imbalanceValueA: 0,
  imbalanceValueB: 0,
  colorIndex: 0,
})

const ABarOption = ref<EChartsOption>({
      title: {
        text: 'A路电流饼形图',
        left: 'left',
        textStyle:{
          fontWeight: 'bold'
        }
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
            { value: null, name: 'A相电流', itemStyle: { color: '#E5B849' } },
            { value: null, name: 'B相电流', itemStyle: { color: '#C8603A' } },
            { value: null, name: 'C相电流', itemStyle: { color: '#AD3762' } },
          ]
        }
      ]
    });
const BBarOption = ref<EChartsOption>({
      title: {
        text: 'B路电流饼形图',
        left: 'left',
        textStyle:{
          fontWeight: 'bold'
        }
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
            { value: null, name: 'A相电流', itemStyle: { color: '#E5B849' } },
            { value: null, name: 'B相电流', itemStyle: { color: '#C8603A' } },
            { value: null, name: 'C相电流', itemStyle: { color: '#AD3762' } },
          ]
        }
      ]
    });
const ALineOption = ref<EChartsOption>({
  title: {
    text: 'A路电流趋势',
    left: 'left',
    textStyle:{
      fontWeight: 'bold'
    }
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
  series: [{data:[],type:"line",symbol:"none"}]
})
const BLineOption = ref<EChartsOption>({
  title: {
    text: 'B路电流趋势',
    left: 'left',
    textStyle:{
      fontWeight: 'bold'
    }
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
  series: [{data:[],type:"line",symbol:"none"}]
})

/** 打开弹窗 */
const open = async (data) => {
  toDetail(data)
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

// 详情跳转
const toDetail = async (item) => {
  console.log('跳转详情', item);
  detailLoading.value = true
  showDetailDialog.value = true;
  if(!item?.powActiveA) {
    const res = await IndexApi.getAisleBalancePage({id: item.id})
    if (res.list) {
      tableData.value = res.list
      tableData.value.forEach(obj => {
        obj.rateA = obj?.rateA?.toFixed(0);
        obj.rateB = obj?.rateB?.toFixed(0);
        obj.powApparentTotal = obj?.powApparentTotal?.toFixed(3);
        obj.powActiveTotal = obj?.powActiveTotal?.toFixed(3);
        obj.powReactiveTotal = obj?.powReactiveTotal?.toFixed(3);
        obj.powApparentA = obj?.powApparentA?.toFixed(3);
        obj.powActiveA = obj?.powActiveA?.toFixed(3);
        obj.powReactiveA = obj?.powReactiveA?.toFixed(3);
        obj.powApparentB = obj?.powApparentB?.toFixed(3);
        obj.powActiveB = obj?.powActiveB?.toFixed(3);
        obj.powReactiveB = obj?.powReactiveB?.toFixed(3);
      });
      item = tableData.value[0]
    }
  }
  balanceObj.a_reactive_power=item.powReactiveA;
  balanceObj.b_reactive_power=item.powReactiveB;
  balanceObj.a_apparent_power=item.powApparentA;
  balanceObj.b_apparent_power=item.powApparentB;
  balanceObj.a_active_power=item.powActiveA;
  balanceObj.b_active_power=item.powActiveB;
  bpow.value=1-apow.value;
  apow.value=balanceObj.pow_apparent_percent=isNaN(item.powApparentA/item.powApparentTotal*100)?0:item.powApparentA/item.powApparentTotal*100;
  balanceObj.pow_active_percent=isNaN(item.powActiveA/item.powActiveTotal*100)?0:item.powActiveA/item.powActiveTotal*100;
  balanceObj.pow_reactive_percent=isNaN(item.powReactiveA/item.powReactiveTotal*100)?0:item.powReactiveA/item.powReactiveTotal*100;
  let response=await IndexApi.getBalanceDetail(item.id)
  // console.log(response)
  const cur_valueA=response.curLista
  if(cur_valueA!=null){
    ABarOption.value.series[0].data[0].value=cur_valueA[0].toFixed(2);
    ABarOption.value.series[0].data[1].value=cur_valueA[1].toFixed(2);
    ABarOption.value.series[0].data[2].value=cur_valueA[2].toFixed(2);
  }else{
    ABarOption.value.series[0].data[0].value=0;
    ABarOption.value.series[0].data[1].value=0;
    ABarOption.value.series[0].data[2].value=0;
  }
  const cur_valueB = response.curListb
  if(cur_valueB!=null){
    BBarOption.value.series[0].data[0].value=cur_valueB[0].toFixed(2);
    BBarOption.value.series[0].data[1].value=cur_valueB[1].toFixed(2);
    BBarOption.value.series[0].data[2].value=cur_valueB[2].toFixed(2);
  }else{
    BBarOption.value.series[0].data[0].value=0;
    BBarOption.value.series[0].data[1].value=0;
    BBarOption.value.series[0].data[2].value=0;
  }
  balanceObj.imbalanceValueA=response.curUnbalancea;
  balanceObj.imbalanceValueB=response.curUnbalanceb;
  let timeList=response.list!=null? response.list.map(item =>item.createTime):[];
  ALineOption.value.series[0].data= response.list!=null? response.list.map(item =>item.curAAvgValue.toFixed(2)):[];
  ALineOption.value.xAxis.data=timeList;
  BLineOption.value.series[0].data= response.list!=null? response.list.map(item =>item.curBAvgValue.toFixed(2)):[];
  BLineOption.value.xAxis.data=timeList;
  detailLoading.value = false;
  // console.log('详情跳转', id, router, router.getRoutes())
  // push({path: '/cabinet/cab/balanceDetail', state: { id }})
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
  height: calc(100vh - 215px);
  overflow: auto;
  display: flex;
  flex-wrap: wrap;
  align-content: flex-start;
  .item {
    width: 25%;
    min-width: 290px;
    height: 120px;
    font-size: 12px;
    box-sizing: border-box;
    background-color: #eef4fc;
    border: 5px solid #fff;
    position: relative;
    .progressContainer {
      position: relative;
      left: 0px;
      height: 50px;
      display: flex;
      justify-content: center;
      align-items: center;
      margin-top: 30px;
      .progress {
        position: relative;
        width: 180px;
        display: flex;
        align-items: center;
        font-size: 14px;
        color: #eee;
        box-sizing: border-box;
        position: relative;
        display: flex;
        justify-content: center;
        margin-top: 30px;
        
        .tip {
          width: 180px;
          position: absolute;
          top: -22px;
          left: 0;
          display: flex;
          justify-content: space-between;
          color: #000;
          font-size: 12px;
        }
        .line {
          position: relative;
          bottom: -10px;
          width: 3px;
          height: 25px;
          background-color: #000;
        }
        .left {
          position: relative;
          bottom: -10px;
          text-align: center;
          box-sizing: border-box;
          background-color: #3b8bf5;
          // border-right: 1px solid #000;
        }
        .right {
          position: relative;
          bottom: -10px;
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
      bottom: 5px;
    }
  }
}

.powerContainer {
  display: flex;
  justify-content: space-between;
  flex-wrap: nowrap;
  .power {
    width: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    // padding-left: 50px;
    margin: 20px 0;
    .label {
      position: absolute;
      top: 10px;
      left: 10px;
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


.cardChilc {
  flex: 1;
  height: 100%;
}

.IechartBar {
  width: 100%;
  height: 100%;
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
:deep(.el-dialog) {
  width: 80%;
  margin-top: 1%;
  margin-bottom: 0;
  background-color: #f1f1f1;
}
.tipInDialog{
  position: absolute;
  display: flex;
  width: 400px;
  height: 30px;
  top: 50px;
  .leftTip{
    display: inline-block;
    // width: 150px;
    margin-left: 40px;
    text-align: center;
    font-size: small;
  }
  .rightTip{
    display: inline-block;
    // width: 150px;
    text-align: center;
    font-size: small;
  }
}
.noData{
  position: absolute;
  display: inline-block;
  right: 10px;
  top: 8px;
}
.topCard{
  position: relative;
  height: 135px;
}
.progressInDialog{
  // left: 50px;
  // right: 50px;
  display: inline-block;
  margin-top: 20px;
  width: 100%;
  .left{
    text-align: center;
    vertical-align: middle;
    margin-left: 10%;
    display: inline-block;
    width:100px;
    height: 20px;
    background-color: rgb(59, 139, 245);
  }
  .line{
    vertical-align: middle;
    display: inline-block;
    width: 3px;
    height: 36px;
    background-color: #000;
  }
  .right{
    text-align: center;
    vertical-align: middle;
    display: inline-block;
    width: 100px;
    height: 20px;
    background-color: rgb(248, 111, 19);
  }
}
.powerInDialog{
  width: 400px;
  margin:20px;
  .label{
    position: relative;
    top:-25px;
    font-size: large;
    left: -15px;
  }
}
</style>