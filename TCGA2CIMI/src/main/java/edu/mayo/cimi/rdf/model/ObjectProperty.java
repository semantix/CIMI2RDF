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
        return id  + getSuffix();
    }

    public String getSuffix()
    {
        return "_OP" + super.getSuffix();
    }

    public void addCDE(String cdeKey)
    {
        if (!cdeKeys.contains(cdeKey))
            cdeKeys.add(cdeKey);
    }

    private boolean isEnumerated;
    private boolean isJavaType = false;
    private String context;
    private ValueDomain[] vds;
    public String getTTL(String contextp, ValueDomain[] vdsp) throws ModelException
    {
        this.context = contextp;
        this.vds = vdsp;

        isJavaType = false;
        boolean isISO = false;
        for (int i=0; i < vdsp.length;i++)
        {
            if (vdsp[i] == null)
                continue;

            String dataType = vdsp[i].vdDatatype;
            boolean isEnum = vdsp[i].isEnumerated;

            if (!isISO)
            {
                if ((dataType.indexOf("java") != -1) ||
                        (dataType.indexOf("xsd") != -1))
                    isJavaType = true;

                if (("ISO".equals(ValueDomain.getCIMIType(dataType)))||
                    (("OBJECT".equals(ValueDomain.getCIMIType(dataType)))))
                    isISO = true;
            }
        }
        return this.getTTL();
    }

    public String getTTL() throws ModelException
    {
        String targetType = "ITEM_GROUP";

        if ((!this.isEnumerated)||(this.isJavaType))
            targetType = "ELEMENT";

        String text =  "\n<http://rdf.cacde-qa.org/cacde/element#" + this.getRDFName() + ">" +
                "\nrdf:type cimi:" + targetType + " ;" +
                "\nrdf:type mms:DataElement ;" +
                "\nrdfs:label \"" + ModelUtils.removeTypeInformation(this.longName).trim() + "\"^^xsd:string ;" +
                "\nskos:definition \"" + ModelUtils.removeTypeInformation(this.longName).trim() + "\"^^xsd:string ;" +
                "\nmms:dataElementDescription \"" + ModelUtils.removeTypeInformation(this.longName).trim() + "\"^^xsd:string ;" +
                "\nmms:dataElementLabel \"" + ModelUtils.removeTypeInformation(this.longName).trim() + "\"^^xsd:string ;" +
                "\nmms:dataElementName \"" + ModelUtils.removeTypeInformation(this.prefName).trim() + "\"^^xsd:string ;" +
                "\nmms:dataElementType \"Property\"^^xsd:string ;" ;

        if (!ModelUtils.isNull(this.context))
            text += "\nmms:context cacde:" + context + " ;" ;

        if ((this.vds != null)&&(!("ELEMENT".equals(targetType))))
            for (int i=0; i < this.vds.length; i++)
                if (ModelUtils.isNull(this.vds[i].getRDFName()))
                    continue;
                else
                    text += "\ncimi:ITEM_GROUP.item cacde:" + this.vds[i].getRDFName() + " ; ";

        text += "\n.";

        return text;
    }

    public String getRDFName() throws ModelException
    {
        if (!ModelUtils.isNull(this.longName))
            return ModelUtils.removeNonAlphaNum(this.longName)  + getSuffix();

        if (!ModelUtils.isNull(this.prefName))
            return ModelUtils.removeNonAlphaNum(this.prefName)  + getSuffix();

        return getId()  + getSuffix();
    }
}
