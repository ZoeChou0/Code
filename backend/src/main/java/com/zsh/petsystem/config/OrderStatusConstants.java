package com.zsh.petsystem.config; // 确保包名正确

public final class OrderStatusConstants {

  private OrderStatusConstants() {
    // 私有构造函数，防止实例化
  }

  // Order Statuses (与数据库中存储的值一致，建议用英文大写)
  public static final String PENDING_PAYMENT = "PENDING_PAYMENT"; // 订单：待支付
  public static final String PAID = "PAID"; // 订单：已支付 (意味着服务待开始或进行中)
  public static final String COMPLETED = "COMPLETED"; // 订单：已完成
  public static final String CANCELLED = "CANCELLED"; // 订单：已取消

  // Reservation Statuses (可以放在这里，或者单独的 ReservationStatusConstants)
  public static final String PENDING_CONFIRMATION = "PENDING_CONFIRMATION"; // 预约：待服务商确认
  public static final String CONFIRMED = "CONFIRMED"; // 预约：服务商已确认
  public static final String AWAITING_PAYMENT = "AWAITING_PAYMENT"; // 预约：等待用户支付 (在订单创建后)
  // public static final String PAID = "PAID"; // Reservation 也可有 PAID 状态，与
  // Order.PAID 含义可能略有不同或同步
  // public static final String COMPLETED = "COMPLETED"; // 预约：也标记为已完成 (与订单同步)
  public static final String CANCELLED_USER = "CANCELLED_USER"; // 预约：用户取消
  public static final String CANCELLED_PROVIDER = "CANCELLED_PROVIDER"; // 预约：服务商取消
  public static final String REJECTED = "REJECTED"; // 预约：服务商拒绝

  // 确保这些值与您数据库中实际存储的值一致，或者在比较时使用 .equalsIgnoreCase()
  // 例如，如果数据库存的是小写 "completed"，那么这里应该是:
  // public static final String COMPLETED = "completed";
}