package top.hcode.hoj.service.oj.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.manager.oj.ClipboardManager;
import top.hcode.hoj.pojo.dto.ClipboardDTO;
import top.hcode.hoj.pojo.vo.ClipboardVO;
import top.hcode.hoj.service.oj.ClipboardService;

import javax.annotation.Resource;

/**
 * 云剪贴板服务实现
 *
 * @author whysblog
 * @since 2025-11-21
 */
@Service
public class ClipboardServiceImpl implements ClipboardService {

    @Resource
    private ClipboardManager clipboardManager;

    @Override
    public CommonResult<ClipboardVO> saveClipboard(ClipboardDTO clipboardDTO) {
        try {
            return CommonResult.successResponse(clipboardManager.saveClipboard(clipboardDTO));
        } catch (StatusFailException | StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<IPage<ClipboardVO>> getClipboardList(int currentPage, int limit) {
        try {
            return CommonResult.successResponse(clipboardManager.getClipboardList(currentPage, limit));
        } catch (StatusFailException | StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<ClipboardVO> getClipboard(Long id) {
        try {
            return CommonResult.successResponse(clipboardManager.getClipboard(id));
        } catch (StatusFailException | StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<ClipboardVO> updateClipboard(Long id, ClipboardDTO clipboardDTO) {
        try {
            return CommonResult.successResponse(clipboardManager.updateClipboard(id, clipboardDTO));
        } catch (StatusFailException | StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<Void> deleteClipboard(Long id) {
        try {
            clipboardManager.deleteClipboard(id);
            return CommonResult.successResponse();
        } catch (StatusFailException | StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }
}

