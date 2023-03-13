package com.greedytown.domain.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenDto {

    String accessToken;
    String refreshToken;
}
