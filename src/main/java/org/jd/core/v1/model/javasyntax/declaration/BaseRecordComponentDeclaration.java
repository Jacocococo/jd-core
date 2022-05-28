package org.jd.core.v1.model.javasyntax.declaration;

import org.jd.core.v1.util.Base;

public interface BaseRecordComponentDeclaration extends Declaration, Base<RecordComponentDeclaration> {
    default boolean isClassDeclaration() { return false; }
}
