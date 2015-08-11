package net.trackmate.model.tgmm.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.util.HashSet;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;

import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.OverlayRenderer;
import net.imglib2.util.LinAlgHelpers;
import net.trackmate.bdv.wrapper.OverlayGraphWrapper;
import net.trackmate.bdv.wrapper.OverlayVertexWrapper;
import net.trackmate.bdv.wrapper.SpatialSearch;
import net.trackmate.model.Link;
import net.trackmate.model.tgmm.SpotCovariance;
import net.trackmate.model.tgmm.TgmmModel;
import net.trackmate.trackscheme.AbstractNamedDefaultKeyStrokeAction;
import net.trackmate.trackscheme.GraphIdBimap;
import net.trackmate.trackscheme.SelectionHandler;
import net.trackmate.trackscheme.ShowTrackScheme;
import net.trackmate.trackscheme.TrackSchemeVertex;
import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import bdv.viewer.ViewerPanel;
import bdv.viewer.state.ViewerState;

public class ModelEditHandler implements MouseListener, MouseMotionListener, OverlayRenderer, KeyListener
{
	private static final double DEFAULT_RADIUS = 10.;

	/**
	 * By how portion of the current radius we change this radius for every
	 * change request.
	 */
	private static final double RADIUS_CHANGE_FACTOR = 0.1;

	/** The radius below which a spot cannot go. */
	private static final double MIN_RADIUS = 2.;

	private final ViewerPanel viewer;

	private final TgmmModel model;

	private final ShowTrackScheme trackscheme;

	private final SelectionHandler selectionHandler;

	private final OverlayGraphWrapper< SpotCovariance, Link< SpotCovariance >> wrapper;

	/**
	 * Used to read current transform from {@link #viewer} state, that
	 * transforms data coordinates into user (viewer) coordinates.
	 */
	private final AffineTransform3D t = new AffineTransform3D();

	/**
	 * Used to store position in user (viewer) coordinates.
	 */
	private final RealPoint from = new RealPoint( 3 );

	/**
	 * Used to store position in data coordinates.
	 */
	private final RealPoint to = new RealPoint( 3 );

	/**
	 * Radius used to create new spots.
	 */
	private double radius = DEFAULT_RADIUS;

	/**
	 * A reference used in PoolObjectCollection methods.
	 */
	private final SpotCovariance ref;

	private final ActionMap actionMap;

	private final InputMap inputMap;

	private final GhostOverlay overlay;

	private final SpotMover spotMover;

	private final LinkedSpotCreator linkedSpotCreator;

	private final LinkCreator linkCreator;

	/*
	 * CONSTRUCTOR
	 */


	public ModelEditHandler( final TgmmModel model, final OverlayGraphWrapper< SpotCovariance, Link< SpotCovariance > > wrapper, final ViewerPanel viewer, final ShowTrackScheme trackscheme )
	{
		this.model = model;
		this.wrapper = wrapper;
		this.viewer = viewer;
		this.trackscheme = trackscheme;
		this.selectionHandler = trackscheme.getSelectionHandler();
		this.ref = model.getGraph().vertexRef();
		this.actionMap = new ActionMap();
		this.inputMap = new InputMap();
		this.overlay = new GhostOverlay();
		this.spotMover = new SpotMover( 'v' );
		this.linkedSpotCreator = new LinkedSpotCreator( 'A' );
		this.linkCreator = new LinkCreator( 'l' );
		install();
	}

	private void install()
	{
		final HashSet< AbstractNamedDefaultKeyStrokeAction > actions = new HashSet< AbstractNamedDefaultKeyStrokeAction >();
		actions.add( new CreateSpotAction() );
		actions.add( new ChangeSpotRadiusAction( true, true ) );
		actions.add( new ChangeSpotRadiusAction( true, false ) );
		actions.add( new ChangeSpotRadiusAction( false, true ) );
		actions.add( new ChangeSpotRadiusAction( false, false ) );

		for ( final AbstractNamedDefaultKeyStrokeAction action : actions )
		{
			actionMap.put( action.name(), action );
			inputMap.put( action.getDefaultKeyStroke(), action.name() );
		}
	}

	public ActionMap getActionMap()
	{
		return actionMap;
	}

	public InputMap getDefaultInputMap()
	{
		return inputMap;
	}

	/*
	 * MOUSE METHODS.
	 */

	@Override
	public void mouseClicked( final MouseEvent e )
	{
		new Thread( "Select spot thread." )
		{
			@Override
			public void run()
			{
				final ViewerState state = viewer.getState();
				final int timepoint = state.getCurrentTimepoint();
				final SpatialSearch< OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance > > > search =
						wrapper.getSpatialSearch( timepoint );
				viewToData( e.getPoint(), state );
				search.search( to );
				final OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance >> v = search.nearestNeighbor();
				if ( null == v )
					return;

				final double boundingSphereRadiusSquared = v.get().getBoundingSphereRadiusSquared();
				final double sqDist = search.nearestNeighborSquareDistance();
				if ( sqDist < boundingSphereRadiusSquared )
				{
					final TrackSchemeVertex tv = v.getTrackSchemeVertex();
					if ( e.isShiftDown() )
					{
						selectionHandler.select( tv, true );
					}
					else
					{
						selectionHandler.clearSelection();
						selectionHandler.select( tv, false );
					}
					repaint();
				}
			}
		}.start();
	}

	@Override
	public void mouseEntered( final MouseEvent e )
	{}

	@Override
	public void mouseExited( final MouseEvent e )
	{}

	@Override
	public void mousePressed( final MouseEvent e )
	{}

	@Override
	public void mouseReleased( final MouseEvent e )
	{}

	@Override
	public void mouseDragged( final MouseEvent e )
	{}

	@Override
	public void mouseMoved( final MouseEvent e )
	{
		spotMover.move();
		linkCreator.move();
	}

	@Override
	public void keyPressed( final KeyEvent e )
	{
		if ( e.getKeyChar() == spotMover.key )
		{
			spotMover.grab();
		}
		else if ( e.getKeyChar() == linkedSpotCreator.key && e.isShiftDown() )
		{
			linkedSpotCreator.create();
		}
		else if ( e.getKeyChar() == linkCreator.key )
		{
			linkCreator.create();
		}
	}

	@Override
	public void keyReleased( final KeyEvent e )
	{
		if ( Character.toLowerCase( e.getKeyChar() ) == Character.toLowerCase( spotMover.key ) )
		{
			spotMover.release();
		}
		else if ( Character.toLowerCase( e.getKeyChar() ) == Character.toLowerCase( linkedSpotCreator.key ) )
		{
			linkedSpotCreator.release();
		}
		else if ( Character.toLowerCase( e.getKeyChar() ) == Character.toLowerCase( linkCreator.key ) )
		{
			linkCreator.release();
		}
	}

	@Override
	public void keyTyped( final KeyEvent e )
	{}

	/*
	 * PRIVATE METHODS.
	 */

	private void repaint()
	{
		trackscheme.repaint();
		viewer.repaint();
	}

	private OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance >> getSpotUnderMouse()
	{
		viewer.getGlobalMouseCoordinates( to );
		final ViewerState state = viewer.getState();
		final int timepoint = state.getCurrentTimepoint();

		final SpatialSearch< OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance > > > search =
				wrapper.getSpatialSearch( timepoint );
		search.search( to );

		final OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance >> v = search.nearestNeighbor();
		if ( null == v || search.nearestNeighborSquareDistance() > v.get().getBoundingSphereRadiusSquared() )
			return null;

		return v;
	}

	/*
	 * PRIVATE METHODS
	 */
	
	/**
	 * Places the data coordinates corresponding to the specified point (in view
	 * coordinates) in to the {@link #to} field.
	 * 
	 * @param ml
	 * @param state
	 */
	private final void viewToData( final Point ml, final ViewerState state )
	{
		state.getViewerTransform( t );
		from.setPosition( ml.getX(), 0 );
		from.setPosition( ml.getY(), 1 );
		from.setPosition( 0., 2 );
		t.applyInverse( to, from );
	}

	/*
	 * INNER CLASSES
	 */

	private class SpotMover
	{
		/**
		 * The spot currently moved by the mouse.
		 */
		private SpotCovariance movedSpot;

		private final char key;

		private boolean moving = false;

		public SpotMover( final char key )
		{
			this.key = key;
		}

		private void grab()
		{
			if ( moving )
				return;

			final OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance >> v = getSpotUnderMouse();
			if ( null == v )
				return;

			moving = true;
			movedSpot = v.get();
		}

		private void release()
		{
			if ( movedSpot == null )
				return;

			wrapper.updateSearchFor( movedSpot.getTimepointId() );
			moving = false;
			movedSpot = null;
		}
		
		private void move()
		{
			if ( !moving || movedSpot == null )
				return;

			viewer.getGlobalMouseCoordinates( to );
			movedSpot.setX( to.getDoublePosition( 0 ) );
			movedSpot.setY( to.getDoublePosition( 1 ) );
			movedSpot.setZ( to.getDoublePosition( 2 ) );
		}
	}

	private class LinkedSpotCreator
	{
		private final double[] loc = new double[ 3 ];

		private final char key;

		private final GraphIdBimap< SpotCovariance, Link< SpotCovariance >> idBimap;

		private LinkedSpotCreator( final char key )
		{
			this.key = key;
			this.idBimap = model.getGraph().getIdBimap();
		}

		private void create()
		{
			/*
			 * Check if a spot is within radius.
			 */

			final OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance >> v = getSpotUnderMouse();
			if ( null == v )
				return;
			overlay.ghostSpot = v;
			final TrackSchemeVertex tsgs = v.getTrackSchemeVertex();

			/*
			 * Move to next timepoint.
			 */

			final ViewerState state = viewer.getState();
			final int timepoint = state.getCurrentTimepoint();
			final int newTimePoint = timepoint + 1;
			viewer.setTimepoint( newTimePoint );

			/*
			 * Create spot and link there.
			 */

			viewer.getGlobalMouseCoordinates( to );
			to.localize( loc );
			final SpotCovariance spot = model.createSpot( newTimePoint, loc, radius, ref );
			final int id = idBimap.getVertexId( spot );
			final TrackSchemeVertex tsgt = trackscheme.getGraph().addVertex().init( id, "Created!", newTimePoint, false );
			wrapper.add( newTimePoint, id );
			trackscheme.relayout();

			model.createLink( v.get(), spot );
			trackscheme.getGraph().addEdge( tsgs, tsgt );

			repaint();

			spotMover.movedSpot = spot;
			spotMover.moving = true;

		}

		private void release()
		{
			spotMover.release();
			overlay.ghostSpot = null;
			repaint();
		}
	}

	private class LinkCreator
	{
		private final char key;

		private boolean creating = false;

		private OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance >> target;

		private OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance >> source;

		private LinkCreator( final char key )
		{
			this.key = key;
		}

		private void create()
		{
			/*
			 * Check if a spot is within radius.
			 */

			final OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance >> v = getSpotUnderMouse();
			if ( null == v )
				return;
			overlay.ghostSpot = v;
			overlay.ghostLink = true;
			viewer.getGlobalMouseCoordinates( to );
			to.localize( overlay.lPos2 );
			creating = true;
			overlay.ghostLink = true;
			source = v;
			target = null;
			viewer.setTimepoint( viewer.getState().getCurrentTimepoint() + 1 );
		}

		private void move()
		{
			if ( !creating )
				return;

			viewer.getGlobalMouseCoordinates( to );
			final int timepoint = viewer.getState().getCurrentTimepoint();
			final SpatialSearch< OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance >>> search = wrapper.getSpatialSearch( timepoint );
			search.search( to );

			final OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance >> v = search.nearestNeighbor();
			if ( v == null )
			{
				to.localize( overlay.lPos2 );
				return;
			}

			target = v;
			final double sqDist = search.nearestNeighborSquareDistance();
			final double snapSqDist = target.get().getBoundingSphereRadiusSquared();
			if ( sqDist < snapSqDist )
			{
				target.localize( overlay.lPos2 );
			}
			else
			{
				target = null;
				to.localize( overlay.lPos2 );
			}
		}

		private void release()
		{
			if ( null != target )
			{
				model.createLink( source.get(), target.get() );
				trackscheme.getGraph().addEdge( source.getTrackSchemeVertex(), target.getTrackSchemeVertex() );
				target = null;
			}
			creating = false;
			source = null;
			overlay.ghostLink = false;
			overlay.ghostSpot = null;
			repaint();
		}

	}

	/*
	 * ACTIONS
	 */

	private class ChangeSpotRadiusAction extends AbstractNamedDefaultKeyStrokeAction
	{
		private static final long serialVersionUID = 1L;

		private final double factor;

		public ChangeSpotRadiusAction( final boolean fast, final boolean increase )
		{
			super(
					fast ?
							increase ? "increaseSpotRadiusFast" : "decreaseSpotRadiusFast"
							:
							increase ? "increaseSpotRadius" : "decreaseSpotRadius",
					fast ?
							increase ? KeyStroke.getKeyStroke( "shift E" ) : KeyStroke.getKeyStroke( "shift Q" )
							:
							increase ? KeyStroke.getKeyStroke( 'e' ) : KeyStroke.getKeyStroke( 'q' ) );

			this.factor = increase ?
					fast ? 10. : 1.
					:
					fast ? -5. : -1.;
		}

		@Override
		public void actionPerformed( final ActionEvent e )
		{
			final OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance >> v = getSpotUnderMouse();
			if ( null == v )
				return;

			final SpotCovariance spot = v.get();

			double rad = Math.sqrt( spot.getBoundingSphereRadiusSquared() );
			rad += factor * RADIUS_CHANGE_FACTOR * rad;

			if ( rad < MIN_RADIUS )
			{
				rad = MIN_RADIUS;
			}

			radius = rad;
			spot.editRadius( radius );
			viewer.getDisplay().repaint();
		}

	}
	
	private class CreateSpotAction extends AbstractNamedDefaultKeyStrokeAction
	{
		private static final long serialVersionUID = 1L;

		private final double[] loc = new double[ 3 ];

		private final GraphIdBimap< SpotCovariance, Link< SpotCovariance >> idBimap;

		public CreateSpotAction()
		{
			super( "createSpot", KeyStroke.getKeyStroke( 'a' ) );
			this.idBimap = model.getGraph().getIdBimap();
		}

		@Override
		public void actionPerformed( final ActionEvent e )
		{
			viewer.getGlobalMouseCoordinates( to );
			to.localize( loc );
			final ViewerState state = viewer.getState();
			final int timepoint = state.getCurrentTimepoint();
			final SpotCovariance spot = model.createSpot( timepoint, loc, radius, ref );

			/*
			 * FIXME How to keep this in sync with the wrapper and TrackScheme?
			 * Discuss with @tpietzsch. Do this manually here? Have the model
			 * implements Listenable? Right now I am doing this manually.
			 */

			final int id = idBimap.getVertexId( spot );
			trackscheme.getGraph().addVertex().init( id, "Created!", timepoint, false );
			wrapper.add( timepoint, id );
			trackscheme.relayout();
			repaint();
		}
	}

	/*
	 * OVERLAY FOR CURRENT EDIT.
	 */

	@Override
	public final void drawOverlays( final Graphics g )
	{
		overlay.drawOverlays( g );
	}

	@Override
	public void setCanvasSize( final int width, final int height )
	{}

	private class GhostOverlay implements OverlayRenderer
	{

		private OverlayVertexWrapper< SpotCovariance, Link< SpotCovariance > > ghostSpot;

		private final AffineTransform3D transform = new AffineTransform3D();

		private final double[] lPos1 = new double[ 3 ];

		private final double[] gPos1 = new double[ 3 ];

		private final double[] lPos2 = new double[ 3 ];

		private final double[] gPos2 = new double[ 3 ];

		private final double[][] S = new double[ 3 ][ 3 ];

		private final double[][] T = new double[ 3 ][ 3 ];

		private final double[][] TS = new double[ 3 ][ 3 ];

		private final double nSigmas = SpotCovariance.nSigmas;

		private final BasicStroke stroke;

		private final Color color = Color.WHITE;

		private boolean ghostLink = false;


		public GhostOverlay()
		{
			final float dash[] = { 4.0f };
			stroke = new BasicStroke( 1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f );
		}


		@Override
		public void drawOverlays( final Graphics g )
		{
			if ( null == ghostSpot && !ghostLink )
				return;

			final Graphics2D graphics = ( Graphics2D ) g;
			graphics.setColor( color );
			graphics.setStroke( stroke );
			final AffineTransform torig = graphics.getTransform();

			viewer.getState().getViewerTransform( transform );

			if ( null != ghostSpot )
			{
				ghostSpot.get().localize( lPos1 );
				transform.apply( lPos1, gPos1 );

				ghostSpot.get().getCovariance( S );
				for ( int r = 0; r < 3; ++r )
					for ( int c = 0; c < 3; ++c )
						T[ r ][ c ] = transform.get( r, c );

				LinAlgHelpers.mult( T, S, TS );
				LinAlgHelpers.multABT( TS, T, S );
				// will not return orthogonal V.
				S[ 0 ][ 1 ] = S[ 1 ][ 0 ];
				S[ 0 ][ 2 ] = S[ 2 ][ 0 ];
				S[ 1 ][ 2 ] = S[ 2 ][ 1 ];

				final double[][] S2 = new double[ 2 ][ 2 ];
				for ( int r = 0; r < 2; ++r )
					for ( int c = 0; c < 2; ++c )
						S2[ r ][ c ] = S[ r ][ c ];
				final EigenvalueDecomposition eig2 = new Matrix( S2 ).eig();
				final double[] eigVals2 = eig2.getRealEigenvalues();
				final double w = nSigmas * Math.sqrt( eigVals2[ 0 ] );
				final double h = nSigmas * Math.sqrt( eigVals2[ 1 ] );
				final Matrix V2 = eig2.getV();
				final double c = V2.getArray()[ 0 ][ 0 ];
				final double s = V2.getArray()[ 1 ][ 0 ];
				final double theta = Math.atan2( s, c );
				graphics.translate( gPos1[ 0 ], gPos1[ 1 ] );
				graphics.rotate( theta );
				graphics.draw( new Ellipse2D.Double( -w, -h, 2 * w, 2 * h ) );
				graphics.setTransform( torig );
			}
			
			if ( ghostLink )
			{
				final int x1 = ( int ) gPos1[ 0 ];
				final int y1 = ( int ) gPos1[ 1 ];
				
				transform.apply( lPos2, gPos2 );
				final int x2 = ( int ) gPos2[ 0 ];
				final int y2 = ( int ) gPos2[ 1 ];
				
				graphics.drawLine( x1, y1, x2, y2 );
			}

		}

		@Override
		public void setCanvasSize( final int width, final int height )
		{}
	}


}
