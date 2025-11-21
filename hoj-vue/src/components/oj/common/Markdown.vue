<template>
  <div
    v-if="isAvoidXss"
    v-dompurify-html="html"
    v-highlight
    v-katex
    class="markdown-body"
  ></div>
  <div
    v-else
    v-html="html"
    v-highlight
    v-katex
    class="markdown-body"
  ></div>
</template>
<script>
export default {
  name: "Markdown",
  props: {
    isAvoidXss: {
      default: false,
      type: Boolean,
    },
    content: {
      require: true,
      type: String,
    },
  },
  data(){
    return{
        pdfLogo: require('@/assets/pdf-logo.svg'),
        videoLogo: require('@/assets/video-logo.svg'),
    }
  },
  computed: {
    html: function () {
      if (this.content == null || this.content == undefined) {
        return "";
      }
      
      // 检查内容是否已经是HTML格式
      if (this.content.trim().startsWith('<') && this.content.trim().endsWith('>')) {
        return this.content;
      }
      
      // 尝试使用全局markdown-it实例
      let res;
      try {
        res = this.$markDown.render(this.content);
      } catch (error) {
        res = this.content; // 如果失败，直接返回原始内容
      }
      
      // 获取pdf链接生成预览模块（外链不内嵌，仅提供打开/下载；同源链接内嵌预览）
      const pdfRegex = /<a.*?href="(.*?\.pdf)".*?>(.*?)<\/a>/gi;
      res = res.replace(pdfRegex, (match, href, text) => {
        let isExternal = false;
        try {
          const u = new URL(href, window.location.origin);
          isExternal = u.origin !== window.location.origin;
        } catch (e) {
          isExternal = false;
        }

        const card = `
        <p></p>
        <file-card>
            <div>
                <img class="pdf-svg" src="${this.pdfLogo}">
            </div>
            <div>
                <h5 class="filename">${text}</h5>
                <p><a href="${href}" target="_blank">Download</a></p>
            </div>
        </file-card>`;

        if (isExternal) {
          // 外链PDF常见设置了 X-Frame-Options/CSP，无法内嵌，直接返回卡片+外链
          return card;
        } else {
          // 同源PDF使用object内嵌预览
          return (
            card +
            `
        <object data="${href}" type="application/pdf" width="100%" height="800px"> 
            <embed src="${href}"> 
            This browser does not support PDFs. Please download the PDF to view it: <a href="${href}" target="_blank">Download PDF</a>.</p> 
            </embed> 
        </object>`
          );
        }
      });
      
      // 获取视频链接生成预览模块 (.mp4, .ts)
      res = res.replace(
        /<a.*?href="(.*?\.(mp4|ts))".*?>(.*?)<\/a>/gi,
        `<p></p>
        <file-card>
            <div>
                <img class="video-svg" src="${this.videoLogo}">
            </div>
            <div>
                <h5 class="filename">$3</h5>
                <p><a href="$1" target="_blank">Download</a></p>
            </div>
        </file-card>
        <video controls width="100%" style="max-height: 600px; margin: 10px 0;">
            <source src="$1" type="video/mp4">
            <source src="$1" type="video/mp2t">
            Your browser does not support the video tag. Please download the video: <a href="$1" target="_blank">Download Video</a>.
        </video>
        `
      );
      return res;
    },
  },
};
</script>
<style>
file-card .pdf-svg {
  padding: 0 !important;
  margin: 0 !important;
  box-shadow: none !important;
}
file-card .video-svg {
  padding: 0 !important;
  margin: 0 !important;
  box-shadow: none !important;
}
file-card {
  margin: 1rem 0;
  display: flex;
  align-items: center;
  max-width: 100%;
  border-radius: 4px;
  transition: 0.2s ease-out 0s;
  color: #7a8e97;
  background: #fff;
  padding: 0.6rem;
  position: relative;
  border: 1px solid rgba(0, 0, 0, 0.15);
}
file-card > div:first-of-type {
  display: flex;
  align-items: center;
  padding-right: 1rem;
  width: 5rem;
  height: 5rem;
  flex-shrink: 0;
  flex-grow: 0;
}
file-card .filename {
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 1.2rem;
  margin-bottom: 0.5rem !important;
  font-family: "Roboto";
  font-weight: 400 !important;
  line-height: 1.2 !important;
  color: #000;
  word-wrap: break-word;
  word-break: break-all;
  white-space: normal !important;
  -webkit-line-clamp: 1;
  display: -webkit-box;
  -webkit-box-orient: vertical;
}
file-card p {
  margin: 0;
  line-height: 1;
  font-family: "Roboto";
}
</style>