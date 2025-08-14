package ca.spottedleaf.dataconverter.types.nbt;

import ca.spottedleaf.dataconverter.types.ListType;
import ca.spottedleaf.dataconverter.types.MapType;
import net.minecraft.nbt.*;

import java.util.List;

public final class NBTListType implements ListType {

    private final ListTag tag;

    public NBTListType() {
        this.tag = new ListTag();
    }

    public NBTListType(final ListTag tag) {
        this.tag = tag;
    }

    public ListTag getTag() {
        return this.tag;
    }

    @Override
    public int size() {
        return this.tag.size();
    }

    @Override
    public void remove(final int index) {
        this.tag.remove(index);
    }

    @Override
    public Number getNumber(final int index) {
        final Tag tag = this.tag.get(index);
        if (tag instanceof NumericTag) {
            return ((NumericTag) tag).getAsNumber();
        }
        return null;
    }

    @Override
    public int getInt(final int index) {
        return this.tag.getInt(index);
    }

    @Override
    public long getLong(final int index) {
        final Tag tag = this.tag.get(index);
        if (tag instanceof NumericTag) {
            return ((NumericTag) tag).getAsLong();
        }
        return 0L;
    }

    @Override
    public short getShort(final int index) {
        final Tag tag = this.tag.get(index);
        if (tag instanceof NumericTag) {
            return ((NumericTag) tag).getAsShort();
        }
        return 0;
    }

    @Override
    public byte getByte(final int index) {
        final Tag tag = this.tag.get(index);
        if (tag instanceof NumericTag) {
            return ((NumericTag) tag).getAsByte();
        }
        return 0;
    }

    @Override
    public float getFloat(final int index) {
        return this.tag.getFloat(index);
    }

    @Override
    public double getDouble(final int index) {
        return this.tag.getDouble(index);
    }

    @Override
    public byte[] getByteArray(final int index) {
        final Tag tag = this.tag.get(index);
        if (tag instanceof ByteArrayTag) {
            return ((ByteArrayTag) tag).getAsByteArray();
        }
        return null;
    }

    @Override
    public int[] getIntArray(final int index) {
        final Tag tag = this.tag.get(index);
        if (tag instanceof IntArrayTag) {
            return ((IntArrayTag) tag).getAsIntArray();
        }
        return null;
    }

    @Override
    public long[] getLongArray(final int index) {
        final Tag tag = this.tag.get(index);
        if (tag instanceof LongArrayTag) {
            return ((LongArrayTag) tag).getAsLongArray();
        }
        return null;
    }

    @Override
    public String getString(final int index) {
        return this.tag.getString(index);
    }

    @Override
    public MapType<String> getMap(final int index) {
        final Tag tag = this.tag.get(index);
        if (tag instanceof CompoundTag) {
            return new NBTMapType((CompoundTag) tag);
        }
        return null;
    }

    @Override
    public ListType getList(final int index) {
        final Tag tag = this.tag.get(index);
        if (tag instanceof ListTag) {
            return new NBTListType((ListTag) tag);
        }
        return null;
    }

    @Override
    public void addByte(final byte value) {
        this.tag.add(ByteTag.valueOf(value));
    }

    @Override
    public void addShort(final short value) {
        this.tag.add(ShortTag.valueOf(value));
    }

    @Override
    public void addInt(final int value) {
        this.tag.add(IntTag.valueOf(value));
    }

    @Override
    public void addLong(final long value) {
        this.tag.add(LongTag.valueOf(value));
    }

    @Override
    public void addFloat(final float value) {
        this.tag.add(FloatTag.valueOf(value));
    }

    @Override
    public void addDouble(final double value) {
        this.tag.add(DoubleTag.valueOf(value));
    }

    @Override
    public void addByteArray(final byte[] value) {
        this.tag.add(new ByteArrayTag(value));
    }

    @Override
    public void addIntArray(final int[] value) {
        this.tag.add(new IntArrayTag(value));
    }

    @Override
    public void addLongArray(final long[] value) {
        this.tag.add(new LongArrayTag(value));
    }

    @Override
    public void addString(final String value) {
        this.tag.add(StringTag.valueOf(value));
    }

    @Override
    public void addMap(final MapType<String> value) {
        if (value instanceof NBTMapType) {
            this.tag.add(((NBTMapType) value).getTag());
        }
    }

    @Override
    public void addList(final ListType value) {
        if (value instanceof NBTListType) {
            this.tag.add(((NBTListType) value).getTag());
        }
    }

    @Override
    public void setByte(final int index, final byte value) {
        this.tag.set(index, ByteTag.valueOf(value));
    }

    @Override
    public void setShort(final int index, final short value) {
        this.tag.set(index, ShortTag.valueOf(value));
    }

    @Override
    public void setInt(final int index, final int value) {
        this.tag.set(index, IntTag.valueOf(value));
    }

    @Override
    public void setLong(final int index, final long value) {
        this.tag.set(index, LongTag.valueOf(value));
    }

    @Override
    public void setFloat(final int index, final float value) {
        this.tag.set(index, FloatTag.valueOf(value));
    }

    @Override
    public void setDouble(final int index, final double value) {
        this.tag.set(index, DoubleTag.valueOf(value));
    }

    @Override
    public void setByteArray(final int index, final byte[] value) {
        this.tag.set(index, new ByteArrayTag(value));
    }

    @Override
    public void setIntArray(final int index, final int[] value) {
        this.tag.set(index, new IntArrayTag(value));
    }

    @Override
    public void setLongArray(final int index, final long[] value) {
        this.tag.set(index, new LongArrayTag(value));
    }

    @Override
    public void setString(final int index, final String value) {
        this.tag.set(index, StringTag.valueOf(value));
    }

    @Override
    public void setMap(final int index, final MapType<String> value) {
        if (value instanceof NBTMapType) {
            this.tag.set(index, ((NBTMapType) value).getTag());
        }
    }

    @Override
    public void setList(final int index, final ListType value) {
        if (value instanceof NBTListType) {
            this.tag.set(index, ((NBTListType) value).getTag());
        }
    }

    @Override
    public Object getGeneric(final int index) {
        return this.tag.get(index);
    }

    @Override
    public void addGeneric(final Object value) {
        if (value instanceof Tag) {
            this.tag.add((Tag) value);
        }
    }

    @Override
    public void setGeneric(final int index, final Object value) {
        if (value instanceof Tag) {
            this.tag.set(index, (Tag) value);
        }
    }

    @Override
    public ListType copy() {
        return new NBTListType(this.tag.copy());
    }

    @Override
    public List<Object> getList() {
        // Not directly supported by NBT, would need conversion
        throw new UnsupportedOperationException("NBT does not support direct list conversion");
    }
}
