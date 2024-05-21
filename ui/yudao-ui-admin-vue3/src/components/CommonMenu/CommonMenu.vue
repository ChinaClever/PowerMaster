<template>
 <div class="master">
    <!-- 左大侧 -->
    <div class="master-left">
      <ContentWrap style="height: calc(100% - 15px)">
        <div v-if="!isCloseNav" class="nav-left">
          <!-- 左侧标题栏 -->
          <div class="navBar">{{navTitle}}</div>
          <!-- 信息展示模式 -->
          <slot v-if="switchNav" name="NavInfo"></slot>
            <!-- 筛选模式 -->
          <div v-else style="margin-top: 10px">
            <NavTree ref="navTree" @node-click="handleClick" @check="handleCheck" :showCheckbox="showCheckbox" :showSearch="true" :dataList="dataList" :load="load" :lazy="lazy" />
          </div>
        </div>
        <div v-if="!isCloseNav" class="openNavtree" @click.prevent="handleSwitchNav">
          <Icon icon="ep:switch" />切换
        </div>
        <div v-if="!isCloseNav" class="reduce" @click.prevent="isCloseNav = true"><Icon icon="ep:arrow-left" />收起</div>
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
      color: #777777;
      cursor: pointer;
      font-size: 13px;
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
  }
}
.navBar {
  box-sizing: border-box;
  width: 100%;
  height: 46px;
  line-height: 46px;
  padding-left: 10px;
  background-color: #d5ffc1;
  font-size: 14px;
}
.nav-left {
  width: 215px;
  height: 100%;
  .overview {
    padding: 0 20px;
    .count {
      height: 70px;
      margin-bottom: 15px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-left: 15px;
      padding-right: 10px;
      box-shadow: 0 3px 4px 1px rgba(0,0,0,.12);
      border-radius: 3px;
      border: 1px solid #eee;
      .info {
        height: 46px;
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        justify-content: space-between;
        font-size: 13px;
        .value {
          font-size: 15px;
          font-weight: bold;
        }
      }
    }
  }
  .status {
    display: flex;
    flex-wrap: wrap;
    .box {
      height: 70px;
      width: 50%;
      box-sizing: border-box;
      display: flex;
      justify-content: center;
      align-items: center;
      flex-direction: column;
      .top {
        display: flex;
        align-items: center;
        .tag {
          width: 8px;
          height: 8px;
          background-color: #3bbb00;
          margin-right: 3px;
          margin-top: 2px;
        }
        .empty {
          background-color: #ccc;
        }
        .warn {
          background-color: #ffc402;
        }
        .error {
          background-color: #fa3333;
        }
      }
      .value {
        font-size: 14px;
        margin-top: 5px;
        color: #aaa;
        .number {
          font-size: 14px;
          font-weight: bold;
          margin-right: 5px;
          color: #000;
        }
      }
    }
  }
  .header {
    display: flex;
    flex-direction: column;
    align-items: center;
    font-size: 13px;
    padding-top: 28px;
    .header_img {
      width: 110px;
      height: 110px;
      border-radius: 50%;
      display: flex;
      justify-content: center;
      align-items: center;
      border: 1px solid #555;
      img {
        width: 75px;
        height: 75px;
      }
    }
    .name {
      font-size: 15px;
      margin: 15px 0;
    }
  }
  .line {
    height: 1px;
    margin-top: 28px;
    margin-bottom: 20px;
    background: linear-gradient(297deg, #fff, #dcdcdc 51%, #fff);
  }
}
:deep(.master-left .el-card__body) {
  padding: 0;
}
</style>