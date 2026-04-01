package cn.edu.ccst.manpower_resource.service;

import java.util.Map;

/**
 * 仪表盘服务接口
 */
public interface IDashboardService {
    
    /**
     * 获取统计数据
     */
    Map<String, Object> getStats();
}
