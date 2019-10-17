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
        for (ReplaceRuleBean replaceRule : ReplaceRuleManager.getEnabled()) {
            if (isUseTo(replaceRule.getUseTo(), bookTag, bookName)) {
                try {
                    if(replaceTitle){
                        content = StringUtils.trim(StringUtils.fullToHalf(content)
                                .replace("(", "（")
                                .replace(")", "）")
                                .replaceAll("[\\[\\]【】]+", "")
                                .replaceAll("\\s+", " "));
                        Pattern pattern = Pattern.compile(replaceRule.getFixedRegex(), Pattern.MULTILINE);
                        Matcher matcher = pattern.matcher(content);
                        if (matcher.find()) {
                            int num = StringUtils.stringToInt(matcher.group(2));
                            if (num > 0) {
                                content = matcher.replaceFirst("第" + num + "章 ");
                                break;
                            }
                        }
                    } else {
                        content = content.replaceAll(replaceRule.getFixedRegex(), replaceRule.getReplacement());
                    }

                } catch (Exception ignored) {
                }
            }
        }
        return toTraditional(content);
    }

    private boolean isUseTo(String useTo, String bookTag, String bookName) {
        return TextUtils.isEmpty(useTo)
                || useTo.contains(bookTag)
                || useTo.contains(bookName);
    }

}
