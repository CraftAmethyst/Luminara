package ca.spottedleaf.dataconverter.types.json;

import ca.spottedleaf.dataconverter.types.ListType;
import ca.spottedleaf.dataconverter.types.MapType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.List;

public record JsonListType(JsonArray json, boolean compressed) implements ListType {

    public JsonListType() {
        this(new JsonArray(), false);
    }

    public JsonListType(final JsonArray json) {
        this(json, false);
    }

    @Override
    public int size() {
        return this.json.size();
    }

    @Override
    public void remove(final int index) {
        this.json.remove(index);
    }

    @Override
    public Number getNumber(final int index) {
        final JsonElement element = this.json.get(index);
        return element != null && element.isJsonPrimitive() ? element.getAsNumber() : null;
    }

    @Override
    public int getInt(final int index) {
        final JsonElement element = this.json.get(index);
        return element != null ? element.getAsInt() : 0;
    }

    @Override
    public long getLong(final int index) {
        final JsonElement element = this.json.get(index);
        return element != null ? element.getAsLong() : 0L;
    }

    @Override
    public short getShort(final int index) {
        final JsonElement element = this.json.get(index);
        return element != null ? element.getAsShort() : 0;
    }

    @Override
    public byte getByte(final int index) {
        final JsonElement element = this.json.get(index);
        return element != null ? element.getAsByte() : 0;
    }

    @Override
    public float getFloat(final int index) {
        final JsonElement element = this.json.get(index);
        return element != null ? element.getAsFloat() : 0.0f;
    }

    @Override
    public double getDouble(final int index) {
        final JsonElement element = this.json.get(index);
        return element != null ? element.getAsDouble() : 0.0;
    }

    @Override
    public byte[] getByteArray(final int index) {
        final JsonElement element = this.json.get(index);
        if (element != null && element.isJsonArray()) {
            final JsonArray array = element.getAsJsonArray();
            final byte[] ret = new byte[array.size()];
            for (int i = 0; i < ret.length; ++i) {
                ret[i] = array.get(i).getAsByte();
            }
            return ret;
        }
        return null;
    }

    @Override
    public int[] getIntArray(final int index) {
        final JsonElement element = this.json.get(index);
        if (element != null && element.isJsonArray()) {
            final JsonArray array = element.getAsJsonArray();
            final int[] ret = new int[array.size()];
            for (int i = 0; i < ret.length; ++i) {
                ret[i] = array.get(i).getAsInt();
            }
            return ret;
        }
        return null;
    }

    @Override
    public long[] getLongArray(final int index) {
        final JsonElement element = this.json.get(index);
        if (element != null && element.isJsonArray()) {
            final JsonArray array = element.getAsJsonArray();
            final long[] ret = new long[array.size()];
            for (int i = 0; i < ret.length; ++i) {
                ret[i] = array.get(i).getAsLong();
            }
            return ret;
        }
        return null;
    }

    @Override
    public String getString(final int index) {
        final JsonElement element = this.json.get(index);
        return element != null ? element.getAsString() : null;
    }

    @Override
    public MapType<String> getMap(final int index) {
        final JsonElement element = this.json.get(index);
        if (element != null && element.isJsonObject()) {
            return new JsonMapType(element.getAsJsonObject(), this.compressed);
        }
        return null;
    }

    @Override
    public ListType getList(final int index) {
        final JsonElement element = this.json.get(index);
        if (element != null && element.isJsonArray()) {
            return new JsonListType(element.getAsJsonArray(), this.compressed);
        }
        return null;
    }

    @Override
    public void addByte(final byte value) {
        this.json.add(value);
    }

    @Override
    public void addShort(final short value) {
        this.json.add(value);
    }

    @Override
    public void addInt(final int value) {
        this.json.add(value);
    }

    @Override
    public void addLong(final long value) {
        this.json.add(value);
    }

    @Override
    public void addFloat(final float value) {
        this.json.add(value);
    }

    @Override
    public void addDouble(final double value) {
        this.json.add(value);
    }

    @Override
    public void addByteArray(final byte[] value) {
        final JsonArray array = new JsonArray();
        for (final byte b : value) {
            array.add(b);
        }
        this.json.add(array);
    }

    @Override
    public void addIntArray(final int[] value) {
        final JsonArray array = new JsonArray();
        for (final int i : value) {
            array.add(i);
        }
        this.json.add(array);
    }

    @Override
    public void addLongArray(final long[] value) {
        final JsonArray array = new JsonArray();
        for (final long l : value) {
            array.add(l);
        }
        this.json.add(array);
    }

    @Override
    public void addString(final String value) {
        this.json.add(value);
    }

    @Override
    public void addMap(final MapType<String> value) {
        if (value instanceof JsonMapType) {
            this.json.add(((JsonMapType) value).json());
        }
    }

    @Override
    public void addList(final ListType value) {
        if (value instanceof JsonListType) {
            this.json.add(((JsonListType) value).json());
        }
    }

    @Override
    public void setByte(final int index, final byte value) {
        this.json.set(index, new JsonPrimitive(value));
    }

    @Override
    public void setShort(final int index, final short value) {
        this.json.set(index, new JsonPrimitive(value));
    }

    @Override
    public void setInt(final int index, final int value) {
        this.json.set(index, new JsonPrimitive(value));
    }

    @Override
    public void setLong(final int index, final long value) {
        this.json.set(index, new JsonPrimitive(value));
    }

    @Override
    public void setFloat(final int index, final float value) {
        this.json.set(index, new JsonPrimitive(value));
    }

    @Override
    public void setDouble(final int index, final double value) {
        this.json.set(index, new JsonPrimitive(value));
    }

    @Override
    public void setByteArray(final int index, final byte[] value) {
        final JsonArray array = new JsonArray();
        for (final byte b : value) {
            array.add(b);
        }
        this.json.set(index, array);
    }

    @Override
    public void setIntArray(final int index, final int[] value) {
        final JsonArray array = new JsonArray();
        for (final int i : value) {
            array.add(i);
        }
        this.json.set(index, array);
    }

    @Override
    public void setLongArray(final int index, final long[] value) {
        final JsonArray array = new JsonArray();
        for (final long l : value) {
            array.add(l);
        }
        this.json.set(index, array);
    }

    @Override
    public void setString(final int index, final String value) {
        this.json.set(index, new JsonPrimitive(value));
    }

    @Override
    public void setMap(final int index, final MapType<String> value) {
        if (value instanceof JsonMapType) {
            this.json.set(index, ((JsonMapType) value).json());
        }
    }

    @Override
    public void setList(final int index, final ListType value) {
        if (value instanceof JsonListType) {
            this.json.set(index, ((JsonListType) value).json());
        }
    }

    @Override
    public Object getGeneric(final int index) {
        return this.json.get(index);
    }

    @Override
    public void addGeneric(final Object value) {
        if (value instanceof JsonElement) {
            this.json.add((JsonElement) value);
        }
    }

    @Override
    public void setGeneric(final int index, final Object value) {
        if (value instanceof JsonElement) {
            this.json.set(index, (JsonElement) value);
        }
    }

    @Override
    public ListType copy() {
        return new JsonListType(this.json.deepCopy(), this.compressed);
    }

    @Override
    public List<Object> getList() {
        // Not directly supported, would need conversion
        throw new UnsupportedOperationException("JSON does not support direct list conversion");
    }
}
