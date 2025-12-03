package com.dkd.manage.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;

import com.dkd.common.utils.StringUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.dkd.common.annotation.Log;
import com.dkd.common.core.controller.BaseController;
import com.dkd.common.core.domain.AjaxResult;
import com.dkd.common.enums.BusinessType;
import com.dkd.manage.domain.Sku;
import com.dkd.manage.service.ISkuService;
import com.dkd.common.utils.poi.ExcelUtil;
import com.dkd.common.core.page.TableDataInfo;
import org.springframework.web.multipart.MultipartFile;

/**
 * 商品管理Controller
 *
 * @author luo
 * @date 2025-09-22
 */
@RestController
@RequestMapping("/manage/sku")
public class SkuController extends BaseController {
    @Autowired
    private ISkuService skuService;

    /**
     * 查询商品管理列表
     */
    @PreAuthorize("@ss.hasPermi('manage:sku:list')")
    @GetMapping("/list")
    public TableDataInfo list(Sku sku) {
        startPage();
        List<Sku> list = skuService.selectSkuList(sku);
        return getDataTable(list);
    }

    /**
     * 导出商品管理列表
     */
    @PreAuthorize("@ss.hasPermi('manage:sku:export')")
    @Log(title = "商品管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Sku sku, @RequestParam(required = false) List<Long> skuIds) {
        List<Sku> list;
        // 检查是否传递了选中的商品ID
        if (CollectionUtils.isNotEmpty(skuIds)) {
            // 按选中的商品ID导出
            System.out.println("接收到skuIds: " + skuIds); // 调试日志
            list = skuService.selectSkuListByIds(skuIds);
            System.out.println("查询结果数量: " + list.size()); // 调试日志
        } else {
            // 按查询条件导出全部
            list = skuService.selectSkuList(sku);
        }

        ExcelUtil<Sku> util = new ExcelUtil<Sku>(Sku.class);
        util.exportEasyExcel(response, list, "商品管理数据");
    }

    /**
     * 导入商品管理列表
     */
    @PreAuthorize("@ss.hasPermi('manage:sku:add')")
    @Log(title = "商品管理", businessType = BusinessType.IMPORT)
    @PostMapping("/import")
    public AjaxResult excelImport(MultipartFile file) throws Exception {
        ExcelUtil<Sku> util = new ExcelUtil<Sku>(Sku.class);
        List<Sku> skuList = util.importEasyExcel(file.getInputStream());
        return toAjax(skuService.insertSkuBatch(skuList));
    }


    /**
     * 获取商品管理详细信息
     */

    @PreAuthorize("@ss.hasPermi('manage:sku:query')")
    @GetMapping(value = "/{skuId}")
    public AjaxResult getInfo(@PathVariable("skuId") Long skuId) {
        return success(skuService.selectSkuBySkuId(skuId));
    }

    /**
     * 新增商品管理
     */
    @PreAuthorize("@ss.hasPermi('manage:sku:add')")
    @Log(title = "商品管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Sku sku) {
        return toAjax(skuService.insertSku(sku));
    }

    /**
     * 修改商品管理
     */
    @PreAuthorize("@ss.hasPermi('manage:sku:edit')")
    @Log(title = "商品管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Sku sku) {
        return toAjax(skuService.updateSku(sku));
    }

    /**
     * 删除商品管理
     */
    @PreAuthorize("@ss.hasPermi('manage:sku:remove')")
    @Log(title = "商品管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{skuIds}")
    public AjaxResult remove(@PathVariable Long[] skuIds) {
        return toAjax(skuService.deleteSkuBySkuIds(skuIds));
    }
}
