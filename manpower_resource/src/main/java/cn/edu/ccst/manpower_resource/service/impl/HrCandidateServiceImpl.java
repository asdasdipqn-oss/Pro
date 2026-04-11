package cn.edu.ccst.manpower_resource.service.impl;

import cn.edu.ccst.manpower_resource.common.ResultCode;
import cn.edu.ccst.manpower_resource.dto.CandidateRegisterRequest;
import cn.edu.ccst.manpower_resource.dto.CandidateProfileDTO;
import cn.edu.ccst.manpower_resource.entity.HrCandidate;
import cn.edu.ccst.manpower_resource.entity.CandidateProfileHistory;
import cn.edu.ccst.manpower_resource.exception.BusinessException;
import cn.edu.ccst.manpower_resource.mapper.HrCandidateMapper;
import cn.edu.ccst.manpower_resource.mapper.CandidateProfileHistoryMapper;
import cn.edu.ccst.manpower_resource.service.IHrCandidateService;
import cn.edu.ccst.manpower_resource.util.JwtUtil;
import cn.edu.ccst.manpower_resource.vo.CandidateProfileVO;
import cn.edu.ccst.manpower_resource.security.LoginUser;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;

/**
 * 求职者Service实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HrCandidateServiceImpl extends ServiceImpl<HrCandidateMapper, HrCandidate> implements IHrCandidateService {

    private final HrCandidateMapper candidateMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final CandidateProfileHistoryMapper candidateProfileHistoryMapper;

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
        log.info("[getProfile] 开始查询求职者个人信息，token中的username: {}, 实际查询参数: {}", username, username);

        // 先查询所有该用户名的求职者记录（不限制deleted）
        java.util.List<HrCandidate> allCandidates = candidateMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<HrCandidate>()
                        .eq(HrCandidate::getUsername, username)
        );

        log.info("[getProfile] 查询到的求职者记录总数: {}", allCandidates.size());
        for (HrCandidate c : allCandidates) {
            log.info("[getProfile] candidate: id={}, username={}, deleted={}, realName={}",
                    c.getId(), c.getUsername(), c.getDeleted(), c.getRealName());
        }

        // 尝试所有查询条件
        HrCandidate candidate = candidateMapper.selectOne(
                new LambdaQueryWrapper<HrCandidate>()
                        .eq(HrCandidate::getUsername, username)
        );

        if (candidate == null) {
            log.error("[getProfile] 未找到求职者，所有条件都无法匹配，username: {}", username);
            log.error("[getProfile] deleted字段值: null");
            log.error("[getProfile] 尝试查询条件: eq(username={})", username);
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        log.info("找到求职者，candidateId: {}, deleted: {}, realName: {}",
                candidate.getId(), candidate.getDeleted(), candidate.getRealName());

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
        // 查询求职者（deleted=0 OR deleted is null）
        HrCandidate candidate = candidateMapper.selectOne(
                new LambdaQueryWrapper<HrCandidate>()
                        .eq(HrCandidate::getUsername, username)
                        .and(wrapper -> wrapper.eq(HrCandidate::getDeleted, 0).or().isNull(HrCandidate::getDeleted))
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

        // 保存到历史记录
        saveProfileToHistory(candidate, request);

        // 限制历史记录数量，最多保留最近10条
        limitProfileHistory(candidate.getId());
    }

    private void saveProfileToHistory(HrCandidate candidate, cn.edu.ccst.manpower_resource.dto.CandidateProfileDTO request) {
        log.info("开始保存求职者历史记录，candidateId: {}, realName: {}", candidate.getId(), candidate.getRealName());
        try {
            // 查询最后一次修改，避免重复保存
            cn.edu.ccst.manpower_resource.entity.CandidateProfileHistory lastRecord = candidateProfileHistoryMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<cn.edu.ccst.manpower_resource.entity.CandidateProfileHistory>()
                            .eq(cn.edu.ccst.manpower_resource.entity.CandidateProfileHistory::getCandidateId, candidate.getId())
                            .eq(cn.edu.ccst.manpower_resource.entity.CandidateProfileHistory::getDeleted, 0)
                            .orderByDesc(cn.edu.ccst.manpower_resource.entity.CandidateProfileHistory::getSubmitTime)
            );

            cn.edu.ccst.manpower_resource.entity.CandidateProfileHistory historyRecord = new cn.edu.ccst.manpower_resource.entity.CandidateProfileHistory();

            // 如果没有最后一条记录，直接保存
            // 如果有最后一条记录，比较内容是否变化
            boolean shouldSave = false;
            if (lastRecord == null) {
                shouldSave = true;
                log.debug("没有历史记录，将保存新的记录，candidateId: {}", candidate.getId());
            } else {
                shouldSave = !java.util.Objects.equals(lastRecord.getRealName(), candidate.getRealName()) ||
                        !java.util.Objects.equals(lastRecord.getPhone(), candidate.getPhone()) ||
                        !java.util.Objects.equals(lastRecord.getEmail(), candidate.getEmail()) ||
                        !java.util.Objects.equals(lastRecord.getEducation(), candidate.getEducation());
                log.debug("有历史记录，shouldSave: {}, lastRecord: {}, candidate: {}", shouldSave, lastRecord.getRealName(), candidate.getRealName());
            }

            if (shouldSave) {
            historyRecord.setCandidateId(candidate.getId());
            historyRecord.setRealName(candidate.getRealName());
            historyRecord.setPhone(candidate.getPhone());
            historyRecord.setEmail(candidate.getEmail());
            historyRecord.setGender(candidate.getGender());
            historyRecord.setIdCard(candidate.getIdCard());
            historyRecord.setEducation(candidate.getEducation());
            historyRecord.setGraduateSchool(candidate.getGraduateSchool());
            historyRecord.setGraduateDate(candidate.getGraduateDate() != null ? candidate.getGraduateDate().toString() : null);
            historyRecord.setWorkExperience(candidate.getWorkExperience());
            historyRecord.setExpectedSalary(candidate.getExpectedSalary());
            historyRecord.setExpectedPosition(candidate.getExpectedPosition());
            historyRecord.setResumeUrl(candidate.getResumeUrl());
            historyRecord.setSubmitTime(LocalDateTime.now());
            historyRecord.setCreateTime(LocalDateTime.now());
            historyRecord.setUpdateTime(LocalDateTime.now());
            historyRecord.setDeleted(0);
            candidateProfileHistoryMapper.insert(historyRecord);
            log.info("成功保存求职者历史记录，candidateId: {}, realName: {}", candidate.getId(), candidate.getRealName());
        } else {
            log.debug("跳过保存历史记录，内容未变化");
        }
        } catch (Exception e) {
            log.error("保存求职者历史记录失败", e);
        }
    }

    private void limitProfileHistory(Long candidateId) {
        // 查询该求职者的历史记录数量
        Long count = candidateProfileHistoryMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<cn.edu.ccst.manpower_resource.entity.CandidateProfileHistory>()
                        .eq(cn.edu.ccst.manpower_resource.entity.CandidateProfileHistory::getCandidateId, candidateId)
                        .eq(cn.edu.ccst.manpower_resource.entity.CandidateProfileHistory::getDeleted, 0)
        );

        // 如果超过10条，删除最旧的
        if (count > 10) {
            java.util.List<Long> idsToDelete = getHistoryIdsToDelete(candidateId, 10);
            if (!idsToDelete.isEmpty()) {
                candidateProfileHistoryMapper.deleteBatchIds(idsToDelete);
            }
        }
    }

    private java.util.List<Long> getHistoryIdsToDelete(Long candidateId, int keepCount) {
        // 使用子查询删除超过保留数量的旧记录
        // 获取所有历史记录ID，按提交时间倒序
        java.util.List<cn.edu.ccst.manpower_resource.entity.CandidateProfileHistory> allRecords = candidateProfileHistoryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<cn.edu.ccst.manpower_resource.entity.CandidateProfileHistory>()
                        .select(cn.edu.ccst.manpower_resource.entity.CandidateProfileHistory::getId)
                        .eq(cn.edu.ccst.manpower_resource.entity.CandidateProfileHistory::getCandidateId, candidateId)
                        .eq(cn.edu.ccst.manpower_resource.entity.CandidateProfileHistory::getDeleted, 0)
                        .orderByDesc(cn.edu.ccst.manpower_resource.entity.CandidateProfileHistory::getSubmitTime)
        );

        // 跳过前keepCount条记录，返回后面的ID
        java.util.List<Long> idsToDelete = new java.util.ArrayList<>();
        for (int i = keepCount; i < allRecords.size(); i++) {
            idsToDelete.add(allRecords.get(i).getId());
        }
        return idsToDelete;
    }

    @Override
    public java.util.List<cn.edu.ccst.manpower_resource.entity.CandidateProfileHistory> getProfileHistory(String username) {
        log.info("[getProfileHistory] 查询求职者历史记录，username: {}", username);

        // 先查询所有该用户名的求职者记录（不限制deleted）
        java.util.List<HrCandidate> allCandidates = candidateMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<HrCandidate>()
                        .eq(HrCandidate::getUsername, username)
        );

        log.info("[getProfileHistory] 查询到的求职者记录总数: {}", allCandidates.size());
        for (HrCandidate c : allCandidates) {
            log.info("[getProfileHistory] candidate: id={}, username={}, deleted={}, realName={}",
                    c.getId(), c.getUsername(), c.getDeleted(), c.getRealName());
        }

        // 查询求职者（deleted=0 OR deleted is null）
        HrCandidate candidate = candidateMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<HrCandidate>()
                        .eq(HrCandidate::getUsername, username)
                        .and(wrapper -> wrapper.eq(HrCandidate::getDeleted, 0).or().isNull(HrCandidate::getDeleted))
        );

        if (candidate == null) {
            log.error("[getProfileHistory] 未找到求职者，username: {}", username);
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        log.info("[getProfileHistory] 找到求职者，candidateId: {}, deleted: {}", candidate.getId(), candidate.getDeleted());

        // 查询历史记录
        java.util.List<cn.edu.ccst.manpower_resource.entity.CandidateProfileHistory> historyList = candidateProfileHistoryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<cn.edu.ccst.manpower_resource.entity.CandidateProfileHistory>()
                        .eq(cn.edu.ccst.manpower_resource.entity.CandidateProfileHistory::getCandidateId, candidate.getId())
                        .eq(cn.edu.ccst.manpower_resource.entity.CandidateProfileHistory::getDeleted, 0)
                        .orderByDesc(cn.edu.ccst.manpower_resource.entity.CandidateProfileHistory::getSubmitTime)
        );

        return historyList;
    }
}
