package com.dkd.manage.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import com.dkd.common.annotation.Excel;
import com.dkd.common.core.domain.BaseEntity;

/**
 * 订单对象 tb_order
 *
 * @author luo
 * @date 2025-10-14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("订单对象")
public class Order extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @ApiModelProperty("主键")
    private Long id;

    /** 订单编号 */
    @ApiModelProperty("订单编号")
    @Excel(name = "订单编号")
    private String orderNo;

    /** 第三方平台单号 */
    @ApiModelProperty("第三方平台单号")
    @Excel(name = "第三方平台单号")
    private String thirdNo;

    /** 机器编号 */
    @ApiModelProperty("机器编号")
    @Excel(name = "机器编号")
    private String innerCode;

    /** 货道编号 */
    @ApiModelProperty("货道编号")
    @Excel(name = "货道编号")
    private String channelCode;

    /** skuId（逻辑外键，关联 tb_sku.sku_id） */
    @ApiModelProperty("skuId（逻辑外键，关联 tb_sku.sku_id）")
    @Excel(name = "skuId", readConverterExp = "逻=辑外键，关联,t=b_sku.sku_id")
    private Long skuId;

    /** 商品名称 */
    @ApiModelProperty("商品名称")
    @Excel(name = "商品名称")
    private String skuName;

    /** 商品类别Id（逻辑外键，关联 tb_sku_class.class_id） */
    @ApiModelProperty("商品类别Id（逻辑外键，关联 tb_sku_class.class_id）")
    @Excel(name = "商品类别Id", readConverterExp = "逻=辑外键，关联,t=b_sku_class.class_id")
    private Long classId;

    /** 订单状态:0-待支付;1-支付完成;2-出货成功;3-出货失败;4-已取消 */
    @ApiModelProperty("订单状态:0-待支付;1-支付完成;2-出货成功;3-出货失败;4-已取消")
    @Excel(name = "订单状态:0-待支付;1-支付完成;2-出货成功;3-出货失败;4-已取消")
    private Long status;

    /** 支付金额 */
    @ApiModelProperty("支付金额")
    @Excel(name = "支付金额")
    private Long amount;

    /** 商品金额 */
    @ApiModelProperty("商品金额")
    @Excel(name = "商品金额")
    private Long price;

    /** 支付类型，1支付宝 2微信 */
    @ApiModelProperty("支付类型，1支付宝 2微信")
    @Excel(name = "支付类型，1支付宝 2微信")
    private String payType;

    /** 支付状态，0-未支付;1-支付完成;2-退款中;3-退款完成 */
    @ApiModelProperty("支付状态，0-未支付;1-支付完成;2-退款中;3-退款完成")
    @Excel(name = "支付状态，0-未支付;1-支付完成;2-退款中;3-退款完成")
    private Long payStatus;

    /** 合作商账单金额 */
    @ApiModelProperty("合作商账单金额")
    @Excel(name = "合作商账单金额")
    private Long bill;

    /** 点位地址 */
    @ApiModelProperty("点位地址")
    @Excel(name = "点位地址")
    private String addr;

    /** 所属区域Id（逻辑外键，关联 tb_region.id） */
    @ApiModelProperty("所属区域Id（逻辑外键，关联 tb_region.id）")
    @Excel(name = "所属区域Id", readConverterExp = "逻=辑外键，关联,t=b_region.id")
    private Long regionId;

    /** 区域名称 */
    @ApiModelProperty("区域名称")
    @Excel(name = "区域名称")
    private String regionName;

    /** 所属商圈 */
    @ApiModelProperty("所属商圈")
    @Excel(name = "所属商圈")
    private Long businessType;

    /** 合作商Id（逻辑外键，关联 tb_partner.id） */
    @ApiModelProperty("合作商Id（逻辑外键，关联 tb_partner.id）")
    @Excel(name = "合作商Id", readConverterExp = "逻=辑外键，关联,t=b_partner.id")
    private Long partnerId;

    /** 跨站身份验证 */
    @ApiModelProperty("跨站身份验证")
    @Excel(name = "跨站身份验证")
    private String openId;

    /** 点位Id（逻辑外键，关联 tb_node.id） */
    @ApiModelProperty("点位Id（逻辑外键，关联 tb_node.id）")
    @Excel(name = "点位Id", readConverterExp = "逻=辑外键，关联,t=b_node.id")
    private Long nodeId;

    /** 点位名称 */
    @ApiModelProperty("点位名称")
    @Excel(name = "点位名称")
    private String nodeName;

    /** 取消原因 */
    @ApiModelProperty("取消原因")
    @Excel(name = "取消原因")
    private String cancelDesc;

}
