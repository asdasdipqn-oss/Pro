package cn.edu.ccst.manpower_resource.service;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.dto.ApproveDTO;
import cn.edu.ccst.manpower_resource.dto.PageQuery;
import cn.edu.ccst.manpower_resource.entity.ApprovalRecord;
import cn.edu.ccst.manpower_resource.vo.ApprovalRecordVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 审批记录表 服务类
 */
public interface IApprovalRecordService extends IService<ApprovalRecord> {

    /**
     * 创建审批记录（发起审批）
     */
    ApprovalRecord createRecord(Integer businessType, Long businessId, Long approverId);

    /**
     * 执行审批
     */
    void approve(Long approverId, ApproveDTO dto);

    /**
     * 查询待我审批的列表
     */
    PageResult<ApprovalRecordVO> getPendingList(Long approverId, Integer businessType, PageQuery query);

    /**
     * 查询我已审批的列表
     */
    PageResult<ApprovalRecordVO> getApprovedList(Long approverId, Integer businessType, PageQuery query);

    /**
     * 根据业务查询审批记录
     */
    List<ApprovalRecordVO> getByBusiness(Integer businessType, Long businessId);

    /**
     * 查询所有审批记录（管理员）
     */
    PageResult<ApprovalRecordVO> pageAll(Integer businessType, Integer status, PageQuery query);
}
