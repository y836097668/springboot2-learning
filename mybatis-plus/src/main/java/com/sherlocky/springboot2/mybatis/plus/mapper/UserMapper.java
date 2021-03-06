package com.sherlocky.springboot2.mybatis.plus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sherlocky.springboot2.mybatis.plus.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper extends BaseMapper<User> {

    public List<User> selectByName(@Param("name") String name);
}