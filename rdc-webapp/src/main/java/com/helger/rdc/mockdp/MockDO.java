package com.helger.rdc.mockdp;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.concurrent.ExecutorServiceHelper;
import com.helger.commons.concurrent.ThreadHelper;
import com.helger.commons.datetime.PDTFactory;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.io.ByteArrayWrapper;
import com.helger.commons.mime.CMimeType;
import com.helger.commons.state.ESuccess;
import com.helger.json.IJsonObject;
import com.helger.json.serialize.JsonWriterSettings;
import com.helger.rdc.api.RdcConfig;
import com.helger.rdc.api.RdcIdentifierFactory;
import com.helger.rdc.api.me.EMEProtocol;
import com.helger.rdc.api.me.incoming.IMEIncomingHandler;
import com.helger.rdc.api.me.incoming.MEIncomingException;
import com.helger.rdc.api.me.model.MEMessage;
import com.helger.rdc.api.rest.RDCPayload;
import com.helger.rdc.webapi.as4.ApiPostLookendAndSend;
import com.helger.xml.serialize.read.DOMReader;
import com.helger.xml.serialize.write.EXMLSerializeIndent;
import com.helger.xml.serialize.write.XMLWriter;
import com.helger.xml.serialize.write.XMLWriterSettings;

import eu.de4a.iem.jaxb.common.idtypes.LegalPersonIdentifierType;
import eu.de4a.iem.jaxb.common.types.CanonicalEvidenceType;
import eu.de4a.iem.jaxb.common.types.ErrorListType;
import eu.de4a.iem.jaxb.common.types.ErrorType;
import eu.de4a.iem.jaxb.common.types.RequestTransferEvidenceUSIIMDRType;
import eu.de4a.iem.jaxb.common.types.ResponseTransferEvidenceType;
import eu.de4a.iem.jaxb.w3.cv11.bc.LegalEntityLegalNameType;
import eu.de4a.iem.xml.de4a.DE4AMarshaller;
import eu.de4a.iem.xml.de4a.DE4AResponseDocumentHelper;
import eu.de4a.iem.xml.de4a.EDE4ACanonicalEvidenceType;
import eu.de4a.kafkaclient.DE4AKafkaClient;

public final class MockDO implements IMEIncomingHandler
{
  public static final AtomicBoolean DO_ACTIVE = new AtomicBoolean (true);
  private static final Logger LOGGER = LoggerFactory.getLogger (MockDO.class);

  @SuppressWarnings ("unused")
  @Nonnull
  private static ESuccess _handleDBARequest (@Nonnull final MEMessage aMessage, @Nonnull final Document aDoc)
  {
    LOGGER.info ("Handling as DBA request");

    final RequestTransferEvidenceUSIIMDRType aRequest = DE4AMarshaller.drImRequestMarshaller ().read (aDoc);
    if (aRequest == null)
    {
      DE4AKafkaClient.send (EErrorLevel.ERROR, "Passed request ist not a valid IM request");
      return ESuccess.FAILURE;
    }

    final String sExampleRequest = "<RequestTransferEvidence xmlns=\"http://www.de4a.eu/2020/data/requestor/pattern/intermediate\"\r\n" +
                                   "                         xmlns:de4a=\"http://www.de4a.eu/2020/commons/type\"\r\n" +
                                   "                         xmlns:de4aid=\"http://www.de4a.eu/2020/commons/identity/type\"\r\n" +
                                   "                         xmlns:eilp=\"http://eidas.europa.eu/attributes/legalperson\"\r\n" +
                                   "                         xmlns:einp=\"http://eidas.europa.eu/attributes/naturalperson\">\r\n" +
                                   "   <de4a:RequestId>696b5578-70e2-425c-86c9-bffab3226495</de4a:RequestId>\r\n" +
                                   "   <de4a:SpecificationId>SpecificationId</de4a:SpecificationId>\r\n" +
                                   "   <de4a:TimeStamp>2021-09-21T09:20:44.894Z</de4a:TimeStamp>\r\n" +
                                   "   <de4a:ProcedureId>ProcedureId</de4a:ProcedureId>\r\n" +
                                   "   <de4a:DataEvaluator>\r\n" +
                                   "      <de4a:AgentUrn>iso6523-actorid-upis::9999:nl000000024</de4a:AgentUrn>\r\n" +
                                   "      <de4a:AgentName>(RVO) Rijksdienst voor Ondernemend Nederland (Netherlands Enterprise Agency)</de4a:AgentName>\r\n" +
                                   "   </de4a:DataEvaluator>\r\n" +
                                   "   <de4a:DataOwner>\r\n" +
                                   "      <de4a:AgentUrn>iso6523-actorid-upis::9999:at000000271</de4a:AgentUrn>\r\n" +
                                   "      <de4a:AgentName>(BMDW) Bundesministerium für Digitalisierung und Wirtschaftsstandort</de4a:AgentName>\r\n" +
                                   "   </de4a:DataOwner>\r\n" +
                                   "   <de4a:DataRequestSubject>\r\n" +
                                   "      <de4a:DataSubjectCompany>\r\n" +
                                   "         <de4aid:LegalPersonIdentifier>AT/NL/???</de4aid:LegalPersonIdentifier>\r\n" +
                                   "         <de4aid:LegalName>Carl-Markus Piswanger e.U.</de4aid:LegalName>\r\n" +
                                   "      </de4a:DataSubjectCompany>\r\n" +
                                   "   </de4a:DataRequestSubject>\r\n" +
                                   "   <de4a:RequestGrounds>\r\n" +
                                   "      <de4a:ExplicitRequest>SDGR14</de4a:ExplicitRequest>\r\n" +
                                   "   </de4a:RequestGrounds>\r\n" +
                                   "   <de4a:CanonicalEvidenceTypeId>urn:de4a-eu:CanonicalEvidenceType::CompanyRegistration</de4a:CanonicalEvidenceTypeId>\r\n" +
                                   "</RequestTransferEvidence>";

    final LegalPersonIdentifierType aCompany = aRequest.getDataRequestSubject ().getDataSubjectCompany ();
    if (aCompany == null)
    {
      DE4AKafkaClient.send (EErrorLevel.ERROR, "No DRS company found");
      return ESuccess.FAILURE;
    }

    if (!"urn:de4a-eu:CanonicalEvidenceType::CompanyRegistration".equals (aRequest.getCanonicalEvidenceTypeId ()))
    {
      DE4AKafkaClient.send (EErrorLevel.ERROR,
                            "The CanonicalEvidenceType '" + aRequest.getCanonicalEvidenceTypeId () + "' is not supported");
      return ESuccess.FAILURE;
    }

    final String sExampleResponse = "<ResponseTransferEvidence xmlns=\"http://www.de4a.eu/2020/data/requestor/pattern/intermediate\"\r\n" +
                                    "                          xmlns:de4a=\"http://www.de4a.eu/2020/commons/type\"\r\n" +
                                    "                          xmlns:de4aid=\"http://www.de4a.eu/2020/commons/identity/type\"\r\n" +
                                    "                          xmlns:eilp=\"http://eidas.europa.eu/attributes/legalperson\"\r\n" +
                                    "                          xmlns:einp=\"http://eidas.europa.eu/attributes/naturalperson\">\r\n" +
                                    "   <de4a:RequestId>21ce328c-f614-43cc-a035-1e41552ad577</de4a:RequestId>\r\n" +
                                    "   <de4a:SpecificationId>SpecificationId</de4a:SpecificationId>\r\n" +
                                    "   <de4a:TimeStamp>2021-09-06T17:02:12.129Z</de4a:TimeStamp>\r\n" +
                                    "   <de4a:ProcedureId>ProcedureId</de4a:ProcedureId>\r\n" +
                                    "   <de4a:DataEvaluator>\r\n" +
                                    "      <de4a:AgentUrn>iso6523-actorid-upis::9915:de4atest</de4a:AgentUrn>\r\n" +
                                    "      <de4a:AgentName>(BMDW) Bundesministerium für Digitalisierung und Wirtschaftsstandort</de4a:AgentName>\r\n" +
                                    "   </de4a:DataEvaluator>\r\n" +
                                    "   <de4a:DataOwner>\r\n" +
                                    "      <de4a:AgentUrn>iso6523-actorid-upis::9999:nl990000106</de4a:AgentUrn>\r\n" +
                                    "      <de4a:AgentName>(KVK) Chamber of Commerce of Netherlands</de4a:AgentName>\r\n" +
                                    "   </de4a:DataOwner>\r\n" +
                                    "   <de4a:DataRequestSubject>\r\n" +
                                    "      <de4a:DataSubjectCompany>\r\n" +
                                    "         <de4aid:LegalPersonIdentifier>NL/AT/90000471</de4aid:LegalPersonIdentifier>\r\n" +
                                    "         <de4aid:LegalName>ELVILA SA</de4aid:LegalName>\r\n" +
                                    "      </de4a:DataSubjectCompany>\r\n" +
                                    "   </de4a:DataRequestSubject>\r\n" +
                                    "   <de4a:CanonicalEvidenceTypeId>urn:de4a-eu:CanonicalEvidenceType::CompanyRegistration</de4a:CanonicalEvidenceTypeId>\r\n" +
                                    "   <de4a:CanonicalEvidence>\r\n" +
                                    "      <dba:LegalEntity xmlns=\"http://www.de4a.eu/2020/data/owner/pattern/intermediate\"\r\n" +
                                    "                       xmlns:cvb=\"http://www.w3.org/ns/corevocabulary/BasicComponents\"\r\n" +
                                    "                       xmlns:dba=\"urn:eu-de4a:xsd:CanonicalEvidenceType::CompanyRegistration:v0.6\">\r\n" +
                                    "         <dba:CompanyName>\r\n" +
                                    "            <cvb:LegalEntityLegalName>Regional Tris-ice Coöperatie</cvb:LegalEntityLegalName>\r\n" +
                                    "         </dba:CompanyName>\r\n" +
                                    "         <dba:CompanyType>coöp</dba:CompanyType>\r\n" +
                                    "         <dba:CompanyStatus>economically active</dba:CompanyStatus>\r\n" +
                                    "         <dba:CompanyActivity>\r\n" +
                                    "            <dba:NaceCode>7500</dba:NaceCode>\r\n" +
                                    "         </dba:CompanyActivity>\r\n" +
                                    "         <dba:RegistrationDate>1980-09-18</dba:RegistrationDate>\r\n" +
                                    "         <dba:CompanyEUID>NLNHR.90000471</dba:CompanyEUID>\r\n" +
                                    "         <dba:CompanyContactData>\r\n" +
                                    "            <dba:Email>nepemailadres@kvk.nl</dba:Email>\r\n" +
                                    "            <dba:Telephone>+31 0209999999</dba:Telephone>\r\n" +
                                    "         </dba:CompanyContactData>\r\n" +
                                    "         <dba:RegisteredAddress>\r\n" +
                                    "            <dba:Thoroughfare>Leverkruidweg</dba:Thoroughfare>\r\n" +
                                    "            <dba:LocationDesignator>379</dba:LocationDesignator>\r\n" +
                                    "            <dba:PostCode>1508 WN</dba:PostCode>\r\n" +
                                    "            <dba:PostName>Zaandam</dba:PostName>\r\n" +
                                    "            <dba:AdminUnitL1>NL</dba:AdminUnitL1>\r\n" +
                                    "         </dba:RegisteredAddress>\r\n" +
                                    "         <dba:PostalAddress>\r\n" +
                                    "            <dba:Thoroughfare>Leverkruidweg</dba:Thoroughfare>\r\n" +
                                    "            <dba:LocationDesignator>379</dba:LocationDesignator>\r\n" +
                                    "            <dba:PostCode>1508 WN</dba:PostCode>\r\n" +
                                    "            <dba:PostName>Zaandam</dba:PostName>\r\n" +
                                    "            <dba:AdminUnitL1>NL</dba:AdminUnitL1>\r\n" +
                                    "         </dba:PostalAddress>\r\n" +
                                    "      </dba:LegalEntity>\r\n" +
                                    "   </de4a:CanonicalEvidence>\r\n" +
                                    "</ResponseTransferEvidence>";

    final ResponseTransferEvidenceType aResponse = DE4AResponseDocumentHelper.createResponseTransferEvidence (aRequest);

    if (!DO_ACTIVE.get ())
    {
      // TODO error message
      DE4AKafkaClient.send (EErrorLevel.ERROR, "We cannot reach our DO - oooooohhhhh :(");
      final ErrorListType aErrorList = new ErrorListType ();
      final ErrorType aError = new ErrorType ();
      aError.setCode ("67890");
      aError.setText ("Our DO is not reachable - please try again later");
      aErrorList.addError (aError);
      aResponse.setErrorList (aErrorList);
    }
    else
      if (!"AT/NL/???".equals (aCompany.getLegalPersonIdentifier ()))
      {
        // TODO error message
        DE4AKafkaClient.send (EErrorLevel.ERROR,
                              "The DRS company identifier '" + aCompany.getLegalPersonIdentifier () + "' is not supported");
        final ErrorListType aErrorList = new ErrorListType ();
        final ErrorType aError = new ErrorType ();
        aError.setCode ("12345");
        aError.setText ("The eIDAS identifier '" + aCompany.getLegalPersonIdentifier () + "' is unknown");
        aErrorList.addError (aError);
        aResponse.setErrorList (aErrorList);
      }
      else
      {
        DE4AKafkaClient.send (EErrorLevel.INFO,
                              "The DRS company identifier '" + aCompany.getLegalPersonIdentifier () + "' was found - building result");

        // Copy whatever needs to be copied
        final CanonicalEvidenceType aCE = new CanonicalEvidenceType ();
        {
          final eu.de4a.iem.jaxb.t42.v0_6.LegalEntityType p = new eu.de4a.iem.jaxb.t42.v0_6.LegalEntityType ();
          {
            final eu.de4a.iem.jaxb.t42.v0_6.NamesType a = new eu.de4a.iem.jaxb.t42.v0_6.NamesType ();
            final LegalEntityLegalNameType aLegalName = new LegalEntityLegalNameType ();
            aLegalName.setValue ("Bla Blub GmbH");
            a.setLegalEntityLegalName (aLegalName);
            p.addCompanyName (a);
          }
          p.setCompanyType ("GmbH");
          p.setCompanyStatus ("active");
          {
            final eu.de4a.iem.jaxb.t42.v0_6.ActivityType a = new eu.de4a.iem.jaxb.t42.v0_6.ActivityType ();
            a.addNaceCode ("1234");
            p.setCompanyActivity (a);
          }
          p.setRegistrationDate (PDTFactory.getCurrentLocalDate ().minusDays (1000));
          p.setCompanyEUID ("AT98765");
          {
            final eu.de4a.iem.jaxb.t42.v0_6.AddressType a = new eu.de4a.iem.jaxb.t42.v0_6.AddressType ();
            a.setThoroughfare ("Wien");
            a.setPostCode ("1010");
            a.setPoBox ("543");
            a.setAdminUnitL1 ("Austria");
            p.addRegisteredAddress (a);
          }
          aCE.setAny (eu.de4a.iem.xml.de4a.t42.v0_6.DE4AT42Marshaller.legalEntity ().getAsDocument (p).getDocumentElement ());
        }
        aResponse.setCanonicalEvidence (aCE);
      }

    final DE4AMarshaller <ResponseTransferEvidenceType> aMarshaller = DE4AMarshaller.drImResponseMarshaller (EDE4ACanonicalEvidenceType.T42_COMPANY_INFO_V06);
    LOGGER.info ("Message to be send back:\n" + aMarshaller.setFormattedOutput (true).getAsString (aResponse));

    _waitAndRunAsync (aMessage, aMarshaller.getAsBytes (aResponse));

    return ESuccess.SUCCESS;
  }

  private static void _waitAndRunAsync (@Nonnull final MEMessage aMessage, @Nonnull final byte [] aBytes)
  {
    final ExecutorService aES = Executors.newFixedThreadPool (1);
    aES.submit ( () -> {
      // Ensure sync response is received first
      ThreadHelper.sleep (1000);

      // Start new transmission
      final ICommonsList <RDCPayload> aPayloads = new CommonsArrayList <> ();
      final RDCPayload a = new RDCPayload ();
      a.setValue (aBytes);
      a.setMimeType (CMimeType.APPLICATION_XML.getAsString ());
      a.setContentID ("ResponseTransferEvidence");
      aPayloads.add (a);

      // Swap sender and receiver
      // Different response type
      final IJsonObject aJson = ApiPostLookendAndSend.perform (aMessage.getReceiverID (),
                                                               aMessage.getSenderID (),
                                                               aMessage.getDocumentTypeID (),
                                                               RdcConfig.getIdentifierFactory ()
                                                                        .createProcessIdentifier (RdcIdentifierFactory.PROCESS_SCHEME,
                                                                                                  "response"),
                                                               EMEProtocol.AS4.getTransportProfileID (),
                                                               aPayloads);
      LOGGER.info ("Sending result:\n" + aJson.getAsJsonString (JsonWriterSettings.DEFAULT_SETTINGS_FORMATTED));
    });
    ExecutorServiceHelper.shutdownAndWaitUntilAllTasksAreFinished (aES);
  }

  private static void _handleXML (@Nonnull final MEMessage aMessage,
                                  @Nullable final String sNamespaceURL,
                                  @Nullable final String sLocalName,
                                  @Nonnull final Document aDoc)
  {
    boolean bHandled = false;

    if ("RequestTransferEvidence".equals (sLocalName) &&
        "http://www.de4a.eu/2020/data/requestor/pattern/intermediate".equals (sNamespaceURL))
    {
      // DBA request at DT/DO
      if (_handleDBARequest (aMessage, aDoc).isSuccess ())
      {
        bHandled = true;
      }
    }
    else
      if ("ResponseTransferEvidence".equals (sLocalName) &&
          "http://www.de4a.eu/2020/data/requestor/pattern/intermediate".equals (sNamespaceURL))
      {
        // DBA response at DR/DT
      }

    if (!bHandled)
    {
      // Do something with it
      LOGGER.info ("Received XML:\n" +
                   XMLWriter.getNodeAsString (aDoc, new XMLWriterSettings ().setIndent (EXMLSerializeIndent.INDENT_AND_ALIGN)));
    }
  }

  static void handleIncomingRequest (@Nonnull final MEMessage aMessage, @Nonnull final ByteArrayWrapper aBytes)
  {
    LOGGER.info ("Now trying to read the message as XML");
    final Document aDoc = DOMReader.readXMLDOM (aBytes.bytes (), aBytes.getOffset (), aBytes.size ());
    if (aDoc == null)
    {
      DE4AKafkaClient.send (EErrorLevel.ERROR,
                            "Failed to read content as XML. Content as UTF-8:\n" +
                                               new String (aBytes.bytes (), aBytes.getOffset (), aBytes.size (), StandardCharsets.UTF_8));
    }
    else
    {
      final String sNamespaceURL = aDoc.getDocumentElement ().getNamespaceURI ();
      final String sLocalName = aDoc.getDocumentElement ().getLocalName ();
      LOGGER.info ("Received a document with declaration '{" + sNamespaceURL + "}" + sLocalName + "'");

      _handleXML (aMessage, sNamespaceURL, sLocalName, aDoc);
    }
  }

  public void handleIncomingRequest (@Nonnull final MEMessage aMessage) throws MEIncomingException
  {
    if (aMessage.payloads ().size () >= 2)
    {
      // The first one is the RegRep stupidity
      final ByteArrayWrapper p = aMessage.payloads ().get (1).getData ();
      handleIncomingRequest (aMessage, p);
    }
    else
    {
      DE4AKafkaClient.send (EErrorLevel.ERROR, "Incoming message seems to be ill-formatted - too few payloads. Trying first one.");
      final ByteArrayWrapper p = aMessage.payloads ().get (0).getData ();
      handleIncomingRequest (aMessage, p);
    }
  }
}
