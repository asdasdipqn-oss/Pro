package cn.edu.ccst.manpower_resource.service;

import cn.edu.ccst.manpower_resource.dto.ResignationPredictRequest;
import cn.edu.ccst.manpower_resource.vo.ResignationPredictVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface ResignationPredictionService {

    /**
     * 预测员工离职风险
     * 
     * @param request 预测请求参数
     * @return 预测结果
     */
    ResignationPredictVO predictResignationRisk(ResignationPredictRequest request);

    /**
     * 流式预测员工离职风险（SSE）
     * 
     * @param request 预测请求参数
     * @return SSE发射器
     */
    SseEmitter predictResignationRiskStream(ResignationPredictRequest request);
}
