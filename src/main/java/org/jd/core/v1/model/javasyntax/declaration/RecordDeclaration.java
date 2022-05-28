package org.jd.core.v1.model.javasyntax.declaration;

import org.jd.core.v1.model.javasyntax.reference.BaseAnnotationReference;
import org.jd.core.v1.model.javasyntax.type.BaseType;
import org.jd.core.v1.model.javasyntax.type.BaseTypeParameter;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.declaration.ClassFileRecordBodyDeclaration;

public class RecordDeclaration extends InterfaceDeclaration {
    protected BaseRecordComponentDeclaration recordComponentDeclarations;

    public RecordDeclaration(BaseAnnotationReference annotationReferences, int flags, String internalName, String name,
            BaseTypeParameter typeParameters, BaseType interfaces, ClassFileRecordBodyDeclaration bodyDeclaration) {
        super(annotationReferences, flags, internalName, name, typeParameters, interfaces, bodyDeclaration);
    }

    public BaseRecordComponentDeclaration getRecordComponentDeclarations() {
        return recordComponentDeclarations;
    }

    @Override
    public void accept(DeclarationVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "RecordDeclaration{" + internalTypeName + "}";
    }
}
