package cn.edu.ccst.manpower_resource.security;

import cn.edu.ccst.manpower_resource.entity.HrCandidate;
import cn.edu.ccst.manpower_resource.entity.SysMenu;
import cn.edu.ccst.manpower_resource.entity.SysUser;
import cn.edu.ccst.manpower_resource.mapper.HrCandidateMapper;
import cn.edu.ccst.manpower_resource.mapper.SysUserMapper;
import cn.edu.ccst.manpower_resource.mapper.SysRoleMapper;
import cn.edu.ccst.manpower_resource.mapper.SysMenuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义 UserDetailsService，同时支持员工和求职者用户
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final SysUserMapper sysUserMapper;
    private final HrCandidateMapper candidateMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysMenuMapper sysMenuMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user: {}", username);

        // 先尝试从员工表查询
        SysUser sysUser = sysUserMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
                        .eq(SysUser::getDeleted, 0)
        );

        if (sysUser != null && sysUser.getId() != null) {
            log.debug("Found sys_user: {}, returning LoginUser with EMPLOYEE type", username);
            // 员工用户：返回 LoginUser 对象（查询角色和权限）
            List<String> roles = getUserRoles(sysUser.getId());
            List<String> permissions = getUserPermissions(sysUser.getId());
            return new LoginUser(sysUser, roles, permissions);
        }

        // 员工表找不到，尝试从求职者表查询
        log.debug("User not in sys_user, checking hr_candidate");
        HrCandidate candidate = candidateMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<HrCandidate>()
                            .eq(HrCandidate::getUsername, username)
                            .eq(HrCandidate::getDeleted, 0)
            );

        if (candidate == null || candidate.getId() == null) {
            log.error("User not found: {}", username);
            throw new UsernameNotFoundException("User not found: " + username);
        }

        log.debug("Found candidate: {}, returning LoginUser with CANDIDATE type", username);
        // 求职者用户：返回 LoginUser 对象
        return new LoginUser(candidate);
    }

    private List<String> getUserRoles(Long userId) {
        List<cn.edu.ccst.manpower_resource.entity.SysRole> roles = sysRoleMapper.selectRolesByUserId(userId);
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }
        return roles.stream()
                .map(cn.edu.ccst.manpower_resource.entity.SysRole::getRoleCode)
                .collect(Collectors.toList());
    }

    private List<String> getUserPermissions(Long userId) {
        List<SysMenu> menus = sysMenuMapper.selectMenusByUserId(userId);
        if (menus == null || menus.isEmpty()) {
            return Collections.emptyList();
        }
        // 从菜单中提取权限标识
        return menus.stream()
                .filter(m -> m.getPermission() != null && !m.getPermission().isEmpty())
                .map(SysMenu::getPermission)
                .collect(Collectors.toList());
    }
}
