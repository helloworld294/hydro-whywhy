package top.hcode.hoj.pojo.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 云剪贴板VO
 *
 * @author whysblog
 * @since 2025-11-21
 */
@Data
@Accessors(chain = true)
public class ClipboardVO {

    private Long id;

    private String content;

    private String title;

    private Date gmtCreate;

    private Date gmtModified;
}

