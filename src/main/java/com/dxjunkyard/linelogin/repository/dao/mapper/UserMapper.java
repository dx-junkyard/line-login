package com.dxjunkyard.linelogin.repository.dao.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.dxjunkyard.linelogin.domain.resource.Login;
import com.dxjunkyard.linelogin.domain.resource.RegisterUserProperty;
import com.dxjunkyard.linelogin.domain.resource.UserProperty;

@Mapper
public interface UserMapper {
    UserProperty getUserProperty(String user_id);
    void registerUserProperty(RegisterUserProperty userProperty);
    void updateUserProperty(RegisterUserProperty userProperty);
    String login(Login login);
}
