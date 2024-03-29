package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    void save(DishDTO dishDTO);

    PageResult page(DishPageQueryDTO dto);

    void deleteDish(List<Long> ids);

    void startOrStop(Integer status, Long id);

    void updateDish(DishDTO dishDTO);

    DishVO getById(Long id);
}
