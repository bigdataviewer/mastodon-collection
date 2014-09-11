package pietzsch.mappedelementpool;

public class ByteUtils
{
	public static final int INT_SIZE = 4;

	public static final int LONG_SIZE = 8;

	public static final int FLOAT_SIZE = 4;

	public static final int DOUBLE_SIZE = 8;

	public static void putInt( final int value, final byte[] array, final int offset )
	{
		array[ offset ] = ( byte ) ( 0xff & ( value >> 24 ) );
		array[ offset + 1 ] = ( byte ) ( 0xff & ( value >> 16 ) );
		array[ offset + 2 ] = ( byte ) ( 0xff & ( value >> 8 ) );
		array[ offset + 3 ] = ( byte ) ( 0xff & value );
	}

	public static int getInt( final byte[] array, final int offset )
	{
		return ( ( array[ offset ] & 0xff ) << 24 ) |
				( ( array[ offset + 1 ] & 0xff ) << 16 ) |
				( ( array[ offset + 2 ] & 0xff ) << 8 ) |
				( array[ offset + 3 ] & 0xff );
	}

	public static void putLong( final long value, final byte[] array, final int offset )
	{
		array[ offset ] = ( byte ) ( 0xff & ( value >> 56 ) );
		array[ offset + 1 ] = ( byte ) ( 0xff & ( value >> 48 ) );
		array[ offset + 2 ] = ( byte ) ( 0xff & ( value >> 40 ) );
		array[ offset + 3 ] = ( byte ) ( 0xff & ( value >> 32 ) );
		array[ offset + 4 ] = ( byte ) ( 0xff & ( value >> 24 ) );
		array[ offset + 5 ] = ( byte ) ( 0xff & ( value >> 16 ) );
		array[ offset + 6 ] = ( byte ) ( 0xff & ( value >> 8 ) );
		array[ offset + 7 ] = ( byte ) ( 0xff & value );
	}

	public static long getLong( final byte[] array, final int offset )
	{
		return  ( ( long ) ( array[ offset ] & 0xff ) << 56 ) |
				( ( long ) ( array[ offset + 1 ] & 0xff ) << 48 ) |
				( ( long ) ( array[ offset + 2 ] & 0xff ) << 40 ) |
				( ( long ) ( array[ offset + 3 ] & 0xff ) << 32 ) |
				( ( long ) ( array[ offset + 4 ] & 0xff ) << 24 ) |
				( ( long ) ( array[ offset + 5 ] & 0xff ) << 16 ) |
				( ( long ) ( array[ offset + 6 ] & 0xff ) << 8 ) |
				( array[ offset + 7 ] & 0xff );
	}

	public static void putFloat( final float value, final byte[] array, final int offset )
	{
		putInt( Float.floatToRawIntBits( value ), array, offset );
	}

	public static float getFloat( final byte[] array, final int offset )
	{
		return( Float.intBitsToFloat( getInt( array, offset ) ) );
	}

	public static void putDouble( final double value, final byte[] array, final int offset )
	{
		putLong( Double.doubleToRawLongBits( value ), array, offset );
	}

	public static double getDouble( final byte[] array, final int offset )
	{
		return( Double.longBitsToDouble( getLong( array, offset ) ) );
	}
}
