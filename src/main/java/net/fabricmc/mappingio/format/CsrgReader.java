package net.fabricmc.mappingio.format;

import net.fabricmc.mappingio.MappedElementKind;
import net.fabricmc.mappingio.MappingFlag;
import net.fabricmc.mappingio.MappingUtil;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

public class CsrgReader {

    public static void readClasses(Reader reader, MemoryMappingTree visitor) throws IOException {
        readClasses(reader, MappingUtil.NS_SOURCE_FALLBACK, MappingUtil.NS_TARGET_FALLBACK, visitor);
    }

    public static void readClasses(Reader reader, String sourceNamespace, String targetNamespace, MemoryMappingTree visitor) throws IOException {
        readClasses(new ColumnFileReader(reader, ' '), sourceNamespace, targetNamespace, visitor);
    }

    private static void readClasses(ColumnFileReader reader, String sourceNamespace, String targetNamespace, MemoryMappingTree visitor) throws IOException {

        MappingTree tree = visitor;

        var flags = visitor.getFlags();
        MappingVisitor parentVisitor = null;

        if (flags.contains(MappingFlag.NEEDS_UNIQUENESS)) {
            parentVisitor = visitor;
            visitor = new MemoryMappingTree();
        } else if (flags.contains(MappingFlag.NEEDS_MULTIPLE_PASSES)) {
            reader.mark();
        }

        while (true) {
            var visitHeader = visitor.visitHeader();

            if (visitHeader) {
                visitor.visitNamespaces(sourceNamespace, List.of(targetNamespace));
            }

            if (visitor.visitContent()) {

                do {
                    var srcName = reader.nextCol();
                    if (srcName.contains("#")) {
                        continue;
                    }
                    var dstName = reader.nextCol();
                    if (dstName == null) {
                        System.out.println("Invalid line: " + srcName);
                    }
                    if (dstName.contains("#")) {
                        dstName = dstName.substring(0, dstName.indexOf('#'));
                    }
                    if (srcName.isEmpty() || dstName.isEmpty()) {
                        continue;
                    }

                    if (visitor.visitClass(srcName)) {
                        visitor.visitDstName(MappedElementKind.CLASS, 0, dstName);
                    }


                } while (reader.nextLine(0));

                int realLocation = tree.getNamespaceId(targetNamespace);

                // resolve missing inner classes
                for (MappingTree.ClassMapping classDef : tree.getClasses()) {
                    if (classDef.getSrcName().contains("$") && classDef.getDstName(realLocation) == null) {
                        String parentName = classDef.getSrcName().substring(0, classDef.getSrcName().indexOf('$'));
                        MappingTree.ClassMapping parent = tree.getClass(parentName);
                        if (parent != null && parent.getDstName(realLocation) != null) {
                            if (visitor.visitClass(classDef.getSrcName())) {
                                visitor.visitDstName(MappedElementKind.CLASS, 0, parent.getDstName(realLocation) + classDef.getSrcName().substring(parentName.length()));
                            }
                        }
                    }
                }
            }

            if (visitor.visitEnd()) break;

            reader.reset();
        }

        if (parentVisitor != null) {
            ((MappingTree) visitor).accept(parentVisitor);
        }
    }

}
