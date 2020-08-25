package com.utilities;

import com.sap.aii.mapping.api.*;
import com.sap.aii.mapping.lookup.*;
import com.sap.engine.interfaces.messaging.api.MessageDirection;
import com.sap.engine.interfaces.messaging.api.MessageKey;
import com.sap.engine.interfaces.messaging.api.PublicAPIAccessFactory;
import com.sap.engine.interfaces.messaging.api.auditlog.AuditAccess;
import com.sap.engine.interfaces.messaging.api.auditlog.AuditLogStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

@SuppressWarnings("unused")
public class MapsUtilities implements DinamicConfKeys2 {
    public XmlUtil xuUtil = new XmlUtil();
    public Map map = null;
    public TransformationInput inputTr;
    public InputStream input;
    public OutputStream output;
    public TransformationOutput outputTr;
    public SystemAccessor accessor;
    public String sMessageID;
    private AbstractTrace trace = null;

    public MapsUtilities(TransformationInput input, TransformationOutput output) {
        this.inputTr = input;
        this.outputTr = output;
        this.sMessageID = input.getInputHeader().getMessageId();
        // rbResource = PropertyResourceBundle.getBundle( sMAP_PROPERTIES );
    }

    public MapsUtilities(TransformationInput input) {
        this.inputTr = input;
        this.sMessageID = input.getInputHeader().getMessageId();
    }

    public MapsUtilities() {
    }

    public InputStream getInpuStream() {
        return this.inputTr.getInputPayload().getInputStream();
    }

    public OutputStream getOutpuStream() {
        return this.outputTr.getOutputPayload().getOutputStream();
    }


    public Document soapLookUp(Document docReq, String sChannelName)
            throws LookupException {
        // Returns an abstract trace object for writing trace message to
        // monitoring

        trace = (AbstractTrace) map
                .get(StreamTransformationConstants.MAPPING_TRACE);
        trace.addInfo("SoapLookUp is started..");

        Document docRes = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        factory.setNamespaceAware(false);
        factory.setValidating(false);

        try {
            docRes = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .newDocument();
        } catch (Exception e) {
            e.getMessage();
        }

        // Convertir de Document a String
        String SOAPxml = xuUtil.getStringFromDocument(docReq);

        trace.addInfo("Processing the request..");

        // Llamar al Canal SOAP para procesar la peticion
        try {
            Channel channel = inputTr.getInputParameters().getChannel(
                    sChannelName);
            accessor = LookupService.getSystemAccessor(channel);
            trace.addInfo("Soap Channel detected..");
        } catch (Exception e) {
            trace.addInfo("Error calling the Soap Channel..");
            e.getMessage();
        }
        InputStream inputSt = new ByteArrayInputStream(SOAPxml.getBytes());
        XmlPayload payload = LookupService.getXmlPayload(inputSt);
        try {
            builder = factory.newDocumentBuilder();
        } catch (Exception e) {
            e.getMessage();
        }

        // Ejecutando el Lookup
        Payload result = null;

        try {
            result = accessor.call(payload);
            trace.addInfo("SoapLookUp correctly executed..");
        } catch (LookupException e) {
            // Close accessor
            if (accessor != null) {
                try {
                    trace.addInfo("Error in receiveing the response..");
                    accessor.close();
                } catch (LookupException ex) {
                    ex.getMessage();
                }
            }
        }

        // Parseando Respuesta SOAP
        try {
            docRes = builder.parse(result.getContent());
            trace.addInfo("Parsing the response..");
        } catch (Exception e) {
            e.getMessage();
        }

        trace.addInfo("Returning the response..");
        return docRes;
    }

    // public Document soapLookUp(Document docReq, Channel channel ) throws
    // LookupException
    // {
    // // Returns an abstract trace object for writing trace message to
    // monitoring
    //
    // // trace = (AbstractTrace)
    // map.get(StreamTransformationConstants.MAPPING_TRACE );
    // // trace.addInfo("SoapLookUp is started..");
    //
    // Document docRes = null;
    // DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    // DocumentBuilder builder = null;
    // factory.setNamespaceAware( false );
    // factory.setValidating( false );
    //
    //
    // try
    // {
    // docRes =
    // DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    // }
    // catch ( Exception e )
    // {
    // e.getMessage() ;
    // }
    //
    // // Convertir de Document a String
    // String SOAPxml = xuUtil.getStringFromDocument(docReq);
    //
    // trace.addInfo("Processing the request..");
    //
    // // Llamar al Canal SOAP para procesar la peticion
    // try
    // {
    // accessor = LookupService.getSystemAccessor( channel );
    // // trace.addInfo("Soap Channel detected..");
    // }
    // catch( Exception e )
    // {
    // // trace.addInfo("Error calling the Soap Channel..");
    // e.getMessage();
    // }
    // InputStream inputSt = new ByteArrayInputStream(SOAPxml.getBytes());
    // XmlPayload payload = LookupService.getXmlPayload(inputSt);
    // try
    // {
    // builder = factory.newDocumentBuilder();
    // }
    // catch( Exception e )
    // {
    // e.getMessage();
    // }
    //
    // // Ejecutando el Lookup
    // Payload result = null;
    //
    // try
    // {
    // result = accessor.call( payload );
    // // trace.addInfo("SoapLookUp correctly executed..");
    // }
    // catch( LookupException e )
    // {
    // //Close accessor
    // if( accessor != null )
    // {
    // try
    // {
    // // trace.addInfo("Error in receiveing the response..");
    // accessor.close();
    // }
    // catch( LookupException ex )
    // {
    // ex.getMessage();
    // }
    // }
    // }
    //
    // // Parseando Respuesta SOAP
    // try
    // {
    // docRes = builder.parse( result.getContent() );
    // // trace.addInfo("Parsing the response..");
    // }
    // catch( Exception e )
    // {
    // e.getMessage();
    // }
    //
    // // trace.addInfo("Returning the response..");
    // return docRes;
    // }

    public Document soapLookUp(Document docReq, Channel channel)
            throws LookupException, Exception {
        // Returns an abstract trace object for writing trace message to
        // monitoring

        // trace = (AbstractTrace)
        // map.get(StreamTransformationConstants.MAPPING_TRACE );
        // trace.addInfo("SoapLookUp is started..");

        Document docRes = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        factory.setNamespaceAware(false);
        factory.setValidating(false);
        docRes = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .newDocument();

        // Convertir de Document a String
        String SOAPxml = xuUtil.getStringFromDocument(docReq);

        // getTrace().addInfo("Processing the request..");

        // Llamar al Canal SOAP para procesar la peticion

        // getTrace().addInfo("invocando il WS con " + SOAPxml);
        accessor = LookupService.getSystemAccessor(channel);
        // trace.addInfo("Soap Channel detected..");

        InputStream inputSt = new ByteArrayInputStream(SOAPxml.getBytes());
        XmlPayload payload = LookupService.getXmlPayload(inputSt);

        builder = factory.newDocumentBuilder();

        // Ejecutando el Lookup
        Payload result = null;

        result = accessor.call(payload);
        // getTrace().addInfo("SoapLookUp correctly executed..");
        // Close accessor
        if (accessor != null) {

            // getTrace().addInfo("Error in receiveing the response..");
            accessor.close();

        }

        // Parsing Risposta SOAP

        // docRes = builder.parse(result.getContent());
        // getTrace().addInfo(
        // "Parsing the response.." + "docRes = "
        // + xuUtil.getStringFromDocument(docRes));

        // getTrace().addInfo("Returning the response..");
        return docRes;
    }

    public String getDynamicConf(String sKey) {
        DynamicConfiguration dconf = inputTr.getDynamicConfiguration();
        if (sKey.equalsIgnoreCase("FileName") == true)
            return dconf.get(KEY_FILENAME);
        else if (sKey.equalsIgnoreCase("ConversationId") == true)
            return dconf.get(KEY_CONVERSATION_ID);
        else if (sKey.equalsIgnoreCase("Interface") == true)
            return dconf.get(KEY_INTERFACE);
        else if (sKey.equalsIgnoreCase("InterfaceNamespace") == true)
            return dconf.get(KEY_INTERFACE_NAMESPACE);
        else if (sKey.equalsIgnoreCase("MessageId") == true)
            return dconf.get(KEY_MESSAGE_ID);
        else if (sKey.equalsIgnoreCase("ProcessingMode") == true)
            return dconf.get(KEY_PROCESSING_MODE);
        else if (sKey.equalsIgnoreCase("QualityOfService") == true)
            return dconf.get(KEY_QUALITY_OF_SERVICE);
        else if (sKey.equalsIgnoreCase("QueueId") == true)
            return dconf.get(KEY_QUEUE_ID);
        else if (sKey.equalsIgnoreCase("ReceiverService"))
            return dconf.get(KEY_RECEIVER_SERVICE);
        else if (sKey.equalsIgnoreCase("RefToMessageId") == true)
            return dconf.get(KEY_REF_TO_MESSAGE_ID);
        else if (sKey.equalsIgnoreCase("SenderService") == true)
            return dconf.get(KEY_SENDER_SERVICE);
        else if (sKey.equalsIgnoreCase("TimeSent"))
            return dconf.get(KEY_TIME_SENT);
        else if (sKey.equalsIgnoreCase("Directory"))
            return dconf.get(KEY_DIRECTORY);
        else if (sKey.equalsIgnoreCase("FileEncoding") == true)
            return dconf.get(KEY_FILE_ENCODING);
        else if (sKey.equalsIgnoreCase("FileType"))
            return dconf.get(KEY_FILE_TYPE);
        else if (sKey.equalsIgnoreCase("SourceFileSize") == true)
            return dconf.get(KEY_FILE_SIZE);
        else if (sKey.equalsIgnoreCase("SourceFTPHost") == true)
            return dconf.get(KEY_FTP_HOST);
        else if (sKey.equalsIgnoreCase("SourceFileTimestamp") == true)
            return dconf.get(KEY_SOURCE_FILE_TIME_STAMP);
        else if (sKey.equalsIgnoreCase("HTTPDest") == true)
            return dconf.get(KEY_HTTP_DEST);
        else if (sKey.equalsIgnoreCase("TargetURL") == true)
            return dconf.get(KEY_TARGET_URL_HTTP);
        else if (sKey.equalsIgnoreCase("SHeaderCC") == true)
            return dconf.get(KEY_S_HEADER_CC);
        else if (sKey.equalsIgnoreCase("SHeaderFROM") == true)
            return dconf.get(KEY_S_HEADER_FROM);
        else if (sKey.equalsIgnoreCase("SHeaderSUBJECT") == true)
            return dconf.get(KEY_S_HEADER_SUBJECT);
        else if (sKey.equalsIgnoreCase("RfcDestination") == true)
            return dconf.get(KEY_RFC_DESTINATION);
        else if (sKey.equalsIgnoreCase("SRemoteHost") == true)
            return dconf.get(KEY_REMOTE_HOST);
        else if (sKey.equalsIgnoreCase("SRemoteUser") == true)
            return dconf.get(KEY_REMOTE_USER);
        else if (sKey.equalsIgnoreCase("TAuthKey") == true)
            return dconf.get(KEY_AUTH_KEY);
        else
            return "not found";
    }

    public void addNewDynamicConf(String sNamespace, String sKey, String sNewVal) {
        DynamicConfigurationKey key = DynamicConfigurationKey.create(
                sNamespace, sKey);
        DynamicConfiguration dconf = inputTr.getDynamicConfiguration();
        dconf.put(key, sNewVal);
    }

    public void setDynamicConf(String sKey, String sNewVal) {
        DynamicConfiguration dconf = inputTr.getDynamicConfiguration();
        if (sKey.compareToIgnoreCase("FileName") == 1)
            dconf.put(KEY_FILENAME, sNewVal);
        else if (sKey.compareToIgnoreCase("ConversationId") == 1)
            dconf.put(KEY_CONVERSATION_ID, sNewVal);
        else if (sKey.compareToIgnoreCase("Interface") == 1)
            dconf.put(KEY_INTERFACE, sNewVal);
        else if (sKey.compareToIgnoreCase("InterfaceNamespace") == 1)
            dconf.put(KEY_INTERFACE_NAMESPACE, sNewVal);
        else if (sKey.compareToIgnoreCase("MessageId") == 1)
            dconf.put(KEY_MESSAGE_ID, sNewVal);
        else if (sKey.compareToIgnoreCase("ProcessingMode") == 1)
            dconf.put(KEY_PROCESSING_MODE, sNewVal);
        else if (sKey.compareToIgnoreCase("QualityOfService") == 1)
            dconf.put(KEY_QUALITY_OF_SERVICE, sNewVal);
        else if (sKey.compareToIgnoreCase("QueueId") == 1)
            dconf.put(KEY_QUEUE_ID, sNewVal);
        else if (sKey.compareToIgnoreCase("ReceiverService") == 1)
            dconf.put(KEY_RECEIVER_SERVICE, sNewVal);
        else if (sKey.compareToIgnoreCase("RefToMessageId") == 1)
            dconf.put(KEY_REF_TO_MESSAGE_ID, sNewVal);
        else if (sKey.compareToIgnoreCase("SenderService") == 1)
            dconf.put(KEY_SENDER_SERVICE, sNewVal);
        else if (sKey.compareToIgnoreCase("TimeSent") == 1)
            dconf.put(KEY_TIME_SENT, sNewVal);
        else if (sKey.compareToIgnoreCase("Directory") == 1)
            dconf.put(KEY_DIRECTORY, sNewVal);
        else if (sKey.compareToIgnoreCase("FileEncoding") == 1)
            dconf.put(KEY_FILE_ENCODING, sNewVal);
        else if (sKey.compareToIgnoreCase("FileType") == 1)
            dconf.put(KEY_FILE_TYPE, sNewVal);
        else if (sKey.compareToIgnoreCase("SourceFileSize") == 1)
            dconf.put(KEY_FILE_SIZE, sNewVal);
        else if (sKey.compareToIgnoreCase("SourceFTPHost") == 1)
            dconf.put(KEY_FTP_HOST, sNewVal);
        else if (sKey.compareToIgnoreCase("SourceFileTimestamp") == 1)
            dconf.put(KEY_SOURCE_FILE_TIME_STAMP, sNewVal);
        else if (sKey.compareToIgnoreCase("HTTPDest") == 1)
            dconf.put(KEY_HTTP_DEST, sNewVal);
        else if (sKey.compareToIgnoreCase("TargetURL") == 1)
            dconf.put(KEY_TARGET_URL_HTTP, sNewVal);
        else if (sKey.compareToIgnoreCase("SHeaderCC") == 1)
            dconf.put(KEY_S_HEADER_CC, sNewVal);
        else if (sKey.compareToIgnoreCase("SHeaderFROM") == 1)
            dconf.put(KEY_S_HEADER_FROM, sNewVal);
        else if (sKey.compareToIgnoreCase("SHeaderSUBJECT") == 1)
            dconf.put(KEY_S_HEADER_SUBJECT, sNewVal);
        else if (sKey.compareToIgnoreCase("RfcDestination") == 1)
            dconf.put(KEY_RFC_DESTINATION, sNewVal);
        else if (sKey.compareToIgnoreCase("SRemoteHost") == 1)
            dconf.put(KEY_REMOTE_HOST, sNewVal);
        else if (sKey.compareToIgnoreCase("SRemoteUser") == 1)
            dconf.put(KEY_REMOTE_USER, sNewVal);
        else if (sKey.compareToIgnoreCase("TAuthKey") == 1)
            dconf.put(KEY_AUTH_KEY, sNewVal);
    }

    public String getInboundParam(String sInbParam) {
        return inputTr.getInputParameters().getString(sInbParam);
    }

    public String getRunTimeConstants(String sRunConst) {
        if (sRunConst.compareToIgnoreCase("SENDER_SERVICE") == 1)
            return (String) map
                    .get(StreamTransformationConstants.SENDER_SERVICE);
        else if (sRunConst.compareToIgnoreCase("INTERFACE") == 1)
            return (String) map.get(StreamTransformationConstants.INTERFACE);
        else if (sRunConst.compareToIgnoreCase("INTERFACE_NAMESPACE") == 1)
            return (String) map
                    .get(StreamTransformationConstants.INTERFACE_NAMESPACE);
        else if (sRunConst.compareToIgnoreCase("RECEIVER_SERVICE") == 1)
            return (String) map
                    .get(StreamTransformationConstants.RECEIVER_SERVICE);
        else
            return "";
    }

    public Vector<byte[]> getAttachByte() {
        Vector<byte[]> vAttachVect = new Vector<byte[]>();
        InputAttachments inputAttachments = this.inputTr.getInputAttachments();
        if (inputAttachments.areAttachmentsAvailable()) {
            Collection<String> idCollection = inputAttachments
                    .getAllContentIds(true);
            Object[] arrayObj = idCollection.toArray();
            for (int i = 0; i < arrayObj.length; i++) {
                vAttachVect.addElement(inputAttachments.getAttachment(
                        (String) arrayObj[i]).getContent());
            }
        }
        return vAttachVect;
    }

    public Vector<String> getAttachBase64String() {
        Vector<String> vAttachVect = new Vector<String>();
        InputAttachments inputAttachments = inputTr.getInputAttachments();
        if (inputAttachments.areAttachmentsAvailable()) {
            Collection<String> idCollection = inputAttachments
                    .getAllContentIds(true);
            Object[] arrayObj = idCollection.toArray();
            for (int i = 0; i < arrayObj.length; i++) {
                vAttachVect.addElement(inputAttachments.getAttachment(
                        (String) arrayObj[i]).getBase64EncodedContent());
            }
        }
        return vAttachVect;
    }

    public Vector<String> getAttachBase64String(TransformationInput inputTras) {
        Vector<String> vAttachVect = new Vector<String>();
        InputAttachments inputAttachments = inputTras.getInputAttachments();
        if (inputAttachments.areAttachmentsAvailable()) {
            Collection<String> idCollection = inputAttachments
                    .getAllContentIds(true);
            Object[] arrayObj = idCollection.toArray();
            for (int i = 0; i < arrayObj.length; i++) {
                vAttachVect.addElement(inputAttachments.getAttachment(
                        (String) arrayObj[i]).getBase64EncodedContent());
            }
        }
        return vAttachVect;
    }

    public static void setOutboundAttach(TransformationOutput output,
                                         byte[] bAttach, String filename, String mimeType) {
        OutputAttachments outputAttachments = output.getOutputAttachments();
        Attachment attachments = outputAttachments.create(filename,
                mimeType /* ( ad esempio "application/pdf") */, bAttach);
        outputAttachments.setAttachment(attachments);
    }

    public Document fileContentConversion(ArrayList<String> sHeaderFields,
                                          ArrayList<String> sBodyFields, ArrayList<String> sTrailerFields,
                                          String sElementSeparator, String sFieldSeparator) {
        /**
         * Questo metodo sostituisce il file content conversion di un file
         * adapter: si utilizza solo in caso di conversione di un flat file (con
         * un header, un body e opzionalmente un trailer), dati i nomi dei campi
         * da convertire
         */

        InputStream is = inputTr.getInputPayload().getInputStream();
        Document docOutput = null;
        try {
            docOutput = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().newDocument();
        } catch (Exception e) {
            e.getMessage();
        }
        Node nHeader = xuUtil.createNode(docOutput, "Header");
        Node nBody = xuUtil.createNode(docOutput, "Body");
        Node nTrailer = xuUtil.createNode(docOutput, "Trailer");
        Node nFile = xuUtil.createNode(docOutput, "File");
        Node nField;

        String sFile = xuUtil.inputStream2StringConverter(is);

        StringTokenizer stElement = new StringTokenizer(sFile,
                sElementSeparator);
        String sField = "";
        int iElemnum = stElement.countTokens();
        short iFieldCount = 0;
        short iElemCount = 0;
        if (iElemnum == 2) {
            while (stElement.hasMoreTokens()) {
                if (iElemCount == 0) {
                    sField = stElement.nextToken();
                    StringTokenizer stField = new StringTokenizer(sField,
                            sFieldSeparator);
                    while (stField.hasMoreTokens()) {

                        nField = xuUtil.createNode(docOutput, sHeaderFields
                                .get(iFieldCount), stField.nextToken());
                        nHeader.appendChild(nField);
                        iFieldCount++;
                    }
                }

                else if (iElemCount == 1) {
                    sField = stElement.nextToken();
                    StringTokenizer stField = new StringTokenizer(sField,
                            sFieldSeparator);
                    while (stField.hasMoreTokens()) {

                        nField = xuUtil.createNode(docOutput, sBodyFields
                                .get(iFieldCount), stField.nextToken());
                        nBody.appendChild(nField);
                        iFieldCount++;
                    }

                }
                iFieldCount = 0;
                iElemCount++;
            }
        }
        if (iElemnum == 3) {
            while (stElement.hasMoreTokens()) {
                if (iElemCount == 0) {
                    sField = stElement.nextToken();
                    StringTokenizer stField = new StringTokenizer(sField,
                            sFieldSeparator);
                    while (stField.hasMoreTokens()) {
                        nField = xuUtil.createNode(docOutput, sHeaderFields
                                .get(iFieldCount), stField.nextToken());
                        nHeader.appendChild(nField);
                        iFieldCount++;
                    }
                }

                else if (iElemCount == 1) {
                    sField = stElement.nextToken();
                    StringTokenizer stField = new StringTokenizer(sField,
                            sFieldSeparator);
                    while (stField.hasMoreTokens()) {

                        nField = xuUtil.createNode(docOutput, sBodyFields
                                .get(iFieldCount), stField.nextToken());
                        nBody.appendChild(nField);
                        iFieldCount++;
                    }

                } else if (iElemCount == 2) {
                    sField = stElement.nextToken();
                    StringTokenizer stField = new StringTokenizer(sField,
                            sFieldSeparator);
                    while (stField.hasMoreTokens()) {

                        nField = xuUtil.createNode(docOutput, sTrailerFields
                                .get(iFieldCount), stField.nextToken());
                        nTrailer.appendChild(nField);
                        iFieldCount++;
                    }

                }
                iFieldCount = 0;
                iElemCount++;
            }
        }
        nFile.appendChild(nHeader);
        nFile.appendChild(nBody);
        nFile.appendChild(nTrailer);
        docOutput.appendChild(nFile);
        return docOutput;
    }

    public boolean schemaValidator(Document docIn, String sProperties)
            throws SAXException {

        /**
         * I parametri che accetta questo metodo sono i seguenti:
         *
         * docIn: documento XML che si vuole validare sProperties: va il path
         * del package ove risiede il file di properties (includendo anche il
         * nome del file di properties), esempio
         * "com.PosteIT.proof.XmlValidate")
         *
         * IMPORTANTE: il file XSD indicato nel file di properties va incluso
         * nello stesso path della classe MapsUtilities, non nello stesso path
         * del file di properties!
         */
        boolean bValid = false;
        String sMAP_PROPERTIES = sProperties;
        ResourceBundle rbResource = PropertyResourceBundle
                .getBundle(sMAP_PROPERTIES);
        Schema schema = null;

        Source schemaFile = new StreamSource(this.getClass()
                .getResourceAsStream(rbResource.getString("RESOURCE.name")));

        SchemaFactory factory = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        try {
            schema = factory.newSchema(schemaFile);
        } catch (SAXException e) {
            e.printStackTrace();
        }
        Validator validator = schema.newValidator();
        // validate the DOM tree
        try {
            // trace.addInfo("Validando contra la estructura de entrada...");
            validator.validate(new DOMSource(docIn));
            // trace.addInfo("El Xml de la factura de entrada respeta el
            // XSD...");
            bValid = true;
        } catch (SAXException e) {
            bValid = false;
            // instance document is invalid!
            // trace.addWarning("El Xml de la factura de entrada NO respeta el
            // XSD...");
            throw (new SAXException(
                    "La factura de entrada no respeta el XSD estandar - "
                            + e.getMessage()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bValid;
    }

    public void removeInputAttachmentFromOutboundMessage()
    {
        OutputAttachments outputAttach = outputTr.getOutputAttachments();
        Vector<String> vAttachVect = new Vector<String>();
        InputAttachments inputAttachments = inputTr.getInputAttachments();
        if (inputAttachments.areAttachmentsAvailable()) {
            Collection<String> idCollection = inputAttachments.getAllContentIds(true);
            Object[] arrayObj = idCollection.toArray();
            for (int i = 0; i < arrayObj.length; i++) {
                outputAttach.removeAttachment((String) arrayObj[i]);
            }
        }
    }

    public void executeAudit(String errorMsg)
    // ricordarsi di importare nell'ESR il com.sap.aii.af.ms.ifc_api.jar
    {
        // Il message ID deve essere del tipo: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx, quindi se non lo è
        //va convertito
        MessageKey key = null;
        AuditAccess audit = null;
        try {
            audit = PublicAPIAccessFactory.getPublicAPIAccess()
                    .getAuditAccess();
            key = new MessageKey(sCheckAndConvertMessID(this.sMessageID), MessageDirection.OUTBOUND);
            audit.addAuditLogEntry(key, AuditLogStatus.WARNING, errorMsg);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

    }

    public static void executeAudit(String errorMsg, String sMessID)
    // ricordarsi di importare nell'ESR il com.sap.aii.af.ms.ifc_api.jar
    {
        // Il message ID deve essere del tipo: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx, quindi se non lo è
        //va convertito
        MessageKey key = null;
        AuditAccess audit = null;
        try {
            audit = PublicAPIAccessFactory.getPublicAPIAccess()
                    .getAuditAccess();
            key = new MessageKey(sCheckAndConvertMessID(sMessID), MessageDirection.OUTBOUND);
            audit.addAuditLogEntry(key, AuditLogStatus.WARNING, errorMsg);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

    }
    public static String sCheckAndConvertMessID(String sMessID)
    {
        if(sMessID.length() < 35)
            return sMessID.substring(0, 8) + "-" +  sMessID.substring(8, 8+4) + "-" +  sMessID.substring(8+4, 8+8) + "-" +  sMessID.substring(8+8, 8+12) + "-" +  sMessID.substring(20);
        else
            return sMessID;
    }

}


