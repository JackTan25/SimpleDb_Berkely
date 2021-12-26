package simpledb;
import java.io.Serializable;
import java.util.*;

/**
 * TupleDesc describes the schema of a tuple.
 * 译: TupleDesc用来描述元组的模式或架构
 */
@SuppressWarnings("All")
public class TupleDesc implements Serializable {
    /**
     * 记录TupleDesc里的所有TDItems
     */
    int numFileds;
    TDItem[] tdItems;
    /**
     * A help class to facilitate organizing the information of each field
     * 译:这是一个用于帮助组织各个字段的信息的帮助类
     * */
    public static class TDItem implements Serializable {
        private static final long serialVersionUID = 1L;
        /**
         * The type of the field
         * 译：字段的类型
         * */
        Type fieldType;
        /**
         * The name of the field
         * 译：字段的名字
         * */
        String fieldName;

        public TDItem(Type t, String n) {
            this.fieldName = n;
            this.fieldType = t;
        }

        @Override
        public String toString() {
            return fieldName + "(" + fieldType + ")";
        }

        @Override
        public boolean equals(Object obj) {
            //如果指向同一个对象，就直接返回最好
            if(this == obj){
                return true;
            }
            if(obj instanceof TDItem){
                TDItem another = (TDItem) obj;
                boolean typeBool = (another.fieldType.equals(this.fieldType));
                boolean nameBool = (another.fieldName==null&&fieldName==null||another.fieldName.equals(this.fieldName));
                return typeBool&&nameBool;
            }else{
                return false;
            }
        }
    }

    /**
     * @return
     *        An iterator which iterates over all the field TDItems
     *        that are included in this TupleDesc
     *  译：可以返回包含这个TupleDesc的字段TDItems内容的迭代器
     * */
    public Iterator<TDItem> iterator() {
        // some code goes here
        return new TDItemIterator();
    }
    /**
     * 首先实现迭代器,这个迭代器要返回当前TupleDesc的所以TDItem,那么这个我们显然需要有一个存放所有TDItem的域
     */
    public class TDItemIterator implements Iterator<TDItem>{
        int pos = -1;
        @Override
        public boolean hasNext() {
            return pos<tdItems.length-1;
        }

        @Override
        public TDItem next() {
            if(!hasNext()){
                throw new NoSuchElementException();
            }
            return tdItems[pos+1];
        }
    }
    private static final long serialVersionUID = 1L;

    /**
     * Create a new TupleDesc with typeAr.length fields with fields of the
     * specified types, with associated named fields.
     * 译：构造函数,创建一个新的有typeAr.length个字段的TupleDesc对象，同时给定每个字段的特定类型和每个字段的名字
     * @param typeAr
     *            array specifying the number of and types of fields in this
     *            TupleDesc. It must contain at least one entry.
     *            译：包含了在本TupleDesc中所有字段类型和字段数量的数组，长度至少为1
     * @param fieldAr
     *            array specifying the names of the fields. Note that names may
     *            be null.
     *            译：包含了所有字段的名字，注意names可能为null
     */
    public TupleDesc(Type[] typeAr, String[] fieldAr) {
        // some code goes here
        if(typeAr.length==0){
            throw new IllegalArgumentException("字段类型数组至少含有一个元素");
        }
        if(fieldAr.length!=typeAr.length){
            throw new IllegalArgumentException("字段类型数组的长度与字段名数组的长度不一致");
        }
        numFileds = typeAr.length;
        tdItems = new TDItem[numFileds];
        for(int i=0;i<tdItems.length;i++){
            tdItems[i] = new TDItem(typeAr[i],fieldAr[i]);
        }
    }

    /**
     * Constructor. Create a new tuple desc with typeAr.length fields with
     * fields of the specified types, with anonymous (unnamed) fields.
     * 译：构造器，创建一个带有各字段类型的新的TupleDesc，但是每个字段名字是匿名的
     * @param typeAr
     *            array specifying the number of and types of fields in this
     *            TupleDesc. It must contain at least one entry.
     *            译：包含了在本TupleDesc中所有字段类型和字段数量的数组，长度至少为1
     */
    public TupleDesc(Type[] typeAr) {
        // some code goes here
        /*if(typeAr.length==0){
            throw new IllegalArgumentException("类型数组的长度应至少为1");
        }
        numFileds = typeAr.length;
        tdItems = new TDItem[numFileds];
        for(int i=0;i<numFileds;i++){
            tdItems[i] = new TDItem(typeAr[i],null);
        }*/
        //更好的写法
        this(typeAr,new String[typeAr.length]);
    }

    /**
     * @return the number of fields in this TupleDesc
     */
    public int numFields() {
        // some code goes here
        return this.numFileds;
    }

    /**
     * Gets the (possibly null) field name of the ith field of this TupleDesc.
     * 
     * @param i
     *            index of the field name to return. It must be a valid index.
     * @return the name of the ith field
     * @throws NoSuchElementException
     *             if i is not a valid field reference.
     */
    public String getFieldName(int i) throws NoSuchElementException {
        // some code goes here
        if(i<0||i>tdItems.length-1){
            throw new NoSuchElementException("不存在该元素");
        }
        return tdItems[i].fieldName;
    }

    /**
     * Gets the type of the ith field of this TupleDesc.
     * 
     * @param i
     *            The index of the field to get the type of. It must be a valid
     *            index.
     * @return the type of the ith field
     * @throws NoSuchElementException
     *             if i is not a valid field reference.
     */
    public Type getFieldType(int i) throws NoSuchElementException {
        // some code goes here
        if(i<0||i>tdItems.length-1){
            throw new NoSuchElementException("不存在该元素");
        }
        return tdItems[i].fieldType;
    }

    /**
     * Find the index of the field with a given name.
     * 
     * @param name
     *            name of the field.
     * @return the index of the field that is first to have the given name.
     * @throws NoSuchElementException
     *             if no field with a matching name is found.
     */
    public int fieldNameToIndex(String name) throws NoSuchElementException {
        // some code goes here
        for(int i=0;i<tdItems.length;i++){
            if(tdItems[i].fieldName!=null&&tdItems[i].fieldName.equals(name)){
                return i;
            }
        }
        throw new NoSuchElementException();
    }

    /**
     * @return The size (in bytes) of tuples corresponding to this TupleDesc.
     *         Note that tuples from a given TupleDesc are of a fixed size.
     */
    public int getSize() {
        // some code goes here
        int size = 0;
        for(int i=0;i<tdItems.length;i++){
            size += tdItems[i].fieldType.getLen();
        }
        return size;
    }

    /**
     * Merge two TupleDescs into one, with td1.numFields + td2.numFields fields,
     * with the first td1.numFields coming from td1 and the remaining from td2.
     * 
     * @param td1
     *            The TupleDesc with the first fields of the new TupleDesc
     * @param td2
     *            The TupleDesc with the last fields of the TupleDesc
     * @return the new TupleDesc
     */
    public static TupleDesc merge(TupleDesc td1, TupleDesc td2) {
        // some code goes here
        int numsFiled = td1.numFileds+td2.numFileds;
        Type[] types = new Type[numsFiled];String[] names = new String[numsFiled];
        int cnt = 0;
        for(int i=0;i<td1.numFileds;i++){
            types[cnt] = td1.tdItems[i].fieldType;
            names[cnt++] = td1.tdItems[i].fieldName;
        }
        for(int i=0;i<td2.numFileds;i++){
            types[cnt] = td2.tdItems[i].fieldType;
            names[cnt++] = td2.tdItems[i].fieldName;
        }
        return new TupleDesc(types,names);
    }

    /**
     * Compares the specified object with this TupleDesc for equality. Two
     * TupleDescs are considered equal if they are the same size and if the n-th
     * type in this TupleDesc is equal to the n-th type in td.
     * 
     * @param o
     *            the Object to be compared for equality with this TupleDesc.
     * @return true if the object is equal to this TupleDesc.
     */
    @Override
    public boolean equals(Object o) {
        // some code goes here
        if(this==o){
            return true;
        }
        if(o instanceof TupleDesc){
            TupleDesc another = (TupleDesc) o;
            if(another.numFileds!=this.numFileds){
                return false;
            }
            for(int i=0;i<this.numFileds;i++){
                if(!this.tdItems[i].equals(another.tdItems[i])){
                    return false;
                }
            }
            return true;
        }else{
            return false;
        }
    }

    @Override
    public int hashCode() {
        // If you want to use TupleDesc as keys for HashMap, implement this so
        // that equal objects have equals hashCode() results
        throw new UnsupportedOperationException("unimplemented");
    }

    /**
     * Returns a String describing this descriptor. It should be of the form
     * "fieldType[0](fieldName[0]), ..., fieldType[M](fieldName[M])", although
     * the exact format does not matter.
     * 
     * @return String describing this descriptor.
     */
    @Override
    public String toString() {
        // some code goes here
        String res = "";
        for(int i=0;i<this.numFileds;i++){
            res += "fileType["+i+"]"+"("+"fieldName["+i+"]"+")";
            if(i!=this.numFileds-1){
                res+=",";
            }
        }
        return res;
    }
}
