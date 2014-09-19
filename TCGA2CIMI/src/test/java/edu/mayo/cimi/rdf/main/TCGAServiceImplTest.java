package edu.mayo.cimi.rdf.main;

import junit.framework.TestCase;

public class TCGAServiceImplTest extends TestCase
{
    public void testGetDomainsAndCDEs() {
        RDFStoreQueries storeQueries = new RDFStoreQueries();
        String domainNCdes = storeQueries.getDistinctDomains();
        assertNotNull("Response is null" + domainNCdes);
    }
}