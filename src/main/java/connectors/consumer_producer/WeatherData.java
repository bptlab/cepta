/**
 * Autogenerated by Avro
 * <p>
 * DO NOT EDIT DIRECTLY
 */
package connectors.consumer_producer;

import org.apache.avro.message.BinaryMessageDecoder;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.SchemaStore;
import org.apache.avro.specific.SpecificData;

@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class WeatherData extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
    public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"TrainData\",\"namespace\":\"connectors.consumer_producer\",\"fields\":[{\"name\":\"id\",\"type\":\"int\"},{\"name\":\"name\",\"type\":\"string\"}]}");
    private static final long serialVersionUID = -1645862618279718008L;
    private static SpecificData MODEL$ = new SpecificData();
    private static final BinaryMessageEncoder<WeatherData> ENCODER =
            new BinaryMessageEncoder<WeatherData>(MODEL$, SCHEMA$);
    private static final BinaryMessageDecoder<WeatherData> DECODER =
            new BinaryMessageDecoder<WeatherData>(MODEL$, SCHEMA$);
    @SuppressWarnings("unchecked")
    private static final org.apache.avro.io.DatumWriter<WeatherData>
            WRITER$ = (org.apache.avro.io.DatumWriter<WeatherData>) MODEL$.createDatumWriter(SCHEMA$);
    @SuppressWarnings("unchecked")
    private static final org.apache.avro.io.DatumReader<WeatherData>
            READER$ = (org.apache.avro.io.DatumReader<WeatherData>) MODEL$.createDatumReader(SCHEMA$);
    @Deprecated
    public int id;
    @Deprecated
    public String name;

    /**
     * Default constructor.  Note that this does not initialize fields
     * to their default values from the schema.  If that is desired then
     * one should use <code>newBuilder()</code>.
     */
    public WeatherData() {
    }

    /**
     * All-args constructor.
     * @param id The new value for id
     * @param name The new value for name
     */
    public WeatherData(java.lang.Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public static org.apache.avro.Schema getClassSchema() {
        return SCHEMA$;
    }

    /**
     * Return the BinaryMessageDecoder instance used by this class.
     */
    public static BinaryMessageDecoder<WeatherData> getDecoder() {
        return DECODER;
    }

    /**
     * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
     * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
     */
    public static BinaryMessageDecoder<WeatherData> createDecoder(SchemaStore resolver) {
        return new BinaryMessageDecoder<WeatherData>(MODEL$, SCHEMA$, resolver);
    }

    /** Deserializes a TrainData from a ByteBuffer. */
    public static WeatherData fromByteBuffer(
            java.nio.ByteBuffer b) throws java.io.IOException {
        return DECODER.decode(b);
    }

    /**
     * Creates a new TrainData RecordBuilder.
     * @return A new TrainData RecordBuilder
     */
    public static WeatherData.Builder newBuilder() {
        return new WeatherData.Builder();
    }

    /**
     * Creates a new TrainData RecordBuilder by copying an existing Builder.
     * @param other The existing builder to copy.
     * @return A new TrainData RecordBuilder
     */
    public static WeatherData.Builder newBuilder(WeatherData.Builder other) {
        return new WeatherData.Builder(other);
    }

    /**
     * Creates a new TrainData RecordBuilder by copying an existing TrainData instance.
     * @param other The existing instance to copy.
     * @return A new TrainData RecordBuilder
     */
    public static WeatherData.Builder newBuilder(WeatherData other) {
        return new WeatherData.Builder(other);
    }

    /** Serializes this TrainData to a ByteBuffer. */
    public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
        return ENCODER.encode(this);
    }

    public org.apache.avro.Schema getSchema() {
        return SCHEMA$;
    }

    // Used by DatumWriter.  Applications should not call.
    public java.lang.Object get(int field$) {
        switch (field$) {
            case 0:
                return id;
            case 1:
                return name;
            default:
                throw new org.apache.avro.AvroRuntimeException("Bad index");
        }
    }

    // Used by DatumReader.  Applications should not call.
    @SuppressWarnings(value = "unchecked")
    public void put(int field$, java.lang.Object value$) {
        switch (field$) {
            case 0:
                id = (java.lang.Integer) value$;
                break;
            case 1:
                name = (String) value$;
                break;
            default:
                throw new org.apache.avro.AvroRuntimeException("Bad index");
        }
    }

    /**
     * Gets the value of the 'id' field.
     * @return The value of the 'id' field.
     */
    public java.lang.Integer getId() {
        return id;
    }

    /**
     * Sets the value of the 'id' field.
     * @param value the value to set.
     */
    public void setId(java.lang.Integer value) {
        this.id = value;
    }

    /**
     * Gets the value of the 'name' field.
     * @return The value of the 'name' field.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the 'name' field.
     * @param value the value to set.
     */
    public void setName(String value) {
        this.name = value;
    }

    @Override
    public void writeExternal(java.io.ObjectOutput out)
            throws java.io.IOException {
        WRITER$.write(this, SpecificData.getEncoder(out));
    }

    @Override
    public void readExternal(java.io.ObjectInput in)
            throws java.io.IOException {
        READER$.read(this, SpecificData.getDecoder(in));
    }

    /**
     * RecordBuilder for TrainData instances.
     */
    public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<WeatherData>
            implements org.apache.avro.data.RecordBuilder<WeatherData> {

        private int id;
        private String name;

        /** Creates a new Builder */
        private Builder() {
            super(SCHEMA$);
        }

        /**
         * Creates a Builder by copying an existing Builder.
         * @param other The existing Builder to copy.
         */
        private Builder(WeatherData.Builder other) {
            super(other);
            if (isValidValue(fields()[0], other.id)) {
                this.id = data().deepCopy(fields()[0].schema(), other.id);
                fieldSetFlags()[0] = true;
            }
            if (isValidValue(fields()[1], other.name)) {
                this.name = data().deepCopy(fields()[1].schema(), other.name);
                fieldSetFlags()[1] = true;
            }
        }

        /**
         * Creates a Builder by copying an existing TrainData instance
         * @param other The existing instance to copy.
         */
        private Builder(WeatherData other) {
            super(SCHEMA$);
            if (isValidValue(fields()[0], other.id)) {
                this.id = data().deepCopy(fields()[0].schema(), other.id);
                fieldSetFlags()[0] = true;
            }
            if (isValidValue(fields()[1], other.name)) {
                this.name = data().deepCopy(fields()[1].schema(), other.name);
                fieldSetFlags()[1] = true;
            }
        }

        /**
         * Gets the value of the 'id' field.
         * @return The value.
         */
        public java.lang.Integer getId() {
            return id;
        }

        /**
         * Sets the value of the 'id' field.
         * @param value The value of 'id'.
         * @return This builder.
         */
        public WeatherData.Builder setId(int value) {
            validate(fields()[0], value);
            this.id = value;
            fieldSetFlags()[0] = true;
            return this;
        }

        /**
         * Checks whether the 'id' field has been set.
         * @return True if the 'id' field has been set, false otherwise.
         */
        public boolean hasId() {
            return fieldSetFlags()[0];
        }


        /**
         * Clears the value of the 'id' field.
         * @return This builder.
         */
        public WeatherData.Builder clearId() {
            fieldSetFlags()[0] = false;
            return this;
        }

        /**
         * Gets the value of the 'name' field.
         * @return The value.
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the value of the 'name' field.
         * @param value The value of 'name'.
         * @return This builder.
         */
        public WeatherData.Builder setName(String value) {
            validate(fields()[1], value);
            this.name = value;
            fieldSetFlags()[1] = true;
            return this;
        }

        /**
         * Checks whether the 'name' field has been set.
         * @return True if the 'name' field has been set, false otherwise.
         */
        public boolean hasName() {
            return fieldSetFlags()[1];
        }


        /**
         * Clears the value of the 'name' field.
         * @return This builder.
         */
        public WeatherData.Builder clearName() {
            name = null;
            fieldSetFlags()[1] = false;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public WeatherData build() {
            try {
                WeatherData record = new WeatherData();
                record.id = fieldSetFlags()[0] ? this.id : (java.lang.Integer) defaultValue(fields()[0]);
                record.name = fieldSetFlags()[1] ? this.name : (String) defaultValue(fields()[1]);
                return record;
            } catch (java.lang.Exception e) {
                throw new org.apache.avro.AvroRuntimeException(e);
            }
        }
    }

}
