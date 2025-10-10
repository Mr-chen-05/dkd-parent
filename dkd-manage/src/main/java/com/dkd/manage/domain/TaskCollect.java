package com.dkd.manage.domain;

import java.io.Serializable;
import java.time.LocalDate;
import lombok.Data;

/**
 * 工单按日统计表
 * @TableName tb_task_collect
 */
@Data
public class TaskCollect implements Serializable {
    /**
     * 
     */
    private Integer id;

    /**
     * 
     */
    private Integer userId;

    /**
     * 当日工单完成数
     */
    private Integer finishCount;

    /**
     * 当日进行中的工单数
     */
    private Integer progressCount;

    /**
     * 当日取消工单数
     */
    private Integer cancelCount;

    /**
     * 汇总的日期
     */
    private LocalDate collectDate;

    private static final long serialVersionUID = 1L;
}