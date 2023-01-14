package xyz.wagyourtail.fukkit2.mixinpatcher;

import net.lenni0451.classtransform.TransformerManager;
import net.lenni0451.classtransform.targets.IInjectionTarget;
import net.lenni0451.classtransform.transformer.types.RemovingTargetAnnotationHandler;
import net.lenni0451.classtransform.utils.annotations.AnnotationParser;
import net.lenni0451.classtransform.utils.tree.IClassProvider;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.wagyourtail.fukkit2.mixinpatcher.annotations.CChangeMethods;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CMethodChanger extends RemovingTargetAnnotationHandler<CChangeMethods> {
    public static final Map<String, Class<? extends Annotation>> ANNOTATIONS = List.of(
        Redirect.class,
        Inject.class,
        ModifyVariable.class
        //TODO: the rest
    ).stream().collect(Collectors.toMap(e -> "L" + e.getCanonicalName().replace('.', '/') + ";", Function.identity()));

    public CMethodChanger() {
        super(CChangeMethods.class, CChangeMethods::value);
    }

    @Override
    public void transform(CChangeMethods annotation, TransformerManager transformerManager, IClassProvider classProvider, Map<String, IInjectionTarget> injectionTargets, ClassNode transformedClass, ClassNode transformer, MethodNode transformerMethod, MethodNode target) {
        // remove matching desc's from target
        boolean flag = false;
        for (AnnotationNode a : target.visibleAnnotations) {
            for (String an : ANNOTATIONS.keySet()) {
                if (an.equals(a.desc)) {
                    changeTarget(a, annotation.target(), ANNOTATIONS.get(an));
                    flag = true;
                }
            }
        }
        for (AnnotationNode a : target.invisibleAnnotations) {
            for (String an : ANNOTATIONS.keySet()) {
                if (an.equals(a.desc)) {
                    changeTarget(a, annotation.target(), ANNOTATIONS.get(an));
                    flag = true;
                }
            }
        }
        if (!flag) {
            throw new RuntimeException("No valid annotations found on target method");
        }
    }

    private void changeTarget(AnnotationNode node, String[] newTargets, Class<? extends Annotation> annotation) {
        for (int i = 0; i < node.values.size(); i += 2) {
            if (node.values.get(i).equals("method")) {
                node.values.set(i + 1, Arrays.asList(newTargets));
                return;
            }
        }
        throw new RuntimeException("Couldn't find method in annotation");
    }

}
