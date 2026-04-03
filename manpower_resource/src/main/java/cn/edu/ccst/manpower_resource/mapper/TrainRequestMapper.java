package cn.edu.ccst.manpower_resource.mapper;

import cn.edu.ccst.manpower_resource.entity.TrainRequest;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 培训需求表 Mapper 接口
 * </p>
 *
 * @author
 * @since 2026-04-03
 */
public interface TrainRequestMapper extends BaseMapper<TrainRequest> {

    /**
     * 查询用户的培训需求列表
     */
    List<TrainRequest> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询待审批的培训需求列表
     */
    List<TrainRequest> selectPendingApprovals();
}
