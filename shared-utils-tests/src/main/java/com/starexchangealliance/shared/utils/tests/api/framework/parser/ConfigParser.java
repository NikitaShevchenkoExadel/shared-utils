package com.starexchangealliance.shared.utils.tests.api.framework.parser;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.starexchangealliance.shared.utils.tests.api.framework.model.Block;
import com.starexchangealliance.shared.utils.tests.api.framework.model.Body;
import com.starexchangealliance.shared.utils.tests.api.framework.model.HttpMethodBlock;
import com.starexchangealliance.shared.utils.tests.api.framework.model.Response;
import com.starexchangealliance.shared.utils.tests.api.framework.model.impl.database.Dump;
import com.starexchangealliance.shared.utils.tests.api.framework.model.impl.execute.ExecuteOperation;
import com.starexchangealliance.shared.utils.tests.api.framework.model.impl.http.Patch;
import com.starexchangealliance.shared.utils.tests.api.framework.model.impl.http.Post;
import com.starexchangealliance.shared.utils.tests.api.framework.model.impl.http.Put;
import com.starexchangealliance.shared.utils.tests.api.framework.model.impl.instructruction.Break;
import com.starexchangealliance.shared.utils.tests.api.framework.model.impl.mock.When;
import com.starexchangealliance.shared.utils.tests.api.framework.model.impl.variable.Variable;
import com.starexchangealliance.shared.utils.tests.api.framework.model.impl.Config;
import com.starexchangealliance.shared.utils.tests.api.framework.model.impl.auth.Authentication;
import com.starexchangealliance.shared.utils.tests.api.framework.model.impl.http.Delete;
import com.starexchangealliance.shared.utils.tests.api.framework.model.impl.http.Get;
import org.apache.commons.lang3.tuple.Pair;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;

public class ConfigParser {

    private final File root;

    public ConfigParser(File root) {
        this.root = root;
    }

    public Config parse(File each) {
        new XSDValidator().validate(each);

        try {
            XMLStreamReader r =
                    XMLInputFactory.newInstance().createXMLStreamReader(new BufferedInputStream(new FileInputStream(each)));

            try {
                return new InternalParser(root, each.getParentFile(), r).parse();
            } finally {
                r.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public class FileSearcher {

        private final File root;
        private final File dir;

        public FileSearcher(File root, File dir) {
            this.root = root;
            this.dir = dir;
        }

        public File search(final String name) {
            final String targetName = name.startsWith("/") ? name.substring(1) : name;

            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.equals(targetName);
                }
            };

            return find(dir, filter).or(new Supplier<File>() {
                @Override
                public File get() {
                    throw new FileLinkingError(dir, root, name);
                }
            });
        }

        public Optional<File> find(File dir, FilenameFilter filter) {
            if (root.equals(dir)) {
                return Optional.absent();
            } else {
                File[] files = dir.listFiles(filter);
                if (files.length == 1) {
                    return Optional.of(files[0]);
                } else {
                    return find(dir.getParentFile(), filter);
                }
            }
        }
    }

    public class FileLinkingError extends RuntimeException {
        public FileLinkingError(File start, File root, String fileKey) {
            super("Unable to find file by key [" + fileKey + "]" + "Initial scan folder [" + start + "] with strategy recursive walk to root folder [" + root + "]");
        }
    }

    public class InternalParser {

        private final XMLStreamReader r;
        private final FileSearcher fileSearcher;

        public InternalParser(File root, File dir, XMLStreamReader r) {
            this.r = r;
            this.fileSearcher = new FileSearcher(root, dir);
        }

        public Config parse() throws Exception {
            final Config config = new Config();

            parseBlock("config", new F<String>() {

                List<String> elements = Arrays.asList("when",
                        "get", "post", "put", "delete", "patch", "dump", "break", "var", "auth", "execute");

                @Override
                public void apply(String input) throws Exception {
                    if ("config".equals(input)) {
                        parseAttributes(new F<Pair<String, String>>() {
                            @Override
                            public void apply(Pair<String, String> input) throws Exception {
                                String key = input.getLeft();
                                String value = input.getRight();

                                if ("onlyThis".equals(key)) {
                                    config.setOnlyThis(Boolean.parseBoolean(value));
                                } else if ("active".equals(key)) {
                                    config.setActive(Boolean.parseBoolean(value));
                                } else if ("patch".equals(key)) {
                                    config.setPatch(Optional.of(value));
                                }
                            }
                        });

                    } else if ("description".equals(input)) {
                        config.setDescription(r.getElementText());

                    } else {
                        if (elements.contains(input)) {
                            config.addBlock(readBlock(input));
                        }
                    }
                }
            });

            return config;
        }

        public Block readBlock(String name) throws Exception {
            if ("get".equals(name)) {
                return parseMethod(new Get(), name);

            } else if ("post".equals(name)) {
                return parseMethod(new Post(), name);

            } else if ("put".equals(name)) {
                return parseMethod(new Put(), name);

            } else if ("delete".equals(name)) {
                return parseMethod(new Delete(), name);

            } else if ("patch".equals(name)) {
                return parseMethod(new Patch(), name);

            } else if ("dump".equals(name)) {
                return parseDump();

            } else if ("auth".equals(name)) {
                return parseAuth();

            } else if ("break".equals(name)) {
                return parseBreak();

            } else if ("var".equals(name)) {
                return parseVariable();

            } else if ("when".equals(name)) {
                return parseWhen();

            } else if ("execute".equals(name)) {
                return parseExecute();

            } else {
                throw new RuntimeException("Unknown block " + name);
            }
        }

        private Block parseExecute() throws Exception {
            final ExecuteOperation e = new ExecuteOperation();
            parseAttributes(new F<Pair<String, String>>() {

                                @Override
                                public void apply(Pair<String, String> input) throws Exception {
                                    String key = input.getLeft();
                                    String value = input.getRight();

                                    if ("class".equals(key)) {
                                        e.setClassName(value);
                                    } else if ("file".equals(key)) {
                                        e.setFile(Optional.of(fileSearcher.search(value)));
                                    }
                                }
                            }
            );

            return e;
        }

        private Block parseWhen() throws Exception {
            final When e = new When();
            parseAttributes(new F<Pair<String, String>>() {

                                @Override
                                public void apply(Pair<String, String> input) throws Exception {
                                    String key = input.getLeft();
                                    String value = input.getRight();

                                    if ("pokitdok".equals(key)) {
                                        e.setService(key);
                                        e.setRequest(fileSearcher.search(value));
                                    } else if ("then".equals(key)) {
                                        e.setResponse(fileSearcher.search(value));
                                    }
                                }
                            }
            );

            parseCommentInBlock(e, "when");
            return e;
        }

        private void parseCommentInBlock(final Block e, String name) throws Exception {
            parseBlock(name, new F<String>() {

                @Override
                public void apply(String input) throws Exception {
                    parseComment(e);
                }
            });
        }

        public void parseAttributes(F<Pair<String, String>> function) throws Exception {
            for (int idx = 0; idx < r.getAttributeCount(); idx++) {
                function.apply(Pair.of(r.getAttributeName(idx).toString(), r.getAttributeValue(idx)));
            }
        }

        public void parseBlock(String name, F<String> function) throws Exception {
            Boolean doNext = true;
            while (r.hasNext() && doNext) {
                int next = r.next();
                if (next == XMLStreamReader.START_ELEMENT) {
                    function.apply(r.getName().getLocalPart());
                } else if (next == XMLStreamReader.END_ELEMENT) {
                    if (r.getName().getLocalPart().equals(name)) {
                        doNext = false;
                    }
                }
            }
        }

        public void parseRequestHeader(HttpMethodBlock h) {
            h.addRequestHeader(r.getAttributeValue(0), r.getAttributeValue(1));
        }

        public void parseResponseHeader(Response h) {
            h.addResponseHeader(r.getAttributeValue(0), r.getAttributeValue(1));
        }

        public void parseParam(Body h) {
            h.addParam(r.getAttributeValue(0), r.getAttributeValue(1));
        }

        public void parseFile(Body h) {
            Optional<String> fileName = r.getAttributeCount() == 3 ? Optional.fromNullable(r.getAttributeValue(2)) : Optional.<String>absent();
            h.addFile(r.getAttributeValue(0), fileSearcher.search(r.getAttributeValue(1)), fileName);
            h.setMultipart(true);
        }

        public void parseBody(final Body h) throws Exception {
            parseBlock("body", new F<String>() {
                @Override
                public void apply(String input) {
                    if ("param".equals(input)) {
                        parseParam(h);
                    } else if ("file".equals(input)) {
                        parseFile(h);
                    }
                }
            });
        }

        public void parseResponse(final Response h) throws Exception {
            parseAttributes(new F<Pair<String, String>>() {
                @Override
                public void apply(Pair<String, String> input) {
                    String key = input.getLeft();
                    String value = input.getRight();

                    if ("code".equals(key)) {
                        h.setCode(Integer.parseInt(value));
                    } else if ("file".equals(key)) {
                        h.setResponseFile(Optional.of(fileSearcher.search(value)));
                    }
                }
            });
            parseBlock("response", new F<String>() {
                @Override
                public void apply(String input) {
                    if ("header".equals(input)) {
                        parseResponseHeader(h);
                    }
                }
            });
        }

        public <E extends HttpMethodBlock & Response> E parseMethod(final E e, String name) throws Exception {
            parseAttributes(new F<Pair<String, String>>() {

                @Override
                public void apply(Pair<String, String> input) {
                    String key = input.getLeft();
                    String value = input.getRight();

                    if ("url".equals(key)) {
                        e.setUrl(value);
                    } else if ("multipart".equals(key)) {
                        ((Body) e).setMultipart(Boolean.parseBoolean(value));
                    }
                }
            });

            parseBlock(name, new F<String>() {

                @Override
                public void apply(String input) throws Exception {
                    if ("header".equals(input)) {
                        parseRequestHeader(e);
                    } else if ("body".equals(input)) {
                        parseBody((Body) e);
                    } else if ("response".equals(input)) {
                        parseResponse(e);
                    } else if ("comment".equals(input)) {
                        parseComment(e);
                    }
                }
            });

            return e;
        }

        private <E extends Block> void parseComment(E e) throws XMLStreamException {
            e.setComment(r.getElementText());
        }

        public Block parseDump() throws Exception {
            final Dump e = new Dump();
            parseAttributes(new F<Pair<String, String>>() {
                @Override
                public void apply(Pair<String, String> input) throws Exception {
                    String key = input.getLeft();
                    String value = input.getRight();

                    if ("file".equals(key)) {
                        e.setFile(Optional.of(fileSearcher.search(value)));
                    }
                }
            });
            parseBlock("dump", new F<String>() {

                @Override
                public void apply(String input) throws Exception {
                    if ("sql".equals(input)) {
                        e.addSQL(r.getElementText());
                    } else if ("comment".equals(input)) {
                        parseComment(e);
                    }
                }
            });
            return e;
        }

        public Block parseAuth() throws Exception {
            final Authentication e = new Authentication();
            parseAttributes(new F<Pair<String, String>>() {

                @Override
                public void apply(Pair<String, String> input) throws Exception {
                    String key = input.getLeft();
                    String value = input.getRight();

                    if ("credentials".equals(key)) {
                        e.setCredentials(Optional.of(value));
                    }
                }
            });

            parseBlock("auth", new F<String>() {

                List<String> elements = Arrays.asList("when",
                        "get", "post", "put", "delete", "patch", "dump", "break", "var", "execute");

                @Override
                public void apply(String input) throws Exception {
                    if (elements.contains(input)) {
                        e.addBlock(readBlock(input));
                    } else if ("comment".equals(input)) {
                        parseComment(e);
                    }
                }
            });
            return e;
        }

        public Block parseBreak() {
            return new Break();
        }

        public Block parseVariable() throws Exception {
            final Variable e = new Variable();
            parseAttributes(new F<Pair<String, String>>() {

                                @Override
                                public void apply(Pair<String, String> input) throws Exception {
                                    String key = input.getLeft();
                                    String value = input.getRight();

                                    if ("name".equals(key)) {
                                        e.setName(value);
                                    } else if ("path".equals(key)) {
                                        e.setPath(value);
                                    }
                                }
                            }
            );

            parseCommentInBlock(e, "var");
            return e;
        }
    }

    public interface F<I> {
        public void apply(I input) throws Exception;
    }
}
