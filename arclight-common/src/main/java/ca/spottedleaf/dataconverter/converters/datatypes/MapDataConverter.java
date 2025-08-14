package ca.spottedleaf.dataconverter.converters.datatypes;

import ca.spottedleaf.dataconverter.converters.DataConverter;
import ca.spottedleaf.dataconverter.types.MapType;

public abstract class MapDataConverter extends DataConverter<MapType<String>, MapType<String>> {

    public MapDataConverter(final int toVersion) {
        super(toVersion);
    }

    public MapDataConverter(final int toVersion, final int versionStep) {
        super(toVersion, versionStep);
    }

    @Override
    public final MapType<String> convert(final MapType<String> data, final long sourceVersion, final long toVersion) {
        return this.convertMap(data, sourceVersion, toVersion);
    }

    public abstract MapType<String> convertMap(final MapType<String> data, final long sourceVersion, final long toVersion);

}
