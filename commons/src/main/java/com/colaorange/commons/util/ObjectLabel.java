package com.colaorange.commons.util;

/**
 * Created by Dennis
 */
public class ObjectLabel<T> {
    T object;
    String label;

    public ObjectLabel(T object, String label) {
        this.object = object;
        this.label = label;
    }

    public T getObject() {
        return object;
    }

    public void setObject(T object) {
        this.object = object;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ObjectLabel<?> that = (ObjectLabel<?>) o;

        return object != null ? object.equals(that.object) : that.object == null;
    }

    @Override
    public int hashCode() {
        return object != null ? object.hashCode() : 0;
    }
}
