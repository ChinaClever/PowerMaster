<script lang="ts" setup>
import { formatDate } from '@/utils/formatTime'
import { AlarmApi } from '@/api/system/notify/alarm'
import * as NotifyMessageApi from '@/api/system/notify/message'

defineOptions({ name: 'Message' })

const { push } = useRouter()
const activeName = ref('notice')
const unreadCount = ref(0) // 未读消息数量
const list = ref<any[]>([]) // 消息列表

// 获得消息列表
const getList = async () => {
  push('/Alarm')
  // list.value = await NotifyMessageApi.getUnreadNotifyMessageList()
  // 强制设置 unreadCount 为 0，避免小红点因为轮询太慢，不消除
  // unreadCount.value = 3
}

// 获得未读消息数
const getUnreadCount = async () => {
  const res = await AlarmApi.getUnhandleAlarm({})
  console.log('获得未读消息数', res)
  if (res) {
    // unreadCount.value = res[1] + res[2]
    unreadCount.value = res.total
  }
  // NotifyMessageApi.getUnreadNotifyMessageCount().then((data) => {
  //   unreadCount.value = data
  // })
}

// 跳转我的站内信
const goMyList = () => {
  push({
    name: 'MyNotifyMessage'
  })
}

// ========== 初始化 =========
onMounted(() => {
  // 首次加载小红点
  getUnreadCount()
  // 轮询刷新小红点
  setInterval(
    () => {
      getUnreadCount()
    },
    1000 * 60 * 2
  )
})
</script>
<template>
  <div class="message">
    <el-badge :value="unreadCount" :max="1000000" >
      <Icon :size="18" class="cursor-pointer" :icon="unreadCount ? 'ep:bell-filled' : 'ep:bell'" @click="getList" :color="unreadCount ? 'red' : ''" />
    </el-badge>
    <!-- <ElPopover :width="400" placement="bottom" trigger="click">
      <template #reference>
        <ElBadge :is-dot="unreadCount > 0" class="item">
          
        </ElBadge>
      </template> -->
      <!-- <ElTabs v-model="activeName">
        <ElTabPane label="我的站内信" name="notice">
          <el-scrollbar class="message-list">
            <template v-for="item in list" :key="item.id">
              <div class="message-item">
                <img alt="" class="message-icon" src="@/assets/imgs/avatar.gif" />
                <div class="message-content">
                  <span class="message-title">
                    {{ item.templateNickname }}：{{ item.templateContent }}
                  </span>
                  <span class="message-date">
                    {{ formatDate(item.createTime) }}
                  </span>
                </div>
              </div>
            </template>
          </el-scrollbar>
        </ElTabPane>
      </ElTabs> -->
      <!-- 更多 -->
      <!-- <div style="margin-top: 10px; text-align: right">
        <XButton preIcon="ep:view" title="查看全部" type="primary" @click="goMyList" />
      </div> -->
    <!-- </ElPopover> -->
  </div>
</template>
<style lang="scss" scoped>
.message-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 260px;
  line-height: 45px;
}

.message-list {
  display: flex;
  height: 400px;
  flex-direction: column;

  .message-item {
    display: flex;
    align-items: center;
    padding: 20px 0;
    border-bottom: 1px solid var(--el-border-color-light);

    &:last-child {
      border: none;
    }

    .message-icon {
      width: 40px;
      height: 40px;
      margin: 0 20px 0 5px;
    }

    .message-content {
      display: flex;
      flex-direction: column;

      .message-title {
        margin-bottom: 5px;
      }

      .message-date {
        font-size: 12px;
        color: var(--el-text-color-secondary);
      }
    }
  }
}
:deep(.el-badge__content) {
  scale: 0.8;
}
</style>
