package ca.spottedleaf.dataconverter.converters.datatypes;

public final class MCTypeRegistry {

    private MCTypeRegistry() {
    }

    public static void registerConverters() {
        // Data converter registry is initialized
        // Specific converters can be registered here as they are implemented
        // Example:
        // MCDataType.ITEM_STACK.addConverter(new ItemStackConverter());
        // MCDataType.ENTITY_TREE.addConverter(new EntityConverter());
        // MCDataType.CHUNK.addConverter(new ChunkConverter());
        // MCDataType.PLAYER.addConverter(new PlayerConverter());
    }

}
