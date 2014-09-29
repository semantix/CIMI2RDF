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

    private boolean isEnumerated;
    private String context;
    private String[] vds;
    public String getTTL(boolean isEnumeratedp, String contextp, String[] vdsp) throws ModelException
    {
        this.isEnumerated = isEnumeratedp;
        this.context = contextp;
        this.vds = vdsp;

        return this.getTTL();
    }

    public String getTTL() throws ModelException
    {
        String targetType = "ITEM_GROUP";

        if (!this.isEnumerated)
            targetType = "ELEMENT";

        String text =  "\n<http://rdf.cacde-qa.org/cacde/element#" + this.getRDFName() + ">" +
                "\nrdf:type cimi:" + targetType + " ;" +
                "\nrdf:type mms:DataElement ;" +
                "\nrdfs:label \"" + this.longName + "\"^^xsd:string ;" +
                "\nskos:definition \"" + this.longName + "\"^^xsd:string ;" +
                "\nmms:dataElementDescription \"" + this.longName + "\"^^xsd:string ;" +
                "\nmms:dataElementLabel \"" + this.longName + "\"^^xsd:string ;" +
                "\nmms:dataElementName \"" + this.prefName + "\"^^xsd:string ;" +
                "\nmms:dataElementType \"Property\"^^xsd:string ;" ;

        if (!ModelUtils.isNull(this.context))
            text += "\nmms:context cacde:" + context + " ;" ;

        if (this.vds != null)
            for (int i=0; i < this.vds.length; i++)
                if (ModelUtils.isNull(this.vds[i]))
                    continue;
                else
                    text += "\ncimi:ITEM_GROUP.item cacde:" + this.vds[i] + " ; ";

        text += "\n.";

        return text;
    }

    public String getRDFName() throws ModelException
    {
        if (!ModelUtils.isNull(this.longName))
            return ModelUtils.removeNonAlphaNum(this.longName);

        if (!ModelUtils.isNull(this.prefName))
            return ModelUtils.removeNonAlphaNum(this.prefName);

        return getId();
    }
}
