<script lang="ts" setup>
import { useAppStore } from '@/store/modules/app'
import { propTypes } from '@/utils/propTypes'
import { useDesign } from '@/hooks/web/useDesign'
import { useRouter } from 'vue-router'

defineOptions({ name: 'Collapse' })

//组件挂载时候调用
onMounted(() => {
  toggleStyles()
})

const mediaQueryList = window.matchMedia('(min-width: 1600px) and (max-width: 2048px)') //检查视口宽度是否小于2048像素和是否大于1600像素
const maxScreenList =  window.matchMedia('(min-width: 2048px)') //检查视口宽度是否大于2048像素

const router = useRouter()

const { getPrefixCls } = useDesign()

const prefixCls = getPrefixCls('collapse')

defineProps({
  color: propTypes.string.def('')
})

const appStore = useAppStore()

const collapse = computed(() => appStore.getCollapse)

const toggleCollapse = () => {
  const collapsed = unref(collapse)
  appStore.setCollapse(!collapsed)
}

//不同分辨率的页面的初始化显示
const initializeStyle = () => {    
  if (maxScreenList.matches) {  
    appStore.setCollapse(false)
  } else if (mediaQueryList.matches) {  
    appStore.setCollapse(false)
  } else {  
    appStore.setCollapse(true);  
  }  
}

//根据页面的分辨率不同进行改变
const toggleStyles = () => {
  if (mediaQueryList.matches) {
    appStore.setCollapse(false)
  } else {
    if (maxScreenList.matches) {
      appStore.setCollapse(false)
    } else {
        const collapsed = unref(collapse)
        appStore.setCollapse(!collapsed)
    }
  }
}
// 监听媒体查询变化
mediaQueryList.addEventListener('change', toggleStyles)

// 在路由切换时调用初始化样式函数
router.beforeEach((to, from, next) => {
  initializeStyle()
  next()
})
</script>

<template>
  <div :class="prefixCls" @click="toggleCollapse">
    <Icon
      :color="color"
      :icon="collapse ? 'ep:expand' : 'ep:fold'"
      :size="18"
      class="cursor-pointer"
    />
  </div>
</template>
