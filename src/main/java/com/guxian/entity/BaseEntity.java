package com.guxian.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * @author GuXianWN
 */
@Data
public class BaseEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private String id;
    private Date createTime;
    private Date updateTime;
}
