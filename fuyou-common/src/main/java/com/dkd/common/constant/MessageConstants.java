package com.dkd.common.constant;
/**
 * 信息提示常量类
 */
public class MessageConstants {
    public static final String PRESENT_DELETE_ERROR = "该管理下有点位信息,有：{0}";
    public static final String REGION_UPDATE_ERROR = "该区域更新失败!";
    public static final String ADD_EQUIPMENT_FAIL = "该设备添加失败";
    public static final String ADD_CHANNEL_FAIL = "批量新增售货机货道失败！";

    public static final String NODE_UPDATE_ERROR = "点位更新失败！";
    public static final String UPDATE_VM_FAIL = "设备更新失败";
    // ===== 新增：设备类型管理相关提示 =====
    public static final String VM_TYPE_ROW_COL_INVALID = "行列数必须大于0";
    public static final String VM_TYPE_ROW_COL_EXCEED_LIMIT = "行列数不能超过10";
    public static final String VM_TYPE_CAPACITY_INVALID = "货道容量必须大于0";
    public static final String VM_TYPE_CAPACITY_EXCEED_LIMIT = "货道容量不能超过1000";
    public static final String VM_TYPE_NOT_EXIST = "设备类型不存在!";
    public static final String VM_TYPE_NAME_EXIST = "设备类型名称已存在";
    public static final String VM_TYPE_MODEL_EXIST = "设备型号编码已存在";
    public static final String VM_TYPE_CAPACITY_DECREASE_CONFLICT = "无法将货道容量从 %d 减少至 %d，存在以下超限情况：\n%s";
    public static final String VM_TYPE_CAPACITY_DECREASE_SUGGESTIONS = "\n\n💡 处理建议：\n1. 需要清理总库存数量：%d 件商品\n2. 可通过补货管理模块批量调整库存\n3. 或联系运维人员现场处理超限货道\n4. 处理完成后重新尝试容量调整";
    public static final String VM_TYPE_ROW_COL_DECREASE_CONFLICT = "无法缩减设备类型行列数，以下货道中仍有商品：\n%s";
    
    // ===== 货道信息格式化相关 =====
    public static final String CHANNEL_STOCK_INFO_WITH_COUNT = " (库存:{0})";
    public static final String CHANNEL_STOCK_INFO_CONFIGURED = " (已配置商品)";
    public static final String CHANNEL_CONFLICT_DETAIL_FORMAT = "设备[{0}] 的关联商品货道：\n   → {1}";
    public static final String CHANNEL_EXCESS_STOCK_FORMAT = "{0} (库存:{1}→需减少:{2})";
    public static final String CHANNEL_WARNING_STOCK_FORMAT = "{0} (库存:{1}→满载)";
    
    // ===== 业务异常提示信息 =====
    public static final String VM_UPDATE_COUNT_MISMATCH = "设备表更新数量({0})与预期({1})不一致，数据完整性异常";
    public static final String CHANNEL_UPDATE_COUNT_ZERO = "设备类型[{0}]下没有货道被更新，货道数据异常";
    public static final String WARNING_CHANNELS_FULL_LOAD = "设备[{0}] → {1}";
    public static final String CONFLICT_CHANNELS_DETAIL = "设备[{0}] 的超限货道：\n   → {1}";
    
    // ===== 通用格式常量 =====
    public static final String CHANNEL_CODE_SEPARATOR = "-";
    public static final String CHANNEL_CODE_FORMAT_DESC = "行-列";
    public static final String VM_NAME_SEPARATOR = "、";

    public static final String VM_TYPE_EXITS_VM = "该设备类型下有设备关联:{0}";
    public static final String DELETE_VM_NOT_EXIST = "该设备不存在！";
    public static final String VM_STATUS_NOT_REVOKE = "当前设备:{0}没有撤机,不能删除";
    public static final String DELETE_VM_FAIL = "设备删除失败！";
    public static final String DELETE_CHANNEL_FAIL = "设备货到删除失败！";
    public static final String DELETE_NODE_NOT_EXIST = "该点位不存在,无法删除";
    public static final String NODE_HAVE_VM = "{0}下有设备,设备编号为:{1}";
    public static final String DELETE_SKU_CLASS_NOT_EMPTY = "{0}商品类型下仍有{1}商品";
    public static final String DELETE_SKU_CLASS_NOT_EXIST = "需要删除的商品类型不存在！";
    public static final String DELETE_SKU_NOT_EXIST = "选择的商品不存在！删除失败！";
    public static final String DELETE_SKU = "设备编号为{0}下的货道编号{1}有{2}商品，无法删除！";
    public static final String INNER_CODE_NOT_EXIST = "设备{0}不存在！";
    public static final String VM_STATUS_RUNNING_DEPLOY_NOT_ALLOWED = "该设备状态为运行中，无法进行投放";
    public static final String VM_STATUS_NOT_RUNNING_REPAIR_NOT_ALLOWED = "该设备状态不是运行中，无法进行维修";
    public static final String VM_STATUS_NOT_RUNNING_SUPPLY_NOT_ALLOWED = "该设备状态不是运行中，无法进行补货";
    public static final String VM_STATUS_NOT_RUNNING_REVOKE_NOT_ALLOWED = "该设备状态不是运行中，无法进行撤机";
    public static final String TASK_EXIST_NOT_ALLOWED = "该设备有未完成的同类型工单，不能重复创建";
    public static final String USER_NOT_EXIST = "员工不存在！";
    public static final String USER_NOT_IN_REGION = "员工区域与设备区域不一致,无法处理此工单！";
    public static final String TASK_DETAILS_NOT_EMPTY = "补货工单详情不能为空!";
    public static final String TASK_DETAILS_SAVE_FAIL = "新增工单详情失败！";
    public static final String TASK_STATUS_CANCEL_NOT_ALLOWED = "工单状态为已取消,不能再次取消！";
    public static final String TASK_STATUS_FINISH_NOT_ALLOWED = "工单状态为已完成 ,不能再次取消！";
    public static final String DELETE_EMP_NOT_EMPTY = "{0}是启用状态,不能删除！";

    public static final String UPDATE_VM_STATUS_FAILD = "{0}设备状态更新失败";
}
