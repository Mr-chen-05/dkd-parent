package com.dkd.manage.service.impl;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.dkd.common.constant.MessageConstants;
import com.dkd.common.exception.ServiceException;
import com.dkd.manage.domain.Channel;
import com.dkd.manage.domain.VendingMachine;
import com.dkd.manage.mapper.ChannelMapper;
import com.dkd.manage.mapper.VendingMachineMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dkd.manage.mapper.VmTypeMapper;
import com.dkd.manage.domain.VmType;
import com.dkd.manage.service.IVmTypeService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 设备类型管理Service业务层处理
 * 负责处理设备类型相关的业务逻辑，包括设备类型的增删改查以及货道同步等复杂操作
 *
 * @author luo
 * @date 2025-09-18
 */
@Service
@Slf4j
public class VmTypeServiceImpl implements IVmTypeService {
    @Autowired
    private VmTypeMapper vmTypeMapper;

    @Autowired
    private VendingMachineServiceImpl vendingMachineService;

    @Autowired
    private ChannelMapper channelMapper;

    @Autowired
    private VendingMachineMapper vendingMachineMapper;

    private IVmTypeService vmTypeServiceProxy;

    /**
     * 根据主键查询设备类型信息
     *
     * @param id 设备类型管理主键
     * @return 设备类型管理对象
     */
    @Override
    public VmType selectVmTypeById(Long id) {
        return vmTypeMapper.selectVmTypeById(id);
    }

    /**
     * 查询设备类型列表
     *
     * @param vmType 查询条件封装的对象
     * @return 符合条件的设备类型列表
     */
    @Override
    public List<VmType> selectVmTypeList(VmType vmType) {
        return vmTypeMapper.selectVmTypeList(vmType);
    }

    /**
     * 新增设备类型
     *
     * @param vmType 设备类型信息
     * @return 影响的记录数
     */
    @Override
    public int insertVmType(VmType vmType) {
        return vmTypeMapper.insertVmType(vmType);
    }


    /**
     * 修改设备类型信息
     * 此方法包含复杂的业务逻辑，包括：
     * 1. 参数校验
     * 2. 唯一性校验
     * 3. 行列变化时的货道同步处理
     * 4. 容量变化时的处理
     *
     * @param vmType 设备类型信息
     * @return 影响的记录数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateVmType(VmType vmType) {
        // 参数校验：行列数必须大于0
        if (vmType.getVmRow() <= 0 || vmType.getVmCol() <= 0) {
            throw new ServiceException(MessageConstants.VM_TYPE_ROW_COL_INVALID);
        }

        // 参数校验：行列数不能超过10（业务约束）
        if (vmType.getVmRow() > 10 || vmType.getVmCol() > 10) {
            throw new ServiceException(MessageConstants.VM_TYPE_ROW_COL_EXCEED_LIMIT);
        }

        // 参数校验：货道容量必须大于0
        if (vmType.getChannelMaxCapacity() <= 0) {
            throw new ServiceException(MessageConstants.VM_TYPE_CAPACITY_INVALID);
        }

        // 参数校验：货道容量不能超过1000（业务约束，防止过大）
        if (vmType.getChannelMaxCapacity() > 1000) {
            throw new ServiceException(MessageConstants.VM_TYPE_CAPACITY_EXCEED_LIMIT);
        }

        // 1. 查询原始设备类型信息，用于后续比较和处理
        VmType oldVmType = vmTypeMapper.selectVmTypeById(vmType.getId());
        if (oldVmType == null) {
            throw new ServiceException(MessageConstants.VM_TYPE_NOT_EXIST);
        }

        // 2. 检查型号名称和编码的唯一性（仅在修改时检查）
        // 只有当名称或型号发生变化时才进行唯一性校验
        if (!Objects.equals(vmType.getName(), oldVmType.getName())
                || !Objects.equals(vmType.getModel(), oldVmType.getModel())) {
            checkVmTypeUnique(vmType);
        }

        // 3. 判断各种变化情况，用于后续处理逻辑判断
        boolean isRowColChanged = !Objects.equals(oldVmType.getVmRow(), vmType.getVmRow())
                || !Objects.equals(oldVmType.getVmCol(), vmType.getVmCol());
        boolean isCapacityDecreased = vmType.getChannelMaxCapacity() < oldVmType.getChannelMaxCapacity();
        boolean isCapacityIncreased = vmType.getChannelMaxCapacity() > oldVmType.getChannelMaxCapacity();

        // 获取代理对象
        vmTypeServiceProxy = (IVmTypeService) AopContext.currentProxy();
        // 4. 如果容量减少，需要先检查是否会导致现有库存超过新容量限制
        if (isCapacityDecreased) {
            checkCapacityDecreaseAndCleanup(vmType.getId(), oldVmType.getChannelMaxCapacity(), vmType.getChannelMaxCapacity());
        }

        // 5. 执行更新数据库操作
        int result = vmTypeMapper.updateVmType(vmType);

        if (result > 0) {
            log.info("设备类型[{}]更新成功，名称：{}，型号：{}",
                    vmType.getId(), vmType.getName(), vmType.getModel());

            // 6. 处理行列变化情况
            if (isRowColChanged) {
                log.info("设备类型[{}]行列数由 {}×{} 调整为 {}×{}",
                        vmType.getId(), oldVmType.getVmRow(), oldVmType.getVmCol(),
                        vmType.getVmRow(), vmType.getVmCol());

                // 判断行列是增加还是减少
                boolean isRowIncreased = vmType.getVmRow() > oldVmType.getVmRow();
                boolean isColIncreased = vmType.getVmCol() > oldVmType.getVmCol();
                boolean isRowDecreased = vmType.getVmRow() < oldVmType.getVmRow();
                boolean isColDecreased = vmType.getVmCol() < oldVmType.getVmCol();

                // 先处理减少（可能抛异常）：删除超出新行列范围的货道
                if (isRowDecreased || isColDecreased) {
                    vmTypeServiceProxy.syncChannelsForVmTypeOnDecrease(
                            oldVmType.getVmRow(), oldVmType.getVmCol(),
                            vmType.getVmRow(), vmType.getVmCol(),
                            vmType.getId()
                    );
                }

                // 再处理增加：为新增行列创建新的货道
                if (isRowIncreased || isColIncreased) {
                    vmTypeServiceProxy.syncChannelsForVmTypeOnIncrease(
                            oldVmType.getVmRow(), oldVmType.getVmCol(),
                            vmType.getVmRow(), vmType.getVmCol(),
                            vmType.getId()
                    );
                }
            }

            // 7. 处理容量变化：无论增加还是减少，检查通过后都需要更新
            if (isCapacityIncreased || isCapacityDecreased) {
                vmTypeServiceProxy.updateChannelsMaxCapacity(vmType.getId(), vmType.getChannelMaxCapacity());
                log.info("设备类型[{}]最大容量由 {} 调整为 {}", 
                        vmType.getId(), oldVmType.getChannelMaxCapacity(), vmType.getChannelMaxCapacity());
            }
        }

        return result;
    }

    /**
     * 检查设备类型名称/型号编码的唯一性
     * 防止出现重复的设备类型名称或型号编码
     *
     * @param vmType 待检查的设备类型对象
     */
    private void checkVmTypeUnique(VmType vmType) {
        // 检查名称唯一性
        VmType nameQuery = new VmType();
        nameQuery.setName(vmType.getName());
        List<VmType> sameNameTypes = vmTypeMapper.selectVmTypeList(nameQuery);
        if (sameNameTypes.stream().anyMatch(t -> !t.getId().equals(vmType.getId()))) {
            throw new ServiceException(MessageConstants.VM_TYPE_NAME_EXIST);
        }

        // 检查型号编码唯一性
        VmType modelQuery = new VmType();
        modelQuery.setModel(vmType.getModel());
        List<VmType> sameModelTypes = vmTypeMapper.selectVmTypeList(modelQuery);
        if (sameModelTypes.stream().anyMatch(t -> !t.getId().equals(vmType.getId()))) {
            throw new ServiceException(MessageConstants.VM_TYPE_MODEL_EXIST);
        }
    }

    /**
     * 检查容量减少是否合理并提供智能处理建议
     * 当货道最大容量减少时，需要检查是否会导致现有库存超过新容量限制
     * 如果存在冲突，提供详细的冲突信息和处理建议
     *
     * @param vmTypeId 设备类型ID
     * @param oldCapacity 原始容量
     * @param newCapacity 新容量
     */
    private void checkCapacityDecreaseAndCleanup(Long vmTypeId, Long oldCapacity, Long newCapacity) {
        // 查询使用该设备类型的所有售货机
        VendingMachine vmQuery = new VendingMachine();
        vmQuery.setVmTypeId(vmTypeId);
        List<VendingMachine> vendingMachines = vendingMachineMapper.selectVendingMachineList(vmQuery);
        if (vendingMachines.isEmpty()) {
            return;
        }

        // 获取所有售货机ID
        List<Long> vmIds = vendingMachines.stream().map(VendingMachine::getId).collect(Collectors.toList());
        // 查询这些售货机的所有货道
        List<Channel> allChannels = channelMapper.selectChannelByVmIds(vmIds);
        // 建立售货机ID到设备编码的映射关系
        Map<Long, String> vmIdToCodeMap = vendingMachines.stream()
                .collect(Collectors.toMap(VendingMachine::getId, VendingMachine::getInnerCode));

        // 统计分析：分类统计货道情况
        Map<String, List<String>> conflictVmChannels = new LinkedHashMap<>(); // 超限货道
        Map<String, List<String>> warningVmChannels = new LinkedHashMap<>(); // 接近上限货道（库存=新容量）
        int totalChannels = 0;
        int emptyChannels = 0;
        int conflictChannels = 0;
        int warningChannels = 0;
        
        for (Channel channel : allChannels) {
            totalChannels++;
            Long currentCapacity = channel.getCurrentCapacity();
            
            // 空货道统计
            if (currentCapacity == null || currentCapacity == 0) {
                emptyChannels++;
                continue;
            }
            
            String vmCode = vmIdToCodeMap.get(channel.getVmId());
            
            // 超限货道：当前库存 > 新容量限制
            if (currentCapacity > newCapacity) {
                conflictChannels++;
                conflictVmChannels.computeIfAbsent(vmCode, k -> new ArrayList<>())
                        .add(MessageFormat.format(MessageConstants.CHANNEL_EXCESS_STOCK_FORMAT,
                                channel.getChannelCode(), currentCapacity, currentCapacity - newCapacity));
            } 
            // 警告货道：当前库存 = 新容量限制（满载状态）
            else if (currentCapacity.equals(newCapacity)) {
                warningChannels++;
                warningVmChannels.computeIfAbsent(vmCode, k -> new ArrayList<>())
                        .add(MessageFormat.format(MessageConstants.CHANNEL_WARNING_STOCK_FORMAT,
                                channel.getChannelCode(), currentCapacity));
            }
        }

        // 记录容量减少的统计信息
        log.info("设备类型[{}]容量减少影响分析 - 总货道:{}, 空货道:{}, 超限货道:{}, 满载货道:{}", 
                vmTypeId, totalChannels, emptyChannels, conflictChannels, warningChannels);

        // 如果有警告货道，记录提醒信息
        if (!warningVmChannels.isEmpty()) {
            String warningDetails = warningVmChannels.entrySet().stream()
                    .map(entry -> MessageFormat.format(MessageConstants.WARNING_CHANNELS_FULL_LOAD,
                            entry.getKey(), String.join("; ", entry.getValue())))
                    .collect(Collectors.joining(", "));
            log.warn("容量减少后以下货道将处于满载状态: {}", warningDetails);
        }

        // 如果存在超限冲突，抛出异常并提供处理建议
        if (!conflictVmChannels.isEmpty()) {
            String conflictDetails = conflictVmChannels.entrySet().stream()
                    .map(entry -> MessageFormat.format(MessageConstants.CONFLICT_CHANNELS_DETAIL,
                            entry.getKey(), String.join("; ", entry.getValue())))
                    .collect(Collectors.joining("\n"));

            // 计算需要清理的总库存
            int totalExcessStock = allChannels.stream()
                    .filter(ch -> ch.getCurrentCapacity() != null && ch.getCurrentCapacity() > newCapacity)
                    .mapToInt(ch -> Math.toIntExact(ch.getCurrentCapacity() - newCapacity))
                    .sum();

            throw new ServiceException(String.format(
                    MessageConstants.VM_TYPE_CAPACITY_DECREASE_CONFLICT + 
                    MessageConstants.VM_TYPE_CAPACITY_DECREASE_SUGGESTIONS,
                    oldCapacity, newCapacity, conflictDetails, totalExcessStock
            ));
        }
        
        // 检查通过，记录成功信息
        log.info("设备类型[{}]容量减少检查通过 - 无冲突货道，可安全执行容量调整", vmTypeId);
    }

    /**
     * 更新所有货道的最大容量
     * 当设备类型的最大容量发生变化时，需要同步更新所有相关货道的最大容量和设备的channel_max_capacity
     * 支持容量增加和减少两种场景
     *
     * @param vmTypeId 设备类型ID
     * @param newMaxCapacity 新的最大容量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateChannelsMaxCapacity(Long vmTypeId, Long newMaxCapacity) {
        // 查询使用该设备类型的所有售货机
        VendingMachine vmQuery = new VendingMachine();
        vmQuery.setVmTypeId(vmTypeId);
        List<VendingMachine> vendingMachines = vendingMachineMapper.selectVendingMachineList(vmQuery);
        if (vendingMachines.isEmpty()) {
            log.info("设备类型[{}]下没有关联的售货机，无需更新货道容量", vmTypeId);
            return;
        }

        // 获取所有售货机ID
        List<Long> vmIds = vendingMachines.stream().map(VendingMachine::getId).collect(Collectors.toList());
        
        // 1. 批量更新这些售货机的所有货道最大容量
        int channelUpdateCount = channelMapper.updateMaxCapacityByVmIds(vmIds, newMaxCapacity);
        
        // 2. 批量更新设备表中的channel_max_capacity字段
        int vmUpdateCount = vendingMachineMapper.updateChannelMaxCapacityByVmTypeId(vmTypeId, newMaxCapacity);

        log.info("设备类型[{}]容量更新完成 - 更新了 {} 个货道，{} 台设备的最大容量为 {}", 
                vmTypeId, channelUpdateCount, vmUpdateCount, newMaxCapacity);
        
        // 数据一致性检查
        if (vmUpdateCount != vendingMachines.size()) {
            throw new ServiceException(MessageFormat.format(MessageConstants.VM_UPDATE_COUNT_MISMATCH, 
                    vmUpdateCount, vendingMachines.size()));
        }
        
        if (channelUpdateCount == 0) {
            throw new ServiceException(MessageFormat.format(MessageConstants.CHANNEL_UPDATE_COUNT_ZERO, vmTypeId));
        }
    }

    /**
     * 新增行列时：同步货道
     * 当设备类型的行列数增加时，需要为所有使用该类型的售货机创建新增行列对应的货道
     *
     * @param oldRow 原始行数
     * @param oldCol 原始列数
     * @param newRow 新行数
     * @param newCol 新列数
     * @param vmTypeId 设备类型ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void syncChannelsForVmTypeOnIncrease(Long oldRow, Long oldCol, Long newRow, Long newCol, Long vmTypeId) {
        long start = System.currentTimeMillis();
        // 查询使用该设备类型的所有售货机
        VendingMachine vmQuery = new VendingMachine();
        vmQuery.setVmTypeId(vmTypeId);
        List<VendingMachine> vendingMachines = vendingMachineMapper.selectVendingMachineList(vmQuery);
        if (vendingMachines.isEmpty()) {
            return;
        }

        // 获取所有售货机ID
        List<Long> vmIds = vendingMachines.stream().map(VendingMachine::getId).collect(Collectors.toList());
        // 查询这些售货机已有的所有货道
        List<Channel> existing = channelMapper.selectChannelByVmIds(vmIds);
        // 建立售货机ID到已有货道编码的映射关系
        Map<Long, Set<String>> vmChannelCodesMap = existing.stream()
                .collect(Collectors.groupingBy(Channel::getVmId,
                        Collectors.mapping(Channel::getChannelCode, Collectors.toSet())));

        // 确定需要新增的货道
        List<Channel> toInsert = new ArrayList<>();
        for (VendingMachine vm : vendingMachines) {
            // 获取该售货机已有的货道编码集合
            Set<String> existingCodes = vmChannelCodesMap.getOrDefault(vm.getId(), new HashSet<>());
            // 计算新的行列范围内的所有货道编码
            Set<String> required = new HashSet<>();
            for (int r = 1; r <= newRow; r++) {
                for (int c = 1; c <= newCol; c++) {
                    required.add(r + MessageConstants.CHANNEL_CODE_SEPARATOR + c);
                }
            }
            // 从需要的货道中移除已有的货道，得到真正需要新增的货道
            required.removeAll(existingCodes);
            // 为每个需要新增的货道创建Channel对象
            for (String code : required) {
                toInsert.add(createChannel(vm.getId(), vm.getInnerCode(), code, vm.getChannelMaxCapacity()));
            }
        }

        // 批量插入新增的货道，提高性能
        if (!toInsert.isEmpty()) {
            int batchSize = 1000; // 批量处理大小
            for (int i = 0; i < toInsert.size(); i += batchSize) {
                List<Channel> batch = toInsert.subList(i, Math.min(i + batchSize, toInsert.size()));
                channelMapper.batchInsertChannel(batch);
            }
            log.info("为 {} 台设备补充 {} 个货道，耗时 {}ms",
                    vendingMachines.size(), toInsert.size(), System.currentTimeMillis() - start);
        }
    }

    /**
     * 减少行列时：同步货道
     * 当设备类型的行列数减少时，需要删除超出新行列范围的货道
     *
     * @param oldRow 原始行数
     * @param oldCol 原始列数
     * @param newRow 新行数
     * @param newCol 新列数
     * @param vmTypeId 设备类型ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void syncChannelsForVmTypeOnDecrease(Long oldRow, Long oldCol, Long newRow, Long newCol, Long vmTypeId) {
        long start = System.currentTimeMillis();
        // 查询使用该设备类型的所有售货机
        VendingMachine vmQuery = new VendingMachine();
        vmQuery.setVmTypeId(vmTypeId);
        List<VendingMachine> vendingMachines = vendingMachineMapper.selectVendingMachineList(vmQuery);
        if (vendingMachines.isEmpty()) {
            return;
        }

        // 获取所有售货机ID和设备编码的映射关系
        List<Long> vmIds = vendingMachines.stream().map(VendingMachine::getId).collect(Collectors.toList());
        Map<Long, String> vmIdToCodeMap = vendingMachines.stream()
                .collect(Collectors.toMap(VendingMachine::getId, VendingMachine::getInnerCode));

        // 查询这些售货机的所有货道
        List<Channel> allChannels = channelMapper.selectChannelByVmIds(vmIds);

        // 筛选出需要删除的货道和存在商品的冲突货道
        List<Channel> toDelete = new ArrayList<>();
        Map<String, List<String>> conflictVmChannels = new LinkedHashMap<>();

        for (Channel channel : allChannels) {
            String[] parts = channel.getChannelCode().split(MessageConstants.CHANNEL_CODE_SEPARATOR);
            // 解析货道编码，格式为: MessageConstants.CHANNEL_CODE_FORMAT_DESC
            if (parts.length == 2) {
                try {
                    int row = Integer.parseInt(parts[0]);
                    int col = Integer.parseInt(parts[1]);
                    // 如果货道的行列超出新范围，则需要删除
                    if (row > newRow || col > newCol) {
                        toDelete.add(channel);
                        // 如果该货道关联了商品SKU，则记录为冲突（无论库存多少）
                        if (channel.getSkuId() != null && channel.getSkuId() != 0) {
                            String vmCode = vmIdToCodeMap.get(channel.getVmId());
                        String stockInfo = (channel.getCurrentCapacity() != null) ? 
                                MessageFormat.format(MessageConstants.CHANNEL_STOCK_INFO_WITH_COUNT, channel.getCurrentCapacity()) 
                                : MessageConstants.CHANNEL_STOCK_INFO_CONFIGURED;
                            conflictVmChannels.computeIfAbsent(vmCode, k -> new ArrayList<>())
                                    .add(channel.getChannelCode() + stockInfo);
                        }
                    }
                } catch (NumberFormatException e) {
                    log.warn("非法货道编号: {}", channel.getChannelCode());
                }
            }
        }

        // 如果存在关联商品的冲突货道，抛出异常阻止操作
        if (!conflictVmChannels.isEmpty()) {
            String conflictDetails = conflictVmChannels.entrySet().stream()
                    .map(entry -> MessageFormat.format(MessageConstants.CHANNEL_CONFLICT_DETAIL_FORMAT, 
                            entry.getKey(), String.join("; ", entry.getValue())))
                    .collect(Collectors.joining("\n"));

            throw new ServiceException(String.format(
                    MessageConstants.VM_TYPE_ROW_COL_DECREASE_CONFLICT,
                    conflictDetails
            ));
        }

        // 批量删除超出范围的货道
        if (!toDelete.isEmpty()) {
            List<Long> channelIds = toDelete.stream().map(Channel::getId).collect(Collectors.toList());
            int batchSize = 1000; // 批量处理大小
            for (int i = 0; i < channelIds.size(); i += batchSize) {
                List<Long> batch = channelIds.subList(i, Math.min(i + batchSize, channelIds.size()));
                channelMapper.deleteChannelByIds(batch.toArray(new Long[0]));
            }
            log.info("删除 {} 个超出范围的货道，耗时 {}ms", toDelete.size(), System.currentTimeMillis() - start);
        }
    }

    /**
     * 创建新货道
     * 用于为售货机创建新的货道对象
     *
     * @param vmId 售货机ID
     * @param innerCode 售货机内部编码
     * @param channelCode 货道编码（格式：行-列）
     * @param maxCapacity 最大容量
     * @return 新创建的货道对象
     */
    private Channel createChannel(Long vmId, String innerCode, String channelCode, Long maxCapacity) {
        Channel channel = new Channel();
        channel.setVmId(vmId); // 关联售货机ID
        channel.setInnerCode(innerCode); // 继承售货机内部编码
        channel.setChannelCode(channelCode); // 设置货道编码
        channel.setMaxCapacity(maxCapacity); // 设置最大容量
        channel.setCurrentCapacity(0L); // 初始当前容量为0
        channel.setLastSupplyTime(null); // 初始补货时间为null
        return channel;
    }



    /**
     * 批量删除设备类型管理
     *
     * @param ids 需要删除的设备类型管理主键数组
     * @return 影响的记录数
     */
    @Override
    public int deleteVmTypeByIds(Long[] ids) {
        // 根据id批量查询判断是否与设备关联
        List<VendingMachine> vendingMachineList = vendingMachineMapper.selectBatchVendingMachineByVmTypeId(ids);
        log.info("设备类型管理关联的售货机：{}",vendingMachineList);
        if (CollectionUtils.isNotEmpty(vendingMachineList)){
            // 有就不能删
            // 拼接设备名
            String vmNames = StringUtils.join(vendingMachineList.stream().map(VendingMachine::getInnerCode).collect(Collectors.toList()), MessageConstants.VM_NAME_SEPARATOR);
            throw new ServiceException(MessageFormat.format(MessageConstants.VM_TYPE_EXITS_VM,vmNames));
        }
        // 没有删除
        return vmTypeMapper.deleteVmTypeByIds(ids);
    }

    /**
     * 删除设备类型管理信息
     *
     * @param id 设备类型管理主键
     * @return 影响的记录数
     */
    @Override
    public int deleteVmTypeById(Long id) {
        return vmTypeMapper.deleteVmTypeById(id);
    }
}
