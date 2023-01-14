package xyz.wagyourtail.fukkit2.mixinpatcher;

import net.lenni0451.classtransform.TransformerManager;
import net.lenni0451.classtransform.targets.IInjectionTarget;
import net.lenni0451.classtransform.utils.tree.IClassProvider;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import xyz.wagyourtail.fukkit2.mixinpatcher.annotations.CRemoveAnotation;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CAnotationRemover extends RemovingTargetAnnotationHandler<CRemoveAnotation> {
    public CAnotationRemover() {
        super(CRemoveAnotation.class, CRemoveAnotation::value);
    }

    @Override
    public void transform(CRemoveAnotation annotation, TransformerManager transformerManager, IClassProvider classProvider, Map<String, IInjectionTarget> injectionTargets, ClassNode transformedClass, ClassNode transformer, MethodNode transformerMethod, MethodNode target) {
        List<String> annotations = Arrays.stream(annotation.annotation()).map(e -> "L" + e.getCanonicalName().replace('.', '/') + ";").toList();
        // remove matching desc's from target
        if (target.visibleAnnotations != null) {
            target.visibleAnnotations.removeIf(an -> annotations.contains(an.desc));
        }
        if (target.invisibleAnnotations != null) {
            target.invisibleAnnotations.removeIf(an -> annotations.contains(an.desc));
        }
    }

    @Override
    public void transform(CRemoveAnotation annotation, TransformerManager transformerManager, IClassProvider classProvider, Map<String, IInjectionTarget> injectionTargets, ClassNode transformedClass, ClassNode transformer, FieldNode transformerMethod, FieldNode target) {
        List<String> annotations = Arrays.stream(annotation.annotation()).map(e -> "L" + e.getCanonicalName().replace('.', '/') + ";").toList();
        // remove matching desc's from target
        if (target.visibleAnnotations != null) {
            target.visibleAnnotations.removeIf(an -> annotations.contains(an.desc));
        }
        if (target.invisibleAnnotations != null) {
            target.invisibleAnnotations.removeIf(an -> annotations.contains(an.desc));
        }
    }

}
