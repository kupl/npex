package npex.extractor.nullhandle;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import npex.common.CommonExpressionTable;
import npex.common.NPEXException;
import npex.common.helper.TypeHelper;
import npex.common.utils.TypeUtil;
import npex.extractor.nullhandle.NullValue.KIND;
import npex.extractor.runtime.RuntimeMethodInfo;
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
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtModifiable;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

public class NullValue {
  static Logger logger = LoggerFactory.getLogger(NullValue.class);

  public enum KIND {
    PLAIN, BINARY, DONT_LEARN
  }

  final private KIND kind;
  final private String[] exprs;
  final private CtExpression raw;
  final private CtTypeReference type;
  final private CtAbstractInvocation invo;
  private boolean negated;

  private static final HashMap<CtExecutable, Boolean> builderPatternTable = new HashMap<>();

  private static final NullValue SKIP = createPlain(new String[] { "NPEX_SKIP_VALUE" }, null, null, null);
  private static final NullValue THIS = createPlain(new String[] { "$(-1)" }, null, null, null);

  private NullValue(KIND kind, String[] exprs, CtExpression raw, CtTypeReference type, CtAbstractInvocation invo) {
    this.kind = kind;
    this.exprs = exprs;
    this.raw = raw;
    this.type = type;
    this.invo = invo;
    this.negated = false;
  }

  public boolean isSkip() {
    return this == SKIP;
  }

  public JSONObject toJSON() {
    var obj = new JSONObject();
    obj.put("kind", kind);
    obj.put("exprs", new JSONArray(exprs));
    obj.put("raw", raw == null ? JSONObject.NULL : (negated ? String.format("!(%s)", raw.toString()) : raw.toString()));
    obj.put("raw_type", type == null ? JSONObject.NULL : type.toString());
    return obj;
  }

  public NullValue negate() {
    if (kind.equals(KIND.BINARY) && (exprs[0].equals("NE") || exprs[0].equals("EQ"))) {
      this.exprs[0] = this.exprs[0].equals("EQ") ? "NE" : "EQ";
      this.negated = true;
      return this;
    } else if (kind.equals(KIND.PLAIN) && (exprs[0].equals("true") || exprs[0].equals("false"))) {
      this.exprs[0] = Boolean.toString(!Boolean.valueOf(this.exprs[0]));
      this.negated = true;
      return this;
    } else {
      logger.error("Could not negate null value: {}", toJSON().toString());
      return null;
    }
  }

  public static NullValue fromExpression(CtAbstractInvocation invo, CtExpression raw) {
    CtTypeReference type = TypeHelper.getType(raw);
    CtTypeReference invoRetType = TypeHelper.getType(invo);
    if (type == null || invoRetType == null) {
      String msg = String.format("Cannot extract null values for %s: type information is unavailable", invo);
      throw new NPEXException(invo, msg);
    }

    if (TypeUtil.isNullLiteral(raw) && invoRetType.isSubtypeOf(TypeUtil.OBJECT)) {
      return createPlain(new String[] { "null" }, raw, TypeUtil.NULL_TYPE, invo);
    }

    try {
      if (!type.isSubtypeOf(invoRetType)) {
        logger.error("{}: null-value {}'s type {} should be subtype of invocation's type {}", raw.getPosition(), raw,
            type, invoRetType);
        return null;
      }
    } catch (NullPointerException e) {
      logger.error("{}: null-value {}'s type {} should be subtype of invocation's type {}", raw.getPosition(), raw,
          invoRetType, invoRetType);
      return null;
    }

    try {
      if (raw instanceof CtBinaryOperator bo) {
        BinaryOperatorKind bokind = bo.getKind();
        CtExpression lhs = bo.getLeftHandOperand();
        CtExpression rhs = bo.getRightHandOperand();
        String[] exprs = new String[] { bokind.toString(), convert(lhs, invo), convert(rhs, invo) };
        return createBinary(exprs, bo, type, invo);
      } else {
        String converted = convert(raw, invo);
        return createPlain(new String[] { converted }, raw, type, invo);
      }
    } catch (NPEXException e) {
      logger.error(e.getMessage());
      return null;
    }
  }


  public static NullValue createSkip(CtAbstractInvocation invo) {
    return SKIP;
  }

  /*
   * A method that creates a skip value. In case of a builder pattern method,
   * where the method is virtual and returns 'this', create a null value with
   * 'this'.
   */
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
      String packagePath = exec.getParent(CtPackage.class).toString();
      if (packagePath.startsWith("java.util") || packagePath.startsWith("java.lang")) {
        return RuntimeMethodInfo.hasCalleeBuilderPatterns(exec) ? THIS : SKIP;
      }

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

    // deal with final variable initialzations
    if (raw instanceof CtVariableRead read) {
      try {
        CtVariable var = read.getVariable().getDeclaration();
        if (var.isFinal() && var.getDefaultExpression() != null)
          raw = var.getDefaultExpression();
      } catch (NullPointerException e) {
      }
    }

    // deal with literal NULL
    if (TypeUtil.isNullLiteral(raw)) {
      return "null";
    }

    if (raw instanceof CtLiteral) {
      return "NPEXOtherLiteral";
    }

    if (raw instanceof CtConstructorCall ccall) {
      return ccall.getArguments().isEmpty() ? "NPEXDefaultNew" : "NPEXNonDefaultNew";
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

  private static NullValue createPlain(String[] exprs, CtExpression raw, CtTypeReference type, CtAbstractInvocation invo) {
    return new NullValue(KIND.PLAIN, exprs, raw, type, invo);
  }

  private static NullValue createBinary(String[] exprs, CtExpression raw, CtTypeReference type, CtAbstractInvocation invo) {
    return new NullValue(KIND.BINARY, exprs, raw, type, invo);
  }

}