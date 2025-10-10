package com.dkd.manage.domain.vo;

import com.dkd.manage.domain.Emp;
import lombok.Data;

@Data
public class EmpVo {
    private Integer id;

    private Integer userId;

    private String userName;

    private String roleName;

    private String mobile;

    private String regionName;

    // 本月统计
    private Integer finishCountMonth;
    private Integer progressCountMonth;
    private Integer cancelCountMonth;

    // 本周统计
    private Integer finishCountWeek;
    private Integer progressCountWeek;
    private Integer cancelCountWeek;

    // 本年统计
    private Integer finishCountYear;
    private Integer progressCountYear;
    private Integer cancelCountYear;
}
