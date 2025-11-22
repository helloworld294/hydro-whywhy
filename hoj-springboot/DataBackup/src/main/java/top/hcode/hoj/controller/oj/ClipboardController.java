package top.hcode.hoj.controller.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.ClipboardDTO;
import top.hcode.hoj.pojo.vo.ClipboardVO;
import top.hcode.hoj.service.oj.ClipboardService;

/**
 * 云剪贴板控制器
 *
 * @author whysblog
 * @since 2025-11-21
 */
@RestController
@RequestMapping("/api")
public class ClipboardController {

    @Autowired
    private ClipboardService clipboardService;

    /**
     * 保存剪贴板内容
     */
    @PostMapping("/clipboard")
    @RequiresAuthentication
    public CommonResult<ClipboardVO> saveClipboard(@RequestBody ClipboardDTO clipboardDTO) {
        return clipboardService.saveClipboard(clipboardDTO);
    }

    /**
     * 获取剪贴板列表
     */
    @GetMapping("/clipboard")
    @RequiresAuthentication
    public CommonResult<IPage<ClipboardVO>> getClipboardList(
            @RequestParam(value = "currentPage", required = false, defaultValue = "1") int currentPage,
            @RequestParam(value = "limit", required = false, defaultValue = "20") int limit) {
        return clipboardService.getClipboardList(currentPage, limit);
    }

    /**
     * 获取单个剪贴板内容
     */
    @GetMapping("/clipboard/{id}")
    @RequiresAuthentication
    public CommonResult<ClipboardVO> getClipboard(@PathVariable("id") Long id) {
        return clipboardService.getClipboard(id);
    }

    /**
     * 更新剪贴板内容
     */
    @PutMapping("/clipboard/{id}")
    @RequiresAuthentication
    public CommonResult<ClipboardVO> updateClipboard(
            @PathVariable("id") Long id,
            @RequestBody ClipboardDTO clipboardDTO) {
        return clipboardService.updateClipboard(id, clipboardDTO);
    }

    /**
     * 删除剪贴板
     */
    @DeleteMapping("/clipboard/{id}")
    @RequiresAuthentication
    public CommonResult<Void> deleteClipboard(@PathVariable("id") Long id) {
        return clipboardService.deleteClipboard(id);
    }
}

