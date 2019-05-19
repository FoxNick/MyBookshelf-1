package com.kunfei.bookshelf.widget.page;

import android.annotation.SuppressLint;

import com.kunfei.bookshelf.base.observer.MyObserver;
import com.kunfei.bookshelf.bean.BookChapterBean;
import com.kunfei.bookshelf.bean.BookContentBean;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.help.BookshelfHelp;
import com.kunfei.bookshelf.model.WebBookModel;
import com.kunfei.bookshelf.model.content.WebBook;
import com.kunfei.bookshelf.utils.NetworkUtil;
import com.kunfei.bookshelf.utils.RxUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 网络页面加载器
 */

public class PageLoaderNet extends PageLoader {
    private static final String TAG = "PageLoaderNet";
    private List<String> downloadingChapterList = new ArrayList<>();
    private ExecutorService executorService;
    private Scheduler scheduler;

    PageLoaderNet(PageView pageView, BookShelfBean bookShelfBean, Callback callback) {
        super(pageView, bookShelfBean, callback);
        executorService = Executors.newFixedThreadPool(20);
        scheduler = Schedulers.from(executorService);
    }

    @Override
    public void refreshChapterList() {
        if (!callback.getChapterList().isEmpty()) {
            isChapterListPrepare = true;
            // 打开章节
            skipToChapter(book.getDurChapter(), book.getDurChapterPage());
        } else {
            WebBookModel.getInstance().getChapterList(book)
                    .compose(RxUtils::toSimpleSingle)
                    .subscribe(new MyObserver<BookShelfBean>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            compositeDisposable.add(d);
                        }

                        @Override
                        public void onNext(BookShelfBean bookShelfBean) {
                            isChapterListPrepare = true;
                            // 目录加载完成
                            if (!bookShelfBean.getChapterList().isEmpty()) {
                                BookshelfHelp.delChapterList(bookShelfBean.getNoteUrl());
                                callback.onCategoryFinish(bookShelfBean.getChapterList());
                            }
                            // 加载并显示当前章节
                            skipToChapter(bookShelfBean.getDurChapter(), bookShelfBean.getDurChapterPage());
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (e instanceof WebBook.NoSourceThrowable) {
                                mPageView.autoChangeSource();
                            } else {
                                chapterError(e.getMessage());
                            }
                        }
                    });
        }
    }

    @Override
    public void changeSourceFinish(BookShelfBean book) {
        if (book == null) {
            super.changeSourceFinish(null);
        } else {
            this.book = book;
            callback.onCategoryFinish(book.getChapterList());
            refreshChapterList();
        }
    }

    @SuppressLint("DefaultLocale")
    private synchronized void loadContent(final int chapterIndex) {
        if (downloadingChapterList.size() >= 20) return;
        if (DownloadingList(listHandle.CHECK, callback.getChapterList().get(chapterIndex).getDurChapterUrl()))
            return;
        if (null != book && callback.getChapterList().size() > 0) {
            Observable.create((ObservableOnSubscribe<Integer>) e -> {
                if (shouldRequestChapter(chapterIndex)) {
                    DownloadingList(listHandle.ADD, callback.getChapterList().get(chapterIndex).getDurChapterUrl());
                    e.onNext(chapterIndex);
                }
                e.onComplete();
            })
                    .flatMap(index -> WebBookModel.getInstance().getBookContent(book.getBookInfoBean(), callback.getChapterList().get(chapterIndex)))
                    .subscribeOn(scheduler)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<BookContentBean>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            compositeDisposable.add(d);
                        }

                        @SuppressLint("DefaultLocale")
                        @Override
                        public void onNext(BookContentBean bookContentBean) {
                            DownloadingList(listHandle.REMOVE, bookContentBean.getDurChapterUrl());
                            finishContent(bookContentBean.getDurChapterIndex());
                        }

                        @Override
                        public void onError(Throwable e) {
                            DownloadingList(listHandle.REMOVE, callback.getChapterList().get(chapterIndex).getDurChapterUrl());
                            if (chapterIndex == mCurChapterPos) {
                                if (e instanceof WebBook.NoSourceThrowable) {
                                    mPageView.autoChangeSource();
                                } else {
                                    chapterError(e.getMessage());
                                }
                            }
                        }

                        @Override
                        public void onComplete() {
                        }
                    });
        }
    }

    /**
     * 编辑下载列表
     */
    private synchronized boolean DownloadingList(listHandle editType, String value) {
        if (editType == listHandle.ADD) {
            downloadingChapterList.add(value);
            return true;
        } else if (editType == listHandle.REMOVE) {
            downloadingChapterList.remove(value);
            return true;
        } else {
            return downloadingChapterList.indexOf(value) != -1;
        }
    }

    /**
     * 章节下载完成
     */
    private void finishContent(int chapterIndex) {
        if (chapterIndex == mCurChapterPos) {
            super.parseCurChapter();
        }
        if (chapterIndex == mCurChapterPos - 1) {
            super.parsePrevChapter();
        }
        if (chapterIndex == mCurChapterPos + 1) {
            super.parseNextChapter();
        }
    }

    /**
     * 刷新当前章节
     */
    @SuppressLint("DefaultLocale")
    public void refreshDurChapter() {
        BookshelfHelp.delChapter(BookshelfHelp.getCachePathName(book.getBookInfoBean().getName(), book.getTag()),
                mCurChapterPos, callback.getChapterList().get(mCurChapterPos).getDurChapterName());
        skipToChapter(mCurChapterPos, 0);
    }

    @Override
    protected String getChapterContent(BookChapterBean chapter) {
        return BookshelfHelp.getChapterCache(book, chapter);
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected boolean noChapterData(BookChapterBean chapter) {
        return !BookshelfHelp.isChapterCached(book.getBookInfoBean(), chapter);
    }

    private boolean shouldRequestChapter(Integer chapterIndex) {
        return NetworkUtil.isNetWorkAvailable() && noChapterData(callback.getChapterList().get(chapterIndex));
    }

    // 装载上一章节的内容
    @Override
    void parsePrevChapter() {
        if (mCurChapterPos >= 1) {
            loadContent(mCurChapterPos - 1);
        }
        super.parsePrevChapter();
    }

    // 装载当前章内容。
    @Override
    void parseCurChapter() {
        for (int i = mCurChapterPos; i < Math.min(mCurChapterPos + 5, book.getChapterListSize()); i++) {
            loadContent(i);
        }
        super.parseCurChapter();
    }

    // 装载下一章节的内容
    @Override
    void parseNextChapter() {
        for (int i = mCurChapterPos; i < Math.min(mCurChapterPos + 5, book.getChapterListSize()); i++) {
            loadContent(i);
        }
        super.parseNextChapter();
    }

    @Override
    public void updateChapter() {
        mPageView.getActivity().toast("目录更新中");
        WebBookModel.getInstance().getChapterList(book)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BookShelfBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(BookShelfBean bookShelfBean) {
                        isChapterListPrepare = true;

                        if (bookShelfBean.getChapterList().size() > callback.getChapterList().size()) {
                            mPageView.getActivity().toast("更新完成,有新章节");
                            callback.onCategoryFinish(bookShelfBean.getChapterList());
                        } else {
                            mPageView.getActivity().toast("更新完成,无新章节");
                        }

                        // 加载并显示当前章节
                        skipToChapter(bookShelfBean.getDurChapter(), bookShelfBean.getDurChapterPage());
                    }

                    @Override
                    public void onError(Throwable e) {
                        chapterError(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public void closeBook() {
        super.closeBook();
        executorService.shutdown();
    }

    public enum listHandle {
        ADD, REMOVE, CHECK
    }
}
