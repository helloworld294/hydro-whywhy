<template>
  <el-row type="flex" justify="space-around">
    <el-col :span="24">
      <el-card :padding="10">
        <div slot="header">
          <span class="panel-title">{{ $t('m.Total_Ranklist') }}</span>
        </div>
        <div class="echarts">
          <ECharts :options="options" ref="chart" :autoresize="true"></ECharts>
        </div>
      </el-card>
      <el-card :padding="10" style="text-align: center;">
        <el-input
          :placeholder="$t('m.Rank_Search_Placeholder')"
          v-model="searchUser"
          @keyup.enter.native="getRankData(1)"
        >
          <el-button
            slot="append"
            icon="el-icon-search"
            class="search-btn"
            @click="getRankData(1)"
          ></el-button>
        </el-input>
      </el-card>
      <vxe-table
        :data="dataRank"
        :loading="loadingTable"
        align="center"
        highlight-hover-row
        :seq-config="{ seqMethod }"
        auto-resize
        style="font-weight: 500;"
      >
        <vxe-table-column type="seq" min-width="50"></vxe-table-column>
        <vxe-table-column
          field="username"
          :title="$t('m.User')"
          min-width="200"
          show-overflow
          align="left"
        >
          <template v-slot="{ row }">
            <avatar
              :username="row.username"
              :inline="true"
              :size="25"
              color="#FFF"
              :src="row.avatar"
              class="user-avatar"
            ></avatar>
            <a
              @click="getInfoByUsername(row.uid, row.username)"
              style="color:#2d8cf0;"
              >{{ row.username }}</a
            >
            <span style="margin-left:2px" v-if="row.titleName">
              <el-tag effect="dark" size="small" :color="row.titleColor">
                {{ row.titleName }}
              </el-tag>
            </span>
          </template>
        </vxe-table-column>
        <vxe-table-column
          field="nickname"
          :title="$t('m.Nickname')"
          width="160"
        >
          <template v-slot="{ row }">
            <el-tag
              effect="plain"
              size="small"
              v-if="row.nickname"
              :type="nicknameColor(row.nickname)"
            >
              {{ row.nickname }}
            </el-tag>
          </template>
        </vxe-table-column>
        <vxe-table-column :title="$t('m.Score')" min-width="80">
          <template v-slot="{ row }">
            <span>{{ row.totalScore }}</span>
          </template>
        </vxe-table-column>
        <vxe-table-column field="ac" :title="$t('m.AC')" min-width="80">
          <template v-slot="{ row }">
            <span>
              <a
                @click="goUserACStatus(row.username)"
                style="color:rgb(87, 163, 243);"
                >{{ row.ac }}</a
              >
            </span>
          </template>
        </vxe-table-column>
        <vxe-table-column :title="$t('m.Total')" min-width="100" field="total">
        </vxe-table-column>
        <vxe-table-column :title="$t('m.Rating')" min-width="80">
          <template v-slot="{ row }">
            <span>{{ getACRate(row.ac, row.total) }}</span>
          </template>
        </vxe-table-column>
        <vxe-table-column
          field="signature"
          :title="$t('m.Signature')"
          min-width="300"
          show-overflow="ellipsis"
          align="left"
        >
          <template v-slot="{ row }">
            <span v-katex class="rank-signature-body" v-if="row.signature">{{
              row.signature
            }}</span>
          </template>
        </vxe-table-column>
      </vxe-table>

      <Pagination
        :total="total"
        :page-size.sync="limit"
        :current.sync="page"
        @on-change="getRankData"
        show-sizer
        @on-page-size-change="getRankData(1)"
        :layout="'prev, pager, next, sizes'"
      ></Pagination>
    </el-col>
  </el-row>
</template>

<script>
import api from '@/common/api';
import utils from '@/common/utils';
import { RULE_TYPE } from '@/common/constants';
import { mapGetters } from 'vuex';
import Avatar from 'vue-avatar';
const Pagination = () => import('@/components/oj/common/Pagination');
export default {
  name: 'total-rank',
  components: {
    Pagination,
    Avatar,
  },
  data() {
    return {
      page: 1,
      limit: 30,
      total: 0,
      searchUser: null,
      loadingTable: false,
      screenWidth: 768,
      dataRank: [],
      allRankData: [],
      options: {
        tooltip: {
          trigger: 'axis',
        },
        legend: {
          data: ['Score'],
        },
        grid: {
          x: '3%',
          x2: '3%',
          left: '8%',
          right: '8%',
        },
        toolbox: {
          show: true,
          feature: {
            dataView: { show: true, readOnly: true },
            magicType: { show: true, type: ['line', 'bar', 'stack'] },
            saveAsImage: { show: true },
          },
          right: '8%',
          top: '5%',
        },
        calculable: true,
        xAxis: [
          {
            type: 'category',
            data: ['root'],
            axisLabel: {
              interval: 0,
              showMinLabel: true,
              showMaxLabel: true,
              align: 'center',
              formatter: (value, index) => {
                if (this.screenWidth < 768) {
                  if (this.isAuthenticated && this.userInfo.username == value) {
                    return utils.breakLongWords(value, 14);
                  } else {
                    return '';
                  }
                } else {
                  return utils.breakLongWords(value, 14);
                }
              },
            },
          },
        ],
        yAxis: [
          {
            type: 'value',
            axisLabel: {
              rotate: 50,
              textStyle: {
                fontSize: '12em',
              },
            },
          },
        ],
        series: [
          {
            name: this.$i18n.t('m.Score'),
            type: 'bar',
            data: [0],
            barMaxWidth: '80',
            itemStyle: {
              color: '#91c7ae',
            },
            markPoint: {
              data: [{ type: 'max', name: 'max' }],
            },
          },
        ],
      },
    };
  },
  created() {
    this.screenWidth = window.screen.width;
    const that = this;
    window.onresize = () => {
      return (() => {
        that.screenWidth = document.documentElement.clientWidth;
      })();
    };
  },
  mounted() {
    this.getRankData(1);
  },
  methods: {
    async getRankData(page) {
      let bar = this.$refs.chart;
      bar.showLoading({ maskColor: 'rgba(250, 250, 250, 0.8)' });
      this.loadingTable = true;
      
      try {
        // 同时获取ACM和OI排名数据
        const [acmRes, oiRes] = await Promise.all([
          api.getUserRank(1, 10000, RULE_TYPE.ACM, this.searchUser),
          api.getUserRank(1, 10000, RULE_TYPE.OI, this.searchUser)
        ]);

        // 合并数据
        const acmData = acmRes.data.data.records || [];
        const oiData = oiRes.data.data.records || [];
        
        // 使用Map来合并相同用户的数据
        const userMap = new Map();
        
        // 处理ACM数据
        acmData.forEach(user => {
          userMap.set(user.uid, {
            uid: user.uid,
            username: user.username,
            nickname: user.nickname,
            signature: user.signature,
            avatar: user.avatar,
            titleName: user.titleName,
            titleColor: user.titleColor,
            ac: user.ac || 0,
            total: user.total || 0,
            acmRating: user.rating || 0,
            oiScore: 0,
            totalScore: user.rating || 0
          });
        });
        
        // 合并OI数据
        oiData.forEach(user => {
          const existing = userMap.get(user.uid);
          if (existing) {
            // 如果用户已存在，AC取最大值（因为ACM和OI的AC都是从user_acproblem统计的，应该相同）
            // 如果不同，取最大值避免重复计算
            existing.ac = Math.max(existing.ac || 0, user.ac || 0);
            // total累加（因为ACM和OI的total统计方式不同）
            existing.total = (existing.total || 0) + (user.total || 0);
            existing.oiScore = (user.score || 0);
            existing.totalScore = (existing.acmRating || 0) + (user.score || 0);
          } else {
            // 如果用户不存在，添加新用户
            userMap.set(user.uid, {
              uid: user.uid,
              username: user.username,
              nickname: user.nickname,
              signature: user.signature,
              avatar: user.avatar,
              titleName: user.titleName,
              titleColor: user.titleColor,
              ac: user.ac || 0,
              total: user.total || 0,
              acmRating: 0,
              oiScore: user.score || 0,
              totalScore: user.score || 0
            });
          }
        });
        
        // 转换为数组并按总分数降序排序
        this.allRankData = Array.from(userMap.values()).sort((a, b) => {
          if (b.totalScore !== a.totalScore) {
            return b.totalScore - a.totalScore;
          }
          // 分数相同则按AC数降序
          if (b.ac !== a.ac) {
            return b.ac - a.ac;
          }
          return a.total - b.total;
        });
        
        // 更新总数
        this.total = this.allRankData.length;
        
        // 前端分页
        const start = (page - 1) * this.limit;
        const end = start + this.limit;
        this.dataRank = this.allRankData.slice(start, end);
        
        // 更新图表（只显示第一页的前10名）
        if (page === 1) {
          this.changeCharts(this.allRankData.slice(0, 10));
        }
        
        this.loadingTable = false;
        bar.hideLoading();
      } catch (err) {
        this.loadingTable = false;
        bar.hideLoading();
      }
    },
    seqMethod({ rowIndex }) {
      return this.limit * (this.page - 1) + rowIndex + 1;
    },
    changeCharts(rankData) {
      let [usernames, scoreData] = [[], []];
      rankData.forEach((ele) => {
        usernames.push(ele.username);
        scoreData.push(ele.totalScore);
      });
      this.options.xAxis[0].data = usernames;
      this.options.series[0].data = scoreData;
    },
    getInfoByUsername(uid, username) {
      this.$router.push({
        path: '/user-home',
        query: { uid, username },
      });
    },
    goUserACStatus(username) {
      this.$router.push({
        path: '/status',
        query: { username, status: 0 },
      });
    },
    getACRate(ac, total) {
      return utils.getACRate(ac, total);
    },
    nicknameColor(nickname) {
      let typeArr = ['', 'success', 'info', 'danger', 'warning'];
      let index = nickname.length % 5;
      return typeArr[index];
    },
  },
  computed: {
    ...mapGetters(['isAuthenticated', 'userInfo']),
  },
};
</script>

<style scoped>
.echarts {
  margin: 0 auto;
  width: 100%;
  height: 400px;
}
@media screen and (max-width: 768px) {
  /deep/.el-card__body {
    padding: 0 !important;
  }
}
@media screen and (min-width: 768px) {
  .el-input-group {
    width: 50%;
  }
}
@media screen and (min-width: 1050px) {
  .el-input-group {
    width: 30%;
  }
}
</style>
<style>
.rank-signature-body img {
  height: 50px !important;
  width: 50px !important;
}
.rank-signature-body p {
  margin: 0;
  padding: 0;
}
.user-avatar {
  margin-right: 5px !important;
  vertical-align: middle;
}
.search-btn {
  color: #fff !important;
  background-color: #409eff !important;
  border-color: #409eff !important;
}
</style>

