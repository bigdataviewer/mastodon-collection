package org.mastodon.properties;

import org.mastodon.RefPool;
import org.mastodon.collection.RefCollection;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefDoubleMap;
import org.mastodon.collection.ref.RefDoubleHashMap;
import org.mastodon.properties.undo.DoublePropertyUndoRedoStack;
import org.mastodon.properties.undo.PropertyUndoRedoStack;

public class DoublePropertyMap< O > extends AbstractPropertyMap< O, Double >
{
	private final RefDoubleMap< O > map;

	private final double noEntryValue;

	public DoublePropertyMap( final RefCollection< O > pool, final double noEntryValue )
	{
		map = RefCollections.createRefDoubleMap( pool, noEntryValue );
		this.noEntryValue = noEntryValue;
		tryRegisterPropertyMap( pool );
	}

	public DoublePropertyMap( final RefCollection< O > pool, final double noEntryValue, final int initialCapacity )
	{
		map = RefCollections.createRefDoubleMap( pool, noEntryValue, initialCapacity );
		this.noEntryValue = noEntryValue;
		tryRegisterPropertyMap( pool );
	}

	public DoublePropertyMap( final RefPool< O > pool, final double noEntryValue )
	{
		map = new RefDoubleHashMap<>( pool, noEntryValue );
		this.noEntryValue = noEntryValue;
		tryRegisterPropertyMap( pool );
	}

	public DoublePropertyMap( final RefPool< O > pool, final double noEntryValue, final int initialCapacity )
	{
		map = new RefDoubleHashMap<>( pool, noEntryValue, initialCapacity );
		this.noEntryValue = noEntryValue;
		tryRegisterPropertyMap( pool );
	}

	public double set( final O key, final double value )
	{
		notifyBeforePropertyChange( key );
		final double old = map.put( key, value );
		notifyPropertyChanged( key );
		return old;
	}

	@Override
	public Double set( final O key, final Double value )
	{
		notifyBeforePropertyChange( key );
		final double old = map.put( key, value );
		notifyPropertyChanged( key );
		return ( old == noEntryValue ) ? null : Double.valueOf( old );
	}

	@Override
	public Double remove( final O key )
	{
		notifyBeforePropertyChange( key );
		final double old = map.remove( key );
		notifyPropertyChanged( key );
		return ( old == noEntryValue ) ? null : Double.valueOf( old );
	}

	public double removeDouble( final O key )
	{
		notifyBeforePropertyChange( key );
		final double old = map.remove( key );
		notifyPropertyChanged( key );
		return old;
	}

	public double getDouble( final O key )
	{
		return map.get( key );
	}

	@Override
	public Double get( final O key )
	{
		return map.get( key );
	}

	@Override
	public boolean isSet( final O key )
	{
		return map.containsKey( key );
	}

	@Override
	public int size()
	{
		return map.size();
	}

	public RefDoubleMap< O > getMap()
	{
		return map;
	}

	public void release()
	{
		map.clear();
		tryUnregisterPropertyMap();
	}

	public double getNoEntryValue()
	{
		return noEntryValue;
	}

	@Override
	public void beforeDeleteObject( final O key )
	{
		map.remove( key );
	}

	@Override
	public PropertyUndoRedoStack< O > createUndoRedoStack()
	{
		return new DoublePropertyUndoRedoStack<>( this );
	}

	@Override
	public void beforeClearPool()
	{
		map.clear();
	}

	@Override
	public void clear()
	{
		throw new UnsupportedOperationException( "TODO" );
	}
}
