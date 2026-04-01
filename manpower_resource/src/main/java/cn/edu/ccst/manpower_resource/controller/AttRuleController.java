package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.entity.AttRule;
import cn.edu.ccst.manpower_resource.service.IAttRuleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "考勤规则管理", description = "考勤规则CRUD操作")
@RestController
@RequestMapping("/att/rule")
@RequiredArgsConstructor
public class AttRuleController {

    private final IAttRuleService attRuleService;

    @Operation(summary = "分页查询考勤规则")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Page<AttRule>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String ruleName,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<AttRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(ruleName), AttRule::getRuleName, ruleName)
                .eq(status != null, AttRule::getStatus, status)
                .orderByDesc(AttRule::getCreateTime);
        Page<AttRule> page = attRuleService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(page);
    }

    @Operation(summary = "获取所有启用的考勤规则")
    @GetMapping("/list")
    public Result<List<AttRule>> list() {
        List<AttRule> list = attRuleService.list(new LambdaQueryWrapper<AttRule>()
                .eq(AttRule::getStatus, 1)
                .orderByDesc(AttRule::getIsDefault));
        return Result.success(list);
    }

    @Operation(summary = "获取默认考勤规则")
    @GetMapping("/default")
    public Result<AttRule> getDefault() {
        AttRule rule = attRuleService.getOne(new LambdaQueryWrapper<AttRule>()
                .eq(AttRule::getIsDefault, 1)
                .eq(AttRule::getStatus, 1)
                .last("LIMIT 1"));
        return Result.success(rule);
    }

    @Operation(summary = "获取考勤规则详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<AttRule> getById(@PathVariable Long id) {
        return Result.success(attRuleService.getById(id));
    }

    @Operation(summary = "新增考勤规则")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Void> add(@RequestBody AttRule attRule) {
        attRule.setCreateTime(LocalDateTime.now());
        attRule.setUpdateTime(LocalDateTime.now());
        // 如果设为默认规则，取消其他默认
        if (attRule.getIsDefault() != null && attRule.getIsDefault() == 1) {
            attRuleService.update(new AttRule().setIsDefault(0),
                    new LambdaQueryWrapper<AttRule>().eq(AttRule::getIsDefault, 1));
        }
        attRuleService.save(attRule);
        return Result.success();
    }

    @Operation(summary = "修改考勤规则")
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Void> update(@RequestBody AttRule attRule) {
        attRule.setUpdateTime(LocalDateTime.now());
        if (attRule.getIsDefault() != null && attRule.getIsDefault() == 1) {
            attRuleService.update(new AttRule().setIsDefault(0),
                    new LambdaQueryWrapper<AttRule>().eq(AttRule::getIsDefault, 1));
        }
        attRuleService.updateById(attRule);
        return Result.success();
    }

    @Operation(summary = "删除考勤规则")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public Result<Void> delete(@PathVariable Long id) {
        attRuleService.removeById(id);
        return Result.success();
    }
}
