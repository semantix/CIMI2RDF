package edu.mayo.cimi.rdf.model;

/**
 * Created by dks02 on 9/15/14.
 */
public abstract class Top
{
    public String id;
    public int suffixIfNameRepeated = 0; // added as a suffix to name (RDF) to distinguish
    public abstract String getId() throws ModelException;

    public abstract String getTTL() throws ModelException;

    public String getSuffix()
    {
        if (suffixIfNameRepeated > 0)
            return ("_" + suffixIfNameRepeated);

        return "";
    }
}
