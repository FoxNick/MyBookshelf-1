package com.kunfei.bookshelf.bean;

import java.util.List;

public class FindKindGroupBean {
    private String groupName;
    private String groupTag;
    private List<SearchBookBean> books;
    private String tag;
    private int childrenCount;
    private List<FindKindBean> children;

    public FindKindGroupBean() {
    }
    public FindKindGroupBean(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getChildrenCount() {
        return childrenCount;
    }

    public void setChildrenCount(int childrenCount) {
        this.childrenCount = childrenCount;
    }

    public List<FindKindBean> getChildren() {
        return children;
    }

    public void setChildren(List<FindKindBean> children) {
        this.children = children;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupTag() {
        return groupTag;
    }

    public void setGroupTag(String groupTag) {
        this.groupTag = groupTag;
    }

    public List<SearchBookBean> getBooks() {
        return books;
    }

    public void setBooks(List<SearchBookBean> books) {
        this.books = books;
    }
}
