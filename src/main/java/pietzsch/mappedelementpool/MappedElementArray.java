package pietzsch.mappedelementpool;

/**
 * TODO: javadoc
 *
 * @param <T>
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public interface MappedElementArray< T extends MappedElement >
{
	public long size();

	public long maxSize();

	public T createAccess();

	public void updateAccess( final T access, final long index );

	public static interface Factory< A > // A extends MappedElementArray< T >
	{
		public A createArray( final long numElements, final int bytesPerElement );

		public A createArrayAndCopy( final long numElements, final int bytesPerElement, final A copyFrom );
	}
}
