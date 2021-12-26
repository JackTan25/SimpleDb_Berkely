package simpledb;

/** A class to represent a fixed-width histogram over a single integer-based field.
 */
public class IntHistogram {
    //1.取值范围最小值
    private int max;
    //2.取值范围1最小值
    private int min;
    //3.分出多少个buckets
    private int buckets;
    //4.总共的元组数量
    private int ntups;
    //5.每个bucket的宽度
    private int width;
    //6.每个区间的合法元组数
    private int[] histogram;
    /**
     * Create a new IntHistogram.
     *
     * This IntHistogram should maintain a histogram of integer values that it receives.
     * It should split the histogram into "buckets" buckets.
     *
     * The values that are being histogrammed will be provided one-at-a-time through the "addValue()" function.
     *
     * Your implementation should use space and have execution time that are both
     * constant with respect to the number of values being histogrammed.  For example, you shouldn't
     * simply store every value that you see in a sorted list.
     *
     * @param buckets The number of buckets to split the input value into.
     * @param min The minimum integer value that will ever be passed to this class for histogramming
     * @param max The maximum integer value that will ever be passed to this class for histogramming
     */
    public IntHistogram(int buckets, int min, int max) {
    	// some code goes here
        this.buckets = buckets;
        this.min = min;
        this.max = max;
        this.width = (int)Math.ceil((double)(max-min+1)/buckets);
        this.ntups = 0;
        histogram = new int[buckets];
        for(int i=0;i<histogram.length;i++) {
            histogram[i] = 0;
        }
    }
    private int valToIndex(int val){
        if(val==max){
            return buckets-1;
        }else{
            return (val-min)/width;
        }
    }
    /**
     * Add a value to the set of values that you are keeping a histogram of.
     * @param v Value to add to the histogram
     */
    public void addValue(int v) {
    	// some code goes here
        int idx = valToIndex(v);
        histogram[idx]++;
        ntups++;
    }

    /**
     * Estimate the selectivity of a particular predicate and operand on this table.
     *
     * For example, if "op" is "GREATER_THAN" and "v" is 5,
     * return your estimate of the fraction of elements that are greater than 5.
     *
     * @param op Operator
     * @param v Value
     * @return Predicted selectivity of this particular operator and value
     */
    public double estimateSelectivity(Predicate.Op op, int v) {
        int height;
        int idx = valToIndex(v);
        int left = idx*width+min;
        int right = idx*width+min+width-1;
        switch (op){
            case EQUALS:
                if(v<min||v>max){
                    return 0.0;
                }else{
                    return histogram[idx]*1.0/width/ntups;
                }
            case GREATER_THAN:
                if(v>max){
                    return 0.0;
                }else if(v<min){
                    return 1.0;
                }else{
                    height = histogram[idx];
                    double  p1 = (double) height/ntups*(right-v)/width;
                    int allsAfter = 0;
                    for(int i=idx+1;i<buckets;i++){
                        allsAfter += histogram[i];
                    }
                    p1 += allsAfter*1.0/ntups;
                    return p1;
                }
            case LESS_THAN:
                if(v>max){
                    return 1.0;
                }else if(v<min){
                    return 0.0;
                }else{
                    double  p1 = (double) histogram[idx]/ntups*(v-left)/width;
                    int allsBefore = 0;
                    for(int i=idx-1;i>=0;i--){
                        allsBefore += histogram[i];
                    }
                    p1 += allsBefore*1.0/ntups;
                    return p1;
                }
            case LESS_THAN_OR_EQ:
                return estimateSelectivity(Predicate.Op.LESS_THAN, v) + estimateSelectivity(Predicate.Op.EQUALS, v);
            case GREATER_THAN_OR_EQ:
                return estimateSelectivity(Predicate.Op.GREATER_THAN, v) + estimateSelectivity(Predicate.Op.EQUALS, v);
            case LIKE:
                return avgSelectivity();
            case NOT_EQUALS:
                return 1 - estimateSelectivity(Predicate.Op.EQUALS, v);
            default:
                throw new RuntimeException("Should not reach hear");
        }
    }

    /**
     * @return
     *     the average selectivity of this histogram.
     *
     *     This is not an indispensable method to implement the basic
     *     join optimization. It may be needed if you want to
     *     implement a more efficient optimization
     * */
    public double avgSelectivity()
    {
        // some code goes here
        return 1.0;
    }

    /**
     * @return A string describing this histogram, for debugging purposes
     */
    public String toString() {

        // some code goes here
        return "[to be implemented]";
    }
}
