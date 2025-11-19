package com.dkd.manage.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.dkd.common.core.domain.R;
import com.dkd.manage.domain.dto.OrderDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.dkd.common.annotation.Log;
import com.dkd.common.core.controller.BaseController;
import com.dkd.common.core.domain.AjaxResult;
import com.dkd.common.enums.BusinessType;
import com.dkd.manage.domain.Order;
import com.dkd.manage.service.IOrderService;
import com.dkd.common.utils.poi.ExcelUtil;
import com.dkd.common.core.page.TableDataInfo;

/**
 * 订单Controller
 *
 * @author luo
 * @date 2025-10-14
 */
@Api(tags = "订单管理")
@RestController
@RequestMapping("/manage/order")
public class OrderController extends BaseController {
    @Autowired
    private IOrderService orderService;

    /**
     * 查询订单列表
     */
    @ApiOperation("查询订单列表")
    @PreAuthorize("@ss.hasPermi('manage:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(OrderDto orderDto) {
        startPage();
        List<Order> list = orderService.selectOrderListVO(orderDto);
        return getDataTable(list);
    }

    /**
     * 导出订单列表
     */
    @ApiOperation("导出订单列表")
    @PreAuthorize("@ss.hasPermi('manage:order:export')")
    @Log(title = "订单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, OrderDto orderDto) {
        List<Order> list = orderService.selectOrderListVO(orderDto);
        ExcelUtil<Order> util = new ExcelUtil<Order>(Order. class);
        util.exportEasyExcel(response, list, "订单数据");
    }

    /**
     * 获取订单详细信息
     */
    @ApiOperation("获取订单详细信息")
    @PreAuthorize("@ss.hasPermi('manage:order:query')")
    @ApiImplicitParam(name = "id", value = "订单id", required = true, dataType = "Long", paramType = "path")
    @GetMapping(value = "/{id}")
    public R<Order> getInfo(@PathVariable("id") Long id) {
        return R.ok(orderService.selectOrderById(id));
    }

    /**
     * 新增订单
     */
    @ApiOperation("新增订单")
    @PreAuthorize("@ss.hasPermi('manage:order:add')")
    @Log(title = "订单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Order order) {
        return toAjax(orderService.insertOrder(order));
    }

    /**
     * 修改订单
     */
    @ApiOperation("修改订单")
    @PreAuthorize("@ss.hasPermi('manage:order:edit')")
    @Log(title = "订单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Order order) {
        return toAjax(orderService.updateOrder(order));
    }

    /**
     * 删除订单
     */
    @ApiOperation("删除订单")
    @PreAuthorize("@ss.hasPermi('manage:order:remove')")
    @Log(title = "订单", businessType = BusinessType.DELETE)
    @ApiImplicitParam(name = "ids", value = "主键", required = true, dataType = "Long[]", paramType = "path")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(orderService.deleteOrderByIds(ids));
    }
}
