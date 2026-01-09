package com.myo.blog.controller;

import com.myo.blog.common.aop.RateLimit;
import com.myo.blog.entity.CommentVo;
import com.myo.blog.service.CommentsService;
import com.myo.blog.entity.Result;
import com.myo.blog.entity.params.CommentParam;
import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.UserAgent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@CrossOrigin
@RestController
@RequestMapping("comments")
public class CommentsController {
    @Autowired
    private CommentsService commentsService;

    ///comments/article/{id}

    @GetMapping("article/{id}")
    public Result comments(@PathVariable("id") Long id){
        return commentsService.commentsByArticleId(id);
    }

    // 防止刷评论，60秒内只能发1条
    @RateLimit(time = 60, count = 1, msg = "评论发布太快了，请休息一下")
    @PostMapping("create/change")
    public Result comment(@RequestBody CommentParam commentParam){
        return commentsService.comment(commentParam);
    }

    @GetMapping("queryCommentCount/{id}")
    public Result show(@PathVariable("id") Long i){

        return commentsService.commentsByArticleId(i);

    }

    @GetMapping("test22")
    public static String browserName(HttpServletRequest request){
        String userAgent = request.getHeader("User-Agent");
        UserAgent ua = UserAgent.parseUserAgentString(userAgent);
        Browser browser = ua.getBrowser();
        return browser.getName() + "-" + browser.getVersion(userAgent);
    }


}
