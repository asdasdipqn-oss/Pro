package cn.edu.ccst.manpower_resource.service;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.dto.EmpEmployeeDTO;
import cn.edu.ccst.manpower_resource.dto.EmpEmployeeQuery;
import cn.edu.ccst.manpower_resource.entity.EmpEmployee;
import cn.edu.ccst.manpower_resource.vo.EmpEmployeeVO;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IEmpEmployeeService extends IService<EmpEmployee> {

    PageResult<EmpEmployeeVO> pageList(EmpEmployeeQuery query);

    EmpEmployee getDetail(Long id);

    void createEmployee(EmpEmployeeDTO dto);

    void updateEmployee(EmpEmployeeDTO dto);

    void deleteEmployee(Long id);

    void updateStatus(Long id, Integer status);

    void exportEmployees(EmpEmployeeQuery query, HttpServletResponse response);

    String importEmployees(MultipartFile file);

    void downloadTemplate(HttpServletResponse response);

    void batchDelete(List<Long> ids);

    void batchUpdateDept(List<Long> ids, Long deptId);

    void batchUpdateStatus(List<Long> ids, Integer status);
}
