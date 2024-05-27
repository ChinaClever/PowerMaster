<template>
  <ContentWrap>
    <div style="display: flex; justify-content: center;">
      <div ref="frameContainer" class="frameContainer" style="position: relative;">
        <div class="portLeft">
          <div class="port" v-for="port in portLeft" :key="port.id" :id="'portLeft' + port.id">{{port.name}}</div>
        </div>
        <draggable
          class="frameList"
          :list="frameList"
          :itemKey="item => item.id"
          ghost-class="ghost"
          chosen-class="chosenClass"
          animation="100"
          @start="onStart"
          @end.prevent="onEnd"
          @click.right="handleRightClick"
        >
          <template #item="{ element }">
            <div :class="element.target ? 'frame active' : 'frame'" :id="'frame' + element.id">
              {{ element.name }}
            </div>
          </template>
        </draggable>
        <div class="portRight">
          <div class="port" v-for="port in portRight" :key="port.id" :id="'portRight' + port.id">{{port.name}}</div>
        </div>
      </div>
    </div>
  </ContentWrap>
</template>

<script lang="ts" setup>
import { newInstance, BezierConnector, BrowserJsPlumbInstance } from '@jsplumb/browser-ui'
import { string } from 'vue-types';
import draggable from "vuedraggable";
import { CabinetApi } from '@/api/cabinet/info'

const frameContainer = ref()
const maxHiddenNumber = 10
let instance: BrowserJsPlumbInstance | null = null

//拖拽开始的事件
const onStart = (e) => {
  console.log("开始拖拽")
}

//拖拽结束的事件
const onEnd = (e) => {
  // 如果没有进行置换的拖拽直接返回
  if (e.oldIndex == e.newIndex) return false
  // const oldFrameId = frameList[e.oldIndex].id
  // const newFrameId = frameList[e.newIndex].id
  const instances = instance?.connections
  interface Type {
    sourceId: string;
    targetId: string;
  }
  let config: Type[] = []
  instances?.forEach(item => {
    config.push({
      sourceId: item.sourceId,
      targetId: item.targetId,
    })
  })
  // 删除所有连接
  instance?.deleteEveryConnection()
  // 重连
  nextTick(() => {
    config.forEach((item, index) => {
      instance?.connect({
        source: document.getElementById(item.sourceId) as Element,
        target: document.getElementById(item.targetId) as Element,
        anchor: ['Right', 'Left'],
        endpoint: { type:"Dot", options:{ radius:2, fillStyle: 'rgba(0, 0, 0, 0)'}},
        paintStyle: {
          strokeWidth: 2, // 设置连接线的宽度为 2 像素
          stroke: frameList.length > maxHiddenNumber ? 'rgba(0, 0, 0, 0)' : '#000', // 设置连接线的颜色为黑色
          outlineWidth: 13,
          outlineStroke: 'rgba(0, 0, 0, 0)'
        }
      })
    })
  })
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
    stroke: frameList.length > maxHiddenNumber ? 'rgba(0, 0, 0, 0)' : '#000', // 设置连接线的颜色为黑色
    outlineWidth: 13,
    outlineStroke: 'rgba(0, 0, 0, 0)'
  }
  const hoverPaintStyleConfig = {
    ...paintStyleConfig,
    stroke: '#000',
  }
  // 禁止端口之间和机柜之间互相连接
  instance.bind('beforeDrop', connection => connection.sourceId.slice(0, 5) != connection.targetId.slice(0, 5))
  // 机柜创建连接点
  frameList.forEach(frame => {
    if (!frame.target) return
    const frameElement = document.getElementById(`frame${frame.id}`) as Element
    instance?.addEndpoint(frameElement, {
      ...addEndpointConfig,
      anchor: 'Left',
    })
    instance?.addEndpoint(frameElement, {
      ...addEndpointConfig,
      anchor: 'Right',
    })
  })
  // 左侧端口创建连接点并初始连接
  portLeft.forEach(port => {
    const portElement = document.getElementById(`portLeft${port.id}`) as Element
    // 添加瞄点
    instance?.addEndpoint(portElement, {
      ...addEndpointConfig,
      anchor: 'Right',
    })
    if (!port.targetId) return
    const targetElement = document.getElementById(`frame${port.targetId}`) as Element
    // 进行连线
    let connect = instance?.connect({
      source: portElement,
      target: targetElement,
      endpoint: { type:"Dot", options:{ radius:2, fillStyle: 'rgba(0, 0, 0, 0)'}},
      anchors: ['Right', 'Left'],
      paintStyle: paintStyleConfig
    })
    // 监听鼠标移入服务器事件
    targetElement.addEventListener('mouseover', (e) => {
      if (connect)
      instance?.deleteConnection(connect)
      connect = instance?.connect({
        source: portElement,
        target: targetElement,
        endpoint: { type:"Dot", options:{ radius:2, fillStyle: 'rgba(0, 0, 0, 0)'}},
        anchors: ['Right', 'Left'],
        paintStyle: hoverPaintStyleConfig
      })
    })
    // 监听鼠标移出服务器事件
    targetElement.addEventListener('mouseout', (e) => {
      if (connect) instance?.deleteConnection(connect)
    })
  })
  // 右侧端口创建连接点并初始连接
  portRight.forEach(port => {
    const portElement = document.getElementById(`portRight${port.id}`) as Element
    instance?.addEndpoint(portElement, {
      ...addEndpointConfig,
      anchor: 'Left',
    })
    if (!port.targetId) return
    const targetElement = document.getElementById(`frame${port.targetId}`) as Element
    let connect = instance?.connect({
      source: targetElement,
      target: portElement,
      // endpoint: "Rectangle",
      anchors: ['Left', 'Right'],
      endpoint: { type:"Dot", options:{ radius:2, fillStyle: 'rgba(0, 0, 0, 0)'}},
      paintStyle: paintStyleConfig,
    })
    // 监听鼠标移入服务器事件
    targetElement.addEventListener('mouseover', (e) => {
      if (connect) {
        instance?.deleteConnection(connect)
        connect = instance?.connect({
          source: portElement,
          target: targetElement,
          endpoint: { type:"Dot", options:{ radius:2, fillStyle: 'rgba(0, 0, 0, 0)'}},
          anchors: ['Left', 'Right'],
          paintStyle: hoverPaintStyleConfig
        })
      }
    })
    // 监听鼠标移出服务器事件
    targetElement.addEventListener('mouseout', (e) => {
      if (connect) instance?.deleteConnection(connect)
    })
  })
}

const handleRightClick = (e) => {
  e.preventDefault()
  console.log('handleRightClick', e)
}

// const createPortList = (num) => {
//   for (let i = 0; i < num; i++) {

//   }
// }

const getData = async() => {
  const res = await CabinetApi.getCabinetInfoItem({id: 1})
  console.log('res', res)
  // const leftPort = res.outletA
  // const rightPort = res.outletB
}

onMounted(() => {
  initConnect()
  getData()
})

const portLeft = reactive([
  {
    id: 101,
    name: '插座位1',
    targetId: 201,
  },
  {
    id: 102,
    name: '插座位2',
    targetId: 202,
  },
  {
    id: 103,
    name: '插座位3',
    targetId: 203,
  },
  {
    id: 104,
    name: '插座位4',
    targetId: 204,
  },
  {
    id: 105,
    name: '插座位5',
    targetId: null,
  },
  {
    id: 106,
    name: '插座位6',
    targetId: null,
  },
  {
    id: 107,
    name: '插座位7',
    targetId: null,
  },
  {
    id: 108,
    name: '插座位8',
    targetId: null,
  },
  {
    id: 109,
    name: '插座位9',
    targetId: null,
  },
  {
    id: 110,
    name: '插座位10',
    targetId: null,
  },
  {
    id: 111,
    name: '插座位11',
    targetId: null,
  },
  {
    id: 112,
    name: '插座位12',
    targetId: null,
  },
  {
    id: 113,
    name: '插座位13',
    targetId: null,
  },
  {
    id: 114,
    name: '插座位14',
    targetId: null,
  },
  {
    id: 115,
    name: '插座位15',
    targetId: null,
  },
  {
    id: 116,
    name: '插座位16',
    targetId: null,
  },
])
const portRight = reactive([
  {
    id: 301,
    name: '插座位1',
    targetId: 201,
  },
  {
    id: 302,
    name: '插座位2',
    targetId: 202,
  },
  {
    id: 303,
    name: '插座位3',
    targetId: 203,
  },
  {
    id: 304,
    name: '插座位4',
    targetId: 204,
  },
  {
    id: 305,
    name: '插座位5',
    targetId: null,
  },
  {
    id: 306,
    name: '插座位6',
    targetId: null,
  },
  {
    id: 307,
    name: '插座位7',
    targetId: null,
  },
  {
    id: 308,
    name: '插座位8',
    targetId: null,
  },
  {
    id: 309,
    name: '插座位9',
    targetId: null,
  },
  {
    id: 310,
    name: '插座位10',
    targetId: null,
  },
  {
    id: 311,
    name: '插座位11',
    targetId: null,
  },
  {
    id: 312,
    name: '插座位12',
    targetId: null,
  },
  {
    id: 313,
    name: '插座位13',
    targetId: null,
  },
  {
    id: 314,
    name: '插座位14',
    targetId: null,
  },
  {
    id: 315,
    name: '插座位15',
    targetId: null,
  },
  {
    id: 316,
    name: '插座位16',
    targetId: null,
  },
])
let frameList = reactive([
  {id: 201,name: '1', target:[]},
  {id: 202,name: '2', target:[]},
  {id: 203,name: '3', target:[]},
  {id: 204,name: '4', target:[]},
  {id: 2042,name: '5'},
  {id: 2043,name: '6'},
  {id: 2044,name: '7'},
  {id: 2045,name: '8'},
  {id: 2046,name: '9'},
  {id: 2012,name: '10'},
  {id: 2014,name: '11'},
  {id: 2013,name: '12'},
  {id: 2011,name: '13'},
  {id: 2015,name: '14'},
  {id: 2016,name: '15'},
  {id: 2017,name: '16'},
  {id: 2018,name: '17'},
  {id: 2019,name: '18'},
  {id: 2010,name: '19'},
  {id: 20211,name: '20'},
  {id: 20210,name: '21'},
  {id: 20212,name: '22'},
  {id: 20213,name: '23'},
  {id: 20214,name: '24'},
  {id: 20215,name: '25'},
  {id: 20216,name: '26'},
  {id: 20217,name: '27'},
  {id: 20218,name: '28'},
  {id: 20219,name: '29'},
  {id: 202567,name: '30'},
  {id: 2022942,name: '31'},
  {id: 2023242,name: '32'},
  {id: 2022342,name: '33'},
  {id: 202131,name: '34'},
  {id: 202123,name: '35'},
  {id: 2021231,name: '36'},
  {id: 2024235,name: '37'},
  {id: 202364,name: '38'},
  {id: 202634,name: '39'},
  {id: 202135,name: '40'},
  {id: 202124,name: '41'},
  {id: 2021234,name: '42'},
])

frameList.reverse()
// portLeft.reverse()
// portRight.reverse()
</script>

<style lang="scss" scoped>
.frameContainer {
  display: flex;
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
  width: 250px;
  border: 5px solid #90b8df;
  box-sizing: border-box;
  margin: 10px 180px;
  font-size: 12px;
  .frame {
    padding: 3px 10px;
    background-color: #fff;
    border-bottom: 1px solid #fafafa;
  }
  .active {
    background-color: #5298df;
  }
}
.ghost {
  border: solid 1px rgb(19, 41, 239);
}
.chosenClass {
  background-color: #f1f1f1;
}
</style>