<template>
  <!-- <ContentWrap>
    <div class="btn-main">
      <div class="btns">
        <template v-for="item in btns" :key="item.value">
          <el-button @click="switchBtn(item.value)" type="primary" :plain="chosenBtn != item.value">{{item.name}}</el-button>
        </template>
      </div>
      <div>
        <el-button v-if="!editEnable" @click="editEnable = true" type="primary" plain><Icon icon="ep:edit" class="mr-5px" />编辑</el-button>
        <el-button v-if="editEnable" @click="handleCancel" type="primary" plain>取消</el-button>
        <el-button v-if="editEnable" @click="handleConfig" type="primary" plain>配置</el-button>
        <el-button v-if="editEnable" @click="handleSubmit" type="primary" plain>保存</el-button>
      </div>
    </div>
  </ContentWrap> -->
  <ContentWrap class="topologyContentWrap">
    <div ref="topologyContainer" class="topologyContainer" style="position: relative;z-index: 1;">
      <div class="Container" :style="{alignItems: machineColInfo.pduBar && machineColInfo.barA ? 'unset' : 'center'}">
        <div v-if="machineColInfo.pduBar && machineColInfo.barA" class="Bus">
          <div class="startBus" :style="{opacity: machineColInfo.barA.direction ? 0 : 1}">
            始端箱
          </div>
          <div class="startBus" v-if="!machineColInfo.barB.direction">
            始端箱
          </div>
        </div>
        <div class="main">
          <div>
            <div v-if="machineColInfo.pduBar && machineColInfo.barA" class="busListContainer">
            <div class="bridge"></div>
            <div class="busList1">
              <template v-for="(bus, index) in machineColInfo.barA.boxList" :key="index">
                <!-- 插接箱 -->
                <div v-if="bus.type == 0" class="plugin-box">
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
                  <div :id="bus.boxName + '_A-1'" class="pointA"></div>
                  <div :id="bus.boxName + '_A-2'" class="pointB"></div>
                  <div :id="bus.boxName + '_A-3'" class="pointC"></div>
                </div>
                <!-- 连接器 -->
                <div v-else class="template-box">
                  <div class="connector">
                    <span class="text">连接器</span>
                  </div>
                  <div class="Tbox">
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
                </div>
              </template>
            </div>
          </div>
          <div v-if="machineColInfo.pduBar && machineColInfo.barB" class="busListContainer">
            <div class="bridge"></div>
            <div class="busList2">
              <template v-for="(bus, index) in machineColInfo.barA.boxList" :key="index">
                <!-- 插接箱 -->
                <div v-if="bus.type == 0" class="plugin-box">
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
                  <div :id="bus.boxName + '_B-1'" class="pointA"></div>
                  <div :id="bus.boxName + '_B-2'" class="pointB"></div>
                  <div :id="bus.boxName + '_B-3'" class="pointC"></div>
                </div>
                <!-- 连接器 -->
                <div v-else class="template-box">
                  <div class="connector">
                    <span class="text">连接器</span>
                  </div>
                  <div class="Tbox">
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
                </div>
              </template>
            </div>
          </div>
          </div>
          <div class="cabinetContainer">
            <div class="cabinetList" v-if="cabinetList && cabinetList.length">
              <template v-for="(cabinet,index) in cabinetList" :key="index">
                <div class="cabinetBox">
                  <div class="point">
                    <div v-if="cabinet.cabinetName" :id="'cab-A-' + index" class="leftPoint"></div>
                    <div v-if="cabinet.cabinetName" :id="'cab-B-' + index" class="rightPoint"></div>
                  </div>
                  <div class="cabinet">
                    <div :class="cabinet.cabinetName ? 'inner_fill' : 'inner_empty'" :id="'cabinet-' + index"></div>
                  </div>
                  <div class="status">{{cabinet.cabinetName || ''}}</div>
                </div>
              </template>
            </div>
          </div>
        </div>
        <div v-if="machineColInfo.pduBar && machineColInfo.barA" class="Bus">
          <div class="startBus" :style="{opacity: machineColInfo.barA.direction}">
            始端箱
          </div>
          <div class="startBus" v-if="machineColInfo.barB.direction">
            始端箱
          </div>
        </div>
      </div>
      <div class="btns">
        <template v-for="item in btns" :key="item.value">
          <el-button @click="switchBtn(item.value)" type="primary" :plain="chosenBtn != item.value">{{item.name}}</el-button>
        </template>
      </div>
      <div class="mask" @click.prevent="" @dblclick="handleJump"></div>
    </div>
  </ContentWrap>
</template>

<script lang="ts" setup>
import { newInstance, BezierConnector, BrowserJsPlumbInstance, JsPlumbDefaults } from '@jsplumb/browser-ui'
import { MachineColumnApi } from '@/api/cabinet/column'
import { CabinetApi } from '@/api/cabinet/info'
import { ElMessage, ElMessageBox } from 'element-plus'

const message = useMessage()
const {push} = useRouter()
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
const machineColInfo = reactive<any>({})
const cabinetList = ref<any>([])
const busListA = ref<any>([])
const busListB = ref<any>([])
const operateMenu = ref({  // 操作菜单
  left: '0px',
  top: '0px',
  show: false,
  add: false,
  curIndex: 0,
})
// 连接初始化准备
const initConnect = () => {
  // 创建实例
  instance = newInstance({
    container: topologyContainer.value
  })
  // 监听连接
  instance.bind('beforeDrop', ({connection}) => {
    console.log('connection', connection)
    const sourceId = connection.source.id
    const targetId = connection.target.id
    const cabId = sourceId.includes('cab') ? sourceId : targetId // 机柜id
    const cabIndex = cabId.split('-')[2]  // 机柜下标
    const cabRoad = cabId.split('-')[1] // 机柜AB路
    const pluginId = sourceId.includes('plugin') ? sourceId : targetId // 插接箱id
    const pluginName = pluginId.split('_')[0] // 插接箱名字
    const pluginOutLet = pluginId.split('-')[2] // 插接箱输出位
    // 机柜与机柜 插接箱与插接箱之间不能相互连接   机柜A路只能连母线A路 机柜B路只能连母线B路
    if ((cabId.split('-')[0] ==  pluginId.split('-')[0]) ||  (cabRoad == 'A' && pluginId.includes('B')) || (cabRoad == 'B' && pluginId.includes('A'))) {
      return false
    }
    cabinetList.value[cabIndex][`boxOutletId${cabRoad}`] = +pluginOutLet
    cabinetList.value[cabIndex][`boxName${cabRoad}`] = 1 + Number(pluginName.split('-')[1])
    console.log('监听连接', connection, cabId, pluginId, cabinetList.value,)
    // 处理手动连接的样式
    if (cabRoad == 'A') {
      connection.paintStyle = {
        strokeWidth: 1,
        stroke: "#ccc",
        dashstyle: "5 5"
      }
    } else if (cabRoad == 'B') {
      connection.paintStyle = {
        strokeWidth: 1,
        stroke: "#bb0000",
        dashstyle: "5 5"
      }
    }
    return true
  })
  // 监听连接断开
  instance.bind('beforeDetach', function(connection) {
    console.log('监听连接断开', connection, connection.sourceId, connection.source)
    if (connection.suspendedElement) { // 用户手动断开连接
      const targetId = connection.target.id.includes('cab') ? connection.target.id : connection.source.id
      const cabRoad = targetId.split('-')[1]
      const index = targetId.split('-')[2]
      cabinetList.value[index][`boxOutletId${cabRoad}`] = ''
      cabinetList.value[index][`boxName${cabRoad}`] = ''
    }
    // 如果返回 false，则连接断开操作会被取消
    return true
  })
  
}

const toCreatConnect = () => {
  if (cabinetList.value && cabinetList.value.length && machineColInfo.barA) {
    console.log('toCreatConnect', cabinetList.value, machineColInfo)
    nextTick(() => {
      machineColInfo.barA.boxList.forEach(item => {
        if (item.type) return
        const boxElementA = document.getElementById(item.boxName + '_A-1') as Element
        const boxElementB = document.getElementById(item.boxName + '_A-2') as Element
        const boxElementC = document.getElementById(item.boxName + '_A-3') as Element
        console.log('boxElementA', boxElementA, item.boxName)
        // 删除瞄点
        instance?.removeAllEndpoints(boxElementA)
        instance?.removeAllEndpoints(boxElementB)
        instance?.removeAllEndpoints(boxElementC)
        // 添加瞄点
        instance?.addEndpoint(boxElementA, {
          source: true,
          target: true,
          endpoint: 'Dot'
        })
        instance?.addEndpoint(boxElementB, {
          source: true,
          target: true,
          endpoint: 'Dot'
        })
        instance?.addEndpoint(boxElementC, {
          source: true,
          target: true,
          endpoint: 'Dot'
        })
        // 更新瞄点
        instance?.revalidate(boxElementA)
        instance?.revalidate(boxElementB)
        instance?.revalidate(boxElementC)
      })
      machineColInfo.barB.boxList.forEach(item => {
        if (item.type) return
        const boxElementA = document.getElementById(item.boxName + '_B-1') as Element
        const boxElementB = document.getElementById(item.boxName + '_B-2') as Element
        const boxElementC = document.getElementById(item.boxName + '_B-3') as Element
        console.log('boxElementA', boxElementA, item.boxName)
        instance?.removeAllEndpoints(boxElementA)
        instance?.removeAllEndpoints(boxElementB)
        instance?.removeAllEndpoints(boxElementC)
        instance?.deleteEndpoint(item.boxName + '_B-1')
        instance?.deleteEndpoint(item.boxName + '_B-2')
        instance?.deleteEndpoint(item.boxName + '_B-3')
        // 添加瞄点
        instance?.addEndpoint(boxElementA, {
          source: true,
          target: true,
          endpoint: 'Dot'
        })
        instance?.addEndpoint(boxElementB, {
          source: true,
          target: true,
          endpoint: 'Dot'
        })
        instance?.addEndpoint(boxElementC, {
          source: true,
          target: true,
          endpoint: 'Dot'
        })
        instance?.revalidate(boxElementA)
        instance?.revalidate(boxElementB)
        instance?.revalidate(boxElementC)
      })
      cabinetList.value.forEach((item, index) => {
        if (!item.cabinetName) return
        addCabinetAnchor(index, item)
      })
    })
  }
}
// 更新插接箱瞄点
const updatePluginAnchor = () => {
  if (cabinetList.value && cabinetList.value.length && machineColInfo.barA) {
    machineColInfo.barA.boxList.forEach(item => {
      if (item.type) return
      const boxElementA = document.getElementById(item.boxName + '_A-1') as Element
      const boxElementB = document.getElementById(item.boxName + '_A-2') as Element
      const boxElementC = document.getElementById(item.boxName + '_A-3') as Element
      console.log('更新插接箱瞄点boxElementA', boxElementA, item, machineColInfo.barA)
      // 更新瞄点
      instance?.revalidate(boxElementA)
      instance?.revalidate(boxElementB)
      instance?.revalidate(boxElementC)
    })
    machineColInfo.barB.boxList.forEach(item => {
      const boxElementA = document.getElementById(item.boxName + '_B-1') as Element
      const boxElementB = document.getElementById(item.boxName + '_B-2') as Element
      const boxElementC = document.getElementById(item.boxName + '_B-3') as Element
      // 更新瞄点
      instance?.revalidate(boxElementA)
      instance?.revalidate(boxElementB)
      instance?.revalidate(boxElementC)
    })
  }
}
// 给某个机柜加瞄点 并进行连接
const addCabinetAnchor = (index, data = {} as any) => {
  const cabElementA = document.getElementById('cab-A-' + index) as Element
  const cabElementB = document.getElementById('cab-B-' + index) as Element
  console.log('cabElementB', cabElementB, cabElementA, data)
  instance?.removeAllEndpoints(cabElementA)
  instance?.removeAllEndpoints(cabElementB)
  // 添加瞄点
  if (!data.boxNameA || !data.boxOutletIdA) instance?.addEndpoint(cabElementA, {
    source: true,
    target: true,
    endpoint: 'Dot'
  })
  if (!data.boxNameB || !data.boxOutletIdB) instance?.addEndpoint(cabElementB, {
    source: true,
    target: true,
    endpoint: 'Dot'
  })
  if (data.boxNameA && data.boxOutletIdA) { // A路有连接
    const source = document.getElementById('cab-A-' + index) as Element
    const target = document.getElementById(`plugin-${data.boxNameA-1}_A-${data.boxOutletIdA}`)  as Element
    console.log('target', source, target)
    instance?.connect({
      source,
      target,
      paintStyle: {
        strokeWidth: 1,
        stroke: '#ccc',
        dashstyle: '5 5'
      }
    })
  }
  if (data.boxNameB && data.boxOutletIdB) { // B路有连接
    const source = document.getElementById('cab-B-' + index) as Element
    const target = document.getElementById(`plugin-${data.boxNameB-1}_B-${data.boxOutletIdB}`)  as Element
    console.log('target---', source, target)
    instance?.connect({
      source,
      target,
      paintStyle: {
        strokeWidth: 1,
        stroke: '#bb0000',
        dashstyle: '5 5'
      }
    })
  }
  instance?.revalidate(cabElementA)
  instance?.revalidate(cabElementB)
}
// 更新机柜和插接箱的连接 
const updateCabinetConnect = () => {
  cabinetList.value.forEach((item, index) => {
    if (!item.cabinetName || !machineColInfo.barA) return
    nextTick(() => {
      console.log('更新机柜的连接')
      const cabElementA = document.getElementById('cab-A-' + index) as Element
      const cabElementB = document.getElementById('cab-B-' + index) as Element
      instance?.revalidate(cabElementA)
      instance?.revalidate(cabElementB)
      updatePluginAnchor()
    })
  })
  
}
// 接口获取柜列信息
const getMachineColInfo = async() => {
  const res = await MachineColumnApi.getAisleDetail({id:6})
  Object.assign(machineColInfo, res)
  console.log('getMachineColInfo', res, res.length)
  handleCabinetList(res)
}
// 处理机柜列表
const handleCabinetList = (data) => {
  const arr = [] as any
  for (let i=0; i < data.length; i++) {
    arr.push({})
  }
  data.cabinetList && data.cabinetList.forEach(item => {
    arr.splice(item.index - 1, 1, item)
  })
  console.log('arr', arr)
  cabinetList.value = arr
  toCreatConnect()
}
//
const switchBtn = (value) => {
  chosenBtn.value = value
  console.log('switchBtn')
}
const handleJump = () => {
  push({path: '/aisle/topology', state: {id: 6}})
}

getMachineColInfo()

window.addEventListener('resize', function() {
  console.log('resize')
  toCreatConnect()
})

onMounted(() => {
  initConnect()
  document.addEventListener('mouseup',(event) => {
    const element = event.target as HTMLElement
    if (event.button == 0 && operateMenu.value.show && element.className != 'menu_item') {
      operateMenu.value.show = false
    }
  })
})
</script>

<style lang="scss" scoped>
.mask {
  width: 100%;
  height: calc(100% - 32px);
  position: absolute;
  top: 0;
  z-index: 999;
}
.btns {
  width: 100%;
  display: flex;
  justify-content: center;
}
:deep(.el-card__body) {
  box-sizing: border-box;
  height: 100%;
  // overflow-x: auto;
  // overflow-y: hidden;
}
:deep(.el-card__body > div) {
  box-sizing: border-box;
  height: 100%;
}
.topologyContentWrap {
  box-sizing: border-box;
  height: 100%;
  // overflow-x: auto;
  // overflow-y: hidden;
}
.Container {
  height: calc(100% - 32px);
  display: flex;
  // align-items: center;
  padding-bottom: 20px;
  min-height: calc(100vh - 270px);
  .Bus {
    width: 66px;
    .startBus {
      width: 66px;
      height: 66px;
      box-sizing: border-box;
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
    height: 100%;
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
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
        box-sizing: border-box;
        position: absolute;
        width: 100%;
        height: 100%;
        // left: -60px;
      }
      .busList1,.busList2 {
        padding: 0 40px;
        display: flex;
        justify-content: space-between;
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
          // margin: 0 39px;
          .connector {
            margin: -16px auto 0;
          }
          .Tbox {
            width: 75px;
            position: absolute;
            top: 0;
            border: 1px solid #999;
            border-top: none;
            background-color: rgb(252, 252, 252);
            display: flex;
            flex-wrap: wrap;
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
  width: 100%;
  align-self: flex-end;
  box-sizing: border-box;
  margin-top: 50px;
  position: relative;
  .menu {
    box-sizing: border-box;
    position: absolute;
    padding: 10px 0;
    background-color: #fff;
    box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
    z-index: 999;
    .menu_item {
      width: 70px;
      height: 30px;
      padding: 0 10px;
      box-sizing: border-box;
      display: flex;
      justify-content: center;
      align-items: center;
    }
    .menu_item:hover {
      background-color: rgb(231, 245, 255);
      color: rgb(82, 177, 255);
    }
  }
  .cabinetList {
    width: 100%;
    display: flex;
    justify-content: center;
    .cabinetBox {
      flex: 1;
      .point {
        height: 7px;
        display: flex;
        justify-content: space-around;
        .leftPoint {
          width: 5px;
          height: 5px;
          border: 1px solid;
          border-radius: 50%;
        }
        .rightPoint {
          width: 5px;
          height: 5px;
          border: 1px solid;
          border-radius: 50%;
        }
      }
      .cabinet {
        width: 100%;
        height: 350px;
        max-width: 105px;
        max-height: 350px;
        box-sizing: border-box;
        border: 2px solid;
      }
      .inner_empty {
        width: 100%;
        height: 100%;
        box-sizing: border-box;
        border: 5px solid #888;
        background-color: #f9f9f9;
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