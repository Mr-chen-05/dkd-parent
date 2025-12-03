package com.dkd.manage.domain.vo;

import com.dkd.manage.domain.VendingMachine;
import lombok.Data;

/**
 * 设备管理VO对象，包含工单状态相关信息
 * 
 * @author luo
 * @date 2025-11-25
 */
@Data
public class VendingMachineVO extends VendingMachine {

    /**
     * 是否有活跃工单（状态为1-待办或2-进行中）
     * 0-否 1-是
     */
    private Integer hasActiveTask;

    /**
     * 关联的工单状态
     * 1-待办 2-进行中 3-已取消 4-已完成
     * null表示无关联工单
     */
    private Integer taskStatus;

    private String nodeName;

    private String regionName;

    private String partnerName;

    private String vmTypeName;

    private java.util.Map<String, Object> runningStatusObj;

    /**
     * 判断是否有活跃工单（便捷方法）
     */
    public boolean isHasActiveTask() {
        return hasActiveTask != null && hasActiveTask == 1;
    }
}
