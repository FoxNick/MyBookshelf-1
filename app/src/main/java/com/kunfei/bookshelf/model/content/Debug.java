package com.kunfei.bookshelf.model.content;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.hwangjr.rxbus.RxBus;
import com.kunfei.bookshelf.bean.BookChapterBean;
import com.kunfei.bookshelf.bean.BookContentBean;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.constant.RxBusTag;
import com.kunfei.bookshelf.help.BookshelfHelp;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.model.UpLastChapterModel;
import com.kunfei.bookshelf.model.WebBookModel;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeRule;
import com.kunfei.bookshelf.utils.NetworkUtils;
import com.kunfei.bookshelf.utils.RxUtils;
import com.kunfei.bookshelf.utils.StringUtils;
import com.kunfei.bookshelf.utils.TimeUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import static com.kunfei.bookshelf.constant.AppConstant.SCRIPT_ENGINE;
import javax.script.SimpleBindings;

import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class Debug {
    public static String SOURCE_DEBUG_TAG;
    @SuppressLint("ConstantLocale")
    private static final DateFormat DEBUG_TIME_FORMAT = new SimpleDateFormat("[mm:ss.SSS]", Locale.getDefault());
    private static long startTime;
    private AnalyzeRule analyzeRule;

    private static String getDoTime() {
        return TimeUtils.millis2String(System.currentTimeMillis() - startTime, DEBUG_TIME_FORMAT);
    }

    public static void printLog(String tag, String msg) {
        printLog(tag, 1, msg, true);
    }

    static void printLog(String tag, int state, String msg) {
        printLog(tag, state, msg, true);
    }

    static void printLog(String tag, int state, String msg, boolean print) {
        printLog(tag, state, msg, print, false);
    }

    public static void printLog(String tag, int state, String msg, boolean print, boolean formatHtml) {
        if (print && Objects.equals(SOURCE_DEBUG_TAG, tag)) {
            if (formatHtml) {
                msg = StringUtils.formatHtml(msg);
            }
            if (state == 111) {
                msg = msg.replace("\n", ",");
            }
            msg = String.format("%s %s", getDoTime(), msg);
            RxBus.get().post(RxBusTag.PRINT_DEBUG_LOG, msg);
        }
    }

    public static void newDebug(String tag, String key, @NonNull CompositeDisposable compositeDisposable) {
        new Debug(tag, key, compositeDisposable);
    }

    private CompositeDisposable compositeDisposable;

    private Debug(String tag, String key, CompositeDisposable compositeDisposable) {
        UpLastChapterModel.destroy();
        startTime = System.currentTimeMillis();
        SOURCE_DEBUG_TAG = tag;
        this.compositeDisposable = compositeDisposable;
        if (NetworkUtils.isUrl(key)) {
            printLog(String.format("%s %s", getDoTime(), "⇒开始访问详情页:" + key));
            BookShelfBean bookShelfBean = new BookShelfBean();
            bookShelfBean.setTag(Debug.SOURCE_DEBUG_TAG);
            bookShelfBean.setNoteUrl(key);
            bookShelfBean.setDurChapter(0);
            bookShelfBean.setGroup(0);
            bookShelfBean.setDurChapterPage(0);
            bookShelfBean.setFinalDate(System.currentTimeMillis());
            bookInfoDebug(bookShelfBean);
        } else if (key.contains("::")) {
            int num = Integer.parseInt(key.substring(key.indexOf("::") + 2));

            //printLog(String.format("%s %s", getDoTime(), "⇒开始访问发现页:" + url));
            try {
                BookSourceBean SY = BookSourceManager.getBookSourceByUrl(tag);
                if (!TextUtils.isEmpty(SY.getRuleFindUrl())) {
                    String findRule;
                    boolean isJsAndCache = SY.getRuleFindUrl().startsWith("<js>");
                    if (isJsAndCache) {
                        String jsStr = SY.getRuleFindUrl().substring(4, SY.getRuleFindUrl().lastIndexOf("<"));
                        findRule = evalJS(jsStr, SY.getBookSourceUrl()).toString();
                    } else {
                        findRule = SY.getRuleFindUrl();
                    }
                    String[] kindA = findRule.split("(&&|\n)+");
                    String[] kind = kindA[num].split("::");
                    printLog(String.format("%s %s", getDoTime(), "⇒开始访问发现页[" + kind[0] + "]:" + kind[1]));
                    findDebug(kind[1]);
                }else{
                    printLog(String.format("%s %s", getDoTime(), "≡" + "无发现规则"));
                    finish();
                }
            } catch (Exception exception) {
                printLog(String.format("%s %s", getDoTime(), "≡" + "发现规则语法错误"));
                finish();
            }
            //findDebug(url);
        } else {
            printLog(String.format("%s %s", getDoTime(), "⇒开始搜索关键字:" + key));
            searchDebug(key);
        }
    }
    /**
     * 执行JS
     */
    private Object evalJS(String jsStr, String baseUrl) {
        try {
            SimpleBindings bindings = new SimpleBindings();
            bindings.put("java", getAnalyzeRule());
            bindings.put("baseUrl", baseUrl);
            return SCRIPT_ENGINE.eval(jsStr, bindings);
        } catch (Exception e) {
            return e.getMessage();
        }
    }
    private AnalyzeRule getAnalyzeRule() {
        if (analyzeRule == null) {
            analyzeRule = new AnalyzeRule(null);
        }
        return analyzeRule;
    }
    private void findDebug(String url) {
        printLog(String.format("\n%s ≡开始获取发现页", getDoTime()));
        WebBookModel.getInstance().findBook(url, 1, Debug.SOURCE_DEBUG_TAG)
                .compose(RxUtils::toSimpleSingle)
                .subscribe(new Observer<List<SearchBookBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @SuppressLint("DefaultLocale")
                    @Override
                    public void onNext(List<SearchBookBean> searchBookBeans) {
                        SearchBookBean searchBookBean = searchBookBeans.get(0);
                        if (!TextUtils.isEmpty(searchBookBean.getNoteUrl())) {
                            bookInfoDebug(BookshelfHelp.getBookFromSearchBook(searchBookBean));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        printError(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void searchDebug(String key) {
        printLog(String.format("\n%s ≡开始获取搜索页", getDoTime()));
        WebBookModel.getInstance().searchBook(key, 1, Debug.SOURCE_DEBUG_TAG)
                .compose(RxUtils::toSimpleSingle)
                .subscribe(new Observer<List<SearchBookBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @SuppressLint("DefaultLocale")
                    @Override
                    public void onNext(List<SearchBookBean> searchBookBeans) {
                        SearchBookBean searchBookBean = searchBookBeans.get(0);
                        if (!TextUtils.isEmpty(searchBookBean.getNoteUrl())) {
                            bookInfoDebug(BookshelfHelp.getBookFromSearchBook(searchBookBean));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        printError(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void bookInfoDebug(BookShelfBean bookShelfBean) {
        printLog(String.format("\n%s ≡开始获取详情页", getDoTime()));
        WebBookModel.getInstance().getBookInfo(bookShelfBean)
                .compose(RxUtils::toSimpleSingle)
                .subscribe(new Observer<BookShelfBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(BookShelfBean bookShelfBean) {
                        bookChapterListDebug(bookShelfBean);
                    }

                    @Override
                    public void onError(Throwable e) {
                        printError(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void bookChapterListDebug(BookShelfBean bookShelfBean) {
        printLog(String.format("\n%s ≡开始获取目录页", getDoTime()));
        WebBookModel.getInstance().getChapterList(bookShelfBean)
                .compose(RxUtils::toSimpleSingle)
                .subscribe(new Observer<List<BookChapterBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @SuppressLint("DefaultLocale")
                    @Override
                    public void onNext(List<BookChapterBean> chapterBeanList) {
                        if (chapterBeanList.size() > 0) {
                            BookChapterBean nextChapter = chapterBeanList.size() > 2 ? chapterBeanList.get(1) : null;
                            bookContentDebug(bookShelfBean, chapterBeanList.get(0), nextChapter);
                        } else {
                            printError("获取到的目录为空");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        printError(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void bookContentDebug(BookShelfBean bookShelfBean, BookChapterBean bookChapterBean, BookChapterBean nextChapterBean) {
        printLog(String.format("\n%s ≡开始获取正文页", getDoTime()));
        WebBookModel.getInstance().getBookContent(bookShelfBean, bookChapterBean, nextChapterBean)
                .compose(RxUtils::toSimpleSingle)
                .subscribe(new Observer<BookContentBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(BookContentBean bookContentBean) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        printError(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                            finish();
                    }
                });
    }

    private void printLog(String log) {
        RxBus.get().post(RxBusTag.PRINT_DEBUG_LOG, log);
    }

    private void printError(String msg) {
        RxBus.get().post(RxBusTag.PRINT_DEBUG_LOG, msg);
        finish();
    }

    private void finish() {
        RxBus.get().post(RxBusTag.PRINT_DEBUG_LOG, "finish");
    }

}