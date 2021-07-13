package npex.extractor.nullhandle;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.json.JSONArray;
import org.json.JSONObject;

import npex.common.utils.FactoryUtils;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtVariable;

public class NullValue {
	final private String kind;
	final private String[] exprs;
	final private CtExpression raw;


	private static final List<String> emptyCollections = Arrays.asList(new String[]{ "java.util.Collections.EMPTY_LIST",
			"java.util.Collections.EMPTY_MAP", "java.util.Collections.EMPTY_SET" });
  private static final List<String> defaultValues = Arrays.asList(new String[]{ "0", "0L", "0.0F", "false", "java.lang.Boolean.FALSE", "\"\"", "'\\u0000'" });
	public static final NullValue SKIP = new NullValue("PLAIN", new String[] {"NPEX_SKIP_VALUE"}, null);


 
  private NullValue(String kind, String[] exprs, CtExpression raw) {
    this.kind = kind;
    this.exprs = exprs;
    this.raw = raw;
  }
	public JSONObject toJSON() {
		var obj = new JSONObject();
		obj.put("kind", kind);
		obj.put("exprs", new JSONArray(exprs));
		obj.put("raw", raw == null ? JSONObject.NULL : raw.toString());
		return obj;
	}

	public String getRawString() {
		return raw != null ? raw.toString() : null;
	}
  public static NullValue fromExpression(CtAbstractInvocation invo, CtExpression raw) {
    if (raw instanceof CtBinaryOperator bo) {
      BinaryOperatorKind bokind = bo.getKind();
      CtExpression lhs = bo.getLeftHandOperand();
      CtExpression rhs = bo.getRightHandOperand();
      String[] exprs = new String[] { bokind.toString(), convert(lhs, invo), convert(rhs, invo) };
      return new NullValue("BINARY", exprs, bo);
    }

    String converted = convert(raw, invo);
    return new NullValue("PLAIN", new String[] {converted}, raw);
  }

 private static String convert(CtExpression raw, CtAbstractInvocation invo) {
   String result;
    if (raw.equals(FactoryUtils.NULL_LIT)) {
      return "null";
    }

    result = convertLiteral(raw);
    if (result != null) {
      return result;
    }

    if (raw instanceof CtConstructorCall ccall) {
      return ccall.getArguments().isEmpty() ? "NPEXDefaultNew" : "NPEXNonDefaultNew";
    }

    if (raw.toString().equals("java.lang.Object.class")) {
      return "java.lang.Object.class";
    }

    if (emptyCollections.contains(raw.toString())) {
      return "NPEXEmptyCollections";
    }

    CtExpression base = invo instanceof CtInvocation vinvo ? vinvo.getTarget() : null;
    List<CtExpression> args = invo != null ? invo.getArguments() : Collections.EMPTY_LIST;
    Function<CtExpression, String> symbolize = e -> {
      if (e.equals(base))
        return "$(-1)";
      else if (args.contains(e))
        return String.format("$(%d)", args.indexOf(e));
      else
        return e.toString();
    };

		String converted = symbolize.apply(raw);
    return raw.toString().equals(converted) ? "NPEXNonLiteral" : converted;
  }


  private static String convertLiteral(CtExpression raw) {
    if (raw instanceof CtLiteral lit) {
      return defaultValues.contains(lit.toString()) ? "NPEX_DEFAULT_LITERAL" : "NPEX_NON_DEFAULT_LITERAL";
    } else if (raw instanceof CtVariableRead read) {
      try {
        CtVariable var = read.getVariable().getDeclaration();
        if (var.isFinal() && var.getDefaultExpression()instanceof CtLiteral lit)
          return defaultValues.contains(lit.toString()) ? "NPEX_DEFAULT_LITERAL" : "NPEX_NON_DEFAULT_LITERAL";
      } catch (NullPointerException e) {
        return null;
      }
    }
    return null;
  }
}