package edu.mayo.cimi.rdf.model;

import edu.mayo.cimi.rdf.main.ModelUtils;

/**
 * Created by dks02 on 9/12/14.
 */
public class CDEProperty extends Top
{
    public String prefName;
    public String longName;
    public ValueDomain valueDomain;

    public String getId() throws ModelException
    {
        this.id =  ModelUtils.key(prefName+longName);
        return id;
    }
}
