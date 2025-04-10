<template>
  <CommonMenu @check="handleCheck"  @node-click="handleClick" :showSearch="true" :dataList="navList" navTitle="需量监测">
    <template #NavInfo>
      <div >
      <div class="nav_data">
      <br/> 
    <div class="descriptions-container" style="font-size: 14px;">

    <!-- <div v-show="item.location !== null" v-for="item in maxCurAll" :key="item.devKey" style="  margin-top: 15px;margin-left: 10px;">
      <div>{{ item.location}}</div>
    </div>  -->
    <div v-for="item in maxCurAll" :key="item.devKey" class="description-item">
      <span>所在位置 :</span>
      <span class="text-ellipsis">{{ item.location }}</span>
    </div>     
    <div v-for="item in maxCurAll" :key="item.devKey" class="description-item">
      <span>网络地址 :</span>
      <span>{{ item.devKey}}</span>
    </div>    
    <div  v-for="item in maxCurAll" :key="item.devKey" class="description-item">
      <span >发生时间 :</span>
      <span class="value">{{ item.l1MaxCurTime }}</span>
    </div>
    <div v-for="item in maxCurAll" :key="item.devKey" class="description-item">
      <span>{{ flagName }} :</span>
      <span>{{ item.l1MaxCur}}A</span>
    </div>    

  </div>
     </div>   
    </div>
    </template>
    <template #ActionBar>
      <el-form
        class="-mb-15px"
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        label-width="68px"                          
      >
        <el-form-item label="时间段" prop="createTime" label-width="60px">
          <el-button 
            @click=" startTime = new Date(now1.getTime() - 24 * 60 * 60 * 1000);endTime = new Date(); queryParams.timeType = 0;queryParams.oldTime = getFullTimeByDate(new Date(now1.getTime() - 24 * 60 * 60 * 1000));queryParams.newTime = getFullTimeByDate(now1);queryParams.timeArr = null;handleQuery();showSearchBtn = false;dateSwitch = true;"
            :type="queryParams.timeType == 0 ? 'primary' : ''"
          >
            最近24小时
          </el-button>
          <el-button 
            @click="queryParams.timeType = 1;now = new Date();now.setDate(1);now.setHours(0,0,0,0); startTime = now;endTime = now;   queryParams.oldTime = getFullTimeByDate(now);queryParams.newTime = null;   queryParams.timeArr = null;handleMonthPick();showSearchBtn = false;;dateSwitch = false" 
            :type="queryParams.timeType == 1 ? 'primary' : ''"
          >
            月份
          </el-button>
          <el-button 
            @click="queryParams.timeType = 2;queryParams.oldTime = null;queryParams.newTime = null;queryParams.timeArr = null;showSearchBtn = true;dateSwitch = false" 
            :type="queryParams.timeType == 2 ? 'primary' : ''"
          >
            自定义
          </el-button> 
          <div style="float: left;">
        <el-form-item >
          <el-date-picker
            v-if="queryParams.timeType == 1"
            v-model="queryParams.oldTime"
            value-format="YYYY-MM-DD HH:mm:ss"
            type="month"
            :disabled-date="disabledDate"
            @change="handleMonthPick"
            class="!w-160px"
          />
          <el-date-picker
            v-if="queryParams.timeType == 2"
            v-model="queryParams.timeArr"
            value-format="YYYY-MM-DD HH:mm:ss"
            type="daterange"
            :shortcuts="shortcuts"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :disabled-date="disabledDate"
            @change="handleDayPick"
            class="!w-190px"
          />
       
        <el-form-item :style="{marginLeft: '20px'}">
          <el-button  v-if="queryParams.timeType == 2 || queryParams.timeType == 1" @click="handleQuery" style="margin-left: -10px"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
          <el-button v-if="queryParams.timeType == 2 || queryParams.timeType == 1" @click="resetQuery" style="margin-left: 10px"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
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
        </el-form-item>
      </div>                           
        </el-form-item>
        
        
        <div style="float:right ">
          <el-button @click="visModeShow(0)" :type="valueMode == 0 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 2px" />电流</el-button>
          <el-button @click="visModeShow(1)" :type="valueMode == 1 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 2px" />功率</el-button>          
          <el-button @click="pageSizeArr=[24,36,48,96];queryParams.pageSize = 24;getList();switchValue = 1;" :type="switchValue == 1 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 8px" />阵列模式</el-button>
          <el-button @click="pageSizeArr=[15, 25,30, 50, 100];queryParams.pageSize = 15;getList();switchValue = 2;" :type="switchValue == 2 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 8px" />表格模式</el-button>
        </div>
      </el-form>
    </template>
    <template #Content>
     <div v-if="switchValue && list.length > 0" class="table-height">
    <!-- 三相数据显示 -->
      <el-table v-show="switchValue == 2 && valueMode == 0 && MaxLineId > 1" v-loading="loading" :data="list"  :show-overflow-tooltip="true"   @cell-dblclick="toPDUDisplayScreen" >
        <el-table-column label="编号" align="center" prop="tableId" width="80px" >
          <template #default="{ $index }">
            {{ $index + 1 + (queryParams.pageNo - 1) * queryParams.pageSize }}
          </template>  
        </el-table-column>
      
        <!-- 数据库查询 -->
        <el-table-column label="所在位置" align="center" prop="location" width="180px" />
        <el-table-column label="网络地址" align="center" prop="devKey" :class-name="ip" width="125px"/>
        <el-table-column label="L1最大电流(kA)" align="center" prop="l1MaxCur" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" >
              {{ scope.row.l1MaxCur }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="发生时间" align="center" prop="l1MaxCurTime"/>
        <el-table-column label="L2最大电流(A)" align="center" prop="l2MaxCur" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.l2MaxCur !== null && scope.row.l2MaxCur !== undefined ">
              {{ scope.row.l2MaxCur }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="发生时间" align="center" prop="l2MaxCurTime"  />
        <el-table-column label="L3最大电流(A)" align="center" prop="l3MaxCur" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.l2MaxCur !== null && scope.row.l2MaxCur !== undefined ">
              {{ scope.row.l3MaxCur }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="发生时间" align="center" prop="l3MaxCurTime" />

        <el-table-column label="操作" align="center" width="130px">
          <template #default="scope">
            <el-button
              link
              type="primary"
              
              @click="location=scope.row.location;onlyDevKey=scope.row.devKey;showDialog(scope.row.pduId,dateSwitch?'hour':'day',flagValue=0,scope.row.l1MaxCur)"
              v-if="scope.row.status != null && scope.row.status != 5"
              style="background-color:#409EFF;color:#fff;border:none;width:60px;height:30px;"
            >
            详情
            </el-button>
            <el-button
              link
              type="danger"
              @click="handleDelete(scope.row.id)"
              v-if="scope.row.status == 5"
              style="background-color:#fa3333;color:#fff;border:none;width:60px;height:30px;"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>  
    <!-- 单相数据显示 -->
      <el-table v-show="switchValue == 2 && valueMode == 0 && !(MaxLineId > 1)" v-loading="loading" :data="list"  :show-overflow-tooltip="true"  @cell-dblclick="toPDUDisplayScreen" >
        <el-table-column label="编号" align="center" prop="tableId" width="80px" >
          <template #default="{ $index }">
            {{ $index + 1 + (queryParams.pageNo - 1) * queryParams.pageSize }}
          </template>  
        </el-table-column>
        <!-- 数据库查询 -->
        <el-table-column label="所在位置" align="center" prop="location" />
        <el-table-column label="网络地址" align="center" prop="devKey" :class-name="ip" />
        <el-table-column label="最大电流(kA)" align="center" prop="l1MaxCur"  >
          <template #default="scope" >
            <el-text line-clamp="2" >
              {{ scope.row.l1MaxCur }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="发生时间" align="center" prop="l1MaxCurTime"/>

        <el-table-column label="操作" align="center" width="130px">
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click="location=scope.row.location;onlyDevKey=scope.row.devKey;showDialog(scope.row.pduId,dateSwitch?'hour':'day',flagValue=0)"
              v-if="scope.row.status != null && scope.row.status != 5"
            >
            详情
            </el-button>
            <el-button
              link
              type="danger"
              @click="handleDelete(scope.row.id)"
              v-if="scope.row.status == 5"
              style="background-color:#fa3333;color:#fff;border:none;width:60px;height:30px;"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table> 
    <!-- 三相有数据显示 -->      
      <el-table v-show="switchValue == 2 && valueMode == 1 && MaxLineId > 1" v-loading="loading" :data="list"  :show-overflow-tooltip="true"  @cell-dblclick="toPDUDisplayScreen" >
        <el-table-column label="编号" align="center" prop="tableId" width="80px" >
          <template #default="{ $index }">
            {{ $index + 1 + (queryParams.pageNo - 1) * queryParams.pageSize }}
          </template>  
        </el-table-column>
        <el-table-column label="所在位置" align="center" prop="location" width="180px" />
        <el-table-column label="网络地址" align="center" prop="devKey" :class-name="ip" width="125px"/>
        <el-table-column label="L1最大功率(kW)" align="center" prop="l1MaxPow" width="140px" >
          <template #default="scope" >
            <el-text line-clamp="2" >
              {{ scope.row.l1MaxPow }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="发生时间" align="center" prop="l1MaxPowTime" />
        <el-table-column label="L2最大功率(kW)" align="center" prop="l2MaxPow" width="140px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.l2MaxCur !== null && scope.row.l2MaxCur !== undefined ">
              {{ scope.row.l2MaxPow }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="发生时间" align="center" prop="l2MaxPowTime" />
        <el-table-column label="L3最大功率(kW)" align="center" prop="l3MaxPow" width="140px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.l2MaxCur !== null && scope.row.l2MaxCur !== undefined ">
              {{ scope.row.l3MaxPow }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="发生时间" align="center" prop="l3MaxPowTime" />
        <el-table-column label="操作" align="center" width="130px">
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click="location=scope.row.location;onlyDevKey=scope.row.devKey;showDialogOne(scope.row.pduId, dateSwitch ? 'hour' : 'day',1, scope.row.l1MaxPow);"
              v-if="scope.row.status != null && scope.row.status != 5"
              style="background-color:#409EFF;color:#fff;border:none;width:60px;height:30px;"
            >
            详情
            </el-button>
            <el-button
              link
              type="danger"
              @click="handleDelete(scope.row.id)"
              v-if="scope.row.status == 5"
              style="background-color:#fa3333;color:#fff;border:none;width:60px;height:30px;"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    <!-- 单相数据显示 -->      
      <el-table v-show="switchValue == 2 && valueMode == 1 && !(MaxLineId > 1)" v-loading="loading" :data="list"  :show-overflow-tooltip="true" @cell-dblclick="toPDUDisplayScreen" >
        <el-table-column label="编号" align="center" prop="tableId" width="80px" >
          <template #default="{ $index }">
            {{ $index + 1 + (queryParams.pageNo - 1) * queryParams.pageSize }}
          </template>  
        </el-table-column>
        <el-table-column label="所在位置" align="center" prop="location"  />
        <el-table-column label="网络地址" align="center" prop="devKey" :class-name="ip" width="125px"/>
        <el-table-column label="最大功率(kW)" align="center" prop="l1MaxPow"  >
          <template #default="scope" >
            <el-text line-clamp="2" >
              {{ scope.row.l1MaxPow }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="发生时间" align="center" prop="l1MaxPowTime" />
        <el-table-column label="操作" align="center" width="130px">
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click="location=scope.row.location;onlyDevKey=scope.row.devKey;showDialogOne(scope.row.pduId, dateSwitch ? 'hour' : 'day', 1,scope.row.l1MaxPow);"
              v-if="scope.row.status != null && scope.row.status != 5"
            >
            详情
            </el-button>
            <el-button
              link
              type="danger"
              @click="handleDelete(scope.row.id)"
              v-if="scope.row.status == 5"
              style="background-color:#fa3333;color:#fff;border:none;width:60px;height:30px;"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table> 
      
      
      <div  v-if="switchValue == 1 && list.length > 0  && valueMode == 1" class="arrayContainer">
        <div class="arrayItem" v-for="item in list" :key="item.devKey">
          <div class="devKey">{{ item.location != null && item.location != '未绑定' ? item.location : item.devKey }}</div>
          <div class="content" v-show="item.l3MaxPow !== undefined && item.l3MaxPow !== null">
            <!-- <div style="padding: 0 28px"><Pie :width="50" :height="50" :max="{L1:item.l1MaxPow,L2:item.l2MaxPow,L3:item.l3MaxPow}" /></div> -->
            <div class="info" style="margin-bottom: 60px">
              <div>L1最大功率：{{ (item.l1MaxPow || 0).toFixed(2) }}kW</div>
              <div>L2最大功率：{{ (item.l2MaxPow || 0).toFixed(2) }}kW</div>
              <div>L3最大功率：{{ (item.l3MaxPow || 0).toFixed(2) }}kW</div>
              <!-- <div>AB路占比：{{item.fzb}}</div> -->
            </div>
            <div style="margin-left: 10px;margin-bottom: 50px; margin-top: -20px; width: 100px;height: 100px" ><Bar :max="{L1:item.l1MaxPow,L2:item.l2MaxPow,L3:item.l3MaxPow}" /></div>
          </div>       
          <div class="content" v-show="item.l3MaxPow == undefined && item.l3MaxPow == null && item.l2MaxPow == undefined && item.l2MaxPow == null && item.l1MaxPow != undefined && item.l1MaxPow != null">
            <!-- <div style="padding: 0 28px"><Pie :width="50" :height="50" :max="{L1:item.l1MaxPow,L2:item.l2MaxPow,L3:item.l3MaxPow}" /></div> -->
            <div class="info">
              <div >最大功率：{{item.l1MaxPow}}kW</div>
              <div >发生时间：{{item.l1MaxPowTime}}</div>
              <!-- <div>AB路占比：{{item.fzb}}</div> -->
            </div>
          </div> 
          <div class="content" v-show="item.l3MaxPow == undefined && item.l3MaxPow == null && item.l2MaxPow == undefined && item.l2MaxPow == null && item.l1MaxPow == undefined && item.l1MaxPow == null ">
            <!-- <div style="padding: 0 28px"><Pie :width="50" :height="50" :max="{L1:item.l1MaxPow,L2:item.l2MaxPow,L3:item.l3MaxPow}" /></div> -->
            <div class="info">
              <div style="margin-top: 30px;">该设备暂无数据</div>
              <!-- <div>AB路占比：{{item.fzb}}</div> -->
            </div>
          </div>          
          <!-- <div class="room">{{item.jf}}-{{item.mc}}</div> -->              
          <!-- <button class="detail" @click="toPDUDisplayScreen(item)" v-if="item.status != null && item.status != 5">详情</button> -->    
          <button class="detail" v-show="item.l3MaxPow !== undefined && item.l3MaxPow !== null" @click="onlyDevKey=item.devKey;location=item.location;showDialogOne(item.pduId,dateSwitch?'hour':'day',flagValue=1,item.l1MaxPow);location1=item.location">详情</button>
        </div>
      </div>

      <el-dialog
        v-model="dialogVisibleOne"
        @close="handleClose"
        width="70%"
      >     
        <!-- 自定义的头部内容（可选） -->
        <template #header>
          <el-button @click="lineidBeforeChartUnmountOne()" style="float:right" show-close="false" >关闭</el-button>
          <span style="float: right; margin: 7px 10px;">时间段：{{ queryParams1.startTime }} - {{ queryParams1.endTime }}</span>
          <div><span style="font-weight: 700; font-size: 20px;">功率详情</span> 所在位置：{{ location?location:'未绑定' }}  网络地址：{{onlyDevKey.split('-').length > 0 ? onlyDevKey.split('-')[0] : onlyDevKey}}</div> 
        </template>

        <!-- 自定义的主要内容 -->
        
           
          <div ref="lineidChartContainerOne" id="lineidChartContainerOne" class="adaptiveStyle"  style="width: 90vw;height: 60vh;margin-left: -100px;"></div>
      </el-dialog>

      <div  v-if="switchValue == 1 && list.length > 0 && valueMode == 0" class="arrayContainer">
        <div class="arrayItem" v-for="item in list" :key="item.devKey">
          <div class="devKey">{{ item.location != null && item.location != '未绑定' ? item.location : item.devKey }}</div>
          <div class="content" v-show="item.l3MaxCur !== undefined && item.l3MaxCur !== null" style="width: 500px;height: 100px">
            <!--<div style="padding: 0 28px"><Pie :width="50" :height="50" :max="{L1:item.l1MaxCur,L2:item.l2MaxCur,L3:item.l3MaxCur}" /></div>-->
            <div class="info" style="margin-bottom: 20px;">
              
              <div>L1最大电流：{{ (item.l1MaxCur || 0).toFixed(2) }}A</div>
              <div>L2最大电流：{{ (item.l2MaxCur || 0).toFixed(2) }}A</div>
              <div>L3最大电流：{{ (item.l3MaxCur || 0).toFixed(2) }}A</div>
              <!-- <div>AB路占比：{{item.fzb}}</div> -->
            </div>
            <!-- <div  style="height: 50px;background-color: black"></div> -->
            <div style="margin-left: 10px;margin-bottom: 30px; width: 100px;height: 100px" ><Bar :max="{L1:item.l1MaxCur,L2:item.l2MaxCur,L3:item.l3MaxCur}" /></div>
          </div>
          <div class="content" v-if="item.l3MaxCur == undefined && item.l3MaxCur == null && item.l2MaxCur == undefined && item.l2MaxCur == null && item.l1MaxCur != undefined && item.l1MaxCur != null">
            <!--<div style="padding: 0 28px"><Pie :width="50" :height="50" :max="{L1:item.l1MaxCur,L2:item.l2MaxCur,L3:item.l3MaxCur}" /></div>-->
            <div class="info">
              
              <div >最大电流：{{ item.l1MaxCur }}A</div>
              <div >发生时间：{{ item.l1MaxCurTime }}</div>

              <!-- <div>AB路占比：{{item.fzb}}</div> -->
            </div>
           <!--<div style="padding: 0 28px"><Pie :width="100" :height="100" :max="{L1:item.l1MaxCur,L2:item.l2MaxCur,L3:item.l3MaxCur}" /></div> -->
          </div>   
         <!-- <div class="content" v-if="item.l3MaxCur == undefined || item.l3MaxCur == null && item.l2MaxCur == undefined && item.l2MaxCur == null && item.l1MaxCur == undefined && item.l1MaxCur == null"> -->
          <div class="content" v-if="item.l1MaxCur == null">
            <!--<div style="padding: 0 28px"><Pie :width="50" :height="50" :max="{L1:item.l1MaxCur,L2:item.l2MaxCur,L3:item.l3MaxCur}" /></div>-->
            <div class="info">
              
              <div style="margin-top: 30px;">该设备暂无数据</div>

              <!-- <div>AB路占比：{{item.fzb}}</div> -->
            </div>
            <!-- <div style="padding: 0 28px"><Pie :width="100" :height="100" :max="{L1:item.l1MaxCur,L2:item.l2MaxCur,L3:item.l3MaxCur}" /></div> -->
          </div>       
          <!-- <div class="room">{{item.jf}}-{{item.mc}}</div> -->                
          <!--<button class="detail" @click="toPDUDisplayScreen(item)" v-if="item.status != null && item.status != 5">详情</button>--> 
          <button class="detail" v-show="item.l3MaxCur !== undefined && item.l3MaxCur !== null" @click="onlyDevKey=item.devKey;location=item.location;showDialog(item.pduId,dateSwitch?'hour':'day',flagValue=0,item.l1MaxCur);">详情</button>
        </div>
      </div>
      
      <el-dialog
        v-model="dialogVisible"
        @close="handleClose"
      >     
        <!-- 自定义的头部内容（可选） -->
        <template #header>
          <el-button @click="lineidBeforeChartUnmount()" style="float:right" show-close="false" >关闭</el-button>
          <span style="float: right; margin:7px 10px;">{{ queryParams1.startTime }} - {{ queryParams1.endTime }}</span>
          <div>
            <div><span style="font-weight: 700; font-size: 20px;">需量电流详情</span> 所在位置：{{location?location:'未绑定'}} 网络地址：{{onlyDevKey.split('-').length > 0 ? onlyDevKey.split('-')[0] : onlyDevKey}}</div> 
          </div>
          
        </template>

        <!-- 自定义的主要内容 -->
        
          <!-- <pow /> -->
         
          <div ref="lineidChartContainer" id="lineidChartContainer"  style="width: 90vw;height: 60vh;margin-left: -100px;"></div>
      </el-dialog>
    </div>
      <Pagination
        :total="total"
        :page-size-arr="pageSizeArr"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />

      <template v-if="list.length == 0 && switchValue != 2">
        <el-empty description="暂无数据" :image-size="300" />
      </template>
    </template>
  </CommonMenu>
  
  <!-- 表单弹窗：添加/修改 -->
  <!-- <PDUDeviceForm ref="formRef" @success="getList" /> -->
</template>

<script setup lang="ts">
// import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download';
import { PDUDeviceApi } from '@/api/pdu/pdudevice';
import Pie from './component/Pie.vue';
// import PDUDeviceForm from './PDUDeviceForm.vue'
import { ElTree } from 'element-plus';
import { CabinetApi } from '@/api/cabinet/info';

import * as echarts from 'echarts';
import { ref, onMounted, onUnmounted } from 'vue';
import Bar from './component/Bar.vue'
import pow from './component/pow.vue'

const searchbth = ref(false);
const flagName = ref('最大电流');
const now1 = new Date();
let startTime = new Date(now1.getTime() - 24 * 60 * 60 * 1000);
let endTime = new Date();

const formatToTwoDecimals = (num) => {
    if (typeof num === 'number') {
      return num.toFixed(2);
    } else if (typeof num === 'string') {
      // 如果已经是字符串，则尝试转换为数字再格式化
      const parsedNum = parseFloat(num);
      return isNaN(parsedNum) ? "0.00" : parsedNum.toFixed(2);
    } else {
      return "0.00"; // 或者根据你的需求选择一个默认值
    }
  };

// 使用 ref 来获取 DOM 元素
const chartDom = ref<HTMLDivElement | null>(null);
let myChart: echarts.ECharts | null = null;

// 图表配置
const option = {
  series: [
    {
      name: 'Nightingale Chart',
      type: 'pie',
      radius: [1, 20],
      center: ['50%', '50%'],
      roseType: 'area',
      itemStyle: {
        borderRadius: 8
      },
      data: [
        { value: 40, name: 'rose 1' },
        { value: 38, name: 'rose 2' },
        { value: 32, name: 'rose 3' }
      ]
    }
  ]
};
// 组件挂载时初始化图表
onMounted(() => {
  giveValue()
  getList()
  getNavList()
  getListAll(0);
  if (chartDom.value) {
    myChart = echarts.init(chartDom.value);
    myChart.setOption(option);
  }
});
// 组件卸载时销毁图表
onUnmounted(() => {
  if (myChart) {
    myChart.dispose();
    myChart = null;
  }
});

/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })
const navList = ref([]) as any // 左侧导航栏树结构列表
const { push } = useRouter()
const MaxLineId = ref(0);
const valueMode = ref(0);
const now = ref()
const showSearchBtn = ref(false)
const pageSizeArr = ref([24,36,48,96])
const switchValue = ref(1)
const dialogVisible = ref(false) //全屏弹窗的显示隐藏
const dialogVisibleOne = ref(false) //全屏弹窗的显示隐藏
const detailsId = ref(0)
const linedata = ref([]) as any
const flagValue = ref(0)
let regularTime = null
const dateSwitch = ref(true)
const onlyDevKey = ref('')
const location = ref('')
const createTimes = ref('')
const endTimes = ref('')
// const statusNumber = reactive({
//   normal : 0,
//   warn : 0,
//   alarm : 0,
//   offline : 0
// })
// 时间段快捷选项
const shortcuts = [
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setDate(start.getDate() - 7)
      return [start, end]
    },
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setMonth(start.getMonth() - 1)
      return [start, end]
    },
  },
  {
    text: '最近六个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setMonth(start.getMonth() - 6)
      return [start, end]
    },
  },
]
const statusList = reactive([
  {
    name: '正常',
    selected: true,
    value: 0,
    cssClass: 'btn_normal',
    activeClass: 'btn_normal normal'
  },
  {
    name: '预警',
    selected: true,
    value: 1,
    cssClass: 'btn_warn',
    activeClass: 'btn_warn warn'
  },
  {
    name: '告警',
    selected: true,
    value: 2,
    cssClass: 'btn_error',
    activeClass: 'btn_error error'
  },
  {
    name: '离线',
    selected: true,
    value: 5,
    cssClass: 'btn_offline',
    activeClass: 'btn_offline offline'

  },
])

const handleClick = (row) => {
  if(row.type != null  && row.type == 3){
    queryParams.devKey = row.devKey
    handleQuery();
  }
}

const handleCheck = async (row) => {
  if(row.length == 0){
    queryParams.pduKeyList = null;
    getList();
    return;
  }
  const pduKeys = [] as any
  var haveCabinet = false;
  row.forEach(item => {
    console.log('row',item)
    if (item.type == 4) {
      pduKeys.push(item.unique)
      haveCabinet = true;
    }
  })
  if(!haveCabinet ){
    queryParams.pduKeyList = [-1]
  }else{
    queryParams.pduKeyList = pduKeys
  }
  console.log('呜呜呜呜',queryParams.pduKeyList)

  getList();
}


const filterText = ref('')
const treeRef = ref<InstanceType<typeof ElTree>>()

watch(filterText, (val) => {
  treeRef.value!.filter(val)
})

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const getFullTimeByDate = (date) => {
  var year = date.getFullYear();//年
  var month = date.getMonth();//月
  var day = date.getDate();//日
  var hours = date.getHours();//时
  var min = date.getMinutes();//分
  var second = date.getSeconds();//秒
  return year + "-" +
      ((month + 1) > 9 ? (month + 1) : "0" + (month + 1)) + "-" +
      (day > 9 ? day : ("0" + day)) + " " +
      (hours > 9 ? hours : ("0" + hours)) + ":" +
      (min > 9 ? min : ("0" + min)) + ":" +
      (second > 9 ? second : ("0" + second));
}

const disabledDate = (date) => {
  // 获取今天的日期
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  // 设置date的时间为0时0分0秒，以便与today进行比较
  date.setHours(0, 0, 0, 0);

  // 如果date在今天之后，则禁用
  if(queryParams.timeType == 0){
    return date > today;
  }else {
    return date >= today;
  }
  
}

const handleDayPick = () => {
  if(queryParams?.oldTime && queryParams.timeType == 2){

    queryParams.oldTime = null;
    queryParams.newTime = null;
  }

 if (queryParams.timeArr && queryParams.timeType == 2) {

    queryParams.oldTime = queryParams.timeArr[0];
    queryParams.newTime = queryParams.timeArr[1].split(" ")[0]+ " " + "23:59:59";
    startTime = new Date(queryParams.oldTime);
    endTime = new Date(queryParams.newTime);
  }
  handleQuery()
}

const handleMonthPick = () => {

  if(queryParams.oldTime){
    var newTime = new Date(queryParams.oldTime);
    newTime.setMonth(newTime.getMonth() + 1);
    newTime.setDate(newTime.getDate() - 1);
    newTime.setHours(23,59,59)
    queryParams.newTime = getFullTimeByDate(newTime);
    startTime = new Date(queryParams.oldTime);
     endTime = newTime;
  }else {
    queryParams.newTime = null;
  }
  handleQuery()
} 

const loading = ref(false) // 列表的加载中
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
    pf:null
  }
]) as any// 列表的数据
const maxCurAll = ref([
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
    pf:null
  }
]) as any// 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 24,
  devKey: undefined,
  createTime: [],
  cascadeNum: undefined,
  serverRoomData:undefined,
  status:[],
  cabinetIds:[],
  timeType : 0,
  timeArr:[],
  oldTime : getFullTimeByDate(new Date(new Date().getFullYear(),new Date().getMonth(),1,0,0,0)),
  newTime : getFullTimeByDate(new Date(new Date().getFullYear(),new Date().getMonth() + 1,1,23,59,59)),
}) as any
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中


const queryParams1 = reactive({
  id : undefined,
  type : undefined,
  startTime : getFullTimeByDate(new Date(new Date().getFullYear(),new Date().getMonth(),1,0,0,0)),
  endTime : getFullTimeByDate(new Date(new Date().getFullYear(),new Date().getMonth() + 1,1,23,59,59)),
})


/** 查询列表 */
const getList = async () => {
  // loading.value = true
  try {
    const data = await PDUDeviceApi.getPDULinePage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const visModeShow = (flag) => {
  if(flag == 0){
    valueMode.value =0;
    flagName.value ='最大电流';
    getListAll(flag);
  }else{
    valueMode.value =1;
    flagName.value ='最大功率';
    getListAll(flag);
  }
}

const getListAll = async (flagVlaue) => {
  queryParams.flagVlaue = flagVlaue
  const allData = await PDUDeviceApi.getPDUDeviceMaxCur(queryParams)
  maxCurAll.value = allData.list
  maxCurAll.value.forEach((obj) => {
      obj.l1MaxCur = obj.l1MaxCur?.toFixed(1);
  })
}



// 接口获取导航列表
const getNavList = async() => {
  let arr = [] as any
  var temp = await CabinetApi.getRoomPDUList()
  arr = arr.concat(temp);
  navList.value = arr
}

// const toPDUDisplayScreen = (row) =>{
//   push('/pdu/pdudisplayscreen?devKey=' + row.devKey + '&location=' + row.location + '&id=' + row.id);
// }
import { useRouter } from 'vue-router';
import { LineChart } from 'echarts/charts'
import { s } from 'vite/dist/node/types.d-aGj9QkWt';
import { ElementPlus } from '@element-plus/icons-vue/dist/types';

const router = useRouter();
const toPDUDisplayScreen = (row: { devKey: string; location: string; id: number }) => {
  const { devKey, location } = row;
  router.push({
    path: '/pdu/power/pdudisplayscreen',
    query: { devKey,  location }
  });
};

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  if(queryParams.timeType != 0 && queryParams.oldTime == null ){
    return;
  }
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  statusList.forEach((item) => item.selected = true)
  queryParams.status = [];
  handleQuery()
}

/** 添加/修改操作 */
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await PDUDeviceApi.deletePDUDevice(id)
    message.success(t('common.delSuccess'))
    // 刷新列表
    // await getList()
  } catch {}
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    exportLoading.value = true
    const data = await PDUDeviceApi.exportPDUDevice(queryParams)
    download.excel(data, 'PDU设备.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}
/** 获得es历史数据PDU相id的最大值 */
const giveValue = async () => {
  MaxLineId.value = await PDUDeviceApi.getPDUMaxLineId(queryParams)
}

//L1,L2,L3的数据
//const lChartData = ref({
//  cur_max_value : [] as number[], //电流
//  pow_active_max_value : [] as number[], //功率
//});
//
//const llChartData = ref({
//  cur_max_value : [] as number[],
//  pow_active_max_value : [] as number[],
//});
//
//const lllChartData = ref({
//  cur_max_value : [] as number[],
//  pow_active_max_value : [] as number[],
//});
//
//const lineidDateTimes = ref([] as string[])

const instance = getCurrentInstance();
let lineidChart = null as echarts.ECharts | null; // 显式声明 rankChart 的类型
const lineidChartContainer = ref<HTMLElement | null>(null);
let lineidChartOne = null as echarts.ECharts | null; // 显式声明 rankChart 的类型
const lineidChartContainerOne = ref<HTMLElement | null>(null);


const updateChart = (lChartData, llChartData, lllChartData, lineidDateTimes) => {
  interface DataItem {
    Year: any;
    Country: any;
    Income: string; // 现在我们指定Income为字符串类型
  }

  const formatToTwoDecimals = (num) => {
    if (typeof num === 'number') {
      return num.toFixed(2);
    } else if (typeof num === 'string') {
      // 如果已经是字符串，则尝试转换为数字再格式化
      const parsedNum = parseFloat(num);
      return isNaN(parsedNum) ? "0.00" : parsedNum.toFixed(2);
    } else {
      return "0.00"; // 或者根据你的需求选择一个默认值
    }
  };

  const newData: DataItem[] = [];
  for (let i = 0; i < lineidDateTimes.value.length; i++) {
    newData.push({ Year: lineidDateTimes.value[i], Country: 'L1-电流', Income: formatToTwoDecimals(lChartData.value.cur_max_value[i]) });
    newData.push({ Year: lineidDateTimes.value[i], Country: 'L2-电流', Income: formatToTwoDecimals(llChartData.value.cur_max_value[i]) });
    newData.push({ Year: lineidDateTimes.value[i], Country: 'L3-电流', Income: formatToTwoDecimals(lllChartData.value.cur_max_value[i]) });
  }

  const newData1: DataItem[] = [];
  for (let i = 0; i < lineidDateTimes.value.length; i++) {
    newData1.push({ Year: lineidDateTimes.value[i], Country: 'L1功率', Income: formatToTwoDecimals(lChartData.value.pow_active_max_value[i]) });
    newData1.push({ Year: lineidDateTimes.value[i], Country: 'L2功率', Income: formatToTwoDecimals(llChartData.value.pow_active_max_value[i]) });
    newData1.push({ Year: lineidDateTimes.value[i], Country: 'L3功率', Income: formatToTwoDecimals(lllChartData.value.pow_active_max_value[i]) });
  }

  if(flagValue.value == 0){
    return {
      dataset: [
    {
      id: 'dataset_raw',
      source: newData
    },
    {
      id: 'dataset_l',
      fromDatasetId: 'dataset_raw',
      transform: {
        type: 'filter',
        config: {
          and: [
              { dimension: 'Country', '=': 'L1-电流' }
            ]
        }
      }
    },
    {
      id: 'dataset_ll',
      fromDatasetId: 'dataset_raw',
      transform: {
        type: 'filter',
        config: {
          and: [
              { dimension: 'Country', '=': 'L2-电流' }
            ]
        }
      }
    },
    {
      id: 'dataset_lll',
      fromDatasetId: 'dataset_raw',
      transform: {
        type: 'filter',
        config: {
          and: [
              { dimension: 'Country', '=': 'L3-电流' }
            ]
        }
      }
    }
  ],
  tooltip: {
    trigger: 'axis',
    formatter: function (params) {
        let result = params[0].value.Year + '<br>';
        params.forEach((param) => {
            let unit = param.seriesName.includes('功率')? 'kW' : 'A';
            result += `${param.seriesName}: ${param.value.Income} ${unit}<br>`;
        });
        return result;
    }
},
  xAxis: {
    type: 'category',
    axisTick: {
      alignWithLabel: true, // 让刻度线与标签对齐
    },
  },
  yAxis: {
    
  },
  legend: {
    data: ['L1-电流', 'L2-电流', 'L3-电流']
  },
  series: [
    {
      type: 'line',
      datasetId: 'dataset_l',
      showSymbol: false,
      encode: {
        x: 'Year',
        y: 'Income',
        itemName: 'Year',
        tooltip: ['Income']
      },
      name: 'L1-电流'
    },
    {
      type: 'line',
      datasetId: 'dataset_ll',
      showSymbol: false,
      encode: {
        x: 'Year',
        y: 'Income',
        itemName: 'Year',
        tooltip: ['Income']
      },
      name: 'L2-电流'
    },
    {
      type: 'line',
      datasetId: 'dataset_lll',
      showSymbol: false,
      encode: {
        x: 'Year',
        y: 'Income',
        itemName: 'Year',
        tooltip: ['Income']
      },
      name: 'L3-电流'
    }
  ]
    }
  }else if(flagValue.value == 1){
    return {
      dataset: [
    {
      id: 'dataset_raw',
      source: newData1
    },
    {
      id: 'dataset_l',
      fromDatasetId: 'dataset_raw',
      transform: {
        type: 'filter',
        config: {
          and: [
              { dimension: 'Country', '=': 'L1功率' }
            ]
        }
      }
    },
    {
      id: 'dataset_ll',
      fromDatasetId: 'dataset_raw',
      transform: {
        type: 'filter',
        config: {
          and: [
              { dimension: 'Country', '=': 'L2功率' }
            ]
        }
      }
    },
    {
      id: 'dataset_lll',
      fromDatasetId: 'dataset_raw',
      transform: {
        type: 'filter',
        config: {
          and: [
              { dimension: 'Country', '=': 'L3功率' }
            ]
        }
      }
    }
  ],
  tooltip: {
    trigger: 'axis',
    formatter: function (params) {
        let result = '';
        params.forEach((param) => {
            let unit = param.seriesName.includes('功率')? 'kW' : 'A';
            result += `${param.seriesName}: ${param.value.Income} ${unit}<br>`;
        });
        return result;
    }
},
  xAxis: {
    type: 'category',
    nameLocation: 'middle'
  },
  yAxis: {
    
  },
  legend: {
    data: ['L1功率', 'L2功率', 'L3功率']
  },
  series: [
    {
      type: 'line',
      datasetId: 'dataset_l',
      showSymbol: false,
      encode: {
        x: 'Year',
        y: 'Income',
        itemName: 'Year',
        tooltip: ['Income']
      },
      name: 'L1功率'
    },
    {
      type: 'line',
      datasetId: 'dataset_ll',
      showSymbol: false,
      encode: {
        x: 'Year',
        y: 'Income',
        itemName: 'Year',
        tooltip: ['Income']
      },
      name: 'L2功率'
    },
    {
      type: 'line',
      datasetId: 'dataset_lll',
      showSymbol: false,
      encode: {
        x: 'Year',
        y: 'Income',
        itemName: 'Year',
        tooltip: ['Income']
      },
      name: 'L3功率'
    }
  ]
    }
  }
}

// const updateChart = (lChartData,llChartData,lllChartData,lineidDateTimes ) => {
//   if(flagValue.value == 0){
//     return {
//       title: { text: ''},
//       tooltip: { trigger: 'axis', formatter: function (params) {
//         let result = params[0].name + '<br>';
//         params.forEach(param => {
//           result += param.marker + param.seriesName + ': &nbsp;&nbsp;&nbsp;&nbsp' + param.value;
//           if (param.seriesName === 'L1-电流' || param.seriesName === 'L2-电流' || param.seriesName === 'L3-电流') {
//             result += ' A';
//           }
//           result += '<br>';
//         });
//         return result.trimEnd(); // 去除末尾多余的换行符
//       }},
//       legend: {
//         data: ['L1-电流', 'L2-电流', 'L3-电流'], // 图例项
//         selected:{
//           'L1-电流':true,
//           'L2-电流':true,
//           'L3-电流':true
//         }
//       },
//       grid: {left: '3%', right: '3%', bottom: '3%',containLabel: true},
//       toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
//       xAxis: {
//         type: 'category',nameLocation: 'end',
//         boundaryGap: false,
//         //axisLabel:{
//         //  show:true,
//         //  interval:0,
//         //  rotate:-45
//         //},
//         data:lineidDateTimes.value
//       },
//       yAxis: {
//         type: 'value'
//       },
//       series: [
//         {
//           name: 'L1-电流',
//           type: 'line',
//           data: lChartData.value.cur_max_value,
//           symbol: 'circle',
//           symbolSize: 4
//         },
//         {
//           name: 'L2-电流',
//           type: 'line',
//           data: llChartData.value.cur_max_value,
//           symbol: 'circle',
//           symbolSize: 4,
//           lineStyle:{type: 'dashed'}
//         },
//         {
//           name: 'L3-电流',
//           type: 'line',
//           data: lllChartData.value.cur_max_value,
//           symbol: 'circle',
//           symbolSize: 4,
//           lineStyle:{type: 'dashed'}
//         }
//       ]
//     }
//   }else if(flagValue.value == 1){
//     return {
//       title: { text: ''},
//       tooltip: { trigger: 'axis', formatter: function (params) {
//         let result = params[0].name + '<br>';
//         params.forEach(param => {
//           result += param.marker + param.seriesName + ': &nbsp;&nbsp;&nbsp;&nbsp' + param.value;
//           if (param.seriesName === 'A路最大功率' || param.seriesName === 'B路最大功率' || param.seriesName === 'C路最大功率') {
//             result += ' KW';
//           }
//           result += '<br>';
//         });
//         return result.trimEnd(); // 去除末尾多余的换行符
//         }
//       },
//       legend: {
//         data: ['A路最大功率', 'B路最大功率', 'C路最大功率'], // 图例项
//         selected: {'A路最大功率': true,
//     'B路最大功率': true,
//     'C路最大功率': true
//       }
//       },
//       grid: {left: '3%', right: '70%', bottom: '3%',containLabel: true,width: '100%'},
//       toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
//       xAxis: {
//         type: 'category',nameLocation: 'end',
//         boundaryGap: false,
//         //axisLabel:{
//         //  interval:0,
//         //  rotate:-45
//         //},
        
//         data:lineidDateTimes.value
//       },
//       yAxis: {
//         type: 'value',
//       },
//       series: [
//         {
//           name: 'A路最大功率',
//           type: 'line',
//           data: lChartData.value.pow_active_max_value,
//           symbol: 'circle',
//           symbolSize: 4,
//           lineStyle:{type: 'dashed'}
//         },
//         {
//           name: 'B路最大功率',
//           type: 'line',
//           data: llChartData.value.pow_active_max_value,
//           symbol: 'circle',
//           symbolSize: 4,
//           lineStyle:{type: 'dashed'}
//         },
//         {
//           name: 'C路最大功率',
//           type: 'line',
//           data: lllChartData.value.pow_active_max_value,
//           symbol: 'circle',
//           symbolSize: 4,
//           lineStyle:{type: 'dashed'}
//         }
//       ]
//     }
//   }
// }

const updateChartData = (chartData, dataArray) => {
  chartData.value.cur_max_value = dataArray.map(item => item.cur_max_value);
  chartData.value.pow_active_max_value = dataArray.map(item => item.pow_active_max_value);
};

window.addEventListener('resize',function(){
  lineidChart?.resize();
  lineidChartOne?.resize();
}
);

//获取电流信息
const getLineid = async (id, type,flagValue) => {
  queryParams1.id = id;
  queryParams1.type = type;
  queryParams1.startTime = getFullTimeByDate(startTime);
  queryParams1.endTime = getFullTimeByDate(endTime);
  const result = await PDUDeviceApi.getMaxLineHisdata(queryParams1)

  const lChartData = ref({
    cur_max_value : [] as number[], //电流
    pow_active_max_value : [] as number[], //功率
  });

  const llChartData = ref({
    cur_max_value : [] as number[],
    pow_active_max_value : [] as number[],
  });

  const lllChartData = ref({
    cur_max_value : [] as number[],
    pow_active_max_value : [] as number[],
  });

  const lineidDateTimes = ref([] as string[])

  createTimes.value = result.dateTimes[0]
  endTimes.value = result.dateTimes[result.dateTimes.length-1]

  if(type === 'hour'){
    lineidDateTimes.value = result.dateTimes.map(dateTimeStr => {
      // 找到最后一个冒号的位置
      let lastIndexOfColon = dateTimeStr.lastIndexOf(':');
      // 截取到最后一个冒号之前的部分
      return dateTimeStr.substring(0, lastIndexOfColon);
    })
  }else if(type === 'day'){
    lineidDateTimes.value = result.dateTimes.map(dateTimeStr => {
      return dateTimeStr.substring(0, 10);
    })
  }

  //result.dateTimes

  const lData = result.l
  const llData = result.ll
  const lllData = result.lll

  // 更新图表数据
  updateChartData(lChartData, lData);
  updateChartData(llChartData, llData);
  updateChartData(lllChartData, lllData);
 
  // 假设 updateChart 函数返回正确的 ECharts 配置对象
  const option = updateChart(lChartData, llChartData, lllChartData, lineidDateTimes);
 
  // 获取正确的图表容器
  const chartContainer = flagValue === 0 ? document.getElementById('lineidChartContainer') : document.getElementById('lineidChartContainerOne');
  if (!chartContainer) {
    console.error('Chart container not found');
    return;
  }
 
  // 检查是否已经存在一个图表实例
  if (!lineidChart || lineidChart.getDom() !== chartContainer) {
    // 如果不存在或者容器已更改，则创建新的图表实例
    if (lineidChart) {
      // 如果存在旧实例，则销毁它
      lineidChart.dispose();
    }
    lineidChart = echarts.init(chartContainer);
  }
  

  // 设置图表选项
  lineidChart.setOption(option, true);
}

const lineidBeforeChartUnmount = () => {
  lineidChart?.dispose() // 销毁图表实例
  dialogVisible.value = false
}

const lineidBeforeChartUnmountOne = () => {
  lineidChartOne?.dispose() // 销毁图表实例
  dialogVisibleOne.value = false
}

const showDialog = (id, type,flagValue,l1MaxCur) => {
  lineidChart?.dispose()
  getLineid(id, type,flagValue)
  if(l1MaxCur!= null && l1MaxCur != undefined && l1MaxCur != 0){
  dialogVisible.value = true
}else {
  ElMessage({
    message: '该设备没有数据',
    type: 'warning',
  });
}
  flagValue.value = 0
}

const showDialogOne = (id,type,flagValue,l1MaxPow) => {
  lineidChartOne?.dispose()
  getLineid(id, type,flagValue)
  console.log('l1MaxPow',l1MaxPow)
  console.log('id',id)
  if(l1MaxPow!= null && l1MaxPow != undefined && l1MaxPow != 0){
  dialogVisibleOne.value = true
}else {
  ElMessage({
    message: '该设备没有数据',
    type: 'warning',
  });
}
  flagValue.value = 1
}

window.addEventListener('resize', function() {
  lineidChart?.resize();
  lineidChartOne?.resize();
});
</script>

<style scoped lang="scss">

.text-ellipsis {
  white-space: nowrap;    /* 禁止换行 */
  overflow: hidden;       /* 隐藏溢出内容 */
  text-overflow: ellipsis; /* 显示省略号 */
  width: 120px;
}
:deep(.ip:hover) {
  color: blue !important;
  cursor: pointer;
}

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

.btn_offline,
.btn_normal,
.btn_warn,
.btn_error {
  width: 58px;
  height: 35px;
  cursor: pointer;
  border-radius: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
  &:hover {
    color: #7bc25a;
  }
}
.btn_offline {
  border: 1px solid #aaa;
  background-color: #fff;
  margin-right: 8px;
}
.offline {
  background-color: #aaa;
  color: #fff;
  &:hover {
    color: #fff;
  }
}
.btn_normal {
  border: 1px solid #3bbb00;
  background-color: #fff;
  margin-right: 8px;
}
.normal {
  background-color: #3bbb00;
  color: #fff;
  &:hover {
    color: #fff;
  }
}
.btn_warn {
  border: 1px solid #ffc402;
  background-color: #fff;
  margin-right: 8px;
}
.warn {
  background-color: #ffc402;
  color: #fff;
  &:hover {
    color: #fff;
  }
}
.btn_error {
  border: 1px solid #fa3333;
  background-color: #fff;
  margin-right: 8px;
}
.error {
  background-color: #fa3333;
  color: #fff;
  &:hover {
    color: #fff;
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
.progressContainer {
  display: flex;
  justify-content: center;
  align-items: center;
  .progress {
    width: 100px;
    height: 18px;
    line-height: 18px;
    font-size: 12px;
    box-sizing: border-box;
    border-radius: 150px;
    overflow: hidden;
    position: relative;
    vertical-align: middle;
    background-color: #bfa;
    display: flex;
    justify-content: center;
    .left {
      overflow: hidden;
      box-sizing: border-box;
      background-color: var(--el-color-primary);

    }
    .right {
      overflow: hidden;
      background-color:  #f56c6c;
    }
  }
}

@media screen and (min-width:2048px) {
  .table-height{
    height: 78vh;
    margin-top:-10px;
    overflow: hidden;
    overflow-y: auto;
  }
  .arrayContainer {
    display: flex;
    flex-wrap: wrap;
    .arrayItem {
      width: 20%;
      height: 140px;
      box-sizing: border-box;
      background-color: #eef4fc;
      border: 5px solid #fff;
      padding-top: 40px;
      position: relative;
      .content {
        display: flex;
        align-items: center;
        .icon {
          width: 60px;
          height: 30px;
          margin: 0 28px;
          text-align: center;
        }
        .info{
          padding-left: 1vw;
          font-size: 15px;
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
}

@media screen and (max-width:2048px) and (min-width:1600px) {
  .table-height{
    height: 720px;
    margin-top:-10px;
    overflow: hidden;
    overflow-y: auto;
  }
  .arrayContainer {
    display: flex;
    flex-wrap: wrap;
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
          width: 60px;
          height: 30px;
          margin: 0 28px;
          text-align: center;
        }
        .info{
          padding-left: 1vw;
          font-size: 15px;
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
}

@media screen and (max-width:1600px) {
  .table-height{
    height: 600px;
    margin-top:-10px;
    overflow: hidden;
    overflow-y: auto;
  }
  .arrayContainer {
    display: flex;
    flex-wrap: wrap;
    .arrayItem {
      width: 33%;
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
          width: 60px;
          height: 30px;
          margin: 0 28px;
          text-align: center;
        }
        .info{
          padding-left: 1vw;
          font-size: 15px;
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
}

:deep(.master-left .el-card__body) {
  padding: 0;
}
:deep(.el-form) {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
}
:deep(.el-form .el-form-item) {
  margin-right: 0;
}
.description-item {
  display: flex;
  align-items: left;
  margin-top: 15px;
  margin-left: 10px;
  
}

.label {
  width:100px; /* 控制冒号前的宽度 */
  //text-align: right; /* 文本右对齐 */
  //margin-right: 10px; /* 控制冒号后的间距 */
}

.value {
  flex: 1; /* 自动扩展以对齐数据 */
  
}
.line {
  height: 1px;
  margin-top: 28px;
  margin-bottom: 20px;
  background: linear-gradient(297deg, #fff, #dcdcdc 51%, #fff);
}

:deep(.el-dialog) {
  width: 80%;
  margin-top: 70px;
}
.custom-content-container{
  display: flex;
  justify-content: space-between;
  flex-wrap: nowrap;
}
.adaptiveStyle {
  z-index: 5;
}
/* 尝试隐藏关闭按钮，但可能不总是有效 */
:deep(.el-dialog__headerbtn) {
  display: none !important; /* 使用 !important 尝试提高优先级 */
}
.custom-content{
  display: flex;
  flex-direction: column;
  justify-content: center;
}
::v-deep .el-table .el-table__header th{
  background-color: #f7f7f7;
  color: #909399;
  height: 60px;
}

:deep(.el-card){
  --el-card-padding:5px;
}
.cardChilc {
  flex: 1;
  margin: 0 10px;
  box-sizing: border-box;
}
</style>
