package cn.edu.ccst.manpower_resource.service.impl;

import cn.edu.ccst.manpower_resource.common.ResultCode;
import cn.edu.ccst.manpower_resource.dto.LoginRequest;
import cn.edu.ccst.manpower_resource.dto.RegisterRequest;
import cn.edu.ccst.manpower_resource.entity.EmpEmployee;
import cn.edu.ccst.manpower_resource.entity.OrgDepartment;
import cn.edu.ccst.manpower_resource.entity.OrgPosition;
import cn.edu.ccst.manpower_resource.entity.SysUser;
import cn.edu.ccst.manpower_resource.entity.SysUserRole;
import cn.edu.ccst.manpower_resource.exception.BusinessException;
import cn.edu.ccst.manpower_resource.mapper.EmpEmployeeMapper;
import cn.edu.ccst.manpower_resource.mapper.OrgDepartmentMapper;
import cn.edu.ccst.manpower_resource.mapper.OrgPositionMapper;
import cn.edu.ccst.manpower_resource.mapper.SysUserMapper;
import cn.edu.ccst.manpower_resource.mapper.SysUserRoleMapper;
import cn.edu.ccst.manpower_resource.security.LoginUser;
import cn.edu.ccst.manpower_resource.service.AuthService;
import cn.edu.ccst.manpower_resource.util.JwtUtil;
import cn.edu.ccst.manpower_resource.vo.LoginVO;
import cn.edu.ccst.manpower_resource.vo.UserInfoVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmpEmployeeMapper empEmployeeMapper;
    private final OrgPositionMapper orgPositionMapper;
    private final OrgDepartmentMapper orgDepartmentMapper;

    @Override
    public LoginVO login(LoginRequest request, String ip) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        String userType = loginUser.getUserType();

        // 处理员工登录
        if ("EMPLOYEE".equals(userType)) {
            SysUser user = loginUser.getUser();

            // 更新最后登录时间和IP
            user.setLastLoginTime(LocalDateTime.now());
            user.setLastLoginIp(ip);
            userMapper.updateById(user);

            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), "EMPLOYEE");

            // 获取用户部门信息
            Long deptId = null;
            String departmentName = null;
            String employeeName = null;
            if (user.getEmployeeId() != null) {
                EmpEmployee employee = empEmployeeMapper.selectById(user.getEmployeeId());
                if (employee != null) {
                    employeeName = employee.getEmpName();
                    if (employee.getDeptId() != null) {
                        deptId = employee.getDeptId();
                        OrgDepartment department = orgDepartmentMapper.selectById(deptId);
                        if (department != null) {
                            departmentName = department.getDeptName();
                        }
                    }
                }
            }

            return LoginVO.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .userId(user.getId())
                    .username(user.getUsername())
                    .employeeName(employeeName)
                    .roles(loginUser.getRoles())
                    .permissions(loginUser.getPermissions())
                    .deptId(deptId)
                    .departmentName(departmentName)
                    .userType(userType)
                    .build();
        }

        // 处理求职者登录
        return LoginVO.builder()
                .token(jwtUtil.generateToken(loginUser.getUserId(), loginUser.getUsername(), "CANDIDATE"))
                .tokenType("Bearer")
                .userId(loginUser.getUserId())
                .username(loginUser.getUsername())
                .roles(List.of("CANDIDATE"))
                .permissions(List.of())
                .deptId(null)
                .departmentName(null)
                .userType(userType)
                .build();
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, request.getUsername())
        );
        if (count > 0) {
            throw new BusinessException(ResultCode.USER_EXIST);
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmployeeId(request.getEmployeeId());
        user.setStatus(1);
        user.setDeleted(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);

        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(4L);
        userRole.setCreateTime(LocalDateTime.now());
        userRoleMapper.insert(userRole);
    }

    @Override
    public UserInfoVO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ResultCode.USER_NOT_LOGIN);
        }

        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        SysUser user = loginUser.getUser();

        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmployeeId(user.getEmployeeId());
        vo.setStatus(user.getStatus());

        // 查询员工岗位和部门信息
        if (user.getEmployeeId() != null) {
            EmpEmployee employee = empEmployeeMapper.selectById(user.getEmployeeId());
            if (employee != null) {
                vo.setEmployeeName(employee.getEmpName());
                vo.setDeptId(employee.getDeptId());
                if (employee.getDeptId() != null) {
                    OrgDepartment department = orgDepartmentMapper.selectById(employee.getDeptId());
                    if (department != null) {
                        vo.setDepartmentName(department.getDeptName());
                    }
                }
                if (employee.getPositionId() != null) {
                    OrgPosition position = orgPositionMapper.selectById(employee.getPositionId());
                    if (position != null) {
                        vo.setPositionName(position.getPositionName());
                    } else {
                        vo.setPositionName("未设置岗位");
                    }
                } else {
                    vo.setPositionName("未设置岗位");
                }
            } else {
                vo.setPositionName("未设置岗位");
            }
        } else {
            vo.setPositionName("未设置岗位");
        }

        vo.setRoles(loginUser.getRoles());
        vo.setPermissions(loginUser.getPermissions());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }

    @Override
    public void logout() {
        SecurityContextHolder.clearContext();
    }
}
