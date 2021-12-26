package simpledb;
import java.util.*;
/**
 * The Join operator implements the relational join operation.
 */
@SuppressWarnings("all")
public class Join extends Operator {

    private static final long serialVersionUID = 1L;
    //1.用于进行连接判断
    private JoinPredicate joinPredicate;
    //2.左子迭代器
    private DbIterator child1;
    //3.右子迭代器
    private DbIterator child2;
    //4.缓存联接后的结果
    DbIterator filterResult;
    //5.连接后属性类型和名字
    TupleDesc td;
    //6.缓冲区大小设置
    int memorySize = 131072;
    /**
     * Constructor. Accepts to children to join and the predicate to join them
     * on
     *
     * @param p
     *            The predicate to use to join the children
     * @param child1
     *            Iterator for the left(outer) relation to join
     * @param child2
     *            Iterator for the right(inner) relation to join
     */
    public Join(JoinPredicate p, DbIterator child1, DbIterator child2) {
        // some code goes here
        this.joinPredicate = p;
        this.child1 = child1;
        this.child2 = child2;
        td = getTupleDesc();
    }

    public JoinPredicate getJoinPredicate() {
        // some code goes here
        return joinPredicate;
    }

    /**
     * @return
     *       the field name of join field1. Should be quantified by
     *       alias or table name.
     * */
    public String getJoinField1Name() {
        // some code goes here
        return child1.getTupleDesc().getFieldName(joinPredicate.getIndex1());
    }

    /**
     * @return
     *       the field name of join field2. Should be quantified by
     *       alias or table name.
     * */
    public String getJoinField2Name() {
        // some code goes here
        return child2.getTupleDesc().getFieldName(joinPredicate.getIndex2());
    }

    /**
     * @see simpledb.TupleDesc#merge(TupleDesc, TupleDesc) for possible
     *      implementation logic.
     */
    @Override
    public TupleDesc getTupleDesc() {
        // some code goes here
        return TupleDesc.merge(child1.getTupleDesc(),child2.getTupleDesc());
    }

    @Override
    public void open() throws DbException, NoSuchElementException,
            TransactionAbortedException {
        // some code goes here
        super.open();
        child1.open();
        child2.open();
        td = getTupleDesc();
        filterResult = dbnlSortedJoin(child1,child2,joinPredicate);
        filterResult.open();
    }
    public TupleIterator dbnlSortedJoin(DbIterator child1,DbIterator child2,JoinPredicate predicate) throws DbException, TransactionAbortedException {
        ArrayList<Tuple> tuples = new ArrayList<>();
        //计算缓冲区1与缓存区2的容量
        int cacheSize1 = memorySize/child1.getTupleDesc().getSize();
        int cacheSize2 = memorySize/child2.getTupleDesc().getSize();
        Tuple[] leftCache = new Tuple[cacheSize1];
        Tuple[] rightCache = new Tuple[cacheSize2];
        int idx1 = 0,idx2 = 0;
        while(child1.hasNext()){
            Tuple left = child1.next();
            leftCache[idx1++] = left;
            if(idx1>=leftCache.length){//缓冲区1已经满了
                child2.rewind();//重置2
                while(child2.hasNext()){
                    Tuple right = child2.next();
                    rightCache[idx2++] = right;
                    if(idx2>=rightCache.length){//缓冲区2也满了
                        mergeSortedJoin(leftCache,rightCache,predicate,tuples);
                        idx2 = 0;
                        Arrays.fill(rightCache,null);
                    }
                }
                if(idx2>0){//最后一批不满整个缓冲区的
                    mergeSortedJoin(leftCache,rightCache,predicate,tuples);
                    idx2 = 0;
                    Arrays.fill(rightCache,null);
                }
                idx1 = 0;
                Arrays.fill(leftCache,null);
            }
        }
        if(idx1>0){//最后一批不满整个缓冲区的
            while(child2.hasNext()){
                Tuple right = child2.next();
                rightCache[idx2++] = right;
                if(idx2>=rightCache.length){//缓冲区2也满了
                    mergeSortedJoin(leftCache,rightCache,predicate,tuples);
                    idx2 = 0;
                    Arrays.fill(rightCache,null);
                }
            }
            if(idx2>0){//最后一批不满整个缓冲区的
                mergeSortedJoin(leftCache,rightCache,predicate,tuples);
                idx2 = 0;
                Arrays.fill(rightCache,null);
            }
        }
        return new TupleIterator(td,tuples);
    }

    public void mergeSortedJoin(Tuple[] leftCache,Tuple[] rightCache,JoinPredicate predicate,List<Tuple> tuples){
        int m = leftCache.length-1;
        int n = rightCache.length-1;
        //将后面的null清空掉
        while(m>0&&leftCache[m]==null) {
            m--;
        }
        while(n>0&&rightCache[n]==null){
            n--;
        }
        //接下来就是排序
        int idx1 = predicate.getIndex1();
        int idx2 = predicate.getIndex2();
        JoinPredicate rt1 = new JoinPredicate(idx1,Predicate.Op.GREATER_THAN,idx1);
        JoinPredicate lt1 = new JoinPredicate(idx1,Predicate.Op.LESS_THAN,idx1);
        JoinPredicate rt2 = new JoinPredicate(idx2,Predicate.Op.GREATER_THAN,idx2);
        JoinPredicate lt2 = new JoinPredicate(idx2,Predicate.Op.LESS_THAN,idx2);
        JoinPredicate rt = new JoinPredicate(idx1,Predicate.Op.GREATER_THAN,idx2);
        Comparator<Tuple> comparator1 = new Comparator<Tuple>(){
            @Override
            public int compare(Tuple o1, Tuple o2) {
                if(rt1.filter(o1,o2)){
                    return 1;
                }else if(lt1.filter(o1,o2)){
                    return -1;
                }else{
                    return 0;
                }
            }
        };
        Comparator<Tuple> comparator2 = new Comparator<Tuple>() {
            @Override
            public int compare(Tuple o1, Tuple o2) {
                if(rt2.filter(o1,o2)){
                    return 1;
                }else if(lt2.filter(o1,o2)){
                    return -1;
                }else{
                    return 0;
                }
            }
        };
        Arrays.sort(leftCache,0,m+1,comparator1);
        Arrays.sort(rightCache,0,n+1,comparator2);
        //排完序之后我们就应该去匹配了
        switch (predicate.getOperator()){
            case EQUALS:
                int pos1 = 0,pos2 = 0;
                while(pos1<=m&&pos2<=n){
                    Tuple left = leftCache[pos1];
                    Tuple right = rightCache[pos2];
                    if(predicate.filter(left,right)){
                        int k1 = pos1,k2 = pos2;
                        while(k1<=m&&predicate.filter(leftCache[k1],right)){
                            k1++;
                        }
                        while(k2<=n&&predicate.filter(left,rightCache[k2])){
                            k2++;
                        }
                        for(int i=pos1;i<k1;i++){
                            for(int j=pos2;j<k2;j++){
                                tuples.add(mergeTuple(leftCache[i],rightCache[j]));
                            }
                        }
                        pos1 = k1;pos2= k2;
                    }else if(rt.filter(left,right)){
                        pos2++;
                    }else{
                        pos1++;
                    }
                }
                break;
            case GREATER_THAN:
            case GREATER_THAN_OR_EQ:
                pos1 = 0;
                while(pos1<=m){
                    pos2 = 0;
                    while(pos2<=n&&predicate.filter(leftCache[pos1],rightCache[pos2])){
                        Tuple tuple = mergeTuple(leftCache[pos1],rightCache[pos2]);
                        tuples.add(tuple);
                        pos2++;
                    }
                    pos1++;
                }
                break;
            case LESS_THAN:
            case LESS_THAN_OR_EQ:
                pos1 = 0;
                while(pos1<=m){
                    pos2 = n;
                    while (pos2>=0&&predicate.filter(leftCache[pos1],rightCache[pos2])){
                        Tuple tuple = mergeTuple(leftCache[pos1],rightCache[pos2]);
                        tuples.add(tuple);
                        pos2--;
                    }
                    pos1++;
                }
        }
    }

    public Tuple mergeTuple(Tuple t1,Tuple t2){
        Tuple td = new Tuple(this.td);
        for(int i=0;i<t1.getTupleDesc().numFields();i++){
            td.setField(i,t1.getField(i));
        }
        int len = t1.getTupleDesc().numFields();
        for(int i=len;i<len+t2.getTupleDesc().numFields();i++){
            td.setField(i,t2.getField(i-len));
        }
        return td;
    }
    @Override
    public void close() {
        // some code goes here
        super.close();
        child1.close();
        child2.close();
        filterResult.close();
    }

    @Override
    public void rewind() throws DbException, TransactionAbortedException {
        // some code goes here
        child2.rewind();
        child1.rewind();
        filterResult.rewind();
    }

    /**
     * Returns the next tuple generated by the join, or null if there are no
     * more tuples. Logically, this is the next tuple in r1 cross r2 that
     * satisfies the join predicate. There are many possible implementations;
     * the simplest is a nested loops join.
     * <p>
     * Note that the tuples returned from this particular implementation of Join
     * are simply the concatenation of joining tuples from the left and right
     * relation. Therefore, if an equality predicate is used there will be two
     * copies of the join attribute in the results. (Removing such duplicate
     * columns can be done with an additional projection operator if needed.)
     * <p>
     * For example, if one tuple is {1,2,3} and the other tuple is {1,5,6},
     * joined on equality of the first column, then this returns {1,2,3,1,5,6}.
     *
     * @return The next matching tuple.
     * @see JoinPredicate#filter
     */
    @Override
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
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
        return new DbIterator[]{child1,child2};
    }

    @Override
    public void setChildren(DbIterator[] children) {
        // some code goes here
        child1 = children[0];
        child2 = children[1];
    }
}
