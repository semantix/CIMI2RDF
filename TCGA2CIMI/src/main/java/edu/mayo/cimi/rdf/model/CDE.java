package edu.mayo.cimi.rdf.model;

import edu.mayo.cimi.rdf.main.ModelUtils;

/**
 * Created by dks02 on 9/12/14.
 */
public class CDE extends Top
{
    public String publicId;
    public String name; // short name given in TCGA or user context, But a CDE is idetified by publicID
    public String longName;
    public String definition;
    public String objectClassKey;
    public String objectPropertyKey;
    public String valueDomainKey;


    public CDE(String publicId)
    {
        this.publicId = publicId;
    }

    public String getId() throws ModelException
    {
        this.id =  ModelUtils.key(this.publicId);
        return id;
    }

    public String getTTL()
    {
        return "CDE:" + name;
    }

    public String getRDFName() throws ModelException
    {
        if (!ModelUtils.isNull(this.longName))
            return ModelUtils.removeNonAlphaNum(this.longName);

        if (!ModelUtils.isNull(this.name))
            return ModelUtils.removeNonAlphaNum(this.name);

        return getId();
    }
}
