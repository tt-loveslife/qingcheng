package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.pojo.system.Admin;
import com.qingcheng.service.system.AdminService;
import org.apache.commons.collections.map.AbstractMapDecorator;
import org.apache.zookeeper.proto.SetACLRequest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDetailServiceImpl implements UserDetailsService {
    @Reference
    private AdminService adminService;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        System.out.println("经过UserDetail服务");
        Map<String, Object> searchMap = new HashMap<>();
        searchMap.put("login_name", s);
        searchMap.put("status", "1");
        List<Admin> adminList = adminService.findList(searchMap);
        if (adminList == null || adminList.size() == 0){
            return null;
        }
        Admin admin = adminList.get(0);
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        return new User(s, admin.getPassword(), list);
    }
}
