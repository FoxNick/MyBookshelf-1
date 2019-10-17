// 
// Decompiled by Procyon v0.5.30
// 

package com.kunfei.bookshelf.widget.page;

import java.util.Iterator;
import java.util.List;

public class TxtLine
{
    public List<TxtChar> CharsData;
    
    public TxtLine() {
        this.CharsData = null;
    }
    
    public List<TxtChar> getCharsData() {
        return this.CharsData;
    }
    
    public String getLineData() {
        final List<TxtChar> charsData = this.CharsData;
        String s;
        String string = s = "";
        if (charsData != null) {
            if (charsData.size() == 0) {
                return "";
            }
            final Iterator<TxtChar> iterator = this.CharsData.iterator();
            while (true) {
                s = string;
                if (!iterator.hasNext()) {
                    break;
                }
                final TxtChar txtChar = iterator.next();
                final StringBuilder sb = new StringBuilder();
                sb.append(string);
                sb.append(txtChar.chardata);
                string = sb.toString();
            }
        }
        return s;
    }
    
    public void setCharsData(final List<TxtChar> charsData) {
        this.CharsData = charsData;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ShowLine [Linedata=");
        sb.append(this.getLineData());
        sb.append("]");
        return sb.toString();
    }
}
