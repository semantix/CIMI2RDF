package edu.mayo.cimi.rdf.main;

import edu.mayo.cimi.rdf.model.ModelException;

/**
 * Created by dks02 on 9/12/14.
 */
public class ModelUtils
{
    public static boolean isNull(String str)
    {
        if ((str == null)||("null".equalsIgnoreCase(str.trim()))||("".equalsIgnoreCase(str.trim())))
            return true;
        return false;
    }

    public static String removeNonAlphaNum(String value)
    {
        if (!isNull(value))
            return value.replaceAll("[^a-zA-Z0-9.]", "");

        return null;
    }

    public static String key(String inputString) throws ModelException
    {
        if (ModelUtils.isNull(inputString))
            throw (new ModelException("input String is null!!"));

        return ModelUtils.removeNonAlphaNum(inputString);
    }
}
