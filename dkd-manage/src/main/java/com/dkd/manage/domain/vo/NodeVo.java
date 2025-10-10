package com.dkd.manage.domain.vo;

import cn.hutool.core.date.DateTime;
import com.dkd.common.annotation.Excel;
import com.dkd.common.core.domain.BaseEntity;
import com.dkd.manage.domain.Node;
import com.dkd.manage.domain.Partner;
import com.dkd.manage.domain.Region;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 点位管理对象 tb_node
 *
 * @author luo
 * @date 2025-09-14
 */
@Data
public class NodeVo extends Node {
    private Partner partner;

    private Region region;

    private Integer vmCount;

    private String innerCode;

    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastSupplyTime;
}
