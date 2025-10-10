package com.dkd.manage.domain.vo;

import com.dkd.manage.domain.Task;
import com.dkd.manage.domain.TaskType;
import lombok.Data;

@Data
public class TaskVO extends Task {

    private TaskType taskType;
}
