<template>
 <div class="master">
    <!-- 左大侧 -->
    <div class="master-left">
      <ContentWrap style="height: calc(100% - 15px)">
        <div v-if="!isCloseNav" class="nav-left">
          <!-- 左侧标题栏 -->
          <div class="navBar">{{navTitle}}</div>
          <!-- 信息展示模式 -->
          <slot v-if="!switchNav" name="NavInfo"></slot>
            <!-- 筛选模式 -->
          <div v-if="showNavTree" >
            <NavTree ref="navTree" @node-click="handleClick" @check="handleCheck" :showCheckbox="showCheckbox" :showSearch="true" :dataList="dataList" :load="load" :lazy="lazy" />
          </div>
        </div>
        <div v-if="!isCloseNav" class="openNavtree" @click.prevent="isCloseNav = true">
          <Icon icon="ep:arrow-left" />收起
        </div>
        <div v-if="!isCloseNav" class="reduce" @click.prevent="handleSwitchNav">
          <Icon icon="ep:arrow-up" v-if="!switchNav" style="" />
          <Icon icon="ep:arrow-down" v-if="switchNav" />{{!switchNav ? '收缩' : '扩展'}}
        </div>
        <div v-if="isCloseNav" class="expand" @click.prevent="isCloseNav = false"><Icon icon="ep:arrow-right" /><span>展</span><span>开</span></div>
      </ContentWrap>
    </div>
    <!-- 右大侧 -->
    <div class="master-right">
      <ContentWrap>
        <slot name="ActionBar"></slot>
      </ContentWrap>
      <ContentWrap style="min-height: 680px">
        <slot name="Content"></slot>
      </ContentWrap>
    </div>
  </div>
</template>

<script lang="ts" setup>
const emits = defineEmits(['node-click', 'check'])
const props = defineProps({
  dataList: Array,
  navTitle: String,
  showSearch: Boolean,
  isAccordion: {
    type: Boolean,
    default: false
  },
  showNavTree: {
    type: Boolean,
    default: true
  },
  showCheckbox: {
    type: Boolean,
    default: true
  },
  lazy: {
    type: Boolean,
    default: false
  },
  load: {
    type: Function,
    required: false
  },
  placeholder: {
    type: String,
    required: false
  }
})

const switchNav = ref(false) //false: 导航树 true：微模块展示
const isCloseNav = ref(false) // 左侧导航是否收起

// 处理切换按钮点击事件
const handleSwitchNav = () => {
  console.log('处理切换按钮点击事件', switchNav.value)
  switchNav.value = !switchNav.value
}

/** 处理navtree组件节点被点击 */
const handleClick = (data) => {
  emits('node-click', data)
}

/** 处理navtree组件多选被点击 */
const handleCheck = (data) => {
  emits('check', data)
}
</script>

<style lang="scss" scoped >
.master {
  width: 100%;
  box-sizing: border-box;
  display: flex;
  .master-left {
    position: relative;
    overflow: hidden;
    box-sizing: border-box;
    margin-right: 20px;
    transition: all 0.2s linear;
    .openNavtree {
      // width: 100%;
      box-sizing: border-box;
      text-align: right;
      cursor: pointer;
      position: absolute;
      right: 10px;
      top: 12px;
      font-size: 15px;
      display: flex;
      align-items: center;
    }
    .reduce {
      display: flex;
      align-items: center;
      position: absolute;
      right: 10px;
      top: 52px;
      // color: #777777;
      cursor: pointer;
      font-size: 14px;
    }
    .expand {
      width: 30px;
      display: flex;
      flex-direction: column;
      align-items: center;
      color: #777;
      cursor: pointer;
      background-color: #eef4fc;
      padding: 10px 0;
    }
  }
  .master-right {
    flex: 1;
    overflow: hidden;
    width: 100%;
  }
}
.navBar {
  box-sizing: border-box;
  width: 100%;
  height: 46px;
  line-height: 46px;
  padding-left: 10px;
  background-color: #eef4fc;
  font-size: 14px;
}
.nav-left {
  width: 200px;
  height: 100%;
}
:deep(.master-left .el-card__body) {
  padding: 0;
}

</style>