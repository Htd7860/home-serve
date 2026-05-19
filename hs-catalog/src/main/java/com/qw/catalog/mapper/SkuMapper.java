package com.qw.catalog.mapper;

import com.qw.catalog.entity.ServiceSkus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Author：qw
 * @Package：com.qw.catalog.mapper
 * @Project：home-serve
 * @name：SkuMapper
 * @Date：2026/5/17 19:48
 * @Filename：SkuMapper
 */
@Mapper
public interface SkuMapper {
    @Select("select * from service_skus where category_id=#{id}")
    List<ServiceSkus> getByCategory(Long id);

    @Select("select * from service_skus where id=#{id}")
    ServiceSkus getById(Long id);
}
