package com.qingcheng.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qingcheng.dao.AdminMapper;
import com.qingcheng.dao.AdminRoleMapper;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.system.Admin;
import com.qingcheng.pojo.system.AdminAndRoles;
import com.qingcheng.pojo.system.AdminRole;
import com.qingcheng.service.system.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(interfaceClass = AdminService.class)
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private AdminRoleMapper adminRoleMapper;
    /**
     * 返回全部记录
     * @return
     */
    public List<Admin> findAll() {
        return adminMapper.selectAll();
    }

    /**
     * 分页查询
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    public PageResult<Admin> findPage(int page, int size) {
        PageHelper.startPage(page,size);
        Page<Admin> admins = (Page<Admin>) adminMapper.selectAll();
        return new PageResult<Admin>(admins.getTotal(),admins.getResult());
    }

    /**
     * 条件查询
     * @param searchMap 查询条件
     * @return
     */
    public List<Admin> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return adminMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    public PageResult<Admin> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        Page<Admin> admins = (Page<Admin>) adminMapper.selectByExample(example);
        return new PageResult<Admin>(admins.getTotal(),admins.getResult());
    }

    /**
     * 根据Id查询
     * @param id
     * @return
     */
    public AdminAndRoles findById(Integer id) {
        Admin admin = adminMapper.selectByPrimaryKey(id);
        Example example = new Example(AdminRole.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("adminId", admin.getId());
        List<AdminRole> adminRoles = adminRoleMapper.selectByExample(example);
        AdminAndRoles adminAndRoles = new AdminAndRoles();
        adminAndRoles.setAdmin(admin);
        List<Integer> roleIds = new ArrayList<Integer>();
        for(AdminRole adminRole:adminRoles){
            roleIds.add(adminRole.getRoleId());
        }
        adminAndRoles.setRoleIds(roleIds);
        return adminAndRoles;
    }

    /**
     * 新增
     * @param admin
     */
    @Transactional
    public void add(AdminAndRoles adminAndRoles) {
        Admin admin = adminAndRoles.getAdmin();
        adminMapper.insertSelective(admin);
        List<Integer> roleIds = adminAndRoles.getRoleIds();
        for(Integer roleId:roleIds){
            AdminRole adminRole = new AdminRole();
            adminRole.setAdminId(admin.getId());
            adminRole.setRoleId(roleId);
            adminRoleMapper.insertSelective(adminRole);
        }
    }

    /**
     * 修改
     *
     * @param adminAndRoles
     */
    @Transactional
    public void update(AdminAndRoles adminAndRoles) {
        // 先删除中间表的所有数据
        Integer adminId = adminAndRoles.getAdmin().getId();
        Example example = new Example(AdminRole.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("adminId", adminId);
        adminRoleMapper.deleteByExample(example);

        adminMapper.updateByPrimaryKeySelective(adminAndRoles.getAdmin());
        for (Integer roleId:adminAndRoles.getRoleIds()){
            AdminRole adminRole = new AdminRole();
            adminRole.setAdminId(adminId);
            adminRole.setRoleId(roleId);
            adminRoleMapper.insert(adminRole);
        }
    }

    /**
     *  删除
     * @param id
     */
    public void delete(Integer id) {
        adminMapper.deleteByPrimaryKey(id);
    }

    public boolean checkPass(String username, String inputPass) {
        Map<String, Object> searchMap = new HashMap<String, Object>();
        searchMap.put("username", username);
        List<Admin> list = findList(searchMap);
        if (list == null || list.size() == 0){
            throw new RuntimeException("用户名不存在");
        }

        if (!list.get(0).getPassword().equals(inputPass)){
            return false;
        }
        return true;
    }

    /**
     * 构建查询条件
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Admin.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 用户名
            if(searchMap.get("loginName")!=null && !"".equals(searchMap.get("loginName"))){
                criteria.andEqualTo("loginName",searchMap.get("loginName"));
            }
            // 密码
            if(searchMap.get("password")!=null && !"".equals(searchMap.get("password"))){
                criteria.andEqualTo("password",searchMap.get("password"));
            }
            // 状态
            if(searchMap.get("status")!=null && !"".equals(searchMap.get("status"))){
                criteria.andLike("status","%"+searchMap.get("status")+"%");
            }

            // id
            if(searchMap.get("id")!=null ){
                criteria.andEqualTo("id",searchMap.get("id"));
            }

        }
        return example;
    }

}
