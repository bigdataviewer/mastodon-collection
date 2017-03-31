package org.mastodon.pool;

import static org.mastodon.pool.ByteUtils.INT_SIZE;

public class TestObject extends PoolObject< TestObject, TestObjectPool, ByteMappedElement >
{
	protected static final int ID_OFFSET = 0;

	protected static final int SIZE_IN_BYTES = ID_OFFSET + INT_SIZE;

	TestObject( final TestObjectPool pool )
	{
		super( pool );
	}

	public TestObject init( final int id )
	{
		setId( id );
		return this;
	}

	@Override
	protected void setToUninitializedState()
	{
		setId( -1 );
	}

	public int getId()
	{
		return access.getInt( ID_OFFSET );
	}

	public void setId( final int id )
	{
		access.putInt( id, ID_OFFSET );
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "TestObject(" );
		sb.append( getId() );
		sb.append( ")" );
		return sb.toString();
	}
}
