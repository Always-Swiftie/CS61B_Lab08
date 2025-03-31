package hashmap;

import java.security.Key;
import java.util.*;

/**
 *  A hash table-backed Map implementation.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {
    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            this.key = k;
            this.value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    // You should probably define some more!
    private int size;//哈希表中元素个数
    private double loadFactor;//负载因子，默认为0.75
    private int threshold;//阈值
    /** Constructors */
    @SuppressWarnings("unchecked")
    public MyHashMap() { //无参构造，初始桶容量为16
        this.buckets=(Collection<Node>[]) new Collection[16];
        this.loadFactor=0.75;
        //需要初始化数组里面的每一个桶
        for(int i=0;i<16;i++){
            buckets[i]=createBucket();
        }
    }
    @SuppressWarnings("unchecked")
    public MyHashMap(int initialCapacity) {
        this.buckets=(Collection<Node>[]) new Collection[initialCapacity];
        this.loadFactor=0.75;
        for(int i=0;i<initialCapacity;i++){
            buckets[i]=createBucket();
        }
    }
    /**
     * MyHashMap constructor that creates a backing array of initialCapacity.
//     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialCapacity initial size of backing array
     * @param loadFactor maximum load factor
     */
    @SuppressWarnings("unchecked")
    public MyHashMap(int initialCapacity, double loadFactor) {
        this.buckets=(Collection<Node>[]) new Collection[initialCapacity];
        this.loadFactor=loadFactor;
        this.threshold=(int)(loadFactor*buckets.length);
        for(int i=0;i<initialCapacity;i++){
            buckets[i]=createBucket();
        }
    }
    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *  Note that that this is referring to the hash table bucket itself,
     *  not the hash map itself.
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {//工厂方法，返回一个指定类型的桶
        // TODO: Fill in this method.
        //ver.1 using LinkedList as bucket
        return new LinkedList<>();
    }

    private int hash(K key){//哈希方法-根据key值获取哈希值
        if(key instanceof String str){//如果key是String类型
            return str.hashCode()^(str.hashCode()>>>16);
        }
        return key.hashCode()^(key.hashCode()>>>16);
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!
    @Override
    public void put(K key, V value) {
        //首先得到当前键值对的哈希值，找到放的桶索引
        int index=hash(key)&(buckets.length-1);
        /*
        case1:index处桶为empty-直接创建新桶，塞入当前键值对
        case2:index处桶不为empty-如果当前桶中key已经存在————update value/如果key不存在————add
         */
        if(buckets[index].isEmpty()){//case1:
            buckets[index].add(new Node(key,value));
            size++;
        }else{//case2:
            boolean isFound=false;
            for (Node p:buckets[index]){
                if(p.key.equals(key)){//update
                    p.value=value;
                    isFound=true;
                    break;
                }
            }
            //走到这里，说明表中没有相同的key，直接添加节点
            if(!isFound){//add
                buckets[index].add(new Node(key,value));
                size++;
            }
        }
        //检查是否超过阈值
        if(!check()){//要执行resize操作
            resize();
        }
    }

    @Override
    public V get(K key) {
        //case1:得到的hashcode在buckets数组中没有
        int hashcode=hash(key);
        int index=hashcode&(buckets.length-1);
        if(buckets[index]==null){
            return null;
        }
        if(buckets[index].isEmpty()){
            return null;
        }
        for(Node p:buckets[index]){
            if(p.key.equals(key)){
                return p.value;
            }
        }
        return null;
    }

    @Override
    public boolean containsKey(K key) {
        int hashcode=hash(key);
        int index=hashcode&(buckets.length-1);
        if(buckets[index].isEmpty()) {
            return false;
        }
        for(Node p:buckets[index]){
            if(p.key.equals(key)){
                return true;
            }
        }
        return false;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public void clear() {
            for(int i=0;i< buckets.length;i++){
                buckets[i]=createBucket();
            }
            this.size=0;
            updateThreshold();
    }

    @Override
    public Set<K> keySet() {//返回一个集合，包含这个Hashmap中包含的所有key
        //遍历整个桶数组，如果当前索引位置为空,就跳过
        //如果当前索引位置不为空，进入链表中把每个key值记录到set中里面
        Set<K> keys=new HashSet<>();
        for(int i=0;i< buckets.length;i++){
            if(!buckets[i].isEmpty()){
                for(Node p:buckets[i]){
                    keys.add(p.key);
                }
            }
        }
        return keys;
    }
    @Override
    public V remove(K key) {
        //通过hashcode找到对应的节点，如果不存在返回null/存在就在链表中删除这个节点
        //如何调用remove方法？
        int hashcode=hash(key);
        int index=hashcode&(buckets.length-1);
        //case1:
        if(buckets[index]==null){
            return null;//not found!
        }
        for(Node p:buckets[index]){
            if(p.key.equals(key)){
                buckets[index].remove(p);//first attempt
                return p.value;
            }
        }
        return null;
    }
    @Override
    public Iterator<K> iterator() {
        return null;
    }
    private boolean check(){
        return this.size()<this.threshold;
    }
    private void updateThreshold(){
        this.threshold=(int)(loadFactor*buckets.length);
    }
    @SuppressWarnings("unchecked")
    private void resize(){
        //扩容机制--原数组长度翻倍
        Collection<Node>[] newBuckets=new Collection[this.buckets.length<<1];
        for(int i=0;i< newBuckets.length;i++){
            newBuckets[i]=createBucket();
        }
        //同一个表中扩容后会分出两组，一组是hashcode&buckets.length==0的，另一组是！=0的，前者一部分放在新桶的原来下标处即可，后者放在下标为原下标加上旧的桶长
        for(int i=0;i<buckets.length;i++){
            LinkedList<Node> a=new LinkedList<>();//第一组：hashcode对old的数组长度按位与==0的
            LinkedList<Node> b=new LinkedList<>();//第二组: hashcode对old的数组长度按位与！=0的
            for(Node p:buckets[i]){
                int hashcode=hash(p.key);
                if((hashcode&buckets.length)==0){
                    a.add(p);
                }else{
                    b.add(p);
                }
            }
            newBuckets[i]=a;
            newBuckets[i+buckets.length]=b;
        }
        buckets=newBuckets;
        updateThreshold();
    }
}
