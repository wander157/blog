package com.lyq.community.dto;

import lombok.Data;

/**
 * Created by codedrinker on 2019/6/14.
 */
@Data
public class NotificationDTO {
    private Integer id;
    private Long gmtCreate;
    private Integer status;
    private Long notifier;
    private String notifierName;
    private String outerTitle;
    private Long outerId;
    private String typeName;
    private Integer type;
}
