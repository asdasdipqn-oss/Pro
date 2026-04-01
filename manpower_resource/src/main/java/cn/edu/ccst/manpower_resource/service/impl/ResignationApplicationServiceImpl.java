package cn.edu.ccst.manpower_resource.service.impl;

import cn.edu.ccst.manpower_resource.entity.ResignationApplication;
import cn.edu.ccst.manpower_resource.mapper.ResignationApplicationMapper;
import cn.edu.ccst.manpower_resource.service.IResignationApplicationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ResignationApplicationServiceImpl extends ServiceImpl<ResignationApplicationMapper, ResignationApplication> 
        implements IResignationApplicationService {
}
