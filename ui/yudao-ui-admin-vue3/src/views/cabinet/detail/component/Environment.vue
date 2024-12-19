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
  },
  width: {
    type: [Number,String],
  }
})

console.log('loadFactor',props.loadFactor)

let data = [{
    name: "a",
    value: 40
}, {
    name: "b",
    value: 30
}, {
    name: "c",
    value: 20
}, {
    name: "d",
    value: 10
}]
let label = data.map((item) => {
    return item.name
})
let left = data.map((item) => {
    return -item.value-10
})
let right = data.map((item) => {
    return item.value
})
const echartsOption = reactive({
    dataZoom: {
        type: 'inside',
        start: 0,
        end: 100,
        yAxisIndex: [0]
    },
    tooltip: {
        trigger: 'axis',
        // formatter: "{b}<br/>{a0}："+(-1*{c0})+"%<br/>{a1}：{c1}%",
        formatter: (params) => {
            return `${params[0].name}<br/>${params[0].seriesName}：${-params[0].value}%<br/>${params[1].seriesName}：${params[1].value}%`
        },
        axisPointer: {
            // 坐标轴指示器，坐标轴触发有效
            type: 'shadow' // 默认为直线，可选为：'line' | 'shadow'
        }
    },
    legend: {
        show: true,
        icon: 'rect',
        itemWidth: 20,
        itemHeight: 10,
        itemGap: 9,
        top:'0',
        left: '45%',
        textStyle: {
            fontSize: 14,
        },
        data: ['左', '右'],
    },
    grid: {
        left: '10%',
        right:'10%',
        top:'10%',
        bottom :'5%'
    },
    xAxis: [{
        type: 'value',
        axisTick: {
            show: false
        },
        axisLine: {
            show: false
        },
        splitLine: {
            show: false
        },
        axisLabel: {
            show: false
        }
    }],
    yAxis: [{
        type: 'category',
        inverse: true,
        axisTick: {
            show: false
        },
        axisLine: {
            show: false,
            onZero: false //默认为true，轴线会在中间，及x轴0刻度处
        },
        splitLine: {
            show: false
        },
        axisLabel: {
            interval: 0,
            margin: 10,
            textStyle: {
                fontSize: 14,
                color: '#000'
            }
        },
        data: label
    }],
    series: [{
            name: '左',
            type: 'bar',
            barWidth: 15,
            stack: '总量',
            label: {
                show: true,
                fontSize: 14,
                distance: 10,
                color: '#000',
                position: 'left',
                formatter: (params)=>{
                    return -params.value + '%'
                }
            },
            data: left
        },
        {
            name: '右',
            type: 'bar',
            barWidth: 15,
            stack: '总量',
            label: {
               show: true,
                fontSize: 14,
                distance: 10,
                color: '#000',
                position: 'right',
                formatter: (params)=>{
                    return params.value + '%'
                }
            },
            data: right
        }
    ]
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