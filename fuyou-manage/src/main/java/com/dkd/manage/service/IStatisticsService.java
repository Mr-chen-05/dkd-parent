package com.dkd.manage.service;

import com.dkd.manage.domain.VendingMachine;
import com.dkd.manage.domain.vo.VendingMachineVO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 统计服务接口
 */
public interface IStatisticsService {

    /**
     * 工单统计（维修/运营）
     */
    List<Map<String, Object>> userTaskStats(Date start, Date end);

    /**
     * 销售统计（订单量、销售额）
     */
    Map<String, Object> salesStats(LocalDate start, LocalDate end);

    /**
     * 商品热榜（TopN）
     */
    List<Map<String, Object>> skuSaleRank(LocalDate start, LocalDate end, Integer limit);

    /**
     * 销售趋势（按日或按月）
     */
    Map<String, Object> salesTrend(LocalDateTime start, LocalDateTime end, String granularity);

    /**
     * 按产品类别的销售趋势（多序列）
     */
    Map<String, Object> salesTrendByClass(LocalDateTime start, LocalDateTime end, String granularity);

    /**
     * 销售区域分布（TopN），支持 granularity=none|month
     */
    Map<String, Object> salesRegionDistribution(LocalDate start, LocalDate end, Integer topN, String granularity);

    /**
     * 合作商点位TopN及汇总
     */
    Map<String, Object> partnerNodeTop(Integer topN);

    /**
     * 异常设备列表（非运营状态），返回包含工单状态的设备信息
     */
    List<VendingMachineVO> abnormalEquipmentList(Integer limit);

}