package org.sonatype.nexus.plugins.ruby;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.sonatype.sisu.filetasks.builder.FileRef.file;
import static org.sonatype.sisu.filetasks.builder.FileRef.path;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import javax.inject.Inject;

import org.hamcrest.Matcher;
import org.junit.runners.Parameterized.Parameters;
import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.client.core.subsystem.content.Content;
import org.sonatype.nexus.client.core.subsystem.content.Location;
import org.sonatype.nexus.client.rest.BaseUrl;
import org.sonatype.nexus.ruby.BundleRunner;
import org.sonatype.nexus.ruby.GemRunner;
import org.sonatype.nexus.ruby.JRubyScriptingContainer;
import org.sonatype.nexus.testsuite.support.NexusRunningITSupport;
import org.sonatype.sisu.filetasks.FileTaskBuilder;
import org.sonatype.sisu.goodies.common.Time;

public abstract class GemsNexusRunningITSupport extends NexusRunningITSupport {

    @Parameters
    public static Collection<String[]> data() {
      String[][] data = new String[][] { { "gemsproxy" },
             { "gemshost" }, 
             { "gemshostgroup" }, 
             { "gemsproxygroup" }, 
             { "gemsgroup" } };
      return Arrays.asList(data);
    }
    
    @Inject
    private FileTaskBuilder overlays;
    
    protected GemRunner gemRunner;
    
    protected final JRubyScriptingContainer ruby;
    protected final String repoId;

    private BundleRunner bundleRunner;

    public GemsNexusRunningITSupport( String nexusBundleCoordinates, String repoId ) {
        super( nexusBundleCoordinates );
        this.repoId = repoId;
        this.ruby = new ITestJRubyScriptingContainer();
    }

    public GemsNexusRunningITSupport( String repoId ) {
        this( null, repoId );
     }

    protected GemRunner gemRunner() {
        if ( gemRunner == null )
        {
            gemRunner = createGemRunner();
        }
        return gemRunner;
    }

    protected BundleRunner bundleRunner() {
        if ( bundleRunner == null )
        {
            bundleRunner = createBundleRunner();
        }
        return bundleRunner;
    }

    GemRunner createGemRunner() {
        BaseUrl base = client().getConnectionInfo().getBaseUrl();
        return new GemRunner( ruby,  base.toString() + "content/repositories/" );
    }
    
    BundleRunner createBundleRunner() {
        return new BundleRunner( ruby );
    }

    protected File assertFileDownload(String name, Matcher<Boolean> matcher) {
        File nil = new File( util.createTempDir(), "null" );
        
        try
        {
            client().getSubsystem( Content.class ).download( new Location( repoId, name ), nil );
        }
        catch (IOException e)
        {
            // just ignore it and let matcher test
        }
        assertThat( nil.exists(), matcher );
    
        nil.deleteOnExit();
        
        return nil;
    }

    @Override
    protected NexusBundleConfiguration configureNexus(final NexusBundleConfiguration configuration) {
        return configuration
            .addPlugins(
                artifactResolver().resolvePluginFromDependencyManagement(
                    "org.sonatype.nexus.plugins", "nexus-ruby-plugin"
                )
            )
            .addOverlays(
                overlays.copy()
                    .file( file( testData().resolveFile( "nexus.xml" ) ) )
                    .to().directory( path( "sonatype-work/nexus/conf" ) )
            )
            .setStartTimeout( Time.minutes( 2 ).toSecondsI() )
            .setLogLevel( "DEBUG" )
            .setPort( 4711 );
    }

}