package fanap.dinner.document;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * AdminAPI annotation. use it for controller(class)s and endpoint(method)s should be in private spec.
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface AdminAPI {
    String value() default "";
}