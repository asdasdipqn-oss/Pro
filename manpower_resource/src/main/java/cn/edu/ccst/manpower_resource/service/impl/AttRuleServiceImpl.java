package cn.edu.ccst.manpower_resource.service.impl;

import cn.edu.ccst.manpower_resource.entity.AttRule;
import cn.edu.ccst.manpower_resource.mapper.AttRuleMapper;
import cn.edu.ccst.manpower_resource.service.IAttRuleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 考勤规则表 服务实现类
 * </p>
 *
 * @author
 * @since 2025-12-17
 */
@Service
public class AttRuleServiceImpl extends ServiceImpl<AttRuleMapper, AttRule> implements IAttRuleService {

    @Override
    public AttRule getDefaultRule() {
        return baseMapper.selectOne(new LambdaQueryWrapper<AttRule>()
                .eq(AttRule::getStatus, 1) // 启用状态
                .eq(AttRule::getIsDefault, 1) // 默认规则
                .last("LIMIT 1"));
    }
}
