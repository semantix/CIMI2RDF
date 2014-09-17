package edu.mayo.cimi.rdf.model;

import edu.mayo.cimi.rdf.main.ModelUtils;

/**
 * Created by dks02 on 9/15/14.
 */
public class Study extends Top
{
    public String studyName;

    public Study(String name)
    {
        studyName = name;
    }

    public String getId() throws ModelException
    {
        this.id =  ModelUtils.key(studyName);
        return id;
    }
}
