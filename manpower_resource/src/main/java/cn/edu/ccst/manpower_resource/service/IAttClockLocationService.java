package cn.edu.ccst.manpower_resource.service;

import cn.edu.ccst.manpower_resource.entity.AttClockLocation;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;

/**
 * 打卡地点 Service接口
 */
public interface IAttClockLocationService extends IService<AttClockLocation> {

    /**
     * 获取所有启用的打卡地点
     */
    List<AttClockLocation> getEnabledLocations();

    /**
     * 验证打卡位置是否在允许范围内
     * @param latitude 纬度
     * @param longitude 经度
     * @return 如果在范围内返回匹配的地点，否则返回null
     */
    AttClockLocation validateLocation(BigDecimal latitude, BigDecimal longitude);

    /**
     * 计算两点之间的距离（米）
     */
    double calculateDistance(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2);
}
