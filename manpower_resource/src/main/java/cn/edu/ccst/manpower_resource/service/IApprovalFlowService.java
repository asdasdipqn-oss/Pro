package cn.edu.ccst.manpower_resource.service;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.dto.ApprovalFlowDTO;
import cn.edu.ccst.manpower_resource.dto.PageQuery;
import cn.edu.ccst.manpower_resource.entity.ApprovalFlow;
import cn.edu.ccst.manpower_resource.vo.ApprovalFlowVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 审批流程配置表 服务类
 */
public interface IApprovalFlowService extends IService<ApprovalFlow> {

    /**
     * 创建审批流程
     */
    ApprovalFlowVO create(ApprovalFlowDTO dto);

    /**
     * 更新审批流程
     */
    ApprovalFlowVO update(ApprovalFlowDTO dto);

    /**
     * 删除审批流程
     */
    void delete(Long id);

    /**
     * 获取流程详情（含节点）
     */
    ApprovalFlowVO getDetail(Long id);

    /**
     * 分页查询审批流程
     */
    PageResult<ApprovalFlowVO> page(Integer flowType, Integer status, PageQuery query);

    /**
     * 获取所有启用的流程列表
     */
    List<ApprovalFlowVO> listEnabled(Integer flowType);

    /**
     * 根据流程类型获取默认流程
     */
    ApprovalFlow getByType(Integer flowType);
}
