package edu.mayo.cimi.rdf.model;

import edu.mayo.cimi.rdf.main.ModelUtils;

import java.util.Vector;

/**
 * Created by dks02 on 9/19/14.
 */
public class ObjectClass extends Top
{
    public String longName;
    public String prefName;
    public Vector<String> cdeKeys = new Vector<String>();

    public ObjectClass(String prefName)
    {
        this.prefName = prefName;
    }

    public String getId() throws ModelException
    {
        this.id = ModelUtils.key(this.prefName);
        return id  + getSuffix();
    }

    public String getSuffix()
    {
        return "_OC" + super.getSuffix();
    }

    public void addCDE(String cdeKey)
    {
        if (!cdeKeys.contains(cdeKey))
            cdeKeys.add(cdeKey);
    }

    public String getRDFName() throws ModelException
    {
        if (!ModelUtils.isNull(this.longName))
            return ModelUtils.removeNonAlphaNum(this.longName)  + getSuffix();

        if (!ModelUtils.isNull(this.prefName))
            return ModelUtils.removeNonAlphaNum(this.prefName)  + getSuffix();

        return getId() + getSuffix();
    }

    public String getTTL() throws ModelException
    {
        return  "\n<http://rdf.cacde-qa.org/cacde/element#" + this.getRDFName() + ">" +
                "\nrdf:type cimi:ITEM_GROUP ;" +
                "\nrdf:type mms:DataElement ;" +
                "\nrdfs:label \"" + ModelUtils.removeTypeInformation(this.longName).trim() + "\"^^xsd:string ;" +
                "\nskos:definition \"" + ModelUtils.removeTypeInformation(this.longName).trim() + "\"^^xsd:string ;" +
                "\nmms:dataElementDescription \"" + ModelUtils.removeTypeInformation(this.longName).trim() + "\"^^xsd:string ;" +
                "\nmms:dataElementLabel \"" + ModelUtils.removeTypeInformation(this.longName).trim() + "\"^^xsd:string ;" +
                "\nmms:dataElementName \"" + ModelUtils.removeTypeInformation(this.prefName).trim() + "\"^^xsd:string ;" +
                "\nmms:dataElementType \"Object Class\"^^xsd:string ;";
    }
}
