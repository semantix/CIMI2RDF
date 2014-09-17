package edu.mayo.cimi.rdf.model;

import edu.mayo.cimi.rdf.main.ModelUtils;

import java.util.Vector;

/**
 * Created by dks02 on 9/12/14.
 */
public class CDE extends Top
{
    public String name;
    public String longName;
    public String definition;
    public String objectClassPrefName;
    public String objectClassLongName;
    public Vector<CDEProperty> properties;

    public CDE(String publicId)
    {
        super.id = publicId;
    }

    public String getId() throws ModelException
    {
        return ModelUtils.key(id);
    }
}
