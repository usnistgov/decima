
package gov.nist.decima.xml.assessment.service;

import gov.nist.decima.xml.service.ResourceResolverExtensionService;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ResourceResolverTest {
    private static final ResourceResolverExtensionService service = ResourceResolverExtensionService.getInstance();

    private static final Map<String, String> testSystemIdMap = new HashMap<>();
    static {
        // testSystemIdMap.put(, );
    }

    @Test
    public void TestSystemIds() throws SAXException, IOException {
        EntityResolver2 resolver = service.getEntityResolver();
        for (Map.Entry<String, String> entry : testSystemIdMap.entrySet()) {
            InputSource source = resolver.resolveEntity(null, entry.getKey());
            Assert.assertNotNull(source);
            Assert.assertEquals(entry.getValue(), source.getSystemId());
        }
    }
}
