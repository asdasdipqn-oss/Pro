package cn.edu.ccst.manpower_resource.service;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.dto.TrainRequestDTO;
import cn.edu.ccst.manpower_resource.entity.TrainRequest;
import cn.edu.ccst.manpower_resource.vo.TrainRequestVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 培训需求表 服务类
 * </p>
 *
 * @author
 * @since 2026-04-03
 */
public interface ITrainRequestService extends IService<TrainRequest> {

    /**
     * 提交培训需求
     */
    void submit(Long applicantId, TrainRequestDTO dto);

    /**
     * 撤回培训需求
     */
    void cancel(Long id, Long applicantId);

    /**
     * 获取我的培训需求列表
     */
    List<TrainRequestVO> getMyRequests(Long userId);

    /**
     * 获取待审批的培训需求列表
     */
    List<TrainRequestVO> getPendingApprovals();

    /**
     * 审批培训需求
     */
    void approve(Long id, Long approverId, Integer status, String comment);

    /**
     * 分页查询所有培训需求（管理员）
     */
    PageResult<TrainRequestVO> pageAll(Integer pageNum, Integer pageSize, Integer status);

    /**
     * 获取详情
     */
    TrainRequestVO getDetail(Long id);
}
