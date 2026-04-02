package cn.edu.ccst.manpower_resource.mapper;

import cn.edu.ccst.manpower_resource.entity.OrgPosition;
import cn.edu.ccst.manpower_resource.vo.PositionVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 岗位表 Mapper 接口
 * </p>
 *
 * @author
 * @since 2025-12-17
 */
public interface OrgPositionMapper extends BaseMapper<OrgPosition> {

    /**
     * 查询岗位列表（含部门名称）
     */
    List<PositionVO> selectPositionWithDept(@Param("deptId") Long deptId);

}
