package com.dxjunkyard.linelogin.repository.dao.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TokenMapper {
    void registerToken(String session_id,String user_id);
    String getUserId(String token);
    String getTokenByUserId(String user_id);
}
