package cn.edu.ccst.manpower_resource.service.impl;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.common.ResultCode;
import cn.edu.ccst.manpower_resource.dto.PageQuery;
import cn.edu.ccst.manpower_resource.entity.OrgDepartment;
import cn.edu.ccst.manpower_resource.entity.RecruitJob;
import cn.edu.ccst.manpower_resource.exception.BusinessException;
import cn.edu.ccst.manpower_resource.mapper.OrgDepartmentMapper;
import cn.edu.ccst.manpower_resource.mapper.RecruitJobMapper;
import cn.edu.ccst.manpower_resource.service.IRecruitJobService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecruitJobServiceImpl extends ServiceImpl<RecruitJobMapper, RecruitJob> implements IRecruitJobService {

    private final OrgDepartmentMapper departmentMapper;

    @Override
    public List<RecruitJob> listPublished() {
        List<RecruitJob> jobs = baseMapper.selectList(new LambdaQueryWrapper<RecruitJob>()
                .eq(RecruitJob::getStatus, 1)
                .eq(RecruitJob::getDeleted, 0)
                .orderByDesc(RecruitJob::getCreateTime));
        fillDeptName(jobs);
        return jobs;
    }

    @Override
    public PageResult<RecruitJob> pageJobs(PageQuery query, Integer status) {
        Page<RecruitJob> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<RecruitJob> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(RecruitJob::getStatus, status);
        }
        wrapper.eq(RecruitJob::getDeleted, 0)
                .orderByDesc(RecruitJob::getCreateTime);
        Page<RecruitJob> result = baseMapper.selectPage(page, wrapper);
        fillDeptName(result.getRecords());
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    private void fillDeptName(List<RecruitJob> jobs) {
        List<Long> deptIds = jobs.stream()
                .map(RecruitJob::getDeptId)
                .filter(id -> id != null)
                .distinct().collect(Collectors.toList());
        if (deptIds.isEmpty()) return;
        List<OrgDepartment> depts = departmentMapper.selectBatchIds(deptIds);
        Map<Long, String> deptNameMap = depts.stream()
                .collect(Collectors.toMap(OrgDepartment::getId, OrgDepartment::getDeptName));
        for (RecruitJob job : jobs) {
            if (job.getDeptId() != null) {
                job.setDeptName(deptNameMap.getOrDefault(job.getDeptId(), ""));
            }
        }
    }

    @Override
    @Transactional
    public void publishJob(RecruitJob job) {
        // 自动生成岗位编号
        String jobCode = "JOB" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        job.setJobCode(jobCode);
        job.setStatus(1);
        job.setDeleted(0);
        job.setPublishTime(LocalDateTime.now());
        job.setCreateTime(LocalDateTime.now());
        job.setUpdateTime(LocalDateTime.now());
        baseMapper.insert(job);
    }

    @Override
    @Transactional
    public void updateJob(RecruitJob job) {
        RecruitJob old = baseMapper.selectById(job.getId());
        if (old == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        job.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(job);
    }

    @Override
    @Transactional
    public void closeJob(Long id) {
        RecruitJob job = baseMapper.selectById(id);
        if (job == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        System.out.println("[RecruitJob] 关闭岗位: id=" + id + ", status从" + job.getStatus() + "改为2");
        job.setStatus(2); // 关闭
        job.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(job);
        System.out.println("[RecruitJob] 关闭岗位成功");
    }
}
