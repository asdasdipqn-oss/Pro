package cn.edu.ccst.manpower_resource.service;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.dto.SysUserDTO;
import cn.edu.ccst.manpower_resource.dto.SysUserQuery;
import cn.edu.ccst.manpower_resource.entity.SysRole;
import cn.edu.ccst.manpower_resource.entity.SysUser;
import cn.edu.ccst.manpower_resource.vo.UserInfoVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ISysUserService extends IService<SysUser> {

    List<SysRole> getUserRoles(Long userId);

    PageResult<UserInfoVO> pageList(SysUserQuery query);

    UserInfoVO getDetail(Long id);

    void createUser(SysUserDTO dto);

    void updateUser(SysUserDTO dto);

    void deleteUser(Long id);

    void updateStatus(Long id, Integer status);

    void resetPassword(Long id, String newPassword);

    void changePassword(Long userId, String oldPassword, String newPassword);

    void updateProfile(Long userId, String username);
}
