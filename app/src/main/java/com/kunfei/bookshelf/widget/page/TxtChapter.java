// 
// Decompiled by Procyon v0.5.30
// 

package com.kunfei.bookshelf.widget.page;

import java.util.ArrayList;
import java.util.List;

public class TxtChapter
{
    private String msg;
    private List<Integer> paragraphLengthList;
    private int position;
    private Status status;
    private List<Integer> txtPageLengthList;
    private List<TxtPage> txtPageList;
    
    TxtChapter(final int position) {
        this.txtPageList = new ArrayList<TxtPage>();
        this.txtPageLengthList = new ArrayList<Integer>();
        this.paragraphLengthList = new ArrayList<Integer>();
        this.status = Status.LOADING;
        this.position = position;
    }
    
    void addPage(final TxtPage txtPage) {
        this.txtPageList.add(txtPage);
    }
    
    void addParagraphLength(final int n) {
        this.paragraphLengthList.add(n);
    }
    
    void addTxtPageLength(final int n) {
        this.txtPageLengthList.add(n);
    }
    
    public String getMsg() {
        return this.msg;
    }
    
    TxtPage getPage(final int n) {
        if (!this.txtPageList.isEmpty()) {
            final List<TxtPage> txtPageList = this.txtPageList;
            return txtPageList.get(Math.max(0, Math.min(n, txtPageList.size() - 1)));
        }
        return null;
    }
    
    int getPageLength(final int n) {
        final List<Integer> txtPageLengthList = this.txtPageLengthList;
        if (txtPageLengthList != null && n >= 0 && n < txtPageLengthList.size()) {
            return this.txtPageLengthList.get(n);
        }
        return -1;
    }
    
    int getPageSize() {
        return this.txtPageList.size();
    }
    
    int getParagraphIndex(final int n) {
        for (int i = 0; i < this.paragraphLengthList.size(); ++i) {
            if ((i == 0 || this.paragraphLengthList.get(i - 1) < n) && n <= this.paragraphLengthList.get(i)) {
                return i;
            }
        }
        return -1;
    }
    
    List<Integer> getParagraphLengthList() {
        return this.paragraphLengthList;
    }
    
    public int getPosition() {
        return this.position;
    }
    
    Status getStatus() {
        return this.status;
    }
    
    List<Integer> getTxtPageLengthList() {
        return this.txtPageLengthList;
    }
    
    List<TxtPage> getTxtPageList() {
        return this.txtPageList;
    }
    
    public void setMsg(final String msg) {
        this.msg = msg;
    }
    
    void setStatus(final Status status) {
        this.status = status;
    }
    
    public enum Status
    {
        CATEGORY_EMPTY, 
        CHANGE_SOURCE, 
        EMPTY, 
        ERROR, 
        FINISH, 
        LOADING;
    }
}
