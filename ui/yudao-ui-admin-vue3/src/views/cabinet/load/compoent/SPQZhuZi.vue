<template>
  <Echart :height="height" :width="width" :options="echartsOption" />
</template>

<script lang="ts" setup>
import 'echarts-liquidfill'
import { pa } from 'element-plus/es/locale';

const echartsOption = reactive<any>({
  xAxis: {
    data: ['S', 'P', 'Q'],
    "axisLine":{
      "show":false //隐藏x轴线
    },
    "axisTick":{
      "show":false //隐藏x轴刻度
    }
  },
  yAxis: {},
  label: {
    show: true,
    position: 'top',
    formatter: valueFormatter
  },  
});

function valueFormatter(param) {
  if(param.name == 'S'){
    return Number(param.data).toFixed(3)+"kVA";
  }else if(param.name=='P'){
    return Number(param.data).toFixed(3)+"kW";
  }else if(param.name=='Q'){
    return Number(param.data).toFixed(3)+"kVar";
  }
  return '';
}
const props = defineProps({
  ARoad:{
    type:Array,
    required:true,
  },
  BRoad:{
    type:Array,
    required:true
  },
  height:{
    type:String,
    default:'120%'
  },
  width:{
    type:String,
    default:'90%'
  }
})

watch(() => [props.ARoad,props.BRoad],(val) => {
  console.log('val', val)
  echartsOption.series = [
    {
      type: 'bar',
      data: val[0]
    },
    {
      type: 'bar',
      data: val[1]
    }
  ]
},{immediate:true})
</script>

<style lang="scss" scoped>

</style>
