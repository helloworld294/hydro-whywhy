package top.hcode.hoj.manager.oj;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.dao.user.ClipboardEntityService;
import top.hcode.hoj.pojo.dto.ClipboardDTO;
import top.hcode.hoj.pojo.entity.user.Clipboard;
import top.hcode.hoj.pojo.vo.ClipboardVO;
import top.hcode.hoj.shiro.AccountProfile;
import top.hcode.hoj.validator.CommonValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * 云剪贴板管理器
 *
 * @author whysblog
 * @since 2025-11-21
 */
@Component
public class ClipboardManager {

    @Autowired
    private ClipboardEntityService clipboardEntityService;

    @Autowired
    private CommonValidator commonValidator;

    /**
     * 保存剪贴板内容
     */
    public ClipboardVO saveClipboard(ClipboardDTO clipboardDTO) throws StatusFailException, StatusForbiddenException {
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        if (userRolesVo == null) {
            throw new StatusForbiddenException("请先登录！");
        }

        // 验证内容长度
        commonValidator.validateContentLength(clipboardDTO.getContent(), "剪贴板内容", 100000);
        if (clipboardDTO.getTitle() != null) {
            commonValidator.validateContentLength(clipboardDTO.getTitle(), "标题", 100);
        }

        Clipboard clipboard = new Clipboard();
        clipboard.setUid(userRolesVo.getUid());
        clipboard.setContent(clipboardDTO.getContent());
        clipboard.setTitle(clipboardDTO.getTitle() != null && !clipboardDTO.getTitle().trim().isEmpty() 
            ? clipboardDTO.getTitle() : "未命名");

        boolean isOk = clipboardEntityService.save(clipboard);
        if (!isOk) {
            throw new StatusFailException("保存失败！");
        }

        ClipboardVO vo = new ClipboardVO();
        vo.setId(clipboard.getId());
        vo.setContent(clipboard.getContent());
        vo.setTitle(clipboard.getTitle());
        vo.setGmtCreate(clipboard.getGmtCreate());
        vo.setGmtModified(clipboard.getGmtModified());
        return vo;
    }

    /**
     * 获取剪贴板列表
     */
    public IPage<ClipboardVO> getClipboardList(int currentPage, int limit) throws StatusFailException, StatusForbiddenException {
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        if (userRolesVo == null) {
            throw new StatusForbiddenException("请先登录！");
        }

        Page<Clipboard> page = new Page<>(currentPage, limit);
        QueryWrapper<Clipboard> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", userRolesVo.getUid());
        queryWrapper.orderByDesc("gmt_modified");

        IPage<Clipboard> clipboardPage = clipboardEntityService.page(page, queryWrapper);

        List<ClipboardVO> voList = new ArrayList<>();
        for (Clipboard clipboard : clipboardPage.getRecords()) {
            ClipboardVO vo = new ClipboardVO();
            vo.setId(clipboard.getId());
            vo.setContent(clipboard.getContent());
            vo.setTitle(clipboard.getTitle());
            vo.setGmtCreate(clipboard.getGmtCreate());
            vo.setGmtModified(clipboard.getGmtModified());
            voList.add(vo);
        }

        Page<ClipboardVO> voPage = new Page<>(currentPage, limit);
        voPage.setRecords(voList);
        voPage.setTotal(clipboardPage.getTotal());
        return voPage;
    }

    /**
     * 获取单个剪贴板内容
     */
    public ClipboardVO getClipboard(Long id) throws StatusFailException, StatusForbiddenException {
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        if (userRolesVo == null) {
            throw new StatusForbiddenException("请先登录！");
        }

        Clipboard clipboard = clipboardEntityService.getById(id);
        if (clipboard == null) {
            throw new StatusFailException("剪贴板不存在！");
        }

        if (!clipboard.getUid().equals(userRolesVo.getUid())) {
            throw new StatusForbiddenException("无权访问！");
        }

        ClipboardVO vo = new ClipboardVO();
        vo.setId(clipboard.getId());
        vo.setContent(clipboard.getContent());
        vo.setTitle(clipboard.getTitle());
        vo.setGmtCreate(clipboard.getGmtCreate());
        vo.setGmtModified(clipboard.getGmtModified());
        return vo;
    }

    /**
     * 更新剪贴板内容
     */
    public ClipboardVO updateClipboard(Long id, ClipboardDTO clipboardDTO) throws StatusFailException, StatusForbiddenException {
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        if (userRolesVo == null) {
            throw new StatusForbiddenException("请先登录！");
        }

        Clipboard clipboard = clipboardEntityService.getById(id);
        if (clipboard == null) {
            throw new StatusFailException("剪贴板不存在！");
        }

        if (!clipboard.getUid().equals(userRolesVo.getUid())) {
            throw new StatusForbiddenException("无权修改！");
        }

        // 验证内容长度
        commonValidator.validateContentLength(clipboardDTO.getContent(), "剪贴板内容", 100000);
        if (clipboardDTO.getTitle() != null) {
            commonValidator.validateContentLength(clipboardDTO.getTitle(), "标题", 100);
        }

        clipboard.setContent(clipboardDTO.getContent());
        clipboard.setTitle(clipboardDTO.getTitle() != null && !clipboardDTO.getTitle().trim().isEmpty() 
            ? clipboardDTO.getTitle() : "未命名");

        boolean isOk = clipboardEntityService.updateById(clipboard);
        if (!isOk) {
            throw new StatusFailException("更新失败！");
        }

        ClipboardVO vo = new ClipboardVO();
        vo.setId(clipboard.getId());
        vo.setContent(clipboard.getContent());
        vo.setTitle(clipboard.getTitle());
        vo.setGmtCreate(clipboard.getGmtCreate());
        vo.setGmtModified(clipboard.getGmtModified());
        return vo;
    }

    /**
     * 删除剪贴板
     */
    public void deleteClipboard(Long id) throws StatusFailException, StatusForbiddenException {
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        if (userRolesVo == null) {
            throw new StatusForbiddenException("请先登录！");
        }

        Clipboard clipboard = clipboardEntityService.getById(id);
        if (clipboard == null) {
            throw new StatusFailException("剪贴板不存在！");
        }

        if (!clipboard.getUid().equals(userRolesVo.getUid())) {
            throw new StatusForbiddenException("无权删除！");
        }

        boolean isOk = clipboardEntityService.removeById(id);
        if (!isOk) {
            throw new StatusFailException("删除失败！");
        }
    }
}

