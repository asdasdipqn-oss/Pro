package cn.edu.ccst.manpower_resource.service;

import cn.edu.ccst.manpower_resource.dto.LoginRequest;
import cn.edu.ccst.manpower_resource.dto.RegisterRequest;
import cn.edu.ccst.manpower_resource.vo.LoginVO;
import cn.edu.ccst.manpower_resource.vo.UserInfoVO;

public interface AuthService {

    LoginVO login(LoginRequest request, String ip);

    void register(RegisterRequest request);

    UserInfoVO getCurrentUser();

    void logout();
}
