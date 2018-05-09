/**
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 17 United States Code Section 105, works of NIST employees are
 * not subject to copyright protection in the United States and are considered to
 * be in the public domain. Permission to freely use, copy, modify, and distribute
 * this software and its documentation without fee is hereby granted, provided that
 * this notice and disclaimer of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE. IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM, OR
 * IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

package gov.nist.decima.xml.testing;

import gov.nist.decima.core.Decima;
import gov.nist.decima.core.assessment.Assessment;
import gov.nist.decima.core.assessment.logic.LogicAssessment;
import gov.nist.decima.core.assessment.result.ResultStatus;
import gov.nist.decima.core.document.handling.CachingStrategy;
import gov.nist.decima.core.document.handling.MappedCachingStrategy;
import gov.nist.decima.xml.assessment.schema.SchemaAssessment;
import gov.nist.decima.xml.assessment.schematron.DefaultSchematronHandler;
import gov.nist.decima.xml.assessment.schematron.SchematronAssessment;
import gov.nist.decima.xml.assessment.schematron.SchematronHandler;
import gov.nist.decima.xml.document.DefaultXMLDocumentFactory;
import gov.nist.decima.xml.document.MutableXMLDocument;
import gov.nist.decima.xml.document.XMLDocument;
import gov.nist.decima.xml.document.XMLDocumentFactory;
import gov.nist.decima.xml.document.XPathCondition;
import gov.nist.decima.xml.jdom2.JDOMUtil;
import gov.nist.decima.xml.schematron.DefaultSchematronCompiler;
import gov.nist.decima.xml.schematron.Schematron;
import gov.nist.decima.xml.schematron.SchematronCompilationException;
import gov.nist.decima.xml.templating.document.post.template.TemplateParser;
import gov.nist.decima.xml.templating.document.post.template.TemplateParserException;
import gov.nist.decima.xml.templating.document.post.template.TemplatePostProcessor;
import gov.nist.decima.xml.templating.document.post.template.TemplateProcessor;
import gov.nist.decima.xml.testing.assertion.Assertion;
import gov.nist.decima.xml.testing.assertion.BaseRequirementAssertion;
import gov.nist.decima.xml.testing.assertion.DerivedRequirementAssertion;
import gov.nist.decima.xml.testing.assertion.Operator;
import gov.nist.decima.xml.testing.assertion.OverallAssertion;
import gov.nist.decima.xml.testing.assertion.RemainingAssertion;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.sax.SAXEngine;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssessmentUnitTestParser {
    private static final Class<?>[] ONE_ARGUMENT = { Schematron.class };
    private static final Class<?>[] TWO_ARGUMENT = { Schematron.class, String.class };

    private static final Map<String, ResultStatus> statusTranslationMap;
    private static final Map<String, Operator> operatorTranslationMap;

    static {
        statusTranslationMap = new HashMap<>();
        statusTranslationMap.put("NOT_IN_SCOPE", ResultStatus.NOT_IN_SCOPE);
        statusTranslationMap.put("NOT_TESTED", ResultStatus.NOT_TESTED);
        statusTranslationMap.put("NOT_APPLICABLE", ResultStatus.NOT_APPLICABLE);
        statusTranslationMap.put("INFORMATIONAL", ResultStatus.INFORMATIONAL);
        statusTranslationMap.put("PASS", ResultStatus.PASS);
        statusTranslationMap.put("WARNING", ResultStatus.WARNING);
        statusTranslationMap.put("FAIL", ResultStatus.FAIL);

        operatorTranslationMap = new HashMap<>();
        operatorTranslationMap.put("EQUAL", Operator.EQUAL);
        operatorTranslationMap.put("GREATER_THAN", Operator.GREATER_THAN);
        operatorTranslationMap.put("LESS_THAN", Operator.LESS_THAN);

        try {
            instance = new AssessmentUnitTestParser();
        } catch (JDOMException | MalformedURLException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    private static ResultStatus translateResult(String value) {
        return statusTranslationMap.get(value);
    }

    private static Operator translateOperator(String value) {
        return operatorTranslationMap.get(value);
    }

    private static final AssessmentUnitTestParser instance;

    public static AssessmentUnitTestParser getInstance() {
        return instance;
    }

    private final Map<String, Schematron> urlToSchematronMap = new HashMap<>();
    // private final Map<SchematronAssessmentInfo, SchematronAssessment> schematronAssessmentMap =
    // new
    // HashMap<>();
    private final XMLDocumentFactory xmlDocumentFactory;
    private final SAXEngine saxEngine;
    private File resultDir;

    private AssessmentUnitTestParser() throws JDOMException, MalformedURLException, SAXException {
        // prevent construction
        CachingStrategy<MutableXMLDocument> cachingStrategy = new MappedCachingStrategy<MutableXMLDocument>();
        DefaultXMLDocumentFactory documentFactory = new DefaultXMLDocumentFactory(cachingStrategy);
        documentFactory.registerPostProcessor(new TemplatePostProcessor());
        this.xmlDocumentFactory = documentFactory;
        this.saxEngine = initSAXEngine();
    }

    protected SAXEngine initSAXEngine() throws MalformedURLException, SAXException, JDOMException {
        return JDOMUtil.newValidatingSAXEngine(new URL("classpath:schema/content-unit-test.xsd"));
    }

    public XMLDocumentFactory getXMLDocumentFactory() {
        return xmlDocumentFactory;
    }

    public File getResultDirectory() {
        return resultDir;
    }

    public void setResultDirectory(File dir) {
        this.resultDir = dir;
    }

    private SAXEngine getSaxEngine() {
        return saxEngine;
    }

    public DefaultAssessmentUnitTest parse(File file) throws ParserException, MalformedURLException {
        return parse(file.toURI().toURL());
    }

    /**
     * Parses the provided URL and builds a Decima JUnit test based on the resources content
     * 
     * @param url
     *            a URL pointing to a resource containing a Decima JUnit test definition.
     * @return the unit test instance
     * @throws ParserException
     *             if an error occurred while parsing the resource
     */
    public DefaultAssessmentUnitTest parse(URL url) throws ParserException {
        Document document;
        try {
            document = getSaxEngine().build(url);
        } catch (IOException | JDOMException e) {
            throw new ParserException("unable to load unit test document", e);
        }
        return buildUnitTest(document, url);
    }
    //
    // public AssessmentUnitTest parse(InputStream is, String baseURI) throws ParserException {
    // ContentUnitTestDocument doc;
    // try {
    // doc = ContentUnitTestDocument.Factory.parse(is);
    // } catch (XmlException | IOException e) {
    // throw new ParserException("unable to load unit test document", e);
    // }
    // return buildUnitTest(doc, baseURI);
    // }

    protected DefaultAssessmentUnitTest buildUnitTest(Document doc, URL testURL) throws ParserException {
        Element cut = doc.getRootElement();

        AssessmentUnitTestBuilder builder = new AssessmentUnitTestBuilder(getXMLDocumentFactory());
        builder.setSourceURI(testURL.toString());
        builder.setDerivedRequirement(cut.getChild("requirement", cut.getNamespace()).getTextNormalize());
        builder.setSummary(cut.getChild("description", cut.getNamespace()).getTextNormalize());
        builder.setResultDirectory(resultDir);

        TemplateProcessor tp;
        try {
            tp = TemplateParser.getInstance().parse(cut.getChild("template", TemplateParser.TEMPLATE_NAMESPACE),
                    testURL);
        } catch (TemplateParserException e) {
            throw new ParserException("Unable to parse template", e);
        }

        builder.setTemplate(tp);
        Element assessmentsElement = cut.getChild("assessments", cut.getNamespace());
        List<Element> assessments = assessmentsElement.getChildren();
        for (int index = 0; index < assessments.size(); index++) {
            Element assessmentElement = assessments.get(index);
            File resultDir = new File(getResultDirectory(), "assessment-" + index);

            Assessment<XMLDocument> assessment;
            if ("schematron-assessment".equals(assessmentElement.getName())) {
                assessment = buildSchematronAssessment(assessmentElement, resultDir);
            } else if ("schema-assessment".equals(assessmentElement.getName())) {
                assessment = buildSchemaAssessment(assessmentElement);
            } else if ("logic-assessment".equals(assessmentElement.getName())) {
                try {
                    assessment = buildLogicAssessment(assessmentElement);
                } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
                        | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    throw new ParserException(e);
                }
            } else {
                throw new ParserException(new UnsupportedOperationException("Unsupported assessment type: {"
                        + assessmentElement.getNamespaceURI() + "}" + assessmentElement.getName()));
            }

            Attribute testAttr = assessmentElement.getAttribute("test");
            if (testAttr != null) {
                assessment = Decima.newConditionalAssessment(assessment, new XPathCondition(testAttr.getValue()));
            }
            builder.addAssessment(assessment);
        }

        Element assertionsElement = cut.getChild("assertions", cut.getNamespace());
        Element overallElement = assertionsElement.getChild("assert-overall", cut.getNamespace());
        if (overallElement != null) {
            builder.addAssertion(buildAssertOverall(overallElement));
        } else {
            for (Element assertionElement : assertionsElement.getChildren()) {
                Assertion assertion;
                switch (assertionElement.getName()) {
                case "assert-derived-requirement":
                    assertion = buildAssertDerivedRequirement(assertionElement);
                    break;
                case "assert-base-requirement":
                    assertion = buildAssertBaseRequirement(assertionElement);
                    break;
                case "assert-remaining":
                    assertion = buildAssertRemaining(assertionElement);
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported assertion type: {"
                            + assertionElement.getNamespaceURI() + "}" + assertionElement.getName());
                }

                builder.addAssertion(assertion);
            }
        }
        return builder.build();
    }

    protected static LogicAssessment<XMLDocument> buildLogicAssessment(Element assessmentElement)
            throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParserException {
        String className = assessmentElement.getAttributeValue("class");
        List<Element> paramElements = assessmentElement.getChildren("parameter", assessmentElement.getNamespace());

        Class<?> clazz = Class.forName(className);
        if (!LogicAssessment.class.isAssignableFrom(clazz)) {
            throw new ParserException(
                    "Class '" + clazz.getName() + "' does not implement " + LogicAssessment.class.getName());
        }

        Constructor<?> ctor;
        Object[] args;
        if (paramElements.isEmpty()) {
            ctor = clazz.getConstructor();
            args = null;
        } else {
            Class<?>[] paramTypes = new Class<?>[paramElements.size()];
            args = new Object[paramElements.size()];
            for (int i = 0; i < paramElements.size(); i++) {
                paramTypes[i] = String.class;
                args[i] = paramElements.get(i).getValue();
            }
            ctor = clazz.getConstructor(paramTypes);
        }

        @SuppressWarnings("unchecked")
        LogicAssessment<XMLDocument> retval = (LogicAssessment<XMLDocument>) ctor.newInstance(args);

        return retval;
    }

    private SchematronAssessment buildSchematronAssessment(Element assessmentElement, File resultDir)
            throws ParserException {

        // TODO: cache schematron assessments. Need to find a way to handle result output
        SchematronAssessmentInfo schematronInfo;
        try {
            schematronInfo = new SchematronAssessmentInfo(assessmentElement);
        } catch (MalformedURLException ex) {
            throw new ParserException(ex);
        }
        // SchematronAssessment retval = schematronAssessmentMap.get(schematronInfo);
        // if (retval == null) {

        Schematron schematron = getSchematron(schematronInfo.getRulsetLocation());

        String phase = schematronInfo.getPhase();

        SchematronHandler schematronHandler = newSchematronHandler(schematronInfo.getHandlerClass(), schematron, phase);

        SchematronAssessment retval = new SchematronAssessment(schematron, phase, schematronHandler);

        retval.addParameters(schematronInfo.getParameters());
        retval.setResultDirectory(resultDir);
        // }
        return retval;
    }

    protected final SchematronHandler newSchematronHandler(String handlerClassName, Schematron schematron, String phase)
            throws ParserException {
        // Use a specialized SchematronHandler?
        SchematronHandler schematronHandler;
        if (handlerClassName == null) {
            // We now need a SchematronHandler instance
            schematronHandler = new DefaultSchematronHandler(schematron);
        } else {
            // get the handler class to use
            Class<? extends SchematronHandler> handlerClass;
            try {
                @SuppressWarnings("unchecked")
                Class<? extends SchematronHandler> clazz
                        = (Class<? extends SchematronHandler>) Class.forName(handlerClassName);
                handlerClass = clazz;
            } catch (ClassNotFoundException ex) {
                throw new ParserException(ex);
            }

            // construct the handler
            // analyze the constructor arguments
            // A typical constructor will allow for a Schematron, and optionally a phase (String)
            Constructor<?> constructor = null;
            Object[] params = null;
            try {
                constructor = handlerClass.getConstructor(TWO_ARGUMENT);
                params = new Object[] { schematron, phase };
            } catch (NoSuchMethodException ex) {
                // do nothing
            }

            if (constructor == null) {
                try {
                    constructor = handlerClass.getConstructor(ONE_ARGUMENT);
                    params = new Object[] { schematron };
                } catch (NoSuchMethodException ex) {
                    // do nothing
                }
            }

            if (constructor == null) {
                try {
                    constructor = handlerClass.getConstructor();
                    params = new Object[] {};
                } catch (NoSuchMethodException ex) {
                    throw new ParserException("the SchematronEvaluator must have a constructor with the signature of: "
                            + "Schematron, String; Schematron; or no arguments");
                }
            }

            // now construct the handler
            try {
                schematronHandler = (SchematronHandler) constructor.newInstance(params);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | SecurityException e) {
                throw new ParserException(e);
            }

        }
        return schematronHandler;
    }

    private Schematron getSchematron(URL url) throws ParserException {
        Schematron retval = urlToSchematronMap.get(url.toExternalForm());
        if (retval == null) {
            try {
                DefaultSchematronCompiler schematronCompiler = new DefaultSchematronCompiler();
                retval = schematronCompiler.newSchematron(url);
            } catch (SchematronCompilationException e) {
                throw new ParserException(e);
            }
            urlToSchematronMap.put(url.toExternalForm(), retval);
        }
        return retval;
    }

    private SchemaAssessment buildSchemaAssessment(Element assessmentElement) {
        SchemaAssessment retval = new SchemaAssessment(assessmentElement.getAttributeValue("derived-requirement"));
        return retval;
    }

    private Assertion buildAssertDerivedRequirement(Element assertionElement) {
        return new DerivedRequirementAssertion(assertionElement.getAttributeValue("requirement-id"),
                translateResult(assertionElement.getAttributeValue("result")));
    }

    private Assertion buildAssertBaseRequirement(Element assertionElement) {
        return new BaseRequirementAssertion(assertionElement.getAttributeValue("requirement-id"),
                translateResult(assertionElement.getAttributeValue("result")));
    }

    private Assertion buildAssertRemaining(Element assertionElement) {
        return new RemainingAssertion(translateResult(assertionElement.getAttributeValue("result")),
                assertionElement.getAttributeValue("quantifier"),
                translateOperator(assertionElement.getAttributeValue("operator")));
    }

    private Assertion buildAssertOverall(Element assertionElement) {
        return new OverallAssertion(translateResult(assertionElement.getAttributeValue("result")),
                assertionElement.getAttributeValue("quantifier"),
                translateOperator(assertionElement.getAttributeValue("operator")));
    }

}
