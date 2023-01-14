package xyz.wagyourtail.fukkit2.mixinpatcher;

import net.lenni0451.classtransform.TransformerManager;
import net.lenni0451.classtransform.targets.IInjectionTarget;
import net.lenni0451.classtransform.transformer.types.RemovingTargetAnnotationHandler;
import net.lenni0451.classtransform.utils.tree.IClassProvider;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import xyz.wagyourtail.fukkit2.mixinpatcher.annotations.CReplaceArgs;

import java.util.Map;

public class CArgsReplacer  extends RemovingTargetAnnotationHandler<CReplaceArgs> {
    public CArgsReplacer() {
        super(CReplaceArgs.class, CReplaceArgs::value);
    }

    @Override
    public void transform(CReplaceArgs annotation, TransformerManager transformerManager, IClassProvider classProvider, Map<String, IInjectionTarget> injectionTargets, ClassNode transformedClass, ClassNode transformer, MethodNode transformerMethod, MethodNode target) {
        target.desc = transformerMethod.desc;
    }

}
