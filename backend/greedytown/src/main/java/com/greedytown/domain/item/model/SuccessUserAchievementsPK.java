package com.greedytown.domain.item.model;


import lombok.Data;

import java.io.Serializable;

@Data
public class SuccessUserAchievementsPK implements Serializable {

    private Long achievementsSeq;
    private Long userSeq;

}
