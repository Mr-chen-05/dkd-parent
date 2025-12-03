package com.dkd.manage.mapper;

import com.dkd.manage.domain.TaskCollect;
import org.apache.ibatis.annotations.Mapper;

/**
* @author luo20
* @description 针对表【tb_task_collect(工单按日统计表)】的数据库操作Mapper
* @createDate 2025-09-18 08:20:56
* @Entity com.dkd.manage.domain.TaskCollect
*/
@Mapper
public interface TaskCollectMapper {

    int deleteByPrimaryKey(Long id);

    int insert(TaskCollect record);

    int insertSelective(TaskCollect record);

    TaskCollect selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TaskCollect record);

    int updateByPrimaryKey(TaskCollect record);

}
