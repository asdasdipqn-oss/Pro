package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "测试登录", description = "测试登录接口")
@RestController
@RequestMapping("/test-login")
public class TestLoginController {

    @Operation(summary = "测试登录（不使用Spring Security）")
    @PostMapping
    public Result<String> testLogin(@RequestParam String username) {
        return Result.success("测试成功，用户：" + username);
    }
}
