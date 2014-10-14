package edu.mayo.cimi.rdf.model;

import edu.mayo.cimi.rdf.main.ModelUtils;

import java.util.HashMap;
import java.util.Vector;

/**
 * Created by dks02 on 9/12/14.
 */
public class TCGADomain extends Top
{
    public String domainName;
    public HashMap<String, TCGADomainEntry> entries = new HashMap<String, TCGADomainEntry>();

    public TCGADomain(String domainName)
    {
        this.domainName= domainName;
    }

    public String getId() throws ModelException
    {
        this.id =  ModelUtils.key(this.domainName);
        return id;
    }

    public void add(TCGADomainEntry entry) throws ModelException
    {
        if (entry == null)
            return;

        if (!exists(entry))
            entries.put(entry.getId(), entry);
    }

    public boolean exists(TCGADomainEntry entry) throws ModelException
    {
        return (entries.get(entry.getId()) != null);
    }

    public Vector<String> getUniqueCDEsReferenced()
    {
        Vector<String> ucde = new Vector<String>();

        for (TCGADomainEntry e : entries.values())
        {
            if (!ucde.contains(e.cdeKey))
                ucde.add(e.cdeKey);
        }

        return ucde;
    }

    public String getTTL() throws ModelException
    {
        return  "\n\n<http://tcga.nci.nih.gov/BCR/DataDictionary#" + this.getRDFName() + ">" +
            "\nrdf:type cimi:ITEM_GROUP ;";
    }

    public String getRDFName() throws ModelException
    {
        if (!ModelUtils.isNull(this.domainName))
            return ModelUtils.removeNonAlphaNum(this.domainName);

        return getId();
    }
}
