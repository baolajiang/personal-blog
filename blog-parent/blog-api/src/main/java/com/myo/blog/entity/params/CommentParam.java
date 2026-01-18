package com.myo.blog.entity.params;

import lombok.Data;

@Data
public class CommentParam {

    private String articleId;

    private String content;

    private String parent;

    private String toUserId;
}
