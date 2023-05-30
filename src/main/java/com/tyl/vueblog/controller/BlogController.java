package com.tyl.vueblog.controller;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tyl.vueblog.common.Result;
import com.tyl.vueblog.entity.Blog;
import com.tyl.vueblog.entity.User;
import com.tyl.vueblog.service.BlogService;
import com.tyl.vueblog.shiro.AccountProfile;
import net.sf.jsqlparser.expression.LongValue;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author tyl
 * @since 2023-03-25
 */
@RestController
public class BlogController {

    @Autowired(required = false)
    private RedisTemplate redisTemplate;

    @Autowired
    BlogService blogService;



    @GetMapping("/blogs")
    public Result<IPage> list(@RequestParam(defaultValue = "1") Integer currentPage) {

        Page page = new Page(currentPage, 5);
        IPage pageData = blogService.page(page, new QueryWrapper<Blog>().orderByDesc("created"));

        return Result.success(pageData);
    }

//    @RequiresAuthentication
    @GetMapping("/blog/{id}/{userId}")
    public Result<Map<String,Object>> detail(@PathVariable(name = "id") Integer  id,@PathVariable(name = "userId") String userId) {
        Blog blog = blogService.getById(id);
        Integer incr=null;
        Boolean flag=null;
        Map<String,Object> map=new HashMap<>();
        if(blog==null){
           throw new RuntimeException("博客已被删除");
        }
        Object o = redisTemplate.opsForValue().get(blog.getId() + "_love");

        Object o2=redisTemplate.opsForValue().get(userId+"_"+blog.getId()+"_flag");

        if(o==null){
            redisTemplate.opsForValue().set(blog.getId()+"_love",0);
            incr=(Integer) redisTemplate.opsForValue().get(blog.getId()+"_love");

        }else {
            incr=(Integer) redisTemplate.opsForValue().get(blog.getId()+"_love");

        }

        if(o2==null){
            redisTemplate.opsForValue().set(userId+"_"+blog.getId()+"_flag",false);
            flag=(Boolean) redisTemplate.opsForValue().get(userId+"_"+blog.getId()+"_flag");
        }else{
            flag=(Boolean) redisTemplate.opsForValue().get(userId+"_"+blog.getId()+"_flag");
        }
        map.put("data",blog);
        map.put("total",incr);
        map.put("flag",flag);
        return Result.success(map);
    }



    @RequiresAuthentication
    @PostMapping("/blog/edit")
    public Result edit(@RequestBody Blog blog, HttpServletRequest request) {

         String jwt = request.getHeader("Authorization");

          Integer o = (Integer) redisTemplate.opsForValue().get(jwt);

             Blog temp = null;
             temp = new Blog();
             temp.setUserId(o);
             temp.setCreated(LocalDateTime.now());
             temp.setStatus(0);

             BeanUtil.copyProperties(blog, temp, "id", "userId", "created", "status");
             blogService.saveOrUpdate(temp);
             return Result.success("操作成功");
    }


    @GetMapping("/blog/addlove")
    public Result<Map<String,Object>> addlove(@RequestParam("blogId") Integer blogId,@RequestParam("userId") String userId){
        Map<String,Object> map=new HashMap<>();
        Long data = redisTemplate.opsForValue().increment(blogId + "_love", 1);
        redisTemplate.opsForValue().set(userId+"_"+blogId+"_flag",true);

        Boolean flag=(Boolean)redisTemplate.opsForValue().get(userId+"_"+blogId+"_flag");
        map.put("data",data);
        map.put("flag",flag);
        return Result.success(map);
    }

    @GetMapping("/blog/delove")
    public Result<Map<String,Object>> delove(@RequestParam("blogId") Integer blogId,@RequestParam("userId") String userId){
        Map<String,Object> map=new HashMap<>();
        Long data = redisTemplate.opsForValue().decrement(blogId + "_love", 1);

        redisTemplate.opsForValue().set(userId+"_"+blogId+"_flag",false);

        Boolean flag=(Boolean)redisTemplate.opsForValue().get(userId+"_"+blogId+"_flag");
        map.put("data",data);
        map.put("flag",flag);
        return Result.success(map);
    }

}
