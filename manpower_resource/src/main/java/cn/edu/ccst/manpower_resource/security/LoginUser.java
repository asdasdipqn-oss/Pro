package cn.edu.ccst.manpower_resource.security;

import cn.edu.ccst.manpower_resource.entity.HrCandidate;
import cn.edu.ccst.manpower_resource.entity.SysUser;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class LoginUser implements UserDetails {

    private SysUser user;
    private HrCandidate candidate;
    private List<String> roles;
    private List<String> permissions;
    private String userType;

    // 员工用户构造器
    public LoginUser(SysUser user, List<String> roles, List<String> permissions) {
        this.user = user;
        this.roles = roles;
        this.permissions = permissions;
        this.userType = "EMPLOYEE";
    }

    // 求职者用户构造器
    public LoginUser(HrCandidate candidate) {
        this.candidate = candidate;
        this.userType = "CANDIDATE";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if ("CANDIDATE".equals(userType)) {
            return List.of(new SimpleGrantedAuthority("ROLE_CANDIDATE"));
        }
        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
        authorities.addAll(permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList()));
        return authorities;
    }

    @Override
    public String getPassword() {
        if ("CANDIDATE".equals(userType)) {
            return candidate.getPassword();
        }
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        if ("CANDIDATE".equals(userType)) {
            return candidate.getUsername();
        }
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        if ("CANDIDATE".equals(userType)) {
            return candidate.getStatus() == 1;
        }
        return user.getStatus() == 1;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        if ("CANDIDATE".equals(userType)) {
            return candidate.getDeleted() == 0;
        }
        return user.getDeleted() == 0;
    }

    public Long getUserId() {
        if ("CANDIDATE".equals(userType)) {
            return candidate.getId();
        }
        return user.getId();
    }

    public SysUser getUser() {
        return user;
    }

    public HrCandidate getCandidate() {
        return candidate;
    }

    public String getUserType() {
        return userType;
    }
}
