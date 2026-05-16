package com.qw.catalog.service.impl;

import com.qw.catalog.entity.ServiceSkus;
import com.qw.catalog.mapper.ServiceSkusMapper;
import com.qw.catalog.service.IServiceSkusService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务SKU 服务实现类
 * </p>
 *
 * @author qw
 * @since 2026-05-16
 */
@Service
public class ServiceSkusServiceImpl extends ServiceImpl<ServiceSkusMapper, ServiceSkus> implements IServiceSkusService {

}
