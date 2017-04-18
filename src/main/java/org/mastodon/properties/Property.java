package org.mastodon.properties;

import org.mastodon.properties.undo.PropertyUndoRedoStack;

/**
 * A property of objects of type {@code O} which listeners can subscribe to to
 * be notified about changes.
 *
 * @param <O>
 *
 * @author Tobias Pietzsch
 */
public interface Property< O >
{
	/**
	 * Register a {@link BeforePropertyChangeListener} that will be notified before the
	 * value of this property is changed. Specifically,
	 * {@link BeforePropertyChangeListener#beforePropertyChange(PropertyMap, Object)
	 * beforePropertyChange} is triggered as the first step of
	 * {@link #set(Object, Object)} and {@link #remove(Object)}.
	 *
	 * @param listener
	 *            the listener to register.
	 * @return {@code true} if the listener was successfully registered.
	 *         {@code false} if it was already registered.
	 */
	public boolean addBeforePropertyChangeListener( final BeforePropertyChangeListener< O > listener );

	/**
	 * Removes the specified {@link BeforePropertyChangeListener} from the set of
	 * listeners.
	 *
	 * @param listener
	 *            the listener to remove.
	 * @return {@code true} if the listener was present in the listeners of this
	 *         model and was successfully removed.
	 */
	public boolean removeBeforePropertyChangeListener( final BeforePropertyChangeListener< O > listener );

	/**
	 * Register a {@link PropertyChangeListener} that will be notified when the
	 * value of this property was changed. Specifically,
	 * {@link PropertyChangeListener#propertyChanged(PropertyMap, Object)
	 * propertyChanged} is triggered as the last step of
	 * {@link #set(Object, Object)} and {@link #remove(Object)}.
	 *
	 * @param listener
	 *            the listener to register.
	 * @return {@code true} if the listener was successfully registered.
	 *         {@code false} if it was already registered.
	 */
	public boolean addPropertyChangeListener( final PropertyChangeListener< O > listener );

	/**
	 * Removes the specified {@link PropertyChangeListener} from the set of
	 * listeners.
	 *
	 * @param listener
	 *            the listener to remove.
	 * @return {@code true} if the listener was present in the listeners of this
	 *         model and was successfully removed.
	 */
	public boolean removePropertyChangeListener( final PropertyChangeListener< O > listener );


	/**
	 * Pause sending events to {@link BeforePropertyChangeListener}s and
	 * {@link PropertyChangeListener}s.
	 */
	public void pauseListeners();

	/**
	 * Resume sending events to {@link BeforePropertyChangeListener}s and
	 * {@link PropertyChangeListener}s.
	 */
	public void resumeListeners();

	/**
	 * Optional.
	 */
	public default PropertyUndoRedoStack< O > createUndoRedoStack()
	{
		throw new UnsupportedOperationException();
	}
}