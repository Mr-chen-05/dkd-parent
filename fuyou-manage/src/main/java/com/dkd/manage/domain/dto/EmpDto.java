package com.dkd.manage.domain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class EmpDto {
    private Long userId;

    private String userName;

    private String roleCode;

    private Long status;

    // 本月时间范围
    private LocalDate monthStart;
    private LocalDate monthEnd;

    // 本周时间范围
    private LocalDate weekStart;
    private LocalDate weekEnd;

    // 本年时间范围
    private LocalDate yearStart;
    private LocalDate yearEnd;

}
