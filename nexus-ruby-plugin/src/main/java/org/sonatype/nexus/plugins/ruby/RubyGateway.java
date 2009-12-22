package org.sonatype.nexus.plugins.ruby;

import java.io.File;
import java.io.IOException;

import org.apache.maven.model.Model;
import org.sonatype.nexus.ruby.MavenArtifact;

/**
 * This component is the central "ruby interaction" point, and is meant to focus all "ruby related" calls to make it
 * easy to swap out and use proper stuff instead. What we have now is POC nexus-ruby-tools, and gemGenerateIndexes is
 * not implemented. The "proper" stuff should use JRuby and invoke the proper Gem:: classes doing the actual work.
 * 
 * @author cstamas
 */
public interface RubyGateway
{
    /**
     * Just builds proper GEM filename out from information in POM object.
     * 
     * @param pom Pom of the artifact.
     * @return
     */
    String getGemFileName( Model pom );

    /**
     * Does Maven2 artifact conversion into Gem and writes the Gem to the target file. The file written to target should
     * be a complete Ruby Gem.
     * 
     * @param mart
     * @param target
     * @throws IOException
     */
    void createGemFromArtifact( MavenArtifact mart, File target )
        throws IOException;

    /**
     * Creates a Gem::Specification from the Maven2 artifact Pom and writes it into the designated target File as Yaml
     * serialized file (for latter indexer use, this is the gemspec of "lazy" Gems).
     * 
     * @param pom
     * @param target
     * @throws IOException
     */
    void createAndWriteGemspec( Model pom, File target )
        throws IOException;

    /**
     * Invokes "gem generate_index --directory=${basedir}". Should do the same as the CLI command written above.
     * 
     * @param basedir
     */
    void gemGenerateIndexes( File basedir );
}
