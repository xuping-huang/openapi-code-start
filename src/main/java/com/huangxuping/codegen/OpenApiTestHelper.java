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
                tests = analyzeUpdateOperationTest(operation);
                break;
            case "patch":
                tests = analyzePartialUpdateOperationTest(operation);
                break;
            case "post":
                tests = analyzeCreateOperationTest(operation);
                break;
            case "delete":
                tests = analyzeDeleteOperationTest(operation);
                break;
        }
        PostmanTest lastTest = tests.get(tests.size() - 1);
        lastTest.hasMore = false;

        String[] pathStrs = StringUtils.split(operation.path, "/");
        List<PostmanPathItem> paths = new ArrayList<>();
        for(int i=0 ; i<pathStrs.length; i++){
            String pathStr = pathStrs[i];
            if (pathStr.startsWith(":")) {
                pathStr = "{{"+pathStr.substring(1)+"}}";
            }
            PostmanPathItem pathItem = new PostmanPathItem(pathStr);
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
        tests.add(new PostmanTest(PostmanTest.UNAUTHORIZED_TITLE));
        usedStatus.add("401");
        tests.add(new PostmanTest(PostmanTest.EXPIRED_TITLE));
        usedStatus.add("401");
        tests.add(new PostmanTest(PostmanTest.NOT_ALLOW_TITLE));
        usedStatus.add("403");
        switch(operation.httpMethod){
            case "get":
                tests.add(new PostmanTest("404 - reference id not found"));
                usedStatus.add("404");
                break;
            case "put":
            case "patch":
                tests.add(new PostmanTest("409 - entity property conflict"));
                usedStatus.add("409");
                break;
            case "post":
                tests.add(new PostmanTest("409 - entity property conflict"));
                usedStatus.add("409");
                break;
            case "delete":
                tests.add(new PostmanTest("404 - reference not found"));
                usedStatus.add("404");
                break;
        }
        for(CodegenResponse response : operation.responses){
            if ( response.code.startsWith("2") ) continue;
            if (usedStatus.contains(response.code)) continue;

            if ( response.code.equalsIgnoreCase("404") ) {
                tests.add(new PostmanTest("404 - reference id not found"));
            } else if ( response.code.equalsIgnoreCase("405") ) {
                tests.add(new PostmanTest("405 - not allow to call this method"));
            } else if ( response.code.equalsIgnoreCase("409") ) {
                tests.add(new PostmanTest("409 - entity property conflict"));
            } else if ( response.code.equalsIgnoreCase("415") ) {
                tests.add(new PostmanTest("415 - unsupported Media Type"));
            }
        }
        return tests;
    }

    static private ArrayList<PostmanTest> analyzeSearchOperationTest(CodegenOperation operation) {
        ArrayList<PostmanTest> tests = new ArrayList<PostmanTest>();
        tests.add(new PostmanTest("200 - get all"));
        if ( operation.allParams != null ) {
            for (CodegenParameter param : operation.allParams) {
                PostmanParam pp = new PostmanParam(param);
                tests.addAll(handleParam(pp));
            }
            tests.addAll(PostmanTest.getSearchTests("search"));
        }
        tests.add(new PostmanTest("400 - null value"));
        ArrayList respTests = analyzeOperationResponseTest(operation);
        tests.addAll(respTests);

        for (PostmanTest test : tests) {
            test.isSearch = true;
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
        if(param.isEmail || param.name.toLowerCase().endsWith("mail")) {
            tests.addAll(PostmanTest.getEmailTests(param.paramName));
            return tests;
        }
        if(param.isUuid || param.name.toLowerCase().endsWith("id")) {
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
        ArrayList<PostmanTest> tests = new ArrayList<PostmanTest>();
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
        tests.add(new PostmanTest("400 - null value"));
        ArrayList respTests = analyzeOperationResponseTest(operation);
        tests.addAll(respTests);

        for (PostmanTest test : tests) {
            test.isGetById = true;
        }
        return tests;
    }
    static private ArrayList<PostmanTest> analyzeUpdateOperationTest(CodegenOperation operation) {
        ArrayList<PostmanTest> tests = new ArrayList<PostmanTest>();
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

        tests.addAll(PostmanTest.getUpdateTests("update"));
        tests.add(new PostmanTest("400 - null value"));
        ArrayList respTests = analyzeOperationResponseTest(operation);
        tests.addAll(respTests);

        for (PostmanTest test : tests) {
            test.isUpdateById = true;
        }
        return tests;
    }
    static private ArrayList<PostmanTest> analyzePartialUpdateOperationTest(CodegenOperation operation) {
        ArrayList<PostmanTest> tests = new ArrayList<PostmanTest>();
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

        tests.addAll(PostmanTest.getUpdateTests("update"));
        tests.add(new PostmanTest("400 - null value"));
        ArrayList respTests = analyzeOperationResponseTest(operation);
        tests.addAll(respTests);
        for (PostmanTest test : tests) {
            test.isPartialUpdateById = true;
        }
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
        ArrayList<PostmanTest> tests = new ArrayList<PostmanTest>();
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
        tests.addAll(PostmanTest.getCreateTests("create"));
        tests.add(new PostmanTest("400 - null value"));
        ArrayList respTests = analyzeOperationResponseTest(operation);
        tests.addAll(respTests);
        for (PostmanTest test : tests) {
            test.isCreate = true;
        }
        return tests;
    }
    static private ArrayList<PostmanTest> analyzeDeleteOperationTest(CodegenOperation operation) {
        ArrayList<PostmanTest> tests = new ArrayList<PostmanTest>();
        tests.add(PostmanTest.getDeleteSuccessTests());
        if ( operation.allParams != null ) {
            for (CodegenParameter param : operation.allParams) {
                PostmanParam pp = new PostmanParam(param);
                tests.addAll(handleParam(pp));
            }
        }
        tests.addAll(PostmanTest.getDeleteTests("delete"));
        tests.add(new PostmanTest("400 - null value"));
        ArrayList respTests = analyzeOperationResponseTest(operation);
        tests.addAll(respTests);

        for (PostmanTest test : tests) {
            test.isDeleteById = true;
        }
        return tests;
    }
}
