package com.huangxuping.codegen;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.openapitools.codegen.*;
import org.openapitools.codegen.languages.AbstractJavaCodegen;
import org.openapitools.codegen.utils.URLPathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;


public class PostmanGenerator extends AbstractJavaCodegen {
    private static final Logger LOGGER = LoggerFactory.getLogger(PostmanGenerator.class);

    public static final String TITLE = "title";
    public static final String SERVER_PORT = "serverPort";
    public static final String API_FIRST = "apiFirst";

    protected String title = "OpenAPI Spring";
    protected String configPackage = "org.openapitools.configuration";
    protected boolean reactive = false;
    protected boolean useBeanValidation = true;
    protected String outputFolder = "";
    private GeneratorUtil generatorUtil = new GeneratorUtil();

    public PostmanGenerator() {
        super();
        outputFolder = "generated-code/postman";
        embeddedTemplateDir = templateDir = "postman";
    }

    @Override
    public CodegenType getTag() {
        return CodegenType.OTHER;
    }

    @Override
    public String getName() {
        return "postman";
    }

    @Override
    public String getHelp() {
        return "Generates postman test file.";
    }

    private void loadPropertiesFromConfig() {
        additionalProperties.put("postmanEnvId", UUID.randomUUID().toString());
        additionalProperties.put("postmanCollId", UUID.randomUUID().toString());
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
        //TODO: add doc templates
        modelDocTemplateFiles.remove("model_doc.mustache");
        apiDocTemplateFiles.remove("api_doc.mustache");

        typeMapping.put("file", "Resource");
        importMapping.put("Resource", "org.springframework.core.io.Resource");


        supportingFiles.add(new SupportingFile("postman_environment.mustache",
                "", "app.postman_environment.json"));
        supportingFiles.add(new SupportingFile("postman_collection.mustache",
                "", "app.postman_collection.json"));
        // 加载额外的additionalProperties属性
        loadPropertiesFromConfig();

    }

    private void analyzeOperationTest(CodegenOperation operation) {
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
        operation.vendorExtensions.put("x-valid-methods", tests);
    }

    private List<PostmanPathItem> clonePathItem(List<PostmanPathItem> paths){
        List<PostmanPathItem> rets = new ArrayList<>();
        for(PostmanPathItem item : paths) {
            rets.add(item.clone());
        }
        return rets;
    }

    private ArrayList<PostmanTest> analyzeOperationResponseTest(CodegenOperation operation) {
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

    private ArrayList<PostmanTest> analyzeSearchOperationTest(CodegenOperation operation) {
        ArrayList tests = new ArrayList<PostmanTest>();
        for(CodegenParameter param : operation.allParams) {
            PostmanParam pp = new PostmanParam(param);
            tests.addAll(handleParam(pp));
        }
        tests.addAll(PostmanTest.getSearchTests("for search"));
        return tests;
    }

    private ArrayList<PostmanTest> handleParam(PostmanParam param){
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

    private ArrayList<PostmanTest> analyzeGetOperationTest(CodegenOperation operation) {
        ArrayList tests = new ArrayList<PostmanTest>();
        tests.add(PostmanTest.getFindByIdSuccessTest());
        for(CodegenParameter param : operation.allParams) {
            if(param.isPathParam) {
                if (param.paramName.toLowerCase().endsWith("id")) {
                    tests.addAll(PostmanTest.getIdTests(param.paramName));
                    continue;
                }
            }
            PostmanParam pp = new PostmanParam(param);
            tests.addAll(handleParam(pp));
        }
        return tests;
    }
    private ArrayList<PostmanTest> analyzeUpdateOperationTest(CodegenOperation operation) {
        ArrayList tests = new ArrayList<PostmanTest>();
        tests.add(PostmanTest.getUpdateSuccessTests());
        for(CodegenParameter param : operation.allParams) {
            PostmanParam pp = new PostmanParam(param);
            if(param.isBodyParam) continue;
            tests.addAll(handleParam(pp));
        }
        if (operation.bodyParam != null && operation.bodyParam.isModel) {
            tests.addAll(findBodyTests(operation.bodyParam));
        }

        tests.addAll(PostmanTest.getUpdateTests("for update"));
        return tests;
    }
    private ArrayList<PostmanTest> findBodyTests(CodegenParameter bodyParam){
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
    private ArrayList<PostmanTest> analyzeCreateOperationTest(CodegenOperation operation) {
        ArrayList tests = new ArrayList<PostmanTest>();
        tests.add(PostmanTest.getCreateSuccessTests(operation.returnBaseType));
        for(CodegenParameter param : operation.allParams) {
            PostmanParam pp = new PostmanParam(param);
            if(param.isBodyParam) continue;
            tests.addAll(handleParam(pp));
        }
        if (operation.bodyParam != null && operation.bodyParam.isModel) {
            tests.addAll(findBodyTests(operation.bodyParam));
        }
        tests.addAll(PostmanTest.getCreateTests("for create"));
        return tests;
    }
    private ArrayList<PostmanTest> analyzeDeleteOperationTest(CodegenOperation operation) {
        ArrayList tests = new ArrayList<PostmanTest>();
        tests.add(PostmanTest.getDeleteSuccessTests());
        for(CodegenParameter param : operation.allParams) {
            PostmanParam pp = new PostmanParam(param);
            tests.addAll(handleParam(pp));
        }
        tests.addAll(PostmanTest.getDeleteTests("for delete"));
        return tests;
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
            analyzeOperationTest(operation);
        }
        return objs;
    }

    @Override
    public void preprocessOpenAPI(OpenAPI openAPI) {
        super.preprocessOpenAPI(openAPI);

        if(!additionalProperties.containsKey(TITLE)) {
            // From the title, compute a reasonable name for the package and the API
            String title = openAPI.getInfo().getTitle();

            // Drop any API suffix
            if (title != null) {
                title = title.trim().replace(" ", "-");
                if (title.toUpperCase(Locale.ROOT).endsWith("API")) {
                    title = title.substring(0, title.length() - 3);
                }

                this.title = org.openapitools.codegen.utils.StringUtils.camelize(sanitizeName(title), true);
            }
            additionalProperties.put(TITLE, this.title);
        }

        if(!additionalProperties.containsKey(SERVER_PORT)) {
            URL url = URLPathUtils.getServerURL(openAPI);
            this.additionalProperties.put(SERVER_PORT, URLPathUtils.getPort(url, 8080));
        }

        if (openAPI.getPaths() != null) {
            for (String pathname : openAPI.getPaths().keySet()) {
                PathItem path = openAPI.getPaths().get(pathname);
                if (path.readOperations() != null) {
                    for (Operation operation : path.readOperations()) {
                        if (operation.getTags() != null) {
                            List<Map<String, String>> tags = new ArrayList<Map<String, String>>();
                            for (String tag : operation.getTags()) {
                                Map<String, String> value = new HashMap<String, String>();
                                value.put("tag", tag);
                                value.put("hasMore", "true");
                                tags.add(value);
                            }
                            if (tags.size() > 0) {
                                tags.get(tags.size() - 1).remove("hasMore");
                            }
                            if (operation.getTags().size() > 0) {
                                String tag = operation.getTags().get(0);
                                operation.setTags(Arrays.asList(tag));
                            }
                            operation.addExtension("x-tags", tags);
                        }
                    }
                }
            }
        }
    }

}