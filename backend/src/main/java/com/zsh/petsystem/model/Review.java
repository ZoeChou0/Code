package com.zsh.petsystem.model;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("reviews")
public class Review {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long serviceItemId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
