package ca.spottedleaf.dataconverter.types;

import java.util.Map;
import java.util.Set;

public interface MapType<K> {

    int size();

    boolean isEmpty();

    boolean hasKey(final K key);

    boolean hasKey(final K key, final int type);

    Set<K> getKeys();

    void remove(final K key);

    // generic

    Object getGeneric(final K key);

    void setGeneric(final K key, final Object value);

    // int

    Number getNumber(final K key);

    void setInt(final K key, final int value);

    int getInt(final K key);

    int getInt(final K key, final int dfl);

    // long

    void setLong(final K key, final long value);

    long getLong(final K key);

    long getLong(final K key, final long dfl);

    // short

    void setShort(final K key, final short value);

    short getShort(final K key);

    short getShort(final K key, final short dfl);

    // byte

    void setByte(final K key, final byte value);

    byte getByte(final K key);

    byte getByte(final K key, final byte dfl);

    // float

    void setFloat(final K key, final float value);

    float getFloat(final K key);

    float getFloat(final K key, final float dfl);

    // double

    void setDouble(final K key, final double value);

    double getDouble(final K key);

    double getDouble(final K key, final double dfl);

    // byte[]

    void setByteArray(final K key, final byte[] value);

    byte[] getByteArray(final K key);

    byte[] getByteArray(final K key, final byte[] dfl);

    // int[]

    void setIntArray(final K key, final int[] value);

    int[] getIntArray(final K key);

    int[] getIntArray(final K key, final int[] dfl);

    // long[]

    void setLongArray(final K key, final long[] value);

    long[] getLongArray(final K key);

    long[] getLongArray(final K key, final long[] dfl);

    // String

    void setString(final K key, final String value);

    String getString(final K key);

    String getString(final K key, final String dfl);

    // Map

    MapType<K> getMap(final K key);

    void setMap(final K key, final MapType<K> value);

    MapType<K> getOrCreateMap(final K key);

    // List

    ListType getList(final K key, final int type);

    void setList(final K key, final ListType value);

    ListType getOrCreateList(final K key, final int type);

    // copy

    MapType<K> copy();

    // misc

    boolean getBoolean(final K key);

    boolean getBoolean(final K key, final boolean dfl);

    void setBoolean(final K key, final boolean value);

    Map<K, Object> getMap();
}
