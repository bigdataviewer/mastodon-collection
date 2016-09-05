package org.mastodon.graph.traversal;

import static org.mastodon.graph.algorithm.traversal.GraphSearch.EdgeClass.BACK;
import static org.mastodon.graph.algorithm.traversal.GraphSearch.EdgeClass.TREE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.mastodon.graph.TestEdge;
import org.mastodon.graph.TestVertex;
import org.mastodon.graph.algorithm.traversal.DepthFirstSearch;
import org.mastodon.graph.algorithm.traversal.GraphSearch.EdgeClass;
import org.mastodon.graph.algorithm.traversal.GraphSearch.SearchDirection;
import org.mastodon.graph.object.ObjectEdge;
import org.mastodon.graph.object.ObjectVertex;
import org.mastodon.graph.traversal.GraphsForTests.GraphTestBundle;
import org.mastodon.graph.traversal.GraphsForTests.TraversalTester;

/**
 * We assume that for unsorted search, child vertices are returned in the order
 * they are added to the graph. If they are not, this test will fail, but it
 * does not necessary means it is incorrect
 */
public class DepthFirstSearchUndirectedTest
{

	@Test
	public void testForkPoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = GraphsForTests.forkPoolObjects();

		final TestVertex first = bundle.vertices.get( 0 );
		final DepthFirstSearch< TestVertex, TestEdge > dfs = new DepthFirstSearch<>( bundle.graph, SearchDirection.UNDIRECTED );

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE, TREE } );
		final List< TestVertex > processedVertices = Arrays.asList( new TestVertex[] {
				bundle.vertices.get( 1 ),
				bundle.vertices.get( 2 ),
				bundle.vertices.get( 0 )
		} );
		final TraversalTester< TestVertex, TestEdge, DepthFirstSearch< TestVertex, TestEdge > > traversalTester =
				new TraversalTester<>(
				bundle.vertices.iterator(),
				processedVertices.iterator(),
				bundle.edges.iterator(),
				edgeClass.iterator() );

		dfs.setTraversalListener( traversalTester );
		dfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testForkStdObjects()
	{
		final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> bundle = GraphsForTests.forkStdObjects();

		final ObjectVertex< Integer > first = bundle.vertices.get( 0 );
		final DepthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer > > dfs = new DepthFirstSearch<>( bundle.graph, SearchDirection.UNDIRECTED );

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE, TREE } );
		final List< ObjectVertex< Integer > > processedVertices = new ArrayList<>( 3 );
		processedVertices.add( bundle.vertices.get( 1 ) );
		processedVertices.add( bundle.vertices.get( 2 ) );
		processedVertices.add( bundle.vertices.get( 0 ) );

		final TraversalTester< ObjectVertex< Integer >, ObjectEdge< Integer >, DepthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >>> traversalTester =
				new TraversalTester<>(
						bundle.vertices.iterator(),
						processedVertices.iterator(),
						bundle.edges.iterator(),
						edgeClass.iterator() );

		dfs.setTraversalListener( traversalTester );
		dfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testLoopPoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = GraphsForTests.loopPoolObjects();

		final TestVertex first = bundle.vertices.get( 0 );
		final DepthFirstSearch< TestVertex, TestEdge > dfs = new DepthFirstSearch<>( bundle.graph, SearchDirection.UNDIRECTED );

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE, TREE, TREE, TREE, TREE, TREE, BACK } );

		final List< TestVertex > discoveredVertices = Arrays.asList( new TestVertex[] {
				bundle.vertices.get( 0 ),
				bundle.vertices.get( 6 ),
				bundle.vertices.get( 5 ),
				bundle.vertices.get( 4 ),
				bundle.vertices.get( 3 ),
				bundle.vertices.get( 2 ),
				bundle.vertices.get( 1 )
		} );

		final List< TestVertex > processedVertices = Arrays.asList( new TestVertex[] {
				bundle.vertices.get( 1 ),
				bundle.vertices.get( 2 ),
				bundle.vertices.get( 3 ),
				bundle.vertices.get( 4 ),
				bundle.vertices.get( 5 ),
				bundle.vertices.get( 6 ),
				bundle.vertices.get( 0 )
		} );

		final List< TestEdge > expectedEdges = Arrays.asList( new TestEdge[] {
				bundle.edges.get( 6 ),
				bundle.edges.get( 5 ),
				bundle.edges.get( 4 ),
				bundle.edges.get( 3 ),
				bundle.edges.get( 2 ),
				bundle.edges.get( 1 ),
				bundle.edges.get( 0 )
		} );

		final TraversalTester< TestVertex, TestEdge, DepthFirstSearch< TestVertex, TestEdge > > traversalTester =
				new TraversalTester<>(
				discoveredVertices.iterator(),
				processedVertices.iterator(),
				expectedEdges.iterator(),
				edgeClass.iterator() );

		dfs.setTraversalListener( traversalTester );
		dfs.start( first );
		traversalTester.searchDone();
	}

	@SuppressWarnings( "unchecked" )
	@Test
	public void testLoopStdObjects()
	{
		final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> bundle = GraphsForTests.loopStdObjects();

		final ObjectVertex< Integer > first = bundle.vertices.get( 0 );
		final DepthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer > > dfs = new DepthFirstSearch<>( bundle.graph, SearchDirection.UNDIRECTED );

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE, TREE, TREE, TREE, TREE, TREE, BACK } );
		final List< ObjectVertex< Integer > > discoveredVertices = Arrays.asList( new ObjectVertex[] {
				bundle.vertices.get( 0 ),
				bundle.vertices.get( 6 ),
				bundle.vertices.get( 5 ),
				bundle.vertices.get( 4 ),
				bundle.vertices.get( 3 ),
				bundle.vertices.get( 2 ),
				bundle.vertices.get( 1 )
		} );

		final List< ObjectVertex< Integer > > processedVertices = Arrays.asList( new ObjectVertex[] {
				bundle.vertices.get( 1 ),
				bundle.vertices.get( 2 ),
				bundle.vertices.get( 3 ),
				bundle.vertices.get( 4 ),
				bundle.vertices.get( 5 ),
				bundle.vertices.get( 6 ),
				bundle.vertices.get( 0 )
		} );

		final List< ObjectEdge< Integer > > expectedEdges = Arrays.asList( new ObjectEdge[] {
				bundle.edges.get( 6 ),
				bundle.edges.get( 5 ),
				bundle.edges.get( 4 ),
				bundle.edges.get( 3 ),
				bundle.edges.get( 2 ),
				bundle.edges.get( 1 ),
				bundle.edges.get( 0 )
		} );


		final TraversalTester< ObjectVertex< Integer >, ObjectEdge< Integer >, DepthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >>> traversalTester =
				new TraversalTester<>(
						discoveredVertices.iterator(),
						processedVertices.iterator(),
						expectedEdges.iterator(),
						edgeClass.iterator() );

		dfs.setTraversalListener( traversalTester );
		dfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testExamplePoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = GraphsForTests.wpExamplePoolObjects();

		final TestVertex first = bundle.vertices.get( 0 );
		final DepthFirstSearch< TestVertex, TestEdge > dfs = new DepthFirstSearch<>( bundle.graph, SearchDirection.UNDIRECTED );

		final List< TestVertex > expectedVertices = Arrays.asList( new TestVertex[] {
				bundle.vertices.get( 0 ),
				bundle.vertices.get( 1 ),
				bundle.vertices.get( 3 ),
				bundle.vertices.get( 5 ),
				bundle.vertices.get( 4 ),
				bundle.vertices.get( 2 ),
				bundle.vertices.get( 6 )
		} );
		final List< TestVertex > processedVertices = Arrays.asList( new TestVertex[] {
				bundle.vertices.get( 3 ),
				bundle.vertices.get( 4 ),
				bundle.vertices.get( 5 ),
				bundle.vertices.get( 1 ),
				bundle.vertices.get( 6 ),
				bundle.vertices.get( 2 ),
				bundle.vertices.get( 0 )
		} );
		final List< TestEdge > expectedEdges = Arrays.asList( new TestEdge[] {
				bundle.edges.get( 0 ),
				bundle.edges.get( 3 ),
				bundle.edges.get( 4 ),
				bundle.edges.get( 5 ),
				bundle.edges.get( 2 ),
				bundle.edges.get( 1 ),
				bundle.edges.get( 6 )
		} );
		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE, TREE, TREE, TREE, BACK, TREE, TREE } );

		final TraversalTester< TestVertex, TestEdge, DepthFirstSearch< TestVertex, TestEdge > > traversalTester =
				new TraversalTester<>(
				expectedVertices.iterator(),
				processedVertices.iterator(),
				expectedEdges.iterator(),
				edgeClass.iterator() );

		dfs.setTraversalListener( traversalTester );
		dfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testExampleStdObjects()
	{
		final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> bundle = GraphsForTests.wpExampleStdObjects();

		final ObjectVertex< Integer > first = bundle.vertices.get( 0 );
		final DepthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer > > dfs = new DepthFirstSearch<>( bundle.graph, SearchDirection.UNDIRECTED );

		final List< ObjectVertex< Integer > > expectedVertices = new ArrayList<>( 7 );
		expectedVertices.add( bundle.vertices.get( 0 ) );
		expectedVertices.add( bundle.vertices.get( 1 ) );
		expectedVertices.add( bundle.vertices.get( 3 ) );
		expectedVertices.add( bundle.vertices.get( 5 ) );
		expectedVertices.add( bundle.vertices.get( 4 ) );
		expectedVertices.add( bundle.vertices.get( 2 ) );
		expectedVertices.add( bundle.vertices.get( 6 ) );

		final List< ObjectVertex< Integer > > processedVertices = new ArrayList<>( 7 );
		processedVertices.add( bundle.vertices.get( 3 ) );
		processedVertices.add( bundle.vertices.get( 4 ) );
		processedVertices.add( bundle.vertices.get( 5 ) );
		processedVertices.add( bundle.vertices.get( 1 ) );
		processedVertices.add( bundle.vertices.get( 6 ) );
		processedVertices.add( bundle.vertices.get( 2 ) );
		processedVertices.add( bundle.vertices.get( 0 ) );

		final List< ObjectEdge< Integer > > expectedEdges = new ArrayList<>( 7 );
		expectedEdges.add( bundle.edges.get( 0 ) );
		expectedEdges.add( bundle.edges.get( 3 ) );
		expectedEdges.add( bundle.edges.get( 4 ) );
		expectedEdges.add( bundle.edges.get( 5 ) );
		expectedEdges.add( bundle.edges.get( 2 ) );
		expectedEdges.add( bundle.edges.get( 1 ) );
		expectedEdges.add( bundle.edges.get( 6 ) );

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE, TREE, TREE, TREE, BACK, TREE, TREE } );

		final TraversalTester< ObjectVertex< Integer >, ObjectEdge< Integer >, DepthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >>> traversalTester =
				new TraversalTester<>(
				expectedVertices.iterator(),
				processedVertices.iterator(),
				expectedEdges.iterator(),
				edgeClass.iterator() );

		dfs.setTraversalListener( traversalTester );
		dfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testSingleEdgePoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = GraphsForTests.singleEdgePoolObjects();

		final TestVertex first = bundle.vertices.get( 0 );
		final DepthFirstSearch< TestVertex, TestEdge > dfs = new DepthFirstSearch<>( bundle.graph, SearchDirection.UNDIRECTED );

		final List< TestVertex > expectedVertices = Arrays.asList( new TestVertex[] {
				bundle.vertices.get( 0 ),
				bundle.vertices.get( 1 )
		} );
		final List< TestVertex > processedVertices = Arrays.asList( new TestVertex[] {
				bundle.vertices.get( 1 ),
				bundle.vertices.get( 0 )
		} );
		final List< TestEdge > expectedEdges = Arrays.asList( new TestEdge[] {
				bundle.edges.get( 0 )
		} );
		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE } );

		final TraversalTester< TestVertex, TestEdge, DepthFirstSearch< TestVertex, TestEdge > > traversalTester =
				new TraversalTester<>(
				expectedVertices.iterator(),
				processedVertices.iterator(),
				expectedEdges.iterator(),
				edgeClass.iterator() );

		dfs.setTraversalListener( traversalTester );
		dfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testSingleEdgeStdObjects()
	{
		final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> bundle = GraphsForTests.singleEdgeStdObjects();

		final ObjectVertex< Integer > first = bundle.vertices.get( 0 );
		final DepthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer > > dfs = new DepthFirstSearch<>( bundle.graph, SearchDirection.UNDIRECTED );

		final List< ObjectVertex< Integer > > expectedVertices = new ArrayList<>( 2 );
		expectedVertices.add( bundle.vertices.get( 0 ) );
		expectedVertices.add( bundle.vertices.get( 1 ) );

		final List< ObjectVertex< Integer > > processedVertices = new ArrayList<>( 2 );
		processedVertices.add( bundle.vertices.get( 1 ) );
		processedVertices.add( bundle.vertices.get( 0 ) );

		final List< ObjectEdge< Integer > > expectedEdges = new ArrayList<>( 1 );
		expectedEdges.add( bundle.edges.get( 0 ) );

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE } );

		final TraversalTester< ObjectVertex< Integer >, ObjectEdge< Integer >, DepthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >>> traversalTester =
				new TraversalTester<>(
				expectedVertices.iterator(),
				processedVertices.iterator(),
				expectedEdges.iterator(),
				edgeClass.iterator() );

		dfs.setTraversalListener( traversalTester );
		dfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testStraightLinePoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = GraphsForTests.straightLinePoolObjects();

		final TestVertex first = bundle.vertices.get( 0 );
		final DepthFirstSearch< TestVertex, TestEdge > dfs = new DepthFirstSearch<>( bundle.graph, SearchDirection.UNDIRECTED );

		final List< TestVertex > expectedVertices = bundle.vertices;

		final List< TestVertex > processedVertices = Arrays.asList( new TestVertex[] {
				bundle.vertices.get( 6 ),
				bundle.vertices.get( 5 ),
				bundle.vertices.get( 4 ),
				bundle.vertices.get( 3 ),
				bundle.vertices.get( 2 ),
				bundle.vertices.get( 1 ),
				bundle.vertices.get( 0 )
		} );
		final List< TestEdge > expectedEdges = bundle.edges;

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE, TREE, TREE, TREE, TREE, TREE } );

		final TraversalTester< TestVertex, TestEdge, DepthFirstSearch< TestVertex, TestEdge > > traversalTester =
				new TraversalTester<>(
				expectedVertices.iterator(),
				processedVertices.iterator(),
				expectedEdges.iterator(),
				edgeClass.iterator() );

		dfs.setTraversalListener( traversalTester );
		dfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testStraightLineStdObjects()
	{
		final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> bundle = GraphsForTests.straightLineStdObjects();

		final ObjectVertex< Integer > first = bundle.vertices.get( 0 );
		final DepthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer > > dfs = new DepthFirstSearch<>( bundle.graph, SearchDirection.UNDIRECTED );

		final List< ObjectVertex< Integer > > expectedVertices = bundle.vertices;

		final List< ObjectVertex< Integer > > processedVertices = new ArrayList<>( 2 );
		processedVertices.add( bundle.vertices.get( 6 ) );
		processedVertices.add( bundle.vertices.get( 5 ) );
		processedVertices.add( bundle.vertices.get( 4 ) );
		processedVertices.add( bundle.vertices.get( 3 ) );
		processedVertices.add( bundle.vertices.get( 2 ) );
		processedVertices.add( bundle.vertices.get( 1 ) );
		processedVertices.add( bundle.vertices.get( 0 ) );

		final List< ObjectEdge< Integer > > expectedEdges = bundle.edges;

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE, TREE, TREE, TREE, TREE, TREE } );

		final TraversalTester< ObjectVertex< Integer >, ObjectEdge< Integer >, DepthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >>> traversalTester =
				new TraversalTester<>(
				expectedVertices.iterator(),
				processedVertices.iterator(),
				expectedEdges.iterator(),
				edgeClass.iterator() );

		dfs.setTraversalListener( traversalTester );
		dfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testTwoComponentsPoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = GraphsForTests.twoComponentsPoolObjects();

		final TestVertex first = bundle.vertices.get( 0 );
		final DepthFirstSearch< TestVertex, TestEdge > dfs = new DepthFirstSearch<>( bundle.graph, SearchDirection.UNDIRECTED );

		final List< TestVertex > expectedVertices = Arrays.asList( new TestVertex[] {
				bundle.vertices.get( 0 ),
				bundle.vertices.get( 1 ),
				bundle.vertices.get( 3 ),
				bundle.vertices.get( 5 ),
				bundle.vertices.get( 4 ),
				bundle.vertices.get( 2 ),
				bundle.vertices.get( 6 )
		} );
		final List< TestVertex > processedVertices = Arrays.asList( new TestVertex[] {
				bundle.vertices.get( 3 ),
				bundle.vertices.get( 4 ),
				bundle.vertices.get( 5 ),
				bundle.vertices.get( 1 ),
				bundle.vertices.get( 6 ),
				bundle.vertices.get( 2 ),
				bundle.vertices.get( 0 )
		} );
		final List< TestEdge > expectedEdges = Arrays.asList( new TestEdge[] {
				bundle.edges.get( 0 ),
				bundle.edges.get( 3 ),
				bundle.edges.get( 4 ),
				bundle.edges.get( 5 ),
				bundle.edges.get( 2 ),
				bundle.edges.get( 1 ),
				bundle.edges.get( 6 )
		} );
		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE, TREE, TREE, TREE, BACK, TREE, TREE } );

		final TraversalTester< TestVertex, TestEdge, DepthFirstSearch< TestVertex, TestEdge > > traversalTester =
				new TraversalTester<>(
				expectedVertices.iterator(),
				processedVertices.iterator(),
				expectedEdges.iterator(),
				edgeClass.iterator() );

		dfs.setTraversalListener( traversalTester );
		dfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testTwoComponentsStdObjects()
	{
		final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> bundle = GraphsForTests.twoComponentsStdObjects();

		final ObjectVertex< Integer > first = bundle.vertices.get( 0 );
		final DepthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer > > dfs = new DepthFirstSearch<>( bundle.graph, SearchDirection.UNDIRECTED );

		final List< ObjectVertex< Integer > > expectedVertices = new ArrayList<>( 7 );
		expectedVertices.add( bundle.vertices.get( 0 ) );
		expectedVertices.add( bundle.vertices.get( 1 ) );
		expectedVertices.add( bundle.vertices.get( 3 ) );
		expectedVertices.add( bundle.vertices.get( 5 ) );
		expectedVertices.add( bundle.vertices.get( 4 ) );
		expectedVertices.add( bundle.vertices.get( 2 ) );
		expectedVertices.add( bundle.vertices.get( 6 ) );

		final List< ObjectVertex< Integer > > processedVertices = new ArrayList<>( 7 );
		processedVertices.add( bundle.vertices.get( 3 ) );
		processedVertices.add( bundle.vertices.get( 4 ) );
		processedVertices.add( bundle.vertices.get( 5 ) );
		processedVertices.add( bundle.vertices.get( 1 ) );
		processedVertices.add( bundle.vertices.get( 6 ) );
		processedVertices.add( bundle.vertices.get( 2 ) );
		processedVertices.add( bundle.vertices.get( 0 ) );

		final List< ObjectEdge< Integer > > expectedEdges = new ArrayList<>( 7 );
		expectedEdges.add( bundle.edges.get( 0 ) );
		expectedEdges.add( bundle.edges.get( 3 ) );
		expectedEdges.add( bundle.edges.get( 4 ) );
		expectedEdges.add( bundle.edges.get( 5 ) );
		expectedEdges.add( bundle.edges.get( 2 ) );
		expectedEdges.add( bundle.edges.get( 1 ) );
		expectedEdges.add( bundle.edges.get( 6 ) );

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE, TREE, TREE, TREE, BACK, TREE, TREE } );

		final TraversalTester< ObjectVertex< Integer >, ObjectEdge< Integer >, DepthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >>> traversalTester =
				new TraversalTester<>(
				expectedVertices.iterator(),
				processedVertices.iterator(),
				expectedEdges.iterator(),
				edgeClass.iterator() );

		dfs.setTraversalListener( traversalTester );
		dfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testSingleVertexPoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = GraphsForTests.singleVertexPoolObjects();

		final TestVertex first = bundle.vertices.get( 0 );
		final DepthFirstSearch< TestVertex, TestEdge > dfs = new DepthFirstSearch<>( bundle.graph, SearchDirection.UNDIRECTED );

		final List< TestVertex > expectedVertices = Arrays.asList( new TestVertex[] {
				bundle.vertices.get( 0 )
		} );

		final List< TestVertex > processedVertices = Arrays.asList( new TestVertex[] {
				bundle.vertices.get( 0 )
		} );

		final List< TestEdge > expectedEdges = Collections.emptyList();

		final List< EdgeClass > edgeClass = Collections.emptyList();

		final TraversalTester< TestVertex, TestEdge, DepthFirstSearch< TestVertex, TestEdge > > traversalTester =
				new TraversalTester<>(
				expectedVertices.iterator(),
				processedVertices.iterator(),
				expectedEdges.iterator(),
				edgeClass.iterator() );

		dfs.setTraversalListener( traversalTester );
		dfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testSingleVertexStdObjects()
	{
		final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> bundle = GraphsForTests.singleVertexStdObjects();

		final ObjectVertex< Integer > first = bundle.vertices.get( 0 );
		final DepthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer > > dfs = new DepthFirstSearch<>( bundle.graph, SearchDirection.UNDIRECTED );

		final List< ObjectVertex< Integer > > expectedVertices = new ArrayList<>( 2 );
		expectedVertices.add( bundle.vertices.get( 0 ) );

		final List< ObjectVertex< Integer > > processedVertices = new ArrayList<>( 2 );
		processedVertices.add( bundle.vertices.get( 0 ) );

		final List< ObjectEdge< Integer > > expectedEdges = Collections.emptyList();

		final List< EdgeClass > edgeClass = Collections.emptyList();

		final TraversalTester< ObjectVertex< Integer >, ObjectEdge< Integer >, DepthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >>> traversalTester =
				new TraversalTester<>(
				expectedVertices.iterator(),
				processedVertices.iterator(),
				expectedEdges.iterator(),
				edgeClass.iterator() );

		dfs.setTraversalListener( traversalTester );
		dfs.start( first );
		traversalTester.searchDone();
	}
}