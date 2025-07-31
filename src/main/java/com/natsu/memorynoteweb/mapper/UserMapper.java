package com.natsu.memorynoteweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import com.natsu.memorynoteweb.entity.User;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {//注意表名要和类型相同

    @Select("SELECT * FROM t_users WHERE username = #{username}")
    User selectUserByUsername(String username);

    @Select("SELECT * FROM t_users")
    List<User> find();

    @Insert("INSERT INTO t_users(username, password) VALUES(#{username}, #{password})") //方法的参数是可以被放入的，需要使用#号
    int insert(User user);

    @Select("SELECT * FROM t_users")
    @Results(
            {
                    @Result(column = "id", property = "id"),
                    @Result(column = "username", property = "username"),
                    @Result(column = "password", property = "password"),
                    @Result(column = "id", property = "photos", javaType = List.class,
                            many = @Many(select = "com.natsu.memorynoteweb.mapper.PhotoMapper.selectByUserId"))
            }
    )
    List<User> selectAllUserAndPhotos(); //查询所有用户及其照片信息
}
