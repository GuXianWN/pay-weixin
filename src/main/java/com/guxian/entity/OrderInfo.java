package com.guxian.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author GuXianWN
 * @since 2021-12-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_order_info")
@ApiModel(value="OrderInfo对象", description="")
public class OrderInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "订单id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "订单标题")
    private String title;

    @ApiModelProperty(value = "商户订单编号")
    private String orderNo;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "支付产品id")
    private Long productId;

    @ApiModelProperty(value = "订单金额(分)")
    private Integer totalFee;

    @ApiModelProperty(value = "订单二维码连接")
    private String codeUrl;

    @ApiModelProperty(value = "订单状态")
    private String orderStatus;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;


}
