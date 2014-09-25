package edu.mayo.cimi.rdf.model;

import edu.mayo.cimi.rdf.main.ModelUtils;

import java.util.Vector;

/**
 * Created by dks02 on 9/19/14.
 */
public class ObjectProperty extends Top
{
    public String longName;
    public String prefName;
    public Vector<String> cdeKeys = new Vector<String>();

    public ObjectProperty(String prefName)
    {
        this.prefName = prefName;
    }

    public String getId() throws ModelException
    {
        this.id = ModelUtils.key(this.prefName);
        return id;
    }

    public void addCDE(String cdeKey)
    {
        if (!cdeKeys.contains(cdeKey))
            cdeKeys.add(cdeKey);
    }

    public String getTTL()
    {
        return "PROP:" + prefName;
    }
}
