package cn.edu.ccst.manpower_resource.service.impl;

import cn.edu.ccst.manpower_resource.common.ResultCode;
import cn.edu.ccst.manpower_resource.dto.CandidateRegisterRequest;
import cn.edu.ccst.manpower_resource.dto.CandidateProfileDTO;
import cn.edu.ccst.manpower_resource.entity.HrCandidate;
import cn.edu.ccst.manpower_resource.exception.BusinessException;
import cn.edu.ccst.manpower_resource.mapper.HrCandidateMapper;
import cn.edu.ccst.manpower_resource.service.IHrCandidateService;
import cn.edu.ccst.manpower_resource.util.JwtUtil;
import cn.edu.ccst.manpower_resource.vo.CandidateProfileVO;
import cn.edu.ccst.manpower_resource.security.LoginUser;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * 求职者Service实现类
 */
@Service
@RequiredArgsConstructor
public class HrCandidateServiceImpl extends ServiceImpl<HrCandidateMapper, HrCandidate> implements IHrCandidateService {

    private final HrCandidateMapper candidateMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    public void register(CandidateRegisterRequest request) {
        // 验证两次密码是否一致
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }

        // 检查用户名是否已存在
        Long count = candidateMapper.selectCount(
                new LambdaQueryWrapper<HrCandidate>()
                        .eq(HrCandidate::getUsername, request.getUsername())
                        .eq(HrCandidate::getDeleted, 0)
        );
        if (count > 0) {
            throw new BusinessException(ResultCode.USER_EXIST);
        }

        // 创建求职者（只保存必要信息）
        HrCandidate candidate = new HrCandidate();
        candidate.setUsername(request.getUsername());
        candidate.setPassword(passwordEncoder.encode(request.getPassword()));
        candidate.setStatus(1);
        candidate.setDeleted(0);
        candidate.setCreateTime(LocalDateTime.now());
        candidate.setUpdateTime(LocalDateTime.now());

        candidateMapper.insert(candidate);
    }

    @Override
    public String login(String username, String password, String ip) {
        // 查询求职者
        HrCandidate candidate = candidateMapper.selectOne(
                new LambdaQueryWrapper<HrCandidate>()
                        .eq(HrCandidate::getUsername, username)
                        .eq(HrCandidate::getDeleted, 0)
        );

        if (candidate == null) {
            throw new org.springframework.security.authentication.BadCredentialsException("用户名或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(password, candidate.getPassword())) {
            throw new org.springframework.security.authentication.BadCredentialsException("用户名或密码错误");
        }

        if (candidate.getStatus() != 1) {
            throw new org.springframework.security.authentication.DisabledException("账户已被禁用");
        }

        // 更新最后登录时间和IP
        candidate.setLastLoginTime(LocalDateTime.now());
        candidate.setLastLoginIp(ip);
        candidateMapper.updateById(candidate);

        // 生成JWT token
        String token = jwtUtil.generateToken(candidate.getId(), candidate.getUsername());

        return token;
    }

    @Override
    public cn.edu.ccst.manpower_resource.vo.CandidateProfileVO getProfile(String username) {
        // 查询求职者
        HrCandidate candidate = candidateMapper.selectOne(
                new LambdaQueryWrapper<HrCandidate>()
                        .eq(HrCandidate::getUsername, username)
                        .eq(HrCandidate::getDeleted, 0)
        );

        if (candidate == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 转换为VO
        cn.edu.ccst.manpower_resource.vo.CandidateProfileVO vo = new cn.edu.ccst.manpower_resource.vo.CandidateProfileVO();
        vo.setUsername(candidate.getUsername());
        vo.setRealName(candidate.getRealName());
        vo.setPhone(candidate.getPhone());
        vo.setEmail(candidate.getEmail());
        vo.setGender(candidate.getGender());
        vo.setIdCard(candidate.getIdCard());
        vo.setEducation(candidate.getEducation());
        vo.setGraduateSchool(candidate.getGraduateSchool());
        vo.setMajor(candidate.getMajor());

        // 处理日期转换
        if (candidate.getGraduateDate() != null) {
            vo.setGraduateDate(candidate.getGraduateDate().toString());
        }

        vo.setWorkExperience(candidate.getWorkExperience());
        vo.setExpectedSalary(candidate.getExpectedSalary());
        vo.setExpectedPosition(candidate.getExpectedPosition());
        vo.setResumeUrl(candidate.getResumeUrl());

        return vo;
    }

    @Override
    public void updateProfile(String username, cn.edu.ccst.manpower_resource.dto.CandidateProfileDTO request) {
        // 查询求职者
        HrCandidate candidate = candidateMapper.selectOne(
                new LambdaQueryWrapper<HrCandidate>()
                        .eq(HrCandidate::getUsername, username)
                        .eq(HrCandidate::getDeleted, 0)
        );

        if (candidate == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 更新字段（只更新非空字段）
        if (request.getRealName() != null) {
            candidate.setRealName(request.getRealName());
        }
        if (request.getPhone() != null) {
            candidate.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            candidate.setEmail(request.getEmail());
        }
        if (request.getGender() != null) {
            candidate.setGender(request.getGender());
        }
        if (request.getIdCard() != null) {
            candidate.setIdCard(request.getIdCard());
        }
        if (request.getEducation() != null) {
            candidate.setEducation(request.getEducation());
        }
        if (request.getGraduateSchool() != null) {
            candidate.setGraduateSchool(request.getGraduateSchool());
        }
        if (request.getMajor() != null) {
            candidate.setMajor(request.getMajor());
        }
        if (request.getGraduateDate() != null && !request.getGraduateDate().isEmpty()) {
            try {
                candidate.setGraduateDate(java.time.LocalDate.parse(request.getGraduateDate()));
            } catch (Exception e) {
                // 日期格式错误，忽略
            }
        }
        if (request.getWorkExperience() != null) {
            candidate.setWorkExperience(request.getWorkExperience());
        }
        if (request.getExpectedSalary() != null) {
            candidate.setExpectedSalary(request.getExpectedSalary());
        }
        if (request.getExpectedPosition() != null) {
            candidate.setExpectedPosition(request.getExpectedPosition());
        }
        if (request.getResumeUrl() != null) {
            candidate.setResumeUrl(request.getResumeUrl());
        }

        candidate.setUpdateTime(LocalDateTime.now());
        candidateMapper.updateById(candidate);
    }
}
