<template>
  <Echart :height="height" :width="width" :options="echartsOption" />
</template>

<script lang="ts" setup>
import 'echarts-liquidfill'

const props = defineProps({
  precent: {
    type: Number,
    required: true
  },
  height: {
    type: Number,
    default: 60
  },
  width: {
    type: Number,
    default: 60
  }
})
const color = ref('');
const echartsOption = reactive<any>({});
//console.log('props.precent',Math.round(props.precent));

const judgeColor = () => {
  if (props.precent == 0) {
    color.value = '#aaa'
  } else if (props.precent < 30) {
    color.value = '#3bbb00'
  } else if (props.precent < 60) {
    color.value = '#3b8bf5'
  } else if (props.precent < 90) {
    color.value = '#ffc402'
  } else {
    color.value = '#fa3333'
  }
}

watch(() => props.precent,(val) => {
  console.log('props.precent val', val)
  judgeColor()
  echartsOption.series = [
    {
      type: 'liquidFill',
      data: [Math.round(val) / 100], // 设置水球图的填充比例
      label: {
        fontSize: 12, // 设置字体大小
        fontWeight: 'bold', // 设置字体粗细
        color: props.precent == 0 ? '#fff' : color.value,
        formatter: (params) => {
          if (params.data == 0) {
            return '未绑定'
          } else {
            return Math.round(val) + '%'
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
        color: Math.round(val) == 0 ? '#aaa' : '#fff'
      }
    }
  ]
},{immediate:true})

</script>

<style lang="scss" scoped>

</style>
