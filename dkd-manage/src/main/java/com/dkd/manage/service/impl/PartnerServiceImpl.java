package com.dkd.manage.service.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import com.dkd.common.constant.MessageConstants;
import com.dkd.common.exception.ServiceException;
import com.dkd.common.utils.DateUtils;
import com.dkd.common.utils.SecurityUtils;
import com.dkd.common.utils.StringUtils;
import com.dkd.manage.domain.Node;
import com.dkd.manage.domain.vo.PartnerVo;
import com.dkd.manage.mapper.NodeMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dkd.manage.mapper.PartnerMapper;
import com.dkd.manage.domain.Partner;
import com.dkd.manage.service.IPartnerService;

/**
 * 合作商管理Service业务层处理
 * 
 * @author luo
 * @date 2025-09-14
 */
@Service
public class PartnerServiceImpl implements IPartnerService 
{
    @Autowired
    private PartnerMapper partnerMapper;

    @Autowired
    private NodeMapper nodeMapper;


    /**
     * 查询合作商管理
     * 
     * @param id 合作商管理主键
     * @return 合作商管理
     */
    @Override
    public Partner selectPartnerById(Long id)
    {
        return partnerMapper.selectPartnerById(id);
    }

    /**
     * 查询合作商管理列表
     * 
     * @param partner 合作商管理
     * @return 合作商管理
     */
    @Override
    public List<Partner> selectPartnerList(Partner partner)
    {
        return partnerMapper.selectPartnerList(partner);
    }

    @Override
    public List<PartnerVo> selectPartnerListVo(Partner partner) {
        return partnerMapper.selectPartnerListVo(partner);
    }

    /**
     * 新增合作商管理
     * 
     * @param partner 合作商管理
     * @return 结果
     */
    @Override
    public int insertPartner(Partner partner)
    {
        partner.setCreateTime(DateUtils.getNowDate());
        // 获取当前登录
        String username = SecurityUtils.getUsername();
        partner.setCreateBy(username);
        partner.setPassword(SecurityUtils.encryptPassword(partner.getPassword()));
        return partnerMapper.insertPartner(partner);
    }

    /**
     * 修改合作商管理
     * 
     * @param partner 合作商管理
     * @return 结果
     */
    @Override
    public int updatePartner(Partner partner)
    {
        // 获取当前登录
        String username = SecurityUtils.getUsername();
        partner.setUpdateBy(username);
        return partnerMapper.updatePartner(partner);
    }

    /**
     * 批量删除合作商管理
     * 
     * @param ids 需要删除的合作商管理主键
     * @return 结果
     */
    @Override
    public int deletePartnerByIds(Long[] ids)
    {
        // 根据合作商ids查询是否有点位信息
        List<Node> nodeList = nodeMapper.selectNodeByPartnerId(ids);
        // 有就报错
        RegionServiceImpl.judgmentIfNodeList(nodeList);
        // 没有删除
        return partnerMapper.deletePartnerByIds(ids);
    }

    /**
     * 删除合作商管理信息
     * 
     * @param id 合作商管理主键
     * @return 结果
     */
    @Override
    public int deletePartnerById(Long id)
    {
        return partnerMapper.deletePartnerById(id);
    }

    @Override
    public int resetPwdPartner(Long id) {
        Partner partner = new Partner();
        partner.setUpdateBy(SecurityUtils.getUsername());
        // 设置加密初始密码
        partner.setPassword(SecurityUtils.encryptPassword("123456"));
        partner.setId(id);
        return partnerMapper.updatePartner(partner);
    }
}
