package edu.mayo.cimi.rdf.main;

import edu.mayo.cimi.rdf.auxiliary.FileUtils;
import edu.mayo.cimi.rdf.model.*;
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
import java.util.Vector;

/**
 * The server side implementation of the RPC service.
 */
public class TCGAServiceImpl
{
    private RDFStoreQueries kb = new RDFStoreQueries();
    private HashMap<String, TCGADomain> tags = new HashMap<String, TCGADomain>();
    private HashMap<String, CDE> cdes = new HashMap<String, CDE>();
    private HashMap<String, ObjectClass> objectClasses = new HashMap<String, ObjectClass>();
    private HashMap<String, ObjectProperty> objectProperties = new HashMap<String, ObjectProperty>();
    private HashMap<String, ValueDomain> valueDomains = new HashMap<String, ValueDomain>();


    public static void main(String arg[])
    {
        try
        {
            TCGAServiceImpl tcgaimpl = new TCGAServiceImpl();
            tcgaimpl.tags.clear();
            String xml = tcgaimpl.kb.getDistinctDomains();
            tcgaimpl.populateDomains(xml);

            int obi = 0;
            for (ObjectClass objcl : tcgaimpl.objectClasses.values())
            {
                System.out.println("Updaing:" + (++obi)+ " of " + tcgaimpl.objectClasses.size() + " Object Class\"" + objcl.prefName + "\"");

                xml = tcgaimpl.kb.getObjectClassDetails(objcl.prefName);
                tcgaimpl.updateObjectClass(objcl, xml);
            }

            System.out.println("Creating Report...");
            String[] header = new String[10];
            header[0] = "TCGA Domain Name";
            header[1] = "CDEs Used";
            header[2] = "Object Classes Used";
            header[3] = "Properties Used";
            header[4] = "Value Set Used";
            header[5] = "Enumerated VS";
            header[6] = "Total CDEs (context: object classes used)";
            header[7] = "Total Properties (context: object classes used)";
            header[8] = "Total Value Sets (context: object classes used)";
            header[9] = "Total Enumerated VS (context: object classes used)";

            String reportContent = ModelUtils.makeCSVRow(0, header) + "\n";

            int i=0;
            for (String key :tcgaimpl.tags.keySet())
               reportContent += ModelUtils.makeCSVRow(++i, tcgaimpl.getReportColumns(tcgaimpl.tags.get(key))) + "\n";

            FileUtils.createFileWithContents("TCGADomain.csv", reportContent);

            System.out.println("DONE DONE DONE !!!");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public TCGADomain populateCDEsForDomains(String domainName) throws ModelException
    {
        try
        {
            TCGADomain domain = new TCGADomain(domainName);

            String xml = kb.getCDEsForDomain(domainName);
            NodeList nl = getNodeList(xml);

            for (int i = 0; i < nl.getLength(); i++)
            {
                Node node = nl.item(i);
                String tag = getValue(node, "tag"); // This should be identical to domain name

                String study = getValue(node, "study");
                String pubId = getValue(node, "publicId");
                String longName = getValue(node, "longname");
                String cdeLn = getValue(node, "cdelongname");
                String defn = getValue(node, "definition");
                String ocln = getValue(node, "objClassLongName");
                String ocpn = getValue(node, "objClassPrefName");
                String propLn = getValue(node, "propLongName");
                String propPn = getValue(node, "propPrefName");
                String vdName = getValue(node, "valueDomainName");
                String vdType = getValue(node, "valueDomainType");

                /*System.out.println("Tag=" + domainName);
                System.out.println("Study=" + study);
                System.out.println("Public Id=" + pubId);
                System.out.println("Long Name=" + cdeLn);*/

                TCGADomainEntry entry = new TCGADomainEntry();
                CDE cde = cdes.get(ModelUtils.key(pubId));

                if (cde == null)
                {
                    cde = new CDE(pubId);
                    cde.name = longName;
                    cde.longName = cdeLn;
                    cde.definition = defn;

                    // Add Object Class Key

                    if (!ModelUtils.isNull(ocpn))
                    {
                        ObjectClass oc = objectClasses.get(ModelUtils.key(ocpn));

                        if (oc == null) {
                            oc = new ObjectClass(ocpn);
                            oc.longName = ocln;
                        }
                        oc.addCDE(cde.getId());
                        objectClasses.put(oc.getId(), oc);
                        cde.objectClassKey = oc.getId();
                    }

                    // Property Key
                    if (!ModelUtils.isNull(propPn))
                    {
                        ObjectProperty property = objectProperties.get(ModelUtils.key(propPn));

                        if (property == null) {
                            property = new ObjectProperty(propPn);
                            property.longName = propLn;
                        }
                        property.addCDE(cde.getId());

                        objectProperties.put(property.getId(), property);
                        cde.objectPropertyKey = property.getId();
                    }

                    ValueDomain vd = valueDomains.get(ModelUtils.key(vdName));
                    if (vd == null)
                    {
                        vd = new ValueDomain(vdName);
                        vd.isEnumerated = ("enumerated".equalsIgnoreCase(vdType.trim()));
                    }
                    valueDomains.put(vd.getId(), vd);

                    cde.valueDomainKey = vd.getId();

                    cdes.put(cde.getId(), cde);
                }

                entry.studyKey = study;
                entry.cdeKey = cde.getId();
                domain.add(entry);
            }
            return domain;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new ModelException(e.getMessage());
        }
    }

    public void updateObjectClass(ObjectClass objClass, String xml) throws ModelException
    {
        try
        {
            NodeList nl = getNodeList(xml);

            boolean isNewCDE = false;
            for (int i = 0; i < nl.getLength(); i++)
            {
                Node node = nl.item(i);
                String pubId = getValue(node, "publicId");
                String cdeLn = getValue(node, "cdelongname");
                String defn = getValue(node, "definition");
                String propLn = getValue(node, "propLongName");
                String propPn = getValue(node, "propPrefName");
                String vdName = getValue(node, "valueDomainName");
                String vdType = getValue(node, "valueDomainType");

                isNewCDE = false;
                CDE cde = cdes.get(ModelUtils.key(pubId));

                if (cde == null)
                {
                    cde = new CDE(pubId);
                    cde.name = cdeLn;
                    cde.longName = cdeLn;
                    cde.definition = defn;

                    objClass.addCDE(cde.getId());
                    cde.objectClassKey = objClass.getId();
                    isNewCDE = true;
                }

                // Property Key
                if (!ModelUtils.isNull(propPn))
                {
                    ObjectProperty property = objectProperties.get(ModelUtils.key(propPn));

                    if (property == null)
                    {
                        property = new ObjectProperty(propPn);
                        property.longName = propLn;
                    }

                    property.addCDE(cde.getId());

                    objectProperties.put(property.getId(), property);
                    cde.objectPropertyKey = property.getId();
                }

                ValueDomain vd = valueDomains.get(ModelUtils.key(vdName));
                if (vd == null)
                {
                    vd = new ValueDomain(vdName);
                    vd.isEnumerated = ("enumerated".equalsIgnoreCase(vdType.trim()));
                }

                valueDomains.put(vd.getId(), vd);

                cde.valueDomainKey = vd.getId();

                if (isNewCDE)
                    cdes.put(cde.getId(), cde);

                objClass.addCDE(cde.getId());
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new ModelException(e.getMessage());
        }
    }

    public void populateDomains(String xml) throws ModelException
    {
        try
        {
            NodeList nl = getNodeList(xml);
            System.out.println("Populating " + nl.getLength() + " Domains...");
            for (int i = 0; i < nl.getLength(); i++)
            {
                Node node = nl.item(i);
                String domainName = getValue(node, "tag");
                TCGADomain domain = tags.get(ModelUtils.key(domainName));

                if (domain == null)
                    domain = populateCDEsForDomains(domainName);
                else
                    continue;

                System.out.println("Found unique " + domain.getUniqueCDEsReferred().size() + " CDEs for Doamin \"" + domainName + "\"!");
                tags.put(domain.getId(), domain);
            }

            System.out.println("Population DONE!! Size = " + tags.size());
        }
        catch (Exception e)
        {
            System.out.println("Exception ONE!!!");
            e.printStackTrace();
            throw new ModelException(e.getMessage());
        }
    }

    private NodeList getNodeList(String xml) throws ModelException
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
            return (NodeList) result;
        }
        catch (Exception e)
        {
            System.out.println("Exception TWO!!!");
            e.printStackTrace();
            throw new ModelException(e.getMessage());
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

    public String[] getReportColumns(TCGADomain dom)
    {
        String[] cols = new String[10];

        Vector<String> cdeRefd =  dom.getUniqueCDEsReferred();
        Vector<String> ocRefd = new Vector<String>();
        Vector<String> prRefd = new Vector<String>();
        Vector<String> vdRefd = new Vector<String>();
        int enumerated = 0;

        for (String cdk : cdeRefd)
        {
            CDE cdeRf = cdes.get(cdk);

            if (!ocRefd.contains(cdeRf.objectClassKey))
                ocRefd.add(cdeRf.objectClassKey);

            if (!prRefd.contains(cdeRf.objectPropertyKey))
                prRefd.add(cdeRf.objectPropertyKey);

            if (!vdRefd.contains(cdeRf.valueDomainKey))
            {
                vdRefd.add(cdeRf.valueDomainKey);
                if (valueDomains.get(cdeRf.valueDomainKey).isEnumerated)
                    enumerated++;
            }
        }

        Vector<String> all_cdeRefd =  dom.getUniqueCDEsReferred();
        Vector<String> all_prRefd = new Vector<String>();
        Vector<String> all_vdRefd = new Vector<String>();
        int all_enumerated = 0;

        for (String obck : ocRefd)
        {
            for (String allCdk : objectClasses.get(obck).cdeKeys)
            {
                CDE cdeRf = cdes.get(allCdk);

                if (!all_cdeRefd.contains(allCdk))
                    all_cdeRefd.add(allCdk);

                if (!all_prRefd.contains(cdeRf.objectPropertyKey))
                    all_prRefd.add(cdeRf.objectPropertyKey);

                if (!all_vdRefd.contains(cdeRf.valueDomainKey))
                {
                    all_vdRefd.add(cdeRf.valueDomainKey);
                    if (valueDomains.get(cdeRf.valueDomainKey).isEnumerated)
                        all_enumerated++;
                }
            }
        }

        cols[0] = dom.domainName;
        cols[1] = "" + cdeRefd.size(); // Number of CDEs used for this domain
        cols[2] = "" + ocRefd.size(); // Number of object classes used
        cols[3] = "" + prRefd.size(); // Number of properties used
        cols[4] = "" + vdRefd.size(); // Number of Value Set used
        cols[5] = "" + enumerated; // Number of Enumerated Value Set used
        cols[6] = "" + all_cdeRefd.size(); // Total Number of CDEs (context: object classes used)
        cols[7] = "" + all_prRefd.size(); // Total Number of properties (context: object classes used)
        cols[8] = "" + all_vdRefd.size(); // Total Number of Value Set (context: object classes used)
        cols[9] = "" + all_enumerated; // Total Number of Enumerated Value Sets (context: object classes used)
        return cols;
    }
}
