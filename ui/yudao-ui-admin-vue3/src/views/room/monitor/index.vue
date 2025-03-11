<template>
  <div class="demo-collapse">
    <el-collapse v-model="activeNames" @change="handleChange">
      <el-collapse-item title="未区分" name="1" style="padding: 10px 0;">
        <div class="arrayContainer">
          <div class="arrayItem" v-for="item in 16" :key="item.devKey" @click="handleRoomHome">
            <div class="devKey">{{ item.location != null ? item.location : item.devKey }}</div>
            <div class="content">
              <div class="icon" >
                <div v-if="item.eleActiveTotal != null" >
                  统计
                </div>    
              </div>
              <div class="info" >                  
                <div  v-if="item.eleActiveTotal != null">
                  <el-text v-if="item.eleActiveTotal != null" >
                    有功电能：{{item.eleActiveTotal}}kWh
                  </el-text>
                </div>
                <div  v-if="item.powApparentTotal != null">
                  <el-text v-if="item.powApparentTotal != null" >
                    视在功率：{{item.powApparentTotal}}kVA
                  </el-text>
                </div>
                <div  v-if="item.powActiveTotal != null">
                  <el-text v-if="item.powActiveTotal != null" >
                    有功功率：{{item.powActiveTotal}}kW
                  </el-text>
                </div>
                <div  v-if="item.powReactiveTotal != null">
                  <el-text v-if="item.powReactiveTotal != null" >
                    无功功率：{{item.powReactiveTotal}}kVar
                  </el-text>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-collapse-item>
      <el-collapse-item title="第一层" name="2" style="padding: 10px 0;">
        <div class="arrayContainer">
          <div class="arrayItem" v-for="item in 5" :key="item.devKey">
            <div class="devKey">{{ item.location != null ? item.location : item.devKey }}</div>
            <div class="content">
              <div class="icon" >
                <div v-if="item.eleActiveTotal != null" >
                  统计
                </div>    
              </div>
              <div class="info" >                  
                <div  v-if="item.eleActiveTotal != null">
                  <el-text v-if="item.eleActiveTotal != null" >
                    有功电能：{{item.eleActiveTotal}}kWh
                  </el-text>
                </div>
                <div  v-if="item.powApparentTotal != null">
                  <el-text v-if="item.powApparentTotal != null" >
                    视在功率：{{item.powApparentTotal}}kVA
                  </el-text>
                </div>
                <div  v-if="item.powActiveTotal != null">
                  <el-text v-if="item.powActiveTotal != null" >
                    有功功率：{{item.powActiveTotal}}kW
                  </el-text>
                </div>
                <div  v-if="item.powReactiveTotal != null">
                  <el-text v-if="item.powReactiveTotal != null" >
                    无功功率：{{item.powReactiveTotal}}kVar
                  </el-text>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-collapse-item>
      <el-collapse-item title="第二层" name="3" style="padding: 10px 0;">
        <div class="arrayContainer">
          <div class="arrayItem" v-for="item in 30" :key="item.devKey">
            <div class="devKey">{{ item.location != null ? item.location : item.devKey }}</div>
            <div class="content">
              <div class="icon" >
                <div v-if="item.eleActiveTotal != null" >
                  统计
                </div>    
              </div>
              <div class="info" >                  
                <div  v-if="item.eleActiveTotal != null">
                  <el-text v-if="item.eleActiveTotal != null" >
                    有功电能：{{item.eleActiveTotal}}kWh
                  </el-text>
                </div>
                <div  v-if="item.powApparentTotal != null">
                  <el-text v-if="item.powApparentTotal != null" >
                    视在功率：{{item.powApparentTotal}}kVA
                  </el-text>
                </div>
                <div  v-if="item.powActiveTotal != null">
                  <el-text v-if="item.powActiveTotal != null" >
                    有功功率：{{item.powActiveTotal}}kW
                  </el-text>
                </div>
                <div  v-if="item.powReactiveTotal != null">
                  <el-text v-if="item.powReactiveTotal != null" >
                    无功功率：{{item.powReactiveTotal}}kVar
                  </el-text>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-collapse-item>
      <el-collapse-item title="第三层" name="4" style="padding: 10px 0;">
        <div class="arrayContainer">
          <div class="arrayItem" v-for="item in 20" :key="item.devKey">
            <div class="devKey">{{ item.location != null ? item.location : item.devKey }}</div>
            <div class="content">
              <div class="icon" >
                <div v-if="item.eleActiveTotal != null" >
                  统计
                </div>    
              </div>
              <div class="info" >                  
                <div  v-if="item.eleActiveTotal != null">
                  <el-text v-if="item.eleActiveTotal != null" >
                    有功电能：{{item.eleActiveTotal}}kWh
                  </el-text>
                </div>
                <div  v-if="item.powApparentTotal != null">
                  <el-text v-if="item.powApparentTotal != null" >
                    视在功率：{{item.powApparentTotal}}kVA
                  </el-text>
                </div>
                <div  v-if="item.powActiveTotal != null">
                  <el-text v-if="item.powActiveTotal != null" >
                    有功功率：{{item.powActiveTotal}}kW
                  </el-text>
                </div>
                <div  v-if="item.powReactiveTotal != null">
                  <el-text v-if="item.powReactiveTotal != null" >
                    无功功率：{{item.powReactiveTotal}}kVar
                  </el-text>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-collapse-item>
    </el-collapse>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import type { CollapseModelValue } from 'element-plus'

const activeNames = ref(['1'])
const handleChange = (val: CollapseModelValue) => {
  console.log(val)
}
const list = ref([
  { 
    id:null,
    status:null,
    apparentPow:null,
    pow:null,
    ele:null,
    devKey:null,
    location:null,
    dataUpdateTime : "",
    pduAlarm:"",
    pf:null,
    acur : null,
    bcur : null,
    ccur : null,
    curUnbalance : null,
  }
]) as any// 列表的数据
const item = ref(
  { 
    id:null,
    status:null,
    apparentPow:null,
    pow:null,
    ele:null,
    devKey:null,
    location:null,
    dataUpdateTime : "",
    pduAlarm:"",
    pf:null,
    acur : null,
    bcur : null,
    ccur : null,
    curUnbalance : null,
  }
) as any// 列表的数据

const { push } = useRouter() // 路由跳转
const handleRoomHome = () => {
  push({path: '/room/home', state: { roomId: 1 }})
}
</script>

<style scoped lang="scss">
.arrayContainer {
  display: flex;
  flex-wrap: wrap;
  padding: 0 20px;
  .arrayItem {
    width: 25%;
    height: 140px;
    font-size: 13px;
    box-sizing: border-box;
    background-color: #eef4fc;
    border: 5px solid #fff;
    padding-top: 40px;
    position: relative;
    .content {
      display: flex;
      align-items: center;
      .icon {
        width: 74px;
        height: 30px;
        margin: 0 28px;
        font-size: large;
        text-align: center;
      }
    }
    .devKey{
      position: absolute;
      left: 8px;
      top: 8px;
    }
    .room {
      position: absolute;
      left: 8px;
      top: 8px;
    }
    .status {
      width: 40px;
      height: 20px;
      font-size: 12px;
      display: flex;
      align-items: center;
      justify-content: center;

      color: #fff;
      position: absolute;
      right: 38px;
      top: 8px;
    }
    .detail {
      width: 40px;
      height: 25px;
      padding: 0;
      border: 1px solid #ccc;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #fff;
      position: absolute;
      right: 8px;
      bottom: 8px;
      cursor: pointer;
    }
  }
}

:deep(.el-collapse-item__header) {
  padding: 0 20px;
}
</style>