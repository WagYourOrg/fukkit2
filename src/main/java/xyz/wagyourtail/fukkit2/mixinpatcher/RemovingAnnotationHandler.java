package xyz.wagyourtail.fukkit2.mixinpatcher;

import net.lenni0451.classtransform.TransformerManager;
import net.lenni0451.classtransform.targets.IInjectionTarget;
import net.lenni0451.classtransform.transformer.AnnotationHandler;
import net.lenni0451.classtransform.utils.ASMUtils;
import net.lenni0451.classtransform.utils.tree.IClassProvider;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.tree.*;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Map;

public abstract class RemovingAnnotationHandler<T extends Annotation> extends AnnotationHandler {

    private final Class<? extends Annotation> annotationClass;

    public RemovingAnnotationHandler(final Class<T> annotationClass) {
        this.annotationClass = annotationClass;
    }

    @Override
    public final void transform(TransformerManager transformerManager, IClassProvider classProvider, Map<String, IInjectionTarget> injectionTargets, ClassNode transformedClass, ClassNode transformer) {
        Iterator<MethodNode> itm = transformer.methods.iterator();
        while (itm.hasNext()) {
            MethodNode transformerMethod = itm.next();
            T annotation = (T) this.getAnnotation(this.annotationClass, transformerMethod, classProvider);
            if (annotation == null) continue;
            itm.remove();

            this.transform(annotation, transformerManager, classProvider, injectionTargets, transformedClass, transformer, ASMUtils.cloneMethod(transformerMethod));
        }
        Iterator<FieldNode> itf = transformer.fields.iterator();
        while (itf.hasNext()) {
            FieldNode transformerMethod = itf.next();
            T annotation = (T) this.getAnnotation(this.annotationClass, transformerMethod, classProvider);
            if (annotation == null) continue;
            itf.remove();

            this.transform(annotation, transformerManager, classProvider, injectionTargets, transformedClass, transformer, cloneField(transformerMethod));
        }
    }

    /**
     * Transform the target class using the given transformer class
     *
     * @param annotation         The annotation of the transformer
     * @param transformerManager The transformer manager
     * @param classProvider      The class provider
     * @param injectionTargets   The available injection targets
     * @param transformedClass   The target {@link ClassNode}
     * @param transformer        The transformer {@link ClassNode}
     * @param transformerMethod  The current {@link MethodNode} of the transformer
     */
    public abstract void transform(final T annotation, final TransformerManager transformerManager, final IClassProvider classProvider, final Map<String, IInjectionTarget> injectionTargets, final ClassNode transformedClass, final ClassNode transformer, final MethodNode transformerMethod);

    /**
     * Transform the target class using the given transformer class
     *
     * @param annotation         The annotation of the transformer
     * @param transformerManager The transformer manager
     * @param classProvider      The class provider
     * @param injectionTargets   The available injection targets
     * @param transformedClass   The target {@link ClassNode}
     * @param transformer        The transformer {@link ClassNode}
     * @param transformerField  The current {@link FieldNode} of the transformer
     */
    public abstract void transform(final T annotation, final TransformerManager transformerManager, final IClassProvider classProvider, final Map<String, IInjectionTarget> injectionTargets, final ClassNode transformedClass, final ClassNode transformer, final FieldNode transformerField);

    /**
     * Clone a {@link MethodNode}
     *
     * @param methodNode The {@link MethodNode} to clone
     * @return The cloned {@link MethodNode}
     */
    public static FieldNode cloneField(final FieldNode methodNode) {
        FieldNode clonedMethod = new FieldNode(methodNode.access, methodNode.name, methodNode.desc, methodNode.signature, methodNode.value);
        if (methodNode.visibleAnnotations != null) {
            for (AnnotationNode node : methodNode.visibleAnnotations) {
                AnnotationVisitor visitor = clonedMethod.visitAnnotation(node.desc, true);
                node.accept(visitor);
            }
        }
        if (methodNode.invisibleAnnotations != null) {
            for (AnnotationNode node : methodNode.invisibleAnnotations) {
                AnnotationVisitor visitor = clonedMethod.visitAnnotation(node.desc, false);
                node.accept(visitor);
            }
        }
        if (methodNode.visibleTypeAnnotations != null) {
            for (TypeAnnotationNode node : methodNode.visibleTypeAnnotations) {
                AnnotationVisitor visitor = clonedMethod.visitTypeAnnotation(
                    node.typeRef,
                    node.typePath,
                    node.desc,
                    true
                );
                node.accept(visitor);
            }
        }
        if (methodNode.invisibleTypeAnnotations != null) {
            for (TypeAnnotationNode node : methodNode.invisibleTypeAnnotations) {
                AnnotationVisitor visitor = clonedMethod.visitTypeAnnotation(
                    node.typeRef,
                    node.typePath,
                    node.desc,
                    false
                );
                node.accept(visitor);
            }
        }
        if (methodNode.attrs != null) {
            for (Attribute attribute : methodNode.attrs) {
                clonedMethod.visitAttribute(attribute);
            }
        }
        return clonedMethod;
    }

}
