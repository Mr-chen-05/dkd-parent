package com.dkd.manage.service.impl;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.dkd.common.constant.MessageConstants;
import com.dkd.common.exception.ServiceException;
import com.dkd.common.utils.DateUtils;
import com.dkd.manage.domain.Channel;
import com.dkd.manage.mapper.ChannelMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dkd.manage.mapper.SkuMapper;
import com.dkd.manage.domain.Sku;
import com.dkd.manage.service.ISkuService;

/**
 * 商品管理Service业务层处理
 *
 * @author luo
 * @date 2025-09-22
 */
@Service
public class SkuServiceImpl implements ISkuService {
    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private ChannelMapper channelMapper;

    /**
     * 查询商品管理
     *
     * @param skuId 商品管理主键
     * @return 商品管理
     */
    @Override
    public Sku selectSkuBySkuId(Long skuId) {
        return skuMapper.selectSkuBySkuId(skuId);
    }

    /**
     * 查询商品管理列表
     *
     * @param sku 商品管理
     * @return 商品管理
     */
    @Override
    public List<Sku> selectSkuList(Sku sku) {
        return skuMapper.selectSkuList(sku);
    }

    /**
     * 新增商品管理
     *
     * @param sku 商品管理
     * @return 结果
     */
    @Override
    public int insertSku(Sku sku) {
        sku.setCreateTime(DateUtils.getNowDate());
        return skuMapper.insertSku(sku);
    }

    @Override
    public int insertSkuBatch(List<Sku> skuList) {
        return skuMapper.insertSkuBatch(skuList);
    }

    /**
     * 修改商品管理
     *
     * @param sku 商品管理
     * @return 结果
     */
    @Override
    public int updateSku(Sku sku) {
        sku.setUpdateTime(DateUtils.getNowDate());
        return skuMapper.updateSku(sku);
    }

    /**
     * 批量删除商品管理
     *
     * @param skuIds 需要删除的商品管理主键
     * @return 结果
     */
    @Override
    public int deleteSkuBySkuIds(Long[] skuIds) {
        // 查询判断删除的商品是否存在
        List<Sku> skuList = skuMapper.selectBatchSkuBySkuId(skuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(MessageConstants.DELETE_SKU_NOT_EXIST);
        }

        // 查询商品是否跟设备的货道关联
        List<Channel> channelList = channelMapper.selectChannelBySkuIds(skuIds);
        // 为空直接删除
        if (CollectionUtils.isEmpty(channelList)) {
            return skuMapper.deleteSkuBySkuIds(skuIds);
        }

        // 按商品ID分组货道信息
        Map<Long, List<Channel>> channelBySkuId = channelList.stream().collect(Collectors.groupingBy(Channel::getSkuId));

        // 构建错误信息
        StringBuilder errorMsg = new StringBuilder();

        for (Sku sku : skuList) {
            List<Channel> channels = channelBySkuId.get(sku.getSkuId());
            if (CollectionUtils.isNotEmpty(channels)) {
                // 按设备编号分组
                Map<String, List<Channel>> channelsByInnerCode = channels.stream().collect(Collectors.groupingBy(Channel::getInnerCode));

                // 为每个设备构建错误信息
                for (Map.Entry<String, List<Channel>> entry : channelsByInnerCode.entrySet()) {
                    String innerCode = entry.getKey();
                    List<Channel> icChannels = entry.getValue();

                    String channelCodes = icChannels.stream().map(Channel::getChannelCode).collect(Collectors.joining(","));

                    errorMsg.append(MessageFormat.format(MessageConstants.DELETE_SKU, innerCode, channelCodes, sku.getSkuName())).append("\n");
                }
            }
        }

        if (errorMsg.length() > 0) {
            throw new ServiceException(errorMsg.toString().trim()); // 去除最后的换行符
        }

        return skuMapper.deleteSkuBySkuIds(skuIds);
    }

    /**
     * 删除商品管理信息
     *
     * @param skuId 商品管理主键
     * @return 结果
     */
    @Override
    public int deleteSkuBySkuId(Long skuId) {
        return skuMapper.deleteSkuBySkuId(skuId);
    }

    @Override
    public List<Sku> selectSkuListByIds(List<Long> skuIds) {
        if (CollectionUtils.isNotEmpty(skuIds)) {
            // 把skuIds转换为Long[]
            Long[] ids = skuIds.stream().map(Long::longValue).toArray(Long[]::new);
            return skuMapper.selectBatchSkuBySkuId(ids);
        }
       return null;
    }
}
