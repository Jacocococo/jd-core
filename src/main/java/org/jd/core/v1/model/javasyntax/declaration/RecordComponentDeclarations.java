package org.jd.core.v1.model.javasyntax.declaration;

import java.util.Collection;

import org.jd.core.v1.util.DefaultList;

public class RecordComponentDeclarations extends DefaultList<RecordComponentDeclaration> implements BaseRecordComponentDeclaration {
    public RecordComponentDeclarations() {}

    public RecordComponentDeclarations(int capacity) {
        super(capacity);
    }

    public RecordComponentDeclarations(Collection<RecordComponentDeclaration> collection) {
        super(collection);
        assert (collection != null) && (collection.size() > 1) : "Uses 'RecordComponentDeclaration' implementation instead";
    }

    @SuppressWarnings("unchecked")
    public RecordComponentDeclarations(RecordComponentDeclaration declaration, RecordComponentDeclaration... declarations) {
        super(declaration, declarations);
        assert (declarations != null) && (declarations.length > 0) : "Uses 'RecordComponentDeclaration' implementation instead";
    }

    @Override
    public void accept(DeclarationVisitor visitor) {
        visitor.visit(this);
    }
}
