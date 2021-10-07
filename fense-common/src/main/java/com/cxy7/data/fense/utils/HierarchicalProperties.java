package com.cxy7.data.fense.utils;

import java.io.*;
import java.util.*;

/**
 * @author: XiaoYu
 * @date: 2021/3/13 11:59 上午
 */
public class HierarchicalProperties extends Properties {

    Properties delegator;
    Properties parent;

    public HierarchicalProperties(Properties delegator, Properties parent) {
        this.delegator = delegator;
        this.parent = parent;
    }

    public Properties getDelegator() {
        return delegator;
    }

    public void setDelegator(Properties self) {
        this.delegator = self;
    }

    public Properties getParent() {
        return parent;
    }

    public void setParent(Properties parent) {
        this.parent = parent;
    }

    @Override
    public synchronized void clear() {
        delegator.clear();
    }

    @Override
    public synchronized Object clone() {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized boolean contains(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized boolean containsKey(Object key) {
        return delegator.containsKey(key) || parent.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized Enumeration<Object> elements() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Map.Entry<Object, Object>> entrySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized boolean equals(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized Object get(Object key) {
        Object v = delegator.get(key);
        if (v == null) {
            v = parent.get(key);
        }
        return v;
    }

    @Override
    public String getProperty(String key) {
        String v = delegator.getProperty(key);
        if (v == null) {
            v = parent.getProperty(key);
        }
        return v;
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        String v = this.getProperty(key);
        if (v == null) {
            v = defaultValue;
        }
        return v;
    }

    @Override
    public synchronized int hashCode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized boolean isEmpty() {
        return delegator.isEmpty() && parent.isEmpty();
    }

    @Override
    public synchronized Enumeration<Object> keys() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Object> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void list(PrintStream out) {
        delegator.list(out);
    }

    @Override
    public void list(PrintWriter out) {
        delegator.list(out);
    }

    @Override
    public synchronized void load(InputStream inStream) throws IOException {
        delegator.load(inStream);
    }

    @Override
    public synchronized void load(Reader reader) throws IOException {
        delegator.load(reader);
    }

    @Override
    public synchronized void loadFromXML(InputStream in) throws IOException {
        delegator.loadFromXML(in);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Enumeration<?> propertyNames() {
        Enumeration<?> selfEnumeration = delegator.propertyNames();
        Enumeration<?> childEnumeration = parent.propertyNames();
        Set names = new HashSet();
        while (selfEnumeration.hasMoreElements()) {
            names.add(selfEnumeration.nextElement());
        }
        while (childEnumeration.hasMoreElements()) {
            names.add(childEnumeration.nextElement());
        }
        final Iterator i = names.iterator();
        return new Enumeration() {
            public boolean hasMoreElements() {
                return i.hasNext();
            }

            public Object nextElement() {
                return i.next();
            }
        };
    }

    @Override
    public synchronized Object put(Object key, Object value) {
        return delegator.put(key, value);
    }

    @Override
    public synchronized void putAll(Map<? extends Object, ? extends Object> t) {
        delegator.putAll(t);
    }

    @Override
    public synchronized Object remove(Object key) {
        return delegator.remove(key);
    }

    @Override
    public synchronized Object setProperty(String key, String value) {
        return delegator.setProperty(key, value);
    }

    @Override
    public synchronized int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public void save(OutputStream out, String comments) {
        delegator.save(out, comments);
    }

    @Override
    public void store(OutputStream out, String comments) throws IOException {
        delegator.store(out, comments);
    }

    @Override
    public void store(Writer writer, String comments) throws IOException {
        delegator.store(writer, comments);
    }

    @Override
    public void storeToXML(OutputStream os, String comment) throws IOException {
        delegator.storeToXML(os, comment);
    }

    @Override
    public void storeToXML(OutputStream os, String comment, String encoding)
            throws IOException {
        delegator.storeToXML(os, comment, encoding);
    }

    @Override
    public Set<String> stringPropertyNames() {
        Set<String> selfEnumeration = delegator.stringPropertyNames();
        Set<String> childEnumeration = parent.stringPropertyNames();
        Set<String> names = new HashSet<String>();
        names.addAll(selfEnumeration);
        names.addAll(childEnumeration);
        return names;
    }

    @Override
    public synchronized String toString() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Object> values() {
        throw new UnsupportedOperationException();
    }
}
