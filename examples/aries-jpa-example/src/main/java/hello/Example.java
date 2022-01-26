package hello;
import org.junit.Assert;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;
interface I { }

public class Example {
    public static List<Object> getJpaAnnotatedMembers(Class<?> c, Class<? extends java.lang.annotation.Annotation> annotation) {
        final List<Object> jpaAnnotated = new ArrayList<Object>();
        // for (Class<?> cl = c; cl != Object.class && cl != null; cl = cl.getSuperclass()) { /* developer's patch */
        for (Class<?> cl = c; cl != Object.class; cl = cl.getSuperclass()) {  
            parseClass(annotation, jpaAnnotated, cl);
        }
        return jpaAnnotated;
    }
    private static void parseClass(Class<? extends java.lang.annotation.Annotation> annotation, final List<Object> jpaAnnotated, Class<?> cl) {
        for (Field field : cl.getDeclaredFields()) {
            if (field.isAnnotationPresent(annotation)) {
                field.setAccessible(true);
                jpaAnnotated.add(field);
            }
        }
        for (Method method : cl.getDeclaredMethods()) {
            if ((method.isAnnotationPresent(annotation)) && method.getName().startsWith("set") && method.getParameterTypes().length == 1) {
                jpaAnnotated.add(method);
            }
        }
    }

  public static void goo() {
    getJpaAnnotatedMembers(I.class, java.lang.Deprecated.class);
  }

}
