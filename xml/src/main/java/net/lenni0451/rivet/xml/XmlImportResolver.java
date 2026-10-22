package net.lenni0451.rivet.xml;

import javax.annotation.WillClose;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

@FunctionalInterface
public interface XmlImportResolver {

    static XmlImportResolver filesystem() {
        return filesystem(null);
    }

    static XmlImportResolver filesystem(final Path root) {
        return (source, includingSource) -> {
            if (source.startsWith(XmlConstants.CLASSPATH_PREFIX)) {
                return null;
            }
            Path base;
            if (includingSource != null && includingSource.startsWith(XmlConstants.FILE_PREFIX)) {
                base = Path.of(includingSource.substring(XmlConstants.FILE_PREFIX.length())).getParent();
            } else {
                base = root == null ? Path.of("") : root;
            }

            Path resolved;
            try {
                resolved = base.resolve(source.startsWith(XmlConstants.FILE_PREFIX) ? source.substring(XmlConstants.FILE_PREFIX.length()) : source).toRealPath();
            } catch (NoSuchFileException e) {
                return null;
            }
            if (root != null && !resolved.startsWith(root.toRealPath())) {
                throw new IOException("Import escapes configured root: " + source);
            }
            return new ResolvedImport(XmlConstants.FILE_PREFIX + resolved, Files.newInputStream(resolved));
        };
    }

    static XmlImportResolver classpath() {
        return classpath(XmlImportResolver.class.getClassLoader(), "");
    }

    static XmlImportResolver classpath(final ClassLoader classLoader, final String root) {
        String normalizedRoot = normalizeResourcePath(root);
        return (source, includingSource) -> {
            String base = normalizedRoot;
            String relative = source;
            if (source.startsWith(XmlConstants.CLASSPATH_PREFIX)) {
                relative = source.substring(XmlConstants.CLASSPATH_PREFIX.length());
            } else if (includingSource != null && includingSource.startsWith(XmlConstants.CLASSPATH_PREFIX)) {
                String includingPath = includingSource.substring(XmlConstants.CLASSPATH_PREFIX.length());
                base = includingPath.substring(0, Math.max(0, includingPath.lastIndexOf('/')));
            }

            String path = normalizeResourcePath(base + "/" + relative);
            if (!normalizedRoot.isEmpty() && !path.startsWith(normalizedRoot + "/")) {
                throw new IOException("Import escapes configured classpath root: " + source);
            }
            InputStream stream = classLoader.getResourceAsStream(path);
            return stream == null ? null : new ResolvedImport(XmlConstants.CLASSPATH_PREFIX + path, stream);
        };
    }

    private static String normalizeResourcePath(final String path) {
        String normalized = Path.of("/", path).normalize().toString().replace('\\', '/');
        return normalized.substring(1);
    }


    ResolvedImport resolve(final String source, final String includingSource) throws IOException;


    record ResolvedImport(String source, @WillClose InputStream stream) implements AutoCloseable {
        @Override
        public void close() throws IOException {
            this.stream.close();
        }
    }

}
