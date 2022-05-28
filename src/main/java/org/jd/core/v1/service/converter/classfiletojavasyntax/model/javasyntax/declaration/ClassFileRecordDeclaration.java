package org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.declaration;

import org.jd.core.v1.model.javasyntax.declaration.BaseRecordComponentDeclaration;
import org.jd.core.v1.model.javasyntax.declaration.RecordDeclaration;
import org.jd.core.v1.model.javasyntax.reference.BaseAnnotationReference;
import org.jd.core.v1.model.javasyntax.type.BaseType;
import org.jd.core.v1.model.javasyntax.type.BaseTypeParameter;

public class ClassFileRecordDeclaration extends RecordDeclaration implements ClassFileTypeDeclaration {
    protected int firstLineNumber;

    public ClassFileRecordDeclaration(BaseAnnotationReference annotationReferences, int flags, String internalName,
            String name, BaseTypeParameter typeParameters, BaseType interfaces, ClassFileRecordBodyDeclaration bodyDeclaration) {
        super(annotationReferences, flags, internalName, name, typeParameters, interfaces, bodyDeclaration);
        this.firstLineNumber = bodyDeclaration==null ? 0 : bodyDeclaration.getFirstLineNumber();
    }

    @Override
    public int getFirstLineNumber() {
        return firstLineNumber;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    @SuppressWarnings("unchecked")
    public void setRecordComponentDeclarations(BaseRecordComponentDeclaration list) {
        this.recordComponentDeclarations = list;
    }

    @Override
    public String toString() {
        return "ClassFileRecordDeclaration{" + internalTypeName + ", firstLineNumber=" + firstLineNumber + "}";
    }
}
