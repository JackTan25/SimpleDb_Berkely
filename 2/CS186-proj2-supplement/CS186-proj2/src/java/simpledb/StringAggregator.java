package simpledb;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Knows how to compute some aggregate over a set of StringFields.
 */
@SuppressWarnings("all")
public class StringAggregator implements Aggregator {

    private static final long serialVersionUID = 1L;

    //1.指定分组的属性的索引
    int gbIndex;
    //2.指定分组字段的类型
    Type gbType;
    //3.指定被聚合的字段的索引
    int agIndex;
    //4.指定聚合前的原始td
    TupleDesc originTd;
    //5.指定聚合后的td
    TupleDesc td;
    //6.存储目前已经处理的tuple随分组聚合后的结果
    HashMap<Field,Integer> gbv2aggrv;
    //7.聚合操作的类型
    Op op;
    /**
     * Aggregate constructor
     * @param gbfield the 0-based index of the group-by field in the tuple, or NO_GROUPING if there is no grouping
     * @param gbfieldtype the type of the group by field (e.g., Type.INT_TYPE), or null if there is no grouping
     * @param afield the 0-based index of the aggregate field in the tuple
     * @param what aggregation operator to use -- only supports COUNT
     * @throws IllegalArgumentException if what != COUNT
     */

    public StringAggregator(int gbIndex, Type gbfieldtype, int agIndex, Op op,TupleDesc td) {
        // some code goes here
        if(op!=Op.COUNT){
            throw new IllegalArgumentException("String类型不支持"+op.toString()+"操作,支持Count操作");
        }
        this.agIndex = agIndex;
        this.gbIndex = gbIndex;
        this.gbType = gbfieldtype;
        this.td = td;
        this.op = op;
        gbv2aggrv = new HashMap<>();
    }

    /**
     * Merge a new tuple into the aggregate, grouping as indicated in the constructor
     * @param tup the Tuple containing an aggregate field and a group-by field
     */
    @Override
    public void mergeTupleIntoGroup(Tuple tup) {
        // some code goes here
        //首先判断输入的tuple是否合法
        //1.看对应的分组字段类型是否一致
        if(gbIndex!=Aggregator.NO_GROUPING) {
            if(gbIndex>=tup.getTupleDesc().numFields()||!tup.getTupleDesc().getFieldType(gbIndex).equals(gbType)){
                throw new IllegalArgumentException("分组索引越界或者分组类型不对应");
            }
        }
        //2.tuple是否和originTd一致
        if(agIndex>=tup.getTupleDesc().numFields()||tup.getTupleDesc().getFieldType(agIndex)!=Type.STRING_TYPE){
            throw new IllegalArgumentException("聚合索引越界或者聚合类型不是整型");
        }
        //3.判断传入的tuple是否和之前的一致
        if(originTd==null){
            originTd = tup.getTupleDesc();
        }else if(!originTd.equals(tup.getTupleDesc())){
            throw new IllegalArgumentException("tuple类型错误");
        }
        //分组字段
        Field gbField = null;
        if(gbIndex!=Aggregator.NO_GROUPING){
            gbField = tup.getField(gbIndex);
        }
        //聚合字段
            StringField aggrField = (StringField) tup.getField(agIndex);
        if(gbv2aggrv.containsKey(gbField)){
            int oldValue = gbv2aggrv.get(gbField);
            gbv2aggrv.put(gbField,oldValue+1);
        }else{
            gbv2aggrv.put(gbField,1);
        }
    }

    /**
     * Create a DbIterator over group aggregate results.
     *
     * @return a DbIterator whose tuples are the pair (groupVal,
     *   aggregateVal) if using group, or a single (aggregateVal) if no
     *   grouping. The aggregateVal is determined by the type of
     *   aggregate specified in the constructor.
     */
    @Override
    public DbIterator iterator() {
        // some code goes here
        ArrayList<Tuple> tuples = new ArrayList<>();
        for (Field field : gbv2aggrv.keySet()) {
            Tuple tuple = new Tuple(td);
            if(gbIndex!=Aggregator.NO_GROUPING){
                tuple.setField(0,field);
                tuple.setField(1,new IntField(gbv2aggrv.get(field)));
            }else{
                tuple.setField(0,new IntField(gbv2aggrv.get(field)));
            }
            tuples.add(tuple);
        }
        return new TupleIterator(td,tuples);
    }
}
