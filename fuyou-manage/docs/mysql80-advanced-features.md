# MySQL 8.0+ 高级特性优化建议

## 🚀 已应用的MySQL 8.0特性

### 1. ✅ 窗口函数 (ROW_NUMBER)
**应用于**: `abnormalEquipmentList` - 异常设备查询

**优化前** (使用子查询):
```sql
LEFT JOIN (
    SELECT t1.inner_code, t1.task_status
    FROM tb_task t1
    INNER JOIN (
        SELECT inner_code, MAX(create_time) as max_create_time
        FROM tb_task GROUP BY inner_code
    ) t2 ON t1.inner_code = t2.inner_code 
        AND t1.create_time = t2.max_create_time
) latest_task ON ...
```

**优化后** (使用窗口函数):
```sql
LEFT JOIN (
    SELECT inner_code, task_status
    FROM (
        SELECT 
            inner_code,
            task_status,
            ROW_NUMBER() OVER (
                PARTITION BY inner_code 
                ORDER BY create_time DESC, task_id DESC
            ) as rn
        FROM tb_task
    ) ranked_tasks
    WHERE rn = 1
) latest_task ON ...
```

**性能提升**: 预计提升20-30%，特别是在数据量大时优势明显

---

## 💡 可进一步应用的MySQL 8.0+特性

### 2. CTE (公用表表达式) - 提升可读性

**当前写法**:
```sql
SELECT ... FROM (
    SELECT ... FROM (
        SELECT ...
    ) t1
) t2
```

**建议改为CTE**:
```sql
WITH ranked_tasks AS (
    SELECT 
        inner_code,
        task_status,
        ROW_NUMBER() OVER (
            PARTITION BY inner_code 
            ORDER BY create_time DESC, task_id DESC
        ) as rn
    FROM tb_task
),
latest_tasks AS (
    SELECT inner_code, task_status
    FROM ranked_tasks
    WHERE rn = 1
)
SELECT vm.*, latest_tasks.task_status, ...
FROM tb_vending_machine vm
LEFT JOIN latest_tasks ON vm.inner_code = latest_tasks.inner_code
WHERE ...
```

**优势**:
- 代码可读性大幅提升
- 易于调试和维护
- 性能相当或略优

### 3. 分析函数 - 用于趋势统计

**应用场景**: `salesTrend` 可以添加移动平均、同比环比等

```sql
WITH daily_sales AS (
    SELECT 
        DATE(create_time) as sale_date,
        SUM(amount) as daily_amount
    FROM tb_order
    WHERE status = 2
    GROUP BY DATE(create_time)
)
SELECT 
    sale_date,
    daily_amount,
    -- 7日移动平均
    AVG(daily_amount) OVER (
        ORDER BY sale_date 
        ROWS BETWEEN 6 PRECEDING AND CURRENT ROW
    ) as ma_7,
    -- 环比增长率
    (daily_amount - LAG(daily_amount, 1) OVER (ORDER BY sale_date)) 
    / LAG(daily_amount, 1) OVER (ORDER BY sale_date) * 100 as growth_rate,
    -- 同比（去年同期）
    LAG(daily_amount, 365) OVER (ORDER BY sale_date) as last_year_amount
FROM daily_sales
ORDER BY sale_date;
```

### 4. JSON聚合函数 - 优化返回结构

**当前**: 返回多行，Java层组装
**优化**: 直接返回JSON结构

```sql
-- 商品销售排行，直接返回JSON数组
SELECT JSON_ARRAYAGG(
    JSON_OBJECT(
        'skuId', sku_id,
        'skuName', sku_name,
        'count', cnt,
        'amount', amt
    )
) as ranking
FROM (
    SELECT 
        sku_id,
        sku_name,
        COUNT(*) as cnt,
        SUM(amount) as amt
    FROM tb_order
    WHERE status = 2
    GROUP BY sku_id, sku_name
    ORDER BY cnt DESC
    LIMIT 10
) t;
```

### 5. 递归CTE - 用于层级统计

**应用场景**: 如果有区域层级结构，可以递归查询

```sql
WITH RECURSIVE region_tree AS (
    -- 基础查询：顶级区域
    SELECT region_id, parent_id, region_name, 1 as level
    FROM tb_region
    WHERE parent_id IS NULL
    
    UNION ALL
    
    -- 递归查询：子区域
    SELECT r.region_id, r.parent_id, r.region_name, rt.level + 1
    FROM tb_region r
    INNER JOIN region_tree rt ON r.parent_id = rt.region_id
)
SELECT 
    rt.region_name,
    rt.level,
    COUNT(o.id) as order_count,
    SUM(o.amount) as total_amount
FROM region_tree rt
LEFT JOIN tb_order o ON rt.region_id = o.region_id
WHERE o.status = 2
GROUP BY rt.region_id, rt.region_name, rt.level
ORDER BY rt.level, total_amount DESC;
```

### 6. GROUPING SETS - 多维度汇总

**应用场景**: 同时统计多个维度

```sql
SELECT 
    COALESCE(region_name, '总计') as region,
    COALESCE(class_id, '所有类别') as class,
    COUNT(*) as order_count,
    SUM(amount) as total_amount
FROM tb_order
WHERE status = 2
GROUP BY GROUPING SETS (
    (region_name, class_id),  -- 按区域和类别
    (region_name),             -- 仅按区域
    (class_id),                -- 仅按类别
    ()                         -- 总计
)
ORDER BY region, class;
```

### 7. LATERAL JOIN - 相关子查询优化

**应用场景**: 为每个设备获取最新的N个工单

```sql
SELECT 
    vm.*,
    recent_tasks.task_list
FROM tb_vending_machine vm
LEFT JOIN LATERAL (
    SELECT JSON_ARRAYAGG(
        JSON_OBJECT(
            'taskId', task_id,
            'taskStatus', task_status,
            'createTime', create_time
        )
    ) as task_list
    FROM (
        SELECT task_id, task_status, create_time
        FROM tb_task
        WHERE inner_code = vm.inner_code
        ORDER BY create_time DESC
        LIMIT 5
    ) t
) recent_tasks ON TRUE
WHERE vm.vm_status IN (0, 2);
```

### 8. 不可见索引 - 安全测试索引影响

```sql
-- 创建不可见索引，不影响现有查询
CREATE INDEX idx_order_status_time ON tb_order(status, create_time) INVISIBLE;

-- 测试性能
SET SESSION optimizer_switch='use_invisible_indexes=on';
-- 执行查询测试

-- 如果效果好，设为可见
ALTER TABLE tb_order ALTER INDEX idx_order_status_time VISIBLE;
```

### 9. 降序索引 - 优化ORDER BY DESC

```sql
-- MySQL 8.0+支持真正的降序索引
CREATE INDEX idx_vm_update_time_desc ON tb_vending_machine(update_time DESC);

-- 对于 ORDER BY update_time DESC 查询性能更好
```

### 10. 函数索引 - 优化计算列查询

```sql
-- 为DATE(create_time)创建函数索引
CREATE INDEX idx_order_create_date 
ON tb_order ((DATE(create_time)));

-- 这样即使使用DATE函数也能利用索引
SELECT * FROM tb_order 
WHERE DATE(create_time) = '2025-11-25';
```

---

## 🎯 建议实施的优化

### 高优先级

1. **✅ 已实施**: 窗口函数替代子查询（异常设备查询）
2. **✅ 已实施**: 修复DATE函数索引失效问题
3. **建议实施**: 使用CTE提升代码可读性
4. **建议实施**: 创建降序索引优化ORDER BY DESC

### 中优先级

5. **建议实施**: 添加移动平均等分析函数
6. **建议实施**: 使用JSON聚合简化数据结构
7. **建议实施**: 函数索引（如果确实需要DATE函数）

### 低优先级（按需）

8. 递归CTE（有层级结构时）
9. GROUPING SETS（有多维汇总需求时）
10. LATERAL JOIN（有相关子查询需求时）

---

## 📝 实施示例：使用CTE改写异常设备查询

```xml
<select id="selectAbnormalEquipmentWithTaskStatus" resultType="com.dkd.manage.domain.vo.VendingMachineVO">
    WITH ranked_tasks AS (
        SELECT 
            inner_code,
            task_status,
            ROW_NUMBER() OVER (
                PARTITION BY inner_code 
                ORDER BY create_time DESC, task_id DESC
            ) as rn
        FROM tb_task
    ),
    latest_task_status AS (
        SELECT inner_code, task_status
        FROM ranked_tasks
        WHERE rn = 1
    )
    SELECT 
        vm.id,
        vm.inner_code,
        vm.channel_max_capacity,
        vm.node_id,
        vm.addr,
        vm.last_supply_time,
        vm.business_type,
        vm.region_id,
        vm.partner_id,
        vm.vm_type_id,
        vm.vm_status,
        vm.running_status,
        vm.longitudes,
        vm.latitude,
        vm.client_id,
        vm.policy_id,
        vm.create_time,
        vm.update_time,
        lts.task_status,
        CASE 
            WHEN lts.task_status IN (1, 2) THEN 1 
            ELSE 0 
        END as has_active_task
    FROM tb_vending_machine vm
    LEFT JOIN latest_task_status lts ON vm.inner_code = lts.inner_code
    WHERE vm.vm_status IN (0, 2)
    AND (lts.task_status IS NULL OR lts.task_status != 4)
    ORDER BY vm.update_time DESC
    <if test="limit != null">
        LIMIT #{limit}
    </if>
</select>
```

---

## 🔧 性能对比测试

### 测试方法

```sql
-- 1. 测试当前查询
SET profiling = 1;
-- 执行查询
SET profiling = 0;
SHOW PROFILES;

-- 2. 使用EXPLAIN分析
EXPLAIN FORMAT=JSON
SELECT ... ;

-- 3. 查看实际执行计划
EXPLAIN ANALYZE
SELECT ... ;
```

### MySQL 8.0+ 特有的EXPLAIN ANALYZE

```sql
-- 显示实际执行统计
EXPLAIN ANALYZE
SELECT vm.*, latest_task.task_status
FROM tb_vending_machine vm
LEFT JOIN (
    SELECT inner_code, task_status
    FROM (
        SELECT 
            inner_code,
            task_status,
            ROW_NUMBER() OVER (PARTITION BY inner_code ORDER BY create_time DESC) as rn
        FROM tb_task
    ) ranked_tasks
    WHERE rn = 1
) latest_task ON vm.inner_code = latest_task.inner_code
WHERE vm.vm_status IN (0, 2);
```

输出示例：
```
-> Nested loop left join (actual time=0.123..45.678 rows=150 loops=1)
    -> Filter: (vm.vm_status in (0,2)) (actual time=0.045..12.345 rows=150 loops=1)
        -> Table scan on vm (actual time=0.023..10.123 rows=1000 loops=1)
    -> Filter: (ranked_tasks.rn = 1) (actual time=0.001..0.234 rows=1 loops=150)
        -> Window aggregate: row_number() OVER (PARTITION BY ... ORDER BY ...) 
           (actual time=0.001..0.123 rows=5 loops=150)
```

---

## 📊 预期性能提升（MySQL 8.0+ vs 5.7）

| 特性 | MySQL 5.7 | MySQL 8.0+ | 提升幅度 |
|------|-----------|------------|---------|
| 窗口函数 vs 子查询 | 需要多重子查询 | 窗口函数 | 20-30% |
| 降序索引 | 模拟（反向扫描） | 真正降序 | 10-15% |
| CTE | 需要临时表或子查询 | 原生支持 | 5-10% |
| 不可见索引 | 不支持 | 支持 | 安全测试 |
| EXPLAIN ANALYZE | 不支持 | 支持 | 更好的调试 |

---

## ✅ 总结

**已完成的优化**:
1. ✅ 使用窗口函数优化异常设备查询
2. ✅ 修复DATE函数导致的索引失效
3. ✅ 修复WHERE条件问题
4. ✅ 修复GROUP BY别名问题
5. ✅ 添加COALESCE保护NULL值

**建议下一步**:
1. 添加降序索引: `CREATE INDEX idx_vm_update_time_desc ON tb_vending_machine(update_time DESC);`
2. 考虑使用CTE改写复杂查询，提升可读性
3. 添加分析函数支持移动平均等高级统计
4. 使用EXPLAIN ANALYZE验证优化效果

**性能预期**:
- 整体性能提升: 80-95%（相比原始实现）
- MySQL 8.0特性额外提升: 10-20%
