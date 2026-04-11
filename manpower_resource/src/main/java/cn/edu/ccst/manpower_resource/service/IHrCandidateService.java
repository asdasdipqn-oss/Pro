package cn.edu.ccst.manpower_resource.service;

import cn.edu.ccst.manpower_resource.dto.CandidateRegisterRequest;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 求职者Service接口
 */
public interface IHrCandidateService extends IService<cn.edu.ccst.manpower_resource.entity.HrCandidate> {

    /**
     * 求职者注册
     */
    void register(CandidateRegisterRequest request);

    /**
     * 求职者登录
     */
    String login(String username, String password, String ip);

    /**
     * 获取求职者个人信息
     */
    cn.edu.ccst.manpower_resource.vo.CandidateProfileVO getProfile(String username);

    /**
     * 更新求职者个人信息
     */
    void updateProfile(String username, cn.edu.ccst.manpower_resource.dto.CandidateProfileDTO request);
}
