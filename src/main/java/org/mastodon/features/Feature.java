package org.mastodon.features;

import org.mastodon.collection.RefCollection;
import org.mastodon.features.FeatureRegistry.DuplicateKeyException;

/**
 * TODO
 *
 * @param <M>
 *            usually a map from O to something.
 * @param <O>
 *            type of object to which feature should be attached.
 * @param <F>
 *            represents a feature value (usually wraps the value type of M).
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public abstract class Feature< M, O, F extends FeatureValue< ? > >
{
	private final String key;

	/**
	 * Unique ID. These IDs are generated by FeatureRegistry, starting from 0.
	 * As long as there are not excessively many features, the ID can be used as
	 * an index to look up features in a list instead of a map.
	 */
	private final int id;

	protected Feature( final String key ) throws DuplicateKeyException
	{
		this.key = key;
		this.id = FeatureRegistry.getUniqueFeatureId( key );
		FeatureRegistry.registerFeature( this );
	}

	public String getKey()
	{
		return key;
	}

	/*
	 * Following part is for the graph to create feature maps, initialize
	 * features, serialize, etc...
	 */

	protected abstract M createFeatureMap( final RefCollection< O > pool );

	public abstract F createFeatureValue( O object, Features< O > features );

	protected abstract FeatureCleanup< O > createFeatureCleanup( M featureMap );

	public abstract UndoFeatureMap< O > createUndoFeatureMap( M featureMap );

	public int getUniqueFeatureId()
	{
		return id;
	}

	protected static class NotifyValueChange< O > implements NotifyFeatureValueChange
	{
		private final Features< O > features;

		private final Feature< ?, O, ? > feature;

		private final O object;

		public NotifyValueChange( final Features< O > features, final Feature< ?, O, ? > feature, final O object )
		{
			this.features = features;
			this.feature = feature;
			this.object = object;
		}

		@Override
		public void notifyBeforeFeatureChange()
		{
			features.notifyBeforeFeatureChange( feature, object );
		}
	}

	@Override
	public int hashCode()
	{
		return id;
	}

	@Override
	public boolean equals( final Object obj )
	{
		return obj instanceof Feature
				&& ( ( Feature< ?, ?, ? > ) obj ).key.equals( key );
	}

	@Override
	public String toString()
	{
		return getClass().getName() + "(\"" + key + "\")";
	}
}