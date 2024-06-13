<template>
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
      >
        <template #item="{ element }">
          <div :class="element.targetId ? 'frame active' : 'frame'" :id="'frame' + element.id">
            {{ element.name }}
          </div>
        </template>
      </draggable>
      <div class="portRight">
        <div class="port" v-for="port in portRight" :key="port.id" :id="'portRight' + port.id">{{port.name}}</div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { newInstance, BezierConnector, BrowserJsPlumbInstance } from '@jsplumb/browser-ui'
import { ref, reactive, onMounted  } from 'vue'
import draggable from "vuedraggable";

const frameContainer = ref()
let instance: BrowserJsPlumbInstance | null = null

//拖拽开始的事件
const onStart = (e) => {
  console.log("开始拖拽")
}

//拖拽结束的事件
const onEnd = (e) => {
  // 如果没有进行置换的拖拽直接返回
  if (e.oldIndex == e.newIndex) return false
  console.log('拖拽结束的事件frameList', e, frameList, instance?.connections)
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
    stroke: frameList.length > 10 ? 'rgba(0, 0, 0, 0)' : '#000', // 设置连接线的颜色为黑色
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
    instance?.addEndpoint(portElement, {
      ...addEndpointConfig,
      anchor: 'Right',
    })
    if (!port.targetId) return
    const targetElement = document.getElementById(`frame${port.targetId}`) as Element
    let connect = instance?.connect({
      source: portElement,
      target: targetElement,
      // endpoint: "Rectangle",
      endpoint: { type:"Dot", options:{ radius:2, fillStyle: 'rgba(0, 0, 0, 0)'}},
      anchors: ['Right', 'Left'],
      paintStyle: paintStyleConfig
    })
    targetElement.addEventListener('mouseover', (e) => {
      if (connect)
      instance?.deleteConnection(connect)
      console.log('connect', connect)
      connect = instance?.connect({
        source: portElement,
        target: targetElement,
        // endpoint: "Rectangle",
        endpoint: { type:"Dot", options:{ radius:2, fillStyle: 'rgba(0, 0, 0, 0)'}},
        anchors: ['Right', 'Left'],
        paintStyle: {
          strokeWidth: 2, // 设置连接线的宽度为 2 像素
          stroke: 'red', // 设置连接线的颜色为黑色
          outlineWidth: 13,
          outlineStroke: 'rgba(0, 0, 0, 0)'
        }
      })
      console.log('targetElement', connect)
    })
    targetElement.addEventListener('mouseout', (e) => {
      if (connect)
      {
        instance?.deleteConnection(connect)
      }
    })
    // if (connect && frameList.length > 5) {
    //   connect.hoverPaintStyle = hoverPaintStyleConfig
    // }
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
    const connect = instance?.connect({
      source: targetElement,
      target: portElement,
      // endpoint: "Rectangle",
      anchors: ['Right', 'Left'],
      endpoint: { type:"Dot", options:{ radius:2, fillStyle: 'rgba(0, 0, 0, 0)'}},
      paintStyle: paintStyleConfig
    })
    if (connect && frameList.length > 5) {
      connect.hoverPaintStyle = hoverPaintStyleConfig
    }
  })
}

onMounted(() => {
  initConnect()
})

const portLeft = reactive([
  {
    id: 101,
    name: '端口1',
    targetId: 201,
  },
  {
    id: 102,
    name: '端口2',
    targetId: 202,
  },
  {
    id: 103,
    name: '端口3',
    targetId: 203,
  },
  {
    id: 104,
    name: '端口4',
    targetId: 204,
  },
  {
    id: 105,
    name: '端口1',
    targetId: null,
  },
  {
    id: 106,
    name: '端口2',
    targetId: null,
  },
  {
    id: 107,
    name: '端口3',
    targetId: null,
  },
  {
    id: 108,
    name: '端口4',
    targetId: null,
  },
])
const portRight = reactive([
  {
    id: 301,
    name: '端口1',
    targetId: 201,
  },
  {
    id: 302,
    name: '端口2',
    targetId: 202,
  },
  {
    id: 303,
    name: '端口3',
    targetId: 203,
  },
  {
    id: 304,
    name: '端口4',
    targetId: 204,
  },
])
let frameList = reactive([
  {id: 201,name: '1'},
  {id: 202,name: '2'},
  {id: 203,name: '3'},
  {id: 204,name: '4'},
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
  padding: 0 30px;
  padding-top: 30px;
  border: 1px solid;
  .port {
    width: 50px;
    height: 25px;
    border: 1px solid #000;
    line-height: 25px;
    text-align: center;
    margin-bottom: 15px;
    font-size: 12px;
  }
}
.frameList {
  width: 150px;
  border: 5px solid #415beb;
  box-sizing: border-box;
  margin: 10px 100px;
  .frame {
    padding: 5px 15px;
    background-color: #fff;
    border-bottom: 1px solid #415beb;
  }
  .active {
    background-color: #6fff9a;
  }
}
.ghost {
  border: solid 1px rgb(19, 41, 239);
}
.chosenClass {
  background-color: #f1f1f1;
}
</style>