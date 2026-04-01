package cn.edu.ccst.manpower_resource.mapper;

import cn.edu.ccst.manpower_resource.entity.SysMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 菜单权限表 Mapper 接口
 * </p>
 *
 * @author 
 * @since 2025-12-17
 */
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);

    List<SysMenu> selectMenusByRoleId(@Param("roleId") Long roleId);
}
