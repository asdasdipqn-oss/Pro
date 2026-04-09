package cn.edu.ccst.manpower_resource.mapper;

import cn.edu.ccst.manpower_resource.entity.RecruitApplication;
import cn.edu.ccst.manpower_resource.vo.RecruitApplicationVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 招聘投递记录Mapper接口
 */
@Mapper
public interface RecruitApplicationMapper extends BaseMapper<RecruitApplication> {

    /**
     * 根据求职者ID查询投递记录
     */
    List<RecruitApplicationVO> selectByJobSeeker(@Param("jobSeekerId") Long jobSeekerId);

    /**
     * 根据ID查询投递记录详情（返回VO）
     */
    RecruitApplicationVO selectVOById(@Param("id") Long id);

    /**
     * 插入投递申请
     */
    void insertApplication(@Param("request") cn.edu.ccst.manpower_resource.dto.JobApplicationRequest request, @Param("jobSeekerId") Long jobSeekerId);
}
