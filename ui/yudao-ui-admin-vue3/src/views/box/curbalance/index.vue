<template>
  <CommonMenu @check="handleCheck"  @node-click="handleClick" :showSearch="true" :dataList="serverRoomArr" navTitle="插接箱平衡">
    <template #NavInfo>
      <div>
        <!-- <div class="header">
          <div class="header_img"><img alt="" src="@/assets/imgs/Box.png" /></div>
        </div> -->
        <!-- <div class="line"></div> -->
        <div class="status">
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
        <el-form-item v-if="switchValue == 0 ">
          <button :class="{ 'btnallSelected': butColor === 0 , 'btnallNotSelected': butColor === 1 }" type = "button" @click="toggleAllStatus">
            全部
          </button>
          <template v-for="(status, index) in statusList" :key="index">
            <button v-if="butColor === 0" :class="[status.activeClass]" @click.prevent="handleSelectStatus(status.value)">{{status.name}}</button>
            <button v-else-if="butColor === 1" :class="[onclickColor === status.value ? status.activeClass:status.cssClass]" @click.prevent="handleSelectStatus(status.value)">{{status.name}}</button>
          </template>
        </el-form-item>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-if="switchValue == 0 "
        >
          <Icon icon="ep:plus" class="mr-5px" /> 平衡度范围颜色
        </el-button>
        <el-form-item label="网络地址" prop="devKey">
          <el-autocomplete
            v-model="queryParams.devKey"
            :fetch-suggestions="querySearch"
            clearable
            class="!w-200px"
            placeholder="请输入网络地址"
            @select="handleQuery"
          />
        <el-form-item style="margin-left: 10px">
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
          <el-button @click="pageSizeArr=[24,36,48,96];queryParams.pageSize = 24;switchValue = 0;" :type="switchValue == 0 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />电流</el-button>            
          <el-button @click="statusList.forEach((item) => item.selected = true);pageSizeArr=[24,36,48];queryParams.pageSize = 24;switchValue = 1;" :type="switchValue == 1 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />电压</el-button>
          <el-button @click="pageSizeArr=[24,36,48,96];queryParams.pageSize = 24;visMode = 0" :type="visMode == 0 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 4px" />阵列模式</el-button>
          <el-button @click="pageSizeArr=[15, 25,30, 50, 100];queryParams.pageSize = 15;visMode = 1;" :type="visMode == 1 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 4px" />表格模式</el-button>
        </div>
      </el-form>
    </template>
    <template #Content>
      <div v-if="visMode !== 0 && list.length > 0">
        <el-table v-if="visMode == 1" v-loading="loading" style="margin-top:-10px;height:720px;oveflow-y:auto;" :data="list" :show-overflow-tooltip="true"  @cell-dblclick="toDeatil" :border="true">
        <el-table-column label="编号" align="center" prop="tableId" width="80px"/>
        <!-- 数据库查询 -->
        <el-table-column label="所在位置" align="center" prop="location"  width="300px"/>
        <el-table-column label="网络地址" align="center" prop="devKey" :class-name="ip"/>
        <el-table-column label="运行状态" align="center" prop="color" v-if="switchValue == 0">
          <template #default="scope" >
              <el-tag type="info"  v-if="scope.row.color == 1">{{statusList[3].name}}</el-tag>
              <el-tag type="success"  v-else-if="scope.row.color == 2">{{statusList[0].name}}</el-tag>
              <el-tag type="warning" v-else-if="scope.row.color == 3">{{statusList[1].name}}</el-tag>
              <el-tag type="danger" v-else-if="scope.row.color == 4">{{statusList[2].name}}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="不平衡度(%)" align="center" prop="curUnbalance" width="130px" v-if="switchValue == 0">
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.curUnbalance != null" >
              {{ scope.row.curUnbalance }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="A相(A)" align="center" prop="acur" width="130px" v-if="switchValue == 0" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.acur != null">
              {{ scope.row.acur }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="B相(A)" align="center" prop="bcur" width="130px" v-if="switchValue == 0">
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.bcur != null">
              {{ scope.row.bcur }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="C相(A)" align="center" prop="ccur" width="130px" v-if="switchValue == 0">
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.ccur != null">
              {{ scope.row.ccur }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="不平衡度(%)" align="center" prop="volUnbalance" width="130px" v-if="switchValue == 1">
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.volUnbalance != null" >
              {{ scope.row.volUnbalance }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="A相(V)" align="center" prop="avol" width="130px" v-if="switchValue == 1">
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.avol">
              {{ scope.row.avol }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="B相(V)" align="center" prop="bvol" width="130px" v-if="switchValue == 1">
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.bvol">
              {{ scope.row.bvol }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="C相(V)" align="center" prop="cvol" width="130px" v-if="switchValue == 1">
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.cvol">
              {{ scope.row.cvol }}
            </el-text>
          </template>
        </el-table-column>
        <!-- 数据库查询 -->
        <el-table-column label="操作" align="center">
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click="showDialogCur(scope.row)"
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
      </div>

      <div v-if="visMode == 0 && switchValue == 0  && list.length > 0" class="arrayContainer">
        <div class="arrayItem" v-for="item in list" :key="item.devKey">
          <div class="devKey">{{ item.location != null ? item.location : item.devKey }}</div>
          <div class="content">
            <div class="info" style="margin-left:10px;">                  
              <div v-if="item.acur != null">A相：{{item.acur}}A</div>
              <div v-if="item.bcur != null">B相：{{item.bcur}}A</div>
              <div v-if="item.ccur != null">C相：{{item.ccur}}A</div>
              <!-- <div >网络地址：{{ item.devKey }}</div> -->
              <!-- <div>AB路占比：{{item.fzb}}</div> -->
            </div>
            <div class="icon" >
              <div v-if="item.curUnbalance != null" >
                <span style="font-size: 20px;">{{ item.curUnbalance }}%</span><br/>电流不平衡度
              </div>
            </div>
          </div>
          <!-- <div class="room">{{item.jf}}-{{item.mc}}</div> -->
          <div class="status" >
            <el-tag type="info"  v-if="item.color == 0">单相设备</el-tag>
            <el-tag type="info"  v-if="item.color == 1 && item.status != 0">{{statusList[3].name}}</el-tag>
            <el-tag type="success"  v-if="item.color == 2 && item.status != 0">{{ statusList[0].name }}</el-tag>
            <el-tag type="warning" v-if="item.color == 3 && item.status != 0">{{ statusList[1].name }}</el-tag>
            <el-tag type="danger" v-if="item.color == 4 && item.status != 0">{{ statusList[2].name }}</el-tag>
          </div>
          <button class="detail" @click="showDialogCur(item)" v-if="item.color != null && item.color != 0">详情</button>
        </div>
      </div>

      <el-dialog v-model="dialogVisibleCur" @close="handleClose">
        <!-- 自定义的头部内容（可选） -->
        <template #header>
          <div>
            <span style="font-weight:bold;font-size:20px;margin-right:10px">电流不平衡</span>
            <span style="margin-right:10px">机房：{{ curRoomName? curRoomName : '未绑定' }}</span>
            <span>母线：{{ busName }}&nbsp;&nbsp;</span>
            <span>插接箱：{{ boxName }}&nbsp;&nbsp;</span>
            <span>网络地址：{{ curdevkey }}</span>
          </div>
        </template>
        <!-- 自定义的主要内容 -->
        <div class="custom-content">
          <div class="custom-content-container">
            <el-card class="cardChilc" shadow="hover">
            <curUnblance :max="balanceObj.imbalanceValueA.toFixed(2)" :customColor="colorList[balanceObj.colorIndex].color" :name="colorList[colorFlag].name"/>
            </el-card>
            <el-card class="cardChilc" style="margin: 0 10px" shadow="hover">
              <div class="IechartBar" style="position: relative;">
                <div style="display: inline-block;
                width: 70%;
                height: 100%;">
                  <Echart :options="ABarOption" :height="300" />
                </div>
                <div style="display: inline-block;
                    position: absolute;
                    width: 150px;
                    height: 100px;
                    top: 30%;">
                  <div>
                    <span class="bullet" style="color:#E5B849;">•</span><span style="width:50px;font-size:14px;">Ia：</span><span style="font-size:16px;">{{cur_valueACopy[0]}}A</span>
                  </div>
                  <div style="margin-top:10px;">
                    <span class="bullet" style="color:#C8603A;">•</span><span style="width:50px;font-size:14px;">Ib：</span><span style="font-size:16px;">{{cur_valueACopy[1]}}A</span>
                  </div>
                  <div style="margin-top:10px;">
                    <span class="bullet" style="color:#AD3762;">•</span><span style="width:50px;font-size:14px;">Ic：</span><span style="font-size:16px;">{{cur_valueACopy[2]}}A</span>
                  </div>
                </div>
                <!--<Echart :options="ABarOption" :height="300" />-->
              </div>
            </el-card>
            <el-card class="cardChilc" shadow="hover">
              <div class="IechartBar">
                <Echart :options="ALineOption" :height="300" />
              </div>
            </el-card>
          </div>
          <div class="custom-content-container">
            <el-card  class="cardChilc" shadow="hover">
              <volUnblance :max="balanceObj.imbalanceValueB.toFixed(2)" :customColor=" colorVolList[balanceObj.colorIndex].color" :name="colorVolList[colorFlag].name" />
            </el-card>
            <el-card class="cardChilc" style="margin: 0 10px" shadow="hover">
              <div class="IechartBar" style="position: relative;">
                <div style="display: inline-block;
                width: 70%;
                height: 100%;">
                  <Echart :options="BBarOption" :height="300"/>
                </div>
                <div style="display: inline-block;
                    position: absolute;
                    width: 150px;
                    height: 100px;
                    top: 30%;">
                  <div>
                    <span class="bullet" style="color:#E5B849;">•</span><span style="width:50px;font-size:14px;">Ua：</span><span style="font-size:16px;">{{vol_valueACopy[0]}}V</span>
                  </div>
                  <div style="margin-top:10px;">
                    <span class="bullet" style="color:#C8603A;">•</span><span style="width:50px;font-size:14px;">Ub：</span><span style="font-size:16px;">{{vol_valueACopy[1]}}V</span>
                  </div>
                  <div style="margin-top:10px;">
                    <span class="bullet" style="color:#AD3762;">•</span><span style="width:50px;font-size:14px;">Uc：</span><span style="font-size:16px;">{{vol_valueACopy[2]}}V</span>
                  </div>
                </div>
              </div>
            </el-card>
            <el-card class="cardChilc" shadow="hover">
              <div class="IechartBar">
                <Echart :options="BLineOption" :height="300"/>
              </div>
            </el-card>
          </div>
        </div>
      </el-dialog>

      <div v-if="visMode == 0 && switchValue == 1  && list.length > 0" class="arrayContainer">
        <div class="arrayItem" v-for="item in list" :key="item.devKey">
          <div class="devKey">{{ item.location != null ? item.location : item.devKey }}</div>
          <div class="content">
            <div class="info" style="margin-left:10px;">                  
              <div v-if="item.avol != null">A相：{{item.avol}}V</div>
              <div v-if="item.bvol != null">B相：{{item.bvol}}V</div>
              <div v-if="item.cvol != null">C相：{{item.cvol}}V</div>
              <!-- <div >网络地址：{{ item.devKey }}</div> -->
              <!-- <div>AB路占比：{{item.fzb}}</div> -->
            </div>
            <div class="icon" >
              <div v-if="item.volUnbalance != null" >
                <span style="font-size: 20px;">{{ item.volUnbalance }}%</span><br/>电压不平衡度
              </div>              
            </div>
          </div>
          <!-- <div class="room">{{item.jf}}-{{item.mc}}</div> -->
          <div class="status" >
            <el-tag type="info"  v-if="item.color === 0">单相设备</el-tag>
            <el-tag type="info" v-else >电压不平衡</el-tag>
          </div>
          <button class="detail" @click="showDialogVol(item)" v-if="item.color != null && item.color != 0">详情</button>
        </div>
      </div>

      <el-dialog v-model="dialogVisibleVol" @close="handleClose">
        <!-- 自定义的头部内容（可选） -->
        <template #header>
          <div>
            <span style="font-weight:bold;font-size:20px;margin-right:10px">电压不平衡</span>
            <span style="margin-right:10px">机房：{{ volRoomName? volRoomName : '未绑定' }}</span>
            <span>母线：{{ busName }}&nbsp;&nbsp;</span>
            <span>插接箱：{{ boxName }}&nbsp;&nbsp;</span>
            <span>网络地址：{{ voldevkey }}</span>
          </div>
        </template>
        <!-- 自定义的主要内容 -->
        <div class="custom-content">
          <div class="custom-content-container">
            <el-card class="cardChilc" shadow="hover">
              <curUnblance :max="balanceObj.imbalanceValueA.toFixed(2)" :customColor="colorList[balanceObj.colorIndex].color" :name="colorList[colorFlag].name" />
            </el-card>
            <el-card class="cardChilc" style="margin: 0 10px" shadow="hover">
              <div class="IechartBar" style="position: relative;">
                <div style="display: inline-block;
                width: 70%;
                height: 100%;">
                  <Echart :options="ABarOption" :height="300" />
                </div>
                <div style="display: inline-block;
                    position: absolute;
                    width: 150px;
                    height: 100px;
                    top: 30%;">
                  <div>
                    <span class="bullet" style="color:#E5B849;">•</span><span style="width:50px;font-size:14px;">Ia：</span><span style="font-size:16px;">{{cur_valueACopy[0]}}A</span>
                  </div>
                  <div style="margin-top:10px;">
                    <span class="bullet" style="color:#C8603A;">•</span><span style="width:50px;font-size:14px;">Ib：</span><span style="font-size:16px;">{{cur_valueACopy[1]}}A</span>
                  </div>
                  <div style="margin-top:10px;">
                    <span class="bullet" style="color:#AD3762;">•</span><span style="width:50px;font-size:14px;">Ic：</span><span style="font-size:16px;">{{cur_valueACopy[2]}}A</span>
                  </div>
                </div>
                <!--<Echart :options="ABarOption" :height="300" />-->
              </div>
            </el-card>
            <el-card class="cardChilc" shadow="hover">
              <div class="IechartBar">
                <Echart :options="ALineOption" :height="300" />
              </div>
            </el-card>
          </div>
          <div class="custom-content-container">
            <el-card class="cardChilc" shadow="hover">
              <volUnblance :max="balanceObj.imbalanceValueB.toFixed(2)" :customColor=" colorVolList[balanceObj.colorIndex].color" :name="colorVolList[colorFlag].name" />
            </el-card>
            <el-card class="cardChilc" style="margin: 0 10px" shadow="hover">
              <div class="IechartBar" style="position: relative;">
                <div style="display: inline-block;
                width: 70%;
                height: 100%;">
                  <Echart :options="BBarOption" :height="300"/>
                </div>
                <div style="display: inline-block;
                    position: absolute;
                    width: 150px;
                    height: 100px;
                    top: 30%;">
                  <div>
                    <span class="bullet" style="color:#E5B849;">•</span><span style="width:50px;font-size:14px;">Ua：</span><span style="font-size:16px;">{{vol_valueACopy[0]}}V</span>
                  </div>
                  <div style="margin-top:10px;">
                    <span class="bullet" style="color:#C8603A;">•</span><span style="width:50px;font-size:14px;">Ub：</span><span style="font-size:16px;">{{vol_valueACopy[1]}}V</span>
                  </div>
                  <div style="margin-top:10px;">
                    <span class="bullet" style="color:#AD3762;">•</span><span style="width:50px;font-size:14px;">Uc：</span><span style="font-size:16px;">{{vol_valueACopy[2]}}V</span>
                  </div>
                </div>
              </div>
            </el-card>
            <el-card class="cardChilc" shadow="hover">
              <div class="IechartBar">
                <Echart :options="BLineOption" :height="300"/>
              </div>
            </el-card>
          </div>
        </div>
      </el-dialog>

      <Pagination
        :total="total"
        :page-size-arr="pageSizeArr"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
      <template v-if="list.length == 0 && switchValue !== null">
        <el-empty description="暂无数据" :image-size="595" />
      </template>
    </template>
  </CommonMenu>

  <!-- 表单弹窗：添加/修改 -->
  <CurbalanceColorForm ref="curBalanceColorForm" @success="handleSuccess" />
</template>

<script setup lang="ts">
// import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download';
import { IndexApi } from '@/api/bus/boxindex';
import CurbalanceColorForm from './CurbalanceColorForm.vue';
import { ElTree } from 'element-plus';
import { EChartsOption } from 'echarts';
import { BoxCurbalanceColorApi } from '@/api/bus/boxcurbalancecolor';
import curUnblance from './component/curUnblance.vue';
import volUnblance from './component/volUnblance.vue';

/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' });

const curdevkey = ref() as any;
const voldevkey = ref() as any;
const dialogVisibleCur = ref(false);
const dialogVisibleVol = ref(false);
const { push } = useRouter();
const visMode = ref(0);
const curBalanceColorForm = ref();
const flashListTimer = ref();
const firstTimerCreate = ref(true);
const pageSizeArr = ref([24,36,48,96]);
const switchValue = ref(0);
const statusNumber = reactive({
  lessFifteen : 0,
  greaterFifteen : 0,
  greaterThirty : 0,
  smallCurrent : 0
})

const location = ref();
const curRoomName = ref();
const volRoomName = ref();
const boxName = ref();
const busName = ref();

const butColor = ref(0);
const onclickColor = ref(-1);

const statusList = reactive([
  {
    name: '<15%',
    selected: true,
    value: 2,
    cssClass: 'btn_normal',
    activeClass: 'btn_normal normal'
  },
  {
    name: '15%-30%',
    selected: true,
    value: 3,
    cssClass: 'btn_warn',
    activeClass: 'btn_warn warn'
  },
  {
    name: '>30%',
    selected: true,
    value: 4,
    cssClass: 'btn_error',
    activeClass: 'btn_error error'
  },
  {
    name: '小电流不平衡',
    selected: true,
    value: 1,
    cssClass: 'btn_offline',
    activeClass: 'btn_offline offline'
  },
])

const colorList = [
  {
    name: '电流不平衡',
    color: '#aaa'
  },
  {
    name: '电流不平衡',
    color: '#3bbb00'
  },
  {
    name: '电流不平衡',
    color: '#ffc402'
  },
  {
    name: '电流不平衡',
    color: '#fa3333'
  }
]

const colorVolList = [{
  name: '电压不平衡',
  color: '#aaa',  //灰色
},{
  name: '电压不平衡',
  color: '#3bbb00', //绿色
},{
  name: '电压不平衡',
  color: '#ffc402', //黄色
},{
  name: '大电压不平衡',
  color: '#075F71',
}]

const devKeyList = ref([])
const loadAll = async () => {
  var data = await IndexApi.devKeyList();
  var objectArray = data.map((str) => {
    return { value: str };
  });
  return objectArray;
}

const querySearch = async (queryString: string, cb: any) => {
  if(queryString.length>7){
    var results = await IndexApi.boxFindKeys({key:queryString});
    let arr: any[] = [];
    results.map(item => {
      arr.push({value:item})
    });
    cb(arr)
  }else{
      const results = queryString
    ? devKeyList.value.filter(createFilter(queryString))
    : devKeyList.value
  cb(results)
  }
}

const createFilter = (queryString: string) => {
  return (devKeyList) => {
    return (
      devKeyList.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0
    )
  }
}

const handleClick = (row) => {
}

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
})as any
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中

const balanceObj = reactive({
  pow_apparent_percent: 0,
  pow_active_percent: 0,
  cur_valueA: [],
  vol_value: [],
  imbalanceValueA: 0,
  imbalanceValueB: 0,
  colorIndex: 0
})

const ABarOption = ref<EChartsOption>({})
const BBarOption = ref<EChartsOption>({})

const ALineOption = ref<EChartsOption>({
  title: {
    text: '电流趋势',
    left: 'left'
  },
  tooltip: {
    trigger: 'axis',
    formatter: function (params) {
      let tooltipContent = `记录时间: ${params[0].name}<br/>`;
      // 遍历params数组，构建电压信息
      const phases = ['A相电流', 'B相电流', 'C相电流'];
      params.forEach((item, index) => {
        if (index < phases.length && item.seriesName) {
          tooltipContent += `${phases[index]}: ${item.value} A<br/>`;
        }
      });
      
      return tooltipContent;
    }
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  yAxis: {
    type: 'value',
    name: '电流',
    axisLabel: {
      formatter: '{value} A'
    }
  },
  xAxis: {},
  series: []
})

const BLineOption = ref<EChartsOption>({
  title: {
    text: '电压趋势',
    left: 'left'
  },
  tooltip: {
    trigger: 'axis',
    formatter: function (params) {
      let tooltipContent = `记录时间: ${params[0].name}<br/>`; // 显示记录时间
      
      // 遍历params数组，构建电压信息
      const phases = ['A相电压', 'B相电压', 'C相电压'];
      params.forEach((item, index) => {
        if (index < phases.length && item.seriesName) {
          tooltipContent += `${phases[index]}: ${item.value} V<br/>`;
        }
      });
      
      return tooltipContent;
    }
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  yAxis: {
    type: 'value',
    name: '电压',
    axisLabel: {
      formatter: '{value} V'
    }
  },
  xAxis:{},
  series: []
})

const getCurBalance = async () => {
    var range = await BoxCurbalanceColorApi.getBoxCurbalanceColor();
    if(range != null){
      statusList[0].name = '<' + range.rangeOne + '%';
      statusList[1].name = range.rangeTwo + '%-' +  range.rangeThree + "%";
      statusList[2].name = '>' + range.rangeFour + '%';
    }
}

const handleSuccess = (formData: any) => {
  if(formData != null){
    statusList[0].name = '<' + formData.rangeOne + '%';
    statusList[1].name = formData.rangeTwo + '%-' +  formData.rangeThree + "%";
    statusList[2].name = '>' +formData.rangeFour + '%';
  }
};

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await IndexApi.getBalancePage(queryParams)
     var tableIndex = 0;
    // var lessFifteen = 0;
    // var greaterFifteen = 0;
    // var greaterThirty = 0;
    // var smallCurrent = 0;
    data.list.forEach((obj) => {
      obj.tableId = (queryParams.pageNo - 1) * queryParams.pageSize + ++tableIndex;
      
      obj.acur = obj.acur?.toFixed(2);
      obj.bcur = obj.bcur?.toFixed(2);
      obj.ccur = obj.ccur?.toFixed(2);
      obj.curUnbalance = obj.curUnbalance?.toFixed(0);
      obj.avol = obj.avol?.toFixed(1);
      obj.bvol = obj.bvol?.toFixed(1);
      obj.cvol = obj.cvol?.toFixed(1);
      obj.volUnbalance = obj.volUnbalance?.toFixed(0);

    });

    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const getStatistics = async () => {
  const data = await IndexApi.getBalanceStatistics()
    statusNumber.smallCurrent = data.smallCurrent;
    statusNumber.lessFifteen = data.lessFifteen;
    statusNumber.greaterFifteen = data.greaterFifteen;
    statusNumber.greaterThirty = data.greaterThirty;
  }

const getNavList = async() => {
  const res = await IndexApi.getBoxMenu()
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
  const boxId = row.boxId;
  const location = row.location ? row.location : devKey;
  const roomName = row.roomName;
  push({path: '/bus/boxmonitor/boxbalancedetail', state: { devKey, boxId ,location,roomName }})
}

const colorFlag = ref(0);

const showDialogCur = (item) => {
  colorFlag.value = item.color-1;
  dialogVisibleCur.value = true;
  curdevkey.value = item.devKey;
  curRoomName.value = item.roomName;
  boxName.value = item.boxName;
  getBalanceDetail(item);
  getBalanceTrend(item);
}

const showDialogVol = (item) => {
  colorFlag.value = item.color-1;
  dialogVisibleVol.value = true;
  voldevkey.value = item.devKey;
  volRoomName.value = item.roomName;
  boxName.value = item.boxName;
  getBalanceDetail(item);
  getBalanceTrend(item);
}

const cur_valueACopy = ref([]);
const vol_valueACopy = ref([]);

const getBalanceDetail = async(item) => {
  const res = await IndexApi.getBoxBalanceDetail({devKey: item.devKey});
  // 定义默认值
  const defaultCurrentValue = [0.00, 0.00, 0.00];
  const defaultVoltageValue = [0.0, 0.0, 0.0];
 
  let cur_valueA = res.cur_value ? res.cur_value : defaultCurrentValue;
  let vol_value = res.vol_value ? res.vol_value : defaultVoltageValue;

  cur_valueACopy.value = cur_valueA.map(number => number.toFixed(2));
  vol_valueACopy.value = vol_value.map(number => number.toFixed(1));
 
  // 设置电流饼形图数据
  ABarOption.value = {
    title: {
      text: '相电流',
      left: 'left'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{b} : {c}'
    },
    series: [
      {
        type: 'pie',
        radius: ['30%', '80%'],
        center: ['50%', '50%'],
        roseType: 'radius',
        itemStyle: {
          borderRadius: 5
        },
        label: {
          show: true,
          position: 'inside',
          formatter: (params) => `${params.value}A`,
          fontSize: 14,
          fontWeight: 'bold'
        },
        data: [
          { value: cur_valueA[0].toFixed(2), name: 'A相电流', itemStyle: { color: '#E5B849' } },
          { value: cur_valueA[1].toFixed(2), name: 'B相电流', itemStyle: { color: '#C8603A' } },
          { value: cur_valueA[2].toFixed(2), name: 'C相电流', itemStyle: { color: '#AD3762' } },
        ]
      }
    ]
  };
 
  // 设置电压饼形图数据
  BBarOption.value = {
    title: {
      text: '相电压',
      left: 'left'
    },
    tooltip: {
      trigger: 'item'
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '80%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          position: 'inside',
          formatter: (params) => `${params.value}V`,
          fontSize: 14,
          fontWeight: 'bold'
        },
        data: [
          { value: vol_value[0].toFixed(1), name: 'A相电压', itemStyle: { color: '#075F71' } },
          { value: vol_value[1].toFixed(1), name: 'B相电压', itemStyle: { color: '#119CB5' } },
          { value: vol_value[2].toFixed(1), name: 'C相电压', itemStyle: { color: '#45C0C9' } },
        ]
      }
    ]
  };
 
  balanceObj.imbalanceValueA = res.curUnbalance || 0;
  balanceObj.imbalanceValueB = res.volUnbalance || 0;
  balanceObj.colorIndex = (res.color || 1) - 1;
  busName.value = res.busName || '未知';
}

const getBalanceTrend = async (item) => {
  const res = await IndexApi.getBoxBalanceTrend({
    boxId: item.boxId
  })
  if (res.length > 0) {
    const timeList = res.map(item => item.dateTime);
    if(res[0].cur && res[0].cur.length == 1) {
      ALineOption.value.xAxis = {
        type: 'category',
        boundaryGap: false,
        data: timeList
      }
      ALineOption.value.series = [
        {
          name: 'A',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.cur[0].curValue.toFixed(2)),
        },
      ]
    } else if (res[0].cur && res[0].cur.length == 3) {
      ALineOption.value.xAxis = {
        type: 'category',
        boundaryGap: false,
        data: timeList
      }
      ALineOption.value.series = [
        {
          name: 'A',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.cur[0].curValue.toFixed(2)),
        },
        {
          name: 'B',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.cur[1].curValue.toFixed(2)),
        },
        {
          name: 'C',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.cur[2].curValue.toFixed(2)),
        },
      ]
    }
    if (res[0].vol && res[0].vol.length == 1) {
      BLineOption.value.xAxis = {
        type: 'category',
        boundaryGap: false,
        data: timeList
      }
      BLineOption.value.series = [
        {
          name: 'A',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.vol[0].volValue.toFixed(1)),
        },
      ]
    } else if(res[0].vol && res[0].vol.length == 3) {
      BLineOption.value.xAxis = {
        type: 'category',
        boundaryGap: false,
        data: timeList
      }
      BLineOption.value.series = [
        {
          name: 'A',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.vol[0].volValue.toFixed(1)),
        },
        {
          name: 'B',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.vol[1].volValue.toFixed(1)),
        },
        {
          name: 'C',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.vol[2].volValue.toFixed(1)),
        },
      ]
    }
  }
}

const handleSelectStatus = (index) => {
  butColor.value = 1;
  onclickColor.value = index;
  queryParams.curUnbalanceStatus = [index];
  handleQuery();
}

const toggleAllStatus = () => {
  butColor.value = 0;
  onclickColor.value = -1;
  queryParams.curUnbalanceStatus = [0,1,2,3,4];
  handleQuery();
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList();
  getStatistics();
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  butColor.value = 0;
  //statusList.forEach((item) => item.selected = true)
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
    const data = await IndexApi.exportIndex(queryParams)
    download.excel(data, 'PDU设备.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

watch(() => list.value ,async()=>{
  if(dialogVisibleCur.value) {
    getBalanceDetail({devKey: curdevkey.value})
  }
  if(dialogVisibleVol.value) {
    getBalanceDetail({devKey: voldevkey.value})
  }
})

/** 初始化 **/
onMounted(async () => {
  devKeyList.value = await loadAll();
  getCurBalance();
  getList();
  getNavList();
  getStatistics();
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
        width: 78px;
        height: 30px;
        margin: 0 28px;
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
        width: 78px;
        height: 30px;
        margin: 0 28px;
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
}

@media screen and (max-width:2048px) and (min-width:1600px){
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
    .content {
      display: flex;
      align-items: center;
      .icon {
        width: 78px;
        height: 30px;
        margin: 0 28px;
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
}

@media screen and (max-width:1600px){
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
      .icon {
        width: 78px;
        height: 30px;
        margin: 0 28px;
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
  margin-right:-20px;
}

:deep(.el-dialog) {
  width: 80%;
  margin-top: 70px;
  background-color: #f1f1f1;
}

.custom-content{
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.custom-content-container{
  display: flex;
  justify-content: space-between;
  flex-wrap: nowrap;
}

.question {
  width: 12px;
  height: 12px;
  position: absolute;
  top: 2px;
  right: 2px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid;
  border-radius: 50%;
  font-size: 12px;
  cursor: pointer;
}

.cardChilc {
  flex: 1;
  margin: 0 10px;
  box-sizing: border-box;
  .box {
    position: relative;
    height: 121px;
    width: 200px;
    display: flex;
    flex-direction: column;
    align-items: center;
    border: 1px solid #00289e;
    margin: 72px auto;
    
    .value {
      font-size: 30px;
      padding: 20px 0;
    }
    .day {
      width: 100%;
      font-size: 16px;
      text-align: center;
      color: #fff;
      background-color: #00289e;
      padding: 10px 0;
    }
  }
}

:deep(.el-card){
  --el-card-padding:5px;
}
</style>
