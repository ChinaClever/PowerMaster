<template>
  <CommonMenu @check="handleCheck"  @node-click="handleClick" :showSearch="true" :dataList="serverRoomArr" navTitle="母线电力">
    <template #NavInfo>
      <div>
        <div class="status">
          <div class="box">
            <div class="top">
              <div class="tag"></div>正常
            </div>
            <div class="value"><span class="number">{{ statusNumber.normal }}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag empty"></div>离线
            </div>
            <div class="value"><span class="number">{{ statusNumber.offline }}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag error"></div>告警
            </div>
            <div class="value"><span class="number">{{ statusNumber.alarm }}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag empty"></div>总共
            </div>
            <div class="value"><span class="number">{{ busTotal }}</span>个</div>
          </div>
        </div>        
        <div class="line"></div>

      </div>
    </template>
    <template #ActionBar>
      <el-form
        class="-mb-15px"
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        label-width="68px"
        v-show="switchValue !== 4"  
      > <el-form-item>
        <el-form-item>
          <el-button :class="{ 'btnallSelected': butColor === 0 , 'btnallNotSelected': butColor === 1 }" type = "button" @click="toggleAllStatus">全部</el-button>
          <template v-for="(status,index) in statusList" :key="index">
            <button v-if="butColor === 0" :class="[status.activeClass]" @click.prevent="handleSelectStatus(status.value)">{{status.name}}</button>
            <button v-else-if="butColor === 1" :class="[onclickColor === status.value ? status.activeClass:status.cssClass]" @click.prevent="handleSelectStatus(status.value)">{{status.name}}</button>
          </template>
        </el-form-item>
        <el-form-item label="网络地址" prop="devKey" style="margin-left:10px">
          <el-autocomplete
            v-model="queryParams.devKey"
            :fetch-suggestions="querySearch"
            clearable
            class="!w-160px"
            placeholder="请输入网络地址"
            @select="handleQuery"
          />
        <el-form-item style="margin-left: 5px;margin-right: 6px">
          <el-button @click="handleQuery" style="width:70px"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
          <el-button @click="resetQuery" style="width:70px;" ><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
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
        </el-form-item >
      </el-form-item>
        <div style="float:right;margin-top: 1.5px;">
          <el-button @click="valueMode = 0;" :type="valueMode == 0 ? 'primary' : ''" ><Icon icon="ep:grid" style="margin: 0px" />电流</el-button>            
          <el-button @click="valueMode = 1;" :type="valueMode == 1 ? 'primary' : ''" ><Icon icon="ep:grid" style="margin: 0px" />电压</el-button>            
          <el-button @click="valueMode = 2;" :type="valueMode == 2 ? 'primary' : ''" style="width:95px"><Icon icon="ep:grid" style="margin: 0px" />有功功率</el-button>            
          <el-button @click="valueMode = 3;" :type="valueMode == 3 ? 'primary' : ''" style="width:95px"><Icon icon="ep:grid" style="margin: 0px" />无功功率</el-button>
          <el-button @click="valueMode = 4;" :type="valueMode == 4 ? 'primary' : ''" style="width:95px"><Icon icon="ep:grid" style="margin-right: 3px" />视在功率</el-button>
          <el-button @click="pageSizeArr=[24,36,48,96];queryParams.pageSize = 24;switchValue = 0;showPagination = 0" style="width:95px" :type="switchValue == 0 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 3px" />阵列模式</el-button>
          <el-button @click="pageSizeArr=[15, 25,30, 50, 100];queryParams.pageSize = 15;switchValue = 3;showPagination = 0" style="width:95px" :type="switchValue == 3 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 3px" />表格模式</el-button>
          <el-button @click="pageSizeArr=[15, 25,30, 50, 100];queryDeletedPageParams.pageSize = 15;getDeletedList();switchValue = 4;showPagination = 1" style="width:80px" :type="switchValue === 4 ? 'primary' : ''" v-show="switchValue == 3"><Icon icon="ep:expand" style="margin-right: 4px" />已删除</el-button>      
        </div>
      </el-form>
      <el-form
        class="-mb-15px"
        :model="queryDeletedPageParams"
        ref="queryFormRef"
        :inline="true"
        label-width="68px"     
        v-show="switchValue == 4"        
      >
        <el-form-item label="网络地址" prop="devKey">
          <el-autocomplete
            v-model="queryDeletedPageParams.devKey"
            :fetch-suggestions="querySearch"
            clearable
            class="!w-200px"
            placeholder="请输入网络地址"
            @select="handleQuery"
          />
        <el-form-item style="margin-left: 10px;">
          <el-button @click="handleQuery" style="width:70px"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
          <el-button @click="resetQuery" style="width:70px"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
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
        <div style="float:right">                
          <el-button @click="pageSizeArr=[24,36,48,96];queryParams.pageSize = 24;switchValue = 0;showPagination = 0" :type="switchValue == 0 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />阵列模式</el-button>
          <el-button @click="pageSizeArr=[15, 25,30, 50, 100];queryParams.pageSize = 15;switchValue = 3;showPagination = 0" :type="switchValue == 3 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 4px" />表格模式</el-button>
          <el-button @click="pageSizeArr=[15, 25,30, 50, 100];queryDeletedPageParams.pageSize = 15;getDeletedList();switchValue = 4;showPagination = 1" :type="switchValue === 4 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 8px" />已删除</el-button>      
        </div>
      </el-form>
    </template>
    <template #Content>
      <div v-if="switchValue && list.length > 0" style="height:720px;margin-top:-10px;overflow-y: auto;">
        <el-table v-if="switchValue == 3" v-loading="loading" :data="list"  @cell-dblclick="toDeatil" :border="true">
        <el-table-column label="编号" align="center" prop="tableId" width="80px"/>
        <el-table-column label="状态" min-width="50" align="center">
          <template #default="scope">
            <div :style="{color: statusList[scope.row.status].color}">{{statusList[scope.row.status] && statusList[scope.row.status].name}}</div>
          </template>
        </el-table-column>
        <!-- 数据库查询 -->
        <el-table-column label="所在位置" min-width="110" align="center" prop="location" />
        <el-table-column label="设备名称" align="center" prop="busName" />
        <el-table-column label="网络地址" align="center" prop="devKey" :class-name="ip"/>
        <el-table-column v-if="valueMode == 0" label="A相电流(A)" align="center" prop="acur" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.acur != null" :type=" scope.row.acurStatus != 0 ? 'danger' : '' ">
              {{ scope.row.acur }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 0" label="B相电流(A)" align="center" prop="bcur" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.bcur != null" :type=" scope.row.bcurStatus != 0 ? 'danger' : '' ">
              {{ scope.row.bcur }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 0" label="C相电流(A)" align="center" prop="ccur" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.ccur != null" :type=" scope.row.ccurStatus != 0 ? 'danger' : '' ">
              {{ scope.row.ccur }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column  v-if="valueMode == 1"  label="A相电压(V)" align="center" prop="avol" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.avol" :type=" scope.row.avolStatus != 0 ? 'danger' : '' ">
              {{ scope.row.avol }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 1" label="B相电压(V)" align="center" prop="bvol" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.bvol" :type=" scope.row.bvolStatus != 0 ? 'danger' : '' ">
              {{ scope.row.bvol }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 1" label="C相电压(V)" align="center" prop="cvol" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.cvol" :type=" scope.row.cvolStatus != 0 ? 'danger' : '' ">
              {{ scope.row.cvol }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 2" label="A相有功功率(kW)" align="center" prop="aactivePow" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.aactivePow" :type=" scope.row.aactivePowStatus != 0 ? 'danger' : '' ">
              {{ scope.row.aactivePow }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 2" label="B相有功功率(kW)" align="center" prop="bactivePow" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.bactivePow" :type=" scope.row.bactivePowStatus != 0 ? 'danger' : '' ">
              {{ scope.row.bactivePow }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 2" label="C相有功功率(kW)" align="center" prop="cactivePow" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.cactivePow" :type=" scope.row.cactivePowStatus != 0 ? 'danger' : '' ">
              {{ scope.row.cactivePow }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 2" label="总有功功率(kW)" align="center" prop="powValue" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.powValue" :type=" scope.row.powStatus != 0 ? 'danger' : '' ">
              {{ scope.row.powValue }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 3" label="A相无功功率(kVar)" align="center" prop="areactivePow" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.aactivePow">
              {{ scope.row.aactivePow }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 3" label="B相无功功率(kVar)" align="center" prop="breactivePow" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.breactivePow">
              {{ scope.row.breactivePow }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 3" label="C相无功功率(kVar)" align="center" prop="creactivePow" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.creactivePow">
              {{ scope.row.creactivePow }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 3" label="总无功功率(kVar)" align="center" prop="powReactive" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.powReactive">
              {{ scope.row.powReactive }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 4" label="A相视在功率(kVA)" align="center" prop="apowApparent" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.apowApparent">
              {{ scope.row.apowApparent }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 4" label="B相视在功率(kVA)" align="center" prop="bpowApparent" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.bpowApparent">
              {{ scope.row.bpowApparent }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 4" label="C相视在功率(kVA)" align="center" prop="cpowApparent" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.cpowApparent">
              {{ scope.row.cpowApparent }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 4" label="总视在功率(kVA)" align="center" prop="powApparent" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.powApparent">
              {{ scope.row.powApparent }}
            </el-text>
          </template>
        </el-table-column>
        <!-- 数据库查询 -->
        <el-table-column label="操作" align="center">
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click="toDeatil(scope.row)"
              v-if="scope.row.status != null && scope.row.status != 0"
              style="background-color:#409EFF;color:#fff;border:none;width:100px;height:30px;"
            >
            设备详情
            </el-button>
            <el-button
              link
              type="danger"
              @click="handleDelete(scope.row.busId)"
              v-if="scope.row.status == 0"
              style="background-color:#fa3333;color:#fff;border:none;width:60px;height:30px;"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>    
    <!-- 查询已删除-->
      <el-table v-else-if="switchValue == 4" v-loading="loading" :data="deletedList" :stripe="true" :show-overflow-tooltip="true"  :border=true>
        <el-table-column label="编号" align="center" prop="tableId" width="80px"/>
        <!-- 数据库查询 -->
        <el-table-column label="所在位置" align="center" prop="location" />
        <el-table-column label="设备名称" align="center" prop="busName" />
        <el-table-column label="运行状态" align="center" prop="status" >
          <template #default="scope">
            <el-tag type="info" v-if="scope.row.status">已删除</el-tag>
          </template>
        </el-table-column>        
        <el-table-column label="网络地址" align="center" prop="devKey" :class-name="ip"/>
        <!-- 数据库查询 -->
        <el-table-column label="操作" align="center">
          <template #default="scope">
            <el-button
              link
              type="danger"
              @click="handleRestore(scope.row.busId)"
            >
              恢复设备
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      </div>
      <Pagination
        v-show="showPagination == 1"
        :total="deletedTotal"
        :page-size-arr="pageSizeArr"
        v-model:page="queryDeletedPageParams.pageNo"
        v-model:limit="queryDeletedPageParams.pageSize"
        @pagination="getDeletedList"
      />     
      <div v-if="switchValue == 0  && list.length > 0" class="arrayContainer">
        <template v-for="item in list" :key="item.devKey">
          <div v-if="item.id !== null" class="arrayItem">
          <div class="devKey">{{ item.location != null ? item.location : item.devKey }}</div>
          <div class="content">
            
            <div class="info" v-if="valueMode == 0" style="padding: 0 18px;margin-right:30px;">                  
              <div v-if="item.acur != null">
                <el-text v-if="item.acur != null" :type=" item.acurStatus != 0 ? 'danger' : '' ">
                  A相：{{item.acur}}A
                </el-text>
              </div>
              <div v-if="item.bcur != null">
                <el-text v-if="item.bcur != null" :type=" item.bcurStatus != 0 ? 'danger' : '' ">
                  B相：{{item.bcur}}A
                </el-text>
              </div>
              <div v-if="item.ccur != null">
                <el-text v-if="item.ccur != null" :type=" item.ccurStatus != 0 ? 'danger' : '' ">
                  C相：{{item.ccur}}A
                </el-text>
              </div>
            </div>
            <div class="info" v-if="valueMode == 1" style="padding: 0 18px;margin-right:30px;">                  
              <div v-if="item.avol != null">
                <el-text v-if="item.avol != null" :type=" item.avolStatus != 0 ? 'danger' : '' ">
                  A相：{{item.avol}}V
                </el-text>
              </div>
              <div  v-if="item.bvol != null">
                <el-text v-if="item.bvol != null" :type=" item.bvolStatus != 0 ? 'danger' : '' ">
                  B相：{{item.bvol}}V
                </el-text>
              </div>
              <div v-if="item.cvol != null">
                <el-text v-if="item.cvol != null" :type=" item.cvolStatus != 0 ? 'danger' : '' ">
                  C相：{{item.cvol}}V
                </el-text>
              </div>
            </div>
            <div class="info" v-if="valueMode == 2" style="padding: 0 18px;margin-right:30px;">                  
              <div  v-if="item.aactivePow != null">
                <el-text v-if="item.aactivePow != null" :type=" item.aactivePowStatus != 0 ? 'danger' : '' ">
                  A相：{{item.aactivePow}}kW
                </el-text>
              </div>
              <div  v-if="item.bactivePow != null">
                <el-text v-if="item.bactivePow != null" :type=" item.bactivePowStatus != 0 ? 'danger' : '' ">
                  B相：{{item.bactivePow}}kW
                </el-text>
              </div>
              <div  v-if="item.cactivePow != null">
                <el-text v-if="item.cactivePow != null" :type=" item.cactivePowStatus != 0 ? 'danger' : '' ">
                  C相：{{item.cactivePow}}kW
                </el-text>
              </div>
            </div>
            <div class="info" v-if="valueMode == 3" style="padding: 0 18px;margin-right:30px;">                  
              <div v-if="item.areactivePow != null">
                <el-text v-if="item.areactivePow != null">
                  A相：{{item.areactivePow}}kVar
                </el-text>
              </div>
              <div v-if="item.breactivePow != null">
                <el-text v-if="item.breactivePow != null">
                  B相：{{item.breactivePow}}kVar
                </el-text>
              </div>
              <div v-if="item.creactivePow != null">
                <el-text v-if="item.creactivePow != null">
                  C相：{{item.creactivePow}}kVar
                </el-text>
              </div>
            </div>
            <div class="info" v-if="valueMode == 4" style="padding: 0 18px;margin-right:30px;">                  
              <div v-if="item.apowApparent != null">
                <el-text v-if="item.apowApparent != null">
                  A相：{{item.apowApparent}}kVA
                </el-text>
              </div>
              <div v-if="item.bpowApparent != null">
                <el-text v-if="item.bpowApparent != null">
                  B相：{{item.bpowApparent}}kVA
                </el-text>
              </div>
              <div v-if="item.cpowApparent != null">
                <el-text v-if="item.cpowApparent != null">
                  C相：{{item.cpowApparent}}kVA
                </el-text>
              </div>
            </div>
            <div style="padding: 0 4px" v-if="valueMode == 0 && item.acur != null"><Bar :width="80" :height="100" :max="{L1:item.acur,L2:item.bcur,L3:item.ccur,L4:item.acurStatus,L5:item.bcurStatus,L6:item.ccurStatus}" /></div>
            <div style="padding: 0 4px" v-if="valueMode == 1 && item.avol != null"><Bar :width="80" :height="100" :max="{L1:item.avol,L2:item.bvol,L3:item.cvol,L4:item.avolStatus,L5:item.bvolStatus,L6:item.cvolStatus}" /></div>
            <div class="icon" v-if="valueMode != 0 && valueMode != 1">
              <!-- <div v-if="valueMode == 0 && item.acur != null" style="font-size:large">
                电流
              </div>     -->
              <!-- <div v-if="valueMode == 1 && item.avol != null" style="font-size:large">
                电压
              </div> -->
              <div v-if="valueMode == 2 && item.aactivePow != null" style="font-size:20px;">
                {{item.powValue?.toFixed(3)}}
              </div>
              <div v-if="valueMode == 2 && item.aactivePow != null" style="font-size:16px;" >
                总有功功率(kW)
              </div>
              <div v-if="valueMode == 3 && item.areactivePow != null" style="font-size:20px;">
                {{item.powReactive?.toFixed(3)}}
              </div>
              <div v-if="valueMode == 3 && item.areactivePow != null" style="font-size:16px;" >
                总无功功率(kVar)
              </div>
              <div v-if="valueMode == 4 && item.areactivePow != null" style="font-size:20px;">
                {{item.powApparent?.toFixed(3)}}
              </div>
              <div v-if="valueMode == 4 && item.powApparent != null" style="font-size:16px;" >
                总视在功率(kVA)
              </div> 
            </div>
          </div>
          <!-- <div class="room">{{item.jf}}-{{item.mc}}</div> -->
          <div class="status" v-if="valueMode == 0">
            <el-tag type="info" v-if="item.status === 0" >离线</el-tag>
            <el-tag type="danger" v-else-if="item.status === 2" >告警</el-tag>
            <el-tag type="success" v-else-if="item.status === 1" >正常</el-tag>
          </div>
          <div class="status" v-if="valueMode == 1">
            <el-tag type="info" v-if="item.status === 0" >离线</el-tag>
            <el-tag type="danger" v-else-if="item.status === 2" >告警</el-tag>
            <el-tag type="success" v-else-if="item.status === 1" >正常</el-tag>
          </div>
          <div class="status" v-if="valueMode == 2">
            <el-tag type="info" v-if="item.status === 0" >离线</el-tag>
            <el-tag type="danger" v-else-if="item.status === 2" >告警</el-tag>
            <el-tag type="success" v-else-if="item.status === 1" >正常</el-tag>
          </div>
          <div class="status" v-if="valueMode == 3">
            <el-tag type="info" v-if="item.status === 0" >离线</el-tag>
            <el-tag type="success" v-else-if="item.status === 1" >正常</el-tag>
          </div>
          <div class="status" v-if="valueMode == 4">
            <el-tag type="info" v-if="item.status === 0" >离线</el-tag>
            <el-tag type="success" v-else >正常</el-tag>
          </div>
          <button class="detail" @click="toDeatil(item)" v-if="item.status != null && item.status != 0" >详情</button>
        </div>
        </template>
      </div>
      <Pagination
        v-show="showPagination == 0"
        :total="total"
        :page-size-arr="pageSizeArr"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
      <template v-if="list.length == 0 && showPagination == 0">
        <el-empty description="暂无数据" :image-size="595" />
      </template>
    </template>
  </CommonMenu>


  <!-- 表单弹窗：添加/修改 -->
  <!-- <CurbalanceColorForm ref="curBalanceColorForm" @success="getList" /> -->
</template>

<script setup lang="ts">
// import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { IndexApi } from '@/api/bus/busindex'
// import CurbalanceColorForm from './CurbalanceColorForm.vue'
import { ElTree } from 'element-plus'
import Bar from './Bar.vue'
// import { CurbalanceColorApi } from '@/api/pdu/curbalancecolor'

/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })

const { push } = useRouter()
const showPagination = ref(0)
const curBalanceColorForm = ref()
const flashListTimer = ref();
const firstTimerCreate = ref(true);
const pageSizeArr = ref([24,36,48,96])
const switchValue = ref(0)
const valueMode = ref(0)
const statusNumber = reactive({
  normal : 0,
  warn : 0,
  alarm : 0,
  offline : 0
})
const normalFlag = ref(true)
const reportFlag = ref(true)
const offlineFlag = ref(true)

const butColor = ref(0);
const onclickColor = ref(-1);

const statusList = reactive([
  {
    name: '离线',
    selected: true,
    value: 0,
    cssClass: 'btn_offline',
    activeClass: 'btn_offline offline'
  },
  {
    name: '正常',
    selected: true,
    value: 1,
    cssClass: 'btn_normal',
    activeClass: 'btn_normal normal',
    color: '#3bbb00'
  },
  {
    name: '告警',
    selected: true,
    value: 2,
    cssClass: 'btn_error',
    activeClass: 'btn_error error',
    color: '#fa3333'
  }
])
const devKeyList = ref([])
const loadAll = async () => {
  var data = await IndexApi.devKeyList();
  var objectArray = data.map((str) => {
    return { value: str };
  });
  return objectArray;
}

const querySearch = (queryString: string, cb: any) => {

  const results = queryString
    ? devKeyList.value.filter(createFilter(queryString))
    : devKeyList.value
  // call callback function to return suggestions
  cb(results)
}

const createFilter = (queryString: string) => {
  return (devKeyList) => {
    return (
      devKeyList.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0
    )
  }
}

const handleClick = (row) => {
  console.log("click",row)
}

const handleCheck = async (row) => {
  if(row.length == 0){
    queryParams.busDevKeyList = null;
    queryDeletedPageParams.busDevKeyList = null;
    getList();
    getDeletedList();
    return;
  }
  const ids = [] as any
  var haveCabinet = false;
  row.forEach(item => {
    if (item.type == 6) {
      ids.push(item.unique)
      haveCabinet = true;
    }
  })
  if(!haveCabinet ){
    queryParams.busDevKeyList = [-1]
    queryDeletedPageParams.busDevKeyList = [-1]
  }else{
    queryParams.busDevKeyList = ids
    queryDeletedPageParams.busDevKeyList = ids
  }

  getList();
  getDeletedList();
}

const serverRoomArr =  ref([])

const filterText = ref('')
const treeRef = ref<InstanceType<typeof ElTree>>()

watch(filterText, (val) => {
  treeRef.value!.filter(val)
})


const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

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
    pf:null,
    acur : null,
    bcur : null,
    ccur : null,
    curUnbalance : null,
  }
]) as any// 列表的数据
const allList = ref([
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
const deletedList = ref([
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
    aloadRate : null,
    bloadRate : null,
    cloadRate : null,
    curUnbalance : null,
  }
]) as any// 列表的数据
const total = ref(0) // 列表的总页数
const busTotal = ref(0)//母线总数
const deletedTotal = ref(0) // 已删除列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 24,
  devKey: undefined,
  createTime: [],
  cascadeNum: undefined,
  serverRoomData:undefined,
  status:undefined,
  cabinetIds : [],
  isDeleted: 0,
})as any
const queryParamsAll = reactive({
  pageNo: 1,
  pageSize: -1,
  devKey: undefined,
  createTime: [],
  cascadeNum: undefined,
  serverRoomData:undefined,
  status:undefined,
  cabinetIds : [],
  isDeleted: 0,
})as any
const queryDeletedPageParams = reactive({
  pageNo: 1,
  pageSize: 24,
  devAddr: undefined,
  createTime: [],
  cascadeNum: undefined,
  serverRoomData:undefined,
  status:undefined,
  cabinetIds : [],
  isDeleted: 1,
})as any
const queryFormRef = ref() // 搜索的表单
const queryFormRef2 = ref()
const exportLoading = ref(false) // 导出的加载中

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await IndexApi.getBusRedisPage(queryParams)
    list.value = data.list
    filterData()
    console.log('查询列表的数据',list.value)
    var tableIndex = 0;

    list.value.forEach((obj) => {
      obj.tableId = (queryParams.pageNo - 1) * queryParams.pageSize + ++tableIndex;
      if(obj?.acur == null){
        return;
      } 
      obj.acur = obj.acur?.toFixed(2);
      obj.bcur = obj.bcur?.toFixed(2);
      obj.ccur = obj.ccur?.toFixed(2);
      //测试数据
      // obj.acur = 0.3
      // obj.bcur = 0.6
      // obj.ccur = 0.9
      // obj.avol = 210
      // obj.bvol = 250
      // obj.cvol = 400
      // obj.acurStatus = 1
      // obj.cvolStatus = 9
      obj.avol = obj.avol?.toFixed(1);
      obj.bvol = obj.bvol?.toFixed(1);
      obj.cvol = obj.cvol?.toFixed(1);
      obj.aactivePow = obj.aactivePow?.toFixed(3);
      obj.bactivePow = obj.bactivePow?.toFixed(3);
      obj.cactivePow = obj.cactivePow?.toFixed(3);
      obj.areactivePow = obj.areactivePow?.toFixed(3);
      obj.breactivePow = obj.breactivePow?.toFixed(3);
      obj.creactivePow = obj.creactivePow?.toFixed(3);
      obj.powApparent = obj.powApparent?.toFixed(3);
    });

    total.value = data.total
  } finally {
    loading.value = false
  }
}

const getDeletedList = async () => {
  try {
    const data = await IndexApi.getDeletedIndexPage(queryDeletedPageParams)
    deletedList.value = data.list
    var tableIndex = 0;    

    deletedList.value.forEach((obj) => {
      obj.tableId = (queryDeletedPageParams.pageNo - 1) * queryDeletedPageParams.pageSize + ++tableIndex;
    });

    deletedTotal.value = data.total
  } catch (error) {
    
  }
}

const getListAll = async () => {
  try {

    const allData =  await IndexApi.getBusIndexStatistics()
    //设置左边数量
    statusNumber.normal = allData.normal;
    statusNumber.offline = allData.offline;
    statusNumber.alarm = allData.alarm;
    statusNumber.warn = allData.warn;
    busTotal.value = allData.total
  } catch (error) {
    
  }
}

const getNavList = async() => {
  const res = await IndexApi.getBusMenu()
  serverRoomArr.value = res
  if (res && res.length > 0) {
    const room = res[0]
    const keys = [] as string[]
    room.children.forEach(child => {
      if(child.children.length > 0) {
        child.children.forEach(son => {
          keys.push(son.id + '-' + son.type)
        })
      }
    })
  }
}

const toDeatil = (row) =>{
  const devKey = row.devKey;
  const busId = row.busId;
  const location = row.location != null ? row.location : row.devKey
  const busName = row.busName;
  push({path: '/bus/busmonitor/buspowerdetail', state: { devKey, busId , location , busName }})

}

const filterData = () => {
  const data0 = list.value.filter(item => item.status === 1 && item.acurStatus != null); // 正常状态数据
  console.log('data0',data0)
  const data1 = list.value.filter(item => item.status === 2); // 告警状态数据
  console.log('data1',data1)
  const data2 = list.value.filter(item => item.status === 0 || item.acurStatus == null || item.status == null); // 离线状态数据
  console.log('data2',data2)
 
  if (normalFlag.value && !reportFlag.value && !offlineFlag.value) {
    list.value = data0; // 仅正常状态
  } else if (reportFlag.value && !normalFlag.value && !offlineFlag.value) {
    list.value = data1; // 仅告警状态
  } else if (offlineFlag.value && !normalFlag.value && !reportFlag.value) {
    list.value = data2; // 仅离线状态
  } else if (normalFlag.value && reportFlag.value && !offlineFlag.value) {
    list.value = [...data0, ...data1];
  } else if (normalFlag.value && offlineFlag.value && !reportFlag.value) {
    list.value = [...data0, ...data2];
  } else if (reportFlag.value && offlineFlag.value && !normalFlag.value) {
    list.value = [...data1, ...data2];
  } else if (normalFlag.value && reportFlag.value && offlineFlag.value) {
    list.value = [...data0, ...data1, ...data2];
  } else {
    list.value = list.value;
  }

  console.log('执行完毕')
}

const handleSelectStatus = (index) => {
  butColor.value = 1;
  onclickColor.value = index;
  queryParams.status = [index];
  handleQuery();
}

const toggleAllStatus = () => {
  butColor.value = 0;
  onclickColor.value = -1;
  queryParams.status = [];
  handleQuery();
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
  getDeletedList();
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  butColor.value = 0;
  queryParams.status = [];
  onclickColor.value = -1;
  handleQuery()
}

/** 添加/修改操作 */

const openForm = (type: string) => {
  curBalanceColorForm.value.open(type)
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await IndexApi.deleteIndex(id)
    message.success(t('common.delSuccess'))
    // 刷新列表
     await getList()
  } catch {}
}

/** 恢复按钮操作 */
const handleRestore = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await IndexApi.restoreIndex(id)
    message.success(t('common.restoreSuccess'))
    // 刷新列表
     await getDeletedList()
  } catch {}
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    exportLoading.value = true
    const data = await IndexApi.exportIndex(queryParams)
    download.excel(data, 'PDU设备.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

/** 初始化 **/
onMounted(async () => {
  devKeyList.value = await loadAll();
  getList()
  getNavList();
  getListAll();
  // flashListTimer.value = setInterval((getListNoLoading), 5000);
  flashListTimer.value = setInterval((getList), 5000);
})

onBeforeUnmount(()=>{
  if(flashListTimer.value){
    clearInterval(flashListTimer.value)
    flashListTimer.value = null;
  }
})

onBeforeRouteLeave(()=>{
  if(flashListTimer.value){
    clearInterval(flashListTimer.value)
    flashListTimer.value = null;
    firstTimerCreate.value = false;
  }
})

onActivated(() => {
  getList()
  getNavList();
  if(!firstTimerCreate.value){
    // flashListTimer.value = setInterval((getListNoLoading), 5000);
    flashListTimer.value = setInterval((getList), 5000);
  }
})
</script>

<style scoped lang="scss">
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
    margin-top: 30px;
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
    margin-top: 18px;
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

@media screen and (min-width:2048px){
  .arrayContainer {
    width:100%;
    height: 720px;
    overflow: hidden;
    overflow-y: auto;
    display: flex;
    flex-wrap: wrap;
    align-content: flex-start;
    margin-top: -10px;

    .arrayItem {
      width: 20%;
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
        height: 100%;
        .icon {
          font-size: 20px;
          width: 150px;
          height: 100px;
          text-align: center;
          .text-pf{
            font-size: 16px;
          }
        }
        .info{
          font-size: 16px;
          margin-bottom: 20px;
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
        margin-left: -30px;
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
  .arrayContainer {
    width:100%;
    height: 720px;
    overflow: hidden;
    overflow-y: auto;
    display: flex;
    flex-wrap: wrap;
    align-content: flex-start;
    margin-top: -10px;

    .arrayItem {
      width: 25%;
      height: 140px;
      font-size: 13px;
      box-sizing: border-box;
      background-color: #eef4fc;
      border: 5px solid #fff;
      padding-top: 40px;
      position: relative;
      border-radius: 7px;
      .content {
        display: flex;
        align-items: center;
        height: 100%;
        .icon {
          font-size: 20px;
          width: 150px;
          height: 100px;
          margin-left: -30px;
          text-align: center;
          .text-pf{
            font-size: 16px;
          }
        }
        .info{
          font-size: 16px;
          margin-bottom: 20px;
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
  .arrayContainer {
    width:100%;
    height: 720px;
    overflow: hidden;
    overflow-y: auto;
    display: flex;
    flex-wrap: wrap;
    align-content: flex-start;
    margin-top: -10px;

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
        height: 100%;
        .icon {
          font-size: 20px;
          width: 150px;
          height: 100px;
          margin-left: -30px;
          text-align: center;
          .text-pf{
            font-size: 16px;
          }
        }
        .info{
          font-size: 16px;
          margin-bottom: 20px;
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

.btnallSelected {
  margin-right: 10px;
  width: 58px;
  height: 35px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #409EFF;
  color: white;
  border: none;
  border-radius: 5px;
}

.btnallNotSelected{
  margin-right: 10px;
  width: 58px;
  height: 35px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  color: #000000;
  border: 1px solid #409EFF;
  border-radius: 5px;
  &:hover {
    color: #7bc25a;
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

::v-deep .el-table .el-table__header th{
  background-color: #f5f7fa;
  color: #909399;
  height: 80px;
}

:deep(.el-card){
  --el-card-padding:5px;
}

:deep(.el-tag){
  margin-right:-60px;
}
</style>
