package edu.mayo.cimi.rdf.model;

import edu.mayo.cimi.rdf.main.ModelUtils;

import java.util.Vector;

/**
 * Created by dks02 on 9/12/14.
 */
public class TCGADomain extends Top
{
    public String domainName;
    public Vector<TCGADomainEntry> entries = new Vector<TCGADomainEntry>();

    public TCGADomain(String domainName)
    {
        this.domainName= domainName;
    }

    public String getId() throws ModelException
    {
        this.id =  ModelUtils.key(domainName);
        return id;
    }
}
