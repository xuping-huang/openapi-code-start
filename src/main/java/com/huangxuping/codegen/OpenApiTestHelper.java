package com.huangxuping.codegen;

import org.apache.commons.lang3.StringUtils;
import org.openapitools.codegen.*;

import java.util.ArrayList;
import java.util.List;

public class OpenApiTestHelper {
    static public List<PostmanTest> analyzeOperationTest(CodegenOperation operation) {
        List<PostmanTest> tests = new ArrayList<>();
        switch(operation.httpMethod){
            case "get":
                if(operation.isListContainer)
                    tests = analyzeSearchOperationTest(operation);
                else
                    tests = analyzeGetOperationTest(operation);
                break;
            case "put":
            case "patch":
                tests = analyzeUpdateOperationTest(operation);
                break;
            case "post":
                tests = analyzeCreateOperationTest(operation);
                break;
            case "delete":
                tests = analyzeDeleteOperationTest(operation);
                break;
        }
        ArrayList respTests = analyzeOperationResponseTest(operation);
        tests.addAll(respTests);
        PostmanTest lastTest = tests.get(tests.size() - 1);
        lastTest.hasMore = false;

        String[] pathStrs = StringUtils.split(operation.path, "/");
        List<PostmanPathItem> paths = new ArrayList<>();
        for(int i=0 ; i<pathStrs.length; i++){
            PostmanPathItem pathItem = new PostmanPathItem(pathStrs[i]);
            if(i == pathStrs.length - 1){
                pathItem.hasMore = false;
            }
            paths.add(pathItem);
        }
        for(PostmanTest test : tests){
            test.paths = clonePathItem(paths);
            test.pathHandle(operation.returnBaseType);
        }
        return tests;
    }

    static private List<PostmanPathItem> clonePathItem(List<PostmanPathItem> paths){
        List<PostmanPathItem> rets = new ArrayList<>();
        for(PostmanPathItem item : paths) {
            rets.add(item.clone());
        }
        return rets;
    }

    static private ArrayList<PostmanTest> analyzeOperationResponseTest(CodegenOperation operation) {
        ArrayList tests = new ArrayList<PostmanTest>();
        ArrayList usedStatus = new ArrayList<String>();
        tests.add(new PostmanTest("failure 401 - unauthorized request"));
        usedStatus.add("401");
        tests.add(new PostmanTest("failure 403 - not allow to call this service"));
        usedStatus.add("403");
        switch(operation.httpMethod){
            case "get":
                tests.add(new PostmanTest("failure 404 - target not found"));
                usedStatus.add("404");
                break;
            case "put":
            case "patch":
                tests.add(new PostmanTest("failure 409 - entity property conflict"));
                usedStatus.add("409");
                break;
            case "post":
                tests.add(new PostmanTest("failure 409 - entity property conflict"));
                usedStatus.add("409");
                break;
            case "delete":
                tests.add(new PostmanTest("failure 404 - target not found"));
                usedStatus.add("404");
                break;
        }
        for(CodegenResponse response : operation.responses){
            if ( response.code.startsWith("2") ) continue;
            if (usedStatus.contains(response.code)) continue;

            if ( response.code.equalsIgnoreCase("404") ) {
                tests.add(new PostmanTest("failure 404 - target not found"));
            } else if ( response.code.equalsIgnoreCase("405") ) {
                tests.add(new PostmanTest("failure 405 - not allow to call this method"));
            } else if ( response.code.equalsIgnoreCase("409") ) {
                tests.add(new PostmanTest("failure 409 - entity property conflict"));
            } else if ( response.code.equalsIgnoreCase("415") ) {
                tests.add(new PostmanTest("failure 415 - unsupported Media Type"));
            }
        }
        return tests;
    }

    static private ArrayList<PostmanTest> analyzeSearchOperationTest(CodegenOperation operation) {
        ArrayList tests = new ArrayList<PostmanTest>();
        if ( operation.allParams != null ) {
            for (CodegenParameter param : operation.allParams) {
                PostmanParam pp = new PostmanParam(param);
                tests.addAll(handleParam(pp));
            }
            tests.addAll(PostmanTest.getSearchTests("for search"));
        }
        return tests;
    }

    static private ArrayList<PostmanTest> handleParam(PostmanParam param){
        ArrayList tests = new ArrayList<PostmanTest>();
        String validTag = (String)param.vendorExtensions.get("x-valid-tag");
        if("year".equalsIgnoreCase(validTag)){
            tests.addAll(PostmanTest.getYearTests(param.paramName));
            return tests;
        }
        if("month".equalsIgnoreCase(validTag)){
            tests.addAll(PostmanTest.getMonthTests(param.paramName));
            return tests;
        }
        if("day".equalsIgnoreCase(validTag)){
            tests.addAll(PostmanTest.getDayTests(param.paramName));
            return tests;
        }
        if("week".equalsIgnoreCase(validTag)){
            tests.addAll(PostmanTest.getWeekTests(param.paramName));
            return tests;
        }
        if("quarter".equalsIgnoreCase(validTag)){
            tests.addAll(PostmanTest.getQuarterTests(param.paramName));
            return tests;
        }
        if("creditcard".equalsIgnoreCase(validTag)){
            tests.addAll(PostmanTest.getCreditCardTests(param.paramName));
            return tests;
        }
        if("idcard".equalsIgnoreCase(validTag)){
            tests.addAll(PostmanTest.getIdCardTests(param.paramName));
            return tests;
        }
        if(param.isEmail) {
            tests.addAll(PostmanTest.getEmailTests(param.paramName));
            return tests;
        }
        if(param.isUuid) {
            tests.addAll(PostmanTest.getUuidTests(param.paramName));
            return tests;
        }
        if(param.isString) {
            tests.addAll(PostmanTest.getStringTests(param.paramName));
            return tests;
        }
        if(param.isInteger || param.isLong) {
            tests.addAll(PostmanTest.getIntLongTests(param.paramName));
            return tests;
        }
        if(param.isNumber || param.isNumeric || param.isFloat || param.isDouble){
            tests.addAll(PostmanTest.getFloatTests(param.paramName));
            return tests;
        }
        if(param.isBoolean){
            tests.addAll(PostmanTest.getBooleanTests(param.paramName));
            return tests;
        }
        if(param.isDate || param.isDateTime) {
            tests.addAll(PostmanTest.getDateTests(param.paramName));
            return tests;
        }
        if(param.isEnum) {
            tests.addAll(PostmanTest.getEnumTests(param.paramName));
            return tests;
        }
        return tests;
    }

    static private ArrayList<PostmanTest> analyzeGetOperationTest(CodegenOperation operation) {
        ArrayList tests = new ArrayList<PostmanTest>();
        tests.add(PostmanTest.getFindByIdSuccessTest());
        if ( operation.allParams != null ) {
            for (CodegenParameter param : operation.allParams) {
                if (param.isPathParam) {
                    if (param.paramName.toLowerCase().endsWith("id")) {
                        tests.addAll(PostmanTest.getIdTests(param.paramName));
                        continue;
                    }
                }
                PostmanParam pp = new PostmanParam(param);
                tests.addAll(handleParam(pp));
            }
        }
        return tests;
    }
    static private ArrayList<PostmanTest> analyzeUpdateOperationTest(CodegenOperation operation) {
        ArrayList tests = new ArrayList<PostmanTest>();
        tests.add(PostmanTest.getUpdateSuccessTests());
        if ( operation.allParams != null ) {
            for (CodegenParameter param : operation.allParams) {
                PostmanParam pp = new PostmanParam(param);
                if (param.isBodyParam) continue;
                tests.addAll(handleParam(pp));
            }
        }
        if (operation.bodyParam != null && operation.bodyParam.isModel) {
            tests.addAll(findBodyTests(operation.bodyParam));
        }

        tests.addAll(PostmanTest.getUpdateTests("for update"));
        return tests;
    }
    static private ArrayList<PostmanTest> findBodyTests(CodegenParameter bodyParam){
        ArrayList tests = new ArrayList<PostmanTest>();
        CodegenModel model = (CodegenModel)bodyParam.vendorExtensions.get("x-refModel");
        if (model != null){
            for(CodegenProperty property: model.allVars){
                PostmanParam postParam = new PostmanParam(property);
                tests.addAll(handleParam(postParam));
            }
        }
        return tests;
    }
    static private ArrayList<PostmanTest> analyzeCreateOperationTest(CodegenOperation operation) {
        ArrayList tests = new ArrayList<PostmanTest>();
        tests.add(PostmanTest.getCreateSuccessTests(operation.returnBaseType));
        if ( operation.allParams != null ) {
            for (CodegenParameter param : operation.allParams) {
                PostmanParam pp = new PostmanParam(param);
                if (param.isBodyParam) continue;
                tests.addAll(handleParam(pp));
            }
        }
        if (operation.bodyParam != null && operation.bodyParam.isModel) {
            tests.addAll(findBodyTests(operation.bodyParam));
        }
        tests.addAll(PostmanTest.getCreateTests("for create"));
        return tests;
    }
    static private ArrayList<PostmanTest> analyzeDeleteOperationTest(CodegenOperation operation) {
        ArrayList tests = new ArrayList<PostmanTest>();
        tests.add(PostmanTest.getDeleteSuccessTests());
        if ( operation.allParams != null ) {
            for (CodegenParameter param : operation.allParams) {
                PostmanParam pp = new PostmanParam(param);
                tests.addAll(handleParam(pp));
            }
        }
        tests.addAll(PostmanTest.getDeleteTests("for delete"));
        return tests;
    }
}
