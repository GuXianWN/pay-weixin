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
@TableName("t_payment_info")
@ApiModel(value="PaymentInfo对象", description="")
public class PaymentInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "支付记录id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "商户订单编号")
    private String orderNo;

    @ApiModelProperty(value = "支付系统交易编号")
    private String transactionId;

    @ApiModelProperty(value = "支付类型")
    private String paymentType;

    @ApiModelProperty(value = "交易类型")
    private String tradeType;

    @ApiModelProperty(value = "交易状态")
    private String tradeState;

    @ApiModelProperty(value = "支付金额(分)")
    private Integer payerTotal;

    @ApiModelProperty(value = "通知参数")
    private String content;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;


}
