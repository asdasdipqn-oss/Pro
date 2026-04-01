package cn.edu.ccst.manpower_resource.service;

import cn.edu.ccst.manpower_resource.entity.SysMenu;
import cn.edu.ccst.manpower_resource.vo.MenuTreeVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ISysMenuService extends IService<SysMenu> {

    List<MenuTreeVO> getMenuTree();

    List<MenuTreeVO> getUserMenuTree(Long userId);

    SysMenu getDetail(Long id);

    void createMenu(SysMenu menu);

    void updateMenu(SysMenu menu);

    void deleteMenu(Long id);
}
