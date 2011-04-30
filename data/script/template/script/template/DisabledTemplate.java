package script.template;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Documented;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;


/**
 * Marker annotation that is used to mark disabled DAO's so they will be ignored by {@link TemplateLoader}
 * 
 * @author SoulKeeper
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DisabledTemplate
{
}