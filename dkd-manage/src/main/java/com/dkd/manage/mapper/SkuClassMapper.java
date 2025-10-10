package com.dkd.manage.mapper;

import java.util.List;
import com.dkd.manage.domain.SkuClass;
import org.apache.ibatis.annotations.Param;

/**
 * 商品管理Mapper接口
 * 
 * @author luo
 * @date 2025-09-22
 */
public interface SkuClassMapper 
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
     * 删除商品管理
     * 
     * @param classId 商品管理主键
     * @return 结果
     */
    public int deleteSkuClassByClassId(Long classId);

    /**
     * 批量删除商品管理
     * 
     * @param classIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSkuClassByClassIds(Long[] classIds);
    /**
     * 根据商品类型id批量查询商品类型信息
     * @param classIds
     * @return
     */
    List<SkuClass> selectBatchSkuClassByClassId(@Param("classIds") Long[] classIds);
}
