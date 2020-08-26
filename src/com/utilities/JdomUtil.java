package com.utilities;

import org.apache.commons.collections4.IteratorUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.Iterator;
import java.util.List;

public class JdomUtil {

    public JdomUtil()
    {
    }

    //Converte un Document JDOM in string
    public static String ConvertJDOM(Document doc)
    {
        Format format = Format.getPrettyFormat();
        //Format format = Format.getCompactFormat();
        // Creamos el serializador con el formato deseado
        XMLOutputter xmloutputter = new XMLOutputter(format);
        // Serializamos el document parseado
        String docStr = xmloutputter.outputString(doc);
        return docStr;
    }

    public static String ConvertJDOM(Document doc, String sEncoding)
    {
        Format format = Format.getCompactFormat();
        format.setEncoding(sEncoding);
        format.setTextMode(Format.TextMode.PRESERVE);
        //Format format = Format.getCompactFormat();
        // Creamos el serializador con el formato deseado
        XMLOutputter xmloutputter = new XMLOutputter(format);
        // Serializamos el document parseado
        String docStr = xmloutputter.outputString(doc);
        return docStr;
    }

    public static String ConvertJDOM(Element element)
    {
        //Format format = Format.getPrettyFormat();
        Format format = Format.getCompactFormat();
        // Creamos el serializador con el formato deseado
        XMLOutputter xmloutputter = new XMLOutputter(format);
        // Serializamos el document parseado
        String docStr = xmloutputter.outputString(element);
        return docStr;
    }

    //Metodo che restituisce il nome del nodo radice
    public static String RecongnizePattern(Document xml)
    {
        String sPAttern = null;
        Element eRoot = xml.getRootElement();
        sPAttern = eRoot.getName();
        return sPAttern;
    }

    // Metodo per la creazione di un document a partire da un InputStream
    public static Document createJDOM(InputStream input) throws JDOMException, IOException
    {
        //creating JDOM SAX parser
        SAXBuilder builder = new SAXBuilder();
        //reading XML document
        Document xml = null;
        xml = builder.build(input);
        return xml;
    }

    // Scrive il DocOutput in un OutputStream
    public static void createDocOutputStream(Document docOut, OutputStream outputStream) throws IOException
    {
        Format format = Format.getPrettyFormat();
        // Creamos el serializador con el formato deseado
        XMLOutputter xmloutputter = new XMLOutputter(format);
        // Serializamos el document parseado
        xmloutputter.output(docOut, outputStream);
    }

    // Metodo analogo al getDocumentElementByTagName nella DOM: restituiste un Array di Element
    public static Object[] getDocumentElementByTagName(Document doc, String sPattern)
    {
        ElementFilter fFilter = new ElementFilter(sPattern);
        Iterator<Element> iIter = doc.getDescendants(fFilter);
        return IteratorUtils.toArray(iIter, Element.class); // Restituisce un Element[]
    }
    public static Object[] getDocumentElementByTagName(Element ele, String sPattern)
    {
        ElementFilter fFilter = new ElementFilter(sPattern);
        Iterator<Element> iIter = ele.getDescendants(fFilter);
        return IteratorUtils.toArray(iIter, Element.class); // Restituisce un Element[]
    }
    public static Document StringToDocument(String sIn) throws JDOMException, IOException
    {
        SAXBuilder builder = new SAXBuilder();
        InputStream stream = new ByteArrayInputStream(sIn.getBytes("UTF-8"));
        return builder.build(stream);
    }
    public static Element StringToElement(String xmlstr) throws JDOMException, IOException, ParserConfigurationException, SAXException
    {
        StringReader stringReader = new StringReader(xmlstr);
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(stringReader);
        Element elem = doc.getRootElement();
        return elem.detach();
    }
    public static String lookUp(List<Element> elem, String qualfkey, String qualfvalue,
                         String field) {
        String sResult = null;

        for (Element item : elem) {
            if (item.getChildTextTrim(qualfkey).equals(qualfvalue.trim()))
            {
                sResult = item.getChildTextTrim(field);
                break;
            }
        }

        return (sResult);
    }
    public static String lookUp(List<Element> elem, String qualfkey1, String qualfvalue1, String qualfkey2, String qualfvalue2,String field) {
        String sResult = null;

        for (Element item : elem) {
            if (item.getChildTextTrim(qualfkey1).equals(qualfvalue1.trim()) && item.getChildTextTrim(qualfkey2).equals(qualfvalue2.trim()) )
            {
                sResult = item.getChildTextTrim(field);
                break;
            }
        }

        return (sResult);
    }
    public static String addSoapEnvelop(Element sDocIn, Element sHeader, String sNamespacePrefix, String sNamespaceUri){

        Element eDocIn = null, eHeaderIn = null;
        Element eBody = new Element("Body");
        Element eSoapEnvelope = new Element("Envelope");
        eBody.addContent(sDocIn);

        eSoapEnvelope.addContent(sHeader);

        Namespace nsSoap = Namespace.getNamespace("soapenv", "http://schemas.xmlsoap.org/soap/envelope/");
        sHeader.setNamespace(nsSoap);

        eSoapEnvelope.setNamespace(nsSoap);
        eBody.setNamespace(nsSoap);
        eSoapEnvelope.addContent(eBody);
        if(sNamespaceUri != null)
        {
            Namespace ns = Namespace.getNamespace(sNamespacePrefix, sNamespaceUri);
            eSoapEnvelope.addNamespaceDeclaration(ns);
        }

        Document docOut = new Document(eSoapEnvelope);
        return ConvertJDOM(docOut, "utf-8");

    }

    public static String addSoapEnvelop(Element sDocIn, Element sHeader){

        return addSoapEnvelop(sDocIn, sHeader, null, null);

    }

    public static Element removeSoapEnvelope(String response, String sNodeName)
    {
        return removeSoapEnvelope(response, sNodeName, null, null);
    }

    public static Element removeSoapEnvelope(String response, String sNodeName, String sPrefix, String sNamespace)
    {
        Namespace ns = Namespace.getNamespace(sPrefix, sNamespace);
        Document docInput = null;
        Element eResponse = null;
        try {
            docInput = StringToDocument(response);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Element eRoot = docInput.detachRootElement();

        eResponse = ((Element) getDocumentElementByTagName(eRoot, sNodeName)[0]).detach();

        if(sNamespace != null)
        {
            eResponse.setNamespace(ns);
        }

        return eResponse;
    }

}


