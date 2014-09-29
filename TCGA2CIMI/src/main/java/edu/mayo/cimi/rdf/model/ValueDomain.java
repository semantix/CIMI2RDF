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

    public Vector<String> usageByObjectClasses = new Vector<String>();

    public ValueDomain(String vdName)
    {
        this.name = vdName;
    }

    public String getId() throws ModelException
    {
        this.id =  ModelUtils.key(name);
        return id;
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
                "\nrdf:type mms:DataElement ;" +
                "\nrdfs:label \"" + this.name + "\"^^xsd:string ;" +
                "\nskos:definition \"" + this.name + "\"^^xsd:string ;" +
                "\nmms:dataElementDescription \"" + this.name + "\"^^xsd:string ;" +
                "\nmms:dataElementLabel \"" + this.name + "\"^^xsd:string ;" +
                "\nmms:dataElementName \"" + this.name + "\"^^xsd:string ;";

        if (this.isEnumerated)
                text += "\nmms:dataElementType \"Enumerated\"^^xsd:string ;" ;
        else
                text += "\nmms:dataElementType \"NonEnumerated\"^^xsd:string ;" ;

        return text;
    }

    public String getRDFName() throws ModelException
    {
        if (!ModelUtils.isNull(this.name))
            return ModelUtils.removeNonAlphaNum(this.name);

        return getId();
    }
}
