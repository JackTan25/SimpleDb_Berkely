package simpledb;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * TableStats represents statistics (e.g., histograms) about base tables in a
 * query. 
 * 
 * This class is not needed in implementing proj1 and proj2.
 */
public class TableStats {

    private static final ConcurrentHashMap<String, TableStats> statsMap = new ConcurrentHashMap<String, TableStats>();

    static final int IOCOSTPERPAGE = 1000;

    public static TableStats getTableStats(String tablename) {
        return statsMap.get(tablename);
    }

    public static void setTableStats(String tablename, TableStats stats) {
        statsMap.put(tablename, stats);
    }
    
    public static void setStatsMap(HashMap<String,TableStats> s)
    {
        try {
            java.lang.reflect.Field statsMapF = TableStats.class.getDeclaredField("statsMap");
            statsMapF.setAccessible(true);
            statsMapF.set(null, s);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, TableStats> getStatsMap() {
        return statsMap;
    }

    public static void computeStatistics() {
        Iterator<Integer> tableIt = Database.getCatalog().tableIdIterator();

        System.out.println("Computing table stats.");
        while (tableIt.hasNext()) {
            int tableid = tableIt.next();
            TableStats s = new TableStats(tableid, IOCOSTPERPAGE);
            setTableStats(Database.getCatalog().getTableName(tableid), s);
        }
        System.out.println("Done.");
    }

    /**
     * Number of bins for the histogram. Feel free to increase this value over
     * 100, though our tests assume that you have at least 100 bins in your
     * histograms.
     */
    //1.指定我们生成直方图的分组数
    static final int NUM_HIST_BINS = 100;
    //2.Key对应一个字段的名字,Val对应这个字段的最小和最大值
    HashMap<String,Integer[]> attr;
    //3.这里是为每一个字段生成一个直方图
    HashMap<String,Object> name2hist;
    //4.指定表
    HeapFile table;
    //5.指定表的属性类型和名字
    TupleDesc td;
    //6.说明对每一页进行读取的时候IO成本的大小
    int ioCostPerPage;
    //7.记录总的元组数
    int ntups;
    /**
     * Create a new TableStats object, that keeps track of statistics on each
     * column of a table
     * 
     * @param tableid
     *            The table over which to compute statistics
     * @param ioCostPerPage
     *            The cost per page of IO. This doesn't differentiate between
     *            sequential-scan IO and disk seeks.
     */
    public TableStats(int tableid, int ioCostPerPage) {
        // For this function, you'll have to get the
        // DbFile for the table in question,
        // then scan through its tuples and calculate
        // the values that you need.
        // You should try to do this reasonably efficiently, but you don't
        // necessarily have to (for example) do everything
        // in a single scan of the table.
        // some code goes here
        table = (HeapFile)Database.getCatalog().getDbFile(tableid);
        this.ioCostPerPage = ioCostPerPage;
        attr = new HashMap<>();
        name2hist = new HashMap<>();
        td = table.getTupleDesc();
        Transaction transaction = new Transaction();
        DbFileIterator iter = table.iterator(transaction.getId());
        process(iter);
    }

    private void process(DbFileIterator iter){
        try {
            iter.open();
            //首先只处理INT_TYPE,获取到每一个字段的范围
            while (iter.hasNext()){
                ntups++;
                Tuple tuple = iter.next();
                for(int i=0;i<td.numFields();i++){
                    if(tuple.getField(i).getType()==Type.INT_TYPE){
                        String fieldName = td.getFieldName(i);
                        int val = ((IntField)tuple.getField(i)).getValue();
                        if(attr.containsKey(fieldName)){
                            Integer[] min_max = attr.get(fieldName);
                            if(val<min_max[0]){
                                min_max[0] = val;
                            }else if(val>min_max[1]){
                                min_max[1] = val;
                            }
                            attr.put(fieldName,min_max);
                        }else{
                            Integer[] min_max = {val,val};
                            attr.put(fieldName,min_max);
                        }
                    }
                }
            }
            //接下来我们需要处理的是为每一个整型字段建立直方图
            for (Map.Entry<String, Integer[]> stringEntry : attr.entrySet()) {
                String key = stringEntry.getKey();
                Integer[] min_max = stringEntry.getValue();
                IntHistogram intHistogram = new IntHistogram(NUM_HIST_BINS, min_max[0], min_max[1]);
                name2hist.put(key,intHistogram);
            }
            //接下来就是给每一个直方图放入值了
            iter.rewind();
            while (iter.hasNext()){
                Tuple tuple = iter.next();
                for(int i=0;i<td.numFields();i++){
                    String name = td.getFieldName(i);
                    if(td.getFieldType(i)==Type.INT_TYPE){
                        int val = ((IntField)tuple.getField(i)).getValue();
                        IntHistogram intHistogram = (IntHistogram) name2hist.get(name);
                        intHistogram.addValue(val);
                        name2hist.put(name,intHistogram);
                    }else{//说明是String类型
                        String val = ((StringField) tuple.getField(i)).getValue();
                        if(name2hist.containsKey(name)){
                            StringHistogram stringHistogram = (StringHistogram) name2hist.get(name);
                            stringHistogram.addValue(val);
                            name2hist.put(name,stringHistogram);
                        }else{
                            StringHistogram stringHistogram = new StringHistogram(NUM_HIST_BINS);
                            stringHistogram.addValue(val);
                            name2hist.put(name,stringHistogram);
                        }
                    }
                }
            }
        } catch (DbException e) {
            e.printStackTrace();
        } catch (TransactionAbortedException e) {
            e.printStackTrace();
        }
    }
    /**
     * Estimates the cost of sequentially scanning the file, given that the cost
     * to read a page is costPerPageIO. You can assume that there are no seeks
     * and that no pages are in the buffer pool.
     * 
     * Also, assume that your hard drive can only read entire pages at once, so
     * if the last page of the table only has one tuple on it, it's just as
     * expensive to read as a full page. (Most real hard drives can't
     * efficiently address regions smaller than a page at a time.)
     * 
     * @return The estimated cost of scanning the table.
     */
    public double estimateScanCost() {
        // some code goes here
        return table.numPages()*ioCostPerPage;
    }

    /**
     * This method returns the number of tuples in the relation, given that a
     * predicate with selectivity selectivityFactor is applied.
     * 
     * @param selectivityFactor
     *            The selectivity of any predicates over the table
     * @return The estimated cardinality of the scan with the specified
     *         selectivityFactor
     */
    public int estimateTableCardinality(double selectivityFactor) {
        // some code goes here
        return (int) Math.ceil(totalTuples() * selectivityFactor);
    }

    /**
     * The average selectivity of the field under op.
     * @param field
     *        the index of the field
     * @param op
     *        the operator in the predicate
     * The semantic of the method is that, given the table, and then given a
     * tuple, of which we do not know the value of the field, return the
     * expected selectivity. You may estimate this value from the histograms.
     * */
    public double avgSelectivity(int field, Predicate.Op op) {
        // some code goes here
        return 1.0;
    }

    /**
     * Estimate the selectivity of predicate <tt>field op constant</tt> on the
     * table.
     * 
     * @param field
     *            The field over which the predicate ranges
     * @param op
     *            The logical operation in the predicate
     * @param constant
     *            The value against which the field is compared
     * @return The estimated selectivity (fraction of tuples that satisfy) the
     *         predicate
     */
    public double estimateSelectivity(int field, Predicate.Op op, Field constant) {
        // some code goes here
        String fieldName = td.getFieldName(field);
        if (constant.getType() == Type.INT_TYPE) {
            int value = ((IntField)constant).getValue();
            IntHistogram histogram = (IntHistogram) name2hist.get(fieldName);
            return histogram.estimateSelectivity(op, value);
        } else {
            String value = ((StringField)constant).getValue();
            StringHistogram histogram = (StringHistogram)name2hist.get(fieldName);
            return histogram.estimateSelectivity(op, value);
        }
    }

    /**
     * return the total number of tuples in this table
     * */
    public int totalTuples() {
        // some code goes here
        return ntups;
    }

}
