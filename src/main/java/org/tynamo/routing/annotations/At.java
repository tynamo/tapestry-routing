package org.tynamo.routing.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.tynamo.routing.Behavior;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface At {

	String value();

	Behavior behavior() default Behavior.DEFAULT;

	String[] order() default {};

}
