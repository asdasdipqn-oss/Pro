package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.entity.AttDeptRule;
import cn.edu.ccst.manpower_resource.service.IAttDeptRuleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "部门考勤规则", description = "部门与考勤规则关联管理")
@RestController
@RequestMapping("/att/dept-rule")
@RequiredArgsConstructor
public class AttDeptRuleController {

    private final IAttDeptRuleService attDeptRuleService;

    @Operation(summary = "查询部门的考勤规则")
    @GetMapping("/dept/{deptId}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<List<AttDeptRule>> getByDept(@PathVariable Long deptId) {
        List<AttDeptRule> list = attDeptRuleService.list(new LambdaQueryWrapper<AttDeptRule>()
                .eq(AttDeptRule::getDeptId, deptId));
        return Result.success(list);
    }

    @Operation(summary = "设置部门考勤规则")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Void> add(@RequestBody AttDeptRule attDeptRule) {
        // 先删除该部门已有的规则关联
        attDeptRuleService.remove(new LambdaQueryWrapper<AttDeptRule>()
                .eq(AttDeptRule::getDeptId, attDeptRule.getDeptId()));
        attDeptRule.setCreateTime(LocalDateTime.now());
        attDeptRuleService.save(attDeptRule);
        return Result.success();
    }

    @Operation(summary = "删除部门考勤规则关联")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Void> delete(@PathVariable Long id) {
        attDeptRuleService.removeById(id);
        return Result.success();
    }
}
