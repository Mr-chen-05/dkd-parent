package com.dkd.manage.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
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
import com.dkd.manage.domain.SkuClass;
import com.dkd.manage.service.ISkuClassService;
import com.dkd.common.utils.poi.ExcelUtil;
import com.dkd.common.core.page.TableDataInfo;

/**
 * 商品管理Controller
 * 
 * @author luo
 * @date 2025-09-22
 */
@RestController
@RequestMapping("/manage/skuType")
public class SkuClassController extends BaseController
{
    @Autowired
    private ISkuClassService skuClassService;

    /**
     * 查询商品管理列表
     */
    @PreAuthorize("@ss.hasPermi('manage:skuType:list')")
    @GetMapping("/list")
    public TableDataInfo list(SkuClass skuClass)
    {
        startPage();
        List<SkuClass> list = skuClassService.selectSkuClassList(skuClass);
        return getDataTable(list);
    }

    /**
     * 导出商品管理列表
     */
    @PreAuthorize("@ss.hasPermi('manage:skuType:export')")
    @Log(title = "商品管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SkuClass skuClass)
    {
        List<SkuClass> list = skuClassService.selectSkuClassList(skuClass);
        ExcelUtil<SkuClass> util = new ExcelUtil<SkuClass>(SkuClass.class);
        util.exportEasyExcel(response, list, "商品管理数据");
    }

    /**
     * 获取商品管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('manage:skuType:query')")
    @GetMapping(value = "/{classId}")
    public AjaxResult getInfo(@PathVariable("classId") Long classId)
    {
        return success(skuClassService.selectSkuClassByClassId(classId));
    }

    /**
     * 新增商品管理
     */
    @PreAuthorize("@ss.hasPermi('manage:skuType:add')")
    @Log(title = "商品管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SkuClass skuClass)
    {
        return toAjax(skuClassService.insertSkuClass(skuClass));
    }

    /**
     * 修改商品管理
     */
    @PreAuthorize("@ss.hasPermi('manage:skuType:edit')")
    @Log(title = "商品管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SkuClass skuClass)
    {
        return toAjax(skuClassService.updateSkuClass(skuClass));
    }

    /**
     * 删除商品管理
     */
    @PreAuthorize("@ss.hasPermi('manage:skuType:remove')")
    @Log(title = "商品管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{classIds}")
    public AjaxResult remove(@PathVariable Long[] classIds)
    {
        return toAjax(skuClassService.deleteSkuClassByClassIds(classIds));
    }
}
