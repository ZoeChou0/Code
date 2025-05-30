package com.zsh.petsystem.dto;

import lombok.Data;

@Data
public class ReviewDTO {
    private Long reservationId;
    private Integer rating;
    private String Comment;

    private Long reviewId;
}
