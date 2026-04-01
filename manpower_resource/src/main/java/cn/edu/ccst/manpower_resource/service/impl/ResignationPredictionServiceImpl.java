package cn.edu.ccst.manpower_resource.service.impl;

import cn.edu.ccst.manpower_resource.common.ResultCode;
import cn.edu.ccst.manpower_resource.dto.ResignationPredictRequest;
import cn.edu.ccst.manpower_resource.entity.AttClockRecord;
import cn.edu.ccst.manpower_resource.entity.AttDailyStat;
import cn.edu.ccst.manpower_resource.entity.EmpEmployee;
import cn.edu.ccst.manpower_resource.entity.LeaveApplication;
import cn.edu.ccst.manpower_resource.entity.OrgDepartment;
import cn.edu.ccst.manpower_resource.entity.OrgPosition;
import cn.edu.ccst.manpower_resource.exception.BusinessException;
import cn.edu.ccst.manpower_resource.mapper.AttClockRecordMapper;
import cn.edu.ccst.manpower_resource.mapper.AttDailyStatMapper;
import cn.edu.ccst.manpower_resource.mapper.EmpEmployeeMapper;
import cn.edu.ccst.manpower_resource.mapper.LeaveApplicationMapper;
import cn.edu.ccst.manpower_resource.mapper.OrgDepartmentMapper;
import cn.edu.ccst.manpower_resource.mapper.OrgPositionMapper;
import cn.edu.ccst.manpower_resource.service.ResignationPredictionService;
import cn.edu.ccst.manpower_resource.vo.ResignationPredictVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.data.message.AiMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResignationPredictionServiceImpl implements ResignationPredictionService {

    private final EmpEmployeeMapper employeeMapper;
    private final AttDailyStatMapper dailyStatMapper;
    private final AttClockRecordMapper clockRecordMapper;
    private final LeaveApplicationMapper leaveApplicationMapper;
    private final OrgDepartmentMapper departmentMapper;
    private final OrgPositionMapper positionMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    
    @Value("${deepseek.api.key:***********************************}")
    private String apiKey;

    private static final String DEEPSEEK_BASE_URL = "https://api.deepseek.com/v1";

    @Override
    public ResignationPredictVO predictResignationRisk(ResignationPredictRequest request) {
        Long employeeId = request.getEmployeeId();
        Integer months = request.getMonths() != null ? request.getMonths() : 6;

        // 1. 获取员工信息
        EmpEmployee employee = employeeMapper.selectById(employeeId);
        if (employee == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "员工不存在");
        }

        // 2. 获取部门和岗位信息
        String departmentName = "";
        String positionName = "";
        if (employee.getDeptId() != null) {
            OrgDepartment department = departmentMapper.selectById(employee.getDeptId());
            if (department != null) {
                departmentName = department.getDeptName();
            }
        }
        if (employee.getPositionId() != null) {
            OrgPosition position = positionMapper.selectById(employee.getPositionId());
            if (position != null) {
                positionName = position.getPositionName();
            }
        }

        // 3. 计算分析时间范围
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months);

        // 4. 获取打卡记录数据（直接从打卡表获取）
        List<AttClockRecord> clockRecords = clockRecordMapper.selectList(
                new LambdaQueryWrapper<AttClockRecord>()
                        .eq(AttClockRecord::getEmployeeId, employeeId)
                        .between(AttClockRecord::getClockDate, startDate, endDate)
        );

        // 5. 获取请假数据
        List<LeaveApplication> leaveApplications = leaveApplicationMapper.selectList(
                new LambdaQueryWrapper<LeaveApplication>()
                        .eq(LeaveApplication::getEmployeeId, employeeId)
                        .eq(LeaveApplication::getStatus, 2) // 已通过
                        .ge(LeaveApplication::getStartTime, startDate.atStartOfDay())
                        .le(LeaveApplication::getEndTime, endDate.atTime(23, 59, 59))
        );

        // 6. 计算统计指标
        ResignationPredictVO vo = calculateStatistics(employee, departmentName, positionName, 
                clockRecords, leaveApplications, months, startDate, endDate);

        // 7. 调用AI分析
        String aiAnalysis = analyzeWithAI(vo, employee);
        
        // 8. 解析AI响应并更新VO
        parseAIResponse(vo, aiAnalysis);

        return vo;
    }

    private ResignationPredictVO calculateStatistics(
            EmpEmployee employee,
            String departmentName,
            String positionName,
            List<AttClockRecord> clockRecords,
            List<LeaveApplication> leaveApplications,
            Integer months,
            LocalDate startDate,
            LocalDate endDate) {

        ResignationPredictVO vo = new ResignationPredictVO();
        vo.setEmployeeId(employee.getId());
        vo.setEmployeeName(employee.getEmpName());
        vo.setDepartmentName(departmentName);
        vo.setPositionName(positionName);
        vo.setAnalysisMonths(months);

        // 计算应出勤工作日天数（排除周末）
        int totalWorkDays = 0;
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            int dayOfWeek = current.getDayOfWeek().getValue();
            if (dayOfWeek < 6) { // 周一到周五
                totalWorkDays++;
            }
            current = current.plusDays(1);
        }

        // 按日期分组打卡记录
        Map<LocalDate, List<AttClockRecord>> recordsByDate = clockRecords.stream()
                .collect(Collectors.groupingBy(AttClockRecord::getClockDate));

        // 统计考勤数据
        int lateCount = 0;
        int earlyLeaveCount = 0;
        int actualAttendDays = 0;
        
        // 标准上班时间 9:00，下班时间 18:00
        int standardClockInHour = 9;
        int standardClockInMinute = 0;
        int standardClockOutHour = 18;
        int standardClockOutMinute = 0;

        for (Map.Entry<LocalDate, List<AttClockRecord>> entry : recordsByDate.entrySet()) {
            LocalDate date = entry.getKey();
            List<AttClockRecord> dayRecords = entry.getValue();
            
            // 只要当天有打卡记录就算出勤
            if (!dayRecords.isEmpty()) {
                actualAttendDays++;
                
                // 检查迟到（上班卡）
                AttClockRecord clockIn = dayRecords.stream()
                        .filter(r -> r.getClockType() != null && r.getClockType() == 1)
                        .findFirst().orElse(null);
                if (clockIn != null && clockIn.getClockTime() != null) {
                    int clockInHour = clockIn.getClockTime().getHour();
                    int clockInMinute = clockIn.getClockTime().getMinute();
                    if (clockInHour > standardClockInHour || 
                        (clockInHour == standardClockInHour && clockInMinute > standardClockInMinute)) {
                        lateCount++;
                    }
                }
                
                // 检查早退（下班卡）
                AttClockRecord clockOut = dayRecords.stream()
                        .filter(r -> r.getClockType() != null && r.getClockType() == 2)
                        .findFirst().orElse(null);
                if (clockOut != null && clockOut.getClockTime() != null) {
                    int clockOutHour = clockOut.getClockTime().getHour();
                    int clockOutMinute = clockOut.getClockTime().getMinute();
                    if (clockOutHour < standardClockOutHour || 
                        (clockOutHour == standardClockOutHour && clockOutMinute < standardClockOutMinute)) {
                        earlyLeaveCount++;
                    }
                }
            }
        }
        
        // 缺勤天数 = 应出勤天数 - 实际出勤天数
        int absentDays = totalWorkDays - actualAttendDays;
        if (absentDays < 0) absentDays = 0;

        // 统计请假数据
        BigDecimal totalLeaveDays = BigDecimal.ZERO;
        for (LeaveApplication leave : leaveApplications) {
            if (leave.getDuration() != null) {
                totalLeaveDays = totalLeaveDays.add(leave.getDuration());
            }
        }

        // 计算出勤率 = 实际出勤天数 / 应出勤天数 * 100
        BigDecimal attendanceRate = BigDecimal.ZERO;
        if (totalWorkDays > 0) {
            attendanceRate = BigDecimal.valueOf(actualAttendDays)
                    .divide(BigDecimal.valueOf(totalWorkDays), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            // 限制出勤率最大为100%
            if (attendanceRate.compareTo(BigDecimal.valueOf(100)) > 0) {
                attendanceRate = BigDecimal.valueOf(100);
            }
        }

        vo.setTotalWorkDays(totalWorkDays);
        vo.setActualAttendDays(actualAttendDays);
        vo.setAttendanceRate(attendanceRate);
        vo.setLateCount(lateCount);
        vo.setEarlyLeaveCount(earlyLeaveCount);
        vo.setAbsentCount(absentDays);
        vo.setLeaveDays(totalLeaveDays);

        return vo;
    }

    private String analyzeWithAI(ResignationPredictVO vo, EmpEmployee employee) {
        try {
            // 构建系统提示词
            String systemPrompt = buildSystemPrompt();
            
            // 构建用户输入
            String userInput = buildUserInput(vo, employee);

            // 调用DeepSeek API
            ChatLanguageModel model = OpenAiChatModel.builder()
                    .apiKey(apiKey)
                    .baseUrl(DEEPSEEK_BASE_URL)
                    .modelName("deepseek-chat")
                    .temperature(0.7)
                    .maxTokens(2000)
                    .build();

            String response = model.generate(systemPrompt + "\n\n" + userInput);
            
            log.info("AI分析完成，员工ID: {}, 响应: {}", vo.getEmployeeId(), response);
            
            return response;
        } catch (Exception e) {
            log.error("AI分析失败，员工ID: {}", vo.getEmployeeId(), e);
            return generateFallbackAnalysis(vo);
        }
    }

    private String buildSystemPrompt() {
        return """
                你是一位专业的人力资源数据分析师，擅长通过员工的考勤数据来评估员工的离职风险。
                
                请根据以下员工数据进行分析，并以JSON格式返回分析结果：
                {
                  "resignationProbability": 0-100之间的数字，表示离职概率,
                  "riskLevel": "LOW" | "MEDIUM" | "HIGH",
                  "aiAnalysis": "详细的分析说明，包括对出勤率、迟到早退等数据的解读",
                  "suggestions": "针对该员工的管理建议"
                }
                
                评估标准：
                1. 出勤率低于85%：高风险
                2. 频繁迟到早退（月均超过3次）：中高风险
                3. 缺勤次数多（月均超过2次）：高风险
                4. 综合考虑多个指标，给出合理的离职概率评估
                
                请确保返回的是有效的JSON格式，不要包含其他文字说明。
                """;
    }

    private String buildUserInput(ResignationPredictVO vo, EmpEmployee employee) {
        return String.format("""
                员工基本信息：
                - 姓名：%s
                - 部门：%s
                - 岗位：%s
                - 入职时间：%s
                
                最近%d个月的数据统计：
                - 应出勤天数：%d天
                - 实际出勤天数：%d天
                - 出勤率：%.2f%%
                - 迟到次数：%d次
                - 早退次数：%d次
                - 缺勤次数：%d次
                - 请假天数：%.1f天
                
                请分析该员工的离职风险，并给出专业建议。
                """,
                vo.getEmployeeName(),
                vo.getDepartmentName(),
                vo.getPositionName(),
                employee.getHireDate(),
                vo.getAnalysisMonths(),
                vo.getTotalWorkDays(),
                vo.getActualAttendDays(),
                vo.getAttendanceRate(),
                vo.getLateCount(),
                vo.getEarlyLeaveCount(),
                vo.getAbsentCount(),
                vo.getLeaveDays()
        );
    }

    private void parseAIResponse(ResignationPredictVO vo, String aiResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            
            // 尝试从响应中提取JSON部分
            String jsonStr = aiResponse;
            if (aiResponse.contains("```json")) {
                int start = aiResponse.indexOf("```json") + 7;
                int end = aiResponse.indexOf("```", start);
                if (end > start) {
                    jsonStr = aiResponse.substring(start, end).trim();
                }
            } else if (aiResponse.contains("{")) {
                int start = aiResponse.indexOf("{");
                int end = aiResponse.lastIndexOf("}") + 1;
                if (end > start) {
                    jsonStr = aiResponse.substring(start, end);
                }
            }
            
            JsonNode rootNode = mapper.readTree(jsonStr);
            
            if (rootNode.has("resignationProbability")) {
                vo.setResignationProbability(
                    BigDecimal.valueOf(rootNode.get("resignationProbability").asDouble())
                );
            }
            
            if (rootNode.has("riskLevel")) {
                vo.setRiskLevel(rootNode.get("riskLevel").asText());
            }
            
            if (rootNode.has("aiAnalysis")) {
                vo.setAiAnalysis(rootNode.get("aiAnalysis").asText());
            }
            
            if (rootNode.has("suggestions")) {
                vo.setSuggestions(rootNode.get("suggestions").asText());
            }
            
        } catch (JsonProcessingException e) {
            log.error("解析AI响应失败", e);
            // 使用默认规则计算
            calculateRiskByRules(vo);
            vo.setAiAnalysis(aiResponse);
        }
    }

    private String generateFallbackAnalysis(ResignationPredictVO vo) {
        calculateRiskByRules(vo);
        
        StringBuilder analysis = new StringBuilder();
        analysis.append("根据数据分析：\n");
        analysis.append(String.format("- 应出勤%d天，实际出勤%d天\n", vo.getTotalWorkDays(), vo.getActualAttendDays()));
        analysis.append(String.format("- 出勤率为%.2f%%", vo.getAttendanceRate()));
        
        if (vo.getAttendanceRate().compareTo(BigDecimal.valueOf(85)) < 0) {
            analysis.append("，低于正常水平，存在较大风险\n");
        } else if (vo.getAttendanceRate().compareTo(BigDecimal.valueOf(95)) < 0) {
            analysis.append("，处于正常范围\n");
        } else {
            analysis.append("，表现良好\n");
        }
        
        if (vo.getLateCount() > vo.getAnalysisMonths() * 3) {
            analysis.append(String.format("- 迟到次数较多（%d次），需要加强管理\n", vo.getLateCount()));
        }
        
        vo.setAiAnalysis(analysis.toString());
        
        // 生成建议
        StringBuilder suggestions = new StringBuilder();
        if ("HIGH".equals(vo.getRiskLevel())) {
            suggestions.append("建议：1. 与员工进行深入沟通，了解工作状态；2. 评估是否存在工作压力或个人问题；3. 考虑调整工作安排或提供支持");
        } else if ("MEDIUM".equals(vo.getRiskLevel())) {
            suggestions.append("建议：1. 定期关注员工状态；2. 适当调整工作安排；3. 加强团队建设");
        } else {
            suggestions.append("建议：1. 保持现有管理方式；2. 继续关注员工发展");
        }
        vo.setSuggestions(suggestions.toString());
        
        return analysis.toString();
    }

    private void calculateRiskByRules(ResignationPredictVO vo) {
        int riskScore = 0;
        
        // 出勤率评分
        if (vo.getAttendanceRate().compareTo(BigDecimal.valueOf(85)) < 0) {
            riskScore += 40;
        } else if (vo.getAttendanceRate().compareTo(BigDecimal.valueOf(95)) < 0) {
            riskScore += 20;
        }
        
        // 迟到早退评分
        int avgLatePerMonth = vo.getLateCount() / vo.getAnalysisMonths();
        if (avgLatePerMonth > 3) {
            riskScore += 20;
        } else if (avgLatePerMonth > 1) {
            riskScore += 10;
        }
        
        // 缺勤评分
        int avgAbsentPerMonth = vo.getAbsentCount() / vo.getAnalysisMonths();
        if (avgAbsentPerMonth > 2) {
            riskScore += 10;
        }
        
        vo.setResignationProbability(BigDecimal.valueOf(riskScore));
        
        if (riskScore >= 60) {
            vo.setRiskLevel("HIGH");
        } else if (riskScore >= 30) {
            vo.setRiskLevel("MEDIUM");
        } else {
            vo.setRiskLevel("LOW");
        }
    }

    @Override
    public SseEmitter predictResignationRiskStream(ResignationPredictRequest request) {
        // 创建SSE发射器，设置5分钟超时
        SseEmitter emitter = new SseEmitter(300000L);
        
        executorService.execute(() -> {
            try {
                Long employeeId = request.getEmployeeId();
                Integer months = request.getMonths() != null ? request.getMonths() : 6;

                // 1. 获取员工信息
                EmpEmployee employee = employeeMapper.selectById(employeeId);
                if (employee == null) {
                    sendSseError(emitter, "员工不存在");
                    return;
                }

                // 2. 获取部门和岗位信息
                String departmentName = "";
                String positionName = "";
                if (employee.getDeptId() != null) {
                    OrgDepartment department = departmentMapper.selectById(employee.getDeptId());
                    if (department != null) {
                        departmentName = department.getDeptName();
                    }
                }
                if (employee.getPositionId() != null) {
                    OrgPosition position = positionMapper.selectById(employee.getPositionId());
                    if (position != null) {
                        positionName = position.getPositionName();
                    }
                }

                // 3. 计算分析时间范围
                LocalDate endDate = LocalDate.now();
                LocalDate startDate = endDate.minusMonths(months);

                // 4. 获取打卡记录数据
                List<AttClockRecord> clockRecords = clockRecordMapper.selectList(
                        new LambdaQueryWrapper<AttClockRecord>()
                                .eq(AttClockRecord::getEmployeeId, employeeId)
                                .between(AttClockRecord::getClockDate, startDate, endDate)
                );

                // 5. 获取请假数据
                List<LeaveApplication> leaveApplications = leaveApplicationMapper.selectList(
                        new LambdaQueryWrapper<LeaveApplication>()
                                .eq(LeaveApplication::getEmployeeId, employeeId)
                                .eq(LeaveApplication::getStatus, 2)
                                .ge(LeaveApplication::getStartTime, startDate.atStartOfDay())
                                .le(LeaveApplication::getEndTime, endDate.atTime(23, 59, 59))
                );

                // 6. 计算统计指标
                ResignationPredictVO vo = calculateStatistics(employee, departmentName, positionName,
                        clockRecords, leaveApplications, months, startDate, endDate);

                // 7. 发送基础数据
                sendSseEvent(emitter, "stats", objectMapper.writeValueAsString(vo));

                // 8. 流式调用AI分析
                streamAnalyzeWithAI(emitter, vo, employee);

            } catch (Exception e) {
                log.error("SSE流式预测失败", e);
                sendSseError(emitter, e.getMessage());
            }
        });

        emitter.onCompletion(() -> log.info("SSE连接完成"));
        emitter.onTimeout(() -> log.warn("SSE连接超时"));
        emitter.onError((ex) -> log.error("SSE连接错误", ex));

        return emitter;
    }

    private void streamAnalyzeWithAI(SseEmitter emitter, ResignationPredictVO vo, EmpEmployee employee) {
        try {
            String systemPrompt = buildSystemPrompt();
            String userInput = buildUserInput(vo, employee);
            String fullPrompt = systemPrompt + "\n\n" + userInput;

            // 使用流式API
            StreamingChatLanguageModel streamingModel = OpenAiStreamingChatModel.builder()
                    .apiKey(apiKey)
                    .baseUrl(DEEPSEEK_BASE_URL)
                    .modelName("deepseek-chat")
                    .temperature(0.7)
                    .build();

            StringBuilder fullResponse = new StringBuilder();

            streamingModel.generate(fullPrompt, new StreamingResponseHandler<AiMessage>() {
                @Override
                public void onNext(String token) {
                    fullResponse.append(token);
                    // 流式发送每个token
                    sendSseEvent(emitter, "token", token);
                }

                @Override
                public void onComplete(Response<AiMessage> response) {
                    try {
                        // 解析完整的AI响应
                        parseAIResponse(vo, fullResponse.toString());
                        // 发送最终结果
                        sendSseEvent(emitter, "result", objectMapper.writeValueAsString(vo));
                        sendSseEvent(emitter, "done", "completed");
                        emitter.complete();
                    } catch (Exception e) {
                        log.error("AI响应解析失败", e);
                        sendSseError(emitter, e.getMessage());
                    }
                }

                @Override
                public void onError(Throwable error) {
                    log.error("AI流式调用失败", error);
                    // 回退到规则计算
                    generateFallbackAnalysis(vo);
                    try {
                        sendSseEvent(emitter, "result", objectMapper.writeValueAsString(vo));
                        sendSseEvent(emitter, "done", "completed");
                        emitter.complete();
                    } catch (Exception e) {
                        sendSseError(emitter, e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            log.error("AI流式分析失败", e);
            generateFallbackAnalysis(vo);
            try {
                sendSseEvent(emitter, "result", objectMapper.writeValueAsString(vo));
                sendSseEvent(emitter, "done", "completed");
                emitter.complete();
            } catch (Exception ex) {
                sendSseError(emitter, ex.getMessage());
            }
        }
    }

    private void sendSseEvent(SseEmitter emitter, String eventName, String data) {
        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(data));
        } catch (IOException e) {
            log.error("SSE发送失败", e);
        }
    }

    private void sendSseError(SseEmitter emitter, String message) {
        try {
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data(message));
            emitter.complete();
        } catch (IOException e) {
            log.error("SSE发送错误信息失败", e);
            emitter.completeWithError(e);
        }
    }
}
