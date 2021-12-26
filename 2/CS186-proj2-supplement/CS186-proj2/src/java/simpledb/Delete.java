package simpledb;

/**
 * The delete operator. Delete reads tuples from its child operator and removes
 * them from the table they belong to.
 */
public class Delete extends Operator {

    private static final long serialVersionUID = 1L;

    //1.指定事务Id
    private TransactionId tid;
    //2.需要插入的tule迭代器
    private DbIterator child;
    //3.指定要插入的表Id
    private int tableId;
    //4.记录插入后影响的tuple的数量
    int count;
    //5.插入后影响的行数count单独作为一个td
    TupleDesc td;
    //6.是否已经获取过插入影响数量
    boolean hasAccessed;
    /**
     * Constructor specifying the transaction that this delete belongs to as
     * well as the child to read from.
     * 
     * @param t
     *            The transaction this delete runs in
     * @param child
     *            The child operator from which to read tuples for deletion
     */
    public Delete(TransactionId t, DbIterator child) {
        // some code goes here
        this.tid = t;
        this.child = child;
        this.count = 0;
        this.td = new TupleDesc(new Type[]{Type.INT_TYPE});
    }

    @Override
    public TupleDesc getTupleDesc() {
        // some code goes here
        return td;
    }

    @Override
    public void open() throws DbException, TransactionAbortedException {
        // some code goes here
        super.open();
        child.open();
        hasAccessed = false;
        while(child.hasNext()){
            Database.getBufferPool().deleteTuple(tid,child.next());count++;
        }
    }

    @Override
    public void close() {
        // some code goes here
        super.close();
        child.close();
    }

    @Override
    public void rewind() throws DbException, TransactionAbortedException {
        // some code goes here
        hasAccessed = false;
    }

    /**
     * Deletes tuples as they are read from the child operator. Deletes are
     * processed via the buffer pool (which can be accessed via the
     * Database.getBufferPool() method.
     * 
     * @return A 1-field tuple containing the number of deleted records.
     * @see Database#getBufferPool
     * @see BufferPool#deleteTuple
     */
    @Override
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
        // some code goes here
        if(hasAccessed){
            return null;
        }
        Tuple tuple = new Tuple(td);
        tuple.setField(0,new IntField(count));
        hasAccessed = true;
        return tuple;
    }

    @Override
    public DbIterator[] getChildren() {
        // some code goes here
        return new DbIterator[]{child};
    }

    @Override
    public void setChildren(DbIterator[] children) {
        // some code goes here
        child = children[0];
    }
}
