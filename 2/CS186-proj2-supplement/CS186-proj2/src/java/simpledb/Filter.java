package simpledb;
import java.util.*;

/**
 * Filter is an operator that implements a relational select.
 */
public class Filter extends Operator {

    private static final long serialVersionUID = 1L;
    //1.过滤时用到的表达式
    private Predicate predicate;
    //2.元组类型和属性名解释
    private TupleDesc td;
    //3.子迭代器,即需要根据predicate过滤掉
    private DbIterator child;
    //4.缓存过滤掉的结果,加快hasNext和next方法
    private TupleIterator filterResult;
    /**
     * Constructor accepts a predicate to apply and a child operator to read
     * tuples to filter from.
     * 
     * @param p
     *            The predicate to filter tuples with
     * @param child
     *            The child operator
     */
    public Filter(Predicate p, DbIterator child) {
        // some code goes here
        this.child = child;
        this.predicate = p;
        this.td = child.getTupleDesc();
    }

    public Predicate getPredicate() {
        // some code goes here
        return predicate;
    }

    @Override
    public TupleDesc getTupleDesc() {
        // some code goes here
        return td;
    }

    @Override
    public void open() throws DbException, NoSuchElementException,
            TransactionAbortedException {
        // some code goes here
        child.open();
        super.open();
        filterResult = filter(child,predicate);
        filterResult.open();
    }
    public TupleIterator filter(DbIterator child,Predicate predicate) throws DbException, TransactionAbortedException {
        ArrayList<Tuple> tuples = new ArrayList<>();
        while(child.hasNext()){
            Tuple tuple = child.next();
            if(predicate.filter(tuple)){
                tuples.add(tuple);
            }
        }
        return new TupleIterator(child.getTupleDesc(),tuples);
    }
    public void close() {
        // some code goes here
        child.close();
        super.close();
        filterResult = null;
    }

    public void rewind() throws DbException, TransactionAbortedException {
        // some code goes here
        filterResult.rewind();
    }

    /**
     * AbstractDbIterator.readNext implementation. Iterates over tuples from the
     * child operator, applying the predicate to them and returning those that
     * pass the predicate (i.e. for which the Predicate.filter() returns true.)
     * 
     * @return The next tuple that passes the filter, or null if there are no
     *         more tuples
     * @see Predicate#filter
     */
    protected Tuple fetchNext() throws NoSuchElementException,
            TransactionAbortedException, DbException {
        // some code goes here
        if(filterResult.hasNext()) {
            return filterResult.next();
        } else {
            return null;
        }
    }

    @Override
    public DbIterator[] getChildren() {
        // some code goes here
        return new DbIterator[]{this.child};
    }

    @Override
    public void setChildren(DbIterator[] children) {
        // some code goes here
        child = children[0];
    }

}
