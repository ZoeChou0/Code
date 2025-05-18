package com.zsh.petsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsh.petsystem.common.Result;
import com.zsh.petsystem.dto.AlipayOrderDTO;
import com.zsh.petsystem.entity.AlipayOrder;
import com.zsh.petsystem.enums.StatusType;
import com.zsh.petsystem.enums.TradeStatusType;
import com.zsh.petsystem.mapper.AlipayOrderMapper;
import com.zsh.petsystem.service.AlipayOrderService;
import com.zsh.petsystem.util.DataTransferUtil;
import com.zsh.petsystem.util.NoUtil;
import com.zsh.petsystem.util.RandomUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Service
public class AlipayOrderServiceImpl extends ServiceImpl<AlipayOrderMapper, AlipayOrder> implements AlipayOrderService {

    @Resource
    private AlipayOrderMapper alipayOrderMapper;

    @Override
    public Result<?> createOrder() {
        // 生成订单号
        String orderId = NoUtil.getOrderNo();
        AlipayOrder alipayOrder = new AlipayOrder();
        alipayOrder.setOrderId(orderId);
        // 设置订单标题
        int quantity = RandomUtil.randomInt(1, 10);
        alipayOrder.setSubject("测试订单" + quantity + "个");
        // 设置总金额
        alipayOrder.setTotalAmount(new BigDecimal(50).multiply(new BigDecimal(quantity)));
        // 设置交易状态
        alipayOrder.setTradeStatus(TradeStatusType.WAIT_BUYER_PAY.getCode());
        alipayOrder.setCreateTime(new Timestamp(new Date().getTime()));
        // 由于没有实际业务，这里随便设置产品码和产品名称（根据实际业务需求取舍即可）
        alipayOrder.setProductCode("FAST_INSTANT_TRADE_PAY");
        alipayOrder.setProductName("测试产品");
        alipayOrder.setCreateTime(new Timestamp(new Date().getTime()));

        alipayOrderMapper.insert(alipayOrder);
        AlipayOrderDTO alipayOrderDTO = DataTransferUtil.shallowCopy(alipayOrder, AlipayOrderDTO.class);
        return Result.success(alipayOrderDTO, "创建订单成功");
    }

    @Override
    public Result<?> getOrderInfo(String orderId) {
        QueryWrapper<AlipayOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        List<AlipayOrder> alipayOrders = alipayOrderMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(alipayOrders)) {
            AlipayOrder alipayOrder = alipayOrders.get(0);
            AlipayOrderDTO alipayOrderDTO = DataTransferUtil.shallowCopy(alipayOrder, AlipayOrderDTO.class);
            return Result.success(alipayOrderDTO, "查询订单成功");
        }
        return Result.failed("查询订单失败");
    }

    @Override
    public Result<?> paySuccess(String orderId, Integer payMethod) {
        AlipayOrder alipayOrder = new AlipayOrder();
        alipayOrder.setOrderId(orderId);
        alipayOrder.setTradeStatus(TradeStatusType.TRADE_SUCCESS.getCode());
        alipayOrder.setPayMethod(payMethod);
        alipayOrder.setGmtPayment(new Timestamp(new Date().getTime()));
        alipayOrderMapper.updateById(alipayOrder);
        // ... 其他业务逻辑（如恢复锁定库存，扣减真实库存等）
        return Result.success("支付成功");
    }

    @Override
    public void paySuccessByOrderId(String orderId, Integer payMethod) {
        QueryWrapper<AlipayOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        queryWrapper.eq("trade_status", TradeStatusType.WAIT_BUYER_PAY.getCode());
        queryWrapper.eq("status", StatusType.ENABLE);
        List<AlipayOrder> alipayOrders = alipayOrderMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(alipayOrders)) {
            AlipayOrder alipayOrder = alipayOrders.get(0);
            paySuccess(alipayOrder.getOrderId(), payMethod);
        }
    }
}
