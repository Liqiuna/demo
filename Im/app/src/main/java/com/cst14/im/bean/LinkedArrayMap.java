package com.cst14.im.bean;

import android.support.v4.util.ArrayMap;

import java.util.LinkedList;

/**
 * LinkedArrayMap can store key and value orderly, can get value by key or location
 * All you need is to keep key unique, or will cause missing data
 * All optional operations including adding, removing, and replacing elements are supported.
 */
public class LinkedArrayMap<K, V> {

    private LinkedList<K> kList;
    private ArrayMap<K, V> vMap;

    public LinkedArrayMap() {
        kList = new LinkedList<>();
        vMap = new ArrayMap<>();
    }

    public void evictIfOverCount(int count) {
        while (kList.size() > count && vMap.size() > count) {
            removeFirst();
        }
    }

    public void addAll(LinkedArrayMap<K, V> map) {
        for (K key : map.kList) {
            kList.add(key);
            vMap.put(key, map.vMap.get(key));
        }
        map.clear();
    }

    public void addFirst(LinkedArrayMap<K, V> map) {
        int i = 0;
        for (K key : map.kList) {
            add(i, key, map.vMap.get(key));
            i++;
        }
    }

    public void add(int location, K key, V value) {
        kList.add(location, key);
        vMap.put(key, value);
    }

    public boolean add(K key, V value) {
        boolean ok = kList.add(key);
        vMap.put(key, value);
        return ok;
    }

    public void addFirst(K key, V value) {
        kList.addFirst(key);
        vMap.put(key, value);
    }

    public void addLast(K key, V value) {
        kList.addLast(key);
        vMap.put(key, value);
    }

    public V remove(int location) {
        K key = kList.remove(location);
        return vMap.remove(key);
    }

    public boolean remove(K key) {
        boolean ok = kList.remove(key);
        vMap.remove(key);
        return ok;
    }

    public V remove() {
        return removeFirst();
    }

    public V removeFirst() {
        K key = kList.removeFirst();
        return vMap.remove(key);
    }

    public V removeLast() {
        K key = kList.removeLast();
        return vMap.remove(key);
    }

    public V get(int location) {
        K key = kList.get(location);
        return vMap.get(key);
    }

    public V get(K key) {
        return vMap.get(key);
    }

    public V getFirst() {
        K key = kList.getFirst();
        return vMap.get(key);
    }

    public V getLast() {
        K key = kList.getLast();
        return vMap.get(key);
    }

    public boolean contains(K key) {
        return vMap.get(key) != null;
    }

    public int indexOf(K key) {
        return kList.indexOf(key);
    }

    public int size() {
        return vMap.size();
    }

    public void clear() {
        kList.clear();
        vMap.clear();
    }
}
