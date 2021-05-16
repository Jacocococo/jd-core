package jd.instruction.bytecode.reconstructor;

import java.util.ArrayList;
import java.util.List;

import jd.Constants;
import jd.classfile.ClassFile;
import jd.classfile.ConstantPool;
import jd.classfile.Field;
import jd.classfile.Method;
import jd.classfile.analyzer.SignatureAnalyzer;
import jd.classfile.constant.ConstantFieldref;
import jd.classfile.constant.ConstantMethodref;
import jd.classfile.constant.ConstantNameAndType;
import jd.classfile.constant.ConstantString;
import jd.classfile.constant.ConstantValue;
import jd.instruction.bytecode.ByteCodeConstants;
import jd.instruction.bytecode.instruction.ANewArray;
import jd.instruction.bytecode.instruction.DupStore;
import jd.instruction.bytecode.instruction.GetStatic;
import jd.instruction.bytecode.instruction.Goto;
import jd.instruction.bytecode.instruction.IConst;
import jd.instruction.bytecode.instruction.IfInstruction;
import jd.instruction.bytecode.instruction.Instruction;
import jd.instruction.bytecode.instruction.Invokestatic;
import jd.instruction.bytecode.instruction.Invokevirtual;
import jd.instruction.bytecode.instruction.Ldc;
import jd.instruction.bytecode.instruction.NewArray;
import jd.instruction.bytecode.instruction.PutStatic;
import jd.instruction.bytecode.instruction.TernaryOpStore;
import jd.instruction.bytecode.visitor.ReplaceGetStaticVisitor;
import jd.util.ReferenceMap;


/*
 * Recontruction du mot cle '.class' depuis les instructions generees par le 
 * JDK 1.4 de SUN :
 * ...
 * ifnotnull( getstatic( current or outer class, 'class$...', Class ) )
 *  dupstore( invokestatic( current or outer class, 'class$', nom de la classe ) )
 *  putstatic( current class, 'class$...', Class, dupload )
 *  ternaryOpStore( dupload )
 *  goto 
 * ???( getstatic( class, 'class$...' ) )
 * ...
 */
public class DotClass14Reconstructor 
{
	public static void Reconstruct(
		ReferenceMap referenceMap, ClassFile classFile, List<Instruction> list)
	{
		int i = list.size();
		
		if  (i < 6)
			return;
		
		i -= 5;
		ConstantPool constants = classFile.getConstantPool();
		
		while (i-- > 0)
		{
			Instruction instruction = list.get(i);
			
			if (instruction.opcode != ByteCodeConstants.IFXNULL)
				continue;
			
			IfInstruction ii = (IfInstruction)instruction;
		
			if (ii.value.opcode != ByteCodeConstants.GETSTATIC)
				continue;
						
			int jumpOffset = ii.GetJumpOffset();
			
			instruction = list.get(i+1);
			
			if (instruction.opcode != ByteCodeConstants.DUPSTORE)
				continue;
			
			DupStore ds = (DupStore)instruction;
			
			if (ds.objectref.opcode != ByteCodeConstants.INVOKESTATIC)
				continue;
			
			Invokestatic is = (Invokestatic)ds.objectref;
			
			if (is.args.size() != 1)
				continue;

			instruction = is.args.get(0);
			
			if (instruction.opcode != ByteCodeConstants.LDC)
				continue;			

			instruction = list.get(i+2);
			
			if (instruction.opcode != ByteCodeConstants.PUTSTATIC)
				continue;
			
			PutStatic ps = (PutStatic)instruction;
			
			if ((ps.valueref.opcode != ByteCodeConstants.DUPLOAD) ||
				(ds.offset != ps.valueref.offset))
				continue;
			
			instruction = list.get(i+3);
			
			if (instruction.opcode != ByteCodeConstants.TERNARYOPSTORE)
				continue;
			
			TernaryOpStore tos = (TernaryOpStore)instruction;
			
			if ((tos.objectref.opcode != ByteCodeConstants.DUPLOAD) ||
				(ds.offset != tos.objectref.offset))
				continue;

			instruction = list.get(i+4);
			
			if (instruction.opcode != ByteCodeConstants.GOTO)
				continue;
			
			Goto g = (Goto)instruction;
			instruction = list.get(i+5);
			
			if ((g.offset >= jumpOffset) || (jumpOffset > instruction.offset))
				continue;
			
			GetStatic gs = (GetStatic)ii.value;			

			if (ps.index != gs.index)
				continue;
			
			ConstantFieldref cfr = constants.getConstantFieldref(gs.index);
			
			if (searchMatchingClassFile(cfr.class_index, classFile) == null)
				continue;
			
			ConstantNameAndType cnatField = constants.getConstantNameAndType(
					cfr.name_and_type_index);

			String descriptorField = 
				constants.getConstantUtf8(cnatField.descriptor_index);
			
			if (! Constants.INTERNAL_CLASS_SIGNATURE.equals(descriptorField))
				continue;
			
			String nameField = constants.getConstantUtf8(cnatField.name_index);

			if (!nameField.startsWith(Constants.CLASS_DOLLAR) && 
				!nameField.startsWith(Constants.ARRAY_DOLLAR))
				continue;
			
			ConstantMethodref cmr = 
				constants.getConstantMethodref(is.index);
			
			ClassFile matchingClassFile = 
				searchMatchingClassFile(cmr.class_index, classFile);
			if (matchingClassFile == null)
				continue;
			
			ConstantNameAndType cnatMethod = 
				constants.getConstantNameAndType(cmr.name_and_type_index);
			String nameMethod = 
				constants.getConstantUtf8(cnatMethod.name_index);
			
			if (! nameMethod.equals(Constants.CLASS_DOLLAR))
				continue;
			
			Ldc ldc = (Ldc)is.args.get(0);	
			ConstantValue cv = constants.getConstantValue(ldc.index);
			
			if (cv.tag != Constants.CONSTANT_String)
				continue;
			
			// Trouve !			
			ConstantString cs = (ConstantString)cv;
			String signature = constants.getConstantUtf8(cs.string_index);
			
			if (SignatureAnalyzer.GetArrayDimensionCount(signature) == 0)
			{
				String internalName = signature.replace(
					Constants.PACKAGE_SEPARATOR, 
					Constants.INTERNAL_PACKAGE_SEPARATOR);
				
				referenceMap.add(internalName);
				
				// Ajout du nom interne
				int index = constants.addConstantUtf8(internalName);
				// Ajout d'une nouvelle classe
				index = constants.addConstantClass(index);			
				ldc = new Ldc(
					ByteCodeConstants.LDC, instruction.offset, 
					instruction.lineNumber, index);
				
				// Remplacement de l'intruction GetStatic par l'instruction Ldc
				ReplaceGetStaticVisitor visitor = 
					new ReplaceGetStaticVisitor(gs.index, ldc);
				
				visitor.visit(instruction);	
			}
			else
			{
				IConst iconst0 = new IConst(
					ByteCodeConstants.ICONST, instruction.offset, 
					instruction.lineNumber, 0);
				Instruction newArray;

				String signatureWithoutDimension = 
					SignatureAnalyzer.CutArrayDimensionPrefix(signature);
				
				if (SignatureAnalyzer.IsObjectSignature(signatureWithoutDimension))
				{
				    //  8: iconst_0
				    //  9: anewarray 62	java/lang/String
				    //  12: invokevirtual 64	java/lang/Object:getClass	()Ljava/lang/Class;
					String tmp = signatureWithoutDimension.replace(
						Constants.PACKAGE_SEPARATOR, 
						Constants.INTERNAL_PACKAGE_SEPARATOR);
					String internalName = tmp.substring(1, tmp.length()-1);	

					// Ajout du nom de la classe pour generer la liste des imports
					referenceMap.add(internalName);
					// Ajout du nom interne
					int index = constants.addConstantUtf8(internalName);
					// Ajout d'une nouvelle classe
					index = constants.addConstantClass(index);			

					newArray = new ANewArray(
							ByteCodeConstants.ANEWARRAY, instruction.offset, 
							instruction.lineNumber, index, iconst0);
				}
				else
				{
				    //  8: iconst_0
				    //  9: newarray byte
				    //  11: invokevirtual 62	java/lang/Object:getClass	()Ljava/lang/Class;
					newArray = new NewArray(
						ByteCodeConstants.NEWARRAY, instruction.offset, 
						instruction.lineNumber, 
						SignatureAnalyzer.GetTypeFromSignature(signatureWithoutDimension),
						iconst0);
				}
				
				// Ajout de la methode 'getClass'
				int methodNameIndex = constants.addConstantUtf8("getClass");
				int methodDescriptorIndex = 
					constants.addConstantUtf8("()Ljava/lang/Class;");
				int nameAndTypeIndex = constants.addConstantNameAndType(
					methodNameIndex, methodDescriptorIndex);
				int cmrIndex = constants.addConstantMethodref(
					constants.objectClassIndex, nameAndTypeIndex);	
				
				Invokevirtual iv = new Invokevirtual(
					ByteCodeConstants.INVOKEVIRTUAL, instruction.offset, 
					instruction.lineNumber, cmrIndex, newArray, 
					new ArrayList<Instruction>(0));
				
				// Remplacement de l'intruction GetStatic
				ReplaceGetStaticVisitor visitor = 
					new ReplaceGetStaticVisitor(gs.index, iv);
				
				visitor.visit(instruction);	
			}

			// Retrait de l'intruction Goto
			list.remove(i+4);
			// Retrait de l'intruction TernaryOpStore
			list.remove(i+3);
			// Retrait de l'intruction PutStatic
			list.remove(i+2);
			// Retrait de l'intruction DupStore
			list.remove(i+1);
			// Retrait de l'intruction IfNotNull
			list.remove(i);	
			
			if (matchingClassFile == classFile)
			{
				// Recherche de l'attribut statique et ajout de l'attribut SYNTHETIC
				Field[] fields = classFile.getFields();
				int j = fields.length;
				
				while (j-- > 0)
				{
					Field field = fields[j];
					
					if (field.name_index == cnatField.name_index)
					{
						field.access_flags |= Constants.ACC_SYNTHETIC;
						break;
					}
				}
				
				// Recherche de la methode statique et ajout de l'attribut SYNTHETIC
				Method[] methods = classFile.getMethods();
				j = methods.length;
				
				while (j-- > 0)
				{
					Method method = methods[j];
					
					if (method.name_index == cnatMethod.name_index)
					{
						method.access_flags |= Constants.ACC_SYNTHETIC;
						break;
					}
				}
			}
			else
			{
				// Recherche de l'attribut statique et ajout de l'attribut SYNTHETIC
				ConstantPool matchingConstants = 
					matchingClassFile.getConstantPool();
				Field[] fields = matchingClassFile.getFields();
				int j = fields.length;
				
				
				while (j-- > 0)
				{
					Field field = fields[j];
					
					if (nameField.equals(
							matchingConstants.getConstantUtf8(field.name_index)))
					{
						field.access_flags |= Constants.ACC_SYNTHETIC;
						break;
					}
				}

				// Recherche de la methode statique et ajout de l'attribut SYNTHETIC
				Method[] methods = matchingClassFile.getMethods();
				j = methods.length;
				
				while (j-- > 0)
				{
					Method method = methods[j];
					
					if (nameMethod.equals(
							matchingConstants.getConstantUtf8(method.name_index)))
					{
						method.access_flags |= Constants.ACC_SYNTHETIC;
						break;
					}
				}				
			}
		}
	}
	
	private static ClassFile searchMatchingClassFile(
		int classIndex, ClassFile classFile)
	{
		if (classIndex == classFile.getThisClassIndex())
			return classFile;
		
		String className = 
			classFile.getConstantPool().getConstantClassName(classIndex);
		
		for (;;)
		{
			classFile = classFile.getOuterClass();
			
			if (classFile == null)
				return null;
			
			if (classFile.getThisClassName().equals(className))
				return classFile;
		}
	}
}
