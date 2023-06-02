package com.guxian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.guxian.entity.OrderInfo;
import com.guxian.entity.Product;
import com.guxian.enums.OrderStatus;
import com.guxian.mapper.OrderInfoMapper;
import com.guxian.mapper.ProductMapper;
import com.guxian.service.IOrderInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guxian.util.OrderNoUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author GuXianWN
 * @since 2021-12-17
 */
@Service
@Slf4j
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements IOrderInfoService {
    @Resource
    private ProductMapper productMapper;

    @Override
    public OrderInfo createOrderBy(Long productId) {
        //查找已存在但未支付的订单
        OrderInfo order = this.getNoPayOrderByProductId(productId);
        if (order != null) {
            return order;
        }
        //获取商品信息
        Product product = productMapper.selectById(productId);
        //生成订单
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setTitle(product.getTitle());
        orderInfo.setProductId(productId);
        orderInfo.setOrderNo(OrderNoUtils.getOrderNo());
        orderInfo.setTotalFee(product.getPrice());
        orderInfo.setOrderStatus(OrderStatus.NOTPAY.getType());
        baseMapper.insert(orderInfo);
        return orderInfo;
    }

    @Override
    public OrderInfo getNoPayOrderByProductId(Long productId) {
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("product_id", productId);
        queryWrapper.eq("order_status", OrderStatus.NOTPAY.getType());
        OrderInfo orderInfo = baseMapper.selectOne(queryWrapper);
        return orderInfo;
    }

    @Override
    public void saveCodeUrl(String orderNo, String codeUrl) {
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no", orderNo);
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCodeUrl(codeUrl);
        baseMapper.update(orderInfo, queryWrapper);
    }

    @Override
    public List<OrderInfo> listOrderByCreatTimeDesc() {
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<OrderInfo>()
                .orderByDesc("create_time");
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 根据订单号更新订单
     *
     * @param orderNo     订单号
     * @param orderStatus 订单状态
     */
    @Override
    public void updateStatusByOrderNo(String orderNo, OrderStatus orderStatus) {
        log.info("更新订单状态===>{}", orderStatus.getType());
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderStatus(orderStatus.getType());
        baseMapper.update(orderInfo, new QueryWrapper<OrderInfo>()
                .eq("order_no", orderNo)
        );
    }

    @Override
    public String getOrderInfoStatus(String orderNo) {
        OrderInfo orderInfo = baseMapper.selectOne(new QueryWrapper<OrderInfo>()
                .eq("order_no", orderNo));
        if (orderInfo == null) {
            return null;
        }
        return orderInfo.getOrderStatus();
    }

    @Override
    public List<OrderInfo> getNoPayOrderByDuration(int minutes) {
        Instant minus = Instant.now().minus(Duration.ofMinutes(minutes));
        return baseMapper.selectList(new QueryWrapper<OrderInfo>()
                .eq("order_status", OrderStatus.NOTPAY.getType())
                .le("create_time", minus));
    }

    @Override
    public OrderInfo getOrderByOrderNo(String orderNo) {
        return baseMapper.selectOne(new QueryWrapper<OrderInfo>()
                .eq("order_no", orderNo));
    }
}
