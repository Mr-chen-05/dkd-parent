package com.dkd.manage.service;

import java.util.List;
import com.dkd.manage.domain.Order;
import com.dkd.manage.domain.dto.OrderDto;

/**
 * 订单Service接口
 * 
 * @author luo
 * @date 2025-10-11
 */
public interface IOrderService 
{
    /**
     * 查询订单
     * 
     * @param id 订单主键
     * @return 订单
     */
    public Order selectOrderById(Long id);

    /**
     * 查询订单列表
     * 
     * @param orderDto 订单
     * @return 订单集合
     */
    public List<Order> selectOrderListVO(OrderDto orderDto);


    /**
     * 新增订单
     * 
     * @param order 订单
     * @return 结果
     */
    public int insertOrder(Order order);

    /**
     * 修改订单
     * 
     * @param order 订单
     * @return 结果
     */
    public int updateOrder(Order order);

    /**
     * 批量删除订单
     * 
     * @param ids 需要删除的订单主键集合
     * @return 结果
     */
    public int deleteOrderByIds(Long[] ids);

    /**
     * 删除订单信息
     * 
     * @param id 订单主键
     * @return 结果
     */
    public int deleteOrderById(Long id);
}
