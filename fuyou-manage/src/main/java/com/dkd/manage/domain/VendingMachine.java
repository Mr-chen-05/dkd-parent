package com.dkd.manage.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.dkd.common.annotation.Excel;
import com.dkd.common.core.domain.BaseEntity;

/**
 * 设备管理对象 tb_vending_machine
 * 
 * @author luo
 * @date 2025-09-18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class VendingMachine extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键，设备唯一ID */
    private Long id;

    /** 设备编号 */
    @Excel(name = "设备编号")
    private String innerCode;

    /** 设备最大容量（总货道数） */
    private Long channelMaxCapacity;

    /** 点位ID */
    private Long nodeId;

    /** 详细地址 */
    @Excel(name = "详细地址")
    private String addr;

    /** 上次补货时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastSupplyTime;

    /** 商圈类型 */
    private Long businessType;

    /** 区域ID */
    private Long regionId;

    /** 合作商 */
    @Excel(name = "合作商")
    private Long partnerId;

    /** 设备型号 */
    @Excel(name = "设备型号")
    private Long vmTypeId;

    /** 设备状态 */
    @Excel(name = "设备状态")
    private Long vmStatus;

    /** 运行状态JSON字符串 */
    private String runningStatus;

    /** 经度 */
    private Long longitudes;

    /** 纬度 */
    private Long latitude;

    /** 客户端连接ID，用于EMQ认证 */
    private String clientId;

    /** 策略ID */
    private Long policyId;


}
