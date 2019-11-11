package com.kunfei.bookshelf.model.content;

import android.text.TextUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.BaseModelImpl;

import com.kunfei.bookshelf.bean.BaseChapterBean;
import com.kunfei.bookshelf.bean.BookContentBean;
import com.kunfei.bookshelf.bean.BookInfoBean;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.BookChapterBean;
import com.kunfei.bookshelf.bean.SearchBookBean;


import com.kunfei.bookshelf.model.BookSourceManager;

import com.kunfei.bookshelf.model.analyzeRule.AnalyzeHeaders;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeUrl;

import com.kunfei.bookshelf.model.impl.IHttpGetApi;
import com.kunfei.bookshelf.model.impl.IShuqiApi;

import com.kunfei.bookshelf.model.impl.IStationBookModel;
import com.kunfei.bookshelf.utils.MD5Utils;
import com.kunfei.bookshelf.utils.NetworkUtils;
import com.kunfei.bookshelf.utils.StringUtils;


import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import retrofit2.Response;

public class DefaultShuqi extends BaseModelImpl implements IStationBookModel {
    private static final String TAG = DefaultShuqi.class.getSimpleName();
    public String tag;
    private String name;
    private BookSourceBean bookSourceBean;
    private Map<String, String> headerMap;

    private DefaultShuqi(String tag){
        this.tag = tag;
        try {
            URL url = new URL(tag);
            name = url.getHost();
        } catch (MalformedURLException e) {
            name = tag;
        }
        bookSourceBean = BookSourceManager.getBookSourceByUrl(tag);
        if (bookSourceBean != null) {
            name = bookSourceBean.getBookSourceName();
            headerMap = AnalyzeHeaders.getMap(bookSourceBean);
        }

    }

    public static DefaultShuqi getInstance(String tag) {
        return new DefaultShuqi(tag);
    }

    /**
     * 发现书籍
     */

    public Observable<List<SearchBookBean>> findBook(String url, int page) {
        try {
            final AnalyzeUrl analyzeUrl = new AnalyzeUrl(url, null, page, headerMap, tag);

            return getResponseO(analyzeUrl)
                    .flatMap(response -> fanalyzeSearchBook(response.body()));
        } catch (Exception e) {
            return Observable.error(new Throwable(String.format("%s错误:%s", url, e.getLocalizedMessage())));
        }
    }
    private Observable<List<SearchBookBean>> fanalyzeSearchBook(final String response) {
        return Observable.create(e -> {
            List<SearchBookBean> searchBooks = new ArrayList<>();
            SearchBookBean item;
            String zhuangtai;
            JsonObject root = new JsonParser().parse(response).getAsJsonObject();

                JsonArray booksArray = root.getAsJsonArray("data");
                for (JsonElement element : booksArray) {
                    JsonObject book = element.getAsJsonObject();
                    String bookId = book.get("bid").getAsString();
                    if(book.get("status").getAsInt() == 1)
                    {
                        zhuangtai = "完结";
                    }else{
                        zhuangtai = "连载";
                    }
                    item = new SearchBookBean();
                    item.setTag(tag);
                    item.setOrigin(name);
                    item.setSearchTime(Integer.MAX_VALUE);
                    item.setAuthor(book.get("author").getAsString());
                    item.setKind(book.get("category").getAsString()+","+zhuangtai+","+book.get("words").getAsInt());
                    item.setLastChapter(book.get("last_chapter_name").getAsString());
                    item.setName(book.get("title").getAsString());
                    item.setNoteUrl("http://c1.shuqireader.com/httpserver/filecache/get_book_content_" + bookId);
                    item.setCoverUrl(book.get("cover").getAsString().replace("\\/", "/"));
                    item.setIntroduce(book.get("desc").getAsString());
                    item.putVariable("bookId", bookId);
                    searchBooks.add(item);
                }

            e.onNext(searchBooks);
            e.onComplete();
        });
    }
    /**
     * 搜索书籍
     */

    public Observable<List<SearchBookBean>> searchBook(String content, int page) {
        return BaseModelImpl.getInstance().getRetrofitString("http://read.xiaoshuo1-sm.com")
                .create(IShuqiApi.class)
                .getSearchBook("is_serchpay", "3", content, page, "30")
                .flatMap(response -> analyzeSearchBook(response));
    }

    private Observable<List<SearchBookBean>> analyzeSearchBook(final Response<String> response) {
        return Observable.create(e -> {
            String baseUrl;
            baseUrl = NetworkUtils.getUrl(response);
            if (TextUtils.isEmpty(response.body())) {
                e.onError(new Throwable(MApplication.getInstance().getString(R.string.get_web_content_error, baseUrl)));
                return;
            } else {
                Debug.printLog(tag, "┌成功获取搜索结果");
                Debug.printLog(tag, "└" + baseUrl);
            }
            Debug.printLog(tag, "┌解析搜索列表");
            String body = response.body();
            List<SearchBookBean> searchBooks = new ArrayList<>();
            SearchBookBean item;
            String zhuangtai;
            JsonObject root = new JsonParser().parse(body).getAsJsonObject();
            JsonObject info = root.getAsJsonObject("info");
                int pageI = info.get("page").getAsInt();
                if (pageI == 1) {
                    if (root.has("aladdin")) {
                        JsonObject aladdin = root.getAsJsonObject("aladdin");
                        String bookId = aladdin.get("bid").getAsString();
                        if(aladdin.get("status").getAsInt() == 1)
                        {
                            zhuangtai = "完结";
                        }else{
                            zhuangtai = "连载";
                        }
                        item = new SearchBookBean();
                        item.setTag(tag);
                        item.setOrigin(name);
                        item.setSearchTime(Integer.MAX_VALUE);
                        item.setAuthor(aladdin.get("author").getAsString());
                        item.setKind(aladdin.get("category").getAsString()+","+zhuangtai+","+aladdin.get("words").getAsInt());
                        item.setLastChapter(aladdin.get("latest_chapter").getAsJsonObject().get("cname").getAsString());
                        item.setName(aladdin.get("title").getAsString());
                        item.setNoteUrl("http://c1.shuqireader.com/httpserver/filecache/get_book_content_" + bookId);
                        item.setCoverUrl(aladdin.get("cover").getAsString().replace("\\/", "/"));
                        item.setIntroduce(aladdin.get("desc").getAsString());
                        item.putVariable("bookId", bookId);
                        searchBooks.add(item);
                    }
                }

            if (root.has("data")) {
                JsonArray booksArray = root.getAsJsonArray("data");
                for (JsonElement element : booksArray) {
                    JsonObject book = element.getAsJsonObject();
                    String bookId = book.get("bid").getAsString();
                    if(book.get("status").getAsInt() == 1)
                    {
                        zhuangtai = "完结";
                    }else{
                        zhuangtai = "连载";
                    }
                    item = new SearchBookBean();
                    item.setTag(tag);
                    item.setOrigin(name);
                    item.setSearchTime(Integer.MAX_VALUE);
                    item.setAuthor(book.get("author").getAsString());
                    item.setKind(book.get("category").getAsString()+","+zhuangtai+","+book.get("words").getAsInt());
                    item.setLastChapter(book.get("first_chapter").getAsString());
                    item.setName(book.get("title").getAsString());
                    item.setNoteUrl("http://c1.shuqireader.com/httpserver/filecache/get_book_content_" + bookId);
                    item.setCoverUrl(book.get("cover").getAsString().replace("\\/", "/"));
                    item.setIntroduce(book.get("desc").getAsString());
                    item.putVariable("bookId", bookId);
                    searchBooks.add(item);
                }
            }
            Debug.printLog(tag, "└找到 " + searchBooks.size() + " 个匹配的结果");
            SearchBookBean item1 = searchBooks.get(0);
            Debug.printLog(tag, 1, "┌获取书名");
            String bookName = item1.getName();
            Debug.printLog(tag, 1, "└" + bookName);
            if (!TextUtils.isEmpty(bookName)) {
                Debug.printLog(tag, 1, "┌获取作者");
                Debug.printLog(tag, 1, "└" + item1.getAuthor());
                Debug.printLog(tag, 1, "┌获取分类");
                Debug.printLog(tag, 111, "└" + item1.getKind());
                Debug.printLog(tag, 1, "┌获取最新章节");
                Debug.printLog(tag, 1, "└" + item1.getLastChapter());
                Debug.printLog(tag, 1, "┌获取简介");
                Debug.printLog(tag, 1, "└" + item1.getIntroduce());
                Debug.printLog(tag, 1, "┌获取封面");
                Debug.printLog(tag, 1, "└" + item1.getCoverUrl());
            }
            Debug.printLog(tag, "-书籍列表解析结束");
            e.onNext(searchBooks);
            e.onComplete();
        });
    }

    /**
     * 网络请求并解析书籍信息
     */

    public Observable<BookShelfBean> getBookInfo(BookShelfBean bookShelfBean) {
        String bid = bookShelfBean.getVariableMap().get("bookId");
        String Data = bid + "1514984538213800000037e81a9d8f02596e1b895d07c171d5c9";
        String Sign = MD5Utils.strToMd5By32(Data);
        HashMap<String, String> fieldMap = new HashMap<>();
        fieldMap.put("timestamp", "1514984538213");
        fieldMap.put("user_id", "8000000");
        fieldMap.put("bookId", bid);
        fieldMap.put("sign", Sign);
        return BaseModelImpl.getInstance().getRetrofitString("http://walden1.shuqireader.com")
                .create(IShuqiApi.class)
                .getBookDetail(fieldMap)
                .flatMap(response -> analyzeBookInfo(response, bookShelfBean));
    }

    private Observable<BookShelfBean> analyzeBookInfo(final Response<String> response, final BookShelfBean bookShelfBean) {
        return Observable.create(e -> {
            String baseUrl;
            baseUrl = NetworkUtils.getUrl(response);
            String s = response.body();
            if (TextUtils.isEmpty(s)) {
                e.onError(new Throwable(MApplication.getInstance().getString(R.string.get_book_info_error) + baseUrl));
                return;
            } else {
                Debug.printLog(tag, "┌成功获取详情页");
                Debug.printLog(tag, "└" + baseUrl);
            }
            JsonObject root = new JsonParser().parse(s).getAsJsonObject();
            JsonObject data = root.getAsJsonObject("data");
            JsonObject jsonx = data.getAsJsonObject("lastChapter");
            String chapterName = jsonx.get("chapterName").getAsString();
            BookInfoBean bookInfoBean = bookShelfBean.getBookInfoBean();
            bookShelfBean.setLastChapterName(chapterName);
            bookInfoBean.setTag(tag);
            bookInfoBean.setOrigin(name);
            bookInfoBean.setCoverUrl(data.get("imgUrl").getAsString());
            bookInfoBean.setName(data.get("bookName").getAsString());
            bookInfoBean.setAuthor(data.get("authorName").getAsString());
            bookInfoBean.setIntroduce(data.get("desc").getAsString());
            bookInfoBean.setNoteUrl(bookShelfBean.getNoteUrl());   //id
            bookInfoBean.setChapterUrl(bookShelfBean.getNoteUrl());

            Debug.printLog(tag, "┌详情信息预处理");
            Debug.printLog(tag, "└详情预处理完成");

            Debug.printLog(tag, "┌获取书名");
            String bookName = bookInfoBean.getName();
            Debug.printLog(tag, "└" + bookName);

            Debug.printLog(tag, "┌获取作者");
            Debug.printLog(tag, "└" + bookInfoBean.getAuthor());

            Debug.printLog(tag, "┌获取最新章节");
            Debug.printLog(tag, "└" + chapterName);

            Debug.printLog(tag, "┌获取简介");
            Debug.printLog(tag, 1, "└" + bookInfoBean.getIntroduce());

            Debug.printLog(tag, "┌获取封面");
            Debug.printLog(tag, "└" + bookInfoBean.getCoverUrl());

            Debug.printLog(tag, "-详情页解析完成");
            e.onNext(bookShelfBean);
            e.onComplete();
        });
    }

    /**
     * 网络解析图书目录
     */

    public Observable<List<BookChapterBean>> getChapterList(BookShelfBean bookShelfBean) {
        String bid = bookShelfBean.getVariableMap().get("bookId");
        String Data = bid + "1514984538213800000037e81a9d8f02596e1b895d07c171d5c9";
        String Sign = MD5Utils.strToMd5By32(Data);
        HashMap<String, String> fieldMap = new HashMap<>();
        fieldMap.put("timestamp", "1514984538213");
        fieldMap.put("user_id", "8000000");
        fieldMap.put("bookId", bid);
        fieldMap.put("sign", Sign);
        return BaseModelImpl.getInstance().getRetrofitString("http://walden1.shuqireader.com")
                .create(IShuqiApi.class)
                .getChapterList(fieldMap)
                .flatMap(response -> analyzeChapterList(response, bookShelfBean.getNoteUrl()));
    }

    private Observable<List<BookChapterBean>> analyzeChapterList(final Response<String> response, String noteUrl) {
        return Observable.create(e -> {
            String baseUrl;
            baseUrl = NetworkUtils.getUrl(response);
            String s = response.body();
            if (TextUtils.isEmpty(s)) {
                e.onError(new Throwable(MApplication.getInstance().getString(R.string.get_chapter_list_error) + baseUrl));
                return;
            } else {
                Debug.printLog(tag, 1, "┌成功获取目录页");
                Debug.printLog(tag, 1, "└" + baseUrl);
            }
            List<BookChapterBean> chapterBeans = new ArrayList<>();
            JsonObject root = new JsonParser().parse(s).getAsJsonObject();
            JsonObject data = root.getAsJsonObject("data");
            JsonArray chapterListArray = data.getAsJsonArray("chapterList");
            for (JsonElement element : chapterListArray) {
                JsonArray volumeListsArray = element.getAsJsonObject().getAsJsonArray("volumeList");
                for (JsonElement ele : volumeListsArray) {
                    String chapterId = ele.getAsJsonObject().get("chapterId").getAsString();
                    String chapterName = ele.getAsJsonObject().get("chapterName").getAsString();
                    BookChapterBean temp = new BookChapterBean();
                    temp.setDurChapterUrl(noteUrl + "_" + chapterId + "_1_0.xml");   //id
                    temp.setDurChapterName(chapterName);
                    temp.setNoteUrl(noteUrl);
                    chapterBeans.add(temp);
                }
            }
            Debug.printLog(tag, 1, "┌解析目录列表");
            Debug.printLog(tag, 1, "└找到 " + chapterBeans.size() + " 个章节");
            Debug.printLog(tag, 1, "┌获取章节名称");
            Debug.printLog(tag, 1, "└" + chapterBeans.get(chapterBeans.size() - 1).getDurChapterName());
            Debug.printLog(tag, 1, "┌获取章节网址");
            Debug.printLog(tag, 1, "└" + chapterBeans.get(chapterBeans.size() - 1).getDurChapterUrl());
            Debug.printLog(tag, 1, "-目录解析完成");
            e.onNext(chapterBeans);
            e.onComplete();
        });
    }

    /**
     * 章节缓存
     */

    public Observable<BookContentBean> getBookContent(BaseChapterBean chapterBean, final BaseChapterBean nextChapterBean, final BookShelfBean bookShelfBean) {
        if (StringUtils.isBlank(chapterBean.getDurChapterUrl())) {
            return Observable.error(new NullPointerException("chapter url is invalid"));
        }
        return BaseModelImpl.getInstance().getRetrofitString(StringUtils.getBaseUrl(chapterBean.getDurChapterUrl()))
                .create(IHttpGetApi.class)
                .get(chapterBean.getDurChapterUrl(), AnalyzeHeaders.getMap(null))
                .flatMap(response -> analyzeBookContent(response.body(), chapterBean));
    }

    private Observable<BookContentBean> analyzeBookContent(String response, BaseChapterBean chapterBean) {
        return Observable.create(e -> {
            if (TextUtils.isEmpty(response)) {
                e.onError(new Throwable(MApplication.getInstance().getString(R.string.get_content_error) + StringUtils.getBaseUrl(chapterBean.getDurChapterUrl())));
                return;
            }
            Debug.printLog(tag, "┌成功获取正文页");
            Debug.printLog(tag, "└" + chapterBean.getDurChapterUrl());
            BookContentBean bookContentBean = new BookContentBean();
            bookContentBean.setDurChapterUrl(chapterBean.getDurChapterUrl());
            bookContentBean.setDurChapterIndex(chapterBean.getDurChapterIndex());
            //bookContentBean.setDurChapterName(chapterBean.getDurChapterName());
            bookContentBean.setNoteUrl(chapterBean.getNoteUrl());
            bookContentBean.setDurChapterContent(decodeChapterContent(getContent(response)));
            Debug.printLog(tag, 1, "┌解析正文内容");
            //Debug.printLog(tag, 1, "└" + bookContentBean.getDurChapterContent());
            if (bookContentBean.getDurChapterContent() != null && bookContentBean.getDurChapterContent().length() > 4000) {
                Debug.printLog(tag, 1, "└" + bookContentBean.getDurChapterContent().substring(0, 4000) + "···");
            } else {
                Debug.printLog(tag, 1, "└" + bookContentBean.getDurChapterContent());
            }
            e.onNext(bookContentBean);
            e.onComplete();
        });
    }

    private static String getContent(String text) {
        Pattern pattern = Pattern.compile("(?<=\\[CDATA\\[)(\\S+)(?=\\]\\]\\>)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    public String decodeChapterContent(String string) {
        if (string == null) {
            return "";
        }
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < bytes.length; i++) {
            int charAt = bytes[i];
            if (65 <= charAt && charAt <= 90) {
                charAt = charAt + 13;
                if (charAt > 90) {
                    charAt = ((charAt % 90) + 65) - 1;
                }
            } else if (97 <= charAt && charAt <= 122) {
                charAt = charAt + 13;
                if (charAt > 122) {
                    charAt = ((charAt % 122) + 97) - 1;
                }
            }
            bytes[i] = (byte) charAt;
        }
        String content = new String(bytes, StandardCharsets.UTF_8);
        return StringUtils.base64Decode(content).replace("<br/>", "\n");
    }

}