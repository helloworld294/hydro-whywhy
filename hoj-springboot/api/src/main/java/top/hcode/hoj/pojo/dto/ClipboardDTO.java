package top.hcode.hoj.pojo.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 云剪贴板DTO
 *
 * @author whysblog
 * @since 2025-11-21
 */
@Data
@Accessors(chain = true)
public class ClipboardDTO {

    private String content;

    private String title;
}

