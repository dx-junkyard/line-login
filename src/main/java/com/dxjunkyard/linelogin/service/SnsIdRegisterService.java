package com.dxjunkyard.linelogin.service;


import com.dxjunkyard.linelogin.repository.dao.mapper.SnsRegisterMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SnsIdRegisterService {
    @Autowired
    private SnsRegisterMapper snsRegisterMapper;

    public void registerSnsId(String sns_id, String user_id) {
        snsRegisterMapper.registerSnsId(sns_id, user_id);
        return;
    }
}
