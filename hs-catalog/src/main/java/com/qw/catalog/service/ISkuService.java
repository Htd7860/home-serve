package com.qw.catalog.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.qw.catalog.entity.ServiceSkus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author：qw
 * @Package：com.qw.catalog.service
 * @Project：home-serve
 * @name：ISkuService
 * @Date：2026/5/17 19:47
 * @Filename：ISkuService
 */
public interface ISkuService {
    List<ServiceSkus> getByCategory(Long id) throws JsonProcessingException;
    ServiceSkus getById(Long id) throws JsonProcessingException;
    BigDecimal[] calculateMoney(LocalDateTime appointTime, BigDecimal baseMoney,boolean haveDis,Double distance);
}
