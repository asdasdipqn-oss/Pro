package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.entity.AttClockLocation;
import cn.edu.ccst.manpower_resource.service.IAttClockLocationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 打卡地点管理 Controller
 */
@Tag(name = "打卡地点管理", description = "打卡地点CRUD操作")
@RestController
@RequestMapping("/att/location")
@RequiredArgsConstructor
public class AttClockLocationController {

    private final IAttClockLocationService locationService;

    @Operation(summary = "分页查询打卡地点")
    @GetMapping("/page")
    public Result<Page<AttClockLocation>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "地点名称") @RequestParam(required = false) String locationName,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        
        Page<AttClockLocation> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AttClockLocation> wrapper = new LambdaQueryWrapper<AttClockLocation>()
                .like(locationName != null, AttClockLocation::getLocationName, locationName)
                .eq(status != null, AttClockLocation::getStatus, status)
                .orderByAsc(AttClockLocation::getSort);
        
        return Result.success(locationService.page(page, wrapper));
    }

    @Operation(summary = "获取所有启用的打卡地点")
    @GetMapping("/enabled")
    public Result<List<AttClockLocation>> getEnabledLocations() {
        return Result.success(locationService.getEnabledLocations());
    }

    @Operation(summary = "获取打卡地点详情")
    @GetMapping("/{id}")
    public Result<AttClockLocation> getById(@PathVariable Long id) {
        return Result.success(locationService.getById(id));
    }

    @Operation(summary = "新增打卡地点")
    @PostMapping
    public Result<Boolean> add(@RequestBody AttClockLocation location) {
        return Result.success(locationService.save(location));
    }

    @Operation(summary = "修改打卡地点")
    @PutMapping
    public Result<Boolean> update(@RequestBody AttClockLocation location) {
        return Result.success(locationService.updateById(location));
    }

    @Operation(summary = "删除打卡地点")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(locationService.removeById(id));
    }

    @Operation(summary = "修改打卡地点状态")
    @PutMapping("/{id}/status")
    public Result<Boolean> updateStatus(
            @PathVariable Long id,
            @Parameter(description = "状态:0-禁用 1-启用") @RequestParam Integer status) {
        AttClockLocation location = new AttClockLocation();
        location.setId(id);
        location.setStatus(status);
        return Result.success(locationService.updateById(location));
    }
}
