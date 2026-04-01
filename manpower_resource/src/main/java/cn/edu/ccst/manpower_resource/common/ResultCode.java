package cn.edu.ccst.manpower_resource.common;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public enum ResultCode {
    SUCCESS(200, "success"),
    FAIL(500, "fail"),
    PARAM_ERROR(1001, "param error"),
    PARAM_NOT_VALID(1002, "param not valid"),
    PARAM_IS_BLANK(1003, "param is blank"),
    PARAM_TYPE_ERROR(1004, "param type error"),
    USER_NOT_LOGIN(2001, "user not login"),
    USER_LOGIN_ERROR(2002, "login error"),
    USER_ACCOUNT_FORBIDDEN(2003, "account forbidden"),
    USER_NOT_EXIST(2004, "user not exist"),
    USER_EXIST(2005, "user exist"),
    USER_PASSWORD_ERROR(2006, "password error"),
    USER_TOKEN_EXPIRED(2007, "token expired"),
    USER_TOKEN_INVALID(2008, "token invalid"),
    NO_PERMISSION(3001, "no permission"),
    ACCESS_DENIED(3002, "access denied"),
    DATA_NOT_EXIST(4001, "data not exist"),
    DATA_ALREADY_EXIST(4002, "data already exist"),
    DATA_ERROR(4003, "data error"),
    SYSTEM_ERROR(5001, "system error"),
    SYSTEM_BUSY(5002, "system busy");

    private final Integer code;
    private final String message;
}
