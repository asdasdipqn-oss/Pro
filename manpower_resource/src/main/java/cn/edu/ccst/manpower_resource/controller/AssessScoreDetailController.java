package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.entity.AssessScoreDetail;
import cn.edu.ccst.manpower_resource.service.IAssessScoreDetailService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "评分明细管理", description = "考核指标评分明细")
@RestController
@RequestMapping("/assess/score-detail")
@RequiredArgsConstructor
public class AssessScoreDetailController {

    private final IAssessScoreDetailService assessScoreDetailService;

    @Operation(summary = "查询考核结果的评分明细")
    @GetMapping("/result/{resultId}")
    public Result<List<AssessScoreDetail>> getByResult(@PathVariable Long resultId) {
        List<AssessScoreDetail> list = assessScoreDetailService.list(new LambdaQueryWrapper<AssessScoreDetail>()
                .eq(AssessScoreDetail::getResultId, resultId));
        return Result.success(list);
    }

    @Operation(summary = "获取评分明细详情")
    @GetMapping("/{id}")
    public Result<AssessScoreDetail> getById(@PathVariable Long id) {
        return Result.success(assessScoreDetailService.getById(id));
    }

    @Operation(summary = "新增评分明细")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public Result<Void> add(@RequestBody AssessScoreDetail detail) {
        detail.setCreateTime(LocalDateTime.now());
        assessScoreDetailService.save(detail);
        return Result.success();
    }

    @Operation(summary = "批量保存评分明细")
    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public Result<Void> batchSave(@RequestBody List<AssessScoreDetail> details) {
        LocalDateTime now = LocalDateTime.now();
        details.forEach(d -> d.setCreateTime(now));
        assessScoreDetailService.saveBatch(details);
        return Result.success();
    }

    @Operation(summary = "修改评分明细")
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR','MANAGER')")
    public Result<Void> update(@RequestBody AssessScoreDetail detail) {
        assessScoreDetailService.updateById(detail);
        return Result.success();
    }

    @Operation(summary = "删除评分明细")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Void> delete(@PathVariable Long id) {
        assessScoreDetailService.removeById(id);
        return Result.success();
    }
}
