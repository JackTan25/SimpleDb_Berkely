package simpledb;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;

/**
 * Query is a wrapper class to manage the execution of queries. It takes a query
 * plan in the form of a high level DbIterator (built by initiating the
 * constructors of query plans) and runs it as a part of a specified
 * transaction.
 * 
 * @author Sam Madden
 */

public class Query implements Serializable {

    private static final long serialVersionUID = 1L;

    transient private DbIterator op;
    transient private LogicalPlan logicalPlan;
    TransactionId tid;
    transient private boolean started = false;

    public TransactionId getTransactionId() {
        return this.tid;
    }

    public void setLogicalPlan(LogicalPlan lp) {
        this.logicalPlan = lp;
    }

    public LogicalPlan getLogicalPlan() {
        return this.logicalPlan;
    }

    public void setPhysicalPlan(DbIterator pp) {
        this.op = pp;
    }

    public DbIterator getPhysicalPlan() {
        return this.op;
    }

    public Query(TransactionId t) {
        tid = t;
    }

    public Query(DbIterator root, TransactionId t) {
        op = root;
        tid = t;
    }

    public void start() throws IOException, DbException,
            TransactionAbortedException {
        op.open();

        started = true;
    }

    public TupleDesc getOutputTupleDesc() {
        return this.op.getTupleDesc();
    }

    /** @return true if there are more tuples remaining. */
    public boolean hasNext() throws DbException, TransactionAbortedException {
        return op.hasNext();
    }

    /**
     * Returns the next tuple, or throws NoSuchElementException if the iterator
     * is closed.
     * 
     * @return The next tuple in the iterator
     * @throws DbException
     *             If there is an error in the database system
     * @throws NoSuchElementException
     *             If the iterator has finished iterating
     * @throws TransactionAbortedException
     *             If the transaction is aborted (e.g., due to a deadlock)
     */
    public Tuple next() throws DbException, NoSuchElementException,
            TransactionAbortedException {
        if (!started)
            throw new DbException("Database not started.");

        return op.next();
    }

    /** Close the iterator */
    public void close() throws IOException {
        op.close();
        started = false;
    }

    public void execute() throws IOException, DbException, TransactionAbortedException {
        TupleDesc td = this.getOutputTupleDesc();
        ArrayList<String> list = new ArrayList<>();
        ArrayList<ArrayList<String>> datas = new ArrayList<>();
        String names = "";
        for (int i = 0; i < td.numFields(); i++) {
            names += td.getFieldName(i) + "\t";list.add(td.getFieldName(i));
        }
        // 表头（列名）
        String[] colNames = list.toArray(new String[list.size()]);
        System.out.println(names);
        for (int i = 0; i < names.length() + td.numFields() * 4; i++) {
            System.out.print("-");
        }
        System.out.println("");

        this.start();
        int cnt = 0;
        while (this.hasNext()) {
            Tuple tup = this.next();
            ArrayList<String> list1 = reverse_Tuple(tup);
            datas.add(list1);
            System.out.println(tup);
            cnt++;
        }
        String[][] rowData = new String[datas.size()][list.size()];
        for (int i=0;i<datas.size();i++){
            for(int j=0;j<list.size();j++){
                rowData[i][j] = datas.get(i).get(j);
            }
        }
        SimpleView.table = new JTable(rowData,colNames);
        // 设置表格内容颜色
        SimpleView.table.setForeground(Color.BLACK);                   // 字体颜色
        SimpleView.table.setFont(new Font(null, Font.PLAIN, 14));      // 字体样式
        SimpleView.table.setSelectionForeground(Color.DARK_GRAY);      // 选中后字体颜色
        SimpleView.table.setSelectionBackground(Color.LIGHT_GRAY);     // 选中后字体背景
        SimpleView.table.setGridColor(Color.GRAY);                     // 网格颜色

        // 设置表头
        SimpleView.table.getTableHeader().setFont(new Font(null, Font.BOLD, 14));  // 设置表头名称字体样式
        SimpleView.table.getTableHeader().setForeground(Color.RED);                // 设置表头名称字体颜色
        SimpleView.table.getTableHeader().setResizingAllowed(false);               // 设置不允许手动改变列宽
        SimpleView.table.getTableHeader().setReorderingAllowed(false);             // 设置不允许拖动重新排序各列

        // 设置行高
        SimpleView.table.setRowHeight(30);

        // 第一列列宽设置为40
        SimpleView.table.getColumnModel().getColumn(0).setPreferredWidth(40);

        System.out.println("\n " + cnt + " rows.");
        this.close();
    }
    public ArrayList<String> reverse_Tuple(Tuple tuple){
        TupleDesc td = this.getOutputTupleDesc();
        ArrayList<String> list = new ArrayList<>();
        for(int i=0;i<tuple.getTupleDesc().numFields();i++){
            list.add(tuple.getField(i).toString());
        }
        return list;
    }
}
