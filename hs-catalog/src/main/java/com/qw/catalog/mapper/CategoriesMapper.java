package com.qw.catalog.mapper;

import com.qw.catalog.entity.ServiceCategories;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Author：qw
 * @Package：com.qw.catalog.mapper
 * @Project：home-serve
 * @name：CategoriesMapper
 * @Date：2026/5/17 18:00
 * @Filename：CategoriesMapper
 */
@Mapper
public interface CategoriesMapper {

    @Select("select * from service_categories")
    List<ServiceCategories> list();

    @Select("select * from service_categories where id=#{id}")
    ServiceCategories getById(Long id);


    void insertWithOne(ServiceCategories categories);

    int updateCategoryById(ServiceCategories categories);

    @Delete("delete from service_categories where id=#{id}")
    int deleteById(Integer id);
}
