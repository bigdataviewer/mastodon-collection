package org.mastodon.labels;

import gnu.trove.impl.Constants;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The LabelMapping maps a set of labels of an object to an index value
 * which can be more compactly stored than the set of labels. It provides an
 * {@link #intern(Set)} function that supplies a canonical object for each set
 * of labels, and functions
 * {@link #addLabelToSetAtIndex(Object, int)},
 * {@link #removeLabelFromSetAtIndex(Object, int)} for efficiently adding and
 * removing labels to the set at a given index value.
 *
 * @param <T>
 *            the desired type of the labels, for instance {@link Integer}
 *            or {@link String}.
 *
 * @author Lee Kamentsky
 * @author Tobias Pietzsch
 */
class LabelMapping< T >
{
	private static final int INT_NO_ENTRY_VALUE = -1;

	/**
	 * Maximum number of distinct label sets that can be represented by this
	 * mapping.
	 */
	private static final int MAX_NUM_LABEL_SETS = Integer.MAX_VALUE;

	/**
	 * TODO
	 */
	private final HashMap< Set< T >, InternedSet< T > > internedSets;

	/**
	 * Maps indices to {@link InternedSet} (canonical label sets).
	 * {@code setsByIndex.get( i ).index == i} holds.
	 */
	private final ArrayList< InternedSet< T > > setsByIndex;

	/**
	 * Lookup tables for adding labels. Assume that by adding label <em>L</em>
	 * to label set <em>S</em> we obtain <em>S' = S &cup; {L}</em>.
	 * {@code addMapsByIndex} contains at index of set <em>S</em> a map from
	 * <em>L</em> to index of <em>S'</em>.
	 *
	 * <p>
	 * When a new <em>(L,S)</em> combination occurs for the first time in
	 * {@link #addLabelToSetAtIndex(Object, int)}, it is added to the lookup
	 * table.
	 */
	private final ArrayList< TObjectIntMap< T > > addMapsByIndex;

	/**
	 * Lookup tables for removing labels. Assume that by removing label <em>L</em>
	 * from label set <em>S</em> we obtain <em>S' = S &setminus; {L}</em>.
	 * {@code subMapsByIndex} contains at index of set <em>S</em> a map from
	 * <em>L</em> to index of <em>S'</em>.
	 *
	 * <p>
	 * When a new <em>(L,S)</em> combination occurs for the first time in
	 * {@link #removeLabelFromSetAtIndex(Object, int)}, it is added to the lookup
	 * table.
	 */
	private final ArrayList< TObjectIntMap< T > > subMapsByIndex;

	/**
	 * the empty label set.
	 */
	private final InternedSet< T > theEmptySet;

	/**
	 * Create a new {@code LabelMapping} that maps label sets to {@code int}s.
	 */
	LabelMapping()
	{
		internedSets = new HashMap< Set< T >, InternedSet< T > >();
		setsByIndex = new ArrayList< InternedSet< T > >();
		addMapsByIndex = new ArrayList< TObjectIntMap< T > >();
		subMapsByIndex = new ArrayList< TObjectIntMap< T > >();

		final HashSet< T > background = new HashSet< T >( 0 );
		theEmptySet = intern( background );
	}

	/**
	 * Canonical representative for a label set. Contains a label set and the
	 * index to which it is mapped.
	 */
	static class InternedSet< T >
	{
		final Set< T > set;

		final int hashCode;

		final int index;

		public InternedSet( final Set< T > set, final int index )
		{
			this.set = set;
			this.hashCode = set.hashCode();
			this.index = index;
		}

		public Set< T > getSet()
		{
			return set;
		}

		@Override
		public int hashCode()
		{
			return hashCode;
		}

		@Override
		public boolean equals( final Object obj )
		{
			return obj == this;
		}
	}

	InternedSet< T > emptySet()
	{
		return theEmptySet;
	}

	/**
	 * Return the index value of the given set.
	 */
	int indexOf( final Set< T > key )
	{
		return intern( key ).index;
	}

	/**
	 * Return the canonical set for the given index value.
	 */
	InternedSet< T > setAtIndex( final int index )
	{
		return setsByIndex.get( index );
	}

	/**
	 * Return the canonical set for the given label set.
	 */
	InternedSet< T > intern( final Set< T > src )
	{
		InternedSet< T > interned = internedSets.get( src );
		if ( interned != null )
			return interned;

		synchronized ( this )
		{
			interned = internedSets.get( src );
			if ( interned != null )
				return interned;

			final int intIndex = setsByIndex.size();
			if ( intIndex > MAX_NUM_LABEL_SETS )
				throw new AssertionError( String.format( "Too many labels (or types of multiply-labeled pixels): %d maximum", intIndex ) );

			final HashSet< T > srcCopy = new HashSet< T >( src );
			interned = new InternedSet< T >( srcCopy, intIndex );
			setsByIndex.add( interned );
			addMapsByIndex.add( new TObjectIntHashMap< T >( Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, INT_NO_ENTRY_VALUE ) );
			subMapsByIndex.add( new TObjectIntHashMap< T >( Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, INT_NO_ENTRY_VALUE ) );
			internedSets.put( srcCopy, interned );
			return interned;
		}
	}

	/**
	 * Get the canonical set obtained by adding {@code label} to the
	 * {@link #setAtIndex(int) set at index} {@code index}.
	 */
	InternedSet< T > addLabelToSetAtIndex( final T label, final int index )
	{
		final TObjectIntMap< T > addMap = addMapsByIndex.get( index );
		int i = addMap.get( label );
		if ( i != INT_NO_ENTRY_VALUE )
			return setsByIndex.get( i );

		synchronized ( this )
		{
			i = addMap.get( label );
			if ( i != INT_NO_ENTRY_VALUE )
				return setsByIndex.get( i );

			final HashSet< T > set = new HashSet< T >( setsByIndex.get( index ).set );
			set.add( label );
			final InternedSet< T > interned = intern( set );
			addMap.put( label, interned.index );
			return interned;
		}
	}

	/**
	 * Get the canonical set obtained by removing {@code label} from the
	 * {@link #setAtIndex(int) set at index} {@code index}.
	 */
	InternedSet< T > removeLabelFromSetAtIndex( final T label, final int index )
	{
		final TObjectIntMap< T > subMap = subMapsByIndex.get( index );
		int i = subMap.get( label );
		if ( i != INT_NO_ENTRY_VALUE )
			return setsByIndex.get( i );

		synchronized ( this )
		{
			i = subMap.get( label );
			if ( i != INT_NO_ENTRY_VALUE )
				return setsByIndex.get( i );

			final HashSet< T > set = new HashSet< T >( setsByIndex.get( index ).set );
			set.remove( label );
			final InternedSet< T > interned = intern( set );
			subMap.put( label, interned.index );
			return interned;
		}
	}

	/**
	 * Returns the number of indexed labeling sets
	 */
	public int numSets()
	{
		return setsByIndex.size();
	}

	/**
	 * Returns the (unmodifiable) set of labels for the given index value.
	 */
	// TODO: cache unmodifiable sets (in InternedSet)?
	public Set< T > labelsAtIndex( final int index )
	{
		return Collections.unmodifiableSet( setsByIndex.get( index ).set );
	}

	/**
	 * Return the set of all labels defined in this {@code LabelMapping}.
	 */
	// TODO: build only once (while adding labels).
	public Set< T > getLabels()
	{
		final HashSet< T > result = new HashSet< T >();
		for ( final InternedSet< T > instance : setsByIndex )
		{
			for ( final T label : instance.set )
			{
				result.add( label );
			}
		}
		return result;
	}

	/**
	 * Internals. Can be derived for implementing de/serialisation of the
	 * {@link LabelMapping}.
	 */
	public static class SerialisationAccess< T >
	{
		private final LabelMapping< T > labelMapping;

		protected SerialisationAccess( final LabelMapping< T > labelMapping )
		{
			this.labelMapping = labelMapping;
		}

		protected List< Set< T > > getLabelSets()
		{
			final ArrayList< Set< T > > labelSets= new ArrayList<>( labelMapping.numSets() );
			for ( final InternedSet< T > interned : labelMapping.setsByIndex )
				labelSets.add( interned.getSet() );
			return labelSets;
		}

		protected void setLabelSets( final List< Set< T > > labelSets )
		{
			if ( labelSets.isEmpty() )
				throw new IllegalArgumentException( "expected non-empty list of label-sets" );

			if ( !labelSets.get( 0 ).isEmpty() )
				throw new IllegalArgumentException( "label-set at index 0 expected to be the empty label set" );

			// clear everything
			labelMapping.internedSets.clear();
			labelMapping.setsByIndex.clear();
			labelMapping.addMapsByIndex.clear();
			labelMapping.subMapsByIndex.clear();

			// add back the empty set
			final InternedSet< T > theEmptySet = labelMapping.theEmptySet;
			labelMapping.setsByIndex.add( theEmptySet );
			labelMapping.addMapsByIndex.add( new TObjectIntHashMap< T >( Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, INT_NO_ENTRY_VALUE ) );
			labelMapping.subMapsByIndex.add( new TObjectIntHashMap< T >( Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, INT_NO_ENTRY_VALUE ) );
			labelMapping.internedSets.put( theEmptySet.getSet(), theEmptySet );

			// add remaining label sets
			for ( int i = 1; i < labelSets.size(); ++i )
			{
				final Set< T > set = labelSets.get( i );
				final InternedSet< T > interned = labelMapping.intern( set );
				if ( interned.index != i )
					throw new IllegalArgumentException( "no duplicates allowed in list of label-sets" );
			}
		}
	}
}