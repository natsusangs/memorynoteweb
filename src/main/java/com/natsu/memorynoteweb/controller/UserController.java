package com.natsu.memorynoteweb.controller;

import com.natsu.memorynoteweb.entity.User;
import com.natsu.memorynoteweb.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
public class UserController {

    @Autowired //自动注入userMapper
    private UserMapper userMapper;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User user){
        System.out.println(user);
        User selectedUser = userMapper.selectUserByUsername(user.getUsername());

        String password = selectedUser.getPassword();

        Map<String, Object> result = new HashMap<>();

        if(password == null){
            result.put("success", false);
            result.put("message", "用户名不存在");
            return result;
        } else if(!password.equals(user.getPassword())){
            result.put("success", false);
            result.put("message", "密码错误");
            return result;
        } else {
            // 登录成功，返回token和用户信息
            result.put("success", true);
            result.put("message", "登录成功");
            result.put("token", "jwt-token-" + System.currentTimeMillis()); // 生成实际的JWT token

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("username", selectedUser.getUsername());
            userInfo.put("id", selectedUser.getId());
            result.put("userInfo", userInfo);

            return result;
        }
    }

    @GetMapping("/user")
    public List query(){
        List<User> list = userMapper.selectList(null);
        System.out.println(list);
        return list; //自动转换为json格式
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> save(@RequestBody User user) {
        Map<String, String> response = new HashMap<>();

        try {
            int i = userMapper.insert(user);
            if(i > 0){
                response.put("status", "success");
                response.put("message", "注册成功");
                return ResponseEntity.ok(response);
            } else{
                response.put("status", "fail");
                response.put("message", "注册失败");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (DuplicateKeyException e) {
            // 捕获重复键异常，返回用户名已占用的提示
            response.put("status", "error");
            response.put("message", "用户名已占用");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            // 捕获其他异常
            response.put("status", "error");
            response.put("message", "注册失败，请稍后重试");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/user/all")
    public List<User> queryAll(){
        List<User> list = userMapper.selectAllUserAndPhotos();
        System.out.println(list);
        return list;
    }



}
