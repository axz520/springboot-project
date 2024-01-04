package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 新增员工
     * @param employeeDTO
     */
    void insert(EmployeeDTO employeeDTO);

    /**
     * 分页查询
     * @param employeePageQueryDTO
     * @return
     */
    PageResult page(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 禁止/启用状态
     * @param id
     * @param status
     */
    void startOrStop(Long id, Integer status);

    /**
     * 根据id获取信息
     * @param id
     * @return
     */
    Employee getById(Long id);

    /**
     * 更新信息
     * @param employeeDTO
     * @return
     */
    void updateInfo(EmployeeDTO employeeDTO);
}
