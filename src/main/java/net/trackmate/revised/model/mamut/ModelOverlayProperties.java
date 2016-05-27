package net.trackmate.revised.model.mamut;

import net.trackmate.revised.bdv.overlay.wrap.OverlayProperties;
import net.trackmate.revised.ui.selection.Selection;

/**
 * Provides spot {@link OverlayProperties properties} for BDV {@link OverlayGraph}.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class ModelOverlayProperties implements OverlayProperties< Spot, Link >
{
	private final ModelGraph modelGraph;

	private final BoundingSphereRadiusStatistics radiusStats;

	private final Selection< Spot, Link > selection;

	public ModelOverlayProperties(
			final ModelGraph modelGraph,
			final BoundingSphereRadiusStatistics radiusStats,
			final Selection< Spot, Link > selection )
	{
		this.modelGraph = modelGraph;
		this.radiusStats = radiusStats;
		this.selection = selection;
	}

	@Override
	public void localize( final Spot v, final double[] position )
	{
		v.localize( position );
	}

	@Override
	public double getDoublePosition( final Spot v, final int d )
	{
		return v.getDoublePosition( d );
	}

	@Override
	public void setPosition( final Spot v, final double position, final int d )
	{
		v.setPosition( position, d );
	}

	@Override
	public void setPosition( final Spot v, final double[] position )
	{
		v.setPosition( position );
	}

	@Override
	public void getCovariance( final Spot v, final double[][] mat )
	{
		v.getCovariance( mat );
	}

	@Override
	public void setCovariance( final Spot v, final double[][] mat )
	{
		v.setCovariance( mat );
	}

	@Override
	public double getBoundingSphereRadiusSquared( final Spot v )
	{
		return v.getBoundingSphereRadiusSquared();
	}

	@Override
	public int getTimepoint( final Spot v )
	{
		return v.getTimepoint();
	}

	@Override
	public boolean isVertexSelected( final Spot v )
	{
		return selection.isSelected( v );
	}

	@Override
	public boolean isEdgeSelected( final Link e )
	{
		return selection.isSelected( e );
	}

	@Override
	public double getMaxBoundingSphereRadiusSquared( final int timepoint )
	{
		radiusStats.readLock().lock();
		try
		{
			return radiusStats.getMaxBoundingSphereRadiusSquared( timepoint );
		}
		finally
		{
			radiusStats.readLock().unlock();
		}
	}

	@Override
	public Spot addVertex( final int timepoint, final double[] position, final double radius, final Spot ref )
	{
		return modelGraph.addVertex( ref ).init( timepoint, position, radius );
	}

	@Override
	public Link addEdge( final Spot source, final Spot target, final Link ref )
	{
		return modelGraph.addEdge( source, target, ref ).init();
	}

	@Override
	public Link getEdge( final Spot source, final Spot target, final Link edge )
	{
		return modelGraph.getEdge( source, target, edge );
	}

	@Override
	public void removeEdge( final Link edge )
	{
		modelGraph.remove( edge );
	}

	@Override
	public void removeVertex( final Spot vertex )
	{
		modelGraph.remove( vertex );
	}

	@Override
	public void notifyGraphChanged()
	{
		modelGraph.notifyGraphChanged();
	}

}
