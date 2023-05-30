package com.tyl.vueblog.service.impl;

import com.tyl.vueblog.entity.User;
import com.tyl.vueblog.mapper.UserMapper;
import com.tyl.vueblog.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author tyl
 * @since 2023-03-25
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}
