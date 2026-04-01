package cn.edu.ccst.manpower_resource.service.impl;

import cn.edu.ccst.manpower_resource.common.PageResult;
import cn.edu.ccst.manpower_resource.common.ResultCode;
import cn.edu.ccst.manpower_resource.dto.EmpEmployeeDTO;
import cn.edu.ccst.manpower_resource.dto.EmpEmployeeQuery;
import cn.edu.ccst.manpower_resource.entity.EmpEmployee;
import cn.edu.ccst.manpower_resource.entity.LeaveApplication;
import cn.edu.ccst.manpower_resource.entity.OrgDepartment;
import cn.edu.ccst.manpower_resource.entity.OrgPosition;
import cn.edu.ccst.manpower_resource.entity.SysUser;
import cn.edu.ccst.manpower_resource.exception.BusinessException;
import cn.edu.ccst.manpower_resource.mapper.EmpEmployeeMapper;
import cn.edu.ccst.manpower_resource.mapper.LeaveApplicationMapper;
import cn.edu.ccst.manpower_resource.mapper.OrgDepartmentMapper;
import cn.edu.ccst.manpower_resource.mapper.OrgPositionMapper;
import cn.edu.ccst.manpower_resource.mapper.SysUserMapper;
import cn.edu.ccst.manpower_resource.service.IEmpEmployeeService;
import cn.edu.ccst.manpower_resource.vo.EmpEmployeeVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmpEmployeeServiceImpl extends ServiceImpl<EmpEmployeeMapper, EmpEmployee> implements IEmpEmployeeService {

    private final OrgDepartmentMapper departmentMapper;
    private final OrgPositionMapper positionMapper;
    private final SysUserMapper sysUserMapper;
    private final LeaveApplicationMapper leaveApplicationMapper;

    @Override
    public PageResult<EmpEmployeeVO> pageList(EmpEmployeeQuery query) {
        Page<EmpEmployee> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<EmpEmployee> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getEmpCode()), EmpEmployee::getEmpCode, query.getEmpCode())
                .like(StringUtils.hasText(query.getEmpName()), EmpEmployee::getEmpName, query.getEmpName())
                .eq(query.getDeptId() != null, EmpEmployee::getDeptId, query.getDeptId())
                .eq(query.getPositionId() != null, EmpEmployee::getPositionId, query.getPositionId())
                .eq(query.getEmpStatus() != null, EmpEmployee::getEmpStatus, query.getEmpStatus())
                .eq(query.getEmpType() != null, EmpEmployee::getEmpType, query.getEmpType())
                .orderByDesc(EmpEmployee::getCreateTime);

        Page<EmpEmployee> result = baseMapper.selectPage(page, wrapper);
        List<EmpEmployeeVO> voList = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), voList);
    }

    @Override
    public EmpEmployee getDetail(Long id) {
        EmpEmployee emp = baseMapper.selectById(id);
        if (emp == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return emp;
    }

    @Override
    @Transactional
    public void createEmployee(EmpEmployeeDTO dto) {
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<EmpEmployee>().eq(EmpEmployee::getEmpCode, dto.getEmpCode()));
        if (count > 0) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "Emp code exists");
        }

        EmpEmployee emp = new EmpEmployee();
        BeanUtils.copyProperties(dto, emp);
        emp.setDeleted(0);
        emp.setCreateTime(LocalDateTime.now());
        emp.setUpdateTime(LocalDateTime.now());
        baseMapper.insert(emp);
    }

    @Override
    @Transactional
    public void updateEmployee(EmpEmployeeDTO dto) {
        EmpEmployee emp = baseMapper.selectById(dto.getId());
        if (emp == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        BeanUtils.copyProperties(dto, emp, "id", "empCode", "deleted", "createTime");
        emp.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(emp);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        EmpEmployee emp = baseMapper.selectById(id);
        if (emp == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        
        // 检查是否有关联的系统用户
        SysUser sysUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getEmployeeId, id)
                .eq(SysUser::getDeleted, 0));
        if (sysUser != null) {
            throw new BusinessException("该员工已关联系统用户，不能删除");
        }
        
        // 使用 MyBatis Plus 的逻辑删除
        boolean result = removeById(id);
        if (!result) {
            throw new BusinessException("删除员工失败");
        }
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        EmpEmployee emp = baseMapper.selectById(id);
        if (emp == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        
        Integer oldStatus = emp.getEmpStatus();
        emp.setEmpStatus(status);
        emp.setUpdateTime(LocalDateTime.now());
        
        // 如果是离职（状态2），进行联动处理
        if (status == 2 && (oldStatus == null || oldStatus != 2)) {
            emp.setLeaveDate(LocalDate.now()); // 设置离职日期
            handleResignation(emp.getId());
        }
        
        baseMapper.updateById(emp);
    }
    
    /**
     * 离职联动处理：冻结用户账号、取消待审批的请假申请
     */
    private void handleResignation(Long employeeId) {
        // 1. 冻结关联的系统用户账号
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getEmployeeId, employeeId)
                .eq(SysUser::getDeleted, 0));
        if (user != null) {
            user.setStatus(0); // 冻结账号
            user.setUpdateTime(LocalDateTime.now());
            sysUserMapper.updateById(user);
        }
        
        // 2. 取消该员工所有待审批的请假申请（状态0-待审批, 1-审批中）
        List<LeaveApplication> pendingLeaves = leaveApplicationMapper.selectList(
                new LambdaQueryWrapper<LeaveApplication>()
                        .eq(LeaveApplication::getEmployeeId, employeeId)
                        .in(LeaveApplication::getStatus, 0, 1));
        for (LeaveApplication leave : pendingLeaves) {
            leave.setStatus(4); // 已撤回
            leave.setUpdateTime(LocalDateTime.now());
            leaveApplicationMapper.updateById(leave);
        }
    }

    private EmpEmployeeVO toVO(EmpEmployee emp) {
        EmpEmployeeVO vo = new EmpEmployeeVO();
        BeanUtils.copyProperties(emp, vo);

        if (emp.getDeptId() != null) {
            OrgDepartment dept = departmentMapper.selectById(emp.getDeptId());
            if (dept != null) vo.setDepartmentName(dept.getDeptName());
        }
        if (emp.getPositionId() != null) {
            OrgPosition pos = positionMapper.selectById(emp.getPositionId());
            if (pos != null) vo.setPositionName(pos.getPositionName());
        }
        return vo;
    }

    @Override
    public void exportEmployees(EmpEmployeeQuery query, HttpServletResponse response) {
        // 查询数据
        query.setPageNum(1);
        query.setPageSize(10000);
        List<EmpEmployeeVO> list = pageList(query).getRecords();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("员工列表");
            
            // 表头
            String[] headers = {"工号", "姓名", "性别", "手机号", "部门", "岗位", "状态", "入职日期", "身份证号", "邮箱"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // 数据行
            for (int i = 0; i < list.size(); i++) {
                EmpEmployeeVO emp = list.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(emp.getEmpCode() != null ? emp.getEmpCode() : "");
                row.createCell(1).setCellValue(emp.getEmpName() != null ? emp.getEmpName() : "");
                row.createCell(2).setCellValue(emp.getGender() != null ? (emp.getGender() == 1 ? "男" : "女") : "");
                row.createCell(3).setCellValue(emp.getPhone() != null ? emp.getPhone() : "");
                row.createCell(4).setCellValue(emp.getDepartmentName() != null ? emp.getDepartmentName() : "");
                row.createCell(5).setCellValue(emp.getPositionName() != null ? emp.getPositionName() : "");
                row.createCell(6).setCellValue(getStatusText(emp.getEmpStatus()));
                row.createCell(7).setCellValue(emp.getHireDate() != null ? emp.getHireDate().toString() : "");
                row.createCell(8).setCellValue(emp.getIdCard() != null ? emp.getIdCard() : "");
                row.createCell(9).setCellValue(emp.getEmail() != null ? emp.getEmail() : "");
            }

            // 设置响应
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=" + 
                    URLEncoder.encode("员工列表.xlsx", StandardCharsets.UTF_8));
            workbook.write(response.getOutputStream());
        } catch (IOException e) {
            throw new BusinessException("导出失败: " + e.getMessage());
        }
    }

    @Override
    public String importEmployees(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        List<EmpEmployee> employees = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;
        StringBuilder failMsg = new StringBuilder();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    EmpEmployee emp = new EmpEmployee();
                    emp.setEmpCode(getCellStringValue(row.getCell(0)));
                    emp.setEmpName(getCellStringValue(row.getCell(1)));
                    String gender = getCellStringValue(row.getCell(2));
                    emp.setGender("男".equals(gender) ? 1 : 2);
                    emp.setPhone(getCellStringValue(row.getCell(3)));
                    
                    // 根据部门名称查找部门ID
                    String deptName = getCellStringValue(row.getCell(4));
                    if (StringUtils.hasText(deptName)) {
                        OrgDepartment dept = departmentMapper.selectOne(new LambdaQueryWrapper<OrgDepartment>()
                                .eq(OrgDepartment::getDeptName, deptName.trim())
                                .eq(OrgDepartment::getDeleted, 0)
                                .last("LIMIT 1"));
                        if (dept != null) {
                            emp.setDeptId(dept.getId());
                        }
                    }
                    
                    // 根据岗位名称查找岗位ID
                    String positionName = getCellStringValue(row.getCell(5));
                    if (StringUtils.hasText(positionName)) {
                        OrgPosition position = positionMapper.selectOne(new LambdaQueryWrapper<OrgPosition>()
                                .eq(OrgPosition::getPositionName, positionName.trim())
                                .eq(OrgPosition::getDeleted, 0)
                                .last("LIMIT 1"));
                        if (position != null) {
                            emp.setPositionId(position.getId());
                        }
                    }
                    
                    String statusStr = getCellStringValue(row.getCell(6));
                    emp.setEmpStatus(parseStatus(statusStr));
                    String hireDateStr = getCellStringValue(row.getCell(7));
                    if (StringUtils.hasText(hireDateStr)) {
                        emp.setHireDate(LocalDate.parse(hireDateStr, DateTimeFormatter.ISO_DATE));
                    }
                    emp.setIdCard(getCellStringValue(row.getCell(8)));
                    emp.setEmail(getCellStringValue(row.getCell(9)));
                    emp.setDeleted(0);
                    emp.setCreateTime(LocalDateTime.now());
                    emp.setUpdateTime(LocalDateTime.now());

                    // 检查工号是否已存在
                    if (StringUtils.hasText(emp.getEmpCode())) {
                        Long count = baseMapper.selectCount(new LambdaQueryWrapper<EmpEmployee>()
                                .eq(EmpEmployee::getEmpCode, emp.getEmpCode()));
                        if (count > 0) {
                            failCount++;
                            failMsg.append("第").append(i + 1).append("行工号已存在; ");
                            continue;
                        }
                    }

                    employees.add(emp);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    failMsg.append("第").append(i + 1).append("行解析失败; ");
                }
            }

            // 批量插入
            if (!employees.isEmpty()) {
                saveBatch(employees);
            }
        } catch (IOException e) {
            throw new BusinessException("读取文件失败: " + e.getMessage());
        }

        return "成功导入" + successCount + "条，失败" + failCount + "条。" + 
                (failMsg.length() > 0 ? "失败原因：" + failMsg : "");
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("员工导入模板");
            
            String[] headers = {"工号*", "姓名*", "性别(男/女)", "手机号", "部门名称", "岗位名称", "状态(在职/离职/试用期)", "入职日期(yyyy-MM-dd)", "身份证号", "邮箱"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                sheet.setColumnWidth(i, 4000);
            }

            // 示例数据
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("EMP001");
            exampleRow.createCell(1).setCellValue("张三");
            exampleRow.createCell(2).setCellValue("男");
            exampleRow.createCell(3).setCellValue("13800138000");
            exampleRow.createCell(4).setCellValue("技术部");
            exampleRow.createCell(5).setCellValue("Java开发");
            exampleRow.createCell(6).setCellValue("在职");
            exampleRow.createCell(7).setCellValue("2024-01-01");
            exampleRow.createCell(8).setCellValue("110101199001011234");
            exampleRow.createCell(9).setCellValue("zhangsan@example.com");

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=" + 
                    URLEncoder.encode("员工导入模板.xlsx", StandardCharsets.UTF_8));
            workbook.write(response.getOutputStream());
        } catch (IOException e) {
            throw new BusinessException("下载模板失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        // 使用 MyBatis Plus 的逻辑删除
        removeBatchByIds(ids);
    }

    @Override
    @Transactional
    public void batchUpdateDept(List<Long> ids, Long deptId) {
        if (ids == null || ids.isEmpty()) return;
        baseMapper.selectList(new LambdaQueryWrapper<EmpEmployee>().in(EmpEmployee::getId, ids))
                .forEach(emp -> {
                    emp.setDeptId(deptId);
                    emp.setUpdateTime(LocalDateTime.now());
                    baseMapper.updateById(emp);
                });
    }

    @Override
    @Transactional
    public void batchUpdateStatus(List<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty()) return;
        baseMapper.selectList(new LambdaQueryWrapper<EmpEmployee>().in(EmpEmployee::getId, ids))
                .forEach(emp -> {
                    emp.setEmpStatus(status);
                    emp.setUpdateTime(LocalDateTime.now());
                    baseMapper.updateById(emp);
                });
    }

    private String getStatusText(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 1: return "在职";
            case 2: return "离职";
            case 3: return "试用期";
            default: return "";
        }
    }

    private Integer parseStatus(String statusStr) {
        if ("在职".equals(statusStr)) return 1;
        if ("离职".equals(statusStr)) return 2;
        if ("试用期".equals(statusStr)) return 3;
        return 1;
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }
}
