package com.dkd.manage.service.impl;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.dkd.common.constant.DkdContants;
import com.dkd.common.constant.MessageConstants;
import com.dkd.common.exception.ServiceException;
import com.dkd.common.utils.DateUtils;
import com.dkd.manage.domain.Emp;
import com.dkd.manage.domain.TaskDetails;
import com.dkd.manage.domain.VendingMachine;
import com.dkd.manage.domain.dto.TaskDetailsDto;
import com.dkd.manage.domain.dto.TaskDto;
import com.dkd.manage.domain.vo.TaskVO;
import com.dkd.manage.mapper.EmpMapper;
import com.dkd.manage.mapper.TaskDetailsMapper;
import com.dkd.manage.mapper.VendingMachineMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.dkd.manage.mapper.TaskMapper;
import com.dkd.manage.domain.Task;
import com.dkd.manage.service.ITaskService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 工单Service业务层处理
 *
 * @author luo
 * @date 2025-09-29
 */
@Service
public class TaskServiceImpl implements ITaskService {
    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskDetailsMapper taskDetailsMapper;

    @Autowired
    private VendingMachineMapper vendingMachineMapper;

    @Autowired
    private EmpMapper empMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 查询工单
     *
     * @param taskId 工单主键
     * @return 工单
     */
    @Override
    public Task selectTaskByTaskId(Long taskId) {
        return taskMapper.selectTaskByTaskId(taskId);
    }

    /**
     * 查询工单列表
     *
     * @param task 工单
     * @return 工单
     */
    @Override
    public List<Task> selectTaskList(Task task) {
        return taskMapper.selectTaskList(task);
    }

    /**
     * 新增工单
     *
     * @param taskDto 工单
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertTask(TaskDto taskDto) {
        // 1. 查询售货机是否存在
        VendingMachine vendingMachine = vendingMachineMapper.selectVendingMachineByInnerCode(taskDto.getInnerCode());
        if (vendingMachine == null) {
            // 设备不存在
            throw new ServiceException(MessageFormat.format(MessageConstants.INNER_CODE_NOT_EXIST, taskDto.getInnerCode()));
        }
        // 2. 校验售货机状态与工单类型是否相符
        checkCreateTask(vendingMachine.getVmStatus(), taskDto.getProductTypeId());
        // 3. 检查设备是否有未完成的同类型工单
        hasTask(taskDto.getInnerCode(), taskDto.getProductTypeId());
        // 4. 查询并校验员工是否存在
        Emp emp = empMapper.selectEmpById(taskDto.getUserId());
        if (emp == null) {
            throw new ServiceException(MessageConstants.USER_NOT_EXIST);
        }
        // 5. 校验员工区域是否匹配
        if (!emp.getRegionId().equals(vendingMachine.getRegionId())) {
            throw new ServiceException(MessageConstants.USER_NOT_IN_REGION);
        }
        // 6. 将dto转为po并补充属性，保存工单
        Task task = BeanUtil.copyProperties(taskDto, Task.class);
        task.setTaskStatus(DkdContants.TASK_STATUS_CREATE);// 创建工单
        task.setUserName(emp.getUserName());// 执行人名称
        task.setRegionId(vendingMachine.getRegionId());// 所属区域id
        task.setAddr(vendingMachine.getAddr());// 地址
        // task.setCreateTime(DateUtils.getNowDate());// 创建时间
        task.setTaskCode(generateTaskCode());// 工单编号
        int taskResult = taskMapper.insertTask(task);
        // 7. 判断是否为补货工单
        if (taskDto.getProductTypeId().equals(DkdContants.TASK_TYPE_SUPPLY)) {
            // 8.保存工单详情判断是否为空
            List<TaskDetailsDto> details = taskDto.getDetails();
            if (CollUtil.isEmpty(details)) {
                throw new ServiceException(MessageConstants.TASK_DETAILS_NOT_EMPTY);
            }
            // 将dto转为vo补充属性
            List<TaskDetails> taskDetails = details.stream().map(d -> {
                TaskDetails taskDetail = BeanUtil.copyProperties(d, TaskDetails.class);
                taskDetail.setTaskId(task.getTaskId());
                return taskDetail;
            }).collect(Collectors.toList());

            // 批量新增
            Integer taskDetailCount = taskDetailsMapper.insertBatchTaskDetails(taskDetails);
            // 判断保存工单详情是否成功
            if (taskDetailCount != taskDetails.size()) {
                throw new ServiceException(MessageConstants.TASK_DETAILS_SAVE_FAIL);
            }
        }

        return taskResult;
    }

    /**
     * 生成并获取当天任务代码的唯一标识。
     * 该方法首先尝试从Redis中获取当天的任务代码计数，如果不存在，则初始化为1并返回"日期0001"格式的字符串。
     * 如果存在，则对计数加1并返回更新后的任务代码。
     *
     * @return 返回当天任务代码的唯一标识，格式为"日期XXXX"，其中XXXX是四位数字的计数。
     */
    public String generateTaskCode() {
        // 获取当前日期并格式化为"yyyyMMdd"
        String dateStr = DateUtils.getDate().replaceAll("-", "");
        // 根据日期生成redis的键
        String key = "dkd.task.code." + dateStr;
        // 判断key是否存在
        if (!redisTemplate.hasKey(key)) {
            // 如果key不存在，设置初始值为1，并指定过期时间为1天
            redisTemplate.opsForValue().set(key, String.valueOf(1), Duration.ofDays(1));
            // 返回工单编号（日期+0001）
            return dateStr + "0001";
        }
        // 如果key存在，计数器+1（0002），确保字符串长度为4位
        return dateStr + StrUtil.padPre(Objects.requireNonNull(redisTemplate.opsForValue().increment(key)).toString(), 4, '0');
    }

    /**
     * 检查设备是否已有未完成的同类型工单。
     * 本方法用于在创建新工单前，验证指定设备是否已经有处于进行中的同类型工单。
     * 如果存在未完成的同类型工单，则抛出服务异常，阻止新工单的创建。
     *
     * @param innerCode     设备的内部编码，用于唯一标识设备。
     * @param productTypeId 任务的类型，决定任务的性质（投放、维修、补货、撤机）。
     */
    private void hasTask(String innerCode, Long productTypeId) {
        // 创建Task对象，并设置设备编号和工单类型ID，以及任务状态为进行中
        Task taskParam = new Task();
        taskParam.setInnerCode(innerCode);
        taskParam.setProductTypeId(productTypeId);
        // taskParam.setTaskStatus(DkdContants.TASK_STATUS_PROGRESS); //忽略此字段

        // 查询数据库中符合指定条件的工单列表并且把工单状态为进行或待处理过滤一下
        List<Task> taskList = taskMapper.selectTaskList(taskParam).stream().filter(
                task -> task.getTaskStatus().equals(DkdContants.TASK_STATUS_PROGRESS) || task.getTaskStatus().equals(DkdContants.TASK_STATUS_CREATE)
        ).collect(Collectors.toList());
        // 如果存在未完成的同类型工单，则抛出服务异常
        if (CollUtil.isNotEmpty(taskList)) {
            throw new ServiceException(MessageConstants.TASK_EXIST_NOT_ALLOWED);
        }
    }

    /**
     * 根据设备的状态和任务类型，验证是否可以创建相应的任务。
     * 如果条件不满足，抛出服务异常。
     *
     * @param vmStatus      设备的状态，表示设备是否在运行。
     * @param productTypeId 任务的类型，决定任务的性质（投放、维修、补货、撤机）。
     */
    private void checkCreateTask(Long vmStatus, Long productTypeId) {
        // 如果是投放工单，且设备状态为运行中，则抛出异常，因为设备已在运营中无法进行投放
        if (Objects.equals(productTypeId, DkdContants.TASK_TYPE_DEPLOY) && Objects.equals(vmStatus, DkdContants.VM_STATUS_RUNNING)) {
            throw new ServiceException(MessageConstants.VM_STATUS_RUNNING_DEPLOY_NOT_ALLOWED);
        }

        // 如果是维修工单，且设备状态不是运行中，则抛出异常，因为设备不在运营中无法进行维修
        if (Objects.equals(productTypeId, DkdContants.TASK_TYPE_REPAIR) && !Objects.equals(vmStatus, DkdContants.VM_STATUS_RUNNING)) {
            throw new ServiceException(MessageConstants.VM_STATUS_NOT_RUNNING_REPAIR_NOT_ALLOWED);
        }

        // 如果是补货工单，且设备状态不是运行中，则抛出异常，因为设备不在运营状态无法进行补货
        if (Objects.equals(productTypeId, DkdContants.TASK_TYPE_SUPPLY) && !Objects.equals(vmStatus, DkdContants.VM_STATUS_RUNNING)) {
            throw new ServiceException(MessageConstants.VM_STATUS_NOT_RUNNING_SUPPLY_NOT_ALLOWED);
        }

        // 如果是撤机工单，且设备状态不是运行中，则抛出异常，因为设备不在运营状态无法进行撤机
        if (Objects.equals(productTypeId, DkdContants.TASK_TYPE_REVOKE) && !Objects.equals(vmStatus, DkdContants.VM_STATUS_RUNNING)) {
            throw new ServiceException(MessageConstants.VM_STATUS_NOT_RUNNING_REVOKE_NOT_ALLOWED);
        }
    }

    /**
     * 修改工单
     *
     * @param task 工单
     * @return 结果
     */
    @Override
    public int updateTask(Task task) {
        task.setUpdateTime(DateUtils.getNowDate());
        return taskMapper.updateTask(task);
    }

    /**
     * 批量删除工单
     *
     * @param taskIds 需要删除的工单主键
     * @return 结果
     */
    @Override
    public int deleteTaskByTaskIds(Long[] taskIds) {
        return taskMapper.deleteTaskByTaskIds(taskIds);
    }

    /**
     * 删除工单信息
     *
     * @param taskId 工单主键
     * @return 结果
     */
    @Override
    public int deleteTaskByTaskId(Long taskId) {
        return taskMapper.deleteTaskByTaskId(taskId);
    }

    @Override
    public List<TaskVO> selectTaskVOList(Task task) {
        return taskMapper.selectTaskVOList(task);
    }

    @Override
    public int cancelTask(Task task) {
        // 1.判断工单状态是否可以取消
        // 先根据工单ID查询工单信息
        Task taskInfo = taskMapper.selectTaskByTaskId(task.getTaskId());
        // 2.判断工单状态是否为已取消，如果是则不允许取消
        if (Objects.equals(taskInfo.getTaskStatus(), DkdContants.TASK_STATUS_CANCEL)) {
            throw new ServiceException(MessageConstants.TASK_STATUS_CANCEL_NOT_ALLOWED);
        }
        // 3.判断工单状态是否为已完成，如果是则不允许取消
        if (Objects.equals(taskInfo.getTaskStatus(), DkdContants.TASK_STATUS_FINISH)) {
            throw new ServiceException(MessageConstants.TASK_STATUS_FINISH_NOT_ALLOWED);
        }
        // 4.都不是的，则允许取消工单
        task.setTaskStatus(DkdContants.TASK_STATUS_CANCEL);
        return taskMapper.updateTask(task);
    }
}
