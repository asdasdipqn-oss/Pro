package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.dto.PageQuery;
import cn.edu.ccst.manpower_resource.entity.TrainPlan;
import cn.edu.ccst.manpower_resource.security.LoginUser;
import cn.edu.ccst.manpower_resource.service.ITrainPlanService;
import cn.edu.ccst.manpower_resource.vo.TrainParticipantVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "培训管理", description = "培训计划相关操作")
@RestController
@RequestMapping("/train/plan")
@RequiredArgsConstructor
public class TrainPlanController {

    private final ITrainPlanService trainPlanService;

    @Operation(summary = "分页查询培训计划")
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Result<PageResult<TrainPlan>> page(PageQuery query,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        return Result.success(trainPlanService.pageList(query, status));
    }

    @Operation(summary = "查询我的培训")
    @GetMapping("/my")
    public Result<List<TrainPlan>> myTrains(@AuthenticationPrincipal LoginUser loginUser) {
        Long employeeId = loginUser.getUser().getEmployeeId();
        return Result.success(trainPlanService.listByEmployee(employeeId));
    }

    @Operation(summary = "获取培训详情")
    @GetMapping("/{id}")
    public Result<TrainPlan> getDetail(@Parameter(description = "计划ID") @PathVariable Long id) {
        return Result.success(trainPlanService.getById(id));
    }

    @Operation(summary = "创建培训计划")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Result<Void> create(@Valid @RequestBody TrainPlan plan) {
        trainPlanService.createPlan(plan);
        return Result.success();
    }

    @Operation(summary = "修改培训计划")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Result<Void> update(@Valid @RequestBody TrainPlan plan) {
        trainPlanService.updatePlan(plan);
        return Result.success();
    }

    @Operation(summary = "删除培训计划")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Result<Void> delete(@Parameter(description = "计划ID") @PathVariable Long id) {
        trainPlanService.deletePlan(id);
        return Result.success();
    }

    @Operation(summary = "指定参训人员")
    @PostMapping("/{id}/participants")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Result<Void> assignParticipants(
            @Parameter(description = "计划ID") @PathVariable Long id,
            @Parameter(description = "员工ID列表") @RequestBody List<Long> employeeIds) {
        trainPlanService.assignParticipants(id, employeeIds);
        return Result.success();
    }

    @Operation(summary = "获取培训参训人员列表")
    @GetMapping("/{id}/participants")
    public Result<List<TrainParticipantVO>> getParticipants(@Parameter(description = "计划ID") @PathVariable Long id) {
        return Result.success(trainPlanService.getParticipants(id));
    }

    @Operation(summary = "移除参训人员")
    @DeleteMapping("/{id}/participants/{employeeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Result<Void> removeParticipant(
            @Parameter(description = "计划ID") @PathVariable Long id,
            @Parameter(description = "员工ID") @PathVariable Long employeeId) {
        trainPlanService.removeParticipant(id, employeeId);
        return Result.success();
    }

    @Operation(summary = "参训签到")
    @PutMapping("/{id}/sign-in")
    public Result<Void> signIn(@AuthenticationPrincipal LoginUser loginUser,
                               @Parameter(description = "计划ID") @PathVariable Long id) {
        trainPlanService.signIn(id, loginUser.getUser().getEmployeeId());
        return Result.success();
    }

    @Operation(summary = "录入考核成绩")
    @PutMapping("/participant/{participantId}/score")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public Result<Void> recordScore(
            @Parameter(description = "参训记录ID") @PathVariable Long participantId,
            @Parameter(description = "成绩") @RequestParam Integer score,
            @Parameter(description = "评价") @RequestParam(required = false) String evaluation) {
        trainPlanService.recordScore(participantId, score, evaluation);
        return Result.success();
    }
}
