<template>
  <Echart :height="60" :width="60" :options="echartsOption" />
</template>

<script lang="ts" setup>
import 'echarts-liquidfill'

const {precent} = defineProps({
  precent: {
    type: Number,
    required: true
  },
})
const color = ref('')
console.log('precent', precent)

const judgeColor = () => {
  if (precent == 0) {
    color.value = '#aaa'
  } else if (precent < 30) {
    color.value = '#3bbb00'
  } else if (precent < 60) {
    color.value = '#3b8bf5'
  } else if (precent < 90) {
    color.value = '#ffc402'
  } else {
    color.value = '#fa3333'
  }
}

judgeColor()

const num = precent;

const echartsOption = reactive({
  series: [
    {
      type: 'liquidFill',
      data: [num/100], // 设置水球图的填充比例
      label: {
        fontSize: 12, // 设置字体大小
        fontWeight: 'bold', // 设置字体粗细
        color: precent == 0 ? '#fff' : color.value,
        formatter: (params) => {
          if (params.data == 0) {
            return '空载'
          } else {
            return params.data * 100 + '%'
          }
        }
      },
      radius: '100%',
      amplitude: 1, // 调整波浪的振幅
      outline: {
        show: false // 不显示外圈轮廓线
      },
      color: [color.value], //3b8bf5 // 水的颜色
      backgroundStyle: { // 球的背景色
        color: precent == 0 ? '#aaa' : '#fff'
      }
    }
  ]
})

onUnmounted(() => {
  console.log('onUnmounted******')
})
</script>

<style lang="scss" scoped>

</style>
