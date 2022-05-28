package org.jd.core.v1.model.classfile.attribute;

import org.jd.core.v1.model.classfile.RecordComponent;

public class AttributeRecord implements Attribute {
    protected RecordComponent[] components;

    public AttributeRecord(RecordComponent[] components) {
        this.components = components;
    }

    public RecordComponent[] getComponents() {
        return components;
    }
}
