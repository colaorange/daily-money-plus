package com.colaorange.dailymoney.core.context;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Dennis
 */
public interface EventQueue {

    public void subscribe(EventListener<?> l);

    public void unsubscribe(EventListener<?> l);

    public void publish(Event<?> event);

    public void publish(String name, Object data);


    public class Event<T> {

        private String name;

        private T data;

        private Map<String, Object> args;

        public Event(String name) {
            this(name, null, null);
        }

        public Event(String name, T data) {
            this(name, data, null);
        }

        public Event(String name, T data, Map<String, Object> args) {
            this.name = name;
            this.data = data;
            this.args = args;
        }

        public String getName() {
            return name;
        }

        public T getData() {
            return data;
        }

        public <D> D getArg(String arg) {
            return args == null ? null : (D) args.get(arg);
        }

        void setArgs(Map<String, Object> args) {
            this.args = args;
        }
    }

    public class EventBuilder<T> {
        String name;
        T data;
        Map<String, Object> args;

        public EventBuilder() {
        }

        public EventBuilder(String name) {
            this.name = name;
        }

        public EventBuilder<T> withName(String name) {
            this.name = name;
            return this;
        }

        public EventBuilder<T> withData(T data) {
            this.data = data;
            return this;
        }

        public EventBuilder<T> withArg(String arg, Object value) {
            if (args == null) {
                args = new LinkedHashMap<>();
            }
            args.put(arg, value);
            return this;
        }

        public Event<T> build() {
            return new Event<T>(name, data, args);
        }

    }


    public interface EventListener<T> {
        public void onEvent(Event<T> event);
    }
}
