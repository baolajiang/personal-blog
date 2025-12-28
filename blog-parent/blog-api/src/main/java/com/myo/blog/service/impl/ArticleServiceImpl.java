package com.myo.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myo.blog.controller.UploadController;
import com.myo.blog.dao.dos.Archives;
import com.myo.blog.dao.mapper.ArticleBodyMapper;
import com.myo.blog.dao.mapper.ArticleMapper;
import com.myo.blog.dao.mapper.ArticleTagMapper;
import com.myo.blog.dao.pojo.Article;
import com.myo.blog.dao.pojo.ArticleBody;
import com.myo.blog.dao.pojo.ArticleTag;
import com.myo.blog.dao.pojo.SysUser;
import com.myo.blog.service.*;

import com.myo.blog.entity.ArticleBodyVo;
import com.myo.blog.entity.ArticleVo;
import com.myo.blog.entity.Result;
import com.myo.blog.entity.TagVo;
import com.myo.blog.entity.params.ArticleParam;
import com.myo.blog.entity.params.PageParams;
import com.myo.blog.utils.*;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private TagService tagService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private ArticleTagMapper articleTagMapper;
    @Autowired
    private QiniuUtils qiniuUtils;
    //查询文章列表
    @Override
    public Result listArticle(PageParams pageParams,String token) {
        Page<Article> page = new Page<>(pageParams.getPage(),pageParams.getPageSize());

        boolean isToken=!"undefined".equals(token);
        IPage<Article> articleIPage = articleMapper.listArticle(
                page,
                pageParams.getCategoryId(),
                pageParams.getTagId(),
                pageParams.getYear(),
                pageParams.getMonth(),
                isToken
        );

        List<Article> records = articleIPage.getRecords();
        // 将数据库实体转换为前端视图对象 (VO)
        List<ArticleVo> articleVoList = copyList(records, true, true, false, false, isToken);

        // MyBatis Plus 自动查询的总记录数
        long total = articleIPage.getTotal();

        //封装到 Map 中返回
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("articles", articleVoList); // 文章列表
        resultData.put("total", total);            // 总条数

        return Result.success(resultData);
    }
    //查询一个多少文章数量
    public Result listArticleCount(String token){
        boolean isToken=!"undefined".equals(token);
        Integer count=articleMapper.listArticleCount(isToken);
        return Result.success(count);
    };
    /*查询mac*/
    @Override
    public Result queryMAC() {
        //获取request 设置IP地址
        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
        String ip=IpUtils.getIpAddr(request);
        //String mac=LocalMACUtil.getLocalMac();//
        String addr=IpUtils.getCityInfo(ip);

        return Result.success(addr);
    }

//    @Override
//    public Result listArticle(PageParams pageParams) {
//        /**
//         * 1. 分页查询 article数据库表
//         */
//        Page<Article> page = new Page<>(pageParams.getPage(),pageParams.getPageSize());
//        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
//        if (pageParams.getCategoryId() != null){
//            // and category_id=#{categoryId}
//            queryWrapper.eq(Article::getCategoryId,pageParams.getCategoryId());
//        }
//        List<Long> articleIdList = new ArrayList<>();
//        if (pageParams.getTagId() != null){
//            //加入标签 条件查询
//            //article表中 并没有tag字段 一篇文章 有多个标签
//            //article_tag  article_id 1 : n tag_id
//            LambdaQueryWrapper<ArticleTag> articleTagLambdaQueryWrapper = new LambdaQueryWrapper<>();
//            articleTagLambdaQueryWrapper.eq(ArticleTag::getTagId,pageParams.getTagId());
//            List<ArticleTag> articleTags = articleTagMapper.selectList(articleTagLambdaQueryWrapper);
//            for (ArticleTag articleTag : articleTags) {
//                articleIdList.add(articleTag.getArticleId());
//            }
//            if (articleIdList.size() > 0){
//                // and id in(1,2,3)
//                queryWrapper.in(Article::getId,articleIdList);
//            }
//        }
//        //是否置顶进行排序
//        //order by create_date desc
//        queryWrapper.orderByDesc(Article::getWeight,Article::getCreateDate);
//        Page<Article> articlePage = articleMapper.selectPage(page, queryWrapper);
//        List<Article> records = articlePage.getRecords();
//        //能直接返回吗？ 很明显不能
//        List<ArticleVo> articleVoList = copyList(records,true,true);
//        return Result.success(articleVoList);
//    }

    @Override
    public Result hotArticle(int limit) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Article::getViewCounts);
        queryWrapper.select(Article::getId,Article::getTitle);
        queryWrapper.last("limit "+limit);
        //select id,title from article order by view_counts desc limit 5
        List<Article> articles = articleMapper.selectList(queryWrapper);

        return Result.success(copyList(articles,false,false));
    }

    @Override
    public Result newArticles(int limit) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Article::getCreateDate);
        queryWrapper.select(Article::getId,Article::getTitle);
        queryWrapper.last("limit "+limit);
        //select id,title from article order by create_date desc desc limit 5
        List<Article> articles = articleMapper.selectList(queryWrapper);

        return Result.success(copyList(articles,false,false));
    }

    @Override
    public Result listArchives() {
        List<Archives> archivesList = articleMapper.listArchives();
        return Result.success(archivesList);
    }

    @Autowired
    private ThreadService threadService;


    @Override
    public Result findArticleById(Long articleId, String token) {
        Article article = this.articleMapper.selectById(articleId);
        if (article == null) {
            return Result.fail(404, "文章不存在");
        }

        boolean isToken = !"undefined".equals(token);

        // 1. 隐藏文章
        if (!isToken && article.getViewKeys() == 2) {
            return Result.fail(403, "该文章不存在或您无权访问");
        }

        // 2. 转换 VO
        ArticleVo articleVo = copy(article, true, true, true, true);

        // 3. 加密脱敏
        if (!isToken && article.getViewKeys() == 1) {

            // 标题 & 简介
            int titleLen = articleVo.getTitle() != null ? articleVo.getTitle().length() : 5;
            articleVo.setTitle(ArticleUtils.keys(titleLen));

            int summaryLen = articleVo.getSummary() != null ? articleVo.getSummary().length() : 20;
            articleVo.setSummary(ArticleUtils.keys(summaryLen));

            // 正文
            if (articleVo.getBody() != null && articleVo.getBody().getContent() != null) {
                int bodyLen = articleVo.getBody().getContent().length();
                int maskLen = Math.min(bodyLen, 800);
                articleVo.getBody().setContent(ArticleUtils.keys(maskLen));
            }

            // --- 【修改点】标签统一为一个 "******" ---
            List<TagVo> maskTagList = new ArrayList<>();
            TagVo maskTag = new TagVo();
            maskTag.setTagName("******");
            maskTagList.add(maskTag);

            articleVo.setTags(maskTagList);

            // --- 【新增】分类加密 ---
            if (articleVo.getCategory() != null) {
                articleVo.getCategory().setCategoryName("******");
            }
        }

        threadService.updateArticleViewCount(articleMapper, article);
        return Result.success(articleVo);
    }

    @Override
    @Transactional
    public Result publish(ArticleParam articleParam) {
        //此接口 要加入到登录拦截当中
        SysUser sysUser = UserThreadLocal.get();
        /**
         * 1. 发布文章 目的 构建Article对象
         * 2. 作者id  当前的登录用户
         * 3. 标签  要将标签加入到 关联列表当中
         * 4. body 内容存储 article bodyId
         */
        Article article = new Article();
        article.setAuthorId(sysUser.getId());
        article.setWeight(Article.Article_Common);
        article.setViewCounts(0);
        article.setTitle(articleParam.getTitle());
        article.setSummary(articleParam.getSummary());
        article.setCommentCounts(0);
        article.setCreateDate(System.currentTimeMillis());
        article.setCategoryId(Long.parseLong(articleParam.getCategory().getId()));


        //获取最新文章的id   articlesId
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(Article::getId);
        queryWrapper.last("limit 1");
        queryWrapper.orderByDesc(Article::getCreateDate);
        List<Article> articles = articleMapper.selectList(queryWrapper);
        int id=0;
        if(articles.size()!=0){
            id=Integer.parseInt(String.valueOf(articles.get(0).getId()));
        }

        //封面cover
        String cover=articleParam.getCover();


        article.setCover(cover);
        //获取最新文章的位置id然后加169

        int articlesId=id+1;
        //将文章id添加
        article.setId(Long.parseLong(String.valueOf(articlesId)));
        //没有article.setId的活会自动添加
        this.articleMapper.insert(article);

        // ==================【测试开始：插入这段代码】==================
/*        if (true) {
            throw new RuntimeException("===> 我是故意来捣乱的异常，测试回滚！ <===");
        }*/
        // ==================【测试结束】==================

        //tag
        List<TagVo> tags = articleParam.getTags();
        if (tags != null){
            for (TagVo tag : tags) {
                Long articleId = article.getId();
                ArticleTag articleTag = new ArticleTag();
                articleTag.setTagId(Long.parseLong(tag.getId()));
                articleTag.setArticleId(articleId);
                articleTagMapper.insert(articleTag);
            }
        }
        //body
        ArticleBody articleBody  = new ArticleBody();
        articleBody.setArticleId(article.getId());
        articleBody.setContent(articleParam.getBody().getContent());
        articleBody.setContentHtml(articleParam.getBody().getContentHtml());
        //

        //获取最新文章的body中id   articlesBodyId
        LambdaQueryWrapper<ArticleBody> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.select(ArticleBody::getId);
        queryWrapper2.last("limit 1");
        queryWrapper2.orderByDesc(ArticleBody::getId);
        List<ArticleBody> articleBodyList = articleBodyMapper.selectList(queryWrapper2);
        long id2=0;
        if(articleBodyList.size()!=0){
            id2=articleBodyList.get(0).getId();
        }
        int articlesBodyId=Integer.parseInt(String.valueOf(id2))+1;
        //将id添加到获取最新文章的body中id
        articleBody.setId( Long.parseLong(String.valueOf(articlesBodyId)));
        //没有articleBody.setId的活会自动添加
        articleBodyMapper.insert(articleBody);
        article.setBodyId(articleBody.getId());
        articleMapper.updateById(article);
        Map<String,String> map = new HashMap<>();
        map.put("id",article.getId().toString());
        return Result.success(map);
    }


    private List<ArticleVo> copyList(List<Article> records, boolean isTag, boolean isAuthor) {
        List<ArticleVo> articleVoList = new ArrayList<>();
        for (Article record : records) {
            articleVoList.add(copy(record,isTag,isAuthor,false,false));
        }
        return articleVoList;
    }
    private List<ArticleVo> copyList(List<Article> records, boolean isTag, boolean isAuthor, boolean isBody,boolean isCategory) {
        List<ArticleVo> articleVoList = new ArrayList<>();
        for (Article record : records) {
            articleVoList.add(copy(record,isTag,isAuthor,isBody,isCategory));
        }
        return articleVoList;
    }

    private List<ArticleVo> copyList(List<Article> records, boolean isTag, boolean isAuthor, boolean isBody, boolean isCategory, boolean isToken) {
        List<ArticleVo> articleVoList = new ArrayList<>();

        for (Article record : records) {
            // 1. 隐藏文章 (viewKeys=2) -> 未登录直接跳过
            if (!isToken && record.getViewKeys() == 2) {
                continue;
            }
            // 2. 转换数据
            ArticleVo articleVo = copy(record, isTag, isAuthor, isBody, isCategory);
            // 3. 加密文章 (viewKeys=1) -> 未登录进行脱敏
            if (!isToken && record.getViewKeys() == 1) {

                // --- 标题和简介乱码 (保持不变) ---
                int titleLen = articleVo.getTitle() != null ? articleVo.getTitle().length() : 5;
                articleVo.setTitle(ArticleUtils.keys(titleLen));

                int summaryLen = articleVo.getSummary() != null ? articleVo.getSummary().length() : 10;
                articleVo.setSummary(ArticleUtils.keys(summaryLen));

                // --- 【修改点】标签统一为一个 "******" ---
                // 不管原来有几个标签，这里创建一个新的列表，只放一个伪造的标签
                List<TagVo> maskTagList = new ArrayList<>();
                TagVo maskTag = new TagVo();
                maskTag.setTagName("******");
                // maskTag.setId("-1"); // 如果前端报错key重复，可以给个假ID
                maskTagList.add(maskTag);

                // 直接替换掉原来的标签列表
                articleVo.setTags(maskTagList);

                // 如果当前 ArticleVo 里有分类信息，就把名字改掉
                if (articleVo.getCategory() != null) {
                    articleVo.getCategory().setCategoryName("******");
                    // articleVo.getCategory().setId("-1"); // 可选：防止点击跳转
                }
            }

            articleVoList.add(articleVo);
        }
        return articleVoList;
    }

    @Autowired
    private CategoryService categoryService;

    private ArticleVo copy(Article article, boolean isTag, boolean isAuthor, boolean isBody,boolean isCategory){

        ArticleVo articleVo = new ArticleVo();
        articleVo.setId(String.valueOf(article.getId()));

        BeanUtils.copyProperties(article,articleVo);
        articleVo.setViewKeys(String.valueOf(article.getViewKeys()));
        articleVo.setCreateDate(new DateTime(article.getCreateDate()).toString("yyyy-MM-dd HH:mm"));
        //并不是所有的接口 都需要标签 ，作者信息
        if (isTag){
            Long articleId = article.getId();
            articleVo.setTags(tagService.findTagsByArticleId(articleId));
        }
        if (isAuthor){
            Long authorId = article.getAuthorId();
            articleVo.setAuthor(sysUserService.findUserById(authorId).getNickname());
        }
        if (isBody){
            Long bodyId = article.getBodyId();
            articleVo.setBody(findArticleBodyById(bodyId));
        }
        if (isCategory){
            Long categoryId = article.getCategoryId();
            articleVo.setCategory(categoryService.findCategoryById(categoryId));
        }


        return articleVo;
    }

    @Autowired
    private ArticleBodyMapper articleBodyMapper;

    private ArticleBodyVo findArticleBodyById(Long bodyId) {
        ArticleBody articleBody = articleBodyMapper.selectById(bodyId);
        ArticleBodyVo articleBodyVo = new ArticleBodyVo();
        articleBodyVo.setContent(articleBody.getContent());
        return articleBodyVo;
    }

}
