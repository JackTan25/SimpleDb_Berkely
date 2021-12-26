package simpledb;

import java.util.*;

/**
 * The Aggregation operator that computes an aggregate (e.g., sum, avg, max,
 * min). Note that we only support aggregates over a single column, grouped by a
 * single column.
 */
@SuppressWarnings("all")
public class Aggregate extends Operator {

    private static final long serialVersionUID = 1L;
    //1.聚合索引
    private int agIndex;
    //2.分组索引
    private int gbIndex;
    //3.具体操作
    private Aggregator.Op op;
    //4.需要聚合的tuples
    private DbIterator child;
    //5.child的类型描述
    private TupleDesc child_td;
    //6.用于实现聚合的聚合器
    private Aggregator aggregator;
    //7.聚合后的结果保存在这里
    private DbIterator aggregateIter;
    //8.指定用于分组的字段的类型
    private Type gbFieldType;
    //9.指定聚合后的类型描述
    private TupleDesc td;
    /**
     * Constructor.
     * 
     * Implementation hint: depending on the type of afield, you will want to
     * construct an {@link IntegerAggregator} or {@link StringAggregator} to help
     * you with your implementation of readNext().
     * 
     * 
     * @param child
     *            The DbIterator that is feeding us tuples.
     * @param afield
     *            The column over which we are computing an aggregate.
     * @param gfield
     *            The column over which we are grouping the result, or -1 if
     *            there is no grouping
     * @param op
     *            The aggregation operator to use
     */
    public Aggregate(DbIterator child, int afield, int gfield, Aggregator.Op op) {
	    // some code goes here
        this.child = child;
        this.agIndex  = afield;
        this.gbIndex = gfield;
        this.op = op;
        child_td = child.getTupleDesc();
        gbFieldType = gbIndex==Aggregator.NO_GROUPING?null:child_td.getFieldType(gbIndex);
        if(child_td.getFieldType(agIndex)==Type.INT_TYPE){
            aggregator = new IntegerAggregator(gbIndex,gbFieldType,agIndex,op,getTupleDesc());
        }else if(child_td.getFieldType(agIndex)==Type.STRING_TYPE){
            aggregator = new StringAggregator(gbIndex,gbFieldType,agIndex,op,getTupleDesc());
        }
    }
    /**
     * @return If this aggregate is accompanied by a groupby, return the groupby
     *         field index in the <b>INPUT</b> tuples. If not, return
     *         {@link simpledb.Aggregator#NO_GROUPING}
     * */
    public int groupField() {
        // some code goes here
        return gbIndex;
    }

    /**
     * @return If this aggregate is accompanied by a group by, return the name
     *         of the groupby field in the <b>OUTPUT</b> tuples If not, return
     *         null;
     * */
    public String groupFieldName() {
        // some code goes here
        return gbIndex==Aggregator.NO_GROUPING?null:td.getFieldName(gbIndex);
    }

    /**
     * @return the aggregate field
     * */
    public int aggregateField() {
        // some code goes here
        return agIndex;
    }

    /**
     * @return return the name of the aggregate field in the <b>OUTPUT</b>
     *         tuples
     * */
    public String aggregateFieldName() {
	    // some code goes here
	    return td.getFieldName(agIndex);
    }

    /**
     * @return return the aggregate operator
     * */
    public Aggregator.Op aggregateOp() {
        // some code goes here
        return op;
    }

    public static String nameOfAggregatorOp(Aggregator.Op aop) {
	    return aop.toString();
    }

    @Override
    public void open() throws NoSuchElementException, DbException,
	    TransactionAbortedException {
	    // some code goes here
        super.open();
        child.open();
        while(child.hasNext()){
            Tuple next = child.next();
            aggregator.mergeTupleIntoGroup(next);
        }
        aggregateIter = aggregator.iterator();
        aggregateIter.open();
    }

    /**
     * Returns the next tuple. If there is a group by field, then the first
     * field is the field by which we are grouping, and the second field is the
     * result of computing the aggregate, If there is no group by field, then
     * the result tuple should contain one field representing the result of the
     * aggregate. Should return null if there are no more tuples.
     */
    @Override
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
        // some code goes here
        if(aggregateIter.hasNext()){
            return aggregateIter.next();
        }else{
            return null;
        }
    }

    @Override
    public void rewind() throws DbException, TransactionAbortedException {
	    // some code goes here
        aggregateIter.rewind();
    }

    /**
     * Returns the TupleDesc of this Aggregate. If there is no group by field,
     * this will have one field - the aggregate column. If there is a group by
     * field, the first field will be the group by field, and the second will be
     * the aggregate value column.
     * 
     * The name of an aggregate column should be informative. For example:
     * "aggName(aop) (child_td.getFieldName(afield))" where aop and afield are
     * given in the constructor, and child_td is the TupleDesc of the child
     * iterator.
     */
    @Override
    public TupleDesc getTupleDesc() {
	// some code goes here
        if(td!=null){
            return td;
        }else{
            String[] names;
            Type[] types;
            if(gbIndex==Aggregator.NO_GROUPING){
                types = new Type[]{Type.INT_TYPE};
                names = new String[]{child_td.getFieldName(agIndex)};
            }else{
                types = new Type[]{child_td.getFieldType(gbIndex),Type.INT_TYPE};
                names = new String[]{child_td.getFieldName(gbIndex),child_td.getFieldName(agIndex)};
            }
            td =  new TupleDesc(types,names);
            return td;
        }
    }

    @Override
    public void close() {
	    // some code goes here
        super.close();
        aggregateIter.close();
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
