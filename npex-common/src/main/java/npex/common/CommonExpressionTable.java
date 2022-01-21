package npex.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import npex.common.utils.TypeUtil;
import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtExpression;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

/**
 * This class contains a map from commonly accessible java types including primitives and those are defined under
 * the java packages (e.g., java.lang.Collections), to the top-3 frequent expressions in the learning DB.
 */
public class CommonExpressionTable {
	final static Logger logger = LoggerFactory.getLogger(CommonExpressionTable.class);

	static private Map<CtTypeReference, List<String>> map = new HashMap<>();

	static {
		// primitives
		map.put(TypeUtil.BYTE_PRIMITIVE, Arrays.asList(new String[] {"0"}));
		map.put(TypeUtil.SHORT_PRIMITIVE, Arrays.asList(new String[] {"0"}));
		map.put(TypeUtil.INTEGER_PRIMITIVE, Arrays.asList(new String[] {"0", "-1", "1"}));
		map.put(TypeUtil.LONG_PRIMITIVE, Arrays.asList(new String[] {"0L", "-1L", "java.lang.Long.MAX_VALUE"}));
		map.put(TypeUtil.FLOAT_PRIMITIVE, Arrays.asList(new String[] { "0.0F", "1.0F", "java.lang.Float.MAX_VALUE" }));
		map.put(TypeUtil.DOUBLE_PRIMITIVE, Arrays.asList(new String[] {"0.0", "java.lang.Double.NaN", "1.0"}));
		map.put(TypeUtil.BOOLEAN_PRIMITIVE, Arrays.asList(new String[] {"false", "true"}));
		map.put(TypeUtil.CHAR_PRIMITIVE, Arrays.asList(new String[] {"'\u0000'", "'0'"}));

		// String
		map.put(TypeUtil.STRING, Arrays.asList(new String[] {"\"\"", "\"null\"", "\"NULL\""}));

		// BOOLEAN
    map.put(TypeUtil.BOOLEAN, Arrays.asList(new String[] { "java.lang.Boolean.TRUE", "java.lang.Boolean.FALSE" }));

		// Class
		map.put(TypeUtil.CLASS, Arrays.asList(new String[] {"java.lang.Object.class"}));

		// collcetions
		map.put(TypeUtil.LIST, Arrays.asList(new String[] {"java.util.Collections.emptyList()"}));
		map.put(TypeUtil.MAP, Arrays.asList(new String[] {"java.util.Collections.emptyMap()"}));
		map.put(TypeUtil.SET, Arrays.asList(new String[] { "java.util.Collections.emptySet()" }));

		// Enumeration
		map.put(TypeUtil.ENUMERATION, Arrays.asList(new String[] { "java.util.Collections.emptyEnumeration()" }));
	}

	public static List<CtExpression> find(CtTypeReference type) {
		List<CtExpression> exprs = new ArrayList<>();
		Factory factory = type.getFactory();
		CtTypeReference key = map.keySet().stream().filter(k -> type.getTypeDeclaration() != null && type.isSubtypeOf(k)).findAny().orElse(type);

		for (String s : map.getOrDefault(type, new ArrayList<String>())) {
      CtCodeSnippetExpression e = factory.createCodeSnippetExpression(s);
			e.setType(type);
			exprs.add(e.compile());
		}

		return exprs;
	}
}