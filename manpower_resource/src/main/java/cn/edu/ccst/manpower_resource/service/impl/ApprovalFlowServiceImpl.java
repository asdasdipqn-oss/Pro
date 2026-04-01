package cn.edu.ccst.manpower_resource.service.impl;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.common.ResultCode;
import cn.edu.ccst.manpower_resource.dto.ApprovalFlowDTO;
import cn.edu.ccst.manpower_resource.dto.ApprovalFlowNodeDTO;
import cn.edu.ccst.manpower_resource.dto.PageQuery;
import cn.edu.ccst.manpower_resource.entity.ApprovalFlow;
import cn.edu.ccst.manpower_resource.entity.ApprovalFlowNode;
import cn.edu.ccst.manpower_resource.exception.BusinessException;
import cn.edu.ccst.manpower_resource.mapper.ApprovalFlowMapper;
import cn.edu.ccst.manpower_resource.mapper.ApprovalFlowNodeMapper;
import cn.edu.ccst.manpower_resource.service.IApprovalFlowService;
import cn.edu.ccst.manpower_resource.vo.ApprovalFlowNodeVO;
import cn.edu.ccst.manpower_resource.vo.ApprovalFlowVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 审批流程配置表 服务实现类
 */
@Service
@RequiredArgsConstructor
public class ApprovalFlowServiceImpl extends ServiceImpl<ApprovalFlowMapper, ApprovalFlow> implements IApprovalFlowService {

    private final ApprovalFlowNodeMapper flowNodeMapper;

    @Override
    @Transactional
    public ApprovalFlowVO create(ApprovalFlowDTO dto) {
        // 检查编码是否重复
        ApprovalFlow exists = baseMapper.selectOne(new LambdaQueryWrapper<ApprovalFlow>()
                .eq(ApprovalFlow::getFlowCode, dto.getFlowCode())
                .eq(ApprovalFlow::getDeleted, 0));
        if (exists != null) {
            throw new BusinessException("流程编码已存在");
        }

        // 创建流程
        ApprovalFlow flow = new ApprovalFlow();
        flow.setFlowCode(dto.getFlowCode());
        flow.setFlowName(dto.getFlowName());
        flow.setFlowType(dto.getFlowType());
        flow.setDescription(dto.getDescription());
        flow.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        flow.setDeleted(0);
        flow.setCreateTime(LocalDateTime.now());
        flow.setUpdateTime(LocalDateTime.now());
        baseMapper.insert(flow);

        // 创建节点
        if (dto.getNodes() != null && !dto.getNodes().isEmpty()) {
            for (ApprovalFlowNodeDTO nodeDTO : dto.getNodes()) {
                ApprovalFlowNode node = new ApprovalFlowNode();
                node.setFlowId(flow.getId());
                node.setNodeName(nodeDTO.getNodeName());
                node.setNodeOrder(nodeDTO.getNodeOrder());
                node.setApproverType(nodeDTO.getApproverType());
                node.setApproverId(nodeDTO.getApproverId());
                node.setCreateTime(LocalDateTime.now());
                flowNodeMapper.insert(node);
            }
        }

        return getDetail(flow.getId());
    }

    @Override
    @Transactional
    public ApprovalFlowVO update(ApprovalFlowDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("流程ID不能为空");
        }

        ApprovalFlow flow = baseMapper.selectById(dto.getId());
        if (flow == null || flow.getDeleted() == 1) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        // 检查编码是否重复
        ApprovalFlow exists = baseMapper.selectOne(new LambdaQueryWrapper<ApprovalFlow>()
                .eq(ApprovalFlow::getFlowCode, dto.getFlowCode())
                .ne(ApprovalFlow::getId, dto.getId())
                .eq(ApprovalFlow::getDeleted, 0));
        if (exists != null) {
            throw new BusinessException("流程编码已存在");
        }

        // 更新流程
        flow.setFlowCode(dto.getFlowCode());
        flow.setFlowName(dto.getFlowName());
        flow.setFlowType(dto.getFlowType());
        flow.setDescription(dto.getDescription());
        if (dto.getStatus() != null) {
            flow.setStatus(dto.getStatus());
        }
        flow.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(flow);

        // 删除原有节点，重新创建
        flowNodeMapper.delete(new LambdaQueryWrapper<ApprovalFlowNode>()
                .eq(ApprovalFlowNode::getFlowId, flow.getId()));

        if (dto.getNodes() != null && !dto.getNodes().isEmpty()) {
            for (ApprovalFlowNodeDTO nodeDTO : dto.getNodes()) {
                ApprovalFlowNode node = new ApprovalFlowNode();
                node.setFlowId(flow.getId());
                node.setNodeName(nodeDTO.getNodeName());
                node.setNodeOrder(nodeDTO.getNodeOrder());
                node.setApproverType(nodeDTO.getApproverType());
                node.setApproverId(nodeDTO.getApproverId());
                node.setCreateTime(LocalDateTime.now());
                flowNodeMapper.insert(node);
            }
        }

        return getDetail(flow.getId());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ApprovalFlow flow = baseMapper.selectById(id);
        if (flow == null || flow.getDeleted() == 1) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        // 使用 MyBatis Plus 的逻辑删除
        removeById(id);

        // 删除节点
        flowNodeMapper.delete(new LambdaQueryWrapper<ApprovalFlowNode>()
                .eq(ApprovalFlowNode::getFlowId, id));
    }

    @Override
    public ApprovalFlowVO getDetail(Long id) {
        ApprovalFlow flow = baseMapper.selectById(id);
        if (flow == null || flow.getDeleted() == 1) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        ApprovalFlowVO vo = new ApprovalFlowVO();
        BeanUtils.copyProperties(flow, vo);

        // 查询节点
        List<ApprovalFlowNode> nodes = flowNodeMapper.selectList(new LambdaQueryWrapper<ApprovalFlowNode>()
                .eq(ApprovalFlowNode::getFlowId, id)
                .orderByAsc(ApprovalFlowNode::getNodeOrder));

        List<ApprovalFlowNodeVO> nodeVOs = nodes.stream().map(node -> {
            ApprovalFlowNodeVO nodeVO = new ApprovalFlowNodeVO();
            BeanUtils.copyProperties(node, nodeVO);
            return nodeVO;
        }).collect(Collectors.toList());

        vo.setNodes(nodeVOs);
        return vo;
    }

    @Override
    public PageResult<ApprovalFlowVO> page(Integer flowType, Integer status, PageQuery query) {
        Page<ApprovalFlow> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<ApprovalFlow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalFlow::getDeleted, 0);
        if (flowType != null) {
            wrapper.eq(ApprovalFlow::getFlowType, flowType);
        }
        if (status != null) {
            wrapper.eq(ApprovalFlow::getStatus, status);
        }
        wrapper.orderByDesc(ApprovalFlow::getCreateTime);

        Page<ApprovalFlow> result = baseMapper.selectPage(page, wrapper);

        List<ApprovalFlowVO> voList = result.getRecords().stream().map(flow -> {
            ApprovalFlowVO vo = new ApprovalFlowVO();
            BeanUtils.copyProperties(flow, vo);
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), voList);
    }

    @Override
    public List<ApprovalFlowVO> listEnabled(Integer flowType) {
        LambdaQueryWrapper<ApprovalFlow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalFlow::getDeleted, 0)
                .eq(ApprovalFlow::getStatus, 1);
        if (flowType != null) {
            wrapper.eq(ApprovalFlow::getFlowType, flowType);
        }
        wrapper.orderByAsc(ApprovalFlow::getFlowType);

        List<ApprovalFlow> list = baseMapper.selectList(wrapper);
        return list.stream().map(flow -> {
            ApprovalFlowVO vo = new ApprovalFlowVO();
            BeanUtils.copyProperties(flow, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public ApprovalFlow getByType(Integer flowType) {
        return baseMapper.selectOne(new LambdaQueryWrapper<ApprovalFlow>()
                .eq(ApprovalFlow::getFlowType, flowType)
                .eq(ApprovalFlow::getStatus, 1)
                .eq(ApprovalFlow::getDeleted, 0)
                .last("LIMIT 1"));
    }
}
