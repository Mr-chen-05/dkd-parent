package com.dkd.manage.mapper;

import java.util.List;
import com.dkd.manage.domain.VendingMachine;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 设备管理Mapper接口
 * 
 * @author luo
 * @date 2025-09-18
 */
public interface VendingMachineMapper 
{
    /**
     * 查询设备管理
     * 
     * @param id 设备管理主键
     * @return 设备管理
     */
    public VendingMachine selectVendingMachineById(Long id);

    /**
     * 查询设备管理列表
     * 
     * @param vendingMachine 设备管理
     * @return 设备管理集合
     */
    public List<VendingMachine> selectVendingMachineList(VendingMachine vendingMachine);

    /**
     * 新增设备管理
     * 
     * @param vendingMachine 设备管理
     * @return 结果
     */
    public int insertVendingMachine(VendingMachine vendingMachine);

    /**
     * 修改设备管理
     * 
     * @param vendingMachine 设备管理
     * @return 结果
     */
    public int updateVendingMachine(VendingMachine vendingMachine);

    /**
     * 删除设备管理
     * 
     * @param id 设备管理主键
     * @return 结果
     */
    public int deleteVendingMachineById(Long id);

    /**
     * 批量删除设备管理
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteVendingMachineByIds(Long[] ids);

    /**
     * 根据点位id更新设备信息
     * @param vendingMachine
     * @return
     */
    Integer updateVendingMachineByNodeId(VendingMachine vendingMachine);
    /**
     * 根据设备类型id批量查询设备信息
     * @param ids
     * @return
     */
    List<VendingMachine> selectBatchVendingMachineByVmTypeId(@Param("ids") Long[] ids);

    /**
     * 根据设备id批量查询设备信息
     * @param ids
     * @return
     */
    List<VendingMachine> selectBatchVendingMachineByIds(@Param("ids") Long[] ids);

    /**
     * 根据点位id批量查询设备信息
     * @param ids
     * @return
     */
    List<VendingMachine> selectBatchVendingMachineByNodeId(@Param("ids") Long[] ids);

    /**
     * 根据设备类型ID批量更新设备的货道最大容量
     * @param vmTypeId 设备类型ID
     * @param newMaxCapacity 新的最大容量
     * @return 影响的记录数
     */
    int updateChannelMaxCapacityByVmTypeId(@Param("vmTypeId") Long vmTypeId, @Param("newMaxCapacity") Long newMaxCapacity);

    /**
     * 根据设备编号查询设备
     * @param innerCode
     * @return
     */
    @Select("select * from tb_vending_machine where inner_code=#{innerCode}")
    VendingMachine selectVendingMachineByInnerCode(@Param("innerCode") String innerCode);
}
