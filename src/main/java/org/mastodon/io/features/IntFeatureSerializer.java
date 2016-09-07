package org.mastodon.io.features;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.io.ObjectToFileIdMap;
import org.mastodon.io.features.RawFeatureIO.Serializer;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

public class IntFeatureSerializer< O > implements Serializer< TObjectIntMap< O >, O >
{
	@Override
	public void writeFeatureMap(
			final ObjectToFileIdMap< O > idmap,
			final TObjectIntMap< O > featureMap,
			final ObjectOutputStream oos )
					throws IOException
	{
		final TIntIntHashMap fmap = new TIntIntHashMap();
		featureMap.forEachEntry( ( final O key, final int value ) -> {
			fmap.put( idmap.getId( key ), value );
			return true;
		} );
		oos.writeObject( fmap );
	}

	@Override
	public void readFeatureMap(
			final FileIdToObjectMap< O > idmap,
			final TObjectIntMap< O > featureMap,
			final ObjectInputStream ois )
					throws IOException, ClassNotFoundException
	{
		final TIntIntHashMap fmap = ( TIntIntHashMap ) ois.readObject();
		featureMap.clear();
		final O ref = idmap.createRef();
		fmap.forEachEntry( ( final int key, final int value ) -> {
			featureMap.put( idmap.getObject( key, ref ), value );
			return true;
		} );
		idmap.releaseRef( ref );
	}
}
