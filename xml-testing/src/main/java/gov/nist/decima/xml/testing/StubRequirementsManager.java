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

import gov.nist.decima.core.requirement.AbstractBaseRequirement;
import gov.nist.decima.core.requirement.AbstractRequirement;
import gov.nist.decima.core.requirement.BaseRequirement;
import gov.nist.decima.core.requirement.DerivedRequirement;
import gov.nist.decima.core.requirement.RequirementType;
import gov.nist.decima.core.requirement.RequirementsManager;
import gov.nist.decima.core.requirement.SpecificationReference;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StubRequirementsManager implements RequirementsManager {
    private static final String STUB_STATEMENT = "something";

    public Map<String, BaseRequirement> baseRequirements;

    /**
     * Construct a new requirements manager that does not require a requirements definition.
     * 
     * @param testedDerivedRequirements
     *            the set of derived requirements that have been reported during testing
     */
    public StubRequirementsManager(Set<String> testedDerivedRequirements) {
        this.baseRequirements = new HashMap<>();
        for (String derivedReqId : testedDerivedRequirements) {
            StubBaseRequirement base = new StubBaseRequirement(derivedReqId);
            baseRequirements.put(derivedReqId, base);
            base.addDerivedRequirement(new StubDerivedRequirement(derivedReqId, base));
        }
    }

    @Override
    public List<URI> getRequirementDefinitions() {
        return Collections.emptyList();
    }

    @Override
    public BaseRequirement getBaseRequirementById(String id) {
        return baseRequirements.get(id);
    }

    @Override
    public Collection<BaseRequirement> getBaseRequirements() {
        return baseRequirements.values();
    }

    @Override
    public DerivedRequirement getDerivedRequirementById(String id) {
        BaseRequirement base = getBaseRequirementById(id);
        return base == null ? null : base.getDerivedRequirementById(id);
    }

    private static class StubBaseRequirement extends AbstractBaseRequirement {

        public StubBaseRequirement(String id) {
            super(id, STUB_STATEMENT);
        }

        @Override
        public SpecificationReference getSpecificationReference() {
            throw new UnsupportedOperationException();
        }

    }

    private static class StubDerivedRequirement extends AbstractRequirement implements DerivedRequirement {
        private final BaseRequirement baseRequirement;

        public StubDerivedRequirement(String id, BaseRequirement base) {
            super(id, STUB_STATEMENT);
            this.baseRequirement = base;
        }

        @Override
        public BaseRequirement getBaseRequirement() {
            return baseRequirement;
        }

        @Override
        public RequirementType getType() {
            return RequirementType.MUST;
        }

        @Override
        public String getMessageText(String... args) {
            return "message";
        }

        @Override
        public boolean isConditional() {
            // never conditional, since no requirements XML is used. This simplifies unit test
            // writing.
            return false;
        }
    }
}
