package com.colaorange.commons.util;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
/**
 * 
 * @author Dennis Chen
 *
 */
public class Jsons {

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface GetterProperty {
		//the property name
		String value();
	}
	
	public interface GetterPropertyJson {
	}
	
	private static class GetterPropertyJsonSerializer implements
			JsonSerializer<GetterPropertyJson> {
		public JsonElement serialize(GetterPropertyJson src, Type typeOfSrc,
				JsonSerializationContext context) {
			return toGetterPropertyJsonElement(src);
		}
	}
	
	//not work on interface
	/**
	 * The js function support to render js function, 
	 * Note : the extended class has to be public
	 */
	public interface JsFunction {
		public String getBody();
	}
	
	public static class DefaultJsFunction implements JsFunction{
		StringBuilder body;
		public DefaultJsFunction(){
			body = new StringBuilder();
		}
		public DefaultJsFunction(String body){
			this.body = new StringBuilder(body);
		}
		@Override
		public String getBody() {
			return body.toString();
		}
		public DefaultJsFunction append(String str){
			body.append(str);
			return this;
		}
	}
	
	
	private static ThreadLocal<Map<String,JsFunction>> jsFunctionMap = new ThreadLocal<Map<String,JsFunction>>();
	
	private static String registerJsFunction(JsFunction fn){
		Map<String,JsFunction> m = jsFunctionMap.get();
		if(m==null){
			jsFunctionMap.set(m = new LinkedHashMap<String, Jsons.JsFunction>());
		}
		//id with magic key
		String id = Strings.format("zs_jsfn_{}_{}",System.currentTimeMillis(),m.size());
		m.put(id, fn);
		return id;
	}
	
	private static Map<String,JsFunction> getJsFunctionMapping(){
		Map<String,JsFunction> m = jsFunctionMap.get(); 
		return m==null?null:java.util.Collections.unmodifiableMap(m);
	}
	
	private static void clearJsFunctionMapping(){
		jsFunctionMap.remove();
	}
	
	private static class JsFunctionTypeAdapter extends TypeAdapter<JsFunction> {
		@Override
		public void write(JsonWriter out, JsFunction value) throws IOException {
			if (value == null) {
				out.nullValue();
				return;
			}
			String id = registerJsFunction(value);
			out.value(id);
		}

		@Override
		public JsFunction read(JsonReader in) throws IOException {
			//can't read it. if any json conatins function, it will get gson JsonSyntaxException
			return null;
		}
	}
	
	static final GsonBuilder gb = new GsonBuilder();
	static{
		gb.excludeFieldsWithoutExposeAnnotation();
		gb.registerTypeHierarchyAdapter(JsFunction.class, new JsFunctionTypeAdapter());
	}
	static public final Gson gson = gb.create();
	
	static{
		gb.registerTypeHierarchyAdapter(GetterPropertyJson.class, new GetterPropertyJsonSerializer());
	}
	static public final Gson getterGson = gb.create();
	
	static{
		gb.setPrettyPrinting();
	}
	static public final Gson preetyGson = gb.create(); 
	
	public static String toJson(Object obj) {
		return toJson(obj,false);
	}
	public static String toJson(Object obj,boolean pretty) {
		try{
			String json = pretty?preetyGson.toJson(obj):getterGson.toJson(obj);
			Map<String,JsFunction> fnm = getJsFunctionMapping();
			if(fnm!=null){
				for(Entry<String, JsFunction> entry:fnm.entrySet()){
					//should include string quotation ""
					String key = Strings.format("\"{}\"", entry.getKey());
					String replacement = Strings.format("function(){{}}",entry.getValue().getBody());
					json = json.replace(key, replacement);
				}
			}
			
			return json;
		}finally{
			clearJsFunctionMapping();
		}
	}
	
	public static JsonElement toJsonElement(Object obj) {
		return getterGson.toJsonTree(obj);
	}
	
	@SuppressWarnings("rawtypes")
	private static Map<Class,Map<String,Method>> getterPropertyMethodCache = Collections.newConcurrentMap();
	
	private static JsonElement toGetterPropertyJsonElement(GetterPropertyJson obj) {
		JsonObject jsonObj = (JsonObject)gson.toJsonTree(obj); // the original expose for field
		@SuppressWarnings("rawtypes")
		Class clz = obj.getClass();
		Map<String,Method> methodBound = getterPropertyMethodCache.get(clz);
		if(methodBound==null){
			synchronized(Jsons.class){
				methodBound = getterPropertyMethodCache.get(clz);
				if(methodBound==null){
					getterPropertyMethodCache.put(clz, methodBound = getGetterPropertyMethodBound(clz));
				}
			}
		}
		for(Entry<String, Method> bound:methodBound.entrySet()){
			try {
				Object val = bound.getValue().invoke(obj);
				if (val == null) {
					continue;
				}
				jsonObj.add(bound.getKey(),toJsonElement(val));
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		return jsonObj;
	}

	@SuppressWarnings("rawtypes")
	private static Map<String, Method> getGetterPropertyMethodBound(Class clz) {
		Map<String, Method> methodBound = new TreeMap<String, Method>();
		while (GetterPropertyJson.class.isAssignableFrom(clz)) {
			for (Method m : clz.getDeclaredMethods()) {
				GetterProperty anno = m.getAnnotation(GetterProperty.class);
				if (anno == null) {
					continue;
				}
				String nm = anno.value();
				if (methodBound.containsKey(nm)) {
					continue;
				}
				methodBound.put(nm, m);
			}
			clz = clz.getSuperclass();
		}
		return methodBound;
	}
	public static String format(String json) {
		JsonElement jelement = new JsonParser().parse(json);
		return preetyGson.toJson(jelement);
	}
	public static <T> T fromJson(String json,Class<T> clz) {
		return gson.fromJson(json, clz);
	}
	public static <T> T fromJson(String json,TypeToken<T> typeToken) {
		return gson.fromJson(json, typeToken.getType());
	}
	
	public static <T> T clone(Object obj,Class<T> clz) {
		return gson.fromJson(gson.toJson(obj), clz);
	}
	
	public static String escapeString(String label) {
		return label.replace("\"", "\\\"");
	}
	
	
}
