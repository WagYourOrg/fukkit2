package xyz.wagyourtail.fukkit2.mixinpatcher;

import net.lenni0451.classtransform.TransformerManager;
import net.lenni0451.classtransform.targets.IInjectionTarget;
import net.lenni0451.classtransform.transformer.AnnotationHandler;
import net.lenni0451.classtransform.utils.tree.IClassProvider;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import xyz.wagyourtail.fukkit2.mixinpatcher.annotations.CUnique;

import java.util.Iterator;
import java.util.Map;

import static xyz.wagyourtail.fukkit2.mixinpatcher.RemovingAnnotationHandler.cloneField;

public class CUniqueAdder extends AnnotationHandler {

    @Override
    public void transform(TransformerManager transformerManager, IClassProvider classProvider, Map<String, IInjectionTarget> injectionTargets, ClassNode transformedClass, ClassNode transformer) {
        Iterator<FieldNode> itf = transformer.fields.iterator();
        while (itf.hasNext()) {
            FieldNode transformerMethod = itf.next();
            CUnique annotation = this.getAnnotation(CUnique.class, transformerMethod, classProvider);
            if (annotation == null) continue;
            itf.remove();

            this.transform(annotation, transformerManager, classProvider, injectionTargets, transformedClass, transformer, cloneField(transformerMethod)); //, remapper);

            //TODO: methods
        }
    }

    public void transform(CUnique annotation, TransformerManager transformerManager, IClassProvider classProvider, Map<String, IInjectionTarget> injectionTargets, ClassNode transformedClass, ClassNode transformer, FieldNode transformerField) {//, final MapRemapper remapper) {
        for (FieldNode f : transformedClass.fields) {
            if (f.name.equals(transformerField.name)) {
                if (!f.desc.equals(transformerField.desc)) {
                    throw new RuntimeException("Field " + f.name + " is not the same type as the field in the transformer class.");
                }
                return;
            }
        }

        // remove the annotation from the field
        transformerField.invisibleAnnotations.removeIf(a -> a.desc.equals("L" + CUnique.class.getCanonicalName().replace(".", "/") + ";"));

        transformedClass.fields.add(transformerField);
    }

}
