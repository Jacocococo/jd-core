package org.jd.core.v1.model.javasyntax.declaration;

import org.jd.core.v1.model.javasyntax.reference.BaseAnnotationReference;
import org.jd.core.v1.model.javasyntax.type.Type;

public class RecordComponentDeclaration implements MemberDeclaration {
    protected BaseAnnotationReference annotationReferences;
    protected Type type;
    protected String name;

    public RecordComponentDeclaration(BaseAnnotationReference annotationReferences, Type type, String name) {
        this.annotationReferences = annotationReferences;
        this.type = type;
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public BaseAnnotationReference getAnnotationReferences() {
        return annotationReferences;
    }

    public String getName() {
        return name;
    }

    @Override
    public void accept(DeclarationVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "RecordComponentDeclaration{" + type + " " + name + "}";
    }
}
