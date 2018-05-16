package com.colaorange.dailymoney.core.context;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Dennis
 */
public interface EventQueue {

    public void subscribe(EventListener l);

    public void unsubscribe(EventListener l);

    public void publish(Event event);

    public void publish(String name, Object data);


    public class Event {

        private String name;

        private Object data;

        private Map<String, Object> args;

        public Event(String name) {
            this(name, null, null);
        }

        public Event(String name, Object data) {
            this(name, data, null);
        }

        public Event(String name, Object data, Map<String, Object> args) {
            this.name = name;
            this.data = data;
            this.args = args;
        }

        public String getName() {
            return name;
        }

        public <D> D getData() {
            return (D)data;
        }

        public <D> D getArg(String arg) {
            return args == null ? null : (D) args.get(arg);
        }

        void setArgs(Map<String, Object> args) {
            this.args = args;
        }
    }

    public class EventBuilder {
        String name;
        Object data;
        Map<String, Object> args;

        public EventBuilder() {
        }

        public EventBuilder(String name) {
            this.name = name;
        }

        public EventBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public EventBuilder withData(Object data) {
            this.data = data;
            return this;
        }

        public EventBuilder withArg(String arg, Object value) {
            if (args == null) {
                args = new LinkedHashMap<>();
            }
            args.put(arg, value);
            return this;
        }

        public Event build() {
            return new Event(name, data, args);
        }

    }


    public interface EventListener {
        public void onEvent(Event event);
    }
}
