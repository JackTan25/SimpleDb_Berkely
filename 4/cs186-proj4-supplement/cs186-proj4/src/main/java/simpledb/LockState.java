package simpledb;

/**
 * 该类封装事务和锁的类型
 */
public class LockState {
    private Permissions perm;
    private TransactionId transId;

    public Permissions getPerm() {
        return perm;
    }

    public void setPerm(Permissions perm) {
        this.perm = perm;
    }

    public TransactionId getTransId() {
        return transId;
    }

    public void setTransId(TransactionId transId) {
        this.transId = transId;
    }

    public LockState(Permissions perm, TransactionId transId){
        this.perm = perm;
        this.transId = transId;
    }
}
