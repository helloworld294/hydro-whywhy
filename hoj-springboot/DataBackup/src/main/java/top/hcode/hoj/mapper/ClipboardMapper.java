package top.hcode.hoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.entity.user.Clipboard;

/**
 * <p>
 * 云剪贴板 Mapper 接口
 * </p>
 *
 * @author whysblog
 * @since 2025-11-21
 */
@Mapper
@Repository
public interface ClipboardMapper extends BaseMapper<Clipboard> {
}

