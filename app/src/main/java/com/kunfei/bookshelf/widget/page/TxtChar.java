// 
// Decompiled by Procyon v0.5.30
// 

package com.kunfei.bookshelf.widget.page;

import android.graphics.Point;

public class TxtChar
{
    public Point BottomLeftPosition;
    public Point BottomRightPosition;
    public int Index;
    public Boolean Selected;
    public Point TopLeftPosition;
    public Point TopRightPosition;
    public float charWidth;
    public char chardata;
    
    public TxtChar() {
        this.Selected = false;
        this.TopLeftPosition = null;
        this.TopRightPosition = null;
        this.BottomLeftPosition = null;
        this.BottomRightPosition = null;
        this.charWidth = 0.0f;
        this.Index = 0;
    }
    
    public Point getBottomLeftPosition() {
        return this.BottomLeftPosition;
    }
    
    public Point getBottomRightPosition() {
        return this.BottomRightPosition;
    }
    
    public float getCharWidth() {
        return this.charWidth;
    }
    
    public char getChardata() {
        return this.chardata;
    }
    
    public int getIndex() {
        return this.Index;
    }
    
    public Boolean getSelected() {
        return this.Selected;
    }
    
    public Point getTopLeftPosition() {
        return this.TopLeftPosition;
    }
    
    public Point getTopRightPosition() {
        return this.TopRightPosition;
    }
    
    public void setBottomLeftPosition(final Point bottomLeftPosition) {
        this.BottomLeftPosition = bottomLeftPosition;
    }
    
    public void setBottomRightPosition(final Point bottomRightPosition) {
        this.BottomRightPosition = bottomRightPosition;
    }
    
    public void setCharWidth(final float charWidth) {
        this.charWidth = charWidth;
    }
    
    public void setChardata(final char chardata) {
        this.chardata = chardata;
    }
    
    public void setIndex(final int index) {
        this.Index = index;
    }
    
    public void setSelected(final Boolean selected) {
        this.Selected = selected;
    }
    
    public void setTopLeftPosition(final Point topLeftPosition) {
        this.TopLeftPosition = topLeftPosition;
    }
    
    public void setTopRightPosition(final Point topRightPosition) {
        this.TopRightPosition = topRightPosition;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ShowChar [chardata=");
        sb.append(this.chardata);
        sb.append(", Selected=");
        sb.append(this.Selected);
        sb.append(", TopLeftPosition=");
        sb.append(this.TopLeftPosition);
        sb.append(", TopRightPosition=");
        sb.append(this.TopRightPosition);
        sb.append(", BottomLeftPosition=");
        sb.append(this.BottomLeftPosition);
        sb.append(", BottomRightPosition=");
        sb.append(this.BottomRightPosition);
        sb.append(", charWidth=");
        sb.append(this.charWidth);
        sb.append(", Index=");
        sb.append(this.Index);
        sb.append("]");
        return sb.toString();
    }
}
