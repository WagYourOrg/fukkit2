package xyz.wagyourtail.fukkit2.mixinpatcher.annotations;

import net.lenni0451.classtransform.mappings.annotation.AnnotationRemap;
import net.lenni0451.classtransform.mappings.annotation.RemapType;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface CRemoveAnotation {
    @AnnotationRemap(RemapType.SHORT_MEMBER)
    String[] value();
    Class<? extends Annotation>[] annotation();
}
