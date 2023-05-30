package com.tyl.vueblog.shiro;


import cn.hutool.json.JSONUtil;
import com.tyl.vueblog.common.Result;
import com.tyl.vueblog.util.JwtUtils;
import io.jsonwebtoken.Claims;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtFilter extends AuthenticatingFilter {


    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest request=(HttpServletRequest)servletRequest;
        String jwt=request.getHeader("Authorization");  //因为这里前端发请求会将jwt 放进head参数中  所以这个从head获取的就是 jwt  这个也是jwt 比起token的优势
        if(!StringUtils.hasText(jwt)){
            return null;  //如果是空直接跳过
        }
        //TODO  在这还可以判断是否redis中也用 token 可以保证黑客 直接使用token进行操作
        return new JwtToken(jwt); // 这里是需要自定义token的原因
    }
    //判断token是否过期
    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest request=(HttpServletRequest)servletRequest;
        String jwt=request.getHeader("Authorization");
        if(!StringUtils.hasText(jwt)){
            return true;  //如果是空直接跳过验证
        }else {
            //1 校验 jwt

            //这个是解析token之后返回的对象
            Claims claim = jwtUtils.getClaimByToken(jwt);
            if(claim==null||jwtUtils.isTokenExpired(claim.getExpiration())){
              throw new RuntimeException("token失效,请重新登录");
            }

            //2 执行登入
            return executeLogin(servletRequest,servletResponse);
        }
    }

    //登入失败将json数据传给前端
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        HttpServletResponse httpServletResponse= (HttpServletResponse) response;
        Throwable throwable =e.getCause()==null ? e:e.getCause();

        Result result=Result.fail(throwable.getMessage());

        //这里也可以不使用hutool 改为fasjson  因为springboot默认的是jackson 没有处理的方法(只用才控制层才可以使用) 在这里不可以转
        //可以将任意对象（Bean、Map、集合等）直接转换为JSON字符串。 如果对象是有序的Map等对象，则转换后的JSON字符串也是有序的
        String json = JSONUtil.toJsonStr(result);
        try {
            httpServletResponse.getWriter().println(json);
        } catch (IOException ioException) {

        }
        return false;
    }

    /**
     * 对跨域提供支持
     */
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = WebUtils.toHttp(request);
        HttpServletResponse httpServletResponse = WebUtils.toHttp(response);
        httpServletResponse.setHeader("Access-control-Allow-Origin", httpServletRequest.getHeader("Origin"));
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,PUT,DELETE");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", httpServletRequest.getHeader("Access-Control-Request-Headers"));
        // 跨域时会首先发送一个OPTIONS请求，这里我们给OPTIONS请求直接返回正常状态
        if (httpServletRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            httpServletResponse.setStatus(org.springframework.http.HttpStatus.OK.value());
            return false;
        }
        return super.preHandle(request, response);
    }

}
