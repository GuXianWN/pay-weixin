package com.guxian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.Gson;
import com.guxian.entity.OrderInfo;
import com.guxian.entity.RefundInfo;
import com.guxian.mapper.RefundInfoMapper;
import com.guxian.service.IRefundInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guxian.util.OrderNoUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author GuXianWN
 * @since 2021-12-17
 */
@Service
public class RefundInfoServiceImpl extends ServiceImpl<RefundInfoMapper, RefundInfo> implements IRefundInfoService {
    @Resource
    private OrderInfoServiceImpl orderInfoService;

    @Override
    public RefundInfo creatRefundByOrderNo(String orderNo, String reason) {
        //根据订单号获取订单信息
        OrderInfo order = orderInfoService.getOrderByOrderNo(orderNo);
        //根据订单号生成退款单
        RefundInfo refund = new RefundInfo();
        refund.setOrderNo(orderNo);
        refund.setRefundNo(OrderNoUtils.getRefundNo());
        refund.setTotalFee(order.getTotalFee());
        refund.setRefund(order.getTotalFee());
        refund.setReason(reason);
        //保存退款单
        baseMapper.insert(refund);
        return refund;
    }

    @Override
    public void updateRefund(String body) {
        Gson gson = new Gson();
        HashMap<String, String> map = gson.fromJson(body, HashMap.class);
        RefundInfo refund = new RefundInfo();
        refund.setRefundId(map.get("refund_id"));

        if (map.get("status") != null) {
            refund.setRefundStatus(map.get("status"));
            refund.setContentReturn(body);
        }
        if (map.get("refund_status") != null) {
            refund.setRefundStatus(map.get("refund_status"));
            refund.setContentNotify(body);
        }
        baseMapper.update(refund, new QueryWrapper<RefundInfo>()
                .eq("refund_no", map.get("out_refund_no")));
    }
}
