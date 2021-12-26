package simpledb;

import java.io.IOException;

/**
 * Inserts tuples read from the child operator into the tableid specified in the
 * constructor
 */
@SuppressWarnings("All")
public class Insert extends Operator {

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
     * Constructor.
     *
     * @param t
     *            The transaction running the insert.
     * @param child
     *            The child operator from which to read tuples to be inserted.
     * @param tableid
     *            The table in which to insert tuples.
     * @throws DbException
     *             if TupleDesc of child differs from table into which we are to
     *             insert.
     */
    public Insert(TransactionId t,DbIterator child, int tableid)
            throws DbException {
        // some code goes here
        this.tid = t;
        this.child = child;
        this.tableId = tableid;
        this.count = 0;
        this.td = new TupleDesc(new Type[]{Type.INT_TYPE});
    }

    @Override
    public TupleDesc getTupleDesc() {
        // some code goes here
        return td;
    }

    @Override
    public void open() throws DbException, TransactionAbortedException{
        // some code goes here
        hasAccessed = false;
        super.open();
        child.open();
        while(child.hasNext()){
            try {
                Database.getBufferPool().insertTuple(tid,tableId,child.next());
            } catch (IOException e) {
                e.printStackTrace();
            }
            count++;
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
     * Inserts tuples read from child into the tableid specified by the
     * constructor. It returns a one field tuple containing the number of
     * inserted records. Inserts should be passed through BufferPool. An
     * instances of BufferPool is available via Database.getBufferPool(). Note
     * that insert DOES NOT need check to see if a particular tuple is a
     * duplicate before inserting it.
     *
     * @return A 1-field tuple containing the number of inserted records, or
     *         null if called more than once.
     * @see Database#getBufferPool
     * @see BufferPool#insertTuple
     */
    @Override
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
        // some code goes here
        if(hasAccessed) {
            return null;
        }
        hasAccessed = true;
        Tuple tuple = new Tuple(td);
        tuple.setField(0,new IntField(count));
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
