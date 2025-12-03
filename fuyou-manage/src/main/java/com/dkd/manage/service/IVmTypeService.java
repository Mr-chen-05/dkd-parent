package com.dkd.manage.service;

import java.util.List;
import com.dkd.manage.domain.VmType;

/**
 * 设备类型管理Service接口
 * 
 * @author luo
 * @date 2025-09-18
 */
public interface IVmTypeService 
{
    /**
     * 查询设备类型管理
     * 
     * @param id 设备类型管理主键
     * @return 设备类型管理
     */
    public VmType selectVmTypeById(Long id);

    /**
     * 查询设备类型管理列表
     * 
     * @param vmType 设备类型管理
     * @return 设备类型管理集合
     */
    public List<VmType> selectVmTypeList(VmType vmType);

    /**
     * 新增设备类型管理
     * 
     * @param vmType 设备类型管理
     * @return 结果
     */
    public int insertVmType(VmType vmType);

    /**
     * 修改设备类型管理
     * 
     * @param vmType 设备类型管理
     * @return 结果
     */
    public int updateVmType(VmType vmType);

    /**
     * 批量删除设备类型管理
     * 
     * @param ids 需要删除的设备类型管理主键集合
     * @return 结果
     */
    public int deleteVmTypeByIds(Long[] ids);

    /**
     * 删除设备类型管理信息
     * 
     * @param id 设备类型管理主键
     * @return 结果
     */
    public int deleteVmTypeById(Long id);

    void updateChannelsMaxCapacity(Long id, Long channelMaxCapacity);

    void syncChannelsForVmTypeOnDecrease(Long vmRow, Long vmCol, Long vmRow1, Long vmCol1, Long id);

    void syncChannelsForVmTypeOnIncrease(Long vmRow, Long vmCol, Long vmRow1, Long vmCol1, Long id);
}
