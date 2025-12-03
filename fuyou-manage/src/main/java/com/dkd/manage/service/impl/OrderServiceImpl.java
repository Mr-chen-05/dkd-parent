package com.dkd.manage.service.impl;

import java.util.List;
import com.dkd.common.utils.DateUtils;
import com.dkd.manage.domain.dto.OrderDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dkd.manage.mapper.OrderMapper;
import com.dkd.manage.domain.Order;
import com.dkd.manage.service.IOrderService;

/**
 * 订单Service业务层处理
 * 
 * @author luo
 * @date 2025-10-11
 */
@Service
public class OrderServiceImpl implements IOrderService 
{
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 查询订单
     * 
     * @param id 订单主键
     * @return 订单
     */
    @Override
    public Order selectOrderById(Long id)
    {
        return orderMapper.selectOrderById(id);
    }

    /**
     * 查询订单列表
     * 
     * @param orderDto 订单
     * @return 订单
     */
    @Override
    public List<Order> selectOrderListVO(OrderDto orderDto)
    {
        return orderMapper.selectOrderListVO(orderDto);
    }


    /**
     * 新增订单
     * 
     * @param order 订单
     * @return 结果
     */
    @Override
    public int insertOrder(Order order)
    {
        order.setCreateTime(DateUtils.getNowDate());
        return orderMapper.insertOrder(order);
    }

    /**
     * 修改订单
     * 
     * @param order 订单
     * @return 结果
     */
    @Override
    public int updateOrder(Order order)
    {
        order.setUpdateTime(DateUtils.getNowDate());
        return orderMapper.updateOrder(order);
    }

    /**
     * 批量删除订单
     * 
     * @param ids 需要删除的订单主键
     * @return 结果
     */
    @Override
    public int deleteOrderByIds(Long[] ids)
    {
        return orderMapper.deleteOrderByIds(ids);
    }

    /**
     * 删除订单信息
     * 
     * @param id 订单主键
     * @return 结果
     */
    @Override
    public int deleteOrderById(Long id)
    {
        return orderMapper.deleteOrderById(id);
    }
}
