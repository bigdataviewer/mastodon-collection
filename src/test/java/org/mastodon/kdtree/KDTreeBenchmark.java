package org.mastodon.kdtree;

import java.util.Random;

import org.mastodon.collection.ref.RefArrayList;
import org.mastodon.pool.DoubleMappedElement;

import net.imglib2.RealLocalizable;
import net.imglib2.util.BenchmarkHelper;

public class KDTreeBenchmark
{
	private final int numDataVertices;

	private final int numTestVertices;

	private final double minCoordinateValue;

	private final double maxCoordinateValue;

	private final RealPointPool vertexPool;

	private final RefArrayList< RealPoint > dataVertices;

	private final RefArrayList< RealPoint > testVertices;

	public KDTreeBenchmark(final int numDataVertices, final int numTestVertices, final double minCoordinateValue, final double maxCoordinateValue)
	{
		this.numDataVertices = numDataVertices;
		this.numTestVertices = numTestVertices;
		this.minCoordinateValue = minCoordinateValue;
		this.maxCoordinateValue = maxCoordinateValue;
		vertexPool = new RealPointPool( 3, numDataVertices + numTestVertices );
		dataVertices = new RefArrayList<>( vertexPool, numDataVertices );
		testVertices = new RefArrayList<>( vertexPool, numTestVertices );
		createDataVertices();
	}

	private void createDataVertices()
	{
		final RealPoint vertex = vertexPool.createRef();
		final int n = vertex.numDimensions();
		final double[] p = new double[ n ];
		final double size = ( maxCoordinateValue - minCoordinateValue );
		final Random rnd = new Random( 4379 );
		for ( int i = 0; i < numDataVertices; ++i )
		{
			for ( int d = 0; d < n; ++d )
				p[ d ] = rnd.nextDouble() * size + minCoordinateValue;
			vertexPool.create( vertex );
			vertex.setPosition( p );
			dataVertices.add( vertex );
		}
		for ( int i = 0; i < numTestVertices; ++i )
		{
			for ( int d = 0; d < n; ++d )
				p[ d ] = rnd.nextDouble() * 2 * size + minCoordinateValue - size / 2;
			vertexPool.create( vertex );
			vertex.setPosition( p );
			testVertices.add( vertex );
		}
		vertexPool.releaseRef( vertex );
	}

	private KDTree< RealPoint, DoubleMappedElement > kdtree;

	public void createKDTree()
	{
		kdtree = KDTree.kdtree( dataVertices, vertexPool );
	}

	public void markInvalid()
	{
		final int numInvalidDataVertices = numDataVertices / 2;
		final Random rnd = new Random( 124 );
		final KDTreeNode< RealPoint, DoubleMappedElement > node = kdtree.createRef();
		for ( int i = 0; i < numInvalidDataVertices; ++i )
		{
			final int j = rnd.nextInt( kdtree.size() );
			kdtree.getObject( j, node );
			node.setValid( false );
		}
	}

	public void nearestNeighborSearch( final int numRuns )
	{
		final NearestNeighborSearchOnKDTree< RealPoint, DoubleMappedElement > kd = new NearestNeighborSearchOnKDTree<>( kdtree );
		for ( int i = 0; i < numRuns; ++i )
			for ( final RealLocalizable t : testVertices )
			{
				kd.search( t );
				kd.getSampler().get();
			}
	}

	public void nearestValidNeighborSearch( final int numRuns )
	{
		final NearestValidNeighborSearchOnKDTree< RealPoint, DoubleMappedElement > kd = new NearestValidNeighborSearchOnKDTree<>( kdtree );
		for ( int i = 0; i < numRuns; ++i )
			for ( final RealLocalizable t : testVertices )
			{
				kd.search( t );
				kd.getSampler().get();
			}
	}

	private net.imglib2.KDTree< RealPoint > kdtreeImgLib2;

	public void createKDTreeImgLib2()
	{
		kdtreeImgLib2 = new net.imglib2.KDTree<>( dataVertices, dataVertices );
	}

	public void nearestNeighborSearchImgLib2( final int numRuns )
	{
		final net.imglib2.neighborsearch.NearestNeighborSearchOnKDTree< RealPoint > kd = new net.imglib2.neighborsearch.NearestNeighborSearchOnKDTree<>( kdtreeImgLib2 );
		for ( int i = 0; i < numRuns; ++i )
			for ( final RealLocalizable t : testVertices )
			{
				kd.search( t );
				kd.getSampler().get();
			}
	}

	public static void main( final String[] args )
	{
		final KDTreeBenchmark b = new KDTreeBenchmark( 10000, 1000, -5, 5 );
		final boolean printIndividualTimes = true;

		System.out.println( "createKDTree()" );
		BenchmarkHelper.benchmarkAndPrint( 10, printIndividualTimes, new Runnable()
		{
			@Override
			public void run()
			{
				b.createKDTree();
			}
		} );

		b.markInvalid();

		System.out.println( "nearestNeighborSearch()" );
		BenchmarkHelper.benchmarkAndPrint( 10, printIndividualTimes, new Runnable()
		{
			@Override
			public void run()
			{
				b.nearestNeighborSearch( 10 );
			}
		} );

		System.out.println( "nearestValidNeighborSearch()" );
		BenchmarkHelper.benchmarkAndPrint( 10, printIndividualTimes, new Runnable()
		{
			@Override
			public void run()
			{
				b.nearestValidNeighborSearch( 10 );
			}
		} );

		System.out.println( "createKDTreeImgLib2()" );
		BenchmarkHelper.benchmarkAndPrint( 10, printIndividualTimes, new Runnable()
		{
			@Override
			public void run()
			{
				b.createKDTreeImgLib2();
			}
		} );

		System.out.println( "nearestNeighborSearchImgLib2()" );
		BenchmarkHelper.benchmarkAndPrint( 10, printIndividualTimes, new Runnable()
		{
			@Override
			public void run()
			{
				b.nearestNeighborSearchImgLib2( 10 );
			}
		} );
	}
}
