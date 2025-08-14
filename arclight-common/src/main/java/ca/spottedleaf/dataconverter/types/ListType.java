package ca.spottedleaf.dataconverter.types;

import java.util.List;

public interface ListType {

    int size();

    void remove(final int index);

    Number getNumber(final int index);

    int getInt(final int index);

    long getLong(final int index);

    short getShort(final int index);

    byte getByte(final int index);

    float getFloat(final int index);

    double getDouble(final int index);

    byte[] getByteArray(final int index);

    int[] getIntArray(final int index);

    long[] getLongArray(final int index);

    String getString(final int index);

    MapType<String> getMap(final int index);

    ListType getList(final int index);

    void addByte(final byte value);

    void addShort(final short value);

    void addInt(final int value);

    void addLong(final long value);

    void addFloat(final float value);

    void addDouble(final double value);

    void addByteArray(final byte[] value);

    void addIntArray(final int[] value);

    void addLongArray(final long[] value);

    void addString(final String value);

    void addMap(final MapType<String> value);

    void addList(final ListType value);

    void setByte(final int index, final byte value);

    void setShort(final int index, final short value);

    void setInt(final int index, final int value);

    void setLong(final int index, final long value);

    void setFloat(final int index, final float value);

    void setDouble(final int index, final double value);

    void setByteArray(final int index, final byte[] value);

    void setIntArray(final int index, final int[] value);

    void setLongArray(final int index, final long[] value);

    void setString(final int index, final String value);

    void setMap(final int index, final MapType<String> value);

    void setList(final int index, final ListType value);

    Object getGeneric(final int index);

    void addGeneric(final Object value);

    void setGeneric(final int index, final Object value);

    ListType copy();

    List<Object> getList();
}
