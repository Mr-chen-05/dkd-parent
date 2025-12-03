package com.dkd.manage.service.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import com.dkd.common.constant.MessageConstants;
import com.dkd.common.exception.ServiceException;
import com.dkd.common.utils.DateUtils;
import com.dkd.common.utils.StringUtils;
import com.dkd.manage.domain.Emp;
import com.dkd.manage.domain.Node;
import com.dkd.manage.domain.vo.RegionVo;
import com.dkd.manage.mapper.EmpMapper;
import com.dkd.manage.mapper.NodeMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dkd.manage.mapper.RegionMapper;
import com.dkd.manage.domain.Region;
import com.dkd.manage.service.IRegionService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 区域管理Service业务层处理
 * 
 * @author luo
 * @date 2025-09-14
 */
@Service
public class RegionServiceImpl implements IRegionService 
{
    @Autowired
    private RegionMapper regionMapper;

    @Autowired
    private NodeMapper nodeMapper;

    @Autowired
    private EmpMapper empMapper;

    /**
     * 查询区域管理
     * 
     * @param id 区域管理主键
     * @return 区域管理
     */
    @Override
    public Region selectRegionById(Long id)
    {
        return regionMapper.selectRegionById(id);
    }

    /**
     * 查询区域管理列表
     * 
     * @param region 区域管理
     * @return 区域管理
     */
    @Override
    public List<Region> selectRegionList(Region region)
    {
        return regionMapper.selectRegionList(region);
    }

    /**
     * 新增区域管理
     * 
     * @param region 区域管理
     * @return 结果
     */
    @Override
    public int insertRegion(Region region)
    {
        region.setCreateTime(DateUtils.getNowDate());
        return regionMapper.insertRegion(region);
    }

    /**
     * 修改区域管理
     * 
     * @param region 区域管理
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateRegion(Region region)
    {
        // 先更新区域
        int row = regionMapper.updateRegion(region);
        // 判断更新是否成功
        if (row < 0){
            throw new ServiceException(MessageConstants.REGION_UPDATE_ERROR);
        }
        // 再更新员工的区域
        Emp emp = new Emp();
        emp.setRegionId(region.getId());
        emp.setRegionName(region.getRegionName());

        return empMapper.updateEmpByRegionId(emp);
    }

    /**
     * 批量删除区域管理
     * 
     * @param ids 需要删除的区域管理主键
     * @return 结果
     */
    @Override
    public int deleteRegionByIds(Long[] ids)
    {
        // 根据区域ids查询是否有点位信息
        List<Node> nodeList = nodeMapper.selectNodeByRegionId(ids);
        // 有就报错
        judgmentIfNodeList(nodeList);
        // 没有删除
        return regionMapper.deleteRegionByIds(ids);
    }
    // 判断是否有点位信息
    static void judgmentIfNodeList(List<Node> nodeList) {
        if (CollectionUtils.isNotEmpty(nodeList)){
            List<String> nodeNameList = new ArrayList<>(nodeList.size());
            // 存储点位名
            for (Node node : nodeList) {
                nodeNameList.add(node.getNodeName());
            }
            // 拼接点位名
            String nodeNames = StringUtils.join(nodeNameList, "、");
            throw new ServiceException(MessageFormat.format(MessageConstants.PRESENT_DELETE_ERROR, nodeNames));
        }
    }

    /**
     * 删除区域管理信息
     * 
     * @param id 区域管理主键
     * @return 结果
     */
    @Override
    public int deleteRegionById(Long id)
    {
        return regionMapper.deleteRegionById(id);
    }

    @Override
    public List<RegionVo> selectRegionListVo(Region region) {
        return regionMapper.selectRegionListVo(region);
    }
}
