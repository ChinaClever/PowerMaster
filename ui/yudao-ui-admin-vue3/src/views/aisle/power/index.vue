<template>
  <CommonMenu @check="handleCheck"  @node-click="handleClick" :showSearch="true" :dataList="serverRoomArr" navTitle="均衡配电">
    <template #NavInfo>
      <div>
        <div class="header">
          <div class="header_img"><img alt="" src="@/assets/imgs/aisle.png" /></div>
        </div>
        <div class="line"></div>
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
        <div>
          <el-form-item label="柜列名称" prop="devKey">
            <el-input
              v-model="queryParams.name"
              placeholder="请输入柜列名称"
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
        <div style="float:right">
          <el-button @click="valueMode = 2;" :type="valueMode == 2 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />功率</el-button>                             
          <el-button @click="valueMode = 0;" :type="valueMode == 0 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />电流</el-button>            
          <el-button @click="valueMode = 1;" :type="valueMode == 1 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />电压</el-button>            
          <el-button @click="switchValue = 0;pageSizeArr=[24,36,48];queryParams.pageSize = 24;getList();" :type="switchValue == 0 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />阵列模式</el-button>
          <el-button @click="switchValue = 3;pageSizeArr=[15, 25,30, 50, 100];queryParams.pageSize = 15;getList();" :type="switchValue == 3 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 4px" />表格模式</el-button>
          <el-button @click="handleStopDelete();switchValue = 2;" :type="switchValue ===2 ? 'primary' : ''" v-show="switchValue ===3"><Icon icon="ep:expand" style="margin-right: 8px" />已删除</el-button>
        </div>
      </el-form>
    </template>
    <template #Content>    
      <div v-if="switchValue == 2" style="height:710px;overflow-y:auto;">
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

          <el-table-column label="柜列名称" min-width="110" align="center">
              <template #default="scope">
                <div>{{scope.row.aisleName}}</div>
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
                type="danger"
                @click="handleRestore(scope.row.id)"
                style="background-color:#409EFF;color:#fff;border:none;width:90px;height:30px;"
              >
                恢复柜列
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
      </div>
      <div v-if="switchValue == 3" style="height:710px;overflow-y:auto;">
        <el-table v-if="switchValue == 3 && valueMode == 0" v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true"  @cell-dblclick="toDetail" :border="true">
          <el-table-column label="编号" align="center" prop="tableId" width="80px"/>
          <!-- 数据库查询 -->
          <el-table-column label="所在位置" align="center" prop="location" width="300px"/>
          <el-table-column label="Ia (A)" align="center">
            <el-table-column label="A路" align="center" prop="AIa">
              <template #default="scope" >
                <el-text line-clamp="2" v-if="scope.row.curAList?.[0] != null">
                  {{ scope.row.curAList[0].toFixed(2) }}
                </el-text>
              </template>
            </el-table-column>
            <el-table-column label="B路" align="center" prop="BIa">
              <template #default="scope" >
                <el-text line-clamp="2" v-if="scope.row.curBList?.[0] != null">
                  {{ scope.row.curBList[0].toFixed(2) }}
                </el-text>
              </template>
            </el-table-column>
          </el-table-column>
          <el-table-column label="Ib (A)" align="center">
            <el-table-column label="A路" align="center" prop="AIb">
              <template #default="scope" >
                <el-text line-clamp="2" v-if="scope.row.curAList?.[1] != null">
                  {{ scope.row.curAList[1].toFixed(2) }}
                </el-text>
              </template>
            </el-table-column>
            <el-table-column label="B路" align="center" prop="BIb">
              <template #default="scope" >
                <el-text line-clamp="2" v-if="scope.row.curBList?.[1] != null">
                  {{ scope.row.curBList[1].toFixed(2) }}
                </el-text>
              </template>
            </el-table-column>
          </el-table-column>
          <el-table-column label="Ic (A)" align="center">
            <el-table-column label="A路" align="center" prop="AIc">
              <template #default="scope" >
                <el-text line-clamp="2" v-if="scope.row.curAList?.[2] != null">
                  {{ scope.row.curAList[2].toFixed(2) }}
                </el-text>
              </template>
            </el-table-column>
            <el-table-column label="B路" align="center" prop="BIc">
              <template #default="scope" >
                <el-text line-clamp="2" v-if="scope.row.curBList?.[2] != null">
                  {{ scope.row.curBList[2].toFixed(2) }}
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
                @click="toDetail(scope.row)"
                style="background-color:#409EFF;color:#fff;border:none;width:40px;height:30px;"
              >
              详情
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
            </template>
          </el-table-column>
        </el-table>

        <el-table v-if="switchValue == 3 && valueMode == 1" v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true"  @cell-dblclick="toDetail" :border="true">
          <el-table-column label="编号" align="center" prop="tableId" width="80px"/>
          <!-- 数据库查询 -->
          <el-table-column label="所在位置" align="center" prop="location" width="300px"/>
          <el-table-column label="Ua (V)" align="center">
            <el-table-column label="A路" align="center" prop="AUa" >
              <template #default="scope" >
                <el-text line-clamp="2" v-if="scope.row.volAList?.[0] != null">
                  {{ scope.row.volAList[0].toFixed(1) }}
                </el-text>
              </template>
            </el-table-column>
            <el-table-column label="B路" align="center" prop="BUa" >
              <template #default="scope" >
                <el-text line-clamp="2" v-if="scope.row.volBList?.[0] != null">
                  {{ scope.row.volBList[0].toFixed(1) }}
                </el-text>
              </template>
            </el-table-column>
          </el-table-column>
          <el-table-column label="Ub (V)" align="center">
            <el-table-column label="A路" align="center" prop="AUb" >
              <template #default="scope" >
                <el-text line-clamp="2" v-if="scope.row.volAList?.[1] != null">
                  {{ scope.row.volAList[1].toFixed(1) }}
                </el-text>
              </template>
            </el-table-column>
            <el-table-column label="B路" align="center" prop="BUb" >
              <template #default="scope" >
                <el-text line-clamp="2" v-if="scope.row.volBList?.[1] != null">
                  {{ scope.row.volBList[1].toFixed(1) }}
                </el-text>
              </template>
            </el-table-column>
          </el-table-column>
          <el-table-column label="Uc (V)" align="center">
            <el-table-column label="A路" align="center" prop="AUc" >
              <template #default="scope" >
                <el-text line-clamp="2" v-if="scope.row.volAList?.[2] != null">
                  {{ scope.row.volAList[2].toFixed(1) }}
                </el-text>
              </template>
            </el-table-column>
            <el-table-column label="B路" align="center" prop="BUc" >
              <template #default="scope" >
                <el-text line-clamp="2" v-if="scope.row.volBList?.[2] != null">
                  {{ scope.row.volBList[2].toFixed(1) }}
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
                @click="toDetail(scope.row)"
                style="background-color:#409EFF;color:#fff;border:none;width:40px;height:30px;"
              >
              详情
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
            </template>
          </el-table-column>
        </el-table>

        <el-table v-if="switchValue == 3 && valueMode == 2" v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true"  @cell-dblclick="toDetail" :border="true">
          <el-table-column label="编号" align="center" prop="tableId" width="80px"/>
          <!-- 数据库查询 -->
          <el-table-column label="所在位置" align="center" prop="location" width="300px"/>
          <el-table-column label="视在功率 (kVA)" align="center">
            <el-table-column label="A路" align="center" prop="powApparentA" >
              <template #default="scope" >
                <el-text line-clamp="2" v-if="scope.row.powApparentA != null">
                  {{ Number(scope.row.powApparentA).toFixed(3) }}
                </el-text>
              </template>
            </el-table-column>
            <el-table-column label="B路" align="center" prop="powApparentB" >
              <template #default="scope" >
                <el-text line-clamp="2" v-if="scope.row.powApparentB != null">
                  {{ Number(scope.row.powApparentB).toFixed(3) }}
                </el-text>
              </template>
            </el-table-column>
          </el-table-column>
          <el-table-column label="有功功率 (kW)" align="center">
            <el-table-column label="A路" align="center" prop="powActiveA" >
              <template #default="scope" >
                <el-text line-clamp="2" v-if="scope.row.powActiveA != null">
                  {{ Number(scope.row.powActiveA).toFixed(3) }}
                </el-text>
              </template>
            </el-table-column>
            <el-table-column label="B路" align="center" prop="powActiveB" >
              <template #default="scope" >
                <el-text line-clamp="2" v-if="scope.row.powActiveB != null">
                  {{ Number(scope.row.powActiveB).toFixed(3) }}
                </el-text>
              </template>
            </el-table-column>
          </el-table-column>
          <el-table-column label="无功功率 (kVar)" align="center">
            <el-table-column label="A路" align="center" prop="powReactiveA" >
              <template #default="scope" >
                <el-text line-clamp="2" v-if="scope.row.powReactiveA != null">
                  {{ Number(scope.row.powReactiveA).toFixed(3) }}
                </el-text>
              </template>
            </el-table-column>
            <el-table-column label="B路" align="center" prop="powReactiveB" >
              <template #default="scope" >
                <el-text line-clamp="2" v-if="scope.row.powReactiveB != null">
                  {{ Number(scope.row.powReactiveB).toFixed(3) }}
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
                @click="toDetail(scope.row)"
                style="background-color:#409EFF;color:#fff;border:none;width:40px;height:30px;"
              >
              详情
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
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-show="switchValue == 0 && list.length > 0"  class="arrayContainer">
        <div class="arrayItem" v-for="item in list" :key="item.devKey">
          <div class="devKey">{{ item.location != null ? item.location : item.devKey }}</div>
          <div v-if="item.eleActiveTotal != null && item.eleActiveTotal != 0" class="content">
            <div class="info" >
              <div v-if="item.powApparentTotal != null">
                <el-text>
                  总视在功率：{{item.powApparentTotal ? item.powApparentTotal : Number(0).toFixed(3)}}kVA
                </el-text>
              </div>
              <div v-if="item.powActiveTotal != null">
                <el-text>
                  总有功功率：{{item.powActiveTotal ? item.powActiveTotal : Number(0).toFixed(3)}}kW
                </el-text>
              </div>
              <div v-if="item.powReactiveTotal != null">
                <el-text>
                  总无功功率：{{item.powReactiveTotal ? item.powReactiveTotal : Number(0).toFixed(3)}}kVar
                </el-text>
              </div>
            </div>
            <div v-if="item.powerFactor != null" style="display: flex;flex-direction: column;">
              <div style="text-align: center;font-size: 24px;">{{item.powerFactor ? item.powerFactor : 0}}</div> <!-- TODO 换成总功率因数 -->
              <div style="text-align: center;font-size: 10px;">总功率因数</div>
            </div>
          </div>
          <div style="display:flex;height:10vh;justify-content: center;align-item:center;margin-bottom: -3vh;margin-top: 1vh">
            <Environment v-if="valueMode == 0 && item.curAList" class="chart" width="100%" height="100%" :load-factor="{first: item.curAList?.[2] ? item.curAList[2].toFixed(0) : 0,second: item.curAList?.[1] ? item.curAList[1].toFixed(0) : 0,third: item.curAList?.[0] ? item.curAList[0].toFixed(0) : 0,label: ['Ic','Ib','Ia'],unit: ['A','A','A'],color: ['#AD3762','#C8603A','#E5B849']}" style="margin-right:-15px;"/> <!-- TODO 换成A路电流 -->
            <Environment v-else-if="valueMode == 1 && item.volAList" class="chart" width="100%" height="100%" :load-factor="{first: item.volAList?.[2] ? item.volAList[2].toFixed(0) : 0,second: item.volAList?.[1] ? item.volAList[1].toFixed(0) : 0,third: item.volAList?.[0] ? item.volAList[0].toFixed(0) : 0,label: ['Uc','Ub','Ua'],unit: ['V','V','V'],color: ['#45C0C9','#119CB5','#075F71']}" style="margin-right:-15px;"/> <!-- TODO 换成A路电压 -->
            <Environment v-else-if="valueMode == 2 && item.curAList" class="chart" width="100%" height="100%" :load-factor="{first: item.powReactiveA ? Number(item.powReactiveA).toFixed(0) : 0,second: item.powActiveA ? Number(item.powActiveA).toFixed(0) : 0,third: item.powApparentA ? Number(item.powApparentA).toFixed(0) : 0,label: ['Q','P','S'],unit: ['kVar', 'kW', 'kVA'],color: ['#800080','#91cc75','#5470c6']}" style="margin-right:-15px;"/>

            <EnvironmentCopy v-if="valueMode == 0 && item.curBList" class="chart" width="100%" height="100%" :load-factor="{first: item.curBList?.[2] ? item.curBList[2].toFixed(0) : 0,second: item.curBList?.[1] ? item.curBList[1].toFixed(0) : 0,third: item.curBList?.[0] ? item.curBList[0].toFixed(0) : 0,label: ['Ic','Ib','Ia'],unit: ['A','A','A'],color: ['#AD3762','#C8603A','#E5B849']}"/> <!-- TODO 换成B路电流 -->
            <EnvironmentCopy v-else-if="valueMode == 1 && item.volBList" class="chart" width="100%" height="100%" :load-factor="{first: item.volBList?.[2] ? item.volBList[2].toFixed(0) : 0,second: item.volBList?.[1] ? item.volBList[1].toFixed(0) : 0,third: item.volBList ? item.volBList[0].toFixed(0) : 0,label: ['Uc','Ub','Ua'],unit: ['V','V','V'],color: ['#45C0C9','#119CB5','#075F71']}"/> <!-- TODO 换成B路电压 -->
            <EnvironmentCopy v-else-if="valueMode == 2 && item.curBList" class="chart" width="100%" height="100%" :load-factor="{first: item.powReactiveB ? Number(item.powReactiveB).toFixed(0) : 0,second: item.powActiveB ? Number(item.powActiveB).toFixed(0) : 0,third: item.powApparentB ? Number(item.powApparentB).toFixed(0) : 0,label: ['Q','P','S'],unit: ['kVar', 'kW', 'kVA'],color: ['#800080','#91cc75','#5470c6']}"/>
          </div>
          <div v-if="item.curAList || item.curBList" style="display:flex;justify-content: space-around;padding: 5px 0;">
            <div v-if="item.curAList">A路</div>
            <div v-if="item.curBList">B路</div>
          </div>
          <div v-if="item.eleActiveTotal != null && item.eleActiveTotal != 0" style="position: absolute;bottom: 0;right: 0">
            <button class="detail" @click="toDetail(item)">详情</button>
          </div>
          <!-- <div v-if="item.eleActiveTotal != null" style="display: inline-block;
            width: 50%;
            height: 50%;
            margin-right:-15px;"
          >
            <Environment v-if="valueMode == 0" class="chart" width="100%" height="100%" :load-factor="item"/>
            <EnvironmentCopy v-else-if="valueMode == 2" class="chart" width="100%" height="100%" :load-factor="item"/>
          </div>
          <div v-if="item.eleActiveTotal != null" style="display: inline-block;
              width: 50%;
              height: 50%;">
            <EnvironmentCopy v-if="valueMode == 0" class="chart" width="100%" height="100%" :load-factor="item"/>
            <EnvironmentCopy v-else-if="valueMode == 1" class="chart" width="100%" height="100%" :load-factor="item"/>
            <EnvironmentCopy v-else-if="valueMode == 2" class="chart" width="100%" height="100%" :load-factor="item"/>
          </div> -->
          <!-- <div class="room">{{item.jf}}-{{item.mc}}</div> -->
          <!-- <div class="status" >
            <el-tag type="info" v-if="item.status == null ||  item.status == 5" >离线</el-tag>
          </div> -->
          <div class="status" >
            <el-tag v-if="item.eleActiveTotal != null && item.eleActiveTotal != 0" >在线</el-tag>
            <el-tag v-else-if="(item.eleActiveTotal == null || item.eleActiveTotal == 0) && item.status != 5" type="info">无数据</el-tag>
            <el-tag v-else type="info">离线</el-tag>            
          </div>
        </div>
      </div>

      <Pagination
        v-if="switchValue == 2"
        :total="queryDeleteParams.pageTotal"
        v-model:page="queryDeleteParams.pageNo"
        v-model:limit="queryDeleteParams.pageSize"
        @pagination="handleStopDelete"
      />
      <Pagination
        v-else
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
import download from '@/utils/download'
import { IndexApi } from '@/api/aisle/aisleindex'
// import CurbalanceColorForm from './CurbalanceColorForm.vue'
import { ElTree } from 'element-plus'
import { IndexApi as BusIndexApi } from '@/api/bus/busindex'
import Environment from './component/Environment.vue'
import EnvironmentCopy from './component/EnvironmentCopy.vue'

// import { CurbalanceColorApi } from '@/api/pdu/curbalancecolor'

/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })

const { push } = useRouter()

const curBalanceColorForm = ref()
const flashListTimer = ref();
const firstTimerCreate = ref(true);
const pageSizeArr = ref([24,36,48])
const switchValue = ref(0)
const valueMode = ref(2)
const deletedList = ref<any>([]) //已删除的

const handleClick = (row) => {
  console.log("click",row)
}

const handleCheck = async (row) => {
  if(row.length == 0){
    queryParams.aisleIds = null;
    getList();
    return;
  }
  const ids = [] as any
  var haveCabinet = false;
  row.forEach(item => {
    if (item.type == 2) {
      ids.push(item.id)
      haveCabinet = true;
    }
  })
  if(!haveCabinet ){
    queryParams.aisleIds = [-1]
  }else{
    queryParams.aisleIds = ids
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
  name: undefined,
  createTime: [],
  cascadeNum: undefined,
  serverRoomData:undefined,
  status:undefined,
  aisleIds : [],
})as any
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中

const queryDeleteParams = reactive({
  company: undefined,
  showCol: [1, 2, 12, 13, 15, 16] as number[],
  name: undefined,
  pageNo: 1,
  pageSize: 20,
  pageTotal: 0,
})

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await IndexApi.getAisleRedisPage(queryParams)

    list.value = data.list
    var tableIndex = 0;

    list.value.forEach((obj) => {
      obj.tableId = (queryParams.pageNo - 1) * queryParams.pageSize + ++tableIndex;

      obj.eleActiveTotal = obj.eleActiveTotal?.toFixed(1);
      obj.powApparentTotal = obj.powApparentTotal?.toFixed(3);
      obj.powActiveTotal = obj.powActiveTotal?.toFixed(3);
      obj.powReactiveTotal = obj.powReactiveTotal?.toFixed(3);
      
      obj.eleActiveA = obj.eleActiveA?.toFixed(1);

      obj.eleActiveB = obj.eleActiveB?.toFixed(1);
    });

    total.value = data.total
  } finally {
    loading.value = false
  }
}

const getListNoLoading = async () => {
  try {
    const data = await IndexApi.getAisleRedisPage(queryParams)
    list.value = data.list
    var tableIndex = 0;    

    list.value.forEach((obj) => {
      obj.tableId = (queryParams.pageNo - 1) * queryParams.pageSize + ++tableIndex;
      
      obj.eleActiveTotal = obj.eleActiveTotal?.toFixed(1);
      obj.powApparentTotal = obj.powApparentTotal?.toFixed(3);
      obj.powActiveTotal = obj.powActiveTotal?.toFixed(3);
      obj.powReactiveTotal = obj.powReactiveTotal?.toFixed(3);
      
      obj.eleActiveA = obj.eleActiveA?.toFixed(1);

      obj.eleActiveB = obj.eleActiveB?.toFixed(1);
    });

    total.value = data.total
  } catch (error) {
    
  }
}

const getNavList = async() => {
  const res = await IndexApi.getAisleMenu()
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

const toDetail = async(item) =>{
  push({path: '/aisle/aislemonitor/columnHome', state: { id: item.id, roomId: item.roomId }})
}

// const openNewPage = (scope) => {
//   const url = 'http://' + scope.row.devKey.split('-')[0] + '/index.html';
//   window.open(url, '_blank');
// }


/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  queryDeleteParams.pageNo = 1
  queryDeleteParams.name = queryParams.name
  if(switchValue.value == 2) {
    handleStopDelete()
  } else {
    getList()
  }
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryParams.name = undefined;
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

//已删除
const handleStopDelete = async() =>{
  const res = await IndexApi.deletedAisleInfo(queryDeleteParams)
  deletedList.value = res.list;
  queryDeleteParams.pageTotal = res.total;
}

//恢复机房
const handleRestore = async (flagAisleid) => {
  ElMessageBox.confirm('确认恢复柜列吗？', '提示', {
    confirmButtonText: '确 认',
    cancelButtonText: '取 消',
    type: 'warning'
  }).then(async () => {
    const res = await IndexApi.restoreAisleInfo({id: flagAisleid});
    if(res != null || res != "") {
      message.success('恢复成功')
      deletedList.value = deletedList.value.filter(item => item.id != flagAisleid)
    } else {
      message.error('恢复失败')
    }
  })
}

/** 初始化 **/
onMounted(async () => {
  getList()
  getNavList();
  flashListTimer.value = setInterval((getListNoLoading), 5000);
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
    flashListTimer.value = setInterval((getListNoLoading), 5000);
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
    font-size: 13px;
    box-sizing: border-box;
    background-color: #eef4fc;
    border: 5px solid #fff;
    padding-top: 40px;
    position: relative;
    .content {
      display: flex;
      justify-content: space-around;
      align-items: center;
      .icon {
        width: 74px;
        height: 30px;
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
      margin-right: 8px;
      margin-bottom: 8px;
      cursor: pointer;
      z-index: 10
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
:deep .el-table thead th.el-table__cell {
  background: var(--el-fill-color-light);
}
</style>
