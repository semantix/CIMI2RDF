package edu.mayo.cimi.rdf.model;

/**
 * Created by dks02 on 9/15/14.
 */
public abstract class Top
{
    public String id;
    public abstract String getId() throws ModelException;

    public abstract String getTTL() throws ModelException;
}
