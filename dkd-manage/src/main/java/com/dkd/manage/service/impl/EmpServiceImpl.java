package com.dkd.manage.service.impl;

import java.text.MessageFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.dkd.common.constant.DkdContants;
import com.dkd.common.constant.MessageConstants;
import com.dkd.common.exception.ServiceException;
import com.dkd.common.utils.DateUtils;
import com.dkd.manage.domain.Region;
import com.dkd.manage.domain.Role;
import com.dkd.manage.domain.VendingMachine;
import com.dkd.manage.domain.dto.EmpDto;
import com.dkd.manage.domain.vo.EmpVo;
import com.dkd.manage.mapper.RegionMapper;
import com.dkd.manage.mapper.RoleMapper;
import com.dkd.manage.mapper.VendingMachineMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dkd.manage.mapper.EmpMapper;
import com.dkd.manage.domain.Emp;
import com.dkd.manage.service.IEmpService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 人员列表Service业务层处理
 *
 * @author luo
 * @date 2025-09-17
 */
@Service
public class EmpServiceImpl implements IEmpService {
    @Autowired
    private EmpMapper empMapper;
    @Autowired
    private RegionMapper regionMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private VendingMachineMapper vendingMachineMapper;

    /**
     * 查询人员列表
     *
     * @param id 人员列表主键
     * @return 人员列表
     */
    @Override
    public Emp selectEmpById(Long id) {
        return empMapper.selectEmpById(id);
    }

    /**
     * 查询人员列表列表
     *
     * @param emp 人员列表
     * @return 人员列表
     */
    @Override
    public List<Emp> selectEmpList(Emp emp) {
        return empMapper.selectEmpList(emp);
    }

    /**
     * 新增人员列表
     *
     * @param emp 人员列表
     * @return 结果
     */
    @Override
    public int insertEmp(Emp emp) {
        // 根据区域id添加区域名称
        emp.setRegionName(regionMapper.selectRegionById(emp.getRegionId()).getRegionName());
        // 根据角色id添加角色名称和角色编号
        Role role = roleMapper.selectRoleByRoleId(emp.getRoleId());
        emp.setRoleName(role.getRoleName());
        emp.setRoleCode(role.getRoleCode());
        return empMapper.insertEmp(emp);
    }

    /**
     * 修改人员列表
     *
     * @param emp 人员列表
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateEmp(Emp emp) {
        return empMapper.updateEmp(emp);
    }

    /**
     * 批量删除人员列表
     *
     * @param ids 需要删除的人员列表主键
     * @return 结果
     */
    @Override
    public int deleteEmpByIds(Long[] ids) {
        // 根据ids查询员工
        List<Emp> empList = empMapper.selectEmpByIds(ids);
        // 筛选出状态为启用的员工拼接成字符串
        String userNames = empList.stream().filter(emp -> emp.getStatus().equals(DkdContants.EMP_STATUS_NORMAL)).map(Emp::getUserName).collect(Collectors.joining("、"));
        if (!userNames.isEmpty()) {
            throw new ServiceException(MessageFormat.format(MessageConstants.DELETE_EMP_NOT_EMPTY, userNames));
        }
        return empMapper.deleteEmpByIds(ids);
    }

    /**
     * 删除人员列表信息
     *
     * @param id 人员列表主键
     * @return 结果
     */
    @Override
    public int deleteEmpById(Long id) {
        return empMapper.deleteEmpById(id);
    }

    @Override
    public List<EmpVo> selectEmpWorkList(EmpDto empDto) {
        // 计算月初和月末
        LocalDate now = LocalDate.now();
        // 计算本月的开始和结束日期
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());

        // 计算本周的开始和结束日期（周一到周日）
        LocalDate weekStart = now.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = now.with(DayOfWeek.SUNDAY);

        // 计算本年的开始和结束日期
        LocalDate yearStart = now.withDayOfYear(1);
        LocalDate yearEnd = now.withDayOfYear(now.lengthOfYear());

        EmpDto empdto = new EmpDto()
                .setMonthStart(monthStart)
                .setMonthEnd(monthEnd)
                .setWeekStart(weekStart)
                .setWeekEnd(weekEnd)
                .setYearStart(yearStart)
                .setYearEnd(yearEnd)
                .setStatus(DkdContants.EMP_STATUS_NORMAL)
                .setRoleCode(empDto.getRoleCode())
                .setUserId(empDto.getUserId())
                .setUserName(empDto.getUserName());

        return empMapper.selectEmpWorkList(empdto);
    }

    @Override
    public List<Emp> getBusinessList(String innerCode) {
        // 根据设备编号获取设备绑定的区域id
        VendingMachine vendingMachine = new VendingMachine();
        vendingMachine.setInnerCode(innerCode);
        List<VendingMachine> vendingMachineList = vendingMachineMapper.selectVendingMachineList(vendingMachine);
        // 判断当前设备是否则存在
        if (vendingMachineList.isEmpty()) {
            throw new ServiceException(MessageFormat.format(MessageConstants.INNER_CODE_NOT_EXIST, innerCode));
        }
        // 根据区域id查询员工表里启用并且角色是运营员的员工
        Emp emp = new Emp();
        emp.setRegionId(vendingMachineList.get(0).getRegionId());
        emp.setStatus(DkdContants.EMP_STATUS_NORMAL);
        emp.setRoleCode(DkdContants.ROLE_CODE_BUSINESS);
        return empMapper.selectEmpList(emp);
    }

    @Override
    public List<Emp> getOperationList(String innerCode) {
        // 根据设备编号获取设备绑定的区域id
        VendingMachine vendingMachine = new VendingMachine();
        vendingMachine.setInnerCode(innerCode);
        List<VendingMachine> vendingMachineList = vendingMachineMapper.selectVendingMachineList(vendingMachine);
        // 判断当前设备是否则存在
        if (vendingMachineList.isEmpty()) {
            throw new ServiceException(MessageFormat.format(MessageConstants.INNER_CODE_NOT_EXIST, innerCode));
        }
        // 根据区域id查询员工表里启用并且角色是运营员的员工
        Emp emp = new Emp();
        emp.setRegionId(vendingMachineList.get(0).getRegionId());
        emp.setStatus(DkdContants.EMP_STATUS_NORMAL);
        emp.setRoleCode(DkdContants.ROLE_CODE_OPERATOR);
        return empMapper.selectEmpList(emp);
    }
}
