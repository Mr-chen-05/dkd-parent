package com.dkd.manage.mapper;

import com.dkd.manage.domain.vo.VendingMachineVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 统计Mapper接口
 * 
 * @author luo
 * @date 2025-11-25
 */
public interface StatisticsMapper {

    /**
     * 按工单类型统计工单数据（维修/运营）
     * 
     * @param start 开始时间
     * @param end   结束时间
     * @return 统计结果列表，按工单类型分组
     */
    List<Map<String, Object>> selectTaskStatsByType(@Param("start") Date start, @Param("end") Date end);

    /**
     * 销售统计（订单量、销售额）
     * 
     * @param beginTime 开始日期
     * @param endTime   结束日期
     * @return 统计结果（order_count, order_amount）
     */
    Map<String, Object> selectSalesStats(@Param("beginTime") LocalDate beginTime, @Param("endTime") LocalDate endTime);

    /**
     * 商品销售排行（TopN）
     * 
     * @param beginTime 开始日期
     * @param endTime   结束日期
     * @param limit     限制数量
     * @return 商品销售排行列表
     */
    List<Map<String, Object>> selectSkuSaleRank(@Param("beginTime") LocalDate beginTime,
            @Param("endTime") LocalDate endTime,
            @Param("limit") Integer limit);

    /**
     * 销售趋势（按日或按月）
     * 
     * @param beginTime   开始日期
     * @param endTime     结束日期
     * @param granularity 粒度（day/month）
     * @return 销售趋势数据
     */
    List<Map<String, Object>> selectSalesTrend(@Param("beginTime") LocalDate beginTime,
            @Param("endTime") LocalDate endTime,
            @Param("granularity") String granularity);

    /**
     * 按产品类别的销售趋势（多序列）
     * 
     * @param beginTime   开始日期
     * @param endTime     结束日期
     * @param granularity 粒度（day/month）
     * @return 按类别分组的销售趋势数据
     */
    List<Map<String, Object>> selectSalesTrendByClass(@Param("beginTime") LocalDate beginTime,
            @Param("endTime") LocalDate endTime,
            @Param("granularity") String granularity);

    /**
     * 销售区域分布（TopN）
     * 
     * @param beginTime 开始日期
     * @param endTime   结束日期
     * @param topN      TopN数量
     * @return 区域销售分布数据
     */
    List<Map<String, Object>> selectSalesRegionDistribution(@Param("beginTime") LocalDate beginTime,
            @Param("endTime") LocalDate endTime,
            @Param("topN") Integer topN);

    /**
     * 销售区域分布（按月）
     * 
     * @param beginTime 开始日期
     * @param endTime   结束日期
     * @return 按月的销售数据
     */
    List<Map<String, Object>> selectSalesRegionDistributionByMonth(@Param("beginTime") LocalDate beginTime,
            @Param("endTime") LocalDate endTime);

    /**
     * 合作商点位统计（TopN）
     * 
     * @param topN TopN数量
     * @return 合作商点位统计数据
     */
    List<Map<String, Object>> selectPartnerNodeStats(@Param("topN") Integer topN);

    /**
     * 合作商点位汇总统计
     * 
     * @return 汇总数据（total_nodes, partner_count）
     */
    Map<String, Object> selectPartnerNodeSummary();

    /**
     * 异常设备列表（包含工单状态）
     * 
     * @param limit 限制数量
     * @return 异常设备列表
     */
    List<VendingMachineVO> selectAbnormalEquipmentWithTaskStatus(@Param("limit") Integer limit);
}
