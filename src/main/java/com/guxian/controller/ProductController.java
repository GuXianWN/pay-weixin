package com.guxian.controller;


import com.guxian.entity.Product;
import com.guxian.entity.RespBean;
import com.guxian.service.impl.ProductServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@Api(tags = "商品管理")
@RestController
@RequestMapping("/api/product")
public class ProductController {
    @Resource
    private ProductServiceImpl productService;

    @ApiOperation("获取商品列表")
    @GetMapping("list")
    public RespBean getProductList() {
        List<Product> list = productService.list();
        return RespBean.success().data("productList",list);
    }
}
