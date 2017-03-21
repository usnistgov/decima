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
package gov.nist.decima.example;

import gov.nist.decima.core.Decima;
import gov.nist.decima.core.assessment.Assessment;
import gov.nist.decima.core.assessment.AssessmentException;
import gov.nist.decima.core.assessment.AssessmentExecutor;
import gov.nist.decima.core.assessment.AssessmentReactor;
import gov.nist.decima.core.assessment.result.AssessmentResults;
import gov.nist.decima.core.document.DocumentException;
import gov.nist.decima.core.requirement.RequirementsManager;
import gov.nist.decima.core.requirement.RequirementsParserException;
import gov.nist.decima.xml.DecimaXML;
import gov.nist.decima.xml.assessment.result.ReportGenerator;
import gov.nist.decima.xml.assessment.result.XMLResultBuilder;
import gov.nist.decima.xml.document.XMLDocument;
import gov.nist.decima.xml.requirement.XMLRequirementsParser;
import gov.nist.decima.xml.schematron.SchematronCompilationException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

public class Application {
  public static void main(String[] args) {
    new Application().run(args);
  }

  public Application() {
  }

  /**
   * Execute the application. This is a basic example of the code needed to create and execute
   * assessments, and to generate results and reports.
   * 
   * @param args
   *          the command line arguments
   */
  public void run(String[] args) {
    if (args.length != 1) {
      throw new IllegalArgumentException("A single file argument must be provided.");
    }

    File file = new File(args[0]);
    XMLDocument document;
    try {
      document = DecimaXML.newXMLDocument(file);
    } catch (FileNotFoundException | DocumentException ex) {
      throw new IllegalArgumentException("The provided file was invalid.", ex);
    }

    try {
      /*
       * Create the assessments
       */
      List<Assessment<XMLDocument>> assessments = new ArrayList<>(2);
      // Create an XML Schmea based assessment
      Source schemaSource = new StreamSource("classpath:schema.xsd");
      assessments.add(DecimaXML.newSchemaAssessment("XSD-1-1", Collections.singletonList(schemaSource)));
      // Create an Schmeatron based assessment
      assessments.add(DecimaXML.newSchematronAssessment(new URL("classpath:schematron.sch"), "phase"));

      /*
       * Establish an executor that runs the assessments against a provided document
       */
      AssessmentExecutor<XMLDocument> assessmentExecutor
          = Decima.newAssessmentExecutorFactory().newAssessmentExecutor(assessments);

      /*
       * Load the requirements definition
       */
      RequirementsManager requirements = Decima.newRequirementsManager().load(new URL("classpath:requirements.xml"),
          XMLRequirementsParser.instance());

      /*
       * Perform the assessment
       */
      AssessmentReactor reactor = Decima.newAssessmentReactor(requirements);
      reactor.pushAssessmentExecution(document, assessmentExecutor);
      AssessmentResults results = reactor.react();

      /*
       * Write the results to an XML file
       */
      File resultFile = new File("results.xml");
      OutputStream out = new BufferedOutputStream(new FileOutputStream(resultFile));
      new XMLResultBuilder().write(results, out);
      out.close();

      /*
       * Generate an HTML report
       */
      File reportFile = new File("report.html");
      ReportGenerator generator = new ReportGenerator();
      generator.generate(resultFile, reportFile);
    } catch (SchematronCompilationException | RequirementsParserException | URISyntaxException | AssessmentException
        | IOException | TransformerException ex) {
      throw new RuntimeException(ex);
    }
  }
}
