package com.colaorange.commons.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @author Dennis Chen
 *
 */
public class Collections {

	@SafeVarargs
	public static <T> Set<T> asSet(T... values){
		if(values==null)
			return null;
		Set<T> set = new LinkedHashSet<T>();
		for(T obj:values){
			set.add(obj);
		}
		return set;
	}
	
	@SafeVarargs
	public static <T> List<T> asList(T... values){
		if(values==null)
			return null;
		List<T> list = new ArrayList<T>(values.length);
		for(T obj:values){
			list.add(obj);
		}
		return list;
	}
	
	@SafeVarargs
	public static <T> T[] asArray(T... values){
		return values;
	}
	
	public static <T> Set<T> arrayAsSet(T[] values){
		if(values==null)
			return null;
		Set<T> set = new LinkedHashSet<T>();
		for(T obj:values){
			set.add(obj);
		}
		return set;
	}
	
	public static <T> List<T> arrayAsList(T[] values){
		if(values==null)
			return null;
		List<T> list = new ArrayList<T>(values.length);
		for(T obj:values){
			list.add(obj);
		}
		return list;
	}	

	public static <T> T getOrNull(List<T> list, int i) {
		return list==null || list.size()<i ? null:list.get(i);
	}

	
	public static <T> Set<T> newConcurrentSet(){
		return java.util.Collections.newSetFromMap(new ConcurrentHashMap<T, Boolean>());
	}
	public static <T> Set<T> newConcurrentSet(int initialCapacity){
		return java.util.Collections.newSetFromMap(new ConcurrentHashMap<T, Boolean>(initialCapacity));
	}
	public static <T> Set<T> newConcurrentSet(Set<T> set){
		ConcurrentHashMap<T,Boolean> m = new ConcurrentHashMap<T, Boolean>(set.size());
		for(T s:set){
			m.put(s, Boolean.TRUE);
		}
		return java.util.Collections.newSetFromMap(m);
	}
	public static <K,T> Map<K,T> newConcurrentMap(){
		return new ConcurrentHashMap<K,T>();
	}
	public static <K,T> Map<K,T> newConcurrentMap(int initialCapacity){
		return new ConcurrentHashMap<K,T>(initialCapacity);
	}
	public static <K,T> Map<K,T> newConcurrentMap(Map<K,T> map){
		return new ConcurrentHashMap<K,T>(map);
	}
}
