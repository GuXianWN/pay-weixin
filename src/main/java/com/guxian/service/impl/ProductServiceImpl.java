package com.guxian.service.impl;

import com.guxian.entity.Product;
import com.guxian.mapper.ProductMapper;
import com.guxian.service.IProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author GuXianWN
 * @since 2021-12-17
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements IProductService {

}
