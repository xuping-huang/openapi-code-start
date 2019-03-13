package com.huangxuping.codegen;

public class PostmanPathItem {
    public String pathName ;
    public Boolean hasMore = true;
    public PostmanPathItem(String pathName){
        this.pathName = pathName;
    }

    public PostmanPathItem clone(){
        PostmanPathItem ret = new PostmanPathItem(this.pathName);
        ret.hasMore = this.hasMore;
        return ret;
    }
}
