package cn.edu.ccst.manpower_resource.service;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.dto.PageQuery;
import cn.edu.ccst.manpower_resource.dto.SalaryGenerateDTO;
import cn.edu.ccst.manpower_resource.entity.SalaryRecord;
import cn.edu.ccst.manpower_resource.vo.SalaryRecordVO;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

public interface ISalaryRecordService extends IService<SalaryRecord> {

    List<SalaryRecord> getByEmployee(Long employeeId, Integer year);

    SalaryRecord getByMonth(Long employeeId, Integer year, Integer month);

    PageResult<SalaryRecordVO> pageByMonth(Integer year, Integer month, PageQuery query);

    void confirmSalary(Long id);

    void paySalary(Long id);

    /**
     * 生成月度薪资
     * 薪资规则：
     * - 基本工资：3000元
     * - 全勤奖：3000元（请假一天扣100元，扣完为止）
     * - 根据打卡记录计算实际出勤
     */
    int generateMonthlySalary(SalaryGenerateDTO dto);

    /**
     * 一键发放指定月份所有已确认的薪资
     */
    int batchPay(Integer year, Integer month);

    /**
     * 一键确认指定月份所有草稿状态的薪资
     */
    int batchConfirm(Integer year, Integer month);

    /**
     * 重新计算指定记录的薪资
     */
    void recalculate(Long id);

    /**
     * 获取薪资统计信息
     */
    SalarySummary getSummary(Integer year, Integer month);

    /**
     * 薪资统计摘要
     */
    class SalarySummary {
        public int totalCount;       // 总人数
        public int draftCount;       // 草稿数
        public int confirmedCount;   // 已确认数
        public int paidCount;        // 已发放数
        public java.math.BigDecimal totalAmount; // 总金额
    }

    /**
     * 导出薪资数据
     */
    void exportSalary(Integer year, Integer month, HttpServletResponse response);

    /**
     * 推送薪资通知
     */
    int pushSalaryNotification(Integer year, Integer month);

    /**
     * 获取员工未读薪资通知
     */
    List<SalaryRecord> getUnreadNotifications(Long employeeId);

    /**
     * 标记薪资通知为已读
     */
    void markAsRead(Long id, Long employeeId);

    /**
     * 批量删除指定月份的草稿状态薪资记录
     */
    int batchDelete(Integer year, Integer month);
}
