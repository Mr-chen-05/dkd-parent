package com.dkd.manage.mapper;

import java.util.List;
import com.dkd.manage.domain.Channel;
import com.dkd.manage.domain.dto.ChannelSkuDto;
import com.dkd.manage.domain.vo.ChannelVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.security.core.parameters.P;

/**
 * 售货机货道Mapper接口
 * 
 * @author luo
 * @date 2025-09-18
 */
public interface ChannelMapper 
{
    /**
     * 查询售货机货道
     * 
     * @param id 售货机货道主键
     * @return 售货机货道
     */
    public Channel selectChannelById(Long id);

    /**
     * 查询售货机货道列表
     * 
     * @param channel 售货机货道
     * @return 售货机货道集合
     */
    public List<Channel> selectChannelList(Channel channel);

    /**
     * 新增售货机货道
     * 
     * @param channel 售货机货道
     * @return 结果
     */
    public int insertChannel(Channel channel);

    /**
     * 修改售货机货道
     * 
     * @param channel 售货机货道
     * @return 结果
     */
    public int updateChannel(Channel channel);

    /**
     * 删除售货机货道
     * 
     * @param id 售货机货道主键
     * @return 结果
     */
    public int deleteChannelById(Long id);

    /**
     * 批量删除售货机货道
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteChannelByIds(Long[] ids);
    /**
     * 批量新增售货机货道
     * @param channelList
     * @return
     */
    Integer batchInsertChannel(@Param("channelList") List<Channel> channelList);

    /**
     * 
     * @param id
     * @return
     */
    @Select("select * from channel where vm_id = #{id}")
    List<Channel> selectChannelByVmId(@Param("id") Long id);

    List<Channel> selectChannelByVmIds(@Param("vmIds") List<Long> vmIds);

    int updateMaxCapacityByVmIds(@Param("vmIds") List<Long> vmIds,@Param("newMaxCapacity") Long newMaxCapacity);

    /**
     * 根据设备id批量删除设备对应的货道信息
     * @param ids
     * @return
     */
    int deleteChannelByVmIds(@Param("ids") Long[] ids);

    /**
     * 根据商品id批量查询货道信息
     * @param skuIds
     * @return
     */
    List<Channel> selectChannelBySkuIds(@Param("skuIds") Long[] skuIds);

    List<ChannelVO> selectChannelListByInnerCode(@Param("innerCode") String innerCode);

    List<Channel> batchGetChannelInfo(@Param("channelDtoList") List<ChannelSkuDto> channelDtoList);

    int batchUpdateChannel(@Param("updateList") List<Channel> updateList);
}
