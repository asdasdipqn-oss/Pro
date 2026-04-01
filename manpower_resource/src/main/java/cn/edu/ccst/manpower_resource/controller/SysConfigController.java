package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.entity.SysConfig;
import cn.edu.ccst.manpower_resource.service.ISysConfigService;
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

@Tag(name = "系统配置管理", description = "系统配置CRUD操作")
@RestController
@RequestMapping("/sys/config")
@RequiredArgsConstructor
public class SysConfigController {

    private final ISysConfigService sysConfigService;

    @Operation(summary = "分页查询系统配置")
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<SysConfig>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String configName,
            @RequestParam(required = false) Integer configType) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(configName), SysConfig::getConfigName, configName)
                .eq(configType != null, SysConfig::getConfigType, configType)
                .orderByAsc(SysConfig::getConfigKey);
        Page<SysConfig> page = sysConfigService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(page);
    }

    @Operation(summary = "获取所有系统配置")
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<SysConfig>> list() {
        return Result.success(sysConfigService.list(new LambdaQueryWrapper<SysConfig>()
                .orderByAsc(SysConfig::getConfigKey)));
    }

    @Operation(summary = "根据配置键获取配置值")
    @GetMapping("/key/{configKey}")
    public Result<SysConfig> getByKey(@PathVariable String configKey) {
        SysConfig config = sysConfigService.getOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, configKey));
        return Result.success(config);
    }

    @Operation(summary = "获取配置详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SysConfig> getById(@PathVariable Long id) {
        return Result.success(sysConfigService.getById(id));
    }

    @Operation(summary = "新增系统配置")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> add(@RequestBody SysConfig sysConfig) {
        sysConfig.setCreateTime(LocalDateTime.now());
        sysConfig.setUpdateTime(LocalDateTime.now());
        sysConfigService.save(sysConfig);
        return Result.success();
    }

    @Operation(summary = "修改系统配置")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(@RequestBody SysConfig sysConfig) {
        sysConfig.setUpdateTime(LocalDateTime.now());
        sysConfigService.updateById(sysConfig);
        return Result.success();
    }

    @Operation(summary = "删除系统配置")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        sysConfigService.removeById(id);
        return Result.success();
    }
}
