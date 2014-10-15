package edu.mayo.cimi.rdf.model;

import edu.mayo.cimi.rdf.main.ModelUtils;

import java.util.Vector;

/**
 * Created by dks02 on 9/12/14.
 */
public class ValueDomain extends Top
{
    public String name;
    public boolean isEnumerated;
    public String vdDatatype;

    public Vector<String> usageByObjectClasses = new Vector<String>();

    public ValueDomain(String vdName)
    {
        this.name = vdName;
    }

    public String getSuffix()
    {
        return "_VD" + super.getSuffix();
    }

    public String getId() throws ModelException
    {
        this.id =  ModelUtils.key(name);
        return id  + getSuffix();
    }

    private String context = null;

    public void addUsage(String objectClassKey)
    {
        if (ModelUtils.isNull(objectClassKey))
            return;

        if (!this.usageByObjectClasses.contains(objectClassKey))
            this.usageByObjectClasses.add(objectClassKey);
    }

    public String getTTL() throws ModelException
    {
        String text =  "\n<http://rdf.cacde-qa.org/cacde/element#" + this.getRDFName() + ">" +
                "\nrdf:type cimi:ELEMENT ;"+
                //"\nrdf:type mms:DataElement ;" +
                "\nrdfs:label \"" + this.name + "\"^^xsd:string ;" +
                "\nskos:definition \"" + this.name + "\"^^xsd:string ;" +
                "\nmms:dataElementDescription \"" + this.name + "\"^^xsd:string ;" +
                "\nmms:dataElementLabel \"" + this.name + "\"^^xsd:string ;" +
                "\nmms:dataElementName \"" + this.name + "\"^^xsd:string ;";

        if (this.isEnumerated)
        {
            text += "\nmms:dataElementType \""+ this.vdDatatype +"\"^^xsd:string ;";
            text += "\ncimi:ELEMENT.value cimi:CODED_TEXT;";
            text += "\nrdf:type mms:EnumeratedValueDomain ;";
        }
        else
        {
            text += "\nmms:dataElementType \""+ this.vdDatatype +"\"^^xsd:string ;";
            text += "\ncimi:ELEMENT.value cimi:" + getCIMIType(this.vdDatatype) + ";";
            text += "\nrdf:type mms:ValueDomain ;";
        }
        return text;
    }

    public String getRDFName() throws ModelException
    {
        if (!ModelUtils.isNull(this.name))
            return ModelUtils.removeNonAlphaNum(this.name) + getSuffix();

        return getId() + getSuffix();
    }

    public static String getCIMIType(String myType)
    {
        if (myType == null)
            return "String";

        if(myType.equalsIgnoreCase("NUMBER"))
            return "Real";
        if(myType.equalsIgnoreCase("CHARACTER"))
            return "Character";
        if(myType.equalsIgnoreCase("DATETIME"))
            return "DATE_TIME";
        if(myType.equalsIgnoreCase("DATE/TIME"))
            return "DATE_TIME";
        if(myType.equalsIgnoreCase("DATE"))
            return "DATE";
        if(myType.equalsIgnoreCase("TIME"))
            return "TIME";

        if(myType.equalsIgnoreCase("ALPHANUMERIC"))
            return "String";

        if(myType.indexOf("Alpha") != -1)
            return "String";

        if(myType.equalsIgnoreCase("java.lang.Integer"))
            return "Integer";
        if(myType.equalsIgnoreCase("java.lang.Long"))
            return "Integer";
        if(myType.equalsIgnoreCase("java.lang.Float"))
            return "Real";
        if(myType.equalsIgnoreCase("java.lang.Double"))
            return "Real";
        if(myType.equalsIgnoreCase("java.lang.Boolean"))
            return "YESNO";
        if(myType.equalsIgnoreCase("Boolean"))
            return "YESNO";
        if(myType.equalsIgnoreCase("java.util.Date"))
            return "DATE";
        if(myType.equalsIgnoreCase("java.sql.Timestamp"))
            return "DATE_TIME";
        if(myType.equalsIgnoreCase("xsd:string"))
            return "String";

        if(myType.equalsIgnoreCase("java.lang.String"))
            return "String";

        if(myType.equalsIgnoreCase("String"))
            return "String";

        if(myType.startsWith("ISO"))
            return "ISO";

        if(myType.equalsIgnoreCase("OBJECT"))
            return "OBJECT";

        if(myType.startsWith("anyClass"))
            return "URI_VALUE";

        System.out.print("Could not map input value domain data type:" + myType);
        System.out.println(" Returning Type=String");

        return "String";
    }
}
