<template>
  <Echart :height="60" :width="60" :options="echartsOption" />
</template>

<script lang="ts" setup>
import 'echarts-liquidfill'

const echartsOption = reactive({
  series: [
    {
      type: 'liquidFill',
      data: [0], // 设置水球图的填充比例
      label: {
        fontSize: 12, // 设置字体大小
        fontWeight: 'bold', // 设置字体粗细
        color: '#fff',
        formatter: (params) => {
          console.log('params', params)
          if (params.data == 0) {
            return '未开通'
          } else {
            return params.data
          }
        }
      },
      radius: '100%',
      amplitude: 2, // 调整波浪的振幅
      outline: {
        show: false // 不显示外圈轮廓线
      },

      color: ['#aaa'], //3b8bf5
      backgroundStyle: {
        color: '#aaa'
      }
    }
  ]
})
</script>

<style lang="scss" scoped>

</style>