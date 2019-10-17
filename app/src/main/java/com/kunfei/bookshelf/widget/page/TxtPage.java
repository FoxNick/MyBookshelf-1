// 
// Decompiled by Procyon v0.5.30
// 

package com.kunfei.bookshelf.widget.page;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

class TxtPage
{
    private List<String> lines;
    private int position;
    private String title;
    private int titleLines;
    public List<TxtLine> txtLists;
    
    TxtPage(final int position) {
        this.lines = new ArrayList<String>();
        this.txtLists = null;
        this.position = position;
    }
    
    void addLine(final String s) {
        this.lines.add(s);
    }
    
    void addLines(final List<String> list) {
        this.lines.addAll(list);
    }
    
    String getContent() {
        final StringBuilder sb = new StringBuilder();
        if (this.lines != null) {
            for (int i = 0; i < this.lines.size(); ++i) {
                sb.append(this.lines.get(i));
            }
        }
        return sb.toString();
    }
    
    String getLine(final int n) {
        return this.lines.get(n);
    }
    
    List<String> getLines() {
        return this.lines;
    }
    
    int getPosition() {
        return this.position;
    }
    
    String getTitle() {
        return this.title;
    }
    
    int getTitleLines() {
        return this.titleLines;
    }
    
    public List<TxtLine> getTxtLists() {
        return this.txtLists;
    }
    
    void setTitle(final String title) {
        this.title = title;
    }
    
    void setTitleLines(final int titleLines) {
        this.titleLines = titleLines;
    }
    
    public void setTxtLists(final List<TxtLine> txtLists) {
        this.txtLists = txtLists;
    }
    
    int size() {
        return this.lines.size();
    }
}
