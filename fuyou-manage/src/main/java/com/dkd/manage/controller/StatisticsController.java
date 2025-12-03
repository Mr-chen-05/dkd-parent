package com.dkd.manage.controller;

import com.dkd.common.core.controller.BaseController;
import com.dkd.common.core.domain.AjaxResult;
import com.dkd.common.utils.DateUtils;
import com.dkd.manage.domain.Order;
import com.dkd.manage.domain.Task;
import com.dkd.manage.domain.VendingMachine;
import com.dkd.manage.service.IStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计Controller
 *
 * @author luo
 * @date 2025-09-18
 */
@RestController
@RequestMapping("/manage/statistics")
public class StatisticsController extends BaseController {

    @Autowired
    private IStatisticsService statisticsService;

    /**
     * 工单统计（维修/运营），返回两组统计数据
     * 参数：start、end（yyyy-MM-dd HH:mm:ss）
     */
    @GetMapping("/userTaskStats")
    public AjaxResult userTaskStats(@RequestParam(required = false) String start,
                                    @RequestParam(required = false) String end) {
        Date startDate = DateUtils.parseDate(start);
        Date endDate = DateUtils.parseDate(end);
        return success(statisticsService.userTaskStats(startDate, endDate));
    }

    /**
     * 销售统计（订单量、销售额）
     * 参数：start、end（yyyy-MM-dd）
     */
    @GetMapping("/salesStats")
    public AjaxResult salesStats(@RequestParam(required = false) String start,
                                 @RequestParam(required = false) String end) {
        LocalDate s = start != null ? LocalDate.parse(start.substring(0, 10)) : null;
        LocalDate e = end != null ? LocalDate.parse(end.substring(0, 10)) : null;
        return success(statisticsService.salesStats(s, e));
    }

    /**
     * 商品热榜（TopN）
     * 参数：start、end（yyyy-MM-dd）、limit
     */
    @GetMapping("/skuSaleRank")
    public AjaxResult skuSaleRank(@RequestParam(required = false) String start,
                                  @RequestParam(required = false) String end,
                                  @RequestParam(defaultValue = "10") Integer limit) {
        LocalDate s = start != null ? LocalDate.parse(start.substring(0, 10)) : null;
        LocalDate e = end != null ? LocalDate.parse(end.substring(0, 10)) : null;
        return success(statisticsService.skuSaleRank(s, e, limit));
    }

    /**
     * 销售趋势（按日或按月）
     * 参数：start、end（yyyy-MM-dd HH:mm:ss）、granularity=day|month
     */
    @GetMapping("/salesTrend")
    public AjaxResult salesTrend(@RequestParam String start,
                                 @RequestParam String end,
                                 @RequestParam(defaultValue = "day") String granularity) {
        LocalDateTime s = LocalDateTime.parse(start, DateTimeFormatter.ofPattern(DateUtils.YYYY_MM_DD_HH_MM_SS));
        LocalDateTime e = LocalDateTime.parse(end, DateTimeFormatter.ofPattern(DateUtils.YYYY_MM_DD_HH_MM_SS));
        return success(statisticsService.salesTrend(s, e, granularity));
    }

    /**
     * 按产品类别的销售趋势（多序列）
     */
    @GetMapping("/salesTrendByClass")
    public AjaxResult salesTrendByClass(@RequestParam String start,
                                        @RequestParam String end,
                                        @RequestParam(defaultValue = "day") String granularity) {
        LocalDateTime s = LocalDateTime.parse(start, DateTimeFormatter.ofPattern(DateUtils.YYYY_MM_DD_HH_MM_SS));
        LocalDateTime e = LocalDateTime.parse(end, DateTimeFormatter.ofPattern(DateUtils.YYYY_MM_DD_HH_MM_SS));
        return success(statisticsService.salesTrendByClass(s, e, granularity));
    }

    /**
     * 销售区域分布（TopN）
     * 参数：start、end（yyyy-MM-dd）、topN
     */
    @GetMapping("/salesRegionDistribution")
    public AjaxResult salesRegionDistribution(@RequestParam(required = false) String start,
                                              @RequestParam(required = false) String end,
                                              @RequestParam(defaultValue = "10") Integer topN,
                                              @RequestParam(required = false, defaultValue = "none") String granularity) {
        LocalDate s = start != null ? LocalDate.parse(start.substring(0, 10)) : null;
        LocalDate e = end != null ? LocalDate.parse(end.substring(0, 10)) : null;
        return success(statisticsService.salesRegionDistribution(s, e, topN, granularity));
    }

    /**
     * 合作商点位TopN及汇总
     * 参数：topN
     */
    @GetMapping("/partnerNodeTop")
    public AjaxResult partnerNodeTop(@RequestParam(defaultValue = "5") Integer topN) {
        return success(statisticsService.partnerNodeTop(topN));
    }

    /**
     * 异常设备列表（非运营状态）
     * 参数：limit
     */
    @GetMapping("/abnormalEquipmentList")
    public AjaxResult abnormalEquipmentList(@RequestParam(defaultValue = "10") Integer limit) {
        return success(statisticsService.abnormalEquipmentList(limit));
    }
}
