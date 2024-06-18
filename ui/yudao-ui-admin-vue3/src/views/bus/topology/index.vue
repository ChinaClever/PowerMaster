<template>
  <ContentWrap>
    <div class="btns">
      <template v-for="item in btns" :key="item.value">
        <el-button @click="switchBtn(item.value)" type="primary" :plain="chosenBtn != item.value">{{item.name}}</el-button>
      </template>
    </div>
  </ContentWrap>
  <ContentWrap>
  <div class="topologyContainer">
      <div class="loadA">
        <div class="box">
          <div class="title">A路始端箱</div>
          <div class="info" v-for="(item, index) in startingBoxA[btns[chosenBtn].paramBus]" :key="index">
            <div>{{btns[chosenBtn].unitName}}{{letters[index]}}</div>
            <div>{{item.toFixed(btns[chosenBtn].fixNum)}}{{btns[chosenBtn].unit}}</div>
          </div>
        </div>
        <div class="line"></div>
        <div class="boxList">
          <div class="boxItem" v-for="(box, index) in pluginBoxA" :key="index">
            <div class="layer"></div>
            <div class="stage"></div>
            <div class="box">
              <div class="title">A{{(index + 1)}}#插接箱</div>
              <div class="info" v-for="(item, i) in box.box_data.line_item_list[btns[chosenBtn].paramBox]" :key="i">
                <div>{{btns[chosenBtn].unitName}}{{letters[i]}}</div>
                <div>{{item.toFixed(btns[chosenBtn].fixNum)}}{{btns[chosenBtn].unit}}</div>
              </div>
            </div>
          </div>
          <div class="tip">A路</div>
        </div>
      </div>
      <div class="loadB">
        <div class="box boxB" v-if="startingBoxB">
          <div class="title">B路始端箱{{startingBoxB}}</div>
          <div class="info" v-for="(item,index) in startingBoxB[btns[chosenBtn].paramBus]" :key="index">
            <div>{{btns[chosenBtn].unitName}}{{letters[index]}}</div>
            <div>{{item.toFixed(btns[chosenBtn].fixNum)}}{{btns[chosenBtn].unit}}</div>
          </div>
        </div>
        <div class="line"></div>
        <div class="boxList">
          <div class="boxItem" v-for="(item, index) in boxs" :key="index">
            <div class="box">
              <div class="title">{{item.t2+(index + 1)}}#插接箱</div>
              <div class="info">
                <div>Ua</div>
                <div>0.1V</div>
              </div>
              <div class="info">
                <div>Ua</div>
                <div>0.1V</div>
              </div>
              <div class="info">
                <div>Ua</div>
                <div>0.1V</div>
              </div>
            </div>
            <div class="stage"></div>
            <div class="layer"></div>
          </div>
          <div class="tip">B路</div>
        </div>
      </div>
    </div>
  </ContentWrap>
</template>

<script lang="ts" setup>
import { BusTopologyApi } from '@/api/bus/topology'

const boxs = [
  {
    t1: 'A',
    t2: 'B',
  },
  {
    t1: 'A',
    t2: 'B',
  },
  // {
  //   t1: 'A',
  //   t2: 'B',
  // },
  // {
  //   t1: 'A',
  //   t2: 'B',
  // },
  // {
  //   t1: 'A',
  //   t2: 'B',
  // },
  // {
  //   t1: 'A',
  //   t2: 'B',
  // },
  // {
  //   t1: 'A',
  //   t2: 'B',
  // },
  // {
  //   t1: 'A',
  //   t2: 'B',
  // },
  // {
  //   t1: 'A',
  //   t2: 'B',
  // },
  {
    t1: 'A',
    t2: 'B',
  },
  {
    t1: 'A',
    t2: 'B',
  },
  {
    t1: 'A',
    t2: 'B',
  },
  {
    t1: 'A',
    t2: 'B',
  },
  {
    t1: 'A',
    t2: 'B',
  },
  {
    t1: 'A',
    t2: 'B',
  },
]

const btns = [
  {
    name: '电流',
    value: 0,
    unit: 'A',
    unitName: 'I',
    paramBox: 'cur_value',
    paramBus: 'cur_value',
    fixNum: 2,
  },
  {
    name: '电压',
    value: 1,
    unit: 'V',
    unitName: 'U',
    paramBox: 'vol_value',
    paramBus: 'vol_value',
    fixNum: 1,
  },
  {
    name: '有功功率',
    value: 2,
    unit: 'kW',
    unitName: 'P',
    paramBox: 'pow_active',
    paramBus: 'pow_value',
    fixNum: 3,
  },
  {
    name: '无功功率',
    value: 3,
    unit: 'kVar',
    unitName: 'P',
    paramBox: 'pow_reactive',
    paramBus: 'pow_reactive',
    fixNum: 3,
  },
  {
    name: '视在功率',
    value: 4,
    unit: 'kVA',
    unitName: 'P',
    paramBox: 'pow_apparent',
    paramBus: 'pow_apparent',
    fixNum: 3,
  },
  {
    name: '功率因素',
    value: 5,
    unit: '',
    unitName: 'P',
    paramBox: 'power_factor',
    paramBus: 'power_factor',
    fixNum: 2,
  },
  {
    name: '温度',
    value: 6,
    unit: '°C',
    unitName: 'T',
    paramBox: 'tem_value',
    paramBus: 'tem_value',
    fixNum: 2,
  },
]

const letters = ['a', 'b', 'c', 'd']

const chosenBtn = ref(0)
const pluginBoxA = ref([])
const pluginBoxB = ref([])
const startingBoxA = ref([])
const startingBoxB = ref({})

const switchBtn = (value) => {
  chosenBtn.value = value
  console.log('switchBtn', )
}

const getBusData = async() => {
  const res = await BusTopologyApi.getBusTopology({ids: null})
  console.log('res', res)
  if (res.length > 0) {
    startingBoxA.value = res[0].busData ? res[0].busData.bus_data.line_item_list : null
    startingBoxB.value = res[1].busData ? res[1].busData.bus_data.line_item_list : null
    pluginBoxA.value = res[0].boxDataList
    pluginBoxB.value = res[1].boxDataList
    console.log('startingBoxA.value', startingBoxA.value, startingBoxA.value[(btns[chosenBtn.value].paramBox)], pluginBoxA.value[0])
  }
}
getBusData()
</script>

<style scoped lang="scss">
.btns {
  display: flex;
  justify-content: flex-end;
}

.topologyContainer {
  box-sizing: border-box;
  height: calc(100vh - 250px);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  // height: calc(100% - var(--app-content-padding) - var(--app-content-padding) - var(--app-footer-height)) !important;
  overflow: auto;
  padding: 20px 0;
  .loadA,
  .loadB {
    display: flex;
    .box {
      width: 100px;
      height: fit-content;
      border: 1px solid #ddd;
      background-color: #fff;
      flex-shrink: 0;
      .title {
        padding: 15px 13px;
        font-size: 13px;
        font-weight: bold;
        color: #0051ff;
      }
      .info {
        display: flex;
        justify-content: space-between;
        margin-bottom: 10px;
        padding: 0 10px;
        font-size: 12px;
      }
    }
    .line {
      flex-shrink: 0;
      width: 12px;
      height: 30px;
      background-color: red;
      margin-top: 48px;
    }
    .boxList {
      position: relative;
      flex: 1;
      box-sizing: border-box;
      display: flex;
      justify-content: space-between;
      padding: 0 10px 20px 10px;
      border-top: 4px solid red;
      margin-top: 60px;
      .boxItem {
        display: flex;
        flex-direction: column;
        align-items: center;
        margin-right: 15px;
        .layer {
          width: 30px;
          height: 8px;
          background-color: red;
        }
        .stage {
          width: 4px;
          height: 10px;
          background-color: red;
        }
      }
      .tip {
        position: absolute;
        left: 25px;
        top: -35px;
        color: red;
        font-size: 20px;
        font-weight: bold;
      }
    }
  }
  .loadB {
    .boxB {
      margin-top: 100px;
    }
    .line {
      margin-top: 150px;
      background-color: green;
    }
    .boxList {
      margin-top: 0px;
      margin-bottom: 60px;
      border-top: none;
      border-bottom: 4px solid green;
      padding: 0 10px;
      .boxItem {
        justify-content: flex-end;
        .layer {
          background-color: green;
        }
        .stage {
          background-color: green;
        }
      }
      .tip {
        top: 170px;
        color: green;
      }
    }
  }
}
</style>