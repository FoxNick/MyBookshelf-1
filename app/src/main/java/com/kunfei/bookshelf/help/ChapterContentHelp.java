package com.kunfei.bookshelf.help;

import android.text.TextUtils;

import com.kunfei.bookshelf.bean.ReplaceRuleBean;
import com.kunfei.bookshelf.model.ReplaceRuleManager;
import com.kunfei.bookshelf.utils.StringUtils;
import com.luhuiguo.chinese.ChineseUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChapterContentHelp {
    private static ChapterContentHelp instance;

    public static synchronized ChapterContentHelp getInstance() {
        if (instance == null)
            instance = new ChapterContentHelp();
        return instance;
    }

    /**
     * 转繁体
     */
    private String toTraditional(String content) {
        int convertCTS = ReadBookControl.getInstance().getTextConvert();
        switch (convertCTS) {
            case 0:
                break;
            case 1:
                content = ChineseUtils.toSimplified(content);
                break;
            case 2:
                content = ChineseUtils.toTraditional(content);
                break;
        }
        return content;
    }

    /**
     * 替换净化
     */
    public String replaceContent(String bookName, String bookTag, String content, Boolean replaceEnable, Boolean replaceTitle) {
        if (!replaceEnable) return toTraditional(content);
        if (ReplaceRuleManager.getEnabled().size() == 0) return toTraditional(content);
        //替换
        if(replaceTitle) {
            content = StringUtils.trim(StringUtils.fullToHalf(content)
                    .replace("(", "（")
                    .replace(")", "）")
                    .replaceAll("[\\[\\]【】]+", ""));
        }
        for (ReplaceRuleBean replaceRule : ReplaceRuleManager.getEnabled()) {
            if (isUseTo(replaceRule.getUseTo(), bookTag, bookName)) {
                try {
                    content = content.replaceAll(replaceRule.getFixedRegex(), replaceRule.getReplacement());
                } catch (Exception ignored) {
                }
            }
        }
        if(replaceTitle){
            return toTraditional(toNumChapter(content));
        } else {
            return toTraditional(content);
        }
    }
    /**
     * 章节数转数字
     */
    public String toNumChapter(String s) {
        if (s == null) {
            return null;
        }
        s = StringUtils.fullToHalf(s).replaceAll("章 章 ", "章 ");
        Pattern pattern = Pattern.compile("^(第([零〇一二两三四五六七八九十百千万]+)[章])");
        Matcher matcher = pattern.matcher(s);
        if (matcher.find()) {
            return matcher.replaceFirst("第" + StringUtils.stringToInt(matcher.group(2)) + "章");
        }
        return s;
    }
    private boolean isUseTo(String useTo, String bookTag, String bookName) {
        return TextUtils.isEmpty(useTo)
                || useTo.contains(bookTag)
                || useTo.contains(bookName);
    }

}
