package cn.edu.ccst.manpower_resource.service;

import cn.edu.ccst.manpower_resource.dto.SysRoleDTO;
import cn.edu.ccst.manpower_resource.entity.SysRole;
import com.baomidou.mybatisplus.extension.service.IService;

import cn.edu.ccst.manpower_resource.entity.SysMenu;
import java.util.List;

public interface ISysRoleService extends IService<SysRole> {

    List<SysRole> listAll();

    List<SysMenu> getRoleMenus(Long roleId);

    SysRole getDetail(Long id);

    void createRole(SysRoleDTO dto);

    void updateRole(SysRoleDTO dto);

    void deleteRole(Long id);

    void assignMenus(Long roleId, List<Long> menuIds);
}
