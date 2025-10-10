package com.dkd.manage.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.dkd.common.utils.DateUtils;
import com.dkd.manage.domain.dto.ChannelConfigDto;
import com.dkd.manage.domain.dto.ChannelSkuDto;
import com.dkd.manage.domain.vo.ChannelVO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dkd.manage.mapper.ChannelMapper;
import com.dkd.manage.domain.Channel;
import com.dkd.manage.service.IChannelService;

/**
 * 售货机货道Service业务层处理
 * 
 * @author luo
 * @date 2025-09-18
 */
@Service
public class ChannelServiceImpl implements IChannelService 
{
    @Autowired
    private ChannelMapper channelMapper;

    /**
     * 查询售货机货道
     * 
     * @param id 售货机货道主键
     * @return 售货机货道
     */
    @Override
    public Channel selectChannelById(Long id)
    {
        return channelMapper.selectChannelById(id);
    }

    /**
     * 查询售货机货道列表
     * 
     * @param channel 售货机货道
     * @return 售货机货道
     */
    @Override
    public List<Channel> selectChannelList(Channel channel)
    {
        return channelMapper.selectChannelList(channel);
    }

    /**
     * 新增售货机货道
     * 
     * @param channel 售货机货道
     * @return 结果
     */
    @Override
    public int insertChannel(Channel channel)
    {
        channel.setCreateTime(DateUtils.getNowDate());
        channel.setUpdateTime(DateUtils.getNowDate());
        return channelMapper.insertChannel(channel);
    }

    /**
     * 修改售货机货道
     * 
     * @param channel 售货机货道
     * @return 结果
     */
    @Override
    public int updateChannel(Channel channel)
    {
        return channelMapper.updateChannel(channel);
    }

    /**
     * 批量删除售货机货道
     * 
     * @param ids 需要删除的售货机货道主键
     * @return 结果
     */
    @Override
    public int deleteChannelByIds(Long[] ids)
    {
        return channelMapper.deleteChannelByIds(ids);
    }

    /**
     * 删除售货机货道信息
     * 
     * @param id 售货机货道主键
     * @return 结果
     */
    @Override
    public int deleteChannelById(Long id)
    {
        return channelMapper.deleteChannelById(id);
    }

    @Override
    public List<ChannelVO> selectChannelListByInnerCode(String innerCode) {
        return channelMapper.selectChannelListByInnerCode(innerCode);
    }

    @Override
    public int updateChannelConfig(ChannelConfigDto channelConfigDto) {
        // 1. 收集所有需要查询的条件
        List<ChannelSkuDto> channelDtoList = channelConfigDto.getChannelList();
        if (CollectionUtils.isEmpty(channelDtoList)) {
            return 0;
        }

        // 2. 批量查询所有货道信息
        List<Channel> channels = channelMapper.batchGetChannelInfo(channelDtoList);

        // 3. 转换为 Map 方便查找
        Map<String, Channel> channelMap = channels.stream()
                .collect(Collectors.toMap(
                        c -> c.getInnerCode() + "_" + c.getChannelCode(),
                        c -> c
                ));

        // 4. 构建更新列表
        List<Channel> updateList = new ArrayList<>();
        Date now = DateUtils.getNowDate();
        for (ChannelSkuDto dto : channelDtoList) {
            String key = dto.getInnerCode() + "_" + dto.getChannelCode();
            Channel channel = channelMap.get(key);
            if (channel != null) {
                channel.setSkuId(dto.getSkuId());
                channel.setUpdateTime(now);
                updateList.add(channel);
            }
        }

        // 5. 批量更新
        if (CollectionUtils.isEmpty(updateList)) {
            return 0;
        }

        return channelMapper.batchUpdateChannel(updateList);
    }


}
