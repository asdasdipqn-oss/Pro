package cn.edu.ccst.manpower_resource.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * 求职者认证令牌
 */
public class CandidateAuthenticationToken extends AbstractAuthenticationToken {

    private final Long userId;
    private final String username;

    public CandidateAuthenticationToken(String username, Long userId, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.username = username;
        this.userId = userId;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return this;
    }

    @Override
    public String getName() {
        return username;
    }

    @Override
    public String toString() {
        return username;
    }

    public Long getUserId() {
        return userId;
    }
}
