package com.huangxuping.codegen;

import io.swagger.v3.oas.models.OpenAPI;
import org.openapitools.codegen.*;

import java.util.*;

public class GeneratorUtil {
    public void handleOperation(CodegenOperation operation){
        List<CodegenParameter> params = operation.allParams;
        if (params != null && params.size() == 0) {
            operation.allParams = null;
        }
        List<CodegenResponse> responses = operation.responses;
        if (responses != null) {
            for (CodegenResponse resp : responses) {
                if ("0".equals(resp.code)) {
                    resp.code = "default";
                }
            }
        }
        if (operation.examples != null && !operation.examples.isEmpty()) {
            // Leave application/json* items only
            for (Iterator<Map<String, String>> it = operation.examples.iterator(); it.hasNext(); ) {
                final Map<String, String> example = it.next();
                final String contentType = example.get("contentType");
                if (contentType == null || !contentType.startsWith("application/json")) {
                    it.remove();
                }
            }
        }
    }

    public void handleOperationWithModels(CodegenOperation operation, List<Object> allModels){
        if (operation.bodyParam != null && operation.bodyParam.isModel) {
            List<String> inlineRefModels = new ArrayList<>();
            for (Object item: allModels) {
                HashMap map = (HashMap)item;
                // dataType === model.name
                CodegenModel model = (CodegenModel)map.get("model");
                if ( model.classname.equalsIgnoreCase(operation.bodyParam.dataType)){
                    if (model.classname.startsWith("InlineObject")) {
                        for (CodegenProperty property : model.allVars){
                            if (property != null) {
                                if (property.isListContainer && property.items.isModel) {
                                    inlineRefModels.add(property.items.complexType);
                                } else if (property.isModel) {
                                    inlineRefModels.add(property.complexType);
                                }
                            }
                        }
                    } else {
                        operation.bodyParam.vendorExtensions.put("x-refModel", model);
                    }
                    break;
                }
            }
            // TODO: 当前只支持第一个引用参数，因为"x-refModel"只有一个，以后需要可以考虑扩展成支持多个
            if (inlineRefModels.size() > 0) {
                for (Object item: allModels) {
                    HashMap map = (HashMap) item;
                    CodegenModel model = (CodegenModel) map.get("model");
                    if (model.classname.equalsIgnoreCase(inlineRefModels.get(0))) {
                        operation.bodyParam.vendorExtensions.put("x-refModel", model);
                        break;
                    }
                }
            }
        }

        if (operation.httpMethod != null) {
            switch(operation.httpMethod){
                case "get":
                    operation.vendorExtensions.put("x-get-method", true);
                    break;
                case "put":
                    operation.vendorExtensions.put("x-put-method", true);
                    break;
                case "post":
                    operation.vendorExtensions.put("x-post-method", true);
                    break;
                case "delete":
                    operation.vendorExtensions.put("x-delete-method", true);
                    break;
                case "patch":
                    operation.vendorExtensions.put("x-patch-method", true);
                    break;
                case "head":
                    operation.vendorExtensions.put("x-head-method", true);
                    break;
            }
        }
    }


}
