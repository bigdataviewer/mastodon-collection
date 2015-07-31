package net.trackmate.bdv;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;

import mpicbg.spim.data.SpimDataException;
import net.imglib2.RealPoint;
import net.imglib2.algorithm.kdtree.ConvexPolytope;
import net.imglib2.algorithm.kdtree.HyperPlane;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.InteractiveDisplayCanvasComponent;
import net.imglib2.ui.TransformListener;
import net.trackmate.bdv.wrapper.HasTrackSchemeVertex;
import net.trackmate.bdv.wrapper.OverlayEdge;
import net.trackmate.bdv.wrapper.OverlayGraph;
import net.trackmate.bdv.wrapper.OverlayGraphWrapper;
import net.trackmate.bdv.wrapper.OverlayVertex;
import net.trackmate.bdv.wrapper.SpatialSearch;
import net.trackmate.bdv.wrapper.SpotOverlayProperties;
import net.trackmate.graph.PoolObjectList;
import net.trackmate.io.RawIO;
import net.trackmate.model.Link;
import net.trackmate.model.Model;
import net.trackmate.model.ModelGraph;
import net.trackmate.model.Spot;
import net.trackmate.trackscheme.ShowTrackScheme;
import net.trackmate.trackscheme.ShowTrackScheme.HACK_SelectionListener;
import net.trackmate.trackscheme.TrackSchemeGraph;
import net.trackmate.trackscheme.TrackSchemeUtil;
import net.trackmate.trackscheme.TrackSchemeVertex;
import bdv.BigDataViewer;
import bdv.export.ProgressWriterConsole;
import bdv.viewer.InputActionBindings;
import bdv.viewer.ViewerPanel;
import bdv.viewer.animate.TranslationAnimator;
import bdv.viewer.state.ViewerState;

public class Launcher
{
	public static void main( final String[] args ) throws IOException, SpimDataException
	{
		/*
		 * Settings.
		 */

//		final String bdvFile = "/Volumes/Data/BDV_MVD_5v_final.xml";
//		final String modelFile = "/Volumes/Data/model-small.raw";
//		final int timepointIndex = 50;
		final String bdvFile = "D:/Users/Jean-Yves/Development/Data/drosophila.xml";
		final String modelFile = null;
		final int timepointIndex = 1;

		/*
		 * Load BDV.
		 */

		System.setProperty( "apple.laf.useScreenMenuBar", "true" );
		final BigDataViewer bdv = BigDataViewer.open( bdvFile, new File( bdvFile ).getName(), new ProgressWriterConsole() );
		bdv.getViewer().setTimepoint( timepointIndex );

		/*
		 * Load model.
		 */

		final Model model;
		if ( null != modelFile )
		{
			model = RawIO.read( new File( modelFile ) );
		}
		else
		{
			model = new Model( new ModelGraph() );
		}

		/*
		 * Build TrackScheme.
		 */

		final ModelGraph graph = model.getGraph();
		final TrackSchemeGraph tsg = TrackSchemeUtil.buildTrackSchemeGraph( graph, graph.getIdBimap() );
		final ShowTrackScheme trackscheme = new ShowTrackScheme( tsg );
		trackscheme.setSelectionListener( new HACK_SelectionListener()
		{
			final Spot spot = graph.vertexRef();

			@Override
			public void select( final TrackSchemeVertex v )
			{
				graph.getIdBimap().getVertex( v.getModelVertexId(), spot );
				centerViewOn( spot, bdv.getViewer() );
			}

			@Override
			public void repaint()
			{
				bdv.getViewer().getDisplay().repaint();
			}
		} );

		final OverlayGraphWrapper< Spot, Link > overlayGraph =
				new OverlayGraphWrapper< Spot, Link >( tsg, graph, graph.getIdBimap(), SpotOverlayProperties.instance );
		overlayGraph.HACK_updateTimepointSets();
		final TracksOverlay tracksOverlay = new TracksOverlay( overlayGraph, bdv.getViewer(), model.frames().size() );
		bdv.getViewer().getDisplay().addOverlayRenderer( tracksOverlay );
		bdv.getViewer().addRenderTransformListener( tracksOverlay );
		setupContextTrackscheme( bdv, overlayGraph, trackscheme );

		final ViewerPanel viewer = bdv.getViewer();
		viewer.getDisplay().addMouseListener( new MouseAdapter()
		{
			@Override
			public void mouseClicked( final MouseEvent e )
			{
				// Check if the mouse is not off-screen
				final Point mouseScreenLocation = e.getPoint();
				// MouseInfo.getPointerInfo().getLocation();
				final Point viewerPosition = viewer.getLocationOnScreen();
				final Dimension viewerSize = viewer.getSize();
				if ( mouseScreenLocation.x < viewerPosition.x || mouseScreenLocation.y < viewerPosition.y || mouseScreenLocation.x > viewerPosition.x + viewerSize.width || mouseScreenLocation.y > viewerPosition.y + viewerSize.height ) { return; }

				final ViewerState state = viewer.getState();
				final int timepoint = state.getCurrentTimepoint();
				final int sourceId = state.getCurrentSource();

				// Ok, then create this spot, wherever it is.
				final double[] coordinates = new double[ 3 ];
				viewer.getGlobalMouseCoordinates( RealPoint.wrap( coordinates ) );

				final Spot spot = model.createSpot( timepoint, coordinates[ 0 ], coordinates[ 1 ], coordinates[ 2 ], 5d, model.getGraph().vertexRef() );
				System.out.println( spot );// DEBUG
				/*
				 * TODO: this has no effect, because the overlay and trackscheme
				 * do not know that a spot was created in the model. The model
				 * should be listenable, and TrackScheme and the overlay should
				 * register as listeners.
				 */
			}
		} );
	}

	private static final void centerViewOn( final Spot spot, final ViewerPanel viewer )
	{
		final ViewerState state = viewer.getState();
		final InteractiveDisplayCanvasComponent< AffineTransform3D > display = viewer.getDisplay();

		final int tp = spot.getTimePointId();
		viewer.setTimepoint( tp );

		final AffineTransform3D t = new AffineTransform3D();
		state.getViewerTransform( t );

		final double[] spotCoords = new double[ 3 ];
		spot.localize( spotCoords );

		// Translate view so that the target spot is in the middle of the
		// display
		final double dx = display.getWidth() / 2 - ( t.get( 0, 0 ) * spotCoords[ 0 ] + t.get( 0, 1 ) * spotCoords[ 1 ] + t.get( 0, 2 ) * spotCoords[ 2 ] );
		final double dy = display.getHeight() / 2 - ( t.get( 1, 0 ) * spotCoords[ 0 ] + t.get( 1, 1 ) * spotCoords[ 1 ] + t.get( 1, 2 ) * spotCoords[ 2 ] );
		final double dz = -( t.get( 2, 0 ) * spotCoords[ 0 ] + t.get( 2, 1 ) * spotCoords[ 1 ] + t.get( 2, 2 ) * spotCoords[ 2 ] );

		// But use an animator to do this smoothly.
		final double[] target = new double[] { dx, dy, dz };
		viewer.setTransformAnimator( new TranslationAnimator( t, target, 300 ) );
	}

	private static void setupContextTrackscheme(
			final BigDataViewer bdv,
			// TODO: should the overlayGraph parameter be more generic?
			final OverlayGraphWrapper< Spot, Link > overlayGraph,
			final ShowTrackScheme trackScheme )
	{
		final ContextTrackscheme< ?, ? > context = ContextTrackscheme.create( overlayGraph, trackScheme );

		final String REFRESH_CONTEXT_TRACKSCHEME = "refresh context trackscheme";
		final InputMap inputMap = new InputMap();
		inputMap.put( KeyStroke.getKeyStroke( "R" ), REFRESH_CONTEXT_TRACKSCHEME );
		final ActionMap actionMap = new ActionMap();
		actionMap.put( REFRESH_CONTEXT_TRACKSCHEME, new AbstractAction()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed( final ActionEvent e )
			{
				System.out.println( REFRESH_CONTEXT_TRACKSCHEME + ": Do nothing." );
			}
		} );
		final InputActionBindings bindings = bdv.getViewerFrame().getKeybindings();
		bindings.addActionMap( "trackscheme", actionMap );
		bindings.addInputMap( "trackscheme", inputMap );

		bdv.getViewer().addRenderTransformListener( new TransformListener< AffineTransform3D >()
		{
			@Override
			public void transformChanged( final AffineTransform3D transform )
			{
				final ViewerState state = bdv.getViewer().getState();
				final int timepoint = state.getCurrentTimepoint();
				final AffineTransform3D viewerTransform = new AffineTransform3D();
				state.getViewerTransform( viewerTransform );
				final int width = bdv.getViewer().getWidth();
				final int height = bdv.getViewer().getHeight();
				context.buildContext( timepoint, viewerTransform, width, height );
			}
		} );
	}

	private static class ContextTrackscheme< V extends OverlayVertex< V, E > & HasTrackSchemeVertex, E extends OverlayEdge< E, V > >
	{
		private final OverlayGraph< V, E > graph;

		private final ShowTrackScheme trackScheme;

		private final PoolObjectList< TrackSchemeVertex > roots;

		public ContextTrackscheme(
				final OverlayGraph< V, E > graph,
				final ShowTrackScheme trackScheme )
		{
			this.graph = graph;
			this.trackScheme = trackScheme;
			roots = trackScheme.getGraph().createVertexList();
		}

		public void buildContext(
				final int timepoint,
				final AffineTransform3D viewerTransform,
				final int width,
				final int height )
		{
			final int depth = 200;
			final int minTimepoint = timepoint - 2;
			final int maxTimepoint = timepoint + 2;

			final ConvexPolytope crop = new ConvexPolytope(
					new HyperPlane( 0, 0, 1, -depth ),
					new HyperPlane( 0, 0, -1, -depth ),
					new HyperPlane( 1, 0, 0, 0 ),
					new HyperPlane( -1, 0, 0, -width ),
					new HyperPlane( 0, 1, 0, 0 ),
					new HyperPlane( 0, -1, 0, -height ) );
			final ConvexPolytope tcrop = ConvexPolytope.transform( crop, viewerTransform.inverse() );

			final int mark = trackScheme.getNewLayoutTimestamp();

			// mark vertices in crop region with timestamp and find roots.
			roots.clear();
			for ( int t = minTimepoint; t <= maxTimepoint; ++t )
			{
				final SpatialSearch< V > search = graph.getSpatialSearch( t );
				if ( search != null )
				{
					search.clip( tcrop );
					for ( final V v : search.getInsideVertices() )
					{
						final TrackSchemeVertex tv = v.getTrackSchemeVertex();
						tv.setLayoutTimestamp( mark );
						if ( t == minTimepoint || tv.incomingEdges().isEmpty() )
							roots.add( tv );
					}
				}
			}
			roots.getIndexCollection().sort(); // TODO sort roots by something
												// meaningful...

			// layout and repaint
			trackScheme.relayout( roots, mark );
		}

		public static < V extends OverlayVertex< V, E > & HasTrackSchemeVertex, E extends OverlayEdge< E, V > >
				ContextTrackscheme< V, E > create(
						final OverlayGraph< V, E > graph,
						final ShowTrackScheme trackScheme )
		{
			return new ContextTrackscheme< V, E >( graph, trackScheme );
		}
	}
}