package org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.declaration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jd.core.v1.model.classfile.ClassFile;
import org.jd.core.v1.model.javasyntax.declaration.FormalParameter;
import org.jd.core.v1.model.javasyntax.declaration.FormalParameters;
import org.jd.core.v1.model.javasyntax.type.BaseType;
import org.jd.core.v1.model.javasyntax.type.TypeArgument;

public class ClassFileRecordBodyDeclaration extends ClassFileBodyDeclaration {
	private FormalParameters formalParameters = new FormalParameters();
	
	public ClassFileRecordBodyDeclaration(ClassFile classFile, Map<String, TypeArgument> bindings,
			Map<String, BaseType> typeBounds, ClassFileBodyDeclaration outerBodyDeclaration) {
		super(classFile, bindings, typeBounds, outerBodyDeclaration);
	}

	// Only static fields are allowed in records
	// (other than the record parameters).
	public void setFieldDeclarations(List<ClassFileFieldDeclaration> fieldDeclarations) {
		super.setFieldDeclarations(fieldDeclarations);
        if (fieldDeclarations != null) {
        	this.fieldDeclarations = fieldDeclarations.stream().map(e -> {
        		if((e.getFlags() & FLAG_STATIC) == 0) {
        			e.setFlags(e.getFlags() | FLAG_SYNTHETIC);
        			
        			FormalParameter formalParameter = new FormalParameter(e.getType(),
        					e.getFieldDeclarators().getFirst().getName());
        			formalParameters.add(formalParameter);
        		}
        		return e;
        	}).collect(Collectors.toList());
        }
    }
	
	// All default methods are on the same line,
	// so we can just hide those methods
	public void setMethodDeclarations(List<ClassFileConstructorOrMethodDeclaration> methodDeclarations) {
        super.setMethodDeclarations(methodDeclarations);
		if (methodDeclarations != null) {
			int firstLineNumber = methodDeclarations.stream().filter(e -> { 
				return !e.getMethod().getName().contains("lambda$") // only lambdas and static scopes comes after
						&& !e.getMethod().getName().contains("<clinit>");
			}).reduce((first, second) -> second).get().getFirstLineNumber(); // Last element will now be the right one
			this.methodDeclarations = methodDeclarations.stream().map(e -> {
        		if(e.getFirstLineNumber() == firstLineNumber)
        			e.setFlags(e.getFlags() | FLAG_SYNTHETIC);
        		return e;
			}).collect(Collectors.toList());
        }
    }

	public FormalParameters getFormalParameters() {
		return formalParameters;
	}
}
