package confluencemavenplugin.mojo;

import java.io.File;
import java.net.MalformedURLException;

import org.apache.maven.plugin.*;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.settings.*;
import org.codehaus.swizzle.confluence.Confluence;

import confluencemavenplugin.*;

@Mojo(
		name="deploy",
		defaultPhase=LifecyclePhase.DEPLOY,
		requiresOnline=true,
		requiresProject=true
)
public class Deploy extends AbstractMojo {

	@Component
	private Settings settings;
	
	@Parameter(name="outputDirectory", defaultValue="${project.build.directory}/confluence")
	private File outputDirectory;

	@Parameter(name="serverId", required=true)
	private String serverId;
	
	@Parameter(name="endpoint", required=true)
	private String endpoint;

	@Parameter(name="spaceKey", required=true)
	private String spaceKey;

	@Parameter(name="readmePageId", required=true)
	private String readmePageId;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info(getClass().getName() + "execute***");

		Server server = settings.getServer(serverId);
		if (server == null)
			throw new MojoFailureException("Unable to find any server identified by \"" + serverId + "\" in your settings.xml");

//		Confluence confluence = new Confluence(
//				new Confluence.Credentials(server.getUsername(), server.getPassword()), 
//				endpoint, 
//				spaceKey
//		);

		ConfluenceMavenPlugin plugin = new ConfluenceMavenPlugin();
		
		Confluence confluence = null;
		try {
			confluence = new Confluence(endpoint);
			confluence.login(server.getUsername(), server.getPassword());

			plugin.deploy(confluence, spaceKey, outputDirectory, readmePageId);
		} catch (MalformedURLException e) {
			throw new MojoExecutionException("Malformed URL of confluence", e);
		} catch (DeployException e) {
			throw new MojoExecutionException("Unable to deploy to confluence", e);
		} catch (Exception e) {
			throw new MojoExecutionException("Unable to login to confluence", e);
		} finally {
			if (confluence != null) {
				try { confluence.logout(); } 
				catch (Exception ignore) { }
			}
		}
	}

}
