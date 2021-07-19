package npex.extractor.context;

import java.util.function.Predicate;

import npex.common.helper.TypeHelper;
import npex.common.utils.FactoryUtils;
import spoon.SpoonException;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

public class ReturnTypeHasDefault extends Context {
	public Boolean extract(CtAbstractInvocation invo, int nullPos) {
		// Primitives have default values
    CtTypeReference retType = TypeHelper.getType(invo);
		if ((!retType.equals(FactoryUtils.VOID_TYPE) && retType.isPrimitive()) || retType.equals(FactoryUtils.STRING_TYPE))
			return true;
		
		// Check if return type has a default constructor
		Predicate<CtExecutableReference> hasDefault =  exRef -> exRef.getExecutableDeclaration() instanceof CtConstructor ctor && ctor.getParameters().size() == 0;
		try {
			return retType.getTypeDeclaration() != null && retType.getTypeDeclaration().getAllExecutables().stream().anyMatch(hasDefault);
		} catch (SpoonException e) {
			return false;
		}
		
  }
    
}