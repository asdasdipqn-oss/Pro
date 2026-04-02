package cn.edu.ccst.manpower_resource.service;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.dto.AppealDTO;
import cn.edu.ccst.manpower_resource.dto.PageQuery;
import cn.edu.ccst.manpower_resource.entity.AttAppeal;
import cn.edu.ccst.manpower_resource.vo.AttAppealVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 考勤异常申诉表 服务类
 */
public interface IAttAppealService extends IService<AttAppeal> {

    void submit(Long employeeId, AppealDTO dto);

    void cancel(Long id, Long employeeId);

    PageResult<AttAppealVO> pageByEmployee(Long employeeId, PageQuery query);

    AttAppealVO getDetail(Long id);

    List<AttAppealVO> getPendingApprovals();

    void approve(Long id, Long approverId, Integer status, String comment);

    PageResult<AttAppealVO> pageAll(PageQuery query, Integer status);

    /**
     * 获取指定月份已通过的申诉列表
     * @param year 年份
     * @param month 月份
     * @return 已通过的申诉列表
     */
    List<AttAppealVO> getApprovedAppeals(Integer year, Integer month);
}
