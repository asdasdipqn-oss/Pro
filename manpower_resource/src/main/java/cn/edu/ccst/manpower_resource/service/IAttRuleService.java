package cn.edu.ccst.manpower_resource.service;

import cn.edu.ccst.manpower_resource.entity.AttRule;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 考勤规则表 服务类
 * </p>
 *
 * @author
 * @since 2025-12-17
 */
public interface IAttRuleService extends IService<AttRule> {

    /**
     * 获取默认考勤规则
     * @return 默认考勤规则
     */
    AttRule getDefaultRule();
}
