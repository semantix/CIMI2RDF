package edu.mayo.cimi.rdf.model;

/**
 * Created by dks02 on 9/12/14.
 */
public class TCGADomainEntry extends Top
{
    public String studyKey;
    public String cdeKey;

    public String getId() throws ModelException
    {
        this.id =  this.cdeKey + this.studyKey;
        return id;
    }

    public String getTTL()
    {
        return null;
    }
}
