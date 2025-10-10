package com.dkd.manage.service.impl;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

import cn.hutool.core.bean.BeanUtil;
import com.dkd.common.constant.MessageConstants;
import com.dkd.common.exception.ServiceException;
import com.dkd.common.utils.DateUtils;
import com.dkd.common.utils.SecurityUtils;
import com.dkd.manage.domain.VendingMachine;
import com.dkd.manage.domain.vo.NodeVo;
import com.dkd.manage.mapper.VendingMachineMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dkd.manage.mapper.NodeMapper;
import com.dkd.manage.domain.Node;
import com.dkd.manage.service.INodeService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 点位管理Service业务层处理
 * 
 * @author luo
 * @date 2025-09-14
 */
@Service
@Slf4j
public class NodeServiceImpl implements INodeService 
{
    @Autowired
    private NodeMapper nodeMapper;

    @Autowired
    private VendingMachineMapper vendingMachineMapper;

    /**
     * 查询点位管理
     * 
     * @param id 点位管理主键
     * @return 点位管理
     */
    @Override
    public Node selectNodeById(Long id)
    {
        return nodeMapper.selectNodeById(id);
    }

    /**
     * 查询点位管理列表
     * 
     * @param node 点位管理
     * @return 点位管理
     */
    @Override
    public List<Node> selectNodeList(Node node)
    {
        return nodeMapper.selectNodeList(node);
    }

    /**
     * 新增点位管理
     * 
     * @param node 点位管理
     * @return 结果
     */
    @Override
    public int insertNode(Node node)
    {
        node.setCreateBy(SecurityUtils.getUsername());
        return nodeMapper.insertNode(node);
    }

    /**
     * 修改点位管理
     * 
     * @param node 点位管理
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateNode(Node node)
    {
        int updateNode = nodeMapper.updateNode(node);
        if (updateNode < 0){
            throw new ServiceException(MessageConstants.NODE_UPDATE_ERROR);
        }
        // 同步更新设备管理的地址、区域、合作商
        VendingMachine vendingMachine = new VendingMachine();
        BeanUtil.copyProperties(node,vendingMachine,"id");
        log.info("同步更新设备管理地址、区域、合作商:{}",vendingMachine);
        // 同步详细地址,点位id
        vendingMachine.setAddr(node.getAddressDetail());
        vendingMachine.setNodeId(node.getId());
        Integer updateVendingMachineRes = vendingMachineMapper.updateVendingMachineByNodeId(vendingMachine);
        if (updateVendingMachineRes < 0){
            throw new ServiceException(MessageConstants.UPDATE_VM_FAIL);
        }
        return updateNode;
    }

    /**
     * 批量删除点位管理
     * 
     * @param ids 需要删除的点位管理主键
     * @return 结果
     */
    @Override
    public int deleteNodeByIds(Long[] ids)
    {
        // 删除点位之前判断点位是否存在
        List<Node> nodeList = nodeMapper.selectBatchNodeById(ids);
        if (CollectionUtils.isEmpty(nodeList)){
            throw new ServiceException(MessageConstants.DELETE_NODE_NOT_EXIST);
        }
        // 拼接点位名称
        String nodeNames = nodeList.stream().map(Node::getNodeName).collect(Collectors.joining("、"));
        // 存在，判断设备里是否关联此点位，如果有则不能删除
        List<VendingMachine> vendingMachineList = vendingMachineMapper.selectBatchVendingMachineByNodeId(ids);
        if (CollectionUtils.isNotEmpty(vendingMachineList)){
            String innerCodes = vendingMachineList.stream().map(VendingMachine::getInnerCode).collect(Collectors.joining("、"));
            throw new ServiceException(MessageFormat.format(MessageConstants.NODE_HAVE_VM,nodeNames,innerCodes));
        }
        return nodeMapper.deleteNodeByIds(ids);
    }

    /**
     * 删除点位管理信息
     * 
     * @param id 点位管理主键
     * @return 结果
     */
    @Override
    public int deleteNodeById(Long id)
    {
        return nodeMapper.deleteNodeById(id);
    }

    @Override
    public List<NodeVo> selectNodeListVo(Node node) {
        return nodeMapper.selectNodeListVo(node);
    }

    @Override
    public List<NodeVo> selectMachineByNodeId(Long id) {
        return nodeMapper.selectMachineByNodeId(id);
    }
}
