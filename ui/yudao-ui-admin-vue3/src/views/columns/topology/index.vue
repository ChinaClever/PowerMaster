<template>
  <ContentWrap>
    <div class="btn-main">
      <div class="btns">
        <template v-for="item in btns" :key="item.value">
          <el-button @click="switchBtn(item.value)" type="primary" :plain="chosenBtn != item.value">{{item.name}}</el-button>
        </template>
      </div>
      <div>
        <el-button v-if="!editEnable" @click="editEnable = true" type="primary" plain><Icon icon="ep:edit" class="mr-5px" />编辑</el-button>
        <el-button v-if="editEnable" @click="editEnable = false" type="primary" plain>取消</el-button>
        <el-button v-if="editEnable" @click="handleConfig" type="primary" plain>配置</el-button>
        <el-button v-if="editEnable" @click="handleSubmit" type="primary" plain>保存</el-button>
      </div>
    </div>
  </ContentWrap>
  <ContentWrap>
    <div ref="topologyContainer" class="topologyContainer" style="position: relative;z-index: 1">
      <div class="Container" :style="{alignItems: machineColInfo.pduBar && machineColInfo.barA ? 'unset' : 'center'}">
        <div v-if="machineColInfo.pduBar && machineColInfo.barA" class="Bus">
          <div class="startBus">
            始端箱
          </div>
          <div class="startBus">
            始端箱
          </div>
        </div>
        <div class="main">
          <div v-if="machineColInfo.pduBar && machineColInfo.barA" class="busListContainer">
            <div class="bridge"></div>
            <div class="busList1">
              <div class="plugin-box">
                <div class="box">
                  <div>Ia</div>
                  <div>30</div>
                </div>
                <div class="box">
                  <div>Ib</div>
                  <div>8.1</div>
                </div>
                <div class="box">
                  <div>Ic</div>
                  <div>50</div>
                </div>
                <div class="name">电流</div>
                <div id="plugin-A-1" class="pointA"></div>
                <div id="plugin-B-1" class="pointB"></div>
                <div id="plugin-C-1" class="pointC"></div>
              </div>
              <div class="plugin-box">
                <div class="box">
                  <div>Ia</div>
                  <div>30</div>
                </div>
                <div class="box">
                  <div>Ib</div>
                  <div>40</div>
                </div>
                <div class="box">
                  <div>Ic</div>
                  <div>50</div>
                </div>
                <div class="name">电流</div>
              </div>
              <div class="connector">
                <span class="text">连接器</span>
              </div>
              <div class="template-box">
                <div class="connector">
                  <span class="text">连接器</span>
                </div>
                <div class="T">
                  <div>T(L1)</div>
                  <div>30°C</div>
                </div>
                <div class="T">
                  <div>T(L2)</div>
                  <div>30°C</div>
                </div>
                <div class="T">
                  <div>T(L3)</div>
                  <div>30°C</div>
                </div>
                <div class="T">
                  <div>T(N)</div>
                  <div>30°C</div>
                </div>
              </div>
              <div class="plugin-box">
                <div class="box">
                  <div>Ia</div>
                  <div>30</div>
                </div>
                <div class="box">
                  <div>Ib</div>
                  <div>40</div>
                </div>
                <div class="box">
                  <div>Ic</div>
                  <div>50</div>
                </div>
                <div class="name">电流</div>
              </div>
            </div>
          </div>
          <div v-if="machineColInfo.pduBar && machineColInfo.barA" class="busListContainer">
            <div class="bridge"></div>
            <div class="busList2">
              <div class="plugin-box">
                <div class="box">
                  <div>Ia</div>
                  <div>30</div>
                </div>
                <div class="box">
                  <div>Ib</div>
                  <div>40</div>
                </div>
                <div class="box">
                  <div>Ic</div>
                  <div>50</div>
                </div><div class="name">电流</div>
                <div id="plugin-A-2" class="pointA"></div>
                <div id="plugin-B-2" class="pointB"></div>
                <div id="plugin-C-2" class="pointC"></div>
              </div>
              <div class="plugin-box">
                <div class="box">
                  <div>Ia</div>
                  <div>30</div>
                </div>
                <div class="box">
                  <div>Ib</div>
                  <div>40</div>
                </div>
                <div class="box">
                  <div>Ic</div>
                  <div>50</div>
                </div>
                <div class="name">电流</div>
              </div>
              <div class="connector">
                <span class="text">连接器</span>
              </div>
              <div class="template-box">
                <div class="connector">
                  <span class="text">连接器</span>
                </div>
                <div class="T">
                  <div>T(L1)</div>
                  <div>30°C</div>
                </div>
                <div class="T">
                  <div>T(L2)</div>
                  <div>30°C</div>
                </div>
                <div class="T">
                  <div>T(L3)</div>
                  <div>30°C</div>
                </div>
                <div class="T">
                  <div>T(N)</div>
                  <div>30°C</div>
                </div>
              </div>
              <div class="plugin-box">
                <div class="box">
                  <div>Ia</div>
                  <div>30</div>
                </div>
                <div class="box">
                  <div>Ib</div>
                  <div>40</div>
                </div>
                <div class="box">
                  <div>Ic</div>
                  <div>50</div>
                </div>
                <div class="name">电流</div>
              </div>
            </div>
          </div>
          <div class="cabinetContainer">
            <div class="cabinetList">
              <template v-for="(cabinet,index) in cabinetList" :key="index">
                <div class="cabinet">
                  <div :class="cabinet.id ? 'inner_fill' : 'inner_empty'"></div>
                  <div :id="'cab-A-' + index" class="leftPoint"></div>
                  <div :id="'cab-B-' + index" class="rightPoint"></div>
                  <div class="status">{{cabinet.cabinetName || ('机柜' + index)}}</div>
                </div>
              </template>
              <div class="operateBox">
                <div class="operateIcon" @click.prevent="addMachine">+</div>
                <div class="operateIcon" @click.prevent="deleteMachine">-</div>
              </div>
            </div>
          </div>
        </div>
        <div v-if="machineColInfo.pduBar && machineColInfo.barA" class="Bus">
          <div class="startBus">
            始端箱
          </div>
          <div class="startBus">
            始端箱
          </div>
        </div>
      </div>
    </div>
  </ContentWrap>
  <!-- 添加或修改用户对话框 -->
  <ColForm ref="columnForm" @success="handleFormSave" />
</template>

<script lang="ts" setup>
import { newInstance, BezierConnector, BrowserJsPlumbInstance } from '@jsplumb/browser-ui'
import { MachineColumnApi } from '@/api/cabinet/column'
import ColForm from './component/ColForm.vue'

let instance: BrowserJsPlumbInstance | null = null

const btns = [
  {
    value: 0,
    name: '负荷'
  },
  {
    value: 1,
    name: '电流'
  },
  {
    value: 2,
    name: '电压'
  },
  {
    value: 3,
    name: '功率因素'
  },
  {
    value: 4,
    name: '有功功率'
  },
  {
    value: 5,
    name: '无功功率'
  },
  {
    value: 6,
    name: '视在功率'
  },
  {
    value: 7,
    name: '供电平衡'
  },
  {
    value: 8,
    name: '温度'
  },
  {
    value: 9,
    name: '容量'
  },
  {
    value: 10,
    name: '用能'
  },
]
const topologyContainer = ref()
const chosenBtn = ref(0)
const editEnable = ref(false)
const columnForm = ref()
const machineColInfo = reactive<any>({})
const cabinetList = ref<any>([])
const boxA = ref<any>([])
const boxB = ref<any>([])

const initConnect = () => {
  // 创建实例
  instance = newInstance({
    container: topologyContainer.value
  })
}

const toCreatConnect = () => {
  const sourceEle = document.getElementById('plugin-A-1') as Element
  const targetEle = document.getElementById('cab-A-1') as Element
  const sourceEle1 = document.getElementById('plugin-B-1') as Element
  const targetEle1 = document.getElementById('cab-A-2') as Element
  const sourceEle2 = document.getElementById('plugin-C-1') as Element
  const targetEle2 = document.getElementById('cab-A-3') as Element
  const sourceEle21 = document.getElementById('plugin-A-2') as Element
  const targetEle22 = document.getElementById('cab-B-1') as Element
  const sourceEle23 = document.getElementById('plugin-B-2') as Element
  const targetEle24 = document.getElementById('cab-B-2') as Element
  const sourceEle25 = document.getElementById('plugin-C-2') as Element
  const targetEle26 = document.getElementById('cab-B-3') as Element
  // 进行连线
  instance?.connect({
    source: sourceEle,
    target: targetEle,
    paintStyle: {
      strokeWidth: 1,
      stroke: '#ccc',
      dashstyle: '5 5'
    }
  })
  instance?.connect({
    source: sourceEle1,
    target: targetEle1,
    paintStyle: {
      strokeWidth: 1,
      stroke: '#ccc',
      dashstyle: '5 5'
    }
  })
  instance?.connect({
    source: sourceEle2,
    target: targetEle2,
    paintStyle: {
      strokeWidth: 1,
      stroke: '#ccc',
      dashstyle: '5 5'
    }
  })
  // 进行连线
  instance?.connect({
    source: sourceEle21,
    target: targetEle22,
    paintStyle: {
      strokeWidth: 1,
      stroke: '#bb0000',
      dashstyle: '5 5'
    }
  })
  instance?.connect({
    source: sourceEle23,
    target: targetEle24,
    paintStyle: {
      strokeWidth: 1,
      stroke: '#bb0000',
      dashstyle: '5 5'
    }
  })
  instance?.connect({
    source: sourceEle25,
    target: targetEle26,
    paintStyle: {
      strokeWidth: 1,
      stroke: '#bb0000',
      dashstyle: '5 5'
    }
  })
}

const handleConfig = () => {
  console.log('handleConfig')
  columnForm.value.open()
}

const handleSubmit = () => {
  console.log('handleSubmit')
}

const handleFormSave = (data) => {
  console.log('handleFormSave', data)
  let big = 0
  let small = 0
  let type = 0
  if (data.cjxAmount > data.ljqAmount) {
    big = data.cjxAmount
    small = data.ljqAmount
  } else {
    big = data.ljqAmount
    small = data.cjxAmount
    type = 1
  }
  const diff = (big/(small + 1)).toFixed()
  const arr = [] as any
  for(let i = 0; i < big; i++) {
    arr.push({
      boxName: '',
      type,
    })
  }
  let count = 0
  for(let i = 0; i < small; i++) {
    arr.splice((+diff) * (i+1) + count, 0, {
      boxName: '',
      type: type ? 0 : 1,
    })
    count++
  }
  const boxA = {
    busName: data.nameA,
    devIp: data.ipA,
    path: 'A',
    direction: 0,
    boxList: arr
  }
  const boxB = {
    busName: data.nameB,
    devIp: 'B',
    path: data.pathB,
    direction: 0,
    boxList: arr
  }
}

const getMachineColInfo = async() => {
  const res = await MachineColumnApi.getAisleDetail({id:6})
  Object.assign(machineColInfo, res)
  console.log('getMachineColInfo', res, res.length)
  handleCabinetList(res)
}

const saveMachineBus = async() => {
  const res = await MachineColumnApi.saveAisleBus({
    aisleId: machineColInfo.id,
    barVos: [
      {
        busName: '',
        devIp: '',
        path: 'A',
        direction: 0,
        boxList: []
      }
    ]
  })
  console.log('saveMachineBus', res)
}

const handleCabinetList = (data) => {
  const arr = [] as any
  for (let i=0; i < data.length; i++) {
    arr.push({
      id: '',
      cabinetName: '',
      pduBox: 0,
      powCapacity: '',
    })
  }
  data.cabinetList && data.cabinetList.forEach(item => {
    arr.splice(item.index - 1, 1, item)
  })
  console.log('arr', arr)
  cabinetList.value = arr
}

const addMachine = () => {
  console.log('addMachine')
  cabinetList.value.push({
    id: '',
    cabinetName: '',
    pduBox: 0,
    powCapacity: '',
  })
}

const deleteMachine = () => {
  console.log('deleteMachine')
  cabinetList.value.pop()
}

const switchBtn = (value) => {
  chosenBtn.value = value
  console.log('switchBtn', )
}

getMachineColInfo()

onMounted(() => {
  initConnect()
  toCreatConnect()
})
</script>

<style lang="scss" scoped>
.btn-main {
  display: flex;
  justify-content: space-between;
}
// .btns {
//   display: flex;
//   justify-content: flex-end;
// }
.topologyContainer {
  overflow-x: auto;
  overflow-y: hidden;
}
.Container {
  display: flex;
  // align-items: center;
  padding-bottom: 20px;
  min-height: calc(100vh - 270px);
  .Bus {
    .startBus {
      width: 66px;
      height: 66px;
      border: 1px solid #999;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: silver;
      box-shadow: 0 0 10px silver;
      margin-bottom: 50px;
    }
  }
  .main {
    flex: 1;
    .busListContainer {
      height: 80px;
      margin-bottom: 38px;
      position: relative;
      .bridge {
        height: 12px;
        background-color: rgb(155, 166, 190);
        margin-top: 27px;
      }
      .busList2 {
        position: absolute;
        width: 100%;
        height: 100%;
        left: -50px;
      }
      .busList1,.busList2 {
        
        display: flex;
        justify-content: space-around;
        font-size: 13px;
        .plugin-box {
          position: relative;
          height: fit-content;
          font-size: 12px;
          display: flex;
          border: 1px solid;
          border-top: none;
          padding-top: 20px;
          background-color: #fff;
          .name {
            position: absolute;
            top: 2px;
            left: 18px;
          }
          .box {
            display: flex;
            flex-direction: column;
            align-items: center;
            margin: 0 3px;
          }
          .line {
            width: 5px;
            height: 100px;
            background-color: #000;
          }
          .pointA {
            position: absolute;
            bottom: -8px;
            left: 5px;
            border: 1px solid;
            width: 5px;
            height: 5px;
            border-radius: 50%;
          }
          .pointB {
            position: absolute;
            bottom: -8px;
            left: 28px;
            border: 1px solid;
            width: 5px;
            height: 5px;
            border-radius: 50%;
          }
          .pointC {
            position: absolute;
            bottom: -8px;
            right: 5px;
            border: 1px solid;
            width: 5px;
            height: 5px;
            border-radius: 50%;
          }
        }
        .connector {
          position: relative;
          height: 20px;
          width: 50px;
          border-radius: 2px;
          background-color: #E6E6E6;
          margin-top: -16px;
          .text {
            position: absolute;
            bottom: 24px;
            left: 5px;
          }
        }
        .template-box {
          position: relative;
          width: 75px;
          font-size: 12px;
          border: 1px solid #999;
          border-top: none;
          background-color: rgb(252, 252, 252);
          display: flex;
          flex-wrap: wrap;
          .connector {
            margin: -16px auto 0;
          }
          .T {
            width: 50%;
            box-sizing: border-box;
            display: flex;
            flex-direction: column;
            align-items: center;
          }
        }
      }
    }
  }
}
.cabinetContainer {
  box-sizing: border-box;
  margin-top: 50px;
  .cabinetList {
    width: 100%;
    display: flex;
    justify-content: center;
    .cabinet {
      position: relative;
      // min-width: 110px;
      width: 105px;
      // flex: 1;
      height: 350px;
      box-sizing: border-box;
      border: 2px solid;
      .inner_empty {
        width: 100%;
        height: 100%;
        box-sizing: border-box;
        border: 5px solid #888;
        background-color: #E6E6E6;
      }
      .inner_fill {
        width: 100%;
        height: 100%;
        box-sizing: border-box;
        border: 5px solid #888;
        background-color: #83f8b8;
      }
      .status {
        font-size: 12px;
        line-height: 1;
        color: #000;
        text-align: center;
        margin-top: 10px;
      }
    }
    .leftPoint {
      position: absolute;
      left: 20px;
      top: -8px;
      width: 5px;
      height: 5px;
      border: 1px solid;
      border-radius: 50%;
    }
    .rightPoint {
      position: absolute;
      right: 20px;
      top: -8px;
      width: 5px;
      height: 5px;
      border: 1px solid;
      border-radius: 50%;
    }
  }
  .operateBox {
    width: 20px;
    display: flex;
    flex-direction: column;
    justify-content: space-around;
    cursor: pointer;
    .operateIcon {
      width: 25px;
      height: 25px;
      display: flex;
      box-sizing: border-box;
      justify-content: center;
      align-items: center;
      font-size: 25px;
      border-radius: 50%;
      background-color: greenyellow;
      line-height: 1;
      padding-bottom: 3px;
    }
  }
}
</style>