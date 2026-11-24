package com.fyf.mapper;

import com.fyf.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LoginMapper {
    @Select("select id,username,password from user where username=#{username} and status=1")
    User selectUserByUsername(String username);

    int insertUser(User user);
}
