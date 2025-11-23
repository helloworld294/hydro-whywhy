<template>
  <div>
    <div class="container" v-loading="loading">
      <div class="title-article" style="text-align: left">
        <h1 class="title" id="sharetitle">
          <span>{{ clipboard.title || $t('m.Untitled') }}</span>
        </h1>
        <div class="title-msg">
          <span class="c999" style="padding: 0 6px">
            <i class="fa fa-clock-o"> {{ $t('m.Release_Time') }}：</i>
            <el-tooltip :content="clipboard.gmtModified | localtime" placement="top">
              <span>{{ clipboard.gmtModified | fromNow }}</span>
            </el-tooltip>
          </span>

          <a @click="copyToClipboard(clipboard.content)" class="copy-btn" :title="$t('m.Copy')">
            <i class="el-icon-document-copy"></i>
            <span>{{ $t('m.Copy') }}</span>
          </a>
          <a
            @click="editClipboard"
            class="edit-btn"
            :title="$t('m.Edit')"
            v-if="isAuthenticated"
          >
            <i class="el-icon-edit-outline"></i>
            <span>{{ $t('m.Edit') }}</span>
          </a>
          <a
            @click="deleteClipboard"
            class="delete-btn"
            :title="$t('m.Delete')"
            v-if="isAuthenticated"
          >
            <i class="el-icon-delete"></i>
            <span>{{ $t('m.Delete') }}</span>
          </a>
        </div>
      </div>

      <el-card class="content-card">
        <div class="clipboard-content">
          <Markdown :isAvoidXss="true" :content="clipboard.content"></Markdown>
        </div>
      </el-card>
    </div>

    <!-- 编辑对话框 -->
    <el-dialog
      :title="$t('m.Edit')"
      :visible.sync="showEditDialog"
      width="70%"
      @close="resetForm"
    >
      <el-form :model="form" label-width="80px">
        <el-form-item :label="$t('m.Title')">
          <el-input v-model="form.title" :placeholder="$t('m.Title_Placeholder')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('m.Content')" required>
          <Editor :value.sync="form.content"></Editor>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="showEditDialog = false">{{ $t('m.Cancel') }}</el-button>
        <el-button type="primary" @click="submitClipboard" :loading="saving">
          {{ $t('m.Save') }}
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import api from '@/common/api';
import myMessage from '@/common/message';
import { mapGetters } from 'vuex';
import Markdown from '@/components/oj/common/Markdown';
const Editor = () => import('@/components/admin/Editor.vue');

export default {
  components: {
    Editor,
    Markdown,
  },
  data() {
    return {
      loading: false,
      saving: false,
      clipboard: {
        id: null,
        title: '',
        content: '',
        gmtModified: null,
      },
      showEditDialog: false,
      form: {
        title: '',
        content: '',
      },
    };
  },
  mounted() {
    this.getClipboard();
  },
  computed: {
    ...mapGetters(['isAuthenticated', 'userInfo']),
  },
  methods: {
    getClipboard() {
      const id = this.$route.params.clipboardID;
      if (!id) {
        myMessage.error('参数错误');
        this.$router.push({ name: 'ClipboardList' });
        return;
      }

      this.loading = true;
      api
        .getClipboard(id)
        .then((res) => {
          this.clipboard = res.data.data;
          this.form.title = this.clipboard.title;
          this.form.content = this.clipboard.content;
          this.loading = false;
        })
        .catch((err) => {
          this.loading = false;
          myMessage.error(err.data.msg || '获取失败');
          this.$router.push({ name: 'ClipboardList' });
        });
    },
    editClipboard() {
      this.showEditDialog = true;
    },
    submitClipboard() {
      if (!this.form.content || this.form.content.trim() === '') {
        myMessage.error(this.$i18n.t('m.Content_Required'));
        return;
      }

      this.saving = true;
      const data = {
        title: this.form.title,
        content: this.form.content,
      };

      api
        .updateClipboard(this.clipboard.id, data)
        .then((res) => {
          myMessage.success(this.$i18n.t('m.Update_Successfully'));
          this.showEditDialog = false;
          this.getClipboard();
          this.saving = false;
        })
        .catch((err) => {
          this.saving = false;
          myMessage.error(err.data.msg || '更新失败');
        });
    },
    deleteClipboard() {
      this.$confirm(this.$i18n.t('m.Delete_Tips'), 'Tips', {
        confirmButtonText: this.$i18n.t('m.OK'),
        cancelButtonText: this.$i18n.t('m.Cancel'),
        type: 'warning',
      })
        .then(() => {
          api
            .deleteClipboard(this.clipboard.id)
            .then((res) => {
              myMessage.success(this.$i18n.t('m.Delete_Successfully'));
              this.$router.push({ name: 'ClipboardList' });
            })
            .catch((err) => {
              myMessage.error(err.data.msg || '删除失败');
            });
        })
        .catch(() => {});
    },
    copyToClipboard(text) {
      // 移除HTML标签，只复制纯文本
      const textContent = text.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, ' ');
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(textContent).then(
          () => {
            myMessage.success(this.$i18n.t('m.Copy_Successfully'));
          },
          () => {
            this.fallbackCopyTextToClipboard(textContent);
          }
        );
      } else {
        this.fallbackCopyTextToClipboard(textContent);
      }
    },
    fallbackCopyTextToClipboard(text) {
      const textArea = document.createElement('textarea');
      textArea.value = text;
      textArea.style.position = 'fixed';
      textArea.style.left = '-999999px';
      textArea.style.top = '-999999px';
      document.body.appendChild(textArea);
      textArea.focus();
      textArea.select();
      try {
        document.execCommand('copy');
        myMessage.success(this.$i18n.t('m.Copy_Successfully'));
      } catch (err) {
        myMessage.error('复制失败');
      }
      document.body.removeChild(textArea);
    },
    resetForm() {
      this.form.title = this.clipboard.title;
      this.form.content = this.clipboard.content;
    },
  },
};
</script>

<style scoped>
.title-article .title {
  margin: 0;
  padding: 0;
  font-size: 24px;
  font-weight: 500;
  color: #34495e;
}
.title-msg {
  margin-top: 15px;
  font-size: 12px;
  color: #999 !important;
}
.title-msg a {
  color: #999;
  text-decoration: none;
  margin-left: 15px;
}
.copy-btn:hover,
.edit-btn:hover {
  color: #409eff;
}
.delete-btn:hover {
  color: #f56c6c;
}
.content-card {
  margin-top: 20px;
  text-align: left;
}
.clipboard-content {
  padding: 20px;
  min-height: 200px;
  word-wrap: break-word;
  white-space: pre-wrap;
}
</style>

