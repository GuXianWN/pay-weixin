package com.guxian.task;

import com.guxian.entity.OrderInfo;
import com.guxian.service.impl.OrderInfoServiceImpl;
import com.guxian.service.impl.WxPayServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author GuXianWN
 * @date 2021/12/21 13:28
 **/
@Slf4j
@Component
public class WxPayTask {
    @Resource
    private OrderInfoServiceImpl orderInfoService;
    @Resource
    private WxPayServiceImpl wxPayService;

    /**
     * 秒 分 时 日 月 周
     *  * 表示每秒都执行
     *  1-3从第一秒开始执行到第三秒结束执行
     *  0/3从第零秒开始每三秒执行一次
     *
     * 日和周不能同时制定
     */

    /**
     * 30秒查询一次创建超过5分钟且没支付的订单
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void orderConfirm() throws Exception {
        List<OrderInfo> orderInfoList = orderInfoService.getNoPayOrderByDuration(5);
        for (OrderInfo orderInfo : orderInfoList) {
            String orderNo = orderInfo.getOrderNo();
            log.warn("超时订单===>{}", orderNo);
            //核实订单状态
            wxPayService.checkOrderStatus(orderNo);
        }
    }
}
