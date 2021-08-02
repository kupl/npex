package npex.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import npex.common.helper.TypeHelper;
import npex.common.utils.TypeUtil;
import spoon.reflect.code.CtExpression;
import spoon.reflect.reference.CtTypeReference;

/**
 * This class contains a map from commonly accessible java types including primitives and those are defined under
 * the java packages (e.g., java.lang.Collections) to top-3 frequent constant values in the learning DB.
 * Former value has a high rank.
 */
public class CommonExpressionTable {

	static private Map<CtTypeReference, List<String>> map = new HashMap<>();

	static {
		// constants for primitives
		map.put(TypeUtil.BYTE_PRIMITIVE, Arrays.asList(new String[] {"0"}));
		map.put(TypeUtil.SHORT_PRIMITIVE, Arrays.asList(new String[] {"0"}));
		map.put(TypeUtil.INTEGER_PRIMITIVE, Arrays.asList(new String[] {"0", "-1", "1"}));
		map.put(TypeUtil.LONG_PRIMITIVE, Arrays.asList(new String[] {"0L", "-1L", "java.lang.Long.MAX_VALUE"}));
		map.put(TypeUtil.FLOAT_PRIMITIVE, Arrays.asList(new String[] { "0.0F", "1.0F", "java.lang.Float.MAX_VALUE" }));
		map.put(TypeUtil.DOUBLE_PRIMITIVE, Arrays.asList(new String[] {"0,0", "java.lang.Double.NaN", "1.0"}));
		map.put(TypeUtil.BOOLEAN_PRIMITIVE, Arrays.asList(new String[] {"false", "true"}));
		map.put(TypeUtil.CHAR_PRIMITIVE, Arrays.asList(new String[] {"'\u0000'", "'0'"}));

		// constants for String
		map.put(TypeUtil.STRING, Arrays.asList(new String[] {"\"\"", "\"null\"", "\"NULL\""}));

		// constants for Class
		map.put(TypeUtil.CLASS, Arrays.asList(new String[] {"java.lang.Object.class"}));

		// constants for Collection
		map.put(TypeUtil.COLLECTION, Arrays.asList(new String[] {"java.util.Collections.emptyList()", "java.util.Collections.emptyMap()", "java.util.Collections.emptySet()"}));

		// constants for Enumeration
		map.put(TypeUtil.ENUMERATION, Arrays.asList(new String[] { "java.util.Collections.emptyEnumeration()" }));
	}

	public static boolean isCommon(CtExpression e) {
		CtTypeReference etyp = TypeHelper.getType(e);
		Optional<CtTypeReference> matchedTyp = map.keySet().stream().filter(ty -> etyp.isSubtypeOf(ty)).findFirst();
		if (matchedTyp.isPresent() && map.get(matchedTyp.get()).contains(e.toString())) {
			return true;
		}
		return false;
	}

}