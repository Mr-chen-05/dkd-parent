package com.dkd.manage.service;

import java.util.List;
import com.dkd.manage.domain.SkuClass;

/**
 * 商品管理Service接口
 * 
 * @author luo
 * @date 2025-09-22
 */
public interface ISkuClassService 
{
    /**
     * 查询商品管理
     * 
     * @param classId 商品管理主键
     * @return 商品管理
     */
    public SkuClass selectSkuClassByClassId(Long classId);

    /**
     * 查询商品管理列表
     * 
     * @param skuClass 商品管理
     * @return 商品管理集合
     */
    public List<SkuClass> selectSkuClassList(SkuClass skuClass);

    /**
     * 新增商品管理
     * 
     * @param skuClass 商品管理
     * @return 结果
     */
    public int insertSkuClass(SkuClass skuClass);

    /**
     * 修改商品管理
     * 
     * @param skuClass 商品管理
     * @return 结果
     */
    public int updateSkuClass(SkuClass skuClass);

    /**
     * 批量删除商品管理
     * 
     * @param classIds 需要删除的商品管理主键集合
     * @return 结果
     */
    public int deleteSkuClassByClassIds(Long[] classIds);

    /**
     * 删除商品管理信息
     * 
     * @param classId 商品管理主键
     * @return 结果
     */
    public int deleteSkuClassByClassId(Long classId);
}
