package com.tyl.vueblog;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tyl.vueblog.entity.User;
import com.tyl.vueblog.mapper.UserMapper;
import com.tyl.vueblog.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class VueblogApplicationTests {

    @Autowired(required = false)
    private UserService  m;

    @Test
    void contextLoads() {
        LambdaQueryWrapper<User> wrapper =new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername,"markerhub");
        User user = m.getOne(wrapper);
        System.out.println(user.toString());
    }

}
