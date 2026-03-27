package com.daox.online.entity.mysql;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class AuditLogs implements Serializable {

    private String id;

    private String action;

    private String resourceType;

    private String resourceId;

    private String status;

    private String message;

    private String operatorId;

    private String operatorIdentifier;

    private String operatorName;

    private String operatorRole;

    private String requestIp;

    private String requestMethod;

    private String requestPath;

    private String requestSource;

    private String beforeSnapshot;

    private String afterSnapshot;

    private Date createdAt;
}
