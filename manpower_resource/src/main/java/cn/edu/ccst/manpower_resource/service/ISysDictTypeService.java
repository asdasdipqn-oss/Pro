package cn.edu.ccst.manpower_resource.service;

import cn.edu.ccst.manpower_resource.entity.SysDictData;
import cn.edu.ccst.manpower_resource.entity.SysDictType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ISysDictTypeService extends IService<SysDictType> {

    List<SysDictType> listAll();

    List<SysDictData> getDataByType(String typeCode);

    void createType(SysDictType type);

    void updateType(SysDictType type);

    void deleteType(Long id);
}
