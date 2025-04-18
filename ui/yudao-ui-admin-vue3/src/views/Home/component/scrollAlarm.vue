<template>
  <div 
    class="scroll-notice"
    @mouseenter="pause"
    @mouseleave="resume"
  >
    <div 
      class="scroll-content"
      :style="{ transform: `translateY(-${currentIndex * itemHeight}px)` }"
    >
      <div 
        v-for="(item, index) in noticesWithClone"
        :key="index"
        class="scroll-item is-urgent"
        @click="handleClick(item)"
      >
        <Icon :size="18" icon="ep:bell-filled" style="margin-right: 8px;" />
        {{ item.alarmPosition + "在" + formatDate(item.startTime, 'YYYY-MM-DD HH:mm:ss') + "发生" + item.alarmTypeDesc }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { formatDate } from '@/utils/formatTime'

const props = defineProps({
  notices: {
    type: Array,
    default: () => [
      { id: 1, text: 'PDU离线告警', urgent: false },
      { id: 2, text: '始端箱设备告警', urgent: true },
      { id: 3, text: '插接箱状态告警', urgent: false },
      { id: 1, text: 'PDU离线告警', urgent: false },
      { id: 2, text: '始端箱设备告警', urgent: true },
      { id: 3, text: '插接箱状态告警', urgent: false },
      { id: 1, text: 'PDU离线告警', urgent: false },
      { id: 2, text: '始端箱设备告警', urgent: true },
      { id: 3, text: '插接箱状态告警', urgent: false },
      { id: 1, text: 'PDU离线告警', urgent: false },
      { id: 2, text: '始端箱设备告警', urgent: true },
      { id: 3, text: '插接箱状态告警', urgent: false },
    ]
  },
  speed: {
    type: Number,
    default: 2000 // 滚动间隔(ms)
  },
  itemHeight: {
    type: Number,
    default: 40 // 每项高度(px)
  }
})

const emit = defineEmits(['itemClick'])

const currentIndex = ref(0)
let timer = null

// 添加前两项实现无缝衔接
const noticesWithClone = computed(() => [
  ...props.notices,
  ...props.notices.slice(0, 2)
])

function scrollNext() {
  currentIndex.value++
  if (currentIndex.value >= props.notices.length) {
    currentIndex.value = 0
  }
}

function startScroll() {
  timer = setInterval(scrollNext, props.speed)
}

function pause() {
  clearInterval(timer)
}

function resume() {
  startScroll()
}

function handleClick(item) {
  emit('itemClick', item)
}

onMounted(() => {
  startScroll()
})

onUnmounted(() => {
  clearInterval(timer)
})
</script>

<style scoped>
.scroll-notice {
  height: v-bind('itemHeight + "px"');
  overflow: hidden;
  position: relative;
}

.scroll-content {
  transition: transform 0.5s ease;
}

.scroll-item {
  height: v-bind('itemHeight + "px"');
  line-height: v-bind('itemHeight + "px"');
  padding: 0 15px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: flex;
  align-items: center;
}

.is-urgent {
  color: red;
  font-weight: bold;
}

.icon {
  margin-right: 8px;
  font-size: 14px;
}
</style>