package com.dkd.manage.service.impl;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.dkd.common.constant.MessageConstants;
import com.dkd.common.exception.ServiceException;
import com.dkd.common.utils.DateUtils;
import com.dkd.manage.domain.Sku;
import com.dkd.manage.mapper.SkuMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dkd.manage.mapper.SkuClassMapper;
import com.dkd.manage.domain.SkuClass;
import com.dkd.manage.service.ISkuClassService;

/**
 * 商品管理Service业务层处理
 *
 * @author luo
 * @date 2025-09-22
 */
@Service
public class SkuClassServiceImpl implements ISkuClassService {
    @Autowired
    private SkuClassMapper skuClassMapper;

    @Autowired
    private SkuMapper skuMapper;

    /**
     * 查询商品管理
     *
     * @param classId 商品管理主键
     * @return 商品管理
     */
    @Override
    public SkuClass selectSkuClassByClassId(Long classId) {
        return skuClassMapper.selectSkuClassByClassId(classId);
    }

    /**
     * 查询商品管理列表
     *
     * @param skuClass 商品管理
     * @return 商品管理
     */
    @Override
    public List<SkuClass> selectSkuClassList(SkuClass skuClass) {
        return skuClassMapper.selectSkuClassList(skuClass);
    }

    /**
     * 新增商品管理
     *
     * @param skuClass 商品管理
     * @return 结果
     */
    @Override
    public int insertSkuClass(SkuClass skuClass) {
        skuClass.setCreateTime(DateUtils.getNowDate());
        return skuClassMapper.insertSkuClass(skuClass);
    }

    /**
     * 修改商品管理
     *
     * @param skuClass 商品管理
     * @return 结果
     */
    @Override
    public int updateSkuClass(SkuClass skuClass) {
        skuClass.setUpdateTime(DateUtils.getNowDate());
        return skuClassMapper.updateSkuClass(skuClass);
    }

    /**
     * 批量删除商品管理
     *
     * @param classIds 需要删除的商品管理主键
     * @return 结果
     */
    @Override
    public int deleteSkuClassByClassIds(Long[] classIds) {
        // 判断是否有该类型
        List<SkuClass> skuClasses = skuClassMapper.selectBatchSkuClassByClassId(classIds);
        if (CollectionUtils.isEmpty(skuClasses)) {
            throw new ServiceException(MessageConstants.DELETE_SKU_CLASS_NOT_EXIST);
        }

        // 根据商品类型id批量查询商品信息
        List<Sku> skuList = skuMapper.selectBatchSkuByclassIds(classIds);

        // 如果没有关联商品，直接删除
        if (CollectionUtils.isEmpty(skuList)) {
            return skuClassMapper.deleteSkuClassByClassIds(classIds);
        }

        // 按商品类型分组
        Map<Long, List<Sku>> skuMapByClassId = skuList.stream()
                .collect(Collectors.groupingBy(Sku::getClassId));

        // 构建错误消息
        StringBuilder errorMessage = new StringBuilder();
        for (SkuClass skuClass : skuClasses) {
            List<Sku> skus = skuMapByClassId.get(skuClass.getClassId());
            if (CollectionUtils.isNotEmpty(skus)) {
                String skuNames = skus.stream()
                        .map(Sku::getSkuName)
                        .collect(Collectors.joining("、"));
                // 使用消息常量格式
                errorMessage.append(MessageFormat.format(
                        MessageConstants.DELETE_SKU_CLASS_NOT_EMPTY,
                        skuClass.getClassName(),
                        skuNames
                )).append("\n");
            }
        }

        // 如果有商品类型下存在商品，则抛出异常
        if (errorMessage.length() > 0) {
            throw new ServiceException(errorMessage.toString());
        }

        return skuClassMapper.deleteSkuClassByClassIds(classIds);
    }


    /**
     * 删除商品管理信息
     *
     * @param classId 商品管理主键
     * @return 结果
     */
    @Override
    public int deleteSkuClassByClassId(Long classId) {
        return skuClassMapper.deleteSkuClassByClassId(classId);
    }
}
