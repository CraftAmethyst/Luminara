package ca.spottedleaf.dataconverter.types.json;

import ca.spottedleaf.dataconverter.types.ListType;
import ca.spottedleaf.dataconverter.types.MapType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Map;
import java.util.Set;

public final class JsonMapType implements MapType<String> {

    private final JsonObject json;
    private final boolean compressed;

    public JsonMapType() {
        this.json = new JsonObject();
        this.compressed = false;
    }

    public JsonMapType(final JsonObject json) {
        this.json = json;
        this.compressed = false;
    }

    public JsonMapType(final JsonObject json, final boolean compressed) {
        this.json = json;
        this.compressed = compressed;
    }

    public JsonObject getJson() {
        return this.json;
    }

    public boolean isCompressed() {
        return this.compressed;
    }

    @Override
    public int size() {
        return this.json.size();
    }

    @Override
    public boolean isEmpty() {
        return this.json.size() == 0;
    }

    @Override
    public boolean hasKey(final String key) {
        return this.json.has(key);
    }

    @Override
    public boolean hasKey(final String key, final int type) {
        final JsonElement element = this.json.get(key);
        if (element == null) {
            return false;
        }
        // JSON doesn't have strict typing like NBT, so we approximate
        return true;
    }

    @Override
    public Set<String> getKeys() {
        return this.json.keySet();
    }

    @Override
    public void remove(final String key) {
        this.json.remove(key);
    }

    @Override
    public Object getGeneric(final String key) {
        return this.json.get(key);
    }

    @Override
    public void setGeneric(final String key, final Object value) {
        if (value instanceof JsonElement) {
            this.json.add(key, (JsonElement) value);
        }
    }

    @Override
    public Number getNumber(final String key) {
        final JsonElement element = this.json.get(key);
        if (element != null && element.isJsonPrimitive()) {
            final JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isNumber()) {
                return primitive.getAsNumber();
            }
        }
        return null;
    }

    @Override
    public void setInt(final String key, final int value) {
        this.json.addProperty(key, value);
    }

    @Override
    public int getInt(final String key) {
        final JsonElement element = this.json.get(key);
        return element != null ? element.getAsInt() : 0;
    }

    @Override
    public int getInt(final String key, final int dfl) {
        final JsonElement element = this.json.get(key);
        return element != null ? element.getAsInt() : dfl;
    }

    @Override
    public void setLong(final String key, final long value) {
        this.json.addProperty(key, value);
    }

    @Override
    public long getLong(final String key) {
        final JsonElement element = this.json.get(key);
        return element != null ? element.getAsLong() : 0L;
    }

    @Override
    public long getLong(final String key, final long dfl) {
        final JsonElement element = this.json.get(key);
        return element != null ? element.getAsLong() : dfl;
    }

    @Override
    public void setShort(final String key, final short value) {
        this.json.addProperty(key, value);
    }

    @Override
    public short getShort(final String key) {
        final JsonElement element = this.json.get(key);
        return element != null ? element.getAsShort() : 0;
    }

    @Override
    public short getShort(final String key, final short dfl) {
        final JsonElement element = this.json.get(key);
        return element != null ? element.getAsShort() : dfl;
    }

    @Override
    public void setByte(final String key, final byte value) {
        this.json.addProperty(key, value);
    }

    @Override
    public byte getByte(final String key) {
        final JsonElement element = this.json.get(key);
        return element != null ? element.getAsByte() : 0;
    }

    @Override
    public byte getByte(final String key, final byte dfl) {
        final JsonElement element = this.json.get(key);
        return element != null ? element.getAsByte() : dfl;
    }

    @Override
    public void setFloat(final String key, final float value) {
        this.json.addProperty(key, value);
    }

    @Override
    public float getFloat(final String key) {
        final JsonElement element = this.json.get(key);
        return element != null ? element.getAsFloat() : 0.0f;
    }

    @Override
    public float getFloat(final String key, final float dfl) {
        final JsonElement element = this.json.get(key);
        return element != null ? element.getAsFloat() : dfl;
    }

    @Override
    public void setDouble(final String key, final double value) {
        this.json.addProperty(key, value);
    }

    @Override
    public double getDouble(final String key) {
        final JsonElement element = this.json.get(key);
        return element != null ? element.getAsDouble() : 0.0;
    }

    @Override
    public double getDouble(final String key, final double dfl) {
        final JsonElement element = this.json.get(key);
        return element != null ? element.getAsDouble() : dfl;
    }

    @Override
    public void setByteArray(final String key, final byte[] value) {
        final JsonArray array = new JsonArray();
        for (final byte b : value) {
            array.add(b);
        }
        this.json.add(key, array);
    }

    @Override
    public byte[] getByteArray(final String key) {
        final JsonElement element = this.json.get(key);
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
    public byte[] getByteArray(final String key, final byte[] dfl) {
        final byte[] ret = this.getByteArray(key);
        return ret != null ? ret : dfl;
    }

    @Override
    public void setIntArray(final String key, final int[] value) {
        final JsonArray array = new JsonArray();
        for (final int i : value) {
            array.add(i);
        }
        this.json.add(key, array);
    }

    @Override
    public int[] getIntArray(final String key) {
        final JsonElement element = this.json.get(key);
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
    public int[] getIntArray(final String key, final int[] dfl) {
        final int[] ret = this.getIntArray(key);
        return ret != null ? ret : dfl;
    }

    @Override
    public void setLongArray(final String key, final long[] value) {
        final JsonArray array = new JsonArray();
        for (final long l : value) {
            array.add(l);
        }
        this.json.add(key, array);
    }

    @Override
    public long[] getLongArray(final String key) {
        final JsonElement element = this.json.get(key);
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
    public long[] getLongArray(final String key, final long[] dfl) {
        final long[] ret = this.getLongArray(key);
        return ret != null ? ret : dfl;
    }

    @Override
    public void setString(final String key, final String value) {
        this.json.addProperty(key, value);
    }

    @Override
    public String getString(final String key) {
        final JsonElement element = this.json.get(key);
        return element != null ? element.getAsString() : null;
    }

    @Override
    public String getString(final String key, final String dfl) {
        final JsonElement element = this.json.get(key);
        return element != null ? element.getAsString() : dfl;
    }

    @Override
    public MapType<String> getMap(final String key) {
        final JsonElement element = this.json.get(key);
        if (element != null && element.isJsonObject()) {
            return new JsonMapType(element.getAsJsonObject(), this.compressed);
        }
        return null;
    }

    @Override
    public void setMap(final String key, final MapType<String> value) {
        if (value instanceof JsonMapType) {
            this.json.add(key, ((JsonMapType) value).json);
        }
    }

    @Override
    public MapType<String> getOrCreateMap(final String key) {
        JsonElement element = this.json.get(key);
        if (element != null && element.isJsonObject()) {
            return new JsonMapType(element.getAsJsonObject(), this.compressed);
        }

        final JsonMapType ret = new JsonMapType(new JsonObject(), this.compressed);
        this.json.add(key, ret.json);
        return ret;
    }

    @Override
    public ListType getList(final String key, final int type) {
        final JsonElement element = this.json.get(key);
        if (element != null && element.isJsonArray()) {
            return new JsonListType(element.getAsJsonArray(), this.compressed);
        }
        return null;
    }

    @Override
    public void setList(final String key, final ListType value) {
        if (value instanceof JsonListType) {
            this.json.add(key, ((JsonListType) value).getJson());
        }
    }

    @Override
    public ListType getOrCreateList(final String key, final int type) {
        JsonElement element = this.json.get(key);
        if (element != null && element.isJsonArray()) {
            return new JsonListType(element.getAsJsonArray(), this.compressed);
        }

        final JsonListType ret = new JsonListType(new JsonArray(), this.compressed);
        this.json.add(key, ret.getJson());
        return ret;
    }

    @Override
    public MapType<String> copy() {
        return new JsonMapType(this.json.deepCopy(), this.compressed);
    }

    @Override
    public boolean getBoolean(final String key) {
        final JsonElement element = this.json.get(key);
        return element != null ? element.getAsBoolean() : false;
    }

    @Override
    public boolean getBoolean(final String key, final boolean dfl) {
        final JsonElement element = this.json.get(key);
        return element != null ? element.getAsBoolean() : dfl;
    }

    @Override
    public void setBoolean(final String key, final boolean value) {
        this.json.addProperty(key, value);
    }

    @Override
    public Map<String, Object> getMap() {
        // Not directly supported, would need conversion
        throw new UnsupportedOperationException("JSON does not support direct map conversion");
    }
}
