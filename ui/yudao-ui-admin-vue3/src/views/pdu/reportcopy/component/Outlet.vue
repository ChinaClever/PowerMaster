<template>
  <Echart :height="height" :width="width" :options="echartsOption" />
</template>

<script lang="ts" setup>
import * as echarts from 'echarts'
import { ref, defineProps, watchEffect } from 'vue'

const prop = defineProps({
  list: {
    type: Array,
    required: true,
    default: () => []
  },
  height: {
    type: [Number, String],
    default: '300px'  // 缩小默认高度
  },
  width: {
    type: [Number, String],
    default: '100%'
  },
  color: {
    type: String,
    default: '#00778c'
  }
})

// 设置 ECharts 配置
const echartsOption = ref({
  backgroundColor: '#fff',
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    },
    formatter: function(params: any) {
      let result = `<div style="font-size:12px;font-weight:bold">输出位 ${params[0].name}</div>`;
      for (const param of params) {
        result += `
          <div style="display:flex;align-items:center;margin-top:3px;font-size:12px">
            <span style="display:inline-block;width:8px;height:8px;border-radius:50%;background:${param.color};margin-right:5px"></span>
            <span>${param.seriesName}: <strong>${param.value.toFixed(2)} A</strong></span>
          </div>
        `;
      }
      return result;
    }
  },
  grid: {
    left: '8%',    // 调整边距以适应小尺寸
    right: '5%',
    bottom: '15%', // 增加底部空间给X轴标签
    top: '15%'     // 增加顶部空间给标题
  },
  xAxis: {
    type: 'category',
    data: Array.from({ length: 24 }, (_, i) => i < 9 ? `0${i + 1}` : `${i + 1}`), // 输出位01-24
    axisLabel: {
      interval: 0,
      color: '#666',
      fontSize: 10,  // 缩小字体
      margin: 8      // 标签与轴线间距
    },
    axisTick: {
      alignWithLabel: true,
      length: 3      // 缩短刻度线
    },
    name: '输出位',
    nameTextStyle: {
      fontSize: 12   // 缩小名称字体
    },
    nameGap: 20      // 缩小名称与轴线距离
  },
  yAxis: {
    type: 'value',
    name: '电流(A)',
    nameTextStyle: {
      fontSize: 12   // 缩小名称字体
    },
    nameGap: 20,     // 缩小名称与轴线距离
    axisLabel: {
      fontSize: 10   // 缩小标签字体
    },
    splitLine: {
      lineStyle: {
        type: 'dashed',
        color: '#eee'
      }
    }
  },
  series: [{
    name: '电流值',
    type: 'bar',
    barWidth: '40%', // 缩小柱宽
    data: prop.list,
    itemStyle: {
      color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: '#00778c' },
        { offset: 1, color: '#00a8a8' } // 从 #00778c 渐变到 #00a8a8
      ]),
      borderRadius: [2, 2, 0, 0], // 缩小圆角
      shadowBlur: 2,             // 缩小阴影
      shadowOffsetY: 1
    },
    label: {
      show: true,
      position: 'top',
      formatter: (params: any) => `${params.value.toFixed(2)}`,
      fontSize: 9,    // 缩小标签字体
      color: '#333'
    }
  }],
  toolbox: {
    right: 10,        // 调整位置
    top: 5,
    feature: {
      saveAsImage: {
        title: '保存',
        pixelRatio: 2
      },
      dataView: {
        title: '数据',
        readOnly: true
      },
      dataZoom: {
        title: {
          zoom: '缩放',
          back: '还原'
        }
      },
      restore: {
        title: '还原'
      }
    },
    iconStyle: {
      borderWidth: 0.5 // 缩小图标边框
    }
  }
})

// 监听数据变化
watchEffect(() => {
  echartsOption.value.series[0].data = prop.list
  echartsOption.value.series[0].itemStyle.color = new echarts.graphic.LinearGradient(0, 0, 0, 1, [
    { offset: 0, color: prop.color },
    { offset: 1, color: '#00a8a8' } // 从 prop.color 渐变到 #00a8a8
  ])
})
</script>

<style lang="scss" scoped>
/* 可以添加容器样式控制整体大小 */
</style>
