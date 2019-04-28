package com.huangxuping.codegen;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PostmanTest {
    public String validateValue;
    public String title;
    public String uuid;
    public String statusCode;
    public Boolean isCreate = false;
    public Boolean isUpdateById = false;
    public Boolean isDeleteById = false;
    public Boolean isGetById = false;
    public Boolean isSearch = true;
    public Boolean hasMore = true;
    public Boolean isFailValidate = false;
    public Boolean isNoAuth = false;
    public Boolean isTokenExpired = false;
    public Boolean isForbiddenToken = false;

    public List<PostmanPathItem> paths;
    static final public String UNAUTHORIZED_TITLE = "401 - unauthorized request";
    static final public String EXPIRED_TITLE = "401 - expired token";
    static final public String NOT_ALLOW_TITLE = "403 - not allow to call this service";

    public PostmanTest(String title) {
        this.title = title;
        if ( title.startsWith("2") || title.startsWith("4") ) {
            statusCode = title.substring(0, 3);
            if (statusCode.equalsIgnoreCase("201")) {
                this.isCreate = true;
                this.isSearch = false;
            }
            if ( title.startsWith("4") ) {
              this.isFailValidate = true;
            }
            if ( title.equals(UNAUTHORIZED_TITLE) ) {
                this.isNoAuth = true;
            }
            if ( title.equals(EXPIRED_TITLE) ) {
                this.isTokenExpired = true;
            }
            if ( title.equals(NOT_ALLOW_TITLE) ) {
                this.isForbiddenToken = true;
            }
        }
        this.uuid = UUID.randomUUID().toString();
    }

    public void pathHandle(String eneityName) {
//        if(this.paths != null){
//            if (this.isGetById || this.isUpdateById || this.isDeleteById){
//                for(PostmanPathItem item: paths){
//                    if(item.pathName.startsWith(":")) {
//                        item.pathName = "{{"+eneityName+"_ID1}}";
//                    }
//                }
//            }
//        }
    }

    static public PostmanTest getCreateSuccessTests(String entityName) {
        return new PostmanTest("201 - create " + entityName);
    }

    static public PostmanTest getDeleteSuccessTests() {
        PostmanTest ret = new PostmanTest(("204 - delete by id"));
        ret.isDeleteById = true;
        ret.isSearch = false;
        return ret;
    }

    static public PostmanTest getUpdateSuccessTests() {
        PostmanTest ret = new PostmanTest("200 - update by id");
        ret.isUpdateById = true;
        ret.isSearch = false;
        return ret;
    }

    static public PostmanTest getFindByIdSuccessTest() {
        PostmanTest ret = new PostmanTest("200 - find by id");
        ret.isGetById = true;
        ret.isSearch = false;
        return ret;
    }

    static public ArrayList<PostmanTest> getIdTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.add(new PostmanTest(paramName + ": invalid id"));
        rets.add(new PostmanTest(paramName + ": wrong format id"));
        rets.add(new PostmanTest(paramName + ": ref entity invalid"));
        return rets;
    }
    static public ArrayList<PostmanTest> getEmailTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.add(new PostmanTest(paramName + ": invalid email format"));
        return rets;
    }
    static public ArrayList<PostmanTest> getStringTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.add(new PostmanTest("400 - " + paramName + ": invalid value"));
        rets.add(new PostmanTest("200 - " + paramName + ": case insensitive"));
        return rets;
    }
    static private ArrayList<PostmanTest> getNumberTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.add(new PostmanTest("400 - " + paramName + ": too min"));
        rets.add(new PostmanTest("400 - " + paramName + ": too max"));
        rets.add(new PostmanTest("400 - " + paramName + ": not number"));
        rets.add(new PostmanTest("400 - " + paramName + ": is beyond number range"));

        return rets;
    }
    static public ArrayList<PostmanTest> getIntLongTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.addAll(getNumberTests(paramName));
        rets.add(new PostmanTest("400 - " + paramName + ": is decimal"));
        return rets;
    }
    static public ArrayList<PostmanTest> getFloatTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.addAll(getNumberTests(paramName));
        return rets;
    }
    static public ArrayList<PostmanTest> getBooleanTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.add(new PostmanTest("400 - " + paramName + ": invalid bool value"));
        rets.add(new PostmanTest("200 - " + paramName + ": is yes/no"));
        rets.add(new PostmanTest("200 - " + paramName + ": is true/false"));
        return rets;
    }
    static public ArrayList<PostmanTest> getDateTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.add(new PostmanTest("400 - " + paramName + ": invalid date format"));
        rets.add(new PostmanTest("400 - " + paramName + ": too early or later"));
        rets.add(new PostmanTest("400 - " + paramName + ": early than start date or later than end date"));

        return rets;
    }
    static public ArrayList<PostmanTest> getUuidTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.add(new PostmanTest("400 - " + paramName + ": invalid uuid format"));
        return rets;
    }
    static public ArrayList<PostmanTest> getEnumTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.add(new PostmanTest("400 - " + paramName + ": value not in enum range"));
        rets.add(new PostmanTest("200 - " + paramName + ": case insensitive"));
        return rets;
    }
    static public ArrayList<PostmanTest> getYearTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.add(new PostmanTest("400 - " + paramName + ": too early or later"));
        rets.add(new PostmanTest("400 - " + paramName + ": not number"));
        rets.add(new PostmanTest("400 - " + paramName + ": is decimal"));
        rets.add(new PostmanTest("400 - " + paramName + ": is beyond number range"));
        return rets;
    }
    static public ArrayList<PostmanTest> getMonthTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.add(new PostmanTest("400 - " + paramName + ": not number"));
        rets.add(new PostmanTest("400 - " + paramName + ": is decimal"));
        rets.add(new PostmanTest("400 - " + paramName + ": invalid month number"));
        return rets;
    }
    static public ArrayList<PostmanTest> getDayTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.add(new PostmanTest("400 - " + paramName + ": not number"));
        rets.add(new PostmanTest("400 - " + paramName + ": is decimal"));
        rets.add(new PostmanTest("400 - " + paramName + ": too small or large"));
        rets.add(new PostmanTest("400 - " + paramName + ": invalid in the year/month"));
        return rets;
    }
    static public ArrayList<PostmanTest> getWeekTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.add(new PostmanTest("400 - " + paramName + ": not number"));
        rets.add(new PostmanTest("400 - " + paramName + ": is decimal"));
        rets.add(new PostmanTest("400 - " + paramName + ": too small or large"));
        rets.add(new PostmanTest("400 - " + paramName + ": invalid in the year/month"));
        return rets;
    }
    static public ArrayList<PostmanTest> getQuarterTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.add(new PostmanTest("400 - " + paramName + ": not number"));
        rets.add(new PostmanTest("400 - " + paramName + ": is decimal"));
        rets.add(new PostmanTest("400 - " + paramName + ": too small or large"));
        return rets;
    }
    static public ArrayList<PostmanTest> getSearchTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.add(new PostmanTest("200 - " + paramName + ": no search condition"));
        rets.add(new PostmanTest("200 - " + paramName + ": one condition"));
        rets.add(new PostmanTest("200 - " + paramName + ": case insensitive"));
        rets.add(new PostmanTest("200 - " + paramName + ": combine condition"));
        rets.add(new PostmanTest("200 - " + paramName + ": all condition"));
        rets.add(new PostmanTest("200 - " + paramName + ": condition with sort"));
        return rets;
    }
    static public ArrayList<PostmanTest> getCreateTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.add(new PostmanTest("409 - " + paramName + ": uniqueness property repeated"));
        rets.add(new PostmanTest("400 - " + paramName + ": reference id invalid"));
        return rets;
    }
    static public ArrayList<PostmanTest> getUpdateTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.add(new PostmanTest("409 - " + paramName + ": modify uniqueness name repeated"));
        rets.add(new PostmanTest("400 - " + paramName + ": invalid update id"));
        rets.add(new PostmanTest("400 - " + paramName + ": update freeze status entity"));
        rets.add(new PostmanTest("400 - " + paramName + ": reference id invalid"));

        return rets;
    }
    static public ArrayList<PostmanTest> getDeleteTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.add(new PostmanTest("400 - " + paramName + ": invalid delete id"));
        rets.add(new PostmanTest("400 - " + paramName + ": delete freeze status entity"));
        rets.add(new PostmanTest("400 - " + paramName + ": entity being referenced"));

        return rets;
    }
    static public ArrayList<PostmanTest> getCreditCardTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.add(new PostmanTest("400 - " + paramName + ": payment expired date"));
        rets.add(new PostmanTest("400 - " + paramName + ": invalid card number"));
        rets.add(new PostmanTest("400 - " + paramName + ": payment too small or large"));
        return rets;
    }
    static public ArrayList<PostmanTest> getIdCardTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.add(new PostmanTest("400 - " + paramName + ": invalid id card"));
        rets.add(new PostmanTest("400 - " + paramName + ": id card repeated with another"));
        return rets;
    }
    static public ArrayList<PostmanTest> getTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.add(new PostmanTest(paramName + ": "));
        return rets;
    }
}
