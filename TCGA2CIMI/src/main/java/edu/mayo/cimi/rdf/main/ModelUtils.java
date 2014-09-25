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

    public static String makeCSVRow(int rowNumber, String[] cols, boolean showTotalReferenced)
    {
        if (cols == null)
            return "";

        int count = 0;

        String row = "";

        String refTitle = (showTotalReferenced)? (appendDQ("Total Referenced") + ",") : "";

        if (rowNumber < 1)
            row =  refTitle + appendDQ("Sr#");

        if (cols.length < 1)
            return row;

        String tempRow = "";
        for (String col : cols)
        {
            tempRow += "," + appendDQ((col == null) ? "" : col);

            if ((col != null)&&("1".equals(col.trim())))
                count++;
        }

        String refVal = (showTotalReferenced)? ("" + ((count > 0)?count:" ") + ","):"";

        if (rowNumber > 0)
            row +=  refVal + appendDQ("" + rowNumber) + tempRow;
        else
            row += tempRow;

        return row;
    }

    public static String appendDQ(String str)
    {
        return "\"" + str + "\"";
    }
}
