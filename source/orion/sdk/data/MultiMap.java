package orion.sdk.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements a list-based map that allows key duplicates.
 * @author Tim
 * @since 1.0.00
 * 
 * @param <K>
 * 	The key type
 * @param <V>
 * 	The value type
 */
public class MultiMap<K, V>
{
	protected List<K> keys = new ArrayList<K>();
	protected List<V> values = new ArrayList<V>();

	public MultiMap() { }

	public MultiMap(List<K> keys, List<V> values) {
		this.keys = keys;
		this.values = values;
	}
	
	public void Add(K key, V value)
	{
		keys.add(key);
		values.add(value);
	}

	public void Insert(int index, K key, V value)
	{
		keys.add(index, key);
		values.add(index, value);
	}

	public void AddRange(MultiMap<K, V> range)
	{
		for (int i = 0; i < range.Count(); i++)
			Add(range.keys.get(i), range.values.get(i));
	}

	public void Remove(K key)
	{
		int index = keys.indexOf(key);
		keys.remove(index);
		values.remove(index);
	}

	public void RemoveAt(int index)
	{
		keys.remove(index);
		values.remove(index);
	}

	public void ChangeKey(K oldKey, K newKey)
	{
		for (int i = 0; i < Count(); i++)
			if (keys.get(i).equals(oldKey))
				{
					keys.set(i, newKey);				
				}
	}

	public V get(K key)
	{
		int index = keys.indexOf(key);
		return values.get(index);
	}

	public void set(K key, V value)
	{
		int index = keys.indexOf(key);
		values.set(index, value);
	}

	public K getKey(int index)
	{
		return keys.get(index);
	}

	public void setKey(int index, K key)
	{
		keys.set(index, key);
	}

	public V getValue(int index)
	{
		return values.get(index);
	}

	public void setValue(int index, V value)
	{
		values.set(index, value);
	}

	public void Clear()
	{
		keys.clear();
		values.clear();
	}

	/**
	 * Returns a list of all the values with the given key.
	 * @param key
	 * @return
	 */
	public List<V> ValuesOf(K key)
	{
		List<V> result = new ArrayList<V>();
		for (int i = 0; i < keys.size(); i++)
			if (keys.get(i).equals(key))
				result.add(values.get(i));
		return result;
	}

	public int Count()
	{
		return keys.size();
	}

	public boolean ContainsKey(K key)
	{
		return keys.contains(key);
	}

	public boolean ContainsValue(V value)
	{
		return values.contains(value);
	}
}

