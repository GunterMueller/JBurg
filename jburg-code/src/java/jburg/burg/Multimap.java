package jburg.burg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Multimap maps a list of objects to a Comparable key.
 * @author tharwood
 *
 * @param <code><V></code> - the class of the objects to be mapped.
 */
class Multimap<K extends Comparable<K>, V extends Object>
{
    private Map<K, List<V>> contents = new TreeMap<K, List<V>>();

    /**
     *  Add a value to its container.
     *  @param key - the container's key.
     *  @param value - the value.
     */
    public void addToSet(K key, V value)
    {
        getSet(key).add(value);
    }

    /**
     *  Add all values in a Collection to their container.
     *  @param key - the container's key.
     *  @param values - the Collection of values.
     */
    public void addAllToSet(K key, Collection<V> values)
    {
         getSet(key).addAll(values);
    }

    /**
     *  Fetch the container for the given key,
     *  creating it if necessary.
     *  @param key - the container's key.
     *  @return the container associated with the key.
     *  @see {@link #get(K)}, which delegates to this method.
     */
    public List<V> getSet(K key)
    {
        if ( !contents.containsKey(key))
            contents.put(key, new ArrayList<V>());
        return contents.get(key);
    }

    /**
     *  Expose {@link #getSet} with a more map-like API.
     */
    public List<V> get(K key)
    {
        return getSet(key);
    }

    /**
     *  Expose the underlying map's keySet() method.
     */
    public Set<K> keySet()
    {
        return contents.keySet();
    }

    /**
     *  Expose the underlying map's values() method.
     */
    public Collection<List<V>> values()
    {
        return contents.values();
    }

    /**
     *  Expose the underlying map's entrySet() method.
     */
    public Iterable<Map.Entry<K, List<V>>> entrySet()
    {
        return contents.entrySet();
    }

    /**
     *  Expose the underlying map's containsKey() method.
     *  @param key - the key of interest.
     */
    public boolean containsKey(K key)
    {
        return contents.containsKey(key);
    }

}
