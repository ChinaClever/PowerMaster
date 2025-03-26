<template>
  <div class="demo-collapse">
    <div style="display: flex;align-items: center;justify-content: space-between">
      <el-form
        class="-mb-15px"
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        label-width="68px"                          
      >
        <div>
          <el-form-item label="机房名" prop="devKey">
            <el-input
              v-model="queryParams.roomName"
              placeholder="请输入机房名"
              clearable
              class="!w-160px"
              height="35"
            />
          </el-form-item>
          <el-form-item style="margin-left: 10px;">
            <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
            <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
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
      <div class="btns">
        <el-button @click="valueMode = 0;" :type="valueMode == 0 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />机房功率</el-button>                             
        <el-button @click="valueMode = 1;" :type="valueMode == 1 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />机房温度</el-button>            
        <el-button @click="valueMode = 2;switchValue = 0" :type="valueMode == 2 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />机房对比</el-button>    
        <el-button @click="handleAdd"><Icon icon="ep:grid" style="margin-right: 4px" />新建机房</el-button>        
        <el-button @click="switchValue = 0;" :type="switchValue == 0 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />阵列模式</el-button>
        <el-button @click="switchValue = 3;" :type="switchValue == 3 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 4px" />表格模式</el-button>
        <el-button @click="handleStopDelete();switchValue = 2;" :type="switchValue ===2 ? 'primary' : ''" v-show="switchValue ===3"><Icon icon="ep:expand" style="margin-right: 8px" />已删除</el-button>
      </div>
    </div>
    <div v-if="switchValue == 0" style="padding: 20px 0;background-color: #fff">
      <el-skeleton :loading="loading" animated v-if="valueMode===0" style="padding: 0 20px">
        <el-row>
          <template v-if="roomShowType">
            <el-col>
              <div ref="scrollableContainer" class="arrayContainer">
                <div 
                  class="arrayItem"
                  v-for="(item, index) in addrAllRoomList[0]"
                  :key="`card-${index}`"
                >
                  <el-card shadow="hover">
                    <div class="flex items-center h-21px mb-2">
                      <!-- <Icon :icon="item.icon" :size="25" class="mr-8px" /> -->
                      <span class="text-15px">{{ item.roomName || '' }}</span>
                    </div>
                    <div style="display: flex;justify-content: space-around;align-items: center;">
                      <div style="">
                        <div><el-text>视在功率：{{item.powApparent ? item.powApparent.toFixed(3) : '0.000'}}kVA</el-text></div>
                        <div><el-text>有功功率：{{item.powActive ? item.powActive.toFixed(3) : '0.000'}}kW</el-text></div>
                        <div><el-text>无功功率：{{item.powReactive ? item.powReactive.toFixed(3) : '0.000'}}kVAr</el-text></div>
                      </div>
                      <div style="display: flex;flex-direction: column">
                        <div v-if="item.displayFlag" style="text-align: center;font-size: 24px;">{{item.displayType ? (item.pue ? item.pue : 0) : (item.loadRate ? item.loadRate : 0)}}</div>
                        <div v-if="item.displayFlag" style="text-align: center;font-size: 12px;">{{item.displayType ? "PUE" : "负载率"}}</div>
                      </div>
                    </div>
                  </el-card>
                  <div style="position: absolute;bottom: 0;right: 0">
                    <button class="detail" @click="handleRoomHome(item.id)" >详情</button>
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
              <div ref="scrollableContainer" class="arrayContainer">
                <div 
                  class="arrayItem"
                  v-for="(item, index) in addrAllRoomList[0]"
                  :key="`card-${index}`"
                >
                  <el-card shadow="hover" @dblclick="handleRoomHome(item.id)">
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
                    <div class="mt-14px flex justify-around text-12px text-gray-400">
                      <div style="width: 33%;text-align:center"><el-text>最高湿度：</el-text></div>
                      <div style="width: 33%;text-align:center"><el-text>{{item.humMaxFront ? item.humMaxFront.toFixed(0) : '0'}}%</el-text></div>
                      <div style="width: 33%;text-align:center"><el-text>{{item.humMaxBlack ? item.humMaxBlack.toFixed(0) : '0'}}%</el-text></div>
                    </div>
                    <div class="mt-14px flex justify-around text-12px text-gray-400">
                      <div style="width: 33%;text-align:center"><el-text>平均湿度：</el-text></div>
                      <div style="width: 33%;text-align:center"><el-text>{{item.humAvgFront ? item.humAvgFront.toFixed(0) : '0'}}%</el-text></div>
                      <div style="width: 33%;text-align:center"><el-text>{{item.humAvgBlack ? item.humAvgBlack.toFixed(0) : '0'}}%</el-text></div>
                    </div>
                    <div style="position: absolute;bottom: 0;right: 0">
                      <button class="detail" @click="handleRoomHome(item.id)" >详情</button>
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
                <div ref="scrollableContainer" class="arrayContainer">
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
                  <div ref="scrollableContainer" class="arrayContainer">
                    <div 
                      class="arrayItem"
                      v-for="(item, index) in addrAllRoomList[i+1]"
                      :key="`card-${index}`"
                    >
                      <el-card shadow="hover">
                        <div class="flex items-center h-21px mb-2">
                          <!-- <Icon :icon="item.icon" :size="25" class="mr-8px" /> -->
                          <span class="text-15px">{{ item.roomName || '' }}</span>
                        </div>
                        <div style="display: flex;justify-content: space-around;align-items: center;">
                          <div style="">
                            <div><el-text>视在功率：{{item.powApparent ? item.powApparent.toFixed(3) : '0.000'}}kVA</el-text></div>
                            <div><el-text>有功功率：{{item.powActive ? item.powActive.toFixed(3) : '0.000'}}kW</el-text></div>
                            <div><el-text>无功功率：{{item.powReactive ? item.powReactive.toFixed(3) : '0.000'}}kVAr</el-text></div>
                          </div>
                          <div style="display: flex;flex-direction: column">
                            <div v-if="item.displayFlag" style="text-align: center;font-size: 24px;">{{item.displayType ? (item.pue ? item.pue : 0) : (item.loadRate ? item.loadRate : 0)}}</div>
                            <div v-if="item.displayFlag" style="text-align: center;font-size: 12px;">{{item.displayType ? "PUE" : "负载率"}}</div>
                          </div>
                        </div>
                      </el-card>
                      <div style="position: absolute;bottom: 0;right: 0">
                        <button class="detail" @click="handleRoomHome(item.id)" >详情</button>
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
                  <div ref="scrollableContainer" class="arrayContainer" @scroll="handleScroll">
                    <div 
                      class="arrayItem"
                      v-for="(item, index) in addrAllRoomList[i+1]"
                      :key="`card-${index}`"
                    >
                      <el-card shadow="hover" @dblclick="handleRoomHome(item.id)">
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
                        <div class="mt-14px flex justify-around text-12px text-gray-400">
                          <div style="width: 33%;text-align:center"><el-text>最高湿度：</el-text></div>
                          <div style="width: 33%;text-align:center"><el-text>{{item.humMaxFront ? item.humMaxFront.toFixed(0) : '0'}}%</el-text></div>
                          <div style="width: 33%;text-align:center"><el-text>{{item.humMaxBlack ? item.humMaxBlack.toFixed(0) : '0'}}%</el-text></div>
                        </div>
                        <div class="mt-14px flex justify-around text-12px text-gray-400">
                          <div style="width: 33%;text-align:center"><el-text>平均湿度：</el-text></div>
                          <div style="width: 33%;text-align:center"><el-text>{{item.humAvgFront ? item.humAvgFront.toFixed(0) : '0'}}%</el-text></div>
                          <div style="width: 33%;text-align:center"><el-text>{{item.humAvgBlack ? item.humAvgBlack.toFixed(0) : '0'}}%</el-text></div>
                        </div>
                        <div style="position: absolute;bottom: 0;right: 0">
                          <button class="detail" @click="handleRoomHome(item.id)" >详情</button>
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
      <el-table v-loading="loading" :data="deletedList" :stripe="true" :show-overflow-tooltip="true"  :border=true>
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
      <el-table v-if="switchValue == 3 && valueMode == 0" v-loading="loading" :data="addrAllRoomList.flat()" :stripe="true" :show-overflow-tooltip="true"  @cell-dblclick="toDetail" :border="true">
        <el-table-column label="楼层" align="center" prop="addr" width="80px"/>
        <!-- 数据库查询 -->
        <el-table-column label="机房名" align="center" prop="roomName" width="300px"/>
        <el-table-column label="视在功率 (KVA)" align="center" prop="powApparent">
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.powApparent != null">
              {{ scope.row.powApparent.toFixed(3) }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="有功功率 (KW)" align="center" prop="AIb">
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.powActive != null">
              {{ scope.row.powActive.toFixed(3) }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="无功功率 (KVAR)" align="center" prop="BIb">
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.powReactive != null">
              {{ scope.row.powReactive.toFixed(3) }}
            </el-text>
          </template>
        </el-table-column>
        
        <!-- 数据库查询 -->
        <el-table-column label="操作" align="center" width="140px">
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
              @click="handleDelete(scope.row.id)"
              style="background-color:#fa3333;color:#fff;border:none;width:40px;height:30px;"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-table v-if="switchValue == 3 && valueMode == 1" v-loading="loading" :data="addrAllRoomList.flat()" :stripe="true" :show-overflow-tooltip="true"  @cell-dblclick="toDetail" :border="true">
        <el-table-column label="楼层" align="center" prop="addr" width="80px"/>
        <!-- 数据库查询 -->
        <el-table-column label="机房名" align="center" prop="roomName" width="300px"/>
        <el-table-column label="最高温度 (&deg;C)" align="center">
          <el-table-column label="前门" align="center" prop="AUa" >
            <template #default="scope" >
              <el-text line-clamp="2" v-if="scope.row.powApparent != null">
                {{ scope.row.powApparent.toFixed(1) }}
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="后门" align="center" prop="BUa" >
            <template #default="scope" >
              <el-text line-clamp="2" v-if="scope.row.powApparent != null">
                {{ scope.row.powApparent.toFixed(1) }}
              </el-text>
            </template>
          </el-table-column>
        </el-table-column>
        <el-table-column label="平均温度 (&deg;C)" align="center">
          <el-table-column label="前门" align="center" prop="AUb" >
            <template #default="scope" >
              <el-text line-clamp="2" v-if="scope.row.powApparent != null">
                {{ scope.row.powApparent.toFixed(1) }}
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="后门" align="center" prop="BUb" >
            <template #default="scope" >
              <el-text line-clamp="2" v-if="scope.row.powApparent != null">
                {{ scope.row.powApparent.toFixed(1) }}
              </el-text>
            </template>
          </el-table-column>
        </el-table-column>
        <el-table-column label="平均湿度 (%)" align="center">
          <el-table-column label="前门" align="center" prop="AUc" >
            <template #default="scope" >
              <el-text line-clamp="2" v-if="scope.row.powApparent != null">
                {{ scope.row.powApparent.toFixed(0) }}
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="后门" align="center" prop="BUc" >
            <template #default="scope" >
              <el-text line-clamp="2" v-if="scope.row.powApparent != null">
                {{ scope.row.powApparent.toFixed(0) }}
              </el-text>
            </template>
          </el-table-column>
        </el-table-column>

        <!-- 数据库查询 -->
        <el-table-column label="操作" align="center" width="140px">
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
              @click="handleDelete(scope.row.id)"
              style="background-color:#fa3333;color:#fff;border:none;width:40px;height:30px;"
            >
              删除
            </el-button>
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
          <el-form-item label="名称" label-width="90">
            <el-input v-model="rowColInfo.roomName" placeholder="请输入" />
          </el-form-item>
          <el-form-item label="楼层" prop="type" label-width="90">
            <el-select v-model="rowColInfo.addr" placeholder="请选择">
              <el-option v-for="(addr_item,addr_index) in addrList" :key="addr_index" :label="addr_item" :value="addr_item" />
            </el-select>
          </el-form-item>
        </div>
        <div style="margin-bottom: 10px">
          <el-text>地砖（地砖按60CM*60CM）</el-text>
        </div>
        <div class="double-formitem">
          <el-form-item label="行数" label-width="90">
            <el-input-number v-model="rowColInfo.row" :min="1" :max="100" controls-position="right" placeholder="请输入" />
          </el-form-item>
          <el-form-item label="列数" label-width="90">
            <el-input-number v-model="rowColInfo.col" :min="1" :max="70" controls-position="right" placeholder="请输入" />
          </el-form-item>
        </div>
        
        <div style="margin-bottom: 5px">
          <el-text>容量</el-text>
        </div>
        <el-form-item label="机房总电力容量" label-width="160">
          <el-input v-model="rowColInfo.powerCapacity" placeholder="请输入">
            <template #append>kVA</template>
          </el-input>
        </el-form-item>
        <el-form-item label="非IT设备总额定功率" label-width="160">
          <el-input v-model="rowColInfo.airPower" placeholder="包括制冷系统（如空调、冷源设备、新风系统等）">
            <template #append>kVA</template>
          </el-input>
        </el-form-item>

        <div class="double-formitem">
          <el-form-item label="显示选择" label-width="90" style="padding-top: 15px">
            <el-switch v-model="rowColInfo.displayFlag" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <el-radio-group v-model="radio" size="large" style="margin-left: 15px;">
            <el-radio-button label="负载率" value="负载率"/>
            <el-radio-button label="PUE" value="PUE"/>
          </el-radio-group>
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

const activeNames = ref()
const valueMode = ref(0)
const switchValue = ref(0)
const roomFlag =ref();
const dialogVisible = ref(false);
const isAddRoom = ref(false) // 是否为添加机房模式 
const deletedList = ref<any>([]) //已删除的
const roomId = ref(0) // 房间id
const radio = ref("负载率")
const rowColInfo = reactive({
  roomName: '', // 机房名
  addr: '未区分', //楼层
  row: 14, // 行
  col: 18, // 列
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

const loading = ref(true)
const roomShowType = ref(true)
const powOptionsData = ref([{}])
const powOptionsDataOne = reactive<EChartsOption>({}) as EChartsOption
const powInfo = reactive({}) // 功率数据信息
const powCopyInfo = reactive({})
const roomAddrList = ref(['未区分'])
const addrAllRoomList = ref([[]])
const clickIndex = ref(0)

const queryDeleteParams = reactive({
  company: undefined,
  showCol: [1, 2, 12, 13, 15, 16] as number[],
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

const handleRoomHome = (id) => {
  push({path: '/room/home', state: { roomId: id }})
}

/** 搜索按钮操作 */
const handleQuery = async () => {
  // 还未实现不同楼层同名机房
  const res = await MachineRoomApi.getAddrAllRoomList(queryParams)
  console.log(res)
  if(res) {
    addrAllRoomList.value = addrAllRoomList.value.map(() => [])
    console.log("111",activeNames.value)
    activeNames.value = []
    res.forEach((ele,i) => {
      let index = roomAddrList.value.findIndex(item => item == ele.addr)
      if(index != -1) {
        addrAllRoomList.value[index].push(ele)
        if(ele.addr != '未区分')
        activeNames.value.push(index-1)
      }
    })
    activeNames.value = [...new Set(activeNames.value)]
    console.log("222",activeNames.value)
  }
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryParams.roomName = undefined;
  roomAddrList.value.forEach(async (item,index) => {
    await getAddrAllRoomList({addr: item},index)
  })
}

// 处理弹窗取消事件
const handleDialogCancel = () => {
  dialogVisible.value = false;
  isAddRoom.value = false;
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
    addrAllRoomList.value.forEach((item,index) => {
      addrAllRoomList.value[index] = addrAllRoomList.value[index].filter(ele => ele.id != id)
    })
    // getRoomAddrList()
  })
}

//已删除
const handleStopDelete = async() =>{
  const res = await MachineRoomApi.deletedRoomInfo({
    pageNo: queryDeleteParams.pageNo,
    pageSize: queryDeleteParams.pageSize,
  })
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
    if(res != null || res != "")
    message.success('恢复成功')
    deletedList.value = deletedList.value.filter(item => item.id != flagRoomid)
    getRoomAddrList()
  })
}

// 重置表单
const resetForm = () => {
  Object.assign(rowColInfo, {
    roomName: '', // 机房名
    addr: '未区分', //楼层
    row: 14, // 行
    col: 18, // 列,
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
}

// 处理设置提交
const submitSetting = async() => {
  if(rowColInfo.roomName == '') {
    message.error('机房名称不能为空,请输入!')
    return
  }
  const resSelect = await MachineRoomApi.selectRoomByName({name: rowColInfo.roomName});
  if(resSelect != null){
    message.error('该机房名称已存在,请重新输入!');
    rowColInfo.roomName = '';
    return
  }

   let roomFlagId:any = null;
   let messageRoomFlag = "保存成功！";
   console.log("aaaaaaaaaa",rowColInfo)

   if(radio.value === "PUE") {
    rowColInfo.displayType = 1
   }

   try {
    const res = await MachineRoomApi.saveRoomDetail({
      id: roomFlagId,
      roomName: rowColInfo.roomName,
      addr: rowColInfo.addr,
      xLength: rowColInfo.col,
      yLength: rowColInfo.row,
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

//获取机房楼层
const getRoomAddrList = async() => {
  const res =  await MachineRoomApi.getRoomAddrList({})
  roomAddrList.value = addrList.value.filter(value => res.includes(value))
  roomAddrList.value.forEach(async (item,index) => {
    await getAddrAllRoomList({addr: item},index)
  })
}

const getAddrAllRoomList = async(query,index) => {
  const res2 = await MachineRoomApi.getAddrAllRoomList(query)
  console.log(res2)
  addrAllRoomList.value[index] = res2
  
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
        console.log('params', params)
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
        name: '有功功率',
        data: addrAllRoomList.value[index].map(item => item.powActive ? item.powActive : 0),
        type: 'bar',
        label: {
          show: true,
          position: 'top', // 顶部显示
          formatter: '{c}kW', // 显示数据值
        },
      },
      {
        name: '无功功率',
        data: addrAllRoomList.value[index].map(item => item.powReactive ? item.powReactive : 0),
        type: 'bar',
        label: {
          show: true,
          position: 'top', // 顶部显示
          formatter: '{c}kVar', // 显示数据值
        },
      },
      {
        name: '视在功率',
        data: addrAllRoomList.value[index].map(item => item.powApparent ? item.powApparent : 0),
        type: 'bar',
        label: {
          show: true,
          position: 'top', // 顶部显示
          formatter: '{c}kVA', // 显示数据值
        },
      },
      {
        name: '功率因素',
        data: addrAllRoomList.value[index].map(item => item.powerFactor ? item.powerFactor : 0),
        type: 'bar',
        label: {
          show: true,
          position: 'top', // 顶部显示
          formatter: '{c}', // 显示数据值
        },
      },
    ]
  })
}

const getAllApi = async () => {
  await getRoomAddrList()
  loading.value = false
}

getAllApi()

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
      position: absolute;
      right: 8px;
      bottom: 8px;
      cursor: pointer;
    }
  }
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

:deep(.el-collapse-item__header) {
  padding: 0 20px;
}
:deep(.el-card) {
  background-color: #eef4fc;
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
</style>