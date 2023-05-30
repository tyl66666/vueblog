package com.tyl.vueblog.shiro;

import org.apache.shiro.authc.AuthenticationToken;

public class JwtToken implements AuthenticationToken {

    private String token;

    public JwtToken(String jwt){
        this.token=jwt;
    }

    //用户名
    @Override
    public Object getPrincipal() {
        return token;
    }

    //密码
    @Override
    public Object getCredentials() {
        return token;
    }
}
