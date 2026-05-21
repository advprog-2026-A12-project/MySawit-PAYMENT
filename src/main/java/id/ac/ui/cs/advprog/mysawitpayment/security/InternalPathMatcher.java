package id.ac.ui.cs.advprog.mysawitpayment.security;

public class InternalPathMatcher {

    private final String internalPathPrefix;

    public InternalPathMatcher(String internalPathPrefix) {
        this.internalPathPrefix = normalizePathPrefix(internalPathPrefix);
    }

    public boolean matches(String path) {
        return path != null && path.startsWith(internalPathPrefix);
    }

    private String normalizePathPrefix(String pathPrefix) {
        String normalized = pathPrefix.startsWith("/") ? pathPrefix : "/" + pathPrefix;
        return normalized.endsWith("/") ? normalized : normalized + "/";
    }
}
