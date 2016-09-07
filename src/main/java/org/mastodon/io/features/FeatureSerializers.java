package org.mastodon.io.features;

import java.util.HashMap;

import org.mastodon.features.Feature;
import org.mastodon.io.features.RawFeatureIO.Serializer;

public class FeatureSerializers
{
	private static final HashMap< Feature< ?, ?, ? >, Serializer< ?, ? > > serializers = new HashMap<>();

	@SuppressWarnings( "unchecked" )
	public static < M, O > Serializer< M, O > get( final Feature< M, O, ? > feature )
	{
		final Serializer< M, O > serializer = ( Serializer< M, O > ) serializers.get( feature );
		return serializer;
	}

	public static < M, O > void put( final Feature< M, O, ? > feature, final Serializer< M, O > serializer )
	{
		serializers.put( feature, serializer );
	}
}
