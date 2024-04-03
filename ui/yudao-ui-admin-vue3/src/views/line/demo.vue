<template>
  <el-card>
    <div ref="wrapper" class="wrapper">
      <div class="box1" ref="sourceElement">插接箱</div>
      <div class="boxList">
        <div class="box2" ref="targetElement1">机柜1</div>
        <div class="box2" ref="targetElement2">机柜2</div>
        <div class="box2" ref="targetElement3">机柜3</div>
      </div>
    </div>
  </el-card>
</template>

<script lang="ts" setup>
import { newInstance, BezierConnector,ready } from '@jsplumb/browser-ui'
import {onMounted, ref} from 'vue'
const wrapper = ref()
const sourceElement = ref()
const targetElement1 = ref()
const targetElement2 = ref()
const targetElement3 = ref()
onMounted(() => {
  ready(() => {
    console.log('wsww', wrapper.value)
    const instance = newInstance({
      container: wrapper.value
    })
    console.log('instance', instance)
    // instance.setDraggable(sourceElement.value, false)
    instance.setDraggable(targetElement1.value, false)
    instance.setDraggable(targetElement2.value, false)
    instance.setDraggable(targetElement3.value, false)
    // instance.addEndpoint(sourceElement.value, {
    //   source: true,
    //   anchor: 'Left',
    //   endpoint: "Dot"
    // })
    // instance.addEndpoint(sourceElement.value, {
    //   source: true,
    //   anchor: { type:"Perimeter", options:{ shape:"Triangle", anchorCount:5, rotation: 180 } },
    //   endpoint: "Dot"
    // })
    instance.addEndpoint(sourceElement.value, {
      source: true,
      anchor: [0.4, 1, 0, 1],
      endpoint: "Dot"
    })
    instance.addEndpoint(sourceElement.value, {
      source: true,
      anchor: [0.5, 1, 0, 1],
      endpoint: "Dot"
    })
    instance.addEndpoint(sourceElement.value, {
      source: true,
      anchor: [0.6, 1, 0, 1],
      endpoint: "Dot"
    })
    // instance.addEndpoint(sourceElement.value, {
    //   source: true,
    //   anchor: 'Bottom',
    //   endpoint: "Dot"
    // })
    // instance.addEndpoint(sourceElement.value, {
    //   source: true,
    //   anchor:'BottomLeft',
    //   endpoint: "Dot"
    // })
    instance.addEndpoint(targetElement1.value, {
      target: true,
      anchor: 'Top',
      endpoint: "Dot"
    })
    instance.addEndpoint(targetElement2.value, {
      target: true,
      anchor: 'Top',
      endpoint: "Dot"
    })
    instance.addEndpoint(targetElement3.value, {
      target: true,
      anchor: 'Top',
      endpoint: "Dot"
    })
    // instance.connect({
    //   source: sourceElement.value,
    //   target: targetElement.value,
    //   anchors:["Right", "Left" ],
    //   endpoint:"Dot",
    //   connector:{
    //       type:BezierConnector.type,
    //       options:{
    //           curviness: 10
    //       }
    //   }
    // })
  })
})
</script>

<style lang="scss" scoped>
.wrapper {
  background-color: #f5f5f5;
  border: 1px solid #000;
  border-radius: 5px;
  height: 400px;
  overflow: hidden;
  position: relative;
  width: 100%;
  box-sizing: border-box;
}
.box1 {
  width: 150px;
  height: 150px;
  background-color: #f0f0f0;
  border: 1px solid #ccc;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 16px;
  position:absolute;
  top: 0px;
  left: calc(50% - 75px);
}
.boxList {
  width: 100%;
  display: flex;
  justify-content: space-around;
  position: absolute;
  top: 200px;
}
.box2 {
  width: 150px;
  height: 150px;
  background-color: #f0f0f0;
  border: 1px solid #ccc;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 16px;
}
</style>