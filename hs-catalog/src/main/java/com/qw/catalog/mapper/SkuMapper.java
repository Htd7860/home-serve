package com.qw.catalog.mapper;

import com.qw.catalog.entity.ServiceSkus;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
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
    List<ServiceSkus> getByCategory(Integer id);

    @Select("select * from service_skus where id=#{id}")
    ServiceSkus getById(Long id);

    @Insert("insert into service_skus (category_id, name, description, cover_image, base_price, duration_minutes, unit, sales_count, status, created_at, updated_at) " +
            "values (#{categoryId},#{name},#{description},#{coverImage},#{basePrice},#{durationMinutes},#{unit},#{salesCount},#{status},now(),now()) ")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertWithImg(ServiceSkus sku);

    @Insert("insert into service_skus (category_id, name, description, base_price, duration_minutes, unit, sales_count, status, created_at, updated_at) " +
            "values (#{categoryId},#{name},#{description},#{basePrice},#{durationMinutes},#{unit},#{salesCount},#{status},now(),now()) ")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertWithNoImg(ServiceSkus sku);

    int updateSku(ServiceSkus sku);

    @Select("select id from service_skus")
    List<Long> getAllIds();
}
