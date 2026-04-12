package cn.edu.ccst.manpower_resource.security;

import cn.edu.ccst.manpower_resource.entity.SysMenu;
import cn.edu.ccst.manpower_resource.entity.SysRole;
import cn.edu.ccst.manpower_resource.entity.SysUser;
import cn.edu.ccst.manpower_resource.mapper.SysMenuMapper;
import cn.edu.ccst.manpower_resource.mapper.SysRoleMapper;
import cn.edu.ccst.manpower_resource.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
                        .eq(SysUser::getDeleted, 0)
        );
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        List<SysRole> roles = roleMapper.selectRolesByUserId(user.getId());
        List<String> roleNames = roles.stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toList());

        List<SysMenu> menus = menuMapper.selectMenusByUserId(user.getId());
        List<String> permissions = menus.stream()
                .map(SysMenu::getPermission)
                .filter(p -> p != null && !p.isEmpty())
                .collect(Collectors.toList());

        return new LoginUser(user, roleNames, permissions);
    }
}
