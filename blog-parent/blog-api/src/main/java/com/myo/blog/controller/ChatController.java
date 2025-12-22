package com.myo.blog.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Value("${chat.openai.api-key}")
    private String apiKey;

    @Value("${chat.openai.base-url}")
    private String baseUrl;

    @Value("${chat.openai.model}")
    private String model;

    // 🔴 修改点 1：返回类型改成 Map (为了生成 JSON)
    @PostMapping("/send")
    public Map<String, Object> chat(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        String fullUrl = baseUrl.endsWith("/") ? baseUrl + "chat/completions" : baseUrl + "/chat/completions";

        JSONObject body = new JSONObject();
        body.set("model", this.model);
        JSONObject systemMsg = new JSONObject().set("role", "system").set("content", "你是一个乐于助人的技术博客助手。");
        JSONObject userMsg = new JSONObject().set("role", "user").set("content", userMessage);
        body.set("messages", new JSONArray().put(systemMsg).put(userMsg));
        body.set("max_tokens", 512);

        // 准备返回给前端的 Map
        Map<String, Object> result = new HashMap<>();

        try {
            String response = HttpRequest.post(fullUrl)
                    .header("Authorization", "Bearer " + this.apiKey)
                    .header("Content-Type", "application/json")
                    .body(body.toString())
                    .timeout(20000)
                    .execute()
                    .body();

            JSONObject jsonResponse = JSONUtil.parseObj(response);

            // 错误处理
            if(jsonResponse.containsKey("error")) {
                result.put("code", 500);
                result.put("msg", jsonResponse.getJSONObject("error").getStr("message"));
                return result;
            }

            String aiReply = jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getStr("content");

            // 🔴 修改点 2：封装成标准 JSON 格式
            // 这样前端看到 code=200 才会放行
            result.put("code", 200);
            result.put("data", aiReply);
            result.put("msg", "success");

        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "后端报错: " + e.getMessage());
        }

        return result;
    }
}