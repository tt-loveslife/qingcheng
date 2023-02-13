package com.qingcheng.controller.system;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.entity.PageResult;
import com.qingcheng.entity.Result;
import com.qingcheng.pojo.system.Admin;
import com.qingcheng.pojo.system.AdminAndRoles;
import com.qingcheng.service.system.AdminService;
import com.qingcheng.util.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Reference
    private AdminService adminService;

    @GetMapping("/findAll")
    public List<Admin> findAll(){
        return adminService.findAll();
    }

    @GetMapping("/findPage")
    public PageResult<Admin> findPage(int page, int size){
        return adminService.findPage(page, size);
    }

    @PostMapping("/findList")
    public List<Admin> findList(@RequestBody Map<String,Object> searchMap){
        return adminService.findList(searchMap);
    }

    @PostMapping("/findPage")
    public PageResult<Admin> findPage(@RequestBody Map<String,Object> searchMap,int page, int size){
        return  adminService.findPage(searchMap,page,size);
    }

    @GetMapping("/findById")
    public AdminAndRoles findById(Integer id){
        return adminService.findById(id);
    }


    @PostMapping("/add")
    public Result add(@RequestBody AdminAndRoles adminAndRoles){
        String pass = adminAndRoles.getAdmin().getPassword();
        String gensalt = BCrypt.gensalt();
        String hashpw = BCrypt.hashpw(pass, gensalt);
        adminAndRoles.getAdmin().setPassword(hashpw);
        if (adminAndRoles.getAdmin().getId() != null && !"".equals(adminAndRoles.getAdmin().getId())){
            adminService.update(adminAndRoles);
        }else{
            adminService.add(adminAndRoles);
        }
        return new Result();
    }

    @PostMapping("/update")
    public Result update(@RequestBody AdminAndRoles adminAndRoles){
        if (adminAndRoles != null && adminAndRoles.getAdmin() != null){
            if (!"".equals(adminAndRoles.getAdmin().getPassword())){
                String gensalt = BCrypt.gensalt();
                String hashpw = BCrypt.hashpw(adminAndRoles.getAdmin().getPassword(), gensalt);
                adminAndRoles.getAdmin().setPassword(hashpw);
                adminService.update(adminAndRoles);
            }
        }
        return new Result();
    }

    @GetMapping("/delete")
    public Result delete(Integer id){
        adminService.delete(id);
        return new Result();
    }

}
