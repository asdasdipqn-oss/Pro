package cn.edu.ccst.manpower_resource.controller;

import cn.edu.ccst.manpower_resource.common.Result;
import cn.edu.ccst.manpower_resource.dto.ResignationPredictRequest;
import cn.edu.ccst.manpower_resource.service.ResignationPredictionService;
import cn.edu.ccst.manpower_resource.vo.ResignationPredictVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Tag(name = "AI离职预测", description = "基于考勤和请假数据的员工离职风险预测")
@RestController
@RequestMapping("/ai/resignation")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
public class ResignationPredictionController {

    private final ResignationPredictionService resignationPredictionService;

    @Operation(
        summary = "预测员工离职风险",
        description = "通过AI分析员工的考勤率、请假率等数据，预测离职风险并提供管理建议"
    )
    @PostMapping("/predict")
    public Result<ResignationPredictVO> predictResignationRisk(
            @Parameter(description = "预测请求参数", required = true)
            @Valid @RequestBody ResignationPredictRequest request) {
        ResignationPredictVO vo = resignationPredictionService.predictResignationRisk(request);
        return Result.success(vo);
    }

    @Operation(
        summary = "流式预测员工离职风险",
        description = "通过SSE流式输出AI分析结果，避免超时"
    )
    @GetMapping(value = "/predict/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter predictResignationRiskStream(
            @Parameter(description = "员工ID", required = true)
            @RequestParam Long employeeId,
            @Parameter(description = "分析月数")
            @RequestParam(required = false, defaultValue = "6") Integer months) {
        ResignationPredictRequest request = new ResignationPredictRequest();
        request.setEmployeeId(employeeId);
        request.setMonths(months);
        return resignationPredictionService.predictResignationRiskStream(request);
    }

    @Operation(
        summary = "快速预测员工离职风险",
        description = "使用默认参数（6个月）快速预测指定员工的离职风险"
    )
    @GetMapping("/predict/{employeeId}")
    public Result<ResignationPredictVO> quickPredict(
            @Parameter(description = "员工ID", required = true)
            @PathVariable Long employeeId) {
        ResignationPredictRequest request = new ResignationPredictRequest();
        request.setEmployeeId(employeeId);
        request.setMonths(6);
        ResignationPredictVO vo = resignationPredictionService.predictResignationRisk(request);
        return Result.success(vo);
    }

    @Operation(
        summary = "导出离职风险评估报告",
        description = "导出指定员工的离职风险评估报告为Word文档"
    )
    @GetMapping("/export/{employeeId}")
    public void exportReport(
            @Parameter(description = "员工ID", required = true)
            @PathVariable Long employeeId,
            @Parameter(description = "分析月数")
            @RequestParam(required = false, defaultValue = "6") Integer months,
            HttpServletResponse response) throws IOException {
        ResignationPredictRequest request = new ResignationPredictRequest();
        request.setEmployeeId(employeeId);
        request.setMonths(months);
        ResignationPredictVO vo = resignationPredictionService.predictResignationRisk(request);
        
        // 生成Word文档
        XWPFDocument document = generateWordReport(vo);
        
        // 先写入内存
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.write(baos);
        document.close();
        byte[] content = baos.toByteArray();
        
        // 设置响应头
        String fileName = URLEncoder.encode(vo.getEmployeeName() + "-离职风险评估报告.docx", StandardCharsets.UTF_8);
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        response.setContentLength(content.length);
        
        response.getOutputStream().write(content);
        response.getOutputStream().flush();
    }
    
    private XWPFDocument generateWordReport(ResignationPredictVO vo) {
        XWPFDocument document = new XWPFDocument();
        
        // 标题
        XWPFParagraph titlePara = document.createParagraph();
        titlePara.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = titlePara.createRun();
        titleRun.setText("员工离职风险评估报告");
        titleRun.setBold(true);
        titleRun.setFontSize(22);
        titleRun.setFontFamily("微软雅黑");
        titleRun.addBreak();
        
        // 基本信息标题
        addSectionTitle(document, "一、基本信息");
        
        // 基本信息表格
        XWPFTable infoTable = document.createTable(2, 4);
        setTableWidth(infoTable);
        setTableCell(infoTable, 0, 0, "员工姓名");
        setTableCell(infoTable, 0, 1, vo.getEmployeeName());
        setTableCell(infoTable, 0, 2, "所属部门");
        setTableCell(infoTable, 0, 3, vo.getDepartmentName() != null ? vo.getDepartmentName() : "-");
        setTableCell(infoTable, 1, 0, "岗位");
        setTableCell(infoTable, 1, 1, vo.getPositionName() != null ? vo.getPositionName() : "-");
        setTableCell(infoTable, 1, 2, "分析周期");
        setTableCell(infoTable, 1, 3, "最近" + vo.getAnalysisMonths() + "个月");
        
        document.createParagraph(); // 空行
        
        // 风险评估标题
        addSectionTitle(document, "二、风险评估");
        
        String riskText = "HIGH".equals(vo.getRiskLevel()) ? "高风险" : 
                          "MEDIUM".equals(vo.getRiskLevel()) ? "中风险" : "低风险";
        
        XWPFTable riskTable = document.createTable(1, 4);
        setTableWidth(riskTable);
        setTableCell(riskTable, 0, 0, "离职风险等级");
        setTableCell(riskTable, 0, 1, riskText);
        setTableCell(riskTable, 0, 2, "离职概率");
        setTableCell(riskTable, 0, 3, (vo.getResignationProbability() != null ? vo.getResignationProbability() : "0") + "%");
        
        document.createParagraph();
        
        // 考勤数据标题
        addSectionTitle(document, "三、考勤数据");
        
        XWPFTable attTable = document.createTable(2, 6);
        setTableWidth(attTable);
        setTableCell(attTable, 0, 0, "应出勤天数");
        setTableCell(attTable, 0, 1, (vo.getTotalWorkDays() != null ? vo.getTotalWorkDays() : 0) + "天");
        setTableCell(attTable, 0, 2, "实际出勤天数");
        setTableCell(attTable, 0, 3, (vo.getActualAttendDays() != null ? vo.getActualAttendDays() : 0) + "天");
        setTableCell(attTable, 0, 4, "出勤率");
        setTableCell(attTable, 0, 5, (vo.getAttendanceRate() != null ? vo.getAttendanceRate() : "0") + "%");
        setTableCell(attTable, 1, 0, "迟到次数");
        setTableCell(attTable, 1, 1, (vo.getLateCount() != null ? vo.getLateCount() : 0) + "次");
        setTableCell(attTable, 1, 2, "早退次数");
        setTableCell(attTable, 1, 3, (vo.getEarlyLeaveCount() != null ? vo.getEarlyLeaveCount() : 0) + "次");
        setTableCell(attTable, 1, 4, "缺勤次数");
        setTableCell(attTable, 1, 5, (vo.getAbsentCount() != null ? vo.getAbsentCount() : 0) + "次");
        
        document.createParagraph();
        
        // AI分析结果
        addSectionTitle(document, "四、AI分析结果");
        XWPFParagraph analysisPara = document.createParagraph();
        XWPFRun analysisRun = analysisPara.createRun();
        analysisRun.setText(vo.getAiAnalysis() != null ? vo.getAiAnalysis() : "-");
        analysisRun.setFontSize(11);
        analysisRun.setFontFamily("微软雅黑");
        
        document.createParagraph();
        
        // 管理建议
        addSectionTitle(document, "五、管理建议");
        XWPFParagraph suggestionsPara = document.createParagraph();
        XWPFRun suggestionsRun = suggestionsPara.createRun();
        suggestionsRun.setText(vo.getSuggestions() != null ? vo.getSuggestions() : "-");
        suggestionsRun.setFontSize(11);
        suggestionsRun.setFontFamily("微软雅黑");
        
        document.createParagraph();
        
        // 页脚
        XWPFParagraph footerPara = document.createParagraph();
        footerPara.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun footerRun = footerPara.createRun();
        footerRun.setText("报告生成时间：" + java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + 
                " | 中小型企业人力资源管理系统");
        footerRun.setFontSize(9);
        footerRun.setColor("909399");
        footerRun.setFontFamily("微软雅黑");
        
        return document;
    }
    
    private void addSectionTitle(XWPFDocument document, String title) {
        XWPFParagraph para = document.createParagraph();
        XWPFRun run = para.createRun();
        run.setText(title);
        run.setBold(true);
        run.setFontSize(14);
        run.setFontFamily("微软雅黑");
        run.setColor("409EFF");
    }
    
    private void setTableWidth(XWPFTable table) {
        table.setWidth("100%");
    }
    
    private void setTableCell(XWPFTable table, int row, int col, String text) {
        XWPFTableCell cell = table.getRow(row).getCell(col);
        cell.setText(text);
        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
    }
}
