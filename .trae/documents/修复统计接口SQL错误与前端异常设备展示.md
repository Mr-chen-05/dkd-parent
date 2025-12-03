## 问题分析
- 合作商点位Top5接口报错：SQL语法错误，`tb_partner`不存在列`partner_id`，应为主键`id`，导致 `JOIN` 失败（`StatisticsMapper.xml:156-172`）。
- 异常设备列表接口报错：`vm.inner_code = latest_task.inner_code` 在 MySQL8 遇到隐式不同排序规则（`utf8mb4_0900_ai_ci` vs `utf8mb4_unicode_ci`）进行`=`比较触发 1267 错误（`StatisticsMapper.xml:182-226`）。

## 修复方案
1. 修正合作商点位统计的连接条件：将 `LEFT JOIN tb_partner p ON n.partner_id = p.partner_id` 改为 `LEFT JOIN tb_partner p ON n.partner_id = p.id`，其余 `GROUP BY n.partner_id, p.partner_name` 保持不变。
2. 统一异常设备查询的连接字符比较的排序规则：在 `ON` 条件显式指定同一排序规则，例如：`ON vm.inner_code COLLATE utf8mb4_unicode_ci = latest_task.inner_code COLLATE utf8mb4_unicode_ci`，避免跨表隐式排序规则冲突。
3. 保持 `SELECT` 字段、`LIMIT`、`ORDER BY` 不变；不改动 `StatisticsServiceImpl.java` 与 `StatisticsController.java` 的方法签名与入参。
4. 前端体验优化（可选）：在 `abnormal-equipment-table.vue` 加入加载状态展示（使用现有 `loading` 变量，容器加 `v-loading="loading"`），接口修复后首屏避免空白感。

## 代码改动点
- 后端：`e:\fuyou-parent\fuyou-manage\src\main\resources\mapper\manage\StatisticsMapper.xml`
  - 更新 `selectPartnerNodeStats` 的 `JOIN` 条件（约 `156-172` 行）。
  - 更新 `selectAbnormalEquipmentWithTaskStatus` 的 `ON` 条件（约 `208-220` 行）。
- 前端（可选）：`e:\ideaProgram\dkd\dkd-vue\src\views\home\components\abnormal-equipment-table.vue`
  - 在根容器或表格外层添加 `v-loading="loading"`，首屏开启 `loading`，拉取完成后关闭。

## 验证步骤
1. 后端单接口验证：
   - `GET /manage/statistics/partnerNodeTop?topN=5` 应返回 `seriesData`、`totalNodes`、`partnerCount` 字段，且无 500。
   - `GET /manage/statistics/abnormalEquipmentList?limit=10` 应返回设备数组，包含 `innerCode/addr/updateTime/hasActiveTask/taskStatus`，无 1267 错误。
2. 前端页面验证：刷新首页看板
   - “合作商点位数Top5”图与右侧汇总数值正常显示。
   - “异常设备监控”列表展示设备条目；无报错红框；加载过程显示转圈。

## 风险与兼容
- 排序规则显式设置仅影响该查询，不改变库/表级设置；若后续同类字符串连接比较出现类似问题，建议统一库/表字符集与排序规则。
- `tb_partner` 主键为 `id` 的假设已由 `Partner.java` 验证；修复后与 `Node.partnerId` 一致，兼容现有数据结构。

请确认以上方案，我将据此修改 XML 并优化前端加载效果，随后执行接口与页面验证。