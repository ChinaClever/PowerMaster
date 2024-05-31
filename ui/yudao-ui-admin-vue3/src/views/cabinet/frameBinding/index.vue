<template>
  <ContentWrap>
    <div style="display: flex; justify-content: center;">
      <div ref="frameContainer" class="frameContainer" style="position: relative;z-index: 1"  @click.right="handleRightClick">
        <div class="portLeft">
          <div class="port" v-for="port in portLeft" :key="port" :id="'portLeft' + port">插座位{{port}}</div>
        </div>
        <div class="frameList">
          <div class="bottomFrame">
            <div v-for="(frame,index) in frameListInit.length" :key="index" class="frame">{{frameListInit.length - index}}</div>
          </div>
          <div class="topFrame">
            <template v-for="(frame,index) in frameList" :key="index">
              <draggable
                :style="`height: ${frame[0] ? (frame[0].uHeight)*23 : 23}px`"
                class="frame"
                :id="'frame-' + index"
                :list="frame"
                :itemKey="item => item.id"
                :group="frame[0] ? groupFrameFill : groupFrameBlank"
                tag="div"
                animation="100"
                @start="onStart"
                @end.prevent="onEnd"
              >
                <template #item="{ element }">
                  <div v-if="element" class="active" :id="'frame-' + index + '-child'" >
                    {{ element.rackName }}
                  </div>
                </template>
              </draggable>
            </template>
          </div>
        </div>
        <!-- <draggable
          class="frameList"
          :list="frameList"
          :itemKey="item => item.id"
          :group="{ name: 'myGroup', pull: 'clone', put: true }"
          animation="100"
          @start="onStart"
          @end.prevent="onEnd"
        >
          <template #item="{ element }">
            <div :class="element.target ? 'frame active' : 'frame'" :id="'frame' + element.id">
              {{ element.name }}
            </div>
          </template>
        </draggable> -->
        <div class="portRight">
          <div class="port" v-for="port in portRight" :key="port" :id="'portRight' + port">插座位{{port}}</div>
        </div>
        <div class="menu" v-if="operateMenu.show" :style="{left: `${operateMenu.left}`, top: `${operateMenu.top}`}">
          <div class="menu_item" v-if="operateMenu.add" @click="openBindingFrom('add')">新增</div>
          <div class="menu_item" v-if="!operateMenu.add" @click="openBindingFrom('edit')">编辑</div>
          <div class="menu_item" v-if="!operateMenu.add" @click="deleteMachine">删除</div>
        </div>
      </div>
    </div>
  </ContentWrap>
  <BindingFrom  ref="binding" @success="handleFormSuccess" />
</template>

<script lang="ts" setup>
import { newInstance, BezierConnector, BrowserJsPlumbInstance } from '@jsplumb/browser-ui'
import draggable from "vuedraggable";
import { CabinetApi } from '@/api/cabinet/info'
import BindingFrom from './component/bindingFrom.vue'

const binding = ref()
const portLeft = ref<number[]>([])
const portRight = ref<number[]>([])
let frameList = ref([])
let frameListInit = ref<any>([])
const cabinetInfo = ref<any>({})
const groupFrameFill = {
  name: 'FrameFill',
  pull: true, //允许拖出,如果设置 字符串'clone' 表示该组拖出的元素会被克隆
  put: true // 拖入
}
const groupFrameBlank = {
  name: 'FrameBlank',
  pull: false, //允许拖出,如果设置 字符串'clone' 表示该组拖出的元素会被克隆
  put: true, // 拖入
}
const showMenuAdd = ref(true)
const operateMenu = ref({
  left: '0px',
  top: '0px',
  show: false,
  add: false,
  curIndex: 0,
  Uindex: 0,
})
const events = ref<any>([])
const frameContainer = ref()
const maxHiddenNumber = 100
let instance: BrowserJsPlumbInstance | null = null

//拖拽开始的事件
const onStart = (e) => {
  console.log("开始拖拽")
}

//拖拽结束的事件
const onEnd = (e) => {
  console.log('拖拽结束的事件', e)
  // 如果没有进行置换的拖拽直接返回
  if (e.from == e.to) return false
  // const oldFrameId = frameList[e.oldIndex].id
  // const newFrameId = frameList[e.newIndex].id
  // const instances = instance?.connections
  // interface Type {
  //   sourceId: string;
  //   targetId: string;
  // }
  // console.log('instances', instances)
  // let config: Type[] = []
  // instances?.forEach(item => {
  //   config.push({
  //     sourceId: item.sourceId,
  //     targetId: item.targetId,
  //   })
  // })
  // 删除所有连接
  instance?.deleteEveryConnection()
  initConnect()
  // 重连
  // nextTick(() => {
  //   console.log('config', config)
  //   config.forEach((item, index) => {
  //     console.log('document.getElementById(item.sourceId) as Element', document.getElementById(item.sourceId) as Element)
  //     instance?.connect({
  //       source: document.getElementById(item.sourceId) as Element,
  //       target: document.getElementById(item.targetId) as Element,
  //       anchor: ['Right', 'Left'],
  //       endpoint: { type:"Dot", options:{ radius:2, fillStyle: 'rgba(0, 0, 0, 0)'}},
  //       paintStyle: {
  //         strokeWidth: 2, // 设置连接线的宽度为 2 像素
  //         stroke: frameList.length > maxHiddenNumber ? 'rgba(0, 0, 0, 0)' : '#000', // 设置连接线的颜色为黑色
  //         outlineWidth: 13,
  //         outlineStroke: 'rgba(0, 0, 0, 0)'
  //       }
  //     })
  //   })
  // })
}



const initConnect = () => {
  // 创建实例
  instance = newInstance({
    container: frameContainer.value
  })
  // 公共参数配置
  const addEndpointConfig = {
    source: true,
    target: true,
    endpoint: { type: "Dot", options:{ radius:2, fillStyle: 'rgba(0, 0, 0, 0)'}},
  }
  const paintStyleConfig = {
    strokeWidth: 2, // 设置连接线的宽度为 2 像素
    stroke: frameList.value.length > maxHiddenNumber ? 'rgba(0, 0, 0, 0)' : '#000', // 设置连接线的颜色为黑色
    outlineWidth: 13,
    outlineStroke: 'rgba(0, 0, 0, 0)'
  }
  const hoverPaintStyleConfig = {
    ...paintStyleConfig,
    stroke: '#000',
  }
  // 禁止端口之间和机柜之间互相连接
  instance.bind('beforeDrop', connection => {
    console.log('禁止端口之间和机柜之间互相连接', connection)
    return connection.sourceId.slice(0, 5) != connection.targetId.slice(0, 5)
  })
  // 左侧端口创建连接点
  portLeft.value.forEach(port => {
    const portElement = document.getElementById(`portLeft${port}`) as Element
    // 添加瞄点
    instance?.addEndpoint(portElement, {
      ...addEndpointConfig,
      anchor: 'Right',
    })
  })
  // 右侧端口创建连接点
  portRight.value.forEach(port => {
    const portElement = document.getElementById(`portRight${port}`) as Element
    // 添加瞄点
    instance?.addEndpoint(portElement, {
      ...addEndpointConfig,
      anchor: 'Left',
    })
  })
  // 机柜部分
  frameList.value.forEach((fm, index) => {
    if (fm[0]) {
      const frame = fm[0] as any
      const frameElement = document.getElementById(`frame-${index}-child`) as Element
      console.log('frameElement', frameElement)
      const connectInfo = [] as any
      // 创建左侧瞄点
      instance?.addEndpoint(frameElement, {
        ...addEndpointConfig,
        anchor: [0, 0.7, 0, 0],
      })
      // 创建右侧瞄点
      instance?.addEndpoint(frameElement, {
        ...addEndpointConfig,
        anchor: [1.04, 0.7, 0, 0],
      })
      // 创建左侧瞄点 并连接
      if (frame.outletIdA && frame.outletIdA.length > 0) {
        frame.outletIdA.forEach(port => {
          // 创建左侧瞄点
          // instance?.addEndpoint(frameElement, {
          //   ...addEndpointConfig,
          //   anchor: 'Left',
          // })
          const targetElement = document.getElementById(`portLeft${port}`) as Element
          // 进行连线
          let connect = instance?.connect({
            source: frameElement,
            target: targetElement,
            endpoint: { type:"Dot", options:{ radius:2, fillStyle: 'rgba(0, 0, 0, 0)'}},
            anchors: ['Left', 'Right'],
            paintStyle: paintStyleConfig
          })
          connectInfo.push({
            connect,
            element: targetElement
          })
        })
      }
      // 创建右侧瞄点 并连接
      if (frame.outletIdB && frame.outletIdB.length > 0) {
        frame.outletIdB.forEach(port => {
          // 创建右侧瞄点
          // instance?.addEndpoint(frameElement, { 
          //   ...addEndpointConfig,
          //   anchor: 'Right',
          // })
          const targetElement = document.getElementById(`portRight${port}`) as Element
          // 进行连线
          let connect = instance?.connect({
            source: frameElement,
            target: targetElement,
            endpoint: { type:"Dot", options:{ radius:2, fillStyle: 'rgba(0, 0, 0, 0)'}},
            anchors: ['Right', 'Left'],
            paintStyle: paintStyleConfig
          })
          connectInfo.push({
            connect,
            element: targetElement
          })
        })
      }
      // 监听鼠标移入服务器事件
      frameElement.addEventListener('mouseover', (e) => {
        connectInfo.forEach((item, index) => {
          instance?.deleteConnection(item.connect)
          connectInfo[index].connect = instance?.connect({
            source: frameElement,
            target: item.element,
            endpoint: { type:"Dot", options:{ radius:2, fillStyle: 'rgba(0, 0, 0, 0)'}},
            anchors: item.element.id.includes('Left') ? ['Left', 'Right'] : ['Right', 'Left'],
            paintStyle: hoverPaintStyleConfig
          })
        })
      })
      // 监听鼠标移出服务器事件
      frameElement.addEventListener('mouseout', (e) => {
        connectInfo.forEach(item => {
          instance?.deleteConnection(item.connect)
        })
      })
    }
  })
}

const handleRightClick = (e) => {
  e.preventDefault()
  const container = e.currentTarget
  const currentId = e.target.id.split('-')[1]
  const className = e.target.className
  if (className.includes('active') || className == 'frame') {
    const rect = container.getBoundingClientRect()
    const offsetX = e.clientX - Math.ceil(rect.left) + 1
    const offsetY = e.clientY - Math.ceil(rect.top) + 1
    console.log('handleRightClick',e.target, className, currentId, offsetX, offsetY, Math.ceil(rect.left), Math.ceil(rect.top), e.clientX, e.clientY)
    const Uindex = toCountU(currentId)
    operateMenu.value = {
      left: offsetX + 'px',
      top: offsetY + 'px',
      show: true,
      add: className == 'frame',
      curIndex: currentId,
      Uindex
    }
  }
}

const getData = async() => {  
  const res = await CabinetApi.getCabinetInfoItem({id: 1})
  console.log('res', res)
  cabinetInfo.value = res
  const leftPort = [] as number[]
  const rightPort = [] as number[]
  const frames = [] as any
  for(let i = 1; i <= res.outletA; i++) {
    leftPort.push(i)
  }
  for(let i = 1; i <= res.outletB; i++) {
    rightPort.push(i)
  }
  for(let i = 1; i <= res.cabinetHeight; i++) {
    frames.push([])
  }
  frameListInit.value = [...frames]
  if (res.rackIndexList.length > 0) {
    res.rackIndexList.forEach(item => {
      frames.splice(item.uAddress-1, item.uHeight, [{
        id: item.id,
        rackName: item.rackName,
        type: item.type,
        company: item.company,
        uAddress: item.uAddress,
        uHeight: item.uHeight,
        outletIdA: item.outletIdA,
        outletIdB: item.outletIdB,
      }])
    })
  }
  frameList.value = frames.reverse()
  portLeft.value = leftPort.reverse()
  portRight.value = rightPort.reverse()
  console.log('leftPort', frameList.value, frameListInit.value)
  nextTick(() => {
    initConnect()
  })
}

const openBindingFrom = (type) => {
  const index = operateMenu.value.curIndex
  const frameListCopy = frameList.value as any
  console.log('openBindingFrom', frameListCopy, index,frameListCopy.length)
  const data = frameListCopy[index].length > 0 ? frameListCopy[index][0] : {}
  binding.value.open(type, data, operateMenu.value.Uindex)
}


const handleFormSuccess = (data) => {
  console.log('handleFormSuccess', data, operateMenu.value, frameList.value, [...frameList.value])
  const curIndex = operateMenu.value.curIndex
  const frames = [...frameList.value] as any
  const value = frames[curIndex][0] as any
  if (value) { // 如果对应位置有值  只能替换对应的值
    const id = value.id || -1
    const count = data.uHeight - value.uHeight
    if (count == 0) {
      frames.splice(curIndex, 1, {
        id,
        ...data
      })
    } else if (count > 0) {
      const arr = [] as any
      for(let i=0; i< count; i++) {
        arr.push([])
      }
      frames.splice(curIndex, 1 + count, {
        id,
        ...data
      }, ...arr)
    } else if (count < 0) {
      const arr = [] as any
      for(let i=0; i< -count; i++) {
        arr.push([])
      }
      frames.splice(curIndex, 1, {
        id,
        ...data
      }, ...arr)
    }
  } else {
    frames.splice(curIndex - (data.uHeight - 1), data.uHeight, [{
      ...data,
      outletIdA: [],
      outletIdB: [],
    }])
  }
  console.log('frames', frames, JSON.stringify(frames))
  frameList.value = frames
  instance?.deleteEveryConnection()
  nextTick(() => {
    initConnect()
  })
}

// 计算所处的是哪个U位
const toCountU = (index) => {
  let count = 0
  frameList.value.find((item, i) => {
    const target = item[0] as any
    if (target && target.uHeight > 1) {
      count = count + target.uHeight
    } else {
      count++
    }
    if (i == index) return true
  })
  console.log('frameListInit.value.length - count', frameListInit.value.length - count + 1, count)
  const result = frameListInit.value.length - count + 1
  return result
}

//

onMounted(() => {
  // initConnect()
  getData()
  document.addEventListener('mousedown',(event) => {
    const element = event.target as HTMLElement
    if (event.button == 0 && operateMenu.value.show && element.className != 'menu_item') {
      operateMenu.value.show = false
    }
  })
})

onUnmounted(() => {
  document.removeEventListener('mousedown', () => {})
})

</script>

<style lang="scss" scoped>
.frameContainer {
  display: flex;
  align-items: center;
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
}
.portRight,
.portLeft {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-evenly;
  padding: 0 30px;
  .port {
    position: relative;
    width: 55px;
    height: 26px;
    font-size: 12px;
    // border-bottom: 1px solid #000;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    box-sizing: border-box;
    background-color: antiquewhite;
    margin-bottom: 10px;
  }
  .port::before {
    position: absolute;
    top: -50px;
    left: -40px;
    content: '';
    width: 60px;
    height: 60px;
    border-radius: 60px;
    // border: 1px solid #000;
    background-color: #fff;
  }
  .port::after {
    position: absolute;
    top: -50px;
    right: -40px;
    content: '';
    width: 60px;
    height: 60px;
    border-radius: 60px;
    // border: 1px solid #000;
    background-color: #fff;
  }
}
.frameList {
  position: relative;
  width: 250px;
  height: fit-content;
  border: 5px solid #90b8df;
  box-sizing: border-box;
  margin: 0 200px;
  font-size: 12px;
  .frame {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 23px;
    box-sizing: border-box;
    background-color: transparent;
    border-bottom: 1px solid #eaeaea;
    .active {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #5298df;
    }
  }
  .topFrame {
    width: 240px;
    box-sizing: border-box;
    position: absolute;
    top: 0;
  }
  .bottomFrame {
    .frame {
      width: 240px;
      background-color: transparent;
      justify-content: flex-start;
      border-bottom: none;
    }
    // height: 23px;
    // box-sizing: border-box;
    // background-color: #fff;
    // border-bottom: 1px solid #fafafa;
  }
}
.ghost {
  border: solid 1px rgb(19, 41, 239);
}
.chosenClass {
  background-color: #f1f1f1;
}
</style>