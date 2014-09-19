package edu.mayo.cimi.rdf.model;

import edu.mayo.cimi.rdf.main.ModelUtils;

/**
 * Created by dks02 on 9/12/14.
 */
public class ValueDomain extends Top
{
    public String name;
    public boolean isEnumerated;

    public ValueDomain(String vdName)
    {
        this.name = vdName;
    }

    public String getId() throws ModelException
    {
        this.id =  ModelUtils.key(name);
        return id;
    }
}
