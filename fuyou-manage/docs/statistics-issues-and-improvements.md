# 统计优化问题检查与进一步优化建议

## 🔍 已发现的问题和改进点

### ❌ 问题1: `selectTaskStatsByType` - WHERE条件顺序问题
**位置**: `StatisticsMapper.xml` 第19-23行

**问题**:
```xml
<where>
    <if test="start != null and end != null">
        AND t.create_time BETWEEN #{start} AND #{end}
    </if>
    AND tt.type IN (1, 2)  <!-- ❌ 这行在<where>标签内，但如果上面的if不成立，会导致SQL以AND开头 -->
</where>
```

**修复**: tt.type条件应该移到WHERE之外或者改写逻辑

---

### ❌ 问题2: 异常设备查询的重复字段问题
**位置**: `StatisticsMapper.xml` 第189-195行

**问题**: 当多个工单的`create_time`完全相同时，会返回重复记录

**优化**: 添加task_id作为次要排序条件

---

### ⚠️ 问题3: DATE函数导致索引失效
**位置**: 多处SQL查询中使用 `DATE(create_time)`

**问题**:
```sql
WHERE status = 2
AND DATE(create_time) BETWEEN #{beginTime} AND #{endTime}
```

DATE函数会导致索引失效，应该改为直接比较datetime

**优化**: 使用范围查询代替函数

---

### ⚠️ 问题4: GROUP BY与COALESCE的兼容性问题
**位置**: `salesRegionDistribution` 查询

**问题**:
```sql
SELECT 
    COALESCE(region_name, addr, '未知区域') as region_key,
    COALESCE(SUM(amount), 0) as amount
FROM tb_order
GROUP BY region_key  -- ❌ 某些MySQL版本不支持按别名分组
```

**修复**: 应该重复完整的COALESCE表达式

---

### ⚠️ 问题5: NULL值处理不完善
**位置**: 多个返回Map的查询

**问题**: MyBatis返回的Map中，数字字段可能为NULL，导致Java代码中的类型转换异常

**优化**: 在SQL中统一使用COALESCE处理所有可能为NULL的数字字段

---

### 💡 问题6: 缺少查询超时保护
**位置**: 所有查询

**建议**: 添加查询超时保护，防止慢查询拖垮系统

---

### 💡 问题7: 缺少缓存机制
**位置**: Service层

**建议**: 统计数据实时性要求不高，可以添加缓存

---

## 🚀 详细修复方案

### 修复1: WHERE条件重写
```xml
<select id="selectTaskStatsByType" resultType="map">
    SELECT 
        tt.type as task_type,
        tt.type_name,
        COUNT(*) as total,
        COUNT(DISTINCT t.user_id) as worker_count,
        SUM(CASE WHEN t.task_status = 4 THEN 1 ELSE 0 END) as completed_total,
        SUM(CASE WHEN t.task_status = 3 THEN 1 ELSE 0 END) as cancel_total,
        SUM(CASE WHEN t.task_status IN (1, 2) THEN 1 ELSE 0 END) as progress_total
    FROM tb_task t
    LEFT JOIN tb_task_type tt ON t.product_type_id = tt.type_id
    WHERE tt.type IN (1, 2)
    <if test="start != null and end != null">
        AND t.create_time BETWEEN #{start} AND #{end}
    </if>
    GROUP BY tt.type, tt.type_name
    ORDER BY tt.type
</select>
```

### 修复2: 避免DATE函数
```xml
<!-- ❌ 错误写法 -->
AND DATE(create_time) BETWEEN #{beginTime} AND #{endTime}

<!-- ✅ 正确写法 -->
AND create_time >= #{beginTime} 
AND create_time < DATE_ADD(#{endTime}, INTERVAL 1 DAY)
```

或者在Java层处理：
```java
// 将LocalDate转换为包含时间的范围
LocalDateTime startDateTime = start.atStartOfDay();
LocalDateTime endDateTime = end.plusDays(1).atStartOfDay();
```

### 修复3: GROUP BY别名问题
```xml
<!-- ❌ 错误写法 -->
SELECT 
    COALESCE(region_name, addr, '未知区域') as region_key,
    COALESCE(SUM(amount), 0) as amount
FROM tb_order
GROUP BY region_key

<!-- ✅ 正确写法 -->
SELECT 
    COALESCE(region_name, addr, '未知区域') as region_key,
    COALESCE(SUM(amount), 0) as amount
FROM tb_order
WHERE status = 2
<if test="beginTime != null and endTime != null">
    AND create_time >= #{beginTime} 
    AND create_time < DATE_ADD(#{endTime}, INTERVAL 1 DAY)
</if>
GROUP BY COALESCE(region_name, addr, '未知区域')
ORDER BY amount DESC
```

### 修复4: 异常设备查询去重
```xml
<select id="selectAbnormalEquipmentWithTaskStatus">
    SELECT 
        vm.*,
        latest_task.task_status,
        CASE 
            WHEN latest_task.task_status IN (1, 2) THEN 1 
            ELSE 0 
        END as has_active_task
    FROM tb_vending_machine vm
    LEFT JOIN (
        SELECT 
            t1.inner_code,
            t1.task_status
        FROM tb_task t1
        INNER JOIN (
            SELECT 
                inner_code,
                MAX(create_time) as max_create_time,
                MAX(task_id) as max_task_id  -- ✅ 添加task_id避免重复
            FROM tb_task
            GROUP BY inner_code
        ) t2 ON t1.inner_code = t2.inner_code 
            AND t1.create_time = t2.max_create_time
            AND t1.task_id = t2.max_task_id  -- ✅ 添加额外条件
    ) latest_task ON vm.inner_code = latest_task.inner_code
    WHERE vm.vm_status IN (0, 2)
    AND (latest_task.task_status IS NULL OR latest_task.task_status != 4)
    ORDER BY vm.update_time DESC
    <if test="limit != null">
        LIMIT #{limit}
    </if>
</select>
```

### 修复5: 添加NULL值保护
```xml
<!-- 所有COUNT、SUM等聚合函数都应该有默认值 -->
SELECT 
    COALESCE(COUNT(*), 0) as total,
    COALESCE(COUNT(DISTINCT t.user_id), 0) as worker_count,
    COALESCE(SUM(CASE WHEN ... END), 0) as completed_total
```

---

## 🎯 进一步优化建议

### 优化1: 添加缓存机制

```java
@Service
public class StatisticsServiceImplOptimized implements IStatisticsService {
    
    @Autowired
    private StatisticsMapper statisticsMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 销售统计（带缓存）
     */
    @Override
    public Map<String, Object> salesStats(LocalDate start, LocalDate end) {
        String cacheKey = "stats:sales:" + start + ":" + end;
        
        // 尝试从缓存获取
        Map<String, Object> cached = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // 从数据库查询
        Map<String, Object> result = statisticsMapper.selectSalesStats(start, end);
        
        // 缓存5分钟
        redisTemplate.opsForValue().set(cacheKey, result, 5, TimeUnit.MINUTES);
        
        return result;
    }
}
```

### 优化2: 添加@Cacheable注解（Spring Cache）

```java
@Cacheable(value = "salesStats", key = "#start + ':' + #end", 
           unless = "#result == null")
@Override
public Map<String, Object> salesStats(LocalDate start, LocalDate end) {
    return statisticsMapper.selectSalesStats(start, end);
}
```

### 优化3: 批量查询优化

对于需要多次调用的场景，可以合并查询：

```java
/**
 * 批量获取多个时间段的统计数据
 */
public List<Map<String, Object>> batchSalesStats(List<DateRange> dateRanges) {
    // 使用SQL的UNION ALL合并多个查询
    return statisticsMapper.selectBatchSalesStats(dateRanges);
}
```

### 优化4: 异步统计任务

对于复杂的统计，可以使用异步任务：

```java
@Async
public CompletableFuture<Map<String, Object>> salesStatsAsync(LocalDate start, LocalDate end) {
    Map<String, Object> result = statisticsMapper.selectSalesStats(start, end);
    return CompletableFuture.completedFuture(result);
}
```

### 优化5: 物化视图（MySQL 5.7不支持，可用定时任务模拟）

```sql
-- 创建统计汇总表
CREATE TABLE tb_order_stats_daily (
    stat_date DATE PRIMARY KEY,
    order_count INT,
    order_amount BIGINT,
    updated_at TIMESTAMP
);

-- 定时任务更新汇总表
INSERT INTO tb_order_stats_daily 
SELECT 
    DATE(create_time) as stat_date,
    COUNT(*) as order_count,
    SUM(amount) as order_amount,
    NOW() as updated_at
FROM tb_order
WHERE status = 2
AND DATE(create_time) = CURDATE() - INTERVAL 1 DAY
ON DUPLICATE KEY UPDATE
    order_count = VALUES(order_count),
    order_amount = VALUES(order_amount),
    updated_at = NOW();
```

### 优化6: 添加查询监控

```java
@Aspect
@Component
public class StatisticsQueryMonitor {
    
    @Around("execution(* com.dkd.manage.service.impl.Statistics*.*(..))")
    public Object monitorQuery(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        String methodName = pjp.getSignature().getName();
        
        try {
            Object result = pjp.proceed();
            long duration = System.currentTimeMillis() - start;
            
            // 记录慢查询
            if (duration > 1000) {
                log.warn("慢查询警告: {} 耗时 {}ms", methodName, duration);
            }
            
            return result;
        } catch (Exception e) {
            log.error("查询异常: {}", methodName, e);
            throw e;
        }
    }
}
```

### 优化7: 分页查询优化

对于大数据量的统计，可以添加游标分页：

```java
/**
 * 使用游标分页处理大数据量统计
 */
public void processBigDataStats(Consumer<List<Order>> processor) {
    Long lastId = 0L;
    int batchSize = 1000;
    
    while (true) {
        List<Order> batch = statisticsMapper.selectOrderByIdRange(lastId, batchSize);
        if (batch.isEmpty()) break;
        
        processor.accept(batch);
        lastId = batch.get(batch.size() - 1).getId();
    }
}
```

---

## 📊 性能测试建议

### 1. 压力测试脚本

```bash
# 使用JMeter或ab工具测试
ab -n 1000 -c 10 http://localhost:8080/api/statistics/sales?start=2025-01-01&end=2025-11-25
```

### 2. SQL执行计划分析

```sql
-- 分析每个查询的执行计划
EXPLAIN SELECT ... FROM tb_order WHERE ...;

-- 检查是否使用了索引
SHOW INDEX FROM tb_order;
```

### 3. 慢查询日志

```sql
-- 开启慢查询日志
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 1;

-- 查看慢查询
SELECT * FROM mysql.slow_log ORDER BY query_time DESC LIMIT 10;
```

---

## ✅ 检查清单

- [ ] 修复WHERE条件问题
- [ ] 修复DATE函数导致的索引失效
- [ ] 修复GROUP BY别名问题
- [ ] 添加异常设备查询去重
- [ ] 添加NULL值保护
- [ ] 实施缓存机制（可选）
- [ ] 添加查询监控（可选）
- [ ] 性能测试验证
- [ ] 慢查询日志分析

---

## 🎯 优先级建议

### 🔴 高优先级（必须修复）
1. WHERE条件问题
2. DATE函数索引失效问题
3. GROUP BY别名兼容性问题
4. NULL值处理

### 🟡 中优先级（强烈建议）
1. 异常设备查询去重
2. 添加基本缓存
3. 查询监控

### 🟢 低优先级（可选优化）
1. 物化视图/汇总表
2. 异步查询
3. 批量查询

---

**总结**: 发现了5个需要修复的问题和7个进一步优化的方向。建议先修复高优先级问题，然后进行性能测试验证效果。
