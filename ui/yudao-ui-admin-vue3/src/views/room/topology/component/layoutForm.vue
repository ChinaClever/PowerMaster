<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="80%" align-center>
    <el-form 
      ref="layoutForm"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="130"
      center
    >
      <div style="display: flex;">
        <div>
          <el-form-item label="柜列方向" prop="direction">
            <el-select v-model="formData.direction" placeholder="请选择活动区域" style="width: 100px">
              <el-option label="横向" :value="1"/>
              <el-option label="纵向" :value="2"/>
            </el-select>
          </el-form-item>
        </div>
        <div style="flex: 1;">
          <el-form-item label="柜列名称" prop="name" label-width="130">
            <el-input v-model="formData.name" placeholder="请输入柜列名称" />
          </el-form-item>
        </div>
        <div style="flex: 1;">
          <el-form-item v-if="formData.type == 1" label="柜列数量" prop="amount">
            <el-input-number v-model="formData.amount" :min="minAmount" :max="formData.direction == 1 ? operateInfo.maxlndexX : operateInfo.maxlndexY" />
          </el-form-item>
        </div>
        <div style="flex: 1;">
          <el-form-item label="总电力容量">
            <el-input type="number" v-model="formData.powerCapacity" placeholder="请输入总电力容量" />
          </el-form-item>
        </div>
      </div>

      <div style="display: flex;">
        <div>
          <el-form-item label="柜列用能告警" label-width="130">
            <el-switch v-model="isAutoCreate.aisleAlram" :active-value="1" :inactive-value="0" style="width: 100px" />
          </el-form-item>
        </div>
        <div style="flex: 1;">
          <el-form-item label="柜列坐标" label-width="130" />
        </div>
        <div style="flex: 1;">
          <el-form-item label="横坐标" label-width="130">
            <el-input-number v-model="formData.xCoordinate" :min="1" :max="operateInfo.xLength" controls-position="right" placeholder="请输入" />
          </el-form-item>
        </div>
        <div style="flex: 1;">
          <el-form-item label="纵坐标" label-width="130">
            <el-input-number v-model="formData.yCoordinate" :min="1" :max="operateInfo.yLength" controls-position="right" placeholder="请输入" />
          </el-form-item>
        </div>
      </div>
      
      <div v-if="isAutoCreate.aisleAlram" style="display: flex;">
        <div>
          <el-form-item label="日用能告警" label-width="130">
            <el-switch v-model="formData.eleAlarmDay" :active-value="1" :inactive-value="0" style="width: 100px" />
          </el-form-item>
        </div>
        <div style="flex: 1;">
          <el-form-item v-if="formData.eleAlarmDay" label="日用能限制" label-width="130">
            <el-input type="number" v-model="formData.eleLimitDay" :min="0" controls-position="right" placeholder="请输入柜列日用能限制">
              <template #append>kVA</template>
            </el-input>
          </el-form-item>
        </div>
        <div style="flex: 1;">
          <el-form-item label="月用能告警" label-width="130">
            <el-switch v-model="formData.eleAlarmMonth" :active-value="1" :inactive-value="0" />
          </el-form-item>
        </div>
        <div style="flex: 1;">
          <el-form-item v-if="formData.eleAlarmMonth" label="月用能限制" label-width="130">
            <el-input type="number" v-model="formData.eleLimitMonth" :min="0" controls-position="right" placeholder="请输入柜列月用能限制">
              <template #append>kVA</template>
            </el-input>
          </el-form-item>
        </div>
      </div>

      <hr v-if="formType == 'add'" />

      <div v-if="formType == 'add'" style="display: flex;justify-content: space-evenly;">
        <div :style="isAutoCreate.cabinet ? '' : 'flex:1'">
          <el-form-item label="机柜配置" label-width="130">
            <el-switch v-model="isAutoCreate.cabinet" :active-value="1" :inactive-value="0" style="width: 100px" />
          </el-form-item>
        </div>
        <div v-if="isAutoCreate.cabinet" style="flex: 1;">
          <el-form-item label="机柜高度" prop="cabinetHeight" label-width="130">
            <el-input type="number" v-model="machineFormData.cabinetHeight" placeholder="请输入机柜高度">
              <template #append>U</template>
            </el-input>
          </el-form-item>
        </div>
        <div v-if="isAutoCreate.cabinet" style="flex: 1;">
          <el-form-item label="机柜电力容量" prop="powCapacity" label-width="130">
            <el-input type="number" v-model="machineFormData.powCapacity" placeholder="请输入机柜电力容量">
              <template #append>kVA</template>
            </el-input>
          </el-form-item>
        </div>
        <div v-if="isAutoCreate.cabinet" style="flex: 1;">
          <el-form-item label="机柜类型：" prop="type" label-width="130">
            <el-select v-model="machineFormData.cabinetType" placeholder="请选择机柜类型">
              <el-option label="IT机柜" value="IT机柜" />
              <el-option label="网络柜" value="网络柜" />
              <el-option label="配电-电池柜" value="配电-电池柜" />
              <el-option label="水阀占位柜" value="水阀占位柜" />
              <el-option label="适配框" value="适配框" />
              <el-option label="柱子" value="柱子" />
              <el-option label="占位" value="占位" />
              <template #footer>
                <el-input
                  v-model="machineFormData.cabinetType"
                  placeholder="请输入机柜类型"
                  size="small"
                />
              </template>
            </el-select>
          </el-form-item>
        </div>
      </div>

      <div v-if="isAutoCreate.cabinet && formType == 'add'" >
        <div style="display: flex;">
          <div>
            <el-form-item label="列头柜" label-width="130">
              <el-switch v-model="machineFormData.first" :active-value="1" :inactive-value="0" style="width: 100px" />
            </el-form-item>
          </div>
          <div style="flex: 1;">
            <el-form-item label="空调1位置" label-width="130">
              <el-input type="number" v-model="machineFormData.airList[0]" placeholder="请输入空调1位置" />
            </el-form-item>
          </div>
          <div style="flex: 1;">
            <el-form-item label="空调2位置" label-width="130">
              <el-input type="number" v-model="machineFormData.airList[1]" placeholder="请输入空调2位置" />
            </el-form-item>
          </div>
          <div style="flex: 1;">
            <el-form-item label="空调3位置" label-width="130">
              <el-input type="number" v-model="machineFormData.airList[2]" placeholder="请输入空调3位置" />
            </el-form-item>
          </div>
        </div>

        <hr/>

        <div style="display: flex;justify-content: space-evenly;">
          <div :style="isAutoCreate.pdu ? '' : 'flex:1'">
            <el-form-item label="机柜与PDU绑定" label-width="130">
              <el-switch v-model="isAutoCreate.pdu" :active-value="1" :inactive-value="0" style="width: 100px" />
            </el-form-item>
          </div>
          <div v-if="isAutoCreate.pdu" style="flex: 1;">
            <el-form-item label="起始IP地址" label-width="130">
              <el-input v-model="machineFormData.pduIpA" placeholder="请输入PDU起始IP地址" />
            </el-form-item>
          </div>
          <div v-if="isAutoCreate.pdu" style="flex: 1;">
            <el-form-item  label="起始级联地址" label-width="130">
              <el-input type="number" v-model="machineFormData.casIdA" placeholder="请输入PDU起始级联地址" />
            </el-form-item>
          </div>
          <div v-if="isAutoCreate.pdu" style="flex: 1;">
            <el-form-item label="设备主副机数量" label-width="130">
              <el-input type="number" v-model="machineFormData.casIdA" placeholder="请输入设备主副机数量" />
            </el-form-item>
          </div>
        </div>

        <div style="display: flex;justify-content: space-evenly;">
          <div :style="isAutoCreate.sensor ? '' : 'flex:1'">
            <el-form-item v-if="isAutoCreate.pdu" label="机柜与传感器" label-width="130">
              <el-switch v-model="isAutoCreate.sensor" :active-value="1" :inactive-value="0" style="width: 100px" />
            </el-form-item>
          </div>
          <div v-if="isAutoCreate.sensor && isAutoCreate.pdu" style="flex: 1;">
            <el-form-item label="前门-中" label-width="130">
              <el-select v-model="sensorFormData.pathPdu" placeholder="请选择AB路" @change="sensorFormData.sensorId = null" style="width: 50%">
                <el-option label="A路" value="A" />
                <el-option label="B路" value="B" />
              </el-select>
              <el-select v-model="sensorFormData.sensorId " placeholder="请选择传感器id" style="width: 50%">
                <template v-if="sensorFormData.type == 1 && sensorFormData.pathPdu == 'A'">
                  <el-option v-for="id in sensorAIds" :key="id" :label="id" :value="id" />
                </template>
                <template v-else-if="sensorFormData.type == 1 && sensorFormData.pathPdu == 'B'">
                  <el-option v-for="id in sensorBIds" :key="id" :label="id" :value="id" />
                </template>
                <el-option v-else v-for="id in 2" :key="id" :label="id" :value="id" />
              </el-select>
            </el-form-item>
          </div>
          <div v-if="isAutoCreate.sensor && isAutoCreate.pdu" style="flex: 1;">
            <el-form-item  label="后门-中" label-width="130">
              <el-select v-model="sensorFormData.pathPdu" placeholder="请选择AB路" @change="sensorFormData.sensorId = null" style="width: 50%">
                <el-option label="A路" value="A" />
                <el-option label="B路" value="B" />
              </el-select>
              <el-select v-model="sensorFormData.sensorId " placeholder="请选择传感器id" style="width: 50%">
                <template v-if="sensorFormData.type == 1 && sensorFormData.pathPdu == 'A'">
                  <el-option v-for="id in sensorAIds" :key="id" :label="id" :value="id" />
                </template>
                <template v-else-if="sensorFormData.type == 1 && sensorFormData.pathPdu == 'B'">
                  <el-option v-for="id in sensorBIds" :key="id" :label="id" :value="id" />
                </template>
                <el-option v-else v-for="id in 2" :key="id" :label="id" :value="id" />
              </el-select>
            </el-form-item>
          </div>
          <div v-if="isAutoCreate.sensor && isAutoCreate.pdu" style="flex: 1;">
            <el-form-item label-width="130" />
          </div>
        </div>

        <hr/>

        <el-form-item v-if="isAutoCreate.cabinet" label="机柜与插接箱绑定" label-width="130">
          <el-switch v-model="isAutoCreate.bus" :active-value="1" :inactive-value="0" />
        </el-form-item>

        <div v-if="isAutoCreate.bus" style="display: flex;justify-content: space-evenly;align-items: center">
          <div style="flex: 1;">
            <el-form-item label="A路 母线IP" label-width="130">
              <el-input v-model="machineFormData.busIpa" placeholder="请输入A路母线IP" />
            </el-form-item>
          </div>
          <div style="flex: 1;">
            <el-form-item  label="母线编号" label-width="130">
              <el-input type="number" v-model="machineFormData.busSerialNuma" placeholder="请输入A路母线编号" />
            </el-form-item>
          </div>
          <div style="flex: 1;">
            <el-form-item label="插接箱地址" label-width="130">
              <el-input type="number" v-model="machineFormData.boxAddra" placeholder="请输入插接箱地址" />
            </el-form-item>
          </div>
          <div style="flex: 1;">
            <el-form-item label="输出位数量" label-width="130">
              <el-input type="number" v-model="machineFormData.outNuma" placeholder="请输入输出位数量" />
            </el-form-item>
          </div>
        </div>
        <div v-if="isAutoCreate.bus" style="display: flex;justify-content: space-evenly;align-items: center">
          <div style="flex: 1;">
            <el-form-item label="B路 母线IP" label-width="130">
              <el-input v-model="machineFormData.busIpb" placeholder="请输入B路母线IP" />
            </el-form-item>
          </div>
          <div style="flex: 1;">
            <el-form-item  label="母线编号" label-width="130">
              <el-input type="number" v-model="machineFormData.busSerialNumb" placeholder="请输入B路母线编号" />
            </el-form-item>
          </div>
          <div style="flex: 1;">
            <el-form-item label="插接箱地址" label-width="130">
              <el-input type="number" v-model="machineFormData.boxAddrb" placeholder="请输入插接箱地址" />
            </el-form-item>
          </div>
          <div style="flex: 1;">
            <el-form-item label="输出位数量" label-width="130">
              <el-input type="number" v-model="machineFormData.outNumb" placeholder="请输入输出位数量" />
            </el-form-item>
          </div>
        </div>

        <hr/>

        
        <div style="display: flex;">
          <div>
            <el-form-item label="机柜用能告警" label-width="130">
              <el-switch v-model="isAutoCreate.cabinetAlarm" :active-value="1" :inactive-value="0" style="width: 100px" />
            </el-form-item>
          </div>
          <div v-if="isAutoCreate.cabinetAlarm" style="flex: 1;">
            <el-form-item label="日用能告警" label-width="130">
              <el-switch v-model="machineFormData.eleAlarmDay" :active-value="1" :inactive-value="0" style="width: 15%" />
              <el-input type="number" v-if="machineFormData.eleAlarmDay" v-model="machineFormData.eleLimitDay" :min="0" controls-position="right" placeholder="请输入机柜日用能限制" style="width: 85%">
                <template #append>kVA</template>
              </el-input>
            </el-form-item>
          </div>
          <div v-if="isAutoCreate.cabinetAlarm" style="flex: 1;">
            <el-form-item label="月用能告警" label-width="130">
              <el-switch v-model="machineFormData.eleAlarmMonth" :active-value="1" :inactive-value="0" style="width: 15%" />
              <el-input type="number" v-if="machineFormData.eleAlarmMonth" v-model="machineFormData.eleLimitMonth" :min="0" controls-position="right" placeholder="请输入机柜月用能限制" style="width: 85%">
                <template #append>kVA</template>
              </el-input>
            </el-form-item>
          </div>
          <div v-if="isAutoCreate.cabinetAlarm" style="flex: 1;">
            <el-form-item label-width="130" />
          </div>
        </div>
      </div>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button :disabled="formLoading" type="primary" @click="submitForm">确 定</el-button>
    </template>
    <Dialog id="machine-dialog" v-model="dialogVisibleCabinet" title="自动创建机柜" width="60%" top="9vh">
      <div class="formContainer">
        <el-form
          ref="machineForm"
          v-loading="formLoading"
          :model="machineFormData"
          :rules="machineFormRules"
          label-width="140px"
          center
        >
        <el-collapse v-model="activeNames" @change="handleChange" accordion>
          <el-collapse-item title="机柜参数" name="1">
            <div class="collapse-container">
                <el-form-item label="机柜名称：" prop="cabinetName">
                <el-input v-model="machineFormData.cabinetName" placeholder="请输入" />
                </el-form-item>
                <el-form-item label="机柜类型：" prop="type">
                <el-select v-model="machineFormData.cabinetType" placeholder="请选择">
                  <el-option label="IT机柜" value="IT机柜" />
                  <el-option label="网络柜" value="网络柜" />
                  <el-option label="配电-电池柜" value="配电-电池柜" />
                  <el-option label="水阀占位柜" value="水阀占位柜" />
                  <el-option label="适配框" value="适配框" />
                  <el-option label="柱子" value="柱子" />
                  <el-option label="占位" value="占位" />
                </el-select>
                </el-form-item>
                <el-form-item label="机柜高度(U)：" prop="cabinetHeight">
                <el-input v-model="machineFormData.cabinetHeight" placeholder="请输入" />
                </el-form-item>
                <el-form-item  label="电力容量(kVA)：" prop="powCapacity">
                  <el-input v-model="machineFormData.powCapacity" placeholder="请输入" />
                </el-form-item>
                <!-- <el-form-item label="所属公司：" prop="company">
                  <el-input v-model="machineFormData.company" placeholder="请输入" />
                </el-form-item> -->
                <div class="double-formitem">
                  <el-form-item label="月用能告警">
                    <el-switch @click="showFlag = !showFlag" v-model="machineFormData.eleAlarmMonth" :active-value="1" :inactive-value="0" />
                  </el-form-item>
                  <el-form-item label="月用能限制">
                    <el-input-number v-model="machineFormData.eleLimitMonth" :min="0" controls-position="right" placeholder="请输入" />
                  </el-form-item>
                </div>
                <div class="double-formitem">
                  <el-form-item label="日用能告警">
                    <el-switch @click="showFlagCopy = !showFlagCopy" v-model="machineFormData.eleAlarmDay" :active-value="1" :inactive-value="0" />
                  </el-form-item>
                  <el-form-item label="日用能限制">
                    <el-input-number v-model="machineFormData.eleLimitDay" :min="0" controls-position="right" placeholder="请输入" />
                  </el-form-item>
                </div>
            </div>
          </el-collapse-item>
          <el-collapse-item title="快速绑定PDU/母线" name="2">
            <el-tabs type="border-card" class="demo-tabs" v-model="machineFormData.pduBox">
              <el-tab-pane label="PDU" :name=false>
                <div class="pduBus">
                  <el-form-item label-width="0">
                    <el-col :span="2" class="text-center">
                      <span class="text-gray-500">起始IP地址</span>
                    </el-col>
                    <el-col :span="6">
                      <el-input v-model="machineFormData.pduIpA" placeholder="请输入" />
                    </el-col>
                    <el-col :span="2" class="text-center">
                      <span class="text-gray-500">级联地址</span>
                    </el-col>
                    <el-col :span="6">
                      <el-input v-model="machineFormData.casIdA" placeholder="请输入" />
                    </el-col>
                    <el-col :span="2" class="text-center">
                      <span class="text-gray-500">级联数量</span>
                    </el-col>
                    <el-col :span="6">
                      <el-input v-model="machineFormData.casIdA" placeholder="请输入" />
                    </el-col>
                  </el-form-item>
                  <el-form-item label-width="0">
                    <el-col :span="2" class="text-center">
                      <span class="text-gray-500">传感器绑定</span>
                    </el-col>
                    <el-col :span="6">
                      <el-input model-value="温湿度传感器" disabled placeholder="请输入" />
                    </el-col>
                    <el-col :span="2" class="text-center">
                      <span class="text-gray-500">前门中间</span>
                    </el-col>
                    <el-col :span="6">
                      <el-tooltip placement="right"  effect="light">
                        <template #content>id:{{item.id}}<br />PDU: {{item.pathPdu}}<br />传感器id: {{item.sensorId}}</template>
                        <div :class="item.pathPdu ? 'boxActive' : 'box'" @click.prevent="handleSensorEdit(item, 0, index)">
                          温湿度传感器（中）
                          <div v-if="item.pathPdu" @click.stop="handleSensorDelete(0, index)" class="delete"><Icon icon="ep:close" />
                          </div>
                        </div>
                      </el-tooltip>
                    </el-col>
                    <el-col :span="2" class="text-center">
                      <span class="text-gray-500">后门中间</span>
                    </el-col>
                    <el-col :span="6">
                      <el-tooltip placement="right"  effect="light">
                        <template #content>id:{{item.id}}<br />PDU: {{item.pathPdu}}<br />传感器id: {{item.sensorId}}</template>
                        <div :class="item.pathPdu ? 'boxActive' : 'box'" @click.prevent="handleSensorEdit(item, 0, index)">
                          温湿度传感器（中）
                          <div v-if="item.pathPdu" @click.stop="handleSensorDelete(0, index)" class="delete"><Icon icon="ep:close" />
                          </div>
                        </div>
                      </el-tooltip>
                    </el-col>
                  </el-form-item>
                </div>
              </el-tab-pane>
              <el-tab-pane label="母线" :name="1">
                <div class="Bus">
                  <div>
                    <div class="title">A路</div>
                    <el-form-item label-width="0">
                      <el-col :span="2" class="text-center">
                        <span class="text-gray-500">母线IP</span>
                      </el-col>
                      <el-col :span="4">
                        <el-input v-model="machineFormData.busIpa" placeholder="请输入" />
                      </el-col>
                      <el-col :span="2" class="text-center">
                        <span class="text-gray-500">母线编号</span>
                      </el-col>
                      <el-col :span="4">
                        <el-input type="number" min="0" v-model="machineFormData.busSerialNuma" placeholder="请输入" @change="(val) => judgeValue(val, 'busSerialNuma')" />
                      </el-col>
                      <el-col :span="2" class="text-center">
                        <span class="text-gray-500">插接箱地址</span>
                      </el-col>
                      <el-col :span="4">
                        <el-input type="number" min="0" v-model="machineFormData.boxAddra" placeholder="请输入" @change="(val) => judgeValue(val, 'boxAddra')" />
                      </el-col>
                      <el-col :span="2" class="text-center">
                        <span class="text-gray-500">输出位数量</span>
                      </el-col>
                      <el-col :span="4">
                        <el-input type="number" min="0" v-model="machineFormData.outNuma" placeholder="请输入" @change="(val) => judgeValue(val, 'outNuma')" />
                      </el-col>
                    </el-form-item>
                  </div>
                  <div>
                    <div class="title">B路</div>
                    <el-form-item label-width="0">
                      <el-col :span="2" class="text-center">
                        <span class="text-gray-500">母线IP</span>
                      </el-col>
                      <el-col :span="4">
                        <el-input v-model="machineFormData.busIpb" placeholder="请输入" />
                      </el-col>
                      <el-col :span="2" class="text-center">
                        <span class="text-gray-500">母线编号</span>
                      </el-col>
                      <el-col :span="4">
                        <el-input type="number" min="0" v-model="machineFormData.busSerialNumb" placeholder="请输入" @change="(val) => judgeValue(val, 'busSerialNumb')" />
                      </el-col>
                      <el-col :span="2" class="text-center">
                        <span class="text-gray-500">插接箱地址</span>
                      </el-col>
                      <el-col :span="4">
                        <el-input type="number" min="0" v-model="machineFormData.boxAddrb" placeholder="请输入" @change="(val) => judgeValue(val, 'boxAddrb')" />
                      </el-col>
                      <el-col :span="2" class="text-center">
                        <span class="text-gray-500">输出位数量</span>
                      </el-col>
                      <el-col :span="4">
                        <el-input type="number" min="0" v-model="machineFormData.outNumb" placeholder="请输入" @change="(val) => judgeValue(val, 'outNumb')" />
                      </el-col>
                    </el-form-item>
                  </div>
                </div>
              </el-tab-pane>
            </el-tabs>
            
          </el-collapse-item>
        </el-collapse>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="dialogVisibleCabinet = false">取 消</el-button>
        <el-button :disabled="formLoading" type="primary" @click="dialogVisibleCabinet = false">确 定</el-button>
      </template>
      <Dialog id="sensorDialog" v-model="sensorVisible" width="500px" title="传感器">
        <div class="sensorDialog" style="padding-left: 20px">
          <el-form
            ref="sensorForm"
            v-loading="sensorLoading"
            :model="sensorFormData"
            :rules="sensorFormRules"
            label-width="80px"
            center
          >
            <el-form-item label="类型">
              <el-input disabled :value="sensorType[sensorFormData.type]" />
            </el-form-item>
            <el-form-item label="PDU" prop="pathPdu">
              <el-select v-model="sensorFormData.pathPdu" placeholder="请选择" @change="sensorFormData.sensorId = null">
                <el-option label="A路" value="A" />
                <el-option label="B路" value="B" />
              </el-select>
            </el-form-item>
            <el-form-item label="传感器id" prop="sensorId">
              <el-select v-model="sensorFormData.sensorId " placeholder="请选择">
                <template v-if="sensorFormData.type == 1 && sensorFormData.pathPdu == 'A'">
                  <el-option v-for="id in sensorAIds" :key="id" :label="id" :value="id" />
                </template>
                <template v-else-if="sensorFormData.type == 1 && sensorFormData.pathPdu == 'B'">
                  <el-option v-for="id in sensorBIds" :key="id" :label="id" :value="id" />
                </template>
                <el-option v-else v-for="id in 2" :key="id" :label="id" :value="id" />
              </el-select>
            </el-form-item>
          </el-form>
        </div>
        <template #footer>
        <el-button @click="sensorVisible = false">取 消</el-button>
        <el-button :disabled="sensorLoading" type="primary" @click="submitSensorForm">确 定</el-button>
      </template>
      </Dialog>
    </Dialog>
  </Dialog>
</template>
<script lang="ts" setup>
import { FormRules } from 'element-plus'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const beginAmount = ref(1) // data传进来时的数量 柜列且长度大于一时才需要
const minAmount = ref(1) // 最小机柜数量
const dialogVisible = ref(false) // 弹窗的是否展示
const dialogVisibleCabinet = ref(false) // 自动创建机柜弹窗的是否展示
const isAutoCreate = ref({
  cabinet: false,
  pdu: false,
  sensor: false,
  bus: false,
  aisleAlram: false,
  cabinetAlarm: false
})
const sensorFormData = reactive({
  type: null,
  sensorId: null,
  pathPdu: '',
  channel: null
},)
const sensorVisible = ref(false)
const dialogTitle = ref('') // 弹窗的标题
const operateInfo = ref<any>({}) // 机柜列中机柜最大数量  默认26
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  eleAlarmDay: 0, // 日用能告警
  eleLimitDay: 1000, // 日用能限制
  eleAlarmMonth: 0, // 月用能告警
  eleLimitMonth: 1000, // 月用能限制
  powerCapacity:480, //柜列电力容量
  type: 1,
  name: '',
  cabinetHeight: 42,
  pduBar: false,
  direction: operateInfo.value.maxlndexX ? 1 : 2,
  amount: 12,
  id: '',
  cabinetList: [] as any,
  xCoordinate: 0,
  yCoordinate: 0
})
const formRules = reactive<FormRules>({
  name: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
  // id: [{ required: true, message: 'id不能为空', trigger: 'blur' }],
})
const layoutForm = ref() // 表单 Ref

const item = ref({
  pathPdu: 'A',
  sensorId: '1',
  id: '1'
})

const machineFormData = ref({
  cabinetName: '',
  cabinetType: 'IT机柜',
  cabinetHeight: 42, //U
  powCapacity: 40, // kVA
  pduBox: true,
  pduIp: "",
  addr: 0,
  addrNum: 0,
  busIpa: "",
  busSerialNuma: 1,
  boxAddra: 2,
  outNuma: 3,
  busIpb: "",
  busSerialNumb: 1,
  boxAddrb: 2,
  outNumb: 3,
  channel: 0,
  position: 0,
  sensorId: 0,
  sensorType: 0,
  eleAlarmDay: false,
  eleAlarmMonth: false,
  eleLimitDay: 1000,
  eleLimitMonth: 1000,
  first: false,
  airList: [0,0,0]
})


/** 打开弹窗 */
const open = async (type: string, data, info) => {
  dialogVisible.value = true
  dialogTitle.value = type == 'edit' ? '编辑柜列': '新增柜列'
  formType.value = type
  operateInfo.value = info
  resetForm()
  console.log('data', data,info)
  if (data && data.type == 1 && data.amount > 1 && data.cabinetList.length > 0) { // 如果是柜列且长度大于一，则该柜列的最小长度应为其中最后那个绑定id机柜的下标值，如果最小值比这个下标值还小的话就会删掉该机柜
    const length = data.amount
    beginAmount.value = length
    for (let i =1; i <= length; i++) {
      console.log('data.cabinetList[length - i].index', data.cabinetList[length - i].index)
      if (data.cabinetList[length - i].id > 0) {
        minAmount.value = data.cabinetList[length - i].index
        break
      }
    }
  }
  if (data) formData.value = data
  formData.value.xCoordinate = Number(operateInfo.value.lndexX)+1
  formData.value.yCoordinate = Number(operateInfo.value.lndexY)+1
  console.log('formData.value', formData.value)
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  console.log('校验表单',)
  if (!layoutForm) return
  const valid = await layoutForm.value.validate()
  if (!valid) return
  // 提交请求
  if (formData.value.type == 1 && formData.value.cabinetList.length > 0) {
    const diff = formData.value.amount - beginAmount.value
    console.log('diff', diff)
    if (diff > 0) { // 增加
      for (let i = 0; i < diff; i++) {
        formData.value.cabinetList.push({
          id: 0,
          cabinetName: '',
          runStatus: 0
        })
      }
    } else if(diff < 0) { // 减少
      formData.value.cabinetList.splice(formData.value.amount - 1, -diff)
    }
  }

  if(isAutoCreate.value.pdu) {
    machineFormData.value.pduBox = false 
  } else {
    machineFormData.value.pduBox = true 
  }
  
  machineFormData.value.airList.forEach((element,index) => {
    if(element == "") {
      machineFormData.value.airList[index] = 0
    }
  });
  
  formLoading.value = true
  try {
    dialogVisible.value = false
    // 发送操作成功的事件
    if(isAutoCreate.value.cabinet && (isAutoCreate.value.pdu || isAutoCreate.value.bus)) {
      if(isAutoCreate.value.bus) {
        formData.value.pduBar = true
      }
      emit('success', {...formData.value,cabinetFirstVO: machineFormData.value });
    } else if(isAutoCreate.value.cabinet) {
      emit('success', {...formData.value,cabinetFirstVO: {
        cabinetHeight: machineFormData.value.cabinetHeight,
        powCapacity: machineFormData.value.powCapacity,
        cabinetType: machineFormData.value.cabinetType,
        first: machineFormData.value.first,
        airList: machineFormData.value.airList
      } });
    } else {
      emit('success', {...formData.value});
    }
  } catch (error) {
    console.log('error', error)
  } finally {
    formLoading.value = false
  }
}

const judgeValue = (val,label) => {
  if(val < 0) {
    machineFormData.value[label] = 0
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    eleAlarmDay: 0, // 日用能告警
    eleLimitDay: 1000, // 日用能限制
    eleAlarmMonth: 0, // 月用能告警
    eleLimitMonth: 1000, // 月用能限制
    powerCapacity:480,
    type: 1,
    name: '',
    cabinetHeight: 42,
    pduBar: false,
    direction: operateInfo.value.maxlndexX ? 1 : 2,
    amount: 12,
    id: '',
    cabinetList: [],
    xCoordinate: Number(operateInfo.value.lndexX)+1,
    yCoordinate: Number(operateInfo.value.lndexY)+1
  }

  machineFormData.value = {
    cabinetName: '',
    cabinetType: 'IT机柜',
    cabinetHeight: 42, //U
    powCapacity: 40, // kVA
    pduBox: true,
    pduIp: "",
    addr: 0,
    addrNum: 0,
    busIpa: "",
    busSerialNuma: 1,
    boxAddra: 2,
    outNuma: 3,
    busIpb: "",
    busSerialNumb: 1,
    boxAddrb: 2,
    outNumb: 3,
    channel: 0,
    position: 0,
    sensorId: 0,
    sensorType: 0,
    eleAlarmDay: false,
    eleAlarmMonth: false,
    eleLimitDay: 1000,
    eleLimitMonth: 1000,
    first: false,
    airList: [0,0,0]
  }
  isAutoCreate.value = {
    cabinet: false,
    pdu: false,
    sensor: false,
    bus: false,
    aisleAlram: false,
    cabinetAlarm: false
  }
  console.log(typeof(operateInfo.value.lndexX))
  minAmount.value = 1
  layoutForm.value?.resetFields()
}

watch(() => formData.value.direction, (val) => {
  if (val == 1 && formData.value.amount > operateInfo.value.maxlndexX) {
    formData.value.amount = operateInfo.value.maxlndexX
  } else if(val == 2 && formData.value.amount > operateInfo.value.maxlndexY) {
    formData.value.amount = operateInfo.value.maxlndexY
  }
})
</script>
<style lang="scss" scoped>
.double-formitem {
  display: flex;
  & > div {
    flex: 1;
  }
}
.box {
  background-color: #f5f7fa;
  color: #a8abb2;
  cursor: pointer;
  display: flex;
  align-items: center;
  padding-left: 10px;
  border-radius: 5px;
}
.boxActive {
  background-color: #78d560;
  color: #fff;
  cursor: pointer;
  position: relative;
  display: flex;
  align-items: center;
  padding-left: 10px;
  border-radius: 5px;
  .delete {
    display: none;
    position: absolute;
    top: -5px;
    right: 0px;
  }
  &:hover {
    .delete {
      display: block;
      position: absolute;
      top: -5px;
      right: 0px;
    }
  }
  
}
</style>