package com.myo.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myo.blog.dao.dos.articles;
import com.myo.blog.dao.mapper.*;
import com.myo.blog.dao.pojo.*;
import com.myo.blog.service.*;
import com.myo.blog.entity.*; // 包含 CategoryVo, ArticleVo 等
import com.myo.blog.entity.params.ArticleParam;
import com.myo.blog.entity.params.PageParams;
import com.myo.blog.utils.*;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 文章服務實作類 (ArticleService Implementation)
 * 負責處理文章相關的所有業務邏輯，包括列表查詢、詳情查看、發布文章等。
 */
@Service
public class ArticleServiceImpl implements ArticleService {

    // 注入 MyBatis Plus 的 Mapper 介面，用於操作資料庫
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private ArticleBodyMapper articleBodyMapper;
    @Autowired
    private ArticleTagMapper articleTagMapper;

    // 注入其他服務 Service，用於處理關聯業務
    @Autowired
    private TagService tagService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ThreadService threadService;



    // 【新增注入】直接注入 Mapper 以便進行批量查詢 (Batch Query)
    // 這是為了在 copyList 方法中解決 N+1 問題，直接用 ID 列表查出資料
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private CategoryMapper categoryMapper;



    /**
     * 查詢文章列表 (分頁)
     * 1. 分頁查詢資料庫表
     * 2. 轉換成 VO 對象 (同時進行效能優化)
     *
     * @param pageParams 分頁參數
     * @param token 用戶 Token (用於判斷權限)
     * @return Result
     */
    @Override
    public Result listArticle(PageParams pageParams, String token) {
        // 1. 建立分頁物件
        Page<Article> page = new Page<>(pageParams.getPage(), pageParams.getPageSize());

        // 2. 判斷是否有 Token (是否登入)
        boolean isToken = false;
        if (StringUtils.isNotBlank(token) && !"undefined".equals(token)) {
            // 务必验证 Token 是否合法
            isToken = JWTUtils.checkToken(token) != null;
        }

        // 3. 呼叫 Mapper 進行自定義的分頁查詢 (支持歸檔、標籤、分類篩選)
        IPage<Article> articleIPage = articleMapper.listArticle(
                page,
                pageParams.getCategoryId(),
                pageParams.getTagId(),
                pageParams.getYear(),
                pageParams.getMonth(),
                isToken
        );

        List<Article> records = articleIPage.getRecords();

        // 4. 【核心優化點】 將資料庫實體轉換為前端視圖對象 (VO)
        // 這裡調用我們優化後的 copyList 方法，減少資料庫查詢次數
        List<ArticleVo> articleVoList = copyList(records, true, true, false, false, isToken);

        // 5. 獲取總記錄數
        long total = articleIPage.getTotal();

        // 6. 封裝結果返回
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("articles", articleVoList);
        resultData.put("total", total);

        return Result.success(resultData);
    }

    /**
     * 查詢文章總數
     */
    @Override
    public Result listArticleCount(String token) {

        boolean isToken = false;
        if (StringUtils.isNotBlank(token) && !"undefined".equals(token)) {
            // 务必验证 Token 是否合法
            isToken = JWTUtils.checkToken(token) != null;
        }
        Integer count = articleMapper.listArticleCount(isToken);
        return Result.success(count);
    }

    /**
     * 查詢訪客的 IP 和城市信息
     */
    @Override
    public Result queryMAC() {
        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
        String ip = IpUtils.getIpAddr(request);
        String addr = IpUtils.getCityInfo(ip);
        return Result.success(addr);
    }

    /**
     * 最熱文章 (依照瀏覽量排序)
     */
    @Override
    public Result hotArticle(int limit) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Article::getViewCounts);
        queryWrapper.select(Article::getId, Article::getTitle);
        queryWrapper.last("limit " + limit);
        List<Article> articles = articleMapper.selectList(queryWrapper);

        // 熱門文章列表不需要 Body 和 Category 詳情，傳 false 以提升效能
        return Result.success(copyList(articles, false, false));
    }

    /**
     * 最新文章 (依照創建時間排序)
     */
    @Override
    public Result newArticles(int limit) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Article::getCreateDate);
        queryWrapper.select(Article::getId, Article::getTitle);
        queryWrapper.last("limit " + limit);
        List<Article> articles = articleMapper.selectList(queryWrapper);

        return Result.success(copyList(articles, false, false));
    }

    /**
     * 文章歸檔列表 (按年月分組)
     */
    @Override
    public Result listarticles() {
        List<articles> articlesList = articleMapper.listarticles();
        return Result.success(articlesList);
    }

    /**
     * 查詢單篇文章詳情
     * @param articleId 文章 ID
     * @param token 用戶 Token
     */
    @Override
    public Result findArticleById(Long articleId, String token) {
        Article article = this.articleMapper.selectById(articleId);
        if (article == null) {
            return Result.fail(404, "文章不存在");
        }


        boolean isToken = false;
        if (StringUtils.isNotBlank(token) && !"undefined".equals(token)) {
            // 务必验证 Token 是否合法
            isToken = JWTUtils.checkToken(token) != null;
        }

        // 1. 隱藏文章判斷 (viewKeys=2 為隱藏)
        // 【修復 NPE】 增加 null 判斷，避免 ViewKeys 為 null 時拆箱報錯
        if (!isToken && article.getViewKeys() != null && article.getViewKeys() == 2) {
            return Result.fail(403, "該文章不存在或您無權訪問");
        }

        // 2. 轉換 VO (單篇文章不需要批量優化，直接調用 copy 即可)
        // 這裡需要 Body (正文) 和 Category (分類)
        ArticleVo articleVo = copy(article, true, true, true, true);

        // 3. 加密脫敏 (viewKeys=1 為加密)
        // 如果未登入，將標題、簡介、內容打碼
        // 【修復 NPE】 增加 null 判斷
        if (!isToken && article.getViewKeys() != null && article.getViewKeys() == 1) {
            int titleLen = articleVo.getTitle() != null ? articleVo.getTitle().length() : 5;
            articleVo.setTitle(ArticleUtils.keys(titleLen));

            int summaryLen = articleVo.getSummary() != null ? articleVo.getSummary().length() : 20;
            articleVo.setSummary(ArticleUtils.keys(summaryLen));

            // 正文打碼
            if (articleVo.getBody() != null && articleVo.getBody().getContent() != null) {
                int bodyLen = articleVo.getBody().getContent().length();
                int maskLen = Math.min(bodyLen, 800);
                articleVo.getBody().setContent(ArticleUtils.keys(maskLen));
            }

            // 標籤打碼 (統一顯示為 ******)
            List<TagVo> maskTagList = new ArrayList<>();
            TagVo maskTag = new TagVo();
            maskTag.setTagName("******");
            maskTagList.add(maskTag);
            articleVo.setTags(maskTagList);

            // 分類打碼
            if (articleVo.getCategory() != null) {
                articleVo.getCategory().setCategoryName("******");
            }
        }

        // 記錄閱讀數 (使用線程池異步處理，不阻塞主線程)
        threadService.updateArticleViewCount(articleMapper, article);
        return Result.success(articleVo);
    }

    /**
     * 發布文章
     */
    @Override
    @Transactional
    public Result publish(ArticleParam articleParam) {
        // 1. 处理文章基本信息
        Article article = new Article();
        BeanUtils.copyProperties(articleParam, article);

        // 删除危险的ID生成逻辑
        // LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        // queryWrapper.select(Article::getId);
        // queryWrapper.last("limit 1");
        // queryWrapper.orderByDesc(Article::getCreateDate);
        // List<Article> articles = articleMapper.selectList(queryWrapper);
        // long id = 0;
        // if (articles.size() != 0) {
        //     id = articles.get(0).getId();
        // }
        // article.setId(id + 1); // 删除这行

        // 设置其他属性
        article.setCommentCounts(0);
        article.setViewCounts(0);
        article.setWeight(Article.Article_Common);
        article.setCreateDate(System.currentTimeMillis());
        article.setCategoryId(Long.parseLong(articleParam.getCategory().getId()));
        article.setCover(articleParam.getCover());




        // 直接插入，MyBatis Plus会自动使用雪花算法生成ID
        this.articleMapper.insert(article);

        // 2. 处理标签关联
        List<TagVo> tags = articleParam.getTags();
        if (tags != null) {
            for (TagVo tag : tags) {
                ArticleTag articleTag = new ArticleTag();
                articleTag.setTagId(Long.parseLong(tag.getId()));
                articleTag.setArticleId(article.getId()); // 使用数据库生成的ID
                articleTagMapper.insert(articleTag);
            }
        }

        // 3. 处理文章内容
        ArticleBody articleBody = new ArticleBody();
        articleBody.setArticleId(article.getId());
        articleBody.setContent(articleParam.getBody().getContent());
        articleBody.setContentHtml(articleParam.getBody().getContentHtml());

        // 文章内容也会自动使用雪花算法生成ID
        articleBodyMapper.insert(articleBody);

        // 4. 回填BodyID到Article表
        article.setBodyId(articleBody.getId());
        articleMapper.updateById(article);

        Map<String, String> map = new HashMap<>();
        map.put("id", article.getId().toString());

        return Result.success(map);
    }


    /**
     * 重載方法：簡化版 copyList (兼容舊代碼調用，不需要 token 的場景)
     */
    private List<ArticleVo> copyList(List<Article> records, boolean isTag, boolean isAuthor) {
        return copyList(records, isTag, isAuthor, false, false, false);
    }

    /**
     * 核心優化方法：批量將 Article 轉換為 ArticleVo
     * 解決了 N+1 查詢問題：避免在迴圈中多次查詢資料庫
     *
     * @param records 資料庫查出的文章列表
     * @param isTag 是否查詢標籤
     * @param isAuthor 是否查詢作者
     * @param isBody 是否查詢內容
     * @param isCategory 是否查詢分類
     * @param isToken 是否有登入 Token (用於判斷隱藏文章)
     * @return List<ArticleVo>
     */
    private List<ArticleVo> copyList(List<Article> records, boolean isTag, boolean isAuthor, boolean isBody, boolean isCategory, boolean isToken) {
        List<ArticleVo> articleVoList = new ArrayList<>();

        // === 1. 收集 ID (準備階段) ===
        // 遍歷所有文章，把需要的 AuthorId 和 CategoryId 收集起來，準備一次查完
        List<Long> authorIds = new ArrayList<>();
        List<Long> categoryIds = new ArrayList<>();

        for (Article record : records) {
            // 【修復 NPE】 如果是隱藏文章且未登入，跳過，不需要收集它的 ID
            if (!isToken && record.getViewKeys() != null && record.getViewKeys() == 2) {
                continue;
            }
            if (isAuthor) {
                authorIds.add(record.getAuthorId());
            }
            if (isCategory) {
                categoryIds.add(record.getCategoryId());
            }
        }

        // === 2. 批量查詢 (資料庫交互階段) ===

        // A. 查詢作者信息，並轉為 Map<Long, SysUser> 方便查找
        Map<Long, SysUser> authorMap = new HashMap<>();
        if (isAuthor && !authorIds.isEmpty()) {
            // 使用 MyBatis Plus 的 selectBatchIds (SELECT * FROM sys_user WHERE id IN (...))
            List<SysUser> users = sysUserMapper.selectBatchIds(authorIds);
            for (SysUser u : users) {
                authorMap.put(u.getId(), u);
            }
        }

        // B. 查詢分類信息，並轉為 Map<Long, CategoryVo> 方便查找
        // 注意：Map 的 Value 是 CategoryVo，因為 ArticleVo 需要的是這個類型
        Map<Long, CategoryVo> categoryMap = new HashMap<>();
        if (isCategory && !categoryIds.isEmpty()) {
            List<Category> categories = categoryMapper.selectBatchIds(categoryIds);
            for (Category c : categories) {
                // 將 Category 實體轉換為 VO 對象
                CategoryVo vo = new CategoryVo();
                BeanUtils.copyProperties(c, vo);
                vo.setId(String.valueOf(c.getId())); // ID 類型轉換 (Long -> String)
                categoryMap.put(c.getId(), vo);
            }
        }

        // === 3. 組裝數據 (記憶體處理階段) ===
        for (Article record : records) {
            // 【修復 NPE】 再次過濾隱藏文章
            if (!isToken && record.getViewKeys() != null && record.getViewKeys() == 2) {
                continue;
            }

            // 【重點】調用 copy 時，isAuthor 和 isCategory 傳入 false
            // 這是為了防止 copy 方法內部再去查詢資料庫，我們稍後用 Map 手動賦值
            ArticleVo articleVo = copy(record, isTag, false, isBody, false);

            // 手動填入作者信息 (從 Map 獲取，不查庫)
            if (isAuthor && authorMap.containsKey(record.getAuthorId())) {
                articleVo.setAuthor(authorMap.get(record.getAuthorId()).getNickname());
            }

            // 手動填入分類信息 (從 Map 獲取，不查庫)
            if (isCategory && categoryMap.containsKey(record.getCategoryId())) {
                articleVo.setCategory(categoryMap.get(record.getCategoryId()));
            }

            // 加密/脫敏處理 (viewKeys=1)
            // 【修復 NPE】 增加 null 判斷
            if (!isToken && record.getViewKeys() != null && record.getViewKeys() == 1) {
                int titleLen = articleVo.getTitle() != null ? articleVo.getTitle().length() : 5;
                articleVo.setTitle(ArticleUtils.keys(titleLen));

                int summaryLen = articleVo.getSummary() != null ? articleVo.getSummary().length() : 10;
                articleVo.setSummary(ArticleUtils.keys(summaryLen));

                List<TagVo> maskTagList = new ArrayList<>();
                TagVo maskTag = new TagVo();
                maskTag.setTagName("******");
                maskTagList.add(maskTag);
                articleVo.setTags(maskTagList);

                if (articleVo.getCategory() != null) {
                    articleVo.getCategory().setCategoryName("******");
                }
            }

            articleVoList.add(articleVo);
        }
        return articleVoList;
    }

    /**
     * 單篇文章轉換方法
     * 負責將 Article 實體屬性複製到 ArticleVo
     */
    private ArticleVo copy(Article article, boolean isTag, boolean isAuthor, boolean isBody, boolean isCategory) {
        ArticleVo articleVo = new ArticleVo();
        // ID 轉換
        articleVo.setId(String.valueOf(article.getId()));
        // 屬性複製 (使用 BeanUtils)
        BeanUtils.copyProperties(article, articleVo);

        // 【修復 NPE】 避免 viewKeys 為 null 時轉成 "null" 字串，改為預設 "0"
        if (article.getViewKeys() != null) {
            articleVo.setViewKeys(String.valueOf(article.getViewKeys()));
        } else {
            articleVo.setViewKeys("0"); // 默認為 0 (公開)
        }

        // 時間格式化
        articleVo.setCreateDate(new DateTime(article.getCreateDate()).toString("yyyy-MM-dd HH:mm"));

        // 標籤查詢 (暫時維持單次查詢，若有性能問題可後續優化)
        if (isTag) {
            Long articleId = article.getId();
            articleVo.setTags(tagService.findTagsByArticleId(articleId));
        }

        // 作者查詢 (若被 copyList 調用，此處應為 false)
        if (isAuthor) {
            Long authorId = article.getAuthorId();
            articleVo.setAuthor(sysUserService.findUserById(authorId).getNickname());
        }

        // 內容查詢
        if (isBody) {
            Long bodyId = article.getBodyId();
            articleVo.setBody(findArticleBodyById(bodyId));
        }

        // 分類查詢 (若被 copyList 調用，此處應為 false)
        if (isCategory) {
            Long categoryId = article.getCategoryId();
            articleVo.setCategory(categoryService.findCategoryById(categoryId));
        }

        return articleVo;
    }

    /**
     * 查詢文章內容
     */
    private ArticleBodyVo findArticleBodyById(Long bodyId) {
        ArticleBody articleBody = articleBodyMapper.selectById(bodyId);
        ArticleBodyVo articleBodyVo = new ArticleBodyVo();
        articleBodyVo.setContent(articleBody.getContent());
        return articleBodyVo;
    }


}