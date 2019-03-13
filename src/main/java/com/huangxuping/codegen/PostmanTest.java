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
    public Boolean hasMore = true;
    public List<PostmanPathItem> paths;

    public PostmanTest(String title) {
        this.title = title;
        if ( title.startsWith("failure") || title.startsWith("success") ) {
            statusCode = title.substring(8, 11);
            if (statusCode.equalsIgnoreCase("201")) {
                this.isCreate = true;
            }
        }
        this.uuid = UUID.randomUUID().toString();
    }

    public void pathHandle(String eneityName) {
        if(this.paths != null){
            if (this.isGetById || this.isUpdateById || this.isDeleteById){
                for(PostmanPathItem item: paths){
                    if(item.pathName.startsWith(":")) {
                        item.pathName = "{{"+eneityName+"_ID1}}";
                    }
                }
            }
        }
    }

    static public PostmanTest getCreateSuccessTests(String entityName) {
        return new PostmanTest("success 201 - created " + entityName);
    }

    static public PostmanTest getDeleteSuccessTests() {
        PostmanTest ret = new PostmanTest(("success 200 - deleted by id"));
        ret.isDeleteById = true;
        return ret;
    }

    static public PostmanTest getUpdateSuccessTests() {
        PostmanTest ret = new PostmanTest("success 200 - updated by id");
        ret.isUpdateById = true;
        return ret;
    }

    static public PostmanTest getFindByIdSuccessTest() {
        PostmanTest ret = new PostmanTest("success 200 - find by id");
        ret.isGetById = true;
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
        rets.add(new PostmanTest("failure 400 - " + paramName + ": too short"));
        rets.add(new PostmanTest("failure 400 - " + paramName + ": too long"));
        rets.add(new PostmanTest("failure 400 - " + paramName + ": invalid value"));
        rets.add(new PostmanTest("success 200 - " + paramName + ": value case diff"));
        rets.add(new PostmanTest("success 200 - " + paramName + ": value with header & tail spacer"));
        return rets;
    }
    static private ArrayList<PostmanTest> getNumberTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.add(new PostmanTest("failure 400 - " + paramName + ": too min"));
        rets.add(new PostmanTest("failure 400 - " + paramName + ": too max"));
        rets.add(new PostmanTest("success 200 - " + paramName + ": exactly min"));
        rets.add(new PostmanTest("success 200 - " + paramName + ": exactly max"));
        rets.add(new PostmanTest("failure 400 - " + paramName + ": not number"));
        rets.add(new PostmanTest("failure 400 - " + paramName + ": is beyond number range"));

        return rets;
    }
    static public ArrayList<PostmanTest> getIntLongTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.addAll(getNumberTests(paramName));
        rets.add(new PostmanTest("failure 400 - " + paramName + ": is decimal"));
        return rets;
    }
    static public ArrayList<PostmanTest> getFloatTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.addAll(getNumberTests(paramName));
        return rets;
    }
    static public ArrayList<PostmanTest> getBooleanTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.add(new PostmanTest("failure 400 - " + paramName + ": invalid bool value"));
        rets.add(new PostmanTest("success 200 - " + paramName + ": is y/n"));
        rets.add(new PostmanTest("success 200 - " + paramName + ": is t/f"));
        rets.add(new PostmanTest("success 200 - " + paramName + ": is yes/no"));
        rets.add(new PostmanTest("success 200 - " + paramName + ": is true/false"));
        return rets;
    }
    static public ArrayList<PostmanTest> getDateTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.add(new PostmanTest("failure 400 - " + paramName + ": invalid date format"));
        rets.add(new PostmanTest("failure 400 - " + paramName + ": too early"));
        rets.add(new PostmanTest("failure 400 - " + paramName + ": too later"));
        rets.add(new PostmanTest("success 200 - " + paramName + ": exactly lower bound"));
        rets.add(new PostmanTest("success 200 - " + paramName + ": exactly later bound"));
        rets.add(new PostmanTest("failure 400 - " + paramName + ": early than start date"));
        rets.add(new PostmanTest("failure 400 - " + paramName + ": later than end date"));

        return rets;
    }
    static public ArrayList<PostmanTest> getUuidTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.add(new PostmanTest("failure 400 - " + paramName + ": invalid uuid format"));
        return rets;
    }
    static public ArrayList<PostmanTest> getEnumTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.add(new PostmanTest("failure 400 - " + paramName + ": value not in enum range"));
        rets.add(new PostmanTest("success 200 - " + paramName + ": value case diff"));
        return rets;
    }
    static public ArrayList<PostmanTest> getYearTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.add(new PostmanTest("failure 400 - " + paramName + ": too early"));
        rets.add(new PostmanTest("failure 400 - " + paramName + ": too later"));
        rets.add(new PostmanTest("failure 400 - " + paramName + ": not number"));
        rets.add(new PostmanTest("failure 400 - " + paramName + ": is decimal"));
        rets.add(new PostmanTest("failure 400 - " + paramName + ": is beyond number range"));
        rets.add(new PostmanTest("success 200 - " + paramName + ": exactly lower bound year"));
        rets.add(new PostmanTest("success 200 - " + paramName + ": exactly uppder bound year"));
        return rets;
    }
    static public ArrayList<PostmanTest> getMonthTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.add(new PostmanTest("failure 400 - " + paramName + ": not number"));
        rets.add(new PostmanTest("failure 400 - " + paramName + ": is decimal"));
        rets.add(new PostmanTest("failure 400 - " + paramName + ": invalid month number"));
        return rets;
    }
    static public ArrayList<PostmanTest> getDayTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.add(new PostmanTest("failure 400 - " + paramName + ": not number"));
        rets.add(new PostmanTest("failure 400 - " + paramName + ": is decimal"));
        rets.add(new PostmanTest("failure 400 - " + paramName + ": too small"));
        rets.add(new PostmanTest("failure 400 - " + paramName + ": too large"));
        rets.add(new PostmanTest("failure 400 - " + paramName + ": invalid in the year/month"));
        return rets;
    }
    static public ArrayList<PostmanTest> getWeekTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.add(new PostmanTest("failure 400 - " + paramName + ": not number"));
        rets.add(new PostmanTest("failure 400 - " + paramName + ": is decimal"));
        rets.add(new PostmanTest("failure 400 - " + paramName + ": too small"));
        rets.add(new PostmanTest("failure 400 - " + paramName + ": too large"));
        rets.add(new PostmanTest("failure 400 - " + paramName + ": invalid in the year/month"));
        return rets;
    }
    static public ArrayList<PostmanTest> getQuarterTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.add(new PostmanTest("failure 400 - " + paramName + ": not number"));
        rets.add(new PostmanTest("failure 400 - " + paramName + ": is decimal"));
        rets.add(new PostmanTest("failure 400 - " + paramName + ": too small"));
        rets.add(new PostmanTest("failure 400 - " + paramName + ": too large"));
        return rets;
    }
    static public ArrayList<PostmanTest> getSearchTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.add(new PostmanTest("success 200 - " + paramName + ": no search condition"));
        rets.add(new PostmanTest("success 200 - " + paramName + ": one condition"));
        rets.add(new PostmanTest("success 200 - " + paramName + ": case insensitive"));
        rets.add(new PostmanTest("success 200 - " + paramName + ": combine condition"));
        rets.add(new PostmanTest("success 200 - " + paramName + ": all condition"));
        rets.add(new PostmanTest("success 200 - " + paramName + ": sort by"));
        rets.add(new PostmanTest("success 200 - " + paramName + ": condition with sort"));
        return rets;
    }
    static public ArrayList<PostmanTest> getCreateTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.add(new PostmanTest("failure 409 - " + paramName + ": uniqueness name repeated"));
        rets.add(new PostmanTest("failure 400 - " + paramName + ": null body"));
        rets.add(new PostmanTest(paramName + ": reference id invalid"));
        return rets;
    }
    static public ArrayList<PostmanTest> getUpdateTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.add(new PostmanTest("failure 409 - " + paramName + ": modify uniqueness name repeated"));
        rets.add(new PostmanTest("failure 400 - " + paramName + ": null body"));
        rets.add(new PostmanTest("failure 400 - " + paramName + ": invalid update id"));
        rets.add(new PostmanTest("failure 400 - " + paramName + ": update freeze status entity"));
        rets.add(new PostmanTest("failure 400 - " + paramName + ": reference id invalid"));

        return rets;
    }
    static public ArrayList<PostmanTest> getDeleteTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.add(new PostmanTest("failure 400 - " + paramName + ": invalid delete id"));
        rets.add(new PostmanTest("failure 400 - " + paramName + ": delete freeze status entity"));
        rets.add(new PostmanTest("failure 400 - " + paramName + ": entity being referenced"));

        return rets;
    }
    static public ArrayList<PostmanTest> getCreditCardTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.add(new PostmanTest("failure 400 - " + paramName + ": payment expired date"));
        rets.add(new PostmanTest("failure 400 - " + paramName + ": invalid card number"));
        rets.add(new PostmanTest("failure 400 - " + paramName + ": payment too small"));
        rets.add(new PostmanTest("failure 400 - " + paramName + ": payment too large"));
        return rets;
    }
    static public ArrayList<PostmanTest> getIdCardTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.add(new PostmanTest("failure 400 - " + paramName + ": invalid id card"));
        rets.add(new PostmanTest("failure 400 - " + paramName + ": id card repeated with another"));
        return rets;
    }
    static public ArrayList<PostmanTest> getTests(String paramName){
        ArrayList rets = new ArrayList<PostmanTest>();
        rets.add(new PostmanTest(paramName + ": "));
        return rets;
    }
}
