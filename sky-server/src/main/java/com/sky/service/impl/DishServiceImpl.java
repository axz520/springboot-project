package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorsMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorsMapper dishFlavorsMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Override
    @Transactional
    public void save(DishDTO dishDTO) {
        // 菜品表插入1条数据
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);

        Long id = dish.getId();

        // 口味表插入0-n数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && !flavors.isEmpty()) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(id);
            });
            dishFlavorsMapper.insertBatch(flavors);
        }
    }

    /**
     * 分页查询
     * @param dto
     * @return
     */
    @Override
    public PageResult page(DishPageQueryDTO dto) {
        // 分页limit
        PageHelper.startPage(dto.getPage(), dto.getPageSize());

        // select
        Page<DishVO> dish = dishFlavorsMapper.page(dto);
        List<DishVO> dishResult = dish.getResult();
        return new PageResult(
                dish.getTotal(), dishResult
        );
    }

    /**
     * 删除菜品
     * @param ids
     */
    @Override
    @Transactional
    public void deleteDish(List<Long> ids) {
        // 起售中的菜品不能删除
        ids.forEach(id -> {
            Dish dish = dishMapper.getById(id);
            // 判断status
            if(Objects.equals(dish.getStatus(), StatusConstant.ENABLE)) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        });
        // 被套餐关联的菜品不能删除
        List<Long> setMealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if(setMealIds != null && !setMealIds.isEmpty()) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        // 删除菜品后，关联的口味数据也需要删除
        dishMapper.delete(ids);
        ids.forEach(id -> {
            dishFlavorsMapper.deleteByDishId(id);
        });
    }

    /**
     * 菜品起售停售
     *
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Dish dish = dishMapper.getById(id);
        dish.setStatus(status);
        dishMapper.update(dish);
    }

    /**
     * 修改菜品
     * @param dishDTO
     */
    @Override
    @Transactional
    public void updateDish(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        // 修改到dish
        dishMapper.update(dish);
        // 删除flavor
        dishFlavorsMapper.deleteByDishId(dishDTO.getId());
        // 增加新的flavor
        List<DishFlavor> list = dishDTO.getFlavors();
        if(list != null && !list.isEmpty()) {
            dishFlavorsMapper.insertBatch(list);
            list.forEach(flavor -> {
                flavor.setDishId(dishDTO.getId());
            });
        }
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @Override
    public DishVO getById(Long id) {
        // 获取dish信息
        Dish dish = dishMapper.getById(id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        // 获取flavors
        List<DishFlavor> dishFlavorList = dishFlavorsMapper.getByDishId(id);
        dishVO.setFlavors(dishFlavorList);
        return dishVO;
    }
}
