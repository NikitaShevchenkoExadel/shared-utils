package com.starexchangealliance.shared.utils.tests.api.framework.parser;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class XSDValidator {

    private static final Schema SCHEMA;

    static {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        URL url = Thread.currentThread().getContextClassLoader().getResource("config.xsd");
        Objects.requireNonNull(url);

        try {
            SCHEMA = factory.newSchema(new File(url.getPath()));
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    public void validate(File file) {
        final Multimap<String, XSDIssue> errors = ArrayListMultimap.create();

        Validator validator = SCHEMA.newValidator();
        validator.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(SAXParseException e) throws SAXException {
                collect(errors, "WARNING", e);
            }

            @Override
            public void error(SAXParseException e) throws SAXException {
                collect(errors, "ERROR", e);
            }

            @Override
            public void fatalError(SAXParseException e) throws SAXException {
                collect(errors, "FATAL_ERROR", e);
            }
        });

        try {
            validator.validate(new StreamSource(file));
        } catch (SAXException | IOException e) {
            throw new RuntimeException(e);
        }

        if (!errors.isEmpty())
            throw new XSDException(errors);
    }

    private void collect(Multimap<String, XSDIssue> map, String level, SAXParseException e) {
        map.put(level, new XSDIssue(e.getMessage(), e.getLineNumber(), e.getColumnNumber()));
    }

    public class XSDIssue {

        public final String message;
        public final int lineNumber;
        public final int columnNumber;

        public XSDIssue(String message, int lineNumber, int columnNumber) {
            this.message = message;
            this.lineNumber = lineNumber;
            this.columnNumber = columnNumber;
        }

        @Override
        public String toString() {
            return "XSDIssue{" + "message='" + message + '\'' + ", lineNumber=" + lineNumber + ", columnNumber=" + columnNumber + '}';
        }
    }

    public class XSDException extends RuntimeException {

        public XSDException(Multimap<String, XSDIssue> errors) {
            super(errors.toString());
        }
    }
}
