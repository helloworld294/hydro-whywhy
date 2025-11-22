package top.hcode.hoj.service.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.ClipboardDTO;
import top.hcode.hoj.pojo.vo.ClipboardVO;

public interface ClipboardService {

    CommonResult<ClipboardVO> saveClipboard(ClipboardDTO clipboardDTO);

    CommonResult<IPage<ClipboardVO>> getClipboardList(int currentPage, int limit);

    CommonResult<ClipboardVO> getClipboard(Long id);

    CommonResult<ClipboardVO> updateClipboard(Long id, ClipboardDTO clipboardDTO);

    CommonResult<Void> deleteClipboard(Long id);
}

