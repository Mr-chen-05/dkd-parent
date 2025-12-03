-- =============================================
-- 统计查询性能优化索引
-- 创建日期: 2025-11-25
-- 说明: 为StatisticsServiceImpl优化而添加的索引
-- =============================================

-- ============================================
-- 任务表索引
-- ============================================

-- 用于 userTaskStats: 按创建时间、类型、状态统计
CREATE INDEX idx_task_create_time_type_status 
ON tb_task(create_time, product_type_id, task_status);

-- 用于 abnormalEquipmentList: 按设备编码查找最新工单
CREATE INDEX idx_task_inner_code_create_time 
ON tb_task(inner_code, create_time DESC);

-- 用于按用户ID查询（如果需要）
CREATE INDEX idx_task_user_id 
ON tb_task(user_id);

-- ============================================
-- 订单表索引
-- ============================================

-- 用于 salesStats, salesTrend: 按状态和创建时间统计
CREATE INDEX idx_order_status_create_time 
ON tb_order(status, create_time);

-- 用于 skuSaleRank: 按状态、商品统计
CREATE INDEX idx_order_status_sku 
ON tb_order(status, sku_id, sku_name);

-- 用于 salesRegionDistribution: 按状态、区域统计
CREATE INDEX idx_order_status_region 
ON tb_order(status, region_name);

-- 用于 salesTrendByClass: 按状态、类别、时间统计
CREATE INDEX idx_order_status_class_time 
ON tb_order(status, class_id, create_time);

-- 用于按地址查询（作为region_name的补充）
CREATE INDEX idx_order_addr 
ON tb_order(addr);

-- ============================================
-- 点位表索引
-- ============================================

-- 用于 partnerNodeTop: 按合作商ID统计点位
CREATE INDEX idx_node_partner_id 
ON tb_node(partner_id);

-- ============================================
-- 设备表索引
-- ============================================

-- 用于 abnormalEquipmentList: 按状态和更新时间查询
CREATE INDEX idx_vm_status_update_time 
ON tb_vending_machine(vm_status, update_time DESC);

-- 用于按设备编码查询
CREATE INDEX idx_vm_inner_code 
ON tb_vending_machine(inner_code);

-- ============================================
-- 合作商表索引（如果需要）
-- ============================================

-- 如果tb_partner表还没有主键索引，确保有
-- ALTER TABLE tb_partner ADD PRIMARY KEY (partner_id);

-- ============================================
-- 工单类型表索引（如果需要）
-- ============================================

-- 如果tb_task_type表还没有索引
CREATE INDEX idx_task_type_type 
ON tb_task_type(type);

-- =============================================
-- 索引使用说明
-- =============================================

/*
执行建议：
1. 在业务低峰期执行，避免影响在线业务
2. 对于大表（百万级以上），建议使用ONLINE方式创建索引（MySQL 5.6+）：
   CREATE INDEX ... ON ... (...) ALGORITHM=INPLACE, LOCK=NONE;
3. 创建索引后，检查执行计划确认索引被使用
4. 定期监控索引使用情况，删除未使用的索引

索引效果验证：
-- 检查索引是否创建成功
SHOW INDEX FROM tb_task;
SHOW INDEX FROM tb_order;
SHOW INDEX FROM tb_node;
SHOW INDEX FROM tb_vending_machine;

-- 分析查询执行计划（示例）
EXPLAIN SELECT ... FROM tb_task WHERE create_time BETWEEN ... AND ...;
*/
