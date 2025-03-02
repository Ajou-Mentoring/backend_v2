package pheonix.classconnect.backend.security.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.schema.impl.XSStringImpl;

import java.util.List;

@Slf4j
@Getter
public class SamlResponse {
    private final Assertion assertion;
    private final String encodedResponse;
    private final String decodedResponse;

    public SamlResponse(
            final Assertion assertion,
            final String encodedResponse,
            final String decodedResponse) {

        this.assertion = assertion;
        this.encodedResponse = encodedResponse;
        this.decodedResponse = decodedResponse;
    }

    /**
     * Retrieves the Name ID from the SAML response. This is normally
     * the name of the authenticated user.
     *
     * @return The Name ID from the SAML response.
     */
    public String getNameID() {
        printAllAttributes();

        return assertion.getSubject().getNameID().getValue();
    }
    public void printAllAttributes() {
        List<AttributeStatement> attributeStatements = assertion.getAttributeStatements();
        for (AttributeStatement attributeStatement : attributeStatements) {
            for (Attribute attribute : attributeStatement.getAttributes()) {
                String attributeName = attribute.getName();
                List<XMLObject> attributeValues = attribute.getAttributeValues();
                for (XMLObject attributeValue : attributeValues) {
                    if (attributeValue instanceof XSStringImpl) {
                        String value = ((XSStringImpl) attributeValue).getValue();
                        log.info("{} : {}", attributeName, value);
                        System.out.println(attributeName + ": " + value);
                    }
                }
            }
        }
    }
}
