package cn.edu.ccst.manpower_resource.service.impl;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.common.ResultCode;
import cn.edu.ccst.manpower_resource.dto.SysUserDTO;
import cn.edu.ccst.manpower_resource.dto.SysUserQuery;
import cn.edu.ccst.manpower_resource.entity.EmpEmployee;
import cn.edu.ccst.manpower_resource.entity.OrgDepartment;
import cn.edu.ccst.manpower_resource.entity.SysRole;
import cn.edu.ccst.manpower_resource.entity.SysUser;
import cn.edu.ccst.manpower_resource.entity.SysUserRole;
import cn.edu.ccst.manpower_resource.exception.BusinessException;
import cn.edu.ccst.manpower_resource.mapper.EmpEmployeeMapper;
import cn.edu.ccst.manpower_resource.mapper.OrgDepartmentMapper;
import cn.edu.ccst.manpower_resource.mapper.SysRoleMapper;
import cn.edu.ccst.manpower_resource.mapper.SysUserMapper;
import cn.edu.ccst.manpower_resource.mapper.SysUserRoleMapper;
import cn.edu.ccst.manpower_resource.service.ISysUserService;
import cn.edu.ccst.manpower_resource.vo.UserInfoVO;
import cn.edu.ccst.manpower_resource.vo.UserWithEmployeeVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmpEmployeeMapper employeeMapper;
    private final OrgDepartmentMapper departmentMapper;

    @Override
    public List<SysRole> getUserRoles(Long userId) {
        return roleMapper.selectRolesByUserId(userId);
    }

    @Override
    public PageResult<UserInfoVO> pageList(SysUserQuery query) {
        Page<SysUser> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getUsername()), SysUser::getUsername, query.getUsername())
                .eq(query.getStatus() != null, SysUser::getStatus, query.getStatus())
                .eq(SysUser::getDeleted, 0)
                .orderByDesc(SysUser::getCreateTime);

        // 如果有角色筛选，需要通过子查询或使用自定义SQL
        if (query.getRoleId() != null) {
            wrapper.apply("id IN (SELECT user_id FROM sys_user_role WHERE role_id = {0})", query.getRoleId());
        }

        Page<SysUser> result = baseMapper.selectPage(page, wrapper);
        List<UserInfoVO> voList = result.getRecords().stream().map(this::toUserInfoVO).collect(Collectors.toList());
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), voList);
    }

    @Override
    public UserInfoVO getDetail(Long id) {
        SysUser user = baseMapper.selectById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmployeeId(user.getEmployeeId());
        vo.setStatus(user.getStatus());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setCreateTime(user.getCreateTime());

        // 获取员工名称
        if (user.getEmployeeId() != null) {
            EmpEmployee employee = employeeMapper.selectById(user.getEmployeeId());
            if (employee != null) {
                vo.setEmployeeName(employee.getEmpName());
                vo.setDeptId(employee.getDeptId());
                if (employee.getDeptId() != null) {
                    OrgDepartment department = departmentMapper.selectById(employee.getDeptId());
                    if (department != null) {
                        vo.setDepartmentName(department.getDeptName());
                    }
                }
            }
        }

        // 获取角色名称
        List<SysRole> roles = roleMapper.selectRolesByUserId(user.getId());
        if (roles != null && !roles.isEmpty()) {
            vo.setRoleNames(roles.stream().map(SysRole::getRoleName).collect(Collectors.toList()));
        }

        return vo;
    }

    @Override
    @Transactional
    public void createUser(SysUserDTO dto) {
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException(ResultCode.USER_EXIST);
        }

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmployeeId(dto.getEmployeeId());
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        user.setDeleted(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        baseMapper.insert(user);

        if (dto.getRoleId() != null) {
            saveUserRoles(user.getId(), List.of(dto.getRoleId()));
        }
    }

    @Override
    @Transactional
    public void updateUser(SysUserDTO dto) {
        SysUser user = baseMapper.selectById(dto.getId());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        if (StringUtils.hasText(dto.getUsername()) && !dto.getUsername().equals(user.getUsername())) {
            Long count = baseMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
            if (count > 0) {
                throw new BusinessException(ResultCode.USER_EXIST);
            }
            user.setUsername(dto.getUsername());
        }

        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            user.setPwdUpdateTime(LocalDateTime.now());
        }
        user.setEmployeeId(dto.getEmployeeId());
        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus());
        }
        user.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(user);

        if (dto.getRoleId() != null) {
            userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, dto.getId()));
            saveUserRoles(dto.getId(), List.of(dto.getRoleId()));
        }
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        SysUser user = baseMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        // 使用 MyBatis Plus 的逻辑删除
        removeById(id);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        SysUser user = baseMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(user);
    }

    @Override
    public void resetPassword(Long id, String newPassword) {
        SysUser user = baseMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPwdUpdateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(user);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPwdUpdateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(user);
    }

    @Override
    public void updateProfile(Long userId, String username) {
        SysUser user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        if (StringUtils.hasText(username) && !username.equals(user.getUsername())) {
            Long count = baseMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
            if (count > 0) {
                throw new BusinessException(ResultCode.USER_EXIST);
            }
            user.setUsername(username);
        }
        user.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(user);
    }

    private void saveUserRoles(Long userId, List<Long> roleIds) {
        for (Long roleId : roleIds) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRole.setCreateTime(LocalDateTime.now());
            userRoleMapper.insert(userRole);
        }
    }

    private UserInfoVO toUserInfoVO(SysUser user) {
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmployeeId(user.getEmployeeId());
        vo.setStatus(user.getStatus());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setCreateTime(user.getCreateTime());

        List<SysRole> roles = roleMapper.selectRolesByUserId(user.getId());
        vo.setRoles(roles.stream().map(SysRole::getRoleCode).collect(Collectors.toList()));
        vo.setRoleNames(roles.stream().map(SysRole::getRoleName).collect(Collectors.toList()));

        // 获取员工名称
        if (user.getEmployeeId() != null) {
            EmpEmployee employee = employeeMapper.selectById(user.getEmployeeId());
            if (employee != null) {
                vo.setEmployeeName(employee.getEmpName());
            }
        }

        return vo;
    }
}
