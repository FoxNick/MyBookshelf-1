package com.kunfei.bookshelf.widget.page;

import android.text.Layout;
import android.text.StaticLayout;

import androidx.annotation.NonNull;

import com.kunfei.bookshelf.bean.BookChapterBean;
import com.kunfei.bookshelf.help.ChapterContentHelp;
import com.kunfei.bookshelf.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

class ChapterProvider {
    private PageLoader pageLoader;
    private ChapterContentHelp contentHelper = new ChapterContentHelp();

    ChapterProvider(PageLoader pageLoader) {
        this.pageLoader = pageLoader;
    }

    TxtChapter dealLoadPageList(BookChapterBean chapter, boolean isPrepare) {
        TxtChapter txtChapter = new TxtChapter(chapter.getDurChapterIndex());
        // 判断章节是否存在
        if (!isPrepare || pageLoader.noChapterData(chapter)) {
            if (pageLoader instanceof PageLoaderNet && !NetworkUtils.isNetWorkAvailable()) {
                txtChapter.setStatus(TxtChapter.Status.ERROR);
                txtChapter.setMsg("网络连接不可用");
            }
            return txtChapter;
        }
        String content;
        try {
            content = pageLoader.getChapterContent(chapter);
        } catch (Exception e) {
            txtChapter.setStatus(TxtChapter.Status.ERROR);
            txtChapter.setMsg("读取内容出错\n" + e.getLocalizedMessage());
            return txtChapter;
        }
        if (content == null) {
            txtChapter.setStatus(TxtChapter.Status.ERROR);
            txtChapter.setMsg("缓存文件不存在");
            return txtChapter;
        }
        return loadPageList(chapter, content);
    }

    /**
     * 将章节数据，解析成页面列表
     *
     * @param chapter：章节信息
     * @param content：章节的文本
     */
    private TxtChapter loadPageList(BookChapterBean bookChapterBean, @NonNull String str) {
        //生成的页面
        TxtChapter txtChapter = new TxtChapter(bookChapterBean.getDurChapterIndex());
        if (this.pageLoader.book.isAudio()) {
            txtChapter.setStatus(TxtChapter.Status.FINISH);
            txtChapter.setMsg(str);
            TxtPage txtPage = new TxtPage(txtChapter.getTxtPageList().size());
            txtPage.setTitle(contentHelper.replaceContent(this.pageLoader.book.getBookInfoBean().getName(), this.pageLoader.book.getTag(), bookChapterBean.getDurChapterName(), true,true));
            txtPage.addLine(contentHelper.replaceContent(this.pageLoader.book.getBookInfoBean().getName(), this.pageLoader.book.getTag(), bookChapterBean.getDurChapterName(), true,true));
            txtPage.addLine(str);
            txtPage.setTitleLines(1);
            txtChapter.addPage(txtPage);
            addTxtPageLength(txtChapter, txtPage.getContent().length());
            txtChapter.addPage(txtPage);
            return txtChapter;
        }
        str = contentHelper.replaceContent(this.pageLoader.book.getBookInfoBean().getName(), this.pageLoader.book.getTag(), str, pageLoader.book.getReplaceEnable(),false);
        String str3 = "\n";
        String[] allLine = str.split(str3);
        List<String> lines = new ArrayList<>();
        List<TxtLine> txtLists = new ArrayList<>();//记录每个字的位置 //pzl
        int rHeight = pageLoader.mVisibleHeight - (pageLoader.contentMarginHeight * 2);
        boolean showTitle = pageLoader.readBookControl.getShowTitle().booleanValue();// 是否展示标题
        String paragraph = null;
        if (showTitle) {
            paragraph = contentHelper.replaceContent(this.pageLoader.book.getBookInfoBean().getName(), this.pageLoader.book.getTag(), bookChapterBean.getDurChapterName(), true,true);
            paragraph = paragraph.trim() + str3;
        }
        int i = 1;
        int i3 = 0;
        int titleLinesCount = 0;
        int i2 = 1;
        while (true) {
            String obj = "\n";
            String[] strArr;
            TxtChapter txtChapter2;
            if (!showTitle) {
                if (i2 >= allLine.length) {
                    break;
                }
            }
            if (i2 == i && showTitle) {
                rHeight = (rHeight - pageLoader.mFirstPageMarginTop) - pageLoader.mFirstPageMarginButtom;
            }
            if (!showTitle) {
                paragraph = allLine[i2].replaceAll("\\s", " ").trim();
                i2++;
                if (!paragraph.equals("")) {
                    paragraph = pageLoader.indent + paragraph + str3;
                }
            }
            addParagraphLength(txtChapter, paragraph.length());
            while (paragraph.length() > 0) {
                //当前空间，是否容得下一行文字
                if (showTitle) {
                    rHeight -= pageLoader.mTitlePaint.getTextSize();
                } else {
                    rHeight -= pageLoader.mTextPaint.getTextSize();
                }
                // 一页已经填充满了，创建 TextPage
                if (rHeight <= 0) {
                    // 创建Page
                    TxtPage txtPage2 = new TxtPage(txtChapter.getTxtPageList().size());
                    txtPage2.setTitle(contentHelper.replaceContent(this.pageLoader.book.getBookInfoBean().getName(), this.pageLoader.book.getTag(), bookChapterBean.getDurChapterName(), true,true));
                    txtPage2.addLines(lines);
                    txtPage2.txtLists = new ArrayList(txtLists);
                    txtPage2.setTitleLines(titleLinesCount);
                    txtChapter.addPage(txtPage2);
                    addTxtPageLength(txtChapter, txtPage2.getContent().length());
                    //重置Lines
                    lines.clear();
                    txtLists.clear();
                    rHeight = pageLoader.mVisibleHeight - (pageLoader.contentMarginHeight * 2);
                    titleLinesCount = 0;
                } else {
                    int wordCount;
                    String obj2;
                    //测量一行占用的字节数
                    if (showTitle) {
                        wordCount = new StaticLayout(paragraph, pageLoader.mTitlePaint, pageLoader.mVisibleWidth, Layout.Alignment.ALIGN_NORMAL, 0.0f, 0.0f, false).getLineEnd(i3);
                    } else {
                        wordCount = new StaticLayout(paragraph, pageLoader.mTextPaint, pageLoader.mVisibleWidth, Layout.Alignment.ALIGN_NORMAL, 0.0f, 0.0f, false).getLineEnd(i3);
                    }
                    String substring = paragraph.substring(i3, wordCount);
                    if (substring.equals(obj)) {
                        strArr = allLine;
                        txtChapter2 = txtChapter;
                        obj2 = obj;
                    } else {
                        //将一行字节，存储到lines中
                        lines.add(substring);
                        //begin pzl
                        //记录每个字的位置
                        char[] toCharArray = substring.toCharArray();
                        TxtLine txtLine = new TxtLine();
                        txtLine.CharsData = new ArrayList();
                        i = toCharArray.length;
                        while (i3 < i) {
                            strArr = allLine;
                            String str2 = String.valueOf(toCharArray[i3]);
                            txtChapter2 = txtChapter;
                            float measureText = pageLoader.mTextPaint.measureText(str2);
                            if (showTitle) {
                                measureText = pageLoader.mTitlePaint.measureText(str2);
                            }
                            TxtChar txtChar = new TxtChar();
                            obj2 = obj;
                            txtChar.chardata = toCharArray[i3];
                            txtChar.charWidth = measureText;//字宽
                            txtChar.Index = 66;//每页每个字的位置
                            txtLine.CharsData.add(txtChar);
                            i3++;
                            txtChapter = txtChapter2;
                            allLine = strArr;
                            obj = obj2;
                        }
                        strArr = allLine;
                        txtChapter2 = txtChapter;
                        obj2 = obj;
                        txtLists.add(txtLine);
                        //end pzl
                        //设置段落间距
                        if (showTitle) {
                            titleLinesCount++;
                            rHeight -= pageLoader.mTitleInterval;
                        } else {
                            rHeight -= pageLoader.mTextInterval;
                        }
                    }
                    paragraph = paragraph.substring(wordCount);
                    txtChapter = txtChapter2;
                    allLine = strArr;
                    obj = obj2;
                    i3 = 0;
                }
            }
            strArr = allLine;
            txtChapter2 = txtChapter;
            String str7 = obj;
            //增加段落的间距
            if (!(showTitle || lines.size() == 0)) {
                rHeight = (rHeight - pageLoader.mTextPara) + pageLoader.mTextInterval;
            }
            if (showTitle) {
                rHeight = (rHeight - pageLoader.mTitlePara) + pageLoader.mTitleInterval;
                txtChapter = txtChapter2;
                allLine = strArr;
                str3 = str7;
                i = 1;
                showTitle = false;
            } else {
                txtChapter = txtChapter2;
                allLine = strArr;
                str3 = str7;
                i = 1;
            }
            i3 = 0;
        }
        if (lines.size() != 0) {
            //创建Page
            TxtPage txtPage3 = new TxtPage(txtChapter.getTxtPageList().size());
            txtPage3.setTitle(contentHelper.replaceContent(this.pageLoader.book.getBookInfoBean().getName(), this.pageLoader.book.getTag(), bookChapterBean.getDurChapterName(), true,true));
            txtPage3.addLines(lines);
            txtPage3.txtLists = new ArrayList(txtLists);
            txtPage3.setTitleLines(titleLinesCount);
            txtChapter.addPage(txtPage3);
            addTxtPageLength(txtChapter, txtPage3.getContent().length());
            //重置Lines
            lines.clear();
            txtLists.clear();
        }
        if (txtChapter.getPageSize() > 0) {
            txtChapter.setStatus(TxtChapter.Status.FINISH);
        } else {
            txtChapter.setStatus(TxtChapter.Status.ERROR);
            txtChapter.setMsg("未加载到内容");
        }
        return txtChapter;
    }

    private void addTxtPageLength(TxtChapter txtChapter, int length) {
        if (txtChapter.getTxtPageLengthList().isEmpty()) {
            txtChapter.addTxtPageLength(length);
        } else {
            txtChapter.addTxtPageLength(txtChapter.getTxtPageLengthList().get(txtChapter.getTxtPageLengthList().size() - 1) + length);
        }
    }

    private void addParagraphLength(TxtChapter txtChapter, int length) {
        if (txtChapter.getParagraphLengthList().isEmpty()) {
            txtChapter.addParagraphLength(length);
        } else {
            txtChapter.addParagraphLength(txtChapter.getParagraphLengthList().get(txtChapter.getParagraphLengthList().size() - 1) + length);
        }
    }
}
