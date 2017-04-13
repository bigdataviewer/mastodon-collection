package org.mastodon.pooldemo;

import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.ByteMappedElementArray;
import org.mastodon.pool.MemPool;
import org.mastodon.pool.Pool;
import org.mastodon.pool.PoolObject;
import org.mastodon.pool.SingleArrayMemPool;

public class Vector3Pool extends Pool< Vector3, ByteMappedElement >
{
	public Vector3Pool( final int initialCapacity )
	{
		this( initialCapacity, new Vector3Factory() );
	}

	@Override
	public Vector3 create( final Vector3 obj )
	{
		return super.create( obj );
	}

	public Vector3 create()
	{
		return super.create( createRef() );
	}

	@Override
	public void delete( final Vector3 obj )
	{
		super.delete( obj );
	}

	private Vector3Pool( final int initialCapacity, final Vector3Pool.Vector3Factory f )
	{
		super( initialCapacity, f );
		f.pool = this;
	}

	private static class Vector3Factory implements PoolObject.Factory< Vector3, ByteMappedElement >
	{
		private Vector3Pool pool;

		@Override
		public int getSizeInBytes()
		{
			return Vector3.SIZE_IN_BYTES;
		}

		@Override
		public Vector3 createEmptyRef()
		{
			return new Vector3( pool );
		}

		@Override
		public MemPool.Factory< ByteMappedElement > getMemPoolFactory()
		{
			return SingleArrayMemPool.factory( ByteMappedElementArray.factory );
		}

		@Override
		public Class< Vector3 > getRefClass()
		{
			return Vector3.class;
		}
	};
}
