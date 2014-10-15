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
import java.io.File;
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

            Vector<String> uniqueCDEsUsedInAllDomains = new Vector<String>();

            for (TCGADomain dom : tcgaimpl.tags.values())
                for (TCGADomainEntry entry : dom.entries.values())
                    if (!uniqueCDEsUsedInAllDomains.contains(entry.cdeKey))
                        uniqueCDEsUsedInAllDomains.add(entry.cdeKey);

            System.out.println("Unique TCGA Domains = " + tcgaimpl.tags.keySet().size());
            System.out.println("Unique CDEs (for all Domains) = " + uniqueCDEsUsedInAllDomains.size());
            System.out.println("Unique Total CDEs = " + tcgaimpl.cdes.keySet().size());
            System.out.println("Unique Object Classes = " + tcgaimpl.objectClasses.keySet().size());
            System.out.println("Unique Properties = " + tcgaimpl.objectProperties.keySet().size());
            System.out.println("Unique Value Domains = " + tcgaimpl.valueDomains.keySet().size());

            int evdcount = 0;
            for (ValueDomain vds : tcgaimpl.valueDomains.values())
                if (vds.isEnumerated)
                    evdcount++;

            System.out.println("Unique Enumerated Value Domains = " + evdcount);



            String turtleFileName = "TCGA_DataElements.ttl";
            System.out.println("Creating Turtle File (TTL)" + turtleFileName);


            tcgaimpl.createTTLFile(turtleFileName);

            // Creates report for all domains.
            tcgaimpl.createDomainReport("TCGADomain.csv");
            tcgaimpl.createDomainMatrix("Matrix_Domain_OCLS.csv", ReportType.OBJECTCLASSES);
            tcgaimpl.createDomainMatrix("Matrix_Domain_PROP.csv", ReportType.PROPERTIES);
            tcgaimpl.createDomainMatrix("Matrix_Domain_VD.csv", ReportType.VALUEDOMAINS);
            tcgaimpl.createDomainMatrix("Matrix_Domain_EVD.csv", ReportType.ENUMVALUEDOMAINS);

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
                String vdDataType = getValue(node, "vdDataType");

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
                    cde.longName = cdeLn.replaceAll("java.lang.String", "");
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
                        addObjectClass(oc);
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

                        addObjectProperty(property);
                        cde.objectPropertyKey = property.getId();
                    }

                    ValueDomain vd = valueDomains.get(ModelUtils.key(vdName));
                    if (vd == null)
                    {
                        vd = new ValueDomain(vdName);
                        vd.isEnumerated = ("enumerated".equalsIgnoreCase(vdType.trim()));

                    }

                    vd.vdDatatype = vdDataType;
                    vd.addUsage(cde.objectClassKey);

                    addValueDomain(vd);

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

    private void addObjectProperty(ObjectProperty property) throws ModelException
    {
        if (toSkip(property.getId()))
            return;

        property.suffixIfNameRepeated = checkDuplicates(property.getId());

        objectProperties.put(property.getId(), property);
    }

    private void addObjectClass(ObjectClass oc) throws ModelException
    {
        if (toSkip(oc.getId()))
            return;

        oc.suffixIfNameRepeated = checkDuplicates(oc.getId());

        objectClasses.put(oc.getId(), oc);
    }

    private void addValueDomain(ValueDomain vd) throws ModelException
    {
        if (toSkip(vd.getId()))
            return;

        vd.suffixIfNameRepeated = checkDuplicates(vd.getId());

        valueDomains.put(vd.getId(), vd);
    }

    private boolean toSkip(String id)
    {
      return ((id == null)||
          (id.startsWith("java"))||
              (id.startsWith("xsd"))||
              (id.equals("OBJECT")));
    }

    private int checkDuplicates(String id) throws ModelException
    {
        int howmanytimes = 0;

//        if (objectClasses.containsKey(id))
//            howmanytimes++;
//
//        if (objectProperties.containsKey(id))
//            howmanytimes++;
//
//        if (valueDomains.containsKey(id))
//            howmanytimes++;

        return howmanytimes;
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
                String vdDataType = getValue(node, "vdDatatype");

                isNewCDE = false;
                CDE cde = cdes.get(ModelUtils.key(pubId));

                if (cde == null)
                {
                    cde = new CDE(pubId);
                    cde.name = cdeLn;
                    cde.longName = cdeLn.replaceAll("java.lang.String", "");
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

                    addObjectProperty(property);
                    cde.objectPropertyKey = property.getId();
                }

                ValueDomain vd = valueDomains.get(ModelUtils.key(vdName));
                if (vd == null)
                {
                    vd = new ValueDomain(vdName);
                    vd.isEnumerated = ("enumerated".equalsIgnoreCase(vdType.trim()));
                }

                vd.addUsage(cde.objectClassKey);
                vd.vdDatatype = vdDataType;

                addValueDomain(vd);

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

                System.out.println("Found unique " + domain.getUniqueCDEsReferenced().size() + " CDEs for Doamin \"" + domainName + "\"!");
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

            if (xml == null) {
                System.out.println("HERE");
                return null;
            }

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

    public String[] getAllDomainsReportColumns(TCGADomain dom)
    {
        String[] cols = new String[10];

        Vector<String> cdeRefd =  dom.getUniqueCDEsReferenced();
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
                if (valueDomains.get(cdeRf.valueDomainKey) == null)
                    continue;

                if (valueDomains.get(cdeRf.valueDomainKey).isEnumerated)
                    enumerated++;
            }
        }

        Vector<String> all_cdeRefd =  dom.getUniqueCDEsReferenced();
        Vector<String> all_prRefd = new Vector<String>();
        Vector<String> all_vdRefd = new Vector<String>();
        int all_enumerated = 0;

        for (String obck : ocRefd)
        {
            if (objectClasses.get(obck) == null)
                continue;

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

                    if (valueDomains.get(cdeRf.valueDomainKey) == null)
                        continue;

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

    public void createDomainReport(String fileName)
    {
        try {
            System.out.println("Creating Report: " + fileName);
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

            String reportContent = ModelUtils.makeCSVRow(0, header, false) + "\n";

            int i = 0;
            for (String key : tags.keySet())
                reportContent += ModelUtils.makeCSVRow(++i, getAllDomainsReportColumns(tags.get(key)), false) + "\n";

            FileUtils.createFileWithContents(fileName, reportContent);
        }
        catch(Exception e)
        {
            e.printStackTrace();

        }
    }

    public enum ReportType
    {
        OBJECTCLASSES,
        PROPERTIES,
        VALUEDOMAINS,
        ENUMVALUEDOMAINS
    }

    public void createDomainMatrix(String fileName, ReportType reportType)
    {
        try
        {
            System.out.println("Creating Report: " + fileName);
            int rows = tags.keySet().size() + 1;
            int cols = 0;

            Vector<String> enumeratedVDKeys = new Vector<String>();
            switch (reportType)
            {
                case OBJECTCLASSES: cols = objectClasses.size() + 1; break;
                case PROPERTIES: cols = objectProperties.size() + 1;break;
                case VALUEDOMAINS: cols = valueDomains.size() + 1;break;
                case ENUMVALUEDOMAINS:
                    for (String vdKey : valueDomains.keySet())
                        if (valueDomains.get(vdKey).isEnumerated)
                            enumeratedVDKeys.add(vdKey);

                    cols = enumeratedVDKeys.size() + 1; break;
            }

            String[][] matrix = new String[rows][cols];

            matrix[0][0] = "TCGA Domain Name";

            int i = 0;
            for (String tag: tags.keySet())
                matrix[++i][0] = tag;

            int j = 0;
            switch (reportType)
            {
                case OBJECTCLASSES: for(String obck : objectClasses.keySet())
                                        matrix[0][++j] = obck;
                                    break;
                case PROPERTIES: for(String obpr : objectProperties.keySet())
                                            matrix[0][++j] = obpr;
                                        break;
                case VALUEDOMAINS:  for(String vdk : valueDomains.keySet())
                                        matrix[0][++j] = vdk;
                                    break;
                case ENUMVALUEDOMAINS: for(String evdk : enumeratedVDKeys)
                                            matrix[0][++j] = evdk;
                                       break;
            }


            for (int m=1; m < rows; m++)
            {
                TCGADomain dom = tags.get(matrix[m][0]);

                Vector<String> cdeRefd = dom.getUniqueCDEsReferenced();
                Vector<String> ocRefd = new Vector<String>();
                Vector<String> prRefd = new Vector<String>();
                Vector<String> vdRefd = new Vector<String>();

                for (String cdk : cdeRefd)
                {
                    CDE cdeRf = cdes.get(cdk);

                    if (!ocRefd.contains(cdeRf.objectClassKey))
                        ocRefd.add(cdeRf.objectClassKey);

                    if (!prRefd.contains(cdeRf.objectPropertyKey))
                        prRefd.add(cdeRf.objectPropertyKey);

                    if (!vdRefd.contains(cdeRf.valueDomainKey))
                        vdRefd.add(cdeRf.valueDomainKey);
                }

                for (int n = 1; n < cols; n++)
                {
                    switch (reportType)
                    {
                        case OBJECTCLASSES: matrix[m][n] = (ocRefd.contains(matrix[0][n])) ? "1" : "0";
                            break;
                        case PROPERTIES: matrix[m][n] = (prRefd.contains(matrix[0][n])) ? "1" : "0";
                            break;
                        case VALUEDOMAINS:
                        case ENUMVALUEDOMAINS: matrix[m][n] = (vdRefd.contains(matrix[0][n])) ? "1" : "0";
                            break;
                    }

                }
            }

            for (i=1; i < rows; i++)
                matrix[i][0] = tags.get(matrix[i][0]).domainName;

            for (j=1; j < cols; j++)
            {
                switch (reportType)
                {
                    case OBJECTCLASSES: matrix[0][j] = objectClasses.get(matrix[0][j]).longName + "(" + objectClasses.get(matrix[0][j]).prefName + ")";
                        break;
                    case PROPERTIES: matrix[0][j] = objectProperties.get(matrix[0][j]).longName + "(" + objectProperties.get(matrix[0][j]).prefName + ")";
                        break;
                    case VALUEDOMAINS:
                    case ENUMVALUEDOMAINS: matrix[0][j] = valueDomains.get(matrix[0][j]).name + ((valueDomains.get(matrix[0][j]).isEnumerated)?"(Enumerated)": "");
                        break;
                }

            }

            String reportContent = "";

            for (i=0; i < rows;i++)
                reportContent += ModelUtils.makeCSVRow(i, matrix[i], true) + "\n";

            FileUtils.createFileWithContents(fileName, reportContent);
        }
        catch(Exception e)
        {
            e.printStackTrace();

        }
    }

    private Vector<String> printOCs = new Vector<String>();
    private Vector<String> printPrs = new Vector<String>();
    private Vector<String> printVDs = new Vector<String>();
    private Vector<String> printCDEs = new Vector<String>();

    public void createTTLFile(String fileName) throws ModelException
    {
        String prefixFileName = "TCGA_TTL_PREFIX.ttl";

        try {
            File prefFl = new File(prefixFileName);
            String pref = FileUtils.getContents(prefFl);

            StringBuffer allContent = new StringBuffer(pref + "\n");

            StringBuffer OCcontent = new StringBuffer("\n");


            //Pattern 1
            StringBuffer domainContent = new StringBuffer("");

            // Top Root node
            StringBuffer TCGADomainRootNodeText = new StringBuffer("\n<http://tcga.nci.nih.gov/BCR/DataDictionary#TCGADomain>" +
                    "\nrdf:type cimi:ITEM_GROUP ;" +
                    "\nrdf:type mms:DataElement ;" +
                    "\nrdfs:label \"TCGADomain\"^^xsd:string ;" +
                    "\nskos:definition \"TCGADomain\"^^xsd:string ;" +
                    "\nmms:dataElementDescription \"TCGADomain\"^^xsd:string ;" +
                    "\nmms:dataElementLabel \"TCGADomain\"^^xsd:string ;" +
                    "\nmms:dataElementName \"TCGADomain\"^^xsd:string ;" +
                    "\nmms:dataElementType \"TCGADomain\"^^xsd:string ;");

            for (TCGADomain dom : tags.values())
            {
                domainContent.append(dom.getTTL());
                Vector<String> uniqueCDEsForthisdomain = new Vector<String>();
                Vector<String> uniqueOCssForthisdomain = new Vector<String>();

                for (TCGADomainEntry entry : dom.entries.values())
                {
                     //domainContent.append("\ncimi:ITEM_GROUP.item cacde:" + cdes.get(entry.cdeKey).getRDFName() + " ;");

                    if (!uniqueCDEsForthisdomain.contains(entry.cdeKey))
                        uniqueCDEsForthisdomain.add(entry.cdeKey);

                    if (!uniqueOCssForthisdomain.contains(cdes.get(entry.cdeKey).objectClassKey))
                        uniqueOCssForthisdomain.add(cdes.get(entry.cdeKey).objectClassKey);
                }

                //domainContent.append("\n.");

                if (!uniqueCDEsForthisdomain.isEmpty())
                {
                    //domainContent.append(dom.getTTL());

                    for (String cdeK : uniqueCDEsForthisdomain)
                    {
                        CDE cdei = cdes.get(cdeK);

                        if (cdei == null)
                        {
                            System.out.println("CDE is null for " + cdeK);
                            continue;
                        }

                        domainContent.append("\ncimi:ITEM_GROUP.item cacde:" + cdei.getRDFName() + " ;");
                    }
                }

                //domainContent.append("\n.");

                if (!uniqueOCssForthisdomain.isEmpty())
                {
                    //domainContent.append(dom.getTTL());

                    for (String ockey : uniqueOCssForthisdomain)
                    {
                        ObjectClass oci = objectClasses.get(ockey);

                        if (oci == null)
                        {
                            System.out.println("OC is null for " + ockey);
                            continue;
                        }

                        domainContent.append("\ncimi:ITEM_GROUP.item cacde:" + oci.getRDFName() + " ;");
                    }
                }

                domainContent.append("\nmms:context tcga:TCGADomain ;");
                domainContent.append("\n.");
                TCGADomainRootNodeText.append("\ncimi:ITEM_GROUP.item tcga:" + dom.getRDFName() + ";");
            }

            TCGADomainRootNodeText.append("\n.");
            domainContent = TCGADomainRootNodeText.append(domainContent.toString());

            StringBuffer cdeContent = new StringBuffer("");
            for (CDE cde : cdes.values())
            {
                cdeContent.append(cde.getTTL());
            }

            StringBuffer propertyContent = new StringBuffer("");
            StringBuffer vdContent = new StringBuffer("");

            for (ObjectClass oc : objectClasses.values())
            {
                HashMap<String, Vector<String>> vdKeysInContext = new HashMap<String, Vector<String>>();
                //if (!printOCs.contains(oc.getId()))
                //{
                    OCcontent.append("\n" + oc.getTTL());

                    for (String cdk : oc.cdeKeys)
                    {
                        String propKey = cdes.get(cdk).objectPropertyKey;
                        if (propKey == null)
                            continue;

                        ObjectProperty prop = objectProperties.get(propKey);

                        if (prop == null)
                            continue;

                        Vector<String> vdsInc = vdKeysInContext.get(propKey);
                        if (vdsInc == null)
                            vdsInc = new Vector<String>();

                        if (!vdsInc.contains(cdes.get(cdk).valueDomainKey))
                            vdsInc.add(cdes.get(cdk).valueDomainKey);

                        vdKeysInContext.put(propKey, vdsInc);
                        OCcontent.append("\ncimi:ITEM_GROUP.item cacde:" + prop.getRDFName() + " ;");
                    }

                    OCcontent.append("\n.");
                    //printOCs.add(oc.getId());
                //}

                for (String pk : vdKeysInContext.keySet())
                {
                    //if (!printPrs.contains(pk))
                    //{
                        ObjectProperty op = objectProperties.get(pk);
                        boolean isEnum = true;
                        String contextOC = oc.getRDFName();
                        Vector<String> vdrefs = vdKeysInContext.get(pk);
                        ValueDomain[] vdrs = new ValueDomain[vdrefs.size()];

                        int i=0;
                        for (String vdk : vdrefs)
                            vdrs[i++] = valueDomains.get(vdk);


                        propertyContent.append(op.getTTL(contextOC, vdrs));
                        if (!printPrs.contains(pk))
                            printPrs.add(pk);
                    //}
                }
            }

            for (ObjectProperty op : objectProperties.values())
            {
                if (!printPrs.contains(op.getId()))
                {
                    boolean isEnum = true;
                    String contextOC = null;
                    String vdKey = null;

                    for (String cdKey : op.cdeKeys)
                    {
                        CDE cd = cdes.get(cdKey);
                        if (cd == null)
                            continue;

                        vdKey = cd.valueDomainKey;
                        contextOC = objectClasses.get(cd.objectClassKey).getRDFName();
                        break;
                    }

                    ValueDomain[] vds = {valueDomains.get(vdKey)};
                    propertyContent.append(op.getTTL(contextOC, vds));
                    printPrs.add(op.getId());
                }
            }



            for (ValueDomain vdom : valueDomains.values())
            {
                if (!printVDs.contains(vdom.getId()))
                {
                    vdContent.append(vdom.getTTL());

                    for (String usage : vdom.usageByObjectClasses)
                        vdContent.append("\nmms:context cacde:" + objectClasses.get(usage).getRDFName() + " ;");

                    vdContent.append("\n.");
                    printVDs.add(vdom.getId());
                }
            }

            // Pattern 1
            allContent.append(domainContent).append(cdeContent);

            //Pattern 2
            allContent.append("\n\n")
                    .append(OCcontent)
                    .append(propertyContent)
                    .append(vdContent);

            FileUtils.createFileWithContents(fileName, allContent.toString());
        }
        catch(Exception e)
        {
            System.out.println("Error in creating TTL File." + e.getMessage() + "\n\n");
            e.printStackTrace();
        }
    }
}
