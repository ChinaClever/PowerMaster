<template>
  <div ref="scrollableContainer" :style="props.isFromHome ? `height: 30vh;overflow: auto;` : ``" @scroll="handleScroll">
    <div style="display: flex;align-items: center;justify-content: space-between">
      <el-form
        class="-mb-15px"
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        label-width="68px"                          
      >
        <div v-if="!props.isFromHome" style="display: flex;aligns-item: center">
          <el-button @click="valueMode = 0;" :type="valueMode == 0 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />机房功率</el-button>                             
          <el-button @click="valueMode = 1;" :type="valueMode == 1 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />机房温度</el-button>            
          <el-button @click="valueMode = 2;" :type="valueMode == 2 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />机房对比</el-button>    
          <el-form-item label="机房名" prop="devKey">
            <el-input
              v-model="searchRoomName"
              placeholder="请输入机房名"
              clearable
              class="!w-160px"
              height="35"
            />
          </el-form-item>
          <el-form-item style="margin-left: 10px;">
            <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
            <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
            <el-button v-if="activeNames?.length == 0" @click="openAllCollapse"><Icon icon="ep:arrow-down" class="mr-5px" /> 一键展开</el-button>
            <el-button v-else @click="activeNames = [];console.log(activeNames)"><Icon icon="ep:arrow-up" class="mr-5px" /> 一键收缩</el-button>
            <el-button
              type="primary"
              plain
              @click="openForm('create')"
              v-hasPermi="['pdu:PDU-device:create']"
            >
              <Icon icon="ep:plus" class="mr-5px" /> 新增
            </el-button>
            <el-button
              type="success"
              plain
              @click="handleExport"
              :loading="exportLoading"
              v-hasPermi="['pdu:PDU-device:export']"
            >
              <Icon icon="ep:download" class="mr-5px" /> 导出
            </el-button>
          </el-form-item>
        </div>
      </el-form>
      <div v-if="!props.isFromHome" class="btns">
        <el-button v-if="!editRoom" @click="editRoom = true"><Icon icon="ep:grid" style="margin-right: 4px" />编辑机房</el-button>        
        <el-button v-else @click="editRoom = false"><Icon icon="ep:grid" style="margin-right: 4px" />取消编辑</el-button>        
        <el-button v-if="editRoom" @click="handleAdd"><Icon icon="ep:grid" style="margin-right: 4px" />新建机房</el-button>        
        <el-button @click="switchValue = 0;" :type="switchValue == 0 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />阵列模式</el-button>
        <el-button @click="switchValue = 3;" :type="switchValue == 3 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 4px" />表格模式</el-button>
        <el-button @click="handleStopDelete();switchValue = 2;" :type="switchValue ===2 ? 'primary' : ''" v-show="switchValue ===3"><Icon icon="ep:expand" style="margin-right: 8px" />已删除</el-button>
      </div>
    </div>
    <div v-if="switchValue == 0" style="padding: 20px 0;background-color: #fff">
      <el-skeleton :loading="loading" animated v-if="valueMode===0" style="padding: 0 20px;box-sizing: border-box">
        <el-row>
          <template v-if="roomShowType">
            <el-col>
              <div class="arrayContainer" :style="isFromHome ? 'padding: 0' : ''">
                <div 
                  class="arrayItem"
                  v-for="(item, index) in addrAllRoomList[0]"
                  @dblclick="handleRoomHome(item.id)"
                  :key="`card-${index}`"
                >
                  <el-card shadow="hover">
                    <div class="flex items-center h-21px mb-2 justify-between">
                      <!-- <Icon :icon="item.icon" :size="25" class="mr-8px" /> -->
                      <div class="text-15px">{{ item.roomName || '' }}</div>
                      <div><el-tag v-if="item.alarmCount > 0" type="danger">告警</el-tag></div>
                    </div>
                    <div style="display: flex;justify-content: space-between;align-items: center;padding: 0 0 15px 10px;">
                      
                      <div v-if="item.powApparent >= 1000 || item.powActive >= 1000 || item.powReactive >= 1000" style="display: flex;flex-direction: column;height: 100px;justify-content:space-between">
                        <div><span class="bullet" style="background-color:#E5B849;"></span><el-text :style="isFromHome ? 'font-size: 12px' : ''">视在功率：{{item.powApparent ? (item.powApparent/1000).toFixed(1) : '0.0'}}MVA</el-text></div>
                        <div><span class="bullet" style="background-color:#C8603A;"></span><el-text :style="isFromHome ? 'font-size: 12px' : ''">有功功率：{{item.powActive ? (item.powActive/1000).toFixed(1) : '0.0'}}MW</el-text></div>
                        <div><span class="bullet" style="background-color:#AD3762;"></span><el-text :style="isFromHome ? 'font-size: 12px' : ''">无功功率：{{item.powReactive ? (item.powReactive/1000).toFixed(1) : '0.0'}}MVAr</el-text></div>
                      </div>
                      <div v-else style="display: flex;flex-direction: column;height: 100px;justify-content:space-between">
                        <div><span class="bullet" style="background-color:#E5B849;"></span><el-text :style="isFromHome ? 'font-size: 12px' : ''">视在功率：{{item.powApparent ? item.powApparent.toFixed(1) : '0.0'}}kVA</el-text></div>
                        <div><span class="bullet" style="background-color:#C8603A;"></span><el-text :style="isFromHome ? 'font-size: 12px' : ''">有功功率：{{item.powActive ? item.powActive.toFixed(1) : '0.0'}}kW</el-text></div>
                        <div><span class="bullet" style="background-color:#AD3762;"></span><el-text :style="isFromHome ? 'font-size: 12px' : ''">无功功率：{{item.powReactive ? item.powReactive.toFixed(1) : '0.0'}}kVAr</el-text></div>
                      </div>
                      <div v-if="item.displayFlag && !item.displayType" style="display: flex;flex-direction: column;">
                        <el-progress type="dashboard" :percentage="item.roomLoadFactor ? Math.min(item.roomLoadFactor,100).toFixed(0) : 0" width="100" stroke-width="12" :color="item.roomLoadFactor>90 ? `rgba(173, 55, 98, ${item.roomLoadFactor/100})` : (item.roomLoadFactor>=60 ? `rgba(200, 96, 58, ${(item.roomLoadFactor+10)/100})` : `rgba(229, 184, 73, ${(item.roomLoadFactor+40)/100})`)">
                          <span class="percentage-value" :style="{textAlign: 'center',fontSize: isFromHome ? '22px' : '24px'}">{{item.roomLoadFactor ? item.roomLoadFactor.toFixed(0) : 0}}%</span><br/>
                          <span class="percentage-label" :style="{textAlign: 'center',fontSize: '12px'}">负载率</span>
                        </el-progress>
                        <!-- <div v-if="item.displayFlag" :style="{textAlign: 'center',fontSize: isFromHome ? '24px' : '34px'}">{{item.displayType ? (item.roomPue ? item.roomPue.toFixed(2) : 0) : (item.roomLoadFactor ? item.roomLoadFactor.toFixed(0) : '0%')}}</div>
                        <div v-if="item.displayFlag" :style="{textAlign: 'center',fontSize: isFromHome ? '14px' : '22px'}">{{item.displayType ? "PUE" : "负载率"}}</div> -->
                      </div>
                      <div v-else>
                        <Echart :height="100" :width="100" :options="addrAllPowChartOptions[0][index]" />
                      </div>
                      <div></div>
                    </div>
                  </el-card>
                  <div style="position: absolute;bottom: 5px;right: 5px">
                    <div style="display: flex;height: 25px;justify-content: space-between">
                      <button v-if="editRoom" class="detail" @click="openSetting(item)" :style="props.isFromHome ? 'width:25px;height:20px;font-size:11px' : ''" >编辑</button>
                      <button class="detail" @click="handleRoomHome(item.id)" :style="props.isFromHome ? 'width:25px;height:20px;font-size:11px' : ''" >详情</button>
                    </div>
                  </div>
                </div>
              </div>
            </el-col>
          </template>
        </el-row>
      </el-skeleton>
      <el-skeleton :loading="loading" animated v-else-if="valueMode===1" style="padding: 0 20px">
        <el-row>
          <template v-if="roomShowType">
            <el-col>
              <div class="arrayContainer" :style="props.isFromHome ? 'padding: 0' : ''">
                <div 
                  class="arrayItem"
                  v-for="(item, index) in addrAllRoomList[0]"
                  @dblclick="handleRoomHome(item.id)"
                  :key="`card-${index}`"
                >
                  <el-card shadow="hover">
                    <div class="flex items-center h-21px">
                      <!-- <Icon :icon="item.icon" :size="25" class="mr-8px" /> -->
                      <span class="text-15px">{{ item.roomName || '' }}</span>
                    </div>
                    <div class="mt-14px flex justify-around text-12px text-gray-400">
                      <div style="width: 33%;text-align:center"></div>
                      <div style="width: 33%;text-align:center"><el-text>前门</el-text></div>
                      <div style="width: 33%;text-align:center"><el-text>后门</el-text></div>
                    </div>
                    <div class="mt-14px flex justify-around text-12px text-gray-400">
                      <div style="width: 33%;text-align:center"><el-text>最高温度：</el-text></div>
                      <div style="width: 33%;text-align:center"><el-text>{{item.temMaxFront ? item.temMaxFront.toFixed(1) : '0.0'}}&deg;C</el-text></div>
                      <div style="width: 33%;text-align:center"><el-text>{{item.temMaxBlack ? item.temMaxBlack.toFixed(1) : '0.0'}}&deg;C</el-text></div>
                    </div>
                    <div class="mt-14px flex justify-around text-12px text-gray-400">
                      <div style="width: 33%;text-align:center"><el-text>平均温度：</el-text></div>
                      <div style="width: 33%;text-align:center"><el-text>{{item.temAvgFront ? item.temAvgFront.toFixed(1) : '0.0'}}&deg;C</el-text></div>
                      <div style="width: 33%;text-align:center"><el-text>{{item.temAvgBlack ? item.temAvgBlack.toFixed(1) : '0.0'}}&deg;C</el-text></div>
                    </div>
                    <!-- <div class="mt-14px flex justify-around text-12px text-gray-400">
                      <div style="width: 33%;text-align:center"><el-text>最高湿度：</el-text></div>
                      <div style="width: 33%;text-align:center"><el-text>{{item.humMaxFront ? item.humMaxFront.toFixed(0) : '0'}}%</el-text></div>
                      <div style="width: 33%;text-align:center"><el-text>{{item.humMaxBlack ? item.humMaxBlack.toFixed(0) : '0'}}%</el-text></div>
                    </div> -->
                    <div class="mt-14px flex justify-around text-12px text-gray-400">
                      <div style="width: 33%;text-align:center"><el-text>平均湿度：</el-text></div>
                      <div style="width: 33%;text-align:center"><el-text>{{item.humAvgFront ? item.humAvgFront.toFixed(0) : '0'}}%</el-text></div>
                      <div style="width: 33%;text-align:center"><el-text>{{item.humAvgBlack ? item.humAvgBlack.toFixed(0) : '0'}}%</el-text></div>
                    </div>
                    <div style="position: absolute;bottom: 5px;right: 5px">
                      <div style="display: flex;height: 25px;justify-content: space-between">
                        <button v-if="editRoom" class="detail" @click="openSetting(item)" :style="props.isFromHome ? 'width:25px;height:20px;font-size:11px' : ''" >编辑</button>
                        <button class="detail" @click="handleRoomHome(item.id)" :style="props.isFromHome ? 'width:25px;height:20px;font-size:11px' : ''" >详情</button>
                      </div>
                    </div>
                  </el-card>
                </div>
              </div>
            </el-col>
          </template>
        </el-row>
      </el-skeleton>
      <el-skeleton :loading="loading" animated v-else-if="valueMode===2" style="padding: 0 20px">
          <el-row>
            <template v-if="roomShowType">
              <el-col 
                :xl="24"
                :lg="24"
                :md="24"
                :sm="24"
                :xs="24">
                <div class="arrayContainer" :style="props.isFromHome ? 'padding: 0' : ''">
                  <!-- <div ref="powChartContainer" id="powChartContainer" style="width: 1702px;height: 400px;"></div> -->
                  <Echart :options="powOptionsData[0]" :height="400" :width="1702"/>
                </div>
              </el-col>
            </template>
          </el-row>
      </el-skeleton>
    </div>
    <el-collapse v-if="switchValue == 0" v-model="activeNames" @change="handleChange">
      <el-collapse-item v-for="(e,i) in roomAddrList.slice(1)" :key="i" :title="e" :name="i" style="padding: 10px 0;">
        <el-skeleton :loading="loading" animated v-if="valueMode===0" style="padding: 0 20px">
            <el-row>
              <template v-if="roomShowType">
                <el-col>
                  <div class="arrayContainer" :style="props.isFromHome ? 'padding: 0' : ''">
                    <div 
                      class="arrayItem"
                      v-for="(item, index) in addrAllRoomList[i+1]"
                      @dblclick="handleRoomHome(item.id)"
                      :key="`card-${index}`"
                    >
                      <el-card shadow="hover">
                        <div class="flex items-center h-21px mb-2 justify-between">
                          <!-- <Icon :icon="item.icon" :size="25" class="mr-8px" /> -->
                          <div class="text-15px">{{ item.roomName || '' }}</div>
                          <div><el-tag v-if="item.alarmCount > 0" type="danger">告警</el-tag></div>
                        </div>
                        <div style="display: flex;justify-content: space-between;align-items: center;padding: 0 0 15px 10px;">
                          <div v-if="item.powApparent >= 1000 || item.powActive >= 1000 || item.powReactive >= 1000" style="display: flex;flex-direction: column;height: 100px;justify-content:space-between">
                            <div><span class="bullet" style="background-color:#E5B849;"></span><el-text :style="isFromHome ? 'font-size: 12px' : ''">视在功率：{{item.powApparent ? (item.powApparent/1000).toFixed(1) : '0.0'}}MVA</el-text></div>
                            <div><span class="bullet" style="background-color:#C8603A;"></span><el-text :style="isFromHome ? 'font-size: 12px' : ''">有功功率：{{item.powActive ? (item.powActive/1000).toFixed(1) : '0.0'}}MW</el-text></div>
                            <div><span class="bullet" style="background-color:#AD3762;"></span><el-text :style="isFromHome ? 'font-size: 12px' : ''">无功功率：{{item.powReactive ? (item.powReactive/1000).toFixed(1) : '0.0'}}MVAr</el-text></div>
                          </div>
                          <div v-else style="display: flex;flex-direction: column;height: 100px;justify-content:space-between">
                            <div><span class="bullet" style="background-color:#E5B849;"></span><el-text :style="isFromHome ? 'font-size: 12px' : ''">视在功率：{{item.powApparent ? item.powApparent.toFixed(1) : '0.0'}}kVA</el-text></div>
                            <div><span class="bullet" style="background-color:#C8603A;"></span><el-text :style="isFromHome ? 'font-size: 12px' : ''">有功功率：{{item.powActive ? item.powActive.toFixed(1) : '0.0'}}kW</el-text></div>
                            <div><span class="bullet" style="background-color:#AD3762;"></span><el-text :style="isFromHome ? 'font-size: 12px' : ''">无功功率：{{item.powReactive ? item.powReactive.toFixed(1) : '0.0'}}kVAr</el-text></div>
                          </div>
                          <div v-if="item.displayFlag && !item.displayType" style="display: flex;flex-direction: column">
                            <el-progress type="dashboard" :percentage="item.roomLoadFactor ? Math.min(item.roomLoadFactor,100).toFixed(0) : 0" width="100" stroke-width="12" :color="item.roomLoadFactor>90 ? `rgba(173, 55, 98, ${item.roomLoadFactor/100})` : (item.roomLoadFactor>=60 ? `rgba(200, 96, 58, ${(item.roomLoadFactor+10)/100})` : `rgba(229, 184, 73, ${(item.roomLoadFactor+40)/100})`)">
                              <span class="percentage-value" :style="{textAlign: 'center',fontSize: isFromHome ? '22px' : '24px'}">{{item.roomLoadFactor ? item.roomLoadFactor.toFixed(0) : 0}}%</span><br/>
                              <span class="percentage-label" :style="{textAlign: 'center',fontSize: '12px'}">负载率</span>
                            </el-progress>
                            <!-- <div v-if="item.displayFlag" :style="{textAlign: 'center',fontSize: isFromHome ? '24px' : '34px'}">{{item.displayType ? (item.roomPue ? item.roomPue.toFixed(2) : 0) : (item.roomLoadFactor ? item.roomLoadFactor.toFixed(0) : 0)}}</div>
                            <div v-if="item.displayFlag" :style="{textAlign: 'center',fontSize: isFromHome ? '14px' : '22px'}">{{item.displayType ? "PUE" : "负载率"}}</div> -->
                          </div>
                          <div v-else>
                            <Echart :height="100" :width="100" :options="addrAllPowChartOptions[i+1][index]" />
                          </div>
                          <div></div>
                        </div>
                      </el-card>
                      <div style="position: absolute;bottom: 5px;right: 5px">
                        <div style="display: flex;height: 25px;justify-content: space-between">
                          <button v-if="editRoom" class="detail" @click="openSetting(item)" :style="props.isFromHome ? 'width:25px;height:20px;font-size:11px' : ''" >编辑</button>
                          <button class="detail" @click="handleRoomHome(item.id)" :style="props.isFromHome ? 'width:25px;height:20px;font-size:11px' : ''" >详情</button>
                        </div>
                      </div>
                    </div>
                  </div>
                </el-col>
              </template>
            </el-row>
        </el-skeleton>
        <el-skeleton :loading="loading" animated v-else-if="valueMode===1" style="padding: 0 20px">
            <el-row>
              <template v-if="roomShowType">
                <el-col>
                  <div class="arrayContainer" :style="props.isFromHome ? 'padding: 0' : ''">
                    <div 
                      class="arrayItem"
                      v-for="(item, index) in addrAllRoomList[i+1]"
                      @dblclick="handleRoomHome(item.id)"
                      :key="`card-${index}`"
                    >
                      <el-card shadow="hover">
                        <div class="flex items-center h-21px">
                          <!-- <Icon :icon="item.icon" :size="25" class="mr-8px" /> -->
                          <span class="text-15px">{{ item.roomName || '' }}</span>
                        </div>
                        <div class="mt-14px flex justify-around text-12px text-gray-400">
                          <div style="width: 33%;text-align:center"></div>
                          <div style="width: 33%;text-align:center"><el-text>前门</el-text></div>
                          <div style="width: 33%;text-align:center"><el-text>后门</el-text></div>
                        </div>
                        <div class="mt-14px flex justify-around text-12px text-gray-400">
                          <div style="width: 33%;text-align:center"><el-text>最高温度：</el-text></div>
                          <div style="width: 33%;text-align:center"><el-text>{{item.temMaxFront ? item.temMaxFront.toFixed(1) : '0.0'}}&deg;C</el-text></div>
                          <div style="width: 33%;text-align:center"><el-text>{{item.temMaxBlack ? item.temMaxBlack.toFixed(1) : '0.0'}}&deg;C</el-text></div>
                        </div>
                        <div class="mt-14px flex justify-around text-12px text-gray-400">
                          <div style="width: 33%;text-align:center"><el-text>平均温度：</el-text></div>
                          <div style="width: 33%;text-align:center"><el-text>{{item.temAvgFront ? item.temAvgFront.toFixed(1) : '0.0'}}&deg;C</el-text></div>
                          <div style="width: 33%;text-align:center"><el-text>{{item.temAvgBlack ? item.temAvgBlack.toFixed(1) : '0.0'}}&deg;C</el-text></div>
                        </div>
                        <!-- <div class="mt-14px flex justify-around text-12px text-gray-400">
                          <div style="width: 33%;text-align:center"><el-text>最高湿度：</el-text></div>
                          <div style="width: 33%;text-align:center"><el-text>{{item.humMaxFront ? item.humMaxFront.toFixed(0) : '0'}}%</el-text></div>
                          <div style="width: 33%;text-align:center"><el-text>{{item.humMaxBlack ? item.humMaxBlack.toFixed(0) : '0'}}%</el-text></div>
                        </div> -->
                        <div class="mt-14px flex justify-around text-12px text-gray-400">
                          <div style="width: 33%;text-align:center"><el-text>平均湿度：</el-text></div>
                          <div style="width: 33%;text-align:center"><el-text>{{item.humAvgFront ? item.humAvgFront.toFixed(0) : '0'}}%</el-text></div>
                          <div style="width: 33%;text-align:center"><el-text>{{item.humAvgBlack ? item.humAvgBlack.toFixed(0) : '0'}}%</el-text></div>
                        </div>
                        <div style="position: absolute;bottom: 5px;right: 5px">
                          <div style="display: flex;height: 25px;justify-content: space-between">
                            <button v-if="editRoom" class="detail" @click="openSetting(item)" :style="props.isFromHome ? 'width:25px;height:20px;font-size:11px' : ''" >编辑</button>
                            <button class="detail" @click="handleRoomHome(item.id)" :style="props.isFromHome ? 'width:25px;height:20px;font-size:11px' : ''" >详情</button>
                          </div>
                        </div>
                      </el-card>
                    </div>
                  </div>
                </el-col>
              </template>
            </el-row>
        </el-skeleton>
        <el-skeleton :loading="loading" animated v-else-if="valueMode===2" style="padding: 0 20px">
            <el-row>
              <template v-if="roomShowType">
                <el-col 
                  :xl="24"
                  :lg="24"
                  :md="24"
                  :sm="24"
                  :xs="24">
                  <Echart :options="powOptionsData[i+1]" :height="400" :width="1702"/>
                </el-col>
              </template>
            </el-row>
        </el-skeleton>
      </el-collapse-item>
    </el-collapse>
    <div v-if="switchValue == 2">
      <el-table v-loading="loading" :data="deletedList" :stripe="true" :show-overflow-tooltip="true"  :border="true">
        <el-table-column label="编号" min-width="110" align="center">
            <template #default="scope">
              <div>{{scope.row.id}}</div>
            </template>
        </el-table-column>

        <el-table-column label="机房名称" min-width="110" align="center">
            <template #default="scope">
              <div>{{scope.row.roomName}}</div>
            </template>
        </el-table-column>
        
        <el-table-column label="删除日期" min-width="110" align="center">
          <template #default="scope">
              {{ (new Date(scope.row.updateTime)).toISOString().slice(0, 10) }}
            </template>
        </el-table-column>

        <el-table-column label="操作" align="center" width="120px">
          <template #default="scope">
            <el-button
              link
              type="danger"
              @click="handleRestore(scope.row.id)"
              style="background-color:#409EFF;color:#fff;border:none;width:90px;height:30px;"
            >
              恢复机房
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <Pagination
        :total="queryDeleteParams.pageTotal"
        v-model:page="queryDeleteParams.pageNo"
        v-model:limit="queryDeleteParams.pageSize"
        @pagination="handleStopDelete"
      />
    </div>
    <div v-if="switchValue == 3">
      <el-table v-if="valueMode == 0" v-loading="loading" :data="addrAllRoomList.flat()" :stripe="true" :show-overflow-tooltip="true"  @cell-dblclick="toDetail" :border="true">
        <el-table-column label="楼层" align="center" prop="addr" width="80px"/>
        <!-- 数据库查询 -->
        <el-table-column label="机房名" align="center" prop="roomName" width="300px"/>
        <el-table-column label="视在功率 (kVA)" align="center">
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.powApparent != null">
              {{ scope.row.powApparent.toFixed(1) }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="有功功率 (kW)" align="center">
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.powActive != null">
              {{ scope.row.powActive.toFixed(1) }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="无功功率 (kVar)" align="center">
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.powReactive != null">
              {{ scope.row.powReactive.toFixed(1) }}
            </el-text>
          </template>
        </el-table-column>
        
        <!-- 数据库查询 -->
        <el-table-column label="操作" align="center" width="180px">
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click="handleRoomHome(scope.row.id)"
              style="background-color:#409EFF;color:#fff;border:none;width:40px;height:30px;"
            >
            详情
            </el-button>
            <el-button
              link
              type="danger"
              v-if="editRoom"
              @click="openSetting(scope.row)"
              style="background-color:#5470c6;color:#fff;border:none;width:40px;height:30px;"
            >
              编辑
            </el-button>
            <el-button
              link
              type="danger"
              v-if="scope.row.flagType"
              @click="handleDelete(scope.row.id)"
              style="background-color:#fa3333;color:#fff;border:none;width:40px;height:30px;"
            >
              删除
            </el-button>
            <el-button v-else style="border:none;width:40px;height:30px;opacity: 0;cursor: default" />
          </template>
        </el-table-column>
      </el-table>

      <el-table v-if="valueMode == 1" v-loading="loading" :data="addrAllRoomList.flat()" :stripe="true" :show-overflow-tooltip="true"  @cell-dblclick="toDetail" :border="true">
        <el-table-column label="楼层" align="center" prop="addr" width="80px"/>
        <!-- 数据库查询 -->
        <el-table-column label="机房名" align="center" prop="roomName" width="300px"/>
        <el-table-column label="最高温度 (&deg;C)" align="center">
          <el-table-column label="前门" align="center">
            <template #default="scope" >
              <el-text line-clamp="2" v-if="scope.row.temMaxFront != null">
                {{ scope.row.temMaxFront.toFixed(1) }}
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="后门" align="center">
            <template #default="scope" >
              <el-text line-clamp="2" v-if="scope.row.temMaxBlack != null">
                {{ scope.row.temMaxBlack.toFixed(1) }}
              </el-text>
            </template>
          </el-table-column>
        </el-table-column>
        <el-table-column label="平均温度 (&deg;C)" align="center">
          <el-table-column label="前门" align="center">
            <template #default="scope" >
              <el-text line-clamp="2" v-if="scope.row.temAvgFront != null">
                {{ scope.row.temAvgFront.toFixed(1) }}
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="后门" align="center">
            <template #default="scope" >
              <el-text line-clamp="2" v-if="scope.row.temAvgBlack != null">
                {{ scope.row.temAvgBlack.toFixed(1) }}
              </el-text>
            </template>
          </el-table-column>
        </el-table-column>
        <el-table-column label="最高湿度 (%)" align="center">
          <el-table-column label="前门" align="center">
            <template #default="scope" >
              <el-text line-clamp="2" v-if="scope.row.humMaxFront != null">
                {{ scope.row.humMaxFront.toFixed(0) }}
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="后门" align="center">
            <template #default="scope" >
              <el-text line-clamp="2" v-if="scope.row.humMaxBlack != null">
                {{ scope.row.humMaxBlack.toFixed(0) }}
              </el-text>
            </template>
          </el-table-column>
        </el-table-column>
        <el-table-column label="平均湿度 (%)" align="center">
          <el-table-column label="前门" align="center">
            <template #default="scope" >
              <el-text line-clamp="2" v-if="scope.row.humAvgFront != null">
                {{ scope.row.humAvgFront.toFixed(0) }}
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="后门" align="center">
            <template #default="scope" >
              <el-text line-clamp="2" v-if="scope.row.humAvgBlack != null">
                {{ scope.row.humAvgBlack.toFixed(0) }}
              </el-text>
            </template>
          </el-table-column>
        </el-table-column>

        <!-- 数据库查询 -->
        <el-table-column label="操作" align="center" width="180px">
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click="handleRoomHome(scope.row.id)"
              style="background-color:#409EFF;color:#fff;border:none;width:40px;height:30px;"
            >
            详情
            </el-button>
            <el-button
              link
              type="danger"
              v-if="editRoom"
              @click="openSetting(scope.row)"
              style="background-color:#5470c6;color:#fff;border:none;width:40px;height:30px;"
            >
              编辑
            </el-button>
            <el-button
              link
              type="danger"
              v-if="scope.row.flagType"
              @click="handleDelete(scope.row.id)"
              style="background-color:#fa3333;color:#fff;border:none;width:40px;height:30px;"
            >
              删除
            </el-button>
            <el-button v-else style="border:none;width:40px;height:30px;opacity: 0;cursor: default" />
          </template>
        </el-table-column>
      </el-table>

      <el-table v-if="valueMode == 2" v-loading="loading" :data="addrAllRoomList.flat()" :stripe="true" :show-overflow-tooltip="true"  @cell-dblclick="toDetail" :border="true">
        <el-table-column label="楼层" align="center" prop="addr" width="80px"/>
        <!-- 数据库查询 -->
        <el-table-column label="机房名" align="center" prop="roomName" width="300px"/>
        <el-table-column label="视在功率 (kVA)" align="center">
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.powApparent != null">
              {{ scope.row.powApparent.toFixed(1) }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="有功功率 (kW)" align="center">
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.powActive != null">
              {{ scope.row.powActive.toFixed(1) }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="无功功率 (kVar)" align="center">
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.powReactive != null">
              {{ scope.row.powReactive.toFixed(1) }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="功率因数" align="center">
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.powerFactor != null">
              {{ scope.row.powerFactor.toFixed(2) }}
            </el-text>
          </template>
        </el-table-column>
        
        <!-- 数据库查询 -->
        <el-table-column label="操作" align="center" width="180px">
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click="handleRoomHome(scope.row.id)"
              style="background-color:#409EFF;color:#fff;border:none;width:40px;height:30px;"
            >
            详情
            </el-button>
            <el-button
              link
              type="danger"
              v-if="editRoom"
              @click="openSetting(scope.row)"
              style="background-color:#5470c6;color:#fff;border:none;width:40px;height:30px;"
            >
              编辑
            </el-button>
            <el-button
              link
              type="danger"
              v-if="scope.row.flagType"
              @click="handleDelete(scope.row.id)"
              style="background-color:#fa3333;color:#fff;border:none;width:40px;height:30px;"
            >
              删除
            </el-button>
            <el-button v-else style="border:none;width:40px;height:30px;opacity: 0;cursor: default" />
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <el-dialog v-model="dialogVisible" title="机房配置" width="30%" :before-close="handleDialogCancel">
      <el-form>
        <div style="margin-bottom: 5px">
          <el-text>机房</el-text>
        </div>
        <div class="double-formitem">
          <el-form-item label-width="60" style="flex: 0">
            <el-checkbox-button v-model="addrFlag" >
              分楼层
            </el-checkbox-button>
          </el-form-item>
          <el-form-item label="名称" label-width="60">
            <el-input v-model="rowColInfo.roomName" placeholder="请输入" />
          </el-form-item>
          <el-form-item v-if="addrFlag" label="楼层" prop="type" label-width="60">
            <el-select v-model="rowColInfo.addr" placeholder="请选择">
              <el-option v-for="(addr_item,addr_index) in addrList.slice(1)" :key="addr_index" :label="addr_item" :value="addr_item" />
            </el-select>
          </el-form-item>
        </div>
        <div style="margin-bottom: 5px">
          <el-text>拓扑</el-text>
        </div>
        <div v-if="!rowColInfo.areaFlag" class="double-formitem">
          <div style="width: 30%;display: flex;justify-content: space-between;margin-left: 60px;margin-bottom: 18px">
            <el-radio-group v-model="rowColInfo.areaFlag">
              <el-radio-button :label="true">面积</el-radio-button>
              <el-radio-button :label="false">地砖</el-radio-button>
            </el-radio-group>
          </div>
          <el-form-item label="行数" label-width="60">
            <el-input type="number" v-model="rowColInfo.row" :min="1" :max="100" controls-position="right" placeholder="请输入" />
          </el-form-item>
          <el-form-item label="列数" label-width="60">
            <el-input type="number" v-model="rowColInfo.col" :min="1" :max="100" controls-position="right" placeholder="请输入" />
          </el-form-item>
        </div>
        <div v-else class="double-formitem">
          <div style="width: 30%;display: flex;justify-content: space-between;margin-left: 60px;margin-bottom: 18px">
            <el-radio-group v-model="rowColInfo.areaFlag">
              <el-radio-button :label="true">面积</el-radio-button>
              <el-radio-button :label="false">地砖</el-radio-button>
            </el-radio-group>
          </div>
          <el-form-item label="宽度" label-width="60">
            <el-input type="number" v-model="rowColInfo.width" :min="1" :max="60" placeholder="请输入">
              <template #append>m</template>
            </el-input>
          </el-form-item>
          <el-form-item label="长度" label-width="60">
            <el-input type="number" v-model="rowColInfo.length" :min="1" :max="60" placeholder="请输入">
              <template #append>m</template>
            </el-input>
          </el-form-item>
        </div>
        
        <div style="margin-bottom: 5px">
          <el-text>容量</el-text>
        </div>
        <!-- <el-form-item label="非IT设备总额定功率" label-width="160">
          <el-input v-model="rowColInfo.airPower" placeholder="包括制冷系统（如空调、冷源设备、新风系统等）">
            <template #append>kVA</template>
          </el-input>
        </el-form-item> -->
        <div class="double-formitem">
          <el-form-item label-width="60" style="flex: 0">
            <el-checkbox-button v-model="rowColInfo.displayFlag" >
              负载率
            </el-checkbox-button>
          </el-form-item>
          <el-form-item v-if="rowColInfo.displayFlag" label="电力容量" label-width="160">
            <el-input type="number" v-model="rowColInfo.powerCapacity" placeholder="请输入">
              <template #append>kVA</template>
            </el-input>
          </el-form-item>
        </div>

        

        <div style="margin-bottom: 5px">
          <el-text>排序</el-text>
        </div>
        <div class="double-formitem">
          <el-form-item label-width="60" style="flex: 0">
            <el-checkbox-button v-model="rowColInfo.displayFlag">
              {{rowColInfo.displayFlag ? '已启用' : '未启用'}}
            </el-checkbox-button>
          </el-form-item>
          <el-form-item v-if="rowColInfo.displayFlag" label="排序序号" label-width="160">
            <el-input type="number" v-model="rowColInfo.powerCapacity" placeholder="请输入" />
          </el-form-item>
        </div>

        <!-- <div class="double-formitem">
          <el-form-item label="日用能告警" label-width="90">
            <el-switch v-model="rowColInfo.eleAlarmDay" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <el-form-item label="日用能限制" label-width="90">
            <el-input-number v-model="rowColInfo.eleLimitDay" :min="0" :max="9999" controls-position="right" placeholder="请输入" />
          </el-form-item>
        </div>
        <div class="double-formitem">
          <el-form-item label="月用能告警" label-width="90">
            <el-switch v-model="rowColInfo.eleAlarmMonth" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <el-form-item label="月用能限制" label-width="90">
            <el-input-number v-model="rowColInfo.eleLimitMonth" :min="0" :max="9999" controls-position="right" placeholder="请输入" />
          </el-form-item>
        </div> -->
      </el-form>
      <template #footer>
        <el-button @click="handleDialogCancel">取 消</el-button>
        <el-button type="primary" @click="submitSetting">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import type { CollapseModelValue,ElMessageBox  } from 'element-plus'
import * as echarts from 'echarts';
import { formatTime } from '@/utils'
import { MachineRoomApi } from '@/api/cabinet/room'
import { MachineHomeApi } from '@/api/cabinet/home'
import { number } from 'vue-types';
import { use } from 'echarts/core';
import { DataZoomComponent } from 'echarts/components';

use([DataZoomComponent]); // 提前注册

const currentLegendSelected = ref({})
const powChartContainer = ref([]);
let powChart = []; 
const activeNames = ref()
const valueMode = ref(0)
const switchValue = ref(0)
const roomFlag =ref();
const dialogVisible = ref(false);
const deletedList = ref<any>([]) //已删除的
const roomId = ref(0) // 房间id
const radio = ref("负载率")
const radioareaFlag = ref('"0"')
const rowColInfo = reactive({
  roomName: '', // 机房名
  addr: '一楼', //楼层
  row: 14, // 行
  col: 18, // 列
  areaFlag: true, //新建类型 砖数 面积
  width: 8.4, //宽度
  length: 10.8, //长度
  powerCapacity:0, //电力容量
  airPower: null, //空调额定功率
  displayType: 0, //0负载率 1PUE
  displayFlag: 0, // 显示选择
  eleAlarmDay: 0, // 日用能告警
  eleLimitDay: 1000, // 日用能限制
  eleAlarmMonth: 0, // 月用能告警
  eleLimitMonth: 1000, // 月用能限制
})
const queryParams = reactive({
  roomName: undefined,
})as any

const addrFlag = ref(false)

const searchRoomName = ref("")

const flashListTimer = ref();

const loading = ref(true)
const roomShowType = ref(true)
const powOptionsData = ref([{}])
const powOptionsDataOne = reactive<EChartsOption>({}) as EChartsOption
const powInfo = reactive({}) // 功率数据信息
const powCopyInfo = reactive({})
const roomAddrList = ref(['未区分'])
const addrAllRoomList = ref([[]])
const addrAllPowChartOptions = ref([[{}],[{}],[{}],[{}],[{}],[{}],[{}],[{}],[{}],[{}]])
const clickIndex = ref(0)

const editRoom = ref(false)

const emit = defineEmits(['backData']) 

const queryDeleteParams = reactive({
  roomName: undefined,
  pageNo: 1,
  pageSize: 20,
  pageTotal: 0,
})

const addrList = ref([
  '未区分',
  '一楼',
  '二楼',
  '三楼',
  '四楼',
  '五楼',
  '六楼',
  '七楼',
  '八楼',
  '九楼',
  '十楼',
]) // 楼层


const { push } = useRouter() // 路由跳转
const message = useMessage() // 消息弹窗

const props = defineProps({
  isFromHome: {
    type: Boolean,
    default: false,
  },
  valueButton: {
    type: Number,
    default: 0,
  }
})

const openSetting = (item) => {
  roomFlag.value = 2;
  console.log(item)
  Object.assign(rowColInfo, {
    roomName: item.roomName,
    row: item.yLength,
    col: item.xLength,
    areaFlag: item.areaFlag,
    width: item.areayLength ? item.areayLength : 8.4,
    length: item.areaxLength ? item.areaxLength : 10.8,
    powerCapacity:item.powerCapacity,
    addr: item.addr,
    airPower:item.airPower ? item.airPower : 0,
    displayType: item.displayType ? 1 : 0, //0负载率 1PUE
    displayFlag: item.displayFlag ? 1 : 0,
    eleAlarmDay: item.eleAlarmDay,
    eleLimitDay: item.eleLimitDay,
    eleAlarmMonth: item.eleAlarmMonth,
    eleLimitMonth: item.eleLimitMonth,
  })
  radio.value = item.displayType ? "PUE" : "负载率"
  roomId.value = item.id
  dialogVisible.value = true;
}

const handleRoomHome = (id) => {
  console.log(id)
  push({path: '/room/roommonitor/home', query: { roomId: id }})
}

const toDetail = (e) => {
  push({path: '/room/roommonitor/home', query: { roomId: e.id }})
}

/** 搜索按钮操作 */
const handleQuery = async () => {
  queryDeleteParams.pageNo = 1
  queryParams.roomName = searchRoomName.value
  queryDeleteParams.roomName = queryParams.roomName
  getAllApi()
  handleStopDelete()
  // const res = await MachineRoomApi.getAddrAllRoomList(queryParams)
  // console.log(res)
  // if(res) {
  //   addrAllRoomList.value = addrAllRoomList.value.map(() => [])
  //   addrAllPowChartOptions.value = addrAllPowChartOptions.value.map(() => [{}])
  //   activeNames.value = []
  //   res.forEach((ele,i) => {
  //     let index = roomAddrList.value.findIndex(item => item == ele.addr)
  //     if(index != -1) {
  //       addrAllRoomList.value[index].push(ele)
  //       if(ele.addr != '未区分')
  //       activeNames.value.push(index-1)
  //     }
  //   })
  //   activeNames.value = [...new Set(activeNames.value)]
  // }
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryParams.roomName = undefined;
  searchRoomName.value = ''
  queryDeleteParams.roomName = queryParams.roomName
  handleStopDelete()
  getAllApi()
}

// 处理弹窗取消事件
const handleDialogCancel = () => {
  dialogVisible.value = false;
}

// 处理点击添加机房事件
const handleAdd = () => {
  roomFlag.value = 1;
  dialogVisible.value = true;
  resetForm();
}

// 处理点击删除机房事件
const handleDelete = (id) => {
  console.log(id)
  ElMessageBox.confirm('确认删除机房吗？', '提示', {
    confirmButtonText: '确 认',
    cancelButtonText: '取 消',
    type: 'warning'
  }).then(async () => {
    const res = await MachineRoomApi.deleteRoom({id})
    console.log('handleDelete', res)
    message.success('删除成功')
    for(let index = 0;index < addrAllRoomList.value.length;index++) {
      let deleteIndex = addrAllRoomList.value[index].findIndex(ele => ele.id == id)
      if(deleteIndex != -1) {
        addrAllRoomList.value[index].splice(deleteIndex,1)
        addrAllPowChartOptions.value[index].splice(deleteIndex,1)
        break
      }
    }
    // getRoomAddrList()
  })
}

//已删除
const handleStopDelete = async() =>{
  const res = await MachineRoomApi.deletedRoomInfo(queryDeleteParams)
  deletedList.value = res.list;
  queryDeleteParams.pageTotal = res.total;
}

//恢复机房
const handleRestore = async (flagRoomid) => {
  ElMessageBox.confirm('确认恢复机房吗？', '提示', {
    confirmButtonText: '确 认',
    cancelButtonText: '取 消',
    type: 'warning'
  }).then(async () => {
    const res = await MachineRoomApi.restoreRoomInfo({id: flagRoomid});
    if(res != null || res != "") {
      message.success('恢复成功')
      deletedList.value = deletedList.value.filter(item => item.id != flagRoomid)
      getRoomAddrList()
    } else {
      message.error('恢复失败')
    }
  })
}

// 重置表单
const resetForm = () => {
  Object.assign(rowColInfo, {
    roomName: '', // 机房名
    addr: '一楼', //楼层
    row: 14, // 行
    col: 18, // 列,
    areaFlag: true, //新建类型 砖数 面积
    width: 8.4, //宽度
    length: 10.8, //长度
    powerCapacity:0,
    airPower:null,
    displayType: 0, //0负载率 1PUE
    displayFlag: 0, // 显示选择
    eleAlarmDay: 0, // 日用能告警
    eleLimitDay: 1000, // 日用能限制
    eleAlarmMonth: 0, // 月用能告警
    eleLimitMonth: 1000, // 月用能限制
  })
  radio.value = "负载率"
  addrFlag.value = false
}

// 处理设置提交
const submitSetting = async() => {
  if(rowColInfo.roomName == '') {
    message.error('机房名称不能为空,请输入!')
    return
  } else if(rowColInfo.powerCapacity === "") {
    message.error('机房电力容量不能为空,请输入!')
    return
  }

  if(roomFlag.value == 1){
    const resSelect = await MachineRoomApi.selectRoomByName({name: rowColInfo.roomName});
    if(resSelect != null){
      message.error('该机房名称已存在,请重新输入!');
      rowColInfo.roomName = '';
      return
    }
  }
  

  let roomFlagId:any = null;
  let messageRoomFlag = "保存成功！";
  if(roomFlag.value == 2){
    roomFlagId = roomId.value; 
    messageRoomFlag = "修改成功！";
  }

  if(radio.value === "PUE") {
  rowColInfo.displayType = 1
  } else {
    rowColInfo.displayType = 0
  }
  console.log(rowColInfo)
    if(rowColInfo.width <= 0 || rowColInfo.length <= 0 || rowColInfo.width > 60 || rowColInfo.length > 60) {
      message.error('机房面积有误或过大,请重新输入!')
      return
    }

    if(rowColInfo.areaFlag == false) {
      rowColInfo.width = Number((rowColInfo.row * 0.6).toFixed(1))
      rowColInfo.length = Number((rowColInfo.col * 0.6).toFixed(1))
    } else {
      rowColInfo.width = Number(rowColInfo.width)
      rowColInfo.length = Number(rowColInfo.length)
      rowColInfo.row = Math.ceil((rowColInfo.width * 10)/6)
      rowColInfo.col = Math.ceil((rowColInfo.length * 10)/6)
    }

    console.log(rowColInfo,roomFlagId)

  if(roomFlag.value == 2) {
    const resFind = await MachineRoomApi.findAreaById({xLength: rowColInfo.col,yLength: rowColInfo.row,id: roomFlagId});
    if(resFind) {
      message.error('减少的行数或列数中有机柜或柜列,请重新设置行数或列数!')
      return
    }
  }

   try {
    const res = await MachineRoomApi.saveRoomDetail({
      id: roomFlagId,
      roomName: rowColInfo.roomName,
      addr: rowColInfo.addr,
      xLength: rowColInfo.col,
      yLength: rowColInfo.row,
      areaFlag: rowColInfo.areaFlag, //新建类型 砖数 面积
      areayLength: rowColInfo.width, //宽度
      areaxLength: rowColInfo.length, //长度
      powerCapacity:rowColInfo.powerCapacity, 
      airPower:rowColInfo.airPower, 
      displayType: rowColInfo.displayType, 
      displayFlag: rowColInfo.displayFlag, 
      eleAlarmDay: rowColInfo.eleAlarmDay,
      eleAlarmMonth: rowColInfo.eleAlarmMonth,
      eleLimitDay: rowColInfo.eleLimitDay,
      eleLimitMonth: rowColInfo.eleLimitMonth,
    })
    
    if(res != null || res != "")
    message.success(messageRoomFlag);
    dialogVisible.value = false;
    roomId.value = res;
   } catch (error) {
    console.log(error)
   }
   
   getRoomAddrList();
}

watch(() => rowColInfo.areaFlag, (val) => {
  if(val == true) {
    rowColInfo.width = Number((rowColInfo.row * 0.6).toFixed(1))
    rowColInfo.length = Number((rowColInfo.col * 0.6).toFixed(1))
  } else {
    rowColInfo.width = Number(rowColInfo.width)
    rowColInfo.length = Number(rowColInfo.length)
    rowColInfo.row = Math.ceil((rowColInfo.width * 10)/6)
    rowColInfo.col = Math.ceil((rowColInfo.length * 10)/6)
  }
})

//获取机房楼层
const getRoomAddrList = async() => {
  const res =  await MachineRoomApi.getRoomAddrListAll({})
  if(res) {
    roomAddrList.value = Object.keys(res) 
    .filter(key => addrList.value.includes(key)) 
    .sort((a, b) => addrList.value.indexOf(a) - addrList.value.indexOf(b));

    roomAddrList.value.forEach((item,index) => {
      if(queryParams.roomName) {
        getAddrAllRoomList(res[item].filter(item => 
          item.roomName.toLowerCase().includes(queryParams.roomName.toLowerCase())
        ),index)
      } else {
        getAddrAllRoomList(res[item],index)
      }
    })

    emit('backData', res)
  }
}

const getAddrAllRoomList = (roomList,index) => {
  addrAllRoomList.value[index] = roomList

  addrAllRoomList.value[index].forEach((item,i) => {
    addrAllPowChartOptions.value[index][i] = {
      tooltip: {
        trigger: 'item',
        formatter: function (param) {
          let result = ''
          result += param.name + ':' + param.value;
          if (param.name === '视在功率') {
            result += 'kVA';
          } else if(param.name === '有功功率') {
            result += 'kW'
          } else if(param.name === '无功功率') {
            result += 'kVar'
          }
          
          return result.trimEnd(); // 去除末尾多余的换行符
        }
      },
      series: [
        {
          type: 'pie',
          radius: ['55%', '90%'],
          label: {
            show: false,
          },
          data: [
            { value: item.powApparent ? item.powApparent.toFixed(3) : '0.000', name: '视在功率', itemStyle: { color: '#E5B849' } },
            { value: item.powActive ? item.powActive.toFixed(3) : '0.000', name: '有功功率', itemStyle: { color: '#C8603A' } },
            { value: item.powReactive ? item.powReactive.toFixed(3) : '0.000', name: '无功功率', itemStyle: { color: '#AD3762' } },
          ]
        }
      ]
    }
  })
  // initPowChart(index)
  if(loading.value) {
    powOptionsData.value[index] = {}
    Object.assign(powOptionsData.value[index], {
      grid: {
        left: 50,
        right: 20,
        bottom: 20
      },
      legend: {
        right: 10,
        selectedMode: 'single'
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        },
        formatter: function (params) {
          // console.log('params', params)
          let result = '';
          params.forEach(function (item) {
            // item 是每一个系列的数据
            const seriesName = item.seriesName; // 系列名称
            const value = item.value; // 数据值
            const marker = item.marker; // 标志图形
            let unit = ''
            if (seriesName == '有功功率') {
              unit = 'kW'
            } else if (seriesName == '无功功率') {
              unit = 'kVar'
            } else if (seriesName == '视在功率') {
              unit = 'kVA'
            }
            result += `${marker}${seriesName}: ${value}${unit}<br/>`;
          });
          return result;
        }
      },
      xAxis: {
        type: 'category',
        data: addrAllRoomList.value[index].map(item => item.roomName)
      },
      yAxis: {
        type: 'value',
      },
      series: [
        {
          name: '视在功率',
          data: addrAllRoomList.value[index].map(item => item.powApparent ? item.powApparent : 0),
          type: 'bar',
          barWidth: 30, // 固定柱宽为 30 像素
          label: {
            show: true,
            position: 'top', // 顶部显示
            formatter: '{c}kVA', // 显示数据值
          },
          itemStyle: { color: '#AD3762' }
        },
        {
          name: '有功功率',
          data: addrAllRoomList.value[index].map(item => item.powActive ? item.powActive : 0),
          type: 'bar',
          barWidth: 30, // 固定柱宽为 30 像素
          label: {
            show: true,
            position: 'top', // 顶部显示
            formatter: '{c}kW', // 显示数据值
          },
          itemStyle: { color: '#E5B849' }
        },
        {
          name: '无功功率',
          data: addrAllRoomList.value[index].map(item => item.powReactive ? item.powReactive : 0),
          type: 'bar',
          barWidth: 30, // 固定柱宽为 30 像素
          label: {
            show: true,
            position: 'top', // 顶部显示
            formatter: '{c}kVar', // 显示数据值
          },
          itemStyle: { color: '#C8603A' }
        },
        {
          name: '功率因素',
          data: addrAllRoomList.value[index].map(item => item.powerFactor ? item.powerFactor : 0),
          type: 'bar',
          barWidth: 30, // 固定柱宽为 30 像素
          label: {
            show: true,
            position: 'top', // 顶部显示
            formatter: '{c}', // 显示数据值
          },
        },
      ]
    })
  }
  // else {
  //   powOptionsData.value[index].series[0].data = addrAllRoomList.value[index].map(item => item.powApparent ? item.powApparent : 0)
  //   powOptionsData.value[index].series[1].data = addrAllRoomList.value[index].map(item => item.powActive ? item.powActive : 0)
  //   powOptionsData.value[index].series[2].data = addrAllRoomList.value[index].map(item => item.powReactive ? item.powReactive : 0)
  //   powOptionsData.value[index].series[3].data = addrAllRoomList.value[index].map(item => item.powerFactor ? item.powerFactor : 0)
  // }
  
}

const legendSelectChanged = (params) => {
  console.log(params)
  currentLegendSelected.value = params.selected
  // if(powChartContainer[index].value) {
  //   if (!powChart) {
  //     powChart[index] = echarts.init(powChartContainer[index].value);
  //   }
  //   console.log(powChart[index])
  //   powChart[index].setOption(
  //         {
  //       grid: {
  //         left: 50,
  //         right: 20,
  //         bottom: 20
  //       },
  //       legend: {
  //         right: 10,
  //         selectedMode: 'single',
  //         selected: currentLegendSelected
  //       },
  //       tooltip: {
  //         trigger: 'axis',
  //         axisPointer: {
  //           type: 'shadow'
  //         },
  //         formatter: function (params) {
  //           console.log('params', params)
  //           let result = '';
  //           params.forEach(function (item) {
  //             // item 是每一个系列的数据
  //             const seriesName = item.seriesName; // 系列名称
  //             const value = item.value; // 数据值
  //             const marker = item.marker; // 标志图形
  //             let unit = ''
  //             if (seriesName == '有功功率') {
  //               unit = 'kW'
  //             } else if (seriesName == '无功功率') {
  //               unit = 'kVar'
  //             } else if (seriesName == '视在功率') {
  //               unit = 'kVA'
  //             }
  //             result += `${marker}${seriesName}: ${value}${unit}<br/>`;
  //           });
  //           return result;
  //         }
  //       },
  //       xAxis: {
  //         type: 'category',
  //         data: addrAllRoomList.value[index].map(item => item.roomName)
  //       },
  //       yAxis: {
  //         type: 'value',
  //       },
  //       series: [
  //         {
  //           name: '视在功率',
  //           data: addrAllRoomList.value[index].map(item => item.powApparent ? item.powApparent : 0),
  //           type: 'bar',
  //           barWidth: 30, // 固定柱宽为 30 像素
  //           label: {
  //             show: true,
  //             position: 'top', // 顶部显示
  //             formatter: '{c}kVA', // 显示数据值
  //           },
  //           itemStyle: { color: '#AD3762' }
  //         },
  //         {
  //           name: '有功功率',
  //           data: addrAllRoomList.value[index].map(item => item.powActive ? item.powActive : 0),
  //           type: 'bar',
  //           barWidth: 30, // 固定柱宽为 30 像素
  //           label: {
  //             show: true,
  //             position: 'top', // 顶部显示
  //             formatter: '{c}kW', // 显示数据值
  //           },
  //           itemStyle: { color: '#E5B849' }
  //         },
  //         {
  //           name: '无功功率',
  //           data: addrAllRoomList.value[index].map(item => item.powReactive ? item.powReactive : 0),
  //           type: 'bar',
  //           barWidth: 30, // 固定柱宽为 30 像素
  //           label: {
  //             show: true,
  //             position: 'top', // 顶部显示
  //             formatter: '{c}kVar', // 显示数据值
  //           },
  //           itemStyle: { color: '#C8603A' }
  //         },
  //         {
  //           name: '功率因素',
  //           data: addrAllRoomList.value[index].map(item => item.powerFactor ? item.powerFactor : 0),
  //           type: 'bar',
  //           barWidth: 30, // 固定柱宽为 30 像素
  //           label: {
  //             show: true,
  //             position: 'top', // 顶部显示
  //             formatter: '{c}', // 显示数据值
  //           },
  //         },
  //       ]
  //     }
  //   )
  // }
}

const getAllApi = async () => {
  await getRoomAddrList()
  activeNames.value = []
  roomAddrList.value.forEach(async (item,index) => {
    activeNames.value.push(index)
  })
  loading.value = false
}

const openAllCollapse = () => {
  activeNames.value = []
  roomAddrList.value.forEach((item,index) => {
    activeNames.value.push(index)
  })
}

const scrollableContainer = ref(null); // 挂载到机房状态
 
let scrollInterval; // 机房状态的定时器
let scrollTimeout; // 用于检测滚动是否停止的延迟定时器
let isScrollingManually = false; // 标记是否正在手动滚动

const startScrolling = () => {
  // 检查是否已经有一个定时器在运行
  if (scrollInterval || isScrollingManually) return;
 
  scrollInterval = setInterval(() => {
    // 机房状态的滚动逻辑
    scrollContainer('scrollableContainer');
  }, 3000);
};
 
const scrollContainer = (containerName) => {
  let containerRef, interval;
  if (containerName === 'scrollableContainer') {
    containerRef = scrollableContainer;
    interval = scrollInterval;
  }

  // 检查 containerRef.value 是否存在，用来解决控制台报错
  if (!containerRef?.value) {
    return; // 可以返回一个特定的值或对象来表示错误
  }
 
  const { scrollTop, clientHeight, scrollHeight } = containerRef.value;
  const scrollStep = 10; // 滚动步长
  const scrollTolerance = -10; // 停止前的容忍范围
 
  if (scrollTop + clientHeight >= scrollHeight) {
    // 滚动到顶部
    containerRef.value.scrollTop = 0;
  } else if (scrollTop + scrollStep + scrollTolerance >= scrollHeight - clientHeight) {
    // 接近底部时停止定时器
    clearInterval(interval);
    if (containerName === 'scrollableContainer') {
      scrollInterval = null;
    }
  } else {
    // 继续滚动
    containerRef.value.scrollTop += scrollStep;
  }
};
 
const stopScrolling = () => {
  clearInterval(scrollInterval);
  scrollInterval = null;
};

const handleScroll = (event, containerName) => {
  // 停止自动滚动
  isScrollingManually = true;
  stopScrolling();
 
  // 设置延迟来判断滚动是否停止
  clearTimeout(scrollTimeout);
  scrollTimeout = setTimeout(() => {
    // 如果在延迟期间没有新的滚动事件，则恢复自动滚动
    isScrollingManually = false;
    startScrolling();
  }, 1000); // 延迟时间，单位毫秒，可以根据需要调整
};

watch( ()=> props.valueButton, (val) => {
  console.log(props.valueButton)
  valueMode.value = val
})

getAllApi()

onMounted(() => {
  flashListTimer.value = setInterval((getRoomAddrList), 5000);
  if(props.isFromHome) {
    // 添加滚动事件监听器
    if (scrollableContainer.value) {
      scrollableContainer.value.addEventListener('scroll', (event) => handleScroll(event, 'scrollableContainer'));
    }

    // 初始启动自动滚动
    startScrolling();
  }
})

onBeforeUnmount(() => {
  if(flashListTimer.value){
    clearInterval(flashListTimer.value)
    flashListTimer.value = null;
  }
  if(props.isFromHome) {
    // 移除滚动事件监听器
    if (scrollableContainer.value) {
      scrollableContainer.value.removeEventListener('scroll', (event) => handleScroll(event, 'scrollableContainer'));
    }

    // 确保在组件卸载时清除定时器
    stopScrolling();
  }
})

onBeforeRouteLeave(()=>{
  if(flashListTimer.value){
    clearInterval(flashListTimer.value)
    flashListTimer.value = null;
  }
})

const handleChange = async (val: CollapseModelValue) => {
  console.log(val)
}

</script>

<style scoped lang="scss">
.arrayContainer {
  display: flex;
  flex-wrap: wrap;
  padding: 0 20px;
  background-color: #fff;
  .arrayItem {
    width: 25%;
    font-size: 13px;
    box-sizing: border-box;
    border: 5px solid #fff;
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
      cursor: pointer;
      margin-right: 3px;
    }
  }
}
.bullet {
  display: inline-block;
  margin-right: 5px;
  border-radius: 50%;
  width: 10px;
  height: 10px;
}
.btns {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 5px;
}
.double-formitem {
  display: flex;
  align-items: center;
  & > div {
    flex: 1;
  }
}

.percentage-value {
  display: block;
  margin-top: 36px;
  font-size: 14px;
}
.percentage-label {
  display: block;
  margin-top: 10px;
  font-size: 10px;
}

:deep(.el-collapse-item__header) {
  padding: 0 20px;
}
:deep(.el-card) {
  background-color: #fff;
}
:deep .el-input-group__append {
  padding: 0 10px; /* 设置为所需的字体大小 */
}
:deep .el-table thead th.el-table__cell {
  background: var(--el-fill-color-light);
}
:deep .el-pagination {
  justify-content: flex-end;
  float: none;
}
:deep(.el-card__body) {
  padding: 10px 10px 30px 10px;
}
</style>