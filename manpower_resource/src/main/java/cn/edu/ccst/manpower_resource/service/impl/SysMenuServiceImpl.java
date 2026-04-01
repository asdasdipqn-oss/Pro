package cn.edu.ccst.manpower_resource.service.impl;

import cn.edu.ccst.manpower_resource.common.ResultCode;
import cn.edu.ccst.manpower_resource.entity.SysMenu;
import cn.edu.ccst.manpower_resource.exception.BusinessException;
import cn.edu.ccst.manpower_resource.mapper.SysMenuMapper;
import cn.edu.ccst.manpower_resource.service.ISysMenuService;
import cn.edu.ccst.manpower_resource.vo.MenuTreeVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

    @Override
    public List<MenuTreeVO> getMenuTree() {
        List<SysMenu> all = baseMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getDeleted, 0)
                .orderByAsc(SysMenu::getSort));
        return buildTree(all, 0L);
    }

    @Override
    public List<MenuTreeVO> getUserMenuTree(Long userId) {
        List<SysMenu> menus = baseMapper.selectMenusByUserId(userId);
        return buildTree(menus, 0L);
    }

    @Override
    public SysMenu getDetail(Long id) {
        SysMenu menu = baseMapper.selectById(id);
        if (menu == null || menu.getDeleted() == 1) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return menu;
    }

    @Override
    @Transactional
    public void createMenu(SysMenu menu) {
        menu.setDeleted(0);
        menu.setCreateTime(LocalDateTime.now());
        menu.setUpdateTime(LocalDateTime.now());
        baseMapper.insert(menu);
    }

    @Override
    @Transactional
    public void updateMenu(SysMenu menu) {
        SysMenu old = baseMapper.selectById(menu.getId());
        if (old == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        menu.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(menu);
    }

    @Override
    @Transactional
    public void deleteMenu(Long id) {
        Long childCount = baseMapper.selectCount(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, id)
                .eq(SysMenu::getDeleted, 0));
        if (childCount > 0) {
            throw new BusinessException("Has child menus");
        }

        SysMenu menu = baseMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        // 使用 MyBatis Plus 的逻辑删除
        removeById(id);
    }

    private List<MenuTreeVO> buildTree(List<SysMenu> all, Long parentId) {
        return all.stream()
                .filter(m -> parentId.equals(m.getParentId()))
                .map(m -> {
                    MenuTreeVO vo = new MenuTreeVO();
                    vo.setId(m.getId());
                    vo.setParentId(m.getParentId());
                    vo.setMenuName(m.getMenuName());
                    vo.setMenuType(m.getMenuType());
                    vo.setPermission(m.getPermission());
                    vo.setPath(m.getPath());
                    vo.setComponent(m.getComponent());
                    vo.setIcon(m.getIcon());
                    vo.setSort(m.getSort());
                    vo.setVisible(m.getVisible());
                    vo.setStatus(m.getStatus());
                    vo.setChildren(buildTree(all, m.getId()));
                    return vo;
                })
                .collect(Collectors.toList());
    }
}
