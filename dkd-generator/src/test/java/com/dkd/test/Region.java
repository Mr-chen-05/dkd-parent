package com.dkd.test;

import com.dkd.common.annotation.Excel;
import com.dkd.common.core.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 区域管理对象 tb_region
 * 
 * @author luo
 * @date 2025-09-14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Region extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键，自增 */
    private Long id;

    /** 区域名称 */
    @Excel(name = "区域名称")
    private String regionName;


}
