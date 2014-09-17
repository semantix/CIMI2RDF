package edu.mayo.cimi.rdf.main;

import edu.mayo.cimi.rdf.auxiliary.ConnectionInfo;
import edu.mayo.cimi.rdf.auxiliary.Store;

import java.net.MalformedURLException;

/**
 * Created by dks02 on 9/12/14.
 */
public class RDFStoreQueries
{
    private static Store s_store = null;


    public String getDomainsAndCDEs(String filter)
    {
        String query = getPrefixes() +
                "select distinct ?tag ?study ?publicId ?longname" +
                "{" +
                "    GRAPH <http://tcga.nci.nih.gov/cde> " +
                "    {" +
                "       ?dataelement tcga:name ?longname ." +
                "       ?dataelement tcga:cde ?publicId ." +
                "       OPTIONAL {?dataelement tcga:studies ?studies ." +
                "       ?studies tcga:study ?study . }" +
                "       ?dataelement tcga:tags ?tags ." +
                "       ?tags tcga:tag ?tag ." +
                "     }" +
                "}";

        return getQueryResults(query);
    }

    public String getDictionaries()
    {
        String query = getPrefixes() +
                "SELECT ?publicId WHERE {" +
                " ?cde cde:PublicId ?publicId ." +
                "} LIMIT 10";

        String query2 = "PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>   " +
                "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>  " +
                "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>  " +
                "PREFIX owl:<http://www.w3.org/2002/07/owl#>  " +
                "    PREFIX tcga:<http://tcga.nci.nih.gov/BCR/DataDictionary#> " +
                "PREFIX cde:<http://rdf.cadsr.org/cde#> " +
                "" +
                "select distinct ?publicId ?cdelongname ?definition ?objClassLongName ?objClassPrefName ?propLongName ?propPrefName ?valueDomainName ?valueDomainType" +
                "  { " +
                "    GRAPH <http://tcga.nci.nih.gov/cde> " +
                "     {" +
                "      ?dataelement tcga:name ?longname ." +
                "      ?dataelement tcga:cde ?publicId ." +
                "      OPTIONAL {?dataelement tcga:studies ?studies ." +
                "      ?studies tcga:study ?study . }" +
                "      ?dataelement tcga:tags ?tags ." +
                "      ?tags tcga:tag ?tag ." +
                "      FILTER (?tag=\"clinical shared\")" +
                "      }" +
                "    GRAPH <http://rdf.cadsr.org/cde> " +
                "    {" +
                "       ?cde cde:PUBLICID ?publicId ." +
                "       ?cde cde:CONTEXTNAME ?context ." +
                "      ?cde cde:LONGNAME ?cdelongname ." +
                "      ?cde cde:PREFERREDDEFINITION ?definition ." +
                "      ?cde cde:DATAELEMENTCONCEPT ?deConcept ." +
                "      ?deConcept cde:ObjectClass ?objectClass ." +
                "      ?objectClass cde:LongName ?objClassLongName ." +
                "      ?objectClass cde:PreferredName ?objClassPrefName ." +
                "      ?deConcept cde:Property ?property ." +
                "      ?property cde:LongName ?propLongName ." +
                "      ?property cde:PreferredName ?propPrefName ." +
                "      ?cde cde:VALUEDOMAIN ?valuedomain ." +
                "      ?valuedomain cde:ValueDomainType ?valueDomainType ." +
                "      ?valuedomain cde:LongName ?valueDomainName ." +
                "" +
                "     }" +
                "   " +
                "}";


        return getQueryResults(query2);
    }

    public static String PREFIXES = null;

    public static String getPrefixes()
    {
        if (PREFIXES == null)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("PREFIX xsd:<http://www.w3.org/2001/XMLSchema#> ");
            sb.append("PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> ");
            sb.append("PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> ");
            sb.append("PREFIX owl:<http://www.w3.org/2002/07/owl#> ");

            sb.append("PREFIX tcga:<http://tcga.nci.nih.gov/BCR/DataDictionary#> ");
            sb.append("PREFIX cde:<http://rdf.cadsr.org/cde#> ");

            PREFIXES = sb.toString();
        }
        return PREFIXES;
    }

    private String getQueryResults(String query)
    {
        Store store = getStore();

        try
        {
            return store.query(query, Store.OutputFormat.SPARQL_XML);
        }
        catch (Exception e)
        {
            System.out.println("Error in query:\n" + query);
            System.out.println("**************************************");
            System.out.println("Error retrieving categories from 4store. " + e);
        }

        return null;
    }

    public static Store getStore()
    {
        if (s_store == null)
        {
            try
            {
                s_store = new Store(ConnectionInfo.STORE_URL);
            }
            catch (MalformedURLException e)
            {
                System.out.println("Error creating new 4store. " + e.toString() );
            }
        }
        return s_store;
    }
}
