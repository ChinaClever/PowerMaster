<template>
  <template v-for="btn in btns" :key="btn.value">
    <div class="name" v-if="chosenBtn == btn.value">{{btn.unitName}}</div>
  </template>
  <div class="boxList">
    <template v-if="chosenBtn == 0 && pluginData.lineLoadRate">
      <div class="box" v-for="(line, boxIndex) in pluginData.lineLoadRate" :key="boxIndex">
        <div v-if="boxIndex == 0">A</div>
        <div v-if="boxIndex == 1">B</div>
        <div v-if="boxIndex == 2">C</div>
        <div>{{line.toFixed(0)}}%</div>
      </div>
    </template>
    <template v-if="chosenBtn == 1 && pluginData.lineCur">
      <div class="box" v-for="(line, boxIndex) in pluginData.lineCur" :key="boxIndex">
        <div v-if="boxIndex == 0">A</div>
        <div v-if="boxIndex == 1">B</div>
        <div v-if="boxIndex == 2">C</div>
        <div>{{line.toFixed(2)}}</div>
      </div>
    </template>
    <template v-if="chosenBtn == 2 && pluginData.lineVol">
      <div class="box" v-for="(line, boxIndex) in pluginData.lineVol" :key="boxIndex">
        <div v-if="boxIndex == 0">A</div>
        <div v-if="boxIndex == 1">B</div>
        <div v-if="boxIndex == 2">C</div>
        <div>{{line.toFixed(1)}}</div>
      </div>
    </template>
    <template v-if="chosenBtn == 3 && pluginData.powerFactor">
      <div class="box" v-for="(line, boxIndex) in pluginData.powerFactor" :key="boxIndex">
        <div>Pf{{boxIndex + 1}}</div>
        <div>{{line.toFixed(2)}}</div>
      </div>
    </template>
    <template v-if="chosenBtn == 4 && pluginData.powActive">
      <div class="box" v-for="(line, boxIndex) in pluginData.powActive" :key="boxIndex">
        <div>P{{boxIndex + 1}}</div>
        <div>{{line.toFixed(1)}}</div>
      </div>
    </template>
    <template v-if="chosenBtn == 7 && pluginData.powApparent">
      <div class="box" v-for="(line, boxIndex) in pluginData.powApparent" :key="boxIndex">
        <div>P{{boxIndex + 1}}</div>
        <div>{{line.toFixed(1)}}</div>
      </div>
    </template>
    <template v-if="chosenBtn == 8 && pluginData.temData">
      <div class="box" v-for="(line, boxIndex) in pluginData.temData" :key="boxIndex">
        <div v-if="boxIndex == 0">Ta</div>
        <div v-if="boxIndex == 1">Tb</div>
        <div v-if="boxIndex == 2">Tc</div>
        <div v-if="boxIndex == 3">Tn</div>
        <div>{{line}}</div>
      </div>
    </template>
    <template v-if="chosenBtn == 10 && pluginData.yesterdayEq">
      <div class="box" v-for="(line, boxIndex) in pluginData.yesterdayEq" :key="boxIndex">
        <div>E{{boxIndex + 1}}</div>
        <div>{{line.toFixed(1)}}</div>
      </div>
    </template>
  </div>
  
</template>

<script lang="ts" setup>

const {chosenBtn, pluginData, btns} = defineProps({
  chosenBtn: {
    type: Number || String
  },
  pluginData: {
    type: Object
  },
  btns: {
    type: Array
  },
})
console.log('chosenBtn', chosenBtn, pluginData)
</script>

<style lang="scss" scoped>
.name {
  width: 100%;
  padding: 3px 0;
  display: flex;
  align-items: center;
  justify-content: center;
}
.boxList {
  min-width: 150px;
  min-height: 32px;
  display: flex;
  justify-content: center;
  .box {
    display: flex;
    flex: 1;
    flex-direction: column;
    align-items: center;
    margin: 0 3px;
  }
}
</style>