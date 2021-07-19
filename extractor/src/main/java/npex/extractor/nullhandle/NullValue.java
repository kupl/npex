package npex.extractor.nullhandle;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import npex.common.NPEXException;
import npex.common.helper.TypeHelper;
import npex.common.utils.FactoryUtils;
import npex.common.utils.TypeUtil;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.code.UnaryOperatorKind;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

public class NullValue {
  static Logger logger = LoggerFactory.getLogger(NullValue.class);
	final private String kind;
	final private String[] exprs;
	final private CtExpression raw;

	private static final HashMap<CtExecutable, Boolean> builderPatternTable = new HashMap<>();
	private static final List<String> emptyCollections = Arrays.asList(new String[]{ "java.util.Collections.EMPTY_LIST",
			"java.util.Collections.EMPTY_MAP", "java.util.Collections.EMPTY_SET" });
	private static final List<String> defaultValues = Arrays.asList(new String[]{ "0", "0L", "0.0F", "false", "java.lang.Boolean.FALSE", "\"\"", "'\\u0000'" });

	private static final NullValue SKIP = new NullValue("PLAIN", new String[] { "NPEX_SKIP_VALUE" }, null);
	private static final NullValue THIS = new NullValue("PLAIN", new String[] { "this" }, null);

 
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
    obj.put("isConstant", raw != null ? isConstant(raw) : false);
		return obj;
	}

	public String getRawString() {
		return raw != null ? raw.toString() : null;
	}

  public static NullValue fromExpression(CtAbstractInvocation invo, CtExpression raw) {
    try {
      if (raw instanceof CtBinaryOperator bo) {
        BinaryOperatorKind bokind = bo.getKind();
        CtExpression lhs = bo.getLeftHandOperand();
        CtExpression rhs = bo.getRightHandOperand();
        String[] exprs = new String[] { bokind.toString(), convert(lhs, invo), convert(rhs, invo) };
        return new NullValue("BINARY", exprs, bo);
      } else {
        String converted = convert(raw, invo);
        return new NullValue("PLAIN", new String[] { converted }, raw);
      }
    } catch (NPEXException e) {
      logger.error(e.getMessage());
      return null;
    }
  }

  public static NullValue createSkip(CtAbstractInvocation invo) {
    return SKIP;
  }

  /* A method that creates a skip value. In case of a builder pattern method, where
  the method is virtual and returns 'this', create a null value with 'this'. */
  public static NullValue createSkip(CtInvocation invo) {
    CtExecutableReference execRef = invo.getExecutable();
    if (execRef.isStatic() || execRef.isConstructor()) {
      return SKIP;
    }

    CtExecutable exec = execRef.getExecutableDeclaration();
    if (exec == null) {
      return SKIP;
    }

    if (builderPatternTable.containsKey(exec)) {
      return builderPatternTable.get(exec) ? THIS : SKIP;
    }

    if (exec != null) {
      List<CtReturn> returns = exec.getElements(new TypeFilter<>(CtReturn.class));
      Predicate<CtReturn> returnsThis = ret -> {
        CtExpression rexp = ret.getReturnedExpression();
        return rexp != null && rexp.toString().equals("this");
      };

      if (!returns.isEmpty() && returns.stream().allMatch(returnsThis)) {
        builderPatternTable.put(exec, true);
        return THIS;
      } else {
        builderPatternTable.put(exec, false);
      }
    }
    return SKIP;
  }

 private static String convert(CtExpression raw, CtAbstractInvocation invo) {
   String result;
   CtTypeReference type = TypeHelper.getType(raw);
   CtTypeReference invoRetType = TypeHelper.getType(invo);

   if (type == null || invoRetType == null) {
     String msg = String.format("Cannot extract null values for %s: type information is unavailable", invo);
     throw new NPEXException(invo, msg);
   }

   if (raw.equals(FactoryUtils.NULL_LIT) && invoRetType.isSubtypeOf(TypeUtil.OBJECT)) {
      return "null";
   }

   if (!type.isSubtypeOf(invoRetType)) {
     String msg = String.format("null-value's type (%s) should be subtype of invocation's return type (%s)", type,
              invoRetType);
			throw new NPEXException(invo, msg);
		}


    result = convertLiteral(raw);
    if (result != null) {
      return result;
    }

    if (raw.toString().equals("java.lang.Object.class")) {
      return "java.lang.Object.class";
    }

    if (raw instanceof CtConstructorCall ccall) {
      return ccall.getArguments().isEmpty() ? "NPEXDefaultNew" : "NPEXNonDefaultNew";
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

  private static boolean isLiteral(CtExpression e) {
    if (e instanceof CtLiteral)
      return true;
    
    return e instanceof CtUnaryOperator un && un.getKind().equals(UnaryOperatorKind.NEG) && un.getOperand() instanceof CtLiteral;
  }


  private static String convertLiteral(CtExpression raw) {
    if (isLiteral(raw)) {
      return defaultValues.contains(raw.toString()) ? "NPEX_DEFAULT_LITERAL" : "NPEX_NON_DEFAULT_LITERAL";
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

  private static boolean isConstant(CtExpression raw) {
    if (isLiteral(raw))
      return true;

    if (raw instanceof CtFieldRead read) {
      try {
        CtField fld = read.getVariable().getFieldDeclaration();
        String pkgName = fld.getDeclaringType().getPackage().getQualifiedName();
        if (pkgName.startsWith("java.lang") || pkgName.equals("java.util")) {
          if (fld.isStatic() && fld.isFinal() && fld.isPublic()) {
            return true;
          }
        }
      } catch (Exception e) {
        return false;
      }
    }
    return false;
  }
}