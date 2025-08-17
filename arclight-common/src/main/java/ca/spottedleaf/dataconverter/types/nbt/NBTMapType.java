package ca.spottedleaf.dataconverter.types.nbt;

import ca.spottedleaf.dataconverter.types.ListType;
import ca.spottedleaf.dataconverter.types.MapType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;

import java.util.Map;
import java.util.Set;

public record NBTMapType(CompoundTag tag) implements MapType<String> {

    public NBTMapType() {
        this(new CompoundTag());
    }

    @Override
    public int size() {
        return this.tag.size();
    }

    @Override
    public boolean isEmpty() {
        return this.tag.isEmpty();
    }

    @Override
    public boolean hasKey(final String key) {
        return this.tag.contains(key);
    }

    @Override
    public boolean hasKey(final String key, final int type) {
        return this.tag.contains(key, type);
    }

    @Override
    public Set<String> getKeys() {
        return this.tag.getAllKeys();
    }

    @Override
    public void remove(final String key) {
        this.tag.remove(key);
    }

    @Override
    public Object getGeneric(final String key) {
        return this.tag.get(key);
    }

    @Override
    public void setGeneric(final String key, final Object value) {
        if (value instanceof Tag) {
            this.tag.put(key, (Tag) value);
        }
    }

    @Override
    public Number getNumber(final String key) {
        final Tag tag = this.tag.get(key);
        if (tag instanceof NumericTag) {
            return ((NumericTag) tag).getAsNumber();
        }
        return null;
    }

    @Override
    public void setInt(final String key, final int value) {
        this.tag.putInt(key, value);
    }

    @Override
    public int getInt(final String key) {
        return this.tag.getInt(key);
    }

    @Override
    public int getInt(final String key, final int dfl) {
        return this.hasKey(key) ? this.getInt(key) : dfl;
    }

    @Override
    public void setLong(final String key, final long value) {
        this.tag.putLong(key, value);
    }

    @Override
    public long getLong(final String key) {
        return this.tag.getLong(key);
    }

    @Override
    public long getLong(final String key, final long dfl) {
        return this.hasKey(key) ? this.getLong(key) : dfl;
    }

    @Override
    public void setShort(final String key, final short value) {
        this.tag.putShort(key, value);
    }

    @Override
    public short getShort(final String key) {
        return this.tag.getShort(key);
    }

    @Override
    public short getShort(final String key, final short dfl) {
        return this.hasKey(key) ? this.getShort(key) : dfl;
    }

    @Override
    public void setByte(final String key, final byte value) {
        this.tag.putByte(key, value);
    }

    @Override
    public byte getByte(final String key) {
        return this.tag.getByte(key);
    }

    @Override
    public byte getByte(final String key, final byte dfl) {
        return this.hasKey(key) ? this.getByte(key) : dfl;
    }

    @Override
    public void setFloat(final String key, final float value) {
        this.tag.putFloat(key, value);
    }

    @Override
    public float getFloat(final String key) {
        return this.tag.getFloat(key);
    }

    @Override
    public float getFloat(final String key, final float dfl) {
        return this.hasKey(key) ? this.getFloat(key) : dfl;
    }

    @Override
    public void setDouble(final String key, final double value) {
        this.tag.putDouble(key, value);
    }

    @Override
    public double getDouble(final String key) {
        return this.tag.getDouble(key);
    }

    @Override
    public double getDouble(final String key, final double dfl) {
        return this.hasKey(key) ? this.getDouble(key) : dfl;
    }

    @Override
    public void setByteArray(final String key, final byte[] value) {
        this.tag.putByteArray(key, value);
    }

    @Override
    public byte[] getByteArray(final String key) {
        return this.tag.getByteArray(key);
    }

    @Override
    public byte[] getByteArray(final String key, final byte[] dfl) {
        return this.hasKey(key) ? this.getByteArray(key) : dfl;
    }

    @Override
    public void setIntArray(final String key, final int[] value) {
        this.tag.putIntArray(key, value);
    }

    @Override
    public int[] getIntArray(final String key) {
        return this.tag.getIntArray(key);
    }

    @Override
    public int[] getIntArray(final String key, final int[] dfl) {
        return this.hasKey(key) ? this.getIntArray(key) : dfl;
    }

    @Override
    public void setLongArray(final String key, final long[] value) {
        this.tag.putLongArray(key, value);
    }

    @Override
    public long[] getLongArray(final String key) {
        return this.tag.getLongArray(key);
    }

    @Override
    public long[] getLongArray(final String key, final long[] dfl) {
        return this.hasKey(key) ? this.getLongArray(key) : dfl;
    }

    @Override
    public void setString(final String key, final String value) {
        this.tag.putString(key, value);
    }

    @Override
    public String getString(final String key) {
        return this.tag.getString(key);
    }

    @Override
    public String getString(final String key, final String dfl) {
        return this.hasKey(key) ? this.getString(key) : dfl;
    }

    @Override
    public MapType<String> getMap(final String key) {
        final Tag tag = this.tag.get(key);
        if (tag instanceof CompoundTag) {
            return new NBTMapType((CompoundTag) tag);
        }
        return null;
    }

    @Override
    public void setMap(final String key, final MapType<String> value) {
        if (value instanceof NBTMapType) {
            this.tag.put(key, ((NBTMapType) value).tag);
        }
    }

    @Override
    public MapType<String> getOrCreateMap(final String key) {
        final Tag tag = this.tag.get(key);
        if (tag instanceof CompoundTag) {
            return new NBTMapType((CompoundTag) tag);
        }

        final NBTMapType ret = new NBTMapType();
        this.tag.put(key, ret.tag);
        return ret;
    }

    @Override
    public ListType getList(final String key, final int type) {
        final Tag tag = this.tag.get(key);
        if (tag instanceof ListTag) {
            return new NBTListType((ListTag) tag);
        }
        return null;
    }

    @Override
    public void setList(final String key, final ListType value) {
        if (value instanceof NBTListType) {
            this.tag.put(key, ((NBTListType) value).tag());
        }
    }

    @Override
    public ListType getOrCreateList(final String key, final int type) {
        final Tag tag = this.tag.get(key);
        if (tag instanceof ListTag) {
            return new NBTListType((ListTag) tag);
        }

        final NBTListType ret = new NBTListType();
        this.tag.put(key, ret.tag());
        return ret;
    }

    @Override
    public MapType<String> copy() {
        return new NBTMapType(this.tag.copy());
    }

    @Override
    public boolean getBoolean(final String key) {
        return this.tag.getBoolean(key);
    }

    @Override
    public boolean getBoolean(final String key, final boolean dfl) {
        return this.hasKey(key) ? this.getBoolean(key) : dfl;
    }

    @Override
    public void setBoolean(final String key, final boolean value) {
        this.tag.putBoolean(key, value);
    }

    @Override
    public Map<String, Object> getMap() {
        // Not directly supported by NBT, would need conversion
        throw new UnsupportedOperationException("NBT does not support direct map conversion");
    }
}
