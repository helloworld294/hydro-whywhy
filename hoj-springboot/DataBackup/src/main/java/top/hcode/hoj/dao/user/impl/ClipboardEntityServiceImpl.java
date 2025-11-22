package top.hcode.hoj.dao.user.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.dao.user.ClipboardEntityService;
import top.hcode.hoj.mapper.ClipboardMapper;
import top.hcode.hoj.pojo.entity.user.Clipboard;

/**
 * <p>
 * 云剪贴板服务实现类
 * </p>
 *
 * @author whysblog
 * @since 2025-11-21
 */
@Service
public class ClipboardEntityServiceImpl extends ServiceImpl<ClipboardMapper, Clipboard> implements ClipboardEntityService {
}

