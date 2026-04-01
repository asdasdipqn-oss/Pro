package cn.edu.ccst.manpower_resource.service;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.dto.PageQuery;
import cn.edu.ccst.manpower_resource.entity.TrainPlan;
import cn.edu.ccst.manpower_resource.vo.TrainParticipantVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ITrainPlanService extends IService<TrainPlan> {

    PageResult<TrainPlan> pageList(PageQuery query, Integer status);

    List<TrainPlan> listByEmployee(Long employeeId);

    void createPlan(TrainPlan plan);

    void updatePlan(TrainPlan plan);

    void deletePlan(Long id);

    void assignParticipants(Long planId, List<Long> employeeIds);

    List<TrainParticipantVO> getParticipants(Long planId);

    void removeParticipant(Long planId, Long employeeId);

    void signIn(Long planId, Long employeeId);

    void recordScore(Long participantId, Integer score, String evaluation);
}
