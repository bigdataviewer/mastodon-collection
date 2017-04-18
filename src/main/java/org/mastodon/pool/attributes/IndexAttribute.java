package org.mastodon.pool.attributes;

import org.mastodon.pool.AbstractAttribute;
import org.mastodon.pool.PoolObject;
import org.mastodon.pool.PoolObjectLayout.IndexField;

public class IndexAttribute< O extends PoolObject< O, ?, ? > >
	extends AbstractAttribute< O >
{
	private final int offset;

	public IndexAttribute( final IndexField layoutField )
	{
		this.offset = layoutField.getOffset();
	}

	public void setQuiet( final O key, final int value )
	{
		access( key ).putIndex( value, offset );
	}

	public void set( final O key, final int value )
	{
		notifyBeforePropertyChange( key );
		access( key ).putIndex( value, offset );
		notifyPropertyChanged( key );
	}

	public int get( final O key )
	{
		return access( key ).getIndex( offset );
	}
}