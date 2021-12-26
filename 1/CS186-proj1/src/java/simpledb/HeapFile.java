package simpledb;
import java.io.*;
import java.util.*;

/**
 * HeapFile is an implementation of a DbFile that stores a collection of tuples
 * in no particular order. Tuples are stored on pages, each of which is a fixed
 * size, and the file is simply a collection of those pages. HeapFile works
 * closely with HeapPage. The format of HeapPages is described in the HeapPage
 * constructor.
 * 
 * @see simpledb.HeapPage#HeapPage
 * @author Sam Madden
 */
public class HeapFile implements DbFile {
    //1.用来存储表的属性名和类型
    private TupleDesc tupleDesc;
    //2.用来指明数据实际的存放位置
    private File file;
    //3.说明分页的数量
    private int numPage;
    /**
     * Constructs a heap file backed by the specified file.
     * 
     * @param f
     *            the file that stores the on-disk backing store for this heap
     *            file.
     */
    public HeapFile(File f, TupleDesc td) {
        // some code goes here
        file = f;
        tupleDesc = td;
        //这里一定可以整除，在后面分析ScanTest时会解释这个问题
        numPage = (int)(file.length()/BufferPool.PAGE_SIZE);
    }

    /**
     * Returns the File backing this HeapFile on disk.
     * 
     * @return the File backing this HeapFile on disk.
     */
    public File getFile() {
        // some code goes here
        return file;
    }

    /**
     * Returns an ID uniquely identifying this HeapFile. Implementation note:
     * you will need to generate this tableid somewhere ensure that each
     * HeapFile has a "unique id," and that you always return the same value for
     * a particular HeapFile. We suggest hashing the absolute file name of the
     * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
     * 
     * @return an ID uniquely identifying this HeapFile.
     */
    @Override
    public int getId() {
        // some code goes here
        return file.getAbsoluteFile().hashCode();
    }

    /**
     * Returns the TupleDesc of the table stored in this DbFile.
     * 
     * @return TupleDesc of this DbFile.
     */
    @Override
    public TupleDesc getTupleDesc() {
        // some code goes here
        return tupleDesc;
    }

    // see DbFile.java for javadocs
    @Override
    public Page readPage(PageId pid) {
        // some code goes here
        Page page = null;
        //定义存储数据的data数组
        byte[] data = new byte[BufferPool.PAGE_SIZE];
        try {
            RandomAccessFile raf = new RandomAccessFile(getFile(),"r");
            int pgm = pid.pageNumber();
            //通过偏移量来锁定位置
            raf.seek(pgm*BufferPool.PAGE_SIZE);
            raf.read(data,0,data.length);
            page = new HeapPage((HeapPageId) pid,data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return page;
    }

    // see DbFile.java for javadocs
    @Override
    public void writePage(Page page) throws IOException {
        // some code goes here
        // not necessary for proj1
    }

    /**
     * Returns the number of pages in this HeapFile.
     */
    public int numPages() {
        // some code goes here
        return numPage;
    }

    // see DbFile.java for javadocs
    @Override
    public ArrayList<Page> insertTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for proj1
    }

    // see DbFile.java for javadocs
    @Override
    public Page deleteTuple(TransactionId tid, Tuple t) throws DbException,
            TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for proj1
    }

    // see DbFile.java for javadocs
    @Override
    public DbFileIterator iterator(TransactionId tid) {
        // some code goes here
        return new HeapFileIterator(tid);
    }
    public class HeapFileIterator implements DbFileIterator{
        //指定当前是在哪一页
        int pos;

        TransactionId transactionId;
        //存储当前页的tuples
        Iterator<Tuple> tuplesInPage;
        public HeapFileIterator(TransactionId transactionId){
            this.transactionId = transactionId;
        }
        public Iterator<Tuple> getTuplesInPage(HeapPageId pageId) throws TransactionAbortedException, DbException {
            HeapPage page = (HeapPage)Database.getBufferPool().getPage(transactionId,pageId,Permissions.READ_ONLY);
            return page.iterator();
        }
        @Override
        public void open() throws DbException, TransactionAbortedException {
            pos = 0;
            HeapPageId heapPageId = new HeapPageId(getId(), pos);
            //这里不应该直接去调用readPage来获取对应页，而应该通过BufferPool来读取
            tuplesInPage = getTuplesInPage(heapPageId);
        }

        @Override
        public boolean hasNext() throws DbException, TransactionAbortedException {
            //判断迭代器是否已经打开,为空说明已经关闭了
            if(tuplesInPage==null){
                return false;
            }else {
                //如果当前页的tuplesInPage还没遍历完就返回true
                if(tuplesInPage.hasNext()){
                    return true;
                }else{
                    //如果后面还有页面就进入下一页
                    if(pos<numPage-1){
                        pos++;
                        HeapPageId pageId = new HeapPageId(getId(), pos);
                        tuplesInPage = getTuplesInPage(pageId);
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public Tuple next() throws DbException, TransactionAbortedException, NoSuchElementException {
           if(hasNext()){
              return tuplesInPage.next();
           }else{
               throw new NoSuchElementException("Closed or No more tuples");
           }
        }

        @Override
        public void rewind() throws DbException, TransactionAbortedException {
            open();
        }

        @Override
        public void close() {
            pos = 0;
            tuplesInPage = null;
        }
    }
}

