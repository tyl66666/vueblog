package com.tyl.vueblog.controller;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tyl.vueblog.common.Result;
import com.tyl.vueblog.common.dto.LoginDto;
import com.tyl.vueblog.entity.User;
import com.tyl.vueblog.service.UserService;
import com.tyl.vueblog.util.JwtUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
public class AccountController {
    private String jwt=null;


    @Autowired
    private UserService userService;


    // RedisTemplate（使用这个需要改变序列化）  StringRedisTemplate（不需要改变序列化）
     @Autowired(required = false)
    private RedisTemplate redisTemplate;


     @Autowired(required = false)
     private StringRedisTemplate template;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")

    public Result<User> login(@Validated @RequestBody LoginDto loginDto, HttpServletResponse response) {
        //可以改为shiro内部的 的登入 login

        User user = userService.getOne(new QueryWrapper<User>().eq("username", loginDto.getUsername()));
         if(user==null){
             throw new RuntimeException("登入失败, 密码或账号不正确");
         }
        if(!user.getPassword().equals(SecureUtil.md5(loginDto.getPassword()))) {
            return Result.fail("密码错误！");
        }
        //产生token 放进head中
        jwt = jwtUtils.generateToken(user.getId());
        response.setHeader("Authorization", jwt);


//        问题场景：
//        和前端同事联调接口中，前端同事反映说在跨域请求的情况下无法获取 head中 token的值，后来找到解决办法
//
//        解决：
//        1】在登录拦截器处理类中的响应对象，把token对象暴露出来即可
//        2】关键代码 response.setHeader("Access-Control-Expose-Headers", "token"); 示例如下：



        response.setHeader("Access-Control-Expose-Headers", "Authorization");

        //TODO 还需要将token 存入redis 中 确保不是随意给的token (可以解决 为登入 但head中传了 token)
        //  解决方法 : 将redis中 token 与传过来的对比

        //将数据保存到redis  以后可以改进成为  用shiro自带的redis解决
       redisTemplate.opsForValue().set(jwt,user.getId());


        // 用户可以另一个接口
        return Result.success(user);
    }

    // 退出
    @RequiresAuthentication
    @GetMapping("/logout")
    public Result logout() {
        SecurityUtils.getSubject().logout();
        redisTemplate.delete(jwt);
        return Result.success("退出成功");
    }
}
