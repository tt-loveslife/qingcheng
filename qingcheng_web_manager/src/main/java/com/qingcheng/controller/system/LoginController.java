package com.qingcheng.controller.system;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.pojo.system.Admin;
import com.qingcheng.service.system.AdminService;
import com.qingcheng.util.BCrypt;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoginController {

    @Reference
    private AdminService adminService;

    @RequestMapping("/name")
    public String showName(){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        return name;
    }

    @RequestMapping("/checkPass")
    public boolean checkPass(@RequestBody Map<String, String> ruleForm){
        String username = ruleForm.get("username");
        String inputPass = ruleForm.get("originPassword");
        Map<String, Object> searchMap = new HashMap<>();
        searchMap.put("username", username);
        List<Admin> list = adminService.findList(searchMap);
        if (list == null || list.size() == 0){
            return false;
        }
        return BCrypt.checkpw(inputPass, list.get(0).getPassword());
    }

    @RequestMapping("/updatePass")
    public boolean updatePass(@RequestBody Map<String, String> ruleForm){
        try{
            String username = ruleForm.get("username");
            String inputPass = ruleForm.get("newPassword");
            Map<String, Object> searchMap = new HashMap<>();
            searchMap.put("username", username);
            List<Admin> list = adminService.findList(searchMap);
            if (list == null || list.size() == 0){
                return false;
            }
            String gensalt = BCrypt.gensalt();
            String hashpw = BCrypt.hashpw(inputPass, gensalt);
            list.get(0).setPassword(hashpw);
            adminService.update(list.get(0));
        }catch (Exception e){
            return false;
        }
        return true;
    }
}
