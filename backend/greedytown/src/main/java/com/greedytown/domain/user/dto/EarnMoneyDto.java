package com.greedytown.domain.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EarnMoneyDto {

    private Integer gameInfo;
    private Long money;
}
