package com.dkd.manage.service.impl;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import cn.hutool.core.bean.BeanUtil;
import com.dkd.common.constant.DkdContants;
import com.dkd.common.constant.MessageConstants;
import com.dkd.common.exception.ServiceException;
import com.dkd.common.utils.DateUtils;
import com.dkd.common.utils.uuid.UUIDUtils;
import com.dkd.manage.domain.*;
import com.dkd.manage.domain.vo.VendingMachineVO;
import com.dkd.manage.mapper.ChannelMapper;
import com.dkd.manage.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dkd.manage.mapper.VendingMachineMapper;
import org.springframework.transaction.annotation.Transactional;

/**
 * 设备管理Service业务层处理
 *
 * @author luo
 * @date 2025-09-18
 */
@Service
@Slf4j
public class VendingMachineServiceImpl implements IVendingMachineService {
    @Autowired
    private VendingMachineMapper vendingMachineMapper;

    @Autowired
    private IVmTypeService vmTypeService;

    @Autowired
    private INodeService nodeService;

    @Autowired
    private IChannelService channelService;

    @Autowired
    private ChannelMapper channelMapper;

    @Autowired
    private IRegionService regionService;

    @Autowired
    private IPartnerService partnerService;

    /**
     * 查询设备管理
     *
     * @param id 设备管理主键
     * @return 设备管理
     */
    @Override
    public VendingMachine selectVendingMachineById(Long id) {
        return vendingMachineMapper.selectVendingMachineById(id);
    }

    @Override
    public VendingMachineVO getVendingMachineDetail(Long id) {
        VendingMachine vm = vendingMachineMapper.selectVendingMachineById(id);
        if (vm == null) {
            return null;
        }
        VendingMachineVO vo = new VendingMachineVO();
        BeanUtil.copyProperties(vm, vo);

        if (vm.getNodeId() != null) {
            Node node = nodeService.selectNodeById(vm.getNodeId());
            if (node != null) {
                vo.setNodeName(node.getNodeName());
            }
        }
        if (vm.getRegionId() != null) {
            Region region = regionService.selectRegionById(vm.getRegionId());
            if (region != null) {
                vo.setRegionName(region.getRegionName());
            }
        }
        if (vm.getVmTypeId() != null) {
            VmType vmType = vmTypeService.selectVmTypeById(vm.getVmTypeId());
            if (vmType != null) {
                vo.setVmTypeName(vmType.getName());
            }
        }
        if (vm.getPartnerId() != null) {
            Partner partner = partnerService.selectPartnerById(vm.getPartnerId());
            if (partner != null) {
                vo.setPartnerName(partner.getPartnerName());
            }
        }

        if (vm.getRunningStatus() != null && !vm.getRunningStatus().isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> obj = mapper.readValue(vm.getRunningStatus(), Map.class);
                vo.setRunningStatusObj(obj);
            } catch (Exception ignored) {}
        }
        return vo;
    }

    /**
     * 查询设备管理列表
     *
     * @param vendingMachine 设备管理
     * @return 设备管理
     */
    @Override
    public List<VendingMachine> selectVendingMachineList(VendingMachine vendingMachine) {
        return vendingMachineMapper.selectVendingMachineList(vendingMachine);
    }

    /**
     * 新增设备管理
     *
     * @param vendingMachine 设备管理
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertVendingMachine(VendingMachine vendingMachine) {
        // 1.新增设备
        // 1-1 生成8位唯一标识，补充设备编号
        String innerCode = UUIDUtils.getUUID();
        vendingMachine.setInnerCode(innerCode);
        // 1-2 查询售货机类型表，补充设备容量
        VmType vmType = vmTypeService.selectVmTypeById(vendingMachine.getVmTypeId());
        vendingMachine.setChannelMaxCapacity(vmType.getChannelMaxCapacity());
        // 1-3 查询点位表，补充区域，点位，合作商等信息
        Node node = nodeService.selectNodeById(vendingMachine.getNodeId());
        // 商圈类型、区域、合作商
        BeanUtil.copyProperties(node, vendingMachine, "id", "updateTime", "createTime");
        // 设备地址
        vendingMachine.setAddr(node.getAddressDetail());
        // 1-4 设备状态
        // 0-表示未投放
        vendingMachine.setVmStatus(DkdContants.VM_STATUS_NODEPLOY);
        int result = vendingMachineMapper.insertVendingMachine(vendingMachine);
        if (result < 0) {
            throw new ServiceException(MessageConstants.ADD_EQUIPMENT_FAIL);
        }
        // 2.新增货道
        // 2-1.货道集合
        List<Channel> channelList = new ArrayList<>();
        for (int i = 1; i <= vmType.getVmRow(); i++) {
            for (int j = 1; j <= vmType.getVmCol(); j++) {
                Channel channel = new Channel();
                channel.setChannelCode(i + "-" + j);
                channel.setVmId(vendingMachine.getId());
                channel.setInnerCode(innerCode);
                channel.setMaxCapacity(vmType.getChannelMaxCapacity());
                channelList.add(channel);
            }
        }
        Integer insertChannelRes = channelMapper.batchInsertChannel(channelList);
        log.info("货道数量：{}", insertChannelRes);
        if (insertChannelRes != channelList.size()) {
            throw new ServiceException(MessageConstants.ADD_CHANNEL_FAIL);
        }
        return result;
    }

    /**
     * 修改设备管理
     *
     * @param vendingMachine 设备管理
     * @return 结果
     */
    @Override
    public int updateVendingMachine(VendingMachine vendingMachine) {
        //如果点位id为null，更新策略时不需要同步
        if (vendingMachine.getNodeId() != null){
            // 根据点位id查询点位信息
            Node node = nodeService.selectNodeById(vendingMachine.getNodeId());
            // 同步区域、合作商、商圈类型
            BeanUtil.copyProperties(node, vendingMachine, "id", "createTime", "updateTime");
            // 设备地址
            vendingMachine.setAddr(node.getAddressDetail());
        }
        int updVmRes = vendingMachineMapper.updateVendingMachine(vendingMachine);
        if (updVmRes < 0) {
            throw new ServiceException(MessageConstants.UPDATE_VM_FAIL);
        }
        return updVmRes;
    }

    /**
     * 批量删除设备管理
     *
     * @param ids 需要删除的设备管理主键
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteVendingMachineByIds(Long[] ids) {
        // 查询设备是否存在
        List<VendingMachine> vendingMachineList = vendingMachineMapper.selectBatchVendingMachineByIds(ids);
        if (CollectionUtils.isEmpty(vendingMachineList)){
            throw new ServiceException(MessageConstants.DELETE_VM_NOT_EXIST);
        }
        //  判断设备状态是否是撤机，不是把设备编号拼接起来
        List<VendingMachine> vendingMachines = vendingMachineList.stream().filter(vendingMachine -> !vendingMachine.getVmStatus().equals(DkdContants.VM_STATUS_REVOKE)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(vendingMachines)){
            String innerCodes = vendingMachines.stream().map(VendingMachine::getInnerCode).collect(Collectors.joining("、"));
            throw new ServiceException(MessageFormat.format(MessageConstants.VM_STATUS_NOT_REVOKE, innerCodes));
        }
        // 批量删除设备
        int vmRes = vendingMachineMapper.deleteVendingMachineByIds(ids);
        if (vmRes < 0) {
            throw new ServiceException(MessageConstants.DELETE_VM_FAIL);
        }
        // 批量删除设备对应的货道信息
        int vcRes = channelMapper.deleteChannelByVmIds(ids);
        if (vcRes < 0) {
            throw new ServiceException(MessageConstants.DELETE_CHANNEL_FAIL);
        }
        return vmRes;
    }

    /**
     * 删除设备管理信息
     *
     * @param id 设备管理主键
     * @return 结果
     */
    @Override
    public int deleteVendingMachineById(Long id) {
        return vendingMachineMapper.deleteVendingMachineById(id);
    }

}
