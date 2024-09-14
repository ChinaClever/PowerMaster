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

// debugger
// // 设置饼图的选项
// const echartsOption = ref({
//   radar: { indicator: prop.list.nameAndMax },
//   series: [
//     { name: '服务器IT总视在功率',
//      type: 'radar', 
//      label: { 
//       show: true,
//        position: 'top' } ,
//        data: [
//          { value: prop.list.value, name: '总视在功率' }, 
//         ] }
//   ]
// })
    // 设置雷达图的选项
    const echartsOption = ref({
      radar: {
        indicator: prop.list.nameAndMax,
      },
      series: [
        {
          name: '服务器IT总视在功率',
          type: 'radar', // 确保类型为 'radar'
          label: {
            show: true,
            position: 'inside', // 更改为合适的标签位置
          },
          data: [
            { value: prop.list.value, name: '总视在功率' },
          ],
        },
      ],
    });



onUnmounted(() => {
  console.log('onUnmounted******')
})

</script>

<style lang="scss" scoped>

</style>