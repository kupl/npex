package npex.common.utils;

import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtTypeReference;

public class TypeUtil {
	public static final TypeFactory f = new TypeFactory();

  public static final CtTypeReference<?> NULL_TYPE = f.NULL_TYPE;
  
  public static final CtTypeReference<java.lang.Void> VOID = f.VOID_PRIMITIVE;
  public static final CtTypeReference<java.lang.String> STRING = f.STRING;
  public static final CtTypeReference<java.lang.Boolean> BOOLEAN = f.BOOLEAN;
	public static final CtTypeReference<java.lang.Integer> INTEGER = f.INTEGER;
  public static final CtTypeReference<java.lang.Long> LONG = f.LONG;
  public static final CtTypeReference<java.lang.Float> FLOAT = f.FLOAT;
  public static final CtTypeReference<java.lang.Double> DOUBLE = f.DOUBLE;
  public static final CtTypeReference<java.lang.Void> VOID_PRIMITIVE = f.VOID_PRIMITIVE;
  public static final CtTypeReference<java.lang.Boolean> BOOLEAN_PRIMITIVE = f.BOOLEAN_PRIMITIVE;
  public static final CtTypeReference<java.lang.Integer> INTEGER_PRIMITIVE = f.INTEGER_PRIMITIVE;
  public static final CtTypeReference<java.lang.Float> FLOAT_PRIMITIVE = f.FLOAT_PRIMITIVE;
  public static final CtTypeReference<java.lang.Double> DOUBLE_PRIMITIVE = f.DOUBLE_PRIMITIVE;
  public static final CtTypeReference<java.lang.Object> OBJECT = f.OBJECT;
  
	public static final CtTypeReference<java.util.Collection> COLLECTION = f.COLLECTION;
	public static final CtTypeReference<java.util.Enumeration> ENUMERATION = f.createReference(java.util.Enumeration.class);
  public static final CtTypeReference<java.lang.Class> CLASS = f.createReference(java.lang.Class.class);

  public static final CtTypeReference<java.lang.StringBuilder> STRING_BUILDER = f.createReference(java.lang.StringBuilder.class);
}