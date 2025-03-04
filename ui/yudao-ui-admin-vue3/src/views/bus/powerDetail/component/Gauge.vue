<template>
  <Echart :height="height" :width="width" :options="echartsOption" />
</template>

<script lang="ts" setup>
import 'echarts'
import { reactive, watch, defineProps, onUnmounted } from 'vue';
const props = defineProps({
  loadFactor: {
    type: Number,
    required: true
  },
  height: {
    type: [Number,String],
    default: 60
  },
  width: {
    type: [Number,String],
    default: 60
  }
})

const loadFactorRound = Math.round(props.loadFactor);

// 设置饼图的选项
const echartsOption = reactive({
  tooltip: {
      formatter: "{a} <br/>{c} {b}"
  },
  series: [{
      type: "gauge",
      z: 3,
      center: ["50%", "50%"],
      min: 0,
      max: 100,
      splitNumber: 5,
      radius: "95%",
      pointer: {
          itemStyle: {
              color: "auto"
          }
      },
      axisLine: {
          lineStyle: {
              width: 10,
              color: [[.4, "#1e90ff"], [.7, "lime"], [1, "#C23531"]]
          }
      },
      axisTick: {
          length: 10,
          lineStyle: {
              color: "#000"
          }
      },
      splitLine: {
          length: 10,
          lineStyle: {
              color: "auto"
          }
      },
      title: {
          fontWeight: "normal",
          fontSize: 16,
          offsetCenter: [0, "83%"],
          color: "#838385"
      },
      detail: {
          formatter: function(t) {
              return "NaN" !== t.toString() && void 0 !== t.toString() ? t : "--"
          },
          shadowColor: "#fff",
          fontWeight: "normal",
          color: "#838385",
          fontSize: 30,
          offsetCenter: [0, "50%"]
      },
      data: [{
          value: loadFactorRound,
          name:  "负载率(%)"
      }]
  }]
})
onUnmounted(() => {
  console.log('onUnmounted******')
})

watch(() => props.loadFactor, (newVal) => {
  echartsOption.series[0].data[0].value = newVal;
});

</script>

<style lang="scss" scoped>

</style>