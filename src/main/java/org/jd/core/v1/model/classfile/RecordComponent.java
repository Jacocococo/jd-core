package org.jd.core.v1.model.classfile;

import java.util.Map;

import org.jd.core.v1.model.classfile.attribute.Attribute;

public class RecordComponent {
    protected String name;
    protected String descriptor;
    protected Map<String, Attribute> attributes;

    public RecordComponent(String name, String descriptor, Map<String, Attribute> attributes) {
        this.name = name;
        this.descriptor = descriptor;
        this.attributes = attributes;
    }

    public String getName() {
        return name;
    }

    public String getDescriptor() {
        return descriptor;
    }

    @SuppressWarnings("unchecked")
    public <T extends Attribute> T getAttribute(String name) {
        return (attributes == null) ? null : (T)attributes.get(name);
    }

    @Override
    public String toString() {
        return "RecordComponent{" + name + " " + descriptor + "}";
    }
}
