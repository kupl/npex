package npex.extractor.nullhandle;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.json.JSONArray;
import org.json.JSONObject;

import npex.common.NPEXException;
import npex.common.utils.FactoryUtils;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.reference.CtTypeReference;

public class NullValue {
	final private String kind;
	final private String[] exprs;
	final private CtExpression raw;

	public static final NullValue SKIP = createPlain("NPEX_SKIP_VALUE", null);
	public static final NullValue EMPTY_COLLECTIONS = createPlain("NPEXEmptyCollections", null);
	public static final NullValue OBJECT_CLASS = createPlain("java.lang.Object.class", null);
	public static final NullValue NULL_LIT = createLiteral(FactoryUtils.createNullLiteral());

	private static final String[] emptyCollections = { "java.util.Collections.EMPTY_LIST",
			"java.util.Collections.EMPTY_MAP", "java.util.Collections.EMPTY_SET" };

	private NullValue(String kind, String[] exprs, CtExpression raw) {
		this.kind = kind;
		this.exprs = exprs;
		this.raw = raw;
	}

	public String getRawString() {
		return raw != null ? raw.toString() : null;
	}

	public JSONObject toJSON() {
		var obj = new JSONObject();
		obj.put("kind", kind);
		obj.put("exprs", new JSONArray(exprs));
		obj.put("raw", raw == null ? JSONObject.NULL : raw.toString());
		return obj;
	}

	private static NullValue createNonLiteral(CtExpression raw, CtAbstractInvocation invo) {
		CtExpression base = invo instanceof CtInvocation vinvo ? vinvo.getTarget() : null;
		List<CtExpression> args = invo.getArguments();
		Function<CtExpression, String> convert = e -> {
			if (e.equals(base))
				return "$(-1)";
			else if (args.contains(e))
				return String.format("$(%d)", args.indexOf(e));
			else
				return e.toString();
		};

		if (raw instanceof CtBinaryOperator bo) {
			BinaryOperatorKind bokind = bo.getKind();
			CtExpression lhs = bo.getLeftHandOperand();
			CtExpression rhs = bo.getRightHandOperand();
			String[] exprs = new String[] { bokind.toString(), convert.apply(lhs), convert.apply(rhs) };
			return new NullValue("BINARY", exprs, bo);
		}

		String converted = convert.apply(raw);
		if (converted.equals(raw)) {
			return createPlain("NPEXNonLiteral", raw);
		}

		return createPlain(converted, raw);

	}

	private static NullValue createPlain(String expr, CtExpression raw) {
		return new NullValue("PLAIN", new String[] { expr }, raw);
	}

	private static NullValue createBinary(CtBinaryOperator bo, CtAbstractInvocation invo) {
		BinaryOperatorKind bokind = bo.getKind();
		CtExpression lhs = bo.getLeftHandOperand();
		CtExpression rhs = bo.getRightHandOperand();
		CtExpression base = invo instanceof CtInvocation vinvo ? vinvo.getTarget() : null;
		List<CtExpression> args = invo.getArguments();

		Function<CtExpression, String> convert = e -> {
			if (e.equals(base))
				return "$(-1)";
			else if (args.contains(e))
				return String.format("$(%d)", args.indexOf(e));
			else
				return e.toString();
		};
		String[] exprs = new String[] { bokind.toString(), convert.apply(lhs), convert.apply(rhs) };
		return new NullValue("BINARY", exprs, bo);
	}

	private static String convert(CtExpression org, CtExpression base, List<CtExpression> args) {
		if (org.equals(base))
			return "$(-1)";
		else if (args.contains(org))
			return String.format("$(%d)", args.indexOf(org));
		else
			return org.toString();
	}

	private static NullValue createLiteral(CtLiteral lit) {
		return createPlain(lit.toString(), lit);
	}

	private static NullValue createConstructorCall(CtConstructorCall ccall) {
		String expr = ccall.getArguments().isEmpty() ? "NPEXDefaultNew" : "NPEXNonDefaultNew";
		return createPlain(expr, ccall);
	}

	private static NullValue createEmptyCollections(CtExpression<? extends java.util.Collection> raw) {
		return createPlain("NPEXEmptyCollections", raw);
	}

	public static NullValue create(CtAbstractInvocation invo, CtExpression raw) throws NPEXException {
		if (raw == null) {
			throw new NPEXException(String.format("Cannot extract null values for %s: null-value expression is NULL!", invo));
		}

		if (invo == null) {
			throw new NPEXException(String.format("Cannot extract null values for %s: null-value expression is NULL!", invo));
		}

		CtTypeReference type = raw.getType();
		CtTypeReference invoRetType = invo.getExecutable().getType();
		if (type == null || invoRetType == null) {
			throw new NPEXException(
					String.format("Cannot extract null values for %s: type information is unavailable", invo));
		}

		if (raw.toString().equals("null")) {
			if (invoRetType.isPrimitive()) {
				throw new NPEXException(String.format("null literal cannot be a null value for %s", invo));
			}
			return NULL_LIT;
		}

		if (!type.isSubtypeOf(invoRetType)) {
			throw new NPEXException(String.format("null-value's type (%s) should be subtype of invocation's return type (%s)",
					type, invoRetType));
		}

		if (raw instanceof CtLiteral lit) {
			return createLiteral(lit);
		}

		if (raw instanceof CtConstructorCall ccall) {
			return createConstructorCall(ccall);
		}

		if (raw.toString().equals("java.lang.Object.class")) {
			return OBJECT_CLASS;
		}

		if (Arrays.asList(emptyCollections).contains(raw.toString())) {
			return EMPTY_COLLECTIONS;
		}

		return createNonLiteral(raw, invo);
	}
}