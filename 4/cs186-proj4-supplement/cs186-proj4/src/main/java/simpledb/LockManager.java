package simpledb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
public class LockManager {
    //用于记录每一页上面锁的类型和对应事务
    private HashMap<PageId, List<LockState>> pageLocks;
    //用于记录当前阻塞的事务正在等待的页
    private HashMap<TransactionId,PageId> waitingInfo;
    public LockManager(){
        pageLocks = new HashMap<>();
        waitingInfo = new HashMap<>();
    }
    //=============申请锁,释放锁beging==================

    /**
     * 此方法判断事务transId事务是否可以在pageId这一页面上加上S锁
     *
     * @param transId
     * @param pageId
     * @return
     */
    public synchronized boolean grantSLock(TransactionId transId,PageId pageId){
        //如果pageId上没有锁，加上s锁后返回true
        List<LockState> lockStates = pageLocks.get(pageId);
        if(lockStates==null||lockStates.size()==0){
            return lock(transId,pageId,Permissions.READ_ONLY);
        }else if(lockStates.size() == 1){//只有一个锁的话
            LockState lockState = lockStates.get(0);
            //1.如果这个锁是transId的锁
            if(lockState.getTransId().equals(transId)){
                //是读锁直接返回true,是写锁就加上读锁再返回
                return lockState.getPerm()==Permissions.READ_ONLY?true:lock(transId,pageId,Permissions.READ_ONLY);
            }else{
                //不是transId的锁,如果是读锁，可以直接加上读锁返回true,否则返回需要等待
                return lockState.getPerm()==Permissions.READ_ONLY?lock(transId,pageId,Permissions.READ_ONLY):wait(transId,pageId);
            }
        }else{//多个锁的情况
            //1.两个锁(一读一写),都是transId的 2..两个锁(一读一写),不是transId的  -->有写锁的情况
            //3.多个锁(全是读锁),有transId的   4.多个锁(全是读锁),没有transId的   -->没有写锁的情况
            for (LockState lockState : lockStates) {
                if(lockState.getPerm()==Permissions.READ_WRITE){//对应情况1,2
                    if(lockState.getTransId().equals(transId)){
                        return true;
                    }else{//申请不到就需要等待
                        return wait(transId,pageId);
                    }
                }else if(lockState.getTransId().equals(transId)){//存在transId的读锁
                        return true;
                }
            }
            //到达这里说明没有写锁,全是读锁且没有transId的,加锁返回即可
            return lock(transId,pageId,Permissions.READ_ONLY);
        }
    }

    /**
     * 该方法判断transId是否可以给pageId对应页面加上写锁(X锁)
     * @param pageId
     * @param perm
     * @return
     */
    public synchronized boolean grantXLock(TransactionId transId,PageId pageId){
        List<LockState> lockStates = pageLocks.get(pageId);
        if(lockStates==null||lockStates.size()==0){//没有锁可以直接加
            return lock(transId,pageId,Permissions.READ_WRITE);
        }else{
            //一个锁的情况
            if(lockStates.size()==1){
                LockState lockState = lockStates.get(0);
                if(lockState.getTransId().equals(transId)){//如果是自己的锁
                    return lockState.getPerm()==Permissions.READ_WRITE?true:lock(transId,pageId,Permissions.READ_WRITE);
                }else {//不是自己的锁就需要等待
                    return wait(transId,pageId);
                }
            }else {//多个锁的情况
                //1.两个锁(一读一写),都是transId的 2..两个锁(一读一写),不是transId的  -->有写锁的情况
                //3.多个锁(全是读锁)                                                 -->没有写锁的情况
                for (LockState lockState : lockStates) {
                    if(lockState.getPerm()==Permissions.READ_WRITE){
                        if(lockState.getTransId().equals(transId)){//是transId的锁
                            return true;
                        }
                    }
                }
                return wait(transId,pageId);
            }
        }
    }

    public synchronized boolean lock(TransactionId transId,PageId pageId,Permissions perm){
        List<LockState> lockStates = pageLocks.get(pageId);
        if(lockStates==null){
            lockStates = new ArrayList<>();
        }
        LockState lockState = new LockState(perm, transId);
        lockStates.add(lockState);
        pageLocks.put(pageId,lockStates);
        //加锁之后需要注意获得锁说明当前事务已经可以继续执行不需要等待资源了
        waitingInfo.remove(transId);
        return true;
    }

    public synchronized boolean wait(TransactionId transId,PageId pageId){
        waitingInfo.put(transId,pageId);
        return false;
    }

    /**
     * 释放transId对pageId的锁
     * @param transId
     * @param pageId
     */
    public synchronized void unLock(TransactionId transId,PageId pageId){
        List<LockState> lockStates = pageLocks.get(pageId);
        if(lockStates==null||lockStates.size()==0){
            return;
        }
        for (int i=0;i<lockStates.size();i++){
            LockState lockState = lockStates.get(i);
            if(lockState.getTransId().equals(transId)){
                lockStates.remove(i);
            }
        }
        pageLocks.put(pageId,lockStates);
    }

    /**
     * 释放transId上的所有锁
     * @param transId
     */
    public synchronized void releaseAllLocks(TransactionId transId){
        List<PageId> pages = getAllPagesByTid(transId);
        for (PageId page : pages) {
            unLock(transId,page);
        }
    }

    /**
     * 获取transId加锁的所有页面
     * @param transId
     * @return
     */
    public synchronized List<PageId> getAllPagesByTid(TransactionId transId){
        List<PageId> lists = new ArrayList<>();
        for (Map.Entry<PageId, List<LockState>> pageIdListEntry : pageLocks.entrySet()) {
            List<LockState> value = pageIdListEntry.getValue();
            for (LockState lockState : value) {
                if(lockState.getTransId().equals(transId)){
                    lists.add(pageIdListEntry.getKey());break;
                }
            }
        }
        return lists;
    }
    //=============申请锁,释放锁ending==================

    //=============死锁的检测===========================

    /**
     * 判断死锁的原理就是看当前transId申请的pageId上面的锁的主人是否也在申请transId所拥有的资源而造成死锁
     * @param transId
     * @param pageId
     * @return
     */
    public synchronized boolean deadLock(TransactionId transId,PageId pageId){
        //1.获取pageId上的所有事务
        List<LockState> lockStates = pageLocks.get(pageId);
        //2.获取transId所拥有的所有页
        List<PageId> pages = getAllPagesByTid(transId);
        for (LockState lockState : lockStates) {
            TransactionId holder = lockState.getTransId();
            //看holder是否也在等待pages中的页,不过我们要排除掉transId本身防止误判，因为可能transId在其他页上有读锁
            if(!holder.equals(transId)){
                boolean waiting = isWaitingResources(holder,pages,transId);
                if(waiting){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断transId是否在等待pids上的资源，toRemove传入这个参数是为了防止误判
     * @param transId
     * @param pids
     * @param toRemove
     * @return
     */
    public synchronized boolean isWaitingResources(TransactionId transId,List<PageId> pids,TransactionId toRemove){
        PageId pageId = waitingInfo.get(transId);
        if(pids==null|| pids.size()==0){
            return false;
        }
        //是否直接依赖
        for (PageId pid : pids) {
            if(pid.equals(pageId)){
                return true;
            }
        }
        //间接依赖
        List<LockState> lockStates = pageLocks.get(pageId);
        if(lockStates==null||lockStates.size()==0){
            return false;
        }
        for (LockState lockState : lockStates) {
            TransactionId holder = lockState.getTransId();
            if(!holder.equals(toRemove)){
                boolean waiting = isWaitingResources(holder,pids,toRemove);
                if(waiting){
                    return true;
                }
            }
        }
        return false;
    }
    //=================================================
}
