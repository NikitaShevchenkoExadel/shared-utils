package com.starexchangealliance.shared.utils.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.starexchangealliance.shared.utils.tests.api.framework.model.BasePresentBodyRequest;
import com.starexchangealliance.shared.utils.tests.api.framework.model.Block;
import com.starexchangealliance.shared.utils.tests.api.framework.model.HttpMethodBlock;
import com.starexchangealliance.shared.utils.tests.api.framework.model.Operation;
import com.starexchangealliance.shared.utils.tests.api.framework.model.Response;
import com.starexchangealliance.shared.utils.tests.api.framework.model.impl.Config;
import com.starexchangealliance.shared.utils.tests.api.framework.model.impl.auth.Authentication;
import com.starexchangealliance.shared.utils.tests.api.framework.model.impl.database.Dump;
import com.starexchangealliance.shared.utils.tests.api.framework.model.impl.execute.ExecuteOperation;
import com.starexchangealliance.shared.utils.tests.api.framework.model.impl.instructruction.Break;
import com.starexchangealliance.shared.utils.tests.api.framework.model.impl.mock.When;
import com.starexchangealliance.shared.utils.tests.api.framework.model.impl.variable.Variable;
import com.starexchangealliance.shared.utils.tests.api.framework.parser.ConfigCollector;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.AssumptionViolatedException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.starexchangealliance.shared.utils.tests.PostgresDatabaseHelper.apply;
import static com.starexchangealliance.shared.utils.tests.PostgresDatabaseHelper.clearDatabaseQuery;
import static com.starexchangealliance.shared.utils.tests.PostgresDatabaseHelper.dropDatabaseQuery;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;


@Slf4j
public abstract class BaseTest {

    private static volatile boolean wasExecuted;

    private final boolean stopOnError;

    @Autowired
    FrameworkTestParameters frameworkTestParameters;

    @Autowired
    WebApplicationContext ctx;

    @Autowired
    DatabaseConfig databaseConfig;

    @Autowired
    DataSource dataSource;

    @Autowired
    AutowireCapableBeanFactory beanFactory;

    private MockMvc api;
    private File file;
    private ConfigCollector.MappingResult result;

    @SuppressWarnings({"unused"})
    public BaseTest(String ignore, Object file, Object result, boolean stopOnError) {
        this.file = (File) file;
        this.result = (ConfigCollector.MappingResult) result;
        this.stopOnError = stopOnError;
    }

    protected static Collection<Object[]> data(File folderConfig, Pattern filterDir) {
        ConfigCollector.Result result = new ConfigCollector(folderConfig, filterDir).collect();

        return filterOnlyThis(result.get()).stream().map(e -> {
            String shortPath = e.getKey().getPath().replace(folderConfig.toString(), "");
            return new Object[]{shortPath, e.getKey(), e.getValue()};
        }).collect(Collectors.toList());
    }

    private static Set<Map.Entry<File, ConfigCollector.MappingResult>> filterOnlyThis(Map<File, ConfigCollector.MappingResult> original) {
        Set<Map.Entry<File, ConfigCollector.MappingResult>> onlyThis =
                original.entrySet().stream().filter(m -> {
                    ConfigCollector.MappingResult v = m.getValue();
                    return v.config != null && v.config.getActive() && v.config.getOnlyThis();
                }).collect(Collectors.toSet());

        return onlyThis.isEmpty() ? original.entrySet() : onlyThis;
    }

    @Before
    public void setUpBefore() throws Exception {
        //this is where the magic happens, we actually do "by hand" what the spring runner would do for us,
        // read the JavaDoc for the class bellow to know exactly what it does, the method names are quite accurate though
        new TestContextManager(getClass()).prepareTestInstance(this);

        this.api = MockMvcBuilders.webAppContextSetup(ctx).build();

        String url = databaseConfig.getUrl();
        if (!url.contains("localhost")) {
            throw new RuntimeException("Test framework should apply migrations ONLY for local database. " +
                    "URL which has been used is " + url);
        }

        if (result.config != null) {
            Config config = result.config;
            if (config.getActive()) {
                if (!wasExecuted){
                    new DatabaseMigrationRunner(dataSource, databaseConfig).execute();
                    wasExecuted = true;
                }
            } else {
                throw new AssumptionViolatedException(config.getDescription());
            }
        }
    }

    @Test
    public void test() throws Throwable {
        if (result.config != null) {
            Config cfg = result.config;
            log.info("..............................................");
            log.info("Description:" + cfg.getDescription());

            cleanDirectoryRedundantActualFiles(file);
            migrateDatabase(cfg);
            new ConfigRunner(file, cfg).run();

        } else {
            throw result.exception;
        }
    }

    private void cleanDirectoryRedundantActualFiles(File file) {
        FileUtils.listFiles(file.getParentFile(), null, true).forEach(e -> {
            if (e.getName().contains("_actual.json")) {
                FileUtils.deleteQuietly(e);
            }
        });
    }

    private void migrateDatabase(Config cfg) throws Exception {
        List<String> patches = cfg.getPatch().transform(patchesNames -> {
            String[] names = patchesNames.split(",");

            for (int index = 0; index < names.length; index++) {
                String name = names[index];
                names[index] = name.endsWith(".sql") ? name : name + ".sql";
            }

            return Arrays.asList(names);
        }).or(Collections.singletonList(frameworkTestParameters.getFileNamePatchDefault()));

        List<SQLSource> patchesFiles = new ArrayList<>();
        patchesFiles.add(clearDatabaseQuery(dataSource));

        for (String each : patches) {
            patchesFiles.add(new FileSource(new File(frameworkTestParameters.getFolderPatches(), each)));
        }

        apply(dataSource, patchesFiles);
    }

    private static class StopSignal extends RuntimeException {
    }

    private class DatabaseMigrationRunner {

        private final DataSource dataSource;
        private final DatabaseConfig databaseConfig;

        DatabaseMigrationRunner(DataSource dataSource, DatabaseConfig databaseConfig) {
            this.dataSource = dataSource;
            this.databaseConfig = databaseConfig;
        }

        synchronized void execute() throws Exception {
            try {
                migrate();
            } catch (Exception e) {
                ListSource source = dropDatabaseQuery(dataSource);
                apply(dataSource, Collections.singletonList(source));
                migrate();
            }
        }

        private void migrate() {
            String description = String.format("Testing tool URL[%s] USER[%s]",
                    databaseConfig.getUrl(), databaseConfig.getUser());
            BaseTest.this.migrate(dataSource, description);
        }
    }

    protected abstract void migrate(DataSource dataSource,String message);

    private class ConfigRunner {

        private final Pattern routePattern = Pattern.compile("#\\{(.*?)\\}", Pattern.DOTALL);

        private final String path;
        private final Config cfg;
        private final AtomicInteger pos = new AtomicInteger();
        private final Context ctx = new Context();

        private Throwable error = null;

        ConfigRunner(File file, Config cfg) {
            this.path = file.getAbsolutePath().replace(frameworkTestParameters.getFolderScenarios().getAbsolutePath(), "");
            this.cfg = cfg;
        }

        void run() throws Throwable {
            try {
                scenario(cfg.getBlocks(), null);

                if (error != null) {
                    throw error;
                }

            } catch (StopSignal ignore) {
            }
        }

        private void scenario(List<Block> blocks, String token) throws Throwable {
            for (Block each : blocks) {
                try {
                    block(each, token);
                } catch (StopSignal e) {
                    throw e;

                } catch (Throwable e) {
                    if (error == null) {
                        error = e;
                    }

                    if (stopOnError) {
                        throw e;
                    }
                }
            }
        }

        private void block(Block each, String token) throws Throwable {
            log.info("..............................................");

            if (StringUtils.isNoneBlank(each.getComment())) {
                log.info(each.getComment());
            }

            if (each instanceof Response && each instanceof HttpMethodBlock) {
                pos.incrementAndGet();

                Response asResponse = (Response) each;
                HttpMethodBlock asMethod = (HttpMethodBlock) each;

                int expectedCode = asResponse.getCode();
                Map<String, String> expectedHeaders = asResponse.getResponseHeaders();
                Optional<File> maybeExpectedBody = Optional.ofNullable(asResponse.getResponseFile().orNull());

                WsResponse response = exchange(asMethod, token);

                int actualCode = response.code;
                Map<String, String> actualHeaders = response.headers;
                String actualBody = response.content;

                ctx.setBody(actualBody);

                TestValidator v = new TestValidator();
                v.validateAndSave(expectedCode, actualCode).
                        validateAndSave(expectedHeaders, actualHeaders).
                        validateAndSave(maybeExpectedBody, actualBody);

                v.rethrowOnErrors();

            } else if (each instanceof Dump) {
                pos.incrementAndGet();

                Dump dump = (Dump) each;

                List<String> sql = dump.getSql().stream().map(this::inject).collect(Collectors.toList());

                log.info(String.format("%2s. %-8s", String.valueOf(pos.get()), "Dump"));
                for (String e : sql) {
                    log.info(String.format("%10s %-100s", "", e));
                }

                String expected = FileUtils.readFileToString(dump.getFile().get(), "UTF-8");

                ObjectMapper mapper = new ObjectMapper();
                String actual = mapper.writeValueAsString(new SQLReader(dataSource).read(sql));

                ctx.setBody(actual);

                try {
                    TreeComparator.compare(expected, actual);
                } catch (TreeComparator.ComparisonException e) {
                    save(actual);
                    throw e;
                }

            } else if (each instanceof Authentication) {
                Authentication auth = (Authentication) each;
                Preconditions.checkArgument(auth.getCredentials().isPresent(),
                        "Credentials for authentication not provided");

                Credentials credentials = new Credentials(auth.getCredentials().get());
                log.info(String.format("%15s", "Authentication [" + credentials.credentialsFile.getName() + "]"));

                scenario(auth.getBlocks(), credentials.authenticate());

            } else if (each instanceof Variable) {
                Variable var = (Variable) each;

                log.info(String.format("%2s. %-8s %40s", String.valueOf(pos.get()),
                        "Jpath variable", var.getName() + " -> " + var.getPath()));
                try {
                    String result = ctx.setJPath(var.getName(), var.getPath());
                    log.info(String.format("%5s [Value in contextOf]=%s", "", result));
                } catch (Exception e) {
                    log.info("Failed [variable] " + var.getName() + " [path] " + var.getPath());
                    throw e;
                }
            } else if (each instanceof Break) {
                log.info(String.format("%2s. %-8s", String.valueOf(pos.get()), "BREAK"));

                throw new StopSignal();
            } else if (each instanceof When) {
                When when = (When) each;
            } else if (each instanceof ExecuteOperation) {
                pos.incrementAndGet();

                ExecuteOperation operation = (ExecuteOperation) each;
                log.info(String.format("%2s. %-8s", String.valueOf(pos.get()),
                        "ExecuteOperation on " + operation.getClassName()));

                Operation op = (Operation) Class.forName(operation.getClassName()).newInstance();
                beanFactory.autowireBean(op);

                Object result = op.applyOperation();

                Optional<String> expectedBody = Optional.ofNullable(operation.getFile().orNull()).map(file -> {
                    try {
                        return FileUtils.readFileToString(file, "UTF-8");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

                if (expectedBody.isPresent()) {
                    String actual = result instanceof CharSequence ? result.toString() :
                            new ObjectMapper().writeValueAsString(result);
                    String expected = expectedBody.get();

                    try {
                        TreeComparator.compare(expected, actual);
                    } catch (TreeComparator.ComparisonException e) {
                        save(actual);
                        throw e;
                    }
                }
            }
        }

        private WsResponse exchange(HttpMethodBlock method, String token) throws Exception {
            Map<String, String> headers = inject(method.getRequestHeaders());
            if (token != null) {
                headers.put("Authorization", "Bearer " + token);
            }

            MockHttpServletRequestBuilder request = createRequest(method);
            request = addRequestHeaders(request, headers);
            boolean isJson = false;
            if (headers.containsKey("Content-Type")) {
                String type = headers.get("Content-Type");
                isJson = type.equalsIgnoreCase("application/json");
            }
            request = addRequestBody(request, method, isJson);

            MockHttpServletResponse result = api.perform(request).andReturn().getResponse();
            return new WsResponse(result.getStatus(), extractHeaders(result), result.getContentAsString());
        }

        private Map<String, String> extractHeaders(MockHttpServletResponse response) {
            Map<String, String> result = new LinkedHashMap<>();
            for (String each : response.getHeaderNames()) {
                result.put(each, String.valueOf(response.getHeaderValue(each)));
            }
            return result;
        }

        private MockHttpServletRequestBuilder createRequest(HttpMethodBlock method) {
            String url = inject(method.getUrl());
            String name = method.getName();

            log.info(String.format("%2s. %-8s %-40s", String.valueOf(pos.get()), name, url));

            if ("GET".equalsIgnoreCase(name)) {
                return get(url);
            } else if ("POST".equalsIgnoreCase(name)) {
                BasePresentBodyRequest body = (BasePresentBodyRequest) method;

                if (body.getMultipart() || !body.getFiles().isEmpty()) {
                    return multipart(url); // TODO check works fine
                } else {
                    return post(url);
                }
            } else if ("PUT".equalsIgnoreCase(name)) {
                return put(url);
            } else if ("DELETE".equalsIgnoreCase(name)) {
                return delete(url);
            } else if ("PATCH".equalsIgnoreCase(name)) {
                return patch(url);
            } else {
                throw new IllegalArgumentException("Unknown method " + name);
            }
        }

        private MockHttpServletRequestBuilder addRequestHeaders(MockHttpServletRequestBuilder request,
                                                                Map<String, String> headers) {
            if (!headers.isEmpty()) {
                log.info(String.format("%25s", "[header]"));
                for (Map.Entry<String, String> each : headers.entrySet()) {
                    log.info(String.format("%20s=%s", each.getKey(), each.getValue()));
                }
            }

            for (String key : headers.keySet()) {
                String value = headers.get(key);
                request = request.header(key, value);
            }
            return request;
        }

        private MockHttpServletRequestBuilder addRequestBody(MockHttpServletRequestBuilder request,
                                                             HttpMethodBlock method, boolean isJson) throws IOException {
            if (method instanceof BasePresentBodyRequest) {
                BasePresentBodyRequest b = (BasePresentBodyRequest) method;

                Map<String, String> newMap = new LinkedHashMap<>();
                for (Map.Entry<String, String> entry : b.getParams().entrySet()) {
                    newMap.put(inject(entry.getKey()), inject(entry.getValue()));
                }

                if (!newMap.isEmpty()) {
                    log.info(String.format("%24s", "[body]"));
                    for (Map.Entry<String, String> each : newMap.entrySet()) {
                        log.info(String.format("%20s=%s", each.getKey(), each.getValue()));
                    }
                }

                if (isJson) {
                    String content = new ObjectMapper().writeValueAsString(newMap);
                    request.content(content);
                } else {
                    for (Map.Entry<String, String> entry : newMap.entrySet()) {
                        request = request.param(entry.getKey(), entry.getValue());
                    }
                }

                for (Map.Entry<String, String> entry : newMap.entrySet()) {
                    request = request.param(entry.getKey(), entry.getValue());
                }

                if (!b.getFiles().isEmpty()) {
                    for (Map.Entry<String, File> entry : b.getFiles().entrySet()) {
                        request = ((MockMultipartHttpServletRequestBuilder) request).
                                file(entry.getKey(), FileUtils.readFileToByteArray(entry.getValue()));
                    }
                }

                return request;
            }
            return request;
        }

        private void save(String actual) throws IOException {
            int position = pos.get();

            File folder = new File(frameworkTestParameters.getFolderScenarios(), path.replace("config.xml", ""));

            FileUtils.writeStringToFile(new File(folder,
                    String.format("action_%s_actual.json", position)), prettify(actual), "UTF-8");
        }

        private String prettify(String actual) {
            try {
                if (actual.startsWith("{") || actual.startsWith("[")) {
                    ObjectMapper mapper = new ObjectMapper();
                    Object json = mapper.readValue(actual, Object.class);
                    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
                }
            } catch (Exception ignore) {
            }
            return actual;
        }

        String inject(String original) {
            return recursiveInject(original);
        }

        String recursiveInject(String original) {
            Matcher m = routePattern.matcher(original);
            if (m.find()) {
                return original.substring(0, m.start()) + ctx.get(m.group(1)) + original.substring(m.end());
            } else {
                return original;
            }
        }

        Map<String, String> inject(Map<String, String> input) {
            Map<String, String> result = new LinkedHashMap<>();
            for (String key : input.keySet()) {
                result.put(key, inject(input.get(key)));
            }
            return result;
        }

        private class TestValidator {
            private static final int LIMIT = 80;

            private List<String> result = new ArrayList<>();

            TestValidator validateAndSave(int expectedCode,
                                          int actualCode) throws Exception {
                try {
                    Assert.assertEquals(expectedCode, actualCode);
                } catch (Throwable e) {
                    e.printStackTrace();
                    result.add(" HTTP CODE should be [" + expectedCode + "] but was [" + actualCode + "]");
                    save(String.valueOf(actualCode));
                }
                return this;
            }

            TestValidator validateAndSave(Map<String, String> expectedHeaders,
                                          Map<String, String> actualHeaders) throws Exception {
                try {
                    for (Map.Entry<String, String> entry : expectedHeaders.entrySet()) {
                        String value = actualHeaders.get(entry.getKey());
                        Assert.assertEquals(entry.getValue(), value);
                    }
                } catch (Throwable e) {
                    String expected = stingify(expectedHeaders);
                    String actual = stingify(actualHeaders);

                    result.add(" HTTP HEADERS should be [" + cut(expected) + "] but was [" + cut(actual) + "]");
                    save(actual);
                }
                return this;
            }

            private void validateAndSave(Optional<File> expectedBodyFile, String actualBody) throws Exception {
                Optional<String> expectedBody = expectedBodyFile.map(file -> {
                    try {
                        return FileUtils.readFileToString(file, "UTF-8");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

                try {
                    expectedBody.ifPresent(s -> TreeComparator.compare(s, actualBody));
                } catch (TreeComparator.ComparisonException e) {
                    result.add(" HTTP BODY should be [" + cut(expectedBody.get()) + "] but was [" + cut(actualBody) + "]");
                    save(actualBody);
                }
            }

            void rethrowOnErrors() {
                if (result.size() > 0) {
                    throw new RuntimeException("Errors:\n " + String.join(" \n", result));
                }
            }

            private String stingify(Map<String, String> map) {
                StringBuilder o = new StringBuilder();
                for (Map.Entry<String, String> each : map.entrySet()) {
                    o.append(each.getKey()).append("=").append(each.getValue());
                }
                return o.toString();
            }

            private String cut(String s) {
                return StringUtils.abbreviate(s, LIMIT);
            }

        }

        private class WsResponse {
            private int code;
            private Map<String, String> headers;
            private String content;

            WsResponse(int code,
                       Map<String, String> headers,
                       String content) {
                this.code = code;
                this.headers = headers;
                this.content = content;
            }
        }
    }

    private class Credentials {

        final File credentialsFile;

        final String email;
        final String password;

        Credentials(String credentials) throws IOException {
            credentials = credentials.endsWith(".json") ? credentials : credentials + ".json";

            this.credentialsFile = new File(frameworkTestParameters.getFolderCredentials(), credentials);
            Preconditions.checkArgument(credentialsFile.exists(),
                    "File with credentials for authentication not found by path:" + credentialsFile);

            DocumentContext ctx = JsonPath.parse(FileUtils.readFileToString(credentialsFile, "UTF-8"));

            this.email = ctx.read("$.email").toString();
            this.password = ctx.read("$.password").toString();
        }

        String authenticate() throws Exception {
            MockHttpServletResponse result = null;
            try {
                result = api.perform(post("/api/v1/authentication/token").
                        param("email", this.email).
                        param("password", this.password)).
                        andReturn().getResponse();

                return JsonPath.parse(result.getContentAsString()).read("$.token").toString();

            } catch (Throwable e) {
                StringBuilder o = new StringBuilder();
                o.append("Unable to authenticate with credentials").append("\n");
                o.append("Email:").append(email).append("\n");
                o.append("Password:").append(password).append("\n");
                o.append("=========================").append("\n");
                o.append("Exception").append("\n");
                o.append(e.getLocalizedMessage()).append("\n");

                if (result != null) {
                    o.append("HTTP code:").append(result.getStatus()).append("\n");
                    o.append("HTTP body:").append("\n");
                    String content = result.getContentAsString();
                    o.append(content.isEmpty() ? "EMPTY" : content);
                }
                throw new RuntimeException(o.toString());
            }
        }
    }

    private class Context {
        private final Map<String, String> context = new HashMap<>();

        private final String BODY = UUID.randomUUID().toString();

        void set(String key, String value) {
            context.put(key, value);
        }

        String setJPath(String key, String value) {
            String result = JsonPath.parse(getBody()).read(value).toString();
            set(key, result);
            return result;
        }

        String getBody() {
            return context.get(BODY);
        }

        void setBody(String value) {
            set(BODY, value);
        }

        String get(String key) {
            String result = context.get(key);
            if (result == null)
                throw new IllegalArgumentException("Unable to find value for key " + key + ". Available keys " + context);
            return result;
        }
    }
}
