package org.example.demo4.trade;

public interface PayService {

    /**
     * 发起支付，返回支付流水号。
     * 预留微信/支付宝接入点：未来实现 WechatPayService / AlipayService 替换即可。
     */
    String pay(Order order, String method);
}
