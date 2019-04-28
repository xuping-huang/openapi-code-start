package com.huangxuping.codegen;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.openapitools.codegen.*;
import org.openapitools.codegen.utils.URLPathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

public class NodeJSServerGenerator extends DefaultCodegen implements CodegenConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeJSServerGenerator.class);
    protected String implFolder = "service";
    public static final String SERVER_PORT = "serverPort";

    protected String apiVersion = "1.0.0";
    protected String projectName = "openapi-server";
    protected String defaultServerPort = "8080";
    public static final String NEED_SWAGGER_DOC = "swgdoc";
    public static final String DATABASE_TYPE = "db";
    public static final String M2M_TOKEN = "m2mToken";
    public static final String USER_TOKEN = "userToken";

    protected String exportedName;
    private GeneratorUtil generatorUtil = new GeneratorUtil();

    public NodeJSServerGenerator() {
        super();

        // set the output folder here
        outputFolder = "generated-code/j-nodejs";

        /*
         * Models.  You can write model files using the modelTemplateFiles map.
         * if you want to create one template for file, you can do so here.
         * for multiple files for model, just put another entry in the `modelTemplateFiles` with
         * a different extension
         */
        modelTemplateFiles.put(
                "model.repo.mustache",
                ".model.js"
        );
        modelTestTemplateFiles.put(
                "unit.model.mustache",   // the template to use
                ".model.spec.js");       // the extension for each file to write

        /*
         * Api classes.  You can write classes for each Api file with the apiTemplateFiles map.
         * as with models, add multiple entries with different extensions for multiple files per
         * class
         */
        apiTemplateFiles.put(
                "controller.operation.mustache",
                ".js");
        apiTemplateFiles.put(
                "service.operation.mustache",   // the template to use
                ".js");       // the extension for each file to write
        apiTestTemplateFiles.put(
                "e2e.operation.mustache",   // the template to use
                ".test.js");       // the extension for each file to write
        apiTestTemplateFiles.put(
                "unit.controller.mustache",   // the template to use
                ".spec.js");       // the extension for each file to write
        apiTestTemplateFiles.put(
                "unit.service.mustache",   // the template to use
                ".spec.js");       // the extension for each file to write

        /*
         * Template Location.  This is the location which templates will be read from.  The generator
         * will use the resource stream to attempt to read the templates.
         */
        embeddedTemplateDir = templateDir = "j-nodejs";

        /*
         * Reserved words.  Override this with reserved words specific to your language
         */
        setReservedWordsLowerCase(
                Arrays.asList(
                        "break", "case", "class", "catch", "const", "continue", "debugger",
                        "default", "delete", "do", "else", "export", "extends", "finally",
                        "for", "function", "if", "import", "in", "instanceof", "let", "new",
                        "return", "super", "switch", "this", "throw", "try", "typeof", "var",
                        "void", "while", "with", "yield")
        );

        /*
         * Additional Properties.  These values can be passed to the templates and
         * are available in models, apis, and supporting files
         */
        additionalProperties.put("apiVersion", apiVersion);
        additionalProperties.put("implFolder", implFolder);

        supportingFiles.add(new SupportingFile("model.index.mustache", ("src/model").replace(".", File.separator), "index.js"));
        supportingFiles.add(new SupportingFile("route.operation.mustache", "./src", "routes.js"));
        supportingFiles.add(new SupportingFile("model.lookup.mustache", ("src/lib").replace(".", File.separator), "lookup-load.js"));
        supportingFiles.add(new SupportingFile("db.tools.mustache", "./scripts", "tools.js"));
        supportingFiles.add(new SupportingFile("db.create-db.mustache", "./scripts", "create-db.sql"));
        supportingFiles.add(new SupportingFile("test.data.mustache", "./test/lib", "test-data.js"));
        supportingFiles.add(new SupportingFile("e2e.index.mustache", "./test/e2e", "index.e2e.js"));


        cliOptions.add(new CliOption(SERVER_PORT,
                "TCP port to listen on."));
        cliOptions.add(new CliOption(NEED_SWAGGER_DOC,
                "need swagger doc comment."));
        cliOptions.add(new CliOption(DATABASE_TYPE,
                "database type."));
        cliOptions.add(new CliOption(M2M_TOKEN,
                "whether need machine token"));
        cliOptions.add(new CliOption(USER_TOKEN,
                "whether need user token"));

        this.setSkipOverwrite(false);
    }

    @Override
    public String apiPackage() {
        return "src/controller";
    }

    /**
     * Configures the type of generator.
     *
     * @return the CodegenType for this generator
     * @see org.openapitools.codegen.CodegenType
     */
    @Override
    public CodegenType getTag() {
        return CodegenType.SERVER;
    }

    /**
     * Configures a friendly name for the generator.  This will be used by the generator
     * to select the library with the -g flag.
     *
     * @return the friendly name for the generator
     */
    @Override
    public String getName() {
        return "j-nodejs";
    }

    /**
     * Returns human-friendly help for the generator.  Provide the consumer with help
     * tips, parameters here
     *
     * @return A string value for the help message
     */
    @Override
    public String getHelp() {
        return "Generates a nodejs server library using the swagger-tools project.  By default, " +
                "it will also generate service classes--which you can disable with the `-Dnoservice` environment variable.";
    }

    @Override
    public String toApiName(String name) {
        if (name.length() == 0) {
            return "DefaultController";
        }
        return StringUtils.lowerCase(name);
    }

    @Override
    public String toApiFilename(String name) {
        return "api." + toApiName(name);
    }

    @Override
    public String toApiTestFilename(String name) {
        return this.toApiFilename(name);
    }

    @Override
    public String apiFilename(String templateName, String tag) {
        String result = super.apiFilename(templateName, tag);

        if (templateName.equals("service.operation.mustache")) {
            String stringToMatch = "controller" + File.separator;
            String replacement =  implFolder + File.separator;
            result = result.replace(stringToMatch, replacement).replace("api.","service.");
        }
        return result;
    }
    @Override
    public String apiTestFilename(String templateName, String tag) {
        String result = super.apiTestFilename(templateName, tag);

        if (templateName.equals("e2e.operation.mustache")) {
            String stringToMatch = "controller" + File.separator;
            String replacement =  implFolder + File.separator + "../../e2e" + File.separator;
            result = result.replace(stringToMatch, replacement).replace("api.","");
        }

        if (templateName.equalsIgnoreCase(("unit.service.mustache"))) {
            String stringToMatch = "controller" + File.separator;
            String replacement =  implFolder + File.separator ;
            result = result.replace(stringToMatch, replacement).replace("api.","service.");
        }
        return result;
    }
    protected String initialMediumUnderlineName(String name){
        StringBuilder sb = new StringBuilder();

        for ( int i = 0 ; i < name.length() ; i++){
            if ( CharUtils.isAsciiAlphaUpper(name.charAt(i) ) ){
                if ( i > 0 ){
                    sb.append('-');
                }
                sb.append(CharUtils.toChar((char)(name.charAt(i)+32)));
            }else {
                sb.append(name.charAt(i));
            }
        }
        return sb.toString();
    }

    @Override
    public String apiTestFileFolder() {
        return (this.outputFolder + "/test/unit/controller").replace('/', File.separatorChar);
    }

    @Override
    public String modelTestFileFolder() {
        return (this.outputFolder + "/test/unit/model").replace('/', File.separatorChar);
    }
    @Override
    public String toModelTestFilename(String name) {
        return initialMediumUnderlineName(name);
    }

    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
        @SuppressWarnings("unchecked")
        Map<String, Object> objectMap = (Map<String, Object>) objs.get("operations");
        @SuppressWarnings("unchecked")
        List<CodegenOperation> operations = (List<CodegenOperation>) objectMap.get("operation");
        for (CodegenOperation operation : operations) {
            operation.httpMethod = operation.httpMethod.toLowerCase();
            generatorUtil.handleOperation(operation);
        }
        return objs;
    }

    @Override
    public String toModelFilename(String name) {
        String ret = initialMediumUnderlineName(name);
        return ret;
    }

    private String implFileFolder(String output) {
        return outputFolder + File.separator + output + File.separator + apiPackage().replace('.', File.separatorChar);
    }

    /**
     * Escapes a reserved word as defined in the `reservedWords` array. Handle escaping
     * those terms here.  This logic is only called if a variable matches the reserved words
     *
     * @return the escaped term
     */
    @Override
    public String escapeReservedWord(String name) {
        if (this.reservedWordsMappings().containsKey(name)) {
            return this.reservedWordsMappings().get(name);
        }
        return "_" + name;
    }

    /**
     * Location to write api files.  You can use the apiPackage() as defined when the class is
     * instantiated
     */
    @Override
    public String apiFileFolder() {
        return outputFolder + File.separator + apiPackage().replace('.', File.separatorChar);
    }
    @Override
    public String modelPackage() {
        return "src/model";
    }

    private void analyzeOperationTest(CodegenOperation operation) {
        List<PostmanTest> tests = OpenApiTestHelper.analyzeOperationTest(operation);
        operation.vendorExtensions.put("x-valid-methods", tests);
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



    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> getOperations(Map<String, Object> objs) {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> apiInfo = (Map<String, Object>) objs.get("apiInfo");
        List<Map<String, Object>> apis = (List<Map<String, Object>>) apiInfo.get("apis");
        for (Map<String, Object> api : apis) {
            Map<String, Object> ops = (Map<String, Object>) api.get("operations");
            List<CodegenOperation> codeOps = (List<CodegenOperation>) ops.get("operation");
            codeOps.sort(Comparator.comparing(e -> e.path));

            result.add(ops);
        }
        return result;
    }

    private static List<Map<String, Object>> sortOperationsByPath(List<CodegenOperation> ops) {
        Multimap<String, CodegenOperation> opsByPath = ArrayListMultimap.create();

        for (CodegenOperation op : ops) {
            opsByPath.put(op.path, op);
        }

        List<Map<String, Object>> opsByPathList = new ArrayList<Map<String, Object>>();
        for (Entry<String, Collection<CodegenOperation>> entry : opsByPath.asMap().entrySet()) {
            Map<String, Object> opsByPathEntry = new HashMap<String, Object>();
            opsByPathList.add(opsByPathEntry);
            opsByPathEntry.put("path", entry.getKey());
            opsByPathEntry.put("operation", entry.getValue());
            List<CodegenOperation> operationsForThisPath = Lists.newArrayList(entry.getValue());
            operationsForThisPath.get(operationsForThisPath.size() - 1).hasMore = false;
            if (opsByPathList.size() < opsByPath.asMap().size()) {
                opsByPathEntry.put("hasMore", "true");
            }
        }

        return opsByPathList;
    }

    @Override
    public void processOpts() {
        super.processOpts();

        StringBuilder message = new StringBuilder();
        message.append(System.lineSeparator()).append(System.lineSeparator())
                .append("=======================================================================================")
                .append(System.lineSeparator())
                .append("Currently, Node.js server doesn't work as its dependency doesn't support OpenAPI Spec3.")
                .append(System.lineSeparator())
                .append("For further details, see https://github.com/OpenAPITools/openapi-generator/issues/34")
                .append(System.lineSeparator())
                .append("=======================================================================================")
                .append(System.lineSeparator()).append(System.lineSeparator());
        LOGGER.warn(message.toString());
        /*
         * Supporting Files.  You can write single files for the generator with the
         * entire object tree available.  If the input file has a suffix of `.mustache
         * it will be processed by the template engine.  Otherwise, it will be copied
         */
        // supportingFiles.add(new SupportingFile("e2e.operation.mustache",
        //   "controllers",
        //   "controller.js")
        // );
//        writeOptional(outputFolder, new SupportingFile("index.mustache", "", "index.js"));
//        writeOptional(outputFolder, new SupportingFile("package.mustache", "", "package.json"));
//        if (System.getProperty("noservice") == null) {
//            apiTemplateFiles.put(
//                    "service.mustache",   // the template to use
//                    "Service.js");       // the extension for each file to write
//        }
    }

    @Override
    public void preprocessOpenAPI(OpenAPI openAPI) {

        URL url = URLPathUtils.getServerURL(openAPI);
        String host =  URLPathUtils.getProtocolAndHost(url);
        String port = URLPathUtils.getPort(url, defaultServerPort) ;
        String basePath = url.getPath();

        if (!StringUtils.isEmpty(host)) {
            String[] parts = host.split(":");
            if (parts.length > 1) {
                port = parts[1];
            }
        } else {
            // host is empty, default to https://localhost
            host = "http://localhost";
            LOGGER.warn("'host' in the specification is empty or undefined. Default to http://localhost.");
        }

        if (additionalProperties.containsKey(SERVER_PORT)) {
            port = additionalProperties.get(SERVER_PORT).toString();
        }
        this.additionalProperties.put(SERVER_PORT, port);

        boolean needSwaggerDoc = false;
        if (additionalProperties.containsKey(NEED_SWAGGER_DOC)) {
            needSwaggerDoc = Boolean.parseBoolean(additionalProperties.get(NEED_SWAGGER_DOC).toString());
        }
        this.additionalProperties.put(NEED_SWAGGER_DOC, needSwaggerDoc);

        boolean needM2MToken = false;
        if (additionalProperties.containsKey(M2M_TOKEN)) {
            needM2MToken = Boolean.parseBoolean(additionalProperties.get(M2M_TOKEN).toString());
        }
        this.additionalProperties.put(M2M_TOKEN, needM2MToken);

        boolean needUserToken = false;
        if (additionalProperties.containsKey(USER_TOKEN)) {
            needUserToken = Boolean.parseBoolean(additionalProperties.get(USER_TOKEN).toString());
        }
        this.additionalProperties.put(USER_TOKEN, needUserToken);

        if (additionalProperties.containsKey(DATABASE_TYPE)) {
            String dbType = additionalProperties.get(DATABASE_TYPE).toString().toLowerCase().trim();
            switch (dbType) {
                case "dynamodb" :
                case "dynamo" :
                    this.additionalProperties.put("db_isDynamoDB", true);
                    break;
                case "jsondb" :
                case "json" :
                    this.additionalProperties.put("db_isJsonDB", true);
                    break;
                case "sqlite" :
                    this.additionalProperties.put("orm_isSequelize", true);
                    this.additionalProperties.put("db_isSqlite", true);
                    break;
                case "mysql" :
                    this.additionalProperties.put("orm_isSequelize", true);
                    this.additionalProperties.put("db_isMysql", true);
                    break;
                case "mariadb" :
                    this.additionalProperties.put("orm_isSequelize", true);
                    this.additionalProperties.put("db_isMariadb", true);
                    break;
                case "postgres" :
                    this.additionalProperties.put("orm_isSequelize", true);
                    this.additionalProperties.put("db_isPostgres", true);
                    break;
                case "mssql" :
                    this.additionalProperties.put("orm_isSequelize", true);
                    this.additionalProperties.put("db_isMssql", true);
                    break;
                case "neo4j" :
                    this.additionalProperties.put("db_isNeo4j", true);
                    break;
                case "mongodb" :
                case "mongo" :
                    this.additionalProperties.put("db_isMongoDB", true);
                    break;
            }
        }

        if (openAPI.getInfo() != null) {
            Info info = openAPI.getInfo();
            if (info.getTitle() != null) {
                // when info.title is defined, use it for projectName
                // used in package.json
                projectName = info.getTitle()
                        .replaceAll("[^a-zA-Z0-9]", "-")
                        .replaceAll("^[-]*", "")
                        .replaceAll("[-]*$", "")
                        .replaceAll("[-]{2,}", "-")
                        .toLowerCase(Locale.ROOT);
                this.additionalProperties.put("projectName", projectName);
            }
        }

        // need vendor extensions for x-swagger-router-controller
        Paths paths = openAPI.getPaths();
        if (paths != null) {
            for (String pathname : paths.keySet()) {
                PathItem path = paths.get(pathname);
                Map<HttpMethod, Operation> operationMap = path.readOperationsMap();
                if (operationMap != null) {
                    for (HttpMethod method : operationMap.keySet()) {
                        Operation operation = operationMap.get(method);
                        String tag = "default";
                        if (operation.getTags() != null && operation.getTags().size() > 0) {
                            tag = toApiName(operation.getTags().get(0));
                        }
                        if (operation.getOperationId() == null) {
                            operation.setOperationId(getOrGenerateOperationId(operation, pathname, method.toString()));
                        }
                        if (operation.getExtensions() == null ||
                                operation.getExtensions().get("x-swagger-router-controller") == null) {
                            operation.addExtension("x-swagger-router-controller", sanitizeTag(tag));
                        }
                    }
                }
            }
        }
    }

    @Override
    public Map<String, Object> postProcessModels(Map<String, Object> objs) {
//        Iterator entrys = objs.entrySet().iterator();
//        while(entrys.hasNext()) {
//            Entry<String, Object> entry = (Entry)entrys.next();
//            List<Map<String, Object>> models = (List)entry.getValue();
//            Iterator var8 = models.iterator();
//
//            while(var8.hasNext()) {
//                Map<String, Object> mo = (Map)var8.next();
//                CodegenModel cm = (CodegenModel)mo.get("model");
//                for (CodegenProperty property : cm.allVars) {
//                    if (property.name.equalsIgnoreCase("id")) {
//                        property.vendorExtensions.put("x-is-key", true);
//                    }
//                }
//            }
//        }
        return objs;
    }

    @Override
    public Map<String, Object> postProcessSupportingFileData(Map<String, Object> objs) {
//        generateYAMLSpecFile(objs);

        for (Map<String, Object> operations : getOperations(objs)) {
            @SuppressWarnings("unchecked")
            List<CodegenOperation> ops = (List<CodegenOperation>) operations.get("operation");

            List<Map<String, Object>> opsByPathList = sortOperationsByPath(ops);
            operations.put("operationsByPath", opsByPathList);
        }
        return super.postProcessSupportingFileData(objs);
    }

    @Override
    public String removeNonNameElementToCamelCase(String name) {
        return removeNonNameElementToCamelCase(name, "[-:;#]");
    }

    @Override
    public String escapeUnsafeCharacters(String input) {
        return input.replace("*/", "*_/").replace("/*", "/_*");
    }

    @Override
    public String escapeQuotationMark(String input) {
        // remove " to avoid code injection
        return input.replace("\"", "");
    }
}
