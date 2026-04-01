package cn.edu.ccst.manpower_resource.service;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.dto.PageQuery;
import cn.edu.ccst.manpower_resource.vo.ApprovalLogVO;

import java.util.List;

/**
 * 审批日志服务接口
 */
public interface IApprovalLogService {

    /**
     * 记录审批日志
     */
    void log(String businessType, Long businessId, Long approverId, String action, String comment);

    /**
     * 根据业务查询日志
     */
    List<ApprovalLogVO> getByBusiness(String businessType, Long businessId);

    /**
     * 分页查询所有日志
     */
    PageResult<ApprovalLogVO> pageAll(PageQuery query, String businessType);
}
