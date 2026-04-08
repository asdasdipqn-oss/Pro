package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.entity.AssessTask;
import cn.edu.ccst.manpower_resource.security.LoginUser;
import cn.edu.ccst.manpower_resource.service.IAssessTaskService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "考核任务管理", description = "考核任务CRUD操作")
@RestController
@RequestMapping("/assess/task")
@RequiredArgsConstructor
public class AssessTaskController {

    private final IAssessTaskService assessTaskService;

    @Operation(summary = "分页查询考核任务（管理员）")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public Result<PageResult<AssessTask>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "任务标题") @RequestParam(required = false) String title,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {

        Page<AssessTask> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AssessTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(title), AssessTask::getTitle, title)
                .eq(status != null, AssessTask::getStatus, status)
                .eq(AssessTask::getDeleted, 0)
                .orderByDesc(AssessTask::getCreateTime);

        Page<AssessTask> result = assessTaskService.page(page, wrapper);
        return Result.success(PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords()));
    }

    @Operation(summary = "查询已发布的考核任务（员工可见）")
    @GetMapping("/published")
    public Result<PageResult<AssessTask>> publishedPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Page<AssessTask> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AssessTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(AssessTask::getStatus, 1, 2)
                .eq(AssessTask::getDeleted, 0)
                .orderByDesc(AssessTask::getCreateTime);

        Page<AssessTask> result = assessTaskService.page(page, wrapper);
        return Result.success(PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords()));
    }

    @Operation(summary = "获取考核任务详情")
    @GetMapping("/{id}")
    public Result<AssessTask> getById(@Parameter(description = "任务ID") @PathVariable Long id) {
        return Result.success(assessTaskService.getById(id));
    }

    @Operation(summary = "创建考核任务")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Void> create(@RequestBody AssessTask task, @AuthenticationPrincipal LoginUser loginUser) {
        System.out.println("========== Creating AssessTask ==========");
        System.out.println("Task: " + task);
        System.out.println("Title: " + task.getTitle());
        System.out.println("Deadline: " + task.getDeadline());
        System.out.println("PlanId: " + task.getPlanId());
        try {
            task.setCreateBy(loginUser.getUserId());
            task.setStatus(0);
            task.setDeleted(0);
            task.setCreateTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());
            assessTaskService.save(task);
            System.out.println("========== AssessTask saved successfully ==========");
            return Result.success();
        } catch (Exception e) {
            System.out.println("========== ERROR SAVING ASSESS TASK ==========");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            return Result.fail(500, "创建考核任务失败: " + e.getMessage());
        }
    }

    @Operation(summary = "更新考核任务")
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Void> update(@RequestBody AssessTask task) {
        task.setUpdateTime(LocalDateTime.now());
        assessTaskService.updateById(task);
        return Result.success();
    }

    @Operation(summary = "发布考核任务")
    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Void> publish(@Parameter(description = "任务ID") @PathVariable Long id) {
        AssessTask task = new AssessTask();
        task.setId(id);
        task.setStatus(1);
        task.setUpdateTime(LocalDateTime.now());
        assessTaskService.updateById(task);
        return Result.success();
    }

    @Operation(summary = "截止考核任务")
    @PutMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Void> close(@Parameter(description = "任务ID") @PathVariable Long id) {
        AssessTask task = new AssessTask();
        task.setId(id);
        task.setStatus(2);
        task.setUpdateTime(LocalDateTime.now());
        assessTaskService.updateById(task);
        return Result.success();
    }

    @Operation(summary = "删除考核任务（逻辑删除）")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Void> delete(@Parameter(description = "任务ID") @PathVariable Long id) {
        AssessTask task = new AssessTask();
        task.setId(id);
        task.setDeleted(1);
        task.setUpdateTime(LocalDateTime.now());
        assessTaskService.updateById(task);
        return Result.success();
    }
}
