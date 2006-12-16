/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.shared.test.plugin;

import java.io.File;

import org.apache.maven.project.MavenProject;

/**
 * Test tool that provides a single point of access for staging a plugin artifact - along with its
 * POM lineage - into a clean test-time local repository. This involves modifying the plugin POM to 
 * provide a stable test-time version for test-build POMs to reference, then installing the plugin
 * jar and associated POMs (including those ancestors that are reachable using &lt;relativePath&gt;)
 * into the test local repository.
 * 
 * <p>
 * <b>WARNING:</b> Currently, the <code>RepositoryTool</code> will not 
 * resolve parent POMs that exist <b>only</b> in your normal local repository, and are not reachable 
 * using the relativePath element. This may result in failed test builds, as one or more of the 
 * plugin's ancestor POMs cannot be resolved.
 * </p>
 * 
 * @plexus.component role="org.apache.maven.shared.test.plugin.PluginTestTool" role-hint="default"
 * @author jdcasey
 *
 */
public class PluginTestTool
{
    public static final String ROLE = PluginTestTool.class.getName();

    /**
     * @plexus.requirement role-hint="default"
     */
    private ProjectTool projectTool;

    /**
     * @plexus.requirement role-hint="default"
     */
    private RepositoryTool repositoryTool;

    /**
     * Stage the plugin, using a stable version, into a temporary local-repository directory that is
     * generated by this method. When the plugin is staged, return the local repository base directory
     * for use in test builds.
     * 
     * @param testVersion The test version for the plugin, used for reference in test-build POMs and
     *   fully-qualified goals
     * @return The base-directory location of the generated local repository
     */
    public File preparePluginForIntegrationTesting( String testVersion )
        throws TestToolsException
    {
        return prepareForTesting( testVersion, false, null );
    }

    /**
     * Stage the plugin, using a stable version, into a temporary local-repository directory that is
     * generated by this method. When the plugin is staged, return the local repository base directory
     * for use in test builds. This method also skips unit testing during plugin jar production, 
     * since it is assumed that executing these tests would lead to a recursive test-and-build loop.
     * 
     * @param testVersion The test version for the plugin, used for reference in test-build POMs and
     *   fully-qualified goals
     * @return The base-directory location of the generated local repository
     */
    public File preparePluginForUnitTestingWithMavenBuilds( String testVersion )
        throws TestToolsException
    {
        return prepareForTesting( testVersion, true, null );
    }

    /**
     * Stage the plugin, using a stable version, into the specified local-repository directory. 
     * When the plugin is staged, return the local repository base directory for verification.
     * 
     * @param testVersion The test version for the plugin, used for reference in test-build POMs and
     *   fully-qualified goals
     * @param localRepositoryDir The base-directory location of the test local repository, into which
     *   the plugin's test version should be staged.
     * @return The base-directory location of the generated local repository
     */
    public File preparePluginForIntegrationTesting( String testVersion, File localRepositoryDir )
        throws TestToolsException
    {
        return prepareForTesting( testVersion, false, localRepositoryDir );
    }

    /**
     * Stage the plugin, using a stable version, into the specified local-repository directory. 
     * When the plugin is staged, return the local repository base directory for verification. This 
     * method also skips unit testing during plugin jar production, since it is assumed that 
     * executing these tests would lead to a recursive test-and-build loop.
     * 
     * @param testVersion The test version for the plugin, used for reference in test-build POMs and
     *   fully-qualified goals
     * @param localRepositoryDir The base-directory location of the test local repository, into which
     *   the plugin's test version should be staged.
     * @return The base-directory location of the generated local repository
     */
    public File preparePluginForUnitTestingWithMavenBuilds( String testVersion, File localRepositoryDir )
        throws TestToolsException
    {
        return prepareForTesting( testVersion, true, localRepositoryDir );
    }

    private File prepareForTesting( String testVersion, boolean skipUnitTests, File localRepositoryDir )
        throws TestToolsException
    {
        File pomFile = new File( "pom.xml" );
        File buildLog = new File( "target/test-build-logs/setup.build.log" );
        File localRepoDir = localRepositoryDir;

        if ( localRepoDir == null )
        {
            localRepoDir = new File( "target/test-local-repository" );
        }

        MavenProject project = projectTool.packageProjectArtifact( pomFile, testVersion, skipUnitTests, buildLog );
        repositoryTool.createLocalRepositoryFromPlugin( project, localRepoDir );

        return localRepoDir;
    }

}
