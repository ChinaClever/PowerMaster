<template>
  <CommonMenu @check="handleCheck"  @node-click="handleClick" :showSearch="true" :dataList="serverRoomArr" navTitle="插接箱配电">
    <template #NavInfo>
      <div>
        <!-- <div class="header">
          <div class="header_img"><img alt="" src="@/assets/imgs/Box.png" /></div>
        </div> -->
        <!-- <div class="line"></div> -->
        <!-- <div class="status">
          <div class="box">
            <div class="top">
              <div class="tag"></div>{{ statusList[0].name }}
            </div>
            <div class="value"><span class="number">{{statusNumber.lessFifteen}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag empty"></div>小电流
            </div>
            <div class="value"><span class="number">{{statusNumber.smallCurrent}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag warn"></div>{{ statusList[1].name }}
            </div>
            <div class="value"><span class="number">{{statusNumber.greaterFifteen}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag error"></div>{{ statusList[2].name }}
            </div>
            <div class="value"><span class="number">{{statusNumber.greaterThirty}}</span>个</div>
          </div>
        </div> -->
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
              <!--<div class="tag error"></div>-->总共
            </div>
            <div class="value"><span class="number">{{ statusNumber.total }}</span>个</div>
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
      >
        <!--<el-form-item label="参数类型" prop="type">
        <el-cascader
          v-model="defaultSelected"
          collapse-tags
          :options="typeSelection"
          collapse-tags-tooltip
          :show-all-levels="true"
          @change="typeCascaderChange"
          class="!w-130px"
        />
        </el-form-item>-->
        <el-form-item prop="devKey">
          <el-button :class="{ 'btnallSelected': butColor === 0 , 'btnallNotSelected': butColor === 1 }" type = "button" @click="toggleAllStatus">全部</el-button>
          <template v-for="(status, index) in statusList" :key="index">
            <button v-if="butColor === 0" :class="[status.activeClass]" @click.prevent="handleSelectStatus(status.value)">{{status.name}}</button>
            <button v-else-if="butColor === 1" :class="[onclickColor === status.value ? status.activeClass:status.cssClass]" @click.prevent="handleSelectStatus(index)">{{status.name}}</button>
          </template> 
          <span>网络地址：</span>
          <el-autocomplete
            v-model="queryParams.devKey"
            :fetch-suggestions="querySearch"
            clearable
            class="!w-150px"
            placeholder="请输入网络地址"
            @select="handleQuery"
          />
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
        </el-form-item>
        <div style="float:right">
          <el-button @click="valueMode = 4;" :type="valueMode == 4 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />视在功率</el-button>
          <el-button @click="valueMode = 2;" :type="valueMode == 2 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />有功功率</el-button>            
          <el-button @click="valueMode = 3;" :type="valueMode == 3 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />无功功率</el-button> 
          <!-- <el-button v-if= "!shouldShowLabel" @click="valueMode = 0;" :type="valueMode == 0 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />电流</el-button>            
          <el-button v-if= "!shouldShowLabel" @click="valueMode = 1;" :type="valueMode == 1 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />电压</el-button>                                  -->
          <el-button @click="pageSizeArr=[24,36,48,96];queryParams.pageSize = 24;getList();switchValue = 0;" :type="switchValue == 0 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />阵列模式</el-button>
          <el-button @click="pageSizeArr=[15, 25,30, 50, 100];queryParams.pageSize = 15;getList();switchValue = 3;" :type="switchValue == 3 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 4px" />表格模式</el-button>
        </div>
      </el-form>
    </template>
    <template #Content>
      <el-table v-show="switchValue == 3" v-loading="loading" style="height:720px;margin-top:-10px;" :data="list" :stripe="true" :show-overflow-tooltip="true"  @cell-dblclick="toDeatil" :border="true">
        <el-table-column label="编号" align="center" prop="tableId" width="80px"/>
        <!-- 数据库查询 -->
        <el-table-column label="所在位置" align="center" prop="location" />
        <el-table-column label="网络地址" align="center" prop="devKey" :class-name="ip"/>
        <el-table-column v-if="valueMode == 0 && typeText == 'line'" label="A相电流(A)" align="center" prop="acur" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.phaseCur != null">
              {{ scope.row.phaseCur[0] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 0 && typeText == 'line'" label="B相电流(A)" align="center" prop="bcur" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.phaseCur != null">
              {{ scope.row.phaseCur[1] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 0 && typeText == 'line'" label="C相电流(A)" align="center" prop="ccur" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.phaseCur != null" >
              {{ scope.row.phaseCur[2] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column  v-if="valueMode == 1 && typeText == 'line'"  label="A相电压(V)" align="center" prop="avol" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.phaseVol">
              {{ scope.row.phaseVol[0] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 1 && typeText == 'line'" label="B相电压(V)" align="center" prop="bvol" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.phaseVol">
              {{ scope.row.phaseVol[1] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 1 && typeText == 'line'" label="C相电压(V)" align="center" prop="cvol" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.phaseVol">
              {{ scope.row.phaseVol[2] }}
            </el-text>
          </template>
        </el-table-column>
        <!-- <el-table-column v-if="valueMode == 2 && typeText == 'line'" label="A相有功功率(kW)" align="center" prop="aactivePow" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.phaseActivePow">
              {{ scope.row.phaseActivePow[0] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 2 && typeText == 'line'" label="B相有功功率(kW)" align="center" prop="bactivePow" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.phaseActivePow">
              {{ scope.row.phaseActivePow[1] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 2 && typeText == 'line'" label="C相有功功率(kW)" align="center" prop="cactivePow" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.phaseActivePow">
              {{ scope.row.phaseActivePow[2] }}
            </el-text>
          </template>
        </el-table-column> -->
        <!-- <el-table-column v-if="valueMode == 3 && typeText == 'line'" label="A相无功功率(kVar)" align="center" prop="areactivePow" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.phaseReactivePow">
              {{ scope.row.phaseReactivePow[0] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 3 && typeText == 'line'" label="B相无功功率(kVar)" align="center" prop="breactivePow" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.phaseReactivePow">
              {{ scope.row.phaseReactivePow[1] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 3 && typeText == 'line'" label="C相无功功率(kVar)" align="center" prop="creactivePow" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.phaseReactivePow">
              {{ scope.row.phaseReactivePow[2] }}
            </el-text>
          </template>
        </el-table-column>
         -->
                <el-table-column v-if="valueMode == 4 && typeText == 'line'" label="输出位1视在功率(kVA)" align="center" prop="areactivePow" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.outletApparentPow">
              {{ scope.row.outletApparentPow[0] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 4 && typeText == 'line'" label="输出位2视在功率(kVA)" align="center" prop="breactivePow" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.outletApparentPow">
              {{ scope.row.outletApparentPow[1] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 4 && typeText == 'line'" label="输出位3视在功率(kVA)" align="center" prop="creactivePow" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.outletApparentPow">
              {{ scope.row.outletApparentPow[2] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 4 && typeText == 'line'" label="总视在功率(kVA)" align="center" prop="creactivePow" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.powApparent">
              {{ scope.row.powApparent }}
            </el-text>
          </template>
        </el-table-column>
        
        <el-table-column v-if="valueMode == 2" label="输出位1有功功率(kW)" align="center" prop="aactivePow" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.outletActivePow">
              {{ scope.row.outletActivePow[0] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 2" label="输出位2有功功率(kW)" align="center" prop="bactivePow" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.outletActivePow">
              {{ scope.row.outletActivePow[1] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 2" label="输出位3有功功率(kW)" align="center" prop="cactivePow" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.outletActivePow">
              {{ scope.row.outletActivePow[2] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 2 && typeText == 'line'" label="总有功功率(kW)" align="center" prop="cactivePow" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.powActive">
              {{ scope.row.powActive }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 3" label="输出位1无功功率(kVar)" align="center" prop="areactivePow" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.outletReactivePow">
              {{ scope.row.outletReactivePow[0] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 3" label="输出位2无功功率(kVar)" align="center" prop="breactivePow" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.outletReactivePow">
              {{ scope.row.outletReactivePow[1] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 3" label="输出位3无功功率(kVar)" align="center" prop="creactivePow" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.outletReactivePow">
              {{ scope.row.outletReactivePow[2] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 3" label="总无功功率(kVar)" align="center" prop="creactivePow" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.powReactive">
              {{ scope.row.powReactive }}
            </el-text>
          </template>
        </el-table-column>
        <!--<el-table-column v-if="valueMode == 0 && typeText == 'loop'" label="回路1电流(A)" align="center" prop="acur" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopCur != null">
              {{ scope.row.loopCur[0] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 0 && typeText == 'loop'" label="回路2电流(A)" align="center" prop="bcur" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopCur != null">
              {{ scope.row.loopCur[1] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 0 && typeText == 'loop'" label="回路3电流(A)" align="center" prop="ccur" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopCur != null" >
              {{ scope.row.loopCur[2] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 0 && typeText == 'loop'" label="回路4电流(A)" align="center" prop="acur" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopCur != null">
              {{ scope.row.loopCur[3] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 0 && typeText == 'loop'" label="回路5电流(A)" align="center" prop="bcur" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopCur != null">
              {{ scope.row.loopCur[4] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 0 && typeText == 'loop'" label="回路6电流(A)" align="center" prop="ccur" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopCur != null" >
              {{ scope.row.loopCur[5] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 0 && typeText == 'loop'" label="回路7电流(A)" align="center" prop="acur" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopCur != null">
              {{ scope.row.loopCur[6] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 0 && typeText == 'loop'" label="回路8电流(A)" align="center" prop="bcur" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopCur != null">
              {{ scope.row.loopCur[7] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 0 && typeText == 'loop'" label="回路9电流(A)" align="center" prop="ccur" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopCur != null" >
              {{ scope.row.loopCur[8] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column  v-if="valueMode == 1 && typeText == 'loop'"  label="回路1电压(V)" align="center" prop="avol" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopVol">
              {{ scope.row.loopVol[0] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 1 && typeText == 'loop'" label="回路2电压(V)" align="center" prop="bvol" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopVol">
              {{ scope.row.loopVol[1] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 1 && typeText == 'loop'" label="回路3电压(V)" align="center" prop="cvol" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopVol">
              {{ scope.row.loopVol[2] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column  v-if="valueMode == 1 && typeText == 'loop'"  label="回路4电压(V)" align="center" prop="avol" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopVol">
              {{ scope.row.loopVol[3] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 1 && typeText == 'loop'" label="回路5电压(V)" align="center" prop="bvol" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopVol">
              {{ scope.row.loopVol[4] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 1 && typeText == 'loop'" label="回路6电压(V)" align="center" prop="cvol" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopVol">
              {{ scope.row.loopVol[5] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column  v-if="valueMode == 1 && typeText == 'loop'"  label="回路7电压(V)" align="center" prop="avol" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopVol">
              {{ scope.row.loopVol[6] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 1 && typeText == 'loop'" label="回路8电压(V)" align="center" prop="bvol" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopVol">
              {{ scope.row.loopVol[7] }}
            </el-text>
          </template>
        </el-table-column>--
        <el-table-column v-if="valueMode == 1 && typeText == 'loop'" label="回路9电压(V)" align="center" prop="cvol" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopVol">
              {{ scope.row.loopVol[8] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 2 && typeText == 'loop'" label="回路1有功功率(kW)" align="center" prop="aactivePow" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopActivePow">
              {{ scope.row.loopActivePow[0] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 2 && typeText == 'loop'" label="回路2有功功率(kW)" align="center" prop="bactivePow" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopActivePow">
              {{ scope.row.loopActivePow[1] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 2 && typeText == 'loop'" label="回路3有功功率(kW)" align="center" prop="cactivePow" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopActivePow">
              {{ scope.row.loopActivePow[2] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 2 && typeText == 'loop'" label="回路4有功功率(kW)" align="center" prop="aactivePow" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopActivePow">
              {{ scope.row.loopActivePow[3] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 2 && typeText == 'loop'" label="回路5有功功率(kW)" align="center" prop="bactivePow" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopActivePow">
              {{ scope.row.loopActivePow[4] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 2 && typeText == 'loop'" label="回路6有功功率(kW)" align="center" prop="cactivePow" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopActivePow">
              {{ scope.row.loopActivePow[5] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 2 && typeText == 'loop'" label="回路7有功功率(kW)" align="center" prop="aactivePow" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopActivePow">
              {{ scope.row.loopActivePow[6] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 2 && typeText == 'loop'" label="回路8有功功率(kW)" align="center" prop="bactivePow" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopActivePow">
              {{ scope.row.loopActivePow[7] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 2 && typeText == 'loop'" label="回路9有功功率(kW)" align="center" prop="cactivePow" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopActivePow">
              {{ scope.row.loopActivePow[8] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 3 && typeText == 'loop'" label="回路1无功功率(kVar)" align="center" prop="areactivePow" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopReactivePow">
              {{ scope.row.loopReactivePow[0] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 3 && typeText == 'loop'" label="回路2无功功率(kVar)" align="center" prop="breactivePow" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopReactivePow">
              {{ scope.row.loopReactivePow[1] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 3 && typeText == 'loop'" label="回路3无功功率(kVar)" align="center" prop="creactivePow" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopReactivePow">
              {{ scope.row.loopReactivePow[2] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 3 && typeText == 'loop'" label="回路4无功功率(kVar)" align="center" prop="areactivePow" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopReactivePow">
              {{ scope.row.loopReactivePow[3] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 3 && typeText == 'loop'" label="回路5无功功率(kVar)" align="center" prop="breactivePow" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopReactivePow">
              {{ scope.row.loopReactivePow[4] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 3 && typeText == 'loop'" label="回路6无功功率(kVar)" align="center" prop="creactivePow" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopReactivePow">
              {{ scope.row.loopReactivePow[5] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 3 && typeText == 'loop'" label="回路7无功功率(kVar)" align="center" prop="areactivePow" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopReactivePow">
              {{ scope.row.loopReactivePow[6] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 3 && typeText == 'loop'" label="回路8无功功率(kVar)" align="center" prop="breactivePow" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopReactivePow">
              {{ scope.row.loopReactivePow[7] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 3 && typeText == 'loop'" label="回路9无功功率(kVar)" align="center" prop="creactivePow" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopReactivePow">
              {{ scope.row.loopReactivePow[8] }}
            </el-text>
          </template>
        </el-table-column>-->


        <!-- 数据库查询 -->
        <el-table-column label="操作" align="center">
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click="toDeatil(scope.row)"
              v-if="scope.row.status != null && scope.row.status != 5"
              style="background-color:#409EFF;color:#fff;border:none;width:100px;height:30px;"
            >
            设备详情
            </el-button>
            <el-button
              link
              type="danger"
              @click="handleDelete(scope.row.boxId)"
              v-if="scope.row.status == 5"
              style="background-color:#fa3333;color:#fff;border:none;width:60px;height:30px;"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-show="switchValue == 0  && list.length > 0" class="arrayContainer">
        <template v-for="item in list" :key="item.devKey">
          <div v-if="item.id !== null" class="arrayItem">
          <div class="devKey">{{ item.location != null ? item.location : item.devKey }}</div>
          <div class="content">
            <div class="icon" >
              <!-- <div v-if="shouldShow('current', 'phaseCur', 'line') || shouldShow('current', 'loopCur', 'loop')">  
                电流
              </div> -->
              <!-- <div v-if="valueMode == 0 && item.phaseCur != null && typeText == 'line'" >
                电流
              </div>
              <div v-else-if="valueMode == 0 && item.loopCur != null && typeText == 'loop'" >
                电流
              </div> -->
              <div v-if="valueMode == 0 && item.phaseCur != null && typeText == 'line'">
                电流
              </div>
              <!--<div v-else-if="valueMode == 0 && item.loopCur != null && typeText == 'loop'" >
                电流
              </div>-->
              <div v-if="valueMode == 1 && item.phaseVol != null && typeText == 'line'" >
                电压
              </div>
              <!--<div v-else-if="valueMode == 1 && item.loopVol != null && typeText == 'loop'" >
                电压
              </div>-->
              <div v-if="valueMode == 2 && item.outletActivePow != null && typeText == 'line'">
                有功功率
              </div>
              <!--<div v-if="valueMode == 2 && item.loopActivePow != null && typeText == 'loop'">
                有功功率
              </div>
              <div v-if="valueMode == 2 && item.outletActivePow != null && typeText == 'outlet'">
                有功功率
              </div>-->
              <div v-if="valueMode == 3 && item.outletReactivePow != null && typeText == 'line'" >
                无功功率
              </div>
              <div v-if="valueMode == 4 && item.outletApparentPow != null && typeText == 'line'" >
                视在功率
              </div>
             <!--<div v-if="valueMode == 3 && item.outletReactivePow != null && typeText == 'outlet'" >
                无功功率
              </div>-->
            </div>
            <div class="info" v-if="valueMode == 0" >
              <div v-if="item.phaseCur != null && typeText == 'line'">
                <div v-for="(phaseCur,index) in item.phaseCur" :key="index">
                  <el-text  v-if="item.phaseCur != null">
                    {{phaseLineText[index]}}{{phaseCur}}A
                  </el-text>
                </div>
              </div>
              <!--<div v-else-if="item.loopCur != null && typeText == 'loop'">
                <div v-for="(loopCur,index) in item.loopCur" :key="index">
                  <el-text  v-if="item.loopCur != null" :type=" item.loopCurStatus == 0 ? 'danger' : '' " >
                    {{loopLineText[index]}}{{loopCur}}A
                  </el-text>
                </div>
              </div>-->
            </div>
            <div class="info" v-if="valueMode == 1">
              <div v-if="item.phaseVol != null && typeText == 'line'">
                <div v-for="(phaseVol,index) in item.phaseVol" :key="index">
                  <el-text  v-if="item.phaseVol != null">
                    {{phaseLineText[index]}}{{phaseVol}}V
                  </el-text>
                </div>
              </div>
              <!--<div v-else-if="item.loopVol != null && typeText == 'loop'">
                <div v-for="(loopVol,index) in item.loopVol" :key="index">
                  <el-text  v-if="item.loopVol != null" :type=" item.loopVolStatus == 0 ? 'danger' : '' " >
                    {{loopLineText[index]}}{{loopVol}}V
                  </el-text>
                </div>
              </div>-->
            </div>
            <div class="info" v-if="valueMode == 2">
              <!--<div v-if="item.phaseActivePow != null && typeText == 'line'">
                <div v-for="(phaseActivePow,index) in item.phaseActivePow" :key="index">
                  <el-text  v-if="item.phaseActivePow != null">
                    {{phaseLineText[index]}}{{phaseActivePow}}kW
                  </el-text>
                </div>
              </div>
              <div v-else-if="item.loopActivePow != null && typeText == 'loop'">
                <div v-for="(loopActivePow,index) in item.loopActivePow" :key="index">
                  <el-text  v-if="item.loopActivePow != null" :type=" item.loopActivePowStatus == 0 ? 'danger' : '' " >
                    {{loopLineText[index]}}{{loopActivePow}}kW
                  </el-text>
                </div>
              </div>-->
              <div>
                <div v-for="(outletActivePow,index) in item.outletActivePow" :key="index">
                  <el-text  v-if="item.outletActivePow != null">
                    {{outletLineText[index]}}{{outletActivePow}}kW
                  </el-text>
                </div>
              </div>
            </div>
            <div class="info" v-if="valueMode == 3">
              <!--<div  v-if="item.phaseReactivePow != null && typeText == 'line'">
                <div v-for="(phaseReactivePow,index) in item.phaseReactivePow" :key="index">
                  <el-text  v-if="item.phaseReactivePow != null">
                    {{phaseLineText[index]}}{{phaseReactivePow}}kVar
                  </el-text>
                </div>
              </div>
              <div  v-else-if="item.loopReactivePow != null && typeText == 'loop'">
                <div v-for="(loopReactivePow,index) in item.loopReactivePow" :key="index">
                  <el-text  v-if="item.loopReactivePow != null">
                    {{loopLineText[index]}}{{loopReactivePow}}kVar
                  </el-text>
                </div>
              </div>-->
              <div>
                <div v-for="(outletReactivePow,index) in item.outletReactivePow" :key="index">
                  <el-text  v-if="item.outletReactivePow != null">
                    {{outletLineText[index]}}{{outletReactivePow}}kVar
                  </el-text>
                </div>
              </div>
            </div>
            <div class="info" v-if="valueMode == 4">
              <!--<div  v-if="item.phaseReactivePow != null && typeText == 'line'">
                <div v-for="(phaseReactivePow,index) in item.phaseReactivePow" :key="index">
                  <el-text  v-if="item.phaseReactivePow != null">
                    {{phaseLineText[index]}}{{phaseReactivePow}}kVar
                  </el-text>
                </div>
              </div>
              <div  v-else-if="item.loopReactivePow != null && typeText == 'loop'">
                <div v-for="(loopReactivePow,index) in item.loopReactivePow" :key="index">
                  <el-text  v-if="item.loopReactivePow != null">
                    {{loopLineText[index]}}{{loopReactivePow}}kVar
                  </el-text>
                </div>
              </div>-->
              <div>
                <div v-for="(outletApparentPow,index) in item.outletApparentPow" :key="index">
                  <el-text  v-if="item.outletApparentPow != null">
                    {{outletLineText[index]}}{{outletApparentPow}}kVar
                  </el-text>
                </div>
              </div>
            </div>
          </div>
          <!-- <div class="room">{{item.jf}}-{{item.mc}}</div> -->
          <div class="status">
            <el-tag type="info" v-if="item.loopCurStatus == null " >离线</el-tag>
            <el-tag type="danger" v-else-if="item.loopCurStatus[0] != 0 || item.loopCurStatus[1] != 0  || item.loopCurStatus[2] != 0 " >告警</el-tag>
            <el-tag type="success" v-else >正常</el-tag>
          </div>
          <!--<div class="status" v-if="valueMode == 1">
            <el-tag type="info" v-if="item.loopVolStatus == null " >离线</el-tag>
            <el-tag type="danger" v-else-if="item.loopVolStatus[0] != 0 || item.loopVolStatus[1] != 0 || item.loopVolStatus[2] != 0 " >告警</el-tag>
            <el-tag type="success" v-else >正常</el-tag>
          </div>
          <div class="status" v-if="valueMode == 2">
            <el-tag type="info" v-if="item.loopActivePowStatus == null " >离线</el-tag>
            <el-tag type="danger" v-else-if="item.loopActivePowStatus[0] != 0 || item.loopActivePowStatus[1] != 0 || item.loopActivePowStatus[2] != 0" >告警</el-tag>
            <el-tag type="success" v-else >正常</el-tag>
          </div>
          <div class="status" v-if="valueMode == 3">
            <el-tag type="info" v-if="item.status == null ||  item.status == 5" >离线</el-tag>
          </div>-->
          <button class="detail" @click="toDeatil(item)" v-if="item.status != null && item.status != 5" >详情</button>
        </div>
        </template>
      </div>
      <Pagination
        :total="total"
        :page-size-arr="pageSizeArr"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
      <template v-if="list.length == 0 && switchValue != 3">
        <el-empty description="暂无数据" :image-size="300" />
      </template>
    </template>
  </CommonMenu>


  <!-- 表单弹窗：添加/修改 -->
  <!-- <CurbalanceColorForm ref="curBalanceColorForm" @success="getList" /> -->
</template>

<script setup lang="ts">
// import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download';
import { IndexApi } from '@/api/bus/boxindex';
// import CurbalanceColorForm from './CurbalanceColorForm.vue'
import { ElTree } from 'element-plus';

/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' });

const { push } = useRouter();

const curBalanceColorForm = ref();
const flashListTimer = ref();
const firstTimerCreate = ref(true);
const pageSizeArr = ref([24,36,48,96]);
const switchValue = ref(0);
const valueMode = ref(2);

const phaseLineText = ref(['A相：','B相：','C相：']);
const loopLineText = ref(['回路3：','回路6：','回路9：']);
const outletLineText = ref(['输出位1：','输出位2：','输出位3：']);

const phaseText = ref([['A相电流(A)','B相电流(A)','C相电流(A)'],['A相电压(V)','B相电压(V)','C相电压(V)'],
['A相有功功率(kW)','B相有功功率(kW)','C相有功功率(kW)'],['A相无功功率(kVar)','B相无功功率(kVar)','C相无功功率(kVar)']]);

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

const devKeyList = ref([]);
const statusNumber = reactive({
  normal : 0,
  alarm : 0,
  offline : 0,
  total : 0
});
const loadAll = async () => {
  var data = await IndexApi.devKeyList();
  var objectArray = data.map((str) => {
    return { value: str };
  });
  return objectArray;
};

const querySearch = (queryString: string, cb: any) => {

  const results = queryString
    ? devKeyList.value.filter(createFilter(queryString))
    : devKeyList.value
  // call callback function to return suggestions
  cb(results)
};

const createFilter = (queryString: string) => {
  return (devKeyList) => {
    return (
      devKeyList.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0
    )
  }
}

const handleClick = (row) => {
  console.log("click",row)
};

const handleCheck = async (row) => {
  if(row.length == 0){
    queryParams.boxDevKeyList = null;
    getList();
    return;
  }
  const ids = [] as any
  var haveCabinet = false;
  row.forEach(item => {
    if (item.type == 7) {
      ids.push(item.unique)
      haveCabinet = true;
    }
  })
  if(!haveCabinet ){
    queryParams.boxDevKeyList = [-1]
  }else{
    queryParams.boxDevKeyList = ids
  }

  getList();
};

const serverRoomArr =  ref([]);

const filterText = ref('');
const treeRef = ref<InstanceType<typeof ElTree>>();

watch(filterText, (val) => {
  treeRef.value!.filter(val)
});


const message = useMessage(); // 消息弹窗
const { t } = useI18n(); // 国际化

const loading = ref(false); // 列表的加载中
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
]) as any;// 列表的数据
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
]) as any;// 列表的数据

const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 24,
  devKey: undefined,
  createTime: [],
  cascadeNum: undefined,
  serverRoomData:undefined,
  status:[],
  cabinetIds : [],
})as any;
const queryParamsAll = reactive({
  pageNo: 1,
  pageSize: -1,
  devKey: undefined,
  createTime: [],
  cascadeNum: undefined,
  serverRoomData:undefined,
  status:[],
  cabinetIds : [],
})as any;
const queryFormRef = ref(); // 搜索的表单
const exportLoading = ref(false); // 导出的加载中

/** 查询列表 */
const getList = async () => {
  loading.value = true;
  try {
    const data = await IndexApi.getBoxRedisPage(queryParams);

    list.value = data.list;
    var tableIndex = 0;

    list.value.forEach((obj) => {
      obj.tableId = (queryParams.pageNo - 1) * queryParams.pageSize + ++tableIndex;
      if(obj?.phaseCur == null){
        return;
      }
      for(var i= 0;i < obj.phaseCur.length; i++)
      {
        obj.phaseCur[i] = obj.phaseCur[i]?.toFixed(2);
        obj.phaseVol[i] = obj.phaseVol[i]?.toFixed(2);
        obj.phaseActivePow[i] = obj.phaseActivePow[i]?.toFixed(3);
        obj.phaseReactivePow[i] = obj.phaseReactivePow[i]?.toFixed(3);
        obj.phaseApparentPow[i] = obj.phaseApparentPow[i]?.toFixed(3);
        obj.phasePowFactor[i] = obj.phasePowFactor[i]?.toFixed(2);
      }
      
      for(var i= 0;i < obj.loopCur.length; i++)
      {
        obj.loopCur[i] = obj.loopCur[i]?.toFixed(2);
        obj.loopVol[i] = obj.loopVol[i]?.toFixed(1);
        obj.loopActivePow[i] = obj.loopActivePow[i]?.toFixed(3);
        obj.loopReactivePow[i] = obj.loopReactivePow[i]?.toFixed(3);
      } 
      
      for(var i= 0;i < obj.outletActivePow.length; i++)
      {
        obj.outletActivePow[i] = obj.outletActivePow[i]?.toFixed(3);
        obj.outletReactivePow[i] = obj.outletReactivePow[i]?.toFixed(3);
        obj.outletApparentPow[i] = obj.outletApparentPow[i]?.toFixed(3);
        obj.outletPowFactor[i] = obj.outletPowFactor[i]?.toFixed(2);
      }
    });

    for(let i = 0; i < list.value.length; i++){
      const loopCur = list.value[i].loopCur;
      const selectedElements = [];
      const indicesToSelect = [2, 5, 8];
      for (let j = 0; j < indicesToSelect.length; j++) {
        if (j < loopCur.length){
          selectedElements.push(loopCur[indicesToSelect[j]]);
        }
      }

      list.value[i].loopCur = selectedElements;
    }

    total.value = data.total;
  } finally {
    loading.value = false;
  }
};

const getListAll = async () => {
  try {
    var normal = 0;
    var offline = 0;
    var alarm = 0;

    const allData = await IndexApi.getBoxRedisPage(queryParamsAll);
    allList.value = allData.list
    allList.value.forEach((objAll) => {
      if(objAll?.dataUpdateTime == null && objAll?.phaseCur == null){
        objAll.status = 0;
        offline++;
        return;
      }
      if(objAll?.status == 1){
        normal++;
      } else if (objAll?.status == 2){
        alarm++;
      }
    });
    //设置左边数量
    statusNumber.normal = normal;
    statusNumber.offline = offline;
    statusNumber.alarm = alarm;
    statusNumber.total = allData.total;
  } catch (error) {
    
  }
}

const getListNoLoading = async () => {
  try {
    const data = await IndexApi.getBoxRedisPage(queryParams);
    list.value = data.list;
    var tableIndex = 0;

    list.value.forEach((obj) => {
      obj.tableId = (queryParams.pageNo - 1) * queryParams.pageSize + ++tableIndex;
      if(obj?.phaseCur == null){
        return;
      }
      for(var i= 0;i < obj.phaseCur.length; i++)
      {
        obj.phaseCur[i] = obj.phaseCur[i]?.toFixed(2);
        obj.phaseVol[i] = obj.phaseVol[i]?.toFixed(2);
        obj.phaseActivePow[i] = obj.phaseActivePow[i]?.toFixed(3);
        obj.phaseReactivePow[i] = obj.phaseReactivePow[i]?.toFixed(3);
        obj.phaseApparentPow[i] = obj.phaseApparentPow[i]?.toFixed(3);
        obj.phasePowFactor[i] = obj.phasePowFactor[i]?.toFixed(2);
      }
      
      for(var i= 0;i < obj.loopCur.length; i++)
      {
        obj.loopCur[i] = obj.loopCur[i]?.toFixed(2);
        obj.loopVol[i] = obj.loopVol[i]?.toFixed(1);
        obj.loopActivePow[i] = obj.loopActivePow[i]?.toFixed(3);
        obj.loopReactivePow[i] = obj.loopReactivePow[i]?.toFixed(3);
      } 
      
      for(var i= 0;i < obj.outletActivePow.length; i++)
      {
        obj.outletActivePow[i] = obj.outletActivePow[i]?.toFixed(3);
        obj.outletReactivePow[i] = obj.outletReactivePow[i]?.toFixed(3);
        obj.outletApparentPow[i] = obj.outletApparentPow[i]?.toFixed(3);
        obj.outletPowFactor[i] = obj.outletPowFactor[i]?.toFixed(2);
      }
    });

    for(let i = 0; i < list.value.length; i++){
      const loopCur = list.value[i].loopCur;
      const selectedElements = [];
      const indicesToSelect = [2, 5, 8];
      for (let j = 0; j < indicesToSelect.length; j++) {
        if (j < loopCur.length){
          selectedElements.push(loopCur[indicesToSelect[j]]);
        }
      }

      list.value[i].loopCur = selectedElements;
    }

    total.value = data.total;
  } catch (error) {
    
  }
};

const getNavList = async() => {
  const res = await IndexApi.getBoxMenu();
  serverRoomArr.value = res;
  if (res && res.length > 0) {
    const room = res[0];
    const keys = [] as string[];
    room.children.forEach(child => {
      if(child.children.length > 0) {
        child.children.forEach(son => {
          keys.push(son.id + '-' + son.type);
        })
      }
    })
  }
};

const toDeatil = (row) =>{
  const devKey = row.devKey;
  const boxId = row.boxId;
  const location = row.location != null ? row.location : row.devKey;
  push({path: '/bus/boxmonitor/boxpowerdetail', state: { devKey, boxId ,location}});
};

const defaultSelected = ref(['line']);
const typeSelection = ref([]) as any;
const shouldShowLabel = ref(false);
var typeText = ref('line');
// 参数类型改变触发
//const typeCascaderChange = (selected) => {
//  queryParams.type = selected[0];
//  typeText = selected[0];
//  if(selected[0] == 'outlet'){
//    shouldShowLabel.value = true;
//    if(valueMode.value == 0 || valueMode.value == 1){
//      valueMode.value =2;
//    }
//  }else{
//    shouldShowLabel.value = false;
//  }
//  // 自动搜索
//  handleQuery();
//}

// 参数类型选择框初始化，相固定3相
//const getTypeMaxValue = async () => {
//    const typeSelectionValue  = [
//    {
//      value: "line",
//      label: '相'
//    },
//    {
//    value: "loop",
//    label: '回路'
//    },
//    {
//    value: "outlet",
//    label: '输出位'
//    },
//  ]
//  typeSelection.value = typeSelectionValue;
//  shouldShowLabel.value = false;
//}


// const openNewPage = (scope) => {
//   const url = 'http://' + scope.row.devKey.split('-')[0] + '/index.html';
//   window.open(url, '_blank');
// }


/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1;
  getList();
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

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields();
  butColor.value = 0;
  queryParams.status = [];
  onclickColor.value = -1;
  handleQuery();
}

/** 添加/修改操作 */

const openForm = (type: string) => {
  curBalanceColorForm.value.open(type);
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm();
    // 发起删除
    await IndexApi.deleteIndex(id);
    message.success(t('common.delSuccess'));
    // 刷新列表
    // await getList()
  } catch {}
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm();
    // 发起导出
    exportLoading.value = true;
    const data = await IndexApi.exportIndex(queryParams);
    download.excel(data, 'PDU设备.xls');
  } catch {
  } finally {
    exportLoading.value = false;
  }
}

/** 初始化 **/
onMounted(async () => {
  devKeyList.value = await loadAll();
  getList();
  getNavList();
  getListAll();
  //getTypeMaxValue();
  flashListTimer.value = setInterval((getListNoLoading), 5000);
  flashListTimer.value = setInterval((getListAll), 5000);
})

onBeforeUnmount(()=>{
  if(flashListTimer.value){
    clearInterval(flashListTimer.value);
    flashListTimer.value = null;
  }
})

onBeforeRouteLeave(()=>{
  if(flashListTimer.value){
    clearInterval(flashListTimer.value);
    flashListTimer.value = null;
    firstTimerCreate.value = false;
  }
})

onActivated(() => {
  getList();
  getNavList();
  if(!firstTimerCreate.value){
    flashListTimer.value = setInterval((getListNoLoading), 5000);
    flashListTimer.value = setInterval((getListAll), 5000);
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
        flex-direction: row;
        align-items: self-end;
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
    margin-top: 20px;
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
          width: 100px;
          height: 50px;
          margin-left:20px;
          margin-right:20px;
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
          width: 100px;
          height: 50px;
          margin-left:20px;
          margin-right:20px;
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
          width: 100px;
          height: 50px;
          margin-left:20px;
          margin-right:20px;
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

::v-deep(.el-table .el-table__header th){
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
