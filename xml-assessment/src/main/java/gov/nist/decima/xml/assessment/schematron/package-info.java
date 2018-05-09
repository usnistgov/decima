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
/**
 * Provides ISO Schematron-based assessment capabilities.
 * <p>
 * Note: an id is required for pattern, rule, assert, and report elements to allow computation of
 * evaluated elements at runtime.
 * <p>
 * The following example illustrates the typical way of setting up an XML Schema-based assessment
 * for execution:
 * 
 * <pre>
 * {
 *   &#64;code
 *   // Set the Schematron phase to use (optional)
 *   String phase = "phase1";
 * 
 *   // Load the Schematron and create the assessment
 *   SchematronAssessment assessment = new SchematronAssessment(new URL("classpath:schematron-file.sch"), phase);
 * 
 *   // setup Schematron parameters (optional)
 *   assessment.addParameter("param1", "value1");
 *   assessment.addParameter("param2", "value2");
 * 
 *   // Set result directory for compiled schematron and SVRL output (optional)
 *   assessment.setResultDirectory(new File("svrl-result"));
 * 
 *   // the assessment can now be executed
 * }
 * </pre>
 */

package gov.nist.decima.xml.assessment.schematron;