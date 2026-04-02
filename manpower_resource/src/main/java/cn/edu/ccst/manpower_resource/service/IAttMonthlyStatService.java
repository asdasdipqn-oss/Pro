package cn.edu.ccst.manpower_resource.service;

import cn.edu.ccst.manpower_resource.entity.AttMonthlyStat;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;

/**
 * <p>
 * 考勤月度汇总表 服务类
 * </p>
 *
 * @author
 * @since 2025-12-17
 */
public interface IAttMonthlyStatService extends IService<AttMonthlyStat> {

    /**
     * 根据打卡记录生成月度统计数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param employeeId 员工ID，为空时生成所有员工
     * @return 生成的记录数
     */
    int generateMonthlyStats(LocalDate startDate, LocalDate endDate, Long employeeId);
}
