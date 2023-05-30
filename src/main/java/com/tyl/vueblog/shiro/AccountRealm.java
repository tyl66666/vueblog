package com.tyl.vueblog.shiro;


import cn.hutool.core.bean.BeanUtil;
import com.tyl.vueblog.entity.User;
import com.tyl.vueblog.service.UserService;
import com.tyl.vueblog.util.JwtUtils;
import io.jsonwebtoken.Claims;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AccountRealm extends AuthorizingRealm {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserService userService;

    //判断传过来是否为JwtToken 才可以实现强转
    @Override
    public boolean supports(AuthenticationToken token) {
        return  token instanceof JwtToken;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        return null;
    }


    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {

        JwtToken jwtToken=(JwtToken)authenticationToken;

        Claims claimByToken = jwtUtils.getClaimByToken((String) jwtToken.getPrincipal());
        String userId = claimByToken.getSubject();
        User user = userService.getById(Long.valueOf(userId));
        if(user==null){
            throw new UnknownAccountException("账户不存在");
        }

        if(user.getStatus()==-1){
            throw new LockedAccountException("账户被锁定");
        }

        AccountProfile profile=new AccountProfile();
        BeanUtil.copyProperties(user,profile);

        return new SimpleAuthenticationInfo(profile, jwtToken.getCredentials(),getName() );
    }
}
