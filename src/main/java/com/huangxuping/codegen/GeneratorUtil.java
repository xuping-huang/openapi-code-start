package com.huangxuping.codegen;

import io.swagger.v3.oas.models.OpenAPI;
import org.openapitools.codegen.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
            for (Object item: allModels) {
                HashMap map = (HashMap)item;
                // dataType === model.name
                CodegenModel model = (CodegenModel)map.get("model");
                if ( model.classname.equalsIgnoreCase(operation.bodyParam.dataType)){
                    operation.bodyParam.vendorExtensions.put("x-refModel", model);
                    break;
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
            }
        }
    }


}
