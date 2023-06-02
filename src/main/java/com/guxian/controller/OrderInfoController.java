package com.guxian.controller;


import com.guxian.entity.OrderInfo;
import com.guxian.entity.RespBean;
import com.guxian.enums.OrderStatus;
import com.guxian.service.impl.OrderInfoServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author GuXianWN
 * @since 2021-12-17
 */
@CrossOrigin
@Api(tags = "订单管理")
@RestController
@RequestMapping("/api/order-info")
public class OrderInfoController {
    @Resource
    private OrderInfoServiceImpl orderInfoService;

    @GetMapping("list")
    @ApiOperation("订单列表")
    public RespBean list(){
        List<OrderInfo> list = orderInfoService.listOrderByCreatTimeDesc();
        return RespBean.success().data("list",list);
    }

    @GetMapping("query-order-status/{orderNo}")
    @ApiOperation("查询订单状态")
    public RespBean queryOrderStatus(@PathVariable String orderNo){
        String status=orderInfoService.getOrderInfoStatus(orderNo);
        if (OrderStatus.SUCCESS.getType().equals(status)){
            return RespBean.success().setCode(0).setMessage("支付成功");
        }
        return RespBean.success().setCode(101).setMessage("支付中...");
    }
}
