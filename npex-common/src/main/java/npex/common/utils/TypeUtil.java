package npex.common.utils;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.DefaultCoreFactory;

public class TypeUtil {
	private static final DefaultCoreFactory df = new DefaultCoreFactory();
	private static final TypeFactory f = new TypeFactory();
  
  public static final CtTypeReference<java.lang.Void> VOID = f.VOID_PRIMITIVE;
  public static final CtTypeReference<java.lang.String> STRING = f.STRING;
  public static final CtTypeReference<java.lang.Boolean> BOOLEAN = f.BOOLEAN;
	public static final CtTypeReference<java.lang.Integer> INTEGER = f.INTEGER;
  public static final CtTypeReference<java.lang.Float> FLOAT = f.FLOAT;
  public static final CtTypeReference<java.lang.Double> DOUBLE = f.DOUBLE;

  public static final CtTypeReference<?> NULL_TYPE = f.NULL_TYPE;

  /* 8 Java primitives */
  public static final CtTypeReference<java.lang.Short> SHORT_PRIMITIVE = f.SHORT_PRIMITIVE;
  public static final CtTypeReference<java.lang.Byte> BYTE_PRIMITIVE = f.BYTE_PRIMITIVE;
  public static final CtTypeReference<java.lang.Integer> INTEGER_PRIMITIVE = f.INTEGER_PRIMITIVE;
  public static final CtTypeReference<java.lang.Long> LONG_PRIMITIVE = f.LONG_PRIMITIVE;
  public static final CtTypeReference<java.lang.Float> FLOAT_PRIMITIVE = f.FLOAT_PRIMITIVE;
  public static final CtTypeReference<java.lang.Double> DOUBLE_PRIMITIVE = f.DOUBLE_PRIMITIVE;
  public static final CtTypeReference<java.lang.Boolean> BOOLEAN_PRIMITIVE = f.BOOLEAN_PRIMITIVE;
  public static final CtTypeReference<java.lang.Character> CHAR_PRIMITIVE = f.CHARACTER_PRIMITIVE;

  public static final CtTypeReference<java.lang.Void> VOID_PRIMITIVE = f.VOID_PRIMITIVE;
  public static final CtTypeReference<java.lang.Object> OBJECT = f.OBJECT;
  
	public static final CtTypeReference<java.util.Collection> COLLECTION = f.COLLECTION;
	public static final CtTypeReference<java.util.List> LIST = f.LIST;
	public static final CtTypeReference<java.util.Map> MAP = f.MAP;
	public static final CtTypeReference<java.util.Set> SET = f.SET;
	public static final CtTypeReference<java.util.Enumeration> ENUMERATION = f.createReference(java.util.Enumeration.class);
  public static final CtTypeReference<java.lang.Class> CLASS = f.createReference(java.lang.Class.class);

  public static final CtTypeReference<java.lang.StringBuilder> STRING_BUILDER = f.createReference(java.lang.StringBuilder.class);

  private static final CtLiteral NULL_LIT = df.createLiteral().setValue(null);

  static {
    NULL_LIT.setType(f.NULL_TYPE);
  }

  public static boolean isNullLiteral(CtExpression e) {
    if (e.equals(NULL_LIT))
      return true;

    CtTypeReference typ = e.getType();
    if (typ != null && typ.equals(f.NULL_TYPE)) {
      return true;
    }

    return false;
  }
}