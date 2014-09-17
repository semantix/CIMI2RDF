package edu.mayo.cimi.rdf.main;

import junit.framework.TestCase;

public class TCGAServiceImplTest extends TestCase
{
    public void testGetDomainsAndCDEs()
    {
        RDFStoreQueries storeQueries = new RDFStoreQueries();
        String domainNCdes = storeQueries.getDomainsAndCDEs(null);
        assertNotNull("Response is null" + domainNCdes);
    }

    public void testGetDictionaries() throws Exception
    {
        RDFStoreQueries storeQueries = new RDFStoreQueries();
       assertNotNull("response is null", storeQueries.getDictionaries());
    }
}