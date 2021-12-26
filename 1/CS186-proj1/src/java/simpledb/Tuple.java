package simpledb;
import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Tuple maintains information about the contents of a tuple. Tuples have a
 * specified schema specified by a TupleDesc object and contain Field objects
 * with the data for each field.
 */
public class Tuple implements Serializable {

    private static final long serialVersionUID = 1L;

    TupleDesc td;

    Field[] fields;

    RecordId recordId;
    /**
     * Create a new tuple with the specified schema (type).
     * 
     * @param td
     *            the schema of this tuple. It must be a valid TupleDesc
     *            instance with at least one field.
     */
    public Tuple(TupleDesc td) {
        // some code goes here
        if(td==null||td.numFileds<=0){
            throw new IllegalArgumentException();
        }
        this.td = td;
        fields = new Field[td.numFileds];
    }

    /**
     * @return The TupleDesc representing the schema of this tuple.
     */
    public TupleDesc getTupleDesc() {
        // some code goes here
        return td;
    }

    /**
     * @return The RecordId representing the location of this tuple on disk. May
     *         be null.
     */
    public RecordId getRecordId() {
        // some code goes here
        return recordId;
    }

    /**
     * Set the RecordId information for this tuple.
     * 
     * @param rid
     *            the new RecordId for this tuple.
     */
    public void setRecordId(RecordId rid) {
        // some code goes here
        this.recordId = recordId;
    }

    /**
     * Change the value of the ith field of this tuple.
     * 
     * @param i
     *            index of the field to change. It must be a valid index.
     * @param f
     *            new value for the field.
     */
    public void setField(int i, Field f) {
        // some code goes here
        if(i<0||i>fields.length){
            throw new IndexOutOfBoundsException();
        }
        fields[i] = f;
    }

    /**
     * @return the value of the ith field, or null if it has not been set.
     * 
     * @param i
     *            field index to return. Must be a valid index.
     */
    public Field getField(int i) {
        // some code goes here
        if(i<0||i>fields.length){
            throw new IndexOutOfBoundsException();
        }
        return fields[i];
    }

    /**
     * Returns the contents of this Tuple as a string. Note that to pass the
     * system tests, the format needs to be as follows:
     * 
     * column1\tcolumn2\tcolumn3\t...\tcolumnN\n
     * 
     * where \t is any whitespace, except newline, and \n is a newline
     */
    @Override
    public String toString() {
        // some code goes here
        StringBuilder builder = new StringBuilder();
        for (Field field : fields) {
            builder.append(field.toString()+"\t");
        }
        builder.append("\n");
        return builder.toString();
    }
    
    /**
     * @return
     *        An iterator which iterates over all the fields of this tuple
     * */
    public Iterator<Field> fields() {
        // some code goes here
        return new FieldIterator();
    }

    private class FieldIterator implements Iterator<Field> {
        private int pos = 0;

        @Override
        public boolean hasNext() {
            return fields.length > pos;
        }

        @Override
        public Field next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return fields[pos++];
        }
    }
}