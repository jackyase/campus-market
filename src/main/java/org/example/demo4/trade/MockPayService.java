package org.example.demo4.trade;

import org.springframework.stereotype.Service;

@Service
public class MockPayService implements PayService {

    @Override
    public String pay(Order order, String method) {
        // 模拟支付，直接成功并生成流水号
        return "MOCK" + System.currentTimeMillis();
    }
}
