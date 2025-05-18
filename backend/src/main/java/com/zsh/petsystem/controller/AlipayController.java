// package com.zsh.petsystem.controller;

// import com.zsh.petsystem.common.Result;
// import com.zsh.petsystem.config.AlipayConfig;
// import com.zsh.petsystem.dto.AliPayReq;
// import com.zsh.petsystem.service.AlipayService;
// import jakarta.annotation.Resource;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import lombok.extern.slf4j.Slf4j;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.stereotype.Controller;
// import org.springframework.web.bind.annotation.*;
// import io.swagger.v3.oas.annotations.Operation;
// import org.springframework.util.StringUtils;

// import java.util.HashMap;
// import java.util.Map;

// /**
//  * 支付宝支付控制器
//  *
//  * @author javgo.cn
//  * @date 2024/1/13
//  */
// // @RestController
// // @RequestMapping("/alipay")
// // public class AlipayController {

// // @Resource
// // private AlipayConfig alipayConfig;

// // @Resource
// // private AlipayService alipayService;

// // @Operation(summary = "支付宝电脑网站支付接口")
// // @GetMapping("/pcPayment")
// // public void pcPayment(AliPayReq aliPayReq, HttpServletResponse response)
// // throws IOException {
// // response.setContentType("text/html;charset=" + alipayConfig.getCharset());
// // Result<?> result = alipayService.initiatePcPayment(aliPayReq);
// // response.getWriter().write(result.getData().toString());
// // response.getWriter().flush();
// // response.getWriter().close();
// // }

// // @Operation(summary = "支付宝支付通知")
// // @PostMapping("/notify")
// // @ResponseBody
// // public Result<?> processPaymentNotification(HttpServletRequest request) {
// // Map<String, String> params = new HashMap<>();
// // Map<String, String[]> requestParams = request.getParameterMap();
// // requestParams.keySet().forEach(r -> params.put(r, request.getParameter(r)));
// // return alipayService.processPaymentNotification(params);
// // }

// // @Operation(summary = "查询支付状态")
// // @GetMapping("/queryPaymentStatus")
// // @ResponseBody
// // public Result<?> queryPaymentStatus(String outTradeNo, String tradeNo) {
// // return alipayService.queryPaymentStatus(outTradeNo, tradeNo);
// // }
// // }

// @RestController
// @RequestMapping("/alipay")
// @Slf4j
// public class AlipayController {

//     @Resource
//     private AlipayConfig alipayConfig; // 需要注入 AlipayConfig 来获取字符集

//     @Resource
//     private AlipayService alipayService; // 注入的是 Service

//     // @Operation(summary = "支付宝电脑网站支付接口")
//     // 注意：这个方法修改为返回 ResponseEntity<Result<?>> 更符合你的项目风格
//     // 并且应该使用 POST 请求，因为它是发起一个操作，并且带有请求体(虽然这里用查询参数模拟)
//     @PostMapping("/pc") // **建议改为 POST**
//     @ResponseBody // 如果类注解是 @RestController，这个可以省略
//     public ResponseEntity<?> initiatePcPayment(@RequestBody AliPayReq aliPayReq) {
//         // 直接调用 Service 层的方法，Service 层负责处理 Reservation 的逻辑
//         Result<?> result = alipayService.initiatePcPayment(aliPayReq);

//         // 根据 Service 返回结果构建 ResponseEntity
//         if (result.getCode() == 200 && result.getData() != null) {
//             // 如果成功，并且返回的是 HTML 表单
//             // 为了安全和标准，通常不直接在 API 响应体中返回 HTML
//             // 更好的做法是前端接收到一个包含支付信息的对象，然后在前端跳转或渲染
//             // 但如果必须返回 HTML，需要设置 Content-Type
//             // 这里我们还是按之前的逻辑，假设 Service 返回了需要前端处理的数据或 URL
//             return ResponseEntity.ok(result);

//             /*
//              * 或者，如果坚持要服务器端直接输出 HTML (不推荐用于 API)
//              * HttpHeaders headers = new HttpHeaders();
//              * headers.setContentType(MediaType.TEXT_HTML);
//              * // 确保 result.getData() 是 String 类型的 HTML
//              * return new ResponseEntity<>(result.getData().toString(), headers,
//              * HttpStatus.OK);
//              */
//         } else {
//             // 如果失败，返回包含错误信息的 Result 对象
//             // 可以根据 result.getCode() 或具体错误信息设置不同的 HTTP 状态码
//             return ResponseEntity.badRequest().body(result); // 或者 InternalServerError 等
//         }
//     }

//     @Operation(summary = "支付宝支付通知")
//     @PostMapping("/notify") // 支付宝异步通知通常是 POST
//     @ResponseBody
//     public String processPaymentNotification(HttpServletRequest request) { // 支付宝要求直接返回 "success" 或 "failure" 字符串
//         Map<String, String> params = new HashMap<>();
//         Map<String, String[]> requestParams = request.getParameterMap();
//         // 支付宝文档建议这样转换参数
//         for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
//             String name = entry.getKey();
//             String[] values = entry.getValue();
//             String valueStr = "";
//             for (int i = 0; i < values.length; i++) {
//                 valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
//             }
//             params.put(name, valueStr);
//         }
//         log.info("Received Alipay notification params: {}", params); // 打印接收到的参数

//         Result<?> result = alipayService.processPaymentNotification(params);

//         // 根据支付宝要求，直接返回 "success" 或 "failure"
//         if (result.getCode() == 200) {
//             log.info("Alipay notification processed successfully. Returning 'success'.");
//             return "success";
//         } else {
//             log.warn("Alipay notification processing failed. Message: {}. Returning 'failure'.", result.getMessage());
//             return "failure";
//         }
//     }

//     @Operation(summary = "查询支付状态")
//     @GetMapping("/queryPaymentStatus")
//     @ResponseBody

//     public ResponseEntity<?> queryPaymentStatus(@RequestParam(required = false) String outTradeNo,
//             @RequestParam(required = false) String tradeNo) {
//         if (StringUtils.isEmpty(outTradeNo) && StringUtils.isEmpty(tradeNo)) {
//             return ResponseEntity.badRequest().body(Result.failed("订单号 (outTradeNo) 或支付宝交易号 (tradeNo) 至少需要一个"));
//         }
//         Result<?> result = alipayService.queryPaymentStatus(outTradeNo, tradeNo);
//         // 可以根据 result 的内容判断是否成功，并返回合适的 HTTP 状态
//         if (result.getCode() == 200) {
//             return ResponseEntity.ok(result);
//         } else {
//             // 查询失败或状态不对，返回 4xx 或 5xx
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result); // 或者根据情况返回 BadRequest 等
//         }
//     }
// }

package com.zsh.petsystem.controller;

import com.zsh.petsystem.common.Result;
import com.zsh.petsystem.dto.AliPayReq; // 只包含 outTradeNo
import com.zsh.petsystem.service.AlipayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag; // 可选：为了 Swagger UI
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/alipay") // 基础路径
@Slf4j
// @Tag(name = "AlipayController", description = "支付宝支付相关接口") // 如果不用 Swagger
// 可以去掉
public class AlipayController {

    @Resource
    private AlipayService alipayService;

    @PostMapping("/pc")
    @Operation(summary = "发起支付宝PC网页支付")
    public Result<?> initiatePcPayment(@RequestBody AliPayReq aliPayReq) {
        log.info("收到支付请求，订单号: {}", aliPayReq.getOutTradeNo());
        return alipayService.initiatePcPayment(aliPayReq);
    }

    /**
     * 接收支付宝异步通知的接口 (POST)
     * * @param request 包含支付宝发送的参数
     * 
     * @return 字符串 "success" 或 "failure" 给支付宝服务器
     */
    @PostMapping("/notify")
    @Operation(summary = "支付宝支付异步通知处理")
    @ResponseBody // 确保返回纯文本
    public String processPaymentNotification(HttpServletRequest request) {
        log.info("!!!!!!!!!!!!!!!!!!!! ASYNC NOTIFY RECEIVED AT CONTROLLER - STARTING !!!!!!!!!!!!!!!!!!!!");
        System.out.println(
                "!!!!!!!!!!!!!!!!!!!! SYSOUT: ASYNC NOTIFY RECEIVED AT CONTROLLER - STARTING !!!!!!!!!!!!!!!!!!!!");

        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        log.info("Raw Request Parameters from Alipay Notification:");
        requestParams.forEach((key, values) -> {
            String valueStr = String.join(",", values);
            params.put(key, valueStr);
            log.info("Param: {} = {}", key, valueStr);
        });

        log.info("Parameters map constructed: {}", params);
        log.info("Calling alipayService.processPaymentNotification...");

        Result<?> result = null; // 初始化 result
        try {
            result = alipayService.processPaymentNotification(params);
            log.info("alipayService.processPaymentNotification call completed. Result code: {}, Result data: {}",
                    result != null ? result.getCode() : "NULL_RESULT",
                    result != null ? result.getData() : "NULL_RESULT");
        } catch (Exception e) {
            log.error(
                    "!!!!!!!!!!!!!!!!!!!! EXCEPTION during alipayService.processPaymentNotification call !!!!!!!!!!!!!!!!!!!!",
                    e);
            // 即使Service层抛出异常，这里也应该返回 "failure" 给支付宝，而不是让Spring Boot返回500错误页面
            return "failure";
        }

        if (result != null && result.getCode() == 200 && "success".equals(result.getData())) {
            log.info("Alipay notification processed successfully by service. Returning 'success' to Alipay.");
            return "success";
        } else {
            log.warn(
                    "Alipay notification processing by service failed or status not allowed. Result: {}. Returning 'failure' to Alipay.",
                    result);
            return "failure";
        }
    }

    @GetMapping("/queryPaymentStatus")
    @Operation(summary = "查询支付宝支付状态")
    public Result<?> queryPaymentStatus(
            @RequestParam(required = false) String outTradeNo,
            @RequestParam(required = false) String tradeNo) {
        log.info("查询支付状态请求，outTradeNo: {}, tradeNo: {}", outTradeNo, tradeNo);
        return alipayService.queryPaymentStatus(outTradeNo, tradeNo);
    }
}