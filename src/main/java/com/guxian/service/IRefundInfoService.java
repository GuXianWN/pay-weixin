package com.guxian.service;

import com.guxian.entity.RefundInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author GuXianWN
 * @since 2021-12-17
 */
public interface IRefundInfoService extends IService<RefundInfo> {
    RefundInfo creatRefundByOrderNo(String orderNo,String reason);

    void updateRefund(String body);
}
