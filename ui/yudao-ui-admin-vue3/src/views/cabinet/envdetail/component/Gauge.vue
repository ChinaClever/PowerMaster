<template>
  <Echart :height="height" :width="width" :options="echartsOption" />
</template>

<script lang="ts" setup>
import 'echarts'
const prop = defineProps({
  value: {
    type: [Number,String],
    required: true
  },
  height: {
    type: [Number,String],
    default: 60
  },
  width: {
    type: [Number,String],
    default: 60
  },
  titleName :{
    type : [String]
  },
  formatterType : {
    type : [String],
    default: '°C'
  }
})

const formatter = ref() as any;
const lastValue = ref();
const name = ref();

// 设置饼图的选项
const echartsOption = reactive({
  series: [{
      type: "gauge",
      radius: "90%",
      splitNumber: 5,
      axisLine: {
          show: !0,
          roundCap: !0,
          lineStyle: {
              width: 6,
              color: [[1, "#b7c7e8"]]
          }
      },
      axisTick: {
          splitNumber: 5,
          lineStyle: {
              width: 1.5,
              color: "#8c8c8c"
          },
          distance: 8
      },
      splitLine: {
          length: 10,
          lineStyle: {
              width: 2.5,
              color: "#b8c7e8"
          },
          show: !0,
          distance: 8
      },
      axisLabel: {
          color: "#8c8c8c",
          distance: 10,
          show: !0
      },
      title: {
          show: !0,
          offsetCenter: [0, "90%"],
          fontSize: 12,
          fontWeight: 400,
          color: "#585858"
      },
      detail: {
          color: "#585858",
          formatter: "{value}".concat(prop.formatterType),
          fontSize: 12,
          offsetCenter: [0, "65%"],
          fontWeight: 400
      },
      data: [{
          value: lastValue,
          name: name
      }]
  }]
})

watchEffect(()=>{

  formatter.value = prop.formatterType;
  lastValue.value = prop.value;
  name.value = prop.titleName;

})

onUnmounted(() => {
  console.log('onUnmounted******')
})

</script>

<style lang="scss" scoped>

</style>