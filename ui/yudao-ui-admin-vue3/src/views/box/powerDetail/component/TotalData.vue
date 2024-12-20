<template>
  <Echart :height="height" :width="width" :options="echartsOption" />
</template>

<script lang="ts" setup>
import 'echarts'
const prop = defineProps({
  list: {
    type: Object,
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

const series = ref()

const echartsOption = ref({
  title: {
    text: '总数据',
    left: 'center'
  },
  tooltip: {
    trigger: 'item'
  },
  legend: {
    top: '5%',
    left: 'center'
  },
  series: [
    {
      name: '总数据',
      type: 'pie',
      radius: '50%',
      // adjust the start and end angle
      data: [ series ],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }
  ]
});

watchEffect(() => {
  // 直接访问即可，watchEffect会自动跟踪变化
  series.value = prop.list.data;

});
</script>

<style lang="scss" scoped>

</style>