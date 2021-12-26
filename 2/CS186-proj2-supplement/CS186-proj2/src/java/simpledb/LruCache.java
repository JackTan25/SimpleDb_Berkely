package simpledb;

import java.util.HashMap;
import java.util.Iterator;

public class LruCache<K,V> {
    Node head;
    Node tail;
    int capacity;
    HashMap<K,Node> mp;
    class Node{
        Node left;
        Node right;
        K key;
        V value;
        public Node(){

        }
        public Node(K k,V v){
            key = k;value = v;
        }
    }
    public LruCache(int capacity){
        if(capacity<=0){
            throw new IllegalArgumentException("容量必须为正数");
        }
        head = new Node();
        tail = new Node();
        head.right = tail;
        tail.left = head;
        mp = new HashMap<>();
        this.capacity = capacity;
    }
    private Node removeTail(){
        Node p = tail.left;
        tail.left = p.left;
        p.left.right = tail;
        mp.remove(p.key);
        return p;
    }
    private void change(Node p){
        Node l = p.left;
        Node r = p.right;
        l.right = r;
        r.left = l;
        linkFirst(p);
    }
    public boolean containsKey(K k){
        return mp.containsKey(k);
    }

    private void linkFirst(Node p){
        p.right = head.right;
        p.left = head;
        head.right.left = p;
        head.right = p;
    }//  如果已满需要删除结点的话需要返回删除的v，否则就返回null
    public V put(K key,V val){
        Node p = null;
        if(mp.size()==capacity){
            p = removeTail();
        }
        Node node =new Node(key,val);
        linkFirst(node);
        mp.put(key,node);
        return p==null?null:p.value;
    }//获取v需要靠率将对应的节点放到链表头步
    public V get(K key){
        Node p  = mp.get(key);
        if(p==null){
            return null;
        }else{
            change(p);
            return p.value;
        }
    }
    public Iterator<V> iterator(){
        return new LruCacheItr();
    }
    class LruCacheItr implements Iterator<V>{
        Node h = head;
        @Override
        public boolean hasNext() {
            return h.right != tail;
        }

        @Override
        public V next() {
            if(!hasNext()){
                return null;
            }else{
                h = h.right;
                return h.value;
            }
        }
    }
}