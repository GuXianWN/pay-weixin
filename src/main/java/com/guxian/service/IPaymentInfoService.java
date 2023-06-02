package com.guxian.service;

import com.guxian.entity.PaymentInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author GuXianWN
 * @since 2021-12-17
 */
public interface IPaymentInfoService extends IService<PaymentInfo> {

    void createPaymentInfo(String plainTest);
}
