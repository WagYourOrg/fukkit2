package xyz.wagyourtail.fukkit2.mixinpatcher;

import net.lenni0451.classtransform.TransformerManager;
import net.lenni0451.classtransform.targets.IInjectionTarget;
import net.lenni0451.classtransform.utils.tree.IClassProvider;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import xyz.wagyourtail.fukkit2.mixinpatcher.annotations.CAddAnnotation;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CAnnotationAdder extends RemovingTargetAnnotationHandler<CAddAnnotation> {

    public CAnnotationAdder() {
        super(CAddAnnotation.class, CAddAnnotation::value);
    }

    @Override
    public void transform(CAddAnnotation annotation, TransformerManager transformerManager, IClassProvider classProvider, Map<String, IInjectionTarget> injectionTargets, ClassNode transformedClass, ClassNode transformer, MethodNode transformerMethod, MethodNode target) {
        List<String> annotations = Arrays.stream(annotation.annotation()).map(e -> "L" + e.getCanonicalName().replace(".", "/") + ";").collect(Collectors.toList());
        for (AnnotationNode node : transformerMethod.visibleAnnotations) {
            if (annotations.contains(node.desc)) {
                target.visibleAnnotations.add(node);
                annotations.remove(node.desc);
            }
        }
        for (AnnotationNode node : transformerMethod.invisibleAnnotations) {
            if (annotations.contains(node.desc)) {
                target.invisibleAnnotations.add(node);
                annotations.remove(node.desc);
            }
        }
        if (annotations.size() > 0) {
            throw new RuntimeException("Could not find annotations: " + annotations);
        }
    }

    @Override
    public void transform(CAddAnnotation annotation, TransformerManager transformerManager, IClassProvider classProvider, Map<String, IInjectionTarget> injectionTargets, ClassNode transformedClass, ClassNode transformer, FieldNode transformerMethod, FieldNode target) {
        List<String> annotations = Arrays.stream(annotation.annotation()).map(e -> "L" + e.getCanonicalName().replace(".", "/") + ";").collect(Collectors.toList());
        for (AnnotationNode node : transformerMethod.visibleAnnotations) {
            if (annotations.contains(node.desc)) {
                target.visibleAnnotations.add(node);
                annotations.remove(node.desc);
            }
        }
        for (AnnotationNode node : transformerMethod.invisibleAnnotations) {
            if (annotations.contains(node.desc)) {
                target.invisibleAnnotations.add(node);
                annotations.remove(node.desc);
            }
        }
        if (annotations.size() > 0) {
            throw new RuntimeException("Could not find annotations: " + annotations);
        }
    }

}
