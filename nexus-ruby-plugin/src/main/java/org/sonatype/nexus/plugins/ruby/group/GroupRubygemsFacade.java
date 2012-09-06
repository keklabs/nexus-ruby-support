package org.sonatype.nexus.plugins.ruby.group;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.sonatype.nexus.plugins.ruby.RubyGateway;
import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.plugins.ruby.fs.AbstractRubygemsFacade;
import org.sonatype.nexus.plugins.ruby.fs.GzipContentGenerator;
import org.sonatype.nexus.plugins.ruby.fs.RubyLocalRepositoryStorage;
import org.sonatype.nexus.plugins.ruby.fs.SpecsIndexType;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

public class GroupRubygemsFacade extends AbstractRubygemsFacade {

    public GroupRubygemsFacade( RubyGateway gateway, RubyRepository repository )
    {
        super( gateway, repository );
    }

    @Override
    public StorageFileItem retrieveSpecsIndex( RubyLocalRepositoryStorage storage, 
            SpecsIndexType type, boolean gzipped ) 
            throws ItemNotFoundException, LocalStorageException
    {
        StorageFileItem result = storage.retrieveSpecsIndex( repository, type, false );

        if ( gzipped )
        {
            result.setContentGeneratorId( GzipContentGenerator.ID );
        }
        return result;
    }

    @Override
    public void mergeSpecsIndex( RubyLocalRepositoryStorage storage, SpecsIndexType type,
            StorageItem localItem, List<StorageItem> specsItems )
            throws UnsupportedStorageOperationException, LocalStorageException, IOException {
        List<InputStream> streams = new LinkedList<InputStream>();
        for( StorageItem item: specsItems )
        {
            streams.add( ( (StorageFileItem) item ).getInputStream() );
        }
        InputStream is = localItem == null ? 
                null :
                ( (StorageFileItem) localItem ).getInputStream();
        storeSpecsIndex( repository, 
                storage, 
                type,
                gateway.mergeSpecs( is, streams ) );
    }   
}