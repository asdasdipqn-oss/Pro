package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.entity.SysDictData;
import cn.edu.ccst.manpower_resource.entity.SysDictType;
import cn.edu.ccst.manpower_resource.service.ISysDictTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "字典管理", description = "基础数据-字典配置")
@RestController
@RequestMapping("/sys/dict")
@RequiredArgsConstructor
public class SysDictTypeController {

    private final ISysDictTypeService dictTypeService;

    @Operation(summary = "获取所有字典类型")
    @GetMapping("/type/list")
    public Result<List<SysDictType>> listTypes() {
        return Result.success(dictTypeService.listAll());
    }

    @Operation(summary = "根据类型获取字典数据")
    @GetMapping("/data/{typeCode}")
    public Result<List<SysDictData>> getDataByType(
            @Parameter(description = "字典类型编码") @PathVariable String typeCode) {
        return Result.success(dictTypeService.getDataByType(typeCode));
    }

    @Operation(summary = "新增字典类型")
    @PostMapping("/type")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> createType(@Valid @RequestBody SysDictType type) {
        dictTypeService.createType(type);
        return Result.success();
    }

    @Operation(summary = "修改字典类型")
    @PutMapping("/type")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateType(@Valid @RequestBody SysDictType type) {
        dictTypeService.updateType(type);
        return Result.success();
    }

    @Operation(summary = "删除字典类型")
    @DeleteMapping("/type/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteType(@Parameter(description = "类型ID") @PathVariable Long id) {
        dictTypeService.deleteType(id);
        return Result.success();
    }
}
