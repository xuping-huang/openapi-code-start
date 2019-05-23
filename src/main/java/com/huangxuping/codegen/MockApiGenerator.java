package com.huangxuping.codegen;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import org.apache.commons.lang3.tuple.Pair;
import org.openapitools.codegen.CliOption;
import org.openapitools.codegen.CodegenOperation;
import org.openapitools.codegen.CodegenType;
import org.openapitools.codegen.SupportingFile;
import org.openapitools.codegen.languages.AbstractJavaCodegen;
import org.openapitools.codegen.utils.URLPathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;


public class MockApiGenerator extends AbstractJavaCodegen {
    private static final Logger LOGGER = LoggerFactory.getLogger(MockApiGenerator.class);

    public static final String TITLE = "title";
    public static final String SERVER_PORT = "serverPort";
    public static final String API_FIRST = "apiFirst";

    protected String title = "OpenAPI Spring";
    protected String configPackage = "org.openapitools.configuration";
    protected boolean reactive = false;
    protected boolean useBeanValidation = true;
    protected String outputFolder = "";
    private GeneratorUtil generatorUtil = new GeneratorUtil();

    public MockApiGenerator() {
        super();
        outputFolder = "generated-code/mockapi";
        embeddedTemplateDir = templateDir = "mockapi";
    }

    @Override
    public CodegenType getTag() {
        return CodegenType.OTHER;
    }

    @Override
    public String getName() {
        return "mockapi";
    }

    @Override
    public String getHelp() {
        return "Generates api mock file.";
    }

    private void loadPropertiesFromConfig() {
    }

    @Override
    public void processOpts() {
        apiTestTemplateFiles.clear();
        apiTemplateFiles.clear();
        modelTemplateFiles.clear();
        supportingFiles.clear();

        List<Pair<String,String>> configOptions = additionalProperties.entrySet().stream()
                .filter(e -> !Arrays.asList(API_FIRST, "hideGenerationTimestamp").contains(e.getKey()))
                .filter(e -> cliOptions.stream().map(CliOption::getOpt).anyMatch(opt -> opt.equals(e.getKey())))
                .map(e -> Pair.of(e.getKey(), e.getValue().toString()))
                .collect(Collectors.toList());
        additionalProperties.put("configOptions", configOptions);

        LOGGER.info("----------------------------------");
        super.processOpts();
        // clear model and api doc template as this codegen
        // does not support auto-generated markdown doc at the moment
        modelDocTemplateFiles.remove("model_doc.mustache");
        apiDocTemplateFiles.remove("api_doc.mustache");

        typeMapping.put("file", "Resource");
        importMapping.put("Resource", "org.springframework.core.io.Resource");


        supportingFiles.add(new SupportingFile("route.mustache",
                "", "app-route.js"));
        supportingFiles.add(new SupportingFile("mockapi.mustache",
                "", "api-mock.js"));
        supportingFiles.add(new SupportingFile("mocktest.mustache",
                "", "test-mock.js"));
        // 加载额外的additionalProperties属性
        loadPropertiesFromConfig();

    }

    @Override
    public Map<String, Object> postProcessOperationsWithModels(Map<String, Object> objs, List<Object> allModels) {
        @SuppressWarnings("unchecked")
        Map<String, Object> objectMap = (Map<String, Object>) objs.get("operations");
        @SuppressWarnings("unchecked")
        List<CodegenOperation> operations = (List<CodegenOperation>) objectMap.get("operation");
        for (CodegenOperation operation : operations) {
            operation.httpMethod = operation.httpMethod.toLowerCase(Locale.ROOT);
            generatorUtil.handleOperation(operation);
            generatorUtil.handleOperationWithModels(operation, allModels);
        }
        return objs;
    }

    @Override
    public void preprocessOpenAPI(OpenAPI openAPI) {
        super.preprocessOpenAPI(openAPI);
    }

}