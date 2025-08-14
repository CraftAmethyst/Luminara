package ca.spottedleaf.dataconverter.minecraft.datatypes;

import ca.spottedleaf.dataconverter.converters.DataConverter;
import ca.spottedleaf.dataconverter.minecraft.MCVersionRegistry;
import ca.spottedleaf.dataconverter.types.MapType;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum MCDataType {

    LEVEL("level"),
    PLAYER("player"),
    CHUNK("chunk"),
    HOTBAR("hotbar"),
    OPTIONS("options"),
    STRUCTURE("structure"),
    STATS("stats"),
    SAVED_DATA("saved_data"),
    ADVANCEMENTS("advancements"),
    POI_CHUNK("poi_chunk"),
    ENTITY_CHUNK("entity_chunk"),
    BLOCK_ENTITY("block_entity"),
    ITEM_STACK("item_stack"),
    BLOCK_STATE("block_state"),
    ENTITY_NAME("entity_name"),
    ENTITY_TREE("entity"),
    GAME_EVENT_NAME("game_event_name"),
    BIOME("biome"),
    WORLD_GEN_SETTINGS("world_gen_settings"),
    ;

    private final String typeName;
    private final Long2ObjectLinkedOpenHashMap<List<DataConverter<MapType<String>, MapType<String>>>> converters = new Long2ObjectLinkedOpenHashMap<>();
    private final LongLinkedOpenHashSet versions = new LongLinkedOpenHashSet();
    private final LongArrayList versionsSorted = new LongArrayList();

    MCDataType(final String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return this.typeName;
    }

    public void addConverter(final DataConverter<MapType<String>, MapType<String>> converter) {
        final long version = converter.getEncodedVersion();
        MCVersionRegistry.checkVersion(version);
        this.converters.computeIfAbsent(version, (final long keyInMap) -> {
            return new ArrayList<>();
        }).add(converter);
        if (this.versions.add(version)) {
            this.versionsSorted.add(version);
            this.versionsSorted.sort(null);
        }
    }

    public void addConverter(final int version, final DataConverter<MapType<String>, MapType<String>> converter) {
        this.addConverter(DataConverter.encodeVersions(version, 0), converter);
    }

    public void addConverter(final long version, final DataConverter<MapType<String>, MapType<String>> converter) {
        MCVersionRegistry.checkVersion(version);
        this.converters.computeIfAbsent(version, (final long keyInMap) -> {
            return new ArrayList<>();
        }).add(converter);
        if (this.versions.add(version)) {
            this.versionsSorted.add(version);
            this.versionsSorted.sort(null);
        }
    }

    public MapType<String> convert(final MapType<String> data, final long fromVersion, final long toVersion) {
        if (fromVersion >= toVersion) {
            return null;
        }

        MapType<String> ret = data;

        for (int i = 0, len = this.versionsSorted.size(); i < len; ++i) {
            final long version = this.versionsSorted.getLong(i);
            if (version <= fromVersion) {
                continue;
            }
            if (version > toVersion) {
                break;
            }

            final List<DataConverter<MapType<String>, MapType<String>>> converters = this.converters.get(version);
            for (int j = 0, jlen = converters.size(); j < jlen; ++j) {
                final DataConverter<MapType<String>, MapType<String>> converter = converters.get(j);
                final MapType<String> replace = converter.convert(ret, fromVersion, toVersion);
                if (replace != null) {
                    ret = replace;
                }
            }
        }

        return ret == data ? null : ret;
    }

    public List<DataConverter<MapType<String>, MapType<String>>> getConverters(final long version) {
        return this.converters.getOrDefault(version, Collections.emptyList());
    }

    public LongLinkedOpenHashSet getVersions() {
        return this.versions;
    }

    public LongArrayList getVersionsSorted() {
        return this.versionsSorted;
    }
}
