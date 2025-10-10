package com.dkd.manage.mapper;

import java.util.List;
import com.dkd.manage.domain.Sku;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.parameters.P;

/**
 * 商品管理Mapper接口
 * 
 * @author luo
 * @date 2025-09-22
 */
public interface SkuMapper 
{
    /**
     * 查询商品管理
     * 
     * @param skuId 商品管理主键
     * @return 商品管理
     */
    public Sku selectSkuBySkuId(@Param("skuId") Long skuId);

    /**
     * 查询商品管理列表
     * 
     * @param sku 商品管理
     * @return 商品管理集合
     */
    public List<Sku> selectSkuList(Sku sku);

    /**
     * 新增商品管理
     * 
     * @param sku 商品管理
     * @return 结果
     */
    public int insertSku(Sku sku);
    /**
     * 批量新增商品管理
     *
     * @param skuList 商品管理
     * @return 结果
     */
    public int insertSkuBatch(@Param("skuList") List<Sku> skuList);

    /**
     * 修改商品管理
     * 
     * @param sku 商品管理
     * @return 结果
     */
    public int updateSku(Sku sku);

    /**
     * 删除商品管理
     * 
     * @param skuId 商品管理主键
     * @return 结果
     */
    public int deleteSkuBySkuId(Long skuId);

    /**
     * 批量删除商品管理
     * 
     * @param skuIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSkuBySkuIds(Long[] skuIds);

    /**
     * 根据商品类型id批量查询商品信息
     * @param classIds
     * @return
     */
    List<Sku> selectBatchSkuByclassIds(@Param("classIds") Long[] classIds);

    /**
     * 根据商品id批量查询商品信息
     * @param skuIds
     * @return
     */
    List<Sku> selectBatchSkuBySkuId(@Param("skuIds") Long[] skuIds);
}
