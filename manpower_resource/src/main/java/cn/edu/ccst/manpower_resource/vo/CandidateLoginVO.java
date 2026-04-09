package cn.edu.ccst.manpower_resource.vo;

import lombok.Data;
import java.io.Serializable;

/**
 * 求职者登录响应VO
 */
@Data
public class CandidateLoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String token;

    private String tokenType;

    private String username;

    private String userType;

    private String realName;
}
