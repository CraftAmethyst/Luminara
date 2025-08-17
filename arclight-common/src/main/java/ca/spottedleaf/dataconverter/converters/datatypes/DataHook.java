package ca.spottedleaf.dataconverter.converters.datatypes;

public interface DataHook<T, R> {

    R preHook(final T data, final long fromVersion, final long toVersion);

    R postHook(final T data, final long fromVersion, final long toVersion);

}
