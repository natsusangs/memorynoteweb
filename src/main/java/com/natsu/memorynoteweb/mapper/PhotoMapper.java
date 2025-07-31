package com.natsu.memorynoteweb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.natsu.memorynoteweb.entity.Photo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PhotoMapper extends BaseMapper<Photo> {

    @Select("SELECT * FROM photo WHERE uid = #{uid}")
    List<Photo> selectByUserId(int uid); // 根据用户ID查询照片列表

    @Insert("INSERT INTO photo (uid, path, gps_data, gps_time, created_time) " +
            "VALUES (#{uid}, #{path}, #{gpsData}, #{gpsTime}, #{uploadTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertPhoto(Photo photo);

}
