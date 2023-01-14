package xyz.wagyourtail.fukkit2.mixinpatcher.annotations;

import net.lenni0451.classtransform.mappings.annotation.AnnotationRemap;
import net.lenni0451.classtransform.mappings.annotation.RemapType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD})
public @interface CChangeMethods {
    @AnnotationRemap(RemapType.SHORT_MEMBER)
    String[] value();

//    @AnnotationRemap(RemapType.SHORT_MEMBER)
    String[] target();
}
