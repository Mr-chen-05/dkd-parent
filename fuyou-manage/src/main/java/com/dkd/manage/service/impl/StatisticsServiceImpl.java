package com.dkd.manage.service.impl;

import com.dkd.manage.domain.vo.VendingMachineVO;
import com.dkd.manage.mapper.StatisticsMapper;
import com.dkd.manage.service.IStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计服务实现（优化版本）
 * 
 * @author luo
 * @date 2025-11-25
 */
@Service
public class StatisticsServiceImpl implements IStatisticsService {

    @Autowired
    private StatisticsMapper statisticsMapper;

    /**
     * 工单统计（维修/运营）
     * 优化：使用SQL聚合统计，避免N+1查询和Java内存计算
     */
    @Override
    public List<Map<String, Object>> userTaskStats(Date start, Date end) {
        // 直接从数据库获取按类型分组的统计结果
        List<Map<String, Object>> stats = statisticsMapper.selectTaskStatsByType(start, end);

        // 初始化结果Map，确保维修和运营两种类型都存在
        Map<String, Map<String, Object>> resultMap = new HashMap<>();
        resultMap.put("1", createEmptyStats(true)); // 维修工单
        resultMap.put("2", createEmptyStats(false)); // 运营工单

        // 填充实际统计数据
        for (Map<String, Object> stat : stats) {
            String taskType = String.valueOf(stat.get("task_type"));
            if ("1".equals(taskType) || "2".equals(taskType)) {
                Map<String, Object> statData = new LinkedHashMap<>();
                statData.put("total", stat.get("total"));
                statData.put("completedTotal", stat.get("completed_total"));
                statData.put("cancelTotal", stat.get("cancel_total"));
                statData.put("progressTotal", stat.get("progress_total"));
                statData.put("workerCount", stat.get("worker_count"));
                statData.put("date", null);
                statData.put("repair", "1".equals(taskType));
                resultMap.put(taskType, statData);
            }
        }

        // 返回固定顺序：运营、维修
        return Arrays.asList(
                resultMap.get("2"), // 运营
                resultMap.get("1") // 维修
        );
    }

    /**
     * 创建空统计数据
     */
    private Map<String, Object> createEmptyStats(boolean isRepair) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("total", 0);
        map.put("completedTotal", 0);
        map.put("cancelTotal", 0);
        map.put("progressTotal", 0);
        map.put("workerCount", 0);
        map.put("date", null);
        map.put("repair", isRepair);
        return map;
    }

    /**
     * 销售统计（订单量、销售额）
     * 优化：使用SQL聚合，直接返回统计结果
     */
    @Override
    public Map<String, Object> salesStats(LocalDate start, LocalDate end) {
        return statisticsMapper.selectSalesStats(start, end);
    }

    /**
     * 商品销售排行（TopN）
     * 优化：SQL完成分组、聚合、排序、限制，避免全量加载
     */
    @Override
    public List<Map<String, Object>> skuSaleRank(LocalDate start, LocalDate end, Integer limit) {
        return statisticsMapper.selectSkuSaleRank(start, end, limit);
    }

    /**
     * 销售趋势（按日或按月）
     * 优化：SQL完成时间格式化和分组聚合
     */
    @Override
    public Map<String, Object> salesTrend(LocalDateTime start, LocalDateTime end, String granularity) {
        LocalDate startDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();

        // 从数据库获取实际数据
        List<Map<String, Object>> trendData = statisticsMapper.selectSalesTrend(startDate, endDate, granularity);

        // 构建数据Map，方便快速查找
        Map<String, Long> dataMap = new HashMap<>();
        for (Map<String, Object> item : trendData) {
            String dateKey = (String) item.get("date_key");
            Long amount = ((Number) item.get("amount")).longValue();
            dataMap.put(dateKey, amount);
        }

        // 生成完整的时间轴（包括没有数据的日期，填充0）
        String format = "month".equalsIgnoreCase(granularity) ? "yyyy-MM" : "yyyy-MM-dd";
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
        List<String> keys = new ArrayList<>();
        LocalDateTime cursor = start;
        while (!cursor.isAfter(end)) {
            keys.add(dtf.format(cursor));
            cursor = "month".equalsIgnoreCase(granularity) ? cursor.plusMonths(1) : cursor.plusDays(1);
        }

        // 构建序列数据
        List<Long> seriesData = keys.stream()
                .map(k -> dataMap.getOrDefault(k, 0L))
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("xAxisData", keys);
        result.put("seriesData", seriesData);
        result.put("yAxisName", "单位：元");
        return result;
    }

    /**
     * 按产品类别的销售趋势（多序列）
     * 优化：SQL完成二维分组聚合
     */
    @Override
    public Map<String, Object> salesTrendByClass(LocalDateTime start, LocalDateTime end, String granularity) {
        LocalDate startDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();

        // 从数据库获取按类别和日期分组的数据
        List<Map<String, Object>> trendData = statisticsMapper.selectSalesTrendByClass(startDate, endDate, granularity);

        // 按class_id分组
        Map<String, Map<String, Long>> bucketByClass = new LinkedHashMap<>();
        for (Map<String, Object> item : trendData) {
            String classId = String.valueOf(item.get("class_id"));
            String dateKey = (String) item.get("date_key");
            Long amount = ((Number) item.get("amount")).longValue();

            bucketByClass.computeIfAbsent(classId, k -> new LinkedHashMap<>());
            bucketByClass.get(classId).put(dateKey, amount);
        }

        // 生成完整的时间轴
        String format = "month".equalsIgnoreCase(granularity) ? "yyyy-MM" : "yyyy-MM-dd";
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
        List<String> keys = new ArrayList<>();
        LocalDateTime cursor = start;
        while (!cursor.isAfter(end)) {
            keys.add(dtf.format(cursor));
            cursor = "month".equalsIgnoreCase(granularity) ? cursor.plusMonths(1) : cursor.plusDays(1);
        }

        // 构建每个类别的序列数据
        List<Map<String, Object>> series = new ArrayList<>();
        bucketByClass.forEach((cls, map) -> {
            List<Long> data = keys.stream()
                    .map(k -> map.getOrDefault(k, 0L))
                    .collect(Collectors.toList());
            Map<String, Object> s = new LinkedHashMap<>();
            s.put("name", cls);
            s.put("data", data);
            series.add(s);
        });

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("xAxisData", keys);
        result.put("seriesDataByClass", series);
        result.put("yAxisName", "单位：元");
        return result;
    }

    /**
     * 销售区域分布（TopN），支持 granularity=none|month
     * 优化：SQL完成分组、聚合、排序、限制
     */
    @Override
    public Map<String, Object> salesRegionDistribution(LocalDate start, LocalDate end, Integer topN,
            String granularity) {
        // 如果按月统计
        if ("month".equalsIgnoreCase(Optional.ofNullable(granularity).orElse("none"))) {
            List<Map<String, Object>> monthlyData = statisticsMapper.selectSalesRegionDistributionByMonth(start, end);

            // 构建数据Map
            Map<String, Long> dataMap = new HashMap<>();
            for (Map<String, Object> item : monthlyData) {
                String dateKey = (String) item.get("date_key");
                Long amount = ((Number) item.get("amount")).longValue();
                dataMap.put(dateKey, amount);
            }

            // 生成完整的月份轴
            String format = "yyyy-MM";
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
            List<String> keys = new ArrayList<>();
            LocalDate cursor = start;
            while (cursor != null && end != null && !cursor.isAfter(end)) {
                keys.add(cursor.format(dtf));
                cursor = cursor.plusMonths(1);
            }

            List<Long> seriesData = keys.stream()
                    .map(k -> dataMap.getOrDefault(k, 0L))
                    .collect(Collectors.toList());

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("xAxisData", keys);
            result.put("seriesData", seriesData);
            result.put("yAxisName", "单位：元");
            return result;
        }

        // 按区域统计
        List<Map<String, Object>> regionData = statisticsMapper.selectSalesRegionDistribution(start, end, topN);

        List<String> xAxisData = regionData.stream()
                .map(item -> (String) item.get("region_key"))
                .collect(Collectors.toList());
        List<Long> seriesData = regionData.stream()
                .map(item -> ((Number) item.get("amount")).longValue())
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("xAxisData", xAxisData);
        result.put("seriesData", seriesData);
        result.put("yAxisName", "单位：元");
        return result;
    }

    /**
     * 合作商点位TopN及汇总
     * 优化：SQL完成分组统计，避免N+1查询
     */
    @Override
    public Map<String, Object> partnerNodeTop(Integer topN) {
        // 获取TopN统计
        List<Map<String, Object>> stats = statisticsMapper.selectPartnerNodeStats(topN);

        // 构建饼图数据
        List<Map<String, Object>> seriesData = stats.stream().map(item -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", item.get("partner_name"));
            m.put("value", item.get("node_count"));
            return m;
        }).collect(Collectors.toList());

        // 获取汇总数据
        Map<String, Object> summary = statisticsMapper.selectPartnerNodeSummary();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("seriesData", seriesData);
        result.put("totalNodes", summary.get("total_nodes"));
        result.put("partnerCount", summary.get("partner_count"));
        return result;
    }

    /**
     * 异常设备列表（非运营状态），返回包含工单状态的设备信息
     * 优化：单SQL完成设备和工单的关联查询，避免O(n*m)复杂度
     */
    @Override
    public List<VendingMachineVO> abnormalEquipmentList(Integer limit) {
        // 使用优化的SQL查询，一次性获取设备和最新工单状态
        return statisticsMapper.selectAbnormalEquipmentWithTaskStatus(limit);
    }
}