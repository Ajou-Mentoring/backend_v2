package pheonix.classconnect.backend.security.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.xerces.parsers.DOMParser;
import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.*;
import org.opensaml.saml2.core.validator.ResponseSchemaValidator;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml2.metadata.SingleSignOnService;
import org.opensaml.saml2.metadata.provider.DOMMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.keyinfo.KeyInfoHelper;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.signature.X509Data;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.util.XMLHelper;
import org.opensaml.xml.validation.ValidationException;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import pheonix.classconnect.backend.exceptions.ErrorCode;
import pheonix.classconnect.backend.exceptions.MainApplicationException;
import pheonix.classconnect.backend.security.utils.BrowserUtils;

import javax.xml.namespace.QName;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

@Slf4j
public class SamlClient {
    private static boolean initializedOpenSaml = false;

    public enum SamlIdpBinding { POST, Redirect }

    private final String relyingPartyIdentifier;
    private final String assertionConsumerServiceUrl;
    private final String identityProviderUrl;
    private final String responseIssuer;
    private final List<Credential> credentials;
    private final SamlIdpBinding samlBinding;

    /**
     * Constructs an SAML client using explicit parameters.
     *
     * @param relyingPartyIdentifier      the identifier of the relying party.
     * @param assertionConsumerServiceUrl the url where the identity provider
     *                                    will post back the SAML response.
     * @param identityProviderUrl         the url where the SAML request will
     *                                    be submitted.
     * @param responseIssuer              the expected issuer ID for
     *                                    SAML responses.
     * @param certificates                the list of base-64 encoded
     *                                    certificates to use to validate
     *                                    responses.
     * @param samlBinding                 what type of SAML binding should
     *                                    the client use.
     * @throws pheonix.classconnect.backend.exceptions.MainApplicationException              thrown if any error occur
     *                                    while loading the provider information.
     */
    public SamlClient(
            String relyingPartyIdentifier,
            String assertionConsumerServiceUrl,
            String identityProviderUrl,
            String responseIssuer,
            List<X509Certificate> certificates,
            SamlIdpBinding samlBinding) throws MainApplicationException {

        ensureOpenSamlIsInitialized();

        if (relyingPartyIdentifier == null)
            throw new IllegalArgumentException("relyingPartyIdentifier");

        if (identityProviderUrl == null)
            throw new IllegalArgumentException("identityProviderUrl");

        if (responseIssuer == null)
            throw new IllegalArgumentException("responseIssuer");

        if (certificates == null || certificates.isEmpty())
            throw new IllegalArgumentException("certificates");

        this.relyingPartyIdentifier = relyingPartyIdentifier;
        this.assertionConsumerServiceUrl = assertionConsumerServiceUrl;
        this.identityProviderUrl = identityProviderUrl;
        this.responseIssuer = responseIssuer;
        this.credentials = certificates.stream()
                .map(SamlClient::getCredential)
                .collect(Collectors.toList());
        this.samlBinding = samlBinding;
    }

    /**
     * Builds an encoded SAML request.
     *
     * @return The base-64 encoded SAML request.
     * @throws MainApplicationException thrown if an unexpected error occurs.
     */
    public String getSamlRequest() throws MainApplicationException {
        AuthnRequest request =
                (AuthnRequest) buildSamlObject(AuthnRequest.DEFAULT_ELEMENT_NAME);

        // ADFS needs IDs to start with a letter
        request.setID("z" + UUID.randomUUID());

        request.setVersion(SAMLVersion.VERSION_20);
        request.setIssueInstant(DateTime.now());
        request.setProtocolBinding(
                "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-" +
                        // Response 는 무조건 POST 바인딩으로 가져와야한다.
                        SamlIdpBinding.POST);

        request.setAssertionConsumerServiceURL(assertionConsumerServiceUrl);

        Issuer issuer = (Issuer)buildSamlObject(Issuer.DEFAULT_ELEMENT_NAME);
        issuer.setValue(relyingPartyIdentifier);
        request.setIssuer(issuer);

        NameIDPolicy nameIDPolicy = (NameIDPolicy)buildSamlObject
                (NameIDPolicy.DEFAULT_ELEMENT_NAME);

        nameIDPolicy.setFormat("urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified");

        request.setNameIDPolicy(nameIDPolicy);



        StringWriter stringWriter = new StringWriter();
        try {
            Marshaller marshaller =
                    Configuration.getMarshallerFactory().getMarshaller(request);

            Element dom = marshaller.marshall(request);
            XMLHelper.writeNode(dom, stringWriter);
        } catch(MarshallingException ex) {
            log.error(ex.getMessage());
            throw new MainApplicationException(ErrorCode.SYS_INTERNAL_SERVER_ERROR, "SAML Request를 XML로 변환하는 과정에서 에러가 발생했습니다.");
        }

        String messageXML = stringWriter.toString();
        log.debug("Issuing SAML request: " + messageXML);

        // for binding POST
        if(this.samlBinding == SamlIdpBinding.POST) {
            return Base64.encodeBytes(messageXML.getBytes(StandardCharsets.UTF_8));
        }

        // for binding Redirect
        Deflater deflater = new Deflater(Deflater.DEFLATED, true);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DeflaterOutputStream deflaterOutputStream =
                new DeflaterOutputStream(byteArrayOutputStream, deflater);

        try {
            deflaterOutputStream.write(messageXML.getBytes());
            deflaterOutputStream.close();
        } catch (IOException ex) {
            log.error(ex.getMessage());
            throw new MainApplicationException(ErrorCode.SYS_INTERNAL_SERVER_ERROR, "SAML Request를 Byte로 변환하는 과정에서 에러가 발생했습니다.");
        }

        String samlRequest = Base64.encodeBytes
                (byteArrayOutputStream.toByteArray(), Base64.DONT_BREAK_LINES);

        return URLEncoder.encode(samlRequest, StandardCharsets.UTF_8);
    }

    /**
     * Decodes and validates an SAML response returned by an identity provider.
     *
     * @param encodedResponse the encoded response returned by
     *                        the identity provider.
     * @return An {@link SamlResponse} object containing information
     *         decoded from the SAML response.
     * @throws MainApplicationException if the signature is invalid,
     *                       or if any other error occurs.
     */
    public SamlResponse
    decodeAndValidateSamlResponse(
            String encodedResponse,
            UUID seq
    ) throws MainApplicationException {
        String decodedResponse;
        decodedResponse = new String(Base64.decode(encodedResponse), StandardCharsets.UTF_8);

        log.debug(
                "\n\tdecodeAndValidateSamlResponse-> Seq={}, decoded SAMLResponse={}",
                seq, decodedResponse);

        Response response = null;
        try {
            DOMParser parser = createDOMParser();
            parser.parse(new InputSource(new StringReader(decodedResponse)));

            response = (Response)Configuration.getUnmarshallerFactory()
                    .getUnmarshaller(parser.getDocument().getDocumentElement())
                    .unmarshall(parser.getDocument().getDocumentElement());
        } catch (IOException | SAXException | UnmarshallingException ex) {
            log.error(ex.getMessage());
            throw new MainApplicationException(ErrorCode.SYS_INTERNAL_SERVER_ERROR, "SAML Response를 디코딩하는 과정에서 에러가 발생했습니다.");
        }

        validateResponse(response);
        validateAssertion(response);
        validateSignature(response);

        Assertion assertion = response.getAssertions().get(0);
        return new SamlResponse(assertion, encodedResponse, decodedResponse);
    }

    /**
     * Redirects an {@link HttpServletResponse} to the configured
     * identity provider.
     *
     * @param response   The {@link HttpServletResponse}.
     * @throws IOException   thrown if an IO error occurs.
     * @throws MainApplicationException thrown is an unexpected error occurs.
     */
    public void redirectToIdentityProvider(HttpServletResponse response)
            throws IOException, MainApplicationException {

        Map<String, String> values = new HashMap<>();
        values.put("SAMLRequest", getSamlRequest());
        values.put("userInfo", "userId,userNo,empStudentNo,entryOpt");

        if(this.samlBinding == SamlIdpBinding.POST)
            BrowserUtils.postUsingBrowser(identityProviderUrl, response, values);
        else
            BrowserUtils.getUsingBrowser(identityProviderUrl, response, values);
    }

    /**
     * Processes a POST containing the SAML response.
     *
     * @param request the {@link HttpServletRequest}.
     * @return An {@link SamlResponse} object containing information
     *         decoded from the SAML response.
     * @throws MainApplicationException thrown is an unexpected error occurs.
     */
    public SamlResponse processPostFromIdentityProvider(HttpServletRequest request)
            throws MainApplicationException {
        String encodedResponse = request.getParameter("SAMLResponse");

        UUID seq = null;
        HttpSession session = request.getSession();
        if (session != null) {
            seq = (UUID)session.getAttribute("Seq");
        }

        log.debug(
                "\n\tprocessPostFromIdentityProvider-> Seq={}, encoded SAMLResponse={}",
                seq, encodedResponse);

        return decodeAndValidateSamlResponse(encodedResponse, seq);
    }

    /**
     * Constructs an SAML client using XML metadata obtained from
     * the identity provider. <p> When using Okta as an identity provider,
     * it is possible to pass null to relyingPartyIdentifier and
     * assertionConsumerServiceUrl; they will be inferred from
     * the metadata provider XML.
     *
     * @param relyingPartyIdentifier      the identifier for the relying party.
     * @param assertionConsumerServiceUrl the url where the identity provider
     *                                    will post back the SAML response.
     * @param metadata                    the XML metadata obtained from the
     *                                    identity provider.
     * @return The created {@link SamlClient}.
     * @throws MainApplicationException thrown if any error occur while loading
     *         the metadata information.
     */
    public static SamlClient fromMetadata(
            String relyingPartyIdentifier,
            String assertionConsumerServiceUrl,
            Reader metadata,
            String requestBinding) throws MainApplicationException {

        if ("POST".equals(requestBinding)) {
            return fromMetadata(
                    relyingPartyIdentifier,
                    assertionConsumerServiceUrl,
                    metadata,
                    SamlIdpBinding.POST);
        }

        return fromMetadata(
                relyingPartyIdentifier,
                assertionConsumerServiceUrl,
                metadata,
                SamlIdpBinding.Redirect);
    }

    /**
     * Constructs an SAML client using XML metadata obtained from
     * the identity provider. <p> When using Okta as an identity provider,
     * it is possible to pass null to relyingPartyIdentifier and
     * assertionConsumerServiceUrl; they will be inferred from
     * the metadata provider XML.
     *
     * @param relyingPartyIdentifier      the identifier for the relying party.
     * @param assertionConsumerServiceUrl the url where the identity provider
     *                                    will post back the SAML response.
     * @param metadata                    the XML metadata obtained from
     *                                    the identity provider.
     * @param samlBinding                 the HTTP method to use
     *                                    for binding to the IdP.
     * @return The created {@link SamlClient}.
     * @throws MainApplicationException              thrown if any error occur
     *                                    while loading the metadata information.
     */
    public static SamlClient fromMetadata(
            String relyingPartyIdentifier,
            String assertionConsumerServiceUrl,
            Reader metadata,
            SamlIdpBinding samlBinding) throws MainApplicationException {

        return fromMetadata(
                relyingPartyIdentifier,
                assertionConsumerServiceUrl,
                metadata,
                samlBinding,
                null);
    }

    /**
     * Constructs an SAML client using XML metadata obtained from the identity provider. <p> When
     * using Okta as an identity provider, it is possible to pass null to relyingPartyIdentifier and
     * assertionConsumerServiceUrl; they will be inferred from the metadata provider XML.
     *
     * @param relyingPartyIdentifier      the identifier for the relying party.
     * @param assertionConsumerServiceUrl the url where the identity provider will post back the
     *                                    SAML response.
     * @param metadata                    the XML metadata obtained from the identity provider.
     * @param samlBinding                 the HTTP method to use for binding to the IdP.
     * @return The created {@link SamlClient}.
     * @throws MainApplicationException thrown if any error occur while loading the metadata information.
     */
    public static SamlClient fromMetadata(
            String relyingPartyIdentifier,
            String assertionConsumerServiceUrl,
            Reader metadata,
            SamlIdpBinding samlBinding,
            List<X509Certificate> certificates) throws MainApplicationException {

        log.debug("\n\tfromMetadata-> SamlIdpBinding={}", samlBinding);

        ensureOpenSamlIsInitialized();

        MetadataProvider metadataProvider = createMetadataProvider(metadata);
        EntityDescriptor entityDescriptor = getEntityDescriptor(metadataProvider);

        IDPSSODescriptor idpSsoDescriptor = getIDPSSODescriptor(entityDescriptor);
        SingleSignOnService idpBinding = getIdpBinding(idpSsoDescriptor, samlBinding);
        List<X509Certificate> x509Certificates = getCertificates(idpSsoDescriptor);
        boolean isOkta = entityDescriptor.getEntityID().contains(".okta.com");

        if (relyingPartyIdentifier == null) {
            // Okta's own toolkit uses the entity ID as a relying party identifier,
            // so if we detect that the IDP is Okta let's tolerate a null value
            // for this parameter.
            if (isOkta)
                relyingPartyIdentifier = entityDescriptor.getEntityID();
            else
                throw new IllegalArgumentException("relyingPartyIdentifier");
        }

        if (assertionConsumerServiceUrl == null && isOkta) {
            // Again, Okta's own toolkit uses this value
            // for the assertion consumer url, which
            // kinda makes no sense since this is supposed to
            // be a url pointing to a server outside Okta,
            // but it probably just straight ignores this and use the one from
            // it's own config anyway.
            assertionConsumerServiceUrl = idpBinding.getLocation();
        }

        if (certificates != null) {
            // Adding certificates given to this method
            // because some idp metadata file does not embedded signing certificate
            x509Certificates.addAll(certificates);
        }

        String identityProviderUrl = idpBinding.getLocation();
        String responseIssuer = entityDescriptor.getEntityID();

        return new SamlClient(
                relyingPartyIdentifier,
                assertionConsumerServiceUrl,
                identityProviderUrl,
                responseIssuer,
                x509Certificates,
                samlBinding);
    }

    private void validateResponse(Response response) throws MainApplicationException {
        try {
            new ResponseSchemaValidator().validate(response);
        } catch(ValidationException ex) {
            log.error(ex.getMessage());
            throw new MainApplicationException(ErrorCode.SYS_INTERNAL_SERVER_ERROR, "SAML Response의 스키마가 다릅니다.");
        }

        if (!response.getIssuer().getValue().equals(responseIssuer)) {
            log.error("The response issuer didn't match the expected value" +
                            "responseIssuer= {}, response.getIssuer().getValue() = {}",
                    responseIssuer, response.getIssuer().getValue()
            );

            throw new MainApplicationException(ErrorCode.SYS_INTERNAL_SERVER_ERROR, String.format("SAML Response의 발급자가 다릅니다. 기댓값: [%s], 실제값: [%s]}", responseIssuer, response.getIssuer().getValue()));
        }

        String statusCode = response.getStatus().getStatusCode().getValue();

        if (!statusCode.equals("urn:oasis:names:tc:SAML:2.0:status:Success"))
            throw new MainApplicationException(ErrorCode.SYS_INTERNAL_SERVER_ERROR, "유효하지 않은 상태값입니다. 실제값: [" + statusCode + "]");
    }

    private void validateAssertion(Response response) throws MainApplicationException {
        if (response.getAssertions().size() != 1) {
            throw new MainApplicationException(ErrorCode.SYS_INTERNAL_SERVER_ERROR, "SAML Response의 Assertion은 단일 대상이어야 합니다.");
        }

        Assertion assertion = response.getAssertions().get(0);
        if(!assertion.getIssuer().getValue().equals(responseIssuer)) {
            throw new MainApplicationException(ErrorCode.SYS_INTERNAL_SERVER_ERROR, String.format("SAML Response의 발급자가 다릅니다. 기댓값: [%s], 실제값: [%s]}", responseIssuer, response.getIssuer().getValue()));
        }

        if(assertion.getSubject().getNameID() == null) {
            throw new MainApplicationException(ErrorCode.SYS_INTERNAL_SERVER_ERROR,
                    "SAML Response에서 NameID 값이 누락되었습니다.");
        }

        enforceConditions(assertion.getConditions());
    }

    private void enforceConditions(Conditions conditions) throws MainApplicationException {
        DateTime now = DateTime.now();

        long notBeforeSkew = 0L;
        DateTime notBefore = conditions.getNotBefore();
        DateTime skewedNotBefore = notBefore.minus(notBeforeSkew);
        if (now.isBefore(skewedNotBefore)) {
            String notStr = notBefore.toString();
            throw new MainApplicationException(ErrorCode.SYS_INTERNAL_SERVER_ERROR, "The assertion cannot be used before " + notStr);
        }

        DateTime notOnOrAfter = conditions.getNotOnOrAfter();
        if (now.isAfter(notOnOrAfter)) {
            String notStr = notOnOrAfter.toString();
            throw new MainApplicationException(ErrorCode.SYS_INTERNAL_SERVER_ERROR, "The assertion cannot be used after " + notStr);
        }
    }

    private void validateSignature(Response response) throws MainApplicationException {
        Signature responseSignature = response.getSignature();
        Signature assertionSignature = response.getAssertions().get(0).getSignature();

        if (responseSignature == null && assertionSignature == null) {
            throw new MainApplicationException(ErrorCode.SYS_INTERNAL_SERVER_ERROR, "서명되지 않은 SAML Response입니다.");
        }

        if (responseSignature != null && !validate(responseSignature))
            throw new MainApplicationException(ErrorCode.SYS_INTERNAL_SERVER_ERROR, "유효하지 않은 서명입니다. [Response의 서명이 유효하지 않음]");

        if (assertionSignature != null && !validate(assertionSignature))
            throw new MainApplicationException(ErrorCode.SYS_INTERNAL_SERVER_ERROR, "유효하지 않은 서명입니다. [Assertion의 서명이 유효하지 않음]");
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean validate(Signature signature) {
        if (signature == null) return false;

        // It's fine if any of the credentials match the signature
        return credentials.stream()
                .anyMatch(c -> {
                    try {
                        SignatureValidator signatureValidator = new SignatureValidator(c);
                        signatureValidator.validate(signature);
                        return true;
                    } catch (ValidationException ex) {
                        ex.printStackTrace();
                        return false;
                    }
                });
    }

    private synchronized static void
    ensureOpenSamlIsInitialized() throws MainApplicationException {
        if (!initializedOpenSaml) {
            try {
                DefaultBootstrap.bootstrap();
                initializedOpenSaml = true;
            } catch (Throwable ex) {
                log.error(ex.getMessage());
                throw new MainApplicationException(ErrorCode.SYS_INTERNAL_SERVER_ERROR, "SAML Library 초기화 중 에러가 발생했습니다.");
            }
        }
    }

    private static DOMParser createDOMParser() throws MainApplicationException {
        return new DOMParser() {{
            try {
                setFeature(INCLUDE_COMMENTS_FEATURE, false);
            } catch (Throwable ex) {
                log.error(ex.getMessage());
                throw new MainApplicationException(ErrorCode.SYS_INTERNAL_SERVER_ERROR,
                        "Cannot disable comments parsing to mitigate " +
                                "https://www.kb.cert.org/vuls/id/475445");
            }
        }};
    }

    private static MetadataProvider createMetadataProvider(Reader metadata)
            throws MainApplicationException {
        try {
            DOMParser parser = createDOMParser();
            parser.parse(new InputSource(metadata));
            DOMMetadataProvider provider =
                    new DOMMetadataProvider(parser.getDocument().getDocumentElement());

            provider.initialize();
            return provider;
        } catch (IOException | SAXException | MetadataProviderException ex) {
            log.error(ex.getMessage());
            throw new MainApplicationException(ErrorCode.SYS_INTERNAL_SERVER_ERROR, "IDP metadata 로딩 오류입니다.");
        }
    }

    private static EntityDescriptor getEntityDescriptor(MetadataProvider metadataProvider)
            throws MainApplicationException {
        EntityDescriptor descriptor;

        try {
            descriptor = (EntityDescriptor)metadataProvider.getMetadata();
        } catch (MetadataProviderException ex) {
            log.error(ex.getMessage());
            throw new MainApplicationException(ErrorCode.SYS_INTERNAL_SERVER_ERROR, "entity descriptor를 찾을 수 없습니다.");
        }

        if(descriptor == null)
            throw new MainApplicationException(ErrorCode.SYS_INTERNAL_SERVER_ERROR, "entity descriptor를 찾을 수 없습니다. [null]");

        return descriptor;
    }

    private static IDPSSODescriptor getIDPSSODescriptor(EntityDescriptor entityDescriptor)
            throws MainApplicationException {
        IDPSSODescriptor idpssoDescriptor = entityDescriptor
                .getIDPSSODescriptor("urn:oasis:names:tc:SAML:2.0:protocol");

        if(idpssoDescriptor == null)
            throw new MainApplicationException(ErrorCode.SYS_INTERNAL_SERVER_ERROR, "IDP SSO descriptor를 찾을 수 없습니다.");

        return idpssoDescriptor;
    }

    private static SingleSignOnService getIdpBinding(
            IDPSSODescriptor idpSsoDescriptor,
            SamlIdpBinding samlBinding) throws MainApplicationException {

        return idpSsoDescriptor
                .getSingleSignOnServices()
                .stream()
                .filter(x -> x.getBinding().equals(
                        "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-" + samlBinding.toString()))
                .findAny()
                .orElseThrow(() -> new MainApplicationException(ErrorCode.SYS_INTERNAL_SERVER_ERROR, "metadata에서 HTTP-POST SSO binding 정보를 찾을 수 없습니다."));
    }

    private static List<X509Certificate> getCertificates(IDPSSODescriptor idpSsoDescriptor)
            throws MainApplicationException {
        List<X509Certificate> certificates;

        try {
            certificates = idpSsoDescriptor
                    .getKeyDescriptors()
                    .stream()
                    .filter(x -> x.getUse() == UsageType.SIGNING)
                    .flatMap(SamlClient::getDatasWithCertificates)
                    .map(SamlClient::getFirstCertificate)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new MainApplicationException(ErrorCode.SYS_INTERNAL_SERVER_ERROR, "인증서를 가져오는 도중 에러가 발생했습니다.");
        }

        return certificates;
    }

    private static Stream<X509Data> getDatasWithCertificates(KeyDescriptor descriptor) {
        return descriptor
                .getKeyInfo()
                .getX509Datas()
                .stream()
                .filter(d -> d.getX509Certificates().size() > 0);
    }

    private static X509Certificate getFirstCertificate(X509Data data) {
        try {
            org.opensaml.xml.signature.X509Certificate cert =
                    data.getX509Certificates().stream().findFirst().orElse(null);

            if (cert != null) return KeyInfoHelper.getCertificate(cert);
        } catch(CertificateException e) {
            log.error("Exception in getFirstCertificate", e);
        }

        return null;
    }

    private static Credential getCredential(X509Certificate certificate) {
        BasicX509Credential credential = new BasicX509Credential();
        credential.setEntityCertificate(certificate);
        credential.setPublicKey(certificate.getPublicKey());
        credential.setCRLs(Collections.emptyList());
        return credential;
    }

    private static XMLObject buildSamlObject(QName qname) {
        return Configuration
                .getBuilderFactory()
                .getBuilder(qname)
                .buildObject(qname);
    }
}
