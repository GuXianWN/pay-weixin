package com.guxian.service;

import com.guxian.entity.OrderInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guxian.enums.OrderStatus;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author GuXianWN
 * @since 2021-12-17
 */
public interface IOrderInfoService extends IService<OrderInfo> {
    OrderInfo createOrderBy(Long productId);

    OrderInfo getNoPayOrderByProductId(Long productId);

    void saveCodeUrl(String orderNo,String codeUrl);

    List<OrderInfo> listOrderByCreatTimeDesc();

    void updateStatusByOrderNo(String orderNo, OrderStatus orderStatus);

    String getOrderInfoStatus(String orderNo);

    List<OrderInfo> getNoPayOrderByDuration(int minutes);

    OrderInfo getOrderByOrderNo(String orderNo);
}
