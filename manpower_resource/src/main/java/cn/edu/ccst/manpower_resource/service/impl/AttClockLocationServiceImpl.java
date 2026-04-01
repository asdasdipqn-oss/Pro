package cn.edu.ccst.manpower_resource.service.impl;

import cn.edu.ccst.manpower_resource.entity.AttClockLocation;
import cn.edu.ccst.manpower_resource.mapper.AttClockLocationMapper;
import cn.edu.ccst.manpower_resource.service.IAttClockLocationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 打卡地点 Service实现
 */
@Service
public class AttClockLocationServiceImpl extends ServiceImpl<AttClockLocationMapper, AttClockLocation> 
        implements IAttClockLocationService {

    // 地球半径（米）
    private static final double EARTH_RADIUS = 6371000;

    @Override
    public List<AttClockLocation> getEnabledLocations() {
        return baseMapper.selectList(new LambdaQueryWrapper<AttClockLocation>()
                .eq(AttClockLocation::getStatus, 1)
                .orderByAsc(AttClockLocation::getSort));
    }

    @Override
    public AttClockLocation validateLocation(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }
        
        List<AttClockLocation> locations = getEnabledLocations();
        for (AttClockLocation location : locations) {
            double distance = calculateDistance(
                    latitude, longitude,
                    location.getLatitude(), location.getLongitude()
            );
            // 如果在范围内，返回该地点
            if (distance <= location.getRadius()) {
                return location;
            }
        }
        return null;
    }

    @Override
    public double calculateDistance(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        // 使用Haversine公式计算两点之间的距离
        double latRad1 = Math.toRadians(lat1.doubleValue());
        double latRad2 = Math.toRadians(lat2.doubleValue());
        double lonRad1 = Math.toRadians(lon1.doubleValue());
        double lonRad2 = Math.toRadians(lon2.doubleValue());

        double deltaLat = latRad2 - latRad1;
        double deltaLon = lonRad2 - lonRad1;

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(latRad1) * Math.cos(latRad2)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}
