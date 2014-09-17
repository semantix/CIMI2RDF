package edu.mayo.cimi.rdf.main;

import edu.mayo.cimi.rdf.model.CDE;
import edu.mayo.cimi.rdf.model.ModelException;
import edu.mayo.cimi.rdf.model.TCGADomain;
import edu.mayo.cimi.rdf.model.TCGADomainEntry;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.util.HashMap;

/**
 * The server side implementation of the RPC service.
 */
public class TCGAServiceImpl
{
    private RDFStoreQueries kb = new RDFStoreQueries();
    private HashMap<String, TCGADomain> tags = new HashMap<String, TCGADomain>();
    private HashMap<String, CDE> cdes = new HashMap<String, CDE>();


    public static void main(String arg[])
    {
        TCGAServiceImpl tcgaimpl = new TCGAServiceImpl();
        tcgaimpl.CreateDomainAndCDEMap();
    }

    public void CreateDomainAndCDEMap()
    {
        String xml = kb.getDomainsAndCDEs(null);
        tags.clear();
        populateDomains(xml);
    }

    public void populateDomains(String xml)
    {
        try
        {
            DocumentBuilderFactory dbf =
                    DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xml));

            Document doc = db.parse(is);

            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();

            XPathExpression expr = xpath.compile( "//results/result" );
            Object result = expr.evaluate( doc, XPathConstants.NODESET );
            NodeList nl = (NodeList) result;
            System.out.println("length=" + nl.getLength());
            for(int j=0 ; j < nl.getLength() ; j++)
            {
                Node node = (Node) nl.item(j);

                String pubId = getValue(node, "publicId");
                String cdeLn = getValue(node, "longname");
                String study = getValue(node, "study");
                String domainName = getValue(node, "tag");

                System.out.println("Tag=" + domainName);
                System.out.println("Study=" + study);
                System.out.println("Public Id=" + pubId);
                System.out.println("Long Name=" + cdeLn);

                TCGADomainEntry entry = new TCGADomainEntry();
                entry.cdeId = pubId;
                entry.studyId = study;

                if (!cdes.containsKey(ModelUtils.key(pubId)))
                {
                    CDE cde = new CDE(pubId);
                    cde.longName = cdeLn;
                    cdes.put(cde.getId(), cde);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private String getValue(Node node1, String attrib) throws ModelException
    {
        if (node1 == null)
            throw new ModelException("Node is null in getValue()");

        try
        {
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            String evalString = ".//binding[@name='" + attrib + "']/literal/node()";
            Node tagN = (Node) xpath.evaluate(evalString, node1, XPathConstants.NODE);
            return ((tagN != null) ? (tagN.getNodeValue()) : null);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new ModelException(e.getMessage());
        }
    }
}
