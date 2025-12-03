<template>
  <div style="margin-top:5px">
    <el-card shadow>
      <div
        slot="header"
        class="rank-title"
      >
        <span class="panel-title">{{ $t('m.Record_List') }}</span>
        <div style="font-size: 12px; color: #999; margin-top: 5px;">
          训练ID: {{ trainingID }} | 当前页数据: {{ dataRank.length }} | 总数据: {{ total }} | 页码: {{ page }}/{{ Math.ceil(total / limit) }}
        </div>
      </div>
      <div class="training-rank-search">
        <el-input
          :placeholder="$t('m.Training_Rank_Search_Placeholder')"
          v-model="keyword"
          @keyup.enter.native="getTrainingRankData"
        >
          <el-button
            slot="append"
            icon="el-icon-search"
            class="search-btn"
            @click="getTrainingRankData"
          ></el-button>
        </el-input>
        <el-button
          type="primary"
          icon="el-icon-refresh"
          @click="refreshData"
          :loading="loading"
          style="margin-left: 10px;"
        >
          刷新
        </el-button>
      </div>
      <vxe-table
        round
        border
        auto-resize
        size="medium"
        align="center"
        :data="dataRank"
        :cell-class-name="cellClassName"
        ref="TraningtRank"
        :seq-config="{ startIndex: (this.page - 1) * this.limit }"
        @cell-click="getUserProblemSubmission"
        :loading="loading"
        empty-text="暂无训练记录"
      >
        <vxe-table-column
          field="rank"
          type="seq"
          width="50"
          fixed="left"
        ></vxe-table-column>
        <vxe-table-column
          field="username"
          fixed="left"
          v-if="!isMobileView"
          min-width="300"
          :title="$t('m.User')"
          header-align="center"
          align="left"
        >
          <template v-slot="{ row }">
            <div class="contest-rank-user-box">
              <span style="margin-right: 0.5rem">
                <avatar
                  :username="row.username"
                  :inline="true"
                  :size="37"
                  color="#FFF"
                  :src="row.avatar"
                  :title="row.username"
                ></avatar>
              </span>
              <span class="contest-rank-user-info">
                <a @click="getUserHomeByUsername(row.uid, row.username)">
                  <span
                    class="contest-username"
                    :title="row.username"
                  >
                    <span
                      class="contest-rank-flag"
                      v-if="row.uid == userInfo.uid"
                    >Own</span>
                    <span
                      class="contest-rank-flag"
                      v-if="row.gender == 'female'"
                    >Girl</span>
                    {{ row.username }}</span>
                  <span
                    class="contest-school"
                    v-if="row.school"
                    :title="row.school"
                  >{{
                    row.school
                  }}</span>
                </a>
              </span>
            </div>
          </template>
        </vxe-table-column>
        <vxe-table-column
          field="username"
          v-else
          min-width="300"
          :title="$t('m.User')"
          header-align="center"
          align="left"
        >
          <template v-slot="{ row }">
            <div class="contest-rank-user-box">
              <span style="margin-right: 0.5rem">
                <avatar
                  :username="row.username"
                  :inline="true"
                  :size="37"
                  color="#FFF"
                  :src="row.avatar"
                  :title="row.username"
                ></avatar>
              </span>
              <span class="contest-rank-user-info">
                <a @click="getUserHomeByUsername(row.uid, row.username)">
                  <span
                    class="contest-username"
                    :title="row.username"
                  >
                    <span
                      class="contest-rank-flag"
                      v-if="row.uid == userInfo.uid"
                    >Own</span>
                    <span
                      class="contest-rank-flag"
                      v-if="row.gender == 'female'"
                    >Girl</span>
                    {{ row.username }}</span>
                  <span
                    class="contest-school"
                    v-if="row.school"
                    :title="row.school"
                  >{{
                    row.school
                  }}</span>
                </a>
              </span>
            </div>
          </template>
        </vxe-table-column>
        <vxe-table-column
          field="realname"
          min-width="96"
          :title="$t('m.RealName')"
          show-overflow
          v-if="isTrainingAdmin"
        >
        </vxe-table-column>
        <vxe-table-column
          field="rating"
          :title="$t('m.Total_AC')"
          min-width="90"
        >
          <template v-slot="{ row }">
            <span><a
                @click="getUserACSubmit(row.username)"
                style="color:rgb(87, 163, 243);font-size:16px"
              >{{ row.ac }}</a>
              <br />
              <span class="judge-time">({{ row.totalRunTime }}ms)</span>
            </span>
          </template>
        </vxe-table-column>
        <vxe-table-column
          min-width="70"
          v-for="(problem, index) in trainingProblemList"
          :key="index"
          :field="problem.problemId"
        >
          <template v-slot:header>
            <div style="text-align: center;">
              <div style="font-weight: bold; margin-bottom: 2px;">
                <a
                  @click="getTrainingProblemById(problem.problemId)"
                  class="emphasis"
                  style="color:#495060; font-size: 12px;"
                >{{ problem.problemId }}</a>
              </div>
              <div style="font-size: 10px; color: #999;">
                <a
                  @click="getTrainingProblemById(problem.problemId)"
                  class="emphasis"
                  style="color:#999;"
                >{{ problem.title || '题目' + problem.problemId }}</a>
              </div>
            </div>
          </template>
          <template v-slot="{ row }">
            <template v-if="row.submissionInfo[problem.problemId]">
              <el-tooltip
                effect="dark"
                placement="top"
              >
                <div slot="content">
                  {{
                    JUDGE_STATUS[row.submissionInfo[problem.problemId].status]
                      .name
                  }}
                </div>
                <span
                  class="judge-status submission-hover"
                  :style="
                    'color:' +
                      JUDGE_STATUS[row.submissionInfo[problem.problemId].status]
                        .rgb
                  "
                >
                  {{
                    JUDGE_STATUS[row.submissionInfo[problem.problemId].status]
                      .short
                  }}
                </span>
              </el-tooltip>
              <br />
              <span class="judge-time">
                ({{
                  row.submissionInfo[problem.problemId].runTime
                    ? row.submissionInfo[problem.problemId].runTime
                    : 0
                }}ms)
              </span>
            </template>
          </template>
        </vxe-table-column>
      </vxe-table>
      <Pagination
        :total="total"
        :page-size.sync="limit"
        :current.sync="page"
        @on-change="getTrainingRankData"
        @on-page-size-change="getTrainingRankData(1)"
        :layout="'prev, pager, next, sizes'"
      ></Pagination>
    </el-card>
  </div>
</template>
<script>
import Avatar from "vue-avatar";
import { mapActions, mapGetters } from "vuex";
import { JUDGE_STATUS } from "@/common/constants";
const Pagination = () => import("@/components/oj/common/Pagination");
import api from "@/common/api";
import { mapState } from "vuex";
import time from "@/common/time";

export default {
  name: "TrainingRank",
  components: {
    Pagination,
    Avatar,
  },
  data() {
    return {
      total: 0,
      page: 1,
      limit: 30,
      keyword: '',
      trainingID: "",
      dataRank: [],
      JUDGE_STATUS: {},
      groupID: null,
      loading: false,
    };
  },
  mounted() {
    this.JUDGE_STATUS = Object.assign({}, JUDGE_STATUS);
    if (this.$route.params.groupID) {
      this.groupID = this.$route.params.groupID;
    }
    if (!this.trainingProblemList.length) {
      this.getTrainingProblemList();
    }
    this.trainingID = this.$route.params.trainingID;
    this.getTrainingRankData();
  },
  methods: {
    ...mapActions(["getTrainingProblemList"]),

    getTrainingRankData() {
      // 检查训练访问权限
      if (this.trainingMenuDisabled) {
        console.log('训练榜单被禁用，无法加载数据');
        this.$message.warning('您没有权限查看此训练的榜单');
        return;
      }
      
      let params = {
        tid: this.trainingID,
        limit: this.limit,
        currentPage: this.page,
        keyword: this.keyword
      };
      console.log('获取训练榜单数据:', params);
      this.loading = true;
      
      api.getTrainingRank(params).then(
        (res) => {
          console.log('训练榜单API响应:', res);
          if (res.data && res.data.data) {
            console.log('API返回的数据结构:', res.data.data);
            console.log('API返回的total值:', res.data.data.total);
            console.log('API返回的records:', res.data.data.records);
            console.log('records数组长度:', res.data.data.records ? res.data.data.records.length : 0);
            
            this.total = res.data.data.total || 0;
            this.applyToTable(res.data.data.records || []);
            console.log('训练榜单数据加载成功，总数:', this.total, '当前页数据条数:', this.dataRank.length);
          } else {
            console.warn('训练榜单数据格式异常:', res);
            this.dataRank = [];
            this.total = 0;
          }
          this.loading = false;
        },
        (err) => {
          console.error('获取训练榜单失败:', err);
          const errorMsg = err.data && err.data.msg ? err.data.msg : (err.message || '获取训练榜单失败，请稍后重试');
          this.$message.error(errorMsg);
          this.dataRank = [];
          this.total = 0;
          this.loading = false;
        }
      );
    },

    getUserACSubmit(username) {
      if (!this.groupID) {
        this.$router.push({
          name: "SubmissionList",
          query: { username: username, status: 0 },
        });
      } else {
        this.$router.push({
          name: "GroupSubmissionList",
          params: { groupID: this.groupID },
          query: { username: username, status: 0 },
        });
      }
    },
    getUserHomeByUsername(uid, username) {
      this.$router.push({
        name: "UserHome",
        query: { username: username, uid: uid },
      });
    },
    getTrainingProblemById(pid) {
      if (!this.groupID) {
        this.$router.push({
          name: "TrainingProblemDetails",
          params: {
            trainingID: this.trainingID,
            problemID: pid,
          },
        });
      } else {
        this.$router.push({
          name: "GroupTrainingProblemDetails",
          params: {
            trainingID: this.trainingID,
            groupID: this.groupID,
            problemID: pid,
          },
        });
      }
    },
    getUserProblemSubmission({ row, column }) {
      if (
        column.property !== "rank" &&
        column.property !== "username" &&
        column.property !== "realname" &&
        column.property !== "rating"
      ) {
        if (!this.groupID) {
          this.$router.push({
            name: "SubmissionList",
            query: { username: row.username, problemID: column.property },
          });
        } else {
          this.$router.push({
            name: "GroupSubmissionList",
            params: { groupID: this.groupID },
            query: { username: row.username, problemID: column.property },
          });
        }
      }
    },
    cellClassName({ row, rowIndex, column, columnIndex }) {
      if (row.username == this.userInfo.username) {
        if (
          column.property == "rank" ||
          column.property == "username" ||
          column.property == "realname"
        ) {
          return "own-submit-row";
        }
      }
      if (column.property === "username" && row.userCellClassName) {
        return row.userCellClassName;
      }
    },
    applyToTable(dataRank) {
      console.log('处理训练榜单数据:', dataRank);
      console.log('输入数据类型:', typeof dataRank);
      console.log('输入数据是否为数组:', Array.isArray(dataRank));
      console.log('输入数据长度:', dataRank ? dataRank.length : 'null/undefined');
      
      if (!Array.isArray(dataRank)) {
        console.warn('训练榜单数据不是数组:', dataRank);
        this.dataRank = [];
        return;
      }
      
      dataRank.forEach((rank, i) => {
        if (dataRank[i].gender == "female") {
          dataRank[i].userCellClassName = "bg-female";
        }
        // 确保submissionInfo存在
        if (!dataRank[i].submissionInfo) {
          dataRank[i].submissionInfo = {};
        }
      });
      this.dataRank = dataRank;
      console.log('训练榜单数据处理完成，最终数据条数:', this.dataRank.length);
      console.log('最终数据:', this.dataRank);
    },
    refreshData() {
      console.log('手动刷新训练榜单数据');
      this.page = 1;
      this.keyword = '';
      this.getTrainingRankData();
    },
    parseTotalTime(totalTime) {
      return time.secondFormat(totalTime);
    },
  },
  watch: {
    trainingMenuDisabled(newVal) {
      console.log('训练榜单权限状态变化:', newVal);
      // 当权限状态改变时，重新加载数据
      if (!newVal && this.trainingID) {
        console.log('权限已获得，重新加载训练榜单数据');
        this.getTrainingRankData();
      }
    }
  },
  computed: {
    ...mapState({
      trainingProblemList: (state) => state.training.trainingProblemList,
    }),
    ...mapGetters(["isTrainingAdmin", "userInfo", "trainingMenuDisabled"]),
    training() {
      return this.$store.state.training.training;
    },
    isMobileView() {
      return window.screen.width < 768;
    },
  },
};
</script>
<style scoped>
.rank-title {
  text-align: center;
}
/deep/.el-card__body {
  padding: 20px !important;
}
.training-rank-search{
  text-align: center;
  margin: 10px auto;
  width: 90%;
}
@media screen and (min-width: 768px){
  .training-rank-search{
    width: 50%;
  }
}
@media screen and (min-width: 1050px){
  .training-rank-search{
    width: 30%;
  }
}

.vxe-cell p,
.vxe-cell span {
  margin: 0;
  padding: 0;
}
@media screen and (max-width: 768px) {
  /deep/.el-card__body {
    padding: 0 !important;
  }
}
a.emphasis {
  color: #495060 !important;
}
a.emphasis:hover {
  color: #2d8cf0 !important;
}

/deep/.vxe-table .vxe-header--column:not(.col--ellipsis) {
  padding: 4px 0 !important;
}
/deep/.vxe-table .vxe-body--column {
  padding: 4px 0 !important;
  line-height: 20px !important;
}
/deep/.vxe-table .vxe-body--column:not(.col--ellipsis) {
  line-height: 20px !important;
  padding: 0 !important;
}
/deep/.vxe-body--column {
  min-width: 0;
  height: 51px !important;
  box-sizing: border-box;
  text-align: left;
  text-overflow: ellipsis;
  vertical-align: middle;
}
/deep/.vxe-table .vxe-cell {
  padding-left: 5px !important;
  padding-right: 5px !important;
}
.judge-status {
  font-size: 16px;
  font-weight: bold;
}
.judge-time {
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}
</style>
