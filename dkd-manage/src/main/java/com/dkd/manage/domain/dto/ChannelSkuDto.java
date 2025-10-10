package com.dkd.manage.domain.dto;

import lombok.Data;

// 某个货道对应的sku信息
@Data
public class ChannelSkuDto {

    private String innerCode;
    private String channelCode;
    private Long skuId;
}